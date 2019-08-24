package com.edroid.common.base;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 管理桌面悬浮窗口
 * 
 * @author YYichou 2014-05-02
 *
 */
public class FloatViewManager implements OnTouchListener {

	public static class FloatView {
	    public interface OnClickListener {
	        public void onClick();
	    }

		public View rootView;
		
		WindowManager.LayoutParams wmParams;
		private int layoutID;
		private WindowManager wm;
		private OnClickListener listener;
		
		public void onClick() {
		    if(listener != null)
		        listener.onClick();
        }
		
		public void setFocusable(boolean f) {
			if(f)
				clearFlags(LayoutParams.FLAG_NOT_FOCUSABLE);
			else
				addFlags(LayoutParams.FLAG_NOT_FOCUSABLE);
		}

		public void setTouchable(boolean f) {
			if(f)
				clearFlags(LayoutParams.FLAG_NOT_TOUCHABLE);
			else
				addFlags(LayoutParams.FLAG_NOT_TOUCHABLE);
		}
		
		public void addFlags(int flags) {
	        setFlags(flags, flags);
	    }
		
		public void clearFlags(int flags) {
	        setFlags(0, flags);
	    }
		
		public void setFlags(int flags, int mask) {
			wmParams.flags = (wmParams.flags&~mask) | (flags&mask);
			wm.updateViewLayout(rootView, wmParams);
		}
	}

	private final ArrayList<FloatView> list = new ArrayList<FloatViewManager.FloatView>();
	
	private final Rect rect = new Rect();
	private float downX, downY;
	private float moveX, moveY;
	private int scnW, scnH, stbH;
	
	private WindowManager wm;
	private LayoutParams wmParams;
	
	public FloatViewManager() {
	}
	
	public FloatView getFloatView(int layoutId) {
		if(list.size() > 0) {
			for(FloatView v : list) {
				if(v.layoutID == layoutId) return v;
			}
		}
		return null;
	}
	
	public FloatView create(Context context, int layout, FloatView.OnClickListener listener) {
		return create(context, layout, 100, 0, listener);
	}
	
	public FloatView create(Context context, int layout, int x, int y, FloatView.OnClickListener listener) {
		FloatView floatView = getFloatView(layout);
		if(floatView != null) {
			floatView.listener = listener;
			return floatView;
		}
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(layout, null);
		
		// 1、获取WindowManager对象
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		// 2、设置LayoutParams(全局变量）相关参数
		wmParams = new WindowManager.LayoutParams();
		
		// 3、设置相关的窗口布局参数 （悬浮窗口效果）
		wmParams.type = LayoutParams.TYPE_PHONE; // 设置window type
//		wmParams.type = LayoutParams.TYPE_APPLICATION; // 设置window type
		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
		// 4、设置Window flag == 不影响后面的事件 
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL|LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
		 * 注意，flag的值可以为： LayoutParams.FLAG_NOT_TOUCH_MODAL 不影响后面的事件
		 * LayoutParams.FLAG_NOT_FOCUSABLE 不可聚焦 LayoutParams.FLAG_NOT_TOUCHABLE
		 * 不可触摸
		 */
//		wmParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE|LayoutParams.FLAG_NOT_FOCUSABLE;

		Display display = wm.getDefaultDisplay();
		display.getRectSize(rect);
		stbH = rect.top;
		scnW = rect.width();
		scnH = rect.height();
		System.out.println("displayRect=" + rect);
		
		// 5、 调整悬浮窗口至左上角，便于调整坐标
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = x;
		wmParams.y = y;
		// 6、设置悬浮窗口长宽数据
		wmParams.width = LayoutParams.WRAP_CONTENT;
		wmParams.height = LayoutParams.WRAP_CONTENT;
		
		view.setOnTouchListener(this);
		
		wm.addView(view, wmParams);
		
		floatView = new FloatView();
		view.setTag(floatView);
		
		floatView.wm = wm;
		floatView.rootView = view;
		floatView.wmParams = wmParams;
		floatView.layoutID = layout;
		floatView.listener = listener;
		
		list.add(floatView);
		
		return floatView;
	}
	
	private void updateButtonPosition(FloatView floatView) {
		wm.updateViewLayout(floatView.rootView, floatView.wmParams);
	}
	
//	public void setTouchAble(FloatView floatView, boolean touchable) {
//		floatView.wmParams.flags = touchable? (LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE) 
//				: (LayoutParams.FLAG_NOT_TOUCHABLE|LayoutParams.FLAG_NOT_FOCUSABLE);
//		
//		wm.updateViewLayout(floatView.rootView, floatView.wmParams);
//	}
	
	public void destroy(int layoutId) {
		FloatView floatView = getFloatView(layoutId);
		if(floatView != null) {
			wm.removeView(floatView.rootView);
			list.remove(floatView);
		}
	}
	
	private boolean moved = false;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		FloatView floatView = (FloatView) v.getTag();
		if(floatView == null)
			return false;
		
		WindowManager.LayoutParams wmParams = floatView.wmParams;
		
		//获取窗口可见大小
		v.getGlobalVisibleRect(rect);
		v.getLocalVisibleRect(rect);
		
		
		// 2、获取相对屏幕的坐标，即以屏幕左上角为原点 。y轴坐标= y（获取到屏幕原点的距离）-状态栏的高度
		float x = event.getRawX();
		float y = event.getRawY(); // statusBarHeight是系统状态栏的高度
		
		// 3、处理触摸移动
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: { // 捕获手指触摸按下动作
			downX = x;
			downY = y;
			moved = false;
			break;
		}

		case MotionEvent.ACTION_MOVE: { // 捕获手指触摸移动动作
			moveX = x - downX;
			moveY = y - downY;
			
			if(Math.abs(moveX) > 5 || Math.abs(moveY) > 5) {
			    moved = true;
			}
			
			wmParams.x += (x - downX);
			wmParams.y += (y - downY);
			
			downX = x;
			downY = y;
			
			if(wmParams.x < 0)
				wmParams.x = 0;
			else if(wmParams.x + rect.width() > scnW)
				wmParams.x = scnW - rect.width();
			
			if(wmParams.y < stbH) 
				wmParams.y = stbH;
			else if(wmParams.y + rect.height() > scnH)
				wmParams.y = scnH - rect.height();
			
			updateButtonPosition(floatView);
			break;
		}

		case MotionEvent.ACTION_UP: { // 捕获手指触摸离开动作
			updateButtonPosition(floatView);
			if(!moved) {
			    floatView.onClick();
			    v.performClick();
			}
			break;
		}
		
		}
		
		return true;
	}
	
	
	private static FloatViewManager instance;
	public static FloatViewManager getInstance() {
		if(instance == null)
			instance = new FloatViewManager();
		return instance;
	}
}
