package com.hcp.dao;

import java.util.ArrayList;
import java.util.List;

import com.hcp.biz.entities.CompletionInfo;
import android.content.Context;
import android.text.TextUtils;

public class CompletionInfoDao extends BaseDao{
	
	private static CompletionInfoDao mInstance;
	
	private static Object mLocker = new Object();

	protected CompletionInfoDao(Context context) {
		super(context);
	}
	
	public static CompletionInfoDao getInstance(Context context){
		
		if(mInstance == null){
			synchronized (mLocker) {
				if(mInstance == null){
					mInstance = new CompletionInfoDao(context);
				}
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
		
		public List<CompletionInfo> getCompletionInfos() throws Exception{
			
			String[] whereArgs = null;
			String whereClause = null;
			if(mWhereClause != null){
				whereArgs = mWhereClause.whereArgs.toArray(new String[mWhereClause.whereArgs.size()]);
				whereClause = mWhereClause.whereClause.toString();
			}
			
			return mBizDBHelper.queryList(CompletionInfo.class, null, whereClause, whereArgs, null, mHaving, mOrderBy);
		}
	}
	
	public class Insert{
		public long insertCompletion(CompletionInfo trans) throws Exception{
			return mBizDBHelper.insert(CompletionInfo.class, trans);
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
