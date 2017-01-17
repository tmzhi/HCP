package com.hcp.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;

import java.io.File;

public class AppCommon {
	public static final String APP_TEMP_DIRECTORY = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator 
			+ "HCP" + File.separator;

	
	static{
		File appDirectory = new File(APP_TEMP_DIRECTORY);
		if(!appDirectory.exists()){
			appDirectory.mkdir();
		}
	}
	
	/**
	 * Get application version
	 * @param context
	 * @return
	 */
	public static int getAppVersion(Context context){
		return getPackageInfo(context).versionCode;
	}
	
	/**
	 * Get application version Name
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context){
		return getPackageInfo(context).versionName;
	}
	
	private static PackageInfo getPackageInfo(Context context){

		PackageManager manager = context.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			
		}
		return info;
	}
	
	public static void installApp(Context context, String apkPath){

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath))
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
        Process.killProcess(Process.myPid());
	}
}
