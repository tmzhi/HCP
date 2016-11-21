package com.hcp.entities;

import java.math.BigDecimal;

public class LotQuantityInWarehouse {

	/**
	 * 组织编号
	 */
	public String OrganizationCode;

	/**
	 * 组件编号
	 */
	public String Segment1;

	/**
	 * 组件名称
	 */
	public String ItemDescription;

	/**
	 * 单位
	 */
	public String PrimaryUOM;

	/**
	 * 类别
	 */
	public String ConcatenatedSegments;

	/**
	 * 批次控制
	 */
	public String LotControl;

	/**
	 * 子库
	 */
	public String SubInventoryCode;

	/**
	 * 子库说明
	 */
	public String SubInventoryDescription;

	/**
	 * 货位
	 */
	public String SupplySegment;

	/**
	 * 货位编号
	 */
	public String LocatorSegment2;

	/**
	 * 批次号
	 */
	public String LotNumber;

	/**
	 * 批次生成日期
	 */
	public String LotDate;

	/**
	 * 现有量
	 */
	public BigDecimal StandingCrop;
}
