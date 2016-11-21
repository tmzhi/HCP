package com.hcp.common.activity;

import com.hcp.common.ToastHelper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.WindowManager;

public class BaseActivity extends Activity {
	
	protected Activity mInstance;
	
	protected ToastHelper mToastHelper;
	
	protected Resources mResources;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide soft input keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mInstance = this;
		
		mToastHelper = ToastHelper.getInstance(mInstance);
		
		mResources = getResources();
	}

	@Override
	protected void onResume() {
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		super.onResume();
	}
}
