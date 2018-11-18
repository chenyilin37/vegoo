package vegoo.stockcommon.bo;

import java.util.Date;
import java.util.List;

import vegoo.stockcommon.dao.KDayDao;

public interface KDayService extends BoService{
	Date getEarliestTradeDate(String stockCode);
	Date getLatestTradeDate(String stockCode);
	Date getLatestTradeDate(String stockCode, Date endDate);
	
	void saveKDayData(List<KDayDao> newItems);
	double getZhangFu(String stockCode, Date beginDate, Date endDate) throws Exception;
	double[] getMaxMinZhangFu(String stockCode, Date beginDate, Date endDate) throws Exception;
	double getOpen(String stockCode, Date transDate);
	double getHigh(String stockCode, Date transDate);
	double getLow(String stockCode, Date transDate);
	double getClose(String stockCode, Date transDate);

}
