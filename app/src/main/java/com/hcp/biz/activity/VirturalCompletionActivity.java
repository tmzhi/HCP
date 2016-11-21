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

import com.hcp.biz.query.activity.CompletionQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.ProcedureTransfer;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WipTransactionCompletion;
import com.hcp.entities.WorkOrder;
import com.hcp.entities.WorkOrder3;
import com.hcp.http.RequestUtil;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.AppCache;
import com.hcp.util.AppConfig;
import com.hcp.util.PrintManager;
import com.hcp.wo.R;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VirturalCompletionActivity extends ScannerActivity{
	private final int DO_REQUEST_SUBMIT = 0;
	private final int DO_REQUEST_GET_WORKORDER_COMPLETION = 1;
	private final int DO_REQUEST_GET_WORKORDER = 2;

	private final int SUBMIT_ON_OPERATION_TRANSACTION = 10;
	private final int SUBMIT_ON_COMPLETION = 11;
	private final int SUBMIT_ON_TRANSACTION_ISSUE = 12;

	private EditText mEdtSubInventory;
	private EditText mEdtWipNameFrom;
	private EditText mEdtWipNameTo;
	private EditText mEdtTransactionQty;

	private TextView mTvClassCodeFrom;
	private TextView mTvStartTimeFrom;
	private TextView mTvCompletionTimeFrom;
	private TextView mTvSegment;
	private TextView mTvClassCodeTo;
	private TextView mTvStartTimeTo;
	private TextView mTvCompletionTimeTo;

	private Button mBtnSubmit;

	private ImageButton mImbQuery;

	private WORequestManager mRequestManager;

	private ProgressDialog mProgressDialog;

	private WorkOrder3 mCurrentWorkOrderCompletion;
	private List<WorkOrder> mCurrentWorkOrders;

	private ToastHelper mToastHelper;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_virtual_completion);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mEdtSubInventory = (EditText) findViewById(R.id.edt_vc_sub_inventory);
		mEdtSubInventory.setOnTouchListener(mHideKeyBoardTouchEvent);
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

		mEdtWipNameFrom = (EditText) findViewById(R.id.edt_vc_wip_entity_from);
		mEdtWipNameFrom.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtWipNameFrom.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				clearFromValues();

				clearToValues();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mEdtWipNameFrom.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						getWorkOrderOnCompletion();
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});

		mEdtWipNameTo = (EditText) findViewById(R.id.edt_vc_wip_entity_to);
		mEdtWipNameTo.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtWipNameTo.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				clearToValues();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		mEdtWipNameTo.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						getWorkOrder();
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});

		mEdtTransactionQty = (EditText) findViewById(R.id.edt_vc_transaction_quantity);
		mEdtTransactionQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvClassCodeFrom = (TextView) findViewById(R.id.tv_vc_class_code_from);
		mTvStartTimeFrom = (TextView) findViewById(R.id.tv_vc_start_time_from);
		mTvCompletionTimeFrom = (TextView) findViewById(R.id.tv_vc_completion_time_from);
		mTvSegment = (TextView) findViewById(R.id.tv_vc_segment);
		mTvClassCodeTo = (TextView) findViewById(R.id.tv_vc_class_code_to);
		mTvStartTimeTo = (TextView) findViewById(R.id.tv_vc_start_time_to);
		mTvCompletionTimeTo = (TextView) findViewById(R.id.tv_vc_completion_time_to);

		mBtnSubmit = (Button) findViewById(R.id.btn_vc_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_vc_query);
		mImbQuery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, CompletionQueryActivity.class);
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

	private void getWorkOrderOnCompletion() {
		final String wipEntityName = mEdtWipNameFrom.getText().toString();

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
				msg.what = DO_REQUEST_GET_WORKORDER_COMPLETION;
				try {
					WorkOrder3 workOrder = mRequestManager
							.getWorkOrderOnCompletion(wipEntityName);
					msg.obj = workOrder;
				} catch (Exception e) {

				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void getWorkOrder() {
		final String wipEntityName = mEdtWipNameTo.getText().toString();
		final String segment = mTvSegment.getText().toString();

		if (TextUtils.isEmpty(wipEntityName)) {
			mToastHelper.show("【工单号】不得为空!");
			return;
		}

		if(TextUtils.isEmpty(segment)){
			mToastHelper.show("【料号】不得为空!");
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
				try {
					List<WorkOrder> workOrder = mRequestManager
							.getWorkOrder(wipEntityName, segment);
					msg.obj = workOrder;
				} catch (Exception e) {

				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void submit() {
		if (mCurrentWorkOrderCompletion == null) {
			mToastHelper.show("请先获取前道工单信息!");
			return;
		}

		if (mCurrentWorkOrders == null) {
			mToastHelper.show("请先获取后道工单信息!");
			return;
		}

		String transactionQtyStr = mEdtTransactionQty.getText().toString();
		if (TextUtils.isEmpty(transactionQtyStr)) {
			mToastHelper.show("请输入【领料数量】!");
			return;
		}

		String subInventory = mEdtSubInventory.getText().toString();
		if (TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("请输入【子库】!");
			return;
		}

		BigDecimal transactionQty = new BigDecimal(transactionQtyStr);

		final ProcedureTransfer proce = new ProcedureTransfer();
		proce.WipEntityName = mCurrentWorkOrderCompletion.WipEntityName;
		proce.FromIntraOperationStepMeaning = "排队";
		proce.FromOperationSeqNum = "10";
		proce.Organization = mCurrentWorkOrderCompletion.Organization;
		proce.Reason = "Trx From Mobile";
		proce.Reference = "IMEI:" + Device.getIMEI(this);
		proce.ToIntraOperationStepMeaning = "移动";
		proce.ToOperationSeqNum = "10";
		proce.TransactionQuantity = transactionQty;
		proce.TransactionType = "移动";
		proce.CreatedByName = AppCache.getInstance().getLoginUser().toUpperCase(Locale.getDefault());

		final WipTransactionCompletion wip = new WipTransactionCompletion();
		wip.Locator = subInventory
				+ ".00."
				+ StringUtils.trimToEmpty(mCurrentWorkOrderCompletion.ProjectNumber);
		wip.Organization = mCurrentWorkOrderCompletion.Organization;
		wip.LotNumber = mCurrentWorkOrderCompletion.LotNumber;
		wip.OverCompletion = "N";
		wip.ProjectNumber = mCurrentWorkOrderCompletion.ProjectNumber;
		wip.Reason = "Trx From Mobile";
		wip.Reference = Device.getIMEI(this);
		wip.SubInventory = subInventory;
		wip.TransactionQuantity = transactionQty;
		wip.TransactionType = "WIP Completion";
		wip.WipEntityName = mCurrentWorkOrderCompletion.WipEntityName;

		long groupID = System.currentTimeMillis()
				+ new Random().nextInt(999999);

		final List<CUX_WIP_TRANSACTION_INT> transactions = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		WorkOrder workOrder = mCurrentWorkOrders.get(0);
		CUX_WIP_TRANSACTION_INT transIssue = mRequestManager
				.createDefaultTrans(WORequestManager.TRANS_ISSUE);
		transIssue.GroupID = groupID;
		transIssue.OrganizationCode = workOrder.Organization;
		transIssue.WipEntityName = workOrder.WipEntityName;
		transIssue.ProjectNumber = workOrder.ProjectNumber;
		transIssue.JobType = workOrder.ClassCode;
		transIssue.Assembly = workOrder.ConcatenatedSegments;
		transIssue.AssemblyLotNumber = workOrder.LotNumber;
		transIssue.AssemblyUomCode = workOrder.PrimaryUomCode;
		transIssue.StartQuantity = workOrder.QuantityIssued;
		transIssue.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
		transIssue.ComponentItem = workOrder.Segment1;
		transIssue.ComponentUomCode = workOrder.ItemPrimaryUomCode;
		transIssue.ComponentLotNumber = mCurrentWorkOrderCompletion.LotNumber;
		transIssue.RequiredQuantity = workOrder.RequiredQuantity;
		transIssue.TransactionQuantity = transactionQty;
		transIssue.ComponentSubinventory = subInventory;
		transIssue.ComponentLocator = subInventory + ".00." + StringUtils.trimToEmpty(workOrder.ProjectNumber);
		transIssue.Department = workOrder.DepartmentCode;

		transactions.add(transIssue);

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "虚拟完工提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getSuccessMsg();
		final String wipNameFrom = mEdtWipNameFrom.getText().toString();
		final String wipNameTo = mEdtWipNameTo.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_SUBMIT;
				try {

					//Three step on submit. First, Operation Transaction; Second, Wip Completion;
					//Third, Transaction Issue.
					msg.arg1 = SUBMIT_ON_OPERATION_TRANSACTION;
					String result = mRequestManager.submitProcedure(proce);
					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
						msg.arg1 = SUBMIT_ON_COMPLETION;
						result = mRequestManager.submitTransactionComp(wip);
						if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
							msg.arg1 = SUBMIT_ON_TRANSACTION_ISSUE;
							result = mRequestManager.submitTransactionInt(transactions);

							if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){

								PrintManager manager = PrintManager.getInstance(mInstance);
								manager.connect();
								manager.print(transMsg.getBytes("GB2312"));
								manager.printOneDimenBarcode(wipNameFrom);
								manager.printOneDimenBarcode(wipNameTo);
								manager.print("\n------------------------\n\n\n\n".getBytes());
								manager.close();
							}
						}
					}
					msg.obj = result;
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
				case DO_REQUEST_GET_WORKORDER_COMPLETION:
					if(msg.obj != null){
						mCurrentWorkOrderCompletion = (WorkOrder3) msg.obj;

						mTvClassCodeFrom.setText(mCurrentWorkOrderCompletion.ClassCode);
						mTvStartTimeFrom.setText(mCurrentWorkOrderCompletion.ScheduledStartDate);
						mTvCompletionTimeFrom.setText(mCurrentWorkOrderCompletion.ScheduledCompletionDate);
						mTvSegment.setText(mCurrentWorkOrderCompletion.ConcatenatedSegments);
					}else{
						mToastHelper.show("工单获取失败!");
					}
					break;
				case DO_REQUEST_GET_WORKORDER:
					if (msg.obj != null) {

						mCurrentWorkOrders = (List<WorkOrder>) msg.obj;
						if(mCurrentWorkOrders.size() > 0){
							WorkOrder workOrder = mCurrentWorkOrders.get(0);
							mTvClassCodeTo.setText(workOrder.ClassCode);
							mTvCompletionTimeTo.setText(workOrder.ScheduledCompletionDate);
							mTvStartTimeTo.setText(workOrder.ScheduledStartDate);
						}
					} else {
						mToastHelper.show("工单获取失败!");
					}
					break;
				case DO_REQUEST_SUBMIT:
					if (msg.obj != null) {
						String result = (String) msg.obj;

						String oper = StringUtils.EMPTY;

						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg();

							clearFromValues();

							clearToValues();

							clearInputValues();

							mEdtSubInventory.requestFocus();

						}else if(RequestUtil.RESPONSE_MSG_ERROR_AUTHORITY.equalsIgnoreCase(result)){
							mToastHelper.show(mResources.getString(R.string.request_error_authority));
						} else {

							switch (msg.arg1) {
								case SUBMIT_ON_COMPLETION:
									oper = "工单完工:";
									break;
								case SUBMIT_ON_OPERATION_TRANSACTION:
									oper = "工序移动:";
									break;
								case SUBMIT_ON_TRANSACTION_ISSUE:
									oper = "工单领料:";
							}

							mToastHelper.show(oper + "错误:" + result);
						}
					}
					break;
			}

			return false;
		}
	});

	private void clearFromValues(){

		mTvClassCodeFrom.setText("");
		mTvCompletionTimeFrom.setText("");
		mTvStartTimeFrom.setText("");
		mTvSegment.setText("");
		mCurrentWorkOrderCompletion = null;
	}

	private void clearToValues(){

		mTvClassCodeTo.setText("");
		mTvCompletionTimeTo.setText("");
		mTvStartTimeTo.setText("");
		mCurrentWorkOrders = null;
	}

	private void clearInputValues(){

		mEdtSubInventory.setText(StringUtils.EMPTY);
		mEdtTransactionQty.setText(StringUtils.EMPTY);
		mEdtWipNameFrom.setText(StringUtils.EMPTY);
		mEdtWipNameTo.setText(StringUtils.EMPTY);
	}

	private void showTransSuccessMsg() {

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(getSuccessMsg().toString()).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private String getSuccessMsg(){

		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("提交成功\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("子库:" + mEdtSubInventory.getText().toString() + "\n");
		stbMsg.append("前道工单:" + mEdtWipNameFrom.getText().toString() + "\n");
		stbMsg.append("工单类型:"+ mTvClassCodeFrom.getText().toString() + "\n");
		stbMsg.append("开始时间:"+ mTvStartTimeFrom.getText().toString() + "\n");
		stbMsg.append("完成时间:" + mTvCompletionTimeFrom.getText().toString() + "\n");

		stbMsg.append("制品号码:" + mTvSegment.getText().toString() + "\n");
		stbMsg.append("后道工单:" + mEdtWipNameTo.getText().toString() + "\n");
		stbMsg.append("开始时间:" + mTvStartTimeTo.getText().toString() + "\n");
		stbMsg.append("完成时间:" + mTvCompletionTimeTo.getText().toString() + "\n");
		stbMsg.append("移动数量:" + mEdtTransactionQty.getText().toString() + "\n");

		return stbMsg.toString();
	}

	@Override
	protected void decodeCallback(String barcode) {
		if (mEdtSubInventory.hasFocus()) {
			mEdtSubInventory.setText(barcode);
			mEdtWipNameFrom.requestFocus();
		} else if (mEdtWipNameFrom.hasFocus()) {
			mEdtWipNameFrom.setText(barcode);
			getWorkOrderOnCompletion();
		}else if(mEdtWipNameTo.hasFocus()){
			mEdtWipNameTo.setText(barcode);

		}
	}
}
