package smfa;

import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FAClearTransactionHistoryAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
	    				+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sDBID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID);
	    
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAClearTransactionHistory)){
	    	return;
	    }
	    
		FAClearTransactionHistory cProcessor = new FAClearTransactionHistory();
		cProcessor.setFiscalYear(clsManageRequestParameters.get_Request_Parameter("FISCALYEAR", request));
		if (cProcessor.getFiscalYear().compareTo("") == 0){
			
		}
		cProcessor.setFiscalPeriod(clsManageRequestParameters.get_Request_Parameter("FISCALPERIOD", request));
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost(primary) - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				
				);
		//Need a connection here because it involves a data transaction:
		try{
			cProcessor.doProcess(sUserID, conn);
		}catch(Exception e){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067471]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAClearTransactionHistorySelect"
					+ "?" + cProcessor.getQueryString()
					+ "&Warning=Could not finish clearing transaction history: " + clsServletUtilities.URLEncode(cProcessor.getErrorMessageString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		if (cProcessor.getErrorMessageString().length() == 0){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAClearTransactionHistorySelect"
					+ "?Status=" + clsServletUtilities.URLEncode("Clearing transaction history finished successfully.")
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
		}else{
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAClearTransactionHistorySelect"
					+ "?Warning=" + clsServletUtilities.URLEncode("Clearing transaction history encountered error(s): " + cProcessor.getErrorMessageString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		
		return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
