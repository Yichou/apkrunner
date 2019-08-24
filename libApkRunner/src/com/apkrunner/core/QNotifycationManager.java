package com.apkrunner.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.sdk.SdkManager;
import com.apkrunner.utils.Singleton;


/**
 * 
 * @author Yichou 2013-9-26
 *
 */
public final class QNotifycationManager implements InvocationHandler {
	static final String TAG = "NM";
	
	private static final String mtd_enqueueNotificationWithTag = "enqueueNotificationWithTag";
	private static final String mtd_cancelAllNotifications = "cancelAllNotifications";
	private static final String mtd_cancelNotificationWithTag = "cancelNotificationWithTag";
	
	
	private Object mReal;

	
	private boolean match(String src, String dst) {
		return src.equals(Frameworks.decode(dst));
	}
	
	private QNotifycationManager() {
	}

//    void enqueueToast(String pkg, ITransientNotification callback, int duration);
//    void cancelToast(String pkg, ITransientNotification callback);
	
//    void enqueueNotificationWithTag(String pkg, String tag, int id,
//            in Notification notification, inout int[] idReceived, int userId);

//
//    void setNotificationsEnabledForPackage(String pkg, boolean enabled);
//    boolean areNotificationsEnabledForPackage(String pkg);
	
	public static int findParamIndex(Object[] args, Class<?> clazz) {
		for(int i=0; i<args.length; i++) {
			if(args[i]!=null && args[i].getClass().equals(clazz)) {
				return i;
			}
		}
		
		return -1;
	}

	public static int findParamIndexByValue(Object[] args, Object object) {
		for(int i=0; i<args.length; i++) {
			if(object.equals(args[i])) {
				return i;
			}
		}
		
		return -1;
	}
    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		method.setAccessible(true);
		String name = method.getName();
		
		ApkInfo runner = ApkRunner.currentRunning();
		if(runner == null)
			return method.invoke(mReal, args);
		
		QLog.i(TAG, "invoke " + name);
		
//	    void enqueueNotificationWithTag(String pkg, String tag, int id,
//      		in Notification notification, inout int[] idReceived, int userId);
		if(match(name, mtd_enqueueNotificationWithTag)) {
//			int i0 = findParamIndex(args, Notification.class);
//			Notification n = (Notification) args[i0];
//			
			SdkManager.onNotifycationReq();
			
//			
//			int i1 = findParamIndexByValue(args, runner.getPackgeName())
//			if(runner.getPackgeName().equals(args[0])) { //从应用发出
//				QLog.d(TAG, n.toString());
//				
//				if(n.contentView.getLayoutId() == com.android.internal.R.layout.notification_template_base) {
//					args[0] = runner.getMainPackgeName();
//				}
//				
//				//接收返回值
//				((int[])args[5])[0] = (Integer) args[2];
//			}
			
			//这里还是会有问题，擦
			
//			for(Object arg : args) {
//				if(ApkRunner.getShellPkg().equals(arg))
//					return method.invoke(mReal, args);
//			}
			
			return null;
		}
//	    void cancelNotificationWithTag(String pkg, String tag, int id, int userId);
		else if (match(name, mtd_cancelNotificationWithTag)) {
//			for(Object arg : args) {
//				if(ApkRunner.getShellPkg().equals(arg))
//					return method.invoke(mReal, args);
//			}
			
			return null;
		}
//		void cancelAllNotifications(String pkg, int userId);
		else if (match(name, mtd_cancelAllNotifications)) {
//			for(Object arg : args) {
//				if(ApkRunner.getShellPkg().equals(arg))
//					return method.invoke(mReal, args);
//			}
			
			return null;
		}
		
		return method.invoke(mReal, args);
	}
	
	private static final Singleton<QNotifycationManager> gDefault = new Singleton<QNotifycationManager>() {

		@Override
		protected QNotifycationManager create() {
			return new QNotifycationManager();
		}

		@Override
		protected QNotifycationManager create(Object object) {
			return null;
		}
	};
	
	public static QNotifycationManager getDefault() {
		return gDefault.get();
	}
	
	private Object mProxy = null;
	
	public Object getProxy(Object real) {
		synchronized (this) {
			if(mProxy == null) {
				this.mReal = real;
				mProxy = Proxy.newProxyInstance(real.getClass().getClassLoader(),
						real.getClass().getInterfaces(),
						this);
			}
		}
		
		return mProxy;
	}
	
	public static void proxy() {
		Object old = Frameworks.YNotificationManager.getNotificationManager();
		
		if (old != null && !(old instanceof QNotifycationManager)) {
			Frameworks.YNotificationManager.setNotificationManager(getDefault().getProxy(old));
		}
	}
}
