package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICListUnusedPOsGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICListUnusedPOs
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //local variables
		String sWarning = "";
		String sCallingClass = "";
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Log the report usage:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_LISTUNUSEDPOS, "REPORT", "IC List Unused POs", "[1376509404]");
	    	    
    	String sReportTitle = "IC List Unused Purchase Orders";    	
    	String sCriteria = "Listing any purchase orders that have been assigned, but do no yet have any lines"
    		+ " added to them.";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICListUnusedPOsGenerate") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICListUnusedPOs) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	//Add this report to the list of 'Return tos':
		CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY,
				SMUtilities.updateURLHistory(
						"Listing unused POs", 
						clsServletUtilities.URLEncode(request.getRequestURI().toString() 
								+ clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*"), 
				CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY),
				CurrentSession.getAttribute("URLMaxSize"))
				);
    	
    	ICListUnusedPOsReport rpt = new ICListUnusedPOsReport();
    	if (!rpt.processReport(
    			conn, 
    			sDBID,
    			sUserID,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + rpt.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080868]");
	    out.println("</BODY></HTML>");
	}
}
