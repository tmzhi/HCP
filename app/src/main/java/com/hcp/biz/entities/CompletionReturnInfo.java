package com.hcp.biz.entities;

import java.math.BigDecimal;
import java.util.Date;

public class CompletionReturnInfo {
	public String sub_inventory;
	public String wip_entity_name;
	public String class_code;
	public Date start_time;
	public Date completion_time;
	public String assembly_code;
	public String assembly_name;
	public BigDecimal complition_quantity;
	public BigDecimal remaining_quantity;
	public String remark;
	public BigDecimal transaction_quantity;
	public String create_by;
	public Date create_time;
}
