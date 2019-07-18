package smar;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ARSetInactiveCustomersAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARSetInactiveCustomers))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    					+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sWarning = "";
	    
    	//Get connection:
    	Enumeration <String> paramNames = request.getParameterNames();
	    String sCustomerNumber = "";
	    String SQL = "";
	    String sSetActiveFlagTo = clsManageRequestParameters.get_Request_Parameter("SETACTIVEFLAGTO", request);
	    if (sSetActiveFlagTo.trim().compareToIgnoreCase("") == 0){
	    	sWarning = "SETACTIVEFLAGTO is blank";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smar.ARSetInactiveCustomersSelection" + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;	    	
	    }
	    while(paramNames.hasMoreElements()) {
			String sParamName = paramNames.nextElement();
			if (sParamName.contains("CUSTNUM")){
				if (request.getParameter(sParamName) != null){
					sCustomerNumber = sParamName.replace("CUSTNUM", "");
					SQL = "UPDATE "
						+ SMTablearcustomer.TableName
						+ " SET " + SMTablearcustomer.iActive
						+ " = " + sSetActiveFlagTo
						+ ", " + SMTablearcustomer.datLastMaintained + " = NOW()"
						+ ", " + SMTablearcustomer.sLastEditUserFullName + " = '" + sUserFullName + "'"
						+ ", " + SMTablearcustomer.lLastEditUserID + " = " + sUserID + ""
						+ " WHERE " + SMTablearcustomer.sCustomerNumber
							+ " = '" + sCustomerNumber + "'"
					;
					try{
						if (!clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID)){
							sWarning = "Could not set customer " + sCustomerNumber + " to inactive.";
				    		response.sendRedirect(
				    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smar.ARSetInactiveCustomersSelection" + "?"
				    				+ "Warning=" + sWarning
				    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				    		);			
				        	return;
						}
					}catch(SQLException e){
						sWarning = "Could not set customer " + sCustomerNumber + " to inactive - error:"
							+ e.getMessage() + ".";
			    		response.sendRedirect(
			    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smar.ARSetInactiveCustomersSelection" + "?"
			    				+ "Warning=" + sWarning
			    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		);			
			        	return;
					}
				}
			}
	    }
	    SMLogEntry SMLog = new SMLogEntry(sDBID, getServletContext());
	    SMLog.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARSETINACTIVECUSTOMER, "Customer " + sCustomerNumber, SQL, "[1376509288]");
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smar.ARSetInactiveCustomersSelection" + "?"
				+ "Warning=" + "Selected customers successfully set to inactive."
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
}
