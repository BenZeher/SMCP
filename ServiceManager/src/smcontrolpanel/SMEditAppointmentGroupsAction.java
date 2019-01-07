package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMAppointmentCalendarGroup;
import SMDataDefinition.SMTableappointmentgroups;
import smcontrolpanel.SMSystemFunctions;

public class SMEditAppointmentGroupsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String OBJECT_NAME = SMAppointmentCalendarGroup.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditAppointmentGroups)){return;}
	    //Read the entry fields from the request object:
		SMAppointmentCalendarGroup entry = new SMAppointmentCalendarGroup(request);
		//smaction.getCurrentSession().setAttribute(SMScheduleGroup.ParamObjectName, entry);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sAppointmentGroupName = entry.getsappointmentgroupname();
			    try {
					entry.delete(getServletContext(), 
							smaction.getsDBID(), 
							smaction.getUserName(), 
							smaction.getUserID(),
							smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
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
						entry.getObjectName() + ": " + sAppointmentGroupName + " was successfully deleted.", 
						SMTableappointmentgroups.igroupid + "=" + "0"
					);
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
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
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					request
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(SMScheduleGroup.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getsappointmentgroupname() + " was successfully saved.",
					SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
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