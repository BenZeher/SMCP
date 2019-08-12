package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMConfigWorkOrderAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public static String LASTENTRYEDITED_PARAM = "LastEntryEdited";
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMConfigureWorkOrders)){return;}
		
		//If there was an 'original calling class' parameter, then we can build the 'Original URL':
		String sOriginalTruckScheduleURL = "";
		boolean bReturnToTruckSchedule = 
				clsManageRequestParameters.get_Request_Parameter(
						SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM, request).compareToIgnoreCase("Y") == 0;
		//So IF we are told to return to the truck schedule:
		String sTruckScheduleQueryString = "";
		if (bReturnToTruckSchedule){
			sTruckScheduleQueryString = 
				(String) smaction.getCurrentSession().getAttribute(SMViewTruckScheduleReport.TRUCKSCHEDULEQUERYSTRING);
			//Trap the null - this was giving an occasional error - TJR - 9/23/2013
			if (sTruckScheduleQueryString == null){
				sTruckScheduleQueryString = "";
			}
			//Then IF there is a truck schedule query string in the session:
			if (sTruckScheduleQueryString.compareToIgnoreCase("") != 0){
				//We save that as the 'original URL' so we can go back to it:
				sOriginalTruckScheduleURL = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMViewTruckScheduleGenerate?"
				+ sTruckScheduleQueryString
				//We leave this off for now, because if we are ADDING a new entry, this ID won't be correct yet:
				//+ "&" + LASTENTRYEDITED_PARAM + "=" + entry.slid() 
				;
			}
		}
		SMWorkOrderHeader workorder;
		workorder = new SMWorkOrderHeader();
		try {
			workorder.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
					"Error updating reading request information: " + e2.getMessage(), 
					"", 
					""
			);
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " loadFromHTTPRequest failed");
    		}
			return;
		}
		
		smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
	    String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.COMMAND_FLAG, request);
	    
	    //This is the string that will be used to redirect the user back to the previous page:
	    String 	sRedirectString = SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
		;
		if (bReturnToTruckSchedule){
			sRedirectString += "&" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "=Y";
		}
	    
		//Save:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMConfigWorkOrderEdit.SAVECOMMAND_VALUE) == 0){
    		try {
				workorder.saveFromConfigure(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					SMWorkOrderHeader.SAVING_FROM_CONFIGURE_SCREEN);
			} catch (Exception e) {
				sRedirectString += "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid();
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				//Keep the state of the 'record was changed' variable:
				sRedirectString += "&" + SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG, request);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					sRedirectString
				);
				return;
			}
    		//If the save succeeds:
    		//Add the ID to the redirect string:
    		sRedirectString += "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid();
    		smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
			//If there is an original URL, then go back to that:
    		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			//If there's NOT an original URL, then go to the 'Redirect' string:
			}else{
				if (sOriginalTruckScheduleURL.compareToIgnoreCase("") != 0){
					response.sendRedirect(
						sOriginalTruckScheduleURL
						+ "&" + LASTENTRYEDITED_PARAM + "=" + workorder.getlid());
				}else{
					smaction.redirectAction(
						"", 
						SMTableworkorders.ObjectName + " was successfully saved.",
						sRedirectString
					);
				}
			}
    	}
    	
    	//If it's a request to delete the work order:
    	if (sCommandValue.compareToIgnoreCase(
    			SMConfigWorkOrderEdit.DELETECOMMAND_VALUE) == 0){
    		try {
    			workorder.delete(getServletContext(), sDBID, sUserID, sUserFullName, SMWorkOrderHeader.SAVING_FROM_CONFIGURE_SCREEN);
			} catch (Exception e) {
	    		//Add the ID to the redirect string:
	    		sRedirectString += "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid();
				//Keep the state of the 'record was changed' variable:
				sRedirectString += "&" + SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG, request);
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					"Could not delete work order: " + e.getMessage(), 
					"", 
					sRedirectString
				);
				return;
			}
    		//If the delete succeeds:
			smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
			//If there is an original URL, then go back to that:
    		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			//If there's NOT an original URL, then go to the 'Redirect' string:
			}else{
				if (sOriginalTruckScheduleURL.compareToIgnoreCase("") != 0){
					response.sendRedirect(
						sOriginalTruckScheduleURL
						+ "&" + LASTENTRYEDITED_PARAM + "=" + workorder.getlid());
				}else{
					//If it is successfully deleted, and we aren't returning to the truck schedule or some other URL,
					//then we need to return to the 'Configure' screen, but with an lid of '-1':
					sRedirectString = SMWorkOrderHeader.Paramlid + "=" + "-1"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID();
					smaction.redirectAction(
						"", 
						SMTableworkorders.ObjectName + " was successfully deleted.",
						sRedirectString
					);
				}
			}
    	}
    	
    	//If it's a request to post the work order:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMConfigWorkOrderEdit.POSTCOMMAND_VALUE) == 0){
    		//Add the ID to the redirect string:
    		sRedirectString += "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid();
    		try {			
				workorder.post_without_data_transaction(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					SMWorkOrderHeader.SAVING_FROM_CONFIGURE_SCREEN);
			} catch (Exception e) {
				//Keep the state of the 'record was changed' variable:
				sRedirectString += "&" + SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG, request);
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					"Could not post " + SMTableworkorders.ObjectName + ": " + e.getMessage(), 
					"", 
					sRedirectString
				);
				return;
			}
    		String sNotificationStatus = "";
    	   	try {
        		workorder.mailPostingNotification(
        			sUserID,
        			sUserFullName,
        			getServletContext(), 
        			sDBID,
        			(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
    		} catch (Exception e) {
    			//No need to cancel the post if the email notification doesn't go, but we'll let the user know the email failed:
    			sNotificationStatus = "  Email posting notification was NOT sent - " + e.getMessage();
    		}
        	
    		//If the post succeeded:
    		smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
			//If there is an original URL, then go back to that:
    		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			//If there's NOT an original URL, then go to the 'Redirect' string:
			}else{
				if (sOriginalTruckScheduleURL.compareToIgnoreCase("") != 0){
					response.sendRedirect(
						sOriginalTruckScheduleURL
						+ "&" + LASTENTRYEDITED_PARAM + "=" + workorder.getlid());
				}else{
					smaction.redirectAction(
						"", 
						SMTableworkorders.ObjectName + " was successfully posted." + sNotificationStatus,
						sRedirectString
					);
				}
			}
    	}
    	
    	//If it's a request to un-post the work order:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMConfigWorkOrderEdit.UNPOSTCOMMAND_VALUE) == 0){
			//Add the ID to the redirect string:
			sRedirectString += "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid();
    		try {
				workorder.unpostWorkOrder_without_data_transaction(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					SMWorkOrderHeader.SAVING_FROM_CONFIGURE_SCREEN);
			} catch (Exception e) {
				//Keep the state of the 'record was changed' variable:
				sRedirectString += "&" + SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMConfigWorkOrderEdit.RECORDWASCHANGED_FLAG, request);
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					"Could not unpost work order: " + e.getMessage(), 
					"", 
					sRedirectString
				);
				return;
			}
    		//If it succeeds:
    		smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
			//If there is an original URL, then go back to that:
    		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			//If there's NOT an original URL, then go to the 'Redirect' string:
			}else{
				if (sOriginalTruckScheduleURL.compareToIgnoreCase("") != 0){
					response.sendRedirect(
						sOriginalTruckScheduleURL
						+ "&" + LASTENTRYEDITED_PARAM + "=" + workorder.getlid());
				}else{
					smaction.redirectAction(
						"", 
						SMTableworkorders.ObjectName + " was successfully unposted.",
						sRedirectString
					);
				}
			}
    	}
    	
		//If this class has been called from the 'order finder' button:
		if (request.getParameter(SMConfigWorkOrderEdit.ORDERFINDERBUTTON) != null){
			//Then call the finder to search for orders:
			sRedirectString = 
				//Link to finder:
				SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?ObjectName=Order"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + SMWorkOrderHeader.Paramstrimmedordernumber
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
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*" + SMConfigWorkOrderEdit.RETURNINGFROMORDERFINDER + "=Y"
				+ "*" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
				;
			if (bReturnToTruckSchedule){
				sRedirectString += "*" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "=Y";
			}
			//System.out.println("sRedirectString = " + sRedirectString);
			//Store the work order in the session:
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			try {
				response.sendRedirect(sRedirectString);
			} catch (Exception e) {
				System.out.println("Error [1441899379] redirecting with sRedirectString: '" + sRedirectString + "' - " + e.getMessage());
			}
			return;
		}
		
		//If this class was called from the extended order button:
		if (request.getParameter(SMConfigWorkOrderEdit.EXTENDEDORDERFINDERBUTTON) != null){
			//Then call the finder to do the 'extended' search for orders:
			sRedirectString = 
				//Link to finder:
				SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?ObjectName=" + FinderResults.OBJECT_ORDER_EXTENDED
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + SMWorkOrderHeader.Paramstrimmedordernumber
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
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*" + SMConfigWorkOrderEdit.RETURNINGFROMORDERFINDER + "=Y"
				+ "*" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
				;
			if (bReturnToTruckSchedule){
				sRedirectString += "*" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "=Y";
			}
			//System.out.println("sRedirectString = " + sRedirectString);
			//Store the work order in the session:
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			response.sendRedirect(sRedirectString);
			return;
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
