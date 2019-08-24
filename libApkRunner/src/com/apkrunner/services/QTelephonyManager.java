package com.apkrunner.services;


import android.content.Context;
import android.telephony.TelephonyManager;

public class QTelephonyManager extends TelephonyManager {

	public QTelephonyManager(Context context) {
		super(context);
	}
	
	@Override
	public String getSimSerialNumber() {
		return super.getSimSerialNumber();
	}
	
	@Override
	public String getSubscriberId() {
		return super.getSubscriberId();
	}
	
	@Override
	public String getNetworkCountryIso() {
		return "cn";
	}
	
	@Override
	public String getNetworkOperator() {
		return QIPhoneSubInfo.getOp();
	}

	/**
	 * CHN-UNICOM
	 */
	@Override
	public String getNetworkOperatorName() {
		return QIPhoneSubInfo.getOpName();
	}

	@Override
	public int getNetworkType() {
		int ret = super.getNetworkType();
//		QIPhoneSubInfo.log.i("getNetworkType() " + ret);
		return ret;
	}
	
	/**
	 * SIM 卡状态，5为正常状态
	 */
	@Override
	public int getSimState() {
//		int ret = super.getSimState();
//		QIPhoneSubInfo.log.i("getSimState() " + ret);
		
		return TelephonyManager.SIM_STATE_READY;
	}
	
	@Override
	public String getSimOperator() {
		return QIPhoneSubInfo.getOp();
	}
	
	@Override
	public String getSimOperatorName() {
		return QIPhoneSubInfo.getOpName();
	}
	
	@Override
	public String getSimCountryIso() {
		return "cn";
	}
}
