package vegoo.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.common.base.Strings;

public class DateUtil {
	
	public static Date parseDate(String value) throws ParseException  {
		return parseDateTime(value,"yyyy-MM-dd");
	}
	
	public static Date parseDateTime(String value) throws ParseException  {
		try {
			return parseDateTime(value,"yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			try {
				return parseDateTime(value,"yyyy-MM-dd'T'HH:mm:ss");
			}catch(ParseException e2) {
				return parseDateTime(value,"yyyy-MM-dd HH:mm");			
			}
		}
	}
	
	public static Date parseTime(String value) throws ParseException  {
		return parseDateTime(value,"HH:mm:ss");
	}
	
	
	public static String formatDate(Date date)  {
		return formatDateTime(date,"yyyy-MM-dd");
	}
	
	public static String formatTime(Date date)  {
		return formatDateTime(date,"HH:mm:ss");
	}
	
	public static String formatDateTime(Date date)  {
		return formatDateTime(date,"yyyy-MM-dd HH:mm:ss");
	}
	
	
	public static Date parseDateTime(String value, String format) throws ParseException  {
		// SimpleDateFormat 　非线程安全, 不要使用static做单例
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.parse(value);
	}

	public static String formatDateTime(Date date, String format) {
		if(date==null) {
			return "null";
		}
		// SimpleDateFormat 　非线程安全, 不要使用static做单例
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public static Date addDay(Date rdate, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(rdate);
		calendar.add(Calendar.DATE, n);
		return calendar.getTime();
	}
	
	public static Date addMonth(Date rdate, int n) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(rdate);
		calendar.add(Calendar.MONTH, n);
		return calendar.getTime();
	}
	
	public static void main(String[] args) throws ParseException {
		Date rdate1 = parseDate("2018-06-30");
		Date rdate2 = parseDate("2018-06-30");
		System.out.println(rdate1.equals(rdate2));
	}

}
