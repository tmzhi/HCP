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
import com.hcp.intraware.entity.ISPSubInventoryTransferLotNo;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPSubInventoryTransferLotNoSelectActivity extends ScannerActivity implements View.OnClickListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtLotno;

	private WORequestManager mRequestManager;

	private SubInventoryAdapter mAdapter = new SubInventoryAdapter();

	private List<ISPSubInventoryTransferLotNo> mLotNos = new ArrayList<ISPSubInventoryTransferLotNo>();

	private int mSelectPosition = -1;
	private String mOrganization;
	private String mComponent;
	private String mSubInventory;
	private String mLocator;
	private String mProjectNo;
	private int mRequestCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_subinventory_transfer_lot_no_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mEdtLotno = (EditText) findViewById(R.id.edt_isp_subinventory_transfer_lot_no_select_lot_no);
		mEdtLotno.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					getISPLotNos();
				}
				return false;
			}
		});

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_subinventory_transfer_lot_no_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_subinventory_transfer_lot_no_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_subinventory_transfer_lot_no_select_list);
		mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectPosition = position;
				mAdapter.notifyDataSetChanged();
			}
		});
		mLvList.setAdapter(mAdapter);

		mOrganization = getIntent().getStringExtra(RequestKey.ISP_ORGANIZATION);
		mComponent = getIntent().getStringExtra(RequestKey.ISP_COMPONENT);
		mSubInventory = getIntent().getStringExtra(RequestKey.ISP_SUBINVENTORY);
		mLocator = getIntent().getStringExtra(RequestKey.ISP_LOCATOR);
		mProjectNo = getIntent().getStringExtra(RequestKey.ISP_PROJECTNO);
	}

	@Override
	protected void onResume() {
		if(mLotNos == null || mLotNos.size() == 0){
			getISPLotNos();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPLotNos() {
		mMsg = null;

		final String lotno = mEdtLotno.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"批次信息", "正在获取批次信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPSubInventoryTransferLotNo> lots = null;
				try {
					lots = mRequestManager
							.getISPSubInventoryTransferLotNos(mOrganization, mComponent, mSubInventory, mLocator, mProjectNo, lotno);

						mLotNos = lots;
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
			mToastHelper.show("请先选择批次号!!");
		}else{
			ISPSubInventoryTransferLotNo item = mLotNos.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOT_NO, item.LotNo);
			data.putExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_REMAINING_QUANTITY, item.RemainingQuantity + "");
			setResult(RequestCode.ISP_SUBINVENTORY_TRANSFER_LOT_NO, data);
			finish();
		}
	}

	private void refresh() {
		getISPLotNos();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_subinventory_transfer_lot_no_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_subinventory_transfer_lot_no_select_refresh:
				refresh();
				break;
		}
	}

	private class SubInventoryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mLotNos.size();
		}

		@Override
		public Object getItem(int position) {
			return mLotNos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_subinventory_transfer_lot_no_select, null);

				holder = new ViewHolder();
				holder.lotno = (TextView) convertView.findViewById(R.id.tv_item_isp_subinventory_transfer_lot_no_select_lot_no);
				holder.remaingQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_subinventory_transfer_lot_no_select_remaining_quantity);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPSubInventoryTransferLotNo item = mLotNos.get(position);
			holder.lotno.setText(item.LotNo);
			holder.remaingQuantity.setText(item.RemainingQuantity + "");

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.lotno.setTextColor(mResources.getColor(R.color.white));
					holder.remaingQuantity.setTextColor(mResources.getColor(R.color.white));

				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.lotno.setTextColor(mResources.getColor(R.color.black));
					holder.remaingQuantity.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView lotno;
			private TextView remaingQuantity;
		}
	}
}
