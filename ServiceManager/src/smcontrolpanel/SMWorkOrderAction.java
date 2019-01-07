package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smar.SMOption;
import ConnectionPool.WebContextParameters;
import SMClasses.SMFinderFunctions;
import SMClasses.SMLogEntry;
import SMClasses.SMMaterialReturn;
import SMClasses.SMWorkOrderDetail;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableworkorderdetailsheets;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMWorkOrderAction extends HttpServlet{
	public static final String WORK_ORDER_EMAIL_SUBJECT = "Work Order Receipt";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1, request)){return;}

		SMWorkOrderHeader workorder;
		workorder = new SMWorkOrderHeader();
		try {
			workorder.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
					"Error updating reading request information: " + e2.getMessage(), 
					"", 
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
			);
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " loadFromHTTPRequest failed");
    		}
			return;
		}
		
		try {
			smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
		} catch (Exception e2) {
			clsServletUtilities.sysprint(this.toString(), smaction.getUserName(), "Error [1423260726]  - " + e2.getMessage() + ".");
		}
	    try {
		} catch (Exception e2) {
			smaction.redirectAction(
					"Error [1424451785] reading session information: " + e2.getMessage(), 
					"", 
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
			);
		}
		
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.COMMAND_FLAG, request);
	    
	    //See if the user had been warned about clicking before the screen was fully displayed:
	    if (clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.FULLY_DISPLAYED_WARNING_FIELD, request).compareToIgnoreCase(SMWorkOrderEdit.FULLY_DISPLAYED_WARNING_VALUE_YES) == 0){
	    	//Record the fact:
	    	SMLogEntry log = new SMLogEntry(smaction.getsDBID(), getServletContext());
	    	log.writeEntry(
	    		smaction.getUserID(), 
	    		SMLogEntry.LOG_OPERATION_WORKORDERCOMMANDWOFULLDISPLAY, 
	    		"WOID:" + workorder.getlid() + " - command: " + sCommandValue, 
	    		"Fully Displayed Warning Was Triggered Previously", 
	    		"[1431635498]");
	    }
	    
	  //If it's a request from an ASYNC post:
    	if (sCommandValue.compareToIgnoreCase(SMWorkOrderEdit.ASYNC_POST) == 0){
    		//Set the response type
    		response.setContentType("text/plain");  
    		response.setCharacterEncoding("UTF-8");
    		
    		//Get any values expected from request
    		String sdattimeleftpreviousValue = clsManageRequestParameters.get_Request_Parameter(SMTableworkorders.dattimeleftprevious, request);
    		String sdattimearrivedatcurrentValue = clsManageRequestParameters.get_Request_Parameter(SMTableworkorders.dattimearrivedatcurrent, request);
    		String sdattimeleftcurrentValue = clsManageRequestParameters.get_Request_Parameter(SMTableworkorders.dattimeleftcurrent, request);
    		String sdattimearrivedatnextValue = clsManageRequestParameters.get_Request_Parameter(SMTableworkorders.dattimearrivedatnext, request);
    		
    		//If this is a request to update the Left previous job time
    		if(sdattimeleftpreviousValue.compareToIgnoreCase("") != 0) {
    			try { 
    				workorder.setdattimeleftprevious(sdattimeleftpreviousValue);
    				workorder.ajax_Update_Left_Previous_Time(getServletContext(), smaction.getsDBID(),smaction.getUserName());
    			} catch (Exception e) {
    				response.getWriter().write("Warning: Failed to update 'Left previous job' time - " + e.getMessage()); 
    				return;
    			}  
    			response.getWriter().write("Updated 'Left previous job' time.");
    			return;
    		}
    		
    		//If this is a request to update the Arrived current job time
    		if(sdattimearrivedatcurrentValue.compareToIgnoreCase("") != 0) {
    			try { 
    				workorder.setdattimearrivedatcurrent(sdattimearrivedatcurrentValue);
    				workorder.ajax_Update_Arrived_Current_Time(getServletContext(), smaction.getsDBID(),smaction.getUserName());
    			} catch (Exception e) {
    				response.getWriter().write("Warning: Failed to update 'Arrived current job' time - " + e.getMessage()); 
    				return;
    			}  			
    			response.getWriter().write("Updated 'Arrived at current job' time.");
    			return;
    		}
    		
    		//If this is a request to update the Left current job time
    		if(sdattimeleftcurrentValue.compareToIgnoreCase("") != 0) {
    			try { 
    				workorder.setdattimeleftcurrent(sdattimeleftcurrentValue);
    				workorder.ajax_Update_Left_Current_Time(getServletContext(), smaction.getsDBID(),smaction.getUserName());
    			} catch (Exception e) {
    				response.getWriter().write("Warning: Failed to update 'Left current job' time - " + e.getMessage()); 
    				return;
    			}  			
    			response.getWriter().write("Updated 'Left current job' time.");
    			return;
    		}
    
    		//If this is a request to update the Arrived next job time
    		if(sdattimearrivedatnextValue.compareToIgnoreCase("") != 0) {
    			try { 
    				workorder.setdattimearrivedatnext(sdattimearrivedatnextValue);
    				workorder.ajax_Update_Arrived_Next_Time(getServletContext(), smaction.getsDBID(),smaction.getUserName());
    			} catch (Exception e) {
    				response.getWriter().write("Warning: Failed to update 'Arrived next job' time - " + e.getMessage()); 
    				return;
    			}  			
    			response.getWriter().write("Updated 'Arrived at next job' time.");
    			return;
    		}
    		
    		
    		return;
    	}
	    
    	//If it's a request to save the work order:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.SAVECOMMAND_VALUE) == 0){
       		try {
    				workorder.saveFromEditScreen(
    					getServletContext(), 
    					smaction.getsDBID(), 
    					smaction.getUserID(),
    					smaction.getFullUserName());
    			} catch (Exception e) {
    				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
    				smaction.redirectAction(
    						e.getMessage(), 
    						"", 
    						SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
    						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
    				);
    				return;
    			}
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					SMTableworkorders.ObjectName + " was successfully saved.",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
			}
    	}
    	
    	//If it's a request to post the work order:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.POSTCOMMAND_VALUE) == 0){
			try {
				smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
			} catch (Exception e) {
				//If there's no valid session, we don't have to remove the attribute...
			}
    		try {
				workorder.post_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					SMWorkOrderHeader.SAVING_FROM_EDIT_SCREEN);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
						"Could not post: " + e.getMessage(), 
						"", 
						SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
    		
       		String sNotificationStatus = "";
    	   	try {
        		workorder.mailPostingNotification( 
        			smaction.getUserID(),
        			smaction.getFullUserName(),
        			getServletContext(), 
        			smaction.getsDBID(),
        			(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
    		} catch (Exception e) {
    			//No need to cancel the post if the email notification doesn't go, but we'll let the user know the email failed:
    			sNotificationStatus = "  Email posting notification was NOT sent - " + e.getMessage();
    		}
    	   	
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					SMTableworkorders.ObjectName + " was successfully posted." + sNotificationStatus,
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
				);
			}
    	}

    	//If it's a request to go to 'signature' mode:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.ACCEPTSIGNATURECOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("[1546720408] In " + this.toString() + " into signature mode");
    			System.out.println("[1546720409] " + workorder.read_out_debug_data());
    		}
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderSignatureEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
			;
			redirectProcess(sRedirectString, response);

			return;
    	}

    	//If it's a request to email:
    	//Record the result of attempting to email:
    	SMLogEntry log = new SMLogEntry(smaction.getsDBID(), getServletContext());
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.EMAILRECEIPTCOMMAND_VALUE) == 0){
    		//First we'll need to reload the work order, because the HTTP request won't carry all the fields we'll need,
    		//in particular the 'signature' which is a JSON string:
    		try {
				workorder.load(smaction.getsDBID(), smaction.getFullUserName(), getServletContext());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					e1.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
				return;
			}
    		try {
				sendEmail(request, workorder, smaction);
			} catch (Exception e) {
				log.writeEntry(
					smaction.getUserID(),
					SMLogEntry.LOG_OPERATION_WORKORDEREMAIL, 
					"Work order #: " + workorder.getlid(), 
					"Attempted to send to: '" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.EMAIL_TO_FIELD, request) + "'"
						+ " - Error message: " + e.getMessage(), 
					"[1417634373]");
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					e.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
				return;
			}
			log.writeEntry(
					smaction.getUserID(),
					SMLogEntry.LOG_OPERATION_WORKORDEREMAIL, 
					"Work order #: " + workorder.getlid(), 
					 "Sent to: '" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.EMAIL_TO_FIELD, request) + "'", 
					"[1417634374]");
			smaction.redirectAction(
				"", 
				"Email sent to " + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.EMAIL_TO_FIELD, request) + ".",
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
			);
			return;
		}
    	//If it's a request to print:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into print mode");
    		}
    		try {
				smaction.getPwOut().println(getHTMLWorkOrderForm(
					1, 
					workorder.getlid(), 
					smaction.getsDBID(),
					smaction,
					false)
				);
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				if (bDebugMode){
					System.out.println("[1395345167] string = "
						//Need this if we are being called from 'importing work orders':
						+ "&" + SMTableorderheaders.strimmedordernumber + "=" + workorder.getstrimmedordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
					);
				}
				smaction.redirectAction(
					e1.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						//Need this if we are being called from 'importing work orders':
						+ "&" + SMTableorderheaders.strimmedordernumber + "=" + workorder.getstrimmedordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
			}
			return;
    	}
    	
    	//If it's a request to find a non-dedicated item:
    	if (sCommandValue.length() > SMWorkOrderEdit.FINDITEM_COMMAND_VALUE_BASE.length()){
	    	if (sCommandValue.substring(0, SMWorkOrderEdit.FINDITEM_COMMAND_VALUE_BASE.length()).compareToIgnoreCase(
	    		SMWorkOrderEdit.FINDITEM_COMMAND_VALUE_BASE) == 0){
	    		//Get the line number from the command string:
	    		String sLineNumber = sCommandValue.substring(SMWorkOrderEdit.FINDITEM_COMMAND_VALUE_BASE.length(), sCommandValue.length());
				String sRedirectString = 
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&ObjectName=" + smar.FinderResults.SEARCH_NONDEDICATEDITEMS
					+ "&ResultClass=FinderResults"
					+ "&SearchingClass=" + smaction.getCallingClass()
					+ "&ReturnField=" 
					+ SMWorkOrderHeader.WORK_ORDER_ITEMLINE_MARKER 
					+ sLineNumber 
					+  SMWorkOrderDetail.Paramsitemnumber
					
					+ SMFinderFunctions.getStdITEMSearchAndResultString()
					
					/*
					+ "&SearchField1=" + SMTableicitems.sItemDescription
					+ "&SearchFieldAlias1=Description"
					+ "&SearchField2=" + SMTableicitems.sItemNumber
					+ "&SearchFieldAlias2=Item%20No."
					+ "&SearchField3=" + SMTableicitems.sComment1
					+ "&SearchFieldAlias3=Comment%201"
					+ "&SearchField4=" + SMTableicitems.sComment2
					+ "&SearchFieldAlias4=Comment%202"
					+ "&ResultListField1="  + SMTableicitems.sItemNumber
					+ "&ResultHeading1=Item%20No."
					+ "&ResultListField2="  + SMTableicitems.sItemDescription
					+ "&ResultHeading2=Description"
					+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
					+ "&ResultHeading3=Cost%20Unit"
					+ "&ResultListField4="  + SMTableicitems.inonstockitem
					+ "&ResultHeading4=Non-stock?"
					+ "&ResultListField5="  + SMTableicitems.sPickingSequence
					+ "&ResultHeading5=Picking%20Sequence"
					*/
					
					+ "&ParameterString="
					+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "*" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "*" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "*" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				;
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				redirectProcess(sRedirectString, response);
				return;
	    	}
    	}
    	//If it's a request to view pricing:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.VIEW_PRICING_COMMAND_VALUE) == 0){
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
				"", 
				"",
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=Y"
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
			);
			return;
		}
    	//If it's a request to remove pricing:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.REMOVE_PRICING_COMMAND_VALUE) == 0){
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
				"", 
				"",
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "="
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
			);
			return;
		}
    	
    	//If it's a request to add a detail sheet:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderHeader.ADD_DETAIL_SHEET_COMMAND_VALUE) == 0){
    		//Find out what kind of detail sheet it is:
    		String sWorkOrderDetailSheetID = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.ADD_DETAIL_SHEET_DROPDOWN_NAME, request);
    		int iDetailSheetType = -1;
    		if (sWorkOrderDetailSheetID.compareToIgnoreCase("") == 0){
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					"You must select a detail sheet from the drop down list.", 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "="
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + "Y"
				);
				return;
    		}
			String SQL = "SELECT"
				+ " " + SMTableworkorderdetailsheets.itype
				+ " FROM " + SMTableworkorderdetailsheets.TableName
				+ " WHERE ("
					+ "(" + SMTableworkorderdetailsheets.lid + " = " + sWorkOrderDetailSheetID + ")"
				+ ")"
			;
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				getServletContext(),
    				smaction.getsDBID(), 
    				"MySQL", 
    				SMUtilities.getFullClassName(this.toString()) + ".checking for detail sheet type - user: " + smaction.getUserID()
    				+ " - "
    				+ smaction.getFullUserName()
    			);
    			if (rs.next()){
    				iDetailSheetType = rs.getInt(SMTableworkorderdetailsheets.itype);
    			}
    			rs.close();
    		} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					"Error [1436907531] reading detail sheet type with SQL: '" + SQL + "'- " + e.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "="
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + "Y"
				);
				return;
    		}
    		
    		String sRedirectString = "";
    		if (iDetailSheetType == SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML){
    			//Call the custom detail sheet screen:
				sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCustomDetailSheetEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
				+ "&" + SMWorkOrderHeader.ADD_DETAIL_SHEET_DROPDOWN_NAME + "=" + sWorkOrderDetailSheetID
				+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				+ "&CallingClass=" + smaction.getCallingClass()
				;
				redirectProcess(sRedirectString, response);
    			return;
    		}
    		
    		//Otherwise, go on and paste the plain text detail sheet:
    		try {
				workorder.addDetailSheet(request, smaction, getServletContext(), SMWorkOrderHeader.SAVING_FROM_EDIT_SCREEN);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					e.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "="
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + "Y"
				);
				return;
			}
			smaction.redirectAction(
				"", 
				"Detail sheet was successfully added.",
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "="
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + "Y"
			);
			return;
		}
    	
    	//If it's a request to create a material return:
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.MATERIALRETURNCOMMAND_VALUE) == 0){
    		String sMaterialReturnID = "";
    		try {
    			sMaterialReturnID = createMaterialReturn(request, workorder, smaction);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					e.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
				return;
			}
			smaction.redirectAction(
				"", 
				"Material return # " + sMaterialReturnID + " was created for this work order.",
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
			);
			return;
		}
    	
       	//If Create And Upload Folder Button was pressed
    	if (sCommandValue.compareToIgnoreCase(
    		    SMWorkOrderEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
    		SMOption opt = new SMOption();
        	try {
				opt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
				smaction.redirectAction(
					e1.getMessage(), 
					"",
					SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request)
				);
				return;
			}

        	String sRedirectString = "";
        	String sNewFolderName = opt.getgdriveworkorderfolderprefix() + workorder.getlid() + opt.getgdriveworkorderfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
			try {
	       		 sRedirectString = opt.getgdriveuploadfileurl()
	              		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + opt.getgdriveworkorderparentfolderid()
	        				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGDriveFolder.WORK_ORDER_TYPE_PARAM_VALUE
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + workorder.getlid()
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + opt.getBackGroundColor()
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
	                  	;
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
    	
       	//If Add most recent Items
    	if (sCommandValue.compareToIgnoreCase(SMWorkOrderEdit.RECENTITEMSCOMMAND_VALUE) == 0){
    		//Add most recent lines to the work order
    		String sWarning = "";
    		String sStatus = "NOTE: The " + workorder.getsnumberofitems() + " most used items over the last " + workorder.getsnumberofdays()
    				+ " days were added to the list of items."
    		;
    		try {
				workorder.add_most_recent_lines(workorder, 
												getServletContext(), 
												smaction.getsDBID(),  
												smaction.getUserID(),
												smaction.getFullUserName());
				//Log that these items have been displayed
				//log.writeEntry(
				//		smaction.getUserName(),
				//		SMLogEntry.LOG_OPERATION_WORKORDERRECENTITEMSDISPLAY, 
				//		"work order #: " + workorder.getlid() 
				//			+ ", currently including " + Integer.toString(workorder.getDetailCount()) + " lines"
				//			+ ", using imechid = '" + workorder.getmechid() + "', ", 
				//		 "Viewed " + workorder.getsnumberofitems() + " items for the past " + workorder.getsnumberofdays()+ " days", 
				//		"[1456855451]");
			} catch (Exception e) {
				sWarning = e.getMessage();
				sStatus = "";
			}
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
				sWarning, 
				sStatus,
				SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + SMWorkOrderEdit.RETURNINGFROMRECENTITEMS_PARAM + "=Y"
					);
			return;
    	}    	
		return;
	}
	
	private String getCreateGDriveReturnURL(HttpServletRequest req) throws Exception{
		String sTemp = "";
		try {
			sTemp = clsServletUtilities.getServerURL(req, getServletContext());
		}catch(Exception e) {
			throw new Exception("Error [1542748060] "+e.getMessage());
		}
		sTemp += "/" + WebContextParameters.getInitWebAppName(getServletContext()) + "/";
		sTemp += "smcontrolpanel.SMCreateGDriveFolder";
		return sTemp;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395236124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395236125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	private String getHTMLWorkOrderForm(
			int iNumberOfCopies, 
			String sWorkOrderNumber, 
			String sConf,
			SMMasterEditAction smaction,
			boolean bEmailMode) throws Exception{
		String s = 
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
				+ "<HTML lang=\"" + SMUtilities.LANGUAGE_HTML_ENGLISH + "\"> <HEAD>" 
		   		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}\n"
		   		+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
		   		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
		   		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
		   		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
		   		+ "@page { size:8.5in 11in; margin: 0.4in }\n"
		   		+ "</STYLE>"
		   		;
		//For printing signature:
		s += "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
					+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>";
		s += "</HEAD><BODY BGCOLOR=\"#FFFFFF\">"
		;
	 	//Retrieve information
	 	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".printProposal - user: " + smaction.getUserName()
					);
		} catch (Exception e1) {
	 		s += "<FONT COLOR=RED>Unable to get data connection - " + e1.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 	
	 	SMWorkOrderReceipt wor = new SMWorkOrderReceipt();
	 	try {
	 		

	 		
	 		s += wor.processReport(sConf, 
	 				sWorkOrderNumber, 
	 				smaction.getUserName(),
	 				smaction.getUserID(),
	 				smaction.getFullUserName(),
	 				getServletContext(), 
	 				bEmailMode);
	 		
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	 		s += "<FONT COLOR=RED>Error - " + e.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 		
	 	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	 	s += "</BODY></HTML>";
	 	return s;
	}
	
	private void sendEmail(HttpServletRequest req, SMWorkOrderHeader wo, SMMasterEditAction sm) throws Exception{
		if (bDebugMode){
			System.out.println("In " + this.toString() + " into email mode");
		}
		String sEmailTo = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.EMAIL_TO_FIELD, req);
		if (bDebugMode){
			System.out.println("EMAILTO = " + sEmailTo);
		}
		if (sEmailTo.compareToIgnoreCase("") == 0){
			throw new Exception("Email address cannot be blank.");
		}
		SMOption opt = new SMOption();
		try {
			opt.load(sm.getsDBID(), getServletContext(), sm.getUserName());
		} catch (Exception e) {
			throw new Exception("Error loading SM Options data to email work order receipt - " + e.getMessage() + ".");
		}
		String sSystemRootPath = SMUtilities.getAbsoluteRootPath(req, getServletContext());
		
		String sSubject = "";
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
			);
			if (rs.next()){
				sSubject += rs.getString(SMTablecompanyprofile.sCompanyName).trim() + " - " + WORK_ORDER_EMAIL_SUBJECT
						+ " #" + wo.getlid();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1395168775] reading company profile - " + e.getMessage() + ".");
		}
		
		try {
			String sWorkOrderReceipt = getHTMLWorkOrderForm(1, wo.getlid(), sm.getsDBID(), sm, true);
	        
			String sSignatureBoxWidth = wo.getlsignatureboxwidth();
			//If a signature has not been collected get the default dimensions for the signature.
			if(sSignatureBoxWidth.compareToIgnoreCase("0") == 0){
				sSignatureBoxWidth = opt.getisignatureboxwidth();
				wo.setlsignatureboxwidth(sSignatureBoxWidth);
			} 
			int iSignatureWidth = Integer.parseInt(sSignatureBoxWidth);
			String sSignatureBoxHeight = Integer.toString((int) (Math.round(iSignatureWidth/SMTablesmoptions.SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO)));
			
            clsEmailInlineHTML.emailEmbeddedHTMLWithSignature(
            	sSystemRootPath,
            	wo.getmsignature(), 
            	Integer.parseInt(sSignatureBoxWidth),
	    		Integer.parseInt(sSignatureBoxHeight),
	    		Integer.parseInt(SMTableworkorders.SIGNATURE_PEN_WIDTH),
	    		SMTableworkorders.SIGNATURE_PEN_R_COLOUR,
	    		SMTableworkorders.SIGNATURE_PEN_G_COLOUR,
	    		SMTableworkorders.SIGNATURE_PEN_B_COLOUR,
            	opt.getSMTPServer(), 
            	opt.getSMTPUserName(), 
            	opt.getSMTPPassword(), 
            	sEmailTo, 
            	opt.getSMTPReplyToAddress(),
            	sSubject, 
            	sWorkOrderReceipt, 
            	getServletContext());
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		return;
	}
	private String createMaterialReturn(HttpServletRequest req, SMWorkOrderHeader wo, SMMasterEditAction sm) throws Exception{
		if (bDebugMode){
			System.out.println("In " + this.toString() + " into create material return mode");
		}
		String sDescription = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.MATERIAL_RETURN_DESCRIPTION_FIELD, req);
		if (bDebugMode){
			System.out.println("MATERIAL RETURN DESCRIPTION = " + sDescription);
		}
		if (sDescription.compareToIgnoreCase("") == 0){
			throw new Exception("You must enter a description to create a material return.");
		}
		SMMaterialReturn matreturn = new SMMaterialReturn();
		matreturn.setsdescription(sDescription);
		matreturn.setsNewRecord("1");
		matreturn.setstrimmedordernumber(wo.getstrimmedordernumber());
		matreturn.setsworkorderid(wo.getlid());

		try {
			matreturn.save_without_data_transaction(getServletContext(), 
													sm.getsDBID(), 
													sm.getUserID(), 
													sm.getFullUserName());
		} catch (Exception e2) {
			throw new Exception("Could not create material return - " + e2.getMessage());
		}
		return matreturn.getslid();
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

