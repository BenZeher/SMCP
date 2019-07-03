package smcontrolpanel;

import SMClasses.MySQLs;
import SMClasses.SMUser;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditUsersAction extends HttpServlet{
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditUsers))
		{
			return;
		}
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditUsers)){return;}
	    //Read the entry fields from the request object:
		SMUser entry = new SMUser(request, getServletContext(), smaction.getsDBID());
		smaction.setCallingClass("smcontrolpanel.SMEditUsersEdit");
		
		//smaction.getCurrentSession().setAttribute(SMScheduleGroup.ParamObjectName, entry);
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    //If it is a request to delete the user record from the edit class
	    if(request.getParameter(SMEditUsersEdit.DELETE_USER_BUTTON_NAME) != null){
		    	try{
		    		entry.delete(getServletContext(), sDBID, entry.getlid(), entry.getsUserFullName());
		    	}catch (Exception e){
		    		response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersEdit?"
			    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    			+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
			    			);
		    		return;
		    	}
	    		response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?"
		    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    			+ "&Status=" + clsServletUtilities.URLEncode("Successfully deleted user with ID: " + entry.getlid()  + " (" + entry.getsUserName() + ")")
		    			); 
	    			return;
	    }
	    
	  //If it is a request to save the user record from the edit class
	    if(request.getParameter(SMEditUsersEdit.UPDATE_USER_BUTTON_NAME) != null){
		    //Validate user input
		    try {
				validateEntries(entry, request);
			} catch (Exception e) {
		   		response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersEdit?"
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
				);
		   		return;
			}
		    //Save the record
		    try {
		    	//Update users table record
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
				//Update security groups, appointment groups, alarm sequences, device users, and custom links
				ArrayList<String> sSQLList = new ArrayList<String>(0);
				sSQLList.add(MySQLs.Delete_User_From_Security_User_Groups(entry.getlid()));
				sSQLList.add(MySQLs.Delete_User_From_Appointment_User_Groups(entry.getlid()));
				sSQLList.add(MySQLs.Delete_User_From_Alarm_Sequences(entry.getlid()));
				sSQLList.add(MySQLs.Delete_User_From_Device_Users(entry.getlid()));
				sSQLList.add(MySQLs.Delete_Users_Custom_Links(entry.getlid()));
				    
				Enumeration<?> paramNames = request.getParameterNames();
				String sSecurityGroupMarker = SMEditUsersEdit.UPDATE_SECURITY_GROUPS_PREFIX;
				String sAppointmentGroupMarker = SMEditUsersEdit.UPDATE_APPOINTMENT_GROUP_PREFIX;
				String sAlarmSequenceMarker = SMEditUsersEdit.UPDATE_AS_ALARM_SEQUENCES_PREFIX;
				String sDeviceUserMarker = SMEditUsersEdit.UPDATE_AS_DEVICES_PREFIX;
				String sUsersCustomLinkMarker = SMEditUsersEdit.UPDATE_CUSTOM_LINK_PREFIX;
				    
				while(paramNames.hasMoreElements()) {
				  String sParamName = (String)paramNames.nextElement();
				  //Add all insert statements for security groups.
				  if (sParamName.contains(sSecurityGroupMarker)){
					  String sSecurityGroupName = (sParamName.substring(sParamName.indexOf(sSecurityGroupMarker) + sSecurityGroupMarker.length()));
					  sSQLList.add(MySQLs.Insert_Security_User_Groups_SQL(sSecurityGroupName, entry.getlid()));
				  }
				  //Add all insert statements for appointment groups.
				  if (sParamName.contains(sAppointmentGroupMarker)){
					  String sAppointmentGroupName = (sParamName.substring(sParamName.indexOf(sAppointmentGroupMarker) + sAppointmentGroupMarker.length()));
					  sSQLList.add(MySQLs.Insert_Appointment_User_Groups_SQL(sAppointmentGroupName, entry.getlid()));
				  }
				  //Add all insert statements for alarm sequence users.
				  if (sParamName.contains(sAlarmSequenceMarker)){
					  String sAlarmSequenceID = (sParamName.substring(sParamName.indexOf(sAlarmSequenceMarker) + sAlarmSequenceMarker.length()));
					  sSQLList.add(MySQLs.Insert_Alarm_Sequence_User_SQL(sAlarmSequenceID, entry.getlid()));
				  }
				  //Add all insert statements for device users.
				  if (sParamName.contains(sDeviceUserMarker)){
					  String sDeviceID = (sParamName.substring(sParamName.indexOf(sDeviceUserMarker) + sDeviceUserMarker.length()));
					  sSQLList.add(MySQLs.Insert_Device_User_SQL(sDeviceID, entry.getlid()));
				  }
				  //Add all insert statements for users custom links.
				  if (sParamName.contains(sUsersCustomLinkMarker)){
					  String sCustomLinkID = (sParamName.substring(sParamName.indexOf(sUsersCustomLinkMarker) + sUsersCustomLinkMarker.length()));
					  sSQLList.add(MySQLs.Insert_Users_Custom_Links_SQL(sCustomLinkID, entry.getlid()));
				  }
				}
				try{
					if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID) == false){
						throw new Exception("");
					}else{
						//sOutPut = "Successfully updated all groups" + ".";
					}
				}catch (SQLException ex){
					throw new Exception("");
				}
		    } catch (Exception e) {
		    	smaction.getCurrentSession().setAttribute(SMUser.ParamObjectName, entry);
		    	smaction.redirectAction(
				"Could not save: " + e.getMessage(), 
				"", 
				SMUser.Paramlid + "=" + entry.getlid()
		    			);
		    }

			smaction.redirectAction(
					"", 
					"'" + entry.getsUserName() + "' was updated successfully.", 
					SMUser.Paramlid + "=" + entry.getlid()
				);
				return;
	    }

	}

	private void validateEntries(SMUser entry, HttpServletRequest request) throws Exception {
		if(entry.bIsNewRecord()) {
		    if (entry.getsUserName().compareToIgnoreCase("") == 0){
		    	throw new Exception("Username field can be blank. The user needs this to sign in.");
		    }	
		}
		return;
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
