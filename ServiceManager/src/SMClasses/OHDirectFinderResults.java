package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
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
	public static final String FINDER_LIST_FORMAT_PARAM = "FinderListFormat";
	public static final String FINDER_LIST_FORMAT_QUOTES_VALUE = "FinderListFormatQuotes";
	public static final String FINDER_LIST_FORMAT_QUOTE_LINES_VALUE = "FinderListFormatQuoteLines";
	public static final String FINDER_SEARCHING_CLASS_PARAM = "SearchingClass";
	public static final String FINDER_RETURN_FIELD_PARAM = "ReturnField";
	public static final String FINDER_RETURN_ADDITIONAL_PARAMS = "ReturnParams";
	public static final String RESULT_LIST_FIELD = "ResultListField";
	public static final String RESULT_LIST_HEADING = "ResultHeading";
	public static final String QUOTE_LINE_SEPARATOR = ",";

	//This is used to give the finder box a special title'
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
		String sListFormat = clsManageRequestParameters.get_Request_Parameter(FINDER_LIST_FORMAT_PARAM, request);
		String sReturnField = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_FIELD_PARAM, request);
		String sSearchingClass = clsManageRequestParameters.get_Request_Parameter(FINDER_SEARCHING_CLASS_PARAM, request);
		String sAdditionalReturnParams= clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_ADDITIONAL_PARAMS, request);	
		String sSearchLastModifiedStartDate= clsManageRequestParameters.get_Request_Parameter(OHDirectFinder.LAST_MODIFIED_START_DATE_PARAM, request);
		String sSearchLastModifiedEndDate= clsManageRequestParameters.get_Request_Parameter(OHDirectFinder.LAST_MODIFIED_END_DATE_PARAM, request);
		String sSearchCreatedStartDate= clsManageRequestParameters.get_Request_Parameter(OHDirectFinder.CREATED_START_DATE_PARAM, request);
		String sSearchCreatedEndDate= clsManageRequestParameters.get_Request_Parameter(OHDirectFinder.CREATED_END_DATE_PARAM, request);
		String sSearchJobText= clsManageRequestParameters.get_Request_Parameter(OHDirectFinder.SEARCH_JOB_TEXT_PARAM, request);		

		//Create page
		String title = sEndPointName + " search results.";
		if(sEndPointName.compareToIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_QUOTE) == 0) {
	    	title =  SMOHDirectFieldDefinitions.ENDPOINT_QUOTE_NAME + "search results.";
	    }
	    if(sEndPointName.compareToIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_ORDER) == 0) {
	    	title = SMOHDirectFieldDefinitions.ENDPOINT_ORDER_NAME + "search results.";
	    }
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
		out.println("<TABLE WIDTH=100% BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\" CELLSPACING=2 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n");
		out.println(printResultHeadings(arrDisplayHeadings));
		
		
		String sAPIQueryString = "";
		long lStartingTime = 0L;
		//Build query string
		if (sEndPointName.equalsIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_QUOTE)){
			sAPIQueryString = buildQuoteAPIQueryString(
					sEndPointName,
					sSearchLastModifiedStartDate,
					sSearchLastModifiedEndDate,
					sSearchCreatedStartDate,
					sSearchCreatedEndDate,
					sSearchJobText
			);
			
			//Get the quote list and print it
			lStartingTime = System.currentTimeMillis();
			try {
				out.println(printQuoteRows(
						arrResultListFields, 
						sAPIQueryString,
						sSearchingClass,
						sReturnField,
						sAdditionalReturnParams,
						sSearchJobText,
						sDBID,
						sUserID,
						sUserFullName,
						sLicenseModuleLevel,
						sListFormat
						));
			} catch (Exception e) {
				out.println("Error [1593030984] printing quotes - " + e.getMessage());
			}
		}

		out.println("</TABLE>\n<BR>");
		long lEndingTime = System.currentTimeMillis();
		out.println("Processing took " + (lEndingTime - lStartingTime) + " ms.\n");
		out.println("</BODY></HTML>");
	}

	private String buildQuoteAPIQueryString(
			String sEndPointName,
			String sSearchLastModifiedStartDate,
			String sSearchLastModifiedEndDate,
			String sSearchCreatedStartDate,
			String sSearchCreatedEndDate,
			String sSearchJobText) {
		
		sSearchLastModifiedStartDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sSearchLastModifiedStartDate);
		sSearchLastModifiedEndDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sSearchLastModifiedEndDate);
		sSearchCreatedStartDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sSearchCreatedStartDate);
		sSearchCreatedEndDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(sSearchCreatedEndDate);
		
		String sRequest = sEndPointName + "?%24filter="
				+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20ge%20'" +sSearchLastModifiedStartDate + "'"
				+ "%20and%20"
				+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20le%20'" +sSearchLastModifiedEndDate + "'"
				+ "%20and%20"
				+ SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20ge%20'" + sSearchCreatedStartDate + "'"
				+ "%20and%20"
				+ SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20le%20'" + sSearchCreatedEndDate + "'"
				+ "%20and%20"
				+ "substringof('" + sSearchJobText +  "'%2C%20" + SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME + ")%20eq%20true"
				+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
				;
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
			String sSearchingClass,
			String sReturnField,
			String sAdditionalReturnParams,
			String sSearchJobText,
			String sDBID, 
			String sUserID,
			String sUserFullname,
			String sLicenseModuleLevel,
			String sListFormat
			) throws Exception{
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
			throw new Exception("Error [1591191993] - getting connection - " + e1.getMessage());
		}
		
		boolean bAllowQuoteDisplay = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOHDirectQuote, 
			sUserID, 
			conn, 
			sLicenseModuleLevel);
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();

		try {
			ql.getQuoteList(sAPIRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [1591191994] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591191995]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			
			//Print a row:
			if ((i % 2) == 0) {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >" + "\n";
			}else {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			}
			
		  for(int j = 0; j < arrResultListFields.size(); j++) {

			  if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER) == 0) {
				String sQuoteNumberLink = ql.getQuoteNumbers().get(i);
				if (bAllowQuoteDisplay) {
					//If it's just the standard quote list, then we display a link back to the calling class.
					//But if it's a list of QUOTE LINES we're ultimately after, then we'll link
					// to a list of quote LINES and each of THOSE will link back to the calling class:
					if (sListFormat.compareToIgnoreCase(FINDER_LIST_FORMAT_QUOTES_VALUE) == 0) {
						sQuoteNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ sSearchingClass
							+ "?" + sReturnField + "=" + sQuoteNumberLink 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + sAdditionalReturnParams.replace("*", "&")
							+ "\">" + sQuoteNumberLink + "</A>"
						;
					}else {
							sQuoteNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMOHDirectFinderQuoteLines"
							+ "?" + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "=" + sQuoteNumberLink 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + OHDirectFinderResults.FINDER_RETURN_FIELD_PARAM + "=" + sReturnField
							+ "&" + OHDirectFinderResults.FINDER_SEARCHING_CLASS_PARAM + "=" + sSearchingClass
							+ "&" + OHDirectFinderResults.FINDER_RETURN_ADDITIONAL_PARAMS + "=" + sAdditionalReturnParams.replace("*", "&")
							+ "\">" + sQuoteNumberLink + "</A>"
						;
					}
				}	
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ sQuoteNumberLink
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getCreatedDates().get(i)
						+ "</TD>" + "\n"
					;	
			}
			
			if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDBY) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getCreatedBys().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getLastModifiedDates().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDBY) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ ql.getLastModifiedBys().get(i)
						+ "</TD>" + "\n"
					;
			}
			
			if(arrResultListFields.get(j).compareToIgnoreCase(SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME) == 0) {
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
						+ highlightResult(ql.getQuoteNames().get(i), sSearchJobText)
						+ "</TD>" + "\n"
					;
			}
			//TODO add the rest of the quote field definitions.
		  }
			s += "  </TR>" + "\n";
		}
		return s;
	}

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