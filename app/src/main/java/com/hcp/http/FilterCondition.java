package com.hcp.http;

/**
 * Created by Administrator on 2014/10/27.
 */
public class FilterCondition {
	private String fieldName;
	private Object value;
	private FilterOperator operator;
	private LinkOperator link;

	public FilterCondition() {
		link = LinkOperator.AND;
	}

	public FilterCondition(String fieldName, Object value,
			FilterOperator operator) {
		this.fieldName = fieldName;
		this.value = value;
		this.operator = operator;
		this.link = LinkOperator.AND;
	}

	public FilterCondition(String fieldName, Object value,
			FilterOperator operator, LinkOperator link) {
		this.fieldName = fieldName;
		this.value = value;
		this.operator = operator;
		this.link = link;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public FilterOperator getOperator() {
		return operator;
	}

	public void setOperator(FilterOperator operator) {
		this.operator = operator;
	}

	public LinkOperator getLink() {
		return link;
	}

	public void setLink(LinkOperator link) {
		this.link = link;
	}

}
