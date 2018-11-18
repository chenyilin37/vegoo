package vegoo.newstock.core.services;

import java.util.Collection;
import java.util.Set;

import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.core.dao.BlockDao;

public interface StockDataService {

	Block getBlock(String blockCode);
	Stock getStock(String stockCode);

	Collection<Stock> getStocksOfBlock(String blockCode);


}
