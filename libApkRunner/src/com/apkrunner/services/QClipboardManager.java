package com.apkrunner.services;

import java.lang.reflect.Method;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.core.BaseProxy;
import com.apkrunner.core.Frameworks;

import android.os.Build.VERSION;


/**
 * 剪切板管理器代理
 * 
 * @author YYichou 2014-3-4
 *
 */
public class QClipboardManager extends BaseProxy {

	protected QClipboardManager(Object src) {
		super(src);
	}

	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		if(match(name, "getPrimaryClip")) {
			ApkInfo apkInfo = ApkRunner.currentRunning();
			if(apkInfo!=null ) {
				if(apkInfo.getPackgeName().equals(args[0])) {
					args[0] = apkInfo.getShellPackgeName();
				}
			}
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	public static void proxy() {
		if(Frameworks.YContextImpl.isNewMod() || VERSION.SDK_INT < 11)
			return;
		
		Object old = Frameworks.YClipboardManager.getService();
		if(old != null && !(old instanceof QClipboardManager)) {
			Frameworks.YClipboardManager.setService(newProxyInstance(old, 
					new QClipboardManager(old)));
		}
	}
}
