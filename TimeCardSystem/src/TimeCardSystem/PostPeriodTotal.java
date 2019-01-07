package TimeCardSystem;

/*
 * This servlet posts all the selected(final) time.
 * It will:
 * 	1.	calculate the time and sum them together, got "Total", "Double Time", "Over Time", "Leave Time" per department.
 *  2.	Insert them into Totals table.
 *  3.	Lable all totals with employee and period end date.
 *  4.	Mark all precoessed time entries with a period time label for future reference.
 *  5.	Time totals in the total table can reviewed and edited at any given time later.
 *   
 * Parameters passed in:
 * 	1.	Period end time
 * 	2.	Department ID
 */
import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.LeaveAdjustments;
import TCSDataDefinition.TimeEntries;
import TCSDataDefinition.TimeTotals;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PostPeriodTotal extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;

	@SuppressWarnings("deprecation")
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		String title = "Time Card System";
		String subtitle = "Posting period time totals...";

		out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

		HttpSession CurrentSession = request.getSession();

		String sSQL = "";
		String sStartingDate = "1970-01-01";		
		String sPeriodEndDate = "";
		String sTruePeriodEndDate = "";
		double dWeeklySum = 0;
		double dRegular = 0;
		double dOverTime = 0;
		double dDoubleTime = 0;
		double dLeaveTime = 0;
		ResultSet rsAvailableTime = null;
		ResultSet rsLeaveTime = null;
		try {

			SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			//get ending date
			String sMonth = request.getParameter("SelectedEndMonth");
			if (sMonth.length() == 1){
				sMonth = "0" + sMonth;
			}
			String sDay = request.getParameter("SelectedEndDay");
			if (sDay.length() == 1){
				sDay = "0" + sDay;
			}
			
			Date datPeriodEndDate = Date.valueOf(
					request.getParameter("SelectedEndYear") + "-" 
					+ sMonth + "-" 
					+ sDay
					);
			c.setTime(datPeriodEndDate);
			sTruePeriodEndDate = SQLDateformatter.format(c.getTime());
			//c.add(Calendar.DAY_OF_MONTH, 1);
			c.set(c.get(Calendar.YEAR), 
					c.get(Calendar.MONTH), 
					c.get(Calendar.DAY_OF_MONTH), 
					23, 
					59, 
					59);
			sPeriodEndDate = SQLDateformatter.format(c.getTime());
			out.println("<BR><H3>Posting time entries for period ending " + sTruePeriodEndDate + ":</H3><BR>");
		} catch (Exception ex) {
			out.println("<BR>Error formatting dates - " + ex.getMessage() + "<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		//Find all the employees that need to be processed.
		String sEmployeesSQL = TimeCardSQLs.Get_Employee_List_By_Department_SQL(
			request.getParameter("SelectedDepartment"), false); //don't include inactive employees
		if (bDebugMode){
			System.out.println("[1417727826] In " + this.toString() + ".GetEmployee_ListBy_Department_SQL = " + sSQL);
		}
		try{
			ResultSet rsEmployees = clsDatabaseFunctions.openResultSet(
				sEmployeesSQL, 
				getServletContext(), 
				(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

			while (rsEmployees.next()){

				//reset all the cumulative values:
				dWeeklySum = 0;
				dRegular = 0;
				dOverTime = 0;
				dDoubleTime = 0;
				dLeaveTime = 0;
				
				String sEmployeeID = rsEmployees.getString("sEmployeeID");
				
				//remove all old post records for this employee
				ArrayList<String> alSQLs = new ArrayList<String>(0);
				
				String sRemoveUnfinalizedTimeTotalsSQL = "DELETE FROM " + TimeTotals.TableName
					+ " WHERE (" 
						+ "(" + TimeTotals.iFinalized + " = 0)"
				;
				if (sEmployeeID.compareTo("") != 0){
					sRemoveUnfinalizedTimeTotalsSQL = sRemoveUnfinalizedTimeTotalsSQL + " AND (" + TimeTotals.sEmployeeID + " = '" + sEmployeeID + "')"; 
				}
				sRemoveUnfinalizedTimeTotalsSQL += ")";
				
				alSQLs.add(sRemoveUnfinalizedTimeTotalsSQL);
				
				String sRemoveTimeEntriesMarkedAsTemporary = "UPDATE " + TimeEntries.TableName 
					+ " SET " + TimeEntries.sPeriodDate + " = '0000-00-00'" 
					+ " WHERE (" 
						+ "(" + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')";
				if (sEmployeeID.compareTo("") != 0){
					sRemoveTimeEntriesMarkedAsTemporary = sRemoveTimeEntriesMarkedAsTemporary + " AND (" + TimeEntries.sEmployeeID + " = '" + sEmployeeID + "')";
				}
				sRemoveTimeEntriesMarkedAsTemporary += ")";	
				
				alSQLs.add(sRemoveTimeEntriesMarkedAsTemporary);
				
				String sRemoveLeaveEntriesMarkedAsTemporary = "UPDATE " + LeaveAdjustments.TableName 
					+ " SET " + LeaveAdjustments.sPeriodDate + " = '0000-00-00'" 
						+ " WHERE (" 
							+ "(" + LeaveAdjustments.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')";
				if (sEmployeeID.compareTo("") != 0){
					sRemoveLeaveEntriesMarkedAsTemporary = sRemoveLeaveEntriesMarkedAsTemporary + " AND (" + LeaveAdjustments.sEmployeeID + " = '" + sEmployeeID + "')";
				};				
				sRemoveLeaveEntriesMarkedAsTemporary += ")";	
					
				alSQLs.add(sRemoveLeaveEntriesMarkedAsTemporary);

				try{
					clsDatabaseFunctions.executeSQLsInTransaction(
						alSQLs, 
						getServletContext(), 
						(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
				}catch (SQLException e4){
					out.println("<BR>Error executing SQLs in transaction - "
							+ "1: " + sRemoveUnfinalizedTimeTotalsSQL
							+ ", 2: " + sRemoveTimeEntriesMarkedAsTemporary
							+ ", 3: " + sRemoveLeaveEntriesMarkedAsTemporary
							+ e4.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}

				//for each employee, process all entries before the period end date without any missing INs or OUTs.
				/**
				 This is the SQL statement that results from the next call:
				 SELECT * FROM TimeEntries WHERE sEmployeeID = 'CHE001'
				 AND (
				(dtInTime >= '1970-01-01' AND dtInTime <= '2014-12-02 23:59:59')
				 OR (dtOutTime  >= '1970-01-01' AND dtOutTime <= '2014-12-02 23:59:59')
				)
				 AND dtInTime <> '0000-00-00'
				 AND dtOutTime <> '0000-00-00'
				 AND (sPeriodDate = '0000-00-00' OR sPeriodDate = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')
				 ORDER BY dtInTime
				 */
				sSQL = TimeCardSQLs.Get_Employee_TimeEntry_List_SQL(rsEmployees.getString("sEmployeeID"), 
						sStartingDate, 
						sPeriodEndDate + " 23:59:59",
						true, // don't include any entry with null in or out.
						false); //don't include any finalized entry
				//out.println("<BR><BR>Find available time entries SQL: " + sSQL);
				if (bDebugMode){
					System.out.println("[1417727827] rsAvailableTime SQL = " + sSQL);
				}
				try{
					rsAvailableTime = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					while (rsAvailableTime.next()){

						if (rsAvailableTime.getString("dtOutTime").compareTo("0000-00-00 00:00:00") != 0 && 
								rsAvailableTime.getString("dtInTime").compareTo("0000-00-00 00:00:00") != 0){
							//we don't deal with records with no punch out. all *missing* punch out should be filled by now.

							boolean TimeCalculatable = true;
							//set null time to be "NA" so they will default to current time and date in the next page
							if (rsAvailableTime.getString("dtInTime").compareTo("0000-00-00 00:00:00") == 0){
								TimeCalculatable = false;
							}
							if (TimeCalculatable){
								//calculate the time
								double dDailySum = 0;
								Timestamp inTime = Timestamp.valueOf(rsAvailableTime.getString("dtInTime"));
								Timestamp outTime = Timestamp.valueOf(rsAvailableTime.getString("dtOutTime"));
								dDailySum = outTime.getTime() - inTime.getTime();

								if (dDailySum > 18000000 && inTime.getDay() != 0 && outTime.getDay() != 0){
									dDailySum = dDailySum - 1800000;
								}

								//round the hour figure half-up
								dDailySum = TimeCardUtilities.RoundHalfUp(dDailySum / 1000 / 3600.0, 2);
								dWeeklySum = dWeeklySum + dDailySum;
								//calculate the double time
								dDoubleTime = dDoubleTime + TimeCardUtilities.CalculateDoubleTime(rsAvailableTime.getString("dtInTime"), rsAvailableTime.getString("dtOutTime"), rsEmployees.getString("tStartTime")) /60.0;
							}
						}
					}
				}catch (Exception e){
					out.println("<BR>Error reading available time with SQL - "
							+ sSQL + " - " + e.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}

				try{
					//for each employee, process all leave times.
					sSQL = TimeCardSQLs.Get_Leave_Adjustments_SQL(rsEmployees.getString("sEmployeeID"),
							-1, //select every types, deal with NP types later.
							sStartingDate, 
							sPeriodEndDate,
							"id", // sort by id.
							0,
							false); //ascending order

					rsLeaveTime = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					while (rsLeaveTime.next()){

						if (rsLeaveTime.getInt("iPaidLeave") == 1 && rsLeaveTime.getInt("iSpecialAdjustment") != 1){
							//we don't sum non-paid leave here.
							//System.out.println("Leave time added: " + rsLeaveTime.getInt("id") + "-" + rsLeaveTime.getDouble("dDuration"));
							dLeaveTime = dLeaveTime + rsLeaveTime.getDouble("dDuration");
							//System.out.println("7.accumulated dLeaveTime = " + dLeaveTime);
						}
					}
				}catch(SQLException e1){
					out.println("<BR>Error reading leave time with SQL - "
							+ sSQL + " - " + e1.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}
				dWeeklySum = dWeeklySum + dLeaveTime;
				if (dWeeklySum - dDoubleTime > 40){
					dRegular = dWeeklySum - dDoubleTime;
					dOverTime = dRegular - 40;
					dRegular = dRegular - dOverTime - dLeaveTime;
				}else{
					dRegular = dWeeklySum - dDoubleTime - dLeaveTime;

				}

				/* TimeTotals;
				+------------------+--------------+------+-----+---------------------+----------------+
				| Field            | Type         | Null | Key | Default             | Extra          |
				+------------------+--------------+------+-----+---------------------+----------------+
				| id               | mediumint(9) |      | PRI | NULL                | auto_increment |
				| sEmployeeID      | varchar(9)   | YES  |     |                     |                |
				| datPeriodEndDate | datetime     | YES  |     | 0000-00-00 00:00:00 |                |
				| dPeriodTotal     | double       | YES  |     | 0                   |                |
				| dPeriodRegular   | double       | YES  |     | 0                   |                |
				| dPeriodOverTime  | double       | YES  |     | 0                   |                |
				| dPeriodDouble    | double       | YES  |     | 0                   |                |
				| dPeriodLeave     | double       | YES  |     | 0                   |                |
				| sCreatorID       | varchar(9)   | YES  |     |                     |                |
				| iFinalized       | int(1)       | YES  |     | 0                   |                |
				+------------------+--------------+------+-----+---------------------+----------------+
				 */

				//save this result into total table.
				sSQL = TimeCardSQLs.Get_Insert_Time_Total_SQL(rsEmployees.getString("sEmployeeID"), 
						sTruePeriodEndDate,  
						TimeCardUtilities.RoundHalfUp(dWeeklySum, 2),  
						TimeCardUtilities.RoundHalfUp(dRegular, 2), 
						TimeCardUtilities.RoundHalfUp(dOverTime, 2), 
						TimeCardUtilities.RoundHalfUp(dDoubleTime, 2),  
						TimeCardUtilities.RoundHalfUp(dLeaveTime, 2),
						(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID));
				if (bDebugMode){
					System.out.println("[1417727828] - SQL = " + sSQL);
				}
				out.println("<BR><B>" + rsEmployees.getString("sEmployeeID") + " - " + rsEmployees.getString("sEmployeeFirstName") + " " + rsEmployees.getString("sEmployeeLastName") + "</B>");
				out.println("<BR>&nbsp;&nbsp;&nbsp;&nbsp;Weekly Total: " + TimeCardUtilities.RoundHalfUp(dWeeklySum, 2));
				out.println("&nbsp;&nbsp;Regular Hour: " + TimeCardUtilities.RoundHalfUp(dRegular, 2));
				out.println("&nbsp;&nbsp;Over Time: " + TimeCardUtilities.RoundHalfUp(dOverTime, 2));
				out.println("&nbsp;&nbsp;Double Time: " + TimeCardUtilities.RoundHalfUp(dDoubleTime, 2));
				out.println("&nbsp;&nbsp;Leave Time: " + TimeCardUtilities.RoundHalfUp(dLeaveTime, 2) + "<BR>");

				try{
					clsDatabaseFunctions.executeSQL(
						sSQL, 
						getServletContext(), 
						(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
				}catch(SQLException e5){
					out.println("<BR>Error executing time insert SQL: "
							+ sSQL + " - " + e5.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}
				//if totals get saved successfully, flag all the time records as "posted".
				try{
					rsAvailableTime.beforeFirst();
					while (rsAvailableTime.next()){
						int iTimeEntryID = rsAvailableTime.getInt("id");
						if (iTimeEntryID == 0){
							throw new Exception("Error [1417742475] Time entry ID is zero.");
						}
						sSQL = TimeCardSQLs.flagTimeEntryAsPosted(iTimeEntryID);
						if (bDebugMode){
							System.out.println("[1417716103] Update time entries flag SQL: " + sSQL);
						}
						clsDatabaseFunctions.executeSQL(
							sSQL, 
							getServletContext(), 
							(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					}
					rsAvailableTime.close();
				}catch(Exception e2){
					out.println("<BR>Error executing get flag post time SQL: "
							+ sSQL + " - " + e2.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}
				//flag all the leave records as "posted"
				rsLeaveTime.beforeFirst();
				try{
					while (rsLeaveTime.next()){
						sSQL = TimeCardSQLs.Get_Flag_Post_Leave_Adjustment(
							TimeCardUtilities.TEMPORARY_POSTING_DATE, 
							rsLeaveTime.getInt("id"));
						//out.println("<BR><BR>Update time entries flag SQL: " + sSQL);
						clsDatabaseFunctions.executeSQL(
							sSQL, 
							getServletContext(), 
							(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					}
				}catch(Exception e3){
					out.println("<BR>Error executing get flag post leave adjustment SQL: "
							+ sSQL + " - " + e3.getMessage() + "<BR>");
					out.println("</BODY></HTML>");
					return;
				}
				rsLeaveTime.close();
			}
			rsEmployees.close();
		}catch(SQLException e){
			out.println("<BR>Error in main loop reading employees with SQL: "
					+ sEmployeesSQL + " - " + e.getMessage() + "<BR>");
			out.println("</BODY></HTML>");
			return;
		}

		//Record the posting:
		TCLogEntry log = new TCLogEntry(
			(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB),
			getServletContext()
		);
		log.writeEntry(
			(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID), 
			TCLogEntry.LOG_OPERATION_POSTED_TIME_ENTRIES, 
			"Dept: '" + request.getParameter("SelectedDepartment") + "',"
				+ "Starting date: '" + sStartingDate + ", '"
				+ "Period end date: '" + sPeriodEndDate + ", '"
			, 
			"", 
			"[1517937216]"
		);
		
		//back to main menu after finished.
		out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
			+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
		out.println("</BODY></HTML>");
	}
}
