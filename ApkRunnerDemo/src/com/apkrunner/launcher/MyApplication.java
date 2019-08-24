package com.apkrunner.launcher;

import java.io.File;
import java.util.HashMap;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.app.RunnerApplication;
import com.apkrunner.core.FrameworkObserver.ApkListener;
import com.apkrunner.core.QActivityManager;
import com.apkrunner.core.QActivityManager.QStartActivityHandler;
import com.edroid.common.utils.FileUtils;
import com.edroid.common.utils.Logger;
import com.umeng.analytics.MobclickAgent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

public class MyApplication extends RunnerApplication implements ApkListener {
	static final String TAG = "app";
	static final Logger log = Logger.create("runnerapp");
	
	static MyApplication instance;
	
	public static String CHANNEL = "default";
//	public static String APP_CHANNEL = "";
	public static Context sContext;
	static Handler sHandler = new Handler(Looper.getMainLooper());
	
	boolean enableScript  = true;
	
	
	static {
		if(VERSION.SDK_INT >= 14)
			ApkRunner.NATIVE_HOOK = true;
		
		ApkRunner.APPDATA_TO_SD = false;
		
		ApkRunner.PROXY_ACTIVITY_NAME = "com.apkrunner.ProxyActivity";
		ApkRunner.PROXY_SERVICE_NAME = "com.apkrunner.ProxyService";
		ApkRunner.setMaxProcessCount(5);
		
		QActivityManager.getDefault().setStartActivityHandler(new QStartActivityHandler() {
			
			@Override
			public boolean onStartActivity(Intent intent) {
				
				if ("application/vnd.android.package-archive".equals(intent.getType())) { // 模拟安装
//					String file = intent.getData().getSchemeSpecificPart();
					
					return true;
				}
				
				return false;
			}
		});
	}
	
	public static void sendEventDelay(final String name, final String param, int delayMillis) {
		sHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				log.w("延迟事件：" + name + "=" + param);
				sendEvent(name, param);
			}
		}, delayMillis);
	}
	
	public static void sendEvent(String name, String param) {
		HashMap<String, String> map = new HashMap<String, String>(2);
		map.put(CHANNEL, param);
		MobclickAgent.onEvent(sContext, name, map);
	}
	
	public static String getParam(String name) {
		return MobclickAgent.getConfigParams(sContext, CHANNEL +"_"+ name);
	}
	
	@Override
	public void onCreate() {
		sContext = this;
		instance = this;
		sHandler = new Handler();
		
		if (!ApkRunner.isAppProcess()) {
			try {
				Bundle metaData = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).applicationInfo.metaData;
				CHANNEL = metaData.getString("UMENG_CHANNEL");
			} catch (NameNotFoundException e1) {
			}

			MobclickAgent.updateOnlineConfig(this);
		}
		
//		   IBinder b = ServiceManager.getService(Context.APP_OPS_SERVICE);
//		   System.out.println(b);
//		   IAppOpsService mAppOps = IAppOpsService.Stub.asInterface(b);
//		   System.out.println(mAppOps);
//
//		   IBinder b2 = ServiceManager.getService(Context.APP_OPS_SERVICE);
//		   System.out.println(b2);
//		   IAppOpsService mAppOps2 = IAppOpsService.Stub.asInterface(b2);
//		   System.out.println(mAppOps2);
		   
		super.onCreate();
	}
	
	private void rmdir(String path) {
		FileUtils.removeDir(new File(Environment.getExternalStorageDirectory(), path));
	}

	@Override
	public void onLoadFinish(ApkInfo apkInfo) {
		log.w("onApkLoaded: " + apkInfo);
		
		log.e("enableScript=" + enableScript);
		
		if(!enableScript)
			return;
	}

	@Override
	public void onLaunchFinish(ApkInfo apk) {
	}
	
	public static MyApplication getInstance() {
		return instance;
	}
}
