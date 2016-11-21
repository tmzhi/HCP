package com.hcp.dao;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.hcp.db.BaseDBHelper;
import com.hcp.entities.SubInventoryAuthority;

public class SubInventoryAuthorityDao {
	private static SubInventoryAuthorityDao mInstance;
	
	private static Object mLocker = new Object();
	
	public static final String SEPARATOR = "&";
	
	private BaseDBHelper mBaseDBHelper;
	
	public static SubInventoryAuthorityDao getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new SubInventoryAuthorityDao(context);
			}
		}
		return mInstance;
	}
	
	private SubInventoryAuthorityDao(Context context){
		mBaseDBHelper = BaseDBHelper.getInstance(context);
	}
	
	public SubInventoryAuthority getAuthority(String username) throws Exception{
		return mBaseDBHelper.query(SubInventoryAuthority.class, "username=?", new String[]{username});
	}
	
	public long insert(SubInventoryAuthority authority) throws Exception{
		return mBaseDBHelper.insert(SubInventoryAuthority.class, authority);
	}
	
	public int update(SubInventoryAuthority authority) throws Exception{
		return mBaseDBHelper.update(SubInventoryAuthority.class, authority, "username=?", new String[]{authority.username});
	}
	
	public boolean exists(String username){
		return mBaseDBHelper.exists(SubInventoryAuthority.class, "username=?", new String[]{username});
	}
	
	public int delete(String username) throws Exception{
		return mBaseDBHelper.delete(SubInventoryAuthority.class, "username=?", new String[]{username});
	}
	
	public List<SubInventoryAuthority> getAllAuthorities() throws Exception{
		return mBaseDBHelper.queryAll(SubInventoryAuthority.class);
	}
	
	public String[] disconvertToAuthorities(SubInventoryAuthority authority){
		
		if(TextUtils.isEmpty(authority.subinventories)){
			throw new IllegalArgumentException("SubInventories is empty!");
		}
		
		return authority.subinventories.split(SEPARATOR);
	}
}
