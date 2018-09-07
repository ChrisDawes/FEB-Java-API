package com.hcl.feb.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;

/**
 * A class that provides code that can be used to interact with the Forms Experience Builder REST API.  Initialize a FEBAPI
 * object to work with the FEB REST API:
 * 
 * <pre>
 * FEBResponse r = null;
 * FEBAPI febapi = new FEBAPIImpl(host, context, true, null, user, pwd, freedomIdentifyKey);
 * r = febapi.listRecords(appid, "F_Form1", filters);
 * ...
 * r = febapi.retrieveRecord("93c6f09d-ec4c-424e-8f4f-5d8eb20a911f", "F_Form1", "d2d8efc7-d4cc-4ab0-90ad-cbc1ff8c453f");
 * ...
 * r = febapi.getFormMetaData("fb08bbe5-b769-4179-881c-a084d261ba41", "F_Form1");
 * ...
 * r = febapi.submitRecord(appid, "F_Form1", jsonStr);
 * ...
 * r = febapi.retrieveRecord(appid, "F_Form1", recUid);
 * 
 * if(r.responseCode == 200) {
 *	 json = r.responseJSON;
 *	 ...
 * }
 * </pre>
 * 
 * The API uses SLF4J for logging, which gives the user the freedom to bind it to your own logging implementation preference.  For details on 
 * SLF4J refer to https://www.slf4j.org/manual.html
 * 
 * @author ChristopherDawes
 * @version 1, 12/22/2017
 */
public interface FEBAPI {
	
	/**
	 * Returns a FEBResponse object that contains a list of all the application records that matched the specified filters.
	 * 
	 * Example(s):
	 * 
	 * 1. No Filter:
	 * <pre>
	 * {@code FEBResponse r = febapi.listRecords(appid, "F_Form1", null);}
	 * </pre>
	 * 
	 * 2. AND Filter:
	 * <pre>
	 * {@code FEBFilters filters = new FEBFilters();
	 * filters.addFilter(new FEBFilterParam("F_Status",FEBFilterOperator.EQUALS,"Closed"));
	 * filters.addFilter(new FEBFilterParam("F_Owner",FEBFilterOperator.EQUALS,"Christopher Dawes"));
	 * filters.setFilterRelationship(FEBFilterRelationship.AND);
	 * filters.setFrom(0);
	 * filters.setTo(10);
	 * FEBResponse r = febapi.listRecords(appid, "F_Form1", filters);}
	 * </pre>
	 *
	 * 3. OR Filter:
	 * <pre>
	 * {@code FEBFilters filters = new FEBFilters();
	 * filters.addFilter(new FEBFilterParam("F_Owner",FEBFilterOperator.CONTAINS,"Dawes"));
	 * filters.addFilter(new FEBFilterParam("F_Owner",FEBFilterOperator.CONTAINS,"Jones"));
	 * filters.setFilterRelationship(FEBFilterRelationship.OR);
	 * filters.setFrom(20);
	 * filters.setTo(31);
	 * filters.setSortBy("lastUpdated");
	 * filters.setFilterOrder(FEBFilterOrder.DESCENDING);
	 * FEBResponse r = febapi.listRecords(appid, "F_Form1", filters);}
	 * </pre>
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param filters - FEBFilters object that contains all the info related to filters to apply to the query.  The FEBFilters object is made up of an array of FEBFilterParam objects and a filter relationship. For example:
	 * <pre>
	 * {@code FEBFilters filters = new FEBFilters();
	 * filters.addFilter(new FEBFilterParam("F_Status","equals","Closed"));
	 * filters.addFilter(new FEBFilterParam("F_Owner","equals","CDawes"));} 
	 * </pre>
	 * @param returnFormat - The format to return the data.  Valid values are XML and JSON, defaults to JSON.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if general exception occurs
	 * 
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_list.html">FEB REST API - List</a>
	 */
	public FEBResponse listRecords (String appUid, String formId, FEBFilters filters, FEBReturnFormat returnFormat) throws FEBAPIException;
	
	/**
	 * Returns a FEBResponse object that contains a single record from a form.  User specifies the Application UID, the Form ID and the Record UID. 
	 * 
	 * Example:
	 * <pre>
	 * {@code
	 *      FEBAPIImpl febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 *   	FEBResponse r = febapi.retrieveRecord(appid, "F_Form1", recUid);
	 *		if(r.isResponse20x()) {
	 *			json = r.responseJSON;
	 *			JSONArray items = (JSONArray)json.get("items");
	 *			JSONObject rec = (JSONObject)items.get(0);
	 *
	 *			//extract data from the record
	 *			String fname = rec.get("F_FirstName");
	 * 		} 
	 * }
	 * </pre>
	 * 
	 * The JSON returned looks like:
	 * 
	 * <pre>
	 * {
     *    "application_uid": "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f",
     *    "formId": "F_Form1",
     *    "recordCount": 1,
     *    "items": [
     *    {
     *       "lastModified": "2017-12-30T23:07:44.048Z",
     *       "lastModifiedBy": {
     *           "displayName": "Christopher Dawes",
     *           "email": "cdawes@ca.ibm.com",
     *           "login": "cdawes@ca.ibm.com"
     *       },
     *       "created": "2017-12-30T22:53:45.375Z",
     *       "createdBy": {
     *           "displayName": "Christopher Dawes",
     *           "email": "cdawes@ca.ibm.com",
     *           "login": "cdawes@ca.ibm.com"
     *       },
     *       "draft_ownerid": "",
     *       "flowState": "ST_Active",
     *       "id": 8,
     *       "uid": "13a01e85-9363-455d-8134-00f0caee1264",
     *       "F_FirstName": "Chris",
     *       "F_LastName": "BingBong",
     *       "F_Resume": {
     *           "uid": "f8851f00-e6ae-4571-88a7-58ab79387351",
     *           "fileName": "whitelist.xml",
     *           "id": 234704
     *       },
     *       "availableSubmitButtons": [
     *           "S_Update"
     *       ]
     *     }
     *   ]
     * }
	 * </pre>
	 * 
	 * Example:
	 * <pre>
	 * {@code
	 *      FEBAPIImpl febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 *   	FEBResponse r = febapi.retrieveRecord(appid, "F_Form1", recUid);
	 *		if(r.isResponse20x()) {
	 *			json = r.responseJSON;
	 *			JSONArray items = (JSONArray)json.get("items");
	 *			JSONObject rec = (JSONObject)items.get(0);
	 *
	 *			//extract data from the record
	 *			String fname = rec.get("F_FirstName");
	 * 		}} 
	 * </pre>
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param recordUid - The uid of the record to retrieve.
	 * @param returnFormat - The format to return the data.  Valid values are XML and JSON, defaults to JSON.
	 * 
	 * @return FEBResponse
	 * 
	 *
	 * @throws FEBAPIException if general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_retrieve.html">FEB REST API - Retrieve</a>
	 */
	public FEBResponse retrieveRecord (String appUid, String formId, String recordUid, FEBReturnFormat returnFormat) throws FEBAPIException;
	
	/**
	 * Retrieves a description of all the items in your form.
	 * 
	 * Example Response:
	 * <pre>
	 * {
	 * "metadata": {
	 *	"fields": [{
	 *		"name": "F_FirstName",
	 *		"uiType": "textField",
	 *		"dataType": "string",
	 *		"label": "First Name"
	 *	},
	 *	{
	 *		"name": "F_LastName",
	 *		"uiType": "textField",
	 *		"dataType": "string",
	 *		"label": "Last Name"
	 *	},
	 *	{
	 *		"name": "F_Number1",
	 *		"uiType": "number",
	 *		"dataType": "decimal",
	 *		"label": "Hours worked",
	 *		"decimalPlaces": 2
	 *	}]
     *       }
	 * }
	 * </pre>
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_metadata.html">FEB REST API - Metadata</a>
	 */
	public FEBResponse getFormMetaData (String appUid, String formId) throws FEBAPIException;
	
	/**
	 * Retrieves the specified attachment from the FEB Application and writes it to the filePath provided.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param attachmentUID - The UID of the attachment.
	 * @param filePath - The path to write the file.  User must have access to write to the directory.
	 * 
	 * @return FEBResponse - Contains the HTTP Response code (responseCode) and message (responseText).
	 * @throws FEBAPIException if general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_retrieve_attachment.html">FEB REST API - Retrieve Attachment</a>
	 */
	public FEBResponse retrieveAttachmentByUidToPath (String appUid, String formId, String attachmentUID, String filePath) throws FEBAPIException;
		
	/**
	 * Retrieves the specified attachment from the FEB Application and returns it as an InputStream.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param attachmentUID - The UID of the attachment.
	 * 
	 * @return FEBResponse - Contains the HTTP Response code (responseCode) and message (responseText).
	 * @throws FEBAPIException if general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_retrieve_attachment.html">FEB REST API - Retrieve Attachment</a>
	 */
	public InputStream retrieveAttachmentByUidToStream (String appUid, String formId, String attachmentUID) throws FEBAPIException;
		
	/**
	 * Retrieves the record id, extracts the attachment specified by "fieldID" and then writes it to the filePath specified using the file name of the original file. 
	 *  
	 * @param appUid - The application uid.
	 * @param formId - The form id.
	 * @param recId - The record id.
	 * @param fieldID - The ID of the attachment field in the form.
	 * @param filePath - Specify a directory only, the original file name will be used.
	 * @return FEBResponse
	 * @throws FEBAPIException if general exception occurs
	 */
	public FEBResponse retrieveAttachmentByFieldIdToPath (String appUid, String formId, String recId, String fieldID, String filePath) throws FEBAPIException;
		
	/**
	 * Retrieves the record id, extracts the attachment specified by "fieldID" and then returns the content as an InputStream.
	 * 
	 * @param appUid - The application uid.
	 * @param formId - The form id.
	 * @param recId - The record id.
	 * @param fieldID - The ID of the attachment field in the form.
	 * @return FEBResponse
	 * @throws FEBAPIException if general exception occurs
	 */
	public InputStream retrieveAttachmentByFieldIdToStream (String appUid, String formId, String recId, String fieldID) throws FEBAPIException;
		
	/**
	 * Uploads a file to the specified FEB application and then "links" it with a submitted record.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param mediaType - The media type of the file being uploaded.
	 * @param filePath - The path to the file to upload.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if the file doesn't exist or don't have read access
	 * @throws FEBAPIException if general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_create_attachment.html">FEB REST API - Create Attachment</a>
	 */
	public FEBResponse uploadAttachment (String appUid, String formId, String mediaType, String filePath) throws FEBAPIException;
	
	/**
	 * Upload a file as an attachment to a FEB application from an InputStream.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param mediaType - The mediaType of the file.
	 * @param fileName - The name that should be used for the file.
	 * @param fileStream - The stream that contains the file to be uploaded.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if general exception occurs
	 */
	public FEBResponse uploadAttachment (String appUid, String formId, String mediaType, String fileName, InputStream fileStream) throws FEBAPIException;
	
	/**
	 * Creates a record in the specified application and form.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param jsonData - The JSON data to be used to create the FEB record.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if a general exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_create.html">FEB REST API - Create</a>
	 */
	public FEBResponse submitRecord (String appUid, String formId, String jsonData) throws FEBAPIException;
	
	/**
	 * Creates a record in the specified application and form and also includes a single attachment.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param jsonData - The JSON data to be used to create the FEB record, this must be the complete representation of the record.
	 * @param pressedButton - The ID of the stage button being triggered.
	 * @param attachFieldID - The ID of the attachment field in the form.
	 * @param mediaType - The media-type of the file being attached.
	 * @param filePath - The path of the file being attached.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if file doens't exist or can't be read
	 * @throws FEBAPIException if pressedButton is null or empty
	 * @throws FEBAPIException if the jsonData fails to parse
	 * @throws FEBAPIException if the attachment upload fails
	 */
	public FEBResponse submitRecordWithAttachment (String appUid, String formId, String jsonData, String pressedButton, String attachFieldID, String mediaType, String filePath) throws FEBAPIException;
	
	/**
	 * Creates a FEB Record with an attachment from an input stream.
	 * 
	 * @param appUid - the UID of the application.
	 * @param formId - the ID of the form.
	 * @param jsonData - The String of json data to use for the submission.
	 * @param pressedButton - The ID of the stage button that is triggered.
	 * @param attachFieldID - The ID of the attachment field.
	 * @param mediaType - The media type of the file being attached.
	 * @param fileName - The name of the file.
	 * @param fileStream - The InputStream that contains the file content to be uploaded.
	 * @return FEBResponse
	 * @throws FEBAPIException if pressedButton is null or empty
	 * @throws FEBAPIException if the jsonData fails to parse
	 * @throws FEBAPIException if the attachment upload fails
	 */
	public FEBResponse submitRecordWithAttachment (String appUid, String formId, String jsonData, String pressedButton, String attachFieldID, String mediaType, String fileName, InputStream fileStream) throws FEBAPIException;
	
	/**
	 * Returns the JSON Object that can be used to create a new record in the specified form.  The sample is
	 * extracted from the "definitions" object in the Swagger response for this form.
	 * 
	 * <pre>
	 * Example:
	 * {@code
	 * 		FEBAPIImpl febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 * 		FEBResponse swag = febapi.getSampleJSONForForm(appid, "F_Form1");
	 *		JSONObject sampleRec = null;
	 *		if(swag.isResponse20x()) {
	 *			sampleRec = swag.responseJSON;	
	 *		
	 *			if(sampleRec != null) {
	 *				sampleRec.put("flowState", "ST_Start");
	 *				sampleRec.put("pressedButton", "S_Submit");
	 *				sampleRec.put("F_FirstName", "Joe");
	 *				sampleRec.put("F_LastName", "Sample");
	 *			}
	 *		}
	 *}
	 *</pre>
	 * 
	 * @param appUid  - The UID of the application.
	 * @param formId - The ID of the form.
	 * @return FEBResponse
	 * @throws FEBAPIException if appUid is null or empty
	 * @throws FEBAPIException if formId is null or empty
	 */
	public FEBResponse getSampleJSONForForm (String appUid, String formId) throws FEBAPIException;
	
	/**
	 * Returns the JSON Object generated by Swagger for the form.
	 * 
	 * <pre>
	 * Example:
	 * {@code
	 * 		FEBAPIImpl febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 * 		FEBResponse swagRes = febapi.getCompleteJSONForForm(appid, "F_Form1");
	 *		JSONObject swagJSON = null;
	 *		if(swagRes.isResponse20x()) {
	 *			swagJSON = swagRes.responseJSON;	
	 *			. . .			
	 *		}
	 *}
	 *</pre>
	 * 
	 * @param appUid - The UID of the application.
	 * @param formId - The ID of the form.
	 * @return FEBResponse
	 * @throws FEBAPIException if appUid is null or empty
	 * @throws FEBAPIException if formId is null or empty
	 * 
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_access_rest_api.html'>Data Access REST API</a>
	 */
	public FEBResponse getCompleteJSONForForm (String appUid, String formId) throws FEBAPIException;
	
	/**
	 *  
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param jsonData - The JSONObject to use for the update.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_create.html">FEB REST API - Create</a>
	 */
	public FEBResponse submitRecord (String appUid, String formId, JSONObject jsonData) throws FEBAPIException;
	
	/**
	 * Create a new FEB record from a file containing the valid JSON representation of that form.  The JSON 
	 * must include the "required" items as outlined in the API Create documentation.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param jsonData - The File object that contains the JSON data.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_create.html">FEB REST API - Create</a>
	 */
	public FEBResponse submitRecord (String appUid, String formId, File jsonData) throws FEBAPIException;
	
	/**
	 * Updates a FEB record using the JSON in the specified String.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param recordUid - The uid of the record to be updated.
	 * @param jsonData - The JSON data will replace all the data of the existing record.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_update.html">FEB REST API - Update</a>
	 * 
	 */
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, String jsonData) throws FEBAPIException;
	
	/**
	 * Updates the FEB record from the JSONObject.  The JSON must contain the flowState and pressedButton properties, 
	 * otherwise an error will occur.
	 * 
	 * @param appUid - The UID of the application.
	 * @param formId - The ID of the form.
	 * @param recordUid - The ID of the record to retrieve.
	 * @param jsonData - The JSONObject to use for the update.
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs.
	 * @throws FEBAPIException if pressedButton is not specified.
	 * @throws FEBAPIException if flowState is not specified.
	 * 
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_update.html">FEB REST API - Update</a>
	 */
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, JSONObject jsonData) throws FEBAPIException;
	
	/**
	 * Updates the FEB record from the content contained in the JSON file.  The file must contain valid JSON.  The JSON must 
	 * contain the flowState and pressedButton properties, otherwise an error will occur.
	 * 
	 * @param appUid - The UID of the application.
	 * @param formId - The ID of the form.
	 * @param recordUid - The ID of the record to retrieve.
	 * @param jsonData - The file that contains the JSON data for the update.
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs
	 * 
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_update.html">FEB REST API - Update</a>
	 */
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, File jsonData) throws FEBAPIException;
	
	/**
	 * Retrieves the FEB record and then replaces the value of the fields that are defined in the "itemsToSet".  Cannot not use this function to 
	 * update an attachment in a FEB record, but you could update any of the field data.
	 * 
	 * @param appUid - The UID of the application.
	 * @param formId - The ID of the form.
	 * @param recordUid - The ID of the record to retrieve.
	 * @param pressedButton - The ID of the stage button being triggered.
	 * @param flowState - The ID of the stage the record is currently in.
	 * @param itemsToSet The map of items and their values you want to update.
	 * @return FEBResponse
	 * @throws FEBAPIException if pressedButton is not specified
	 * @throws FEBAPIException if flowState is not specified
	 */
	public FEBResponse retrieveAndUpdateRecord (String appUid, String formId, String recordUid, String pressedButton, String flowState, HashMap<String,String> itemsToSet) throws FEBAPIException;
	
	
	/**
	 * Not Implemented.
	 * 
	 * @param appUid - The UID of the application.
	 * @param formId - The ID of the form.
	 * @param recordUid - The ID of the record to be retrieved.
	 * @param pressedButton - The ID of the submit button being triggered.
	 * @param flowState - The ID of the stage the record is currently in.
	 * @param itemsToSet - The map of items and their values you want to update.
	 * @param attachFieldID - The ID of the attachment field.
	 * @param mediaType - The media type of the file being attached.
	 * @param filePath - The path of the file to be attached.
	 * @return FEBResponse
	 * @throws FEBAPIException - Not Implemented
	 */
	public FEBResponse retrieveAndUpdateRecordWithAttachment (String appUid, String formId, String recordUid, String pressedButton, String flowState, HashMap<String,String> itemsToSet, String attachFieldID, String mediaType, String filePath) throws FEBAPIException;
	
	/**
	 * Deletes a specific record from a form.  User specifies the Application UID, the Form ID and the Record UID.
	 * 
	 * @param appUid - The FEB application UID.
	 * @param formId - The ID of the form.
	 * @param recordUid - The uid of the record to be deleted.
	 *  
	 * @return FEBResponse
	 * @throws FEBAPIException - if a generic exception occurs
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_data_rest_api_delete.html">FEB REST API - Delete</a>
	 */
	public FEBResponse deleteRecord (String appUid, String formId, String recordUid) throws FEBAPIException;
	
	/**
	 * Deletes all the records that match the criteria of the provided filter.
	 * 
	 * @param appUid - The UID of the application
	 * @param formId - The ID of the form
	 * @param filters - Reference {@link FEBFilters} and {@link FEBFilterParam}
	 * 
	 * <pre>
	 * {@code FEBFilters filters = new FEBFilters();
	 * filters.addFilter(new FEBFilterParam("F_Status","equals","Closed"));
	 * filters.addFilter(new FEBFilterParam("F_Owner","equals","CDawes"));
	 * 
	 * FEBAPI febapi = new FEBAPIImpl(host, user, pwd, freedomIdentifyKey);
	 * FEBResponse r = febapi.deleteRecords(appid, "c:/temp", filters);
	 * }
	 * </pre>
	 * @return FEBResponse - The responseText will contain a report of all the applications that were processed
	 * @throws FEBAPIException if generic exception occurs
	 */
	public FEBResponse deleteRecords(String appUid, String formId, FEBFilters filters) throws FEBAPIException;
	
	/**
	 * Exports the specified FEB application, as a .nitro_s file, from the server and writes it to the specified basePath.
	 *  
	 * @param appUid - The UID of the application to export
	 * @param basePath - The directory to write the exported application.
	 * @param includeData - Set to true if you want to include submitted data, otherwise false.  Defaults to false.
	 * 
	 * @return FEBResponse
	 * @throws FEBAPIException if cannot write to basePath
	 * 
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>Application Management REST API</a>
	 */
	public FEBResponse exportApplication (String appUid, String basePath, boolean includeData) throws FEBAPIException;
	
	/**
	 * Exports the specified FEB application from the server and writes it to the specified InputStream.
	 * 
	 * @param appUid - The application UID
	 * @param includeData - If true all the submitted data and attachments will be included.  Default is false.
	 * @return FEBResponse
	 * @throws FEBAPIException if IO exception occurs
	 * 
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>Application Management REST API</a>
	 */
	public InputStream exportApplication (String appUid, boolean includeData) throws FEBAPIException;
	
	/**
	 * Deletes the application identified by the appUid from the server.
	 * 
	 * Usage:
	 * 
	 * <pre>
	 * {@code
	 * FEBAPI febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 * FEBResponse r = febapi.deleteApplication("0bb3c130-4230-4f74-88ae-afe97571ad0e");
	 * }</pre>
	 * 
	 * @param appUid - The UID of the application to delete from the server.  
	 * @return FEBResponse 
	 * @throws FEBAPIException if IO or generic exception occur
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>Application Management REST API</a>
	 */
	public FEBResponse deleteApplication (String appUid) throws FEBAPIException;
	
	/**
	 * Deletes all the applications by the UID.
	 * 
	 * Usage:
	 * 
	 * <pre>
	 * {@code
	 * FEBAPI febapi = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
	 * ArrayList<String> apps = new ArrayList<String>(Arrays.asList("0bb3c130-4230-4f74-88ae-afe97571ad0e","6173bf6c-f2e7-43f2-8226-3c27d2154ff1","ec651f4a-0b97-4cf3-9c66-ef61e8b8ae25"));
	 * FEBResponse r = febapi.deleteApplications(apps);
	 * System.out.println(r.responseText);}</pre>
	 * 
	 * @param appUids - ArrayList of application UIDs to delete. 
	 * @return FEBResponse - The responseText will contain a report of all the applications that were processed
	 * 
	 * For example:
	 * ec651f4a-0b97-4cf3-9c66-ef61e8b8ae25 deleted successfully
	 * Failed to delete 6173bf6c-f2e7-43f2-8226-3c27d2154ff1
	 * 
	 * @throws FEBAPIException if IO or generic exception occur
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>Application Management REST API</a>
	 */
	public FEBResponse deleteApplications (ArrayList<String> appUids) throws FEBAPIException;
	
	/**
	 * Imports the file at the specified path into the server.  Returns the UID of the newly created application as 
	 * well as the application.xml nested within a JSONObject.
	 * 
	 * Example:
	 * <pre>
	 * {
     * "authors":[
     * {
     *    "name":"Christopher Dawes"
     * }
     * ],
     * "title":"Ticket Tracking",
     * "content":{
     * "attributes":{
     *    "type":"application\/xml"
     * },
     * "children":[
     * "..."
     * ]
     *}
     *}
	 * </pre>
	 * 
	 * @param appPath - The path of the nitro_s file to import, the file object for the app to import.
	 * @param deployApp - Set to true if you want to deploy the application, otherwise false.  Defaults to false.
	 * @param includeData - Set to true if you want to import the data that is part of the application.  Only 
	 * 						applicable if deployApp is set to true. Defaults to false.
	 * @param removePreviousIds - Set to true to remove owners associated with the application being imported.  Only the 
	 * importer will be in the Administrator role of the application.
	 * @param tags - Comma separated list of the tags to add to the application being imported.  
	 * 
	 * @return FEBResponse
	 * 
	 * @throws FEBAPIException if file cannot be found
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse importApplication (String appPath, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException;
	
	/**
	 * Imports the file at the specified path into the server.  Returns the UID of the newly created application as 
	 * well as the application.xml nested within a JSONObject.
	 * 
	 * Example:
	 * <pre>
	 * {
     * "authors":[
     * {
     *    "name":"Christopher Dawes"
     * }
     * ],
     * "title":"Ticket Tracking",
     * "content":{
     * "attributes":{
     *    "type":"application\/xml"
     * },
     * "children":[
     * "..."
     * ]
     *}
     *}
	 * </pre>
	 * 
	 * @param appFile - The path of the nitro_s file to import, the file object for the app to import.
	 * @param deployApp - Set to true if you want to deploy the application, otherwise false.  Defaults to false.
	 * @param includeData - Set to true if you want to import the data that is part of the application.  Only 
	 * 						applicable if deployApp is set to true. Defaults to false.
	 * @param removePreviousIds - Will remove all users on the access tab, leaving only the importer.
	 * @param tags - Comma separated list of the tags to add to the application being imported.  
	 * 
	 * @return FEBResponse
	 * 
	 * @throws FEBAPIException if file cannot be found
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse importApplication (File appFile, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException;
	
	/**
	 * Import application from InputStream. Returns the UID of the newly created application as 
	 * well as the application.xml nested within a JSONObject.
	 * 
	 * @param appStream - The InputStream of the application to import
	 * @param appName - is this really needed?
	 * @param deployApp - True will deploy the application. Default is false.
	 * @param includeData - True will import the data, only applicable if deploy is also true.  Default is false.
	 * @param removePreviousIds - Will remove all users on the access tab, leaving only the importer.
	 * @param tags - Comma separated list of the tags to add to the application being imported.
	 * @return FEBResponse
	 * @throws FEBAPIException if file cannot be found
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse importApplication (InputStream appStream, String appName, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException;
	
	/**
	 * Upgrades a FEB application with the nitro_s file at the specified string path.
	 * 
	 * @param appPath/appFile - The path of the nitro_s file to use for the upgrade, the file object for the app to use for the upgrade.
	 * @param includeData - Set to true if you want to import the data that is part of the application. Defaults to false.
	 * @param appUid - The UID of the application on the server to upgrade.
	 *  
	 * @return FEBResponse
	 * 
	 * @throws FEBAPIException if the file cannot be found
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse upgradeApplication (String appPath, boolean includeData, String appUid) throws FEBAPIException;
	
	/**
	 * Upgrades a FEB application with the specified nitro_s file.
	 * 
	 * @param appFile - The file object of the FEB application (must be a .nitro_s file)
	 * @param includeData - Set to true if you want to import the data that is part of the application. Defaults to false.
	 * @param appUid - The UID of the application on the server to upgrade.
	 * @return FEBResponse
	 * @throws FEBAPIException - if the file cannot be found
	 * 
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse upgradeApplication (File appFile, boolean includeData, String appUid) throws FEBAPIException;
	
	/**
	 * Upgrades a FEB application with the stream of the nitro_s file.
	 * 
	 * @param appStream - The input stream that contains the FEB application.
	 * @param includeData - Set to true if you want to import the data that is part of the application. Defaults to false.
	 * @param appUid - The UID of the application on the server to upgrade.
	 * @return FEBResponse
	 * @throws FEBAPIException if a generic exception occurs
	 * 
	 * @see <a href='https://www.ibm.com/support/knowledgecenter/SS6KJL_8.6.4/FEB/ref_rest_api_auto_deploy.html'>REST API Documentation</a>
	 */
	public FEBResponse upgradeApplication (InputStream appStream, boolean includeData, String appUid) throws FEBAPIException;
	
	/**
	 * Returns all the applications for the user credentials provided when setting up the API object.
	 * 
	 * Note: This function returns an XML response.
	 * 
	 * Sample Response:
	 * <pre>
	 * {@code<feed xmlns="http://www.w3.org/2005/Atom" xmlns:nitro="http://www.ibm.com/xmlns/prod/forms/nitro/feed/1.0" xml:base="https://tapintofeb.victoria.ibm.com/forms-basic/" xml:lang="en">
    <id>urn:uuid:1225c695-cfb8-4ebb-bbbb-80da344efa6afeed</id>
    <title type="text">All Applications</title>
    <generator uri="http://www.ibm.com/products/software/forms/nitro" version="1.0">IBM Forms Experience Builder</generator>
    <updated>2018-03-22T22:44:39.354Z</updated>
    <link href="https://localhost/forms-basic/secure/org/myapps?pageSize=5&amp;count=true&amp;page=1" rel="self"></link>
    <link href="https://localhost/forms-basic/secure/org/myapps?count=true&amp;pageSize=5&amp;page=2" rel="next"></link>
    <entry>
        <id>969ef543-f7c0-4525-8e32-3e2a4828f6a6</id>
        <category term="application"></category>
        <link href="secure/1/app/969ef543-f7c0-4525-8e32-3e2a4828f6a6/source/latest/application.xml" rel="edit"></link>
        <link href="landing/org/app/969ef543-f7c0-4525-8e32-3e2a4828f6a6/viewdata/index.html" rel="view"></link>
        <link href="landing/org/app/969ef543-f7c0-4525-8e32-3e2a4828f6a6/launch/index.html?form=F_Form1" title=F_Form1" rel="form"></link>
        <link href="resources/1/_969ef543_f7c0_4525_8e32_3e2a4828f6a6/F_Form1" title="F_Form1" rel="form-data"></link>
        <link href="landing/org/app/969ef543-f7c0-4525-8e32-3e2a4828f6a6/viewdata/index.html"></link>
        <title type="text">Sample App 1</title>
        <author>
            <name>Johnny Guy</name>
            <email>jguy@company.com</email>
            <nitro:isVisitor>false</nitro:isVisitor>
        </author>
        <updated>2018-01-18T02:28:36.795Z</updated>
        <summary type="text"> </summary>
        <nitro:name>_969ef543_f7c0_4525_8e32_3e2a4828f6a6</nitro:name>
        <nitro:orgId>1</nitro:orgId>
        <nitro:status>publish-stopped</nitro:status>
        <nitro:created>2017-11-02T05:51:26.000Z</nitro:created>
        <nitro:count>0</nitro:count>
        <nitro:tags></nitro:tags>
        <nitro:default-form>F_Form1</nitro:default-form>
    </entry>
    
    . . .
    </feed>}</pre>
	 * 
	 * 
	 * @param pageNum	Results are paginated, this is the page number. If not specified the first page is shown.
	 * @param pageSize	The number of applications to show per page.  Default is 10.
	 * @param sortBy  Sorts the applications, possible values are FEBFilterMetaColumns.APP_SORTBY_TITLE.toString() and FEBFilterMetaColumns.APP_SORTBY_LAST_UPDATED_TIMESTAMP.toString()
	 * @param orderBy Results can be sorted as FEBFilterOrder.ASCENDING.toString() or FEBFilterOrder.DESCENDING.toString()
	 * @return FEBResponse
	 * @throws FEBAPIException if generic exception occurs
	 */
	public FEBResponse listAppsForUser (Integer pageNum, Integer pageSize, String sortBy, String orderBy) throws FEBAPIException;
	
	/**
	 * Returns all the applications for the user credentials provided when setting up the API object. This service can only be called
	 * by users in the AdministrativeUsers Role.  This service returns a JSON response. 
	 * 
	 * Note: This function does not work for a FEB server using Derby as the database.
	 * <pre>
	 * Sample Response:
	 * 
	 * {@code{
	*	   "total":13184,
	*	   "apps":[
	*	      {
	*	         "uid":"969ef543-f7c0-4525-8e32-3e2a4828f6a6",
	*	         "name":"My FEB App",
	*	         "owners":"christopher.dawes@hcl.com",
	*	         "lastModified":"2018-01-18T02:28:36Z",
	*	         "state":"published"
	*	      },
	*	      
	*	      . . .
	*	      
	*	      ]
	*	}}</pre>
	*
	*  Valid states are: published, oos-published, saved, publish-stopped
	 * 
	 * @param pageNum - The page number of the responses
	 * @param pageSize - The number of records to include per page
	 * @param sortBy - The attribute used to sort the responses.  Valid items are {@link FEBFilterMetaColumns}.
	 * @param orderBy - The attribute used to order the responses. Valid values are {@link FEBFilterOrder}
	 * @return FEBResponse
	 * @throws FEBAPIException if a general exception occurs
	 */
	public FEBResponse adminListApps (Integer pageNum, Integer pageSize, String sortBy, String orderBy) throws FEBAPIException;
	
	/**
	 * Stops the application.
	 * This function communicates in XML, therefore the FEBResponse will contain responseXML.
	 * 
	 * @param appid - The UID of the application
	 * @return FEBResponse
	 * @throws FEBAPIException if a general exception occurs
	 */
	public FEBResponse stopApplication(String appid) throws FEBAPIException;
	
	/**
	 * Starts the application.
	 * This function communicates in XML, therefore the FEBResponse will contain responseXML.
	 * 
	 * @param appid - The UID of the application
	 * @return FEBResponse
	 * @throws FEBAPIException if a general exception occurs
	 */
	public FEBResponse startApplication(String appid) throws FEBAPIException;
	
	/**
	 * Generates a number as a string based on the current date that can be used as the freedomIdentifyKey.
	 * @return
	 */
	public String generateFreedomIdentifyKey();

}
