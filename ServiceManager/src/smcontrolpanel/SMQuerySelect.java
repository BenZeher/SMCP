package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMQuerySelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String PARAM_EXPORTOPTIONS = "EXPORTOPTIONS";
	public static String EXPORT_COMMADELIMITED_VALUE = "COMMADELIMITED";
	public static String EXPORT_HTML_VALUE = "HTML";
	public static String EXPORT_NOEXPORT_VALUE = "NOEXPORT";
	public static String EXPORT_COMMADELIMITED_LABEL = "Comma delimited file";
	public static String EXPORT_HTML_LABEL = "HTML (web page) file";
	public static String EXPORT_NOEXPORT_LABEL = "Do not export - display on screen";
	public static String PARAM_QUERYID = "QUERYID";
	public static String PARAM_QUERYTITLE = "QUERYTITLE";
	public static String PARAM_QUERYSTRING = "QUERYSTRING";
	public static String PARAM_RAWQUERYSTRING = "RAWQUERYSTRING";
	public static String PARAM_SYSTEMQUERYID = "SYSTEMQUERYID";
	public static String PARAM_PWFORQUICKLINK = "PWFORQUICKLINK";
	public static String PARAM_FONTSIZE = "FONTSIZE";
	public static String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
	public static String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";
	public static String PARAM_TOTALNUMERICFIELDS = "TOTALNUMERICFIELDS";
	public static String PARAM_SHOWSQLCOMMAND = "SHOWSQLCOMMAND";
	public static String PARAM_HIDEHEADERFOOTER = "HIDEHEADERFOOTER";
	public static String PARAM_HIDECOLUMNLABELS = "HIDECOLUMNLABELS";
	private static String CALLED_CLASS_NAME = "smcontrolpanel.SMQueryParameters";

	private String sCompanyName = "";
	private String sDBID = "";
	private String sUserName = "";
	private String sUserID = "";
	private String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMQuerySelector
		)
		){
			return;
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String title = "SM Query Selector";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMQuerySelector) 
				+ "\">Summary</A><BR><BR>");
		//Link to the data definitions mapping:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayDataDefs?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Display data definitions</A>"
				+ "&nbsp;&nbsp;<A HREF=\"" + "https://sites.google.com/site/airotechservicemanager/sm-query-list" 
				+ "\">List pre-built queries</A>"
				);
		out.println("&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\">List your PRIVATE queries</A>");
		out.println("&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "&" + SMSavedQueriesSelect.SHOW_PUBLIC_QUERIES + "=Y"
			+ "\">List all PUBLIC queries</A>");
		
		//String sQueryTitle = SMUtilities.URLDecode(SMUtilities.get_Request_Parameter(PARAM_QUERYTITLE, request));
	
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(PARAM_QUERYTITLE, request);
		if (sQueryTitle.compareToIgnoreCase("") == 0){
			sQueryTitle = "";
		}
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(PARAM_QUERYSTRING, request);
		if (sQueryString.compareToIgnoreCase("") == 0){
			sQueryString = "";
		}
		
		//If we are handling a request to delete a query:
	    String sDeleteQueryID = clsManageRequestParameters.get_Request_Parameter(SMSavedQueriesSelect.DELETE_QUERY_ID_PARAM, request);
	    if (sDeleteQueryID.compareToIgnoreCase("") != 0){
	    	String sRedirectFromDelete = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMSavedQueriesSelect.SHOW_PUBLIC_QUERIES + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMSavedQueriesSelect.SHOW_PUBLIC_QUERIES, request)
			;
	    	try {
				deleteQuery (sDeleteQueryID, sDBID, sUserID, sUserFullName);
				sRedirectFromDelete += "&Status=Successfully deleted query " + sDeleteQueryID + ".";
			} catch (Exception e) {
				sRedirectFromDelete += "&Warning=Error deleting query " + sDeleteQueryID + " - " + e.getMessage();
			}
	    	response.sendRedirect(sRedirectFromDelete);
	    	return;
	    }
		
		//IF we are returning from a 'saved query' selection, then read the query and fill in the title and query from there:
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(SMSavedQueriesSelect.RADIO_BUTTON_GROUP_NAME, request);
		if (sQueryID.compareToIgnoreCase("") != 0){

			//Get the query ID from the submitting class:
			String SQL = "SELECT"
				+ " " + SMTablesavedqueries.ssql
				+ ", " + SMTablesavedqueries.stitle
				+ " FROM " + SMTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + SMTablesavedqueries.id + " = " + sQueryID + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".reading query - user: " + sUserID
						+ " - "
						+ sUserFullName
						);
				if (rs.next()){
					sQueryTitle = rs.getString(SMTablesavedqueries.stitle);
					sQueryString = rs.getString(SMTablesavedqueries.ssql);
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<B><FONT COLOR=\"RED\">Could not read saved query with SQL: " + SQL + " - " + e.getMessage() + "</FONT></B><BR>");
			}
		}
		
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext())
				+ CALLED_CLASS_NAME + "\" METHOD='POST'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + PARAM_QUERYID + "' VALUE='" + sQueryID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");

		out.println("<TR><TD ALIGN=RIGHT><B>Query title: </B></TD>");
		out.println("<TD>");

		out.println("Query Title "
				+ clsCreateHTMLFormFields.TDTextBox(
						PARAM_QUERYTITLE, 
						sQueryTitle, 
						40, 
						72, 
						""
				) 
		);

		out.println("</TD></TR>");

		//Output as comma-delimited:
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
			PARAM_EXPORTOPTIONS, request).compareToIgnoreCase(EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
			PARAM_EXPORTOPTIONS, request).compareToIgnoreCase(EXPORT_HTML_VALUE) == 0;
		
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Export to file options:<B></TD>");
		out.println("<TD>");
		
		String sChecked = " checked";
		String sCommaDelimitedChecked = "";
		if (bExportAsCommaDelimited){
			sCommaDelimitedChecked = sChecked;
		}
		String sHTMLChecked = "";
		if (bExportAsHTML){
			sHTMLChecked = sChecked;
		}
		String sNoExportChecked = "";
		if (!bExportAsHTML && !bExportAsCommaDelimited){
			sNoExportChecked = sChecked;
		}
		
		out.println("<input type=\"radio\" name=\"" + PARAM_EXPORTOPTIONS + "\" value=\"" 
			+ EXPORT_NOEXPORT_VALUE + "\"" + sNoExportChecked + ">" + EXPORT_NOEXPORT_LABEL + "<br>");
		out.println("<input type=\"radio\" name=\"" + PARAM_EXPORTOPTIONS + "\" value=\"" 
			+ EXPORT_COMMADELIMITED_VALUE + "\"" + sCommaDelimitedChecked + ">" + EXPORT_COMMADELIMITED_LABEL + "<br>");
		out.println("<input type=\"radio\" name=\"" + PARAM_EXPORTOPTIONS + "\" value=\"" 
			+ EXPORT_HTML_VALUE + "\"" + sHTMLChecked + ">" + EXPORT_HTML_LABEL + "<br>");

		out.println("</TD>");
		out.println("</TR>");
		
		//Include border:
		boolean bIncludeBorder = (request.getParameter(PARAM_INCLUDEBORDER) != null);
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Show borders?<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDEBORDER + "\"");
		if (bIncludeBorder){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>Display borders around fields in report");

		out.println("</TD>");
		out.println("</TR>");

		//Alternating row color:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Alternate row colors?<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_ALTERNATEROWCOLORS + "\"");
		if (bIncludeBorder){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>Every other row gets a shaded background");

		out.println("</TD>");
		out.println("</TR>");
		
		//Font sizes:
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(PARAM_FONTSIZE, request);
		if (sFontSize.compareToIgnoreCase("") == 0){
			sFontSize = "small";
		}
		
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Font size?<B></TD>");
		out.println("<TD>");
		out.println("<SELECT NAME = \"" + PARAM_FONTSIZE + "\">");

		String sSelected = "";
		if (sFontSize.compareToIgnoreCase("xx-small") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "xx-small" + "\">" + "XX Small");
		if (sFontSize.compareToIgnoreCase("x-small") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "x-small" + "\">" + "X Small");
		if (sFontSize.compareToIgnoreCase("small") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "small" + "\">" + "Small");
		if (sFontSize.compareToIgnoreCase("medium") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "medium" + "\">" + "Medium");
		if (sFontSize.compareToIgnoreCase("large") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "large" + "\">" + "Large");
		if (sFontSize.compareToIgnoreCase("x-large") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "x-large" + "\">" + "X Large");
		if (sFontSize.compareToIgnoreCase("xx-large") == 0){ sSelected = " selected=yes ";} else {sSelected="";}
		out.println("<OPTION" + sSelected + " VALUE=\"" + "xx-large" + "\">" + "XX Large");
		out.println("</SELECT>");

		out.println("</TD>");
		out.println("</TR>");

		//Total numeric fields?:
		boolean bTotalNumericFields = (request.getParameter(PARAM_TOTALNUMERICFIELDS) != null);
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Total numeric fields?<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_TOTALNUMERICFIELDS + "\" checked");
		if (bTotalNumericFields){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>Any number fields will be totaled at the bottom");

		//Show resulting SQL command?:
		boolean bShowSQLCommand = (request.getParameter(PARAM_SHOWSQLCOMMAND) != null);
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Show the SQL command?<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_SHOWSQLCOMMAND + "\"");
		if (bShowSQLCommand){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>The resulting SQL command will be shown after the results.");
		
		out.println("</TD>");
		out.println("</TR>");
		
		//Hide header and footer
		boolean bHideHeaderFooter = (request.getParameter(PARAM_HIDEHEADERFOOTER) != null);
		boolean bHideColumnLabels = (request.getParameter(PARAM_HIDECOLUMNLABELS) != null);
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Hide header and footer?<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_HIDEHEADERFOOTER + "\"");
		if (bHideHeaderFooter){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>Only the query results will be displayed.");
		
		out.println("<BR>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_HIDECOLUMNLABELS + "\"");
		if (bHideColumnLabels){
			out.println(" CHECKED ");
		}
		out.println("width=0.25>Do not show column labels.");
		
		out.println("</TD>");
		out.println("</TR>");
		
		//Enter password to generate quick link
		out.println("<TR><TD ALIGN=RIGHT><B>Password:</B> (Enter your password to create a quick link to this query.)</TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=PASSWORD NAME=\"" 
			+ PARAM_PWFORQUICKLINK 
			+ "\" VALUE=\"" + "" 
			+ "\" SIZE = " + "40" 
			+ " MAXLENGTH = " + "70" + ">" 
			+ ""
		);
		out.println("</TD></TR>");
		
		out.println("</TABLE>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Submit query----\"><BR>");
		//Multi-line text box here for the query:
		out.println("<B><U>Enter SQL query below:</U></B><BR>");
		out.println("<TEXTAREA NAME=\"" + PARAM_QUERYSTRING + "\""
				+ " rows=\"" + "10" + "\""
				+ " cols=\"" + "120" + "\""
				+ ">" + sQueryString + "</TEXTAREA>"
		);

		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Submit query----\">");
		out.println("</FORM>");

		out.println("<B><U>SYSTEM VARIABLES AVAILABLE FOR USE IN QUERIES:</U></B><BR>");
		out.println("<B>" + SMCustomQuery.LINKBASE_VARIABLE + "</B> - HTTP Link Base (useful for creating hyperlinks)<BR>");
		out.println("<B>" + SMCustomQuery.DATABASE_ID_PARAM_VARIABLE + "</B> - Current database id used for the db parameter in hyperlinks (ie. &db=" + SMCustomQuery.DATABASE_ID_PARAM_VARIABLE+ ")<BR>");
		out.println("<B>" + SMCustomQuery.USER_VARIABLE + "</B> - User name<BR>");
		out.println("To create a user prompt, enclose any query parameters between '" + SMCustomQuery.STARTINGPARAMDELIMITER + "'"
			+ " and '" + SMCustomQuery.ENDINGPARAMDELIMITER + "'.<BR>");
		out.println("To create a prompt with a date picker, define the parameter as : <B>[[" 
			+ SMCustomQuery.DATEPICKER_PARAM_VARIABLE + "{Prompt}{DefaultDate}]]</B><BR>");
		out.println("Date picker example: <B>[[" 
				+ SMCustomQuery.DATEPICKER_PARAM_VARIABLE + "{Enter the starting date:}{1/1/2014}]]</B><BR>");
		out.println("Default dates can be typed or keywords <B>TODAY, FIRSTDAYOFYEAR, FIRSTDAYOFMONTH, LASTDAYOFYEAR, LASTDAYOFMONTH</B> can be used.<BR>");
		out.println("To create a prompt with a drop down list, define the parameter as : <B>[[" 
				+ SMCustomQuery.DROPDOWN_PARAM_VARIABLE + "{Prompt}{'value 1', 'value 2', 'value 3'}{First description, Second description, Third description}]]</B><BR>");
		out.println("Drop down list example:<B>[[" 
				+ SMCustomQuery.DROPDOWN_PARAM_VARIABLE + "{Prompt}{'1','2','3'}{Yellow,Blue,Red}]]</B><BR>");
		out.println("To assign valuse to MySQL variables, enclose the 'SET' commands in a '<B>*SETCOMMANDS*</B>' phrase : <B>[[" 
				+ SMCustomQuery.SETVARIABLECOMMAND + "Set variable string]]</B><BR>");
		out.println("Set variable example:<B>[[" 
				+ SMCustomQuery.SETVARIABLECOMMAND + "SET @rownum = NULL, @item = NULL]]</B><BR>");
		
		out.println("<BR>Refer to the <A HREF=\"" 
			+ "https://sites.google.com/site/airotechservicemanager/technical-resources/creatingsmcpqueries" 
			+ "\">technical note about creating SMCP Queries</A>"
			+ " for examples and details."
		);
		out.println("</BODY></HTML>");
	}
	private void deleteQuery (String sDeleteQueryID, String sDBID, String sUserID, String sUserFullName) throws Exception{
		//Get the title and comment of the query before deleting:
		String sQueryTitle = "";
		String sQueryUser = "";
		String sQueryComment = "";
		String sQueryString = "";
		
		String SQL = "SELECT *"
				+ " FROM " + SMTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + SMTablesavedqueries.id + " = " + sDeleteQueryID + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".reading query - user: " + sUserID + " - " + sUserFullName);
				if (rs.next()){
					sQueryTitle = rs.getString(SMTablesavedqueries.stitle);
					sQueryString = rs.getString(SMTablesavedqueries.ssql);
					sQueryComment = rs.getString(SMTablesavedqueries.scomment);
					sQueryUser = rs.getString(SMTablesavedqueries.suserfullname);
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error reading query to be deleted - " + e.getMessage());
			}
		SQL = "DELETE FROM"
				+ " " + SMTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + SMTablesavedqueries.id + " = " + sDeleteQueryID + ")"
				+ ")"
		;
		try {
			clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ".deleteQuery - user: " + sUserName);
		} catch (Exception e) {
			throw new Exception("Error executing delete - " + e.getMessage());
		}
		//Log the save action:
		
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMQUERYDELETE, 
				"Query ID: '" + sDeleteQueryID + "', Saved by: '" + sQueryUser + "', Title: '" + sQueryTitle + "', Comment = '" + sQueryComment + "'", 
				"Query String =  " + sQueryString,
				"[1376509353]");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
