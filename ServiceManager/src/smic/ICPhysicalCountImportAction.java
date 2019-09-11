package smic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
			objICPhysicalInventoryEntry.addErrorMessage( "Error [1548960605] - " + e1.getMessage());
			CurrentSession.setAttribute(ICPhysicalCountImportSelect.IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT,objICPhysicalInventoryEntry.getErrorMessages() );
			objICPhysicalInventoryEntry.clearError();
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
					m_uploadedFile);
		} catch (Exception e) {

			objICPhysicalInventoryEntry.addErrorMessage( e.getMessage());
			CurrentSession.setAttribute(ICPhysicalCountImportSelect.IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT,objICPhysicalInventoryEntry.getErrorMessages() );
			objICPhysicalInventoryEntry.clearError();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sICPhysicalCountImportActionCallingClass
					+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
					+ "&" + ICPhysicalCountEntry.ParamDesc + "=" + m_sCountDesc
					+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
			return;
		}

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
	private void createTempImportFileFolder(String sTempFileFolder) throws Exception{
		File dir = new File(sTempFileFolder);
		if (dir.exists()) {
			return;
		}

		//Need to create the path:
		try{
			// Create one directory
			if (!new File(sTempFileFolder).mkdir()) {
				throw new Exception("<BR>Error [1548955603] creating temp upload folder.");
			}    
		}catch (Exception e){//Catch exception if any
			throw new Exception("<BR>Error [1548955604] creating temp upload folder: " + e.getMessage() + ".");
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
			FileItem fUploadedFile
			) throws Exception{

		String sTempFilePath = SMUtilities.getAbsoluteRootPath(req, getServletContext())
				//+ System.getProperty("file.separator")
				+ "iccountuploads"
				;

		//If the folder has not been created, create it now:
		try {
			createTempImportFileFolder(sTempFilePath);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
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
					fUploadedFile);
		} catch (Exception e1) {
			throw new Exception("Error [1548956216] processing import - <BR>" + e1.getMessage());
		}
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
			FileItem fUploadedFile
			) throws Exception{


		String fileName = "ICIMPORT_" + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
		try {
			fUploadedFile.write(new File(sTempImportFilePath, fileName));
		} catch (Exception e) {
			//System.out.println("Error [1548956219]  error on fi.write: " + e.getMessage());
			throw new Exception("Error [1548956218] writing temporary file: " + e.getMessage());
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

		//Start Validation
		BufferedReader br = null;
		ArrayList<String> arrItemNumbers = new ArrayList<String>();
		ArrayList<String> arrQuantities = new ArrayList<String>();
		ArrayList<String> arrItemDesc = new ArrayList<String>();
		ArrayList<String> arrUofM = new ArrayList<String>();
		ArrayList<String> arrErrors = new ArrayList<String>();
		//This item extracts the Item #'s and Quantities of each item from the file.
		try {
			br = new BufferedReader(new FileReader(sTempImportFilePath + System.getProperty("file.separator") + fileName));
			String line = null;
			int iLineCounter = 0;
			String sItem = "";
			while ((line = br.readLine()) != null) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".insertCountLines - at line " + iLineCounter);
				}
				iLineCounter++;
				//If the file has a header row and if this is the first line, then it's the header line
				//so reset the line counter and don't do any validation of it:
				if (
						bIncludesHeaderRow
						&& (iLineCounter == 1)
						)
				{
					//Otherwise, if it's NOT the first row of a file with a header row, process:
				}else{
					int iFieldCounter = 0;
					String[] fields = line.split(",");
					for (String sDelimitedField : fields) {
						if (iFieldCounter == FIELD_QTY){
							arrQuantities.add( sDelimitedField.trim().replace("\"", ""));
						}
						if (iFieldCounter == FIELD_ITEM){
							sItem = sDelimitedField.trim().replace("\"", "");
							arrItemNumbers.add(stripSuffix(sItem));
						}
						iFieldCounter++;
					}					
					if (iFieldCounter < NUMBER_OF_FIELDS_PER_LINE){
						br.close();
						throw new Exception("Error [1548957349] - Line number " + iLineCounter + " has less than " 
								+ NUMBER_OF_FIELDS_PER_LINE + " fields in it ('" + line + "').");
					}
					arrErrors.add("");
				}
			}
			if(iLineCounter == 0) {
				br.close();
				throw new Exception("[1567790544] File has no lines.");
			}
		} catch (Exception e) {
			throw new Exception("[1567772303] Error reading file information");
		}
		br.close();


		// We now need to get the Desc and the U/M from these the item# from this list. If an error occurs, we should add onto the errors for that entry. 
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

		//This function grabs the Descriptions and Units of Measure for each item within the list. 
		for(int i = 0 ; i<arrItemNumbers.size(); i++) {
			String sItem = arrItemNumbers.get(i);
			//Try Function to get Desc with Catch adding to Errors list
			ArrayList<String> arrUofMAndDesc = new ArrayList<String>();
			try {
				arrUofMAndDesc = getUoMAndDesc(sItem, conn, sDBID, sUserFullName);
				if(arrUofMAndDesc.get(0).compareToIgnoreCase("")==0 || arrUofMAndDesc.get(1).compareToIgnoreCase("")==0) {
					arrUofM.add("");
					arrItemDesc.add("");
					arrErrors.set(i, arrErrors.get(i) + "Item Number is not Valid; ");
				}else {
					arrUofM.add(i,arrUofMAndDesc.get(0));
					arrItemDesc.add(i,arrUofMAndDesc.get(1));
				}
			}catch (Exception e) {
				arrUofM.add("");
				arrItemDesc.add("");
				arrErrors.set(i, arrErrors.get(i) + "Item Number is not Valid; ");
			}
		}


		//Check to see, if we are not adding items, that all items are contained within the worksheet already.
		if(!bAddNewItems) {
			for(int i = 0 ; i < arrItemNumbers.size(); i++) {
				String SQL = "SELECT"
						+ " " + SMTableicinventoryworksheet.sitemnumber + " FROM " + SMTableicinventoryworksheet.TableName
						+ " WHERE (" 
						+ " " + SMTableicinventoryworksheet.sitemnumber + " = '" + arrItemNumbers.get(i) + "'"
						+ " AND" 
						+ " " + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + sPhysicalInventoryID
						+ ")"
						;
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
						arrErrors.set(i, arrErrors.get(i) + "This item is not in this physical inventory already;  ");
					}
					rs.close();
				} catch (SQLException e) {
					throw new Exception("Error [1548958827] - SQL Error validating item number ('" + arrItemNumbers.get(i) + "') on line number " 
							+ i + " with SQL - " + SQL + " - " + e.getMessage() + ".");
				}
			}
		}

		//Check for valid Quantities
		for(int i = 0; i < arrQuantities.size(); i++) {
			try {
				Double.parseDouble(arrQuantities.get(i));
			} catch (Exception e) {
				arrErrors.set(i, arrErrors.get(i) + "The quantity " + arrQuantities.get(i) + " is not a valid number;  ");
			}
		}

		boolean bContainsErrors = false;
		//Check to see if there are errors at all
		for(int i = 0; i < arrItemNumbers.size(); i++) {
			if(arrErrors.get(i).compareToIgnoreCase("")!=0) {
				bContainsErrors=true;
			}
		}

		if(bContainsErrors==true) {
			throw new Exception(returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}
		
		//Now after making sure all entries are valid, we now should be inserting them into the physical count.
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1548956219] starting data transaction - " + returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}

		//We'll need a physical inventory object to process the file:
		ICPhysicalInventoryEntry objICPhysicalInventoryEntry = new ICPhysicalInventoryEntry();
		
		objICPhysicalInventoryEntry.slid(sPhysicalInventoryID);
		if (!objICPhysicalInventoryEntry.load(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1548956220]");
			throw new Exception("Error [1538513151] - Could not load physical inventory - " + objICPhysicalInventoryEntry.getErrorMessages() + returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}
		
		//Create the Count Entry
		ICPhysicalCountEntry count = new ICPhysicalCountEntry();
		count.setsCreatedByID(sUserID);
		count.setsCreatedByFullName(sUserFullName);
		count.setsDescription(sCountDesc);
		count.setsPhysicalInventoryID(sPhysicalInventoryID);

		//If errors occur print the error, and the list
		if(!count.save_without_data_transaction(conn, sUserID, sUserFullName)) {
			throw new Exception("Error Inserting Count [1568216019] : " + count.getErrorMessages() + returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}
		
		//Add each individual Line Entry
		for(int i = 0; i < arrItemNumbers.size(); i++) {
			ICPhysicalCountLineEntry countEntry = new ICPhysicalCountLineEntry();
			countEntry.setsCountID(count.slid());
			countEntry.setsItemNumber(arrItemNumbers.get(i));
			countEntry.setsPhysicalInventoryID(sPhysicalInventoryID);
			countEntry.setsQty(arrQuantities.get(i));
			if(!countEntry.save_without_data_transaction(conn, bAddNewItems)) {
				arrErrors.set(i, arrErrors.get(i) + "Error saving: " + countEntry.getErrorMessages() + "; ");
			}
			if (bAddNewItems){
				objICPhysicalInventoryEntry.addSingleItem(arrItemNumbers.get(i), conn);
			}
		}
		
		
		for(int i = 0; i < arrItemNumbers.size(); i++) {
			if(arrErrors.get(i).compareToIgnoreCase("")!=0) {
				bContainsErrors=true;
			}
		}
		
		if(bContainsErrors==true) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception(returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			throw new Exception("Error [1538513252] Could not commit data transaction." + returnErrors(arrItemNumbers, arrQuantities, arrItemDesc, arrUofM, arrErrors));
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080880]");
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
	
	//Collapse both into one function

	public ArrayList<String> getUoMAndDesc(
			String sItemNumber, 
			Connection conn,
			String sDBID, 
			String sUserFullName
			) throws Exception {
		String SQL = "SELECT " + SMTableicitems.TableName + "." +  SMTableicitems.sCostUnitOfMeasure + 
				" , " + SMTableicitems.TableName + "." +  SMTableicitems.sItemDescription 
				+ " FROM " + SMTableicitems.TableName +
				" WHERE " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + "  =  " + "'"+sItemNumber+"'";
		ArrayList<String> returnList = new ArrayList<String>();
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".validateItemNumber - user: " + sUserFullName)
					);

			while(rs.next()) {
				returnList.add(0,rs.getString(SMTableicitems.sCostUnitOfMeasure));
				returnList.add(1,rs.getString(SMTableicitems.sItemDescription));
			}
			rs.close();
			return returnList;
		} catch (Exception e) {
			throw e;
		}
	}

	public String returnErrors(
			ArrayList<String> ItemNumbers,
			ArrayList<String> Quantities,
			ArrayList<String> ItemDesc,
			ArrayList<String> UofM,
			ArrayList<String> Errors
			) {
		String error = "<TABLE Width = 100%>\n"
				+ "<TR>\n"
				+ "<TD><B>Position</B></TD>\n"
				+ "<TD><B>Item Number</B></TD>\n"
				+ "<TD><B>Qty</B></TD>\n"
				+ "<TD><B>Item Desc.</B></TD>\n"
				+ "<TD><B>U/M</B></TD>\n"
				+ "<TD><B>Errors</B></TD>\n"
				+ "</TR>\n"
				;
		for(int i = 0; i < ItemNumbers.size(); i++) {
			error+="<TR>\n";
			if(Errors.get(i).compareToIgnoreCase("")!=0) {
				error+="<TD>";
				error +="<font color=\"red\">* " +(i+1) + "</font>";
				error+="</TD>\n";
				
				error+="<TD>";
				error +="<font color=\"red\"> " +ItemNumbers.get(i) + "</font>";
				error+="</TD>\n";
				
				error+="<TD>";
				error +="<font color=\"red\"> " +Quantities.get(i) + "</font>";
				error+="</TD>\n";
				
				error+="<TD>";
				error +="<font color=\"red\"> " +ItemDesc.get(i) + "</font>";
				error+="</TD>\n";
				
				error+="<TD>";
				error +="<font color=\"red\"> " +UofM.get(i) + "</font>";
				error+="</TD>\n";
				
				error+="<TD>";
				error +="<font color=\"red\"> " +Errors.get(i) + "</font>";
				error+="</TD>\n";
			}else {
				error+="<TD>";
				error +=(i+1) + "";
				error+="</TD>\n";
				
				error+="<TD>";
				error += ItemNumbers.get(i) + "";
				error+="</TD>\n";
				
				error+="<TD>";
				error += Quantities.get(i) + "";
				error+="</TD>\n";
				
				error+="<TD>";
				error += ItemDesc.get(i) + "";
				error+="</TD>\n";
				
				error+="<TD>";
				error += UofM.get(i) + "";
				error+="</TD>\n";
				
				error+="<TD>";
				error += Errors.get(i) + "";
				error+="</TD>\n";
			}

			
			error+="</TR>\n";
		}
		error+= "</TABLE>\n";
		return error;
	}
	
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
