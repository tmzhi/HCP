package com.hcp.stocktaking.biz;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.stocktaking.entity.T_Inventory_Snapshot;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.wo.R;
import com.seuic.listgrid.GridColumn;
import com.seuic.listgrid.ListGrid;
import com.seuic.listgrid.ListGrid.ColumnType;
import com.seuic.listgrid.ListGrid.ListRow;

import java.util.ArrayList;
import java.util.List;

public class GetSnapShotActivity extends ScannerActivity {

	private final int PER_COUNT = 50;

	private Button mBtnGet;

	private ListGrid mGrid;
	private EditText mEdtPageCount;
	private TextView mTvTotalPage;
	private TextView mTvCount;

	private ProgressDialog mProgressDialog;

	private LinearLayout mGridContainer;
	private WORequestManager mRequestManager;

	private List<T_Inventory_Snapshot> mSnapshots;

	private int mPages;
	private int mTotalCount;
	private int mCurPage;
	private int mPageCount;

	private ImageButton mImbPre;
	private ImageButton mImbNext;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snapshot);

		mRequestManager = WORequestManager.getInstance(mInstance);

		mBtnGet = (Button) findViewById(R.id.btn_snapshot_get);
		mBtnGet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getSnapshot();
			}
		});

		mGridContainer = (LinearLayout) findViewById(R.id.lin_snapshot_container);

		mEdtPageCount = (EditText) findViewById(R.id.edt_snapshot_page_count);
		mEdtPageCount.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtPageCount.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {

					if (mPageCount == 0) {
						mToastHelper.show("请先获取盘点快照!");
						return false;
					}

					String pageStr = mEdtPageCount.getText().toString();
					if (TextUtils.isEmpty(pageStr)) {
						mToastHelper.show("请输入页号");
						return false;
					}

					int page = Integer.parseInt(pageStr);
					if (page > mPageCount) {
						page = mPageCount;
					} else if (page < 1) {
						page = 1;
					}

					getSnapshot(page);
				}
				return false;
			}
		});
		mTvCount = (TextView) findViewById(R.id.tv_snapshot_count);

		mTvTotalPage = (TextView) findViewById(R.id.tv_snapshot_total_page);

		mImbNext = (ImageButton) findViewById(R.id.imb_snapshot_next);
		mImbNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mPageCount < 1){
					return;
				}

				int page = mCurPage;
				page++;

				if(page > mPageCount){
					page = mPageCount;
				}

				getSnapshot(page);
			}
		});
		mImbPre = (ImageButton) findViewById(R.id.imb_snapshot_pre);
		mImbPre.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mPageCount < 1){
					return;
				}
				int page = mCurPage;
				page--;

				if(page < 1){
					page = 1;
				}

				getSnapshot(page);
			}
		});

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;

		List<GridColumn> columns = new ArrayList<GridColumn>();
		columns.add(new GridColumn("料件编号", (int) (150 * density)));
		columns.add(new GridColumn("子库", (int) (100 * density)));
		columns.add(new GridColumn("货位", (int) (100 * density)));
		columns.add(new GridColumn("项目号", (int) (100 * density)));
		columns.add(new GridColumn("批次号", (int) (100 * density)));
		columns.add(new GridColumn("现有量", (int) (140 * density)));
		mGrid = new ListGrid(this, columns, ColumnType.WIDTH);
		mGrid.setHeaderTextSize(14);
		mGrid.setCellTextSize(12);
		mGrid.setFullRowSelect(true);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mGrid.setPadding(0, 0, 0, 0);
		mGrid.setLayoutParams(lp);
		mGridContainer.addView(mGrid);

	}

	private void getSnapshot(){

		if(mProgressDialog != null && mProgressDialog.isShowing()){
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(mInstance, "获取", "正在获取盘点快照...");
		mProgressDialog.show();

		new Thread(new Runnable() {
			@Override
			public void run() {

				try{
					if(!generateSnapshot()) {
						return;
					}

					if(!getSnapshotCount()){
						return;
					}

					if(!getSnapshotInLimit(1, PER_COUNT)){
						return;
					}

				}catch (Exception ex){

				}finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
						}
					});
				}

			}
		}).start();

	}

	private boolean generateSnapshot(){
		try {
			if(!mRequestManager.hasSnapShot()){
				changeDialogMsg("正在生成盘点快照...");
				mRequestManager.generateSnapShot();
			}
			return true;
		} catch (Exception e) {
			showErrorMsg(String.format("获取快照异常:%s", e.getMessage()));
			return false;
		}
	}

	private boolean getSnapshotCount(){
		try {
			mTotalCount = mRequestManager.getSnapShotCount();
			return true;
		} catch (Exception e) {
			showErrorMsg(String.format("获取快照异常:%s", e.getMessage()));
			return false;
		}
	}

	private void getSnapshotByPage(int page){
		String result = snapshotCheck();
		if(TextUtils.isEmpty(result)){
			mToastHelper.show(result);
			return;
		}
	}

	private String snapshotCheck(){

		if(mPageCount < 1){
			return "请先获取盘点快照!";
		}

		return null;
	}

	private void getSnapshot(final int page) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			return;
		}

		mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(mInstance, "请求", "正在获取盘点快照!");
		mProgressDialog.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSnapshots = mRequestManager.getSnapShotInLimt(page, PER_COUNT);

					showSnaps();

					setCount(page);
				} catch (Exception e) {
					showErrorMsg(String.format("获取盘点快照失败:%s", e.getMessage()));
				} finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
						}
					});
				}
			}
		}).start();
	}

	private boolean getSnapshotInLimit(final int offset, int limit){
		try {
			mSnapshots = mRequestManager.getSnapShotInLimt(offset, limit);

			showSnaps();

			setCount(offset);

			return true;
		} catch (Exception e) {
			showErrorMsg(String.format("获取快照异常:%s", e.getMessage()));
			return false;
		}
	}

	private void setCount(final int page){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mCurPage = page;
				mTvCount.setText(String.format("总计:%d", mTotalCount));
				mPageCount = mTotalCount / PER_COUNT + (mTotalCount % PER_COUNT == 0 ? 0 : 1);
				mTvTotalPage.setText(mPageCount + "");
				mEdtPageCount.setText(mCurPage + "");
			}
		});

	}

	private void showSnaps(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mGrid.clearRows();
				for (T_Inventory_Snapshot item : mSnapshots) {
					ListRow row = mGrid.newRow();
					row.setValue(0, item.Item_No == null ? "" : item.Item_No);
					row.setValue(1, item.Sub_Inventory == null ? "" : item.Sub_Inventory);
					row.setValue(2, item.Locator == null ? "" : item.Locator);
					row.setValue(3, item.Lot_No == null ? "" : item.Lot_No);
					row.setValue(4, item.Project_No == null ? "" : item.Project_No);
					row.setValue(5, item.Quantity + "");

					mGrid.insertRow(row);
				}
			}
		});
	}

	private void changeDialogMsg(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.setMessage(msg);
				}
			}
		});
	}

	private void showErrorMsg(final String msg){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToastHelper.show(msg);

				hideProgressDialog();
			}
		});
	}
	private void hideProgressDialog(){
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

}
