package com.hcp.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {
	
	private OnProgressUpdate progressUpdate;
	
	public void setProgressUpdate(OnProgressUpdate progressUpdate){
		this.progressUpdate = progressUpdate;
	}
	
	public void download(String downloadFile, String localFile) throws IOException{
		
		URL fileUrl = new URL(downloadFile);
		
		File local = new File(localFile);
		
		download(fileUrl, local);
	}

	public void download(URL downloadFile, File localFile) throws IOException {
		
		URLConnection urlCon;
		
		urlCon = downloadFile.openConnection();
		urlCon.setConnectTimeout(1000 * 5);
		
		InputStream is = urlCon.getInputStream();
		
		int contentLength = urlCon.getContentLength();
		int readLength = 0;
		int currentReadLength = 0;
		
		byte[] buffer = new byte[1024];
		OutputStream os = new FileOutputStream(localFile);
		while ((currentReadLength = is.read(buffer)) != -1) {
			
			readLength += currentReadLength;
			os.write(buffer, 0, currentReadLength);
			
			if(progressUpdate != null){
				progressUpdate.updateProgress(contentLength, readLength, currentReadLength);
			}
		}
		os.flush();
		os.close();
		is.close();
	}
	
	public interface OnProgressUpdate{
		void updateProgress(int contentLength, int totalReadLength, int currentReadLength);
	}

}
