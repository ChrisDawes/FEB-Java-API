/**
 * 
 */
package com.hcl.feb.api;

/**
 * Defines the relationship between multiple filters assigned.  The operators are assigned to the searchOperator parameter.
 * The operators are AND and OR.
 * 
 * @author ChristopherDawes
 *
 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_list_filter.html'>REST API - List Filters</a>
 */
public enum FEBFilterRelationship {
	
	AT_LEAST_ONE_MATCHES("OR"), ALL_MATCH("AND");
	
	String value;
	
	FEBFilterRelationship(String value)
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
