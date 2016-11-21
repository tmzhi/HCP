package com.hcp.biz.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.ListView;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.LotQuantityInWarehouse;
import com.hcp.entities.SubInventoryAuthority;
import com.hcp.entities.WorkOrder3;
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

public class SpecificIssueActivity extends ScannerActivity implements
		TextWatcher {

	private final int DO_REQUEST_ISSUE = 0;
	private final int DO_REQUEST_GET_DATA = 1;

	private EditText mEdtWipName;
	private EditText mEdtComponent;
	private EditText mEdtSubInventory;

	private TextView mTvClassCode;
	private TextView mTvStartTime;
	private TextView mTvCompletionTime;
	private TextView mTvComponentName;
	private TextView mTvRemainingQty;
	private TextView mTvTransactionQty;
	private TextView mTvLocator;

	private Button mBtnSubmit;

	private Resources mResources;

	private WorkOrder3 mCurrentWorkOrder;
	private List<LotQuantityInWarehouse> mCurrentLots;
	private ProgressDialog mProgressDialog;

	private WORequestManager mRequestManager;

	private ListView mLvLocatorList;

	private LocatorAdapter mLocatorAdapter;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private Map<String, TransactionLot> mBindRemainingByGroup = new HashMap<String, TransactionLot>();

	private AlertDialog mDlLocatorTrans = null;

	private SubInventoryAuthorityDao mSADao;

	private List<String> mEnabledSubinventoies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_specific_issue);

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

		mEdtComponent = (EditText) findViewById(R.id.edt_si_component_code);
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

		mEdtWipName = (EditText) findViewById(R.id.edt_si_wip_name);
		mEdtWipName.addTextChangedListener(this);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_si_sub_inventory);
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

		mTvTransactionQty = (TextView) findViewById(R.id.tv_si_transaction_qty);

		mTvClassCode = (TextView) findViewById(R.id.tv_si_class_code);
		mTvStartTime = (TextView) findViewById(R.id.tv_si_start_time);
		mTvCompletionTime = (TextView) findViewById(R.id.tv_si_completion_time);
		mTvComponentName = (TextView) findViewById(R.id.tv_si_component_name);
		mTvRemainingQty = (TextView) findViewById(R.id.tv_si_remaining_qty);

		mBtnSubmit = (Button) findViewById(R.id.btn_si_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		initLocatorView();
	}

	private boolean hasSubinventoryAuthority(String subInventory){
		return TextUtils.isEmpty(subInventory)
				|| mEnabledSubinventoies == null
				|| mEnabledSubinventoies.size() == 0
				|| mEnabledSubinventoies.contains(subInventory);
	}

	private void initLocatorView(){

		mTvLocator = (TextView) findViewById(R.id.tv_si_locator);
		mTvLocator.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLocatorList();
			}
		});
	}

	private void showLocatorList(){
		if(mCurrentLots != null && mCurrentLots.size() > 0){
			mLvLocatorList = new ListView(mInstance);
			mLvLocatorList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
					showLocatorTransQty(position);
				}
			});

			mLocators = Arrays.asList(mBindRemainingByGroup.keySet().toArray(new String[mBindRemainingByGroup.size()]));

			mLocatorAdapter = new LocatorAdapter();
			mLvLocatorList.setAdapter(mLocatorAdapter);

			mDlLocatorTrans = new AlertDialog.Builder(mInstance)
					.setTitle("货位")
					.setView( mLvLocatorList)
					.setPositiveButton("确定", null)
					.create();

			mDlLocatorTrans.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					BigDecimal totalTransQty = BigDecimal.ZERO;
					StringBuilder stb = new StringBuilder();
					for(TransactionLot lot : mBindRemainingByGroup.values()){
						totalTransQty = totalTransQty.add(lot.transactionQty);

						if(lot.transactionQty.compareTo(BigDecimal.ZERO)>0){
							stb.append(lot.locator);
							stb.append(" | ");
						}
					}
					mTvTransactionQty.setText(totalTransQty + "");
					mTvLocator.setText(stb.delete(stb.length() - 3, stb.length()));
				}
			});

			mDlLocatorTrans.show();
		}
	}

	private void showLocatorTransQty(int position){

		final TransactionLot lot = mBindRemainingByGroup.get(mLocators.get(position));

		final EditText editText = new EditText(mInstance);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editText.setSingleLine();
		editText.setText(lot.transactionQty + "");

		AlertDialog dialog = new AlertDialog.Builder(mInstance)
				.setView(editText)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String qtyStr = editText.getText().toString();
						if(TextUtils.isEmpty(qtyStr)){
							mToastHelper.show("请输入【交易数量】!");
							return;
						}

						BigDecimal transQty = new BigDecimal(qtyStr);
						if(transQty.compareTo(lot.remianingQty) > 0){
							mToastHelper.show("【交易数量】不得大于【现有量】!");
							return;
						}

						lot.transactionQty = new BigDecimal(qtyStr);
						mLocatorAdapter.notifyDataSetChanged();
					}
				})
				.create();

		dialog.show();
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

				WorkOrder3 workOrder = null;
				List<LotQuantityInWarehouse> lots = null;
				try {
					workOrder = mRequestManager
							.getWorkOrderOnCompletion(wipEntityName);
					if (workOrder != null) {
						lots = mRequestManager
								.getLotQuantityInWarehouses(
										workOrder.Organization,
										subInventory, componentCode, "", workOrder.ProjectNumber, true);
					}
				} catch (Exception e) {

				} finally {

					if(workOrder != null){
						result = new ArrayList<Object>();
						result.add(workOrder);
						result.add(lots);
						msg.obj = result;
					}
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void submit() {
		if (mCurrentWorkOrder == null) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String issuedQtyStr = mTvTransactionQty.getText().toString();
		if (TextUtils.isEmpty(issuedQtyStr)) {
			mToastHelper.show("请输入【发料数量】!");
			return;
		}
		BigDecimal issueQty = new BigDecimal(issuedQtyStr);

		String standingQtyStr = mTvRemainingQty.getText().toString();
		BigDecimal standingQty = new BigDecimal(standingQtyStr);

		if (issueQty.compareTo(standingQty) > 0) {
			mToastHelper.show("【发料数量】不得多于【现有数量】!");
			return;
		}
		long groupID = System.currentTimeMillis()
				+ new Random().nextInt(999999);
		final List<CUX_WIP_TRANSACTION_INT> transIssues = new ArrayList<CUX_WIP_TRANSACTION_INT>();

		for(TransactionLot transLot : mBindRemainingByGroup.values()){

			BigDecimal transQty = transLot.transactionQty;

			if(transQty.compareTo(BigDecimal.ZERO) > 0){
				for (LotQuantityInWarehouse lot : mCurrentLots) {
					if(!lot.LocatorSegment2.equals(transLot.locator)){
						continue;
					}

					if (transQty.compareTo(BigDecimal.ZERO) <= 0) {
						break;
					}

					if (lot.StandingCrop.compareTo(BigDecimal.ZERO) <= 0) {
						continue;
					}

					CUX_WIP_TRANSACTION_INT transIssue = mRequestManager
							.createDefaultTrans(WORequestManager.TRANS_ISSUE);
					transIssue.GroupID = groupID;
					transIssue.OrganizationCode = mCurrentWorkOrder.Organization;
					transIssue.WipEntityName = mCurrentWorkOrder.WipEntityName;
					transIssue.ProjectNumber = mCurrentWorkOrder.ProjectNumber;
					transIssue.JobType = mCurrentWorkOrder.ClassCode;
					transIssue.Assembly = mCurrentWorkOrder.ConcatenatedSegments;
					transIssue.AssemblyLotNumber = mCurrentWorkOrder.LotNumber;
					transIssue.AssemblyUomCode = mCurrentWorkOrder.PrimaryUomCode;
					transIssue.StartQuantity = mCurrentWorkOrder.StartQuantity;
					transIssue.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
					transIssue.ComponentItem = mEdtComponent.getText().toString();
					transIssue.ComponentUomCode = lot.PrimaryUOM;
					transIssue.ComponentLotNumber = lot.LotNumber;
					transIssue.RequiredQuantity = issueQty;
					transIssue.TransactionQuantity = transQty
							.compareTo(lot.StandingCrop) > 0 ? lot.StandingCrop
							: transQty;
					transQty = transQty.subtract(lot.StandingCrop);
					transIssue.ComponentSubinventory = lot.SubInventoryCode;
					transIssue.ComponentLocator = lot.SupplySegment;
					//transIssue.Department = workOrder.DepartmentCode;

					transIssues.add(transIssue);

					if(transQty.compareTo(BigDecimal.ZERO) < 0){
						break;
					}
				}
			}
		}


		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "领料单提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getSuccessMsg();
		final String wipName = mEdtWipName.getText().toString();
		final String component = mEdtComponent.getText().toString();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_ISSUE;
				try {
					String result = mRequestManager.submitTransactionInt(transIssues);

					msg.obj = result;

					if(RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)){

						PrintManager manager = PrintManager.getInstance(mInstance);
						manager.connect();
						manager.print(transMsg.getBytes("GB2312"));
						manager.printOneDimenBarcode(wipName);
						manager.printOneDimenBarcode(component);
						manager.print("\n------------------------\n\n\n\n".getBytes());
						manager.close();
					}
				} catch (Exception e) {
					msg.obj = e.getMessage();
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

						mCurrentWorkOrder = (WorkOrder3)results.get(0);
						mCurrentLots = (List<LotQuantityInWarehouse>) results
								.get(1);

						mTvClassCode.setText(mCurrentWorkOrder.ClassCode);
						mTvStartTime.setText(mCurrentWorkOrder.ScheduledStartDate);
						mTvCompletionTime.setText(mCurrentWorkOrder.ScheduledCompletionDate);

						BigDecimal remainingQty = BigDecimal.ZERO;
						if (mCurrentLots != null) {
							for (LotQuantityInWarehouse lot : mCurrentLots) {
								remainingQty = remainingQty.add(lot.StandingCrop);
							}
						}

						mTvComponentName.setText(mCurrentLots.get(0).ItemDescription);
						mTvRemainingQty.setText(remainingQty + "");

						bindingLocator(mCurrentLots);

						mTvLocator.setText(StringUtils.join(mBindRemainingByGroup.keySet().toArray(new String[mBindRemainingByGroup.keySet().size()]), ","));

						showLocatorList();

					} else {
						mToastHelper.show("工单及库存现有量获取失败!");
					}
					break;
				case DO_REQUEST_ISSUE:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg();

							clearValues();

							clearInputValues();

							mEdtSubInventory.requestFocus();
						} else {
							mToastHelper.show(result);
						}
					}
					break;
			}
			return false;
		}
	});

	private void bindingLocator(List<LotQuantityInWarehouse> list){
		for(LotQuantityInWarehouse item : list){
			String locator = item.LocatorSegment2;

			TransactionLot lot = null;

			if(!mBindRemainingByGroup.containsKey(locator)){
				lot = new TransactionLot();
				lot.locator = locator;
				mBindRemainingByGroup.put(locator, lot);
			}else{
				lot = mBindRemainingByGroup.get(locator);
			}

			lot.remianingQty = lot.remianingQty.add(item.StandingCrop);
		}
	}

	//private Issue saveIssue(){
	//Issue issue = new Issue();
//		issue.sub_inventory = mEdtSubInventory.getText().toString();
//		issue.wip_entity_name = mEdtWipName.getText().toString();
//		issue.class_code = mTvClassCode.getText().toString();
//		Date statTime = new Date(System.currentTimeMillis());
//		Date completionTime = new Date(System.currentTimeMillis());
//		try {
//			statTime = DateFormat.defaultParse(mTvStartDate.getText().toString());
//			completionTime = DateFormat.defaultParse(mTvCompleteDate.getText().toString());
//		} catch (ParseException e) {
//			mToastHelper.show(e.getMessage());
//		}
//		issue.start_time = statTime;
//		issue.complete_time = completionTime;
//		issue.component_code = mEdtComponent.getText().toString();
//		issue.component_name = mTvComponentName.getText().toString();
//		issue.wip_supply_meaning = mTvIssuedType.getText().toString();
//		issue.issued_quantity = new BigDecimal(mTvReceivedQty.getText().toString());
//		issue.quantity_open = new BigDecimal(mTvUnReceivedQty.getText().toString());
//		issue.remaining_quantity = new BigDecimal(mTvStandingCorp.getText().toString());
//		issue.transaction_quantity = new BigDecimal(mEdtIssuedQty.getText().toString());
//		issue.create_by = AppCache.getInstance().getLoginUser();
//		issue.create_time = new Date(System.currentTimeMillis());
//		try {
//			mBizDBHelper.insert(Issue.class, issue);
//		} catch (Exception e) {
//			mToastHelper.show(e.getMessage());
//		}

	//return issue;
	//}

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
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_class_code)
				+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_start_time)
				+ mTvStartTime.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_completion_time)
				+ mTvCompletionTime.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_component_code)
				+ mEdtComponent.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_component_name)
				+ mTvComponentName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_remaining_qty)
				+ mTvRemainingQty.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.si_transaction_qty)
				+ mTvTransactionQty.getText().toString());

		return stbMsg.toString();
	}

	private void clearInputValues(){
		mEdtComponent.setText("");
		mEdtSubInventory.setText("");
		mTvTransactionQty.setText("");
		mEdtWipName.setText("");
	}

	private void clearValues() {

		mTvClassCode.setText("");
		mTvCompletionTime.setText("");
		mTvStartTime.setText("");
		mTvComponentName.setText("");
		mTvRemainingQty.setText("");
	}

	@Override
	protected void decodeCallback(String barcode) {

		if(mDlLocatorTrans != null && mDlLocatorTrans.isShowing()){

			if(mBindRemainingByGroup.containsKey(barcode)){
				showLocatorTransQty(mLocators.indexOf(barcode));
			}else{
				mToastHelper.show("当前不存在该货位!");
			}

			return;
		}

		if (mEdtSubInventory.hasFocus()) {
			mEdtSubInventory.setText(barcode);
			mEdtWipName.requestFocus();
		} else if (mEdtWipName.hasFocus()) {
			mEdtWipName.setText(barcode);
			mEdtComponent.requestFocus();
		} else if (mEdtComponent.hasFocus()) {
			mEdtComponent.setText(barcode);
			getWorkOrder();
		}
	}

	private void resetData() {
		mTvClassCode.setText("");
		mTvStartTime.setText("");
		mTvCompletionTime.setText("");
		mTvComponentName.setText("");
		mTvLocator.setText("");

		mCurrentWorkOrder = null;
		mCurrentLots = null;
	}

	@Override
	public void afterTextChanged(Editable s) {
		resetData();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	private List<String> mLocators;
	private class LocatorAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mLocators.size();
		}

		@Override
		public Object getItem(int position) {
			return mLocators.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;

			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.locator_list_item, null);

				viewHolder = new ViewHolder();
				viewHolder.locator =(TextView) convertView.findViewById(R.id.tv_lli_locator_segment2) ;
				viewHolder.remaining =(TextView) convertView.findViewById(R.id.tv_lli_remaining) ;
				viewHolder.transaction_qty =(TextView) convertView.findViewById(R.id.tv_lli_transaction_qty) ;

				convertView.setTag(viewHolder);

			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			TransactionLot tl = mBindRemainingByGroup.get(mLocators.get(position));
			viewHolder.locator.setText(tl.locator);
			viewHolder.remaining.setText(tl.remianingQty + "");
			viewHolder.transaction_qty.setText(tl.transactionQty + "");

			return convertView;
		}

		private class ViewHolder{
			TextView locator;
			TextView remaining;
			TextView transaction_qty;
		}

	}

	private class TransactionLot{
		BigDecimal transactionQty = BigDecimal.ZERO;
		String locator;
		BigDecimal remianingQty = BigDecimal.ZERO;
	}
}
