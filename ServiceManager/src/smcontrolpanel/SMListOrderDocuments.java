package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMListOrderDocuments extends HttpServlet {
	//OBSOLETE?  This only works if people are using FTP sites for their documents - otherwise, if they are using Google Drive, this
	//function is never called.
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
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
    	String sOrderNumber = clsManageRequestParameters.get_Request_Parameter("OrderNumber", request).trim();
		//System.out.println("SMListOrderDocuments: sOrderNumber = '" + sOrderNumber + "'");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMDisplayOrderInformation");
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
		String sOrderDocURL = "";
		String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
		
		try{
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rsOptions.next()){
				sOrderDocURL = rsOptions.getString(SMTablesmoptions.sorderdocsftpurl);
			}else{
				out.println("<B>Could not read order documents URL.</B>");
			}
			rsOptions.close();
		}catch (SQLException e){
			System.out.println("[1579273275] Error reading order docs URL in " + this.toString() + " - " + e.getMessage());
			out.println("<B>Error reading order documents URL: " + e.getMessage() + "</B>");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080582]");

		if (sOrderDocURL.trim().compareToIgnoreCase("") == 0){
			out.println("<B>Order documents FTP URL is not set in System Options:</B>");
		}else{
		    //Go right to the FTP URL:
			response.sendRedirect(
				sOrderDocURL
				+ "/" + sOrderNumber
			);	    	
		}
	    

    	return;
	}
	

}
