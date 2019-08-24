package com.apkrunner.sdk;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.core.Frameworks;
import com.apkrunner.core.QActivityThread;
import com.apkrunner.core.QLog;
import com.apkrunner.core.QRunnerService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;




/**
 * 插件入口
 * 
 * @author Yichou 2013-11-18 16:43:37
 *
 */
public final class SdkEntry {
	public interface ApkLoadCallback {
		public void onSuccess(ApkInfo info);
	}
	
	static {
		QLog.i("", "SdkEntry loaded in pid " + Process.myPid());

		ApkRunner.PLUGIN_MOD = true;
		ApkRunner.SDK_MOD = true;
		ApkRunner.APPLICATION_DECLARED = false;
		
		ApkRunner.PROXY_SERVICE_NAME = "com.maopaoke.mpkrunner.QService";
		ApkRunner.PROXY_ACTIVITY_NAME = "com.maopaoke.mpkrunner.QActivity";
	}
	
	static boolean bInited = false;
	
	private static void checkInit(Context context) {
		if(!bInited) {
			Frameworks.YClassLoader.insertParent(context.getClassLoader(),
					SdkEntry.class.getClassLoader());
			
			bInited = true;
		}
	}
	
	public static void setProxyActivity(String name) {
		ApkRunner.PROXY_ACTIVITY_NAME = name;
	}

	public static void setProxyService(String name) {
		ApkRunner.PROXY_SERVICE_NAME = name;
	}
	
//	public static void loadApk(Context context, String apkPath, final ApkLoadCallback callback) {
//		ApkRunner.attachShellContext(context.getApplicationContext());
//		checkInit(context);
//		QActivityThread.proxy();
//
//		final ApkInfo apkInfo = ApkRunner.getApkInfo(context, apkPath);
//		
//		Intent intent = new Intent(ApkRunner.INTENT_ACTION_LAUNCH);
//		intent.setClassName(context, ApkRunner.getProxyServiceName());
//		context.getApplicationContext().stopService(intent);
//		
//		context.getApplicationContext().bindService(intent, new ServiceConnection() {
//			
//			@Override
//			public void onServiceDisconnected(ComponentName name) {
//			}
//			
//			@Override
//			public void onServiceConnected(ComponentName name, IBinder service) {
//				System.out.println("ManagerService connected!");
//				
//				QRunnerService managerService = ((QRunnerService.LocalBinder)service).getService();
//				managerService.setApkInfo(apkInfo);
//				apkInfo.attachManagerService(managerService);
//				ApkRunner.setCurrentRunning(apkInfo);
//				
//				callback.onSuccess(apkInfo);
//			}
//		}, Service.BIND_AUTO_CREATE);
//	}
}
