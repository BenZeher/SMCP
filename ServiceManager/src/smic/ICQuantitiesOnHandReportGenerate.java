package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICQuantitiesOnHandReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICQuantitiesOnHandReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sWarning = "";
	    String sCallingClass = "";
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sStartingItem = "";
	    String sEndingItem = "";
	    
	    sStartingItem = request.getParameter("StartingItemNumber");
	    sEndingItem = request.getParameter("EndingItemNumber");
	    
    	String sReportTitle = "IC Quantities On Hand Report";

    	String sCriteria = "Starting with item <B>" + sStartingItem + "</B>"
    		+ ", ending with item <B>" + sEndingItem + ".</B>";
    	
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    
	    if (bOutputToCSV){
	    	 response.setContentType("text/csv");
             String disposition = "attachment; fileName=icqtysonhand.csv";
             response.setHeader("Content-Disposition", disposition);
	    }else{
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
			   "Transitional//EN\">" +
		       "<HTML>" +
		       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
			   "<BODY BGCOLOR=\"#FFFFFF\">" +
			   "<TABLE BORDER=0 WIDTH=100%>" +
			   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
			   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICQuantitiesOnHandReportGenerate") 
			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
			   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
			   
			   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
					   
	    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICQuantitiesOnHandReport) 
		    	+ "\">Summary</A><BR><BR>");
			out.println("</TD></TR></TABLE>");
	    }		
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
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
    	ICQuantitiesOnHandReport itemval = new ICQuantitiesOnHandReport();
    	if (!itemval.processReport(
    			conn, 
    			sStartingItem, 
    			sEndingItem, 
    			sDBID,
    			sUserID,
    			out,
    			bOutputToCSV,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
    	)){
    		out.println("Could not print report - " + itemval.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080960]");
	    out.println("</BODY></HTML>");
	}
}
