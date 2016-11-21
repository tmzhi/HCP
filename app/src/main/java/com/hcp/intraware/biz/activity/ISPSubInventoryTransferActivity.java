package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.SubInventoryTransaction;
import com.hcp.http.RequestUtil;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferComponentSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferLocatorSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferLotNoSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferProjectNoSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferSubInventorySelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventoryTransferWipSelectActivity;
import com.hcp.intraware.constants.RequestCode;
import com.hcp.intraware.constants.RequestKey;
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPAccountAliasReceipt;
import com.hcp.intraware.entity.ISPSupplyType;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.AppCache;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ISPSubInventoryTransferActivity extends ScannerActivity implements
		TextWatcher, OnClickListener {

	private EditText mEdtOrg;
	private EditText mEdtWip;
	private EditText mEdtComponent;
	private EditText mEdtLotNo;
	private EditText mEdtSubinventoryFrom;
	private EditText mEdtLocatorFrom;
	private EditText mEdtLocatorProjectNoFrom;
	private EditText mEdtSubinventoryTo;
	private EditText mEdtLocatorTo;
	private EditText mEdtLocatorProjectNoTo;
	private EditText mEdtQuantity;

	private TextView mTvTime;

	private ImageButton mImbQueryWip;
	private ImageButton mImbQuerySubInventoryFrom;
	private ImageButton mImbQueryLocatorFrom;
	private ImageButton mImbQueryLocatorProjectNoFrom;
	private ImageButton mImbQuerySubInventoryTo;
	private ImageButton mImbQueryLocatorTo;
	private ImageButton mImbQueryLocatorProjectNoTo;
	private ImageButton mImbQueryComponent;
	private ImageButton mImbQueryLotNo;

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
		setContentView(R.layout.activity_intraware_subinventory_transfer);

		initView();
	}

	private void initView(){
		mAppConfig = AppConfig.getInstance(mInstance);
		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);

		mEdtWip = (EditText)findViewById(R.id.edt_intraware_st_wip);
		mEdtWip.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubinventoryFrom = (EditText) findViewById(R.id.edt_intraware_st_sub_inventory_from);
		mEdtSubinventoryFrom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocatorFrom = (EditText) findViewById(R.id.edt_intraware_st_locator_from);
		mEdtLocatorFrom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocatorProjectNoFrom = (EditText) findViewById(R.id.edt_intraware_st_locator_project_no_from);
		mEdtLocatorProjectNoFrom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocatorTo = (EditText) findViewById(R.id.edt_intraware_st_locator_to);
		mEdtLocatorTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubinventoryTo = (EditText) findViewById(R.id.edt_intraware_st_sub_inventory_to);
		mEdtSubinventoryTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocatorProjectNoTo = (EditText) findViewById(R.id.edt_intraware_st_locator_project_no_to);
		mEdtLocatorProjectNoTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLotNo = (EditText) findViewById(R.id.edt_intraware_st_lot_no);
		mEdtLotNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtQuantity = (EditText) findViewById(R.id.edt_intraware_st_quantity);
		mEdtQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponent = (EditText) findViewById(R.id.edt_intraware_st_component);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtComponent.addTextChangedListener(this);

		mEdtOrg = (EditText) findViewById(R.id.edt_intraware_st_org);
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

		mTvTime = (TextView) findViewById(R.id.tv_intraware_st_date);
		mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

		mBtnSubmit = (Button) findViewById(R.id.btn_intraware_st_submit);
		mBtnSubmit.setOnClickListener(this);

		mImbQueryComponent = (ImageButton) findViewById(R.id.imb_intraware_st_component_query);
		mImbQueryComponent.setOnClickListener(this);

		mImbQueryWip = (ImageButton) findViewById(R.id.imb_intraware_st_wip_query);
		mImbQueryWip.setOnClickListener(this);

		mImbQueryLocatorFrom = (ImageButton) findViewById(R.id.imb_intraware_st_locator_from_query);
		mImbQueryLocatorFrom.setOnClickListener(this);

		mImbQuerySubInventoryFrom = (ImageButton) findViewById(R.id.imb_intraware_st_subinventory_from_query);
		mImbQuerySubInventoryFrom.setOnClickListener(this);

		mImbQueryLocatorProjectNoFrom = (ImageButton) findViewById(R.id.imb_intraware_st_locator_project_no_from_query);
		mImbQueryLocatorProjectNoFrom.setOnClickListener(this);

		mImbQueryLocatorTo = (ImageButton) findViewById(R.id.imb_intraware_st_locator_to_query);
		mImbQueryLocatorTo.setOnClickListener(this);

		mImbQuerySubInventoryTo = (ImageButton) findViewById(R.id.imb_intraware_st_subinventory_to_query);
		mImbQuerySubInventoryTo.setOnClickListener(this);

		mImbQueryLocatorProjectNoTo = (ImageButton) findViewById(R.id.imb_intraware_st_locator_project_no_to_query);
		mImbQueryLocatorProjectNoTo.setOnClickListener(this);

		mImbQueryLotNo = (ImageButton) findViewById(R.id.imb_intraware_st_lot_no_query);
		mImbQueryLotNo.setOnClickListener(this);
	}

	private void clearValues(boolean includeOrgCode){

		if(includeOrgCode){
			mEdtOrg.setText("");
		}

		mEdtWip.setText("");
		mEdtLotNo.setText("");
		mEdtComponent.setText("");
		mEdtQuantity.setText("");
		mEdtLocatorFrom.setText("");
		mEdtLocatorProjectNoFrom.setText("");
		mEdtSubinventoryFrom.setText("");
		mEdtSubinventoryTo.setText("");
		mEdtLocatorTo.setText("");
		mEdtLocatorProjectNoTo.setText("");
		mEdtSubinventoryTo.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
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
		final String subinventoryFrom = mEdtSubinventoryFrom.getText().toString();
		final String locatorFrom = mEdtLocatorFrom.getText().toString();
		final String projectnoFrom = mEdtLocatorProjectNoFrom.getText().toString();
		final String subinventoryTo = mEdtSubinventoryTo.getText().toString();
		final String locatorTo = mEdtLocatorTo.getText().toString();
		final String projectnoTo = mEdtLocatorProjectNoTo.getText().toString();
		final String lotno = mEdtLotNo.getText().toString();
		final String quantity = mEdtQuantity.getText().toString();
		final String time = mTvTime.getText().toString();

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"提交", "项目转移提交中...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {
					if (!TextUtils.isEmpty(mMsg = checkQuantityEnough(org, wip, component, lotno, projectnoFrom, quantity))) {
						return;
					}

					SubInventoryTransaction subTran = new SubInventoryTransaction();
					subTran.OrganizationCode =org;
					subTran.TransactionType = "Project Transfer";
					subTran.InventoryItemNumber = component;
					subTran.SubInventoryCode = subinventoryFrom;
					subTran.Locator = subinventoryFrom + "." + locatorFrom + "." + projectnoFrom;
					subTran.ProjectNumber = projectnoFrom;
					subTran.LotNumber = lotno;
					subTran.TransferSubInventory = subinventoryTo;
					subTran.TransferLocator = subinventoryTo + "." + locatorTo + "." + projectnoTo;
					subTran.TransactionQuantity = new BigDecimal(quantity);
					subTran.WipEntityName = wip;
					subTran.DocumentNumber = org + "_Project_Transfer_" + time;
					subTran.CreatedByName = AppCache.getInstance().getLoginUser();
					subTran.ReasonName = "Trx From Mobile";
					subTran.TransactionReference = "Trx From Mobile : " + Device.getIMEI(mInstance);

					List<SubInventoryTransaction> subTrans = new ArrayList<SubInventoryTransaction>();
					subTrans.add(subTran);
					result = mRequestManager.submitSubInventoryTransaction(subTrans);

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

	private String checkQuantityEnough(String org, String wip, String component, String lotno, String projectno, String quantity) throws Exception{
		String msg = null;
		boolean res = mRequestManager.ispIsSubInventoryTransferQuantityEnough(org, wip, component, lotno, projectno, quantity);
		if(!res){
			msg = "Can’t find data in ISP demand org dependent on ISP supply org’s submission data 在需求方找不到对应的物料或现有量不足 ";
		}
		return msg;
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

		String subinventoryFrom = mEdtSubinventoryFrom.getText().toString();
		if(TextUtils.isEmpty(subinventoryFrom)){
			return "【原始子库】不得为空!";
		}

		String locatorFrom = mEdtLocatorFrom.getText().toString();
		if(TextUtils.isEmpty(locatorFrom)){
			return "【原始货位】不得为空!";
		}

		String subinventoryTo = mEdtSubinventoryTo.getText().toString();
		if(TextUtils.isEmpty(subinventoryTo)){
			return "【目标子库】不得为空!";
		}

		String locatorTo = mEdtLocatorTo.getText().toString();
		if(TextUtils.isEmpty(locatorTo)){
			return "【目标货位】不得为空!";
		}

		String locatorProjectNoTo = mEdtLocatorProjectNoTo.getText().toString();
		if(TextUtils.isEmpty(locatorProjectNoTo)){
			return "【目标货位项目号】不得为空!";
		}

		String quantity = mEdtQuantity.getText().toString();
		if(TextUtils.isEmpty(quantity)){
			return "【数量】不得为空!";
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
		if (mEdtOrg.hasFocus()) {
			mEdtOrg.setText(barcode);
			mEdtComponent.requestFocus();
		} else if (mEdtComponent.hasFocus()) {
			mEdtComponent.setText(barcode);
			mEdtSubinventoryFrom.requestFocus();
		} else if (mEdtSubinventoryFrom.hasFocus()) {
			mEdtSubinventoryFrom.setText(barcode);
			mEdtLocatorFrom.requestFocus();
		}else if (mEdtLocatorFrom.hasFocus()) {
			mEdtLocatorFrom.setText(barcode);
			mEdtLocatorProjectNoFrom.requestFocus();
		} else if (mEdtLocatorProjectNoFrom.hasFocus()) {
			mEdtLocatorProjectNoFrom.setText(barcode);
			mEdtLotNo.requestFocus();
		}else if (mEdtLotNo.hasFocus()) {
			mEdtLotNo.setText(barcode);
			mEdtSubinventoryTo.requestFocus();
		}else if (mEdtSubinventoryTo.hasFocus()) {
			mEdtSubinventoryTo.setText(barcode);
			mEdtLocatorTo.requestFocus();
		}else if (mEdtLocatorTo.hasFocus()) {
			mEdtLocatorTo.setText(barcode);
			mEdtLocatorProjectNoTo.requestFocus();
		}else if (mEdtLocatorProjectNoTo.hasFocus()) {
			mEdtLocatorProjectNoTo.setText(barcode);
			mEdtQuantity.requestFocus();
		}else {
			mEdtWip.setText(barcode);
			mEdtWip.requestFocus();
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
			case R.id.imb_intraware_st_wip_query:
				selectWip();
				break;
			case R.id.imb_intraware_st_component_query:
				selectComponent();
				break;
			case R.id.imb_intraware_st_subinventory_from_query:
				selectSubInventoryFrom();
				break;
			case R.id.imb_intraware_st_locator_from_query:
				selectLocatorFrom();
				break;
			case R.id.imb_intraware_st_locator_project_no_from_query:
				selectLocatorProjectNoFrom();
				break;
			case R.id.imb_intraware_st_subinventory_to_query:
				selectSubInventoryTo();
				break;
			case R.id.imb_intraware_st_locator_to_query:
				selectLocatorTo();
				break;
			case R.id.imb_intraware_st_locator_project_no_to_query:
				selectLocatorProjectNoTo();
				break;
			case R.id.imb_intraware_st_lot_no_query:
				selectLotNo();
				break;
			case R.id.btn_intraware_st_submit:
				submit();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode){
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_WIP:
				if(data != null){
					mEdtWip.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_WIP));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_COMPONENT:
				if(data != null){
					mEdtComponent.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_COMPONENT));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_LOT_NO:
				if(data != null){
					mEdtLotNo.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOT_NO));
					mEdtQuantity.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_REMAINING_QUANTITY));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_FROM:
				if(data != null){
					mEdtSubinventoryFrom.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_SUBINVENTORY));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_FROM:
				if(data != null){
					mEdtLocatorFrom.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOCATOR));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_FROM:
				if(data != null){
					mEdtLocatorProjectNoFrom.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_TO:
				if(data != null){
					mEdtSubinventoryTo.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_SUBINVENTORY));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_TO:
				if(data != null){
					mEdtLocatorTo.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOCATOR));
				}
				break;
			case RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_TO:
				if(data != null){
					mEdtLocatorProjectNoTo.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO));
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectSubInventoryFrom(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferSubInventorySelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_FROM);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_FROM);
	}

	private void selectLocatorFrom(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferLocatorSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_SUBINVENTORY, mEdtSubinventoryFrom.getText().toString());
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_FROM);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_FROM);
	}

	private void selectLocatorProjectNoFrom(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferProjectNoSelectActivity.class);
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_FROM);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_FROM);
	}

	private void selectSubInventoryTo(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferSubInventorySelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_TO);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_SUBINVENTORY_TO);
	}

	private void selectLocatorTo(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferLocatorSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_SUBINVENTORY, mEdtSubinventoryTo.getText().toString());
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_TO);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_TO);
	}

	private void selectLocatorProjectNoTo(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferProjectNoSelectActivity.class);
		intent.putExtra(RequestKey.ISP_REQUEST_CODE, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_TO);
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOCATOR_PROJECT_NO_TO);
	}

	private void selectLotNo(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferLotNoSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_COMPONENT, mEdtComponent.getText().toString());
		intent.putExtra(RequestKey.ISP_SUBINVENTORY, mEdtSubinventoryFrom.getText().toString());
		intent.putExtra(RequestKey.ISP_LOCATOR, mEdtComponent.getText().toString());
		intent.putExtra(RequestKey.ISP_PROJECTNO, mEdtLocatorProjectNoFrom.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_LOT_NO);
	}

	private void selectWip(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferWipSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_COMPONENT, mEdtComponent.getText().toString());
		intent.putExtra(RequestKey.ISP_PROJECTNO, mEdtLocatorProjectNoFrom.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_WIP);
	}

	private void selectComponent(){
		Intent intent = new Intent(mInstance, ISPSubInventoryTransferComponentSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY_TRANSFER_COMPONENT);
	}
}
