package smic;

//import SMDataDefinition.*;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;

public class ICCreateBatchFromReceipts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    String title = "Creating batch from receipts";
	    String subtitle = "Please wait . . .";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println("</BODY></HTML>");
	    
		//Need a connection for the data transaction:
	    Connection conn = clsDatabaseFunctions.getConnection(
	    	getServletContext(), 
	    	sDBID,
	    	"MySQL",
	    	this.toString() + ".doPost - User: " + sUserID
	    	+ " - "
	    	+ sUserFullName
	    		);
	    if(conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=Error - Could not get data connection to create receipt batch."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
	    
	    ICAutoCreateReceiptBatch iccreate = new ICAutoCreateReceiptBatch();
		iccreate.setCreatedBy(sUserFullName, sUserID);
		if(iccreate.checkToCreateNewReceiptBatch(conn, getServletContext(), sDBID, sUserID, sUserFullName)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080810]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Status=Receipt batch " + iccreate.getM_sBatchNumber() + " successfully created and posted."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}else{
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080811]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditBatches" + "?"
					+ "Warning=Batch NOT created: " + iccreate.getErrorMessage()
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
