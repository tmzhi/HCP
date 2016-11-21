package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.Organization;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class OrganizationActivity extends ScannerActivity implements View.OnClickListener{

	private TextView mTvCurrent;
	private Spinner mSpnList;
	private Button mBtnBind;
	private Button mBtnRefresh;

	private WORequestManager mRequestManager;
	private AppConfig mAppconfig;

	private List<Organization> mOrgs = new ArrayList<Organization>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intraware_organization);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);
		mAppconfig = AppConfig.getInstance(mInstance);

		mTvCurrent = (TextView) findViewById(R.id.tv_intraware_organization_current);
		mSpnList = (Spinner) findViewById(R.id.spn_intraware_organization_list);
		mBtnBind = (Button) findViewById(R.id.btn_intraware_organization_bind);
		mBtnBind.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_intraware_organization_refresh);
		mBtnRefresh.setOnClickListener(this);

		setCurrentOrg();
	}

	@Override
	protected void onResume() {
		if(mOrgs == null || mOrgs.size() == 0){
			getOrganizations();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getOrganizations() {
		mMsg = null;

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"组织信息", "正在更新组织信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<Organization> orgs = null;
				try {
					orgs = mRequestManager
							.getOrganizations();
					if (orgs != null && orgs.size() > 0) {

						mOrgs = orgs;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								String[] orgNames = new String[mOrgs.size()];
								for (int i = 0; i < orgNames.length; i++) {
									String name = mOrgs.get(i).Attribute6;
									orgNames[i] = TextUtils.isEmpty(name) ? mOrgs.get(i).OrgId + " - " + mOrgs.get(i).OrgCode : name;
								}
								ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(mInstance,
										R.layout.simple_spinner_item, orgNames);
								spnAdapter
										.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
								mSpnList.setAdapter(spnAdapter);
							}
						});
					}
				} catch (Exception e) {
					mMsg = e.getMessage();
				} finally {
					hideProgress(mMsg);
				}
			}
		}).start();
	}

	private void hideProgress(final String msg){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}

				if (!TextUtils.isEmpty(msg)) {
					mToastHelper.show(msg);
				}
			}
		});
	}

	private void bind() {

		if(mOrgs != null && mOrgs.size() > 0){
			int index = mSpnList.getSelectedItemPosition();
			Organization org = mOrgs.get(index);
			mAppconfig.setOrganizationCode(org.OrgCode);
			mAppconfig.setOrganizationId(org.OrgId);
			mAppconfig.setOrganizationName(org.Attribute6);

			setCurrentOrg();

			mToastHelper.show("绑定成功");
		}else{
			mToastHelper.show("请先获取组织信息!");
		}
	}

	private void setCurrentOrg(){
		if(mAppconfig.getOrganizationId() > 0){
			mTvCurrent.setText(
					String.format("%s-%s(%s)",
							mAppconfig.getOrganizationId(),
							mAppconfig.getOrganizationCode(),
							TextUtils.isEmpty(mAppconfig.getOrganizationName()) ? mAppconfig.getOrganizationId() + "-" + mAppconfig.getOrganizationCode() : mAppconfig.getOrganizationName()));
		}
	}

	private void refresh() {
		getOrganizations();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_intraware_organization_bind:
				bind();
				break;
			case R.id.btn_intraware_organization_refresh:
				refresh();
				break;
		}
	}
}
