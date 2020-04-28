package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
		

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sQuoteNumber = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER, request);

		/*******************************************************/

		String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
		out.println(SMUtilities.getMasterStyleSheetLink());
		//Customized title
		String sReportTitle = "OHDirect Plus Quote List";
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"#FFFFFF\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		
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
				+ ql.getQuoteNumbers().get(i)
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
					+ ql.getBillToNames().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getShipToNames().get(i)
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
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
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
		
		String sRequest = "C_DealerQuoteLine?$filter=" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "%20asc"
		;
		//String sRequest = "C_DealerQuote?%24filter="
		//	+ SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteNumber + "'"
		//;
		
		try {
			ql.getQuoteLineList(sRequest, conn);
		} catch (Exception e4) {
			throw new Exception("Error [202004273622] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019777]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ ql.getLineNumbers().get(i)
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ ql.getQuantities().get(i)
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getLabels().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getDescriptions().get(i)
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getLastConfigurationDescriptions().get(i)
					+ "</TD>" + "\n"
				;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ ql.getTotalCosts().get(i)
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
