package com.hcp.biz.query.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.hcp.biz.entities.OperationTransaction;
import com.hcp.common.DateFormat;
import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.common.view.DateTimePickerDialog;
import com.hcp.dao.OperationTransactionDao;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class OperationTransactionQueryActivity extends ScannerActivity{

	private ListGrid mGrid;

	private OperationTransactionDao mDao;

	private EditText mEdtWipName;
	private Spinner mSpnTransactionType;
	private TextView mTvStartTime;
	private TextView mTvEndTime;
	private Button mBtnQuery;
	private Button mBtnStartTime;
	private Button mBtnEndTime;

	private Date mStartTime;
	private Date mEndTime;

	private LinearLayout mGridContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_biz_operation_query);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		mEdtWipName = (EditText) findViewById(R.id.edt_bo_query_wip_name);

		mSpnTransactionType = (Spinner) findViewById(R.id.spn_bo_query_transaction_type);
		String[] transactionTypes = getResources().getStringArray(R.array.wpt_transaction_types);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, transactionTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnTransactionType.setAdapter(adapter);

		mEndTime = new Date(System.currentTimeMillis());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mEndTime);
		calendar.add(Calendar.HOUR_OF_DAY, -12);
		mStartTime = calendar.getTime();

		mTvStartTime = (TextView) findViewById(R.id.tv_bo_query_start_time);
		setDateTime(mTvStartTime, mStartTime);

		mTvEndTime = (TextView) findViewById(R.id.tv_bo_query_end_time);
		setDateTime(mTvEndTime, mEndTime);

		mBtnQuery = (Button) findViewById(R.id.btn_bo_query_query);
		mBtnQuery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				query();
			}
		});

		mBtnStartTime = (Button) findViewById(R.id.btn_bo_query_start_time);
		mBtnStartTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DateTimePickerDialog dialog = new DateTimePickerDialog(mInstance, mStartTime);
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

		mBtnEndTime = (Button) findViewById(R.id.btn_bo_query_end_time);
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

		mGridContainer = (LinearLayout) findViewById(R.id.lin_bo_query_container);
		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("工单号", (int) (80 * density)));
		columns.add(new GridColumn("工单类型", (int) (80 * density)));
		columns.add(new GridColumn("开始时间", (int) (120 * density)));
		columns.add(new GridColumn("完成时间", (int) (120 * density)));
		columns.add(new GridColumn("处理类型", (int) (80 * density)));
		columns.add(new GridColumn("初始序号", (int) (80 * density)));
		columns.add(new GridColumn("初始状态", (int) (80 * density)));
		columns.add(new GridColumn("目标序号", (int) (80 * density)));
		columns.add(new GridColumn("目标状态", (int) (80 * density)));
		columns.add(new GridColumn("移动数量", (int) (100 * density)));
		columns.add(new GridColumn("创建时间", (int) (120 * density)));
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

		mDao = OperationTransactionDao.getInstance(this);
	}

	private void setDateTime(TextView input, Date date){
		input.setText(DateFormat.defaultFormat2(date));
	}

	private void query() {

		mGrid.clearRows();

		try {
			String startTime = mTvStartTime.getText().toString();
			String endTime = mTvEndTime.getText().toString() + ":59";
			String wipName = mEdtWipName.getText().toString();
			String transactionType = mSpnTransactionType.getSelectedItem().toString();

			List<OperationTransaction> list = mDao.onQuery()
					.whereClause()
					.addTransactionType(transactionType)
					.addWipName(wipName)
					.addStartTime(startTime)
					.addEndTime(endTime)
					.confirm()
					.orderByTimeDesc()
					.getTransactionInts();

			if(list != null && list.size()>0){

				List<ListRow> rows = new ArrayList<ListGrid.ListRow>();

				for(OperationTransaction item : list){
					ListRow row = mGrid.newRow();
					row.setValue(0, item.wip_entity_name);
					row.setValue(1, item.class_code);
					row.setValue(2, DateFormat.defaultFormat(item.start_time));
					row.setValue(3, DateFormat.defaultFormat(item.completion_time));
					row.setValue(4, item.transaction_type);
					row.setValue(5, item.serial_from + "");
					row.setValue(6, item.step_from);
					row.setValue(7, item.serial_to + "");
					row.setValue(8, item.step_to);
					row.setValue(9, item.transaction_quantity + "");
					row.setValue(10, DateFormat.defaultFormat(item.create_time));
					row.setValue(11, item.create_by + "");

					rows.add(row);
				}
				mGrid.insertRows(rows);
			}
		} catch (Exception e) {
			ToastHelper.getInstance(OperationTransactionQueryActivity.this).show(e.getMessage());
		}
	}
}
