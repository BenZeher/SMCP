package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMSystemFunctions;
import SMClasses.SMAppointmentCalendarGroup;
import SMDataDefinition.SMTableappointmentgroups;
import SMDataDefinition.SMTableappointmentusergroups;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;

public class SMEditAppointmentGroupsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SMAppointmentCalendarGroup.ParamObjectName;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMAppointmentCalendarGroup entry = new SMAppointmentCalendarGroup(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditAppointmentGroupsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditAppointmentGroups
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditAppointmentGroups)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a appointment group entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(OBJECT_NAME) != null){
	    	entry = (SMAppointmentCalendarGroup) currentSession.getAttribute(OBJECT_NAME);
	    	currentSession.removeAttribute(OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentGroupsSelect"
							+ "?" + SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
				}
	    	}
	    }
	    smedit.printHeaderTable();
	    
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to selection" + "</A>");
	    }
	    
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTableappointmentgroups.igroupid + "=" + entry.getigroupid()
				+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getigroupid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMAppointmentCalendarGroup entry) throws SQLException{

		String s = "<TABLE BORDER=1>";
		
		//Group ID:
		String sID = "NEW";
		if (
			(!sm.getAddingNewEntryFlag() )
		){
			sID = entry.getigroupid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Appointment Group ID</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableappointmentgroups.igroupid + "\" VALUE=\"" 
			+ entry.getigroupid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		//Group Name:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTableappointmentgroups.sappointmentgroupname,
				entry.getsappointmentgroupname().replace("\"", "&quot;"), 
				SMTableappointmentgroups.sappointmentgroupnamelength, 
				"<B>Group name: ",
				"Unique name for the appointment calendar group.",
				"40"
			);
		
		//Group Description:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTableappointmentgroups.sappointmentgroupdesc,
				entry.getsappointmentgroupdesc().replace("\"", "&quot;"), 
				SMTableappointmentgroups.sappointmentgroupdesclength, 
				"<B>Description: ",
				"Short description of the appointment calendar group.",
				"75"
		);
		
		s += "</TABLE>";
		
		s += "<BR><B>Select Users To Include In '" + entry.getsappointmentgroupname() + "' Group:</B>";
	    
	    //Add user list
		ArrayList<String> sUserTable = new ArrayList<String>(0);
		try{
			//First get a list of all active users AND inactive users that may still might be in appointment group(s):
	        String sSQL = "SELECT DISTINCT " + SMTableusers.lid
	        		+ ", " + SMTableusers.sUserFirstName
	        		+ ", " + SMTableusers.sUserLastName
	        		+ " FROM " + SMTableusers.TableName
	        		+ " LEFT JOIN " + SMTableappointmentusergroups.TableName 
	        		+ " ON " + SMTableusers.TableName + "." + SMTableusers.lid 
	        		+ " = " + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid
	        		+ " WHERE ("
	        		+ " (" + SMTableusers.iactive + "=1) "
	        		+ "OR (" +SMTableappointmentusergroups.sappointmentgroupname + "=\"" + entry.getsappointmentgroupname() + "\")"
	        				+ ")"
	        		+ " ORDER BY " + SMTableusers.sUserFirstName
	        		;
	        ResultSet rsUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());
        	
	        sSQL = SMMySQLs.Get_Appointment_Group_Users_SQL(entry.getsappointmentgroupname());
	        ResultSet rsGroupUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sm.getsDBID());
	        
	        String sCheckedOrNot = "";
        	while (rsUsers.next()){
        		sCheckedOrNot = Is_User_In_Group(rsUsers.getString(SMTableusers.lid), rsGroupUsers);
        		sUserTable.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + " NAME=\"" + SMAppointmentCalendarGroup.UPDATE_USER_MARKER + "" 
        			+  rsUsers.getString(SMTableusers.lid) + "\">" 
        			+ rsUsers.getString(SMTableusers.sUserFirstName) 
        			+ " " + rsUsers.getString(SMTableusers.sUserLastName)
        			+ "" 
        		);
        	}
        	rsUsers.close();
        	rsGroupUsers.close();
        	//Print the table:
        	s += SMUtilities.Build_HTML_Table(4, sUserTable,1,true);
        	
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		return s;
	}
	
	public String Is_User_In_Group(String sUserID, ResultSet rs){
		
		try {
			// Set this recordset to the beginning every time:
			rs.beforeFirst();
			while (rs.next()){
				if (rs.getString(SMTableappointmentusergroups.luserid).compareTo(sUserID) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class in Is_User_In_Group!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return "";
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
