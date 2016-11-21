package com.hcp.wo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
import com.hcp.common.activity.BaseActivity;
import com.hcp.dao.AuthorityDao;
import com.hcp.entities.Authority;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BizAuthorityActivity extends BaseActivity {

	private EditText mEdtUser;
	private Button mBtnAdd;
	private ListView mLvList;

	private List<Authority> mAuthorities = new ArrayList<Authority>();

	private AuthorityAdapter mAdapter = new AuthorityAdapter();

	private ToastHelper mToastHelper;
	private BizAuthorityActivity mInstance;

	private AuthorityDao mAuthorityDao;
	private String[] mOperations;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authority_setting);

		mToastHelper = ToastHelper.getInstance(this);
		mAuthorityDao = AuthorityDao.getInstance(this);
		mInstance = this;

		mOperations = getResources().getStringArray(R.array.operations);

		try {
			mAuthorities = mAuthorityDao.getAllAuthorities();
		} catch (Exception e) {
			mToastHelper.show(e.getMessage());
		}

		mEdtUser = (EditText) findViewById(R.id.edt_authority_user);

		mBtnAdd = (Button) findViewById(R.id.btn_authority_add);
		mBtnAdd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String user = mEdtUser.getText().toString();
				if(TextUtils.isEmpty(user)){
					mToastHelper.show("请输入用户名!");
					return;
				}

				if(mAuthorityDao.exists(user)){
					mToastHelper.show("该用户已存在");
					return;
				}

				Authority authority = new Authority();
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

		mLvList = (ListView) findViewById(R.id.lv_authority_users);
		mLvList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {

				final Authority authority = mAuthorities.get(position);
				final int location = position;
				new AlertDialog.Builder(mInstance)
						.setTitle("删除")
						.setMessage("确认删除该项?")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									if(mAuthorityDao.delete(authority.username)>0){
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

				final Authority authority = mAuthorities.get(position);
				String[] authorities = null;
				int authoritesIndex = -1;
				if(!TextUtils.isEmpty(authority.authorities)){
					authorities = authority.authorities.split(AuthorityDao.SEPARATOR);
					authoritesIndex = 0;
				}

				final boolean[] selected = new boolean[mOperations.length];
				for(int i=0, length=selected.length; i<length; i++){
					if(authoritesIndex>-1
							&& authoritesIndex < authorities.length
							&& authorities[authoritesIndex].equals(mOperations[i])){
						selected[i] = true;
						authoritesIndex ++;
					}else{
						selected[i] = false;
					}
				}

				Builder builder = new AlertDialog.Builder(mInstance);
				builder.setTitle("请选择可执行的操作");
				DialogInterface.OnMultiChoiceClickListener mutiListener =
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialogInterface,
												int which, boolean isChecked) {
								selected[which] = isChecked;
							}
						};
				builder.setMultiChoiceItems(mOperations, selected, mutiListener);

				DialogInterface.OnClickListener btnListener =
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int which) {
								List<String> selections = new ArrayList<String>();
								for(int i=0; i<selected.length; i++) {
									if(selected[i]) {
										selections.add(mOperations[i]);
									}
								}
								authority.authorities = StringUtils.join(selections, AuthorityDao.SEPARATOR);
								try {
									mAuthorityDao.update(authority);
								} catch (Exception e) {
									mToastHelper.show(e.getMessage());
								}
								mAdapter.notifyDataSetChanged();
							}
						};
				builder.setPositiveButton("确定", btnListener);
				builder.show();
			}
		});
		mLvList.setAdapter(mAdapter);
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
				convertView = LayoutInflater.from(BizAuthorityActivity.this).inflate(R.layout.authority_item, null);

				TextView tvUser = (TextView) convertView.findViewById(R.id.tv_authority_item_username);
				TextView tvAuthorities = (TextView) convertView.findViewById(R.id.tv_authority_item_authorities);

				viewHolder = new ViewHolder();
				viewHolder.user = tvUser;
				viewHolder.authorities = tvAuthorities;

				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Authority authority = mAuthorities.get(position);
			viewHolder.user.setText(authority.username);
			viewHolder.authorities.setText(authority.authorities == null ? "" : authority.authorities.replace(AuthorityDao.SEPARATOR, " | "));

			return convertView;
		}

		private class ViewHolder{
			private TextView user;
			private TextView authorities;
		}
	}
}
