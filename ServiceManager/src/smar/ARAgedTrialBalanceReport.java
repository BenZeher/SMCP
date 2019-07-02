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
import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARAgedTrialBalanceReport  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String FIND_STARTING_CUSTOMER_BUTTON_NAME = "FINDSTARTINGCUSTOMERBUTTON";
	public static final String FIND_STARTING_CUSTOMER_BUTTON_LABEL = "Find";
	public static final String FIND_ENDING_CUSTOMER_BUTTON_NAME = "FINDENDINGCUSTOMERBUTTON";
	public static final String FIND_ENDING_CUSTOMER_BUTTON_LABEL = "Find";
	public static final String DOWNLOAD_TO_HTML = "DOWNLOADTOHTML";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARAgingReport)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
					+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Aging Report";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));

		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAgingReport)
				+ "\">Summary</A><BR>");

		try {
			ArrayList<String> alValues = new ArrayList<String>(0);
			ArrayList<String> alOptions = new ArrayList<String>(0);
			out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAgedTrialBalanceReportGenerate\">");
			out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
			out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
			out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
			//Starting date will be 1990-01-01
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartYear VALUE=1990>");
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartMonth VALUE=1>");
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartDay VALUE=1>");

			out.println("<TR><TD ALIGN=CENTER WIDTH=100%><TABLE BORDER=0 WIDTH=100%>");

			out.println("<TR><TD ALIGN=LEFT WIDTH=30%><B>Age As Of:</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=35%><B>Cutoff By:</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=35%><B>Cutoff Date:</B></TD>" +
			"</TR>");
			//out.println("<TR><TD ALIGN=LEFT>" + ARUtilities.TDDateSelection("AsOfDate", new Date(System.currentTimeMillis()), "") + "</TD>");

			String sAsOfDate = clsManageRequestParameters.get_Request_Parameter("AsOfDate", request);
			if (sAsOfDate.compareToIgnoreCase("") == 0){
				sAsOfDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
			}
			out.println(
					"<TR><TD ALIGN=LEFT>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + "AsOfDate" + "\""
					+ " VALUE=\"" + sAsOfDate + "\""
					+ " SIZE=12"
					+ " MAXLENGTH=10"
					+ ">"
					+ SMUtilities.getDatePickerString("AsOfDate", getServletContext())
					+ "</TD>");

			alValues.clear(); alOptions.clear();
			alValues.add("0"); alOptions.add("Doc. Date");
			String sDefaultCutoffBy = clsManageRequestParameters.get_Request_Parameter("CutOffBy", request);
			out.println("<TD ALIGN=LEFT>" 
					+ clsCreateHTMLFormFields.TDListBox(
							"CutoffBy", alValues, alOptions, alValues.size(), sDefaultCutoffBy) + "</TD>");
			String sCutOffDate = clsManageRequestParameters.get_Request_Parameter("CutOffDate", request);
			if (sCutOffDate.compareToIgnoreCase("") == 0){
				sCutOffDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
			}
			out.println(
					"<TD ALIGN=LEFT>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + "CutOffDate" + "\""
					+ " VALUE=\"" + sCutOffDate + "\""
					+ " SIZE=12"
					+ " MAXLENGTH=10"
					+ ">"
					+ SMUtilities.getDatePickerString("CutOffDate", getServletContext())
					+ "</TD>");

			out.println("</TR>");

			out.println("<TR><TD ALIGN=LEFT><B>Customer Selection:</B></TD>");

			String sSQL = "";
			String sStartingCustomerNumber = clsManageRequestParameters.get_Request_Parameter("StartingCustomer", request);
			String sEndingCustomerNumber = clsManageRequestParameters.get_Request_Parameter("EndingCustomer", request);
			ResultSet rsCustomers = null;
			//get customer list from database if it's not passed in:
			if (sStartingCustomerNumber.compareToIgnoreCase("") == 0){
				sSQL ="SELECT " 
						+ SMTablearcustomer.sCustomerNumber + ", "
						+ SMTablearcustomer.sCustomerName
						+ " FROM " + SMTablearcustomer.TableName
						+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " ASC LIMIT 1";
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
			}
			if (sEndingCustomerNumber.compareToIgnoreCase("") == 0){
				sSQL =  "SELECT " 
						+ SMTablearcustomer.sCustomerNumber + ", "
						+ SMTablearcustomer.sCustomerName
						+ " FROM " + SMTablearcustomer.TableName
						+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " DESC LIMIT 1";
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
			}
			out.println("<TD WIDTH=30%>" + "<B>Starting with:</B> " 
					+ clsCreateHTMLFormFields.TDTextBox(
							"StartingCustomer", 
							sStartingCustomerNumber, 
							10, 
							SMTablearcustomer.sCustomerNumberLength, 
					"")
					+ "<INPUT TYPE=" + "\"SUBMIT\"" 
					+ " NAME=\"" + FIND_STARTING_CUSTOMER_BUTTON_NAME + "\""
					+ " VALUE=\"" + FIND_STARTING_CUSTOMER_BUTTON_LABEL + "\">"
					+ "</TD>"
			);

			out.println("<TD WIDTH=30%>" + "<B>Ending with:</B> " 
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
			out.println("</TABLE></TD></TR>");

			out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
			out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD ALIGN=LEFT WIDTH=20%><B>Aging Type</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=20%><B>Print Transactions In</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=12%><B>Current</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=12%><B>1st</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=12%><B>2nd</B></TD>" +
					"<TD ALIGN=LEFT WIDTH=12%><B>3rd</B></TD>" +
			"</TR>");
			out.println("<TR>");
			
			//Aging Type:
			String sAgingType = clsManageRequestParameters.get_Request_Parameter("AgingType", request);
			alValues.clear(); alOptions.clear();
			alValues.add("0"); alOptions.add("Regular only (non-retainage)");
			alValues.add("1"); alOptions.add("Retainage only");
			out.println("<TD>" + 
					ARUtilities.Create_Edit_Form_List_Field("AgingType", alValues, sAgingType, alOptions)
					+ "</TD>");

			//Print transactions in
			String sPrintTransactionsIn = clsManageRequestParameters.get_Request_Parameter("PrintTransactionIn", request);
			alValues.clear(); alOptions.clear();
			alValues.add("0"); alOptions.add("Detail");
			alValues.add("1"); alOptions.add("Summary");

			out.println("<TD>" + 
					ARUtilities.Create_Edit_Form_List_Field(
							"PrintTransactionIn", 
							alValues, 
							sPrintTransactionsIn, 
							alOptions)
					+ "</TD>");
			//deadline for current
			String sCurrent = clsManageRequestParameters.get_Request_Parameter("Current", request);
			if (sCurrent.compareToIgnoreCase("") == 0){
				sCurrent = "30";
			}
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("Current", sCurrent, 6, 10, "") + "</TD>");
			//deadline for 1st
			String s1st = clsManageRequestParameters.get_Request_Parameter("1st", request);
			if (s1st.compareToIgnoreCase("") == 0){
				s1st = "60";
			}
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("1st", s1st, 6, 10, "Days") + "</TD>");
			//deadline for 2nd
			String s2nd = clsManageRequestParameters.get_Request_Parameter("2nd", request);
			if (s2nd.compareToIgnoreCase("") == 0){
				s2nd = "90";
			}
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("2nd", s2nd, 6, 10, "Days") + "</TD>");
			//deadline for 3rd
			String s3rd = clsManageRequestParameters.get_Request_Parameter("3rd", request);
			if (s3rd.compareToIgnoreCase("") == 0){
				s3rd = "120";
			}
			out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("3rd", s3rd, 6, 10, "Days") + "</TD>");
			//dead debts - always equals to 3rd
			//out.println("<TD WIDTH=11%>" + ARUtilities.TDTextBox("Over", "120", 10, 10, "Where?") + "</TD>");
			out.println("</TR>");
			out.println("</TABLE>");
			out.println("</TD></TR>");

			//Get the account sets:
			//Account sets:
			out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
			out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR><TD ALIGN=LEFT WIDTH=100%>" 
					+ "<B>For Control Account Set:&nbsp;");

			String sAccountSet = clsManageRequestParameters.get_Request_Parameter(ARAccountSet.ParamsAcctSetCode, request);
			try{
				String SQL = "SELECT * FROM " + SMTablearacctset.TableName;
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + " - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
						);

				out.println ("<SELECT NAME=\"" + ARAccountSet.ParamsAcctSetCode + "\">" );
				out.println ("<OPTION VALUE=\"" + "" + "\">");
				out.println ("ALL Account Sets");
				while (rs.next()){
					if (rs.getString(SMTablearacctset.sAcctSetCode).compareToIgnoreCase(sAccountSet) == 0){
						out.println ("<OPTION SELECTED=yes VALUE=\"" + rs.getString(SMTablearacctset.sAcctSetCode) + "\">");
					}else{
						out.println ("<OPTION VALUE=\"" + rs.getString(SMTablearacctset.sAcctSetCode) + "\">");
					}
					out.println (rs.getString(SMTablearacctset.sAcctSetCode) + " - " + rs.getString(SMTablearacctset.sDescription));
				}
				rs.close();
				//End the drop down list:
				out.println ("</SELECT>");

			}catch (SQLException e){
				out.println("Error loading account sets - " + e.getMessage());
			}
			out.println ("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

			String sSortBy = clsManageRequestParameters.get_Request_Parameter("SORTBY", request);
			out.println("<B>Sort by:</B>&nbsp;");
			out.println ("<SELECT NAME=\"" + "SORTBY" + "\">" );
			if (sSortBy.compareToIgnoreCase("NAME") == 0){
				out.println ("<OPTION VALUE=\"" + "ACCOUNT" + "\">" + "Account number");
				out.println ("<OPTION SELECTED=yes VALUE=\"" + "NAME" + "\">" + "Account name");
			}else{
				out.println ("<OPTION SELECTED=yes VALUE=\"" + "ACCOUNT" + "\">" + "Account number");
				out.println ("<OPTION VALUE=\"" + "NAME" + "\">" + "Account name");
			}
			out.println ("</SELECT>");
			out.println("</TD>");
			out.println("</TR>");
			out.println("</TABLE></TD></TR>");

			out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
			out.println("<TABLE BORDER=0 WIDTH=100%>");
			
			boolean bDefaultIOCOTCL = false;
			if (clsManageRequestParameters.get_Request_Parameter("IOCOTCL",request).compareToIgnoreCase("Y") == 0){
				bDefaultIOCOTCL = true;
			}
			boolean bDefaultPCWAZB = false;
			if (clsManageRequestParameters.get_Request_Parameter("PCWAZB",request).compareToIgnoreCase("Y") == 0){
				bDefaultPCWAZB = true;
			}
			boolean bDefaultIP = true;
			if (clsManageRequestParameters.get_Request_Parameter("IP",request).compareToIgnoreCase("N") == 0){
				bDefaultIP = false;
			}
			boolean bDefaultIAD = true;
			if (clsManageRequestParameters.get_Request_Parameter("IAD",request).compareToIgnoreCase("N") == 0){
				bDefaultIAD = false;
			}
			boolean bDefaultIPT = false;
			if (clsManageRequestParameters.get_Request_Parameter("IPT",request).compareToIgnoreCase("Y") == 0){
				bDefaultIPT = true;
			}
			out.println("<TR>" +
					"<TD ALIGN=LEFT WIDTH=55%>" + 
					clsCreateHTMLFormFields.TDCheckBox("IOCOTCL", bDefaultIOCOTCL, "Include only customers over their credit limits") + 
					clsCreateHTMLFormFields.TDCheckBox("PCWAZB", bDefaultPCWAZB, "Print customers with a zero balance (CURRENT balance for a regular aging, RETAINAGE balance for a retainage aging)") + 
					clsCreateHTMLFormFields.TDCheckBox("IP", bDefaultIP, "Include prepayments") +  //default to true
					clsCreateHTMLFormFields.TDCheckBox("IAD", bDefaultIAD, "Include applied details") + //default to true
					clsCreateHTMLFormFields.TDCheckBox("IPT", bDefaultIPT, "Include paid transactions") + 
					"</TD>" +
			"</TR>");
			out.println("</TABLE></TD></TR>");

			out.println("<TR><TD ALIGN=CENTER WIDTH=100%>");
			out.println("<TABLE BORDER=0 WIDTH=100%>");
			boolean bDownLoadToHTML = (request.getParameter(DOWNLOAD_TO_HTML) != null);
			out.println("<TR>" +
					"<TD ALIGN=LEFT WIDTH=55%>" + 
					clsCreateHTMLFormFields.TDCheckBox(DOWNLOAD_TO_HTML, bDownLoadToHTML, "Download to HTML file") + 
					"</TD>" +
					"</TR>");
			out.println("</TABLE>");
			
			out.println("</TABLE>");
			out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
			out.println("</FORM>");

		} catch (SQLException ex) {
			// handle any errors
			out.println("<B>Error: " + ex.getMessage() + "</B>");
		}

		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
