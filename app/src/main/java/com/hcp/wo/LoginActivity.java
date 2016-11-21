package com.hcp.wo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcp.biz.request.LoginRequestManager;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.AppCommon;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ScanToolBroadcastSettings;
import com.hcp.common.ToastHelper;
import com.hcp.common.XmlUtil;
import com.hcp.common.activity.BaseActivity;
import com.hcp.dao.LocatorDao;
import com.hcp.dao.SubInventoryDao;
import com.hcp.download.Downloader;
import com.hcp.stocktaking.entity.Locator;
import com.hcp.stocktaking.entity.SubInventory;
import com.hcp.update.UpdateAsynTask;
import com.hcp.update.UpdateMessage;
import com.hcp.util.AppCache;
import com.hcp.util.AppConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class LoginActivity extends BaseActivity {

	private final String AUTHORITY_PASSWORD = "1982376450";
	private final String SUBINVENTORY_AYTHORITY_PASSWORD = "1937246850";

	private final int CHECK_UPDATE = 1;
	private final int UPDATE = 2;

	private Button mBtnLogin;
	private Button mBtnSettings;

	private EditText mEdtUserCode;
	private EditText mEdtPassword;

	private ImageView mImgLogo;

	private TextView mTvVersion;

	private ProgressDialog mLoginPD;

	private LoginActivity mInstance;
	private Resources mRes;
	private AppConfig mAppConfig;

	private final String ACTION_SCAN = "com.android.server.hcp.broadcast";
	private final String KEY_BARCODE = "scannerdata";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		init();
	}

	private void init(){

		mInstance = this;
		mRes = getResources();
		mAppConfig = AppConfig.getInstance(this);

		mBtnLogin = (Button) findViewById(R.id.btn_login_login);
		mBtnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});

		mBtnSettings = (Button) findViewById(R.id.btn_login_settings);
		mBtnSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setting();
			}
		});

		mImgLogo = (ImageView) findViewById(R.id.img_login_logo);
		mImgLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toAppSetting();
			}
		});

		mTvVersion = (TextView) findViewById(R.id.tv_login_version);
		mTvVersion.setText("Ver:" + AppCommon.getAppVersionName(mInstance));

		mEdtUserCode = (EditText) findViewById(R.id.edt_login_usercode);
		mEdtPassword = (EditText) findViewById(R.id.edt_login_password);

		mEdtPassword.setText("123456");

		setBarCodeBroadcast();
	}

	private void setBarCodeBroadcast(){
		Intent intent = new Intent(ScanToolBroadcastSettings.ACTION_APP_SETTING);
		intent.putExtra(ScanToolBroadcastSettings.TYPE_BARCODE_BROADCAST_ACTION, ACTION_SCAN);
		intent.putExtra(ScanToolBroadcastSettings.TYPE_BARCODE_BROADCAST_KEY, KEY_BARCODE);
		sendBroadcast(intent);
	}

	ProgressDialog mProgressDialog;

	@Override
	protected void onResume() {

		//test();

		setDefaultUser();

		doUpdate();

		super.onResume();
	}

	private void test(){

	}

	private void setDefaultUser(){

		if(TextUtils.isEmpty(mEdtUserCode.getText().toString())){
			mEdtUserCode.setText(mAppConfig.getLastLoginUser());
		}
	}

	private void doUpdate(){

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this, "提示", "正在检查更新...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {

				Message msg = Message.obtain();
				msg.what = CHECK_UPDATE;
				msg.obj = checkUpdate();
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	private UpdateMessage checkUpdate(){
		UpdateMessage result = null;
		try {

			Downloader downloader = new Downloader();
			downloader.download(String.format("http://%s:9797/update.xml", mAppConfig.getServerIP()), AppCommon.APP_TEMP_DIRECTORY + "update.xml");

			result = XmlUtil.deserialize(UpdateMessage.class, AppCommon.APP_TEMP_DIRECTORY + "update.xml", XmlUtil.ENCODING_GB2312);
		} catch (Exception e) {
			Log.e("LoginActivity", e.getMessage());
		}

		return result;
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			if(mProgressDialog != null){
				mProgressDialog.cancel();
			}

			switch (msg.what) {
				case CHECK_UPDATE:

					UpdateMessage result = (UpdateMessage) msg.obj;
					if(result != null && result.version > AppCommon.getAppVersion(mInstance)){

						StringBuffer updateMessage = new StringBuffer();
						updateMessage.append("有可用更新!\n");
						updateMessage.append("------------------------------------\n");
						updateMessage.append("版本号:" + result.versionName + "\n");
						updateMessage.append("更新内容:\n");
						updateMessage.append(result.message);

						AlertDialog dialog = new AlertDialog.Builder(mInstance)
								.setTitle("提示")
								.setMessage(updateMessage.toString())
								.setNegativeButton("取消", null)
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										update();
									}
								})
								.create();

						dialog.setCanceledOnTouchOutside(false);
						dialog.setCancelable(false);
						dialog.show();
					}
					break;
				case UPDATE:
					break;
			}

			return false;
		}
	});

	private void update(){
		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(mInstance, "更新", "正在下载安装文件...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setProgress(0);
		mProgressDialog.setProgressNumberFormat("%1d kb/%2d kb");
		mProgressDialog.show();

		UpdateAsynTask task = new UpdateAsynTask(mInstance);
		task.setProgressDialog(mProgressDialog);

		String updateFileUrl = String.format("http://%s:9797/HCP.apk", mAppConfig.getServerIP());

		task.execute(updateFileUrl, AppCommon.APP_TEMP_DIRECTORY + "HCP.apk");
	}

	private void login() {
		String usercode = mEdtUserCode.getText().toString();
		String password = mEdtPassword.getText().toString();

		if(AUTHORITY_PASSWORD.equals(password)){
			Intent intent = new Intent(this, BizAuthorityActivity.class);
			startActivity(intent);
			return;
		}

		if(SUBINVENTORY_AYTHORITY_PASSWORD.equals(password)){
			Intent intent = new Intent(this, SubInventoryAuthorityActivity.class);
			startActivity(intent);
			return;
		}

		if (TextUtils.isEmpty(usercode) || TextUtils.isEmpty(password)) {
			ToastHelper.getInstance(this).show(
					mRes.getString(R.string.login_error_on_empty_field));
			return;
		}

		mLoginPD = ProgressDialog.show(this,
				mRes.getString(R.string.login_login),
				mRes.getString(R.string.login_on_progress));

		new LoginAsynTask().execute(usercode, password);

	}

	private void setting() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(intent);
	}

	private void toAppSetting() {
		Intent intent = new Intent(this, AppSettingActivity.class);
		startActivity(intent);
	}

	private class LoginAsynTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {

			String result = null;

			String userCode = params[0];
			String password = params[1];

			try {

				if(!LoginRequestManager.getInstance(LoginActivity.this).isLoginSuccess(userCode, password)){
					return mRes.getString(R.string.login_error_on_check);
				}

				WORequestManager manager = WORequestManager.getInstance(mInstance);
				try{

					List<Locator> locs = manager.getLocators();
					if(locs != null && locs.size() > 0){
						LocatorDao.getInstance(mInstance).clear();
						LocatorDao.getInstance(mInstance).insertLocators(locs);
					}
				}catch (Exception ex){
					ex.printStackTrace();
				}

				try{
					List<SubInventory> subs = manager.getSubInventories();
					if(subs != null && subs.size() > 0){
						SubInventoryDao.getInstance(mInstance).clear();
						SubInventoryDao.getInstance(mInstance).insertSubInventories(subs);
					}
				}catch (Exception ex){
					ex.printStackTrace();
				}

				mAppConfig.setLastLoginUser(userCode);
				AppCache.getInstance().setLoginUser(userCode);

			} catch (Exception e) {
				result = e.getMessage();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);

			// Hide progress dialog
			if (mLoginPD != null && mLoginPD.isShowing()) {
				mLoginPD.cancel();
				mLoginPD = null;
			}

			if (TextUtils.isEmpty(result)) {

				mEdtPassword.setText(StringUtils.EMPTY);

				startActivity(new Intent(mInstance, FuncChooseActivity.class));
			} else {
				ToastHelper.getInstance(mInstance).show(result);
			}
		}
	}
}
