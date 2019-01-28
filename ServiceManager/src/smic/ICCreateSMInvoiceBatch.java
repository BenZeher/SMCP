package smic;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;

public class ICCreateSMInvoiceBatch extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
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
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=" + SMUtilities.URLEncode("Error - Could not get data connection to read invoices.")
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }

		ICImportSMInvoices icsmi = new ICImportSMInvoices();
		icsmi.setCreatedBy(sUserFullName, sUserID);
		if(icsmi.importInvoices(conn,getServletContext(),sUserFullName, sDBID, sUserID)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080811]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Status=" + SMUtilities.URLEncode("Invoice batch " + icsmi.getM_sBatchNumber() + " successfully created, " //and posted "
					+ Long.toString(icsmi.getNumberOfInvoicesProcessed()) + " invoices were processed, "
					+ Long.toString(icsmi.getNumberOfInvoicesImported()) + " invoices were imported into the batch.")
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}else{
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080812]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=" + SMUtilities.URLEncode("Batch NOT created: " + icsmi.getErrorMessage())
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
