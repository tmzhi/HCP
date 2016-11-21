package com.hcp.query.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.OperationQuery;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.util.ArrayList;
import java.util.List;

public class OperationQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtWipName;

	private Button mBtnQuery;

	private ListGrid mGrid;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private List<OperationQuery> mOps = new ArrayList<OperationQuery>();

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_operation_query);

		mEdtWipName = (EditText) findViewById(R.id.edt_operation_query_wip_name);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mGridContainer = (LinearLayout) findViewById(R.id.lin_operation_querycontainer);

		mBtnQuery = (Button) findViewById(R.id.btn_operation_queryquery);
		mBtnQuery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				query();
			}
		});

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("组织", (int) (100 * density)));
		columns.add(new GridColumn("工单号", (int) (100 * density)));
		columns.add(new GridColumn("工单状态", (int) (100 * density)));
		columns.add(new GridColumn("装配件编号", (int) (120 * density)));
		columns.add(new GridColumn("装配件名称", (int) (120 * density)));
		columns.add(new GridColumn("工单需求数量", (int) (120 * density)));
		columns.add(new GridColumn("工单完工数量", (int) (120 * density)));
		columns.add(new GridColumn("工序号", (int) (120 * density)));
		columns.add(new GridColumn("排队数量", (int) (120 * density)));
		columns.add(new GridColumn("运行数量", (int) (120 * density)));
		columns.add(new GridColumn("移动数量", (int) (120 * density)));
		columns.add(new GridColumn("拒绝数量", (int) (120 * density)));
		columns.add(new GridColumn("报废数量", (int) (120 * density)));
		columns.add(new GridColumn("完成数量", (int) (120 * density)));
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

	private void query() {

		final String wipName = mEdtWipName.getText().toString();

		if (TextUtils.isEmpty(wipName)) {
			ToastHelper.getInstance(this).show("【工单号】不得为空");
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"获取工序信息", "工序获取中,请稍后...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();

				try {
					List<OperationQuery> ops = WORequestManager.getInstance(
							OperationQueryActivity.this).getOperationSeq(wipName);
					msg.obj = ops;
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
		mEdtWipName.setText(barcode);
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
				mOps = (List<OperationQuery>) msg.obj;

				List<ListRow> rows = new ArrayList<ListRow>();
				for (OperationQuery item : mOps) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.Orgnization);
					row.setValue(1, item.WipName);
					row.setValue(2, item.Meaning);
					row.setValue(3, item.Segment1);
					row.setValue(4, item.Description);
					row.setValue(5, item.WoStartQuantity + "");
					row.setValue(6, item.WoCompletedQuantity + "");
					row.setValue(7, item.OperationSeqNum);
					row.setValue(8, item.InQueueQuantity + "");
					row.setValue(9, item.RunningQuantity + "");
					row.setValue(10, item.WaitingToMoveQuantity + "");
					row.setValue(11, item.RejectedQuantity + "");
					row.setValue(12, item.ScrappedQuantity + "");
					row.setValue(13, item.CompletedQuantity + "");
					rows.add(row);
				}

				mGrid.insertRows(rows);
			} else if (msg.what == DATA_NOT_FOUND) {
				ToastHelper.getInstance(OperationQueryActivity.this).show("查询失败...");
			}

			return false;
		}
	});

}
