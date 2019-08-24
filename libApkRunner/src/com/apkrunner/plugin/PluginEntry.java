package com.apkrunner.plugin;

import com.apkrunner.ApkRunner;
import com.apkrunner.core.Frameworks;
import com.apkrunner.core.QLog;
import com.apkrunner.sdk.SdkManager;

import android.content.Context;
import android.os.Process;



/**
 * 插件入口
 * 
 * @author Yichou 2013-11-18 16:43:37
 *
 */
public final class PluginEntry {
	static final String TAG = "PluginEntry";
	
	static {
		QLog.d(TAG, "PluginEntry loaded in pid " + Process.myPid());

		ApkRunner.PLUGIN_MOD = true;
		ApkRunner.SDK_MOD = false;
		ApkRunner.APPLICATION_DECLARED = false;
		
		ApkRunner.PROXY_SERVICE_NAME = "com.maopaoke.mpkrunner.QService";
		ApkRunner.PROXY_ACTIVITY_NAME = "com.maopaoke.mpkrunner.QActivity";
		ApkRunner.ENTRY_ACTIVITY_NAME = "com.maopaoke.hsy.QActivity";
	}
	
	static Context sOuterContext;
	static boolean bInited = false;
	
	private static void checkInit(Context context) {
		if(!bInited) {
			Frameworks.YClassLoader.insertParent(context.getClassLoader(),
					PluginEntry.class.getClassLoader());
			
			bInited = true;
		}
	}	
	
	public static void init(Context context) {
		QLog.i(TAG, "-------- init -------");
		
		sOuterContext = context.getApplicationContext();
		ApkRunner.init(sOuterContext);
		SdkManager.setOuterContext(sOuterContext);
		checkInit(context);
	}

	public static void setProxyActivity(String name) {
		ApkRunner.PROXY_ACTIVITY_NAME = name;
	}

	public static void setEntryActivity(String name) {
		ApkRunner.ENTRY_ACTIVITY_NAME = name;
	}

	public static void setProxyService(String name) {
		ApkRunner.PROXY_SERVICE_NAME = name;
	}
	
	public static void runApk(Context context, String apkPath, boolean mainProc) {
		checkInit(context);
		
		ApkRunner.runApk(context, apkPath, mainProc);
	}
}
