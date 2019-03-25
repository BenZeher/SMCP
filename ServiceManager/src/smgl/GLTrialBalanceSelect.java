package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglaccountsegments;
import SMDataDefinition.SMTableglacctsegmentvalues;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLTrialBalanceSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static String PARAM_REPORT_TYPE = "PARAMREPORTTYPE";
	public static String REPORT_TYPE_BALANCES = "REPORTTYPEBALANCES";
	public static String REPORT_TYPE_NET_CHANGES = "REPORTTYPENETCHANGES";
	public static String REPORT_TYPE_BALANCES_LABEL = "Account balances as of the year/period";
	public static String REPORT_TYPE_NET_CHANGES_LABEL = "Net changes for the period";
	public static String PARAM_FISCAL_PERIOD_SELECTION = "FISCALPERIODSELECTION";
	public static String PARAM_VALUE_DELIMITER = " - ";
	public static String PARAM_DOWNLOAD_TO_HTML = "DOWNLOADTOHTML";
	public static String PARAM_STARTING_ACCOUNT = "StartingAccount";
	public static String PARAM_ENDING_ACCOUNT = "EndingVendor";
	public static String PARAM_PROCESS_FOR_NO_ACTIVITY = "PROCESSFORNOACTIVITY";
	public static String PARAM_STARTING_ACCOUNT_GROUP = "STARTINGACCOUNTGROUP";
	public static String PARAM_ENDING_ACCOUNT_GROUP = "ENDINGACCOUNTGROUP";
	public static String PARAM_STARTING_SEGMENT_BASE = "STARTINGSEGMENTBASE";
	public static String PARAM_ENDING_SEGMENT_BASE = "ENDINGSEGMENTBASE";
	public static String DEFAULT_FIRST_STARTING_VALUE = "";
	public static String DEFAULT_LAST_ENDING_VALUE = "ZZZZZZ";
	
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APAgedPayables)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Trial Balance";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));

		if (clsManageRequestParameters.get_Request_Parameter("Warning", request).compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>WARNING: " + clsManageRequestParameters.get_Request_Parameter("Warning", request) + "</B></FONT><BR>");
		}
		if (clsManageRequestParameters.get_Request_Parameter("Status", request).compareToIgnoreCase("") != 0){
			out.println("<BR><B>NOTE: " + clsManageRequestParameters.get_Request_Parameter("Status", request) + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLTrialBalance)
				+ "\">Summary</A><BR>");

		ArrayList<String> alValues = new ArrayList<String>(0);
		ArrayList<String> alOptions = new ArrayList<String>(0);
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLTrialBalanceAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE border=4>\n");

		//Selection criteria:
		//Balance or net changes
		alValues.add(REPORT_TYPE_BALANCES);
		alValues.add(REPORT_TYPE_NET_CHANGES);
		alOptions.add(REPORT_TYPE_BALANCES_LABEL);
		alOptions.add(REPORT_TYPE_NET_CHANGES_LABEL);
		out.println("  <TR>\n");
		out.println("    <TD ALIGN=RIGHT >"
			+  "<B>Type of report:&nbsp;</B>"
			+ "</TD>\n"
		);
		out.println("    <TD COLSPAN=2>"
			+ ServletUtilities.clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Field(
				PARAM_REPORT_TYPE, 
				alOptions, 
				alValues, 
				REPORT_TYPE_BALANCES
			)
			+ "</TD>\n"
		);
		out.println("    <TD>&nbsp;</TD\n");
		out.println("  </TR>\n");
		
		// Year/period
		//Get a drop down of the available periods:
		alValues.clear();
		alOptions.clear();
		String sSQL = "SELECT DISTINCT"
			+ " CONCAT(CAST(" + SMTablegltransactionlines.ifiscalyear + " AS CHAR), '" 
				+ PARAM_VALUE_DELIMITER 
				+ "', CAST(" + SMTablegltransactionlines.ifiscalperiod + " AS CHAR)) AS FISCALSELECTION"
			+ " FROM " + SMTablegltransactionlines.TableName
			+ " ORDER BY " + SMTablegltransactionlines.ifiscalyear + " DESC, " + SMTablegltransactionlines.ifiscalperiod + " DESC"
		;
		try {
			ResultSet rsFiscalSelections = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting period selections - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsFiscalSelections.next()){
				alValues.add(rsFiscalSelections.getString("FISCALSELECTION"));
				alOptions.add(rsFiscalSelections.getString("FISCALSELECTION"));
			}
			rsFiscalSelections.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1553266235] getting fiscal period selections - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("  <TR>");
		out.println("    <TD ALIGN=RIGHT >"
			+  "<B>Fiscal Period:&nbsp;</B>"
			+ "</TD>"
		);
		out.println("    <TD COLSPAN=2>");
		out.println("&nbsp;<SELECT NAME=\"" + PARAM_FISCAL_PERIOD_SELECTION + "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
		}
		out.println("</SELECT>");
		out.println("</TD>");
		out.println("    <TD>&nbsp;</TD");
		out.println("  </TR>");
		
		// Checkbox to include accounts with no activity
		out.println("  <TR>");
		out.println("    <TD ALIGN=RIGHT >"
			+  "<B>Filter by activity:&nbsp;</B>"
			+ "</TD>"
		);
		out.println("    <TD COLSPAN=2>"
			+ clsCreateHTMLFormFields.TDCheckBox(PARAM_PROCESS_FOR_NO_ACTIVITY, false, "Include accounts with no activity")
			+ "</TD>"
		);
		out.println("    <TD>&nbsp;</TD>");
		out.println("  </TR>");
					
		//Account number range:
		alValues.clear();
		alOptions.clear();
		sSQL = "SELECT "
			+ SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglaccounts.TableName
			+ " ORDER BY " + SMTableglaccounts.sAcctID
		;
		try {
			ResultSet rsGLAccounts = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting account groups - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsGLAccounts.next()){
				alValues.add(rsGLAccounts.getString(SMTableglaccounts.sAcctID));
				alOptions.add(rsGLAccounts.getString(SMTableglaccounts.sAcctID) + PARAM_VALUE_DELIMITER + rsGLAccounts.getString(SMTableglaccounts.sDesc));
			}
			rsGLAccounts.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1553266336] getting GL accounts - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("  <TR>");
		out.println("    <TD ALIGN=RIGHT >"
			+  "<B>Accounts:&nbsp;</B>"
			+ "</TD>"
		);
		// Starting account number
		out.println("    <TD>");
		out.println("Starting with:&nbsp;<SELECT NAME=\"" + PARAM_STARTING_ACCOUNT + "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
		}
		out.println("</SELECT>");
		out.println("</TD>");
		
		//Ending with account
		out.println("    <TD>"
			+  "&nbsp;Ending with:&nbsp;"
			+ "<SELECT NAME=\"" + PARAM_ENDING_ACCOUNT + "\">");
		for (int i=0;i<alValues.size();i++){
			if(i == (alValues.size() - 1)){
				out.println("<OPTION selected=yes VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}else{
				out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}
		}
		out.println("</SELECT>");
		out.println("</TD>");
		out.println("    <TD>&nbsp;</TD");
		out.println("  </TR>");
		
		// Starting with account group
		alValues.clear();
		alOptions.clear();
		sSQL = "SELECT "
			+ SMTableglaccountgroups.lid
			+ ", " + SMTableglaccountgroups.sgroupcode
			+ ", " + SMTableglaccountgroups.sdescription
			+ " FROM " + SMTableglaccountgroups.TableName
			+ " ORDER BY " + SMTableglaccountgroups.sgroupcode
		;
		try {
			ResultSet rsAccountGroups = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting account groups - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsAccountGroups.next()){
				alValues.add(rsAccountGroups.getString(SMTableglaccountgroups.sgroupcode));
				alOptions.add(rsAccountGroups.getString(SMTableglaccountgroups.sgroupcode) + PARAM_VALUE_DELIMITER + rsAccountGroups.getString(SMTableglaccountgroups.sdescription));
			}
			rsAccountGroups.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1553266236] getting GL account groups - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("  <TR>");
		out.println("    <TD ALIGN=RIGHT >"
			+  "<B>Account Groups:&nbsp;</B>"
			+ "</TD>"
		);
		out.println("    <TD>");
		out.println("Starting with:&nbsp;<SELECT NAME=\"" + PARAM_STARTING_ACCOUNT_GROUP + "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
		}
		out.println("</SELECT>");
		out.println("</TD>");
		
		//Ending with account group
		out.println("    <TD>"
			+  "&nbsp;Ending with:&nbsp;"
			+ "<SELECT NAME=\"" + PARAM_ENDING_ACCOUNT_GROUP + "\">");
		for (int i=0;i<alValues.size();i++){
			if(i == (alValues.size() - 1)){
				out.println("<OPTION selected=yes VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}else{
				out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}
		}
		out.println("</SELECT>");
		out.println("</TD>");
		out.println("    <TD>&nbsp;</TD");
		out.println("  </TR>");
		
		//Build a range of segment values for each of the possibly segments:
		ArrayList<String> alAccountSegments = new ArrayList<String>(0);
		ArrayList<String> alAccountSegmentDescriptions = new ArrayList<String>(0);
		//First get the segment names:
		sSQL = "SELECT * FROM " + SMTableglaccountsegments.TableName
			+ " ORDER BY " + SMTableglaccountsegments.sdescription
		;
		try {
			ResultSet rsAccountSegments = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting account segments - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsAccountSegments.next()){
				alAccountSegments.add(Long.toString(rsAccountSegments.getLong(SMTableglaccountsegments.lid)));
				alAccountSegmentDescriptions.add(rsAccountSegments.getString(SMTableglaccountsegments.sdescription).toUpperCase());
			}
			rsAccountSegments.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1553266237] getting GL account segments - " + e1.getMessage() + "</B></FONT><BR>");
		}
		
		//Now Get all the segment values:
		alValues.clear();
		alOptions.clear();
		sSQL = "SELECT * FROM " + SMTableglacctsegmentvalues.TableName
				+ " ORDER BY " + SMTableglacctsegmentvalues.svalue
			;
			try {
				ResultSet rsSegmentValues = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".getting account segment values - User: " + sUserID
					+ " - "
					+ sUserFullName
				);
				while(rsSegmentValues.next()){
					alValues.add(Long.toString(rsSegmentValues.getLong(SMTableglacctsegmentvalues.lsegmentid))
						+ PARAM_VALUE_DELIMITER + Long.toString(rsSegmentValues.getLong(SMTableglacctsegmentvalues.lid))
						+ PARAM_VALUE_DELIMITER + rsSegmentValues.getString(SMTableglacctsegmentvalues.svalue)
					);
					alOptions.add(rsSegmentValues.getString(SMTableglacctsegmentvalues.svalue) + PARAM_VALUE_DELIMITER + rsSegmentValues.getString(SMTableglaccountsegments.sdescription));
				}
				rsSegmentValues.close();
			} catch (Exception e1) {
				out.println("<BR><FONT COLOR=RED><B>Error [1553266238] getting GL account segment values - " + e1.getMessage() + "</B></FONT><BR>");
			}
		
		out.println("  <TR>");
		out.println("    <TD ALIGN=LEFT COLSPAN=4>"
			+  "<B><I><U>Segments&nbsp;</U></I></B>"
			+ "</TD>"
		);
		out.println("  </TR>");
		
		for(int iAccountSegmentIndex = 0; iAccountSegmentIndex < alAccountSegments.size(); iAccountSegmentIndex++){
			out.println("  <TR>");
			out.println("    <TD ALIGN=RIGHT><B>" + alAccountSegmentDescriptions.get(iAccountSegmentIndex).toUpperCase() + ":&nbsp;</B></TD>");
			out.println("    <TD>"
				+ "FROM:&nbsp;"
			);
			//Add START drop down here
			out.println("&nbsp;<SELECT NAME=\"" + PARAM_STARTING_SEGMENT_BASE + alAccountSegments.get(iAccountSegmentIndex) 
				+ PARAM_VALUE_DELIMITER + alAccountSegmentDescriptions.get(iAccountSegmentIndex) + "\">");
			
			//Add the first default value:
			out.println("<OPTION selected=yes VALUE=\"" 
				+ alAccountSegments.get(iAccountSegmentIndex)
				+ PARAM_VALUE_DELIMITER + "0"
				+ PARAM_VALUE_DELIMITER + DEFAULT_FIRST_STARTING_VALUE
				+ "\" >" + DEFAULT_FIRST_STARTING_VALUE
			);
			for (int iSegmentValueIndex=0;iSegmentValueIndex<alValues.size();iSegmentValueIndex++){
				String sSegmentID = alValues.get(iSegmentValueIndex).substring(0, alValues.get(iSegmentValueIndex).indexOf(PARAM_VALUE_DELIMITER));
				if(sSegmentID.compareToIgnoreCase(alAccountSegments.get(iAccountSegmentIndex)) == 0){
					out.println("<OPTION VALUE=\"" + alValues.get(iSegmentValueIndex) + "\"> " + alOptions.get(iSegmentValueIndex));
				}
			}
			out.println("</SELECT></TD>");
			
			out.println("<TD>TO:&nbsp;"
				);
			//Add END drop down here
			out.println("&nbsp;<SELECT NAME=\"" + PARAM_ENDING_SEGMENT_BASE + alAccountSegments.get(iAccountSegmentIndex)
				+ PARAM_VALUE_DELIMITER + alAccountSegmentDescriptions.get(iAccountSegmentIndex)+ "\">");
			for (int iSegmentValueIndex=0;iSegmentValueIndex<alValues.size();iSegmentValueIndex++){
				String sSegmentID = alValues.get(iSegmentValueIndex).substring(0, alValues.get(iSegmentValueIndex).indexOf(PARAM_VALUE_DELIMITER));

				if(sSegmentID.compareToIgnoreCase(alAccountSegments.get(iAccountSegmentIndex)) == 0){
					//We want to know if this is the last value for this segment, so we can 'select' it.
					//If the NEXT value coming up is for a different segment, then we know we're on the last value for this segment:
					String sNextSegmentID = "";
					if (iSegmentValueIndex < alValues.size() - 1){
						sNextSegmentID = alValues.get(iSegmentValueIndex + 1).substring(0, alValues.get(iSegmentValueIndex + 1).indexOf(PARAM_VALUE_DELIMITER));
					}
					out.println("<OPTION VALUE=\"" + alValues.get(iSegmentValueIndex) + "\"> " + alOptions.get(iSegmentValueIndex));
				}
			}
			//Add the last ending default value:
			out.println("<OPTION selected=yes  VALUE=\""
				+ alAccountSegments.get(iAccountSegmentIndex)
				+ PARAM_VALUE_DELIMITER + "0"
				+ PARAM_VALUE_DELIMITER + DEFAULT_LAST_ENDING_VALUE
				+ "\" >" + DEFAULT_LAST_ENDING_VALUE	
			);
			out.println("</SELECT>");
			
			out.println("    <TD>"
					+ "&nbsp;"
				);
			out.println("  </TR>");
		}

		//End the table:
		out.println("</TABLE>\n");
		out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Print----\">");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
