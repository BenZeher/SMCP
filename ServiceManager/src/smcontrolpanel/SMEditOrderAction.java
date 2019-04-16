package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ConnectionPool.ServerSettingsFileParameters;
import ConnectionPool.WebContextParameters;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smar.SMOption;

public class SMEditOrderAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){return;}
		//System.out.println("CallingClass2 = " + smaction.getCallingClass());
	    //Read the entry fields from the request object:
		SMOrderHeader entry;
		entry = new SMOrderHeader(request);
		
		smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
		String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
      	
	    String sGDriveReturnURL = "";
    	try {
    		sGDriveReturnURL = getCreateGDriveReturnURL(request);
    	}catch(Exception e) {
    		smaction.getPwOut().println(
					"<BR><FONT COLOR=RED><B>" 
					+ "Error getting return url for '" + entry.getM_strimmedordernumber() 
					+ "' - " + e.getMessage() 
					+ "</B></FONT>");
    	}
    	//If it's a request to clone:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    	SMEditOrderEdit.CLONEORDERCOMMAND_VALUE) == 0){

    		Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			smaction.getsDBID(), 
    			"MySQL", 
    			this.toString() + ".CLONEORDER - " +
    					"user: " + smaction.getUserID() + " - " + smaction.getFullUserName() + 
    					" [1331729557]");
    		if (conn == null){
    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not get data connection.", 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
    		}
    		
			boolean bCloneProposal;
			if (request.getParameter(SMEditOrderEdit.CLONEPROPOSAL_CHECKBOX) != null){
				bCloneProposal = true;
			}else{
				bCloneProposal = false;
			}
    		//Do not allow a proposal to be cloned for an active or standing order - it should only be possible for a quote:
    		if (bCloneProposal){
    			if (entry.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
    				clsDatabaseFunctions.rollback_data_transaction(conn);
    				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080500]");
        			smaction.redirectAction( 
					"You can only clone proposals on quotes, not on active or standing orders", 
					"",
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
					);
        			return;
    			}
    		}
			//complete loading the order. 
    		if(!entry.load(conn)){
   				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080501]");
    			smaction.redirectAction( 
				entry.getErrorMessages(), 
				"",
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
				+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
    			return;
    		}
			boolean bCloneDetails;
			if (request.getParameter(SMEditOrderEdit.CLONEDETAILS_CHECKBOX) != null){
				bCloneDetails = true;
			}else{
				bCloneDetails = false;
			}
			
			//get a cloned order.
			SMOrderHeader newOrder = null;
			Date datNow = null;

			try {
				clsDBServerTime st = new clsDBServerTime(sDBID, sUserFullName, getServletContext());
				datNow = st.getCurrentTimeAsJavaSQLDate();
				newOrder = entry.Clone(bCloneDetails, 
									   datNow, 
									   sUserID,
									   sUserFullName,
									   conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080502]");
    			smaction.redirectAction( 
				e.getMessage(), 
				"",
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
				+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
    			return;
			}
			//Save cloned order immediately.
			clsDatabaseFunctions.start_data_transaction(conn);
			//save order directly without any other actions
			if (!newOrder.save_cloned_order(conn, 
								     smaction.getUserID(), 
								     smaction.getFullUserName(), 
								     "Clone_Order")){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080503]");
    			smaction.redirectAction( 
				"Error cloning order - " + newOrder.getErrorMessages(), 
				"",
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
				+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				
			}else{
				//If we are cloning the proposal, try that now:
				if (bCloneProposal){
					SMProposal proposal = new SMProposal(entry.getM_strimmedordernumber());
					try {
						proposal.load(getServletContext(), sDBID);
					} catch (Exception e) {
        				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080504]");
            			smaction.redirectAction( 
    					"Error cloning order - " + e.getMessage(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
						+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
						+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
    					);
					}
					try {
						proposal.cloneProposal(
							conn, 
							smaction.getUserID(), 
							smaction.getUserName(),
							newOrder.getM_strimmedordernumber(),
							(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
					} catch (Exception e) {
        				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080505]");
            			smaction.redirectAction( 
    					"Error cloning order - " + e.getMessage(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
						+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
						+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
    					);
					}
				}
				clsDatabaseFunctions.commit_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080506]");
    			smaction.redirectAction( 
					"", 
					"Order #" + entry.getM_sOrderNumber() + " cloned successfully.",
					SMOrderHeader.Paramstrimmedordernumber + "=" + newOrder.getM_strimmedordernumber()
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
    			);
			}
			return;
    	}
    	//If it's a request to invoice:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    		    		SMEditOrderEdit.CREATEINVOICECOMMAND_VALUE) == 0){
    		Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			smaction.getsDBID(), 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".creating invoice - " +
    	    					"user: " + smaction.getUserName() + 
    	    					" [1331729628]");
    		if (conn == null){
    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not get data connection.", 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
    		}
    		if (entry.validate_for_invoicing(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080507]");
				String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMCreateMultipleInvoicesSelection"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y"
				+ "&" + SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y"
				+ "&CallingClass=" + smaction.getCallingClass()
				;
				redirectProcess(sRedirectString, response);
				return;
    		}else{
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080508]");
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
				entry.getErrorMessages(), 
				"", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
				+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
				+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
    		}
    	}
    	//If it's a request to print an installation work order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    		    		SMEditOrderEdit.PRINTITEMIZEDWOCOMMAND_VALUE) == 0){
    		int iNOofCopies = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.NOOFINSTALLATIONWOCOPIES_NAME, request));
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMPrintInstallationTicketGenerate"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + "StartingOrderNumber" + "=" + entry.getM_strimmedordernumber()
			+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=" + iNOofCopies 
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	//If it's a print a service work order:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    	SMEditOrderEdit.PRINTSERVICEWOCOMMAND_VALUE) == 0){
    		int iNOofCopies = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.NOOFSERVICEWOCOPIES_NAME, request));
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMPrintServiceTicketGenerate"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + "StartingOrderNumber" + "=" + entry.getM_strimmedordernumber()
			+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=" + iNOofCopies 
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}

    	//If it's a request to go to the lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
	    	SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    		SMEditOrderEdit.DETAILSCOMMAND_VALUE) == 0){
    		
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to the totals:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    		SMEditOrderEdit.TOTALSCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderTotalsEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to a proposal:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    		SMEditOrderEdit.PROPOSALCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMProposalEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableproposals.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to change the customer:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
		    		SMEditOrderEdit.CHANGECUSTOMERCOMMAND_VALUE) == 0){
    		//First, save the current order header in the session:
    		//smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMChangeCustomerEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMChangeCustomerEdit.STRIMMEDORDERNUMBER_PARAM + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If we are RETURNING from selecting a new customer:
    	if (clsManageRequestParameters.get_Request_Parameter(
			SMChangeCustomerEdit.CHANGECUSTOMER_COMMAND, request).compareToIgnoreCase("") != 0){
    		String sWarning = "";
    		String sStatus = "Successfully changed customer to '" 
    			+ clsManageRequestParameters.get_Request_Parameter(SMChangeCustomerEdit.CUSTOMERNUMBER_PARAM, request) + "'.";
    		//No matter whether we succeed or not, we are going to force the called function to reload the order:
    		smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
    		try {
				entry.changeCustomer(
					sDBID, 
					smaction.getUserID(),
					smaction.getFullUserName(),
					getServletContext(),
					clsManageRequestParameters.get_Request_Parameter(SMChangeCustomerEdit.CUSTOMERNUMBER_PARAM, request),
					clsManageRequestParameters.get_Request_Parameter(SMChangeCustomerEdit.STRIMMEDORDERNUMBER_PARAM, request),
					request.getParameter(SMChangeCustomerEdit.COPYBILLTO_CHECKBOX) != null);
			} catch (Exception e) {
				sStatus = "";
				sWarning = "Could not change customer - " + e.getMessage();
			}
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMChangeCustomerEdit.STRIMMEDORDERNUMBER_PARAM, request)
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&CallingClass=" + smaction.getCallingClass()
			+ "&Warning=" + sWarning
			+ "&Status=" + sStatus
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to save the order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    	    SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    		    SMEditOrderEdit.SAVECOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into save");
    		}
    		Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			smaction.getsDBID(), 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".saving order - " +
    					"user: " + smaction.getUserName() +
    					" [1331729231]");
    		if (conn == null){
    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not get data connection.", 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
    		}
    		//See if the price level or price list code has changed - if so, signal to change the 
    		//unit prices on the items:
    		boolean bRecalculateUnitPrices = false;
    		if (clsManageRequestParameters.get_Request_Parameter(
            	    SMEditOrderEdit.PRICECHANGE_FLAG, request).compareToIgnoreCase(
            		    SMEditOrderEdit.PRICECHANGED_VALUE) == 0){
    			bRecalculateUnitPrices = true;
    		}
    		if (!entry.loadDetailLines(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080509]");
    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not load detail lines - " + clsServletUtilities.URLEncode(entry.getErrorMessages()), 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
    		}
    		boolean breacquireGeocode = clsManageRequestParameters.get_Request_Parameter(
            	    SMEditOrderEdit.SHIPTOCHANGE_FLAG, request).compareToIgnoreCase(
                		    SMEditOrderEdit.SHIPTOCHANGED_VALUE) == 0;
			if(!entry.save_order_without_data_transaction(
				conn,
				sDBID,
				getServletContext(),
				smaction.getUserID(), 
				smaction.getFullUserName(), 
				bRecalculateUnitPrices, 
				breacquireGeocode,
				"UPDATEORDER")) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080510]");
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
	    		if (bDebugMode){
	    			System.out.println("In " + this.toString() + " save failed");
	    		}

				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080511]");
				smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					response.sendRedirect(smaction.getOriginalURL().replace("*", "&")
							+ "&Warning=" + clsServletUtilities.URLEncode(entry.getStatusMessages()));
				}else{
					smaction.redirectAction(
						entry.getStatusMessages(), 
						entry.getObjectName() + ": " + entry.getM_strimmedordernumber() + " was successfully saved.",
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				}
			}
    	}

    	//If Rename Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(SMEditOrderEdit.RENAME_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
    		SMOption opt = new SMOption();
        	try {
				opt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not create or upload to document folder - unable to load system options - " + e1.getMessage(), 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
			}
        	

        	String sRedirectString = "";
        	String sNewFolderName = opt.getgdriveorderfolderprefix() + entry.getM_strimmedordernumber() + opt.getgdriveorderfoldersuffix();
        	//Parameters for rename folder web-app
        	String sFolderID = "";
        	try {
				sFolderID = SMUtilities.getGDriveFolderID(entry.getM_sGDocLink());
			} catch (Exception e1) {
				smaction.getPwOut().println(
					"<BR><FONT COLOR=RED><B>" 
					+ "Error getting GDoc Folder ID for order '" + entry.getM_strimmedordernumber() 
					+ "' - " + e1.getMessage() 
					+ "</B></FONT>");
			}
       		 sRedirectString = opt.getgdriverenamefolderurl()
         		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.newfoldername + "=" + sNewFolderName
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.folderid + "=" + sFolderID
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.RENAMED_ORDER_FOLDER_URL_PARAM_VALUE
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.getM_strimmedordernumber()
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + sGDriveReturnURL
             	;

			smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;

    	}
    	
       	//If Create And Upload Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
   			SMEditOrderEdit.COMMAND_FLAG, request).compareToIgnoreCase(SMEditOrderEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
    		SMOption opt = new SMOption();
        	try {
				opt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not create or rename document folder - unable to load system options - " + e1.getMessage(), 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMEditOrderEdit.SHIPTOCHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.SHIPTOCHANGE_FLAG, request)
					+ "&" + SMEditOrderEdit.PRICECHANGE_FLAG + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.PRICECHANGE_FLAG, request)
				);
				return;
			}

        	String sRedirectString = "";
        	String sNewFolderName = opt.getgdriveorderfolderprefix() + entry.getM_strimmedordernumber() + opt.getgdriveorderfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
       		 sRedirectString = opt.getgdriveuploadfileurl()
         		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + opt.getgdriveorderparentfolderid()
   				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.ORDER_RECORD_TYPE_PARAM_VALUE
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.getM_strimmedordernumber()
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + opt.getBackGroundColor()
         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + sGDriveReturnURL
             	;
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		return;
	}
	

		
		private String getCreateGDriveReturnURL(HttpServletRequest req) throws Exception{
			String sURL = "";
			try {
				ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(getServletContext()) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
				sURL += serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME);			
			}catch(Exception e) {
				throw new Exception("Error [1542655042] "+e.getMessage());
			}
			sURL += "/" + WebContextParameters.getInitWebAppName(getServletContext()) + "/";
			sURL += "smcontrolpanel.SMCreateGDriveFolder";
				
			return sURL;
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
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}