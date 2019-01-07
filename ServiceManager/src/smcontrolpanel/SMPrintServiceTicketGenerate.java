package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;

public class SMPrintServiceTicketGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserName = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMPrintServiceTicket
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sStartingOrderNumber = "";
	    String sEndingOrderNumber = "";
	    String sNumberOfCopies = "";
	    sStartingOrderNumber = request.getParameter("StartingOrderNumber");
	    sEndingOrderNumber = request.getParameter("EndingOrderNumber");
	    sNumberOfCopies = clsManageRequestParameters.get_Request_Parameter(SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES, request);
	    
	    int iNumberOfCopies = 0;
	    try {
			iNumberOfCopies = Integer.parseInt(sNumberOfCopies);
		} catch (NumberFormatException e) {
    		sWarning = "Invalid number of copies: " + sNumberOfCopies + ".";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "StartingOrderNumber=" + sStartingOrderNumber
    				+ "&EndingOrderNumber=" + sEndingOrderNumber
    				+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sStartingOrderNumber
    				+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=" + sNumberOfCopies
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		
    		);			
        	return;
		}
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				   "Transitional//EN\">" +
			       "<HTML>" +
			       "<HEAD>" 
			       		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}\n"
			       		+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
			       		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
			       		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
			       		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
			       		+ "@page { size:8.5in 11in; margin: 0.4in }\n"
			       		+ "</STYLE>"
			       + "</HEAD><BODY BGCOLOR=\"#FFFFFF\">"
				   );
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
    				+ "StartingOrderNumber=" + sStartingOrderNumber
    				+ "&EndingOrderNumber=" + sEndingOrderNumber
    				+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=" + sNumberOfCopies
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	//SMServiceTicket ticket = new SMServiceTicket();
    	SMServiceTicketWithFormat ticket = new SMServiceTicketWithFormat();
    	//System.out.println("[1356725990] 1. iNumberOfCopies = " + iNumberOfCopies);
    	if (!ticket.processReport(
    			conn, 
    			sStartingOrderNumber, 
    			sEndingOrderNumber, 
    			sDBID,
    			sUserName,
    			iNumberOfCopies,
    			out)){
    		out.println("Could not print work order - " + ticket.getErrorMessage());
    	}else{
    		SMLogEntry log = new SMLogEntry(conn); 
			log.writeEntry(
					sUserID,
					SMLogEntry.LOG_OPERATION_PRINTSERVICETICKET,
					"",
					"Order numbers from " + sStartingOrderNumber + " to " + sEndingOrderNumber,
					"[1405006253]"
					);
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	    out.println("</BODY></HTML>");
	}
}
