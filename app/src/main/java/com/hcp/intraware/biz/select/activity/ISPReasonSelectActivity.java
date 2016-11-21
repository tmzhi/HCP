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
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPReason;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPReasonSelectActivity extends ScannerActivity implements View.OnClickListener, View.OnKeyListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtName;
	private EditText mEdtDescription;

	private WORequestManager mRequestManager;

	private ReasonAdapter mAdapter = new ReasonAdapter();

	private List<ISPReason> mReasons = new ArrayList<ISPReason>();

	private int mSelectPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_reason_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_reason_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_reason_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mEdtName = (EditText) findViewById(R.id.edt_isp_reason_select_name);
		mEdtName.setOnKeyListener(this);

		mEdtDescription = (EditText) findViewById(R.id.edt_isp_reason_select_description);
		mEdtDescription.setOnKeyListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_reason_select_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		if(mReasons == null || mReasons.size() == 0){
			getISPReasons();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPReasons() {
		mMsg = null;

		final String name = mEdtName.getText().toString();
		final String description = mEdtDescription.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"原因信息", "正在获取原因信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPReason> reasons = null;
				try {
					reasons = mRequestManager
							.getISPReasons(name, description);
					mReasons = reasons;
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
			mToastHelper.show("请先选择原因!");
		}else{
			ISPReason item = mReasons.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_REASON_NAME, item.ReasonName);
			data.putExtra(ResultKey.KEY_REASON_DESCRIPTION, item.ReasonDescription);
			setResult(RequestCode.ISP_REASON, data);
			finish();
		}
	}

	private void refresh() {
		getISPReasons();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_reason_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_reason_select_refresh:
				refresh();
				break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			getISPReasons();
		}
		return false;
	}

	private class ReasonAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mReasons.size();
		}

		@Override
		public Object getItem(int position) {
			return mReasons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_reason_select, null);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.tv_item_isp_reason_name);
				holder.description = (TextView) convertView.findViewById(R.id.tv_item_isp_reason_description);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPReason item = mReasons.get(position);
			holder.name.setText(item.ReasonName);
			holder.description.setText(item.ReasonDescription);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.name.setTextColor(mResources.getColor(R.color.white));
					holder.description.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.name.setTextColor(mResources.getColor(R.color.black));
					holder.description.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView name;
			private TextView description;
		}
	}
}
