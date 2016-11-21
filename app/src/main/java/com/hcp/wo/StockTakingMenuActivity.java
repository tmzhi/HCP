package com.hcp.wo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.hcp.common.activity.BaseActivity;
import com.hcp.stocktaking.biz.EnteringActivity;
import com.hcp.stocktaking.biz.GetSnapShotActivity;
import com.hcp.stocktaking.biz.InventoryCompareActivity;
import com.hcp.stocktaking.biz.ModifyActivity;
import com.hcp.stocktaking.biz.PalletActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockTakingMenuActivity extends BaseActivity{
	
	private final int DIFFERENT_IMPORT = 0;
	private final int COMPARE = 1;
	private final int ENTERING = 2;
	private final int SNAPSHOT = 3;
	private final int MODIFY = 4;
	private final int PALLET = 5;
	
	private GridView mGvContainer;
	
	private OperationsAdapter mAdapter = new OperationsAdapter();
	
	private List<String> mOperationTags = new ArrayList<String>();
	private List<String> mQueryTags = new ArrayList<String>();
	private Map<String, Integer> mOperationsBinder = new HashMap<String, Integer>();
	private StockTakingMenuActivity mInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intraware_menu);
		
		mInstance = this;
		
		init();
		
		mGvContainer = (GridView) findViewById(R.id.gv_main_container);
		mGvContainer.setAdapter(mAdapter);
	}
	
	private void init(){

		
		mQueryTags.add(getResources().getString(R.string.stock_taking_import));
		mQueryTags.add(getResources().getString(R.string.stock_taking_compare));
		mQueryTags.add(getResources().getString(R.string.stock_taking_entering));
		mQueryTags.add(getResources().getString(R.string.stock_taking_modify));
		mQueryTags.add(getResources().getString(R.string.stock_taking_snapshot));
		mQueryTags.add(getResources().getString(R.string.stock_taking_pallet));
		mOperationTags.addAll(mQueryTags);

		mOperationsBinder.put(mQueryTags.get(0), DIFFERENT_IMPORT);
		mOperationsBinder.put(mQueryTags.get(1), COMPARE);
		mOperationsBinder.put(mQueryTags.get(2), ENTERING);
		mOperationsBinder.put(mQueryTags.get(3), MODIFY);
		mOperationsBinder.put(mQueryTags.get(4), SNAPSHOT);
		mOperationsBinder.put(mQueryTags.get(5), PALLET);
	}
	
	private class OperationsAdapter extends BaseAdapter{
		
		private OperationClickListener mClickListener = new OperationClickListener();
		
		@Override
		public int getCount() {
			return mOperationTags.size();
		}

		@Override
		public Object getItem(int position) {
			return mOperationTags.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("ResourceType")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Button btnOper = null;
			
			if(convertView == null){
				btnOper = new Button(mInstance);
				btnOper.setTextSize(16);
				btnOper.setOnClickListener(mClickListener);
				btnOper.setBackgroundResource(R.drawable.btn_main_biz_style);
				btnOper.setTextColor(mInstance.getResources().getColorStateList(R.drawable.btn_main_biz_textcolor_style));
				
				convertView = btnOper;
			}else{
				btnOper = (Button)convertView;
			}
			
			String operName = mOperationTags.get(position);
			btnOper.setText(operName);
			btnOper.setTag(mOperationsBinder.get(operName));
			
			return convertView;
		}
	}
	
	private class OperationClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int tagID = (Integer) v.getTag();
			switch (tagID) {
				case ENTERING:
					toEntering();
					break;
				case MODIFY:
					toModify();
					break;
				case SNAPSHOT:
					toSnapshot();
					break;
				case COMPARE:
					toInventoryCompare();
					break;
				case PALLET:
					toPallet();
					break;
			}
		}

		private void toEntering(){
			startActivity(new Intent(mInstance, EnteringActivity.class));
		}

		private void toModify(){
			startActivity(new Intent(mInstance, ModifyActivity.class));
		}

		private void toSnapshot(){
			startActivity(new Intent(mInstance, GetSnapShotActivity.class));
		}

		private void toInventoryCompare(){
			startActivity(new Intent(mInstance, InventoryCompareActivity.class));
		}

		private void toPallet(){
			startActivity(new Intent(mInstance, PalletActivity.class));
		}
	}

}
