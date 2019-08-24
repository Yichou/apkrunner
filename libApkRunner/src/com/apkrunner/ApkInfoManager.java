package com.apkrunner;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageInfo;


/**
 * 已加载 apkinfo 管理器
 * 
 * @author YYichou 2014-3-4
 *
 */
public final class ApkInfoManager {

	private final HashMap<String, WeakReference<ApkInfo>> mPackges = 
		new HashMap<String, WeakReference<ApkInfo>>();
	

	/**
	 * 从缓存获取 ApkInfo 不存在返回 null
	 * @param context
	 * @param packageName
	 * @return
	 */
	public ApkInfo peekPackageInfo(Context context, String packageName) {
		return getApkInfo(context, packageName, null);
	}
	
	public ApkInfo getApkInfo(Context context, String apkPath) {
		PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
		return getApkInfo(context, packageInfo.packageName, apkPath);
	}
	
	/**
	 * 从缓存获取 ApkInfo 不存在则创建
	 * 
	 * @param context
	 * @param packageName
	 * @param apkPath
	 * @return
	 */
	public ApkInfo getApkInfo(Context context, String packageName, String apkPath) {
		synchronized (mPackges) {
			WeakReference<ApkInfo> ref = mPackges.get(packageName);

			ApkInfo apkInfo = ref != null ? ref.get() : null;
			if (apkInfo == null && apkPath != null) {
				apkInfo = new ApkInfo(context, apkPath);
				mPackges.put(packageName, new WeakReference<ApkInfo>(apkInfo));
			}

			return apkInfo;
		}
	}
}
