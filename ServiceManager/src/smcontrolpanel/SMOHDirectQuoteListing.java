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

import SMClasses.SMOHDirectQuoteList;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMOHDirectQuoteListing extends HttpServlet {

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
		out.println( printHeadings());
		long lStartingTime = System.currentTimeMillis();
		try {
			out.println(printQuoteRows(sDBID, sUserID));
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
	private String printQuoteRows(String sDBID, String sUserID) throws Exception{
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
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		
		//For now, we're just hardwiring in this request:
		String sRequest = "C_DealerQuote?%24filter="
			+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20gt%20'2020-01-09'"
			+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
		;
		
		try {
			ql.getQuoteList(sRequest, conn);
		} catch (Exception e4) {
			throw new Exception("Error [202004273522] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019577]");
		
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			//Print a row:
			if ((i % 2) == 0) {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >" + "\n";
			}else {
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			}
			
			
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
		
		return s;
	}
}
