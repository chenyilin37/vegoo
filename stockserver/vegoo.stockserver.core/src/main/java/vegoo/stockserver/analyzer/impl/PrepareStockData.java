package vegoo.stockserver.analyzer.impl;

import java.sql.Types;
import java.text.ParseException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.commons.DateUtil;
import vegoo.commons.jdbc.JdbcService;
import vegoo.stockcommon.bo.GdhsService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.utils.BaseJob;
import vegoo.stockcommon.utils.StockUtil;

@Component (
		immediate = true, 
		configurationPid = "stockserver.preparedata",
		property = {
		    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 */1 * * * ?",   // 静态信息，每天7，8，18抓三次
		} 
	)
public class PrepareStockData  extends BaseJob implements Job, ManagedService{
	private static final Logger logger = LoggerFactory.getLogger(PrepareStockData.class);
	
	@Reference private KDayService dbKDay;
	@Reference private StockService dbStock;
	// @Reference private GdhsService dbGdhs;

	@Reference private JdbcService db;

	private Future<?> futureGrabbing;

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		futureGrabbing = asyncExecute(new Runnable() {
			@Override
			public void run() {
				try {
					prepareAllData();
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

		// Date today = new Date();
		
		prepareData(StockUtil.getReportDate());
	}


	private void prepareAllData()  {
		Date today = new Date();

		for(int i=0;i<10*5;++i) {
			Date rDate = StockUtil.getReportDate(today, -i);
			prepareData(rDate);
		}
	}
	
	private void prepareData(Date reportDate) {
		prepareDataOfZF0(reportDate);
		propareDataOfZFN(reportDate, 2);
	}
	
	private void prepareDataOfZF0(Date rDate) {
		Set<String> stockCodes = dbStock.getStockCodes(/*reportDate*/);		
		stockCodes.forEach((stockCode)->{
			prepareDataOfZF0(stockCode, rDate);
		});
	}

	private void prepareDataOfZF0(String stockCode, Date reportDate) {
		Date prevDate = StockUtil.getReportDate(reportDate, -1);
		try {
			double zf = dbKDay.getZhangFu(stockCode, prevDate, reportDate);
			double[] zfms = dbKDay.getMaxMinZhangFu(stockCode, prevDate, reportDate);
			if(!existItem(stockCode, reportDate)) {
				InsertDataOfZF(stockCode, reportDate, 0, zf, zfms[0], zfms[1]);
			}else {
				updateDataOfZF(stockCode, reportDate, 0, zf, zfms[0], zfms[1]);
			}
		}catch(Exception e) {
			logger.error("",e);
		}
	}
	
	private static final String EXIST_ZF = "select 1 from StockRZF where scode=? and rdate=?";
	private boolean existItem(String stockCode, Date reportDate) {
		try {
			Integer result = db.queryForObject(EXIST_ZF, 
					new Object[] {stockCode, reportDate}, 
					new int[] {Types.VARCHAR, Types.DATE}, 
					Integer.class);
			return result!=null;
		}catch(Exception e) {
			return false;
		}
	}

	private void propareDataOfZFN(Date reportDate, int N)  {
		try {
			Date today = DateUtil.parseDate( DateUtil.formatDate(new Date())); // 取整
			
			for(int i=1; i<=N; ++i) {
				Date nextDate = StockUtil.getReportDate(reportDate, i);
				if(!nextDate.before(today)) {
					break;
				}
				propareDataOfZFN(reportDate, nextDate, i);
			}
		}catch(Exception e) {
			
		}
	}
	
	private void propareDataOfZFN( Date reportDate, Date nextDate, int n ) {
		Set<String> stockCodes = dbStock.getStockCodes(/*reportDate*/);		
		stockCodes.forEach((stockCode)->{
			try {
				double zf = dbKDay.getZhangFu(stockCode, reportDate, nextDate);
				double[] zfms = dbKDay.getMaxMinZhangFu(stockCode, reportDate, nextDate);
				updateDataOfZF(stockCode, reportDate, n, zf, zfms[0], zfms[1]);
			}catch(Exception e) {
				logger.error("", e);
			}
		});
	}

	private static final String INS_ZF = "insert into StockRZF(SCode, RDate, ZF0, ZFX0, ZFN0)values(?,?,?,?,?)";
	private void InsertDataOfZF(String stockCode, Date reportDate, int i, double zf, double maxZf, double minZf) {
		db.update(INS_ZF, 
				new Object[] {stockCode, reportDate, zf, maxZf, minZf}, 
				new int[] {Types.VARCHAR,Types.DATE,Types.DOUBLE,Types.DOUBLE,Types.DOUBLE});
	}

	private static final String UPD_ZF = "update StockRZF set ZF%1$d=?,ZFX%1$d=?,ZFN%1$d=? where SCode=? AND RDate=?";
	private void updateDataOfZF(String stockCode, Date reportDate, int N, double zf, double maxZf, double minZf) {
		String sql = String.format(UPD_ZF, N);
		db.update(sql, 
				new Object[] {zf, maxZf, minZf,stockCode, reportDate}, 
				new int[] {Types.DOUBLE,Types.DOUBLE,Types.DOUBLE,Types.VARCHAR,Types.DATE});
	}
	
}
