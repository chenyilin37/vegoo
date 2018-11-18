package vegoo.stockcommon.db.redis.gdhs;

import java.text.ParseException;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.base.Strings;

import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.FhsgService;
import vegoo.stockcommon.bo.GdhsService;
import vegoo.stockcommon.dao.GdhsDao;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

@Component (immediate = true)
public class GdhsServiceImpl extends RedisBoServiceImpl implements GdhsService, ManagedService {

	@Reference protected RedisService redis;

    static String getKeyOfGdhs(String stockCode) {
		return getKey(TBL_GDHS, stockCode); 
	}
    
    static String getKeyOfGdhs(String stockCode, Date rDate) {
		return getKey(TBL_GDHS, stockCode, rDate); 
	}
    
    static String getKeyOfGdhs(String stockCode, String rDate) {
		return getKey(TBL_GDHS, stockCode, rDate); 
	}
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		
		
	}

    
	@Override
	public boolean existGDHS(String stockCode, Date endDate) {
		return redis.exists(getKeyOfGdhs(stockCode, endDate));
	}

	@Override
	public void insertGdhs(GdhsDao dao) {
		//String stockCode, Date endDate, double holderNum, double previousHolderNum,
		//double holderNumChange, double holderNumChangeRate, double rangeChangeRate, Date previousEndDate,
		//double holderAvgCapitalisation, double holderAvgStockQuantity, double totalCapitalisation,
		//double capitalStock, Date noticeDate, int dataTag
		
		Date endDate = dao.getEndDate();
		if(Strings.isNullOrEmpty(dao.getSecurityCode())||endDate==null) {
			return;
		}
		
		Map<String, String> props = new HashMap<>();
		
		props.put("HN",  String.valueOf(dao.getHolderNum())); //'HolderNum'
		props.put("HNC",  String.valueOf(dao.getHolderNumChange()));//'HolderNumChange'
		props.put("HNCR",  String.valueOf(dao.getHolderNumChangeRate()));//'HolderNumChangeRate'
		props.put("HAV",  String.valueOf(dao.getHolderAvgCapitalisation()));//'HolderAvgCapitalisation'
		props.put("HAQ",  String.valueOf(dao.getHolderAvgStockQuantity()));//'HolderAvgStockQuantity'
		
		// 报告期的交易日
		
		redis.hmset(getKeyOfGdhs(dao.getSecurityCode(),endDate), props);
		redis.zadd(getKeyOfGdhs(dao.getSecurityCode()), endDate.getTime(), DateUtil.formatDate(endDate));
	}

	@Override
	public Date getLatestEndDate(String stockCode, Date date) {
		String result = redis.getNearest(getKeyOfGdhs(stockCode), date.getTime(), true);
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
	public Set<String> getEndDates(String stkcode) {
		return redis.zrange(getKeyOfGdhs(stkcode), 0, -1);
	}

	@Override
	public long getHolderNum(String stkcode, String sDate) {
		String key = getKeyOfGdhs(stkcode,sDate);
		return redis.getLongValue(key, "HN");
	}

	@Override
	public long getHolderNum(String stkcode, Date dDate) {
		return getHolderNum(stkcode, DateUtil.formatDate(dDate));
	}
	
	public double getAverageQuantity(String stkcode, String sDate) {
		String key = getKeyOfGdhs(stkcode,sDate);
		return redis.getDoubleValue(key, "HAQ");
	}
	public double getAverageAmount(String stkcode, String sDate) {
		String key = getKeyOfGdhs(stkcode,sDate);
		return redis.getDoubleValue(key, "HAV");
	}

	@Override
	public void settleGdhs(boolean reset) {
		// TODO Auto-generated method stub

	}




}
