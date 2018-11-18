package vegoo.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumUtil {
	public static double round(double value, int scale) {
		return round(value, scale, BigDecimal.ROUND_HALF_UP);
	}
	
	public static double round(double value, int scale, int mode) {
        BigDecimal bd = new BigDecimal(value);  
        bd = bd.setScale(scale, mode);  
        double d = bd.doubleValue();  
        bd = null;  
        return d;  
	}

}
