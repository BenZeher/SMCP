package smcontrolpanel;

import SMClasses.SMUser;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditUsersSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static String EDIT_USER_BUTTON_NAME = "SubmitEdit";
	public static String EDIT_USER_BUTTON_VALUE = "Edit Selected User";
	public static String ADD_NEW_USER_BUTTON_NAME = "SubmitAdd";
	public static String ADD_NEW_USER_BUTTON_VALUE = "Add New User";
	public static String DELETE_USER_BUTTON_NAME = "SubmitDelete";
	public static String DELETE_USER_BUTTON_VALUE = "Delete Selected User";
	public static String DELETE_USER_CONFIRM_CHECKBOX_NAME = "ConfirmDelete";
	public static String DELETE_USER_CONFIRM_CHECKBOX_LABEL = "Check to confirm deletion";
	public static String NEW_USER_TEXT_NAME = "NEWUSERNAME";
	public static String NEW_USER_TEXT_LABEL = "New User To Be Added";
	
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditUsers))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage Users";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
		String sWarning = (String) clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String sStatus = (String) clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if(sWarning.compareToIgnoreCase("") != 0){
    		out.println("<FONT COLOR=\"RED\"><B>Warning: " + sWarning + "</B></FONT><BR>");
    	}
    	if(sStatus.compareToIgnoreCase("") != 0){
    		out.println("<FONT><B>Status: " + sStatus + "</B></FONT><BR>");
    	}
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
			String sUserPrefix = "";
	        String sSQL = SMMySQLs.Get_User_List_SQL(true);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + SMUser.Paramlid + "\">" );
        	
        	while (rs.next()){
        		//flag that there are multiple entries.
        		if (rs.getInt(SMTableusers.iactive) != 1){
        			sUserPrefix = "*";
        		}else{
        			sUserPrefix = "";
        		}
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTableusers.lid) + "\">";
        		sOutPut += sUserPrefix + rs.getString(SMTableusers.sUserFirstName) + " " + rs.getString(SMTableusers.sUserLastName)
        				+ " - " + rs.getString(SMTableusers.sUserName);
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in SMEditUsers class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='" + EDIT_USER_BUTTON_NAME + "' VALUE='" + EDIT_USER_BUTTON_VALUE + "' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + DELETE_USER_BUTTON_NAME + "' VALUE='" + DELETE_USER_BUTTON_VALUE + "' STYLE='width: 2.00in; height: 0.24in'>";
		sOutPut = sOutPut + DELETE_USER_CONFIRM_CHECKBOX_LABEL + "  : <INPUT TYPE=CHECKBOX NAME=\"" + DELETE_USER_CONFIRM_CHECKBOX_NAME + "\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + ADD_NEW_USER_BUTTON_NAME + "' VALUE='" + ADD_NEW_USER_BUTTON_VALUE + "' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "<P>" + NEW_USER_TEXT_LABEL + ": <INPUT TYPE=TEXT NAME=\"" + NEW_USER_TEXT_NAME + "\" SIZE=28 MAXLENGTH=50 STYLE=\"width: 2.41in; height: 0.25in\"></P>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}