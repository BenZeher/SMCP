package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTablereminderusers;
import SMDataDefinition.SMTablereminders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;


public class SMDisplayReminders  extends HttpServlet {
	public static final long serialVersionUID = 1L;
	public String sErrorMessage = "";
	public static final String SCHEDULE_CODE_ID_MARKER = "SCHEDULECODE**";
	public static final String SUBMIT_ACKNOWLEDGE_REMINDERS = "Acknowledge Selected Reminders";
	public static final String Paramskipreminders = "SKIPREMINDERS";
	public int iRowCounter = 0;
	
	public SMDisplayReminders(){
		iRowCounter = 0;
		sErrorMessage = "";
	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();		

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    
	    //Check if there are any reminders
	    sErrorMessage = "";
	    String sReminders = "";
		try {
			sReminders = calculateReminders(getServletContext(), sDBID, sUserID);
		} catch (Exception e) {
			sErrorMessage += "Error checking reminders - " + e.getMessage();
		}
	    
		//If there are no reminders remove the session attribute and go to Main Menu
	    if(sReminders.compareToIgnoreCase("") == 0 && sErrorMessage.isEmpty()){
			CurrentSession.removeAttribute(SMUtilities.SMCP_SESSION_PARAM_CHECK_SCHEDULE);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			return;
	    }
	    
	    String title = "Scheduled Reminders";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/MasterStyleSheet.css\">");
	    sErrorMessage += clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sErrorMessage.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sErrorMessage + "</FONT></B><BR>");
		}
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayemindersAction?"
	    		+ Paramskipreminders + "=" + "1"
				+ "\">Skip to Main Menu</A><BR><BR>");
		
	    out.println (getStyle());
		out.println ("<FORM NAME=MAINFORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayRemindersAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=0 style=\" border-collapse:collapse;\">");
		
		out.println(displayReminderHeader());
		out.println(sReminders);

		out.println("</TABLE>");
		
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"" + SUBMIT_ACKNOWLEDGE_REMINDERS + "\">");
		out.println("</FORM>");
		
		out.println("</BODY></HTML>");
	}
	
	public String calculateReminders(ServletContext context, String sConf, String sUserID) throws Exception{
		String s = "";
		iRowCounter = 0;
		Calendar now = Calendar.getInstance();
		now = clearCalandarTime(now);
		Calendar calStartDate = Calendar.getInstance();
		calStartDate = clearCalandarTime(calStartDate);
		Calendar scheduleddate = Calendar.getInstance();
		scheduleddate = clearCalandarTime(scheduleddate);
		final SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
		
    	Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, 
					   sConf, 
					   "MySQL",
					   SMUtilities.getFullClassName(this.toString()) + ".getReminder - user: " + sUserID);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}

    	if (conn == null){
    		throw new Exception ("Connection is null");
    	}
    	
		String SQL = "SELECT * FROM " + SMTablereminderusers.TableName
				+ " WHERE ("
					+ SMTablereminderusers.luserid + " = '" + sUserID + "'"
				+ ")"
				;
		try {
			ResultSet rsUserSchedules = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			while(rsUserSchedules.next()){
				SQL = "SELECT * FROM " + SMTablereminders.TableName
					+ " WHERE ("
						+ SMTablereminders.sschedulecode + " = '" + rsUserSchedules.getString(SMTablereminderusers.sschedulecode) + "'"
					+ ")";
			String sUsersScheduleEntry = "Schedule code: '" + rsUserSchedules.getString(SMTablereminderusers.sschedulecode) + "'"
										+ "' UserID: '" + sUserID + "'";

			ResultSet rsCurrentSchedule = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsCurrentSchedule.next()){
			String sScheduleCode = rsCurrentSchedule.getString(SMTablereminders.sschedulecode);
			String sScheduleDesc = rsCurrentSchedule.getString(SMTablereminders.sdescription);
			String sLastEditedBy = rsCurrentSchedule.getString(SMTablereminders.slastediteduserfullname);
			String sLastEditDate = rsCurrentSchedule.getString(SMTablereminders.datlasteditdate);
			
			//Get the start date 
			try{
					String sStartDate = rsCurrentSchedule.getString(SMTablereminders.datstartdate);
					Date datStartDate = df.parse( sStartDate );
					calStartDate.setTime( datStartDate );
					calStartDate = clearCalandarTime(calStartDate);
				}catch (Exception e){
					throw new Exception (e.getMessage());	
				
				}
			
			//Get the last schedule acknowledged date
			Date datLastAckDate = new Date();
			try {
				String sLastAckDate = rsUserSchedules.getString(SMTablereminderusers.datlastacknowledgedreminderdate);
				datLastAckDate = df.parse(sLastAckDate);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
				Calendar calLastAckDate = Calendar.getInstance();				
				calLastAckDate.setTime(datLastAckDate);
				calLastAckDate = clearCalandarTime(calLastAckDate);
				
			//Check to make sure start date and last ack date are not 0000-00-00	
			 Calendar past = Calendar.getInstance();			
			 past.set(Calendar.YEAR, 1901);
			 if(calLastAckDate.before(past)){
				 sErrorMessage +="Error [1454609666] - Last acknowledged reminder date cannot be 00/00/0000 for schedule code:"
				 	+ "'" + sScheduleCode +"', user: '" + sUserID + ".";
				 return s; 
			 }
			
			//Start date must be before current date
			if(calStartDate.before(now)){		
			
			//If yearly interval		
			if( rsCurrentSchedule.getInt(SMTablereminders.iinterval) == SMTablereminders.INTERVAL_TYPE_YEARLY){
					
				//Calculate most current scheduled reminder date
				int iScheduledMonth = rsCurrentSchedule.getInt(SMTablereminders.imonth);
				int iScheduledDayOfMonth = rsCurrentSchedule.getInt(SMTablereminders.idayofmonth);
				//If EOM/31 is set for Day of Month calculate the last day of the month.
				if(iScheduledDayOfMonth == 31){
					scheduleddate.set(now.get(Calendar.YEAR), iScheduledMonth, 1);
					iScheduledDayOfMonth = scheduleddate.getActualMaximum(Calendar.MONTH);
					scheduleddate.set(now.get(Calendar.YEAR), iScheduledMonth, iScheduledDayOfMonth);
				}else{				
					scheduleddate.set(now.get(Calendar.YEAR), iScheduledMonth, iScheduledDayOfMonth);
				}
		
				//Move backwards through the scheduled reminder dates till last acknowledged reminder date is reached. 
				while(isCalendarDateBefore(calLastAckDate, scheduleddate)){	

					updateLastRunDate(sScheduleCode, sUserID, conn);
					
					s += displayReminder(sScheduleCode,
							 df.format(scheduleddate.getTime()), 
							 sScheduleDesc,
							 sLastEditedBy,
							 sLastEditDate
							 );
		
							scheduleddate.add(Calendar.YEAR, -1);
					}
				
				//If monthly interval
				}else if(rsCurrentSchedule.getInt(SMTablereminders.iinterval) == SMTablereminders.INTERVAL_TYPE_MONTHLY){					
				
				//Calculate most current scheduled reminder date
				int iScheduledDayOfMonth = rsCurrentSchedule.getInt(SMTablereminders.idayofmonth);				
				
				//If EOM/31 is set for Day of Month calculate the last day of the month.
				if(iScheduledDayOfMonth == 31){
					scheduleddate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1);
					iScheduledDayOfMonth = scheduleddate.getActualMaximum(Calendar.MONTH);
					scheduleddate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), iScheduledDayOfMonth);
				}else{				
					scheduleddate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), iScheduledDayOfMonth);
				}

				//Move backwards through the scheduled reminder dates till start date or last acknowledged date is reached.
				while(isCalendarDateBefore(calLastAckDate, scheduleddate)){	
					
					updateLastRunDate(sScheduleCode, sUserID, conn);
					
					s += displayReminder(sScheduleCode,
							 df.format(scheduleddate.getTime()), 
							 sScheduleDesc,
							 sLastEditedBy,
							 sLastEditDate
							 );;
						    
						scheduleddate.add(Calendar.MONTH, -1);	
					}
					
			    //If weekly interval
				}else{
				
				//Calculate most current scheduled reminder date
				scheduleddate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

				//Move backwards through the scheduled reminder dates till start date or last acknowledged date is reached.
				while(isCalendarDateBefore(calLastAckDate, scheduleddate)){

					String sCurrentDayofWeek = scheduleddate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,  Locale.getDefault());

					if(rsCurrentSchedule.getInt(SMTablereminders.isaturday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Saturday") == 0){
						
						updateLastRunDate(sScheduleCode, sUserID, conn);
						
						s += displayReminder(sScheduleCode,
								 df.format(scheduleddate.getTime()), 
								 sScheduleDesc,
								 sLastEditedBy,
								 sLastEditDate
								 );
							
						}else if (rsCurrentSchedule.getInt(SMTablereminders.ifriday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Friday") == 0 ){
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
							
						}else if(rsCurrentSchedule.getInt(SMTablereminders.ithursday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Thursday") == 0 ){
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
							
						}else if(rsCurrentSchedule.getInt(SMTablereminders.iwednesday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Wednesday") == 0 ){
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
							
						}else if(rsCurrentSchedule.getInt(SMTablereminders.ituesday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Tuesday") == 0 ){
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
							
						}else if(rsCurrentSchedule.getInt(SMTablereminders.imonday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Monday") == 0 ){
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
							
						}else if(rsCurrentSchedule.getInt(SMTablereminders.isunday) == 1 
								&& sCurrentDayofWeek.compareToIgnoreCase("Sunday") == 0 ){	
							
							updateLastRunDate(sScheduleCode, sUserID, conn);
							
							s += displayReminder(sScheduleCode,
									 df.format(scheduleddate.getTime()), 
									 sScheduleDesc,
									 sLastEditedBy,
									 sLastEditDate
									 );
						}else {
							//NO days selected
						}
							//decrement schedule date by one day and check again.
							scheduleddate.add(Calendar.DAY_OF_WEEK, -1);
					}					
				}
			}
			}else{
			sErrorMessage += "Error [1454609556]  - Entry in smscheduledusers " +  sUsersScheduleEntry 
					+ " does not match any schedule codes in smschedules. ";
			}
			rsCurrentSchedule.close();
			}		
			rsUserSchedules.close();
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception (e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn);
		return s;
	}
	
	public String displayReminder(String scheduleCode, 
								  String scheduledDate, 
								  String scheduleDescription,
								  String sLastEditedBy,
								  String sLastEditDate) throws Exception{
		String sRowColor = "";
		if((getRowCounter() & 1) == 0){
			sRowColor = "";
		}else{
			sRowColor = "class=\"oddnumberedtablerow\"";
		}
		String s = "";
		s +="<TR " + sRowColor + "><TD class=\"reminder\">"
			 + " <input type=\"checkbox\" " + " style=\"text-align:center;\""
			 + " name=\"SCHEDULECODE**" + scheduleCode +  "\""
		     + " value=\"" +  scheduledDate + "\"> "	
			 + "</TD>"
			 + "<TD class=\"reminder\">" + clsDateAndTimeConversions.resultsetDateStringToString(scheduledDate) + "</TD>"
			 + "<TD class=\"reminder\">" + scheduleCode + "</TD>"
			 + "<TD class=\"reminder\">" + scheduleDescription + "</TD>"
			 + "<TD class=\"reminder\">" + sLastEditedBy + "</TD>"
			 + "<TD class=\"reminder\">" + clsDateAndTimeConversions.resultsetDateTimeStringToString(sLastEditDate) + "</TD>"
			 + "</TR>";
		incrimentRowCounter();
		return s;
	}
	
	public String displayReminderHeader(){
		String s = "";
		s +="<TR class=\"border_bottom\">"
			+ "<TH> </TH>"
			+ "<TH>Reminder Date</TH>"
			+ "<TH>Reminder Code</TH>"
			+ "<TH>Description</TH>"
			+ "<TH>Last edited by</TH>"
			+ "<TH>Last edited date</TH></TR>";
		return s;
	}
	
	public void updateLastRunDate(String sScheduleCode, String sUserID, Connection conn)throws Exception{
		
		String SQL = "UPDATE " + SMTablereminderusers.TableName 
				+ " SET "
				+ SMTablereminderusers.datlastrundate + "= NOW()" 
				+ " WHERE ("
				+ "(" + SMTablereminderusers.sschedulecode + "='" + sScheduleCode + "')"
				+ " AND "
				+ "(" + SMTablereminderusers.luserid + "= " + sUserID + ")"
				+ ")"
				;
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
			}
		}catch(Exception ex){
			throw new Exception("Error [1458709156] updating last run date for schedule code '" + sScheduleCode + "' - " + ex.getMessage());
	}
		return;
	}
	
	public String getStyle(){
		String s = "";
		s += "<style>\n";
		 
		s += "tr.border_bottom th {\n"
		 + "border-bottom:1pt solid black;\n"
		 + " }\n\n"
		 ;
		
		s += "th {\n"
		 + " font-size:medium;\n"
		 + "white-space: nowrap;"
		 + "text-align: left;"
		 + " }\n";
		
		s += ".reminder {\n"
		 + " font-size:small;\n"
		 + " }\n";
				 
		s += "</style>";
		return s;
	}
	
	public Calendar clearCalandarTime(Calendar calendar){
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.AM_PM);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		return calendar;
	}
	
	public static boolean isCalendarDateBefore(Calendar c1, Calendar c2) throws Exception{
	    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    boolean isBefore = false;
	    
	    try {
			isBefore = sdf.parse(sdf.format(c1.getTime())).before(sdf.parse(sdf.format(c2.getTime())));
		} catch (ParseException e) {
			throw new Exception("Error [1455110973] comparing calendar objects - " + e.getMessage());
		}	    
	    return isBefore;
	}
	
	public int getRowCounter(){
		return iRowCounter;
	}
	public void incrimentRowCounter(){
		iRowCounter += 1;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
