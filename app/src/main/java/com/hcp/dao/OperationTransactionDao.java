package com.hcp.dao;

import java.util.ArrayList;
import java.util.List;

import com.hcp.biz.entities.OperationTransaction;

import android.content.Context;
import android.text.TextUtils;

public class OperationTransactionDao extends BaseDao{
	
	private static OperationTransactionDao mInstance;
	
	private static Object mLocker = new Object();

	protected OperationTransactionDao(Context context) {
		super(context);
	}
	
	public static OperationTransactionDao getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new OperationTransactionDao(context);
			}
		}
		return mInstance;
	}
	
	public Query onQuery(){
		return new Query();
	}
	
	public Insert onInsert(){
		return new Insert();
	}
	
	public class Query{
		
		private WhereClause<Query> mWhereClause;
		private String mOrderBy;
		private String mHaving;
		
		public WhereClause<Query> whereClause(){
			mWhereClause = new WhereClause<Query>(this);
			return mWhereClause;
		}
		
		public Query orderBy(String orderBy){
			mOrderBy = orderBy;
			return this;
		}
		
		public Query orderByTimeDesc(){
			mOrderBy = "create_time DESC";
			return this;
		}
		
		public Query having(String having){
			mHaving = having;
			return this;
		}
		
		public List<OperationTransaction> getTransactionInts() throws Exception{
			
			String[] whereArgs = null;
			String whereClause = null;
			if(mWhereClause != null){
				whereArgs = mWhereClause.whereArgs.toArray(new String[mWhereClause.whereArgs.size()]);
				whereClause = mWhereClause.whereClause.toString();
			}
			
			return mBizDBHelper.queryList(OperationTransaction.class, null, whereClause, whereArgs, null, mHaving, mOrderBy);
		}
	}
	
	public class Insert{
		public long insertOperationTransaction(OperationTransaction opTrans) throws Exception{
			return mBizDBHelper.insert(OperationTransaction.class, opTrans);
		}
	}
	
	public class WhereClause<T>{
		StringBuilder whereClause = new StringBuilder();
		List<String> whereArgs = new ArrayList<String>();
		
		T operObj;
		
		public WhereClause(T oper){
			whereClause.append(" 1=1 ");
			
			operObj = oper;
		}
		
		public WhereClause<T> addWipName(String wipName){
			if(!TextUtils.isEmpty(wipName)){
				whereClause.append("AND wip_entity_name=? ");
				whereArgs.add(wipName);
			}
			return this;
		}
		
		public WhereClause<T> addTransactionType(String transactionType){
			if(!TextUtils.isEmpty(transactionType)){
				whereClause.append("AND transaction_type=? ");
				whereArgs.add(transactionType);
			}
			return this;
		}
		
		public WhereClause<T> addStartTime(String startTime){
			if(!TextUtils.isEmpty(startTime)){
				whereClause.append("AND create_time>? ");
				whereArgs.add(startTime);
			}
			return this;
		}
		
		public WhereClause<T> addEndTime(String endTime){
			if(!TextUtils.isEmpty(endTime)){
				whereClause.append("AND create_time<? ");
				whereArgs.add(endTime);
			}
			return this;
		}
		
		public T confirm(){
			return operObj;
		} 
	}
}
