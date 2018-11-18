package vegoo.stockcommon.db.redis;

import java.util.Date;

import vegoo.commons.DateUtil;
import vegoo.stockcommon.bo.BoServiceImpl;

public class RedisBoServiceImpl extends BoServiceImpl implements RedisBoService {

	public static String getKey(String prefix, String subfix, Date date) {
		String sDate = DateUtil.formatDate(date);
		return getKey(prefix, subfix, sDate);
	}

	public static String getKey(String prefix, Date date, String subfix) {
		String sDate = DateUtil.formatDate(date);
		return getKey(prefix, sDate, subfix);
	}
	
	public static String getKey(String... fields) {
		String pattern = getPattern(fields.length);
		return String.format(pattern, (Object[])fields);
	}

	private static String getPattern(int length) {
		StringBuffer sb = new StringBuffer("%s");
		for(int i=1;i<length;++i) {
			sb.append(":").append("%s");
		}
		return sb.toString();
	}
	
	
}
