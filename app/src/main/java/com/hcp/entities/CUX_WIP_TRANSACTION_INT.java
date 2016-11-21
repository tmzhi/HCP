package com.hcp.entities;

import java.math.BigDecimal;

public class CUX_WIP_TRANSACTION_INT {

	/**
	 * 组号
	 */
	public long GroupID;

	/**
	 * 接口号
	 */
	public int InterfaceID;

	/**
	 * 组织代码
	 */
	public String OrganizationCode;

	/**
	 * 工单号码
	 */
	public String WipEntityName;

	/**
	 * 项目号
	 */
	public String ProjectNumber;

	/**
	 * 工单类型
	 */
	public String JobType;

	/**
	 * 装配件料号
	 */
	public String Assembly;

	/**
	 * 装配件批号
	 */
	public String AssemblyLotNumber;

	/**
	 * 装配件单位
	 */
	public String AssemblyUomCode;

	/**
	 * 装配件计划生产量
	 */
	public BigDecimal StartQuantity;

	/**
	 * 事务处理类型
	 */
	public String TransactionType;

	/**
	 * 事务处理时间
	 */
	public String TransactionDate;

	/**
	 * 组件料号
	 */
	public String ComponentItem;

	/**
	 * 组件发料单位
	 */
	public String ComponentUomCode;

	/**
	 * 组件批号
	 */
	public String ComponentLotNumber;

	/**
	 * 组件理论需求量
	 */
	public BigDecimal RequiredQuantity;

	/**
	 * 组件事务处理数量
	 */
	public BigDecimal TransactionQuantity;

	/**
	 * 组件事务处理子库存
	 */
	public String ComponentSubinventory;

	/**
	 * 组件货位
	 */
	public String ComponentLocator;

	/**
	 * 工序编号
	 */
	public String OpSeq;

	/**
	 * 部门
	 */
	public String Department;

	/**
	 * 原因
	 */
	public String Reason;

	/**
	 * 参考
	 */
	public String Reference;

	/**
	 * 操作人员
	 */
	public String UpdateBy;
}
