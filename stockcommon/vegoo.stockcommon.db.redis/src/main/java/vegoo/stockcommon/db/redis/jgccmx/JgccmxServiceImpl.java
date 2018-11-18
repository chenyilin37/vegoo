package vegoo.stockcommon.db.redis.jgccmx;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.JgccmxService;
import vegoo.stockcommon.dao.JgccmxDao;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

@Component (immediate = true)
public class JgccmxServiceImpl  extends ZLServiceImpl implements JgccmxService, ManagedService{

	@Reference protected RedisService redis;
	
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		
		
	}
	
	@Override
	protected RedisService getRedis() {
		return redis;
	}
	
	@Override
	public void insertJgccmx(JgccmxDao dao) {
		String scode = dao.getScode();
		Date reportDate =dao.getReportDate();
		String shcode = dao.getShcode();
		//String indtCode, String lxdm,
		double shareHDNum = dao.getLtNum();
		double vposition = dao.getVposition(); // 
		double zzb = dao.getZzb();
		double ltzb = dao.getLtzb();
		
		updateLTGB(scode, shcode, reportDate, shareHDNum, vposition, zzb,ltzb);
	}
	
	@Override
	public boolean existJgccmx(String scode, Date reportDate, String shcode) {
		return existZL(scode, reportDate, shcode);
	}

	@Override
	public void settleJgccmx(boolean reset) {
		
		
	}



}
