package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
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
import com.hcp.intraware.entity.ISPWipQueryItem;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPWipQueryActivity extends ScannerActivity implements View.OnClickListener{

	private ListView mLvList;
	private Button mBtnRefresh;
	private EditText mEdtWip;

	private WORequestManager mRequestManager;

	private LocatorAdapter mAdapter = new LocatorAdapter();

	private List<ISPWipQueryItem> mWips = new ArrayList<ISPWipQueryItem>();

	private int mSelectPosition = -1;

	private String mOrganization;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_wip_query);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_wip_query_refresh);
		mBtnRefresh.setOnClickListener(this);

		mEdtWip = (EditText) findViewById(R.id.edt_isp_wip_query_wip_required_or_produce);
		mEdtWip.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					getISPWipQueries();
				}
				return false;
			}
		});

		mLvList = (ListView) findViewById(R.id.lv_isp_wip_query_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);

		mOrganization = AppConfig.getInstance(mInstance).getOrganizationCode();

	}

	@Override
	protected void onResume() {
		if(mWips == null || mWips.size() == 0){
			getISPWipQueries();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPWipQueries() {

		final String wip = mEdtWip.getText().toString();

		mMsg = null;

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"工单", "正在获取工单对比...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPWipQueryItem> subs = null;
				try {
					subs = mRequestManager
							.getISPWipQueryItems(wip);
					mWips = subs;
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

	private void refresh() {
		getISPWipQueries();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_wip_query_refresh:
				refresh();
				break;
		}
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
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_wip_query, null);

				holder = new ViewHolder();
				holder.produceOrg = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_produce_organization);
				holder.produceWip = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_produce_wip);
				holder.producePro = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_produce_project_no);
				holder.so = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_so);
				holder.requiredOrg = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_required_organization);
				holder.requiredPro = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_required_project_no);
				holder.requiredWip = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_required_wip);
				holder.pr = (TextView) convertView.findViewById(R.id.tv_item_isp_wip_query_pr);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPWipQueryItem item = mWips.get(position);
			holder.produceOrg.setText(item.ProduceOrganization);
			holder.producePro.setText(item.ProduceProjectNo);
			holder.produceWip.setText(item.ProduceWip);
			holder.so.setText(item.So);
			holder.requiredOrg.setText(item.RequiredOrganization);
			holder.requiredPro.setText(item.RequiredProjectNo);
			holder.requiredWip.setText(item.RequiredWip);
			holder.pr.setText(item.Pr);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.produceOrg.setTextColor(mResources.getColor(R.color.white));
					holder.producePro.setTextColor(mResources.getColor(R.color.white));
					holder.produceWip.setTextColor(mResources.getColor(R.color.white));
					holder.so.setTextColor(mResources.getColor(R.color.white));
					holder.requiredOrg.setTextColor(mResources.getColor(R.color.white));
					holder.requiredPro.setTextColor(mResources.getColor(R.color.white));
					holder.requiredWip.setTextColor(mResources.getColor(R.color.white));
					holder.so.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.produceOrg.setTextColor(mResources.getColor(R.color.black));
					holder.producePro.setTextColor(mResources.getColor(R.color.black));
					holder.produceWip.setTextColor(mResources.getColor(R.color.black));
					holder.so.setTextColor(mResources.getColor(R.color.black));
					holder.requiredOrg.setTextColor(mResources.getColor(R.color.black));
					holder.requiredPro.setTextColor(mResources.getColor(R.color.black));
					holder.requiredWip.setTextColor(mResources.getColor(R.color.black));
					holder.so.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView produceOrg;
			private TextView produceWip;
			private TextView producePro;
			private TextView so;
			private TextView requiredOrg;
			private TextView requiredWip;
			private TextView requiredPro;
			private TextView pr;
		}
	}
}
