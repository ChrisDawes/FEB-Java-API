package com.hcl.feb.api.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hcl.feb.api.FEBAPI;
import com.hcl.feb.api.FEBAPIException;
import com.hcl.feb.api.FEBAPIImpl;
import com.hcl.feb.api.FEBFilterMetaColumns;
import com.hcl.feb.api.FEBFilterOperator;
import com.hcl.feb.api.FEBFilterOrder;
import com.hcl.feb.api.FEBFilterParam;
import com.hcl.feb.api.FEBFilterRelationship;
import com.hcl.feb.api.FEBFilters;
import com.hcl.feb.api.FEBResponse;
import com.hcl.feb.api.FEBReturnFormat;

public class TestFEBAPI {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		//apiTest - 93c6f09d-ec4c-424e-8f4f-5d8eb20a911f
		//APAR Tracking - fb08bbe5-b769-4179-881c-a084d261ba41
		
		final Logger logger = LoggerFactory.getLogger(TestFEBAPI.class);
		
		String host = null;
		String context = null;
		String appid = null;
		String recid= null;
		String user = null;
		String protocol = "";
		boolean ignoreSSL = false;
		char[] pwd = null;
		//list, retrieveRec, create, delete, update, createAttachment, retrieveAttachment, metadata
		String operation = null;
		String freedomIdentifyKey = "1234";
				
		// Parse arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-operation")) {
                operation = args[++i];
            } else if (args[i].equals("-host")) {
                host = args[++i];
            } else if (args[i].equals("-context")) {
                context = args[++i];
            } else if (args[i].equals("-appid")) {
            	appid = args[++i];
    		} else if (args[i].equals("-recid")) {
            	recid = args[++i];
    		} else if (args[i].equals("-user")) {
            	user = args[++i];
    		} else if (args[i].equals("-pwd")) {
            	pwd = args[++i].toCharArray();
    		} else if (args[i].equals("-fid")) {
            	freedomIdentifyKey = args[++i];
    		} else if (args[i].equals("-ignoreSSL")) {
    			if("true".equals(args[++i]))
    				ignoreSSL = true;
    		} else if (args[i].equals("-protocol")) {
            	protocol = args[++i];
    		}
        }
        
        //host, context, protocol (defaults to TLSv1.2), ignoreSSL, user, password, freedomIdentifyKey
        // TAP uses IBM cert that is not in JRE, therefore ignoring SSL		
        FEBAPI fa = null;
        FEBResponse r = null;
		FEBFilters filters = new FEBFilters();
		JSONObject json = null;
		
		try {
			fa = new FEBAPIImpl(host, context, ignoreSSL, protocol, user, pwd, freedomIdentifyKey);
		
			//-------------
			//Bluemix
	//		host = "https://febdemo.mybluemix.net";
	//		appid = "a124a413-972e-4fbe-86b6-2bd3d5c70f03";
	//		user = "82a4687a-c533-49f7-bbc2-5b1279ad9a66";
	//		pwd = "8c17e9a4-f53a-4b6d-87cb-b6fc364f045c".toCharArray();
	//		fa = new FEBAPIImpl(host, user, pwd);
			//--------------
		if("list".equals(operation)) {
				//***********************************************************
				//LIST RECORDS						
//				filters.addFilter(new FEBFilterParam("F_DropDown",FEBFilterOperator.EQUALS,"Closed"));
//				filters.addFilter(new FEBFilterParam("F_DropDown0",FEBFilterOperator.EQUALS,"CDawes"));
//				filters.setFilterRelationship(FEBFilterRelationship.ALL_MATCH);
//				filters.setPageSize(10);
//				filters.setSortBy(FEBFilterMetaColumns.LINE_ID.getValue()); //line id = dbId
				r = fa.listRecords("93c6f09d-ec4c-424e-8f4f-5d8eb20a911f", "F_Form1", null, FEBReturnFormat.OPEN_DOC);
				
				if(r.isResponse20x()) {
					json = r.responseJSON; //now do something with the JSON object
					if(json != null) {
						logger.info("Total Records: {}", json.get("recordCount"));						
						//logger.info("JSON Response: {}", json.toJSONString());
						JSONArray items = (JSONArray)json.get("items");
						//loop through the records
						for(int i=0;i<items.size();i++) {
							JSONObject rec = (JSONObject)items.get(i);
							String uid = (String)rec.get("uid");
							Long lineID = (Long)rec.get("id");
							logger.debug("RID {} of {} = {} :: {}", i, items.size(), uid, lineID);
						}
					} else {
						logger.info("json is null");
						if(r.responseBinary != null) {
							try {
								FileOutputStream out = new FileOutputStream("c:/temp/exportedRestuls.ods");
								out.write(r.responseBinary);
								out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				
//				filters.nextPage();
//				r = fa.listRecords(appid, "F_Form1", filters, null);
//				
//				json = r.responseJSON; //now do something with the JSON object	
//				
//				if(r.isResponse20x()) {
//					if(json != null) {
//						logger.info("Total Records: {}", json.get("recordCount"));						
//						//logger.info("JSON Response: {}", json.toJSONString());
//						JSONArray items = (JSONArray)json.get("items");
//						//loop through the records
//						for(int i=0;i<items.size();i++) {
//							JSONObject rec = (JSONObject)items.get(i);
//							String uid = (String)rec.get("uid");
//							Long lineID = (Long)rec.get("id");
//							logger.debug("RID {} of {} = {} :: {}", i, items.size(), uid, lineID);
//						}
//					} else {
//						logger.info("json is null");
//					}
//				}
				//***********************************************************
			} else if("retrieveRec".equals(operation)) {
				//***********************************************************
				//RETRIEVE SINGLE RECORD
				//fb08bbe5-b769-4179-881c-a084d261ba41 - 6cca8fad-a53a-4f6f-857e-cd5e5cd6d091
				//93c6f09d-ec4c-424e-8f4f-5d8eb20a911f - d2d8efc7-d4cc-4ab0-90ad-cbc1ff8c453f
				r = fa.retrieveRecord("93c6f09d-ec4c-424e-8f4f-5d8eb20a911f", "F_Form1", "7aa99d26-e923-4c33-a76f-f5ce66ef2534", FEBReturnFormat.XML);
				
				if(r.isResponse20x()) {
					if(r.responseJSON != null) {
						json = r.responseJSON;
						JSONArray items = (JSONArray)json.get("items");
						JSONObject rec = (JSONObject)items.get(0);
						logger.info("REC UID: {}", rec.get("uid"));
						logger.info("REC ID: {}", rec.get("id"));
						logger.debug("JSON Response: {}", json.toJSONString());
					} else if(r.responseXML != null) {
					
						Document xml = r.responseXML;
						NodeList nList = r.responseXML.getElementsByTagName("entry");
						
						for (int temp = 0; temp < nList.getLength(); temp++) {
	
							Node nNode = nList.item(temp);
							if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	
								Element eElement = (Element) nNode;
								
								String id = eElement.getElementsByTagName("id").item(0).getTextContent();
	
								logger.info("REC ID: " + id);
								try {
									logger.debug("XML Response: {}", printDoc(r.responseXML));
								} catch(Exception e) {
									
								}
							}
						}
					}

				}
				//***********************************************************
			}  else if("metadata".equals(operation)) {
				//***********************************************************
				//RETRIEVE METADATA	
				r = fa.getFormMetaData("fb08bbe5-b769-4179-881c-a084d261ba41", "F_Form1");
				
				if(r.isResponse20x()) {
					json = r.responseJSON;				
					logger.debug("JSON Response: {}", json.toJSONString());
				}
				//***********************************************************
			} else if("create".equals(operation)) {
				//***********************************************************
				//CREATE RECORD				
				String jsonStr = "{\"pressedButton\" : \"S_Submit\", \"F_FirstName\" : \"Austin\", \"F_LastName\" : \"Dawes\", \"flowState\" : \"ST_Start\"}";
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				
				//BY JSONObject
//				try {
//					JSONParser jsonParser = new JSONParser();
//					JSONObject jsonObject = (JSONObject)jsonParser.parse(jsonStr);
//					r = fa.submitRecord(appid, "F_Form1", jsonObject); //by JSONObject
//				} catch(Exception e){}
	
				r = fa.submitRecord(appid, "F_Form1", jsonStr); //by String				
//				r = fa.submitRecord(appid, "F_Form1", jsonStr); //by File
							
				if(r.responseCode == 200 || r.responseCode == 201) {
					json = r.responseJSON;
					logger.debug("Record Created: UID = {}, Line ID = {}, Stage = {}", json.get("uid"), json.get("id"), json.get("flowState"));					
				}
			//***********************************************************
			} else if("update".equals(operation)) {	
				//***********************************************************
				//UPDATE RECORD			
				//get the record and update the name			
				
				//String jsonStr = "{\"pressedButton\" : \"S_Update\", \"F_FirstName\" : \"Chris\", \"F_LastName\" : \"Dawes\", \"flowState\" : \"ST_Active\"}";
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				
				String recUid = "13a01e85-9363-455d-8134-00f0caee1264"; //"d2d8efc7-d4cc-4ab0-90ad-cbc1ff8c453f";
				recUid ="e9734c53-5e00-44e7-8145-792dd0001ed9";
				
				r = fa.retrieveRecord(appid, "F_Form1", recUid, null);
				if(r.isResponse20x()) {
					json = r.responseJSON;
					JSONArray items = (JSONArray)json.get("items");
					JSONObject rec = (JSONObject)items.get(0);
					//rec.put("F_FirstName", "Chris");
					rec.put("F_LastName", "BingBong");
					rec.put("pressedButton", "S_Update");
					rec.put("flowState", "ST_Active");

					logger.debug("JSON Response: {}", json.toJSONString());
								
					FEBResponse r2 = fa.updateRecord(appid, "F_Form1", recUid, rec.toJSONString());
					//r2 = fa.updateRecord(appid, "F_Form1", recUid, rec); //by JSONObject
					
					if(r2.isResponse20x()) {
						json = r2.responseJSON;
						if(json != null) {
							logger.debug("Record Updated: UID = {}, Line ID = {}, Stage = {}", json.get("uid"), json.get("id"), json.get("flowState"));
						}	
					}
				}		
				//***********************************************************	
			} else if("retrieveAndUpdate".equals(operation)) {	
				//***********************************************************
//				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
//				appid = "088a294e-bfec-4ad6-8cb0-7c2de0e3afed"; //wagner
				//String recUid =""; //d2d8efc7-d4cc-4ab0-90ad-cbc1ff8c453f";
//				recUid = "013ad518-36c7-4060-828c-193f5e430711";
				HashMap<String,String> items = new HashMap<String,String>();
				//items.put("F_FirstName", "James");
				//items.put("F_LastName", "Somebody");
//				items.put("F_Github_issue_Title", "Updated 2");
				
//				appid = "11db64be-501e-44ef-8818-7883fde8f661";
//				recUid = "378df002-e157-444c-8fc7-6c5bd5ffd411";
				
//				HashMap<String,String> items = new HashMap<String,String>();
//				items.put("F_tmpPkgRID", "061ae277-f183-47fd-8c95-f5c50feff5da");
				
				//r = fa.retrieveAndUpdateRecord(appid, "F_Form1", recUid, "S_Update", "ST_Active", items);
				r = fa.retrieveAndUpdateRecord(appid, "F_Form1", recid, "S_UpdateApproverReview", "ST_1stApprover", items);
				
				//String mediaType = "text/xml";
				//String tmpFile = "c:\\temp\\whitelist.xml";
				//r = fa.retrieveAndUpdateRecordWithAttachment(appid, "F_Form1", recUid, "S_Update", "ST_Active", items, "F_Resume", mediaType, tmpFile);
				//***********************************************************
			} else if("delete".equals(operation)) {
				//***********************************************************
				//DELETE RECORD				
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				
				r = fa.deleteRecord(appid, "F_Form1", "6ebdf76f-c8be-43ab-b941-6d204132990d");
							
				//delete does not return any request body
				//***********************************************************	
			} else if("deleteAll".equals(operation)) {
				//***********************************************************
				//DELETE RECORD				
				appid = "928d68eb-7c08-4d86-8083-1a7eef707e8e";
				FEBFilters delFilter = new FEBFilters();
				delFilter.addFilter(new FEBFilterParam("updated",FEBFilterOperator.BEFORE,"2018-03-17%2023%3A28%3A03.810"));
				r = fa.deleteRecords(appid, "F_NotificationsSent", null);
							
				//delete does not return any request body
				//***********************************************************	
			} else if("createRecWAttachment".equals(operation)) {
				//***********************************************************
				//CREATE ATTACHMENT
				//upload file, update record with attachment details
				//-operation createRecWAttachment -host http://localhost:9080 -user wasadmin -pwd wasadmin
				
				String mediaType = "text/xml";
				String tmpFile = "c:\\temp\\whitelist.xml";
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";  //apiTest
				
				String jsonStr = "{\"pressedButton\" : \"S_Submit\", \"F_FirstName\" : \"Christopher\", \"F_LastName\" : \"Dawes\", \"F_Resume\" : { \"uid\" : \"\", \"fileName\" : \"\", \"id\" : \"\" },\"flowState\" : \"ST_Start\"}";
				r = fa.submitRecordWithAttachment(appid, "F_Form1", jsonStr, "S_Submit", "F_Resume", mediaType, tmpFile);
			
				if(r.isResponse20x()) {
					json = r.responseJSON;
					if(json != null) {
						logger.debug("JSON Response: {}", json.toJSONString());
					}
				}
				
			} else if("createRecWManyAttachments".equals(operation)) {
				
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";  //apiTest
				
				//to create a record where there is a table and several attachments...
				//you have to upload each file, then modify the JSON
				String file = "c:/temp/up1.txt";
				FEBResponse up1 = fa.uploadAttachment(appid, "F_Form1", "text/plain", file);
				String up1_uid = "",  up1_fileName = "";
				String up2_uid = "", up2_fileName = "";
				String up3_uid = "", up3_fileName = "";				
				Long up1_id = null, up2_id = null, up3_id = null;
				
				if(up1.isResponse20x()) {
					up1_uid = (String) up1.responseJSON.get("uid");
					up1_id = (Long) up1.responseJSON.get("id");
					up1_fileName = (String)up1.responseJSON.get("fileName");
				} else {
					logger.error("Failed to upload file. Received HTTP " + up1.responseCode + "(" + up1.responseText + ")");
					throw new FEBAPIException("Failed to upload file " + file);
				}
					
				file = "c:/temp/up2.txt";
				FEBResponse up2 = fa.uploadAttachment(appid, "F_Form1", "text/plain", file);
				if(up2.isResponse20x()) {
					up2_uid = (String) up2.responseJSON.get("uid");
					up2_id = (Long) up2.responseJSON.get("id");
					up2_fileName = (String)up2.responseJSON.get("fileName");
				} else {
					logger.error("Failed to upload file. Received HTTP " + up2.responseCode + "(" + up2.responseText + ")");
					throw new FEBAPIException("Failed to upload file " + file);
				}
				
				file = "c:/temp/up3.txt";
				FEBResponse up3 = fa.uploadAttachment(appid, "F_Form1", "text/plain", file);
				if(up3.isResponse20x()) {
					up3_uid = (String) up3.responseJSON.get("uid");
					up3_id = (Long) up3.responseJSON.get("id");
					up3_fileName = (String)up3.responseJSON.get("fileName");
				} else {
					logger.error("Failed to upload file. Received HTTP " + up3.responseCode + "(" + up3.responseText + ")");
					throw new FEBAPIException("Failed to upload file " + file);
				}
				
				//get sample create JSON Object from swagger
				FEBResponse swag = fa.getSampleJSONForForm(appid, "F_Form1");
				JSONObject sampleRec = null;
				if(swag.isResponse20x())
					sampleRec = swag.responseJSON;	
				
				
				if(sampleRec != null) {
					sampleRec.put("flowState", "ST_Start");
					sampleRec.put("pressedButton", "S_Submit");
					sampleRec.put("F_FirstName", "Joe");
					sampleRec.put("F_LastName", "Sample");
					
					//map first attachment
					JSONObject at1 = (JSONObject) sampleRec.get("F_Resume");
					at1.put("uid", up1_uid);
					at1.put("id", up1_id);
					at1.put("fileName", up1_fileName);
					
					//map second attachment
					JSONObject tbl = (JSONObject) sampleRec.get("F_Table1");
					JSONArray rows = (JSONArray) tbl.get("items");
					JSONObject tmpRow = (JSONObject) rows.get(0);
					tmpRow.put("F_SingleLine2", "Row 1");
					JSONObject rowAt = (JSONObject) tmpRow.get("F_Attachment1");
					rowAt.put("uid", up2_uid);
					rowAt.put("id", up2_id);
					rowAt.put("fileName", up2_fileName);
					
					//create a new table row for 3rd attachment
					JSONObject newRow = new JSONObject();
					newRow.put("F_SingleLine2", "Row 2");
					JSONObject newAt = new JSONObject();
					newAt.put("uid", up3_uid);
					newAt.put("id", up3_id);
					newAt.put("fileName", up3_fileName);
					newRow.put("F_Attachment1", newAt);
					
					rows.add(newRow);
					
					FEBResponse newRec = fa.submitRecord(appid, "F_Form1", sampleRec);
					if(newRec.isResponse20x()) {
						System.out.println(newRec.responseJSON);
					}
				}
//***************************************************************************
// ATTACHMENT API FUNCTIONS
//
// Retrieve, Upload
//***************************************************************************
				//***********************************************************
			} else if("retrieveAttachmentByUIDToPath".equals(operation)) {
				//***********************************************************
				//RETRIEVE ATTACHMENT	
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				r = fa.retrieveAttachmentByUidToPath(appid, "F_Form1", "ab7836d6-9697-4000-934b-16c1aedb3dd2", "c:/temp/myOut.xml");
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("retrieveAttachmentByFieldIDToPath".equals(operation)) {
				//***********************************************************
				//RETRIEVE ATTACHMENT	
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				String recId = "13a01e85-9363-455d-8134-00f0caee1264";
				r = fa.retrieveAttachmentByFieldIdToPath(appid, "F_Form1", recId, "F_Resume", "c:/temp");
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("retrieveAttachmentByUIDToStream".equals(operation)) {
				//***********************************************************
				//RETRIEVE ATTACHMENT	
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				InputStream is = fa.retrieveAttachmentByUidToStream(appid, "F_Form1", "9922c225-472e-4e54-80a6-5057b08a1d04");
				
				//now write to file - c:/temp/extractedAttachment
				try {
					printStreamToFile(is, "c:/temp/extractedAttachment");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("retrieveAttachmentByFieldIdToStream".equals(operation)) {
				//***********************************************************
				//RETRIEVE ATTACHMENT	
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				String recId = "13a01e85-9363-455d-8134-00f0caee1264";
				InputStream is = fa.retrieveAttachmentByFieldIdToStream(appid, "F_Form1", recId, "F_Resume");
				
				//now write to file - c:/temp/extractedAttachment
				try {
					printStreamToFile(is, "c:/temp/F_Resume_extractedAttachment");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("uploadAttachmentFromFile".equals(operation)) {
				//***********************************************************
				//UPLOAD ATTACHMENT	
				//-operation uploadAttachmentFromFile -host http://localhost:9080 -user wasadmin -pwd wasadmin
				
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				appid = "6c6bb536-3e39-4259-8dec-0de6f6ececcb";
				r = fa.uploadAttachment(appid, "F_Form1", "text/xml", "c:/temp/whitelist.xml");
				
				System.out.println(r.responseText);
				System.out.println(r.responseJSON.toJSONString());
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("uploadAttachmentFromStream".equals(operation)) {
				//***********************************************************
				//UPLOAD ATTACHMENT	
				//-operation uploadAttachmentFromStream -host http://localhost:9080 -user wasadmin -pwd wasadmin
				
				appid = "93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				FileInputStream is = (FileInputStream)readFileToStream("c:/temp/whitelist.xml");
//				r = fa.submitRecordWithAttachment(appUid, formId, jsonData, pressedButton, attachFieldID, mediaType, fileName, fileStream)(appid, "F_Form1", "text/xml", "whitelistFromIS.xml", is);
				String jsonStr = "{\"pressedButton\" : \"S_Submit\", \"F_FirstName\" : \"Christopher\", \"F_LastName\" : \"Dawes\", \"F_Resume\" : { \"uid\" : \"\", \"fileName\" : \"\", \"id\" : \"\" },\"flowState\" : \"ST_Start\"}";
				r = fa.submitRecordWithAttachment(appid, "F_Form1", jsonStr, "S_Submit", "F_Resume", "text/xml", "whitelistFromIS.xml", is);
				
				//assign to rec - fffda2c2-2d7e-4568-ad6c-5d6e5f17741f
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
				
				
//***************************************************************************
// APP MANAGEMENT API FUNCTIONS
//
// Admin List Apps, List Apps for User, Export, Import, Delete, Upgrade, Start, Stop
//***************************************************************************		
			} else if("listAppsForUser".equals(operation)) {
				r = fa.listAppsForUser(null, null, null, null);
//				r = fa.listAppsForUser(2, 5, FEBFilterMetaColumns.APP_SORTBY_TITLE.toString(), FEBFilterOrder.ASCENDING.toString());
//				r = fa.listAppsForUser(2, 5, FEBFilterMetaColumns.APP_SORTBY_TITLE.toString(), FEBFilterOrder.DESCENDING.toString());					
//				r = fa.listAppsForUser(2, 5, FEBFilterMetaColumns.APP_SORTBY_LAST_UPDATED_TIMESTAMP.toString(), FEBFilterOrder.ASCENDING.toString());					
//				r = fa.listAppsForUser(2, 5, FEBFilterMetaColumns.APP_SORTBY_LAST_UPDATED_TIMESTAMP.toString(), FEBFilterOrder.DESCENDING.toString());
				
				if(r.isResponse20x()) {
					NodeList nList = r.responseXML.getElementsByTagName("entry");
					
					for (int temp = 0; temp < nList.getLength(); temp++) {

						Node nNode = nList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							Element eElement = (Element) nNode;
							
							String id = eElement.getElementsByTagName("id").item(0).getTextContent();
							String name = eElement.getElementsByTagName("title").item(0).getTextContent();
							String status = eElement.getElementsByTagName("nitro:status").item(0).getTextContent();

							System.out.println(name + " (" + id + ") :: Status = " + status);
						}
					}
				} 
				
			} else if("adminListApps".equals(operation)) {
				r = fa.adminListApps(1, 5, null, null);
				
				if(r.isResponse20x()) {
//					System.out.println(r.responseJSON.toString());
					
					Long totalNum = (Long) r.responseJSON.get("total");
					System.out.println("Total Apps = " + totalNum);
					
					JSONArray apps = (JSONArray) r.responseJSON.get("apps");
					JSONObject one = (JSONObject) apps.get(0);
					System.out.println("First App is '" + one.get("name") + "' (" + one.get("uid") + ")");				
				}
			} else if("export".equals(operation)) {
				//***********************************************************
				//EXPORT	
				appid = "fb08bbe5-b769-4179-881c-a084d261ba41"; //"93c6f09d-ec4c-424e-8f4f-5d8eb20a911f";
				r = fa.exportApplication(appid, "c:/temp", false);
				
				//9922c225-472e-4e54-80a6-5057b08a1d04
				//***********************************************************
			} else if("deleteApp".equals(operation)) {
				//***********************************************************
				//DELETE APPLICATION	
				appid = "165ae06b-994e-4184-8c7f-f223dc127911";
				r = fa.deleteApplication(appid);
				//***********************************************************
			} else if("deleteApps".equals(operation)) {
				//***********************************************************
//				ArrayList<String> apps = new ArrayList<String>();
//				apps.add("4a81f2c7-3763-4050-856e-736982a89677");
//				apps.add("20427a6c-12a3-4635-80b9-ca5d76e91129");
//				apps.add("7031bc28-9087-406e-8254-6a0ef491f71e");
				
				ArrayList<String> apps = new ArrayList<String>(Arrays.asList(null,"","ec651f4a-0b97-4cf3-9c66-ef61e8b8ae25"));				
				
				r = fa.deleteApplications(apps);
				
				System.out.println(r.responseText);
				//***********************************************************
			} else if("importApp".equals(operation)) {
				//***********************************************************
				//IMPORT APPLICATION
				String path = "C:\\temp\\fb08bbe5-b769-4179-881c-a084d261ba41.nitro_s";
				File f = new File(path);
				r = fa.importApplication(f.getAbsolutePath(), true, false, false, "sample,dawes,automatic_import"); //import by file
//				r = fa.importApplication(path, false, false, false, "sample,dawes,automatic_import");  //import by string path
				//***********************************************************
			} else if("importAppFromStream".equals(operation)) {
				//***********************************************************
				//IMPORT APPLICATION
				//currently broken.  FEB receives the stream but does not recognize it as a valid FEB application.
				FileInputStream is = (FileInputStream) readFileToStream("C:\\temp\\fb08bbe5-b769-4179-881c-a084d261ba41.nitro_s");

				r = fa.importApplication(is, "test", true, false, false, "sample,dawes,automatic_import");
				
				System.out.println(r.responseJSON.toJSONString());
				
//				r = fa.importApplication(f, false, false, "sample,dawes,automatic_import");
				//***********************************************************
			} else if("upgradeApp".equals(operation)) {
				//***********************************************************
				//UPGRADE APPLICATION
				String path = "C:\\temp\\fb08bbe5-b769-4179-881c-a084d261ba41.nitro_s";
				appid = "fb08bbe5-b769-4179-881c-a084d261ba41";
				File f = new File(path);
				r = fa.upgradeApplication(f.getAbsolutePath(), false, appid);
//				r = fa.upgradeApplication(f, false, appid);
				//***********************************************************
			} else if("upgradeAppFromStream".equals(operation)) {
				//***********************************************************
				//UPGRADE APPLICATION
				String path = "C:\\temp\\fb08bbe5-b769-4179-881c-a084d261ba41.nitro_s";
				appid = "fb08bbe5-b769-4179-881c-a084d261ba41";
				File f = new File(path);
				try {
					r = fa.upgradeApplication(new FileInputStream(f), false, appid);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
//				r = fa.upgradeApplication(f, false, appid);
				//***********************************************************
			} else if("stopApp".equals(operation)) {
				appid = "c092624c-a5cf-4204-88e9-cf318b124263";
				r = fa.stopApplication(appid);
				
			} else if("startApp".equals(operation)) {
				appid = "c092624c-a5cf-4204-88e9-cf318b124263";
				r = fa.startApplication(appid);
			}
		
		 
			if(r != null)			
				logger.debug("Response Code = {}, Response Message = {}", r.responseCode, r.responseText);
		
		} catch (FEBAPIException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param is
	 * @param filePath
	 * @throws IOException
	 */
	private static void printStreamToFile(InputStream is, String filePath) throws IOException {
			
		OutputStream os = new FileOutputStream (new File(filePath));    
	
		int read = 0;
		byte[] bytes = new byte[1024];
 
		while ((read = is.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		}
	
		if (os != null) {
			try {
				os.close();
			} catch (Exception e) {
				//throw new Exception(e.getMessage(), e);					
			}
 
		}
	}
	
	private static InputStream readFileToStream(String filePath) {
		
		File file = new File(filePath);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return (InputStream) fis;
	}
	
	/*
     * Prints DOM document to a string.
     */
    public static String printDoc (Document doc) throws Exception {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      //initialize StreamResult with File object to save to file
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);

      String xmlString = result.getWriter().toString();
      //xmlString = xmlString.replaceAll("null", "");
      return xmlString;
    }
}
