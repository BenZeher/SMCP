package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditOrderTotalsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){return;}
	    //Read the entry fields from the request object:
		SMOrderHeader entry = new SMOrderHeader(request);
		//smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
		smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
		
		String sUserID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);;
    	//If it's a request to clone:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    		    		SMEditOrderTotalsEdit.CLONEORDERCOMMAND_VALUE) == 0){
	    		Connection conn = clsDatabaseFunctions.getConnection(
    	    			getServletContext(), 
    	    			smaction.getsDBID(), 
    	    			"MySQL", 
    	    			this.toString() + ".CLONEORDER - user: " + smaction.getUserID()
    	    			+ " - "
    	    			+ smaction.getFullUserName()
	    				);
    	    		if (conn == null){
    	    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
    					smaction.redirectAction(
    							"Could not get data connection.", 
    							"", 
    							SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
    							+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
    								+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
    					);
    					return;
    	    		}
    	    		
    	    		//Since the screen this was called from doesn't have all the order data on it, we need to load the order itself first:
    	    		if(!entry.load(conn)){
	       				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080522]");
            			smaction.redirectAction( 
    					"Error cloning order: " + entry.getErrorMessages(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber());
            			return;
    	    		}
    	    		
    				boolean bCloneDetails;
    				if (request.getParameter("CLONEDETAILS") != null){
    					bCloneDetails = true;
    				}else{
    					bCloneDetails = false;
    				}
    				//get a cloned order.
    				SMOrderHeader newOrder = null;
    				Date datNow = null;

					try {
						clsDBServerTime st = new clsDBServerTime(conn);
						datNow = st.getCurrentTimeAsJavaSQLDate();
					} catch (Exception e1) {
						System.out.println("[1494256591] - " + e1.getMessage());
	       				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080523]");
            			smaction.redirectAction( 
    					"Error cloning order: " + e1.getMessage(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber());
            			return;
					}
					
    				try {
						newOrder = entry.Clone(bCloneDetails,
											   datNow,
											   sUserID, 
											   sUserFullName,
											   conn);
					} catch (Exception e) {
						//System.out.println("[1494256592] - " + e.getMessage());
        				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080524]");
            			smaction.redirectAction( 
    					"Error cloning order: " + e.getMessage(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber());
            			return;
					}
    				//Save cloned order immediately.
    				clsDatabaseFunctions.start_data_transaction(conn);
        			//save order directly without any other actions
        			if (!newOrder.save_cloned_order(conn, 
    									     smaction.getUserID(), 
    									     smaction.getFullUserName(), 
    									     "Clone_Order")){
        				System.out.println("[1494256593] - " + newOrder.getErrorMessages());
        				clsDatabaseFunctions.rollback_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080525]");
            			smaction.redirectAction( 
    					"Error cloning order: " + newOrder.getErrorMessages(), 
    					"",
    					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
    					);
        				
        			}else{
        				clsDatabaseFunctions.commit_data_transaction(conn);
        				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080526]");
            			smaction.redirectAction( 
            					"", 
            					"Order #" + entry.getM_sOrderNumber() + " cloned successfully.",
            					SMOrderHeader.Paramstrimmedordernumber + "=" + newOrder.getM_strimmedordernumber()
            			);
        			}
					return;
    	}
    	//If it's a request to invoice:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.CREATEINVOICECOMMAND_VALUE) == 0){
    		Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			smaction.getsDBID(), 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".invoicing - user: " + smaction.getUserName()));
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
				);
				return;
    		}
    		if (entry.validate_for_invoicing(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080527]");
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
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080528]");
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
				entry.getErrorMessages(), 
				"", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
    		}
    	}
    	//If it's a request to print a work order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.PRINTITEMIZEDWOCOMMAND_VALUE) == 0){
    		int iNOofCopies = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(SMEditOrderTotalsEdit.NOOFINSTALLATIONWOCOPIES_NAME, request));
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMPrintInstallationTicketGenerate"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + "StartingOrderNumber" + "=" + entry.getM_strimmedordernumber()
			+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=" + Integer.toString(iNOofCopies) 
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	//If it's a request to print a work order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.PRINTSERVICEWOCOMMAND_VALUE) == 0){
    		int iNOofCopies = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(SMEditOrderTotalsEdit.NOOFSERVICEWOCOPIES_NAME, request));
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMPrintServiceTicketGenerate"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + "StartingOrderNumber" + "=" + entry.getM_strimmedordernumber()
			+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=" + Integer.toString(iNOofCopies) 
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	//If it's a request to go to the lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.DETAILSCOMMAND_VALUE) == 0){
    		
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to the header:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.HEADERCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to save:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditOrderTotalsEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMEditOrderTotalsEdit.SAVECOMMAND_VALUE) == 0){
			if(!entry.save_order_totals_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME))
					) {
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not save: " + entry.getErrorMessages(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						+ "&" + SMEditOrderEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getM_strimmedordernumber() + " was successfully saved.",
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				}
			}
		}
		return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("[1579270886] In " + this.toString() + ".redirectAction - error redirecting with string: "
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