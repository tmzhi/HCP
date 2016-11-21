package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.http.RequestUtil;
import com.hcp.intraware.biz.select.activity.ISPComponentSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPWipIssueSubInventorySelectActivity;
import com.hcp.intraware.biz.select.activity.ISPWipSelectActivity;
import com.hcp.intraware.constants.RequestCode;
import com.hcp.intraware.constants.RequestKey;
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPAccountAliasReceipt;
import com.hcp.intraware.entity.ISPSupplyType;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ISPWipIssueActivity extends ScannerActivity implements
		TextWatcher, OnClickListener {

	private EditText mEdtOrg;
	private EditText mEdtWip;
	private EditText mEdtProjectNo;
	private EditText mEdtComponent;
	private EditText mEdtOperation;
	private EditText mEdtUom;
	private EditText mEdtSubinventory;
	private EditText mEdtLocator;
	private EditText mEdtLotNo;
	private EditText mEdtQuantity;

	private TextView mTvTime;

	private ImageButton mImbQueryWip;
	private ImageButton mImbQuerySubInventory;
	private ImageButton mImbQueryComponent;

	private Button mBtnSubmit;

	private List<View> mViewList = new ArrayList<View>();

	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;
	private String mMsg;

	private WORequestManager mRequestManager;
	private AppConfig mAppConfig;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intraware_wip_issue);

		initView();
	}

	private void initView(){
		mAppConfig = AppConfig.getInstance(mInstance);
		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);

		mEdtWip = (EditText)findViewById(R.id.edt_intraware_wi_wip);
		mEdtWip.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocator = (EditText) findViewById(R.id.edt_intraware_wi_locator);
		mEdtLocator.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtProjectNo = (EditText) findViewById(R.id.edt_intraware_wi_project_no);
		mEdtProjectNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLotNo = (EditText) findViewById(R.id.edt_intraware_wi_batch);
		mEdtLotNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtUom = (EditText) findViewById(R.id.edt_intraware_wi_uom);
		mEdtUom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtQuantity = (EditText) findViewById(R.id.edt_intraware_wi_quantity);
		mEdtQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtOperation = (EditText) findViewById(R.id.edt_intraware_wi_operation);
		mEdtOperation.setOnTouchListener(mHideKeyBoardTouchEvent);

		mBtnSubmit = (Button) findViewById(R.id.btn_intraware_wi_submit);
		mBtnSubmit.setOnClickListener(this);

		mImbQuerySubInventory = (ImageButton) findViewById(R.id.imb_intraware_wi_subinventory_query);
		mImbQuerySubInventory.setOnClickListener(this);

		mImbQueryComponent = (ImageButton) findViewById(R.id.imb_intraware_wi_component_query);
		mImbQueryComponent.setOnClickListener(this);

		mImbQueryWip = (ImageButton) findViewById(R.id.imb_intraware_wi_wip_query);
		mImbQueryWip.setOnClickListener(this);

		mEdtComponent = (EditText) findViewById(R.id.edt_intraware_wi_component);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtComponent.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getWip();
				}
			}
		});
		mEdtComponent.addTextChangedListener(this);

		mEdtOrg = (EditText) findViewById(R.id.edt_intraware_wi_org);
		mEdtOrg.setOnTouchListener(mHideKeyBoardTouchEvent);
		if(mAppConfig.getOrganizationId() > 0){
			mEdtOrg.setText(mAppConfig.getOrganizationCode());
		}
		mEdtOrg.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				clearValues(false);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});


		mEdtComponent = (EditText) findViewById(R.id.edt_intraware_wi_component);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubinventory = (EditText) findViewById(R.id.edt_intraware_wi_sub_inventory);
		mEdtSubinventory.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtSubinventory.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				mEdtLocator.setText("");
			}
		});

		mTvTime = (TextView) findViewById(R.id.tv_intraware_wi_date);
		mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

	}

	private void clearValues(boolean includeOrgCode){

		if(includeOrgCode){
			mEdtOrg.setText("");
		}

		mEdtWip.setText("");
		mEdtProjectNo.setText("");
		mEdtComponent.setText("");
		mEdtOperation.setText("");
		mEdtUom.setText("");
		mEdtSubinventory.setText("");
		mEdtLocator.setText("");
		mEdtLotNo.setText("");
		mEdtQuantity.setText("");
		mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
	}

	private void getWip() {

	}

	private void submit() {

		mMsg = null;

		String checkResult = saveCheck();
		if (!TextUtils.isEmpty(checkResult)) {
			mToastHelper.show(checkResult);
			return;
		}

		final String org = mEdtOrg.getText().toString();
		final String wip = mEdtWip.getText().toString();
		final String component = mEdtComponent.getText().toString();
		final String locator = mEdtLocator.getText().toString();
		final String lotno = mEdtLotNo.getText().toString();
		final String quantity = mEdtQuantity.getText().toString();

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"提交", "领料交易提交中...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {
					if (!TextUtils.isEmpty(mMsg = checkWip(org, wip))) {
						return;
					}

					if (!TextUtils.isEmpty(mMsg = checkComponent(org, wip, component))) {
						return;
					}

					if (!TextUtils.isEmpty(mMsg = checkProjectControl(org, component, locator))) {
						return;
					}

					ISPSupplyType supplyType = getSupplyType(org, wip, component);
					if (supplyType != null) {
						if (!"1".equals(supplyType.SupplyType)) {
							mMsg = String.format("在【%s】的工单【%s】中料件【%s】的供应方式不为推式,请核对! ", org, wip, component);
							return;
						} else if ("0".equals(supplyType.SubInventory)) {
							mMsg = String.format("在【%s】的工单【%s】中料件【%s】的子库未维护，请通知【%s】维护供应子库! ", org, wip, component, org);
							return;
						}
					} else {
						mMsg = "未获取到供应方式!";
						return;
					}

					if (!TextUtils.isEmpty(mMsg = checkQuantity(org, wip, component, lotno, quantity, supplyType.Locator))) {
						return;
					}

					final ISPAccountAliasReceipt receipt = new ISPAccountAliasReceipt();
					receipt.OrganizationCode = org;
					receipt.TransactionDate = mTvTime.getText().toString();

					String transDescription = getTransactionDescription("WIP Issue");
					if (TextUtils.isEmpty(transDescription)) {
						mMsg = "交易类型获取失败!";
						return;
					}
					receipt.TransactionType = transDescription;

					receipt.TransactionSourceType = "Account alias";

					String sourceDescription = getSourceDescription(org, "CUX ISP MISC SOURCE");
					if (TextUtils.isEmpty(sourceDescription)) {
						mMsg = "交易来源获取失败!";
						return;
					}
					receipt.TransactionSource = sourceDescription;

					receipt.ItemCode = mEdtComponent.getText().toString();
					receipt.SubInventoryCode = mEdtSubinventory.getText().toString();
					receipt.Locator = mEdtLocator.getText().toString();
					receipt.LotNumber = mEdtLotNo.getText().toString();
					receipt.TransactionUom = mEdtUom.getText().toString();
					receipt.Quantity = new BigDecimal(mEdtQuantity.getText().toString());

					String location = getLocationCode(org);
					if (TextUtils.isEmpty(location)) {
						mMsg = "地址获取失败!";
						return;
					}
					receipt.Location = location;

					receipt.ReasonName = "Trx From Mobile";
					receipt.Attribute1 = mEdtWip.getText().toString();
					receipt.Attribute10 = org + "_WIP Issue_" + mTvTime.getText().toString();
					receipt.TransactionReference = "JobOrder : " + mEdtWip.getText().toString();
					receipt.UserName = "INFOSYS";

					result = mRequestManager.submitISPAccountAliasReceipt(receipt);
					if(result != null && result.toUpperCase().contains(RequestUtil.RESPONSE_MSG_SUCCESS)){
						try{
							CUX_WIP_TRANSACTION_INT transInt = mRequestManager.getISPWipEntity(org, wip, component, lotno);
							List<CUX_WIP_TRANSACTION_INT> list = new ArrayList<CUX_WIP_TRANSACTION_INT>();
							list.add(transInt);
							result = mRequestManager.submitTransactionInt(list);
							if(result != null && result.toUpperCase().contains(RequestUtil.RESPONSE_MSG_SUCCESS)){
								mMsg = "提交成功";

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										clearValues(false);
									}
								});
							}else{
								mMsg = result;
							}
						}catch (Exception ex){
							if(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(ex.getMessage())){
								mMsg = "找不到WIP数据或者现有量不足";
								return;
							}else{
								mMsg = ex.getMessage();
								return;
							}
						}
					}else{
						mMsg = result;
					}
				} catch (Exception e) {
					mMsg = "异常:" + e.getMessage();
				} finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
								mProgressDialog = null;
							}

							mToastHelper.show(mMsg);
						}
					});
				}
			}
		}).start();
	}

	private String checkWip(String org, String wip) throws Exception{
		String msg = null;
		boolean res = mRequestManager.ispIsWip(org, wip);
		if(!res){
			msg = "找不到需求方工单，请PMC检查单据是否已关联。";
		}
		return msg;
	}

	private String checkComponent(String org, String wip, String component) throws Exception{
		String msg = null;
		boolean res = mRequestManager.ispIsWipContainsComponent(org, wip, component);
		if(!res){
			msg = String.format("在【%s】的工单【%s】中不存在料件【%s】,请核对! ", org, wip, component);
		}
		return msg;
	}

	private String checkProjectControl(String org, String component, String locator) throws Exception{
		String msg = null;
		boolean res = mRequestManager.ispIsProjectControl(org, component, locator);
		if(!res){
			msg = "物料受项目控制,请先做项目转移!";
		}
		return msg;
	}

	private ISPSupplyType getSupplyType(String org, String wip, String component) throws Exception{
		ISPSupplyType res;
		res = mRequestManager.getISPSupplyType(org, wip, component);
		return res;
	}

	private String getTransactionDescription(String transtype) throws Exception{
		String res;
		res = mRequestManager.getISPTransactionDescription(transtype);
		return res;
	}

	private String getSourceDescription(String org, String transtype) throws Exception{
		String res;
		res = mRequestManager.getISPSourceDescription(org, transtype);
		return res;
	}

	private String getLocationCode(String org) throws Exception{
		String res;
		res = mRequestManager.getISPLocationCode(org);
		return res;
	}

	private String checkQuantity(String org, String wip, String component, String lotno, String quantity, String locator) throws Exception{
		String msg = null;
		try
		{
			boolean res = mRequestManager.ispIsQuantityAvailable(org, wip, component, lotno, quantity);
			if(!res){
				msg = String.format("在【%s】中货位【%s】中批号【%s】的物料可用量不足【%s】,请通知【%s】在【%s】中补足现有量后再处理.", org, locator, lotno, quantity, org, locator);
			}
		}catch (Exception ex){
			throw new Exception("数量校验异常:" + ex.getMessage());
		}
		return msg;
	}

	private String doRequest(ISPAccountAliasReceipt receipt) throws Exception{
		String msg = null;
		String result = mRequestManager.submitISPAccountAliasReceipt(receipt);
		if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
			msg = "提交成功!";
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					clearValues(false);
				}
			});
		}else{
			mMsg = result;
		}
		return msg;
	}

	private String saveCheck(){
		String org = mEdtOrg.getText().toString();
		if(TextUtils.isEmpty(org)){
			return "【组织】不得为空!";
		}

		String wip = mEdtWip.getText().toString();
		if(TextUtils.isEmpty(wip)){
			return "【工单号】不得为空!";
		}

		String component = mEdtWip.getText().toString();
		if(TextUtils.isEmpty(component)){
			return "【料件编号】不得为空!";
		}

		String operano = mEdtWip.getText().toString();
		if(TextUtils.isEmpty(operano)){
			return "【工序编号】不得为空!";
		}

		String uom = mEdtUom.getText().toString();
		if(TextUtils.isEmpty(uom)){
			return "【单位】不得为空!";
		}

		String subinventory = mEdtSubinventory.getText().toString();
		if(TextUtils.isEmpty(subinventory)){
			return "【子库】不得为空!";
		}

		String locator = mEdtLocator.getText().toString();
		if(TextUtils.isEmpty(locator)){
			return "【货位】不得为空!";
		}

		String quantity = mEdtQuantity.getText().toString();
		if(TextUtils.isEmpty(quantity)){
			return "【货位】不得为空!";
		}

		return "";
	}


	private void showTransSuccessMsg(String msg) {

//		final BigDecimal transQty = new BigDecimal(mEdtIssuedQty.getText().toString());
//
//		AlertDialog dialog = new AlertDialog.Builder(this)
//				.setMessage(msg)
//				.setPositiveButton("确定", null)
//				.create();
//		dialog.setCanceledOnTouchOutside(false);
//		dialog.setCancelable(false);
//		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				if(mOnBatch){
//					Intent intent = new Intent();
//					intent.putExtra(BatchTransactionActivity.REQUEST_ID, mBatchID);
//					intent.putExtra(BatchTransactionActivity.REQUEST_TRANSACTION_QUANTITY, transQty);
//					intent.putExtra(BatchTransactionActivity.REQUEST_STATE, BatchTransactionActivity.STATE_SUBMITTED);
//
//					setResult(BatchTransactionActivity.STATE_SUBMITTED, intent);
//
//					finish();
//				}
//			}
//		});
//		dialog.show();
	}

	private String getTransactionMsg(Map<String, BigDecimal> batchBindQty){

//		StringBuilder stbMsg = new StringBuilder();
//		stbMsg.append("提交成功\n");
//		stbMsg.append("--------------------------\n");
//		stbMsg.append("子库:" + mEdtSubInventory.getText().toString() + "\n");
//		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_class_code)
//				+ mTvClassCode.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_start_time)
//				+ mTvStartDate.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_complete_time)
//				+ mTvCompleteDate.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_component_code)
//				+ mEdtComponent.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_component_name)
//				+ mTvComponentName.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_issued_type)
//				+ mTvIssuedType.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_received_qty)
//				+ mTvReceivedQty.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_unreceived_qty)
//				+ mTvUnReceivedQty.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_standing_crop)
//				+ mTvStandingCorp.getText().toString() + "\n");
//		stbMsg.append(mResources.getString(R.string.mi_issued_qty)
//				+ mEdtIssuedQty.getText().toString() + "\n");
//		stbMsg.append("------------\n");
//		for(Map.Entry<String, BigDecimal> item : batchBindQty.entrySet()){
//			stbMsg.append( mResources.getString(R.string.mi_issued_lot) + item.getKey() + " " +  mResources.getString(R.string.mi_issued_lot) + item.getValue() + "\n");
//		}
//
//
//		return stbMsg.toString();
		return ";";
	}

	@Override
	protected void decodeCallback(String barcode) {
		if (mEdtSubinventory.hasFocus()) {
			mEdtSubinventory.setText(barcode);
			mEdtLocator.requestFocus();
		} else if (mEdtLocator.hasFocus()) {
			mEdtLocator.setText(barcode);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.imb_intraware_wi_wip_query:
				selectWip();
				break;
			case R.id.imb_intraware_wi_component_query:
				selectComponent();
				break;
			case R.id.imb_intraware_wi_subinventory_query:
				selectSubInventory();
				break;
			case R.id.btn_intraware_wi_submit:
				submit();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode){
			case RequestCode.ISP_WIP_ISSUE_SUBINVENTORY:
				if(data != null){
					mEdtSubinventory.setText(data.getStringExtra(ResultKey.KEY_WIP_ISSUE_SUBINVENTORY));
					mEdtLotNo.setText(data.getStringExtra(ResultKey.KEY_WIP_ISSUE_LOTNO));
					mEdtLocator.setText(data.getStringExtra(ResultKey.KEY_WIP_ISSUE_LOCATOR));
				}
				break;
			case RequestCode.ISP_WIP:
				if(data != null){
					mEdtWip.setText(data.getStringExtra(ResultKey.KEY_WIP));
					mEdtProjectNo.setText(data.getStringExtra(ResultKey.KEY_WIP_PROJECT_NO));
				}
				break;
			case RequestCode.ISP_COMPONENT:
				if(data != null){
					mEdtOperation.setText(data.getStringExtra(ResultKey.KEY_COMPONENT_OPERATION));
					mEdtComponent.setText(data.getStringExtra(ResultKey.KEY_COMPONENT));
					mEdtUom.setText(data.getStringExtra(ResultKey.KEY_COMPONENT_UOM));
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectSubInventory(){
		Intent intent = new Intent(mInstance, ISPWipIssueSubInventorySelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_COMPONENT, mEdtComponent.getText().toString());
		intent.putExtra(RequestKey.ISP_PROJECTNO, mEdtProjectNo.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_WIP_ISSUE_SUBINVENTORY);
	}

	private void selectWip(){
		Intent intent = new Intent(mInstance, ISPWipSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_WIP);
	}

	private void selectComponent(){
		Intent intent = new Intent(mInstance, ISPComponentSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_WIP, mEdtWip.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_COMPONENT);
	}
}
