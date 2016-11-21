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
import com.hcp.intraware.entity.ISPWipReturnLocator;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPWipReturnLocatorSelectActivity extends ScannerActivity implements View.OnClickListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtLocator;

	private WORequestManager mRequestManager;

	private SubInventoryAdapter mAdapter = new SubInventoryAdapter();

	private List<ISPWipReturnLocator> mLocators = new ArrayList<ISPWipReturnLocator>();

	private int mSelectPosition = -1;
	private String mOrganization;
	private String mSubInventory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_wip_return_locator_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mEdtLocator = (EditText) findViewById(R.id.edt_isp_wip_return_locator_select_locator);
		mEdtLocator.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					getISPLocator();
				}
				return false;
			}
		});

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_wip_return_locator_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_wip_return_locator_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_wip_return_locator_select_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if(TextUtils.isEmpty(mLocators.get(position).SubInventory)){
					return;
				}

				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);

		mOrganization = getIntent().getStringExtra(RequestKey.ISP_ORGANIZATION);
		mSubInventory = getIntent().getStringExtra(RequestKey.ISP_SUBINVENTORY);
	}

	@Override
	protected void onResume() {
		if(mLocators == null || mLocators.size() == 0){
			getISPLocator();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPLocator() {
		mMsg = null;

		final String locator = mEdtLocator.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"货位信息", "正在获取货位信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPWipReturnLocator> subs = null;
				try {
					subs = mRequestManager
							.getISPWipReturnLocators(mOrganization, mSubInventory, locator);

						mLocators = subs;
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
			mToastHelper.show("请先选择子库!");
		}else{
			ISPWipReturnLocator item = mLocators.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_WIP_RETURN_LOCATOR, item.Locator);
			setResult(RequestCode.ISP_WIP_RETURN_LOCATOR, data);
			finish();
		}
	}

	private void refresh() {
		getISPLocator();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_wip_return_locator_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_wip_return_locator_select_refresh:
				refresh();
				break;
		}
	}

	private class SubInventoryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mLocators.size();
		}

		@Override
		public Object getItem(int position) {
			return mLocators.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_wip_return_locator_select, null);

				holder = new ViewHolder();
				holder.subinventory = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_return_locator_select_subinventory);
				holder.locator = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_return_locator_select_locator);
				holder.locatorDescription = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_return_locator_select_locatordescription);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPWipReturnLocator item = mLocators.get(position);
			holder.subinventory.setText(item.SubInventory);
			holder.locator.setText(item.Locator);
			holder.locatorDescription.setText(item.LocatorDescription);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.subinventory.setTextColor(mResources.getColor(R.color.white));
					holder.locator.setTextColor(mResources.getColor(R.color.white));
					holder.locatorDescription.setTextColor(mResources.getColor(R.color.white));

				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.subinventory.setTextColor(mResources.getColor(R.color.black));
					holder.locator.setTextColor(mResources.getColor(R.color.black));
					holder.locatorDescription.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView subinventory;
			private TextView locator;
			private TextView locatorDescription;
		}
	}
}
