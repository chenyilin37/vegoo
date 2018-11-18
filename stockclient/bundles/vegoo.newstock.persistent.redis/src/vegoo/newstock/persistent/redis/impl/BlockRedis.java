package vegoo.newstock.persistent.redis.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.dao.BlockDao;
import vegoo.newstock.persistent.redis.RedisService;

public abstract class BlockRedis extends BlockStockRedis implements Block{
    
    static String key4Block(String blockUCode) {
		return GC.getKey(GC.TBL_BLOCK,blockUCode); 
	}
    
	public static Set<String> getBlockUCodes(RedisService redis) {
		return redis.zrange(GC.KEY_BLOCKS, 0, -1);
	}
	
	public static boolean existBlock(String blockUCode, RedisService redis) {
		return redis.exists(key4Block(blockUCode));
	}

	public static void insertBlock(BlockDao dao, RedisService redis) {
		Map<String, String> props = new HashMap<>();
		props.put("CODE", dao.getCode());
		props.put("NAME", dao.getName());
		props.put("MARKET", dao.getMarketId()+"");
		
		redis.hmset(key4Block(dao.getUCode()), props);
		redis.zadd(GC.KEY_BLOCKS, dao.getTypeId(), dao.getUCode());
	}
	
	public static void deleteBlock(String blockUCode, RedisService redis) {
		deleteAllStocksOfBlock(blockUCode, redis);
		
		redis.del(key4Block(blockUCode));
		redis.zrem(GC.KEY_BLOCKS, blockUCode);
	}
	
	public static void updateStocksOfBlock(String blkUcode, Set<String> newMembers, RedisService redis) {
		Set<String> oldMembers = getStockCodesOfBlock(blkUcode, redis);
		Set<String> addMembers = Sets.difference(newMembers, oldMembers); // 差集 1中有而2中没有的
		Set<String> DelMembers = Sets.difference(oldMembers, newMembers); // 差集 1中有而2中没有的
		
		for(String stkCode : DelMembers) {
			deleteStockOfBlock(blkUcode, stkCode, redis);
		}
		
		for(String stkCode : addMembers) {
			insertStockOfBlock(blkUcode, stkCode, redis);
		}
	}
	
	protected BlockRedis(StockDataServiceImpl dataService) {
		super(dataService);
		
		
		
	}


	protected Set<String> getStockCodes() {
		
		return null;
	}
	

	
}
