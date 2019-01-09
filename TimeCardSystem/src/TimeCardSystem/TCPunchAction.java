package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsBrowserReader;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.Departments;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCTablerawpunchevents;
import TCSDataDefinition.TimeEntries;

/** Servlet that inserts punch in/out records into the the time entry table.*/

public class TCPunchAction extends HttpServlet{

	private static final String ODD_ROW_BACKGROUND_COLOR = "WHITE";
	private static final String EVEN_ROW_BACKGROUND_COLOR = "LIGHTGRAY";
	private static final String HIGHLIGHT_ROW_BACKGROUND_COLOR = "LIGHTGREEN";
	
	private static final String TIME_IN_SECONDS_TO_REFRESH_AFTER_WAITING = "20";
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
  
		response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
		PrintWriter out = response.getWriter();

		String title = "Time Card System";
		String subtitle = "";
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_USER_SCREENS, "Arial"));
		
		out.println("<META http-equiv=\"Refresh\" content=\"" + TIME_IN_SECONDS_TO_REFRESH_AFTER_WAITING + ";URL="
			+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
			+ MainLogin.CLASS_NAME
			+ "?db=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) 
			+ "\">")
		;

		String sEmployeeID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		String sStartTime = request.getParameter(UserLogin.PARAM_START_TIME);
		boolean bIsEarlyStart = clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAM_IS_EARLY_START, request).compareToIgnoreCase(UserLogin.EARLY_START_VALUE_TRUE) == 0;
		
		//Process punch in or out, depending on what button was clicked:
		String sPunchType = clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAM_PUNCH_TYPE, request);
		
		//Get the latitude and longitude:
		String sUserLatitude = clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAMETER_USER_LATITUDE, request);
		String sUserLongitude = clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAMETER_USER_LONGITUDE, request);
		int iGeocodingAllowedByUser = 0;
		if (clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAMETER_LOCATION_RECORDING_ALLOWED, request).compareToIgnoreCase(UserLogin.PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED) == 0){
			iGeocodingAllowedByUser = 1;
		}
		int iGeocodingSupportedByBrowser = 0;
		if (clsManageRequestParameters.get_Request_Parameter(UserLogin.PARAMETER_LOCATION_RECORDING_SUPPORTED_IN_BROWSER, request).compareToIgnoreCase(UserLogin.PARAMETER_LOCATION_RECORDING_VALUE_SUPPORTED) == 0){
			iGeocodingSupportedByBrowser = 1;
		}
		
		//System.out.println("[1517255626] Lat: '" + sUserLatitude + "', Long: '" + sUserLongitude + "'");
		//System.out.println("[1517255627] request.PARAM_PUNCH_TYPE(): '" + TimeCardUtilities.get_Request_Parameter(UserLogin.PARAM_PUNCH_TYPE, request) + "'");
		//System.out.println("[1517255628] request.PARAMETER_USER_LATITUDE(): '" + TimeCardUtilities.get_Request_Parameter(UserLogin.PARAMETER_USER_LATITUDE, request) + "'");
		
		String sGeoCode = "";
		if (
			(sUserLatitude.compareToIgnoreCase("") != 0)
			&& (sUserLongitude.compareToIgnoreCase("") != 0)
		){
			sGeoCode = sUserLatitude + "," + sUserLongitude;
		}
		
		//Debugging to catch 'phantom' punch ins:
		//Special testing to catch 'phantom' punch ins for Jeff Treiber - TJR
		if (sEmployeeID.compareToIgnoreCase("TRE001") == 0){
			TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
			String sPunchDesc = "";
			if (sPunchType.compareToIgnoreCase(UserLogin.PARAM_PUNCH_TYPE_PUNCH_IN) == 0){
				sPunchDesc = "IN";
			}else{
				sPunchDesc = "OUT";
			}
			log.writeEntry(
				sEmployeeID,
				TCLogEntry.LOG_OPERATION_MISC_DIAGNOSTIC, 
				"Diagnostics for Jeff Treiber punch" + sPunchDesc,
				TimeCardUtilities.now("MM/dd/yyyy hh:mm:ss z") + "\n"
					+ "request.getLocalMethod() = '" + request.getMethod() + "'" + "\n"
					+ "request.getPathInfo() = '" + request.getPathInfo() + "'" + "\n"
					+ "request.getPathTranslated() = '" + request.getPathTranslated() + "'" + "\n"
					+ "request.getProtocol() = '" + request.getProtocol() + "'" + "\n"
					+ "request.getQueryString() = '" + request.getQueryString() + "'" + "\n"
					+ "request.getRemoteAddr() = '" + request.getRemoteAddr() + "'" + "\n"
					+ "request.getRemoteHost() = '" + request.getRemoteHost() + "'" + "\n"
					+ "request.getRemotePort() = '" + request.getRemotePort() + "'" + "\n"
					+ "request.getRemoteUser() = '" + request.getRemoteUser() + "'" + "\n"
					+ "request.getRequestURI() = '" + request.getRequestURI() + "'" + "\n"
					+ "request.getScheme() = '" + request.getScheme() + "'" + "\n"
					+ "request.getServerName() = '" + request.getServerName() + "'" + "\n"
					+ "request.getServerPort() = '" + request.getServerPort() + "'" + "\n"
					+ "request.getServletPath() = '" + request.getServletPath() + "'" + "\n"
					+ "request.getHeader(\"User-Agent\") = '" + request.getHeader("User-Agent") + "'" + "\n"
					+ "Geocode: '" + sGeoCode + "'\n"
					+ "Screen Max Height: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_MAXHEIGHT, request) + "'" + "\n"
					+ "Screen Max Width: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_MAXWIDTH, request) + "'" + "\n"
					+ "Navigator: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_NAVIGATOR, request) + "'" + "\n"
					+ "Platform: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_PLATFORM, request) + "'" + "\n"
					+ "Screen Height: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_SCREEN_HEIGHT, request) + "'" + "\n"
					+ "Screen Width: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_SCREEN_WIDTH, request) + "'" + "\n"
					+ "Version: '" + clsManageRequestParameters.get_Request_Parameter(clsBrowserReader.FORM_FIELD_VERSION, request) + "'" + "\n",
				"1517538189"
			);
		}
		
		if (sPunchType.compareToIgnoreCase(UserLogin.PARAM_PUNCH_TYPE_PUNCH_IN) == 0){
			try {
				out.println(
					processPunchIn(
						bIsEarlyStart, 
						sDBID,
						sEmployeeID,
						sStartTime,
						request.getRemoteHost(),
						sGeoCode,
						iGeocodingAllowedByUser,
						iGeocodingSupportedByBrowser
					)
				);
			} catch (Exception e) {
				out.println("<BR><FONT COLOR=RED><B>Error [1516815564] processing punch IN - " + e.getMessage() + ".</B></FONT><BR>");
			}
		}
		
		if (sPunchType.compareToIgnoreCase(UserLogin.PARAM_PUNCH_TYPE_PUNCH_OUT) == 0){
			try {
				out.println(
					processPunchOut(
					sEmployeeID,
					sDBID,
					request.getRemoteHost(),
					sGeoCode,
					iGeocodingAllowedByUser,
					iGeocodingSupportedByBrowser
					)
				);
			} catch (Exception e) {
				out.println("<BR><FONT COLOR=RED><B>Error [1516815565] processing punch OUT - " + e.getMessage() + ".</B></FONT><BR>");
			}
		}

		out.println("</BODY></HTML>");
		return;
	}
	private String processPunchIn(
		boolean bIsEarlyStart,
		 String sDBID,
		 String sEmployeeID,
		 String sStartTime,
		 String sRemoteHost,
		 String sGeoCode,
		 int iGeocodingAllowedByUser,
		 int iGeocodingSupportedByBrowser
		) throws Exception{

		String s = "";

		//get the database server time
	
		Timestamp sqlTimeStamp;
		String sSQL = "";;
		try {
			sqlTimeStamp = new Timestamp(System.currentTimeMillis());
			sSQL = "SELECT LOCALTIME()";
			ResultSet rsTime = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID
			);
			if (rsTime.next()){
				sqlTimeStamp = rsTime.getTimestamp("LOCALTIME()");
			}
			rsTime.close();
		} catch (Exception e) {
			throw new Exception("Error [1517600434] getting current time with SQL '" + sSQL + "' - " + e.getMessage());
		}

		Timestamp originalTimeStamp = sqlTimeStamp;

		Date datToday = new Date(sqlTimeStamp.getTime());
		int iEmployeeIsLate = 0;  // Not late = 0; Late = 1
		int iNumberOfMinutesLate = 0;

		//check to see if the time is earlier than employee start time or not, plus early start.

		//get the late grace period length for this employee 
		sSQL = "SELECT " + Departments.dLateGracePeriod 
			+ " FROM " + Employees.TableName + ", " + Departments.TableName 
			+ " WHERE (" 
				+ "(" + Employees.sEmployeeID + " = '" + sEmployeeID + "')" 
				+ " AND (" + Employees.iDepartmentID + " = " + Departments.iDeptID + ")"
			+ ")"
		;
		
		double dGracePeriod;
		try {
			ResultSet rsGracePeriod = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID
			);
			if (rsGracePeriod.next()){
				dGracePeriod = rsGracePeriod.getDouble("dLateGracePeriod");
			}else{
				dGracePeriod = 0;
			}
			rsGracePeriod.close();
		} catch (Exception e) {
			throw new Exception("Error [1517600435] getting grace period with SQL '" + sSQL + "' - " + e.getMessage());
		}

		iNumberOfMinutesLate = (int)TimeCardUtilities.RoundHalfUp(
			(sqlTimeStamp.getTime() - Timestamp.valueOf(datToday.toString() + " " + sStartTime).getTime()) / 60000.0,
			0
		);
		if (iNumberOfMinutesLate <= 0){
			//If the punch in time is earlier than the designated start time, record the start time as the effective starting time.
			//set iLate = 0, not late
			iEmployeeIsLate = 0;
			//if this is not an early start, adjust the time to be the start time of the employee
			if (!bIsEarlyStart){
				sqlTimeStamp = Timestamp.valueOf(datToday.toString() + " " + sStartTime);
			}
		}else if (iNumberOfMinutesLate > 0 && iNumberOfMinutesLate <= dGracePeriod){
			//The punch in time is later than start time, but within the grace period, 
			//so record the (late) punch in time as the employee's effective time.
			//set iLate = 0, not late
			iEmployeeIsLate = 0;
		}else{
			//The punch in time is past the grace period, record punch in time as employee's effective start time.
			//Set iEmployeeIsLate to indicate a late.
			iEmployeeIsLate = 1;
		}

		String sIsEarlyStart = "0";
		if (bIsEarlyStart){
			sIsEarlyStart = "1";
		}
		
		//Get a connection, so we can do a data transaction here:
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), sDBID, "MySQL", this.toString() + " - user: '" + sEmployeeID + "'");
		} catch (Exception e) {
			throw new Exception("Error [1517600876] - couldn't get data connection for DBID '" + sDBID + "' - " + e.getMessage());
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060167]");
			throw new Exception("Error [1517600877] - couldn't start data transaction.");
		}
		
		//Insert the punch in record:
		sSQL = "INSERT INTO " + TimeEntries.TableName + "(" 
			+ " " + TimeEntries.sEmployeeID 
			+ ", " + TimeEntries.dtInTime 
			+ ", " + TimeEntries.dtInTimeOri 
			+ ", " + TimeEntries.iInModified 
			+ ", " + TimeEntries.iEarlyStart 
			+ ", " + TimeEntries.iLate 
			+ ", " + TimeEntries.iLateMinute 
			+ ", " + TimeEntries.mChangeLog 
			+ ", " + TimeEntries.iEntryTypeID 
			+ ") VALUES (" 
			+ "'" + sEmployeeID + "'" 
			+ ", '" + sqlTimeStamp.toString() +  "'" 
			+ ", '" + originalTimeStamp.toString() +  "'" 
			+ ", " + "0" 
			+ ", " + sIsEarlyStart 
			+ ", " + iEmployeeIsLate 
			+ ", " + iNumberOfMinutesLate 
			+ ", '" + "" + "'" 
			+ ", " + "0" 
			+ ")"
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060168]");
			throw new Exception("Error [1517600878] executing SQL '" + sSQL + "' - " + e.getMessage());
		}
		
		String sRecSQL = "INSERT INTO " + TCTablerawpunchevents.TableName + "(" 
			+ " " + TCTablerawpunchevents.sEmployeeID 
			+ ", " + TCTablerawpunchevents.iEntryType 
			+ ", " + TCTablerawpunchevents.iApproved 
			+ ", " + TCTablerawpunchevents.dtTime 
			+ ", " + TCTablerawpunchevents.sIPAddress 
			+ ", " + TCTablerawpunchevents.iInOut
			+ ", " + TCTablerawpunchevents.sgeocode
			+ ", " + TCTablerawpunchevents.igeocodingallowedbyuser
			+ ", " + TCTablerawpunchevents.igeocodingsupportedbybrowser
			+ ") VALUES (" 
			+ " '" + sEmployeeID + "'"
			+ ", " + "0" //Entry type.
			+ ", " + "0" //special adjustment or not - approved or not
			+ ", '" + originalTimeStamp.toString() + "'" 
			+ ", '" + sRemoteHost +  "'"
			+ ", " + "0"
			+ ", '" + sGeoCode + "'"
			+ ", " + iGeocodingAllowedByUser
			+ ", " + iGeocodingSupportedByBrowser
			+ ")";

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sRecSQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060169]");
			throw new Exception("Error [1517600879] executing SQL '" + sRecSQL + "' - " + e.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060170]");
			throw new Exception("Error [1517600897] - couldn't commit data transaction.");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060171]");
		
		SimpleDateFormat formatter = new SimpleDateFormat ("'at ' hh:mm:ss a ' on ' MM-dd-yyyy");
		String sEarlyStartPhrase = "";
		if (sIsEarlyStart.compareTo("1") == 0){
			sEarlyStartPhrase = "AS AN <B>'EARLY START'</B> ";
		}

		s += "\n" + "<H2>You've punched in successfully " + sEarlyStartPhrase + formatter.format(datToday) + ".</H2>" + "\n";
		s += "<H2>Your <B><I>designated</I></B> start time is: " + sStartTime
			+ ", your effective punch in time is: " + formatter.format(new java.util.Date(sqlTimeStamp.getTime())) + ".</H2>" + "\n";
		
		//get the ID for the record you just created.
		sSQL = "SELECT * FROM " + TimeEntries.TableName 
			+ " WHERE (" 
				+ "(" + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')" 
				+ " AND (" + TimeEntries.dtInTime + " = \"" + sqlTimeStamp.toString() + "\")"
			+ ")"
		;
		try {
			ResultSet rsID = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID);
			if (rsID.next()){
				s += "Do you have special note to enter?";
				//yes or no. 
				s += "<TABLE BORDER=1>" + "\n"
					+ "  <TR>" + "\n";
				s += "    <TD><A HREF=\"" 
					//+ response.encodeURL( 
					+	ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteEdit?LinkID=" + rsID.getString("id")
					//)
					+ "\"><IMG src=\"" 
					+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext())
					//) 
					+ "yes.gif\"></A></TD>" + "\n";
				s += "    <TD><A HREF=\""
					+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
					+ MainLogin.CLASS_NAME
					+ "?db=" + sDBID
					+ "\"><IMG src=\"" 
					+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
					+ "no.gif\"></A></TD>" + "\n";
			}
			rsID.close();
		} catch (Exception e) {
			throw new Exception("Error [1517600898] - error getting last punch ID with SQL '" + sSQL + " - " + e.getMessage() + ".");
		}

		return s;
	}
	
	private String processPunchOut(
		String sEmployeeID,
		String sDBID,
		String sRemoteHost,
		String sGeoCode,
		int iGeocodingAllowedByUser,
		int iGeocodingSupportedByBrowser
		) throws Exception{
		
		String s = "";
		
		//First of all, check to see if there are any outstanding punch ins that don't have punch outs linked to them yet.

		//Create the 'Previous Unmatched Punchins' table here:
		s += "<TABLE style = \" font-family: Arial; border: 1px solid black; border-collapse: collapse; \" >" + "\n"
			+ "  <TR>" + "\n"
			+ "    <TD style = \" text-align:center; color:white; background-color:black; font-weight:bold; font-size:large; \" COLSPAN=2 >" 
				+ "PREVIOUS OUTSTANDING PUNCH INS" + "</TD>" + "\n"
			+ "  </TR>" + "\n"
		;
		
		int iNumberOfPreviousPunchIns = 0;
		int iMostRecentOutstandingPunchInID = 0;
		boolean bOddRow = false;
		SimpleDateFormat dfDate = new SimpleDateFormat("E, MMM dd yyyy");
		SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm:ss a");

		String sSQL = "SELECT" 
			+ " " + TimeEntries.id 
			+ ", " + TimeEntries.dtInTime 
			+ " FROM " + TimeEntries.TableName 
			+ " WHERE(" 
				+ "(" + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')" 
				+ " AND (" + TimeEntries.dtOutTime + " = '0000-00-00 00:00:00') "
			+ ")" 
			+ " ORDER BY " + TimeEntries.dtInTime
		;
			
		try {
			ResultSet rsOutstandingPunchIns = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			
			//First, let's get the COUNT of outstanding punch ins:
			while (rsOutstandingPunchIns.next()){
				iNumberOfPreviousPunchIns++;
			}
			
			//Go back to the beginning of the result set:
			rsOutstandingPunchIns.beforeFirst();
			int iRowCounter = 0;
			while(rsOutstandingPunchIns.next()){
				iRowCounter++;
				String sBackGroundColor = "";
				if (bOddRow){
					sBackGroundColor = ODD_ROW_BACKGROUND_COLOR;
				}else{
					sBackGroundColor = EVEN_ROW_BACKGROUND_COLOR;
				}
				//But if it's the LAST outstanding punch, highlight the row to show it's the one we're using to match with the punchout:
				if (iRowCounter == iNumberOfPreviousPunchIns){
					sBackGroundColor = HIGHLIGHT_ROW_BACKGROUND_COLOR;
				}
				s += "  <TR bgcolor = \"" + sBackGroundColor + "\" >" + "\n";
				
				s += "    <TD style = \" text-align:right; font-family: arial; font-weight: bold; \" >"  
					+ dfDate.format(rsOutstandingPunchIns.getDate("dtInTime")) 
						+ " " + dfTime.format(rsOutstandingPunchIns.getTime("dtInTime"))
					+ "    </TD>" + "\n";
				
				s += "    <TD style = \" text-align:left; font-family: arial; font-weight: bold; font-style:italic; color:red; \" >";
				
				if (iRowCounter == iNumberOfPreviousPunchIns){
					s += " <-- <I>This punch IN will be linked to your current punch OUT</I>";
				}else{
					s += "&nbsp;";
				}
				
				s += "</TD>" + "\n";
				
				iMostRecentOutstandingPunchInID = rsOutstandingPunchIns.getInt("id"); //This will contain the LAST of the unmatched punch ins, if there are any
				bOddRow = !bOddRow;
			}
			rsOutstandingPunchIns.close();
		} catch (Exception e1) {
			throw new Exception("Error [1517602094] - could not read outstanding punch ins with SQL '" + sSQL + "' - " + e1.getMessage());
		}
		
		//Close the table:
		//If there were no previous unmatched punch ins, then note that:
		if (iNumberOfPreviousPunchIns == 0){
			s += "  <TR>" + "\n"
				+ "    <TD style = \" font-size:small;   \" >" + "\n"
					+ "(You have NO previous outstanding punch-ins)"
				+ " </TD>" + "\n"
				+ "  </TR>" + "\n"
			;
		}
		
		s += "</TABLE>" + "\n";
		
		//If there are no previous unmatched punch ins at all, we display a note to let the user
		// know they should notify their manager, give them the option to add any special notes, 
		// and then after a preset time, we jump back to the main login page for the next person.
		
		//If there are any previous unmatched punch ins, we list those on the screen, 
		// match them up to the most recent unmatched punch in, then give them the option 
		// to record any special notes, and after a preset time, jump back to the main login screen.
		
		//If there are NO previous punch in's, advise the user:
		if (iNumberOfPreviousPunchIns == 0){
			s += "<BR>"
				+ "<H2>NOTE: You have no previous punch in times logged into the system."  
					+ "<BR>Please notify your manager to let them know.</H2>" + "\n"
				+ "<BR>" + "\n" 
				+ "<B><I>The system will punch you out automatically.</I></B><BR>" + "\n"
			;
		}
		
//		System.out.println("[1516986428] - s = '" + s + "'");
		String sPunchOutTime = "";
		try {
			sPunchOutTime = executePunchOut(sDBID, iMostRecentOutstandingPunchInID, sEmployeeID, sRemoteHost, sGeoCode, iGeocodingAllowedByUser, iGeocodingSupportedByBrowser, "KEEPTHENOTE");
		} catch (Exception e) {
			throw new Exception(" Error [1517093901] in punchout process - " + e.getMessage());
		}

		s += "<BR><H2>You've punched out successfully at " + sPunchOutTime + ".</H2>" + "\n"
			+ "<I>If you have additional information to record, please forward it to your manager as soon as possible.</I><BR>" + "\n"
		;
		
		s += "Do you have any special notes to enter?" + "\n";
		s += "<TABLE BORDER=1>" + "\n"
			+ "  <TR>" + "\n";
		s += "    <TD><A HREF=\"" 
			+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteEdit?LinkID=" + iMostRecentOutstandingPunchInID 
			+ "\"><IMG src=\"" 
			+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
			+ "yes.gif\"></A></TD>" + "\n";
		s += "    <TD><A HREF=\""
			+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
			+ MainLogin.CLASS_NAME
			+ "?db=" + sDBID
			+ "\"><IMG src=\"" 
			+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
			+ "no.gif\"></A></TD>" + "\n";
			
		s += "  <TR>" + "\n";
		s += "</TABLE>" + "\n";
		
		return s;
	}

	private String executePunchOut(
		String sDBID, 
		int iMatchingPunchInID, 
		String sEmployeeID, 
		String sRemoteHost,
		String sGeoCode,
		int iGeocodingAllowedByUser,
		int iGeocodingSupportedByBrowser,
		String sLogNote
		) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				"TCPunchAction.executePunchOut - EmployeeID '" + sEmployeeID + "'"
			);
		} catch (Exception e) {
			throw new Exception("Error [1516991091] - could not get data connection - " + e.getMessage());
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060162]");
			throw new Exception("Error [1516991092] - could not start data transaction.");
		}
		
		String sSQL = "UPDATE " + TimeEntries.TableName 
			+ " SET " + TimeEntries.dtOutTime + " = " + "LOCALTIME()"
			+ ", " + TimeEntries.iOutModified + " = 0" 
			+ ", " + TimeEntries.iEntryTypeID + " = 0" //Entry type 'regular' (not special type, like PB excused, etc.
		;
		if (sLogNote.compareTo("KEEPTHENOTE") != 0){
			sSQL += ", " + TimeEntries.mChangeLog + " = '" + clsDatabaseFunctions.FormatSQLStatement(sLogNote) + "'";
		}
		sSQL += " WHERE (" 
			+ "(" + TimeEntries.id + " = " + Integer.toString(iMatchingPunchInID) + ")"
		+ ")"
		;
		
		try {
			clsDatabaseFunctions.executeSQLWithException(
					sSQL, 
				sDBID, 
				"MySQL", 
				"TCPunchAction.executePunchOut - EmployeeID '" + sEmployeeID + "'", 
				getServletContext()
			);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060163]");
			throw new Exception("Error [1516991093] - could not execute punch in SQL '" + sSQL + "' - " + e.getMessage());
		}
		
		sSQL = "INSERT INTO " + TCTablerawpunchevents.TableName + "(" 
			+ TCTablerawpunchevents.sEmployeeID 
			+ ", " + TCTablerawpunchevents.iEntryType 
			+ ", " + TCTablerawpunchevents.iApproved 
			+ ", " + TCTablerawpunchevents.dtTime 
			+ ", " + TCTablerawpunchevents.sIPAddress
			+ ", " + TCTablerawpunchevents.iInOut 
			+ ", " + TCTablerawpunchevents.sgeocode
			+ ", " + TCTablerawpunchevents.igeocodingallowedbyuser
			+ ", " + TCTablerawpunchevents.igeocodingsupportedbybrowser
			+ ") VALUES (" 
			+ "'" + sEmployeeID + "'"
			+ ", 0" //Entry type 'regular' (not special type, like PB excused, etc.
			+ ", 0" //special adjustment or not 
			+ ", " + "LOCALTIME()"
			+ ", '" + sRemoteHost +  "'"
			+ ", 1"
			+ ", '" + sGeoCode +  "'"
			+ ", " + iGeocodingAllowedByUser
			+ ", " + iGeocodingSupportedByBrowser
			+ ")"
		;
		
		try {
			clsDatabaseFunctions.executeSQLWithException(
				sSQL, 
				sDBID, 
				"MySQL", 
				"TCPunchAction.executePunchOut - EmployeeID '" + sEmployeeID + "'", 
				getServletContext()
			);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060164]");
			throw new Exception("Error [1516991094] - could not execute punch in SQL '" + sSQL + "' - " + e.getMessage());
		}
		
		String sPunchOutTime = "";
		SimpleDateFormat formatterPunchOut = new SimpleDateFormat (" hh:mm:ss a ' on ' MMM dd, yyyy");
		sSQL = "SELECT LOCALTIME() AS 'PUNCHOUTTIME'";
		try {
			ResultSet rsTime = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rsTime.next()){
				sPunchOutTime = formatterPunchOut.format(rsTime.getTimestamp("PUNCHOUTTIME"));
			}
			rsTime.close();
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060165]");
			throw new Exception("Error [1516991095] - could not get LOCALTIME with SQL '" + sSQL + "' - " + e.getMessage());
		}
		
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060166]");
		
		return sPunchOutTime;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
