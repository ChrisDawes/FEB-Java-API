package com.hcl.feb.api;

public enum FEBFilterOrder {

	ASCENDING("ASC"), DESCENDING("DESC");
	
	String value;
	
	FEBFilterOrder(String value)
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
