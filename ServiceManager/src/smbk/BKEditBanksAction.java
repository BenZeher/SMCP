package smbk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class BKEditBanksAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.BKEditBanks)){return;}
	    //Read the entry fields from the request object:
		BKBank entry = new BKBank(request);
		//smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sShortName = entry.getsshortname();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		BKBank.Paramlid + "=" + entry.getslid()
				    		);
						return;
				}

		    	//If the delete succeeded, the entry will be initialized:
		    	//Re-set the job number in the new, blank entry:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + sShortName + " was successfully deleted.", 
						""
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					BKBank.Paramlid + "=" + entry.getslid()
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					BKBank.Paramlid + "=" + entry.getslid()
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getsshortname() + " was successfully saved.",
					BKBank.Paramlid + "=" + entry.getslid()
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