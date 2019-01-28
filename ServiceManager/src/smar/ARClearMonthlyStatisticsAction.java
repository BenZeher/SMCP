package smar;

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
public class ARClearMonthlyStatisticsAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARClearMonthlyStatistics)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    /**************Get Parameters**************/
	    String sClearBeforeYear = ARUtilities.get_Request_Parameter("ClearBeforeYear", request);
	    String sClearBeforeMonth = ARUtilities.get_Request_Parameter("ClearBeforeMonth", request);
	    
	    try {
			clearRecords(request, sClearBeforeYear, sClearBeforeMonth, sDBID, sUserID);
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e.getMessage()
			);
		}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + "Monthly statistics BEFORE year " + sClearBeforeYear + ", month " + sClearBeforeMonth
				+ " were successfully cleared."
		);
		return;

	}
	private void clearRecords(HttpServletRequest req, String sYear, String sMonth, String sDBID, String sUserID) throws Exception{
	    if (req.getParameter("ConfirmClearing") == null){
	    	throw new Exception("You chose to clear statistics, but you did not click the 'Confirm clearing' checkbox.");
	    }
	    
    	String SQL = "DELETE FROM "
    		+ SMTablearmonthlystatistics.TableName
    		+ " WHERE (" 
    			+ "((" + SMTablearmonthlystatistics.sYear + " * 100) + " + SMTablearmonthlystatistics.sMonth + ")"
    			+ " < " + Long.toString((Long.parseLong(sYear) * 100L) + Long.parseLong(sMonth))
    		+ ")"
    		;
    	//System.out.println("In " + this.toString() + " SQL = " + SQL);
    	
    	try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID)){
	    		throw new Exception("SQL Error clearing monthly statistics");
	    	}
    	}catch(SQLException e){
    		throw new Exception("Error [1548693424] clearing monthly statistics - " + e.getMessage() + ".");
    	}
    	
    	SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_CLEARMONTHLYSTATISTICS, "AR Monthly statistics successfully cleared", SQL, "[1376509268]");
		return;
	}
}
