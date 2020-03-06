package smic;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicinvoiceexportsequences;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICRecreateAPExportEdit  extends HttpServlet {

	public static final String RADIO_BUTTON_GROUP_NAME = "EXPORTGROUP";
	public static final String SUBMIT_BUTTON_NAME = "SUBMIT_BUTTON";
	public static final String SUBMIT_BUTTON_LABEL = "Export selected sequence";
	private static final long serialVersionUID = 1L;
	private int iColumnCount;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"PO Invoice Export Sequence",
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICRecreateAPExportAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICRecreateAPInvoiceExport
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICRecreateAPInvoiceExport)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    smedit.printHeaderTable();
	    smedit.setbIncludeDeleteButton(false);
	    smedit.setbIncludeUpdateButton(false);
		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit), "");
		} catch (SQLException e) {
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
    				+ "?" + ICRecreateAPExportSelection.STARTING_APEXPORTSEQUENCEDATE + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICRecreateAPExportSelection.STARTING_APEXPORTSEQUENCEDATE, smedit.getRequest())
			+ "&" + ICRecreateAPExportSelection.ENDING_APEXPORTSEQUENCEDATE + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICRecreateAPExportSelection.ENDING_APEXPORTSEQUENCEDATE, smedit.getRequest());
    		if (smedit.getRequest().getParameter(ICRecreateAPExportSelection.SHOW_INDIVIDUAL_INVOICES_CHECKBOX) != null){
    			sRedirectString += "&" + ICRecreateAPExportSelection.SHOW_INDIVIDUAL_INVOICES_CHECKBOX + "=Y";
    		}
    		sRedirectString += "&Warning=Error listing exports: " + e.getMessage()
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID();
			response.sendRedirect(sRedirectString);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm) throws SQLException{

		//Get a resultset of all the export records required:
		String sStartingExportDate = clsManageRequestParameters.get_Request_Parameter(ICRecreateAPExportSelection.STARTING_APEXPORTSEQUENCEDATE, sm.getRequest());
		String sEndingExportDate = clsManageRequestParameters.get_Request_Parameter(ICRecreateAPExportSelection.ENDING_APEXPORTSEQUENCEDATE, sm.getRequest());
		
		try {
			sStartingExportDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingExportDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			throw new SQLException("Error:[1423766549] Invalid starting export date: '" + sStartingExportDate + "' - " + e.getMessage());
		}
		try {
			sEndingExportDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingExportDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			throw new SQLException("Error:[1423766550] Invalid ending export date: '" + sEndingExportDate + "' - " + e.getMessage());
		}
		boolean bShowInvoices = sm.getRequest().getParameter(ICRecreateAPExportSelection.SHOW_INDIVIDUAL_INVOICES_CHECKBOX) != null;
		String SQL = "SELECT"
			+ " " + SMTableicinvoiceexportsequences.TableName + ".*"
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.bdinvoicetotal
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datinvoice
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sdescription
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sinvoicenumber
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendor
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendorname
			+ " FROM "
			+ SMTableicinvoiceexportsequences.TableName + " LEFT JOIN " + SMTableicpoinvoiceheaders.TableName
			+ " ON " + SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid + " = "
			+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lexportsequencenumber
			+ " WHERE ("
				+ "(" + SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.datexported 
				+ " >= '" + sStartingExportDate + "')"
				+ " AND (" + SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.datexported 
				+ " <= '" + sEndingExportDate + " 23:59:59')"
			+ ")"
			+ " ORDER BY " + SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid
			+ ", " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
		;
		String s = "";
		//We need to create a table with radio buttons for each of the export sequences, and, if the use chose to show individual invoices
		//in the export sequence, then we'll have a sub table for each of those, too:
		s+= sStyleScripts();
	
		//Open outer table:
		s += "<TABLE class = \" sequence \" \" width=100% >\n";
		//s += writeSequenceHeaderRow();
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".getEditHTML.get export sequences - user: " + sm.getUserName()
			);
			long lLastSequenceID = -1;
			while (rs.next()){
				//Write a row for each export sequence:
				long lCurrentSequenceID = rs.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid);
				if (lCurrentSequenceID != lLastSequenceID){
					if (lLastSequenceID == -1){
						s += writeSequenceRow(rs, true);
					}else{
						s += writeSequenceRow(rs, false);						
					}
				}
				
				//If the user chose to show the invoices also, write all the invoice rows:
				if (bShowInvoices){
					s += writeInvoiceBox(rs);
				}
				
				//Update the last sequence ID:
				lLastSequenceID = lCurrentSequenceID;
			}
		} catch (Exception e1) {
			s += "</TABLE>";
			throw new SQLException("Error reading export sequences - " + e1.getMessage());
		}
		
		//Close outer table:
		s += "</TABLE>";
		
		s += "<BR><BR><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>";
		return s;
	}
	private String writeSequenceRow(ResultSet rsSequence, boolean bChecked) throws SQLException {
		String s = "";
		s += " <TR>";
		iColumnCount = 0;
		//Export Sequence
		s += "<TD class=\"sequencefieldcenteraligned\" >"
				+ "Export Sequence #:&nbsp;"
				+ Long.toString(rsSequence.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid))
				+ "</TD>";
		iColumnCount++;
		//Export date:
		s += "<TD class=\"sequencefieldcenteraligned\" >"
				+ "Export date:&nbsp;"
				+ clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rsSequence.getString(
				SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.datexported))
				+ "</TD>";
		iColumnCount++;
		//Exported by:
		s += "<TD class=\"sequencefieldleftaligned\" >"
				+ "Originally exported by:&nbsp;"
				+ rsSequence.getString(
				SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.suserfullname)
				+ "</TD>";
		iColumnCount++;
		//Radio button for export:
		String sChecked = "";
		if (bChecked){
			sChecked = " CHECKED";
		}
		s += "<TD class=\"sequencefieldcenteraligned\" >"
			+ "Re-create this export?&nbsp;"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" 
				+ RADIO_BUTTON_GROUP_NAME + "\" VALUE='" 
				+ Long.toString(rsSequence.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid)) 
				+ "' "
				+ sChecked
				+ ">" 
			+ "</TD>";
		iColumnCount++;
		s += "</TR>";
		return s;
	}
	private String writeInvoiceBox(ResultSet rs) throws SQLException{
		String s = "";
		s += " <TR>";
		s += "<TD COLSPAN=" + Integer.toString(iColumnCount) + ">";

		//Now create the inner table for the invoices:
		try {
			s += createInvoiceTable(rs);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		s += "</TD>";
		s += " </TR>";
		return s;
		
	}
	private String createInvoiceTable(ResultSet rs) throws SQLException{
		String s = "";
		s += "<TABLE class = \" invoice \" \" width=100% >\n";
		s += writeInvoiceHeaderRow();
		long lCurrentExportSequence = rs.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid);
		//While the invoices are all for the same export sequence, keep listing them:
		try {
			//Use the current record to write the first line:
			s += writeInvoiceLine(rs);
			//Keep writing as long as we are still on the same export sequence:
			while (rs.next()){
				if (rs.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid) 
						== lCurrentExportSequence){
					s += writeInvoiceLine(rs);
				}else{
					//Get back to the previous row so the outer 'while' loop can increment properly:
					rs.previous();
					break;
				}
			}
		} catch (Exception e) {
			throw new SQLException("Error reading invoice header - " + e.getMessage());
		}
		
		s += "</TABLE>";
		return s;
	}
	private String writeInvoiceHeaderRow(){
		String s = "";
		s += " <TR>";
		s += "<TH class=\"invoicelineheadingright\" >Export Sequence</TH>";
		s += "<TH class=\"invoicelineheadingright\" >Invoice ID</TH>";
		s += "<TH class=\"invoicelineheadingleft\" >Vendor's invoice #</TH>";
		s += "<TH class=\"invoicelineheadingleft\" >Invoice date</TH>";
		s += "<TH class=\"invoicelineheadingleft\" >Vendor</TH>";
		s += "<TH class=\"invoicelineheadingleft\" >Description</TH>";
		s += "<TH class=\"invoicelineheadingright\" >Total</TH>";
		s += " </TR>";
		return s;
	}
	private String writeInvoiceLine(ResultSet rs) throws SQLException {
		String s = "";
		//In case there is a sequence with no invoices in it, then some of these fields could be null:
		if (rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datinvoice) == null){
			return s;
		}
		s += " <TR>";
		try {
			s += "<TD class=\"invoicefieldrightaligned\" >" 
					+ Long.toString(rs.getLong(SMTableicinvoiceexportsequences.TableName + "." + SMTableicinvoiceexportsequences.lid)) + "</TD>";
			s += "<TD class=\"invoicefieldrightaligned\" >" 
					+ Long.toString(rs.getLong(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid)) + "</TD>";
			s += "<TD class=\"invoicefieldleftaligned\" >" 
					+ rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sinvoicenumber) + "</TD>";
			s += "<TD class=\"invoicefieldleftaligned\" >" 
					+ clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datinvoice)) + "</TD>";
			s += "<TD class=\"invoicefieldleftaligned\" >" 
					+ rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendor) + "</TD>";
			s += "<TD class=\"invoicefieldleftaligned\" >" 
					+ rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sdescription) + "</TD>";
			s += "<TD class=\"invoicefieldrightaligned\" >" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					rs.getBigDecimal(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.bdinvoicetotal)) + "</TD>";
			s += " </TR>";
		} catch (Exception e) {
			throw new SQLException("Error reading invoice fields - " + e.getMessage());
		}
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		//String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
			"table.sequence {"
			+ "border-width: " + "1" + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			//+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: white; "
			+ "background-color: black; "
			+ "}"
			+ "\n"
			;

		//This is the def for a left aligned field:
		s +=
			"td.sequencefieldleftaligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for a right aligned field:
		s +=
			"td.sequencefieldrightaligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned field:
		s +=
			"td.sequencefieldcenteraligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;		
		
		//This is the def for the sequence lines heading:
		s +=
			"th.sequencelineheadingleft {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.sequencelineheadingright {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.sequencelineheadingcenter {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;	
		
		//This is the table def for the invoices:
		s +=
				"table.invoice {"
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
				+ "background-color: white; "
				+ "}"
				+ "\n"
				;
		
		//This is the def for the invoice lines heading:
		s +=
			"th.invoicelineheadingleft {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.invoicelineheadingright {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.invoicelineheadingcenter {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;	
		
		//This is the def for a left aligned invoice field:
		s +=
			"td.invoicefieldleftaligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for a right aligned invoice field:
		s +=
			"td.invoicefieldrightaligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned invoice field:
		s +=
			"td.invoicefieldcenteraligned {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					//+ "background-color: #D3D3D3; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
