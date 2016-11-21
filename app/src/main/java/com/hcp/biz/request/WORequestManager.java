package com.hcp.biz.request;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hcp.common.DateFormat;
import com.hcp.common.Device;
import com.hcp.entities.CUX_WIP_TRANSACTION_INT;
import com.hcp.entities.Item;
import com.hcp.entities.LotQuantityInWarehouse;
import com.hcp.entities.OperationQuery;
import com.hcp.entities.Organization;
import com.hcp.entities.ProcedureTransfer;
import com.hcp.entities.SegmentRemaining;
import com.hcp.entities.SubInventoryTransaction;
import com.hcp.entities.TransactionRecord;
import com.hcp.entities.WipTransactionCompletion;
import com.hcp.entities.WorkOrder;
import com.hcp.entities.WorkOrder2;
import com.hcp.entities.WorkOrder3;
import com.hcp.entities.WorkOrderSimple;
import com.hcp.http.RequestUtil;
import com.hcp.intraware.entity.ISPAccountAliasReceipt;
import com.hcp.intraware.entity.ISPBill;
import com.hcp.intraware.entity.ISPComponent;
import com.hcp.intraware.entity.ISPLocator;
import com.hcp.intraware.entity.ISPPurchaseReceive;
import com.hcp.intraware.entity.ISPPurchaseReceiveLocator;
import com.hcp.intraware.entity.ISPPurchaseReceiveOrder;
import com.hcp.intraware.entity.ISPPurchaseReceiveSubInventory;
import com.hcp.intraware.entity.ISPReason;
import com.hcp.intraware.entity.ISPSubInventory;
import com.hcp.intraware.entity.ISPSubInventoryTransferComponent;
import com.hcp.intraware.entity.ISPSubInventoryTransferLocator;
import com.hcp.intraware.entity.ISPSubInventoryTransferLotNo;
import com.hcp.intraware.entity.ISPSubInventoryTransferProjectNo;
import com.hcp.intraware.entity.ISPSubInventoryTransferSubInventory;
import com.hcp.intraware.entity.ISPSubInventoryTransferWip;
import com.hcp.intraware.entity.ISPSupplyType;
import com.hcp.intraware.entity.ISPWip;
import com.hcp.intraware.entity.ISPWipIssueSubInventory;
import com.hcp.intraware.entity.ISPWipQueryItem;
import com.hcp.intraware.entity.ISPWipRetrunSubInventory;
import com.hcp.intraware.entity.ISPWipReturnLocator;
import com.hcp.stocktaking.entity.InventoryCompare;
import com.hcp.stocktaking.entity.Locator;
import com.hcp.stocktaking.entity.Stock;
import com.hcp.stocktaking.entity.SubInventory;
import com.hcp.stocktaking.entity.T_Inventory_Card;
import com.hcp.stocktaking.entity.T_Inventory_Snapshot;
import com.hcp.util.AppCache;
import com.hcp.util.AppConfig;

import org.apache.commons.lang3.StringUtils;
import org.ksoap2.serialization.PropertyInfo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WORequestManager {
	public static final String TRANS_RETURN = "WIP Return";
	public static final String TRANS_ISSUE = "WIP Issue";

	private static WORequestManager mInstance;

	private static Object mInstanceLocker = new Object();
	private AppConfig mAppConfig;

	private Context mContext;

	private WORequestManager(Context context) {
		mContext = context.getApplicationContext();

		mAppConfig = AppConfig.getInstance(context);
	}

	public static WORequestManager getInstance(Context context) {
		if (mInstance == null) {
			synchronized (mInstanceLocker) {
				if (mInstance == null) {
					mInstance = new WORequestManager(context);
				}
			}
		}
		return mInstance;
	}

	public List<WorkOrder> getWorkOrder(String orderCode, String componentCode)
			throws Exception {
		List<WorkOrder> order = null;
		PropertyInfo infoOrder = new PropertyInfo();
		infoOrder.setName("ordercode");
		infoOrder.setValue(orderCode);

		PropertyInfo infoComponet = new PropertyInfo();
		infoComponet.setName("componentcode");
		infoComponet.setValue(componentCode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { infoOrder,
				infoComponet };

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetWorkOrder");

		if (!TextUtils.isEmpty(result)
				&& !RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(result)) {
			order = JSONObject.parseArray(result, WorkOrder.class);
		} else {
			throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
		}

		return order;
	}

	public List<WorkOrder2> getWorkOrderOnQuery(String orderCode,
			String componentCode, String componentName, boolean containsClosed) throws Exception {
		List<WorkOrder2> order = null;
		PropertyInfo infoOrder = new PropertyInfo();
		infoOrder.setName("ordercode");
		infoOrder.setValue(orderCode);

		PropertyInfo infoComponet = new PropertyInfo();
		infoComponet.setName("componentcode");
		infoComponet.setValue(componentCode);
		
		PropertyInfo componentNameInfo = new PropertyInfo();
		componentNameInfo.setName("componentname");
		componentNameInfo.setValue(componentName);
		
		PropertyInfo isClosedInfo = new PropertyInfo();
		isClosedInfo.setName("containsClosed");
		isClosedInfo.setValue(containsClosed);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { infoOrder,
				infoComponet, componentNameInfo, isClosedInfo };

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetWorkOrderOnQuery");

		if (!TextUtils.isEmpty(result)
				&& !RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(result)) {
			order = JSONObject.parseArray(result, WorkOrder2.class);
		} else {
			throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
		}

		return order;
	}

	public List<WorkOrderSimple> getWorkOrderByComponent(String componentCode)
			throws Exception {

		return getWorkOrderByComponent(componentCode, null, null, null);
	}
	
	public List<WorkOrderSimple> getWorkOrderByComponent(String componentCode, String organization, String startTime, String endTime)
			throws Exception {
		List<WorkOrderSimple> order = null;
		
		String methodName = "GetWorkOrderByComponent2";

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("componentcode");
		componentInfo.setValue(componentCode == null ? "" : componentCode);

		PropertyInfo startTimeInfo = new PropertyInfo();
		startTimeInfo.setName("starttime");
		startTimeInfo.setValue(startTime == null ? "" : startTime);
		
		PropertyInfo endTimeInfo = new PropertyInfo();
		endTimeInfo.setName("endtime");
		endTimeInfo.setValue(endTime == null ? "" : endTime);
		
		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization == null ? "" : organization);
		
		PropertyInfo[] propertyInfos = new PropertyInfo[]{componentInfo, startTimeInfo, endTimeInfo, orgInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, methodName);

		if (!TextUtils.isEmpty(result)
				&& !RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND.equals(result)) {
			order = JSONObject.parseArray(result, WorkOrderSimple.class);
		} else {
			throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
		}

		return order;
	}

	public String submitTransactionInt(
			List<CUX_WIP_TRANSACTION_INT> transactionInts) throws Exception {
		String tranIntRequestStr = JSONObject.toJSONString(transactionInts,
				SerializerFeature.DisableCircularReferenceDetect);
		PropertyInfo tranIntInfo = new PropertyInfo();
		tranIntInfo.setName("transactionInt");
		tranIntInfo.setValue(tranIntRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { tranIntInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"TransactionIntOnWS");
	}

	public List<LotQuantityInWarehouse> getLotQuantityInWarehouses(
			String organization, String subInventory, String componentCode, String componentName, String projectNumber, boolean projectContains)
			throws Exception {
		List<LotQuantityInWarehouse> lots = null;

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo compInfo = new PropertyInfo();
		compInfo.setName("componentcode");
		compInfo.setValue(componentCode);
		
		PropertyInfo compNameInfo = new PropertyInfo();
		compNameInfo.setName("componentname");
		compNameInfo.setValue(componentName);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subInventory);
		
		PropertyInfo pmInfo = new PropertyInfo();
		pmInfo.setName("projectNumber");
		pmInfo.setValue(projectNumber);
		
		PropertyInfo projectContainsInfo = new PropertyInfo();
		projectContainsInfo.setName("projectContains");
		projectContainsInfo.setValue(projectContains);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { orgInfo, compInfo, compNameInfo,
				subInfo, pmInfo, projectContainsInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetLotQuantityInWarehouse");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				lots = JSONObject.parseArray(result,
						LotQuantityInWarehouse.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return lots;
	}

	public WorkOrder3 getWorkOrderOnCompletion(String orderCode)
			throws Exception {
		WorkOrder3 comp = null;

		PropertyInfo codeInfo = new PropertyInfo();
		codeInfo.setName("ordercode");
		codeInfo.setValue(orderCode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { codeInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetWorkOrderOnCompletion");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				comp = JSONObject.parseObject(result,
						WorkOrder3.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return comp;
	}
	
	public List<TransactionRecord> getTransactionRecord(String subInventory, String orderCode, String segment, String startTime, String endTime)
			throws Exception {
		List<TransactionRecord> list = null;
		
		PropertyInfo subInventoryInfo = new PropertyInfo();
		subInventoryInfo.setName("subinventory");
		subInventoryInfo.setValue(StringUtils.trimToEmpty(subInventory));

		PropertyInfo codeInfo = new PropertyInfo();
		codeInfo.setName("ordercode");
		codeInfo.setValue(StringUtils.trimToEmpty(orderCode));
		
		PropertyInfo segmentInfo = new PropertyInfo();
		segmentInfo.setName("segment");
		segmentInfo.setValue(StringUtils.trimToEmpty(segment));
		
		PropertyInfo startTimeInfo = new PropertyInfo();
		startTimeInfo.setName("starttime");
		startTimeInfo.setValue(StringUtils.trimToEmpty(startTime));
		
		PropertyInfo endTimeInfo = new PropertyInfo();
		endTimeInfo.setName("endtime");
		endTimeInfo.setValue(StringUtils.trimToEmpty(endTime));

		PropertyInfo[] propertyInfos = new PropertyInfo[] {subInventoryInfo, codeInfo, segmentInfo, startTimeInfo, endTimeInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetTransactionRecord");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				list = JSONObject.parseArray(result, TransactionRecord.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return list;
	}
	
	public List<WipTransactionCompletion> getWipCompletion(String orderCode)
			throws Exception {
		List<WipTransactionCompletion> wips = null;

		PropertyInfo codeInfo = new PropertyInfo();
		codeInfo.setName("ordercode");
		codeInfo.setValue(orderCode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { codeInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetWipCompletion");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				wips = JSONObject.parseArray(result,WipTransactionCompletion.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return wips;
	}

	public List<OperationQuery> getOperationSeq(String wipName)
			throws Exception {
		List<OperationQuery> wips = null;

		PropertyInfo codeInfo = new PropertyInfo();
		codeInfo.setName("wip");
		codeInfo.setValue(wipName);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { codeInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetOperationSeqByWipName");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				wips = JSONObject.parseArray(result,OperationQuery.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return wips;
	}
	
	public List<SegmentRemaining> getSegmentRemaing(String segment, String subinventory, String locator, String projectNumber)
			throws Exception {
		List<SegmentRemaining> segments = null;

		PropertyInfo segmentInfo = new PropertyInfo();
		segmentInfo.setName("segment");
		segmentInfo.setValue(segment);
		
		PropertyInfo subinventoryInfo = new PropertyInfo();
		subinventoryInfo.setName("subinventory");
		subinventoryInfo.setValue(subinventory);
		
		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);
		
		PropertyInfo projectInfo = new PropertyInfo();
		projectInfo.setName("projectnumber");
		projectInfo.setValue(projectNumber);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { segmentInfo, subinventoryInfo, locatorInfo, projectInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetSegmentRemaining");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				segments = JSONObject.parseArray(result, SegmentRemaining.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return segments;
	}

	public List<SegmentRemaining> getSegmentRemaing2(String segment, String subinventory, String locator, String projectNumber)
			throws Exception {
		List<SegmentRemaining> segments = null;

		PropertyInfo segmentInfo = new PropertyInfo();
		segmentInfo.setName("segment");
		segmentInfo.setValue(segment);

		PropertyInfo subinventoryInfo = new PropertyInfo();
		subinventoryInfo.setName("subinventory");
		subinventoryInfo.setValue(subinventory);

		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);

		PropertyInfo projectInfo = new PropertyInfo();
		projectInfo.setName("projectnumber");
		projectInfo.setValue(projectNumber);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { segmentInfo, subinventoryInfo, locatorInfo, projectInfo };
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				propertyInfos, "GetSegmentRemaining2");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				segments = JSONObject.parseArray(result, SegmentRemaining.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return segments;
	}

	public String submitTransactionComp(WipTransactionCompletion wip)
			throws Exception {
		String tranIntRequestStr = JSONObject.toJSONString(wip,
				SerializerFeature.DisableCircularReferenceDetect);
		PropertyInfo tranIntInfo = new PropertyInfo();
		tranIntInfo.setName("transactionComp");
		tranIntInfo.setValue(tranIntRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { tranIntInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"TransactionCompletion");
	}

	public String submitProcedure(ProcedureTransfer proce) throws Exception {
		String proceRequestStr = JSONObject.toJSONString(proce,
				SerializerFeature.DisableCircularReferenceDetect);
		PropertyInfo proceInfo = new PropertyInfo();
		proceInfo.setName("procedureTransfer");
		proceInfo.setValue(proceRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { proceInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"DoProcedureTransfer");
	}

	public CUX_WIP_TRANSACTION_INT createDefaultTrans(String transType) {
		CUX_WIP_TRANSACTION_INT trans = new CUX_WIP_TRANSACTION_INT();
		trans.InterfaceID = 0;
		trans.TransactionType = transType;
		trans.TransactionDate = DateFormat.defaultFormat(new Date(System.currentTimeMillis()));
		trans.ComponentSubinventory = "";
		trans.ComponentLocator = "";
		trans.Reason = "Trx From Mobile";
		trans.Reference = "Trx From Mobile : " + Device.getIMEI(mContext);
		trans.UpdateBy = AppCache.getInstance().getLoginUser()
				.toUpperCase(Locale.getDefault());

		return trans;
	}
	
	public String submitSubInventoryTransaction(List<SubInventoryTransaction> subTrans) throws Exception {
		String tranIntRequestStr = JSONObject.toJSONString(subTrans,
				SerializerFeature.DisableCircularReferenceDetect);
		
		PropertyInfo subTransInfo = new PropertyInfo();
		subTransInfo.setName("subinventorytransaction");
		subTransInfo.setValue(tranIntRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { subTransInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"SubInventoryTransaction");
	}
	
	public int getOperationCount(String wipName, int operationNumber) throws Exception{
		PropertyInfo wipNameInfo = new PropertyInfo();
		wipNameInfo.setName("wipname");
		wipNameInfo.setValue(wipName);
		
		PropertyInfo operatoinNumInfo = new PropertyInfo();
		operatoinNumInfo.setName("operationnumber");
		operatoinNumInfo.setValue(operationNumber);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { wipNameInfo,  operatoinNumInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"GetOperationCount");
		
		return Integer.parseInt(result);
	}

	public boolean isSameTypeSubInventory(String firstsub, String sencondsub) throws Exception{
		PropertyInfo firstSubInfo = new PropertyInfo();
		firstSubInfo.setName("subinventoryFirst");
		firstSubInfo.setValue(firstsub);

		PropertyInfo sencondSubInfo = new PropertyInfo();
		sencondSubInfo.setName("subinventorySecond");
		sencondSubInfo.setValue(sencondsub);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { firstSubInfo,  sencondSubInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"IsSameTypeSubInventory");

		return Boolean.parseBoolean(result);
	}

	public String getItemNoByBarcode(String barcode) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("barcode");
		barcodeInfo.setValue(barcode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"GetItemNoByBarcode");

		return result;
	}

	public Stock getStockByBarcode(String barcode) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("barcode");
		barcodeInfo.setValue(barcode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"GetStockByBarcode");

		Stock stock = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				stock = JSONObject.parseObject(result, Stock.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return stock;
	}

	public T_Inventory_Card getStockFromRecord(String barcode) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("barcode");
		barcodeInfo.setValue(barcode);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"GetStockFromRecord");

		T_Inventory_Card card = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				card = JSONObject.parseObject(result, T_Inventory_Card.class);
			} else {
				return null;
			}
		}
		return card;
	}

	public boolean submitInventoryCard(T_Inventory_Card card) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("card");
		barcodeInfo.setValue(JSON.toJSONString(card));

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"SubmitInventoryCard");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public boolean updateInventoryCard(T_Inventory_Card card) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("card");
		barcodeInfo.setValue(JSON.toJSONString(card));

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"UpdateInventoryCard");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public boolean IsVerifyComponentNo(String componentno) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("componentno");
		barcodeInfo.setValue(componentno);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"IsVerifyComponentNo");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public boolean isVerifyLotNo(String lotno) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("lotno");
		barcodeInfo.setValue(lotno);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"IsVerifyLotNo");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public boolean isVerifyProjectNo(String projectno) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("projectno");
		barcodeInfo.setValue(projectno);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"IsVerifyProjectNo");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public boolean IsLotNoOverTime(String lotno, String componentno) throws Exception{
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("lotno");
		barcodeInfo.setValue(lotno);

		PropertyInfo lotInfo = new PropertyInfo();
		lotInfo.setName("componentno");
		lotInfo.setValue(componentno);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { lotInfo, barcodeInfo};
		String result =  RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"IsLotNoOverTime");
		if (!TextUtils.isEmpty(result)) {
			return Boolean.parseBoolean(result);
		}
		return false;
	}

	public List<Locator> getLocators() throws Exception {
		List<Locator> locs = null;

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "GetLocators");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				locs = JSONObject.parseArray(result,Locator.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return locs;
	}

	public List<SubInventory> getSubInventories() throws Exception {
		List<SubInventory> subs = null;

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "GetSubInventories");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				subs = JSONObject.parseArray(result,SubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return subs;
	}

	public List<T_Inventory_Snapshot> getSnapShotInLimt(int offset, int limit)
			throws Exception {
		List<T_Inventory_Snapshot> wips = null;

		PropertyInfo offsetInfo = new PropertyInfo();
		offsetInfo.setName("offset");
		offsetInfo.setValue(offset);

		PropertyInfo limitInfo = new PropertyInfo();
		limitInfo.setName("limit");
		limitInfo.setValue(limit);

		PropertyInfo[] properties = new PropertyInfo[]{offsetInfo, limitInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				properties, "GetSnapShotInLimt");

		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				wips = JSONObject.parseArray(result, T_Inventory_Snapshot.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}

		return wips;
	}

	public boolean generateSnapShot() throws Exception {
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "GenerateSnapShot", 60 * 1000);
		boolean res = false;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Boolean.parseBoolean(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean hasSnapShot() throws Exception {
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "HasSnapShot");
		boolean res = false;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Boolean.parseBoolean(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public int getSnapShotCount() throws Exception {
		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "GetSnapShotCount");
		int res = 0;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Integer.parseInt(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public BigDecimal getInventoryCountFromSnapshot(String itemno, String subinventory, String projectno, String lotno, String locator) throws Exception {

		PropertyInfo itemnoInfo = new PropertyInfo();
		itemnoInfo.setName("itemno");
		itemnoInfo.setValue(itemno);

		PropertyInfo subinventoryInfo = new PropertyInfo();
		subinventoryInfo.setName("subinventory");
		subinventoryInfo.setValue(subinventory);

		PropertyInfo projectnoInfo = new PropertyInfo();
		projectnoInfo.setName("projectno");
		projectnoInfo.setValue(projectno);

		PropertyInfo lotnoInfo = new PropertyInfo();
		lotnoInfo.setName("lotno");
		lotnoInfo.setValue(lotno);

		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{itemnoInfo, subinventoryInfo, projectnoInfo, lotnoInfo, locatorInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetInventoryCountFromSnapshot");
		BigDecimal res = BigDecimal.ZERO;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = new BigDecimal(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public BigDecimal getInventoryCountFromInventoryCard(String itemno, String subinventory, String projectno, String lotno, String locator) throws Exception {

		PropertyInfo itemnoInfo = new PropertyInfo();
		itemnoInfo.setName("itemno");
		itemnoInfo.setValue(itemno);

		PropertyInfo subinventoryInfo = new PropertyInfo();
		subinventoryInfo.setName("subinventory");
		subinventoryInfo.setValue(subinventory);

		PropertyInfo projectnoInfo = new PropertyInfo();
		projectnoInfo.setName("projectno");
		projectnoInfo.setValue(projectno);

		PropertyInfo lotnoInfo = new PropertyInfo();
		lotnoInfo.setName("lotno");
		lotnoInfo.setValue(lotno);

		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{itemnoInfo, subinventoryInfo, projectnoInfo, lotnoInfo, locatorInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetInventoryCountFromInventoryCard");
		BigDecimal res = BigDecimal.ZERO;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = new BigDecimal(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<InventoryCompare> getInventoryCompare(String itemno, String subinventory, String projectno, String lotno, String locator) throws Exception {

		PropertyInfo itemnoInfo = new PropertyInfo();
		itemnoInfo.setName("itemno");
		itemnoInfo.setValue(itemno);

		PropertyInfo subinventoryInfo = new PropertyInfo();
		subinventoryInfo.setName("subinventory");
		subinventoryInfo.setValue(subinventory);

		PropertyInfo projectnoInfo = new PropertyInfo();
		projectnoInfo.setName("projectno");
		projectnoInfo.setValue(projectno);

		PropertyInfo lotnoInfo = new PropertyInfo();
		lotnoInfo.setName("lotno");
		lotnoInfo.setValue(lotno);

		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{itemnoInfo, subinventoryInfo, projectnoInfo, lotnoInfo, locatorInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetInventoryCompare");
		List<InventoryCompare> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result,InventoryCompare.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean ExistsBarcode(String barcode) throws Exception {
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("barcode");
		barcodeInfo.setValue(barcode);

		PropertyInfo[] infos = new PropertyInfo[]{barcodeInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ExsitsBarcode");
		boolean res = false;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Boolean.parseBoolean(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean isProjectControl(String itemno) throws Exception {
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("itemno");
		barcodeInfo.setValue(itemno);

		PropertyInfo[] infos = new PropertyInfo[]{barcodeInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "IsProjectControl");
		boolean res = false;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Boolean.parseBoolean(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean isLotControl(String itemno) throws Exception {
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("itemno");
		barcodeInfo.setValue(itemno);

		PropertyInfo[] infos = new PropertyInfo[]{barcodeInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "IsLotControl");
		boolean res = false;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = Boolean.parseBoolean(result);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public Item getItemByNo(String itemno) throws Exception {
		PropertyInfo barcodeInfo = new PropertyInfo();
		barcodeInfo.setName("itemno");
		barcodeInfo.setValue(itemno);

		PropertyInfo[] infos = new PropertyInfo[]{barcodeInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetItemByNo");
		Item res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, Item.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<Organization> getOrganizations() throws Exception {

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				null, "GetOrganizations");
		List<Organization> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, Organization.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventory> getISPSubInventories(String organization, String subInventory, String description) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subInventory);

		PropertyInfo desInfo = new PropertyInfo();
		desInfo.setName("description");
		desInfo.setValue(description);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo, desInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventories");
		List<ISPSubInventory> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPWipIssueSubInventory> getISPWipIssueSubInventories(String organization, String component, String projectno, String subinventory) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo proInfo = new PropertyInfo();
		proInfo.setName("projectno");
		proInfo.setValue(projectno);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, comInfo, proInfo, subInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWipIssueSubInventories");
		List<ISPWipIssueSubInventory> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPWipIssueSubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPWipRetrunSubInventory> getISPWipReturnSubInventories(String organization, String subinventory) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWipReturnSubInventories");
		List<ISPWipRetrunSubInventory> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPWipRetrunSubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPWipReturnLocator> getISPWipReturnLocators(String organization, String subinventory, String locator) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo locInfo = new PropertyInfo();
		locInfo.setName("locator");
		locInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo, locInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWipReturnLocators");
		List<ISPWipReturnLocator> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPWipReturnLocator.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPReason> getISPReasons(String name, String description) throws Exception{

		PropertyInfo nameInfo = new PropertyInfo();
		nameInfo.setName("name");
		nameInfo.setValue(name);

		PropertyInfo desInfo = new PropertyInfo();
		desInfo.setName("description");
		desInfo.setValue(description);

		PropertyInfo[] infos = new PropertyInfo[]{nameInfo, desInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPReasons");
		List<ISPReason> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPReason.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}


	public List<ISPLocator> getISPLocators(String organization, String subinventory, String filterSub, String filterLocator) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo filtSubInfo = new PropertyInfo();
		filtSubInfo.setName("filtersub");
		filtSubInfo.setValue(filterSub);

		PropertyInfo filtLocInfo = new PropertyInfo();
		filtLocInfo.setName("filterlocator");
		filtLocInfo.setValue(filterLocator);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo, filtSubInfo, filtLocInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPLocators");
		List<ISPLocator> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPLocator.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPBill> getISPBills(String organization, String bill, String component, String componentDescription) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo billInfo = new PropertyInfo();
		billInfo.setName("bill");
		billInfo.setValue(bill);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo desInfo = new PropertyInfo();
		desInfo.setName("componentdescription");
		desInfo.setValue(componentDescription);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, billInfo, comInfo, desInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPBills");
		List<ISPBill> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPBill.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}
	public List<ISPWip> getISPWips(String organization, String wip, String component, String compnentDescription) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo desInfo = new PropertyInfo();
		desInfo.setName("compnentDescription");
		desInfo.setValue(compnentDescription);


		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, comInfo, desInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWips");
		List<ISPWip> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPWip.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}


	public List<ISPComponent> getISPComponents(String organization, String wip, String component) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, comInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPComponents");
		List<ISPComponent> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPComponent.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean ispIsWip(String organization, String wip) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ISPIsWip");
		return Boolean.parseBoolean(result);
	}

	public boolean ispIsWipContainsComponent(String organization, String wip, String component) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("component");
		componentInfo.setValue(component);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, componentInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ISPIsWipContainsComponent");
		return Boolean.parseBoolean(result);
	}

	public boolean ispIsProjectControl(String organization, String component, String locator) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("component");
		componentInfo.setValue(component);

		PropertyInfo locatorInfo = new PropertyInfo();
		locatorInfo.setName("locator");
		locatorInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, componentInfo, locatorInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ISPIsProjectControl");
		return Boolean.parseBoolean(result);
	}

	public boolean ispIsQuantityAvailable(String organization, String wip, String component, String lotno, String quantity) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("component");
		componentInfo.setValue(component);

		PropertyInfo lotnoInfo = new PropertyInfo();
		lotnoInfo.setName("lotno");
		lotnoInfo.setValue(lotno);

		PropertyInfo quantityInfo = new PropertyInfo();
		quantityInfo.setName("quantity");
		quantityInfo.setValue(quantity);


		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, componentInfo, lotnoInfo, quantityInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ISPIsWipIssueQuantityAvailable");
		return Boolean.parseBoolean(result);
	}

	public ISPSupplyType getISPSupplyType(String organization, String wip, String component) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("component");
		componentInfo.setValue(component);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, componentInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSupplyType");

		ISPSupplyType res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, ISPSupplyType.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public String getISPTransactionDescription(String transType) throws Exception{

		PropertyInfo transInfo = new PropertyInfo();
		transInfo.setName("transType");
		transInfo.setValue(transType);

		PropertyInfo[] infos = new PropertyInfo[]{transInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPTransactionDescription");

		return result;
	}

	public String getISPSourceDescription(String organization, String sourceType) throws Exception{
		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo sourceInfo = new PropertyInfo();
		sourceInfo.setName("sourceType");
		sourceInfo.setValue(sourceType);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, sourceInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSourceDescription");

		return result;
	}

	public String getISPLocationCode(String organization) throws Exception{
		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPLocationCode");

		return result;
	}

	public String submitISPAccountAliasReceipt(ISPAccountAliasReceipt trans) throws Exception {
		String tranIntRequestStr = JSONObject.toJSONString(trans,
				SerializerFeature.DisableCircularReferenceDetect);
		PropertyInfo tranIntInfo = new PropertyInfo();
		tranIntInfo.setName("transaction");
		tranIntInfo.setValue(tranIntRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { tranIntInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"SubmitISPAccountAliasReceipt", 60 * 1000);
	}

	public CUX_WIP_TRANSACTION_INT getISPWipEntity(String organization, String wip, String component, String lotno) throws Exception {
		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo componentInfo = new PropertyInfo();
		componentInfo.setName("component");
		componentInfo.setValue(component);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo lotInfo = new PropertyInfo();
		lotInfo.setName("lotno");
		lotInfo.setValue(lotno);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, componentInfo, lotInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWipEntity");

		CUX_WIP_TRANSACTION_INT res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, CUX_WIP_TRANSACTION_INT.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferComponent> getISPSubInventoryTransferComponent(String organization, String component) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, comInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferComponent");

		List<ISPSubInventoryTransferComponent> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferComponent.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferLocator> getISPSubInventoryTransferLocator(String organization, String subinventory, String locator) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo locInfo = new PropertyInfo();
		locInfo.setName("locator");
		locInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo, locInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferLocator");

		List<ISPSubInventoryTransferLocator> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferLocator.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferSubInventory> getISPSubInventoryTransferSubInventory(String organization, String subinventory) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);


		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferSubInventory");

		List<ISPSubInventoryTransferSubInventory> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferSubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferProjectNo> getISPSubInventoryTransferProjectNo(String projectno) throws Exception{

		PropertyInfo proInfo = new PropertyInfo();
		proInfo.setName("projectno");
		proInfo.setValue(projectno);


		PropertyInfo[] infos = new PropertyInfo[]{proInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferLocatorProjectNo");

		List<ISPSubInventoryTransferProjectNo> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferProjectNo.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferWip> getISPSubInventoryTransferWips(String organization, String component, String projectno, String wip) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo proInfo = new PropertyInfo();
		proInfo.setName("projectno");
		proInfo.setValue(projectno);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, comInfo, proInfo, wipInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferWip");

		List<ISPSubInventoryTransferWip> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferWip.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPSubInventoryTransferLotNo> getISPSubInventoryTransferLotNos(String organization, String component, String subinventory, String locator, String projectno, String lotno) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo locInfo = new PropertyInfo();
		locInfo.setName("locator");
		locInfo.setValue(locator);

		PropertyInfo proInfo = new PropertyInfo();
		proInfo.setName("projectno");
		proInfo.setValue(projectno);

		PropertyInfo lotInfo = new PropertyInfo();
		lotInfo.setName("lotno");
		lotInfo.setValue(lotno);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, comInfo, subInfo, locInfo, proInfo, lotInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPSubInventoryTransferLotNo");

		List<ISPSubInventoryTransferLotNo> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPSubInventoryTransferLotNo.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public boolean ispIsSubInventoryTransferQuantityEnough(String organization,
														   String wip,
														   String component,
														   String lotno,
														   String projectno,
														   String quantity) throws Exception {

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo lotInfo = new PropertyInfo();
		lotInfo.setName("lotno");
		lotInfo.setValue(lotno);

		PropertyInfo proInfo = new PropertyInfo();
		proInfo.setName("projectno");
		proInfo.setValue(projectno);

		PropertyInfo qtyInfo = new PropertyInfo();
		qtyInfo.setName("quantity");
		qtyInfo.setValue(quantity);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, wipInfo, comInfo, lotInfo, proInfo, qtyInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "ISPIsSubInventoryTransferQuantityEnough");
		return Boolean.parseBoolean(result);
	}

	public List<ISPPurchaseReceiveOrder> getISPPurchaseReceiveOrders(String organization, String order) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo orderInfo = new PropertyInfo();
		orderInfo.setName("order");
		orderInfo.setValue(order);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, orderInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveOrders");
		List<ISPPurchaseReceiveOrder> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPPurchaseReceiveOrder.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPPurchaseReceiveSubInventory> getISPPurchaseReceiveSubInventory(String organization, String subinventory) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveSubInventory");

		List<ISPPurchaseReceiveSubInventory> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPPurchaseReceiveSubInventory.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPPurchaseReceiveLocator> getISPPurchaseReceiveLocator(String organization, String subinventory, String locator) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo subInfo = new PropertyInfo();
		subInfo.setName("subinventory");
		subInfo.setValue(subinventory);

		PropertyInfo locInfo = new PropertyInfo();
		locInfo.setName("locator");
		locInfo.setValue(locator);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, subInfo, locInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveLocator");

		List<ISPPurchaseReceiveLocator> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPPurchaseReceiveLocator.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public String getISPPurchaseReceiveOrganizationName(String organization) throws Exception{

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveOrganizationName");
		return result;
	}

	public String submitISPPurchaseReceive(ISPPurchaseReceive receive) throws Exception {
		String receiveRequestStr = JSONObject.toJSONString(receive,
				SerializerFeature.DisableCircularReferenceDetect);
		PropertyInfo receiveInfo = new PropertyInfo();
		receiveInfo.setName("purchasereceive");
		receiveInfo.setValue(receiveRequestStr);

		PropertyInfo[] propertyInfos = new PropertyInfo[] { receiveInfo };
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), propertyInfos,
				"SubmitPurchaseReceive", 60 * 1000);
	}

	public CUX_WIP_TRANSACTION_INT getISPPurchaseReceiveWipIssueData(String purchaseorder, String organization, String component) throws Exception{

		PropertyInfo purInfo = new PropertyInfo();
		purInfo.setName("purchaseorder");
		purInfo.setValue(purchaseorder);

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo comInfo = new PropertyInfo();
		comInfo.setName("component");
		comInfo.setValue(component);

		PropertyInfo[] infos = new PropertyInfo[]{purInfo, orgInfo, comInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveWipIssueData");

		CUX_WIP_TRANSACTION_INT res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, CUX_WIP_TRANSACTION_INT.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public ProcedureTransfer getISPPurchaseReceiveOperationTransaction(String purchaseorder, String organization) throws Exception{

		PropertyInfo purInfo = new PropertyInfo();
		purInfo.setName("purchaseorder");
		purInfo.setValue(purchaseorder);

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, orgInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveOperationTransaction");

		ProcedureTransfer res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, ProcedureTransfer.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public WipTransactionCompletion GetISPPurchaseReceiveWipCompletion(String purchaseorder, String organization, String lotno) throws Exception{

		PropertyInfo purInfo = new PropertyInfo();
		purInfo.setName("purchaseorder");
		purInfo.setValue(purchaseorder);

		PropertyInfo orgInfo = new PropertyInfo();
		orgInfo.setName("organization");
		orgInfo.setValue(organization);

		PropertyInfo lotInfo = new PropertyInfo();
		lotInfo.setName("lotno");
		lotInfo.setValue(lotno);

		PropertyInfo[] infos = new PropertyInfo[]{orgInfo, orgInfo, lotInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPPurchaseReceiveOperationTransaction");

		WipTransactionCompletion res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseObject(result, WipTransactionCompletion.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public List<ISPWipQueryItem> getISPWipQueryItems(String wip) throws Exception{

		PropertyInfo wipInfo = new PropertyInfo();
		wipInfo.setName("wip");
		wipInfo.setValue(wip);

		PropertyInfo[] infos = new PropertyInfo[]{wipInfo};

		String result = RequestUtil.doRequest(mAppConfig.getServerUrl(),
				infos, "GetISPWipQueryItems", 60 * 1000);

		List<ISPWipQueryItem> res = null;
		if (!TextUtils.isEmpty(result)) {
			if (!result.equals(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND)) {
				res = JSONObject.parseArray(result, ISPWipQueryItem.class);
			} else {
				throw new Exception(RequestUtil.RESPONSE_MSG_ERROR_NOT_FOUND);
			}
		}
		return res;
	}

	public String test() throws Exception  {
		
		return RequestUtil.doRequest(mAppConfig.getServerUrl(), null,
				"Test");
	}
}
