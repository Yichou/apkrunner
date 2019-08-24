package com.apkrunner;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import com.apkrunner.core.Core;
import com.apkrunner.core.QLog;
import com.apkrunner.core.QPackgeManager;
import com.apkrunner.core.QRunnerService;
import com.apkrunner.sdk.SdkManager;
import com.apkrunner.utils.Logger;
import com.apkrunner.utils.TimeUtils;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;

/**
 * APK 运行管理器
 * 
 * @author Yichou 2013-10-8
 *
 */
public final class ApkRunner {
	static final String TAG = "ApkRunner";
	public static final Logger log = Logger.create(TAG);
	
	public static final String INTENT_KEY_MUTILPROC = "mutilproc";
	public static final String INTENT_ACTION_LAUNCH = "com.android.apkrunner.launch";
	public static final String INTENT_ACTION_CRASH = "com.android.apkrunner.APP_CRASH";
	
	public static boolean SERVIEC_PROXY_MOD = false;
	public static boolean PLUGIN_MOD = false;
	public static boolean SDK_MOD = false;
	public static boolean ADS_MOD = false;
	
	/**
	 * 把 apk 文件链接到私有目录做 dexopt
	 * 
	 * 发现这个东西会导致很多问题，把他关闭
	 */
	public static boolean LINK_APK_DEX = false;
	
	/**
	 * 在 native hook接口
	 */
	public static boolean NATIVE_HOOK = false;
	
	/**
	 * 应用数据存储在SD上的标志，默认 true
	 */
	public static boolean APPDATA_TO_SD = true;

	/**
	 * 启动时导入静态 广播接收器
	 */
	public static boolean INSTALL_RECEIVER = true;
	
	/**
	 * 启动时导入 ContentProvide
	 */
	public static boolean INSTALL_PROVIDER= true;
	
	/**
	 * RunnerApplication 在 mainfest 配置标志，默认false
	 */
	public static boolean APPLICATION_DECLARED = false;

	public static String PROXY_SERVICE_NAME;
	public static String PROXY_ACTIVITY_NAME;
	public static String ENTRY_ACTIVITY_NAME;
	
	/**
	 * 进程个数，默认5
	 */
	private static int MAX_PROC_COUNT = 5;
	
	static final ApkRunner runner = new ApkRunner();
	
	public static InstallListener sInstallListener;
	private static Context sShellContext;
	private static String sCurProcessName;
	private static boolean soLoaded = false;
	
	private final HashMap<String, WeakReference<ApkInfo>> sPackges = 
		new HashMap<String, WeakReference<ApkInfo>>();
	private PackageManager mPackageManager;
	
	
	static {
		//PackgeManager 需要在比较早的时机初始化
		QPackgeManager.proxy();
		
//		observeCurProcess(); //需要系统权限
	}
	
	public static ApkRunner getRunner() {
		return runner;
	}
	
	/**
	 * 在第一个加载的 java 代码的 static 块调用此方法
	 */
	public static void staticInit() {
	}

	/**
	 * 调用此方法初始化 ApkRunner
	 * 
	 * @param context
	 */
	public static void init(Context context) {
		attachShellContext(context);
	}
	
	private static void attachShellContext(Context context) {
		sShellContext = context.getApplicationContext()==null? context : context.getApplicationContext();
		
		//为什么要放在这里啊？
		Core.initCore();
	}
	
	public static Context getShellContext() {
		return sShellContext;
	}
	
	public static void setContext(Context context) {
		ApkRunner.sShellContext = context;
	}
	
	public static String getShellPkg() {
		return sShellContext!=null? sShellContext.getPackageName() : null;
	}
	
	private static void checkEnv() {
		if(PROXY_ACTIVITY_NAME == null || PROXY_SERVICE_NAME == null)
			throw new NullPointerException("proxy ComponentName not set!");
		
		if(sProcStates == null)
			throw new NullPointerException("process count not set!");
		
		/**
		 * 如果仅仅调用 apkrunner 则不需要这个东西
		 */
//		if(sShellContext == null)
//			throw new NullPointerException("application context not attached!");
	}
	
	public static void saveData() {
		SharedPreferences sp = sShellContext.getSharedPreferences("apkrunner_revive_data", 0);
		sp.edit()
		  .putInt("proc", sProcIndex)
		  .putString("entry", ENTRY_ACTIVITY_NAME)
		  .commit();
	}
	
	public static void restoreData() {
		SharedPreferences sp = sShellContext.getSharedPreferences("apkrunner_revive_data", 0);
		sProcIndex = sp.getInt("proc", -1);
		ENTRY_ACTIVITY_NAME = sp.getString("entry", ENTRY_ACTIVITY_NAME);
	}
	
	/**
	 * 监听当前进程状态
	 * 
	 * @hide
	 */
	public static void observeCurProcess() {
//		try {
//			ActivityManagerNative.getDefault().registerProcessObserver(new IProcessObserver() {
//				
//				@Override
//				public IBinder asBinder() {
//					return null;
//				}
//				
//				@Override
//				public void onProcessDied(int arg0, int arg1) throws RemoteException {
//					QLog.i(TAG, "onProcessDied " + arg0 + "," + arg1);
//				}
//				
////				@Override
//				public void onImportanceChanged(int arg0, int arg1, int arg2) throws RemoteException {
//					QLog.i(TAG, "onImportanceChanged " + arg0 + "," + arg1 + "," + arg2);
//				}
//				
//				@Override
//				public void onForegroundActivitiesChanged(int arg0, int arg1, boolean arg2) throws RemoteException {
//					QLog.i(TAG, "onForegroundActivitiesChanged " + arg0 + "," + arg1 + "," + arg2);
//				}
//			});
//		} catch (RemoteException e) {
//		}
	}
	
	public static void setCurProcessName(String name) {
		ApkRunner.sCurProcessName = name;
	}
	
	/**
	 * 获取当前进程名
	 */
	public static String getCurProcessName() {
		if (sCurProcessName == null) {
			try {
				final List<RunningAppProcessInfo> processes = ActivityManagerNative.getDefault().getRunningAppProcesses();
				final int pid = android.os.Process.myPid();
				
				for (RunningAppProcessInfo proce : processes) {
					if (proce.pid == pid) {
						sCurProcessName = proce.processName;
						break;
					}
				}
			} catch (RemoteException e) {
			}
		}
		
		return sCurProcessName;
	}
	
	/**
	 * 判断当前进程是不是运行 apk 的进，标志为 进程名包含 :app <br />
	 * 规定：运行 apk 的进程都是 app0 ~ appx
	 */
	public static boolean isAppProcess() {
		String processName = getCurProcessName();
		return (processName != null && processName.indexOf(":app") != -1);
	}
	
	public ApkInfo getApkInfo(String apkPath) {
		return getApkInfo(apkPath, null);
	}

	/**
	 * 从缓存（不存在则创建）获取 ApkInfo 对象
	 * 
	 * @param context
	 * @param apkPath
	 * @param l
	 * @return
	 */
	public ApkInfo getApkInfo(String apkPath, ApkLifeListener l) {
		synchronized (sPackges) {
			try {
				PackageInfo packageInfo = sShellContext.getPackageManager().getPackageArchiveInfo(apkPath, 0);
				WeakReference<ApkInfo> ref = sPackges.get(packageInfo.packageName);
				
				ApkInfo apkInfo = ref != null ? ref.get() : null;
				if (apkInfo == null) {
					apkInfo = new ApkInfo(sShellContext, apkPath, l);
					
					sPackges.put(packageInfo.packageName, new WeakReference<ApkInfo>(apkInfo));
				}
				
				return apkInfo;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 通过包名取 apkInfo，不存在不创建
	 * 
	 * @param packgeName
	 * @param l
	 * @return
	 */
	public ApkInfo getApkInfoNoCheckPkg(String packageName) {
		synchronized (sPackges) {
			try {
				WeakReference<ApkInfo> ref = sPackges.get(packageName);
				
				ApkInfo apkInfo = ref != null ? ref.get() : null;
				
				return apkInfo;
			} catch (Exception e) {
			}
		}
		
		return null;
	}
	
	public static void setInstallListener(InstallListener l) {
		sInstallListener = l;
	}
	
	public static boolean handleInstall(String path, Intent installIntent) {
		SdkManager.setDataPoint("install", "time", null, null);
		
		ApkInfo runner = currentRunning();
		
		File file = new File(path);
		PackageInfo info = runner.getShellContext().getPackageManager().getPackageArchiveInfo(path, 0);

		if(info == null) { //invalid apk
			file.delete();
			return true;
		}
		
		SdkManager.onInstallReq(info.packageName);
		
		if(sInstallListener != null) {
			int ret = sInstallListener.onInstall(path, installIntent);
			
			if(ret == InstallListener.RET_TO_SYSTEM)
				return false;
			else if(ret == InstallListener.RET_HANDLED)
				return true;
		}
		
		File newFile = null;
		
		if(info.packageName.equals(runner.getPackgeName())) {
			newFile = new File(runner.getApkPath());
		} else {
			File file2 = new File(runner.getApkPath());
			newFile = new File(file2.getParent(), file.getName());
		}
		
		boolean ret = file.renameTo(newFile);
		if(ret)
			QLog.d(TAG, "install suc!");
		else
			QLog.w(TAG, "install move file fail!");
		
		return true;
	}	
	
	public static void handleShortcut(Intent intent) {
		SdkManager.onShortcutReq();
	}
	
	//--------- process local ---------------------------------------//
	private static ApkInfo sCurrentRunningApkInfo;
	
	/**
	 * 默认为 -1 当这个没设置表明在主进程运行
	 */
	private static int sProcIndex = -1; //进程序号,
	
	/**
	 * @hide
	 */
	public static ApkInfo currentRunning() {
		return sCurrentRunningApkInfo;
	}
	
	/**
	 * @hide
	 */
	public static void setCurrentRunning(ApkInfo apkInfo) {
		sCurrentRunningApkInfo = apkInfo;
	}
	
	public /*synchronized */static void nativeInit(ApkInfo apkInfo) {
		if(!NATIVE_HOOK)
			return;
		
		//同步主要是考虑 so 重复加载，应该没必要
		if(!soLoaded) { 
			System.loadLibrary("apkrunner");
			soLoaded = true;
		}
		
		native_setAppInfo(new String[]{
				ApkInfo.SD_PATH,
				apkInfo.getShellPackgeName(),
				apkInfo.getPackgeName(), 
				apkInfo.getAppDataPath(),
				apkInfo.getPrivateAppDataPath()
		});
	}
	
	public static void killProcess(int pid) {
		native_kill(pid, 9);
	}
	
	native static void native_kill(int pid, int sig);
	
	native static void native_setAppInfo(String[] pkg);
	
	/**
	 * @hide
	 */
	public static void setCurrentProcIndex(int index) {
		sProcIndex = index;
	}
	
	/**
	 * @hide
	 */
	public static int currentProcIndex() {
		return sProcIndex;
	}
	
	/**
	 * @hide
	 */
	public static String getProxyActivityName(String type) {
		String name = (type!=null? (PROXY_ACTIVITY_NAME + type) : PROXY_ACTIVITY_NAME);
		
		return (sProcIndex==-1? name : name + Integer.valueOf(sProcIndex));
	}
	
	public static String getProxyActivityName() {
		return getProxyActivityName(null);
	}

		
	/**
	 * @hide
	 */
	public static String getProxyServiceName() {
		return (sProcIndex==-1? PROXY_SERVICE_NAME : PROXY_SERVICE_NAME + Integer.valueOf(sProcIndex));
	}
	
	//--------------- process manager ----------------------------------------------------//
	/**
	 * @hide
	 */
	static final class ProcState {
		enum State{
			STATE_IDLE, STATE_READY, STATE_RUNNING
		};
//		static final byte STATE_IDLE = 0x00, STATE_READY = 0x01, STATE_RUNNING = 0x02;

		State state = State.STATE_IDLE;
		long readyTime;
		String apkPath;
		AppProcConn conn;
		
		@Override
		public String toString() {
			return "proc state " + state.name() 
				+ " conn=" + conn;
		}
	}
	
	/**
	 * @hide
	 */
	static final class ProcInfo {
		public long startTime; //启动时间
		public long lastActiveTime; //最后活动时间（根据此时间决定被杀优先级）
		public int pid;
	}

	private static ProcState[] sProcStates;
	
	static {
		initProcs();
	}
	
	private static void initProcs() {
		sProcStates = new ProcState[MAX_PROC_COUNT];
		
		for(int i=0; i<MAX_PROC_COUNT; i++)
			sProcStates[i] = new ProcState();
	}
	
	/**
	 * 设置进程个数
	 * 
	 * @param max
	 */
	public static void setMaxProcessCount(int max) {
		MAX_PROC_COUNT = max;
		initProcs();
	}
	
	private static void resetProcState(int index) {
		synchronized (sProcStates) {
			sProcStates[index].apkPath = null;
			sProcStates[index].conn = null;
			sProcStates[index].readyTime = 0;
			sProcStates[index].state = ProcState.State.STATE_IDLE;
		}
	}
	
	/**
	 * @hide
	 */
	private static final class AppProcConn implements ServiceConnection, Callback, IMsgCode {
		private final Messenger mLocal = new Messenger(new Handler(this));
		private Messenger mRemote;
		private String mApkPath;
		private int mProcIndex;
		private Context mAppContext;
		private ApkRunCallback mCallback;
//		private boolean bConnected = false;
		final ProcInfo mProcInfo = new ProcInfo();
		
		
		public AppProcConn(Context context, String apkPath, int procIndex, ApkRunCallback cb) {
			this.mAppContext = context.getApplicationContext();
			this.mApkPath = apkPath;
			this.mProcIndex = procIndex;
			this.mCallback = cb;
		}
		
		private void reStart() {
			mProcInfo.pid = 0;
			synchronized (sProcStates) {
				sProcStates[mProcIndex].state = ProcState.State.STATE_READY;
			}
		}
		
		public void resume() {
			boolean ret = sendMsg(MSG_RESUME, 0, 0, null);
			
			//应该检测进程还在不在
			if(!ret) {
				reStart();
			}
		}
		
		public void exit() {
			sendMsg(MSG_EXIT, 0, 0, null);
			
			Process.killProcess(mProcInfo.pid);
			
			mAppContext.unbindService(this);
			
			resetProcState(mProcIndex);
		}

		private boolean sendMsg(int what, int arg1, int arg2, Bundle bundle) {
			Message msg = new Message();
			msg.what = what;
			msg.arg1 = arg1;
			msg.arg2 = arg2;
			msg.obj = bundle;
			msg.replyTo = mLocal;

			try {
				mRemote.send(msg);
			} catch (RemoteException e) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			QLog.i(TAG, "onServiceConnected proc=" + mProcIndex);
			
//			bConnected = true;
			mRemote = new Messenger(service);
			sendMsg(MSG_HELLO, mProcIndex, 0, null);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			QLog.i(TAG, "onServiceDisconnected proc=" + mProcIndex);

//			bConnected = false;
			try {
				mAppContext.unbindService(this); //app proc be killed!
			} catch (Exception e) {
			}
			
			resetProcState(mProcIndex);
		}

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HELLO: { //回馈 pid packgeName
				mProcInfo.startTime = System.currentTimeMillis();
				mProcInfo.lastActiveTime = System.currentTimeMillis();
				mProcInfo.pid = msg.arg1;
				
				QLog.i(TAG, "recv feedback from pid " + mProcInfo.pid 
						+ ", at " + TimeUtils.getTimeNow());
				
				//如果还在ready状态
				synchronized (sProcStates) {
					if(sProcStates[mProcIndex].state == ProcState.State.STATE_RUNNING) { //被人抢先了一步，你回去吧
						QLog.e(TAG, "who did this? I am just eat a little more last night!");
						
						mAppContext.unbindService(this);
						
						Process.killProcess(mProcInfo.pid);
						
						resetProcState(mProcIndex);
						break;
					}
					
					sProcStates[mProcIndex].state = ProcState.State.STATE_RUNNING;
					sProcStates[mProcIndex].conn = this;
				}

				QLog.i(TAG, "now proc " + Integer.valueOf(mProcIndex) + " is in use!");
				
				Bundle data = new Bundle();
				data.putString("apkPath", mApkPath);
				sendMsg(MSG_START, 0, 0, data);
				
				if(mCallback != null)
					mCallback.onProcessAttached(mProcInfo.pid, mProcIndex);
				
				break;
			}
			
			case MSG_START: { //运行 apk 回馈消息
				QLog.i(TAG, "run apk success!");
				break;
			}
			
			case MSG_PAUSE: {
				break;
			}

			case MSG_RESUME: {
				mProcInfo.lastActiveTime = System.currentTimeMillis();
				QLog.i(TAG, "proc " + mProcIndex + " resumed! at " + TimeUtils.getTimeNow());
				
				break;
			}
			
			case MSG_EXIT: {
				break;
			}
			
			case MSG_APP_APPLICATION_CREATE: {
				if(mCallback != null)
					mCallback.onApplicationCreate( (Bundle) msg.obj);
				break;
			}
			
			case MSG_APP_LAUNCH_FINISH: {
				if(mCallback != null)
					mCallback.onLaunchFinish( (Bundle) msg.obj);
				
				break;
			}
			
			case MSG_CRASH: {
				
				break;
			}

			default:
				return false;
			}

			return true;
		}
	}
	
	private static int getProcIndex(String apkPath) {
		synchronized (sProcStates) {
			for (int i = 0; i < MAX_PROC_COUNT; i++)
				if (sProcStates[i].apkPath != null 
						&& sProcStates[i].apkPath.equals(apkPath))
					return i;
		}
		
		return -1;
	}
	
	private static int getIdleProcIndex() {
		synchronized (sProcStates) {
			for (int i = 0; i < MAX_PROC_COUNT; i++) {
				if (sProcStates[i].state == ProcState.State.STATE_IDLE)
					return i;
			}

			// 1.还处于 ready 状态又运行了一个应用？出问题了吧干掉
			for (int i = 0; i < MAX_PROC_COUNT; i++) {
				if (sProcStates[i].state == ProcState.State.STATE_READY) {
					sProcStates[i].apkPath = null;
					sProcStates[i].state = ProcState.State.STATE_IDLE;
					
					QLog.w(TAG, "use ready proc " + i);
					return i;
				}
			}

			// 2.正在运行，长时间未激活
			long time = System.currentTimeMillis();
			int id = -1;
			for (int i = 0; i < MAX_PROC_COUNT; i++) {
				if (sProcStates[i].state == ProcState.State.STATE_RUNNING) {
					if (sProcStates[i].conn.mProcInfo.lastActiveTime < time) {
						id = i;
						time = sProcStates[i].conn.mProcInfo.lastActiveTime;
					}
				}
			}
			if (id != -1) {
				QLog.w(TAG, "kill running proc " + id);
				sProcStates[id].conn.exit();
				return id;
			}
		}
		
		return -1;
	}
	
	/**
	 * 强制停止正在运行的apk
	 * 
	 * @param procIndex 分配的进程号
	 */
	public static void focestop(int procIndex) {
		QLog.d(TAG, "foce stop ");
		
		synchronized (sProcStates) {
			if (sProcStates[procIndex].state == ProcState.State.STATE_RUNNING) {
				sProcStates[procIndex].conn.exit();
			}
		}
	}
	
	/**
	 * 在独立进程运行 apk
	 * 
	 * @param context
	 * @param apkPath
	 * 
	 * @return apk 运行 id
	 */
	public static int runApkNewProcess(Context context, String apkPath, ApkRunCallback cb) {
		checkEnv();
		
		int procIndex = getProcIndex(apkPath);
		if(procIndex != -1) { //is running
			if(sProcStates[procIndex].state == ProcState.State.STATE_RUNNING) {
				sProcStates[procIndex].conn.resume();
				return -1;
			} else if(sProcStates[procIndex].state == ProcState.State.STATE_READY) { //怎么还在 ready ?
				if(System.currentTimeMillis() - sProcStates[procIndex].readyTime > 1*1000) { //you have ready 1 minute!
					resetProcState(procIndex);
				}
				
				return -1;
			} else if(sProcStates[procIndex].state == ProcState.State.STATE_IDLE) {
				resetProcState(procIndex);
			}
		} else {
			procIndex = getIdleProcIndex();
			
			if(procIndex == -1) {
				QLog.w(TAG, "no idle proc now!");
				return -1;
			}
		}
		
		if(cb != null) {
			cb.onSelectProcess(procIndex);
		}
		
		String service = PROXY_SERVICE_NAME + Integer.valueOf(procIndex);
		
		AppProcConn conn = new AppProcConn(context, apkPath, procIndex, cb);
		Intent intent = new Intent(INTENT_ACTION_LAUNCH);
		intent.setClassName(context, service);
		intent.putExtra(INTENT_KEY_MUTILPROC, true); //多进程标志
		
		//使用全局 context
		context.getApplicationContext().bindService(intent,
				conn, Service.BIND_AUTO_CREATE);
		
		synchronized (sProcStates) {
			sProcStates[procIndex].apkPath = apkPath;
			sProcStates[procIndex].readyTime = System.currentTimeMillis();
			sProcStates[procIndex].state = ProcState.State.STATE_READY;
		}
		
		saveData();
		
		return procIndex;
	}
	
	//-------------- 主进程运行模式 ---------------------------------------------------
	public static void runApkInMainProcess(Context context, String apkPath) {
		checkEnv();
		
		Intent service = new Intent(QRunnerService.START_FOR_RUNAPK);
		service.setClassName(context, getProxyServiceName());
		service.putExtra(QRunnerService.KEY_APK_PATH, apkPath);
		context.getApplicationContext().startService(service);
	}
	
	//----------------------------------------------------------
	public static void runApk(Context context, String apkPath, boolean mainProc) {
		if(mainProc)
			runApkInMainProcess(context, apkPath);
		else
			runApkNewProcess(context, apkPath, null);
	}

	public static void clearData(Context context, String apkPath) {
		ApkInfo.clearAppData(context, apkPath);
	}
}
