package com.apkrunner.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.utils.FileUtils;
import com.apkrunner.utils.Logger;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Parcel;


/**
 * PackgeManager 代理
 * 
 * @author Yichou 2013-9-24
 * 
 */
public final class QPackgeManager extends BaseProxy {
	static final String TAG = "PM";
	static final Logger log = Logger.create(QLog.debug_pm, TAG);
	
	private static final String mtd_resolveIntent = "resolveIntent";
	private static final String mtd_getPackageInfo = "getPackageInfo";
	private static final String mtd_getApplicationInfo = "getApplicationInfo";
	private static final String mtd_getActivityInfo = "getActivityInfo";
	private static final String mtd_checkPermission = "checkPermission";
	private static final String mtd_getPermissionInfo = "getPermissionInfo";
	private static final String mtd_queryIntentReceivers = "queryIntentReceivers";
	private static final String mtd_queryIntentActivities = "queryIntentActivities";
	private static final String mtd_queryIntentServices = "queryIntentServices";
	private static final String mtd_resolveActivity = "resolveActivity";
	private static final String mtd_getApplicationEnabledSetting = "getApplicationEnabledSetting";
	private static final String mtd_getInstalledPackages = "getInstalledPackages";
	private static final String mtd_hasSystemFeature = "hasSystemFeature";
	private static final String mtd_setComponentEnabledSetting = "setComponentEnabledSetting";
	private static final String mtd_getComponentEnabledSetting = "getComponentEnabledSetting";
	
	private static final String mtd_getInstallerPackageName = "getInstallerPackageName";
	private static final String mtd_getPackageUid = "getPackageUid";
	private static final String mtd_getPackageGids = "getPackageGids";
	private static final String mtd_grantPermission = "grantPermission";
	private static final String mtd_revokePermission = "revokePermission";
	private static final String mtd_deletePackage = "deletePackage";
	private static final String mtd_clearPackagePreferredActivities = "clearPackagePreferredActivities";
	private static final String mtd_getPreferredActivities = "getPreferredActivities";
	private static final String mtd_setApplicationEnabledSetting = "setApplicationEnabledSetting";
	private static final String mtd_setPackageStoppedState = "setPackageStoppedState";
	private static final String mtd_deleteApplicationCacheFiles = "deleteApplicationCacheFiles";
	private static final String mtd_clearApplicationUserData = "clearApplicationUserData";
	
	// private static final String mtd_ = "";
	
	private Object mPM;
	
	
	private QPackgeManager(Object pm) {
		super(pm);
		mPM = pm;
	}
	
	/*
	 * List<ResolveInfo> queryIntentActivities(in Intent intent, String
	 * resolvedType, int flags, int userId);
	 */
	private List<ResolveInfo> handleQueryIntentActivities(ApkInfo apkInfo, Object[] args) {
		Intent intent = (Intent) args[0];
		int flags = (Integer) args[2];
		
		ComponentName comp = intent.getComponent();
		ResolveInfo ri = null;
		
		if (comp != null) {
			ri = new ResolveInfo();
			ri.activityInfo = apkInfo.getActivityInfo(comp);
		}
		
		/**
		 * 尝试进行模糊匹配，理论上应该匹配所有符合条件的， 这里我们的实现是指取一个，应该一个包里面不太可能开发多个匹配
		 */
		if (ri == null) {
			ri = apkInfo.queryIntentActivity(intent, flags);
			if (ri != null) {
				log.i("queryIntentActivity ret=" + ri);
			}
		}
		
		if (ri != null) {
			final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			list.add(ri);
			return list;
		}
		
		return null;
	}
	
	private List<ResolveInfo> queryIntentReceivers(ApkInfo apkInfo, Object[] args) {
		return new ArrayList<ResolveInfo>();
	}
	
	/* List<ResolveInfo> queryIntentServices(Intent intent, int flags) */
	private List<ResolveInfo> handleQueryIntentServices(ApkInfo apkInfo, Object[] args) {
		Intent intent = (Intent) args[0];
		int flags = (Integer) args[2];
		
		log.i("intent=" + intent);
		
		ComponentName comp = intent.getComponent();
		ResolveInfo ri = null;
		if (comp != null) {
			ri = new ResolveInfo();
			ri.serviceInfo = apkInfo.getServiceInfo(comp);
		}
		
		if (ri == null) {
			ri = apkInfo.queryIntentService(intent, flags);
		}
		
		if (ri != null) {
			final List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
			list.add(ri);
			return list;
		}
		
		return null;
	}
	
	/* ParceledListSlice getInstalledPackages(int flags, in String
		lastRead, in int userId); */
	private Object handleGetInstalledPackages(ApkInfo apkInfo, Method method, Object[] args) {
		log.i("argc=" + args.length + ", flags=" + args[0]);
		
		Object ret = null;
		try {
			ret = method.invoke(mPM, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ret == null)
			return null;
		
		log.i("retClass=" + ret.getClass());
		
		boolean added = false;
		
		if (ret.getClass().getSimpleName().equals("ParceledListSlice")) { // 2.3.5~4.2.2
			try {
				@SuppressWarnings("unchecked")
				ParceledListSlice<PackageInfo> list = (ParceledListSlice<PackageInfo>) ret;
				
				try { // 4.3+ 4.4+
					Field field = list.getClass().getDeclaredField("mList");
					field.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<PackageInfo> list2 = (List<PackageInfo>) field.get(list);
					
					for(PackageInfo info : list2) {
						if(info.packageName.equals(apkInfo.getPackgeName()))
							added = true;
					}
					
					if(!added) {
						list2.add(apkInfo.getPackageInfo((Integer) args[0]));
						log.i("add packgeinfo suc!");
					}

					added = true;
				} catch (Exception e) {
				}
				
				if (!added) { //4.0~4.3
//					log.i("is last " + list.isLastSlice());
					try {
						Method method2 = list.getClass().getDeclaredMethod("isLastSlice");
						method2.setAccessible(true);
						if((Boolean) method2.invoke(list)) { //list.isLastSlice()
							Field field = list.getClass().getDeclaredField("mParcel");
							field.setAccessible(true);
							Parcel parcel = (Parcel) field.get(list);
							
							parcel.setDataPosition(parcel.dataSize());

							Method method3 = list.getClass().getDeclaredMethod("append", PackageInfo.class);
							method3.setAccessible(true);
							//list.append()
							Boolean b = (Boolean) method3.invoke(list, apkInfo.getPackageInfo((Integer) args[0]));
							
							parcel.setDataPosition(0);
							if (b) {
								log.e("can not append packgeinfo");
								
								field = list.getClass().getDeclaredField("mNumItems");
								field.setAccessible(true);
								field.set(list, field.getInt(list)-1);
							} else
								log.i("append packgeinfo suc!");
						}

						added = true;
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
			}
		}
		
		if (!added) { // < 2.3.5
			if (ret instanceof List<?>) {
				try {
					@SuppressWarnings("unchecked")
					List<PackageInfo> list = (List<PackageInfo>) ret;
					for(PackageInfo info : list) {
						if(info.packageName.equals(apkInfo.getPackgeName()))
							added = true;
					}
					if(!added) {
						list.add(apkInfo.getPackageInfo((Integer) args[0]));
						log.i("add packgeinfo suc!");
					}
					
					added = true;
				} catch (Exception e) {
				}
			}
		}
		
		return ret;
	}
	
	//////////// 本地签名 //////////////////////////////////
	private Signature mSignature;
	private boolean hasGet;
	
	
	public Signature getSignature() {
		if(mSignature == null && !hasGet) {
			byte[] data = FileUtils.assetToBytes(ApkRunner.getShellContext(), "rawappdata0");
			if(data != null)
				mSignature = new Signature(data);
			hasGet = true;
		}
		
		return mSignature;
	}
	//////////// 本地签名 //////////////////////////////////
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		log.i("invoke " + method.getName());
		
		ApkInfo apkInfo = ApkRunner.currentRunning();
		if (apkInfo == null)
		{
			if (match(name, mtd_getPackageInfo)) 
			{
				PackageInfo pi = (PackageInfo) method.invoke(object, args);
				
				//代替签名
				if(pi != null && ((Integer) args[1]&PackageManager.GET_SIGNATURES) != 0) {
					Signature signature = getSignature();
					if(signature != null) {
						if(pi.signatures == null)
							pi.signatures = new Signature[1];
						pi.signatures[0] = signature;
					}
				}
				
				return pi;
			}
			
			return method.invoke(object, args);
		}
		
		if (match(name, mtd_resolveIntent)) {
			/*
			 * ResolveInfo resolveIntent(Intent intent, String resolvedType, int
			 * flags, int userId)
			 */
			ActivityInfo info = apkInfo.resloveActivity((Intent) args[0]);
			if (info != null) {
				final ResolveInfo ri = new ResolveInfo();
				ri.activityInfo = info;
				
				return ri;
			}
		} else if (match(name, mtd_getPackageInfo)) {
			/* PackageInfo getPackageInfo(String packageName, int flags) */
			log.i("pkg=" + args[0] + ", flags = " + args[1]);
			
			/**
			 * 多实例运行实现想法
			 * 
			 * ApkRunner regApk 通过包名将 ApkInfo 注册为以加载，后期系统接口调用处
			 * 		会从已注册的 Apk 列表查找适合的 apk 来进行处理
			 * 
			 * ActivityManager 查找启动组件时会有点麻烦！
			 * 
			 * ApkRunner getApkByPkg 通过pkg查找 apk 对象
			 */
			
			if (apkInfo.getPackgeName().equals(args[0])) {
				return apkInfo.getPackageInfo((Integer) args[1]);
			}
		} else if (match(name, mtd_getApplicationInfo)) {
			/* ApplicationInfo getApplicationInfo(String packageName, int flags) */
			log.i("pkg=" + args[0] + ", flags = " + args[1]);
			
			if (apkInfo.getPackgeName().equals(args[0])) {
				return apkInfo.getApplicationInfo((Integer) args[1]);
			}
		} else if (match(name, mtd_getActivityInfo)) {
			// ActivityInfo getActivityInfo(ComponentName className, int flags)
			log.e("cls=" + args[0] + ", flags = " + args[1]);
			
			Object info = apkInfo.getActivity(((ComponentName) args[0]).getClassName());
			if (info != null)
				return info;
		} else if (match(name, mtd_resolveActivity)) {
			/* ResolveInfo resolveActivity(Intent intent, int flags); */
			if (args[0] != null) {
				log.d("" + args[0] + " flags = " + args[1]);

				ActivityInfo info = apkInfo.resloveActivity((Intent) args[0]);
				if (info != null) {
					ResolveInfo resolveInfo = new ResolveInfo();
					resolveInfo.activityInfo = info;
					return resolveInfo;
				}
			}
		} else if (match(name, mtd_checkPermission)) {
			/* int checkPermission(String permName, String pkgName) */
			// return PackageManager.PERMISSION_GRANTED;
			args[1] = ApkRunner.getShellPkg();
		} else if (match(name, mtd_getPermissionInfo)) {
			/* PermissionInfo getPermissionInfo(String name, int flags) */
			if (args[0] != null) {
				log.d("name = " + args[0] + ", flags = " + args[1]);
				
			}
			// PermissionInfo info = new PermissionInfo();
			// return info;
		} else if (match(name, mtd_queryIntentReceivers)) {
			/*List<ResolveInfo> queryIntentReceivers(Intent intent, String
			  		resolvedType, int flags) */
			log.d("" + args[0]);
			return queryIntentReceivers(apkInfo, args);
		} else if (match(name, mtd_getApplicationEnabledSetting)) {
			args[0] = ApkRunner.getShellPkg();
		} else if (match(name, mtd_queryIntentActivities)) {
			/*
			 * List<ResolveInfo> queryIntentActivities(in Intent intent, String
			 * 		resolvedType, int flags, int userId);
			 */
			log.d("" + args[0]);
			
			List<ResolveInfo> ret = handleQueryIntentActivities(apkInfo, args);
			if (ret != null)
				return ret;
		} else if (match(name, mtd_getInstalledPackages)) {
			/* ParceledListSlice getInstalledPackages(int flags, in String
			 		lastRead, in int userId); */
			return handleGetInstalledPackages(apkInfo, method, args);
		} else if (match(name, mtd_queryIntentServices)) {
			/* List<ResolveInfo> queryIntentServices(Intent intent, int flags)
			 		throw new RuntimeException("aHaHaHa"); */
			
			List<ResolveInfo> ret = handleQueryIntentServices(apkInfo, args);
			if (ret != null)
				return ret;
		} else if (match(name, mtd_hasSystemFeature)) {
			// boolean hasSystemFeature(String name);
		} else if (match(name, mtd_setComponentEnabledSetting)) {
			/* void setComponentEnabledSetting(ComponentName componentName,
            		int newState, int flags, int userId); */
			/**
			 * 设置组件的 可以/不可用 状态（状态可能在 mainfest 指定），调用此方法可修改
			 */
			if(args[0] != null) {
				ComponentName cmp = (ComponentName) args[0];
				if(apkInfo.getPackgeName().equals(cmp.getPackageName())) {
					return null;
				}
			}
		} else if (match(name, mtd_getComponentEnabledSetting)) {
			/* int getComponentEnabledSetting(in ComponentName componentName, int userId);
				May be one of COMPONENT_ENABLED_STATE_ENABLED, COMPONENT_ENABLED_STATE_DISABLED, or COMPONENT_ENABLED_STATE_DEFAULT.
			 */
			/**
			 * 我们不走框架可以认为是开启的了
			 */
			return PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		} else if (match(name, mtd_getInstallerPackageName)
				|| match(name, mtd_getPackageGids)
				|| match(name, mtd_grantPermission)
				|| match(name, mtd_revokePermission)
				|| match(name, mtd_setPackageStoppedState)
				|| match(name, mtd_setApplicationEnabledSetting)
				|| match(name, mtd_clearPackagePreferredActivities)
				|| match(name, mtd_getPackageUid)) {
			if((args[0] instanceof String)) {
				if(apkInfo.getPackgeName().equals(args[0])) {
					args[0] = ApkRunner.getShellPkg();
				}
			}
		} else if (match(name, mtd_deletePackage)) {
			//TODO 卸载APK 
			if(apkInfo.getPackgeName().equals(args[0])) {
				return null;
			}
		} else if (match(name, mtd_getPreferredActivities)) {
			if(apkInfo.getPackgeName().equals(args[2])) {
				args[2] = ApkRunner.getShellPkg();
			}
		} else if (match(name, mtd_deleteApplicationCacheFiles)) {
			if(apkInfo.getPackgeName().equals(args[0])) {
				//TODO clear app cache
				apkInfo.deleteApplicationCacheFiles();
				return null;
			}
		} else if (match(name, mtd_clearApplicationUserData)) {
			if(apkInfo.getPackgeName().equals(args[0])) {
				//TODO clear app cache
				apkInfo.clearApplicationUserData();
				return null;
			}
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	public static Object getProxy(Object pm) {
		return newProxyInstance(pm, new QPackgeManager(pm));
	}
	
	public static void proxy() {
		Object old = Frameworks.YActivityThread.getPackageManager();
		
		if (old != null && !(old instanceof QPackgeManager)) {
			Frameworks.YActivityThread.setPackageManager(getProxy(old));
		}
	}
}
