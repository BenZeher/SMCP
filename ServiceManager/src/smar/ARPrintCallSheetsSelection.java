package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

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
import SMDataDefinition.SMTablecallsheets;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARPrintCallSheetsSelection extends HttpServlet {

	public static String FIND_ID_BUTTON_NAME = "FINDCALLSHEETID";
	public static String FIND_ID_BUTTON_LABEL = "Find";
	public static String FIND_STARTING_CUSTOMER_BUTTON_NAME = "FINDSTARTINGCUSTOMERBUTTON";
	public static String FIND_STARTING_CUSTOMER_BUTTON_LABEL = "Find";
	public static String FIND_ENDING_CUSTOMER_BUTTON_NAME = "FINDENDINGCUSTOMERBUTTON";
	public static String FIND_ENDING_CUSTOMER_BUTTON_LABEL = "Find";
	public static String CALLSHEET_ID_FIELD = "StartingID";
	public static String STARTING_CUSTOMER_FIELD = "StartingCustomer";
	public static String ENDING_CUSTOMER_FIELD = "EndingCustomer";
	public static String STARTING_LAST_CONTACT_DATE_FIELD = "StartingLastContactDate";
	public static String ENDING_LAST_CONTACT_DATE_FIELD = "EndingLastContactDate";
	public static String STARTING_NEXT_CONTACT_DATE_FIELD = "StartingNextContactDate";
	public static String ENDING_NEXT_CONTACT_DATE_FIELD = "EndingNextContactDate";
	public static String ORDERNUMBER_FIELD = "OrderNumber";
	public static String COLLECTOR_FIELD = "Collector";
	public static String RESPONSIBILITY_FIELD = "Responsibility";
	public static String SALESPERSON_FULLNAME = "SalespersonFullName";
	
	public static String PRINTWITHNOTES_FIELD = "PrintWithNotes";
	public static String PRINTWITHNOTES_LABEL = "Print with notes";
	public static String PRINTONLYALERTS_FIELD = "PrintOnlyAlerts";
	public static String PRINTONLYALERTS_LABEL = "ONLY print call sheets with 'Alerts'";
	public static String PRINTZEROBALANCECUSTOMERS_FIELD = "PrintZeroBalanceCustomers";
	public static String PRINTZEROBALANCECUSTOMERS_LABEL = "Print call sheets for customers with zero balance";
	public static String PRINTWITHRESPONSIBILITYONLY_FIELD = "PrintWithResonsibilityOnly";
	public static String PRINTWITHRESPONSIBILITYONLY_LABEL = "Only print call sheets with responsibility assigned";
	public static String PRINT_BUTTON_NAME = "PRINTCALLSHEETS";
	public static String PRINT_BUTTON_LABEL = "Print call sheets";
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ARPrintCallSheets
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

		String title = "Print Call Sheets";
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
			out.println("<B>>STATUS: " + sStatus + "</FONT></B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
		
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARPrintCallSheets) 
				+ "\">Summary</A><BR><BR>");

		out.println ("Print call sheets:");
		out.println ("<FORM NAME=MAINFORM ACTION =\"" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smar.ARPrintCallSheetsGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE CELLPADDING=1 border=0>");

		String sSQL = "";
		String sCallSheetID = clsManageRequestParameters.get_Request_Parameter(CALLSHEET_ID_FIELD, request);
		String sStartingCustomerNumber = clsManageRequestParameters.get_Request_Parameter(STARTING_CUSTOMER_FIELD, request);
		String sEndingCustomerNumber = clsManageRequestParameters.get_Request_Parameter(ENDING_CUSTOMER_FIELD, request);
		String sStartingLastContactDate = clsManageRequestParameters.get_Request_Parameter(STARTING_LAST_CONTACT_DATE_FIELD, request);
		String sEndingLastContactDate = clsManageRequestParameters.get_Request_Parameter(ENDING_LAST_CONTACT_DATE_FIELD, request);
		String sStartingNextContactDate = clsManageRequestParameters.get_Request_Parameter(STARTING_NEXT_CONTACT_DATE_FIELD, request);
		String sEndingNextContactDate = clsManageRequestParameters.get_Request_Parameter(ENDING_NEXT_CONTACT_DATE_FIELD, request);
		String sCollector = clsManageRequestParameters.get_Request_Parameter(COLLECTOR_FIELD, request);
		String sResponsibility = clsManageRequestParameters.get_Request_Parameter(RESPONSIBILITY_FIELD, request);

		boolean bPrintWithNotes = request.getParameter(PRINTWITHNOTES_FIELD) != null;
		boolean bOnlyPrintAlerts = request.getParameter(PRINTONLYALERTS_FIELD) != null;
		boolean bPrintZeroBalanceCustomers = request.getParameter(PRINTZEROBALANCECUSTOMERS_FIELD) != null;
		boolean bPrintWithResponsibilityOnly = request.getParameter(PRINTWITHRESPONSIBILITYONLY_FIELD) != null;
		
		out.println("<TR>");
		out.println("<TD>" + "Call Sheet ID:&nbsp; " 
				+ clsCreateHTMLFormFields.TDTextBox(
						CALLSHEET_ID_FIELD, 
						sCallSheetID, 
						10, 
						10, 
				"")
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_ID_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_ID_BUTTON_LABEL + "\">"
				+ "</TD>"
		);

		out.println("<TD>" 
				+ "(If you need one particular Call Sheet, enter the ID, otherwise, leave this blank.)" 
				+ "</TD>");

		out.println("</TR>");
		
		ResultSet rs = null;
		//get customer list from database if it's not passed in:
		if (sStartingCustomerNumber.compareToIgnoreCase("") == 0){
			sSQL =  "SELECT " 
					+ SMTablearcustomer.sCustomerNumber + ", "
					+ SMTablearcustomer.sCustomerName
					+ " FROM " + SMTablearcustomer.TableName
					+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " ASC LIMIT 1";
			try {
				rs = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (1) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);

				if (rs.next()){
					sStartingCustomerNumber = rs.getString(SMTablearcustomer.sCustomerNumber);
				}
				rs.close();
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
				rs = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (2) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);
				if (rs.next()){
					sEndingCustomerNumber = rs.getString(SMTablearcustomer.sCustomerNumber);
				}
				rs.close();
			} catch (SQLException e) {
				out.println("ERROR reading customer list with SQL: " + sSQL + " - " + e.getMessage());
			}
		}

		out.println("<TR>");
		out.println("<TD>" + "From customer:&nbsp; " 
				+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_CUSTOMER_FIELD, 
						sStartingCustomerNumber, 
						10, 
						SMTablearcustomer.sCustomerNumberLength, 
				"")
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_STARTING_CUSTOMER_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_STARTING_CUSTOMER_BUTTON_LABEL + "\">"
				+ "</TD>"
		);

		out.println("<TD>" + "To:&nbsp; " 
				+ clsCreateHTMLFormFields.TDTextBox(
						ENDING_CUSTOMER_FIELD, 
						sEndingCustomerNumber, 
						10, 
						SMTablearcustomer.sCustomerNumberLength,
				"") 
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_ENDING_CUSTOMER_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_ENDING_CUSTOMER_BUTTON_LABEL + "\">"
				+ "</TD>");

		out.println("</TR>");
		out.println("<TR>");
		
		//Collectors:
		ArrayList<String> arrCollectors = new ArrayList<String>(0);
		ArrayList<String> arrCollectorDescs = new ArrayList<String>(0);
		arrCollectors.add("");
		arrCollectorDescs.add("** Select ALL Collectors **");
		if (sCollector.compareToIgnoreCase("") == 0){
			sSQL = "SELECT DISTINCT " 
				+ SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollector
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollectorFullName
			+ " FROM " + SMTablecallsheets.TableName 
			+ " ORDER BY " 
			+ SMTablecallsheets.sCollector + " ASC";
			try {
				rs = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (1) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);

				while (rs.next()){
					arrCollectors.add(rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollector));
					String sFullName = "(NOT FOUND)";
					if (rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollectorFullName) != null) {
						if (rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollectorFullName).compareToIgnoreCase("") != 0) {
							sFullName = rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollectorFullName);
						}
					}
					arrCollectorDescs.add(
						rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sCollector) + " - " + sFullName
					);
				}
				rs.close();
			} catch (SQLException e) {
				out.println("ERROR [1586806066] reading collector with SQL: '" + sSQL + "' - " + e.getMessage());
			}
		}

		out.println("<TR>");
		out.println("<TD>" + "For collector:&nbsp; "
			+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
				COLLECTOR_FIELD, arrCollectors, sCollector, arrCollectorDescs)
			+ "</TD>"
		);

		//Responsible:
		ArrayList<String> arrResponsibility = new ArrayList<String>(0);
		ArrayList<String> arrResponsibilityDescs = new ArrayList<String>(0);
		arrResponsibility.add("");
		arrResponsibilityDescs.add("** Select ALL Responsible Persons **");
		if (sCollector.compareToIgnoreCase("") == 0){
			sSQL = "SELECT DISTINCT " 
				+ SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibility
			+ ", " + SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibilityFullName
			+ " FROM " + SMTablecallsheets.TableName 
			+ " ORDER BY " 
			+ SMTablecallsheets.sResponsibility + " ASC";
			try {
				rs = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						sDBID,
						"MySQL",
						this.toString() + ".doPost (1) - User: " + sUserID
						+ " - "
						+ sUserFullName
						);

				while (rs.next()){
					arrResponsibility.add(rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibility));
					String sFullName = "(NOT FOUND)";
					if (rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibilityFullName) != null) {
						if (rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibilityFullName).compareToIgnoreCase("") != 0) {
							sFullName = rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibilityFullName);
						}
					}
					arrResponsibilityDescs.add(
						rs.getString(SMTablecallsheets.TableName + "." + SMTablecallsheets.sResponsibility) + " - " + sFullName
					);
				}
				rs.close();
			} catch (SQLException e) {
				out.println("ERROR [1586806067] reading responsibility with SQL: '" + sSQL + "' - " + e.getMessage());
			}
		}

		out.println("<TR>");
		out.println("<TD>" + "For responsible person:&nbsp; "
			+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
				RESPONSIBILITY_FIELD, arrResponsibility, sResponsibility, arrResponsibilityDescs)
			+ "</TD>"
		);

		out.println("</TR>");

		//Last contact dates:
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(System.currentTimeMillis());
    	c.add(Calendar.YEAR, -10);
		if (sStartingLastContactDate.compareToIgnoreCase("") == 0){
			sStartingLastContactDate = clsDateAndTimeConversions.CalendarToString(c, "M/d/yyyy");
		}
		if (sEndingLastContactDate.compareToIgnoreCase("") == 0){
			sEndingLastContactDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}
		out.println("<TR>");
		out.println("<TD>"
				+ "With LAST contact date from:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_LAST_CONTACT_DATE_FIELD, 
						sStartingLastContactDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(STARTING_LAST_CONTACT_DATE_FIELD, getServletContext())
				+ "</TD>"
		);
		out.println("<TD>" + "To:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						ENDING_LAST_CONTACT_DATE_FIELD, 
						sEndingLastContactDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(ENDING_LAST_CONTACT_DATE_FIELD, getServletContext())
				+ "</TD>");
		out.println("</TR>");

		//Next contact dates:
		if (sStartingNextContactDate.compareToIgnoreCase("") == 0){
			sStartingNextContactDate = clsDateAndTimeConversions.CalendarToString(c, "M/d/yyyy");
		}
		if (sEndingNextContactDate.compareToIgnoreCase("") == 0){
			sEndingNextContactDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}
		out.println("<TR>");
		out.println("<TD>"
				+ "With NEXT contact date from:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_NEXT_CONTACT_DATE_FIELD, 
						sStartingNextContactDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(STARTING_NEXT_CONTACT_DATE_FIELD, getServletContext())
				+ "</TD>"
		);
		out.println("<TD>" + "To:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						ENDING_NEXT_CONTACT_DATE_FIELD, 
						sEndingNextContactDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(ENDING_NEXT_CONTACT_DATE_FIELD, getServletContext())
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR><TD>" + PRINTWITHNOTES_LABEL + "<INPUT TYPE=CHECKBOX");
		if (bPrintWithNotes){out.println(" checked=\"yes\" ");}
		out.println(" NAME=\"" + PRINTWITHNOTES_FIELD + "\" width=0.25></TD></TR>"); 

		out.println("<TR><TD>" + PRINTONLYALERTS_LABEL + "<INPUT TYPE=CHECKBOX");
		if (bOnlyPrintAlerts){out.println(" checked=\"yes\" ");}
		out.println(" NAME=\"" + PRINTONLYALERTS_FIELD + "\" width=0.25></TD></TR>"); 
		
		out.println("<TR><TD>" + PRINTZEROBALANCECUSTOMERS_LABEL + "<INPUT TYPE=CHECKBOX");
		if (bPrintZeroBalanceCustomers){out.println(" checked=\"yes\" ");}
		out.println(" NAME=\"" + PRINTZEROBALANCECUSTOMERS_FIELD + "\" width=0.25></TD></TR>");
		
		out.println("<TR><TD>" + PRINTWITHRESPONSIBILITYONLY_LABEL + "<INPUT TYPE=CHECKBOX");
		if (bPrintWithResponsibilityOnly){out.println(" checked=\"yes\" ");}
		out.println(" NAME=\"" + PRINTWITHRESPONSIBILITYONLY_FIELD + "\" width=0.25></TD></TR>");
		
		out.println("</TABLE>");
		out.println("<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + PRINT_BUTTON_NAME + "\""
				+ " VALUE=\"" + PRINT_BUTTON_LABEL + "\">"
		);

		out.println("</FORM>");
		
		//Set the default focus:
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}