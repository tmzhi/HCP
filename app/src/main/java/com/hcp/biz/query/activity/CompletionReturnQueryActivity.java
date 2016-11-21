package com.hcp.biz.query.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;

import com.hcp.biz.entities.CompletionReturnInfo;
import com.hcp.common.DateFormat;
import com.hcp.common.ToastHelper;
import com.hcp.dao.CompletionReturnInfoDao;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.util.ArrayList;
import java.util.List;

public class CompletionReturnQueryActivity extends BaseTransactionQueryActivity{

	private ListGrid mGrid;

	private CompletionReturnInfoDao mDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("子库", (int) (60 * density)));
		columns.add(new GridColumn("工单号", (int) (80 * density)));
		columns.add(new GridColumn("工单类型", (int) (80 * density)));
		columns.add(new GridColumn("开始时间", (int) (120 * density)));
		columns.add(new GridColumn("完成时间", (int) (120 * density)));
		columns.add(new GridColumn("制品号码", (int) (100 * density)));
		columns.add(new GridColumn("制品名称", (int) (160 * density)));
		columns.add(new GridColumn("已完工", (int) (80 * density)));
		columns.add(new GridColumn("剩余数量", (int) (120 * density)));
		columns.add(new GridColumn("工单备注", (int) (120 * density)));
		columns.add(new GridColumn("完工数量", (int) (120 * density)));
		columns.add(new GridColumn("交易时间", (int) (120 * density)));
		columns.add(new GridColumn("操作人", (int) (100 * density)));

		mGrid = new ListGrid(this, columns, ColumnType.WIDTH);
		mGrid.setHeaderTextSize(14);
		mGrid.setCellTextSize(12);
		mGrid.setFullRowSelect(true);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mGrid.setPadding(0, 0, 0, 0);
		mGrid.setLayoutParams(lp);
		mGridContainer.addView(mGrid);

		mDao = CompletionReturnInfoDao.getInstance(this);
	}

	@Override
	protected void query() {

		mGrid.clearRows();

		try {
			String startTime = mTvStartTime.getText().toString();
			String endTime = mTvEndTime.getText().toString() + ":59";
			String wipName = mEdtWipName.getText().toString();
			String component = mEdtComponent.getText().toString();

			List<CompletionReturnInfo> list = mDao.onQuery()
					.whereClause()
					.addComponent(component)
					.addWipName(wipName)
					.addStartTime(startTime)
					.addEndTime(endTime)
					.confirm()
					.orderByTimeDesc()
					.getCompletionReturnsInfos();

			if(list != null && list.size()>0){

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();

				for(CompletionReturnInfo item : list){
					ListRow row = mGrid.newRow();
					row.setValue(0, item.sub_inventory);
					row.setValue(1, item.wip_entity_name);
					row.setValue(2, item.class_code);
					row.setValue(3, DateFormat.defaultFormat(item.start_time));
					row.setValue(4, DateFormat.defaultFormat(item.completion_time));
					row.setValue(5, item.assembly_code);
					row.setValue(6, item.assembly_name);
					row.setValue(7, item.complition_quantity + "");
					row.setValue(8, item.remaining_quantity + "");
					row.setValue(9, item.remark);
					row.setValue(10, item.transaction_quantity + "");
					row.setValue(11, DateFormat.defaultFormat(item.create_time));
					row.setValue(12, item.create_by);

					rows.add(row);
				}
				mGrid.insertRows(rows);
			}
		} catch (Exception e) {
			ToastHelper.getInstance(CompletionReturnQueryActivity.this).show(e.getMessage());
		}
	}
}
