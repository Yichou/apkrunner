package com.apkrunner.core;

import java.util.ArrayList;
import java.util.List;

import com.apkrunner.ApkInfo;
import com.apkrunner.utils.Singleton;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;



/**
 * framework 接口调用监听
 * 
 * @author Yichou
 *
 */
public class FrameworkObserver {
	private CopyOnWriteArray<OnInstallListener> mOnInstallListeners;
	
	private List<ApplicationListener> mApplicationListeners;
	private List<ActivityListener> mActivityListeners;
	private List<ApkListener> mApkListeners;

	
	public interface OnInstallListener {
		public int RET_IGNORE = 0;
		public int RET_HANDLED = 1;
		public int RET_TO_SYSTEM = 2;
		
		public int onInstall(Intent installIntent);
	}
	
	public interface ApplicationListener {
		public void onNewApplication(String className);
		
		/**
		 * application 创建回调
		 */
		public void onApplicationCreate(Application app);
	}
	
	public interface ActivityListener {
		public void onNewActivity(String className);
		
		/**
		 * 调用 activity onCreate 之前
		 * 
		 * @param activity
		 */
		public void performCreateActivity( Activity activity);
		
		/**
		 * activity 创建回调
		 */
		public void onActivityCreate( Activity activity);

		public void onActivityResume( Activity activity);

		public void onActivityPause( Activity activity);

		/**
		 * activity 销毁回调
		 */
		public void onActivityDestroy(Activity activity);
	}
	
	public interface ApkListener {
		public void onLoadFinish(ApkInfo apkInfo);
		
		public void onLaunchFinish(ApkInfo apk);
	}

	public void removeApkListener(ApkListener listener) {
		if (mApkListeners != null) {
			mApkListeners.remove(listener);
		}
	}
	
	public void addApkListener(ApkListener listener) {
		if (mApkListeners == null) {
			mApkListeners = new ArrayList<ApkListener>();
		}
		
		mApkListeners.add(listener);
	}
	
	public void removeApplicationListener(ApplicationListener listener) {
		if (mApplicationListeners != null) {
			mApplicationListeners.remove(listener);
		}
	}
	
	public void addApplicationListener(ApplicationListener listener) {
        if (mApplicationListeners == null) {
        	mApplicationListeners = new ArrayList<ApplicationListener>();
        }

        mApplicationListeners.add(listener);
    }
	
	public void dispatchNewApplication(String className) {
		final List<ApplicationListener> listeners = mApplicationListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ApplicationListener l : listeners) {
				l.onNewApplication(className);
			}
		}
	}

	public void dispatchApplicationCreate(Application app) {
		final List<ApplicationListener> listeners = mApplicationListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ApplicationListener l : listeners) {
				l.onApplicationCreate(app);
			}
		}
	}
	
	public void removeActivityListener(ActivityListener listener) {
		if(mActivityListeners != null) {
			mActivityListeners.remove(listener);
		}
	}
	
	public void addActivityListener(ActivityListener listener) {
		if (mActivityListeners == null) {
			mActivityListeners = new ArrayList<ActivityListener>();
		}
		
		mActivityListeners.add(listener);
	}
	
	public void dispatchNewActivity(String className) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.onNewActivity(className);
			}
		}
	}

	public void dispatchPerformCreateActivity(Activity activity) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.performCreateActivity(activity);
			}
		}
	}
	
	public void dispatchActivityCreate(Activity activity) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.onActivityCreate(activity);
			}
		}
	}
	
	public void dispatchActivityResume(Activity activity) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.onActivityResume(activity);
			}
		}
	}

	public void dispatchActivityPause(Activity activity) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.onActivityPause(activity);
			}
		}
	}

	public void dispatchActivityDestroy(Activity activity) {
		final List<ActivityListener> listeners = mActivityListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ActivityListener l : listeners) {
				l.onActivityDestroy(activity);
			}
		}
	}

	
	public void dispatchApkLoadFinish(ApkInfo apkInfo) {
		final List<ApkListener> listeners = mApkListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ApkListener l : listeners) {
				l.onLoadFinish(apkInfo);
			}
		}
	}

	public void dispatchApkLaunchFinish(ApkInfo apkInfo) {
		final List<ApkListener> listeners = mApkListeners;
		if(listeners != null && listeners.size() > 0) {
			for(ApkListener l : listeners) {
				l.onLaunchFinish(apkInfo);
			}
		}
	}
	
	/**
	 * 安装监听
	 * 
	 * @param listener
	 */
	public void addOnInstallListener(OnInstallListener listener) {
        if (mOnInstallListeners == null) {
        	mOnInstallListeners = new CopyOnWriteArray<OnInstallListener>();
        }

        mOnInstallListeners.add(listener);
    }

	
	
	public void dispatchOnInstall(Intent installIntent) {
		final CopyOnWriteArray<OnInstallListener> listeners = mOnInstallListeners;
        if (listeners != null && listeners.size() > 0) {
            CopyOnWriteArray.Access<OnInstallListener> access = listeners.start();
            try {
                int count = access.size();
                for (int i = 0; i < count; i++) {
                    access.get(i).onInstall(installIntent);
                }
            } finally {
                listeners.end();
            }
        }
	}
	
	private static final Singleton<FrameworkObserver> gDefault = new Singleton<FrameworkObserver>() {

		@Override
		protected FrameworkObserver create() {
			return new FrameworkObserver();
		}
	};
	
	public static FrameworkObserver getDefault() {
		return gDefault.get();
	}
	
	/**
     * Copy on write array. This array is not thread safe, and only one loop can
     * iterate over this array at any given time. This class avoids allocations
     * until a concurrent modification happens.
     * 
     * Usage:
     * 
     * CopyOnWriteArray.Access<MyData> access = array.start();
     * try {
     *     for (int i = 0; i < access.size(); i++) {
     *         MyData d = access.get(i);
     *     }
     * } finally {
     *     access.end();
     * }
     */
    static final class CopyOnWriteArray<T> {
        private ArrayList<T> mData = new ArrayList<T>();
        private ArrayList<T> mDataCopy;

        private final Access<T> mAccess = new Access<T>();

        private boolean mStart;

        static class Access<T> {
            private ArrayList<T> mData;
            private int mSize;

            T get(int index) {
                return mData.get(index);
            }

            int size() {
                return mSize;
            }
        }

        CopyOnWriteArray() {
        }

        private ArrayList<T> getArray() {
            if (mStart) {
                if (mDataCopy == null) mDataCopy = new ArrayList<T>(mData);
                return mDataCopy;
            }
            return mData;
        }

        Access<T> start() {
            if (mStart) throw new IllegalStateException("Iteration already started");
            mStart = true;
            mDataCopy = null;
            mAccess.mData = mData;
            mAccess.mSize = mData.size();
            return mAccess;
        }

        void end() {
            if (!mStart) throw new IllegalStateException("Iteration not started");
            mStart = false;
            if (mDataCopy != null) {
                mData = mDataCopy;
            }
            mDataCopy = null;
        }

        int size() {
            return getArray().size();
        }

        void add(T item) {
            getArray().add(item);
        }

        void addAll(CopyOnWriteArray<T> array) {
            getArray().addAll(array.mData);
        }

        void remove(T item) {
            getArray().remove(item);
        }

        void clear() {
            getArray().clear();
        }
    }
}
