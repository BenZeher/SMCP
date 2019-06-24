package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderDetail;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablematerialreturns;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableworkorderdetails;
import SMDataDefinition.SMTableworkorders;
import SMDataDefinition.SMTableworkperformedcodes;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smar.SMOption;
import smic.ICItem;

public class SMWorkOrderEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION = "REMOVEWOATTRIBUTE";

	//Commands:
	public static final String ASYNC_POST = "ASYNCPOST";
	public static final String POST_BUTTON_LABEL = "Post <B><FONT COLOR=RED>w</FONT></B>ork order"; //W
	public static final String POSTCOMMAND_VALUE = "POSTWORKORDER";
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String CALCULATE_TOTALS_BUTTON_LABEL = "Calculate"; 
	public static final String ACCEPTSIGNATURE_BUTTON_LABEL = "<B><FONT COLOR=RED>A</FONT></B>ccept signature"; //A
	public static final String ACCEPTSIGNATURECOMMAND_VALUE = "SIGN";
	public static final String VIEW_PRICING_LABEL = "<B><FONT COLOR=RED>V</FONT></B>iew current prices"; //V
	public static final String VIEW_PRICING_COMMAND_VALUE = "VIEWPRICING";
	public static final String REMOVE_PRICING_LABEL = "<B><FONT COLOR=RED>R</FONT></B>emove current prices"; //R
	public static final String REMOVE_PRICING_COMMAND_VALUE = "HIDEPRICING";
	public static final String VIEW_PRICING_FLAG = "VIEWPRICINGFLAG";
	public static final String PRINT_RECEIPT_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>rint receipt"; //P
	public static final String PRINTRECEIPTCOMMAND_VALUE = "PRINTWORKORDERRECEIPT";
	public static final String RECENT_ITEMS_BUTTON_LABEL = "Most recent items";
	public static final String RECENTITEMSCOMMAND_VALUE = "RECENTITEMS";
	public static final String RETURNINGFROMRECENTITEMS_PARAM = "RETURNINGFROMRECENTITEMS";
	public static final String EMAIL_RECEIPT_BUTTON_LABEL = "E<B><FONT COLOR=RED>m</FONT></B>ail receipt"; //M
	public static final String EMAILRECEIPTCOMMAND_VALUE = "EMAILWORKORDERRECEIPT";
	public static final String EMAIL_TO_FIELD = "EMAILTO";
	public static final String MATERIAL_RETURN_BUTTON_LABEL = "Materia<B><FONT COLOR=RED>l</FONT></B> return"; //L
	public static final String MATERIALRETURNCOMMAND_VALUE = "CREATEMATERIALRETURN";
	public static final String MATERIAL_RETURN_DESCRIPTION_FIELD = "MATERIALRETURNDESCRIPTION";
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to <B><FONT COLOR=RED>G</FONT></B>oogle Drive"; //G
	public static final String FINDITEM_BUTTON_LABEL = "Find item";
	public static final String FINDITEM_COMMAND_VALUE_BASE = "FINDITEM";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String WORK_PERFORMED_CHECKBOX = "WPCCHECKBOX";
	private static final int NUMBER_OF_BLANK_LINES = 6;
	public static final String SIGNATURE_JSON_OUTPUT_FIELD_NAME = "outputsignature";
	private static final String FORM_IS_FULLY_DISPLAYED_FIELD = "FORMISFULLYDISPLAYEDFIELD";
	private static final String FORM_IS_FULLY_DISPLAYED = "FULLYDISPLAYED";
	//This will be used to log if anyone tried to save before the screen was fully displayed:
	public static final String FULLY_DISPLAYED_WARNING_FIELD = "FULLYDISPLAYEDWARNINGTRIGGERED";
	public static final String FULLY_DISPLAYED_WARNING_VALUE_YES = "YES";
	public static final String FULLY_DISPLAYED_WARNING_VALUE_NO = "NO";
	

	//This controls if the signature box should be displayed or not.
	private boolean m_bDisplaySigantureBox;
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
				"smcontrolpanel.SMWorkOrderAction",
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
			smedit.getPWOut().println("Error [1391791199] loading work order from request: " + e2.getMessage());
			return;
		}

		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a work order object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (!clsServletUtilities.isSessionValid(currentSession)){
			smedit.getPWOut().println("Error [1425683458] - session is invalid.");
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
			smedit.getPWOut().println("Error [1415807186] getting session attribute - " + e1.getMessage());
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
				smedit.getPWOut().println("Error [1428340323] work order ID in request ('" +  sWOIDFromRequest 
					+ "') and work order ID from session object ('" + sWOIDFromSession + "') do not match.");
				return;
			}
			
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			try {
				if(!wohead.load(smedit.getsDBID(), smedit.getFullUserName(), getServletContext())){
					smedit.getPWOut().println("Error [1425920366] work order # " + wohead.getlid() + "' could not be loaded.");
					return;
				}
			} catch (Exception e) {
				smedit.getPWOut().println("Error [1391545542] loading work order - " + e.getMessage());
				return;
			}
		}

		//Set booleans that control screen layout/content
		m_bDisplaySigantureBox = wohead.isWorkOrderPosted();	
		
	    smedit.getPWOut().println(getHeaderString(
			"Work order edit", 
			"", 
			SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
			SMUtilities.DEFAULT_FONT_FAMILY,
			sCompanyName
			))
		;

		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		boolean bUseGoogleDrivePicker = false;
		String sPickerScript = "";
			try {
			 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
						SMCreateGoogleDriveFolderParamDefinitions.WORK_ORDER_TYPE_PARAM_VALUE,
						wohead.getlid(),
						getServletContext(),
						smedit.getsDBID())
						;
			} catch (Exception e) {
				System.out.println("[1554818420] - Failed to load drivepicker.js - " + e.getMessage());
			}
	
			if(sPickerScript.compareToIgnoreCase("") != 0) {
				smedit.getPWOut().println(sPickerScript);
				bUseGoogleDrivePicker = true;
			}
			
	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		smedit.getPWOut().println("<B><FONT COLOR=\"RED\"><div id=\"Warning\">");
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("WARNING:" + sWarning);
		}
		smedit.getPWOut().println("</div></FONT></B>");
		
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		smedit.getPWOut().println("<B><div id=\"Status\">");
		if (sStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("" + sStatus + "");
		}		
		smedit.getPWOut().println("</div></B>");
		
	    //Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "\">Return to user login</A><BR><BR>");
	    
		try{
			smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
				+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
			);
			createEditPage(
				getEditHTML(smedit, wohead, SMTableworkorders.ObjectName, bUseGoogleDrivePicker),
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
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		pwOut.println("</FORM>");
		pwOut.println("");
	}
	
	private String getEditHTML(SMMasterEditEntry sm, SMWorkOrderHeader wo_entry, String sObjectName, boolean bUseGoogleDrivePicker) throws Exception{
		
		//Flag to tell if the command have already been displated:
		boolean bCommandsHaveAlreadyBeenDisplayed = false;
		
		//First, load the mechanics:
		ArrayList<String> arrMechanicsInitials = new ArrayList<String>(0);
		ArrayList<String> arrMechanicsNames = new ArrayList<String>(0);
		ArrayList<String> arrMechanicIDs = new ArrayList<String>(0);
		try {
			loadMechanics(
				arrMechanicsInitials, 
				arrMechanicsNames, 
				arrMechanicIDs,
				getServletContext(), 
				sm.getsDBID(), 
				sm.getUserID(),
				sm.getFullUserName());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		SMOrderHeader orderheader = new SMOrderHeader();
		orderheader.setM_strimmedordernumber(wo_entry.getstrimmedordernumber());
		if (wo_entry.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			if (!orderheader.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
				throw new Exception("Could not load order header '" + wo_entry.getstrimmedordernumber()
					+ "' - " + orderheader.getErrorMessages());
			}
		}
		String s = "";
		//Record the four permissions for editing time because we'll need them again:
		boolean bAllowEditingLeftPreviousSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditLeftPreviousSiteTime, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowEditingArrivedAtCurrentSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditArrivedAtCurrentSiteTime, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowEditingLeftCurrentSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditLeftCurrentSiteTime, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowArrivedAtNextSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditArrivedAtNextSiteTime, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		s += sCommandScripts(wo_entry, sm,
				bAllowEditingLeftPreviousSiteTime,
				bAllowEditingArrivedAtCurrentSiteTime,
				bAllowEditingLeftCurrentSiteTime,
				bAllowArrivedAtNextSiteTime);
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
		
		//Store whether we are in 'view pricing' mode:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + VIEW_PRICING_FLAG + "\" VALUE=\"" 
		+ clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_FLAG, sm.getRequest()) + "\""
		+ " id=\"" + VIEW_PRICING_FLAG + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlid + "\" VALUE=\"" 
				+ wo_entry.getlid() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramltimestamp + "\" VALUE=\"" 
				+ wo_entry.getstimestamp() + "\">";
		//Record the time the use last loaded the work order:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlastreadrecordtimestamp + "\" VALUE=\"" 
			+ wo_entry.getslastreadrecordtimestamp() + "\">" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\" VALUE=\"" 
				+ wo_entry.getstrimmedordernumber() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramiposted + "\" VALUE=\"" 
				+ wo_entry.getsposted() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramiimported + "\" VALUE=\"" 
				+ wo_entry.getsimported() + "\">";
		
		//New Row
		s += "<TR>";
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";		

		//Header information:
		s += "<TR><TD>" 
			+ createOrderHeaderTable(
				sm,
				getServletContext(),
				wo_entry, 
				orderheader, 
				SMUtilities.getFullClassName(this.toString()),
				SMWorkOrderHeader.SAVING_FROM_EDIT_SCREEN,
				bUseGoogleDrivePicker) 
			+ "</TD></TR>";

		s += createInstructionsTable(wo_entry, orderheader);

		//Create the order commands line at the top:
		s += "<TR><TD>" + createCommandsTable(
				wo_entry, 
				sm.getUserID(), 
				sm.getsDBID(),
				clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_FLAG, sm.getRequest()),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
				bCommandsHaveAlreadyBeenDisplayed,
				bUseGoogleDrivePicker) + "</TD></TR>";

		bCommandsHaveAlreadyBeenDisplayed = true;
		
		s += "<TR><TD>" + SMWorkOrderEdit.createJobEntryTimesTable(
			sm, 
			getServletContext(), 
			wo_entry, 
			SMUtilities.getFullClassName(this.toString()))
			+ "</TD></TR>";

		//Create the items table:
		//If the 'show pricing' flag has been set AND if we are in 'Edit' mode, then show the prices for the items:
		boolean bShowPrices = (clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_FLAG, sm.getRequest()).compareToIgnoreCase("") != 0);
		
		//ONLY allow the user to show prices until the work order is posted:
		if (wo_entry.isWorkOrderPosted()){
			bShowPrices = false;
		}
		boolean bAllowItemViewing =
				SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICDisplayItemInformation, 
					sm.getUserID(),
					getServletContext(),
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		boolean bAllowZeroWorkOrderItemPrices =
				SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMZeroWorkOrderItemPrices, 
					sm.getUserID(),
					getServletContext(),
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		s += "<TR><TD>" + createItemsTable(
				sm, 
				wo_entry, 
				orderheader, 
				true, 
				bShowPrices,
				bAllowZeroWorkOrderItemPrices,
				bAllowItemViewing
				) + "</TD></TR>";

		//Create work performed codes table:
		//If the work order is NOT associated with a real order, then just don't show any work performed codes at all:
		if (wo_entry.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			s += "<TR><TD>" + createWorkPerformedTable(sm, wo_entry, orderheader);
		}

		//Create the comments area table:
		s += "<TR><TD>" + createMechanicInfoTable(
				sm, 
				wo_entry,
				arrMechanicsInitials, 
				arrMechanicsNames, 
				arrMechanicIDs) + "</TD></TR>";

		s += "<TR><TD>" + createSignatureBlockTable(sm, wo_entry) + "</TD></TR>";

		//Detail sheets
		s += "<TR><TD>" + SMWorkOrderHeader.createDetailSheetsTable(sm, wo_entry, getServletContext()) + "</TD></TR>";

		//Create the order commands line at the bottom:
		s += "<TR><TD>" + createCommandsTable(
				wo_entry, 
				sm.getUserID(), 
				sm.getsDBID(),
				clsManageRequestParameters.get_Request_Parameter(VIEW_PRICING_FLAG, sm.getRequest()),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
				bCommandsHaveAlreadyBeenDisplayed,
				bUseGoogleDrivePicker) + "</TD></TR>";
		
		bCommandsHaveAlreadyBeenDisplayed = true;
		
		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		//This variable will tell us if the 'screen not fully displayed' warning was triggered:
		s += "\n"
				+ "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + FULLY_DISPLAYED_WARNING_FIELD + "\""
				+ " ID=\"" + FULLY_DISPLAYED_WARNING_FIELD + "\""
				+ " VALUE=\"" + FULLY_DISPLAYED_WARNING_VALUE_NO + "\"" + ">"
			;
		
		//This is placed at the bottom of the form so we can confirm that the screen is fully displayed in the browser:
		s += "\n"
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + FORM_IS_FULLY_DISPLAYED_FIELD + "\""
			+ " ID=\"" + FORM_IS_FULLY_DISPLAYED_FIELD + "\""
			+ " VALUE=\"" + FORM_IS_FULLY_DISPLAYED + "\"" + ">"
		;
		
		return s;
	}
	private boolean loadMechanics(
			ArrayList<String> arrMechanicsInitials,
			ArrayList<String> arrMechanicsNames,
			ArrayList<String> arrMechanicIDs,
			ServletContext context,
			String sDBID,
			String sUserID,
			String sUserFullName
	) throws Exception{

		String SQL = "SELECT"
			+ " " + SMTablemechanics.sMechFullName
			+ ", " + SMTablemechanics.sMechInitial
			+ ", " + SMTablemechanics.lid
			+ " FROM " + SMTablemechanics.TableName
			+ " ORDER BY " + SMTablemechanics.sMechInitial
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".loadMechanics - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName	);
			while (rs.next()){
				arrMechanicsInitials.add(rs.getString(SMTablemechanics.sMechInitial));
				arrMechanicsNames.add(rs.getString(SMTablemechanics.sMechFullName));
				arrMechanicIDs.add(Long.toString(rs.getLong(SMTablemechanics.lid)));
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1391538712] - Could not load mechanics - " + e.getMessage());
		}

		return true;
	}
	private String createCommandsTable(
			SMWorkOrderHeader wo_order, 
			String sUserID, 
			String sDBID, 
			String sViewFlag,
			String sLicenseModuleLevel,
			boolean bCommandsHaveAlreadyBeenDisplayed,
			boolean bUseGoogleDrivePicker){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ SMWorkOrderHeader.ORDERCOMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";

		//ACCEPT SIGNATURE BUTTON:
		//If it's NOT posted show the 'Accept Signature' button:
		if (!wo_order.isWorkOrderPosted()){
			s += createAcceptSignatureButton();
		}
			
		//SAVE button:
		//We need to be able to save no matter what:
		s += createSaveButton();
		
		//POST button:
		//If we are in 'EDIT' mode, and the work order is NOT posted, we need to be able to post:
		if (!wo_order.isWorkOrderPosted()){
			s += createPostButton();
		}
				
		//PRINT RECEIPT button:
		//Anytime it's posted, we need to be able to print:
		if (wo_order.isWorkOrderPosted()){
			s += printreceiptButton();
		}
		
		//EMAIL RECEIPT button:
		//Anytime it's posted, IF the email fields have NOT already been displayed, we need to be able to email:
		if (
			(wo_order.isWorkOrderPosted())
			&& (!bCommandsHaveAlreadyBeenDisplayed)
		){
			s += emailreceiptButton();
		}
		
		//We ALWAYS have the choice to view the prices, if we have the permission, and if we are in 'Edit' mode:
		
		if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewPricingOnWorkOrders, 
						sUserID, 
						getServletContext(), 
						sDBID,
						sLicenseModuleLevel))
				&& (!wo_order.isWorkOrderPosted())
		){
			if (SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMZeroWorkOrderItemPrices, 
						sUserID, 
						getServletContext(), 
						sDBID,
						sLicenseModuleLevel)
					){
				//No need to show the button unless it's turned off currently:
				if (sViewFlag.compareToIgnoreCase("") == 0){
					s += createViewPricingButton();
				}else{
					s += createHidePricingButton();
				}
			}else{
				//No need to show the button unless it's turned off currently:
				if (sViewFlag.compareToIgnoreCase("") == 0){
					s += createViewPricingButton();
				}else{
					s += createHidePricingButton();
				}
			}
		}
		
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMInitiateMaterialReturns, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModuleLevel)){
			//If the commands have NOT already been displayed (we can't have the material returns description box appear twice):
			if (!bCommandsHaveAlreadyBeenDisplayed){
				s += "<BR>" + createMaterialReturnButton();
			}
		}
		
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDriveWorkOrderFolders, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModuleLevel)){
			s += "&nbsp;" + createAndUploadFolderButton(bUseGoogleDrivePicker);
		}
		
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewWorkOrderDocuments, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModuleLevel)){
			
			
			String sGDocLink = wo_order.getsgdoclink();
			if (sGDocLink == null){
				sGDocLink = "";
			}
			if(sGDocLink.compareToIgnoreCase("") != 0){
				s += "&nbsp;" + viewGDriveFolderButton(sGDocLink);
			}
		}	
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		return s;
	}
	private String createCalculateTotalsButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ CALCULATE_TOTALS_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createPostButton(){
		return "<button type=\"button\""
				+ " value=\"" + POST_BUTTON_LABEL + "\""
				+ " name=\"" + POST_BUTTON_LABEL + "\""
				+ " onClick=\"postworkorder();\">"
				+ POST_BUTTON_LABEL
				+ "</button>\n"
				;
	}

	private String createAcceptSignatureButton(){
		return "<button type=\"button\""
				+ " value=\"" + ACCEPTSIGNATURE_BUTTON_LABEL + "\""
				+ " name=\"" + ACCEPTSIGNATURE_BUTTON_LABEL + "\""
				+ " onClick=\"gotosignaturemode();\">"
				+ ACCEPTSIGNATURE_BUTTON_LABEL
				+ "</button>\n"
				;
	}

	private String createViewPricingButton(){
		return "<button type=\"button\""
				+ " value=\"" + VIEW_PRICING_LABEL + "\""
				+ " name=\"" + VIEW_PRICING_LABEL + "\""
				+ " onClick=\"viewpricing();\">"
				+ VIEW_PRICING_LABEL
				+ "</button>\n"
				;
	}
	private String createHidePricingButton(){
		return "<button type=\"button\""
				+ " value=\"" + REMOVE_PRICING_LABEL + "\""
				+ " name=\"" + REMOVE_PRICING_LABEL + "\""
				+ " onClick=\"removepricing();\">"
				+ REMOVE_PRICING_LABEL
				+ "</button>\n"
				;
	}
	private String printreceiptButton(){
		return "<button type=\"button\""
				+ " value=\"" + PRINT_RECEIPT_BUTTON_LABEL + "\""
				+ " name=\"" + PRINT_RECEIPT_BUTTON_LABEL + "\""
				+ " onClick=\"print();\">"
				+ PRINT_RECEIPT_BUTTON_LABEL
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
	
	private String viewGDriveFolderButton(String sGDocLink){

		if (sGDocLink.compareToIgnoreCase("") != 0){
			return "<input type=\"button\" "
					+ "onclick=\"location.href='" + sGDocLink + "';\" "
					+ "value=\"Google Drive folder\" />"
				;
		}else{
			return "";
		}

	}
	
	private String emailreceiptButton(){
		return "<button type=\"button\""
			+ " value=\"" + EMAIL_RECEIPT_BUTTON_LABEL + "\""
			+ " name=\"" + EMAIL_RECEIPT_BUTTON_LABEL + "\""
			+ " onClick=\"email();\">"
			+ EMAIL_RECEIPT_BUTTON_LABEL
			+ "</button>\n"
			+ "&nbsp;To:&nbsp;"
			
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + EMAIL_TO_FIELD + "\""
			+ " id = \"" + EMAIL_TO_FIELD + "\""
			//+ " VALUE=\"" + "" + "\""
			+ " SIZE=" + "18"
			+ " MAXLENGTH=75"
			+ ">"
		;
	}
	private String createMaterialReturnButton(){
		return "<button type=\"button\""
			+ " value=\"" + MATERIAL_RETURN_BUTTON_LABEL + "\""
			+ " name=\"" + MATERIAL_RETURN_BUTTON_LABEL + "\""
			+ " onClick=\"createMaterialReturn();\">"
			+ MATERIAL_RETURN_BUTTON_LABEL
			+ "</button>\n"
			+ "&nbsp;Desc.:&nbsp;"
			
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + MATERIAL_RETURN_DESCRIPTION_FIELD + "\""
			+ " id = \"" + MATERIAL_RETURN_DESCRIPTION_FIELD + "\""
			//+ " VALUE=\"" + "" + "\""
			+ " SIZE=" + "30"
			+ " MAXLENGTH=" + Integer.toString(SMTablematerialreturns.sdescriptionlength)
			+ ">"
		;
	}
	
	private String recentItemsButton(){
		return "<button type=\"button\""
			+ " value=\"" + RECENT_ITEMS_BUTTON_LABEL + "\""
			+ " name=\"" + RECENT_ITEMS_BUTTON_LABEL + "\""
			+ " onClick=\"recentitems();\">"
			+ RECENT_ITEMS_BUTTON_LABEL
			+ "</button>\n"
			;
	}

	private String createItemsTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			SMOrderHeader order,
			boolean bDisplayAllItemsOnOrder,
			boolean bShowPrices,
			boolean bAllowZeroWorkOrderItemPrices,
			boolean bShowItemInformationLink) throws Exception{
		String s = "";
		
		s += "<TABLE class = \" innermost \" style=\" title:ItemsTable; background-color: "
				+ SMWorkOrderHeader.ITEMS_TABLE_BG_COLOR + "; width=100%; border-collapse:collapse;\">\n";  

		//IF we need to show prices, we are going to load the order into a new object, to be used ONLY for calculating prices and totals:
		SMOrderHeader dummyorder = new SMOrderHeader();
		dummyorder.setM_strimmedordernumber(order.getM_strimmedordernumber());
		if (order.getM_strimmedordernumber().compareToIgnoreCase("") != 0){
			if (!dummyorder.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getsDBID())){
				throw new Exception("Error loading order to calculate prices - " + dummyorder.getErrorMessages() + ".");
			}
		}
		//First, remove all the lines on the dummy order so we can use it to recalculate only the items on the work order:
		dummyorder.getM_arrOrderDetails().clear();

		//If the work order is posted, then we build the posted work order layout:
		if (workorder.isWorkOrderPosted()){
			s += displayItemsOnPostedWorkOrder(workorder, order, sm, bShowItemInformationLink);
		}else{
			//If it's not posted, then if it's the 'EDIT' view, we build the 'edit' layout:
			s += displayItemsForEditing(workorder, order, dummyorder, sm, bShowPrices, bAllowZeroWorkOrderItemPrices, bShowItemInformationLink);
		}
		//Close the table:
		s += "</TABLE style = \" title:ItemsTable; \">\n";
		
		if (bShowPrices){
			s += "<TABLE class = \" innermost \" style=\" title:ItemTotalsTable; background-color: "
					+ SMWorkOrderHeader.ITEMS_TABLE_BG_COLOR + "; \" >\n";
			//Show the totals table here:
			BigDecimal bdShippedValue = new BigDecimal("0.00");
			BigDecimal bdTotalExtendedMaterialPrice = new BigDecimal("0.00");
			BigDecimal bdTotalExtendedLaborPrice = new BigDecimal("0.00");
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
			bdTotalExtendedMaterialPrice = bdShippedValue.subtract(bdTotalExtendedLaborPrice);
			s += "<TR>"
					+ "<TD align=right><FONT SIZE=2><B>Material total:</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalExtendedMaterialPrice) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
			s += "<TR>"
					+ "<TD align=right><FONT SIZE=2><B>Labor total:</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalExtendedLaborPrice) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
			s += "<TR>"
				+ "<TD align=right><FONT SIZE=2><B>Extended price total:</B></FONT></TD>"
				+ "<TD align=right>"
				+ "<FONT SIZE=2>" 
				+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue) + "</B></FONT>"
				+ "</TD>"
				+ "</TR>"
			;
			
			//Add a row for the total discounted amount and percentage:
			BigDecimal bdDiscountedAmount = new BigDecimal(dummyorder.getM_dPrePostingInvoiceDiscountAmount().replace(",",""));
			if (bdDiscountedAmount.compareTo(BigDecimal.ZERO) != 0){
				s += "<TR>"
					+ "<TD align=right><FONT SIZE=2><B>" 
						+ dummyorder.getM_sPrePostingInvoiceDiscountDesc() + " (" 
						+ dummyorder.getM_dPrePostingInvoiceDiscountPercentage() + "%)" + "</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>-" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountedAmount) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
			
				//Add a row for the sub total after discount:
				s += "<TR>"
					+ "<TD align=right ><FONT SIZE=2><B>Subtotal after discount:</B></FONT></TD>"
					+ "<TD align=right>"
					+ "<FONT SIZE=2>" 
					+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount)) + "</B></FONT>"
					+ "</TD>"
					+ "</TR>"
				;
			}

			//Add a row for the tax:
			String sTaxAmount;
			try {
				sTaxAmount = dummyorder.getTaxAmount(sm.getsDBID(), sm.getUserName(), getServletContext());
			} catch (Exception e) {
				sTaxAmount = e.getMessage();
			}
			s += "<TR>"
				+ "<TD align=right><FONT SIZE=2><B>Estimated Tax (calculated only on " 
				+ "shipped items that are TAXABLE):</B></FONT></TD>"
				+ "<TD align=right>"
				+ "<FONT SIZE=2>" 
				+ "<B>" + sTaxAmount + "</B></FONT>"
				+ "</TD>"
				+ "</TR>"
			;
			
			//Add a row for total INCLUDING tax:
			String sTotalWithTax = "";
			try {
				sTotalWithTax = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount).add(new BigDecimal(sTaxAmount.replace(",", ""))));
			} catch (Exception e) {
				s += "Error [1390339789] calculating Total With Tax - " + e.getMessage();
			}
			s += "<TR>"
				+ "<TD align=right><FONT SIZE=2><B>Extended price total INCLUDING discount and tax:</B></FONT></TD>"
				+ "<TD align=right >"
				+ "<FONT SIZE=2>" 
				+ "<B>" + sTotalWithTax + "</B></FONT>"
				+ "</TD>"
				+ "</TR>"
			;
			
			//If we can set prices to to 0.00 then create a button to recalculate total
			if(bAllowZeroWorkOrderItemPrices){
				s += "<TR>"
					+ "<TD></TD>"
					+ "<TD align=right >"
					+ "<BR>" + createCalculateTotalsButton()
					+ "</TD>"
					+ "</TR>"
				;
			}
			
			s += "</TABLE style = \" title:ItemTotalsTable; \">\n";
		}
		return s;
	}

	private String displayItemsForEditing(
			SMWorkOrderHeader workorder,
			SMOrderHeader order,
			SMOrderHeader dummyorder,
			SMMasterEditEntry sm,
			boolean bShowPrices,
			boolean bAllowZeroWorkOrderItemPrices,
			boolean bAllowItemViewing
			) throws Exception{
		String s = "";
		//Headings:
		s += "<TR>";
		s += "<TD class=\" fieldleftheading \">Qty used:&nbsp;</TD>";
		s += "<TD class=\" fieldrightheading \">Qty assigned:&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Item #:&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Description:&nbsp;</TD>";
		if(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditLocationOnWorkOrderDetail, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += "<TD class=\" fieldleftheading \">Location:&nbsp;</TD>";
		}
		s += "<TD class=\" fieldleftheading \">UOM:&nbsp;</TD>";
		;
		if (bShowPrices){
			if (bAllowZeroWorkOrderItemPrices){
				s += "<TD class=\" fieldrightheading \">Set&nbsp;to<BR>zero:</TD>";
			}
			s += "<TD class=\" fieldrightheading \">Extended<BR>price:&nbsp;</TD>";
		}
		s += "</TR>"
		;
		
		//Display each of the items on the order:
		int iNumberOfItemLines = 0;
		
		//Display all the items on the work order:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			//IF it's an item, not a work performed code:
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0){
				
				//We need to add a dummy line to the dummy order, so we can calculate prices and totals:
				SMOrderDetail dummydetail = new SMOrderDetail();
				dummydetail.setM_dQtyOrdered(workorder.getDetailByIndex(i).getsbdquantity().replace(",",""));
				dummydetail.setM_dQtyShipped(workorder.getDetailByIndex(i).getsbdquantity().replace(",",""));
				dummydetail.setM_dUniqueOrderID(order.getM_siID());
				dummydetail.setM_strimmedordernumber(order.getM_strimmedordernumber());
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
					actualorderdetail = order.getOrderDetailByDetailNumber(workorder.getDetailByIndex(i).getsorderdetailnumber());
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
						order.calculateExtendedPrice(dummydetail);
					} catch (Exception e) {
						throw new SQLException(e.getMessage() + ".");
					}
				}else{
					//Otherwise, just calculate the price for this item and this qty, disregarding the unit price on the order:
					try {
						dummyorder.updateLinePrice(dummydetail, sm.getsDBID(), sm.getUserName(), getServletContext());
					} catch (Exception e) {
						throw new Exception ("Error [1431442449] updating price for item '" + dummydetail.getM_sItemNumber() + "' - " + e.getMessage());
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
				iNumberOfItemLines++;
				s += buildItemLineForEditing(
					bShowPrices,
					bAllowZeroWorkOrderItemPrices,
					bAllowItemViewing,
					workorder.getDetailByIndex(i),
					order,
					iNumberOfItemLines,
					sm
				);
			}
		}
		//Finally, if the work order is editable, we'll display a few blank lines so that new items can be added to the work order:
		String sDefaultItemLocation = order.getM_sLocation();
		
		//If the work order is NOT associated with a real order, then don't allow the user ANY lines for adding items:
		if (workorder.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			for (int i = 0; i < NUMBER_OF_BLANK_LINES; i++){
				iNumberOfItemLines++;
				s += buildblankItemLine(iNumberOfItemLines, sm, sDefaultItemLocation, bShowPrices);
			}
		}
		
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED + "\" VALUE=\"" + Integer.toString(iNumberOfItemLines) + "\"" + ">";

		if(SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMDisplayMostUsedItemsOnWorkOrder, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){					
		//Display most recently used item button and No. of items and days text box
		s += "<TR>"
				+ "<TD COLSPAN=100%><HR></TD>"
			+ "</TR>"
			+ "<TR>"
			+ "<TD COLSPAN=100%>"
				+ recentItemsButton() 
				+ "No. of Days: <INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramsnumberofdays 
				+ "\" MAXLENGTH=\"3\""
				+ "VALUE=\"" + workorder.getsnumberofdays() + "\" style=\"width: 80px;\">" 
				+ "  No. of Items: <INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramsnumberofitems 
				+ "\" MAXLENGTH=\"3\""
				+ " VALUE=\"" + workorder.getsnumberofitems() + "\" style=\"width: 80px;\">"
				+ "</TD>"				
			+ "</TR>";
		}
			return s;		
		}
	private String displayItemsOnPostedWorkOrder(
			SMWorkOrderHeader workorder,
			SMOrderHeader order,
			SMMasterEditEntry sm,
			boolean bShowItemInformationLink
			) throws Exception{
		String s = "";
		//Headings:
		
		s += "<TR><TD ><B><U>ITEMS/LABOR USED:</U></B></TD></TR>";
		s += "<TR>";
		s += "<TD class=\" fieldrightheading \">Qty used&nbsp;</TD>";
		s += "<TD class=\" fieldrightheading \">Qty assigned&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Item #&nbsp;</TD>";
		s += "<TD class=\" fieldleftheading \">Description&nbsp;</TD>";
		if(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditLocationOnWorkOrderDetail, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
		s += "<TD class=\" fieldleftheading \">Location&nbsp;</TD>";
		}
		s += "<TD class=\" fieldleftheading \">UOM&nbsp;</TD>";

		s += "</TR>"
		;

		//Display each of the items on the order:
		int iNumberOfItemLines = 0;
		for (int i = 0; i < workorder.getDetailCount(); i++){
			//IF it's an item, not a work performed code:
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0){

				iNumberOfItemLines++; //This will be one-based, rather than zero-based

				s += buildItemLineForPostedWorkOrder(
					bShowItemInformationLink,
					workorder.getDetailByIndex(i),
					order,
					iNumberOfItemLines,
					sm,
					sm.getsDBID()
				);
			}
		}
				
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.NUMBER_OF_ITEM_LINES_USED + "\" VALUE=\"" + Integer.toString(iNumberOfItemLines) + "\"" + ">";
		return s;
	}
	
	private String buildItemLineForEditing(
		boolean bShowPrices,
		boolean bAllowZeroWorkOrderItemPrices,
		boolean bAllowItemViewing,
		SMWorkOrderDetail wodetail,
		SMOrderHeader order,
		int iLineNumber,
		SMMasterEditEntry sm
		) throws Exception{
		String s = "";
		if(iLineNumber % 2 == 0){
		s += "<TR bgcolor=" + SMWorkOrderHeader.ITEMS_TABLE_ODD_ROW_COLOR + ">";
		}else{
		s += "<TR>";
		}
		int iColumnCount = 0;
		//QTY
		String sQty = wodetail.getsbdquantity();
		BigDecimal bdQty;
		try {
			bdQty = new BigDecimal(wodetail.getsbdquantity().replace(",", ""));
			if (bdQty.compareTo(BigDecimal.ZERO) == 0){
				sQty = "";
			}
		} catch (Exception e1) {
			//Do nothing here - we just carry the value on...
		}
		s += "<TD class=\" fieldcontrol \" >"
		+ "<INPUT TYPE=TEXT NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdquantity + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdquantity + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(sQty) + "\""
		+ " onchange=\"flagDirty();\""
		+ " SIZE=" + SMWorkOrderHeader.ITEM_QTY_FIELD_WIDTH
		+ " MAXLENGTH=13"
		+ ">"
		+ "</TD>"
		+ "\n"
		;
		iColumnCount++;
		//QTY ASSIGNED:
		s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdqtyassigned() + "</TD>" + "\n";
		iColumnCount++;
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdqtyassigned + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdqtyassigned + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsbdqtyassigned().replace(",","") + "\""
		+ ">" + "\n"
		;
		
		//ITEM:
		
		//If there is no line id, then this line is not saved and it has to be editable:
		if (wodetail.getslid().compareToIgnoreCase("-1") == 0){
			s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT  TYPE=TEXT NAME=\""
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber
			+ "\""
				
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber
			+ "\""
			;

			//If we have returned from a call to the finder, this field could have a value in it:
			String sItemNumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER + clsStringFunctions.PadLeft(Integer.toString(iLineNumber),
					"0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) +  SMWorkOrderDetail.Paramsitemnumber, sm.getRequest());
			if (sItemNumber.compareToIgnoreCase("") == 0){
				sItemNumber = wodetail.getsitemnumber();
			}
			s += " VALUE=\"" 
				+ clsStringFunctions.filter(sItemNumber)
			+ "\"";
			
			s += " onchange=\"flagDirty();\""
			+ " SIZE=" + "7"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderdetails.sItemNumberLength)
			+ ">"
			+ "\n"
			;
			
			//Add a button for the finder:
			s += "<button type=\"button\""
			+ " value=\"" 
				+ FINDITEM_BUTTON_LABEL 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+ "\""
			+ " name=\"" 
				+ FINDITEM_BUTTON_LABEL 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+ "\""
			+ " onClick=\"finditem('" 
				+ FINDITEM_COMMAND_VALUE_BASE 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)
			+ "');\">"
			+ FINDITEM_BUTTON_LABEL
			+ "</button>\n"
			
			+ "</TD>"
			+ "\n"
			;
			iColumnCount++;
		}else{
			//Need a link to the item information here:
			String sItemNumberLink = wodetail.getsitemnumber();
			if (bAllowItemViewing){
				sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICDisplayItemInformation?ItemNumber=" 
				+ wodetail.getsitemnumber()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() 
				+ "\">" + wodetail.getsitemnumber() + "</A>";
			}
			
			s += "<TD class=\"readonlyleftfield\">" + sItemNumberLink
				+ " <INPUT TYPE=HIDDEN NAME=\"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramsitemnumber + "\""
				+ " id = \"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramsitemnumber + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemnumber()) + "\""
				+ ">"
			+ "</TD>" + "\n"
			;
			iColumnCount++;
		}
		
		//Item description COLUMN
		//If there is no line id, then this line is not saved and it has to be editable:
		if (wodetail.getslid().compareToIgnoreCase("-1") == 0){
			s += "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\""
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc
			+ "\""
				
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc
			+ "\""
			;
			s += " VALUE=\"" 
				+ clsStringFunctions.filter(wodetail.getsitemdesc())
			+ "\"";
			
			s += " onchange=\"flagDirty();\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTableorderdetails.sItemDescLength)
			+ ">"
			+ "</TD>"
			+ "\n"
			;
			iColumnCount++;
		}else{
			
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemdesc()
				+ " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc+ "\""
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc + "\""
			+ "\""
			+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemdesc()) + "\""
			+ ">"
			+ "</TD>" + "\n"
			;
			iColumnCount++;
		}
		if(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditLocationOnWorkOrderDetail, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			//Location drop down selection:
			s += "<TD class=\" fieldcontrol \">";
		    ArrayList<String> arrValues = new ArrayList<String>(0);
	        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
	        //arrValues.add("");
	        //arrDescriptions.add("None");
	        try{
				//Get the record to edit:
		        String sSQL = " SELECT * " + "FROM " + SMTablelocations.TableName;
		        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sm.getsDBID(),
		        	"MySQL",
		        	this.toString() + ".buildItemLineForEditing - User: " + sm.getUserID()
		        	+ " - "
		        	+ sm.getFullUserName()
		        		);
		        while (rsLocations.next()){
		        	arrValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
		        	arrDescriptions.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
				}
		        rsLocations.close();
			}catch (SQLException ex){
				s += "<FONT COLOR=RED><B>Error [1457017406] reading locations - " + ex.getMessage() + ".</FONT></B>";
			}       
	         
			String sFieldName = SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramslocationcode ;
			s += clsCreateHTMLFormFields.Create_Edit_Form_List_Field(sFieldName,
					 								arrValues,
					 								wodetail.getslocationcode(),
					 								arrDescriptions);	
			s += "</TD>";
		
		}else{
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramslocationcode + "\""
			+ " ID = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramslocationcode + "\""
				+ "\""
			+ " VALUE=\"" + wodetail.getslocationcode() + "\""+ ">"
				+ "\n"
				;
		}
		//Unit of measure COLUMN
		if (wodetail.getslid().compareToIgnoreCase("-1") == 0){
			s += "<TD class=\"readonlyleftfield\">" + "&nbsp;" + "</TD>" + "\n";
			iColumnCount++;
		}else{
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsuom() + "</TD>" + "\n";
			iColumnCount++;
		}
		//In any case, store the UOM:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsunitofmeasure+ "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsunitofmeasure + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsuom()) + "\""
		+ ">"
		+ "\n"
		;

		//IF we are supposed to show prices, add a column for that here:
		if (bShowPrices){
			//Set to zero column
			String sSetToZeroCheckboxFieldname = SMWorkOrderHeader.SET_TO_ZERO_CHECKBOX_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)
				+  SMWorkOrderDetail.Paramllsetpricetozero;
			if (bAllowZeroWorkOrderItemPrices && Integer.parseInt(wodetail.getsorderdetailnumber()) < 0){
				s += "<TD class=\"readonlyrightfield\" style = \"vertical-align:top;\" ><INPUT TYPE=CHECKBOX";
				if (wodetail.getssetpricetozero().compareToIgnoreCase("1") == 0){
					s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}
				s += " NAME=\"" + sSetToZeroCheckboxFieldname + "\""
					+ " ID = \"" + sSetToZeroCheckboxFieldname + "\""
					+ " width=0.25>"
					+ "</TD>"
				;
			}else {
				s += "<TD></TD>";
			}
			//Extended price COLUMN
			s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdextendedprice() + "</TD>" + "\n";
			iColumnCount++;
		}else{
			//Set to zero column
			//Since this is normally a 'checkbox' field, we'll only include it if the value is 'checked':
			String sSetToZeroCheckboxFieldname = SMWorkOrderHeader.SET_TO_ZERO_CHECKBOX_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)
					+  SMWorkOrderDetail.Paramllsetpricetozero;
			if (wodetail.getssetpricetozero().compareToIgnoreCase("1") == 0){
				s += " <INPUT TYPE=HIDDEN NAME=\"" + sSetToZeroCheckboxFieldname + "\""
					+ " id = \"" + sSetToZeroCheckboxFieldname + "\""
					+ " VALUE=\"" + "yes" + "\""
					+ ">" + "\n"
					;
			}
		}
		
		//Store the rest of the values for the lines in hidden variables here:
		//Extended price:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdextendedprice + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdextendedprice + "\""
			+ "\""
			+ " VALUE=\"" + wodetail.getsbdextendedprice() + "\""
			+ ">" + "\n"
		;
		
		//Detail type:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramidetailtype + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramidetailtype + "\""
			+ "\""
			+ " VALUE=\"" + Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM) + "\""
			+ ">" + "\n"
		;
		
		//Work order line ID:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlid + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlid + "\""
			+ "\""
			+ " VALUE=\"" + wodetail.getslid() + "\""
			+ ">" + "\n"
		;

		//Work order line number:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramllinenumber + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramllinenumber + "\""
			+ "\""
			+ " VALUE=\"" + Integer.toString(iLineNumber) + "\""
			+ ">" + "\n"
		;

		//Sales order detail number:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
			+ "\""
			+ " VALUE=\"" + wodetail.getsorderdetailnumber() + "\""
			+ ">" + "\n"
		;
		
		//Work performed line number:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
			+ "\""
			+ " VALUE=\"" + "-1" + "\""
			+ ">"
			+ "\n"
		;
		
		//Work performed:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformed + "\""
			+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformed + "\""
			+ "\""
			+ " VALUE=\"" + "" + "\""
			+ ">"
			+ "\n"
		;
	
		s += "</TR>"
		;
		
		//If there is a work order detail comment on this order line, display it:
		SMOrderDetail orddetail = order.getOrderDetailByDetailNumber(wodetail.getsorderdetailnumber());
		if (orddetail != null){
			if (orddetail.getM_mTicketComments().compareToIgnoreCase("") != 0){
				s += "<TR style = \" background-color:" + SMWorkOrderHeader.ITEMS_TABLE_WORK_ORDER_DETAIL_COMMENT_BG_COLOR + "; \" >"
					+ "<TD COLSPAN=" + Integer.toString(iColumnCount) + ">"
					+ "<span style= \" font-size:small ; \"><I><B>Work order detail comment:&nbsp;</B>"
					+ order.getOrderDetailByDetailNumber(wodetail.getsorderdetailnumber()).getM_mTicketComments() + "</I>"
					+ "</span>"
					+ "</TD>"
					+ "</TR>"
				;
			}
		}
		
		//Display the work order comment from the icitems file, if there is one:
		String SQL = "SELECT"
			+ " " + SMTableicitems.sworkordercomment
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + wodetail.getsitemnumber() + "'"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL,
				getServletContext(),
				sm.getsDBID(), 
				"MySQL",
				SMUtilities.getFullClassName(this.toString()) + ".buildItemLine - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
					);
			if (rs.next()){
				String sWorkOrderItemComment = rs.getString(SMTableicitems.sworkordercomment);
				if (sWorkOrderItemComment.compareToIgnoreCase("") !=0){
					//Create another line with the work order comment information:
					s += "<TR style = \" background-color:" + SMWorkOrderHeader.ITEMS_TABLE_WORK_ORDER_ITEM_COMMENT_BG_COLOR + "; \" >"
						+ "<TD COLSPAN=" + Integer.toString(iColumnCount) + ">"
						+ "<span style= \" font-size:small ; \"><I><B>Item comment:&nbsp;</B>"
						+ sWorkOrderItemComment + "</I>"
						+ "</span>"
						+ "</TD>"
						+ "</TR>"
					;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1395172604] reading work order comments for item - " + e.getMessage());
		}
		return s;
	}

	private String buildItemLineForPostedWorkOrder(
			boolean bShowItemInformationLink,
			SMWorkOrderDetail wodetail,
			SMOrderHeader order,
			int iLineNumber,
			SMMasterEditEntry sm,
			String sDBID
			) throws Exception{
			String s = "";
			s += "<TR>";
			int iColumnCount = 0;
			//QTY
			s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdquantity() + "</TD>" + "\n";
			iColumnCount++;
			s += "<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdquantity + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdquantity + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsbdquantity()) + "\""
				+ ">" + "\n";
			
			//QTY Assigned
			s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdqtyassigned() + "</TD>" + "\n";
			iColumnCount++;
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdqtyassigned + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdqtyassigned + "\""
				+ "\""
				+ " VALUE=\"" + wodetail.getsbdqtyassigned().replace(",","") + "\""
				+ ">" + "\n"
			;
			
			String sItemNumberLink = wodetail.getsitemnumber();
			if (bShowItemInformationLink){
				sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICDisplayItemInformation?ItemNumber=" 
				+ wodetail.getsitemnumber()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">" + wodetail.getsitemnumber() + "</A>";
			}
			
			s += "<TD class=\"readonlyleftfield\">" + sItemNumberLink + "</TD>";
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemnumber()) + "\""
				+ ">" + "\n"
			;
			iColumnCount++;

			//DESCRIPTION
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsitemdesc() + "</TD>" + "\n";
			iColumnCount++;
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemdesc()) + "\""
				+ ">" + "\n"
			;
			
			//LOCATION
			if(SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditLocationOnWorkOrderDetail, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getslocationcode()  + "</TD>" + "\n";
			iColumnCount++;
			}
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramslocationcode+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramslocationcode + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getslocationcode()) + "\""
				+ ">" + "\n"
			;
			
			//UOM
			s += "<TD class=\"readonlyleftfield\">" + wodetail.getsuom() + "</TD>" + "\n";
			iColumnCount++;
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsunitofmeasure+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsunitofmeasure + "\""
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsuom()) + "\""
				+ ">" + "\n";

			//PRICE
			//IF we are supposed to show prices, add a column for that here:
			//Set to zero column
			//String sSetToZeroCheckboxFieldname = SMWorkOrderHeader.SET_TO_ZERO_CHECKBOX_MARKER 
			//	+ SMUtilities.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)
			//	+  SMWorkOrderDetail.Paramllsetpricetozero;
			//if (bShowPrices){
			//	if (bActivateSetToZeroFunction){
			//		s += "<TD class=\"readonlyrightfield\" style = \"vertical-align:top;\" ><INPUT TYPE=CHECKBOX";
			//		if (wodetail.getssetpricetozero().compareToIgnoreCase("1") == 0){
			//			s += SMUtilities.CHECKBOX_CHECKED_STRING;
			//		}
			//		s += " NAME=\"" + sSetToZeroCheckboxFieldname + "\""
			//			+ " ID = \"" + sSetToZeroCheckboxFieldname + "\""
			//			+ " disabled " //Can't change this on posted orders
			//			+ " width=0.25>"
			//			+ "</TD>"
			//		;
			//	
			//		//Extended price COLUMN
			//		s += "<TD class=\"readonlyrightfield\">" + wodetail.getsbdextendedprice() + "</TD>" + "\n";
			//	}
			//	iColumnCount++;
			//}else{
			//	//Set to zero column
			//	//Since this is normally a 'checkbox' field, we'll only include it if the value is 'checked':
			//	if (wodetail.getssetpricetozero().compareToIgnoreCase("1") == 0){
			//	s += " <INPUT TYPE=HIDDEN NAME=\"" + sSetToZeroCheckboxFieldname + "\""
			//		+ " id = \"" + sSetToZeroCheckboxFieldname + "\""
			//		+ " VALUE=\"" + "yes" + "\""
			//		+ ">" + "\n"
			//		;
			//	}
			//}
			
			//Store the rest of the values for the lines in hidden variables here:
			//Extended price:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdextendedprice + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdextendedprice + "\""
				+ "\""
				+ " VALUE=\"" + wodetail.getsbdextendedprice() + "\""
				+ ">" + "\n"
			;
			
			//Detail type:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramidetailtype + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramidetailtype + "\""
				+ "\""
				+ " VALUE=\"" + Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM) + "\""
				+ ">" + "\n"
			;
			
			//Work order line ID:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlid + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlid + "\""
				+ "\""
				+ " VALUE=\"" + wodetail.getslid() + "\""
				+ ">" + "\n"
			;

			//Work order line number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramllinenumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramllinenumber + "\""
				+ "\""
				+ " VALUE=\"" + Integer.toString(iLineNumber) + "\""
				+ ">" + "\n"
			;

			//Sales order detail number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
				+ "\""
				+ " VALUE=\"" + wodetail.getsorderdetailnumber() + "\""
				+ ">" + "\n"
			;
			
			//Work performed line number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
				+ "\""
				+ " VALUE=\"" + "-1" + "\""
				+ ">"
				+ "\n"
			;
			
			//Work performed:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsworkperformed + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsworkperformed + "\""
				+ "\""
				+ " VALUE=\"" + "" + "\""
				+ ">"
				+ "\n"
			;
			
			s += "</TR>"
			;

			//If there is a work order detail comment on this order line, display it:
			SMOrderDetail orddetail = order.getOrderDetailByDetailNumber(wodetail.getsorderdetailnumber());
			if (orddetail != null){
				if (orddetail.getM_mTicketComments().compareToIgnoreCase("") != 0){
					s += "<TR style = \" background-color:" + SMWorkOrderHeader.ITEMS_TABLE_WORK_ORDER_DETAIL_COMMENT_BG_COLOR + "; \" >"
						+ "<TD COLSPAN=" + Integer.toString(iColumnCount) + ">"
						+ "<span style= \" font-size:small ; \"><I><B>Work order detail comment:&nbsp;</B>"
						+ order.getOrderDetailByDetailNumber(wodetail.getsorderdetailnumber()).getM_mTicketComments() + "</I>"
						+ "</span>"
						+ "</TD>"
						+ "</TR>"
					;
				}
			}
			
			//Display the work order comment from the icitems file, if there is one:
			String SQL = "SELECT"
				+ " " + SMTableicitems.sworkordercomment
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
					+ SMTableicitems.sItemNumber + " = '" + wodetail.getsitemnumber() + "'"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL,
					getServletContext(),
					sm.getsDBID(), 
					"MySQL",
					SMUtilities.getFullClassName(this.toString()) + ".buildItemLine - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
						);
				if (rs.next()){
					String sWorkOrderItemComment = rs.getString(SMTableicitems.sworkordercomment);
					if (sWorkOrderItemComment.compareToIgnoreCase("") !=0){
						//Create another line with the work order comment information:
						s += "<TR style = \" background-color:" + SMWorkOrderHeader.ITEMS_TABLE_WORK_ORDER_ITEM_COMMENT_BG_COLOR + "; \" >"
							+ "<TD COLSPAN=" + Integer.toString(iColumnCount) + ">"
							+ "<span style= \" font-size:small ; \"><I><B>Item comment:&nbsp;</B>"
							+ sWorkOrderItemComment + "</I>"
							+ "</span>"
							+ "</TD>"
							+ "</TR>"
						;
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1395172604] reading work order comments for item - " + e.getMessage());
			}
			
			return s;
		}
	private String buildblankItemLine(
			int iLineNumber, 
			SMMasterEditEntry sm, 
			String sDefaultLocation, 
			boolean bShowPrices) throws Exception{
			String s = "";
			s += "<TR>";
			//Qty
			s += "<TD class=\" fieldcontrol \" >"
					+ "<INPUT TYPE=TEXT NAME=\""
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdquantity
				+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdquantity
				+ "\""
				+ " VALUE=\"" + "" + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + SMWorkOrderHeader.ITEM_QTY_FIELD_WIDTH
				+ " MAXLENGTH=13"
				+ ">"
				+ "</TD>"
				+ "\n"
			;
			
			s += "<TD class=\"readonlyrightfield\">" 
				+ "(N/A)" + "</TD>" + "\n";
			
			//Item
			s += "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=TEXT NAME=\""
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber
				+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemnumber
				+ "\""
			;

			//If we have returned from a call to the finder, this field could have a value in it:
			String sItemNumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER + clsStringFunctions.PadLeft(Integer.toString(iLineNumber),
					"0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) +  SMWorkOrderDetail.Paramsitemnumber, sm.getRequest());
			if (sItemNumber.compareToIgnoreCase("") == 0){
				sItemNumber = "";
			}
			s += " VALUE=\"" 
				+ clsStringFunctions.filter(sItemNumber)
			+ "\"";
			
			s += " onchange=\"flagDirty();\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + Integer.toString(SMTableorderdetails.sItemNumberLength)
				+ ">"
				+ "\n"
			;
			
			//Add a button for the finder:
			s += "<button type=\"button\""
			+ " value=\"" 
				+ FINDITEM_BUTTON_LABEL 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+ "\""
				+ " name=\"" 
				+ FINDITEM_BUTTON_LABEL 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+ "\""
				+ " onClick=\"finditem('" 
				+ FINDITEM_COMMAND_VALUE_BASE 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER)
				+ "');\">"
				+ FINDITEM_BUTTON_LABEL
				+ "</button>\n"
				+ "</TD>"
				+ "\n"
			;
			
			//Item Desc
			s += "<TD class=\" fieldcontrol \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc
				+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsitemdesc
				+ "\""
				+ " VALUE=\"" + "" + "\""
				+ " onchange=\"flagDirty();\""
				+ " style=\"width:100%\""
				+ " MAXLENGTH=" + Integer.toString(SMTableorderdetails.sItemDescLength)
				+ ">"
				+ "</TD>"
				+ "\n"
			;
			if(SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditLocationOnWorkOrderDetail, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				//Add Location drop down list			
			    ArrayList<String> arrValues = new ArrayList<String>(0);
		        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
		        //arrValues.add("");
		        //arrDescriptions.add("None");
		        try{
					//Get the record to edit:
			        String sSQL = " SELECT * " + "FROM " + SMTablelocations.TableName;
			        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
			        	sSQL, 
			        	getServletContext(), 
			        	sm.getsDBID(),
			        	"MySQL",
			        	this.toString() + ".buildItemLineForEditing - User: " + sm.getUserID()
			        	+ " - "
			        	+ sm.getFullUserName()
			        		);
			        while (rsLocations.next()){
			        	arrValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
			        	arrDescriptions.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
					}
			        rsLocations.close();
				}catch (SQLException ex){
					s += "<FONT COLOR=RED><B>Error [1457017425] reading locations - " + ex.getMessage() + ".</FONT></B>";
				}       
		        
				String sFieldName = SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
						+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+  SMWorkOrderDetail.Paramslocationcode ;
				s += "<TD class=\" fieldcontrol \">";
				s +=  clsCreateHTMLFormFields.Create_Edit_Form_List_Field(sFieldName,
						 								arrValues,
						 								sDefaultLocation,
						 								arrDescriptions);
				s += "</TD>";
			}else{
				s += " <INPUT TYPE=HIDDEN NAME=\"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramslocationcode + "\""
					+ " ID = \"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Paramslocationcode + "\""
					+ "\""
					+ " VALUE=\"" + sDefaultLocation + "\""+ ">"
					+ "\n"
				;
			}
			//Item UOM - this gets validated against the ICITEMS table when saved, so the user has no need to enter it here:
			s += "<TD>&nbsp;</TD>";
			
			if (bShowPrices){
				//Add a column for the 'set to zero' field:
				s += "<TD class=\"readonlyrightfield\">&nbsp;</TD>" + "\n";
				//Add a column for the prices:
				s += "<TD class=\"readonlyrightfield\">&nbsp;</TD>" + "\n";
			}
			
			//Store the rest of the values for the lines in hidden variables here:
			//Extended price:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdextendedprice + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdextendedprice + "\""
				+ "\""
				+ " VALUE=\"" + "0.00" + "\""
				+ ">" + "\n"
			;
			
			//Detail type:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramidetailtype + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramidetailtype + "\""
				+ "\""
				+ " VALUE=\"" + Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM) + "\""
				+ ">"
				+ "\n"
			;
			
			//Work order line ID:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlid + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlid + "\""
				+ "\""
				+ " VALUE=\"" + "-1" + "\""
				+ ">"
				+ "\n"
			;

			//Work order line number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramllinenumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramllinenumber + "\""
				+ "\""
				+ " VALUE=\"" + Integer.toString(iLineNumber) + "\""
				+ ">"
				+ "\n"
			;

			//Sales order detail number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
				+ "\""
				+ " VALUE=\"" + "-1" + "\""
				+ ">"
				+ "\n"
			;
			
			//Work performed line number:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
				+ "\""
				+ " VALUE=\"" + "-1" + "\""
				+ ">"
				+ "\n"
			;
			
			//Work performed:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsworkperformed + "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Paramsworkperformed + "\""
				+ "\""
				+ " VALUE=\"" + "" + "\""
				+ ">" + "\n"
			
			+ "</TR>"
			;
			return s;
		}
	private String createWorkPerformedTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			SMOrderHeader order
			) throws Exception{
		String s = "";
		int iNumberOfWPCCodes = 0;
		
		s += "<TABLE class = \" innermost \" style=\" title:WorkPerformedTable; background-color: "
				+ SMWorkOrderHeader.WORKPERFORMED_TABLE_BG_COLOR + "; width=100%; \" >\n";	
		s += "<TR><TD><U><B>Work performed codes:</B></U></TD></TR>";
		
		//If this is editable:
		if (!workorder.isWorkOrderPosted()){
			String SQL = "SELECT"
				+ " " + SMTableworkperformedcodes.sCode
				+ ", " + SMTableworkperformedcodes.sWorkPerformedCode
				+ ", " + SMTableworkperformedcodes.sWorkPerformedPhrase
				+ " FROM " + SMTableworkperformedcodes.TableName
				+ " WHERE ("
					+ "(" + SMTableworkperformedcodes.sCode + " = '" + order.getM_sServiceTypeCode() + "')"
				+ ")"
				+ " ORDER BY " + SMTableworkperformedcodes.iSortOrder
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".createWorkPerrformedTable - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
				);
				while (rs.next()){
					iNumberOfWPCCodes++;
					//Check to see which ones have been checked on the work order:
					String sCheckBoxChecked = "";
					if(workorder.isWorkPerformedCodeOnWorkOrder(rs.getString(SMTableworkperformedcodes.sWorkPerformedCode))){
						sCheckBoxChecked = "checked";
					}
					s +=
						"<TR>"
						+ "<TD class=\" fieldcontrol \">"
						+ "<INPUT TYPE=CHECKBOX "
						+ sCheckBoxChecked
						+ " NAME=\"" 
							+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
							+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ "\""
						+ " id = \""
							+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
							+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ "\""
						//+ sCheckBoxDisabled
						+ " onchange=\"flagDirty();\""
						+ " width=0.25>"
						//+ "</TD>"
				
						//WPC Code:
						//+ "<TD class=\"readonlyleftfield\">" 
						+ "&nbsp;" + rs.getString(SMTableworkperformedcodes.sWorkPerformedCode)
						+ "\n"
						+ "<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ SMWorkOrderDetail.Paramsworkperformedcode
						+ "\""
						+ " VALUE=\"" + clsStringFunctions.filter(rs.getString(SMTableworkperformedcodes.sWorkPerformedCode)) + "\"" + ">" + "\n"
						//+ "</TD>"
						
						//WPC Description:
						//+ "<TD class=\"readonlyleftfield\">" 
						+ "&nbsp;" + rs.getString(SMTableworkperformedcodes.sWorkPerformedPhrase) 
						+ "\n"
						+ "<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ SMWorkOrderDetail.Paramsworkperformed
						+ "\""
						+ " VALUE=\"" + clsStringFunctions.filter(rs.getString(SMTableworkperformedcodes.sWorkPerformedPhrase)) + "\"" + ">" + "\n"
						
						+ "</TD>"

						+ "</TR>"
					;
				}
				rs.close();
			} catch (Exception e1) {
				throw new Exception("Error [1391634073] reading work performed codes - " + e1.getMessage());
			}
		}else{
			//If this is read only, just load all the WPC's from the work order:
			for (int i = 0; i < workorder.getDetailCount(); i++){
				if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0){
					iNumberOfWPCCodes++;
					s += "<TR>"
						+ "<TD class=\"readonlyleftfield\">"
						+ workorder.getDetailByIndex(i).getsworkperformed()
						+ "</TD>"
						+ "</TR>"
						+ "\n"
					;
					//Add the 'hidden' values so these can be carried with the work order in memory on the server:
					
					//This value is the same as having the checkbox on the edit screen for each chosen WPC:
					s += " <INPUT TYPE=HIDDEN NAME=\"" 
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ "\""
						+ " id = \""
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ "\""
						+ " VALUE=\"" + "Y" + "\""
						+ ">"
						+ "\n"
					;
					//Store the WPC code:
					s += "<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ SMWorkOrderDetail.Paramsworkperformedcode
						+ "\""
						+ " VALUE=\"" + clsStringFunctions.filter(workorder.getDetailByIndex(i).getsworkperformedcode()) + "\"" + ">" + "\n"
					;
					
					//Store the work performed:
					s += "<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMWorkOrderHeader.WORK_ORDER_WORKPERFORMEDCODE_MARKER
						+ clsStringFunctions.PadLeft(Integer.toString(iNumberOfWPCCodes), "0", SMWorkOrderHeader.OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
						+ SMWorkOrderDetail.Paramsworkperformed
						+ "\""
						+ " VALUE=\"" + clsStringFunctions.filter(workorder.getDetailByIndex(i).getsworkperformed()) + "\"" + ">" + "\n"
					;
				}
			}
		}
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.TOTAL_NUMBER_OF_WPC_CODES + "\" VALUE=\"" + Integer.toString(iNumberOfWPCCodes) + "\"" + ">" + "\n";
		//Close the table:
		s += "</TABLE style = \" title:WorkPerformedTable; \">\n";
		return s;
	}
	private String sSignatureScripts(SMWorkOrderHeader workorder){
	    //Scripts for signature:
		
		String s = "<script src=\"scripts/jquery-signaturepad-min-01.js\"></script>\n";
	    s += "<script src=\"scripts/json2.min.js\"></script>\n"
	    ;
	    return s;
	}
	private String createMechanicInfoTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			ArrayList<String>arrMechanicsInitials, 
			ArrayList<String>arrMechanicsNames,
			ArrayList<String>arrMechanicIDs
			) throws SQLException{
		String s = "";
		int iColSpan = 5;

		s += "<TABLE class = \" innermost \" style=\" title:MechanicInfoTable; background-color: "
				+ SMWorkOrderHeader.COMMENTS_TABLE_BG_COLOR + "; \" width=100% >\n";

		//Work order comments:
		s += "<TR>";
		s += "<TD><U><B>Description of work performed:</B></U></TD>";
		
		//Set the date:
		s += "<TD class=\" fieldlabel \">Date of work:&nbsp;</TD>";
		if (workorder.isWorkOrderPosted()){
			s += "<TD>" + workorder.getdattimedone() + "</TD>";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramdattimedone + "\" VALUE=\"" + workorder.getdattimedone() 
				+ "\"" + ">";
			//Mechanic:	
			s += "<TD class=\" fieldlabel \">Mechanic:&nbsp;</TD>";
			s += "<TD>" + workorder.getmechanicsname() + "</TD>";
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
					+ SMWorkOrderHeader.Paramsmechanicname 
					+ "\""
					+ " VALUE=\"" + clsStringFunctions.filter(workorder.getmechanicsname()) + "\""
					+ ">"
					;
			//End the row:
			s += "</TR>";
			s += "<TR><TD COLSPAN=" + Integer.toString(iColSpan) + ">" + workorder.getmcomments();
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.Parammcomments 
				+ "\""
				+ " id = \"" 
				+ SMWorkOrderHeader.Parammcomments
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(workorder.getmcomments()) + "\""
				+ ">"
				;
			s += "</TD></TR>";
		}else{
			if (workorder.getdattimedone().compareToIgnoreCase(clsMasterEntry.EMPTY_DATE_STRING) == 0){
				String sCurrentDate = SMUtilities.EMPTY_DATE_VALUE;
				clsDBServerTime st = null;
				
				try {
					st = new clsDBServerTime(sm.getsDBID(), sm.getUserName(), getServletContext());
					sCurrentDate = st.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
				} catch (Exception e) {
					//Just leave it as an empty date
				}
				workorder.setdattimedone(sCurrentDate);
			}
			s += 
				"<TD class=\"fieldcontrol\">"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramdattimedone + "\""
				+ " VALUE=\"" + workorder.getdattimedone().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramdattimedone + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "9"
				+ " MAXLENGTH=" + "10"
				//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(SMWorkOrderHeader.Paramdattimedone, getServletContext())
				+ "</TD>"
				;
			
			//Mechanic:	
			s += "<TD class=\" fieldlabel \">Mechanic:&nbsp;</TD>";
			s += "<TD>" + workorder.getmechanicsname() + "</TD>";
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.Paramsmechanicname 
				+ "\""
				+ " VALUE=\"" + clsStringFunctions.filter(workorder.getmechanicsname()) + "\""
				+ ">"
				;

			s += "</TR>";
			s += "<TR>";
			s += "<TD class=\" fieldcontrol \"  COLSPAN=" + Integer.toString(iColSpan) + ">"
				+ "<TEXTAREA NAME=\"" + SMWorkOrderHeader.Parammcomments + "\""
				+ " rows=\"" + "5" + "\""
				//+ " cols=\"" + Integer.toString(iCols) + "\""
				+ "style=\"width:100%\""
				+ " id = \"" + SMWorkOrderHeader.Parammcomments + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ workorder.getmcomments().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				+ "</TD>"
			;
			s += "</TR>";
			
			s += "<TR><TD><U><B>Additional work required:</B></U></TD></TR>";
			s += "<TR>";
			s += "<TD class=\" fieldcontrol \"  COLSPAN=" + Integer.toString(iColSpan) + ">"
				+ "<TEXTAREA NAME=\"" + SMWorkOrderHeader.Parammadditionalworkcomments + "\""
				+ " rows=\"" + "5" + "\""
				//+ " cols=\"" + Integer.toString(iCols) + "\""
				+ "style=\"width:100%\""
				+ " id = \"" + SMWorkOrderHeader.Parammadditionalworkcomments + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ workorder.getmadditionalworkcomments().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				+ "</TD>"
			;
			s += "</TR>";
		}

		//Close the table:
		s += "</TABLE style = \" title:MechanicInfoTable; \">\n";
		return s;
	}

	private String createInstructionsTable(
			SMWorkOrderHeader wo,
			SMOrderHeader order) throws Exception{
		String s = "";

		s += "<TR><TD>";
		s += "<TABLE class = \" innermost \" style=\" title:InstructionsTable; background-color: "
			+ SMWorkOrderHeader.INSTRUCTIONS_TABLE_BG_COLOR + "; \" width=100% >\n";
		
		//Ticket notes:
		s += "<TR><TD><U><B>Work order notes:&nbsp;</B></U>";
		s += order.getM_sTicketComments().replace("\n", "<BR>");
		s += "</TD></TR>";

		//Directions:
		if (order.getM_sDirections().compareToIgnoreCase("") != 0){
			s += "<TR><TD><U><B>Directions:&nbsp;</B></U>";
			s += order.getM_sDirections().replace("\n", "<BR>");
			s += "</TD></TR>";
		}
		s += "<TR style= \" background-color: white; \"><TD><U><B><FONT COLOR=RED>Instructions:&nbsp;</FONT></B></U>";
		s += wo.getminstructions().replace("\n", "<BR>");
		s += "</TD></TR>";		
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
		+ SMWorkOrderHeader.Paramminstructions 
		+ "\""
		+ " id = \"" 
		+ SMWorkOrderHeader.Paramminstructions
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wo.getminstructions()) + "\""
		+ ">"
		;
		//Close the table:
		s += "</TABLE style = \" title:InstructionsTable; \">\n";
		s += "</TD></TR>";

		return s;
	}
//	private String createManagerNotesTable(SMWorkOrderHeader wo, SMOrderHeader order) throws Exception{
//		String s = "";
//		
//		s += "<TR><TD>";
//		s += "<TABLE class = \" innermost \" style=\" title:ManagerNotesTable; background-color: "
//			+ SMWorkOrderHeader.MANAGERNOTES_TABLE_BG_COLOR + "; \" width=100% >\n";
//		
//		s += "<TR><TD><U><B>Manager Notes:</B></U>";
//		
//		s += "<BR><TEXTAREA NAME=\"" + SMWorkOrderHeader.Parammmanagernotes + "\""
//				+ " rows=\"" + "5" + "\""
//				//+ " cols=\"" + Integer.toString(iCols) + "\""
//				+ "style=\"width:100%\""
//				+ " id = \"" + SMWorkOrderHeader.Parammmanagernotes + "\""
//				+ " onchange=\"flagDirty();\""
//				+ ">"
//				+ wo.get_mmanagernotes().replace("\"", "&quot;")
//				+ "</TEXTAREA>"
//		;
//		s += "</TD></TR>";
//		//Close the table:
//		s += "</TABLE style = \" title:ManagerNotesTable; \">\n";
//		s += "</TD></TR>";
//		return s;
//	}
	private String createSignatureBlockTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder) throws SQLException{
		
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:SignatureBlockTable; background-color: "
				+ SMWorkOrderHeader.SIGNATUREBLOCK_TABLE_BG_COLOR + "; width=100%; \" >\n";
		
		//If the work order is posted:
		if (m_bDisplaySigantureBox){
			s += "<TR>";
			s += "<TD><U><B>Additional work required:</B></U></TD>";
			s += "</TR>";
			s += "<TR><TD>" + workorder.getmadditionalworkcomments();
			s += "</TD></TR>";
			
			//Checkbox:
			s += "<TR><TD>";
			String sCheckboxDisabled = " disabled = \"disabled\"";
			s += "<INPUT TYPE=CHECKBOX ";
			if (workorder.getsiadditionalworkauthorized().compareToIgnoreCase("1") == 0){
				s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}
			s += sCheckboxDisabled;
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
			
			//Signature:
			s += "<TR><TD>";
			s += "<B>Signature:</B>&nbsp;";
			//Add signature here:
			String sSignatureBoxWidth = workorder.getlsignatureboxwidth();
			//If the signature was never saved then load default signature box dimensions to show no signature has been collected
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
			s +=
	        	"\n"
	        	+ "<canvas class=pad width=" + sSignatureBoxWidth + " name=signaturecanvas"
        		+ " height=" + sSignatureBoxHeight + " style=\"border:1px solid  #000000;\" ></canvas>\n"
	        ;	
			s += "&nbsp;<B>Date signed:</B>&nbsp;";
			s += workorder.getdattimesigned();
			s += "</TD>";
			s += "</TR>";
			s += "<TR><TD>";
			s += "<B>Printed name and title:</B>&nbsp;";
			s += workorder.getssignedbyname() + "</TD>";
			s += "</TR>";
		}
			
		//Close the table:
		s += "</TABLE style = \" title:SignatureBlockTable; \">\n";
		return s;

	}
	public static String createOrderHeaderTable(
			SMMasterEditEntry sm,
			ServletContext context,
			SMWorkOrderHeader workorder, 
			SMOrderHeader orderheader,
			String sClassName,
			int iSavingFromWhichScreen,
			boolean bUseGoogleDrivePicker) throws Exception{
		String s = "";
		

		String sWorkOrderID = "(NEW)";
		if(workorder.getlid().compareToIgnoreCase("-1") != 0){
			sWorkOrderID = workorder.getlid();
		}
		
		String sLinkToConfigureWorkOrder = "";
		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMConfigureWorkOrders, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			sLinkToConfigureWorkOrder = "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMConfigWorkOrderEdit?" + SMWorkOrderHeader.Paramlid + "=" 
				+ sWorkOrderID
				+ "&CallingClass=" + SMUtilities.getFullClassName(sm.getCallingClass())
				+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
				+ "\">"
				+ "Configure</A>";
		;
		}
		

		
		String sOrderNumber = workorder.getstrimmedordernumber();
		if (workorder.getstrimmedordernumber().compareToIgnoreCase("") != 0){
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
		}else{
			sOrderNumber = SMWorkOrderHeader.NO_ORDER_NUMBER_MARKER;
		}
		String sPosted = "N";
		if (workorder.getsposted().compareToIgnoreCase("1") == 0){
				sPosted = "Y";
		}
		String sImported = "N";
		if (workorder.getsimported().compareToIgnoreCase("1") == 0){
			sImported = "Y";
		}

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
				+ "\">" + "Work&nbsp;order&nbsp;list" + "</A>"
			;
		}
		//Link to create Delivery Ticket
		String sLinkToCreateDeliveryTicket = "";
		if((SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMManageDeliveryTickets, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
			&& (workorder.getstrimmedordernumber().compareToIgnoreCase("") != 0)){
			sLinkToCreateDeliveryTicket = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMEditDeliveryTicketEdit?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
				+ "&" + SMTabledeliverytickets.strimmedordernumber + "=" + workorder.getstrimmedordernumber()
				+ "&" + SMTabledeliverytickets.iworkorderid + "=" + workorder.getlid()
				+ "&" + SMTabledeliverytickets.smechanicname + "=" + workorder.getmechanicsname()
				+ "&" + SMTabledeliverytickets.lid + "=-1"
				+ "\">Add&nbsp;delivery&nbsp;ticket</A>";
		}

		String sMapAddress = "";
		if(orderheader.getM_sShipToAddress1().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += "" +orderheader.getM_sShipToAddress1().trim() ;
		}
		if(orderheader.getM_sShipToAddress2().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += "<br>" +orderheader.getM_sShipToAddress2().trim() ;
		}
		if(orderheader.getM_sShipToAddress3().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += "<br>" +orderheader.getM_sShipToAddress3().trim() ;
		}		
		if(orderheader.getM_sShipToAddress4().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += "<br>" + orderheader.getM_sShipToAddress4().trim() ;
		}		
		if(orderheader.getM_sShipToCity().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += "<br>" + orderheader.getM_sShipToCity().trim() + " ";
		}		
		if(orderheader.getM_sShipToState().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += orderheader.getM_sShipToState().trim() + " ";
		}
		if(orderheader.getM_sShipToZip().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += orderheader.getM_sShipToZip().trim() + "";
		}
		if(orderheader.getM_sShipToCountry().trim().compareToIgnoreCase("") != 0) {
			sMapAddress += orderheader.getM_sShipToCountry().trim() + "";
		}
		//Make the toolbar for small screens 
		s += "<div class=\"d-block d-md-none\">";
		s += "<div class=\"d-flex justify-content-between\"> <div style=\"font-size:large; padding: 7px;\"><b>" + sWorkOrderID + "</b><font style=\"font-size:small;\">" + sLinkToConfigureWorkOrder + "</font></div>";
		s += "<button class=\"btn\" type=\"button\" onclick=\"$('#headerTable').toggleClass('d-none');\">" + "Details" + " </button>"
		+ "</div><BR>";
		s += "<div class=\"row \">";
	
		s += "<div class=\"col\">"
				+ "<a href=\"" +  clsServletUtilities.createGoogleMapLink(sMapAddress.replace("<br>", "")) + "\">"
				+ "<div class=\"text-center\">"
				+ "<i class=\"t material-icons\" style=\"font-size:35px;color:black\">place" + "</i>" 
				+ "<br>" + sMapAddress + ""
						+ "</div></a>"
		  + "</div>";
		
		s += "<div class=\"col\">"
				+ "<a href=\"tel:" + orderheader.getM_sShiptoPhone() + "\">"
				+ "<div class=\"text-center\">"
				+ "<i class=\"material-icons\" style=\"font-size:35px;color:black\">phone" + "</i>"
				+ "<br>" + orderheader.getM_sShiptoContact() + "<br>" + orderheader.getM_sShiptoPhone() + "</div></a>"
		+ "</div>";
		
		if(bUseGoogleDrivePicker) {
			s += "<div class=\"col\">"
					+ "<a id=\"myLink\" href=\"#\" onclick=\"loadPicker();return false;\">"
					+ "<div class=\"text-center\">"
					+ "<i class=\"material-icons\" style=\"font-size:35px;color:black\">folder" + "</i>"
					+ "<br>" + "Documents" + "</div></a>"
			+ "</div>";
		}
		
		s+= "</div>";
		s += "<hr/>";
		s += "</div>";
		
		
		s += "<div class=\"container-fluid  d-none d-md-block\" id=\"headerTable\">";

		
		if(sLinkToWorkOrderList.compareToIgnoreCase("") != 0 
				|| sLinkToCreateDeliveryTicket.compareToIgnoreCase("") != 0
				|| sLinkToConfigureWorkOrder.compareToIgnoreCase("") != 0) {
			s += "<div class=\"row flex-md-nowrap justify-content-start\">";
			if (sLinkToConfigureWorkOrder.compareToIgnoreCase("") != 0){
				s +=  "<div class=\"p-1 d-none d-md-block\">" + sLinkToConfigureWorkOrder 
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>";
			}
			if (sLinkToWorkOrderList.compareToIgnoreCase("") != 0){
				s +=  "<div class=\"p-1\">" + sLinkToWorkOrderList 
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>";
			}
			if (sLinkToCreateDeliveryTicket.compareToIgnoreCase("") != 0){
				s +=  "<div class=\"p-1\">" + sLinkToCreateDeliveryTicket + "</div>";
			}
			s += "</div>";
		}
		


		s += "<div class=\"row flex-md-nowrap\">";
		s += "<div style=\"white-space: nowrap;\" class=\"col-md p-1 d-none d-md-block\"><b>WO&nbsp;#:</b>&nbsp;" + sWorkOrderID + "</div>";
		s +=  "<div class=\"col-md p-1\"><b>Scheduled:</b>&nbsp;" + "" + workorder.getsscheduleddate() + "</div>"
			+ "<INPUT type=\"hidden\" name=\"" + SMWorkOrderHeader.Paramscheduleddate+ "\" value=\"" + workorder.getsscheduleddate() + "\">"
				
			+ "<div class=\"col-md p-1\"><b>Posted?:</b>&nbsp;" + sPosted + "</div>"
				
			+ "<div class=\"col-md p-1\"><b>Imported?:</b>&nbsp;" + sImported + "</div>"

			+ "<div class=\"col-md p-1\"><b>Order&nbsp;#:</b>&nbsp;" + sOrderNumber + "</div>"
			+ "<div class=\"col-md p-1\"><b>Terms:</b>&nbsp;" + orderheader.getM_sTerms() + "</div>"
			+ "<div style= \"white-space: nowrap;\" class=\"col-md p-1\"><b>Sales&nbsp;#:</b>&nbsp;" + orderheader.getM_sSalesperson() + "-" + sSalespersonName + "</div>"
			+ "<div class=\"col-md p-1\"><b>wage&nbsp;rate:</b>&nbsp;" + orderheader.getM_sSpecialWageRate() + "</div>";
		//Starting time:
		s += "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>Starting time:</b>&nbsp;" + workorder.getsstartingtime() + "</div>"
		+ "<INPUT type=\"hidden\" name=\"" + SMWorkOrderHeader.Paramsstartingtime+ "\" value=\"" + workorder.getsstartingtime() + "\">";

		//Assistant:
		s += "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>Assistant:</b>&nbsp;" + workorder.getsassistant() + "</div>"
			+ "<INPUT type=\"hidden\" name=\"" + SMWorkOrderHeader.Paramsassistant+ "\" value=\"" + workorder.getsassistant() + "\">";

		
		s += "</div>";	
		
		
		s += "<div class=\"row flex-md-nowrap\">";
		s += "<div style= \"white-space:nowrap;\" class=\"col-md p-1\"><b>Bill&nbsp;to:&nbsp;</b>" + orderheader.getM_sBillToName() + "</div>";
		

		
		s += "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>Ship&nbsp;to:</b>&nbsp;" + orderheader.getM_sShipToName() 
			+ "&nbsp;" + "<A HREF=\"" + clsServletUtilities.createGoogleMapLink(sMapAddress.replace("<br>", "")) + "\">" + sMapAddress.replace("<br>", "") + "</A>"
			+ "</div>"
			
			//Ship to contact:
			+ "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>Contact:</b>&nbsp;" + orderheader.getM_sShiptoContact() + "</div>"

			//Ship to phone:
			+ "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>Phone:</b>&nbsp;" + orderheader.getM_sShiptoPhone() + "</div>"
		
			//Second phone:
			+ "<div style=\"white-space:nowrap;\" class=\"col-md p-1\"><b>2nd Phone:</b>&nbsp;" + orderheader.getM_ssecondaryshiptophone() + "</div>"
			;
		//Close the table:
		s += "</div>\n";
		s += "</div>\n";
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
		+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>"
		+ "<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css\" integrity=\"sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T\" crossorigin=\"anonymous\">"
		+ "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/icon?family=Material+Icons\">"
		+ "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
		+ "</HEAD>\n" 
		+ "<BODY"
		+ " style=\"font-family: " + sfontfamily + " !important; background-color:" +sbackgroundcolor + " !important; \""
		+ " class=\"override\">"
		;
		s += "<TABLE BORDER=0 style=\"font-size: medium;\">"
		+"<TR><TD VALIGN=BOTTOM><B>" + scompanyname + ": " + title + "</B></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	private String sCommandScripts(
		SMWorkOrderHeader workorder, 
		SMMasterEditEntry smmaster,
		boolean bAllowEditingLeftPreviousSiteTime,
		boolean bAllowEditingArrivedAtCurrentSiteTime,
		boolean bAllowEditingLeftCurrentSiteTime,
		boolean bAllowArrivedAtNextSiteTime
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

		s += "    shortcut.add(\"Alt+a\",function() {\n";
		s += "        gotosignaturemode();\n";
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
		s += "        adddetailsheet();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+l\",function() {\n";
		s += "        createMaterialReturn();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+m\",function() {\n";
		s += "        email();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        print();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        removepricing();\n";
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
		
		s += "    shortcut.add(\"Alt+w\",function() {\n";
		s += "        postworkorder();\n";
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
		if(m_bDisplaySigantureBox){
			sSignaturePadOptions += ", displayOnly:true";
		}
		
		s += "window.onload = function() {\n"
			+ "\n"
			+ "    initShortcuts();\n"
			//+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = '';\n"
			
			//Display signature:
			+ "    $(document).ready("
			+ "			function () {\n";
		if(m_bDisplaySigantureBox){
			s	+="       $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "});\n";
		
			if (workorder.getmsignature().compareToIgnoreCase("") != 0){
				s += "        $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).regenerate(" + workorder.getmsignature()  + ");\n";
			}
		}
	    s += "        }"
	    	+ "    );\n";
			
		s += "\n"
			+ "}\n"
		;

		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"		
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes.';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		//***********************
		//Check to make sure screen is fully displayed:
		s += "function bScreenIsFullyDisplayed(){\n"
			+ "    try{\n"
			+ "        if (document.getElementById(\"" + FORM_IS_FULLY_DISPLAYED_FIELD + "\").value != \"" 
				+ FORM_IS_FULLY_DISPLAYED + "\" ){\n"
			+ "            document.getElementById(\"" + FULLY_DISPLAYED_WARNING_FIELD + "\").value = \"" + FULLY_DISPLAYED_WARNING_VALUE_YES + "\";\n"
			+ "            alert('The screen does not appear to be fully displayed yet.  Please try again.');\n"
			+ "            return false;\n"
			+ "        }else{\n"
			+ "            return true;\n"
			+ "        }\n"
			+ "    }catch(err) {\n"
			+ "        document.getElementById(\"" + FULLY_DISPLAYED_WARNING_FIELD + "\").value = \"" + FULLY_DISPLAYED_WARNING_VALUE_YES + "\";\n"
			+ "        alert('The screen does not appear to be fully displayed yet.  Please try again.');\n"
			+ "        return false;\n"
			+ "    }\n"
			+ "}\n\n"
		;
		//Post work order
		s += "function postworkorder(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before posting.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (confirm(\"Posting work order - are you sure you've saved all your changes?\")){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + POSTCOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		//Go to signature mode:
		s += "function gotosignaturemode(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before going to the signature.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + ACCEPTSIGNATURECOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		//Turn on the ability to view pricing:
		s += "function viewpricing(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before going to view pricing.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + VIEW_PRICING_COMMAND_VALUE + "\";\n"
			+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			//+ "    }\n"
			+ "}\n"
		;
		
		//Turn off the ability to view pricing:
		s += "function removepricing(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before removing the prices.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REMOVE_PRICING_COMMAND_VALUE + "\";\n"
			+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			//+ "    }\n"
			+ "}\n"
		;
		
		//Save
		s += "function save(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + SAVECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		
		//Email
		s += "function email(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + EMAILRECEIPTCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		//Print
		s += "function print(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + PRINTRECEIPTCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		//Find item
		s += "function finditem(itemfieldid){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = itemfieldid;\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
				
		//Clear signature
		s += "function clearsignature(){\n"
			+ "    flagDirty();\n"
			+ "    $('." + SMWorkOrderHeader.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).clearCanvas();\n"
			+ "}\n"
		;

		//Add detail sheet
		s += "function adddetailsheet(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ SMWorkOrderHeader.ADD_DETAIL_SHEET_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		//Create material return
		s += "function createMaterialReturn(){\n"
			+ "    if (!bScreenIsFullyDisplayed()){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + MATERIALRETURNCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		
		//Create folder/upload file(s)
		s += "function createanduploadfolder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		//Add most recent items
		s += "function recentitems(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 +  RECENTITEMSCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
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
		
		s += "function calculateTimes() {\n";
		s += "    //First get the variables to hold the times:\n";
		if (bAllowEditingLeftPreviousSiteTime){
			s += "    var lLeftPreviousSite = 0;\n";
			s += "    if (!isDate(document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramdattimeleftprevious + "\"].value)){\n";
			s += "        alert('LEFT PREVIOUS JOB date is not valid');\n";
			s += "        return;\n";
			s += "    }\n";
		}
		if (bAllowEditingArrivedAtCurrentSiteTime){
			s += "    var lArrivedAtCurrentSite = 0;\n";
			s += "    if (!isDate(document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramdattimearrivedatcurrent + "\"].value)){\n";
			s += "        alert('ARRIVED AT CURRENT JOB date is not valid');\n";
			s += "        return;\n";
			s += "    }\n";
		}
		if (bAllowEditingLeftCurrentSiteTime){
			s += "    var lLeftCurrentSite = 0;\n";
			s += "    if (!isDate(document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramdattimeleftcurrent + "\"].value)){\n";
			s += "        alert('LEFT CURRENT JOB date is not valid');\n";
			s += "        return;\n";
			s += "    }\n";

		}
		if (bAllowArrivedAtNextSiteTime){
			s += "    var lArrivedAtNextSite = 0;\n";
			s += "    if (!isDate(document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramdattimearrivedatnext + "\"].value)){\n";
			s += "        alert('ARRIVED AT NEXT JOB date is not valid');\n";
			s += "        return;\n";
			s += "    }\n";
		}
		
		s += "    var ElapsedTime = '';\n";
		s += "    var iHasLeftPreviousSite;\n";
		s += "    var iHasArrivedAtCurrentSite;\n";
		s += "    var iHasLeftCurrentSite;\n";
		s += "    var iHasArrivedAtNextSite;\n";

		s += "    //Get 'Left Previous Site' in minutes:\n"
			+ "    var lLeftPreviousSite = getDateAndTimeInMinutes('" + SMWorkOrderHeader.Paramdattimeleftprevious + "');\n" 
			+ "    //Get 'Arrived At Current Site' in minutes:\n"
			+ "    var lArrivedAtCurrentSite = getDateAndTimeInMinutes('" + SMWorkOrderHeader.Paramdattimearrivedatcurrent + "');\n"
			+ "    //Get 'Left Current Site' in minutes:\n"
			+ "    var lLeftCurrentSite = getDateAndTimeInMinutes('" + SMWorkOrderHeader.Paramdattimeleftcurrent + "');\n"
			+ "    //Get 'Arrived At Next Site' in minutes:\n"
			+ "    var lArrivedAtNextSite = getDateAndTimeInMinutes('" + SMWorkOrderHeader.Paramdattimearrivedatnext + "');\n"
		;
		
	    s += "    //figure out which times are blank and which are not:\n"
    		+ "    var radioButtons = document.getElementsByName('" + SMWorkOrderHeader.Paramhasdattimeleftprevious + "');\n"
    		+ "    for (var x = 0; x < radioButtons.length; x ++) {\n"
    		+ "        if (radioButtons[x].checked) {\n"
    		+ "            iHasLeftPreviousSite = radioButtons[x].value;\n"
    		+ "        }\n"
    		+ "    }\n"
   		;

	    s += "    var radioButtons = document.getElementsByName('" + SMWorkOrderHeader.Paramhasdattimearrivedatcurrent + "');\n"
    		+ "    for (var x = 0; x < radioButtons.length; x ++) {\n"
    		+ "        if (radioButtons[x].checked) {\n"
    		+ "            iHasArrivedAtCurrentSite = radioButtons[x].value;\n"
    		+ "        }\n"
    		+ "    }\n"
   		;
	    
	    s += "    var radioButtons = document.getElementsByName('" + SMWorkOrderHeader.Paramhasdattimeleftcurrent + "');\n"
    		+ "    for (var x = 0; x < radioButtons.length; x ++) {\n"
    		+ "        if (radioButtons[x].checked) {\n"
    		+ "            iHasLeftCurrentSite = radioButtons[x].value;\n"
    		+ "        }\n"
    		+ "    }\n"
   		;
	    
	    s += "    var radioButtons = document.getElementsByName('" + SMWorkOrderHeader.Paramhasdattimearrivedatnext + "');\n"
    		+ "    for (var x = 0; x < radioButtons.length; x ++) {\n"
    		+ "        if (radioButtons[x].checked) {\n"
    		+ "            iHasArrivedAtNextSite = radioButtons[x].value;\n"
    		+ "        }\n"
    		+ "    }\n"
   		;

	    s += "    //Now set the visible values:\n";
		if (bAllowEditingLeftPreviousSiteTime && bAllowEditingArrivedAtCurrentSiteTime){
			s += "    if ((iHasLeftPreviousSite == '0') || (iHasArrivedAtCurrentSite == '0')){\n"
				+ "        ElapsedTime = '(Required times are not all entered)';\n"
				+ "    }else{\n"
				+ "        ElapsedTime = getElapsed(lLeftPreviousSite, lArrivedAtCurrentSite);\n"
				+ "    }\n"
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.ELAPSEDTIME1 + "\"].value = ElapsedTime;\n";
		}
		if (bAllowEditingLeftPreviousSiteTime && bAllowEditingLeftCurrentSiteTime){
			s += "    if ((iHasLeftPreviousSite == '0') || (iHasLeftCurrentSite == '0')){\n"
				+ "        ElapsedTime = '(Required times are not all entered)';\n"
				+ "    }else{\n"
				+ "        ElapsedTime = getElapsed(lLeftPreviousSite, lLeftCurrentSite);\n"
				+ "    }\n"
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.ELAPSEDTIME2 + "\"].value = ElapsedTime;\n";
		}
		if (bAllowEditingLeftPreviousSiteTime && bAllowArrivedAtNextSiteTime){
			s += "    if ((iHasLeftPreviousSite == '0') || (iHasArrivedAtNextSite == '0')){\n"
				+ "        ElapsedTime = '(Required times are not all entered)';\n"
				+ "    }else{\n"
				+ "        ElapsedTime = getElapsed(lLeftPreviousSite, lArrivedAtNextSite);\n"
				+ "    }\n"
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.ELAPSEDTIME3 + "\"].value = ElapsedTime;\n";
		}
		if (bAllowEditingArrivedAtCurrentSiteTime && bAllowEditingLeftCurrentSiteTime){
			s += "    if ((iHasArrivedAtCurrentSite == '0') || (iHasLeftCurrentSite == '0')){\n"
				+ "        ElapsedTime = '(Required times are not all entered)';\n"
				+ "    }else{\n"
				+ "        ElapsedTime = getElapsed(lArrivedAtCurrentSite, lLeftCurrentSite);\n"
				+ "    }\n"
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.ELAPSEDTIME4 + "\"].value = ElapsedTime;\n";
		}
		if (bAllowEditingArrivedAtCurrentSiteTime && bAllowArrivedAtNextSiteTime){
			s += "    if ((iHasArrivedAtCurrentSite == '0') || (iHasArrivedAtNextSite == '0')){\n"
				+ "        ElapsedTime = '(Required times are not all entered)';\n"
				+ "    }else{\n"
				+ "        ElapsedTime = getElapsed(lArrivedAtCurrentSite, lArrivedAtNextSite);\n"
				+ "    }\n"
				+ "    document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.ELAPSEDTIME5 + "\"].value = ElapsedTime;\n";
		}

		s += "}\n\n";

		s += "function getDateAndTimeInMinutes(sDateTimeFieldName){\n"
			+ "    //This function reads the date and time from an entry and returns it in the corresponding milliseconds\n"
			+ "    //First get the date in the string:\n"
			+ "    var sDateTime = document.forms[\"MAINFORM\"].elements[sDateTimeFieldName].value;\n"
			+ "    //Get the hours:\n"
			+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedHour');\n"
			+ "    sDateTime = sDateTime + ' ' + e.options[e.selectedIndex].value;\n"
			+ "    //Get the minutes:\n"
			+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedMinute');\n"
			+ "    sDateTime = sDateTime + ':' + e.options[e.selectedIndex].value;\n"
			+ "    sDateTime = sDateTime + ':00';\n"
			+ "    //Get the AM/PM value:\n"
			+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedAMPM');\n"
			+ "    var iAMPMValue = e.options[e.selectedIndex].value;\n"
			+ "    if (iAMPMValue == '1'){\n"
			+ "        sDateTime = sDateTime + ' PM';\n"
			+ "    } else {\n"
			+ "        sDateTime = sDateTime + ' AM';\n"
			+ "    }\n"
			+ "    var lDateTime = Date.parse(sDateTime);\n"
			+ "    return lDateTime/(1000 * 60);\n"
			+ "}\n"
		;
				
		s += "function getElapsed(start, end) {\n"
			+ "    var sElapsed = '';\n"
			+ "    "// total time difference in minutes\n"
			+ "    var timeDiff = end - start;\n"
			+ "    "// get minutes\n"
			+ "    var minutes = Math.round(timeDiff % 60);\n"
			+ "    // remove minutes from the date\n"
			+ "    timeDiff = timeDiff - minutes;\n"
			+ "    // get hours\n"
			+ "    var hours = (timeDiff / 60) % 24;\n"
			+ "    // remove hours from the date\n"
			+ "    timeDiff = timeDiff - hours * 60;\n"
			+ "    // the rest of timeDiff is number of days\n"
			+ "    var days = timeDiff / (60 * 24);\n"
			+ "    if (days > 0){\n"
			+ "        sElapsed = sElapsed + Math.floor(days) + ' day(s), ';\n"
			+ "    }\n"
			+ "    sElapsed = sElapsed + Math.floor(hours) + ' hour(s), ';\n"
			+ "    sElapsed = sElapsed + Math.floor(minutes) + ' minute(s)';\n"
			+ "    return sElapsed;\n"
			+ "}\n\n"
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
		
		s += "function getCurrentTimeValue(sDateTimeFieldName){\n"
				+ "    //This function reads the date and time from an entry\n"
				+ "    //First get the date in the string:\n"
				+ "    var sDateTime = document.forms[\"MAINFORM\"].elements[sDateTimeFieldName].value;\n"
				+ "    //Get the hours:\n"
				+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedHour');\n"
				+ "    sDateTime = sDateTime + ' ' + e.options[e.selectedIndex].value;\n"
				+ "    //Get the minutes:\n"
				+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedMinute');\n"
				+ "    sDateTime = sDateTime + ':' + e.options[e.selectedIndex].value;\n"
				+ "    //Get the AM/PM value:\n"
				+ "    var e = document.getElementById(sDateTimeFieldName + 'SelectedAMPM');\n"
				+ "    var iAMPMValue = e.options[e.selectedIndex].value;\n"
				+ "    if (iAMPMValue == '1'){\n"
				+ "        sDateTime = sDateTime + ' PM';\n"
				+ "    } else {\n"
				+ "        sDateTime = sDateTime + ' AM';\n"
				+ "    }\n"
				+ "    var lDateTime = Date.parse(sDateTime);\n"
				+ "    return sDateTime;\n"
				+ "}\n"
			;
		
		//ASYNC request
		s += "function asyncUpdate(sParamName) {\n"
                + "var xhr = new XMLHttpRequest();\n\n" 
                //Define how the response should be handled
                + "xhr.onreadystatechange = function(){\n"  
                		//If the response is ready then display it in the status
                + "    if (this.readyState == 4 && this.status == 200){\n"
                			//If there is a warning is the response
                + "   		if (this.responseText.includes(\"Warning\")){\n"
                + "        		 document.getElementById(\"Warning\").innerHTML = this.responseText; \n"
                + "        		 document.getElementById(\"Status\").innerHTML = \"\"; \n"
                			//Otherwise display response as status message
                + "    		}else{\n"
                + "         	document.getElementById(\"Status\").innerHTML = this.responseText; \n"
                + "         	document.getElementById(\"Warning\").innerHTML = \"\"; \n"
                + "			}\n" 
                + "    }"
                		//The request completely failed.  
                + "     if (this.readyState == 4 && this.status != 200){\n"
                + "         document.getElementById(\"Warning\").innerHTML = 'WARNING:' + 'Request to update Left previous job time failed.'; \n"
                + "	   }\n" 
                +"};\n\n"
                //Send the request
                // MISSING DB?
                + "xhr.open(\"POST\", \"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderAction" + "\");\n" 
                + "xhr.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");\n"
                //+ "alert(\"Sending request.. Updating \" + sParamName + \"=\" +getCurrentTimeValue(sParamName));\n"
                + "xhr.send(\"" + COMMAND_FLAG + "=" + ASYNC_POST 
                + "&lid=" + workorder.getlid() 
                + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" +  smmaster.getsDBID() 
                + "&\" + sParamName + \"=\" + getCurrentTimeValue(sParamName) );\n"
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

		s+= "@media screen and (max-width: 991px) {\n" + 
				" .picker.modal-dialog {\n" + 
				"    max-width: 355px !important;\n" + 
				" }\n" + 
				" .picker.modal-dialog-content.picker-dialog-content{\n" + 
				"     max-width: 355px !important;\n" + 
				" }\n" + 
				"}";
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
	
	private static String Create_Edit_Form_Switch_Input_Row (
			String sFieldName,
			String sDateFieldName,
			String sPresetDate,
			String sDefaultValue,
			String sLabel,
			String sRemark,
			String sOnChange,
			boolean bDisabled,
			ServletContext context
	){

		String sRow = "<TR>";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>";

		sRow += "<TD ALIGN=LEFT>";
		String sDisabled = "";
		if(!bDisabled) {sDisabled="disabled";}
		sRow +=" <div class=\"custom-control custom-switch\">\n" +
				"  <input name=\"" + sFieldName + "\""
						+ " type=\"checkbox\""
						+ " class=\"custom-control-input\""
						+ " id=\""+ sFieldName + "\""
						+ " value=\"1\" "
						+ sDefaultValue + " "
						+ sDisabled
						+ " data-size=\"large\""
						+ " onchange=\"" + sOnChange + "\">"
						+ "\n" + 
			"  <label class=\"custom-control-label\" for=\"" + sFieldName + "\">" + "" + "</label>\n" +
			 Create_Edit_Form_DateTime_Input_Field(sDateFieldName, sPresetDate, context) +
				"</div>\n";

		sRow += "</TD>";

		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		sRow += "</TR>";
		return sRow;
	}

	private static String Create_Edit_Form_DateTime_Input_Field (
			String sDateFieldName,
			String sValue,
			ServletContext context
	){
	
		//The value of sValue should look something like this: "12/1/2010 03:59 PM"
		String sDatePortion = sValue.substring(0, sValue.indexOf(" ")).trim();
		String sTimePortion = sValue.substring(sValue.indexOf(" "), sValue.length()).trim();

		String s = "<INPUT TYPE=TEXT NAME=\"" + sDateFieldName + "\"";
		s += " VALUE=\"" + sDatePortion + "\"";
		s += " onchange=\"flagDirty();\"";
		s += " ID=\"" + sDateFieldName + "\"";
		s += "SIZE=7";
		s += " MAXLENGTH=10";
		s += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		s += ">";
		s += SMUtilities.getDatePickerString(sDateFieldName, context) + "&nbsp;";
	
		int iMinute = Integer.parseInt(
				sTimePortion.substring(sTimePortion.indexOf(":") + 1, sTimePortion.indexOf(":") + 3));
		int iAMPM = 0;
		if (clsStringFunctions.StringRight(sTimePortion, 2).compareToIgnoreCase("AM") == 0){
			iAMPM = 0;
		}else{
			iAMPM = 1;
		}
		String sHour = sTimePortion.substring(0, sTimePortion.indexOf(":"));
		int iHour = Integer.parseInt(sHour);
		if (iHour == 0 && iAMPM == 1){
			iHour = 12;
		}
		s += "Hour <SELECT NAME=\"" + sDateFieldName + "SelectedHour\" onchange=\"flagDirty();\" ID =\"" + sDateFieldName + "SelectedHour\">";
		for (int i=1; i<=12;i++){
			if (i == iHour){
				s += "<OPTION SELECTED VALUE = " + i + ">" + i  + " &nbsp;&nbsp;&nbsp;";
			}else{
				s += "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s += "</SELECT>";
		s += "Minute <SELECT NAME=\"" + sDateFieldName + "SelectedMinute\" onchange=\"flagDirty();\" ID =\"" + sDateFieldName + "SelectedMinute\">";
		for (int i=0; i<=59;i++){
			String sMinute = clsStringFunctions.PadLeft(Integer.toString(i), "0", 2);
			if (i == iMinute){
				s += "<OPTION SELECTED VALUE = " 
					+ sMinute + ">" + sMinute + " &nbsp;&nbsp;&nbsp;";
			}else{
				s += "<OPTION VALUE = " + sMinute + ">" + sMinute + "";
			}
		}
		s += "</SELECT>";	
		s += "AM/PM<SELECT NAME=\"" + sDateFieldName + "SelectedAMPM\" onchange=\"flagDirty();\" ID =\"" + sDateFieldName + "SelectedAMPM\">";
		for (int i=Calendar.AM; i<=Calendar.PM;i++){
			if (i == iAMPM){
				if (i == Calendar.AM){
					s+= "<OPTION SELECTED VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION SELECTED VALUE = " + Calendar.PM + ">" + "PM";
				}		
			}else{
				if (i == Calendar.AM){
					s += "<OPTION VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION VALUE = " + Calendar.PM + ">" + "PM";
				}
			}
		}
	
		s += "</SELECT>";
		return s;
	}
	public static String createJobEntryTimesTable(
		SMMasterEditEntry sm,
		ServletContext context,
		SMWorkOrderHeader workorder, 
		String sClassName) throws Exception{
		String s = "";
		
		s += "<TABLE class = \"  innermost \" style=\" title:JobEntryTimesTable; background-color: "
			+ SMWorkOrderHeader.JOBTIMES_TABLE_BG_COLOR + "; \" width=100% >\n";
		s += "<TR>";
		//Record the four permissions for editing time:
		boolean bAllowEditingLeftPreviousSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditLeftPreviousSiteTime, 
			sm.getUserID(), 
			context, 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowEditingArrivedAtCurrentSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditArrivedAtCurrentSiteTime, 
			sm.getUserID(), 
			context, 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowEditingLeftCurrentSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditLeftCurrentSiteTime, 
			sm.getUserID(), 
			context, 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bAllowArrivedAtNextSiteTime = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMEditArrivedAtNextSiteTime, 
			sm.getUserID(), 
			context, 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Start the mechanics' site times here:
		String sDefaultDate = SMUtilities.EMPTY_DATETIME_VALUE;
		clsDBServerTime st = null;
		
		try {
			st = new clsDBServerTime(sm.getsDBID(), sm.getUserName(), context);
			sDefaultDate = st.getCurrentDateTimeInSelectedFormat("M/d/yyyy hh:mm a");
		} catch (Exception e) {
			//Let it go as a blank...
		}
		
		//Left previous site:
		String sPresetDate = sDefaultDate;
	    if (workorder.getdattimeleftprevious().replace("\"", "&quot;").compareToIgnoreCase(
	    		clsMasterEntry.EMPTY_DATETIME_STRING) != 0){
	    	sPresetDate = workorder.getdattimeleftprevious().replace("\"", "&quot;");
	    }       	
	    
	    String sDefaultValue = "";
		if (workorder.getdattimeleftprevious().startsWith(SMWorkOrderHeader.EMPTY_DATE_STRING)){
			sDefaultValue = "";
		}else{
			sDefaultValue = "checked";		
		}
		//If we are displaying the job time fields AND the user has permissions to this particular 'job time':
			s += Create_Edit_Form_Switch_Input_Row(
				SMWorkOrderHeader.Paramhasdattimeleftprevious, 
				SMWorkOrderHeader.Paramdattimeleftprevious,
				sPresetDate,
				sDefaultValue,
				"<B>Left previous job:</B>", 
				"", 
				"asyncUpdate('" + SMTableworkorders.dattimeleftprevious + "');",
				bAllowEditingLeftPreviousSiteTime,
				context
				);

		
		//Arrived at current site:
		sPresetDate = sDefaultDate;
	    if (workorder.getdattimearrivedatcurrent().replace("\"", "&quot;").compareToIgnoreCase(
	    		clsMasterEntry.EMPTY_DATETIME_STRING) != 0){
	    	sPresetDate = workorder.getdattimearrivedatcurrent().replace("\"", "&quot;");
	    }
	    
	    sDefaultValue = "";
		if (workorder.getdattimearrivedatcurrent().startsWith(SMWorkOrderHeader.EMPTY_DATE_STRING)){
			sDefaultValue = "";
		}else{
			sDefaultValue = "checked";		
		}
		
			s += Create_Edit_Form_Switch_Input_Row(
				SMWorkOrderHeader.Paramhasdattimearrivedatcurrent, 
				SMWorkOrderHeader.Paramdattimearrivedatcurrent,
				sPresetDate,
				sDefaultValue,
				"<B>Arrived at current job:</B>", 
				"", 
				"asyncUpdate('" + SMTableworkorders.dattimearrivedatcurrent + "');",
				bAllowEditingArrivedAtCurrentSiteTime,
				context
				);

		//Left current site:
		sPresetDate = sDefaultDate;
	    if (workorder.getdattimeleftcurrent().replace("\"", "&quot;").compareToIgnoreCase(
	    		clsMasterEntry.EMPTY_DATETIME_STRING) != 0){
	    	sPresetDate = workorder.getdattimeleftcurrent().replace("\"", "&quot;");
	    }

	    sDefaultValue = "";
		if (workorder.getdattimeleftcurrent().startsWith(SMWorkOrderHeader.EMPTY_DATE_STRING)){
			sDefaultValue = "";
		}else{
			sDefaultValue = "checked";		
		}
		
		s += Create_Edit_Form_Switch_Input_Row(
				SMWorkOrderHeader.Paramhasdattimeleftcurrent, 
				SMWorkOrderHeader.Paramdattimeleftcurrent,
				sPresetDate,
				sDefaultValue,
				"<B>Left current job:</B>", 
				"", 
				"asyncUpdate('" + SMTableworkorders.dattimeleftcurrent + "');",
				bAllowEditingLeftCurrentSiteTime,
				context
				);
		
		
		//Arrived at next site:
		sPresetDate = sDefaultDate;
	    if (workorder.getdattimearrivedatnext().replace("\"", "&quot;").compareToIgnoreCase(
	    		clsMasterEntry.EMPTY_DATETIME_STRING) != 0){
	    	sPresetDate = workorder.getdattimearrivedatnext().replace("\"", "&quot;");
	    }
	    sDefaultValue = "";
		if (workorder.getdattimearrivedatnext().startsWith(SMWorkOrderHeader.EMPTY_DATE_STRING)){
			sDefaultValue = "";
		}else{
			sDefaultValue = "checked";		
		}
		
		s += Create_Edit_Form_Switch_Input_Row(
				SMWorkOrderHeader.Paramhasdattimearrivedatnext, 
				SMWorkOrderHeader.Paramdattimearrivedatnext,
				sPresetDate,
				sDefaultValue,
				"<B>Arrived at next job::</B>", 
				"", 
				"asyncUpdate('" + SMTableworkorders.dattimearrivedatnext + "');",
				bAllowArrivedAtNextSiteTime,
				context
				);
		
		s += "<TR>";
		s += "<TD>";
//		s += "<SUP>1</SUP>&nbsp;Date/time fields are left blank until a specified date or time is selected.";
		//To have any calculations appear, we need at least TWO times to appear:
		int iTimePermissionsCounter = 0;
		if (bAllowEditingLeftPreviousSiteTime) iTimePermissionsCounter++;
		if (bAllowEditingArrivedAtCurrentSiteTime) iTimePermissionsCounter++;
		if (bAllowEditingLeftCurrentSiteTime) iTimePermissionsCounter++;
		if (bAllowArrivedAtNextSiteTime) iTimePermissionsCounter++;
		
		if (iTimePermissionsCounter >= 2){
			//Calculate button:
			s += "<BR><button type=\"button\""
			+ " value=\"" + SMWorkOrderHeader.CALCULATE_TIMES_LABEL + "\""
			+ " name=\"" + SMWorkOrderHeader.CALCULATE_TIMES_LABEL + "\""
			+ " onClick=\"calculateTimes();\">"
			+ SMWorkOrderHeader.CALCULATE_TIMES_LABEL
			+ "</button>\n"
			;
			s += "</TD>";
			s += " </TR>";
			/*
			Possible elapsed times combinations:
			1) From leaving the previous job until arriving at the current one (travel time TO the job)
			2) From leaving the previous job until leaving the current one (time at the job plus travel TO the job)
			3) From leaving the previous job until arriving at the next one (time at the job plus travel time TO AND FROM the job)
			4) From arriving at the current job until leaving the current one (time at the job)
			5) From arriving at the current job until arriving at the next one (time at the job plus travel FROM the job)
			*/
			if (bAllowEditingLeftPreviousSiteTime && bAllowEditingArrivedAtCurrentSiteTime){
				s += "<TR><TD ALIGN=RIGHT>Travel time TO the job:</TD><TD>"
					+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SMWorkOrderHeader.ELAPSEDTIME1 + "\""
					+ " ID = \"" + SMWorkOrderHeader.ELAPSEDTIME1 + "\""	
					+ " SIZE=" + "25"
					+ "></TD></TR>"		
				;
			}
			if (bAllowEditingLeftPreviousSiteTime && bAllowEditingArrivedAtCurrentSiteTime && bAllowEditingLeftCurrentSiteTime){
				s += "<TR><TD ALIGN=RIGHT>Time at the job PLUS travel time TO the job:</TD><TD>"
					+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SMWorkOrderHeader.ELAPSEDTIME2 + "\""
					+ " ID = \"" + SMWorkOrderHeader.ELAPSEDTIME2 + "\""	
					+ " SIZE=" + "25"
					+ "></TD></TR>"			
				;
			}
			if (bAllowEditingLeftPreviousSiteTime && bAllowEditingArrivedAtCurrentSiteTime && bAllowEditingLeftCurrentSiteTime
					&& bAllowArrivedAtNextSiteTime){
				s += "<TR><TD ALIGN=RIGHT>Time at the job PLUS travel time TO AND FROM the job:</TD><TD>"
					+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SMWorkOrderHeader.ELAPSEDTIME3 + "\""
					+ " ID = \"" + SMWorkOrderHeader.ELAPSEDTIME3 + "\""	
					+ " SIZE=" + "25"
					+ "></TD></TR>"			
				;
			}
			if (bAllowEditingArrivedAtCurrentSiteTime && bAllowEditingLeftCurrentSiteTime){
				s += "<TR><TD ALIGN=RIGHT>Time at the job ONLY:</TD><TD>"
					+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SMWorkOrderHeader.ELAPSEDTIME4 + "\""
					+ " ID = \"" + SMWorkOrderHeader.ELAPSEDTIME4 + "\""	
					+ " SIZE=" + "25"
					+ "></TD></TR>"			
				;
			}
			if (bAllowEditingArrivedAtCurrentSiteTime && bAllowEditingLeftCurrentSiteTime && bAllowArrivedAtNextSiteTime){
				s += "<TR><TD ALIGN=RIGHT>Time at the job PLUS travel FROM the job:</TD><TD>"
					+ "<INPUT TYPE=TEXT readonly=\"readonly\" NAME = \"" + SMWorkOrderHeader.ELAPSEDTIME5 + "\""
					+ " ID = \"" + SMWorkOrderHeader.ELAPSEDTIME5 + "\""	
					+ " SIZE=" + "25"
					+ "></TD></TR>"			
				;
			}
		}
	s += "</TABLE title:JobEntryTimesTable; \">\n";
	return s;
	}
}