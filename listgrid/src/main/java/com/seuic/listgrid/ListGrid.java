package com.seuic.listgrid;

import java.util.ArrayList;
import java.util.List;

import com.seuic.listgrid.DataTable.DataRow;
import com.seuic.listgrid.ListGridStyle.HeaderStyle;
import com.seuic.listgrid.ListGridStyle.ItemStyle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ListGrid extends LinearLayout {
	
	private List<GridColumn> mColumns;
	private ListView mListView;
	private Context mContext;
	private DataAdapter mAdapter;
	private ColumnType mColumnType = ColumnType.WIDTH;
	private Resources mResources;
	private int mCellTextSize = ItemStyle.TEXT_SIZE;
	private LinearLayout mHeader;
	private OnGridItemLongClickListener mLongClickListener;
	private OnGridItemgClickListener mOnItemClickListener;
	private List<ListRow> mRows;
	private boolean mFullRowSelect = true;
	
	public ListGrid(Context context,List<GridColumn> columns){
		super(context);
		
		mColumns = columns;
		mContext = context;
		mResources = context.getResources();
		
		initView();
		
	}
	
	public ListGrid(Context context,List<GridColumn> columns ,ColumnType columnType){
		super(context);
		
		mColumns = columns;
		mContext = context;
		mColumnType = columnType;
		mResources = context.getResources();
		
		initView();
	}
	
	private void initView(){

		setOrientation(VERTICAL);
		setBackground(mResources.getDrawable(R.drawable.listgrid_style));
		
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(VERTICAL);
		layout.addView(initHeader());
		layout.addView(initData());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		
		if(mColumnType == ColumnType.WIDTH){
			HorizontalScrollView horScroll = new HorizontalScrollView(mContext);
			horScroll.addView(layout);
			horScroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			addView(horScroll);
		}
		else{
			LinearLayout layoutContent = new LinearLayout(mContext);
			layoutContent.addView(layout);
			layoutContent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			addView(layoutContent);
		}
	}
	
	private LinearLayout initHeader(){
		mHeader = new LinearLayout(mContext);
		if(mColumnType == ColumnType.WEIGHT){
			mHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		}else{
			mHeader.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		}
		//mHeader.setBackgroundColor(HeaderStyle.BACKGROUND_COLOR);
		mHeader.setOrientation(HORIZONTAL);
		mHeader.setBackground(mResources.getDrawable(R.drawable.listgrid_header_style));
		mHeader.addView(getSpliter());
		for(GridColumn column : mColumns){
			TextView tv = new TextView(mContext);
			tv.setText(column.getName());
			if(mColumnType == ColumnType.WEIGHT){
				LayoutParams lp = new LayoutParams(0 , LayoutParams.WRAP_CONTENT , column.getWeight());
				tv.setLayoutParams(lp);
			}
			else{
				tv.setWidth(column.getWidth());
			}
			tv.setTextColor(HeaderStyle.TEXT_COLOR);
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(HeaderStyle.TEXT_SIZE);
			tv.setBackgroundColor(Color.TRANSPARENT);
			tv.setPadding(0, 10, 0, 10);
			tv.setVisibility(column.getVisible());
			mHeader.addView(tv);
			if(mColumns.iterator().hasNext()){
				mHeader.addView(getSpliter());
			}
		}
		return mHeader;
	}
	
	private LinearLayout initData(){
		
		mRows = new ArrayList<ListRow>();
		
		mAdapter = new DataAdapter();
		
		mListView = new ListView(mContext);
		mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		mListView.setOnItemClickListener(new GridItemClickListener());
		mListView.setOnItemSelectedListener(new GridItemSelectedListener());
		mListView.setOnItemLongClickListener(new GridItemLongClickListener());
		mListView.setDivider(mResources.getDrawable(getResourceId(mContext, "drawable", "color_black")));
		mListView.setClickable(false);
		mListView.setDividerHeight(1);
		mListView.setAdapter(mAdapter);

		LinearLayout layout = new LinearLayout(mContext);
		layout.addView(mListView);
		layout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
		return layout;
	}

	public ListGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void refresh(){
		addView(mHeader);
	}
	
	public void removeRow(int index){
		
		if(mCurID >= index){
			mCurID --;
		}
		mRows.remove(index);
		
		mAdapter.notifyDataSetChanged();
	}
	
	public void clearRows(){
		mRows.clear();
		mCurID = -1;
		mCurView = null;
		mAdapter.notifyDataSetChanged();
	}
	
	public void clearSelectState(){
		mCurID = -1;
		
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void setDataSource(DataTable dt){
		mCurView = null;
		mCurID = -1;
		
		mRows = new ArrayList<ListRow>();
		List<DataRow> dataRows = dt.getAllRows();
		int dtColSize = dt.getColumnSize();
		for(DataRow dr : dataRows){
			ListRow row = new ListRow(mColumns);
			for(int i = 0 , length = mColumns.size() ; i < length ; i++){
				if(i == dtColSize){
					break;
				}
				row.setValue(i, dr.get(i));
			}
			mRows.add(row);
		}
		
		mAdapter = new DataAdapter();
		mListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
	
	public void insertRow(ListRow row){
		mRows.add(row);
		mAdapter.notifyDataSetChanged();
	}
	
	public void insertRows(List<ListRow> rows){
		for(ListRow row : rows){
			mRows.add(row);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	public void insertRow(int index , ListRow row){
		mRows.add(index , row);
		mAdapter.notifyDataSetChanged();
	}
	
	public List<GridColumn> getColumns(){
		return mColumns;
	}
	
	public List<ListRow> getAllRows(){
		return mRows;
	}
	
	public ListRow getRow(int index){
		return mRows.get(index);
	}
	
	public int getRowsCount(){
		return mRows.size();
	}
	
	public ListRow getSelectRow(){
		if(mCurID == -1){
			return null;
		}
		return mRows.get(mCurID);
	}
	
	public int getSelectIndex(){
		return mCurID;
	}
	
	public void setCellTextSize(int size){
		mCellTextSize = size;
	}
	
	public void setOnItemClickListener(OnGridItemgClickListener listener){
		mOnItemClickListener = listener;
	}
	
	public void setOnItemLongClickListener(OnGridItemLongClickListener listener){
		mLongClickListener = listener;
	}
	
	public void setFullRowSelect(boolean fullRow){
		mFullRowSelect = fullRow;
	}
	
	public void setHeaderTextSize(int size){
		for(int i = 0 , count = mHeader.getChildCount() ; i < count ; i++){
			View view = mHeader.getChildAt(i);
			if(view instanceof TextView){
				((TextView)view).setTextSize(size);
			}
		}
	}
	
	public void setHeaderShown(boolean shown){
		if(shown){
			mHeader.setVisibility(View.VISIBLE);
		}else{
			mHeader.setVisibility(View.GONE);
		}
	}
	
	public ListRow newRow(){
		ListRow item = new ListRow(mColumns);
		return item;
	}
	
	public List<ListRow> newRows(int size){
		List<ListRow> rows = new ArrayList<ListRow>();
		for(int i = 0; i < size; i++){
			rows.add(newRow());
		}
		return rows;
	}
	
	public class ListRow {
		List<GridColumn> mColumns;
		List<Object> mValues;
		private List<Integer> mTextColors;
		private int backgroundColor = ItemStyle.NORMAL_BACKGROUND_COLOR;
		private boolean selectedEnabled = true;
		
		private ListRow(List<GridColumn> columns){
			mColumns = columns;
			mValues = new ArrayList<Object>();
			mTextColors = new ArrayList<Integer>();
			for(int i = 0, size = columns.size();i< size ; i++){
				mValues.add("");
				mTextColors.add(mColumns.get(i).getTextColor());
			}
		}
		
		public void addColumn(GridColumn column){
			mColumns.add(column);
			mTextColors.add(column.getTextColor());
			mValues.add("");
		}
		
		public int getColumnsSize(){
			return mColumns.size();
		}
		
		public Object getValue(int index){
			return mValues.get(index);
		}
		
		public void setValue(int index , Object value){
			int size = mValues.size();
			if(size == 0){
				return;
			}
			mValues.set(index, value);
			
			if(mRows.contains(this)){
				mAdapter.notifyDataSetChanged();
			}
		}
		
		public void setBackgroundColor(int color){
			this.backgroundColor = color;
			
			if(mRows.contains(this)){
				mAdapter.notifyDataSetChanged();
			}
		}
		
		public int getBackgroundColor(){
			return this.backgroundColor;
		}
		
		public void setCellTextColor(int cellIndex, int color){
			mTextColors.set(cellIndex, color);
		}
		
		public int getCellTextColor(int cellIndex){
			return mTextColors.get(cellIndex);
		}
		
		public void setSelectedEnabled(boolean enabled){
			this.selectedEnabled = enabled;
		}
		
		public boolean isSelectedEnabled(){
			return this.selectedEnabled;
		}
	}
	
	class DataAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mRows.size();
		}

		@Override
		public Object getItem(int position) {
			return mRows.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				LinearLayout layout = new LinearLayout(mContext);
				layout.setOrientation(HORIZONTAL);
				layout.addView(getSpliter());
				
				for(int i = 0,size = mColumns.size(); i <size ;i++ ){
					View cell = null;
					
					if(mColumns.get(i).getCellViewLayout() != -1){
						cell = LayoutInflater.from(mContext).inflate(mColumns.get(i).getCellViewLayout(), null);
					}

					LayoutParams lp = null;
					if(mColumnType == ColumnType.WEIGHT){
						lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT , mColumns.get(i).getWeight());
						lp.gravity = Gravity.CENTER_VERTICAL;
					}
					else{
						lp = new LayoutParams(mColumns.get(i).getWidth(), LayoutParams.WRAP_CONTENT);
						lp.gravity = Gravity.CENTER_VERTICAL;
					}
					
					if(cell == null){
						TextView tv = new TextView(mContext);
						tv.setVisibility(mColumns.get(i).getVisible());
						tv.setGravity(Gravity.CENTER);
						tv.setTextSize(mCellTextSize);
						tv.setPadding(0, 5, 0, 5);
						tv.setBackgroundColor(Color.TRANSPARENT);
						
						cell = tv;
					}
					
					cell.setLayoutParams(lp);

					layout.addView(cell);
					holder.addView(cell);
					if(i != size - 1){
						layout.addView(getSpliter());
					}
				}
				layout.addView(getSpliter());
				convertView = layout;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			ListRow row = mRows.get(position);
			for(int i = 0,size = mColumns.size(); i <size ;i++ ){
				holder.setText(row.getValue(i), i);
				holder.setTextColor(row.getCellTextColor(i), i);
				
				GridColumn column = mColumns.get(i);
				if(column.getHandleView() != null){
					column.getHandleView().handle(holder.getViewList().get(i), row.getValue(i));
				}
			}
			
			if(position != mCurID){
				convertView.setBackgroundColor(mRows.get(position).getBackgroundColor());
			}else{
				mCurView = convertView;
				if(mFullRowSelect){
					convertView.setBackgroundColor(ItemStyle.SELECTED_BACKGROUND_COLOR);
				}
			}
			return convertView;
		}
	}
	
	private View getSpliter(){

		View spliter = new View(mContext);
		spliter.setLayoutParams(new LayoutParams(1,LayoutParams.MATCH_PARENT));
		spliter.setBackgroundColor(Color.BLACK);
		return spliter;
	}
	
	private View mCurView = null;
	private int mCurID = -1;
	
	private void setSelected(View convertView , int position){

		if(position == mCurID){
			return;
		}
		
		if(mCurID != -1){
			mCurView.setBackgroundColor(mRows.get(mCurID).getBackgroundColor());
		}

		//No selected row 
		if(convertView == null && position == -1){
			mCurID = -1;
			mCurView = null;
			return;
		}
		
		if(mFullRowSelect){
			convertView.setBackgroundColor(ItemStyle.SELECTED_BACKGROUND_COLOR);
		}
		mCurView = convertView;
		mCurID = position;
	}
	
	private class GridItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View convertView, int position,
				long id) {
			
			if(mRows.get(position).isSelectedEnabled()){
				setSelected(convertView, position);
			
				if(mOnItemClickListener != null){
					mOnItemClickListener.onClick(convertView, position, id);
				}
			}
		}
	}
	
	private class GridItemSelectedListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> arg0, View convertView, int position,
				long arg3) {
			//setSelected(convertView,position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			//setSelected(null, -1);
		}
	}
	
	private class GridItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int index, long id) {
			if(mLongClickListener == null){
				return false;
			}
			
			mLongClickListener.onLongClick(view , index , id);
			
			return false;
		}
	}
	
	public interface OnGridItemLongClickListener{
		void onLongClick(View view, int index, long id);
	}
	
	public interface OnGridItemgClickListener{
		void onClick(View view, int index, long id);
	}
	
	public enum ColumnType{
		WEIGHT,
		WIDTH
	}
	
	private int getResourceId(Context context, String defType, String name){
		return context.getResources().getIdentifier(name, defType, context.getPackageName());
	}
}
