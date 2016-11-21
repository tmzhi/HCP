package com.hcp.dao;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.hcp.db.BaseDBHelper;
import com.hcp.entities.Authority;

public class AuthorityDao {
	private static AuthorityDao mInstance;
	
	private static Object mLocker = new Object();
	
	public static final String SEPARATOR = "&";
	
	private BaseDBHelper mBaseDBHelper;
	
	public static AuthorityDao getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new AuthorityDao(context);
			}
		}
		return mInstance;
	}
	
	private AuthorityDao(Context context){
		mBaseDBHelper = BaseDBHelper.getInstance(context);
	}
	
	public Authority getAuthority(String username) throws Exception{
		return mBaseDBHelper.query(Authority.class, "username=?", new String[]{username});
	}
	
	public long insert(Authority authority) throws Exception{
		return mBaseDBHelper.insert(Authority.class, authority);
	}
	
	public int update(Authority authority) throws Exception{
		return mBaseDBHelper.update(Authority.class, authority, "username=?", new String[]{authority.username});
	}
	
	public boolean exists(String username){
		return mBaseDBHelper.exists(Authority.class, "username=?", new String[]{username});
	}
	
	public int delete(String username) throws Exception{
		return mBaseDBHelper.delete(Authority.class, "username=?", new String[]{username});
	}
	
	public List<Authority> getAllAuthorities() throws Exception{
		return mBaseDBHelper.queryAll(Authority.class);
	}
	
	public String[] disconvertToAuthorities(Authority authority){
		
		if(TextUtils.isEmpty(authority.authorities)){
			throw new IllegalArgumentException("Authorities is empty!");
		}
		
		return authority.authorities.split(SEPARATOR);
	}
}
