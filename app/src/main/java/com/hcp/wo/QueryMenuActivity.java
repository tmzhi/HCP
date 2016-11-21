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
import com.hcp.query.activity.ComponentBackupQueryActivity;
import com.hcp.query.activity.ComponentQueryActivity;
import com.hcp.query.activity.OperationQueryActivity;
import com.hcp.query.activity.StandingCorpQueryActivity;
import com.hcp.query.activity.WipRecordQueryActivity;
import com.hcp.query.activity.WoQueryActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryMenuActivity extends BaseActivity{
	
	private final int QUERY_COMPONENT = 100;
	private final int QUERY_REMAINING = 101;
	private final int QUERY_WO = 102;
	private final int QUERY_COMPONENT_BACKUP = 103;
	private final int QUERY_WIP_TRANSACTION = 104;
	private final int QUERY_OPERATION = 105;
	
	private GridView mGvContainer;
	
	private OperationsAdapter mAdapter = new OperationsAdapter();
	
	private List<String> mOperationTags = new ArrayList<String>();
	private List<String> mQueryTags = new ArrayList<String>();
	private Map<String, Integer> mOperationsBinder = new HashMap<String, Integer>();
	private QueryMenuActivity mInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mInstance = this;
		
		init();
		
		mGvContainer = (GridView) findViewById(R.id.gv_main_container);
		mGvContainer.setAdapter(mAdapter);
	}
	
	private void init(){

		
		mQueryTags.add(getResources().getString(R.string.main_component_query));
		mQueryTags.add(getResources().getString(R.string.main_standing_corp_query));
		mQueryTags.add(getResources().getString(R.string.main_wo_query));
		mQueryTags.add(getResources().getString(R.string.main_component_backup_query));
		mQueryTags.add(getResources().getString(R.string.main_wip_transaction_query));
		mQueryTags.add(getResources().getString(R.string.main_operation_query));

		mOperationTags.addAll(mQueryTags);

		mOperationsBinder.put(mQueryTags.get(0), QUERY_COMPONENT);
		mOperationsBinder.put(mQueryTags.get(1), QUERY_REMAINING);
		mOperationsBinder.put(mQueryTags.get(2), QUERY_WO);
		mOperationsBinder.put(mQueryTags.get(3), QUERY_COMPONENT_BACKUP);
		mOperationsBinder.put(mQueryTags.get(4), QUERY_WIP_TRANSACTION);
		mOperationsBinder.put(mQueryTags.get(5), QUERY_OPERATION);
	}

	private void toQuery() {
		Intent intent = new Intent(this, WoQueryActivity.class);
		startActivity(intent);
	}

	private void toComponentQuery() {
		Intent intent = new Intent(this, ComponentQueryActivity.class);
		startActivity(intent);
	}

	private void toStandingCorpQuery() {
		Intent intent = new Intent(this, StandingCorpQueryActivity.class);
		startActivity(intent);
	}
	
	private void toComponentBackupQuery() {
		Intent intent = new Intent(this, ComponentBackupQueryActivity.class);
		startActivity(intent);
	}
	
	private void toWipTransactionQuery() {
		Intent intent = new Intent(this, WipRecordQueryActivity.class);
		startActivity(intent);
	}

	private void toOperationQuery() {
		Intent intent = new Intent(this, OperationQueryActivity.class);
		startActivity(intent);
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
			case QUERY_WO:
				toQuery();
				break;
			case QUERY_COMPONENT:
				toComponentQuery();
				break;
			case QUERY_REMAINING:
				toStandingCorpQuery();
				break;
			case QUERY_COMPONENT_BACKUP:
				toComponentBackupQuery();
				break;
			case QUERY_WIP_TRANSACTION:
				toWipTransactionQuery();
				break;
			case QUERY_OPERATION:
				toOperationQuery();
				break;
			}
		}
		
	}
}
