package com.apkrunner.services;

import android.content.Context;
import android.location.ILocationManager;
import android.location.LocationManager;

public class QLocationManager extends LocationManager {

	public QLocationManager(Context context, ILocationManager service) {
		super(context, service);
	}

	
}
