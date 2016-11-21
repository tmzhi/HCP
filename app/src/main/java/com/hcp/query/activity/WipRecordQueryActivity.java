package com.hcp.query.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.DateFormat;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.common.view.DateTimePickerDialog;
import com.hcp.entities.TransactionRecord;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.PrintManager;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WipRecordQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtWipName;
	private EditText mEdtSegment;
	private EditText mEdtSubInventory;

	private TextView mTvStartTime;
	private TextView mTvEndTime;

	private Button mBtnQuery;
	private Button mBtnStartTime;
	private Button mBtnEndTime;

	private ImageButton mImbPrint;

	private ListGrid mGrid;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private Date mStartTime;
	private Date mEndTime;

	private ProgressDialog mPDLPrint;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wip_record_query);

		mEdtSubInventory = (EditText)findViewById(R.id.edt_wrq_sub_inventory);
		mEdtSubInventory.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtWipName = (EditText) findViewById(R.id.edt_wrq_wip_entity_name);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSegment = (EditText) findViewById(R.id.edt_wrq_segment);
		mEdtSegment.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtSegment.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						query();
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});

		mBtnQuery = (Button) findViewById(R.id.btn_wrq_query);
		mBtnQuery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				query();
			}
		});

		mEndTime = new Date(System.currentTimeMillis());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mEndTime);
		calendar.add(Calendar.DAY_OF_WEEK, -1);

		mStartTime = calendar.getTime();

		mTvEndTime = (TextView) findViewById(R.id.tv_wrq_end_time);
		setDateTime(mTvEndTime, mEndTime);

		mTvStartTime = (TextView) findViewById(R.id.tv_wrq_start_time);
		setDateTime(mTvStartTime, mStartTime);

		mBtnStartTime = (Button) findViewById(R.id.btn_wrq_start_time);
		mBtnStartTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickerDialog dialog = new DateTimePickerDialog(mInstance, (Date)mBtnStartTime.getTag());
				dialog.setCallback(new DateTimePickerDialog.OnCallback() {

					@Override
					public void getDateTime(Date date) {
						mStartTime = date;
						setDateTime(mTvStartTime, date);
					}
				});
				dialog.show();
			}
		});

		mBtnEndTime = (Button) findViewById(R.id.btn_wrq_end_time);
		mBtnEndTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickerDialog dialog = new DateTimePickerDialog(mInstance, mEndTime);
				dialog.setCallback(new DateTimePickerDialog.OnCallback() {

					@Override
					public void getDateTime(Date date) {
						mEndTime = date;
						setDateTime(mTvEndTime, date);
					}
				});
				dialog.show();
			}
		});

		mImbPrint = (ImageButton) findViewById(R.id.imb_wrq_print);
		mImbPrint.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<ListRow> rows = mGrid.getAllRows();
				if(rows == null || rows.size() == 0){
					mToastHelper.show("当前列表没有数据!");
					return;
				}

				print(rows);
			}
		});

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		mGridContainer = (LinearLayout) findViewById(R.id.lin_wrq_container);

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("子库", (int) (100 * density)));
		columns.add(new GridColumn("工单号", (int) (100 * density)));
		columns.add(new GridColumn("料号", (int) (100 * density)));
		columns.add(new GridColumn("料件名称", (int) (140 * density)));
		columns.add(new GridColumn("交易数量", (int) (100 * density)));
		columns.add(new GridColumn("交易类型", (int) (140 * density)));
		columns.add(new GridColumn("交易时间", (int) (120 * density)));
		mGrid = new ListGrid(this, columns, ColumnType.WIDTH);
		mGrid.setHeaderTextSize(14);
		mGrid.setCellTextSize(12);
		mGrid.setFullRowSelect(true);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mGrid.setPadding(0, 0, 0, 0);
		mGrid.setLayoutParams(lp);
		mGridContainer.addView(mGrid);

	}

	private void print(final List<ListRow> rows){
		mPDLPrint = ProgressDialogUtil.createUnCanceledDialog(mInstance, "打印", "正在打印...");
		mPDLPrint.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				StringBuilder stbPrintMsg = new StringBuilder();

				PrintManager manager = PrintManager.getInstance(mInstance);

				Message msg = Message.obtain();
				try {
					manager.connect();
					manager.print("交 易 查 询\n\n".getBytes("GB2312"));
				} catch (IOException e1) {
					msg.obj = "打印机连接失败!";
					mPrintHandler.sendMessage(msg);
					return;
				}

				int successCount = 0;

				for(ListRow row : rows){

					stbPrintMsg.delete(0, stbPrintMsg.length());
					stbPrintMsg.append("子  库: " + row.getValue(0) + "\n");
					stbPrintMsg.append("工 单 号: " + row.getValue(1) + "\n");
					stbPrintMsg.append("料  号: " + row.getValue(2) + "\n");
					stbPrintMsg.append("料件名称: " + row.getValue(3) + "\n");
					stbPrintMsg.append("交易数量: " + row.getValue(4) + "\n");
					stbPrintMsg.append("交易类型: " + row.getValue(5) + "\n");
					stbPrintMsg.append("交易时间: " + row.getValue(6) + "\n");

					try {
						manager.print(stbPrintMsg.toString().getBytes("GB2312"));
						manager.printOneDimenBarcode(row.getValue(1) + "");
						manager.printOneDimenBarcode(row.getValue(2) + "");
						manager.print("\n----------------------\n\n".getBytes());
						successCount++;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					manager.close();
				} catch (IOException e) {

				}

				msg.obj = String.format("打印成功【%s】笔, 失败【%s】笔.", successCount + "", (rows.size() - successCount) + "");
				mPrintHandler.sendMessage(msg);
			}
		}).start();
	}

	private Handler mPrintHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			if(mPDLPrint != null){
				mPDLPrint.cancel();
			}

			if(msg.obj != null){
				mToastHelper.show(msg.obj.toString());
			}

			return false;
		}
	});

	private void setDateTime(TextView input, Date date){
		input.setText(DateFormat.defaultFormat2(date));
	}

	private void query() {

		final String segment = mEdtSegment.getText().toString();
		final String wipName = mEdtWipName.getText().toString();
		final String subinventory = mEdtSubInventory.getText().toString();

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"获取交易记录", "交易记录获取中,请稍后...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();

				try {
					List<TransactionRecord> transactionRecords = WORequestManager.getInstance(
							WipRecordQueryActivity.this).getTransactionRecord(subinventory, wipName, segment, mTvStartTime.getText().toString() + ":00", mTvEndTime.getText().toString() + ":59");
					msg.obj = transactionRecords;
					msg.what = DATA_FOUND;
				} catch (Exception e) {
					msg.what = DATA_NOT_FOUND;
				} finally {
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	protected void decodeCallback(String barcode) {
		if(mEdtSubInventory.hasFocus()){
			mEdtSubInventory.setText(barcode);
			mEdtSubInventory.requestFocus();
		}else if (mEdtWipName.hasFocus()) {
			mEdtWipName.setText(barcode);
			mEdtSegment.requestFocus();
		} else {
			mEdtSegment.setText(barcode);
		}
	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {
			mGrid.clearRows();

			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}

			if (msg.what == DATA_FOUND) {

				List<TransactionRecord> records = (List<TransactionRecord>) msg.obj;

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();
				for (TransactionRecord item : records) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.SubInventory);
					row.setValue(1, item.WipEntityName);
					row.setValue(2, item.Segment);
					row.setValue(3, item.Description);
					row.setValue(4, item.TransactionQuantity + "");
					row.setValue(5, item.TransactionType);
					row.setValue(6, item.TransactionDate);
					rows.add(row);
				}
				mGrid.insertRows(rows);
			} else if (msg.what == DATA_NOT_FOUND) {
				ToastHelper.getInstance(WipRecordQueryActivity.this).show("查询失败...");
			}

			return false;
		}
	});

}
