package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablegloptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLResetPostingFlagAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLResetPostingInProcessFlag))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    /**************Get Parameters**************/

    	//Customized title
    	String sTitle = "GL Reset Posting-In-Process Flag";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sTitle + "</B></FONT></TD></TR>"
		   );
		   
    	if (request.getParameter("ConfirmClearing") == null){
    		sWarning = "You chose to reset the posting flag, but you did not check the 'Confirm clearing' checkbox.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
    	//Reset flag:
    	boolean bResetSuccessful = true;
    	try{
    		String SQL = "UPDATE " + SMTablegloptions.TableName 
    			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 0"
    			+ ", " + SMTablegloptions.sprocess + " = ''"
    			+ ", " + SMTablegloptions.luserid + " = 0"
    			+ ", " + SMTablegloptions.datstartdate + " = '0000-00-00'"
    			;
    		if (!clsDatabaseFunctions.executeSQL(
    				SQL, 
    				getServletContext(), 
    				sDBID,
    				"MySQL",
    				this.toString() + ".doGet - User: " + sUserName)){
    			bResetSuccessful = false;
    			sWarning = "Could not execute update statement.";
    		}
    	}catch (SQLException e){
    		bResetSuccessful = false;
    		sWarning = "Error updating flag - " + e.getMessage();
    	}
    	
    	//If there was no error message, simply advise that the delete process was successful:
    	if (bResetSuccessful){
    		sWarning = "Posting flag was successfully reset.";
    	}else{
    		sWarning = sWarning + "<BR>Posting flag was NOT reset.";
    	}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
}
