package com.hcp.biz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.entities.WorkOrder2;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BatchTransactionActivity extends BaseBatchActivity{

	private TextView mTvTitle;
	private EditText mEdtWipComponent;

	private ListView mListView;

	private List<WorkOrder2> mWorkOrders = new ArrayList<WorkOrder2>();

	private SparseArray<TransactionResult> mResultsMapping = new SparseArray<TransactionResult>();

	private ComponentsAdapter mAdapter = new ComponentsAdapter();

	private ProgressDialog mProgressDialog;

	private WORequestManager mRequestManager;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private int mCurrentTransactionType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_batch_transaction);

		mRequestManager = WORequestManager.getInstance(mInstance);

		mTvTitle = (TextView) findViewById(R.id.tv_bt_title);
		mCurrentTransactionType = getIntent().getIntExtra(REQUEST_TRANSACTION_TYPE, -1);
		if(mCurrentTransactionType == TYPE_TRANSACTION_ISSUE){
			mTvTitle.setText("批 量 领 料");
		}else if(mCurrentTransactionType == TYPE_TRANSACTION_RETURN){
			mTvTitle.setText("批 量 退 料");
		}


		mEdtWipComponent = (EditText) findViewById(R.id.edt_bt_wip_name);
		mEdtWipComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtWipComponent.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					doGetWorkOrder(mEdtWipComponent.getText().toString());
				}
			}
		});

		mListView = (ListView) findViewById(R.id.lv_bt_component_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

				if(mResultsMapping.get(position, null) == null){
					doTransaction(position);
				}
			}
		});
	}

	private void doTransaction(int position){
		WorkOrder2 workOrder = mWorkOrders.get(position);

		Class<?> requestCls = null;
		if(mCurrentTransactionType == TYPE_TRANSACTION_ISSUE){
			requestCls = MaterialIssueActivity.class;
		}else if(mCurrentTransactionType == TYPE_TRANSACTION_RETURN){
			requestCls = MaterialReturnActivity.class;
		}

		Intent intent = new Intent(mInstance, requestCls);
		intent.putExtra(REQUEST_ON_BATCH, true);
		intent.putExtra(REQUEST_WIP_NAME, workOrder.WipEntityName);
		intent.putExtra(REQUEST_COMPONENT_CODE, workOrder.Segment1);
		intent.putExtra(REQUEST_ID, position);

		startActivityForResult(intent, mCurrentTransactionType);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == STATE_SUBMITTED){
			int position = data.getIntExtra(BatchTransactionActivity.REQUEST_ID, -1);
			if(position != -1){
				TransactionResult result = mResultsMapping.get(position, null);
				if(result == null){
					result = new TransactionResult();
					result.state = resultCode;
					result.transaction_quantity = (BigDecimal) data.getSerializableExtra(REQUEST_TRANSACTION_QUANTITY);
					mResultsMapping.put(position, result);

				}else{
					result.state = resultCode;
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	private class ComponentsAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mWorkOrders.size();
		}

		@Override
		public Object getItem(int position) {
			return mWorkOrders.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;

			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.component_list_item, null);

				viewHolder = new ViewHolder();
				viewHolder.component_code = (TextView) convertView.findViewById(R.id.tv_cli_component_code);
				viewHolder.component_name = (TextView) convertView.findViewById(R.id.tv_cli_component_name);
				viewHolder.transaction_qty = (TextView) convertView.findViewById(R.id.tv_cli_transaction_qty);
				viewHolder.state_done = (ImageView) convertView.findViewById(R.id.img_cli_done);

				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			WorkOrder2 workOrder = mWorkOrders.get(position);
			viewHolder.component_code.setText(workOrder.Segment1);
			viewHolder.component_name.setText(workOrder.ItemDescription);

			TransactionResult result = mResultsMapping.get(position, null);
			if(result != null){
				viewHolder.transaction_qty.setText(result.transaction_quantity + "");

				if(result.state == STATE_SUBMITTED){
					convertView.setBackgroundColor(0xFFBBFFFF);
					viewHolder.state_done.setVisibility(View.VISIBLE);
				}else{
					viewHolder.state_done.setVisibility(View.GONE);
				}
			}

			return convertView;
		}

		private class ViewHolder{
			TextView component_code;
			TextView component_name;
			TextView transaction_qty;
			ImageView state_done;
		}

	}

	private void doGetWorkOrder(final String orderCode) {

		if(TextUtils.isEmpty(orderCode)){
			mToastHelper.show("工单号不得为空");
			return;
		}

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
		}
		mProgressDialog.setMessage("工单获取中...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					List<WorkOrder2> workOrders = mRequestManager.getWorkOrderOnQuery(orderCode, null, null, false);
					msg.obj = workOrders;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {

			if(mProgressDialog != null){
				mProgressDialog.cancel();
			}

			if(msg.obj != null){
				mWorkOrders = (List<WorkOrder2>) msg.obj;

				mAdapter.notifyDataSetChanged();
			}else{
				mToastHelper.show("工单获取失败!");
			}

			return false;
		}
	});


	@Override
	protected void decodeCallback(String barcode) {
		mEdtWipComponent.setText(barcode);

		doGetWorkOrder(barcode);
	}
}
