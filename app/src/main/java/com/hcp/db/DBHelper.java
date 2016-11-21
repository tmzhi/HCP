package com.hcp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.hcp.common.DateFormat;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper{

	protected SQLiteDatabase mDb;
	
	protected Context mContext;

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		
		mContext = context;
		
		mDb = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public <T> long insert(Class<T> cls, T object) throws Exception{
		
		String table = cls.getSimpleName();
		
		ContentValues values = new ContentValues();
		
		Field[] fields = cls.getFields();
		for(Field feild : fields){
			
			feild.setAccessible(true);
			
			Class<?> feildCls = feild.getType();
			String feildName = feild.getName();
			String feildValue = null;
			
			if(feildCls == String.class){
				feildValue = feild.get(object) == null ? "" : feild.get(object).toString();
			}else if(feildCls == int.class){
				feildValue = feild.getInt(object) + "";
			}else if(feildCls == long.class || feildCls == Long.class){
				feildValue = feild.getLong(object) + "";
			}else if(feildCls == boolean.class || feildCls == Boolean.class){
				feildValue = feild.getBoolean(object) + "";
			}else if(feildCls == BigDecimal.class){
				feildValue = (BigDecimal)feild.get(object) + "";
			}else if(feildCls == Date.class){
				feildValue = DateFormat.defaultFormat((Date)feild.get(object));
			}
			
			values.put(feildName, feildValue);
		}
		
		return mDb.insert(table, null, values);
	}
	
	public <T> int update(Class<T> cls, T object, String whereClause, String[] args) throws Exception{
		
		String table = cls.getSimpleName();
		
		ContentValues values = new ContentValues();
		
		Field[] fields = cls.getFields();
		for(Field feild : fields){
			
			feild.setAccessible(true);
			
			Class<?> feildCls = feild.getType();
			String feildName = feild.getName();
			String feildValue = null;
			
			if(feildCls == String.class){
				feildValue = feild.get(object) == null ? "" : feild.get(object).toString();
			}else if(feildCls == int.class){
				feildValue = feild.getInt(object) + "";
			}else if(feildCls == long.class || feildCls == Long.class){
				feildValue = feild.getLong(object) + "";
			}else if(feildCls == boolean.class || feildCls == Boolean.class){
				feildValue = feild.getBoolean(object) + "";
			}
			
			values.put(feildName, feildValue);
		}
		
		return mDb.update(table, values, whereClause, args);
	}
	
	public <T> int delete(Class<T> cls, String whereClause, String[] args) throws Exception{
		
		String table = cls.getSimpleName();
		
		return mDb.delete(table, whereClause, args);
	}
	
	public <T> boolean exists(Class<T> cls, String whereClause, String[] args){
		
		boolean result = false;
		
		String table = cls.getSimpleName();
		
		Cursor cursor = mDb.query(table, null, whereClause, args, null, null, null);
		
		if(cursor != null && !cursor.isAfterLast()){
			cursor.moveToFirst();
			result = cursor.getCount() > 0;
		}
		cursor.close();
		return result;
	}

	public <T> boolean insert(List<T> objs) throws Exception {

		if (objs == null) {
			throw new IllegalArgumentException("null object");
		}

		Class<?> cls = objs.get(0).getClass();

		String table = cls.getSimpleName();

		Field[] fields = cls.getFields();

		try {
			mDb.beginTransaction();
			for (T obj : objs) {
				ContentValues values = new ContentValues();
				for (Field field : fields) {

					field.setAccessible(true);

					Class<?> feildCls = field.getType();
					String columnName = field.getName();
					String feildValue = null;

					if (feildCls == String.class) {
						feildValue = field.get(obj) == null ? "" : field.get(obj).toString();
					} else if (feildCls == int.class) {
						feildValue = field.getInt(obj) + "";
					} else if (feildCls == long.class) {
						feildValue = field.getLong(obj) + "";
					} else if (feildCls == boolean.class) {
						feildValue = field.getBoolean(obj) + "";
					} else if (feildCls == BigDecimal.class) {
						feildValue = (BigDecimal) field.get(obj) + "";
					} else if (feildCls == Date.class) {
						feildValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format((Date) field.get(obj));
					} else{
						feildValue = String.valueOf(field.get(obj));
					}

					values.put(columnName, feildValue);
				}
				mDb.insert(table, null, values);
			}
			mDb.setTransactionSuccessful();
			return true;
		} catch (Exception ex) {
			throw ex;
		}finally {
			mDb.endTransaction();
		}
	}
	
	public <T> List<T> queryAll(Class<T> cls) throws Exception{
		return queryList(cls, null, null, null, null, null, null);
	}
	
	public <T> List<T> queryList(Class<T> cls, String[] columns, String whereClause,  String[] args, String groupBy, String having, String orderBy) throws Exception{
		List<T> objs = new ArrayList<T>();
		
		String table = cls.getSimpleName();
		Cursor cursor = mDb.query(table, columns, whereClause, args, groupBy, having, orderBy);
		
		if(cursor != null && !cursor.isAfterLast()){
			Field[] fields = cls.getFields();
			
			cursor.moveToFirst();
			
			while (!cursor.isAfterLast()) {
				T obj = cls.newInstance();

				for(Field field : fields){
					
					Class<?> fieldCls = field.getType();
					
					field.setAccessible(true);
					int columnIndex = cursor.getColumnIndex(field.getName());
					
					if(fieldCls == Date.class){
						field.set(obj, DateFormat.defaultParse(cursor.getString(columnIndex)));
					}else if(fieldCls == BigDecimal.class){
						field.set(obj, new BigDecimal(cursor.getString(columnIndex)));
					}else if(fieldCls == int.class){
						field.set(obj, Integer.parseInt((cursor.getString(columnIndex))));
					}
					else{
						field.set(obj, cursor.getString(columnIndex));
					}
				}
				objs.add(obj);
				
				cursor.moveToNext();
			}
				
			
			cursor.close();
		}
		
		return objs;
	}
	
	public <T> T query(Class<T> cls, String whereClause, String[] args) throws Exception{
		T obj = cls.newInstance();
		
		String table = cls.getSimpleName();
		Cursor cursor = mDb.query(table, null, whereClause, args, null, null, null);
		
		if(cursor != null && !cursor.isAfterLast()){
			Field[] fields = cls.getFields();
			
			cursor.moveToFirst();
				
			for(Field field : fields){
				field.setAccessible(true);
				
				Class<?> fieldCls = field.getType();
				
				int columnIndex = cursor.getColumnIndex(field.getName());
				if(fieldCls == Date.class){
					field.set(obj, DateFormat.defaultParse(cursor.getString(columnIndex)));
				}else if(fieldCls == BigDecimal.class){
					field.set(obj, new BigDecimal(cursor.getString(columnIndex)));
				}else if(fieldCls == int.class){
					field.set(obj, Integer.parseInt((cursor.getString(columnIndex))));
				}
				else{
					field.set(obj, cursor.getString(columnIndex));
				}
			}
			
			cursor.close();
		}
		
		return obj;
	}
	
}
