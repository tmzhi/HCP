package com.hcp.wo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hcp.biz.request.WORequestManager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class TestActivity extends Activity{
	
	TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		mTextView = (TextView) findViewById(R.id.tv_test2);
		
		test();
	}
	


	List<String> test = new ArrayList<String>();
	private void test(){
		
		for(int i=0;i<30;i++){
			test.add(StringUtils.EMPTY);
			final int temp = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					int id = temp;
						while(true){
							try {
								test.set(id, WORequestManager.getInstance(TestActivity.this).test() + " "+new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
								mTestHandler.sendEmptyMessage(0);
								Thread.sleep(1000);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				}
			}).start();
		}
	}
	Handler mTestHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<test.size();i++){
				buffer.append(i + ":" + test.get(i) + "\n");
			}
			mTextView.setText(buffer.toString());
			return false;
		}
	});
}
