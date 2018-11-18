package vegoo.stockcommon.db.redis.block;


import java.util.Set;

import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Reference;

import redis.clients.jedis.JedisCommands;
import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.BlockService;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

public abstract class BlockStockServiceImpl extends RedisBoServiceImpl{
	
	private static String getKeyOfStocksBlock(String blockUCode) {
		return getKey(TBL_BLOCK,blockUCode,"STKS"); 
	}
	
	private static String getKeyOfBlocksStock(String stockCode) {
		return getKey(TBL_STOCK,stockCode,"BLKS"); 
	}
	
	protected abstract RedisService getRedis();
	
	public void insertStockOfBlock(String blockUCode, String stkCode) {
		getRedis().sadd(getKeyOfStocksBlock(blockUCode), stkCode);
		getRedis().sadd(getKeyOfBlocksStock(stkCode), blockUCode);
	}
	

	public void deleteStockOfBlock(String blockUCode, String stkCode) {
		getRedis().srem(getKeyOfStocksBlock(blockUCode), stkCode);
		getRedis().srem(getKeyOfBlocksStock(stkCode), blockUCode);
	}

	public void deleteAllStocksOfBlock(String blockUCode) {
		String key = getKeyOfStocksBlock(blockUCode);
		Set<String> stocks = getRedis().smembers(key);
		stocks.forEach(s->{deleteStockOfBlock(blockUCode, s);});
		getRedis().del(key);
	}

	public void deleteAllBlocksOfStock(String stockCode) {
		String key = getKeyOfBlocksStock(stockCode);
		Set<String> stocks = getRedis().smembers(key);
		stocks.forEach(s->{deleteStockOfBlock(s, stockCode);});
		getRedis().del(key);
	}

	
	public Set<String> getBlocksOfStock(String stockcode) {
		return getRedis().smembers(getKeyOfBlocksStock(stockcode));
	}

	public Set<String> getStockCodesOfBlock(String blkUcode) {
		return getRedis().smembers(getKeyOfStocksBlock(blkUcode));
	}
	
	
}
