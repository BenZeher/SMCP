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

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICItemNumberMatchUpGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	private String sCompanyName = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICItemNumberMatchUp
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Validate the entries:
	    String sStartingItem = clsManageRequestParameters.get_Request_Parameter(
	    		ICItemNumberMatchUpCriteriaSelection.STARTINGITEM_PARAM, request);
	    String sEndingItem = clsManageRequestParameters.get_Request_Parameter(
	    		ICItemNumberMatchUpCriteriaSelection.ENDINGITEM_PARAM, request);
	    String sSortbyOurItem = clsManageRequestParameters.get_Request_Parameter(
	    		ICItemNumberMatchUpCriteriaSelection.SORTBYOURITEM_PARAM, request); //either 0-our or 1-vendor. 
	    
	    //Validate entries:
	    sWarning = "";
	    if (sStartingItem.compareToIgnoreCase(sEndingItem) >0 ){
	    	sWarning += "Starting item cannot be greater than ending item.  ";
	    }
	    
		//Construct the query string in case we have to redirect back:
		String sRedirectParams =
			"&" + ICItemNumberMatchUpCriteriaSelection.STARTINGITEM_PARAM + "=" +sStartingItem
			+ "&" + ICItemNumberMatchUpCriteriaSelection.ENDINGITEM_PARAM + "=" +sEndingItem
			+ "&" + ICItemNumberMatchUpCriteriaSelection.SORTBYOURITEM_PARAM + "=" +sSortbyOurItem
		;
		
		//If there's any kind of error, return with the warning:
		if(sWarning.compareToIgnoreCase("") != 0){
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sRedirectParams
    		);			
        	return;
		}
	    
    	String sReportTitle = "Item Number Match-Up List";    	
    	String sCriteria = "Starting with item '<B>" + sStartingItem + "</B>'"
    		+ ", ending with '<B>" + sEndingItem + "</B>'";

   		sCriteria += ", sorted by: ";
   		if (sSortbyOurItem.compareTo("0") == 0){
   			sCriteria += "our item number.";
   		}else{
   			sCriteria += "vendor item number.";
   		}
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserID 
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
    			SMUtilities.getFullClassName(this.toString()) + " - user: " 
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
    				+ sRedirectParams	
    		);			
        	return;
    	}

    	ICItemNumberMatchUpList rpt = new ICItemNumberMatchUpList();
    	if (!rpt.processReport(conn,
							   sStartingItem,
							   sEndingItem,
							   sSortbyOurItem,
							   sDBID,
							   sUserID,
							   out,
							   getServletContext(),
							   (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
			{
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080864]");
				response.sendRedirect(
	    			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    			+ "Warning=" + rpt.getErrorMessage()
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    			+ sRedirectParams	
	    		);
    		return;	
    	}
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080865]");
    		out.println("</BODY></HTML>");
	}
}
