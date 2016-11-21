package com.hcp.entities;

import java.math.BigDecimal;

public class WorkOrderOnCompletion {

	/**
	 * 组织
	 */
	public String Organization;

	/**
	 * 工单号
	 */
	public String WipEntityName;

	/**
	 * 状态
	 */
	public String Meaning;

	/**
	 * 装配件
	 */
	public String ConcatenatedSegments;

	/**
	 * 装配件说明
	 */
	public String Description;

	/**
	 * 工单类型
	 */
	public String ClassCode;

	/**
	 * 开始数量
	 */
	public BigDecimal StartQuantity;

	/**
	 * 已完工量
	 */
	public BigDecimal QuantityCompleted;

	/**
	 * 未完工量
	 */
	public BigDecimal QuantityRemaining;

	/**
	 * 计划开始时间
	 */
	public String ScheduledStartDate;

	/**
	 * 计划完成时间
	 */
	public String ScheduledCompletionDate;

	/**
	 * 默认完成子库存
	 */
	public String CompletionSubinventory;

	/**
	 * 默认完成货位
	 */
	public String CompletionSegment;

	/**
	 * 项目号
	 */
	public String ProjectNumber;

	/**
	 * 项目名称
	 */
	public String ProjectName;

	/**
	 * 批次号
	 */
	public String LotNumber;

	/**
	 * 备注
	 */
	public String Remark;
}
