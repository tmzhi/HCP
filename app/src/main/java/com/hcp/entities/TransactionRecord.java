package com.hcp.entities;

import java.math.BigDecimal;

public class TransactionRecord {

    /**
     * 交易类型ID
     */
    public String TransactionID;

    /**
     * 组织编号
     */
    public String OrganizationID;

    /**
     * 组织编号
     */
    public String OrganizationCode;

    /**
     * 交易类型
     */
    public String TransactionType;

    /**
     * 工单号
     */
    public String WipEntityName;

    /**
     * 子库
     */
    public String SubInventory;

    /**
     * 货位ID
     */
    public String LocatorID;

    /**
     * 货位
     */
    public String Locator;

    /**
     * 料号
     */
    public String Segment;

    /**
     * 物料名称
     */
    public String Description;

    /**
     * 硬追溯
     */
    public String EndAssemblyPeggingFlag;

    /**
     * 交易单位
     */
    public String TransactionUOM;

    /**
     * 交易数量
     */
    public BigDecimal TransactionQuantity;

    /**
     * 主要数量
     */
    public BigDecimal PrimaryQuantity;

    /**
     * 引用
     */
    public String TransactionReference;

    /**
     * 交易时间
     */
    public String TransactionDate;

    /**
     * 项目号
     */
    public String ProjectNumber;

    /**
     * 批次号
     */
    public String LotNumber;

    /**
     * 批次主要数量
     */
    public BigDecimal LotPrimaryQuantity;

    /**
     * 批次交易数量
     */
    public BigDecimal LotTransactionQuantity;
}
