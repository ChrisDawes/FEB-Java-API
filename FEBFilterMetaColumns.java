package com.hcl.feb.api;

public enum FEBFilterMetaColumns {
	
	LINE_ID("dbId"), SORTBY_AUTHOR_NAME("itemAuthor"), AUTHOR_NAME("author_name"), UPDATER_NAME("updater_name"),
	CREATED_TIMESTAMP("creation_time"), SORTBY_LAST_UPDATED_TIMESTAMP("lastUpdated"), LAST_UPDATED_TIMESTAMP("updated"),
	SORTBY_STAGE_ID("flowState"), STAGE_ID("flow_state"), APP_SORTBY_LAST_UPDATED_TIMESTAMP("updated"), 
	APP_SORTBY_TITLE("name");
	
	String value;
	
	FEBFilterMetaColumns(String value)
	  {
	    this.value = value;
	  }

	  public String getValue()
	  {
	    return this.value;
	  }

	  @Override
	  public String toString()
	  {
	    return this.value;
	  }

}
