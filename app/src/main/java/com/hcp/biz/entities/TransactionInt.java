package com.hcp.biz.entities;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionInt {
	
	public String ori_wip_entity_name;
	public String component_code;
	public String component_name;
	public BigDecimal ori_required_quantity;
	public BigDecimal remaining_quantity;
	public String wip_entity_name;
	public BigDecimal required_quantity;
	public BigDecimal transaction_quantity;
	public String create_by;
	public Date create_time;
	
	
}
