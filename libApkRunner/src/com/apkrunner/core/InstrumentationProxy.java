package com.apkrunner.core;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InstrumentationProxy extends Instrumentation {
	private Instrumentation mI;

	
	public InstrumentationProxy(Instrumentation i) {
//		if(i.getClass().getName().equals(Instrumentation.class.getClass().getName()))
//			mI = this.super;
		mI = i;
	}

	@Override
	public void onCreate(Bundle arguments) {

		mI.onCreate(arguments);
	}

	@Override
	public void start() {

		mI.start();
	}

	@Override
	public void onStart() {

		mI.onStart();
	}

	@Override
	public boolean onException(Object obj, Throwable e) {

		return mI.onException(obj, e);
	}

	@Override
	public void sendStatus(int resultCode, Bundle results) {

		mI.sendStatus(resultCode, results);
	}

	@Override
	public void finish(int resultCode, Bundle results) {

		mI.finish(resultCode, results);
	}

	@Override
	public void setAutomaticPerformanceSnapshots() {

		mI.setAutomaticPerformanceSnapshots();
	}

	@Override
	public void startPerformanceSnapshot() {

		mI.startPerformanceSnapshot();
	}

	@Override
	public void endPerformanceSnapshot() {

		mI.endPerformanceSnapshot();
	}

	@Override
	public void onDestroy() {

		mI.onDestroy();
	}

	@Override
	public Context getContext() {

		return mI.getContext();
	}

	@Override
	public ComponentName getComponentName() {

		return mI.getComponentName();
	}

	@Override
	public Context getTargetContext() {

		return mI.getTargetContext();
	}

	@Override
	public boolean isProfiling() {

		return mI.isProfiling();
	}

	@Override
	public void startProfiling() {

		mI.startProfiling();
	}

	@Override
	public void stopProfiling() {

		mI.stopProfiling();
	}

	@Override
	public void setInTouchMode(boolean inTouch) {

		mI.setInTouchMode(inTouch);
	}

	@Override
	public void waitForIdle(Runnable recipient) {

		mI.waitForIdle(recipient);
	}

	@Override
	public void waitForIdleSync() {

		mI.waitForIdleSync();
	}

	@Override
	public void runOnMainSync(Runnable runner) {

		mI.runOnMainSync(runner);
	}

	@Override
	public Activity startActivitySync(Intent intent) {

		return mI.startActivitySync(intent);
	}

	@Override
	public void addMonitor(ActivityMonitor monitor) {

		mI.addMonitor(monitor);
	}

	@Override
	public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {

		return mI.addMonitor(filter, result, block);
	}

	@Override
	public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {

		return mI.addMonitor(cls, result, block);
	}

	@Override
	public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {

		return mI.checkMonitorHit(monitor, minHits);
	}

	@Override
	public Activity waitForMonitor(ActivityMonitor monitor) {

		return mI.waitForMonitor(monitor);
	}

	@Override
	public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {

		return mI.waitForMonitorWithTimeout(monitor, timeOut);
	}

	@Override
	public void removeMonitor(ActivityMonitor monitor) {

		mI.removeMonitor(monitor);
	}

	@Override
	public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {

		return mI.invokeMenuActionSync(targetActivity, id, flag);
	}

	@Override
	public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {

		return mI.invokeContextMenuAction(targetActivity, id, flag);
	}

	@Override
	public void sendStringSync(String text) {

		mI.sendStringSync(text);
	}

	@Override
	public void sendKeySync(KeyEvent event) {

		mI.sendKeySync(event);
	}

	@Override
	public void sendKeyDownUpSync(int key) {

		mI.sendKeyDownUpSync(key);
	}

	@Override
	public void sendCharacterSync(int keyCode) {

		mI.sendCharacterSync(keyCode);
	}

	@Override
	public void sendPointerSync(MotionEvent event) {

		mI.sendPointerSync(event);
	}

	@Override
	public void sendTrackballEventSync(MotionEvent event) {

		mI.sendTrackballEventSync(event);
	}

	@Override
	public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		return mI.newApplication(cl, className, context);
	}

	@Override
	public void callApplicationOnCreate(Application app) {

		mI.callApplicationOnCreate(app);
	}

	@Override
	public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id,
			Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {

		return mI.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		return mI.newActivity(cl, className, intent);
	}

	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {

		mI.callActivityOnCreate(activity, icicle);
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {

		mI.callActivityOnDestroy(activity);
	}

	@Override
	public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {

		mI.callActivityOnRestoreInstanceState(activity, savedInstanceState);
	}

	@Override
	public void callActivityOnPostCreate(Activity activity, Bundle icicle) {

		mI.callActivityOnPostCreate(activity, icicle);
	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {

		mI.callActivityOnNewIntent(activity, intent);
	}

	@Override
	public void callActivityOnStart(Activity activity) {

		mI.callActivityOnStart(activity);
	}

	@Override
	public void callActivityOnRestart(Activity activity) {

		mI.callActivityOnRestart(activity);
	}

	@Override
	public void callActivityOnResume(Activity activity) {

		mI.callActivityOnResume(activity);
	}

	@Override
	public void callActivityOnStop(Activity activity) {

		mI.callActivityOnStop(activity);
	}

	@Override
	public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {

		mI.callActivityOnSaveInstanceState(activity, outState);
	}

	@Override
	public void callActivityOnPause(Activity activity) {

		mI.callActivityOnPause(activity);
	}

	@Override
	public void callActivityOnUserLeaving(Activity activity) {

		mI.callActivityOnUserLeaving(activity);
	}

	@Override
	public void startAllocCounting() {

		mI.startAllocCounting();
	}

	@Override
	public void stopAllocCounting() {

		mI.stopAllocCounting();
	}

	@Override
	public Bundle getAllocCounts() {

		return mI.getAllocCounts();
	}

	@Override
	public Bundle getBinderCounts() {

		return mI.getBinderCounts();
	}

}
