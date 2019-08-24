package com.edroid.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * 时间、日期、本地化 相关工具类
 * 
 * @author Yichou 2013-6-24
 *
 */
public final class TimeUtils {
	/**
	 * @return 当前地域
	 */
	public static Locale getLocale(){
		return Locale.getDefault();
	}
	
	static final String DEF_DATE_TIME_TEMPLATE = "yyyy-MM-dd HH:mm:ss";
	static final String DEF_DATE_TEMPLATE = "yyyy-MM-dd";
	static final String DEF_TIME_TEMPLATE = "HH:mm:ss";
	
	public static String dayOfYearToDate(int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR, day);
		
		SimpleDateFormat format = new SimpleDateFormat("M.d");
		return format.format(c.getTime());
	}
	
	/**
	 * @return 形式  2013/12/12 12:23:32
	 */
	public static String getDateTimeNow() {
		return getNow(DEF_DATE_TIME_TEMPLATE);
	}
	
	/**
	 * @return 形式 2013-04-21
	 */
	public static String getDateNow() {
		return getNow(DEF_DATE_TEMPLATE);
	}

	/**
	 * @return 形式 3.2
	 */
	public static String getDateNowM() {
		return getNow("M.d");
	}
	
	public static String getTimeNow() {
		return getNow(DEF_TIME_TEMPLATE);
	}
	
	/**
	 * @return 形式  12:23:32
	 */
	public static String getTimeNow2() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", getLocale());
		return sdf.format(new Date());
	}

	/**
	 * @param template 格式化模板
	 * 
	 * @return 按模板格式化后的结果
	 */
	public static String getNow(String template) {
		SimpleDateFormat sdf = new SimpleDateFormat(template, getLocale());
		return sdf.format(new Date());
	}
	
	/**
	 * @return 形式  yyyy-MM-dd HH:mm:ss
	 */
	public static String formatDate(long ms) {
		return formatDate(ms, DEF_DATE_TIME_TEMPLATE);
	}
	
	public static String getDateTime(long ms) {
		return formatDate(ms, DEF_DATE_TIME_TEMPLATE);
	}

	public static String getTime(long ms) {
		return formatDate(ms, DEF_TIME_TEMPLATE);
	}

	public static String getDate(long ms) {
		return formatDate(ms, DEF_DATE_TEMPLATE);
	}
	
	public static String formatDate(long ms, String template) {
		// 取系统时间
		SimpleDateFormat format = new SimpleDateFormat(template, getLocale());
		return format.format(new Date(ms));
	}
	
	
	
	public static int getDayOfYear() {
		return Calendar.getInstance(getLocale()).get(Calendar.DAY_OF_YEAR);
	}
	
	public static int getHourOfDay() {
		return Calendar.getInstance(getLocale()).get(Calendar.HOUR_OF_DAY);
	}
	
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}
}
