package com.hcp.biz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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

import com.hcp.biz.query.activity.ReturnQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WorkOrder;
import com.hcp.http.RequestUtil;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.util.AppConfig;
import com.hcp.util.PrintManager;
import com.hcp.wo.R;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MaterialReturnActivity extends ScannerActivity implements
		TextWatcher {

	private final int DO_REQUEST_ISSUE = 0;
	private final int DO_REQUEST_GET_DATA = 1;

	private EditText mEdtSubInventory;
	private EditText mEdtWipName;
	private EditText mEdtComponent;
	private EditText mEdtReturnQty;

	private TextView mTvClassCode;
	private TextView mTvStartTime;
	private TextView mTvCompleteTime;
	private TextView mTvComponentName;
	private TextView mTvIssuedType;
	private TextView mTvIssuedQty;
	private TextView mTvUnIssuedQty;

	private Button mBtnSubmit;

	private ImageButton mImbQuery;

	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;

	private List<WorkOrder> mCurrentWorkOrders = new ArrayList<WorkOrder>();

	private Resources mResources;
	private WORequestManager mRequestManager;
	private String[] mLotControls;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private boolean mOnBatch = false;
	private int mBatchID = -1;

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_return);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mToastHelper = ToastHelper.getInstance(this);
		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);
		mLotControls = mResources.getStringArray(R.array.lot_control);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_mr_sub_inventory);
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

		mEdtWipName = (EditText) findViewById(R.id.edt_mr_wip_name);
		mEdtWipName.addTextChangedListener(this);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponent = (EditText) findViewById(R.id.edt_mr_component_code);
		mEdtComponent.addTextChangedListener(this);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtComponent.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getWorkOrder();
				}

			}
		});

		mEdtReturnQty = (EditText) findViewById(R.id.edt_mr_return_qty);
		mEdtReturnQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvClassCode = (TextView) findViewById(R.id.tv_mr_class_code);
		mTvStartTime = (TextView) findViewById(R.id.tv_mr_start_date);
		mTvCompleteTime = (TextView) findViewById(R.id.tv_mr_complete_date);
		mTvComponentName = (TextView) findViewById(R.id.tv_mr_component_name);
		mTvIssuedQty = (TextView) findViewById(R.id.tv_mr_issued_qty);
		mTvUnIssuedQty = (TextView) findViewById(R.id.tv_mr_unreceived_qty);
		mTvIssuedType = (TextView) findViewById(R.id.tv_mr_issued_type);

		mBtnSubmit = (Button) findViewById(R.id.btn_mr_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_mr_query);
		mImbQuery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, ReturnQueryActivity.class);
				mInstance.startActivity(intent);
			}
		});

		Intent intent = getIntent();
		if(intent != null && intent.hasExtra(BatchTransactionActivity.REQUEST_ON_BATCH)){
			mOnBatch = true;
			String wipName = intent.getStringExtra(BatchTransactionActivity.REQUEST_WIP_NAME);
			String componentCode = intent.getStringExtra(BatchTransactionActivity.REQUEST_COMPONENT_CODE);
			mBatchID = intent.getIntExtra(BatchTransactionActivity.REQUEST_ID, -1);

			mEdtComponent.setEnabled(false);
			mEdtComponent.setFocusable(false);
			mEdtComponent.setText(componentCode);

			mEdtWipName.setEnabled(false);
			mEdtWipName.setFocusable(false);
			mEdtWipName.setText(wipName);

			getWorkOrder();
		}
	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private void submit() {
		if (mCurrentWorkOrders == null || mCurrentWorkOrders.size() == 0) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String subInventory = mEdtSubInventory.getText().toString();
		if(TextUtils.isEmpty(subInventory)){
			mToastHelper.show("请输入【子库】!");
			return;
		}

		String returnQtyStr = mEdtReturnQty.getText().toString();
		if (TextUtils.isEmpty(returnQtyStr)) {
			mToastHelper.show("请输入【退料数量】!");
			return;
		}

		BigDecimal returnQty = new BigDecimal(returnQtyStr);
		BigDecimal issuedQty = new BigDecimal(mTvIssuedQty.getText().toString());

		if (returnQty.compareTo(issuedQty) > 0) {
			mToastHelper.show("【退料数量】不得多于【已发数量】!");
			return;
		}

		long groupIDIssue = System.currentTimeMillis()
				+ new Random().nextInt(999999);
		final List<CUX_WIP_TRANSACTION_INT> transReturns = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		for (WorkOrder item : mCurrentWorkOrders) {
			if (returnQty.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}

			if(item.ComponentLotQuantity == null || item.ComponentLotQuantity.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			CUX_WIP_TRANSACTION_INT transReturn = mRequestManager
					.createDefaultTrans(WORequestManager.TRANS_RETURN);
			transReturn.GroupID = groupIDIssue;
			transReturn.OrganizationCode = item.Organization;
			transReturn.WipEntityName = item.WipEntityName;
			transReturn.ProjectNumber = "";
			transReturn.JobType = item.ClassCode;
			transReturn.Assembly = item.ConcatenatedSegments;
			transReturn.AssemblyLotNumber = item.LotNumber;
			transReturn.AssemblyUomCode = item.PrimaryUomCode;
			transReturn.StartQuantity = item.QuantityIssued;
			transReturn.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
			transReturn.ComponentItem = item.Segment1;
			transReturn.ComponentUomCode = item.ItemPrimaryUomCode;
			transReturn.ComponentLotNumber = item.ComponentLotNumber;
			transReturn.ComponentSubinventory = subInventory;
			transReturn.ComponentLocator = subInventory + ".00." + (item.ComponentPeggingFlag.equals("X") ? StringUtils.trimToEmpty(item.ProjectNumber) : "");
			transReturn.RequiredQuantity = item.RequiredQuantity;
			transReturn.TransactionQuantity = getTransactionQty(item, returnQty);
			returnQty = returnQty.subtract(item.ComponentLotQuantity);
			transReturn.Department = item.DepartmentCode;
			transReturn.OpSeq = item.SeqNumber;

			transReturns.add(transReturn);

			if(isLotControl(item)){
				break;
			}
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "退料单提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getSuccessMsg();
		final String wipName = mEdtWipName.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_ISSUE;
				try {

					String result = mRequestManager.submitTransactionInt(transReturns);
					msg.obj = result;

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

	private BigDecimal getTransactionQty(WorkOrder workOrder, BigDecimal returnQty){

		BigDecimal transQty ;

		if(isLotControl(workOrder)){
			transQty = returnQty;
		}else{
			transQty = returnQty.compareTo(workOrder.ComponentLotQuantity) > 0 ? workOrder.ComponentLotQuantity : returnQty;
		}
		return transQty;
	}

	private boolean isLotControl(WorkOrder workOrder){
		return workOrder.LotControl.equals(mLotControls[1]);
	}

	@Override
	protected void decodeCallback(String barcode) {
		if(mEdtSubInventory.hasFocus()){
			mEdtSubInventory.setText(barcode);
			if(mOnBatch){
				mEdtReturnQty.requestFocus();
				mEdtReturnQty.selectAll();
			}else{
				mEdtWipName.requestFocus();
			}
		}else if (mEdtWipName.hasFocus()) {
			mEdtWipName.setText(barcode);
			mEdtComponent.requestFocus();
		} else if (mEdtComponent.hasFocus()) {
			mEdtComponent.setText(barcode);
			mEdtReturnQty.requestFocus();
			getWorkOrder();
		}
	}

	private void getWorkOrder() {
		final String wipEntityName = mEdtWipName.getText().toString();
		final String componentCode = mEdtComponent.getText().toString();

		if (TextUtils.isEmpty(wipEntityName)
				|| TextUtils.isEmpty(componentCode)) {
			mToastHelper.show("【工单号】,【组件编号】不得为空");
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
				msg.what = DO_REQUEST_GET_DATA;
				try {
					List<WorkOrder> workOrders = mRequestManager.getWorkOrder(
							wipEntityName, componentCode);
					msg.obj = workOrders;
				} catch (Exception e) {

				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void clearValues() {
		mTvClassCode.setText("");
		mTvCompleteTime.setText("");
		mTvStartTime.setText("");
		mTvComponentName.setText("");
		mTvIssuedType.setText("");
		mTvIssuedQty.setText("");
		mTvUnIssuedQty.setText("");
		mCurrentWorkOrders.clear();
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
				case DO_REQUEST_GET_DATA:
					if (msg.obj != null) {

						mCurrentWorkOrders = groupByLotNumber((List<WorkOrder>) msg.obj);
						WorkOrder workOrder = mCurrentWorkOrders.get(0);

						BigDecimal nagative = new BigDecimal(-1);
						for (WorkOrder item : mCurrentWorkOrders) {
							item.ComponentLotQuantity = item.ComponentLotQuantity
									.multiply(nagative);
						}

						mTvClassCode.setText(workOrder.ClassCode);
						mTvStartTime.setText(workOrder.ScheduledStartDate);
						mTvCompleteTime.setText(workOrder.ScheduledCompletionDate);
						mTvComponentName.setText(workOrder.ItemDescription);
						mTvIssuedType.setText(workOrder.WipSupplyMeaning);

						mTvIssuedQty.setText(workOrder.QuantityIssued + "");
						mTvUnIssuedQty.setText(workOrder.QuantityOpen + "");
						mEdtReturnQty.setText(workOrder.QuantityIssued + "");

						if(mOnBatch){
							mEdtSubInventory.requestFocus();
						}

					} else {
						mToastHelper.show("工单及库存现有量获取失败!");
					}
					break;
				case DO_REQUEST_ISSUE:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(getSuccessMsg());

							clearValues();

							mEdtWipName.requestFocus();

						}else if(RequestUtil.RESPONSE_MSG_ERROR_AUTHORITY.equalsIgnoreCase(result)){
							mToastHelper.show(mResources.getString(R.string.request_error_authority));
						} else {
							mToastHelper.show(result);
						}
					}
					break;
			}

			return false;
		}
	});

	private List<WorkOrder> groupByLotNumber(List<WorkOrder> workOrders) {

		Map<String, WorkOrder> lotQtyMap = new HashMap<String, WorkOrder>();
		for (WorkOrder workOrder : workOrders) {
			if (!lotQtyMap.containsKey(workOrder.ComponentLotNumber)) {
				lotQtyMap.put(workOrder.ComponentLotNumber, workOrder);
			} else {
				BigDecimal addQty = workOrder.ComponentLotQuantity;
				BigDecimal currentQty = lotQtyMap
						.get(workOrder.ComponentLotNumber).ComponentLotQuantity;
				lotQtyMap.get(workOrder.ComponentLotNumber).ComponentLotQuantity = currentQty
						.add(addQty);
			}
		}
		List<WorkOrder> group = new ArrayList<WorkOrder>();
		group.addAll(lotQtyMap.values());
		return group;
	}

//	private ReturnInfo saveReturn(){
//		ReturnInfo ret = new ReturnInfo();
//		ret.wip_entity_name = mEdtWipName.getText().toString();
//		ret.class_code = mTvClassCode.getText().toString();
//		Date statTime = new Date(System.currentTimeMillis());
//		Date completionTime = new Date(System.currentTimeMillis());
//		try {
//			statTime = DateFormat.defaultParse(mTvStartTime.getText().toString());
//			completionTime = DateFormat.defaultParse(mTvCompleteTime.getText().toString());
//		} catch (ParseException e) {
//			mToastHelper.show(e.getMessage());
//		}
//		ret.sub_inventory = mEdtSubInventory.getText().toString();
//		ret.start_time = statTime;
//		ret.completion_time = completionTime;
//		ret.component_code = mEdtComponent.getText().toString();
//		ret.component_name = mTvComponentName.getText().toString();
//		ret.wip_supply_meaning = mTvIssuedType.getText().toString();
//		ret.issued_quantity = new BigDecimal(mTvIssuedQty.getText().toString());
//		ret.quantity_open = new BigDecimal(mTvUnIssuedQty.getText().toString());
//		ret.transaction_quantity = new BigDecimal(mEdtReturnQty.getText().toString());
//		ret.create_by = AppCache.getInstance().getLoginUser();
//		ret.create_time = new Date(System.currentTimeMillis());
//		
//		try {
//			mBizDBHelper.insert(ReturnInfo.class, ret);
//		} catch (Exception e) {
//			mToastHelper.show(e.getMessage());
//		}
//		
//		return ret;
//	}

	private void showTransSuccessMsg(String msg) {

		final BigDecimal transQty = new BigDecimal(mEdtReturnQty.getText().toString());

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(msg)
				.setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(mOnBatch){
					Intent intent = new Intent();
					intent.putExtra(BatchTransactionActivity.REQUEST_ID, mBatchID);
					intent.putExtra(BatchTransactionActivity.REQUEST_TRANSACTION_QUANTITY, transQty);
					intent.putExtra(BatchTransactionActivity.REQUEST_STATE, BatchTransactionActivity.STATE_SUBMITTED);

					setResult(BatchTransactionActivity.STATE_SUBMITTED, intent);

					finish();
				}
			}
		});
		dialog.show();
	}

	private String getSuccessMsg(){
		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("提交成功\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_class_code)
				+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_start_time)
				+ mTvStartTime.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_complete_time)
				+ mTvCompleteTime.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_component_code)
				+ mEdtComponent.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_component_name)
				+ mTvComponentName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_issued_type)
				+ mTvIssuedType.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_issued_qty)
				+ mTvIssuedQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_unissued_qty)
				+ mTvUnIssuedQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mr_return_qty)
				+ mEdtReturnQty.getText().toString() + "\n");
		return stbMsg.toString();
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
