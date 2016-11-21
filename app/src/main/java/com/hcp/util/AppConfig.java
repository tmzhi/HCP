package com.hcp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.apache.commons.lang3.StringUtils;

public class AppConfig {

	private final String APPCONFIG_FILE = "AppConfig.xml";

	private final String KEY_SERVER_IP = "ServerIP";
	private final String KEY_SERVER_PORT = "ServerPort";
	private final String KEY_LAST_LOGIN_USER = "LastLoginUser";
	private final String KEY_PRINTER_IP = "PrinterIP";
	private final String KEY_PRINTER_PORT = "PrinterPort";
	private final String KEY_PRINT_ENABLE = "PrintEnable";

	private final String KEY_ORG_ID = "OrgId";
	private final String KEY_ORG_CODE = "OrgCode";
	private final String KEY_ORG_NAME = "OrgName";

	private static AppConfig mInstance;
	private static Object mLocker = new Object();

	private SharedPreferences mShPref;
	private Editor mShPrefEditor;

	private String mServerIP;
	private int mServerPort;
	private String mLastLoginUser;
	private String mPrinterIP;
	private int mPrinterPort;
	private boolean mPrintEnable;
	private String mOrganizationCode;
	private int mOrganizationId;
	private String mOrganizationName;

	private AppConfig(Context context) {

		init(context);
	}

	private void init(Context context) {
		mShPref = context.getSharedPreferences(APPCONFIG_FILE,
				Context.MODE_PRIVATE);
		mShPrefEditor = mShPref.edit();

		mServerIP = mShPref.getString(KEY_SERVER_IP, "192.168.170.70");
		mServerPort = mShPref.getInt(KEY_SERVER_PORT, 9898);
		mLastLoginUser = mShPref.getString(KEY_LAST_LOGIN_USER, StringUtils.EMPTY);
		mPrinterIP = mShPref.getString(KEY_PRINTER_IP, "192.168.161.249");
		mPrinterPort = mShPref.getInt(KEY_PRINTER_PORT, 9100);
		mPrintEnable = mShPref.getBoolean(KEY_PRINT_ENABLE, false);

		mOrganizationCode = mShPref.getString(KEY_ORG_CODE, "");
		mOrganizationId = mShPref.getInt(KEY_ORG_ID, -1);
		mOrganizationName = mShPref.getString(KEY_ORG_NAME, "");
	}

	public String getServerUrl() {
		return String.format("http://%s:%s/", mServerIP, mServerPort);
	}

	public void setServerIP(String ip) {
		this.mServerIP = ip;
		mShPrefEditor.putString(KEY_SERVER_IP, ip);
		mShPrefEditor.commit();
	}

	public String getServerIP() {
		return this.mServerIP;
	}

	public int getServerPort() {
		return this.mServerPort;
	}

	public void setServerPort(int serverPort) {
		this.mServerPort = serverPort;
		mShPrefEditor.putInt(KEY_SERVER_PORT, serverPort);
		mShPrefEditor.commit();
	}
	
	public int getPrinterPort() {
		return this.mPrinterPort;
	}

	public void setPrinterPort(int printerPort) {
		this.mPrinterPort = printerPort;
		mShPrefEditor.putInt(KEY_PRINTER_PORT, printerPort);
		mShPrefEditor.commit();
	}
	
	public String getPrinterIP() {
		return this.mPrinterIP;
	}

	public void setPrinterIP(String printerIP) {
		this.mPrinterIP = printerIP;
		mShPrefEditor.putString(KEY_PRINTER_IP, printerIP);
		mShPrefEditor.commit();
	}
	
	public String getLastLoginUser(){
		return this.mLastLoginUser;
	}
	
	public void setLastLoginUser(String user){
		this.mLastLoginUser = user;
		mShPrefEditor.putString(KEY_LAST_LOGIN_USER, user);
		mShPrefEditor.commit();
	}
	
	public boolean isPrintEnable(){
		return this.mPrintEnable;
	}
	
	public void setPrintEnable(boolean printEnable){
		this.mPrintEnable = printEnable;
		mShPrefEditor.putBoolean(KEY_PRINT_ENABLE, printEnable);
		mShPrefEditor.commit();
	}

	public String getOrganizationName(){
		return this.mOrganizationName;
	}

	public void setOrganizationName(String orgName){
		this.mOrganizationName = orgName;
		mShPrefEditor.putString(KEY_ORG_NAME, orgName);
		mShPrefEditor.commit();
	}

	public int getOrganizationId(){
		return this.mOrganizationId;
	}

	public void setOrganizationId(int orgId){
		this.mOrganizationId = orgId;
		mShPrefEditor.putInt(KEY_ORG_ID, orgId);
		mShPrefEditor.commit();
	}

	public String getOrganizationCode(){
		return this.mOrganizationCode;
	}

	public void setOrganizationCode(String orgCode){
		this.mOrganizationCode = orgCode;
		mShPrefEditor.putString(KEY_ORG_CODE, orgCode);
		mShPrefEditor.commit();
	}

	public static AppConfig getInstance(Context context) {
		if (mInstance == null) {
			synchronized (mLocker) {
				if (mInstance == null) {
					mInstance = new AppConfig(context);
				}
			}
		}
		return mInstance;
	}



}
