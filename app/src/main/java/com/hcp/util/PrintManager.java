package com.hcp.util;

import java.io.IOException;

import android.content.Context;

import com.hcp.device.LPK130WifiPrinter;

public class PrintManager {
	private LPK130WifiPrinter mPrinter;
	private static PrintManager mInstance;
	
	private AppConfig mAppConfig;
	
	private static Object mLocker = new Object();
	
	private PrintManager(Context context){
		mAppConfig = AppConfig.getInstance(context);
	}
	
	public static PrintManager getInstance(Context context){
		if(mInstance == null){
			synchronized (mLocker) {
				if(mInstance == null){
					mInstance = new PrintManager(context);
				}
			}
		}
		
		return mInstance;
	}
	
	public void connect() throws IOException{
		if(mAppConfig.isPrintEnable()){
			
			mPrinter = new LPK130WifiPrinter(mAppConfig.getPrinterIP(), mAppConfig.getPrinterPort());
			
			mPrinter.connect();
		}
	}
	
	public void close() throws IOException{
		if(mAppConfig.isPrintEnable()){
			mPrinter.close();
		}
	}
	
	public void print(byte[] content) throws IOException{
		if(mAppConfig.isPrintEnable()){
			mPrinter.print(content);
		}
	}
	
	public void printOneDimenBarcode(String content) throws IOException{
		if(mAppConfig.isPrintEnable()){
			mPrinter.printOneDimenBarcode(content);
		}
	}
	
	public void print(String content) throws IOException{
		if(mAppConfig.isPrintEnable()){
			mPrinter.print(content.getBytes());
		}
	}
}
