package com.hcp.common;

import android.content.Context;
import android.widget.Toast;

public class ToastHelper {
	private Toast mToast;
	private Context mContext;

	private static ToastHelper mToastHelper;

	private static Object mLocker = new Object();

	private ToastHelper(Context context) {
		mContext = context.getApplicationContext();
	}

	public static ToastHelper getInstance(Context context) {
		if (mToastHelper == null) {
			synchronized (mLocker) {
				mToastHelper = new ToastHelper(context);
			}
		}
		return mToastHelper;
	}

	public void show(String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(msg);
		}
		mToast.show();
	}

}
