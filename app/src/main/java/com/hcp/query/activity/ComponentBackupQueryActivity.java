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
import com.hcp.entities.WorkOrderSimple;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.util.AppConfig;
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

public class ComponentBackupQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtComponent;
	private EditText mEdtOrganization;

	private TextView mTvStartTime;
	private TextView mTvEndTime;

	private Button mBtnQuery;
	private Button mBtnStartTime;
	private Button mBtnEndTime;

	private ListGrid mGrid;

	private ImageButton mImBPrint;

	private Date mStartTime;
	private Date mEndTime;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private ProgressDialog mPDLPrint;

	private List<WorkOrderSimple> mWorkOrders = new ArrayList<WorkOrderSimple>();

	private AppConfig mAppConfig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_component_backup_query);

		mAppConfig = AppConfig.getInstance(mInstance);

		mEdtComponent = (EditText) findViewById(R.id.edt_cbq_component_code);
		mEdtComponent.setOnTouchListener(new HideKeyBoardTouchEvent());
		mEdtComponent.setOnKeyListener(new View.OnKeyListener() {
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

		mEdtOrganization =  (EditText) findViewById(R.id.edt_cbq_organization_code);

		mTvStartTime = (TextView) findViewById(R.id.tv_cbq_start_time);
		mTvEndTime = (TextView) findViewById(R.id.tv_cbq_end_time);

		Date now = new Date(System.currentTimeMillis());
		mStartTime = now;
		mTvStartTime.setText(DateFormat.defaultFormat2(mStartTime));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.DAY_OF_MONTH, 5);
		mEndTime = calendar.getTime();
		mTvEndTime.setText(DateFormat.defaultFormat2(mEndTime));

		mBtnQuery = (Button) findViewById(R.id.btn_cbq_query);
		mBtnQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				query();
			}
		});

		mBtnStartTime = (Button) findViewById(R.id.btn_cbq_start_time);
		mBtnStartTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickerDialog dialog = new DateTimePickerDialog(mInstance, mStartTime);
				dialog.setCallback(new DateTimePickerDialog.OnCallback(){

					@Override
					public void getDateTime(Date date) {
						mStartTime = date;
						mTvStartTime.setText(DateFormat.defaultFormat2(date));
					}
				});
				dialog.show();
			}
		});

		mBtnEndTime = (Button) findViewById(R.id.btn_cbq_end_time);
		mBtnEndTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickerDialog dialog = new DateTimePickerDialog(mInstance, mEndTime);
				dialog.setCallback(new DateTimePickerDialog.OnCallback() {

					@Override
					public void getDateTime(Date date) {
						mEndTime = date;
						mTvEndTime.setText(DateFormat.defaultFormat2(date));
					}
				});
				dialog.show();
			}
		});

		mImBPrint = (ImageButton) findViewById(R.id.imb_biz_query_print);
		if(!mAppConfig.isPrintEnable()){
			mImBPrint.setVisibility(View.GONE);
		}else{
			mImBPrint.setOnClickListener(new OnClickListener() {

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
		}
		mGridContainer = (LinearLayout) findViewById(R.id.lin_cbq_container);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("组织", (int) (80 * density)));
		columns.add(new GridColumn("工单号", (int) (80 * density)));
		columns.add(new GridColumn("组件编号", (int) (120 * density)));
		columns.add(new GridColumn("组件名称", (int) (120 * density)));
		columns.add(new GridColumn("需求数量", (int) (120 * density)));
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
					manager.print("备 料 查 询\n\n".getBytes("GB2312"));
				} catch (IOException e1) {
					msg.obj = "打印机连接失败!";
					mPrintHandler.sendMessage(msg);
					return;
				}

				int successCount = 0;

				for(ListRow row : rows){

					stbPrintMsg.delete(0, stbPrintMsg.length());
					stbPrintMsg.append("组    织: " + row.getValue(0) + "\n");
					stbPrintMsg.append("工 单 号: " + row.getValue(1) + "\n");
					stbPrintMsg.append("组件编号: " + row.getValue(2) + "\n");
					stbPrintMsg.append("组件名称: " + row.getValue(3) + "\n");
					stbPrintMsg.append("需求数量: " + row.getValue(4) + "\n");

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

	private void query() {
		final String component = mEdtComponent.getText().toString();
		final String organization = mEdtOrganization.getText().toString();

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"获取工单", "工单获取,请稍后...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();

				try {
					List<WorkOrderSimple> workOrders = WORequestManager
							.getInstance(mInstance)
							.getWorkOrderByComponent(component, organization, DateFormat.defaultFormat2(mStartTime) + ":00", DateFormat.defaultFormat2(mEndTime) + ":59");
					msg.obj = workOrders;
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
		if(mEdtOrganization.hasFocus()){
			mEdtOrganization.setText(barcode);
			mEdtComponent.requestFocus();
		}else{
			mEdtComponent.setText(barcode);
			query();
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
				mWorkOrders = (List<WorkOrderSimple>) msg.obj;

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();
				for (WorkOrderSimple item : mWorkOrders) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.Organization);
					row.setValue(1, item.WipEntityName);
					row.setValue(2, item.Segment1);
					row.setValue(3, item.ItemDescription);
					row.setValue(4, item.RequiredQuantity + "");

					rows.add(row);
				}
				mGrid.insertRows(rows);
			} else if (msg.what == DATA_NOT_FOUND) {
				ToastHelper.getInstance(mInstance).show(
						"查询失败...");
			}

			return false;
		}
	});

}
