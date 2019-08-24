package com.apkrunner.core;


import com.apkrunner.ApkRunner;
import com.apkrunner.services.QILocationManager;
import com.apkrunner.services.QIWifiManager;
import com.apkrunner.services.QTelephonyManager;
import com.apkrunner.utils.Logger;

import android.content.Context;
import android.content.ContextWrapper;


/**
 * 
 * @author Yichou 2013-12-4
 *
 */
public class QContext extends ContextWrapper {
	static final Logger log = Logger.create(QLog.debug_ctx, "ctx");

	private QTelephonyManager telephonyManager;
	
	public QContext(Context base) {
		super(base);
		
		Frameworks.YContextImpl.setBasePackgeName(base, ApkRunner.getShellContext());
	}
	
	@Override
	public Object getSystemService(String name) {
		Object object = super.getSystemService(name);
		
		log.i("getSystemService " + name);
		
		if (Context.CONNECTIVITY_SERVICE.equals(name)) {
			
		} else if (LOCATION_SERVICE.equals(name)) {
			QILocationManager.makeProxy( object);
		} else if (TELEPHONY_SERVICE.equals(name)) {
			if (ApkRunner.ADS_MOD) {
				if(telephonyManager == null)
					telephonyManager = new QTelephonyManager(getApplicationContext());
				
				return telephonyManager;
			}
		} else if (WIFI_SERVICE.equals(name)) {
			if (ApkRunner.ADS_MOD) {
				QIWifiManager.makeProxy(object);
			}
		}
		
		return object;
	}
}
