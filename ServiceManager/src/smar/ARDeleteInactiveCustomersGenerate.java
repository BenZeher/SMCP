package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ARDeleteInactiveCustomersGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARDeleteInactiveCustomers)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)+ " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    /**************Get Parameters**************/

    	//Customized title
    	String sTitle = "Delete Inactive Customers";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sTitle + "</B></FONT></TD></TR>"
		   );
		   
    	//Delete inactives:
    	String sDeleteErrors = "";
    	try {
    		sDeleteErrors = deleteInactives(getServletContext(), sDBID, sUserID, sUserFullName);
		} catch (Exception e) {
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	//If there was no error message, simply advise that the delete process was successful:
    	if (sDeleteErrors.trim().compareToIgnoreCase("") == 0){
    		sWarning = "ALL inactive customers successfully deleted.";
    	}else{
    		sWarning = sDeleteErrors + "<BR>All other inactive customers were successfully deleted.";
    	}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	
	private String deleteInactives(ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
		
		String sErrors = "";
		
		String SQL = "SELECT "
			+ SMTablearcustomer.sCustomerNumber
			+ " FROM " + SMTablearcustomer.TableName
			+ " WHERE " + SMTablearcustomer.iActive + " = 0"
			;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".deleteInactives - User: " + sUserID
				+ " - "
				+ sUserFullName
				);
			
			Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".deleteInactives - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
			
			if (conn == null){
				throw new Exception("Could not get connection to delete inactives.");
			}
			while (rs.next()){
				ARCustomer cust = new ARCustomer(rs.getString(SMTablearcustomer.sCustomerNumber));
				//If a customer cannot be deleted, add it to the warning string, but go on:
				if (!cust.delete(rs.getString(SMTablearcustomer.sCustomerNumber), conn)){
					if (sErrors.trim().compareToIgnoreCase("") == 0){
						sErrors = " Could not delete " + cust.getM_sCustomerNumber() + ": " 
						+ cust.getErrorMessageString();
					}else{
						sErrors = sErrors + "<BR>" + " Could not delete " 
						+ cust.getM_sCustomerNumber() + cust.getErrorMessageString();
					}
				}
			}
			rs.close();
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067526]");
		}catch (Exception e){
			throw new Exception("Error [1548695957] deleting inactives - " + e.getMessage() + ".");
		}
		
		return sErrors;
	}

}
