package vegoo.stockcommon.bo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import vegoo.stockcommon.dao.BlockDao;

public interface BlockService extends BoService{
	int BLOCKTYPE_HY = 1;
	int BLOCKTYPE_GN = 2;
	int BLOCKTYPE_DY = 3;
	int BLOCKTYPE_BOARD = 9;
	
	boolean existBlock(String blockUCode);

	void insertBlock(BlockDao dao);

	void deleteAllStocksOfBlock(String blkUcode);

	void insertStockOfBlock(String blkUcode, String stkCode);

	void updateStocksOfBlock(String blkUcode, Set<String> stockCodes);

	void deleteStockOfBlock(String blkUcode, String stkCode);

	Set<String> getBlocksOfStock(String stockcode);

	Set<String> getStockCodesOfBlock(String blkUcode);

	Collection<String> getBlockUCodes();

	
	
	
}
