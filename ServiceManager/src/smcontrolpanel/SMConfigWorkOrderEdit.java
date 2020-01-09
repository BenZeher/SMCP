package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
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

import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderDetail;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableworkorderdetails;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMConfigWorkOrderEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String CHECKBOX_MARKER = "CHECKBOXMARKER";
	public static final String REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION = "REMOVEWOATTRIBUTE";
	
	//Commands:
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String VIEW_PRICING_LABEL = "<B><FONT COLOR=RED>V</FONT></B>iew current prices"; //V
	public static final String VIEW_PRICING_COMMAND_VALUE = "VIEWPRICING";
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; //D
	public static final String DELETECOMMAND_VALUE = "DELETE";
	public static final String ORDERFINDERLABEL = "Find order";
	public static final String EXTENDEDORDERFINDERLABEL = "Find order (extended)";
	public static final String ORDERFINDERBUTTON = "ORDERFINDERBUTTON";
	public static final String EXTENDEDORDERFINDERBUTTON = "EXTENDEDORDERFINDERBUTTON";
	public static final String RETURNINGFROMORDERFINDER = "RETURNINGFROMORDERFINDER";
	public static final String ASSIGNREMAININGQTY_BUTTON_LABEL = "Assign <B><FONT COLOR=RED>R</FONT></B>EMAINING qty to selected lines"; //R
	public static final String ASSIGNREMAININGQTY_VALUE = "ASSIGNREMAININGQTY";
	public static final String ASSIGNTOTALQTY_BUTTON_LABEL = "Assign <B><FONT COLOR=RED>T</FONT></B>OTAL qty to selected lines"; //T
	public static final String ASSIGNTOTALQTY_VALUE = "ASSIGNTOTALQTY";
	
	public static final String UNPOST_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>npost work order"; //U
	public static final String UNPOSTCOMMAND_VALUE = "UNPOSTWORKORDER";
	public static final String POST_BUTTON_LABEL = "Post <B><FONT COLOR=RED>w</FONT></B>ork order"; //W
	public static final String POSTCOMMAND_VALUE = "POSTWORKORDER";

	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	
	public static final String NUMBER_OF_ITEM_LINES_USED = "NUMOFITEMLINESUSED";
	public static final String TOTAL_NUMBER_OF_WPC_CODES = "TOTALNUMOFWPCCODES";
	public static final int OVERALL_LENGTH_OF_PADDED_LINE_NUMBER = 8;
	public static final String FORM_NAME = "MAINFORM";
	private static final String ITEM_QTY_FIELD_WIDTH = "8";
	private static final int MAXIMUM_JOB_SEQUENCE_NUMBER = 30;
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
				"smcontrolpanel.SMConfigWorkOrderAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMConfigureWorkOrders
		);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMConfigureWorkOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		String sReturnToTruckSchedule = clsManageRequestParameters.get_Request_Parameter(
				SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM, request);
		SMWorkOrderHeader wohead = new SMWorkOrderHeader();
		try {
			wohead.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smedit.getPWOut().println("Error loading work order from request: " + e2.getMessage());
			return;
		}
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a work order object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		//If we are signaled to remove the 'WORK ORDER' attribute, then remove it first - this may happen if someone is coming in from
		//a link to configure a work order, rather than coming in from a 'resubmit'.
		if (clsManageRequestParameters.get_Request_Parameter(REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION, request).compareToIgnoreCase("") != 0){
			currentSession.removeAttribute(SMTableworkorders.ObjectName);
		}
		if (currentSession.getAttribute(SMTableworkorders.ObjectName) != null){
			wohead = (SMWorkOrderHeader) currentSession.getAttribute(SMTableworkorders.ObjectName);
			//Once we've grabbed the work order from the session, make sure we remove it:
			currentSession.removeAttribute(SMTableworkorders.ObjectName);
			
			//Here we want to check to make sure that the ID of the work order passed in the request matches what's in the
			//session object.  If they don't match it's possible that we've picked up a work order session object from a
			//different work order, possibly one that was passed on from another tab in the browser:
			String sWOIDFromRequest = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramlid, request);
			String sWOIDFromSession = wohead.getlid();
			if(sWOIDFromSession.compareToIgnoreCase(sWOIDFromRequest) != 0){
				currentSession.removeAttribute(SMTableworkorders.ObjectName);
				smedit.getPWOut().println("Error [1428075492] work order ID in request ('" +  sWOIDFromRequest 
					+ "') and work order ID from session object ('" + sWOIDFromSession + "') do not match.");
				return;
			}
			
			//If we are returning from the finder, looking for an order number, then we have to insert the 'found' 
			//order number into the work order, because the one in the session object won't be the one
			//the 'searched' for:
			if (clsManageRequestParameters.get_Request_Parameter(RETURNINGFROMORDERFINDER, request).compareToIgnoreCase("") != 0){
				wohead.setstrimmedordernumber(clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramstrimmedordernumber, request));
			}
			
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			try {
				//If it's a NEW work order, then just proceed, but if it's NOT, then we need to try to load the order:
				if (!wohead.isOrderNew()){
					if(!wohead.load(smedit.getsDBID(), smedit.getFullUserName(), getServletContext())){
						//JC2WO - have to figure out what happens when they are creating the work order for the first time, and
						// there's no ID yet:
						smedit.getPWOut().println("Error [1425920365] work order # " + wohead.getlid() + "' could not be loaded.");
						return;
					}
				}
			} catch (Exception e) {
				smedit.getPWOut().println("Error loading work order - " + e.getMessage());
				return;
			}
		}

		//smedit.printLowProfileHeaderTable();
	    smedit.getPWOut().println(getHeaderString(
			"Configure Work order", 
			"", 
			SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
			SMUtilities.DEFAULT_FONT_FAMILY,
			(String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME)
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

	    //Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "\">Return to user login</A>");
		
		//Print a link to the Truck Schedule:
		String sEditTruckSchedule = "";
		if (SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewTruckSchedules, 
			smedit.getUserID(), 
			getServletContext(), 
			smedit.getsDBID(),
			(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
		){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMConfigureWorkOrders, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			){
				sEditTruckSchedule = "Y";
			}
			
			smedit.getPWOut().println("&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMViewTruckScheduleSelection?AllowScheduleEditing=" + sEditTruckSchedule
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Schedule</A>")
			;
		}
	    
		try{
			smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
				+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
			);
			createEditPage(
				getEditHTML(smedit, wohead, SMTableworkorders.ObjectName),
				FORM_NAME,
				smedit.getPWOut(),
				smedit,
				sReturnToTruckSchedule
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
			SMMasterEditEntry sm,
			String sReturnToTruckSchedule
	){

		String sFormString = "<FORM ID='" + FORM_NAME + "' NAME='" + FORM_NAME + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">\n");
		//Store whether or not we need to return to the truck schedule:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "\" VALUE=\"" 
				+ sReturnToTruckSchedule + "\">");
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");

	}
	
	private String getEditHTML(SMMasterEditEntry sm, SMWorkOrderHeader wo_entry, String sObjectName) throws Exception{
		
		SMOrderHeader orderheader = new SMOrderHeader();
		orderheader.setM_strimmedordernumber(wo_entry.getstrimmedordernumber());
		//If there is already an order number, try to load that order:
		if (wo_entry.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			if (!orderheader.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
				//If we can't load it, then don't worry about that unless it's already been saved - then throw an error:
				if (!wo_entry.isOrderNew()){
					throw new Exception("Error [1426878875] - Could not load order header '" + wo_entry.getstrimmedordernumber()
						+ "' - " + orderheader.getErrorMessages());
				}
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
		s += sCommandScripts(
			wo_entry, 
			sm,
			bAllowEditingLeftPreviousSiteTime,
			bAllowEditingArrivedAtCurrentSiteTime,
			bAllowEditingLeftCurrentSiteTime,
			bAllowArrivedAtNextSiteTime);
		s += sStyleScripts();
		
		//Store whether or not the record has been changed this includes ANY change, including approval:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">"
			+ "\n";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">"
		+ "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlid + "\" VALUE=\"" 
				+ wo_entry.getlid() + "\">" + "\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramltimestamp + "\" VALUE=\"" 
				+ wo_entry.getstimestamp() + "\">" + "\n";

		//Record the time the use last loaded the work order:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramlastreadrecordtimestamp + "\" VALUE=\"" 
			+ wo_entry.getslastreadrecordtimestamp() + "\">" + "\n";

		try {
			s += "<INPUT TYPE=HIDDEN NAME\""+SMWorkOrderHeader.Paramsdbaworkorderlogo+"\" VALUE=\""
					+getWorkOrderLogo(sm,orderheader.getM_idoingbusinessasaddressid())+ "\"> \n"; 
		}catch(Exception e) {
			throw new Exception("ERROR [1549052144] unable to get work order receipt logo "+e.getMessage());
		}
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Parammmanagernotes + "\" VALUE=\"" 
				+ wo_entry.getmmanagernotes() + "\">";
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.Parammadditionalworkcomments
			+ " VALUE=\"" + wo_entry.getmadditionalworkcomments() + "\""
			+ ">"
			;
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
				SMUtilities.getFullClassName(this.toString()))
			+ "</TD></TR>";
		
		//Create the order commands line at the top:
		s += "<TR><TD>" + createCommandsTable(
			wo_entry, 
			sm.getUserID(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		) + "</TD></TR>";
		
		//Create the schedule-related fields:
		//TODO - set this value correctly:
		s += "<TR><TD>" + createScheduleFieldsTable(wo_entry,sm)
			+ "</TD></TR>";

		s += createInstructionsTable(wo_entry, orderheader);
		
		//Create the items table:
		s += "<TR><TD>" + createItemsTable(
			sm, 
			wo_entry, 
			orderheader,
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
		+ "</TD></TR>";
		
		//Create work performed codes table:
		s += "<TR><TD>" + createWorkPerformedTable(wo_entry);
		
		//Create Manager Notes table:
		//TJR - 3/9/2015 - removed this temporarily to see if it's needed:
		//s += createManagerNotesTable(wo_entry, orderheader);
		
		//Create the order commands line at the bottom:
		s += "<TR><TD>" + createCommandsTable(
			wo_entry, 
			sm.getUserID(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		) + "</TD></TR>";

		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		return s;
	}
	
	
	private String getWorkOrderLogo(SMMasterEditEntry sm, String m_idoingbusinessasaddressid) throws Exception{
		String SQL = "SELECT "+SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo +" FROM "+SMTabledoingbusinessasaddresses.TableName
				+" WHERE "+SMTabledoingbusinessasaddresses.lid+" = "+m_idoingbusinessasaddressid;
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".load - user: " + sm.getUserID() + " - " + sm.getFullUserName() + "   [1332178334] "
		);

		if (conn == null){
			throw new Exception ("ERROR [1546457943] unable to instantiate a connection");
		}
		
		String sWorkOrderLogo = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			sWorkOrderLogo = "";
			while(rs.next()) {
				sWorkOrderLogo = rs.getString(SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo);
			}
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080414]");
			throw new Exception ("Error [1546608430] error reading resultset - " + e.getMessage());
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080415]");
		
		return sWorkOrderLogo;
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
		String sWorkOrderID = "(NEW)";
		String sLinkToWorkOrderEdit = "";
		if(!workorder.isOrderNew()){
			sWorkOrderID = workorder.getlid();

			//ALso, if it's NOT a new work order, create a link to EDIT it:
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditWorkOrders, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				sLinkToWorkOrderEdit = "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
					+ "smcontrolpanel.SMWorkOrderEdit?" + SMWorkOrderHeader.Paramlid + "=" 
					+ sWorkOrderID
					+ "&CallingClass=" + SMUtilities.getFullClassName(sm.getCallingClass())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
					+ "\">"
					+ "Edit</A>"
			;
			}
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
			&& (workorder.getstrimmedordernumber().compareToIgnoreCase("") !=0)
		){
			sLinkToWorkOrderList = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + orderheader.getM_strimmedordernumber() 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
				+ "#WorkOrders"
				+ "\">" + "Work order list" + "</A>"
			;
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
				sClassName + ".createOrderHeaderTable - user: " 
				+ sm.getUserID()
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
		
		s +=
			"<TD class=\" fieldlabel \">WO&nbsp;#:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sWorkOrderID;
		
		if (sLinkToWorkOrderEdit.compareToIgnoreCase("") != 0){
			s += sLinkToWorkOrderEdit;
		}
		if (sLinkToWorkOrderList.compareToIgnoreCase("") != 0){
			s += "<TD class=\"readonlyleftfield\">" + sLinkToWorkOrderList + "</TD>"
			;
		}
		s += "</TD>";

		s += "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sPosted 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramiposted + "\""
			+ " ID=\"" + SMWorkOrderHeader.Paramiposted + "\""
			+ " VALUE=\"" + clsStringFunctions.filter(workorder.getsposted()) + "\">"
			+ "</TD>"
				
			+ "<TD class=\" fieldlabel \">Imported?:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sImported + "</TD>"
		
			+ "<TD class=\" fieldlabel \">Terms:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sTerms() + "</TD>"

			+ "<TD class=\" fieldlabel \">Sales&nbsp;#:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + 	orderheader.getM_sSalesperson() + "-" + sSalespersonName + "</TD>"

			+ "<TD class=\" fieldlabel \">Special&nbsp;wage&nbsp;rate:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sSpecialWageRate() + "</TD>"			
			
			+ "<TD class=\" fieldlabel \">Bill&nbsp;to:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sBillToName() + "</TD>"
			;
		s += "</TR>";
		s += "</TABLE title:OrderHeaderTable; \">\n";	
		
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable2; \" width=100% >\n";	
		s += "<TR>";
		
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
		//Close the table:
		s += "</TABLE style = \" title:OrderHeaderTable2; \">\n";
		
		s += "<TABLE class = \" innermost \" style=\" title:EditInformationTable; \" width=100% >\n";	
		s += "<TR>";
		
		//Last scheduled information:
		s += "<TD><B>Last schedule change:</B>&nbsp;"
			+ workorder.getsdattimelastschedulechange() 
			+ "&nbsp;<B>Changed by:</B>&nbsp;"
			+ workorder.getsschedulechangedbyfullname() + "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>"
			+ "<TD><I>NOTE: This indicates either the first time the work order was added to the schedule, or any time after that the mechanic or day was changed."
			+ "  It does NOT include changes to the job order, such as moving the job up or down in the same day.</I></TD>"
		;
		s += "</TR>";
		//Close the table:
		s += "</TABLE style = \" title:EditInformationTable; \">\n";

		return s;
	}

	private String createCommandsTable(
			SMWorkOrderHeader wo_order, 
			String sUserID, 
			String sDBID,
			String sLicenseModuleLevel){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ SMWorkOrderHeader.ORDERCOMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";
			
		//SAVE button:
		s += createSaveButton();
		
		if (!wo_order.isWorkOrderPosted()){
			//DELETE button:
			s += createDeleteButton();
			
			//Assign qty remaining:
			s += createAssignQtyRemainingButton();
			
			//Assign original qty:
			s += createAssignOriginalQtyButton();
			
			s += createPostButton();
		}else{
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMUnpostWorkOrders, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModuleLevel)){
				s += createUnPostButton();
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
	private String createDeleteButton(){
		return "<button type=\"button\""
				+ " value=\"" + DELETE_BUTTON_LABEL + "\""
				+ " name=\"" + DELETE_BUTTON_LABEL + "\""
				+ " onClick=\"deleteworkorder();\">"
				+ DELETE_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createAssignQtyRemainingButton(){
		return "<button type=\"button\""
				+ " value=\"" + ASSIGNREMAININGQTY_BUTTON_LABEL + "\""
				+ " name=\"" + ASSIGNREMAININGQTY_BUTTON_LABEL + "\""
				+ " onClick=\"assignremainingqty();\">"
				+ ASSIGNREMAININGQTY_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createAssignOriginalQtyButton(){
		return "<button type=\"button\""
				+ " value=\"" + ASSIGNTOTALQTY_BUTTON_LABEL + "\""
				+ " name=\"" + ASSIGNTOTALQTY_BUTTON_LABEL + "\""
				+ " onClick=\"assigntotalqty();\">"
				+ ASSIGNTOTALQTY_BUTTON_LABEL
				+ "</button>\n"
				;
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
	private String createUnPostButton(){
		return "<button type=\"button\""
				+ " value=\"" + UNPOST_BUTTON_LABEL + "\""
				+ " name=\"" + UNPOST_BUTTON_LABEL + "\""
				+ " onClick=\"unpostworkorder();\">"
				+ UNPOST_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createItemsTable(
			SMMasterEditEntry sm, 
			SMWorkOrderHeader workorder,
			SMOrderHeader order,
			String sLicenseModuleLevel) throws Exception{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:ItemsTable; background-color: "
				+ SMWorkOrderHeader.ITEMS_TABLE_BG_COLOR + "; \" >\n";

		//Headings:
		s += "<TR>";
		if (!workorder.isWorkOrderPosted()){
			s += "<TD class=\" fieldcenterheading \" ><input type=\"checkbox\" name=\"master\" onClick=\"checkAll();\"></TD>";
		}
		s += "<TD class=\" fieldrightheading \">Line #:&nbsp;</TD>";
		if (workorder.isWorkOrderPosted()){
			s += "<TD class=\" fieldrightheading \">Qty used:&nbsp;</TD>";
		}
		s += "<TD class=\" fieldrightheading \">Qty assigned:&nbsp;</TD>"
			+ "<TD class=\" fieldrightheading \">Qty remaining<BR>on order:&nbsp;</TD>"
			+ "<TD class=\" fieldrightheading \">Qty already<BR>shipped:&nbsp;</TD>"
			+ "<TD class=\" fieldleftheading \">Item #:&nbsp;</TD>"
			+ "<TD class=\" fieldleftheading \">Description:&nbsp;</TD>"
			+ "<TD class=\" fieldleftheading \">UOM:&nbsp;</TD>";
		s += "</TR>"
		;

		boolean bAllowItemViewing =
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation, 
				sm.getUserID(),
				getServletContext(),
				sm.getsDBID(),
				sLicenseModuleLevel);
		//Display each of the items on the order:
		int iNumberOfItemLines = 0;
		for (int i = 0; i < order.get_iOrderDetailCount(); i++){
			iNumberOfItemLines++; //This will be one based, rather than zero based
			
			//Get the corresponding work order line, if there is one:
			SMWorkOrderDetail line = workorder.getDetailByOrderDetailNumber(order.getM_arrOrderDetails().get(i).getM_iDetailNumber());
			if (line == null){
				line = new SMWorkOrderDetail();
				line.setsdetailtype(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM));
				line.setsitemdesc(order.getM_arrOrderDetails().get(i).getM_sItemDesc());
				line.setsitemnumber(order.getM_arrOrderDetails().get(i).getM_sItemNumber());
				line.setsorderdetailnumber(order.getM_arrOrderDetails().get(i).getM_iDetailNumber());
				line.setsuom(order.getM_arrOrderDetails().get(i).getM_sOrderUnitOfMeasure());
				line.setslocationcode(order.getM_arrOrderDetails().get(i).getM_sLocationCode());

			}
			s += buildItemLine(
				order.getM_arrOrderDetails().get(i),
				line,
				order,
				false,
				bAllowItemViewing,
				iNumberOfItemLines,
				sm.getsDBID(),
				sm.getUserID(),
				sm.getFullUserName(),
				workorder.isWorkOrderPosted()
			);
		}
		
		//Store all the item lines and work performed lines that are on the work order but NOT on the order:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			//If there's an order detail on the line:
			if (workorder.getDetailByIndex(i).getsorderdetailnumber().compareToIgnoreCase("-1") == 0){
				//Then add this line to the form in hidden variables:
				iNumberOfItemLines++;
				s += buildItemLine(
					null,
					workorder.getDetailByIndex(i),
					order,
					true,
					bAllowItemViewing,
					iNumberOfItemLines,
					sm.getsDBID(),
					sm.getUserID(),
					sm.getFullUserName(),
					workorder.isWorkOrderPosted()
				);
			}
		}
		
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + NUMBER_OF_ITEM_LINES_USED + "\" VALUE=\"" + Integer.toString(iNumberOfItemLines) + "\"" + ">";
		//Close the table:
		s += "</TABLE style = \" title:ItemsTable; \">\n";
		return s;
	}

	private String buildItemLine(
		SMOrderDetail orderdetail,
		SMWorkOrderDetail wodetail,
		SMOrderHeader order,
		boolean bHideLine,
		boolean bShowItemInformationLink,
		int iLineNumber,
		String sDBID,
		String sUserID,
		String sUserFullName,
		boolean bWorkOrderIsPosted
		) throws Exception{
		String s = "";
		s += "<TR>";
		int iColumnCount = 0;

		if (!bHideLine){
			//Don't show the checkboxes if the work order is posted:
			if (!bWorkOrderIsPosted){
				//Put a checkbox next to the line:
				String sCheckboxFieldname = CHECKBOX_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Parambdqtyassigned;
				s += "<TD ALIGN=CENTER style = \"vertical-align:top;\" ><INPUT TYPE=CHECKBOX";
				//if (req.getParameter(sCheckboxFieldname) != null){
				//	sTemp += SMUtilities.CHECKBOX_CHECKED_STRING;
				//}
				s += " NAME=\"" + sCheckboxFieldname + "\""
					+ " ID = \"" + sCheckboxFieldname + "\""
					+ " width=0.25>"
					+ "</TD>"
				;
				iColumnCount++;
			}
			//Order line number:
			s += "<TD class=\"readonlyrightfield\">" 
				+ orderdetail.getM_iLineNumber() + "</TD>";
			iColumnCount++;
			
			if(bWorkOrderIsPosted){
				//Qty used:
				s += "<TD class=\"readonlyrightfield\">" 
					+ wodetail.getsbdquantity() + "</TD>";
				iColumnCount++;
			}
			
			//Qty assigned:
			//Don't allow this to be edited, if it's a posted work order:
			if(bWorkOrderIsPosted){
				//Qty used:
				s += "<TD class=\"readonlyrightfield\">" 
					+ wodetail.getsbdqtyassigned() + "</TD>";
			}else{
				s += "<TD class=\" fieldcontrolright \" >"
				+ "<INPUT TYPE=TEXT NAME=\"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Parambdqtyassigned + "\""
				+ " id = \"" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
					+  SMWorkOrderDetail.Parambdqtyassigned + "\""
				+ "\""
				//+ " VALUE=\"" + workorder.get_ssignedbyname().replace("\"", "&quot;") + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsbdqtyassigned().replace(",","")) + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + ITEM_QTY_FIELD_WIDTH
				+ " MAXLENGTH=13"
				+ ">"
				+ "</TD>"
				+ "\n"
				;
			}
			iColumnCount++;
			//Qty on order:
			s += "<TD class=\"readonlyrightfield\">" 
				+ orderdetail.getM_dQtyOrdered() + "</TD>";
			iColumnCount++;
			
			//Qty shipped to date:
			s += "<TD class=\"readonlyrightfield\">" 
				+ orderdetail.getM_dQtyShippedToDate() + "</TD>";
			iColumnCount++;
			//Item #:
			String sItemNumberLink = orderdetail.getM_sItemNumber();
			if (bShowItemInformationLink){
				sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICDisplayItemInformation?ItemNumber=" 
				+ orderdetail.getM_sItemNumber()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">" + orderdetail.getM_sItemNumber() + "</A>";
			}
			
			s += "<TD class=\"readonlyleftfield\">" 
				+ sItemNumberLink + "</TD>";
			iColumnCount++;
			
			//Item Desc:
			s += "<TD class=\"readonlyleftfield\">" 
				+ orderdetail.getM_sItemDesc() + "</TD>";
			iColumnCount++;
			
			//UOM:
			s += "<TD class=\"readonlyleftfield\">" 
				+ orderdetail.getM_sOrderUnitOfMeasure() + "</TD>";
			iColumnCount++;
			
			s += "</TR>";
			
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
					+ SMTableicitems.sItemNumber + " = '" + orderdetail.getM_sItemNumber() + "'"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL,
					getServletContext(),
					sDBID, 
					"MySQL",
					SMUtilities.getFullClassName(this.toString()) + ".buildItemLine - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
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
				throw new Exception("Error reading work order comments for item - " + e.getMessage());
			}
		}else{
			//Just hide the qty assigned:
			//Qty assigned:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdqtyassigned + "\""
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMWorkOrderDetail.Parambdqtyassigned + "\""
			+ "\""
			+ " VALUE=\"" + wodetail.getsbdqtyassigned().replace(",", "") + "\""
			+ ">"
			+ "\n"
			;
		}
		
		//Store the hidden fields:
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
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramidetailtype + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramidetailtype + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsdetailtype() + "\""
		+ ">"
		+ "\n"
		;
		
		//Qty:
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdquantity + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdquantity + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsbdquantity().replace(",", "") + "\""
		+ ">"
		+ "\n"
		;
		
		//Unit price
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdunitprice + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdunitprice + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsbdunitprice().replace(",", "") + "\""
		+ ">"
		+ "\n"
		;
		
		//ID
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlid + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Parambdunitprice + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getslid() + "\""
		+ ">"
		+ "\n"
		;
		
		//Line number
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramllinenumber + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramllinenumber + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getslinenumber() + "\""
		+ ">"
		+ "\n"
		;

		//Order detail number
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlorderdetailnumber + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsorderdetailnumber() + "\""
		+ ">"
		+ "\n"
		;
		
		//Work order ID
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkorderid + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkorderid + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsworkorderid() + "\""
		+ ">"
		+ "\n"
		;
		
		//Work order performed line number
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramlworkperformedlinenumber + "\""
		+ "\""
		+ " VALUE=\"" + wodetail.getsworkperformedlinenumber() + "\""
		+ ">"
		+ "\n"
		;
		
		//Item description
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsitemdesc + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsitemdesc + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemdesc()) + "\""
		+ ">"
		+ "\n"
		;
		
		//Item number
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsitemnumber + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsitemnumber + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsitemnumber()) + "\""
		+ ">"
		+ "\n"
		;
		
		//Unit of measure
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsunitofmeasure + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsunitofmeasure + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsuom()) + "\""
		+ ">"
		+ "\n"
		;
		
		//Work performed
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformed + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformed + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsworkperformed()) + "\""
		+ ">"
		+ "\n"
		;
		
		//Work performed code
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformedcode + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramsworkperformedcode + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getsworkperformedcode()) + "\""
		+ ">"
		+ "\n"
		;	
		
		//Location
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramslocationcode + "\""
		+ " id = \"" 
			+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
			+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
			+  SMWorkOrderDetail.Paramslocationcode + "\""
		+ "\""
		+ " VALUE=\"" + clsStringFunctions.filter(wodetail.getslocationcode()) + "\""
		+ ">"
		+ "\n"
		;
		
		//If it's not a hidden line, store values we can use to set the assigned qtys automatically:
		if (!bHideLine){
			//Store the qty on order, and the qty originally ordered so we can access them from javascript:
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMOrderDetail.ParamdQtyShippedToDate + "\""
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMOrderDetail.ParamdQtyShippedToDate + "\""
			+ "\""
			+ " VALUE=\"" + clsStringFunctions.filter(orderdetail.getM_dQtyShippedToDate()) + "\""
			+ ">"
			+ "\n"
			;	
	
			s += " <INPUT TYPE=HIDDEN NAME=\"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMOrderDetail.ParamdQtyOrdered + "\""
			+ " id = \"" 
				+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineNumber), "0", OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) 
				+  SMOrderDetail.ParamdQtyOrdered + "\""
			+ "\""
			+ " VALUE=\"" + clsStringFunctions.filter(orderdetail.getM_dQtyOrdered()) + "\""
			+ ">"
			+ "\n"
			;	
		}
		return s;
	}
	private String createInstructionsTable(SMWorkOrderHeader wo, SMOrderHeader order) throws Exception{
		String s = "";
		
		s += "<TR><TD>";
		s += "<TABLE class = \" innermost \" style=\" title:InstructionsTable; background-color: "
			+ SMWorkOrderHeader.INSTRUCTIONS_TABLE_BG_COLOR + "; \" width=100% >\n";
		
		s += "<TR><TD><U><B>Instructions:</B></U>&nbsp;";
		if (wo.isWorkOrderPosted()){
			s += wo.getminstructions().replace("\n", "<BR>");
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramminstructions + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramminstructions + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(wo.getminstructions()) + "\">" + "\n";
			s += "</TD></TR>";
		}else{
			s += "<BR><TEXTAREA NAME=\"" + SMWorkOrderHeader.Paramminstructions + "\""
				+ " rows=\"" + "5" + "\""
				//+ " cols=\"" + Integer.toString(iCols) + "\""
				+ "style=\"width:100%\""
				+ " id = \"" + SMWorkOrderHeader.Paramminstructions + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ wo.getminstructions().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				;
		}
		s += "</TD></TR>";

		//Work order notes:
		s += "<TR><TD><U><B>Work order notes (from order header):</B></U>&nbsp;";
		s += order.getM_sTicketComments().replace("\n", "<BR>");
		s += "</TD></TR>";
		
		//Directions:
		//if (order.getM_sDirections().compareToIgnoreCase("") != 0){
			s += "<TR><TD><U><B>Directions (from order header):</B></U>&nbsp;";
			s += order.getM_sDirections().replace("\n", "<BR>");
			s += "</TD></TR>";
		//}

		//Close the table:
		s += "</TABLE style = \" title:InstructionsTable; \">\n";
		s += "</TD></TR>";
		return s;
	}
	private String createScheduleFieldsTable(SMWorkOrderHeader wo, SMMasterEditEntry sm) throws Exception{
		String s = "";
		ArrayList<String> sValues = new ArrayList<String>();
		ArrayList<String> sDescriptions = new ArrayList<String>();

		s += "<TR>\n<TD>\n";
		s += "<TABLE class = \" innermost \" style=\" title:ScheduleFieldsTable; background-color: "
			+ SMWorkOrderHeader.SCHEDULEFIELDS_TABLE_BG_COLOR + "; \" width=100% >\n\n";

		s += "<TR>\n<TD>";

		//Order number:
		s += "<B>Order #<FONT COLOR=\"RED\">*</FONT>:</B>&nbsp;";
		if (wo.isOrderNew()){
			s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\""
					+ " VALUE=\"" + wo.getstrimmedordernumber().replace("\"", "&quot;") + "\""
					+ " id = \"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\""
					+ " onchange=\"flagDirty();\""
					+ " SIZE=10"
					+ " MAXLENGTH=" + Integer.toString(SMTableworkorders.strimmedordernumberLength)
					+ " STYLE=\"height: 0.25in\""
					+ ">"
				;
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + ORDERFINDERBUTTON + "'" 
					+ " VALUE='" + ORDERFINDERLABEL + "'" 
					+ " STYLE='height: 0.24in'>"
				;
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + EXTENDEDORDERFINDERBUTTON + "'" 
					+ " VALUE='" + EXTENDEDORDERFINDERLABEL + "'" 
					+ " STYLE='height: 0.24in'>"
					+ "\n"
				;

		}else{
			String sOrderNumber = wo.getstrimmedordernumber();
			if (wo.getstrimmedordernumber().compareToIgnoreCase("") != 0){
				if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMViewOrderInformation, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
					sOrderNumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMDisplayOrderInformation"
						+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sOrderNumber + "</A>"
					;
				}
			}else{
				sOrderNumber = SMWorkOrderHeader.NO_ORDER_NUMBER_MARKER;
			}

			s += sOrderNumber
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramstrimmedordernumber + "\""
				+ " VALUE=\"" + wo.getstrimmedordernumber() + "\">" + "\n";
		}
		
		//Scheduled date:
		//Date; //Always stored as MM/dd/yyyy
		s += "&nbsp;&nbsp;<B>Date<FONT COLOR=\"RED\">*</FONT>:</B>&nbsp;";
		if (wo.isWorkOrderPosted()){
			s += wo.getsscheduleddate()
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramscheduleddate + "\""
					+ " ID=\"" + SMWorkOrderHeader.Paramscheduleddate + "\""
					+ " VALUE=\"" + wo.getsscheduleddate().replace("\"", "&quot;") + "\">" + "\n";
		}else{
			s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramscheduleddate + "\""
				+ " VALUE=\"" + wo.getsscheduleddate().replace("\"", "&quot;") + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramscheduleddate + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=10"
				+ " MAXLENGTH=" + "10"
				+ " STYLE=\"height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(SMWorkOrderHeader.Paramscheduleddate, getServletContext()) + "\n"
			;
		}
		
		//SMTableworkorders.ijoborder
		//Job order on the schedule
		s += "&nbsp;&nbsp;<B>Job order<B><FONT COLOR=\"RED\">*</FONT>:</B>&nbsp;";
		if (wo.isWorkOrderPosted()){
			s += wo.getsjoborder()
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramijoborder + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramijoborder + "\""
				+ " VALUE=\"" + wo.getsjoborder().replace("\"", "&quot;") + "\">" + "\n";
		}else{
			sValues.clear();
			sDescriptions.clear();
			sValues.add("-1");
			sDescriptions.add("-- Select a job order --");
			for (int i = 1; i < MAXIMUM_JOB_SEQUENCE_NUMBER; i++){
				sValues.add(Integer.toString(i));
				sDescriptions.add(Integer.toString(i));
			}

			s += "<SELECT NAME = \"" + SMWorkOrderHeader.Paramijoborder + "\""
				+ " id = \"" + SMWorkOrderHeader.Paramijoborder + "\""
				+ " onchange=\"flagDirty();\""		
				+ ">";
			for (int i = 0; i < sValues.size(); i++){
				s += "<OPTION";
				if (sValues.get(i).toString().compareTo(wo.getsjoborder().replace("\"", "&quot;")) == 0){
					s += " selected=yes";
				}
				s += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
			}
			s += "</SELECT>\n";
		}

		//SMTableworkorders.imechid
		//Load the mechanics and job types:
		s += "&nbsp;&nbsp;<B>Technician<FONT COLOR=\"RED\">*</FONT>:</B>&nbsp;";
		if (wo.isWorkOrderPosted()){
			s += wo.getmechanicsname()
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramimechid + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramimechid + "\""
				+ " VALUE=\"" + wo.getmechid().replace("\"", "&quot;") + "\">" + "\n"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramsmechanicinitials + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramsmechanicinitials + "\""
				+ " VALUE=\"" + wo.getmechanicsinitials().replace("\"", "&quot;") + "\">" + "\n"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramsmechanicname + "\""
				+ " ID=\"" + SMWorkOrderHeader.Paramsmechanicname + "\""
				+ " VALUE=\"" + wo.getmechanicsname().replace("\"", "&quot;") + "\">" + "\n"
				;
		}else{
			try{
				String sSQL = "SELECT * FROM " + SMTablemechanics.TableName			
				+ " ORDER BY " + SMTablemechanics.sMechFullName 
				;
				ResultSet rsMechanics = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sm.getsDBID(),
					"MySQL",
					this.toString() + "createScheduleFieldsTable - User: " 
					+ sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
						);

				sValues.clear();
				sDescriptions.clear();
				ArrayList<String> sUnassignedValues = new ArrayList<String>();
				ArrayList<String> sUnassignedDescriptions = new ArrayList<String>();
				//First, add a blank to make sure the user selects one:
				sValues.add("");
				sDescriptions.add("-- Select a technician --");
				boolean bMechExist = false;

				while (rsMechanics.next()){
					if (rsMechanics.getString(SMTablemechanics.lid).trim().compareTo(wo.getmechid()) == 0){
						bMechExist = true;
					}
					if((rsMechanics.getString(SMTablemechanics.sMechLocation).compareToIgnoreCase("0") == 0) || 
							(rsMechanics.getString(SMTablemechanics.sMechLocation).compareToIgnoreCase("") == 0) || 
							(rsMechanics.getString(SMTablemechanics.sMechLocation).compareToIgnoreCase("null") == 0) || 
							(rsMechanics.getString(SMTablemechanics.sMechLocation) == null) ){
						sUnassignedValues.add(Long.toString(rsMechanics.getLong(SMTablemechanics.lid)).trim());
						sUnassignedDescriptions.add((String) (rsMechanics.getString(SMTablemechanics.sMechInitial).trim() 
								+ " - " + rsMechanics.getString(SMTablemechanics.sMechFullName).trim() + " (Unassigned)"));					
					}else{
						sValues.add(Long.toString(rsMechanics.getLong(SMTablemechanics.lid)).trim());
						sDescriptions.add((String) (rsMechanics.getString(SMTablemechanics.sMechInitial).trim() 
								+ " - " + rsMechanics.getString(SMTablemechanics.sMechFullName).trim()));
					}

				}
				rsMechanics.close();
				//Append the inactive mechanics to the end of the active mechanics
				for (int i = 0; i < sUnassignedValues.size(); i++){
					sValues.add(sUnassignedValues.get(i));
					sDescriptions.add(sUnassignedDescriptions.get(i));
				}
				
				//If the mechanic doesn't exist, and IF it's not just a blank (mechid = "0"), then set the mechanic to 'removed':
				if (!bMechExist && wo.getmechid().compareToIgnoreCase("0") != 0){
					sValues.add(wo.getmechid());
					sDescriptions.add(wo.getmechanicsinitials() 
						+ " - " + wo.getmechanicsname()
						+ " (REMOVED)");
					s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Parambremovedmech + "\" VALUE=\"yes\">";
					s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramsmechanicname + "\" VALUE=\"" + wo.getmechanicsname() + "\">";
					s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.Paramsmechanicinitials + "\" VALUE=\"" + wo.getmechanicsinitials() + "\">";
				}
			} catch(Exception e){
				throw new Exception("Error [1426793956] reading mechanics list - " + e.getMessage());
			}
			s += "<SELECT NAME = \"" + SMWorkOrderHeader.Paramimechid + "\""
					+ " id = \"" + SMWorkOrderHeader.Paramimechid + "\""
					+ " onchange=\"mechanicChange(this);\""		
					+ ">";
			for (int i = 0; i < sValues.size(); i++){
				s += "<OPTION";
				if (sValues.get(i).toString().compareTo(wo.getmechid().replace("\"", "&quot;")) == 0){
					s += " selected=yes";
				}
				s += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
			}
			s += "</SELECT>";
		}
		
		s += "\n";
		s += "</TR>";
		
		//Next row:
		s += "<TR>\n";
		s += "<TD >\n";
		
		//SMTableworkorders.sassistant
		s += "<B>Helper:</B>&nbsp;";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramsassistant + "\""
			+ " VALUE=\"" + wo.getsassistant().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMWorkOrderHeader.Paramsassistant + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + Integer.toString(SMTableworkorders.sassistantLength)
			+ " STYLE=\"height: 0.25in\""
			+ ">"
			;
		
		//SMTableworkorders.sstartingtime
		s += "&nbsp;&nbsp;<B>Starting time:</B>&nbsp;";
			s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramsstartingtime + "\""
			+ " VALUE=\"" + wo.getsstartingtime().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMWorkOrderHeader.Paramsstartingtime + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=10"
			+ " MAXLENGTH=" + Integer.toString(SMTableworkorders.sstartingtimeLength)
			+ " STYLE=\"height: 0.25in\""
			+ ">"
			;
		
		//SMTableworkorders.bdqtyofhours
		s += "&nbsp;&nbsp;<B>Hours:</B>&nbsp;";	
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramqtyofhours + "\""
			+ " VALUE=\"" + wo.getsqtyofhours().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMWorkOrderHeader.Paramqtyofhours + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=8"
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"height: 0.25in\""
			+ ">"
		;

		//SMTableworkorders.bdbackchargehours
		s += "&nbsp;&nbsp;<B>Backcharge hours:</B>&nbsp;";	
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Parambackchargehours + "\""
			+ " VALUE=\"" + wo.getsbackchargehours().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMWorkOrderHeader.Parambackchargehours + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=8"
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"height: 0.25in\""
			+ ">"
		;
		
		//SMTableworkorders.bdtravelhours
		s += "&nbsp;&nbsp;<B>Travel hours:</B>&nbsp;";	
		s += "<INPUT TYPE=TEXT NAME=\"" + SMWorkOrderHeader.Paramtravelhours + "\""
			+ " VALUE=\"" + wo.getstravelhours().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMWorkOrderHeader.Paramtravelhours + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=8"
			+ " MAXLENGTH=" + "10"
			+ " STYLE=\"height: 0.25in\""
			+ ">"
		;
		
		s += "</TD>\n";
		s += "</TR>\n";
		//Next row:
		s += "<TR>\n";
		s += "<TD style = \" text-align:center; \" >\n";
		
		//SMTableworkorders.mworkdescription
		s += "<div style = \" text-align:left; \"><U><B>Work Description:</B></U></div>"
				+ "<TEXTAREA NAME=\"" + SMWorkOrderHeader.Paramworkdescription + "\""
				+ " rows=12 style = \" width:100%; \""
				+ " id = \"" + SMWorkOrderHeader.Paramworkdescription + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ wo.getsworkdescription().replace("\"", "&quot;")
				+ "</TEXTAREA>"
		;
		s += "</TD>\n";
		s += "</TR>\n";
		
		s += "<TR>\n";
		s += "<TD style = \" text-align:center; \" >\n";
		
		//SMTableworkorders.sschedulecomment
		s += "<div style = \" text-align:left; \"><U><B>Schedule comment:</B></U></div>"
			+ "<TEXTAREA NAME=\"" + SMWorkOrderHeader.Paramsschedulecomment + "\""
			+ " rows=12 style = \" width:100%; \""
			+ " id = \"" + SMWorkOrderHeader.Paramsschedulecomment + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ wo.getsschedulecomment().replace("\"", "&quot;")
			+ "</TEXTAREA>"
		;
		s += "</TD>\n";
		s += "</TR>\n";

		//Close the table:
		s += "</TABLE style = \" title:ScheduleFieldsTable; \">\n";
		s += "</TD></TR>";
		return s;	
	}
/*	private String createManagerNotesTable(SMWorkOrderHeader wo, SMOrderHeader order) throws Exception{
		String s = "";
		
		s += "<TR><TD>";
		s += "<TABLE class = \" innermost \" style=\" title:ManagerNotesTable; background-color: "
			+ SMWorkOrderHeader.MANAGERNOTES_TABLE_BG_COLOR + "; \" width=100% >\n";
		
		s += "<TR><TD><U><B>Manager Notes:</B></U>";
		
		s += "<BR><TEXTAREA NAME=\"" + SMWorkOrderHeader.Parammmanagernotes + "\""
				+ " rows=\"" + "5" + "\""
				//+ " cols=\"" + Integer.toString(iCols) + "\""
				+ "style=\"width:100%\""
				+ " id = \"" + SMWorkOrderHeader.Parammmanagernotes + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ wo.getmmanagernotes().replace("\"", "&quot;")
				+ "</TEXTAREA>"
		;
		s += "</TD></TR>";
		//Close the table:
		s += "</TABLE style = \" title:ManagerNotesTable; \">\n";
		s += "</TD></TR>";
		return s;
	}*/
	private String createWorkPerformedTable(
			SMWorkOrderHeader workorder
			) throws Exception{
		String s = "";
		int iNumberOfWPCCodes = 0;
		
		s += "\n\n";
		
		//If this is read only, just load all the WPC's from the work order:
		for (int i = 0; i < workorder.getDetailCount(); i++){
			if (workorder.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
				Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0){
				iNumberOfWPCCodes++;
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
		//Record the number of item lines in total:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMWorkOrderHeader.TOTAL_NUMBER_OF_WPC_CODES + "\" VALUE=\"" + Integer.toString(iNumberOfWPCCodes) + "\"" + ">" + "\n";
		//Close the table:
		s += "</TABLE style = \" title:WorkPerformedTable; \">\n";
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

		boolean bUnpostWorkOrderPermission = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMUnpostWorkOrders, 
			smmaster.getUserID(), 
			getServletContext(), 
			smmaster.getsDBID(),
			(String) smmaster.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
		;
		
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        deleteworkorder();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";

		s += "    shortcut.add(\"Alt+r\",function() {\n";
		s += "        assignremainingqty();\n";
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

		s += "    shortcut.add(\"Alt+t\",function() {\n";
		s += "        assigntotalqty();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		;
		if (bUnpostWorkOrderPermission && (workorder.isWorkOrderPosted())){
			s += "    shortcut.add(\"Alt+u\",function() {\n";
			s += "        unpostworkorder();\n";
			s += "    },{\n";
			s += "        'type':'keydown',\n";
			s += "        'propagate':false,\n";
			s += "        'target':document\n";
			s += "    });\n";
		}
		
		if (!workorder.isWorkOrderPosted()){
			s += "    shortcut.add(\"Alt+w\",function() {\n";
			s += "        postworkorder();\n";
			s += "    },{\n";
			s += "        'type':'keydown',\n";
			s += "        'propagate':false,\n";
			s += "        'target':document\n";
			s += "    });\n";
		}
		s += "}\n";
		s += "\n";

		s += "window.onload = function() {\n"
			+ "\n"
			+ "    initShortcuts();\n"
			+ "}\n"
		;
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this work order?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		//Save
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + SAVECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		
		//Delete
		s += "function deleteworkorder(){\n"
				+ "    if (document.getElementById(\"" + SMWorkOrderHeader.Paramiposted + "\").value =='1'){\n"
				+ "        alert('Cannot delete a posted work order.');\n"
				+ "        return;\n"
				+ "    }\n"  
				+ "    if (confirm(\"Are you sure you want to delete this work order?\")){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETECOMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "    }\n"
			+ "}\n"
		;
		
		//Assign remaining qty
		s += "function assignremainingqty(){\n"
				+ "    if (document.getElementById(\"" + SMWorkOrderHeader.Paramiposted + "\").value =='1'){\n"
				+ "        alert('Cannot change quantities on a posted work order.');\n"
				+ "        return;\n"
				+ "    }\n" 
				+ "    if (getNumberOfCheckedLines() == 0){\n"
				+ "        alert('You chose to assign the selected lines, but no lines are selected.')\n"
				+ "        return;\n"
				+ "    }\n"
				+ "		for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "			var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "			if (testName.substring(0, " + Integer.toString(CHECKBOX_MARKER.length()) + "	) == \"" + CHECKBOX_MARKER + "\"){\n"
	   			+ "				if (document.forms[\"" + FORM_NAME + "\"].elements[i].checked == true){\n"
	   							//Set the assigned qty to the qty remaining:
	   			+ "                 var CheckboxMarkerLength = " + Integer.toString(CHECKBOX_MARKER.length()) + "; \n"
	   			+ "                 var PaddedNumberLength = " + Integer.toString(OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) + "; \n"
	   			+ "                 var sLineNumber = document.forms[\"" + FORM_NAME 
	   									+ "\"].elements[i].name.substring(CheckboxMarkerLength, CheckboxMarkerLength + PaddedNumberLength"  
	   									+  "); \n"
	   			+ "                 var assignQtyFieldName = '" + SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
	   									+ "' + sLineNumber + '" + SMWorkOrderDetail.Parambdqtyassigned + "';\n"
	   			+ "                 var remainingQtyFieldName = '" + SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
	   									+ "' + sLineNumber + '" + SMOrderDetail.ParamdQtyOrdered + "';\n"
	   			+ "                 var remainingQty = document.getElementById(remainingQtyFieldName).value; \n"
				+ "                 document.getElementById(assignQtyFieldName).value = remainingQty;\n"
	   			+ "             }\n"
	   			+ "      	}\n"
	   			+ "  	}\n"
			+ "}\n"
		;
		
		//Assign original qty
		s += "function assigntotalqty(){\n"
			+ "    if (document.getElementById(\"" + SMWorkOrderHeader.Paramiposted + "\").value =='1'){\n"
			+ "        alert('Cannot change quantities on a posted work order.');\n"
			+ "        return;\n"
			+ "    }\n" 
			+ "    if (getNumberOfCheckedLines() == 0){\n"
			+ "        alert('You chose to assign the selected lines, but no lines are selected.')\n"
			+ "        return;\n"
			+ "    }\n"
			+ "		for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
   			+ "			var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
   			+ "			if (testName.substring(0, " + Integer.toString(CHECKBOX_MARKER.length()) + "	) == \"" + CHECKBOX_MARKER + "\"){\n"
   			+ "				if (document.forms[\"" + FORM_NAME + "\"].elements[i].checked == true){\n"
   							//Set the assigned qty to the qty remaining:
   			+ "                 var CheckboxMarkerLength = " + Integer.toString(CHECKBOX_MARKER.length()) + "; \n"
   			+ "                 var PaddedNumberLength = " + Integer.toString(OVERALL_LENGTH_OF_PADDED_LINE_NUMBER) + "; \n"
   			+ "                 var sLineNumber = document.forms[\"" + FORM_NAME 
   									+ "\"].elements[i].name.substring(CheckboxMarkerLength, CheckboxMarkerLength + PaddedNumberLength"  
   									+  "); \n"
   			+ "                 var assignQtyFieldName = '" + SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
   									+ "' + sLineNumber + '" + SMWorkOrderDetail.Parambdqtyassigned + "';\n"
   			+ "                 var remainingQtyFieldName = '" + SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
   									+ "' + sLineNumber + '" + SMOrderDetail.ParamdQtyOrdered + "';\n"
   			+ "                 var remainingQty = document.getElementById(remainingQtyFieldName).value; \n"
   			+ "                 var shippedQtyFieldName = '" + SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
									+ "' + sLineNumber + '" + SMOrderDetail.ParamdQtyShippedToDate + "';\n"
			+ "                 var shippedQty = document.getElementById(shippedQtyFieldName).value; \n"
			+ "                 var totalQty = parseFloat(remainingQty) + parseFloat(shippedQty); \n"
			+ "                 document.getElementById(assignQtyFieldName).value = totalQty.toFixed(" 
									+ SMTableworkorderdetails.bdqtyassignedDecimals + ");\n" 
   			+ "             }\n"
   			+ "      	}\n"
   			+ "  	}\n"
			+ "}\n"
		;
		
		//Post work order
		s += "function postworkorder(){\n"
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
		
		//UNPost work order
		if (bUnpostWorkOrderPermission){
			s += "function unpostworkorder(){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before un-posting.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    if (confirm(\"Are you sure you want to un-post this work order?\")){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + UNPOSTCOMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMWorkOrderHeader.FORM_NAME + "\"].submit();\n"
				+ "    }\n"
				+ "}\n"
			;
			}
		s+= "function checkAll(){\n"
				+ "	if (document.forms." + FORM_NAME + ".master.checked == true){\n" 
				+ "		for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "			var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "			if (testName.substring(0, " + Integer.toString(CHECKBOX_MARKER.length()) + "	) == \"" + CHECKBOX_MARKER + "\"){\n"
	   			+ "				document.forms[\"" + FORM_NAME + "\"].elements[i].checked = true;\n"
	   			+ "      	}\n"
	   			+ "  	}\n"
	   			+ "}else{\n"
				+ "		for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "			var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        	if (testName.substring(0, " + Integer.toString(CHECKBOX_MARKER.length()) + ") == \"" + CHECKBOX_MARKER + "\"){\n"
	   			+ "				document.forms[\"" + FORM_NAME + "\"].elements[i].checked = false;\n"
	   			+ "      	}\n"
	   			+ "  	}\n"
	   			+ "	}\n"
			  + "}\n";
		
		s += "function getNumberOfCheckedLines(){\n"
				+ "    var numberofcheckedlines = 0;\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
				+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
				+ "        if (testName.substring(0, " + Integer.toString(CHECKBOX_MARKER.length()) 
					+ ") == \"" + CHECKBOX_MARKER + "\"){\n"
				+ "            if (document.forms[\"" + FORM_NAME + "\"].elements[i].checked == true){\n"
				+ "                numberofcheckedlines = numberofcheckedlines + 1;\n"
				+ "            }"
				+ "        }\n"
				+ "    }\n"
				+ "    return numberofcheckedlines;\n"
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
		
		//Load the mechanic's info so it can be updated on the screen if a different mechanic is selected:
		//Here we have to build javascript arrays of the ship to locations if the customer has any:
		int iCounter = 0;
		String sstartingtimes = "";
		String sassistants = "";
		
		String SQL = "SELECT"
			+ " " + SMTablemechanics.lid
			+ ", " + SMTablemechanics.sAssistant
			+ ", " + SMTablemechanics.sstartingtime
			+ " FROM " + SMTablemechanics.TableName
			+ " ORDER BY " + SMTablemechanics.sMechInitial
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmaster.getsDBID(), 
				"MySQL", 
				this.toString() + " [1428337087] SQL: " + SQL 
			);
			while (rs.next()){
				iCounter++;
				sstartingtimes += "sstartingtimes[\"" + Long.toString(rs.getLong(SMTablemechanics.lid)) 
					+ "\"] = \"" + rs.getString(SMTablemechanics.sstartingtime).trim().replace("\"", "'") + "\";\n";
				sassistants += "sassistants[\"" + Long.toString(rs.getLong(SMTablemechanics.lid)) 
					+ "\"] = \"" + rs.getString(SMTablemechanics.sAssistant).trim().replace("\"", "'") + "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error [1428337088] reading mechanic starting time and assistant for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var sstartingtimes = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sstartingtimes + "\n";
			
			s += "var sassistants = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sassistants + "\n";
		}
		
		s += "\n";

		s += "function mechanicChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != ''){\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramsstartingtime + "\"].value = sstartingtimes[which];\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramsassistant + "\"].value = sassistants[which];\n"
		+ "    }else{\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramsstartingtime + "\"].value = '';\n"
		+ "        document.forms[\"MAINFORM\"].elements[\"" + SMWorkOrderHeader.Paramsassistant + "\"].value = '';\n"
		+ "    }"
		+ "    flagDirty();\n"
		+ "}\n\n"; 
		
		//*****************
		
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
		
		//This is the def for a left-aligned control on the screen:
		s +=
			"td.fieldcontrolleft {"
			+ "height: " + sRowHeight + "; "
			+ "text-align: left; "
			+ "}"
			+ "\n"
			;

		//This is the def for a right-aligned control on the screen:
		s +=
			"td.fieldcontrolright {"
			+ "height: " + sRowHeight + "; "
			+ "text-align: right; "
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
		
		//This is the def for a NOT underlined center-aligned heading on the screen:
		s +=
			"td.fieldcenterheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			//+ "text-decoration:underline; "
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