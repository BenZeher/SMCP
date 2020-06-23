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
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMSystemFunctions;

public class SMPrintInstallationTicketGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMPrintInstallationTicket
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    String sStartingOrderNumber = "";
	    String sEndingOrderNumber = "";
	    String sNumberOfCopies = "";
	    sStartingOrderNumber = request.getParameter("StartingOrderNumber");
	    sEndingOrderNumber = request.getParameter("EndingOrderNumber");
	    sNumberOfCopies = clsManageRequestParameters.get_Request_Parameter(SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES, request);
	    
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
    				+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=" + sNumberOfCopies
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		
    		);			
        	return;
		}
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				   "Transitional//EN\">" +
			       "<HTML>" +
			       "<HEAD><BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" 
			       		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}\n"
			       		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
			       		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
			       		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
			       		+ "</STYLE>"
			       + "</HEAD>"
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
    				+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=" + sNumberOfCopies
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	SMInstallationTicket ticket = new SMInstallationTicket();
    	if (!ticket.processReport(
    			conn, 
    			sStartingOrderNumber, 
    			sEndingOrderNumber, 
    			sUserName,
    			iNumberOfCopies,
    			out)){
    		out.println("Could not print work order - " + ticket.getErrorMessage());
    	}else{
    		SMLogEntry log = new SMLogEntry(conn); 
			log.writeEntry(
					sUserID,
					"PRINTINSTALLATIONTICKET",
					"",
					"Order numbers from " + sStartingOrderNumber + " to " + sEndingOrderNumber,
					"[1405006252]"
					);
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080603]");
	    out.println("</BODY></HTML>");
	}
}
