package com.hcp.ui.event;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

public abstract class OnKeyDownEvent implements OnKeyListener{

	private boolean handsOn;
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
			handsOn = true;
			onKeyDown(v, keyCode, event);
		} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
			handsOn = false;
		}
		return false;
	}
	
	public abstract void onKeyDown(View v, int keyCode, KeyEvent event);

}
