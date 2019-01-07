package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.sql.*;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;

/** Servlet that insert In-Time records into the the time entry table.*/

public class TimeEntrySave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
		String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		String sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
	    String title = "Time Card System";
	    
	    try {
	    	int iType = Integer.parseInt(request.getParameter("Type"));
/*	        TimeEntries;
	        +--------------+--------------+------+-----+---------------------+----------------+
			| Field        | Type         | Null | Key | Default             | Extra          |
			+--------------+--------------+------+-----+---------------------+----------------+
			| id           | mediumint(9) |      | PRI | NULL                | auto_increment |
			| sEmployeeID  | varchar(9)   | YES  |     |                     |                |
			| dtInTime     | datetime     | YES  |     | 0000-00-00 00:00:00 |                |
			| dtOutTime    | datetime     | YES  |     | 0000-00-00 00:00:00 |                |
			| iInModified  | int(1)       | YES  |     | 0                   |                |
			| iOutModified | int(1)       | YES  |     | 0                   |                |
			| iEarlyStart  | int(1)       | YES  |     | 0                   |                |
			| sPeriodDate  | varchar(10)  | YES  |     | 0000-00-00          |                |
			| mChangeLog   | text         | YES  |     | NULL                |                |
			| dtInTimeOri  | datetime     | YES  |     | 0000-00-00 00:00:00 |                |
			+--------------+--------------+------+-----+---------------------+----------------+
*/
			/**
			Timestamp Parameters:
			    year - the year minus 1900
			    month - 0 to 11
			    date - 1 to 31
			    hour - 0 to 23
			    minute - 0 to 59
			    second - 0 to 59
			    nano - 0 to 999,999,999 
				    	  
			*/
	    	Timestamp sqlTimeStamp = new Timestamp(System.currentTimeMillis());
	    	if (request.getParameter("SelectedYear").compareTo("0000") != 0){
	    	 sqlTimeStamp = new Timestamp(Integer.parseInt(request.getParameter("SelectedYear")) - 1900,
		    							  Integer.parseInt(request.getParameter("SelectedMonth")) - 1,
		    							  Integer.parseInt(request.getParameter("SelectedDay")),
		    							  clsDateAndTimeConversions.NormalToMilitary(
		    											   Integer.parseInt(request.getParameter("SelectedHour")), 
		    											   Integer.parseInt(request.getParameter("SelectedAMPM"))),
		    											   Integer.parseInt(request.getParameter("SelectedMinute")),
		    							  0,
		    							  0);
	    	}
	    	String sSQL;
	    	Date datToday = new Date(sqlTimeStamp.getTime());
	    	int iEarlyStart = 0;
	    	int iLateFlag = 0;
	    	int iLateMinute = 0;

			if (request.getParameter("EarlyStartCheck") != null){
	    		//Early Start
	    		iEarlyStart = 1;
			}
			if (request.getParameter("LateFlagCheck") != null){
	    		//Late
				iLateFlag = 1;
			}
			//calculate late minutes as a normal punch-in
			sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("EmployeeID"));
			ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			String sStartTime;
			if (rsEmployeeInfo.next()){
				sStartTime = rsEmployeeInfo.getString("tStartTime");
				iLateMinute = (int)TimeCardUtilities.RoundHalfUp((sqlTimeStamp.getTime() - Timestamp.valueOf(datToday.toString() + " " + sStartTime).getTime()) / 60000.0, 0);
			}
			rsEmployeeInfo.close();
	    	
			String sTimeString;
			if (request.getParameter("SelectedYear").compareTo("0000") != 0){
				sTimeString = sqlTimeStamp.toString();
			}else{
				sTimeString = "0000-00-00 00:00:00";
			}
			
			boolean bIsNewEntry = false;
			boolean bIsPunchIn = false;
	    	if (Integer.parseInt(request.getParameter("id")) == 0){
	    		bIsNewEntry = true;
	    		//INSERT NEW TIME ENTRY, don't insert anything into rawpunchevents table
	    		if (iType == 0){
	    			bIsPunchIn = true;
		    		sSQL = TimeCardSQLs.Get_Insert_In_Time_Entry_SQL(request.getParameter("EmployeeID"), 
		    														 sTimeString,  
		    														 "0000-00-00 00:00:00", //no original time for non-original time entry 
		    														 1, //this means modified, this time is considered to be modified as this is very likely to be FINAL 
		    														 iEarlyStart,
		    														 iLateFlag,
		    														 iLateMinute,
		    														 request.getParameter("Log"),
		    														 Integer.parseInt(request.getParameter("SpecialEntryType")));
	    		}else{
		    		sSQL = TimeCardSQLs.Get_Insert_Out_Time_Entry_SQL(request.getParameter("EmployeeID"), 
		    														  sTimeString, 
																	  1,
																	  request.getParameter("Log"),
			    													  Integer.parseInt(request.getParameter("SpecialEntryType"))); //this means modified
																	  
	    		}
	    	}else{
	    		//UPDATE CURRENT TIME ENTRY
	    		if (iType == 0){
	    			bIsPunchIn = true;
		    		sSQL = TimeCardSQLs.Get_Update_In_time_SQL(Integer.parseInt(request.getParameter("id")), 
		    													sTimeString, 
		    													1, //modified
		    													iEarlyStart, 
	    														iLateFlag,
	    														iLateMinute,
		    													request.getParameter("Log"),
		    													Integer.parseInt(request.getParameter("SpecialEntryType")));
		    	}else{
		    		sSQL = TimeCardSQLs.Get_Update_Out_time_SQL(Integer.parseInt(request.getParameter("id")), 
		    													sTimeString, 
																1, 
																request.getParameter("Log"),
		    													Integer.parseInt(request.getParameter("SpecialEntryType"))); //modified
	    		}
	    	}

    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    		//SimpleDateFormat formatter = new SimpleDateFormat ("'at ' hh:mm:ss a ' on ' MMM-dd-yyyy");
	    		out.println ("<HTML>\n" + 
		                	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
		                	 "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
		                	 "<H1>" + title + "</H1>\n" + 
		                	 "<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    		//out.println ("<BR><H2>You've modified the time entry sucessfully.</H2>");
	    	}else{
	    		out.println ("<HTML>\n" + 
			       			 //"<META http-equiv='Refresh' content='2;URL=" + request.getParameter("OriginalURL") + "'>" +
			            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
			            	 "<BODY BGCOLOR=\"#" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
			            	 "<H1>" + title + "</H1>\n");
	    		out.println("<BR><H2>Failed in saving out process.</H2><BR>");
	    	}
    		
    		//Log the insert:
    		TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
    		String sProcess = "Updated current time entry";
    		if (bIsNewEntry){
    			sProcess = "Added new time entry";
    		}
    		
    		if (bIsPunchIn){
    			sProcess += ", punch IN";
    		}else{
    			sProcess += ", punch OUT";
    		}
    		log.writeEntry(
    			sUserID, 
    			TCLogEntry.LOG_OPERATION_ADMIN_SAVED_TIME_ENTRY, 
    			sProcess + " for '" + request.getParameter("EmployeeID") + "'", 
    			"SQL = '" + sSQL + "'", 
    			"[1518031076]"
    		);
    		
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}