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
	
	//Member variables for the count:
	private static String m_sPhysicalInventoryID = "";
	private static String m_sCountID = "";
	private static String m_sCountDesc = "";
	//private static String m_sValidStartingItem = ""; //TJR - removed these 10/1/2018
	//private static String m_sValidEndingItem = "";
    
	private static String sPhysicalCountImportStatus = "";
	private static String sPhysicalCountImportError = "";
	private static String sICPhysicalCountImportActionCallingClass = "smic.ICPhysicalCountImportSelect";
	private static boolean bDebugMode = false;
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
	    HttpSession CurrentSession = request.getSession(true);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sDBID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

	    
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
	    
	    if (!processRequest(CurrentSession, request, out, sDBID,sUserID,sUserFullName)){
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?Warning=" + sPhysicalCountImportError
					+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + m_sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}		
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?Warning=" + sPhysicalCountImportError
					+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + m_sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }else{
	    	sPhysicalCountImportStatus = "Import completed without errors.";
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest succeeded: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?Status=" + sPhysicalCountImportStatus
					+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + m_sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID				
				);
			}
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?Status=" + sPhysicalCountImportStatus
					+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + m_sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
		
		return;
	}
	private boolean createTempImportFileFolder(String sTempFileFolder){
	    File dir = new File(sTempFileFolder);
	    if (dir.exists()) {
	      return true;
	    }
	    
	    //Need to create the path:
	    try{
	        // Create one directory
	        if (!new File(sTempFileFolder).mkdir()) {
	        	this.addToErrorMessage("<BR>Error creating temp upload folder.");
	        	return false;
	        }    
        }catch (Exception e){//Catch exception if any
        	this.addToErrorMessage("<BR>Error creating temp upload folder: " + e.getMessage() + ".");
	    }
	    return true;
	}
	private boolean processRequest(
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName
			){

    	String sTempFilePath = SMUtilities.getAbsoluteRootPath(req, getServletContext())
			+ System.getProperty("file.separator")
			+ "iccountuploads"
		;

    	//If the folder has not been created, create it now:
		if (!createTempImportFileFolder(sTempFilePath)){
			return false;
		}
    	
		//First, remove any temporary files:
		if (!deleteCurrentTempImportFiles(sTempFilePath)){
			return false;
		}

		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processRequest - going into writeFileAndProcess");
		}
		
		boolean bResult = writeFileAndProcess(
			sTempFilePath, 
			ses, 
			req, 
			pwOut, 
			sDBID,
			sUserID,
			sUserFullName);
		deleteCurrentTempImportFiles(sTempFilePath);
		return bResult;
		
	}
	@SuppressWarnings("unchecked")
	private boolean writeFileAndProcess(
			String sTempImportFilePath,
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName
	){
		//Check to see if the file has a header row:
		boolean bIncludesHeaderRow = false;
		boolean bAddNewItems = false;
		
	    //Read the file from the request:
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4196);
        // the location for saving data that is larger than getSizeThreshold()
        factory.setRepository(new File(sTempImportFilePath));
        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum size before a FileUploadException will be thrown
        upload.setSizeMax(1000000);
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        
        if (bDebugMode){
        	System.out.println("In " + this.toString() + ".writeFileAndProcess - isMultipart = " + isMultipart);
        }
		List<FileItem> fileItems = null;
		try {
			fileItems = upload.parseRequest(req);
		} catch (FileUploadException e1) {
			this.addToErrorMessage("<BR>"
				+ "Error on upload.parseRequest: " + e1.getMessage());
			if (bDebugMode){
				System.out.println("In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			}
			e1.printStackTrace();
			return false;
		}
		Iterator<FileItem> iter = fileItems.iterator();
		String fileName = "ICIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		    if (item.isFormField()) {
		    	if (item.getFieldName().compareToIgnoreCase("CallingClass") == 0){
		    		sICPhysicalCountImportActionCallingClass = item.getString();
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter CallingClass = " + sICPhysicalCountImportActionCallingClass + "."); 
					}		
		    	}
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
		    		m_sPhysicalInventoryID = item.getString();
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ ICPhysicalInventoryEntry.ParamID + " = " + m_sPhysicalInventoryID + "."); 
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
		    	
		    } else {
		    	//It's a file - 
		    	FileItem fi = item;
		    	//String fileName = fi.getName();
		    	
		        // write the file
		        try {
					fi.write(new File(sTempImportFilePath, fileName));
				} catch (Exception e) {
					this.addToErrorMessage("<BR>"
						+ "Error writing temporary file: " + e.getMessage());
					System.out.println("In " + this.toString() + " error on fi.write: " + e.getMessage());
					return false;
				}
				//InputStream uploadedStream = item.getInputStream();
		    }
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
		if (!validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow, bAddNewItems, sDBID, sUserID, sUserFullName)){
			return false;
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
		
		boolean bResult = true;
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			this.addToErrorMessage("Could not start data transaction.");
			bResult =  false;
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		//We'll need a physical inventory object to process the file:
		ICPhysicalInventoryEntry objICPhysicalInventoryEntry = new ICPhysicalInventoryEntry();
		
		objICPhysicalInventoryEntry.slid(m_sPhysicalInventoryID);
		if (!objICPhysicalInventoryEntry.load(conn)){
			this.addToErrorMessage("Error [1538513151] - Could not load physical inventory - " + objICPhysicalInventoryEntry.getErrorMessages());
		}
		
		if (!insertRecords(sTempImportFilePath, fileName, conn, bIncludesHeaderRow, bAddNewItems, objICPhysicalInventoryEntry,sDBID,sUserID,sUserFullName)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			bResult = false;
		}else{
			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				this.addToErrorMessage("Could not commit data transaction.");
				bResult = false;
			}
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080880]");
		return bResult;

	}
	private boolean insertRecords(
			String sFilePath, 
			String sFileName, 
			Connection conn, 
			boolean bFileIncludesHeaderRow,
			boolean bAddNewItems,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry,
			String sDBID,
			String sUserID,
			String sUserFullName
			){
		if (!insertCount(conn, sDBID, sUserID, sUserFullName)){
			return false;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".insertRecords going into insertCountLines");
		}
		if (!insertCountLines(sFilePath, sFileName, conn, bFileIncludesHeaderRow, bAddNewItems, objICPhysicalInventoryEntry)){
			return false;
		}
		return true;
	}
	private boolean insertCount(
			Connection conn,
			String sDBID,
			String sUserID,
			String sUserFullName
	){
		
		ICPhysicalCountEntry count = new ICPhysicalCountEntry();
		count.setsCreatedByID(sUserID);
		count.setsCreatedByFullName(sUserFullName);
		count.setsDescription(m_sCountDesc);
		count.setsPhysicalInventoryID(m_sPhysicalInventoryID);

		if (!count.save_without_data_transaction(conn, sUserID, sUserFullName)){
			this.addToErrorMessage("<BR>" + "Could not save count - " + count.getErrorMessages());
			return false;
		}else{
			m_sCountID = count.slid();
			return true;
		}
	}
	private boolean insertCountLines(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			boolean bAddNewItems,
			ICPhysicalInventoryEntry objICPhysicalInventoryEntry
	){
		
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
					countline.setsCountID(m_sCountID);
					countline.setsItemNumber(sItem);
					countline.setsPhysicalInventoryID(m_sPhysicalInventoryID);
					countline.setsQty(sQty);
	
					if(!countline.save_without_data_transaction(conn, bAddNewItems)){
						this.addToErrorMessage("Error [1538512163] - Could not save line " + iLineCounter + " - " + countline.getErrorMessages());
						return false;
					}
					
					//Add any NEW items
					if (bAddNewItems){
						objICPhysicalInventoryEntry.addSingleItem(sItem, conn);
					}
				}
			}
		} catch (FileNotFoundException ex) {
			this.addToErrorMessage("<BR>" + "File not found error reading file:= " + ex.getMessage() + ".");
			return false;
		} catch (IOException ex) {
			this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".");
			return false;
		} catch (Exception ex) {
			this.addToErrorMessage("<BR>" + ex.getMessage() + ".");
			return false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".");
				return false;
			}
		}
		return true;
	}
	private String stripSuffix(String sItemNum){
		String sReturnString = sItemNum;
		
		int i = sItemNum.lastIndexOf("-");
		if (i > 0){
			sReturnString = sReturnString.substring(0, i);
		}
		
		return sReturnString;
	}
	private boolean deleteCurrentTempImportFiles(String sTempImportFilePath){
		
		boolean bDeletionSuccessful = true;
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	this.addToErrorMessage("<BR>Temp import file directory does not exist: " + sTempImportFilePath);
	      return false;
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  this.addToErrorMessage("Unable to delete " + sTempImportFilePath + info[i]);
	    	  bDeletionSuccessful = false;
	      } 
	    }
	    return bDeletionSuccessful;

	}
	private boolean validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow, boolean bAddNewItems, String sDBID, String sUserID, String sUserFullName) {

		BufferedReader br = null;
		boolean bResult = true;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
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
					for (String sDelimitedField : fields) {
						if (iFieldCounter > NUMBER_OF_FIELDS_PER_LINE){
							//Allow additional fields, just ignore them:
							//this.addToErrorMessage("<BR>Line number " + iLineCounter + " has more than " 
							//	+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
							//bResult = false;
						}else{
							if (!validateImportField(
									iLineCounter, iFieldCounter, sDelimitedField.trim().replace("\"", ""), bAddNewItems, sDBID,sUserID,sUserFullName)){
								bResult = false;
							}
						}
						iFieldCounter++;
					}
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						this.addToErrorMessage("<BR>Line number " + iLineCounter + " has less than " 
							+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
						bResult = false;
					}
				}
			}
			if (iLineCounter == 0){
				this.addToErrorMessage("<BR>The file has no lines in it.");
				bResult = false;
			}
		} catch (FileNotFoundException ex) {
			this.addToErrorMessage("<BR>" + "File not found error reading file:= " + ex.getMessage() + ".");
			bResult = false;
		} catch (IOException ex) {
			this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".");
			bResult = false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".");
				bResult = false;
			}
		}
		return bResult;
	}

	private boolean validateImportField (int iLineNumber, int iFieldIndex, String sField, boolean bAddNewItems, String sDBID, String sUserID, String sUserFullName){
		
		boolean bResult = true;
		
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
					this.addToErrorMessage("<BR>Invalid qty ('" + sField + "') on line " + iLineNumber + ".");
					bResult = false;
				}
			} catch (Exception e) {
				this.addToErrorMessage("<BR>Invalid qty ('" + sField + "') on line " + iLineNumber + ".");
	        	bResult = false;
			}
		}
		if (iFieldIndex == FIELD_ITEM){
			//Make sure it's a valid item in the range from the physical inventory:
			sField = stripSuffix(sField);
			//System.out.println(sFieldBefore + " - " + sField);
			if (!validateItemNumber(iLineNumber, sField, bAddNewItems, sDBID, sUserID, sUserFullName)){
				bResult = false;
			}
		}
		return bResult;
	}
	private boolean validateItemNumber(int iLineNumber, String sItem, boolean bAddNewItems, String sDBID, String sUserID, String sUserFullName){
		
		boolean bResult = true;
		
		//Make sure it's in the range for the physical inventory:
		String SQL = "";
		if (!bAddNewItems){
			SQL = "SELECT"
				+ " " + SMTableicinventoryworksheet.sitemnumber + " FROM " + SMTableicinventoryworksheet.TableName
				+ " WHERE (" 
					+ " " + SMTableicinventoryworksheet.sitemnumber + " = '" + sItem + "'"
					+ " AND" 
					+ " " + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + m_sPhysicalInventoryID
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
					this.addToErrorMessage("<BR>Invalid item number ('" + sItem + "') on line number " + iLineNumber + ""
							+ " - item is not included in this physical inventory. [Error: 1344008030]");
					bResult = false;
				}
				rs.close();
			} catch (SQLException e) {
				this.addToErrorMessage("<BR>SQL Error validating item number ('" + sItem + "') on line number " 
				+ iLineNumber + " with SQL - " + SQL + " - " + e.getMessage() + ".");
				bResult = false;
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
				this.addToErrorMessage("<BR>Invalid item number ('" + sItem + "') on line number " + iLineNumber + ". [Error: 1344008064]");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			this.addToErrorMessage("<BR>SQL Error validating item number ('" + sItem + "') on line number " 
			+ iLineNumber + " - " + e.getMessage() + ".");
			bResult = false;
		}
		
		return bResult;
	}

	private void addToErrorMessage(String sMsg){
		
		if (sPhysicalCountImportError.length() > 900){
			sPhysicalCountImportError = sPhysicalCountImportError.substring(0, 900) + " . . . (remaining errors truncated).";
		}else{
			sPhysicalCountImportError += sMsg;
		}
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "ICCOUNTIMPORT", sMsg);
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
