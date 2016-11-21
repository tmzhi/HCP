package com.seuic.listgrid;

import android.view.View;

public class GridColumn {

	private String name;
	private int width;
	private float weight = 1;
	private int textColor = ListGridStyle.ItemStyle.TEXT_COLOR;
	private int visible = View.VISIBLE;
	private View cellView;
	private OnHandleView onHandleView;
	private int layoutId = -1;
	
	public GridColumn(String name, int width){
		this.name = name;
		this.width = width;
	}
	
	public GridColumn(String name, float weight){
		this.name = name;
		this.weight = weight;
	}
	
	public String getName(){
		return name;
	}
	
	public int getWidth(){
		return width;
	}
	
	public float getWeight(){
		return weight;
	}
	
	public void setTextColor(int color){
		textColor = color;
	}
	
	public int getTextColor(){
		return textColor;
	}
	
	public void setVisible(int visible){
		this.visible = visible;
	}
	
	public void setCellViewLayout(int layoutId){
		this.layoutId = layoutId;
	}
	
	public int getCellViewLayout(){
		return this.layoutId;
	}
	
	public int getVisible(){
		return visible;
	}
	
	public void setCellView(View view){
		this.cellView = view;
	}
	
	public View getCellView(){
		return this.cellView;
	}
	
	public void setHandleView(OnHandleView handleView){
		onHandleView = handleView;
	}
	
	OnHandleView getHandleView(){
		return onHandleView;
	}
	
	public interface OnHandleView{
		void handle(View view, Object object);
	}
}
