package com.hcp.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;

public class XmlUtil {
	
	public final static String ENCODING_UTF_8 = "UTF-8";
	public final static String ENCODING_GB2312 = "GB2312";
	
	public static <T> T deserialize(Class<T> cls, String file) throws Exception{
		return deserialize(cls, file, ENCODING_UTF_8);
	}
	
	/**
	 * Deserialize object from xml file. if xml tags does not contains object Name,
	 * then find tag witch matches field Name, and return a instance object.
	 * P.S: this class must has constructor with no parameters.
	 * @param cls
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(Class<T> cls, String file, String encoding) throws Exception{
		T obj = null;
		
		String className = cls.getSimpleName();
		
		Field[] fields = cls.getDeclaredFields();
		Map<String, Field> fieldMapping = new HashMap<String, Field>();
		for(Field field : fields){
			field.setAccessible(false);
			fieldMapping.put(field.getName(), field);
		}
		
		Constructor<?>[] constructors = cls.getDeclaredConstructors();
		Constructor<?> constructor = constructors[0];
		constructor.setAccessible(false);
		
		XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
		
		if(TextUtils.isEmpty(encoding)){
			encoding = ENCODING_UTF_8;
		}
		
		InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(new File(file)), encoding);
		pullParser.setInput(inputStreamReader);
		
		int eventType = XmlPullParser.START_DOCUMENT;
		while((eventType = pullParser.next()) != XmlPullParser.END_DOCUMENT){
			switch (eventType) {
			case XmlPullParser.START_TAG:
				
				String tagName = pullParser.getName();
				
				if(tagName.equals(className)){
					constructor.newInstance(obj);
				}else if(fieldMapping.containsKey(tagName)){
					if(obj == null){
						obj = (T) constructor.newInstance();
					}
					
					Field field = fieldMapping.get(tagName);
					
					String value = pullParser.nextText();
					Object valueObj = null;
					
					if(field.getType() == int.class){
						valueObj = Integer.parseInt(value);
					}else if(field.getType() == long.class){
						valueObj = Long.parseLong(value);
					}else{
						valueObj = value;
					}
					
					field.set(obj, valueObj);
				}
				break;

			default:
				break;
			}
		}
		
		return obj;
	}
	
	
}
