package smgl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
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

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class GLImportBatchesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_FIELDS_PER_LINE = 19;
	private static final int FIELD_BATCH_DATE = 0;
	private static final int FIELD_BATCH_DESC = 1;
	private static final int FIELD_ENTRY_NUMBER = 2;
	private static final int FIELD_ENTRY_DESC = 3;
	private static final int FIELD_ENTRY_DATE = 4;
	private static final int FIELD_ENTRY_DOCUMENTDATE = 5;
	private static final int FIELD_ENTRY_FISCALYEAR = 6;
	private static final int FIELD_ENTRY_FISCALPERIOD = 7;
	private static final int FIELD_ENTRY_SOURCELEDGER = 8;
	private static final int FIELD_ENTRY_AUTOREVERSE = 9;
	private static final int FIELD_LINE_NUMBER = 10;
	private static final int FIELD_LINE_DESC = 11;
	private static final int FIELD_LINE_REFERENCE = 12;
	private static final int FIELD_LINE_COMMENT = 13;
	private static final int FIELD_LINE_TRANSACTIONDATE = 14;
	private static final int FIELD_LINE_GLACCT = 15;
	private static final int FIELD_LINE_DEBITAMT = 16;
	private static final int FIELD_LINE_CREDITAMT = 17;
	private static final int FIELD_LINE_SOURCETYPE = 18;
	
	//Fields for older 'ACCPAC STYLE' import files:
	private static final int ACCPAC_FORMAT_FIELD_RECORD_TYPE = 0;

	//Keys used for local hash mapped member variables.
	private static final String CALLING_CLASS = "sCallingClass";
	
	private static boolean bDebugMode = false;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		//Create hash map object of member variables. 
		//A hash map must be past to functions of this class instead of updating global variables for thread safety. 
		HashMap<String,String> mv = new HashMap<String,String>();
		mv.put(CALLING_CLASS, "");
		
		if (bDebugMode){
			System.out.println("[1557852625] In " + this.toString() + " 01");
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
	    CurrentSession.removeAttribute(GLImportBatchesSelect.SESSION_ERROR_OBJECT);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sStatus = "";
	    
	    if (bDebugMode){
	    	System.out.println("[1557852626] In " + this.toString() + ".doPost - contenttype: " 
	    		+ request.getContentType()
	    		+ " - getRequestURI: "
	    		+ request.getRequestURI()
	    	);

			Enumeration<?> headerNames = request.getHeaderNames();
	    	while (headerNames.hasMoreElements()){
	    		String sHeaderName = headerNames.nextElement().toString();
	    		if (bDebugMode){
	    			System.out.println("[1557852627] headerName = " + sHeaderName);
	    			System.out.println("[1557852628] headerNameValue = " + request.getHeader(sHeaderName));
	    		}
	    	}
	    }
	    
	    try {
			processRequest(CurrentSession, request, out, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e) {
			if (bDebugMode){
				System.out.println("[1557852629] In " + this.toString() + ".doPost - processRequest failed: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(CALLING_CLASS)
					+ "?Warning=" + clsServletUtilities.URLEncode(e.getMessage())
	   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}
			CurrentSession.setAttribute(GLImportBatchesSelect.SESSION_ERROR_OBJECT, e.getMessage());
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(CALLING_CLASS)
   	    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}

    	sStatus = "Import completed without errors.";
		if (bDebugMode){
			System.out.println("[1557852630] In " + this.toString() + ".doPost - processRequest succeeded: "
				+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(CALLING_CLASS)
				+ "?Status=" + sStatus
   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID				
			);
		}
    	response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + mv.get(CALLING_CLASS)
			+ "?Status=" + sStatus
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
	        	throw new Exception("Error [1557852631] creating temp upload folder.");
	        }    
        }catch (Exception e){//Catch exception if any
        	throw new Exception("Error [1557852632] creating temp upload folder - " + e.getMessage() + ".");
	    }
	}
	private void processRequest(
			HttpSession ses, 
			HttpServletRequest req,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFullName,
			HashMap<String, String> mv
			) throws Exception{

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
			throw new Exception("Error [1557852633] deleting temp files before import - " + e1.getMessage());
		}

		if (bDebugMode){
			System.out.println("[1557852634] In " + this.toString() + ".processRequest - going into writeFileAndProcess");
		}
		
		try {
			writeFileAndProcess(sTempFilePath, ses, req, pwOut, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e2) {
			throw new Exception (e2.getMessage());
		}
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e3) {
			throw new Exception("Error [1557852635] deleting temp files after import - " + e3.getMessage());
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
		GLOptions glopts = new GLOptions();
		
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
        	System.out.println("[1557853451] In " + this.toString() + ".writeFileAndProcess - isMultipart = " + isMultipart);
        }
		List<FileItem> fileItems = null;
		try {
			fileItems = upload.parseRequest(req);
		} catch (FileUploadException e1) {
			if (bDebugMode){
				System.out.println("In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			}
			throw new Exception("Error [1557852636] on upload.parseRequest: " + e1.getMessage());
		}
		Iterator<FileItem> iter = fileItems.iterator();
		String fileName = "GLIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		    if (item.isFormField()) {
		    	if (item.getFieldName().compareToIgnoreCase("CallingClass") == 0){
		    		mv.put(CALLING_CLASS, item.getString());
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() 
							+ ".writeFileAndProcess, parameter CallingClass = " + mv.get(CALLING_CLASS) + "."); 
					}		
		    	}

		    	if (item.getFieldName().compareToIgnoreCase(GLImportBatchesSelect.PARAM_INCLUDES_HEADER_ROW) == 0){
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
					throw new Exception("Error [1557852637] writing temporary file: " + e.getMessage());
				}
		    }
		}
		
		if (bDebugMode){
			clsServletUtilities.sysprint(
				this.toString(), 
				sUserFullName, 
				"in writeFileAndProcess - bIncludeHeaderRow = " + bIncludesHeaderRow);
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
			throw new Exception("Error [1557852639] getting a connection - " + e1.getMessage());
		}
		
		if (bDebugMode){
			System.out.println("[1557853981] In " + this.toString() + ".writeFileAndProcess going into validateFile");
		}
		
		//Check the file - if it's the older 'ACCPAC' format, rewrite it in the expected format:
		try {
			if(reformatACCPACStyleFile(sTempImportFilePath, fileName)){
				//If it WAS an 'ACCCPAC' style import, then the resulting import file will NOT
				//have a header row:
				bIncludesHeaderRow = false;
			};
		} catch (Exception e2) {
			throw new Exception("Error [1558461509] checking for ACCPAC style format: " + e2.getMessage());
		}
		
		try {
			validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow, conn);
		} catch (Exception e1) {
			throw new Exception("Error [1557852638] validating file: " + e1.getMessage());
		}
		
		if (bDebugMode){
			System.out.println("[1558382743] In " + this.toString() + ".writeFileAndProcess got a connection");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1557852641]");
			throw new Exception("Error [1557852640] Could not start data transaction.");
		}
		if (bDebugMode){
			System.out.println("[1558382744] In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		try {
			createGLBatch(sTempImportFilePath, fileName, conn, bIncludesHeaderRow, glopts, sDBID, sUserID, sUserFullName, mv);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1557852642]");
			throw new Exception("Error [1557852644] inserting GL entries - " + e.getMessage());
			
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1557852643]");
			throw new Exception("Error [1557852645] committing data transaction.");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1557852646]");
		return;

	}

	//This function returns TRUE if it IS an 'ACCPAC STYLE' file, FALSE if not:
	private boolean reformatACCPACStyleFile(String sFilePath, String sFileName) throws Exception{
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
		
		ArrayList<String>arrLineBuffer = new ArrayList<String>(0);
		String line = null;
		int iLineCounter = 1;
		
		String sBatchDate = "";
		String sBatchDesc = "";
		String sEntryNumber = "";
		String sEntryDesc = "";
		String sEntryDate = "";
		String sEntryDocumentDate = "";
		String sEntryFiscalYear = "";
		String sEntryFiscalPeriod = "";
		String sEntrySourceLedger = "";
		String sEntryAutoReverse = "";
		String sLineNumber = "";
		String sLineDescription = "";
		String sLineReference = "";
		String sLineComment = "";
		String sLineTransactionDate = "";
		String sLineGLAcct = "";
		String sLineDebitAmt = "";
		String sLineCreditAmt = "";
		String sLineSourceType = "";
		
		while ((line = br.readLine()) != null) {
			//If it's NOT the 'ACCPAC' format, then just exit and go on:
			//System.out.println("[1558463344] - line = '" + line + "'");
			if (iLineCounter == 1){
				if (line.substring(0, "RECTYPE".length()).compareToIgnoreCase("RECTYPE") != 0){
					br.close();
					return false;
				}
			}
			
			//The first two lines are just headers:
			if (iLineCounter < 3){
				iLineCounter++;
				continue;
			}
			
			//Now we read the actual lines:
			//System.out.println("[1558640023] - iLineCounter = " + iLineCounter + ", line = '" + line + "'.");
			line = filterQuotesAndCommas(line);
			String[] fields = line.split(",");
			if (fields[ACCPAC_FORMAT_FIELD_RECORD_TYPE].compareToIgnoreCase("1") == 0){
				//It's an 'entry' record:
				//System.out.println("[1558547351] fields[11] = '" + fields[11] + "'.");
				sBatchDate = fields[11].substring(4, 6) 
					+ "/" + fields[11].substring(6, 8) 
					+ "/" + fields[11].substring(0, 4);
				//System.out.println("[1558547352] sBatchDate = '" + sBatchDate + "'.");
				//System.out.println("[1558547353] fields[7] = '" + fields[7] + "'.");
				sBatchDesc = fields[7].trim() + " " + fields[11].trim();
				//System.out.println("[1558547354] sBatchDesc = '" + sBatchDesc + "'.");
				sEntryNumber = fields[2].trim();
				sEntryDesc = fields[7].trim();
				sEntryDate = fields[11].substring(4, 6) 
					+ "/" + fields[11].substring(6, 8) 
					+ "/" + fields[11].substring(0, 4);
				sEntryFiscalYear = fields[5].trim();
				sEntryFiscalPeriod = fields[6].trim();
				sEntrySourceLedger = "JE";
				sEntryAutoReverse = "N";
				
			}else{
				//It's a 'line' record:
				
				//These fields are coming from the DETAIL lines:
				
				sEntryDocumentDate = fields[20].substring(4, 6) 
						+ "/" + fields[20].substring(6, 8) 
						+ "/" + fields[20].substring(0, 4);
				sLineNumber = fields[3].trim();
				sLineDescription = fields[19].trim();
				sLineReference = "";
				sLineComment = "";
				sLineTransactionDate = fields[20].substring(4, 6) 
						+ "/" + fields[20].substring(6, 8) 
						+ "/" + fields[20].substring(0, 4);
				sLineGLAcct = fields[4].trim().replace("-", "");
				String sAmount = fields[6].trim().replace(",", "");
				if (sAmount.startsWith("-")){
					sLineDebitAmt = "0.00";
					sLineCreditAmt = sAmount.replaceAll("-", "");
				}else{
					sLineDebitAmt = sAmount;
					sLineCreditAmt = "0.00";
				}
				sLineSourceType = GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_GL);
				
				String sOutPutLine = 
					sBatchDate
					+ ", \"" + sBatchDesc + "\""
					+ ", \"" + sEntryNumber + "\""
					+ ", \"" + sEntryDesc + "\""
					+ ", \"" + sEntryDate + "\""
					+ ", \"" + sEntryDocumentDate + "\""
					+ ", \"" + sEntryFiscalYear  + "\""
					+ ", \"" + sEntryFiscalPeriod + "\""
					+ ", \"" + sEntrySourceLedger + "\""
					+ ", \"" + sEntryAutoReverse + "\""
					+ ", \"" + sLineNumber + "\""
					+ ", \"" + sLineDescription + "\""
					+ ", \"" + sLineReference + "\""
					+ ", \"" + sLineComment + "\""
					+ ", \"" + sLineTransactionDate + "\""
					+ ", \"" + sLineGLAcct + "\""
					+ ", \"" + sLineDebitAmt + "\""
					+ ", \"" + sLineCreditAmt + "\""
					+ ", \"" + sLineSourceType + "\""
				;
				arrLineBuffer.add(sOutPutLine);
			}
			//for (String sDelimitedField : fields) {
			//	if (iFieldCounter == ACCPAC_FORMAT_FIELD_RECORD_TYPE){
			//		//sRecordType = sDelimitedField.trim().replace("\"", "");
			//	}
			//
			//	iFieldCounter++;
			//}
			iLineCounter++;
		}
		br.close();
		
		//Now write the new file to the import file:
		BufferedWriter bw = null;
		try {
			// OVERWRITE MODE SET HERE
			bw = new BufferedWriter(new FileWriter(sFilePath + System.getProperty("file.separator") + sFileName, false));
			for (int i = 0; i < arrLineBuffer.size(); i++){
				bw.write(arrLineBuffer.get(i));
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			throw new Exception("Error [1463665651] writing to file '" 
				+ sFilePath + System.getProperty("file.separator") + sFileName + "' - " + e.getMessage());
		} finally {                       // always close the file
			if (bw != null) try {
				bw.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
		}
		
		//TEST:
		//throw new Exception("ACCPAC TEST");
		return true;
	}
	
	private void createGLBatch(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			GLOptions options,
			String sDBID,
			String sUserID,
			String sUserFullName,
			HashMap<String, String> mv
	) throws Exception{
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
		String line = null;
		int iLineCounter = 1;
		String sBatchDate = "";
		String sBatchDesc = "";
		String sEntryNumber = "";
		String sEntryDesc = "";
		String sEntryDate = "";
		String sEntryDocumentDate = "";
		String sEntryFiscalYear = "";
		String sEntryFiscalPeriod = "";
		String sEntrySourceLedger = "";
		String sEntryAutoReverse = "";
		String sLineNumber = "";
		String sLineDescription = "";
		String sLineReference = "";
		String sLineComment = "";
		String sLineTransactionDate = "";
		String sLineGLAcct = "";
		String sLineDebitAmt = "";
		String sLineCreditAmt = "";
		String sLineSourceType = "";
		String sLastEntryNumber = "0";
		
		GLTransactionBatch glbatch = new GLTransactionBatch("-1");
		GLTransactionBatchEntry glentry = null;
		GLTransactionBatchLine glline = null;
		
		try {
			while ((line = br.readLine()) != null) {
				//If the file has a header row and if this is the first line, then it's the header line
				//so reset the line counter and don't do any validation of it:
				if (
						bFileIncludesHeaderRow
						&& (iLineCounter == 1)
					)
				{
					iLineCounter++;
				//Otherwise, if it's NOT the first row of a file with a header row, process:
				}else{
					int iFieldCounter = 0;
					line = filterQuotesAndCommas(line);
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						if (iFieldCounter == FIELD_BATCH_DATE){
							sBatchDate = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_BATCH_DESC){
							sBatchDesc = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_NUMBER){
							sEntryNumber = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_DESC){
							sEntryDesc = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_DATE){
							sEntryDate = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_DOCUMENTDATE){
							sEntryDocumentDate = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_FISCALYEAR){
							sEntryFiscalYear = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_FISCALPERIOD){
							sEntryFiscalPeriod = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_SOURCELEDGER){
							sEntrySourceLedger = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_ENTRY_AUTOREVERSE){
							sEntryAutoReverse = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_NUMBER){
							sLineNumber = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_DESC){
							sLineDescription = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_REFERENCE){
							sLineReference = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_COMMENT){
							sLineComment = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_TRANSACTIONDATE){
							sLineTransactionDate = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_GLACCT){
							sLineGLAcct = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_DEBITAMT){
							sLineDebitAmt = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_CREDITAMT){
							sLineCreditAmt = sDelimitedField.trim().replace("\"", "");
						}
						if (iFieldCounter == FIELD_LINE_SOURCETYPE){
							sLineSourceType = sDelimitedField.trim().replace("\"", "");
						}
						
						iFieldCounter++;
					}
					//We'll just re-set these on every line, since it won't hurt anything:
					glbatch.setsbatchdescription(sBatchDesc);
					glbatch.setsbatchdate(sBatchDate);
					glbatch.setsbatchstatus(Integer.toString(SMBatchStatuses.IMPORTED));
					if (sLastEntryNumber.compareToIgnoreCase(sEntryNumber) != 0){
						if (glentry != null){
							glbatch.addBatchEntry(glentry);
						}
						glentry = new GLTransactionBatchEntry();
						if (sEntryAutoReverse.compareToIgnoreCase("Y") == 0){
							glentry.setsautoreverse("1");
						}else{
							glentry.setsautoreverse("0");
						}
						glentry.setsdatdocdate(sEntryDocumentDate);
						glentry.setsdatentrydate(sEntryDate);
						glentry.setsentrydescription(sEntryDesc);
						glentry.setsentrynumber(sEntryNumber);
						glentry.setsfiscalperiod(sEntryFiscalPeriod);
						glentry.setsfiscalyear(sEntryFiscalYear);
						glentry.setssourceledger(sEntrySourceLedger);
						sLastEntryNumber = sEntryNumber;
					}
					glline = new GLTransactionBatchLine();
					glline.setsacctid(sLineGLAcct);
					glline.setscomment(sLineComment);
					glline.setscreditamt(sLineCreditAmt);
					glline.setsdebitamt(sLineDebitAmt);
					glline.setsdescription(sLineDescription);
					glline.setslinenumber(sLineNumber);
					glline.setsreference(sLineReference);
					glline.setssourceledger(glentry.getssourceledger());
					glline.setssourcetype(sLineSourceType);
					glline.setstransactiondate(sLineTransactionDate);

					glentry.addLine(glline);
					
				iLineCounter++;
			}
		}	
		//Now add the last line and entry:
		glentry.addLine(glline);
		glbatch.addBatchEntry(glentry);
		
		//Save the batch:
		glbatch.save_without_data_transaction(conn, sUserID, sUserFullName, false);
			
		} catch (FileNotFoundException ex) {
			throw new Exception("Error [1557852649] File not found error reading file:= " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("Error [1557852650] IO exception error reading file:= " + ex.getMessage() + ".");
		} catch (Exception ex) {
			throw new Exception("Error [1557852651] Exception - " + ex.getMessage() + ".");
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("Error [1557852652] IO exception error reading file:= " + ex.getMessage() + ".");
			}
		}
		return;
	}

	private void deleteCurrentTempImportFiles(String sTempImportFilePath) throws Exception{
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	throw new Exception("Error [1557852657] - directory " + sTempImportFilePath + " already exists.");
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  throw new Exception("Error [1557852658] - error deleting " 
	    		+ sTempImportFilePath + System.getProperty("file.separator") + info[i] + ".");
	      }
	    }

	}
	private void validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow, Connection conn) throws Exception{

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
			while ((line = br.readLine()) != null) {
				
				//If it's an empty line, jump out:
				if (line.compareToIgnoreCase("") == 0){
					break;
				}
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
					line = filterQuotesAndCommas(line);
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						if (iFieldCounter > NUMBER_OF_FIELDS_PER_LINE){
							//Allow additional fields, just ignore them:
							//this.addToErrorMessage("<BR>Line number " + iLineCounter + " has more than " 
							//	+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
							//bResult = false;
						}else{
							try {
								int iDataLineNumber = iLineCounter;
								if (bFileIncludesHeaderRow){
									iDataLineNumber = iDataLineNumber - 1;
								}
								validateImportField(iDataLineNumber, iFieldCounter, sDelimitedField.trim().replace("\"", ""), conn);
							} catch (Exception e) {
								throw new Exception(e.getMessage());
							}
						}
						iFieldCounter++;
					}
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						throw new Exception("Error [1557852659] - Line number " + iLineCounter + " has less than " 
							+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
					}
				}
			}
			if (iLineCounter == 0){
				throw new Exception("Error [1557852660] - The file has no lines in it.");
			}
		} catch (FileNotFoundException ex) {
			throw new Exception("Error [1557852661] - File not found error reading file:= " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("Error [1557852662] - IO exception error reading file:= " + ex.getMessage() + ".");
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("Error [1557852663] - IO exception error reading file:= " + ex.getMessage() + ".");
			}
		}
	}

	private void validateImportField (int iLineNumber, int iFieldIndex, String sField, Connection conn) throws Exception{
		
		//Strip off any quotation marks:
		sField = sField.replace("\"", "");
		
		if (bDebugMode){
			System.out.println("[1557948467] In validateImportField, line " + iLineNumber + ", FieldIndex = " + iFieldIndex 
				+ ", value = " + sField);
		}
		if (iFieldIndex == FIELD_BATCH_DATE){
			ServletUtilities.clsValidateFormFields.validateStandardDateField(sField, "Batch date on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_BATCH_DESC){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatches.sBatchDescriptionLength, "Batch description on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_ENTRY_NUMBER){
			ServletUtilities.clsValidateFormFields.validateIntegerField(
				sField, "Entry number on line #" + Integer.toString(iLineNumber), 1, ServletUtilities.clsValidateFormFields.MAX_INT_VALUE);
		}
		if (iFieldIndex == FIELD_ENTRY_DESC){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchentries.sentrydescriptionLength, "Entry description on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_ENTRY_DATE){
			ServletUtilities.clsValidateFormFields.validateStandardDateField(
				sField, "Entry date on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_ENTRY_DOCUMENTDATE){
			ServletUtilities.clsValidateFormFields.validateStandardDateField(
				sField, "Entry document date on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_ENTRY_FISCALYEAR){
			ServletUtilities.clsValidateFormFields.validateIntegerField(
				sField, "Fiscal year on line #" + Integer.toString(iLineNumber), 2015, 2050);
		}
		if (iFieldIndex == FIELD_ENTRY_FISCALPERIOD){
			ServletUtilities.clsValidateFormFields.validateIntegerField(
				sField, "Fiscal period on line #" + Integer.toString(iLineNumber), 1, 13);
		}
		if (iFieldIndex == FIELD_ENTRY_SOURCELEDGER){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchentries.ssourceledgerLength, "Source Ledger on line #" + Integer.toString(iLineNumber), false);
			//If it's not a valid source ledger, reject it:
			boolean bSourceLedgerIsValid = false;
			for (int i = 0; i < GLSourceLedgers.NO_OF_SOURCELEDGERS; i++){
				if (GLSourceLedgers.getSourceLedgerDescription(i).compareToIgnoreCase(sField) == 0){
					bSourceLedgerIsValid = true;
					break;
				}
			}
			if (!bSourceLedgerIsValid){
				throw new Exception("Entry source ledger on line #" + Integer.toString(iLineNumber) + " is not a valid source ledger.");
			}
		}		
		if (iFieldIndex == FIELD_ENTRY_AUTOREVERSE){
			if (
				(sField.compareToIgnoreCase("Y") != 0)
				&& (sField.compareToIgnoreCase("N") != 0)
			){
				throw new Exception("Auto reverse '" + sField + "' on line #" + Integer.toString(iLineNumber) + " must be Y or N.");
			}
		}
		if (iFieldIndex == FIELD_LINE_NUMBER){
			ServletUtilities.clsValidateFormFields.validateIntegerField(
				sField, "Line number on line #" + Integer.toString(iLineNumber), 1, ServletUtilities.clsValidateFormFields.MAX_INT_VALUE);
		}
		if (iFieldIndex == FIELD_LINE_DESC){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchlines.sdescriptionLength, "Line description on line #" + Integer.toString(iLineNumber), true);
		}
		if (iFieldIndex == FIELD_LINE_REFERENCE){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchlines.sreferenceLength, "Line reference on line #" + Integer.toString(iLineNumber), true);
		}
		if (iFieldIndex == FIELD_LINE_COMMENT){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchlines.scommentLength, "Line comment on line #" + Integer.toString(iLineNumber), true);
		}
		if (iFieldIndex == FIELD_LINE_TRANSACTIONDATE){
			ServletUtilities.clsValidateFormFields.validateStandardDateField(
				sField, "Line transaction date on line #" + Integer.toString(iLineNumber), false);
		}
		if (iFieldIndex == FIELD_LINE_GLACCT){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchlines.sacctidLength, "Line GL account on line #" + Integer.toString(iLineNumber), false);
			//Also verify that it's a valid GL account:
			GLAccount glacct = new GLAccount(sField);
			if (!glacct.load(conn)){
				throw new Exception("Line GL account '" + sField + "' on line #" + Integer.toString(iLineNumber) + " is not a valid unformatted GL account.");
			}
			
		}
		if (iFieldIndex == FIELD_LINE_DEBITAMT){
			ServletUtilities.clsValidateFormFields.validateBigdecimalField(
				sField, "Debit amount on line #" + Integer.toString(iLineNumber), 
				SMTablegltransactionbatchlines.bddebitamtScale, 
				BigDecimal.ZERO, 
				new BigDecimal("9999999.99")
			);
		}
		if (iFieldIndex == FIELD_LINE_CREDITAMT){
			ServletUtilities.clsValidateFormFields.validateBigdecimalField(
				sField, "Credit amount on line #" + Integer.toString(iLineNumber), 
				SMTablegltransactionbatchlines.bdcreditamtScale, 
				BigDecimal.ZERO, 
				new BigDecimal("9999999.99")
			);
		}
		if (iFieldIndex == FIELD_LINE_SOURCETYPE){
			ServletUtilities.clsValidateFormFields.validateStringField(
				sField, SMTablegltransactionbatchlines.ssourcetypeLength, "Line source type ('" + sField + "') on line #" + Integer.toString(iLineNumber), true);
		}
	}
	private String filterQuotesAndCommas(String sLine) throws Exception{
		String s = "";
		boolean bInQuoteDelimitedField = false;
		try {
			for (int i = 0; i < sLine.length(); i++){
				String sTestChar = sLine.substring(i, i + 1);
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
		} catch (Exception e) {
			throw new Exception("[1558383531] - error in filerQuotesAndCommas - " + e.getMessage());
		}
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
