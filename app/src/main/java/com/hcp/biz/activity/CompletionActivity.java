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

import com.hcp.biz.entities.CompletionInfo;
import com.hcp.biz.query.activity.CompletionQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.CompletionInfoDao;
import com.hcp.dao.SubInventoryAuthorityDao;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CompletionActivity extends ScannerActivity implements TextWatcher {
	private final int DO_REQUEST_SUBMIT = 0;
	private final int DO_REQUEST_GET_WORKORDER = 1;

	private EditText mEdtSubInventory;
	private EditText mEdtWipName;
	private EditText mEdtCompletionQty;

	private TextView mTvClassCode;
	private TextView mTvStartTime;
	private TextView mTvCompleteTime;
	private TextView mTvAssembly;
	private TextView mTvAssemblyName;
	private TextView mTvCompletedQty;
	private TextView mTvRestQty;
	private TextView mTvRemark;

	private Button mBtnSubmit;

	private ImageButton mImbQuery;

	private WORequestManager mRequestManager;

	private ProgressDialog mProgressDialog;

	private WorkOrder3 mCurrentWorkOrder;

	private ToastHelper mToastHelper;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_completion);
		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mEdtSubInventory = (EditText) findViewById(R.id.edt_comp_sub_inventory);
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

		mEdtWipName = (EditText) findViewById(R.id.edt_comp_ori_code);
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

		mEdtCompletionQty = (EditText) findViewById(R.id.edt_comp_completion_qty);
		mEdtCompletionQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvClassCode = (TextView) findViewById(R.id.tv_comp_class_code);
		mTvStartTime = (TextView) findViewById(R.id.tv_comp_start_time);
		mTvCompleteTime = (TextView) findViewById(R.id.tv_comp_complete_time);
		mTvAssembly = (TextView) findViewById(R.id.tv_comp_assembly);
		mTvAssemblyName = (TextView) findViewById(R.id.tv_comp_assembly_name);
		mTvCompletedQty = (TextView) findViewById(R.id.tv_comp_completed_qty);
		mTvRestQty = (TextView) findViewById(R.id.tv_comp_rest_qty);
		mTvRemark = (TextView) findViewById(R.id.tv_comp_remark);

		mBtnSubmit = (Button) findViewById(R.id.btn_comp_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_comp_query);
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

	private void submit() {
		if (mCurrentWorkOrder == null) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String completionQtyStr = mEdtCompletionQty.getText().toString();
		if (TextUtils.isEmpty(completionQtyStr)) {
			mToastHelper.show("请输入【完成数量】!");
			return;
		}

		String subInventory = mEdtSubInventory.getText().toString();
		if (TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("请输入【子库】!");
			return;
		}

		BigDecimal completionQty = new BigDecimal(completionQtyStr);
		BigDecimal restQty = new BigDecimal(mTvRestQty.getText().toString());

		String overCompletion = "N";
		if (completionQty.compareTo(restQty) > 0) {
			overCompletion = "Y";
		}

		final WipTransactionCompletion wip = new WipTransactionCompletion();
		wip.Locator = subInventory
				+ ".00."
				+ (mCurrentWorkOrder.AssemblyPeggingFlag.equals("X") ? StringUtils.trimToEmpty(mCurrentWorkOrder.ProjectNumber) : "");
		wip.Organization = mCurrentWorkOrder.Organization;
		wip.LotNumber = mCurrentWorkOrder.LotNumber;
		wip.OverCompletion = overCompletion;
		wip.ProjectNumber = mCurrentWorkOrder.ProjectNumber;
		wip.Reason = "Trx From Mobile";
		wip.Reference = Device.getIMEI(this);
		wip.SubInventory = subInventory;
		wip.TransactionQuantity = completionQty;
		wip.TransactionType = "WIP Completion";
		wip.WipEntityName = mCurrentWorkOrder.WipEntityName;

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "完工单据提交中...");
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

		@Override
		public boolean handleMessage(Message msg) {

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}

			switch (msg.what) {
				case DO_REQUEST_GET_WORKORDER:
					if (msg.obj != null) {

						mCurrentWorkOrder = (WorkOrder3) msg.obj;

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
						mTvRemark.setText(mCurrentWorkOrder.Remark);
						mTvRestQty
								.setText(mCurrentWorkOrder.QuantityRemaining + "");

						mEdtCompletionQty
								.setText(mCurrentWorkOrder.QuantityRemaining + "");

					} else {
						mToastHelper.show("工单获取失败!");
					}
					break;
				case DO_REQUEST_SUBMIT:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(saveComps());

							clearValues();

							mEdtWipName.requestFocus();

						}else if(RequestUtil.RESPONSE_MSG_ERROR_AUTHORITY.equalsIgnoreCase(result)){
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
		mTvRemark.setText("");
		mTvRestQty.setText("");
		mTvStartTime.setText("");

		mCurrentWorkOrder = null;
	}

	private CompletionInfo saveComps(){
		CompletionInfo comp = new CompletionInfo();
		comp.assembly_code = mTvAssembly.getText().toString();
		comp.assembly_name = mTvAssemblyName.getText().toString();
		comp.class_code = mTvClassCode.getText().toString();
		try {
			comp.completion_time = DateFormat.defaultParse(mTvCompleteTime.getText().toString());
			comp.start_time = DateFormat.defaultParse(mTvStartTime.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		comp.complition_quantity = new BigDecimal(mTvCompletedQty.getText().toString());
		comp.create_by = AppCache.getInstance().getLoginUser();
		comp.create_time = new Date(System.currentTimeMillis());
		comp.remaining_quantity = new BigDecimal(mTvRestQty.getText().toString());
		comp.remark = mTvRemark.getText().toString();
		comp.sub_inventory = mEdtSubInventory.getText().toString();
		comp.transaction_quantity = new BigDecimal(mEdtCompletionQty.getText().toString());
		comp.wip_entity_name = mEdtWipName.getText().toString();

		try {
			CompletionInfoDao.getInstance(mInstance).onInsert().insertCompletion(comp);
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}
		return comp;
	}

	private void showTransSuccessMsg(CompletionInfo completionInfo) {

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(getSuccessMsg().toString()).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

	}

	private String getSuccessMsg(){

		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("工 单 完 工\n");
		stbMsg.append("--------------------------\n");
		// stbMsg.append(mResources.getString(R.string.mr_ori_code) +
		// mEdtWipName.getText().toString() + "\n");
		stbMsg.append("子库:" + mEdtSubInventory.getText().toString() + "\n");
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append("工单类型:"+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append("开始时间:"+ mTvStartTime.getText().toString() + "\n");
		stbMsg.append("完成时间:" + mTvCompleteTime.getText().toString() + "\n");

		stbMsg.append("制品号码:" + mTvAssembly.getText().toString() + "\n");
		stbMsg.append("制品名称:" + mTvAssemblyName.getText().toString() + "\n");
		stbMsg.append("已完工:" + mTvCompletedQty.getText().toString() + "\n");
		stbMsg.append("剩余数量:" + mTvRestQty.getText().toString() + "\n");
		stbMsg.append("工单备注:" + mTvRemark.getText().toString() + "\n");
		stbMsg.append("完工数量:" + mEdtCompletionQty.getText().toString() + "\n");

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
