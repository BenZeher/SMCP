package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMCriticalDateAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditCriticalDate)){return;}
	    //Read the entry fields from the request object:
		SMCriticalDateEntry entry = new SMCriticalDateEntry(request);
		smaction.getCurrentSession().setAttribute(SMCriticalDateEntry.ParamObjectName, entry);
		
		//Get the command value from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEdit.COMMAND_FLAG, request);
				
	    if(sCommandValue.compareToIgnoreCase(SMCriticalDateEdit.DELETE_COMMAND_VALUE) == 0){
		    if (clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEdit.CONFIRM_DELETE_CHECKBOX, request)
			    	.compareToIgnoreCase(SMCriticalDateEdit.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.getid();
			    if (!entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			    	//System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
			    	smaction.redirectAction("Could not delete: " + entry.getErrorMessages(), "", "");
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(SMCriticalDateEntry.ParamObjectName, entry);
						smaction.redirectAction("", "Entry ID: " + sID + " was successfully deleted.", "");
					}
					return;
			    }
	    	}
	    }

		//If it's an edit, process that:
	    if(sCommandValue.compareToIgnoreCase(SMCriticalDateEdit.SAVE_COMMAND_VALUE) == 0){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName())){
				try {
					smaction.getCurrentSession().setAttribute(SMCriticalDateEntry.ParamObjectName, entry);
				} catch (Exception e) {
					System.out.println("ERROR [1437164079] With Current Session");
				}
				smaction.redirectAction("Could not save: " + entry.getErrorMessages(), "", "");
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:

				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						"Entry ID: " + entry.getid() + " was successfully saved.",
						clsServletUtilities.URLEncode(entry.getQueryString())
					);
				}
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