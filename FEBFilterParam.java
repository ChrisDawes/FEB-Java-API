package com.hcl.feb.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Defines a FEB REST API Filter object. A filter is made up of a parameter, an operator and a value.  The parameter is
 * a field ID (i.e. F_Name), the operator determines how the value will be evaluated (i.e. "equals", "contains", "lt") and
 * the value is the string that you are searching for. 
 * 
 * Usage:
 *  
 *  1. Create a FEBFilter array:
 *  <pre>
 *  FEBFilter[] filters = new FEBFilter[2];
 *  </pre>
 *  
 *  2. Add the filters:
 *  <pre>
 *  filters[0] = new FEBFilter("F_Status",FEBFilterOperator.EQUALS,"Closed");
 *	filters[1] = new FEBFilter("F_Owner",FEBFilterOperator.EQUALS,"Christopher Dawes");
 *  </pre>
 *
 *  3. Add the filters array to the function:
 *  <pre>
 *  r = fa.listRecords(appid, "F_Form1", filters, 0, 10, FEBFilterRelationship.ALL_MATCH);
 *  </pre>
 * 
 * @author ChristopherDawes
 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_list_filter.html'>REST API - List Filters</a>
 *
 */
public class FEBFilterParam {
	private String filterParam;
	private FEBFilterOperator operator;
	private String value;
	private final Logger logger = LoggerFactory.getLogger(FEBFilterParam.class);
  
	/**
	 * Creates a FEBFilter object that contains a parameter, operator and value.
	 * 
	 * @param param - The ID of the form field you want to search.  May not contain spaces or other illegal characters.
	 * @param operator - The operator to use for the filter. All possible are defined in the FEBFilterOperator.
	 * @param value - The string value you are searching for. 
	 * 
	 * @throws FEBAPIException if param contains a space
	 * @throws FEBAPIException if an error occurs encoding the URL
	 */
	public FEBFilterParam(String param, FEBFilterOperator operator, String value) throws FEBAPIException {
		if(param.indexOf(" ") != -1){ 
			logger.error("Fitler parameter ({}) may not contain a space.", param);
			throw new FEBAPIException("Fitler parameter may not contain a space.");
		}
		
		this.filterParam = param;
		this.operator = operator;
		logger.debug("Created filter as {} {} {}", param, operator, value);
		
		try {
			logger.debug("Encoding the filter value.  Original value was \"{}\"", value);
			this.value = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
			//this.value = value;
			logger.debug("Encoded value is \"{}\"", this.value);
		} catch(UnsupportedEncodingException uee) {
			throw new FEBAPIException(uee.getMessage());
		}
	}
	
	/**
	 * Generates the formatted filter string to add to the FEB REST URL.  The format of the generated
	 * string is:
	 * 
	 * {@code "<param>__<operator>=<value>"}
	 * 
	 * @return String
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_list_filter.html'>REST API - List Filters</a>
	 */
	public String getFilterString() {
		logger.debug("Returning formatted filter string = {}", filterParam + "__" + operator.getValue() + "=" + value);
		return filterParam + "__" + operator.getValue() + "=" + value;
	}
  
  	public String getFilterParam() {
		return filterParam;
	}
	
	public void setFilterParam(String filterParm) {
		this.filterParam = filterParm;
	}
	
	public String getOperator() {
		return operator.getValue();
	}
	
	public void setOperator(FEBFilterOperator operator) {
		this.operator = operator;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
