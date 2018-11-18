package vegoo.stockcommon.db.redis.block;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.Sets;

import vegoo.commons.redis.RedisService;
import vegoo.stockcommon.bo.BlockService;
import vegoo.stockcommon.dao.BlockDao;
import vegoo.stockcommon.db.redis.RedisBoServiceImpl;

@Component (
		immediate = true,
		service = { BlockService.class,  ManagedService.class},			
		configurationPid = "stockdata.db.block"
	)
public class BlockServiceImpl extends BlockStockServiceImpl implements BlockService, ManagedService{
	static final String KEY_BLOCKS = "BLOCKS";
	
	@Reference protected RedisService redis;
	
    static String getKeyOfBlock(String blockUCode) {
		return getKey(TBL_BLOCK,blockUCode); 
	}
	
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
   		
	}
	
	@Override
	protected RedisService getRedis() {
		return redis;
	}

	@Override
	public boolean existBlock(String blockUCode) {
		return redis.exists(getKeyOfBlock(blockUCode));
	}

	@Override
	public void insertBlock(BlockDao dao) {
		Map<String, String> props = new HashMap<>();
		props.put("CODE", dao.getCode());
		props.put("NAME", dao.getName());
		props.put("MARKET", dao.getMarketId()+"");
		
		redis.hmset(getKeyOfBlock(dao.getUCode()), props);
		redis.zadd(KEY_BLOCKS, dao.getTypeId(), dao.getUCode());
	}
	
	public void deleteBlock(String blockUCode) {
		deleteAllStocksOfBlock(blockUCode);
		redis.del(getKeyOfBlock(blockUCode));
		redis.zrem(KEY_BLOCKS, blockUCode);
	}
	
	@Override
	public void updateStocksOfBlock(String blkUcode, Set<String> newMembers) {
		Set<String> oldMembers = getStockCodesOfBlock(blkUcode);
		Set<String> addMembers = Sets.difference(newMembers, oldMembers); // 差集 1中有而2中没有的
		Set<String> DelMembers = Sets.difference(oldMembers, newMembers); // 差集 1中有而2中没有的
		
		for(String stkCode : DelMembers) {
			deleteStockOfBlock(blkUcode, stkCode);
		}
		
		for(String stkCode : addMembers) {
			insertStockOfBlock(blkUcode, stkCode);
		}
	}

	@Override
	public Collection<String> getBlockUCodes() {
		return redis.zrange(KEY_BLOCKS, 0, -1);
	}


}
