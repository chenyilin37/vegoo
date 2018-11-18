package vegoo.stockcommon.bo;

import java.util.Date;
import java.util.Set;

import vegoo.stockcommon.dao.JgccDao;

public interface JgccService extends BoService{
    int JG_JIJIN = 1;   // 基金
    int JG_QFII = 2;    // 
    int JG_SHEBAO = 3;   //社保
    int JG_QUANSHANG = 4; //券商
    int JG_BAOXIAN = 5;  // 保险
    int JG_XINTUO = 6;   // 信托
	
	
	boolean isNewJgcc(JgccDao dao, boolean deleteOld);
	boolean existJgcc(String stockCode, Date reportDate);
	public void insertJgcc(JgccDao dao);
	void settleJgcc(boolean reset);
	
	double getLtzb(String stockCode, Date rdate, int jglx);
	double getLtzb(String stockCode, Date rdate, int[] jglxs);
	double getZzb(String stkcode, Date rDate, int[] jglxs);
	double getZzb(String stkcode, Date rDate, int jglx);
	
	double getAmount(String stockCode, Date rdate, int jglx);
	double getVolume(String stockCode, Date rdate, int jglx);
	double getCount(String stockCode, Date rdate, int jglx);
	
}
