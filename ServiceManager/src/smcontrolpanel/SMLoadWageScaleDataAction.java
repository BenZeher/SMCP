package smcontrolpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	
	private static String IMPORT_FILE_PREFIX = "WAGESCALEIMPORT_";
	
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
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM/dd/yyyy");
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sStatus = "";
	    String sCallingClass = "";
	    // Set values needed to write data to temp values for scope reasons
	    boolean bIncludesHeaderRow = false;
	    String sEncryptionKey = "";
		String sFileName = IMPORT_FILE_PREFIX + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		String sTempFilePath = SMUtilities.getAbsoluteSMTempPath(request, getServletContext());

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
	    
	    try {
			validateWageScaleRecords(getServletContext(), sDBID, sUserName, sUserFullName);
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
					+ "?Warning=" + URLEncoder.encode(e.getMessage())
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}

	    //Create a Temporary File Folder if not present
	    try {
			createTempImportFileFolder(sTempFilePath);
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
					+ "?Warning=" + URLEncoder.encode(e.getMessage())
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
	    
    	//Delete Temporary Files if present
		try {
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
					+ "?Warning=" + e.getMessage()
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}

		 //Read the file from the request:
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4196);
        // the location for saving data that is larger than getSizeThreshold()
        //factory.setRepository(new File(sTempFilePath));
        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum size before a FileUploadException will be thrown
        upload.setSizeMax(1000000);
        List<FileItem> fileItems = null;
    	
	    try {
			//Check to see if the file has a header row:
			bIncludesHeaderRow = false;
			
			//populate fileItems
			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e1) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
								+ "?Warning=" + URLEncoder.encode(e1.getMessage())
						+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
				);
				return;
			}
			
			Iterator<FileItem> iter = fileItems.iterator();
			//Get the CallingClass, Includes Header Row, and Encryption Key Information
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
			    if (item.isFormField()) {
			    	if (item.getFieldName().compareToIgnoreCase("CallingClass") == 0){
			    		sCallingClass = item.getString();
			    	}
			    	if (item.getFieldName().compareToIgnoreCase(
				    	"INCLUDESHEADERROW") == 0){
			    			bIncludesHeaderRow = true;
			    	}
			    	if (item.getFieldName().compareToIgnoreCase(SMWageScaleDataEntry.ParamEncryptionKey) == 0){ 
	    				sEncryptionKey = item.getString();
	    				if(sEncryptionKey.compareToIgnoreCase("") == 0){
	    					throw new Exception("Error [1548682855] - Encryption key is required.");
	    				}
	    				if(sEncryptionKey.compareToIgnoreCase("") != 0 && 
	    					sEncryptionKey.length() < SMWageScaleDataEntry.MinimumEncryptionKeyLength){
	    					throw new Exception("Error [1548682856] - Encryption Key must be at least " 
	    				         + Integer.toString(SMWageScaleDataEntry.MinimumEncryptionKeyLength) + " characters");
	    				}
					}  	
			    }else{
			    	//It's a file - 
			    	FileItem fi = item;
			        try {
						fi.write(new File(sTempFilePath, sFileName)); 
					} catch (Exception e) {
						response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
										+ "?Warning=" + URLEncoder.encode(e.getMessage())
								+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
						);
						return;
					}
			    }
			}
		} catch (Exception e) {		
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMLoadWageScaleDataSelect"
					+ "?Warning=" + URLEncoder.encode(e.getMessage())
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
	    
		try {
		    writeFileAndProcess(sTempFilePath, sDBID, sUserName, sUserID, sUserFullName,sFileName, bIncludesHeaderRow, sEncryptionKey);
			deleteCurrentTempImportFiles(sTempFilePath);
		} catch (Exception e) {	
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + URLEncoder.encode(e.getMessage())
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			
			);
			return;
		}
		
    	//if importing is successful, print out a message:
    	out.println("Wage scale job records were successfully imported.<BR>");
    	
    	//create a link to run the real wage scale reports
    	String sLink = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWageScaleReportSelect" 
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
    	out.println("<A HREF=\"" + sLink + "\">Create wage scale report</A>");
    	out.println("</HTML>");
		
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
	        	throw new Exception("Error [1560864620] - could not create upload folder on server.");
	        }    
        }catch (Exception e){
        	throw new Exception("Error [1560864621] - " + e.getMessage());
	    }
	    return;
	}
	private void  writeFileAndProcess(String sTempImportFilePath,
										String sDBID,
										String sUserName,
										String sUserID,
										String sUserFullName,
										String sFileName,
										boolean bIncludesHeaderRow,
										String sEncryptionKey
										) throws Exception{

		try {
			validateFile(sTempImportFilePath, sFileName, bIncludesHeaderRow);
		} catch (Exception e) {
			throw new Exception(" %5B1560799723%5D - " + e.getMessage());
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
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error %5B1548682859%5D - couldn't start data transaction.");
		}
		
		try {
			insertRecords(sTempImportFilePath, sFileName, conn, bIncludesHeaderRow, sEncryptionKey, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error %5B1548682860%5D - couldn't insert records - " + e.getMessage() + ".");
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			throw new Exception("Error %5B1548682861%5D - couldn't commit data transaction.");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080584]");
		return;
	}
	private void insertRecords(
			String sFilePath,
			String sFileName,
			Connection conn,
			boolean bFileIncludesHeaderRow,
			String encryptKey,
			String sUserID
			) throws Exception{

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;

			SMWageScaleDataEntry record = new SMWageScaleDataEntry();
			record.setEncryptionKey(encryptKey);

			while ((line = br.readLine()) != null) {
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
								record.setPeriodEndDate(USDateformatter.parse(sDelimitedField.trim().replace("\"", "")));
							}catch(ParseException ex){
								throw new Exception("Error %5B20191691351531%5D setting period end date '" + sDelimitedField + "' parsing line " + iLineCounter + " - " + record.getErrorMessages() + ".");
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
						throw new Exception("Error %5B2019169143296%5D " + "Could not save line " + iLineCounter + " - " + record.getErrorMessages());
					}
				}
			}
		} catch (FileNotFoundException ex) {
			throw new Exception("Error %5B20191691432395%5D " + "File not found error reading file - " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("Error %5B20191691433148%5D " + "IO exception error reading file - " + ex.getMessage() + ".");
		} catch (NumberFormatException ex){
			throw new Exception("Error %5B20191691433376%5D " + "Number format exception error reading file - " + ex.getMessage() + ".");
		}
		finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("Error %5B2019169143460%5D " + "IO exception error reading file - " + ex.getMessage() + ".");
			}
		}
		return;
	}

	private void deleteCurrentTempImportFiles(String sTempImportFilePath) throws Exception{
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	throw new Exception("Error [1560864809] - Temp import file directory '" + sTempImportFilePath + "' does not exist.");
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (info[i].startsWith(IMPORT_FILE_PREFIX)){
		      if (!n.delete()){
		    	  throw new Exception("Error [1560864810] - Unable to delete file '" + sTempImportFilePath + info[i] + "'.");
		      } 
	      }
	    }
	    return;
	}
	private void validateFile(String sFilePath, String sFileName, boolean bFileIncludesHeaderRow) throws Exception{

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(sFilePath + System.getProperty("file.separator") + sFileName));
			String line = null;
			int iLineCounter = 0;
			while ((line = br.readLine()) != null) {
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
					for (@SuppressWarnings("unused") String sDelimitedField : fields) {
						//Check to make sure there are enough fields on each line.
						iFieldCounter++;
					}
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){

						throw new Exception(" %5B1560799687%5D Line number " + iLineCounter + " has less than " 
								+ NUMBER_OF_FIELDS_PER_LINE + " fields in it. ");
					}
				}
			}
			if (iLineCounter == 0){
				throw new Exception(" %5B1560799719%5D The file has no lines in it.");
			}
		} catch (FileNotFoundException ex) {
			throw new Exception("%5B1560799720%5D" + " File not found error reading file - " + ex.getMessage() + ".");
		} catch (IOException ex) {
			throw new Exception("%5B1560799721%5D" + " IO exception error reading file - " + ex.getMessage() + ".");
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				throw new Exception("%5B1560799722%5D" + " IO exception error reading file - " + ex.getMessage() + ".");
			}
		}
		return;
	}
	
	private void validateWageScaleRecords(ServletContext context, String sDBID, String sUserID, String sUserFullName ) throws Exception{
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
	    	if (rs.next()){
	    		throw new Exception("Error [1560864417] - Wage Scale records must all be deleted before importing a new file.");
	    	}
	    	rs.close();
    	}catch (Exception ex){	
    		throw new Exception("Error [1560864418] checking for existing wage scale records - " + ex.getMessage());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080583]");
		return;
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
