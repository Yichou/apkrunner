package com.apkrunner.app;

import com.apkrunner.ApkRunner;
import com.apkrunner.core.QLog;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

/**
 * 
 * @author Yichou 2013-11-6 17:58:48
 * 
 */
public class RunnerApplication extends Application implements Thread.UncaughtExceptionHandler {
	static final String TAG = "RunnerApplication";
	
	
	static {
		QLog.d(TAG, "Runner Application loaded in proc " 
				+ ApkRunner.getCurProcessName()
				+ ", pid=" + Process.myPid());
		ApkRunner.staticInit();
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ApkRunner.log.error("=====uncaughtException: ", e);
		sendBroadcast(new Intent(ApkRunner.INTENT_ACTION_CRASH));
		Process.killProcess(Process.myPid());
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		Thread.setDefaultUncaughtExceptionHandler(this);
		super.attachBaseContext(base);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ApkRunner.APPLICATION_DECLARED = true;
		
		ApkRunner.init(getApplicationContext());
	}
}
