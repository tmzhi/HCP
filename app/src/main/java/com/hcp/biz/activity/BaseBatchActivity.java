package com.hcp.biz.activity;

import com.hcp.common.activity.ScannerActivity;

import java.math.BigDecimal;

public class BaseBatchActivity extends ScannerActivity {
	public static final String REQUEST_ON_BATCH = "on_batch";
	public static final String REQUEST_WIP_NAME = "wip_name";
	public static final String REQUEST_COMPONENT_CODE = "component_code";
	public static final String REQUEST_STATE = "state";
	public static final String REQUEST_TRANSACTION_QUANTITY = "transaction_quantity";
	public static final String REQUEST_ID = "id";
	
	public static final String REQUEST_TRANSACTION_TYPE = "transaction_type";
	
	public static final int STATE_NEW = 1;
	public static final int STATE_MODIFIED = 2;
	public static final int STATE_SUBMITTED = 3;
	
	public static final int TYPE_TRANSACTION_ISSUE = 101;
	public static final int TYPE_TRANSACTION_RETURN = 102;
	
	protected class TransactionResult{
		
		public BigDecimal transaction_quantity;
		public int state;
	}
}
