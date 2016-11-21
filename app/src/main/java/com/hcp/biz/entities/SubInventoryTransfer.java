package com.hcp.biz.entities;

import java.math.BigDecimal;
import java.util.Date;

public class SubInventoryTransfer {
	public String segment_code;
	public String segment_name;
	public String sub_inventory_from;
	public String locator_from;
	public String project_number_from;
	public BigDecimal remaining_quantity;
	public String sub_inventory_to;
	public String locator_to;
	public String project_number_to;
	public BigDecimal transaction_quantity;
	public Date create_time;
	public String create_by;
}
