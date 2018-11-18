package vegoo.newstock.persistent.redis.impl;

import java.util.Set;

import vegoo.newstock.persistent.redis.RedisService;

public abstract class StockRedis extends BlockStockRedis{
    static String key4Stock(String stockCode) {
		return GC.getKey(GC.TBL_STOCK,stockCode); 
	}
	
	public static Set<String> getStocks(RedisService redis) {
		return redis.zrange(GC.KEY_STOCKS, 0, -1);
	}

	protected StockRedis(StockDataServiceImpl dataService) {
		super(dataService);
		

	}

}
