package vegoo.newstock.persistent.redis.impl;


import java.util.Set;

import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.persistent.redis.RedisService;

public class StockImpl extends StockRedis implements Stock {

	private String code;

	
	public StockImpl(String code, StockDataServiceImpl dataService) {
		super(dataService);
		
		this.code = code;
	}
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return this.getStringValue(key4Stock(getCode()), "NAME");
	}

	@Override
	public int getMarketId() {
		
		return 0;
	}



}
