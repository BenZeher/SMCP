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

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import TCSDataDefinition.TCSTablecompanyprofile;
import TCSDataDefinition.TCTablesavedqueries;

public class TCSavedQueriesSelect  extends HttpServlet {

	public static final String RADIO_BUTTON_GROUP_NAME = "QUERYGROUP";
	public static final String SUBMIT_BUTTON_NAME = "SUBMIT_BUTTON";
	public static final String SUBMIT_BUTTON_LABEL = "Use selected query";
	public static final String SORT_BY_ID = "SORTBYID";
	public static final String SORT_BY_ID_DESC = "SORTBYIDDESC";
	public static final String SORT_BY_DATE = "SORTBYDATE";
	public static final String SORT_BY_DATE_DESC = "SORTBYDATEDESC";
	public static final String SORT_BY_SAVEDBY = "SORTBYSAVEDBY";
	public static final String SORT_BY_SAVEDBY_DESC = "SORTBYSAVEDBYDESC";
	public static final String SORT_BY_TITLE = "SORTBYTITLE";
	public static final String SORT_BY_TITLE_DESC = "SORTBYTITLEDESC";
	public static final String SORT_BY_COMMENT = "SORTBYCOMMENT";
	public static final String SORT_BY_COMMENT_DESC = "SORTBYCOMMENTDESC";
	public static final String SORT_BY_PRIVATE = "SORTBYPRIVATE";
	public static final String SORT_BY_PRIVATE_DESC = "SORTBYPRIVATEDESC";
	public static final String DELETE_BUTTON_NAME = "DELETE_BUTTON";
	public static final String DELETE_BUTTON_LABEL = "Delete";
	public static final String DELETE_QUERY_ID_PARAM = "DELETE_QUERY_ID";
	public static final String SHOW_PUBLIC_QUERIES = "SHOWPUBLICQUERIES";
	//public static final String SHOW_PRIVATE_QUERIES = "SHOWPRIVATEQUERIES";
	private static final String TABLE_BACKGROUND_COLOR = "LightSteelBlue";
	
	private static final long serialVersionUID = 1L;
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
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No database name found in session.</FONT></B><BR>");
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
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>" + "\n");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"BLACK\">STATUS: " + sStatus + "</FONT></B><BR>" + "\n");
		}		
		
    	out.println("<TD><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain?" 
			+ TimeCardUtilities.SESSION_ATTRIBUTE_DB + "=" + sDBID 
			+ "&" + TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER + " = " + sUserID
			+ "\">Return to main admin menu</A><BR>" + "\n");

		//Print a link to the main query page:
		out.println("<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCQuerySelect" 
				+ "\">Return to Manage Queries</A><BR><BR>" + "\n");
		
		//Now print the edit page:
		try {
			out.println(getEditHTML(sDBID, sUserID, request));
		} catch (Exception e) {
			response.sendRedirect(
				"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?Warning=Could not process query - " + e.getMessage()
			);
			return;
		}
		return;
	}
		
	private String getEditHTML(String sDBID, String sUserID, HttpServletRequest req) throws Exception{

		//They can list EITHER private OR public queries - if they aren't listing PUBLIC queries, then they are listing PRIVATE:
		boolean bListPublicQueries = false;
		
		//If it's a request to list ALL queries, make sure the user has that permission:
		if (clsManageRequestParameters.get_Request_Parameter(SHOW_PUBLIC_QUERIES, req).compareToIgnoreCase("") != 0){
			bListPublicQueries = true;
		}
		
		String s = "";
		//We need to create a table with radio buttons for each of the export querylists, and, if the use chose to show individual invoices
		//in the export querylist, then we'll have a sub table for each of those, too:
		s += sStyleScripts();
		s += sCommandScripts();
		
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYSTRING, req);
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYTITLE, req);
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_FONTSIZE, req);
		boolean bIncludeBorder = (req.getParameter(TCQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				req).compareToIgnoreCase(TCQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				req).compareToIgnoreCase(TCQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (req.getParameter(TCQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (req.getParameter(TCQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (req.getParameter(TCQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (req.getParameter(TCQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
	    
	    //TODO - need to build an authentication function, like in SM:
	    
	    
	    
		//boolean bAllowManagePublicQueries = SMSystemFunctions.isFunctionPermitted(
		//	SMSystemFunctions.SMManagePublicQueries, 
		//	sm.getUserID(), 
		//	getServletContext(), 
		//	sm.getConfFile(),
		//	(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
	    boolean bAllowManagePublicQueries = true;
	    
	    s += "<FORM NAME='MAINFORM' 'METHOD='POST' ACTION='" + clsServletUtilities.getURLLinkBase(getServletContext())  + "TimeCardSystem.TCQuerySelect" + "'" + "\n";
	    
		//Store hidden variables:
	    s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_QUERYSTRING
			+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryString)
			+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_QUERYTITLE
			+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
			+ "\">";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ TCQuerySelect.PARAM_FONTSIZE
			+ "\" VALUE=\"" + sFontSize
			+ "\">" + "\n";
		
		if (bAlternateRowColors){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_ALTERNATEROWCOLORS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n";
		}
		if (bTotalNumericFields){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_TOTALNUMERICFIELDS
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n";
		}
		if (bShowSQLCommand){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_SHOWSQLCOMMAND
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n";
		}		
		if (bHideHeaderFooter){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_HIDEHEADERFOOTER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n";
		}
		if (bIncludeBorder){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_INCLUDEBORDER
				+ "\" VALUE=\"" + "Y"
				+ "\">" + "\n";
		}
		if (bExportAsCommaDelimited){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE
				+ "\">" + "\n";
		}
		if (bExportAsHTML){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + TCQuerySelect.EXPORT_HTML_VALUE
				+ "\">" + "\n";
		}
		

		//Store whether we are working with private or public queries here:
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SHOW_PUBLIC_QUERIES
			+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SHOW_PUBLIC_QUERIES, req)
			+ "\">" + "\n";
		
		//Store which query the user wants to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + DELETE_QUERY_ID_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + DELETE_QUERY_ID_PARAM + "\""
		+ "\">" + "\n";

		//Get the sort order:
		//This is the default sort order:
		String sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id + " DESC";
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_COMMENT, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.scomment + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_COMMENT_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.scomment + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_DATE, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.dattimesaved + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_DATE_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.dattimesaved + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_ID, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_ID_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_SAVEDBY, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.sfirstname
					+ " ASC, " + TCTablesavedqueries.TableName + "." + TCTablesavedqueries.slastname
					+ " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_SAVEDBY_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.sfirstname
					+ " DESC, " + TCTablesavedqueries.TableName + "." + TCTablesavedqueries.slastname
					+ " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_TITLE, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.stitle + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_TITLE_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.stitle + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_PRIVATE, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.iprivate + " ASC, " 
			+ TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_PRIVATE_DESC, req).compareToIgnoreCase("") !=0){
			sSortBy = TCTablesavedqueries.TableName + "." + TCTablesavedqueries.iprivate + " DESC,"
			+ TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id + " DESC";
		}
		String SQL = "SELECT"
			+ " * FROM " + TCTablesavedqueries.TableName
		;
		if (bListPublicQueries){
			SQL += " WHERE ("
					+ "(" + TCTablesavedqueries.iprivate + " = 0)"
				+ ")"
			;
		}else{
			SQL += " WHERE ("
					+ "(" + TCTablesavedqueries.suser + " = '" + sUserID + "')"
					+ " AND (" + TCTablesavedqueries.iprivate + " = 1)"
				+ ")"
			;
		}
		SQL += " ORDER BY " + sSortBy
		;
	
		s += "<FONT SIZE=2><B>NOTE:</B> To sort a column in ascending order, click its 'UP' arrow; to sort in descending order, click its 'DOWN' arrow:</FONT>" + "\n";
		s += "<BR><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>" + "\n";
		//Open outer table:
		s += "<TABLE class = \" querylist \" \" width=100% >\n";
		
		s += writeQueryHeaderRow(bListPublicQueries, bAllowManagePublicQueries);
		long lRowCounter = 0;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".getEditHTML.get queries - user: " + sUserID
			);
			while (rs.next()){
				//Write a row for each query:
				if (lRowCounter == 0){
					s += writeQueryRow(rs, true, (lRowCounter % 2) == 0, sUserID, sDBID, bAllowManagePublicQueries);
				}else{
					s += writeQueryRow(rs, false, (lRowCounter % 2) == 0, sUserID, sDBID, bAllowManagePublicQueries);
				}
				//Now turn off the selection after the first row is displayed:
				lRowCounter++;
			}
		} catch (Exception e1) {
			s += "</TABLE>";
			throw new SQLException("Error reading saved queries - " + e1.getMessage());
		}
		
		//Close outer table:
		s += "</TABLE>";
		s += "<INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>";
		return s;
	}
	private String writeQueryRow(
			ResultSet rs, 
			boolean bChecked, 
			boolean bEvenRow,
			String sUser,
			String sDBID,
			boolean bAllowManagePublicQueries) throws SQLException {
		String s = "";
		s += "  <TR>" + "\n";

		//Radio button:
		String sChecked = "";
		if (bChecked){
			sChecked = " CHECKED";
		}
		String sRowSuffix = "oddrow";
		if (bEvenRow){
			sRowSuffix = "evenrow";
		}
		String sQueryID = Long.toString(rs.getLong(TCTablesavedqueries.TableName + "." + TCTablesavedqueries.id));
		s += "    <TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" 
				+ RADIO_BUTTON_GROUP_NAME + "\" VALUE='" 
				+ sQueryID
				+ "' "
				+ sChecked
				+ ">"
				+ "</TD>" + "\n"
		;
		//HIDDEN ID 
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_QUERYID
				+ "\" VALUE=\"" + sQueryID
				+ "\">" + "\n";
		//ID
		s += "    <TD class=\"queryfieldrightaligned" + sRowSuffix + "\" >"
				+ sQueryID
			+ "</TD>" + "\n";
		
		//Date:
		s += "    <TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
				+ clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(
						TCTablesavedqueries.TableName + "." + TCTablesavedqueries.dattimesaved))
				+ "</TD>" + "\n";
		
		//Saved by:
		s += "    <TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(
						TCTablesavedqueries.TableName + "." + TCTablesavedqueries.sfirstname)
				+ " " + rs.getString(
						TCTablesavedqueries.TableName + "." + TCTablesavedqueries.slastname)
				+ "</TD>" + "\n";
		
		//Title
		s += "    <TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(
						TCTablesavedqueries.TableName + "." + TCTablesavedqueries.stitle)
				+ "</TD>" + "\n";
		
		//Comment 
		s += "    <TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(
						TCTablesavedqueries.TableName + "." + TCTablesavedqueries.scomment)
				+ "</TD>" + "\n";
		
		//Private?
		//String sPrivate = "";
		//if (rs.getInt(TCTablesavedqueries.iprivate) != 0){
		//	sPrivate = "X";
		//}
		//s += "<TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
		//		+ sPrivate
		//		+ "</TD>";
		
		//Radio button for delete:
		if (
				(rs.getInt(TCTablesavedqueries.iprivate) != 0)
				|| bAllowManagePublicQueries
			){
		
			s += "    <TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >";
			s += "<button type=\"button\""
				+ " value=\"" + DELETE_BUTTON_LABEL + sQueryID + "\""
				+ " name=\"" + DELETE_BUTTON_NAME + "\""
				+ " onClick=\"deleteQuery('" + sQueryID + "');\">"
				+ DELETE_BUTTON_LABEL
				+ "</button>" 
			;
			s += "</TD>" + "\n";
		}
		
		s += "  </TR>" + "\n";
		return s;
	}

	private String writeQueryHeaderRow(boolean bViewPublicQueries, boolean bAllowManagePublicQueries){
		
		String sViewPublicQueries = "";
		if (bViewPublicQueries){
			sViewPublicQueries = "&" + SHOW_PUBLIC_QUERIES + "=Y";
		}
		String s = "";
		s += "  <TR>" + "\n";
		s += "    <TH class=\"querylineheadingcenter\" >&nbsp;</TH>" + "\n";
		s += "    <TH class=\"querylineheadingright\" >ID"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_ID + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_ID_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>" + "\n";
		s += "    <TH class=\"querylineheadingleft\" >Date"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_DATE + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_DATE_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>" + "\n";
		s += "    <TH class=\"querylineheadingleft\" >Saved by"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_SAVEDBY + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_SAVEDBY_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>" + "\n";
		s += "    <TH class=\"querylineheadingleft\" >Title"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_TITLE + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_TITLE_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>" + "\n";
		s += "    <TH class=\"querylineheadingleft\" >Comment"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_COMMENT + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCSavedQueriesSelect?" 
			+ SORT_BY_COMMENT_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + clsServletUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>" + "\n";

		//If we are viewing private queries OR we are allowed to manage public queries, then add a column for deleting queries:
		if (!bViewPublicQueries || bAllowManagePublicQueries){
			s += "    <TH class=\"querylineheadingcenter\" >Delete?</TH>" + "\n";
		}
		s += "  </TR>" + "\n";
		return s;
	}

	private String sStyleScripts(){
		String s = "";
		//String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
			"table.querylist {"
			+ "border-width: " + "1" + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			//+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			+ "background-color: " + TABLE_BACKGROUND_COLOR + "; "
			+ "}"
			+ "\n"
			;

		//This is the def for a left aligned ODD ROW field:
		s +=
			"td.queryfieldleftalignedoddrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for a right aligned ODD ROW field:
		s +=
			"td.queryfieldrightalignedoddrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned ODD ROW field:
		s +=
			"td.queryfieldcenteralignedoddrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;	
		
		//This is the def for a left aligned EVEN ROW field:
		s +=
			"td.queryfieldleftalignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_GREY + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for a right aligned EVEN ROW field:
		s +=
			"td.queryfieldrightalignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_GREY + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned EVEN ROW field:
		s +=
			"td.queryfieldcenteralignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_GREY + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;	
		
		//This is the def for the querylist lines heading:
		s +=
			"th.querylineheadingleft {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: white; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.querylineheadingright {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: white; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: black; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.querylineheadingcenter {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: white; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: black; "
			+ "}"
			+ "\n"
			;	
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	private String sCommandScripts() {
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		
		s += "<script type=\"text/javascript\">\n";
		
		//s += "function addLine(){\n"
		//	+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
		//			 + LINECOMMANDADDLINE_LABEL + "\";\n"
		//	+ "    document.forms[\"MAINFORM\"].submit();\n"
		//	+ "}\n"
		//;

		s += "function deleteQuery(queryid){\n"
			//Confirm that the user wants to delete:
			+ "    if (confirm('Are you sure you want to delete query number ' + queryid + '?')){\n"
			+ "        document.getElementById(\"" + DELETE_QUERY_ID_PARAM + "\").value = queryid;\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        return;\n"
			+ "    }\n"
			+ "}\n"
		;

		s += "</script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
