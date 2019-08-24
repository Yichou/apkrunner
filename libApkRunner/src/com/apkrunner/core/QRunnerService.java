package com.apkrunner.core;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkLifeListener;
import com.apkrunner.ApkRunner;
import com.apkrunner.IMsgCode;
import com.apkrunner.sdk.SdkManager;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.widget.Toast;

/**
 * apk 服务管理器
 * 
 * @author Yichou 2013-11-13 18:14:19
 *
 */
public class QRunnerService extends Service implements 
	Callback, IMsgCode, ApkLifeListener {
	static final String TAG = "ManagerService";
	
	/**
	 * 一系列启动 RunnerService 命令
	 */
	public static final String START_FOR_BROADCAST = "com.edroid.apkrunner.broadcast";
	public static final String START_FOR_RUNAPK = "com.edroid.apkrunner.runapk";
	public static final String START_FOR_NFC = "com.edroid.apkrunner.notifycation";

	/**
	 * 定义一系列 key 名
	 */
	public static final String KEY_APK_PATH = "kApkPath"; 		//传 apk 路径
	public static final String KEY_REAL_INTENT = "kRealIntent"; //传 真正 intent
	public static final String KEY_PKG = "kPackgeName"; 		//传 apk包名


	private final IBinder mLocalBinder = new LocalBinder();
    private final Handler mHandler = new Handler(this);
    private final Messenger mLocalMessenger = new Messenger(mHandler);
    
    Messenger mRemoteMessenger;
    private ApkInfo mApkInfo;
    
  
    private boolean sendMsg(int what, int arg0, int arg1, Object bundle) {
    	if(mRemoteMessenger == null) {
    		QLog.e(TAG, "who am I talking with?");
    		return false;
    	}
    	
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg0;
		msg.arg2 = arg1;
		msg.obj = bundle;
		msg.replyTo = mLocalMessenger;
//		msg.setData(data);

		try {
			mRemoteMessenger.send(msg);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return false;
	}
    
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_HELLO: {
			ApkRunner.setCurrentProcIndex(msg.arg1);
			mRemoteMessenger = msg.replyTo;
			
			sendMsg(msg.what, Process.myPid(), 0, null);
			break;
		}
		
		case MSG_START: {
			Bundle data = (Bundle) msg.obj;
			if(data == null)
				throw new RuntimeException("who eat my cake? remote data lost!");
			runApk(data.getString("apkPath"));
			break;
		}
		
		case MSG_PAUSE: {
			break;
		}

		case MSG_RESUME: {
			mApkInfo.launch();
			sendMsg(msg.what, 0, 0, null);
			break;
		}
		
		case MSG_EXIT: {
			QLog.i(TAG, "byby!");
			Process.killProcess(Process.myPid());
			System.exit(0);
			break;
		}
		
		case MSG_FUNC_GETAPPICON: {
			break;
		}
		
		default:
			return false;
		}
		
		return true;
	}
	
	private void runApk(String apkPath) {
		mApkInfo = ApkRunner.getRunner().getApkInfo(apkPath, this);
		mApkInfo.attachManagerService(this);
		ApkRunner.setCurrentRunning(mApkInfo);
		mApkInfo.launch();
	}
	
    @Override
    public void onCreate() {
    	QLog.i(TAG, "onCreate");

    	super.onCreate();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		QLog.i(TAG, "onBind " + intent);
		
		boolean flag = intent.getBooleanExtra(ApkRunner.INTENT_KEY_MUTILPROC, false);

		return flag? mLocalMessenger.getBinder() : mLocalBinder;
	}
	
//	private Object getField(String name) {
//		try {
//			Field field = Service.class.getDeclaredField(name);
//			field.setAccessible(true);
//			return field.get(this);
//		} catch (Exception e) {
//		}
//		return null;
//	}
//	
//	public IBinder getToken() {
//		return (IBinder) getField("mToken");
//	}
//	
//	public ActivityThread getActivityThread() {
//		return (ActivityThread) getField("mThread");
//	}
//	
//	public IActivityManager getActivityManager() {
//		return (IActivityManager) getField("mActivityManager");
//	}
	
	private void handleBroadcast(String packageName, Intent intent) {
		try {
			ComponentName cmp = intent.getComponent();
			
			ApkInfo apkInfo = ApkRunner.getRunner().getApkInfoNoCheckPkg(packageName);
			
			BroadcastReceiver receiver = (BroadcastReceiver) apkInfo.loadClass(cmp.getClassName()).newInstance();
			Context context = Frameworks.YContextImpl.getReceiverRestrictedContext(getApplication().getBaseContext());
			receiver.onReceive(new QContext(context), intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////
	private int handleStart(Intent intent, int flags, int startId) {
		if(START_FOR_BROADCAST.equals(intent.getAction())) { //广播转发
			handleBroadcast(intent.getStringExtra(KEY_PKG),
					(Intent) intent.getParcelableExtra(KEY_REAL_INTENT));
			
			stopSelf(startId);
		}
		else if(START_FOR_RUNAPK.equals(intent.getAction())) {
			runApk(intent.getStringExtra(KEY_APK_PATH));

			stopSelf(startId);
		}

		return Service.START_NOT_STICKY;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		QLog.d(TAG, "onStartCommand( " + intent 
				+ ", flags=" + flags
				+ ", startId=" + startId);
		
		if(intent == null)
			return Service.START_NOT_STICKY;
		
		return handleStart(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		QLog.i(TAG, "onDestroy");
		
		super.onDestroy();
	}

	private long loadTime = 0;
	private long startTime = 0;
	

	@Override
	public void onLoad(ApkInfo apk) {
		loadTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		
//		SdkManager.onApkLoad();
		SdkManager.onApkLaunch();
	}
	
	@Override
	public void onLoadFinish(ApkInfo apk) {
		loadTime = (System.currentTimeMillis() - loadTime);
		QLog.d(TAG, "load useTime=" + loadTime);
		
		FrameworkObserver.getDefault().dispatchApkLoadFinish(apk);
	}

	@Override
	public void onLaunch(ApkInfo apk) {
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void onLaunchFinish(ApkInfo apk) {
		if(loadTime != -1) {
			int time = (int) (loadTime + System.currentTimeMillis() - startTime);

			if(QLog.DEBUG)
				Toast.makeText(this, "启动耗时：" + time, Toast.LENGTH_LONG).show();
			
			QLog.d(TAG, "launch useTime=" + time);
			
			SdkManager.onApkLaunchSuc((int) (time));
			
			loadTime = -1;
		}
		
		sendMsg(MSG_APP_LAUNCH_FINISH, 0, 0, mApkInfo.toBundle1());
		
		FrameworkObserver.getDefault().dispatchApkLaunchFinish(apk);
	}

	@Override
	public void onApplicationCreate(ApkInfo apk, Application app) {
		if(mApkInfo == null) 
			mApkInfo = apk;
		
		sendMsg(MSG_APP_APPLICATION_CREATE, 0, 0, apk.toBundle1());
	}
	
	public void notifyCrash() {
		sendMsg(MSG_CRASH, 0, 0, null);
	}
	
	public class LocalBinder extends Binder {
    	public QRunnerService getService() {
            return QRunnerService.this;
        }
    }
}
