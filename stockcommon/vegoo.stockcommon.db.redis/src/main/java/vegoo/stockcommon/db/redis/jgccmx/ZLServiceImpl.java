package vegoo.stockcommon.db.redis.jgccmx;


import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;

import vegoo.commons.DateUtil;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

public abstract class ZLServiceImpl extends RedisBoServiceImpl{
	private static final String F_ZGB = "ZGB";
	private static final String F_ZSZ = "ZSZ";
	private static final String F_ZZB = "ZZB";
	private static final String F_LTGB = "LTGB";
	private static final String F_LTSZ = "LTSZ";
	private static final String F_LTZB = "LTZB";

	protected static String getKeyOfZL(String s1, Date d, String s2) {
		return getKey(TBL_ZL, s1, DateUtil.formatDate(d), s2); 
 	}

    protected static String getKeyOfZL(String s1, String s2, Date d) {
		return getKey(TBL_ZL, s1, s2, DateUtil.formatDate(d)); 
	}
    
	private static String getKeyOfZL(Date d, String s) {
		return getKey(TBL_ZL, DateUtil.formatDate(d), s);
	}
	
	private static String getKey4Item(String scode, String shcode, Date reportDate) {
		return getKeyOfZL(scode, shcode, reportDate);
	}
	private static String getKey4GudongsOfStock(String scode, Date reportDate) {
		return getKeyOfZL("P", scode, reportDate);
	}
	private static String getKey4StocksOfGudong(String shcode, Date reportDate) {
		return getKeyOfZL("D", shcode, reportDate);
	}
	private static String getKey4StocksOfReport(Date reportDate) {
		return getKeyOfZL(reportDate, "P");
	}
	private static String getKey4GudongsOfReport(Date reportDate) {
		return getKeyOfZL(reportDate, "D");
	}
	
	protected abstract RedisService getRedis();
    
	
	protected void updateZGB(String scode, String shcode, Date rdate, double shareHDNum, double vposition, double zzb) {
		if(Strings.isNullOrEmpty(scode)||Strings.isNullOrEmpty(shcode)||rdate==null
				|| shareHDNum<100) { // 不够一手的数据扔掉
			return;
		}
		
		Map<String, String> props = new HashMap<>();
		props.put(F_ZGB, String.valueOf(shareHDNum)); // 总股本
		props.put(F_ZSZ, String.valueOf(vposition));  // 总市值
		props.put(F_ZZB, String.valueOf(zzb));        // 占总股本比
		
		updateZL(scode, shcode, rdate, shareHDNum, vposition, props);
	}
	
	protected void updateLTGB(String scode, String shcode, Date reportDate, 
			double shareHDNum, double vposition, double zzb, double ltzb ) {
		if(Strings.isNullOrEmpty(scode)||Strings.isNullOrEmpty(shcode)
				||reportDate==null || shareHDNum<100) { // 不够一手的数据扔掉
			return;
		}
		
		Map<String, String> props = new HashMap<>();
		
		props.put(F_LTGB, String.valueOf(shareHDNum));  // 流通股本
		props.put(F_LTSZ, String.valueOf(vposition));   // 流通市值
		props.put(F_LTZB, String.valueOf(ltzb));        // 占流通股本比
		
		props.put(F_ZGB, String.valueOf(shareHDNum));   // 流通股本
		props.put(F_ZSZ, String.valueOf(vposition));    // 流通市值
		props.put(F_ZZB, String.valueOf(zzb));          // 占总股本比,如果有总股本

		updateZL(scode, shcode, reportDate, shareHDNum, vposition, props);
	}
	
	private void updateZL(String scode, String shcode, Date reportDate, double volume, double amount, Map<String, String> props) {
		RedisService redis = getRedis();
		redis.hmset(getKey4Item(scode, shcode, reportDate), props);
		
		redis.zadd(getKey4GudongsOfStock(scode, reportDate), volume, shcode);
		redis.zadd(getKey4StocksOfGudong(shcode, reportDate), amount, scode);
		
		redis.sadd(getKey4StocksOfReport(reportDate), scode);
		redis.sadd(getKey4GudongsOfReport(reportDate), shcode);
	}
	
	public boolean existZL(String scode, Date reportDate, String shcode) {
		String key = getKey4Item(scode, shcode, reportDate);
		return getRedis().exists(key);
	}

	public boolean existTop10(String stkcode, Date rdate) {
		String key = getKey4GudongsOfStock(stkcode, rdate);
		return getRedis().exists(key);
	}
	
	public double sumZzbOfTopN(String stkcode, Date rdate, int n) {
		return sumStockValueOfTopN(stkcode, rdate, n, F_ZZB);
	}
	
	public double sumLtzbOfTopN(String stkcode, Date rdate, int n) {
		return sumStockValueOfTopN(stkcode, rdate, n, F_LTZB);
	}
	
	protected double sumStockValueOfTopN(String stkcode, Date rdate, int n, String field) {
		RedisService redis = getRedis();
		
		String key = getKey4GudongsOfStock(stkcode, rdate);
		
		Set<String> items = redis.getTopN(key, n, true);
	
/*		if("002589".equals(stkcode)) {
			try {
				Date d = DateUtil.parseDate("2017-06-30");
				if(rdate.equals(d)) {
					System.out.println(items);
				}
			} catch (ParseException e) {
			}
		}
		
*/		double result = 0;
		
		for(String shcode : items) {
			String k = getKeyOfZL(stkcode, shcode, rdate);
			result += redis.getDoubleValue(k, field);
		}
		
		return result;
	}

	protected double sumGudongValueOfTopN(String shcode, Date rdate, int n, String field) {
		RedisService redis = getRedis();
		
		String key = getKey4StocksOfGudong(shcode, rdate);
		Set<String> items = redis.getTopN(key, n, true);
		
		double result = 0;
		
		for(String stkcode:items) {
			String k = getKeyOfZL(stkcode, shcode, rdate);
			result += redis.getDoubleValue(k, field);
		}
		
		return result;
	}
	
	public double getZzb(String stockCode, String gdCode, Date reportDate) {
		String k = getKey4Item(stockCode, gdCode, reportDate);
		return getRedis().getDoubleValue(k, F_ZZB);
	}

	public double getLtzb(String stockCode, String gdCode, Date reportDate) {
		String k = getKey4Item(stockCode, gdCode, reportDate);
		return getRedis().getDoubleValue(k, F_LTZB);
	}

	public double getLtgb(String stockCode, String gdCode, Date reportDate) {
		String k = getKey4Item(stockCode, gdCode, reportDate);
		return getRedis().getDoubleValue(k, F_LTGB);
	}

	public double getLtsz(String stockCode, String gdCode, Date reportDate) {
		String k = getKey4Item(stockCode, gdCode, reportDate);
		return getRedis().getDoubleValue(k, F_LTSZ);
	}

	public Set<String> getGudongCodes(String stockCode, Date reportDate) {
		String key = getKey4GudongsOfStock( stockCode, reportDate);
		return getRedis().zrange(key, 0, -1);
	}
	
	public Set<String> getGudongCodes(Date reportDate) {
		String key = getKey4GudongsOfReport(reportDate);
		return getRedis().zrange(key, 0, -1);
	}

	public Set<String> getStockCodes(Date reportDate) {
		String key = getKey4StocksOfReport(reportDate);
		return getRedis().smembers(key);
	}
	
	public Set<String> getStockCodes(String shCode, Date reportDate) {
		String key = getKey4StocksOfGudong(shCode, reportDate);
		return getRedis().zrange(key, 0, -1);
	}
	
}
