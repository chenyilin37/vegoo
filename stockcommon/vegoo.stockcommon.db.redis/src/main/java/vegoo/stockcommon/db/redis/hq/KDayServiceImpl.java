package vegoo.stockcommon.db.redis.hq;

import java.text.ParseException;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.KDayService;
import vegoo.stockcommon.bo.JgccService;
import vegoo.stockcommon.dao.KDayDao;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;


@Component (name = "KDayServiceImpl",
			immediate = true)
public class KDayServiceImpl  extends RedisBoServiceImpl implements KDayService, ManagedService{
	private static final Logger logger = LoggerFactory.getLogger(KDayServiceImpl.class);
	
	@Reference protected RedisService redis;

    static String getKeyOfKDay(String stockCode) {
		return getKey(TBL_KDAY, stockCode); 
	}
    
    static String getKeyOfKDay(String stockCode, Date rDate) {
		return getKey(TBL_KDAY, stockCode, rDate); 
	}
    
    static String getKeyOfKDay(String stockCode, String rDate) {
		return getKey(TBL_KDAY, stockCode, rDate); 
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Date getEarliestTradeDate(String stockCode) {
		Set<String> result = redis.zrange(getKeyOfKDay(stockCode), 0, 0);
		if(result==null) {
			return null;
		}
		
		try {
			for(String s : result) {
				return DateUtil.parseDate(s);
			}
		} catch (ParseException e) {
		}
		return null;				
	}

	@Override
	public Date getLatestTradeDate(String stockCode) {
		return getLatestTradeDate(stockCode, new Date());
	}

	@Override
	public Date getLatestTradeDate(String stockCode, Date date) {
		String result = redis.getNearest(getKeyOfKDay(stockCode), date.getTime(), true);
		if(result==null) {
			return null;
		}
		
		try {
			return DateUtil.parseDate(result);
		} catch (ParseException e) {
			return null;				
		}
	}

	@Override
	public void saveKDayData(List<KDayDao> newItems) {
		newItems.forEach((dao)->{
			Date rDate = dao.getTransDate();
			
			Map<String, String> props = new HashMap<>();
			
			props.put("O", String.valueOf(dao.getOpen()));
			props.put("H", String.valueOf(dao.getHigh()));
			props.put("L", String.valueOf(dao.getLow()));
			props.put("C", String.valueOf(dao.getClose()));
			props.put("V", String.valueOf(dao.getVolume()));
			props.put("A", String.valueOf(dao.getAmount()));
			
			props.put("CR", String.valueOf(dao.getChangeRate()));  //涨幅
			props.put("AR", String.valueOf(dao.getAmplitude()));   // 振幅
			props.put("TR", String.valueOf(dao.getTurnoverRate())); // 换手
			
			redis.hmset(getKeyOfKDay(dao.getSCode(), rDate), props);
			redis.zadd(getKeyOfKDay(dao.getSCode()), rDate.getTime(), DateUtil.formatDate(rDate));
		});
	}

	@Override
	public double getOpen(String sCode, Date transDate) {
		return redis.getDoubleValue(getKeyOfKDay(sCode, transDate),"O");
	}
	
	@Override
	public double getHigh(String stockCode, Date date) {
		return redis.getDoubleValue(getKeyOfKDay(stockCode, date),"H");
	}
	
	public double getHigh(String stockCode, String date) {
		return redis.getDoubleValue(getKeyOfKDay(stockCode, date),"H");
	}
	
	@Override
	public double getLow(String sCode, Date transDate) {
		return redis.getDoubleValue(getKeyOfKDay(sCode, transDate),"L");
	}
	
	public double getLow(String sCode, String transDate) {
		return redis.getDoubleValue(getKeyOfKDay(sCode, transDate),"L");
	}

	@Override
	public double getClose(String sCode, Date transDate) {
		return redis.getDoubleValue(getKeyOfKDay(sCode, transDate),"C");
	}
	
	@Override
	public double getZhangFu(String stockCode, Date beginDate, Date endDate) throws Exception {
		Date transDate1 = getLatestTradeDate(stockCode, beginDate);
		Date transDate2 = getLatestTradeDate(stockCode, endDate);
		
		if(transDate1==null || transDate2==null) {
			logger.error("{}没有对应的交易日：{}={}, {}={}", stockCode, 
					DateUtil.formatDate(beginDate), DateUtil.formatDate(transDate1), 
					DateUtil.formatDate(endDate), DateUtil.formatDate(transDate2));
			throw new Exception();		
		}
		
		if(transDate1==transDate2) {
			return 0;
		}
		
		double close1 = getClose(stockCode, transDate1);
		double close2 = getClose(stockCode, transDate2);
		
		if(close1<=0) {
			logger.error("{}对应交易日{}的收盘价为0", stockCode, transDate1);
			throw new Exception();
		}
		return (close2-close1)*100/close1;
	}

	@Override
	public double[] getMaxMinZhangFu(String stockCode, Date beginDate, Date endDate) throws Exception {
		Date transDate1 = getLatestTradeDate(stockCode, beginDate);
		Date transDate2 = getLatestTradeDate(stockCode, endDate);
		
		if(transDate1==null || transDate2==null) {
			logger.error("{}没有对应的交易日：{}={}, {}={}", stockCode, 
					DateUtil.formatDate(beginDate), DateUtil.formatDate(transDate1), 
					DateUtil.formatDate(endDate), DateUtil.formatDate(transDate2));
			throw new Exception();		
		}
		
		if(transDate1==transDate2) {
			return new double[] {0,0};
		}
		
		double close1 = getClose(stockCode, transDate1);
		if(close1 == 0) {
			return new double[] {0,0};
		}
		
		Set<String> transDates = redis.zrangeByScore(getKeyOfKDay(stockCode), 
				transDate1.getTime(), transDate2.getTime());
		
		if(transDates.isEmpty()) {
			return new double[] {0,0};
		}
		
		double high = Double.MIN_VALUE;
		double low  = Double.MAX_VALUE;
		
		for(String date:transDates){
			double h = getHigh(stockCode, date);
			double l = getLow(stockCode, date);
			if(h>high) {
				high = h;
			}
			if(l<low) {
				low = l;
			}
		}
		
		double zfx = (high-close1)*100/close1;
		double zfn = (low-close1)*100/close1;
		if(high == Double.MIN_VALUE || low == Double.MAX_VALUE) {
			logger.error("{}在[{},{}]计算MaxMin时出现极值", stockCode, 
					DateUtil.formatDate(transDate1), 
					DateUtil.formatDate(transDate2));
			throw new Exception();
		}
		
		return new double[] {zfx, zfn};
	}

}
