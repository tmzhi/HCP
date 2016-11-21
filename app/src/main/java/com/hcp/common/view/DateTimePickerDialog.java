package com.hcp.common.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.hcp.wo.R;

import java.util.Calendar;
import java.util.Date;

public class DateTimePickerDialog{

	private Activity mActivity;

	private Date mInitDate;

	private OnCallback mCallback;

	public DateTimePickerDialog(Activity activity, Date initDate){
		this.mActivity = activity;
		this.mInitDate = initDate;
	}

	public void show(){

		if(mInitDate == null){
			mInitDate = new Date(System.currentTimeMillis());
		}

		View view  = LayoutInflater.from(mActivity).inflate(R.layout.datetime_picker, null);

		final DatePicker date = (DatePicker) view.findViewById(R.id.datetime_picker_date);
		final TimePicker time = (TimePicker) view.findViewById(R.id.datetime_picker_time);
		time.setIs24HourView(true);

		init(date, time);

		AlertDialog dialog = new AlertDialog.Builder(mActivity)
				.setTitle("时间选择")
				.setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mCallback != null){
							Calendar calendar = Calendar.getInstance();
							calendar.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getCurrentHour(), time.getCurrentMinute());
							mCallback.getDateTime(calendar.getTime());
						}
					}
				})
				.create();
		dialog.show();
	}

	private void init(DatePicker date, TimePicker time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mInitDate);

		date.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
		time.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		time.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	public void setCallback(OnCallback callback){
		this.mCallback = callback;
	}

	public interface OnCallback{
		void getDateTime(Date date);
	}
}
