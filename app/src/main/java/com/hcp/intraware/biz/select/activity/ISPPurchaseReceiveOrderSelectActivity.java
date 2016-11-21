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
import com.hcp.intraware.entity.ISPPurchaseReceiveOrder;
import com.hcp.wo.R;

import java.util.ArrayList;
import java.util.List;

public class ISPPurchaseReceiveOrderSelectActivity extends ScannerActivity implements View.OnClickListener{

	private ListView mLvList;
	private Button mBtnConfirm;
	private Button mBtnRefresh;

	private EditText mEdtOrder;

	private WORequestManager mRequestManager;

	private LocatorAdapter mAdapter = new LocatorAdapter();

	private List<ISPPurchaseReceiveOrder> mOrders = new ArrayList<ISPPurchaseReceiveOrder>();

	private int mSelectPosition = -1;

	private String mOrganization;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_isp_purchase_receive_order_select);

		initView();

	}

	private void initView(){

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(mInstance);
		mToastHelper = ToastHelper.getInstance(mInstance);

		mEdtOrder = (EditText) findViewById(R.id.edt_isp_purchase_receive_order_select_order);
		mEdtOrder.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					getISPOrders();
				}
				return false;
			}
		});

		mBtnConfirm = (Button) findViewById(R.id.btn_isp_purchase_receive_order_select_confirm);
		mBtnConfirm.setOnClickListener(this);

		mBtnRefresh = (Button) findViewById(R.id.btn_isp_purchase_receive_order_select_refresh);
		mBtnRefresh.setOnClickListener(this);

		mLvList = (ListView) findViewById(R.id.lv_isp_purchase_receive_order_select_list);
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
		if(mOrders == null || mOrders.size() == 0){
			getISPOrders();
		}
		super.onResume();
	}

	private ProgressDialog mProgressDialog;
	private String mMsg;
	private void getISPOrders() {
		mMsg = null;

		final String order = mEdtOrder.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"订单", "正在获取订单信息...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<ISPPurchaseReceiveOrder> orders = null;
				try {
					orders = mRequestManager
							.getISPPurchaseReceiveOrders(mOrganization, order);

						mOrders = orders;
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
			mToastHelper.show("请先选择订单号!");
		}else{
			ISPPurchaseReceiveOrder item = mOrders.get(mSelectPosition);
			Intent data = new Intent();
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER, item.Order);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_LINE, item.Line);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_SHIPMENT, item.Shipment);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_SHIPMENT_LINE, item.ShipmentLine);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_COMPONENT, item.Component);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_COMPONENT_DESCRIPTION, item.ComponentDescription);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_UOM, item.Uom);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_RECEIVED_QUANTITY, item.ReceivedQuantity + "");
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_UNRECEIVED_QUANTITY, item.UnReceivedQuantity + "");
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_PURCHASE_QUANTITY, item.PurchaseQuantity + "");
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_PROJECT_NO, item.ProjectNo);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_LOTNO, item.LotNo);
			data.putExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_WIP, item.Wip);
			setResult(RequestCode.ISP_PURCHASE_RECEIVE_ORDER, data);
			finish();
		}
	}

	private void refresh() {
		getISPOrders();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_isp_purchase_receive_order_select_confirm:
				confirm();
				break;
			case R.id.btn_isp_purchase_receive_order_select_refresh:
				refresh();
				break;
		}
	}

	private class LocatorAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mOrders.size();
		}

		@Override
		public Object getItem(int position) {
			return mOrders.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_isp_purchase_receive_order_select, null);

				holder = new ViewHolder();
				holder.order = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_order);
				holder.line = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_line);
				holder.shipment = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_shipment);
				holder.shipmentLine = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_shipment_line);
				holder.component = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_component);
				holder.componentDescription = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_component_description);
				holder.uom = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_uom);
				holder.receivedQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_received_quantity);
				holder.unReceivedQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_unreceived_quantity);
				holder.purchaseQuantity = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_purchase_quantity);
				holder.lotno = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_lotno);
				holder.projectno = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_project_no);
				holder.wip = (TextView) convertView.findViewById(R.id.tv_item_isp_purchase_receive_order_select_wip);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ISPPurchaseReceiveOrder item = mOrders.get(position);
			holder.order.setText(item.Order);
			holder.line.setText(item.Line);
			holder.shipment.setText(item.Shipment);
			holder.shipmentLine.setText(item.Shipment);
			holder.component.setText(item.Component);
			holder.componentDescription.setText(item.ComponentDescription);
			holder.uom.setText(item.Uom);
			holder.receivedQuantity.setText(item.ReceivedQuantity + "");
			holder.unReceivedQuantity.setText(item.UnReceivedQuantity + "");
			holder.purchaseQuantity.setText(item.PurchaseQuantity + "");
			holder.lotno.setText(item.LotNo);
			holder.projectno.setText(item.ProjectNo);
			holder.wip.setText(item.ProjectNo);

			if(mSelectPosition > -1){
				if(mSelectPosition == position){
					convertView.setBackgroundColor(mResources.getColor(R.color.cyan900));
					holder.order.setTextColor(mResources.getColor(R.color.white));
					holder.line.setTextColor(mResources.getColor(R.color.white));
					holder.shipment.setTextColor(mResources.getColor(R.color.white));
					holder.shipmentLine.setTextColor(mResources.getColor(R.color.white));
					holder.component.setTextColor(mResources.getColor(R.color.white));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.white));
					holder.uom.setTextColor(mResources.getColor(R.color.white));
					holder.receivedQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.unReceivedQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.purchaseQuantity.setTextColor(mResources.getColor(R.color.white));
					holder.lotno.setTextColor(mResources.getColor(R.color.white));
					holder.projectno.setTextColor(mResources.getColor(R.color.white));
					holder.wip.setTextColor(mResources.getColor(R.color.white));
				}else{
					convertView.setBackgroundColor(mResources.getColor(R.color.float_transparent));
					holder.order.setTextColor(mResources.getColor(R.color.black));
					holder.line.setTextColor(mResources.getColor(R.color.black));
					holder.shipment.setTextColor(mResources.getColor(R.color.black));
					holder.shipmentLine.setTextColor(mResources.getColor(R.color.black));
					holder.component.setTextColor(mResources.getColor(R.color.black));
					holder.componentDescription.setTextColor(mResources.getColor(R.color.black));
					holder.uom.setTextColor(mResources.getColor(R.color.black));
					holder.receivedQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.unReceivedQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.purchaseQuantity.setTextColor(mResources.getColor(R.color.black));
					holder.lotno.setTextColor(mResources.getColor(R.color.black));
					holder.projectno.setTextColor(mResources.getColor(R.color.black));
					holder.wip.setTextColor(mResources.getColor(R.color.black));
				}
			}

			return convertView;
		}

		private class ViewHolder{
			private TextView order;
			private TextView line;
			private TextView shipment;
			private TextView shipmentLine;
			private TextView component;
			private TextView componentDescription;
			private TextView uom;
			private TextView receivedQuantity;
			private TextView unReceivedQuantity;
			private TextView purchaseQuantity;
			private TextView lotno;
			private TextView projectno;
			private TextView wip;
		}
	}
}
