package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMSavedQueriesSelect  extends HttpServlet {

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
	private static final String EVEN_ROW_BACKGROUND_COLOR = "#DCDCDC";
	private static final String ODD_ROW_BACKGROUND_COLOR = "#FFFFFF";
	
	private static final long serialVersionUID = 1L;
	private String sRedirect = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Saved queries",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMQuerySelect",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMQuerySelector
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMQuerySelector)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    smedit.printHeaderTable();
	    smedit.setbIncludeDeleteButton(false);
	    smedit.setbIncludeUpdateButton(false);
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, smedit.getUserID()), "");
		} catch (SQLException e) {
    		sRedirect += "&Warning=Error listing queries: " + e.getMessage()
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID();
			response.sendRedirect(sRedirect);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, String sUserID) throws SQLException{

		//They can list EITHER private OR public queries - if they aren't listing PUBLIC queries, then they are listing PRIVATE:
		boolean bListPublicQueries = false;
		
		//If it's a request to list ALL queries, make sure the user has that permission:
		if (clsManageRequestParameters.get_Request_Parameter(SHOW_PUBLIC_QUERIES, sm.getRequest()).compareToIgnoreCase("") != 0){
			bListPublicQueries = true;
		}
		
		String s = "";
		//We need to create a table with radio buttons for each of the export querylists, and, if the use chose to show individual invoices
		//in the export querylist, then we'll have a sub table for each of those, too:
		s += sStyleScripts();
		s += sCommandScripts();
		
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, sm.getRequest());
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYTITLE, sm.getRequest());
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_FONTSIZE, sm.getRequest());
		boolean bIncludeBorder = (sm.getRequest().getParameter(SMQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				sm.getRequest()).compareToIgnoreCase(SMQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				sm.getRequest()).compareToIgnoreCase(SMQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (sm.getRequest().getParameter(SMQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (sm.getRequest().getParameter(SMQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (sm.getRequest().getParameter(SMQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (sm.getRequest().getParameter(SMQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
		boolean bAllowManagePublicQueries = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMManagePublicQueries, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		//Store hidden variables:
	    s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_QUERYSTRING
			+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryString)
			+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_QUERYTITLE
			+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
			+ "\">";

		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SMQuerySelect.PARAM_FONTSIZE
			+ "\" VALUE=\"" + sFontSize
			+ "\">";
		
		if (bAlternateRowColors){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_ALTERNATEROWCOLORS
				+ "\" VALUE=\"" + "Y"
				+ "\">";
		}
		if (bTotalNumericFields){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_TOTALNUMERICFIELDS
				+ "\" VALUE=\"" + "Y"
				+ "\">";
		}
		if (bShowSQLCommand){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_SHOWSQLCOMMAND
				+ "\" VALUE=\"" + "Y"
				+ "\">";
		}		
		if (bHideHeaderFooter){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_HIDEHEADERFOOTER
				+ "\" VALUE=\"" + "Y"
				+ "\">";
		}
		if (bIncludeBorder){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_INCLUDEBORDER
				+ "\" VALUE=\"" + "Y"
				+ "\">";
		}
		if (bExportAsCommaDelimited){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE
				+ "\">";
		}
		if (bExportAsHTML){
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_EXPORTOPTIONS
				+ "\" VALUE=\"" + SMQuerySelect.EXPORT_HTML_VALUE
				+ "\">";
		}
		

		//Store whether we are working with private or public queries here:
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
			+ SHOW_PUBLIC_QUERIES
			+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SHOW_PUBLIC_QUERIES, sm.getRequest())
			+ "\">";
		
		//Store which query the user wants to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + DELETE_QUERY_ID_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + DELETE_QUERY_ID_PARAM + "\""
		+ "\">";

		//Get the sort order:
		//This is the default sort order:
		String sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id + " DESC";
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_COMMENT, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.scomment + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_COMMENT_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.scomment + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_DATE, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.dattimesaved + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_DATE_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.dattimesaved + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_ID, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_ID_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_SAVEDBY, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.suserfullname + " ASC ";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_SAVEDBY_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.suserfullname + " DESC ";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_TITLE, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.stitle + " ASC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_TITLE_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.stitle + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_PRIVATE, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.iprivate + " ASC, " 
			+ SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id + " DESC";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SORT_BY_PRIVATE_DESC, sm.getRequest()).compareToIgnoreCase("") !=0){
			sSortBy = SMTablesavedqueries.TableName + "." + SMTablesavedqueries.iprivate + " DESC,"
			+ SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id + " DESC";
		}
		String SQL = "SELECT"
			+ " * FROM " + SMTablesavedqueries.TableName
		;
		if (bListPublicQueries){
			SQL += " WHERE ("
					+ "(" + SMTablesavedqueries.iprivate + " = 0)"
				+ ")"
			;
		}else{
			SQL += " WHERE ("
					+ "(" + SMTablesavedqueries.luserid + " = '" + sUserID + "')"
					+ " AND (" + SMTablesavedqueries.iprivate + " = 1)"
				+ ")"
			;
		}
		SQL += " ORDER BY " + sSortBy
		;
	
		s += "<FONT SIZE=2><B>NOTE:</B> To sort a column in ascending order, click its 'UP' arrow; to sort in descending order, click its 'DOWN' arrow:</FONT>";
		s += "<BR><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>";
		//Open outer table:
		s += "<TABLE class = \" querylist \" \" width=100% >\n";
		
		s += writeQueryHeaderRow(sm.getsDBID(), bListPublicQueries, bAllowManagePublicQueries);
		long lRowCounter = 0;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".getEditHTML.get queries - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
			);
			while (rs.next()){
				//Write a row for each query:
				if (lRowCounter == 0){
					s += writeQueryRow(rs, true, (lRowCounter % 2) == 0, sUserID, sm.getsDBID(), bAllowManagePublicQueries);
				}else{
					s += writeQueryRow(rs, false, (lRowCounter % 2) == 0, sUserID, sm.getsDBID(), bAllowManagePublicQueries);
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
			String sUserID,
			String sConf,
			boolean bAllowManagePublicQueries) throws SQLException {
		String s = "";
		s += " <TR>";

		//Radio button:
		String sChecked = "";
		if (bChecked){
			sChecked = " CHECKED";
		}
		String sRowSuffix = "oddrow";
		if (bEvenRow){
			sRowSuffix = "evenrow";
		}
		String sQueryID = Long.toString(rs.getLong(SMTablesavedqueries.TableName + "." + SMTablesavedqueries.id));
		s += "<TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" 
				+ RADIO_BUTTON_GROUP_NAME + "\" VALUE='" 
				+ sQueryID
				+ "' "
				+ sChecked
				+ ">"
				+ "</TD>"
		;
		//HIDDEN ID 
		s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_QUERYID
				+ "\" VALUE=\"" + sQueryID
				+ "\">";
		//ID
		s += "<TD class=\"queryfieldrightaligned" + sRowSuffix + "\" >"
				+ sQueryID
			+ "</TD>";
		
		//Date:
		s += "<TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
				+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(
						SMTablesavedqueries.TableName + "." + SMTablesavedqueries.dattimesaved))
				+ "</TD>";
		
		//Saved by:
		s += "<TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(SMTablesavedqueries.TableName + "." + SMTablesavedqueries.suserfullname)
				+ "</TD>";
		
		//Title
		s += "<TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(
						SMTablesavedqueries.TableName + "." + SMTablesavedqueries.stitle)
				+ "</TD>";
		
		//Comment 
		s += "<TD class=\"queryfieldleftaligned" + sRowSuffix + "\" >"
				+ rs.getString(
						SMTablesavedqueries.TableName + "." + SMTablesavedqueries.scomment)
				+ "</TD>";
		
		//Private?
		//String sPrivate = "";
		//if (rs.getInt(SMTablesavedqueries.iprivate) != 0){
		//	sPrivate = "X";
		//}
		//s += "<TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >"
		//		+ sPrivate
		//		+ "</TD>";
		
		//Radio button for delete:
		if (
				(rs.getInt(SMTablesavedqueries.iprivate) != 0)
				|| bAllowManagePublicQueries
			){
		
			s += "<TD class=\"queryfieldcenteraligned" + sRowSuffix + "\" >";
			s += "<button type=\"button\""
				+ " value=\"" + DELETE_BUTTON_LABEL + sQueryID + "\""
				+ " name=\"" + DELETE_BUTTON_NAME + "\""
				+ " onClick=\"deleteQuery('" + sQueryID + "');\">"
				+ DELETE_BUTTON_LABEL
				+ "</button>" 
			;
			s += "</TD>";
		}
		
		s += "</TR>";
		return s;
	}

	private String writeQueryHeaderRow(String sDBID, boolean bViewPublicQueries, boolean bAllowManagePublicQueries){
		
		String sViewPublicQueries = "";
		if (bViewPublicQueries){
			sViewPublicQueries = "&" + SHOW_PUBLIC_QUERIES + "=Y";
		}
		String s = "";
		s += " <TR>";
		s += "<TH class=\"querylineheadingcenter\" >&nbsp;</TH>";
		s += "<TH class=\"querylineheadingright\" >ID"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_ID + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_ID_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>";
		s += "<TH class=\"querylineheadingleft\" >Date"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_DATE + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_DATE_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>";
		s += "<TH class=\"querylineheadingleft\" >Saved by"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_SAVEDBY + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_SAVEDBY_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>";
		s += "<TH class=\"querylineheadingleft\" >Title"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_TITLE + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_TITLE_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>";
		s += "<TH class=\"querylineheadingleft\" >Comment"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_COMMENT + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_up.png\" alt=\"sort_arrow_up.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSavedQueriesSelect?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SORT_BY_COMMENT_DESC + "=Y"
			+ sViewPublicQueries
			+ "\">"
			+ "<img src=\"" + SMUtilities.getImagePath(getServletContext()) 
			+ "sort_arrow_dn.png\" alt=\"sort_arrow_dn.png\"  width=\"12\" height=\"12\" style=\"border: 0px;\"/>"
			+ "</A>"
			+ "</TH>";

		//If we are viewing private queries OR we are allowed to manage public queries, then add a column for deleting queries:
		if (!bViewPublicQueries || bAllowManagePublicQueries){
			s += "<TH class=\"querylineheadingcenter\" >Delete?</TH>";
		}
		s += " </TR>";
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
					+ "background-color: " + ODD_ROW_BACKGROUND_COLOR + "; "
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
					+ "background-color: " + ODD_ROW_BACKGROUND_COLOR + "; "
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
					+ "background-color: " + ODD_ROW_BACKGROUND_COLOR + "; "
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
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
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
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
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
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
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
