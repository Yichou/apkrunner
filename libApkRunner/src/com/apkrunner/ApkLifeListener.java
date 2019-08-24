package com.apkrunner;

import android.app.Application;


/**
 * APK 运行监听
 * 
 * @author Yichou 2013-11-13 17:59:23
 *
 */
public interface ApkLifeListener {
	/**
	 * 开始初始化 apk 回调
	 */
	public void onLoad(ApkInfo apk);
	
	public void onLoadFinish(ApkInfo apk);
	
	/**
	 * application 创建回调
	 */
	public void onApplicationCreate(ApkInfo apk, Application app);

	public void onLaunch(ApkInfo apk);

	/**
	 * 启动完成回调
	 */
	public void onLaunchFinish(ApkInfo apk);
}
