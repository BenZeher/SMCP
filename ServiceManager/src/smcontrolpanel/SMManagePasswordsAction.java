package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMManagePasswordsAction extends HttpServlet {
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
	    String title = "Password Change";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    String sUserToUpdate = clsStringFunctions.filter(request.getParameter("User"));
	    String sNewPassword = clsStringFunctions.filter(request.getParameter("NewPassword"));
	    String sConfirmedPassword = clsStringFunctions.filter(request.getParameter("ConfirmedPassword"));
	    
	    //First, check the password for validity:
	    if (sNewPassword.compareTo(sConfirmedPassword) != 0){
	    	out.println ("The 'confirming' password and the new password don't match.  The password for " + sUserToUpdate + " remains unchanged.");
	    	return;
	    }
	    
	    try {
			SMUtilities.checkSMCPPassword(sNewPassword);
		} catch (Exception e) {
    		out.println (e.getMessage());
    		return;
		}
	    
	    //At this point, we have valid password strings to process
	    
    	if (Change_Current_Password(
    			sDBID,
    			sUserToUpdate,
    			sNewPassword
    			) == true){
    		out.println ("Password changed.");
    	}
    	else {
    		out.println ("Unable to change current password.  The password for " + sUserToUpdate + " remains unchanged.");
    	}
		
		out.println("</BODY></HTML>");
	}
	
	private boolean Change_Current_Password(
			String sDBID,
			String sUserName,
			String sNewPassword
			){
	
		String sSQL = SMMySQLs.Update_User_Password_SQL(sUserName, sNewPassword);
		try {
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in SMChangeUserPasswordAction.Change_Current_Password class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}