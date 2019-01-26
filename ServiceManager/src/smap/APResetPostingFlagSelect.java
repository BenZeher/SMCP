package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APResetPostingFlagSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APResetPostingInProcessFlag))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "AP Reset Posting-In-Process Flag";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) 
	    	+ "#" + Long.toString(SMSystemFunctions.APResetPostingInProcessFlag) 
	    	+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will reset the system's 'Posting-In-Process' flag so that batches"
	    		+ " can be posted and regular AP processes can continue.  The flag is set when a posting"
	    		+ " process is begun and should be reset when it finishes.  If the posting process is"
	    		+ " stopped unexpectedly, the flag is sometimes not reset, and this function will reset it.<BR><BR>"
	    );
	    
	    try{
	    	String SQL = "SELECT * FROM " + SMTableapoptions.TableName;
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
	    			getServletContext(),
	    			sDBID,
	    			"MySQL",
	    			this.toString() + "doGet - User: " 
	    			+ sUserID
	    			+ " - "
	    			+ sUserFullName
	    			);
	    	
	    	if (rs.next()){
	    		if (rs.getInt(SMTableapoptions.ibatchpostinginprocess) == 0){
	    			out.println("NOTE: Currently, the AP posting-in-process flag is already cleared, it does not need to be reset.<BR>");
	    		}else{
	    			out.println("A previous posting is not completed - "
        					+ SMUtilities.getFullNamebyUserID(rs.getString(SMTableapoptions.luserid), getServletContext(), sDBID, ".APResetPostingFlagSelect") + " has been "
        					+ rs.getString(SMTableapoptions.sprocess) + " "
        					+ "since " + rs.getString(SMTableapoptions.datstartdate) + ".<BR><BR>");
	    			out.println("Click the button below to clear this flag.");
	    		}
	    	}else{
	    		out.println("Could not get posting flag record.");
	    	}
	    	rs.close();
	    }catch (SQLException e){
	    	out.println("Error reading options file - " + e.getMessage());
	    }
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APResetPostingFlagAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Reset Posting Flag----\">");
    	out.println("  Check to confirm flag clearing: <INPUT TYPE=CHECKBOX NAME=\"ConfirmClearing\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
