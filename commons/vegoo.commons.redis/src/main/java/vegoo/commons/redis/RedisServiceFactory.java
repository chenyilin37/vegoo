package vegoo.commons.redis;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import vegoo.commons.DateUtil;
import vegoo.commons.redis.impl.RedisServiceImpl;

public class RedisServiceFactory {
	public static RedisService create(String host, int port, String password) {
		RedisServiceImpl result = new RedisServiceImpl();
		result.initJedis(host, port, password);
		return result;
	}

/*
	static int i=0;
	public static void main(String[] args) throws Exception {
		RedisService redis = create("localhost", 6379, null);
		//Set<String> stocks = redis.zrange("STOCKS", 0, -1);
		//stocks.forEach((e)->{System.out.println((++i) +" "+e);});
		
		Date d = DateUtil.parseDate("2102-04-13");
		System.out.println(redis.getNearest("KD_600675", d.getTime(), true));

		System.out.println(redis.getNearest("KD_600675", d.getTime(), false));
		
		Set<String> items = redis.getTops("KD_600675", 10, false);
		items.forEach((e)->{System.out.println((++i) +" "+e);});
		i=0;
		items = redis.getTops("KD_600675", 10, true);
		items.forEach((e)->{System.out.println((++i) +" "+e);});
	}
*/
}
