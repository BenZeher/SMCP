package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import sscommon.SSConstants;

public class ASControllerDiagnosticsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASControllerDiagnostics)){return;}
	    //Read the entry fields from the request object:
		SSController entry = new SSController(request);
		
		//System.out.println("[1463764293] - lid = " + entry.getslid());
		String sServerID = SMBackgroundScheduleProcessor.getServerID(getServletContext());
		
	    if(smaction.isDeleteRequested()){
	    	if (request.getParameter(SMMasterEditEntry.CONFIRM_DELETE_CHECKBOX_NAME) != null){
			    //Save this now so it's not lost after the delete:
			    try {
					entry.deleteControllerLog(getServletContext(), smaction.getConfFile(), smaction.getUserName(), smaction.getUserID(), smaction.getFullUserName(), sServerID);
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete controller log: " + clsServletUtilities.URLEncode(e.getMessage()), 
				    		"", 
				    		SMTablesscontrollers.lid + "=" + entry.getslid()
				    		+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
				    			+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
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
						"Controller's diagnostic log was successfully deleted.", 
						SMTablesscontrollers.lid + "=" + entry.getslid()
							+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
				    		+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
	    		//System.out.println("[1463764294] - lid = " + entry.getslid());
				smaction.redirectAction(
					"You chose to delete the log, but did not check the CONFIRM checkbox.", 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
						+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
				    	+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
				);
				return;
	    	}
	    }
	    
	    //If it's a request to turn 'TEST MODE' on of off:
	    String sTestModeLabel = "ON";
	    String sTestModeValue = SSConstants.QUERY_KEYVALUE_TEST_MODE_ON;
	    if (
	    	(request.getParameter(ASControllerDiagnosticsEdit.TURN_TEST_MODE_ON_BUTTON) != null)
	    	|| (request.getParameter(ASControllerDiagnosticsEdit.TURN_TEST_MODE_OFF_BUTTON) != null)
	    ){
	    	if (request.getParameter(ASControllerDiagnosticsEdit.TURN_TEST_MODE_OFF_BUTTON) != null){
	    		sTestModeLabel = "OFF";
	    		sTestModeValue = SSConstants.QUERY_KEYVALUE_TEST_MODE_OFF;
	    	}
	    	//Check the confirming checkbox:
	    	if (request.getParameter(ASControllerDiagnosticsEdit.TEST_MODE_CHANGE_CONFIRM) != null){
	    		try {
					entry.setTestModeState(
						getServletContext(), 
						smaction.getConfFile(), 
						smaction.getUserName(), 
						smaction.getUserID(),
						smaction.getFullUserName(),
						sTestModeValue,
						sServerID);
				} catch (Exception e) {
			    	smaction.redirectAction(
			    		"Could not turn test mode " + sTestModeLabel + ": " + e.getMessage(), 
			    		"", 
			    		SMTablesscontrollers.lid + "=" + entry.getslid()
			    			+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
			    			+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
			    		);
					return;
				}
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"", 
						"Controller's test mode was set to " + sTestModeLabel + ".", 
						SMTablesscontrollers.lid + "=" + entry.getslid()
							+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
	    		//System.out.println("[1463764294] - lid = " + entry.getslid());
				smaction.redirectAction(
					"You chose to turn test mode " + sTestModeLabel + ", but did not check the CONFIRM checkbox.", 
					"", 
					SMTablesscontrollers.lid + "=" + entry.getslid()
						+ "&" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "=" 
				    	+ clsManageRequestParameters.get_Request_Parameter(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME,  request)
				);
				return;
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