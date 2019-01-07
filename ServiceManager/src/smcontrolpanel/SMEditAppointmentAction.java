package smcontrolpanel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMAppointment;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;

public class SMEditAppointmentAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMViewAppointmentCalendar)){return;}
	    //Read the entry fields from the request object:
		SMAppointment entry = new SMAppointment(request);
		smaction.getCurrentSession().removeAttribute(SMAppointment.ParamObjectName);
		
		//Get the original appointment calendar report selection information if is in the request.
		String sCalendarNavigationStringParams = "";
		
		sCalendarNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM 
				+ "=" + SMViewAppointmentCalendarSelection.DATE_RANGE_CHOOSE;
		
		if (clsManageRequestParameters.get_Request_Parameter(
				SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, request) != null){
			sCalendarNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, request);
			}
		if (clsManageRequestParameters.get_Request_Parameter(
				SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, request) != null){
			sCalendarNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, request);
			} 
		
		boolean bAllowCalendarEditing = request.getParameter(
        		SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER) != null;
		
		if (bAllowCalendarEditing){
			sCalendarNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER + "=Y"; 
		}
		
		ArrayList<String> aUserNames = new ArrayList<String>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()){
			String paramName = (String) parameterNames.nextElement();
			 //String sParamValue = SMUtilities.get_Request_Parameter(paramName, request);
			 if(paramName.contains(SMViewAppointmentCalendarSelection.USER_PREFIX)){
				 aUserNames.add(paramName.substring(SMViewAppointmentCalendarSelection.USER_PREFIX.length()));
			 }
		}

		for(int i = 0; aUserNames.size() > i; i++){
			sCalendarNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.USER_PREFIX + aUserNames.get(i);
		}
		
		//If it is a request to delete the entry
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		"COMMANDFLAG", request).compareToIgnoreCase(SMEditAppointmentEdit.COMMAND_VALUE_DELETE) == 0){
		 
			    //Save this now so it's not lost after the delete:
			    String sEntryID = entry.getslid();
			    try{
			    	entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			    }catch (Exception e){
			    	smaction.redirectAction(
			    			"Could not delete: " + e.getMessage(), 
			    			"", 
			    			"&lid=" + entry.getslid() 
			    			+ sCalendarNavigationStringParams
			    			);
					return;
			    }
			    //If the appointment was opened from the schedule go back to the schedule
			    //otherwise, open a new entry.
			    if(aUserNames.size() == 0){
			    	smaction.setCallingClass("smcontrolpanel.SMEditAppointmentEdit");
			    	smaction.redirectAction(
							"", 
							entry.getObjectName() 
							+ ": " + sEntryID + " was successfully deleted.", 
							"&lid=-1" 
							);
			    }else{
					smaction.setCallingClass("smcontrolpanel.SMViewAppointmentCalendarGenerate");
					smaction.redirectAction(
						"", 
						entry.getObjectName() 
						+ ": " + sEntryID + " was successfully deleted.", 
						"&lid=" + entry.getslid()
						+ sCalendarNavigationStringParams
						);
			    }
					return;
	    }
		
		//If it's an edit, process that:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		"COMMANDFLAG", request).compareToIgnoreCase(SMEditAppointmentEdit.COMMAND_VALUE_SAVE) == 0){
    		
	    	boolean bReacquireGeocode = clsManageRequestParameters.get_Request_Parameter(
            	    SMEditAppointmentEdit.ADDRESSCHANGE_FLAG, request).compareToIgnoreCase(
            	    		SMEditAppointmentEdit.ADDRESSCHANGED_VALUE) == 0;
			try{
	    	entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					bReacquireGeocode
	    			);
			}catch (Exception e){
				smaction.getCurrentSession().setAttribute(SMAppointment.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					"&lid=" + entry.getslid()
					+ sCalendarNavigationStringParams
					);
				return;
			}
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						entry.getStatusMessages(), 
						entry.getObjectName() + ": " + entry.getslid() + " was successfully saved.",
						"&lid=" + entry.getslid()
						+ sCalendarNavigationStringParams
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