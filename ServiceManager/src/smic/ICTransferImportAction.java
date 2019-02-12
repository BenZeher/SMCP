package smic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
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

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICTransferImportAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static int NUMBER_OF_FIELDS_PER_LINE = 4;
	private static int FIELD_QTY = 0;
	private static int FIELD_ITEM = 1;
	private static int FIELD_FROM_LOCATION = 2;
	private static int FIELD_TO_LOCATION = 3;
	

	private static String sCallingClass = "";
	private static String m_sBatchNumber = "m_sBatchNumber";
	private static String m_sEntryNumber = "m_sEntryNumber";
	private static String m_sBatchType = "m_sBatchType";
	
	private static boolean bDebugMode = true;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		//Create  hash map object of member variables so that the values will be updated when passing to other functions in this class
		HashMap<String,String> mv = new HashMap<String,String>();
		mv.put(m_sBatchNumber, "");
		mv.put(m_sEntryNumber, "");
		mv.put(m_sBatchType, "");
		
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " 01");
		}
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sStatus = "";
	    
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
	    
	    try {
			processRequest(CurrentSession, request, out, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e) {
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + clsServletUtilities.URLEncode(e.getMessage())
	  	    		+ "&" + ICEntry.ParamBatchNumber + "=" + mv.get(m_sBatchNumber)
	   	    		+ "&" + ICEntry.ParamEntryNumber + "=" + mv.get(m_sEntryNumber)
	   	    		+ "&" + ICEntry.ParamBatchType + "=" + mv.get(m_sBatchType)
	   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}		
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Warning=" + clsServletUtilities.URLEncode(e.getMessage())
  	    		+ "&" + ICEntry.ParamBatchNumber + "=" + mv.get(m_sBatchNumber)
   	    		+ "&" + ICEntry.ParamEntryNumber + "=" + mv.get(m_sEntryNumber)
   	    		+ "&" + ICEntry.ParamBatchType + "=" + mv.get(m_sBatchType)
   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}

    	sStatus = "Import completed without errors.";
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".doPost - processRequest succeeded: "
				+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Status=" + sStatus
  	    		+ "&" + ICEntry.ParamBatchNumber + "=" + mv.get(m_sBatchNumber)
   	    		+ "&" + ICEntry.ParamEntryNumber + "=" + mv.get(m_sEntryNumber)
   	    		+ "&" + ICEntry.ParamBatchType + "=" + mv.get(m_sBatchType)
   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID				
			);
		}
    	response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
			+ "?Status=" + sStatus
    		+ "&" + ICEntry.ParamBatchNumber + "=" + mv.get(m_sBatchNumber)
    		+ "&" + ICEntry.ParamEntryNumber + "=" + mv.get(m_sEntryNumber)
    		+ "&" + ICEntry.ParamBatchType + "=" + mv.get(m_sBatchType)
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID	
		);
		
		return;
	}
	private void createTempImportFileFolder(String sTempFileFolder) throws Exception{
	    File dir = new File(sTempFileFolder);
	    if (dir.exists()) {
	      return;
	    }
	    
	    //Need to create the path:
	    try{
	        // Create one directory
	        if (!new File(sTempFileFolder).mkdir()) {
	        	throw new Exception("Error [1396369064] creating temp upload folder.");
	        }    
        }catch (Exception e){//Catch exception if any
        	throw new Exception("Error [1396369065] creating temp upload folder - " + e.getMessage() + ".");
	    }
	}
	private void processRequest(
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName,
			HashMap<String, String> mv) throws Exception{

    	String sTempFilePath = SMUtilities.getAbsoluteRootPath(req, getServletContext())
			+ System.getProperty("file.separator")
			+ "iccountuploads"
		;

    	//If the folder has not been created, create it now:
		try {
			createTempImportFileFolder(sTempFilePath);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//First, remove any temporary files:
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e1) {
			throw new Exception("Error deleting temp files before import - " + e1.getMessage());
		}

		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processRequest - going into writeFileAndProcess");
		}
		
		try {
			writeFileAndProcess(sTempFilePath, ses, req, pwOut, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e2) {
			throw new Exception (e2.getMessage());
		}
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e3) {
			throw new Exception("Error deleting temp files after import - " + e3.getMessage());
		}
		
	}
	@SuppressWarnings("unchecked")
	private void writeFileAndProcess(
			String sTempImportFilePath,
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID, 
			String sUserFullName,
			HashMap<String, String> mv
	) throws Exception{
		//Check to see if the file has a header row:
		boolean bIncludesHeaderRow = false;
		ICOption options = new ICOption();
		
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
			if (bDebugMode){
				System.out.println("In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			}
			throw new Exception("Error on upload.parseRequest: " + e1.getMessage());
		}
		Iterator<FileItem> iter = fileItems.iterator();
		String fileName = "ICTRNIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		    if (item.isFormField()) {
		    	if (item.getFieldName().compareToIgnoreCase("CallingClass") == 0){
		    		sCallingClass = item.getString();
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter CallingClass = " + sCallingClass + "."); 
					}		
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(ICEntry.ParamBatchNumber) == 0){
		    		mv.put(m_sBatchNumber, item.getString());		    		
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ ICEntry.ParamBatchNumber + " = " + mv.get(m_sBatchNumber) + "."); 
					}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(ICEntry.ParamBatchType) == 0){
		    		mv.put(m_sBatchType,item.getString());
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ ICEntry.ParamBatchType + " = " + mv.get(m_sBatchType) + "."); 
					}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase(
		    			ICEntry.ParamEntryNumber) == 0){
		    		mv.put(m_sEntryNumber,item.getString());
						if (bDebugMode){
							System.out.println(
								"In " + this.toString() 
								+ ".writeFileAndProcess, parameter "
								+ ICEntry.ParamEntryNumber + " = " + mv.get(m_sEntryNumber) + "."); 
						}
		    	}
		    	if (item.getFieldName().compareToIgnoreCase("INCLUDESHEADERROW") == 0){
		    			bIncludesHeaderRow = true;
		    	}
		    	
		    } else {
		    	//It's a file - 
		    	FileItem fi = item;
		    	//String fileName = fi.getName();
		    	
		        // write the file
		        try {
					fi.write(new File(sTempImportFilePath, fileName));
				} catch (Exception e) {
					throw new Exception("Error [1396369876] writing temporary file: " + e.getMessage());
				}
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
		try {
			validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow);
		} catch (Exception e1) {
			throw new Exception("Error [1396369877] validating file: " + e1.getMessage());
		}
		
		//Get a connection:
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".writeFileAndProcess - user: " + sUserFullName)
			);
		} catch (Exception e1) {
			throw new Exception("Error [1396369878] getting a connection - " + e1.getMessage());
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess got a connection");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080994]");
			throw new Exception("Error [1396369878] Could not start data transaction.");
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		try {
			insertTransferLines(sTempImportFilePath, fileName, conn, bIncludesHeaderRow, options, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080995]");
			throw new Exception("Error [1396370667] inserting transfers - " + e.getMessage());
			
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080996]");
			throw new Exception("Error [1396370668] committing data transaction.");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080997]");
		return;

	}

	private void insertTransferLines(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			ICOption options,
			String sDBID,
			String sUserID,
			String sUserFullName,
			HashMap<String, String> mv
	) throws Exception{
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 1;
			String sQty = "";
			String sItem = "";
			String sFromLocation = "";
			String sToLocation = "";
			while ((line = br.readLine()) != null) {
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
						}
						if (iFieldCounter == FIELD_FROM_LOCATION){
							sFromLocation = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_TO_LOCATION){
							sToLocation = sDelimitedField.trim().replace("\"", "");
						}
						iFieldCounter++;
					}
					
					ICEntryLine entryline = new ICEntryLine();
					entryline.sBatchNumber(mv.get(m_sBatchNumber));
					entryline.setQtyString(sQty);
					entryline.sItemNumber(sItem);
					entryline.sLocation(sFromLocation);
					entryline.sTargetLocation(sToLocation);
					entryline.sEntryNumber(mv.get(m_sEntryNumber));
					entryline.sComment("Imported transfer");
					ICItem item = new ICItem(sItem);
					if(!item.load(conn)){
						throw new Exception("Error [1396382571] loading item information for item '" + sItem + "' - " + item.getErrorMessageString());
					}
					entryline.sDescription(item.getItemDescription());
					
				    //First, make sure there is no posting going on:
			    	try {
			    		options.checkAndUpdatePostingFlagWithoutConnection(getServletContext(), sDBID, sCallingClass, sUserFullName, "TRANSFER IMPORT ACTION");
					} catch (Exception e1) {
						throw new Exception("Error [1396371621] - " + e1.getMessage());
					}
			    	int iImportFileLine = iLineCounter;
			    	if (bFileIncludesHeaderRow){
			    		iImportFileLine = iImportFileLine - 1;
			    	}
			    	try {
						saveTransferLine(entryline, conn, iImportFileLine, sUserID);
					} catch (Exception e) {
						//Reset Posting Flag
						try{
							options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
						}catch(Exception e1){
						}
						throw new Exception(e.getMessage());
					}
			    	//Reset Posting Flag 
			    	try{
						options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
					}catch(Exception e){
					}
				}
				iLineCounter++;
			}
		} catch (FileNotFoundException ex) {
			throw new Exception("Error [1396371622] File not found error reading file:= " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("Error [1396371623] IO exception error reading file:= " + ex.getMessage() + ".");
		} catch (Exception ex) {
			throw new Exception("Error [1396371723] Exception - " + ex.getMessage() + ".");
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("Error [1396371624] IO exception error reading file:= " + ex.getMessage() + ".");
			}
		}
		return;
	}
	private void saveTransferLine(ICEntryLine m_Line, Connection conn, int iImportFileLineNumber, String sUserID) throws Exception{
		ICEntry entry = new ICEntry();
		String sWarning = "";
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1396371936] loading entry lines - " + sWarning);
		}
		
		//Validate the line first in case we can't save it at all:
		//IMPORTANT: Temporarily assign it a line number, just so the validateSingleLine function can return a meaningful error:
		m_Line.sLineNumber(Integer.toString(iImportFileLineNumber));
		if (!entry.validateSingleLine(m_Line, conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1396371937] validating entry lines - " + sWarning);
		}
		//Now set the line number back to -1:
		m_Line.sLineNumber("-1");
		//Validate it now so we know that it's going to be in the entry for sure.  We need to 
		//know this so we can get the line ID and line number from the last line after it's
		//saved, and that's only correct if the line is going to be saved.
		if (!entry.add_line(m_Line)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1396371938] adding entry line - " + sWarning);
		}
		//Update the line number here:
		m_Line.sLineNumber(entry.sLastLine());
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1396371939] saving entry - " + sWarning);
		}
	}

	private void deleteCurrentTempImportFiles(String sTempImportFilePath) throws Exception{
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	throw new Exception("Error [1396369366] - directory " + sTempImportFilePath + " already exists.");
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  throw new Exception("Error [1396369367] - error deleting " 
	    		+ sTempImportFilePath + System.getProperty("file.separator") + info[i] + ".");
	      }
	    }

	}
	private void validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow) throws Exception{

		BufferedReader br = null;
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
							try {
								validateImportField(iLineCounter, iFieldCounter, sDelimitedField.trim().replace("\"", ""));
							} catch (Exception e) {
								throw new Exception(e.getMessage());
							}
						}
						iFieldCounter++;
					}
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						throw new Exception("Error [1396383122] - Line number " + iLineCounter + " has less than " 
							+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
					}
				}
			}
			if (iLineCounter == 0){
				throw new Exception("Error [1396383118] - The file has no lines in it.");
			}
		} catch (FileNotFoundException ex) {
			throw new Exception("Error [1396383119] - File not found error reading file:= " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("Error [1396383120] - IO exception error reading file:= " + ex.getMessage() + ".");
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("Error [1396383121] - IO exception error reading file:= " + ex.getMessage() + ".");
			}
		}
	}

	private void validateImportField (int iLineNumber, int iFieldIndex, String sField) throws Exception{
		
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
					throw new Exception("Error [1396370002] qty ('" + sField + "') on line " + iLineNumber + " cannot be less than zero.");
				}
			} catch (Exception e) {
				throw new Exception("Error [1396370003] Invalid qty ('" + sField + "') on line " + iLineNumber + ".");
			}
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
