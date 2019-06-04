package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMEditOrderHandler extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMManageOrders)
		){
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";
		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, request).trim();
		String sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerCode, request).trim().toUpperCase();
		
		//If this class has been called from the 'find customer' button:
		if (request.getParameter(SMEditOrderSelection.FINDCUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsCustomerCode
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.iActive
				+ "&ResultHeading5=Active"
				+ "&ResultListField6="  + SMTablearcustomer.iOnHold
				+ "&ResultHeading6=On%20Hold"
				+ "&ResultListField7="  + SMTablearcustomer.sCustomerGroup
				+ "&ResultHeading7=Customer%20Group"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ParameterString=*" + SMEditOrderSelection.RETURNINGFROMFINDCUSTOMER_PARAM + "=TRUE"
			;
			redirectProcess(sRedirectString, response);
			return;
		}
		if (request.getParameter(SMEditOrderSelection.CREATEORDER_BUTTON_NAME) != null){
			if (sCustomerNumber.compareToIgnoreCase("") == 0){
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderSelection"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + clsServletUtilities.URLEncode(sOrderNumber)
				+ "&CallingClass=" + sCallingClass
				+ "&Warning=You need a customer number to create an order."
				;
			}else{
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMOrderHeader.ParamsCustomerCode + "=" + clsServletUtilities.URLEncode(sCustomerNumber)
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			}
			redirectProcess(sRedirectString, response);
			return;
		}
		
		if (request.getParameter(SMEditOrderSelection.CREATEQUOTE_BUTTON_NAME) != null){
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMOrderHeader.ParamsCustomerCode + "=" + clsServletUtilities.URLEncode(sCustomerNumber)
			+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
			+ "&" + SMOrderHeader.ParamiOrderType + "=" + SMTableorderheaders.ORDERTYPE_QUOTE
			+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
			;
			redirectProcess(sRedirectString, response);
			return;
		}
		
		if (sOrderNumber.compareToIgnoreCase("") == 0){
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderSelection"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
			+ "&CallingClass=" + sCallingClass
			+ "&Warning=Order number cannot be blank"
			;
		}else{
			//Depending on which button was pushed, redirect to the correct function:
			if (request.getParameter(SMEditOrderSelection.PRINTSERVICEWORKORDER_BUTTON_NAME) != null){
				String sPrintWorkOrderClass = "smcontrolpanel.SMPrintServiceTicketGenerate";
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ sPrintWorkOrderClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + "StartingOrderNumber" + "=" + sOrderNumber
				+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES, request)
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			}
			if (request.getParameter(SMEditOrderSelection.PRINTINSTALLATIONWORKORDER_BUTTON_NAME) != null){
				String sPrintWorkOrderClass = "smcontrolpanel.SMPrintInstallationTicketGenerate";
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ sPrintWorkOrderClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + "StartingOrderNumber" + "=" + sOrderNumber
				+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES, request)
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			}
			if (request.getParameter(SMEditOrderSelection.ADDDELIVERYTICKET_BUTTON_NAME) != null){
				String sAddDeliveryTicketClass = "smcontrolpanel.SMEditDeliveryTicketEdit";
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ sAddDeliveryTicketClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTabledeliverytickets.strimmedordernumber + "=" + sOrderNumber
				+ "&" + SMTabledeliverytickets.lid + "=-1" 
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			}
			if (request.getParameter(SMEditOrderSelection.EDITORDER_BUTTON_NAME) != null){
				try {
					checkForCanceledOrder(sOrderNumber, sDBID, sUserID, sUserFullName);
				} catch (Exception e) {
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=" + e.getMessage()
					;
				}
				if (sRedirectString.compareToIgnoreCase("") == 0){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					;
				}
			}
			if (request.getParameter(SMEditOrderSelection.EDIT_PROPOSAL_NAME) != null){
				try {
					checkForCanceledOrder(sOrderNumber, sDBID, sUserID, sUserFullName);
				} catch (Exception e) {
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=" + e.getMessage()
					;
				}
				if (sRedirectString.compareToIgnoreCase("") == 0){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMProposalEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMProposal.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					;
				}
			}
			if (request.getParameter(SMEditOrderSelection.EDITCHANGEORDERS_BUTTON_NAME) != null){
				try {
					checkForCanceledOrder(sOrderNumber, sDBID, sUserID, sUserFullName);
				} catch (Exception e) {
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=" + e.getMessage()
					;
				}
				SMOrderHeader order = new SMOrderHeader();
				order.setM_strimmedordernumber(sOrderNumber.trim());
				if (!order.load(getServletContext(), sDBID, sUserID, sUserFullName)){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=Could not load order to check order type."
					;
				}else{
					if (order.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
						sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
						+ sCallingClass
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
						+ "&CallingClass=" + sCallingClass
						+ "&Warning=You cannot add change orders to quotes."
						;
					}
				}
				if (sRedirectString.compareToIgnoreCase("") == 0){
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditChangeOrdersEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
				}
			}

			if (request.getParameter(SMEditOrderSelection.PRINTJOBFOLDERLABEL_BUTTON_NAME) != null){
				//We have to find out what kind of order this is, to print the correct work order type:
				String sPrintJobFolderLabelClass = "smcontrolpanel.SMPrintJobFolderLabelGenerate";
				SMOrderHeader ord = new SMOrderHeader();
				ord.setM_strimmedordernumber(sOrderNumber);
				if (!ord.load(getServletContext(), sDBID, sUserID, sUserFullName)){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sPrintJobFolderLabelClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "StartingOrderNumber" + "=" + sOrderNumber
					//+ "&" + SMEditOrderSelection.NUMBEROFWORKORDERCOPIES + "=" 
					//+ SMUtilities.get_Request_Parameter(SMEditOrderSelection.NUMBEROFWORKORDERCOPIES, request)
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					+ "&Warning=Could not load order # " + sOrderNumber + " - " + ord.getErrorMessages()
					;
				}else{
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sPrintJobFolderLabelClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "StartingOrderNumber" + "=" + sOrderNumber
					//+ "&" + SMEditOrderSelection.NUMBEROFWORKORDERCOPIES + "=" 
					//+ SMUtilities.get_Request_Parameter(SMEditOrderSelection.NUMBEROFWORKORDERCOPIES, request)
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					;
				}
			}
			if (request.getParameter(SMEditOrderSelection.CLONEORDER_BUTTON_NAME) != null){
				String sCloneOrderClass = "smcontrolpanel.SMCloneOrder";
				SMOrderHeader ord = new SMOrderHeader();
				ord.setM_sOrderNumber(sOrderNumber);
				if (!ord.load(getServletContext(), sDBID, sUserID, sUserFullName)){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCloneOrderClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "StartingOrderNumber" + "=" + sOrderNumber
					//+ "&" + SMEditOrderSelection.NUMBEROFWORKORDERCOPIES + "=" 
					//+ SMUtilities.get_Request_Parameter(SMEditOrderSelection.NUMBEROFWORKORDERCOPIES, request)
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					+ "&Warning=Could not load order # " + sOrderNumber + " - " + ord.getErrorMessages()
					;
				}else{
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCloneOrderClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "OrderNumber" + "=" + sOrderNumber
					//+ "&" + SMEditOrderSelection.NUMBEROFWORKORDERCOPIES + "=" 
					//+ SMUtilities.get_Request_Parameter(SMEditOrderSelection.NUMBEROFWORKORDERCOPIES, request)
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					;
				}
			}
			if (request.getParameter(SMEditOrderSelection.CANCELORDER_BUTTON_NAME) != null){
				if (request.getParameter(SMEditOrderSelection.CONFIRM_CANCELORDER_CHECKBOX_NAME) == null){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=You chose to cancel an order but you did not click the checkbox to confirm."
					;
				}else{
					SMOrderHeader ord = new SMOrderHeader();
					if (!ord.cancelOrder(sOrderNumber, getServletContext(), sDBID, sUserID, sUserFullName)){
						sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
						+ sCallingClass
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
						+ "&CallingClass=" + sCallingClass
						+ "&Warning=Could not cancel order " + sOrderNumber + " - " + ord.getErrorMessages()
						;
					}else{
						sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
						+ sCallingClass
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
						+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
						+ "&Status=Successfully canceled order number " + sOrderNumber + "."
						;
					}
				}
			}
			if (request.getParameter(SMEditOrderSelection.UNCANCELORDER_BUTTON_NAME) != null){
				if (request.getParameter(SMEditOrderSelection.CONFIRM_UNCANCELORDER_CHECKBOX_NAME) == null){
					sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
					+ "&CallingClass=" + sCallingClass
					+ "&Warning=You chose to UNcancel an order but you did not click the checkbox to confirm."
					;
				}else{
					SMOrderHeader ord = new SMOrderHeader();
					if (!ord.uncancelOrder(sOrderNumber, getServletContext(), sDBID, sUserID, sUserFullName)){
						sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
						+ sCallingClass
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
						+ "&CallingClass=" + sCallingClass
						+ "&Warning=Could not UNcancel order " + sOrderNumber + " - " + ord.getErrorMessages()
						;
					}else{
						sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
						+ sCallingClass
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
						+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
						+ "&Status=Successfully UNcanceled order number " + sOrderNumber + "."
						;
					}
				}
			}
			if (request.getParameter(SMEditOrderSelection.VIEWORDER_BUTTON_NAME) != null){
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			}
			
		}
		redirectProcess(sRedirectString, response);
		return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
		
	}
	private void checkForCanceledOrder(String sOrderNumber, String sDBID, String sUserID, String sUserFullName) throws Exception{
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sOrderNumber.trim());
		if (!order.load(getServletContext(), sDBID, sUserID, sUserFullName)){
			throw new Exception("Could not load order to check canceled date.<BR>" + order.getErrorMessages());
		}else{
			String sCancelDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(
				order.getM_datOrderCanceledDate());
			if (sCancelDate.compareTo("1899-12-31 00:00:00") > 0){
				throw new Exception("Canceled orders cannot be updated.");
			}
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}