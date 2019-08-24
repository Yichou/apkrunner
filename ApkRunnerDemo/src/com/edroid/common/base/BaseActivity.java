package com.edroid.common.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.apkrunner.launcher.R;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

public abstract class BaseActivity extends AppCompatActivity {

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
		if(Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_LAYOUT_FLAGS|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            getWindow().getDecorView().setFitsSystemWindows(true);
        }
        
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
        	if(VERSION.SDK_INT == VERSION_CODES.KITKAT)
        		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().fitsSystemWindows();
        }

		setContentView(getLayoutId());
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null)
			setSupportActionBar(toolbar);
		
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
//			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff336699));
//			actionBar.setHomeButtonEnabled(true);
//			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		forceShowOverflowMenu();
	}
	
	protected abstract int getLayoutId();
	
//	@Override
//	public void setSupportActionBar(Toolbar toolbar) {
//		super.setSupportActionBar(toolbar);
//		
//		getSupportActionBar().setHomeButtonEnabled(true);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
		
		/**
		 * 此处比 Fragment 先响应，如果返回 true 则 Fragment 收到不事件
		 */
		
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			finish();
//			break;
//			
//		default:
//			return false;
//		}
//		
//		return true;
	}

	/**
	 * 显示OverflowMenu的Icon
	 * 
	 * @param featureId
	 * @param menu
	 */
	private void setOverflowIconVisible(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 如果设备有物理菜单按键，需要将其屏蔽才能显示OverflowMenu
	 */
	private void forceShowOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		setOverflowIconVisible(featureId, menu);
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
//		SdkUtils.onPause(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

//		SdkUtils.onResume(this);
	}
}
