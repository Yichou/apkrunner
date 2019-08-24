package com.apkrunner.core;

import java.io.File;
import java.lang.reflect.Field;

import com.apkrunner.ApkInfo;
import com.apkrunner.ApkRunner;
import com.apkrunner.utils.FileUtils;
import com.apkrunner.utils.TimeUtils;

import android.annotation.SuppressLint;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.StringBuilderPrinter;
import android.widget.Toast;


/**
 * 异常处理器
 * 
 * @author Yichou 2013-10-25
 *
 */
public final class CrashHandler {
	
	private CrashHandler() {
	}
	
	private static void exit(final Context mContext) {
		new Thread() {

			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext,
						"~~~>_<~~~ 又崩溃了，即将退出。", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		
		// 退出程序
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}
	
	/**
	 * 开启异常捕获
	 */
	public static void init() {
	}
	
	/**
	 * 收集系统硬件信息
	 * 
	 * @param context
	 * @return
	 */
	public static String collectDeviceInfo(Context context) {
		StringBuilder sb = new StringBuilder(1024);
		
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

			sb.append("name:").append(context.getResources().getString(pi.applicationInfo.labelRes)).append('\n');
			sb.append("pkg:").append(pi.packageName).append('\n');
			sb.append("versionCode:").append(pi.versionCode).append('\n');
			sb.append("versionName:").append(pi.versionName).append('\n');
		} catch (Exception e) {
		}

		sb.append("process:").append(ApkRunner.getCurProcessName()).append('\n');

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				sb.append(field.getName()).append('=').append(field.get(null).toString()).append('\n');
			} catch (Exception e) {
			}
		}
		
		return sb.toString();
	}

	/***
	 * 保存错误信息到文件中
	 */
	@SuppressLint("NewApi")
	public static void crashToFile(final Context mContext, ApplicationErrorReport.CrashInfo info) {
		StringBuilder sb = new StringBuilder(1024);
		File saveFile = new File(ApkInfo.PUBLIC_PATH_ROOT, "/crashs/crash " + TimeUtils.getDateTimeNow() + ".txt");
//		File saveFile =  mContext.getFileStreamPath("crash " + TimeUtils.getDateTimeNow() + ".txt");
		
		if(saveFile.length() == 0) { //第一次创建
			sb.append("-------- device info ---------------\n");
			sb.append(collectDeviceInfo(mContext));
			sb.append('\n');
		}
		
		sb.append("------------- ");
		sb.append(TimeUtils.getDateTimeNow());
		sb.append(" ------------\n");

		info.dump(new StringBuilderPrinter(sb), null);
		
		byte[] data = sb.toString().getBytes();
		
		FileUtils.bytesToFile(saveFile, data);
		
		exit(mContext);
	}
}
