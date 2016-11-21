package com.hcp.intraware.biz.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import com.hcp.entities.LotQuantityInWarehouse;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.http.RequestUtil;
import com.hcp.intraware.biz.select.activity.ISPBillSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPLocatorSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPReasonSelectActivity;
import com.hcp.intraware.biz.select.activity.ISPSubInventorySelectActivity;
import com.hcp.intraware.constants.RequestCode;
import com.hcp.intraware.constants.RequestKey;
import com.hcp.intraware.constants.ResultKey;
import com.hcp.intraware.entity.ISPAccountAliasReceipt;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.util.AppConfig;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ISPAccountAliasIssueActivity extends ScannerActivity implements
		TextWatcher, OnClickListener {

	private EditText mEdtOrg;
	private EditText mEdtSubinventory;
	private EditText mEdtComponent;
	private EditText mEdtLocator;
	private EditText mEdtProjectNo;
	private EditText mEdtLotNo;
	private EditText mEdtUom;
	private EditText mEdtQuantity;
	private EditText mEdtAddress;
	private EditText mEdtBill;
	private EditText mEdtReason;

	private TextView mTvType;
	private TextView mTvOrigin;
	private TextView mTvOriginType;
	private TextView mTvTime;

	private ImageButton mImbQuerySubinventory;
	private ImageButton mImbQueryLocator;
	private ImageButton mImbQueryBill;
	private ImageButton mImbQueryReason;
	private ViewPager mVpContainer;
	private RadioGroup mRgSegment;

	private Button mBtnSubmit;

	private Resources mResources;

	private List<LotQuantityInWarehouse> mCurrentLots;

	private List<View> mViewList = new ArrayList<View>();

	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;
	private String mMsg;

	private WORequestManager mRequestManager;
	private AppConfig mAppConfig;

	private boolean mOnBatch = false;
	private int mBatchID = -1;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intraware_account_alias_issue);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		initView();
	}

	private void initView(){
		mAppConfig = AppConfig.getInstance(mInstance);
		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);

		View viewTrans = View.inflate(mInstance, R.layout.fragment_iaai_trans, null);
		mEdtLocator = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_locator);
		mEdtLocator.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtProjectNo = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_project_no);
		mEdtProjectNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLotNo = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_batch);
		mEdtLotNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtUom = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_uom);
		mEdtUom.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtQuantity = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_quantity);
		mEdtQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtAddress = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_address);

		mEdtBill = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_ori_bill);
		mEdtBill.setOnTouchListener(mHideKeyBoardTouchEvent);

		mImbQueryLocator = (ImageButton) viewTrans.findViewById(R.id.imb_intraware_aai_locator_query);
		mImbQueryLocator.setOnClickListener(this);

		mImbQueryBill = (ImageButton) viewTrans.findViewById(R.id.imb_intraware_aai_bill_query);
		mImbQueryBill.setOnClickListener(this);

		mImbQuerySubinventory = (ImageButton) viewTrans.findViewById(R.id.imb_intraware_aai_sub_inventory_query);
		mImbQuerySubinventory.setOnClickListener(this);

		mImbQueryReason = (ImageButton) viewTrans.findViewById(R.id.imb_intraware_aai_reason_query);
		mImbQueryReason.setOnClickListener(this);

		mEdtComponent = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_component);
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

		mEdtOrg = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_org);
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

		mEdtComponent = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_component);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubinventory = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_sub_inventory);
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

		mEdtReason = (EditText) viewTrans.findViewById(R.id.edt_intraware_aai_reason);

		View viewOther = View.inflate(mInstance, R.layout.fragment_iaai_other, null);
		mTvOrigin = (TextView) viewOther.findViewById(R.id.tv_intraware_aai_trans_ori);
		mTvOrigin.setText("Inside Sourcing Materials Return.Default");

		mTvOriginType = (TextView) viewOther.findViewById(R.id.tv_intraware_aai_ori_type);
		mTvOriginType.setText("Account alias");

		mTvType = (TextView) viewOther.findViewById(R.id.tv_intraware_aai_trans_type);
		mTvType.setText("Account alias issue");

		mTvTime = (TextView) viewOther.findViewById(R.id.tv_intraware_aai_date);
		mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

		mVpContainer = (ViewPager) findViewById(R.id.vp_intraware_aai_container);
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

		mBtnSubmit = (Button) findViewById(R.id.btn_intraware_aai_submit);
		mBtnSubmit.setOnClickListener(this);

		mRgSegment = (RadioGroup) findViewById(R.id.rg_intraware_aai_segment);
		mRgSegment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int index = -1;
				for(int i = 0; i < mRgSegment.getChildCount(); i++){
					if(mRgSegment.getChildAt(i).getId() ==  checkedId){
						index = i;
					}
				}

				if(index > -1){
					mVpContainer.setCurrentItem(index);
				}
			}
		});

		mViewList.add(viewTrans);
		mViewList.add(viewOther);
		mVpContainer.setAdapter(mPageAdapter);

		mRgSegment.check(mRgSegment.getChildAt(0).getId());
	}

	private void clearValues(boolean includeOrgCode){

		if(includeOrgCode){
			mEdtOrg.setText("");
		}

		mEdtBill.setText("");
		mEdtSubinventory.setText("");
		mEdtLocator.setText("");
		mEdtProjectNo.setText("");
		mEdtLotNo.setText("");
		mEdtUom.setText("");
		mEdtQuantity.setText("");
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

		final String orgCode = mEdtOrg.getText().toString();
		if(TextUtils.isEmpty(orgCode)){
			mToastHelper.show("【组织】不得为空!");
			return;
		}

		final String uom = mEdtUom.getText().toString();
		if(TextUtils.isEmpty(uom)){
			mToastHelper.show("【单位】不得为空!");
			return;
		}

		final String component = mEdtComponent.getText().toString();
		if(TextUtils.isEmpty(component)){
			mToastHelper.show("【料件编号】不得为空!");
			return;
		}

		final String subInventory = mEdtSubinventory.getText().toString();
		if(TextUtils.isEmpty(subInventory)){
			mToastHelper.show("【子库存】不得为空!");
			return;
		}

		final String locator = mEdtLocator.getText().toString();
		if(TextUtils.isEmpty(locator)){
			mToastHelper.show("【货位】不得为空!");
			return;
		}

		final String quantityStr = mEdtQuantity.getText().toString();
		if(TextUtils.isEmpty(quantityStr)){
			mToastHelper.show("【数量】不得为空!");
			return;
		}

		final String bill = mEdtBill.getText().toString();
		if(TextUtils.isEmpty(bill)){
			mToastHelper.show("【单据号】不得为空!");
			return;
		}

		final ISPAccountAliasReceipt receipt = new ISPAccountAliasReceipt();
		receipt.OrganizationCode = orgCode;
		receipt.TransactionDate = mTvTime.getText().toString();
		receipt.TransactionType = mTvType.getText().toString();
		receipt.TransactionSourceType = mTvOriginType.getText().toString();
		receipt.TransactionSource = mTvOrigin.getText().toString();
		receipt.ItemCode = mEdtComponent.getText().toString();
		receipt.SubInventoryCode = mEdtSubinventory.getText().toString();
		receipt.Locator = mEdtSubinventory.getText().toString() + "." + mEdtLocator.getText().toString() + "." + mEdtProjectNo.getText().toString();
		receipt.LotNumber = mEdtLotNo.getText().toString();
		receipt.TransactionUom = mEdtUom.getText().toString();
		receipt.Quantity = new BigDecimal(mEdtQuantity.getText().toString());
		receipt.Location = mEdtAddress.getText().toString();
		receipt.ReasonName = mEdtReason.getText().toString();
		receipt.Attribute1 = "";
		receipt.Attribute10 = "";
		receipt.TransactionReference = mEdtBill.getText().toString();

		receipt.UserName = "INFOSYS";

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "杂入交易单提交中...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				String result = null;
				try {
					result = mRequestManager.submitISPAccountAliasReceipt(receipt);
					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
						mMsg = "提交成功!";
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								clearValues(false);

								mEdtReason.setText("");
							}
						});
					}else{
						mMsg = result;
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
			case R.id.imb_intraware_aai_bill_query:
				selectBill();
				break;
			case R.id.imb_intraware_aai_locator_query:
				selectLocator();
				break;
			case R.id.imb_intraware_aai_sub_inventory_query:
				selectSubInventory();
				break;
			case R.id.btn_intraware_aai_submit:
				submit();
				break;
			case R.id.imb_intraware_aai_reason_query:
				selectReason();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode){
			case RequestCode.ISP_SUBINVENTORY:
				if(data != null){
					mEdtSubinventory.setText(data.getStringExtra(ResultKey.KEY_SUBINVENTORY_NAME));
				}
				break;
			case RequestCode.ISP_LOCATOR:
				if(data != null){
					mEdtLocator.setText(data.getStringExtra(ResultKey.KEY_LOCATOR_NAME));
				}
				break;
			case RequestCode.ISP_BILL:
				if(data != null){
					mEdtComponent.setText(data.getStringExtra(ResultKey.KEY_BILL_COMPONENT));
					mEdtLotNo.setText(data.getStringExtra(ResultKey.KEY_BILL_BATCH));
					mEdtProjectNo.setText(data.getStringExtra(ResultKey.KEY_BILL_PROJECT_NO));
					mEdtQuantity.setText(data.getStringExtra(ResultKey.KEY_BILL_TRANSACTION_QUANTITY));
					mEdtBill.setText(data.getStringExtra(ResultKey.KEY_BILL_NAME));
					mEdtUom.setText(data.getStringExtra(ResultKey.KEY_BILL_UOM));
				}
				break;
			case RequestCode.ISP_REASON:
				if(data != null){
					mEdtReason.setText(data.getStringExtra(ResultKey.KEY_REASON_NAME));
				}
				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectSubInventory(){
		Intent intent = new Intent(mInstance, ISPSubInventorySelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_SUBINVENTORY);
	}

	private void selectReason(){
		Intent intent = new Intent(mInstance, ISPReasonSelectActivity.class);
		startActivityForResult(intent, RequestCode.ISP_REASON);
	}

	private void selectLocator(){
		Intent intent = new Intent(mInstance, ISPLocatorSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		intent.putExtra(RequestKey.ISP_SUBINVENTORY, mEdtSubinventory.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_LOCATOR);
	}

	private void selectBill(){
		Intent intent = new Intent(mInstance, ISPBillSelectActivity.class);
		intent.putExtra(RequestKey.ISP_ORGANIZATION, mEdtOrg.getText().toString());
		startActivityForResult(intent, RequestCode.ISP_BILL);
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
