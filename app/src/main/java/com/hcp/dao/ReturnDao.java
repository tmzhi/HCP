package com.hcp.dao;

import java.util.ArrayList;
import java.util.List;

import com.hcp.biz.entities.ReturnInfo;

import android.content.Context;
import android.text.TextUtils;

public class ReturnDao extends BaseDao{
	
	private static ReturnDao mInstance;
	
	private static Object mLocker = new Object();
	
	private ReturnDao(Context context){
		super(context);
	}
	
	public static ReturnDao getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new ReturnDao(context);
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
		
		public List<ReturnInfo> getReturns() throws Exception{
			
			String[] whereArgs = null;
			String whereClause = null;
			if(mWhereClause != null){
				whereArgs = mWhereClause.whereArgs.toArray(new String[mWhereClause.whereArgs.size()]);
				whereClause = mWhereClause.whereClause.toString();
			}
			
			return mBizDBHelper.queryList(ReturnInfo.class, null, whereClause, whereArgs, null, mHaving, mOrderBy);
		}
	}
	
	public class Insert{
		public long insertIssue(ReturnInfo trans) throws Exception{
			return mBizDBHelper.insert(ReturnInfo.class, trans);
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
		
		public WhereClause<T> addComponent(String component){
			if(!TextUtils.isEmpty(component)){
				whereClause.append("AND component_code=? ");
				whereArgs.add(component);
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
