package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecriticaldates;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMCriticalDateReportGenerate extends HttpServlet {

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
			SMSystemFunctions.SMCriticaldatesreport)
			){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    
    	//get current URL
    	String sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString()+ "?" + request.getQueryString()).replace("&", "*");

    	//Get Start and end Dates
    	String sStartingDate = request.getParameter("StartingDate");
	    String sEndingDate = request.getParameter("EndingDate");
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCRITICALDATEREPORT, "REPORT", "Ran Critical Date report for"+sStartingDate+" - "+sEndingDate, "[1535653241]");

		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Invalid starting date: '" + sStartingDate + "' - " + e.getMessage();
			redirectAfterError(response, sCallingClass, sDBID, sWarning);			
            return;
		}
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Invalid ending date: '" + sEndingDate + "' - " + e.getMessage();
			redirectAfterError(response, sCallingClass, sDBID, sWarning);			
            return;
		}

	    //Get types, status, and users
	    ArrayList<String> alTypes = new ArrayList<String>(0);
	    ArrayList<String> alStatus = new ArrayList<String>(0);
    	ArrayList<String> alSelectedUsers = new ArrayList<String>(0);
    	ArrayList<String> arrSalesGroupCodes = new ArrayList<String>(0);
    	ArrayList<String> arrSalesLeadStatusCodes = new ArrayList<String>(0);
    	ArrayList<String> arrFullUserNames = new ArrayList<String>(0);
	    Enumeration<String> paramNames = request.getParameterNames();
	    String sUsersMarker = SMCriticalDateReportCriteriaSelection.UserMarker;
	    String sTypesMarker = SMCriticalDateReportCriteriaSelection.TypeMarker;
	    String sStatusMarker = SMCriticalDateReportCriteriaSelection.StatusMarker;
	    String sSalesGroupMarker = SMCriticalDateReportCriteriaSelection.SALESGROUP_PARAM;
	    String sSalesLeadStatusMarker = SMCriticalDateReportCriteriaSelection.SALESLEADSTATE_PARAM;
	    
	    boolean bUserChoseToPrintOrderTypes = false;
	    boolean bUserChoseToPrintSalesLeadTypes = false;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = paramNames.nextElement();
		  if (sParamName.contains(sUsersMarker)){
			  String sSelectedUserID = sParamName.substring(sParamName.indexOf(sUsersMarker) + sUsersMarker.length());
			  alSelectedUsers.add(sSelectedUserID);
			  arrFullUserNames.add(
				  clsManageRequestParameters.get_Request_Parameter(SMCriticalDateReportCriteriaSelection.FULL_NAME_PARAMETER_BASE + sSelectedUserID , request)
			);
		  }
		  if (sParamName.contains(sTypesMarker)){
			  alTypes.add(sParamName.substring(sParamName.indexOf(sTypesMarker) + sTypesMarker.length()));
			  //If the user chose to print ORDER critical dates, note that:
			  if (Integer.parseInt(alTypes.get(alTypes.size() - 1)) == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE){
				  bUserChoseToPrintOrderTypes = true;
			  }
			  //If the user chose to print SALES LEAD critical dates, note that:
			  if (Integer.parseInt(alTypes.get(alTypes.size() - 1)) == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE){
				  bUserChoseToPrintSalesLeadTypes = true;
			  }
		  }
		  if (sParamName.contains(sStatusMarker)){
			  alStatus.add(sParamName.substring(sParamName.indexOf(sStatusMarker) + sStatusMarker.length()));
		  }
		  if (sParamName.contains(sSalesGroupMarker)){
			  arrSalesGroupCodes.add(sParamName.substring(sParamName.indexOf(sSalesGroupMarker) + sSalesGroupMarker.length()));
		  }
		  if (sParamName.contains(sSalesLeadStatusMarker)){
			  arrSalesLeadStatusCodes.add(sParamName.substring(sParamName.indexOf(sSalesLeadStatusMarker) + sSalesLeadStatusMarker.length()));
		  }
	    }
	    if (alSelectedUsers.size() == 0 || alTypes.size() == 0 || alStatus.size() == 0 ){
	    	if(alSelectedUsers.size() == 0) {
	    		sWarning = "You must select at least one user. ";	
	    	}
	    	if(alTypes.size() == 0) {
	    		sWarning += "You must select at least one type. ";	
	    	}
	    	if(alStatus.size() == 0) {
	    		sWarning += "You must select at least one status. ";	
	    	}
	    	
	    	//If the user chose to list critical dates for ORDERS, then they must pick at least one Sales Group:
	    	if (bUserChoseToPrintOrderTypes){
		    	if (arrSalesGroupCodes.size() == 0){
		    		sWarning += "You must select at least one sales group. ";
		    	}
	    	}
	    	if (bUserChoseToPrintSalesLeadTypes){
		    	if (arrSalesLeadStatusCodes.size() == 0){
		    		sWarning += "You must select at least one sales lead status. ";
		    	}
	    	}
    	}
	    Collections.sort(alSelectedUsers);
	    Collections.sort(arrSalesGroupCodes);
	    Collections.sort(arrSalesLeadStatusCodes);
	    Collections.sort(arrFullUserNames);
	    
    	//save URL History
		String sURLTitle = "Critical Date Report (from " + sStartingDate + " to " + sEndingDate + ", users: ";
		
		if (alSelectedUsers.size() <= 3){
			for (int i=0;i < alSelectedUsers.size();i++){
				sURLTitle += alSelectedUsers.get(i).toString() + ","; 
			}
			sURLTitle = sURLTitle.substring(0, sURLTitle.length() - 1);
		}else{
			for (int i=0;i<4;i++){
				sURLTitle += alSelectedUsers.get(i).toString() + ","; 
			}
			sURLTitle = sURLTitle.substring(0, sURLTitle.length() - 1) + "....";
		}
		sURLTitle += ")";
		
		CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY,
									SMUtilities.updateURLHistory(sURLTitle, 
																 sCurrentURL, 
																 CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY),
																 CurrentSession.getAttribute("URLMaxSize"))
																 );
	    
	    //Get sort order
	    String sSelectedSortOrder = request.getParameter(SMCriticalDateReportCriteriaSelection.ParamSelectedSortOrder);
	    
	    //Get 'Assigned By':
	    String sAssignedBy = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamAssignedbyUserID, request);

    	//Customized title
    	String sReportTitle = "Critical Date Report";

    	String sCriteria = "Starting with date <B>" + sStartingDate + "</B>" +
    					   ", ending with date <B>" + sEndingDate + "</B>" +
    					   "<BR>Including user ids: ";
    	
    	for (int i = 0; i < arrFullUserNames.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + arrFullUserNames.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + arrFullUserNames.get(i) + "</B>";
    		}
    	}
    	;
   				
   		//Show the sales groups, if any:
   		if (bUserChoseToPrintOrderTypes){
   			sCriteria += "<BR>For order sales groups: ";
   			for (int i = 0; i < arrSalesGroupCodes.size(); i++){
   				if (i == 0){
   					sCriteria += "<B>" + arrSalesGroupCodes.get(i) + "</B>";
   				}else{
   					sCriteria += ", <B>" + arrSalesGroupCodes.get(i) + "</B>";
   				}
   			}
   		}
   		
   		if (bUserChoseToPrintSalesLeadTypes){
   			sCriteria += "<BR>For sales lead status: ";
   			for (int i = 0; i < arrSalesLeadStatusCodes.size(); i++){
   				if (i == 0){
   					sCriteria += "<B>" + arrSalesLeadStatusCodes.get(i) + "</B>";
   				}else{
   					sCriteria += ", <B>" + arrSalesLeadStatusCodes.get(i) + "</B>";
   				}
   			}
   		}
   				
   		sCriteria += "<BR>Sorted by ";
   		
   		if (sSelectedSortOrder.compareToIgnoreCase(SMCriticalDateReportCriteriaSelection.ParamSortByDate) == 0){
   			sCriteria += "<B>Critical date </B>";
   		}else if (sSelectedSortOrder.compareToIgnoreCase(SMCriticalDateReportCriteriaSelection.ParamSortByType) == 0){
   			sCriteria += "<B>Document type </B>";
   		}else{
   			//error
   			//System.out.println("Error getting sort by option.");
   		}
   		sCriteria += ".";
   		
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR=\"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserFirstName + " " + sUserLastName
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=4 ><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
	   				   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to...</A><BR></TD>");
	   
	   out.println("</TR></TABLE>");
	   
	    //log usage of this report
	  
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID, 
    		"MySQL", 
    		this.toString() 
    		+ " - user: " 
    		+ sUserID
    		+ " - "
    		+ sUserFirstName
    		+ " "
    		+ sUserLastName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		redirectAfterError(response, sCallingClass, sDBID, sWarning);
    		return;
    	}

    	SMCriticalDateReport cdr = new SMCriticalDateReport();
    	if (!cdr.processReport(
    			conn, 
    			sStartingDate,
    			sEndingDate, 
    			alSelectedUsers,
    			alTypes,
    			alStatus,		
    			arrSalesGroupCodes,
    			arrSalesLeadStatusCodes,
    			sSelectedSortOrder,
    			sAssignedBy,
    			sCurrentURL,
    			sDBID,
    			sUserID,
    			out,
    			getServletContext())){
    		out.println("Could not print report - " + cdr.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080430]");
	    out.println("</BODY></HTML>");
	}
	private void redirectAfterError(HttpServletResponse res, String sCallingClass, String sDBID, String sWarning){
		
		String sRedirect =
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + sWarning
			;
		
		sRedirect += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		try {
			res.sendRedirect(sRedirect);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
        	return;
	}
}
