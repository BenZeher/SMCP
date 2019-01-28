package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;

public class SMExecuteSQLSelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String PARAM_EXECUTESTRING = "EXECUTESTRING";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMExecuteSQL
		)
		){
			return;
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String title = "SM Execute SQL Command";
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
		
		String sExecuteString = clsManageRequestParameters.get_Request_Parameter(PARAM_EXECUTESTRING, request);
		if (sExecuteString.compareToIgnoreCase("") == 0){
			sExecuteString = "";
		}
		
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMExecuteSQLAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");

		//Multi-line text box here for the command:
		out.println("<B><U>Enter SQL command below:</U></B><BR>");
		out.println("<TEXTAREA NAME=\"" + PARAM_EXECUTESTRING + "\""
				+ " rows=\"" + "20" + "\""
				+ " cols=\"" + "120" + "\""
				+ ">" + sExecuteString + "</TEXTAREA>"
		);

		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Execute command----\">");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
