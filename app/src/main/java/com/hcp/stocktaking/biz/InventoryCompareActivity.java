package com.hcp.stocktaking.biz;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.LocatorDao;
import com.hcp.dao.SubInventoryDao;
import com.hcp.stocktaking.entity.InventoryCompare;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoryCompareActivity extends ScannerActivity {

	private Button mBtnQuery;

	private ListGrid mGrid;
	private EditText mEdtItemno;
	private EditText mEdtProjectno;
	private EditText mEdtSubinventory;
	private EditText mEdtLocator;
	private EditText mEdtLotno;

	private CheckBox mCbDefferent;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;
	private WORequestManager mRequestManager;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory_compare);

		mRequestManager = WORequestManager.getInstance(mInstance);

		mBtnQuery = (Button) findViewById(R.id.btn_inventory_compare_query);
		mBtnQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getQuantity();
			}
		});

		mGridContainer = (LinearLayout) findViewById(R.id.lin_inventory_compare_container);

		mEdtItemno = (EditText) findViewById(R.id.edt_inventory_compare_item_no);
		mEdtItemno.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtProjectno = (EditText) findViewById(R.id.edt_inventory_compare_project_no);
		mEdtProjectno.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLocator = (EditText) findViewById(R.id.edt_inventory_compare_locator);
		mEdtLocator.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubinventory = (EditText) findViewById(R.id.edt_inventory_compare_subinventory);
		mEdtSubinventory.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtLotno = (EditText) findViewById(R.id.edt_inventory_compare_lot_no);
		mEdtLotno.setOnTouchListener(mHideKeyBoardTouchEvent);

		mCbDefferent = (CheckBox) findViewById(R.id.cb_inventory_compare_defferent);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("序号", (int) (40 * density)));
		columns.add(new GridColumn("子库", (int) (80 * density)));
		columns.add(new GridColumn("货位", (int) (100 * density)));
		columns.add(new GridColumn("料件编号", (int) (120 * density)));
		columns.add(new GridColumn("项目号", (int) (100 * density)));
		columns.add(new GridColumn("批次号", (int) (100 * density)));
		columns.add(new GridColumn("库存快照数量", (int) (120 * density)));
		columns.add(new GridColumn("盘点卡数量", (int) (120 * density)));
		columns.add(new GridColumn("差异量", (int) (120 * density)));
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

	private void getQuantity(){

		final String lotno = mEdtLotno.getText().toString();
		final String projectno = mEdtProjectno.getText().toString();
		final String itemno = mEdtItemno.getText().toString();
		final String subinventory = mEdtSubinventory.getText().toString();
		final String locator = mEdtLocator.getText().toString();

		if(TextUtils.isEmpty(subinventory)){
			mToastHelper.show("请输入【子库】");
			return;
		}

		if(mProgressDialog != null && mProgressDialog.isShowing()){
			return ;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(mInstance, "请求", "正在获取快照与盘点卡数量!");
		mProgressDialog.show();

		final boolean onlyShowDiff = mCbDefferent.isChecked();

		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<InventoryCompare> compares = getInventoryCompare(itemno, subinventory, projectno, lotno, locator);
				if(compares == null){
					return;
				}

				if(onlyShowDiff && compares.size() > 0){
					Iterator<InventoryCompare> iterator = compares.iterator();
					while(iterator.hasNext()){
						try{
							InventoryCompare item = iterator.next();
							if(item.Card_Quantity == null){
								item.Card_Quantity = BigDecimal.ZERO;
							}

							if(item.Snapshot_Quantity == null){
								item.Snapshot_Quantity = BigDecimal.ZERO;
							}
							if(item.Card_Quantity.subtract(item.Snapshot_Quantity).compareTo(BigDecimal.ZERO) == 0){
								iterator.remove();
							}
						}catch (Exception ex){
							Log.e("Error", ex.getMessage());
						}
					}
				}
				showCompares(compares);
			}
		}).start();
	}

	@Override
	protected void decodeCallback(String barcode) {

		try {
			if(LocatorDao.getInstance(mInstance).existLocator(barcode)){
				mEdtLocator.setText(barcode);
				return;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if(SubInventoryDao.getInstance(mInstance).existSubInventories(barcode)){
				mEdtSubinventory.setText(barcode);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(mEdtProjectno.hasFocus()){
			mEdtProjectno.setText(barcode);
		}else if(mEdtLotno.hasFocus()){
			mEdtLotno.setText(barcode);
		}else{
			mEdtItemno.setText(barcode);
		}
	}

	private List<InventoryCompare> getInventoryCompare(final String itemno, final String subinventory, final String projectno, final String lotno, final String locator){
		try {
			List<InventoryCompare> qty = mRequestManager.getInventoryCompare(itemno, subinventory, projectno, lotno, locator);
			return qty;
		} catch (Exception e) {
			showErrorMsg(String.format("对比数据获取异常:%s", e.getMessage()));
		}
		return null;
	}

	private void showCompares(final List<InventoryCompare> list){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mProgressDialog.setMessage("正在显示数量...");

				mGrid.clearRows();
				int index = 1;
				List<ListRow> rows = new ArrayList<ListRow>();
				for(InventoryCompare item : list){
					ListRow row = mGrid.newRow();
					row.setValue(0, index + "");
					row.setValue(1, item.Sub_Inventory);
					row.setValue(2, item.Locator == null ? "" : item.Locator);
					row.setValue(3, item.Item_No);
					row.setValue(4, item.Project_No == null ? "" : item.Project_No);
					row.setValue(5, item.Lot_No == null ? "" : item.Lot_No);
					row.setValue(6, item.Snapshot_Quantity + "");
					row.setValue(7, item.Card_Quantity + "");
					row.setValue(8, item.Card_Quantity.subtract(item.Snapshot_Quantity) + "");

					index ++;
					rows.add(row);
				}
				mGrid.insertRows(rows);

				if(mProgressDialog != null && mProgressDialog.isShowing()){
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}

			}
		});
	}

	private void changeDialogMsg(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.setMessage(msg);
				}
			}
		});
	}

	private void showErrorMsg(final String msg){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToastHelper.show(msg);

				hideProgressDialog();
			}
		});
	}
	private void hideProgressDialog(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
}
