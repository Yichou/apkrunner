package com.apkrunner.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.apkrunner.core.BaseProxy;
import com.apkrunner.core.Frameworks;
import com.apkrunner.core.QLog;


/**
 * 
 * WIFI 管理器
 * 
 * @author Yichou 2014-1-11
 *
 */
public final class QIWifiManager extends BaseProxy {
	private static final String TAG = "wifi";
	
	private static final String fld_mService = "mService";
	private static final String fld_mMacAddress = "mMacAddress";
	private static final String mtd_getConnectionInfo = "getConnectionInfo";

	private static String mac = "EA-E3-33-69-88-24";
	
	
	public QIWifiManager(Object src) {
		super(src);
	}
	
	public static void setMac(String mac) {
		QIWifiManager.mac = mac;
	}
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		QLog.i(TAG, "onInvoke " + name);

		if(match(name, mtd_getConnectionInfo)) {
			Object ret = super.onInvoke(object, method, name, args);
			//进行替换 MAC 地址
			//00-11-22-13-21-DC
			try {
				Field field = ret.getClass().getDeclaredField(Frameworks.decode(fld_mMacAddress));
				field.setAccessible(true);
				field.set(ret, mac);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return ret;
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	public static void makeProxy(Object manager) {
		try {
			Field field = manager.getClass().getDeclaredField(Frameworks.decode(fld_mService));
			field.setAccessible(true);
			
			Object real = field.get(manager);
			if (real instanceof QIWifiManager) {
				return;
			}
			
			Object proxy = newProxyInstance(real, new QIWifiManager(real));
			field.set(manager, proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
