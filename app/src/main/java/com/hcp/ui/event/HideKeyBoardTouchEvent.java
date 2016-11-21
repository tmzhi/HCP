package com.hcp.ui.event;

import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class HideKeyBoardTouchEvent implements OnTouchListener {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		EditText edt = (EditText) v;
		int inType = edt.getInputType();
		edt.setInputType(InputType.TYPE_NULL);
		edt.onTouchEvent(event);
		edt.setInputType(inType);
		edt.setSelection(edt.getText().length());
		return true;
	}

}
