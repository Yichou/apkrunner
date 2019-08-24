package com.android.commands.input;

import com.apkrunner.core.QInstrumentation;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputImplAct extends BaseInput {

	@Override
	protected void injectKeyEvent(final KeyEvent event) {
//		InputManager.getInstance().injectInputEvent(event,
//                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
		
		final Activity activity = QInstrumentation.getDefault().getTopActivity();
		if(activity != null) {
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					activity.getWindow().getDecorView().dispatchKeyEvent(event);
				}
			});
		}
	}

	@Override
	protected void injectMotionEvent(final MotionEvent event) {
//		InputManager.getInstance().injectInputEvent(event,
//                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
		
		final Activity activity = QInstrumentation.getDefault().getTopActivity();
		if(activity != null) {
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					activity.getWindow().getDecorView().dispatchTouchEvent(event);
				}
			});
		}
	}

}
