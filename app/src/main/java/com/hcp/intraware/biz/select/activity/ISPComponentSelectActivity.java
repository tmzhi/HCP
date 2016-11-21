package com.hcp.intraware.biz.select.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.intraware.constants.RequestCode;
import com.hcp.intraware.constants.RequestKey;
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPComponent;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPComponentSelectActivity extends ScannerActivity implements View.OnClickListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtComponent;

	private WORequestManager mRequestManager;

	private LocatorAdapter mAdapter = new LocatorAdapter();

	private List<ISPComponent> mComponents = new ArrayList<ISPComponent>();

	private int mSelectPosition = -1;

	private String mOrganization;
	private String mWip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_component_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mEdtComponent = (EditText) findViewById(R.id.edt_isp_component_select_component);
		mEdtComponent.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					getISPComponents();
				}
				return false;
			}
		});

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_component_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_component_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_component_select_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);

		mOrganization = getIntent().getStringExtra(RequestKey.ISP_ORGANIZATION);
		mWip = getIntent().getStringExtra(RequestKey.ISP_WIP);

	}

	@Override
	protected void onResume() {
		if(mComponents == null || mComponents.size() == 0){
			getISPComponents();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPComponents() {
		mMsg = null;

		final String component = mEdtComponent.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"组件", "正在获取组件信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPComponent> components = null;
				try {
					components = mRequestManager
							.getISPComponents(mOrganization, mWip, component);

					mComponents = components;
					mSelectPosition = -1;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mAdapter.notifyDataSetChanged();
						}
					});

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

	private void confirm() {

		if(mSelectPosition < 0){
			mToastHelper.show("请先选择组件!");
		}else{
			ISPComponent item = mComponents.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_COMPONENT, item.Component);
			data.putExtra(ResultKey.KEY_COMPONENT_DESCRIPTION, item.ComponentDescription);
			data.putExtra(ResultKey.KEY_COMPONENT_UOM, item.Uom);
			data.putExtra(ResultKey.KEY_COMPONENT_OPERATION, item.Operation);
			data.putExtra(ResultKey.KEY_COMPONENT_REQUIRED_QUANTITY, item.RequiredQuantity);
			data.putExtra(ResultKey.KEY_COMPONENT_ISSUED_QUANTITY, item.IssuedQuantity);
			setResult(RequestCode.ISP_COMPONENT, data);
			finish();
		}
	}

	private void refresh() {
		getISPComponents();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_component_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_component_select_refresh:
				refresh();
				break;
		}
	}

	private class LocatorAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mComponents.size();
		}

		@Override
		public Object getItem(int position) {
			return mComponents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_component_select, null);

				holder = new ViewHolder();
				holder.component = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_component);
				holder.componentDescription = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_component_description);
				holder.uom = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_uom);
				holder.operation = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_operation);
				holder.requiredQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_required_quantity);
				holder.issuedQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_component_select_issued_quantity);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPComponent item = mComponents.get(position);
			holder.component.setText(item.Component);
			holder.componentDescription.setText(item.ComponentDescription);
			holder.uom.setText(item.Uom);
			holder.operation.setText(item.Component);
			holder.requiredQuantity.setText(item.RequiredQuantity);
			holder.issuedQuantity.setText(item.IssuedQuantity);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.component.setTextColor(mResources.getColor(R.color.white));
					holder.uom.setTextColor(mResources.getColor(R.color.white));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.white));
					holder.operation.setTextColor(mResources.getColor(R.color.white));
					holder.requiredQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.issuedQuantity.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.uom.setTextColor(mResources.getColor(R.color.black));
					holder.operation.setTextColor(mResources.getColor(R.color.black));
					holder.component.setTextColor(mResources.getColor(R.color.black));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.black));
					holder.requiredQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.issuedQuantity.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView component;
			private TextView componentDescription;
			private TextView uom;
			private TextView operation;
			private TextView requiredQuantity;
			private TextView issuedQuantity;
		}
	}
}
