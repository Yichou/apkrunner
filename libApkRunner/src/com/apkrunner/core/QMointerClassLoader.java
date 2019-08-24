package com.apkrunner.core;

/**
 * 检测系统类加载
 * 
 * @author Yichou
 * 
 */
public final class QMointerClassLoader extends ClassLoader {
	static final String TAG = "QMointerClassLoader";

	
	public QMointerClassLoader(ClassLoader parentLoader) {
		super(parentLoader);
	}

//	int i=0;
	boolean load = false;
	@Override
	protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
//		System.out.println(i++);

		if (!load && className.equals("android.app.INotificationManager$Stub$Proxy")) {
			load = true;
			throw new ClassNotFoundException("you are ticked!");
		}

		return super.loadClass(className, resolve);
	}

	public static void proxy() {
		ClassLoader loader = QMointerClassLoader.class.getClassLoader();
		Frameworks.YClassLoader.setParent(loader, new QMointerClassLoader(loader.getParent()));
	}
}
