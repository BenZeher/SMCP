package smic;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
public class ICClearStatisticsAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearStatistics)){
			return;
		}
	    response.setContentType("text/html");
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = "0";
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String m_sWarning = "";
	    /**************Get Parameters**************/
	    String sClearBeforeYear = clsManageRequestParameters.get_Request_Parameter("ClearBeforeYear", request);
	    String sClearBeforeMonth = clsManageRequestParameters.get_Request_Parameter("ClearBeforeMonth", request);
	    
	    clearRecords(request, sClearBeforeYear, sClearBeforeMonth, sDBID, sUserID, m_sWarning);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + m_sWarning
		);
		return;

	}
	private void clearRecords(HttpServletRequest req, String sYear, String sMonth, String sDBID, String sUserID, String m_sWarning){
	    if (req.getParameter("ConfirmClearing") == null){
	    	m_sWarning = "You chose to clear statistics, but you did not click the 'Confirm clearing' checkbox.";
	    	return;
	    }
	    
    	String SQL = "DELETE FROM "
    		+ SMTableicitemstatistics.TableName
    		+ " WHERE (" 
    			+ "((" + SMTableicitemstatistics.lYear + " * 100) + " + SMTableicitemstatistics.lMonth + ")"
    			+ " < " + Long.toString((Long.parseLong(sYear) * 100L) + Long.parseLong(sMonth))
    		+ ")"
    		;
    	//System.out.println("In " + this.toString() + " SQL = " + SQL);
    	
    	try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID)){
	    		m_sWarning = "SQL Error clearing item statistics";
	    	}
    	}catch(SQLException e){
    		m_sWarning = "Error clearing item statistics - " + e.getMessage() + ".";
    	}
    	
    	SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_CLEARITEMSTATISTICS, "Item statistics successfully cleared", SQL, "[1376509374]");
    	
		m_sWarning = "Item statistics BEFORE year " + sYear + ", month " + sMonth
			+ " were successfully cleared.";
  	
		return;
	}
}
