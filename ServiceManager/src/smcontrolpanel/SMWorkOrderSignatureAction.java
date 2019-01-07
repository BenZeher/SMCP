package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMWorkOrderSignatureAction extends HttpServlet{
	public static final String WORK_ORDER_EMAIL_SUBJECT = "Work Order Receipt";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditWorkOrders, request)){return;}
		SMWorkOrderHeader workorder;
		workorder = new SMWorkOrderHeader();
		try {
			workorder.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
			smaction.redirectAction(
					"Error [1430250331] updating reading request information: " + e2.getMessage(), 
					"", 
					""
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
		
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderSignatureEdit.COMMAND_FLAG, request);
	    
    	//If it's a request to save the work order:
    	if (sCommandValue.compareToIgnoreCase(
    			SMWorkOrderSignatureEdit.SAVECOMMAND_VALUE) == 0){
       		try {
    				workorder.saveFromAcceptanceScreen(
    					getServletContext(), 
    					smaction.getsDBID(), 
    					smaction.getUserID(),
    					smaction.getFullUserName()
    				);
    			} catch (Exception e) {
    				try {
						smaction.getCurrentSession().setAttribute(SMTableworkorders.ObjectName, workorder);
					} catch (Exception e1) {
						System.out.println("Error [1435068903] - getting current session - " + e1.getMessage());
						smaction.redirectAction(
	    						"Could not save: " + e1.getMessage(), 
	    						"", 
	    						SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
	    						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
	    						+ "&" + SMWorkOrderEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.RECORDWASCHANGED_FLAG, request)
	    				);
	    				return;
					}
    				smaction.redirectAction(
    						"Could not save: " + e.getMessage(), 
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

    	//If it's a request to go to 'edit' mode:
    	if (sCommandValue.compareToIgnoreCase(
    			SMWorkOrderSignatureEdit.EDITCOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into edit mode");
    			System.out.println(workorder.read_out_debug_data());
    		}
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMWorkOrderEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMWorkOrderHeader.Paramlid + "=" + workorder.getlid()
				+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
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

