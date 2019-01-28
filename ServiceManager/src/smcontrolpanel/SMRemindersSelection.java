package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMReminders;
import SMDataDefinition.SMTablereminderusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import SMDataDefinition.SMTablereminders;
import smcontrolpanel.SMSystemFunctions;
import ConnectionPool.WebContextParameters;

public class SMRemindersSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String sCalledClassName = "SMRemindersEdit";
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_EDIT_BUTTON_VALUE = "Edit Selected " + SMReminders.ParamObjectName;
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	public static final String SUBMIT_DELETE_BUTTON_VALUE = "Delete Selected " + SMReminders.ParamObjectName;
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	public static final String SUBMIT_ADD_BUTTON_VALUE = "Add New " + SMReminders.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    //Check user permissions
	    String sEditPersonalSchedule = (String) clsManageRequestParameters.get_Request_Parameter(SMReminders.Paramiremindermode, request);
	    long lCheckPermision = -1;
	    if(sEditPersonalSchedule.compareToIgnoreCase(Integer.toString(SMTablereminders.GENERAL_REMINDER_VALUE)) == 0){
	    	lCheckPermision = SMSystemFunctions.SMEditReminders;
	    }else{
	    	lCheckPermision = SMSystemFunctions.SMEditPersonalReminders;
	    }
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				lCheckPermision))
		{
			return;
		}

		PrintWriter out = response.getWriter();
		
		//Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + SMReminders.ParamObjectName + "s";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMEditReminders) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMReminders.Paramiremindermode + "' VALUE='" + clsManageRequestParameters.get_Request_Parameter(SMReminders.Paramiremindermode, request) + "'>");
	    String sOutPut = "";
	    

	    String sSQL = "";
	    //Use a drop down list here:
		try{
			if(sEditPersonalSchedule.compareToIgnoreCase(Integer.toString(SMTablereminders.GENERAL_REMINDER_VALUE)) == 0){
	        sSQL = "SELECT * FROM " + SMTablereminders.TableName 
	        	+ " WHERE ("
	        	+ "(" + SMTablereminders.iremindermode + "=" 
	        		+ Integer.toString(SMTablereminders.GENERAL_REMINDER_VALUE) + ")"
	        	+ ")"
	        		
	        ;
			}else{
		    sSQL = "SELECT "+ "*" 
			    + " FROM " + SMTablereminderusers.TableName 
		    	+ " LEFT JOIN " + SMTablereminders.TableName
		    	+ " ON " + SMTablereminders.TableName + "." + SMTablereminders.sschedulecode+ " =" 
		    	+ SMTablereminderusers.TableName + "." + SMTablereminderusers.sschedulecode
		    	+ " WHERE("
		    	+ " (" + SMTablereminderusers.TableName + "." + SMTablereminderusers.luserid + "=" + sUserID + ")"
		    	+ " AND"
		    	+ " (" + SMTablereminders.TableName + "." + SMTablereminders.iremindermode + "=" 
		    	+ Integer.toString(SMTablereminders.PERSONAL_REMINDER_VALUE) + ")"
		    		  + ")";
			}
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserFullName);
	     	out.println ("<SELECT NAME=\"" + SMReminders.Paramlid + "\">" );
        	
        	while (rs.next()){
        		String sDescription = rs.getString(SMTablereminders.sdescription);
        		if(sDescription.length() > 80){
        			sDescription = sDescription.substring(0,80) + "...";
        		}
        		out.println ("<OPTION VALUE=\"" + rs.getString(SMTablereminders.lid) + "\">");
        		out.println (rs.getString(SMTablereminders.sschedulecode) + " - " + sDescription);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("<BR><FONT COLOR=RED><B>Error [1452625416] reading Schedules - " + ex.getMessage() + ".</B></FONT><BR>");
			//return false;
		}
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_EDIT_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>";
		
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_ADD_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_ADD_BUTTON_VALUE + "' STYLE='width: 2.25in; height: 0.24in'></P>";
		
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}