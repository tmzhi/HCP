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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hcp.biz.query.activity.IssueQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.LotQuantityInWarehouse;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WorkOrder2;
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

public class MaterialIssueActivity extends ScannerActivity implements
		TextWatcher {

	private final int DO_REQUEST_ISSUE = 0;
	private final int DO_REQUEST_GET_DATA = 1;

	private EditText mEdtWipName;
	private EditText mEdtComponent;
	private EditText mEdtIssuedQty;
	private EditText mEdtSubInventory;

	private TextView mTvClassCode;
	private TextView mTvStartDate;
	private TextView mTvCompleteDate;
	private TextView mTvComponentName;
	private TextView mTvReceivedQty;
	private TextView mTvUnReceivedQty;
	private TextView mTvIssuedType;
	private TextView mTvStandingCorp;

	private Button mBtnSubmit;

	private ImageButton mImbQuery;

	private CheckBox mCbOverQty;

	private Resources mResources;

	private List<WorkOrder2> mCurrentWorkOrders;
	private List<LotQuantityInWarehouse> mCurrentLots;

	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;

	private WORequestManager mRequestManager;

	private boolean mOnBatch = false;
	private int mBatchID = -1;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_issue);

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

		mEdtComponent = (EditText) findViewById(R.id.edt_mi_component_code);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtComponent.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getWorkOrder();
				}
			}
		});
		mEdtComponent.addTextChangedListener(this);

		mEdtWipName = (EditText) findViewById(R.id.edt_mi_wip_name);
		mEdtWipName.addTextChangedListener(this);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_mi_sub_inventory);
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

		mEdtIssuedQty = (EditText) findViewById(R.id.edt_mi_issued_qty);
		mEdtIssuedQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvClassCode = (TextView) findViewById(R.id.tv_mi_class_code);
		mTvStartDate = (TextView) findViewById(R.id.tv_mi_start_time);
		mTvCompleteDate = (TextView) findViewById(R.id.tv_mi_complete_date);
		mTvComponentName = (TextView) findViewById(R.id.tv_mi_component_name);
		mTvReceivedQty = (TextView) findViewById(R.id.tv_mi_received_qty);
		mTvUnReceivedQty = (TextView) findViewById(R.id.tv_mi_unreceived_qty);
		mTvIssuedType = (TextView) findViewById(R.id.tv_mi_issued_type);
		mTvStandingCorp = (TextView) findViewById(R.id.tv_mi_standing_crop);

		mBtnSubmit = (Button) findViewById(R.id.btn_mi_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_mi_query);
		mImbQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, IssueQueryActivity.class);
				mInstance.startActivity(intent);

			}
		});

		mCbOverQty = (CheckBox) findViewById(R.id.cb_mi_over_quantity);

		mToastHelper = ToastHelper.getInstance(this);

		//Is transaction on batch
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
		}
	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private void getWorkOrder() {
		final String wipEntityName = mEdtWipName.getText().toString();
		final String componentCode = mEdtComponent.getText().toString();
		final String subInventory = mEdtSubInventory.getText().toString();

		if (TextUtils.isEmpty(wipEntityName)
				|| TextUtils.isEmpty(componentCode)
				|| TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("【子库】,【工单号】,【组件编号】不得为空");
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

				List<Object> result;
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_GET_DATA;

				List<WorkOrder2> workOrders = null;
				List<LotQuantityInWarehouse> lots = null;
				try {
					workOrders = mRequestManager
							.getWorkOrderOnQuery(wipEntityName, componentCode, null, false);
					if (workOrders != null && workOrders.size() > 0) {
						boolean peggingFlag = workOrders.get(0).ComponentPeggingFlag.equalsIgnoreCase("X");
						lots = mRequestManager
								.getLotQuantityInWarehouses(
										workOrders.get(0).Organization,
										subInventory, componentCode, "", peggingFlag ? workOrders.get(0).ProjectNumber : "", peggingFlag);
					}
				} catch (Exception e) {

				} finally {

					if(workOrders != null){
						result = new ArrayList<Object>();
						result.add(workOrders);
						result.add(lots);
						msg.obj = result;
					}
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void submit() {
		if (mCurrentWorkOrders == null || mCurrentWorkOrders.size() == 0) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String issuedQtyStr = mEdtIssuedQty.getText().toString();
		if (TextUtils.isEmpty(issuedQtyStr)) {
			mToastHelper.show("请输入【发料数量】!");
			return;
		}
		BigDecimal issueQty = new BigDecimal(issuedQtyStr);

		String standingQtyStr = mTvStandingCorp.getText().toString();
		BigDecimal standingQty = new BigDecimal(standingQtyStr);

		if (issueQty.compareTo(standingQty) > 0) {
			mToastHelper.show("【发料数量】不得多于【现有数量】!");
			return;
		}

		String unIssuedQtyStr = mTvUnReceivedQty.getText().toString();
		BigDecimal unIssuedQty = new BigDecimal(unIssuedQtyStr);
		if(!mCbOverQty.isChecked() && issueQty.compareTo(unIssuedQty)>0){
			mToastHelper.show("【发料数量】不得多于【未发数量】!");
			return;
		}

		WorkOrder2 workOrder = mCurrentWorkOrders.get(0);
		long groupID = System.currentTimeMillis()
				+ new Random().nextInt(999999);
		final List<CUX_WIP_TRANSACTION_INT> transIssues = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		final Map<String, BigDecimal> batchBindQuantity = new HashMap<String, BigDecimal>();
		for (LotQuantityInWarehouse lot : mCurrentLots) {
			if (issueQty.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}

			if (lot.StandingCrop.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

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
			transIssue.ComponentLotNumber = lot.LotNumber;
			transIssue.RequiredQuantity = workOrder.RequiredQuantity;
			transIssue.OpSeq = workOrder.SeqNumber;
			transIssue.TransactionQuantity = issueQty
					.compareTo(lot.StandingCrop) > 0 ? lot.StandingCrop
					: issueQty;
			batchBindQuantity.put(lot.LotNumber, transIssue.TransactionQuantity);
			issueQty = issueQty.subtract(lot.StandingCrop);
			transIssue.ComponentSubinventory = lot.SubInventoryCode;
			transIssue.ComponentLocator = lot.SupplySegment;
			transIssue.Department = workOrder.DepartmentCode;

			transIssues.add(transIssue);
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "领料单提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getTransactionMsg(batchBindQuantity);
		final String wipEntityName = mEdtWipName.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_ISSUE;
				try {

					String result = mRequestManager.submitTransactionInt(transIssues);

					msg.obj = result + "";
					Bundle bundle = new Bundle();
					bundle.putString("msg", transMsg);
					msg.setData(bundle);

					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){

						PrintManager manager = PrintManager.getInstance(mInstance);
						manager.connect();
						manager.print(transMsg.getBytes("GB2312"));
						manager.printOneDimenBarcode(wipEntityName);
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
				case DO_REQUEST_GET_DATA:
					if (msg.obj != null) {

						List<Object> results = (List<Object>) msg.obj;

						mCurrentWorkOrders = (List<WorkOrder2>) results.get(0);
						mCurrentLots = (List<LotQuantityInWarehouse>) results
								.get(1);

						WorkOrder2 workOrder = mCurrentWorkOrders.get(0);

						mTvClassCode.setText(workOrder.ClassCode);
						mTvStartDate.setText(workOrder.ScheduledStartDate);
						mTvCompleteDate.setText(workOrder.ScheduledCompletionDate);
						mTvComponentName.setText(workOrder.ItemDescription);
						mTvIssuedType.setText(workOrder.WipSupplyMeaning);

						BigDecimal unReceivedQty = BigDecimal.ZERO;
						BigDecimal standingCorp = BigDecimal.ZERO;
						if (mCurrentLots != null) {
							for (LotQuantityInWarehouse lot : mCurrentLots) {
								standingCorp = standingCorp.add(lot.StandingCrop);
							}
						}
						unReceivedQty = workOrder.RequiredQuantity
								.subtract(workOrder.QuantityIssued);
						mTvReceivedQty.setText(workOrder.QuantityIssued + "");
						mTvUnReceivedQty.setText(unReceivedQty + "");
						mEdtIssuedQty.setText((unReceivedQty.compareTo(standingCorp) > 0 ? standingCorp : unReceivedQty) + "");
						mTvStandingCorp.setText(standingCorp + "");

						mEdtIssuedQty.requestFocus();

					} else {
						mToastHelper.show("工单及库存现有量获取失败!");
					}
					break;
				case DO_REQUEST_ISSUE:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(msg.getData().getString("msg"));

							clearInputValues();

							mEdtSubInventory.requestFocus();
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
	private void showTransSuccessMsg(String msg) {

		final BigDecimal transQty = new BigDecimal(mEdtIssuedQty.getText().toString());

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

	private String getTransactionMsg(Map<String, BigDecimal> batchBindQty){

		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("提交成功\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("子库:" + mEdtSubInventory.getText().toString() + "\n");
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_class_code)
				+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_start_time)
				+ mTvStartDate.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_complete_time)
				+ mTvCompleteDate.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_component_code)
				+ mEdtComponent.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_component_name)
				+ mTvComponentName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_issued_type)
				+ mTvIssuedType.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_received_qty)
				+ mTvReceivedQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_unreceived_qty)
				+ mTvUnReceivedQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_standing_crop)
				+ mTvStandingCorp.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.mi_issued_qty)
				+ mEdtIssuedQty.getText().toString() + "\n");
		stbMsg.append("------------\n");
		for(Map.Entry<String, BigDecimal> item : batchBindQty.entrySet()){
			stbMsg.append( mResources.getString(R.string.mi_issued_lot) + item.getKey() + " " +  mResources.getString(R.string.mi_issued_lot) + item.getValue() + "\n");
		}


		return stbMsg.toString();
	}

	private void clearInputValues(){
		mEdtSubInventory.setText("");
		mEdtIssuedQty.setText("");
		mEdtComponent.setText("");
		mEdtWipName.setText("");
	}

	private void clearValues() {

		mTvClassCode.setText("");
		mTvCompleteDate.setText("");
		mTvComponentName.setText("");
		mTvIssuedType.setText("");
		mTvReceivedQty.setText("");
		mTvStandingCorp.setText("");
		mTvStartDate.setText("");
		mTvUnReceivedQty.setText("");

		mCurrentWorkOrders = null;
		mCurrentLots = null;
	}

	@Override
	protected void decodeCallback(String barcode) {
		if (mEdtSubInventory.hasFocus()) {
			mEdtSubInventory.setText(barcode);

			if(mOnBatch){
				mEdtIssuedQty.requestFocus();
				getWorkOrder();
			}else{
				mEdtWipName.requestFocus();
			}
		} else if (mEdtWipName.hasFocus()) {
			mEdtWipName.setText(barcode);
			mEdtComponent.requestFocus();
		} else if (mEdtComponent.hasFocus()) {
			mEdtComponent.setText(barcode);
			getWorkOrder();
		}
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
