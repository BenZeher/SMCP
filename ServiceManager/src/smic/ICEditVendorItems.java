package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smap.APVendor;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICEditVendorItems extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String EDITVENDORITEM_SUBMIT_BUTTON = "SubmitEdit";
	public static String EDITVENDORITEM_SUBMIT_LABEL = "Update vendor items";
	public static String NEWVENDOR_NAME = "NEWVENDOR";
	public static String NEWVENDORITEM_NAME = "NEWITEM";
	public static String NEWVENDORCOST_NAME = "NEWCOST";
	public static String NEWVENDORITEMCOMMENT_NAME = "NEWCOMMENT";
	public static String EDITINGSINGLEVENDOR_PARAM = "EDITINGSINGLEVENDOR";
	public static String INITIATED_FROM_PO_PARAM = "INITIATEDFROMPO";
	public static String ITEMNUMBER = "ITEMNUMBER";
	public static String VENDOR_PARAMETER_MARKER = "VENDORACCT";
	public static String VENDORITEM_PARAMETER_MARKER = "VENDORITEM";
	public static String VENDORCOST_PARAMETER_MARKER = "VENDORCOST";
	public static String VENDORITEMCOMMENT_PARAMETER_MARKER = "VENDORCOMMENT";
	public static String NUMBEROFCURRENTRECORDS_PARAMETER = "NUMBEROFCURRENTRECORDS";
	
	private static String sCalledClassName = "ICEditVendorItemsAction";
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditVendorItems)){
			return;
		}
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    //First, get the item number:
	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter(ITEMNUMBER, request).trim();
	    
	    if (sItemNumber.compareToIgnoreCase("") == 0){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic/" + SMUtilities.getFullClassName(this.toString())
					+ "?" + ITEMNUMBER + "=" + sItemNumber
					+ "&Warning=Item number cannot be blank."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    }
	    
	    String sEditSingleVendor = clsManageRequestParameters.get_Request_Parameter(EDITINGSINGLEVENDOR_PARAM, request);
	    String sInitiatedFromPO = clsManageRequestParameters.get_Request_Parameter(INITIATED_FROM_PO_PARAM, request);
	    try {
			SMUtilities.addURLToHistory(
				"Editing vendor information for item " + sItemNumber, 
				CurrentSession, 
				request
			);
		} catch (Exception e) {
		}
	    
		String title = "Edit vendor item information";
		String subtitle = "Item # " + sItemNumber;
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("WARNING", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("STATUS", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditVendorItems) 
	    	+ "\">Summary</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A>");
	    
	    //Create a form for editing the records:
	    Edit_Records(
	    		sItemNumber,
	    		sEditSingleVendor,
	    		sInitiatedFromPO,
	    		out, 
	    		sDBID, 
	    		sUserName, 
	    		sUserID,
	    		sUserFullName,
	    		CurrentSession
	    );
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Records(
			String sItem,
			String sEditingSingleVendor,
			String sInitiatedFromPO,
			PrintWriter pwOut, 
			String sConf,
			String sUser,
			String sUserID,
			String sUserFullName,
			HttpSession session
			){
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + ITEMNUMBER + "' VALUE='" + sItem + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" 
			+ SMUtilities.getFullClassName(this.toString()) + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + INITIATED_FROM_PO_PARAM + "' VALUE='" + sInitiatedFromPO + "'>");
		
		pwOut.println("<style type=\"text/css\">");
		pwOut.println(
			"table.main {"
				+ "border-width: 0px; "
				+ "border-spacing: ; "
				+ "border-style: outset; "
				+ "border-color: gray; "
				+ "border-collapse: separate; "
				//+ "background-color: white; "
			+ "}"
		);

		pwOut.println(
			"table.main th {"
				+ "border-width: 0px; "
				+ "padding: 1px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				//+ "background-color: white; "
			+ "}"
		);
		
		pwOut.println(
			"table.main td {"
				+ "border-width: 0px; "
				+ "padding: 1px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				//+ "background-color: white; "
			+ "}"
		);
		
		pwOut.println("</style>");
		
		pwOut.println("<table class=\"main\">");
		pwOut.println("<TR>");
		pwOut.println("<TH>Vendor</TH>");
		pwOut.println("<TH>Vendor item</TH>");
		pwOut.println("<TH>Cost</TH>");
		pwOut.println("<TH>Comment</TH>");
		pwOut.println("</TR>");

		@SuppressWarnings("unchecked")
		ArrayList<ICVendorItem> arrVendorItems 
			= (ArrayList<ICVendorItem>) session.getAttribute(ICEditVendorItemsAction.VENDORITEMARRAY);
		//System.out.println("SIZE of arrVendorItems = " + arrVendorItems.size());

		//Keep a list of the vendors that already have vendor item records:
		ArrayList<String>sCurrentVendors = new ArrayList<String>(0);
		
		//If there is no arraylist object containing vendor item records, that means that EITHER:
		// 1) This is the first time we've come into this class
		// OR
		// 2) We are returning from a successful 'save'
		// In either case, we need to read the records fresh from the database.
		if (arrVendorItems == null){
			try{
				printRecordsFromData(sItem, sConf, sUser, sEditingSingleVendor, sCurrentVendors, pwOut);
			}catch (SQLException e){
				pwOut.println ("Error reading vendor items - " + e.getMessage());
			}
		}else{
			printRecordsFromSessionObject(sCurrentVendors, arrVendorItems, sEditingSingleVendor, pwOut, sConf, sUser, sUserID,sUserFullName);
			session.removeAttribute(ICEditVendorItemsAction.VENDORITEMARRAY);
		}
		
		//Finish the table:
	    pwOut.println("</TABLE>");
	    
		//Store the number of current records:
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + NUMBEROFCURRENTRECORDS_PARAMETER + "' VALUE='" 
			+ Integer.toString(sCurrentVendors.size()) + "'>");
	    
		pwOut.println("<B>Note:</B> To remove a vendor item, clear all the fields for that line and then update.");
		
	    //Finish the form:
	    pwOut.println("<P><INPUT TYPE=SUBMIT NAME='" + EDITVENDORITEM_SUBMIT_BUTTON + "'"
	    	+ " VALUE='" + EDITVENDORITEM_SUBMIT_LABEL + "' STYLE='height: 0.24in'></P>");
	    pwOut.println("</FORM>");
	    
	}
	private void printRecordsFromData(
			String sItem,
			String sDBID, 
			String sUserName,
			String sEditingSingleVendor,
			ArrayList<String> arrVendorsAlreadyListed,
			PrintWriter pwOut
		) throws SQLException{
		
		String SQL = "SELECT"
			+ " " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sCost
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname
	    	+ " FROM " + SMTableicvendoritems.TableName + ", " + SMTableicvendors.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber 
	    			+ " = '" + sItem + "')"
	    		+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + " = " 
	    		+ SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + ")"
	    	+ ")"
	    	+ " ORDER BY " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor
	    	;
	    //System.out.println(SQL);
		int iRowNumber = 0;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sDBID, "MySQL", SMUtilities.getFullClassName(this
							.toString())
							+ ".Edit_Records - user: " + sUserName);
			
			while (rs.next()) {
				iRowNumber++;
				//Create an editable line for each record:
				createRowFromResultSet(rs, sItem, pwOut, iRowNumber);
				arrVendorsAlreadyListed.add(rs.getString(SMTableicvendoritems.TableName + "." 
					+ SMTableicvendoritems.sVendor));
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("Error reading records - " + e.getMessage());
			throw e;
		}
		
		try {
			//The first object in the array contains any NEW vendor item - turn this into a new row:
			createAddRow(
				sItem, 
				pwOut,
				sEditingSingleVendor, 
				"", 
				"", 
				"",
				arrVendorsAlreadyListed
			);
		} catch (SQLException e) {
			pwOut.println("<BR>Error adding new row - " + e.getMessage());
		}
	}
	private void printRecordsFromSessionObject(
			ArrayList<String> arrVendorsAlreadyListed,
			ArrayList<ICVendorItem> arrVendorItems,
			String sEditingSingleVendor,
			PrintWriter pwOut,
			String sDBID,
			String sUserName,
			String sUserID,
			String sUserFullName
		){
		
		int iRowNumber = 0;
		
		for (int i = 1; i < arrVendorItems.size(); i++) {
			iRowNumber++;
			
			String sVendorName = "";
			APVendor ven = new APVendor();
			ven.setsvendoracct(arrVendorItems.get(i).getsvendor());
			if (ven.load(getServletContext(), sDBID, sUserID, sUserFullName)){
				sVendorName = ven.getsname();
			}
			//Create an editable line for each record:
			printRow(
				arrVendorItems.get(i).getsvendor(), 
				arrVendorItems.get(i).getsitemnumber(),
				sVendorName,
				arrVendorItems.get(i).getsvendoritemnumber(),
				arrVendorItems.get(i).getscost(),
				arrVendorItems.get(i).getscomment(),
				pwOut, 
				iRowNumber
			);
			arrVendorsAlreadyListed.add(arrVendorItems.get(i).getsvendor());
		}
		
		String sVendorInNewRow = arrVendorItems.get(0).getsvendor();
		if (sEditingSingleVendor.trim().compareToIgnoreCase("") != 0){
			sVendorInNewRow = sEditingSingleVendor.trim();
		}
		try {
			//The first object in the array contains any NEW vendor item - turn this into a new row:
			createAddRow(
				arrVendorItems.get(0).getsitemnumber(), 
				pwOut,
				sVendorInNewRow, 
				arrVendorItems.get(0).getsvendoritemnumber(), 
				arrVendorItems.get(0).getscost(),
				arrVendorItems.get(0).getscomment(),
				arrVendorsAlreadyListed
			);
		} catch (SQLException e) {
			pwOut.println("<BR>Error adding new row - " + e.getMessage());
		}
	}
	private void createAddRow (
			String sItem, 
			PrintWriter pwOut,
			String sNewVendor,  //If the user is coming here from a link somewhere JUST to edit the record for one vendor
            					// then this field will contain that vendor's acct.
			String sNewVendorItem,
			String sNewVendorCost,
			String sNewComment,
			ArrayList<String>sAlreadyListedVendors
		) throws SQLException{
		
		pwOut.println("<TR>");
		pwOut.println("<TD>");
		
		pwOut.println("<SELECT NAME = \"" + NEWVENDOR_NAME + "\">");
		if (sNewVendor.compareToIgnoreCase("") == 0){
			pwOut.println("<OPTION selected=yes"
				+ " VALUE=\"" + "" + "\">" 
				+ "*** Select a vendor ***"
		    );
		}else{
			pwOut.println("<OPTION selected=no"
				+ " VALUE=\"" + "" + "\">" 
				+ "*** Select a vendor ***"
		    );
		}
		
	    try{
	        String sSQL = "SELECT"
	        	+ " " + SMTableicvendors.svendoracct
	        	+ ", " + SMTableicvendors.sname
	        	+ " FROM " + SMTableicvendors.TableName
	        	+ " ORDER BY " + SMTableicvendors.svendoracct
	        ;
	        ResultSet rsVendors = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".createAddRow - User: " + sUserName);
	        
	        while (rsVendors.next()){
	        	String sVendor = rsVendors.getString(SMTableicvendors.svendoracct).trim();
	        	if (!sAlreadyListedVendors.contains((String) sVendor)){
	        		if (sNewVendor.compareToIgnoreCase(sVendor) == 0){
	        			pwOut.println("<OPTION selected=yes"
	        				+ " VALUE=\"" + 
	        				sVendor 
	        				+ "\">" 
	        				+ sVendor + " - " + rsVendors.getString(SMTableicvendors.sname)
	        		    );
	        		}else{
	        			pwOut.println("<OPTION"
	        				+ " VALUE=\"" + 
	        				sVendor 
	        				+ "\">" 
	        				+ sVendor + " - " + rsVendors.getString(SMTableicvendors.sname)
	        		    );
	        		}
	        	}
	        }
	        rsVendors.close();
	    }catch(SQLException e){
	    	throw e;
	    }
	    pwOut.println("</SELECT>");
	    pwOut.println("</TD>");

		//Vendor item number:
		pwOut.println("<TD>");
		pwOut.println(
    		"<INPUT TYPE=TEXT NAME=\"" + NEWVENDORITEM_NAME + "\""
    		+ " VALUE=\"" 
    		+ sNewVendorItem.replace("\"", "&quot;") + "\""
    		+ " SIZE=15"
    		+ " MAXLENGTH=" + Integer.toString(SMTableicvendoritems.sVendorItemNumberLength)
    		+ " STYLE=\"height: 0.25in\""
		);
		pwOut.println("</TD>");
		//Cost:
		pwOut.println("<TD>");
		pwOut.println(
    		"<INPUT TYPE=TEXT NAME=\"" + NEWVENDORCOST_NAME + "\""
    		+ " VALUE=\"" 
    		+ sNewVendorCost.replace("\"", "&quot;") + "\""
    		+ " SIZE=8"
    		+ " MAXLENGTH=16"
    		+ " STYLE=\"height: 0.25in\""
		);
		pwOut.println("</TD>");
	    
		//Vendor item comment:
		pwOut.println("<TD>");
		pwOut.println(
	    		"<INPUT TYPE=TEXT NAME=\"" + NEWVENDORITEMCOMMENT_NAME + "\""
	    		+ " VALUE=\"" 
	    		+ sNewComment.replace("\"", "&quot;") + "\""
	    		+ " SIZE=35"
	    		+ " MAXLENGTH=" + Integer.toString(SMTableicvendoritems.sCommentLength)
	    		+ " STYLE=\"height: 0.25in\""
			);
		pwOut.println("</TD>");
		
		pwOut.println("</TR>");

	}
	private void createRowFromResultSet(ResultSet rs, String sItem, PrintWriter pwOut, int iRowNumber) throws SQLException{
		pwOut.println("<TR>");
		try{
			printRow(			
				rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor), 
				sItem, 
				rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname),
				rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber),
				clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicvendoritems.sCostScale, 
						rs.getBigDecimal(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sCost)),
				rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment),
				pwOut, 
				iRowNumber
			);
		} catch (SQLException e) {
			throw e;
		}
		pwOut.println("</TR>");
	}
	private void printRow(
			String sVendor, 
			String sItem, 
			String sVendorName,
			String sVendorItemNumber,
			String sCost,
			String sComment,
			PrintWriter pwOut, 
			int iRowNumber
			) {
		pwOut.println("<TR>");
		String sRowNumber = clsStringFunctions.PadLeft(Integer.toString(iRowNumber), "0", 6);
		pwOut.println("<TD>");
		pwOut.println(sVendor + " - " + sVendorName);
		//Store the vendor in a hidden field here:
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + VENDOR_PARAMETER_MARKER + sRowNumber  
			+ "' VALUE='" + sVendor + "'>");
		pwOut.println("</TD>");
		//Vendor item number:
		pwOut.println("<TD>");
		pwOut.println(
    		"<INPUT TYPE=TEXT NAME=\"" + VENDORITEM_PARAMETER_MARKER + sRowNumber + "\""
    		+ " VALUE=\"" 
    		+ sVendorItemNumber.replace("\"", "&quot;") + "\""
    		+ " SIZE=15"
    		+ " MAXLENGTH=" + Integer.toString(SMTableicvendoritems.sVendorItemNumberLength)
    		+ " STYLE=\"height: 0.25in\""
		);
		pwOut.println();
		pwOut.println("</TD>");
		//Cost:
		pwOut.println("<TD>");
		pwOut.println(
    		"<INPUT TYPE=TEXT NAME=\"" + VENDORCOST_PARAMETER_MARKER + sRowNumber + "\""
    		+ " VALUE=\"" 
    		+ sCost.replace("\"", "&quot;") + "\""
    		+ " SIZE=8"
    		+ " MAXLENGTH=16"
    		+ " STYLE=\"height: 0.25in\""
		);
		pwOut.println("</TD>");
		//Vendor item comment:
		pwOut.println("<TD>");
		pwOut.println(
    		"<INPUT TYPE=TEXT NAME=\"" + VENDORITEMCOMMENT_PARAMETER_MARKER + sRowNumber + "\""
    		+ " VALUE=\"" 
    		+ sComment.replace("\"", "&quot;") + "\""
    		+ " SIZE=35"
    		+ " MAXLENGTH=" + Integer.toString(SMTableicvendoritems.sCommentLength)
    		+ " STYLE=\"height: 0.25in\""
		);

		pwOut.println("</TR>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
