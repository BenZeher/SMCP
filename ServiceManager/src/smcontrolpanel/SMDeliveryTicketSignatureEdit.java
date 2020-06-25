package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
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
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;



public class SMDeliveryTicketSignatureEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String REMOVE_DELIVERY_TICKET_ATTRIBUTE_FROM_SESSION = "REMOVEDTATTRIBUTE";

	//Commands:
	public static final String EDIT_BUTTON_LABEL = "<B><FONT COLOR=RED>E</FONT></B>dit delivery ticket"; //E
	public static final String EDITCOMMAND_VALUE = "EDIT";
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String CLEAR_SIGNATURE_BUTTON_LABEL = "<B><FONT COLOR=RED>C</FONT></B>lear signature"; //C
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";

	public static final String SIGNATURE_JSON_OUTPUT_FIELD_NAME = "outputsignature";
	//private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		
		//load delivery ticket from request
		SMDeliveryTicket entry = new SMDeliveryTicket(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMDeliveryTicketSignatureAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMManageDeliveryTickets
		);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMManageDeliveryTickets, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a delivery ticket object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(SMDeliveryTicket.ParamObjectName) != null){
	    	entry = (SMDeliveryTicket) currentSession.getAttribute(SMDeliveryTicket.ParamObjectName);
	    	currentSession.removeAttribute(SMDeliveryTicket.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the OrderNumber or key from the request and try to load the entry:
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
		
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1445280222] getting session attribute - " + e1.getMessage());
			return;
		}
	    smedit.getPWOut().println(getHeaderString(
			"Delivery ticket acceptance", 
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

	    //Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "\">Return to user login</A><BR><BR>");
	    
		try{
			//Add scripts
			smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
				+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
			);
			createEditPage(
				getEditHTML(smedit, entry, entry.getObjectName()),
				SMDeliveryTicket.FORM_NAME,
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
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");

	}
	
	private String getEditHTML(SMMasterEditEntry sm, SMDeliveryTicket entry, String sObjectName) throws Exception{
		String s = "";
		s += sCommandScripts(entry, sm);
		s += sSignatureScripts(entry);
		s += sStyleScripts();

		//Store whether or not the record has been changed this includes ANY change, including approval:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//Store key fields
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Paramlid + "\" VALUE=\"" 
				+ entry.getslid() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Paramstrimmedordernumber + "\" VALUE=\"" 
				+ entry.getstrimmedordernumber() + "\">";
		
		//New Row
		s += "<TR>";
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";			
		
		//Header information:
		s += "<TR><TD>" 
			+ createOrderHeaderTable(
				sm,
				getServletContext(),
				entry,
				SMUtilities.getFullClassName(this.toString())) 
			+ "</TD></TR>";

		//Create the order commands line at the top:
		s += "<TR><TD>" + createCommandsTable(entry) + "</TD></TR>";
	
		//Create Items Delivered table:
		s += "<TR><TD>" + createItemsTable(sm, entry) + "</TD></TR>";

		//Create the comments area table:
		s += "<TR><TD>" + createCommentsTable(sm, entry) + "</TD></TR>";

		//Create the terms table:
		s += "<TR><TD>" + createTermsTable(sm, entry) + "</TD></TR>";

		//Create the signature block table:
		s += "<TR><TD>" + createSignatureBlockTable(sm, entry) + "</TD></TR>";

		//Create the order commands line at the bottom:
		s += "<TR><TD>" + createCommandsTable(entry) + "</TD></TR>";

		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		return s;
	}
	private String createCommentsTable(
			SMMasterEditEntry sm, 
			SMDeliveryTicket deliveryticket) throws SQLException{
		String s = "";
		int iColSpan = 5;

		s += "<TABLE class = \" innermost \" style=\" title:MechanicInfoTable; background-color: "
				+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PINK + "; \" width=100% >\n";

		//Work order comments:
		s += "<TR>";
		s += "<TD width='20%'><U><B>Additional comments:</B></U></TD>";
		s += "<TD COLSPAN=" + Integer.toString(iColSpan) + ">"
				+ (deliveryticket.getmcomments().replace("\"", "&quot;")).replace("\n", "<BR>")
				+ "</TD>";
		
		//Set the date:
		s += "<TD class=\" fieldlabel \">Initial Delivery Date:&nbsp;</TD>";
		s += "<TD>" + deliveryticket.getsdatinitiated() + "</TD>";

		//Delivered by:
		  //If the ticket was initiated through a work order, use that mechanics name:
		String sDeliveredBy = "";
		if(deliveryticket.getsmechanicname().compareToIgnoreCase("") != 0  ){
			sDeliveredBy = deliveryticket.getsmechanicname();
		}else{
			sDeliveredBy = deliveryticket.getsinitiatedbyfullname();
		}
		s += "<TD class=\" fieldlabel \">Delivered by:&nbsp;</TD>";
		s += "<TD>" + sDeliveredBy + "</TD>";

		s += "</TR>";

		//Close the table:
		s += "</TABLE style = \" title:MechanicInfoTable; \">\n";
		return s;
	}

	private String createCommandsTable(
			SMDeliveryTicket deliveryticket){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ SMMasterStyleSheetDefinitions.BACKGROUND_BLUE + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";

		s += createEditButton();
		
		//SAVE button:
		//We need to be able to save no matter what:
		s += createSaveButton();
		
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

	private String createItemsTable(
			SMMasterEditEntry sm, 
			SMDeliveryTicket deliveryticket) throws Exception{
		String s = "";
		//Open Table
		s += "<TABLE class = \" innermost \" style=\" title:ItemsTable; background-color: "
				+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PEACH + "; \" >\n";
		
		//Display the items delivered:
		s += "<TR><TD width='20%'><B><U>ITEMS DELIVERED:</U></B></TD>";
		s += "<TD class=\"readonlyleftfield\">" + deliveryticket.getsdetaillines().replace("\n","<BR>") + "</TD>" + "\n";
		s += "</TR>";
		;	
		
		//Close the table:
		s += "</TABLE style = \" title:ItemsTable; \">\n";
		
		return s;
	}

	private String sSignatureScripts(SMDeliveryTicket deliveryticket){
	    //Scripts for signature:
		
		String s = "<script src=\"scripts/jquery-signaturepad-min-01.js\"></script>\n";
	    s += "<script src=\"scripts/json2.min.js\"></script>\n"
	    ;
	    return s;
	}
   
	private String createTermsTable(
			SMMasterEditEntry sm, 
			SMDeliveryTicket deliveryticket) throws Exception{
		String s = "";
		//Open Table
		s += "<TABLE class = \" innermost \" style=\" title:TermsTable; background-color: "
			+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_GREEN + "; \" width=100% >\n";
		
		s += "<TR><TD><U><B>Terms And Conditions </B></U></TD></TR>";
		s += "<TD class=\"readonlyleftfield\" >";
		
		//Select the terms
		String SQL = "SELECT " + SMTabledeliveryticketterms.mTerms + " FROM " + SMTabledeliveryticketterms.TableName
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createTermsTable - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
					);
			if (rs.next()){
				//Display terms
				s += rs.getString(SMTabledeliveryticketterms.mTerms).replace("\n", "<BR>");
				s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDeliveryTicket.Parammterms + "\" VALUE=\"" + rs.getString(SMTabledeliveryticketterms.mTerms) + "\">";
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error getting delivery ticket term code with SQL: " + SQL + " - " + e.getMessage());
		}
		s += "</TD>";
		//Close the table:
		s += "</TABLE style = \" title:TermsTable; \">\n";
		return s;
	}

	private String createSignatureBlockTable(
			SMMasterEditEntry sm, 
			SMDeliveryTicket deliveryticket) throws SQLException{
		
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:SignatureBlockTable; background-color: "
				+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PEACH + "; \" >\n";
		
		s += "<TR>";
		s += "<TD><U><B>Confirmation of delivery:</B></U></TD>";
		s += "</TR>";
		
		s += "<TR><TD>";
		s += "<B>Signature:</B>&nbsp;";
		//Add signature here:
		String sSignatureBoxWidth = deliveryticket.getlsignatureboxwidth();
		//If this signature has never been saved load width from sm options.
		if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
			SMOption smoptions = new SMOption();
			try {
				smoptions.load(sm.getsDBID(), getServletContext(), sm.getUserID());
			} catch (Exception e) {
				throw new SQLException ("Error loading smoptions for signature box size.");
			}
			sSignatureBoxWidth = smoptions.getisignatureboxwidth();
				deliveryticket.setlsignatureboxwidth(sSignatureBoxWidth);
			}
		int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
		String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
		s += " <INPUT TYPE=HIDDEN NAME=\"" 
			+ SMDeliveryTicket.Paramlsignatureboxwidth
			+ "\""
			+ " id = \"" + SMDeliveryTicket.Paramlsignatureboxwidth + "\""
			+ " VALUE=\"" + deliveryticket.getlsignatureboxwidth() + "\""
			+ ">"
			;	
		s +=
        	"\n"
        	+ "<canvas class=pad width=" + sSignatureBoxWidth + " name=signaturecanvas"
        		+ " height=" + sSignatureBoxHeight + " style=\"border:1px solid  #000000;\""
        		+ " onchange=\"flagDirty();\""
        		+ "></canvas>\n"
        	+ "<input type=hidden name='" + SMDeliveryTicket.Parammsignature + "' class=" + SIGNATURE_JSON_OUTPUT_FIELD_NAME + ">\n"
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
		if (deliveryticket.getsdatsigned().compareToIgnoreCase(clsMasterEntry.EMPTY_DATE_STRING) == 0){
			clsDBServerTime st = null;
			try {
				st = new clsDBServerTime(sm.getsDBID(), sm.getUserName(), getServletContext());
				deliveryticket.setsdatsigned(st.getCurrentDateTimeInSelectedFormat("M/d/yyyy"));
			} catch (Exception e) {
				//Nothing to do here - just accept the empty date and move on
			}
		}
		s += "<INPUT TYPE=TEXT NAME=\"" + SMDeliveryTicket.Paramdatsigneddate + "\""
			+ " VALUE=\"" + deliveryticket.getsdatsigned().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMDeliveryTicket.Paramdatsigneddate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "9"
			+ " MAXLENGTH=" + "10"
			+ ">"
			+ SMUtilities.getDatePickerString(SMDeliveryTicket.Paramdatsigneddate, getServletContext())
			;
		s += "</TD>";
		s += "</TR>";
		
		//Text box for signers name:
		s += "<TR><TD>";
		s += "<B>Printed name and title:</B>&nbsp;";
		s += "<INPUT TYPE=TEXT NAME=\"" + SMDeliveryTicket.Paramssignedbyname + "\""
			+ " id = \"" + SMDeliveryTicket.Paramssignedbyname + "\""
			+ " VALUE=\"" + deliveryticket.getssignedbyname().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "50"
			+ " MAXLENGTH=" + Integer.toString(SMTabledeliverytickets.ssignedbynamelength)
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
			SMDeliveryTicket deliveryticket, 
			String sClassName) throws Exception{
		String s = "";
		
		//Work order number link:
		String sWONumber = deliveryticket.getiworkorderid();
		if (deliveryticket.getiworkorderid().compareToIgnoreCase("0") != 0 ){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sm.getUserID(), 
				context, sm.getsDBID(),
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
		
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable; \" width=100% >\n";	
		
		s += "<TR>";
		//Get delivery ticket number
		String sDeliveryTicketID = deliveryticket.getslid();
		
		// Get link for Order Number
		String sOrderNumber = deliveryticket.getstrimmedordernumber();
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
		if (deliveryticket.getiposted().compareToIgnoreCase("1") == 0){
				sPosted = "Y";
		}

		//Link to other delivery tickets:
		String sLinkToDeliveryTicketList = "";
		if (
			(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sm.getUserID(), 
				context, 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
			&& (deliveryticket.getstrimmedordernumber().compareToIgnoreCase("") != 0)
		){
			sLinkToDeliveryTicketList = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + deliveryticket.getstrimmedordernumber() 
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
			"<TD class=\" fieldlabel \">Delivey ticket&nbsp;#:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sDeliveryTicketID;
	
		s += "</TD>";
		
		if (sLinkToDeliveryTicketList.compareToIgnoreCase("") != 0){
			s += "<TD class=\"readonlyleftfield\">" + sLinkToDeliveryTicketList + "</TD>"
			;
		}
			s+= "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + sPosted + "</TD>";
			
			s += "<TD class=\" fieldlabel \">Initiated&nbsp;by:&nbsp;</TD>"
					+ "<TD class=\"readonlyleftfield\">" + 	sInitiatedBy + "</TD>";
			
			s += "<TD class=\" fieldlabel \">Initiated&nbsp;on:&nbsp;</TD>"
					+ "<TD class=\"readonlyleftfield\">" + 	sInitiatedTime + "</TD>";
			
			s+= "<TD class=\" fieldlabel \">Order Number:&nbsp;</TD>"
					+ "<TD class=\"readonlyleftfield\">" + sOrderNumber + "</TD>";
			
			s+= "<TD class=\" fieldlabel \">WO Number:&nbsp;</TD>"
					+ "<TD class=\"readonlyleftfield\">" + sWONumber + "</TD>";
			
		s += "</TR>";
		s += "</TABLE title:OrderHeaderTable; \">\n";	
		
		s += "<TABLE class = \" innermost \" style=\" title:OrderHeaderTable2; \" width=100% >\n";	
		s += "<TR>";
		
		s += "<TD class=\" fieldlabel \">Bill&nbsp;to:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + deliveryticket.getsbilltoname() + "</TD>";
		
		String sMapAddress	=  deliveryticket.getsshiptoadd1().trim();
		sMapAddress	= sMapAddress.trim() + " " + deliveryticket.getsshiptoadd2().trim();
		sMapAddress	= sMapAddress.trim() + " " + deliveryticket.getsshiptoadd3().trim();
		
		s += "<TD class=\" fieldlabel \">Ship&nbsp;to:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + deliveryticket.getsshiptoname() 

			
			+ "&nbsp;" + "<A HREF=\"" + clsServletUtilities.createGoogleMapLink(sMapAddress) + "\">" + sMapAddress + "</A>"
			+ "</TD>"
			
			//Ship to contact:
			+ "<TD class=\" fieldlabel \">Contact:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + deliveryticket.getsshiptocontact() + "</TD>"
			
			//Ship to phone:
			+ "<TD class=\" fieldlabel \">Phone:&nbsp;</TD>"
			+ "<TD class=\"readonlyleftfield\">" + deliveryticket.getsshiptophone() + "</TD>";
		

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
		
		s += "}\n";
		s += "\n";

		//IF this delivery ticket is editable, then the signature pad has to be editable.
		String sSignaturePadOptions = "drawOnly:true,"
			+ " output:\"." + SIGNATURE_JSON_OUTPUT_FIELD_NAME + "\","
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
		if(deliveryticket.getmsignature().compareToIgnoreCase("") != 0 ){
			sSignaturePadOptions += ", displayOnly:true";
		}
		
		s += "window.onload = function() {\n"
		+ "\n"
		+ "    initShortcuts();\n"
		
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
			//Check to see if the date fields were changed, and if so, flag the record was changed field:
			//+ "    if (document.getElementById(\"" + PROPOSALDATE_PARAM + "\").value != " 
			//	+ "document.getElementById(\"" + SMProposal.ParamdatproposalDate + "\").value){\n"
			//+ "        flagDirty();\n"
			//+ "    }\n"			
			
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this delivery ticket?';\n"
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
			+ "        document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
			//+ "    }\n"
			+ "}\n"
		;
		//Save
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + SAVECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMDeliveryTicket.FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
						
		//Clear signature
		s += "function clearsignature(){\n"
			+ "    flagDirty();\n"
			+ "    $('." + SMDeliveryTicket.FORM_NAME + "').signaturePad({" + sSignaturePadOptions + "}).clearCanvas();\n"
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