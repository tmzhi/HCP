package com.hcp.query.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.WorkOrderSimple;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.util.ArrayList;
import java.util.List;

public class ComponentQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtComponent;

	private ListGrid mGrid;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private List<WorkOrderSimple> mWorkOrders = new ArrayList<WorkOrderSimple>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_component_query);

		mEdtComponent = (EditText) findViewById(R.id.edt_component_query_component);
		mEdtComponent.setOnKeyListener(new View.OnKeyListener() {
			boolean handsOn = false;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && !handsOn) {
					handsOn = true;
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						String component = mEdtComponent.getText().toString();
						query(component);
					}
				} else if (event.getAction() == KeyEvent.ACTION_UP && handsOn) {
					handsOn = false;
				}
				return false;
			}
		});

		mGridContainer = (LinearLayout) findViewById(R.id.lin_component_query_container);

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

	private void query(final String component) {

		if (TextUtils.isEmpty(component)) {
			ToastHelper.getInstance(this).show("请输入组件编号!");
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
				"获取工单", "工单获取,请稍后...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();

				try {
					List<WorkOrderSimple> workOrders = WORequestManager
							.getInstance(ComponentQueryActivity.this)
							.getWorkOrderByComponent(component);
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
		mEdtComponent.setText(barcode);
		query(barcode);
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
				ToastHelper.getInstance(ComponentQueryActivity.this).show(
						"查询失败...");
			}

			return false;
		}
	});

}
