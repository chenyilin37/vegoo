package vegoo.commons;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Strings;
import com.google.gson.*;


public class JsonUtil {
	// 查看eastmoney数据的日期格式
	private static Gson gson = null;
    
	public static Date parseDate(String value) throws ParseException  {
		if(Strings.isNullOrEmpty(value) ) {
			throw new ParseException("null date String",0);
		}
		
		if(value.length()>"yyyy-MM-dd".length()) {
			return DateUtil.parseDateTime(value);
		}else {
			return DateUtil.parseDate(value);
		}
	}
	
	public static String checkDateString(String value) {
		try {
			 parseDate(value);
			 return value;
		} catch (ParseException e) {
			return null;
		}
	}
	
    private static Gson getGson() {
	    	if(gson == null) {
	    	   	GsonBuilder builder = new GsonBuilder();
	    	   	
	    	    builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
	    	        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	    	           String value = json.getAsString(); 
	    	        	try {
	    	                return parseDate(value);
	    	            } catch (Exception e) {
	    	            	return null;
	    	            }    	
	    	        }
	    	    });
	    	    
	    	    builder.registerTypeAdapter(double.class, new JsonDeserializer<Double>() {
	    	        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	    	        	   try {
	    	                return Double.parseDouble(json.getAsString());
	    	            } catch (Exception e) {
	    	            	return  0.0;
	    	            }    	
	    	        }
	    	    });
	    	    
	    	    builder.registerTypeAdapter(int.class, new JsonDeserializer<Integer>() {
	    	        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
	    	        	   try {
	    	                return Integer.parseInt(json.getAsString());
	    	            } catch (Exception e) {
	    	            	return  0;
	    	            }    	
	    	            
	    	        }
	    	    });
	    	    gson = builder.create();    		
	    	  }
	    	  return gson;
    }

    public static <T> T fromJson(String json, Class<T> t) {
    	   return getGson().fromJson(json, t);
    }
    
    public static String toJson(Object object) {
    	   return getGson().toJson(object);
    }
    
}