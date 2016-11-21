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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.entities.LotQuantityInWarehouse;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StandingCorpQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtSubInventory;
	private EditText mEdtComponent;
	private EditText mEdtComponentName;

	private Button mBtnQuery;

	private ListGrid mGrid;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_standing_corp_query);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_sc_query_sub_inventory);
		mEdtSubInventory.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponentName = (EditText) findViewById(R.id.edt_sc_query_compnont_name);

		mEdtComponent = (EditText) findViewById(R.id.edt_sc_query_compnont);
		mEdtComponent.setOnTouchListener(mHideKeyBoardTouchEvent);
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

		mGridContainer = (LinearLayout) findViewById(R.id.lin_sc_query_container);

		mBtnQuery = (Button) findViewById(R.id.btn_sc_query_query);
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
		columns.add(new GridColumn("组织编号", (int) (80 * density)));
		columns.add(new GridColumn("子库", (int) (100 * density)));
		columns.add(new GridColumn("组件编号", (int) (80 * density)));
		columns.add(new GridColumn("组件名称", (int) (150 * density)));
		columns.add(new GridColumn("货位", (int) (140 * density)));
		columns.add(new GridColumn("批次号", (int) (100 * density)));
		columns.add(new GridColumn("批次生成日期", (int) (150 * density)));
		columns.add(new GridColumn("现有量", (int) (140 * density)));
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

		final String component = mEdtComponent.getText().toString();
		final String subInventory = mEdtSubInventory.getText().toString();
		final String componentName = mEdtComponentName.getText().toString();

		if (TextUtils.isEmpty(subInventory) && TextUtils.isEmpty(component)) {
			ToastHelper.getInstance(this).show("请输入【子库】或【料号】!");
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this, "加载",
				"批次数量获取中,请稍后...");
		mProgressDialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = Message.obtain();

				try {
					List<LotQuantityInWarehouse> workOrders = WORequestManager
							.getInstance(mInstance)
							.getLotQuantityInWarehouses("", subInventory,
									StringUtils.trimToEmpty(component), componentName, null, false);
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
		if (mEdtComponent.hasFocus()) {
			mEdtComponent.setText(barcode);
		} else {
			mEdtSubInventory.setText(barcode);
			mEdtComponent.requestFocus();
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
				List<LotQuantityInWarehouse> lots = (List<LotQuantityInWarehouse>) msg.obj;

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();
				for (LotQuantityInWarehouse item : lots) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.OrganizationCode);
					row.setValue(1, item.SubInventoryCode);
					row.setValue(2, item.Segment1);
					row.setValue(3, item.ItemDescription);
					row.setValue(4, item.SupplySegment);
					row.setValue(5, item.LotNumber);
					row.setValue(6, item.LotDate);
					row.setValue(7, item.StandingCrop + "");
					rows.add(row);
				}
				mGrid.insertRows(rows);
			} else {
				ToastHelper.getInstance(StandingCorpQueryActivity.this).show(
						"查询失败...");
			}

			return false;
		}
	});

}
