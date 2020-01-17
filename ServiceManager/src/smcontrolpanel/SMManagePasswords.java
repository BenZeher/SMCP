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

import SMClasses.MySQLs;
import ServletUtilities.clsDatabaseFunctions;

public class SMManagePasswords extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMManageuserpasswords))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage passwords";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<BR>NOTE: " + SMUtilities.PASSWORD_REQUIREMENTS + "<BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMManagePasswordsAction' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("Change password for:<BR>");
	    //Add drop down list
		try{
	        String sSQL = MySQLs.Get_User_List_SQL(false);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"User\">" );
        	
        	while (rs.next()){
        		//flag that there are multiple entries.
        		String sOutPut = "<OPTION VALUE=\"" + rs.getString("sUserName") + "\">";
        		sOutPut = sOutPut + rs.getString("sUserFirstName") + " ";
        		sOutPut = sOutPut + rs.getString("sUserLastName");
        		sOutPut = sOutPut + " - " + rs.getString("sUserName") + " - ";
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("[1579274003] Error in SMManagePasswords class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		String sOutPut = "<P>";
		sOutPut = sOutPut + "Enter the new password:<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=PASSWORD NAME='NewPassword' SIZE=28 MAXLENGTH=50 STYLE='width: 2.41in; height: 0.25in;'></P>";
		sOutPut = sOutPut + "<P>Retype the new password:<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=PASSWORD NAME='ConfirmedPassword' SIZE=28 MAXLENGTH=50 STYLE='width: 2.41in; height: 0.25in;'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='Submit' VALUE='Change Password' STYLE='height: 0.24in'></P>";
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
