package com.apkrunner.core;

import com.apkrunner.ApkRunner;
import com.apkrunner.core.Frameworks.YActivityThread;
import com.apkrunner.core.Frameworks.YServiceManager;
import com.apkrunner.services.QClipboardManager;
import com.apkrunner.services.QIPhoneSubInfo;

import android.os.ServiceManager;

/**
 * 
 * @author Yichou 2014-3-5
 *
 */
public final class Core {

	public static void initCore() {
		if (ApkRunner.ADS_MOD) {
			YServiceManager.addServiceToCache("iphonesubinfo", 
					QIPhoneSubInfo.getProxy(ServiceManager.getService("iphonesubinfo")));
		}
		
//		QLibcore.proxy();
		ELibcoreProxy.proxy2();
		
		QAppOpsService.proxy();
		
		QActivityManager.proxy();
		QPackgeManager.proxy();
		QActivityThread.proxy();
		QNotifycationManager.proxy();
		QInstrumentation.proxy();
		QMointerClassLoader.proxy();
		QClipboardManager.proxy();
		YActivityThread.proxyInitApplication();

		Frameworks.disableDeathOnNetwork();
	}
}
