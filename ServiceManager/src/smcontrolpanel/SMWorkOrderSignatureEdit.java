package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOption;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderDetail;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableworkorderdetails;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smic.ICItem;

public class SMWorkOrderSignatureEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION = "REMOVEWOATTRIBUTE";

	//Commands:
	public static final String EDIT_BUTTON_LABEL = "<B><FONT COLOR=RED>E</FONT></B>dit work order"; //E
	public static final String EDITCOMMAND_VALUE = "EDIT";
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String VIEW_PRICING_LABEL = "<B><FONT COLOR=RED>V</FONT></B>iew prices"; //V
	public static final String VIEW_PRICING_COMMAND_VALUE = "VIEWPRICING";
	public static final String REMOVE_PRICING_LABEL = "<B><FONT COLOR=RED>R</FONT></B>emove prices"; //R
	public static final String REMOVE_PRICING_COMMAND_VALUE = "HIDEPRICING";
	public static final String CLEAR_SIGNATURE_BUTTON_LABEL = "<B><FONT COLOR=RED>C</FONT></B>lear signature"; //C
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String APPROVED_CHECKBOX = "APPROVEDCHECKBOX";
	public static final String SIGNATURE_CHECKBOX = "SIGNATURECHECKBOX";
	public static final String WORK_PERFORMED_CHECKBOX = "WPCCHECKBOX";
	public static final String SIGNATURE_JSON_OUTPUT_FIELD_NAME = "outputsignature";
	//private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMTableworkorders.ObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMWorkOrderSignatureAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditWorkOrders
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditWorkOrders, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		SMWorkOrderHeader wohead = new SMWorkOrderHeader();
		try {
			wohead.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smedit.getPWOut().println("Error [1430251037] loading work order from request: " + e2.getMessage());
			return;
		}
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a work order object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (!clsServletUtilities.isSessionValid(currentSession)){
			smedit.getPWOut().println("Error [1430251038] - session is invalid.");
			return;
		}
		//This minimizes the chance that we'll have a work order object in a session that we shouldn't have floating around:
		if (clsManageRequestParameters.get_Request_Parameter(REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION, request).compareToIgnoreCase("") != 0){
			currentSession.removeAttribute(SMTableworkorders.ObjectName);
		}
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1430251039] getting session attribute - " + e1.getMessage());
			return;
		}
		if (currentSession.getAttribute(SMTableworkorders.ObjectName) != null){
			wohead = (SMWorkOrderHeader) currentSession.getAttribute(SMTableworkorders.ObjectName);
			currentSession.removeAttribute(SMTableworkorders.ObjectName);

			//Here we want to check to make sure that the ID of the work order passed in the request matches what's in the
			//session object.  If they don't match it's possible that we've picked up a work order session object from a
			//different work order, possibly one that was passed on from another tab in the browser:
			String sWOIDFromRequest = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramlid, request);
			String sWOIDFromSession = wohead.getlid();
			if(sWOIDFromSession.compareToIgnoreCase(sWOIDFromRequest) != 0){
				currentSession.removeAttribute(SMTableworkorders.ObjectName);
				smedit.getPWOut().println("Error [1430251040] work order ID in request ('" +  sWOIDFromRequest 
						+ "') and work order ID from session object ('" + sWOIDFromSession + "') do not match.");
				return;
			}

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			try {
				if(!wohead.load(smedit.getsDBID(), smedit.getFullUserName(), getServletContext())){
					smedit.getPWOut().println("Error [1430251041] work order # " + wohead.getlid() + "' could not be loaded.");
					return;
				}
			} catch (Exception e) {
				smedit.getPWOut().println("Error [1430251042] loading work order - " + e.getMessage());
				return;
			}
		}

		smedit.getPWOut().println(getHeaderString(
				"Work order acceptance", 
				"", 
				SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
				SMUtilities.DEFAULT_FONT_FAMILY,
				sCompanyName
				))
		;

		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		if (sStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B>" + sStatus + "</B><BR>");
		}		
		boolean bViewPrices = false;
		if(clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_COMMAND_VALUE, smedit.getRequest()).compareToIgnoreCase("Y")==0) {
			wohead.setiViewPrices("1");
			bViewPrices = true;
		}else {
			wohead.setiViewPrices("0");
		}
		//Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to user login</A><BR><BR>");

		//Determine which edit mode we want to use:
		try{
			smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
					+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
					);
			createEditPage(
					getEditHTML(smedit, wohead, SMTableworkorders.ObjectName,bViewPrices),
					SMWorkOrderHeader.FORM_NAME,
					smedit.getPWOut(),
					smedit
					);
		} catch (Exception e) {
			String sError = "Could not create edit page - " + e.getMessage();
			smedit.getPWOut().println(sError);
			return;
		}
		return;
	}
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm
			) throws Exception{

		String sFormString = "<FORM ID='" + SMWorkOrderHeader.FORM_NAME + "' NAME='" + SMWorkOrderHeader.FORM_NAME + "' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";

		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");

	}

	private String getEditHTML(SMMasterEditEntry sm, SMWorkOrderHeader wo_entry, String sObjectName, Boolean bViewPrices) throws Exception{
		String s = "";
		s += sCommandScripts(wo_entry, sm);
		s += sSignatureScripts(wo_entry);
		s += sStyleScripts();

		//Store whether or not the record has been changed this includes ANY change, including approval:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
				+ ">";

		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + COMMAND_FLAG + "\""
				+ "\">";

		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlid + "\" VALUE=\"" 
				+ wo_entry.getlid() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramltimestamp + "\" VALUE=\"" 
				+ wo_entry.getstimestamp() + "\">";
		//Record the time the user last loaded the work order:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlastreadrecordtimestamp + "\" VALUE=\"" 
				+ wo_entry.getslastreadrecordtimestamp() + "\">" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\" VALUE=\"" 
				+ wo_entry.getstrimmedordernumber() + "\">";

		//Store whether we are in 'view pricing' mode:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + VIEW_PRICING_COMMAND_VALUE + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_COMMAND_VALUE, sm.getRequest()) + "\""
				+ " id=\"" + VIEW_PRICING_COMMAND_VALUE + "\""
				+ "\">";
		//Record if the Prices have been viewed
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.ParamiViewPrices + "\" VALUE=\"" 
				+ wo_entry.getiViewPrices() + "\">";
		//New Row
		s += "<TR>";

		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";		

		SMOrderHeader orderheader = new SMOrderHeader();
		orderheader.setM_strimmedordernumber(wo_entry.getstrimmedordernumber());
		if(!orderheader.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
			throw new Exception("Error [1430252612] - unable to load order # '" + wo_entry.getstrimmedordernumber() 
			+ "' - " + orderheader.getErrorMessages());
		}

		//Header information:
		s += "<TR><TD>" 
				+ createOrderHeaderTable(
						sm,
						getServletContext(),
						wo_entry, 
						orderheader, 
						SMUtilities.getFullClassName(this.toString())) 
				+ "</TD></TR>";

		//Create the order commands line at the top:
		s += "<TR><TD>" + createCommandsTable(wo_entry, sm,bViewPrices) + "</TD></TR>";

		s += "<TR><TD>" + createItemsTable(sm, wo_entry, bViewPrices) + "</TD></TR>";

		//Create work performed codes table:
		s += "<TR><TD>" + createWorkPerformedTable(sm, wo_entry, orderheader);

		//TODO Insert create Totals Table

		//Create the comments area table:
		s += "<TR><TD>" + createMechanicInfoTable(sm, wo_entry) + "</TD></TR>";

		//Create the terms table:
		s += "<TR><TD>" + createTermsTable(sm, orderheader) + "</TD></TR>";

		//Create the signature block table:
		s += "<TR><TD>" + createSignatureBlockTable(sm, wo_entry) + "</TD></TR>";

		//Create the order commands line at the bottom:
		s += "<TR><TD>" + createCommandsTable(wo_entry,sm,bViewPrices) + "</TD></TR>";

		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";

		return s;
	}
	private String createMechanicInfoTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder) throws SQLException{
		String s = "";
		int iColSpan = 5;

		s += "<TABLE class = \" innermost \" style=\" title:MechanicInfoTable; background-color: "
				+ SMWorkOrderHeader.COMMENTS_TABLE_BG_COLOR + "; \" width=100% >\n";

		//Work order comments:
		s += "<TR>";
		s += "<TD><U><B>Description of work performed:</B></U></TD>";

		//Set the date:
		s += "<TD class=\" fieldlabel \">Date of work:&nbsp;</TD>";
		s += "<TD>" + workorder.getdattimedone() + "</TD>";

		//Mechanic:	
		s += "<TD class=\" fieldlabel \">Mechanic:&nbsp;</TD>";
		s += "<TD>" + workorder.getmechanicsname() + "</TD>";

		s += "</TR>";

		s += "<TR>";
		s += "<TD COLSPAN=" + Integer.toString(iColSpan) + ">"
				+ workorder.getmcomments().replace("\"", "&quot;")
				+ "</TD>"
				;
		s += "</TR>";

		//Close the table:
		s += "</TABLE style = \" title:MechanicInfoTable; \">\n";
		return s;
	}

	private String createCommandsTable(
			SMWorkOrderHeader wo_order, SMMasterEditEntry sm, Boolean bViewPrices){
		String s = "";

		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
				+ SMWorkOrderHeader.ORDERCOMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";

		s += createEditButton();

		//SAVE button:
		//We need to be able to save no matter what:
		s += createSaveButton();
		if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewPricingOnWorkOrders, 
						sm.getUserID(), 
						getServletContext(), 
						sm.getsDBID(),
						(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				){
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMZeroWorkOrderItemPrices, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
				//No need to show the button unless it's turned off currently:
				if (!bViewPrices){
					s += createViewPricesButton();
				}else{
					s += createRemovePricesButton();
				}
			}else{
				//No need to show the button unless it's turned off currently:
				if (!bViewPrices){
					s += createViewPricesButton();
				}else{
					s += createRemovePricesButton();
				}
			}
		}

		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		return s;
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createEditButton(){
		return "<button type=\"button\""
				+ " value=\"" + EDIT_BUTTON_LABEL + "\""
				+ " name=\"" + EDIT_BUTTON_LABEL + "\""
				+ " onClick=\"gotoeditmode();\">"
				+ EDIT_BUTTON_LABEL
				+ "</button>\n"
				;
	}

	private String createViewPricesButton(){
		return "<button type=\"button\""
				+ " value=\"" + VIEW_PRICING_LABEL + "\""
				+ " name=\"" + VIEW_PRICING_LABEL + "\""
				+ " onClick=\"viewpricing();\">"
				+ VIEW_PRICING_LABEL
				+ "</button>\n"
				;
	}

	private String createRemovePricesButton(){
		return "<button type=\"button\""
				+ " value=\"" + REMOVE_PRICING_LABEL + "\""
				+ " name=\"" + REMOVE_PRICING_LABEL + "\""
				+ " onClick=\"removepricing();\">"
				+ REMOVE_PRICING_LABEL
				+ "</button>\n"
				;
	}

	private String createItemsTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			Boolean bViewPrices) throws Exception{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:ItemsTable; background-color: "
				+ SMWorkOrderHeader.ITEMS_TABLE_BG_COLOR + "; \" >\n";

		s += createItemsTableForWorkOrderSigning(sm, workorder,bViewPrices);

		//Close the table:
		s += "</TABLE style = \" title:ItemsTable; \">\n";

		return s;
	}
	private String createItemsTableForWorkOrderSigning(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			Boolean bViewPrices) throws Exception{
		String s = "";
		s += displayItemsForCustomerView(workorder, sm, bViewPrices);
		return s;
	}

	private String displayItemsForCustomerView(
			SMWorkOrderHeader workorder,
			SMMasterEditEntry sm,
			Boolean bViewPrices
			) throws Exception{
		String s = "";

		//First display the parts used, not the labor:
		s += "<TR><TD><B><U>ITEMS USED:</U></B></TD>";
		s += "<TR>";
		s += "<TD class=\" fieldrightheading \">Item qty&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Item #</TD>";
		s += "<TD class=\" fieldleftheading \">Description&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">UOM&nbsp;</TD>";
		s += "</TR>";
		;

		SMOrderHeader orderheader = new SMOrderHeader();
		orderheader.setM_strimmedordernumber(workorder.getstrimmedordernumber());
		if (workorder.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			if (!orderheader.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
				throw new Exception("Could not load order header '" + workorder.getstrimmedordernumber()
				+ "' - " + orderheader.getErrorMessages());
			}
		}

		//IF we need to show prices, we are going to load the order into a new object, to be used ONLY for calculating prices and totals:
		SMOrderHeader dummyorder = new SMOrderHeader();
		dummyorder.setM_strimmedordernumber(orderheader.getM_strimmedordernumber());
		if (orderheader.getM_strimmedordernumber().compareToIgnoreCase("") != 0){
			if (!dummyorder.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getsDBID())){
				throw new Exception("Error loading order to calculate prices - " + dummyorder.getErrorMessages() + ".");
			}
		}
		//First, remove all the lines on the dummy order so we can use it to recalculate only the items on the work order:
		dummyorder.getM_arrOrderDetails().clear();

		for (int i = 0; i < workorder.getDetailCount(); i++){
			//IF it's an item, not a work performed code:
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0){

				//We need to add a dummy line to the dummy order, so we can calculate prices and totals:
				SMOrderDetail dummydetail = new SMOrderDetail();
				dummydetail.setM_dQtyOrdered(workorder.getDetailByIndex(i).getsbdquantity().replace(",",""));
				dummydetail.setM_dQtyShipped(workorder.getDetailByIndex(i).getsbdquantity().replace(",",""));
				dummydetail.setM_dUniqueOrderID(orderheader.getM_siID());
				dummydetail.setM_strimmedordernumber(orderheader.getM_strimmedordernumber());
				dummydetail.setM_sItemNumber(workorder.getDetailByIndex(i).getsitemnumber());
				//IF it's a 'configured' line that already exists on the order, we need to use the UNIT PRICE
				//from that line on the order, not necessarily the calculated price for the item:
				int iDetailNumber = 0;
				try {
					iDetailNumber = Integer.parseInt(workorder.getDetailByIndex(i).getsorderdetailnumber());
				} catch (Exception e2) {
					throw new Exception("Error [1430769480] parsing detail number '" + workorder.getDetailByIndex(i).getsorderdetailnumber() + "' - " + e2.getMessage());
				}

				BigDecimal bdQtyAssigned = new BigDecimal("0.0000");
				if (workorder.getDetailByIndex(i).getsbdqtyassigned().compareToIgnoreCase("") == 0){
					workorder.getDetailByIndex(i).setsbdqtyassigned("0.0000");
				}
				try {
					bdQtyAssigned = new BigDecimal(workorder.getDetailByIndex(i).getsbdqtyassigned().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1430769481] parsing qty assigned '" + workorder.getDetailByIndex(i).getsbdqtyassigned() + "' - " + e1.getMessage());
				}
				//Get the actual order detail that corresponds to this work order detail:
				SMOrderDetail actualorderdetail = new SMOrderDetail();
				if (iDetailNumber > 0){
					actualorderdetail = orderheader.getOrderDetailByDetailNumber(workorder.getDetailByIndex(i).getsorderdetailnumber());
				}else{
					actualorderdetail = null;
				}
				//Try to figure out if it's taxable from the item master:
				String sTaxable = "1";
				ICItem item = new ICItem(workorder.getDetailByIndex(i).getsitemnumber());
				if (item.load(getServletContext(), sm.getsDBID())){
					sTaxable = item.getTaxable();
					//If we can't load the item, then check the order detail, if there is one:
				}else{
					if (actualorderdetail != null){
						sTaxable = actualorderdetail.getM_iTaxable();
					}
				}
				dummydetail.setM_iTaxable(sTaxable);
				//Try to set the correct unit price here:
				//If the order detail is a configured item, and if it corresponds to an actual order line:
				if ((actualorderdetail != null) && (bdQtyAssigned.compareTo(BigDecimal.ZERO)) > 0){
					try {
						//Set the unit price to match the unit price on the order for this line:
						dummydetail.setM_dOrderUnitPrice(actualorderdetail.getM_dOrderUnitPrice());
						//extend the unit price times the qty:
						orderheader.calculateExtendedPrice(dummydetail);
					} catch (Exception e) {
						throw new SQLException(e.getMessage() + ".");
					}
				}else{
					//Don't bother with calculating an item on a work order if it has not order associated with it:
					if (
							(orderheader.getM_strimmedordernumber().compareToIgnoreCase("") != 0)
							&& (orderheader.getM_strimmedordernumber() != null)
							){
						//Otherwise, just calculate the price for this item and this qty, disregarding the unit price on the order:
						try {
							dummyorder.updateLinePrice(dummydetail, sm.getsDBID(), sm.getUserName(), getServletContext());
						} catch (Exception e) {
							throw new Exception ("Error [1431442449] updating price for item '" + dummydetail.getM_sItemNumber() + "' - " + e.getMessage());
						}
					}
				}
				String sExtendedPrice = dummydetail.getM_dExtendedOrderPrice();
				if(workorder.getDetailByIndex(i).getssetpricetozero().compareToIgnoreCase("1") == 0) {
					//Set the work order detail price to 0.00 so the display price is 0.00
					sExtendedPrice = "0.00";
					//Set the dummy order detail to 0.00 so totals are calculated correctly on the work order
					dummydetail.setM_dExtendedOrderPrice("0.00");
				}

				workorder.getDetailByIndex(i).setsbdextendedprice(sExtendedPrice);
				dummyorder.addNewDetail(dummydetail);
			}
		}

		BigDecimal bdShippedValue = new BigDecimal("0.00");
		BigDecimal bdTotalExtendedLaborPrice = new BigDecimal("0.00");
		Double dWODiscountAmount= Double.valueOf(workorder.getdPrePostingWODiscountAmount());
		BigDecimal bdWODiscountAmount = new BigDecimal(workorder.getdPrePostingWODiscountAmount());
		for (int i = 0; i < dummyorder.get_iOrderDetailCount(); i++){
			bdShippedValue = bdShippedValue.add(new BigDecimal(dummyorder.getOrderDetail(i).getM_dExtendedOrderPrice().replace(",", "")));
			boolean bIsLaborItem = false;
			ICItem item = new ICItem(dummyorder.getOrderDetail(i).getM_sItemNumber());
			if(!item.load(getServletContext(), sm.getsDBID())){
				//TJR - 1/12/2015 - we are assuming that if we can't read this item, it's NOT a labor item:
				//System.out.println(" [1421078747] Cannot find item '" + dummyorder.getOrderDetail(i).getM_sItemNumber()
				//	+ "' to calculate material and labor totals.");
			}else{
				bIsLaborItem = item.getLaborItem().compareToIgnoreCase("1") == 0;
			}
			if (bIsLaborItem){
				bdTotalExtendedLaborPrice = bdTotalExtendedLaborPrice.add(new BigDecimal(dummyorder.getOrderDetail(i).getM_dExtendedOrderPrice().replace(",", "")));
			}
		}
		//Display each of the items on the order:
		int iNumberOfItemLines = 0;
		//We'll display all NON-LABOR items on the work order first:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			//IF it's an item, not a work performed code:
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0){
				ICItem item = new ICItem(workorder.getDetailByIndex(i).getsitemnumber());
				boolean bIsLaborItem = false;
				if(!item.load(getServletContext(), sm.getsDBID())){
					//ITEMERROR
					//We will have to assume that if the item is deleted, it's NOT a labor item, for now at least...
					System.out.println(" [1421078746] Cannot find item '" + workorder.getDetailByIndex(i).getsitemnumber() + "'.");
				}else{
					bIsLaborItem = item.getLaborItem().compareToIgnoreCase("1") == 0;
				}
				if (!bIsLaborItem){
					iNumberOfItemLines++; //This will be one-based, rather than zero-based
					try {
						s += buildItemLineForCustomerViewing(
								false,
								workorder.getDetailByIndex(i),
								iNumberOfItemLines,
								sm
								);
					} catch (Exception e) {
						throw new Exception (" [1402415095] - error in buildItemLineForCustomerViewing - " + e.getMessage());
					}
				}
			}
		}

		//Now display the hours used:
		s += "<TR><TD><B><U>HOURS USED:</U></B></TD>";
		s += "<TR>";
		s += "<TD class=\" fieldrightheading \">Qty&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Labor ID&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Description&nbsp;:</TD>";
		s += "<TD class=\" fieldleftheading \">UOM:&nbsp;</TD>";
		//if (bShowPrices){
		//	s += "<TD class=\" fieldleftheading \">Extended price:&nbsp;</TD>";
		//}
		s += "</TR>"
				;

		//Next we'll show the labor lines:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			//IF it's an item, not a work performed code:
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0){
				//If the line is ALSO on the order:
				ICItem item = new ICItem(workorder.getDetailByIndex(i).getsitemnumber());
				if(!item.load(getServletContext(), sm.getsDBID())){
					throw new Exception("Cannot find labor item '" + workorder.getDetailByIndex(i).getsitemnumber() + "'.");
				}
				if (item.getLaborItem().compareToIgnoreCase("1") == 0){
					iNumberOfItemLines++; //This will be one-based, rather than zero-based
					s += buildLaborLineForCustomerViewing(false, workorder.getDetailByIndex(i));
				}
			}
		}

		if(bViewPrices) {
			s += "<TR><TD><B><U>PRICES:</U></B></TD>";
			//Add a row for the tax:
			String sTaxAmount;
			try {
				sTaxAmount = dummyorder.getTaxAmount(sm.getsDBID(), sm.getUserName(), getServletContext());
			} catch (Exception e) {
				sTaxAmount = e.getMessage();
			}
			if((dWODiscountAmount != 0) || (sTaxAmount.compareToIgnoreCase("0.00")!=0)) {
			s += "<TR>"

					+ "<TD align=left><FONT SIZE=2><B>Subtotal:&nbsp;&nbsp;&nbsp;"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue) + "</B></FONT>"
					+ "</TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "</TR>"
					;
			if(dWODiscountAmount != 0	) {
				s += "<TR>"

						+ "<TD align=left><FONT SIZE=2><B>Discount:&nbsp;&nbsp;"
						+ "<FONT SIZE=2>" 
						+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdWODiscountAmount) + "</B></FONT>"
						+ "</TD>"
						+ "<TD></TD>"
						+ "<TD></TD>"
						+ "<TD></TD>"
						+ "</TR>"
						;
			}
			//Set Discount to the WO discount amount, rather than the whole Order
			dummyorder.setM_dPrePostingInvoiceDiscountAmount(workorder.getdPrePostingWODiscountAmount());


			if(sTaxAmount.compareToIgnoreCase("0.00")!=0) {
				s += "<TR>"

					+ "<TD align=left><FONT SIZE=2><B>Tax:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "" + sTaxAmount + "</B></FONT>"
					+ "</TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "</TR>"
					;
			}
			}
			//Add a row for total INCLUDING tax:
			String sTotalWithTax = "";
			try {
				sTotalWithTax = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(BigDecimal.valueOf(dWODiscountAmount)).add(new BigDecimal(sTaxAmount.replace(",", ""))));
			} catch (Exception e) {
				s += "Error [1390339789] calculating Total With Tax - " + e.getMessage();
			}
			s += "<TR>"
					+ "<TD align=left><FONT SIZE=2><B>Total:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "" + sTotalWithTax + "</B></FONT>"
					+ "</TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "<TD></TD>"
					+ "</TR>"
					;

		}
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED + "\" VALUE=\"" + Integer.toString(iNumberOfItemLines) + "\"" + ">";
		return s;
	}

	private String buildItemLineForCustomerViewing(
			boolean bShowPrices,
			SMWorkOrderDetail wodetail,
			int iLineNumber,
			SMMasterEditEntry sm
			) throws Exception{
		String s = "";
		s += "<TR>";
		boolean bShowLine = false;
		BigDecimal bdQty;
		try {
			bdQty = new BigDecimal(wodetail.getsbdquantity().replace(",", ""));
		} catch (Exception e) {
			throw new Exception("Qty '" + wodetail.getsbdquantity() + "' is not valid on item " + wodetail.getsitemnumber() + ".");
		}
		if (bdQty.compareTo(BigDecimal.ZERO) != 0){
			bShowLine = true;
		}
		//QTY USED:
		if (bShowLine){
			s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdquantity() + "</TD>" + "\n";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemnumber() + "</TD>" + "\n";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemdesc() + "</TD>" + "\n";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsuom() + "</TD>" + "\n";
			if (bShowPrices){
				//Extended price COLUMN
				s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdextendedprice() + "</TD>" + "\n";
			}
		}

		s += "</TR>"
				;
		return s;
	}

	private String buildLaborLineForCustomerViewing(
			boolean bShowPrices,
			SMWorkOrderDetail wodetail
			) throws Exception{
		String s = "";

		//We don't show this unless it has a qty:
		boolean bShowLine = false;
		BigDecimal bdQty = new BigDecimal(wodetail.getsbdquantity().replace(",", ""));
		if (bdQty.compareTo(BigDecimal.ZERO) != 0){
			bShowLine = true;
		}
		s += "<TR>";

		//Qty used
		if (bShowLine){
			s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdquantity() + "</TD>";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemnumber() + "</TD>";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemdesc() + "</TD>";
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsuom() + "</TD>";
			if (bShowPrices){
				s += "<TD class=\"readonlyrightfield\">" + "&nbsp;" + "</TD>";
				s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdextendedprice() + "</TD>";
			}
		}

		s += "</TR>";
		return s;
	}
	private String createWorkPerformedTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			SMOrderHeader order
			) throws Exception{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:WorkPerformedTable; background-color: "
				+ SMWorkOrderHeader.WORKPERFORMED_TABLE_BG_COLOR + "; \" >\n";	
		s += "<TR><TD><U><B>Work performed codes:</B></U></TD></TR>";

		//If this is read only, just load all the WPC's from the work order:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0){
				s += "<TR>"
						+ "<TD class=\"readonlyleftfield\">"
						+ workorder.getDetailByIndex(i).getsworkperformed()
						+ "</TD>"
						+ "</TR>"
						+ "\n"
						;
			}
		}
		//Close the table:
		s += "</TABLE style = \" title:WorkPerformedTable; \">\n";
		return s;
	}
	private String sSignatureScripts(SMWorkOrderHeader workorder){
		//Scripts for signature:

		String s = "<script src=\"scripts/jquery-signaturepad-min-02.js\"></script>\n";
		s += "<script src=\"scripts/json2.min.js\"></script>\n"
				;
		return s;
	}

	private String createTermsTable(
			SMMasterEditEntry sm, 
			SMOrderHeader orderheader) throws Exception{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:TermsTable; background-color: "
				+ SMWorkOrderHeader.TERMS_TABLE_BG_COLOR + "; \" width=100% >\n";

		s += "<TR><TD><U><B>Terms And Conditions Of Agreement</B></U></TD></TR>";
		s += "<TD class=\"readonlyleftfield\" >";
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName
				+ " WHERE ("
				+ "(" + SMTableservicetypes.sCode + " = '" + orderheader.getM_sServiceTypeCode() + "')"
				+ ")"
				;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".createTermsTable - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			if (rs.next()){
				s += rs.getString(SMTableservicetypes.mworkorderterms).replace("\n", "<BR>");
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error getting terms codes with SQL: " + SQL + " - " + e.getMessage());
		}
		s += "</TD>";
		//Close the table:
		s += "</TABLE style = \" title:TermsTable; \">\n";
		return s;
	}

	private String createSignatureBlockTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder) throws SQLException{

		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:SignatureBlockTable; background-color: "
				+ SMWorkOrderHeader.SIGNATUREBLOCK_TABLE_BG_COLOR + "; \" >\n";

		s += "<TR>";
		s += "<TD><U><B>Additional work required:</B></U></TD>";
		s += "</TR>";

		s += "<TR><TD>" + workorder.getmadditionalworkcomments();

		//We also have to store the additional work comments here to carry them, because when the work order
		// is validated, it needs to see whether there are any 'additional work comments' to turn off the
		// 'additional work authorization' checkbox if there IS no additional work noted:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.Parammadditionalworkcomments
				+ "\""
				+ " id = \"" + SMWorkOrderHeader.Parammadditionalworkcomments + "\""
				+ " VALUE=\"" + workorder.getmadditionalworkcomments() + "\""
				+ ">"
				;

		s += "</TD></TR>";
		s += "<TR><TD>";
		s += "<INPUT TYPE=CHECKBOX ";
		if (workorder.getsiadditionalworkauthorized().compareToIgnoreCase("1") == 0){
			s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += " NAME=\"" + SMWorkOrderHeader.Paramiadditionalworkauthorized + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramiadditionalworkauthorized + "\""
				+ " onchange=\"flagDirty();\""
				+ " width=0.25>";
		;
		s += "<B>&nbsp<--&nbspCheck this box to authorize the additional work listed above - if this box is <I><B>NOT</B></I> checked, "
				+ "you have <I><B>NOT</B></I> authorized "
				+ "this additional work at this time.</B>";
		s += "</TD>";
		s += "</TR>";

		s += "<TR><TD>";
		s += "<B>Signature:</B>&nbsp;";
		//Add signature here:
		String sSignatureBoxWidth = workorder.getlsignatureboxwidth();
		//If this signature has never been saved load width from sm options.
		if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
			SMOption smoptions = new SMOption();
			try {
				smoptions.load(sm.getsDBID(), getServletContext(), sm.getUserName());
			} catch (Exception e) {
				throw new SQLException ("Error loading smoptions for signature box size.");
			}
			sSignatureBoxWidth = smoptions.getisignatureboxwidth();
			workorder.setlsignatureboxwidth(sSignatureBoxWidth);
		}
		int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
		String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.Paramlsignaturboxwidth
				+ "\""
				+ " id = \"" + SMWorkOrderHeader.Paramlsignaturboxwidth + "\""
				+ " VALUE=\"" + workorder.getlsignatureboxwidth() + "\""
				+ ">"
				;
		s +=
				"\n"
						+ "<canvas class=pad width=" + sSignatureBoxWidth + " name=signaturecanvas"
						+ " height=" + sSignatureBoxHeight + " style=\"border:1px solid  #000000;\""
						+ " onchange=\"flagDirty();\""
						+ "></canvas>\n"
						+ "<input type=hidden name='" + SMWorkOrderHeader.Parammsignature + "' class=" + SIGNATURE_JSON_OUTPUT_FIELD_NAME + ">\n"
						;	
		//SIG
		//Button to clear the signature:
		s += "<button type=\"button\""
				+ " value=\"" + CLEAR_SIGNATURE_BUTTON_LABEL + "\""
				+ " name=\"" + CLEAR_SIGNATURE_BUTTON_LABEL + "\""
				+ " onClick=\"clearsignature();\">"
				+ CLEAR_SIGNATURE_BUTTON_LABEL
				+ "</button>\n"
				;
		s += "&nbsp;<B>Date signed</B>:&nbsp;";
		if (workorder.getdattimesigned().compareToIgnoreCase(clsMasterEntry.EMPTY_DATE_STRING) == 0){
			String sCurrentDate = SMUtilities.EMPTY_DATE_VALUE;
			clsDBServerTime st = null;

			try {
				st = new clsDBServerTime(sm.getsDBID(), sm.getUserName(), getServletContext());
				sCurrentDate = st.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
			} catch (Exception e) {
				//Just let the blank date go...
			}

			workorder.setdattimesigned(sCurrentDate);
		}
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramdattimesigned + "\""
				+ " VALUE=\"" + workorder.getdattimesigned().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramdattimesigned + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "9"
				+ " MAXLENGTH=" + "10"
				//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(SMWorkOrderHeader.Paramdattimesigned, getServletContext())
				;
		s += "</TD>";
		s += "</TR>";

		s += "<TR><TD>";
		s += "<B>Printed name and title:</B>&nbsp;";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramssignedbyname + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramssignedbyname + "\""
				+ " VALUE=\"" + workorder.getssignedbyname().replace("\"", "&quot;") + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "50"
				+ " MAXLENGTH=" + Integer.toString(SMTableworkorders.ssignedbynameLength)
				+ ">"
				+ "</TD>"
				;
		s += "</TR>";

		//Close the table:
		s += "</TABLE style = \" title:SignatureBlockTable; \">\n";
		return s;
	}
	public static String createOrderHeaderTable(
			SMMasterEditEntry sm,
			ServletContext context,
			SMWorkOrderHeader workorder, 
			SMOrderHeader orderheader,
			String sClassName) throws Exception{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable; \" width=100% >\n";	

		s += "<TR>";
		String sWorkOrderID = workorder.getlid();
		String sOrderNumber = workorder.getstrimmedordernumber();
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			sOrderNumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
			+ "smcontrolpanel.SMDisplayOrderInformation"
			+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sOrderNumber + "</A>"
			;
		}
		String sPosted = "N";

		//Get the salesperson's full name:
		String SQL = "SELECT"
				+ " " + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.sSalespersonLastName
				+ " FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
				+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + orderheader.getM_sSalesperson() + "')"
				+ ")"
				;
		String sSalespersonName = "(Not found)";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sm.getsDBID(), 
					"MySQL", 
					sClassName + ".createOrderHeaderTable - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			if (rs.next()){
				sSalespersonName = rs.getString(SMTablesalesperson.sSalespersonFirstName) 
						+ " " + rs.getString(SMTablesalesperson.sSalespersonLastName); 
			}
		} catch (Exception e) {
			throw new Exception("Error [1403035522] reading salesperson name - " + e.getMessage());
		}

		//Link to all work orders:
		String sLinkToWorkOrderList = "";
		if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewOrderInformation, 
						sm.getUserID(), 
						context, 
						sm.getsDBID(),
						(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (workorder.getstrimmedordernumber().compareToIgnoreCase("") != 0)
				){
			sLinkToWorkOrderList = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
			+ "smcontrolpanel.SMDisplayOrderInformation"
			+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + orderheader.getM_strimmedordernumber() 
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			+ "#WorkOrders"
			+ "\">" + "Work order list" + "</A>"
			;
		}

		s +=
				"<TD class=\" fieldlabel \">WO&nbsp;#:&nbsp;</TD>"
						+ "<TD class=\"readonlyleftfield\">" + sWorkOrderID;

		s += "</TD>";

		if (sLinkToWorkOrderList.compareToIgnoreCase("") != 0){
			s += "<TD class=\"readonlyleftfield\">" + sLinkToWorkOrderList + "</TD>"
					;
		}
		String sImported = "N";
		if (workorder.getsimported().compareToIgnoreCase("1") == 0){
			sImported = "Y";
		}
		s +=  "<TD class=\" fieldlabel \">Scheduled:&nbsp;</TD>"
				+ "<TD class=\"readonlyleftfield\">" + workorder.getsscheduleddate() + "</TD>"

			+ "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sPosted + "</TD>"

			+ "<TD class=\" fieldlabel \">Imported?:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sImported + "</TD>"

			+ "<TD class=\" fieldlabel \">Order&nbsp;#:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + 	sOrderNumber + "</TD>"

			+ "<TD class=\" fieldlabel \">Terms:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sTerms() + "</TD>"

			+ "<TD class=\" fieldlabel \">Sales&nbsp;#:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + 	orderheader.getM_sSalesperson() + "-" + sSalespersonName + "</TD>"

			+ "<TD class=\" fieldlabel \">Special&nbsp;wage&nbsp;rate:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sSpecialWageRate() + "</TD>"			

			;
		s += "</TR>";
		s += "</TABLE title:OrderHeaderTable; \">\n";	

		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable2; \" width=100% >\n";	
		s += "<TR>";

		s += "<TD class=\" fieldlabel \">Bill&nbsp;to:&nbsp;</TD>"
				+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sBillToName() + "</TD>";

		String sMapAddress = orderheader.getM_sShipToAddress1().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress2().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress3().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress4().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToCity().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToState().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToZip().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToCountry().trim();

		s += "<TD class=\" fieldlabel \">Ship&nbsp;to:&nbsp;</TD>"
				+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShipToName() 
				//+ "</TD>"

				//+ "<TD class=\"readonlyleftfield\">" 
				+ "&nbsp;" + "<A HREF=\"" + clsServletUtilities.createGoogleMapLink(sMapAddress) + "\">" + sMapAddress + "</A>"
				+ "</TD>"

			//Ship to contact:
			+ "<TD class=\" fieldlabel \">Contact:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShiptoContact() + "</TD>"

			//Ship to phone:
			+ "<TD class=\" fieldlabel \">Phone:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShiptoPhone() + "</TD>"
			;

		//Make these fields read only:
		//Starting time:
		s += "<TD class=\" fieldlabel \">Starting time:&nbsp;</TD>"
				+ "<TD class=\"readonlyleftfield\">" + workorder.getsstartingtime() 
				+ "</TD>";

		//Assistant:
		s += "<TD class=\" fieldlabel \">Assistant:&nbsp;</TD>"
				+ "<TD class=\"readonlyleftfield\">" + workorder.getsassistant() 
				+ "</TD>";

		//Close the table:
		s += "</TABLE style = \" title:OrderHeaderTable2; \">\n";
		return s;
	}

	private String getHeaderString(
			String title, 
			String subtitle, 
			String sbackgroundcolor, 
			String sfontfamily, 
			String scompanyname){
		String s = SMUtilities.DOCTYPE
				+ "<HTML>"
				+ "<HEAD>";
		s += "<TITLE>" + subtitle + "</TITLE>"
				+ SMUtilities.faviconLink()
				//This line should keep the font widths 'screen' wide:
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
				+ "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
				+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>"
				+ "</HEAD>\n" 
				+ "<BODY BGCOLOR="
				+ "\"" 
				+ sbackgroundcolor
				+ "\""
				+ " style=\"font-family: " + sfontfamily + ";\""
				+ "\">"
				;
		s += "<TABLE BORDER=0>"
				+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
				;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	private String sCommandScripts(
			SMWorkOrderHeader workorder, 
			SMMasterEditEntry smmaster
			) throws SQLException{
		String s = "";

		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
				;

		s += "<script type='text/javascript'>\n";

		s += "function initShortcuts() {\n";

		s += "    shortcut.add(\"Alt+e\",function() {\n";
		s += "        gotoeditmode();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        save();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+v\",function() {\n";
		s += "        viewpricing();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        viewpricing();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "}\n";
		s += "\n";

		//IF this work order is editable, then the signature pad has to be editable.
		String sSignaturePadOptions = "drawOnly:true,"
				+ " output:\"." + SIGNATURE_JSON_OUTPUT_FIELD_NAME + "\","
				+ " errorMessageDraw: \"\","
				+ " lineTop:" + SMTableworkorders.SIGNATURE_TOP + ","
				+ " penWidth:" + SMTableworkorders.SIGNATURE_PEN_WIDTH + ","
				+ " penColour:" + "\"" + SMTableworkorders.SIGNATURE_PEN_COLOUR + "\","
				+ " lineColour:\"" + SMTableworkorders.SIGNATURE_LINE_COLOUR + "\"," //makes the line transparent
				+ " lineWidth:" + SMTableworkorders.SIGNATURE_LINE_WIDTH + ","
				+ " lineMargin:" + SMTableworkorders.SIGNATURE_LINE_MARGIN
				;

		//SIG
		//displayOnly
		if(workorder.getsposted().compareToIgnoreCase("1") == 0){
			sSignaturePadOptions += ", displayOnly:true";
		}

		s += "window.onload = function() {\n"
				+ "\n"
				+ "    initShortcuts();\n"

			//Display signature:
			+ "    $(document).ready("
			+ "        function () {\n"
			+ "            $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "});\n"
			;
		if (workorder.getmsignature().compareToIgnoreCase("") != 0){
			s += "        $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).regenerate(" + workorder.getmsignature()  + ");\n";
		}
		s += "        }"
				+ "    );\n";

		s += "\n"
				+ "}\n"
				;

		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
				//Check to see if the date fields were changed, and if so, flag the record was changed field:
				//+ "    if (document.getElementById(\"" + PROPOSALDATE_PARAM + "\").value != " 
				//	+ "document.getElementById(\"" + SMProposal.ParamdatproposalDate + "\").value){\n"
				//+ "        flagDirty();\n"
				//+ "    }\n"			

			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
			+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this work order?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
			;

		//Go to edit mode:
		s += "function gotoeditmode(){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before going to editing mode.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + EDITCOMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
				;
		//Save
		s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SAVECOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
				+ "}\n"
				;

		//Turn on the ability to view pricing:
		s += "function viewpricing(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + VIEW_PRICING_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
				;

		//Turn on the ability to view pricing:
		s += "function removepricing(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REMOVE_PRICING_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
				;

		//Clear signature
		s += "function clearsignature(){\n"
				+ "    flagDirty();\n"
				+ "    $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).clearCanvas();\n"
				+ "}\n"
				;

		//Flag work order dirty:
		s += "function flagDirty() {\n"
				+ "    flagRecordChanged();\n"
				+ "}\n"
				;

		s += "function flagRecordChanged() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n"
				;

		s += "function isDate(datestring) {\n"
				+ "    try {\n"
				+ "    //Change the below values to determine which format of date you wish to check. It is set to mm/dd/yyyy by default.\n"
				+ "        var DayIndex = 1;\n"
				+ "        var MonthIndex = 0;\n"
				+ "        var YearIndex = 2;\n"
				+ "\n"
				+ "        datestring = datestring.replace(\"-\", \"/\").replace(\".\", \"/\");\n"
				+ "        var SplitValue = datestring.split(\"/\");\n"
				+ "        var OK = true;\n"
				+ "        if (!(SplitValue[DayIndex].length == 1 || SplitValue[DayIndex].length == 2)) {\n"
				+ "            OK = false;\n"
				+ "        }\n"
				+ "        if (OK && !(SplitValue[MonthIndex].length == 1 || SplitValue[MonthIndex].length == 2)) {\n"
				+ "            OK = false;\n"
				+ "        }\n"
				+ "        if (OK && SplitValue[YearIndex].length != 4) {\n"
				+ "             OK = false;\n"
				+ "        }\n"
				+ "        if (OK) {\n"
				+ "            var Day = parseInt(SplitValue[DayIndex], 10);\n"
				+ "            var Month = parseInt(SplitValue[MonthIndex], 10);\n"
				+ "            var Year = parseInt(SplitValue[YearIndex], 10);\n"
				+ "\n"
				//+ "            if (OK = ((Year > 1900) && (Year < new Date().getFullYear()))) {\n"
				+ "            if (OK = ((Year > 1900) && (Year < 2050))) {\n"
				+ "                if (OK = (Month <= 12 && Month > 0)) {\n"
				+ "                    var LeapYear = (((Year % 4) == 0) && ((Year % 100) != 0) || ((Year % 400) == 0));\n"
				+ "                    if (Month == 2) {\n"
				+ "                        OK = LeapYear ? Day <= 29 : Day <= 28;\n"
				+ "                    }\n"
				+ "                    else {\n"
				+ "                        if ((Month == 4) || (Month == 6) || (Month == 9) || (Month == 11)) {\n"
				+ "                            OK = (Day > 0 && Day <= 30);\n"
				+ "                        }\n"
				+ "                        else {\n"
				+ "                            OK = (Day > 0 && Day <= 31);\n"
				+ "                        }\n"
				+ "                   }\n"
				+ "               }\n"
				+ "            }\n"
				+ "        }\n"
				+ "        return OK;\n"
				+ "    }catch (e) {\n"
				//+ "        dateObj.focus();\n"
				//+ "        dateObj.select();\n"
				+ "        return false;\n"
				+ "    }\n"
				+ "}\n"
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

		//This is the def for a right aligned field:
		s +=
				"td.fieldrightaligned {"
						+ "height: " + sRowHeight + "; "
						+ "font-weight: bold; "
						+ "text-align: right; "
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

		//This is the def for a read only field, left justified:
		s +=
				"td.readonlyleftfield {"
						+ "height: " + sRowHeight + "; "
						//+ "border-width: " + sBorderSize + "px; "
						//+ "padding: 2px; "
						//+ "border-style: none; "
						//+ "border-color: white; "
						//+ "vertical-align: text-middle;"
						//+ "background-color: black; "
						+ "font-weight: normal; "
						+ "text-align: left; "
						//+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//This is the def for a read only field, right justified:
		s +=
				"td.readonlyrightfield {"
						+ "height: " + sRowHeight + "; "
						//+ "border-width: " + sBorderSize + "px; "
						//+ "padding: 2px; "
						//+ "border-style: none; "
						//+ "border-color: white; "
						//+ "vertical-align: text-middle;"
						//+ "background-color: black; "
						+ "font-weight: normal; "
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

		//This is the def for an underlined left-aligned heading on the screen:
		s +=
				"td.fieldleftheading {"
						+ "height: " + sRowHeight + "; "
						+ "font-weight: bold; "
						+ "text-align: left; "
						+ "text-decoration:underline; "
						+ "}"
						+ "\n"
						;

		//This is the def for an underlined right-aligned heading on the screen:
		s +=
				"td.fieldrightheading {"
						+ "height: " + sRowHeight + "; "
						+ "font-weight: bold; "
						+ "text-align: right; "
						+ "text-decoration:underline; "
						+ "}"
						+ "\n"
						;


		//This is the def for the order lines heading:
		s +=
				"th.orderlineheading {"
						+ "height: " + sRowHeight + "; "
						//+ "border-width: " + sBorderSize + "px; "
						//+ "padding: 2px; "
						//+ "border-style: none; "
						//+ "border-color: white; "
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