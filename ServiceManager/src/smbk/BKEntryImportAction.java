package smbk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablebkaccountentries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class BKEntryImportAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_FIELDS_PER_LINE = 3;
	private static final int FIELD_CHECK_NUMBER = 0;
	private static final int FIELD_ISSUE_DATE = 1;
	private static final int FIELD_AMOUNT = 2;
	
	
	//Member variables for local hash map key:
	private static final String sBKEntryImportActionCallingClass = "sBKEntryImportActionCallingClass";
	private static final String sBKEntryImportActionError = "sBKEntryImportActionError";
	private static final String m_sBankID = "m_sBankID";
	private static final String m_sEntryDescription = "m_sEntryDescription";
	private static final String m_sGLAccount = "m_sGLAccount";

	private static boolean bDebugMode = false;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (bDebugMode){
			System.out.println("In " + this.toString() + " 01");
		}
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.BKImportBankEntries))
			{
				return;
			}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		        + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sStatus = "";
	    String sError = "";
	    
		//Create hash map object of member variables. 
		//A hash map must be passed to functions of this class instead of updating global variables for thread safety. 
		HashMap<String,String> mv = new HashMap<String,String>();
		mv.put(sBKEntryImportActionCallingClass, "");
		mv.put(sBKEntryImportActionError, "");
		mv.put(m_sBankID, "");
		mv.put(m_sEntryDescription, "");
		mv.put(m_sGLAccount, "");
	    
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
	    
	    if (!processRequest(CurrentSession, request, out, sDBID, sUserID, sUserFullName, mv)){
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(sBKEntryImportActionCallingClass)
					+ "?Warning=" + sError
					+ "&" + BKBankStatement.Paramlbankid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request)
					+ "&" + SMTablebkaccountentries.sdescription + "=" + clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request)
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}		
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(sBKEntryImportActionCallingClass)
					+ "?Warning=" + sError
					+ "&" + BKBankStatement.Paramlbankid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request)
					+ "&" + SMTablebkaccountentries.sdescription + "=" + clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request)
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }else{
	    	sStatus = "Import completed without errors.";
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest succeeded: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(sBKEntryImportActionCallingClass)
					+ "?Status=" + sStatus
					+ "&" + BKBankStatement.Paramlbankid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request)
					+ "&" + SMTablebkaccountentries.sdescription + "=" + clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request)
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID				
				);
			}
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(sBKEntryImportActionCallingClass)
					+ "?Status=" + sStatus
					+ "&" + BKBankStatement.Paramlbankid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request)
					+ "&" + SMTablebkaccountentries.sdescription + "=" + clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request)
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
		
		return;
	}
	private boolean createTempImportFileFolder(String sTempFileFolder, HashMap<String,String> mv){
	    File dir = new File(sTempFileFolder);
	    if (dir.exists()) {
	      return true;
	    }
	    
	    //Need to create the path:
	    try{
	        // Create one directory
	        if (!new File(sTempFileFolder).mkdir()) {
	        	this.addToErrorMessage("<BR>Error creating temp upload folder.", mv);
	        	return false;
	        }    
        }catch (Exception e){//Catch exception if any
        	this.addToErrorMessage("<BR>Error creating temp upload folder: " + e.getMessage() + ".", mv);
	    }
	    return true;
	}
	private boolean processRequest(
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID, 
			String sUserID, 
			String sUserFullName,
			HashMap<String,String> mv
			){

    	String sTempFilePath = SMUtilities.getAbsoluteRootPath(req, getServletContext())
			+ System.getProperty("file.separator")
			+ "iccountuploads"
		;

    	//If the folder has not been created, create it now:
		if (!createTempImportFileFolder(sTempFilePath, mv)){
			return false;
		}
    	
		//First, remove any temporary files:
		if (!deleteCurrentTempImportFiles(sTempFilePath, mv)){
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
				sUserFullName, mv
				);
		deleteCurrentTempImportFiles(sTempFilePath, mv);
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
			String sUserFullName,
			HashMap<String,String> mv
	){
		//Check to see if the file has a header row:
		boolean bIncludesHeaderRow = false;
		
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
		List<FileItem> fileEntries = null;
		try {
			fileEntries = upload.parseRequest(req);
		} catch (FileUploadException e1) {
			this.addToErrorMessage("<BR>"
				+ "Error on upload.parseRequest: " + e1.getMessage(), mv);
			if (bDebugMode){
				System.out.println("In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			}
			e1.printStackTrace();
			return false;
		}
		Iterator<FileItem> iter = fileEntries.iterator();
		String fileName = "BKENTRYIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		while (iter.hasNext()) {
		    FileItem fileitem = (FileItem) iter.next();
		    if (fileitem.isFormField()) {
		    	if (fileitem.getFieldName().compareToIgnoreCase("CallingClass") == 0){
		    		mv.put(sBKEntryImportActionCallingClass, fileitem.getString());
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter CallingClass = " + mv.get(sBKEntryImportActionCallingClass) + "."); 
					}		
		    	}
		    	if (fileitem.getFieldName().compareToIgnoreCase(SMTablebkaccountentries.sdescription) == 0){
		    		mv.put(m_sEntryDescription, fileitem.getString());		    		
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ SMTablebkaccountentries.sdescription + " = " + mv.get(m_sEntryDescription) + "."); 
					}
		    	}
		    	if (fileitem.getFieldName().compareToIgnoreCase(BKBankStatement.Paramlbankid) == 0){
		    		mv.put(m_sBankID, fileitem.getString());
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter "
							+ BKBankStatement.Paramlbankid + " = " + mv.get(m_sBankID) + "."); 
					}
		    	}

		    	if (fileitem.getFieldName().compareToIgnoreCase(
			    	BKEntryImportSelect.INCLUDES_BKENTRY_HEADER_ROW) == 0){
		    			bIncludesHeaderRow = true;
							if (bDebugMode){
								System.out.println(
									"In " + this.toString() 
									+ ".writeFileAndProcess, parameter "
									+ BKEntryImportSelect.INCLUDES_BKENTRY_HEADER_ROW + " = " + bIncludesHeaderRow + "."); 
							}
		    	}
		    	
		    } else {
		    	//It's a file - 
		    	FileItem fi = fileitem;
		    	//String fileName = fi.getName();
		    	
		        // write the file
		        try {
					fi.write(new File(sTempImportFilePath, fileName));
				} catch (Exception e) {
					this.addToErrorMessage("<BR>"
						+ "Error writing temporary file: " + e.getMessage(), mv);
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
		if (!validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow, mv)){
			return false;
		}
		
		//Get a connection:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
						+ ".writeFileAndProcess - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						)
		);
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess got a connection");
		}
		boolean bResult = true;		
		//Get the GLAccount for this bank:
		try {
			mv.put(m_sGLAccount, getBankGL(mv.get(m_sBankID), conn));
		} catch (Exception e) {
			this.addToErrorMessage(e.getMessage(), mv);
			bResult =  false;
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			this.addToErrorMessage("Could not start data transaction.", mv);
			bResult =  false;
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		if (!insertEntries(sTempImportFilePath, fileName, conn, bIncludesHeaderRow, mv)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			bResult = false;
		}else{
			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				this.addToErrorMessage("Could not commit data transaction.", mv);
				bResult = false;
			}
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067450]");
		return bResult;

	}
 private String getBankGL(String sBankID, Connection conn) throws Exception{
	 
	 BKBank bank = new BKBank();
	 bank.setslid(sBankID);
	 try {
		bank.load(conn);
	} catch (Exception e) {
		throw new Exception("Error loading bank information - " + e.getMessage() + ".");
	}
	 return bank.getsglaccount();
 }
	private boolean insertEntries(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			HashMap<String,String> mv
	) {
		
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
			String sAmt = "";
			String sDocNumber = "";
			String sIssueDate = "";
			while ((line = br.readLine()) != null) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".insertEntries - at line " + iLineCounter);
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
					line = clsStringFunctions.removeDoubleQuotesInDelimitedLine(line);
					String[] fields = line.split(",");
					BigDecimal bdAmount = new BigDecimal("0.00");
					for (String sDelimitedField : fields) {
						if (iFieldCounter == FIELD_AMOUNT){
							sAmt = sDelimitedField.trim().replace("\"", "").replace(",", "");
							bdAmount = new BigDecimal(sAmt);
						}
						if (iFieldCounter == FIELD_CHECK_NUMBER){
							sDocNumber = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ISSUE_DATE){
							sIssueDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sDelimitedField.trim().replace("\"", ""));
						}
						iFieldCounter++;
					}
					//Don't import anything with the same doc number:
					boolean bEntryAlreadyExists = false;
					try {
						bEntryAlreadyExists = (entryAlreadyExists(sDocNumber, conn));
					} catch (Exception e1) {
						this.addToErrorMessage("<BR>" + e1.getMessage(), mv);
						return false;
					}
					if (!bEntryAlreadyExists){
						String sEntryType = Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL);
						if (bdAmount.compareTo(BigDecimal.ZERO) < 0){
							sEntryType = Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT);
						}
						String SQL = "INSERT INTO " + SMTablebkaccountentries.TableName + "("
							+ SMTablebkaccountentries.bdamount
							+ ", " + SMTablebkaccountentries.datentrydate
							+ ", " + SMTablebkaccountentries.ibatchentrynumber
							+ ", " + SMTablebkaccountentries.ibatchnumber
							+ ", " + SMTablebkaccountentries.ibatchtype
							+ ", " + SMTablebkaccountentries.icleared
							+ ", " + SMTablebkaccountentries.ientrytype
							+ ", " + SMTablebkaccountentries.lstatementid
							+ ", " + SMTablebkaccountentries.sdescription
							+ ", " + SMTablebkaccountentries.sdocnumber
							+ ", " + SMTablebkaccountentries.sglaccount
							+ ", " + SMTablebkaccountentries.ssourcemodule
							+ ") VALUES ("
							+ " -1 * " + sAmt //Checks are always positive, but they need to be negatives to be withdrawals
							+ ", '" + sIssueDate + "'"
							+ ", -1"
							+ ", -1"
							+ ", -1"
							+ ", 0"
							+ ", " + sEntryType
							+ ", " + SMTablebkaccountentries.INITIAL_STATEMENT_ID_VALUE
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(mv.get(m_sEntryDescription)) + "'"
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDocNumber) + "'"
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(mv.get(m_sGLAccount)) + "'"
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(SMTablebkaccountentries.SOURCE_MODULE_IMPORTED_ENTRY) + "'"
							+ ")"
						;
						try {
							Statement stmt = conn.createStatement();
							stmt.execute(SQL);
						} catch (Exception e) {
							this.addToErrorMessage("<BR>" + "Error inserting entry with SQL: " + SQL + " - " + e.getMessage(), mv);
							return false;
						}
					}
				}
			}
		} catch (FileNotFoundException ex) {
			this.addToErrorMessage("<BR>" + "File not found error reading file:= " + ex.getMessage() + ".", mv);
			return false;
		} catch (IOException ex) {
			this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".", mv);
			return false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".", mv);
				return false;
			}
		}
		return true;
	}
	private boolean entryAlreadyExists(String sDocNumber, Connection conn) throws Exception{
		boolean bAlreadyExists = false;
		String SQL = "SELECT"
			+ " " + SMTablebkaccountentries.sdocnumber
			+ " FROM " + SMTablebkaccountentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkaccountentries.sdocnumber + "='" + sDocNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				bAlreadyExists = true;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error checking for doc number - " + e.getMessage());
		}
		
		return bAlreadyExists;
	}
	private boolean deleteCurrentTempImportFiles(String sTempImportFilePath, HashMap<String,String> mv){
		
		boolean bDeletionSuccessful = true;
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	this.addToErrorMessage("<BR>Temp import file directory does not exist: " + sTempImportFilePath, mv);
	      return false;
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  this.addToErrorMessage("Unable to delete " + sTempImportFilePath + info[i], mv);
	    	  bDeletionSuccessful = false;
	      } 
	    }
	    return bDeletionSuccessful;

	}

	private boolean validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow, HashMap<String,String> mv) {

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
					//First, remove any delimiting quotes and embedded commas:
					line = filterQuotesAndCommas(line);
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						if (iFieldCounter > NUMBER_OF_FIELDS_PER_LINE){
							//Allow additional fields, just ignore them:
							//this.addToErrorMessage("<BR>Line number " + iLineCounter + " has more than " 
							//	+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
							//bResult = false;
						}else{
							if (!validateImportField(
									iLineCounter, iFieldCounter, sDelimitedField.trim().replace("\"", ""), mv)){
								bResult = false;
							}
						}
						iFieldCounter++;
					}
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						this.addToErrorMessage("<BR>Line number " + iLineCounter + " has less than " 
							+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.", mv);
						bResult = false;
					}
				}
			}
			if (iLineCounter == 0){
				this.addToErrorMessage("<BR>The file has no lines in it.", mv);
				bResult = false;
			}
		} catch (FileNotFoundException ex) {
			this.addToErrorMessage("<BR>" + "File not found error reading file:= " + ex.getMessage() + ".", mv);
			bResult = false;
		} catch (IOException ex) {
			this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".", mv);
			bResult = false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".", mv);
				bResult = false;
			}
		}
		return bResult;
	}
	private String filterQuotesAndCommas(String sLine){
		String s = sLine;
		boolean bInQuoteDelimitedField = false;
		for (int i = 1; i <= sLine.length(); i++){
			String sTestChar = sLine.substring(i, i);
			if (sTestChar.compareToIgnoreCase("\"") == 0){
				bInQuoteDelimitedField = !bInQuoteDelimitedField;
				//Drop the double quote characters:
				continue;
			}
			if ((bInQuoteDelimitedField) && (sTestChar.compareToIgnoreCase(",") == 0)){
				//Drop the comma in this case:
				continue;
			}
			//If we got here, then was can add the character to the string:
			s += sTestChar;
			//System.out.println("[1404247911] filtered line: '" + s + "'.");
		}
		return s;
	}
	private boolean validateImportField (int iLineNumber, int iFieldIndex, String sField, HashMap<String,String> mv){
		
		boolean bResult = true;
		
		//Strip off any quotation marks:
		sField = sField.replace("\"", "");
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + "FieldIndex = " + iFieldIndex 
				+ ", value = " + sField);
		}
		if (iFieldIndex == FIELD_ISSUE_DATE){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sField)){
				this.addToErrorMessage("Invalid issue date on line " + iLineNumber + ": '" + sField + "'.  ", mv);
				return false;
			}
		}
		if (iFieldIndex == FIELD_CHECK_NUMBER){
			if (sField.compareToIgnoreCase("") == 0){
				this.addToErrorMessage("Check number on line " + iLineNumber + " is blank.", mv);
				return false;
			}
			if (sField.length() > SMTablebkaccountentries.sdocnumberlength){
				this.addToErrorMessage("Check number on line " + iLineNumber + " is too long - it can only be " 
					+ SMTablebkaccountentries.sdocnumberlength + " characters long.", mv);
				return false;
			}
		}
		if (iFieldIndex == FIELD_AMOUNT){
			try {
				@SuppressWarnings("unused")
				BigDecimal bdAmount = new BigDecimal(sField.replace(",", ""));
			} catch (Exception e) {
				this.addToErrorMessage("Amount '" + sField + "' on line " + iLineNumber + " is invalid.", mv);
				return false;
			}
		}
		return bResult;
	}

	private void addToErrorMessage(String sMsg, HashMap<String,String> mv){
		
		if (mv.get(sBKEntryImportActionError).length() > 900){
			mv.put(sBKEntryImportActionError, mv.get(sBKEntryImportActionError).substring(0, 900) + " . . . (remaining errors truncated).");
		}else{
			mv.put(sBKEntryImportActionError, mv.get(sBKEntryImportActionError) + sMsg);
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
