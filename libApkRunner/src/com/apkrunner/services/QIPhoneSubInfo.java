package com.apkrunner.services;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.os.SystemProperties;

import com.android.internal.telephony.TelephonyProperties;
import com.apkrunner.core.BaseProxy;
import com.apkrunner.core.QLog;
import com.apkrunner.utils.Logger;


/**
 * 手机信息代理
 * 
 * @author Yichou 2014-1-10
 *
 */
public class QIPhoneSubInfo extends BaseProxy {
	public static final Logger log = Logger.create(QLog.debug_phone, "phone");
	
	public static final String mtd_getSubscriberId = "getSubscriberId";
	public static final String mtd_getDeviceId = "getDeviceId";
	public static final String mtd_queryLocalInterface = "queryLocalInterface";
	public static final String mtd_getIccSerialNumber = "getIccSerialNumber";
	public static final String mtd_getLine1Number = "getLine1Number";
	
	
	private QIPhoneSubInfo(Object src) {
		super(src);
	}
	
	private static String imsi, imei, sim, number = null, op, opName;
	
	
	public static void setImsi(String imsi) {
		QIPhoneSubInfo.imsi = imsi;
		
		op = imsi.substring(0, 5);
		
		//模拟 getSimOperator 返回 IMSI 前5位
		SystemProperties.set(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, 
				op);
		
		int i = imsi.charAt(5);
		
		if(i == 1) {
			opName = "CHN-UNICOM";
		} else if (i == 0 || i== 2 || i == 7) {
			opName = "CHN-MOBIE";
		} else if (i == 3) {
			opName = "CHN-TELECOM";
		} else {
			opName = "UNKNOW";
		}

		SystemProperties.set(TelephonyProperties.PROPERTY_OPERATOR_ALPHA, 
				opName);
	}
	
	public static void setSystemProperties(String key, String val) {
		SystemProperties.set(key, val);
	}
	
	public static void setImei(String imei) {
		QIPhoneSubInfo.imei = imei;
	}
	
	public static void setSim(String sim) {
		QIPhoneSubInfo.sim = sim;
	}
	
	public static void setNumber(String number) {
		QIPhoneSubInfo.number = number;
	}
	
	public static String getOp() {
		return op;
	}
	
	public static String getOpName() {
		return opName;
	}
	
	@Override
	protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
		log.i("invoke " + name);
		
		if(match(name, mtd_getSubscriberId)) {
			return imsi;
		} else if (match(name, mtd_getDeviceId)) {
			return imei;
		} else if (match(name, mtd_getIccSerialNumber)) {
			return sim;
		} else if (match(name, mtd_getLine1Number)) {
			return number;
		}
		
		return super.onInvoke(object, method, name, args);
	}
	
	/**
	 * 代理 IPhoneSubInfo.Stub
	 * 
	 * @author Yichou 2014-1-12
	 *
	 */
//	private static final class StubProxy extends BaseProxy {
//		private Object cache;
//		private IPhoneSubInfo srcsrc;
//		
//		private StubProxy(Object src) {
//			super(src);
//			
//			srcsrc = IPhoneSubInfo.Stub.asInterface((IBinder) src);
//			cache = newProxyInstance(srcsrc, new QIPhoneSubInfo(srcsrc));
//		}
//		
//		@Override
//		protected Object onInvoke(Object object, Method method, String name, Object[] args) throws Throwable {
//			if(match(name, mtd_queryLocalInterface)) {
//				return cache;
//			}
//			
//			return super.onInvoke(object, method, name, args);
//		}
//	}
	
	public static Object getProxy(Object src) {
		return null; //newProxyInstance(src, new StubProxy(src));
	}
}
