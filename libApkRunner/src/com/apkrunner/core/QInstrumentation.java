package com.apkrunner.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.apkrunner.utils.Singleton;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;



/**
 * 
 * @author Yichou 2013-9-26
 *
 */
public final class QInstrumentation extends InstrumentationProxy {
	static final String TAG = "inst";
	private Activity topActivity;

	
	public QInstrumentation(Instrumentation i) {
		super(i);
	}
	
	public Activity getTopActivity() {
		return topActivity;
	}
	
	private void attachActivityContext(Activity activity) {
		Context base = activity.getBaseContext();
		
		try {
			Field field = ContextWrapper.class.getDeclaredField("mBase");
			field.setAccessible(true);
			field.set(activity, null);
		} catch (Exception e) {
		}

		try {
			Method method = ContextThemeWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
			method.setAccessible(true);
			method.invoke(activity, new QContext(base));
		} catch (Exception e) {
			QLog.e(TAG, "activity attach new context fail!");
		}
	}

	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		QLog.d(TAG, "## new act " + className);
		
		FrameworkObserver.getDefault().dispatchNewActivity(className);
		
		return super.newActivity(cl, className, intent);
	}
	
	@Override
	public void callActivityOnCreate(Activity activity, Bundle arg1) {
		FrameworkObserver.getDefault().dispatchPerformCreateActivity(activity);
		
		attachActivityContext(activity);
//		activity.
		super.callActivityOnCreate(activity, arg1);
		
//		try {
//			super.callActivityOnCreate(activity, arg1);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		FrameworkObserver.getDefault().dispatchActivityCreate(activity);
	}
	
	@Override
	public void callActivityOnResume(Activity activity) {
		QLog.i(TAG, "## resume act " + activity.getClass().getName());

		super.callActivityOnResume(activity);
		topActivity = activity;
		
		FrameworkObserver.getDefault().dispatchActivityResume(activity);
	}
	
	@Override
	public void callActivityOnPause(Activity activity) {
		QLog.d(TAG, "## pause act " + activity.getClass().getName());
		
		super.callActivityOnPause(activity);
		if(activity == topActivity && activity.isFinishing())
			topActivity = null;
		
		FrameworkObserver.getDefault().dispatchActivityPause(activity);
	}
	
	@Override
	public void callActivityOnDestroy(Activity activity) {
		QLog.i(TAG, "## destroy act " + activity.getClass().getName());
		
		super.callActivityOnDestroy(activity);
		
//		try {
//			super.callActivityOnDestroy(activity);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		FrameworkObserver.getDefault().dispatchActivityDestroy(activity);
	}
	
	@Override
	public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		QLog.d(TAG, "## new app " + className);
		
		FrameworkObserver.getDefault().dispatchNewApplication(className);

		Application app = super.newApplication(cl, className, new QContext(context));
		return app;
	}
	
	@Override
	public void callApplicationOnCreate(Application app) {
		QLog.d(TAG, "## create app " + app.getClass().getName());
		
		FrameworkObserver.getDefault().dispatchApplicationCreate(app);
		
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//		}

		super.callApplicationOnCreate(app);
		
//		try {
//			super.callApplicationOnCreate(app);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private static final Singleton<QInstrumentation> gDefault = new Singleton<QInstrumentation>() {

		@Override
		protected QInstrumentation create() {
			return null;
		}

		@Override
		protected QInstrumentation create(Object object) {
			return new QInstrumentation((Instrumentation) object);
		}
	};
	
	public static QInstrumentation getDefault() {
		return gDefault.get();
	}
	
	public static void proxy() {
		Object inst = Frameworks.YActivityThread.getInstrumentation();
		
		if(!(inst instanceof QInstrumentation))
			Frameworks.YActivityThread.setInstrumentation(gDefault.get(inst));
	}
}
