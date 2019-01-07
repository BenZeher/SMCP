package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;

public class SMExecuteLockedDBConnection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	
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
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>Killing deadlock database connection - + " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>Killing deadlock database connection - + " + sCompanyName + "</B></FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
    
	   int i = 0;
	   String sConnectionState = ConnectionPool.ConnectionPool.LABELED_CONNECTION_STATUS_BUSY; //by default close busy connection
	   if (request.getParameter(ConnectionPool.ConnectionPool.CONNECTION_ID) != null){
		   i = Integer.parseInt(request.getParameter(ConnectionPool.ConnectionPool.CONNECTION_ID));
	   }else{
		   return;
	   }
	   if (request.getParameter(ConnectionPool.ConnectionPool.CONNECTION_STATE) != null){
		   sConnectionState = request.getParameter(ConnectionPool.ConnectionPool.CONNECTION_STATE);
	   }else{
		   //don't remove any connection
		   return;
	   }
	   if (clsDatabaseFunctions.ExecuteConnection(getServletContext(), i, sConnectionState)){
	   }else{
	   }
	   //redirect back to connection list
	   //refresh connection list
	   out.println("<META http-equiv='Refresh' content='0;URL=" 
			  + SMUtilities.getURLLinkBase(getServletContext()) 
			  + "smcontrolpanel.SMCheckConnectionList"
			  + "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			  + "'>"
		);
	   
	   out.println("</BODY></HTML>");
	}
}
