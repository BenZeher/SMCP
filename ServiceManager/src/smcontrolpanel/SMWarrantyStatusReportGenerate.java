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
import java.util.Collections;
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

public class SMWarrantyStatusReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMWarrantystatusreport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    
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
				} catch (ParseException e1) {
					sWarning = "Error:[1423665446] Invalid starting date: '" + sStartingDate + "' - " + e1.getMessage();
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
					sWarning = "Error:[1423665447] Invalid ending date: '" + sEndingDate + "' - " + e1.getMessage();
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
    	String sReportTitle = "Warranty Status Report";
    	
    	ArrayList<String> arServiceTypes = new ArrayList<String>(0);
		Enumeration<String> e = request.getParameterNames();
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
    	
    	//Get the list of selected salespersons:
    	ArrayList<String> sSalespersons = new ArrayList<String>(0);
	    Enumeration<String> paramNames = request.getParameterNames();
	    String sMarker = "SALESPERSON";
	    while(paramNames.hasMoreElements()) {
	      String sParamName = paramNames.nextElement();
	      //System.out.println("sParamname = " + sParamName);
		  if (sParamName.contains(sMarker)){
			  //System.out.println("sSalespersons.add: " + sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  sSalespersons.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sSalespersons);
    	
    	String sCriteria = "Starting with warranty expiration date <B>" + sStartingDate + "</B>"
    		+ ", ending with warranty expiration date <B>" + sEndingDate + "</B>"
    		+ ", including order types <B>" + sServiceTypes + "</B>";
    	
    	sCriteria = sCriteria + " for salespersons: ";
    	for (int i = 0; i < sSalespersons.size(); i++){
    		if (i == 0){
    			sCriteria = sCriteria + " <B>" + sSalespersons.get(i) + "</B>";
    		}else{
    			sCriteria = sCriteria + ", <B>" + sSalespersons.get(i) + "</B>";
    		}
    	}

    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor  + "\">" +
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
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMWARRANTYSTATUSREPORT, "REPORT", "SMWarrantyStatusReport", "[1376509368]");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMWarrantyStatusReportGenerate");
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
    	SMWarrantyStatusReport wsr = new SMWarrantyStatusReport();
    	if (!wsr.processReport(
    		conn, 
    		sStartingDate, 
    		sEndingDate, 
    		arServiceTypes, 
    		sSalespersons, 
    		out, 
    		sDBID,
    		getServletContext())
    		){
    		out.println("Could not print report - " + wsr.getErrorMessage());
    	}

    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080685]");
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
