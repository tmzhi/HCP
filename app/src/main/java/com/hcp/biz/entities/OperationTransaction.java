package com.hcp.biz.entities;

import java.math.BigDecimal;
import java.util.Date;

public class OperationTransaction {
	public String wip_entity_name;
	public String class_code;
	public Date start_time;
	public Date completion_time;
	public String transaction_type;
	public int serial_from;
	public int serial_to;
	public String step_from;
	public String step_to;
	public BigDecimal transaction_quantity;
	public Date create_time;
	public String create_by;
}
