package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class OHDirectFinderResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String FINDER_ENDPOINT_NAME_PARAM = "EndpointName";
	public static final String FINDER_RETURN_CLASS_PARAM = "ReturnClass";
	public static final String FINDER_RETURN_FIELD_PARAM = "ReturnField";
	public static final String FINDER_RETURN_PARAM = "ReturnParams";
	public static final String RESULT_LIST_FIELD = "ResultListField";
	public static final String RESULT_LIST_HEADING = "ResultHeading";
	public static final String RESULT_FIELD_ALIAS = "ResultFieldAlias";

	//This is used to give the finder box a special title, e.g.: 'List of AP Transactions for vendor number OHD02'
	public static final String FINDER_BOX_TITLE = "FINDERBOXTITLE";
	
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		

		//Get the parameters:
		String sEndPointName = clsManageRequestParameters.get_Request_Parameter(FINDER_ENDPOINT_NAME_PARAM, request);
		String sReturnField = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_FIELD_PARAM, request);
		String sReturnClass = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_CLASS_PARAM, request);
		String sAdditionalReturnParams= "";
		if (request.getParameter(FINDER_RETURN_PARAM) != null){
			sAdditionalReturnParams = (String) request.getParameter(FINDER_RETURN_PARAM);
		}

		String title = sEndPointName + " search results.";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(sCommandScripts());

		ArrayList<String> arrResultListFields = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_LIST_FIELD + Integer.toString(i)) != null){
				arrResultListFields.add((String) request.getParameter(RESULT_LIST_FIELD + Integer.toString(i)));
			}
		}
		if (arrResultListFields.size() == 0){
			out.println("<BR>Error [1590160639] - sResultListFields.size() = 0,"
				+ " Endpoint name = '" + sEndPointName + "'" 
				+ ".<BR>");
			return;
		}

		ArrayList<String> arrDisplayHeadings = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)) != null){
				arrDisplayHeadings.add((String) request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)));
			}
		}

	    //Create HTML
	    //Unless told not to, print a link to the first page after login:
		if (clsManageRequestParameters.get_Request_Parameter(ObjectFinder.DO_NOT_SHOW_MENU_LINK, request).compareToIgnoreCase("") == 0){
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		}
		
		//If there is a 'special title' for the finder box, print it here:
		if (clsManageRequestParameters.get_Request_Parameter(FINDER_BOX_TITLE, request).compareToIgnoreCase("") != 0){
			out.println("<BR><B><I>" + clsManageRequestParameters.get_Request_Parameter(FINDER_BOX_TITLE, request) + "</I></B>\n");
		}

		
		//Now set up the table and header row:
		out.println("<TABLE WIDTH=100% BGCOLOR=\"#FFFFFF\" CELLSPACING=2 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n");
		out.println(printResultHeadings(arrDisplayHeadings));
		
		String sSearchLastModifiedDateRange = request.getParameter("sSearchLastModifiedDate");
		String sSearchCreatedDateRange = request.getParameter("sSearchCreatedDate");
		String sSearchText = request.getParameter("sSearchTextString");
		String sAPIQueryString = "";
		
		
		//Build query string
		if (sEndPointName.equalsIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_QUOTE)){
			
			sAPIQueryString = buildQuoteAPIQueryString(
					sSearchLastModifiedDateRange,
					sSearchCreatedDateRange,
					sSearchText
			);
			
			//Get the quote list and print it
			try {
				out.println(printQuoteRows(
						arrResultListFields, 
						sAPIQueryString,
						sReturnClass,
						sReturnField,
						sAdditionalReturnParams,
						sDBID,
						sUserID,
						sUserFullName,
						sLicenseModuleLevel
						));
			} catch (Exception e) {
				out.println("Error printing quote lines - " + e.getMessage());
			}
			
		}

		out.println("</TABLE>\n<BR>");
		out.println("</BODY></HTML>");

	}

	private String buildQuoteAPIQueryString(
			String sSearchLastModifiedDateRange,
			String sSearchCreatedDateRange,
			String sSearchText) {
		
		//TODO build request API string with given parameters
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE
				+ "?%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc";
			
		return sRequest;
	}
	
	private String printResultHeadings(ArrayList<String> arrDisplayHeadings) {
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" >";
		for (int i = 0; i< arrDisplayHeadings.size(); i++){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \""+ ">"
				+ "<B><FONT SIZE=2>" + arrDisplayHeadings.get(i) + "</FONT></B></TD>\n"
			;
		}
		s += "  </TR>\n";	 
		
		return s;
	}
	
	private String printQuoteRows(
			ArrayList<String> arrResultListFields, 
			String sAPIRequest, 
			String sReturnClass,
			String sReturnField,
			String sAdditionalReturnParams,
			String sDBID, 
			String sUserID,
			String sUserFullname,
			String sLicenseModuleLevel) throws Exception{
		String s = "";
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doGet - UserID: " + sUserID
			);
		} catch (Exception e1) {
			throw new Exception("Error [1590160633] - getting connection - " + e1.getMessage());
		}
		
		boolean bAllowQuoteDisplay = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOHDirectQuote, 
			sUserID, 
			conn, 
			sLicenseModuleLevel);
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		
		//For now, we're just hardwiring in this request85
		//String sRequest = "C_DealerQuote?%24filter="
		//	+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20gt%20'2020-01-09'"
		//	+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
		//;

		try {
			ql.getQuoteList(sAPIRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [1590160634] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590160635]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			//if ((i % 2) == 0) {
			//	s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + "\" >" + "\n";
			//}else {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			//}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER) == 0) {
				String sQuoteNumberLink = ql.getQuoteNumbers().get(i);
				if (bAllowQuoteDisplay) {
					sQuoteNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ sReturnClass
						+ "?" + sReturnField + "=" + sQuoteNumberLink 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + sAdditionalReturnParams
						+ "\">" + sQuoteNumberLink + "</A>"
					;
				}	
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ sQuoteNumberLink
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getCreatedDates().get(i)
						+ "</TD>" + "\n"
					;	
			}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDBY) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getCreatedBys().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getLastModifiedDates().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDBY) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getLastModifiedBys().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(i).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getQuoteNames().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			//TODO add the rest of the field names
			s += "  </TR>" + "\n";
		}
		
		return s;
	}
/*	
	private String highlightResult(String sResult, String sSearchString){
		String s = "";
		int iFirstPosition = sResult.toUpperCase().indexOf(sSearchString.toUpperCase());
		if (iFirstPosition >= 0){
			s = sResult.substring(0, iFirstPosition) + "<span style=\"background-color: #FFFF66\">"
			+ sResult.substring(iFirstPosition, iFirstPosition + sSearchString.length()) 
			+ "</span>"
			+ sResult.substring(iFirstPosition + sSearchString.length(), sResult.length())
			;
		}
		
		return s;
	}
*/
	private String sCommandScripts(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		s += "function sortResultsTable(nSortColumnIndex) { \n"
			//This sort is WAY too slow on long result lists....
			//+ "    sortTable(\"" + TABLE_RESULTS_ID + "\", nSortColumnIndex, 1); \n"
			+ "} \n"
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