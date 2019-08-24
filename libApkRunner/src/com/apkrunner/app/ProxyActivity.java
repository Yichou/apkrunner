package com.apkrunner.app;

import android.app.Activity;
import android.os.Bundle;


/**
 * 
 * @author Yichou 2013-9-26
 * 
 */
public class ProxyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
//		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		System.out.println("you will never see me!");
	}
}
