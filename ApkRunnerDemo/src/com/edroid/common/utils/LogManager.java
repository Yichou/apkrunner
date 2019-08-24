package com.edroid.common.utils;



/**
 * 
 * @author YYichou
 *
 */
public final class LogManager {
	static boolean sDebug;
	static KvTool kvTool;
	
	static {
		initDebugs();
	}
	
	static boolean isDebug(int i) {
		return (i>3598 && i<0x4587);
	}
	
	/**
	 * system_process 初始化的时候SD卡可能还没挂载，此时去读写文件会出错，擦
	 */
	public static void initDebugs() {
//		File file = new File(Environment.getExternalStorageDirectory(), 
//				"debug-apkhooker");
//		
//		if(file.exists()) {
//			sDebug = true;
//			kvTool = new KvTool(file);
//		}
		
		sDebug = true;
	}
	
	public static Logger createLogger(String tag) {
		return Logger.create(
				true/*kvTool != null? isDebug(kvTool.getInt(tag, 0)) : sDebug*/,
				tag);
	}
}
