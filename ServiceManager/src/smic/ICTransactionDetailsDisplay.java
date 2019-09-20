package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICTransactionDetailsDisplay extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
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
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    String sTransactionID = "";
	    sTransactionID = request.getParameter("ICTransactionID");
	    String sOriginalBatchNumber = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMTableictransactions.loriginalbatchnumber, request);
	    String sOriginalEntryNumber = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMTableictransactions.loriginalentrynumber, request);

	    String sReportTitle = "IC Transaction Cost Details";
    	String sCriteria = "Transaction ID <B>" + sTransactionID + "</B><BR>";

    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICTransactionDetailsDisplay") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
    	
		out.println("</TD></TR></TABLE>");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    	);
    	if (conn == null){
    		out.println("Could not get data connection");
        	return;
    	}
    	
    	ICTransactionDetailsReport ictdr = new ICTransactionDetailsReport();
    	try {
			ictdr.processReport(
				conn, 
				sTransactionID,
				sOriginalBatchNumber,
				sOriginalEntryNumber,
				sDBID,
				sUserID,
				out,
				getServletContext(),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			;
		} catch (Exception e) {
			out.println("<B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B>");
		}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080992]");
	    out.println("</BODY></HTML>");
	    return;
	}
}

