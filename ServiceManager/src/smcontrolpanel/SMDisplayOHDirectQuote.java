package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOHDirectQuoteLineCostDetailList;
import SMClasses.SMOHDirectQuoteLineDetailList;
import SMClasses.SMOHDirectQuoteLineList;
import SMClasses.SMOHDirectQuoteList;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayOHDirectQuote extends HttpServlet {

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
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sQuoteNumber = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER, request);
		String sRequestedQuoteLine = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER, request);

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
			out.println(printQuote(sDBID, sUserID, sQuoteNumber, sRequestedQuoteLine, sLicenseModuleLevel));
		} catch (Exception e2) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + ServletUtilities.clsServletUtilities.URLEncode(e2.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					//+ sParamString
			);			
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
			+ "Name"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		return s;
	}
	private String printQuote(String sDBID, String sUserID, String sQuoteNumber, String sRequestedQuoteLine, String sLicenseModuleLevel) throws Exception{
		
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
			s += printQuoteTable(sDBID, sUserID, sQuoteNumber, sRequestedQuoteLine, conn, sLicenseModuleLevel);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588088197]");
			throw new Exception("Error [202004283532] - Error printing quote row - " + e.getMessage());
		}

		return s;
	}
	private String printQuoteTable(
			String sDBID, 
			String sUserID, 
			String sQuoteNumber, 
			String sRequestedQuoteLine,
			Connection conn, 
			String sLicenseModuleLevel) throws Exception{
		String s = "";
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE + "?%24filter="
				+ SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteNumber + "'"
			;
		
		try {
			ql.getQuoteList(sRequest, conn, sDBID, sUserID);
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
					+ "<B>" + ql.getQuoteNames().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}
		
		s += "</TABLE>\n";
		s += "<BR>\n";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">\n";
		
		s += printQuoteLineHeader();
		
		try {
			s += printQuoteLines(conn, sDBID, sUserID, ql.getQuoteIDs().get(0), sRequestedQuoteLine, sLicenseModuleLevel);
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
			+ "Label"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Quantity"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Unit List"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Unit Cost"
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
	private String printQuoteLines(
		Connection conn, 
		String sDBID, 
		String sUserID, 
		String sQuoteID,
		String sRequestedQuoteLine,
		String sLicenseModuleLevel) throws Exception{
		String s = "";
		
		boolean bAllowDisplayItemInformation = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation, 
			sUserID, 
			conn, 
			sLicenseModuleLevel);
		
		//Get the OHDirect connection settings:
		SMOHDirectQuoteLineList ql = new SMOHDirectQuoteLineList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINE + "?$filter=" 
			+ SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER + "%20eq%20'" + sQuoteID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "%20asc"
		;
		
		try {
			ql.getQuoteLineList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [202004273622] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019777]");
		
		BigDecimal bdTotalQuoteCost = new BigDecimal("0.00");
		for(int i = 0; i < ql.getQuoteNumbers().size(); i++) {
			
			//If this is a request to show only a particular line....
			if (sRequestedQuoteLine.compareToIgnoreCase("") != 0) {
				BigDecimal bdRequestedQuoteLine;
				try {
					bdRequestedQuoteLine = new BigDecimal(sRequestedQuoteLine);
				} catch (Exception e) {
					throw new Exception("Error [202006145816] - vendor quote line number '" + sRequestedQuoteLine + "' is invalid.");
				}
				//Then if this line is NOT the one, just keep looping:
				if (ql.getLineNumbers().get(i).compareTo(bdRequestedQuoteLine) != 0) {
					continue;
				}
			}
			
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getLineNumbers().get(i) + "</B"
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
			
			String sItemNumberLink = ql.getLabels().get(i);
			if (bAllowDisplayItemInformation) {
				sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICDisplayItemInformation"
				+ "?ItemNumber=" + sItemNumberLink
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "\">" + sItemNumberLink + "</A>"
			; 
			}else {
				sItemNumberLink = "<B>" + sItemNumberLink + "</B>";
			}
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ sItemNumberLink
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ql.getQuantities().get(i) + "</B"
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ql.getUnitSellingPrices().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ql.getUnitCosts().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ql.getTotalCosts().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			//s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
			//		+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ql.getTotalSellingPrices().get(i)) + "</B"
			//		+ "</TD>" + "\n"
			//	;
			
			bdTotalQuoteCost = bdTotalQuoteCost.add(ql.getTotalCosts().get(i));
			
			//s += printQuoteLineDetails(conn, ql.getQuoteLineIDs().get(i), sDBID, sUserID);
			s += printQuoteLineCostDetails(conn, ql.getQuoteLineIDs().get(i), sDBID, sUserID);
			
			s += "  </TR>" + "\n";
		}
		
		return s;
	}
	
	private String printQuoteLineDetails(Connection conn, String sQuoteLineID, String sDBID, String sUserID) throws Exception{
		String s = "";
		//Get the OHDirect connection settings:
		SMOHDirectQuoteLineDetailList ql = new SMOHDirectQuoteLineDetailList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINEDETAIL 
			+ "?$filter=" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_QUOTELINEID + "%20eq%20'" + sQuoteLineID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER + "%20asc"
		;
		
		try {
			ql.getQuoteLineDetailList(sRequest, conn, sDBID, sUserID);
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

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" COLSPAN=2 >"
					+ "<SPAN style = \" font-weight:bold; \" >"
					+ ql.getDescriptions().get(i)
					+ "</SPAN>"
					+ ": "
					+ "</TD>" + "\n"
				;
			
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" COLSPAN=3 >"
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
	private String printQuoteLineCostDetails(Connection conn, String sQuoteLineID, String sDBID, String sUserID) throws Exception{
		String s = "";
		//Get the OHDirect connection settings:
		SMOHDirectQuoteLineCostDetailList qlcostdetails = new SMOHDirectQuoteLineCostDetailList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINECOSTDETAIL 
			+ "?$filter=" + SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUOTE_LINE_ID + "%20eq%20'" + sQuoteLineID + "'"
			//+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER + "%20asc"
		;
		
		try {
			qlcostdetails.getQuoteLineCostDetailList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [202004273522] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1588019577]");
		
		//Cost details table:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
		s += "    <TD style = \" background-color:white; \" >&nbsp;</TD>" + "\n";  //One blank column to the left
		s += "    <TD COLSPAN = 7 > \n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + " \" style = \" width:100%; \" > \n";
		
		//Headings:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		//Description:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "Description"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		//Quantity:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "Qty"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		//List price:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "List price"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		//Multipliers:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "Multiplier"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		//Base prices:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "Base"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		//Option prices:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<SPAN style = \" font-weight:bold; color:white; \" >"
				+ "Option price"
				+ "</SPAN>"
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		//Print the cost details:
		for(int i = 0; i < qlcostdetails.getQuoteLineDetailIDs().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			
			//Description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ qlcostdetails.getDescriptions().get(i)
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Quantity:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(4, qlcostdetails.getQtys().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//List price:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, qlcostdetails.getListPrices().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Multipliers:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ qlcostdetails.getDiscountMultipliers().get(i)
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Base prices:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, qlcostdetails.getBasePrices().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Option prices:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, qlcostdetails.getOptionPrices().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}

		//Print a line for the overall cost:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\""
				+ " style = \" color:white; font-weight:bold; \""
				+ "COLSPAN=4 >"
				+ "TOTALS:"
				+ "</TD>" + "\n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" "
				+ " style = \" color:white; font-weight:bold; \""
				+ ">"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(qlcostdetails.getTotalBasePrice())
				+ "</B>"
				+ "</TD>" + "\n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" "
				+ " style = \" color:white; font-weight:bold; \""
				+ ">"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(qlcostdetails.getTotalOptionsPrice())
				+ "</B>"
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		//Close the cost details table:
		s += "    </TABLE> \n";
		s += "    </TD> \n";
		s += "  </TR> \n";
		
		return s;
	}
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}
}
