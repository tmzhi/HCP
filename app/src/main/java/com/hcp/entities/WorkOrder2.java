package com.hcp.entities;

/**
 * 相较于WorkOrder，精简了组件批次(ComponentLotNumber)与组件批次数量(ComponentLotQuantity).
 */

import java.math.BigDecimal;

public class WorkOrder2 {

	/**
	 * 组织
	 */
	public String Organization;

	/**
	 * 发放日期
	 */
	public String DateReleased;

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
	 * 装配件单位
	 */
	public String PrimaryUomCode;

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

	/**
	 * 组件编码
	 */
	public String Segment1;

	/**
	 * 组件追溯
	 */
	public String ComponentPeggingFlag;

	/**
	 * 组件描述
	 */
	public String ItemDescription;

	/**
	 * 单位
	 */
	public String ItemPrimaryUomCode;

	/**
	 * 批次控制
	 */
	public String LotControl;

	/**
	 * 部门
	 */
	public String DepartmentCode;

	/**
	 * 供应方式
	 */
	public String WipSupplyMeaning;

	/**
	 * 基准
	 */
	public String BasisType;

	/**
	 * 单位用量
	 */
	public BigDecimal QuantityPerAssembly;

	/**
	 * 产生率
	 */
	public BigDecimal ComponentYieldFactor;

	/**
	 * 需求总量
	 */
	public BigDecimal RequiredQuantity;

	/**
	 * 已发量
	 */
	public BigDecimal QuantityIssued;

	/**
	 * 未发量
	 */
	public BigDecimal QuantityOpen;

	/**
	 * 倒冲子库存
	 */
	public String SupplySubinventory;

	/**
	 * 倒冲货位
	 */
	public String SupplySegment;

	/**
	 * 机台号码
	 */
	public String MachineNumber;

	/**
	 * 机台名称
	 */
	public String MachineDescription;

	/**
	 * 工序号
	 */
	public String SeqNumber;
}
