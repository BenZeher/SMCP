package smar;

//import SMDataDefinition.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

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

public class ARCreateSMInvoiceBatch extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.AREditBatches)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Creating invoice batch";
	    String subtitle = "Please wait . . .";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println("</BODY></HTML>");
	    
		//Need a connection for the data transaction:
	    Connection conn = clsDatabaseFunctions.getConnection(
	    	getServletContext(), 
	    	sDBID,
	    	"MySQL",
	    	this.toString() + ".doPost - User: " 
	    	+ sUserID
	    	+ " - "
	    	+ sUserFullName
	    		);
	    if(conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "AREditBatches" + "?"
					+ "Warning=Error - Could not get data connection to read invoices."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
	    }
	    
		ARImportSMInvoices arsmi = new ARImportSMInvoices();
		arsmi.setCreatedBy(sUserID, sUserFullName);
		
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARINVOICEIMPORT, "Import started", "", "[1376509270]");
		if(arsmi.importInvoices(conn)){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ARINVOICEIMPORT, 
					"Import completed successfully in batch " + arsmi.getM_sBatchNumber(),
					"",
					"[1376509271]");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067522]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "AREditBatches" + "?"
					+ "Warning=Invoice batch " + arsmi.getM_sBatchNumber() + " successfully created."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}else{
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ARINVOICEIMPORT, 
					"Import FAILED: " + arsmi.getErrorMessage(),
					"",
					"[1376509272]");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067523]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "AREditBatches" + "?"
					+ "Warning=Batch NOT created: " + arsmi.getErrorMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}
		return;
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
