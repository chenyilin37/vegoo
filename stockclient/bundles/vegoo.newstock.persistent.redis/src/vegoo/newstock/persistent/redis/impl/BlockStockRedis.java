package vegoo.newstock.persistent.redis.impl;

import java.util.Set;

import vegoo.newstock.persistent.redis.RedisService;

public abstract class BlockStockRedis extends RedisObject{
    private static String key4StocksOfBlock(String blockUCode) {
		return GC.getKey(GC.TBL_BLOCK,blockUCode,"STKS"); 
	}
	
	private static String key4BlocksOfStock(String stockCode) {
		return GC.getKey(GC.TBL_STOCK,stockCode,"BLKS"); 
	}

	public static void insertStockOfBlock(String blockUCode, String stkCode, RedisService redis) {
		redis.sadd(key4StocksOfBlock(blockUCode), stkCode);
		redis.sadd(key4BlocksOfStock(stkCode), blockUCode);
	}

	public static void deleteStockOfBlock(String blockUCode, String stkCode, RedisService redis) {
		redis.srem(key4StocksOfBlock(blockUCode), stkCode);
		redis.srem(key4BlocksOfStock(stkCode), blockUCode);
	}

	public static void deleteAllStocksOfBlock(String blockUCode, RedisService redis) {
		String key = key4StocksOfBlock(blockUCode);
		Set<String> stocks = redis.smembers(key);
		stocks.forEach(s->{deleteStockOfBlock(blockUCode, s, redis);});
		redis.del(key);
	}

	public static void deleteAllBlocksOfStock(String stockCode, RedisService redis) {
		String key = key4BlocksOfStock(stockCode);
		Set<String> stocks = redis.smembers(key);
		stocks.forEach(s->{deleteStockOfBlock(s, stockCode, redis);});
		redis.del(key);
	}

	public static Set<String> getBlockCodesOfStock(String stockcode, RedisService redis) {
		return redis.smembers(key4BlocksOfStock(stockcode));
	}

	public static Set<String> getStockCodesOfBlock(String blkUcode, RedisService redis) {
		return redis.smembers(key4StocksOfBlock(blkUcode));
	}
	
	protected BlockStockRedis(StockDataServiceImpl dataService) {
		super(dataService);
		// TODO Auto-generated constructor stub
	}
	

	
	
}
