package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.ProcedureTransfer;
import com.hcp.entities.WipTransactionCompletion;
import com.hcp.http.RequestUtil;
import com.hcp.intraware.biz.select.activity.ISPPurchaseReceiveLocatorSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPPurchaseReceiveOrderSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPPurchaseReceiveSubInventorySelectActivity;
import com.hcp.intraware.constants.RequestCode;
import com.hcp.intraware.constants.RequestKey;
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPPurchaseReceive;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ISPPurchaseReceiveActivity extends ScannerActivity implements
		TextWatcher, OnClickListener {

	private EditText mEdtOrg;
	private EditText mEdtSubinventory;
	private EditText mEdtLocator;
	private EditText mEdtOrder;
	private EditText mEdtOrderLine;
	private EditText mEdtComponent;
	private EditText mEdtComponentDescription;
	private EditText mEdtShipment;
	private EditText mEdtShipmentLine;
	private EditText mEdtUom;
	private EditText mEdtLotNo;
	private EditText mEdtProjectNo;
	private EditText mEdtBarcode;
	private EditText mEdtPurchaseQuantity;
	private EditText mEdtReceiveQuantity;
	private EditText mEdtReceivedQuantity;

	private TextView mTvTime;

	private ImageButton mImbQuerySubinventory;
	private ImageButton mImbQueryLocator;
	private ImageButton mImbQueryOrder;
	private ViewPager mVpContainer;
	private RadioGroup mRgSegment;

	private Button mBtnSubmit;

	private List<View> mViewList = new ArrayList<View>();

	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;
	private String mMsg;

	private WORequestManager mRequestManager;
	private AppConfig mAppConfig;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intraware_purchase_receive);

		initView();
	}

	private void initView(){
		mAppConfig = AppConfig.getInstance(mInstance);
		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);

		View viewOrder = View.inflate(mInstance, R.layout.fragment_purchase_receive_order, null);
		mEdtOrder = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_order);
		mEdtOrder.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtOrderLine = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_line);
		mEdtOrderLine.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponent = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_component);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponentDescription = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_component_description);

		mEdtShipment = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_shipment);
		mEdtShipment.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtShipmentLine = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_shipment_line);
		mEdtShipmentLine.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtUom = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_uom);
		mEdtUom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLotNo = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_lotno);
		mEdtLotNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtProjectNo = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_project_no);
		mEdtProjectNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtBarcode = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_barcode);
		mEdtBarcode.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtReceiveQuantity = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_receive_quantity);
		mEdtReceiveQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtPurchaseQuantity = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_purchase_quantity);
		mEdtPurchaseQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtReceivedQuantity = (EditText) viewOrder.findViewById(R.id.edt_intraware_purchase_receive_received_quantity);
		mEdtReceivedQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mImbQueryOrder = (ImageButton) viewOrder.findViewById(R.id.imb_intraware_purchase_receive_order_query);
		mImbQueryOrder.setOnClickListener(this);

		View viewSub = View.inflate(mInstance, R.layout.fragment_purchase_receive_subinventory, null);
		mEdtOrg = (EditText) viewSub.findViewById(R.id.edt_intraware_purchase_receive_organization);
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

		mEdtSubinventory = (EditText) viewSub.findViewById(R.id.edt_intraware_purchase_receive_subinventory);
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

		mEdtLocator = (EditText) viewSub.findViewById(R.id.edt_intraware_purchase_receive_locator);
		mEdtLocator.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvTime = (TextView) viewSub.findViewById(R.id.tv_intraware_purchase_receive_date);
		mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

		mImbQueryLocator = (ImageButton) viewSub.findViewById(R.id.imb_intraware_purchase_receive_locator_query);
		mImbQueryLocator.setOnClickListener(this);

		mImbQuerySubinventory = (ImageButton) viewSub.findViewById(R.id.imb_intraware_purchase_receive_subinventory_query);
		mImbQuerySubinventory.setOnClickListener(this);

		mVpContainer = (ViewPager) findViewById(R.id.vp_intraware_purchase_receive_container);
		mVpContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mRgSegment.check(mRgSegment.getChildAt(position).getId());
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mRgSegment = (RadioGroup) findViewById(R.id.rg_intraware_purchase_receive_segment);
		mRgSegment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int index = -1;
				for (int i = 0; i < mRgSegment.getChildCount(); i++) {
					if (mRgSegment.getChildAt(i).getId() == checkedId) {
						index = i;
					}
				}

				if (index > -1) {
					mVpContainer.setCurrentItem(index);
				}
			}
		});

		mBtnSubmit = (Button) findViewById(R.id.btn_intraware_purchase_receive_submit);
		mBtnSubmit.setOnClickListener(this);

		mViewList.add(viewOrder);
		mViewList.add(viewSub);
		mVpContainer.setAdapter(mPageAdapter);

		mRgSegment.check(mRgSegment.getChildAt(0).getId());
	}

	private void clearValues(boolean includeOrgCode){

		if(includeOrgCode){
			mEdtOrg.setText("");
		}

		mEdtSubinventory.setText("");
		mEdtLocator.setText("");
		mEdtProjectNo.setText("");
		mEdtLotNo.setText("");
		mEdtUom.setText("");
		mEdtComponent.setText("");
	}

//	private boolean hasSubinventoryAuthority(String subInventory){
//		return TextUtils.isEmpty(subInventory)
//				|| mEnabledSubinventoies == null
//				|| mEnabledSubinventoies.size() == 0
//				|| mEnabledSubinventoies.contains(subInventory);
//	}

	private void getWorkOrder() {
	}

	private void submit() {

		mMsg = null;

		String checkResult = submitCheck();
		if(!TextUtils.isEmpty(checkResult)){
			mToastHelper.show(checkResult);
			return;
		}


		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"提交", "杂入交易单提交中...");
		mProgressDialog.show();

		final String org = mEdtOrg.getText().toString();
		final String order = mEdtOrder.getText().toString();
		final String orderLine = mEdtOrderLine.getText().toString();
		final String component = mEdtComponent.getText().toString();
		final String shipment = mEdtShipment.getText().toString();
		final String shipmentLine = mEdtShipmentLine.getText().toString();
		final String quantity = mEdtReceiveQuantity.getText().toString();
		final String uom = mEdtUom.getText().toString();
		final String subinventory = mEdtSubinventory.getText().toString();
		final String locator = subinventory + "." + mEdtLocator.getText().toString() + "." + mEdtProjectNo;
		final String projectno = mEdtProjectNo.getText().toString();
		final String lotno = mEdtLotNo.getText().toString();
		final String date = mTvTime.getText().toString();
		final String barcode = mEdtBarcode.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {

					String orgName = getOrganizationName(org);
					if (TextUtils.isEmpty(orgName)) {
						mMsg = " Can't find Operating Unit ";
						return;
					}

					//Submit Purchase Receive
					ISPPurchaseReceive receive = new ISPPurchaseReceive();
					receive.OperatingUnit = orgName;
					receive.Order = order;
					receive.OrderLine = orderLine;
					receive.Component = component;
					receive.Shipment = shipment;
					receive.ShipmentLine = shipmentLine;
					receive.Quantity = quantity;
					receive.Uom = uom;
					receive.Organization = org;
					receive.SubInventory = subinventory;
					receive.Locator = locator;
					receive.LotNo = lotno;
					receive.TransactionType = "RECEIVE";
					receive.TransactionDate = date;
					receive.ReasonCode = "";
					receive.Comments = "RECEIVE";
					receive.DeclareationNo = "";
					receive.Barcode = barcode;
					receive.UserName = "INFOSYS";
					if(!TextUtils.isEmpty(mMsg = submitPurchaseReceive(receive))){
						return;
					}

					//Get Wip Issue
					CUX_WIP_TRANSACTION_INT transInt = getISPPurchaseReceiveWipIssue(order, org, component);
					if(transInt == null) {
						return;
					}

					//Submit Wip Issue
					transInt.OrganizationCode = org;
					transInt.AssemblyLotNumber = "";
					transInt.TransactionType = "WIP Issue";
					transInt.TransactionDate = date;
					transInt.ComponentLotNumber = lotno;
					transInt.TransactionQuantity = new BigDecimal(quantity);
					transInt.ComponentSubinventory = subinventory;
					transInt.ComponentLocator = subinventory + "." + locator + "." + projectno;
					transInt.Reason = "Trx From Mobile";
					transInt.Reference = "IR_" + shipment  + "_" + shipmentLine;
					transInt.UpdateBy = "INFOSYS";
					if(!TextUtils.isEmpty(mMsg = submitWipIssue(transInt))){
						return;
					}

					//Get Operation Transfer
					ProcedureTransfer opera = getISPPurchaseReceiveOperationTransaction(order, org);
					if(opera == null) {
						return;
					}

					//Submit Operation Transfer
					opera.Organization = org;
					opera.TransactionType = "Move";
					opera.FromOperationSeqNum = "10";
					opera.FromIntraOperationStepMeaning = "排队";
					opera.ToIntraOperationStepMeaning = "移动";
					opera.TransactionQuantity = new BigDecimal(quantity);
					opera.CreatedByName = "INFOSYS";
					opera.Reason = "Trx From Mobile";
					opera.Reference = "IR_" + shipment  + "_" + shipmentLine;
					if(!TextUtils.isEmpty(mMsg = submitProcedure(opera))) {
						return;
					}

					//Get Wip Completion
					WipTransactionCompletion completion = getISPPurchaseReceiveWipCompletion(order, org, lotno);
					if(completion == null) {
						return;
					}

					//Submit Wip Completion
					completion.Organization = org;
					completion.TransactionType = "WIP Completion";
					completion.OverCompletion = "2";
					completion.Reason = "Trx From Mobile";
					completion.Reference = "IR_" + shipment  + "_" + shipmentLine;
					if(!TextUtils.isEmpty(mMsg = submitWipCompletion(completion))) {
						return;
					}

				} catch (Exception e) {
					mMsg = "异常:" + e.getMessage();
				}finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (mProgressDialog != null){
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

	private String submitPurchaseReceive(ISPPurchaseReceive receive) throws Exception{
		String result = mRequestManager.submitISPPurchaseReceive(receive);
		if(!RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
			return result;
		}
		return "";
	}

	private CUX_WIP_TRANSACTION_INT getISPPurchaseReceiveWipIssue(String purchase, String organization, String component) throws Exception{
		CUX_WIP_TRANSACTION_INT trans = null;
		try{
			trans = mRequestManager.getISPPurchaseReceiveWipIssueData(purchase, organization, component);
		}catch (Exception ex){
			String exMsg = ex.getMessage();
			if(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(ex.getMessage())){
				exMsg = "Lookup WIP Issue data  找不到WIP数据或者现有量不足";
			}
			throw new Exception(exMsg);
		}
		return trans;
	}

	private String submitWipIssue(CUX_WIP_TRANSACTION_INT transInt) throws Exception{
		List<CUX_WIP_TRANSACTION_INT> list = new ArrayList<CUX_WIP_TRANSACTION_INT>();
		list.add(transInt);
		String result = mRequestManager.submitTransactionInt(list);
		if(!RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
			return result;
		}
		return "";
	}

	private ProcedureTransfer getISPPurchaseReceiveOperationTransaction(String purchase, String organization) throws Exception{
		ProcedureTransfer trans = null;
		try{
			trans = mRequestManager.getISPPurchaseReceiveOperationTransaction(purchase, organization);
		}catch (Exception ex){
			String exMsg = ex.getMessage();
			if(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(ex.getMessage())){
				exMsg = "Lookup Move data  找不到工序移动的数据";
			}
			throw new Exception(exMsg);
		}
		return trans;
	}

	private String submitProcedure(ProcedureTransfer trans) throws Exception{
		String result = mRequestManager.submitProcedure(trans);
		if(!RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
			return result;
		}
		return "";
	}

	private WipTransactionCompletion getISPPurchaseReceiveWipCompletion(String purchase, String organization, String lotno) throws Exception{
		WipTransactionCompletion trans = null;
		try{
			trans = mRequestManager.GetISPPurchaseReceiveWipCompletion(purchase, organization, lotno);
		}catch (Exception ex){
			String exMsg = ex.getMessage();
			if(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(ex.getMessage())){
				exMsg = "Lookup Move data 找不到工序移动的数据";
			}
			throw new Exception(exMsg);
		}
		return trans;
	}

	private String submitWipCompletion(WipTransactionCompletion completion) throws Exception{
		String result = mRequestManager.submitTransactionComp(completion);
		if(!RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
			return result;
		}
		return "";
	}

	private String getOrganizationName(String org) throws Exception{
		return mRequestManager.getISPPurchaseReceiveOrganizationName(org);
	}

	private String submitCheck(){

		String order = mEdtOrder.getText().toString();
		if(TextUtils.isEmpty(order)){
			return "【采购订单】不得为空!";
		}

		String orderline = mEdtOrderLine.getText().toString();
		if(TextUtils.isEmpty(orderline)){
			return "【订单行号】不得为空!";
		}

		String org = mEdtOrg.getText().toString();
		if(TextUtils.isEmpty(org)){
			return "【库存组织】不得为空!";
		}

		String subinventory = mEdtSubinventory.getText().toString();
		if(TextUtils.isEmpty(subinventory)){
			return "【子库编号】不得为空!";
		}

		String locator = mEdtLocator.getText().toString();
		if(TextUtils.isEmpty(locator)){
			return "【货位】不得为空!";
		}

		String component = mEdtComponent.getText().toString();
		if(TextUtils.isEmpty(component)){
			return "【料件编号】不得为空!";
		}

		String shipment = mEdtShipment.getText().toString();
		if(TextUtils.isEmpty(shipment)){
			return "【发运号】不得为空!";
		}

		String shipmentLine = mEdtShipmentLine.getText().toString();
		if(TextUtils.isEmpty(shipmentLine)){
			return "【发运行号】不得为空!";
		}

		String uom = mEdtUom.getText().toString();
		if(TextUtils.isEmpty(uom)){
			return "【单位】不得为空!";
		}

		String lotno = mEdtLotNo.getText().toString();
		if(TextUtils.isEmpty(lotno)){
			return "【批次号】不得为空!";
		}

		String projectno = mEdtProjectNo.getText().toString();
		if(TextUtils.isEmpty(projectno)){
			return "【批次号】不得为空!";
		}

		String quantity = mEdtReceiveQuantity.getText().toString();
		if(TextUtils.isEmpty(quantity)){
			return "【收料数量】不得为空!";
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
			case R.id.imb_intraware_purchase_receive_order_query:
				selectOrder();
				break;
			case R.id.imb_intraware_purchase_receive_locator_query:
				selectLocator();
				break;
			case R.id.imb_intraware_purchase_receive_subinventory_query:
				selectSubInventory();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode){
			case RequestCode.ISP_PURCHASE_RECEIVE_SUBINVENTORY:
				if(data != null){
					mEdtSubinventory.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_SUBINVENTORY));
				}
				break;
			case RequestCode.ISP_PURCHASE_RECEIVE_LOCATOR:
				if(data != null){
					mEdtLocator.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_LOCATOR));
				}
				break;
			case RequestCode.ISP_PURCHASE_RECEIVE_ORDER:
				if(data != null){
					mEdtOrder.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER));
					mEdtOrderLine.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_LINE));
					mEdtShipment.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_SHIPMENT));
					mEdtShipmentLine.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_SHIPMENT_LINE));
					mEdtComponent.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_COMPONENT));
					mEdtComponentDescription.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_COMPONENT_DESCRIPTION));
					mEdtUom.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_UOM));
					mEdtReceivedQuantity.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_UNRECEIVED_QUANTITY));
					mEdtReceiveQuantity.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_RECEIVED_QUANTITY));
					mEdtPurchaseQuantity.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_PURCHASE_QUANTITY));
					mEdtProjectNo.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_PROJECT_NO));
					mEdtLotNo.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_LOTNO));
					mEdtBarcode.setText(data.getStringExtra(ResultKey.KEY_PURCHASE_RECEIVE_ORDER_SHIPMENT));
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectOrder(){
		Intent intent = new Intent(mInstance, ISPPurchaseReceiveOrderSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_PURCHASE_RECEIVE_ORDER);
	}

	private void selectLocator(){
		Intent intent = new Intent(mInstance, ISPPurchaseReceiveLocatorSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_SUBINVENTORY, mEdtSubinventory.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_PURCHASE_RECEIVE_LOCATOR);
	}

	private void selectSubInventory(){
		Intent intent = new Intent(mInstance, ISPPurchaseReceiveSubInventorySelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_PURCHASE_RECEIVE_SUBINVENTORY);
	}

	private PagerAdapter mPageAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViewList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			container.addView(mViewList.get(position));

			return mViewList.get(position);
		}
	};
}
