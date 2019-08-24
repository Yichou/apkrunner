package com.apkrunner.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.utils.Logger;
import com.apkrunner.utils.Singleton;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;



/**
 * ActivityManager 代理
 * 
 * @author Yichou 2013-9-26
 *
 */
public final class QActivityManager extends BaseProxy {
	static final String TAG = "AM";
	static final Logger log = Logger.create(QLog.debug_am, TAG);
	
	public static final String KEY_COMPNET_NAME = "e0dfde";
	public static final String KEY_INTENT = "e0dfdf";
	public static final String FLAG_PROXY = "e1afa0";
	
	private static final String mtd_handleApplicationCrash = "handleApplicationCrash";
	private static final String mtd_startActivity = "startActivity";
	private static final String mtd_startService = "startService";
	private static final String mtd_stopService = "stopService";
	private static final String mtd_setServiceForeground = "setServiceForeground";
	private static final String mtd_bindService = "bindService";
	private static final String mtd_unbindService = "unbindService";
	private static final String mtd_broadcastIntent = "broadcastIntent";
	private static final String mtd_getContentProvider = "getContentProvider";
	private static final String mtd_registerReceiver = "registerReceiver";
	private static final String mtd_stopServiceToken = "stopServiceToken";
	private static final String mtd_getIntentSender = "getIntentSender";
	private static final String mtd_getRunningAppProcesses = "getRunningAppProcesses";
//	private static final String mtd_ = "";
	
	private final HashMap<String, RunServiceInfo> mServices = new HashMap<String, RunServiceInfo>();
	private final HashMap<Object, RunServiceInfo> mConns = new HashMap<Object, RunServiceInfo>();
	private static Context gContext;
	private QStartActivityHandler mStartActivityHandler;
	
	
	public static interface QStartActivityHandler {
		public boolean onStartActivity(Intent intent);
	}
	
	
	protected QActivityManager(Object src) {
		super(src);
	}
	
	public static Context getGlobalContext() {
		if(gContext == null)
			gContext = ApkRunner.getShellContext();
		
		return gContext;
	}
	
	public void setStartActivityHandler(QStartActivityHandler h) {
		this.mStartActivityHandler = h;
	}
	
	private Intent makeProxy(Intent oIntent, String proxyClass) {
		Intent intent = new Intent();
		intent.setClassName(ApkRunner.getShellPkg(), proxyClass);
		intent.putExtra(FLAG_PROXY, true);
		intent.putExtra(KEY_INTENT, oIntent);
		
		/**
		 * 加标志过去会导致一些莫名的问题，我们就默认给他启动一个好了 2014-4-3
		 */
//		intent.addFlags(oIntent.getFlags());
		
		return intent;
	}
	
	private Intent makeProxyActivity(Intent oIntent, ActivityInfo activity) {
		String type = null;
		
		//强制横屏的 activity
		if(activity.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			type = "L";
		} 
		//强制竖屏的 activity
		else if(activity.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			type = "P";
		}
		
		return makeProxy(oIntent, ApkRunner.getProxyActivityName(type));
	}

	private int getIntentIndex(Object[] args) {
		for(int i=0; i<args.length; ++i) {
			if(args[i] != null && Intent.class == args[i].getClass())
				return i;
		}
		
		return -1;
	}
	
	/*int startActivity(IApplicationThread caller, Intent intent,
	        String resolvedType, Uri[] grantedUriPermissions, int grantedMode,
	        IBinder resultTo, String resultWho,
	        int requestCode, boolean onlyIfNeeded,
	        boolean debug, String profileFile, ParcelFileDescriptor profileFd,
	        boolean autoStopProfiler) */
	private boolean handleStartActivity(ApkInfo runner, Object[] args) {
		int i = getIntentIndex(args);
		if(i == -1) return false;
		
		Intent oIntent = (Intent) args[i];
		
		log.i("old intent: " + oIntent);
		
		if(mStartActivityHandler != null && mStartActivityHandler.onStartActivity(oIntent)) {
			return true;
		}

		String action = oIntent.getAction();
		if (action != null) {
			if (Intent.ACTION_VIEW.equals(action)) {
				Uri data = oIntent.getData();
				if(data != null && data.getScheme().equals("http")) {
					return false;
				}
			}
		}

		ActivityInfo info = null;
		ComponentName cmp = oIntent.getComponent();
		
		if (cmp != null) { // 显式启动
			if(cmp.getClassName().startsWith(ApkRunner.getProxyActivityName())) {
				log.w("context lost");
				//直接启动了 ProxyActivity
				((Intent) args[i]).setClassName(runner.getShellPackgeName(), ApkRunner.ENTRY_ACTIVITY_NAME);
				return false;
			}

			info = runner.getActivity(cmp.getClassName());
		} else {
			info = runner.resloveActivity(oIntent);
		}

		if (info != null) {
			args[i] = makeProxyActivity(oIntent, info);
		}
		
		log.i("new intent: " + args[i]);
		
		return false;
	}
	
	private ServiceInfo resloveServiceInfo(ApkInfo runner, Intent intent) {
		ServiceInfo info = null;
		ComponentName cmp = intent.getComponent();

		if (cmp != null) { // 显式启动
			info = runner.getService(cmp.getClassName());
		} else {
			info = runner.resloveService(intent);
		}
		
		return info;
	}
	
	private static final class ServiceBinder extends Binder {
		
	}
	
	private static final class RunServiceInfo {
    	String mClassName;
    	String mProcess;
    	Service mService;
    	IBinder mBinder;
    	ServiceBinder mToken; //假的 Token
    	int mStartCount = 0;
    	int mStartId = 1; //从 1 开始
    	HashSet<Object> connections = new HashSet<Object>(); //所有连接
    }
	
	private RunServiceInfo getService(ApkInfo apkInfo, String className, boolean autoCreate) {
		synchronized (mServices) {
			RunServiceInfo runInfo = mServices.get(className);
			
			if(runInfo == null && autoCreate) {
				runInfo = new RunServiceInfo();
				runInfo.mClassName = className;

				try {
					Class<?> clazz = apkInfo.loadClass(runInfo.mClassName);
					runInfo.mService = (Service) clazz.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(
							"Unable to instantiate service " + runInfo.mClassName
							+ ": " + e.toString(), e);
				}
				
				try {
					Context c = getGlobalContext().createPackageContext(apkInfo.getPackgeName(), Context.CONTEXT_INCLUDE_CODE);
					runInfo.mToken = new ServiceBinder();
					
					Frameworks.YContextImpl.setOuterContext(c, runInfo.mService);
					
					runInfo.mService.attach(new QContext(c), 
							(ActivityThread)QActivityThread.getDefault().getReal(),
							runInfo.mClassName, 
							runInfo.mToken,
							apkInfo.getApplication(), 
							getReal());
					
					log.d("[Service]: " + "call onCreate on >>>" + runInfo.mClassName);
					
					runInfo.mService.onCreate();
					
					mServices.put(className, runInfo);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
//					throw new RuntimeException("attach service fail! " + e.getMessage());
				}
			}
			
			return runInfo;
		}
	}
	
	private RunServiceInfo getService(ApkInfo apkInfo, String className) {
		return getService(apkInfo, className, true);
	}
	
	private void startServiceRun(ApkInfo apkInfo, Intent intent) {
//		log.e("startServiceRun");
		
		ComponentName cmp = intent.getComponent();
		RunServiceInfo runInfo = getService(apkInfo, cmp.getClassName());
		
		if (runInfo != null) {
			// 设置 反序列化类加载器
			intent.setExtrasClassLoader(runInfo.mService.getClassLoader());

			log.d(TAG, "[Service]: " + "call onStartCommand on >>>" 
					+ cmp.getClassName()
					+ ", startId = " + runInfo.mStartId);
			int ret = runInfo.mService.onStartCommand(intent, 0, runInfo.mStartId++);
			
			log.d(TAG, "  start ret=" + ret + " <<<");

			runInfo.mStartCount++;
		}
	}
	
	private void doStartService(final ApkInfo apkInfo, final Intent intent) {
		if(intent.getComponent().getClassName().equals("com.qihoo.express.mini.service.DaemonCoreService"))
			return;
		
//		startServiceRun(apkInfo, intent);
		
//		log.e("---------0");
		QActivityThread.getDefault().runOnMainThread(new Runnable() {

			@Override
			public void run() {
				startServiceRun(apkInfo, intent);
			}
		});
//		log.e("---------1");
	}
	
	private void destroyServiceInner(RunServiceInfo runInfo) {
		log.w("[Service]: " + runInfo.mClassName + " is no longer used, will be removed!");
		
//		log.d(TAG, "call onDestroy on Service >>> " + runInfo.mService);
//		runInfo.mService.onDestroy();
		
		synchronized (mServices) {
			mServices.remove(runInfo.mClassName);
//			log.d(TAG, "" + runInfo.mClassName + " Service destroyed!");
		}
	}
	
	public void stopWholeServiceRun(ApkInfo apkInfo, ComponentName cmp) {
		RunServiceInfo runInfo = getService(apkInfo, cmp.getClassName(), false);
		
		if(runInfo != null) {
			log.d(TAG, "stop service " + cmp);
			runInfo.mStartCount = 0;
			
			if(runInfo.connections.size() == 0) //没有任何 连接 则销毁
				destroyServiceInner(runInfo);
		}
	}
	
	/**
	 * 彻底停止一个 Service，不管其有多少个 start 
	 * 如果没有 connect 则销毁 service
	 * 
	 * <p>Context.stopServie
	 * @param cmp
	 */
	private void doStopWholeService(final ApkInfo apkInfo, final ComponentName cmp) {
		QActivityThread.getDefault().runOnMainThread(new Runnable() {
			
			@Override
			public void run() {
				stopWholeServiceRun(apkInfo, cmp);
			}
		});
	}
	
	public void stopServiceRun(ApkInfo apkInfo, ComponentName cmp, int startId) {
		RunServiceInfo runInfo = getService(apkInfo, cmp.getClassName(), false);
		
		if(runInfo != null) {
			log.d(TAG, "stop service " + cmp);
			runInfo.mStartCount--;
			
			if(runInfo.mStartCount <= 0 && runInfo.connections.size() == 0)
				destroyServiceInner(runInfo);
		}
	}
	
	/**
	 * 停止一次启动
	 * 
	 * <p>Service.stopSelf()
	 * 
	 * @param cmp
	 * @param startId
	 */
	private void doStopService(final ApkInfo apkInfo, final ComponentName cmp, final int startId) {
		QActivityThread.getDefault().runOnMainThread(new Runnable() {
			
			@Override
			public void run() {
				stopServiceRun(apkInfo, cmp, startId);
			}
		});
	}
	
	/**
	 * <p>Context.bindService
	 * 
	 * @param cmp
	 * @param conn
	 */ 
	public void doBindService(final ApkInfo apkInfo, final Intent intent, final Object conn) {
		if(intent.getComponent().getClassName()
				.equals("com.qihoo.express.mini.service.DaemonCoreService"))
			return;
		
//		log.e("do bind service!");
//		bindServiceRun(apkInfo, intent, conn);
		
		QActivityThread.getDefault().runOnMainThread(new Runnable() {
			
			@Override
			public void run() {
//				log.e("do bind service run!");
				
				bindServiceRun(apkInfo, intent, conn);
			}
		});

//		log.e("do bind service 2!");
	}
	
	public void bindServiceRun(ApkInfo apkInfo, Intent intent, Object conn) {
		ComponentName cmp = intent.getComponent();
		RunServiceInfo runInfo = getService(apkInfo, cmp.getClassName());

		if(runInfo != null) {
			log.i(TAG, "bind service " + cmp + ", conn=" + conn);
			
			//判断 conn 是不是已经跟某个 service 结缘
			RunServiceInfo old = mConns.get(conn);
			if(old == runInfo) { //已经连接
				return;
			}
			
			if(!runInfo.connections.contains(conn)) {
				if(runInfo.mBinder == null) {
					log.d(TAG, "[Service]: " +  "call onBind on >>>" + cmp.getClassName());
					runInfo.mBinder = runInfo.mService.onBind(intent);
					log.d(TAG, "  binder=" + runInfo.mBinder + "<<<");
				}
				
				Frameworks.YIServiceConnection.connected(conn, cmp, runInfo.mBinder);
				runInfo.connections.add(conn);
				mConns.put(conn, runInfo); //保存连接关系
			}
		}
	}
	
	public boolean doUnbindService(ApkInfo apkInfo, Object conn) {
		return unbindServiceRun(conn);
	}
	
	public boolean unbindServiceRun(Object conn) {
		RunServiceInfo runInfo = mConns.get(conn);
		
		if(runInfo == null)
			return false;
		
		boolean ret = false;
		
		//从连接池里移除
		runInfo.connections.remove(conn);
		mConns.remove(conn);
		
		if(runInfo.connections.size() == 0) { //unbind service
			log.d(TAG, "[Service]: " +  "call onUnbind on >>>" + runInfo.mClassName);
			ret = runInfo.mService.onUnbind(new Intent());
		}
		
		if(runInfo.mStartCount <= 0 && runInfo.connections.size() == 0)
			destroyServiceInner(runInfo);
		
		return ret;
	}
	
	/*ComponentName startService(IApplicationThread caller, Intent service,
		String resolvedType)*/
	private boolean handleStartService(ApkInfo runner, Object[] args) {
		int i = getIntentIndex(args);
		if(i == -1) return false;
		
		Intent intent = (Intent) args[i];
		log.d(intent.toString());
		ServiceInfo info = resloveServiceInfo(runner, intent);
		
		if (info != null) {
			intent.putExtra("serviceInfo", info);
			doStartService(runner, intent);
			return true;
		}
		
		return false;
	}
	
	/**
	 *  <p>boolean stopServiceToken(ComponentName className, IBinder token,
	 *		int startId)
	 *
	 *	<p>called by Service.stopSelf(startId)
	 *
	 *	<p>this stops the service no matter how many times it was started. 
	 */
	private boolean handleStopServiceToken(ApkInfo runner, Object[] args) {
		log.d("stopSelf(" + args[0] + ", startId=" + args[2]);
		
		ComponentName cmp = (ComponentName) args[0];
		ServiceInfo info = runner.getService(cmp.getClassName());
		
		if(info != null) {
			doStopService(runner, (ComponentName) args[0], (Integer)args[2]);
			return true;
		}
		
		return false;
	}
	
	/*int stopService(IApplicationThread caller, Intent service,
		String resolvedType)
		called by Context.stopService(Intent)*/
	private boolean handleStopService(ApkInfo runner, Object[] args) {
		int i = getIntentIndex(args);
		if(i == -1) return false;
		
		Intent intent = (Intent) args[i];
		log.d(intent.toString());
		ServiceInfo info = resloveServiceInfo(runner, intent);
		
		if (info != null) {
			doStopWholeService(runner, intent.getComponent());
			return true;
		}
		
		return false;
	}
	
	/*int bindService(IApplicationThread caller, IBinder token,
        Intent service, String resolvedType, IServiceConnection connection,
        int flags)*/
	private boolean handleBindService(ApkInfo runner, Object[] args) {
		log.d(args[2] + ", type=" + args[3] + ", conn=" + args[4]);

		int i = getIntentIndex(args);
		if(i == -1) return false;
		
		Intent intent = (Intent) args[i];
		ServiceInfo info = resloveServiceInfo(runner, intent);
		
		if (info != null) {
			intent.putExtra("serviceInfo", info);
			doBindService(runner, intent, args[4]);
			return true;
		}
		
		//屏蔽一些包
		if(intent.getAction() != null && intent.getAction().startsWith("com.google.android.gms.games."))
			return true;
		if(intent.getPackage() != null && "com.google.android.gms".equals(intent.getPackage()))
			return true;
		
		return false;
	}
	
	/*public boolean unbindService(IServiceConnection connection)*/
	private boolean handleUnbindService(ApkInfo runner, Object[] args) {
		return doUnbindService(runner, args[0]);
	}

	/*int broadcastIntent(IApplicationThread caller,
        Intent intent, String resolvedType,  IIntentReceiver resultTo,
        int resultCode, String resultData, Bundle map,
        String requiredPermission, boolean serialized,
        boolean sticky)*/
	private boolean handleBroadcastIntent(ApkInfo runner, Object[] args) {
		int i = getIntentIndex(args);
		if(i == -1) return false;
		Intent oIntent = (Intent) args[i];

		log.d(oIntent.toString());
		
		//拦截某些
		String action = oIntent.getAction();
		if(action != null) {
			if(action.equals("com.android.launcher.action.INSTALL_SHORTCUT")) { //创建快捷方式
				ApkRunner.handleShortcut(oIntent);
				return true;
			}
		}

		ActivityInfo info = null;
		ComponentName cmp = oIntent.getComponent();

		if (cmp != null) { // 显式启动
			info = runner.getReceiver(cmp.getClassName());
		} else {
			info = runner.resloveReceiver(oIntent);
		}

		if (info != null) { //直接给他发过去
			Intent intent = new Intent(QRunnerService.START_FOR_BROADCAST);
			intent.setClassName(ApkRunner.getShellContext(), ApkRunner.getProxyServiceName());
			intent.putExtra(QRunnerService.KEY_REAL_INTENT, oIntent);
			intent.putExtra(QRunnerService.KEY_PKG, runner.getPackgeName());
			
			ApkRunner.getShellContext().startService(intent);
			return true;
		}
		
		return false;
	}
	
	/* ContentProviderHolder getContentProvider(IApplicationThread caller,
		String name, int userId, boolean stable)) */
	private Object handleGetContentProvider(ApkInfo apkInfo, Object[] args) {
		log.i("  author name=" + args[1] /*+ ", userId=" + args[2] + ", stable" + args[3]*/);
		
		ProviderInfo info = apkInfo.getContentProvider((String) args[1]);
		if(info != null) {
			android.app.IActivityManager.ContentProviderHolder holder = new android.app.IActivityManager.ContentProviderHolder(info);
			holder.provider = null;
			
			return holder;
		}
		
		return null;
	}
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		log.i("invoke " + name);
		
		ApkInfo runner = ApkRunner.currentRunning();
		
		if(match(name, mtd_handleApplicationCrash)) {
			/* public void handleApplicationCrash(IBinder app,
            ApplicationErrorReport.CrashInfo crashInfo) throws RemoteException*/
			CrashHandler.crashToFile(ApkRunner.getShellContext(), (CrashInfo) args[1]);
			if(runner != null) {
				runner.getManagerService().notifyCrash();
			}
		}
		
		if(runner == null)
			return super.onInvoke(object, method, name, args);
		
		if(match(name, mtd_startActivity)) {
			if(handleStartActivity(runner, args)) {//START_SUCCESS = 0;
				return 0;
			}
		} 
		else if (match(name, mtd_startService)) {
			/*ComponentName startService(IApplicationThread caller, Intent service,
				String resolvedType)*/
			if(handleStartService(runner, args)){
				return null;
			}
		}
		else if (match(name, mtd_stopService)) {
			//int stopService(IApplicationThread caller, Intent service,
			//	String resolvedType)
			//	called by Context.stopService(Intent)*/
			if(handleStopService(runner, args)) {
				return 1;
			}
		}
		else if (match(name, mtd_bindService)) {
			/*int bindService(IApplicationThread caller, IBinder token,
	        	Intent service, String resolvedType, IServiceConnection connection,
	        	int flags)*/
			if(handleBindService(runner, args)) {
				return 1;
			}
		}
		else if (match(name, mtd_unbindService)) {
			/*public boolean unbindService(IServiceConnection connection)*/
			if(handleUnbindService(runner, args)) {
				return true;
			}
		}
		else if (match(name, mtd_stopServiceToken)) {
//			boolean stopServiceToken(ComponentName className, IBinder token, int startId)
			if(handleStopServiceToken(runner, args)) {
				return true;
			}
		}
		else if (match(name, mtd_setServiceForeground)) {
			/*void setServiceForeground(ComponentName className, IBinder token,
            			int id, Notification notification, boolean removeNotification)*/
		}
		else if (match(name, mtd_broadcastIntent)) { //int broadcastIntent
			if(handleBroadcastIntent(runner, args))
				return 1;
		}
		else if (match(name, mtd_getContentProvider)) {
			/*ContentProviderHolder getContentProvider(IApplicationThread caller,
            		String name, int userId, boolean stable))*/
			Object ret = handleGetContentProvider(runner, args);
			if(ret != null)
				return ret;
			
			try {
				return super.onInvoke(object, method, name, args);
			} catch (Exception e) {
			}
		}
		else if (match(name, mtd_registerReceiver)) {
			/*public Intent registerReceiver(IApplicationThread caller, String callerPackage,
           		IIntentReceiver receiver, IntentFilter filter,
            	String requiredPermission) throws RemoteException;*/
//			log.i("recv = "+args[2]);
			if(runner.getPackgeName().equals(args[1])) {
				args[1] = runner.getShellPackgeName();
			}
			/*void unregisterReceiver(IIntentReceiver receiver)*/
		}
		else if (match(name, mtd_getIntentSender)) {
			/*IIntentSender getIntentSender(int type,
	            String packageName, IBinder token, String resultWho,
	            int requestCode, Intent[] intents, String[] resolvedTypes,
	            int flags, Bundle options, int userId)*/
			
			//PendingIntent 用
			if(runner.getPackgeName().equals(args[1])) {
				args[1] = runner.getShellPackgeName();
			}
		}
		else if (match(name, mtd_getRunningAppProcesses)) {
			@SuppressWarnings("unchecked")
			List<ActivityManager.RunningAppProcessInfo> processes 
				= (List<ActivityManager.RunningAppProcessInfo>) method.invoke(object, args);
			
			for (RunningAppProcessInfo proce : processes) {
				/**
				 * 腾讯应用宝启动时会校验进程名与包名是否一致
				 */
				if (proce.pid == Process.myPid()) {
					ApkRunner.setCurProcessName(proce.processName);
					proce.processName = runner.getPackgeName(); //进程名，跟包名一致
					break;
				}
			}
			
			return processes;
		}
		
		try {
			return super.onInvoke(object, method, name, args);
		} catch (Exception e) {
			throw e.getCause();
		}
	}
	
	private static final Singleton<QActivityManager> gDefault = new Singleton<QActivityManager>() {

		@Override
		protected QActivityManager create(Object object) {
			return new QActivityManager(object);
		}
	};
	
	public static QActivityManager getDefault() {
		return gDefault.get(Frameworks.YActivityManager.getActivityManagerObject());
	}
	
	public static void proxy() {
		Object old = Frameworks.YActivityManager.getActivityManagerObject();
		
		if(old != null && !(old instanceof QActivityManager)) {
			Frameworks.YActivityManager.setActivityManager(
					newProxyInstance(old, gDefault.get(old)));
		}
	}
}
