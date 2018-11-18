package vegoo.stockcommon.db.redis.gudong;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.base.Strings;

import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.GudongService;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

@Component (immediate = true)
public class GudongServiceImpl extends RedisBoServiceImpl implements GudongService, ManagedService{

	static final String KEY_GUDONGS = "GUDONGS";
	static final Map<String, String> GD_TYPES = new HashMap<>();

	static {
		GD_TYPES.put("1","基金");
		GD_TYPES.put("2","QFII");
		GD_TYPES.put("3","社保");
		GD_TYPES.put("4","券商");
		GD_TYPES.put("5","保险");
		GD_TYPES.put("6","信托");
		GD_TYPES.put("81","基本养老基金");
		GD_TYPES.put("82","企业年金");
		GD_TYPES.put("83","金融");
		GD_TYPES.put("84","财务公司");
		GD_TYPES.put("85","投资公司");
		GD_TYPES.put("86","集合理财计划");
		GD_TYPES.put("87","高校");
		GD_TYPES.put("88","个人");
		GD_TYPES.put("99","其它");
	}
	
	@Reference protected RedisService redis;
	
    static String getKeyOfGudong(String shCode) {
		return getKey(TBL_GUDONG, shCode); 
	}
	
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		
		
	}

	@Override
	public String getLXName(String lxId) {
		return GD_TYPES.get(lxId);
	}
	
	public String getLXCode(String lxName) {
		String result = "99";
		for(Map.Entry<String, String> entry:GD_TYPES.entrySet()) {
			if(lxName.equals(entry.getValue()) ) {
				result = entry.getKey();
				break;
			}
		}
		return result;
	}
	
	@Override
	public boolean existGudong(String sHCode) {
		return redis.exists(getKeyOfGudong(sHCode));
	}

	
	@Override
	public void saveGudong(String sHCode, String sHName, String gdlx, String lxdm, String indtCode, String instSName) {
		if(existGudong(sHCode)) {
			return;
		}
		inserteGudong(sHCode,sHName, gdlx, lxdm, indtCode, instSName);
	}
	
	
	public void inserteGudong(String SHCode, String SHName, String gdlx, String lxdm, String indtCode, String indtName) {
		Map<String, String> props = new HashMap<>();
		
		props.put("NAME", SHName);
		props.put("LX", String.valueOf(gdlx));
		props.put("LXDM", lxdm);
		if(!Strings.isNullOrEmpty(indtCode)) {
			props.put("INDTCODE", indtCode);
			props.put("INDTNAME", indtName);
		}
		
		redis.hmset(getKeyOfGudong(SHCode), props);
		redis.zadd(KEY_GUDONGS, Integer.parseInt(gdlx), SHCode);
	}


}
