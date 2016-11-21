package com.hcp.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.hcp.common.AppCommon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class BizDBHelper extends DBHelper {
	
	private static final String DB_SCRIPT_FILE = "biz.sql";
	private static final String DB_SCRIPT_UPGRADE_FILE = "upgrade.sql";
	private static final String DB_PATH = AppCommon.APP_TEMP_DIRECTORY + "biz.db";
	
	private static final int VERSION = 10;
	
	private static BizDBHelper mInstance;
	
	private static Object mLocker = new Object();
	
	public static BizDBHelper getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new BizDBHelper(context);
			}
		}
		return mInstance;
	}

	private BizDBHelper(Context context) {
		super(context, DB_PATH, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			InputStream inputStream = mContext.getAssets().open(DB_SCRIPT_FILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder stbSQL = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				stbSQL.append(line);
				
				if(line.trim().endsWith(";")){
					db.execSQL(stbSQL.toString().replace(";", ""));
					stbSQL.delete(0, stbSQL.length());
				}
			}
			
			reader.close();
			inputStream.close();
			
		} catch (IOException e) {
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		try {
			InputStream inputStream = mContext.getAssets().open(DB_SCRIPT_UPGRADE_FILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder stbSQL = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				stbSQL.append(line);
				
				if(line.trim().endsWith(";")){
					db.execSQL(stbSQL.toString().replace(";", ""));
					stbSQL.delete(0, stbSQL.length());
				}
			}
			
			reader.close();
			inputStream.close();
			
		} catch (IOException e) {
		}
	}
}
