package vegoo.newstock.core.bo;

import java.util.Collection;
import java.util.Set;

import vegoo.newstock.core.dao.BlockDao;


public interface Block {

	String getCode();
	String getName();
	Collection<Stock> getStocks();
	

}
