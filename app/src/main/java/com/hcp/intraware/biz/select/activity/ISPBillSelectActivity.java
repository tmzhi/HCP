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
import com.hcp.intraware.entity.ISPBill;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPBillSelectActivity extends ScannerActivity implements View.OnClickListener, View.OnKeyListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtBill;
	private EditText mEdtComponent;
	private EditText mEdtComponentDescription;

	private WORequestManager mRequestManager;

	private LocatorAdapter mAdapter = new LocatorAdapter();

	private List<ISPBill> mBills = new ArrayList<ISPBill>();

	private int mSelectPosition = -1;

	private String mOrganization;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_bill_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_bill_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_bill_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mEdtBill = (EditText) findViewById(R.id.edt_isp_bill_select_bill);
		mEdtBill.setOnKeyListener(this);

		mEdtComponent = (EditText) findViewById(R.id.edt_isp_bill_select_component);
		mEdtComponent.setOnKeyListener(this);

		mEdtComponentDescription = (EditText) findViewById(R.id.edt_isp_bill_select_component_description);
		mEdtComponentDescription.setOnKeyListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_bill_select_list);
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
		if(mBills == null || mBills.size() == 0){
			getISPBills();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPBills() {
		mMsg = null;

		final String bill = mEdtBill.getText().toString();
		final String component = mEdtComponent.getText().toString();
		final String componentDescription = mEdtComponentDescription.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"单据号", "正在获取单据号信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPBill> subs = null;
				try {
					subs = mRequestManager
							.getISPBills(mOrganization, bill, component, componentDescription);

					mBills = subs;
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
			mToastHelper.show("请先选择单据号!");
		}else{
			ISPBill item = mBills.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_BILL_NAME, item.Bill);
			data.putExtra(ResultKey.KEY_BILL_ORGANIZATION, item.Organization);
			data.putExtra(ResultKey.KEY_BILL_COMPONENT, item.Component);
			data.putExtra(ResultKey.KEY_BILL_COMPONENT_DESCRIPTION, item.ComponentDescription);
			data.putExtra(ResultKey.KEY_BILL_UOM, item.Uom);
			data.putExtra(ResultKey.KEY_BILL_BATCH, item.LotNo);
			data.putExtra(ResultKey.KEY_BILL_PROJECT_NO, item.ProjectNo);
			data.putExtra(ResultKey.KEY_BILL_TRANSACTION_QUANTITY, item.TransactionQuantity + "");
			data.putExtra(ResultKey.KEY_BILL_RECEIVED_QUANTITY, item.ReceivedQuantity + "");
			data.putExtra(ResultKey.KEY_BILL_REMAINING_QUANTITY, item.RemainingQuantity + "");
			setResult(RequestCode.ISP_BILL, data);
			finish();
		}
	}

	private void refresh() {
		getISPBills();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_bill_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_bill_select_refresh:
				refresh();
				break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_ENTER){
			getISPBills();
		}
		return false;
	}

	private class LocatorAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mBills.size();
		}

		@Override
		public Object getItem(int position) {
			return mBills.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_bill_select, null);

				holder = new ViewHolder();
				holder.bill = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_bill);
				holder.organization = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_organization);
				holder.component = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_component);
				holder.description = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_component_description);
				holder.uom = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_uom);
				holder.lotno = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_lotno);
				holder.projectno = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_project_no);
				holder.transactionQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_transaction_quantity);
				holder.receivedQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_received_quantity);
				holder.remainingQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_bill_select_remaining_quantity);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPBill item = mBills.get(position);
			holder.bill.setText(item.Bill);
			holder.organization.setText(item.Organization);
			holder.component.setText(item.Component);
			holder.description.setText(item.ComponentDescription);
			holder.uom.setText(item.Uom);
			holder.lotno.setText(item.LotNo);
			holder.projectno.setText(item.ProjectNo);
			holder.transactionQuantity.setText(item.TransactionQuantity + "");
			holder.receivedQuantity.setText(item.ReceivedQuantity + "");
			holder.remainingQuantity.setText(item.RemainingQuantity + "");

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.bill.setTextColor(mResources.getColor(R.color.white));
					holder.organization.setTextColor(mResources.getColor(R.color.white));
					holder.component.setTextColor(mResources.getColor(R.color.white));
					holder.description.setTextColor(mResources.getColor(R.color.white));
					holder.uom.setTextColor(mResources.getColor(R.color.white));
					holder.lotno.setTextColor(mResources.getColor(R.color.white));
					holder.projectno.setTextColor(mResources.getColor(R.color.white));
					holder.transactionQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.receivedQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.remainingQuantity.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.bill.setTextColor(mResources.getColor(R.color.black));
					holder.organization.setTextColor(mResources.getColor(R.color.black));
					holder.component.setTextColor(mResources.getColor(R.color.black));
					holder.description.setTextColor(mResources.getColor(R.color.black));
					holder.uom.setTextColor(mResources.getColor(R.color.black));
					holder.lotno.setTextColor(mResources.getColor(R.color.black));
					holder.projectno.setTextColor(mResources.getColor(R.color.black));
					holder.transactionQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.receivedQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.remainingQuantity.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView bill;
			private TextView organization;
			private TextView component;
			private TextView description;
			private TextView uom;
			private TextView lotno;
			private TextView projectno;
			private TextView transactionQuantity;
			private TextView receivedQuantity;
			private TextView remainingQuantity;
		}
	}
}
