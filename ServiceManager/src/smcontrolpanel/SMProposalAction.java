package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsManageRequestParameters;

public class SMProposalAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditProposals)){return;}
		//System.out.println("CallingClass2 = " + smaction.getCallingClass());
	    //Read the entry fields from the request object:
		SMProposal proposal;
		proposal = new SMProposal("");
		proposal.loadFromHTTPRequest(request);
		
		smaction.getCurrentSession().removeAttribute(SMTableproposals.ObjectName);
		String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		
    	//If it's a request to print:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMProposalEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMProposalEdit.PRINTCOMMAND_VALUE) == 0){
    		//int iNOofCopies = Integer.parseInt(SMUtilities.get_Request_Parameter(SMProposalEdit.NOOFCOPIES_NAME, request));
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMProposalPrintSelection"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMProposal.Paramstrimmedordernumber + "=" + proposal.getstrimmedordernumber()
			//+ "&" + SMProposalEdit.NOOFCOPIES_NAME + "=" + iNOofCopies 
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	//If it's a request to go to the order header:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMProposalEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMProposalEdit.HEADERCOMMAND_VALUE) == 0){
    		
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + proposal.getstrimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to the order details:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMProposalEdit.COMMAND_FLAG, request).compareToIgnoreCase(
    					SMProposalEdit.DETAILCOMMAND_VALUE) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + proposal.getstrimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to save:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMProposalEdit.COMMAND_FLAG, request).compareToIgnoreCase(SMProposalEdit.SAVECOMMAND_VALUE) == 0){
    		String sOtherParameters = SMProposal.Paramstrimmedordernumber + "=" + proposal.getstrimmedordernumber()
					+ "&CallingClass=smcontrolpanel.SMEditOrderEdit";
    		try {
				proposal.save(
					smaction.getUserName(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					getServletContext(), 
					sDBID,
					(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
				smaction.redirectAction(
						"", 
						"Proposal: " + proposal.getstrimmedordernumber() + " was successfully saved.",
						sOtherParameters
					);
			} catch (Exception e) {
				//System.out.println("[1372876326] Error saving proposal: " + e.getMessage());
				try {
					smaction.getCurrentSession().setAttribute(SMTableproposals.ObjectName, proposal);
				} catch (Exception e1) {
					System.out.println("[1422274545] Error saving proposal: " + e1.getMessage());
					smaction.redirectAction(
							"Error [1422274546], Could not save proposal - could not set attribute session - " + e.getMessage(), 
							"",
							sOtherParameters
						);
				}
				smaction.redirectAction(
						"Could not save proposal - " + e.getMessage(), 
						"",
						sOtherParameters
					);
			}
    	}

    	//If it's a request to delete:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMProposalEdit.COMMAND_FLAG, request).compareToIgnoreCase(SMProposalEdit.DELETECOMMAND_VALUE) == 0){
    		String sOtherParameters = SMProposal.Paramstrimmedordernumber + "=" + proposal.getstrimmedordernumber()
					+ "&CallingClass=smcontrolpanel.SMEditOrderEdit";
    		try {
				proposal.delete(proposal.getstrimmedordernumber(), sDBID, getServletContext(), smaction.getUserName());
				smaction.redirectAction(
						"", 
						"Proposal: " + proposal.getstrimmedordernumber() + " was successfully deleted.",
						sOtherParameters
					);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMTableproposals.ObjectName, proposal);
				smaction.redirectAction(
						"Could not delete proposal - " + e.getMessage(), 
						"",
						sOtherParameters
					);
			}
    	}
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
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}