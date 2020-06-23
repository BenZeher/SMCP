package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOption;
import smap.APVendor;
import smcontrolpanel.SMCriticalDateEntry;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicshipvias;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablevendorreturns;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICEditPOEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String FOUND_VENDOR_PARAMETER = "FOUND" + ICPOHeader.Paramsvendor;
	public static final String FIND_VENDOR_BUTTON_NAME = "Find Vendor";
	public static final String SHIPVIA_LIST_OPTION_NOT_CHOSEN_DESC = "*** Use customized ship via  ***";
	public static final String SHIPVIA_LIST_OPTION_NOT_CHOSEN_VALUE = "0";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
//	public static final String CREATE_DOCUMENT_FOLDER_COMMAND_VALUE = "CREATEDOCFOLDER";
//	public static final String CREATE_DOCUMENT_FOLDER_BUTTON_LABEL = "Create document folder in Google Drive";
//	public static final String UPLOAD_FOLDER_COMMAND_VALUE = "UPLOADTOFOLDER";
//	public static final String UPLOAD_FOLDER_BUTTON_LABEL = "Upload files (and create folder) to Google Drive";
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to Google Drive";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String UPDATE_COMMAND_VALUE = "UPDATEPO";
	public static final String UPDATE_BUTTON_LABEL = "Update Purchase Order";
	public static final String DELETE_BUTTON_LABEL = "Delete Purchase Order";
	public static final String DELETE_COMMAND_VALUE = "DELETEPO";
	public static final String DELETE_CHECKBOX_NAME = "CONFIRMDELETEPO";;
	public static final String FINDVENDOR_COMMAND_VALUE = "FINDVENDOR";
	public static final String RETURNING_FROM_FIND_VENDOR_FLAG = "RETURNINGFROMFINDER";
	public static final String SORT_LINE_COMMAND_VALUE = "SORTPOLINES";
	public static final String ON_HOLD_CHECKBOX_TRUE_VALUE = "1";
	
	//This value will hold the last SAVED value for the origination date, so we can tell when it's been changed on the scree
	public static final String Paramlastsavedpodate = "lastsavedpodate";
	public static final String Paramlastsavedexpecteddate = "lastsavedexpecteddate";
	
	private static final String CRITICAL_DATE_TABLE_BG_COLOR = "#F2C3FA";
	private static final String DETAILS_TABLE_BG_COLOR = "#FFBCA2";
	private static final String RECEIPTS_TABLE_BG_COLOR = "#A3D1FF";
	private static final String DARK_ROW_BG_COLOR = "#DCDCDC";
	
	private static final String HEADER_TABLE_MAX_WIDTH = "1000px";
	private static final String INFO_TABLES_MAX_WIDTH = "1100px";

	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);

		ICPOHeader entry = new ICPOHeader(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditPOAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPurchaseOrders
		);

		if (bDebugMode){
			System.out.println("[1579191499] In " + this.toString() 
					+ " smedit.getAddingNewEntryFlag() = " + smedit.getAddingNewEntryFlag() + ", entry.getsID() = " + entry.getsID());
		}
		String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*");
		if (bDebugMode){
			System.out.println("[1579191503] In " + this.toString() 
					+ "sCurrentCompleteURL = " + sCurrentCompleteURL);
		}

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//If this is a 'new' entry, make sure we set the lid to -1, otherwise the user may accidentally have entered an ID and we may try
		//to create a new PO, or edit one with that ID.
		if(smedit.getAddingNewEntryFlag()){
			entry.setsID("-1");
			entry.setcreatedbyfullname(smedit.getFullUserName());
		}
		
		//If this is a 'resubmit', meaning it's being called by SMEditPOAction, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		//Record this URL so we can return to it later:
		if (!smedit.getAddingNewEntryFlag()){
			smedit.addToURLHistory("Editing PO Number " + entry.getsID());
		}else{
			smedit.addToURLHistory("Adding a new purchase order");
		}

		if (currentSession.getAttribute(ICPOHeader.ParamObjectName) != null){
			entry = (ICPOHeader) currentSession.getAttribute(ICPOHeader.ParamObjectName);
			currentSession.removeAttribute(ICPOHeader.ParamObjectName);

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			if (!smedit.getAddingNewEntryFlag()){
				if (request.getParameter(RETURNING_FROM_FIND_VENDOR_FLAG) == null){
					if(!entry.load(getServletContext(), 
									smedit.getsDBID(),
									smedit.getUserID(),
									smedit.getFullUserName())){
						response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
								+ "?" + ICPOHeader.Paramlid + "=" + entry.getsID()
								+ "&Warning=" + entry.getErrorMessages()
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
					}
				}
			}
		}

		//If we are returning from finding a vendor, update that vendor and vendor info:
		if (request.getParameter(FOUND_VENDOR_PARAMETER) != null){
			entry.setsvendor(request.getParameter(FOUND_VENDOR_PARAMETER));
			APVendor ven = new APVendor();
			ven.setsvendoracct(entry.getsvendor());
			if (!ven.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				//Don't choke on this . . . 
			}
			entry.setsvendorname(ven.getsname());
		}
		smedit.getPWOut().println(clsServletUtilities.getJQueryIncludeString());
		smedit.getPWOut().println(clsServletUtilities.getJQueryUIIncludeString());
		smedit.getPWOut().println(clsServletUtilities.getMasterStyleSheetLink());
		
		smedit.printHeaderTable();

		//Add a link to return to the original URL:
		if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
			smedit.getPWOut().println(
					"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
					+ "Back to report" + "</A>");
		}

		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A>");

		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");

		//If it's NOT a new PO, we can add a link to print the PO here:
		if (entry.getsID().compareToIgnoreCase("-1") != 0){
			//Add a link to create a new PO:
			smedit.getPWOut().println(
					"&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ SMUtilities.getFullClassName(this.toString())
					+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Create new Purchase Order" + "</A>"
					);


			boolean bEditingVendorReturns = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditVendorReturns, 
					smedit.getUserID(), 
					getServletContext(), 
					smedit.getsDBID(),
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					);

			if(bEditingVendorReturns) {
				// Link to Edit Vendor Return Selection Screen
				smedit.getPWOut().println( 
						"&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditVendorReturnSelect"
								+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Manage Vendor Returns" + "</A>"
						);
			}

			smedit.getPWOut().println("<BR>");

			smedit.getPWOut().println( 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPOGenerate"
							+ "?" + "StartingPOID" + "=" + entry.getsID()
							+ "&" + "EndingPOID" + "="
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Print Purchase Order" + "</A>"
					);
			smedit.getPWOut().println( 
					"&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPOGenerate"
							+ "?" + "StartingPOID" + "=" + entry.getsID()
							+ "&" + "EndingPOID" + "="
							+ "&" + ICPurchaseOrderForm.PRINTUNRECEIVEDONLY + "=Y"
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Print Purchase Order with UNRECEIVED items only" + "</A>"
					);

		}
		
		boolean bEditingPOAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditPurchaseOrders, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		boolean bEditingOnHoldAllowed = bEditingPOAllowed;
				
		if (bEditingPOAllowed){
			smedit.getPWOut().println(
				"&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICEditPOSelection"
				+ "?"
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Edit another Purchase Order" + "</A>"
			);
		}
		
		smedit.getPWOut().println( 
			"&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintReceivingLabelsGenerate"
			+ "?" + ICPrintReceivingLabelsAction.PARAM_PONUMBER + "=" + entry.getsID()
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">" + "Print item labels for this PO" + "</A>"
			+ "<BR>"
		);
		boolean bPODocumentViewingAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICViewPODocuments, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		if (bPODocumentViewingAllowed){
		    if (entry.getsgdoclink().compareToIgnoreCase("") != 0){
		    	smedit.getPWOut().println("<A HREF=\"" + entry.getsgdoclink() 
					+ "\">Google Drive folder</A>&nbsp;&nbsp;"
			    );
		    }
	    }
		
		boolean bCreateGDrivePOFoldersAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDrivePOFolders, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		boolean bUseGoogleDrivePicker = false;
		String sPickerScript = "";
			try {
			 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
						SMCreateGoogleDriveFolderParamDefinitions.PO_RECORD_TYPE_PARAM_VALUE,
						entry.getsID(),
						getServletContext(),
						smedit.getsDBID());
			} catch (Exception e) {
				System.out.println("[1557932202] - Failed to load drivepicker.js - " + e.getMessage());
			}
	
			if(sPickerScript.compareToIgnoreCase("") != 0) {
				smedit.getPWOut().println(sPickerScript);
				bUseGoogleDrivePicker = true;
			}
			
		//Create Upload file Link
		if (entry.getsID().compareToIgnoreCase("-1") != 0 && bCreateGDrivePOFoldersAllowed){
			//Upload file link
			try {
					smedit.getPWOut().println(
							getGDocUploadLink(
							   bUseGoogleDrivePicker,
								entry.getsID(), 
								smedit.getsDBID(), 
								getServletContext(), 
								request,
								smedit.getUserName()
							)
						);
				
				
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT><BR>");
			}

		}   
		
		//Create edit form 
		String sFormString = "<FORM ID='" +  SMMasterEditEntry.MAIN_FORM_NAME + "' NAME='" + SMMasterEditEntry.MAIN_FORM_NAME + "' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCalledClass() + "'";
			
			sFormString	+= " METHOD='POST'>";
			smedit.getPWOut().println(sFormString);
			smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + smedit.getsDBID() + "'>");
			smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
					+ "smic.ICEditPOEdit" + "\">");
			smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME=\"" + "OriginalURL" + "\" VALUE=\"" 
					+ smedit.getOriginalURL() + "\">");
		
		//Test if PO is allowed to be edited 
		if ((Integer.parseInt(entry.getsstatus()) == SMTableicpoheaders.STATUS_DELETED)
			|| 	(Integer.parseInt(entry.getsstatus()) == SMTableicpoheaders.STATUS_COMPLETE))
			{
			bEditingPOAllowed = false;
			}
		
		if (Integer.parseInt(entry.getsstatus()) == SMTableicpoheaders.STATUS_DELETED){
			bEditingOnHoldAllowed = false;
		}

		try {
			smedit.getPWOut().println(getEditHTML(smedit, entry, bEditingPOAllowed, bPODocumentViewingAllowed, bUseGoogleDrivePicker));
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + ICPOHeader.Paramlid + "=" + entry.getsID()
					+ "&Warning=Could not load Purchase Order #: " + entry.getsID() + " - " + sError
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		
		//If PO can be edited, display Update and Delete buttons
		if (bEditingPOAllowed || bEditingOnHoldAllowed){
			smedit.getPWOut().println("<P>" + createUpdateButton());	
		}
		if (bEditingPOAllowed){
			smedit.getPWOut().println(createDeleteButton());
		}
		if (bEditingPOAllowed || bEditingOnHoldAllowed){
			smedit.getPWOut().println("<P>");	
		}

		//List the po lines and receipts here:
		//if (!smedit.getAddingNewEntryFlag()){
		if (entry.getsID().compareToIgnoreCase("-1") != 0){
			listPOLines(
					smedit.getPWOut(), 
					getServletContext(), 
					smedit.getsDBID(), 
					smedit.getUserID(), 
					entry,
					bEditingPOAllowed,
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
			smedit.getPWOut().println("<BR>");
			
			listPOReceipts(
					smedit.getPWOut(), 
					getServletContext(), 
					smedit.getsDBID(), 
					smedit.getUserID(), 
					entry,
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
			smedit.getPWOut().println("<BR>");
			

			
			smedit.getPWOut().println(SMCriticalDateEntry.listCriticalDates(
					SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE,
					entry.getsID(),
					INFO_TABLES_MAX_WIDTH,
					CRITICAL_DATE_TABLE_BG_COLOR,
					getServletContext(), 
					smedit.getsDBID(), 
					smedit.getUserID(),
					true
					));
		}
		
		listVendorReturns(
				smedit.getPWOut(), 
				getServletContext(), 
				smedit.getsDBID(), 
				smedit.getUserID(), 
				entry,
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		smedit.getPWOut().println("<BR>");
		smedit.getPWOut().println("</HTML>");
		
		return;
	}

	private String getEditHTML(SMMasterEditEntry sm, 
							   ICPOHeader entry, 
							   boolean bEditingPOAllowed, 
							   boolean bPODocumentViewingAllowed,
							   boolean bUseGoogleDrivePicker
							   ) throws SQLException{

		//First, load the locations:
		ArrayList<String> arrLocations = new ArrayList<String>(0);
		ArrayList<String> arrLocationNames = new ArrayList<String>(0);
		ArrayList<String> arrShipVias = new ArrayList<String>(0);
		ArrayList<String> arrShipViaNames = new ArrayList<String>(0);
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".getEditHTML - user: " 
				+ sm.getUserName()
				+ " - "
				+ sm.getFullUserName()
		);

		if (conn == null){
			throw new SQLException();
		}
		try {
			loadLocations(arrLocations, arrLocationNames, conn);
		} catch (SQLException e) {
			throw e;
		}
		try {
			loadShipVias(arrShipVias, arrShipViaNames, conn);
		} catch (SQLException e) {
			throw e;
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080839]");
		
		String s = sCommandScripts(entry, sm);
		s += sStyleScripts();


			
		s += "<TABLE style=\" max-width:" + HEADER_TABLE_MAX_WIDTH +";border-style:solid; border-color:black; font-size:small; \">";

		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramlid + "\" VALUE=\"" + entry.getsID() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramscreatedbyfullname + "\" VALUE=\"" + entry.getscreatedbyfullname() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsponumber + "\" VALUE=\"" + entry.getsponumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramllstatus + "\" VALUE=\"" + entry.getsstatus() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsassignedtofullname + "\" VALUE=\"" + entry.getsassignedtofullname() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramdatassigned + "\" VALUE=\"" + entry.getsassigneddate() + "\">";
		
		//Store the last saved dates so we can tell if the user changed them. 'onchange' event will not pick up change
		s += "<INPUT TYPE=HIDDEN NAME=\"" + Paramlastsavedexpecteddate + "\" VALUE=\"" + entry.getsdatexpecteddate() + "\""
				+ " id=\"" + Paramlastsavedexpecteddate + "\""
				+ "\">";		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + Paramlastsavedpodate + "\" VALUE=\"" + entry.getspodate() + "\""
				+ " id=\"" + Paramlastsavedpodate + "\""
				+ "\">";		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + "id=\"" + COMMAND_FLAG + "\"" + "\">";
		
		//Store whether or not the record has been changed:
		
		//IF a vendor was found by the 'Find Vendor' function, then we KNOW the record was changed:
		if (sm.getRequest().getParameter(FOUND_VENDOR_PARAMETER) != null){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		//Otherwise, set the 'record changed' flag to match the incoming parameter:
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		}
		//New Row
		String sPOID = "NEW";
		if (entry.getsID().compareToIgnoreCase("-1") != 0){
			sPOID = entry.getsID();
		}else{
			entry.setcreatedbyfullname(sm.getFullUserName());
		}
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Purchase order ID:</TD>"
			+ "<TD>" 
			+ sPOID
			+ "</TD>"
			;

		s += "<TD  style=\" text-align:right; font-weight:bold; \">Assigned:</TD>"
			+ "<TD>" 
			+ entry.getsassigneddate()
			+ "&nbsp;to&nbsp;" + entry.getsassignedtofullname()
			+ "</TD>"
			+ "</TR>"
			;

		//New Row
		s += "<TR>";
		//Status:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Status:</TD>"
			+ "<TD>"
			+ SMTableicpoheaders.getStatusDescription(Integer.parseInt(entry.getsstatus()));

		//If it was deleted, indicate when and by whom:
		if (Integer.parseInt(entry.getsstatus()) == SMTableicpoheaders.STATUS_DELETED){
			s+= " on " + entry.getsdeleteddate() + " by " + entry.getsdeletedbyfullname();
		}
		s += "</TD>"
			;

		if (entry.getsponumber().trim().compareToIgnoreCase("") == 0){
			s += "<TD>&nbsp;</TD><TD>&nbsp;</TD>";
		}else{
			s += "<TD style=\" text-align:right; font-weight:bold; \">";
			s += "Old PO No.:";
			s += "</TD>"
				;
			s += "<TD style=\" text-align:left; \">"
				+ entry.getsponumber()
				+ "</TD>"
				;
		}

		s += "</TR>";

		//New Row 
		s += "<TR>";
	
		//Created by:
		s += "<TD  style=\" text-align:right; font-weight:bold; \">Created by:</TD>"
			+ "<TD>" 
			+ entry.getscreatedbyfullname()
			+ "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">PO Date:</TD>";
		s += "<TD style=\" text-align:left; \">";
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				&& bEditingPOAllowed
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramdatpodate + "\""
			+ " onchange=\"flagDirty();\""
			+ " ID=\"" + ICPOHeader.Paramdatpodate + "\""
			+ " VALUE=\"" + entry.getspodate() + "\""
			+ " SIZE=28"
			+ " MAXLENGTH=10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(ICPOHeader.Paramdatpodate, getServletContext())
			;			
		}else{
			s += entry.getspodate();
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramdatpodate + "\" VALUE=\"" 
			+ entry.getspodate() + "\">";
		}
		s += "</TD>";

		//Expected date
		s += "<TD style=\" text-align:right; font-weight:bold; \">Expected Date:</TD>";
		s += "<TD style=\" text-align:left; \">";
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				|| (entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0)
				&& bEditingPOAllowed
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsdatexpecteddate + "\""
			+ " onchange=\"flagDirty();\""
			+ " ID=\"" + ICPOHeader.Paramsdatexpecteddate + "\""
			+ " VALUE=\"" + entry.getsdatexpecteddate() + "\""
			+ " SIZE=28"
			+ " MAXLENGTH=10"
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ SMUtilities.getDatePickerString(ICPOHeader.Paramsdatexpecteddate, getServletContext())
			;			
		}else{
			s += entry.getsdatexpecteddate();
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsdatexpecteddate + "\" VALUE=\"" 
			+ entry.getsdatexpecteddate() + "\">";
		}
		s += "</TD>";
		s += "</TR>";

		//New Row:
		//Vendor
		//Lock this ONLY if the po has been received:
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Vendor no.:</TD><TD>"; 
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				&& bEditingPOAllowed
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsvendor + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsvendor().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "20"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.svendorLength)
			+ ">"
			;
			s += createVendorButton();
			
		}else{
			s += entry.getsvendor();
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsvendor + "\" VALUE=\"" 
			+ entry.getsvendor() + "\">";
		}
		s += "</TD>";
		//Vendor name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Vendor name:</TD>"
			+ "<TD>" + entry.getsvendorname().replace("\"", "&quot;") + "</TD>"
			;
		s += "</TR>";
		
		//New row
		//Get the additional vendor information:
		String sCompanyAccountCode = "";
		String sWebAddress = "";
		String sVendorEmail = "";
		String SQL = "SELECT"
			+ " " + SMTableicvendors.scompanyacctcode
			+ ", " + SMTableicvendors.swebaddress
			+ ", " + SMTableicvendors.svendoremail
			+ " FROM " + SMTableicvendors.TableName
			+ " WHERE ("
				+ "(" + SMTableicvendors.svendoracct + " = '" + entry.getsvendor() + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sCompanyAccountCode = rs.getString(SMTableicvendors.scompanyacctcode);
				sWebAddress = rs.getString(SMTableicvendors.swebaddress);
				sVendorEmail = rs.getString(SMTableicvendors.svendoremail);
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error [1441894047] In " + this.toString() + ".getEditHTML - error with SQL: " + SQL + " - " + e.getMessage());
		}
		
		s += "<TR>";
		//Vendor's company account code:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Our acct. #:</TD>"
			+ "<TD>" + sCompanyAccountCode.replace("\"", "&quot;") + "</TD>"
			;

		//Vendor's web address:
		String sWebLink = "&nbsp;";
		String sEmailLink = "&nbsp;";
		if (sVendorEmail.compareToIgnoreCase("") != 0){
			sEmailLink =  "<A HREF=\"" 
				+ "mailto: "
				+ sVendorEmail.replace("\"", "&quot;") + "\">" 
			+ sVendorEmail.replace("\"", "&quot;") + "</A>";
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">Send Email to :</TD>"
			+ "<TD>"
			+ sEmailLink 
			+ "</TD>"
			;
		
		s += "</TR>";
		
		if (sWebAddress.compareToIgnoreCase("") != 0){
			sWebLink =  "<A HREF=\"" 
				+ "http://"
				+ sWebAddress.replace("\"", "&quot;").replace("http://", "") + "\">" 
			+ sWebAddress.replace("\"", "&quot;") + "</A>";
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">Web address:</TD>"
			+ "<TD>"
			+ sWebLink 
			+ "</TD>"
			;
		
		s += "</TR>";
		
		//New Row:
		s += "<TR>";
		//Reference
		s += "<TD style=\" text-align:right; font-weight:bold; \">Reference:</TD>";
		if (
				((entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
						|| (entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0))
						&& bEditingPOAllowed
		){
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsreference + "\""
				+ " onchange=\"flagDirty();\""
				+ " VALUE=\"" + entry.getsreference().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sreferenceLength)
				+ ">"
				+ "</TD>"
				;
		}else{
			s += "<TD>" + entry.getsreference().replace("\"", "&quot;") 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsreference + "\" VALUE=\"" 
			+ entry.getsreference() + "\">"
			+ "</TD>"
			;
		}
		//Description
		s += "<TD style=\" text-align:right; font-weight:bold; \">Description:</TD>";
		if (
				((entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
						|| (entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0))
						&& bEditingPOAllowed
		){		
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsdescription + "\""
				+ " onchange=\"flagDirty();\""
				+ " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sdescriptionLength)
				+ ">"
				+ "</TD>"
				;
		}else{
			s += "<TD>" + entry.getsdescription().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsdescription + "\" VALUE=\"" 
			+ entry.getsdescription() + "\">"
			+ "</TD>"
			;
		}
		s += "</TR>";

		//New Row:
		s += "<TR>&nbsp;";
		//Comment
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Comment:</TD>";
		s += "<TD colspan=3>";
		if (
				((entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
						|| (entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0))
						&& bEditingPOAllowed
		){
			s += "<TEXTAREA NAME=\"" + ICPOHeader.Paramscomment + "\""
			+ " onchange=\"flagDirty();\""
			+ " rows=\"2\""
			+ " cols=\"50\""
			+ ">"
			+ entry.getscomment().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			;
		}else{
			s += entry.getscomment().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramscomment + "\" VALUE=\"" 
			+ entry.getscomment() + "\">";
		}
		s += "</TD>";
		s += "</TR>";

		//Bill and ship to's:
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				&& bEditingPOAllowed
		){
			s += createEditableBillAndShipTos(entry, arrLocations, arrLocationNames);
		}else{
			s += createUneditableBillAndShipTos(entry, arrLocations, arrLocationNames);
		}
		
		//New Row:
		s += "<TR>";
		//Ship via code:
		if ((entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				&& bEditingPOAllowed){

			String sCode = "";
			if (entry.getsshipviacode().compareToIgnoreCase("") != 0){
				sCode = entry.getsshipvianame().replace("\"", "&quot;");	
			}else{
				sCode = "";
			}
			
			//ship via name:
			s += "<TD style=\" text-align:right; font-weight:bold; \">Ship via:<BR><I><FONT COLOR=RED>(Enter or pick a ship via from the list)</FONT></I></TD>";
			s += "<TD colspan=3>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipvianame + "\""
				+ " onchange=\"flagDirty();\""
				+ " ID=\"" + ICPOHeader.Paramsshipvianame + "\""
				+ " VALUE=\"" + sCode + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipvianameLength)
				+ ">";
			
				s += "&nbsp;&nbsp;<SELECT ID=\"" + ICPOHeader.Paramsshipviacode + "\""
											+ " onchange=\"shipviaChange(this);\""
											+ " NAME=\"" + ICPOHeader.Paramsshipviacode + "\">";
				//Add one for the 'Other':
				s += "<OPTION";
				if (entry.getsshipviacode().compareToIgnoreCase("") == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + SHIPVIA_LIST_OPTION_NOT_CHOSEN_VALUE + "\">" + SHIPVIA_LIST_OPTION_NOT_CHOSEN_DESC + "</OPTION>";

				for (int i = 0; i < arrShipVias.size(); i++){
					s += "<OPTION";
					if (arrShipVias.get(i).compareToIgnoreCase(entry.getsshipviacode()) == 0){
						s += " selected=YES ";
					}
					s += " VALUE=\"" + arrShipVias.get(i).toString() + "\">" + arrShipViaNames.get(i).toString();
					s += "</OPTION>";
				}
				s += "</SELECT>";
			}
			s += "</TD>";
			
		s += "</TR>";
		
		//Begin the 'payment on hold' section here:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
		s += "    <TD COLSPAN = 4><B><I><U>PAYMENT ON HOLD INFORMATION</U></I?</B></TD>" + "\n";
		s += "  </TR>" + "\n";
		
		s += "  <TR>\n";
		
		//On hold checkbox:
		String sChecked = "";
		if (entry.getspaymentonhold().compareToIgnoreCase("1") == 0){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}//else{
			//sChecked = clsServletUtilities.CHECKBOX_UNCHECKED_STRING;
		//}
		s += "    <TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Payment on hold:</TD>\n";
		s += "    <TD><INPUT TYPE=CHECKBOX"
			+ " " + sChecked
			+ " NAME=\"" + ICPOHeader.Paramipaymentonhold + "\""
			+ " ID=\"" + ICPOHeader.Paramipaymentonhold + "\""
			//+ " VALUE=\"" + sCheckBoxValue + "\""
 		+ " onchange=\"flagDirty();\""
 		+ ">"
 		+ "</TD>" + "\n"
 		;
		
		//Date placed on hold:
		s += "    <TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Date placed on hold:</TD>\n";
		s += "    <TD>" + entry.getdatpaymentplacedonhold() 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramdatpaymentplacedonhold + "\" VALUE=\"" + entry.getdatpaymentplacedonhold() + "\">"
			+ "</TD>\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		//Placed on hold by:
		String sPlacedOnHoldBy = "";
		if (entry.getspaymentonhold().compareToIgnoreCase("1") == 0){
			sPlacedOnHoldBy = "User ID: " + entry.getlpaymentonholdbyuserid() + " - " + entry.getspaymentonholdbyfullname();
		}
		s += "    <TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Placed on hold by:</TD>\n";
		s += "    <TD COLSPAN = 3>" + sPlacedOnHoldBy
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramlpaymentonholdbyuserid + "\" VALUE=\"" + entry.getlpaymentonholdbyuserid() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramspaymentonholdbyfullname + "\" VALUE=\"" + entry.getspaymentonholdbyfullname() + "\">"
		+ "</TD>\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		//On hold reason:
		s += "    <TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Reason for hold:</TD>\n";
		s += "    <TD colspan=3>";
		
		//If the PO is already placed on hold, then don't let the user edit the reason:
		if (entry.getspaymentonhold().compareToIgnoreCase("1") == 0){
			s += "<I>" + entry.getmpaymentonholdreason().replace("\"", "&quot;") + "</I>"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Parammpaymentonholdreason 
				+ "\" VALUE=\"" + entry.getmpaymentonholdreason().replace("\"", "&quot;") + "\">"
			;
		}else{
			s += "<TEXTAREA NAME=\"" + ICPOHeader.Parammpaymentonholdreason + "\""
				+ " onchange=\"flagDirty();\""
				+ " rows=\"1\""
				+ " cols=\"80\""
				+ ">"
				+ entry.getmpaymentonholdreason().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				;
		}

		s += "</TD>\n";
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		//Vendor comments:
		s += "    <TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Vendor comments:</TD>\n";
		s += "    <TD colspan=3>";
		s += "<TEXTAREA NAME=\"" + ICPOHeader.Parammpaymentonholdvendorcomment + "\""
		+ " onchange=\"flagDirty();\""
		+ " rows=\"1\""
		+ " cols=\"80\""
		+ ">"
		+ entry.getmpaymentonholdvendorcomment().replace("\"", "&quot;")
		+ "</TEXTAREA>"
		;
		s += "</TD>\n";
		s += "  </TR>\n";
		
		s += "</TABLE>";
		
		//Add a field for Google Docs link
		String sCreateAndUploadButton = "";
		if (
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDrivePOFolders, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			)
			&& (entry.getsID().compareToIgnoreCase("-1") != 0)
		){
			sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
		}
		
		if ((entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				&& bEditingPOAllowed && bPODocumentViewingAllowed){
			s += "<B><FONT SIZE=2>Document folder link:</FONT></B>" + "&nbsp;" + sCreateAndUploadButton + "<BR>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsgdoclink + "\""
				+ " onchange=\"flagDirty();\""
				+ " VALUE=\"" + entry.getsgdoclink().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "90"
				+ " MAXLENGTH=" + Integer.toString(254)
				+ "<BR>"
			;
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsgdoclink + "\" VALUE=\"" + entry.getsgdoclink() + "\">";
		}
		return s;
	}
	private void listPOLines(
			PrintWriter out, 
			ServletContext context, 
			String sDBID, 
			String sUserID,
			ICPOHeader entry,
			boolean bPOEditingAllowed,
			String sLicenseModuleLevel
	){
		out.println("<br><br><b><u><FONT SIZE=2>PO Detail Lines</FONT></u></b><br>");

		out.println("<TABLE STYLE=\"max-width:"+INFO_TABLES_MAX_WIDTH+"; table-layout: fixed; background-color: " + DETAILS_TABLE_BG_COLOR + ";\" BORDER=0 cellspacing=0 cellpadding=1><TR>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>&nbsp;&nbsp;</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Line&nbsp;#</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Location</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Qty&nbsp;ordered</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Qty&nbsp;received</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Item&nbsp;#</FONT></B></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Ven.&nbsp;Item&nbsp;#</FONT></B></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Non-Inventory?</FONT></B></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>Item&nbsp;description</B></FONT></TD>");
		out.println("<TD class = \"leftjustifiedheading\" ><FONT SIZE=2><B>UOM</B></FONT></TD>");
		out.println("<TD class = \"rightjustifiedheading\" ><FONT SIZE=2><B>Total&nbsp;order&nbsp;cost</B></FONT></TD>");
		out.println("<TD class = \"rightjustifiedheading\" ><FONT SIZE=2><B>Total&nbsp;received&nbsp;cost</B></FONT></TD>");
		

		boolean bViewItemAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation, 
				sUserID, 
				context, 
				sDBID,
				sLicenseModuleLevel);
		boolean bEditVendorItemAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditVendorItems, 
				sUserID, 
				context, 
				sDBID,
				sLicenseModuleLevel);

		BigDecimal bdTotalOrderedCost = new BigDecimal(0);
		BigDecimal bdTotalReceivedCost = new BigDecimal(0);
		boolean bOddRow = false;
		String sBackgroundColor = "";
		
		if (entry.getsID().compareToIgnoreCase("") != 0){
			String SQL = "SELECT"
				+ " " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedreceivedcost
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.lid
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sunitofmeasure
				//TJR - changed this on 10/25/16 to read the actual PO line instead
				//+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber
				+ " FROM " + SMTableicpolines.TableName
				//+ " LEFT JOIN " + SMTableicvendoritems.TableName
				//+ " ON ("
				//+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = "
				//+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber + ")"
				//+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + " ='" + entry.getsvendor() + "')"
				//+ ")"
				+ " WHERE ("
				+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = " + entry.getsID() + ")"
				//+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + " = '" + entry.getsvendor() + "')"
				+ ")"
				+ " ORDER BY " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
				;
			if (bDebugMode){
				System.out.println("[1579191513] In " + this.toString() + ".listPOLines - SQL = " + SQL);
			}
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						context, 
						sDBID, 
						"MySQL", 
						SMUtilities.getFullClassName(this.toString() + " - userID: " + sUserID)
				);

				out.println("<tbody id=\"sortable\" style=\"overflow-y: auto;\">");
				
				
				while (rs.next()){
					
					if(bOddRow){
						sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
					}else{
						sBackgroundColor = "\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"";
					}
					
					out.println("<TR bgcolor=" + sBackgroundColor + ">");
					//Space for drag sort handle
					out.println("<TD ><span class=\"handle\">"
							+ "<span class=\"ui-icon ui-icon-arrowthick-2-n-s\">" 
							+ "</span>"
							+ "</span></TD>");
										
					//Line number:
					String sLineID = Long.toString(rs.getLong(SMTableicpolines.TableName + "." + SMTableicpolines.lid)); 
					String sLineNumber = Long.toString(rs.getLong(SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber)); 
					String sLineNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOLineEdit"
					+ "?" + ICPOLine.Paramlpoheaderid + "=" + entry.getsID()
					+ "&" + ICPOLine.Paramlid + "=" + sLineID
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					//+ "&" + ICPOHeader.Paramsponumber + "=" + entry.getsponumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sLineNumber) + "</A>";
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + sLineNumberLink + "&nbsp;</FONT>");
					out.println("</TD>");
					//Add hidden parameter to store change in line numbers when sorting
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"POLINEID" + sLineID + "\" VALUE=\"" + sLineNumber + "\" >");
					
					//Location:
					out.println("<TD><FONT SIZE=2>&nbsp;" 
							+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.slocation) + "</FONT></TD>"
					);
					
					//Qty ordered
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
									SMTableicpolines.bdqtyorderedScale, rs.getBigDecimal(
											SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered)) + "</FONT></TD>"
					);					
					//Qty received
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
									SMTableicpolines.bdqtyreceivedScale, rs.getBigDecimal(
											SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived)) + "</FONT></TD>"
					);

					//Item
					String sItemNumber = rs.getString(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.sitemnumber);
					String sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber)
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
					if (bViewItemAllowed){
						out.println("<TD><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
					}else {
						out.println("<TD><FONT SIZE=2>" + sItemNumber + "</FONT></TD>");
					}

					//Vendor's item number:
					String sVendorItemNumber = rs.getString(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.svendorsitemnumber);
					if ((sVendorItemNumber == null)
						|| (sVendorItemNumber.trim().compareToIgnoreCase("") == 0)
					){
						sVendorItemNumber = "(NONE)";
					}
					String sVendorItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICEditVendorItems?" + ICEditVendorItems.ITEMNUMBER + "=" 
					+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber)
					+ "&" + ICEditVendorItems.EDITINGSINGLEVENDOR_PARAM + "=" + entry.getsvendor()
					+ "&" + ICEditVendorItems.INITIATED_FROM_PO_PARAM + "=" + entry.getsID()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sVendorItemNumber) + "</A>";
					if (
						bEditVendorItemAllowed
						&& (rs.getLong(SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem) == 0)
					){
						out.println("<TD><FONT SIZE=2>" + sVendorItemNumberLink + "</FONT></TD>");
					}else {
						out.println("<TD><FONT SIZE=2>" + sVendorItemNumber + "</FONT></TD>");
					}
					
					//Non-inventory item?
					String sNonInventoryItem = "N";
					if (rs.getInt(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.lnoninventoryitem) == 1){
						sNonInventoryItem = "Y";
					}
					out.println("<TD ALIGN=CENTER><FONT SIZE=2><B>" + sNonInventoryItem + "</B></FONT></TD>");
					
					//Desc
					out.println("<TD><FONT SIZE=2>" 
							+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription) + "</FONT></TD>"
					);

					//UOM
					out.println("<TD><FONT SIZE=2>" 
							+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.sunitofmeasure) + "</FONT></TD>"
					);

					//total order cost
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
									SMTableicpolines.bdextendedordercostScale, rs.getBigDecimal(
											SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost)) + "</FONT></TD>"
					);

					//total received cost
					out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
									SMTableicpolines.bdextendedreceivedcostScale, rs.getBigDecimal(
											SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedreceivedcost)) + "</FONT></TD>"
					);

					out.println("</TR>");

					bdTotalOrderedCost = bdTotalOrderedCost.add(rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost));

					bdTotalReceivedCost = bdTotalReceivedCost.add(rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedreceivedcost));

					bOddRow = !bOddRow;
				}
				out.println("</tbody>");
				rs.close();
			} catch (SQLException e) {
				System.out.println("[1579191519] In " + this.toString() + "Error[1428417898] with SQL: " + SQL + " - " + e.getMessage());
			}
		}
		if(bOddRow){
			sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
		}else{
			sBackgroundColor = "\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"";
		}
		//Print the grand total:
		out.println("<TR bgcolor= " + sBackgroundColor + ">");
		out.println("<TD></TD>");
		out.println("<TD COLSPAN=9 ALIGN=RIGHT><B><FONT SIZE=2>TOTALS:</FONT></TD>"
				+ "<TD ALIGN=RIGHT><B><FONT SIZE = 2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdextendedordercostScale, bdTotalOrderedCost) + "</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdextendedreceivedcostScale, bdTotalReceivedCost) + "</FONT></B></TD>");
		out.println("</TR>");

		out.println("</TABLE>");

		//Add a link to insert a line:
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				|| 	(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0)
		){
			if (bPOEditingAllowed){
				out.println(
						"<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOLineEdit"
						+ "?" + ICPOLine.Paramlpoheaderid + "=" + entry.getsID()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=TRUE" //Set this to indicate it's an 'add'
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + ICPOHeader.Paramsponumber + "=" + entry.getsponumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "Add new line" + "</A></FONT>"		
				);
			}
		}
	}
	private void listPOReceipts(
			PrintWriter out, 
			ServletContext context, 
			String sDBID, 
			String sUserID,
			ICPOHeader entry,
			String sLicenseModuleLevel
	){


		out.println("<br><b><u><FONT SIZE=2>PO Receipts</FONT></u></b><BR>");
		if (entry.getsID().compareToIgnoreCase("") != 0){
			String SQL = "SELECT"
				+ " " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datdeleted
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.sdeletedbyfullname
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus
				+ ", IF(PORECEIPTLINEQUERY.RECEIPTLINETOTAL IS NULL, 0.00, PORECEIPTLINEQUERY.RECEIPTLINETOTAL) AS TOTAL"
				+ " FROM " + SMTableicporeceiptheaders.TableName
				+ " LEFT JOIN "
				+ " (SELECT "
				+ SMTableicporeceiptlines.lreceiptheaderid + " AS RCPTID"
				+ ", SUM(" + SMTableicporeceiptlines.bdextendedcost + ") AS RECEIPTLINETOTAL"
				+ " FROM " + SMTableicporeceiptlines.TableName
				//+  " WHERE ("
				//+	"(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + entry.getsID() + ")"
				//+ ")"
				+ " GROUP BY " + SMTableicporeceiptlines.lreceiptheaderid
				+ ") AS PORECEIPTLINEQUERY"
				+ " ON " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " = "
				+ "PORECEIPTLINEQUERY.RCPTID" 
				+ " WHERE ("
					+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid + " = " + entry.getsID() + ")"
				+ ")"
				+ " ORDER BY " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				;
			
			/*
			SQL = SELECT
			icporeceiptheaders.datreceived
			, icporeceiptheaders.lid
			, icporeceiptheaders.lpostedtoic
			, icporeceiptheaders.screatedby
			, icporeceiptheaders.datdeleted
			, icporeceiptheaders.sdeletedby
			, icporeceiptheaders.lstatus
			, SUM(icporeceiptlines.bdextendedcost) AS TOTAL FROM icporeceiptheaders
			LEFT JOIN icporeceiptlines ON icporeceiptlines.lreceiptheaderid = icporeceiptheaders.lid
			WHERE (
				icporeceiptheaders.lpoheaderid = 44170
			) GROUP BY icporeceiptlines.lreceiptheaderid ORDER BY icporeceiptheaders.lid
			*/
			if (bDebugMode){
				System.out.println("[1488549770] In " + this.toString() + ".listPOReceipts - SQL = " + SQL);
			}
			boolean bEditReceipts  = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.ICEditReceipts, 
						sUserID, 
						context,
						sDBID,
						sLicenseModuleLevel
				); 
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + " - userID: " + sUserID)
			);

			if(rs.isBeforeFirst()) {
				out.println("<TABLE BORDER=0  WIDTH=100% cellspacing=0 cellpadding=1 style= \"max-width:"+ INFO_TABLES_MAX_WIDTH +";"
						+ " background-color: " + RECEIPTS_TABLE_BG_COLOR + "; \" ><TR>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Receipt&nbsp;#</B></FONT></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Created&nbsp;by</B></FONT></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Date</B></FONT></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Posted&nbsp;to&nbsp;IC?</FONT></B></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Deleted&nbsp;on</FONT></B></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Deleted&nbsp;by</FONT></B></TD>");
				out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Total&nbsp;cost&nbsp;on&nbsp;this&nbsp;receipt</B></FONT></TD>");					
			}
			
			boolean bOddRow = false;
			String sBackgroundColor= "";
			while (rs.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"";
				}
				//Because we are getting a 'SUM' in this query, it will return one record every time, even if there are no
				//receipts at all.  So we have to add this check to make sure that we have a 'real' receipt:
				if (rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid) > 0L){
					out.println("<TR  bgcolor =" + sBackgroundColor +">");
					//Line number:
					String sReceiptID = Long.toString(rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid)); 
					String sReceiptIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICEditReceiptEdit"
					+ "?" + ICPOReceiptHeader.Paramlpoheaderid + "=" + entry.getsID()
					+ "&" + ICPOReceiptHeader.Paramlid + "=" 
					+ Long.toString(rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid))
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sReceiptID) + "</A>";
	
					if (bEditReceipts){
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sReceiptIDLink + "</FONT></TD>");	
					}else{
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sReceiptID + "</FONT></TD>");
					}
	
					String sFontColor = "BLACK";
					if (rs.getInt(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus) == SMTableicporeceiptheaders.STATUS_DELETED){
						sFontColor = "RED";
					}
					
					//Received by:
					out.println("<TD ALIGN=LEFT><FONT SIZE=2 COLOR=" + sFontColor + " >" + rs.getString(
						SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname) + "</FONT></TD>");
					
					//Received date
					out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + ">" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(
							rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived)) 
						+ "</FONT></TD>"
					);
	
					//Posted to IC?
					if (rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic) == 1){
						out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + ">" + "YES" + "</FONT></TD>");
					}else{
						out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + ">" + "NO" + "</FONT></TD>");
					}
					String sDeletedDate = "N/A";
					String sDeletedBy = "N/A";
					if (rs.getInt(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus) == SMTableicporeceiptheaders.STATUS_DELETED){
						sDeletedDate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datdeleted));
						sDeletedBy = rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.sdeletedbyfullname);
					}
					out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + " >" + sDeletedDate + "</FONT></TD>");
					out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + " >" + sDeletedBy + "</FONT></TD>");
					out.println("<TD><FONT SIZE=2 COLOR=" + sFontColor + " >" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicporeceiptlines.bdextendedcostScale, rs.getBigDecimal("TOTAL")) + "</FONT></TD>"
					);
					
					out.println("</TR>");
					bOddRow = !bOddRow;
				}
			}
			rs.close();
			} catch (SQLException e) {
				System.out.println("[1579191528] In " + this.toString() + "Error [1428417800] with SQL: " + SQL + " - " + e.getMessage());
			}
		}
		
		out.println("</TABLE>");

		//Add a link to insert a receipt header:
		if (
				(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_ENTERED)) == 0)
				|| 	(entry.getsstatus().compareToIgnoreCase(Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED)) == 0)
		){
			out.println(
				"<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptEdit"
				+ "?" + ICPOReceiptHeader.Paramlpoheaderid + "=" + entry.getsID()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=TRUE" //Set this to indicate it's an 'add'
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">" + "Add new receipt" + "</A></FONT>"		
			);
		}
	}
	
	private boolean loadLocations(
			ArrayList<String> arrLocs,
			ArrayList<String> arrLocNames,
			Connection conn
	) throws SQLException{

		String SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY " + SMTablelocations.sLocation
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrLocs.add(rs.getString(SMTablelocations.sLocation));
				arrLocNames.add(rs.getString(SMTablelocations.sLocationDescription));
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		}

		return true;
	}
	private boolean loadShipVias(
			ArrayList<String> arrShipVia,
			ArrayList<String> arrShipViaName,
			Connection conn
	) throws SQLException{

		String SQL = "SELECT *"
			+ " FROM " + SMTableicshipvias.TableName
			+ " ORDER BY " + SMTableicshipvias.sshipviacode
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrShipVia.add(rs.getString(SMTableicshipvias.sshipviacode));
				arrShipViaName.add(rs.getString(SMTableicshipvias.sshipvianame));
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		}

		return true;
	}
	private String createEditableBillAndShipTos(
			ICPOHeader entry, 
			ArrayList<String>arrLocations, 
			ArrayList<String>arrLocationNames){

		String s = "";
		//New Row:
	
		//Bill to code:
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Bill To:</TD>";
		s += "<TD>";
		//s += "<SELECT NAME=\"" + ICPOHeader.Paramsbillcode + "\"" + ">";
		s += "<SELECT ID=\"" + ICPOHeader.Paramsbillcode + "\""
				+ " onchange=\"billtoChange(this); flagDirty();\""
				+ " NAME=\"" + ICPOHeader.Paramsbillcode + "\"" + ">";
		//Add one for the 'Other':
		s += "<OPTION";
		if (entry.getsbillcode().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + "" + "\">" + "** OTHER **</OPTION>";

		for (int i = 0; i < arrLocations.size(); i++){
			s += "<OPTION";
			if (arrLocations.get(i).compareToIgnoreCase(entry.getsbillcode()) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + arrLocations.get(i).toString() + "\">" + arrLocationNames.get(i).toString();
			s += "</OPTION>";
		}
		s += "</SELECT></TD>";
		
		//Ship to code:
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Ship To:</TD>";
		s += "<TD>";
		//s += "<SELECT NAME=\"" + ICPOHeader.Paramsshipcode + "\"" + ">";
		s += "<SELECT ID=\"" + ICPOHeader.Paramsshipcode + "\""
				+ " onchange=\"shiptoChange(this); flagDirty();\""
				+ " NAME=\"" + ICPOHeader.Paramsshipcode + "\"" + ">";
		//Add one for the 'Other':
		s += "<OPTION";
		if (entry.getsshipcode().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + "" + "\">" + "** OTHER **</OPTION>";

		for (int i = 0; i < arrLocations.size(); i++){
			s += "<OPTION";
			if (arrLocations.get(i).compareToIgnoreCase(entry.getsshipcode()) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + arrLocations.get(i).toString() + "\">" + arrLocationNames.get(i).toString();
			s += "</OPTION>";
		}
		s += "</SELECT></TD></TR>";
		
		//New Row:
		s += "<TR>";
		//Bill to name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Bill to name:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillname + "\""
			+ " onchange=\"flagDirty();\""
			+ " ID=\"" + ICPOHeader.Paramsbillname + "\""
			+ " VALUE=\"" + entry.getsbillname().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillnameLength)
			+ ">"
			+ "</TD>"
			;

		//Ship to name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Ship to name:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipname + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipname().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipnameLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 1:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Address:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbilladdress1 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbilladdress1().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbilladdress1Length)
			+ ">"
			+ "</TD>"
			;
		//ship to address 1:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Address:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipaddress1 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipaddress1().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipaddress1Length)
			+ ">"
			+ "</TD>"
			;

		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 2:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbilladdress2 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbilladdress2().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbilladdress2Length)
			+ ">"
			+ "</TD>"
			;
		//ship to address 2:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipaddress2 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipaddress2().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipaddress2Length)
			+ ">"
			+ "</TD>"
			;

		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 3:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbilladdress3 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbilladdress3().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbilladdress3Length)
			+ ">"
			+ "</TD>"
			;
		//ship to address 3:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipaddress3 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipaddress3().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipaddress3Length)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 4:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbilladdress4 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbilladdress4().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbilladdress4Length)
			+ ">"
			+ "</TD>"
			;
		//ship to address 4:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipaddress4 + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipaddress4().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipaddress4Length)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to City:
		s += "<TD style=\" text-align:right; font-weight:bold; \">City:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillcity + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillcity().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillcityLength)
			+ ">"
			+ "</TD>"
			;
		//ship to city:
		s += "<TD style=\" text-align:right; font-weight:bold; \">City:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipcity + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipcity().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipcityLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to State:
		s += "<TD style=\" text-align:right; font-weight:bold; \">State:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillstate + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillstate().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillstateLength)
			+ ">"
			+ "</TD>"
			;
		//ship to state:
		s += "<TD style=\" text-align:right; font-weight:bold; \">State:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipstate + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipstate().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipstateLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to country:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Country:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillcountry + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillcountry().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillcountryLength)
			+ ">"
			+ "</TD>"
			;
		//ship to country:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Country:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipcountry + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipcountry().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipcountryLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to Zip:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Postal code:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillpostalcode + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillpostalcode().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillpostalcodeLength)
			+ ">"
			+ "</TD>"
			;
		//ship to Zip:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Postal code:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshippostalcode + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshippostalcode().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshippostalcodeLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to Contact Name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Contact:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillcontactname + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillcontactname().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillcontactnameLength)
			+ ">"
			+ "</TD>"
			;
		//ship to Contact Name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Contact:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipcontactname + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipcontactname().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipcontactnameLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//TODO PHONE
		//Bill to Phone:
		if(entry.getsbillphone().replace("\"", "&quot;").compareToIgnoreCase("")==0) {
		s += "<TD style=\" text-align:right; font-weight:bold; \">Phone:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillphone + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillphone().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillphoneLength)
			+ ">"
			+ "</TD>"
			;
		} else {
			s += "<TD style=\" text-align:right; font-weight:bold; \"><A HREF=\"tel:" + entry.getsbillphone().replace("\"", "&quot;") + "\">Phone:</A></TD>";
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillphone + "\""
				+ " onchange=\"flagDirty();\""
				+ " VALUE=\"" + entry.getsbillphone().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillphoneLength)
				+ ">"
				+ "</TD>"
				;
		}
		
		//ship to Phone:
		if(entry.getsshipphone().replace("\"", "&quot;").compareToIgnoreCase("")==0) {
		s += "<TD style=\" text-align:right; font-weight:bold; \">Phone:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipphone + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipphone().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipphoneLength)
			+ ">"
			+ "</TD>"
			;
		} else {
			s += "<TD style=\" text-align:right; font-weight:bold; \"><A HREF=\"tel:" + entry.getsbillphone().replace("\"", "&quot;") + "\">Phone:</A></TD>";
			s += "<TD>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipphone + "\""
				+ " onchange=\"flagDirty();\""
				+ " VALUE=\"" + entry.getsshipphone().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipphoneLength)
				+ ">"
				+ "</TD>"
				;
		}
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to Fax:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Fax:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsbillfax + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsbillfax().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sbillfaxLength)
			+ ">"
			+ "</TD>"
			;
		//ship to Fax:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Fax:</TD>";
		s += "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICPOHeader.Paramsshipfax + "\""
			+ " onchange=\"flagDirty();\""
			+ " VALUE=\"" + entry.getsshipfax().replace("\"", "&quot;") + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.sshipfaxLength)
			+ ">"
			+ "</TD>"
			;
		s += "</TR>";

		return s;
	}
	private String createUneditableBillAndShipTos(
			ICPOHeader entry, 
			ArrayList<String>arrLocations, 
			ArrayList<String>arrLocationNames){

		String s = "";

		//Bill to code:
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Bill To:</TD>";
		s += "<TD>";
		s += entry.getsbillcode().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillcode + "\" VALUE=\"" 
		+ entry.getsbillcode() + "\">";
		s += "</TD>";

		//Ship to code:
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Ship To:</TD>";
		s += "<TD>";
		s += entry.getsshipcode().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipcode + "\" VALUE=\"" 
		+ entry.getsshipcode() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		//Bill to name:
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Bill to name:</TD>";
		s += "<TD>";
		s += entry.getsbillname().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillname + "\" VALUE=\"" 
		+ entry.getsbillname() + "\">";;
		s += "</TD>";

		//Ship to name:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Ship to name:</TD>";
		s += "<TD>";
		s += entry.getsshipname().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipname + "\" VALUE=\"" 
		+ entry.getsshipname() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 1:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Address:</TD>";
		s += "<TD>";
		s += entry.getsbilladdress1().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbilladdress1 + "\" VALUE=\"" 
		+ entry.getsbilladdress1() + "\">";;
		s += "</TD>";

		//ship to address 1:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Address:</TD>";
		s += "<TD>";
		s += entry.getsshipaddress1().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipaddress1 + "\" VALUE=\"" 
		+ entry.getsshipaddress1() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 2:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsbilladdress2().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbilladdress2 + "\" VALUE=\"" 
		+ entry.getsbilladdress2() + "\">";;
		s += "</TD>";

		//ship to address 1:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsshipaddress2().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipaddress2 + "\" VALUE=\"" 
		+ entry.getsshipaddress2() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 3:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsbilladdress3().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbilladdress3 + "\" VALUE=\"" 
		+ entry.getsbilladdress3() + "\">";;
		s += "</TD>";

		//ship to address 3:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsshipaddress3().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipaddress3 + "\" VALUE=\"" 
		+ entry.getsshipaddress3() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill to address 4:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsbilladdress4().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbilladdress4 + "\" VALUE=\"" 
		+ entry.getsbilladdress4() + "\">";;
		s += "</TD>";

		//ship to address 4:
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";
		s += entry.getsshipaddress4().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipaddress4 + "\" VALUE=\"" 
		+ entry.getsshipaddress4() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill City:
		s += "<TD style=\" text-align:right; font-weight:bold; \">City:</TD>";
		s += "<TD>";
		s += entry.getsbillcity().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillcity + "\" VALUE=\"" 
		+ entry.getsbillcity() + "\">";;
		s += "</TD>";

		//Ship City:
		s += "<TD style=\" text-align:right; font-weight:bold; \">City</TD>";
		s += "<TD>";
		s += entry.getsshipcity().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipcity + "\" VALUE=\"" 
		+ entry.getsshipcity() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New Row:
		s += "<TR>";
		//Bill state:
		s += "<TD style=\" text-align:right; font-weight:bold; \">State:</TD>";
		s += "<TD>";
		s += entry.getsbillstate().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillstate + "\" VALUE=\"" 
		+ entry.getsbillstate() + "\">";;
		s += "</TD>";

		//Ship state:
		s += "<TD style=\" text-align:right; font-weight:bold; \">State:</TD>";
		s += "<TD>";
		s += entry.getsshipstate().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipstate + "\" VALUE=\"" 
		+ entry.getsshipstate() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New row
		s += "<TR>";
		//Bill country:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Country:</TD>";
		s += "<TD>";
		s += entry.getsbillcountry().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillcountry + "\" VALUE=\"" 
		+ entry.getsbillcountry() + "\">";;
		s += "</TD>";

		//Ship country:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Country:</TD>";
		s += "<TD>";
		s += entry.getsshipcountry().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipcountry + "\" VALUE=\"" 
		+ entry.getsshipcountry() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New row
		s += "<TR>";
		//bill Zip:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Postal code:</TD>";
		s += "<TD>";
		s += entry.getsbillpostalcode().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillpostalcode + "\" VALUE=\"" 
		+ entry.getsbillpostalcode() + "\">";;
		s += "</TD>";

		//ship zip:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Postal code:</TD>";
		s += "<TD>";
		s += entry.getsshippostalcode().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshippostalcode + "\" VALUE=\"" 
		+ entry.getsshippostalcode() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New row
		s += "<TR>";
		//bill contact:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Contact:</TD>";
		s += "<TD>";
		s += entry.getsbillcontactname().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillcontactname + "\" VALUE=\"" 
		+ entry.getsbillcontactname() + "\">";;
		s += "</TD>";

		//ship contact:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Contact:</TD>";
		s += "<TD>";
		s += entry.getsshipcontactname().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipcontactname + "\" VALUE=\"" 
		+ entry.getsshipcontactname() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New row
		s += "<TR>";
		//bill phone:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Phone:</TD>";
		s += "<TD>";
		s += entry.getsbillphone().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillphone + "\" VALUE=\"" 
		+ entry.getsbillphone() + "\">";;
		s += "</TD>";

		//ship phone:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Phone:</TD>";
		s += "<TD>";
		s += entry.getsshipphone().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipphone + "\" VALUE=\"" 
		+ entry.getsshipphone() + "\">";;
		s += "</TD>";
		s += "</TR>";

		//New row
		s += "<TR>";
		//bill fax:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Fax:</TD>";
		s += "<TD>";
		s += entry.getsbillfax().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsbillfax + "\" VALUE=\"" 
		+ entry.getsbillfax() + "\">";;
		s += "</TD>";

		//ship fax:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Fax:</TD>";
		s += "<TD>";
		s += entry.getsshipfax().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsshipfax + "\" VALUE=\"" 
		+ entry.getsshipfax() + "\">";;
		s += "</TD>";
		s += "</TR>";

		return s;
	}
	
	private String createUpdateButton(){
		return "<button type=\"button\""
			+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " onClick=\"update();\">"
			+ UPDATE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createDeleteButton(){
		return "<button type=\"button\""
			+ " value=\"" + DELETE_BUTTON_LABEL + "\""
			+ " name=\"" + DELETE_BUTTON_LABEL + "\""
			+ " onClick=\"deletePO();\">"
			+ DELETE_BUTTON_LABEL
			+ "</button>\n"
			+ " Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
			+ DELETE_CHECKBOX_NAME + "\""
			+ " id = \"" + DELETE_CHECKBOX_NAME	+ "\""
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
	
	private String createVendorButton(){
		return  "<button type=\"button\""
				+" value=\"" + FIND_VENDOR_BUTTON_NAME + "\""
				+ "name=\"" + FIND_VENDOR_BUTTON_NAME + "\""
				+ " onClick=\"findVendor();\">"
				+  FIND_VENDOR_BUTTON_NAME
				+ "</button>\n";

	}
	
	private String sCommandScripts(ICPOHeader po, SMMasterEditEntry smmaster) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";
		
		//Create drag sort line items. 
		s += "$(document).ready(\n"
				+ "   function() {\n"
				+ "     $('.handle').css('cursor', 'pointer');\n"
				+ "		$(\"tbody#sortable\").sortable({\n"
				+ "		update: function(event, ui) {  \n" 
				+ "         $('tbody#sortable tr').each(function() {\n"  
				+ "         	$(this).children('input').val($(this).index() + 1);\n" 
				+ "        	});\n"  
				+ "          document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SORT_LINE_COMMAND_VALUE + "\";\n"
				+ "          document.forms[\"MAINFORM\"].submit();\n"
				+ "     },\n" 
				
				+ "		handle: 'td:first .handle',\n"
				+ "		cursor: 'move'\n,"
				+ "		tolerance: 'pointer',\n"
				+ "		containment: 'parent'\n"				
				+ "}); \n"
				+ " if(($(\"tbody#sortable\").children('tr').length <= 1)){\n"
				+ "    $( \"tbody#sortable\").sortable( \"disable\" );\n"
				+ "	}\n"
				+ "		});\n"
				;
		
		s += "window.onbeforeunload = prompttosave;\n";
		
		s += "function prompttosave(){\n"
			//Check to see if the date fields were changed and flag the record was changed field.
			+ "   if (document.getElementById(\"" + Paramlastsavedexpecteddate + "\").value != " 
			+ "document.getElementById(\"" + ICPOHeader.Paramsdatexpecteddate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n "	
			
			+ "   if (document.getElementById(\"" + Paramlastsavedpodate + "\").value != " 
			+ "document.getElementById(\"" + ICPOHeader.Paramdatpodate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n  "
	
			//Don't prompt on updates, deletes, and find vendor
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  FINDVENDOR_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + UPDATE_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + DELETE_COMMAND_VALUE + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
	
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "       return 'You have unsaved changes - are you sure you want to leave this page?';\n"
			+ "    }\n"
			+ "}\n\n"
			;
		
		s += "function flagDirty() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n";
					 
		//Update:
		s += "function update(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + UPDATE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		//Delete:
		s += "function deletePO(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + DELETE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
			;
		
		//Find Vendor:
		 s += "function findVendor(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
		    + FINDVENDOR_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n";
		 
		//Create folder and/or upload files:
		s += "function createanduploadfolder(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		//load locations list
		int iCounter = 0;
		
		String slocationnames = "";
		String saddress1s = "";
		String saddress2s = "";
		String saddress3s = "";
		String saddress4s = "";
		String scities = "";
		String sstates = "";
		String scountries = "";
		String spostalcodes = "";
		String scontacts = "";
		String sphones = "";
		String sfaxes = "";
		
		String SQL = "SELECT"
				+ " " + SMTablelocations.sLocation
				+ ", " + SMTablelocations.sCompanyDescription
				+ ", " + SMTablelocations.sLocationDescription
				+ ", " + SMTablelocations.sAddress1
				+ ", " + SMTablelocations.sAddress2
				+ ", " + SMTablelocations.sAddress3
				+ ", " + SMTablelocations.sAddress4
				+ ", " + SMTablelocations.sCity
				+ ", " + SMTablelocations.sState
				+ ", " + SMTablelocations.sCountry
				+ ", " + SMTablelocations.sZip
				+ ", " + SMTablelocations.sContact
				+ ", " + SMTablelocations.sPhone
				+ ", " + SMTablelocations.sFax
				+ " FROM " + SMTablelocations.TableName
				+ " ORDER BY " + SMTablelocations.sLocation
				;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
													 getServletContext(), 
													 smmaster.getsDBID(), 
													 "MySQL", 
													 this.toString() + " [1354206884] SQL: " + SQL 
													 );
			while (rs.next()){
				iCounter++;
				slocationnames += "slocationnames[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sCompanyDescription).trim().replace("\"", "'") + "\";\n";
				saddress1s += "saddress1s[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sAddress1).trim().replace("\"", "'") + "\";\n";
				saddress2s += "saddress2s[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sAddress2).trim().replace("\"", "'") + "\";\n";
				saddress3s += "saddress3s[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sAddress3).trim().replace("\"", "'") + "\";\n";
				saddress4s += "saddress4s[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sAddress4).trim().replace("\"", "'") + "\";\n";
				scities += "scities[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sCity).trim().replace("\"", "'") + "\";\n";
				sstates += "sstates[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sState).trim().replace("\"", "'") + "\";\n";
				scountries += "scountries[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sCountry).trim().replace("\"", "'") + "\";\n";
				spostalcodes += "spostalcodes[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sZip).trim().replace("\"", "'") + "\";\n";
				sfaxes += "sfaxes[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sFax).trim().replace("\"", "'") + "\";\n";
				scontacts += "scontacts[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sContact).trim().replace("\"", "'") + "\";\n";
				sphones += "sphones[\"" + rs.getString(SMTablelocations.sLocation).trim() + "\"] = \"" + rs.getString(SMTablelocations.sPhone).trim().replace("\"", "'") + "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading locations for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var slocationnames = new Array(" + Integer.toString(iCounter) + ")\n";
			s += slocationnames + "\n";
			
			s += "var saddress1s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += saddress1s + "\n";
			
			s += "var saddress2s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += saddress2s + "\n";
			
			s += "var saddress3s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += saddress3s + "\n";
			
			s += "var saddress4s = new Array(" + Integer.toString(iCounter) + ")\n";
			s += saddress4s + "\n";
			
			s += "var scities = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scities + "\n";
			
			s += "var sstates = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sstates + "\n";
			
			s += "var scountries = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scountries + "\n";
			
			s += "var spostalcodes = new Array(" + Integer.toString(iCounter) + ")\n";
			s += spostalcodes + "\n";
			
			s += "var sfaxes = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sfaxes + "\n";
			
			s += "var scontacts = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scontacts + "\n";
			
			s += "var sphones = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sphones + "\n";
			
		}
		
		s += "\n";
		
		//load shipvia list
		int iShipviaCounter = 0;
		String sshipvianames = "";
		
		SQL = "SELECT *"
				+ " FROM " + SMTableicshipvias.TableName
				+ " ORDER BY " + SMTableicshipvias.sshipviacode
				;

			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
														 getServletContext(), 
														 smmaster.getsDBID(), 
														 "MySQL", 
														 this.toString() + " [1354206884] SQL: " + SQL 
														 );
				while (rs.next()){
					iShipviaCounter++;
					sshipvianames += "sshipvianames[\"" + rs.getString(SMTableicshipvias.sshipviacode).trim() + "\"] = \"" + rs.getString(SMTableicshipvias.sshipvianame).trim().replace("\"", "'") + "\";\n";
				}
				rs.close();
			} catch (SQLException e) {
				throw e;
			}
		//Create the arrays, if there are any:
		if (iShipviaCounter > 0){
			s += "var sshipvianames = new Array(" + Integer.toString(iShipviaCounter) + ")\n";
			s += sshipvianames + "\n";
		}
		s += "\n";

		s += "function billtoChange(selectObj) {\n" 
		// get the index of the selected option 
		//+ "    alert('billtoChange');\n"
		+ "    var idx = selectObj.selectedIndex;\n"
		//+ "    alert('idx = ' + idx);\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "    alert('which = ' + which);\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != ''){\n"
		//+ "        alert('ICPOHeader.Paramsbillname changes to: ' + slocationnames[which]);\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillname + "\"].value = slocationnames[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbilladdress1 + "\"].value = saddress1s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbilladdress2 + "\"].value = saddress2s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbilladdress3 + "\"].value = saddress3s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbilladdress4 + "\"].value = saddress4s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillcity + "\"].value = scities[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillstate + "\"].value = sstates[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillpostalcode + "\"].value = spostalcodes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillfax + "\"].value = sfaxes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillcontactname + "\"].value = scontacts[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillphone + "\"].value = sphones[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsbillcountry + "\"].value = scountries[which];\n"
		+ "    }\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 

		s += "function shiptoChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != ''){\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipname + "\"].value = slocationnames[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipaddress1 + "\"].value = saddress1s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipaddress2 + "\"].value = saddress2s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipaddress3 + "\"].value = saddress3s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipaddress4 + "\"].value = saddress4s[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipcity + "\"].value = scities[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipstate + "\"].value = sstates[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshippostalcode + "\"].value = spostalcodes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipfax + "\"].value = sfaxes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipcontactname + "\"].value = scontacts[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipphone + "\"].value = sphones[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipcountry + "\"].value = scountries[which];\n"
		+ "    }\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 
		
		//load shipvia
		s += "function shipviaChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != '' && which != '0'){\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + ICPOHeader.Paramsshipvianame + "\"].value = sshipvianames[which];\n"
		+ "    }\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 

		s += "function phaseChange(selectObj) {\n" 
		//+ "    alert('billtoChange');\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 	
		
		s += "</script>\n\n";
		
		s += "<style>"
		+".ui-sortable-helper {\n" 
		+ " display: table;\n"
		+"}\n"  
		+ "</style>\n\n";
		
		return s;	
	}
	
	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
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
		
		/*
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
		*/
		//This is the def for a table cell, left justified:
		s +=
			"td.leftjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + "black" + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a table cell, right justified:
		s +=
			"td.rightjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + "black" + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, center justified:
		s +=
			"td.centerjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + "black" + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading on a table:
		s +=
			"td.leftjustifiedheading {"
			+ "border: 1px solid;"
			//+ "border-left: 0px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s +=
			"td.handleheading {"
			+ "border: 0px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
	private void listVendorReturns(
			PrintWriter out, 
			ServletContext context, 
			String sDBID, 
			String sUserID,
			ICPOHeader entry,
			String sLicenseModuleLevel
			){


		out.println("<br><b><u><FONT SIZE=2>Vendor Returns</FONT></u></b><BR>");

		if (entry.getsID().compareToIgnoreCase("") != 0){
			String SQL = "SELECT"
					+ " " + SMTablevendorreturns.TableName + "." + SMTablevendorreturns.iponumber
					+ " , " + SMTablevendorreturns.TableName + "." + SMTablevendorreturns.lid
					+ " FROM " + SMTablevendorreturns.TableName
					+ " WHERE " + SMTablevendorreturns.TableName + "." + SMTablevendorreturns.iponumber 
					+ " = " + entry.getsID()
					;

			boolean bEditVendorReturns  = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.SMEditVendorReturns, 
							sUserID, 
							context,
							sDBID,
							sLicenseModuleLevel
							); 
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						context, 
						sDBID, 
						"MySQL", 
						SMUtilities.getFullClassName(this.toString() + " - userID: " + sUserID)
						);

				if(rs.next() != false) {
					out.println("<TABLE BORDER=0 cellspacing=0 cellpadding=1 style= \""
							+ " background-color: " + RECEIPTS_TABLE_BG_COLOR + "; \" ><TR>");
					out.println("<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Vendor Return Link(s)</B></FONT></TD>");				
					out.println("</TR>");
					rs.beforeFirst();


					boolean bOddRow = false;
					String sBackgroundColor= "";
					while (rs.next()){
						if(bOddRow){
							sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
						}else{
							sBackgroundColor = "\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"";
						}
						//Because we are getting a 'SUM' in this query, it will return one record every time, even if there are no
						//receipts at all.  So we have to add this check to make sure that we have a 'real' receipt:
						out.println("<TR  bgcolor =" + sBackgroundColor +">");
						//Line number:
						int MRID = rs.getInt(SMTablevendorreturns.TableName + "." + SMTablevendorreturns.lid); 
						String sMRIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditVendorReturnEdit"
						+ "?lid=" + MRID
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(Integer.toString(MRID)) + "</A>";

						if (bEditVendorReturns){
							out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sMRIDLink + "</FONT></TD>");	
						}else{
							out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + MRID + "</FONT></TD>");
						}

						out.println("</TR>");
						bOddRow = !bOddRow;

					}
					out.println("</TABLE>");
				}

				rs.close();
			} catch (SQLException e) {
				System.out.println("[1579191570] In " + this.toString() + "Error [1574190570] with SQL: " + SQL + " - " + e.getMessage());
			}
		}


	}
	
	
	private String getGDocUploadLink( 
		boolean bUseGoogleDrivePicker,
		String sPONumber, 
		String sDBID, 
		ServletContext context, 
		HttpServletRequest req,
		String sUser) throws Exception{
		
		if(bUseGoogleDrivePicker){
			return "<a onclick=\"loadPicker()\" href=\"#\">Upload File(s) to Google Drive</a>";	
		}
		
		ICOption icopt = new ICOption();
		SMOption smopt = new SMOption();		
		try {
			smopt.load(sDBID, getServletContext(), sUser);
		} catch (Exception e1) {
			throw new Exception("Error [1452004984] getting SM Options for GDoc link on PO Number '" 
				+ sPONumber + "' - " + e1.getMessage());
		}
		try {
			icopt.load(sDBID, getServletContext(), sUser);
		} catch (Exception e1) {
			throw new Exception("Error [1452004985] getting IC Options for GDoc link on PO Number '" 
					+ sPONumber + "' - " + e1.getMessage());
		}
		
	    String sFolderName =  icopt.getgdrivepurchaseordersfolderprefix()
				+ sPONumber
				+ icopt.getgdrivepurchaseordersfoldersuffix();

	    String sUploadFileLink = smopt.getgdriveuploadfileurl() + "?"
	    		+ SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + icopt.getgdrivepurchaseordersparentfolderid()
	    		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sFolderName
	    		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + SMUtilities.getCreateGDriveReturnURL(req, getServletContext())
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.PO_RECORD_TYPE_PARAM_VALUE
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + sPONumber
	    		;
		return "&nbsp;&nbsp;<A HREF=\"" + sUploadFileLink + "\">Upload File(s) to Google Drive</a>";
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
