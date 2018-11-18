package vegoo.stockdata.crawler.eastmoney.gdhs;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.JobContext;
import org.apache.karaf.scheduler.Scheduler;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import vegoo.commons.JsonUtil;
import vegoo.stockcommon.bo.GdhsService;
import vegoo.stockcommon.bo.GudongService;
import vegoo.stockcommon.bo.SdltgdService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.dao.GdhsDao;
import vegoo.stockdata.core.HttpClient;
import vegoo.stockdata.core.HttpRequestException;
import vegoo.stockdata.crawler.core.BaseGrabJob;
import vegoo.stockdata.crawler.core.ReportDataGrabJob;


/*
 * 这些星号由左到右按顺序代表 ：     *    *    *    *    *    *   *     
                         格式： [秒] [分] [小时] [日] [月] [周] [年] 
 * 时间大小由小到大排列，从秒开始，顺序为 秒，分，时，天，月，年    *为任意 ？为无限制。 由此上面所配置的内容就是，在每天的16点26分启动buildSendHtml() 方法
	
	具体时间设定可参考
	"0/10 * * * * ?" 每10秒触发
	"0 0 12 * * ?" 每天中午12点触发 
	"0 15 10 ? * *" 每天上午10:15触发 
	"0 15 10 * * ?" 每天上午10:15触发 
	"0 15 10 * * ? *" 每天上午10:15触发 
	"0 15 10 * * ? 2005" 2005年的每天上午10:15触发 
	"0 * 14 * * ?" 在每天下午2点到下午2:59期间的每1分钟触发 
	"0 0/5 14 * * ?" 在每天下午2点到下午2:55期间的每5分钟触发 
	"0 0/5 14,18 * * ?" 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发 
	"0 0-5 14 * * ?" 在每天下午2点到下午2:05期间的每1分钟触发 
	"0 10,44 14 ? 3 WED" 每年三月的星期三的下午2:10和2:44触发 
	"0 15 10 ? * MON-FRI" 周一至周五的上午10:15触发 
	"0 15 10 15 * ?" 每月15日上午10:15触发 
	"0 15 10 L * ?" 每月最后一日的上午10:15触发 
	"0 15 10 ? * 6L" 每月的最后一个星期五上午10:15触发 
	"0 15 10 ? * 6L 2002-2005" 2002年至2005年的每月的最后一个星期五上午10:15触发 
	"0 15 10 ? * 6#3" 每月的第三个星期五上午10:15触发
	"0 0 06,18 * * ?"  在每天上午6点和下午6点触发 
	"0 30 5 * * ? *"   在每天上午5:30触发
	"0 0/3 * * * ?"    每3分钟触发
 */

@Component (
	immediate = true, 
	configurationPid = "stockdata.grab.gdhs",
	service = { Job.class,  ManagedService.class},
	property = {
	    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 * 6-23 * * ?",   // 每小时更新一次
	    // Scheduler.PROPERTY_SCHEDULER_CONCURRENT + "= false"
	} 
) 
public class GrabGdhsJob extends ReportDataGrabJob implements Job, ManagedService{
	/*！！！ Job,ManagedService接口必须在此申明，不能一道父类中，否则，karaf无法辨认，Job无法执行  ！！！*/
	private static final Logger logger = LoggerFactory.getLogger(GrabGdhsJob.class);
	
	// 要抓起数据的季报，用逗号分割，对应报告日期；主要用于初始化
	//private static final String PN_REPORT_DATES = "preload-reports";
	//private static final String PN_URL_REPORT   = "url-report";
	private static final String PN_URL_LATEST   = "url-latest";
	private static final String PN_URL_STOCK   = "url-stock";
	
	//protected static final String TAG_REPORTDATE = "<REPORT_DATE>";
	//protected static final String TAG_PAGENO     = "<PAGE_NO>";
	//protected static final String TAG_STOCKCODE  = "<STOCK_CODE>"; 
	
	private String latestURL ;
	private String stockURL ;
	
    @Reference private GdhsService dbGdhs;
    @Reference private StockService dbStock;
    
    private Future<?> futureGrabbing;
	
    @Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		/* ！！！本函数内不要做需要长时间才能完成的工作，否则，会影响其他BUNDLE的初始化！！！  */

    	this.stockURL = (String) properties.get(PN_URL_STOCK);
		this.latestURL = (String) properties.get(PN_URL_LATEST);

		logger.info("{} = {}", PN_URL_LATEST, latestURL);
		logger.info("{} = {}", PN_URL_STOCK, stockURL);
		
		if(!Strings.isNullOrEmpty(stockURL)) {
			asyncGrab();
		}
	}
	
    private void asyncGrab() {
		futureGrabbing = asyncExecute(new Runnable() {
			@Override
			public void run() {
				try {
				   grabGdhsByStocks();
				}finally {
				   futureGrabbing = null;
				}
			}});
    }
	
	private void grabGdhsByStocks() {
		if(Strings.isNullOrEmpty(stockURL)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_LATEST);
			return;
		}
		
		Set<String> stockCodes = dbStock.getStockCodes();
		for(String stkCode : stockCodes) {
			grabGdhsByStock(stkCode);
		}
	}

	private void grabGdhsByStock(String stkCode) {
		String urlPattern = stockURL.replaceAll(TAG_STOCKCODE, stkCode);
		grabGdhsByPages(urlPattern);		
	}
	
	@Override
	protected void executeJob(JobContext context) {
		if(Strings.isNullOrEmpty(latestURL)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_LATEST);
			return;
		}

		if(futureGrabbing != null) {
			if(futureGrabbing.isDone() || futureGrabbing.isCancelled()) {
			  futureGrabbing = null;
			}else {
			  return;
			}
		}
		
		grabGdhsByPages(latestURL);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int h = cal.get(Calendar.HOUR_OF_DAY);
		if(h<3) { // 深夜再抓历史数据
		    asyncGrab();
		}
	}

	private void grabGdhsByPages(String url) {
		int tatolPages = 0;
		int page = 0;
		while(true) {
		   int pc = grabGdhsByPage(++page, url); 
		   if(pc>tatolPages) {
			  tatolPages = pc;
		   }
		   if( page >= tatolPages) {
			   break;
		   }
		}
	}

	private int grabGdhsByPage(int page, String urlPattern) {
		String url = urlPattern.replaceAll(TAG_PAGENO, String.valueOf(page));
		
		GdhsListDto listDto = requestData(url, GdhsListDto.class, "获取股东户数");

		if(listDto == null) {
			return 0;
		}
				
		saveGdhsData(listDto.getData());
		
		return listDto.getPages();
	}

	private void saveGdhsData(List<GdhsItemDto> items) {
		for(GdhsItemDto item : items) {
			String stkCode = item.getSecurityCode();
			if(Strings.isNullOrEmpty(stkCode)) {
				continue;
			}
			
			stkCode = stkCode.trim();
			if(stkCode.length() !=6 ) {
				logger.info("股票代码错误：{} // {}", stkCode, JsonUtil.toJson(item));
				continue;
			}
			
			if(dbGdhs.existGDHS(stkCode, item.getEndDate())) {
				continue;
			}
			
			GdhsDao dao = new GdhsDao();
			
			dao.setSecurityCode(item.getSecurityCode());
			dao.setEndDate(item.getEndDate());
			dao.setHolderNum(item.getHolderNum());
			dao.setPreviousHolderNum(item.getPreviousHolderNum());
			dao.setHolderNumChange(item.getHolderNumChange());
			dao.setHolderNumChangeRate(item.getHolderNumChangeRate());
			dao.setRangeChangeRate(item.getRangeChangeRate());
			dao.setPreviousEndDate(item.getPreviousEndDate());
			dao.setHolderAvgCapitalisation(item.getHolderAvgCapitalisation());
			dao.setHolderAvgStockQuantity(item.getHolderAvgStockQuantity());
			dao.setTotalCapitalisation(item.getTotalCapitalisation());
			dao.setCapitalStock(item.getCapitalStock());
			dao.setNoticeDate(item.getNoticeDate());
			
			try {
				dbGdhs.insertGdhs(dao);
			}catch(Exception e) {
				logger.error("保存股东户数数据是出错: {}",JsonUtil.toJson(item), e);
			}
		}
	}
	
}