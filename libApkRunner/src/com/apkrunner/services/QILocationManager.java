package com.apkrunner.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.core.BaseProxy;
import com.apkrunner.core.Frameworks;
import com.apkrunner.utils.Logger;


/**
 * 位置管理
 * 
 * @author Yichou 2013-12-3
 * 
 */
public class QILocationManager extends BaseProxy {
	static final String TAG = "Location";
	static final Logger log = Logger.create(TAG);
	
	private static final String mtd_getLastLocation = "getLastLocation";
	private static final String mtd_requestLocationUpdates = "requestLocationUpdates";
	private static final String mtd_removeUpdates = "removeUpdates";
	
	private static final String fld_mService = "mService";
	
	
	private QILocationManager(Object src) {
		super(src);
	}
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		log.i(TAG, "invoke " + name);
		
		ApkInfo apkInfo = ApkRunner.currentRunning();
		if (apkInfo != null) {
			if (match(name, mtd_getLastLocation)) {
				// Location getLastLocation(in LocationRequest request, String
				// packageName);
				args[1] = apkInfo.getShellPackgeName();
			} else if (match(name, mtd_requestLocationUpdates)) {
				// android.location.LocationRequest request,
				// android.location.ILocationListener listener,
				// android.app.PendingIntent intent, java.lang.String packageName)
				if(args.length > 3)
					args[3] = apkInfo.getShellPackgeName();
			} else if (match(name, mtd_removeUpdates)) {
				args[2] = apkInfo.getShellPackgeName();
			}
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	public static void makeProxy(Object manager) {
		try {
			Field field = manager.getClass().getDeclaredField(Frameworks.decode(fld_mService));
			field.setAccessible(true);
			
			Object real = field.get(manager);
			if (real instanceof QILocationManager) {
				return;
			}
			
			Object proxy = Proxy.newProxyInstance(real.getClass().getClassLoader(), 
					real.getClass().getInterfaces(),
					new QILocationManager(real));
			field.set(manager, proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
