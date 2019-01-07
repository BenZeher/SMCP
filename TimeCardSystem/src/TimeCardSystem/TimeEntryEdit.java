package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Servlet that reads pin number for validation.*/

public class TimeEntryEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * This form is for editing time entries. 
	 * 1.	if there is already a date passed in, default to that date.
	 * 2.	if there is no date at all, default to today.
	 */
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Time Entry Edit";
	    
    	HttpSession CurrentSession = request.getSession();
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

    	Timestamp tsDate;
    	String sSQL = "";
	    
    	//get default date and time if there is any
    	try{
		   // System.out.println("See if there is a passed in timestamp.");
	    	tsDate = Timestamp.valueOf(request.getParameter("Date"));
	    }catch(Exception ex){
		    //System.out.println("no timestamp passed in, now need to figure out a time to default to.");
	    	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
					 							AccessControlFunctionList.ManagerReviewListTimeEditing)){

    			sSQL = TimeCardSQLs.Retieve_Specific_Time_Entry(Integer.parseInt(request.getParameter("id")));
    			try{
    				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			    	if (rs.next()){
	    				if (Integer.parseInt(request.getParameter("Type")) == 0){ 
			    			//look for paired out date
						    //System.out.println("get out time");
					    	tsDate = rs.getTimestamp("dtOutTime");
			    		}else if (Integer.parseInt(request.getParameter("Type")) == 1){
						    //System.out.println("get in time");
					    	tsDate = rs.getTimestamp("dtInTime");
			    		}else{
			    			//System.out.println("Donno how got here.");
		    	    		tsDate = new Timestamp(System.currentTimeMillis());
			    		}
		    		}else{
		    			//if no time entry, 
					    //System.out.println("no entry with this id.");
	    	    		tsDate = new Timestamp(System.currentTimeMillis());
		    		}
    			}catch(SQLException e){
    				//any error happened, set today as default.
				    //System.out.println("error getting record set.");
    	    		tsDate = new Timestamp(System.currentTimeMillis());
    			}
	    	}else{
			    //System.out.println("not authorized to modify time.");
	    		tsDate = new Timestamp(0);
	    	}
	    }
	    //System.out.println("tsDate.getTime() = " + tsDate.getTime());
    		    
	    try {
	    	//get the employee access level from session
	    	SimpleDateFormat sdfDate = new SimpleDateFormat ("MM-dd-yyyy");
	    	SimpleDateFormat sdfTime = new SimpleDateFormat ("hh:mm:ss a");
        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntrySave\">");
        	
        	//forward the parameters from previous form to the next form.
        	out.println("<INPUT TYPE=HIDDEN NAME=\"Type\" VALUE=\"" + request.getParameter("Type") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=\"" + request.getParameter("id") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + request.getParameter("EmployeeID") + "\">");
        	//out.println("OriginalURL: " + request.getParameter("OriginalURL"));
        	out.println("<TABLE BORDER=2 CELLPADDING=4>");
        	out.println("<TR><TD ALIGN=CENTER><H4> Date: </H4></TD>");
        	//find current monday
        	Calendar c = Calendar.getInstance();
        	c.setTime(tsDate);
        	
        	if (!TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												 AccessControlFunctionList.ManagerReviewListTimeEditing)){
    			//manager, can't change time
        		if (tsDate.getTime() == 0){
        			if (request.getParameter("Type").compareTo("0") == 0){
        				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>No Punch In Record</B></FONT></TD>");
        			}else if (request.getParameter("Type").compareTo("1") == 0){
        				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>No Punch Out Record</B></FONT></TD>");
        			}else{
        				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>I Don't Know What Kind Of Record This Is</B></FONT></TD>");
        			}
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedMonth\" VALUE=\"00\">");
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedDay\" VALUE=\"00\">");
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedYear\" VALUE=\"0000\">");
        		}else{
        			out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" +  sdfDate.format(c.getTime()) + "</B></FONT></TD>");
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedMonth\" VALUE=\"" + (c.get(Calendar.MONTH) + 1) + "\">");
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedDay\" VALUE=\"" + c.get(Calendar.DAY_OF_MONTH) + "\">");
        			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedYear\" VALUE=\"" + c.get(Calendar.YEAR) + "\">");
        		}
    		}else{
    			//administrator, can change time
	        	out.println("<TD>");
        		out.println("Month <SELECT NAME=\"SelectedMonth\">");
	        		for (int i=1; i<=12;i++){
	        			if (i == c.get(Calendar.MONTH) + 1){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
		        	
	        	out.println("Day <SELECT NAME=\"SelectedDay\">");
		    		for (int i=1; i<=31;i++){
	        			if (i == c.get(Calendar.DAY_OF_MONTH)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("Year <SELECT NAME=\"SelectedYear\">");
		    		for (int i=1970; i<=2069;i++){
	        			if (i == c.get(Calendar.YEAR)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("</TD>");
    		}
	    
        	out.println("</TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H4> Time: </H4></TD>");
        	
        	if (!TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
					 							 AccessControlFunctionList.ManagerReviewListTimeEditing)){
    			//manager, can't change time
        		if (tsDate.getTime() == 0){
    				out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>&nbsp;</B></FONT></TD>");
        			
        		}else{
        			out.println("<TD ALIGN=CENTER><FONT SIZE=3><B>" +  sdfTime.format(c.getTime()) + "</B></FONT></TD>");
        		}
    			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedHour\" VALUE=\"" + c.get(Calendar.HOUR) + "\">");
    			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedMinute\" VALUE=\"" + c.get(Calendar.MINUTE) + "\">");
    			out.println("<INPUT TYPE=HIDDEN NAME=\"SelectedAMPM\" VALUE=\"" + c.get(Calendar.AM_PM) + "\">");
    		}else{
    			out.println("<TD>");
        		int iHour;
        		if (c.get(Calendar.HOUR) == 0 && c.get(Calendar.AM_PM) == Calendar.PM){
        			iHour = 12;
        		}else{
        			iHour = c.get(Calendar.HOUR);
        		}
        		out.println("Hour <SELECT NAME=\"SelectedHour\">");
	        		for (int i=0; i<=12;i++){
	        			if (i == iHour){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
	        	out.println("Minute <SELECT NAME=\"SelectedMinute\">");
		    		for (int i=0; i<=59;i++){
	        			if (i == c.get(Calendar.MINUTE)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("AM/PM <SELECT NAME=\"SelectedAMPM\">");
		    		for (int i=Calendar.AM; i<=Calendar.PM;i++){
	        			if (i == c.get(Calendar.AM_PM)){
	        				if (i == Calendar.AM){
	        					out.println("<OPTION SELECTED VALUE=" + Calendar.AM + ">" + "AM");
	        				}else{
	        					out.println("<OPTION SELECTED VALUE=" + Calendar.PM + ">" + "PM");
	        				}		
	        			}else{
	        				if (i == Calendar.AM){
	        					out.println("<OPTION VALUE=" + Calendar.AM + ">" + "AM");
	        				}else{
	        					out.println("<OPTION VALUE=" + Calendar.PM + ">" + "PM");
	        				}
	        			}
		    		}
	    		out.println("</SELECT>");	
	    		out.println("</TD></TR>");
    		}
        	
        	out.println("<TR><TD ALIGN=CENTER><H4> Change Log: </H4></TD>");
        	out.println("<TD><TEXTAREA NAME=Log ROWS=6 COLS=30>");

        	sSQL = TimeCardSQLs.Get_Change_Log_SQL(Integer.parseInt(request.getParameter("id")));
		    ResultSet rsChangeLog = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			while (rsChangeLog.next()){
				out.println(rsChangeLog.getString("mChangeLog"));
			}
			rsChangeLog.close();
			
	        out.println("</TEXTAREA></TD></TR></TABLE>");
	        
        	out.println("<BR><TABLE BORDER=0 WIDTH = 40%>");
        	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
					 							AccessControlFunctionList.ManagerReviewListTimeEditing)){
	        	if (Integer.parseInt(request.getParameter("Type")) == 0){
	        		//early start flag
	        		out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=EarlyStartCheck VALUE=1");
	        		if (Integer.parseInt(request.getParameter("EarlyStart")) == 1){
	        			out.println(" CHECKED");
	        		}
					out.println(">Check here if it is an early start.</TD></TR>");
					
					//late flag
					out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=LateFlagCheck VALUE=1");
	        		if (Integer.parseInt(request.getParameter("LateFlag")) == 1){
	        			out.println(" CHECKED");
	        		}
					out.println(">Check here if it is an UNEXCUSED late.</TD></TR>");
	        	}
				//special time entry type
	        	out.println ("<TR><TD><TABLE BORDER=0><TR><TD><B>Time Entry Type</B></TD><TD><SELECT NAME=\"SpecialEntryType\">");
	        	//get list of special time entry type
		    	sSQL = TimeCardSQLs.Get_Time_Entry_Type_Info_SQL();
	    		ResultSet rsSTET = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		while (rsSTET.next()){
	    			if (rsSTET.getInt("iTypeID") == Integer.parseInt(request.getParameter("SpecialEntryType"))){
	        			out.println("<OPTION SELECTED VALUE=" + rsSTET.getInt("iTypeID") + ">" + rsSTET.getString("sTypeTitle") + " - " + rsSTET.getString("sTypeDesc"));
	        		}else{
	        			out.println("<OPTION VALUE=" + rsSTET.getInt("iTypeID") + ">" + rsSTET.getString("sTypeTitle") + " - " + rsSTET.getString("sTypeDesc"));
	        		}
	        	}
	    		out.println ("</TD></TR></TABLE></TD></TR>");
	        	rsSTET.close();
        	}else{
        		if (request.getParameter("EarlyStart") != null){
	        		if (Integer.parseInt(request.getParameter("EarlyStart")) == 1){
	        			out.println("<TR><TD><INPUT TYPE=HIDDEN NAME=\"EarlyStartCheck\" VALUE=\"1\"></TD></TR>");
	        		}
        		}
        		if (request.getParameter("LateFlag") != null){
	        		if (Integer.parseInt(request.getParameter("LateFlag")) == 1){
	        			out.println("<TR><TD><INPUT TYPE=HIDDEN NAME=\"LateFlagCheck\" VALUE=\"1\"></TD></TR>");
	        		}
        		}
        		out.println("<TR><TD><INPUT TYPE=HIDDEN NAME=\"SpecialEntryType\" VALUE=\"" + request.getParameter("SpecialEntryType") + "\"></TD></TR>");
        	}
        	out.print("</TABLE>");
        	
        	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
        	
        	out.println("</FORM>");
        	
        	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
					 							AccessControlFunctionList.ManagerReviewListTimeEditing)){
	        	//delete current record.
	        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryRemove\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=\"" + request.getParameter("id") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Type\" VALUE=\"" + request.getParameter("Type") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + request.getParameter("EmployeeID") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"SpecialEntryType\" VALUE=\"" + request.getParameter("SpecialEntryType") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
	        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
	        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete.");
	        	out.println("</FORM>");
        	}
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><B>Error [1389641946] in TimeEntryEdit class - " + ex.getMessage() + "</B><BR>");
	    }
 
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
