package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMClasses.SMDeliveryTicket;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMDeliveryTicketSignatureAction extends HttpServlet{
	public static final String DELIVERY_TICKET_EMAIL_SUBJECT = "Delivery Ticket Receipt";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMManageDeliveryTickets, request)){return;}
		SMDeliveryTicket deliveryticket = new SMDeliveryTicket() ;
		try {
			deliveryticket = new SMDeliveryTicket(request);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, deliveryticket);
			smaction.redirectAction(
					"Error [1445265033] updating reading request information: " + e2.getMessage(), 
					"", 
					""
			);
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " loadFromHTTPRequest failed");
    		}
			return;
		}
		
		try {
			smaction.getCurrentSession().removeAttribute(SMDeliveryTicket.ParamObjectName);
		} catch (Exception e2) {
			clsServletUtilities.sysprint(this.toString(), smaction.getUserName(), "Error [1445265034]  - " + e2.getMessage() + ".");
		}
		
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicketSignatureEdit.COMMAND_FLAG, request);
	    
    	//If it's a request to save the delivery ticket:
    	if (sCommandValue.compareToIgnoreCase(
    			SMDeliveryTicketSignatureEdit.SAVECOMMAND_VALUE) == 0){
       		try {
				deliveryticket.saveFromAcceptanceScreen(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName()
				);
			} catch (Exception e) {
				try {
					smaction.getCurrentSession().setAttribute(SMDeliveryTicket.ParamObjectName, deliveryticket);
				} catch (Exception e1) {
					System.out.println("Error [1445265035] - getting current session - " + e1.getMessage());
					smaction.redirectAction(
						"Could not save: " + e1.getMessage(), 
						"", 
						SMDeliveryTicket.Paramlid + "=" + deliveryticket.getslid()
						+ "&" + SMDeliveryTicketSignatureEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicketSignatureEdit.RECORDWASCHANGED_FLAG, request)
    				);
    				return;
				}
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMDeliveryTicket.Paramlid + "=" + deliveryticket.getslid()
					+ "&" + SMDeliveryTicketSignatureEdit.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMDeliveryTicketSignatureEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					SMDeliveryTicket.ParamObjectName + " was successfully saved.",
					SMDeliveryTicket.Paramlid + "=" + deliveryticket.getslid()
				);
			}
    	}

    	//If it's a request to edit:
    	if (sCommandValue.compareToIgnoreCase(
    			SMDeliveryTicketSignatureEdit.EDITCOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into edit mode");
    			//System.out.println(deliveryticket.read_out_debug_data());
    		}
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDeliveryTicketEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMDeliveryTicket.Paramlid + "=" + deliveryticket.getslid()
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

