package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletContext;
//import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMGoogleMapAPIKey;
import ServletUtilities.clsBrowserReader;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsDBServerTime;
import TCSDataDefinition.Departments;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTablecompanyprofile;
import TCSDataDefinition.TimeEntries;

/** Servlet that reads pin number for validation.*/

public class UserLogin extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MAIN";
	private static final String SCREEN_WIDTH_USAGE_PERCENTAGE = "85";
	private static final String TIME_IN_SECONDS_TO_REFRESH_ON_INVALID_PIN = "6";
	private static final String TIME_IN_SECONDS_TO_REFRESH_AFTER_WAITING = "60";
	private static final String NO_PUNCH_IN_MESSAGE = "<I>(No punch in record)</I>";
	private static final String NO_PUNCH_OUT_MESSAGE = "<I>(No punch out record)</I>";
	private static final String PUNCH_LIST_HEADING_BACKGROUND_COLOR = "BLACK";
	private static final String PUNCH_LIST_HEADING_FOREGROUND_COLOR = "WHITE";
	private static final String ODD_ROW_BACKGROUND_COLOR = "WHITE";
	private static final String EVEN_ROW_BACKGROUND_COLOR = "LIGHTGRAY";
	private static final long MINIMUM_HOURS_TO_FORCE_LUNCHTIME_DEDUCTION = 5;
	private static final long NUMBER_OF_MINUTES_DEDUCTED_FOR_LUNCH = 30;
	private static final String FORM_ACTION_TARGET_CLASS = "TimeCardSystem.TCPunchAction";
	
	//Used to get GPS location of the user:
	public static final String PARAMETER_USER_LATITUDE = "USERLATITUDE";
	public static final String PARAMETER_USER_LONGITUDE = "USERLONGITUDE";
	public static final String PARAMETER_LOCATION_RECORDING_ALLOWED = "LOCATIONRECORDINGALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED = "NOTALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED = "ALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER = "LOCATIONRECORDINGSUPPORTEDINBROWSER";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_NOT_SUPPORTED = "NOTSUPPORTED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_SUPPORTED = "SUPPORTED";
	
	private static final boolean RECORD_USERS_GPS_LOCATION= true;
	
	private static final boolean bDebugMode = false;
	
	//Buttons:
	private static final String PUNCH_IN_BUTTON_LABEL = "IN";
	private static final String PUNCH_OUT_BUTTON_LABEL = "OUT";
	private static final String PUNCH_EARLYSTART_BUTTON_LABEL = "EARLY START";
	private static final String PUNCH_IN_BUTTON_NAME = "INBUTTON";
	private static final String PUNCH_OUT_BUTTON_NAME = "OUTBUTTON";
	private static final String PUNCH_EARLYSTART_BUTTON_NAME = "EARLYSTARTBUTTON";
	private static final String BUTTON_BACKGROUND_COLOR = "WHITE";
	private static final String HIGHLIGHTED_BUTTON_TEXT_COLOR = "BLACK";
	private static final String UNHIGHLIGHTED_BUTTON_TEXT_COLOR = "GREY";
	
	public static final String PARAM_IS_EARLY_START = "IsEarlyStart";
	public static final String PARAM_START_TIME = "StartTime";
	public static final String EARLY_START_VALUE_FALSE = "0";
	public static final String EARLY_START_VALUE_TRUE = "1";
	public static final String PARAM_PUNCH_TYPE = "PunchType";
	public static final String PARAM_PUNCH_TYPE_PUNCH_IN = "PUNCHIN";
	public static final String PARAM_PUNCH_TYPE_PUNCH_OUT = "PUNCHOUT";
	
	public static final String PARAM_DO_NOT_REDIRECT = "NOREDIRECT";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		String sPinNumber = clsStringFunctions.filter(request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER));
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    //Get a session for this user.
	    HttpSession CurrentSession = request.getSession(true);
	    CurrentSession.setMaxInactiveInterval(TimeCardUtilities.MAX_SESSION_INTERVAL);
	    
	    //check for valid db name
	    if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB) == null){
	    	//if there is no conf name passed in, check session for stored passwd
	    	if ((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) == null){
	    		//there is no conf name, go back to login screen
	    		CurrentSession.invalidate();
	    		out.println("<BR>The current session is void. Please login again.");
	    	}else{
	    		//a conf name is already stored in session, do nothing.
	    	}
	    }else{
    		//store this conf name into session. overwrite any old one.
	    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB, request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB));
	    }
	    
		String sDBID = "";
		try {
			sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			out.println("<BR>Error reading 'db' attribute from current session - " + e1.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
		
		//Get the company information:
		String sCompanyName = "";
		try {
			sCompanyName = getCompanyName(sDBID);
		} catch (Exception e2) {
			out.println("<BR>" + e2.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
		CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, sCompanyName);
		
	    String title = ConnectionPool.WebContextParameters.getInitProgramTitle(getServletContext()) + " - User Login";
	    String subtitle = "";
	    String sDatabaseServer = "";
	    
	   try{
		   sDatabaseServer = TimeCardUtilities.getDatabaseServer(request, null, getServletContext());
	   }catch(Exception e){
		   out.println("<BR>Error [1541533772] "+e.getMessage());
	   }
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithOnLoadCommand(
	    	title, 
	    	subtitle, 
	    	TimeCardUtilities.BACKGROUND_COLOR_FOR_USER_SCREENS, 
	    	clsBrowserReader.getCollectInfoCommand())
	    );
	    
	    //go back to login screen after specified number of seconds
	    if (request.getParameter(PARAM_DO_NOT_REDIRECT) == null){
	    	String sTimeInSecondsToRefresh_After_Waiting = TIME_IN_SECONDS_TO_REFRESH_AFTER_WAITING;
	    	if (bDebugMode){
	    		sTimeInSecondsToRefresh_After_Waiting = "0";
	    	}
		    out.println("<META http-equiv='Refresh' content='" + sTimeInSecondsToRefresh_After_Waiting + ";URL="
		    	+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
	    		+ MainLogin.CLASS_NAME
		    	+ "?db=" + sDBID
		    	+ "'>"
		    );
	    }
	    out.println(clsBrowserReader.getJavascriptForReading(MAIN_FORM_NAME));
	    
	    out.println(sCommandScripts(RECORD_USERS_GPS_LOCATION));
	    
		out.println(
				"<DIV style = \" color: black; font-family: arial; font-weight: bold; font-size: normal;  \" >" + "\n"
				+ ConnectionPool.WebContextParameters.getInitProgramTitle(getServletContext())
				+ " version " + TimeCardUtilities.sProgramVersion
				+ " last updated " + TimeCardUtilities.sLastUpdated
				+ " currently running on server <B>" + clsServletUtilities.getHostName()
				+ "</B>, using database server <B>" + sDatabaseServer
				+ "</B> for company " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME) + "." + "\n"
				+ "</DIV>" + "\n"
				+ "<BR>" + "\n"
		);
	    
		if (bDebugMode){
			out.println("<BR><FONT COLOR=RED><B>CAUTION! RUNNING IN DEBUG MODE!</B></FONT><BR>" + "\n");
		}
		
	    //check for valid pin number
	    if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER) == null){
	    	//if there is no passwd passed in, check session for stored passwd
	    	if ((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER) == null){
	    		//there is no valid passwd, go back to login screen
	    		CurrentSession.invalidate();
	    		out.println("<BR>The current session is void. Please login again.");
				out.println("</BODY></HTML>");
				return;
	    	}else{
	    		//a pin is already stored in session, do nothing.
	    	}
	    }else{
	    	//store this pinnumber into session. overwrite any old one.
	    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER, request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER));
	    }	    
	    
	    String sSQL = "";
	    
	    //Try to get the user info using the Pin number:
	    try{
	        sSQL = "SELECT"
	        	+ " " + "*" 
	        	+ " FROM " + Employees.TableName + ", " + Departments.TableName 
	        	+ " WHERE (" 
	        		+ "(" + Employees.iDepartmentID + " = " + Departments.iDeptID + ")"
	        		+ " AND (" + Employees.sPinNumber + " = '" + sPinNumber + "')"
	        	+ ")"
	        ;
	        ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(
        		sSQL, 
        		getServletContext(), 
        		(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
       		);
	        
		    if (rsEmployeeInfo.next()){
		    	if (rsEmployeeInfo.getInt(Employees.TableName + "." + Employees.iActive) == 0){
		    		throw new Exception("Error [20192141616325] " + " This user has been set to inactive - contact an administrator for help.");
		    	}
		    	String sEmployeeID = rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeID);
		    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, sEmployeeID);
		    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, sEmployeeID);
		    	CurrentSession.setAttribute(clsServletUtilities.SESSION_PARAM_FULL_USER_NAME, 
		    		rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeFirstName) + " "
					+ rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeLastName)
					+ " - " + sCompanyName
		    	);
		    	
		    	try {
					out.println(
						getTextBody(
							rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeFirstName),
							rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeLastName),
							rsEmployeeInfo.getString(Employees.TableName + "." + Employees.sEmployeeID),
							sDBID,
							rsEmployeeInfo.getString("tStartTime"),
							response,
							rsEmployeeInfo.getString(Employees.sEmployeeFirstName).trim() + " " + rsEmployeeInfo.getString(Employees.sEmployeeLastName).trim(),
							getServletContext()
						)
					);
				} catch (Exception e) {
					out.println("<BR>"
				    	+ "<DIV style = \" color: red; font-family: arial; font-style: italic; font-weight: bold; font-size: large;  \" >"
				    	+ e.getMessage()
				    	+ "</DIV>");
				}
		    }else{
		    	out.println("<BR>"
		    		+ "<DIV style = \" color: red; font-family: arial; font-style: italic; font-weight: bold; font-size: large;  \" >"
		    		+ "The pin code is not valid. Please try again."
		    		+ "</DIV>"
		    	);	
		    	out.println("<META http-equiv='Refresh' content='" + TIME_IN_SECONDS_TO_REFRESH_ON_INVALID_PIN + ";URL="
		    			+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
		    			+ MainLogin.CLASS_NAME
		    			+ "?db=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) + "'>");
		    }
		    rsEmployeeInfo.close();
		    
	    } catch (Exception ex){
	        // handle any errors
	        out.println("<BR>Error in UserLogin - " + ex.getMessage());
	    }
	    out.println("</BODY></HTML>");
	}
	private String getTextBody(
		String sFirstName,
		String sLastName,
		String sEmployeeID,
		String sDBID,
		String sStartTime,
		HttpServletResponse responseObject,
		String sUser,
		ServletContext context
		) throws Exception{
		String s = "";
		
    	s += "\n"
    		+ "<BR>"
    		+ "<DIV style = \" text-align:left; font-family: arial; font-size: large; font-weight: normal; width:" + SCREEN_WIDTH_USAGE_PERCENTAGE + "% \" >"
    		+ "Hello, " 
    		+ sFirstName 
    		+ " " 
    		+ sLastName 
    		+ ", do you want to punch in or punch out?"
    		+ "</DIV>"
    		+ "\n"
    	;
    	
    	try {
			s += "<BR>"	+ getPunchButtonsTable(responseObject, sStartTime, bPunchOutButtonShouldBeHighlighted(sEmployeeID, sDBID, sStartTime));
		} catch (Exception e1) {
			throw new Exception("Error [1510085910] displaying punch buttons - " + e1.getMessage());
		}
    	
    	s +=
    		"\n"
    		+ "<DIV style = \" text-align:center; font-family: arial; font-size: x-large; font-weight: bold; width:" + SCREEN_WIDTH_USAGE_PERCENTAGE + "% \" >" + "\n"
    		+ "  Current Time: " + getCurrentDateAndTime(sDBID, sUser, context) + "\n"
    		+ "</DIV>" + "\n"
    		+ "\n"
    	;
    	
    	s += "<TABLE BORDER=1 WIDTH=" + SCREEN_WIDTH_USAGE_PERCENTAGE + "%>" + "\n";

    	//Print the table of previous punches here:
    	
    	//Header row:
    	s += "  <TR bgcolor=\"" + PUNCH_LIST_HEADING_BACKGROUND_COLOR + " \" >" + "\n"
    	
    		+ "    <TD style = \" text-align:center; font-family: arial; font-weight: bold; font-size: x-large; width: 50%; vertical-align: text-bottom; color: " + PUNCH_LIST_HEADING_FOREGROUND_COLOR + "; \" >"
    		+ "Previous IN(s)"
    		+ "</TD>" + "\n"
    		
    		+ "    <TD style = \" text-align:center; font-family: arial; font-weight: bold; font-size: x-large; width: 50%; vertical-align: text-bottom;  color: " + PUNCH_LIST_HEADING_FOREGROUND_COLOR + "; \" >"
    		+ "Previous OUT(s)"
    		+ "</TD>" + "\n"
    		
    		+ "  </TR>" + "\n"
    	;
    		
    	//Get the user's unposted punch history:
		String sEndingDate = getTodaysDate(sDBID);
		String sSQL = "SELECT * FROM " + TimeEntries.TableName
			+ " WHERE (" 
			
				//Only read time entries for this employee:
				+ "(" + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')"
				
				//And only read time entries which have EITHER a punch in or a punch out between 0000-00-00 and today:
				+ " AND ("
					+ "((" + TimeEntries.dtInTime + " >= '" + "0000-00-00" + "') AND (" + TimeEntries.dtInTime + " <= '" + sEndingDate + "'))"
					+ "OR ((" + TimeEntries.dtOutTime + " >= '" + "0000-00-00" + "') AND (" + TimeEntries.dtOutTime + " <= '" + sEndingDate + "'))"
				+ ")"
					
				//And only get UNposted entries:
				+ " AND (" 
					+ "(" + TimeEntries.sPeriodDate + " = '0000-00-00')"
					+ " OR (" + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')"
				+ ")"
			
			//Get the most recent one first:
			+ ") ORDER BY " + TimeEntries.dtInTime
		;
		
		long lTotalOpenTimeInSeconds = 0;
		SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd-yyyy hh:mm:ss a");
		
        try {
			ResultSet rsHistory = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			boolean bOddRow = true;
			while (rsHistory.next()){
				String sBackGroundColor = "";
				if (bOddRow){
					sBackGroundColor = ODD_ROW_BACKGROUND_COLOR;
				}else{
					sBackGroundColor = EVEN_ROW_BACKGROUND_COLOR;
				}
				
				boolean bCalculatable = true;
				String sDisplayedPunchInTime = "";
				if (rsHistory.getString(TimeEntries.dtInTime).compareTo("0000-00-00 00:00:00") == 0){
					sDisplayedPunchInTime = NO_PUNCH_IN_MESSAGE;
					bCalculatable = false;
				}else{
					try {
						sDisplayedPunchInTime = formatter.format(rsHistory.getTimestamp(TimeEntries.dtInTime));
					} catch (Exception e) {
						rsHistory.close();
						throw new Exception("Error [1510166092] formatting sDisplayedPunchInTime '" + sDisplayedPunchInTime + "' as 'MM-dd-yyyy hh:mm:ss a' date.");
					}
				}
				String sDisplayedPunchOutTime = "";
				if (rsHistory.getString(TimeEntries.dtOutTime).compareTo("0000-00-00 00:00:00") == 0){
					sDisplayedPunchOutTime = NO_PUNCH_OUT_MESSAGE;
					bCalculatable = false;
				}else{
					try {
						sDisplayedPunchOutTime = formatter.format(rsHistory.getTimestamp(TimeEntries.dtOutTime));
					} catch (Exception e) {
						rsHistory.close();
						throw new Exception("Error [1510166093] formatting sDisplayedPunchOutTime '" + sDisplayedPunchOutTime + "' as 'MM-dd-yyyy hh:mm:ss a' date.");
					}
				}
				
				s += "  <TR bgcolor = \"" + sBackGroundColor + "\" >" + "\n";
				
				
				s += "    <TD style = \" text-align:center; font-family: arial; font-weight: bold; \" >"  
					+ sDisplayedPunchInTime
					+ "</TD>" + "\n"
				;
				
				s += "    <TD style = \" text-align:center; font-family: arial; font-weight: bold; \" >"
					+ sDisplayedPunchOutTime
					+ "</TD>" + "\n"
				;
				
				s += "  </TR>" + "\n";
							
				//Calculate the total time of today if calculatable
				if (bCalculatable){
					Timestamp tsInTime = Timestamp.valueOf(rsHistory.getString(TimeEntries.dtOutTime));
					Calendar calInTime = Calendar.getInstance();
					calInTime.setTimeInMillis(tsInTime.getTime());
					
					Timestamp tsOutTime = Timestamp.valueOf(rsHistory.getString(TimeEntries.dtInTime));
					Calendar calOutTime = Calendar.getInstance();
					calInTime.setTimeInMillis(tsOutTime.getTime());
					
					//Get the number of seconds worked in the day:
					long lTimeWorkedInSeconds = (tsInTime.getTime() - tsOutTime.getTime()) / 1000L;  //.getTime() gives us milliseconds

					//If the time worked is more than 5 hours and it's not a Sunday, then we automatically
					// take out 30 minutes for lunch:
					if (
						//If the time actually worked on this day is more than or equal to 5 hrs (18000 seconds), 
						(lTimeWorkedInSeconds >= MINIMUM_HOURS_TO_FORCE_LUNCHTIME_DEDUCTION * 60L * 60L)
						
						//AND neither the starting day nor the ending day is a Sunday,
						&& (calInTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) 
						&& (calOutTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)

					){
						//Then subtract 30 minutes (30 * 60 = 1800 seconds) from the time duration, and add it to the total:
						lTotalOpenTimeInSeconds = lTotalOpenTimeInSeconds + lTimeWorkedInSeconds - (NUMBER_OF_MINUTES_DEDUCTED_FOR_LUNCH * 60L);
					}else{
						//Otherwise, just add the time worked:
						lTotalOpenTimeInSeconds = lTotalOpenTimeInSeconds + lTimeWorkedInSeconds;
					}
				}
				bOddRow = !bOddRow;
			}
			rsHistory.close();
		} catch (Exception e1) {
			throw new Exception("Error [1517600037] reading time entries with SQL '" + sSQL + " - " + e1.getMessage());
		}
    	
    	//finish the table 
    	s += "</TABLE>" + "\n";
    	
    	s += "\n"
    		+ "<DIV style = \" text-align:center; font-family: arial; font-size: x-large; font-weight: bold; width:" + SCREEN_WIDTH_USAGE_PERCENTAGE + "% \" >" + "\n"
    		// Divide the total number of seconds worked by 60 seconds per minute, and 60 seconds per hour (3600) to get the number of HOURS worked, with a fraction:
    		+ "  Your total OPEN time: " + TimeCardUtilities.RoundHalfUp(lTotalOpenTimeInSeconds/3600.0, 2) + " (Hrs)" + "\n"
    		+ "</DIV>" + "\n"
    		+ "\n"
    	;
    	
    	//Print the web form here:
    	s += "\n" + "<FORM"
    		+ " ID=\"" + MAIN_FORM_NAME + "\""
    		+ " NAME=\"" + MAIN_FORM_NAME + "\""
    		+ " METHOD=\"" + "POST" + "\""
    		+ " ACTION=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + FORM_ACTION_TARGET_CLASS  + "\""
    		+ ">" + "\n";
    	//TODO - put any real form fields in here:
    	s += "<INPUT TYPE=HIDDEN"
    		+ " NAME = \"" + PARAM_IS_EARLY_START + "\""
    		+ " ID = \"" + PARAM_IS_EARLY_START + "\""
    		+ " VALUE= \"" + EARLY_START_VALUE_FALSE + "\""
    		+ ">" + "\n"
    		+ "<INPUT TYPE=HIDDEN"
    		+ " NAME = \"" + PARAM_START_TIME + "\""
    		+ " ID = \"" + PARAM_START_TIME + "\""
    		+ " VALUE= \"" + sStartTime + "\""
    		+ ">" + "\n"
    		//Record whether it was a punch IN or a punch  OUT:
    		+ "<INPUT TYPE=HIDDEN"
    		+ " NAME = \"" + PARAM_PUNCH_TYPE + "\""
    		+ " ID = \"" + PARAM_PUNCH_TYPE + "\""
    		+ " VALUE= \"" + "" + "\""
    		
    		+ ">" + "\n"
    	;

		//Values for geocodes:
		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LATITUDE + "\" VALUE=\"" + "" + "\""
			+ " id=\"" + PARAMETER_USER_LATITUDE + "\""
			+ ">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LONGITUDE + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_USER_LONGITUDE + "\""
				+ ">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED 
				+ "\" VALUE=\"" + PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED + "\""
				+ " id=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\""
				+ ">\n";
    	
		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\""
				+ ">\n";
		
		
    	s += "\n" + clsBrowserReader.getFormFields() + "\n";
    	
    	s += "</FORM>" + "\n";
    	
    	//Display the selection screen for the MADGIC report here:
    	try {
			if (TimeCardUtilities.isFunctionPermitted(
				sEmployeeID, 
				sDBID,
				AccessControlFunctionList.ViewPersonalMADGICReport,
				getServletContext())){
					s += getMADGICSelection();
			}
		} catch (Exception e) {
			throw new Exception("Error reading user permissions - " + e.getMessage() + ".");
		}
    	
    	//Display the option to run Milestones report here:
    	try {
			if (TimeCardUtilities.isFunctionPermitted(
				sEmployeeID, 
				sDBID,
				AccessControlFunctionList.ViewPersonalMilestoneReport,
				getServletContext())){
					s += getMilestoneSelection();
			}
		} catch (Exception e) {
			throw new Exception("Error reading user permissions - " + e.getMessage() + "." 
			);
		}
    	
		return s;
	}
	private boolean bPunchOutButtonShouldBeHighlighted(String sEmployeeID, String sDBID, String sStartTime) throws Exception{
		
		boolean bPunchOutButtonShouldBeHighlighted = false;
		
		//We're going to get the very LAST UNposted punch event here:
		
		//If the most recent punch is an OUT, or if we don't have ANY unposted time entries, then
		//the punch OUT should not be highlighted.
		
		//So ONLY if the most recent unposted time entry is a punch IN, will we want the punch OUT highlighted:
		String sEndingDate = getTodaysDate(sDBID);
		String sSQL = "";
		try {
			sSQL = "SELECT * FROM " + TimeEntries.TableName
				+ " WHERE (" 
				
					//Only read time entries for this employee:
					+ "(" + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')"
					
					//And only read time entries which have EITHER a punch in or a punch out between 0000-00-00 and today:
					+ " AND ("
						+ "((" + TimeEntries.dtInTime + " >= '" + "0000-00-00" + "') AND (" + TimeEntries.dtInTime + " <= '" + sEndingDate + "'))"
						+ "OR ((" + TimeEntries.dtOutTime + " >= '" + "0000-00-00" + "') AND (" + TimeEntries.dtOutTime + " <= '" + sEndingDate + "'))"
					+ ")"
						
					//And only get UNposted entries:
					+ " AND (" 
						+ "(" + TimeEntries.sPeriodDate + " = '0000-00-00')"
						+ " OR (" + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')"
					+ ")"
				
				//Get the most recent one first:
				+ ") ORDER BY " + TimeEntries.dtInTime + " DESC"
			;
			ResultSet rsHistory = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rsHistory.first()){
				
				//Only if the most UNposted time entry is a punch IN (which is the case if the out time is blank),
				//Then we'll want the punch OUT to be highlighted:
				if (rsHistory.getString(TimeEntries.dtOutTime).compareTo("0000-00-00 00:00:00") == 0){
					bPunchOutButtonShouldBeHighlighted = true;
				}
			}
			rsHistory.close();
		} catch (Exception e) {
			throw new Exception("Error [1517600219] determining default punch button with SQL '" + sSQL + " - " + e.getMessage());
		}
    	
    	return bPunchOutButtonShouldBeHighlighted;
	}
	private String getTodaysDate(String sDBID) throws Exception{
		String s = "";
		
	    //get the database server time
	    Timestamp tsRightNow = new Timestamp(System.currentTimeMillis());
    	String sSQL = TimeCardSQLs.Get_Current_Time_SQL();
    	try {
			ResultSet rsTime = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rsTime.next()){
				tsRightNow = rsTime.getTimestamp("LOCALTIME()");
			}
			rsTime.close();
		} catch (Exception e) {
			throw new Exception("Error [1510094709] getting current date - " + e.getMessage());
		}
    	
	    Date RightNow = new Date(tsRightNow.getTime());
    	Calendar c = Calendar.getInstance();
    	c.setTime(RightNow);
    	c.add(Calendar.DAY_OF_MONTH, 1);
    	SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
    	s = SQLDateformatter.format(c.getTime());
    
		return s;
	}
	
	private String getCurrentDateAndTime(String sDBID, String sUser, ServletContext context) throws Exception{
		clsDBServerTime objTime = new clsDBServerTime(sDBID, sUser, context);
		return objTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY);
	}
	
	private String getCompanyName(String sDBID) throws Exception{
		String s = "";
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				s = rs.getString(TCSTablecompanyprofile.sCompanyName);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1510092832] reading company name - no record found.");
			}
		} catch (SQLException e) {
			throw new Exception("Error [1510092833] reading company name with SQL: '" + sSQL + "' - " + e.getMessage() + ".");
		}
		return s;
	}
	private String getPunchButtonsTable(HttpServletResponse responseObject, String sStartTime, boolean bHighlightPunchOutButton) throws Exception{
		String s = "";

    	s += "\n" + "<TABLE BORDER=0 WIDTH=" + SCREEN_WIDTH_USAGE_PERCENTAGE + "%>" + "\n";
    	
    	//Punch in:
    	s += "  <TR>" + "\n";
		s += "    <TD  WIDTH=50% style = \" text-align:center; \" >"
			+ createPunchButton(!bHighlightPunchOutButton, PUNCH_IN_BUTTON_NAME, PUNCH_IN_BUTTON_LABEL, "punch('" + PUNCH_IN_BUTTON_NAME + "', '" + sStartTime + "')")
    		+ "</TD>" + "\n"
    	;

		//Punch out:
		s += "    <TD WIDTH=50% style = \" text-align:center; \" >"
			+ createPunchButton(bHighlightPunchOutButton, PUNCH_OUT_BUTTON_NAME, PUNCH_OUT_BUTTON_LABEL, "punch('" + PUNCH_OUT_BUTTON_NAME + "', '')")
    		+ "</TD>" + "\n"
    	;
    	
    	s +=  "  </TR>" + "\n";
    	
    	//Add a row just to make vertical space between the buttons:
    	s += "  <TR>" + "\n"
    		+ "    <TD>&nbsp;</TD>" + "\n"
    		+ "    <TD>&nbsp;</TD>" + "\n"
    		+ "  </TR>" + "\n"
    	;
    	
    	//Early start:
    	s += "  <TR>" + "\n";
		s += "    <TD WIDTH=50% style = \" text-align:center; \" >"
			+ createPunchButton(!bHighlightPunchOutButton, PUNCH_EARLYSTART_BUTTON_NAME, PUNCH_EARLYSTART_BUTTON_LABEL, "punch('" + PUNCH_EARLYSTART_BUTTON_NAME + "', '" + sStartTime + "')")
    		+ "</TD>" + "\n"
    	;

		//Blank:
		s += "    <TD WIDTH=50% style = \" text-align:center; \" >"
			+ "&nbsp;"
    		+ "</TD>" + "\n"
    	;
    	
    	s +=  "  </TR>" + "\n";
    	
    	s += "</TABLE>" + "\n";
	        	
		return s;
	}
	private String getMADGICSelection(){
		String s = "<BR>";
		
		s += "\n" + "<DIV style = \" text-align:center; width:" + SCREEN_WIDTH_USAGE_PERCENTAGE + "% \" >" + "\n" + "\n";
		
		s += "<FORM NAME=\"" + MADGICReportGenerate.MADGIC_REPORT_FORM_NAME + "\""
				+ " ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.MADGICReportGenerate\""
				+ " >\n";
		
		s += "\n" + "<B><I>Display MADGIC Report details for reporting period:</I></B> "
			+ TCMADGICEvent.createReportingPeriodListBox()
			+ "&nbsp;&nbsp;" + "\n"
		;
		
		s += "<INPUT TYPE=\"SUBMIT\" NAME=\"" + MADGICReportGenerate.PARAM_DISPLAY_REPORT + "\" VALUE=\"----Display----\">\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MADGICReportGenerate.PARAM_CHECKBOX_SHOW_DETAILS + "\" VALUE=\"" + "Y" + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_DEPT_OR_EMPLOYEE + "\" VALUE=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MADGICReportGenerate.PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID + "\" VALUE=\"" + "Y" + "\">\n";
		
		s += clsBrowserReader.getFormFields();
		
		s += "</FORM>\n";
		
		s += "</DIV>" + "\n";
		
		return s;
	}
	private String getMilestoneSelection(){
		String s = "<BR><BR>";
		
		s += "<FORM NAME=\"" + MilestonesReportGenerate.MILESTONES_REPORT_FORM_NAME + "\""
				+ " ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.MilestonesReportGenerate\""
				+ " >\n";
		
		s += "<B><I>Display Milestone Report:</I></B>&nbsp;&nbsp; "
		;
		
		s += "<INPUT TYPE=\"SUBMIT\" NAME=\"" + MilestonesReportGenerate.PARAM_DISPLAY_REPORT + "\" VALUE=\"----Display----\">\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MilestonesReportGenerate.PARAM_CHECKBOX_SHOW_DETAILS + "\" VALUE=\"" + "Y" + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_TYPE_OR_EMPLOYEE + "\" VALUE=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + MilestonesReportGenerate.PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID + "\" VALUE=\"" + "Y" + "\">\n";
		
		s += "</FORM>\n";
		
		return s;
	}
	
	private String createPunchButton(boolean bHighlighted, String sButtonName, String sButtonValue, String sCalledFunction){
		String sTextColor = "";
		if (bHighlighted){
			sTextColor = HIGHLIGHTED_BUTTON_TEXT_COLOR;
		}else{
			sTextColor = UNHIGHLIGHTED_BUTTON_TEXT_COLOR;
		}
		return "<button type=\"button\""
			+ " style = \"" 
			+ " background-color: " + BUTTON_BACKGROUND_COLOR + "; "
			//+ "border: none;"
			+ " border-style: solid; "
			+ " border-color: black; "
			+ " color: " + sTextColor + "; "
			+ " padding: 6px 6px; "
			+ " text-align: center; "
			+ " text-decoration: none; "
			+ " display: inline-block; "
			+ " font-size: xx-large; "
			+ " font-weight: bold; "
			+ " width: 40%; "
			+ " box-shadow: 0 12px 16px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19); "
			+ " \" "
			+ " value=\"" + sButtonValue + "\" "
			+ " name=\"" + sButtonName + "\" "
			+ " onClick=\"" + sCalledFunction + "; \">"
			+  sButtonValue 
			+ "</button>\n"
			;
	}
	private String sCommandScripts(boolean bRecordUserLocation){
		String s = "";
		

		//Geocode functions:
		s += "  <script type=\"text/javascript\"\n"
		    //+ "    src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">\n"
		    + "    src=\"https://maps.googleapis.com/maps/api/js?key=" + SMGoogleMapAPIKey.SMCP_GMAPS_API_KEY1 + "\">\n"
		    + "  </script>\n"
			;
			s += "<script type=\"text/javascript\">\n";
			s += "window.onload = triggerInitialGeocode();\n";
			
			s += "var watchID;\n";
			s += "var geoLoc;\n";
			
			s += "var t;\n";
			s += "var timer_is_on=0;\n";

			s += "\n";
			s += "function errorHandler(err) {\n";
			s += "    if(err.code == 1) {\n";
			//s += "        alert('Error: Access is denied!');\n";
			s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\").value = '" 
					+ PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED + "';\n";
			s += "    }else if( err.code == 2) {\n";
			s += "        alert('Error: Position is unavailable!');\n";
			s += "    }else{\n";
			s += "        alert('Geocoding err.code = ' + err.code); \n";
			s += "    } \n";
			s += "}\n";
			s += "\n";

			s += "function triggerInitialGeocode(){\n";
			if (bRecordUserLocation){
				s += "    if(navigator.geolocation){\n";
				//s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\").value = '" + PARAMETER_LOCATION_RECORDING_VALUE_SUPPORTED + "'; \n";
				          // timeout at 60000 milliseconds (60 seconds)
				s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
				//s += "        geoLoc = navigator.geolocation;\n";
				s += "        navigator.geolocation.getCurrentPosition(initialGeocodeTrigger, errorHandler, options);\n";
				//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
				s += "    }else{\n";
				//s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\").value = '" + PARAMETER_LOCATION_RECORDING_VALUE_NOT_SUPPORTED + "'; \n";
				s += "        alert('Browser does not support geolocation!');\n";
				s += "    }\n";
			}
			s += "}\n";

			//This function is just a dummy to allow us to force the geocoding to happen when the screen first loads - 
			//this allows the user to choose, that first time, whether to allow the site to record his location:
			s += "function initialGeocodeTrigger(position){\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
					+ "position.coords.latitude;\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
					+ "position.coords.longitude;\n"
				+ "}\n"
			;
			
			s += "function getGeocode(){\n";
			if (bRecordUserLocation){
				s += "    if(navigator.geolocation){\n";
				s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\").value = '" + PARAMETER_LOCATION_RECORDING_VALUE_SUPPORTED + "'; \n";
				          // timeout at 60000 milliseconds (60 seconds)
				s += "        var options = {enableHighAccuracy:false, maximumAge:120000, timeout:45000};\n";
				//s += "        geoLoc = navigator.geolocation;\n";
				s += "        navigator.geolocation.getCurrentPosition(submitValues, errorHandler, options);\n";
				//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
				
				//Testing only:
				//s += "    alert('Lat = ' + document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value);\n";
				//s += "    alert('Long = ' + document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value);\n";
				s += "    }else{\n";
				s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER + "\").value = '" + PARAMETER_LOCATION_RECORDING_VALUE_NOT_SUPPORTED + "'; \n";
				s += "        alert('Browser does not support geolocation!');\n";
				s += "    }\n";
			}
			s += "}\n";
			
			s += "function submitValues(position){\n"
					
				//TESTING ONLY:
				//+ "    alert('position.coords.latitude = ' + position.coords.latitude);\n"
				//+ "    alert('position.coords.longitude = ' + position.coords.longitude);\n"
					
				+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
					+ "position.coords.latitude;\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
					+ "position.coords.longitude;\n"
				
				//+ "    alert('position.coords.speed = ' + position.coords.speed); \n"
				//+ "    alert('position.coords.altitude = ' + position.coords.altitude); \n"
				//+ "    alert('position.coords.accuracy = ' + position.coords.accuracy); \n"
				//+ "    alert('position.coords.altitudeaccuracy = ' + position.coords.altitudeaccuracy); \n"
				//+ "    alert('position.coords.timestamp = ' + position.coords.timestamp); \n"
				
				//TESTING ONLY:
				//+ "    alert('1 document.getElementById(USERLATITUDE).value = ' + document.getElementById(\"USERLATITUDE\").value);\n"
				//+ "    alert('1 document.getElementById(USERLONGITUDE).value = ' + document.getElementById(\"USERLONGITUDE\").value);\n"
				+ "}\n"
			;
			
			s += "function punch(iPunchType, sStartTime){" + "\n"
			+ "  var sWindowLocation = '';" + "\n"

			+ "  if (iPunchType == '" + PUNCH_IN_BUTTON_NAME + "'){" + "\n"
			+ "    document.getElementById(\"" + PARAM_PUNCH_TYPE + "\").value = \"" + PARAM_PUNCH_TYPE_PUNCH_IN + "\";\n"
			+ "  }" + "\n"
			
			+ "  if (iPunchType == '" + PUNCH_OUT_BUTTON_NAME + "'){" + "\n"
			+ "    document.getElementById(\"" + PARAM_PUNCH_TYPE + "\").value = \"" + PARAM_PUNCH_TYPE_PUNCH_OUT + "\";\n"
			+ "  }" + "\n"
			
			+ "  if (iPunchType == '" + PUNCH_EARLYSTART_BUTTON_NAME + "'){" + "\n"
			+ "    document.getElementById(\"" + PARAM_PUNCH_TYPE + "\").value = \"" + PARAM_PUNCH_TYPE_PUNCH_IN + "\";\n"
			+ "    document.getElementById(\"" + PARAM_IS_EARLY_START + "\").value = \"" + EARLY_START_VALUE_TRUE + "\";\n"
			+ "  }" + "\n"

			+ "    getGeocode();\n"
			+ "    " + clsBrowserReader.getCollectInfoCommand() + ";\n"
			//+ "    alert('submitting form now');\n"
			//+ "    alert('document.getElementById(USERLATITUDE).value = ' + document.getElementById(\"USERLATITUDE\").value);\n"
			//+ "    alert('document.getElementById(USERLONGITUDE).value = ' + document.getElementById(\"USERLONGITUDE\").value);\n"
			+ "    document.forms[\"" + MAIN_FORM_NAME + "\"].submit();\n"
			
			+ "}" + "\n"
		;
			
			s += "</SCRIPT>" + "\n"
		;
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
