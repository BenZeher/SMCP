package smcontrolpanel;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMClasses.SMDoingbusinessasaddress;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;

public class SMEditDoingBusinessAsAddressAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditDoingBusinessAsAddresses)){return;}
	    //Read the entry fields from the request object:
		SMDoingbusinessasaddress entry = new SMDoingbusinessasaddress(request);
		
		//Get the command value from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMEditDoingBusinessAsAddressEdit.COMMAND_FLAG, request);
		
		
	    if(sCommandValue.compareToIgnoreCase(SMEditDoingBusinessAsAddressEdit.DELETE_COMMAND_VALUE) == 0){
		    if (clsManageRequestParameters.get_Request_Parameter(SMEditDoingBusinessAsAddressEdit.CONFIRM_DELETE_CHECKBOX, request)
			    	.compareToIgnoreCase(SMEditDoingBusinessAsAddressEdit.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.getslid();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
			    			clsServletUtilities.URLEncode("Could not delete address '" + sID + " - " + e.getMessage()), 
			    		"", 
			    		SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
			    		);
					return;
				}

			    //If the delete succeeded, the entry will be initialized:
		    	//Re-set the id number in the new, blank entry:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.getCurrentSession().setAttribute(SMDoingbusinessasaddress.ParamObjectName, entry);
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + sID + " was successfully deleted.", 
						""
					);
				}
				return;
	    	}else{
	    		smaction.redirectAction(
	    				clsServletUtilities.URLEncode("You chose to delete without checking the confirm before deleting checkbox."), 
						"", 
						SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
						);
				return;
	    	}
	    }
		//If it's an edit, process that:
	    if(sCommandValue.compareToIgnoreCase(SMEditDoingBusinessAsAddressEdit.SAVE_COMMAND_VALUE) == 0){
			try{
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName());
			
				smaction.redirectAction(
						"", 
						clsServletUtilities.URLEncode(entry.getObjectName() + ": " + entry.getslid() + " was successfully saved."), 
						SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
						);
				return;
			} catch (Exception e){
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.redirectAction(
						clsServletUtilities.URLEncode("Error could not save: " + e.getMessage()), 
						"",
						SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
						+ "&" + SMDoingbusinessasaddress.ParamNewRecord + "=" + entry.getsNewRecord()
						);
			}
	    }
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}