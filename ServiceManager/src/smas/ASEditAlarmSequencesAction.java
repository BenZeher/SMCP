package smas;

import java.io.IOException;
import java.sql.Connection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessalarmsequences;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASEditAlarmSequencesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String OBJECT_NAME = SSAlarmSequence.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASEditAlarmSequences)){return;}
	    //Read the entry fields from the request object:
		SSAlarmSequence entry = new SSAlarmSequence(request);
		
		//****************************************************************************************
		//Handle trigger devices:
		//Load the trigger devices here, since the SSAlarmSequence can't load them itself:
		try {
			loadTriggerDevices(entry, smaction.getsDBID(), smaction.getUserName());
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
			smaction.redirectAction(
				"Could not load trigger devices: " + e1.getMessage(), 
				"", 
				SMTablessalarmsequences.lid + "=" + entry.getslid()
			);
			return;
		}
		
		//Load activation devices:
		try {
			loadActivationDevices(entry, smaction.getsDBID(), smaction.getUserName());
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
			smaction.redirectAction(
				"Could not load activation devices: " + e1.getMessage(), 
				"", 
				SMTablessalarmsequences.lid + "=" + entry.getslid()
			);
			return;
		}
		
		//If the user chose to REMOVE a trigger device, then do that here:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sRemoveTriggerDeviceMarker = SSAlarmSequence.PARAM_REMOVE_TRIGGER_DEVICE_ID_PREFIX;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
		  if (sParamName.contains(sRemoveTriggerDeviceMarker)){
			  String sRemoveTriggerDeviceID = (sParamName.substring(sParamName.indexOf(sRemoveTriggerDeviceMarker) + sRemoveTriggerDeviceMarker.length()));
			  entry.removeTriggerDevice(sRemoveTriggerDeviceID);
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
						SMTablessalarmsequences.lid + "=" + entry.getslid()
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
						SMTablessalarmsequences.lid + "=" + entry.getslid()
					);
				}
				return;
		  }
	    }
	    //*****************************************************************************************************
	    
	    //*****************************************************************************************************
	    //Handle activation devices here:
		//If the user chose to REMOVE an activation device, then do that here:
		paramNames = request.getParameterNames();
	    String sRemoveActivationDeviceMarker = SSAlarmSequence.PARAM_REMOVE_ACTIVATION_DEVICE_ID_PREFIX;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
		  if (sParamName.contains(sRemoveActivationDeviceMarker)){
			  String sRemoveActivationDeviceID = (sParamName.substring(sParamName.indexOf(sRemoveActivationDeviceMarker) + sRemoveActivationDeviceMarker.length()));
			  entry.removeActivationDevice(sRemoveActivationDeviceID);
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
						SMTablessalarmsequences.lid + "=" + entry.getslid()
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
						SMTablessalarmsequences.lid + "=" + entry.getslid()
					);
				}
				return;
		  }
	    }
	    
	    //*******************************************************************************************************************
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sName = entry.getsname();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMTablessalarmsequences.lid + "=" + entry.getslid()
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
						""
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMTablessalarmsequences.lid + "=" + entry.getslid()
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	//Add any new devices to the entry:
			//Check to see if we are adding a trigger device:
			String sNewDeviceID = clsManageRequestParameters.get_Request_Parameter(SSAlarmSequence.PARAM_NEW_TRIGGER_DEVICE_LIST, request);
			
			if ((sNewDeviceID).compareToIgnoreCase("") != 0){
				try {
					SSAlarmSequence.addTriggerDevice(sNewDeviceID, 
													 smaction.getsDBID(), 
													 smaction.getUserName(), 
													 smaction.getUserID(),
													 smaction.getFullUserName(),
													 getServletContext());
				} catch (Exception e) {
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"Could not add device ID '" + sNewDeviceID + "' : " + e.getMessage(), 
						"", 
						SMTablessalarmsequences.lid + "=" + entry.getslid()
					);
					return;
				}
			}
			//Check to see if we are adding an activation device:
			sNewDeviceID = clsManageRequestParameters.get_Request_Parameter(SSAlarmSequence.PARAM_NEW_ACTIVATION_DEVICE_LIST, request);
			
			if ((sNewDeviceID).compareToIgnoreCase("") != 0){
				String sActivationDurationInSeconds = clsManageRequestParameters.get_Request_Parameter(SSAlarmSequence.PARAM_ACTIVATION_DURATION_IN_SECONDS, request);
				try {
					if(Integer.parseInt(sActivationDurationInSeconds) < 1) {
						//smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
						smaction.redirectAction(
							"Duration must be grater that 0 for device ID '" + sNewDeviceID + "'", 
							"", 
							SMTablessalarmsequences.lid + "=" + entry.getslid()
						);
						return;
					}
				}catch(Exception e) {
					//smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"Duration must be an integer greater than 0 for device ID '" + sNewDeviceID + "'", 
						"", 
						SMTablessalarmsequences.lid + "=" + entry.getslid()
					);
					return;
				}
				try {
					SSAlarmSequence.addActivationDevice(sNewDeviceID, 
							sActivationDurationInSeconds, 
							smaction.getsDBID(), 
							smaction.getUserName(), 
							smaction.getUserID(),
							smaction.getFullUserName(),
							getServletContext());
				} catch (Exception e) {
					smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
					smaction.redirectAction(
						"Could not add device ID '" + sNewDeviceID + "' : " + e.getMessage(), 
						"", 
						SMTablessalarmsequences.lid + "=" + entry.getslid()
					);
					return;
				}
			}
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
					SMTablessalarmsequences.lid + "=" + entry.getslid()
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
					SMTablessalarmsequences.lid + "=" + entry.getslid()
				);
			}
	    }
		return;
	}
	private void loadTriggerDevices(SSAlarmSequence entry, String sConf, String sUser) throws Exception{

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".loadTriggerDevices - user: " + sUser));
		} catch (Exception e1) {
			throw new Exception("Error [1460505993] getting connection - " + e1.getMessage());
		}
		
		try {
			entry.loadTriggerDevices(conn);
		} catch (Exception e) {
			throw new Exception("Error [1460505994] - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	}
	private void loadActivationDevices(SSAlarmSequence entry, String sConf, String sUser) throws Exception{

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".loadActivationDevices - user: " + sUser));
		} catch (Exception e1) {
			throw new Exception("Error [1462320917] getting connection - " + e1.getMessage());
		}
		
		try {
			entry.loadActivationDevices(conn);
		} catch (Exception e) {
			throw new Exception("Error [1462320918] - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}