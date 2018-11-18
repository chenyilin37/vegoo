package vegoo.stockcommon.db.redis.sdltg;

import java.util.Date;
import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.SdltgdService;
import vegoo.stockcommon.db.redis.jgccmx.ZLServiceImpl;

@Component (immediate = true)
public class SdltgdServiceImpl extends ZLServiceImpl implements SdltgdService, ManagedService{
	@Reference protected RedisService redis;

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		
		
	}
	@Override
	protected RedisService getRedis() {
		return redis;
	}

	@Override
	public boolean existSdgd(String scode, Date rdate, String shcode) {
		return existZL(scode, rdate, shcode);
	}

	@Override
	public void insertSdgd(String companycode, String sharehdname, String sharehdtype, String sharestype, double rank,
			String scode, Date rdate, double shareHDNum, double vposition, double ltzb, Date ndate, String bz, double bdbl,
			String shcode, double zzb, double bdsum, boolean isLT) {
		if(isLT) {
			updateLTGB(scode, shcode, rdate, shareHDNum, vposition, zzb, ltzb);
		}else {
			updateZGB(scode, shcode, rdate, shareHDNum, vposition, zzb);
		}
	}

	@Override
	public void settleSdltgd(boolean reset) {
		// TODO Auto-generated method stub
		
	}


}
