package vegoo.stockcommon.db.redis;

import vegoo.stockcommon.bo.BoService;

public interface RedisBoService extends BoService {
	String TBL_BLOCK   = "BLK";
	String TBL_STOCK   = "STK";
	String TBL_CAPITAL = "CPT";
	
	String TBL_FHSG = "FH";
	String TBL_GDHS = "HS";
	String TBL_JGCC = "JCG";
	String TBL_ZL   = "ZL";   // jgccmx\sdltgd
	String TBL_GUDONG = "GD";
	String TBL_KDAY = "KD";
	
	
}
