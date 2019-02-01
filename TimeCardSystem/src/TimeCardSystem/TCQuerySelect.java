package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablecompanyprofile;
import TCSDataDefinition.TCTablesavedqueries;

public class TCQuerySelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String PARAM_EXPORTOPTIONS = "EXPORTOPTIONS";
	public static final String EXPORT_COMMADELIMITED_VALUE = "COMMADELIMITED";
	public static final String EXPORT_HTML_VALUE = "HTML";
	public static final String EXPORT_NOEXPORT_VALUE = "NOEXPORT";
	public static final String EXPORT_COMMADELIMITED_LABEL = "Comma delimited file";
	public static final String EXPORT_HTML_LABEL = "HTML (web page) file";
	public static final String EXPORT_NOEXPORT_LABEL = "Do not export - display on screen";
	public static final String PARAM_QUERYID = "QUERYID";
	public static final String PARAM_QUERYTITLE = "QUERYTITLE";
	public static final String PARAM_QUERYSTRING = "QUERYSTRING";
	public static final String PARAM_RAWQUERYSTRING = "RAWQUERYSTRING";
	public static final String PARAM_SYSTEMQUERYID = "SYSTEMQUERYID";
	public static final String PARAM_PWFORQUICKLINK = "PWFORQUICKLINK";
	public static final String PARAM_FONTSIZE = "FONTSIZE";
	public static final String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
	public static final String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";
	public static final String PARAM_TOTALNUMERICFIELDS = "TOTALNUMERICFIELDS";
	public static final String PARAM_SHOWSQLCOMMAND = "SHOWSQLCOMMAND";
	public static final String PARAM_HIDEHEADERFOOTER = "HIDEHEADERFOOTER";
	public static final String PARAM_HIDECOLUMNLABELS = "HIDECOLUMNLABELS";
	private static final String CALLED_CLASS_NAME = "TimeCardSystem.TCQueryParameters";

	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = "";
		try {
			sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDBID = "";
		}
		if (sDBID == null){
			sDBID = "";
		}
		
		if (sDBID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No database name found in session.</FONT></B><BR>");
			return;
		}
		
		String sUserID = "";
		try {
			sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		} catch (Exception e1) {
			sUserID = "";
		}
		if (sUserID == null){
			sUserID = "";
		}
		
		if (sUserID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No employee ID found in session.</FONT></B><BR>");
			return;
		}
		
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, rs.getString(TCSTablecompanyprofile.sCompanyName));
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sTitle = "Time Card System";
		String sSubtitle = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"BLACK\">STATUS: " + sStatus + "</FONT></B><BR>");
		}

    	out.println("<TD><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain?" 
			+ TimeCardUtilities.SESSION_ATTRIBUTE_DB + "=" + sDBID 
			+ "&" + TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER + " = " + sUserID
			+ "\">Return to main admin menu</A><BR>");

		out.println("<BR>");
		
		//Link to the data definitions mapping:
		out.println("<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) 
			+ "TimeCardSystem.TCDisplayDataDefs" + "\">Display data definitions</A>"
		);
		
		out.println("&nbsp;&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect" 
			+ "\">List your PRIVATE queries</A>");
		
		out.println("&nbsp;&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ "&" + TCSavedQueriesSelect.SHOW_PUBLIC_QUERIES + "=Y"
			+ "\">List all PUBLIC queries</A>");
		
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(PARAM_QUERYTITLE, request);
		if (sQueryTitle.compareToIgnoreCase("") == 0){
			sQueryTitle = "";
		}
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(PARAM_QUERYSTRING, request);
		if (sQueryString.compareToIgnoreCase("") == 0){
			sQueryString = "";
		}
		
		//If we are handling a request to delete a query:
	    String sDeleteQueryID = clsManageRequestParameters.get_Request_Parameter(TCSavedQueriesSelect.DELETE_QUERY_ID_PARAM, request);
	    if (sDeleteQueryID.compareToIgnoreCase("") != 0){
	    	String sRedirectFromDelete = clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect"
					+ "?" + TCSavedQueriesSelect.SHOW_PUBLIC_QUERIES + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(TCSavedQueriesSelect.SHOW_PUBLIC_QUERIES, request)
			;
	    	try {
				deleteQuery (sDeleteQueryID, sDBID, sUserID);
				sRedirectFromDelete += "&Status=Successfully deleted query " + sDeleteQueryID + ".";
			} catch (Exception e) {
				sRedirectFromDelete += "&Warning=Error deleting query " + sDeleteQueryID + " - " + e.getMessage();
			}
	    	response.sendRedirect(response.encodeRedirectURL(sRedirectFromDelete));
	    	return;
	    }
		//IF we are returning from a 'saved query' selection, then read the query and fill in the title and query from there:
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(TCSavedQueriesSelect.RADIO_BUTTON_GROUP_NAME, request);
		if (sQueryID.compareToIgnoreCase("") != 0){

			//Get the query ID from the submitting class:
			String SQL = "SELECT"
				+ " " + TCTablesavedqueries.ssql
				+ ", " + TCTablesavedqueries.stitle
				+ " FROM " + TCTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + TCTablesavedqueries.id + " = " + sQueryID + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".reading query - user: " + sUserID);
				if (rs.next()){
					sQueryTitle = rs.getString(TCTablesavedqueries.stitle);
					sQueryString = rs.getString(TCTablesavedqueries.ssql);
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<B><FONT COLOR=\"RED\">Could not read saved query with SQL: " + SQL + " - " + e.getMessage() + "</FONT></B><BR>");
			}
		}
		out.println ("<FORM ACTION =\"" + TimeCardUtilities.getURLLinkBase(getServletContext())
				+ CALLED_CLASS_NAME + "\" METHOD='POST'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + PARAM_QUERYID + "' VALUE='" + sQueryID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ TimeCardUtilities.getFullClassName(this.toString()) + "\">");
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
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_TOTALNUMERICFIELDS + "\"");
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
		out.println("<B>" + TCCustomQuery.SESSIONTAG_VARIABLE + "</B> - Session Tag (useful for creating hyperlinks)<BR>");
		out.println("<B>" + TCCustomQuery.LINKBASE_VARIABLE + "</B> - HTTP Link Base (useful for creating hyperlinks)<BR>");
		out.println("<B>" + TCCustomQuery.USER_VARIABLE + "</B> - User name<BR>");
		out.println("To create a user prompt, enclose any query parameters between '" + TCCustomQuery.STARTINGPARAMDELIMITER + "'"
			+ " and '" + TCCustomQuery.ENDINGPARAMDELIMITER + "'.<BR>");
		out.println("To create a prompt with a date picker, define the parameter as : <B>[[" 
			+ TCCustomQuery.DATEPICKER_PARAM_VARIABLE + "{Prompt}{DefaultDate}]]</B><BR>");
		out.println("Date picker example: <B>[[" 
				+ TCCustomQuery.DATEPICKER_PARAM_VARIABLE + "{Enter the starting date:}{1/1/2014}]]</B><BR>");
		out.println("Default dates can be typed or keywords <B>TODAY, FIRSTDAYOFYEAR, FIRSTDAYOFMONTH, LASTDAYOFYEAR, LASTDAYOFMONTH</B> can be used.<BR>");
		out.println("To create a prompt with a drop down list, define the parameter as : <B>[[" 
				+ TCCustomQuery.DROPDOWN_PARAM_VARIABLE + "{Prompt}{'value 1', 'value 2', 'value 3'}{First description, Second description, Third description}]]</B><BR>");
		out.println("Drop down list example:<B>[[" 
				+ TCCustomQuery.DROPDOWN_PARAM_VARIABLE + "{Prompt}{'1','2','3'}{Yellow,Blue,Red}]]</B><BR>");
		out.println("To assign valuse to MySQL variables, enclose the 'SET' commands in a '<B>*SETCOMMANDS*</B>' phrase : <B>[[" 
				+ TCCustomQuery.SETVARIABLECOMMAND + "Set variable string]]</B><BR>");
		out.println("Set variable example:<B>[[" 
				+ TCCustomQuery.SETVARIABLECOMMAND + "SET @rownum = NULL, @item = NULL]]</B><BR>");
		out.println("<BR>Refer to the <A HREF=\"" 
			+ "https://sites.google.com/site/airotechservicemanager/technical-resources/creatingsmcpqueries" 
			+ "\">technical note about creating SMCP Queries</A>"
			+ " for examples and details."
		);
		out.println("</BODY></HTML>");
	}
	private void deleteQuery (String sDeleteQueryID, String sConfFile, String sUserName) throws Exception{
		//Get the title and comment of the query before deleting:
		String sQueryTitle = "";
		String sQueryUser = "";
		String sQueryComment = "";
		String sQueryString = "";
		
		String SQL = "SELECT *"
				+ " FROM " + TCTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + TCTablesavedqueries.id + " = " + sDeleteQueryID + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sConfFile, 
						"MySQL", 
						this.toString() + ".reading query - user: " + sUserName);
				if (rs.next()){
					sQueryTitle = rs.getString(TCTablesavedqueries.stitle);
					sQueryString = rs.getString(TCTablesavedqueries.ssql);
					sQueryComment = rs.getString(TCTablesavedqueries.scomment);
					sQueryUser = rs.getString(TCTablesavedqueries.sfirstname) + " " + rs.getString(TCTablesavedqueries.slastname);
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error reading query to be deleted - " + e.getMessage());
			}
		SQL = "DELETE FROM"
				+ " " + TCTablesavedqueries.TableName
				+ " WHERE ("
					+ "(" + TCTablesavedqueries.id + " = " + sDeleteQueryID + ")"
				+ ")"
		;
		try {
			clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sConfFile, "MySQL", this.toString() + ".deleteQuery - user: " + sUserName);
		} catch (Exception e) {
			throw new Exception("Error executing delete - " + e.getMessage());
		}
		//Log the save action:
		
		TCLogEntry log = new TCLogEntry(sConfFile, getServletContext());
		log.writeEntry(
				sUserName, 
				TCLogEntry.LOG_OPERATION_QUERY_DELETE, 
				"Query ID: '" + sDeleteQueryID + "', Saved by: '" + sQueryUser + "', Title: '" + sQueryTitle + "', Comment = '" + sQueryComment + "'", 
				"Query String =  " + sQueryString,
				"[1517610207]");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
