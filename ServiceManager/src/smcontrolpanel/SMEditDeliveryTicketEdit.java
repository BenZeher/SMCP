package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMDeliveryTicket;
import SMClasses.SMOption;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTabledeliveryticketterms;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;


public class SMEditDeliveryTicketEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String ACCEPT_SIGNATURE_COMMAND_VALUE = "ACCEPTSIGNATURE";
	public static final String ACCEPT_SIGNATURE_BUTTON_LABEL = "Accept Signature";
	public static final String POST_COMMAND_VALUE = "POSTDELIVERYTICKET";
	public static final String POST_BUTTON_LABEL = "Post Delivery Ticket";
	public static final String DELETE_BUTTON_LABEL = "Delete";
	public static final String DELETE_COMMAND_VALUE = "DELETDELIVERYTICKET";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String SAVE_BUTTON_LABEL = "Save";
	public static final String SAVE_COMMAND_VALUE = "SAVEDELIVERYTICKET";
	public static final String PRINT_BUTTON_LABEL = "Print Delivery Ticket";
	public static final String PRINT_COMMAND_VALUE = "PRINTDELIVERYTICKET";
	public static final String EMAIL_BUTTON_LABEL = "Email Delivery Ticket";
	public static final String EMAIL_COMMAND_VALUE = "EMAILDELIVERYTICKET";
	public static final String EMAIL_TO_FIELD = "EMAILTO";
	public static final String ADD_ITEMS_BUTTON_LABEL = "Add Items";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";


	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMDeliveryTicket entry = new SMDeliveryTicket(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditDeliveryTicketAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMManageDeliveryTickets
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMManageDeliveryTickets)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//Get the company name from the session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1415807186] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//if the object is in the session, use that.
	    if (currentSession.getAttribute(SMDeliveryTicket.ParamObjectName) != null){
	    	entry = (SMDeliveryTicket) currentSession.getAttribute(SMDeliveryTicket.ParamObjectName);
	    	currentSession.removeAttribute(SMDeliveryTicket.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the delivery ticket key from the request and try to load the entry:
	    }else{
	    	if ((entry.getslid()).compareToIgnoreCase("-1") != 0){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMDeliveryTicket.Paramlid + "=" + entry.getslid());
					return;
				}
	    	}
	    }
	    
	    //Load the order header 
	    SMOrderHeader orderheader = new SMOrderHeader();
		orderheader.setM_strimmedordernumber(entry.getstrimmedordernumber());
		if (entry.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			try{
				if (!orderheader.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					smedit.getPWOut().println("<BR><FONT COLOER=RED>Error [1445977549] - Could not load order header '" + entry.getstrimmedordernumber()
						+ "' - " + orderheader.getErrorMessages() + "</FONT><BR>");
					return;
				}
			}catch(Exception e){
				smedit.getPWOut().println("<BR><FONT COLOR=RED>Error [1445977549] - Could not load order header '" + entry.getstrimmedordernumber()
					+ "' - " + orderheader.getErrorMessages() + "</FONT><BR>");
			}
		}
		//*HTML Starts Here
		smedit.getPWOut().println(getHeaderString(
				"Delivery ticket edit", 
				"", 
				SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
				SMUtilities.DEFAULT_FONT_FAMILY,
				sCompanyName
				))
			;
	    
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
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to user login</A><BR>");
				
	    //Add a link to return to the original order or work order:
		if (entry.getiworkorderid().compareToIgnoreCase("0") != 0 
			&& smedit.getCallingClass().compareToIgnoreCase("smcontrolpanel.SMDisplayOrderInformation") != 0){
			smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMWorkOrderEdit"
				+ "?" + SMWorkOrderHeader.Paramlid + "=" + entry.getiworkorderid() 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to Work Order</A><BR>");
		}else{
			if(entry.getstrimmedordernumber().compareToIgnoreCase("") != 0){
			smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + entry.getstrimmedordernumber() 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to Order</A><BR>");
				;
			}
	    }	
	    try {
	    	smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
					+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n");
	    	createEditPage(getEditHTML(smedit, entry, SMDeliveryTicket.ParamObjectName, orderheader), 
	    		SMDeliveryTicket.FORM_NAME,
				smedit.getPWOut(),
				smedit
			);
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
    		smedit.redirectAction(sError, "", SMDeliveryTicket.Paramlid + "=" + entry.getslid());
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

		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
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
	}
	
	private String getEditHTML(SMMasterEditEntry sm, SMDeliveryTicket entry, String sObjectName, SMOrderHeader orderheader) throws SQLException{
		String s = "";
		s += sSignatureScripts();
		s += sCommandScripts(entry, sm);
		s += sStyleScripts();
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""+ " id=\"" + COMMAND_FLAG + "\""+ "\">";
		s += "<INPUT TYPE=HIDDEN NAME='" + SMTabledeliverytickets.smechanicname + "' VALUE='" + entry.getsmechanicname() + "'>";
		s += "<INPUT TYPE=HIDDEN NAME='" + SMTabledeliverytickets.iworkorderid + "' VALUE='" + entry.getiworkorderid() + "'>";
		s += "<INPUT TYPE=HIDDEN NAME='" + SMTabledeliverytickets.strimmedordernumber + "' VALUE='" + entry.getstrimmedordernumber() + "'>";
		 
		 boolean isInTheTopTableOfCommands; 
		 boolean bIsPosted = false;	  
		 if(entry.getiposted().compareToIgnoreCase("1") == 0){
			 bIsPosted = true;
		 }
			 
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";
		
		//Header Information table:
		s += "<TR>\n<TD>\n"; 
		s += createOrderHeaderTable( sm, getServletContext(), entry, orderheader);
		s += "</TD>\n</TR>\n\n";
		
		//Command buttons table:
		s += "<TR>\n<TD>\n";
		isInTheTopTableOfCommands = true;
		s += createCommandsTable(entry, bIsPosted, isInTheTopTableOfCommands);
		s += "</TD>\n</TR>\n\n";
				
		//Get terms:
		s += getTerms(entry, sm, bIsPosted);

		
		//Items on Order table:
		if((entry.getstrimmedordernumber()).compareToIgnoreCase("") != 0){
			if(!bIsPosted){
			s += "<TR>\n<TD>\n";
			s += createOrderDetailLines(sm, entry);
			s += "</TD>\n</TR>\n\n";
			}
		}	
		
		//Items delivered table:
		s += "<TR>\n<TD>\n";
		s += createItemsDeliveredTable(entry, bIsPosted);	
		s += "</TD>\n</TR>\n\n";
		
		//Comments table:
		s += "<TR>\n<TD>\n";
		s += createCommentsTable(entry, bIsPosted);	
		s += "</TD>\n</TR>\n\n";
		
		//Signature block table:
		if(bIsPosted){
		s += "<TR>\n<TD>\n";
		s += createSignatureBlockTable(sm, entry);
		s += "</TD>\n</TR>\n\n";
		}
		
		//Delivered By: table
		s += "<TR>\n<TD>\n";
		s += createDeliveredByTable(entry, bIsPosted, sm);
		s += "</TD>\n</TR>\n\n";
		
		//Command buttons table:
		s += "<TR>\n<TD>\n";
		isInTheTopTableOfCommands = false;
		s += createCommandsTable(entry, bIsPosted, isInTheTopTableOfCommands);
		s += "</TD>\n</TR>\n\n";
		
		//End outer table here:
		s+= "</TD>\n"+ "</TR>\n\n";
		s += "</TABLE style= \"title:ParentTable;\">\n";
		return s;
	}

	private String createAcceptSignatureButton(){
		return "<button type=\"button\""
				+ " value=\"" + ACCEPT_SIGNATURE_BUTTON_LABEL + "\""
				+ " name=\"" + ACCEPT_SIGNATURE_BUTTON_LABEL + "\""
				+ " onClick=\"signature();\">"
				+ ACCEPT_SIGNATURE_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createPostButton(){
		return "<button type=\"button\""
				+ " value=\"" + POST_BUTTON_LABEL + "\""
				+ " name=\"" + POST_BUTTON_LABEL + "\""
				+ " onClick=\"post();\">"
				+ POST_BUTTON_LABEL
				+ "</button>\n";
	}

	private String createDeleteButton(){
		String s = "";
		s = "<button type=\"button\""
		+ " value=\"" + DELETE_BUTTON_LABEL + "\""
		+ " name=\"" + DELETE_BUTTON_LABEL + "\""
		+ " onClick=\"isdelete();\">"
		+ DELETE_BUTTON_LABEL
		+ "</button>\n";
		
		s += "<INPUT TYPE='CHECKBOX' NAME='" + CONFIRM_DELETE_CHECKBOX 
				+ "' VALUE='" + CONFIRM_DELETE_CHECKBOX + "' > Check to confirm before deleting";
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
	
	private String createPrintButton(){
		return "<button type=\"button\""
				+ " value=\"" + PRINT_BUTTON_LABEL + "\""
				+ " name=\"" + PRINT_BUTTON_LABEL + "\""
				+ " onClick=\"print();\">"
				+ PRINT_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createEmailButton(){
		return "<button type=\"button\""
				+ " value=\"" + EMAIL_BUTTON_LABEL + "\""
				+ " name=\"" + EMAIL_BUTTON_LABEL + "\""
				+ " onClick=\"email();\">"
				+ EMAIL_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createCommandsTable(
			SMDeliveryTicket deliveryticket,  
			boolean bDTIsPosted,
			boolean bIsIntheTopTableOfCommands){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ SMMasterStyleSheetDefinitions.BACKGROUND_BLUE + "; \" width=100% >\n";

		s += "<TR>\n<TD style = \"text-align: left; \" >";

		//ACCEPT SIGNATURE BUTTON:
		//If it's NOT posted show the 'Accept Signature' button:
		if (!bDTIsPosted && deliveryticket.getslid().compareToIgnoreCase("-1") != 0){
			s += createAcceptSignatureButton();
		}
			
		//SAVE button:
		//IF posted nothing new should be saved:
		if(!bDTIsPosted){
		s += createSaveButton();
		}
		
		//POST button:
		//If the delivery ticket is NOT posted, we need to be able to post:
		if (!bDTIsPosted && deliveryticket.getslid().compareToIgnoreCase("-1") != 0){
			s += createPostButton();
		}
		
		//DELETE button:
		//if the delivery ticket is not posted and is not new we can delete
		if(!bDTIsPosted && !bIsIntheTopTableOfCommands && deliveryticket.getslid().compareToIgnoreCase("-1") != 0){
			s += createDeleteButton();
		}
		//PRINT RECEIPT button:
		//Any time it's posted, we need to be able to print:
		if (bDTIsPosted){
			s += createPrintButton();
		}
		
		//EMAIL RECEIPT button:
		//Any time it's posted we need to be able to email:
		if (bDTIsPosted && bIsIntheTopTableOfCommands){
			s += createEmailButton();
			s +="&nbspTo:&nbsp";
			s +="<input type='TEXT' name='" + EMAIL_TO_FIELD +"' id='" + EMAIL_TO_FIELD +"' size='18' maxlength='75'>"; 
		}
		
		s += "</TABLE style=\" title:OrderCommands; \">\n";
		return s;
	}
	
	public static String createOrderHeaderTable(
			SMMasterEditEntry sm,
			ServletContext context,
			SMDeliveryTicket deliveryticket, 
			SMOrderHeader orderheader){
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable; \" width=100% >\n";	
		
		s += "<TR>\n";
		String sDeliveryTicketID = "(NEW)";
		if(deliveryticket.getslid().compareToIgnoreCase("-1") != 0){
			sDeliveryTicketID = deliveryticket.getslid();
		}
		
		//Trimmed order number link:
		String sOrderNumber = deliveryticket.getstrimmedordernumber();
		if (deliveryticket.getstrimmedordernumber().compareToIgnoreCase("") != 0){
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
			sOrderNumber = "(NONE)";
		}
		//Work order number link:
		String sWONumber = deliveryticket.getiworkorderid();
		if (deliveryticket.getiworkorderid().compareToIgnoreCase("0") != 0 ){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				sWONumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
					+ "smcontrolpanel.SMWorkOrderEdit"
					+ "?" + SMWorkOrderHeader.Paramlid + "=" + sWONumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + sWONumber + "</A>"
				;
			}
		}else{
			sWONumber = "(NONE)";
		}
		String sPosted = "N";
		if (deliveryticket.getiposted().compareToIgnoreCase("1") == 0){
				sPosted = "Y";
		}
		
		//Link to other delivery tickets:
		String sLinkToDeliveryTicketList = "";
		if (
			(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sm.getUserID(), context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
			&& (deliveryticket.getstrimmedordernumber().compareToIgnoreCase("") != 0)
		){
			sLinkToDeliveryTicketList = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + orderheader.getM_strimmedordernumber() 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
				+ "#DeliveryTicket" 
				+ "\">" + "Delivery ticket list" + "</A>"
			;
		}
		//Initiated name and date:
		String sInitiatedBy = "";
		String sInitiatedTime = "";
		if (deliveryticket.getlinitiatedbyid().compareToIgnoreCase("") != 0){
			sInitiatedBy = deliveryticket.getsinitiatedbyfullname();
		}
		if (deliveryticket.getsdatinitiated().compareToIgnoreCase("") != 0){
			sInitiatedTime = deliveryticket.getsdatinitiated();
		}
		
		s +=
			"<TD class=\" fieldlabel \">Delivery Ticket&nbsp;#:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + sDeliveryTicketID;
		s+= "<INPUT TYPE=HIDDEN NAME='" + SMDeliveryTicket.Paramlid + "' VALUE='" + deliveryticket.getslid() + "'>";
	
		s += "</TD>\n";
		
		if (sLinkToDeliveryTicketList.compareToIgnoreCase("") != 0){
			s += "<TD class=\"readonlyleftfield\">" + sLinkToDeliveryTicketList + "</TD>\n"
			;
		}			
		s+= "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>\n"
		+ "<TD class=\"readonlyleftfield\">" + sPosted + "</TD>\n";
		
		s+= "<TD class=\" fieldlabel \">Initiated by:&nbsp;</TD>\n"
		+ "<TD class=\"readonlyleftfield\">" + sInitiatedBy + "</TD>\n";
		
		s+= "<TD class=\" fieldlabel \">Initiated on:&nbsp;</TD>\n"
		+ "<TD class=\"readonlyleftfield\">" + sInitiatedTime + "</TD>\n";

		s+= "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Paramdatinitiated + "\" VALUE=\"" + deliveryticket.getsdatinitiated() + "\">"
		+ "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Paramlinitiatedbyid + "\" VALUE=\"" + deliveryticket.getlinitiatedbyid() + "\">"
		+ "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Paramsinitiatedbyfullname + "\" VALUE=\"" + deliveryticket.getsinitiatedbyfullname() + "\">"
		+ "</TD>\n"
		;
		
		s+= "<TD class=\" fieldlabel \">Order Number:&nbsp;</TD>\n"
		+ "<TD class=\"readonlyleftfield\">" + sOrderNumber + "</TD>\n";
		
		s+= "<TD class=\" fieldlabel \">WO Number:&nbsp;</TD>\n"
				+ "<TD class=\"readonlyleftfield\">" + sWONumber + "</TD>\n";

		s += "</TR>\n\n";
		s += "</TABLE title:OrderHeaderTable; \">\n";	
		
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable2; \" width=100% >\n";	
		s += "<TR>\n";
		
		s += "<TD class=\" fieldlabel \">Bill&nbsp;to:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sBillToName() + "</TD>\n";
		
		String sMapAddress = orderheader.getM_sShipToAddress1().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress2().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress3().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToAddress4().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToCity().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToState().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToZip().trim();
		sMapAddress	= sMapAddress.trim() + " " + orderheader.getM_sShipToCountry().trim();
		
		s += "<TD class=\" fieldlabel \">Ship&nbsp;to:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShipToName() 
			//+ "</TD>\n"
			
			//+ "<TD class=\"readonlyleftfield\">" 
			+ "&nbsp;" + "<A HREF=\"" + clsServletUtilities.createGoogleMapLink(sMapAddress) + "\">" + sMapAddress + "</A>"
			+ "</TD>\n"
			
			//Ship to contact:
			+ "<TD class=\" fieldlabel \">Contact:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShiptoContact() + "</TD>\n"

			//Ship to phone:
			+ "<TD class=\" fieldlabel \">Phone:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_sShiptoPhone() + "</TD>\n"
		
			//Second phone:
			+ "<TD class=\" fieldlabel \">2nd Phone:&nbsp;</TD>\n"
			+ "<TD class=\"readonlyleftfield\">" + orderheader.getM_ssecondaryshiptophone() + "</TD>\n"
			;

		//Close the table:
		s += "</TABLE style = \" title:OrderHeaderTable2; \">\n";
		return s;
	}
	
	 private String getTerms(SMDeliveryTicket entry, SMMasterEditEntry sm, boolean isPosted){
		 String s = "";
	
		 //Get terms from term table
		
		if(!isPosted){ 
		if(entry.getmterms().compareToIgnoreCase("") == 0 || entry.getmterms() == null){
			
		String SQL = "SELECT " + SMTabledeliveryticketterms.mTerms
				+ " FROM " + SMTabledeliveryticketterms.TableName ;
		try{
		ResultSet rs = clsDatabaseFunctions.openResultSet(
    			SQL, 
    			getServletContext(),
    			sm.getsDBID(), 
    			"MySQL",
    			this.toString() + " - User: " + sm.getUserID()
    			+ " - "
    			+ sm.getFullUserName()
				);
		if (rs.next()){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Parammterms + "\" VALUE=\"" + rs.getString(SMTabledeliveryticketterms.mTerms) + "\">";
		}
			rs.close();
		}catch(Exception e){
			//Exception handled here
			}
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Parammterms + "\" VALUE=\"" + entry.getmterms() + "\">";
		}
		}
		 return s;		
	 }
	 
	private String createOrderDetailLines(SMMasterEditEntry sm, SMDeliveryTicket entry){
		String s = "";
		String SQL = "";

		//Get Order Detail
		try {
	    	SQL = "SELECT * FROM " + SMTableorderdetails.TableName
    			+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
    			+ " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
    			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " 
    			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " = " 
    			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
	    		+ " WHERE ("
	    			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
	    				+ " = '" + entry.getstrimmedordernumber() + "')"
	    		+ ")"
	    		+ " ORDER BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber;
	
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(
	    			SQL, 
	    			getServletContext(),
	    			sm.getsDBID(), 
	    			"MySQL",
	    			this.toString() + " - User: " + sm.getUserID()
	    			+ " - "
	    			+ sm.getFullUserName()
	    			);
	    	//Create table heading 
	    	s+=	"<TABLE class = ' innermost ' style=' title:OrderDetailLinesTable; background-color:"+ 
	    			SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PEACH + "' width=100% >";
	    	s += "<TR>\n<TD ALIGN=LEFT><B>Order Lines:<B></TR>\n\n";
	    	s += "<TR>\n";
			s+= "<TD class=\" fieldrightheading \"><FONT SIZE=2>Quantity</FONT></TD>\n";
			s+= "<TD class=\" fieldleftheading \"><FONT SIZE=2>Qty left on order </FONT></TD>\n";
			s+= "<TD class=\" fieldleftheading \"><FONT SIZE=2>Qty already shipped </FONT></TD>\n";
			s+="<TD class=\" fieldleftheading \"><FONT SIZE=2>Item Number  </FONT></TD>\n";
			s+="<TD class=\" fieldleftheading \"><FONT SIZE=2>Item Description  </FONT></TD>\n";
			s+="<TD class=\" fieldrightheading \"><FONT SIZE=2>UOM</FONT> </TD>\n";
			s+="</TR>\n\n";
			
	    	//Create row for each item on the order
			int numberOfItems = 0;
	    	while (rs.next()){
	    		numberOfItems++;
    			s += "<TR>\n";
    			s+="<TD ALIGN=RIGHT><INPUT TYPE='text' NAME='" + SMDeliveryTicket.ITEM_LINE_QTY +  Integer.toString(numberOfItems) +"' maxlength='3' style='width: 30px;'>&nbsp;&nbsp;&nbsp;&nbsp;</TD>\n";
    			s+="<TD ALIGN=LEFT><FONT SIZE=2>" + clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderdetails.dQtyOrdered), SMTableorderdetails.dQtyOrderedScale) + "</FONT></TD>\n";
    			BigDecimal bdTotalQtyShippedToDate = new BigDecimal("0.0000");
    			bdTotalQtyShippedToDate = new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderdetails.dQtyShipped), SMTableorderdetails.dQtyShippedScale)).add(
    				new BigDecimal(clsManageBigDecimals.doubleToDecimalFormat(rs.getDouble(SMTableorderdetails.dQtyShipped), SMTableorderdetails.dQtyShippedScale)));
    			s+="<TD ALIGN=LEFT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedToDateScale, bdTotalQtyShippedToDate) + "</FONT></TD>\n";
    			s+="<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderdetails.sItemNumber) + "</FONT></TD>\n";
    			s+="<INPUT TYPE=HIDDEN NAME='" + SMDeliveryTicket.ITEM_LINE_NUMBER + Integer.toString(numberOfItems) +"' VALUE='" + rs.getString(SMTableorderdetails.sItemNumber) + "'>";
    			s+="<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderdetails.sItemDesc) + "</FONT></TD>\n";
    			s+="<INPUT TYPE=HIDDEN NAME='" + SMDeliveryTicket.ITEM_LINE_DESC + Integer.toString(numberOfItems) + "' VALUE='" + URLEncoder.encode(rs.getString(SMTableorderdetails.sItemDesc), "UTF-8") + "'>";
    			s+="<TD ALIGN=RIGHT><FONT SIZE=2>" + rs.getString(SMTableorderdetails.sOrderUnitOfMeasure) + "</FONT></TD>\n";
    			s+="</TR>\n\n";
    		}
	    	//store number of items 
	    	s += "<INPUT TYPE=HIDDEN NAME='" + SMDeliveryTicket.NUMBER_OF_ITEM_LINES_USED +"' VALUE='" + Integer.toString(numberOfItems) +"'>";
			s += "<INPUT TYPE=HIDDEN NAME='" + SMDeliveryTicket.Paramstrimmedordernumber + "' VALUE='" + entry.getstrimmedordernumber().replace("\"", "&quot;") + "'>";
	    	s += "<TR>\n<TD>\n";
	    	//Add Items button (same as save button)
	    	s += "<button type=\"button\"" + " value=\"" + SAVE_BUTTON_LABEL + "\"" 
	    		+ " name=\"" + SAVE_BUTTON_LABEL + "\"" 
	    		+ " onClick=\"save();\">"+ ADD_ITEMS_BUTTON_LABEL + "</button>\n";
	    	s += "</TD>\n</TR>\n\n";
	    	//Close Table
	    	s+=	"</TABLE style=' title:OrderDetailLinesTable; ' >";    	
	    	rs.close();
		}catch(Exception e){
			//Handle exception here
		}
		return s;
	}
	
	private String createSignatureBlockTable(
			SMMasterEditEntry sm, 
			SMDeliveryTicket deliveryticket
			) throws SQLException{
		
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:SignatureBlockTable; background-color: "
				+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PEACH + "; width=100%; \" >\n";
			
			//Signature:
			s += "<TR>\n<TD>\n";
			s += "<B>Signature:</B>&nbsp;";
			//Add signature here:
			String sSignatureBoxWidth = deliveryticket.getlsignatureboxwidth();
			//If the signature was never saved then load default signature box dimensions to sho no signature has been collected
			if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
				SMOption smoptions = new SMOption();
				try {
					smoptions.load(sm.getsDBID(), getServletContext(), sm.getUserName());
				} catch (Exception e) {
					throw new SQLException ("Error loading smoptions for signature box size.");
				}
				sSignatureBoxWidth = smoptions.getisignatureboxwidth();
				deliveryticket.setlsignatureboxwidth(sSignatureBoxWidth);
			}
			
			int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
			String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
			s +=
	        	"\n"
	        	+ "<canvas class=pad width=" + iSignatureWidth + " name=signaturecanvas"
	        		+ " height=" + sSignatureBoxHeight + " style=\"border:1px solid  #000000;\" ></canvas>\n"
	        ;	
	
			s += "&nbsp;<B>Date signed:</B>&nbsp;";
			s += deliveryticket.getsdatsigned();
			s += "</TD>\n";
			s += "</TR>\n\n";
			s += "<TR>\n<TD>\n";
			s += "<B>Printed name and title:</B>&nbsp;";
			s += deliveryticket.getssignedbyname() + "</TD>\n";
			s += "</TR>\n\n";
			
		//Close the table:
		s += "</TABLE style = \" title:SignatureBlockTable; \">\n";
		return s;

	}
	
	private String createItemsDeliveredTable(SMDeliveryTicket entry, boolean isPosted){
		String s = "";
		//Open table
		s += "<TABLE class = \" innermost \" style=\" title:DeliveryLines; background-color:"+ 
				SMMasterStyleSheetDefinitions.BACKGROUND_BLUE + "\" width=100% >\n";
		
				s+= "<TR>\n<TD width='10%' valign='top'><B>Items Delivered:</B>";
	
				if (!isPosted){
				s+= "<TEXTAREA NAME='" + SMDeliveryTicket.Paramsdetaillines 
					+ "' ID='" + SMDeliveryTicket.Paramsdetaillines + "' onchange=\"flagDirty();\" rows='6' style = 'width:100%;'>"
				    + entry.getsdetaillines() +"</TEXTAREA>";
				}else{
				s += "<TD class= 'readonlyleftfield' >"  
				+ (entry.getsdetaillines().replace("\n", "<BR>")).replace("\"", "&quote;")
				+ "</TD>\n";
				}
				s += "</TD>\n</TR>\n\n";
		
		s += "</TABLE style=\" title:DeliveryLines; \">\n";
		return s;
	}
	
	private String createCommentsTable(SMDeliveryTicket entry, boolean isPosted){
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:Comments;  background-color:"+ 
				SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PINK + "\" width=100% >\n";
				s += "<TR>\n<TD width='10%' valign='top'>";
				s += "<B>Comments:</B>";
				if(!isPosted){
				s +=  "<TEXTAREA NAME='"+ SMDeliveryTicket.Parammcomments + "'"
					+ " ID=" + SMDeliveryTicket.Parammcomments + " onchange=\"flagDirty();\" rows=\"" + "3" + "\""
					+ " style = \"width: 100%;\" >" + entry.getmcomments()
					+ "</TEXTAREA>";
				}
				else{
				s += "<TD class= 'readonlyleftfield' >"
				+ (entry.getmcomments().replace("\n", "<BR>")).replace("\"", "&quote;")
				+ "</TD>\n";
				}
				s += "</TD>\n</TR>\n\n";
				s += "</TABLE style=\" title:Comments; \">\n";
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
		+"<TR>\n<TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>\n"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>\n";
		}

		s = s + "</TR>\n\n</TABLE>";
		return s;
	}
	
	private String sSignatureScripts(){
	    //Scripts for signature:
		
		String s = "<script src=\"scripts/jquery-signaturepad-min-01.js\"></script>\n";
	    s += "<script src=\"scripts/json2.min.js\"></script>\n"
	    ;
	    return s;
	}
	
	private String createDeliveredByTable(SMDeliveryTicket entry, boolean isPosted, SMMasterEditEntry smedit){
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:DeliveredBy;  background-color:"+ 
				SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PINK + "\" width=100% >\n";
				s += "<TR>\n<TD width='10%' valign='top'>";
				s += "<B>Delivered By:</B>";
				if(!isPosted){
				s +=  "<INPUT TYPE='TEXT' NAME='"+ SMDeliveryTicket.Paramsdeliveredby + "'"
					+ " ID=" + SMDeliveryTicket.Paramsdeliveredby + " onchange=\"flagDirty();\"";
					//If its a new record default delivered by field
					if(entry.getslid().compareToIgnoreCase("-1") == 0){
						if(entry.getsmechanicname().compareToIgnoreCase("") != 0){
							s+= "VALUE='" + entry.getsmechanicname() + "'";
						}else{
							String sUsersFullName = "";
							Connection conn = clsDatabaseFunctions.getConnection(
					    			getServletContext(), 
					    			smedit.getsDBID(), 
					    			"MySQL", 
					    			this.toString() + " - user: " + smedit.getUserID() + " - " + smedit.getFullUserName()
					    			);
							String SQL = "SELECT"
						    	+ " " + SMTableusers.sUserFirstName
						    	+ ", " + SMTableusers.sUserLastName
						    	+ " FROM " + SMTableusers.TableName
						    	+ " WHERE ("
						    		+ "(" + SMTableusers.lid + " = " + smedit.getUserID() + ")"
						    	+ ")"
						    	;
						    try {
								ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
								if (rs.next()){
									sUsersFullName = rs.getString(SMTableusers.sUserFirstName).trim() 
									+ " " + rs.getString(SMTableusers.sUserLastName).trim();
								}
									rs.close();
							}catch (Exception e) {
								 clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080485]");
							}
						    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080486]");
							s+= "VALUE='" + sUsersFullName + "'";
						}
					}else{
						s += "VALUE='" + entry.getsdeliveredby() + "'";
					}			
					s += "";
				}
				else{
				s += "<TD class= 'readonlyleftfield' >"
				+ (entry.getsdeliveredby().replace("\n", "<BR>")).replace("\"", "&quote;")
				+ "</TD>\n";
				}
				s += "</TD>\n</TR>\n\n";
				s += "</TABLE style=\" title:Comments; \">\n";
		return s;
	}
	
	private String sCommandScripts(
			SMDeliveryTicket deliveryticket, 
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

			//IF this delivery ticket is editable, then the signature pad has to be editable.
			String sSignaturePadOptions = "drawOnly:true,"
				+ " output:\"." + SMDeliveryTicketSignatureEdit.SIGNATURE_JSON_OUTPUT_FIELD_NAME + "\","
				+ " errorMessageDraw: \"\","
				+ " lineTop:" + SMTabledeliverytickets.SIGNATURE_TOP + ","
				+ " penWidth:" + SMTabledeliverytickets.SIGNATURE_PEN_WIDTH + ","
				+ " penColour:" + "\"" + SMMasterStyleSheetDefinitions.BACKGROUND_DARK_BLUE + "\","
				+ " lineColour:\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"," //makes the line transparent
				+ " lineWidth:" + SMTabledeliverytickets.SIGNATURE_LINE_WIDTH + ","
				+ " lineMargin:" + SMTabledeliverytickets.SIGNATURE_LINE_MARGIN
			;

			//SIG
			//displayOnly
			sSignaturePadOptions += ", displayOnly:true";
	
			s += "window.onload = function() {\n"
			+ "\n"
			
			//Display signature:
			+ "    $(document).ready("
			+ "        function () {\n"
			+ "            $('." + SMDeliveryTicket.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "});\n"
			;
		    if (deliveryticket.getmsignature().compareToIgnoreCase("") != 0){
		    	s += "        $('." + SMDeliveryTicket.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).regenerate(" + deliveryticket.getmsignature()  + ");\n";
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
					+ SAVE_COMMAND_VALUE + "\" && document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
					+ DELETE_COMMAND_VALUE + "\"){\n"
				+ "        return 'You have unsaved changes - are you sure you want to leave this delivery ticket?';\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n\n"
			;
			
			//Delete:
			s += "function isdelete(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Post
			s += "function post(){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before posting.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + POST_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Print
			s += "function print(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + PRINT_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Email
			s += "function email(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + EMAIL_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Accept Signature
			s += "function signature(){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before accepting signature.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + ACCEPT_SIGNATURE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			

			//Flag delivery ticket dirty:
			s += "function flagDirty() {\n"
					+ "    flagRecordChanged();\n"
					+ "}\n"
				;

			s += "function flagRecordChanged() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
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
