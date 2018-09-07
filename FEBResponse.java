package com.hcl.feb.api;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;

/**
 * The primary object returned by this APIs functions. The object contains:
 * 
 *
 * JSONObject responseJSON 	- The JSONObject returned from the REST API.
 * Document responseXML		- The XML Document returned from the REST API.
 * byte[] responseBinary	- The bytes from the response.  Used only by listRecords when exporting as ms_excel or open_doc.
 * int responseCode 		- The HTTP code returned from the response.
 * String responseText 		- Any text returned from the response.
 * 
 * 
 * @author ChristopherDawes
 *
 */
public class FEBResponse {
	
    public JSONObject responseJSON;
    public byte[] responseBinary;
    public int responseCode;
    public String responseText;
    public Document responseXML;
    
    public FEBResponse() {
    	responseJSON = null;
    	responseXML = null;
    	responseCode = -1;
    	responseText = "";
    	responseBinary = null;
    }
    
    public boolean isResponse20x() {
    	boolean r = false;
    	
    	if(responseCode == 200 || responseCode == 201)
    		r = true;
    	
    	return r;
    }
	
}
