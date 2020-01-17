package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditOrderTotalsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String TAX_DROP_DOWN_PARAM = "TAXDROPDOWN";
	private static final String TOTALS_BG_COLOR = "#FFBCA2";
	private static final String ORDERCOMMANDS_BG_COLOR = "#99CCFF";
	public static final String ORDERUPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String DETAILS_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>etails"; //D
	public static final String HEADER_BUTTON_LABEL = "<B><FONT COLOR=RED>H</FONT></B>eader"; //H
	public static final String ORDERINVOICE_BUTTON_LABEL = "<B><FONT COLOR=RED>I</FONT></B>nvoice"; //I
	public static final String ORDERCLONE_BUTTON_LABEL = "C<B><FONT COLOR=RED>l</FONT></B>one"; //L
	public static final String ORDERITEMIZEDWO_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>rint installation work order"; //P
	public static final String ORDERSERVICEWO_BUTTON_LABEL = "P<B><FONT COLOR=RED>r</FONT></B>int service work order"; //R
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String DETAILSCOMMAND_VALUE = "GOTODETAILS";
	public static final String HEADERCOMMAND_VALUE = "GOTOHEADER";
	public static final String PRINTSERVICEWOCOMMAND_VALUE = "PRINTSERVICEWORKORDER";
	public static final String PRINTITEMIZEDWOCOMMAND_VALUE = "PRINTITEMIZEDWORKORDER";
	public static final String CREATEINVOICECOMMAND_VALUE = "CREATEINVOICE";
	public static final String CLONEORDERCOMMAND_VALUE = "CLONEORDER";
	public static final String DISCOUNTCHANGE_FLAG = "DISCOUNTCHANGEFLAG";
	public static final String DISCOUNTCHANGED_VALUE = "DISCOUNTCHANGED";
	public static final String TOTALAMOUNTOFITEMS_FIELD = "TOTALAMTOFITEMS";
	public static final String NOOFSERVICEWOCOPIES_NAME = "NOOFSERVICEWOCOPIES";
	public static final String NOOFINSTALLATIONWOCOPIES_NAME = "NOOFINSTALLATIONWOCOPIES";
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);

		SMOrderHeader entry = new SMOrderHeader(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditOrderTotalsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditOrders
		);
		//Make sure we remove any leftover order detail objects:
		try {
			smedit.getCurrentSession().removeAttribute(SMOrderDetail.ParamObjectName);
		} catch (Exception e1) {
			// No problem if this fails - that means there's no attribute to remove anyway...
		}
		String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
			+ clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*");
		if (bDebugMode){
			System.out.println("[1579271082] In " + this.toString() 
				+ "sCurrentCompleteURL = " + sCurrentCompleteURL);
		}

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		//If this is a 'resubmit', meaning it's being called by SMEditOrderAction, then
		//the session will have an order header object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				smedit.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " 
				+ smedit.getUserID()
				+ " - "
				+ smedit.getFullUserName()
				
				);
		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
					+ entry.getM_strimmedordernumber()
					+ "&Warning=" + "Could not get data connection."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		
		if (currentSession.getAttribute(SMOrderHeader.ParamObjectName) != null){
			entry = (SMOrderHeader) currentSession.getAttribute(SMOrderHeader.ParamObjectName);
			currentSession.removeAttribute(SMOrderHeader.ParamObjectName);

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			if(!entry.load(conn)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080529]");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
						+ entry.getM_strimmedordernumber()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
				return;
			}
			
			String sCancelDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(
					entry.getM_datOrderCanceledDate());
			if (sCancelDate.compareTo("1899-12-31 00:00:00") > 0){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080530]");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
						+ entry.getM_strimmedordernumber()
						+ "&Warning=" + "Canceled orders can not be updated."
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
				return;
			}
		}
		String sObjectName = "Order";
		if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sObjectName = SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE);
		}
		//Record this URL so we can return to it later:
		if (!smedit.getAddingNewEntryFlag()){
			smedit.addToURLHistory("Editing " + sObjectName + " Number " + entry.getM_strimmedordernumber());
		}
		smedit.setTitle(sObjectName + " totals");
		smedit.printLowProfileHeaderTable();
		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Manage orders</A>");
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");

		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);
		//smedit.setOnSubmitFunction("validateForm()");
		try {
			smedit.createEditPage(getEditHTML(smedit, entry, conn, sObjectName), "");
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080531]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					+ "&Warning=" + clsServletUtilities.URLEncode(sError)
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080532]");
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMOrderHeader entry, Connection conn, String sObjectName) throws SQLException{

		String s = "";
		
		s += sCommandScripts(entry, conn);
		s += sStyleScripts();
		
		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ "\">";
		
		//If the user changes the discount amount or percentage, the line values have to be recalculated.  This
		//flag will be set if either of those are changed, so that the program knows to recalculate the line values:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + DISCOUNTCHANGE_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(DISCOUNTCHANGE_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + DISCOUNTCHANGE_FLAG + "\""
			+ "\">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
			+ " id=\"" + COMMAND_FLAG + "\""
			+ "\">";
		
		//Store the total amount of the items on the order:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdTotalAmountItems + "\" VALUE=\"" 
			+ entry.getM_dTotalAmountItems()  + "\""
			+ " id=\"" + SMOrderHeader.ParamdTotalAmountItems + "\""
			+ ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamLASTEDITDATE + "\" VALUE=\"" 
			+ entry.getM_LASTEDITDATE() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamLASTEDITPROCESS + "\" VALUE=\"" 
			+ entry.getM_LASTEDITPROCESS() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamLASTEDITTIME + "\" VALUE=\"" 
			+ entry.getM_LASTEDITTIME() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamLASTEDITUSERFULLNAME + "\" VALUE=\"" 
			+ entry.getM_LASTEDITUSERFULLNAME() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamLASTEDITUSERID + "\" VALUE=\"" 
				+ entry.getM_LASTEDITUSERID() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramstrimmedordernumber + "\" VALUE=\"" 
			+ entry.getM_strimmedordernumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsCustomerCode + "\" VALUE=\"" 
			+ entry.getM_sCustomerCode() + "\">";
		
		//Store the tax ID so we can check to see if it's valid when we go to invoice:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramitaxid + "\" VALUE=\"" 
			+ entry.getitaxid() + "\">";
		
		//New Row
		s += "<TR>";
		ARCustomer cus = new ARCustomer(entry.getM_sCustomerCode());
		if (entry.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			if (!cus.load(conn)){
				s += " Error loading customer " + entry.getM_sCustomerCode() + " - " + cus.getErrorMessageString();
				return s;
			}
		}
		String sOnHold = "";
		if (cus.getM_iOnHold().compareToIgnoreCase("1") == 0){
			sOnHold = "&nbsp;<FONT COLOR=RED><B>ON HOLD</B></FONT>";
		}
		String sOrderNumber = entry.getM_strimmedordernumber();
		
		s += "<B>" + sObjectName + " number:</B>&nbsp;" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sOrderNumber + "</A>"
				+ "&nbsp;<B>" + sObjectName + " date:</B>&nbsp" + entry.getM_datOrderDate()
				+ "&nbsp;<B>Customer:</B>&nbsp;" 
				+ entry.getM_sCustomerCode()
				+ sOnHold
		; 
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial;\">\n";		
		
		//Create the command line at the top:
		s += createCommandsTable(
				conn, 
				sm.getUserID(), 
				sObjectName,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Create the customer area table:
		s += "<TR><TD><TABLE style=\" title:TotalsArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + TOTALS_BG_COLOR + "; \">" 
			+ createOrderTotalsTable(sm, entry, conn) + "</TD>\n";
		s += "</TR></TD></TABLE style=\" title:ENDTotalsArea; \">\n";
		
		//Create the order commands line at the bottom:
		s += createCommandsTable(
				conn, 
				sm.getUserID(), 
				sObjectName,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		return s;
	}
	
	private String createOrderTotalsTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry, 
			Connection conn) throws SQLException{
		String s = "";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderTotals; \">\n";
		
		//Extended order price:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Total extended price:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ entry.getM_dTotalAmountItems().replace("\"", "&quot;")
			+ "</TD>"
		;
		s += "</TR>";
		
		//Discount description
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Discount description:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\""
			+ " VALUE=\"" + entry.getM_sPrePostingInvoiceDiscountDesc().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "30"
			+ " MAXLENGTH= " + SMTableorderheaders.sPrePostingInvoiceDiscountDescLength
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";

		//Pre=posting invoice discount percentage
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Pre-posting invoice discount percentage:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\""
			+ " VALUE=\"" + entry.getM_dPrePostingInvoiceDiscountPercentage().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\""
			+ " onchange=\"discountPercentageChanged();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH= 7"
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Pre-posting invoice discount amount
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Pre-posting invoice discount amt:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\""
			+ " VALUE=\"" + entry.getM_dPrePostingInvoiceDiscountAmount().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\""
			+ " onchange=\"discountAmountChanged();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH= 9"
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Deposit amount
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Deposit amt:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Parambddepositamount + "\""
			+ " VALUE=\"" + entry.getM_bddepositamount().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Parambddepositamount + "\""
			+ " onchange=\"depositAmountChanged();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH= 9"
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style=\" title:ENDOrderSettings; \">\n";
		return s;
	}

	private String createCommandsTable(Connection conn, String sUserID, String sObjectName, String sLicenseModuleLevel){
		String s = "";
		
		s += "<TR><TD>";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ ORDERCOMMANDS_BG_COLOR + "; \" width=100% >\n";
				//Place the 'update' button here:
				s += "<TR><TD style = \"text-align: left; \" >"
					+ "<B><I>Go to:</I></B>"
					
					+ "<button type=\"button\""
					+ " value=\"" + HEADER_BUTTON_LABEL + "\""
					+ " name=\"" + HEADER_BUTTON_LABEL + "\""
					+ " onClick=\"gotoHeader();\">"
					+ HEADER_BUTTON_LABEL
					+ "</button>\n"
					
					+ "<button type=\"button\""
					+ " value=\"" + DETAILS_BUTTON_LABEL + "\""
					+ " name=\"" + DETAILS_BUTTON_LABEL + "\""
					+ " onClick=\"gotoDetails();\">"
					+ DETAILS_BUTTON_LABEL
					+ "</button>\n"
					
					+ "</TD></TR>"
				;
					
				s += "<TR><TD style = \"text-align: left; \" >"
					+ "<B><I>Order commands:</I></B>"
					
					+ "<button type=\"button\""
					+ " value=\"" + ORDERUPDATE_BUTTON_LABEL + "\""
					+ " name=\"" + ORDERUPDATE_BUTTON_LABEL + "\""
					+ " onClick=\"saveOrder();\">"
					+ ORDERUPDATE_BUTTON_LABEL
					+ "</button>\n"
					;
				
				boolean bAllowPrintServiceWO = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMPrintServiceTicket, 
						sUserID, 
						conn,
						sLicenseModuleLevel);

				if (sObjectName.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(
						SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
					if (bAllowPrintServiceWO){
						s += "<button type=\"button\""
						+ " value=\"" + ORDERSERVICEWO_BUTTON_LABEL + "\""
						+ " name=\"" + ORDERSERVICEWO_BUTTON_LABEL + "\""
						+ " onClick=\"printServiceWorkOrder();\">"
						+ ORDERSERVICEWO_BUTTON_LABEL
						+ "</button>\n"
						;
						//Create a drop down for number of copies':
						s += "<SELECT "
							+ " NAME = \"" + NOOFSERVICEWOCOPIES_NAME + "\""
							+ " ID = \"" + NOOFSERVICEWOCOPIES_NAME + "\""
							+ ">"
						;
						for (long l = 0; l < 5; l++){
							s += "<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
								+ ">" + Long.toString(l + 1)
								;
							if (l == 0){
								s += " copy";
							}else{
								s += " copies";
							}
							s += "</OPTION>";
						}
						
						s += "</SELECT>";
					}
					
					boolean bAllowPrintInstallationWO = SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.SMPrintInstallationTicket, 
							sUserID, 
							conn,
							sLicenseModuleLevel);
					if (bAllowPrintInstallationWO){
						s += "<button type=\"button\""
						+ " value=\"" + ORDERITEMIZEDWO_BUTTON_LABEL + "\""
						+ " name=\"" + ORDERITEMIZEDWO_BUTTON_LABEL + "\""
						+ " onClick=\"printItemizedWorkOrder();\">"
						+ ORDERITEMIZEDWO_BUTTON_LABEL
						+ "</button>\n"
						;
						//Create a drop down for number of copies':
						s += "<SELECT "
							+ " NAME = \"" + NOOFINSTALLATIONWOCOPIES_NAME + "\""
							+ " ID = \"" + NOOFINSTALLATIONWOCOPIES_NAME + "\""
							+ ">"
						;
						for (long l = 0; l < 5; l++){
							s += "<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
								+ ">" + Long.toString(l + 1)
								;
							if (l == 0){
								s += " copy";
							}else{
								s += " copies";
							}
							s += "</OPTION>";
						}
						
						s += "</SELECT>";
					}
					
					boolean bAllowCreateInvoice = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMCreateInvoices, 
						sUserID, 
						conn,
						sLicenseModuleLevel);
					if (bAllowCreateInvoice){
						s += "<button type=\"button\""
						+ " value=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
						+ " name=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
						+ " onClick=\"createInvoice();\">"
						+ ORDERINVOICE_BUTTON_LABEL
						+ "</button>\n"
						;
					}
					s += "<button type=\"button\""
					+ " value=\"" + ORDERCLONE_BUTTON_LABEL + "\""
					+ " name=\"" + ORDERCLONE_BUTTON_LABEL + "\""
					+ " onClick=\"cloneOrder();\">"
					+ ORDERCLONE_BUTTON_LABEL
					+ "</button>\n"
					;
				}
					s += "</TD></TR>"
				;
		
		//Close the table:
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		s += "</TD></TR>";
		return s;
	}

	private String sCommandScripts(SMOrderHeader order, Connection conn) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		s += "<script type=\"text/javascript\">\n";
		
		s += "function validateForm(){\n"
			
			+ "    var sdiscdesc = document.getElementById(\"" 
						+ SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").value;\n"
						//If the discount amt or percentage are not BOTH zero, there must be a discount description:
			+ "    if (sdiscdesc == ''){\n"
			+ "        var sdiscountpercentage = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").value;\n"
			+ "        if (isNumeric(sdiscountpercentage) == false){\n"
			+ "            alert(\"If the discount percentage is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "        discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "        if (discountpercentage != 0.00){\n"
			+ "            alert(\"If the discount percentage is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			//Now check the discount AMT:
			+ "        var sdiscountamount = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").value;\n"
			+ "        if (isNumeric(sdiscountamount) == false){\n"
			+ "            alert(\"If the discount amount is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "        discountamount = getFloat(sdiscountamount);\n"
			+ "        if (discountamount != 0.00){\n"
			+ "            alert(\"If the discount amount is not zero, you MUST enter a discount description.\");\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").focus();\n"
			+ "            document.getElementById(\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\").select();\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "    }\n"

			//Validate number fields:
			+ "    var sdiscountpercentage = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").value;\n"
			+ "    if (isNumeric(sdiscountpercentage) == false){\n"
			+ "        alert(\"Discount percentage '\" + sdiscountpercentage + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "    if (discountpercentage < 0.00){\n"
			+ "        alert(\"Discount percentage cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    if (discountpercentage > 100.00){\n"
			+ "        alert(\"Discount percentage cannot be greater than 100 percent.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			+ "    var sdiscountamount = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").value;\n"
			+ "    if (isNumeric(sdiscountamount) == false){\n"
			+ "        alert(\"Discount amount '\" + sdiscountamount + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountamount = getFloat(sdiscountamount);\n"
			+ "    if (discountamount < 0.00){\n"
			+ "        alert(\"Discount amount cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    return true;\n"
			+ "}\n"
			;
	
		s += "function isInteger(value) {\n"
			+ "    try {\n"
			+ "        var inpVal = parseInt(value.replace(',', ''), 10);\n"
			+ "        if (isNaN(inpVal)) {\n"
			+ "            return false;\n"
			+ "        }\n"
			+ "    } catch (e) {\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    return true;\n"
			+ "}\n"
		;

		s += "function isNumeric(value) {\n"
			+ "    if ((value == null) || (value == '')) return false;\n"
			+ "    var strippedstring = value.replace(',', '');\n"
			+ "    if (!strippedstring.toString().match(/^[-]?\\d*\\.?\\d*$/)) return false;\n"
			+ "    return true\n"
			+ "    }\n"
		;
		
		s += "function getFloat(value) {\n"
			+ "    return parseFloat(value, 10);\n"
			+ "}\n"
		;
		
		s += "function discountPercentageChanged() {\n"
			+ "    document.getElementById(\"" + DISCOUNTCHANGE_FLAG + "\").value = \"" 
				+ DISCOUNTCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "    var sdiscountpercentage = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sdiscountpercentage) == false){\n"
			+ "        alert(\"Discount percentage '\" + sdiscountpercentage + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountpercentage = getFloat(sdiscountpercentage);\n"
			+ "    if (discountpercentage < 0.00){\n"
			+ "        alert(\"Discount percentage cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    if (discountpercentage > 100.00){\n"
			+ "        alert(\"Discount percentage cannot be greater than 100 percent.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			//Recalculate the discount amount based on the new discount percentage:
			+ "    var extorderprice = getFloat(document.getElementById(\"" 
				+ SMOrderHeader.ParamdTotalAmountItems + "\").value.replace(',',''));"
			+ "    var discountamt = Math.round((discountpercentage * extorderprice / 100.00) * 100) / 100.00;"
			+ "    document.getElementById(\"" 
					+ SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").value = discountamt;\n"
			+ "}\n"
		;
		
		s += "function discountAmountChanged() {\n"
			+ "    document.getElementById(\"" + DISCOUNTCHANGE_FLAG + "\").value = \"" 
				 + DISCOUNTCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "    var sdiscountamount = document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sdiscountamount) == false){\n"
			+ "        alert(\"Discount amount '\" + sdiscountamount + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    discountamount = getFloat(sdiscountamount);\n"
			+ "    if (discountamount < 0.00){\n"
			+ "        alert(\"Discount amount cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"

			+ "    var extorderprice = getFloat(document.getElementById(\"" 
				+ SMOrderHeader.ParamdTotalAmountItems + "\").value.replace(',',''));"
			+ "    if (extorderprice == 0.00){\n"
			+ "        document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").value = 0.00;\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    var discpercentage = Math.round(((discountamount * 100.00) / extorderprice) * 100) / 100;"
			+ "    document.getElementById(\"" 
				+ SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\").value = discpercentage;\n"
			+ "}\n"
		;
		
		s += "function depositamountChanged() {\n"
			+ "    document.getElementById(\"" + DISCOUNTCHANGE_FLAG + "\").value = \"" + DISCOUNTCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "    var sdepositamount = document.getElementById(\"" 
				+ SMOrderHeader.Parambddepositamount + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sdepositamount) == false){\n"
			+ "        alert(\"Deposit amount '\" + sdiscountamount + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambddepositamount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambddepositamount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    depositamount = getFloat(sdiscountamount);\n"
			+ "    if (depositamount < 0.00){\n"
			+ "        alert(\"Deposit amount cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambddepositamount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambddepositamount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this page?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;

		s += "function saveOrder(){\n"
			+ "    if (!validateForm()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + SAVECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function gotoDetails(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + DETAILSCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function gotoHeader(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + HEADERCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function printServiceWorkOrder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + PRINTSERVICEWOCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function printItemizedWorkOrder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + PRINTITEMIZEDWOCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function createInvoice(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + CREATEINVOICECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function cloneOrder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + CLONEORDERCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function initShortcuts() {\n";
		s += "    shortcut.add(\"Alt+h\",function() {\n";
		s += "        gotoHeader();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        gotoDetails();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        saveOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        saveOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        printServiceWorkOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        printItemizedWorkOrder();\n";
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
		s += "        cloneOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "}\n";
		s += "\n";
		
		s += "window.onload = function(){\n"
			+ "    document.forms.MAINFORM." + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + ".focus();\n"
			+ "    initShortcuts();\n"
			+ "}\n\n"
		;
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
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "}"
			+ "\n"
			;

		//This is the def for a control on the screen:
		s +=
			"td.fieldcontrol {"
			+ "height: " + sRowHeight + "; "
			+ "text-align: left; "
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
			"th.orderlineheading {"
			+ "height: " + sRowHeight + "; "
			+ "vertical-align: text-bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: white; "
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
