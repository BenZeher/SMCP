package smic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ICEditPhysicalCountLineAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){return;}
	    //Read the entry fields from the request object:
		ICPhysicalCountLineEntry entry = new ICPhysicalCountLineEntry(request);
		smaction.getCurrentSession().removeAttribute(ICPhysicalCountLineEntry.ParamObjectName);
		String sPhysicalInventoryID = entry.getPhysicalInventoryID();
		String sCountID = entry.getCountID();
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.slid();
			    if (!entry.delete(getServletContext(), 
			    		smaction.getsDBID(), 
			    		smaction.getUserID(),
			    		smaction.getFullUserName())){
			    	smaction.getCurrentSession().setAttribute(ICPhysicalCountLineEntry.ParamObjectName, entry);
			    	smaction.redirectAction("Could not delete: " + entry.getErrorMessages(), "", "");
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
			    	entry.setsPhysicalInventoryID(sPhysicalInventoryID);
			    	entry.setsCountID(sCountID);
			    	smaction.getCurrentSession().setAttribute(ICPhysicalCountLineEntry.ParamObjectName, entry);
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(ICPhysicalCountLineEntry.ParamObjectName, entry);
						smaction.redirectAction("", "Entry ID: " + sID + " was successfully deleted.", "");
					}
					return;
			    }
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	boolean bAddNewItemsToPhysicalInventory = (clsManageRequestParameters.get_Request_Parameter(
	    		ICEditPhysicalCountLine.PARAM_INCLUDE_NEW_ITEMS, request).compareToIgnoreCase("") != 0);
	    	
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					bAddNewItemsToPhysicalInventory
				)
			){
				smaction.getCurrentSession().setAttribute(ICPhysicalCountLineEntry.ParamObjectName, entry);
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
						"Physical Count Line ID: " + entry.slid() + " was successfully saved.",
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