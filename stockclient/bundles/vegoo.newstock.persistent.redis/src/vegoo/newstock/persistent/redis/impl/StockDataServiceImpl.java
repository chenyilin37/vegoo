package vegoo.newstock.persistent.redis.impl;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.core.services.StockDataService;
import vegoo.newstock.persistent.redis.RedisService;
import vegoo.newstock.persistent.redis.core.RedisServiceImpl;

@Component(
		name="StockDataService",
		immediate=true,
		configurationPid = "vegoo.redis",
		service = {StockDataService.class}		
)
public class StockDataServiceImpl extends StockDataServiceRedis {
	private static final Logger logger = LoggerFactory.getLogger(StockDataServiceImpl.class);
	static final String PN_HOST   = "host";
	static final String PN_PORT   = "port";
	static final String PN_PASSWORD   = "password";

	private Map<String, Block> _block_Cache = null;
	private Map<String, Stock> _stock_Cache = null;

	private RedisService redis;
	private String host = "127.0.0.1";
	private String password;
	private int port = 6379;
	
	public StockDataServiceImpl() {
		System.out.println("StockDataServiceImpl created......");
	}
	
	@Modified
	public void updated(BundleContext context, Map<String, String> properties){
		System.out.println("StockDataServiceImpl updated......");

		this.password = properties.get(PN_PASSWORD);
		
		String shost = properties.get(PN_HOST);
		if(Strings.isNullOrEmpty(shost)) {
			this.host = shost;
		}
		
		try {
			String sport = (String) properties.get(PN_PORT);
			this.port = Integer.parseInt(sport);
		}catch(Exception e) {
			
		}
	}
	@Deactivate
	public void deactivate() {
		if(redis!=null) {
			((RedisServiceImpl)redis).destroyJedis();
		}
	}
	
	private RedisService createRedisService(String host, int port, String password) {
		RedisServiceImpl result = new RedisServiceImpl();
		result.initJedis(host, port, password);
		return result;
	}
	
	@Override
	protected RedisService getRedis() {
		if(redis==null) {
			this.redis = createRedisService(host, port, password);
		}
		
		return redis;
	}

	@Override
	public Block getBlock(String blockCode) {
		if(_block_Cache == null) {
			_block_Cache = loadBlocks();
		}
		
		return _block_Cache.get(blockCode);
	}

	@Override
	public Stock getStock(String stockCode) {
		if(_stock_Cache == null) {
			_stock_Cache = loadStocks();
		}
		
		return _stock_Cache.get(stockCode);
	}

	protected Map<String, Block> loadBlocks() {
		Map<String, Block> result = new HashMap<>();
		
		Set<String> blkCodes = BlockImpl.getBlockUCodes(getRedis());
		
		blkCodes.forEach((blkCode)->{
			result.put(blkCode, new BlockImpl(blkCode, this));
		});
		
		return result;
	}
	
	protected Map<String, Stock> loadStocks() {
		Map<String, Stock> result = new HashMap<>();
		
		Set<String> stkCodes = StockImpl.getStocks(getRedis());
		
		stkCodes.forEach((code)->{
			result.put(code, new StockImpl(code, this));
		});
		
		return result;
	}
	
	@Override
	public Collection<Stock> getStocksOfBlock(String blockCode) {
		Block block = getBlock(blockCode);
		if(block==null) {
			return null;
		}
		return block.getStocks();
	}


	
	
	
	

}
