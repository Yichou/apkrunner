package com.apkrunner;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.apkrunner.core.ComponentMatcher;
import com.apkrunner.core.FrameworkObserver;
import com.apkrunner.core.Frameworks;
import com.apkrunner.core.QLog;
import com.apkrunner.core.QRunnerService;
import com.apkrunner.core.ComponentMatcher.MatchResult;
import com.apkrunner.utils.CpuUtils;
import com.apkrunner.utils.FileUtils;
import com.apkrunner.utils.CpuUtils.CpuType;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import dalvik.system.DexClassLoader;


/**
 * Apk 加载后的实体类
 * 
 * @author Yichou 2013-10-8
 * 
 * <ul>
 *   <li>2014-3-14：优化几个方法名，确定外部名字叫 shell </li>
 * </ul>
 */
public final class ApkInfo implements Parcelable,
	FrameworkObserver.ApplicationListener, 
	FrameworkObserver.ActivityListener {
	static final String TAG = "ApkInfo";
	
	/**
	 * SD卡路径，末尾带 /
	 */
	public static final String SD_PATH = 
		 Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar;
	
	private static final String PKG = "com.edroid.apkrunner";
	private static final String DIR_PUBLIC = "Android/data/" + PKG;
	private static final String DIR_APPS = "apps";
	private static final String DIR_APP_LIB = "lib";
	
	/**
	 * 存储卡上的根路径，末尾不带 /
	 */
	public static final String PUBLIC_PATH_ROOT = 
		SD_PATH + DIR_PUBLIC ;
	
	/**
	 * 存储卡上的应用根目录，末尾不带 /
	 */
	public static final String PUBLIC_PATH_APPS = 
		PUBLIC_PATH_ROOT + File.separatorChar
		+ DIR_APPS;
	
	/**
	 * 默认读取哪些包信息
	 */
	private static final int DEF_PI_FLAGS = PackageManager.GET_ACTIVITIES
			|PackageManager.GET_META_DATA
			|PackageManager.GET_RECEIVERS
			|PackageManager.GET_SERVICES
			|PackageManager.GET_PROVIDERS
			|PackageManager.GET_SHARED_LIBRARY_FILES
			|PackageManager.GET_CONFIGURATIONS;
	
	private String appDataPath ;
	private String privateAppDataPath ;
	private String appLibPath ;
	private String appSoPath ;
	
	private Context mShellContext;
	private Application mShellApplication;
	private PackageManager mShellPackageManager;
	
	private int mPiFlags = 0;
	private PackageInfo mPackageInfo;
	private String mPackgeName;
	private String mAppLabel;
	private Bitmap mAppIcon;
	private String mApkPath;
	private ClassLoader mClassLoader;
	private Application mApplication;
	private Object mLoadedApk;
	private Resources mResources;
	
	
	private final ComponentMatcher mComponentMatcher = new ComponentMatcher();
	private String mLauncherActivityClass;
	private QRunnerService mManagerService;
	private ApkLifeListener mRunListener;
	
	private Bundle mInfoBundle;
	
	
	public ApkInfo(Context context, String apkPath) {
		this(context, apkPath, null);
	}
	
	public ApkInfo(Context context, String apkPath, ApkLifeListener listener) {
		this.mRunListener = listener;
		FrameworkObserver.getDefault().addActivityListener(this);
		FrameworkObserver.getDefault().addApplicationListener(this);
		
		if(mRunListener != null) mRunListener.onLoad(this);
		loadApk(context, apkPath);
		if(mRunListener != null) mRunListener.onLoadFinish(this);
	}
	
	public ApkInfo(Parcel in) {
		appDataPath = in.readString();
		privateAppDataPath = in.readString();
		appLibPath = in.readString();
		appSoPath = in.readString();
		mPackgeName = in.readString();
		mApkPath = in.readString();
		mAppLabel = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appDataPath);
		dest.writeString(privateAppDataPath);
		dest.writeString(appLibPath);
		dest.writeString(appSoPath);
		dest.writeString(mPackgeName);
		dest.writeString(mApkPath);
		dest.writeString(mAppLabel);
	}

	private PackageInfo createApkPackageInfo(int flags) {
//		long t0 = System.currentTimeMillis();
		PackageInfo ret = mShellPackageManager.getPackageArchiveInfo(mApkPath, flags);
//		QLog.e(TAG, "create packgeInfo 0x" + Integer.toHexString(flags) + " useTime=" + (System.currentTimeMillis() - t0));
		return ret;
	}
	
	private PackageInfo updatePackageInfo(int newFlags) {
		if((newFlags|mPiFlags) != mPiFlags) {
			QLog.d(TAG, "create packgeInfo flags=" + Integer.toHexString(newFlags));
			
			mPiFlags |= newFlags;
			mPackageInfo = createApkPackageInfo(mPiFlags);
			
			//需要重设所有 ApplicationInfo
			setApplicationInfoAll();
		}
		
		return mPackageInfo;
	}
	
	private void makePackageInfo() {
		mPiFlags = DEF_PI_FLAGS;
		mPackageInfo = createApkPackageInfo(mPiFlags);
		
		if(mPackageInfo == null) 
			throw new RuntimeException("invalid apk file!");
		
		mPackgeName = mPackageInfo.packageName;
	}
	
	private void loadApk(Context context, String apkPath) {
		mApkPath = apkPath;
		mShellContext = context.getApplicationContext();
		mShellApplication = (Application) context.getApplicationContext();
		mShellPackageManager = context.getPackageManager();
		
		makePackageInfo();
		
		//放在何处适合
		ApkRunner.setCurrentRunning(this);

		initPath(); //最先初始化
		
		//native 层设置路径，在路径初始化之后
		ApkRunner.nativeInit(this);
		
		setApplicationInfoAll();
		parseApk();
		makeClassLoader();
		makeLoadedApk();
	}
	
	public void attachManagerService(QRunnerService service) {
		this.mManagerService = service;
		getApplication();
	}
	
	/**
	 * 获取 app sharepreference 存储路径，末尾不带 /
	 * @return
	 */
	public String getAppPrefsPath() {
		return appDataPath + "/shared_prefs";
	}

	/**
	 * 获取 app files 存储路径，末尾不带 /
	 * @return
	 */
	public String getAppFilesPath() {
		return appDataPath + "/files";
	}
	
	/**
	 * 安装后名字
	 * 
	 * @return
	 */
	public String getAppLabel() {
		if(mAppLabel == null) {
			mAppLabel = getResources().getText(getApplicationInfo().labelRes).toString();
		}

		return mAppLabel;
	}
	
	/**
	 * 安装后图标
	 * 
	 * @return
	 */
	public Bitmap getAppIcon() {
		return mAppIcon;
	}
	
	private void initPath() {
		String root = null;
		
		if(ApkRunner.APPDATA_TO_SD && !FileUtils.isSDAvailable(32)) {
			ApkRunner.APPDATA_TO_SD = false;
			QLog.w(TAG, "not enough space on sdcard to save app data!");
		}
		
		String privateRoot = mShellContext.getDir(DIR_APPS, Context.MODE_PRIVATE).getAbsolutePath();
		privateAppDataPath = privateRoot + File.separatorChar + mPackgeName;
		
		if(ApkRunner.APPDATA_TO_SD) {
			root = PUBLIC_PATH_APPS;
		} else {
			root = privateRoot;
		}
		
		appDataPath = root + File.separatorChar + mPackgeName;
		appLibPath = appDataPath + File.separatorChar + DIR_APP_LIB;
		appSoPath = privateAppDataPath + File.separatorChar + DIR_APP_LIB;
		
		FileUtils.createDir(getAppPrefsPath());
		FileUtils.createDir(getAppFilesPath());
		
		String privateFilesPath = privateAppDataPath + "/files";
		FileUtils.createDir(privateFilesPath);
		
		FileUtils.createDir(appSoPath);
		
		//设置私有目录权限
		FileUtils.setPermissions(privateAppDataPath, 
				FileUtils.S_IRWXU
				|FileUtils.S_IRGRP|FileUtils.S_IXGRP
				|FileUtils.S_IXOTH);

		FileUtils.setPermissions(appSoPath, 
				FileUtils.S_IRWXU
				|FileUtils.S_IRWXG
				|FileUtils.S_IXOTH);
		
		FileUtils.setPermissions(privateFilesPath, 
				FileUtils.S_IRWXU
				|FileUtils.S_IRWXG
				|FileUtils.S_IXOTH);
		
		FileUtils.createDir(appLibPath);
	}
	
	private void parseApk() {
		try {
			ZipFile zipFile = new ZipFile(mApkPath);
			Enumeration<? extends ZipEntry> e = zipFile.entries();
			
			List<ZipEntry> sos = new ArrayList<ZipEntry>();
			List<ZipEntry> sos_v7a = new ArrayList<ZipEntry>();
			List<ZipEntry> sos_x86 = new ArrayList<ZipEntry>();
			List<ZipEntry> sos_mips = new ArrayList<ZipEntry>();
			
			while(e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				String name = entry.getName();
				
				if(name.startsWith(CpuType.ARM.getZipPath())) {
					sos.add(entry);
				}
				else if(name.startsWith(CpuType.ARMV7.getZipPath())) {
					sos_v7a.add(entry);
				}
				else if(name.startsWith(CpuType.X86.getZipPath())) {
					sos_x86.add(entry);
				}
				else if(name.startsWith(CpuType.MIPS.getZipPath())) {
					sos_mips.add(entry);
				}
				else if (name.equals("AndroidManifest.xml")) {
					InputStream is = null;
					try {
						is = zipFile.getInputStream(entry);
						byte[] mainfestFileData = new byte[is.available()];
						is.read(mainfestFileData);
						
						mComponentMatcher.loadMainfest(mainfestFileData);
					} catch (Exception e2) {
						e2.printStackTrace();
						
						throw new RuntimeException("Can't read or parse AndroidManifest.xml!");
					} finally {
						try {
							is.close();
						} catch (Exception e3) {
						}
					}
				}
			}
			
			//处理找到的 so
			CpuType cpuType = CpuUtils.getCpuType();
			List<ZipEntry> soList = null;
			
			if(cpuType == CpuType.X86) {
				soList = sos_x86;
				QLog.i(TAG, "so abi is x86");
			} else if (cpuType == CpuType.MIPS) {
				soList = sos_mips;
				QLog.i(TAG, "so abi is mips");
			} else if(cpuType == CpuType.ARMV7 && sos_v7a.size() > 0){
				soList = sos_v7a;
				QLog.i(TAG, "so abi is armv7");
			} else {
				soList = sos;
				cpuType = CpuType.ARM;
				QLog.i(TAG, "so abi is arm");
			}
			
			if(soList.size() > 0) {
				QLog.d(TAG, "so count=" + soList.size() + ", abi=" + cpuType.getAbi());
				
				final int START = cpuType.getZipPath().length() - 1;
				for(ZipEntry entry : soList) {
					String name = entry.getName();
					File file = new File(appSoPath, name.substring(START));

					if(!FileUtils.checkExistBySize(file, entry.getCompressedSize())) {
						FileUtils.streamToFile(file, zipFile.getInputStream(entry), false);
						
						//设置权限
						FileUtils.setPermissions(file.getAbsolutePath(),
								FileUtils.S_IRWXU|FileUtils.S_IRGRP|FileUtils.S_IROTH|FileUtils.S_IXGRP|FileUtils.S_IXOTH);
					}
					
					if(!appSoPath.equals(appLibPath)) {
						File file2 = new File(appLibPath, name.substring(START));
						if(!FileUtils.checkExistBySize(file2, entry.getCompressedSize())) {
							FileUtils.streamToFile(file2, zipFile.getInputStream(entry), false);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void makeClassLoader() {
		ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();

		String tmpPath = mApkPath;
		
		if(ApkRunner.LINK_APK_DEX) {
			//使用 ‘app@包名@classes.apk’ 格式生成 dexopt 文件名，保证同一个文件不同文件名不需要再次 dexopt
			//即使apk版本不同，系统会自动检测，是否重新 dexopt
			File root = mShellContext.getDir("app", 0);
			File tmpFile = new File(root, mPackgeName + ".apk");
			if(tmpFile.exists())
				tmpFile.delete();
			
			tmpPath = tmpFile.getAbsolutePath();
			FileUtils.createLink(mApkPath, tmpPath);
			
			QLog.d(TAG, "link [" + mApkPath + "] -> [" + tmpPath + "] for dexopt");
		}
		
		FileUtils.createDir(appLibPath);
		
		mClassLoader = new DexClassLoader(tmpPath, 
				appLibPath,
				appSoPath, 
				baseParent);
		
		/**
		 * 使用完不能删除，从 apk 代码查找资源时还需要
		 */
//		tmpFile.delete();
		
		if(ApkRunner.SDK_MOD) {
			/**
			 * SDK 模式将 插件的 classLoader 放在 app的上层，
			 * 这样 app 可以明的引用  sdk 中的类 
			 */
			Frameworks.YClassLoader.insertParent(mShellContext.getClassLoader(), mClassLoader);
		}
	}
	
	private void makeLoadedApk() {
		mLoadedApk = Frameworks.YActivityThread.getPackageInfoNoCheck(mPackageInfo.applicationInfo, 
				mShellContext.getResources());
		
		if(mLoadedApk == null)
			throw new NullPointerException("can't create LoadedApk in framwork!");
		
		QLog.d(TAG, ":::plugin loadedApk create suc!");
		
		//设置 Classloader
		Frameworks.YLoadedApk.setClassLoader(mLoadedApk, mClassLoader);

		//获取 Resources 对象
		mResources = (Resources) Frameworks.YLoadedApk.getResources(mLoadedApk);
	}
	
	private void setApplicationInfo(ApplicationInfo info) {
		ApplicationInfo mainInfo = mShellContext.getApplicationInfo();
		ApplicationInfo info2 = mPackageInfo.applicationInfo;
		
		info.dataDir = appDataPath;
		info.publicSourceDir = mApkPath;
		info.sourceDir = mApkPath;
		info.uid = mainInfo.uid; //使用相同的 uid
		info.metaData = info2.metaData;
//		info.processName = ;
//		info.packageName = ;
//		info.processName = mainInfo.processName;
		
		try {
			Field field = info.getClass().getDeclaredField("nativeLibraryDir");
			field.setAccessible(true);
			field.set(info, appSoPath);
		} catch (Exception e) {
		}
	}
	
	private void setApplicationInfoAll() {
		setApplicationInfo(mPackageInfo.applicationInfo);
		
		if(mPackageInfo.activities != null) {
			for(ActivityInfo info : mPackageInfo.activities) {
				setApplicationInfo(info.applicationInfo);
			}
		}
		
		if (mPackageInfo.services != null) {
			for (ServiceInfo info : mPackageInfo.services)
				setApplicationInfo(info.applicationInfo);
		}
	}
	
	/**
	 * 获取插件包名
	 * 
	 * @return
	 */
	public String getPackgeName() {
		return mPackgeName;
	}
	
	public int getVersion() {
		return mPackageInfo.versionCode;
	}
	
	public PackageInfo getPackageInfo() {
		return mPackageInfo;
	}
	
	public PackageInfo getPackageInfo(int flags) {
		//获取签名比较耗时，在此处完成
		if((mPiFlags&PackageManager.GET_SIGNATURES) == 0 && (flags&PackageManager.GET_SIGNATURES) != 0) {
			QLog.w(TAG, "make sig PackageInfo flags=0x" + Integer.toHexString(flags));
			long t0 = System.currentTimeMillis();
			
			PackageInfo mSigInfo = null;
			
			//4.0 系统下有BUG
			if(VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH ) {
				mSigInfo = Frameworks.YPackgeManager.getPackageArchiveInfo(mApkPath, 
						PackageManager.GET_SIGNATURES);
			} else {
				mSigInfo = mShellPackageManager.getPackageArchiveInfo(mApkPath, 
						PackageManager.GET_SIGNATURES);
			}
			
			if(mSigInfo != null) {
				mPiFlags |= PackageManager.GET_SIGNATURES;
				mPackageInfo.signatures = mSigInfo.signatures;
			} else {
				QLog.e(TAG, "can't get signatures!");
			}
			
			QLog.i(TAG, "get signature useTime=" + (System.currentTimeMillis() - t0));
		}
		
		if((flags|mPiFlags) == mPiFlags) //所有字段已获取
			return mPackageInfo;
		
		return updatePackageInfo(flags);
	}
	
	public ApplicationInfo getApplicationInfo(int flags) {
		return getPackageInfo(flags).applicationInfo;
	}
	
	//-------------------------------------------------------------------
	public ActivityInfo getActivityInfo(ComponentName cmp) {
		return getActivity(cmp.getClassName());
	}
	
	public ActivityInfo getActivity(String name) {
		if(mPackageInfo.activities == null || name == null)
			return null;
		
		for(ActivityInfo info : mPackageInfo.activities) {
			if(info.name.equals(name)) 
				return info;
		}
		
		return null;
	}
	
	/**
	 * 隐式转显式
	 * 
	 * @param intent 查找成功之后 CompnetName 将被设置为确定组件
	 */
	public ActivityInfo resloveActivity(Intent intent) {
		String className = checkComponetName(mComponentMatcher.match(ComponentMatcher.CMP_ACTIVITY, intent));
		
		if(className != null) {
			intent.setClassName(mPackgeName, className);
			return getActivity(className);
		}
		
		return null;
	}
	
	public boolean hasActivity(String name) {
		return getActivity(name) != null;
	}
	
	public ResolveInfo queryIntentActivity(Intent intent, int flags) {
		MatchResult ret = mComponentMatcher.matchResult(ComponentMatcher.CMP_ACTIVITY, intent);
		if(ret != null) {
			ResolveInfo resolveInfo = new ResolveInfo();
			resolveInfo.priority = ret.filter.getPriority();
			resolveInfo.filter = ret.filter;
			resolveInfo.activityInfo = getActivity(checkComponetName(ret.name));
			
			return resolveInfo;
		}
		
		return null;
	}
	
	//--------------------------------------------------------
	public ServiceInfo getServiceInfo(ComponentName cmp) {
		return getService(cmp.getClassName());
	}
	
	public ServiceInfo getService(String name) {
		if(mPackageInfo.services == null || name == null)
			return null;
		
		for(ServiceInfo info : mPackageInfo.services) {
			if(info.name.equals(name))
				return info;
		}
		
		return null;
	}
	
	public ServiceInfo resloveService(Intent intent) {
		String className = checkComponetName(mComponentMatcher.match(ComponentMatcher.CMP_SERVICE, intent));
		
		if(className != null) {
			intent.setClassName(mPackgeName, className);
			return getService(className);
		}
		
		return null;
	}
	
	public ResolveInfo queryIntentService(Intent intent, int flags) {
		MatchResult ret = mComponentMatcher.matchResult(ComponentMatcher.CMP_SERVICE, intent);
		if(ret != null) {
			ResolveInfo resolveInfo = new ResolveInfo();
			resolveInfo.priority = ret.filter.getPriority();
			resolveInfo.filter = ret.filter;
			resolveInfo.serviceInfo = getService(checkComponetName(ret.name));
			
			return resolveInfo;
		}
		
		return null;
	}
	
	//------------------------------------------------------------------
	public ActivityInfo getReceiver(String name) {
		if(mPackageInfo.receivers != null || name == null)
			return null;
		
		for(ActivityInfo info : mPackageInfo.receivers) {
			if(info.name.equals(name)) 
				return info;
		}
		return null;
	}
	
	public ActivityInfo resloveReceiver(Intent intent) {
		String className = checkComponetName(mComponentMatcher.match(ComponentMatcher.CMP_RECEIVER, intent));
		
		if(className != null) {
			intent.setClassName(mPackgeName, className);
			return getReceiver(className);
		}
		
		return null;
	}
	
	public ProviderInfo getContentProvider(String auth) {
		if(auth == null || mPackageInfo.providers == null)
			return null;
		
		for(ProviderInfo info : mPackageInfo.providers) {
			if(info.authority.equals(auth))
				return info;
		}
		
		return null;
	}
	
	/**
	 * 获取壳 Context
	 */
	public Context getShellContext() {
		return mShellContext;
	}
	
	/**
	 * 获取壳 包名
	 */
	public String getShellPackgeName() {
		return mShellContext.getPackageName();
	}
	
	/**
	 * 获取壳 包管理器
	 */
	public PackageManager getShellPackageManager() {
		return mShellPackageManager;
	}
	
	/**
	 * 获取壳 Application
	 */
	public Application getShellApplication() {
		return mShellApplication;
	}
	
	private String checkComponetName(String name) {
		if(name == null)
			return null;
		
		if(name.charAt(0) == '.')
			return mPackgeName + name;
		else if(name.indexOf('.') == -1)
			return mPackgeName + '.' + name;
		else
			return name;
	}
	
	public String getLauncherActivityClass() {
		if(mLauncherActivityClass == null) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			
			
			
			mLauncherActivityClass = checkComponetName(mComponentMatcher.match(ComponentMatcher.CMP_ACTIVITY,
					intent));
			
			if(mLauncherActivityClass == null)
				throw new RuntimeException("can't find launch activity!");

			QLog.d(TAG, ":::launch activity=" + mLauncherActivityClass);
		}
		
		return mLauncherActivityClass;
	}
	
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}
	
	public String getApkPath() {
		return mApkPath;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		
		sb.append("apkinfo:").append('\n');
		sb.append("  packgeName:").append(mPackgeName).append('\n');
		sb.append("  versionCode:").append(mPackageInfo.versionCode).append('\n');
		
		return sb.toString();
	}
	
	private void installContentProviders(Application app) {
		ProviderInfo[] providers = mPackageInfo.providers;
		
		if(providers == null || providers.length == 0) {
			return;
		}
		
		QLog.i(TAG, "install providers count=" + providers.length);
		
		Arrays.sort(providers, new Comparator<ProviderInfo> () {

			@Override
			public int compare(ProviderInfo lhs, ProviderInfo rhs) {
				return (lhs.initOrder - rhs.initOrder);
			}
		});
		
		ArrayList<ProviderInfo> list = new ArrayList<ProviderInfo>(providers.length);
		for(ProviderInfo provider : providers) {
			list.add(provider);
		}
		
		Frameworks.YActivityThread.installContentProviders(app, list);
		
		Frameworks.YActivityThread.enableJit();
	}
	
	private void installReceivers(Application app) {
		ActivityInfo[] receivers = mPackageInfo.receivers;
		
		if(receivers != null && receivers.length > 0) {
			QLog.i(TAG, "install receviers count=" + receivers.length);
			
			for(ActivityInfo r : receivers) {
				List<IntentFilter> filters = mComponentMatcher.getParseInfo().getReceiver(r.name);
				
				if(filters != null && filters.size() > 0) {
					try {
						BroadcastReceiver receiver = null;
						
						try {
							receiver = (BroadcastReceiver) loadClass(r.name).newInstance();
						} catch (Exception e) {
						}
						
						for(IntentFilter filter : filters) {
							app.registerReceiver(receiver, filter);
						}
						
//						QLog.i(TAG, "installRecevier " + r.name + " suc, filter count=" + filters.size());
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public ApplicationInfo getApplicationInfo() {
		return mPackageInfo.applicationInfo;
	}
	
	//? 有可能导致死循环
	public Application getApplication() {
		if(mApplication == null) {
			mApplication = (Application) Frameworks.YLoadedApk.makeApplication(mLoadedApk);
		}
		
		return mApplication;
	}
	
	public Resources getResources() {
		return mResources;
	}
	
	public Object getLoadedApk() {
		return mLoadedApk;
	}
	
	/**
	 * 获取应用数据存储根目录
	 * 
	 * @return
	 */
	public String getAppDataPath() {
		return appDataPath;
	}
	
	/**
	 * 获取应用在私有目录下的存储路径，末尾不带 /
	 * 
	 * @return
	 */
	public String getPrivateAppDataPath() {
		return privateAppDataPath;
	}
	
	public String getAppLibPath() {
		return appLibPath;
	}
	
	public String getAppSoPath() {
		return appSoPath;
	}
	
	/**
	 * 加载插件中的类
	 * 
	 * @param className
	 * @return
	 * 
	 * @throws ClassNotFoundException
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return mClassLoader.loadClass(className);
	}
	
	public QRunnerService getManagerService() {
		return mManagerService;
	}
	
	public void setRunListener(ApkLifeListener l) {
		this.mRunListener = l;
	}
	
	@SuppressWarnings("deprecation")
	private void showNotify2() {
		Intent intent = new Intent(QRunnerService.START_FOR_NFC);
		intent.setClassName(mShellContext, ApkRunner.getProxyServiceName());
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		QLog.i(TAG, "notify intent " + intent);
		
		Notification n = new Notification(
				android.R.drawable.ic_menu_share,
				null, 
				System.currentTimeMillis());
		
		PendingIntent contentIntent = PendingIntent.getService(mShellContext, 
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		n.setLatestEventInfo(mShellContext, getAppLabel() +" 正在运行", "点击返回", contentIntent);
		n.flags |= Notification.FLAG_NO_CLEAR;
		
		NotificationManager mNM = (NotificationManager) mShellApplication.getSystemService(Context.NOTIFICATION_SERVICE);
		mNM.notify(android.R.drawable.ic_menu_share, n);
	}
	
	public void launch() {
		if(mRunListener != null) mRunListener.onLaunch(this);

		Frameworks.disableDeathOnNetwork();
		
		Intent intent = new Intent();
		intent.setClassName(getPackgeName(), getLauncherActivityClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		
		mShellContext.startActivity(intent);
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("launch apk {")
		  .append("path=").append(mApkPath)
		  .append(", pkg=").append(mPackgeName)
		  .append(", launch act=").append(mLauncherActivityClass)
		  .append(", pid=").append(Process.myPid())
		  .append('}');
		
		QLog.d(TAG, sb.toString());
		
		showNotify2();
	}

	@Override
	public void onNewActivity(String className) {
	}

	@Override
	public void performCreateActivity(Activity activity) {
//		ActivityInfo info = getActivity(activity.getClass().getName());
//		if(info != null) {
//			activity.setRequestedOrientation(info.screenOrientation);
//		}
	} 
	
	@Override
	public void onActivityCreate(Activity activity) {
		if(activity.getClass().getName().equals(mLauncherActivityClass)) {
			if(mRunListener != null) mRunListener.onLaunchFinish(this);
		}
	}
	
	@Override
	public void onActivityDestroy(Activity activity) {
	}

	@Override
	public void onNewApplication(String className) {
	}
	
	@Override
	public void onApplicationCreate(Application application) {
		//显然创建 application 必须是apk的
		if(application.getPackageName().equals(getPackgeName())) {
			QLog.d(TAG, "::: application on create");
			
			this.mApplication = application;
			
			Frameworks.YActivityThread.setInitApplication(application);

			if(ApkRunner.INSTALL_PROVIDER)
				installContentProviders(application);
			
			if(ApkRunner.INSTALL_RECEIVER) {
				if(!getPackgeName().equals("com.qihoo.appstore"))
					installReceivers(application);
			}

			if(mRunListener != null) 
				mRunListener.onApplicationCreate(this, application);
		}
	}
	
	/**
	 * 返回一个 bundle 存储部分信息
	 * @return
	 */
	public Bundle toBundle1() {
		if(mInfoBundle == null) {
			mInfoBundle = new Bundle();
			
			mInfoBundle.putString("label", getApplication().getResources().getText(getApplicationInfo().labelRes).toString());
			mInfoBundle.putString("pkg", getPackgeName());
			mInfoBundle.putString("apkPath", mApkPath);
			mInfoBundle.putString("dataPath", appDataPath);
			mInfoBundle.putString("prefsPath", getAppPrefsPath());
			mInfoBundle.putString("filesPath", getAppFilesPath());
		}
		
		return mInfoBundle;
	}

	@Override
	public void onActivityResume(Activity activity) {
	}

	@Override
	public void onActivityPause(Activity activity) {
	}
	
	public void deleteApplicationCacheFiles() {
	}
	
	/**
	 * 清除程序全部数据
	 */
	public void clearApplicationData() {
		FileUtils.removeDir(getAppDataPath());
		FileUtils.removeDir(getPrivateAppDataPath());
	}

	public void clearApplicationUserData() {
	}
	
	/**
	 * 外部调用
	 * 
	 * @param context
	 * @param apkPath
	 */
	public static void clearAppData(Context context, String apkPath) {
		try {
			PackageInfo info = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
			
			File root = new File(PUBLIC_PATH_APPS, info.packageName);
			File[] files = root.listFiles();
			for(File file : files) {
				if(file.getName().equals(DIR_APP_LIB)) //保留 so 和 dex
					continue;
				
				if(file.isDirectory())
					FileUtils.removeDir(file);
				else
					file.delete();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<ApkInfo> CREATOR = new Parcelable.Creator<ApkInfo>() {
		public ApkInfo createFromParcel(Parcel in) {
			return new ApkInfo(in);
		}

		public ApkInfo[] newArray(int size) {
			return new ApkInfo[size];
		}
	};

}
