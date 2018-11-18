package vegoo.newstock.persistent.redis.impl;

import java.text.ParseException;
import java.util.Date;

import com.google.common.base.Strings;

import vegoo.newstock.core.utils.DateUtil;
import vegoo.newstock.persistent.redis.RedisService;

public abstract class RedisObject {
	
	protected final StockDataServiceImpl dataService;


	
	protected RedisObject(StockDataServiceImpl dataService){
		this.dataService = dataService;
	}
	
	protected final RedisService getRedis() {
		return dataService.getRedis();
	}

	
	public String getStringValue(String key) {
		return getRedis().get(key);
	}

	public Date getDateValue(String key) throws ParseException {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDate(s);
	}
	
	
	public Date getDateTimeValue(String key) throws ParseException {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDateTime(s);
	}

	
	public double getDoubleValue(String key) {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	
	public int getIntValue(String key) {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Integer.parseInt(s);
	}
	
	
	public long getLongValue(String key) {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Long.parseLong(s);
	}

	
	public boolean getBoolValue(String key) {
		String s = getRedis().get(key);
		if(Strings.isNullOrEmpty(s)) {
			return false;
		}
		return Boolean.parseBoolean(s);
	}

	
	public String getStringValue(String key, String field) {
		return getRedis().hget(key,field);
	}

	
	public Date getDateValue(String key, String field) throws ParseException {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDate(s);
	}
	
	
	public Date getDateTimeValue(String key, String field) throws ParseException {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return null;
		}
		return DateUtil.parseDateTime(s);
	}

	
	public double getDoubleValue(String key, String field) {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	
	public int getIntValue(String key, String field) {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Integer.parseInt(s);
	}
	
	
	public long getLongValue(String key, String field) {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return 0;
		}
		return Long.parseLong(s);
	}

	
	public boolean getBoolValue(String key, String field) {
		String s = getRedis().hget(key,field);
		if(Strings.isNullOrEmpty(s)) {
			return false;
		}
		return Boolean.parseBoolean(s);
	}
	
	
}
