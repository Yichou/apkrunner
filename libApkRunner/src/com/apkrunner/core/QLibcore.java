package com.apkrunner.core;

import libcore.io.ErrnoException;
import libcore.io.Libcore;
import libcore.io.Os;
import libcore.io.StructStat;

import java.lang.reflect.Field;

/**
 * 
 * @author Yichou 2013-11-30
 *
 */
public class QLibcore {
	static final String TAG = "QLibcore";
	
	
	private static class QOs extends libcore.io.ForwardingOs {
		private int UID = 1025;

		public QOs(Os os) {
			super(os);
		}
		
		@Override
		public int getuid() {
			UID = super.geteuid();
			return UID;
		}
		
		@Override
		public StructStat stat(String path) throws ErrnoException {
			StructStat ret = super.stat(path);
			
//			ApkInfo apkInfo = ApkRunner.currentRunning();
//			
//			if(path.startsWith(ApkInfo.PUBLIC_PATH_ROOT) 
//					|| (apkInfo != null && path.startsWith(apkInfo.getAppPath()))) 
			{
				try {
					Field field = ret.getClass().getDeclaredField("st_uid");
					field.setAccessible(true);
					field.set(ret, UID);
				} catch (Exception e) {
				}
			}
			
			return ret;
		}
		
		
//		private String tmpString;
//		
//		@Override
//		public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
//			ApkInfo info = ApkRunner.currentRunning();
//			if(info != null) {
//				if(tmpString == null) {
//					//get  /data/data
//					final String STR = info.getShellContext().getFilesDir().getParentFile().getParent();
//					// /data/data/pkg
//					tmpString = STR + File.separatorChar + info.getPackgeName();
//				}
//				
//				if(path.startsWith(tmpString)) {
//					final int L = path.length();
//					char[] chs = new char[L];
//					path.getChars(0, L, chs, 0);
//					
//					int i = 0;
//					int j = 0;
//					// /data/data/pkg/xxx
//					while(i < L) { //find the 4th /
//						if(chs[i] == File.separatorChar)
//							j++;
//						if(j == 4)
//							break;
//						i++;
//					}
//					
//					String newPath = info.getAppDataPath() + File.separatorChar;
//					if(i + 1 != L) //
//						newPath += path.substring(i+1);
////					QLog.i(TAG, "ch path " + path + "->" + newPath);
//					path = newPath;
//
//					FileUtils.checkParentPath(newPath); //check and create the parent path!
//				}
//			} 
//			
//			return super.open(path, flags, mode);
//		}
	}
	
	public static void proxy() {
		proxy2();
	}
	
	public static void proxy2() {
//		if(VERSION.SDK_INT < VERSION_CODES.HONEYCOMB)
//			return;
		try {
			Class<?> clazz = Class.forName("libcore.io.Libcore");
			//没有dex路径校验就没有 Libcore
			Field field = clazz.getDeclaredField("os");
			field.setAccessible(true);
			Object real = field.get(null);

			if(real instanceof QLibcore) {
				return;
			}
			field.set(null, new QOs((Os) real));

			Libcore.os.getuid(); //先拿UID
			
			QLog.i(TAG, "libcore proxyed!");
		} catch (Exception e) {
			//do not show!
		}
	}
}
