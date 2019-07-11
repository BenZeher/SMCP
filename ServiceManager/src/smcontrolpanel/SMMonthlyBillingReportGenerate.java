package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMMonthlyBillingReportGenerate extends HttpServlet {

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
				SMSystemFunctions.SMMonthlyBilling))
		{
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

	    String sStartingDate = "";
	    String sEndingDate = "";
    	if(request.getParameter("DateRange").compareToIgnoreCase("CurrentMonth") == 0){
    		Calendar calendar = Calendar.getInstance();
    		Calendar calFirstDay = Calendar.getInstance();
    		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
    		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
    		Calendar calLastDay = Calendar.getInstance();
    		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
    		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
    	}else{
    		if(request.getParameter("DateRange").compareToIgnoreCase("PreviousMonth") == 0){
        		Calendar calendar = Calendar.getInstance();
        		//Set it back a month:
        		calendar.add(Calendar.MONTH, -1);
        		Calendar calFirstDay = Calendar.getInstance();
        		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
        		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
        		Calendar calLastDay = Calendar.getInstance();
        		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
        		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
    		}else{
    			//User entered dates:
    			
    			sStartingDate = request.getParameter("StartingDate");
    			sEndingDate = request.getParameter("EndingDate");
    		
    			try {
					sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					sWarning = "Error:[1423581189] Invalid starting date: '" + sStartingDate + "' - " + e.getMessage();
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    		
    			try {
					sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					sWarning = "Error:[1423845096] Invalid ending date: '" + sEndingDate + "' - " + e.getMessage();
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    		}
    	}
    	Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sSaleGroupMarker = SMMonthlyBillingReportSelection.SALESGROUP_PARAM;
	    String sServiceTypeMarker = SMMonthlyBillingReportSelection.SERVICETYPE_PARAM;;
    	ArrayList<String> arrSalesGroups = new ArrayList<String>(0);
    	ArrayList<String> arrServiceTypes = new ArrayList<String>(0);
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sSaleGroupMarker)){
			  arrSalesGroups.add(sParamName.substring(sParamName.indexOf(sSaleGroupMarker) + sSaleGroupMarker.length()));
		  }
		  if (sParamName.contains(sServiceTypeMarker)){
			  arrServiceTypes.add(sParamName.substring(sParamName.indexOf(sServiceTypeMarker) + sServiceTypeMarker.length()));
		  }
	    }
	    Collections.sort(arrSalesGroups);
    	if (arrSalesGroups.size() == 0){
    		sWarning = "You must select at least one sales group.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
	    Collections.sort(arrServiceTypes);
    	if (arrServiceTypes.size() == 0){
    		sWarning = "You must select at least one service type.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
		
    	//Show detail?
    	boolean bShowInvoiceDetail = false;
    	if (request.getParameter("Detailed") != null){
    		bShowInvoiceDetail = true;
    	}

    	//Customized title
    	String sReportTitle = "Monthly Billing Report";

    	String sCriteria = "Starting with date <B>" + sStartingDate + "</B>"
    		+ ", ending with date <B>" + sEndingDate + "</B>";
    	
    	//List service types
    	sCriteria	+= ", including service types: ";
    	for (int i = 0; i < arrServiceTypes.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + arrServiceTypes.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + arrServiceTypes.get(i) + "</B>";
    		}
    	}
    	
    	//List sales groups
   		sCriteria += ", including sales groups: ";
    	for (int i = 0; i < arrSalesGroups.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + arrSalesGroups.get(i).substring(
   					0, arrSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
    		}else{
    			sCriteria += ", <B>" + arrSalesGroups.get(i).substring(
       					0, arrSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
    		}
    	}
    	
    	if(bShowInvoiceDetail){
    		sCriteria = sCriteria + ", listing in <B>Detail</B>";
    	}else{
    		sCriteria = sCriteria + ", listing in <B>Summary</B> only.<BR>";
    	}

    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>\n" 
		   +  "<HEAD>"
	       +    SMUtilities.getMasterStyleSheetLink()+"\n"
	       +    "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE>\n"
	       +  "</HEAD>\n<BR>" 
	       +  "<BODY BGCOLOR=\"#FFFFFF\">\n" 
	       +    "<TABLE BORDER=0 WIDTH=100% BGCOLOR= \"" + sColor + "\">\n" 
	       +      "<TR>\n"
	       +        "<TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
	       +          " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMMonthlyBillingReportGenerate") +"</FONT></TD>\n" 
	       +        "<TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD>\n"
	       +      "</TR>\n" 
	       +      "<TR>\n" 
	       +        "<TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
	       +      "</TR>\n" 
	       +      "<TR>\n"
	       +      "<TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD>\n"
	       +      "</TR>\n");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
	   
	    //log usage of this this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMMONTHLYBILLINGREPORT, "REPORT", "SMMonthlyBillingReport", "[1376509329]");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - userID: " 
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

    	SMMonthlyBillingReport amr = new SMMonthlyBillingReport();
    	if (!amr.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate, 
    			arrSalesGroups,
    			arrServiceTypes,
    			bShowInvoiceDetail, 
    			sDBID,
    			sUserID,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + amr.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080590]");
	    out.println("</BODY></HTML>");
	}
}
