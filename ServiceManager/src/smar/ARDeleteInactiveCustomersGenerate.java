package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	private String sWarning = "";
	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)+ " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
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
    	if (!deleteInactives(getServletContext(), sDBID)){
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
    	//If there was no error message, simply advise that the delete process was successful:
    	if (sWarning.trim().compareToIgnoreCase("") == 0){
    		sWarning = "ALL inactive customers successfully deleted.";
    	}else{
    		sWarning = sWarning + "<BR>All other inactive customers were successfully deleted.";
    	}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	
	private boolean deleteInactives(ServletContext context, String sConf){
		
		String SQL = "SELECT "
			+ SMTablearcustomer.sCustomerNumber
			+ " FROM " + SMTablearcustomer.TableName
			+ " WHERE " + SMTablearcustomer.iActive + " = 0"
			;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sConf,
				"MySQL",
				this.toString() + ".deleteInactives - User: " + sUserID
				+ " - "
				+ sUserFullName
				);
			
			Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sConf, 
				"MySQL", 
				this.toString() + ".deleteInactives - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
			
			if (conn == null){
				sWarning = "Could not get connection to delete inactives.";
				return false;
			}
			while (rs.next()){
				ARCustomer cust = new ARCustomer(rs.getString(SMTablearcustomer.sCustomerNumber));
				//If a customer cannot be deleted, add it to the warning string, but go on:
				if (!cust.delete(rs.getString(SMTablearcustomer.sCustomerNumber), conn)){
					if (sWarning.trim().compareToIgnoreCase("") == 0){
						sWarning = " Could not delete " + cust.getM_sCustomerNumber() + ": " 
						+ cust.getErrorMessageString();
					}else{
						sWarning = sWarning + "<BR>" + " Could not delete " 
						+ cust.getM_sCustomerNumber() + cust.getErrorMessageString();
					}
				}
			}
			rs.close();
			clsDatabaseFunctions.freeConnection(context, conn);
		}catch (SQLException e){
			System.out.println("Error deleting inactives - " + e.getMessage() + ".");
			sWarning = "Error deleting inactives - " + e.getMessage() + ".";
			return false;
		}
		
		return true;
	}

}
