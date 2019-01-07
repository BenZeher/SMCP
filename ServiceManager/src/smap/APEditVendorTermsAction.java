package smap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendorTerms;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class APEditVendorTermsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APEditVendorTerms)){return;}
	    //Read the entry fields from the request object:
		APVendorTerms entry = new APVendorTerms(request);
		smaction.getCurrentSession().removeAttribute(APVendorTerms.ParamObjectName);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sTermsCode = entry.getsTermsCode();
			    if (!entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			    	smaction.redirectAction(
			    			"Could not delete: " + entry.getErrorMessages(), 
			    			"", 
			    			APVendorTerms.ParamsTermsCode + "=" + entry.getsTermsCode()
			    			);
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(APVendorTerms.ParamObjectName, entry);
						smaction.redirectAction(
							"", 
							entry.getObjectName() 
							+ ": " + sTermsCode + " was successfully deleted.", "");
					}
					return;
			    }
	    	}else{
	    		smaction.getCurrentSession().setAttribute(APVendorTerms.ParamObjectName, entry);
		    	smaction.redirectAction(
		    			"You chose to delete, but did not check the 'Confirm' checkbox.", 
		    			"", 
		    			APVendorTerms.ParamsTermsCode + "=" + entry.getsTermsCode()
		    			);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					)){
				smaction.getCurrentSession().setAttribute(APVendorTerms.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					APVendorTerms.ParamsTermsCode + "=" + entry.getsTermsCode()
					);
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
						entry.getObjectName() + ": " + entry.getsTermsCode() + " was successfully saved.",
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