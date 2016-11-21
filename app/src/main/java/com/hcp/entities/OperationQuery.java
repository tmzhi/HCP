package com.hcp.entities;

import java.math.BigDecimal;

public class OperationQuery {
	public String Orgnization;

	public String WipName;

	public String Meaning;

	public String Segment1;

	public String Description;

	public BigDecimal WoStartQuantity;

	public BigDecimal WoCompletedQuantity;

	public String OperationSeqNum;

	public BigDecimal InQueueQuantity;

	public BigDecimal RunningQuantity;

	public BigDecimal WaitingToMoveQuantity;

	public BigDecimal RejectedQuantity;

	public BigDecimal ScrappedQuantity;

	public BigDecimal CompletedQuantity;
}
