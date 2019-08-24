package com.android.commands.input;

import java.lang.reflect.Method;

import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputImpl23 extends BaseInput {

//	public boolean More ...injectKeyEvent(android.view.KeyEvent ev, boolean sync) throws android.os.RemoteException;
//	public boolean More ...injectPointerEvent(android.view.MotionEvent ev, boolean sync) throws android.os.RemoteException;
	
	private Method method0, method1;
	static final IWindowManager service = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
	
	@Override
	protected void injectKeyEvent(KeyEvent event) {

		/*(IWindowManager.Stub
                .asInterface(ServiceManager.getService("window")))
                .injectKeyEvent(down, true);*/
		
		if(method0 == null) {
			try {
				Class<?> clazz = Class.forName("android.view.IWindowManager");
				Method method = clazz.getDeclaredMethod("injectKeyEvent",
						KeyEvent.class, boolean.class);
				method.setAccessible(true);
				method0 = method;
			} catch (Exception e) {
			}
		}
		
		if(method0 != null) {
			try {
				method0.invoke(service, 
						event, true);
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void injectMotionEvent(MotionEvent event) {
		if(method1 == null) {
			try {
				Class<?> clazz = Class.forName("android.view.IWindowManager");
				Method method = clazz.getDeclaredMethod("injectPointerEvent",
						android.view.MotionEvent.class, boolean.class);
				method.setAccessible(true);
				method1 = method;
			} catch (Exception e) {
			}
		}
		
		if(method1 != null) {
			try {
				method1.invoke(service, 
						event, true);
			} catch (Exception e) {
			}
		}
	}

}
