package com.apkrunner.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;



/**
 * 代理基类
 * 
 * @author Yichou 2014-1-11
 *
 */
public abstract class BaseProxy implements InvocationHandler {
	private Object src;
	
	
	protected BaseProxy(Object src) {
		this.src = src;
	}
	
	public Object getReal() {
		return src;
	}

	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		method.setAccessible(true);
		String name = method.getName();
		
		/**
		 * 这里抛出什么异常一定要非常明确，因为通过反射去调用方法，方法本身产生的异常并不是直接抛出， 而是被
		 * InvocationTargetException 包装过，而我们代理之后肯定要抛出他本来的异常，因为原代码在 catch
		 * 里面抓取的可能只是这个方法本来抛出的异常，而经过代理之后抛出的异常是 InvocationTargetException
		 * 这样就会导致他预想的 catch 不起作用，导致程序崩溃
		 * 
		 */
		Object ret = null;
		try {
			ret = onInvoke(src, method, name, args);
		} catch (IllegalAccessException e1) {
			throw e1;
		} catch (IllegalArgumentException e1) {
			throw e1;
		} catch (InvocationTargetException e1) {
			throw e1.getTargetException(); //抛出他本来的异常
		}
		
		return ret;
	}
	
	/**
	 * 这里抛出什么异常一定要非常明确，因为通过反射去调用方法，方法本身产生的异常并不是直接抛出，
	 * 而是被 InvocationTargetException 包装过，而我们代理之后肯定要抛出他本来的异常，因为原代码在
	 *  catch 里面抓取的可能只是这个方法本来抛出的异常，而经过代理之后抛出的异常是 InvocationTargetException 
	 *  这样就会导致他预想的 catch 不起作用，导致程序崩溃 
	 * 
	 * 
	 * @param object 调用这个方法 的 对象
	 * @param method 方法
	 * @param name 方法名
	 * @param args 传入方法的参数列表
	 * 
	 * @return 方法调用结果
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws Throwable 
	 */
	protected Object onInvoke(Object object, Method method, String name, Object[] args) 
			throws /*IllegalAccessException, IllegalArgumentException, InvocationTargetException, */Throwable {
		return method.invoke(object, args);
	}
	
	protected static boolean match(String src, String dst) {
		return src.equals(Frameworks.decode(dst));
	}
	
	/**
	 * 创建一个动态代理实例
	 * 
	 * @param cl 加载 interfaces 类的类加载器
	 * @param proxy 方法调用处理器
	 * @param interfaces 要代理的接口，支持多个
	 * 
	 * @return 动态代理实例
	 */
	public static Object newProxyInstance(ClassLoader cl, BaseProxy proxy, Class<?>... interfaces) {
		return Proxy.newProxyInstance(cl, interfaces, proxy);
	}

	/**
	 * 
	 * @param src
	 * @param proxy
	 * @return
	 */
	public static Object newProxyInstance(Object src, BaseProxy proxy) {
		if(src == null)
			return null;
		return newProxyInstance(src.getClass().getClassLoader(), proxy, src.getClass().getInterfaces());
	}
}
