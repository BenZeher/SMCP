package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPrintPOGenerate extends HttpServlet {

	private static final int NUMBER_OF_LINES_ON_PO = 27;
	private static final long serialVersionUID = 1L;

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICPrintPurchaseOrders
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
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
		String sWarning = "";
		String sCallingClass = "";
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sStartingPOID = "";
	    String sEndingPOID = "";
	    sStartingPOID = request.getParameter("StartingPOID");
	    sEndingPOID = request.getParameter("EndingPOID");
	    
	    boolean bPrintUnreceivedOnly = request.getParameter(ICPurchaseOrderForm.PRINTUNRECEIVEDONLY) != null;
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				   "Transitional//EN\">" +
			       "<HTML>" +
			       "<HEAD><BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" 
			       		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}</STYLE>"
			       + "</HEAD>"
				   );
				   
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + " - user: " + sUserID
    			+ " - " + sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "StartingPOID=" + sStartingPOID
    				+ "&EndingPOID=" + sEndingPOID
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	ICPurchaseOrderForm po = new ICPurchaseOrderForm();
    	if (!po.processReport(
    			conn, 
    			sStartingPOID, 
    			sEndingPOID, 
    			sDBID,
    			sUserName,
    			NUMBER_OF_LINES_ON_PO,
    			bPrintUnreceivedOnly,
    			out)){
    		out.println("Could not print PO's - " + po.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080949]");
	    out.println("</BODY></HTML>");
	}
}
