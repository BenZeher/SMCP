package smar;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARAutoCreateCallSheetsSelection extends HttpServlet {

	public static final String FIND_STARTING_CUSTOMER_BUTTON_NAME = "FINDSTARTINGCUSTOMERBUTTON";
	public static final String FIND_STARTING_CUSTOMER_BUTTON_LABEL = "Find";
	public static final String FIND_ENDING_CUSTOMER_BUTTON_NAME = "FINDENDINGCUSTOMERBUTTON";
	public static final String FIND_ENDING_CUSTOMER_BUTTON_LABEL = "Find";
	public static final String STARTING_CUSTOMER_FIELD = "StartingCustomer";
	public static final String ENDING_CUSTOMER_FIELD = "EndingCustomer";
	public static final String STARTING_DATE_FIELD = "StartingDate";
	public static final String ENDING_DATE_FIELD = "EndingDate";
	public static final String REFRESHLIST_BUTTON_NAME = "REFRESHLIST";
	public static final String REFRESHLIST_BUTTON_LABEL = "Refresh invoice list";
	public static final String CREATECALLSHEETS_BUTTON_NAME = "CREATECALLSHEETS";
	public static final String CREATECALLSHEETS_BUTTON_LABEL = "Create call sheets";
	public static final String PARAM_RUNREFRESH = "RUNREFRESH";
	public static final String PARAM_SERVICETYPE = "SERVICETYPE";
	public static final String PARAM_INVNUMBER = "INVNUMBER";

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ARAutoCreateCallSheets
		)
		){
			return;
		}

		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		String title = "Auto-create Call Sheets";
		String subtitle = "";

		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}
		if (bMobileView){
			String sHeading = SMUtilities.DOCTYPE
			+ "<HTML>"
			+ "<HEAD>"
			+ "<TITLE>" + title + "</TITLE>"
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY>"
			//+ " BGCOLOR=\"" + "black" + "\">"
			//+ " COLOR=\"" + "white" + "\">"

			+ SMUtilities.setMobileButtonStyle()

			+ "<TABLE BORDER=0><TR><TD VALIGN=BOTTOM><H2>" + title + "</H2></TD>";
			sHeading += "</TR></TABLE>";
			out.println(sHeading);
		}else{
			out.println(SMUtilities.SMCPTitleSubBGColor(
				title,
				subtitle, 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
				sCompanyName)
			);
		}
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</FONT></B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAutoCreateCallSheets) 
				+ "\">Summary</A><BR><BR>");

		out.println ("List all the invoices with balances, but no call sheets for the chosen customers,"
				+ " invoice dates, and order types below:");
		out.println ("<FORM NAME=MAINFORM ACTION =\"" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smar.ARAutoCreateCallSheetsHandler\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE CELLPADDING=1 border=0>");

		String sSQL = "";
		String sStartingCustomerNumber = clsManageRequestParameters.get_Request_Parameter(STARTING_CUSTOMER_FIELD, request);
		String sEndingCustomerNumber = clsManageRequestParameters.get_Request_Parameter(ENDING_CUSTOMER_FIELD, request);
		ResultSet rsCustomers = null;
		//get customer list from database if it's not passed in:
		if (sStartingCustomerNumber.compareToIgnoreCase("") == 0){
			sSQL =  "SELECT " 
					+ SMTablearcustomer.sCustomerNumber + ", "
					+ SMTablearcustomer.sCustomerName
					+ " FROM " + SMTablearcustomer.TableName
					+ " ORDER BY " + SMTablearcustomer.sCustomerNumber+ " ASC LIMIT 1";
			try {
				rsCustomers = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (1) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);

				if (rsCustomers.next()){
					sStartingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
				}
				rsCustomers.close();
			} catch (SQLException e) {
				out.println("ERROR reading customer list with SQL: " + sSQL + " - " + e.getMessage());
			}
		}
		if (sEndingCustomerNumber.compareToIgnoreCase("") == 0){
			sSQL =  "SELECT " 
					+ SMTablearcustomer.sCustomerNumber + ", "
					+ SMTablearcustomer.sCustomerName
					+ " FROM " + SMTablearcustomer.TableName
					+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " DESC LIMIT 1";
			try {
				rsCustomers = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (2) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);
				if (rsCustomers.next()){
					sEndingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
				}
				rsCustomers.close();
			} catch (SQLException e) {
				out.println("ERROR reading customer list with SQL: " + sSQL + " - " + e.getMessage());
			}
		}

		out.println("<TR>");
		out.println("<TD>" + "Starting with:&nbsp; " 
				+ clsCreateHTMLFormFields.TDTextBox(
						"StartingCustomer", 
						sStartingCustomerNumber, 
						10, 
						SMTablearcustomer.sCustomerNumberLength, 
				"")
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_STARTING_CUSTOMER_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_STARTING_CUSTOMER_BUTTON_LABEL + "\">"
		);

		out.println("Ending with:&nbsp; " 
				+ clsCreateHTMLFormFields.TDTextBox(
						"EndingCustomer", 
						sEndingCustomerNumber, 
						10, 
						SMTablearcustomer.sCustomerNumberLength,
				"") 
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_ENDING_CUSTOMER_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_ENDING_CUSTOMER_BUTTON_LABEL + "\">"
				+ "</TD>");

		//
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter(STARTING_DATE_FIELD, request);
		if (sStartingDate.compareToIgnoreCase("") == 0){
			sStartingDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter(ENDING_DATE_FIELD, request);
		if (sEndingDate.compareToIgnoreCase("") == 0){
			sEndingDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}
		//Calendar c = Calendar.getInstance();
		//set start time to the first month of current month
		//c.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(System.currentTimeMillis()));
		out.println("<TD>"
				+ "With invoice dates from:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_DATE_FIELD, 
						sStartingDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(STARTING_DATE_FIELD, getServletContext())
		);
		//out.println("</TR>");

		//c.setTimeInMillis(SMUtilities.FindLastDayOfMonth(System.currentTimeMillis()));
		out.println("to:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						ENDING_DATE_FIELD, 
						clsDateAndTimeConversions.now("M/d/yyyy"), 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(ENDING_DATE_FIELD, getServletContext())
				+ "</TD>");
		out.println("</TR>");

		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD COLSPAN=2>Include order types:&nbsp;");

		//Create an array of order types:
		ArrayList<String>arrOrderTypes = new ArrayList<String>(0);
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC" ;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				String sChecked = "";
				if (request.getParameter(PARAM_SERVICETYPE + rs.getString(SMTableservicetypes.sCode)) != null){
					sChecked = "checked=\"yes\" ";
					//Add it to the array of order types:
					arrOrderTypes.add(rs.getString(SMTableservicetypes.sCode));
				}
				out.println(
						"<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + PARAM_SERVICETYPE
						+ rs.getString(SMTableservicetypes.sCode) + "\" " + sChecked + "width=0.25>" 
						+ rs.getString(SMTableservicetypes.sName) + "&nbsp;</LABEL>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read service types table - " + e.getMessage());
		}

		out.println("</TD>");
		out.println("</TR>");

		out.println("</TABLE>");
		out.println("<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + REFRESHLIST_BUTTON_NAME + "\""
				+ " VALUE=\"" + REFRESHLIST_BUTTON_LABEL + "\">"
		);

		out.println("<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + CREATECALLSHEETS_BUTTON_NAME + "\""
				+ " VALUE=\"" + CREATECALLSHEETS_BUTTON_LABEL + "\">"
		);

		if (request.getParameter(PARAM_RUNREFRESH) != null){
			createList(
					sStartingCustomerNumber,
					sEndingCustomerNumber,
					sStartingDate,
					sEndingDate,
					arrOrderTypes,
					sDBID,
					sUserID,
					sUserFullName,
					out);
		}
		out.println("</FORM>");

		//Set the default focus:
		out.println("</BODY></HTML>");
	}
	private void createList(
			String sStartingCustomer,
			String sEndingCustomer,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> sOrderTypes,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter pwOut){

		String sStartDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate);
		String sEndDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate);
		//POSSIBLEJOINOPTIMIZATION
		String SQL = "SELECT"
			+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sOrderNumber
			+ " FROM " + SMTableartransactions.TableName + " LEFT JOIN " + SMTableinvoiceheaders.TableName
			+ " ON ("
			+ "(LPAD(" + SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + ",8,' ') = "
			+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + ")"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + " = "
			+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode + ")"

			+ ")"
			+ " LEFT JOIN " + SMTablecallsheets.TableName + " ON "
			+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber + " = "
			+ SMTablecallsheets.TableName + "." + SMTablecallsheets.sOrderNumber
			+ " WHERE ("
			+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode 
			+ " >= '" + sStartingCustomer + "')"
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode 
			+ " <= '" + sEndingCustomer + "')"
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate 
			+ " >= '" + sStartDate + "')"
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate 
			+ " <= '" + sEndDate + "')"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt + " > 0.00)"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype 
			+ " = " + ARDocumentTypes.INVOICE_STRING + ")"
			;

		SQL += " AND (";
		for (int i = 0; i < sOrderTypes.size(); i++){
			if (i > 0){
				SQL += " OR ";
			}
			SQL += "(" + SMTableinvoiceheaders.TableName + "." 
			+ SMTableinvoiceheaders.sServiceTypeCode + " = '" 
			+ sOrderTypes.get(i) + "')";
		}
		SQL += ")";

		//TODO - no call sheet

		SQL += ")"
			+ " ORDER BY " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
			;

		//System.out.println(SQL);

		pwOut.println("<TABLE with=100% border=0 cellpadding=1 style = \"font-size: small;\" >");
		//Print the headings:
		pwOut.println(
			"<TR>"
			+ "<TD VALIGN=BOTTOM><U><B>Create?<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Account<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Inv #<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Inv Date<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Order #<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B><a href=\"#CallSheetExists\">Call sheet<BR>exists?*</a><B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Bill to<B></U></TD>"
			+ "<TD VALIGN=BOTTOM><U><B>Ship to<B></U></TD>"
			+ "</TR>"
		);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".createList - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
			);
			while (rs.next()){
				pwOut.println("<TR>");

				//PARAM_INVNUMBER
				String sInvNumber = rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sInvoiceNumber).trim(); 
				pwOut.println("<TD>"
						+ "<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INVNUMBER
						+ sInvNumber + "\" width=0.25>"
						+ "</TD>"
				);
				pwOut.println(
						"<TD>" + rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sCustomerCode) 
						+ "</TD>"
				);
				pwOut.println(
						"<TD>" + sInvNumber + "</TD>"
				);
				pwOut.println(
						"<TD>" + clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(
						SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.datInvoiceDate))
						+ "</TD>"
				);
				pwOut.println(
						"<TD>" + rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sOrderNumber) 
						+ "</TD>"
				);
				String sCallSheetExists = "N";
				if (rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sOrderNumber) != null){
					 sCallSheetExists = "<B><FONT COLOR=RED>Y</FONT></B>";
				}
				pwOut.println(
						"<TD>" + sCallSheetExists + "</TD>"
				);
				pwOut.println(
						"<TD>" + rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sBillToName) 
						+ "</TD>"
				);
				pwOut.println(
						"<TD>" + rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sShipToName) 
						+ "</TD>"
				);

				pwOut.println("</TR>");
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("<BR><B>Error reading invoice list - " + e.getMessage());
			return;
		}

		pwOut.println("</TABLE>");
		
		pwOut.println("<a name=\"CallSheetExists\"><B>* 'Call sheet exists'</B> - If there is already a call sheet for this ORDER NUMBER,"
				+ " this is indicated with a 'Y', otherwise this is 'N'.");
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}