package com.android.commands.input;

import android.hardware.input.InputManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputImpl4 extends BaseInput {

	@Override
	protected void injectKeyEvent(final KeyEvent event) {
		InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
	}

	@Override
	protected void injectMotionEvent(final MotionEvent event) {
		InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
	}
}
