package com.hcp.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormat {
	
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_FORMAT2 = "yyyy-MM-dd HH:mm";
	
	public static String defaultFormat(Date date){
		return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).format(date);
	}
	
	/**
	 * Default format without second
	 * @param date
	 * @return
	 */
	public static String defaultFormat2(Date date){
		return new SimpleDateFormat(DEFAULT_FORMAT2, Locale.getDefault()).format(date);
	}
	
	public static Date defaultParse(String date) throws ParseException{
		return new SimpleDateFormat(DEFAULT_FORMAT, Locale.getDefault()).parse(date);
	}
}
