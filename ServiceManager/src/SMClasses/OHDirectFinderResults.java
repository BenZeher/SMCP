package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
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
	private boolean bDebugMode = false;
	
	public static final String FINDER_ENDPOINT_NAME_PARAM = "EndpointName";
	public static final String FINDER_RETURN_CLASS_PARAM = "ReturnClass";
	public static final String FINDER_RETURN_FIELD_PARAM = "ReturnField";
	public static final String FINDER_PARAMETER_STRING_PARAM = "ParameterString";
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
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		

		//Get the parameters:
		String sEndPointName = clsManageRequestParameters.get_Request_Parameter(FINDER_ENDPOINT_NAME_PARAM, request);
		String sReturnField = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_FIELD_PARAM, request);
		String sReturnClass = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_CLASS_PARAM, request);
		
		
		String sParameterString = "";
		if (request.getParameter(FINDER_PARAMETER_STRING_PARAM) != null){
			sParameterString = (String) request.getParameter(FINDER_PARAMETER_STRING_PARAM);
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
			out.println("<BR>Error [1429302419] - sResultListFields.size() = 0,"
				+ " Endpoint name = '" + sEndPointName + "'" 
				+ ".<BR>");
			return;
		}
		ArrayList<String> sResultFieldAliases = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_FIELD_ALIAS + Integer.toString(i)) != null){
				sResultFieldAliases.add((String) request.getParameter(RESULT_FIELD_ALIAS + Integer.toString(i)));
			}else{
				sResultFieldAliases.add("");
			}
		}
		if (bDebugMode){
			for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
				System.out.println("[1589812554] - sResultFieldAliases.get(" + i + ") = " + sResultFieldAliases.get(i));
			}
		}
		ArrayList<String> arrDisplayHeadings = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)) != null){
				arrDisplayHeadings.add((String) request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)));
			}
		}

		String sSearchLastModifiedDateRange = request.getParameter("sSearchLastModifiedDate");
		String sSearchCreatedDateRange = request.getParameter("sSearchCreatedDate");
		String sSearchText = request.getParameter("sSearchTextString");
		String sAPIQueryString = "";
		
		//Build query string
		if (sEndPointName.equalsIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_QUOTE_NAME)){
			
			sAPIQueryString = buildQuoteAPIQueryString(
					arrResultListFields,
					sSearchLastModifiedDateRange,
					sSearchCreatedDateRange,
					sSearchText
			);
			
			Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".doGet - UserID: " + sUserID
				);
			} catch (Exception e1) {
				//throw new Exception("Error [202004273105] - getting connection - " + e1.getMessage());
			}
			
			SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
			try {
				ql.getQuoteList(sAPIQueryString, conn, sDBID, sUserID);
			} catch (Exception e4) {
				//throw new Exception("Error [202004273522] - " + e4.getMessage());
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

		
		//Now run the query:
/*
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".doPost - user: '" + sUserID + " - " + sUserFullName + "', company: '" + sCompanyName + "'");
			//Now list the records:
			String sFieldName = "";
			int iCount =0;
			while(rs.next()){
				//for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
					//System.out.println("[1491249955]");
					//System.out.println("[1491249951] field type = '" + rs.getMetaData().getColumnType(i) + "'");
					//System.out.println("[1491249954] field label = '" + rs.getMetaData().getColumnLabel(i) + "'");
				//}
				
				//if (bOddRow){
				//	out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >\n");
				//}else{
				//	out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >\n");
				//}
				
				//We assume the first field returns the value we are looking for, so we make the first
				//field a link, which goes back, ultimately, to the class that called the search:
				sFieldName = (String) arrResultListFields.get(0);
				//If there is an alias for the result field, use that:
				//The aliased field name is indexed one HIGHER, because in the parameters we pass to the previous function,
				//there is never a 'FieldAlias0' - it is one-based.
				if (sResultFieldAliases.get(1).compareToIgnoreCase("") != 0){
					sFieldName = (String) sResultFieldAliases.get(1);
				}
				String sFieldValue = rs.getString(sFieldName);
				if (sFieldValue == null){
					sFieldValue = "";
				}
				if(iCount % 2 ==0) {
					out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + " \" >\n");
				}else {
					out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + " \" >\n");
				}
				if (sReturnField.compareToIgnoreCase("") != 0){
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"    	
						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
						+ sSearchingClass + "?"
						+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + sReturnField + "=" 
						+ clsServletUtilities.URLEncode(sFieldValue.trim())
						//We have to put the '=' back after encoding the string: - TJR - 7/19/2011
						+ clsServletUtilities.URLEncode(sParameterString).replace("*", "&").replace("%3D", "=")
						+ "\">"
						+ sFieldValue.trim()
						+ "</A></FONT></TD>\n");
				}else{
					//If there IS no return field, don't display a link:
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"    	
						+ sFieldValue.trim()
						+ "</FONT></TD>\n");
				}
				
				//Now print the rest of the fields:
				String sDataValue = "";
				for (int i=1;i<arrResultListFields.size();i++){
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"); 
					sFieldName = (String) arrResultListFields.get(i);
					//System.out.println("[1491249952] sFieldName = '" + sFieldName + "'");
					//The aliased field name is indexed one HIGHER, because in the parameters we pass to the previous function,
					//there is never a 'FieldAlias0' - it is one-based.
					String sFieldAlias = (String) sResultFieldAliases.get(i + 1);
					//If there is an alias for the result field, use that:
					if (sFieldAlias.compareToIgnoreCase("") != 0){
						sFieldName = sFieldAlias;
					}
					//System.out.println("[1491249956] sFieldName = '" + sFieldName + "'");
					if (rs.getString(sFieldName) == null || rs.getString(sFieldName).compareTo("") == 0){
						out.println("&nbsp;");
					}else{
						sDataValue = rs.getString(sFieldName).trim();
						if (sFieldName.compareToIgnoreCase(sSearchField) == 0){
							out.println(
									highlightResult(sDataValue, sSearchText)
								//sDataValue.replaceAll(
								//	"(?i)" + sSearchText, "<span style=\"background-color: #FFFF66\">"
								//	+ sSearchText + "</span>")
							);
							// <span style="background-color: #FFFF66">selected text</span>
						}else{
							out.println(sDataValue);
						}
					}
					out.println("</FONT></TD>\n");
				}
				out.println("  </TR>\n");
				iCount++;
			}
			rs.close();
		}catch (Exception ex){
			out.println("Error [1481731164] - could not execute search query with SQL: " + sSQL 
				+ " - " + ex.getMessage() + ".<BR>");
			clsServletUtilities.sysprint(this.toString(), sUserFullName, "Error [1481731165] - could not execute search query with SQL: " + sSQL 
				+ " - " + ex.getMessage() + ".");
		}
*/
		out.println("</TABLE>\n<BR>");
		out.println("</BODY></HTML>");

	}

	private String buildQuoteAPIQueryString(
			ArrayList<String> arrResultListFields,
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
	
/*	
	private String printQuoteResults(
			String sUserID, 
			String sLicenseModuleLevel,
			SMOHDirectQuoteList ql) throws Exception{
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
			throw new Exception("Error [202004273105] - getting connection - " + e1.getMessage());
		}
		
		boolean bAllowQuoteDisplay = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOHDirectQuote, 
			sUserID, 
			conn, 
			sLicenseModuleLevel);
		
		//For now, we're just hardwiring in this request85
		//String sRequest = "C_DealerQuote?%24filter="
		//	+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20gt%20'2020-01-09'"
		//	+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
		//;
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE
				+ "?%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
			;
		
		try {
			ql.getQuoteList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [202004273522] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019577]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			//if ((i % 2) == 0) {
			//	s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + "\" >" + "\n";
			//}else {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			//}
			
			String sQuoteNumberLink = ql.getQuoteNumbers().get(i);
			if (bAllowQuoteDisplay) {
				sQuoteNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMDisplayOHDirectQuote"
					+ "?" + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "=" + sQuoteNumberLink 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "\">" + sQuoteNumberLink + "</A>"
				;
			}
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ sQuoteNumberLink
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ ql.getCreatedDates().get(i)
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getCreatedBys().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getLastModifiedDates().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getLastModifiedBys().get(i)
					+ "</TD>" + "\n"
				;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getQuoteNames().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}
		
		return s;
	}
*/	
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