package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;


public class SMCheckConnectionList extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>Connection Pool Status - + " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>Connection Pool Status - + " + sCompanyName + "</B></FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
    	
	   out.println("<TABLE WIDTH=100% BORDER=0>");
	   String sExecutioner = "";
	   sExecutioner = "" + SMUtilities.getURLLinkBase(getServletContext()) 
				  	+ "smcontrolpanel.SMExecuteLockedDBConnection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID			   
				  	+ "&" + ConnectionPool.ConnectionPool.CONNECTION_ID + "=";
	   try{
		   ArrayList<String> alConnectionStatus = clsDatabaseFunctions.getConnectionStatus(getServletContext(), sExecutioner);
		   
		   for (int i=0; i<alConnectionStatus.size(); i++){
			   out.println("<TR><TD ALIGN=LEFT>" + (String)alConnectionStatus.get(i) + "</TD><TR>");
		   }
		   out.println("</TABLE>");
		   
		   //add dummy connection into the queue.
		   out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.addNewConnection?BUSY=0&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">add available connection</A>&nbsp;&nbsp;&nbsp;&nbsp;");
		   out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.addNewConnection?BUSY=1&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">add busy connection</A>");
		   
		   out.println("</BODY></HTML>");
	   }catch(Exception ex){
		   out.println("Error: No connection pool available.");
		   out.println(ex.getMessage());
		   return;
	   }
	}
}
