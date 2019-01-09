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
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmsequenceusers;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASAuthorizeAlarmUsersEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUser = "";
	
	public static final String AUTHORIZE_USERS_BUTTON_LABEL = "Update alarm users";
	public static final String USER_CHECKBOX_PARAMETER_PREFIX = "ALARMUSER";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ASAuthorizeAlarmSequenceUsers
			)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sAlarmSequenceID = clsManageRequestParameters.get_Request_Parameter(SMTablessalarmsequences.lid, request);
		PrintWriter out = response.getWriter();
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".doPost - user: " + sUser);
		} catch (Exception e) {
			out.println("<BR>Error [1462382482] getting data connection - " + e.getMessage() + ".<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		SSAlarmSequence alarmsequence = new SSAlarmSequence();
		alarmsequence.setslid(sAlarmSequenceID);
		try {
			alarmsequence.load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067594]");
			out.println("<BR>Error [1462382483] could not load alarm sequence ID '" + sAlarmSequenceID + "' - " + e.getMessage() + ".<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		String title = "";
		String subtitle = "";
		
		title = "Authorize users for alarm sequence : " + alarmsequence.getsdescription();
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
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    
		String sWarning = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.WARNING_PARAMETER, request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.STATUS_PARAMETER, request);
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>" + sWarning + "</B></FONT><BR>");
		}
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<BR><B>" + sStatus + "</B></FONT><BR>");
		}
		
	    try {
			Edit_Group(alarmsequence, out, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067595]");
			out.println("<BR>" + e.getMessage() + "<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067596]");
		out.println("</BODY></HTML>");
		return;
	}
	
	public String isUserOnAlarmSequence(String sUserID, ResultSet rsAuthorizedAlarmUsers) throws Exception{
		
		try {
			// Set this recordset to the beginning every time:
			rsAuthorizedAlarmUsers.beforeFirst();
			while (rsAuthorizedAlarmUsers.next()){
				if (Long.toString(rsAuthorizedAlarmUsers.getLong(SMTablessalarmsequenceusers.luserid)).compareTo(sUserID) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException e){
			throw new Exception("Error [1462382678] checking authorized alarm users - " + e.getMessage());
		}
	}

	private void Edit_Group(
			SSAlarmSequence alarmsequence,
			PrintWriter pwOut, 
			Connection conn) throws Exception{
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASAuthorizeAlarmUsersAction' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMTablessalarmsequences.lid + "' VALUE='" + alarmsequence.getslid() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"CallingClass\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
	    String sOutPut = "";
	    pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='" + AUTHORIZE_USERS_BUTTON_LABEL + "' STYLE='height: 0.24in'></P>");
	    pwOut.println("<B>Select Users To Authorize For "
	    	+ alarmsequence.getsdescription() + ":</B>");
	    
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
			+ " FROM " + SMTablessalarmsequenceusers.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmsequenceusers.lalarmsequenceid + " = " + alarmsequence.getslid() + ")"
			+ ")";
        ResultSet rsAuthorizedAlarmUsers = clsDatabaseFunctions.openResultSet(sSQL, conn);
        
        String sCheckedOrNot = "";
        String sInactive = "";
    	while (rsUsers.next()){
    		sCheckedOrNot = isUserOnAlarmSequence(Long.toString(rsUsers.getLong(SMTableusers.lid)), rsAuthorizedAlarmUsers);
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
    	rsAuthorizedAlarmUsers.close();
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
