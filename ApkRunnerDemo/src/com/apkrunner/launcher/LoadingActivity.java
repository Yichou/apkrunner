package com.apkrunner.launcher;

import com.apkrunner.ApkRunCallback;
import com.apkrunner.ApkRunner;
import com.apkrunner.launcher.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class LoadingActivity extends Activity {
	Context mContext;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		setContentView(R.layout.activity_loading);

		final String apkPath = getIntent().getData().getSchemeSpecificPart();
		
		if (getIntent().getAction().equals("main")) {
			ApkRunner.runApkInMainProcess(this, apkPath);
		} else {
			ApkRunner.runApkNewProcess(this, apkPath, new ApkRunCallback() {

				@Override
				public void onSelectProcess(int procIndex) {
					final String fileName = mContext.getPackageName() + ":app" + procIndex;
					SharedPreferences sp = mContext.getSharedPreferences(fileName, 0);
					sp.edit()
						.putString("apkPath", apkPath)
						.putBoolean("auto", false)
						.commit();
				}
			});
		}
		
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(LoadingActivity.this, "启动超时！", Toast.LENGTH_SHORT).show();
				finish();
			}
		}, 10*1000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!isFinishing()) {
			finish();
		}
	}
}
