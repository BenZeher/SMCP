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
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICPrintPhysicalInventoryVarianceReport extends HttpServlet {

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
				SMSystemFunctions.ICEditPhysicalInventory))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //local variables
		String sWarning = "";
		String sCallingClass = "";
		boolean bOnlyShowVariances = false;
		boolean bSummaryOnly = false;
		boolean bInactiveOnly = false;
		
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    if (clsManageRequestParameters.get_Request_Parameter("OnlyShowVariances", request).compareToIgnoreCase("yes") == 0){
	    	bOnlyShowVariances = true;
	    }else{
	    	bOnlyShowVariances = false;
	    }
	    
	    if (clsManageRequestParameters.get_Request_Parameter("Summary", request).compareToIgnoreCase("yes") == 0){
	    	bSummaryOnly = true;
	    }else{
	    	bSummaryOnly = false;
	    }
	    
	    if (clsManageRequestParameters.get_Request_Parameter("OnlyInactive", request).compareToIgnoreCase("yes") == 0){
	    	bInactiveOnly = true;
	    }else{
	    	bInactiveOnly = false;
	    }
	    
	    String sPhysicalInventoryID = "";
	    sPhysicalInventoryID = request.getParameter(ICPhysicalInventoryEntry.ParamID);

	    String sReportTitle = "IC Physical Inventory Variance";
    	String sCriteria = "Physical Inventory #'<B>" + sPhysicalInventoryID + "</B>'<BR>";
    	sCriteria += "<B>NOTE:</B>&nbsp;'Qty on hand' indicates the quantity that WAS on hand when this "
    		+ "physical inventory was created and is not affected by subsquent transactions.";

    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserFullName
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
    	
	    //Add a link to physical inventory list:
	    out.println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICListPhysicalInventories?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to physical inventory list</A><BR>");
	    
	    //Add a link to parent physical inventory:
	    out.println(
		    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
		    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to physical inventory</A><BR>");
    	
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + " - user: " + sUserID
    			+ " - " + sUserFullName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	ICPhysicalInventoryVarianceReport icvar = new ICPhysicalInventoryVarianceReport();
    	if (!icvar.processReport(
    			conn, 
    			sPhysicalInventoryID, 
    			sUserID,
    			sDBID,
    			sUserFullName,
    			bOnlyShowVariances,
    			bSummaryOnly,
    			bInactiveOnly,
    			out,
    			getServletContext())){
    		out.println("Could not print report - " + icvar.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080947]");
	    out.println("</BODY></HTML>");
	}
}

