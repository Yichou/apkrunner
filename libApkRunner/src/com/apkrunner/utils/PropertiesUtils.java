package com.apkrunner.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 键值对存储工具
 * 
 * @author Yichou 2013-8-15
 *
 */
public final class PropertiesUtils /*extends Properties*/ {
	
	public static PropertiesUtils create(File file) /*throws Exception*/ {
		FileInputStream fis = null;
		try {
			FileUtils.checkParentPath(file);
			if(!file.exists())
				file.createNewFile();
			
			fis = new FileInputStream(file);
			return create(fis);
		} catch (Exception e) {
			throw new NullPointerException("can't create Prop for " + file);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 创建实例
	 * 
	 * @param is
	 * @return 失败返回 null
	 */
	public static PropertiesUtils create(InputStream is) throws Exception {
		PropertiesUtils prop = new PropertiesUtils();
		prop.load(is);
		return prop;
	}
	
	private final Properties mProperties = new Properties();
	
	private PropertiesUtils() {
	}
	
	
	
	private void load(InputStream is) throws IOException {
		mProperties.load(is);
	}
	
	public void save(File file) {
		FileOutputStream fos = null;
		try {
			FileUtils.checkParentPath(file);
			fos = new FileOutputStream(file);
			mProperties.store(fos, null);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("can't open save file " + file);
		} catch (IOException e) {
			throw new RuntimeException("I/O exception!");
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}
	
	public int getInt(String key, int def) {
		String value = mProperties.getProperty(key); //null if it can't be found
		if(value != null) {
			try {
				return Integer.valueOf(value);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	public String getString(String key, String def) {
		return mProperties.getProperty(key); //null if it can't be found
	}
	
	public boolean getBoolean(String key, boolean def) {
		String value = mProperties.getProperty(key); //null if it can't be found
		if(value != null) {
			try {
				return Boolean.valueOf(value);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	public void put(String key, String value) {
		mProperties.setProperty(key, value);
	}

	public void put(String key, int value) {
		mProperties.setProperty(key, String.valueOf(value));
	}
	
	public void put(String key, boolean value) {
		mProperties.setProperty(key, String.valueOf(value));
	}
	
	
}
