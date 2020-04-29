package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOHDirectQuoteList;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMOHDirectQuoteListing extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMOHDirectQuoteList)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);

		/*******************************************************/

		out.println(SMUtilities.getMasterStyleSheetLink());
		//Customized title
		String sReportTitle = "OHDirect Plus Quote List";
		
		out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		out.println("  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "  </TR>\n"
			+ "</TABLE>"
		);
		
		if (sWarning.compareToIgnoreCase("") != 0) {
			out.println("<FONT COLOR=RED><B>WARNING: " + sWarning + "</B></FONT><BR>" + "\n");
		}
		
		out.println("<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">\n");
		
		//Print Headings:
		out.println(printHeadings());
		long lStartingTime = System.currentTimeMillis();
		try {
			out.println(printQuoteRows(sDBID, sUserID, sLicenseModuleLevel));
		} catch (Exception e2) {
			out.println("<FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>" + "\n");
			//response.sendRedirect(
			//		"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			//		+ "Warning=" + clsServletUtilities.URLEncode(e2.getMessage())
			//		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			//		//+ sParamString
			//);			
			return;
		}
		
		out.println("</TABLE>\n");
		out.println("<BR>\n");
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime) + " ms.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
			
	}
	
	private String printHeadings() {
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Quote #"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Date created"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Created by"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Last modified"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Modified by"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Name"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		return s;
	}
	private String printQuoteRows(String sDBID, String sUserID, String sLicenseModuleLevel) throws Exception{
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
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		
		//For now, we're just hardwiring in this request:
		//String sRequest = "C_DealerQuote?%24filter="
		//	+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20gt%20'2020-01-09'"
		//	+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
		//;
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE
				+ "?%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
			;
		
		try {
			ql.getQuoteList(sRequest, conn, sDBID);
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
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}
}
