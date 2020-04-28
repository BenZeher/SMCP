package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOHDirectQuoteLineDetailList;
import SMClasses.SMOHDirectQuoteLineList;
import SMClasses.SMOHDirectQuoteList;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayOHDirectQuote extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

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
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sQuoteNumber = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER, request);

		/*******************************************************/

		out.println(SMUtilities.getMasterStyleSheetLink());
		//Customized title
		String sReportTitle = "View OHDirect Plus Quote";
		
		out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		out.println("  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "  </TR>\n"
			+ "</TABLE>"
		);
		
		out.println("<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">\n");
		
		//Print Headings:
		out.println( printQuoteHeadings());
		long lStartingTime = System.currentTimeMillis();
		try {
			out.println(printQuote(sDBID, sUserID, sQuoteNumber));
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
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime) + " ms.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
			
	}
	
	private String printQuoteHeadings() {
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
			+ "Bill to"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Ship to"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		return s;
	}
	private String printQuote(String sDBID, String sUserID, String sQuoteNumber) throws Exception{
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
			throw new Exception("Error [202004274105] - getting connection - " + e1.getMessage());
		}
		
		try {
			s += printQuoteTable(sDBID, sUserID, sQuoteNumber, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588088197]");
			throw new Exception("Error [202004283532] - Error printing quote row - " + e.getMessage());
		}
		
		return s;
	}
	private String printQuoteTable(String sDBID, String sUserID, String sQuoteNumber, Connection conn) throws Exception{
		String s = "";
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		
		//For now, we're just hardwiring in this request:
		String sRequest = "C_DealerQuote?%24filter="
			+ SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteNumber + "'"
		;
		
		try {
			ql.getQuoteList(sRequest, conn);
		} catch (Exception e4) {
			throw new Exception("Error [202004274522] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019677]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getQuoteNumbers().get(i) + "</B"
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getCreatedDates().get(i) + "</B"
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getCreatedBys().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getLastModifiedDates().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getLastModifiedBys().get(i) + "</B"
					+ "</TD>" + "\n"
				;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getBillToNames().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getShipToNames().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}
		
		s += "</TABLE>\n";
		s += "<BR>\n";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">\n";
		
		s += printQuoteLineHeader();
		
		try {
			s += printQuoteLines(conn, sDBID, sUserID, ql.getQuoteIDs().get(0));
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588088198]");
			throw new Exception("Error [202004283554] - Error printing quote lines - " + e.getMessage());
		}
		
		s += "</TABLE>" + "\n";
		
		return s;
	}
	private String printQuoteLineHeader() {
		String s = "";
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Line #"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Quantity"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Label"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Description"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Configuration Desc"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Total Cost"
			+ "</TD>" + "\n"
		;
		

		s += "  </TR>" + "\n";
		
		return s;
		
	}
	private String printQuoteLines(Connection conn, String sDBID, String sUserID, String sQuoteID) throws Exception{
		String s = "";
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteLineList ql = new SMOHDirectQuoteLineList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINE + "?$filter=" 
			+ SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "%20asc"
		;
		
		try {
			ql.getQuoteLineList(sRequest, conn);
		} catch (Exception e4) {
			throw new Exception("Error [202004273622] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019777]");
		
		BigDecimal bdTotalQuoteCost = new BigDecimal("0.00");
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getLineNumbers().get(i) + "</B"
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getQuantities().get(i) + "</B"
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getLabels().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getDescriptions().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ql.getLastConfigurationDescriptions().get(i) + "</B"
					+ "</TD>" + "\n"
				;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ql.getTotalCosts().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			bdTotalQuoteCost = bdTotalQuoteCost.add(ql.getTotalCosts().get(i));
			
			s += printQuoteLineDetails(conn, ql.getQuoteLineIDs().get(i));
			
			s += "  </TR>" + "\n";
		}
		
		//Print a line for the overall cost:
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\""
				+ " style = \" color:white; font-weight:bold; \""
				+ "COLSPAN=5 >"
				+ "QUOTE TOTAL:"
				+ "</TD>" + "\n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" "
				+ " style = \" color:white; font-weight:bold; \""
				+ ">"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalQuoteCost)
				+ "</B>"
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		return s;
	}
	
	private String printQuoteLineDetails(Connection conn, String sQuoteLineID) throws Exception{
		String s = "";
		//Get the OHDirect connection settings:
		SMOHDirectQuoteLineDetailList ql = new SMOHDirectQuoteLineDetailList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINEDETAIL 
			+ "?$filter=" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_QUOTELINEID + "%20eq%20'" + sQuoteLineID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER + "%20asc"
		;
		
		try {
			ql.getQuoteLineDetailList(sRequest, conn);
		} catch (Exception e4) {
			throw new Exception("Error [202004273622] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019777]");
		
		for(int i = 0; i < ql.getQuoteLineDetailIDs().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
				+ " style = \" background-color:white;  \" "
				+ ">"
				+ "&nbsp;"
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" COLSPAN=5 >"
				//+ "<SPAN style = \" font-weight:bold; \" >"
				//+ ql.getDescriptions().get(i)
				//+ "</SPAN>"
				//+ ": "
				+ ql.getValues().get(i)
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
