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
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMWageScaleReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMWageScaleReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    String sStatus = "";
	    //If it's a Delete:
	    if(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.DELETE_BUTTON_LABEL, request)
				   .compareToIgnoreCase(SMWageScaleDataEntry.DELETE_BUTTON_VALUE) == 0){
	       
		    if (clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.CONFIRM_DELETE_CHECKBOX, request)
					   .compareToIgnoreCase(SMWageScaleDataEntry.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Now Delete
			    try {
					SMWageScaleDataEntry wsde = new SMWageScaleDataEntry();
					wsde.delete(getServletContext(), sDBID, sUserID, sUserFullName);
				} catch (Exception e) {
					sWarning = "Could not delete: " + e.getMessage();
		    		response.sendRedirect(
		    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    				+ "Warning=" + sWarning
		    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
						return;
				}
			    //If the Delete Succeeded 
			    sStatus = "ALL wage scale records deleted successfully";
			    response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ "Status=" + sStatus
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
			    return;
			    
	    	}else{
	    		sWarning = "You chose to delete, but did not check the CONFIRM checkbox.";
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ "Warning=" + sWarning
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
				return;
	    	}
		    
	    }
	    
		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter("ORDERNUMBER", request).trim();
		if (sOrderNumber.compareToIgnoreCase("") == 0){
			sWarning = "Order number is missing. Please provide a valid order number.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}else{
			try {
				validateOrder(sOrderNumber, sDBID, sUserID, sUserFullName);
			} catch (Exception e) {
				sWarning = "Invalid order number '" + sOrderNumber + "':" + e.getMessage();
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ "Warning=" + sWarning
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	        		);			
	    		return;
			}
		}
		
	    String sPeriodEndDate = request.getParameter("PeriodEndDate");
		if(!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sPeriodEndDate)){
			sWarning = "Invalid period end date: '" + sPeriodEndDate + "'";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		//Make sure Encryption Key was entered
		String sDecryptionKey = request.getParameter(SMWageScaleDataEntry.ParamEncryptionKey);
		if (sDecryptionKey.compareToIgnoreCase("") == 0 || sDecryptionKey == null){
			sWarning = "Encryption key is missing. ";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		
		//Validate Encryption Key 
		SMWageScaleDataEntry wsde = new SMWageScaleDataEntry();
		if(!wsde.validate_encryption_key(getServletContext(), sDBID, sUserID, sUserFullName,  sDecryptionKey )){
			sWarning = "Encryption Is Invalid. ";
	    	response.sendRedirect(
	    			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    			+ "Warning=" + sWarning
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);	
	    		return;
			}		
		
    	//Customized title
    	String sReportTitle = "Wage Scale Report";
    	
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">");
    	
 	   //log usage of this this report
 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMWAGESCALEREPORT, "REPORT", "SMWageScaleReport", "[1376509367]");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMWageScaleReportGenerate");
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}

    	SMWageScaleReport wsr = new SMWageScaleReport();
    	if (!wsr.processReport(
    		conn, 
    		sPeriodEndDate, 
    		sOrderNumber, 
    		sCompanyName,
    		out, 
    		getServletContext(),
    		sDecryptionKey)
    		){
    		out.println("Could not print report - " + wsr.getErrorMessage());
    	}

    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080684]");
	    out.println("</BODY></HTML>");
	}
	
	private void validateOrder(String sOrderNumber, String sDBID, String sUserID, String sUserFullName) throws Exception{
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sOrderNumber.trim());
		if (!order.load(getServletContext(), sDBID, sUserID, sUserFullName)){
			throw new Exception("Could not load order to check.<BR>" + order.getErrorMessages());
		}else{
			//order is valid. if there is anything else need to be checked, list them below.
		}
	}
}
