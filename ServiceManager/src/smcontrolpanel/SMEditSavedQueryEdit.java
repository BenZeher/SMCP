package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditSavedQueryEdit  extends HttpServlet {

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

		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				                        + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		//Get Query ID
		String sEditQueryID = clsManageRequestParameters.get_Request_Parameter(SMSavedQueriesSelect.EDIT_QUERY_ID_PARAM, request);
		String sQueryString = "";

		
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
				+ "smcontrolpanel.SMSavedQueryAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");

		
		String SQL = "SELECT"
				+ " " + SMTablesavedqueries.ssql
				+ " FROM " + SMTablesavedqueries.TableName
				+ " WHERE " + SMTablesavedqueries.id + " = " + sEditQueryID
				;

			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".loadMechanics - user: " 
					+ sUserFullName	);
				if (rs.next()){
					sQueryString = rs.getString(SMTablesavedqueries.ssql);
				}
				rs.close();
			} catch (Exception e) {
				out.println("Error [1391538712] - Could not load sql statement - " + e.getMessage());
			}

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

