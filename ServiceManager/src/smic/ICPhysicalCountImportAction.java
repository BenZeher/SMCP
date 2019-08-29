package smic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import SMDataDefinition.SMTableicinventoryworksheet;

public class ICPhysicalCountImportAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_FIELDS_PER_LINE = 2;
	private static final int FIELD_ITEM = 0;
	private static final int FIELD_QTY = 1;
	
	private static final String sICPhysicalCountImportActionCallingClass = "smic.ICPhysicalCountImportSelect";
	private static boolean bDebugMode = false;
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (bDebugMode){
			System.out.println("In " + this.toString() + " 01");
		}
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditPhysicalInventory))
		{
			return;
		}


	    //Get the session info:
		ICPhysicalInventoryEntry objICPhysicalInventoryEntry = new ICPhysicalInventoryEntry();
	    HttpSession CurrentSession = request.getSession(true);
		try {
			CurrentSession.removeAttribute(ICPhysicalCountImportSelect.IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sDBID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

	    
	    String sPhysicalInventoryID = "";
		String m_sCountDesc = "";
		boolean bIncludesHeaderRow = false;
	    boolean bAddNewItems = false;
	    FileItem m_uploadedFile = null;
	    
	    //Get the form variables here:
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4196);
        // the location for saving data that is larger than getSizeThreshold()
        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum size before a FileUploadException will be thrown
        upload.setSizeMax(1000000);
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        
        if (bDebugMode){
        	System.out.println("In " + this.toString() + ".writeFileAndProcess - isMultipart = " + isMultipart);
        }
		List<FileItem> fileItems = null;
		try {
			fileItems = upload.parseRequest(request);
		} catch (FileUploadException e1) {
			if (bDebugMode){
				System.out.println("Error [1548956116] In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			}
			CurrentSession.setAttribute(ICPhysicalCountImportSelect.IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT, "Error [1548956116] In " + this.toString() + " error on upload.parseRequest");
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
				return;
		}
		Iterator<FileItem> iter = fileItems.iterator();
		while (iter.hasNext()) {
			FileItem item = null;
		    item = (FileItem) iter.next();
		    if (item.isFormField()) {
		    	
		    	if (item.getFieldName().compareToIgnoreCase(ICPhysicalCountEntry.ParamDesc) == 0){
		    		m_sCountDesc = item.getString();		    		
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ ICPhysicalCountEntry.ParamDesc + " = " + m_sCountDesc + "."); 
					}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(ICPhysicalInventoryEntry.ParamID) == 0){
		    		sPhysicalInventoryID = item.getString();
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ ICPhysicalInventoryEntry.ParamID + " = " + sPhysicalInventoryID + "."); 
					}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(
		    			ICPhysicalCountImportSelect.PARAM_INCLUDES_HEADER_ROW) == 0){
		    			bIncludesHeaderRow = true;
							if (bDebugMode){
								System.out.println(
									"In " + this.toString() 
									+ ".writeFileAndProcess, parameter "
									+ ICPhysicalCountImportSelect.PARAM_INCLUDES_HEADER_ROW + " = " + item.getString() + "."); 
							}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(
		    			ICPhysicalCountImportSelect.PARAM_INCLUDE_NEW_ITEMS) == 0){
		    				bAddNewItems = true;
							if (bDebugMode){
								System.out.println(
									"In " + this.toString() 
									+ ".writeFileAndProcess, parameter "
									+ ICPhysicalCountImportSelect.PARAM_INCLUDE_NEW_ITEMS + " = " + item.getString() + "."); 
							}
		    	}
		    	
		    }else{
		    	//It's a file - 
		    	m_uploadedFile = item;
		    }
	    	
		}
	    
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + ".doPost - contenttype: " 
	    		+ request.getContentType()
	    		+ " - getRequestURI: "
	    		+ request.getRequestURI()
	    	);

			Enumeration<?> headerNames = request.getHeaderNames();
	    	while (headerNames.hasMoreElements()){
	    		String sHeaderName = headerNames.nextElement().toString();
	    		if (bDebugMode){
	    			System.out.println("headerName = " + sHeaderName);
	    			System.out.println("headerNameValue = " + request.getHeader(sHeaderName));
	    		}
	    	}
	    }
	    String Error = "";
	    try {
			processRequest(
				CurrentSession, 
				request, 
				out, 
				sDBID,
				sUserID,
				sUserFullName, 
				sPhysicalInventoryID, 
				bIncludesHeaderRow, 
				bAddNewItems,
				m_sCountDesc,
				m_uploadedFile,
				objICPhysicalInventoryEntry);
		} catch (Exception e) {
			//This will be handled below
		}
	    //Warnings Occured
		Error += objICPhysicalInventoryEntry.getErrorMessages();
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
				+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
				+ "?Warning=" + Error
				+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
				+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}		
		if(!Error.equalsIgnoreCase("")) {
		CurrentSession.setAttribute(ICPhysicalCountImportSelect.IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT, (String)Error);
    	response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
				+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
				+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
			return;
		}else {
			//No Warnings
    	String sPhysicalCountImportStatus = "Import completed without errors.";
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".doPost - processRequest succeeded: "
				+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
				+ "?Status=" + sPhysicalCountImportStatus
				+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
				+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID				
			);
		}
    	response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
				+ "?Status=" + sPhysicalCountImportStatus
				+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
				+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		
		return;
		}
	}
	private void createTempImportFileFolder(String sTempFileFolder, ICPhysicalInventoryEntry objICPhysicalInventoryEntry) throws Exception{
	    File dir = new File(sTempFileFolder);
	    if (dir.exists()) {
	      return;
	    }
	    
	    //Need to create the path:
	    try{
	        // Create one directory
	        if (!new File(sTempFileFolder).mkdir()) {
	        	objICPhysicalInventoryEntry.addErrorMessage("<BR>Error [1548955603] creating temp upload folder.");
	        	return;
	        }    
        }catch (Exception e){//Catch exception if any
        	objICPhysicalInventoryEntry.addErrorMessage("<BR>Error [1548955604] creating temp upload folder: " + e.getMessage() + ".");
        	return;
	    }
	    return;
	}
	private void processRequest(
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sPhysicalInventoryID,
			boolean bIncludesHeaderRow,
			boolean bAddNewItems,
			String sCountDesc,
			FileItem fUploadedFile,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry
			) throws Exception{

    	String sTempFilePath = SMUtilities.getAbsoluteRootPath(req, getServletContext())
			//+ System.getProperty("file.separator")
			+ "iccountuploads"
		;

    	//If the folder has not been created, create it now:
		try {
			createTempImportFileFolder(sTempFilePath, objICPhysicalInventoryEntry);
		} catch (Exception e) {
			objICPhysicalInventoryEntry.addErrorMessage(e.getMessage());
			return;
		}
    	
		//First, remove any temporary files:
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e) {
			objICPhysicalInventoryEntry.addErrorMessage(e.getMessage());
			return;
		}

		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processRequest - going into writeFileAndProcess");
		}
		
		try {
			writeFileAndProcess(
				sTempFilePath, 
				ses, 
				req, 
				pwOut, 
				sDBID,
				sUserID,
				sUserFullName,
				sPhysicalInventoryID,
				bIncludesHeaderRow,
				bAddNewItems,
				sCountDesc,
				fUploadedFile,
				objICPhysicalInventoryEntry);
		} catch (Exception e1) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956216] processing import - " + e1.getMessage());
			throw e1;
		}
		
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e) {
			objICPhysicalInventoryEntry.addErrorMessage(e.getMessage());
			throw e;
		}
		System.out.println("[20192391348330] CURRENTTEMPBATHDELETED");
		return;
		
	}
	private void writeFileAndProcess(
			String sTempImportFilePath,
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sPhysicalInventoryID,
			boolean bIncludesHeaderRow,
			boolean bAddNewItems,
			String sCountDesc,
			FileItem fUploadedFile,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry
	) throws Exception{


		String fileName = "ICIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
        try {
			fUploadedFile.write(new File(sTempImportFilePath, fileName));
		} catch (Exception e) {
			//System.out.println("Error [1548956219]  error on fi.write: " + e.getMessage());
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956218] writing temporary file: " + e.getMessage());
			return;
		}
		
		if (bDebugMode){
			clsServletUtilities.sysprint(
				this.toString(), 
				sUserFullName, 
				"in writeFileAndProcess - bIncludeHeaderRow = " + bIncludesHeaderRow);
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess going into validateFile");
		}
		
		try {
			validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow, bAddNewItems, sDBID, sUserID, sUserFullName, sPhysicalInventoryID,objICPhysicalInventoryEntry);
		} catch (Exception e1) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956218] validating import file - " + e1.getMessage());
			throw e1;
		}
		//Get a connection:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".writeFileAndProcess - user: " + sUserID
				+ " - "
				+ sUserFullName
						)
		);
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess got a connection");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956219] starting data transaction - ");
			return;
		}
		
		if (bDebugMode){
			System.out.println("Error [1548956219] In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		//We'll need a physical inventory object to process the file:
		
		objICPhysicalInventoryEntry.slid(sPhysicalInventoryID);
		if (!objICPhysicalInventoryEntry.load(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1548956220]");
			objICPhysicalInventoryEntry.addErrorMessage("Error [1538513151] - Could not load physical inventory - " + objICPhysicalInventoryEntry.getErrorMessages());
			return;
		}
		System.out.println("[20192391348330] load physical inventory");
		try {
			insertRecords(
				sTempImportFilePath, 
				fileName, 
				conn, 
				bIncludesHeaderRow, 
				bAddNewItems, 
				objICPhysicalInventoryEntry,
				sDBID,
				sUserID,
				sUserFullName,
				sPhysicalInventoryID,
				sCountDesc);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			objICPhysicalInventoryEntry.addErrorMessage("Error [1538513251] inserting records - " + e.getMessage());
			throw e;
		}
		System.out.println("[20192391348330] RECORDS INSERTED");
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			objICPhysicalInventoryEntry.addErrorMessage("Error [1538513252] Could not commit data transaction.");
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080880]");
		return;

	}
	private void insertRecords(
			String sFilePath, 
			String sFileName, 
			Connection conn, 
			boolean bFileIncludesHeaderRow,
			boolean bAddNewItems,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry,
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sPhysicalInventoryID,
			String sCountDesc
			) throws Exception{
		
		String sCountID = "";
		try {
			sCountID = insertCount(conn, sDBID, sUserID, sUserFullName, sPhysicalInventoryID, sCountDesc);
		} catch (Exception e) {
			conn.rollback();
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956704] inserting count - " + e.getMessage());
			throw e;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".insertRecords going into insertCountLines");
		}

		try {
			insertCountLines(
				sFilePath, 
				sFileName, 
				conn, 
				bFileIncludesHeaderRow, 
				bAddNewItems, 
				objICPhysicalInventoryEntry, 
				sPhysicalInventoryID,
				sCountID);
		} catch (Exception e) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548957068] inserting lines from the count - " + e.getMessage());
			throw e;
		}
		
		return;
	}
	private String insertCount(
			Connection conn,
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sPhysicalInventoryID,
			String sCountDesc
	) throws Exception{
		
		ICPhysicalCountEntry count = new ICPhysicalCountEntry();
		count.setsCreatedByID(sUserID);
		count.setsCreatedByFullName(sUserFullName);
		count.setsDescription(sCountDesc);
		count.setsPhysicalInventoryID(sPhysicalInventoryID);

		if (!count.save_without_data_transaction(conn, sUserID, sUserFullName)){
			throw new Exception("Error [1548956615] - Could not save count - " + count.getErrorMessages());
		}else{
			return count.slid();
		}
	}
	private void insertCountLines(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			boolean bAddNewItems,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry,
			String sPhysicalInventoryID,
			String sCountID
	) throws Exception{
		
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
			String sQty = "";
			String sItem = "";
			while ((line = br.readLine()) != null) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".insertCountLines - at line " + iLineCounter);
				}
				iLineCounter++;
				//If the file has a header row and if this is the first line, then it's the header line
				//so reset the line counter and don't do any validation of it:
				if (
						bFileIncludesHeaderRow
						&& (iLineCounter == 1)
					)
				{
				//Otherwise, if it's NOT the first row of a file with a header row, process:
				}else{
					int iFieldCounter = 0;
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						if (iFieldCounter == FIELD_QTY){
							sQty = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ITEM){
							sItem = sDelimitedField.trim().replace("\"", "");
							//TODO - possibly change this logic later:
							//Strip off everything after and including any hyphen at the end of the item:
							sItem = stripSuffix(sItem);
						}
						iFieldCounter++;
					}
					
					ICPhysicalCountLineEntry countline = new ICPhysicalCountLineEntry();
					countline.setsCountID(sCountID);
					countline.setsItemNumber(sItem);
					countline.setsPhysicalInventoryID(sPhysicalInventoryID);
					countline.setsQty(sQty);
	
					if(!countline.save_without_data_transaction(conn, bAddNewItems)){
						objICPhysicalInventoryEntry.addErrorMessage("Error [1538512163] - Could not save line " + iLineCounter + " - " + countline.getErrorMessages());
						return;
					}
					
					//Add any NEW items
					if (bAddNewItems){
						objICPhysicalInventoryEntry.addSingleItem(sItem, conn);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956904] - File not found error reading file:= " + ex.getMessage() + ".");
			return;
		} catch (IOException ex) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956905] - IO exception error reading file:= " + ex.getMessage() + ".");
			return;
		} catch (Exception ex) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548956906] - " + ex.getMessage());
			return;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				objICPhysicalInventoryEntry.addErrorMessage("Error [1548956906] IO exception error reading file:= " + ex.getMessage() + ".");
				return;
			}
		}
		return;
	}
	private String stripSuffix(String sItemNum){
		String sReturnString = sItemNum;
		
		int i = sItemNum.lastIndexOf("-");
		if (i > 0){
			sReturnString = sReturnString.substring(0, i);
		}
		
		return sReturnString;
	}
	private void deleteCurrentTempImportFiles(String sTempImportFilePath) throws Exception{
		
		//TODO - uncomment this:
		/*
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	throw new Exception("<BR>Error [[1548955604]] Temp import file directory does not exist: " + sTempImportFilePath);
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  throw new Exception("<BR>Error [[1548955604]] Unable to delete " + sTempImportFilePath + info[i]);
	      } 
	    }
	    */
	    return;

	}
	private void validateFile(
		String sFilePath, 
		String sFileName, 
		boolean bFileIncludesHeaderRow, 
		boolean bAddNewItems, 
		String sDBID, 
		String sUserID, 
		String sUserFullName,
		String sPhysicalInventoryID,
		ICPhysicalInventoryEntry objICPhysicalInventoryEntry) throws Exception {

		BufferedReader br = null;
		String sFullFileName = "";
		try {
			sFullFileName = sFilePath + System.getProperty("file.separator") + sFileName;
			//System.out.println("[1548962601] sFullFileName = '" + sFullFileName + "'.");
			br = new BufferedReader(new FileReader(sFullFileName));
			String line = null;
			int iLineCounter = 0;
			String errors = "";
			while ((line = br.readLine()) != null) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".validateFile - at line " + iLineCounter);
				}
				iLineCounter++;
				
				//If the file has a header row and if this is the first line, then it's the header line
				//so reset the line counter and don't do any validation of it:
				if (
						bFileIncludesHeaderRow
						&& (iLineCounter == 1)
					)
				{
				//Otherwise, if it's NOT the first row of a file with a header row, process:
				}else{
					int iFieldCounter = 0;
					
					String[] fields = line.split(",");
					//System.out.println("[1548962801] - line = '" + line + "'");

					for (String sDelimitedField : fields) {
						
						//System.out.println("[1548962802] - field = '" + sDelimitedField + "'");
						
						if (iFieldCounter > NUMBER_OF_FIELDS_PER_LINE){
							//Allow additional fields, just ignore them:
							//this.addToErrorMessage("<BR>Line number " + iLineCounter + " has more than " 
							//	+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
							//bResult = false;
						}else{

							
							try {
								validateImportField(
										iLineCounter, 
										iFieldCounter, 
										sDelimitedField.trim().replace("\"", ""),
										bAddNewItems, 
										sDBID,
										sUserID,
										sUserFullName,
										sPhysicalInventoryID,
										objICPhysicalInventoryEntry);
							} catch (Exception e) {
								br.close();
								errors += "Error [1548957655] validating import file - " + e.getMessage();
							}
							iFieldCounter++;
						} // end else
					} //End 'for'

					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						br.close();
						objICPhysicalInventoryEntry.addErrorMessage("Error [1548957349] - Line number " + iLineCounter + " has less than " 
							+ NUMBER_OF_FIELDS_PER_LINE + " fields in it ('" + line + "').");
						return;
					}
				}
			}
			if(objICPhysicalInventoryEntry.getErrorMessages().compareToIgnoreCase("")!=0) {
				objICPhysicalInventoryEntry.addErrorMessage(errors);
				throw new Exception(errors);
				
			}

			if (iLineCounter == 0){
				br.close();
				objICPhysicalInventoryEntry.addErrorMessage("Error [1548957144] - The file has no lines in it.");
				return;
			}
		} catch (FileNotFoundException ex) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1548957164] - File '" 
					+ sFullFileName + "' not found - " + ex.getMessage() + ".");
			throw ex;
		} catch (IOException ex) {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				objICPhysicalInventoryEntry.addErrorMessage("Error [1548957154] IO exception error reading file:= " + e.getMessage() + ".");
				throw e;
			}
			objICPhysicalInventoryEntry.addErrorMessage("Error [15489571744] IO exception error reading file '" + sFullFileName + "'- " + ex.getMessage() + ".");
			throw ex;
		}
		br.close();
		return;
	}

	private void validateImportField (
			int iLineNumber, 
			int iFieldIndex, 
			String sField, 
			boolean bAddNewItems, 
			String sDBID, 
			String sUserID, 
			String sUserFullName,
			String sPhysicalInventoryID,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry) throws Exception{
		
		//Strip off any quotation marks:
		sField = sField.replace("\"", "");
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + "FieldIndex = " + iFieldIndex 
				+ ", value = " + sField);
		}
		if (iFieldIndex == FIELD_QTY){
			//Make sure it's a valid qty:
	    	try {
				BigDecimal bdqty = new BigDecimal(sField);
				//Allow zero quantities, but nothing less
				if (bdqty.compareTo(BigDecimal.ZERO) < 0){
					objICPhysicalInventoryEntry.addErrorMessage("Error [1548957509] - Invalid qty ('" + sField + "') on line " + iLineNumber + ".");
					return;
				}
			} catch (Exception e) {
				objICPhysicalInventoryEntry.addErrorMessage(e.getMessage());
				throw e;
			}
		}
		if (iFieldIndex == FIELD_ITEM){
			//Make sure it's a valid item in the range from the physical inventory:
			sField = stripSuffix(sField);
			//System.out.println(sFieldBefore + " - " + sField);
			try {
				validateItemNumber(iLineNumber, sField, bAddNewItems, sDBID, sUserID, sUserFullName, sPhysicalInventoryID,objICPhysicalInventoryEntry);
			} catch (Exception e) {
				objICPhysicalInventoryEntry.addErrorMessage("Error [1548958965] validating - " + e.getMessage());
				throw e;
			}
		}
		return;
	}
	private void validateItemNumber(
			int iLineNumber, 
			String sItem, 
			boolean bAddNewItems, 
			String sDBID, 
			String sUserID, 
			String sUserFullName,
			String sPhysicalInventoryID,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry) throws Exception{
		//TODO ITS WORKING HERE
		//Make sure it's in the range for the physical inventory:
		String SQL = "";
		if (!bAddNewItems){
			SQL = "SELECT"
				+ " " + SMTableicinventoryworksheet.sitemnumber + " FROM " + SMTableicinventoryworksheet.TableName
				+ " WHERE (" 
					+ " " + SMTableicinventoryworksheet.sitemnumber + " = '" + sItem + "'"
					+ " AND" 
					+ " " + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + sPhysicalInventoryID
				+ ")"
				;
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".validateItemNumber SQL = " + SQL);
			}
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						SMUtilities.getFullClassName(this.toString() + ".validateItemNumber - user: " + sUserFullName)
						);
				
				if (!rs.next()){
					rs.close();
					objICPhysicalInventoryEntry.addErrorMessage("Error [1344008030] - Invalid item number ('" + sItem + "') on line number " + iLineNumber + ""
							+ " - item is not included in this physical inventory.");
					return;
				}
				rs.close();
			} catch (SQLException e) {
				objICPhysicalInventoryEntry.addErrorMessage("Error [1548958827] - SQL Error validating item number ('" + sItem + "') on line number " 
				+ iLineNumber + " with SQL - " + SQL + " - " + e.getMessage() + ".");
				return;
			}
		}
		
		//Make sure it's a valid item number:
		SQL = "SELECT"
			+ " " + SMTableicitems.sItemNumber + " FROM " + SMTableicitems.TableName
			+ " WHERE (" 
				+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".validateItemNumber SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".validateItemNumber - user: " + sUserFullName)
					);
			
			if (!rs.next()){
				rs.close();
				objICPhysicalInventoryEntry.addErrorMessage("Error [1344008064] - Invalid item number ('" + sItem + "') on line number " + iLineNumber + ".");
				return;
			}
			rs.close();
		} catch (SQLException e) {
			objICPhysicalInventoryEntry.addErrorMessage("Error [1344008094] - SQL Error validating item number ('" + sItem + "') on line number " 
			+ iLineNumber + " - " + e.getMessage() + ".");
			return;
		}
		
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
