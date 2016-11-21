package com.seuic.listgrid;

import java.util.List;

import com.seuic.listgrid.ListGridStyle.HeaderStyle;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListGridHeader extends LinearLayout {

	private Context mContext;
	public ListGridHeader(Context context , List<GridColumn> columns){
		super(context);

		mContext = context;
		for(GridColumn column : columns){
			TextView tv_column = new TextView(mContext);
			tv_column.setText(column.getName());
			tv_column.setWidth(column.getWidth());
			tv_column.setHeight(LayoutParams.WRAP_CONTENT);
			tv_column.setBackgroundColor(HeaderStyle.BACKGROUND_COLOR);
			tv_column.setTextColor(Color.WHITE);
			addView(tv_column);
		}
	}
	
	public ListGridHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
}
