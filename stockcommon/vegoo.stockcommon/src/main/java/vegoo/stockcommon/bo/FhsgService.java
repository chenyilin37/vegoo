package vegoo.stockcommon.bo;

import java.util.Date;

import vegoo.stockcommon.dao.FhsgDao;


public interface FhsgService extends BoService{

	boolean existFhsg(String sCode, Date rDate);

	void insertFhsg(FhsgDao dao);

	double adjustWithHoldNum(Date reportDate, String scode, double hdNum);

	double calcLClose(String stockcode, Date transdate, double lClose);

	boolean isNewFhsg(String code, Date rDate, int dataTag, boolean deleteOld);

	void settleFhsg(boolean reset);

}
