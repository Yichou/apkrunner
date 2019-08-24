package com.apkrunner.core;

import java.lang.reflect.Field;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.utils.Logger;
import com.apkrunner.utils.Singleton;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;


/**
 * 
 * @author Yichou 2013-9-29
 * 
 * <p> 2014-3-11 14:16:30 结构优化
 * 
 */
public final class QActivityThread {
	static final Logger log = Logger.create(QLog.debug_at, "AT");

	private Object real;
	private Handler mainHandler;
	private Handler mainHandler2;
	
	
	private QActivityThread() {
		this.real = Frameworks.YActivityThread.getActivityThreadObject();
		this.mainHandler = Frameworks.YActivityThread.getH();
		this.mainHandler2 = new Handler(Looper.getMainLooper());
	}
	
	public Object getReal() {
		return real;
	}
	
	public Handler getMainHandler() {
		return mainHandler;
	}
	
	public void runOnMainThread(Runnable r) {
//		mainHandler.post(r);
		mainHandler2.post(r);
	}

	private boolean handleLaunchActivity(Object object) {
		if (object == null) return false;
		
		Class<?> clazz = object.getClass();
		try {
			Field field = clazz.getDeclaredField("intent");
			field.setAccessible(true);

			Intent intent = (Intent) field.get(object);
			boolean proxyed = intent.getBooleanExtra(QActivityManager.FLAG_PROXY, false);
			if (proxyed) {
				Intent realIntent = intent.getParcelableExtra(QActivityManager.KEY_INTENT);
				ComponentName cmp = realIntent.getComponent();

				log.i("LAUNCH_ACTIVITY " + cmp);
				
				ApkInfo apkInfo = ApkRunner.currentRunning();
				ActivityInfo info = null;
				
				if(apkInfo == null) { //环境丢失
					log.w("apkInfo lost!");
					
					ApkRunner.restoreData();
					
//					Frameworks.YActivityThread.post(new Runnable() {
//						
//						@Override
//						public void run() {
//							Context context = ApkRunner.getApplicationContext();
//							context.startActivity(new Intent()
//								.setClassName(context, ApkRunner.ENTRY_ACTIVITY_NAME)
//								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//						}
//					});
					
					//must finish it
					try {
						Frameworks.YActivityManager.finishActivity((IBinder)ReflectHelper.getField(object, "token"),
								Activity.RESULT_CANCELED,
								null);
		            } catch (Exception ex) {
		            }

		            Process.killProcess(Process.myPid());
					
					return true;
				} else {
					info = apkInfo.getActivity(cmp.getClassName());
				}

				if(info == null) {
					throw new RuntimeException("activity info lost!");
				}
				
				field.set(object, realIntent);
				
				// replace the activityInfo
				ReflectHelper.setField(object, "activityInfo", info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void handleReceiver(Object object) {
		try {
			Class<?> clazz = object.getClass();
			Field field = clazz.getDeclaredField("intent");
			field.setAccessible(true);

			Intent intent = (Intent) field.get(object);
			boolean proxyed = intent.getBooleanExtra(QActivityManager.FLAG_PROXY, false);
			if (proxyed) {
				Intent realIntent = intent.getParcelableExtra(QActivityManager.KEY_INTENT);
				ComponentName cmp = realIntent.getComponent();

				log.i("RECEIVER " + cmp);

				ApkInfo apkInfo = ApkRunner.currentRunning();
				ActivityInfo info = apkInfo.getReceiver(cmp.getClassName());
				if (info != null) {
					field.set(object, realIntent);

					field = clazz.getDeclaredField("info");
					field.setAccessible(true);
					field.set(object, info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleCreateService(Object object) {
		try {
			ServiceInfo info = (ServiceInfo) ReflectHelper.getField(object, "info");
			log.d("CREATE_SERVICE " + info.name);
			
			if(info.name.startsWith(ApkRunner.PROXY_SERVICE_NAME)) { //代理
				info.name = QRunnerService.class.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final class MyCallback implements Handler.Callback {
		
		@Override
		public boolean handleMessage(Message msg) {
			log.d("handleMessage what=" + Integer.toString(msg.what));
			
			// 按调用的可能性排列
			if (msg.what == Frameworks.YActivityThread.YH.LAUNCH_ACTIVITY) { // LAUNCH_ACTIVITY
				if(handleLaunchActivity(msg.obj))
					return true;
			}
			else if (msg.what == Frameworks.YActivityThread.YH.RECEIVER) {
				handleReceiver(msg.obj);
			}
			else if (msg.what == Frameworks.YActivityThread.YH.CREATE_SERVICE) {
				handleCreateService(msg.obj);
			} 
			else if (msg.what == 126) {
//				throw new RuntimeException("");
			}
			
			return false;
		}
	}
	
	private void checkCallback() {
		try {
			//warn h.getClass().getSuperclass() just get the Handler class
			Field field = mainHandler.getClass().getSuperclass().getDeclaredField("mCallback");
			field.setAccessible(true);
			
			Object cur = field.get(mainHandler);
			
			if(cur == null || !(cur instanceof MyCallback)) {
				field.set(mainHandler, new MyCallback());
				log.i("now my callback work!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final Singleton<QActivityThread> gDefault = new Singleton<QActivityThread>() {
		
		@Override
		protected QActivityThread create() {
			return new QActivityThread();
		}
	};
	
	public static QActivityThread getDefault() {
		return gDefault.get();
	}

	public static void proxy() {
		getDefault().checkCallback();
	}
}
