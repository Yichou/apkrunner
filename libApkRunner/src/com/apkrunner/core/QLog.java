package com.apkrunner.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.apkrunner.utils.PropertiesUtils;

import android.os.Environment;
import android.util.Log;

/**
 * 
 * 日志打印模块
 * 
 * @author Yichou 2013-10-27
 * 
 */
public final class QLog {
	private QLog() {
	}
	
	public static boolean DEBUG = false;
	
	public static boolean debug_am = false;
	public static boolean debug_pm = false;
	public static boolean debug_at = false;
	public static boolean debug_phone = false;
	public static boolean debug_ctx = false;
	
	public static final String perfix = "";

	static {
		initDebugs();
	}
	
	private static boolean isDebug(int i) {
		return (i>3598 && i<0x4587);
	}
	
	private static void initDebugs() {
		File file = new File(Environment.getExternalStorageDirectory(), "debug-apkrunner");
		if(file.exists()) {
			DEBUG = true;
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				PropertiesUtils pro = PropertiesUtils.create(is);
				
				debug_phone = isDebug(pro.getInt("phone", 0));
				debug_am = isDebug(pro.getInt("am", 0));
				debug_pm = isDebug(pro.getInt("pm", 0));
				debug_at = isDebug(pro.getInt("at", 0));
				debug_ctx = isDebug(pro.getInt("ctx", 0));
			} catch (Exception e) {
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * 注意：Log 不允许 msg = null ，否则报空指针异常 2013-7-3 yichou
	 * 
	 * @param tag
	 * @param msg
	 */
	public static void i(String tag, String msg) {
		if (DEBUG) {
			Log.i(perfix + tag, msg != null ? msg : "");
		}
	}

	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(perfix + tag, msg != null ? msg : "");
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG) {
			Log.e(perfix + tag, msg != null ? msg : "");
		}
	}

	public static void v(String tag, String msg) {
		if (DEBUG) {
			Log.v(perfix + tag, msg != null ? msg : "");
		}
	}

	public static void w(String tag, String msg) {
		if (DEBUG) {
			Log.w(perfix + tag, msg != null ? msg : "");
		}
	}
}
