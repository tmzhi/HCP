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
import com.hcp.entities.WorkOrder2;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WoQueryActivity extends ScannerActivity {

	private final int DATA_FOUND = 1;
	private final int DATA_NOT_FOUND = 2;

	private EditText mEdtWipName;
	private EditText mEdtComponent;
	private EditText mEdtComponentName;

	private Button mBtnQuery;

	private ListGrid mGrid;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;

	private List<WorkOrder2> mWorkOrders = new ArrayList<WorkOrder2>();

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wo_query);

		mEdtWipName = (EditText) findViewById(R.id.edt_wo_query_wip_name);
		mEdtWipName.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtComponentName = (EditText) findViewById(R.id.edt_wo_query_compnont_name);

		mEdtComponent = (EditText) findViewById(R.id.edt_wo_query_compnont);
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

		mGridContainer = (LinearLayout) findViewById(R.id.lin_wo_query_container);

		mBtnQuery = (Button) findViewById(R.id.btn_wo_query_query);
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
		columns.add(new GridColumn("工单状态", (int) (100 * density)));
		columns.add(new GridColumn("工单类型", (int) (100 * density)));
		columns.add(new GridColumn("组件编号", (int) (100 * density)));
		columns.add(new GridColumn("组件名称", (int) (140 * density)));
		columns.add(new GridColumn("单位", (int) (100 * density)));
		columns.add(new GridColumn("需求总量", (int) (120 * density)));
		columns.add(new GridColumn("已发数量", (int) (140 * density)));
		columns.add(new GridColumn("未发数量", (int) (140 * density)));
		columns.add(new GridColumn("供应方式", (int) (140 * density)));
		columns.add(new GridColumn("开始时间", (int) (120 * density)));
		columns.add(new GridColumn("完成时间", (int) (120 * density)));
		columns.add(new GridColumn("装配件编号", (int) (120 * density)));
		columns.add(new GridColumn("装配件名称", (int) (120 * density)));
		columns.add(new GridColumn("起始数量", (int) (120 * density)));
		columns.add(new GridColumn("剩余数量", (int) (120 * density)));
		columns.add(new GridColumn("已完成数量", (int) (120 * density)));
		columns.add(new GridColumn("子库存", (int) (120 * density)));
		columns.add(new GridColumn("货位", (int) (120 * density)));
		columns.add(new GridColumn("项目号", (int) (120 * density)));
		columns.add(new GridColumn("批次号", (int) (120 * density)));
		columns.add(new GridColumn("机台号码", (int) (120 * density)));
		columns.add(new GridColumn("机台名称", (int) (200 * density)));
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
		final String wipName = mEdtWipName.getText().toString();
		final String componentName = mEdtComponentName.getText().toString();

		if (TextUtils.isEmpty(wipName)) {
			ToastHelper.getInstance(this).show("【工单号】不得为空");
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
					List<WorkOrder2> workOrders = WORequestManager.getInstance(
							WoQueryActivity.this).getWorkOrderOnQuery(wipName,
							component, componentName, true);
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
			mEdtWipName.setText(barcode);
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
				mWorkOrders = (List<WorkOrder2>) msg.obj;

				Map<String, BigDecimal> mapRequired = new HashMap<String, BigDecimal>();
				Map<String, BigDecimal> mapIssued = new HashMap<String, BigDecimal>();
				Map<String, BigDecimal> mapOpen = new HashMap<String, BigDecimal>();

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();
				for (WorkOrder2 item : mWorkOrders) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.Meaning);
					row.setValue(1, item.ClassCode);
					row.setValue(2, item.Segment1);
					row.setValue(3, item.ItemDescription);
					row.setValue(4, item.ItemPrimaryUomCode);
					row.setValue(5, item.RequiredQuantity + "");
					row.setValue(6, item.QuantityIssued + "");
					row.setValue(7, item.QuantityOpen + "");
					row.setValue(8, item.WipSupplyMeaning);
					row.setValue(9, item.ScheduledStartDate);
					row.setValue(10, item.ScheduledCompletionDate);
					row.setValue(11, item.ConcatenatedSegments);
					row.setValue(12, item.Description);
					row.setValue(13, item.StartQuantity + "");
					row.setValue(14, item.QuantityRemaining + "");
					row.setValue(15, item.QuantityCompleted + "");
					row.setValue(16, item.SupplySubinventory);
					row.setValue(17, "");
					row.setValue(18, item.ProjectNumber);
					row.setValue(19, item.LotNumber);
					row.setValue(20, item.MachineNumber);
					row.setValue(21, item.MachineDescription);
					rows.add(row);

					groupTotalQuantityByUom(mapRequired, item.ItemPrimaryUomCode, item.RequiredQuantity);
					groupTotalQuantityByUom(mapOpen, item.ItemPrimaryUomCode, item.QuantityOpen);
					groupTotalQuantityByUom(mapIssued, item.ItemPrimaryUomCode, item.QuantityIssued);

				}

				String qtyRequied = getQuantityGroupMsg(mapRequired);
				String qtyIssued = getQuantityGroupMsg(mapIssued);
				String qtyOpen = getQuantityGroupMsg(mapOpen);

				ListRow totalRow = mGrid.newRow();
				totalRow.setValue(0, "【合计】");
				totalRow.setValue(5, qtyRequied);
				totalRow.setValue(6, qtyIssued);
				totalRow.setValue(7, qtyOpen);
				totalRow.setBackgroundColor(0xFFFFFACD);

				rows.add(totalRow);

				mGrid.insertRows(rows);
			} else if (msg.what == DATA_NOT_FOUND) {
				ToastHelper.getInstance(WoQueryActivity.this).show("查询失败...");
			}

			return false;
		}
	});

	private void groupTotalQuantityByUom(Map<String, BigDecimal> map, String uom, BigDecimal quantity){
		if(map.containsKey(uom)){
			map.put(uom, map.get(uom).add(quantity));
		}else{
			map.put(uom, quantity);
		}
	}

	private String getQuantityGroupMsg(Map<String, BigDecimal> map){
		StringBuilder stbMsg = new StringBuilder();

		Iterator<Map.Entry<String, BigDecimal>> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, BigDecimal> item = it.next();
			stbMsg.append(item.getKey())
					.append(":  ")
					.append(String.valueOf(item.getValue().intValue()));

			if(it.hasNext()){
				stbMsg.append("\n");
			}
		}

		return stbMsg.toString();
	}

}
