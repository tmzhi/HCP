package com.hcp.dao;

import com.hcp.db.BizDBHelper;

import android.content.Context;

public class BaseDao {
	
	protected BizDBHelper mBizDBHelper;
	
	protected BaseDao(Context context){
		mBizDBHelper = BizDBHelper.getInstance(context);
	}
}
