package com.apkrunner;


/**
 * 通信消息码定义
 * 
 * @author Yichou 2013-11-6 21:48:34
 *
 */
public interface IMsgCode {
	public int MSG_HELLO = 0x1001;
	public int MSG_HEART = 0x1002; //心跳消息

	public int MSG_START = 0x1011;
	public int MSG_PAUSE = 0x1012;
	public int MSG_RESUME = 0x1013;
	public int MSG_EXIT = 0x1014;
	public int MSG_CRASH = 0x1015;

	public int MSG_APP_APPLICATION_CREATE= 0x1021;
	public int MSG_APP_LAUNCH_FINISH = 0x1022;

	public int MSG_FUNC_GETAPPICON = 0x1031;
	public int MSG_FUNC_GETAPPLABEL = 0x1032;

	public int MSG_BYBY = 0x1fff;
}
