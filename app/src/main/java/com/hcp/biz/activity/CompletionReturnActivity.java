package com.hcp.biz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hcp.biz.entities.CompletionReturnInfo;
import com.hcp.biz.query.activity.CompletionReturnQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.CompletionReturnInfoDao;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.SegmentRemaining;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WipTransactionCompletion;
import com.hcp.entities.WorkOrder3;
import com.hcp.http.RequestUtil;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.util.AppCache;
import com.hcp.util.AppConfig;
import com.hcp.util.PrintManager;
import com.hcp.wo.R;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CompletionReturnActivity extends ScannerActivity implements TextWatcher {
	private final int DO_REQUEST_SUBMIT = 0;
	private final int DO_REQUEST_GET_WORKORDER = 1;

	private EditText mEdtSubInventory;
	private EditText mEdtWipName;
	private EditText mEdtTransactionQty;

	private TextView mTvClassCode;
	private TextView mTvStartTime;
	private TextView mTvCompleteTime;
	private TextView mTvAssembly;
	private TextView mTvAssemblyName;
	private TextView mTvCompletedQty;
	private TextView mTvRestQty;
	private TextView mTvRemaining;

	private Button mBtnSubmit;

	private ImageButton mImbQuery;

	private WORequestManager mRequestManager;

	private ProgressDialog mProgressDialog;

	private WorkOrder3 mCurrentWorkOrder;
	private List<SegmentRemaining> mRemainings;

	private ToastHelper mToastHelper;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_completion_return);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mEdtSubInventory = (EditText) findViewById(R.id.edt_comp_return_sub_inventory);
		mEdtSubInventory.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtSubInventory.addTextChangedListener(this);
		mEdtSubInventory.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus && !hasSubinventoryAuthority(mEdtSubInventory.getText().toString())){
					new AlertDialog.Builder(mInstance)
							.setMessage("该子库未在授权子库中!")
							.setPositiveButton("确定", null)
							.create()
							.show();

					mEdtSubInventory.setText(StringUtils.EMPTY);
				}
			}
		});


		mEdtWipName = (EditText) findViewById(R.id.edt_comp_return_ori_code);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtWipName.addTextChangedListener(this);
		mEdtWipName.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getWorkOrder();
				}

			}
		});

		mEdtTransactionQty = (EditText) findViewById(R.id.edt_comp_return_transcation_qty);
		mEdtTransactionQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvClassCode = (TextView) findViewById(R.id.tv_comp_return_class_code);
		mTvStartTime = (TextView) findViewById(R.id.tv_comp_return_start_time);
		mTvCompleteTime = (TextView) findViewById(R.id.tv_comp_return_complete_time);
		mTvAssembly = (TextView) findViewById(R.id.tv_comp_return_assembly);
		mTvAssemblyName = (TextView) findViewById(R.id.tv_comp_return_assembly_name);
		mTvCompletedQty = (TextView) findViewById(R.id.tv_comp_return_completed_qty);
		mTvRestQty = (TextView) findViewById(R.id.tv_comp_return_rest_qty);
		mTvRemaining = (TextView) findViewById(R.id.tv_comp_return_remaining);

		mBtnSubmit = (Button) findViewById(R.id.btn_comp_return_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_comp_return_query);
		mImbQuery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, CompletionReturnQueryActivity.class);
				mInstance.startActivity(intent);
			}
		});

		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);
	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private void submit() {
		if (mCurrentWorkOrder == null) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String transactionQtyStr = mEdtTransactionQty.getText().toString();
		if (TextUtils.isEmpty(transactionQtyStr)) {
			mToastHelper.show("请输入【退回数量】!");
			return;
		}

		BigDecimal remainingQty = new BigDecimal(mTvRemaining.getText().toString());
		BigDecimal transactionQty = new BigDecimal(transactionQtyStr);
		if(transactionQty.compareTo(remainingQty)>0){
			mToastHelper.show("【退回数量】不得多于【现有量】!");
			return;
		}

		String subInventory = mEdtSubInventory.getText().toString();
		if (TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("请输入【子库】!");
			return;
		}

		final WipTransactionCompletion wip = new WipTransactionCompletion();
		wip.Locator = mRemainings.get(0).Locator;
		wip.LotNumber = mRemainings.get(0).LotNumber;
		wip.Organization = mRemainings.get(0).OrganizationCode;
		wip.ProjectNumber = mRemainings.get(0).ProjectNumber;
		wip.SubInventory = mRemainings.get(0).SubInventory;
		wip.OverCompletion = "N";
		wip.Reason = "Trx From Mobile";
		wip.Reference = Device.getIMEI(this);
		wip.WipEntityName = mEdtWipName.getText().toString();
		wip.TransactionQuantity = transactionQty;
		wip.TransactionType = "WIP Completion Return";

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "完工退回提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getSuccessMsg();
		final String wipName = mEdtWipName.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_SUBMIT;
				try {
					String result = mRequestManager.submitTransactionComp(wip);

					msg.obj = result + "";

					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){

						PrintManager manager = PrintManager.getInstance(mInstance);
						manager.connect();
						manager.print(transMsg.getBytes("GB2312"));
						manager.printOneDimenBarcode(wipName);
						manager.print("\n------------------------\n\n\n\n".getBytes());
						manager.close();
					}
				} catch (Exception e) {

				}
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}

			switch (msg.what) {
				case DO_REQUEST_GET_WORKORDER:
					if (msg.obj != null) {

						List<Object> results = (List<Object>) msg.obj;

						mCurrentWorkOrder = (WorkOrder3) results.get(0);
						mRemainings = (List<SegmentRemaining>) results.get(1);

						mTvClassCode.setText(mCurrentWorkOrder.ClassCode);
						mTvStartTime.setText(mCurrentWorkOrder.ScheduledStartDate);
						mTvCompleteTime
								.setText(mCurrentWorkOrder.ScheduledCompletionDate);
						mTvAssembly.setText(mCurrentWorkOrder.ConcatenatedSegments);
						mTvAssemblyName.setText(mCurrentWorkOrder.Description);
						mTvCompletedQty.setText(mCurrentWorkOrder.QuantityCompleted
								+ "");
						mTvCompleteTime
								.setText(mCurrentWorkOrder.ScheduledCompletionDate
										+ "");
						mTvRemaining.setText(mCurrentWorkOrder.Remark);
						mTvRestQty
								.setText(mCurrentWorkOrder.QuantityRemaining + "");
						mTvRemaining.setText(mRemainings.get(0).Remaining + "");

						BigDecimal transactionQty =
								mCurrentWorkOrder.QuantityRemaining.compareTo(mRemainings.get(0).Remaining)>0 ?
										mRemainings.get(0).Remaining : mCurrentWorkOrder.QuantityRemaining;
						mEdtTransactionQty
								.setText(transactionQty + "");

					} else {
						mToastHelper.show("工单或现有量获取失败!");
					}
					break;
				case DO_REQUEST_SUBMIT:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(saveReturns());

							clearValues();

							mEdtWipName.requestFocus();

						} else if(RequestUtil.RESPONSE_MSG_ERROR_AUTHORITY.equalsIgnoreCase(result)){
							mToastHelper.show(mResources.getString(R.string.request_error_authority));
						}else {
							mToastHelper.show(result);
						}
					}
					break;
			}

			return false;
		}
	});

	private void clearValues() {
		mTvClassCode.setText("");
		mTvCompleteTime.setText("");
		mTvStartTime.setText("");
		mTvAssembly.setText("");
		mTvAssemblyName.setText("");
		mTvCompletedQty.setText("");
		mTvRemaining.setText("");
		mTvRestQty.setText("");
		mTvStartTime.setText("");

		mCurrentWorkOrder = null;
	}

	private CompletionReturnInfo saveReturns(){
		CompletionReturnInfo returnInfo = new CompletionReturnInfo();

		returnInfo.assembly_code = mTvAssembly.getText().toString();
		returnInfo.assembly_name = mTvAssemblyName.getText().toString();
		returnInfo.class_code = mTvClassCode.getText().toString();
		try {
			returnInfo.completion_time = DateFormat.defaultParse(mTvCompleteTime.getText().toString());
			returnInfo.start_time = DateFormat.defaultParse(mTvStartTime.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		returnInfo.complition_quantity = new BigDecimal(mTvCompletedQty.getText().toString());
		returnInfo.create_by = AppCache.getInstance().getLoginUser();
		returnInfo.create_time = new Date(System.currentTimeMillis());
		returnInfo.remaining_quantity = new BigDecimal(mTvRestQty.getText().toString());
		returnInfo.remark = "";
		returnInfo.sub_inventory = mEdtSubInventory.getText().toString();
		returnInfo.transaction_quantity = new BigDecimal(mEdtTransactionQty.getText().toString());
		returnInfo.wip_entity_name = mEdtWipName.getText().toString();

		try {
			CompletionReturnInfoDao.getInstance(mInstance).onInsert().insertCompletionReturn(returnInfo);
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}

		return returnInfo;
	}

	private void showTransSuccessMsg(CompletionReturnInfo returnInfo) {

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(getSuccessMsg().toString()).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private String getSuccessMsg(){

		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("完 工 退 回\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("子库:" + mEdtSubInventory.getText().toString() + "\n");
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append("工单类型:"+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append("开始时间:"+ mTvStartTime.getText().toString() + "\n");
		stbMsg.append("完成时间:" + mTvCompleteTime.getText().toString() + "\n");

		stbMsg.append("制品号码:" + mTvAssembly.getText().toString() + "\n");
		stbMsg.append("制品名称:" + mTvAssemblyName.getText().toString() + "\n");
		stbMsg.append("已完工:" + mTvCompletedQty.getText().toString() + "\n");
		stbMsg.append("剩余数量:" + mTvRemaining.getText().toString() + "\n");
		stbMsg.append("工单备注:\n");
		stbMsg.append("完工数量:" + mEdtTransactionQty.getText().toString() + "\n");

		return stbMsg.toString();
	}

	@Override
	protected void decodeCallback(String barcode) {
		if (mEdtSubInventory.hasFocus()) {
			mEdtSubInventory.setText(barcode);
			mEdtWipName.requestFocus();
		} else if (mEdtWipName.hasFocus()) {
			mEdtWipName.setText(barcode);
			getWorkOrder();
		}
	}

	private void getWorkOrder() {
		final String wipEntityName = mEdtWipName.getText().toString();
		final String subInventory = mEdtSubInventory.getText().toString();

		if (TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("【子库】不得为空!");
			return;
		}

		if (TextUtils.isEmpty(wipEntityName)) {
			mToastHelper.show("【工单号】不得为空!");
			return;
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"工单获取", "正在获取工单");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				Message msg = Message.obtain();
				msg.what = DO_REQUEST_GET_WORKORDER;

				List<Object> results = new ArrayList<Object>();
				try {
					WorkOrder3 workOrder = mRequestManager
							.getWorkOrderOnCompletion(wipEntityName);
					if(workOrder != null){

						List<SegmentRemaining> remaining = mRequestManager.getSegmentRemaing(workOrder.ConcatenatedSegments, subInventory, null, workOrder.ProjectNumber == null ? "" : workOrder.ProjectNumber);

						if(remaining != null){

							results.add(workOrder);
							results.add(remaining);

							msg.obj = results;

						}


					}
				} catch (Exception e) {

				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	public void afterTextChanged(Editable s) {
		clearValues();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}
}
