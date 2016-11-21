package com.hcp.wo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcp.common.activity.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FuncChooseActivity extends BaseActivity{
	
	private GridView mGvContainer;
	
	private OperationsAdapter mAdapter = new OperationsAdapter();
	
	private List<String> mOperationTags = new ArrayList<String>();
	private FuncChooseActivity mInstance;

	private Map<String, Integer> mNameResKeyMapping = new LinkedHashMap<String, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_func_choose);
		
		mInstance = this;
		
		init();
	}
	
	private void init(){

		mNameResKeyMapping.put(getString(R.string.func_choose_wip_entity), R.drawable.img_material);
		mNameResKeyMapping.put(getString(R.string.func_choose_query), R.drawable.img_query);
		mNameResKeyMapping.put(getString(R.string.func_choose_intraware), R.drawable.img_intra);
		mNameResKeyMapping.put(getString(R.string.func_choose_stock_taking), R.drawable.img_stock);

		mOperationTags = Arrays.asList(mNameResKeyMapping.keySet().toArray(new String[mNameResKeyMapping.keySet().size()]));

		mGvContainer = (GridView) findViewById(R.id.gv_func_choose_container);
		mGvContainer.setAdapter(mAdapter);
		mGvContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						toMainMenu();
						break;
					case 1:
						toQueryMenu();
						break;
					case 2:
						toIntraware();
						break;
					case 3:
						toStock();
						break;
				}
			}
		});
	}
	
	private void toMainMenu() {
		startActivity(new Intent(this, MainActivity.class));
	}

	private void toQueryMenu() {
		startActivity(new Intent(this, QueryMenuActivity.class));
	}

	private void toIntraware() {
		startActivity(new Intent(this, IntrawareMenuActivity.class));
	}

	private void toStock() {
		startActivity(new Intent(this, StockTakingMenuActivity.class));
	}
	
	private class OperationsAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return mOperationTags.size();
		}

		@Override
		public Object getItem(int position) {
			return mOperationTags.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("ResourceType")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			
			if(convertView == null){
				convertView = LayoutInflater.from(mInstance).inflate(R.layout.item_func_choose, null);

				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.tv_item_func_choose_text);
				holder.icon = (ImageView)convertView.findViewById(R.id.img_item_func_choose_icon);

				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}

			String text = mOperationTags.get(position);
			holder.text.setText(text);
			holder.icon.setImageResource(mNameResKeyMapping.get(text));
			
			return convertView;
		}

		private class ViewHolder{
			private TextView text;
			private ImageView icon;
		}
	}
}
