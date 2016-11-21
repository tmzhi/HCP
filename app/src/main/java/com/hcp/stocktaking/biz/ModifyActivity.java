package com.hcp.stocktaking.biz;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hcp.biz.request.WORequestManager;
import com.hcp.common.Device;
import com.hcp.common.ProgressDialogUtil;
import com.hcp.common.activity.ScannerActivity;
import com.hcp.dao.LocatorDao;
import com.hcp.dao.SubInventoryDao;
import com.hcp.stocktaking.entity.T_Inventory_Card;
import com.hcp.ui.event.HideKeyBoardTouchEvent;
import com.hcp.ui.event.OnKeyDownEvent;
import com.hcp.wo.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class ModifyActivity extends ScannerActivity implements
		TextWatcher {

	private final int DO_REQUEST_ISSUE = 0;
	private final int DO_REQUEST_GET_DATA = 1;

	private EditText mEdtBarcode;

	private EditText mEdtProjectNo;
	private EditText mEdtLotNo;
	private TextView mTvItemName;
	private EditText mEdtItemNo;
	private EditText mEdtQuantity;
	private TextView mTvItemDescription;
	private EditText mEdtLocator;
	private EditText mEdtSubInventory;
	private TextView mTvUOM;

	private Button mBtnSubmit;

	private Resources mResources;

	private ProgressDialog mProgressDialog;

	private WORequestManager mRequestManager;

	private HideKeyBoardTouchEvent mHideKeyBoardTouchEvent = new HideKeyBoardTouchEvent();

	private T_Inventory_Card mCurStock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_taking_entering);

		((TextView)findViewById(R.id.tv_stock_taking_entering_title)).setText("修 改 盘 点 卡");

		mResources = getResources();
		mRequestManager = WORequestManager.getInstance(this);

		mEdtBarcode = (EditText) findViewById(R.id.edt_entering_barcode);
		mEdtBarcode.setOnTouchListener(mHideKeyBoardTouchEvent);
		mEdtBarcode.setOnKeyListener(new OnKeyDownEvent() {

			@Override
			public void onKeyDown(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					getStock();
				}
			}
		});
		mEdtBarcode.addTextChangedListener(this);

		mTvItemName = (TextView) findViewById(R.id.tv_entering_item_name);
		mEdtItemNo = (EditText) findViewById(R.id.edt_entering_item_no);
		mEdtItemNo.setOnTouchListener(mHideKeyBoardTouchEvent);

		mTvItemDescription = (TextView) findViewById(R.id.tv_entering_item_desc);
		mEdtProjectNo = (EditText) findViewById(R.id.edt_entering_project_no);
		mEdtLotNo = (EditText) findViewById(R.id.edt_entering_lot_no);
		mEdtQuantity = (EditText) findViewById(R.id.edt_entering_quantity);
		mEdtQuantity.setOnTouchListener(mHideKeyBoardTouchEvent);

		mEdtSubInventory = (EditText) findViewById(R.id.edt_entering_subinventory);
		mEdtLocator = (EditText) findViewById(R.id.edt_entering_locator);

		mBtnSubmit = (Button) findViewById(R.id.btn_entering_submit);
		mBtnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mTvUOM = (TextView) findViewById(R.id.tv_entering_item_uom);
	}

	private String errorMsg = null;
	private void getStock() {
		final String barcode = mEdtBarcode.getText().toString();

		if (TextUtils.isEmpty(barcode)) {
			mToastHelper.show("【条码】不得为空");
			return;
		}

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"条码获取", "正在进行条码解析...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				List<Object> result;
				Message msg = Message.obtain();
				msg.what = DO_REQUEST_GET_DATA;

				try {
					final T_Inventory_Card stock = mRequestManager.getStockFromRecord(barcode);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (stock != null) {
								mCurStock = stock;
								mEdtQuantity.setText(mCurStock.Quantity + "");
								mEdtItemNo.setText(mCurStock.Item_No);
								mTvItemName.setText(mCurStock.Item_Name);
								mTvItemDescription.setText(mCurStock.Item_Desc);
								mEdtProjectNo.setText(mCurStock.Project_No);
								mEdtLotNo.setText(mCurStock.Lot_No);
								mEdtLocator.setText(mCurStock.Locator);
								mEdtSubInventory.setText(mCurStock.Sub_Inventory);
								mTvUOM.setText(mCurStock.UOM);
							}else{
								mToastHelper.show("条码解析失败!");
							}
						}
					});
				} catch (Exception e) {
					mToastHelper.show("条码获取失败!");
				} finally {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		}).start();
	}

	private void submit() {
		String result = submitCheck();
		if(!TextUtils.isEmpty(result)){
			mToastHelper.show(result);
			return;
		}

		final T_Inventory_Card card = new T_Inventory_Card();
		card.BarCode = mEdtBarcode.getText().toString();
		card.Quantity = new BigDecimal(mEdtQuantity.getText().toString());
		card.Item_No = mEdtItemNo.getText().toString();
		card.Item_Desc = mTvItemDescription.getText().toString();
		card.Item_Name = mTvItemName.getText().toString();
		card.Locator= mEdtLocator.getText().toString();
		card.Sub_Inventory = mEdtSubInventory.getText().toString();
		card.Lot_No = mEdtLotNo.getText().toString();
		card.Project_No = mEdtProjectNo.getText().toString();
		card.Device_IMEI = Device.getIMEI(mInstance);
		card.Trx_Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
		card.UOM = mTvUOM.getText().toString();

		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialogUtil.createUnCanceledDialog(this,
					"提交", "【盘点卡】提交中...");
			mProgressDialog.show();
		}

		new Thread(new Runnable() {
			@Override
			public void run() {

				if(!checkComponentNo(card.Item_No)){
					return;
				}

				if(!isProjectControl(card.Item_No, card.Project_No)) {
					return;
				}

				if(!isLotControl(card.Item_No, card.Lot_No)) {
					return;
				}

				if(!TextUtils.isEmpty(card.Project_No) && !checkProjectNo(card.Project_No)){
					return;
				}

				if(!TextUtils.isEmpty(card.Lot_No) && !checkLotno(card.Lot_No) && checkLotnoOverTime(card.Lot_No, card.Item_No)) {
					return;
				}

				try {
					final boolean result = mRequestManager.updateInventoryCard(card);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(result){
								mToastHelper.show("提交成功!");
								clearValues();
								mEdtBarcode.setText("");
								mEdtBarcode.requestFocus();
							}else{
								mToastHelper.show("提交失败!");
							}
						}
					});
				} catch (Exception e) {
					errorMsg = e.getMessage();
				}finally {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(!TextUtils.isEmpty(errorMsg)){
								mToastHelper.show(errorMsg);
								errorMsg = null;
							}
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}
					});
				}
			}
		}).start();

	}

	private boolean checkLotno(String lotno){
		boolean result = false;
		try {
			result = mRequestManager.isVerifyLotNo(lotno);
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(!result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【批次】校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
							return;
						}

						mToastHelper.show("无效的【批次】!");
					}
				});
			}
		}

		return false;
	}

	private boolean checkComponentNo(String componentno){
		boolean result = false;
		try {
			result = mRequestManager.IsVerifyComponentNo(componentno);
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(!result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【料件编码】校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
							return;
						}

						mToastHelper.show("无效的【料件编码】!");
					}
				});
			}
		}

		return false;
	}

	private boolean isProjectControl(final String componentno, final String projectno){
		boolean result = false;
		try {
			boolean iscontrol = mRequestManager.isProjectControl(componentno);
			if(iscontrol && TextUtils.isEmpty(projectno)){
				result = false;
			}else if(!iscontrol && !TextUtils.isEmpty(projectno)){
				result = false;
			}else{
				result = true;
			}
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(!result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【项目管理】校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
							return;
						}

						if(TextUtils.isEmpty(projectno)){
							mToastHelper.show("该料件为项目管理,【项目号】不得为空!");
						}else{
							mToastHelper.show("该料件非项目管理,【项目号】应为空!");
						}
					}
				});
			}
		}

		return false;
	}

	private boolean isLotControl(final String componentno, final String lotno){
		boolean result = false;
		try {
			boolean iscontrol = mRequestManager.isLotControl(componentno);
			if(iscontrol && TextUtils.isEmpty(lotno)){
				result = false;
			}else if(!iscontrol && !TextUtils.isEmpty(lotno)){
				result = false;
			}else{
				result = true;
			}
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(!result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【批次控制】校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
							return;
						}

						if(TextUtils.isEmpty(lotno)){
							mToastHelper.show("该料件为批次控制,【批次号】不得为空!");
						}else{
							mToastHelper.show("该料件非批次控制,【批次号】应为空!");
						}
					}
				});
			}
		}

		return false;
	}

	private boolean checkProjectNo(String projectno){
		boolean result = false;
		try {
			result = mRequestManager.isVerifyProjectNo(projectno);
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(!result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【项目号】校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
							return;
						}

						mToastHelper.show("无效的【项目号】!");
					}
				});
			}
		}

		return false;
	}

	private boolean checkLotnoOverTime(String lotno, String component){
		boolean result = false;
		try {
			result = mRequestManager.IsLotNoOverTime(lotno, component);
			return result;
		} catch (Exception e) {
			errorMsg = e.getMessage();
		}finally {
			if(result){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mProgressDialog != null && mProgressDialog.isShowing()){
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}

						if(!TextUtils.isEmpty(errorMsg)){
							mToastHelper.show(String.format("【批次】有效期校验失败,请检查网络是否异常:%s", errorMsg));
							errorMsg = null;
						}

						mToastHelper.show("【批次】已超过有效期间!");
					}
				});
			}
		}

		return false;
	}

	private String submitCheck(){

		if(mCurStock == null){
			return "请先获取盘点卡信息!";
		}

		final String locator = mEdtLocator.getText().toString();
		if(TextUtils.isEmpty(locator)){
			return "请输入【货位】!";
		}

		try {
			if (!LocatorDao.getInstance(mInstance).existLocator(locator)) {
				return "无效的【货位】!";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final String subinventory = mEdtSubInventory.getText().toString();
		if(TextUtils.isEmpty(subinventory)){
			return "请输入【子库】!";
		}

		try {
			if(!SubInventoryDao.getInstance(mInstance).existSubInventories(subinventory)){
				mEdtSubInventory.setText(subinventory);
				return "无效的【子库】!";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		final String quantityStr = mEdtQuantity.getText().toString();
		if(TextUtils.isEmpty(quantityStr)){
			return "请输入【数量】!";
		}

		BigDecimal quantity = new BigDecimal(quantityStr);
		if(quantity.compareTo(BigDecimal.ZERO) == 0){
			return "【数量】不得等于0!";
		}

		final String itemno = mEdtItemNo.getText().toString();
		if(TextUtils.isEmpty(itemno)){
			return "请输入【料件编号】!";
		}


		return null;
	}

	private void clearValues() {

		mEdtLotNo.setText("");
		mEdtQuantity.setText("");
		mTvItemDescription.setText("");
		mEdtItemNo.setText("");
		mEdtProjectNo.setText("");
		mTvItemName.setText("");
		mEdtLocator.setText("");
		mEdtSubInventory.setText("");
		mTvUOM.setText("");
		mCurStock = null;
	}

	@Override
	protected void decodeCallback(String barcode) {

		try {
			if (LocatorDao.getInstance(mInstance).existLocator(barcode)) {
				mEdtLocator.setText(barcode);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if(SubInventoryDao.getInstance(mInstance).existSubInventories(barcode)){
				mEdtSubInventory.setText(barcode);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mEdtBarcode.setText(barcode);
		getStock();
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		clearValues();
	}
}
