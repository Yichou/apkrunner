package com.apkrunner.utils;

import java.util.Locale;

import android.os.Build;


/**
 * CPU 信息工具
 * 
 * @author Yichou 2013-11-30
 *
 */
public final class CpuUtils {
	public enum CpuType {
		ARM("armeabi"),
		ARMV7("armeabi-v7a"),
		X86("x86"),
		MIPS("mips"),
		UNKNOW(null);
		
		private final String abi;
		private final String zipPath;

		private CpuType(String abi) {
			this.abi = abi;
			this.zipPath = "lib/" + abi + '/';
		}

		public String getAbi() {
			return abi;
		}
		
		/**
		 * 在 apk包中的路径前缀
		 * @return
		 */
		public String getZipPath() {
			return zipPath;
		}
	}
	
	public static CpuType getCpuType() {
		for(CpuType type : CpuType.values()) {
			if(type.abi.equalsIgnoreCase(Build.CPU_ABI))
				return type;
		}
		
		return CpuType.UNKNOW;
	}
	
	public static void printCpuInfo() {
		String arch = System.getProperty("os.arch");
		String arc = arch.substring(0, 3).toUpperCase(Locale.getDefault());
		String rarc = "";
		if (arc.equals("ARM")) {
			rarc = "This is ARM";
		} else if (arc.equals("MIP")) {
			rarc = "This is MIPS";
		} else if (arc.equals("X86")) {
			rarc = "This is X86";
		}
		
		System.out.println(rarc);
	}
	
	public static String getCpuArch() {
		String arch = System.getProperty("os.arch");
		return arch!=null? arch.toLowerCase(Locale.getDefault()) : null;
	}
}
