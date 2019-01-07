package smic;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;

public class ICResetPostingFlag extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserName = "";
	private String sCompanyName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICResetPostingInProcessFlag))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Reset Posting-In-Process Flag";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICResetPostingInProcessFlag) 
	    		+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will reset the system's 'Posting-In-Process' flag so that batches"
	    		+ " can be posted and regular IC processes can continue.  The flag is set when a posting"
	    		+ " process is begun and should be reset when it finishes.  If the posting process is"
	    		+ " stopped unexpectedly, the flag is sometimes not reset, and this function will reset it.<BR><BR>"
	    );
	    
	    ICOption icopt = new ICOption();
	    try {
			icopt.load(sDBID, getServletContext(), sUserName);
		} catch (Exception e1) {
			out.println("Error [1529958402] - could not get posting flag record - " + e1.getMessage());
		}

	    if (icopt.getBatchPostingInProcess() != 0L){
	    	out.println("A previous posting is not completed - "
				+ icopt.getPostingUserFullName() + " has been "
				+ icopt.getPostingProcess() + " "
				+ "since " + icopt.getPostingStartDate() + ".<BR><BR>");
			out.println("Click the button below to clear this flag.");
	    }else{
	    	out.println("NOTE: Currently, the IC posting-in-process flag is already cleared, it does not need to be reset.<BR>");
	    }
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICResetPostingFlagAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Reset Posting Flag----\">");
    	out.println("  Check to confirm flag clearing: <INPUT TYPE=CHECKBOX NAME=\"ConfirmClearing\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
