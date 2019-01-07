package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import smar.FinderResults;
import ConnectionPool.WebContextParameters;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditOrderSelection extends HttpServlet {

	public static final String PRINTSERVICEWORKORDER_BUTTON_NAME = "PRINTSERVICEWORKORDER";
	public static final String PRINTSERVICEWORKORDER_BUTTON_LABEL = "Print 'SERVICE STYLE' work order";
	public static final String NUMBEROFSERVICEWORKORDERCOPIES = "NUMBEROFSERVICEWORKORDERCOPIES";
	public static final String PRINTINSTALLATIONWORKORDER_BUTTON_NAME = "PRINTINSTALLATIONWORKORDER";
	public static final String PRINTINSTALLATIONWORKORDER_BUTTON_LABEL = "Print 'INSTALLATION STYLE' work order";
	public static final String NUMBEROFINSTALLATIONWORKORDERCOPIES = "NUMBEROFINSTALLATIONWORKORDERCOPIES";
	public static final String ADDDELIVERYTICKET_BUTTON_NAME = "ADDDELIVERYTICKET";
	public static final String ADDDELIVERYTICKET_BUTTON_LABEL = "Add interactive delivery ticket";
	public static final String EDITCHANGEORDERS_BUTTON_NAME = "EDITCHANGEORDERS";
	public static final String EDITCHANGEORDERS_BUTTON_LABEL = "Edit change orders";
	public static final String EDITFIELDINFO_BUTTON_LABEL = "Edit field information";
	public static final String PRINTJOBFOLDERLABEL_BUTTON_NAME = "PRINTJOBFOLDERLABEL";
	public static final String PRINTJOBFOLDERLABEL_BUTTON_LABEL = "Print job folder label";
	public static final String VIEWORDER_BUTTON_NAME = "VIEWORDERLABEL";
	public static final String VIEWORDER_BUTTON_LABEL = "View order information";
	public static final String CLONEORDER_BUTTON_NAME = "CLONEORDER";
	public static final String CLONEORDER_BUTTON_LABEL = "Clone order";
	public static final String CLONE_DETAILS_CHECKBOX_NAME = "CLONEDETAILSCHECKBOX";
	public static final String EDITORDER_BUTTON_NAME = "EDITORDER";
	public static final String EDITORDER_BUTTON_LABEL = "Edit order";
	public static final String EDIT_PROPOSAL_NAME = "EDITPROPOSAL";
	public static final String EDIT_PROPOSAL_LABEL = "Edit proposal";
	public static final String CREATEORDER_BUTTON_NAME = "CREATEORDER";
	public static final String CREATEORDER_BUTTON_LABEL = "Create order";
	public static final String CREATEQUOTE_BUTTON_NAME = "CREATEQUOTE";
	public static final String CREATEQUOTE_BUTTON_LABEL = "Create quote";
	public static final String FINDCUSTOMER_BUTTON_NAME = "FINDCUSTOMER";
	public static final String FINDCUSTOMER_BUTTON_LABEL = "Find customer";
	public static final String CANCELORDER_BUTTON_NAME = "CANCELORDER";
	public static final String CANCELORDER_BUTTON_LABEL = "Cancel order";
	public static final String CONFIRM_CANCELORDER_CHECKBOX_NAME = "CONFIRMCANCELCHECKBOX";
	public static final String UNCANCELORDER_BUTTON_NAME = "UNCANCELORDER";
	public static final String UNCANCELORDER_BUTTON_LABEL = "UNCancel order";
	public static final String CONFIRM_UNCANCELORDER_CHECKBOX_NAME = "CONFIRMUNCANCELCHECKBOX";
	public static final String RETURNINGFROMFINDCUSTOMER_PARAM = "RETURNINGFROMFINDCUSTOMER";

	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID;
	private String sUserID;
	private String sUserFirstName = "";
	private String sUserLastName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMManageOrders
		)
		){
			return;
		}

		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID =  (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		
		String title = "Manage Orders";
		String subtitle = "";

		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}
		if (bMobileView){
			String sHeading = SMUtilities.DOCTYPE
			+ "<HTML>"
			+ "<HEAD>"
			+ "<TITLE>" + title + "</TITLE>"
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY>"
			//+ " BGCOLOR=\"" + "black" + "\">"
			//+ " COLOR=\"" + "white" + "\">"

			+ SMUtilities.setMobileButtonStyle()

			+ "<TABLE BORDER=0><TR><TD VALIGN=BOTTOM><H2>" + title + "</H2></TD>";
			sHeading += "</TR></TABLE>";
			out.println(sHeading);
		}else{
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		}

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMEditOrders) 
				+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ID='MAINFORM' NAME='MAINFORM' ACTION =\"" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderHandler\"  METHOD='POST'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");

		//Order number:
		out.println("<TD>" + "<B>Manage order number:</B> " 
				+ "<INPUT TYPE=TEXT ID='OrderNumber' NAME=\"" + SMOrderHeader.ParamsOrderNumber 
				+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, request) + "\""
				+ " class = \"text\""
				+ " style=\"width:100px;\"" 
				+ " MAXLENGTH = 10" 
				+ ">"
				);

		//Link to finder:
		out.println("&nbsp;" + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=Order"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.sBillToAddressLine1
				+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
				+ "&SearchField4=" + SMTableorderheaders.sShipToAddress1
				+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading3=Ship%20To%20Name"
				+ "&ResultListField4="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading4=Service%20Type"
				+ "&ResultListField5="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading5=Salesperson"
				+ "&ResultListField6="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading6=Order%20Date"
				+ "&ResultListField7="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
					+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
				+ "&" + FinderResults.RESULT_FIELD_ALIAS + "7=CANCELEDDATE"
				+ "&ResultHeading7=Canceled"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find order</A>"

				//Add EXTENDED order find:
				+ "&nbsp;&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + FinderResults.OBJECT_ORDER_EXTENDED
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.sCustomerCode
				+ "&SearchFieldAlias3=Customer%20Acct."
				+ "&SearchField4=" + FinderResults.COMPLETE_BILL_TO_ADDRESS
				+ "&SearchFieldAlias4=Complete%20Bill%20To%20Address"
				+ "&SearchField5=" + FinderResults.COMPLETE_SHIP_TO_ADDRESS
				+ "&SearchFieldAlias5=Complete%20Ship%20To%20Address"
				+ "&SearchField6=" + SMTableorderheaders.mTicketComments
				+ "&SearchFieldAlias6=Ticket%20Comments"
				+ "&SearchField7=" + SMTableorderheaders.sBillToContact
				+ "&SearchFieldAlias7=Bill%20To%20Contact"
				+ "&SearchField8=" + SMTableorderheaders.sBillToPhone
				+ "&SearchFieldAlias8=Bill%20To%20Phone"
				+ "&SearchField9=" + SMTableorderheaders.sShipToContact
				+ "&SearchFieldAlias9=Ship%20To%20Contact"
				+ "&SearchField10=" + SMTableorderheaders.sShipToPhone
				+ "&SearchFieldAlias10=Ship%20To%20Phone"
				+ "&SearchField11=" + SMTableorderheaders.sPONumber
				+ "&SearchFieldAlias11=PO%20Number"
				+ "&SearchField12=" + SMTableorderheaders.sOrderCreatedByFullName
				+ "&SearchFieldAlias12=Created%20By%20Full%20Name"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sCustomerCode
				+ "&ResultHeading3=Customer%20Acct."
				+ "&ResultListField4="  + "CompleteBillToAddress"
				+ "&ResultHeading4=Bill%20To%20Address"
				+ "&ResultListField5="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading5=Ship%20To%20Name"
				+ "&ResultListField6="  + "CompleteShipToAddress"
				+ "&ResultHeading6=Ship%20To%20Address"
				+ "&ResultListField7="  + SMTableorderheaders.sBillToContact
				+ "&ResultHeading7=Bill%20To%20Contact"
				+ "&ResultListField8="  + SMTableorderheaders.sBillToPhone
				+ "&ResultHeading8=Bill%20To%20Phone"
				+ "&ResultListField9="  + SMTableorderheaders.sShipToContact
				+ "&ResultHeading9=Ship%20To%20Contact"
				+ "&ResultListField10="  + SMTableorderheaders.sShipToPhone
				+ "&ResultHeading10=Ship%20To%20Phone"
				+ "&ResultListField11="  + SMTableorderheaders.sPONumber
				+ "&ResultHeading11=PO%20Number"
				+ "&ResultListField12="  + SMTableorderheaders.mTicketComments
				+ "&ResultHeading12=Ticket%20Comments"
				+ "&ResultListField13="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading13=Service%20Type"
				+ "&ResultListField14="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading14=Salesperson"
				+ "&ResultListField15="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading15=Order%20Date"
				+ "&ResultListField16="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
							+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
					+ "&" + FinderResults.RESULT_FIELD_ALIAS + "16=CANCELEDDATE"
				+ "&ResultHeading16=Canceled"
				+ "&ResultListField17="  + SMTableorderheaders.sOrderCreatedByFullName
				+ "&ResultHeading17=Created%20By"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find order (extended search)</A>"
				//Add order DETAIL find:
				+ "&nbsp;&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + FinderResults.OBJECT_ORDER_BY_DETAIL
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + "OrderNumber"
				+ "&SearchField1=" + SMTableorderdetails.sItemDesc
				+ "&SearchFieldAlias1=Item%20Description"
				+ "&SearchField2=" + SMTableorderdetails.sItemNumber
				+ "&SearchFieldAlias2=Item%20Number"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading3=Ship%20To%20Name"
				+ "&ResultListField4="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading4=Service%20Type"
				+ "&ResultListField5="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading5=Salesperson"
				+ "&ResultListField6="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading6=Order%20Date"
				+ "&ResultListField7="  + SMTableorderdetails.sItemNumber
				+ "&ResultHeading7=Item%20Number"
				+ "&ResultListField8="  + SMTableorderdetails.sItemDesc
				+ "&ResultHeading8=Item%20Description"
				+ "&ResultListField9="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
					+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
				+ "&" + FinderResults.RESULT_FIELD_ALIAS + "9=CANCELEDDATE"
				+ "&ResultHeading9=Canceled"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find order (detail search)</A>"
				//Quote finder:
				+"&nbsp;&nbsp;" + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.sBillToAddressLine1
				+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
				+ "&SearchField4=" + SMTableorderheaders.sShipToAddress1
				+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading3=Ship%20To%20Name"
				+ "&ResultListField4="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading4=Service%20Type"
				+ "&ResultListField5="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading5=Salesperson"
				+ "&ResultListField6="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading6=Order%20Date"
				//+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				//+ "&ResultHeading4=Phone"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find quote</A>"
		
				//Proposal finder:
				+ "&nbsp;&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + SMTableproposals.ObjectName
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode
				+ "&SearchFieldAlias3=Customer%20Acct."
				+ "&SearchField4=" + FinderResults.COMPLETE_BILL_TO_ADDRESS
				+ "&SearchFieldAlias4=Complete%20Bill%20To%20Address"
				+ "&SearchField5=" + FinderResults.COMPLETE_SHIP_TO_ADDRESS
				+ "&SearchFieldAlias5=Complete%20Ship%20To%20Address"
				+ "&SearchField6=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.mTicketComments
				+ "&SearchFieldAlias6=Ticket%20Comments"
				+ "&SearchField7=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact
				+ "&SearchFieldAlias7=Bill%20To%20Contact"
				+ "&SearchField8=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone
				+ "&SearchFieldAlias8=Bill%20To%20Phone"
				+ "&SearchField9=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToContact
				+ "&SearchFieldAlias9=Ship%20To%20Contact"
				+ "&SearchField10=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToPhone
				+ "&SearchFieldAlias10=Ship%20To%20Phone"
				+ "&SearchField11=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sPONumber
				+ "&SearchFieldAlias11=PO%20Number"
				+ "&ResultListField1="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Proposal%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode
				+ "&ResultHeading3=Customer%20Acct."
				+ "&ResultListField4="  + "CompleteBillToAddress"
				+ "&ResultHeading4=Bill%20To%20Address"
				+ "&ResultListField5="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
				+ "&ResultHeading5=Ship%20To%20Name"
				+ "&ResultListField6="  + "CompleteShipToAddress"
				+ "&ResultHeading6=Ship%20To%20Address"
				+ "&ResultListField7="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact
				+ "&ResultHeading7=Bill%20To%20Contact"
				+ "&ResultListField8="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone
				+ "&ResultHeading8=Bill%20To%20Phone"
				+ "&ResultListField9="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToContact
				+ "&ResultHeading9=Ship%20To%20Contact"
				+ "&ResultListField10="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToPhone
				+ "&ResultHeading10=Ship%20To%20Phone"
				+ "&ResultListField11="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sPONumber
				+ "&ResultHeading11=PO%20Number"
				+ "&ResultListField12="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.mTicketComments
				+ "&ResultHeading12=Ticket%20Comments"
				+ "&ResultListField13="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading13=Service%20Type"
				+ "&ResultListField14="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				+ "&ResultHeading14=Salesperson"
				+ "&ResultListField15="  + SMTableproposals.TableName + "." + SMTableproposals.sdatproposaldate
				+ "&ResultHeading15=Proposal%20Date"
				+ "&ResultListField16="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
							+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
					+ "&" + FinderResults.RESULT_FIELD_ALIAS + "16=CANCELEDDATE"
				+ "&ResultHeading16=Canceled"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find proposal</A>"
				+ "</TD>");

		out.println("</TR></TABLE>");

		out.println("<BR>");

		//List the edit functions here:
		//Retrieve information
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() 
				+ ".doPost - UserID: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName
		);

		if (conn == null){
			out.println("Unable to get data connection");
			return;
		}

		boolean bAllowOrderEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditOrders, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		boolean bAllowChangeOrderEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMEditChangeOrders, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		boolean bAllowPrintJobFolderLabel = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMPrintJobFolderLabel, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		boolean bAllowOrderViewing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMViewOrderInformation, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);

		boolean bAllowCreateQuotes = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMCreateQuotes, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		
		boolean bAllowProposalEditing = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMEditProposals, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
		
		boolean bAllowServiceTicketPrinting = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMPrintServiceTicket, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
		
		boolean bAllowInstallationTicketPrinting = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMPrintInstallationTicket, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
		
		boolean bAllowDeliveryTicketProcessing = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMManageDeliveryTickets, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);

		if (bAllowOrderEditing){
			out.println(
				"<INPUT TYPE=\"SUBMIT\" NAME = \"" + CREATEORDER_BUTTON_NAME 
				+ "\" VALUE=\"" + CREATEORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
				);
			if (bAllowCreateQuotes){
				out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + CREATEQUOTE_BUTTON_NAME 
				+ "\" VALUE=\"" + CREATEQUOTE_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
				);
			}
			out.println("&nbsp;<B>For customer:</B>&nbsp;" 
					+ "<INPUT TYPE=TEXT NAME=\"" + SMOrderHeader.ParamsCustomerCode 
					+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerCode, request) + "\""
					+ " class = \"text\""
					+ " style=\"width:100px;\"" 
					+ " MAXLENGTH = " + Integer.toString(SMTablearcustomer.sCustomerNumberLength) 
					+ ">"
					+ "&nbsp;<INPUT TYPE=\"SUBMIT\" NAME = \"" + FINDCUSTOMER_BUTTON_NAME 
					+ "\" VALUE=\"" + FINDCUSTOMER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
			);
			
			//Add a link to add a new customer, if the user has permission:
			if(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.AREditCustomers, 
				sUserID,
				conn,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				out.println("&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smar.AREditCustomersEdit?" + ARCustomer.ParamsCustomerNumber + "=''" 
					+ "&SubmitAdd=Y"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "Add a new customer" + "</A>"
				);
			}
			out.println("<BR>");
		}

		if (bAllowOrderEditing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + EDITORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + EDITORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + "><BR>");
		}
		
		if (bAllowProposalEditing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + EDIT_PROPOSAL_NAME
					+ "\" VALUE=\"" + EDIT_PROPOSAL_LABEL + "\"" + "class = \"buttonstyle\"" + "><BR>");
		}
		
		if (bAllowChangeOrderEditing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + EDITCHANGEORDERS_BUTTON_NAME 
					+ "\" VALUE=\"" + EDITCHANGEORDERS_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + "><BR>");
		}

		if (bAllowPrintJobFolderLabel){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + PRINTJOBFOLDERLABEL_BUTTON_NAME 
					+ "\" VALUE=\"" + PRINTJOBFOLDERLABEL_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + "><BR>");
		}

		if (bAllowOrderEditing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + CANCELORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + CANCELORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">&nbsp;"
					+ "<B>Confirm Cancel:</B>" 
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CANCELORDER_CHECKBOX_NAME 
					+ "\" VALUE=\"" + "1\"" + ">"
					+ "<BR>"
			);
					
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + UNCANCELORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + UNCANCELORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">&nbsp;"
					+ "<B>Confirm UNCancel:</B>" 
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_UNCANCELORDER_CHECKBOX_NAME 
					+ "\" VALUE=\"" + "1\"" + ">"
					+ "<BR>"
			);
		}
		
		if (bAllowServiceTicketPrinting){
			String sNumberOfServiceCopies = clsManageRequestParameters.get_Request_Parameter(
					SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES, request).trim();
			if (sNumberOfServiceCopies.compareToIgnoreCase("") == 0){
				sNumberOfServiceCopies = "1";
			}
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + PRINTSERVICEWORKORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + PRINTSERVICEWORKORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
					+ "&nbsp;"
					);
			out.println("<B>Number of copies:</B> " 
	    			+ " <SELECT "
					+ " NAME = \"" + NUMBEROFSERVICEWORKORDERCOPIES + "\""
					+ " ID = \"" + NUMBEROFSERVICEWORKORDERCOPIES + "\""
					+ ">");
			
			long lNumberOfServiceCopies = Long.parseLong(sNumberOfServiceCopies);
			for (long l = 0; l < 5; l++){
				if ((l + 1)== lNumberOfServiceCopies){
					out.println("<OPTION SELECTED VALUE = \"" + Long.toString(l + 1) + "\""
							+ ">" + Long.toString(l + 1)
							+ "</OPTION>");
				}else{
				out.println("<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
					+ ">" + Long.toString(l + 1)
					+ "</OPTION>");
				}
			}
			out.println("</SELECT><BR>");
		}
		
		if (bAllowInstallationTicketPrinting){
			String sNumberOfInstallationCopies = clsManageRequestParameters.get_Request_Parameter(
					SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES, request).trim();
			if (sNumberOfInstallationCopies.compareToIgnoreCase("") == 0){
				sNumberOfInstallationCopies = "1";
			}
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + PRINTINSTALLATIONWORKORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + PRINTINSTALLATIONWORKORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
					+ "&nbsp;"
					);
			out.println("<B>Number of copies:</B> " 
	    			+ " <SELECT "
					+ " NAME = \"" + NUMBEROFINSTALLATIONWORKORDERCOPIES + "\""
					+ " ID = \"" + NUMBEROFINSTALLATIONWORKORDERCOPIES + "\""
					+ ">");
			
			long lNumberOfInstallationCopies = Long.parseLong(sNumberOfInstallationCopies);
			for (long l = 0; l < 5; l++){
				if ((l + 1)== lNumberOfInstallationCopies){
					out.println("<OPTION SELECTED VALUE = \"" + Long.toString(l + 1) + "\""
							+ ">" + Long.toString(l + 1)
							+ "</OPTION>");
				}else{
				out.println("<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
					+ ">" + Long.toString(l + 1)
					+ "</OPTION>");
				}
			}
			out.println("</SELECT><BR>");
		}
		
		if(bAllowDeliveryTicketProcessing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + ADDDELIVERYTICKET_BUTTON_NAME 
					+ "\" VALUE=\"" + ADDDELIVERYTICKET_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + ">"
					+ "&nbsp;"
					+ "<BR>"
			);
		}
		
		//Keep this at the bottom:
		if (bAllowOrderViewing){
			out.println("<INPUT TYPE=\"SUBMIT\" NAME = \"" + VIEWORDER_BUTTON_NAME 
					+ "\" VALUE=\"" + VIEWORDER_BUTTON_LABEL + "\"" + "class = \"buttonstyle\"" + "><BR>");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		out.println("</FORM>");

		//Set the default focus:
		out.println("<script language=\"JavaScript\">");
		//out.println("var frmvalidator = new Validator('MAINFORM');\n"
		//			+ "frmvalidator.addValidation('OrderNumber','req','Please enter an order number');\n"
		// 			+ "frmvalidator.addValidation('OrderNumber','num', 'Order number contains number only ');\n");
		out.println("document.MAINFORM.OrderNumber.focus();");
		out.println("</script>");

		out.println("</BODY></HTML>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}