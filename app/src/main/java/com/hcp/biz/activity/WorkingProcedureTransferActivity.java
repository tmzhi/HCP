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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hcp.biz.entities.OperationTransaction;
import com.hcp.biz.query.activity.OperationTransactionQueryActivity;
import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.OperationTransactionDao;
import com.hcp.entities.OperationQuery;
import com.hcp.entities.ProcedureTransfer;
import com.hcp.entities.WorkOrder3;
import com.hcp.http.RequestUtil;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.AppCache;
import com.hcp.util.PrintManager;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkingProcedureTransferActivity extends ScannerActivity implements
		TextWatcher {

	private final int REQUEST_DO_GET_WORKORDER = 0;
	private final int REQUEST_DO_SUBMIT = 1;

	private Spinner mSpnSerialNoFrom;
	private Spinner mSpnSerialNoTo;
	private Spinner mSpnStateFrom;
	private Spinner mSpnStateTo;
	private Spinner mSpnTransactionType;

	private TextView mTvClassCode;
	private TextView mTvStartTime;
	private TextView mTvCompletionTime;

	private EditText mEdtWipName;
	private EditText mEdtTransQty;

	private Button mBtnSubmit;

	private String[] mSerialNos = null;
	private String[] mStates = null;
	private String[] mTransactionTypes = null;

	private Resources mResources;
	private ToastHelper mToastHelper;
	private ProgressDialog mProgressDialog;
	private WORequestManager mRequestManager;

	private WorkOrder3 mCurrentWorkOrder;

	private ImageButton mImbQuery;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private String[] mOperSeq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_working_procedure_transfer);

		mResources = getResources();
		mToastHelper = ToastHelper.getInstance(this);
		mRequestManager = WORequestManager.getInstance(this);

		mSerialNos = mResources.getStringArray(R.array.wpt_serial);
		mStates = mResources.getStringArray(R.array.wpt_states);
		mTransactionTypes = mResources.getStringArray(R.array.wpt_transaction_types);

		mSpnSerialNoFrom = (Spinner) findViewById(R.id.spn_wpt_serial_no_from);
//		mSpnSerialNoFrom.setAdapter(getSpnAdapter(mSerialNos));
//		mSpnSerialNoFrom.setSelection(1);

		mSpnSerialNoTo = (Spinner) findViewById(R.id.spn_wpt_serial_no_to);
//		mSpnSerialNoTo.setAdapter(getSpnAdapter(mSerialNos));
//		mSpnSerialNoTo.setSelection(1);

		mSpnStateFrom = (Spinner) findViewById(R.id.spn_wpt_state_from);
		mSpnStateFrom.setAdapter(getSpnAdapter(mStates));
		mSpnStateFrom.setSelection(0);

		mSpnStateTo = (Spinner) findViewById(R.id.spn_wpt_state_to);
		mSpnStateTo.setAdapter(getSpnAdapter(mStates));
		mSpnStateTo.setSelection(2);

		mSpnTransactionType = (Spinner) findViewById(R.id.spn_wpt_transaction_type);
		mSpnTransactionType.setAdapter(getSpnAdapter(mTransactionTypes));

		mTvClassCode = (TextView) findViewById(R.id.tv_wpt_class_code);
		mTvCompletionTime = (TextView) findViewById(R.id.tv_wpt_completion_time);
		mTvStartTime = (TextView) findViewById(R.id.tv_wpt_start_time);

		mEdtTransQty = (EditText) findViewById(R.id.edt_wpt_transfer_qty);
		mEdtTransQty.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtWipName = (EditText) findViewById(R.id.edt_wpt_ori_wo);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtWipName.addTextChangedListener(this);
		mEdtWipName.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						getWorkOrder();
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});

		mBtnSubmit = (Button) findViewById(R.id.btn_wpt_submit);
		mBtnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mImbQuery = (ImageButton) findViewById(R.id.imb_wpt_query);
		mImbQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mInstance, OperationTransactionQueryActivity.class);
				mInstance.startActivity(intent);

			}
		});
	}

	private void submit() {
		if (mCurrentWorkOrder == null) {
			mToastHelper.show("请先获取工单信息!");
			return;
		}

		String transQtyStr = mEdtTransQty.getText().toString();
		if (TextUtils.isEmpty(transQtyStr)) {
			mToastHelper.show("请输入【移动数量】!");
			return;
		}

		final ProcedureTransfer proce = new ProcedureTransfer();
		proce.WipEntityName = mEdtWipName.getText().toString();
		proce.FromIntraOperationStepMeaning = mSpnStateFrom.getSelectedItem()
				.toString();
		proce.FromOperationSeqNum = mSpnSerialNoFrom.getSelectedItem()
				.toString();
		proce.Organization = mCurrentWorkOrder.Organization;
		proce.Reason = "Trx From Mobile";
		proce.Reference = "IMEI:" + Device.getIMEI(this);
		proce.ToIntraOperationStepMeaning = mSpnStateTo.getSelectedItem()
				.toString();
		proce.ToOperationSeqNum = mSpnSerialNoTo.getSelectedItem().toString();
		proce.TransactionQuantity = new BigDecimal(mEdtTransQty.getText()
				.toString());
		proce.TransactionType = mSpnTransactionType.getSelectedItem().toString();
		proce.CreatedByName = AppCache.getInstance().getLoginUser().toUpperCase(Locale.getDefault());

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "工序移动提交中...");
			mProgressDialog.show();
		}

		final String transMsg = getSuccessMsg();
		final String wipName = mEdtWipName.getText().toString();
		final int operatonNumberFm = Integer.valueOf(mSpnSerialNoFrom.getSelectedItem().toString());
		final int operatonNumberTo = Integer.valueOf(mSpnSerialNoTo.getSelectedItem().toString());

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = REQUEST_DO_SUBMIT;

				boolean isOperationExist = false;
				String errorMsg = null;
				try {
					int operationFrom = mRequestManager.getOperationCount(wipName, operatonNumberFm);
					int operationTo = mRequestManager.getOperationCount(wipName, operatonNumberTo);

					if(operationFrom > 0 && operationTo > 0){
						isOperationExist = true;
					}else{
						errorMsg = "工单,序号不存在!";
					}
				} catch (Exception e1) {
					errorMsg = "请求错误:" + e1.getMessage();
				}

				if(!isOperationExist){

					final String error = errorMsg;
					mInstance.runOnUiThread(new Runnable() {

						@Override
						public void run() {

							if (mProgressDialog != null) {
								mProgressDialog.cancel();
								mProgressDialog = null;
							}
							mToastHelper.show(error);
						}
					});

					return;
				}

				try {
					String result = mRequestManager.submitProcedure(proce);

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
					msg.obj = e.getMessage();
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
				case REQUEST_DO_GET_WORKORDER:
					if (msg.obj != null) {

						if(mOperSeq != null){
							mSpnSerialNoFrom.setAdapter(getSpnAdapter(mOperSeq));
							mSpnSerialNoTo.setSelection(0);

							mSpnSerialNoTo.setAdapter(getSpnAdapter(mOperSeq));
							mSpnSerialNoTo.setSelection(mOperSeq.length - 1);
						}

						mCurrentWorkOrder = (WorkOrder3) msg.obj;

						mTvClassCode.setText(mCurrentWorkOrder.ClassCode);
						mTvStartTime.setText(mCurrentWorkOrder.ScheduledStartDate);
						mTvCompletionTime
								.setText(mCurrentWorkOrder.ScheduledCompletionDate);

					} else {
						mToastHelper.show("工单获取失败!");
					}
					break;
				case REQUEST_DO_SUBMIT:
					if (msg.obj != null) {
						String result = (String) msg.obj;
						if (RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(result)) {

							showTransSuccessMsg(saveOperationTransaction());

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

	private OperationTransaction saveOperationTransaction(){
		OperationTransaction op = new OperationTransaction();
		op.class_code = mTvClassCode.getText().toString();

		Date completionTime = new Date(System.currentTimeMillis());
		Date startTime = new Date(System.currentTimeMillis());

		try {
			completionTime = DateFormat.defaultParse(mTvCompletionTime.getText().toString());
			startTime = DateFormat.defaultParse(mTvStartTime.getText().toString());
		} catch (Exception e) {

		}

		op.completion_time = completionTime;
		op.start_time = startTime;

		op.create_by = AppCache.getInstance().getLoginUser();
		op.create_time = new Date(System.currentTimeMillis());
		op.serial_from = Integer.parseInt(mSpnSerialNoFrom.getSelectedItem().toString());
		op.serial_to = Integer.parseInt(mSpnSerialNoTo.getSelectedItem().toString());
		op.step_from = mSpnStateFrom.getSelectedItem().toString();
		op.step_to = mSpnStateTo.getSelectedItem().toString();
		op.transaction_quantity =new BigDecimal(mEdtTransQty.getText().toString());
		op.transaction_type = mSpnTransactionType.getSelectedItem().toString();
		op.wip_entity_name = mEdtWipName.getText().toString();

		try {
			OperationTransactionDao.getInstance(this).onInsert().insertOperationTransaction(op);
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}

		return op;
	}

	private void showTransSuccessMsg(OperationTransaction op) {

		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage(getSuccessMsg().toString()).setPositiveButton("确定", null)
				.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private String getSuccessMsg(){

		StringBuilder stbMsg = new StringBuilder();
		stbMsg.append("工 序 移 动\n");
		stbMsg.append("--------------------------\n");
		// stbMsg.append(mResources.getString(R.string.mr_ori_code) +
		// mEdtWipName.getText().toString() + "\n");
		stbMsg.append("工单号:" + mEdtWipName.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.wpt_class_code)
				+ mTvClassCode.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.wpt_start_time)
				+ mTvStartTime.getText().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.wpt_end_time)
				+ mTvCompletionTime.getText().toString()+ "\n");
		stbMsg.append(mResources.getString(R.string.wpt_transaction_type)
				+ mSpnTransactionType.getSelectedItem().toString()+ "\n");
		stbMsg.append("初始序号:"+ mSpnSerialNoFrom.getSelectedItem().toString() + "\n");
		stbMsg.append("初始状态:"+ mSpnStateFrom.getSelectedItem().toString() + "\n");
		stbMsg.append("目标序号:"+ mSpnSerialNoTo.getSelectedItem().toString() + "\n");
		stbMsg.append("目标状态:"+ mSpnStateTo.getSelectedItem().toString() + "\n");
		stbMsg.append(mResources.getString(R.string.wpt_transfer_qty)
				+ mEdtTransQty.getText().toString());

		return stbMsg.toString();
	}

	@Override
	protected void decodeCallback(String barcode) {
		mEdtWipName.setText(barcode);
		getWorkOrder();

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
				msg.what = REQUEST_DO_GET_WORKORDER;
				try {
					List<OperationQuery> ops = mRequestManager.getOperationSeq(wipEntityName);
					if(ops != null && ops.size() > 0){
						mOperSeq = new String[ops.size()];
						int index = 0;
						for(OperationQuery item : ops){
							mOperSeq[index] = item.OperationSeqNum;
							index++;
						}

					}

					WorkOrder3 workOrder = mRequestManager
							.getWorkOrderOnCompletion(wipEntityName);
					msg.obj = workOrder;
				} catch (Exception e) {
					Log.i("WorkingProcedure", e.getMessage());
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private void clearValues() {
		mTvClassCode.setText("");
		mTvStartTime.setText("");
		mTvCompletionTime.setText("");

		mCurrentWorkOrder = null;
	}

	private ArrayAdapter<String> getSpnAdapter(String[] array) {
		ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this,
				R.layout.simple_spinner_item, array);
		spnAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spnAdapter;
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
