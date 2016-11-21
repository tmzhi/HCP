package com.hcp.biz.request;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.hcp.entities.User;
import com.hcp.http.RequestUtil;
import com.hcp.util.AppConfig;

public class LoginRequestManager {
	private static LoginRequestManager mInstance;
	private static Object mLocker = new Object();

	private AppConfig mAppConfig;

	private LoginRequestManager(Context context) {
		mAppConfig = AppConfig.getInstance(context);
	}

	public static LoginRequestManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (mLocker) {
				if (mInstance == null) {
					mInstance = new LoginRequestManager(context);
				}
			}
		}
		return mInstance;
	}

	public User getLoginUser(String userCode) throws Exception {
		User user = null;
		PropertyInfo userInfo = new PropertyInfo();
		userInfo.setName("usercode");
		userInfo.setValue(userCode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { userInfo };

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetLoginUser");

		if (!TextUtils.isEmpty(result)
				&& !RequestUtil.RESPONSE_MSG_FAILED.equals(result)) {
			user = JSONObject.parseObject(result, User.class);
		}

		return user;
	}

	public boolean isLoginSuccess(String userCode, String password) throws Exception {

		boolean result = false;

		PropertyInfo ucInfo = new PropertyInfo();
		ucInfo.setName("usercode");
		ucInfo.setValue(userCode);

		PropertyInfo pwInfo = new PropertyInfo();
		pwInfo.setName("password");
		pwInfo.setValue(password);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { ucInfo, pwInfo };
		try {
			result = RequestUtil.RESPONSE_MSG_SUCCESS.equalsIgnoreCase(RequestUtil
					.doRequest(mAppConfig.getServerUrl(), propertyInfos,
							"IsLoginSuccess"));
		} catch (Exception e) {
			throw e;
		}

		return result;
	}
}
