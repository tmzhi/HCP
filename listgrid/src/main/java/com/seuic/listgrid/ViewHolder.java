package com.seuic.listgrid;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.TextView;

public class ViewHolder {
	private List<View> mViewList;
	private int id;
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public ViewHolder(){
		mViewList = new ArrayList<View>();
	}
	
	public void setText(Object text,int position){
		if(mViewList.get(position) instanceof TextView){
			((TextView)mViewList.get(position)).setText(text + "");
		}
	}
	
	public void setTextColor(int color,int position){
		if(mViewList.get(position) instanceof TextView){
			((TextView)mViewList.get(position)).setTextColor(color);
		}
	}
	
	public void addView(View tv){
		mViewList.add(tv);
	}
	
	public List<View> getViewList(){
		return mViewList;
	}
}
