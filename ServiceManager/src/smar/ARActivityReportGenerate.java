package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class ARActivityReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARActivityReport)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    /**************Get Parameters**************/
    	String sStartingCustomer = request.getParameter("StartingCustomer");
    	String sEndingCustomer = request.getParameter("EndingCustomer");
    	
    	java.sql.Date datStartingDate;
    	try {
			datStartingDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyy", request.getParameter("StartingTranDate"));
		} catch (ParseException e) {
    		sWarning = "Invalid starting date.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	String sEndingDate = request.getParameter("EndingTranDate");
    	java.sql.Date datEndingDate;
    	try {
			datEndingDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyy", request.getParameter("EndingTranDate"));
		} catch (ParseException e) {
    		sWarning = "Invalid ending date.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}

    	boolean bIncludeFullyPaidTransactions = false;
    	if (request.getParameter("IncludeFullyPaidTransactions") != null){
    		bIncludeFullyPaidTransactions = true;
    	}
    	/**************End Parameters**************/
    	
    	//Customized title
    	String sReportTitle = "AR Activity Report";
    	String sFullyPaid = "NOT ";
    	if(bIncludeFullyPaidTransactions){
    		sFullyPaid = "";
    	}
    	String sCriteria = "Starting with customer <B>" + sStartingCustomer + "</B>"
    		+ ", ending with customer <B>" + sEndingCustomer + "</B>"
    		+ ", starting on <B>" + request.getParameter("StartingTranDate") + "</B>"
    		+ ", ending on <B>" + sEndingDate + "</B>"
    		+ " <B>" + sFullyPaid  + "INCLUDING</B> fully paid transactions."
    		;
    	String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Accounts Receivable Main Menu</A></TD></TR></TABLE>");
    	
    	//Retrieve information
    	ARActivityReport ar = new ARActivityReport();
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			this.toString() + ".doGet - User: " 
    			+ sUserID
    			+ " - "
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    			);
    	
    	if (conn == null){
    		out.println("Could not print report - could not open connection");
    	}else{
	    	if (!ar.processReport(conn,
	    						sStartingCustomer, 
	    						sEndingCustomer,
	    						datStartingDate,
	    						datEndingDate,
	    						bIncludeFullyPaidTransactions, 
	    						sDBID, 
	    						out,
	    						getServletContext())){
	    		out.println("Could not print report - " + clsStringFunctions.filter(ar.getErrorMessage()));
	    	}
    	}
    	
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARACTIVITYREPORT, "REPORT", "AR Activity Report", "[1376509251]");
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067480]");
    	out.println("</BODY></HTML>");
	}
}
