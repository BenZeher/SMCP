package smas;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import SMDataDefinition.SMTablesseventschedules;
import ServletUtilities.clsManageRequestParameters;

public class ASEditEventSchedulesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSEventSchedule.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASEditEventSchedules)){return;}
	    //Read the entry fields from the request object:
		SSEventSchedule entry = new SSEventSchedule(request);
		String sAddingNewEntryValue = clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request);
		
		//Make sure we remove and object in the current session:
	    if (smaction.getCurrentSession().getAttribute(OBJECT_NAME) != null){
	    	smaction.getCurrentSession().removeAttribute(OBJECT_NAME);
	    }
		
		//****************************************************************************************
		//Load the devices/alarm sequences here, since the SSEventSchedule can't load them itself:
		try {
			entry.loadEventScheduleDevicesAndSequences(
				getServletContext(), 
				smaction.getsDBID(),
				smaction.getUserName()
			);
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
			smaction.redirectAction(
				"Could not load devices/alarm sequences: " + e1.getMessage(), 
				"", 
				SMTablesseventschedules.lid + "=" + entry.getslid()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
			);
			return;
		}
		
		//If the user chose to REMOVE a trigger device, then do that here:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sRemoveDeviceOrAlarmSequenceMarker = SSEventSchedule.PARAM_REMOVE_EVENT_DETAIL_ID_PREFIX;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
		  if (sParamName.contains(sRemoveDeviceOrAlarmSequenceMarker)){
			  String sRemoveDetailID = (sParamName.substring(sParamName.indexOf(sRemoveDeviceOrAlarmSequenceMarker) + sRemoveDeviceOrAlarmSequenceMarker.length()));
		    	try {
				  entry.removeEventScheduleDetail(
					sRemoveDetailID, 
					getServletContext(), 
					smaction.getsDBID(),
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					);
				} catch (Exception e) {
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"Could not remove detail: " + e.getMessage(), 
						"", 
						SMTablesseventschedules.lid + "=" + entry.getslid()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
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
						entry.getObjectName() + ": " + entry.getsname() + " was successfully saved.",
						SMTablesseventschedules.lid + "=" + entry.getslid()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
					);
				}
				return;
		  }
	    }

	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sName = entry.getsname();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserName(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMTablesseventschedules.lid + "=" + entry.getslid()
				    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
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
						entry.getObjectName() + ": " + sName + " was successfully deleted.", 
						"&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMTablesseventschedules.lid + "=" + entry.getslid()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	//Add any new devices/sequences to the entry:
			//Check to see if we are adding a trigger device:
			String sNewDeviceOrAlarmSequenceID = clsManageRequestParameters.get_Request_Parameter(SSEventSchedule.PARAM_ELIGIBLE_DEVICEORALARMSEQUENCE_LIST, request);
			String sResetDelay = clsManageRequestParameters.get_Request_Parameter(SSEventSchedule.PARAM_EVENT_DETAIL_RESET_DELAY_IN_MINUTES, request);
			if ((sNewDeviceOrAlarmSequenceID).compareToIgnoreCase("") != 0){
				sNewDeviceOrAlarmSequenceID = sNewDeviceOrAlarmSequenceID.substring(1, sNewDeviceOrAlarmSequenceID.length());
				String sDeviceOrAlarmSequence = clsManageRequestParameters.get_Request_Parameter(
					SSEventSchedule.PARAM_ELIGIBLE_DEVICEORALARMSEQUENCE_LIST, request).substring(0, 1);
				try {
					entry.addEventScheduleDetail(
						entry.getslid(), 
						sNewDeviceOrAlarmSequenceID, 
						"0", 
						sDeviceOrAlarmSequence, 
						sResetDelay);
				} catch (Exception e) {
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"Could not add device/alarm sequence ID '" + sNewDeviceOrAlarmSequenceID + "' : " + e.getMessage(), 
						"", 
						SMTablesseventschedules.lid + "=" + entry.getslid()
					);
					return;
				}
			}
			
			//for (int i = 0; i < entry.getEventScheduleDetails().size(); i++){
			//	System.out.println("[1484792671] i = " + i + ", ID = " + entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid());
			//}
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMTablesseventschedules.lid + "=" + entry.getslid()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue 
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
					entry.getObjectName() + ": " + entry.getsname() + " was successfully saved.",
					SMTablesseventschedules.lid + "=" + entry.getslid()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" + sAddingNewEntryValue
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