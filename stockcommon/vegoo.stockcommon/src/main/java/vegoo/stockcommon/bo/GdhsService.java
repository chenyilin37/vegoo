package vegoo.stockcommon.bo;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import vegoo.stockcommon.dao.GdhsDao;


public interface GdhsService extends BoService{

	boolean existGDHS(String stockCode, Date endDate);

	void insertGdhs(GdhsDao dao);

	void settleGdhs(boolean reset);

	Set<String> getEndDates(String stkcode);

	long getHolderNum(String stkcode, String sDate);
	long getHolderNum(String stkcode, Date dDate);

	Date getLatestEndDate(String stockCode, Date date);



	// Map<String, Object> queryGDHS(String stkCode, Date rDate);


}
