package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ASConfigureControllerAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String OBJECT_NAME = SSController.ParamObjectName + " configuration";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASConfigureControllers)){return;}
	    //Read the entry fields from the request object:
		SSController entry = new SSController(request);
		
		//Get the server ID:
		String sServerID = SMBackgroundScheduleProcessor.getServerID(getServletContext());
		
		//If it's a request to update the controller software, catch that here:
		if (clsManageRequestParameters.get_Request_Parameter(ASConfigureControllerEdit.UPDATE_CONTROLLER_SOFTWARE_BUTTON_NAME, request).compareToIgnoreCase("") != 0){
			//Check the confirmation:
			if (request.getParameter(ASConfigureControllerEdit.UPDATE_CONTROLLER_SOFTWARE_CONFIRM_NAME) == null){
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to update the controller software but did not click the 'Confirm' button", 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
				return;
			}
			//Now try to update the controller's software:
	    	try {
				entry.updateControllerVersion(
					getServletContext(), 
					smaction.getConfFile(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					sServerID
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not update controller software: " + e.getMessage(), 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
				return;
			}
			//If the configure succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.setCallingClass("smas.ASConfigureControllerSelect");
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getscontrollername() + " is being updated - please wait 60 seconds then refresh.",
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
			}
		}
		
		//If it's a request to restart the controller, catch that here:
		if (clsManageRequestParameters.get_Request_Parameter(ASConfigureControllerEdit.RESTART_CONTROLLER_BUTTON_NAME, request).compareToIgnoreCase("") != 0){
			//Check the confirmation:
			if (request.getParameter(ASConfigureControllerEdit.RESTART_CONTROLLER_CONFIRM_NAME) == null){
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to restart the controller but did not click the 'Confirm' button", 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
				return;
			}
			//Now try to restart the controller's software:
	    	try {
				entry.restartController(
					getServletContext(), 
					smaction.getConfFile(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					sServerID
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not restart controller: " + e.getMessage(), 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
				return;
			}
			//If the configure succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.setCallingClass("smas.ASConfigureControllerSelect");
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getscontrollername() + " is being updated - please wait 60 seconds then refresh.",
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
			}
		}
		
	    if(smaction.isEditRequested()){
	    	try {
				entry.configure_without_data_transaction(
					getServletContext(), 
					smaction.getConfFile(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					sServerID
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not configure controller: " + e.getMessage(), 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
				);
				return;
			}
			//If the configure succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getscontrollername() + " was successfully configured.",
					SMTablesscontrollers.lid + "=" + entry.getslid()
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