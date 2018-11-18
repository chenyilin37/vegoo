package vegoo.stockcommon.bo;

import java.util.Date;
import java.util.Set;

import vegoo.stockcommon.dao.JgccmxDao;

public interface JgccmxService extends BoService{

	void insertJgccmx(JgccmxDao dao);

	boolean existJgccmx(String scode, Date reportDate, String shcode);

	void settleJgccmx(boolean reset);

	double getZzb(String stockCode, String gdCode, Date reportDate);
	double getLtzb(String stockCode, String gdCode, Date reportDate);
	double getLtgb(String stockCode, String gdCode, Date reportDate);
	double getLtsz(String stockCode, String gdCode, Date reportDate);

	Set<String> getGudongCodes(String stockCode, Date reportDate);

	Set<String> getStockCodes(Date reportDate);


}
