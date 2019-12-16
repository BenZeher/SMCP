package smcontrolpanel;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smar.ARCustomer;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMOrderDetailList  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DARK_BG_COLOR = "DCDCDC";
	private static final String LIGHT_BG_COLOR = "FFFFFF";
	private static final String LINECOMMANDS_BG_COLOR = "#99CCFF";
	private static final String COSTVALUES_COLOR = "#120991";
	public static final String LINEDETAILBASE = "LINEDETAIL";
	public static final String LINENUMBERMOVEMARKER = "MOVELINENUMBER";
	public static final String LINECOMMANDHEADER_BUTTON = "LINECOMMANDHEADER";
	public static final String LINECOMMANDHEADER_LABEL = "<B><FONT COLOR=RED>H</FONT></B>eader"; //H
	public static final String LINECOMMANDTOTALS_BUTTON = "LINECOMMANDTOTALS";
	public static final String LINECOMMANDTOTALS_LABEL = "Dis<B><FONT COLOR=RED>c</FONT></B>ount"; //C
	public static final String LINECOMMANDIMPORTWORKORDERS_BUTTON = "LINECOMMANDIMPORTWORKORDERS";
	public static final String LINECOMMANDIMPORTWORKORDERS_LABEL = "Import <B><FONT COLOR=RED>w</FONT></B>ork orders"; //W
	public static final String ORDERINVOICE_BUTTON_LABEL = "<B><FONT COLOR=RED>I</FONT></B>nvoice"; //I
	public static final String LINECOMMANDDELETE_BUTTON = "LINECOMMANDDELETE";
	public static final String LINECOMMANDDELETE_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete selected"; //D
	public static final String LINECOMMANDMOVE_BUTTON = "LINECOMMANDMOVE";
	public static final String LINECOMMANDSHIP_BUTTON = "LINECOMMANDSHIP";
	public static final String LINECOMMANDSHIP_LABEL = "<B><FONT COLOR=RED>S</FONT></B>hip selected"; //S
	public static final String LINECOMMANDUNSHIP_BUTTON = "LINECOMMANDUNSHIP";
	public static final String LINECOMMANDUNSHIP_LABEL = "<B><FONT COLOR=RED>U</FONT></B>n-ship selected"; //U
	public static final String LINECOMMANDADDLINE_BUTTON = "LINECOMMANDADDLINE";
	public static final String LINECOMMANDADDLINE_LABEL = "<B><FONT COLOR=RED>A</FONT></B>dd new"; //A
	public static final String LINECOMMANDINSERTLINE_BUTTON = "LINECOMMANDINSERTLINE";
	public static final String LINECOMMANDINSERTLINE_LABEL = "I<B><FONT COLOR=RED>n</FONT></B>sert above selected"; //N
	public static final String LINECOMMANDMOVE_VALUE = "LINECOMMANDMOVE";
	public static final String LINECOMMANDMOVEABOVE_BUTTON = "LINECOMMANDMOVEABOVE";
	public static final String LINECOMMANDMOVEABOVE_LABEL = "<B><FONT COLOR=RED>M</FONT></B>ove selected ABOVE"; //M
	public static final String MOVEABOVESELECT_NAME = "MOVEABOVESELECT";
	public static final String LINECOMMANDCOPYABOVE_BUTTON = "LINECOMMANDCOPYABOVE";
	public static final String LINECOMMANDCOPYABOVE_LABEL = "Copy selected A<B><FONT COLOR=RED>B</FONT></B>OVE"; //B
	public static final String COPYABOVESELECT_NAME = "COPYABOVESELECT";
	public static final String LINECOMMANDSETMECHANIC_BUTTON = "LINECOMMANDSETMECHANIC";
	public static final String LINECOMMANDSETMECHANIC_LABEL = "S<B><FONT COLOR=RED>e</FONT></B>t technician"; //E
	public static final String LINECOMMANDSETMECHANICNSHIP_BUTTON = "LINECOMMANDSETMECHANICNSHIP";
	public static final String LINECOMMANDSETMECHANICNSHIP_LABEL = "Set technician and shi<B><FONT COLOR=RED>p</FONT></B>"; //P
	public static final String SETMECHANICIDSELECT_NAME = "SETMECHANICIDSELECT";
	public static final String LINECOMMANDCREATEITEM_BUTTON = "LINECOMMANDCREATEITEM";
	public static final String LINECOMMANDCREATEITEM_LABEL = "Crea<B><FONT COLOR=RED>t</FONT></B>e item"; //T
	public static final String LINECOMMANDCREATEPO_BUTTON = "LINECOMMANDCREATEPO";
	public static final String LINECOMMANDCREATEPO_LABEL = "Create P<B><FONT COLOR=RED>O</FONT></B>"; //O
	public static final String LINECOMMANDDIRECTENTRY_BUTTON = "LINECOMMANDDIRECTENTRY";
	public static final String LINECOMMANDDIRECTENTRY_LABEL = "Direct entr<B><FONT COLOR=RED>y</FONT></B>"; //Y
	public static final String LINECOMMANDPASTEINTOPROPOSAL_BUTTON = "LINECOMMANDPASTEINTOPROPOSAL";
	public static final String LINECOMMANDPASTEINTOPROPOSAL_LABEL = "Trans<B><FONT COLOR=RED>f</FONT></B>er selected into proposal"; //F
	public static final String PROPOSALEXISTS_PARAM = "PROPOSALEXISTS";
	public static final String LINECOMMANDGOTOPROPOSAL_BUTTON = "LINECOMMANDGOTOPROPOSAL";
	public static final String LINECOMMANDGOTOPROPOSAL_LABEL = "Proposa<B><FONT COLOR=RED>l</FONT></B>"; //L
	
	//Reprice commands for quotes:
	public static final String LINECOMMANDREPRICE_BUTTON = "LINECOMMANDREPRICE";
	public static final String LINECOMMANDREPRICE_LABEL = "<B><FONT COLOR=RED>R</FONT></B>eprice selected lines"; //R
	public static final String REPRICESELECT_NAME = "REPRICESELECT";
	
	//Repricing methods:
	public static final int REPRICEUSINGMARKUP = 0;
	public static final int REPRICEUSINGMULTIPLIER = 1;
	public static final int REPRICEUSINGMUPERLABORUNIT = 2;
	public static final int REPRICEUSINGMARGIN = 3;
	public static final int REPRICEUSINGTOTALPRICE = 4;
	
	//Repricing labels:
	public static final String REPRICEUSINGMARKUPDESC = "To arrive at a TOTAL mark up amount of:";
	public static final String REPRICEUSINGMULTIPLIERDESC = "To arrive at a TOTAL cost multiplier of:";
	public static final String REPRICEUSINGLABORUNITSDESC = "To arrive at a TOTAL mark up per labor unit of:";
	public static final String REPRICEUSINGMARGINDESC = "To arrive at a TOTAL gross profit percentage of:";
	public static final String REPRICEUSINGTOTALPRICEDESC = "To arrive at a TOTAL price of:";
	
	//Reprice amount text box:
	public static final String REPRICEAMOUNT_PARAM = "REPRICEAMOUNT_PARAM";
	
	//Invoice state change commands
	public static final String INVOICECOMMANDUPDATESTATE_BUTTON = "INVOICECOMMANDUPDATESTATE";
	public static final String INVOICECOMMANDUPDATESTATE_LABEL = "Update Invoicing State";
	
	public static final String LINECOMMAND_FLAG = "LINECOMMANDFLAG";
	public static final String NUMBEROFLINES_FLAG = "NUMBEROFLINESFLAG";
	public static final String LASTDETAILNUMBEREDITED_PARAM = "LASTDETAILNUMBEREDITED";
	private static final String HIGHLIGHTROWCOLOR = "#66ff99";
	private static final int NUMBEROFRECORDSTOBUFFER = 25;
	
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);

		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Order details",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMOrderDetailListAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditOrders
		);

		//Make sure we remove any leftover order detail objects:
		try {
			smedit.getCurrentSession().removeAttribute(SMOrderDetail.ParamObjectName);
		} catch (Exception e1) {
			// No problem if there's no session, then the attribute doesn't exist anyway.
		}
		
		String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
			+ clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*");
		if (bDebugMode){
			System.out.println("In " + this.toString() 
				+ "sCurrentCompleteURL = " + sCurrentCompleteURL);
		}

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(
			SMTableorderheaders.strimmedordernumber, smedit.getRequest());
		
		//Record this URL so we can return to it later:
		smedit.addToURLHistory("Editing Details for Order Number " + sTrimmedOrderNumber);

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				smedit.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " 
				+ smedit.getUserID()
				+ " - "
				+ smedit.getFullUserName());
		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
					+ sTrimmedOrderNumber
					+ "&Warning=" + "Could not get data connection."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}

		smedit.setTitle("Details list");
		smedit.printLowProfileHeaderTable();
		smedit.getPWOut().println(clsServletUtilities.getJQueryIncludeString());
		smedit.getPWOut().println(clsServletUtilities.getJQueryUIIncludeString());
		smedit.getPWOut().println(clsServletUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Manage orders</A>");
		
		String sLinkToWorkOrderList = "";
		if (SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation, 
			smedit.getUserID(), 
			getServletContext(), 
			smedit.getsDBID(),
			(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			sLinkToWorkOrderList = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sTrimmedOrderNumber 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "#WorkOrders"
				+ "\">" + "Work order list" + "</A>"
			;
		}
		if (sLinkToWorkOrderList.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;" + sLinkToWorkOrderList);
		}
		
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");

		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);
		try {
			smedit.createEditPage(getEditHTML(
				smedit, 
				sTrimmedOrderNumber,
				conn),
				""
			);
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080592]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + sTrimmedOrderNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					+ "&Warning=" + clsServletUtilities.URLEncode(sError)
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080593]");
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, String sTrimmedOrderNumber, Connection conn) throws SQLException{

		String s = "";
		
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sTrimmedOrderNumber);
		if (!order.load(conn)){
			s += " Error loading order " + sTrimmedOrderNumber + " - " + order.getErrorMessages();
			return s;
		}
		String sObjectName = "Order";
		if (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sObjectName = SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE);
		}
		ARCustomer cus = new ARCustomer(order.getM_sCustomerCode());
		if (order.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			if (!cus.load(conn)){
				s += " Error loading customer " + order.getM_sCustomerCode() + " - " + cus.getErrorMessageString();
				return s;
			}
		}
		String sOnHold = "";
		if (cus.getM_iOnHold().compareToIgnoreCase("1") == 0){
			sOnHold = "&nbsp;<FONT COLOR=RED><B>ON HOLD</B></FONT>";
		}
		
		s += "<B>" + sObjectName + " number:</B>&nbsp;" 
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sTrimmedOrderNumber 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sTrimmedOrderNumber + "</A>"
				+ "&nbsp;<B>" + sObjectName + " date:</B>&nbsp" + order.getM_datOrderDate()
				//+ "<BR><span style = \" font-size: small; \">"
				;
		if (order.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			s += "&nbsp;<B>Customer:</B>&nbsp;" 
				+ order.getM_sCustomerCode()
				+ sOnHold
				;
		} 
		
		s += sCommandScripts(sTrimmedOrderNumber,
				clsManageRequestParameters.get_Request_Parameter(LASTDETAILNUMBEREDITED_PARAM, sm.getRequest()), 
				conn);
		s += sStyleScripts();
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramstrimmedordernumber + "\" VALUE=\"" 
			+ sTrimmedOrderNumber + "\">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINECOMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINECOMMAND_FLAG + "\""
		+ "\">";
		
		String SQL = "SELECT count(*) FROM " + SMTableorderdetails.TableName
			+ " WHERE ("
				+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
			+ ")"
		;
		long lNumberOfLines = 0;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				lNumberOfLines = rs.getLong(1); 
			}else{
				throw new SQLException("Could not get count of order detail lines.");
			}
			rs.close();
		} catch (Exception e1) {
			throw new SQLException("Error reading count of detail lines with SQL: " + SQL + " - " + e1.getMessage());
		}
		
		//Store the number of lines for later:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + NUMBEROFLINES_FLAG + "\" VALUE=\"" + Long.toString(lNumberOfLines) + "\""
		+ " id=\"" + NUMBEROFLINES_FLAG + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsCustomerCode + "\" VALUE=\"" 
			+ order.getM_sCustomerCode() + "\">";
		
		//Store the tax ID so we can check to see if it's valid when we go to invoice:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramitaxid + "\" VALUE=\"" 
				+ order.getitaxid() + "\">";
		
		//Store whether a proposal exists or not here:
		SQL = "SELECT"
			+ " " + SMTableproposals.strimmedordernumber
			+ " FROM " + SMTableproposals.TableName
			+ " WHERE ("
				+ SMTableproposals.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'"
			+ ")"
		;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					s += "<INPUT TYPE=HIDDEN NAME=\"" + PROPOSALEXISTS_PARAM + "\""
							+ " id=\"" + PROPOSALEXISTS_PARAM + "\""
							+ " VALUE=\"" + "Y" + "\">";
				}else{
					s += "<INPUT TYPE=HIDDEN NAME=\"" + PROPOSALEXISTS_PARAM + "\""
							+ " id=\"" + PROPOSALEXISTS_PARAM + "\""
							+ " VALUE=\"" + "" + "\">";
				}
				rs.close();
			} catch (Exception e1) {
				throw new SQLException("Error reading proposal table with SQL: " + SQL + " - " + e1.getMessage());
			}
		
		//Place the line commands here:
		s += createLineCommandsTable(
				lNumberOfLines, 
				true, 
				true, 
				conn, 
				sm.getUserID(), 
				order,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Create the order lines:
		try {
			s += listOrderLines(
					order,
					conn,
					sm.getUserID(),
					sTrimmedOrderNumber,
					sm.getsDBID(),
					sm.getRequest(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		} catch (SQLException e) {
			throw e;
		}

		//Place the line commands at the end, too:
		s += createLineCommandsTable(
				lNumberOfLines, 
				false, 
				false, 
				conn, 
				sm.getUserID(), 
				order,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));

		//Place the totals at the end:
		if (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
			s += buildTotalsTable(
					order, 
					sm.getsDBID(), 
					sm.getUserID(), 
					conn,
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		}
		return s;
	}
	
	private String createLineCommandsTable(
			long lNumberOfLines, 
			boolean bIncludeCopyLineButton, 
			boolean bIncludeRepriceButton,
			Connection conn, 
			String sUserID,
			SMOrderHeader order,
			String sLicenseModuleLevel) throws SQLException{
		String s = "";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:LineCommands; background-color: "
			+ LINECOMMANDS_BG_COLOR + "; \" width=100% >\n";
		boolean bAllowProposalEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditProposals, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		s += "<TR>";
		s += "<TD style = \"text-align: left; \" >";
		s += "<B><I>Go to:</I></B>";

		s += "<button type=\"button\""
			+ " value=\"" + LINECOMMANDHEADER_LABEL + "\""
			+ " name=\"" + LINECOMMANDHEADER_BUTTON + "\""
			+ " onClick=\"gotoHeader();\">"
			+ LINECOMMANDHEADER_LABEL
			+ "</button>\n"
			
			+ " <button type=\"button\""
			+ " value=\"" + LINECOMMANDTOTALS_LABEL + "\""
			+ " name=\"" + LINECOMMANDTOTALS_BUTTON + "\""
			+ " onClick=\"gotoTotals();\">"
			+ LINECOMMANDTOTALS_LABEL
			+ "</button>\n"
			;
		
			if (bAllowProposalEdit){
				s += " <button type=\"button\""
					+ " value=\"" + LINECOMMANDGOTOPROPOSAL_LABEL + "\""
					+ " name=\"" + LINECOMMANDGOTOPROPOSAL_BUTTON + "\""
					+ " onClick=\"gotoProposal();\">"
					+ LINECOMMANDGOTOPROPOSAL_LABEL
					+ "</button>\n"
				;
			}

		boolean bIsQuote = order.getM_iOrderType().compareToIgnoreCase(
				Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0;
		if (!bIsQuote){
			s += " <button type=\"button\""
				+ " value=\"" + LINECOMMANDIMPORTWORKORDERS_LABEL + "\""
				+ " name=\"" + LINECOMMANDIMPORTWORKORDERS_BUTTON + "\""
				+ " onClick=\"gotoImportWorkOrders();\">"
				+ LINECOMMANDIMPORTWORKORDERS_LABEL
				+ "</button>\n"
			;
			boolean bAllowCreateInvoice = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMCreateInvoices, 
					sUserID, 
					conn,
					sLicenseModuleLevel);
			if (bAllowCreateInvoice){
				s += " <button type=\"button\""
				+ " value=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
				+ " name=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
				+ " onClick=\"createInvoice();\">"
				+ ORDERINVOICE_BUTTON_LABEL
				+ "</button>\n";
			}
		}		
		s += "</TD></TR>"
		;
		
		s += "<TR>";
		s += "<TD style = \"text-align: left; \" >";
		s += "<B><I>Line commands:&nbsp;</I></B>"
		
		+ " <button type=\"button\""
		+ " value=\"" + LINECOMMANDADDLINE_LABEL + "\""
		+ " name=\"" + LINECOMMANDADDLINE_BUTTON + "\""
		+ " onClick=\"addLine();\">"
		+ LINECOMMANDADDLINE_LABEL
		+ "</button>\n"
		
		+ " <button type=\"button\""
		+ " value=\"" + LINECOMMANDINSERTLINE_LABEL + "\""
		+ " name=\"" + LINECOMMANDINSERTLINE_BUTTON + "\""
		+ " onClick=\"insertLine();\">"
		+ LINECOMMANDINSERTLINE_LABEL
		+ "</button>\n"
		
		+ " <button type=\"button\""
		+ " value=\"" + LINECOMMANDDELETE_LABEL + "\""
		+ " name=\"" + LINECOMMANDDELETE_BUTTON + "\""
		+ " onClick=\"deleteLines();\">"
		+ LINECOMMANDDELETE_LABEL
		+ "</button>\n"
		;
		if (!bIsQuote){
			s += " <button type=\"button\""
			+ " value=\"" + LINECOMMANDSHIP_LABEL + "\""
			+ " name=\"" + LINECOMMANDSHIP_BUTTON + "\""
			+ " onClick=\"shipLines();\">"
			+ LINECOMMANDSHIP_LABEL
			+ "</button>\n"
			
			+ " <button type=\"button\""
			+ " value=\"" + LINECOMMANDUNSHIP_LABEL + "\""
			+ " name=\"" + LINECOMMANDUNSHIP_BUTTON + "\""
			+ " onClick=\"unshipLines();\">"
			+ LINECOMMANDUNSHIP_LABEL
			+ "</button>\n";
		}
		
		//Add button to create items:
		boolean bAllowCreateItems = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateItemsFromOrderDetails, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		if (!bIsQuote && bAllowCreateItems){
			s += " <button type=\"button\""
			+ " value=\"" + LINECOMMANDCREATEITEM_LABEL + "\""
			+ " name=\"" + LINECOMMANDCREATEITEM_BUTTON + "\""
			+ " onClick=\"createItems();\">"
			+ LINECOMMANDCREATEITEM_LABEL
			+ "</button>\n";
		}
		//Add button to create a PO:
		boolean bAllowCreatePOs = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreatePOsFromOrderDetails, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		if (!bIsQuote && bAllowCreatePOs){
			s += " <button type=\"button\""
			+ " value=\"" + LINECOMMANDCREATEPO_LABEL + "\""
			+ " name=\"" + LINECOMMANDCREATEPO_BUTTON + "\""
			+ " onClick=\"createPO();\">"
			+ LINECOMMANDCREATEPO_LABEL
			+ "</button>\n";
		}
		//Add button for direct item entry:
		boolean bAllowDirectItemEntry = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMDirectItemEntry, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		if (bAllowDirectItemEntry){
			s += " <button type=\"button\""
			+ " value=\"" + LINECOMMANDDIRECTENTRY_LABEL + "\""
			+ " name=\"" + LINECOMMANDDIRECTENTRY_BUTTON + "\""
			+ " onClick=\"directEntry();\">"
			+ LINECOMMANDDIRECTENTRY_LABEL
			+ "</button>\n";
		}

		//Add button to move lines
		if (bIncludeCopyLineButton){
			s += "<BR><button type=\"button\""
			+ " value=\"" + LINECOMMANDMOVEABOVE_LABEL + "\""
			+ " name=\"" + LINECOMMANDMOVEABOVE_BUTTON + "\""
			+ " onClick=\"moveAbove();\">"
			+ LINECOMMANDMOVEABOVE_LABEL
			+ "</button>\n";
		
			//Create a drop down for the lines to 'move above':
			s += "<SELECT "
				+ " NAME = \"" + MOVEABOVESELECT_NAME + "\""
				+ " ID = \"" + MOVEABOVESELECT_NAME + "\""
				+ ">"
			;
			for (long l = 0; l < lNumberOfLines; l++){
				s += "<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
					+ ">Line " + Long.toString(l + 1)
					+ "</OPTION>"
				;
			}
			
			s += "</SELECT>";
		}
		
		//TODO LTO 20140214
		//Add button to copy lines
		if (bIncludeCopyLineButton){
			s += "&nbsp;<button type=\"button\""
			+ " value=\"" + LINECOMMANDCOPYABOVE_LABEL + "\""
			+ " name=\"" + LINECOMMANDCOPYABOVE_BUTTON + "\""
			+ " onClick=\"copyAbove();\">"
			+ LINECOMMANDCOPYABOVE_LABEL
			+ "</button>\n";
		
			//Create a drop down for the lines to 'copy above':
			s += "<SELECT "
				+ " NAME = \"" + COPYABOVESELECT_NAME + "\""
				+ " ID = \"" + COPYABOVESELECT_NAME + "\""
				+ ">"
			;
			for (long l = 0; l < lNumberOfLines; l++){
				s += "<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
					+ ">Line " + Long.toString(l + 1)
					+ "</OPTION>"
				;
			}
			
			s += "</SELECT>";
		}

		if (bIncludeCopyLineButton && (!bIsQuote)){
			s += "&nbsp;&nbsp;&nbsp;";
			
			//Create a drop down for the lines to 'move above':
			s += "<SELECT "
				+ " NAME = \"" + SETMECHANICIDSELECT_NAME + "\""
				+ " ID = \"" + SETMECHANICIDSELECT_NAME + "\""
				+ ">"
			;
			//get list of mechanics
			try{
				String SQL = "SELECT * FROM " + SMTablemechanics.TableName + " ORDER BY " + SMTablemechanics.sMechInitial;
				ResultSet rsMechanics = clsDatabaseFunctions.openResultSet(SQL, conn);
				s += "<OPTION VALUE = \"" + "0" + "\">" + "** NO TECHNICIAN SELECTED **" + "</OPTION>";
				while(rsMechanics.next()){
					s += "<OPTION VALUE = \"" + Long.toString(rsMechanics.getLong(SMTablemechanics.lid)) + "\""
						+ ">" + rsMechanics.getString(SMTablemechanics.sMechInitial) + " - "
						+ rsMechanics.getString(SMTablemechanics.sMechFullName)
						+ "</OPTION>"
					;
				}
				rsMechanics.close();
			}catch (SQLException e){
				throw e;
			}
			s += "</SELECT>";
			
			//button for setting mechanics for selected lines.
			s += "&nbsp;"
			+ "<button"
			+ " type=\"button\""
			+ " value=\"" + LINECOMMANDSETMECHANIC_LABEL + "\""
			+ " name=\"" + LINECOMMANDSETMECHANIC_BUTTON + "\""
			+ " onClick=\"setMechanic();\">"
			+ LINECOMMANDSETMECHANIC_LABEL
			+ "</button>\n";
			
			//button for setting mechanics for selected lines and ship at the same time.
				s += " <button type=\"button\""
				+ " value=\"" + LINECOMMANDSETMECHANICNSHIP_LABEL + "\""
				+ " name=\"" + LINECOMMANDSETMECHANICNSHIP_BUTTON + "\""
				+ " onClick=\"setMechanicAndShip();\">"
				+ LINECOMMANDSETMECHANICNSHIP_LABEL
				+ "</button>\n";
		}

		//Add the repricing controls:
		if (bIsQuote && bIncludeRepriceButton){
			s += " <button type=\"button\""
			+ " value=\"" + LINECOMMANDREPRICE_LABEL + "\""
			+ " name=\"" + LINECOMMANDREPRICE_BUTTON + "\""
			+ " onClick=\"repriceItems();\">"
			+ LINECOMMANDREPRICE_LABEL
			+ "</button>\n";
		
			//Create a drop down for the repricing methods:
			s += "<SELECT "
				+ " NAME = \"" + REPRICESELECT_NAME + "\""
				+ " ID = \"" + REPRICESELECT_NAME + "\""
				+ ">"
			;
			//create list of methods:
			s += "<OPTION VALUE = \"" + Integer.toString(REPRICEUSINGMUPERLABORUNIT) + "\">" + REPRICEUSINGLABORUNITSDESC + "</OPTION>";
			s += "<OPTION VALUE = \"" + Integer.toString(REPRICEUSINGMARKUP) + "\">" + REPRICEUSINGMARKUPDESC + "</OPTION>";
			s += "<OPTION VALUE = \"" + Integer.toString(REPRICEUSINGMULTIPLIER) + "\">" + REPRICEUSINGMULTIPLIERDESC + "</OPTION>";
			s += "<OPTION VALUE = \"" + Integer.toString(REPRICEUSINGMARGIN) + "\">" + REPRICEUSINGMARGINDESC + "</OPTION>";
			s += "<OPTION VALUE = \"" + Integer.toString(REPRICEUSINGTOTALPRICE) + "\">" + REPRICEUSINGTOTALPRICEDESC + "</OPTION>";
			
			s += "</SELECT>";

			s += "<INPUT TYPE=TEXT NAME=\"" + REPRICEAMOUNT_PARAM + "\""
				+ " id = \"" + REPRICEAMOUNT_PARAM + "\""
				+ " VALUE=\"" + "0.00" + "\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + "13"
			+ ">See note <a href=\"#Footnote1\"><SUP>1</SUP></a> below"
			;
			
			//Add button to paste items into a proposal:
			if (bAllowProposalEdit){
				s += " <button type=\"button\""
				+ " value=\"" + LINECOMMANDPASTEINTOPROPOSAL_LABEL + "\""
				+ " name=\"" + LINECOMMANDPASTEINTOPROPOSAL_BUTTON + "\""
				+ " onClick=\"pasteIntoProposal();\">"
				+ LINECOMMANDPASTEINTOPROPOSAL_LABEL
				+ "</button>\n";
			}
		}
		
		s += "</TD>";
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style=\" title:ENDLineCommands; \">\n";
		return s;
	}
	private String listOrderLines(
			SMOrderHeader order,
			Connection conn,
			String sUserID,
			String sTrimmedOrderNumber,
			String sDBID,
			HttpServletRequest req,
			String sLicenseModuleLevel
	) throws SQLException {
		String s = "";
		String sBackgroundColor = "";
		String sCellStyle = "";
		
		boolean bIsQuote = order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0;
		
		boolean bAllowItemViewing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICDisplayItemInformation, 
					sUserID,
					conn,
					sLicenseModuleLevel
			);
		try{
			String SQL = "SELECT"
				+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.bdEstimatedUnitCost
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemCategory
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sMechInitial
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.isuppressdetailoninvoice
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
				+ ", " + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem
				+ ", " + SMTableicitems.TableName + "." + SMTableicitems.ilaboritem
				+ ", " + SMTableicitems.TableName + "." + SMTableicitems.isuppressitemqtylookup
				+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN "
				+ SMTableicitemlocations.TableName + " ON ((" + SMTableorderdetails.TableName + "." 
				+ SMTableorderdetails.sItemNumber + " = " + SMTableicitemlocations.TableName + "." 
				+ SMTableicitemlocations.sItemNumber + ") AND (" + SMTableorderdetails.TableName + "." 
				+ SMTableorderdetails.sLocationCode + " = " + SMTableicitemlocations.TableName + "." 
				+ SMTableicitemlocations.sLocation + "))"
				+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableorderdetails.TableName
				+ "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "."
				+ SMTableicitems.sItemNumber
			+ " WHERE ("
			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber 
				+ " = '" + sTrimmedOrderNumber + "'"
			+ ")"
			+ " ORDER BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber
			;
			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(SQL, conn);

			//TODO - make this a scrollable table:
			//http://anaturb.net/csstips/sheader.htm
			
			int iColumnCount = 0;
			//<input type="checkbox" name="master" onclick="checkAll();" value="">Select All
			s += 
			"<TABLE  WIDTH=100% BORDER=0 cellspacing=0 cellpadding=1>";
			s += "<THEAD>";
			s += "<TR>";
			s += "<TH width=20px class=\"orderlineheadingright\"></TH>";
			s += "<TH width=20px class=\"orderlineheadingcenter\"><input type=\"checkbox\" name=\"master\" onClick=\"checkAll();\"></TH>";
			iColumnCount++;
			s += "<TH width=50px class=\"orderlineheadingright\" >Line</TH>";
			iColumnCount++;
			if (!bIsQuote){
				s += "<TH width=100px class=\"orderlineheadingright\" >Ordered</TH>";
				iColumnCount++;
				s += "<TH width=100px class=\"orderlineheadingright\" >Shipped</TH>";
				iColumnCount++;
			}else{
				s += "<TH width=100px class=\"orderlineheadingright\" >Quoted</TH>";
				iColumnCount++;
			}
			s += "<TH width=100px class=\"orderlineheadingright\" >On Hand</TH>";
			iColumnCount++;
			s += "<TH width=110px class=\"orderlineheadingleft\" >Item</TH>";
			iColumnCount++;
			s += "<TH width=35px class=\"orderlineheadingcenter\" >Hide</TH>";
			iColumnCount++;
			s += "<TH class=\"orderlineheadingleft\" >Description</TH>";
			iColumnCount++;
			s += "<TH width=10px class=\"orderlineheadingleft\" >U/M</TH>";
			iColumnCount++;
			s += "<TH width=10px class=\"orderlineheadingleft\" >Loc</TH>";
			iColumnCount++;
			if (!bIsQuote){
				s += "<TH width=15px class=\"orderlineheadingleft\" >Cat</TH>";
				iColumnCount++;
				s += "<TH width=15px class=\"orderlineheadingleft\" >Mec</TH>";
				iColumnCount++;
				s += "<TH width=100px class=\"orderlineheadingright\" >Price</TH>";
				iColumnCount++;
			}else{
				s += "<TH width=60px class=\"orderlineheadingright\" >Est.<BR>cost</TH>";
				iColumnCount++;
				s += "<TH width=60px class=\"orderlineheadingright\" >Multiplier</TH>";
				iColumnCount++;
				s += "<TH width=100px class=\"orderlineheadingright\" >Price</TH>";
				iColumnCount++;
				s += "<TH width=100px class=\"orderlineheadingright\" >Ext.<BR>cost</TH>";
				iColumnCount++;
			}

			s += "<TH width=100px class=\"orderlineheadingright\" >Ext.<BR>price</TH>";
			iColumnCount++;
			s += "</TR>";
			s += "</THEAD>";			
			boolean bOddRow = true;

			s += "<tbody id=\"sortable\" style=\"overflow-y: auto;\">\n";
			int BufferCounter = 0;
			String sTemp = "";
			BigDecimal dRemainingOrderTotal = new BigDecimal(0.00);
			BigDecimal bdShippedValue = new BigDecimal(0.00);
			BigDecimal bdTotalExtendedEstimatedCost = new BigDecimal(0.00);
			BigDecimal bdExtendedEstimatedCost = new BigDecimal(0.00);
			BigDecimal bdLaborUnits = new BigDecimal(0.00);
			while(rsDetails.next()){
				if (BufferCounter > NUMBEROFRECORDSTOBUFFER){
					s += sTemp;
					sTemp = "";
					BufferCounter = 0;
				}
				//We are going to add to this string buffer in chunks to try to optimize really large orders:
				if(bOddRow){
					sBackgroundColor = "#" + DARK_BG_COLOR + "";
				}else{
					sBackgroundColor = "#" + LIGHT_BG_COLOR + "";
				}
				
				String sDetailNumber = Integer.toString(rsDetails.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber));
				String sLineNumber = Integer.toString(rsDetails.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber));
				

				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				sTemp += "<TR style =\"background-color:" + sBackgroundColor + ";\""
					+ " id = \"" + "ROWID" + sLineNumber + "\""
					+ ">";

				//Add draggable handle
				sTemp += "<TD width=20px><span class=\"handle\"><span class=\"ui-icon ui-icon-arrowthick-2-n-s\"></span></span></TD>";
				//Add hidden input for row changes
				sTemp += "<INPUT TYPE=\"HIDDEN\" NAME=\"" + LINENUMBERMOVEMARKER + sDetailNumber + "\" VALUE=\"" + sLineNumber + "\" >";
				
				
				//Put the checkbox in the far left column:
				String sCheckboxFieldname = LINEDETAILBASE + sDetailNumber;
				sTemp += "<TD WIDTH=20px ALIGN=CENTER style = \"vertical-align:top;\" ><INPUT TYPE=CHECKBOX";
				if (req.getParameter(sCheckboxFieldname) != null){
					sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}
				sTemp += " NAME=\"" + sCheckboxFieldname + "\""
					+ " ID = \"" + sCheckboxFieldname + "\""
					+ " \n onChange=\"toggle(document.getElementById('ROWID" + sLineNumber + "'), '" + sBackgroundColor.replace("\"", "") + "');\""
					+ " width=0.25>"
					+ "</TD>\n"
				;
				
				String sLineNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMEditOrderDetailEdit?"
					+ SMOrderDetail.ParamiDetailNumber + "=" + sDetailNumber
					+ "&" + SMOrderDetail.Paramstrimmedordernumber + "=" + sTrimmedOrderNumber
					+ "&" + SMOrderDetail.ParamiLineNumber + "=" + sLineNumber
					+ "&" + SMOrderDetail.ParamdUniqueOrderID + "=" 
						+ Double.toString(rsDetails.getDouble(SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID))
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsStringFunctions.PadLeft(sLineNumber, "0", 4) + "</A>";
				
				sTemp += "<TD WIDTH=50px ALIGN=RIGHT " 
					+ " style = \"vertical-align:top;\" "
					+ "><FONT SIZE=2>" 
					+ sLineNumberLink
					+ "</FONT></TD>";
				BigDecimal bdQtyOrdered = rsDetails.getBigDecimal(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered);
				sTemp += "<TD width=100px ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyOrderedScale, bdQtyOrdered) 
					+ "</FONT></TD>";
				
				BigDecimal bdQtyShipped = rsDetails.getBigDecimal(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped);
				if (!bIsQuote){
					sTemp += "<TD width=100px ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, bdQtyShipped) 
						+ "</FONT></TD>";
				}
				BigDecimal bdQtyOnHand = new BigDecimal("0.0000");
				if (rsDetails.getBigDecimal(
						SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand) != null){
					bdQtyOnHand = rsDetails.getBigDecimal(
						SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand); 
				}
				//By default, don't display in red:
				boolean bDisplayInRed = false;
				//But if it's overshipped, then by default display in red:
				if (bdQtyOnHand.compareTo(bdQtyShipped) < 0){
					bDisplayInRed = true;
				}
				//Unless it's a non-stock item, then turn off the red:
				if (rsDetails.getInt(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1){
					bDisplayInRed = false;
				}
				//If the item qty lookup is turned off, then turn off the red:
				if (rsDetails.getInt(SMTableicitems.TableName + "." + SMTableicitems.isuppressitemqtylookup) == 1){
					bDisplayInRed = false;
				}
				if (bDisplayInRed){
					sTemp += "<TD width=100px ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2; COLOR=RED>" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, bdQtyOnHand) 
					+ "</FONT></TD>";
				}else{
					sTemp += "<TD width=100px ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, bdQtyOnHand) 
					+ "</FONT></TD>";
				}

				String sItemNumber = rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber);
				String sItemNumberLink = sItemNumber;
				if (bAllowItemViewing){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sItemNumber + "</A>";
				}				
				sTemp += "<TD width=110px " + sCellStyle + "><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>";
				
				if (rsDetails.getInt(SMTableorderdetails.TableName + "." + SMTableorderdetails.isuppressdetailoninvoice) == 1){
					sTemp += "<TD width=35px ALIGN=CENTER" + sCellStyle + "><FONT SIZE=2>" + "<B>X</B>" + "</FONT></TD>";
				}else{
					sTemp += "<TD width=35px ALIGN=CENTER" + sCellStyle + "><FONT SIZE=2>" + "&nbsp;" + "</FONT></TD>";
				}
				
				sTemp += "<TD " + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc) + "</FONT></TD>";
				sTemp += "<TD width=60px " + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure) + "</FONT></TD>";
				sTemp += "<TD width=60px ALIGN=LEFT" + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode) + "</FONT></TD>";
				
				if (!bIsQuote){
					sTemp += "<TD width=60px ALIGN=LEFT" + sCellStyle + "><FONT SIZE=2>" 
						+ rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemCategory) + "</FONT></TD>";
					sTemp += "<TD width=60px ALIGN=LEFT" + sCellStyle + "><FONT SIZE=2>" 
						+ rsDetails.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sMechInitial) + "</FONT></TD>";
					sTemp += "<TD width=100px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(
							SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice)) + "</FONT></TD>";
				}else{
					BigDecimal bdEstimatedUnitCost = rsDetails.getBigDecimal(
							SMTableorderdetails.TableName + "." + SMTableorderdetails.bdEstimatedUnitCost);
					BigDecimal bdUnitPrice = rsDetails.getBigDecimal(
							SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice);
					BigDecimal bdMultiplier = new BigDecimal(0.00);
					if (bdEstimatedUnitCost.compareTo(BigDecimal.ZERO) != 0){
						bdMultiplier = bdUnitPrice.divide(bdEstimatedUnitCost, 2, RoundingMode.HALF_UP);
					}
					bdExtendedEstimatedCost = bdEstimatedUnitCost.multiply(bdQtyOrdered);
					sTemp += "<TD width=60px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdEstimatedUnitCost) + "</FONT></TD>";
					sTemp += "<TD width=60px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdMultiplier) + "</FONT></TD>";
					sTemp += "<TD width=100px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdUnitPrice) + "</FONT></TD>";
					sTemp += "<TD width=100px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedEstimatedCost) + "</FONT></TD>";
				}
				bdTotalExtendedEstimatedCost = bdTotalExtendedEstimatedCost.add(bdExtendedEstimatedCost);
				BigDecimal bdExtendedPrice = rsDetails.getBigDecimal(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice);
				bdShippedValue = bdShippedValue.add(bdExtendedPrice);
				sTemp += "<TD width=100px ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedPrice) + "</FONT></TD>";
				sTemp += "</TR>";
				
				//Calculate the remaining order total:
				BigDecimal bdRemainingLineTotal = BigDecimal.ZERO;
				if (rsDetails.getBigDecimal(SMTableorderdetails.TableName + "." 
						+ SMTableorderdetails.dExtendedOrderPrice).compareTo(BigDecimal.ZERO) == 0){
					bdRemainingLineTotal = rsDetails.getBigDecimal(
							SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered).multiply(
							rsDetails.getBigDecimal(SMTableorderdetails.TableName + "." 
							+ SMTableorderdetails.dOrderUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
				}else{
					bdRemainingLineTotal = rsDetails.getBigDecimal(SMTableorderdetails.TableName + "." 
							+ SMTableorderdetails.dExtendedOrderPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
				}
				dRemainingOrderTotal = dRemainingOrderTotal.add(bdRemainingLineTotal);
				
				//Total the labor units:
				if (rsDetails.getLong(SMTableicitems.TableName + "." + SMTableicitems.ilaboritem) == 1){
					bdLaborUnits = bdLaborUnits.add(rsDetails.getBigDecimal(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered));
				}
				bOddRow = ! bOddRow;
				BufferCounter++;
			}
			rsDetails.close();
			
			sTemp += "</tbody>";
			
			//Add the totals rows:
			if (bIsQuote){
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Extended cost/price totals:</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalExtendedEstimatedCost) + "</B></FONT>"
					+ "</TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;

				//Add a row for the gross markup:
				BigDecimal bdGrossMarkup = new BigDecimal(0.00);
				bdGrossMarkup = bdShippedValue.subtract(bdTotalExtendedEstimatedCost);

				//Add a row for the labor units:
				sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
						+ " class=\"orderlineheadingright\" ><FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + "><B>Labor qty:</B></FONT></TD>"
						//+ "<TD>&nbsp;</TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + ">" 
						+ "<B>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyOrderedScale, bdLaborUnits)
							+ "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				
				BigDecimal bdMarkUpPerLaborUnit = new  BigDecimal(0.00);
				if (bdLaborUnits.compareTo(BigDecimal.ZERO) > 0){
					bdMarkUpPerLaborUnit = bdGrossMarkup.divide(bdLaborUnits, 2, RoundingMode.HALF_UP);
				}
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + "><B>Mark up per labor unit: "
					+ "<a href=\"#Footnote2\"><SUP>2</SUP></a></B></FONT></TD>"
					//+ "<TD>&nbsp;</TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + ">" 
					+ "<B>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(2, bdMarkUpPerLaborUnit)
						+ "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;

				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + "><B>Mark up: "
					+ "<a href=\"#Footnote3\"><SUP>3</SUP></a></B></FONT></TD>"
					//+ "<TD>&nbsp;</TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + ">" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrossMarkup) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
				
				BigDecimal bdGrossProfitPercentage = new BigDecimal("0.00");
				if (bdShippedValue.compareTo(BigDecimal.ZERO) != 0){
					bdGrossProfitPercentage = (bdGrossMarkup.divide(bdShippedValue, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal (100.00)); 
				}
				sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
						+ " class=\"orderlineheadingright\" ><FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + "><B>Gross profit percentage: "
						+ "<a href=\"#Footnote4\"><SUP>4</SUP></a></B></FONT></TD>"
						//+ "<TD>&nbsp;</TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2 COLOR=" + COSTVALUES_COLOR + ">" 
						+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrossProfitPercentage) + "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				
				//Add a row for the total discounted amount and percentage:
				BigDecimal bdDiscountedAmount = new BigDecimal(order.getM_dPrePostingInvoiceDiscountAmount().replace(",",""));
				if (bdDiscountedAmount.compareTo(BigDecimal.ZERO) != 0){
					sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) + " class=\"orderlineheadingright\" ><FONT SIZE=2><B>" 
							+ order.getM_sPrePostingInvoiceDiscountDesc() + " (" 
							+ order.getM_dPrePostingInvoiceDiscountPercentage() + "%)" + "</B></FONT></TD>"
						+ "<TD>&nbsp;</TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2>" 
						+ "<B>-" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountedAmount) + "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				
					//Add a row for the sub total after discount:
					sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) + " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Subtotal after discount:</B></FONT></TD>"
						+ "<TD>&nbsp;</TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2>" 
						+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount)) + "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				}
	
				//Add a row for the tax:
				String sSalesTaxAmount;
				try {
					sSalesTaxAmount = order.getSalesTaxAmount(conn);
				} catch (Exception e) {
					sSalesTaxAmount = e.getMessage();
				}
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Estimated Sales Tax (calculated only on " 
					+ "quoted items that are TAXABLE):</B></FONT></TD>"
					+ "<TD>&nbsp;</TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + sSalesTaxAmount + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
				
				//Add a row for total INCLUDING sales tax:
				String sTotalWithSalesTax = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount).add(new BigDecimal(sSalesTaxAmount.replace(",", ""))));
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount - 1) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Extended quote total INCLUDING discount and sales tax:</B></FONT></TD>"
					+ "<TD>&nbsp;</TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + sTotalWithSalesTax + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;

				//Add any remaining strings to the buffer:
				s += sTemp;
			}else{
				//TJR!!!
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount ) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Extended price total:</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
				
				//Add a row for the total discounted amount and percentage:
				BigDecimal bdDiscountedAmount = new BigDecimal(order.getM_dPrePostingInvoiceDiscountAmount().replace(",",""));
				if (bdDiscountedAmount.compareTo(BigDecimal.ZERO) != 0){
					sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount ) + " class=\"orderlineheadingright\" ><FONT SIZE=2><B>" 
							+ order.getM_sPrePostingInvoiceDiscountDesc() + " (" 
							+ order.getM_dPrePostingInvoiceDiscountPercentage() + "%)" + "</B></FONT></TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2>" 
						+ "<B>-" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountedAmount) + "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				
					//Add a row for the sub total after discount:
					sTemp += "<TR>"
						+ "<TD align=right colspan = " + Integer.toString(iColumnCount ) + " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Subtotal after discount:</B></FONT></TD>"
						+ "<TD align=right>"
						+ "<FONT SIZE=2>" 
						+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount)) + "</B></FONT>"
						+ "</TD>"
						+ "</TR>"
					;
				}
	
				//Add a row for the sales tax:
				String sSalesTaxAmount;
				try {
					sSalesTaxAmount = order.getSalesTaxAmount(conn);
				} catch (Exception e) {
					sSalesTaxAmount = e.getMessage();
				}
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount) 
					+ " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Estimated Sales Tax (calculated only on " 
					+ "shipped items that are TAXABLE):</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + sSalesTaxAmount + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
				
				//Add a row for total INCLUDING tax:
				String sTotalWithSalesTax = "";
				try {
					sTotalWithSalesTax = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount).add(new BigDecimal(sSalesTaxAmount.replace(",", ""))));
				} catch (Exception e) {
					sTemp += "Error [1390337789] calculating Total With Sales Tax - " + e.getMessage();
				}
				sTemp += "<TR>"
					+ "<TD align=right colspan = " + Integer.toString(iColumnCount) + " class=\"orderlineheadingright\" ><FONT SIZE=2><B>Extended price total INCLUDING discount and sales tax:</B></FONT></TD>"
					+ "<TD align=right >"
					+ "<FONT SIZE=2>" 
					+ "<B>" + sTotalWithSalesTax + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
				
				//Add any remaining strings to the buffer:
				s += sTemp;
			}
			
			s += "</TABLE>";
			
			//Add footnotes to explain the costing fields:
			if (bIsQuote){
				s += "<FONT SIZE=2><a name=\"Footnote1\"><SUP>1</SUP> When repricing, the program will use this amount and the method you use "
					+ "to re-calculate the prices on the selected lines to arrive at a total quote amount.";
				s += "<BR><a name=\"Footnote2\"><SUP>2</SUP> <B>Markup per labor unit</B> is the total mark up"
					+ " divided by the number of labor units (typically hours or days).";
				s += "<BR><a name=\"Footnote3\"><SUP>3</SUP> <B>Markup</B> is the total extended price"
					+ " minus the total extended estimated cost.";
				s += "<BR><a name=\"Footnote4\"><SUP>4</SUP> <B>Gross profit percentage</B> is the mark up"
					+ " divided by the total extended price (then multipled by 100 to get a percentage).</FONT>";
			}
			
		}catch(SQLException e){
			throw e;
		}
		return s;
	}
	private String buildTotalsTable(SMOrderHeader order, String sDBID, String sUserID, Connection conn, String sLicenseModuleLevel){

		String s = "";
		String sBackgroundColor = "";
		BigDecimal bdChangeOrderTotal = new BigDecimal(0);
		
		try{
			String SQL = "SELECT * FROM " + SMTablechangeorders.TableName
			+ " WHERE ("
			+ SMTablechangeorders.sJobNumber + " = '" + order.getM_strimmedordernumber() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablechangeorders.datChangeOrderDate
			;
			ResultSet rsChangeOrders = clsDatabaseFunctions.openResultSet(SQL, conn);

			s += "<B><U>Change Orders</U></B><BR>";
			s += "<TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>";
			s += "<TR>";
			s += "<TD><FONT SIZE=2><B>Date</B></TD>";
			s += "<TD><FONT SIZE=2><B>C.O. #</TD>";
			s += "<TD><FONT SIZE=2><B>Description</B></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>Truck Days</B></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>Total MU</B></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>Amount</B></TD>";
			
			s += "</TR>";

			boolean bOddRow = true;
			if(bOddRow){
				sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"#FFFFFF\"";
			}
			//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += "<TD><FONT SIZE=2><B>" + order.getM_datOrderDate() + "</B></FONT></TD>";
			s += "<TD><FONT SIZE=2>&nbsp;</FONT></TD>";
			s += "<TD ALIGN><FONT SIZE=2><B>ORIGINAL CONTRACT AMOUNT</B></FONT></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>" + order.getM_bdtruckdays() + "</B></FONT></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>" + order.getM_bdtotalmarkup() + "</B></FONT></TD>";
			s += "<TD ALIGN=RIGHT><FONT SIZE=2><B>" + order.getM_bdtotalcontractamount() 
					+ "</B></FONT></TD>";
			s += "</TR>";
			bOddRow = ! bOddRow;
			while(rsChangeOrders.next()){
				bdChangeOrderTotal = bdChangeOrderTotal.add(rsChangeOrders.getBigDecimal(SMTablechangeorders.dAmount.replace("`", "")));
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(
					rsChangeOrders.getDate(SMTablechangeorders.datChangeOrderDate.replace("`", "")),"M/d/yyyy") 
					+ "</FONT></TD>";
				s += "<TD><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(
					rsChangeOrders.getDouble(SMTablechangeorders.dChangeOrderNumber.replace("`", ""))) 
					+ "</FONT></TD>";
				s += "<TD ALIGN><FONT SIZE=2>" + rsChangeOrders.getString(SMTablechangeorders.sDesc.replace("`", ""))
					+ "</FONT></TD>";
				s += "<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(
					rsChangeOrders.getDouble(SMTablechangeorders.dTruckDays.replace("`", ""))) + "</FONT></TD>";
				s += "<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(
					rsChangeOrders.getDouble(SMTablechangeorders.dTotalMarkUp.replace("`", ""))) + "</FONT></TD>";
				s += "<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(
					rsChangeOrders.getDouble(SMTablechangeorders.dAmount.replace("`", ""))) + "</FONT></TD>";
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsChangeOrders.close();
			
			//Print the change order total:
			sBackgroundColor = "\"#FFFFFF\"";
			//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += 
				"<TD ALIGN=RIGHT COLSPAN = 5><B><FONT SIZE=2>CHANGE ORDER TOTAL:</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal) + "</FONT></B></TD>"
				+ "</TR>"
			;
			
			//Print the total of the contract amount AND the change orders:
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += 
				"<TD ALIGN=RIGHT COLSPAN = 5><B><FONT SIZE=2>"
				+ "TOTAL CONTRACT AMOUNT (ORIGINAL CONTRACT AMOUNT <I>PLUS</I> CHANGE ORDER TOTAL):"
				+ "</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal.add(new BigDecimal(
					order.getM_bdtotalcontractamount().replace(",", "")))) 
				+ "</FONT></B></TD>"
				+ "</TR>"
			;
			s += "</TABLE>";
		}catch(SQLException e){
			s += "Error opening change orders query: " + e.getMessage();
			return s;
		}

		//Print a list of invoices for this order:
		try{
			String SQL = "SELECT"
				+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iinvoicingstate
				+ ", SUM(" + SMTableinvoicedetails.TableName + "." 
					+  SMTableinvoicedetails.dExtendedPriceAfterDiscount + ") AS EXTPRICE"
				+ " FROM " + SMTableinvoicedetails.TableName + " INNER JOIN "
				+ SMTableinvoiceheaders.TableName
				+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				+ " WHERE ("
					+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber
					+ " = '" + order.getM_strimmedordernumber() + "'"
				+ ")"
				+ " GROUP BY (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
				+ ")"
				;

			//System.out.println("In " + this.toString() + " Invoice SQL = " + SQL);

			ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += 
			"<BR><B><U>Billing Summary</U></B><BR>";
			s += "<TABLE WIDTH=100% BORDER=0 cellspacing=0 cellpadding=1>";
			s += "<TR>";
			s += "<TD style=\"border: 0px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Invoice date</B></TD>";
			s += "<TD style=\"border: 0px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Invoice number</B></TD>";
			s += "<TD style=\"border: 0px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Invoicing state</B></TD>";
			s += "<TD style=\"border: 0px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Type</B></TD>";
			s += "<TD style=\"border: 0px solid; bordercolor: 000; vertical-align:bottom; text-align:right; \"><FONT SIZE=2><B>Amount</B></TD>";
			s += "</TR>";

			boolean bOddRow = true;
			BigDecimal bdTotalBilled = new BigDecimal(0.00);
			String sCellStyle = "";
			boolean bAllowInvoiceView = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMPrintInvoice, 
						sUserID, 
						conn,
						sLicenseModuleLevel
				);
			while(rsInvoices.next()){
				bdTotalBilled = bdTotalBilled.add(rsInvoices.getBigDecimal("EXTPRICE"));
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				s += "<TR bgcolor =" + sBackgroundColor + ">";

				s += "<TD ALIGN=LEFT " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.utilDateToString(
								rsInvoices.getDate(
										SMTableinvoiceheaders.TableName + "." 
										+ SMTableinvoiceheaders.datInvoiceDate), "M/d/yyyy") + "</FONT></TD>";
				
				String sInvoiceNumber = rsInvoices.getString(SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.sInvoiceNumber).trim();

				if (bAllowInvoiceView){
					s += "<TD ALIGN=LEFT " + sCellStyle + "><FONT SIZE=2>"
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
							+ SMUtilities.lnViewInvoice(sDBID, sInvoiceNumber)
							+ "\">"
							+ sInvoiceNumber
							+ "</A>"
							+ "</FONT></TD>";
				}else{
					s += "<TD ALIGN=LEFT " + sCellStyle + "><FONT SIZE=2>" 
							+ sInvoiceNumber + "</FONT></TD>";
				}

				//Invoicing Sate
				ArrayList<String> arrInvoicingStateDescriptions = new ArrayList<String>(); 
				ArrayList<String> arrInvoicingStateValues = new ArrayList<String>(); 
				
				for(int i = 0; i < SMTableinvoiceheaders.NUMBER_OF_INVOICING_STATES; i++){
					arrInvoicingStateValues.add(Integer.toString(i));
					arrInvoicingStateDescriptions.add(SMTableinvoiceheaders.getInvoicingStateDescription(i));
					
				}
				s += "<TD ALIGN=LEFT " + sCellStyle +  "><FONT SIZE=2>"
						+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field("INVOICESTATE" + sInvoiceNumber ,
								arrInvoicingStateValues, 
								Integer.toString(rsInvoices.getInt(SMTableinvoiceheaders.TableName + "."+ SMTableinvoiceheaders.iinvoicingstate)), 
								arrInvoicingStateDescriptions )
						+ "</FONT>";
				
				s +=  " <button type=\"button\""
						+ " value=\"" + INVOICECOMMANDUPDATESTATE_LABEL + sInvoiceNumber + "\""
						+ " name=\"" + INVOICECOMMANDUPDATESTATE_BUTTON + "\""
						+ " onClick=\"updateInvoicingState(" + "'" + sInvoiceNumber + "'" + ");\">"
						+ INVOICECOMMANDUPDATESTATE_LABEL
						+ "</button>\n"
						;
				
				s += "</TD>";
				//Type
				if (rsInvoices.getInt(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_INVOICE){
					s += "<TD><FONT SIZE=2>INVOICE</FONT></TD>";
				}else{
					s += "<TD><FONT SIZE=2>CREDIT</FONT></TD>";
				}
				
				//EXTPRICE
				s += "<TD ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" 
						+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsInvoices.getDouble("EXTPRICE")) 
						+ "</FONT></TD>";

				s += "</TR>";

				bOddRow = ! bOddRow;
			}
			rsInvoices.close();
			
			//Show the total amount billed:
			sBackgroundColor = "\"#FFFFFF\"";
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += 
				"<TD ALIGN=RIGHT COLSPAN=4" + sCellStyle 
				+ "><B><FONT SIZE=2>AMOUNT INVOICED TO DATE:</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled)
				+ "</B></FONT></TD>"
				+ "</TR>"
			;
			//****************
			boolean bCalculationFailed = false;
			try {
				order.calculateBillingTotals(conn, order.getM_strimmedordernumber());
			} catch (Exception e) {
				s += "<TR>";
				s += "<TD ALIGN=RIGHT ><FONT SIZE=2; COLOR=RED>" 
						+ "<B>ERROR CALCULATING ORDER TOTALS - " + e.getMessage() + "</B>" + "</FONT></TD>";
				s += "</TR>";
				bCalculationFailed = true;
			}
	
			if (bCalculationFailed == false){
				//Show the total amount remaining to be billed
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += 
						"<TD ALIGN=RIGHT COLSPAN=4" + sCellStyle 
						+ "><B><FONT SIZE=2>AMOUNT ON ORDER DETAILS CURRENTLY BEING SHIPPED:</FONT></B></TD>"
						+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped())
						+ "</B></FONT></TD>"
						+ "</TR>"
				;

				s += 
						"<TD ALIGN=RIGHT COLSPAN=4" + sCellStyle 
						+ "><B><FONT SIZE=2>AMOUNT ON ORDER DETAILS REMAINING UNSHIPPED:</FONT></B></TD>"
						+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_TotalAmtStillOnOrder())
						+ "</B></FONT></TD>"
						+ "</TR>"
				;
				
				//Show the total of invoiced and to-be-invoiced:
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += 
						"<TD ALIGN=RIGHT COLSPAN=4" + sCellStyle 
						+ "><B><FONT SIZE=2>TOTAL ORDER AMT (amt invoiced to date PLUS amt shipped PLUS amt left on order):</FONT></B></TD>"
						+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_TotalBilled().add(
									order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped()).add(
											order.getCalculatedOrderTotals_TotalAmtStillOnOrder()))
						+ "</B></FONT></TD>"
						+ "</TR>"
				;
				
				//Show any difference between the amount remaining to be billed AND the amount left on the contract here:
				if (order.getCalculatedOrderTotals_OriginalContractAmount().compareTo(BigDecimal.ZERO) != 0){
					s += "<TR bgcolor =" + sBackgroundColor + ">";
					if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) > 0){
						s += "<TD ALIGN=RIGHT COLSPAN=4" + sCellStyle + ">" 
							+ "<FONT COLOR=RED size=2><B><I>NOTE:</I> THE TOTAL ORDER AMOUNT IN THE BILLING SUMMARY IS "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
							+ " <I>LESS</I> THAN THE TOTAL CONTRACT AMOUNT IN THE CHANGE ORDER LOG.</B></FONT></TD>"
						;
					}
					if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) < 0){
						s += "<TD ALIGN=RIGHT COLSPAN=5" + sCellStyle + ">"
							+ "<FONT COLOR=RED size=2><B><I>NOTE:</I> THE TOTAL ORDER AMOUNT IN THE BILLING SUMMARY IS "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
							+ " <I>MORE</I> THAN THE TOTAL CONTRACT AMOUNT IN THE CHANGE ORDER LOG.</B></FONT></TD>"
						;
					}
					s += "</TR>";
				}
			}
			
			s += "</TABLE>";
		}catch(SQLException e){
			s += "Error opening invoice query: " + e.getMessage();
			return s;
		}
		return s;
	}
	private String sCommandScripts(
			String sTrimmedOrderNumber, 
			String sLastDetailNumberEdited, 
			Connection conn) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		
		s += "<script type=\"text/javascript\">\n";
		
		//Set the default focus:
		int iLastDetailNumberEdited = 0;
		try {
			iLastDetailNumberEdited = Integer.parseInt(sLastDetailNumberEdited);
		} catch (NumberFormatException e) {
			iLastDetailNumberEdited = 0;
		}
		
		if (iLastDetailNumberEdited > 0){
			s += "window.onload = function(){\n"
				+ "    document.forms.MAINFORM." + LINEDETAILBASE + sLastDetailNumberEdited + ".focus();\n"
				+ "    initShortcuts();\n"
				+ "}\n\n"			
			;
		}else{
			s += "window.onload = function(){\n"
				+ "    initShortcuts();\n"
				+ "}\n\n"			
			;
		}
		
		s += "$(document).ready(\n" + 
				"   function() {\n" + 
				"     $('.handle').css('cursor', 'pointer');\n" + 
				"		$(\"tbody#sortable\").sortable({\n" +
				"		update: function(event, ui) {  \n" + 
				"         $('tbody#sortable tr').each(function() {\n" + 
				"         	$(this).children(':hidden').val($(this).index() + 1);\n" + 
				"        	});\n" + 
				"          document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \""+ LINECOMMANDMOVE_VALUE + "\";\n" + 
				"          document.forms[\"MAINFORM\"].submit();\n" + 
				"     },\n" + 
				"		handle: 'td:first .handle',\n" + 
				"		cursor: 'move',\n" + 
				"		tolerance: 'pointer',\n" + 
				"		containment: 'parent'\n" + 
				"}); \n" + 
			//	" if(($(\"tbody#sortable\").children('tr').length <= 1)){\n" + 
			//	"    $( \"tbody#sortable\").sortable( \"disable\" );\n" + 
			//	"	}\n" + 
				"		});\n\n"
				;
		s+= "function checkAll(){\n"
			+ "	if (document.forms.MAINFORM.master.checked == true){\n" 
			+ "		for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
   			+ "			var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
   			+ "			if (testName.substring(0, " + Integer.toString(LINEDETAILBASE.length()) + "	) == \"" + LINEDETAILBASE + "\"){\n"
   			+ "				document.forms[\"MAINFORM\"].elements[i].checked = true;\n"
   			+ "      	}\n"
   			+ "  	}\n"
   			+ " 	var Rows=new Array()\n;" 
   			+ " 	for (j=1;j<getNumberLines()+1; j++){\n"
			+ " 		Rows.push(\"ROWID\" + j);\n"
			+ " 	}\n"
			+ "		for (k=0; k<Rows.length; k++){\n"
			+ " 		document.getElementById(Rows[k]).style.backgroundColor = '" + HIGHLIGHTROWCOLOR + "';"
			+ " 	}\n"
   			+ "}else{\n"
			+ "		for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
   			+ "			var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
   			+ "        	if (testName.substring(0, " + Integer.toString(LINEDETAILBASE.length()) + ") == \"" + LINEDETAILBASE + "\"){\n"
   			+ "				document.forms[\"MAINFORM\"].elements[i].checked = false;\n"
   			+ "      	}\n"
   			+ "  	}\n"
   			+ " 	var Rows=new Array()\n;" 
   			+ " 	for (j=1;j<getNumberLines()+1; j++){\n"
			+ " 		Rows.push(\"ROWID\" + j);\n"
			+ " 	}\n"
			+ "		for (k=0; k<Rows.length; k++){\n"
			+ "  		if (k % 2 == 0){\n"
			+ " 			document.getElementById(Rows[k]).style.backgroundColor = '" + DARK_BG_COLOR + "';\n"
			+ " 		}else{\n"
			+ "				document.getElementById(Rows[k]).style.backgroundColor = '#FFFFFF';\n"
			+ " 		}\n"
			+ " 	}\n"
   			+ "	}\n"
		  + "}\n";
		
		s += "function getNumberOfCheckedLines(){\n"
			+ "    var numberofcheckedlines = 0;\n"
			+ "    for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
			+ "        var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
			+ "        if (testName.substring(0, " + Integer.toString(LINEDETAILBASE.length()) 
				+ ") == \"" + LINEDETAILBASE + "\"){\n"
			+ "            if (document.forms[\"MAINFORM\"].elements[i].checked == true){\n"
			+ "                numberofcheckedlines = numberofcheckedlines + 1;\n"
			+ "            }"
			+ "        }\n"
			+ "    }\n"
			+ "    return numberofcheckedlines;\n"
			+ "}\n"
		;
		s += "function getNumberLines(){\n"
			+ "    var numberoflines = 0;\n"
			+ "    for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
			+ "        var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
			+ "        if (testName.substring(0, " + Integer.toString(LINEDETAILBASE.length()) 
				+ ") == \"" + LINEDETAILBASE + "\"){\n"
			+ "            numberoflines = numberoflines + 1;\n"
			+ "        }\n"
			+ "    }\n"
			+ "    return numberoflines;\n"
			+ "}\n"
		;
		s += "function addLine(){\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDADDLINE_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function deleteLines(){\n"
			//First make sure that at least one line is checked:
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to delete the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			//Next, make sure they are not trying to delete ALL the lines:
			+ "    if (getNumberOfCheckedLines() == getNumberLines()){\n"
			+ "        alert('You cannot delete EVERY line on an order.')\n"
			+ "        return;\n"
			+ "    }\n"
			//Then, confirm that the user wants to delete:
			+ "    if (confirm('Are you sure you want to delete the selected lines?')){\n"
			+ "        document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDDELETE_LABEL + "\";\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        return;\n"
			+ "    }\n"
			+ "}\n"
		;

		s += "function insertLine(){\n"
			//First make sure that at least one line is checked:
			+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to insert a line above the selected line, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (getNumberOfCheckedLines() > 1){\n"
			+ "        alert('To insert a line above the selected line, you must select ONLY one line.')\n"
			+ "        return;\n"
			+ "    }\n"
			
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDINSERTLINE_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function moveAbove(){\n"
				+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
				//+ " 	alert(numberofselectedlines)"
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to move the selected lines, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDMOVEABOVE_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;

		s += "function copyAbove(){\n"
			+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to copy the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDCOPYABOVE_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function setMechanic(){\n"
			+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to set the mechanic on the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDSETMECHANIC_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function setMechanicAndShip(){\n"
			+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to set the mechanic and ship item on the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDSETMECHANICNSHIP_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function repriceItems(){\n"
				+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to reprice using the selected lines, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
			
				+ "    var srepriceamt = document.getElementById(\"" 
					+ REPRICEAMOUNT_PARAM + "\").value;\n"
				+ "    if (isNumeric(srepriceamt) == false){\n"
				+ "        alert(\"The reprice amount '\" + srepriceamt + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + REPRICEAMOUNT_PARAM + "\").focus();\n"
				+ "        document.getElementById(\"" + REPRICEAMOUNT_PARAM + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
			
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDREPRICE_BUTTON + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function gotoHeader(){\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDHEADER_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function gotoTotals(){\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDTOTALS_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function gotoImportWorkOrders(){\n"
			//+ "    alert('This function is still under construction');\n"
			//+ "    return;"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDIMPORTWORKORDERS_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function gotoProposal(){\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDGOTOPROPOSAL_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;

		s += "function createInvoice(){\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + ORDERINVOICE_BUTTON_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		s += "function shipLines(){\n"
			+ "    var numberofselectedlines = getNumberOfCheckedLines();\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to ship the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDSHIP_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function unshipLines(){\n"
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to UNship the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
					 + LINECOMMANDUNSHIP_LABEL + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "function createItems(){\n"
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to create items for the selected lines, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDCREATEITEM_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function createPO(){\n"
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to create a purchase order for the selected lines, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDCREATEPO_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function directEntry(){\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDDIRECTENTRY_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function pasteIntoProposal(){\n"
				+ "    if (document.getElementById(\"" + PROPOSALEXISTS_PARAM + "\").value ==\"\"){\n"
				+ "        alert('You must create a proposal first.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to paste lines into a proposal, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + LINECOMMANDPASTEINTOPROPOSAL_LABEL + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function updateInvoicingState(invoicenumber){\n"
				+ "    document.getElementById(\"" + LINECOMMAND_FLAG + "\").value = \"" 
						 + INVOICECOMMANDUPDATESTATE_LABEL + "\" + invoicenumber +\n"
						 		+ " \"*\" + document.getElementsByName(\"INVOICESTATE\" + invoicenumber)[0].value;\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		//Toggle colors of detail when selected
		s += "function toggle(x,origColor){\n"
			+ "    var newColor = '" + HIGHLIGHTROWCOLOR + "';\n"
			
			//This function converts the rgb( , , ) function to a hex value
			+ "function colorToHex(color) {\n" + 
			"    if (color.substr(0, 1) === '#') {\n" + 
			"        return color;\n" + 
			"    }\n" + 
			"    var digits = /(.*?)rgb\\((\\d+), (\\d+), (\\d+)\\)/.exec(color);\n" + 
			"    \n" + 
			"    var red = parseInt(digits[2]);\n" + 
			"    var green = parseInt(digits[3]);\n" + 
			"    var blue = parseInt(digits[4]);\n" + 
			"    \n" + 
			"    var rgb = blue | (green << 8) | (red << 16);\n" + 
			"    return digits[1] + '#' + rgb.toString(16);\n" + 
			"};\n"
			//Toggle color of line.
			+ "		if ( x.style ) {\n"
			+ "        x.style.backgroundColor = colorToHex(x.style.backgroundColor) === newColor ? origColor : newColor;\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function isNumeric(value) {\n"
			+ "    if ((value == null) || (value == '')) return false;\n"
			+ "    var strippedstring = value.replace(',', '');\n"
			+ "    if (!strippedstring.toString().match(/^[-]?\\d*\\.?\\d*$/)) return false;\n"
			+ "    return true\n"
			+ "    }\n"
		;
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+a\",function() {\n";
		s += "        addLine();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+c\",function() {\n";
		s += "        gotoTotals();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        deleteLines();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+e\",function() {\n";
		s += "        setMechanic();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        pasteIntoProposal();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+h\",function() {\n";
		s += "        gotoHeader();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+i\",function() {\n";
		s += "        createInvoice();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+l\",function() {\n";
		s += "        gotoProposal();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+m\",function() {\n";
		s += "        moveAbove();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+b\",function() {\n";
		s += "        copyAbove();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+n\",function() {\n";
		s += "        insertLine();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+o\",function() {\n";
		s += "        createPO();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        setMechanicAndShip();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        repriceItems();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        shipLines();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+t\",function() {\n";
		s += "        createItems();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+u\",function() {\n";
		s += "        unshipLines();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+w\",function() {\n";
		s += "        gotoImportWorkOrders();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+y\",function() {\n";
		s += "        directEntry();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "}\n";
		s += "\n";
		
		s += "</script>\n";
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
			"table.innermost {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		//s +=
		//	"table.main th {"
		//	+ "border-width: " + sBorderSize + "px; "
		//	+ "padding: 2px; "
		//	//+ "border-style: inset; "
		//	+ "border-style: none; "
		//	+ "border-color: white; "
		//	+ "background-color: white; "
		//	+ "color: black; "
		//	+ "font-family : Arial; "
		//	+ "vertical-align: text-middle; "
		//	//+ "height: 50px; "
		//	+ "}"
		//	+ "\n"
		//	;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: #FFFFFF; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

		//This is the def for a left aligned field:
		s +=
			"td.fieldleftaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a control on the screen:
		s +=
			"td.fieldcontrol {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for an underlined heading on the screen:
		s +=
			"td.fieldheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "text-decoration:underline; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for the order lines heading:
		s +=
			"th.orderlineheadingleft {"
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
			"th.orderlineheadingright {"
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
			"th.orderlineheadingcenter {"
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
		
		s += "tbody.scrolling {\n" 
			+ " height:4em;\n"
			+ " overflow:scroll;\n"
			+ "}\n"
		;
		
		s += ".ui-sortable-helper {\n" 
				+ " display: table;\n"
				+"}\n"  
			;
			
		s += ".handle {\n" 
				+ " cusor: pointer;\n"
				+"}\n"  
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
