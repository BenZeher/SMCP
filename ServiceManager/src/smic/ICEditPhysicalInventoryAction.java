package smic;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ICEditPhysicalInventoryAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){return;}
	    //Read the entry fields from the request object:
		ICPhysicalInventoryEntry entry = new ICPhysicalInventoryEntry(request);
		smaction.getCurrentSession().setAttribute(ICPhysicalInventoryEntry.ParamObjectName, entry);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.slid();
			    if (!entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			    	System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
			    	smaction.redirectAction("Could not delete: " + entry.getErrorMessages(), "", "");
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(ICPhysicalInventoryEntry.ParamObjectName, entry);
						smaction.redirectAction("", "Entry ID: " + sID + " was successfully deleted.", "");
					}
					return;
			    }
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
			if(!entry.save_with_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName())){
				smaction.getCurrentSession().setAttribute(ICPhysicalInventoryEntry.ParamObjectName, entry);
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
						"Physical Inventory ID: " + entry.slid() + " was successfully saved.",
						entry.getQueryString()
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