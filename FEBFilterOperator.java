/**
 * 
 */
package com.hcl.feb.api;

/**
 * Defines the operators that can be used when constructing a FEBFilter.
 * 
 * @author ChristopherDawes
 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_list_filter.html'>REST API - List Filters</a>
 */
public enum FEBFilterOperator {
	EQUALS("equals"),STARTS_WITH("startswith"),ENDS_WITH("endswith"),CONTAINS("contains"),NOT_EQUALS("notequals"),GREATER_THAN("gt"),LESS_THAN("lt"),GREATER_THAN_OR_EQUAL_TO("gte"),LESS_THAN_OR_EQUAL_TO("lte"),AFTER("after"),BEFORE("before"),BETWEEN("between"),YEAR_MATCHES("year"),MONTH_MATCHES("month"),DAY_MATCHES("day");
	
	String value;
	
	FEBFilterOperator(String value)
	  {
	    this.value = value;
	  }

	  public String getValue()
	  {
	    return this.value;
	  }

	  public String toString()
	  {
	    return this.value;
	  }
}