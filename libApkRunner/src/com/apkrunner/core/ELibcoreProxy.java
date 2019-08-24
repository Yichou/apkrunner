package com.apkrunner.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.Environment;
import libcore.io.Libcore;

public class ELibcoreProxy extends BaseProxy {

	protected ELibcoreProxy(Object src) {
		super(src);
	}
	
	private static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		if("getuid".equals(name)) {
			return 1025;
		} else if ("stat".equals(name)) {
			Object ret = super.onInvoke(object, method, name, args);
			ReflectHelper.setField(ret, "st_uid", 1025);
			return ret;
		}  else if ("chmod".endsWith(name) || "chown".equals(name)) {
			if(((String)args[0]).startsWith(SD_PATH)) { //屏蔽SD卡上的操作
				return 0;
			}
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	public static void proxy2() {
		try {
			Class<?> clazz = Class.forName("libcore.io.Libcore");
			//没有dex路径校验就没有 Libcore
			Field field = clazz.getDeclaredField("os");
			field.setAccessible(true);
			Object real = field.get(null);
			if(real instanceof QLibcore) {
				return;
			}
			
			Class<?> i = Class.forName("libcore.io.Os");
			field.set(null, newProxyInstance(i.getClassLoader(), new ELibcoreProxy(real), i));
			
//			System.out.println("libcore proxyed!");

			Libcore.os.getuid(); //先拿UID
		} catch (Exception e) {
			//do not show!
			e.printStackTrace();
		}
	}
}
