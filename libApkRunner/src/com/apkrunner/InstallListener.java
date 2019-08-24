package com.apkrunner;

import android.content.Intent;

/**
 * 包安装监听器
 * 
 * @author Yichou
 *
 */
public interface InstallListener {
	public int RET_IGNORE = 0;
	public int RET_HANDLED = 1;
	public int RET_TO_SYSTEM = 2;
	
	public int onInstall(String path, Intent installIntent);
}
