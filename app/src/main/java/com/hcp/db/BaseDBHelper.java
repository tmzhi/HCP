package com.hcp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hcp.common.AppCommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseDBHelper extends DBHelper {
	
	private static final String DB_SCRIPT_FILE = "base.sql";
	private static final String DB_PATH = AppCommon.APP_TEMP_DIRECTORY + "base.db";
	private static final String DB_SCRIPT_UPGRADE_FILE = "base_upgrade.sql";
	
	private static final int VERSION = 7;
	
	private static BaseDBHelper mInstance;
	
	private static Object mLocker = new Object();
	
	public static BaseDBHelper getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				mInstance = new BaseDBHelper(context);
			}
		}
		return mInstance;
	}

	private BaseDBHelper(Context context) {
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
