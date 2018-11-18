package vegoo.stockcommon.bo;

import java.util.Date;
import java.util.List;
import java.util.Set;

import vegoo.stockcommon.dao.StockCapitalDao;
import vegoo.stockcommon.dao.StockDao;

public interface StockService extends BoService{
	int MARKET_SH = 1;
	int MARKET_SZ = 2;

	boolean existStock(String stkCode);

	void insertStock(StockDao dao);
	void updateStock(String stkCode, String name);

	boolean existStockCapital(String stkcode, Date rDate);

	void insertStockCapital(StockCapitalDao dao);

	Set<String> getStockCodes();

	Set<String> getStockUCodes();

	Set<String> getStockCodes(int marketId);




}
