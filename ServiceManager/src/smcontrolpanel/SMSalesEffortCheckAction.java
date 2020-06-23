package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMSalesEffortCheckAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
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
	   // String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	   // String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	   // String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	   // String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
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
					sWarning = "Error:[1423661922] Invalid Starting date: '" + sStartingDate + "' - " + e.getMessage();
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
					sWarning = "Error:[1423661923] Invalid Ending date: '" + sEndingDate + "' - " + e.getMessage();
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    		}
    	}

	    //Sales types:
    	/*
    	boolean bIncludeSales = false;
    	boolean bIncludeService = false;
    	boolean bShowIndividualOrders = false;
    	String sServiceTypes = "";
		if(request.getParameter("SalesType").compareToIgnoreCase("Sales") == 0){
			sServiceTypes = "Sales";
			bIncludeSales = true;
		}
		if(request.getParameter("SalesType").compareToIgnoreCase("Service") == 0){
			bIncludeService = true;
			sServiceTypes = "Service";
		}
		if(request.getParameter("SalesType").compareToIgnoreCase("SalesAndService") == 0){
			bIncludeSales = true;
			bIncludeService = true;
			sServiceTypes = "Sales AND Service";
		}
		if(request.getParameter("ShowIndividualOrders") != null){
			bShowIndividualOrders = true;
		}
	/*	
    	//Get the list of selected sales groups:
    	ArrayList<String> sSalesGroups = new ArrayList<String>(0);
    	Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sMarker = SMPrintInvoiceAuditSelection.SALESGROUP_PARAM;
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sSalesGroups.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sSalesGroups);
		
    	//Get the list of selected salespersons:
    	ArrayList<String> sSalespersons = new ArrayList<String>(0);
	    paramNames = request.getParameterNames();
	    sMarker = "SALESPERSON";
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
	      //System.out.println("sParamname = " + sParamName);
		  if (sParamName.contains(sMarker)){
			  //System.out.println("sSalespersons.add: " + sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  sSalespersons.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sSalespersons);
	    
    	//Customized title

    	String sReportTitle = "Monthly Sales Report";

    	String sCriteria = "Starting with date <B>" + sStartingDate + "</B>"
    		+ ", ending with date <B>" + sEndingDate + "</B>"
    		+ ", including order types <B>" + sServiceTypes + "</B>";

    	sCriteria = sCriteria + " for salespersons: ";
    	for (int i = 0; i < sSalespersons.size(); i++){
    		if (i == 0){
    			sCriteria = sCriteria + " <B>" + sSalespersons.get(i) + "</B>";
    		}else{
    			sCriteria = sCriteria + ", <B>" + sSalespersons.get(i) + "</B>";
    		}
    	}

    	if(bShowIndividualOrders){
    		sCriteria = sCriteria + ", <B>SHOWING</B> individual orders</>";
    	}else{
    		sCriteria = sCriteria + ", <B>NOT SHOWING </B> individual orders</>";
    	}
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMSalesEffortCheckAction") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
	   
	    //log usage of this this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMSALESEFFORTCHECK, "REPORT", "SMSalesEffortCheck", "[1376509355]");
    	
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
    	/*
    	SMMonthlySalesReport amr = new SMMonthlySalesReport();
    	
    	if (!amr.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate, 
    			arrServiceTypes,
    			bShowIndividualOrders,
    			sDBID,
    			sUserID,
    			sSalespersons,
    			sSalesGroups,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + amr.getErrorMessage());
    	}
    	*/
    	//clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080659]");
	    out.println("</BODY></HTML>");
	}
}
