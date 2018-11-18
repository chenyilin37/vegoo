package vegoo.stockdata.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
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

import com.google.common.base.Strings;

import vegoo.commons.DateUtil;
import vegoo.commons.NumUtil;
import vegoo.stockcommon.bo.GdhsService;
import vegoo.stockcommon.bo.JgccService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.SdltgdService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.utils.StockUtil;

@Component (
	immediate = true, 
	configurationPid = "stockdata.tdxdata",
	property = {
	    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 0 6,19 * * ?",   // 静态信息，每天7，8，18抓三次
	} 
)
public class ExportTdxDataJob extends ExportDataJob implements Job, ManagedService{
	private static final Logger logger = LoggerFactory.getLogger(ExportTdxDataJob.class);
	private static final String rootPath ="tdx-data";
	
	@Reference private StockService dbStock;
	@Reference private GdhsService dbGdhs;
	@Reference private KDayService dbKDay;
	@Reference private JgccService dbJgcc;
	@Reference private SdltgdService dbT10;
	
	private int topN = 20;
	private Future<?> futureGrabbing;
	
    @Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
/*		String tn = (String)properties.get("TOPN");
		if(!Strings.isNullOrEmpty(tn)) {
			try {
				topN = Integer.parseInt(tn);
			}catch(Exception e) {
			}
		}
*/		
		futureGrabbing = asyncExecute(new Runnable() {

			@Override
			public void run() {
				try {
					exportTdxData();
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}finally {
				   futureGrabbing = null;
				}
			}});
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
		exportTdxData();
	}
	
	private void exportTdxData() throws Exception{
		exportGdhsData();   // 股东户数数据
		exportJgccData();   // 机构持仓数据；
		exorptT10Data();    //十大流通股东
		
		//exportBlockData(); // 自定义板块数据； 

		//exportCustomData();
		
	}

	private void exportGdhsData() throws IOException {
		exportSerialGdhs();

		Date today = new Date();
		
		exportReportGdhs(today, 0);
		exportLatestGdhs(today, 0);
	}

	private void exportSerialGdhs() throws IOException {
		export2File(rootPath, "股东-股东户数", (fw)->{readSerialGdhs(fw);});
	}
	
/*
	private void exportSerialGdhs() throws IOException {
		String fileName = "股东-股东户数";
		
		boolean successed = false;
		File file = getFile("."+fileName, rootPath);
		FileWriter fw = new FileWriter(file);
		try {
			readSerialGdhs(fw);
			successed = true;
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			fw.close();
		}
		
		if(successed) {
			File fileOk = getFile(fileName, rootPath);
			if(fileOk.exists()) {
			  fileOk.delete();
			}
			file.renameTo(fileOk);
		}
		
	}*/
	
	private void readSerialGdhs(FileWriter fw) throws IOException {
		
		readSerialGdhs(StockService.MARKET_SZ, fw);
		readSerialGdhs(StockService.MARKET_SH, fw);
		
	}

	
	private void readSerialGdhs(int marketId, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			readSerialGdhsOfStock(marketId, stkcode, fw);
		}
	}

	//SELECT CONCAT_WS('|', left(d.stockcode,1)='6', d.stockcode, DATE_FORMAT(if(d.endtradedate is null, d.enddate, d.endtradedate),'%Y%m%d'), d.HolderNum)
	private void readSerialGdhsOfStock(int marketId, String stkcode, FileWriter fw) throws IOException {
		Set<String> sdates = dbGdhs.getEndDates(stkcode);
		for(String sdate:sdates) {
			Date transDate = getNearestDateOfKDay(stkcode, sdate);
			if (transDate==null) {
				continue;
			}
			
			long hdnum = dbGdhs.getHolderNum(stkcode, sdate);
			
			int mid = toTdxMarkert(marketId);
			
			String d = DateUtil.formatDateTime(transDate, "yyyyMMdd");
			
			String item = String.format("%d|%s|%s|%d\n", mid,stkcode, d, hdnum );
			
			fw.write(item);
		}
	}
	
	private Date getNearestDateOfKDay(String stockCode, String sdate)  {		
		try {
			Date date = DateUtil.parseDate(sdate);
			return getNearestDateOfKDay(stockCode, date);
		} catch (ParseException e) {
			return null;
		}
	}
	
	private Date getNearestDateOfKDay(String stockCode, Date date)  {
		return dbKDay.getLatestTradeDate(stockCode, date);
	}
	
	static private int toTdxMarkert(int marketId) {
		return  marketId==1 ? 1 : 0;
	}

	private void exportReportGdhs(Date date, int index) throws IOException {
		export2File(rootPath, "股东-本季增减", (fw)->{
			Date curReportDate = StockUtil.getReportDate(date, index);
			Date prevReportDate = StockUtil.getReportDate(date, index-1);
			readReportGdhs(curReportDate, prevReportDate, fw);
			});
	}

/*	private void exportReportGdhs(Date date, int index) throws IOException {

		File file = getFile("股东-本季增减", rootPath);

		FileWriter fw = new FileWriter(file);
		try {
			Date curReportDate = StockUtil.getReportDate(date, index);
			Date prevReportDate = StockUtil.getReportDate(date, index-1);
			readReportGdhs(curReportDate, prevReportDate, fw);
		} finally {
			fw.close();
		}
	}
*/
	private void readReportGdhs(Date curReportDate, Date prevReportDate, FileWriter fw) throws IOException {
		readReportGdhs(StockService.MARKET_SZ, curReportDate, prevReportDate, fw);
		readReportGdhs(StockService.MARKET_SH, curReportDate, prevReportDate, fw);
	}

	private void readReportGdhs(int marketId, Date curReportDate, Date prevReportDate, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			readChangeOfGdhs(marketId, stkcode, curReportDate, prevReportDate, fw);
		}
	}

	private void readChangeOfGdhs(int marketId, String stkcode, Date curReportDate, Date prevReportDate, FileWriter fw) throws IOException {
		// dbGdhs.existGDHS(stkcode, curReportDate)
		
		long curhdnum = dbGdhs.getHolderNum(stkcode, curReportDate);
		long prevhdnum = dbGdhs.getHolderNum(stkcode, prevReportDate);
		
		if(curhdnum==0 || prevhdnum==0) {
			return;
		}
		
		double delta = curhdnum - prevhdnum;
		double rate = delta * 100 / prevhdnum;
		
		if(rate > 10000) { //这么大的放大比例，新发行导致
			return;
		}
		
		int mid = toTdxMarkert(marketId);
		
		String ymd = DateUtil.formatDateTime(curReportDate, "yyyyMMdd");
		
		String item = String.format("%d|%s|%s|%.2f\n", mid,stkcode, ymd, rate);
		
		fw.write(item);
	}

	
	private void exportLatestGdhs(Date today, int index) throws IOException {
		export2File(rootPath, "股东-最新增减", (fw)->{
			Date curReportDate = StockUtil.getReportDate(today, index);
			readLatestGdhs(today, curReportDate, fw);
			});
	}
	
/*	private void exportLatestGdhs(Date today, int index) throws IOException {
		File file = getFile("股东-最新增减", rootPath);
		FileWriter fw = new FileWriter(file);
		try {

			Date curReportDate = StockUtil.getReportDate(today, index);
			
			readLatestGdhs(today, curReportDate, fw);

		} finally {
			fw.close();
		}
		
	}
*/
	private void readLatestGdhs(Date today, Date curReportDate, FileWriter fw) throws IOException {
		readLatestGdhs(StockService.MARKET_SZ, today, curReportDate, fw);
		readLatestGdhs(StockService.MARKET_SH, today, curReportDate, fw);
	}

	private void readLatestGdhs(int marketId, final Date today, final Date curReportDate, FileWriter fw) throws IOException {
		Date prevRDate = StockUtil.getReportDate(curReportDate, -1);
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			//如果本期数据还没有公布，用上期数据；
			Date curRdate = curReportDate;
			if(!dbGdhs.existGDHS(stkcode, curRdate)) {

				if(dbGdhs.existGDHS(stkcode, prevRDate)) {
					curRdate = prevRDate;
				}else {
					continue;
				}
			}

			Date lastRDate = dbGdhs.getLatestEndDate(stkcode, today);
			if(lastRDate==null || !lastRDate.after(curRdate)) {
				// 本期数据公布后，没有新数据
				continue;
			}
			readChangeOfGdhs(marketId, stkcode, lastRDate, curRdate, fw);
		}
	}
	
	private void exportJgccData() throws IOException {
		exportSerialJgcc();          // 系列

		Date today = new Date();
		
		exportCWJgcc(today, 0);      // 仓位
		exportZJJgcc(today, 0);      // 仓位增减
	}

	private void exportSerialJgcc() throws IOException {
		export2File(rootPath, "机构-机构持仓", (fw)->{readSerialJgcc(fw);});
	}
	
/*
 * 	private void exportSerialJgcc() throws IOException {
		File file = getFile("机构-机构持仓", rootPath);
		
		FileWriter fw = new FileWriter(file);
		try {
			readSerialJgcc(fw);
		} finally {
			fw.close();
		}
		
	}*/

	private void readSerialJgcc(FileWriter fw) throws IOException {
		
		readSerialJgcc(StockService.MARKET_SZ, fw);
		readSerialJgcc(StockService.MARKET_SH, fw);
		
	}

	
	private void readSerialJgcc(int marketId, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			readSerialJgcc(marketId, stkcode, fw);
		}
	}

	//SELECT CONCAT_WS('|', left(d.stockcode,1)='6', d.stockcode, DATE_FORMAT(if(d.endtradedate is null, d.enddate, d.endtradedate),'%Y%m%d'), d.HolderNum)
	private void readSerialJgcc(int marketId, String stkcode, FileWriter fw) throws IOException {
		Date publicDate = dbKDay.getEarliestTradeDate(stkcode);
		if(publicDate==null) {
			return;
		}

		Date today = new Date();
		
		Date curRDate = StockUtil.getReportDate(today, 0);
		
		// Date jzDate = DateUtil.add(curRDate, 40);
		
		int i=0;
		while(true) {
			Date rdate = StockUtil.getReportDate(publicDate, ++i);
			if(!rdate.before(today)) {
				break;
			}
			
			if(rdate.equals(curRDate) && !dbJgcc.existJgcc(stkcode, rdate)) {
				if(today.before(getReportPublicDate(stkcode, rdate))) { // 预约披露日期
				  continue;
				}
			}
			
			Date transDate = getNearestDateOfKDay(stkcode, rdate);
			if (transDate==null) {
				continue;
			}
			
			double ltzb = dbJgcc.getLtzb(stkcode, rdate, new int[] {JgccService.JG_JIJIN, 
					JgccService.JG_QFII, JgccService.JG_SHEBAO, 
					JgccService.JG_BAOXIAN}); // 基金JgccService.JG_QUANSHANG,
			
			int mid = toTdxMarkert(marketId);
			
			String ymd = DateUtil.formatDateTime(transDate, "yyyyMMdd");
			
			String item = String.format("%d|%s|%s|%.2f\n", mid, stkcode, ymd, ltzb);
			
			fw.write(item);
		};
	}

	private Date getReportPublicDate(String stkcode, Date rdate) {
		// TODO 查询季报披露日期 + 3
		return DateUtil.addDay(rdate, 40);
	}

	private void exportCWJgcc(Date today, int index) throws IOException {
		export2File(rootPath, "机构-本季仓位", (fw)->{
			Date curReportDate = StockUtil.getReportDate(today, index);
			readCWJgcc(curReportDate, fw);
			});
	}
	
/*	private void exportCWJgcc(Date today, int index) throws IOException {
		File file = getFile("机构-本季仓位", rootPath);
		FileWriter fw = new FileWriter(file);
		try {
			Date curReportDate = StockUtil.getReportDate(today, index);

			readCWJgcc(curReportDate, fw);
		} finally {
			fw.close();
		}
	}*/

	private void readCWJgcc(Date rDate, FileWriter fw) throws IOException {
		readCWJgcc(StockService.MARKET_SZ, rDate, fw);
		readCWJgcc(StockService.MARKET_SH, rDate, fw);
	}

	private void readCWJgcc(int marketId, Date rDate, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			readCWJgcc(marketId, stkcode, rDate, fw);
		}
	}

	private void readCWJgcc(int marketId, String stkcode, Date rDate, FileWriter fw) throws IOException {
		if(!dbJgcc.existJgcc(stkcode, rDate)) {
			return;
		}

		double ltzb = dbJgcc.getLtzb(stkcode, rDate, new int[] {JgccService.JG_JIJIN, 
				JgccService.JG_QFII, JgccService.JG_SHEBAO, 
			    JgccService.JG_BAOXIAN}); // 基金,JgccService.JG_QUANSHANG,
		
		
		int mid = toTdxMarkert(marketId);
		
		String ymd = DateUtil.formatDateTime(rDate, "yyyyMMdd");
		
		String item = String.format("%d|%s|%s|%.2f\n", mid,stkcode, ymd, ltzb);
		
		fw.write(item);
	}
	
	private void exportZJJgcc(Date today, int index) throws IOException {
		export2File(rootPath, "机构-仓位增减", (fw)->{
			Date curReportDate = StockUtil.getReportDate(today, index);
			Date prevReportDate = StockUtil.getReportDate(today, index-1);
			exportZJJgcc(curReportDate, prevReportDate, fw);
			});
	}
/*
	private void exportZJJgcc(Date today, int index) throws IOException {
		
		File file = getFile("机构-仓位增减", rootPath);
		FileWriter fw = new FileWriter(file);
		try {
			Date curReportDate = StockUtil.getReportDate(today, index);

			Date prevReportDate = StockUtil.getReportDate(today, index-1);

			exportZJJgcc(curReportDate, prevReportDate, fw);

		} finally {
			fw.close();
		}
		
	}*/

	private void exportZJJgcc(Date rDate, Date prevRDate, FileWriter fw) throws IOException {
		exportZJJgcc(StockService.MARKET_SZ, rDate, prevRDate, fw);
		exportZJJgcc(StockService.MARKET_SH, rDate, prevRDate, fw);
	}

	private void exportZJJgcc(int marketId, Date rDate, Date prevRDate, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			exportZJJgcc(marketId, stkcode, rDate, prevRDate, fw);
		}
	}

	private void exportZJJgcc(int marketId, String stkcode, Date rDate, Date prevRDate, FileWriter fw) throws IOException {
		if(!dbJgcc.existJgcc(stkcode, rDate)||!dbJgcc.existJgcc(stkcode, prevRDate)) {
			return;
		}

		double ltzb1 = dbJgcc.getLtzb(stkcode, rDate, new int[] {JgccService.JG_JIJIN, 
				JgccService.JG_QFII, JgccService.JG_SHEBAO, 
				JgccService.JG_BAOXIAN}); // 基金 JgccService.JG_QUANSHANG,

		double ltzb2 = dbJgcc.getLtzb(stkcode, prevRDate, new int[] {JgccService.JG_JIJIN, 
				JgccService.JG_QFII, JgccService.JG_SHEBAO, 
				JgccService.JG_BAOXIAN}); // 基金JgccService.JG_QUANSHANG,
		
		if(ltzb1==0 && ltzb2==0) {
			return;
		}
		
		double ltzb = ltzb1 - ltzb2;
		
		int mid = toTdxMarkert(marketId);
		
		String ymd = DateUtil.formatDateTime(rDate, "yyyyMMdd");
		
		String item = String.format("%d|%s|%s|%.2f\n", mid,stkcode, ymd, ltzb);
		
		fw.write(item);
	}
	
	private void exorptT10Data() throws IOException {
		export2File(rootPath, "T10-持仓", (fw)->{exportSerialT10(fw);});
		export2File(rootPath, "T10-增减", (fw)->{exportZJT10(fw);});
	}

/*	private void exportSerialT10() throws IOException {
		File file = getFile("T10-持仓", rootPath);
		
		FileWriter fw = new FileWriter(file);
		try {
			readSerialT10(fw);
		} finally {
			fw.close();
		}
	}
*/
	private void exportSerialT10(FileWriter fw) throws IOException {
		
		exportSerialT10(StockService.MARKET_SZ, fw);
		exportSerialT10(StockService.MARKET_SH, fw);
	}

	
	private void exportSerialT10(int marketId, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			exportSerialT10(marketId, stkcode, fw);
		}
	}

	//SELECT CONCAT_WS('|', left(d.stockcode,1)='6', d.stockcode, DATE_FORMAT(if(d.endtradedate is null, d.enddate, d.endtradedate),'%Y%m%d'), d.HolderNum)
	private void exportSerialT10(int marketId, String stkcode, FileWriter fw) throws IOException {
		Date publicDate = dbKDay.getEarliestTradeDate(stkcode);
		if(publicDate==null) {
			return;
		}
		Date today = new Date();
		Date curRDate = StockUtil.getReportDate(today, 0);
		int i=0;
		while(true) {
			Date rdate = StockUtil.getReportDate(publicDate, ++i);
			if(!rdate.before(today)) {
				break;
			}
			
			if(rdate.equals(curRDate) && !dbT10.existTop10(stkcode, rdate)) {
				if(today.before(getReportPublicDate(stkcode, rdate))) { // 预约披露日期
				  continue;
				}
			}
			
			Date transDate = getNearestDateOfKDay(stkcode, rdate);
			if (transDate==null) {
				continue;
			}
			
			double zzb = dbT10.sumZzbOfTopN(stkcode, rdate, topN); // 基金
			
			int mid = toTdxMarkert(marketId);
			
			String d = DateUtil.formatDateTime(transDate, "yyyyMMdd");
			
			String item = String.format("%d|%s|%s|%.2f\n", mid,stkcode, d, zzb);
			
			fw.write(item);
		}
	}
	
	private void exportZJT10(FileWriter fw) throws IOException {
		Date today = new Date();
		
		Date curRDate = StockUtil.getReportDate(today, 0);
		Date prevRDate = StockUtil.getReportDate(today, -1);
		
		exportZJT10(curRDate, prevRDate, fw);
	}

	private void exportZJT10(Date curRDate, Date prevRDate, FileWriter fw) throws IOException {
		exportZJT10(StockService.MARKET_SZ, curRDate, prevRDate,fw);
		exportZJT10(StockService.MARKET_SH, curRDate, prevRDate,fw);
	}

	private void exportZJT10(int marketId, Date curRDate, Date prevRDate, FileWriter fw) throws IOException {
		Set<String> stockCodes = dbStock.getStockCodes(marketId);
		for (String stkcode : stockCodes) {
			if(!dbT10.existTop10(stkcode, curRDate) || !dbT10.existTop10(stkcode, prevRDate)) {
				  continue;
			}
			
			double zzb0 = dbT10.sumZzbOfTopN(stkcode, curRDate, topN); 
			double zzb1 = dbT10.sumZzbOfTopN(stkcode, prevRDate, topN); 
			
			int mid = toTdxMarkert(marketId);
			
			String d = DateUtil.formatDateTime(curRDate, "yyyyMMdd");
			
			String item = String.format("%d|%s|%s|%.2f\n", mid,stkcode, d, zzb0-zzb1);
			
			fw.write(item);		
		}
	}

		
}