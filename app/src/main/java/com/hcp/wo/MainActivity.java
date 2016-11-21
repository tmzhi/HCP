package com.hcp.wo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.hcp.biz.activity.BatchTransactionActivity;
import com.hcp.biz.activity.CompletionActivity;
import com.hcp.biz.activity.CompletionReturnActivity;
import com.hcp.biz.activity.MaterialIssueActivity;
import com.hcp.biz.activity.MaterialReturnActivity;
import com.hcp.biz.activity.MaterialTransferActivity;
import com.hcp.biz.activity.SpecificIssueActivity;
import com.hcp.biz.activity.SubInventoryTransactionActivity;
import com.hcp.biz.activity.SubInventoryTransactionReturnActivity;
import com.hcp.biz.activity.VirturalCompletionActivity;
import com.hcp.biz.activity.WorkingProcedureTransferActivity;
import com.hcp.common.activity.BaseActivity;
import com.hcp.dao.AuthorityDao;
import com.hcp.entities.Authority;
import com.hcp.util.AppCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity{

	private final int TRANSACTION = 1;
	private final int ISSUE = 2;
	private final int RETURN = 3;
	private final int COMPLETION = 4;
	private final int PROCEDURE = 5;
	private final int COMPLETION_RETURN = 6;
	private final int SUB_INVENTORY_TRANSACTION = 7;
	private final int SUB_INVENTORY_TRANSACTION_RETURN = 13;
	private final int VIRTUAL_COMPLETION = 8;
	private final int BATCH_TRANSACTION_ISSUE = 9;
	private final int BATCH_TRANSACTION_RETURN = 10;
	private final int SPECIFIC_ISSUE = 11;
	private final int SUB_INVENTORY_TRANSACTION_BARCODE = 12;
	
	private GridView mGvContainer;
	
	private OperationsAdapter mAdapter = new OperationsAdapter();
	
	private List<String> mOperationTags = new ArrayList<String>();
	private List<String> mQueryTags = new ArrayList<String>();
	private Map<String, Integer> mOperationsBinder = new HashMap<String, Integer>();
	private MainActivity mInstance;

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
		
		String[] operationNames = getResources().getStringArray(R.array.operations);
		
		String loginUser = AppCache.getInstance().getLoginUser();
		try {
			Authority authority = AuthorityDao.getInstance(this).getAuthority(loginUser);
			if(authority != null && !TextUtils.isEmpty(authority.authorities)){
				mOperationTags.addAll(Arrays.asList(authority.authorities.split(AuthorityDao.SEPARATOR)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mOperationTags.addAll(mQueryTags);

		mOperationsBinder.put(operationNames[0], TRANSACTION);
		mOperationsBinder.put(operationNames[1], COMPLETION);
		mOperationsBinder.put(operationNames[2], ISSUE);
		mOperationsBinder.put(operationNames[3], COMPLETION_RETURN);
		mOperationsBinder.put(operationNames[4], RETURN);
		mOperationsBinder.put(operationNames[5], PROCEDURE);
		mOperationsBinder.put(operationNames[6], SUB_INVENTORY_TRANSACTION);
		mOperationsBinder.put(operationNames[7], SUB_INVENTORY_TRANSACTION_RETURN);
		mOperationsBinder.put(operationNames[8], SUB_INVENTORY_TRANSACTION_BARCODE);
		mOperationsBinder.put(operationNames[9], VIRTUAL_COMPLETION);
		mOperationsBinder.put(operationNames[10], BATCH_TRANSACTION_ISSUE);
		mOperationsBinder.put(operationNames[11], BATCH_TRANSACTION_RETURN);
		mOperationsBinder.put(operationNames[12], SPECIFIC_ISSUE);
	}

	private void toMT() {
		Intent intent = new Intent(this, MaterialTransferActivity.class);
		startActivity(intent);
	}

	private void toMI() {
		Intent intent = new Intent(this, MaterialIssueActivity.class);
		startActivity(intent);
	}

	private void toMR() {
		Intent intent = new Intent(this, MaterialReturnActivity.class);
		startActivity(intent);
	}

	private void toCompletion() {
		Intent intent = new Intent(this, CompletionActivity.class);
		startActivity(intent);
	}
	
	private void toCompletionReturn() {
		Intent intent = new Intent(this, CompletionReturnActivity.class);
		startActivity(intent);
	}

	private void toWPT() {
		Intent intent = new Intent(this, WorkingProcedureTransferActivity.class);
		startActivity(intent);
	}
	
	private void toSBT() {
		Intent intent = new Intent(this, SubInventoryTransactionActivity.class);
		startActivity(intent);
	}

	private void toSBTBarcode() {
		Intent intent = new Intent(this, SubInventoryTransactionActivity.class);
		intent.putExtra("type", "barcode");
		startActivity(intent);
	}
	
	private void toVirtualCompletion() {
		Intent intent = new Intent(this, VirturalCompletionActivity.class);
		startActivity(intent);
	}
	
	private void toBatchTransactionIssue() {
		Intent intent = new Intent(this, BatchTransactionActivity.class);
		intent.putExtra(BatchTransactionActivity.REQUEST_TRANSACTION_TYPE, BatchTransactionActivity.TYPE_TRANSACTION_ISSUE);
		startActivity(intent);
	}
	
	private void toBatchTransactionReturn() {
		Intent intent = new Intent(this, BatchTransactionActivity.class);
		intent.putExtra(BatchTransactionActivity.REQUEST_TRANSACTION_TYPE, BatchTransactionActivity.TYPE_TRANSACTION_RETURN);
		startActivity(intent);
	}
	
	private void toSpecificIssue() {
		startActivity(new Intent(this, SpecificIssueActivity.class));
	}

	private void toSubinventoryTransactionReturn() {
		startActivity(new Intent(this, SubInventoryTransactionReturnActivity.class));
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
			case TRANSACTION:
				toMT();
				break;
			case ISSUE:
				toMI();
				break;
			case RETURN:
				toMR();
				break;
			case COMPLETION:
				toCompletion();
				break;
			case PROCEDURE:
				toWPT();
				break;
			case COMPLETION_RETURN:
				toCompletionReturn();
				break;
			case SUB_INVENTORY_TRANSACTION:
				toSBT();
				break;
				case SUB_INVENTORY_TRANSACTION_BARCODE:
					toSBTBarcode();
					break;
			case VIRTUAL_COMPLETION:
				toVirtualCompletion();
				break;
			case BATCH_TRANSACTION_ISSUE:
				toBatchTransactionIssue();
				break;
			case BATCH_TRANSACTION_RETURN:
				toBatchTransactionReturn();
				break;
			case SPECIFIC_ISSUE:
				toSpecificIssue();
				break;
				case SUB_INVENTORY_TRANSACTION_RETURN:
					toSubinventoryTransactionReturn();
					break;
			}
		}
		
	}
}
