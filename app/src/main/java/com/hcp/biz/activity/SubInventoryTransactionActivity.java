package com.hcp.biz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.SegmentRemaining;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.SubInventoryTransaction;
import com.hcp.http.RequestUtil;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
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

public class SubInventoryTransactionActivity extends ScannerActivity implements TextWatcher {
	private final int DO_REQUEST_SUBMIT = 0;
	private final int DO_REQUEST_GET_SEGMENT = 1;

	private EditText mEdtSegmentCode;
	private EditText mEdtSubInventoryFrom;
	private EditText mEdtSubInventoryTo;
	private EditText mEdtTransactionQuantity;
	private EditText mEdtLocatorFrom;
	private EditText mEdtLocatorTo;
	private EditText mEdtBarcode;

	private TextView mTvRemaining;
	private TextView mTvSegmentName;
	private TextView mTvProjectNumberFrom;
	private TextView mTvProjectNumberTo;
	private TextView mTvTitle;

	private LinearLayout mLlBarcodeContainer;

	private Button mBtnSubmit;

	private WORequestManager mRequestManager;

	private ProgressDialog mProgressDialog;

	private List<SegmentRemaining> mCurrentSegments;

	private ListView mLvProjectNumberList;

	private ToastHelper mToastHelper;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private Map<String, List<SegmentRemaining>> mGroupSegmentBinding = new HashMap<String, List<SegmentRemaining>>();
	private List<String> mProjectList = new ArrayList<String>();
	private String mCurrentProjectNumber = null;

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_inventory_transaction);

		mSADao = SubInventoryAuthorityDao.getInstance(mInstance);
		try {
			SubInventoryAuthority subInventoy = mSADao.getAuthority(AppConfig.getInstance(mInstance).getLastLoginUser());
			if(subInventoy != null && !TextUtils.isEmpty(subInventoy.subinventories)){
				mEnabledSubinventoies = Arrays.asList(subInventoy.subinventories.split(SubInventoryAuthorityDao.SEPARATOR));
			}
		} catch (Exception e) {

		}

		mEdtSegmentCode = (EditText) findViewById(R.id.edt_sbt_segment_code);
		mEdtSegmentCode.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtSegmentCode.addTextChangedListener(this);

		mTvProjectNumberFrom = (TextView) findViewById(R.id.tv_sbt_project_number_from);

		mTvProjectNumberFrom.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showProjectNumberList();
			}
		});

		mTvProjectNumberTo = (TextView) findViewById(R.id.tv_sbt_project_number_to);
		mTvProjectNumberTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubInventoryFrom = (EditText) findViewById(R.id.edt_sbt_sub_inventroy_from);
		mEdtSubInventoryFrom.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtSubInventoryFrom.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !hasSubinventoryAuthority(mEdtSubInventoryFrom.getText().toString())) {
					new AlertDialog.Builder(mInstance)
							.setMessage("该子库未在授权子库中!")
							.setPositiveButton("确定", null)
							.create()
							.show();

					mEdtSubInventoryFrom.setText(StringUtils.EMPTY);
				}
			}
		});
		mEdtSubInventoryFrom.addTextChangedListener(this);

		mEdtBarcode = (EditText) findViewById(R.id.edt_sbt_segment_barcode);
		mEdtBarcode.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubInventoryTo = (EditText) findViewById(R.id.edt_sbt_sub_inventroy_to);
		mEdtSubInventoryTo.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !hasSubinventoryAuthority(mEdtSubInventoryTo.getText().toString())) {
					new AlertDialog.Builder(mInstance)
							.setMessage("该子库未在授权子库中!")
							.setPositiveButton("确定", null)
							.create()
							.show();

					mEdtSubInventoryTo.setText(StringUtils.EMPTY);
				}
			}
		});


		mEdtSubInventoryTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtTransactionQuantity = (EditText) findViewById(R.id.edt_sbt_transaction_quantity);
		mEdtTransactionQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocatorFrom = (EditText) findViewById(R.id.edt_sbt_locator_from);
		mEdtLocatorFrom.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtLocatorFrom.addTextChangedListener(this);
		mEdtLocatorFrom.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getSegment();
				}
			}
		});

		mEdtLocatorTo = (EditText) findViewById(R.id.edt_sbt_locator_to);
		mEdtLocatorTo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvRemaining = (TextView) findViewById(R.id.tv_sbt_remaining);
		mTvSegmentName = (TextView) findViewById(R.id.tv_sbt_segment_name);

		mBtnSubmit = (Button) findViewById(R.id.btn_sbt_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mLlBarcodeContainer = (LinearLayout) findViewById(R.id.ll_sbt_barcode_container);
		mTvTitle = (TextView) findViewById(R.id.tv_sbt_title);

		mRequestManager = WORequestManager.getInstance(this);
		mToastHelper = ToastHelper.getInstance(this);

		mEdtLocatorFrom.setText("00");
		mEdtLocatorTo.setText("00");

		if(!getIntent().hasExtra("type")){
			mLlBarcodeContainer.setVisibility(View.GONE);
			mEdtSegmentCode.requestFocus();
			mTvTitle.setText(R.string.sbt_title);
		}else{
			mTvTitle.setText(R.string.sbt_barcode_title);
		}
	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private void submit() {

		if (mCurrentSegments == null) {
			mToastHelper.show("请先获取现有量信息!");
			return;
		}

		String completionQtyStr = mEdtTransactionQuantity.getText().toString();
		if (TextUtils.isEmpty(completionQtyStr)) {
			mToastHelper.show("请输入【移动数量】!");
			return;
		}

		final String subInventoryFrom = mEdtSubInventoryFrom.getText().toString();
		if (TextUtils.isEmpty(subInventoryFrom)) {
			mToastHelper.show("请输入【原始子库】!");
			return;
		}

		final String subInventoryTo = mEdtSubInventoryTo.getText().toString();
		if (TextUtils.isEmpty(subInventoryTo)) {
			mToastHelper.show("请输入【目标子库】!");
			return;
		}

		final String locatorFrom = StringUtils.trimToEmpty(mEdtLocatorFrom.getText().toString());
		final String locatorTo = StringUtils.trimToEmpty(mEdtLocatorTo.getText().toString());

		final BigDecimal remainingQty = new BigDecimal(mTvRemaining.getText().toString());
		final BigDecimal transactionQty = new BigDecimal(mEdtTransactionQuantity.getText().toString());

		if (remainingQty.compareTo(transactionQty) < 0) {
			mToastHelper.show("【移动数量】不得高于【现有量】!");
			return;
		}
		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"提交", "子库转移提交中...");
		mProgressDialog.show();

		final String projectNumberFrom = StringUtils.trimToEmpty(mTvProjectNumberFrom.getText().toString());
		final String projectNumberTo = StringUtils.trimToEmpty(mTvProjectNumberTo.getText().toString());

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(!mRequestManager.isSameTypeSubInventory(subInventoryFrom, subInventoryTo)){
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mProgressDialog.cancel();

								mToastHelper.show("资产仓与费用仓不能互转!");
							}
						});
						return;
					}

				} catch (Exception e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mProgressDialog.cancel();

							mToastHelper.show("子库验证失败!!");
						}
					});
					return;
				}
				BigDecimal tempTransactionQty = transactionQty;
				final List<SubInventoryTransaction> subTrans = new ArrayList<SubInventoryTransaction>();

				List<SegmentRemaining> segmentRemainings = mGroupSegmentBinding.get(mCurrentProjectNumber);
				final Map<String, BigDecimal> lotBindQty = new HashMap<String, BigDecimal>();
				for(SegmentRemaining item : segmentRemainings){

					if(tempTransactionQty.compareTo(BigDecimal.ZERO) <= 0){
						break;
					}

					if(item.Remaining != null && item.Remaining.compareTo(BigDecimal.ZERO) <= 0){
						continue;
					}

					SubInventoryTransaction subTran = new SubInventoryTransaction();
					subTran.CreatedByName = AppCache.getInstance().getLoginUser();
					subTran.DocumentNumber = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
					subTran.InventoryItemNumber = mEdtSegmentCode.getText().toString();
					subTran.Locator = subInventoryFrom + "." + locatorFrom + "." + projectNumberFrom;
					subTran.LotNumber = item.LotNumber;
					subTran.OrganizationCode = item.OrganizationCode;
					subTran.ProjectNumber = projectNumberFrom;
					subTran.ReasonName = "Trx From Mobile";
					subTran.SubInventoryCode = subInventoryFrom;
					subTran.TransactionQuantity = tempTransactionQty.compareTo(item.Remaining)>0 ? item.Remaining : tempTransactionQty;
					tempTransactionQty = tempTransactionQty.subtract(item.Remaining);
					lotBindQty.put(item.LotNumber, subTran.TransactionQuantity);
					subTran.TransactionReference = "Trx From Mobile : " + Device.getIMEI(mInstance);
					subTran.TransactionType = "Subinventory Transfer";
					subTran.TransferLocator = subInventoryTo + "." + locatorTo + "." + projectNumberTo;
					subTran.TransferSubInventory = subInventoryTo;
					subTran.WipEntityName = "";

					subTrans.add(subTran);
				}


				final String segment = mEdtSegmentCode.getText().toString();
				final String successMsg = getSuccessMsg(lotBindQty);
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_SUBMIT;
				try {
					String result = mRequestManager.submitSubInventoryTransaction(subTrans);

					msg.obj = result;

					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){
						Bundle bundle = new Bundle();
						bundle.putString("msg", successMsg);
						msg.setData(bundle);
						PrintManager manager = PrintManager.getInstance(mInstance);
						manager.connect();
						manager.print(successMsg.getBytes("GB2312"));
						manager.printOneDimenBarcode(segment);
						manager.print("\n\n\n\n".getBytes());
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
				case DO_REQUEST_GET_SEGMENT:
					if (msg.obj != null) {

						mCurrentSegments =  (List<SegmentRemaining>) msg.obj;

						mTvSegmentName.setText(mCurrentSegments.get(0).Description);

						BigDecimal remaining = BigDecimal.ZERO;
						for(SegmentRemaining item : mCurrentSegments){
							remaining = remaining.add(item.Remaining);
						}
						mTvRemaining.setText(remaining + "");

						groupSegments();

						showProjectNumberList();

					} else {
						mToastHelper.show("现有量获取失败!");
					}
					break;
				case DO_REQUEST_SUBMIT:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(msg.getData().getString("msg"));

							clearValues();

							clearInputValues();

							mEdtSegmentCode.requestFocus();

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

	private void groupSegments(){

		mGroupSegmentBinding.clear();
		for(SegmentRemaining item : mCurrentSegments){
			String projectNumber = StringUtils.trimToEmpty(item.ProjectNumber);
			if(!mGroupSegmentBinding.containsKey(projectNumber)){
				List<SegmentRemaining> list = new ArrayList<SegmentRemaining>();

				list.add(item);

				mGroupSegmentBinding.put(projectNumber, list);
			}else{
				mGroupSegmentBinding.get(projectNumber).add(item);
			}
		}

		mProjectList = Arrays.asList(mGroupSegmentBinding.keySet().toArray(new String[mGroupSegmentBinding.keySet().size()]));
	}

	private AlertDialog mDlPNumberList;
	private void showProjectNumberList(){

		if(mProjectList == null || mProjectList.size() == 0){
			return;
		}

		if(mProjectList.size() == 1){

			mCurrentProjectNumber = mProjectList.get(0);

			setSegmentInfo(mGroupSegmentBinding.get(mCurrentProjectNumber));

			return;
		}

		mLvProjectNumberList = new ListView(mInstance);
		mLvProjectNumberList.setAdapter(new ProjectNumberAdapter());
		mLvProjectNumberList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

				mCurrentProjectNumber = mProjectList.get(position);

				setSegmentInfo(mGroupSegmentBinding.get(mCurrentProjectNumber));

				mDlPNumberList.cancel();
			}

		});

		mDlPNumberList = new AlertDialog.Builder(mInstance)
				.setTitle("项目列表")
				.setView(mLvProjectNumberList)
				.create();

		mDlPNumberList.show();
	}

	private void setSegmentInfo(List<SegmentRemaining> srs){

		mTvRemaining.setText(getTotalRemaining(srs) + "");

		String projectNumber = mCurrentProjectNumber;
		mTvProjectNumberFrom.setText(projectNumber);
		mTvProjectNumberTo.setText(projectNumber);
	}

	private BigDecimal getTotalRemaining(List<SegmentRemaining> srs){

		BigDecimal remaining = BigDecimal.ZERO;

		for(SegmentRemaining item : srs){
			remaining = remaining.add(item.Remaining);
		}

		return remaining;
	}


	private void clearInputValues(){

		mEdtSubInventoryTo.setText("");
		mEdtSubInventoryFrom.setText("");
		mEdtTransactionQuantity.setText("");
		mEdtSegmentCode.setText("");

		if(!"00".equals(mEdtLocatorFrom.getText().toString())){
			mEdtLocatorFrom.setText("");
		}

		if(!"00".equals(mEdtLocatorTo.getText().toString())){
			mEdtLocatorTo.setText("");
		}
	}

	private void clearValues() {
		mTvSegmentName.setText("");
		mTvRemaining.setText("");

		mTvProjectNumberFrom.setText("");
		mTvProjectNumberTo.setText("");

		mCurrentSegments = null;
	}

	private String getSuccessMsg(Map<String, BigDecimal> lotBindQty){
		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("子库转移\n");
		stbMsg.append("--------------------------\n");
		stbMsg.append("物料编号:" + mEdtSegmentCode.getText().toString() + "\n");
		stbMsg.append("物料名称:" + mTvSegmentName.getText().toString() + "\n");
		stbMsg.append("原始子库:" + mEdtSubInventoryFrom.getText().toString() + "\n");
		stbMsg.append("原始货位:" + mEdtLocatorFrom.getText().toString() + "\n");
		stbMsg.append("项目号:" + mTvProjectNumberFrom.getText().toString() + "\n");
		stbMsg.append("现有量:" + mTvRemaining.getText().toString() + "\n");
		stbMsg.append("目标子库:" + mEdtSubInventoryTo.getText().toString() + "\n");
		stbMsg.append("目标库位:" + mEdtLocatorTo.getText().toString() + "\n");
		stbMsg.append("项目号:" + mTvProjectNumberTo.getText().toString() + "\n");
		stbMsg.append("移动数量:" + mEdtTransactionQuantity.getText().toString() + "\n");
		stbMsg.append("------------\n");
		for(Map.Entry<String, BigDecimal> item : lotBindQty.entrySet()){
			stbMsg.append( mResources.getString(R.string.mi_issued_lot) + item.getKey() + " " +  mResources.getString(R.string.mi_issued_lot_qty) + item.getValue() + "\n");
		}

		return stbMsg.toString();
	}

	private void showTransSuccessMsg(String msg) {

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(msg).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	@Override
	protected void decodeCallback(final String barcode) {

		if(mDlPNumberList != null && mDlPNumberList.isShowing()){
			return;
		}
		if(mEdtBarcode.hasFocus()){

			mEdtBarcode.setText(barcode);

			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(mInstance, "条码解析", "条码解析中...");
			mProgressDialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					String temp = null;
					try {
						temp = mRequestManager.getItemNoByBarcode(barcode);
					} catch (Exception e) {
						e.printStackTrace();
					}

					final String itemNo = temp;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							mProgressDialog.cancel();

							if(!TextUtils.isEmpty(itemNo)){
								mEdtSegmentCode.setText(itemNo);
								mEdtSubInventoryFrom.requestFocus();
							}else{
								mToastHelper.show("条码解析失败!");
							}
						}
					});
				}
			}).start();

		}
		else if (mEdtSegmentCode.hasFocus()) {
			mEdtSegmentCode.setText(barcode);
			mEdtSubInventoryFrom.requestFocus();
		}else if (mEdtSubInventoryFrom.hasFocus()) {
			mEdtSubInventoryFrom.setText(barcode);
			mEdtLocatorFrom.requestFocus();
		}else if(mEdtLocatorFrom.hasFocus()){
			mEdtLocatorFrom.setText(barcode);
			mEdtSubInventoryTo.requestFocus();
			getSegment();
		}else if(mEdtSubInventoryTo.hasFocus()){
			mEdtSubInventoryTo.setText(barcode);
			mEdtLocatorTo.requestFocus();
		}else if(mEdtLocatorTo.hasFocus()){
			mEdtLocatorTo.setText(barcode);
			mEdtTransactionQuantity.requestFocus();
		}
	}

	private void getSegment() {
		final String segmentCode = mEdtSegmentCode.getText().toString();
		final String subInventory = mEdtSubInventoryFrom.getText().toString();
		//final String projectNumber = mEdtProjectNumberFrom.getText().toString();
		final String locatorFrom = mEdtLocatorFrom.getText().toString();

		if (TextUtils.isEmpty(segmentCode)) {
			mToastHelper.show("【物料编号】不得为空!");
			return;
		}

		if (TextUtils.isEmpty(subInventory)) {
			mToastHelper.show("【原始子库】不得为空!");
			return;
		}

		if (TextUtils.isEmpty(locatorFrom)) {
			mToastHelper.show("【原始货位】不得为空!");
			return;
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"现有量", "正在获取现有量...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				Message msg = Message.obtain();
				msg.what = DO_REQUEST_GET_SEGMENT;
				try {
					List<SegmentRemaining> segments  = mRequestManager
							.getSegmentRemaing(segmentCode, subInventory, locatorFrom, "");
					msg.obj = segments;
				} catch (Exception e) {
					e.printStackTrace();
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

	private class ProjectNumberAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mProjectList.size();
		}

		@Override
		public Object getItem(int position) {
			return mProjectList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.project_list_item, null);

				holder = new ViewHolder();
				holder.projectNumber = (TextView) convertView.findViewById(R.id.tv_pli_project_number);
				holder.remaing = (TextView) convertView.findViewById(R.id.tv_pli_remaining);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.projectNumber.setText(mProjectList.get(position));
			holder.remaing.setText(getTotalRemaining(mGroupSegmentBinding.get(mProjectList.get(position))) + "");

			return convertView;
		}

		private class ViewHolder{
			TextView projectNumber;
			TextView remaing;
		}

	}
}
