package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTablearterms;
import SMDataDefinition.SMTableconveniencephrases;
import SMDataDefinition.SMTabledefaultitemcategories;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordersources;
import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditOrderEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String CUSTOMERSETTINGS_BG_COLOR = "#FFBCA2";
	private static final String CUSTOMERBILLTO_BG_COLOR = "#CCFFB2";
	private static final String CUSTOMERSHIPTO_BG_COLOR = "#99CCFF";
	private static final String ORDERDATES_BG_COLOR = "#99CCFF";
	private static final String PROJECTFIELDS_BG_COLOR = "#FFBCA2";
	private static final String CONVENIENCEPHRASES_BG_COLOR = "#FFBCA2";
	private static final String ORDERMEMOS_BG_COLOR = "#CCFFB2";
	private static final String ORDERCOMMANDS_BG_COLOR = "#99CCFF";
	public static final String CONVENIENCEPHRASECONTROL_MARKER = "CPM";
	public static final String ORDERUPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String DETAILS_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>etails"; //D
	public static final String TOTALS_BUTTON_LABEL = "Dis<B><FONT COLOR=RED>c</FONT></B>ount"; //C
	public static final String ORDERCREATE_BUTTON_LABEL = "Create <B><FONT COLOR=RED>o</FONT></B>rder"; //O
	public static final String QUOTECREATE_BUTTON_LABEL = "Create qu<B><FONT COLOR=RED>o</FONT></B>te"; //O
	public static final String ORDERINVOICE_BUTTON_LABEL = "<B><FONT COLOR=RED>I</FONT></B>nvoice"; //I
	public static final String ORDERCLONE_BUTTON_LABEL = "C<B><FONT COLOR=RED>l</FONT></B>one"; //L
	public static final String CHANGECUSTOMER_BUTTON_LABEL = "Change custom<B><FONT COLOR=RED>e</FONT></B>r"; //E
	public static final String ORDERITEMIZEDWO_BUTTON_LABEL = "Print installation <B><FONT COLOR=RED>w</FONT></B>ork order"; //W
	public static final String ORDERSERVICEWO_BUTTON_LABEL = "P<B><FONT COLOR=RED>r</FONT></B>int service work order"; //R
	public static final String PROPOSAL_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>roposal"; //P
	//public static final String CREATE_DOCUMENT_FOLDER_BUTTON_LABEL = "Create document <B><FONT COLOR=RED>f</FONT></B>older in Google Drive"; //F
	public static final String RENAME_FOLDER_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate Google Drive folder name"; //U
	//public static final String UPLOAD_FOLDER_BUTTON_LABEL = "Upload files (and create folder) to Google Drive";
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to <B><FONT COLOR=RED>G</FONT></B>oogle Drive";
	public static final String INSERTCPS_GROUP = "INSERTCPSGROUP";
	public static final String INSERTCPSINTOINTERNALNOTES_LABEL = "Internal notes";
	public static final String INSERTCPSINTOINVOICENOTES_LABEL = "Invoice notes";
	public static final String INSERTCPSINTOTICKETNOTES_LABEL = "Ticket notes";
	public static final String INSERTCPSINTOINTERNALNOTES_ID = "INSERTINTOINTERNAL_ID";
	public static final String INSERTCPSINTOINVOICENOTES_ID = "INSERTINTOINVOICE_ID";
	public static final String INSERTCPSINTOTICKETNOTES_ID = "INSERTINTOTICKET_ID";
	public static final String CONVENIENCEPHRASE_SELECT_NAME = "CPSELECT";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String DETAILSCOMMAND_VALUE = "GOTODETAILS";
	public static final String TOTALSCOMMAND_VALUE = "GOTOTOTALS";
	public static final String PRINTSERVICEWOCOMMAND_VALUE = "PRINTSERVICEWORKORDER";
	public static final String PRINTITEMIZEDWOCOMMAND_VALUE = "PRINTITEMIZEDWORKORDER";
	public static final String CHANGECUSTOMERCOMMAND_VALUE = "CHANGECUSTOMER";
	//public static final String USEREMAILFIELD_VALUE = "SELFEMAIL";
	public static final String CREATEINVOICECOMMAND_VALUE = "CREATEINVOICE";
	public static final String CLONEORDERCOMMAND_VALUE = "CLONEORDER";
	//public static final String CREATE_DOCUMENT_FOLDER_COMMAND_VALUE = "CREATEDOCFOLDER";
	public static final String RENAME_FOLDER_COMMAND_VALUE = "RENAMEFOLDER";
	//public static final String UPLOAD_FOLDER_COMMAND_VALUE = "UPLOADFOLDER";
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";
	public static final String CLONEDETAILS_CHECKBOX = "CLONEDETAILS";
	public static final String CLONEPROPOSAL_CHECKBOX = "CLONEPROPOSAL";
	public static final String PROPOSALCOMMAND_VALUE = "PROPOSAL";
	public static final String PRICECHANGE_FLAG = "PRICECHANGEFLAG";
	public static final String PRICECHANGED_VALUE = "PRICECHANGED";
	public static final String SHIPTOCHANGE_FLAG = "SHIPTOCHANGEFLAG"; //Indicates that the ship to address was changed
	public static final String SHIPTOCHANGED_VALUE = "SHIPTOCHANGED";
	public static final String SAVEDORDERDATE_PARAM = "SAVEDORDERDATE";
	public static final String SAVEDEXPECTEDSHIPDATE_PARAM = "SAVEDEXPECTEDSHIPDATE";
	public static final String SAVEDCOMPLETEDDATE_PARAM = "SAVEDCOMPLETEDDATE";
	public static final String SAVEDCONTRACTRECEIVEDDATE_PARAM = "SAVEDCONTRACTRECEIVEDDATE";
	public static final String SAVEDDRAWINGSSUBMITTEDDATE_PARAM = "SAVEDDRAWINGSSUBMITTEDDATE";
	public static final String SAVEDDRAWINGSAPPROVEDDATE_PARAM = "SAVEDDRAWINGSAPPROVEDDATE";
	public static final String SAVEDWARRANTYEXPIRATIONDATE_PARAM = "SAVEDWARRANTYEXPIRATIONDATE";
	public static final String NO_SALESPERSON_SELECTED_VALUE = "**NO_SALESPERSON_SELECTED**";
	public static final String NOOFSERVICEWOCOPIES_NAME = "NOOFSERVICEWOCOPIES";
	public static final String NOOFINSTALLATIONWOCOPIES_NAME = "NOOFINSTALLATIONWOCOPIES";
	public static final String NOOFQUOTECOPIES_NAME = "NOOFQUOTECOPIES";
	public static final String BLANK_DEFAULT_ITEM_CATEGORY_DESC = "** NONE **";
	
	private static final String DATE_FIELD_WIDTH = "16";
	private static final String PHONE_NUMBER_FIELD_WIDTH = "11";
	
	
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
				"smcontrolpanel.SMEditOrderAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditOrders
		);

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

		//If this is a 'resubmit', meaning it's being called by SMEditOrderAction, then
		//the session will have an order header object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		//Make sure we remove any leftover order detail objects:
		currentSession.removeAttribute(SMOrderDetail.ParamObjectName);
		
		String sDBID = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		if (currentSession.getAttribute(SMOrderHeader.ParamObjectName) != null){
			entry = (SMOrderHeader) currentSession.getAttribute(SMOrderHeader.ParamObjectName);
			currentSession.removeAttribute(SMOrderHeader.ParamObjectName);

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			if (!smedit.getAddingNewEntryFlag()){
				if(!entry.load(getServletContext(), sDBID, sUserID, sUserFullName)){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
							+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
							+ entry.getM_strimmedordernumber()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
							+ "&Warning=" + entry.getErrorMessages()
					);
					return;
				}
				
				String sCancelDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(
						entry.getM_datOrderCanceledDate());
				if (sCancelDate.compareTo("1899-12-31 00:00:00") > 0){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
							+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
							+ entry.getM_strimmedordernumber()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
							+ "&Warning=" + "Canceled orders or quotes can not be updated."
					);
					return;
				}
			}else{
				//If we ARE creating a new order, then if this is NOT a resubmit, meaning it's the first time
				//the user comes to the screen after clicking 'create', 
				//we need to set the customer code, and load the customer:
				if (entry.getM_siID().compareTo("-1") != 0){
					//This means this is NOT a re-submit, because then it would have a -1 in the order id
					String sCustomerCode = entry.getM_sCustomerCode();
					//Now clear the order class so we can go on and build it:
					entry = new SMOrderHeader();
					//If it's a new quote, set the order type:
					if (clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiOrderType, request).compareToIgnoreCase(
							Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
						entry.setM_iOrderType(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE));
					}else{
						//If it's NOT a quote, then it MUST have a customer number:
						if (sCustomerCode.compareToIgnoreCase("") == 0){
							response.sendRedirect(
									"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
									+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
									+ entry.getM_strimmedordernumber()
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
									+ "&Warning=" + "You MUST enter a valid customer number."
								);
								return;
						}
					}
					
					entry.setM_sCustomerCode(sCustomerCode);
					if (sCustomerCode.compareToIgnoreCase("") != 0){
						if (!entry.loadDefaultCustomerInformation(getServletContext(), sDBID, sUserName)){
							response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
								+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
								+ entry.getM_strimmedordernumber()
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
								+ "&Warning=" + clsServletUtilities.URLEncode(entry.getErrorMessages())
							);
							return;
						}
					}
				}
			}
		}
		String sObjectName = "Order";
		if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sObjectName = SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE);
		}
		//Record this URL so we can return to it later:
		if (!smedit.getAddingNewEntryFlag()){
			smedit.addToURLHistory("Editing " + sObjectName + " Number " + entry.getM_strimmedordernumber());
		}else{
			smedit.addToURLHistory("Adding a new " + sObjectName + " header");
		}
		
		//Add places API to bill to and ship to address lines. 
		smedit.getPWOut().println(clsServletUtilities.getJQueryIncludeString());
		String sPlacesAPIIncludeString = clsServletUtilities.getPlacesAPIIncludeString(getServletContext(), smedit.getsDBID());
		if(sPlacesAPIIncludeString.compareToIgnoreCase("") != 0) {		
			smedit.getPWOut().println(clsServletUtilities.getPlacesJavascript(
				"Enter address",
				SMOrderHeader.ParamsBillToAddressLine1,
				SMOrderHeader.ParamsBillToCity,
				SMOrderHeader.ParamsBillToState,
				SMOrderHeader.ParamsBillToZip));
		
			smedit.getPWOut().println(clsServletUtilities.getPlacesJavascript(
				"Enter address",
				SMOrderHeader.ParamsShipToAddress1,
				SMOrderHeader.ParamsShipToCity,
				SMOrderHeader.ParamsShipToState,
				SMOrderHeader.ParamsShipToZip));
		
			smedit.getPWOut().println(sPlacesAPIIncludeString);
		}
		
		//Start html at the top of the page.
		smedit.setTitle(sObjectName + " header");
		smedit.printLowProfileHeaderTable();
		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Manage orders</A>");
		//Add a link to edit sales leads:
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditBids, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditBidSelect?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Edit " + SMBidEntry.ParamObjectName + "s</A>");
		}
		if ((entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0)
			&& (!smedit.getAddingNewEntryFlag())){
			//If the user has permission to configure work orders:
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMConfigureWorkOrders, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				smedit.getPWOut().println("&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMConfigWorkOrderEdit"
					+ "?" + SMWorkOrderHeader.Paramlid + " =-1" 
					+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
					+ "&SubmitEdit=true&CallingClass=smcontrolpanel.SMEditOrderEdit"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Schedule this order</A>");
				;
			}
			
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditBids, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				smedit.getPWOut().println("&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditBidEntry?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
						+ "&" + "SubmitAdd=Y"
						+ "&" + SMBidEntry.Paramdatoriginationdate + "=" + clsServletUtilities.URLEncode(clsDateAndTimeConversions.now("M/d/yyyy"))
						+ "&" + SMBidEntry.Paramscreatedfromordernumber + "=" + clsServletUtilities.URLEncode(entry.getM_strimmedordernumber())
						+ "&" + SMBidEntry.Paramemailaddress + "=" + clsServletUtilities.URLEncode(entry.getM_sEmailAddress())
						+ "&" + SMBidEntry.Paramsaltphonenumber + "=" + clsServletUtilities.URLEncode(entry.getM_ssecondarybilltophone())
						+ "&" + SMBidEntry.Paramscontactname + "=" + clsServletUtilities.URLEncode(entry.getM_sBilltoContact())
						+ "&" + SMBidEntry.Paramscustomername + "=" + clsServletUtilities.URLEncode(entry.getM_sBillToName())
						+ "&" + SMBidEntry.Paramsfaxnumber + "=" + clsServletUtilities.URLEncode(entry.getM_sBillToFax())
						+ "&" + SMBidEntry.Paramsphonenumber + "=" + clsServletUtilities.URLEncode(entry.getM_sBilltoPhone())
						+ "&" + SMBidEntry.Paramssalespersoncode + "=" + clsServletUtilities.URLEncode(entry.getM_sSalesperson())
						+ "&" + SMBidEntry.ParamiOrderSourceID + "=" + clsServletUtilities.URLEncode(entry.getM_iOrderSourceID())
						+ "&" + SMBidEntry.Paramsprojectname + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToName())
						+ "&" + SMBidEntry.Paramsshiptoaddress1 + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToAddress1())
						+ "&" + SMBidEntry.Paramsshiptoaddress2 + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToAddress2())
						+ "&" + SMBidEntry.Paramsshiptoaddress3 + "=" + clsServletUtilities.URLEncode((entry.getM_sShipToAddress3() + " " + entry.getM_sShipToAddress4().trim()))
						+ "&" + SMBidEntry.Paramsshiptocity + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToCity())
						+ "&" + SMBidEntry.Paramsshiptostate + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToState())
						+ "&" + SMBidEntry.Paramsshiptozip + "=" + clsServletUtilities.URLEncode(entry.getM_sShipToZip())
						+ "&CallingClass=smcontrolpanel.SMEditBidEntry"
						+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
						+ "\">Create&nbsp;follow&nbsp;up&nbsp;sales&nbsp;lead</A>");
				;
			}
		}
		
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A><BR>");
		
		//String sWarning = SMUtilities.get_Request_Parameter("Warning", request);
		String sMessage = clsManageRequestParameters.get_Request_Parameter("Message", request);
		//if (! sWarning.equalsIgnoreCase("")){
		//	smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		//}
		if (! sMessage.equalsIgnoreCase("")){
			smedit.getPWOut().println("<B>" + sMessage + "</B><BR>");
		}
		
		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);
		//smedit.setOnSubmitFunction("validateForm()");

		try{
			smedit.createEditPage(getEditHTML(smedit, entry, sObjectName, sDBID, sUserID, sUserFullName), "");
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					+ "&Warning=" + clsServletUtilities.URLEncode(sError)
			);
			return;
		}
		return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			SMOrderHeader entry, 
			String sObjectName,
			String sDBID,
			String sUserID,
			String sUserFullName) throws SQLException{

		String s = "";
		
		s += sCommandScripts(entry, sm);
		s += sStyleScripts();
		
		boolean bUseGoogleDrivePicker = false;
		String sPickerScript = "";
			try {
			 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
						SMCreateGoogleDriveFolderParamDefinitions.ORDER_RECORD_TYPE_PARAM_VALUE,
						entry.getM_strimmedordernumber(),
						getServletContext(),
						sDBID);
			} catch (Exception e) {
				System.out.println("[1554818420] - Failed to load drivepicker.js - " + e.getMessage());
			}
	
			if(sPickerScript.compareToIgnoreCase("") != 0) {
				s += sPickerScript;
				bUseGoogleDrivePicker = true;
			}
			
		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">";

		//If the user changes the price level or the price list, the unit prices have to be recalculated.  This
		//flag will be set if either of those are changed, so that the program knows to recalculate the unit prices:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PRICECHANGE_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(PRICECHANGE_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + PRICECHANGE_FLAG + "\""
			+ "\">";
		
		//If the ship to address is changed, this gets reset - that way the server knows to update the geocode:
		//If it's a new order we ALWAYS get the geocode:
		if (sm.getAddingNewEntryFlag()){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SHIPTOCHANGE_FLAG + "\" VALUE=\"" 
				+ SHIPTOCHANGED_VALUE + "\""
				+ " id=\"" + SHIPTOCHANGE_FLAG + "\""
				+ "\">";
		//Otherwise, we only get it if it's re-submitted back to this class or changed with javascript:
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SHIPTOCHANGE_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(SHIPTOCHANGE_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + SHIPTOCHANGE_FLAG + "\""
				+ "\">";
		}
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdatLastPostingDate + "\" VALUE=\"" 
			+ entry.getM_datLastPostingDate() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdatOrderCreationDate + "\" VALUE=\"" 
			+ entry.getM_datOrderCreationDate() + "\">";
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdatShipmentDate + "\" VALUE=\"" 
			+ entry.getM_datShipmentDate() + "\">";*/
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdOrderTaxAmount + "\" VALUE=\"" 
			+ entry.getsordersalestaxamount() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdTaxBase + "\" VALUE=\"" 
			+ entry.getM_dTaxBase() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdTotalAmountItems + "\" VALUE=\"" 
			+ entry.getM_dTotalAmountItems() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiNextDetailNumber + "\" VALUE=\"" 
			+ entry.getM_iNextDetailNumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiNumberOfInvoices + "\" VALUE=\"" 
			+ entry.getM_iNumberOfInvoices() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiNumberOfLinesOnOrder + "\" VALUE=\"" 
			+ entry.getM_iNumberOfLinesOnOrder() + "\">";
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiNumberOfLinesQtyShipped + "\" VALUE=\"" 
			+ entry.getM_iNumberOfLinesQtyShipped() + "\">";*/
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiOnHold + "\" VALUE=\"" 
			+ entry.getM_iOnHold() + "\">";
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiOrderCompleted + "\" VALUE=\"" 
			+ entry.getM_iOrderCompleted() + "\">";*/
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiOverCreditLimit + "\" VALUE=\"" 
			+ entry.getM_iOverCreditLimit() + "\">";*/
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiPrintStatus + "\" VALUE=\"" 
			+ entry.getM_iPrintStatus() + "\">";*/
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
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParammFieldNotes + "\" VALUE=\"" 
			+ entry.getM_sFieldNotes() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsClonedFrom + "\" VALUE=\"" 
			+ entry.getM_sClonedFrom() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsCustomerControlAcctSet + "\" VALUE=\"" 
			+ entry.getM_sCustomerControlAcctSet() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramsgeocode + "\" VALUE=\"" 
			+ entry.getM_sgeocode() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsiID + "\" VALUE=\"" 
			+ entry.getM_siID() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsLastInvoiceNumber + "\" VALUE=\"" 
			+ entry.getM_sLastInvoiceNumber() + "\">";
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsLastPrintedBy + "\" VALUE=\"" 
			+ entry.getM_sLastPrintedBy() + "\">";*/
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsLastPrintedDate + "\" VALUE=\"" 
			+ entry.getM_sLastPrintedDate() + "\">";*/
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsOrderCreatedByFullName + "\" VALUE=\"" 
			+ entry.getM_sOrderCreatedByFullName() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamlOrderCreatedByID + "\" VALUE=\"" 
				+ entry.getM_lOrderCreatedByID() + "\">";
		/*s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsOrderCreationTime + "\" VALUE=\"" 
			+ entry.getM_sOrderCreationTime() + "\">";*/
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsOrderNumber + "\" VALUE=\"" 
			+ entry.getM_sOrderNumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramstrimmedordernumber + "\" VALUE=\"" 
			+ entry.getM_strimmedordernumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsCustomerCode + "\" VALUE=\"" 
			+ entry.getM_sCustomerCode() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdatOrderCanceledDate + "\" VALUE=\"" 
			+ entry.getM_datOrderCanceledDate() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount + "\" VALUE=\"" 
			+ entry.getM_dPrePostingInvoiceDiscountAmount() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage + "\" VALUE=\"" 
			+ entry.getM_dPrePostingInvoiceDiscountPercentage() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Parambddepositamount + "\" VALUE=\"" 
			+ entry.getM_bddepositamount() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc + "\" VALUE=\"" 
			+ entry.getM_sPrePostingInvoiceDiscountDesc() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, sm.getRequest()) + "\">";
		
		//We have to keep track of these dates because when the date picker is invoked, it can change the 
		//value of these fields, but it won't trigger an 'onchange' event and we won't know the user has
		//changed the form:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDORDERDATE_PARAM + "\""
			+ " id=\"" + SAVEDORDERDATE_PARAM + "\""
			+ " VALUE=\"" + entry.getM_datOrderDate() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\""
			+ " id=\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\""
			+ " VALUE=\"" + entry.getM_datExpectedShipDate() + "\"" + ">";
		//s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDCOMPLETEDDATE_PARAM + "\""
		//	+ " id=\"" + SAVEDCOMPLETEDDATE_PARAM + "\""
		//	+ " VALUE=\"" + entry.getM_datCompletedDate() + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SAVEDWARRANTYEXPIRATIONDATE_PARAM + "\""
			+ " id=\"" + SAVEDWARRANTYEXPIRATIONDATE_PARAM + "\""
			+ " VALUE=\"" + entry.getM_datwarrantyexpiration() + "\"" + ">";
		
		//New Row
		s += "<TR>";
		
		String sOrderNumber = "NEW";
		if (!sm.getAddingNewEntryFlag()){
			sOrderNumber = entry.getM_strimmedordernumber();
		}
		ARCustomer cus = new ARCustomer(entry.getM_sCustomerCode());
		if (entry.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			if (!cus.load(getServletContext(), sm.getsDBID())){
				s += " Error loading customer " + entry.getM_sCustomerCode() + " - " + cus.getErrorMessageString();
				return s;
			}
		}
		String sOnHold = "";
		if (cus.getM_iOnHold().compareToIgnoreCase("1") == 0){
			sOnHold = "&nbsp;<FONT COLOR=RED><B>ON HOLD</B></FONT>";
		}
		
		//Get the default salespersons here:
		String SQL = "SELECT"
				+ " " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ ", " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
				+ " FROM " + SMTabledefaultsalesgroupsalesperson.TableName + " LEFT JOIN " + SMTablesalesgroups.TableName
				+ " ON " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.lsalesgroupid
				+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
				+ " LEFT JOIN " + SMTablesalesperson.TableName + " ON "
				+ SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
				+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			+ " WHERE ("
				+ "(" + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + entry.getM_sCustomerCode() + "')"
			+ ")"
		;
		ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".getdefaultsalespersons - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName());
		String sDefaultSalespersons = "";
		boolean bFirstRecord = true;
		while (rsSalespersons.next()){
			if (!bFirstRecord){
				sDefaultSalespersons += ",";
			}
			sDefaultSalespersons += "&nbsp;"
				+ rsSalespersons.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc) + "</B>:&nbsp;"
				+ rsSalespersons.getString(SMTabledefaultsalesgroupsalesperson.TableName 
					+ "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode)
			;
			bFirstRecord = false;
		}
		String sPriceLevel = "BASE";
		if (cus.getM_sPriceLevel().compareToIgnoreCase("0") != 0){
			sPriceLevel = "LEVEL " + cus.getM_sPriceLevel();
		}
		s += "<B>" + sObjectName + " number:</B>&nbsp;" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMDisplayOrderInformation"
			+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sOrderNumber + "</A>"
			+ "&nbsp;<B>" + sObjectName + " date:</B>&nbsp" + entry.getM_datOrderDate()
			+ "<BR><span style = \" font-size: small; \">"
			;
		String sTax = "";
		try {
			sTax = cus.getTaxJurisdictionAndType(sDBID, getServletContext(), sUserID, sUserFullName);
		} catch (Exception e1) {
			sTax = "(NOT FOUND)";
		}
		
		if (entry.getM_sCustomerCode().compareToIgnoreCase("") != 0){
			s += "<B>Customer:</B>&nbsp;" 
				+ entry.getM_sCustomerCode()
				+ sOnHold
				+ "&nbsp;<B>DEFAULTS</B>:"
				+ "&nbsp;<B>Salespersons by sales group:</B>&nbsp;" + sDefaultSalespersons + "&nbsp;"
				+ "<B>Price level</B>:&nbsp;" + sPriceLevel + ";&nbsp;"
				+ "<B>Terms</B>:&nbsp;" + cus.getM_sTerms() + ";&nbsp;"
				+ "<B>Tax</B>:&nbsp;" + sTax + ";&nbsp;"
				;
			s += "<BR><span style= \" font-size:small; \" > <B>Customer comments:</B>&nbsp;" + cus.getM_mCustomerComments()
					+ "</span>"
				;
		}

		String sMessages = "";
		try {
			sMessages = entry.checkOrderForDiscrepancies(sm.getsDBID(), getServletContext(), sm.getUserID(), sm.getFullUserName());
		} catch (Exception e) {
			sMessages = "Error checking order - " + e.getMessage();
		}
		
		if (sMessages.compareToIgnoreCase("") != 0){
			s += "<BR><B><FONT COLOR=RED>MESSAGE:&nbsp;" + sMessages + "</FONT></B>";
		}
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial;\">\n";		
		
		//Create the order commands line at the top:
		boolean bAllowServiceTicketPrinting = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMPrintServiceTicket, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowInstallationTicketPrinting = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMPrintInstallationTicket, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		s += createOrderCommandsTable(sm, sObjectName, entry, bAllowServiceTicketPrinting, bAllowInstallationTicketPrinting, sDBID, sUserID);
		
		//Create the customer area table:
		s += "<TR><TD><TABLE style=\" title:CustomerArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + CUSTOMERSETTINGS_BG_COLOR + "; \">" 
			+ createCustomerSettingsTable(sm, entry, cus, sObjectName) + "</TD>\n";
		s += "<TD style=\" vertical-align:top; background-color: " + CUSTOMERBILLTO_BG_COLOR + "; \">" 
			+ createCustomerBillToTable(sm, entry) + "</TD>\n";
		s += "<TD style=\" vertical-align:top; background-color: " + CUSTOMERSHIPTO_BG_COLOR + "; \">" 
			+ createCustomerShipToTable(sm, entry) + "</TD>\n";
		s += "</TR></TD></TABLE style=\" title:ENDCustomerArea; \">\n";

		//Create the order area table:
		s += "<TR><TD><TABLE style=\" title:OrderArea; \" width=100% >\n";
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; background-color: " + ORDERDATES_BG_COLOR + "; \">" 
			+ createOrderSettings2Table(sm, entry) + "</TD>\n";
		s += "<TD style=\" vertical-align:top; background-color: " + PROJECTFIELDS_BG_COLOR + "; \">" 
			+ createProjectFieldsTable(sm, entry) + "</TD>\n";
		s += "</TR></TD></TABLE style=\" title:ENDOrderArea; \">\n";
		
		//Create the order memo table:
		s += createOrderMemosTable(sm, entry, sm.getAddingNewEntryFlag(), bUseGoogleDrivePicker);
		
		//Create the order commands line at the bottom:
		s += createOrderCommandsTable(sm, sObjectName, entry, false, false, sDBID, sUserID);
		
		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		s += "<p><FONT COLOR=RED>*</FONT>&nbsp;Required fields on both ORDER and QUOTE.<BR>" +
				"<FONT COLOR=RED>**</FONT>&nbsp;Required fields on ORDER only.</p>";
		

		return s;
	}
	
	private String createCustomerSettingsTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry,
			ARCustomer customer,
			String sObjectName) throws SQLException{
		String s = "";
		String SQL = "";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:CustomerSettings; \">\n";
		
		//The customer price level:
		s += "<TR>";
		s += "<TD class=\"fieldlabel\">Price level<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\""
			+ " id = \"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\""
			+ " onchange=\"flagPricesDirty();\""
			+ " style=\"width:100%;\""
			+ ">";
		s += "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT PRICE LEVEL ***</OPTION>";
		for (int i = 0; i <= 5; i++){
			s += "<OPTION";
			if (entry.getM_iCustomerDiscountLevel().compareToIgnoreCase(Integer.toString(i)) == 0){
				s += " selected=YES ";
			}
			String sDesc = "LEVEL " + Integer.toString(i);
			if (i == 0){
				sDesc = "BASE LEVEL";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" + sDesc + "</OPTION>";
		}
		s += "</SELECT>";
		s += "</TD></TR>";

		//Terms
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Terms<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class= \" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamsTerms + "\"" 
			+ " id = \"" + SMOrderHeader.ParamsTerms + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ ">";

		SQL = "SELECT"
			+ " " + SMTablearterms.sTermsCode
			+ ", " + SMTablearterms.sDescription
			+ " FROM " + SMTablearterms.TableName
			+ " ORDER BY " + SMTablearterms.sTermsCode
		;
		s += "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT TERMS ***</OPTION>";
		try {
			ResultSet rsTerms = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332425569] SQL: " + SQL);
			while (rsTerms.next()){
				String sTerms = rsTerms.getString(SMTablearterms.sTermsCode);
				s += "<OPTION";
				if (entry.getM_sTerms().compareToIgnoreCase(sTerms) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sTerms + "\">" + rsTerms.getString(SMTablearterms.sDescription) + "</OPTION>";
			}
			rsTerms.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading terms - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";

		//Price list:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Price list:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamsDefaultPriceListCode + "\"" 
			+ " id = \"" + SMOrderHeader.ParamsDefaultPriceListCode + "\""
			+ " onchange=\"flagPricesDirty();\""
			+ " style=\"width:100%;\""
			+ ">";

		SQL = "SELECT"
			+ " " + SMTablepricelistcodes.spricelistcode
			+ ", " + SMTablepricelistcodes.sdescription
			+ " FROM " + SMTablepricelistcodes.TableName
			+ " ORDER BY " + SMTablepricelistcodes.spricelistcode
		;
		try {
			ResultSet rsPriceList = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425610] SQL: " + SQL);
			while (rsPriceList.next()){
				String sPLCode = rsPriceList.getString(SMTablepricelistcodes.spricelistcode).trim();
				s += "<OPTION";
				if (entry.getM_sDefaultPriceListCode().trim().compareToIgnoreCase(sPLCode) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sPLCode + "\">" + sPLCode + " - " 
				+ rsPriceList.getString(SMTablepricelistcodes.sdescription).trim()
					+ "</OPTION>";
			}
			rsPriceList.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading price list codes - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";
		
		//Tax type:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Tax<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.Paramitaxid + "\"" 
			+ " id = \"" + SMOrderHeader.Paramitaxid + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ ">";
		s += "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT TAX ***</OPTION>";
		SQL = "SELECT"
			+ " " + SMTabletax.lid
			+ ", " + SMTabletax.staxjurisdiction
			+ ", " + SMTabletax.sdescription
			+ ", " + SMTabletax.staxtype
			+ ", " + SMTabletax.iactive
			+ " FROM " + SMTabletax.TableName
			//+ " WHERE ("
			//	+ "(" + SMTabletax.iactive + " = 1)"
			//+ ")"
			+ " ORDER BY " + SMTabletax.staxjurisdiction + ", " + SMTabletax.staxtype
		;
		//This variable keeps track of whether the current tax was found in the list of taxes:
		//boolean bCurrentTaxWasFound = false;
		//If the 'current' tax is just the blank one, as it would be for a new order, then 
		//the 'current' tax has been found:
		//if (entry.getitaxid().compareToIgnoreCase("") == 0){
		//	bCurrentTaxWasFound = true;
		//}
		try {
			ResultSet rsTax = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425638] SQL: " + SQL);
			while (rsTax.next()){
				//Get the tax ID of each record:
				String sTaxID = Long.toString(rsTax.getLong(SMTabletax.lid));
				//This will contain the 'selected' flag, if appropriate:
				String sOptionSelected = "";
				//This tells us whether to list this tax record as an option or not:
				boolean bListOption = false;
				//Is the tax record an 'active' one?
				boolean bTaxIsActive = false;
				//Is this record the 'matching' tax?
				boolean bTaxRecordMatchesCurrentTax = false;
				//If the this tax record matches the one on the order:
				if (entry.getitaxid().compareToIgnoreCase(sTaxID) == 0){
					sOptionSelected += " selected=YES ";
					//bCurrentTaxWasFound = true;
					bTaxRecordMatchesCurrentTax = true;
				}
				if (rsTax.getLong(SMTabletax.iactive) == 1){
					bTaxIsActive = true;
				}
				//If the tax record is ACTIVE, then list it.  
				//OR if it's the one we're looking for, even if it's NOT active, list it.
				if (bTaxIsActive || bTaxRecordMatchesCurrentTax){
					bListOption = true;
				}
				if (bListOption){
					String sInactiveFlag = "";
					if (!bTaxIsActive){
						sInactiveFlag = " (INACTIVE)";
					}
					s += "<OPTION" + sOptionSelected
						+ " VALUE=\"" + sTaxID + "\">" 
						+ rsTax.getString(SMTabletax.staxjurisdiction)
						+ " - " + rsTax.getString(SMTabletax.staxtype)
						+ " - " + rsTax.getString(SMTabletax.sdescription)
						+ sInactiveFlag
						+ "</OPTION>"
					;
				}
			}
			rsTax.close();
		} catch (SQLException e) {
			throw new SQLException("Error [1454445632] loading taxes - " + e.getMessage());
		}
		/* - TJR - 2/4/2016 - removed this because if the user tries to save an order with a completely
		 * missing tax, a lot of the order validation and saving fails and we don't want to try to
		 * hack around those functions at this time.  They have to have at least an inactive tax type to save.
		 * You can save the order with an inactive tax type (but not create an INVOICE with it), but just can't 
		 * save at all if the tax isn't even in the list of taxes anymore.
		//If the current tax on this order was NOT found, we need to add it to the list so it won't be lost
		//when the order is saved:
		if (!bCurrentTaxWasFound){
			s += "<OPTION selected=YES"
				+ " VALUE=\"" + entry.getitaxid() + "\">" 
				+ entry.getstaxjurisdiction()
				+ " - " + entry.getstaxtype()
				+ " (NOT FOUND)"
				+ "</OPTION>"
			;
		}
		*/
		s += "</SELECT>";
		
		/* - see the note above about not allowing deleted taxes to appear:
		//If the current tax on this order was NOT found, we need to store the jurisdiction, ID, taxtype so
		//it can be saved later:
		if (!bCurrentTaxWasFound){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamsTaxJurisdiction + "\" VALUE=\"" 
				+ entry.getstaxjurisdiction() + "\""
				+ " id=\"" + SMOrderHeader.ParamsTaxJurisdiction + "\""
				+ "\">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.ParamiTaxClass + "\" VALUE=\"" 
				+ entry.getM_iTaxClass() + "\""
				+ " id=\"" + SMOrderHeader.ParamiTaxClass + "\""
				+ "\">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMOrderHeader.Paramstaxtype + "\" VALUE=\"" 
					+ entry.getstaxtype() + "\""
					+ " id=\"" + SMOrderHeader.Paramstaxtype + "\""
					+ "\">";
		}
		*/
		s += "</TD></TR>";
		
		//PO Number
		s += "<TR>";
		String sPORequired = "";
		if (customer.getM_sRequiresPO().compareToIgnoreCase("1") == 0){
			sPORequired = "<B><I><FONT COLOR=RED>&nbsp;REQUIRED!</FONT></I></B>";
		}
		s += "<TD class=\" fieldlabel \">PO number" + sPORequired + ":&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsPONumber + "\""
			+ " VALUE=\"" + entry.getM_sPONumber().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsPONumber + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sPONumberLength)
			+ ">"
		;
		
		//Sales person:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Salesperson:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamsSalesperson + "\""
			+ " id = \"" + SMOrderHeader.ParamsSalesperson + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			 + " >"
			+ "<OPTION VALUE=\"" + NO_SALESPERSON_SELECTED_VALUE + "\"> ** SELECT A SALESPERSON **</OPTION>"
			+ "<OPTION";
		if (entry.getM_sSalesperson().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + "" + "" + "\"> Not assigned (N/A) </OPTION>";
		
		//First, we check to see if the salesperson on this order is still in the salespersons' list:
		String sDeletedSalesperson = "";
		SQL = "SELECT"
			+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			+ " FROM " + SMTableorderheaders.TableName + " LEFT JOIN " +  SMTablesalesperson.TableName
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" 
				+ entry.getM_strimmedordernumber() + "')"
				+ " AND (" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + " IS NULL)"
			+ ")"

		;
		ResultSet rsCheckSalespersons = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332425668] SQL: " + SQL);
		if (rsCheckSalespersons.next()){
			String sSalespersonOnOrder = rsCheckSalespersons.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sSalesperson);
			//So if the salesperson on the order HAS been deleted, and if it's NOT 'N/A', then we'll want
			//to add him to the list:
			if (sSalespersonOnOrder.compareToIgnoreCase("N/A") != 0){
				sDeletedSalesperson = sSalespersonOnOrder;
			}
		}
		rsCheckSalespersons.close();
		
		SQL = "SELECT"
			+ " " + SMTablesalesperson.sSalespersonCode
			+ ", " + SMTablesalesperson.sSalespersonFirstName
			+ ", " + SMTablesalesperson.sSalespersonLastName
			+ " FROM " + SMTablesalesperson.TableName
			;
		if (sDeletedSalesperson.compareToIgnoreCase("") != 0){
			SQL += " UNION SELECT '" + sDeletedSalesperson + "' AS " + SMTablesalesperson.sSalespersonCode
			+ ", '(Salesperson' AS " + SMTablesalesperson.sSalespersonFirstName
			+ ", ' deleted)' AS " + SMTablesalesperson.sSalespersonLastName
		;
		}
		SQL += " ORDER BY " + SMTablesalesperson.sSalespersonFirstName;
		try {
			ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425693] SQL: " + SQL);
			while (rsSalespersons.next()){
				String sSalesCode = rsSalespersons.getString(SMTablesalesperson.sSalespersonCode);
				String sSalesPersonName = rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName).trim() + " "
										 + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName).trim();
				s += "<OPTION";
				if (sSalesCode.compareToIgnoreCase(entry.getM_sSalesperson()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sSalesCode + "\">" ;
				if(sSalesPersonName.equals(" "))
					s += "(BLANK) - ";
				else
					s += sSalesPersonName + " - ";
				s += sSalesCode + "</OPTION>";
			}
			rsSalespersons.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading salespersons with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";

		//Doing Business As Addresses:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Doing business as:<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.Paramidoingbusinessasaddress + "\"" 
			+ " id = \"" + SMOrderHeader.Paramidoingbusinessasaddress + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\"> ** SELECT A BUSINESS ADDRESS **</OPTION>";
		SQL = "SELECT"
			+ " " + SMTabledoingbusinessasaddresses.lid
			+ ", " + SMTabledoingbusinessasaddresses.sDescription
			+ " FROM " + SMTabledoingbusinessasaddresses.TableName
			+ " ORDER BY " + SMTabledoingbusinessasaddresses.sDescription
		;

		try {
			ResultSet rsDBA = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [13324257285] SQL: " + SQL);
			int icounter = 0;
			while(rsDBA.next()) {
				icounter++;
			}
			rsDBA.beforeFirst();
			while (rsDBA.next()){
				String sBDAID = Integer.toString(rsDBA.getInt(SMTabledoingbusinessasaddresses.lid));
				s += "<OPTION";
				if (sBDAID.compareToIgnoreCase(entry.getM_idoingbusinessasaddressid()) == 0 || icounter == 1){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sBDAID + "\">" 
				+ rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription).trim()
				+ "</OPTION>";
			}
			rsDBA.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading DBAs with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";
		
		//Sales Group:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Sales group<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamiSalesGroup + "\"" 
			+ " id = \"" + SMOrderHeader.ParamiSalesGroup + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ ">"
			+ "<OPTION VALUE=\"" + "0" + "\"> ** SELECT A SALES GROUP **</OPTION>";
		SQL = "SELECT"
			+ " " + SMTablesalesgroups.iSalesGroupId
			+ ", " + SMTablesalesgroups.sSalesGroupCode
			+ ", " + SMTablesalesgroups.sSalesGroupDesc
			+ " FROM " + SMTablesalesgroups.TableName
			+ " ORDER BY " + SMTablesalesgroups.sSalesGroupCode
		;
		try {
			ResultSet rsSalesgroups = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425727] SQL: " + SQL);
			while (rsSalesgroups.next()){
				String sSalesGroupID = Long.toString(rsSalesgroups.getLong(SMTablesalesgroups.iSalesGroupId));
				s += "<OPTION";
				if (sSalesGroupID.compareToIgnoreCase(entry.getM_iSalesGroup()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sSalesGroupID + "\">" 
				+ rsSalesgroups.getString(SMTablesalesgroups.sSalesGroupCode).trim()
				+ " " + rsSalesgroups.getString(SMTablesalesgroups.sSalesGroupDesc).trim()
				+ "</OPTION>";
			}
			rsSalesgroups.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading sales groups with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";

		//Service type:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Service type<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT ID= \"" + SMOrderHeader.ParamsServiceTypeCode + "\""
			+ " onchange=\"setDefaultItemCategory(this);\""
			+ " NAME=\"" + SMOrderHeader.ParamsServiceTypeCode + "\"" 
			+ " id = \"" + SMOrderHeader.ParamsServiceTypeCode + "\""
			+ " style=\"width:100%;\""
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\"> ** SELECT A SERVICE TYPE **</OPTION>";
		SQL = "SELECT"
			+ " " + SMTableservicetypes.sCode
			+ ", " + SMTableservicetypes.sName
			+ " FROM " + SMTableservicetypes.TableName
			+ " ORDER BY " + SMTableservicetypes.sName + " DESC"
		;
		try {
			ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425763] SQL: " + SQL);
			while (rsServiceTypes.next()){
				String sServiceTypeCode = rsServiceTypes.getString(SMTableservicetypes.sCode);
				s += "<OPTION";
				if (sServiceTypeCode.compareToIgnoreCase(entry.getM_sServiceTypeCode()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sServiceTypeCode + "\">" 
				+ rsServiceTypes.getString(SMTableservicetypes.sName).trim()
				+ "</OPTION>";
			}
			rsServiceTypes.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading service types with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		s += "</TD></TR>";
		
		//Default location:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Default item<BR>location<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \">"
			+ "<SELECT ID= \"" + SMOrderHeader.ParamsLocation + "\""
			+ " onchange=\"setDefaultItemCategory(this);\""
			+ " NAME=\"" + SMOrderHeader.ParamsLocation + "\"" 
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\">* SELECT LOCATION *</OPTION>";
		SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY " + SMTablelocations.sLocation
		;
		try {
			ResultSet rsDefaultItemLocation = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425792] SQL: " + SQL);
			while (rsDefaultItemLocation.next()){
				String sDefaultItemLocation = rsDefaultItemLocation.getString(SMTablelocations.sLocation);
				s += "<OPTION";
				if (sDefaultItemLocation.compareToIgnoreCase(entry.getM_sLocation()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sDefaultItemLocation + "\">" 
				+ sDefaultItemLocation + " - " 
				+ rsDefaultItemLocation.getString(SMTablelocations.sLocationDescription)
				+ "</OPTION>";
			}
			rsDefaultItemLocation.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading default item locations with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		//Order date
		s += "&nbsp;&nbsp;&nbsp;<B>" + "Order" + " date:&nbsp;</B>";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamdatOrderDate + "\""
			+ " VALUE=\"" + entry.getM_datOrderDate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamdatOrderDate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + DATE_FIELD_WIDTH
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(SMOrderHeader.ParamdatOrderDate, getServletContext())
		;
		s += "</TD></TR>";
		
		//Default item category:
		s += "<TR>";
		s += "<TD style=\"font-weight: bold; text-align: right; \"><B>Default item <br>category:</B>&nbsp;</TD>";
		//s += "<TD style=\"font-weight: bold; text-align: right; \" ><B>Default</B>&nbsp;</TD>";
		//s += "<TD style=\"font-weight: bold; text-align: left; \" ><B> item category:</B></TD>";
	//	s += "</TR>";
	//	s += "<TR>";
		s += "<TD style = \" text-align: left; \" COLSPAN = 2>"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamsDefaultItemCategory + "\"" 
			+ " id = \"" + SMOrderHeader.ParamsDefaultItemCategory + "\""
			+ " onchange=\"flagDirty();\""
			+ ">";
		//Add one item for NO default item category:
		s += "<OPTION";
		if (entry.getM_sDefaultItemCategory().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + "" + "\">" 
		+ BLANK_DEFAULT_ITEM_CATEGORY_DESC
		+ "</OPTION>";
		
		SQL = "SELECT"
			+ " " + SMTableiccategories.sCategoryCode
			+ ", " + SMTableiccategories.sDescription
			+ " FROM " + SMTableiccategories.TableName
			+ " ORDER BY " + SMTableiccategories.sCategoryCode
		;
		try {
			ResultSet rsCategories = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425820] SQL: " + SQL);
			while (rsCategories.next()){
				String sItemCategory = rsCategories.getString(SMTableiccategories.sCategoryCode);
				s += "<OPTION";
				if (sItemCategory.compareToIgnoreCase(entry.getM_sDefaultItemCategory()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sItemCategory + "\">" 
				+ sItemCategory 
				+ " - " + rsCategories.getString(SMTableiccategories.sDescription)
				+ "</OPTION>";
			}
			rsCategories.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading item categories with SQL: " + SQL + " - " + e.getMessage());
		}

		s += "</SELECT>";
		
		s += "</TD>";	
		s += "</TR>";
		
		//Expected ship date
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Expected ship:&nbsp;</TD>";
		s += "<TD ><INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamdatExpectedShipDate + "\""
			+ " VALUE=\"" + entry.getM_datExpectedShipDate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamdatExpectedShipDate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + DATE_FIELD_WIDTH
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(SMOrderHeader.ParamdatExpectedShipDate, getServletContext())
		;
		
		//Order type
		s += "&emsp;&emsp;&emsp;&emsp;&emsp;"
		+ "<B>Type:</B>&nbsp;"
		+ "<SELECT NAME=\"" + SMOrderHeader.ParamiOrderType + "\"" 
		+ " id = \"" + SMOrderHeader.ParamiOrderType + "\""
		+ " onchange=\"checkConversionFromQuote();\""
		//+ " style=\"width:99%;\""
		+ ">";
		//s += "<OPTION VALUE = \"" + "" + "\">SELECT</OPTION>";
		//If we are creating an order, then the list depends on whether it's an order or a quote:
		if (entry.getM_siID().compareTo("-1") == 0){
			//Then if it's a quote, don't give the options to set it to active or standing:
			if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
				s += "<OPTION";
				if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE) 
				+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE) 
				+ "</OPTION>";
			}else{
				//But if it's NOT a quote, don't give the option to choose 'quote':
				s += "<OPTION";
				if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE) 
					+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_ACTIVE) 
					+ "</OPTION>";
				s += "<OPTION";
				if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING) 
					+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_STANDING) 
					+ "</OPTION>";			
			}
		}else{
			s += "<OPTION";
			if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE)) == 0){
				s += " selected=YES ";
			}
			s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE) 
				+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_ACTIVE) 
				+ "</OPTION>";
			s += "<OPTION";
			if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING)) == 0){
				s += " selected=YES ";
			}
			s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING) 
				+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_STANDING) 
				+ "</OPTION>";
			if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
				s += "<OPTION";
				if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
					s += " selected=YES ";
				}
				s += " VALUE = \"" + Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE) 
				+ "\">" +  SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE) 
				+ "</OPTION>";
			}
		}
		
		s += "</SELECT>";
		s += "</TD>";
		s += "</TR>";
		
		//Order sources
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Marketing sources<FONT COLOR=RED>*</FONT>:&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \"" + ">"
			+ "<SELECT NAME=\"" + SMOrderHeader.ParamiOrderSourceID + "\"" 
			+ " id = \"" + SMOrderHeader.ParamiOrderSourceID + "\""
			+ " onchange=\"flagDirty();\""
			+ " style=\"width:100%;\""
			+ ">"
			+ "<OPTION VALUE = \"" + "" + "\"> ** SELECT A MARKETING SOURCE **</OPTION>";
			SQL = "SELECT"
				+ " " + SMTableordersources.iSourceID
				+ ", " + SMTableordersources.sSourceDesc
				+ " FROM " + SMTableordersources.TableName
				+ " ORDER BY " + SMTableordersources.sSourceDesc
				;
			try {
				ResultSet rsOrderSources = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sm.getsDBID(), 
						"MySQL", 
						this.toString() + " [1332426013] SQL: " + SQL);
				while (rsOrderSources.next()){
					String sOrderSourceID = Long.toString(rsOrderSources.getLong(SMTableordersources.iSourceID));
					s += "<OPTION";
					if (sOrderSourceID.compareToIgnoreCase(entry.getM_iOrderSourceID()) == 0){
						s += " selected=YES ";
					}
					s += " VALUE=\"" + sOrderSourceID + "\">" 
					+ rsOrderSources.getString(SMTableordersources.sSourceDesc)
					+ "</OPTION>";
				}
				rsOrderSources.close();
			} catch (SQLException e) {
				throw new SQLException("Error loading order sources with SQL: " + SQL + " - " + e.getMessage());
			}

		s += "</SELECT>";
		s += "</TD></TR>";								
				
		s += "<TR>";
		//Close the table:
		s += "</TABLE style = \" title:ENDCustomerSettings; \">\n";
		return s;
	}
	private String createCustomerBillToTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:CustomerBillTo; \">\n";
		
		//The bill-to address:
		s += "<TR>"
			+ "<TD class=\" fieldlabel \"><B>Bill to<FONT COLOR=RED>*</FONT>:</B></TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToName + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToName + "\""
			+ " VALUE=\"" + entry.getM_sBillToName().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToNameLength)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToAddressLine1 + "\""
			+ " VALUE=\"" + entry.getM_sBillToAddressLine1().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToAddressLine1 + "\""
			+ " onchange=\"flagDirty();\""
			//+ " onFocus=\"geolocate()\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToAddressLine1Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToAddressLine2 + "\""
			+ " VALUE=\"" + entry.getM_sBillToAddressLine2().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToAddressLine2 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToAddressLine2Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToAddressLine3 + "\""
			+ " VALUE=\"" + entry.getM_sBillToAddressLine3().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToAddressLine3 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToAddressLine3Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToAddressLine4 + "\""
			+ " VALUE=\"" + entry.getM_sBillToAddressLine4().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToAddressLine4 + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToAddressLine4Length)
			+ ">"
		;
		s += "</TD></TR>";

		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>City<FONT COLOR=RED>*</FONT>:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToCity + "\""
			+ " VALUE=\"" + entry.getM_sBillToCity().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToCity + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToCityLength)
			+ "</TD>"
		;
		
		s += "<TD class=\" fieldlabel \">&emsp;&emsp;&emsp;<B>State<FONT COLOR=RED>*</FONT>:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToState + "\""
			+ " VALUE=\"" + entry.getM_sBillToState().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToState + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToStateLength)
			+ "</TD>"
		;
		s += "</TR>";

		s += "<TR>";
		
		s += "<TD class=\" fieldlabel \"><B>Zip<FONT COLOR=RED>*</FONT>:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToZip + "\""
			+ " VALUE=\"" + entry.getM_sBillToZip().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToZip + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToZipLength)
			+ "</TD>"
		;

		s += "<TD class=\" fieldlabel \"><B>Fax:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToFax + "\""
			+ " VALUE=\"" + entry.getM_sBillToFax().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToFax + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToFaxLength)
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TD class=\" fieldlabel \"><B>Country:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToCountry + "\""
			+ " VALUE=\"" + entry.getM_sBillToCountry().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToCountry + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToCountryLength)
			+ "</TD>"
		;
		
		s += "<TD class=\" fieldlabel \"><B>Phone:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToPhone + "\""
			+ " VALUE=\"" + entry.getM_sBilltoPhone().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToPhone + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToPhoneLength)
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>2nd phone:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramssecondarybilltophone + "\""
			+ " VALUE=\"" + entry.getM_ssecondarybilltophone().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramssecondarybilltophone + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.ssecondarybilltophoneLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Authorized by:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsBillToContact + "\""
			+ " VALUE=\"" + entry.getM_sBilltoContact().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsBillToContact + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sBillToContactLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Email:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsEmailAddress + "\""
			+ " VALUE=\"" + entry.getM_sEmailAddress().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsEmailAddress + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sEmailAddressLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Invoicing contact:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsInvoicingContact + "\""
			+ " VALUE=\"" + entry.getM_sInvoicingContact().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsInvoicingContact + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sInvoicingContactLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Invoicing email:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsInvoicingEmailAddress + "\""
			+ " VALUE=\"" + entry.getM_sInvoicingEmail().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsInvoicingEmailAddress + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sInvoicingEmailLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";

		//Blank field:
		//s += "<TD>&nbsp;</TD<TD>&nbsp</TD>";
		//s += "</TR>";

		//Close the table:
		s += "</TABLE style=\" title:ENDCustomerBillTo; \">\n";
		return s;
	}
	private String createCustomerShipToTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:CustomerShipTo; \">\n";
		
		//Get any ship-to's for this customer:
		String SQL = "SELECT"
			+ " " + SMTablearcustomershiptos.sShipToCode
			+ ", " + SMTablearcustomershiptos.sDescription
			+ " FROM " + SMTablearcustomershiptos.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + entry.getM_sCustomerCode() + "')"
			+ ")"
			+ " ORDER BY " + SMTablearcustomershiptos.sShipToCode
			;
		
		ArrayList<String>arrShipTos = new ArrayList<String>(0);
		ArrayList<String>arrShipToDescriptions = new ArrayList<String>(0);
		try {
			ResultSet rsShipTos = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1332425930] SQL: " + SQL);
			while (rsShipTos.next()){
				arrShipTos.add(rsShipTos.getString(SMTablearcustomershiptos.sShipToCode));
				arrShipToDescriptions.add(arrShipTos.get(arrShipTos.size()-1) 
					+ " - " + rsShipTos.getString(SMTablearterms.sDescription));
			}
			rsShipTos.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading terms - " + e.getMessage());
		}

		//The ship-to address:
		s += "<TR>"
			+ "<TD class=\" fieldlabel \"><B>Ship to:</B></TD>";
		
		String sFirstOption = "** SELECT A SHIP TO **";
		//If there are NO ship to addresses for this customer:
		if (arrShipTos.size() == 0){
			sFirstOption = "** (NO SHIP TO's) **";
		//If there ARE ship to addresses for this customer:
		}else{
			sFirstOption = "** SELECT A SHIP TO **";
		}
		s += "<TD class=\"fieldcontrol\" COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<SELECT ID= \"" + SMOrderHeader.ParamsShipToCode + "\""
			+ " onchange=\"shiptoChange(this);\""
			+ " NAME=\"" + SMOrderHeader.ParamsShipToCode + "\"" + ">";
		s += "<OPTION VALUE=\"" + "" + "\">" + sFirstOption + "</OPTION>";
		for (int i = 0; i < arrShipTos.size(); i++){
			s += "<OPTION";
			String sCurrentShipTo = arrShipTos.get(i);
			if (sCurrentShipTo != null){
				if (entry.getM_sShipToCode().compareToIgnoreCase(sCurrentShipTo) == 0){
					s += " selected=YES ";
				}
			}
			s += " VALUE=\"" + arrShipTos.get(i).trim() + "\">" + arrShipToDescriptions.get(i).trim() + "</OPTION>";
		}
		s += "</SELECT>";
		s += "</TD></TR>";
		
		s += "<TR><TD>"
				+ "</TD><TD class=\" fieldcontrol \">";
				s += "<input type=button value=\"Use bill-to\"\n" 
						+ "    onClick=\"if(confirm('Copy bill-to address over the ship-to?'))\n"
						//+ "    alert('Bill to copied');\n"
						+ "    copyBillTo();\n"
						+ "     \">\n"
					;
		s += "</TD></TR>";
		//The ship-to name:		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">Name<FONT COLOR=RED>*</FONT>:</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToName + "\""
			+ " VALUE=\"" + entry.getM_sShipToName().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToName + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToNameLength)
			+ ">"
		;
		s += "</TD></TR>";

		//The ship-to address:
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">";
	//	s += "<input type=button value=\"Use bill-to\"\n" 
	//		+ "    onClick=\"if(confirm('Copy bill-to address over the ship-to?'))\n"
	//		//+ "    alert('Bill to copied');\n"
	//		+ "    copyBillTo();\n"
	//		+ "     \">\n"
		;
		s += "</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToAddress1 + "\""
			+ " VALUE=\"" + entry.getM_sShipToAddress1().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToAddress1 + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToAddress1Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToAddress2 + "\""
			+ " VALUE=\"" + entry.getM_sShipToAddress2().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToAddress2 + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToAddress2Length)
			+ ">"
		;
		s += "</TD></TR>";		
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">&nbsp;</TD>"
			+ "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToAddress3 + "\""
			+ " VALUE=\"" + entry.getM_sShipToAddress3().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToAddress3 + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToAddress3Length)
			+ ">"
		;
		s += "</TD></TR>";
		
		s += "<TR>"
			+ "<TD class=\" fieldlabel \">";
//		s += "<input type=button value=\"Test map link\"\n" 
//			+ " onClick=\"testMapLink();\"\n"
//			+ ">\n"
		;
		s += "</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN=" 
			+ Integer.toString(iNumberOfColumns -1) + ">";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToAddress4 + "\""
			+ " VALUE=\"" + entry.getM_sShipToAddress4().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToAddress4 + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToAddress4Length)
			+ ">"
		;
		s += "</TD></TR>";
		
	
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>City:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToCity + "\""
			+ " VALUE=\"" + entry.getM_sShipToCity().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToCity + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToCityLength)
			+ "</TD>"
		;
		
		s += "<TD class=\" fieldlabel \"><B>State:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToState + "\""
			+ " VALUE=\"" + entry.getM_sShipToState().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToState + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToStateLength)
			+ "</TD>"
		;
		s += "</TR>";

		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Zip<FONT COLOR=RED>**</FONT>:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToZip + "\""
			+ " VALUE=\"" + entry.getM_sShipToZip().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToZip + "\""
			+ " onchange=\"flagShipToDirty();\""
			+ " SIZE=" + "11"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToZipLength)
			+ "</TD>"
		;

		s += "<TD class=\" fieldlabel \"></TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "</TD>"
		;

		s += "<TR>";
		s += "<TD></TD>";
		s += "<TD class=\" fieldcontrol \">";
		s += "<input type=button value=\"Test map link\"\n" 
		+ " onClick=\"testMapLink();\"\n"
		+ ">\n"
	    ;
		s += "</TD>";
		s += "<TD class=\" fieldlabel \"><B>Fax:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToFax + "\""
			+ " VALUE=\"" + entry.getM_sShipToFax().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToFax + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToFaxLength)	
			+ "</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Phone:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToPhone + "\""
			+ " VALUE=\"" + entry.getM_sShiptoPhone().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToPhone + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToPhoneLength)
			+ ">"
			+ "</TD>"
		;
		s += "<TD class=\" fieldlabel \">&nbsp;&nbsp;<B>2nd phone:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramssecondaryshiptophone + "\""
			+ " VALUE=\"" + entry.getM_ssecondaryshiptophone().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramssecondaryshiptophone + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + PHONE_NUMBER_FIELD_WIDTH
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.ssecondaryshiptophoneLength)
			+ ">"
			+ "</TD>"
		;
		
		s += "</TR>";
		
		

		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Contact:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsShipToContact + "\""
			+ " VALUE=\"" + entry.getM_sShiptoContact().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamsShipToContact + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sShipToContactLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Email:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " 
			+ Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramsshiptoemail + "\""
			+ " VALUE=\"" + entry.getM_sShipToEmail().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramsshiptoemail + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderheaders.sshiptoemailLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style=\" title:ENDCustomerShipTo; \">\n";
		return s;
	}
	private String createOrderSettings2Table(
			SMMasterEditEntry sm, 
			SMOrderHeader entry) throws SQLException{
		String s = "";
		int iNumberOfColumns = 4;
		int iTextAreaRows = 2;
		int iTextAreaCols = 35;
		
		if (sm == null){
			throw new SQLException ("Error [1419870477] - sm passed to createOrderSettings2Table is NULL.");
		}
		if (entry == null){
			throw new SQLException ("Error [1419870478] - entry passed to createOrderSettings2Table is NULL.");
		}
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderSettings; \">\n";
		
		//Wage scale check box
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Wage scale?</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=CHECKBOX ";
		if (entry.getM_sSpecialWageRate().compareToIgnoreCase("T") == 0){
			s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += " NAME=\"" + SMOrderHeader.ParamsSpecialWageRate + "\""
			+ " id = \"" + SMOrderHeader.ParamsSpecialWageRate + "\""
			+ " onchange=\"flagDirty();\""
			+ " width=0.25>"
			+ "</TD>"
		;
		s += "<TD>&nbsp;</TD>";
		s += "</TR>";
		
		//Carpenter rate
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Carpenter rate:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramscarpenterrate + "\""
			+ " VALUE=\"" + entry.getM_scarpenterrate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramscarpenterrate + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= " + Integer.toString(SMTableorderheaders.scarpenterrateLength)
			+ ">"
			+ "</TD>";
		
		s += "<TD>&nbsp;</TD>";
		s += "</TR>";
		
		//Laborer rate
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Laborer rate:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramslaborerrate + "\""
			+ " VALUE=\"" + entry.getM_slaborerrate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramslaborerrate + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= " + Integer.toString(SMTableorderheaders.slaborerrateLength)
			+ ">"
			+ "</TD>"
		;
		s += "<TD>&nbsp;</TD>";
		s += "</TR>";

		//Electrician rate
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Electrician rate:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramselectricianrate + "\""
			+ " VALUE=\"" + entry.getM_selectricianrate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramselectricianrate + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= " + Integer.toString(SMTableorderheaders.selectricianrateLength)
			+ ">"
			+ "</TD>"
		;
		
		s += "<TD>&nbsp;</TD>";
		s += "</TR>";
		//Wage scale notes:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \" ><B>Wage scale notes:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + ">"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.Paramswagescalenotes + "\""
			+ " rows=\"" + Integer.toString(iTextAreaRows) + "\""
			+ " cols=\"" + Integer.toString(iTextAreaCols) + "\""
			+ " id = \"" + SMOrderHeader.Paramswagescalenotes + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getM_swagescalenotes().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "<BR></TD>"
		;
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style=\" title:ENDOrderSettings; \">\n";
		return s;
	}
	private String createProjectFieldsTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry) throws SQLException{
		String s = "";

		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:ProjectFields; \">\n";
		
		s += "<TR>"
				+ "<TD><BR></TD><TD></TD>"
				+ "<TD></TD><TD></TD>"
		+ "</TR>";
		
		//Total contract amount
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Base contract:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Parambdtotalcontractamount + "\""
			+ " VALUE=\"" + entry.getM_bdtotalcontractamount().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Parambdtotalcontractamount + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= 14"
			+ ">"
			+ "</TD>"
		;
		
		//Total MU
		s += "<TD class=\" fieldlabel \"><B>Total MU:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Parambdtotalmarkup + "\""
			+ " VALUE=\"" + entry.getM_bdtotalmarkup().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Parambdtotalmarkup + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= 14"
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Truck days
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Truck days:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Parambdtruckdays + "\""
			+ " VALUE=\"" + entry.getM_bdtruckdays().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Parambdtruckdays + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= 14"
			+ ">"
			+ "</TD>"
		;

		
		//Warranty expiration
		s += "<TD class=\" fieldlabel \"><B> Warranty expiration:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >" 
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramdatwarrantyexpiration + "\""
			+ " VALUE=\"" + entry.getM_datwarrantyexpiration().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramdatwarrantyexpiration + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=" + "28"
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"width: " + ".45" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(SMOrderHeader.Paramdatwarrantyexpiration, getServletContext())
			+ "</TD>"
			;
		s += "</TR>";	
		
		//Estimated hrs.
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Estimated hrs:&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamdEstimatedHour + "\""
			+ " VALUE=\"" + entry.getM_dEstimatedHour().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.ParamdEstimatedHour + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".90" + " in; height: 0.25in\""
			+ " MAXLENGTH= 10"
			+ ">"
			+ "</TD>";
		;
		
		//Sales Lead ID
		s += "<TD class=\" fieldlabel \"><B>" + SMBidEntry.ParamObjectName + " ID:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramlbidid + "\""
			+ " VALUE=\"" + entry.getsBidID().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMOrderHeader.Paramlbidid + "\""
			+ " onchange=\"flagDirty();\""
			+ " STYLE=\"width: " + ".45" + " in; height: 0.25in\""
			+ " MAXLENGTH= " + "9"
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";
		
		//Quote description
		s += "<TR>";
		s += "<TD class=\" fieldlabel \"><B>Quote description:</B>&nbsp;</TD>";
		s += "<TD class=\" fieldcontrol \" COLSPAN=3>"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.Paramsquotedescription + "\""
			+ " id = \"" + SMOrderHeader.Paramsquotedescription + "\""
			+ " onchange=\"flagDirty();\""
			+ " rows=\"" + "2" + "\""
			+ " STYLE=\"width: " + "90%;\""
			+ " MAXLENGTH= " + SMTableorderheaders.squotedescriptionLength
			+ ">" + entry.getsQuoteDescription().replace("\"", "&quot;") + "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>"
				+ "<TD><BR></TD><TD></TD>"
				+ "<TD></TD><TD></TD>"
		+ "</TR>";
		//Close the table:
		s += "</TABLE style=\" title:ENDOProjectFields; \">\n";
		return s;
	}

	private String createOrderMemosTable(
			SMMasterEditEntry sm, 
			SMOrderHeader entry,
			boolean bAddingNewEntry,
			boolean bUseGoogleDrivePicker) throws SQLException{
		String s = "";
		int iRows = 3;
		int iCols = 55;
		int iNumberOfColumns = 2;
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderTotals; background-color: "
			+ ORDERMEMOS_BG_COLOR + "; \" width=100% >\n";		
		
		//Add a field for the Google Docs Link and buttons for google web apps
		String sRenameFolderButton = "";
		String sCreateAndUploadButton = "";

		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDriveOrderFolders, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			&& !sm.getAddingNewEntryFlag()){
			
			boolean bGDocLinkExists = checkForGDocLink(getServletContext(), sm.getsDBID(), entry.getM_strimmedordernumber(), sm.getUserID(), sm.getFullUserName());

			if(bGDocLinkExists) {
				sRenameFolderButton = createRenameFolderButton();
			}
			sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
		}
		
		//TJR - temporary:
		//sCreateFolderButton = "";
		//sUploadFolderButton = "";
		
		s += "<TD COLSPAN=" + iNumberOfColumns + " class=\" fieldcontrol \" >"
				+ "<B>Document folder link:</B>"
				+ sRenameFolderButton + "&nbsp;" + sCreateAndUploadButton
				+ "<BR><INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.Paramsgdoclink + "\""
				+ " VALUE=\"" + entry.getM_sGDocLink().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMOrderHeader.Paramsgdoclink + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "100"
				+ " MAXLENGTH=" + "254"
				+ ">"
				+ "</TD>"
			;
		
		//Internal notes
		s += "<TR>";
		s += "<TD class=\" fieldheading \" ><B>Internal notes</B>&nbsp;</TD>";
		s += "<TD class=\" fieldheading \" ><B>Work order notes</B>&nbsp;</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.ParammInternalComments + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " id = \"" + SMOrderHeader.ParammInternalComments + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getM_mInternalComments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		
		//Work order notes
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.ParammTicketComments + "\""
			+ " ID=\"" + SMOrderHeader.ParammTicketComments + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getM_sTicketComments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Invoice notes
		s += "<TR>";
		s += "<TD class=\" fieldheading \" ><B>Invoice notes (appears on invoice)</B>&nbsp;</TD>";
		s += "<TD class=\" fieldheading \" ><B>Directions</B>&nbsp;</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.ParammInvoiceComments + "\""
			+ " ID=\"" + SMOrderHeader.ParammInvoiceComments + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getM_mInvoiceComments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		
		//Directions
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.ParammDirections + "\""
			+ " rows=\"" + Integer.toString(iRows) + "\""
			+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getM_sDirections().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD class=\" fieldheading \"><B>Invoicing instructions</B>&nbsp;</TD>";
		s += "</TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMOrderHeader.ParamsInvoicingNotes + "\""
			+ " rows = \"" + Integer.toString(iRows) + "\""
			+ " cols = \"" + Integer.toString(iCols) + "\""
			+ " id = \"" + SMOrderHeader.ParamsInvoicingNotes + "\""
			+ " onchange=\"flagDirty();\""
		
			+ ">" + entry.getM_sInvoicingNotes().replace("\"", "&quot;") 
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Convenience phrases:
		s += "<TR><TD COLSPAN = " + Integer.toString(iNumberOfColumns) + ">";
		
		s +=
			"\n<form name=cpform>\n"
			+ "<input type=\"checkbox\" id=\"cbChoices\" onclick=\"exposeConveniencePhraseList()\">"
			+ "<B>Show convenience phrases<B>&nbsp;\n"
		
			+ "<div id= \"CPINSERTLABEL\" style=\"display:none;\"><B><I>Insert selected phrases into:"
			+ "</I></B>&nbsp;"

			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTOINTERNALNOTES_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTOINTERNALNOTES_ID + "\""
			+ ">"
			+ INSERTCPSINTOINTERNALNOTES_LABEL

			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTOTICKETNOTES_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTOTICKETNOTES_ID + "\""
			+ ">"
			+ INSERTCPSINTOTICKETNOTES_LABEL
			
			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTOINVOICENOTES_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTOINVOICENOTES_ID + "\""
			+ ">"
			+ INSERTCPSINTOINVOICENOTES_LABEL

			+ "</div>"
			
			+ "</form>\n"
		;
			
		s += "<div id=\"ScrollCB\" style=\"height:350;width:100%;background-color:" 
				+ CONVENIENCEPHRASES_BG_COLOR + ";overflow:auto;border:1px solid blue;display:none\">\n"
		;
		
//		s += "<div style = \"width:100%;\">";
//		s += "<SELECT NAME=\"" + CONVENIENCEPHRASE_SELECT_NAME + "\"" + " SIZE=15>";
		
		String SQL = "SELECT * FROM " + SMTableconveniencephrases.TableName
			+ " ORDER BY " + SMTableconveniencephrases.lPhraseID
		;
		ResultSet rscps = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332426095] SQL: " + SQL);
		
		while (rscps.next()){
			//This creates a list of checkboxes:
			String sCPText = clsStringFunctions.filter(rscps.getString(SMTableconveniencephrases.mPhraseText));
			s += "<input type=\"hidden\" id=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableconveniencephrases.lPhraseID)) 
				+ "\" name=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableconveniencephrases.lPhraseID)) + "\""
				
				+ " value=\"" + sCPText 
				+ "\">" 
				+ "<label name=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableconveniencephrases.lPhraseID)) + "\" for=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableconveniencephrases.lPhraseID)) + "\""
				+ " onclick=\"insertConveniencePhrase('" + sCPText + "');\""
				+ " onmouseover=colorChangeRed(this) onmouseout=colorChangeBack(this)>" 
				+ clsStringFunctions.filter(rscps.getString(SMTableconveniencephrases.mPhraseText))
				+ "</label>"
				+ "<br>\n"
			;
			
		}
		rscps.close();
		//s += "</SELECT>";
		s += "</div>";
		s += "</TD></TR>";

		//Close the table:
			
		s += "</TABLE style=\" title:ENDOrderMemos; \">\n";
		return s;
	}
	
	private String createRenameFolderButton(){
		return "<button type=\"button\""
			+ " value=\"" + RENAME_FOLDER_BUTTON_LABEL + "\""
			+ " name=\"" + RENAME_FOLDER_BUTTON_LABEL + "\""
			+ " onClick=\"renamefolder();\">"
			+ RENAME_FOLDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createAndUploadFolderButton(boolean bUseGoogleDrivePicker){
		String sOnClickFunction = "createanduploadfolder()";
		if(bUseGoogleDrivePicker) {
			sOnClickFunction = "loadPicker()";
		}
		
		return "<button type=\"button\""
			+ " value=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " onClick=\"" + sOnClickFunction + "\">"
			+ CREATE_UPLOAD_FOLDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createOrderCommandsTable(
			SMMasterEditEntry sm, 
			String sObjectName,
			SMOrderHeader order,
			boolean bIncludePrintServiceWorkOrder,
			boolean bIncludePrintInstallationWorkOrder,
			String sDBID,
			String sUserID
			){
		String s = "";
		
		s += "<TR><TD>";
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ ORDERCOMMANDS_BG_COLOR + "; \" width=100% >\n";
				//Place the 'update' button here:
				if (sm.getAddingNewEntryFlag()){
					s += "<TR><TD style = \"text-align: left; \" >"
						+ "<B><I>Order commands:</I></B>&nbsp;"
						
						+ "<button type=\"button\"";
					if (sObjectName.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(
							SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
						s += " value=\"" + QUOTECREATE_BUTTON_LABEL + "\""
						+ " name=\"" + QUOTECREATE_BUTTON_LABEL + "\""
						+ " onClick=\"saveOrder();\">"
						+ QUOTECREATE_BUTTON_LABEL
						+ "</button>\n";
					}else{
						s += " value=\"" + ORDERCREATE_BUTTON_LABEL + "\""
						+ " name=\"" + ORDERCREATE_BUTTON_LABEL + "\""
						+ " onClick=\"saveOrder();\">"
						+ ORDERCREATE_BUTTON_LABEL
						+ "</button>\n";
					}
					s += "</TD></TR>";
				}else{
					s += "<TR><TD style = \"text-align: left; \" >"
						+ "<B><I>Go to:</I></B>"
						
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
						
					if (sObjectName.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(
							SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
						//Print service work order button
						if (bIncludePrintServiceWorkOrder){
							s += "<button type=\"button\""
							+ " value=\"" + ORDERSERVICEWO_BUTTON_LABEL + "\""
							+ " name=\"" + ORDERSERVICEWO_BUTTON_LABEL + "\""
							+ " onClick=\"printServiceWorkOrder();\">"
							+ ORDERSERVICEWO_BUTTON_LABEL
							+ "</button>\n";
							
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
						//print installation work order button
						if (bIncludePrintInstallationWorkOrder){
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
							getServletContext(),
							sDBID,
							(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
						if (bAllowCreateInvoice){
							s += "<button type=\"button\""
							+ " value=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
							+ " name=\"" + ORDERINVOICE_BUTTON_LABEL + "\""
							+ " onClick=\"createInvoice();\">"
							+ ORDERINVOICE_BUTTON_LABEL
							+ "</button>\n"
							;
						}
					}
					//TJR - 8/23/2013 - added the ability to see and edit proposals, even if it's not a quote:
					//if (sObjectName.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(
					//	SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
					boolean bAllowProposal = SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMEditProposals, 
						sUserID, 
						getServletContext(),
						sDBID,
						(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
					if (bAllowProposal){
						s += "<button type=\"button\""
							+ " value=\"" + PROPOSAL_BUTTON_LABEL + "\""
							+ " name=\"" + PROPOSAL_BUTTON_LABEL + "\""
							+ " onClick=\"proposal();return false;\">"
							+ PROPOSAL_BUTTON_LABEL
							+ "</button>\n";
						;
					}
					//}
					boolean bAllowCloneOrder = SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.SMCloneOrder, 
							sUserID, 
							getServletContext(),
							sDBID,
							(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
					boolean bProposalExists = checkForProposal(getServletContext(), sm.getsDBID(), order.getM_strimmedordernumber());
					if (bAllowCloneOrder){
						s += "<button type=\"button\""
							+ " value=\"" + ORDERCLONE_BUTTON_LABEL + "\""
							+ " name=\"" + ORDERCLONE_BUTTON_LABEL + "\""
							+ " onClick=\"cloneOrder();\">"
							+ ORDERCLONE_BUTTON_LABEL
							+ "</button>\n"
							+ " <INPUT TYPE=CHECKBOX NAME='" + CLONEDETAILS_CHECKBOX + "'>Clone details"
						;
						if (bProposalExists){
							s += " <INPUT TYPE=CHECKBOX NAME='" + CLONEPROPOSAL_CHECKBOX + "'>Clone proposal" + "&nbsp";
						}
					}
					//If it's an order, then the customer change button depends on the permission - but if it's a quote,
					// then anyone can change the customer:
					boolean bAllowCustomerChange = SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.SMChangeCustomerOnOrders, 
							sUserID, 
							getServletContext(),
							sDBID,
							(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
					if (
						(sObjectName.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0)
						|| bAllowCustomerChange
					){
						s += "<button type=\"button\""
							+ " value=\"" + CHANGECUSTOMER_BUTTON_LABEL + "\""
							+ " name=\"" + CHANGECUSTOMER_BUTTON_LABEL + "\""
							+ " onClick=\"changeCustomer();\">"
							+ CHANGECUSTOMER_BUTTON_LABEL
							+ "</button>\n"
						;
					}
				}
		s += "</TD></TR>";
		//Close the table:
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		s += "</TD></TR>";
		return s;
	}
	private String sCommandScripts(SMOrderHeader order, SMMasterEditEntry smmaster) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";
		
		//s += sPlacesAPIScript();
		s += "<!--\n";
		s += "String.prototype.trim = function () {\n";
		s += "    return this.replace(/^" + "\\" + "s*/, \"\").replace(/" + "\\" + "s*$/, \"\");\n";
		//return this.replace(/^\s*/, "").replace(/\s*$/, "");
		s += "}\n";
		
		//Here we have to build javascript arrays of the ship to locations if the customer has any:
		int iCounter = 0;
		String sshiptonames = "";
		String sshiptoaddress1s = "";
		String sshiptoaddress2s = "";
		String sshiptoaddress3s = "";
		String sshiptoaddress4s = "";
		String sshiptocities = "";
		String sshiptostates = "";
		String sshiptopostalcodes = "";
		String sshiptofaxes = "";
		String sshiptocontacts = "";
		String sshiptophones = "";
		String sshipto2ndphones = "";
		
		String SQL = "SELECT"
			+ " " + SMTablearcustomershiptos.sAddressLine1
			+ ", " + SMTablearcustomershiptos.sAddressLine2
			+ ", " + SMTablearcustomershiptos.sAddressLine3
			+ ", " + SMTablearcustomershiptos.sAddressLine4
			+ ", " + SMTablearcustomershiptos.sCity
			+ ", " + SMTablearcustomershiptos.sContactName
			+ ", " + SMTablearcustomershiptos.sCountry
			+ ", " + SMTablearcustomershiptos.sDescription
			+ ", " + SMTablearcustomershiptos.sFaxNumber
			+ ", " + SMTablearcustomershiptos.sPhoneNumber
			+ ", " + SMTablearcustomershiptos.sPostalCode
			+ ", " + SMTablearcustomershiptos.sShipToCode
			+ ", " + SMTablearcustomershiptos.sState
			+ " FROM " + SMTablearcustomershiptos.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + order.getM_sCustomerCode().trim() + "')"
			+ ")"
			+ " ORDER BY " + SMTablearcustomershiptos.sShipToCode
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmaster.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332425094] SQL: " + SQL 
			);
			while (rs.next()){
				iCounter++;
				sshiptonames += "sshiptonames[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
					+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sDescription).trim().replace("\"", "'") + "\";\n";
				sshiptoaddress1s += "sshiptoaddress1s[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sAddressLine1).trim().replace("\"", "'") + "\";\n";
				sshiptoaddress2s += "sshiptoaddress2s[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sAddressLine2).trim().replace("\"", "'") + "\";\n";
				sshiptoaddress3s += "sshiptoaddress3s[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sAddressLine3).trim().replace("\"", "'") + "\";\n";
				sshiptoaddress4s += "sshiptoaddress4s[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sAddressLine4).trim().replace("\"", "'") + "\";\n";
				sshiptocities += "sshiptocities[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sCity).trim().replace("\"", "'") + "\";\n";
				sshiptostates += "sshiptostates[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sState).trim().replace("\"", "'") + "\";\n";
				sshiptopostalcodes += "sshiptopostalcodes[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sPostalCode).trim().replace("\"", "'") + "\";\n";
				sshiptofaxes += "sshiptofaxes[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sFaxNumber).trim().replace("\"", "'") + "\";\n";
				sshiptocontacts += "sshiptocontacts[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sContactName).trim().replace("\"", "'") + "\";\n";
				sshiptophones += "sshiptophones[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + rs.getString(SMTablearcustomershiptos.sPhoneNumber).trim().replace("\"", "'") + "\";\n";
				sshipto2ndphones += "sshipto2ndphones[\"" + rs.getString(SMTablearcustomershiptos.sShipToCode).trim() 
				+ "\"] = \"" + "" + "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading ship-to locations for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var sshiptonames = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptonames + "\n";
			
			s += "var sshiptoaddress1s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptoaddress1s + "\n";
			
			s += "var sshiptoaddress2s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptoaddress2s + "\n";
			
			s += "var sshiptoaddress3s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptoaddress3s + "\n";
			
			s += "var sshiptoaddress4s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptoaddress4s + "\n";
			
			s += "var sshiptocities = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptocities + "\n";
			
			s += "var sshiptostates = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptostates + "\n";
			
			s += "var sshiptopostalcodes = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptopostalcodes + "\n";
			
			s += "var sshiptofaxes = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptofaxes + "\n";
			
			s += "var sshiptocontacts = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptocontacts + "\n";
			
			s += "var sshiptophones = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshiptophones + "\n";
			
			s += "var sshipto2ndphones = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sshipto2ndphones + "\n";
			
		}
		
		s += "\n";

		s += "function shiptoChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != ''){\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToName + "\"].value = sshiptonames[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress1 + "\"].value = sshiptoaddress1s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress2 + "\"].value = sshiptoaddress2s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress3 + "\"].value = sshiptoaddress3s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress4 + "\"].value = sshiptoaddress4s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToCity + "\"].value = sshiptocities[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToState + "\"].value = sshiptostates[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToZip + "\"].value = sshiptopostalcodes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToFax + "\"].value = sshiptofaxes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToContact + "\"].value = sshiptocontacts[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToPhone + "\"].value = sshiptophones[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.Paramssecondaryshiptophone + "\"].value = sshipto2ndphones[which];\n"
		+ "    }\n"
		+ "    flagShipToDirty();\n"
		+ "}\n\n"; 
		
		s += "function copyBillTo() {\n" 
			//First, set the flag to indicate that we are updating the location:
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToName 
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToName 
				+ "\"].value;\n"
				
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress1 
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToAddressLine1
				+ "\"].value;\n"
				
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress2 
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToAddressLine2
				+ "\"].value;\n"
				
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress3 
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToAddressLine3
				+ "\"].value;\n"
				
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToAddress4
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToAddressLine4
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToCity
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToCity
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToState
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToState
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToZip
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToZip
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToFax
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToFax
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToContact
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToContact
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsShipToPhone
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsBillToPhone
				+ "\"].value;\n"

				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.Paramssecondaryshiptophone
				+ "\"].value = document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.Paramssecondarybilltophone
				+ "\"].value;\n"

				+ "    flagDirty();\n"
			+ "}\n"; 
		
		//Build arrays to set the default item category here:
		iCounter = 0;
		String sdefaultitemcategories = "";
		
		SQL = "SELECT"
			+ " " + SMTabledefaultitemcategories.DefaultItemCategory
			+ ", " + SMTabledefaultitemcategories.LocationCode
			+ ", " + SMTabledefaultitemcategories.ServiceTypeCode
			+ " FROM " + SMTabledefaultitemcategories.TableName
			+ " ORDER BY " + SMTabledefaultitemcategories.DefaultItemCategory
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmaster.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332425177] SQL: " + SQL
			);
			while (rs.next()){
				iCounter++;
				sdefaultitemcategories += "sdefaultitemcategories[\"" 
					+ rs.getString(SMTabledefaultitemcategories.LocationCode).trim()
					+ rs.getString(SMTabledefaultitemcategories.ServiceTypeCode).trim()	+ "\"] = \"" 
					+ rs.getString(SMTabledefaultitemcategories.DefaultItemCategory).trim().replace("\"", "'") 
					+ "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading default item categories for javascript - " + e.getMessage());
		}
		
		//Create the array, if there is one:
		if (iCounter > 0){
			s += "var sdefaultitemcategories = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sdefaultitemcategories + "\n";
		}
		s += "\n";
		
		s += "function setDefaultItemCategory(selectObj) {\n"
			// get the index of the selected option 
			+ "    var locationidx = " + SMOrderHeader.ParamsLocation + ".selectedIndex;\n"
			+ "    var servicetypeidx = " + SMOrderHeader.ParamsServiceTypeCode + ".selectedIndex;\n"
			// get the value of the selected option 
			+ "    var location = " + SMOrderHeader.ParamsLocation + ".options[locationidx].value;\n"
			+ "    var servicetype = " + SMOrderHeader.ParamsServiceTypeCode + ".options[servicetypeidx].value;\n"
			+ "    var dic_selector = location + servicetype;\n"
			// use the selected option value to retrieve the ship to fields from the ship to arrays:
			+ "    if (dic_selector != ''){\n"
			+ "        document.forms[\"MAINFORM\"].elements[\"" + SMOrderHeader.ParamsDefaultItemCategory 
				+ "\"].value = sdefaultitemcategories[dic_selector];\n"
			+ "    }\n"
			+ "    flagDirty();\n"
			+ "}\n\n";
		
		s += "function exposeConveniencePhraseList() {\n" 
			+ "    var status = document.getElementById('cbChoices').checked;\n" 
			+ "    if (status == true) {\n"
			+ "        document.getElementById('ScrollCB').style.display = \"block\";\n"
			+ "        document.getElementById('" + "CPINSERTLABEL" + "').style.display = \"inline\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOINTERNALNOTES_ID + "').style.display = \"inline\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOINVOICENOTES_ID + "').style.display = \"inline\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOTICKETNOTES_ID + "').style.display = \"inline\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOINTERNALNOTES_ID + "').checked = true;"
			+ "    } else {\n"
			+ "        document.getElementById('ScrollCB').style.display = 'none';\n"
			+ "        document.getElementById('" + "CPINSERTLABEL" + "').style.display = \"none\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOINTERNALNOTES_ID + "').style.display = \"none\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOINVOICENOTES_ID + "').style.display = \"none\";\n"
			+ "        document.getElementById('" + INSERTCPSINTOTICKETNOTES_ID + "').style.display = \"none\";\n"
			+ "    }\n" 
			+ "}\n"
		;
		
		s += "function insertConveniencePhrase(sCPText) {\n" 			
			+ "    if (document.getElementById('" + INSERTCPSINTOINTERNALNOTES_ID + "').checked){\n"
			+ "        textarea = document.getElementById('" + SMOrderHeader.ParammInternalComments + "');\n"
			+ "    }\n"

			+ "    if (document.getElementById('" + INSERTCPSINTOINVOICENOTES_ID + "').checked){\n"
			+ "        textarea = document.getElementById('" + SMOrderHeader.ParammInvoiceComments + "');\n"
			+ "    }\n"
			
			+ "    if (document.getElementById('" + INSERTCPSINTOTICKETNOTES_ID + "').checked){\n"
			+ "        textarea = document.getElementById('" + SMOrderHeader.ParammTicketComments + "');\n"
			+ "    }\n"

			+ "    var caretPos = textarea.selectionStart;\n"
			+ "    var text = textarea.value;\n"
			+ "    var first = text.substring(0, caretPos);\n"
			+ "    var second = text.substring(caretPos, text.length);\n"
			+ "    var insertionString = sCPText + '\\n';\n"
			+ "    var newString = first + insertionString + second;\n"
			+ "    textarea.value = newString;\n"
			+ "    var newPos = first.length + insertionString.length;\n"
			+ "    textarea.setSelectionRange(newPos, newPos);\n"
			+ "    textarea.scrollTop = textarea.scrollHeight;"
			//+ "    clickedCheckBoxObj.checked = false;\n"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function validateForm(){\n"
			// check the required option lists: 
			+ "    var sDiscountLevel = document.getElementById(\"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\").value;\n"
			+ "    if (sDiscountLevel==null || sDiscountLevel==\"\"){\n"
			+ "        alert(\"You must select a Price Level.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sTerms = document.getElementById(\"" + SMOrderHeader.ParamsTerms + "\").value;\n"
			+ "    if (sTerms==null || sTerms==\"\"){\n"
			+ "        alert(\"You must select the appropriate terms.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsTerms + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sTaxid = document.getElementById(\"" + SMOrderHeader.Paramitaxid + "\").value;\n"
			+ "    if (sTaxid==null || sTaxid==\"\"){\n"
			+ "        alert(\"You must select the appropriate Tax Group.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Paramitaxid + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sDiscountLevel = document.getElementById(\"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\").value;\n"
			+ "    if (sDiscountLevel==null || sDiscountLevel==\"\"){\n"
			+ "        alert(\"You must select a sales group.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamiCustomerDiscountLevel + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var ssalesgroup = document.getElementById(\"" + SMOrderHeader.ParamiSalesGroup + "\").value;\n"
			+ "    if (ssalesgroup==null || ssalesgroup==\"0\"){\n"
			+ "        alert(\"You must select a sales group.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamiSalesGroup + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sservicetype = document.getElementById(\"" + SMOrderHeader.ParamsServiceTypeCode + "\").value;\n"
			+ "    if (sservicetype==null || sservicetype==\"\"){\n"
			+ "        alert(\"You must select a service type.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsServiceTypeCode + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var slocation = document.getElementById(\"" + SMOrderHeader.ParamsLocation + "\").value;\n"
			+ "    if (slocation==null || slocation==\"\"){\n"
			+ "        alert(\"You must select a location.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsLocation + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sordertype = document.getElementById(\"" + SMOrderHeader.ParamiOrderType + "\").value;\n"
			+ "    if (sordertype==null || sordertype==\"\"){\n"
			+ "        alert(\"You must select an order type.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamiOrderType + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sordersource = document.getElementById(\"" + SMOrderHeader.ParamiOrderSourceID + "\").value;\n"
			+ "    if (sordersource==null || sordersource==\"\"){\n"
			+ "        alert(\"You must select a marketing source.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamiOrderSourceID + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"

			//check the required text fields:
			+ "    var sbilltoname = document.getElementById(\"" + SMOrderHeader.ParamsBillToName + "\").value;\n"
			+ "    if (sbilltoname==null || sbilltoname==\"\"){\n"
			+ "        alert(\"Bill-to name cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsBillToName + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sbilltocity = document.getElementById(\"" + SMOrderHeader.ParamsBillToCity + "\").value;\n"
			+ "    if (sbilltocity==null || sbilltocity==\"\"){\n"
			+ "        alert(\"Bill-to city cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsBillToCity + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sbilltostate = document.getElementById(\"" + SMOrderHeader.ParamsBillToState + "\").value;\n"
			+ "    if (sbilltostate==null || sbilltostate==\"\"){\n"
			+ "        alert(\"Bill-to state cannot be blank.\");\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sbilltozip = document.getElementById(\"" + SMOrderHeader.ParamsBillToZip + "\").value;\n"
			+ "    if (sbilltozip==null || sbilltozip==\"\"){\n"
			+ "        alert(\"Bill-to zip cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsBillToZip + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sshiptoname = document.getElementById(\"" + SMOrderHeader.ParamsShipToName + "\").value;\n"
			+ "    if (sshiptoname==null || sshiptoname==\"\"){\n"
			+ "        alert(\"Ship-to name cannot be blank.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamsShipToName + "\").focus();\n"
			+ "        return false;\n"
			+ "    }\n"

			//check dates:
			+ "    var sorderdate = document.getElementById(\"" + SMOrderHeader.ParamdatOrderDate + "\").value;\n"
			+ "    if (isDate(sorderdate) == false){\n"
			+ "        alert(\"Order date '\" + sorderdate + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatOrderDate + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatOrderDate + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var sexpectedshipdate = document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").value;\n"
			+ "    if ((sexpectedshipdate != '00/00/0000') && (isDate(sexpectedshipdate) == false)){\n"
			+ "        alert(\"Expected ship date '\" + sexpectedshipdate + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    var swarrantyexpirationdate = document.getElementById(\"" + SMOrderHeader.Paramdatwarrantyexpiration + "\").value;\n"
			+ "    if ((swarrantyexpirationdate != '00/00/0000') && (isDate(swarrantyexpirationdate) == false)){\n"
			+ "        alert(\"Warranty expiration date '\" + swarrantyexpirationdate + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Paramdatwarrantyexpiration + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Paramdatwarrantyexpiration + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"

			//Validate number fields:
			+ "    var sestimatedhours = document.getElementById(\"" 
				+ SMOrderHeader.ParamdEstimatedHour + "\").value;\n"
			+ "    if (isNumeric(sestimatedhours) == false){\n"
			+ "        alert(\"Estimated hours '\" + sestimatedhours + \"' are invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdEstimatedHour + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.ParamdEstimatedHour + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			
			+ "    var scontractamt = document.getElementById(\"" 
				+ SMOrderHeader.Parambdtotalcontractamount + "\").value;\n"
			+ "    if (isNumeric(scontractamt) == false){\n"
			+ "        alert(\"Contract amt '\" + scontractamt + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtotalcontractamount + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtotalcontractamount + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
				
			+ "    var stotalmarkup = document.getElementById(\"" 
				+ SMOrderHeader.Parambdtotalmarkup + "\").value;\n"
			+ "    if (isNumeric(stotalmarkup) == false){\n"
			+ "        alert(\"Total markup '\" + stotalmarkup + \"' is invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtotalmarkup + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtotalmarkup + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"			

			+ "    var struckdays = document.getElementById(\"" 
				+ SMOrderHeader.Parambdtruckdays + "\").value;\n"
			+ "    if (isNumeric(struckdays) == false){\n"
			+ "        alert(\"Truck days '\" + struckdays + \"' are invalid.\");\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtruckdays + "\").focus();\n"
			+ "        document.getElementById(\"" + SMOrderHeader.Parambdtruckdays + "\").select();\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "    return true;"
			+ "}\n"
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
			+ "    var strippedstring = value.replace(/,/g, '');\n"
			//+ "    alert(strippedstring);\n"
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
		
		s += "function flagShipToDirty() {\n"
			+ "    document.getElementById(\"" + SHIPTOCHANGE_FLAG + "\").value = \"" 
				 + SHIPTOCHANGED_VALUE + "\";\n"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n"
		;
		
		s += "function checkConversionFromQuote() {\n"
			+"    flagDirty();\n"
		;
		if (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			s += "    if (document.getElementById(\"" + SMOrderHeader.ParamiOrderType + "\").value != \"" 
				 + Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE) + "\"){\n"
				 + "        alert('If you are changing a quote to an order, you must update the order date!');\n"
				 + "        document.getElementById(\"" + SMOrderHeader.ParamdatOrderDate + "\").focus();\n"
				 + "    }\n"
				 + "    return;\n"
			;
		}
		s += "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
			//First check to see if the date fields were changed, and if so, flag the record was changed field:
			+ "    if (document.getElementById(\"" + SAVEDORDERDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMOrderHeader.ParamdatOrderDate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"			
			+ "    if (document.getElementById(\"" + SAVEDEXPECTEDSHIPDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMOrderHeader.ParamdatExpectedShipDate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"								
			+ "    if (document.getElementById(\"" + SAVEDWARRANTYEXPIRATIONDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMOrderHeader.Paramdatwarrantyexpiration + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"			
			
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
		
		s += "function gotoTotals(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + TOTALSCOMMAND_VALUE + "\";\n"
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
		
		s += "function proposal(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + PROPOSALCOMMAND_VALUE + "\";\n"
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
		
		s += "function changeCustomer(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + CHANGECUSTOMERCOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function renamefolder(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + RENAME_FOLDER_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function createanduploadfolder(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		s += "function colorChangeRed(targetLabel){\n"
			+ "    targetLabel.style.backgroundColor=\"RED\"\n"
			+ "}\n"
		;
		
		s += "function colorChangeBack(targetLabel){\n"
			+ "    targetLabel.style.backgroundColor=\"" + CONVENIENCEPHRASES_BG_COLOR + "\"\n"
			+ "}\n"
		;
		
		/*
		s += "function printQuote(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + PRINTQUOTECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "function emailQuote(){\n"
			 + " fg_popup_form('fg_formContainer','fg_form_InnerContainer','fg_backgroundpopup');\n"
			+ "}\n"
		;
		
		s += "var frmvalidator  = new Validator(\"emailinfo\");\n"
			//+ " frmvalidator.EnableOnPageErrorDisplay();\n"
			//+ " frmvalidator.EnableMsgsTogether();\n"
			+ " frmvalidator.addValidation(\"email1\",\"email\",\"Please provide a valid email address\");\n"
			+ " frmvalidator.addValidation(\"email2\",\"email\",\"Please provide a valid email address\");\n"
			+ " frmvalidator.addValidation(\"email3\",\"email\",\"Please provide a valid email address\");\n"
			+ " frmvalidator.addValidation(\"email4\",\"email\",\"Please provide a valid email address\");\n"
			+ " frmvalidator.addValidation(\"email5\",\"email\",\"Please provide a valid email address\");\n"

			//+ " frmvalidator.addValidation(\"message\",\"maxlen=2048\",\"The message is too long!(more than 2KB!)\");\n"

			+ " document.forms['emailinfo'].refresh_container=function()\n"
		+ " {\n"
		+ " var formpopup = document.getElementById('fg_formContainer');\n"
			+ "var innerdiv = document.getElementById('fg_form_InnerContainer');\n"
			+ "var b = innerdiv.offsetHeight+30+30;\n"

			+ "formpopup.style.height = b+\"px\";\n"
		+ "}\n"

		+ "document.forms['emailinfo'].form_val_onsubmit = document.forms['emailinfo'].onsubmit;\n"


		+ "document.forms['emailinfo'].onsubmit=function()\n"
		+ "{\n"
			+ "if(!this.form_val_onsubmit())\n"
			+ "{\n"
				+ "this.refresh_container();\n"
				+ "return false;\n"
			+ "}\n"

			+ "return true;\n"
		+ "}\n"
		+ "function fg_submit_form()\n"
		+ "{\n"
	        //alert('submiting form');
			+ "var containerobj = document.getElementById('fg_form_InnerContainer');\n"
			+ "var sourceobj = document.getElementById('fg_submit_success_message');\n"
			+ "var error_div = document.getElementById('fg_server_errors');\n"
			+ "var formobj = document.forms['emailinfo']\n"

			+ "var submitter = new FG_FormSubmitter(\"popup-contactform.php\",containerobj,sourceobj,error_div,formobj);\n"
			+ "var frm = document.forms['emailinfo'];\n"

			+ "submitter.submit_form(frm);\n"
		+ "}\n";
		*/
		/*
		 * 
						String sMapAddress = rsOrder.getString(SMTableorderheaders.sShipToAddress1).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress2).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress3).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress4).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCity).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToState).trim();
				sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCountry).trim();
				pwOut.println("<TD><FONT SIZE=2><B>Map: </B><A HREF=\"" 
						+ SMUtilities.createGoogleMapLink(sMapAddress)
						+ "\">"
						+ sMapAddress
						+ "</A>" 
						+ "</FONT></TD>");
		 */
		s += "function testMapLink(){\n"
			+ "    var sLinkAddress = '';\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToAddress1 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToAddress2 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToAddress3 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToAddress4 + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToCity + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToState + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    var sTemp = 	document.getElementById(\"" + SMOrderHeader.ParamsShipToZip + "\").value;\n"
			+ "    if (sTemp == null){sTemp = ''};\n"
			+ "    sLinkAddress = sLinkAddress + sTemp.trim() + ' '\n"
			+ "    sLinkAddress = 'https://maps.google.com/maps?hl=en&geocode=&q=' + escape(sLinkAddress);\n"
			+ "    window.open(sLinkAddress, 'newWindow');\n"
			//+ "    alert (sLinkAddress);\n"
			+ "}\n"
			;
		
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+c\",function() {\n";
		s += "        gotoTotals();\n";
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

		s += "    shortcut.add(\"Alt+e\",function() {\n";
		s += "        changeCustomer();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        createnewfolder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+g\",function() {\n";
		s += "        createanduploadfolder();\n";
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

		s += "    shortcut.add(\"Alt+o\",function() {\n";
		s += "        saveOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        proposal();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+q\",function() {\n";
		s += "        printQuote();\n";
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

		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        saveOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+w\",function() {\n";
		s += "        printItemizedWorkOrder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+u\",function() {\n";
		s += "        renamefolder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "}\n";
		s += "\n";
		
		s += "window.onload = function(){\n"
			//+ "    document.forms.MAINFORM." + SMOrderDetail.ParamsItemDesc + ".focus();\n"
			+ "    initShortcuts();\n"
			//+ "    fg_hideform('fg_formContainer','fg_backgroundpopup');\n"
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

	private boolean checkForProposal(ServletContext context, String  sDBID, String sTrimmedOrderNumber){
		String SQL = "SELECT"
			+ " " + SMTableproposals.strimmedordernumber
			+ " FROM " + SMTableproposals.TableName
			+ " WHERE ("
				+ "(" + SMTableproposals.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".checkForProposal [1377091856]");
			if (rs.next()){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean checkForGDocLink(
		ServletContext context, 
		String  sDBID, 
		String sTrimmedOrderNumber,
		String sUserID,
		String sUserFullName){
		
		SMOrderHeader entry = new SMOrderHeader();
		entry.setM_strimmedordernumber(sTrimmedOrderNumber);
		
		try{
			entry.load(getServletContext(), sDBID, sUserID, sUserFullName);
			if(entry.getM_sGDocLink().compareToIgnoreCase("") == 0){
				return false;
			}else{
				return true;
			}
		}catch (Exception e){
			return false;
		}
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
