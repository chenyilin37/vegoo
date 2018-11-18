package vegoo.stockcommon.db.redis.fhsg;

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
import vegoo.stockcommon.dao.FhsgDao;
import vegoo.stockcommon.utils.StockUtil;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;


@Component (immediate = true)
public class FhsgServiceImpl extends RedisBoServiceImpl implements FhsgService, ManagedService{

	@Reference protected RedisService redis;
	
    static String getKeyOfFHSG(String stockCode) {
		return getKey(TBL_FHSG, stockCode); 
	}
    
    static String getKeyOfFHSG(String stockCode, Date rDate) {
		return getKey(TBL_FHSG, stockCode, rDate); 
	}
    
    static String getKeyOfFHSG(String stockCode, String rDate) {
		return getKey(TBL_FHSG, stockCode, rDate); 
	}
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean existFhsg(String sCode, Date rDate) {
		return redis.exists(getKeyOfFHSG(sCode, rDate));
	}

	@Override
	public void insertFhsg(FhsgDao dao) {
		Date rdate =  dao.getRDate();
		
		if(Strings.isNullOrEmpty(dao.getSCode())||rdate==null) {
			return;
		}

		Map<String, String> props = new HashMap<>();
		
		props.put("SZZBL", String.valueOf(dao.getSZZBL()));
		props.put("SGBL", String.valueOf(dao.getSGBL()));
		props.put("ZGBL", String.valueOf(dao.getZGBL()));
		props.put("XJFH", String.valueOf(dao.getXJFH()));
		
		Date YAGGR = dao.getYAGGR();
		if(YAGGR != null) {
		   props.put("YAGGR", DateUtil.formatDate(YAGGR));
		}
		Date GQDJR = dao.getGQDJR();
		if(GQDJR != null) {
		   props.put("GQDJR", DateUtil.formatDate(GQDJR));
		}
		Date CQCXR = dao.getCQCXR();
		if(CQCXR != null) {
		   props.put("CQCXR", DateUtil.formatDate(CQCXR));
		}
		
		props.put("DTAG", String.valueOf(dao.getDataTag()));
		
		redis.hmset(getKeyOfFHSG(dao.getSCode(),rdate), props);
		redis.zadd(getKeyOfFHSG(dao.getSCode()), rdate.getTime(), DateUtil.formatDate(rdate));		
	}
	
	@Override
	public boolean isNewFhsg(String code, Date rDate, int dataTag, boolean deleteOld) {
		String oldTag = redis.hget(getKeyOfFHSG(code,rDate), "DTAG");
		return oldTag ==null || !oldTag.equals(dataTag);
	}

	@Override
	public double adjustWithHoldNum(Date reportDate, String scode, double hdNum) {
		Date prevRDate = StockUtil.getReportDate(reportDate, -1);
		
		Set<String> fhDates = redis.zrangeByScore(getKeyOfFHSG(scode), prevRDate.getTime()+1, reportDate.getTime());
		double result = hdNum;
		for(String d:fhDates) {
			String key = getKeyOfFHSG(scode, d); 
			String cqDate = redis.hget(key, "CQCXR");
			// String djDate = redis.hget(key, "GQDJR");
			if(Strings.isNullOrEmpty(cqDate)) {
				continue;
			}
			
			double szzbl = redis.getDoubleValue(key, "SZZBL");
			result *= (1.0 + szzbl/10);
		}
		
		return result;
	}

	@Override
	public double calcLClose(String stockcode, Date transdate, double lClose) {
		String key = getKeyOfFHSG(stockcode, transdate); 
		double ap = redis.getDoubleValue(key, "ADJUSTPRICE");
		return lClose + ap;
	}


	@Override
	public void settleFhsg(boolean reset) {
		// TODO Auto-generated method stub
		
	}

}
