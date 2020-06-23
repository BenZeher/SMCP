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
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICItemValuationReportGenerate extends HttpServlet {

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
				SMSystemFunctions.ICItemValuationReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //local variables
		String sWarning = "";
		String sCallingClass = "";
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sStartingItem = "";
	    String sEndingItem = "";
	    boolean bShowIndividualBuckets = false;
	    boolean bShowIndividualLocations = false;
	    
	    sStartingItem = request.getParameter("StartingItemNumber");
	    sEndingItem = request.getParameter("EndingItemNumber");
	    
	    if (request.getParameter("ShowIndividualBuckets") != null){
	    	bShowIndividualBuckets = true;
	    }
	    
	    if (request.getParameter("ShowIndividualLocations") != null){
	    	bShowIndividualLocations = true;
	    }
	    
	    
	    int iIncludingQuantities = Integer.parseInt(request.getParameter("IncludingQuantities"));
	    int iIncludingCosts = Integer.parseInt(request.getParameter("IncludingCosts"));
	    
	   	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramNames = request.getParameterNames();
	    String sMarker = "LOCATION";
	    while(paramNames.hasMoreElements()) {
	      String sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sLocations.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
	    
    	String sReportTitle = "Item Valuation Report";
    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);

    	String sCriteria = "Starting with item <B>" + sStartingItem + "</B>"
    		+ ", ending with item <B>" + sEndingItem + "</B>";
    	
    	if (bShowIndividualBuckets){
    		sCriteria = sCriteria + ", <B>SHOWING</B> individual cost buckets";
    	}else {
    		sCriteria = sCriteria + ", <B>NOT SHOWING</B> individual cost buckets";
    	}

    	if (bShowIndividualLocations){
    		sCriteria = sCriteria + ", <B>SHOWING</B> individual locations";
    	}else {
    		sCriteria = sCriteria + ", <B>NOT SHOWING</B> individual locations";
    	}
    	
    	String sIncludingQtyString = "";
    	switch (iIncludingQuantities){
	    	case 0:
	    		sIncludingQtyString = "All"; break;
	    	case 1:
	    		sIncludingQtyString = "Only Positive"; break;
	    	case 2:
	    		sIncludingQtyString = "Only Zero"; break;
	    	case 3:
	    		sIncludingQtyString = "Only Negative"; break;
	    	case 4:
	    		sIncludingQtyString = "Non Zero"; break;
	    	default: 
	    		sIncludingQtyString = "All"; break;
    	}
    	sCriteria = sCriteria + ", including <B>" + sIncludingQtyString + "</B> quantities";

    	String sIncludingCostString = "";
    	switch (iIncludingCosts){
    	case 0:
    		sIncludingCostString = "All"; break;
    	case 1:
    		sIncludingCostString = "Only Positive"; break;
    	case 2:
    		sIncludingCostString = "Only Zero"; break;
    	case 3:
    		sIncludingCostString = "Only Negative"; break;
    	case 4:
    		sIncludingCostString = "Non Zero"; break;
    	default: 
    		sIncludingCostString = "All"; break;
	}
    	sCriteria = sCriteria + ", including <B>" + sIncludingCostString + "</B> costs";
    	
    	sCriteria = sCriteria + 
    		", including locations:"
    		;
    	
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria = sCriteria + " <B>" + sLocations.get(i) + "</B>";
    		}else{
    			sCriteria = sCriteria + ", <B>" + sLocations.get(i) + "</B>";
    		}
    	}
    	sCriteria = sCriteria + ".";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICItemValuationReportGenerate")
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICItemValuationReport) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
    	
		if(sLocations.size() == 0){
    		sWarning = "No locations selected.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
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
    			+ sUserFullName
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
    	out.println(SMUtilities.getMasterStyleSheetLink());


    	ICItemValuationReport itemval = new ICItemValuationReport();
    	if (!itemval.processReport(
    			conn, 
    			sStartingItem, 
    			sEndingItem, 
    			bShowIndividualBuckets,
    			bShowIndividualLocations,
    			iIncludingQuantities,
    			iIncludingCosts,
    			sLocations,
    			sDBID,
    			sUserID,
    			sUserFullName,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + itemval.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080867]");
	    out.println("</BODY></HTML>");
	}
}
