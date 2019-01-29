package smas;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASAuthorizeDeviceUsersEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String AUTHORIZE_USERS_BUTTON_LABEL = "Update device users";
	public static final String USER_CHECKBOX_PARAMETER_PREFIX = "DEVICEUSER";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ASAuthorizeDeviceUsers
			)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sDeviceID = clsManageRequestParameters.get_Request_Parameter(SMTablessdevices.lid, request);
		PrintWriter out = response.getWriter();
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".doPost - user: " + sUser);
		} catch (Exception e) {
			out.println("<BR>Error [1459168923] getting data connection - " + e.getMessage() + ".<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		SSDevice device = new SSDevice();
		device.setslid(sDeviceID);
		try {
			device.load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067597]");
			out.println("<BR>Error [1459168924] could not load device ID '" + sDeviceID + "' - " + e.getMessage() + ".<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		String title = "";
		String subtitle = "";
		
		title = "Authorize users for device : " + device.getsdescription();
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR><BR>");
		
	    //Print a link back to the main Alarm System menu:
		out.println(
			"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
			+ sDBID + "\">Return to Alarm Systems Main Menu</A><BR>");
	    
		String sWarning = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.WARNING_PARAMETER, request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.STATUS_PARAMETER, request);
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>" + sWarning + "</B></FONT><BR>");
		}
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<BR><B>" + sStatus + "</B></FONT><BR>");
		}
		
	    try {
			Edit_Group(device, out, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067598]");
			out.println("<BR>" + e.getMessage() + "<BR>");
			out.println("</BODY></HTML>");
			return;
		}
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067599]");
		out.println("</BODY></HTML>");
		return;
	}
	
	public String isUserOnDevice(String sUserID, ResultSet rsAuthorizedDeviceUsers) throws Exception{
		
		try {
			// Set this recordset to the beginning every time:
			rsAuthorizedDeviceUsers.beforeFirst();
			while (rsAuthorizedDeviceUsers.next()){
				if (Long.toString(rsAuthorizedDeviceUsers.getLong(SMTablessdeviceusers.luserid)).compareTo(sUserID) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException e){
			throw new Exception("Error [1459170146] checking authorized device users - " + e.getMessage());
		}
	}

	private void Edit_Group(
			SSDevice device,
			PrintWriter pwOut, 
			Connection conn) throws Exception{
	    
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASAuthorizeDeviceUsersAction' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTablessdevices.lid + "' VALUE='" + device.getslid() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"CallingClass\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
	    String sOutPut = "";
	    pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='" + AUTHORIZE_USERS_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>");
	    pwOut.println("<B>Select Users To Authorize For "
	    	+ device.getsdescription() + ":</B>");
	    
	    //Add user list
		ArrayList<String> sUserTable = new ArrayList<String>(0);
		//First get a list of all the users:
        String sSQL = "SELECT * FROM " + SMTableusers.TableName
           	+ " WHERE ("
       		+ "(" + SMTableusers.iactive + " = 1)"
        	+ ")"
			+ " ORDER BY " + SMTableusers.sUserFirstName;
        ResultSet rsUsers;
		try {
			rsUsers = clsDatabaseFunctions.openResultSet(sSQL, conn);
		} catch (Exception e) {
			throw new Exception("Error [1459169609] getting user recordset with SQL: " + sSQL + " - " + e.getMessage());
		}

		//Now get a list of all the authorized users for this device:
		sSQL = "SELECT"
			+ " * "
			+ " FROM " + SMTablessdeviceusers.TableName
			+ " WHERE ("
				+ "(" + SMTablessdeviceusers.ldeviceid + " = " + device.getslid() + ")"
			+ ")";
        ResultSet rsAuthorizedDeviceUsers = clsDatabaseFunctions.openResultSet(sSQL, conn);
        
        String sCheckedOrNot = "";
        String sInactive = "";
    	while (rsUsers.next()){
    		sCheckedOrNot = isUserOnDevice(Long.toString(rsUsers.getLong(SMTableusers.lid)), rsAuthorizedDeviceUsers);
    		if (rsUsers.getInt(SMTableusers.iactive) == 0){
    			sInactive = "&nbsp;<FONT COLOR=BLACK><B>(INACTIVE)</B></FONT>";
    		}else{
    			sInactive = "";
    		}
    		sUserTable.add(
    			"<INPUT TYPE=CHECKBOX " 
    			+ sCheckedOrNot 
    			+ " NAME=\"" + USER_CHECKBOX_PARAMETER_PREFIX +  Long.toString(rsUsers.getLong(SMTableusers.lid)) + "\">" 
    			+ rsUsers.getString(SMTableusers.sUserFirstName) 
    			+ " " + rsUsers.getString(SMTableusers.sUserLastName)
    			+ sInactive
    		);
    	}
    	rsUsers.close();
    	rsAuthorizedDeviceUsers.close();
    	//Print the table:
    	pwOut.println(SMUtilities.Build_HTML_Table(4, sUserTable,1,true));
        	
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='" + AUTHORIZE_USERS_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + "</FORM>";
		pwOut.println(sOutPut);
		
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
