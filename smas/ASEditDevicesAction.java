package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import SMDataDefinition.SMTablessdevices;

public class ASEditDevicesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String OBJECT_NAME = SSDevice.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASEditDevices)){return;}
	    //Read the entry fields from the request object:
		SSDevice entry = new SSDevice(request);
		//smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sDevice = entry.getsdescription();
			    try {
					entry.delete(getServletContext(), smaction.getConfFile(), 
							smaction.getUserName(),
							smaction.getUserID(),
							smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMTablessdevices.lid + "=" + entry.getslid()
				    		);
						return;
				}

		    	//If the delete succeeded, the entry will be initialized:
		    	//Re-set the id in the new, blank entry:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + sDevice + " was successfully deleted.", 
						""
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMTablessdevices.lid + "=" + entry.getslid()
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getConfFile(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMTablessdevices.lid + "=" + entry.getslid()
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	smaction.getCurrentSession().removeAttribute(OBJECT_NAME);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getsdescription() + " was successfully saved.",
					SMTablessdevices.lid + "=" + entry.getslid()
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