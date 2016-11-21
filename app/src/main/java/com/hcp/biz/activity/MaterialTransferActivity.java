package com.hcp.biz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hcp.biz.entities.TransactionInt;
import com.hcp.biz.query.activity.TransactionQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.dao.TransactionIntDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WorkOrder;
import com.hcp.entities.WorkOrder2;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MaterialTransferActivity extends ScannerActivity {

	private final int ON_ERROR_CALL_BACK = -1;
	private final int DO_GET_WORKORDER_ON_ORI = 0;
	private final int DO_GET_WORKORDER_ON_CHARGING = 1;
	private final int DO_TRANSACTION_INT = 2;

	private EditText mEdtSubInventory;
	private EditText mEdtOriCode;
	private EditText mEdtComponentCode;
	private EditText mEdtOriCodeCharging;
	private EditText mEdtMovedQty;

	private TextView mTvComponentName;
	private TextView mTvProvidedQty;
	private TextView mTvMovableQty;
	private TextView mTvComponentNameCharging;
	private TextView mTvComponentCodeCharging;
	private TextView mTvComponentQty;

	private ImageButton mImgQuery;

	private Button mBtnSubmit;

	private ToastHelper mToastHelper;

	private List<WorkOrder> mCurrentWorkOrders = new ArrayList<WorkOrder>();

	private WorkOrder2 mCurrentWorkOrderCharging = null;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private Resources mResources;
	private WORequestManager mRequestManager;

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_transfer);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_mt_sub_inventory);
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

		mEdtOriCode = (EditText) findViewById(R.id.edt_ori_code);
		mEdtOriCode.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtOriCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				clearValues();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mEdtComponentCode = (EditText) findViewById(R.id.edt_component_code);
		mEdtComponentCode.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtComponentCode.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						if (!isOriRequiredTextEmpty()) {
							doGetWorkOrder(mEdtOriCode.getText().toString(),
									mEdtComponentCode.getText().toString(),
									DO_GET_WORKORDER_ON_ORI);
						} else {
							mToastHelper.show("【原工单号】或【组件编号】不得为空!");
						}
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});
		mEdtComponentCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				clearValues();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mEdtOriCodeCharging = (EditText) findViewById(R.id.edt_ori_code_charging);
		mEdtOriCodeCharging.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtOriCodeCharging.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						if (!isChargingRequiredTextEmpty()) {
							doGetWorkOrderCharging(mEdtOriCodeCharging
											.getText().toString(),
									mTvComponentCodeCharging.getText()
											.toString(),
									DO_GET_WORKORDER_ON_CHARGING);
						} else {
							mToastHelper.show("【原工单号】或【组件编号】不得为空!");
						}
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});
		mEdtMovedQty = (EditText) findViewById(R.id.edt_moved_qty);
		mEdtMovedQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvComponentName = (TextView) findViewById(R.id.tv_component_name);
		mTvProvidedQty = (TextView) findViewById(R.id.tv_provided_qty);
		mTvMovableQty = (TextView) findViewById(R.id.tv_movable_qty);
		mTvComponentCodeCharging = (TextView) findViewById(R.id.tv_component_code_charging);
		mTvComponentNameCharging = (TextView) findViewById(R.id.tv_component_name_charging);
		mTvComponentQty = (TextView) findViewById(R.id.tv_component_qty);

		mBtnSubmit = (Button) findViewById(R.id.btn_mt_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImgQuery = (ImageButton) findViewById(R.id.imb_mt_query);
		mImgQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, TransactionQueryActivity.class);
				mInstance.startActivity(intent);

			}
		});

		mToastHelper = ToastHelper.getInstance(this);

	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private TransactionInt saveTrans(){
		TransactionInt transInt = new TransactionInt();
		transInt.component_code = mEdtComponentCode.getText().toString();
		transInt.component_name = mTvComponentName.getText().toString();
		transInt.create_by = AppCache.getInstance().getLoginUser();
		transInt.create_time = new Date(System.currentTimeMillis());
		transInt.transaction_quantity = new BigDecimal(mEdtMovedQty.getText().toString());
		transInt.ori_required_quantity = new BigDecimal(mTvProvidedQty.getText().toString());
		transInt.ori_wip_entity_name = mEdtOriCode.getText().toString();
		transInt.remaining_quantity = new BigDecimal(mTvMovableQty.getText().toString());
		transInt.required_quantity = new BigDecimal(mTvComponentQty.getText().toString());
		transInt.wip_entity_name = mEdtOriCodeCharging.getText().toString();

		try {
			TransactionIntDao.getInstance(this).onInsert().insertTransactionInt(transInt);
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}

		return transInt;
	}

	private void showTransSuccessMsg(final TransactionInt transactionInt) {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(getSuccessMsg()).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
	}

	private String getSuccessMsg(){
		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("工 单 移 料\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("原 始 工 单\n");
		stbMsg.append(mResources.getString(R.string.mt_ori_code)
				+ mEdtOriCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_component_code)
				+ mEdtComponentCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_component_name)
				+ mTvComponentName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_provide_qty)
				+ mTvProvidedQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_movable_qty)
				+ mTvMovableQty.getText().toString() + "\n");
		stbMsg.append("\n投 料 工 单\n");
		stbMsg.append("工单号:" + mEdtOriCodeCharging.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_component_code)
				+ mTvComponentCodeCharging.getText() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_component_name)
				+ mTvComponentNameCharging.getText() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_component_qty)
				+ mTvComponentQty.getText() + "\n");
		stbMsg.append(mResources.getString(R.string.mt_moved_qty)
				+ mEdtMovedQty.getText().toString() + "\n");

		return stbMsg.toString();
	}

	private void clearValues() {
		mTvComponentName.setText("");
		mTvProvidedQty.setText("");
		mTvMovableQty.setText("");
		mTvComponentCodeCharging.setText("");
		mTvComponentNameCharging.setText("");
		mTvComponentQty.setText("");
		mEdtMovedQty.setText("");
		mCurrentWorkOrders.clear();

		mCurrentWorkOrderCharging = null;
	}

	private ProgressDialog mProgressDialog;

	private void doGetWorkOrder(final String orderCode,
								final String componentCode, final int type) {

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("工单获取中!");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					List<WorkOrder> workOrder = mRequestManager.getWorkOrder(
							orderCode, componentCode);
					msg.obj = workOrder;
					msg.what = type;
					mRefreshByWorkOrder.sendMessage(msg);
				} catch (Exception e) {
					msg.what = ON_ERROR_CALL_BACK;
					msg.obj = e.getMessage();
					mRefreshByWorkOrder.sendMessage(msg);
				}
			}
		}).start();
	}

	private void doGetWorkOrderCharging(final String orderCode,
										final String componentCode, final int type) {

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("工单获取中!");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				try {
					List<WorkOrder2> workOrder = mRequestManager
							.getWorkOrderOnQuery(orderCode, componentCode, null, false);
					msg.obj = workOrder;
					msg.what = type;
					mRefreshByWorkOrder.sendMessage(msg);
				} catch (Exception e) {
					msg.what = ON_ERROR_CALL_BACK;
					msg.obj = e.getMessage();
					mRefreshByWorkOrder.sendMessage(msg);
				}
			}
		}).start();
	}

	private boolean isOriRequiredTextEmpty() {

		final String orderCode = mEdtOriCode.getText().toString();
		final String componentCode = mEdtComponentCode.getText().toString();

		if (TextUtils.isEmpty(orderCode)) {
			return true;
		}

		if (TextUtils.isEmpty(componentCode)) {
			return true;
		}

		return false;
	}

	private boolean isChargingRequiredTextEmpty() {

		final String orderCode = mEdtOriCodeCharging.getText().toString();
		final String componentCode = mTvComponentCodeCharging.getText()
				.toString();

		if (TextUtils.isEmpty(orderCode)) {
			return true;
		}

		if (TextUtils.isEmpty(componentCode)) {
			return true;
		}

		return false;
	}

	private Handler mRefreshByWorkOrder = new Handler(new Handler.Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}

			switch (msg.what) {

				case DO_TRANSACTION_INT:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(saveTrans());

							clearValues();

							mEdtComponentCode.setText("");
							mEdtOriCode.setText("");
							mEdtOriCodeCharging.setText("");
							mEdtMovedQty.setText("");

							mEdtOriCode.requestFocus();

						}else if(RequestUtil.RESPONSE_MSG_ERROR_AUTHORITY.equalsIgnoreCase(result)){
							mToastHelper.show(mResources.getString(R.string.request_error_authority));
						}else if (RequestUtil.RESPONSE_MSG_FAILED.equals(result)) {
							mToastHelper.show("提交失败!");
						} else {
							mToastHelper.show(result);
						}
					}
					break;

				case DO_GET_WORKORDER_ON_ORI:
					if (msg.obj != null) {
						List<WorkOrder> workOrders = (List<WorkOrder>) msg.obj;
						mCurrentWorkOrders = groupByLotNumber(workOrders);
						mTvComponentName.setText(mCurrentWorkOrders.get(0).ItemDescription);

						BigDecimal nagative = new BigDecimal(-1);
						for (WorkOrder workOrder : mCurrentWorkOrders) {
							workOrder.ComponentLotQuantity = workOrder.ComponentLotQuantity
									.multiply(nagative);
						}
						mTvProvidedQty.setText(mCurrentWorkOrders.get(0).QuantityIssued
								+ "");
						mTvMovableQty.setText(mCurrentWorkOrders.get(0).QuantityIssued
								+ "");
						mTvComponentCodeCharging.setText(mCurrentWorkOrders.get(0).Segment1);
						mTvComponentNameCharging.setText(mCurrentWorkOrders.get(0).ItemDescription);
					} else {
						mToastHelper.show(msg.obj.toString());
					}
					break;

				case DO_GET_WORKORDER_ON_CHARGING:
					if (msg.obj != null) {
						mCurrentWorkOrderCharging = ((List<WorkOrder2>) msg.obj)
								.get(0);
						BigDecimal componentQtyCharging = mCurrentWorkOrderCharging.RequiredQuantity
								.subtract(mCurrentWorkOrderCharging.QuantityIssued);
						BigDecimal issuedQty = mCurrentWorkOrders.get(0).QuantityIssued;
						mTvComponentQty.setText(componentQtyCharging + "");
						mEdtMovedQty.setText((componentQtyCharging
								.compareTo(issuedQty) > 0 ? issuedQty
								: componentQtyCharging)
								+ "");
					} else {
						mToastHelper.show(msg.obj.toString());
					}
					break;

				case ON_ERROR_CALL_BACK:
					mToastHelper.show(msg.obj.toString());
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void submit() {

		String subInventory = mEdtSubInventory.getText().toString();
		if(TextUtils.isEmpty(subInventory)){
			mToastHelper.show("【子库】不得为空!");
			return;
		}

		String checkResult = submitCheck();
		if (!TextUtils.isEmpty(checkResult)) {
			mToastHelper.show(checkResult);
			return;
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"移料提交", "数据提交中!");
			mProgressDialog.show();
		}

		final List<CUX_WIP_TRANSACTION_INT> transReturns = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		final List<CUX_WIP_TRANSACTION_INT> transIssues = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		BigDecimal moveQty = new BigDecimal(mEdtMovedQty.getText().toString());

		long groupIDReturn = System.currentTimeMillis()
				+ new Random().nextInt(999999);
		long groupIDIssue = System.currentTimeMillis()
				+ new Random().nextInt(999999);
		for (WorkOrder workOrder : mCurrentWorkOrders) {
			if (moveQty.compareTo(BigDecimal.ZERO) > 0) {

				CUX_WIP_TRANSACTION_INT transIssue = mRequestManager
						.createDefaultTrans(WORequestManager.TRANS_ISSUE);
				transIssue.GroupID = groupIDIssue;
				transIssue.OrganizationCode = mCurrentWorkOrderCharging.Organization;
				transIssue.WipEntityName = mCurrentWorkOrderCharging.WipEntityName;
				transIssue.ProjectNumber = mCurrentWorkOrderCharging.ProjectNumber;
				transIssue.JobType = mCurrentWorkOrderCharging.ClassCode;
				transIssue.Assembly = mCurrentWorkOrderCharging.ConcatenatedSegments;
				transIssue.AssemblyLotNumber = mCurrentWorkOrderCharging.LotNumber;
				transIssue.AssemblyUomCode = mCurrentWorkOrderCharging.PrimaryUomCode;
				transIssue.StartQuantity = mCurrentWorkOrderCharging.QuantityIssued;
				transIssue.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
				transIssue.ComponentItem = mCurrentWorkOrderCharging.Segment1;
				transIssue.ComponentUomCode = mCurrentWorkOrderCharging.ItemPrimaryUomCode;
				transIssue.ComponentLotNumber = workOrder.ComponentLotNumber;
				transIssue.RequiredQuantity = mCurrentWorkOrderCharging.RequiredQuantity;
				transIssue.TransactionQuantity = workOrder.RequiredQuantity
						.compareTo(moveQty) < 0 ? workOrder.RequiredQuantity
						: moveQty;
				transIssue.ComponentSubinventory = subInventory;
				transIssue.ComponentLocator = subInventory + ".00." + (workOrder.ComponentPeggingFlag.equals("X") ? workOrder.ProjectNumber : "");
				transIssue.Department = workOrder.DepartmentCode;
				transIssue.OpSeq = workOrder.SeqNumber;
				transIssues.add(transIssue);

				CUX_WIP_TRANSACTION_INT transReturn = mRequestManager
						.createDefaultTrans(WORequestManager.TRANS_RETURN);
				transReturn.GroupID = groupIDReturn;
				transReturn.OrganizationCode = workOrder.Organization;
				transReturn.WipEntityName = workOrder.WipEntityName;
				transReturn.ProjectNumber = workOrder.ProjectNumber;
				transReturn.JobType = workOrder.ClassCode;
				transReturn.Assembly = workOrder.ConcatenatedSegments;
				transReturn.AssemblyLotNumber = workOrder.LotNumber;
				transReturn.AssemblyUomCode = workOrder.PrimaryUomCode;
				transReturn.StartQuantity = workOrder.QuantityIssued;
				transReturn.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
				transReturn.ComponentItem = workOrder.Segment1;
				transReturn.ComponentUomCode = workOrder.ItemPrimaryUomCode;
				transReturn.ComponentLotNumber = workOrder.ComponentLotNumber;
				transReturn.RequiredQuantity = workOrder.RequiredQuantity;
				transReturn.TransactionQuantity = transIssue.TransactionQuantity;
				transReturn.Department = workOrder.DepartmentCode;
				transReturn.ComponentSubinventory = subInventory;
				transReturn.ComponentLocator = subInventory + ".00." + (workOrder.ComponentPeggingFlag.equals("X") ? workOrder.ProjectNumber : "");
				transReturn.OpSeq = workOrder.SeqNumber;
				transReturns.add(transReturn);

				moveQty = moveQty.subtract(workOrder.RequiredQuantity);
			}
		}

		transReturns.addAll(transIssues);

		final String transMsg = getSuccessMsg();
		final String wipName = mEdtOriCode.getText().toString();
		final String wipNameCharging = mEdtOriCodeCharging.getText().toString();
		final String component = mEdtComponentCode.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_TRANSACTION_INT;
				try {
					String result = mRequestManager
							.submitTransactionInt(transReturns);
					msg.obj = result;

					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){

						PrintManager manager = PrintManager.getInstance(mInstance);
						manager.connect();
						manager.print(transMsg.getBytes("GB2312"));
						manager.printOneDimenBarcode(wipName);
						manager.printOneDimenBarcode(wipNameCharging);
						manager.printOneDimenBarcode(component);
						manager.print("\n------------------------\n\n\n\n".getBytes());
						manager.close();
					}
				} catch (Exception e) {

				} finally {
					mRefreshByWorkOrder.sendMessage(msg);
				}
			}
		}).start();
	}

	private String submitCheck() {
		String movableQtyStr = mTvMovableQty.getText().toString();
		String componentQtyStr = mTvComponentQty.getText().toString();
		String movedQtyStr = mEdtMovedQty.getText().toString();
		String oriCode = mEdtOriCode.getText().toString();
		String oriCodeCharging = mEdtOriCodeCharging.getText().toString();

		if (TextUtils.isEmpty(oriCode) || TextUtils.isEmpty(oriCodeCharging)) {
			return "【工单号】不得为空!";
		}

		if (oriCode.equals(oriCodeCharging)) {
			return "【工单号】不得重复!";
		}

		if (TextUtils.isEmpty(movableQtyStr)) {
			return "【可移数量】不得为空!";
		}

		if (TextUtils.isEmpty(componentQtyStr)) {
			return "【组件数量】不得为空!";
		}

		if (TextUtils.isEmpty(movedQtyStr)) {
			return "【移动数量】不得为空!";
		}

		BigDecimal movableQty = new BigDecimal(movableQtyStr);
		BigDecimal componentQty = new BigDecimal(componentQtyStr);
		BigDecimal movedQty = new BigDecimal(movedQtyStr);

		if (movedQty.compareTo(movableQty) > 0
				|| movedQty.compareTo(componentQty) > 0) {
			return "【移动数量】需低于【可移数量】与【组件数量】!";
		}
		return "";
	}

	@Override
	protected void decodeCallback(String barcode) {
		if(mEdtSubInventory.hasFocus()){
			mEdtSubInventory.setText(barcode);
			mEdtOriCode.requestFocus();
		}else if (mEdtComponentCode.hasFocus()) {
			mEdtComponentCode.setText(barcode);
			if (!isOriRequiredTextEmpty()) {
				doGetWorkOrder(mEdtOriCode.getText().toString(),
						mEdtComponentCode.getText().toString(),
						DO_GET_WORKORDER_ON_ORI);
			}
		} else if (mEdtOriCodeCharging.hasFocus()) {
			mEdtOriCodeCharging.setText(barcode);
			if (!isOriRequiredTextEmpty()) {
				doGetWorkOrderCharging(
						mEdtOriCodeCharging.getText().toString(),
						mTvComponentCodeCharging.getText().toString(),
						DO_GET_WORKORDER_ON_CHARGING);
			}
		} else {
			mEdtOriCode.setText(barcode);
			mEdtComponentCode.requestFocus();
		}
	}
}
