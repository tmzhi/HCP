package com.hcp.wo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.hcp.common.activity.BaseActivity;
import com.hcp.intraware.biz.activity.ISPAccountAliasIssueActivity;
import com.hcp.intraware.biz.activity.ISPAccountAliasReceiptActivity;
import com.hcp.intraware.biz.activity.ISPPurchaseReceiveActivity;
import com.hcp.intraware.biz.activity.ISPSubInventoryTransferActivity;
import com.hcp.intraware.biz.activity.ISPWipIssueActivity;
import com.hcp.intraware.biz.activity.ISPWipQueryActivity;
import com.hcp.intraware.biz.activity.ISPWipReturnActivity;
import com.hcp.intraware.biz.activity.OrganizationActivity;
import com.hcp.util.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntrawareMenuActivity extends BaseActivity{
	
	private final int GROUP_CHANGE = 0;
	private final int IN = 1;
	private final int OUT = 2;
	private final int ISSUE = 3;
	private final int RETURN = 4;
	private final int TRANSFER = 5;
	private final int PURCHASE = 6;
	private final int COMPARE = 7;
	
	private GridView mGvContainer;
	
	private OperationsAdapter mAdapter = new OperationsAdapter();
	
	private List<String> mOperationTags = new ArrayList<String>();
	private List<String> mQueryTags = new ArrayList<String>();
	private Map<String, Integer> mOperationsBinder = new HashMap<String, Integer>();
	private IntrawareMenuActivity mInstance;

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
		mQueryTags.add(getResources().getString(R.string.intraware_group_change));
		mQueryTags.add(getResources().getString(R.string.intraware_in));
		mQueryTags.add(getResources().getString(R.string.intraware_out));
		mQueryTags.add(getResources().getString(R.string.intraware_issue));
		mQueryTags.add(getResources().getString(R.string.intraware_return));
		mQueryTags.add(getResources().getString(R.string.intraware_move));
		mQueryTags.add(getResources().getString(R.string.intraware_aar));
		mQueryTags.add(getResources().getString(R.string.intraware_compare));
		mOperationTags.addAll(mQueryTags);

		mOperationsBinder.put(mQueryTags.get(0), GROUP_CHANGE);
		mOperationsBinder.put(mQueryTags.get(1), IN);
		mOperationsBinder.put(mQueryTags.get(2), OUT);
		mOperationsBinder.put(mQueryTags.get(3), ISSUE);
		mOperationsBinder.put(mQueryTags.get(4), RETURN);
		mOperationsBinder.put(mQueryTags.get(5), TRANSFER);
		mOperationsBinder.put(mQueryTags.get(6), PURCHASE);
		mOperationsBinder.put(mQueryTags.get(7), COMPARE);
	}
	
	private void toOrganization() {
		Intent intent = new Intent(this, OrganizationActivity.class);
		startActivity(intent);
	}

	private void toISPAccountAliasReceipt() {
		Intent intent = new Intent(this, ISPAccountAliasReceiptActivity.class);
		startActivity(intent);
	}

	private void toISPAccountAliasIssue() {
		Intent intent = new Intent(this, ISPAccountAliasIssueActivity.class);
		startActivity(intent);
	}

	private void toISPWipIssue() {
		Intent intent = new Intent(this, ISPWipIssueActivity.class);
		startActivity(intent);
	}

	private void toISPWipReturn() {
		Intent intent = new Intent(this, ISPWipReturnActivity.class);
		startActivity(intent);
	}

	private void toISPSubInventoryTransfer() {
		Intent intent = new Intent(this, ISPSubInventoryTransferActivity.class);
		startActivity(intent);
	}

	private void toISPPurchaseReceive() {
		Intent intent = new Intent(this, ISPPurchaseReceiveActivity.class);
		startActivity(intent);
	}

	private void toISPCompare() {
		Intent intent = new Intent(this, ISPWipQueryActivity.class);
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
				btnOper.setTextSize(14);
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

			if(tagID != GROUP_CHANGE && AppConfig.getInstance(mInstance).getOrganizationId() < 0){
				new AlertDialog.Builder(mInstance)
						.setTitle("提示")
						.setMessage("未绑定组织,请进入【组织切换】绑定当前组织!")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								toOrganization();
							}
						})
						.setNegativeButton("取消", null)
						.create()
						.show();
				return;
			}

			switch (tagID) {
				case GROUP_CHANGE:
					toOrganization();
					break;
				case IN:
					toISPAccountAliasReceipt();
					break;
				case OUT:
					toISPAccountAliasIssue();
					break;
				case ISSUE:
					toISPWipIssue();
					break;
				case RETURN:
					toISPWipReturn();
					break;
				case TRANSFER:
					toISPSubInventoryTransfer();
					break;
				case PURCHASE:
					toISPPurchaseReceive();
					break;
				case COMPARE:
					toISPCompare();
					break;
			}
		}
		
	}
}
