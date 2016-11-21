package com.hcp.wo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hcp.common.ToastHelper;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.SubInventoryAuthorityDao;
import com.hcp.entities.SubInventoryAuthority;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SubInventoryAuthorityActivity extends ScannerActivity {

	private EditText mEdtUser;
	private Button mBtnAdd;
	private ListView mLvList;

	private List<SubInventoryAuthority> mAuthorities = new ArrayList<SubInventoryAuthority>();

	private AuthorityAdapter mAdapter = new AuthorityAdapter();

	private ToastHelper mToastHelper;
	private SubInventoryAuthorityActivity mInstance;

	private SubInventoryAuthorityDao mAuthorityDao;

	private List<String> mSubinventoryList;
	private SubInventoryAdapter mSubInventoryAdapter;
	private ListView mLvSubinventoryList;
	private EditText mEdtSubinventoryInput;
	private Button mBtnSubinventoryAdd;

	private AlertDialog mAddingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_inventory_authority_setting);

		mToastHelper = ToastHelper.getInstance(this);
		mAuthorityDao = SubInventoryAuthorityDao.getInstance(this);
		mInstance = this;

		try {
			mAuthorities = mAuthorityDao.getAllAuthorities();
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}

		mEdtUser = (EditText) findViewById(R.id.edt_sia_user);

		mBtnAdd = (Button) findViewById(R.id.btn_sia_add);
		mBtnAdd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String user = mEdtUser.getText().toString();
				if(TextUtils.isEmpty(user)){
					mToastHelper.show("请输入用户名!");
					return;
				}

				if(mAuthorityDao.exists(user)){
					mToastHelper.show("该用户已存在!");
					return;
				}

				SubInventoryAuthority authority = new SubInventoryAuthority();
				authority.username = user;

				try {
					mAuthorityDao.insert(authority);

					mAuthorities.add(authority);
					mAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					mToastHelper.show(e.getMessage());
				}
			}
		});

		mLvList = (ListView) findViewById(R.id.lv_sia_users);
		mLvList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {

				final SubInventoryAuthority authority = mAuthorities.get(position);
				final int location = position;
				new AlertDialog.Builder(mInstance)
						.setTitle("删除")
						.setMessage("确认删除该项?")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									if(mAuthorityDao.delete(authority.username) > 0){
										mToastHelper.show("删除成功");
										mAuthorities.remove(location);
										mAdapter.notifyDataSetChanged();
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						})
						.setNegativeButton("取消", null)
						.show();

				return false;
			}
		});
		mLvList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {

				final SubInventoryAuthority authority = mAuthorities.get(position);
				mSubinventoryList = new ArrayList<String>();

				if(!TextUtils.isEmpty(authority.subinventories)){
					String[] subinvnetories = authority.subinventories.split(SubInventoryAuthorityDao.SEPARATOR);
					for(String subinventory : subinvnetories){
						mSubinventoryList.add(subinventory);
					}
				}

				View subinventoryListView = LayoutInflater.from(mInstance).inflate(R.layout.view_sub_inventory_add, null);

				mLvSubinventoryList = (ListView)subinventoryListView.findViewById(R.id.lv_vsia_users);
				mLvSubinventoryList.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
												   View view, int position, long id) {

						final int location = position;
						new AlertDialog.Builder(mInstance)
								.setTitle("删除")
								.setMessage("确认删除该项?")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										mSubinventoryList.remove(location);
										mSubInventoryAdapter.notifyDataSetChanged();

									}
								})
								.setNegativeButton("取消", null)
								.show();

						return false;
					}

				});

				mSubInventoryAdapter = new SubInventoryAdapter();
				mLvSubinventoryList.setAdapter(mSubInventoryAdapter);

				mEdtSubinventoryInput = (EditText) subinventoryListView.findViewById(R.id.edt_vsia_subinventory);
				mBtnSubinventoryAdd = (Button) subinventoryListView.findViewById(R.id.btn_vsia_add);
				mBtnSubinventoryAdd.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String subinventory = mEdtSubinventoryInput.getText().toString();
						if(TextUtils.isEmpty(subinventory)){
							mToastHelper.show("请输入子库");
							return;
						}

						addSubinventory(subinventory);
					}
				});

				Builder builder = new AlertDialog.Builder(mInstance);
				builder.setTitle("请添加子库");
				builder.setView(subinventoryListView);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						authority.subinventories = StringUtils.join(mSubinventoryList, SubInventoryAuthorityDao.SEPARATOR);
						try {
							mAuthorityDao.update(authority);
						} catch (Exception e) {
							mToastHelper.show(e.getMessage());
						}
						mAdapter.notifyDataSetChanged();
					}
				});

				mAddingDialog = builder.create();
				mAddingDialog.show();
			}
		});
		mLvList.setAdapter(mAdapter);
	}

	@Override
	protected void decodeCallback(String barcode) {
		if(isAddingSub()){
			addSubinventory(barcode);
		}
	}

	private void addSubinventory(String subinventory){
		if(!mSubinventoryList.contains(subinventory)){
			mSubinventoryList.add(subinventory);
			mSubInventoryAdapter.notifyDataSetChanged();
		}else{
			mToastHelper.show("该子库已存在!");
		}
	}

	private boolean isAddingSub(){
		return mAddingDialog != null && mAddingDialog.isShowing();
	}

	private class AuthorityAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mAuthorities.size();
		}

		@Override
		public Object getItem(int position) {
			return mAuthorities.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder = null;

			if(convertView == null){
				convertView = LayoutInflater.from(SubInventoryAuthorityActivity.this).inflate(R.layout.subinventory_authority_item, null);

				TextView tvUser = (TextView) convertView.findViewById(R.id.tv_subinventory_authority_item_username);
				TextView tvAuthorities = (TextView) convertView.findViewById(R.id.tv_subinventory_authority_item_subinventories);

				viewHolder = new ViewHolder();
				viewHolder.user = tvUser;
				viewHolder.authorities = tvAuthorities;

				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			SubInventoryAuthority authority = mAuthorities.get(position);
			viewHolder.user.setText(authority.username);
			viewHolder.authorities.setText(authority.subinventories == null ? "" : authority.subinventories.replace(SubInventoryAuthorityDao.SEPARATOR, " | "));

			return convertView;
		}

		private class ViewHolder{
			private TextView user;
			private TextView authorities;
		}
	}

	private class SubInventoryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mSubinventoryList.size();
		}

		@Override
		public Object getItem(int position) {
			return mSubinventoryList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView tvSub = null;

			if(convertView == null){

				tvSub = new TextView(mInstance);
				tvSub.setPadding(15, 15, 15, 15);
				tvSub.setTextSize(20);

				convertView = tvSub;

			}else{
				tvSub = (TextView) convertView;
			}

			tvSub.setText(mSubinventoryList.get(position));

			return convertView;
		}
	}
}
