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

public class SMListBidDocuments extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	//OBSOLETE? - this class would only get called if people use an FTP site for their sales lead documents-if they use Google Drive, this class
	//is not needed
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);

	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

    	String sBidNumber = clsManageRequestParameters.get_Request_Parameter("BidNumber", request).trim();
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMListBidDocuments");
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
		String sBidDocURL = "";
		String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
		
		try{
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rsOptions.next()){
				sBidDocURL = rsOptions.getString(SMTablesmoptions.sbiddocsftpurl);
			}else{
				out.println("<B>Could not read " + SMBidEntry.ParamObjectName + " documents URL.</B>");
			}
			rsOptions.close();
		}catch (SQLException e){
			System.out.println("Error reading " + SMBidEntry.ParamObjectName + " docs URL in " + this.toString() + " - " + e.getMessage());
			out.println("<B>Error reading " + SMBidEntry.ParamObjectName + " documents URL: " + e.getMessage() + "</B>");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);

		if (sBidDocURL.trim().compareToIgnoreCase("") == 0){
			out.println("<B>" + SMBidEntry.ParamObjectName + " documents FTP URL is not set in System Options:</B>");
		}else{
		    //Go right to the FTP URL:
			response.sendRedirect(
				sBidDocURL
				+ "/" + sBidNumber
			);	    	
		}

	    return;
	}

}
