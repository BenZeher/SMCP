package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.SpecialEntryTypes;
import TCSDataDefinition.TimeEntries;

/** Servlet that insert In-Time records into the the time entry table.*/

public class AddDefaultPunch extends HttpServlet{
	public static final String ENTRY_TYPE_TITLE = "ENTRYTYPETITLE";
	public static final String ENTRY_TYPE_TITLE_HOLIDAY = "HOL";
	static final long serialVersionUID = 0;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession(true);
		String sUser = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
	    String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String title = "Time Card System";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    //System.out.println ("OriginalURL = " + request.getParameter("OriginalURL"));
	    out.println("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_EID) + "'>");
	    
	    try {
	    	String sEmpID = request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_EID);
	    	Timestamp tsInTime = Timestamp.valueOf(request.getParameter("FocusDate") + " " + request.getParameter("StartTime"));
	    	String sEntryTypeTitle = request.getParameter(ENTRY_TYPE_TITLE);
	    	if (sEntryTypeTitle == null){
	    		sEntryTypeTitle = "";
	    	}
	    	String sEntryTypeID = "0"; //Defaults to regular time
	    	if (sEntryTypeTitle.compareToIgnoreCase(ENTRY_TYPE_TITLE_HOLIDAY) == 0){
	    		//Get the special entry type ID for 'Holiday':
	    		String SQL = "SELECT * FROM " + SpecialEntryTypes.TableName
	    			+ " WHERE ("
	    				+ "(" + SpecialEntryTypes.sTypeTitle + " = '" + ENTRY_TYPE_TITLE_HOLIDAY + "')"
	    			+ ")"
	    		;
	    		try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB), 
						"MySQL", 
						this.toString() + ".reading special entry types."
					);
					if (rs.next()){
						sEntryTypeID = Integer.toString(rs.getInt(SpecialEntryTypes.iTypeID));
						rs.close();
					}else{
						rs.close();
						out.println("<BR> Error [1432236971] getting special entry type '" + ENTRY_TYPE_TITLE_HOLIDAY + "' with SQL: " + SQL + ".");
						return;
					}
				} catch (Exception e) {
					out.println("<BR> Error [1432236972] reading special entry types with SQL: " + SQL + " - " + e.getMessage());
					return;
				}
	    	}
	    	String sInTime = tsInTime.toString();
	    	String sOutTime;
	    	if (Integer.parseInt(request.getParameter("FullDay")) == 1){
	    		tsInTime.setTime(tsInTime.getTime() + 8 * 3825 * 1000); // 3600 * (8.5 / 8) = 3825
	    	}
	    	sOutTime = tsInTime.toString();
	    	
	    	//String sSQL = TimeCardSQLs.Get_Insert_Full_Time_Entry_SQL(sEmpID, sInTime, sInTime, sOutTime);
			String sSQL = "INSERT INTO " + TimeEntries.TableName + "(" 
				+ " " + TimeEntries.sEmployeeID 
				+ ", " + TimeEntries.dtInTime 
				+ ", " + TimeEntries.dtInTimeOri 
				+ ", " + TimeEntries.dtOutTime 
				+ ", " + TimeEntries.iInModified
				+ ", " + TimeEntries.iOutModified 
				+ ", " + TimeEntries.iEarlyStart 
				+ ", " + TimeEntries.sPeriodDate 
				+ ", " + TimeEntries.mChangeLog
				+ ", " + TimeEntries.iEntryTypeID
				+ ") VALUES (" 
				+ "'" + sEmpID + "'," 
				+ "\"" + sInTime +  "\"," 
				+ "\"" + sInTime +  "\"," 
				+ "\"" + sOutTime +  "\","
				+ " 1," 
				+ " 1," 
				+ " 0," 
				+ " '0000-00-00'," 
				+ "'Recovered from a missing day.'"
				+ ", " + sEntryTypeID
				+ ")";
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	//Record the added Time Entry record:
	    	TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
	    	log.writeEntry(
	    		sUser, 
	    		TCLogEntry.LOG_OPERATION_ADMIN_ADDED_DEFAULTPUNCH_TIME_ENTRY, 
	    		"Entered Time Entry for '" + sEmpID + "', "
	    			+ "in time: '" + sInTime + "', "
	    			+ "out time: '" + sOutTime + "'"
	    		, 
	    		sSQL, 
	    		"[1518030142]"
	    	);
	    	
    		out.println ("<H2>Time entry record added.</H2");
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	System.out.println ("Err [1432236806] " + ex.getMessage());
	    	out.println(TimeCardUtilities.Session_Expire_Handling(getServletContext()));
	    }
	    out.println("</BODY></HTML>");
	}
}