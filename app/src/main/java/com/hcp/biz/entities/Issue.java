package com.hcp.biz.entities;

import java.math.BigDecimal;
import java.util.Date;

public class Issue {
	
	public String sub_inventory;
	public String wip_entity_name;
	public String class_code;
	public Date start_time;
	public Date complete_time;
	public String component_code;
	public String component_name;
	public String wip_supply_meaning;
	public BigDecimal issued_quantity;
	public BigDecimal quantity_open;
	public BigDecimal remaining_quantity;
	public BigDecimal transaction_quantity;
	public Date create_time;
	public String create_by;
}
