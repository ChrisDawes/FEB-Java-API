package com.hcl.feb.api;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

//import java.util.Base64; //java 1.8
import org.apache.commons.codec.binary.Base64; //java 1.6  //add to manifest -> Import-Package: org.apache.commons.codec.binary
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The purpose of this class is to assist developers in programming with the FEB REST API.
 * The first release of the API supports JSON.
 * 
 * @author ChristopherDawes
 *
 */
public class FEBAPIImpl implements FEBAPI {
	
	private String server = ""; //hostname and port of FEB server
	private String context = ""; //forms or forms-basic
	private String credentials = ""; //the encoded user/pwd
	private String protocol = ""; //SSL, TLSv1.2, etc
	private String freedomIdentifyKey = "";
	private String urlBase = "";
	private String appUrlBase = "";
	private String orgUrlBase = "";
	private String adminUrlBase = "";
	private boolean ignoreSSL = false;
//	private String logDir = "";
//	private boolean debug = false;
	
	private static final String APP_IMPORT = "import";
	private static final String APP_UPGRADE = "upgrade";
	private static final String APP_STOP = "stop";
	private static final String APP_START = "start";
	private static final String HTTP_GET = "GET";
	private static final String HTTP_POST = "POST";
	private static final String HTTP_PUT = "PUT";
	private static final String HTTP_DELETE = "DELETE";
	private static final String ENCODING_UTF8 = "UTF-8";
	private static final String JSON_MEDIATYPE = "application/json";
	private static final String ATOM_MEDIATYPE = "application/atom+xml";
	private static final String TEXT_MEDIATYPE = "text/plain";
//	private static final String FEB_EXTENSION = ".nitro_s";
	private static final String MULTIPART_MEDIATYPE = "multipart/form-data";
	private static final String LINE_FEED = "\r\n";
	
	private final Logger logger = LoggerFactory.getLogger(FEBAPIImpl.class);
	
	/**
	 * 
	 * @param hostname - The hostname of the FEB server.
	 * @param username - The username to use for this API request
	 * @param password - The user's password
	 */
	public FEBAPIImpl (String hostname, String username, char[] password) {
		this.server = hostname;
		this.context = "forms-basic";
		this.credentials = this.getEncodedString(username, password);
		
		this.urlBase = this.server + "/" + this.context + "/secure/org/data/";
		this.appUrlBase = this.server + "/" + this.context + "/secure/org/app";
		this.orgUrlBase = this.server + "/" + this.context + "/secure/org/";
		this.adminUrlBase = this.server + "/" + this.context + "/secure/org/admin/";
		this.credentials = this.getEncodedString(username, password);		
	}
	
	/**
	 * 
	 * @param hostname - The hostname of the FEB server.
	 * @param username - The username to use for this API request
	 * @param password - The user's password
	 * @param freedomIdentifyKey - The value to assign as the freedomIdentifyKey
	 */
	public FEBAPIImpl (String hostname, String username, char[] password, String freedomIdentifyKey) {
		this.server = hostname;
		this.context = "forms-basic";
		this.credentials = this.getEncodedString(username, password);
		this.freedomIdentifyKey = freedomIdentifyKey;
		
		this.urlBase = this.server + "/" + this.context + "/secure/org/data/";
		this.appUrlBase = this.server + "/" + this.context + "/secure/org/app";
		this.orgUrlBase = this.server + "/" + this.context + "/secure/org/";
		this.adminUrlBase = this.server + "/" + this.context + "/secure/org/admin/";
		this.credentials = this.getEncodedString(username, password);		
	}
	
	/**
	 * 
	 * @param hostname - The server host (i.e. "https://myformsserver.com")
	 * @param context - The context of the forms application (i.e. "forms-basic") 
	 * @param ignoreSSL - A development flag to bypass certificate verification.  Defaults to false.  Should not be true in a production implementation.
	 * @param protocol - If ignoreSSL is "true" then you must specify a communication protocol, i.e. SSL, TLSv1.2
	 * @param username - The username to use for this API request
	 * @param password - The user's password
	 * @param freedomIdentifyKey - The value to assign as the freedomIdentifyKey
	 * @throws FEBAPIException if hostname is not specified
	 * @throws FEBAPIException if username is not specified
	 * @throws FEBAPIException if password is not specified
	 */
	public FEBAPIImpl (String hostname, String context, boolean ignoreSSL, String protocol, String username, char[] password, String freedomIdentifyKey) throws FEBAPIException {
				
		this.protocol = protocol;		
		this.freedomIdentifyKey = freedomIdentifyKey;
		this.ignoreSSL = ignoreSSL;
		
		if(context == null || context.isEmpty()) {			
			this.context = "forms-basic";
			logger.debug("Context was not provided, defaulting to {}", this.context);
		} else { 
			this.context = context;
			logger.debug("Custom context was provided, setting to {}", this.context);
		}
		
		if(ignoreSSL && (protocol == null || protocol.isEmpty())) {
			this.protocol = "TLSv1.2";
			logger.debug("Protocol was not provided, defaulting to {}", this.protocol);
		}
		
		//usage validation and output
		if(hostname == null || hostname.isEmpty()) {
			logger.debug("Host name was not provided, throwing an exception...");
			throw new FEBAPIException("Server host name is required. Example: http://myserver.com");
		} else {
			this.server = hostname;
			logger.debug("Hostname was provided, setting to {}", this.server);
		}
		
		if(username == null || username.isEmpty()) {
			logger.debug("Username is required and was not provided, throwing an exception...");
			throw new FEBAPIException("The username is required.");	
		}
		
		if(password == null || new String(password).isEmpty()) {
			logger.debug("Password is required and was not provided, throwing an exception...");
			throw new FEBAPIException("The password is required.");
		}
		
		this.urlBase = this.server + "/" + this.context + "/secure/org/data/";
		this.appUrlBase = this.server + "/" + this.context + "/secure/org/app";
		this.orgUrlBase = this.server + "/" + this.context + "/secure/org/";
		this.adminUrlBase = this.server + "/" + this.context + "/secure/org/admin/";
		this.credentials = this.getEncodedString(username, password);		
	}
	
	/* DEPRECATED*/
//	private String processFEBFilters(ArrayList<FEBFilterParam> filters, FEBFilterRelationship filterOperator) throws FEBAPIException {
//		String r = "";		
//
//		if(filters != null) {
//			logger.debug("Building URL Filter Params...");
//			for(int i=0;i<filters.size();i++) {
//				r = this.addURLParam(r, filters.get(i).getFilterString());
//				logger.debug("    Adding param {}", filters.get(i).getFilterString());
//			}				
//		}
//		
//		if(filterOperator != null && !filterOperator.getValue().isEmpty()) {
//			r = this.addURLParam(r, "searchOperator=" + filterOperator);
//			logger.debug("    Adding param {}", "searchOperator=" + filterOperator);
//		}
//		
//		return r;
//	}

	/**
	 * Returns all the FEB records that match the specified criteria.
	 * filters - According to the supported filters in KC
	 */
	public FEBResponse listRecords (String appUid, String formId, FEBFilters filters, FEBReturnFormat returnFormat) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId;
		
			if(filters != null)
				apiURL += filters.getFilterURLString();
			
			logger.debug("REST URL = {}", apiURL);	
			
			HashMap<String,String> headers = new HashMap<String,String>();
			if(returnFormat == null || "".equals(returnFormat.toString())) {
				headers.put("Accept", FEBReturnFormat.JSON.toString());
			} else {
				headers.put("Accept", returnFormat.toString());
			}
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_GET, headers, false);			
			
			populateFEBResponse(conn, r);			
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		return r;	
		
	}
	
	/**
	 * Note: FreedomIdentifyKey is added automatically when the connection to the URL is established.
	 * 
	 * @param urlStr
	 * @param method
	 * @return
	 */
	private HttpURLConnection establishURLConnection(String urlStr, String method, HashMap<String,String> headers, boolean omitFIK) throws FEBAPIException {
		URL url = null;
		HttpURLConnection conn = null;
		try {
			
			if(this.freedomIdentifyKey != null && !this.freedomIdentifyKey.isEmpty() && !omitFIK) {
				urlStr = this.addURLParam(urlStr, this.getFIKParam()); //add freedomIdentifyKey
			}
			
			logger.debug("Modified URL = " + urlStr);
			url = new URL(urlStr);
			
			if(url != null) {
				conn = (HttpURLConnection) url.openConnection();
				if(conn instanceof HttpsURLConnection) {			
		            if (this.ignoreSSL) {
		                ((HttpsURLConnection) conn).setSSLSocketFactory(getSSLContext(this.protocol).getSocketFactory());
						((HttpsURLConnection) conn).setHostnameVerifier(getHostNameVerifier());
		            } else {
		                ((HttpsURLConnection) conn).setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
		            }
					((HttpsURLConnection) conn).setRequestMethod(method);
				} else if(conn instanceof HttpURLConnection) {
					conn.setRequestMethod(method);
				}
			}
		} catch(ProtocolException pe) {
			throw new FEBAPIException(pe.getMessage(), pe);
		} catch(Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		//add optional headers
		if(headers != null) {
			Set<String> ks = headers.keySet();
			Iterator<String> iter = ks.iterator();
			while(iter.hasNext()){
				String key = iter.next();
				logger.debug("Adding header " + key + " = " + headers.get(key));
				conn.addRequestProperty(key, headers.get(key));			
			}
		}
		
		conn.addRequestProperty("Authorization", "Basic " + this.credentials);
		logger.debug("Adding Authorization header: " + "Basic " + this.credentials);
		conn.setDoOutput(true);
		
		if(method.equals(HTTP_POST) || method.equals(HTTP_PUT)) {
			conn.setDoInput(true);
		}			

		
		if(this.freedomIdentifyKey != null && !this.freedomIdentifyKey.isEmpty() && !omitFIK) {
			logger.debug("Adding Cookie: freedomIdentifyKey=" + this.freedomIdentifyKey);
			conn.addRequestProperty("Cookie", "freedomIdentifyKey=" + this.freedomIdentifyKey);
		}
		
		return (HttpURLConnection)conn;
	}
	
	@SuppressWarnings("unchecked")
	private void populateFEBResponse(HttpURLConnection conn, FEBResponse resp) throws FEBAPIException {
		
		InputStream is = null;
		try {
			
			resp.responseCode = conn.getResponseCode();
			resp.responseText = conn.getResponseMessage();			
			
			if(resp.responseCode == 200 || resp.responseCode == 201) {
				is = conn.getInputStream();
				if(is != null) {
					
					if(conn.getContentType().startsWith(JSON_MEDIATYPE)) {

						InputStreamReader isr = new InputStreamReader(is, ENCODING_UTF8);
						JSONParser jsonParser = new JSONParser();
						
						try {
							resp.responseJSON = (JSONObject) jsonParser.parse(isr);
							
							//one client found that the integer objects were shown as doubles
							// normalize id to an integer
							Object id = resp.responseJSON.get("id");
							if(id instanceof Double)								
								resp.responseJSON.put("id", ((Double) id).intValue());
							
							//need to check for any attachment objects - loop through all the elements (if has id, uid, filename) update the id to Long
//							JSONObject tmp = resp.responseJSON;

						} catch(ParseException pe) {
							//will get here if a delete operation was performed...as there is no output from the response
							resp.responseJSON = null;
							//throw new FEBAPIException("Error occurred parsing the JSON string.  " + pe.getMessage());
						}

					} else if(conn.getContentType().startsWith(ATOM_MEDIATYPE)) {
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document d = dBuilder.parse(is);
						d.getDocumentElement().normalize();
						resp.responseXML = d;
					} else {
						resp.responseBinary = new byte[conn.getContentLength()];
						DataInputStream dis = new DataInputStream(is);
						dis.readFully(resp.responseBinary);
						dis.close();
					}
				}
			}
			
		} catch(Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
				conn.disconnect();	
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new FEBAPIException(e.getMessage(), e);
				}
			}
		}
	}
	
//private Document parse(InputStreamReader isr) throws Exception {
//	DocumentBuilderFactory factory = DocumentBuilderFactory
//	        .newInstance();
//	    factory.setNamespaceAware(true);
//	    
//	    return factory
//	        .newDocumentBuilder()
//	        .parse(new InputSource(isr));
//}
	
private void writeFEBAttachment(HttpURLConnection conn, FEBResponse resp, String filePath) throws FEBAPIException {
		
		InputStream is = null;
		try {
			
			resp.responseCode = conn.getResponseCode();
			resp.responseText = conn.getResponseMessage();
						
			if(resp.responseCode == 200 || resp.responseCode == 201) {
				is = conn.getInputStream();
				if(is != null) {
//					File f = new File(filePath);
//					if(f.canWrite()) {
						printStreamToFile(is, filePath);
//					} else {
//						throw new Exception("Do not have permission to write to specified path");
//					}
				}
			}
		} catch(IOException ioe) {
			throw new FEBAPIException(ioe.getMessage(), ioe);
		} catch(Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new FEBAPIException(e.getMessage(), e);
				}
			}
		}
	}
	
	/**
	 * Determines if the param needs to be preceded by a ? or &.
	 * 
	 * @param theParams
	 * @param paramToAdd
	 * @return
	 */
	private String addURLParam(String theURL, String paramToAdd) {
		String r = theURL;
		if(paramToAdd != null && !paramToAdd.isEmpty()) {
			if(r.indexOf("?") == -1) {
				r += "?";
			} else {
				r += "&";
			}
			r += paramToAdd;
		}
		return r;
	}
	
	public FEBResponse retrieveRecord (String appUid, String formId, String recordUid, FEBReturnFormat returnFormat) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/" + recordUid;	
			logger.debug("REST URL = {}", apiURL);
					
			HashMap<String,String> headers = new HashMap<String,String>();
			
			if(returnFormat == null || "".equals(returnFormat.toString())) {
				headers.put("Accept", FEBReturnFormat.JSON.toString());
			} else {
				headers.put("Accept", returnFormat.toString());
			}
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_GET, headers, false);
						
			populateFEBResponse(conn, r);
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		return r;
	}
	
//	private String encodeURL(String url) throws FEBAPIException {
//		try {
//			url = URLEncoder.encode(url, ENCODING_UTF8);
//		} catch(UnsupportedEncodingException uee) {
//			throw new FEBAPIException("There was a problem encoding the URL", uee);
//		}
//		return url;
//	}
	
	public FEBResponse retrieveAttachmentByUidToPath (String appUid, String formId, String attachmentUID, String filePath) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/attachment/" + attachmentUID;
			logger.debug("REST URL = {}", apiURL);
					
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("Accept", JSON_MEDIATYPE);
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_GET, headers, false);
						
			//populateFEBResponse(conn, r);
			writeFEBAttachment(conn, r, filePath);
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		return r;
	}
	
	public InputStream retrieveAttachmentByUidToStream (String appUid, String formId, String attachmentUID) throws FEBAPIException {
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/attachment/" + attachmentUID;
			logger.debug("REST URL = {}", apiURL);
					
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_GET, null, false);
						
			return conn.getInputStream();
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
	}
	
	public FEBResponse retrieveAttachmentByFieldIdToPath (String appUid, String formId, String recId, String fieldID, String filePath) throws FEBAPIException {
		try {
			FEBResponse r = new FEBResponse();
			//retrieve record
			r = retrieveRecord(appUid, formId, recId, FEBReturnFormat.JSON);
			
			if(r.responseCode == 200) {
				JSONObject recJson = r.responseJSON;
				JSONArray items = (JSONArray) recJson.get("items");
				JSONObject rec = (JSONObject) items.get(0);
				
				JSONObject file = (JSONObject) rec.get(fieldID);
				
				String attachmentUID = (String) file.get("uid");
				String attachmentName = (String) file.get("fileName");
				
				//get attachment
				r = retrieveAttachmentByUidToPath(appUid, formId, attachmentUID, filePath + "/" + attachmentName);						
			}
						
			return r;
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}	
	}
	
	public InputStream retrieveAttachmentByFieldIdToStream (String appUid, String formId, String recId, String fieldID) throws FEBAPIException {
		
		InputStream is = null;
		
		try {
			FEBResponse r = new FEBResponse();
			//retrieve record
			r = retrieveRecord(appUid, formId, recId, FEBReturnFormat.JSON);
			
			if(r.responseCode == 200) {
				JSONObject recJson = r.responseJSON;
				JSONArray items = (JSONArray) recJson.get("items");
				JSONObject rec = (JSONObject) items.get(0);
				
				JSONObject file = (JSONObject) rec.get(fieldID);
				
				String attachmentUID = (String) file.get("uid");
				
				//get attachment
				is =  retrieveAttachmentByUidToStream(appUid, formId, attachmentUID);						
			}
			
			return is;
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}	
	}
	
	public FEBResponse uploadAttachment (String appUid, String formId, String mediaType, String filePath) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/attachment"; //?invitecode=2414893244";
			//String apiURL = "http://localhost:9080/forms-basic/anon/org/data/" + appUid + "/" + formId + "/attachment?invitecode=1770144750"; //91380104";
			logger.debug("REST URL = {}", apiURL);
					
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("Accept", JSON_MEDIATYPE);
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_POST, headers, false);
						
			File theFile = new File(filePath);
			
			if(!theFile.exists()|| !theFile.canRead())
				throw new FEBAPIException("File does not exist or don't have read access.");
				
			FileInputStream fis = new FileInputStream(theFile);
	    	uploadFileToServer(conn, theFile.getName(), theFile.getName(), mediaType, fis);
	    	fis.close();
						
			populateFEBResponse(conn, r);
			
			conn.disconnect();
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage() + ":: " + r.responseText, e);
		}
		
		return r;		
	}
	
	public FEBResponse uploadAttachment (String appUid, String formId, String mediaType, String fileName, InputStream fileStream) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/attachment"; //?invitecode=2414893244";
			logger.debug("REST URL = {}", apiURL);
					
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("Accept", JSON_MEDIATYPE);
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_POST, headers, false);
						
	    	uploadFileToServer(conn, fileName, fileName, mediaType, fileStream);
						
			populateFEBResponse(conn, r);
			
			conn.disconnect();
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage() + ":: " + r.responseText, e);
		}
		
		return r;		
	}

	public FEBResponse submitRecord (String appUid, String formId, JSONObject jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		String apiURL = this.urlBase + appUid + "/" + formId;
		logger.debug("REST URL = {}", apiURL);
		logger.debug("SUBMITTED JSON = {}", jsonData.toJSONString());
		r = submitUpdateImpl(apiURL, HTTP_POST, jsonData.toJSONString());
		
		return r;
	}
	
	public FEBResponse submitRecord (String appUid, String formId, File jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		if(!jsonData.exists() || !jsonData.canRead())
			throw new FEBAPIException("File does not exist or don't have read access.");
		
		String fileContents = null;
		try {
			fileContents = readFile(jsonData);
		} catch (IOException e) {
			throw new FEBAPIException("Failed to convert file to string.", e);
		}
			
		String apiURL = this.urlBase + appUid + "/" + formId;
		logger.debug("REST URL = {}", apiURL);
		logger.debug("SUBMITTED JSON = {}", fileContents);
		r = submitUpdateImpl(apiURL, HTTP_POST, fileContents);
		
		return r;
	}	
	
	public FEBResponse submitRecord (String appUid, String formId, String jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();

		String apiURL = this.urlBase + appUid + "/" + formId;
		logger.debug("REST URL = {}", apiURL);
		logger.debug("SUBMITTED JSON = {}", jsonData);
		r = submitUpdateImpl(apiURL, HTTP_POST, jsonData);
		
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public FEBResponse submitRecordWithAttachment (String appUid, String formId, String jsonData, String pressedButton, String attachFieldID, String mediaType, String filePath) throws FEBAPIException {
		
		FEBResponse r = new FEBResponse();
		FEBResponse r2 = new FEBResponse();
		JSONObject json = null;

		String apiURL = this.urlBase + appUid + "/" + formId;
		logger.debug("REST URL = {}", apiURL);
		
		File f = new File(filePath);
		
		if(!f.exists() || !f.canRead())
			throw new FEBAPIException("File does not exist or don't have read access.");
		
		if(pressedButton == null || pressedButton.isEmpty())
			throw new FEBAPIException("You must identify the ID of the submit button being triggered.");
		else {
			logger.debug("Adding property to JSON: pressedButton = {}", pressedButton);
			
			//get file name from path		
			r = this.uploadAttachment(appUid, formId, mediaType, filePath);	
			
			if(r.responseCode == 200) {
				json = r.responseJSON;
				if(json != null) {
					logger.debug("Attachment Uploaded...id = {}, uid = {}, fileName = {}", json.get("id"), json.get("uid"), json.get("fileName"));
					
					JSONParser jsonParser = new JSONParser();
					try {
						JSONObject jsonObject = (JSONObject)jsonParser.parse(jsonData);
						jsonObject.put("pressedButton", pressedButton);
						JSONObject attachField = (JSONObject)jsonObject.get(attachFieldID); 
						attachField.put("uid", json.get("uid"));
						attachField.put("id", json.get("id"));
						attachField.put("fileName", json.get("fileName"));
						
						r2 = this.submitRecord(appUid, "F_Form1", jsonObject.toJSONString());
												
						if(r2.responseCode == 200 || r2.responseCode == 201) {
							json = r2.responseJSON;
							logger.debug("Record Created: UID = {}, Line ID = {}, Stage = {}", json.get("uid"), json.get("id"), json.get("flowState"));
						}
					} catch (ParseException pe) {
						throw new FEBAPIException("Failed to parse JSON data.", pe);
					}
				}
			} else {
				throw new FEBAPIException("Failed to upload attachment.  Request returned HTTP " + r.responseCode + " ("+ r.responseText + ")");
				//return r;
			}
		}
		
		
		
		return r2;
	}
	
@SuppressWarnings("unchecked")
public FEBResponse submitRecordWithAttachment (String appUid, String formId, String jsonData, String pressedButton, String attachFieldID, String mediaType, String fileName, InputStream fileStream) throws FEBAPIException {
		
		FEBResponse r = new FEBResponse();
		FEBResponse r2 = new FEBResponse();
		JSONObject json = null;

		String apiURL = this.urlBase + appUid + "/" + formId;
		logger.debug("REST URL = {}", apiURL);
		
//		File f = new File(fileName);
		
		if(pressedButton == null || pressedButton.isEmpty())
			throw new FEBAPIException("You must identify the ID of the submit button being triggered.");
		else {
			logger.debug("Adding property to JSON: pressedButton = {}", pressedButton);
			
			//get file name from path		
			r = this.uploadAttachment(appUid, formId, mediaType, fileName, fileStream);	
			
			if(r.responseCode == 200) {
				json = r.responseJSON;
				if(json != null) {
					logger.debug("Attachment Uploaded...id = {}, uid = {}, fileName = {}", json.get("id"), json.get("uid"), json.get("fileName"));
					
					JSONParser jsonParser = new JSONParser();
					try {
						JSONObject jsonObject = (JSONObject)jsonParser.parse(jsonData);
						jsonObject.put("pressedButton", pressedButton);
						JSONObject attachField = (JSONObject)jsonObject.get(attachFieldID); 
						attachField.put("uid", json.get("uid"));
						attachField.put("id", json.get("id"));
						attachField.put("fileName", json.get("fileName"));
						
						r2 = this.submitRecord(appUid, "F_Form1", jsonObject.toJSONString());
												
						if(r2.responseCode == 200 || r2.responseCode == 201) {
							json = r2.responseJSON;
							logger.debug("Record Created: UID = {}, Line ID = {}, Stage = {}", json.get("uid"), json.get("id"), json.get("flowState"));
						}
					} catch (ParseException pe) {
						throw new FEBAPIException("Failed to parse JSON data.", pe);
					}
				}
			} else {
				throw new FEBAPIException("Failed to upload attachment.  Request returned HTTP " + r.responseCode + " ("+ r.responseText + ")");
				//return r;
			}
		}
		
		return r2;
	}
	
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, String jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		String apiURL = this.urlBase + appUid + "/" + formId + "/" + recordUid;			
		logger.debug("REST URL = {}", apiURL);
				
		r = submitUpdateImpl(apiURL, HTTP_PUT, jsonData);
		
		return r;
	}
	
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, JSONObject jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		String apiURL = this.urlBase + appUid + "/" + formId + "/" + recordUid;			
		logger.debug("REST URL = {}", apiURL);
		logger.debug("JSON = {}", jsonData.toJSONString());
		String pressedButton = (String) jsonData.get("pressedButton");
		String flowState = (String) jsonData.get("flowState");
		
		if(pressedButton == null || pressedButton.isEmpty())
			throw new FEBAPIException("You must identify the ID of the submit button being triggered.");
		
		if(flowState == null || flowState.isEmpty())
			throw new FEBAPIException("You must identify the ID of the current stage of the record being updated.");
		
				
		r = submitUpdateImpl(apiURL, HTTP_PUT, jsonData.toJSONString());
		
		return r;
	}
	
	public FEBResponse updateRecord (String appUid, String formId, String recordUid, File jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		String fileContents = null;
		try {
			fileContents = readFile(jsonData);
		} catch (IOException e) {
			throw new FEBAPIException("Failed to convert file to string.", e);
		}
		
		String apiURL = this.urlBase + appUid + "/" + formId + "/" + recordUid;			
		logger.debug("REST URL = {}", apiURL);
				
		r = submitUpdateImpl(apiURL, HTTP_PUT, fileContents);
		
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public FEBResponse retrieveAndUpdateRecord (String appUid, String formId, String recordUid, String pressedButton, String flowState, HashMap<String,String> itemsToSet) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		FEBResponse r2 = new FEBResponse();		
		JSONObject json = null;
		
		r = this.retrieveRecord(appUid, formId, recordUid, FEBReturnFormat.JSON);
		if(r.responseCode == 200) {
			json = r.responseJSON;
			logger.debug("JSON Response from RETRIEVE: {}", json.toJSONString());
			
			JSONArray items = (JSONArray)json.get("items");
			JSONObject rec = (JSONObject)items.get(0);
			
			if(itemsToSet != null) {
				Set<String> set = itemsToSet.keySet();
				Iterator<String> i = set.iterator();
				while(i.hasNext()) {
					String tmpKey = i.next();
					String tmpVal = itemsToSet.get(tmpKey);
					logger.debug("Updating JSON, adding key = {}, value = {}", tmpKey, tmpVal);
					rec.put(tmpKey, tmpVal);
				}
			}
			
			if(pressedButton == null || pressedButton.isEmpty())
				throw new FEBAPIException("You must identify the ID of the submit button being triggered.");
			else {
				logger.debug("Adding property to JSON: pressedButton = {}", pressedButton);
				rec.put("pressedButton", pressedButton);
			}
			
			if(flowState == null || flowState.isEmpty())
				throw new FEBAPIException("You must identify the ID of the current stage of the record being updated.");
			else {
				logger.debug("Adding property to JSON: flowState = {}", flowState);
				rec.put("flowState", flowState);
			}
						
			r2 = this.updateRecord(appUid, formId, recordUid, rec); //by JSONObject
			
			if(r2.responseCode == 200) {
				json = r2.responseJSON;
				if(json != null) {
					logger.debug("Record Updated: UID = {}, Line ID = {}, Stage = {}", json.get("uid"), json.get("id"), json.get("flowState"));
				}	
			}
		}
		
		return r2;
	}
	
	public FEBResponse retrieveAndUpdateRecordWithAttachment(String appUid, String formId, String recordUid, String pressedButton, String flowState, HashMap<String,String> itemsToSet, String attachFieldID, String mediaType, String filePath) throws FEBAPIException {
//		FEBResponse r = new FEBResponse();
//		FEBResponse r2 = new FEBResponse();		
//		JSONObject json = null;
		
		throw new FEBAPIException("Not Implemented.");
		
		//return r2;
	}
	
	private FEBResponse submitUpdateImpl(String url, String method, String jsonData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		PrintStream fr = null;
		OutputStreamWriter osw = null;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		
		try {
								
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("Accept", JSON_MEDIATYPE);
			headers.put("Content-Type", JSON_MEDIATYPE);
			conn = establishURLConnection(url, method, headers, false);
			
			//upload the json content used to update the record
			if(jsonData != null) {
				dos = new DataOutputStream(conn.getOutputStream());
				osw = new OutputStreamWriter(dos, "UTF-8");
				osw.write(jsonData);
				osw.flush();
				osw.close();
			}
						
			populateFEBResponse(conn, r);
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
			
			if(dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					throw new FEBAPIException(e.getMessage(), e);
				}
			}
		}
		
		return r;
	}
	
	public FEBResponse deleteRecord (String appUid, String formId, String recordUid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/" + recordUid;
			logger.debug("REST URL = {}", apiURL);
					
			r = submitUpdateImpl(apiURL, HTTP_DELETE, null);
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		return r;
	}
	
	public FEBResponse deleteRecords(String appUid, String formId, FEBFilters filters) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		JSONObject recordsJSON;
		boolean moreRecs = true;	
		FEBResponse listRes = null;
		FEBResponse delRes = null;
		Integer pageSize = 50;
					
		while(moreRecs) {
			
			listRes = this.listRecords(appUid, formId, filters, FEBReturnFormat.JSON);
		
			if(listRes.responseCode != 200)
				throw new FEBAPIException("Failed to get records to delete. (" + listRes.responseText + ")");
				
			//get all the records we want to delete
			recordsJSON = listRes.responseJSON;
			
			if(recordsJSON == null) 
				throw new FEBAPIException("There are no records to delete. (" + listRes.responseText + ")");
				
			JSONArray recs = (JSONArray) recordsJSON.get("items");
			int recCount = ((Long) recordsJSON.get("recordCount")).intValue();
			
			if(recs.size() != 0) {
				//then loop through and delete
				for(int i=0;i<recs.size();i++) {
					JSONObject rec = (JSONObject)recs.get(i);
					String rid = ((String) rec.get("uid"));
					
					logger.debug("  Trying to delete record " + rid);
					
					delRes = deleteRecord(appUid, formId, rid);
					
					if(delRes.responseCode != 200) {
						r.responseText += "\nFailed to delete record " + rid;
						logger.debug("  Failed to delete record " + rid);
					} else {
						r.responseText += "\nSuccessfully deleted record " + rid;
						logger.debug("  Successfully deleted record " + rid);
					}
				}						
			} 
			
			if(filters != null)
				pageSize = filters.getPageSize();
			
			if(recCount == pageSize) {
				//do not have to change the from and to because the records are deleted, so the next time
				//through the results will be reduced
				logger.debug("  Getting the next page of records to delete...");
			} else {
				moreRecs = false;
			}				
		}		
		
		return r;
	}
	
	public FEBResponse getFormMetaData (String appUid, String formId) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		try {
			String apiURL = this.urlBase + appUid + "/" + formId + "/metadata";
			logger.debug("REST URL = {}", apiURL);	
					
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("Accept", JSON_MEDIATYPE);
			HttpURLConnection conn = establishURLConnection(apiURL, HTTP_GET, headers, false);
						
			populateFEBResponse(conn, r);	
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		}
		
		return r;
	}
	
	/**
	 * If the freedomIdentifyKey is set then it returns the portion to be added to the REST API URL, otherwise returns empty string.
	 *  
	 * @return
	 */
	private String getFIKParam() {
		String r = "";
		if(this.freedomIdentifyKey != null && this.freedomIdentifyKey != "") 
			r = "freedomIdentifyKey=" + this.freedomIdentifyKey;
		
		return r;
	}
	
	/**
	 * Returns the base64 encoded string with your username and password for inserting into the request header
	 * @param name
	 * @param pwd
	 * @return The base64 encoded username and password
	 */
	private String getEncodedString(String name, char[] pwd) {
		
		String authString = name + ":" + new String(pwd);
//		byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes()); //java 1.8
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes()); //java 1.6
		String authStringEnc = new String(authEncBytes);
		
		return authStringEnc;
	}
	
//	/**
//     * Gets the SSL socket factory which is used in SSL connection. 
//     *  
//     * @return the SSL socket factory. 
//     * @throws ConnectionException if unable to create SSL socket factory. 
//     */ 
//    public SSLSocketFactory getSSLSocketFactory() throws ConnectionException { 
//        try { 
//            if (keyManagers.size() > 0 || trustManagers.size() > 0) { 
//                SSLContext context = SSLContext.getInstance("SSL"); 
//                context.init((KeyManager[]) keyManagers.toArray(new KeyManager[] {}), 
//                        (TrustManager[]) trustManagers.toArray(new TrustManager[] {}), 
//                        null); 
//                return context.getSocketFactory(); 
//            } 
//            else { 
//                return HttpsURLConnection.getDefaultSSLSocketFactory(); 
//            } 
//        } catch (Exception e) { 
//            throw new ConnectionException("Unable to create SSL socket factory", e); 
//        } 
//    }     
	
//    private Vector keyManagers = new Vector();  
//    private Vector trustManagers = new Vector(); 
    
	/**
	 * Returns the SSLContext for use in HTTPS connections
	 * @return
	 * @throws Exception
	 */
	private SSLContext getSSLContext(String commType) throws Exception {
		/***
		 * http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
		 * 
		 * Change all HttpURLConnection to HttpsURLConnection
		 *
		 */
		// Create a trust manager that does not validate certificate chains
	    TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	    };
	    
	    // Install the all-trusting trust manager
	    SSLContext sc = SSLContext.getInstance(commType); //SSL_TLS
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
//	    sc.init((KeyManager[]) keyManagers.toArray(new KeyManager[] {}), 
//                (TrustManager[]) trustManagers.toArray(new TrustManager[] {}), 
//                null); 
	    
	    return sc;
	}

	/**
	 * Returns a HostNameVerifier for use in HTTPS connections.
	 * @return
	 */
	private HostnameVerifier getHostNameVerifier() {
		// Create all-trusting host name verifier
//	    HostnameVerifier allHostsValid = new HostnameVerifier() {
//	        @Override
//	    	public boolean verify(String hostname, SSLSession session) {
//	            return true;
//	        }
//	    };
	    
	    //return allHostsValid;
		return HttpsURLConnection.getDefaultHostnameVerifier();
	}
	
	private void uploadFileToServer(HttpURLConnection conn, String name, String fileName, String contentType, InputStream is) throws FEBAPIException {
    	
    	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    	
    	conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    	conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		
		//Send request
		PrintWriter pw = null;
		OutputStream output = null;
		
		if(name == null)
			name = "dummyFile";
		
		if(fileName == null)
			fileName = "dummyFileName";
		
		try {
			output = conn.getOutputStream();
			pw = new PrintWriter(new OutputStreamWriter(output), true); //true = auto flush
			
			// set header params
			pw.append("--" + boundary).append(LINE_FEED);
			pw.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
			contentType = "application/octet-stream"; //debugging
			pw.append("Content-Type: " + contentType).append(LINE_FEED);
			pw.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			pw.append(LINE_FEED).flush();
			
			//read in the file and append to writer
			try {  
		        byte[] buffer = new byte[4096];  				        
		        for (int n; (n = is.read(buffer)) != -1; ) {
		            output.write(buffer, 0, n);  
		        }
		        output.flush();
		        is.close();
		    } finally {  	
		    	if (is != null) try { is.close(); } catch (IOException logOrIgnore) {}
		    }
		    pw.append(LINE_FEED).flush();
		    
		    //End of multipart/form-data.
		    pw.append("--" + boundary + "--").append(LINE_FEED);
		    pw.close();
		} catch (IOException ioe) {
			throw new FEBAPIException(ioe.getMessage(), ioe);
		} finally {
		
			if (pw != null) {
				pw.close();
				pw.flush();
			}
			if(output != null) {
				try {
					output.close();
					output.flush();
				} catch (IOException ioe) {
					throw new FEBAPIException(ioe.getMessage(), ioe);
				}
			}
		}
	    
		//Get Response	 - returns the XML that contains the content ID that needs to be inserted into the form			    
//		String r = printStreamToString(con.getInputStream());
//		return r;
    
    }
	
	/**
	 * 
	 * @param is
	 * @param filePath
	 * @throws IOException
	 */
	private void printStreamToFile(InputStream is, String filePath) throws FEBAPIException {
			
		OutputStream os = null;
		
		try {
			os = new FileOutputStream (new File(filePath));		
	
			int read = 0;
			byte[] bytes = new byte[1024];
	 
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
	
		} catch (FileNotFoundException e) {
			throw new FEBAPIException(e.getMessage(), e);			
		} catch (IOException e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ioe) {
					throw new FEBAPIException(ioe.getMessage(), ioe);					
				} 
			}
		}
	}
	
//	private void printToLog(String type, String str) throws FEBAPIException {
//		try {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:ss SSS");
//			
//			if(this.logDir != null) {
//				//check to see if the directory exists
//				File f = new File(this.logDir);
//				if(!f.exists() || !f.canWrite()) {
//					throw new Exception("Specified directory (" + this.logDir + ") does not exist or cannot be written to.  You must create it before we can continue.");
//				}
//				
//				//Check to see if the file exists first
//				f = new File(this.logDir + "\\debug.log");
//				boolean appendToFile = false;
//				if(f.exists()) {
//					appendToFile = true;
//				}
//				//if importing only one then it creates a debug file for the app being imported rather then writing to the main debug.log
//				if(this.debug) {
//					if(type.equals("DEBUG") || type.equals("INFO")) {				
//						PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.logDir + "\\debug.log", appendToFile)));
//						out.println(sdf.format(new Date()) + " :: " + str);
//						out.close();
//					}
//				} else if(type.equals("INFO")) {
//					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.logDir + "\\debug.log", appendToFile)));
//					out.println(sdf.format(new Date()) + " :: " + str);
//					out.close();
//				}
//			}
//		
//		} catch (IOException ioe) {
//			throw new FEBAPIException(ioe.getMessage(), ioe);
//		} catch (Exception e) {
//			throw new FEBAPIException(e.getMessage(), e);
//		}
//		
//	}
	
	public FEBResponse exportApplication (String appUid, String basePath, boolean includeData) throws FEBAPIException {
		FEBResponse r = new FEBResponse();		
		HttpURLConnection conn = null;
		InputStream is = null;
		String fileName = null;
		
		try {			
			String actionURL = "/archive?mode=source&submitted=";
			if(includeData){
				actionURL += "true";
			} else {
				actionURL += "false";
			}
			String url = this.appUrlBase + "/" + appUid + actionURL;
			logger.debug("Exporting {}", url);		
			
			conn = this.establishURLConnection(url, HTTP_GET, null, false);						
			is = conn.getInputStream();
			
			fileName = basePath + "/" + appUid + ".nitro_s";
			File dir = new File(basePath);
			File f = new File(fileName);
			
			if(!dir.isDirectory()){
				throw new FEBAPIException("Path (" + dir.getAbsolutePath() + ") is not a directory.");
			}
			if(!dir.canWrite())
				throw new FEBAPIException("Cannot write to specified path (" + dir.getAbsolutePath() + ").");
			
			logger.debug("Writing export to file {}", fileName);
			
			OutputStream os = new FileOutputStream(f);
		    try {  
		        byte[] buffer = new byte[4096];  
		        for (int n; (n = is.read(buffer)) != -1; )   
		            os.write(buffer, 0, n);  
		    } finally { os.close(); }
		    
		    //this.populateFEBResponse(conn, r); //the content of the stream has already been written out
			
			r.responseCode = conn.getResponseCode();
			r.responseText = conn.getResponseMessage();
		} catch(Exception e) {
			fileName = null;
			throw new FEBAPIException("fail: " + e.getMessage(), e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new FEBAPIException("There was a problem closing the connection: " + e.getMessage(), e);
				}
			}
			
			if(conn != null) {				
		        conn.disconnect();
			}
		}
		
		//check to make sure the file exists
		if(fileName != null) {
			File fh = new File(fileName);
			if(!fh.exists()) {
				throw new FEBAPIException("Something went wrong and the application was not exported where it was expected: " + fileName);
			} 
		}
		
		return r;
	}
	
	public InputStream exportApplication (String appUid, boolean includeData) throws FEBAPIException {
			
		HttpURLConnection conn = null;
		
		try {			
			String actionURL = "/archive?mode=source&submitted=";
			if(includeData){
				actionURL += "true";
			} else {
				actionURL += "false";
			}
			String url = this.appUrlBase + "/" + appUid + actionURL;
			logger.debug("Exporting {}", url);		
			
			conn = this.establishURLConnection(url, HTTP_GET, null, false);						
			return conn.getInputStream();
		} catch (IOException ioe) {
			throw new FEBAPIException("Failed to retrieve expxorted application: " + ioe.getMessage(), ioe);
		}
	}
	
	public FEBResponse deleteApplication (String appUid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;
		String url = this.appUrlBase + "/" + appUid;
		logger.debug("Deleting {}", url);
		
		try {						
			conn = this.establishURLConnection(url, HTTP_DELETE, null, false);
			r.responseCode = conn.getResponseCode();
			r.responseText = conn.getResponseMessage();			
			conn.disconnect();
		} catch (IOException ioe) {
			throw new FEBAPIException(ioe.getMessage(), ioe);
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
				conn.disconnect();		        
			}
		}
		
		return r;
	}
	
	public FEBResponse deleteApplications (ArrayList<String> appUids) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;
		
		if(appUids != null) {
			for(int i=0;i<appUids.size();i++) {
				String appid = appUids.get(i);
				
				if(appid != null && !"".equals(appid)) {
					String url = this.appUrlBase + "/" + appid;
					logger.debug("Deleting {}", url);
					
					try {						
						conn = this.establishURLConnection(url, HTTP_DELETE, null, false);
						r.responseCode = conn.getResponseCode();
						
						if(r.responseCode == 200)
							r.responseText += "\n" + appUids.get(i) + " deleted successfully";
						else
							r.responseText += "\n Failed to delete " + appUids.get(i);
						
						conn.disconnect();
					} catch (IOException ioe) {
						throw new FEBAPIException(ioe.getMessage(), ioe);
					} catch (Exception e) {
						throw new FEBAPIException(e.getMessage(), e);
					} finally {
						if(conn != null) {
							conn.disconnect();		        
						}
					}
				}
			}
		}
		
		return r;
	}
	
	public FEBResponse importApplication (String pathOfApp, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		File appFile = new File(pathOfApp);
		if(!appFile.exists())
			throw new FEBAPIException("Cannot find specified file.");
		
		try {
			r = importFEBApplicationImpl(new FileInputStream(appFile), appFile.getName(), deployApp, includeData, removePreviousIds, APP_IMPORT, null, tags);
		} catch (FileNotFoundException e) {
			throw new FEBAPIException("Could not locate file.");
		}
		
		return r;
	}
	
	public FEBResponse importApplication (File appFile, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		if(!appFile.exists())
			throw new FEBAPIException("Cannot find specified file.");
		
		try {
			r = importFEBApplicationImpl(new FileInputStream(appFile), appFile.getName(), deployApp, includeData, removePreviousIds, APP_IMPORT, null, tags);
		} catch (FileNotFoundException e) {
			throw new FEBAPIException("Could not locate file.");
		}
		
		return r;
	}
	
	public FEBResponse importApplication (InputStream appStream, String appName, boolean deployApp, boolean includeData, boolean removePreviousIds, String tags) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		r = importFEBApplicationImpl(appStream, appName, deployApp, includeData, removePreviousIds, APP_IMPORT, null, tags);
				
		return r;
	}
	
	public FEBResponse upgradeApplication (String pathOfApp, boolean includeData, String appUid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		File appFile = new File(pathOfApp);
		if(!appFile.exists())
			throw new FEBAPIException("Cannot find specified file.");
		
		try {
			r = importFEBApplicationImpl(new FileInputStream(appFile), appFile.getName(), false, includeData, false, APP_UPGRADE, appUid, null);
		} catch (FileNotFoundException e) {
			throw new FEBAPIException("Could not locate file.");
		}
		
		return r;
	}
	
	public FEBResponse upgradeApplication (File appFile, boolean includeData, String appUid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		if(!appFile.exists())
			throw new FEBAPIException("Cannot find specified file.");
		
		try {
			r = importFEBApplicationImpl(new FileInputStream(appFile), appFile.getName(), false, includeData, false, APP_UPGRADE, appUid, null);
		} catch (FileNotFoundException e) {
			throw new FEBAPIException("Could not locate file.");
		}
		
		return r;
	}
	
	public FEBResponse upgradeApplication (InputStream appStream, boolean includeData, String appUid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		r = importFEBApplicationImpl(appStream, null, false, includeData, false, APP_UPGRADE, appUid, null);
		
		return r;
	}
	
	/**
	 * Used to Import an application into a FEB server.  
	 * theApp: The nitro_s file to import
	 * deployApp: true or false, determines if app should be deployed
	 * withData: true or false, determines if the data should be imported
	 */
	private FEBResponse importFEBApplicationImpl(InputStream fileStream, String appName, boolean deployApp, boolean includeData, boolean removePreviousIds, String operation, String appUid, String tags) throws FEBAPIException {
		
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;		
		String actionURL = "";		
		
		//check the file extension
//		String extension = .substring(p.indexOf("."));
//		if(!extension.equals(FEB_EXTENSION))
//			throw new FEBAPIException("Specified file is not a valid FEB application.");
		
		if(operation.equals(APP_IMPORT)) {
			actionURL = "?deploy=";
		} else if (operation.equals(APP_UPGRADE)) {
			//https://tapintofeb.victoria.ibm.com/forms/secure/org/app/f39631fd-c349-4620-8256-7c23e272944b/archive?replaceEmbeddedData=on&runDatabaseUpgradeNow=on&iframe=true&format=json-for-dojo-io
			actionURL = "/" + appUid + "/archive?replaceEmbeddedData=on&runDatabaseUpgradeNow=on";
		}
		
		try {
			//https://febontap.victoria.ibm.com/forms/secure/org/app?deploy=true&importData=true&importAllForms=true&format=json-for-dojo-io&iframe=true
			if(operation.equals(APP_IMPORT)) {
				if(deployApp) {
					actionURL += "true";
				} else {
					actionURL += "false";
				}
				
				if(includeData) {
					actionURL += "&importData=true&importAllForms=true";
				} 
				
				if(removePreviousIds)
					actionURL += "&cleanIds=true";
				else					
					actionURL += "&cleanIds=false";
				
			} else if (operation.equals(APP_UPGRADE)) {
				if(includeData) {
					actionURL += "&replaceSubmittedData=on";
				} 
			}
			
			//debug...content here is the FEB app...
			//printStreamToFile(fileStream, "c:/temp/debug.out");
			
			String url = this.appUrlBase + actionURL;					
			
			logger.debug("REST URL = {}", url);
			conn = this.establishURLConnection(url, HTTP_POST, null, false);
			uploadFileToServer(conn, appName, appName, MULTIPART_MEDIATYPE, fileStream);
			//fileStream.close();
		
			//Get Response - why does it return the application.xml content?
			r.responseCode = conn.getResponseCode();
			r.responseText = conn.getResponseMessage();
			
			logger.debug("Import code = " + r.responseCode + " :: Import Response Text = " + r.responseText);
			
			InputStream is = conn.getInputStream();				
			
			if(is != null) {
				//content is wrapped in "<textarea>" tag...have to strip that out
				String tempString = printStreamToString(is); 
				
				if(!"".equals(tempString)) {
					String jsonString = tempString.replaceFirst("<textarea>", "").replaceFirst("</textarea>", "");
					jsonString = jsonString.replaceAll("&quot;", "\"");
	
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject)jsonParser.parse(jsonString);
					
					r.responseJSON = jsonObject;				
					
					//add tags if they were specified
					if(r.responseCode == 200 && tags != null && !tags.isEmpty()) {
						
						HttpURLConnection tagConn = null;
						DataOutputStream tagOutput = null;
						try {											
							
							String importedID = (String)jsonObject.get("id");
							logger.debug("Imported application UID = {}", importedID);
							
							if(importedID != null) {
							
								logger.debug("Adding tags to the imported application = {}", tags);
								
								url = this.appUrlBase + "/" + importedID + "/tags";						
								logger.debug("REST URL = {}", url);	
								
								HashMap<String,String> tagHeaders = new HashMap<String,String>();
								tagHeaders.put("Content-Type", TEXT_MEDIATYPE);
								tagConn = this.establishURLConnection(url, HTTP_PUT, tagHeaders, false);
								tagOutput = new DataOutputStream(tagConn.getOutputStream());
								tagOutput.writeBytes(tags);
								tagOutput.flush();
								tagOutput.close();	
								
								logger.debug("TAG Response Code = {}, Response Message = {}", tagConn.getResponseCode(), tagConn.getResponseMessage());
							}
							
						} catch (Exception e) {
							throw new FEBAPIException(e.getMessage(), e);
						} finally {
							if(tagConn != null) {							
						        tagConn.disconnect();					        
							}
							if(tagOutput != null) {
								tagOutput.close();
							}
						}
					}
				}
			}	
			
			return r;
		    
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
		        conn.disconnect();
			}
		}
	}
	
	public FEBResponse listAppsForUser (Integer pageNum, Integer pageSize, String sortBy, String orderBy) throws FEBAPIException {
		
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;
		String url = this.orgUrlBase + "myapps"; //this.adminUrlBase + "apps"; //
		
		if(pageNum != null) {
			logger.debug("Adding param...{}", "page=" + pageNum);		
			url = this.addURLParam(url, "page=" + pageNum);							
		}
		
		if(pageSize != null) {
			logger.debug("Adding param...{}", "pageSize=" + pageSize);
			url = this.addURLParam(url, "pageSize=" + pageSize);
		}
		
		if(sortBy != null && !sortBy.isEmpty()) {
			logger.debug("Adding param...{}", "sortBy=" + sortBy);
			url = this.addURLParam(url, "sortBy=" + sortBy);
		}
		
		if(orderBy != null && !orderBy.isEmpty()) {
			logger.debug("Adding param...{}", "order=" + orderBy);
			url = this.addURLParam(url, "order=" + orderBy);
		}
		
		logger.debug("REST URL = {}", url);
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", ATOM_MEDIATYPE);
		headers.put("Content-Type", ATOM_MEDIATYPE);
		conn = this.establishURLConnection(url, HTTP_GET, headers, false);
		
		populateFEBResponse(conn, r);
		
		return r;
	}
	
	/**
	 * 
	 */
	public FEBResponse adminListApps(Integer pageNum, Integer pageSize, String sortBy, String orderBy) throws FEBAPIException {
		
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;
		String url = this.adminUrlBase + "apps";
		
		if(pageNum != null) {
			logger.debug("Adding param...{}", "page=" + pageNum);		
			url = this.addURLParam(url, "page=" + pageNum);							
		}
		
		if(pageSize != null) {
			logger.debug("Adding param...{}", "pageSize=" + pageSize);
			url = this.addURLParam(url, "pageSize=" + pageSize);
		}
		
		if(sortBy != null && !sortBy.isEmpty()) {
			logger.debug("Adding param...{}", "sortBy=" + sortBy);
			url = this.addURLParam(url, "sortBy=" + sortBy);
		}
		
		if(orderBy != null && !orderBy.isEmpty()) {
			logger.debug("Adding param...{}", "order=" + orderBy);
			url = this.addURLParam(url, "order=" + orderBy);
		}
		
		logger.debug("REST URL = {}", url);
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", JSON_MEDIATYPE);
		headers.put("Content-Type", JSON_MEDIATYPE);
		conn = this.establishURLConnection(url, HTTP_GET, headers, false);
		
		populateFEBResponse(conn, r);
		
		if(r.responseCode == 404) {
			throw new FEBAPIException("User is not part of the AdministrativeUsers role.");
		}
		
		return r;
	}
	
	public FEBResponse stopApplication(String appid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		logger.debug("Stopping FEB Application " + appid);
		
		startStopApplicationImpl(appid, APP_STOP);
		
		return r;
	}
	
	private FEBResponse startStopApplicationImpl(String appid, String action) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		DataOutputStream dos = null;
		PrintStream fr = null;		
		HttpURLConnection conn = null;
		
		//https://tapintofeb.victoria.ibm.com/forms-basic/secure/org/app/c092624c-a5cf-4204-88e9-cf318b124263/deployed/latest?tzOffset=-28800
		String url = this.appUrlBase + "/" + appid + "/deployed/latest";
		
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", "application/atom+xml");
		headers.put("Content-Type", "application/atom+xml");
		conn = this.establishURLConnection(url, HTTP_PUT, headers, false);
		
		String actionStr = "";
		
		if(APP_STOP.equals(action))
			actionStr = "false";
		else if(APP_START.equals(action))
			actionStr = "true";
		
		String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<a:entry xmlns:a=\"http://www.w3.org/2005/Atom\">\r\n" + 
				"  <a:content type=\"application/xml\">\r\n" + 
				"    <deploySettings>\r\n" + 
				"      <deployStatus started=\"" + actionStr + "\" deployed=\"" + actionStr + "\" sync=\"false\"></deployStatus>\r\n" + 
				"    </deploySettings>\r\n" + 
				"  </a:content>\r\n" + 
				"</a:entry>";
		
		try {
			dos = new DataOutputStream(conn.getOutputStream());
			fr = new PrintStream(dos);
			fr.print(xmlData);
			fr.flush();
			fr.close();
			
			populateFEBResponse(conn, r);
			
		} catch (Exception e) {
			throw new FEBAPIException(e.getMessage(), e);
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
			
			if(dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					throw new FEBAPIException(e.getMessage(), e);
				}
			}
		}
		
		return r;
	}
	
	public FEBResponse startApplication(String appid) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		logger.debug("Starting FEB Application " + appid);
		
		startStopApplicationImpl(appid, APP_START);
		
		return r;
	}
	
	public FEBResponse getSampleJSONForForm (String appUid, String formId) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		
		r = getCompleteJSONForForm(appUid, formId);
		
		if(r.isResponse20x()) {
			if(r.responseJSON != null) {
				logger.debug(r.responseJSON.toJSONString());
			
				JSONObject definitions = (JSONObject) r.responseJSON.get("definitions");
				JSONObject entries = (JSONObject) definitions.get("Entries");
				JSONObject example = (JSONObject) entries.get("example");
				JSONArray ar = (JSONArray) example.get("items");
				
				JSONObject theJson = (JSONObject) ar.get(0);
				clearValuesInJson(theJson);
				
				r.responseJSON = theJson;
			}
		}
		
		return r;
	}
	
	public FEBResponse getCompleteJSONForForm (String appUid, String formId) throws FEBAPIException {
		FEBResponse r = new FEBResponse();
		HttpURLConnection conn = null;
		
		if(appUid == null || "".equals(appUid))
			throw new FEBAPIException("App Id is required.");
		
		if(formId == null || "".equals(formId))
			throw new FEBAPIException("Form Id is required.");
		
		String url = this.urlBase + appUid + "/" + formId + "/swagger.json";
		
		HashMap<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", "application/json");
		
		conn = this.establishURLConnection(url, HTTP_GET, headers, true);
		
		populateFEBResponse(conn, r);
		
		return r;
	}
	
	/**
	 * Generates a number as a string based on the current date that can be used as the freedomIdentifyKey.
	 * @return
	 */
	public String generateFreedomIdentifyKey() {
		return Integer.toString((int) new Date().getTime());
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private String printStreamToString(InputStream is) throws IOException {
		
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    String line;
	    StringBuffer sb = new StringBuffer(); 
	    while((line = rd.readLine()) != null) {
	      sb.append(line);
	      sb.append('\r');
	    }
	    rd.close();
	    
	    return sb.toString();
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String readFile(File file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	
	private void clearValuesInJson(JSONObject theJson) {
		Set keySet = theJson.keySet();
		Iterator keyIter = keySet.iterator();
		while(keyIter.hasNext() ) {
			String key = (String) keyIter.next();
			Object curVal = theJson.get(key);
			
			if(key.equals("F_QFS_AnimalByProdTable")) {
				System.out.println("found table");
			}
			
			if(curVal instanceof String || curVal instanceof Long) {
				theJson.put(key, "");
			} else if(curVal instanceof Boolean) {
				theJson.put(key, false);
			} else {
				//theJson.put(key, new JSONObject());
				//clear its children
				JSONObject curObj = (JSONObject) theJson.get(key);
				Set objKeySet = curObj.keySet();
				Iterator objKeyIter = objKeySet.iterator();
				while(objKeyIter.hasNext()) {
					String objKey = (String) objKeyIter.next();
					
					if(curObj.get(objKey) instanceof String || curObj.get(objKey) instanceof Long) {
						curObj.put(objKey, "");
					} else if(curObj.get(objKey) instanceof JSONArray) {
						curObj.put(objKey, new JSONArray());
					}
				}
				theJson.put(key, curObj);
			}
		}
	}
}
