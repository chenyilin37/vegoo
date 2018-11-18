package vegoo.stockserver.analyzer.impl;

import java.sql.Types;
import java.text.ParseException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.karaf.scheduler.JobContext;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.commons.jdbc.JdbcService;
import vegoo.stockcommon.bo.JgccService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.utils.BaseJob;
import vegoo.stockcommon.utils.StockUtil;
import vegoo.stockserver.analyzer.JgccAnalyzer;


@Component (
		immediate = true,
		configurationPid = "stockserver.analyzer.jgcc",
		property = {
			    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 */1 * * * ?",   // 静态信息，每天7，8，18抓三次
			} 
)

public class JgccAnalyzerImpl extends BaseJob implements JgccAnalyzer, ManagedService {
	private static final Logger logger = LoggerFactory.getLogger(JgccAnalyzerImpl.class);
	
	@Reference private KDayService dbKDay;
	@Reference private StockService dbStock;
	@Reference private JgccService dbJgcc;

	@Reference private JdbcService db;

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
		propareData(rDate, true);		
	}


	private void propareAllData() {
		Date today = new Date();
		
		for(int i=0;i<4*5;++i) {
			Date rDate = StockUtil.getReportDate(today, -i);
			propareData(rDate, i==0);
		}
	}
	
	private void propareData(Date reportDate, boolean isCurrent) {
		Set<String> stockCodes = dbStock.getStockCodes(); //reportDate
		stockCodes.forEach((stockCode)->{
			propareData(stockCode,reportDate,isCurrent);
		});
	}

	private void propareData(String stockCode, Date reportDate, boolean isCurrent) {
		for(int i=1;i<=6;++i) {
			propareData(stockCode, reportDate, i, isCurrent);
		}
	}
	
	private void propareData(String stockCode, Date reportDate, int jglx, boolean isCurrent) {
		Date prevDate = StockUtil.getReportDate(reportDate, -1);
		

		double ltzb0 = dbJgcc.getLtzb(stockCode, reportDate, jglx);
		double amount0 = dbJgcc.getAmount(stockCode, reportDate, jglx);
		
		double ltzb1 = dbJgcc.getLtzb(stockCode, prevDate, jglx);

		if(ltzb0 < 0.01 && ltzb1<0.01) { //小于一手的数据抛掉
			return;
		}
		
		double deltaLtzb = ltzb0 - ltzb1;
		double deltaAmount = 0;
		if(ltzb0>0) {
			deltaAmount = amount0 * deltaLtzb/ltzb0;
		}else {
			deltaAmount = - dbJgcc.getAmount(stockCode, prevDate, jglx);
		}
		
		if(existJGCC(stockCode, reportDate, jglx)) {
			if(isCurrent) {
				double vol0 = dbJgcc.getVolume(stockCode, reportDate, jglx);
				double count0 = dbJgcc.getCount(stockCode, reportDate, jglx);
				updateJGCC(stockCode, reportDate, jglx, count0, vol0, amount0, ltzb0, ltzb1, deltaLtzb, deltaAmount);
			}else {
				updateJGCC(stockCode, reportDate, jglx, ltzb1, deltaLtzb, deltaAmount);
			}
		}else {
			double vol0 = dbJgcc.getVolume(stockCode, reportDate, jglx);
			double count0 = dbJgcc.getCount(stockCode, reportDate, jglx);
			insertJGCC(stockCode, reportDate, jglx, count0, vol0, amount0, ltzb0, ltzb1, deltaLtzb, deltaAmount);
		}
	}

	private void updateJGCC(String stockCode, Date reportDate, int jglx, double count0, double vol0, double amount0,
			double ltzb0, double ltzb1, double deltaLtzb, double deltaAmount) {
		
	}

	private static final String EXIST_JGCC = "select 1 from JGCCA where scode=? and rdate=? and lx=?";
	private boolean existJGCC(String stockCode, Date reportDate, int jglx) {
		try {
			Integer result = db.queryForObject(EXIST_JGCC, 
					new Object[] {stockCode, reportDate, jglx}, 
					new int[] {Types.VARCHAR, Types.DATE, Types.INTEGER}, 
					Integer.class);
			return result!=null;
		}catch(Exception e) {
			return false;
		}
	}

	private static final String INS_JGCC = "insert into JGCCA(SCode, RDate, lx, Count, ShareHDNum,VPosition,LTZB,PrevLTZB,ChangeAmount,ChangeLTZB)values(?,?,?,?,?,?,?,?,?,?)";
	private void insertJGCC(String stockCode, Date reportDate, int jglx, double count, double volume, double amount, double ltzb, 
			double prevLtzb, double deltaLtzb, double deltaAmount) {
		
		db.update(INS_JGCC, 
				new Object[] {stockCode, reportDate, jglx, count, volume, amount, ltzb, prevLtzb, deltaAmount, deltaLtzb}, 
				new int[] {Types.VARCHAR,Types.DATE,Types.INTEGER, 
						   Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE});
	}

	private static final String UPD_JGCC = "update JGCCA set PrevLTZB=?,ChangeAmount=?,ChangeLTZB=? where SCode=? AND RDate=? and lx=?";
	private void updateJGCC(String stockCode, Date reportDate, int jglx, 
			double prevLtzb,double deltaLtzb, double deltaAmount) {
		db.update(UPD_JGCC, 
				new Object[] {prevLtzb, deltaAmount, deltaLtzb,stockCode, reportDate, jglx}, 
				new int[] {Types.DOUBLE,Types.DOUBLE,Types.VARCHAR,Types.DATE,Types.DOUBLE,Types.INTEGER});
	}


	
}
