package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class SMCanceledJobsReportGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "0";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCanceledJobsReport))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID= (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Canceled Orders Report";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
		   
 	   //log usage of this this report
 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCANCELEDJOBSREPORT, "REPORT", "SMCanceledJobsReport", "[1376509311]");

    	//Calculate time period
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy EEE");
    	//Start date
    	
    	String sStartDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
    /*	if (!SMUtilities.IsValidDateString("M/d/yyyy", sStartDate)){
    		out.println("Invalid start date - '" + sStartDate);
    		return;
    	}*/
    	try {
			sStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			out.println("Invalid start date - '" + sStartDate + "' - " + e.getMessage());
    		return;
		}
    	
    	
    	String sEndDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
    	/*if (!SMUtilities.IsValidDateString("M/d/yyyy", sEndDate)){
    		out.println("Invalid end date - '" + sEndDate);
    		return;
    	}*/
    	try {
			sEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			out.println("Invalid end date - '" + sEndDate + "' - " + e.getMessage());
    		return;
		}
    	
    	/*
	    Timestamp datStartingDate = new Timestamp(Integer.parseInt(request.getParameter("StartingDateSelectedYear")) - 1900,
										          Integer.parseInt(request.getParameter("StartingDateSelectedMonth")) - 1,
										          Integer.parseInt(request.getParameter("StartingDateSelectedDay")),
										          0,
										          0,
										          0,
										          0);
	    //End date
	    Timestamp datEndingDate = new Timestamp(Integer.parseInt(request.getParameter("EndingDateSelectedYear")) - 1900,
									            Integer.parseInt(request.getParameter("EndingDateSelectedMonth")) - 1,
									            Integer.parseInt(request.getParameter("EndingDateSelectedDay")),
									            23,
									            59,
									            59,
									            999999999);
		*/
									            
	    //boolean bCheckItemCategories = false;
		ArrayList<String> alItemCategories = new ArrayList<String>(0);
	    if (request.getParameter("CheckItemCategories") != null){
	    	//create a list of 
			Enumeration<?> paramNames = request.getParameterNames();
			while(paramNames.hasMoreElements()) {
				String s = paramNames.nextElement().toString();
				//System.out.println("paramNames.nextElement() = " + s);
				if (s.substring(0, 2).compareTo("!!") == 0){
					alItemCategories.add(s.substring(2));
				}
			}
    	}else{
    		alItemCategories.add("ALLIC");
    	}
	    
	    try{
		    String SQL = "SELECT" + 
					" " + SMTableorderheaders.sOrderNumber + "," + 
					" " + SMTableorderheaders.sBillToName + "," + 
					" " + SMTableorderheaders.sDefaultItemCategory + "," + 
					" " + SMTableorderheaders.LASTEDITUSERFULLNAME + "," + 
					" " + SMTableorderheaders.LASTEDITUSERID + "," +
					" " + SMTableorderheaders.datOrderCanceledDate + "," + 
					" " + SMTableorderheaders.mInternalComments +
				  " FROM" + 
				  	" " + SMTableorderheaders.TableName + 
				  " WHERE" + 
				  	" " + SMTableorderheaders.datOrderCanceledDate + " >= '" + sStartDate + "'" + 
				  	" AND" + 
				  	" " + SMTableorderheaders.datOrderCanceledDate + " <= '" + sEndDate + " 23:59:59'";
			
			if (alItemCategories.size() == 0){
				//no location selected, make the SQL return nothing
				SQL = SQL + 
					" AND" +
					" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = ''";
			}else{
				if (alItemCategories.get(0).toString().compareTo("ALLIC") != 0){
					SQL = SQL + " AND (";
					String sItemCategories = "";
					for (int i=0;i<alItemCategories.size();i++){
						sItemCategories = sItemCategories + " OR" + 
									  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultItemCategory + " = '" + alItemCategories.get(i) + "'";
					}
					//remove the leading OR
					sItemCategories = sItemCategories.substring(4);
					SQL = SQL + sItemCategories + ")";
				}
			}
			
			SQL = SQL + " ORDER BY" +
				  	" " + SMTableorderheaders.sDefaultItemCategory + "," +
				  	" " + SMTableorderheaders.sOrderNumber;
			
		    ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
		    //print out column headers
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    out.println("<TR><TD COLSPAN=5 ALIGN=CENTER><HR></TD></TR>" +
		    			"<TR BGCOLOR=\"#EEEEEE\"><TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><B>Order#</B></TD>" +
		    				"<TD ALIGN=LEFT VALIGN=TOP WIDTH=23%><B>Bill To Name</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><B>USER</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=13%><B>Canceled Date</B></TD>" +
		    				"<TD ALIGN=LEFT VALIGN=TOP WIDTH=50%><B>Reason For Cancellation</B></TD>" +
	    				"</TR>");
		    boolean bFlipper = false;
		    String sbgColor = "";
		    String sCurrentCategory = null;
		    while (rs.next()){
		    	
		    	if (sCurrentCategory == null || rs.getString(SMTableorderheaders.sDefaultItemCategory).compareTo(sCurrentCategory) != 0){
		    		out.println("<TR><TD COLSPAN=5 ALIGN=CENTER><HR></TD></TR>");
		    		out.println("<TR><TD COLSPAN=2 ALIGN=LEFT><B>Default Category:&nbsp;&nbsp;&nbsp;&nbsp;" + rs.getString(SMTableorderheaders.sDefaultItemCategory) + "</B></TD></TR>");
		    		out.println("<TR><TD COLSPAN=2 ALIGN=CENTER><HR></TD></TR>");
		    		sCurrentCategory = rs.getString(SMTableorderheaders.sDefaultItemCategory);
		    	}
		    	bFlipper = !bFlipper;
		    	if (bFlipper){
		    		sbgColor = "\"#FFFFFF\"";
		    	}else{
		    		sbgColor = "\"#DDDDDD\"";
		    	}
    			out.println("<TR BGCOLOR=" + sbgColor + ">");
	    		//out.println("<TD ALIGN=CENTER>" + rs.getInt(SMTablecustomercalllog.id) + "</TD>");
    			out.println("<TD ALIGN=CENTER><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
    				+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rs.getString(SMTableorderheaders.sOrderNumber) 
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
    				+ rs.getString(SMTableorderheaders.sOrderNumber).trim() + "</A></TD>");
	    		out.println("<TD ALIGN=LEFT>" + clsStringFunctions.FormatSQLResult(rs.getString(SMTableorderheaders.sBillToName)) + "</TD>");
	    		out.println("<TD ALIGN=CENTER>" + rs.getString(SMTableorderheaders.LASTEDITUSERFULLNAME) + "</TD>");
	    		out.println("<TD ALIGN=CENTER>" + USDateOnlyformatter.format(rs.getTimestamp(SMTableorderheaders.datOrderCanceledDate)) + "</TD>");
	    		out.println("<TD ALIGN=LEFT>" + clsStringFunctions.FormatSQLResult(rs.getString(SMTableorderheaders.mInternalComments)) + "</TD>");
	    		out.println("</TR>");
		    }
		    rs.close();
		    out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=5><HR></TD></TR>");
		    out.println("</TABLE>");
	    }catch (SQLException ex){
	    	System.out.println("Error in SMCanceledJobsReportGenerate");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}