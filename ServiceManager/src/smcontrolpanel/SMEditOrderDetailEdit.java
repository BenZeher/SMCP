package smcontrolpanel;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import smic.ICItem;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesitelocations;
import SMDataDefinition.SMTableworkperformedcodes;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditOrderDetailEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String TAX_DROP_DOWN_PARAM = "TAXDROPDOWN";
	private static final String ITEMEDIT_BG_COLOR = "#CCFFB2";
	private static final String ORDERCOMMANDS_BG_COLOR = "#99CCFF";
	private static final String WORKPERFORMEDCODES_BG_COLOR = "#FFBCA2";
	public static final String WPFCONTROL_MARKER = "WPF";
	public static final String HEADER_BUTTON_LABEL = "<B><FONT COLOR=RED>H</FONT></B>eader"; //H
	public static final String DETAILS_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>etails"; //D
	public static final String TOTALS_BUTTON_LABEL = "Dis<B><FONT COLOR=RED>c</FONT></B>ount"; //C
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVE_AND_ADD_BUTTON_LABEL = "Save and <B><FONT COLOR=RED>a</FONT></B>dd a new line"; //A
	public static final String SAVE_AND_GO_TO_NEXT_BUTTON_LABEL = "Save and go to <B><FONT COLOR=RED>n</FONT></B>ext line"; //N
	public static final String SAVE_AND_INSERT_NEXT_LINE_BUTTON_LABEL = "Save and <B><FONT COLOR=RED>i</FONT></B>nsert new line"; //I
	public static final String INSERTCPS_GROUP = "INSERTCPSGROUP";
	public static final String CONVENIENCEPHRASE_SELECT_NAME = "CPSELECT";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String SAVEANDADDCOMMAND_VALUE = "SAVEANDADD";
	public static final String SAVEANDGOTONEXTCOMMAND_VALUE = "SAVEANDGOTONEXT";
	public static final String SAVEANDINSERTNEWCOMMAND_VALUE = "SAVEANDINSERTNEW";
	public static final String DETAILSCOMMAND_VALUE = "GOTODETAILS";
	public static final String TOTALSCOMMAND_VALUE = "GOTOTOTALS";
	public static final String HEADERCOMMAND_VALUE = "GOTOHEADER";
	public static final String FINDITEMCOMMAND_VALUE = "FINDITEM";
	public static final String FINDNONDEDICATEDITEMCOMMAND_VALUE = "FINDNONDEDICATEDITEM";
	public static final String ITEMCHANGEDCOMMAND_VALUE = "ITEMCHANGED";
	public static final String PRINTSERVICEWOCOMMAND_VALUE = "PRINTSERVICEWORKORDER";
	public static final String PRINTITEMIZEDWOCOMMAND_VALUE = "PRINTITEMIZEDWORKORDER";
	public static final String CREATEINVOICECOMMAND_VALUE = "CREATEINVOICE";
	public static final String CLONEORDERCOMMAND_VALUE = "CLONEORDER";
	public static final String PRICECHANGE_FLAG = "PRICECHANGEFLAG";
	public static final String PRICECHANGED_VALUE = "PRICECHANGED";
	public static final String ITEM_FINDER_LABEL = "<B><FONT COLOR=RED>F</FONT></B>ind item"; //F
	public static final String USE_ORDER_DATE_AS_LBD_LABEL = "<B><FONT COLOR=RED>U</FONT></B>se order date";    //U
	public static final String NONDEDICATEDITEM_FINDER_LABEL = "Find n<B><FONT COLOR=RED>o</FONT></B>n-dedicated item";  //O
	public static final String UPDATE_ITEM_DATA_FLAG = "UPDATEITEMDATA";
	public static final String LASTLOADEDITEMNUMBER = "LASTLOADEDITEMNUMBER";
	public static final String INSERTNEWLINEABOVEDETAILNUMBER = "INSERTNEWLINEABOVEDETAILNUMBER";
	public static final String ITEMQTYINFO_PARAM = "ITEMQTYINFOPARAM";
	public static final String EXTESTCOST_PARAM = "EXTENDEDESTIMATEDCOST";
	public static final String COSTMULTIPLIER_PARAM = "COSTMULTIPLIER";
	public static final String COSTMULTIPLIER_BUTTON = "COSTMULTIPLIERBUTTON";
	public static final String COSTMULTIPLIER_LABEL = "Calculate <B><FONT COLOR=RED>p</FONT></B>rice"; //P
	public static final String LASTSITELABEL_PARAM = "LASTSITELABEL";
	public static final String SAVEDLINEBOOKEDDATE_PARAM = "SAVEDLINEBOOKEDDATE";
	public static final String SAVEDEXPECTEDSHIPDATE_PARAM = "SAVEDEXPECTEDSHIPDATE";
	public static final String BLANK_ITEM_CATEGORY_DESC = "** NONE SELECTED **";
	
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		SMOrderDetail detail = new SMOrderDetail(request);
		String sTrimmedOrderNumberFromRequest = detail.getM_strimmedordernumber();
		SMMasterEditEntry smedit = new SMMasterEditEntry(request,
														 response,
														 getServletContext(),
														 detail.getObjectName(),
														 SMUtilities.getFullClassName(this.toString()),
														 "smcontrolpanel.SMEditOrderDetailAction",
														 "smcontrolpanel.SMUserLogin",
														 "Go back to user login",
														 SMSystemFunctions.SMEditOrders
														 );

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		//If this is a 'resubmit', meaning it's being called by SMEditOrderDetailsAction, then
		//the session will have an order header object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		//Just in case there's one left floating around in the session:
		currentSession.removeAttribute(SMOrderHeader.ParamObjectName);
		//Record this URL so we can return to it later:

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				smedit.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " 
				+ smedit.getUserID()
				+ " - "
				+ smedit.getFullUserName()
				+ " [1331736557]");
		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOrderDetailList"
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
					+ detail.getM_strimmedordernumber()
					+ "&CallingClass=" + smedit.getCallingClass()
					+ "&Warning=" + "Could not get data connection."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
	
		SMOrderHeader order = new SMOrderHeader();
		if (currentSession.getAttribute(SMOrderDetail.ParamObjectName) != null){
			detail = (SMOrderDetail) currentSession.getAttribute(SMOrderDetail.ParamObjectName);
			currentSession.removeAttribute(SMOrderDetail.ParamObjectName);

			//Now let's make sure that the order detail in the session object matches (by order number) the order number in the request object:
			if ((sTrimmedOrderNumberFromRequest.compareToIgnoreCase("") != 0) && (sTrimmedOrderNumberFromRequest.compareToIgnoreCase(detail.getM_strimmedordernumber())) != 0){
				String sError = "Error [1425503692] Order number requested ('" + sTrimmedOrderNumberFromRequest + "')"
					 + " doesn't match the order number ('" + detail.getM_strimmedordernumber() + "') stored in the session - " + order.getErrorMessages();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&CallingClass=" + "smcontrolpanel.SMOrderDetailList"
						+ "&Warning=" + clsServletUtilities.URLEncode(sError)
				);
				return;
			}
			
			//Load the order from the order number in the session attribute:
			order.setM_strimmedordernumber(detail.getM_strimmedordernumber());
			if (!order.load(conn)){
				String sError = "Error [1425502855] Could not load order/quote from sessions object to edit detail - " + order.getErrorMessages();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&CallingClass=" + "smcontrolpanel.SMOrderDetailList"
						+ "&Warning=" + clsServletUtilities.URLEncode(sError)
				);
				return;
			}
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			//So we'll load the order from the detail object, which came in from the request:
			//Load the order from the order number in the session attribute:
			order.setM_strimmedordernumber(detail.getM_strimmedordernumber());
			if (!order.load(conn)){
				String sError = "Error [1425502856] Could not load order/quote from request string to edit detail - " + order.getErrorMessages();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&CallingClass=" + "smcontrolpanel.SMOrderDetailList"
						+ "&Warning=" + clsServletUtilities.URLEncode(sError)
				);
				return;
			}
			if (!smedit.getAddingNewEntryFlag()){
				//If we have a valid detail number, we'll try that:
				if (detail.getM_iDetailNumber().compareToIgnoreCase("") != 0){
					if(!detail.load_line(conn)){
						clsDatabaseFunctions.freeConnection(getServletContext(), conn);
						response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOrderDetailList"
								+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
								+ detail.getM_strimmedordernumber()
								+ "&CallingClass=" + smedit.getCallingClass()
								+ "&Warning=" + detail.getErrorMessages()
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
					}
				}else{
					if(!detail.load_line_using_line_number(conn)){
						clsDatabaseFunctions.freeConnection(getServletContext(), conn);
						response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOrderDetailList"
								+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
								+ detail.getM_strimmedordernumber()
								+ "&CallingClass=" + smedit.getCallingClass()
								+ "&Warning=" + detail.getErrorMessages()
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
					}
				}
			}else{
				//If we ARE adding a new detail, and it's NOT a resubmit, then initialize the order detail
				//and set the order number on it:
				detail = new SMOrderDetail();
				detail.setM_strimmedordernumber(order.getM_strimmedordernumber());
				detail.setM_sItemCategory(order.getM_sDefaultItemCategory());
				detail.setM_sLocationCode(order.getM_sLocation());
				detail.setM_datLineBookedDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
				
				//If a 'Last Site Label' was passed in, that means that the last detail had a site label
				//and we want to default this detail to that same site label:
				if (clsManageRequestParameters.get_Request_Parameter(
					LASTSITELABEL_PARAM, smedit.getRequest()).compareToIgnoreCase("") != 0){
					detail.setM_sLabel(clsManageRequestParameters.get_Request_Parameter(
					LASTSITELABEL_PARAM, smedit.getRequest()));
				}
			}
		}
		
		String sHeaderObjectName = "Order";
		if (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sHeaderObjectName = SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE);
		}
		smedit.setTitle(sHeaderObjectName + " Detail");
		smedit.printLowProfileHeaderTable();
		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Manage orders</A>");
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");

		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);

		//IF this we are returning from an item 'FIND', then we need to update the item data:
		boolean bReturningFromItemUpdate = false;
		if (request.getParameter(UPDATE_ITEM_DATA_FLAG) != null){
			bReturningFromItemUpdate = true;
			detail.setM_sItemNumber(clsManageRequestParameters.get_Request_Parameter(SMOrderDetail.ParamsItemNumber, request));
			try {
				if (bDebugMode){
					System.out.println("In " + this.toString() + " - order.getM_iCustomerDiscountLevel() = " + order.getM_iCustomerDiscountLevel());
				}
				order.updateLineWithItemData(
					detail,
					detail.getM_sItemNumber(), 
					order.getM_sDefaultPriceListCode(), 
					order.getM_iCustomerDiscountLevel(),
					true,
					conn
				);
			} catch (SQLException e) {
				String sError = "Could not update item data - " + e.getMessage();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				currentSession.setAttribute(SMOrderDetail.ParamObjectName, detail);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.getFullClassName(this.toString())
						+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG, request)
						+ "&CallingClass=" + smedit.getCallingClass()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&Warning=" + clsServletUtilities.URLEncode(sError)
				);
				return;
			}
		}
		try {
			smedit.createEditPage(getEditHTML(smedit, detail, order, conn, bReturningFromItemUpdate, sHeaderObjectName), "");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMOrderDetailList"
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + detail.getM_strimmedordernumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					+ "&CallingClass=" + smedit.getCallingClass()
					+ "&Warning=" + clsServletUtilities.URLEncode(sError)
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		return;
	}
	private String getEditHTML(
		SMMasterEditEntry sm, 
		SMOrderDetail detail, 
		SMOrderHeader order, 
		Connection cn,
		boolean bReturningFromItemUpdate,
		String sHeaderObjectName) throws SQLException{
		
		String s = "";
		s += sCommandScripts(order, detail, cn, bReturningFromItemUpdate);
		s += sStyleScripts();
		ICItem item = null;
		if (detail.getM_sItemNumber().compareToIgnoreCase("") != 0){
			item = new ICItem(detail.getM_sItemNumber());
			if (!item.load(cn)){
				item = null;
				s += "<BR><FONT COLOR=RED><B>WARNING: Item " + detail.getM_sItemNumber() 
						+ " is not found in inventory - you cannot save this detail.</B></FONT><BR>";
			}else{
				String sStockItem = "0";
				if (item.getNonStockItem().compareToIgnoreCase("0") == 0){
					sStockItem = "1";
				}
				s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamiIsStockItem 
				+ "\" VALUE=\"" + sStockItem + "\""
				+ " id=\"" + SMOrderDetail.ParamiIsStockItem + "\""
				+ "\">";

				s += "<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamSuppressItemQtyLookup 
				+ "\" VALUE=\"" + item.getSuppressItemQtyLookup() + "\""
				+ " id=\"" + ICItem.ParamSuppressItemQtyLookup + "\""
				+ "\">";

			}
		}

		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ "\">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//Store the calling class:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
			+ sm.getCallingClass() + "\">";
		
		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdUniqueOrderID + "\" VALUE=\"" 
			+ detail.getM_dUniqueOrderID() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.Paramstrimmedordernumber + "\" VALUE=\"" 
			+ detail.getM_strimmedordernumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamiDetailNumber + "\" VALUE=\"" 
			+ detail.getM_iDetailNumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamiLineNumber + "\" VALUE=\"" 
			+ detail.getM_iLineNumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdExtendedOrderCost + "\""
			+ " VALUE=\"" + detail.getM_dExtendedOrderCost() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdOrderUnitCost + "\""
			+ " VALUE=\"" + detail.getM_dOrderUnitCost() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdOriginalQty + "\""
			+ " VALUE=\"" + detail.getM_dOriginalQty() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdQtyShippedToDate + "\""
			+ " VALUE=\"" + detail.getM_dQtyShippedToDate() + "\"" + ">";
		
		//Save the item number from the last time we refreshed the screen.  That way, we can tell if the
		//user has changed the item number by comparing it to this value:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LASTLOADEDITEMNUMBER + "\""
			+ " id=\"" + LASTLOADEDITEMNUMBER + "\""
			+ " VALUE=\"" + detail.getM_sItemNumber() + "\"" + ">";
		
		//We save these in case the mechanic on the order detail record is no longer in the mechanic's table- 
		// this way, we at least keep the original name of the mechanic in the record in case we have to go 
		// back and figure out who it was:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamsMechFullName + "\""
			+ " VALUE=\"" + detail.getM_sMechFullName() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.Paramimechid + "\""
				+ " VALUE=\"" + detail.getM_sMechID() + "\"" + ">";

		//If the user chose to INSERT a new line above an existing line, this variable will tell us
		//which line we are supposed to put the new line ABOVE:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + INSERTNEWLINEABOVEDETAILNUMBER + "\""
			+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(INSERTNEWLINEABOVEDETAILNUMBER, sm.getRequest()) + "\"" + ">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, sm.getRequest()) + "\">";
		
		//We have to keep track of these dates because when the date picker is invoked, it can change the 
		//value of these fields, but it won't trigger an 'onchange' event and we won't know the user has
		//changed the form:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDLINEBOOKEDDATE_PARAM + "\""
			+ " id=\"" + SAVEDLINEBOOKEDDATE_PARAM + "\""
			+ " VALUE=\"" + detail.getM_datLineBookedDate() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\""
			+ " id=\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\""
			+ " VALUE=\"" + detail.getM_datDetailExpectedShipDate() + "\"" + ">";

		//New Row
		s += "<TR>";
		
		String sLineNumber = "NEW";
		if (!sm.getAddingNewEntryFlag()){
			sLineNumber = detail.getM_iLineNumber();
		}
		ARCustomer cus = new ARCustomer(order.getM_sCustomerCode());
		if (order.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			if (!cus.load(cn)){
				s += " Error loading customer " + order.getM_sCustomerCode() + " - " + cus.getErrorMessageString();
				return s;
			}
		}
		String sOnHold = "";
		if (cus.getM_iOnHold().compareToIgnoreCase("1") == 0){
			sOnHold = "&nbsp;<FONT COLOR=RED><B>ON HOLD</B></FONT>";
		}
		s += "<B>" + sHeaderObjectName + " number:</B>&nbsp;" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + detail.getM_strimmedordernumber() 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + detail.getM_strimmedordernumber() + "</A>"
				+ "&nbsp;<B>" + sHeaderObjectName + " date:</B>&nbsp" + order.getM_datOrderDate()
				+ "&nbsp;<B>Customer:</B>&nbsp;" 
				+ order.getM_sCustomerCode()
				+ sOnHold
				+ "&nbsp;<B>Line number:</B>&nbsp;" + sLineNumber
		; 
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial;\">\n";		
		
		//Create the order commands line at the top:
		s += createDetailCommandsTable(sm.getAddingNewEntryFlag());
		
		//Create the edit area table:
		s += "<TR><TD><TABLE style=\" title:DetailArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + ITEMEDIT_BG_COLOR + "; \">" 
			+ createItemEditTable(sm, order, detail, item, cn) + "</TD>\n";
		//Create the order commands line at the bottom:
		s += createDetailCommandsTable(sm.getAddingNewEntryFlag());
		
		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		return s;
	}
	
	private String createItemEditTable(
			SMMasterEditEntry sm,
			SMOrderHeader header,
			SMOrderDetail detail,
			ICItem item,
			Connection conn) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		int iRows = 3;
		int iCols = 55;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:Itemedit; \">\n";
		s += "<TR>";
		//Qty ordered
		
		String sOnChangeFunction = "flagDirty();calculateextendedprice();";
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sOnChangeFunction += "updateShipQtyOnQuote();"; 
		}
		s += "<TD class=\" fieldlabel \"><B>Quantity:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdQtyOrdered + "\""
			+ " id = \"" + SMOrderDetail.ParamdQtyOrdered + "\""
			+ " VALUE=\"" + detail.getM_dQtyOrdered().replace("\"", "&quot;") + "\""
			+ " onchange=\"" + sOnChangeFunction + "\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH=" + "13"
			+ ">"
			+ "</TD>"
		;
		
		//Item number
		s += "<TD class=\" fieldlabel \"><B>Item:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamsItemNumber + "\""
			+ " id = \"" + SMOrderDetail.ParamsItemNumber + "\""
			+ " VALUE=\"" + detail.getM_sItemNumber().replace("\"", "&quot;") + "\""
			+ " onchange=\"updateItemInfo();\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + SMTableorderdetails.sItemNumberLength
			+ ">"
			
			//Finder button:
			+ "<button type=\"button\""
			+ " value=\"" + ITEM_FINDER_LABEL + "\""
			+ " name=\"" + ITEM_FINDER_LABEL + "\""
			+ " onClick=\"findItem();\">"
			+ ITEM_FINDER_LABEL
			+ "</button>\n"
			
			//Non-dedicated item finder button:
			+ "<button type=\"button\""
			+ " value=\"" + NONDEDICATEDITEM_FINDER_LABEL + "\""
			+ " name=\"" + NONDEDICATEDITEM_FINDER_LABEL + "\""
			+ " onClick=\"findNonDedicatedItem();\">"
			+ NONDEDICATEDITEM_FINDER_LABEL
			+ "</button>\n"
			
			+ "</TD>"
		;	
		
		s += "</TR>";
		
		s += "<TR>";
		//item description
		s += "<TD class=\" fieldlabel \"><B>Description:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamsItemDesc + "\""
			+ " id = \"" + SMOrderDetail.ParamsItemDesc + "\""
			+ " VALUE=\"" + detail.getM_sItemDesc().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "45"
			+ " MAXLENGTH=" + SMTableorderdetails.sItemDescLength
			+ ">"
			+ "</TD>"
		;
		//UOM
		s += "<TD class=\" fieldlabel \"><B>Unit of measure:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamsOrderUnitOfMeasure + "\""
			+ " id = \"" + SMOrderDetail.ParamsOrderUnitOfMeasure + "\""
			+ " VALUE=\"" + detail.getM_sOrderUnitOfMeasure().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH=" + SMTableorderdetails.sOrderUnitOfMeasureLength
			+ ">"
			+ "</TD>"
		;

		s += "</TR>";
		
		//If it's a quote, add the unit and extended cost:
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			s += "<TR>";
			//estimated unit cost
			s += "<TD class=\" fieldlabel \"><B>Estimated unit cost:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.Parambdestimatedunitcost + "\""
				+ " id = \"" + SMOrderDetail.Parambdestimatedunitcost + "\""
				+ " VALUE=\"" + detail.getM_bdEstimatedUnitCost().replace("\"", "&quot;") + "\""
				+ " onchange=\"calculateextendedcost();\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + "13"
				+ ">"
				+ "&nbsp;<B>Cost multiplier:</B>"
				+ "<INPUT TYPE=TEXT NAME=\"" + COSTMULTIPLIER_PARAM + "\""
				+ " id = \"" + COSTMULTIPLIER_PARAM + "\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + "13"
				+ ">"

				//Cost multiplier button
				+ "<button type=\"button\""
				+ " value=\"" + COSTMULTIPLIER_BUTTON + "\""
				+ " name=\"" + COSTMULTIPLIER_BUTTON + "\""
				+ " onClick=\"multiplyToCalculatePrice();\">"
				+ COSTMULTIPLIER_LABEL
				+ "</button>\n"
				+ "</TD>"
			;
			BigDecimal bdQtyOrdered;
			try {
				bdQtyOrdered = new BigDecimal(detail.getM_dQtyOrdered().replace(",", ""));
			} catch (Exception e) {
				throw new SQLException("Error [1387834624] reading detail.getM_dQtyOrdered(): '" 
				+ detail.getM_dQtyOrdered() + "' - " + e.getMessage());
			}
			BigDecimal bdEstimatedUnitCost;
			try {
				bdEstimatedUnitCost = new BigDecimal(detail.getM_bdEstimatedUnitCost().replace(",", ""));
			} catch (Exception e) {
				throw new SQLException("Error [1387834625] reading detail.getM_bdEstimatedUnitCost(): '" 
				+ detail.getM_bdEstimatedUnitCost() + "' - " + e.getMessage());
			}
			BigDecimal bdExtendedEstimatedCost = bdQtyOrdered.multiply(bdEstimatedUnitCost);
			s += "<TD class=\" fieldlabel \"><B>Extended estimated cost:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+"<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + EXTESTCOST_PARAM + "\""
				+ " ID = \"" + EXTESTCOST_PARAM + "\""	
				+ " VALUE=\"" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedEstimatedCost) + "\""
				+ " SIZE=" + "8"
				+ "></TD>"
			;
			
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.Parambdestimatedunitcost + "\""
			+ " id = \"" + SMOrderDetail.Parambdestimatedunitcost + "\""
			+ " VALUE=\"" + detail.getM_bdEstimatedUnitCost().replace("\"", "&quot;") + "\""
			+ " onchange=\"calculateextendedprice();\""
			+ ">"
			;
		}
		s += "</TR>";
		
		s += "<TR>";
		
		//qty shipped
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
			s += "<TD class=\" fieldlabel \"><B>Qty shipped:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdQtyShipped + "\""
				+ " id = \"" + SMOrderDetail.ParamdQtyShipped + "\""
				+ " VALUE=\"" + detail.getM_dQtyShipped().replace("\"", "&quot;") + "\""
				+ " onchange=\"calculateextendedprice();\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + "13"
				+ ">"
				+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + ITEMQTYINFO_PARAM + "\""
				+ " ID = \"" + ITEMQTYINFO_PARAM + "\""	
				+ " SIZE=" + "28"
				+ ">"
				+ "</TD>"
			;
		}else{
			s += "<TD class=\" fieldlabel \"><B>Qty on hand:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdQtyShipped + "\""
				+ " id = \"" + SMOrderDetail.ParamdQtyShipped + "\""
				+ " VALUE=\"" + detail.getM_dQtyShipped().replace("\"", "&quot;") + "\""
				+ " onchange=\"calculateextendedprice();\""
				+ ">"
				+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + ITEMQTYINFO_PARAM + "\""
				+ " ID = \"" + ITEMQTYINFO_PARAM + "\""	
				+ " SIZE=" + "28"
				+ ">"
				+ "</TD>"
			;
		}
		//unit price
		s += "<TD class=\" fieldlabel \"><B>Unit price:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdOrderUnitPrice + "\""
			+ " id = \"" + SMOrderDetail.ParamdOrderUnitPrice + "\""
			+ " VALUE=\"" + detail.getM_dOrderUnitPrice().replace("\"", "&quot;") + "\""
			+ " onchange=\"calculateextendedprice();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH=" + "13"
			+ ">"
			+ "</TD>"
		;
		
		s += "</TR>";
		
		s += "<TR>";
		
		//extended price
		s += "<TD class=\" fieldlabel \"><B>Extended price:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdExtendedOrderPrice + "\""
			+ " id = \"" + SMOrderDetail.ParamdExtendedOrderPrice + "\""
			+ " VALUE=\"" + detail.getM_dExtendedOrderPrice().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "8"
			+ " MAXLENGTH=" + "13"
			+ ">"
			+ "</TD>"
		;
		//location
		s += "<TD class=\" fieldlabel \">Location:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT ID= \"" + SMOrderDetail.ParamsLocationCode + "\""
			+ " onchange=\"locationChange();\""
			+ " NAME=\"" + SMOrderDetail.ParamsLocationCode + "\"" 
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\"> ** SELECT A LOCATION **</OPTION>";
		String SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY " + SMTablelocations.sLocation
		;
		try {
			ResultSet rsItemLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsItemLocation.next()){
				String sItemLocation = rsItemLocation.getString(SMTablelocations.sLocation);
				s += "<OPTION";
				if (sItemLocation.compareToIgnoreCase(detail.getM_sLocationCode()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sItemLocation + "\">" 
				+ sItemLocation + " - " 
				+ rsItemLocation.getString(SMTablelocations.sLocationDescription)
				+ "</OPTION>";
			}
			rsItemLocation.close();
		} catch (Exception e) {
			throw new SQLException("Error loading item locations with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT></TD>";
		
		s += "</TR>";
		s += "<TR>";
		//category
		s += "<TD class=\" fieldlabel \">Category:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderDetail.ParamsItemCategory + "\"" 
			+ " id = \"" + SMOrderDetail.ParamsItemCategory + "\""
			+ " onchange=\"flagDirty();\""
			+ ">";
		//Add a blank item in the list:
		s += "<OPTION";
		if (detail.getM_sItemCategory().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + "" + "\">" 
		+ BLANK_ITEM_CATEGORY_DESC
		+ "</OPTION>";
		
		SQL = "SELECT"
			+ " " + SMTableiccategories.sCategoryCode
			+ ", " + SMTableiccategories.sDescription
			+ " FROM " + SMTableiccategories.TableName
			+ " ORDER BY " + SMTableiccategories.sCategoryCode
		;
		try {
			ResultSet rsCategories = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsCategories.next()){
				String sItemCategory = rsCategories.getString(SMTableiccategories.sCategoryCode);
				s += "<OPTION";
				if (sItemCategory.compareToIgnoreCase(detail.getM_sItemCategory()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sItemCategory + "\">" 
				+ sItemCategory + " - " + rsCategories.getString(SMTableiccategories.sDescription)
				+ "</OPTION>";
			}
			rsCategories.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading item categories with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT></TD>";
		
		//mechanic
		s += "<TD class=\" fieldlabel \">Mechanic:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderDetail.ParamsMechInitial + "\"" 
			+ " id = \"" + SMOrderDetail.ParamsMechInitial + "\""
			+ " onchange=\"flagDirty();\""
			+ ">";

		SQL = "SELECT"
			+ " " + SMTablemechanics.sMechInitial
			+ ", " + SMTablemechanics.sMechFullName
			+ " FROM " + SMTablemechanics.TableName
			+ " ORDER BY " + SMTablemechanics.sMechInitial
		;
		s += "<OPTION VALUE=\"" + "" + "\">" 
		+ "** SELECT MECHANIC **"
		+ "</OPTION>";
		try {
			ResultSet rsMechanics = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsMechanics.next()){
				String sMechanicInitials = rsMechanics.getString(SMTablemechanics.sMechInitial);
				s += "<OPTION";
				if (sMechanicInitials.compareToIgnoreCase(detail.getM_sMechInitial()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sMechanicInitials + "\">" 
				+ sMechanicInitials + " - " + rsMechanics.getString(SMTablemechanics.sMechFullName)
				+ "</OPTION>";
			}
			rsMechanics.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading mechanics with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT></TD>";
		
		s += "</TR>";
		s += "<TR>";
		//taxable
		s += "<TD class=\" fieldlabel \"><B>Taxable?</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=CHECKBOX ";
		if (detail.getM_iTaxable().compareToIgnoreCase("1") == 0){
			s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += " NAME=\"" + SMOrderDetail.ParamiTaxable + "\""
			+ " id = \"" + SMOrderDetail.ParamiTaxable + "\""
			+ " onchange=\"flagDirty();\""
			+ " width=0.25>"
			+ "</TD>"
		;
		
		//Print on delivery ticket?
		s += "<TD class=\" fieldlabel \"><B>Print on delivery ticket?</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=CHECKBOX ";
		if (detail.getM_iprintondeliveryticket().compareToIgnoreCase("1") == 0){
			s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += " NAME=\"" + SMOrderDetail.Paramiprintondeliveryticket + "\""
			+ " id = \"" + SMOrderDetail.Paramiprintondeliveryticket + "\""
			+ " onchange=\"flagDirty();\""
			+ " width=0.25>"
			+ "</TD>"
		;
		
		s += "</TR>";
		s += "<TR>";
		
		//door label
		s += "<TD class=\" fieldlabel \">Site label:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderDetail.ParamsLabel + "\"" 
			+ " id = \"" + SMOrderDetail.ParamsLabel + "\""
			+ " onchange=\"flagDirty();\""
			+ ">";

		SQL = "SELECT"
			+ " " + SMTablesitelocations.sLabel
			+ " FROM " + SMTablesitelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablesitelocations.sAcct + " = '" + header.getM_sCustomerCode() + "')"
				+ " AND (" + SMTablesitelocations.sShipToCode + " = '" + header.getM_sShipToCode() + "')"
			+ ")"
			+ " ORDER BY " + SMTablesitelocations.sShipToCode
		;
		try {
			ResultSet rsSiteLocations = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iCounter = 0;
			while (rsSiteLocations.next()){
				if (iCounter == 0){
					s += "<OPTION VALUE=\"" + "" + "\">" 
					+ "** SELECT A LABEL **"
					+ "</OPTION>";
				}
				String sLabel = rsSiteLocations.getString(SMTablesitelocations.sLabel);
				s += "<OPTION";
				if (sLabel.compareToIgnoreCase(detail.getM_sLabel()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sLabel + "\">" 
				+ rsSiteLocations.getString(SMTablesitelocations.sLabel)
				+ "</OPTION>";
				iCounter++;
			}
			if (iCounter == 0){
				s += "<OPTION VALUE=\"" + "" + "\">" 
				+ "** NONE AVAILABLE **"
				+ "</OPTION>";
			}
			rsSiteLocations.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading site locations with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT></TD>";
		//line booked date
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
			s += "<TD class=\" fieldlabel \">Line booked date:&nbsp;</TD>";
			s += "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdatLineBookedDate + "\""
				+ " VALUE=\"" + detail.getM_datLineBookedDate().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMOrderDetail.ParamdatLineBookedDate + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "28"
				+ " MAXLENGTH=" + "10"
				+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(SMOrderDetail.ParamdatLineBookedDate, getServletContext())
				
				//TODO LTO 20140418
				//use order date as LBD button:
				+ "<button type=\"button\""
				+ " value=\"" + USE_ORDER_DATE_AS_LBD_LABEL + "\""
				+ " name=\"" + USE_ORDER_DATE_AS_LBD_LABEL + "\""
				+ " id=\"" + USE_ORDER_DATE_AS_LBD_LABEL + "\""
				+ " onClick=\"useOrderDateAsLBD();\">"
				+ USE_ORDER_DATE_AS_LBD_LABEL
				+ "</button>\n"
				
				+ "</TD>";
		}else{
			s += "<TD>&nbsp;</TD><TD>&nbsp;</TD>"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderDetail.ParamdatLineBookedDate + "\""
				+ " VALUE=\"" + detail.getM_datLineBookedDate() + "\"" + ">";
				;
		}
		s += "</TR>";
		s += "<TR>";
		//expected ship date
		s += "<TD class=\" fieldlabel \">Expected ship date:&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderDetail.ParamdatDetailExpectedShipDate + "\""
			+ " VALUE=\"" + detail.getM_datDetailExpectedShipDate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderDetail.ParamdatDetailExpectedShipDate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "28"
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(SMOrderDetail.ParamdatDetailExpectedShipDate, getServletContext())
			+ "</TD>";
		
		//Hide detail on invoice
		s += "<TD class=\" fieldlabel \"><B>Hide on invoice?</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=CHECKBOX ";
		if (detail.getM_isuppressdetailoninvoice().compareToIgnoreCase("1") == 0){
			s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += " NAME=\"" + SMOrderDetail.Paramisuppressdetailoninvoice + "\""
			+ " id = \"" + SMOrderDetail.Paramisuppressdetailoninvoice + "\""
			+ " onchange=\"flagDirty();\""
			+ " width=0.25>"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";

		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
			//shipped to date (RO)
			s += "<TD class=\" fieldlabel \"><B>Shipped to date:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+ detail.getM_dQtyShippedToDate().replace("\"", "&quot;")
				+ "</TD>"
			;
			s += "<TD class=\" fieldheading \" COLSPAN=2 ><B>Internal detail comment</B>&nbsp;</TD>";	
			/*//qty backordered (RO)
			s += "<TD class=\" fieldlabel \"><B>Qty backordered:</B></TD>"
				+ "<TD class=\" fieldcontrol \" >"
				+ detail.getM_dQtyBackOrdered().replace("\"", "&quot;")
				+ "</TD>"
			;*/
		}else{
			s += "<TD>&nbsp;</TD><TD>&nbsp;</TD>";
		}
		s += "</TR>";
		
		s += "<TR>";

		//Non-Stock item? (RO)
		String sYesOrNo = "NO";
		if (item == null){
			sYesOrNo = "N/A";
		}else{
			if (item.getNonStockItem().compareTo("1") == 0){
				sYesOrNo = "YES";
			}
		}
		s += "<TD class=\" fieldlabel \"><B>Non-stock item?:</B></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ sYesOrNo
			+ "</TD>"
		;
		
		//Internal Comments:
		s += "<TD class=\" fieldcontrol \" COLSPAN=2 >"
				+ "<TEXTAREA NAME=\"" + SMOrderDetail.ParammInternalComments + "\""
				+ " rows=\"" + Integer.toString(iRows) + "\""
				+ " cols=\"" + Integer.toString(iCols) + "\""
				+ " id = \"" + SMOrderDetail.ParammInternalComments + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ detail.getM_mInternalComments().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		
		//invoice comment
		s += "<TR>";
		s += "<TD class=\" fieldheading \" COLSPAN=2 ><B>Invoice detail comment</B>&nbsp;</TD>";
		s += "<TD class=\" fieldheading \" COLSPAN=2><B>Work order detail comment</B>&nbsp;</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" COLSPAN=2 >"
			+ "<TEXTAREA NAME=\"" + SMOrderDetail.ParammInvoiceComments + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " id = \"" + SMOrderDetail.ParammInvoiceComments + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ detail.getM_mInvoiceComments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		//ticket comment
		s += "<TD class=\" fieldcontrol \" COLSPAN=2>"
			+ "<TEXTAREA NAME=\"" + SMOrderDetail.ParammTicketComments + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " id = \"" + SMOrderDetail.ParammTicketComments + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ detail.getM_mTicketComments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		
		s += "</TR>";
		
		//Work performed codes:
		s += "<TR><TD COLSPAN = " + Integer.toString(iNumberOfColumns) + ">";
		
		s +=
			"\n<form name=wpfform>\n"
			+ "<input type=\"checkbox\" id=\"wpfChoices\" onclick=\"exposeWorkPerformedCodesList()\">"
			+ "<B>Show work performed codes<B>&nbsp;\n"
			+ "</form>\n"
		;
			
		s += "<div id=\"ScrollWPF\" style=\"height:350;width:100%;background-color:" 
				+ WORKPERFORMEDCODES_BG_COLOR + ";overflow:auto;border:1px solid blue;display:none\">\n"
		;
		
		SQL = "SELECT * FROM " + SMTableworkperformedcodes.TableName
			+ " WHERE ("
				+ "(" + SMTableworkperformedcodes.sCode + " = '" + header.getM_sServiceTypeCode() + "')"
			+ ")"
			+ " ORDER BY " + SMTableworkperformedcodes.iSortOrder
		;
		try {
			ResultSet rswpf = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rswpf.next()){
				//This creates a list of codes:
				String sWPCDesc = clsStringFunctions.filter(rswpf.getString(SMTableworkperformedcodes.sWorkPerformedPhrase));
				s += "<input type=\"hidden\" id=\"" + WPFCONTROL_MARKER 
				+ rswpf.getString(SMTableworkperformedcodes.sWorkPerformedCode) 
				+ "\" name=\"" + WPFCONTROL_MARKER 
				+ rswpf.getString(SMTableworkperformedcodes.sWorkPerformedCode) + "\""
				
				+ " value=\"" + sWPCDesc 
				+ "\"><label for=\"" + WPFCONTROL_MARKER 
				+ rswpf.getString(SMTableworkperformedcodes.sWorkPerformedCode) 
				+ "\" name=\"" + WPFCONTROL_MARKER 
				+ rswpf.getString(SMTableworkperformedcodes.sWorkPerformedCode) + "\""
				+ " onclick=\"insertWorkPerformedCode('" + sWPCDesc + "');\""
				+ " onmouseover=colorChangeRed(this) onmouseout=colorChangeBack(this)>"
				+ clsStringFunctions.filter(rswpf.getString(SMTableworkperformedcodes.sWorkPerformedCode))
				+ " - "
				+ clsStringFunctions.filter(rswpf.getString(SMTableworkperformedcodes.sWorkPerformedPhrase)) 
				+ "</label>"
				+ "<br>\n"
				;
			}
			rswpf.close();
		} catch (Exception e) {
			throw new SQLException("Error reading work performed codes with SQL: " + SQL + " - " + e.getMessage());
		}
		s += "</div>";
		s += "</TD></TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:ENDItemEdit; \">\n";
		return s;
	}
	private String createDetailCommandsTable(boolean bAddingNewEntry){
		String s = "";
		
		s += "<TR><TD>";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ ORDERCOMMANDS_BG_COLOR + "; \" width=100% >\n";
				//Place the 'update' button here:
				s += "<TR><TD style = \"text-align: left; \" >"
					//+ " <input type=button"
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
					
					+ "<button type=\"button\""
					+ " value=\"" + TOTALS_BUTTON_LABEL + "\""
					+ " name=\"" + TOTALS_BUTTON_LABEL + "\""
					+ " onClick=\"gotoTotals();\">"
					+ TOTALS_BUTTON_LABEL
					+ "</button>\n"
					
					+ "<button type=\"button\""
					+ " value=\"" + SAVE_BUTTON_LABEL + "\""
					+ " name=\"" + SAVE_BUTTON_LABEL + "\""
					+ " onClick=\"saveDetail();\">"
					+ SAVE_BUTTON_LABEL
					+ "</button>\n"
					
					+ "<button type=\"button\""
					+ " value=\"" + SAVE_AND_ADD_BUTTON_LABEL + "\""
					+ " name=\"" + SAVE_AND_ADD_BUTTON_LABEL + "\""
					+ " onClick=\"saveAndAdd();\">"
					+ SAVE_AND_ADD_BUTTON_LABEL
					+ "</button>\n"
					
					+ "<button type=\"button\""
					+ " value=\"" + SAVE_AND_GO_TO_NEXT_BUTTON_LABEL + "\""
					+ " name=\"" + SAVE_AND_GO_TO_NEXT_BUTTON_LABEL + "\""
					+ " onClick=\"saveAndGoToNext();\">"
					+ SAVE_AND_GO_TO_NEXT_BUTTON_LABEL
					+ "</button>\n"
					
					+ "<button type=\"button\""
					+ " value=\"" + SAVE_AND_INSERT_NEXT_LINE_BUTTON_LABEL + "\""
					+ " name=\"" + SAVE_AND_INSERT_NEXT_LINE_BUTTON_LABEL + "\""
					+ " onClick=\"saveAndInsertNew();\">"
					+ SAVE_AND_INSERT_NEXT_LINE_BUTTON_LABEL
					+ "</button>\n"
					
				;
					if (!bAddingNewEntry){
						//Anything here?
					}
					s += "</TD></TR>"
				;
		
		//Close the table:
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		s += "</TD></TR>";
		return s;
	}

	private String sCommandScripts(
			SMOrderHeader header,
			SMOrderDetail detail, 
			Connection conn, 
			boolean bReturningFromItemUpdate) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		s += "<script type=\"text/javascript\">\n";
		
		//Here we have to build a javascript array of the qtys on hand:
		int iCounter = 0;
		String sqtysonhand = "";
		String scostonhand = "";
		
		//If we have a valid item number, get the qtys on hand:
		String SQL = "SELECT"
			+ " " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
			+ ", ICILOC." + SMTableicitemlocations.sItemNumber
			+ ", ICILOC." + SMTableicitemlocations.sQtyOnHand
			+ ", ICILOC." + SMTableicitemlocations.sTotalCost
			+ " FROM " + SMTablelocations.TableName + " LEFT JOIN " 
			+ "(SELECT * FROM " + SMTableicitemlocations.TableName
			+ " WHERE ("
				+ "(" + SMTableicitemlocations.sItemNumber + " = '" + detail.getM_sItemNumber() + "')"
			+ ")"
			+ ") AS ICILOC"
			+ " ON " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
			+ "= ICILOC." + SMTableicitemlocations.sLocation
			;
		ResultSet rsItemLoc = clsDatabaseFunctions.openResultSet(SQL, conn);
		while (rsItemLoc.next()){
			iCounter++;
			String sLocation = rsItemLoc.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
			String sQtyOH = "0.0000";
			if (rsItemLoc.getBigDecimal("ICILOC." + SMTableicitemlocations.sQtyOnHand) != null){
				sQtyOH = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderdetails.dQtyShippedScale,
					rsItemLoc.getBigDecimal("ICILOC." + SMTableicitemlocations.sQtyOnHand));
			}
			String sCostOH = "0.0000";
			if (rsItemLoc.getBigDecimal("ICILOC." + SMTableicitemlocations.sQtyOnHand) != null){
				sCostOH = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderdetails.dOrderUnitCostScale,
					rsItemLoc.getBigDecimal("ICILOC." + SMTableicitemlocations.sTotalCost));
			}
			
			sqtysonhand += "sqtysonhand[\"" + sLocation	+ "\"] = \"" + sQtyOH.replace(",", "") + "\";\n";
			scostonhand += "scostonhand[\"" + sLocation + "\"] = \"" + sCostOH.replace(",", "") + "\";\n";
		}
		if (iCounter > 0){
			s += "var sqtysonhand = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sqtysonhand + "\n";
			
			s += "var scostonhand = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scostonhand + "\n";
		}
		s += "\n";
		
		s += "function locationChange() {\n" 
			+ "    updateQtyOHInfo();\n"
			+ "    flagDirty();\n"
			+ "}\n\n";
		
		s += "function updateQtyOHInfo() {\n"
			// get the index of the selected option 
			+ "    var idx = document.forms[\"MAINFORM\"].elements[\"" + SMOrderDetail.ParamsLocationCode 
				+ "\"].selectedIndex;\n"
			// get the value of the selected option 
			+ "    var which = document.forms[\"MAINFORM\"].elements[\"" + SMOrderDetail.ParamsLocationCode 
				+ "\"].options[idx].value;\n"
			// use the selected option value to retrieve:
			+ "    if (which != ''){\n"
			+ "        document.forms[\"MAINFORM\"].elements[\"" + ITEMQTYINFO_PARAM + "\"].value = "
				+ "'Loc ' + which + ' Qty OH: ' + sqtysonhand[which];\n"
			+ "    }else{\n"
			+ "        document.forms[\"MAINFORM\"].elements[\"" + ITEMQTYINFO_PARAM + "\"].value = '';\n"
			+ "    }"
			+ "}\n\n";
		
		s += "function exposeWorkPerformedCodesList() {\n" 
			+ "    var status = document.getElementById('wpfChoices').checked;\n" 
			+ "    if (status == true) {\n"
			+ "        document.getElementById('ScrollWPF').style.display = \"block\";\n"
			+ "    } else {\n"
			+ "        document.getElementById('ScrollWPF').style.display = 'none';\n"
			+ "    }\n" 
			+ "}\n"
		;
		
		//s += "function insertWorkPerformedCode(clickedCheckBoxObj) {\n" 
		s += "function insertWorkPerformedCode(sWPCDesc) {\n"
			+ "    textarea = document.getElementById('" + SMOrderDetail.ParammInvoiceComments + "');\n"
			+ "    var caretPos = textarea.selectionStart;\n"
			+ "    var text = textarea.value;\n"
			+ "    var first = text.substring(0, caretPos);\n"
			+ "    var second = text.substring(caretPos, text.length);\n"
			//+ "    var insertionString = clickedCheckBoxObj.value + '\\n';\n"
			+ "    var insertionString = sWPCDesc + '\\n';\n"
			+ "    var newString = first + insertionString + second;\n"
			+ "    textarea.value = newString;\n"
			+ "    var newPos = first.length + insertionString.length;\n"
			+ "    textarea.setSelectionRange(newPos, newPos);\n"
			+ "    textarea.scrollTop = textarea.scrollHeight;"
			
			//+ "    document.getElementById('" + SMOrderDetail.ParammInvoiceComments
			//	+ "').value = "
			//	+ "document.getElementById('" + SMOrderDetail.ParammInvoiceComments 
			//			+ "').value + "
			//	+ " clickedCheckBoxObj.value + '\\n';\n"
			//+ "    clickedCheckBoxObj.checked = false;\n"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function validateForm(){\n"

			//check the required text fields:
			+ "    var sitemnumber = document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").value;\n"
			+ "    if (sitemnumber==null || sitemnumber==\"\"){\n"
			+ "        alert(\"Item number cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"

			+ "    var sitemdescription = document.getElementById(\"" + SMOrderDetail.ParamsItemDesc + "\").value;\n"
			+ "    if (sitemdescription==null || sitemdescription==\"\"){\n"
			+ "        alert(\"Description cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamsItemDesc + "\").focus();\n"
			
			+ "        return false;\n"
			+ "    }\n"

			+ "    var suom = document.getElementById(\"" + SMOrderDetail.ParamsOrderUnitOfMeasure + "\").value;\n"
			+ "    if (suom==null || suom==\"\"){\n"
			+ "        alert(\"Description cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamsOrderUnitOfMeasure + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"

			// check the required option lists:
			+ "    var slocation = document.getElementById(\"" 
						+ SMOrderDetail.ParamsLocationCode + "\").value;\n"
			+ "    if (slocation==null || slocation==\"\"){\n"
			+ "        alert(\"You must select a location.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamsLocationCode + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			;
			//check dates:
			if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
				s += "    var linebookeddate = document.getElementById(\"" + SMOrderDetail.ParamdatLineBookedDate + "\").value;\n"
				+ "    if (isDate(linebookeddate) == false){\n"
				+ "        alert(\"Line booked date '\" + linebookeddate + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdatLineBookedDate + "\").focus();\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdatLineBookedDate + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n";
			}
			s += "    var sexpectedshipdate = document.getElementById(\"" + SMOrderDetail.ParamdatDetailExpectedShipDate + "\").value;\n"
			+ "    if ((sexpectedshipdate != '00/00/0000') && (isDate(sexpectedshipdate) == false)){\n"
			+ "        alert(\"Expected ship date '\" + sexpectedshipdate + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"

			//Validate number fields:
			+ "    var sqtyordered = document.getElementById(\"" 
				+ SMOrderDetail.ParamdQtyOrdered + "\").value;\n"
			+ "    if (isNumeric(sqtyordered) == false){\n"
			+ "        alert(\"Qty ordered '\" + sqtyordered + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			;
			if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
				s += "    var sqtyshipped = document.getElementById(\"" 
					+ SMOrderDetail.ParamdQtyShipped + "\").value;\n"
				+ "    if (isNumeric(sqtyshipped) == false){\n"
				+ "        alert(\"Qty shipped '\" + sqtyordered + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").focus();\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
				;
			}
			s += "    var sunitprice = document.getElementById(\"" 
				+ SMOrderDetail.ParamdOrderUnitPrice + "\").value;\n"
			+ "    if (isNumeric(sunitprice) == false){\n"
			+ "        alert(\"Unit price '\" + sunitprice + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdOrderUnitPrice + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdOrderUnitPrice + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			+ "    var sextprice = document.getElementById(\"" 
				+ SMOrderDetail.ParamdExtendedOrderPrice + "\").value;\n"
			+ "    if (isNumeric(sextprice) == false){\n"
			+ "        alert(\"Extended price '\" + sextprice + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdExtendedOrderPrice + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdExtendedOrderPrice + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n";
			
			//Here we check if the qty shipped is > the qty OH:
			if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
				s += "    if ((document.getElementById(\"" + SMOrderDetail.ParamiIsStockItem 
						+ "\").value == '1') && (document.getElementById(\"" + ICItem.ParamSuppressItemQtyLookup + "\").value != '1')){\n"
				+ "        qtyshipped = getFloat(sqtyshipped);\n"
				+ "        var idx = document.forms[\"MAINFORM\"].elements[\"" + SMOrderDetail.ParamsLocationCode 
					+ "\"].selectedIndex;\n"
				+ "        var which = document.forms[\"MAINFORM\"].elements[\"" + SMOrderDetail.ParamsLocationCode 
					+ "\"].options[idx].value;\n"
				+ "        if (which != ''){\n"
				+ "            var qtyonhand = getFloat(sqtysonhand[which]);\n"
				+ "        }else{\n"
				+ "            var qtyonhand = getFloat('0.00');\n"
				+ "        }\n"
				+ "        if (qtyshipped > qtyonhand){\n"
				+ "            if (!confirm('Qty shipped is greater than the Qty on hand - do you still want to proceed?')){\n"
				+ "                return false;\n"
				+ "            }\n"
				+ "        }\n"
				+ "    }\n";
			}
			s += "    return true;\n"
			+ "}\n\n"
			;
			
		s += "function isDate(datestring) {\n"
			+ "    try {\n"
			//Change the below values to determine which format of date you wish to check. It is set to mm/dd/yyyy by default.
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
		
		s += "function flagPricesDirty() {\n"
			+ "    document.getElementById(\"" + PRICECHANGE_FLAG + "\").value = \"" 
				 + PRICECHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n"
		;
		
		s += "function updateShipQtyOnQuote() {\n"
			+ "    document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").value = " 
				 + "document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").value;\n"
			+ "    calculateextendedprice();\n"
			+ "    calculateextendedcost();\n"
			+ "    flagDirty();\n"
			+ "}\n\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		//Set initial focus:
		if (bReturningFromItemUpdate){
			s += "window.onload = function(){\n"
				+ "    initShortcuts();\n"
				+ "    updateQtyOHInfo();\n"
				+ "    document.forms.MAINFORM." + SMOrderDetail.ParamsItemDesc + ".focus();\n"
				+ "    document.forms.MAINFORM." + SMOrderDetail.ParamsItemDesc + ".select();\n"
				+ "}\n\n"
			;
		}else{
			s += "window.onload = function(){\n"
				+ "    initShortcuts();\n"
				+ "    updateQtyOHInfo();\n"
				+ "    document.forms.MAINFORM." + SMOrderDetail.ParamdQtyOrdered + ".focus();\n"
				+ "    document.forms.MAINFORM." + SMOrderDetail.ParamdQtyOrdered + ".select();\n"
				
				+ "}\n\n"
			;
		}
		
		s += "function promptToSave(){\n"
			//First check to see if the date fields were changed, and if so, flag the record was changed field:
			+ "    if (document.getElementById(\"" + SAVEDLINEBOOKEDDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMOrderDetail.ParamdatLineBookedDate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMOrderDetail.ParamdatDetailExpectedShipDate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (\n"
			+ "            (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVEANDADDCOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVEANDINSERTNEWCOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ FINDITEMCOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ FINDNONDEDICATEDITEMCOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ ITEMCHANGEDCOMMAND_VALUE + "\" )\n"
			+ "             && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVEANDGOTONEXTCOMMAND_VALUE + "\" )\n"
			+ "        ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this page?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		s += "function updateItemInfo(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ ITEMCHANGEDCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function findItem(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ FINDITEMCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function findNonDedicatedItem(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					+ FINDNONDEDICATEDITEMCOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			s += "function calculateextendedcost(){\n"
				+ "    flagDirty();\n"
				+ "    var sqtyordered = document.getElementById(\"" 
					+ SMOrderDetail.ParamdQtyOrdered + "\").value.replace(',','');\n"
				+ "    if (isNumeric(sqtyordered) == false){\n"
				+ "        alert(\"Qty ordered '\" + sqtyordered + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").focus();\n"
				+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
				+ "    qtyordered = getFloat(sqtyordered);\n"
				+ "    var sestimatedunitcost = document.getElementById(\"" 
							+ SMOrderDetail.Parambdestimatedunitcost + "\").value.replace(',','');\n"
				+ "    if (isNumeric(sestimatedunitcost) == false){\n"
				+ "        alert(\"Estimated unit cost '\" + sestimatedunitcost + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").focus();\n"
				+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
				+ "    estimatedunitcost = getFloat(sestimatedunitcost);\n"
				
				+ "    var extestcost = Math.round(qtyordered * estimatedunitcost * 100.00) / 100.00;\n"
				+ "    document.getElementById(\"" 
						+ EXTESTCOST_PARAM + "\").value = extestcost;\n"
				+ "}\n"
			;
			s += "function multiplyToCalculatePrice(){\n"
				+ "    flagDirty();\n"
				+ "    var smultiplier = document.getElementById(\"" 
					+ COSTMULTIPLIER_PARAM + "\").value.replace(',','');\n"
				+ "    if (isNumeric(smultiplier) == false){\n"
				+ "        alert(\"Cost multiplier '\" + smultiplier + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + COSTMULTIPLIER_PARAM + "\").focus();\n"
				+ "        document.getElementById(\"" + COSTMULTIPLIER_PARAM + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
				+ "    dmultiplier = getFloat(smultiplier);\n"
				+ "    var sestimatedunitcost = document.getElementById(\"" 
							+ SMOrderDetail.Parambdestimatedunitcost + "\").value.replace(',','');\n"
				+ "    if (isNumeric(sestimatedunitcost) == false){\n"
				+ "        alert(\"Estimated unit cost '\" + sestimatedunitcost + \"' is invalid.\");\n"
				+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").focus();\n"
				+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").select();\n"
				+ "        return false;\n"
				+ "    }\n"
				+ "    estimatedunitcost = getFloat(sestimatedunitcost);\n"
				
				+ "    var unitprice = Math.round(dmultiplier * estimatedunitcost * 100.00) / 100.00;\n"
				+ "    document.getElementById(\"" 
						+ SMOrderDetail.ParamdOrderUnitPrice + "\").value = unitprice;\n"
				+ "    calculateextendedprice();\n"
				+ "}\n"
			;
		}
		s += "function saveDetail(){\n"
			+ "    if (!validateForm()){\n"
			+ "        return;\n"
			+ "    }\n"
			//Here we confirm that the user hasn't just changed the item number and immediately clicked 'save'
			//because in that case, the item information may not be updated before they save:
			+ "    if (document.getElementById(\"" + LASTLOADEDITEMNUMBER 
				+ "\").value == document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").value){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SAVECOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        alert('Line was NOT saved - item information is being updated.');\n"
			+ "        updateItemInfo();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function saveAndAdd(){\n"
			+ "    if (!validateForm()){\n"
			+ "        return;\n"
			+ "    }\n"
			//Here we confirm that the user hasn't just changed the item number and immediately clicked 'save'
			//because in that case, the item information may not be updated before they save:
			+ "    if (document.getElementById(\"" + LASTLOADEDITEMNUMBER 
				+ "\").value == document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").value){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SAVEANDADDCOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        alert('Line was NOT saved - item information is being updated.');\n"
			+ "        updateItemInfo();\n"
			+ "    }\n"
			+ "}\n"
		;

		s += "function saveAndGoToNext(){\n"
			+ "    if (!validateForm()){\n"
			+ "        return;\n"
			+ "    }\n"
			//Here we confirm that the user hasn't just changed the item number and immediately clicked 'save'
			//because in that case, the item information may not be updated before they save:
			+ "    if (document.getElementById(\"" + LASTLOADEDITEMNUMBER 
				+ "\").value == document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").value){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SAVEANDGOTONEXTCOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        alert('Line was NOT saved - item information is being updated.');\n"
			+ "        updateItemInfo();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function saveAndInsertNew(){\n"
			+ "    if (!validateForm()){\n"
			+ "        return;\n"
			+ "    }\n"
			//Here we confirm that the user hasn't just changed the item number and immediately clicked 'save'
			//because in that case, the item information may not be updated before they save:
			+ "    if (document.getElementById(\"" + LASTLOADEDITEMNUMBER 
				+ "\").value == document.getElementById(\"" + SMOrderDetail.ParamsItemNumber + "\").value){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SAVEANDINSERTNEWCOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"MAINFORM\"].submit();\n"
			+ "    }else{\n"
			+ "        alert('Line was NOT saved - item information is being updated.');\n"
			+ "        updateItemInfo();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function gotoDetails(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + DETAILSCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function gotoTotals(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + TOTALSCOMMAND_VALUE + "\";\n"
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
		
		s += "function getFloat(value) {\n"
			+ "    return parseFloat(value, 10);\n"
			+ "}\n"
		;
		
		s += "function calculateextendedprice() {\n"
			+ "    flagDirty();\n"
			+ "    var sqtyshipped = document.getElementById(\"" 
				+ SMOrderDetail.ParamdQtyShipped + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sqtyshipped) == false){\n"
			+ "        alert(\"Qty shipped '\" + sqtyshipped + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sqtyordered = document.getElementById(\"" 
				+ SMOrderDetail.ParamdQtyOrdered + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sqtyordered) == false){\n"
			+ "        alert(\"Qty ordered '\" + sqtyordered + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyOrdered + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    qtyshipped = getFloat(sqtyshipped);\n"
			+ "    if (qtyshipped < 0.00){\n"
			+ "        alert(\"Qty shipped cannot be less than zero.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    qtyordered = getFloat(sqtyordered);\n"
			+ "    if (qtyshipped > qtyordered){\n"
			+ "        alert(\"Qty shipped cannot be greater than qty ordered.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdQtyShipped + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sunitprice = document.getElementById(\"" 
				+ SMOrderDetail.ParamdOrderUnitPrice + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sunitprice) == false){\n"
			+ "        alert(\"Unit price '\" + sunitprice + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdOrderUnitPrice + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.ParamdOrderUnitPrice + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    unitprice = getFloat(sunitprice);\n"
			
			+ "    var extprice = Math.round(qtyshipped * unitprice * 100.00) / 100.00;\n"
			+ "    document.getElementById(\"" 
					+ SMOrderDetail.ParamdExtendedOrderPrice + "\").value = extprice;\n"
		;
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			s += "    var sestimatedunitcost = document.getElementById(\"" 
					+ SMOrderDetail.Parambdestimatedunitcost + "\").value.replace(',','');\n"
			+ "    if (isNumeric(sestimatedunitcost) == false){\n"
			+ "        alert(\"Estimated unit cost '\" + sestimatedunitcost + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderDetail.Parambdestimatedunitcost + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    estimatedunitcost = getFloat(sestimatedunitcost);\n"
			+ "    if (estimatedunitcost > 0.00){\n"
			+ "        var costmultiplier = Math.round((unitprice / estimatedunitcost) * 100.00) / 100.00;\n"
			+ "        document.getElementById(\"" 
					   + COSTMULTIPLIER_PARAM + "\").value = costmultiplier;\n"
			+ "    } else {\n"
			+ "        document.getElementById(\"" 
						+ COSTMULTIPLIER_PARAM + "\").value = 0.00;\n"
			+ "    }\n"
			;
		}
		s+= "}\n"
		;
		
		s += "function colorChangeRed(targetLabel){\n"
			+ "targetLabel.style.backgroundColor=\"RED\""
			+ "}\n"
		;
		
		s += "function colorChangeBack(targetLabel){\n"
			+ "targetLabel.style.backgroundColor=\"" + WORKPERFORMEDCODES_BG_COLOR + "\""
			+ "}\n"
		;
		
		//TODO LTO 20140218
		s += "function useOrderDateAsLBD(){\n"
				//get order date and put the date into the text box.
			+ "document.getElementById(\"" + SMOrderDetail.ParamdatLineBookedDate + "\").value = " + "\"" + header.getM_datOrderDate() + "\";\n"
			+ "}\n"
		;

		s += "function initShortcuts() {\n";
		
		//s += "    shortcut.add(\"Alt+s\", function() {\n";
		//s += "        alert('test');\n";
		//s += "    });\n";
		
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
		
		s += "    shortcut.add(\"Alt+c\",function() {\n";
		s += "        gotoTotals();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        saveDetail();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+a\",function() {\n";
		s += "        saveAndAdd();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+n\",function() {\n";
		s += "        saveAndGoToNext();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+i\",function() {\n";
		s += "        saveAndInsertNew();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        findItem();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+o\",function() {\n";
		s += "        findNonDedicatedItem();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+u\",function() {\n";
		s += "        useOrderDateAsLBD();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		if (header.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			s += "    shortcut.add(\"Alt+p\",function() {\n";
			s += "        multiplyToCalculatePrice();\n";
			s += "    },{\n";
			s += "        'type':'keydown',\n";
			s += "        'propagate':false,\n";
			s += "        'target':document\n";
			s += "    });\n";
		}
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
		
		//TJR - 5/13/2011 - I left all these comments in to use as samples here or elsewhere:
		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";
		
		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";
		
		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";
		
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
