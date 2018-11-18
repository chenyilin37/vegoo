package vegoo.commons.redis;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;

public interface RedisService extends JedisCommands,MultiKeyCommands{
	public String getStringValue(String key);
	public Date getDateValue(String key) throws ParseException;
	public Date getDateTimeValue(String key) throws ParseException;
	public double getDoubleValue(String key);
	public int getIntValue(String key);
	public long getLongValue(String key);
	public boolean getBoolValue(String key);

	// hget
	public String getStringValue(String key, String field);
	public Date getDateValue(String key, String field) throws ParseException;
	public Date getDateTimeValue(String key, String field) throws ParseException;
	public double getDoubleValue(String key, String field);
	public int getIntValue(String key, String field);
	public long getLongValue(String key, String field);
	public boolean getBoolValue(String key, String field);
	
	
	public String getNearest(String key, double score, boolean reverse);
	public Set<String> getTopN(String key,int count, boolean reverse);
	
}
