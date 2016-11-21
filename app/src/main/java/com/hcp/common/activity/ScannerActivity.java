package com.hcp.common.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

public class ScannerActivity extends BaseActivity {

	private BarcodeReceiver mBarcodeReceiver;

	@Override
	protected void onResume() {

		if (mBarcodeReceiver == null) {
			mBarcodeReceiver = new BarcodeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(BarcodeReceiver.ACTION_SCAN);
			registerReceiver(mBarcodeReceiver, filter);
		}

		super.onResume();
	}

	@Override
	protected void onPause() {

		if (mBarcodeReceiver != null) {
			unregisterReceiver(mBarcodeReceiver);
			mBarcodeReceiver = null;
		}

		super.onPause();
	}

	private class BarcodeReceiver extends BroadcastReceiver {

		public static final String ACTION_SCAN = "com.android.server.hcp.broadcast";
		private final String KEY_BARCODE = "scannerdata";

		@Override
		public void onReceive(Context context, Intent intent) {
			String barcode = intent.getStringExtra(KEY_BARCODE);
			if (!TextUtils.isEmpty(barcode)) {
				decodeCallback(barcode);
			}
		}
	}

	protected void decodeCallback(String barcode) {

	}
}
