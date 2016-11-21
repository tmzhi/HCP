package com.hcp.biz.query.activity;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hcp.common.DateFormat;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.common.view.DateTimePickerDialog;
import com.hcp.wo.R;

public class BaseTransactionQueryActivity extends ScannerActivity{
	
	protected EditText mEdtWipName;
	protected EditText mEdtComponent;
	
	protected TextView mTvStartTime;
	protected TextView mTvEndTime;
	
	protected Button mBtnQuery;
	protected Button mBtnStartTime;
	protected Button mBtnEndTime;
	
	protected Date mStartTime;
	protected Date mEndTime;
	
	protected LinearLayout mGridContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_biz_query);
		
		mEndTime = new Date(System.currentTimeMillis());
		
		mEdtWipName = (EditText) findViewById(R.id.edt_biz_query_wip_name);
		mEdtComponent = (EditText) findViewById(R.id.edt_biz_query_compnont);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mEndTime);
		calendar.add(Calendar.HOUR_OF_DAY, -12);
		mStartTime = calendar.getTime();
		
		mTvStartTime = (TextView) findViewById(R.id.tv_biz_query_start_time);
		setDateTime(mTvStartTime, mStartTime);
		
		mTvEndTime = (TextView) findViewById(R.id.tv_biz_query_end_time);
		setDateTime(mTvEndTime, mEndTime);
		
		mBtnQuery = (Button) findViewById(R.id.btn_biz_query_query);
		mBtnQuery.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				query();
			}
		});
		
		mBtnStartTime = (Button) findViewById(R.id.btn_biz_query_start_time);
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
		
		mBtnEndTime = (Button) findViewById(R.id.btn_biz_query_end_time);
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
		
		mGridContainer = (LinearLayout) findViewById(R.id.lin_biz_query_container);
	}
	
	protected void query(){
		
	}
	
	private void setDateTime(TextView input, Date date){
		input.setText(DateFormat.defaultFormat2(date));
	}
	
	@Override
	protected void decodeCallback(String barcode) {
		if(mEdtComponent.hasFocus()){
			mEdtComponent.setText(barcode);
		}else if(mEdtWipName.hasFocus()){
			mEdtWipName.setText(barcode);
			mEdtComponent.requestFocus();
		}
	}
}
