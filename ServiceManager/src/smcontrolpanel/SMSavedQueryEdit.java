package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMSavedQueryEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String PARAM_SAVEQUERY = "SAVEQUERY";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMQuerySelector
		)
		){
			return;
		}
		//Get Query ID
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYID, request);
		if (sQueryID.compareToIgnoreCase("") == 0){
			sQueryID = "";
		}
		
		//Get the query passed in.
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, request);
		if (sQueryString.compareToIgnoreCase("") == 0){
			sQueryString = "";
		}
		
		//If it's a request to save the record, do that.
		if(request.getParameter(PARAM_SAVEQUERY) != null){
			try{
	    		String SQL = "UPDATE " + SMTablesavedqueries.TableName
	    			+ " SET " + SMTablesavedqueries.ssql + " = "
	    			+ sQueryString
	    			+ " WHERE ("
	    			+ SMTablesavedqueries.id " = " 
	    			+ ")"
		    	;
		    		
	    		if (!clsDatabaseFunctions.executeSQL(
	    				SQL, 
	    				getServletContext(), 
	    				sDBID,
	    				"MySQL",
	    				this.toString() + ".doGet - User: " + sUserFullName)){
	    			//bResetSuccessful = false;
	    			sWarning = "Could not execute update statement.";
	    		}
	    	}catch (SQLException e){
	    		//bResetSuccessful = false;
	    		//sWarning = "Error updating flag - " + e.getMessage();
	    	}
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String title = "SM Edit Saved Query";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMExecuteSQL) 
				+ "\">Summary</A><BR><BR>");
		//Link to the data definitions mapping:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayDataDefs?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Display data definitions</A>"
				);

		
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMSavedQueryEdit\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");

		//Multi-line text box here for the command:
		out.println("<B><U>Edit query below:</U></B><BR>");
		out.println("<TEXTAREA NAME=\"" + SMQuerySelect.PARAM_QUERYSTRING + "\""
				+ " rows=\"" + "20" + "\""
				+ " cols=\"" + "120" + "\""
				+ ">" + sQueryString + "</TEXTAREA>"
		);

		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=\""  + PARAM_SAVEQUERY + "\" VALUE=\"----Save query----\">");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
