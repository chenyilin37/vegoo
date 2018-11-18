package vegoo.stockcommon.bo;

public interface GudongService extends BoService{

	// void saveGudong(String hdCode, String hdName, String hdType);

	boolean existGudong(String sHCode);

	// void inserteGudong(String SHCode, String SHName, String gdlx, String lxdm, String indtCode, String indtName);

	void saveGudong(String sHCode, String sHName, String gdlx, String lxdm, String indtCode, String instSName);

	String getLXName(String lxId);

	String getLXCode(String lxName);

}
