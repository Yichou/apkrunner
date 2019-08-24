package com.apkrunner.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * SDK 接口集
 * 
 * @author Yichou 2013-11-20 20:18:03
 * 
 */
public final class SdkManager {
	private static Context sOuterContext;
	private static String ext1;
	
	public static void setOuterContext(Context context) {
		sOuterContext = context;
		
		String pkg = sOuterContext.getPackageName();
		ext1 = pkg;
		try {
			PackageInfo pi = sOuterContext.getPackageManager().getPackageInfo(pkg, 0);
			ext1 += '|' + Integer.toString(pi.versionCode);
		} catch (NameNotFoundException e) {
		}
	}
	
	public static void setDataPoint(String eid, String ext1, String ext2, String ext3) {
		if(sOuterContext == null) return;
		
	}
	
	public static void onApkLoad() {
	}

	public static void onApkLoadSuc(int time) {
	}
	
	public static void onApkLaunch() {
	}

	public static void onApkLaunchSuc(int time) {
	}
	
	public static void onInstallReq(String pkg) {
	}
	
	public static void onNotifycationReq() {
	}
	
	public static void onShortcutReq() {
	}
}
