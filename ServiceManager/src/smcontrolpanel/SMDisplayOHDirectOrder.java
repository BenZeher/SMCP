package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOHDirectOrderLineCostDetailList;
import SMClasses.SMOHDirectOrderLineList;
import SMClasses.SMOHDirectOrderList;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayOHDirectOrder extends HttpServlet {

	public static final String ADDITIONAL_PARAMETERS = "ADDITIONALPARAMETERS";
	public static final String ADDITIONAL_PARAMETERS_DELIMITER = "*";
	
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		//TODO Review adding a new permission for viewing orders
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
		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.ORDER_FIELD_ORDERNUMBER, request);
		String sRequestedOrderLine = clsManageRequestParameters.get_Request_Parameter(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LINENUMBER, request);
		String sAdditionalParameters = clsManageRequestParameters.get_Request_Parameter(ADDITIONAL_PARAMETERS, request);

		/*******************************************************/

		out.println(SMUtilities.getMasterStyleSheetLink());
		//Customized title
		String sReportTitle = "View OHDirect Plus Order";
		
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
		out.println( printOrderHeadings());
		long lStartingTime = System.currentTimeMillis();
		try {
			out.println(printOrder(sDBID, sUserID, sOrderNumber, sRequestedOrderLine, sLicenseModuleLevel));
		} catch (Exception e2) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + ServletUtilities.clsServletUtilities.URLEncode(e2.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + sAdditionalParameters.replace(ADDITIONAL_PARAMETERS_DELIMITER, "&")
			);			
			return;
		}
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime) + " ms.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
			
	}
	
	private String printOrderHeadings() {
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + "\" >" + "\n";
		
		s += "    <TD class = \"" +SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL 
				+ "\" style = \" color:white; font-weight:bold; \" >"
			+ "Order #"
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
	private String printOrder(String sDBID, String sUserID, String sOrderNumber, String sRequestedOrderLine, String sLicenseModuleLevel) throws Exception{
		
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
			throw new Exception("Error [1593712663] - getting connection - " + e1.getMessage());
		}
		
		try {
			s += printOrderTable(sDBID, sUserID, sOrderNumber, sRequestedOrderLine, conn, sLicenseModuleLevel);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1593712664]");
			throw new Exception("Error [1593712665] - Error printing order row - " + e.getMessage());
		}

		return s;
	}
	private String printOrderTable(
			String sDBID, 
			String sUserID, 
			String sOrderNumber, 
			String sRequestedOrderLine,
			Connection conn, 
			String sLicenseModuleLevel) throws Exception{
		String s = "";
		
		//Get the OHDirect connection settings:
		SMOHDirectOrderList ol = new SMOHDirectOrderList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_ORDER + "?%24filter="
				+ SMOHDirectFieldDefinitions.ORDER_FIELD_ORDERNUMBER + "%20eq%20'" + sOrderNumber + "'"
			;
		
		try {
			ol.getOrderList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [1593712666] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1593712667]");
		
		for(int i = 0; i < ol.getOrderNumbers().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ol.getOrderNumbers().get(i) + "</B"
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + ol.getCreatedDates().get(i) + "</B"
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ol.getCreatedBys().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ol.getLastModifiedDates().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ol.getLastModifiedBys().get(i) + "</B"
					+ "</TD>" + "\n"
				;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ol.getOrderNames().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}
		
		s += "</TABLE>\n";
		s += "<BR>\n";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">\n";
		
		s += printOrderLineHeader();
		
		try {
			s += printOrderLines(conn, sDBID, sUserID, ol.getOrderIDs().get(0), sRequestedOrderLine, sLicenseModuleLevel);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1593712668]");
			throw new Exception("Error [1593712669] - Error printing quote lines - " + e.getMessage());
		}
		
		s += "</TABLE>" + "\n";
		
		return s;
	}
	private String printOrderLineHeader() {
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
	private String printOrderLines(
		Connection conn, 
		String sDBID, 
		String sUserID, 
		String sQuoteID,
		String sRequestedOrderLine,
		String sLicenseModuleLevel) throws Exception{
		String s = "";
		
		boolean bAllowDisplayItemInformation = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation, 
			sUserID, 
			conn, 
			sLicenseModuleLevel);
		
		//Get the OHDirect connection settings:
		SMOHDirectOrderLineList oll = new SMOHDirectOrderLineList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_ORDERLINE + "?$filter=" 
			+ SMOHDirectFieldDefinitions.ORDERLINE_FIELD_ORDER + "%20eq%20'" + sQuoteID + "'"
			+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LINENUMBER + "%20asc"
		;
		
		try {
			oll.getOrderLineList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [1593712670] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1593712671]");
		
		BigDecimal bdTotalQuoteCost = new BigDecimal("0.00");
		for(int i = 0; i < oll.getOrderNumbers().size(); i++) {
			
			//If this is a request to show only a particular line....
			if (sRequestedOrderLine.compareToIgnoreCase("") != 0) {
				BigDecimal bdRequestedOrderLine;
				try {
					bdRequestedOrderLine = new BigDecimal(sRequestedOrderLine);
				} catch (Exception e) {
					throw new Exception("Error [1593712672] - vendor quote line number '" + sRequestedOrderLine + "' is invalid.");
				}
				//Then if this line is NOT the one, just keep looping:
				if (oll.getLineNumbers().get(i).compareTo(bdRequestedOrderLine) != 0) {
					continue;
				}
			}
			
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + "\" >" + "\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<B>" + oll.getLineNumbers().get(i) + "</B"
				+ "</TD>" + "\n"
			;

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + oll.getDescriptions().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + oll.getLastConfigurationDescriptions().get(i) + "</B"
					+ "</TD>" + "\n"
				;
			
			String sItemNumberLink = oll.getLabels().get(i);
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
				+ "<B>" + oll.getQuantities().get(i) + "</B"
				+ "</TD>" + "\n"
			;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(oll.getUnitCosts().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(oll.getTotalCosts().get(i)) + "</B"
					+ "</TD>" + "\n"
				;
			
			
			bdTotalQuoteCost = bdTotalQuoteCost.add(oll.getTotalCosts().get(i));
			
			//s += printQuoteLineDetails(conn, ql.getQuoteLineIDs().get(i), sDBID, sUserID);
			s += printOrderLineCostDetails(conn, oll.getOrderLineIDs().get(i), sDBID, sUserID);
			
			s += "  </TR>" + "\n";
		}
		
		return s;
	}

	private String printOrderLineCostDetails(Connection conn, String sQuoteLineID, String sDBID, String sUserID) throws Exception{
		String s = "";
		//Get the OHDirect connection settings:
		SMOHDirectOrderLineCostDetailList olcostdetails = new SMOHDirectOrderLineCostDetailList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINECOSTDETAIL 
			+ "?$filter=" + SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUOTE_LINE_ID + "%20eq%20'" + sQuoteLineID + "'"
			//+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER + "%20asc"
		;
		
		try {
			olcostdetails.getOrderLineCostDetailList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [1593712673] - " + e4.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1593712674]");
		
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
				+ "Detail Description"
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
		for(int i = 0; i < olcostdetails.getOrderLineDetailIDs().size(); i++) {
			//Print a row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >" + "\n";
			
			//Description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ olcostdetails.getDescriptions().get(i)
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Quantity:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(4, olcostdetails.getQtys().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//List price:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, olcostdetails.getListPrices().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Multipliers:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ olcostdetails.getDiscountMultipliers().get(i)
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Base prices:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, olcostdetails.getBasePrices().get(i))
					+ "</SPAN>"
					+ "</TD>" + "\n"
				;
			
			//Option prices:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >"
					+ "<SPAN style = \" font-weight:normal; \" >"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, olcostdetails.getOptionPrices().get(i))
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
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(olcostdetails.getTotalBasePrice())
				+ "</B>"
				+ "</TD>" + "\n"
			;
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" "
				+ " style = \" color:white; font-weight:bold; \""
				+ ">"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(olcostdetails.getTotalOptionsPrice())
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
