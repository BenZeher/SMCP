package smic;

//import SMDataDefinition.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;

public class ICCreateBatchFromInvoices extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICEditBatches
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    			  + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	   
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    String title = "Creating batch from invoices";
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
	    	+ "- "
	    	+ sUserFullName
	    		);
	    if(conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=Error - Could not get data connection to create invoice batch."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
	    
	    ICAutoCreateInvoiceBatch iccreate = new ICAutoCreateInvoiceBatch();
		iccreate.setCreatedBy(sUserFullName, sUserID);
		if(iccreate.checkToCreateNewBatch(conn,sUserFullName, getServletContext(), sDBID, sUserID)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080808]");
			String sStatus = "";
			if (iccreate.getM_sBatchNumber().compareToIgnoreCase("-1") == 0){
				sStatus += "No adjustment batch was created";
			}else{
				sStatus += "Successfully created and posted adjustment batch " + iccreate.getM_sBatchNumber();
			}
			sStatus += " - export sequence number was " + iccreate.getsExportSequenceNumber() + ".";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Status=" + sStatus
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}else{
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080809]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=Invoice export NOT processed: " + clsServletUtilities.URLEncode(iccreate.getErrorMessage())
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