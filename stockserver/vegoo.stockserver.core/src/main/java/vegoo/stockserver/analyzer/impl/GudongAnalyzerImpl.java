package vegoo.stockserver.analyzer.impl;

import java.sql.Types;
import java.util.Date;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.commons.jdbc.JdbcService;
import vegoo.stockcommon.bo.JgccmxService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.utils.BaseJob;
import vegoo.stockcommon.utils.StockUtil;
import vegoo.stockserver.analyzer.GudongAnalyzer;

/*
 * 帅选出实力股东
 */
@Component (
		immediate = true,
		configurationPid = "stockserver.analyzer.gd"
)
public class GudongAnalyzerImpl extends BaseJob implements GudongAnalyzer, ManagedService{
	private static final Logger logger = LoggerFactory.getLogger(GudongAnalyzerImpl.class);
	
	@Reference private KDayService dbKDay;
	@Reference private StockService dbStock;

	@Reference private JdbcService db;
	@Reference private JgccmxService dbJgccmx;

	private Future<?> futureGrabbing;
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		futureGrabbing = asyncExecute(new Runnable() {
			@Override
			public void run() {
				try {
					propareAllData();
				} catch (Exception e) {
					logger.error("",e);
				}finally {
				   futureGrabbing = null;
				}
			}
		});		
	}

	@Override
	protected void executeJob(JobContext context) throws Exception {
		if(futureGrabbing != null) {
			if(futureGrabbing.isDone() || futureGrabbing.isCancelled()) {
			  futureGrabbing = null;
			}else {
			  return;
			}
		}
		
		Date rDate = StockUtil.getReportDate(new Date(), 0);
		prepareData(rDate);		
	}
	
	private void propareAllData() {
		Date today = new Date();
		
		for(int i=0;i<4*5;++i) {
			Date rDate = StockUtil.getReportDate(today, -i);
			prepareData(rDate);
		}
	}
	
	private void prepareData(Date reportDate) {
		Set<String> stockCodes = getStockCodes(reportDate);
		stockCodes.forEach((stockCode)->{
			Set<String> gudongCodes = getGudongCodes(stockCode, reportDate);
			propareData(stockCode, reportDate, gudongCodes);
		});
	}
	
	private void propareData(String stockCode, Date reportDate,Set<String> gudongCodes) {
		gudongCodes.forEach((gdCode)->{
			propareDataOfPrev(stockCode, reportDate, gdCode);
		});
	}

	private void propareDataOfPrev(String stockCode, Date reportDate, String gdCode) {
		Date prevDate = StockUtil.getReportDate(reportDate, -1);
		double ltzb0 = dbJgccmx.getLtzb(stockCode, gdCode, reportDate);
		double zzb0 = dbJgccmx.getZzb(stockCode, gdCode, reportDate);
		double amount0 = dbJgccmx.getLtsz(stockCode, gdCode, reportDate);
		double ltzb1 = dbJgccmx.getLtzb(stockCode, gdCode, prevDate);
		
		double deltaLtzb = ltzb0 - ltzb1;
		double deltaValue = 0;

		if(ltzb0>0) {
			deltaValue = deltaLtzb*amount0/ltzb0;
		}else {
			deltaValue = - dbJgccmx.getLtsz(stockCode, gdCode, prevDate);
		}
		if(existJgccmxa(stockCode, reportDate, gdCode)) {
			updatePrevData(stockCode, reportDate, gdCode, deltaLtzb, deltaValue);
		}else {
			insertJgccmxData(stockCode, reportDate, gdCode, amount0, ltzb0, zzb0, deltaLtzb, deltaValue);
		}
	}
	
	private boolean existJgccmxa(String stockCode, Date reportDate, String gdCode) {
		try {
			String sql = "select 1 from jgccmxa where SCode=? AND RDate=? AND SHCode=?";
			Integer result = db.queryForObject(sql, 
					new Object[] {stockCode, reportDate, gdCode},
					new int[] {Types.VARCHAR,Types.DATE,Types.VARCHAR},
					Integer.class);
			return result != null && result==1;
		}catch(Exception e) {
			return false;
		}
	}

	private static final String NEW_JGCCMXA = "insert into jgccmxa(SCode,RDate,SHCode, Vposition, LTZB, ZZB,ChangeLTZB, ChangeValue)values(?,?,?,?,?,?,?,?)";
	private void insertJgccmxData(String stockCode, Date reportDate, String gdCode, double Vposition,double LTZB,double ZZB, double deltaLtzb, double deltaValue) {
		try {
			db.update(NEW_JGCCMXA, 
					new Object[] {stockCode, reportDate, gdCode, Vposition, LTZB, ZZB, deltaLtzb, deltaValue }, 
					new int[] {Types.VARCHAR,Types.DATE,Types.VARCHAR,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE});
		}catch(Exception e) {
			logger.error("",e);
		}
	}

	private static final String UPD_PRVDATA = "update jgccmxa set ChangeLTZB=?, ChangeValue=? where SCode=? AND RDate=? AND SHCode=?";
	private void updatePrevData(String stockCode, Date reportDate, String gdCode, double deltaLtzb, double deltaValue) {
		try {
		db.update(UPD_PRVDATA, 
				new Object[] {deltaLtzb, deltaValue, stockCode, reportDate, gdCode}, 
				new int[] {Types.DOUBLE,Types.DOUBLE,Types.VARCHAR,Types.DATE,Types.VARCHAR});
		}catch(Exception e) {
			logger.error("",e);
		}
	}
	
	private Set<String> getGudongCodes(String stockCode, Date reportDate) {
		return dbJgccmx.getGudongCodes(stockCode, reportDate);
	}

	private Set<String> getStockCodes(Date reportDate) {
		return dbJgccmx.getStockCodes(reportDate);
	}
	

}
