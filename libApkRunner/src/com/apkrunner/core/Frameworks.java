package com.apkrunner.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apkrunner.ApkRunner;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Surface;



/**
 * framework 方法调用封装
 * 
 * @author Yichou 2013-10-27
 *
 */
public final class Frameworks {
	static final String TAG = "fw";
	
	private static boolean B_LOG_EX = true;
//	public static final Class<?>[] SIG_PARAM_TYPES_NULL = null;
//	public static final Object[] SIG_PARAMS_NULL = null;
	
	/**
	  * 预留字符加密
	 */
	public static String decode(String src) {
		return src;
	}
	
	public static Object getField(Object object, String name) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(object);
		} catch (Throwable e) {
			if(B_LOG_EX) e.printStackTrace();
		}
		return null;
	}
	
	public static int getStaticIntField(Class<?> clazz, String name, int def) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field.getInt(null);
		} catch (Throwable e) {
			if(B_LOG_EX) e.printStackTrace();
		}
		
		return def;
	}

	public static void setStaticField(Class<?> clazz, String name, Object newValue) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			field.set(null, newValue);
		} catch (Throwable e) {
			if(B_LOG_EX) e.printStackTrace();
		}
	}

	public static void setField(Object object, String name, Object newValue) {
		setField(object, name, newValue, true);
	}
	
	public static void setField(Object object, String name, Object newValue, boolean printError) {
		try {
			Field field = object.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(object, newValue);
		} catch (Throwable e) {
			if(printError && B_LOG_EX) e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	public static void disableDeathOnNetwork() {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy
				.Builder().permitNetwork()
				.build();
			StrictMode.setThreadPolicy(policy);
		}
	}
	
	public static final class YApplicationLoaders {
		/**
		 * 把 ClassLoader 缓存到全局，避免系统创建 PathClassloader
		 * 
		 * 此法无效
		 */
		public static void putClassLoader(String zip, ClassLoader classLoader) {
//			try {
//				Class<?> clazz = Class.forName("android.app.ApplicationLoaders");
//				
//				Method method = clazz.getDeclaredMethod("getDefault", (Class<?>[])null);
//				method.setAccessible(true);
//				Object object = method.invoke(null, (Object[])null);
//				
//				Field field = clazz.getDeclaredField("mLoaders");
//				field.setAccessible(true);
//				
//				@SuppressWarnings("unchecked")
//				Map<String, ClassLoader> map = (Map<String, ClassLoader>) field.get(object);
//				map.put(mApkPath, mClassLoader);
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
		}
	}

	public static final class YClassLoader {
		public static final String cls = "java.lang.ClassLoader";
		
		public static final String fld_parent = "parent";
		
		
		public static void insertParent(ClassLoader src, Object parent) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				ClassLoader oldParent = src.getParent();
				
				Field field = clazz.getDeclaredField(decode(fld_parent));
				field.setAccessible(true);
				
				field.set(src, parent);
				field.set(parent, oldParent);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}

		public static void setParent(Object src, Object parent) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				Field field = clazz.getDeclaredField(decode(fld_parent));
				field.setAccessible(true);
				field.set(src, parent);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YApplication {
		public static final String cls = "android.app.Application";
		public static final String mtd_attach = "attach";

		
		public static void attach(Object application, Object context) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				Method method = clazz.getDeclaredMethod(decode(mtd_attach), Context.class);
				method.setAccessible(true);
				method.invoke(application, context);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YCompatibilityInfo {
		public static final String cls = "android.content.res.CompatibilityInfo";
	}
	
	public static final class YContextImpl {
		public static final String cls = "android.app.ContextImpl";

		public static final String mtd_init = "init";
		public static final String mtd_setOuterContext = "setOuterContext";
		public static final String mtd_getReceiverRestrictedContext = "getReceiverRestrictedContext";

		public static final String fld_mPackageInfo = "mPackageInfo";
		public static final String fld_SYSTEM_SERVICE_MAP = "SYSTEM_SERVICE_MAP";
		public static final String fld_mPackageManager = "mPackageManager";
		public static final String fld_mBasePackageName = "mBasePackageName";
		public static final String fld_mOpPackageName = "mOpPackageName";
		
		
		public static void setPackageManager(Object contextImpl, Object pm) {
			setField(contextImpl, decode(fld_mPackageManager), pm);
		}
		
		public static Object getLoadedApk(Object contextImpl) {
			return getField(contextImpl, decode(fld_mPackageInfo));
		}
		
		public static Context getReceiverRestrictedContext(Object contextImpl) {
			try {
				Method method = contextImpl.getClass().getDeclaredMethod(decode(mtd_getReceiverRestrictedContext));
				method.setAccessible(true);
				return (Context) method.invoke(contextImpl);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		public static void setOuterContext(Object impl, Object outer) {
			try {
				Method method = impl.getClass().getDeclaredMethod(decode(mtd_setOuterContext), 
						Context.class);
				method.setAccessible(true);
				method.invoke(impl, outer);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
		
		public static Object getServiceFetcher(String serviceName) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Field field = clazz.getDeclaredField(decode(fld_SYSTEM_SERVICE_MAP));
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) field.get(null);
				return map.get(serviceName);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		public static void registerService(String serviceName, Object fetcher) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Field field = clazz.getDeclaredField(decode(fld_SYSTEM_SERVICE_MAP));
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) field.get(null);
				map.put(serviceName, fetcher);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
		
		/**
		 * android 4.3 ContextImpl 增加了2 方法 getBasePackgeName 和 getOpPackgeName
		 * 在进程交互的地方读写 Parcel，需要传包名的时候会使用 getOpPackgeName ，即它的宿主包名，这样我们这里帮他主动
		 * 设置这2个参数，就避免了很多动态代理改包名！
		 * 
		 * @param object
		 * @param out
		 */
		public static void setBasePackgeName(Object object, Context out) {
			setField(object, fld_mBasePackageName, out.getPackageName(), false);
			setField(object, fld_mOpPackageName, out.getPackageName(), false);
		}
		
		public static boolean isNewMod() {
			try {
				Context.class.getDeclaredMethod("getBasePackageName");
				return true;
			} catch (Throwable e) {
			}
			
			return false;
		}
	}
	
	public static final class YLoadedApk {
		public static final String fld_mClassLoader = "mClassLoader";
		public static final String fld_mResources = "mResources";
		public static final String fld_mApplication = "mApplication";
		
		public static final String mtd_makeApplication = "makeApplication";
		public static final String mtd_getResources = "getResources";

		
		public static Object getCompatibilityInfo(Object loadedApk) {
			// CompatibilityInfo mCompatibilityInfo; //2.3.1~2.3.7
			// CompatibilityInfoHolder mCompatibilityInfo = new CompatibilityInfoHolder(); //4.0~4.3
			// DisplayAdjustments mDisplayAdjustments = new DisplayAdjustments(); //4.4 +
			try {
				Field field = loadedApk.getClass().getDeclaredField("mCompatibilityInfo");
				field.setAccessible(true);
				Object cmpt = field.get(loadedApk);
				
				if(cmpt.getClass().getSimpleName().equals("CompatibilityInfo")) { //2.3
					return cmpt;
				}
				
				//4.0+
				Method method = cmpt.getClass().getDeclaredMethod("get");
				method.setAccessible(true);
				return method.invoke(cmpt);
			} catch (Throwable e) {
			}

			try { //4.4+
				Method method = loadedApk.getClass().getDeclaredMethod("getCompatibilityInfo");
				method.setAccessible(true);
				return method.invoke(loadedApk);
			} catch (Throwable e) {
			}
			
			return null;
		}
		
		public static void setClassLoader(Object object, Object classLoader) {
			setField(object, decode(fld_mClassLoader), classLoader);
		}
		
		public static void setApplication(Object object, Object app) {
			setField(object, decode(fld_mApplication), app);
		}
		
		public static Object getResources(Object object) {
			try {
				Method method = object.getClass().getDeclaredMethod("getResources",
						YActivityThread.getActivityThreadClass());
				method.setAccessible(true);
				return method.invoke(object, YActivityThread.getActivityThreadObject());
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		/*Application makeApplication(boolean forceDefaultAppClass,
            	Instrumentation instrumentation)*/
		public static Object makeApplication(Object object) {
			try {
				QLog.i(TAG, "try make application " + object);
				
				Method method = object.getClass().getMethod(decode(mtd_makeApplication),
						boolean.class, Instrumentation.class);
				method.setAccessible(true);
				return method.invoke(object, 
						false, YActivityThread.getInstrumentation());
			} catch (Throwable e) {
				if(B_LOG_EX) e.getCause().printStackTrace();
			}
			
			return null;
		}
	}
	
	public static final class YTelephonyManager {
		/**
		 * @notuse
		 * 
		 * @param src
		 * @return
		 */
		public static Object getSubscriberInfo(Object src) {
			try {
				Method method = src.getClass().getDeclaredMethod("getSubscriberInfo"
						);
				method.setAccessible(true);
				return method.invoke(src);
			} catch (Throwable e) {
			}
			
			return null;
		}
	}
	
	public static final class YActivityThread {
		public static final String cls = "android.app.ActivityThread";
		
		public static final String fld_mResCompatibilityInfo = "mResCompatibilityInfo";
		public static final String fld_mInstrumentation = "mInstrumentation";
		public static final String fld_mH = "mH";
		public static final String fld_mCallback = "mCallback";
		public static final String fld_sPackageManager = "sPackageManager";
		public static final String fld_mInitialApplication = "mInitialApplication";
		
		
		public static final String mtd_getPackageInfoNoCheck = "getPackageInfoNoCheck";
		public static final String mtd_currentActivityThread = "currentActivityThread";
		public static final String mtd_getPackageManager = "getPackageManager";
		public static final String mtd_installContentProviders = "installContentProviders";
		
		private static Object sATObject;
		private static Object sH;
		private static Class<?> sATClass;
		
		
		public static Class<?> getActivityThreadClass() {
			if(sATClass != null)
				return sATClass;
			
			try {
				sATClass = Class.forName(decode(cls));
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return sATClass;
		}
		
		public static Object getActivityThreadObject() {
			if(sATObject != null)
				return sATObject;
			
			try {
				Method method = getActivityThreadClass().getDeclaredMethod(decode(mtd_currentActivityThread)
						);
				method.setAccessible(true);
				sATObject = method.invoke(null);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return sATObject;
		}
		
		public static Object getPackageInfoNoCheck(Object info, Resources container) {
			Class<?> clazz = getActivityThreadClass();
			Object object = getActivityThreadObject();

			try { //4.0 +
				Class<?> clazz2 = Class.forName(decode(YCompatibilityInfo.cls));
				
				Method method = clazz.getDeclaredMethod(decode(mtd_getPackageInfoNoCheck), 
						ApplicationInfo.class, clazz2);
				method.setAccessible(true);
				return method.invoke(object, info, container.getCompatibilityInfo());
			} catch (Throwable e) {
				//do not show
//				if(B_LOG_EX) e.printStackTrace();
			}
			
			try {//2.2 +
				Method method = clazz.getDeclaredMethod(decode(mtd_getPackageInfoNoCheck), 
						ApplicationInfo.class);
				method.setAccessible(true);
				
				return method.invoke(object, info);
			} catch (Throwable e) {
			}
			
			return null;
		}
		
		public static Object getInstrumentation() {
			return getField(getActivityThreadObject(), decode(fld_mInstrumentation));
		}
		
		public static void setInstrumentation(Object dst) {
			setField(getActivityThreadObject(), decode(fld_mInstrumentation), dst);
		}
		
		public static Handler getH() {
			if(sH != null)
				return (Handler) sH;
			
			sH = getField(getActivityThreadObject(), decode(fld_mH));

			return (Handler) sH;
		}
		
		public static void setHCallback(Callback dst) {
			try {
				Object h = getH();

				//warn h.getClass().getSuperclass() just get the Handler class
				Field field = h.getClass().getSuperclass().getDeclaredField(decode(fld_mCallback));
				field.setAccessible(true);
				field.set(h, dst);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
		
		public static void enableJit() {
			getH().sendEmptyMessageDelayed(YH.ENABLE_JIT, 10*1000);
		}
		
		public static void post(Runnable r) {
			 getH().post(r);
		}
		
		public static Object getPackageManager() {
			try {
				Method method = getActivityThreadClass().getDeclaredMethod(decode(mtd_getPackageManager)
						);
				method.setAccessible(true);
				return method.invoke(null);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		public static void setInitApplication(Object app) {
			setField(getActivityThreadObject(), decode(fld_mInitialApplication), app);
		}
		
		public static void proxyInitApplication() {
			try {
				if(!ApkRunner.APPLICATION_DECLARED) {
					/*
					 * sPackgeManger 改变之后 有一个地方有缓存，mInitialApplication 的 ContextImpl 
					 * mPackgeManager 对象缓存，ActivityThread 多处用到此变量，所以这里将此变量置空
					 */
					//set ActivityThread.mInitialApplication
					Application app = (Application) getField(getActivityThreadObject(), decode(fld_mInitialApplication));
					if(app != null)
						YContextImpl.setPackageManager(app.getBaseContext(), null);
				}
//				QLog.i(TAG, "pm replaced? " + (getPackageManager() == dst));
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
		
		public static void setPackageManager(Object dst) {
			setStaticField(getActivityThreadClass(), decode(fld_sPackageManager), dst);
		}
		
		public static void installContentProviders(Object context, Object providers) {
			try {
				Method method = getActivityThreadClass().getDeclaredMethod(decode(mtd_installContentProviders),
						Context.class, List.class);
				method.setAccessible(true);
				method.invoke(getActivityThreadObject(),
						context, providers);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
		
		public static final class YH {
			public static final String cls = "android.app.ActivityThread$H";
			
			public static final String mtd_codeToString = "codeToString";

			public static final String fld_CREATE_SERVICE = "CREATE_SERVICE";
			public static final String fld_RECEIVER = "RECEIVER";
			public static final String fld_LAUNCH_ACTIVITY = "LAUNCH_ACTIVITY";
			
			
			public static int LAUNCH_ACTIVITY = 100;
			public static final int PAUSE_ACTIVITY = 101;
			public static final int PAUSE_ACTIVITY_FINISHING = 102;
			public static final int STOP_ACTIVITY_SHOW = 103;
			public static final int STOP_ACTIVITY_HIDE = 104;
			public static final int SHOW_WINDOW = 105;
			public static final int HIDE_WINDOW = 106;
			public static final int RESUME_ACTIVITY = 107;
			public static final int SEND_RESULT = 108;
			public static final int DESTROY_ACTIVITY = 109;
			public static final int BIND_APPLICATION = 110;
			public static final int EXIT_APPLICATION = 111;
			public static final int NEW_INTENT = 112;
			public static int RECEIVER = 113;
			public static int CREATE_SERVICE = 114;
			public static final int SERVICE_ARGS = 115;
			public static final int STOP_SERVICE = 116;
			public static final int REQUEST_THUMBNAIL = 117;
			public static final int CONFIGURATION_CHANGED = 118;
			public static final int CLEAN_UP_CONTEXT = 119;
			public static final int GC_WHEN_IDLE = 120;
			public static final int BIND_SERVICE = 121;
			public static final int UNBIND_SERVICE = 122;
			public static final int DUMP_SERVICE = 123;
			public static final int LOW_MEMORY = 124;
			public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
			public static final int RELAUNCH_ACTIVITY = 126;
			public static final int PROFILER_CONTROL = 127;
			public static final int CREATE_BACKUP_AGENT = 128;
			public static final int DESTROY_BACKUP_AGENT = 129;
			public static final int SUICIDE = 130;
			public static final int REMOVE_PROVIDER = 131;
			public static final int ENABLE_JIT = 132;
			public static final int DISPATCH_PACKAGE_BROADCAST = 133;
			public static final int SCHEDULE_CRASH = 134;
			public static final int DUMP_HEAP = 135;
			public static final int DUMP_ACTIVITY = 136;
			public static final int SLEEPING = 137;
			public static final int SET_CORE_SETTINGS = 138;
			public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
			public static final int TRIM_MEMORY = 140;
			public static final int DUMP_PROVIDER = 141;
			public static final int UNSTABLE_PROVIDER_DIED = 142;
			
			
			static {
				try {
					Class<?> clazz = Class.forName(decode(cls));
					
					CREATE_SERVICE = getStaticIntField(clazz, decode(fld_CREATE_SERVICE), CREATE_SERVICE);
					RECEIVER = getStaticIntField(clazz, decode(fld_RECEIVER), RECEIVER);
					LAUNCH_ACTIVITY = getStaticIntField(clazz, decode(fld_LAUNCH_ACTIVITY), LAUNCH_ACTIVITY);
				} catch (Throwable e) {
					if(B_LOG_EX) e.printStackTrace();
				}
			}
			
			public static String codeToString(int code) {
				try {
					Object h = getH();
					Method method = h.getClass().getDeclaredMethod(decode(mtd_codeToString),
							int.class);
					method.setAccessible(true);
					return (String) method.invoke(h, code);
				} catch (Throwable e) {
					//just test func ,do not show!
				}
				
				return Integer.toString(code);
			}
		}
	}
	
	public static final class YActivityManager {
		public static final String cls = "android.app.ActivityManagerNative";
		
		public static final String mtd_getDefault = "getDefault";

		public static final String fld_gDefault = "gDefault";
		public static final String fld_mInstance = "mInstance";
		
		
		public static Object getActivityManagerObject() {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Method method = clazz.getDeclaredMethod(decode(mtd_getDefault)
						);
				method.setAccessible(true);
				return method.invoke(null);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}

			return null;
		}
		
		//boolean finishActivity(IBinder token, int code, Intent data)
		public static void finishActivity(IBinder token, int code, Intent data) {
			try {
                ActivityManagerNative.getDefault()
                    .finishActivity(token, code, data);
            } catch (RemoteException e) {
            	if(B_LOG_EX) e.printStackTrace();
            }
		}
		
		public static void setActivityManager(Object dst) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				Object object = getActivityManagerObject();

				
				//static final Singleton<IActivityManager> gDefault
				Field field = clazz.getDeclaredField(decode(fld_gDefault)); 
				field.setAccessible(true);
				
				Object gDefault = field.get(null);
				
				if (gDefault.getClass() == object.getClass()) { // 2.2.2~2.3.7
					field.set(null, dst);
				} else { //4.0 +
					//? extends Singleton
					Field field2 = gDefault.getClass().getSuperclass().getDeclaredField(decode(fld_mInstance));
					field2.setAccessible(true);
					field2.set(gDefault, dst);
				}
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YNotificationManager {
		public static final String cls = "android.app.NotificationManager";
		
		public static final String mtd_getService = "getService";

		public static final String fld_sService = "sService";
		
		
		public static Object getNotificationManager() {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Method method = clazz.getDeclaredMethod(decode(mtd_getService)
					);
				method.setAccessible(true);
				return method.invoke(null);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		public static void setNotificationManager(Object dst) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Field field = clazz.getDeclaredField(decode(fld_sService));
				field.setAccessible(true);
				field.set(null, dst);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YServiceManager {
		public static final String cls = "android.os.ServiceManager";
		public static final String mtd_getService = "getService";
		public static final String fld_sCache = "sCache";
			
		public static void init() {
		}
		
		public static Object getService(String name) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				Method method = clazz.getDeclaredMethod(decode(mtd_getService), 
						String.class);
				method.setAccessible(true);
				return method.invoke(null, name);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		/**
		 * 往 ServiceManager 里添加缓存
		 */
		@SuppressWarnings("unchecked")
		public static void addServiceToCache(String name, Object service) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				
				//HashMap<String, IBinder> sCache
				Field field = clazz.getDeclaredField(decode(fld_sCache));
				field.setAccessible(true);
				
				@SuppressWarnings("rawtypes")
				Map sCache = (Map)field.get(null);
				sCache.put(name, service);
				
				QLog.d(TAG, "add service " + name + "->" + service);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YSurface {
//		Surface
		public static Bitmap screenshot(int width, int height) {
//			Surface.sc
			try {
				Method method = Surface.class.getDeclaredMethod("screenshot",
						int.class, int.class);
				method.setAccessible(true);
				return (Bitmap) method.invoke(null, width, height);
			} catch (Throwable e) {
			}
			
			return null;
		}
	}
	
	public static final class YPackgeManager {
		public static final String mtd_generatePackageInfo = "generatePackageInfo";
		
		/**
		 * 修复 4.0 已下获取签名失败补丁
		 * @param archiveFilePath
		 * @param flags
		 * @return
		 */
		public static PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
			PackageParser packageParser = new PackageParser(archiveFilePath);
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			final File sourceFile = new File(archiveFilePath);
			PackageParser.Package pkg = packageParser.parsePackage(sourceFile, archiveFilePath, metrics, 0);
			if (pkg == null) {
				return null;
			}
			
			if ((flags & PackageManager.GET_SIGNATURES) != 0) {
				packageParser.collectCertificates(pkg, 0);
			}
			
			try {
				//PackageParser.generatePackageInfo(pkg, null, flags, 0, 0, null, state);
				Method method = PackageParser.class.getDeclaredMethod(decode(mtd_generatePackageInfo), 
						pkg.getClass(), int[].class, int.class, long.class, long.class);
				method.setAccessible(true);
				return (PackageInfo) method.invoke(null, pkg, null, flags, 0, 0);
			} catch (Throwable e) {
			}
			
			return null;
		}
	}
	
	public static final class YLocationManager {
		public static final String fld_mService = "mService";
		
		
		public static Object getService(Object object) {
			return getField(object, decode(fld_mService));
		}
		
		public static void setService(Object object, Object service) {
			setField(object, decode(fld_mService), service);
		}
	}
	
	public static final class YWifiManager {
//		public static final String cls = "android.os.ServiceManager";
//		public static final String mtd_getService = "getService";
		public static final String fld_mService = "mService";
		
		
		public static Object getService(Object object) {
			return getField(object, decode(fld_mService));
		}

		public static void setService(Object object, Object service) {
			setField(object, decode(fld_mService), service);
		}
	}
	
	public static abstract class YCommonManager {
		public static String cls ;
		public static final String mtd_getService = "getService";
		public static final String fld_sService = "sService";
		
		
		public static Object getService() {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				Method method = clazz.getDeclaredMethod(decode(mtd_getService)
						);
				method.setAccessible(true);
				return method.invoke(null);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
			
			return null;
		}
		
		public static void setService(Object newValue) {
			try {
				Class<?> clazz = Class.forName(decode(cls));
				setStaticField(clazz, decode(fld_sService), newValue);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
	
	public static final class YClipboardManager extends YCommonManager {
		static {
			cls = "android.content.ClipboardManager";
		}
		
		public static Object getService() {
			return YCommonManager.getService();
		}
	}

	public static final class YAudioManager extends YCommonManager {
		static {
			cls = " android.media.AudioManager";
		}
	}
	
	public static final class YIServiceConnection {
//		public static final String cls = "android.app.IServiceConnection";
		
		public static final String mtd_connected = "connected";
		
		private static Method method;
		
		
		//void connected(ComponentName name, IBinder service) throws RemoteException
		public static void connected(Object conn, Object cmp, Object binder) {
			try {
				if(method == null) {
					Method[] methods = conn.getClass().getDeclaredMethods();
					for(Method m: methods) {
						m.setAccessible(true);
						if(m.getName().equals(decode(mtd_connected))) {
							method = m;
							break;
						}
					}
				}
				method.invoke(conn, cmp, binder);
			} catch (Throwable e) {
				if(B_LOG_EX) e.printStackTrace();
			}
		}
	}
}
