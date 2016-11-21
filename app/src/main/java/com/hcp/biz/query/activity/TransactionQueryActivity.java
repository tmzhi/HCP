package com.hcp.biz.query.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;

import com.hcp.biz.entities.TransactionInt;
import com.hcp.common.DateFormat;
import com.hcp.common.ToastHelper;
import com.hcp.dao.TransactionIntDao;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.util.ArrayList;
import java.util.List;

public class TransactionQueryActivity extends BaseTransactionQueryActivity{

	private ListGrid mGrid;

	private TransactionIntDao mDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("原工单号", (int) (80 * density)));
		columns.add(new GridColumn("组件编号", (int) (80 * density)));
		columns.add(new GridColumn("组件名称", (int) (160 * density)));
		columns.add(new GridColumn("工单号", (int) (80 * density)));
		columns.add(new GridColumn("移动数量", (int) (120 * density)));
		columns.add(new GridColumn("可移数量", (int) (120 * density)));
		columns.add(new GridColumn("组件数量", (int) (120 * density)));
		columns.add(new GridColumn("交易时间", (int) (120 * density)));
		columns.add(new GridColumn("操作人", (int) (120 * density)));
		mGrid = new ListGrid(this, columns, ColumnType.WIDTH);
		mGrid.setHeaderTextSize(14);
		mGrid.setCellTextSize(12);
		mGrid.setFullRowSelect(true);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mGrid.setPadding(0, 0, 0, 0);
		mGrid.setLayoutParams(lp);
		mGridContainer.addView(mGrid);

		mDao = TransactionIntDao.getInstance(this);
	}

	@Override
	protected void query() {

		mGrid.clearRows();

		try {
			String startTime = mTvStartTime.getText().toString();
			String endTime = mTvEndTime.getText().toString() + ":59";
			String wipName = mEdtWipName.getText().toString();
			String component = mEdtComponent.getText().toString();

			List<TransactionInt> list = mDao.onQuery()
					.whereClause()
					.addComponent(component)
					.addWipName(wipName)
					.addStartTime(startTime)
					.addEndTime(endTime)
					.confirm()
					.orderByTimeDesc()
					.getTransactionInts();

			if(list != null && list.size()>0){

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();

				for(TransactionInt item : list){
					ListRow row = mGrid.newRow();
					row.setValue(0, item.ori_wip_entity_name);
					row.setValue(1, item.component_code);
					row.setValue(2, item.component_name);
					row.setValue(3, item.wip_entity_name);
					row.setValue(4, item.transaction_quantity + "");
					row.setValue(5, item.required_quantity + "");
					row.setValue(6, item.remaining_quantity + "");
					row.setValue(7, DateFormat.defaultFormat(item.create_time));
					row.setValue(8, item.create_by + "");

					rows.add(row);
				}
				mGrid.insertRows(rows);
			}
		} catch (Exception e) {
			ToastHelper.getInstance(TransactionQueryActivity.this).show(e.getMessage());
		}
	}
}
