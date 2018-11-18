package vegoo.newstock.persistent.redis.impl;

public interface GC {
	String KEY_BLOCKS = "BLOCKS";
	String KEY_STOCKS = "STOCKS";
	
	String TBL_BLOCK   = "BLK";
	String TBL_STOCK   = "STK";
	String TBL_CAPITAL = "CPT";
	
	String TBL_FHSG = "FH";
	String TBL_GDHS = "HS";
	String TBL_JGCC = "JCG";
	String TBL_ZL   = "ZL";   // jgccmx\sdltgd
	String TBL_GUDONG = "GD";
	String TBL_KDAY = "KD";

	
	
    static String getKey(String... fields) {
		String pattern = getPattern(fields.length);
		return String.format(pattern, (Object[])fields);
	}

    static String getPattern(int length) {
		StringBuffer sb = new StringBuffer("%s");
		for(int i=1;i<length;++i) {
			sb.append(":").append("%s");
		}
		return sb.toString();
	}
	
	
}
