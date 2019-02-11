package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.Connection;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ARListCustomersOnHoldGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARListCustomersOnHold))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    /**************Get Parameters**************/

    	//Customized title
    	String sReportTitle = "List Customers On Hold";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
		   );
		   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A></TD></TR></TABLE>");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID,
    		"MySQL",
    		this.toString() + ".doGet - User: " 
    		+ sUserID
    		+ " - "
    		+ sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    					+ "Warning=" + sWarning
    		);			
        	return;
    	}

    	boolean bShowCustomerComments = false;
    	if (request.getParameter(ARListCustomersOnHold.SHOWCUSTOMERCOMMENTS) != null){
    		bShowCustomerComments = true;
    	}
    	
    	ARListCustomersOnHoldReport onholdReport = new ARListCustomersOnHoldReport();
    	if (!onholdReport.processReport(conn,
    		out,
    		bShowCustomerComments
    		)){
    		out.println("Could not print report - " + onholdReport.getErrorMessage());
    	}else{
    	    SMLogEntry log = new SMLogEntry(conn);
    	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARLISTCUSTOMERSONHOLD, "REPORT", "AR List Customers On Hold", "[1376509283]");
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067559]");
	    out.println("</BODY></HTML>");
	}
}
