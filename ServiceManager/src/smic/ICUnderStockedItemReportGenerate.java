package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICUnderStockedItemReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//formats
	private static final SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICUnderStockedItemReport
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		
		String sWarning = "";
		String sCallingClass = "";
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Log the report usage:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUNDERSTOCKEDITEMREPORT, "REPORT", "ICUnderStockedItemReport", "[1376509413]");
	   
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = "LOCATION";
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
		
	    if (sLocations.size() == 0){
    		sWarning = "You must select at least one location.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
	    
    	String sReportTitle = "IC Under Stocked Item Report";   
    	String sCriteria = "Selected locations: ";
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sLocations.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sLocations.get(i) + "</B>";
    		}
    	}
/*
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    
	    if (bOutputToCSV){
	    	 response.setContentType("text/csv");
             String disposition = "attachment; fileName=poreceiving.csv";
             response.setHeader("Content-Disposition", disposition);
	    }else{
*/
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICUnderStockedItemReportGenerate")
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=4><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPOReceivingReport) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
	    //}
			
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	ICUnderStockedItemReport rpt = new ICUnderStockedItemReport();
    	if (!rpt.processReport(
    			conn, 
    			sLocations,
    			sDBID,
    			sUserID,
    			out,
    			//bOutputToCSV,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + rpt.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081005]");
	    out.println("</BODY></HTML>");
	}
}
