package vegoo.newstock.persistent.redis.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.core.dao.BlockDao;
import vegoo.newstock.core.services.StockDataService;
import vegoo.newstock.persistent.redis.RedisService;

public abstract class StockDataServiceRedis implements StockDataService {

	protected abstract RedisService getRedis();

		
	

	
}
