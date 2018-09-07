package com.hcl.feb.api;

/**
 * Object that defines the supported return formats from the FEB REST endpoints.
 * 
 * listRecords supports all return formats. If exporting as ms_excel or open_doc then the binary file is stored in FEBResponse.responseBinary
 * retrieveRecord can be exported as XML or JSON
 * 
 * @author ChristopherDawes
 *
 */
public enum FEBReturnFormat {
	XML("application/atom+xml"),JSON("application/json"),MS_EXCEL("application/x-msexcel"),OPEN_DOC("application/vnd.oasis.opendocument.spreadsheet");
	
	String value;
	
	FEBReturnFormat(String value)
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
