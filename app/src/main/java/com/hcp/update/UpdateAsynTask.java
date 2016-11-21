package com.hcp.update;

import java.io.IOException;

import com.hcp.common.AppCommon;
import com.hcp.download.Downloader;
import com.hcp.download.Downloader.OnProgressUpdate;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class UpdateAsynTask extends AsyncTask<String, Integer, Boolean> implements OnProgressUpdate{
		
		private ProgressDialog mProgressDialog;
		
		private Context mContext;
		
		private String localFile;
		
		public UpdateAsynTask(Context context){
			this.mContext = context;
		}
		
		public void setProgressDialog(ProgressDialog progressDialog){
			this.mProgressDialog = progressDialog;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			
			boolean result = false;
			
			String downloadFile = params[0];
			localFile = params[1];
			
			try {
				Downloader downloader = new Downloader();
				downloader.setProgressUpdate(this);
				
				downloader.download(downloadFile, localFile);
				
		        result = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return result;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			
			if(mProgressDialog != null){
				mProgressDialog.setMax(values[0]/1024);
				mProgressDialog.setProgress(values[1]/1024);
			}
			
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			if(mProgressDialog != null){
				mProgressDialog.cancel();
			}
			
			if(result){
				AppCommon.installApp(mContext, localFile);
			}
			
			super.onPostExecute(result);
		}

		@Override
		public void updateProgress(int contentLength, int totalReadLength,
				int currentReadLength) {
			publishProgress(contentLength, totalReadLength, currentReadLength);
		}
	}

