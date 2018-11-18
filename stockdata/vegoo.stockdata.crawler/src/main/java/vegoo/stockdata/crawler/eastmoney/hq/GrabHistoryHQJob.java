package vegoo.stockdata.crawler.eastmoney.hq;

import java.util.ArrayList;
import java.util.Collection;
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

import vegoo.commons.JsonUtil;
import vegoo.stockcommon.bo.BlockService;
import vegoo.stockcommon.bo.FhsgService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.StockService;
import vegoo.stockcommon.dao.KDayDao;
import vegoo.stockcommon.dao.StockCapitalDao;
import vegoo.stockcommon.utils.StockUtil;
import vegoo.stockdata.crawler.core.BaseGrabJob;

@Component (
		immediate = true, 
		configurationPid = "stockdata.grab.historyhq",
		service = { Job.class,  ManagedService.class}, 
		property = {
			//!!!!   历史日线数据只在收盘后抓去, 注意定时  !!!!	
		    Scheduler.PROPERTY_SCHEDULER_EXPRESSION + "= 0 0 0-5/3,16-23/3 * * ?", 
		} 
	)
public class GrabHistoryHQJob extends BaseGrabJob implements Job,ManagedService {
	private static final Logger logger = LoggerFactory.getLogger(GrabHistoryHQJob.class);

	//static final String PN_URL_LIVEDATA   = "url-livedata";
	static final String PN_URL_KDAY   = "url-kday";
	static final String PN_URL_MLINE   = "url-mline";
	
	//private String urlLivedata;
	private String urlKday;
	
    @Reference private StockService dbStock;
    @Reference private KDayService dbHQ;
    @Reference private FhsgService dbFhsg; 
    @Reference private BlockService dbBlock;

    private Future<?> futureGrabbing;

    @Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		/* ！！！本函数内不要做需要长时间才能完成的工作，否则，会影响其他BUNDLE的初始化！！！  */

    	//this.urlLivedata = (String) properties.get(PN_URL_LIVEDATA);
		this.urlKday = (String) properties.get(PN_URL_KDAY);

		//logger.info("{} = {}", PN_URL_LIVEDATA, urlLivedata);
		logger.info("{} = {}", PN_URL_KDAY, urlKday);

		futureGrabbing = asyncExecute(new Runnable() {
			@Override
			public void run() {
				try {
				    grabHistoryKDay();
				}finally {
					futureGrabbing = null;
				}
			}});
	}

	@Override
	protected void executeJob(JobContext context) {
		if((futureGrabbing == null)||(futureGrabbing.isDone() || futureGrabbing.isCancelled())) {
			grabHistoryKDay();
		}
	}
	
	private void grabHistoryKDay() {
		if(Strings.isNullOrEmpty(urlKday)) {
			logger.error("没有在配置文件中设置{}参数！", PN_URL_KDAY);
			return;
		}
		
		List<String> items = new ArrayList<>();

		Collection<String> blockCodes = dbBlock.getBlockUCodes();
		if(blockCodes!=null) {
			items.addAll(blockCodes);
		}
		
		Set<String> stockUCodes = dbStock.getStockUCodes();
		if(stockUCodes!=null) {
			items.addAll(stockUCodes);
		}

		grabHistoryKDay(items);
	}

	private void grabHistoryKDay(List<String> stockUCodes) {
		Date curTransDate = StockUtil.getLastTransDate(true); // 已经收市的最后一个交易日
		for(String stkUcode : stockUCodes) {
			String stockCode = stkUcode.substring(0,stkUcode.length()-1);
			
			Date dbLastDate = dbHQ.getLatestTradeDate(stockCode, curTransDate);
			
			if(dbLastDate==null || dbLastDate.before(curTransDate)) {
			   grabHistoryKDay(stkUcode, dbLastDate);			
			}
		}
	}

	private void grabHistoryKDay(String stkUCode, Date lastDate) {
		String url = urlKday.replaceAll(TAG_STOCKUCODE, stkUCode);  // 
		
		StockKDayDto theDto = requestData(url, StockKDayDto.class, "获取股票基本信息");
		
		if(theDto == null) {
			return ;
		}
		
		processHistoryKDay(url, theDto.getCode(), theDto, lastDate);
	}

	private void processHistoryKDay(String url,String stkcode, StockKDayDto theDto, Date lastDate) {
		saveHistoryKDayFlow(url, stkcode, theDto.getFlow());
		saveHistoryKDayData(url, stkcode, theDto, lastDate);
	}

	private void saveHistoryKDayFlow(String url, String stkcode, StockKDayFlowDto[] flows) {
		if(flows==null || flows.length ==0) {
			logger.error("日线数据格式有变化，没有流通股本数据， URL如下: {}", url);
			return;
		}
		
		for(StockKDayFlowDto dto: flows) {
			if(dbStock.existStockCapital(stkcode, dto.getTime())) {
				continue;
			}
			
			StockCapitalDao dao = new StockCapitalDao();
			
			dao.setStockCode(stkcode);
			dao.setTransDate(dto.getTime());
			dao.setLtg(dto.getLtg());
			
			dbStock.insertStockCapital(dao);
		}
	}

	private void saveHistoryKDayData(String url,String stkcode, StockKDayDto kDto, Date lastDate) {
		List<KDayDao> newItems = getNewKDayData(url,stkcode, kDto, lastDate);
		
		if(newItems==null || newItems.isEmpty()) {
			return;
		}
		dbHQ.saveKDayData(newItems);
	}

	/*
 	// 日期、开盘、收盘、最高、最低、量、额，振幅、换手率
	"2008-05-12,1.62,1.79,1.85,1.62,182125,541669792,-",  
    "2008-05-13,1.80,2.02,2.02,1.79,49982,159771423,12.7%",
    "2008-05-14,2.09,2.07,2.15,1.94,49221,164265608,10.27%",
    */
	private List<KDayDao> getNewKDayData(String url,String stkcode, StockKDayDto kDto, Date lastDate) {
		String[] items = kDto.getData();
		if(items == null) {
			logger.error("日线数据格式或接口可能有变化，没有数据， URL如下: {}", url);
			return null;
		}

		List<KDayDao> result = new ArrayList<>();

		StockKDayFlowDto[] flow = kDto.getFlow();
		
		KDayDao prevDao = null;
		for(String item : items) {
			String[] flds = split(item, ",");
			if(flds.length != 8 && flds.length != 9) {
				logger.error("日线数据格式有变化，应该类似：2008-05-13,1.80,2.02,2.02,1.79,49982,159771423,12.7%，实际为：{} URL: {}", item, url);
			    continue;
			}
			
			try {
				Date tdate =  JsonUtil.parseDate(flds[0]);

				KDayDao dao = new KDayDao();
				
				dao.setSCode(stkcode);
				dao.setTransDate(tdate);
				dao.setOpen(Double.parseDouble(flds[1]));
				dao.setClose(Double.parseDouble(flds[2]));
				dao.setHigh(Double.parseDouble(flds[3]));
				dao.setLow(Double.parseDouble(flds[4]));
				dao.setVolume(Double.parseDouble(flds[5]));
				dao.setAmount(Double.parseDouble(flds[6])/100);
				
				//计算量比
				double toRate = calcTurnoverRate(tdate, dao.getVolume(), flow);
				dao.setTurnoverRate(toRate);
				
				// 计算振幅和涨幅
				if(prevDao == null) {
					dao.setChangeRate(0);
					dao.setAmplitude(0);
				}else {  // TODO 除权日，这样算涨幅不对！！！
					double lclose = dbFhsg.calcLClose(stkcode, tdate, prevDao.getClose());
					dao.setLClose(lclose);
					dao.setChangeRate((dao.getClose()-lclose)*100/lclose);
					dao.setAmplitude((dao.getHigh()-dao.getLow())*100/lclose);
				}

				if(lastDate == null || tdate.after(lastDate)) {
					result.add(dao);
				}
				
				prevDao = dao;
			}catch(Exception e) {
				logger.error("日线数据格式有变化，应该类似：2008-05-13,1.80,2.02,2.02,1.79,49982,159771423,12.7%，实际为：{} URL: {}", item, url);
				logger.error("",e);
			    return null;
			}			
		}
		return result;
	}

	private double calcTurnoverRate(Date tdate, double volume, StockKDayFlowDto[] flow) {
		if(flow==null) {
			return 0;
		}
		
		double ltg = 0;
		
		for(int i=0; i<flow.length; ++i) {
			StockKDayFlowDto dto = flow[flow.length-1-i];
			if(!tdate.before(dto.getTime())) {
				ltg = dto.getLtg();
				break;
			}
		}

		if(ltg == 0) {
			return 0;
		}
		
		return volume*100/ltg;
	}

}
