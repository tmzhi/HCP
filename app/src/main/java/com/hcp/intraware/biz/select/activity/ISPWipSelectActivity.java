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
import com.hcp.intraware.entity.ISPWip;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPWipSelectActivity extends ScannerActivity implements View.OnClickListener, View.OnKeyListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtWip;
	private EditText mEdtComponent;
	private EditText mEdtComponentDescription;

	private WORequestManager mRequestManager;

	private LocatorAdapter mAdapter = new LocatorAdapter();

	private List<ISPWip> mWips = new ArrayList<ISPWip>();

	private int mSelectPosition = -1;

	private String mOrganization;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_wip_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mEdtWip = (EditText) findViewById(R.id.edt_isp_wip_select_wip);
		mEdtWip.setOnKeyListener(this);

		mEdtComponent = (EditText) findViewById(R.id.edt_isp_wip_select_component);
		mEdtComponent.setOnKeyListener(this);

		mEdtComponentDescription = (EditText) findViewById(R.id.edt_isp_wip_select_component_description);
		mEdtComponentDescription.setOnKeyListener(this);

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_wip_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_wip_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_wip_select_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);

		mOrganization = getIntent().getStringExtra(RequestKey.ISP_ORGANIZATION);

	}

	@Override
	protected void onResume() {
		if(mWips == null || mWips.size() == 0){
			getISPWips();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPWips() {
		mMsg = null;

		final String wip = mEdtWip.getText().toString();
		final String component = mEdtComponent.getText().toString();
		final String componentDescription = mEdtComponentDescription.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"工单", "正在获取工单信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPWip> wips = null;
				try {
					wips = mRequestManager
							.getISPWips(mOrganization, wip, component, componentDescription);

					mWips = wips;
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
			mToastHelper.show("请先选择工单!");
		}else{
			ISPWip item = mWips.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_WIP, item.Wip);
			data.putExtra(ResultKey.KEY_WIP_COMPONENT, item.Component);
			data.putExtra(ResultKey.KEY_WIP_COMPONENT_DESCRIPTION, item.ComponentDescription);
			data.putExtra(ResultKey.KEY_WIP_TYPE, item.WipType);
			data.putExtra(ResultKey.KEY_WIP_STATUS, item.WipStatus);
			data.putExtra(ResultKey.KEY_WIP_PROJECT_NO, item.ProjectNo);
			setResult(RequestCode.ISP_WIP, data);
			finish();
		}
	}

	private void refresh() {
		getISPWips();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_wip_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_wip_select_refresh:
				refresh();
				break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			getISPWips();
		}
		return false;
	}

	private class LocatorAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mWips.size();
		}

		@Override
		public Object getItem(int position) {
			return mWips.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_wip_select, null);

				holder = new ViewHolder();
				holder.wip = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_wip);
				holder.component = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_component);
				holder.componentDescription = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_component_name);
				holder.wipType = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_wip_type);
				holder.wipStatus = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_wip_status);
				holder.projectno = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_select_project_no);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPWip item = mWips.get(position);
			holder.wip.setText(item.Wip);
			holder.component.setText(item.Component);
			holder.componentDescription.setText(item.ComponentDescription);
			holder.wipType.setText(item.WipType);
			holder.wipStatus.setText(item.WipStatus);
			holder.projectno.setText(item.ProjectNo);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.wip.setTextColor(mResources.getColor(R.color.white));
					holder.component.setTextColor(mResources.getColor(R.color.white));
					holder.wipType.setTextColor(mResources.getColor(R.color.white));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.white));
					holder.wipStatus.setTextColor(mResources.getColor(R.color.white));
					holder.projectno.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.wip.setTextColor(mResources.getColor(R.color.black));
					holder.wipType.setTextColor(mResources.getColor(R.color.black));
					holder.component.setTextColor(mResources.getColor(R.color.black));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.black));
					holder.wipStatus.setTextColor(mResources.getColor(R.color.black));
					holder.projectno.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView wip;
			private TextView component;
			private TextView componentDescription;
			private TextView projectno;
			private TextView wipType;
			private TextView wipStatus;
		}
	}
}
