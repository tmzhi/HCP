package com.hcp.util;

public class AppCache {
	private static AppCache mInstance;

	private static Object mLocker = new Object();

	private String mLoginUser;

	public static AppCache getInstance() {
		if (mInstance == null) {
			synchronized (mLocker) {
				if (mInstance == null) {
					mInstance = new AppCache();
				}
			}
		}
		return mInstance;
	}

	public void setLoginUser(String loginUser) {
		this.mLoginUser = loginUser;
	}

	public String getLoginUser() {
		return this.mLoginUser;
	}
}
