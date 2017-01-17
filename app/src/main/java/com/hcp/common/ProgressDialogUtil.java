package com.hcp.common;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {
	public static ProgressDialog createUnCanceledDialog(Context context,
			String title, String msg) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(msg);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
}
