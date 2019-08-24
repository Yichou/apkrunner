package com.apkrunner;

import android.os.Bundle;


/**
 * 
 * @author Yichou 2014-1-3 14:08:51
 *
 */
public abstract class ApkRunCallback {
	public void onSelectProcess(int procIndex) {
	}
	
	public void onProcessAttached(int pid, int procIndex) {
	}
	
	public void onApplicationCreate(Bundle apkInfo) {
	}
	
	public void onLaunchFinish(Bundle apkInfo) {
	}
}
