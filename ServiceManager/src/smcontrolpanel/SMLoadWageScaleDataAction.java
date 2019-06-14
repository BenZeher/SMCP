package smcontrolpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import SMDataDefinition.SMTablewagescalerecords;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMLoadWageScaleDataAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static int NUMBER_OF_FIELDS_PER_LINE = 24;
	
	private static int FIELD_EMPLOYEENAME = 0;
	private static int FIELD_EMPLOYEESSN = 1;
	private static int FIELD_EMPLOYEEADDRESS = 2;
	private static int FIELD_EMPLOYEEADDRESS2 = 3;
	private static int FIELD_EMPLOYEECITY = 4;
	private static int FIELD_EMPLOYEESTATE = 5;
	private static int FIELD_EMPLOYEEZIPCODE = 6;
	private static int FIELD_EMPLOYEETITLE = 7;
	private static int FIELD_PERIODENDDATE = 8;
	private static int FIELD_COSTNUMBER = 9;
	private static int FIELD_REGHOURS = 10;
	private static int FIELD_OTHOURS = 11;
	private static int FIELD_DTHOURS = 12;
	private static int FIELD_PAYRATE = 13;
	private static int FIELD_HOLIDAYHOURS = 14;
	private static int FIELD_PERSONALHOURS = 15;
	private static int FIELD_VACHOURS = 16;
	private static int FIELD_GROSS = 17;
	private static int FIELD_FEDERAL = 18;
	private static int FIELD_SS = 19;
	private static int FIELD_MEDICARE = 20;
	private static int FIELD_STATE = 21;
	private static int FIELD_MISCDED = 22;
	private static int FIELD_NETPAY = 23;
	private static int FIELD_VACALLOWED = 24;
	
	private static String sError = "";
	
	//Member variables for the data:
	/*
	private static String m_sEmployeeName = "";
	private static String m_sEmployeeSSN = "";
	private static String m_sEmployeeAddress = "";
	private static String m_sEmployeeCity = ""; 
	private static String m_sEmployeeState = "";
	private static String m_sEmployeeZipCode = "";
	private static String m_sEmployeeTitle = "";
	private static String m_sCostNumber = ""; //combination of day and order number
	private static double m_dRegHours = 0;
	private static double m_dOTHours = 0;
	private static double m_dDTHours = 0;
	private static double m_dPayRate = 0;	
	private static double m_dHolidayHours = 0;
	private static double m_dPersonalHours = 0;
	private static double m_dVacHours = 0;
	private static double m_dGross = 0;
	private static double m_dFederal = 0;
	private static double m_dSS = 0;
	private static double m_dMedicare = 0;
	private static double m_dState = 0;
	private static double m_dMiscDed = 0;
	private static double m_dNetPay = 0;
	*/
	private static boolean bDebugMode = false;
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM/dd/yyyy");
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (bDebugMode){
			System.out.println("In " + this.toString() + " 01");
		}
		
		PrintWriter out = response.getWriter();

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sStatus = "";
	    sError = "";
	    sCallingClass = "smcontrolpanel.SMLoadWageScaleDataSelect";
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
	    
    	//Customized title

	    String title = "Import wage scale job data.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    if (!validateWageScaleRecords(getServletContext(), sDBID, sUserName, sUserFullName)){
	    	sError = "ALL wage scale records must be deleted before uploading a new file";
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sError
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    	return;
	    }
		   String fileName = "WAGESCALEIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		   String  sTempFilePath = SMUtilities.getAbsoluteRootPath(request, getServletContext())
					+ "uploads"
					+ System.getProperty("file.separator");
	    //Create a Temporary File Folder if not present
	    if (!createTempImportFileFolder(sTempFilePath)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sError
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
    	//Delete Temporary Files if present
		if (!deleteCurrentTempImportFiles(sTempFilePath)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sError
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
	    
	    
	    // Set values needed to write data to temp values for scope reasons
		   boolean bIncludesHeaderRow = false;
		   String encryptionKey = "";

			 //Read the file from the request:
	        DiskFileItemFactory factory = new DiskFileItemFactory();
	        // maximum size that will be stored in memory
	        factory.setSizeThreshold(4196);
	        // the location for saving data that is larger than getSizeThreshold()
	        factory.setRepository(new File(sTempFilePath));
	        ServletFileUpload upload = new ServletFileUpload(factory);
	        // maximum size before a FileUploadException will be thrown
	        upload.setSizeMax(1000000);
	        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	        if (bDebugMode){
	        	System.out.println("In " + this.toString() + ".writeFileAndProcess - isMultipart = " + isMultipart);
	        }
	    	List<FileItem> fileItems = null;
	    	
		    try {
				//Check to see if the file has a header row:
				bIncludesHeaderRow = false;
				
				//populate fileItems
				try {
					fileItems = upload.parseRequest(request);
				} catch (FileUploadException e1) {
					this.addToErrorMessage("<BR>"
						+ "Error on upload.parseRequest: " + e1.getMessage());
					if (bDebugMode){
						System.out.println("In " + this.toString() + " error on upload.parseRequest: " 
							+ e1.getMessage());
					}
					throw new Exception("Error [1548682854] - " + e1.getMessage());
				}
				Iterator<FileItem> iter = fileItems.iterator();
				//Get the CallingClass, Includes Header Row, and Encryption Key Information
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
				    	if (item.getFieldName().compareToIgnoreCase(
					    	"INCLUDESHEADERROW") == 0){
				    			bIncludesHeaderRow = true;
									if (bDebugMode){
										System.out.println(
											"In " + this.toString() 
											+ ".writeFileAndProcess, parameter "
											+ "INCLUDESHEADERROW = " + bIncludesHeaderRow + "."); 
									}
				    	}
				    	
				    	if (item.getFieldName().compareToIgnoreCase(
						    	SMWageScaleDataEntry.ParamEncryptionKey) == 0){ 
				    				encryptionKey = item.getString();
				    				if(encryptionKey.compareToIgnoreCase("") == 0){
				    					this.addToErrorMessage("Encryption Key is required");
				    					throw new Exception("Error [1548682855] - Encryption key is required.");
				    				}
				    				if(encryptionKey.compareToIgnoreCase("") != 0 && 
				    					encryptionKey.length() < SMWageScaleDataEntry.MinimumEncryptionKeyLength){
				    					this.addToErrorMessage("Encryption Key must at least " 
				    				         + Integer.toString(SMWageScaleDataEntry.MinimumEncryptionKeyLength) + " characters");
				    					throw new Exception("Error [1548682856] - Encryption Key must be at least " 
				    				         + Integer.toString(SMWageScaleDataEntry.MinimumEncryptionKeyLength) + " characters");
				    				}
								}  	
				    }
				}
			} catch (Exception e) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
						+ "?Warning=" + sError
						+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
				}		
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
						+ "?Warning=" + sError
						+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
				);
				return;
			}
	    //Verify there are no wage scale records before uploading file
	  
	   // System.out.println(sTempFilePath+ " \n"+fileItems.toString()+"\n"+SMUtilities.getAbsoluteRootPath(request, getServletContext()) +"\n"+fileName+"[1560456895]");
	    try {
		    writeFileAndProcess(sTempFilePath, CurrentSession, request, out, sDBID, sUserName, sUserID, sUserFullName,fileName, bIncludesHeaderRow, encryptionKey,fileItems,upload);
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e) {
			if (bDebugMode){
				System.out.println("In " + this.toString() + ".doPost - processRequest failed: "
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sError
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}		
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sError
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
    	//if importing is successful, print out a message:
    	out.println("Importing of wage scale job records is successful.<BR>");
    	
    	//create a link to run the real wage scale reports
    	String sLink = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWageScaleReportSelect" 
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
    	out.println("<A HREF=\"" + sLink + "\">Create wage scale report</A>");
    	out.println("</HTML>");
		
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
	private boolean  writeFileAndProcess(String sTempImportFilePath,
										HttpSession ses, 
										HttpServletRequest req,
										PrintWriter pwOut,
										String sDBID,
										String sUserName,
										String sUserID,
										String sUserFullName,
										String fileName,
										boolean bIncludesHeaderRow,
										String encryptionKey,
										List<FileItem> fileItems, 
										ServletFileUpload upload
										) throws Exception{
    	Iterator<FileItem> iter = fileItems.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();
		    if(item.isFormField()) {
		    	
		    }else{
		    	//It's a file - 
		    	FileItem fi = item;
		    	//System.out.println(fi.getName() + " AND " + fileName + " IN "+ sTempImportFilePath + "\n");
		        // write the file
		        try {
					fi.write(new File(sTempImportFilePath, fileName));
				} catch (Exception e) {
					this.addToErrorMessage("<BR>"
						+ "Error writing temporary file: " + e.getMessage());
					throw new Exception("Error [1548682857] - error writing temporary file - " + e.getMessage());
				}
				//InputStream uploadedStream = item.getInputStream();
		    }
		}
		
		if (!validateFile(sTempImportFilePath, fileName, bIncludesHeaderRow)){
			throw new Exception("Error [1548682858] - error validating file.");
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
						+ sUserFullName)
		);
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess got a connection");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			this.addToErrorMessage("Could not start data transaction.");
			throw new Exception("Error [1548682859] - couldn't start data transaction.");
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".writeFileAndProcess going into insertRecords");
		}
		
		if (!insertRecords(sTempImportFilePath, fileName, conn, bIncludesHeaderRow, encryptionKey, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1548682860] - couldn't insert records.");
		}else{
			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				this.addToErrorMessage("Could not commit data transaction.");
				throw new Exception("Error [1548682861] - couldn't commit data transaction.");
			}
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080584]");
		return true;

	}
	private boolean insertRecords(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			String encryptKey,
			String sUserID
	){
		
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
			
			SMWageScaleDataEntry record = new SMWageScaleDataEntry();
			record.setEncryptionKey(encryptKey);
			
			while ((line = br.readLine()) != null) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".insertCountLines - at line " + iLineCounter);
				}
				iLineCounter++;
				
				if (bFileIncludesHeaderRow && (iLineCounter == 1)){
					//If the file has a header row and if this is the first line, then it's the header line
					//so reset the line counter and don't do any validation of it:
				}else if (line.trim().length() == 0){
					//skip and goto the next line.
				}else{
					//Otherwise, if it's NOT the first row of a file with a header row, process:
					int iFieldCounter = 0;
					String[] fields = line.split(",");
					
					for (String sDelimitedField : fields) {
						
						if (iFieldCounter == FIELD_EMPLOYEENAME){
							record.setEmployeeName(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_EMPLOYEESSN){
							record.setEmployeeSSN(sDelimitedField.trim().replace("\"", "").replace("-", ""));
						}
						if (iFieldCounter == FIELD_EMPLOYEEADDRESS){
							record.setEmployeeAddress(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_EMPLOYEEADDRESS2){
							record.setEmployeeAddress2(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_EMPLOYEECITY){
							record.setEmployeeCity(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_EMPLOYEESTATE){
							record.setEmployeeState(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_EMPLOYEEZIPCODE){
							record.setEmployeeZipCode(sDelimitedField.trim().replace("\"", ""));
						}
						if (iFieldCounter == FIELD_EMPLOYEETITLE){
							record.setEmployeeTitle(clsDatabaseFunctions.FormatSQLStatement(sDelimitedField.trim().replace("\"", "")));
						}
						if (iFieldCounter == FIELD_COSTNUMBER){
							record.setCostNumber(sDelimitedField.trim().replace("\"", ""));
						}
						if (iFieldCounter == FIELD_PERIODENDDATE){
							try{
								//System.out.println("[1381412624]sDelimitedField.trim().replace(\"\\\"\", \"\") = " + sDelimitedField.trim().replace("\"", ""));
								record.setPeriodEndDate(USDateformatter.parse(sDelimitedField.trim().replace("\"", "")));
							}catch(ParseException ex){
								this.addToErrorMessage("Error parsing line " + iLineCounter + " - " + record.getErrorMessages());
								return false;
							}
						}
						if (iFieldCounter == FIELD_REGHOURS){
							record.setRegHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_OTHOURS){
							record.setOTHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_DTHOURS){
							record.setDTHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_PAYRATE){
							record.setPayRate(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_HOLIDAYHOURS){
							record.setHolidayHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_PERSONALHOURS){
							record.setPersonalHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_VACHOURS){
							record.setVacHours(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_GROSS){
							record.setGross(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_FEDERAL){
							record.setFederal(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_SS){
							record.setSS(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_MEDICARE){
							record.setMedicare(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_STATE){
							record.setState(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_MISCDED){
							record.setMiscDed(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_NETPAY){
							record.setNetPay(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						if (iFieldCounter == FIELD_VACALLOWED){
							record.setVacAllowed(Double.parseDouble(process_number(sDelimitedField.trim().replace("\"", ""))));
						}
						iFieldCounter++;
					}
					
					if(!record.save_without_data_transaction(iLineCounter, conn, sUserID)){
						this.addToErrorMessage("Could not save line " + iLineCounter + " - " + record.getErrorMessages());
						return false;
					}
				}
			}
		} catch (FileNotFoundException ex) {
			this.addToErrorMessage("<BR>" + "File not found error reading file:= " + ex.getMessage() + ".");
			return false;
		} catch (IOException ex) {
			this.addToErrorMessage("<BR>" + "IO exception error reading file:= " + ex.getMessage() + ".");
			return false;
		} catch (NumberFormatException ex){
			this.addToErrorMessage("<BR>" + "Number format exception error reading file:= " + ex.getMessage() + ".");
			return false;
		}
		finally {
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
	@SuppressWarnings("unused")
	private boolean validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow) {

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
				}else if (!line.isEmpty()){
					//only count number of fields if the line is not empty.
					int iFieldCounter = 0;
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						//Check to make sure there is enough fields on each line.
						/*
						if (iFieldCounter > NUMBER_OF_FIELDS_PER_LINE){
							//Allow additional fields, just ignore them:
							//this.addToErrorMessage("<BR>Line number " + iLineCounter + " has more than " 
							//	+ NUMBER_OF_FIELDS_PER_LINE + " fields in it.");
							//bResult = false;
						}else{
							if (!validateImportField(
									iLineCounter, iFieldCounter, sDelimitedField.trim().replace("\"", ""))){
								bResult = false;
							}
						}
						*/
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
	
	private boolean validateWageScaleRecords(ServletContext context, String sDBID, String sUserID, String sUserFullName ) {
		boolean bIsValid = false;
		String sSQL = "";
    	ResultSet rs = null;
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
						+ ".validateWageScaleRecords - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						)
		);
    	try{
	    	sSQL = " SELECT * FROM " + SMTablewagescalerecords.TableName;
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    	//IF there are no entries return true
	    	if (!rs.next()){
	    		bIsValid = true;
	    	}
	    	rs.close();
    	}catch (SQLException ex){	
    		System.out.println("Error validating wagescalerecords table is empty");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080583]");
		return bIsValid;
	}

	private void addToErrorMessage(String sMsg){
		
		if (sError.length() > 900){
			sError = sError.substring(0, 900) + " . . . (remaining errors truncated).";
		}else{
			sError += sMsg;
		}
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "SMWAGESCALEIMPORT", sMsg);
		}
	}
	private String process_number(String s){
		if (s.substring(s.length() - 1).compareTo("-") == 0){
			return "-" + s.substring(0, s.length() - 1).trim();
		}else{
			return s.trim();
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
