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
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICOnHandByDescriptionGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    		request, 
	    		response, 
	    		getServletContext(), 
	    		SMSystemFunctions.ICOnHandByDescriptionSelection)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //local variables
		
		String sWarning = "";
		String sCallingClass = "";
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sSearchText = "";
	    
	    sSearchText = request.getParameter(ICOnHandByDescriptionSelection.TEXTSNIP_PARAM);
	    
    	String sReportTitle = "IC Quantities On Hand Searched By Item Description";

    	String sCriteria = "Listing items having quantities on hand with '<B>" + sSearchText + "</B>'"
    		+ " in the item description.";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICOnHandByDescriptionGenerate") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICQuantitiesOnHandReport) 
	    	+ "\">Summary</A>");
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
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + ICOnHandByDescriptionSelection.TEXTSNIP_PARAM + "=" + sSearchText
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
	    //log usage of this this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICONHANDBYDESCRIPTION, "REPORT", "ICOnHandByDescription - searching on '" + sSearchText + "'", "[1376509405]");
    	
    	ICOnHandByDescriptionReport rpt = new ICOnHandByDescriptionReport();
    	if (!rpt.processReport(
    			conn, 
    			sSearchText, 
    			sDBID,
    			sUserID,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
    	)){
    		out.println("Could not print report - " + rpt.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080869]");
	    out.println("</BODY></HTML>");
	}
}
