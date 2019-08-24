package com.apkrunner.services;

import java.lang.reflect.Method;

import com.apkrunner.core.BaseProxy;
import com.apkrunner.core.Frameworks;


/**
 * AudioManager 代理
 * 
 * @author YYichou 2014-3-6
 *
 */
public final class QAudioManager extends BaseProxy {

	protected QAudioManager(Object src) {
		super(src);
	}
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		// TODO Auto-generated method stub
		return super.onInvoke(object, method, name, args);
	}
	
	public static void proxy() {
		if(Frameworks.YContextImpl.isNewMod())
			return;
		
		Object old = Frameworks.YAudioManager.getService();
		if(old != null && !(old instanceof QClipboardManager)) {
			Frameworks.YAudioManager.setService(newProxyInstance(old, 
					new QAudioManager(old)));
		}
	}
}
