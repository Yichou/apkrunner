package com.apkrunner.core;

import java.lang.reflect.Field;

/**
 * 反射辅助类
 * 
 * @author YYichou 2014-3-30
 *
 */
public final class ReflectHelper {

	/**
	 * 设置字段
	 * 
	 * @param src
	 * @param srcClass 字段定义的类
	 * @param name
	 * @param data
	 * @return 设置前的值
	 * 
	 * @throws Exception
	 */
	public static Object setField(Object src, Class<?> srcClass, String name, Object data) throws Exception {
		Field field = srcClass.getDeclaredField(name);
		field.setAccessible(true);
		Object ret = field.get(src);
		field.set(src, data);
		return ret;
	}
	
	public static Object setField(Object src, String srcClassName, String name, Object data) throws Exception {
		return setField(src, Class.forName(srcClassName), name, data);
	}
	
	public static Object setField(Object src, String name, Object data) throws Exception {
		return setField(src, src.getClass(), name, data);
	}

	/**
	 * 获取字段
	 * 
	 * @param src
	 * @param srcClass 字段定义类
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static Object getField(Object src, Class<?> srcClass, String name) throws Exception {
		Field field = srcClass.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(src);
	}
	
	public static Object getField(Object src, String srcClassName, String name) throws Exception {
		return getField(src, Class.forName(srcClassName), name);
	}
	
	public static Object getField(Object src, String name) throws Exception {
		return getField(src, src.getClass(), name);
	}
}
