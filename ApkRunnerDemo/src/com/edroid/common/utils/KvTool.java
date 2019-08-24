package com.edroid.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.res.AssetManager;

/**
 * 键值对存储工具
 * 
 * @author Yichou 2013-8-15
 *
 */
public final class KvTool {
	private final Properties mProperties = new Properties();
	private File file;
	
	
	public KvTool(AssetManager asset, String name) {
		InputStream is = null;
		try {
			is = asset.open(name);
			load(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e2) {
			}
		}
	}
	
	public KvTool(File file) {
		this.file = file;
		
		FileInputStream fis = null;
		try {
			FileUtils.checkParentPath(file);
			if(!file.exists())
				file.createNewFile();
			
			fis = new FileInputStream(file);
			load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}
	
	private void load(InputStream is) throws IOException {
		mProperties.load(is);
	}
	
	public void save() {
		save(file);
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

	public long getLong(String key, long def) {
		String value = mProperties.getProperty(key); //null if it can't be found
		if(value != null) {
			try {
				return Long.valueOf(value);
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
	
	public KvTool putString(String key, String value) {
		return put(key, value);
	}
	public KvTool put(String key, String value) {
		mProperties.setProperty(key, value);
		return this;
	}

	public KvTool putInt(String key, int value) {
		return put(key, value);
	}
	public KvTool put(String key, int value) {
		mProperties.setProperty(key, String.valueOf(value));
		return this;
	}

	public KvTool putLong(String key, long value) {
		return put(key, value);
	}
	public KvTool put(String key, long value) {
		mProperties.setProperty(key, String.valueOf(value));
		return this;
	}
	
	public KvTool putBoolean(String key, boolean value) {
		return put(key, value);
	}
	
	public KvTool put(String key, boolean value) {
		mProperties.setProperty(key, String.valueOf(value));
		return this;
	}
	
	public void commit() {
		if(file == null) 
			throw new RuntimeException("asset mode cant save! call save(File)");
		
		save();
	}
}
