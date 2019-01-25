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

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMChangeUserPasswordAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMChangeyourpassword))
			{
				return;
			}
		
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String title = "Password Change";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    String sCurrentPassword = clsStringFunctions.filter(request.getParameter("CurrentPassword"));
	    String sNewPassword = clsStringFunctions.filter(request.getParameter("NewPassword"));
	    String sConfirmedPassword = clsStringFunctions.filter(request.getParameter("ConfirmedPassword"));
	    
	    //First, check the password for validity:
	    if (sNewPassword.compareTo(sConfirmedPassword) != 0){
	    	out.println ("Your 'confirming' password and your new password don't match.  Your password remains unchanged.");
	    	return;
	    }
    	try {
			SMUtilities.checkSMCPPassword(sNewPassword);
		} catch (Exception e) {
	    	out.println (e.getMessage());
	    	return;
		}

    	if (sCurrentPassword.length() == 0){
    		out.println ("Your current password cannot be blank.  Your password remains unchanged.");
    		return;
    	}

	    if (sNewPassword.compareTo(sCurrentPassword) == 0){
	    	out.println ("Your new password must be different than your curent password.  Your password remains unchanged.");
	    	return;
	    }
	    
	    //At this point, we have valid old and new password strings to process
	    //First, see if the current password matches:
	    
	    if (Check_Current_Password(
	    		sDBID, 
	    		sUserName, 
	    		sCurrentPassword
	    		) == true){
	    	
	    	if (Change_Current_Password(
	    			sDBID,
	    			sUserName,
	    			sNewPassword
	    			) == true){
	    		out.println ("Password changed.");
	    	}
	    	else {
	    		out.println ("Unable to change current password.  Your password remains unchanged.");
	    	}
	    }
	    else{
	    	out.println ("Invalid current password.  Your password remains unchanged.");
	    	return;
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private boolean Check_Current_Password(
			String sDBID,
			String sUserName,
			String sCurrentPassword
			){
		
		try{
	        String sSQL = SMMySQLs.Get_User_Password_Check_SQL(clsDatabaseFunctions.FormatSQLStatement(sUserName), clsDatabaseFunctions.FormatSQLStatement(sCurrentPassword));
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    if (rs.next()){
		    	String sHashedPw = rs.getString("sHashedPw");
		    	String sHashedCurrentPassword = rs.getString("HashedCurrentPassword");
		    	if (sHashedPw.compareTo(sHashedCurrentPassword) == 0){
		    		rs.close();
		    		return true;
		    	}
		    	else{
		    		rs.close();
		    		return false;
		    	}
		    }else{
		    	rs.close();
		    	return false;
		    }
		}catch (SQLException ex){
	    	System.out.println("Error in SMChangeUserPasswordAction.Check_Current_Password class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
	}
	
	private boolean Change_Current_Password(
			String sDBID,
			String sUserName,
			String sNewPassword
			){
	
		//System.out.println("*** Into Check Current PAssword)");
		String sSQL = SMMySQLs.Update_User_Password_SQL(clsDatabaseFunctions.FormatSQLStatement(sUserName), clsDatabaseFunctions.FormatSQLStatement(sNewPassword));
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