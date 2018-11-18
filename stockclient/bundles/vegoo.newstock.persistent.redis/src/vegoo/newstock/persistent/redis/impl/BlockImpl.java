package vegoo.newstock.persistent.redis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.persistent.redis.RedisService;

public class BlockImpl extends BlockRedis {

	private String code;
	private List stocks;
	

	public BlockImpl(String blkCode, StockDataServiceImpl dataService) {
		super(dataService);
		this.code = blkCode;
	}
	

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return getStringValue(key4Block(code),"NAME");
	}

	@Override
	public Collection<Stock> getStocks() {
		if(stocks==null) {
			stocks = loadStocks();
		}
		return stocks;
	}
	
	private List<Stock> loadStocks() {
		
		List<Stock> result = new ArrayList<>();
		Set<String> stkCodes = getStockCodesOfBlock(getCode(), getRedis());
		stkCodes.forEach((code)->{
			Stock stock = dataService.getStock(code);
			if(stock!=null) {
				result.add(stock);
			}
		});
		return result;
	}





}
