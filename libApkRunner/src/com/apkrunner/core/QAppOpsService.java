package com.apkrunner.core;

import java.lang.reflect.Method;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.ServiceManager;

import com.android.internal.app.IAppOpsService;

public class QAppOpsService {
	private static Object iin = null;
	
	
	/**
	 * 如何代理 ServiceManager 中的系统服务
	 * 
	 *  原理：代理 IBinder queryLocalInterface 接口，返回代理过的接口
	 *  
	 *  1.取得一个接口，ServiceManager.getService 获取 IBinder xxx.Stub.asInterface 实例化接口，动态代理该接口
	 *  2.代理掉 IBinder queryLocalInterface 方法，返回第一步创建的代理对象
	 *  3.OK
	 *  
	 */
	public static void proxy() {
		if (VERSION.SDK_INT < VERSION_CODES.KITKAT)
			return;

		{
			IBinder b = ServiceManager.getService(Context.APP_OPS_SERVICE);

			try {
				Class<?> cls = Class.forName("com.android.internal.app.IAppOpsService");
				
				iin = BaseProxy.newProxyInstance(cls.getClassLoader(), new BaseProxy(IAppOpsService.Stub.asInterface(b)) {
					
					@Override
					protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
						if("checkAudioOperation".equals(name)) {
							return AppOpsManager.MODE_ALLOWED;
						}
						
						return super.onInvoke(object, method, name, args);
					}
				}, cls);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Class<?> cls = Class.forName("android.os.IBinder");
				
				Object p = BaseProxy.newProxyInstance(cls.getClassLoader(), new BaseProxy(b) {

					@Override
					protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
						if("queryLocalInterface".equals(name)) {
							return iin;
						}
						
						return super.onInvoke(object, method, name, args);
					}
				}, cls);

				Frameworks.YServiceManager.addServiceToCache("appops", p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
