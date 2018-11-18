package vegoo.stockcommon.db.redis.jgcc;

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
import vegoo.stockcommon.bo.JgccService;
import vegoo.stockcommon.dao.JgccDao;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

@Component (immediate = true)
public class JgccServiceImpl extends RedisBoServiceImpl implements JgccService, ManagedService {
	@Reference protected RedisService redis;

    static String key4Jgcc(String stockCode, Date rDate) {
		return key4Jgcc(stockCode, DateUtil.formatDate(rDate)); 
 	}
    
    static String key4Jgcc(String stockCode, String rDate) {
		return getKey(TBL_JGCC, stockCode, rDate); 
	}
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isNewJgcc(JgccDao dao, boolean deleteOld) {
		String oldTag = redis.hget(key4Jgcc(dao.getScode(), dao.getReportDate()), "DTAG"+dao.getJglx());
		return oldTag ==null || !oldTag.equals(dao.getDataTag()+"");
	}

	@Override
	public boolean existJgcc(String scode, Date reportDate) {
		String key = key4Jgcc(scode, reportDate);
		return redis.exists(key);
	}

	@Override
	public void insertJgcc(JgccDao dao) {
		if(Strings.isNullOrEmpty(dao.getScode())||dao.getReportDate()==null) {
			return;
		}

		Map<String, String> props = new HashMap<>();
		
		int i = dao.getJglx();
		
		props.put("CNT"+i, String.valueOf(dao.getCount()));
		props.put("HN"+i, String.valueOf(dao.getShareHDNum()));
		props.put("VP"+i, String.valueOf(dao.getvPosition()));
		props.put("ZZB"+i, String.valueOf(dao.getTabRate()));
		props.put("LTZB"+i, String.valueOf(dao.getLtzb()));
		props.put("DTAG"+i, String.valueOf(dao.getDataTag()));
		
		redis.hmset(key4Jgcc(dao.getScode(), dao.getReportDate()), props);
		
	}
	@Override
	public double getLtzb(String stockCode, Date rdate, int jglx) {
		return getFieldValue(stockCode, rdate, jglx,"LTZB");
	}
	
	@Override
	public double getLtzb(String stockCode, Date rdate, int[] jglxs) {
		return sumFieldValues(stockCode, rdate, jglxs, "LTZB");
	}
	
	@Override
	public double getZzb(String stkcode, Date rDate, int[] jglxs) {
		return sumFieldValues(stkcode, rDate, jglxs, "ZZB");
	}
	
	@Override
	public double getZzb(String stkcode, Date rDate, int jglx) {
		return getFieldValue(stkcode, rDate, jglx, "ZZB");
	}
	
	public double getCount(String stockCode, Date rdate, int jglx) {
		return getFieldValue(stockCode, rdate, jglx,"CNT");
	}

	@Override
	public double getAmount(String stockCode, Date rdate, int jglx) {
		return getFieldValue(stockCode, rdate, jglx,"VP");
	}

	@Override
	public double getVolume(String stockCode, Date rdate, int jglx) {
		return getFieldValue(stockCode, rdate, jglx,"HN");
	}

	@Override
	public void settleJgcc(boolean reset) {
		// TODO Auto-generated method stub
		
	}

	protected double getFieldValue(String stockCode, Date rdate, int jglx, String field) {
		String key = key4Jgcc(stockCode, rdate);
		return redis.getDoubleValue(key, field+jglx);
	}
	
	protected double sumFieldValues(String stockCode, Date rdate, int[] jglxs, String field) {
		double result = 0;
		for(int lx : jglxs) {
			result += getFieldValue(stockCode, rdate, lx, field);
		}
		return result;
	}




}
