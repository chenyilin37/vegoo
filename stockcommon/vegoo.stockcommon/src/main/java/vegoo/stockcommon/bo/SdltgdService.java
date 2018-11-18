package vegoo.stockcommon.bo;

import java.util.Date;

public interface SdltgdService extends BoService{

	boolean existSdgd(String scode, Date rdate, String sharehdcode);

	void insertSdgd(String companycode, String sharehdname, String sharehdtype, String sharestype, double rank,
			String scode, Date rdate, double sharehdnum, double ltag, double zb, Date ndate, String bz, double bdbl,
			String sharehdcode, double sharehdratio, double bdsum, boolean isLT);

	void settleSdltgd(boolean reset);

	double sumZzbOfTopN(String stkcode, Date rdate, int n);

	double sumLtzbOfTopN(String stkcode, Date rdate, int n);

	boolean existTop10(String stkcode, Date rdate);


}
