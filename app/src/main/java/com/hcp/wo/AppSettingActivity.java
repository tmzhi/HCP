package com.hcp.wo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter.LengthFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.hcp.common.ToastHelper;
import com.hcp.common.activity.BaseActivity;
import com.hcp.util.AppConfig;

public class AppSettingActivity extends BaseActivity {

	private EditText mEdtIP;
	private EditText mEdtPort;
	private EditText mEdtPrinterIP;
	private EditText mEdtPrinterPort;

	private CheckBox mCbPrintEnable;

	private AppSettingActivity mInstance;
	private ToastHelper mToastHelper;
	private AppConfig mAppConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_settings);

		mInstance = this;
		mToastHelper = ToastHelper.getInstance(this);
		mAppConfig = AppConfig.getInstance(this);

		mEdtIP = (EditText) findViewById(R.id.edt_app_settings_ip);
		mEdtIP.setText(AppConfig.getInstance(mInstance).getServerIP());
		mEdtIP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final EditText edtIP = new EditText(mInstance);
				edtIP.setText(mEdtIP.getText().toString());
				edtIP.setSingleLine();
				new AlertDialog.Builder(mInstance)
						.setTitle("IP设置")
						.setView(edtIP)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										if (which == AlertDialog.BUTTON_POSITIVE) {
											if (!TextUtils.isEmpty(edtIP
													.getText().toString())) {
												mEdtIP.setText(edtIP.getText()
														.toString());
												mAppConfig.setServerIP(edtIP
														.getText().toString());

											} else {
												mToastHelper.show("服务器IP不得为空!");
											}
										}
									}

								}).create().show();
			}
		});

		mEdtPort = (EditText) findViewById(R.id.edt_app_settings_port);
		mEdtPort.setText(AppConfig.getInstance(mInstance).getServerPort() + "");
		mEdtPort.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText edtPort = new EditText(mInstance);
				edtPort.setText(mEdtPort.getText().toString());
				edtPort.setInputType(InputType.TYPE_CLASS_NUMBER);
				edtPort.setFilters(new LengthFilter[] { new LengthFilter(4) });
				edtPort.setSingleLine();
				new AlertDialog.Builder(mInstance)
						.setTitle("端口设置")
						.setView(edtPort)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										if (which == AlertDialog.BUTTON_POSITIVE) {
											if (!TextUtils.isEmpty(edtPort
													.getText().toString())) {
												mEdtPort.setText(edtPort
														.getText().toString());
												mAppConfig.setServerPort(Integer
														.parseInt(edtPort
																.getText()
																.toString()));

											} else {
												mToastHelper.show("端口号不得为空!");
											}
										}
									}

								}).create().show();
			}
		});

		mEdtPrinterIP = (EditText) findViewById(R.id.edt_app_settings_printer_ip);
		mEdtPrinterIP.setText(AppConfig.getInstance(mInstance).getPrinterIP() + "");
		mEdtPrinterIP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText edtPrintIP = new EditText(mInstance);
				edtPrintIP.setText(mEdtPrinterIP.getText().toString());
				edtPrintIP.setSingleLine();
				new AlertDialog.Builder(mInstance)
						.setTitle("打印机IP设置")
						.setView(edtPrintIP)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										if (which == AlertDialog.BUTTON_POSITIVE) {
											if (!TextUtils.isEmpty(edtPrintIP
													.getText().toString())) {
												mEdtPrinterIP.setText(edtPrintIP
														.getText().toString());
												mAppConfig.setPrinterIP(edtPrintIP
														.getText()
														.toString());

											} else {
												mToastHelper.show("打印机IP不得为空!");
											}
										}
									}

								}).create().show();
			}
		});

		mEdtPrinterPort = (EditText) findViewById(R.id.edt_app_settings_printer_port);
		mEdtPrinterPort.setText(AppConfig.getInstance(mInstance).getPrinterPort() + "");
		mEdtPrinterPort.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText edtPrinterPort = new EditText(mInstance);
				edtPrinterPort.setText(mEdtPort.getText().toString());
				edtPrinterPort.setInputType(InputType.TYPE_CLASS_NUMBER);
				edtPrinterPort.setFilters(new LengthFilter[] { new LengthFilter(4) });
				edtPrinterPort.setSingleLine();
				new AlertDialog.Builder(mInstance)
						.setTitle("打印机端口设置")
						.setView(edtPrinterPort)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										if (which == AlertDialog.BUTTON_POSITIVE) {
											if (!TextUtils.isEmpty(edtPrinterPort
													.getText().toString())) {
												mEdtPrinterPort.setText(edtPrinterPort
														.getText().toString());
												mAppConfig.setPrinterPort(Integer
														.parseInt(edtPrinterPort
																.getText()
																.toString()));

											} else {
												mToastHelper.show("打印机端口号不得为空!");
											}
										}
									}

								}).create().show();
			}
		});

		mCbPrintEnable = (CheckBox) findViewById(R.id.cb_app_settings_print_enable);
		mCbPrintEnable.setChecked(mAppConfig.isPrintEnable());
		mCbPrintEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mAppConfig.setPrintEnable(isChecked);
			}
		});
	}
}
