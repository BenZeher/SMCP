package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMAverageMUReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), -1L)){
			return;
		}
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String sWarning = "";
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    boolean bPrintIndividual = 
	    	clsManageRequestParameters.get_Request_Parameter(
	    		SMAverageMUReportSelection.PRINTINDIVIDUAL_PARAMETER, request).compareToIgnoreCase(
	    			SMAverageMUReportSelection.PRINTINDIVIDUAL_VALUE_YES) == 0;
	    String sIndividualSalesperson = clsManageRequestParameters.get_Request_Parameter(
	    		SMAverageMUReportSelection.INDIVIDUALSALESPERSON_PARAMETER, request);
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

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
				} catch (ParseException e1) {
					sWarning = "Invalid starting date: '" + sStartingDate + "'";
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    			try {
					sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
				} catch (ParseException e1) {
					sWarning = "Invalid ending date: '" + sEndingDate + "'";
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    		}
    	}
	    
    	//Customized title
    	String sReportTitle = "";
    	if (bPrintIndividual){
    		sReportTitle = "Average Mark Up Per Truck Day Report For Salesperson " + sIndividualSalesperson;
    	}else{
    		sReportTitle = "Average Mark Up Per Truck Day Report";
    	}
    	
    	String sGroupBy = "";
    	if(request.getParameter("GroupBy").compareToIgnoreCase("Salesperson") == 0){
    		sGroupBy = "Salesperson";
    	}else{
    		sGroupBy = "Order Type";
    	}
    	
    	boolean bSummaryOnly = false;
    	if(request.getParameter("Summary") != null){
    		bSummaryOnly = true;
    	}
    	
    	ArrayList<String> arServiceTypes = new ArrayList<String>(0);
		Enumeration<?> e = request.getParameterNames();
    	String sParam = "";
    	String sType = "";
    	 while (e.hasMoreElements()){
    		 sParam = (String) e.nextElement();
    		 if(clsStringFunctions.StringLeft(sParam, "SERVICETYPE".length()).compareToIgnoreCase("SERVICETYPE") == 0){
    			 if (request.getParameter(sParam) != null){
    				 sType = clsStringFunctions.StringRight(sParam, sParam.length() - "SERVICETYPE".length());
    				 arServiceTypes.add(sType);
    				 //System.out.println("Added sType: " + sType);
    			 }
    		 }
    	  } 
    	String sServiceTypes = "";
    	sServiceTypes = getSelectedServiceTypes(arServiceTypes, sDBID);
    	String sCriteria = "Starting with order date <B>" + sStartingDate + "</B>"
    		+ ", ending with order date <B>" + sEndingDate + "</B>"
    		+ ", including order types <B>" + sServiceTypes + "</B>"
    		+ ", grouped by <B>" + sGroupBy + "</B>";
    	if(bSummaryOnly){
    		sCriteria = sCriteria + ", listing in <B>Summary</B> only.";
    	}else{
    		sCriteria = sCriteria + ", listing in <B>Detail</B>.";
    	}
    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
    	 out.println(SMUtilities.getMasterStyleSheetLink());
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
     	
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
				   
 	   //log usage of this this report
 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMAVERAGEMUREPORT, "REPORT", "SMAverageMUReport", "[1376509307]");
    	
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

    	SMAverageMUReport amr = new SMAverageMUReport();
    	if (!amr.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate, 
    			arServiceTypes, 
    			sGroupBy, 
    			bSummaryOnly,
    			bPrintIndividual,
    			sIndividualSalesperson,
    			sDBID,
    			out,
    			getServletContext())){
    		out.println("Could not print report - " + amr.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080409]");
	    out.println("</BODY></HTML>");
	}
	private String getSelectedServiceTypes(ArrayList<String> arServiceTypes, String sDBID){
		
		String sDesc = "";
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName 
			+ " ORDER BY " + SMTableservicetypes.sName + " DESC";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			String sType = "";
			while(rs.next()){
				sType = rs.getString(SMTableservicetypes.sCode);
				for (int i = 0; i < arServiceTypes.size(); i++){
					if(arServiceTypes.get(i).compareToIgnoreCase(sType) == 0){
						sDesc = sDesc + rs.getString(SMTableservicetypes.sName) + ", ";
					}
				}
			}
			sDesc = clsStringFunctions.StringLeft(sDesc, sDesc.length() - ", ".length());
			rs.close();
		}catch (SQLException e){
			sDesc = "COULD NOT READ SERVICE TYPES";
		}
		
		return sDesc;
	}
}
