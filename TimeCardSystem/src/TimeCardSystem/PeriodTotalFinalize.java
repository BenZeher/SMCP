package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TimeTotals;

/** Servlet that insert In-Time records into the the time entry table.*/

public class PeriodTotalFinalize extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
	    String title = "Time Card System";
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
    	String sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
    	String sRecordID = clsManageRequestParameters.get_Request_Parameter("RecID", request);
    	String sEmployee = clsManageRequestParameters.get_Request_Parameter("RecEmployee", request);
    	String sEndingPeriodDate = request.getParameter("RecPeriod");
    	
    	ArrayList<String> alDepartments = new ArrayList<String>(0);
    	Enumeration<?> paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			//System.out.println("[1365780081] paramNames.nextElement() = " + s);
			try {
				if (s.substring(0, 13).compareTo("RecDepartment") == 0){
					alDepartments.add(s.substring(13));
				}
			}catch (Exception ex){
				out.println ("<BR>Error Message: " + ex.getMessage() + "<BR>");
			}
		}
    	
    	Date datPeriodEndDate = null;
    	String sGetPeriodEndDateSQL = "";
	    try {
	    	//get the period date 
	    	sGetPeriodEndDateSQL = "SELECT DISTINCT " + TimeTotals.datPeriodEndDate + " FROM " + TimeTotals.TableName 
	    		+ " WHERE (" 
	    			+ "(" + TimeTotals.iFinalized + " = 0)"
	    		+ ")"
	    	;
    		ResultSet rsPeriodEndDate = clsDatabaseFunctions.openResultSet(sGetPeriodEndDateSQL, getServletContext(), sDBID);
    		if (rsPeriodEndDate.next()){
    			datPeriodEndDate = rsPeriodEndDate.getDate("datPeriodEndDate");
    		}
    		rsPeriodEndDate.close();
	    }catch(Exception e){
    		out.println("<BR><FONT COLOR=RED><B>Error [1517938443] getting period end date with SQL '" + sGetPeriodEndDateSQL + "' - " + e.getMessage() + ".</B></FONT>");
    		return;
	    }
	    
	    boolean bNoFinalizedResults = true;
	    
		if (datPeriodEndDate != null){
			bNoFinalizedResults = false;
	    	if (sRecordID.compareTo("0") == 0){
	    		//if there is no "id" indicated, finalize all selected unfinalized 
	    		//totals as well as temp-marked time entries.
	    		String sTimeTotalsSQL = TimeCardSQLs.Get_Period_Total_Time_SQL(sEndingPeriodDate,
					  0,
					  "",
					  sEmployee,
					  alDepartments);
	    		try {
					ResultSet rsTimeTotals = clsDatabaseFunctions.openResultSet(sTimeTotalsSQL, getServletContext(), sDBID);
					while (rsTimeTotals.next()){
						// sSQL = TimeCardSQLs.Get_Finalize_Time_Total_SQL(rsTimeTotals.getString("id"), "");
						String sUpdateTimeTotalsSQL = "UPDATE " + TimeTotals.TableName 
							+ " SET " + TimeTotals.iFinalized + " = 1"
							+ " WHERE (" 
								+ "(" + TimeTotals.id + " = " + rsTimeTotals.getString("id") + ")"
							+ ")"
						;

						try {
							if (clsDatabaseFunctions.executeSQL(sUpdateTimeTotalsSQL, getServletContext(), sDBID)){
								ArrayList<String> alSQLs = new ArrayList<String>(0);
								alSQLs.add(TimeCardSQLs.Get_Flag_Post_Time_Entry(formatter.format(rsTimeTotals.getDate("datPeriodEndDate")), 
																			 	 rsTimeTotals.getString("sEmployeeID")));
								alSQLs.add(TimeCardSQLs.Get_Flag_Post_Leave_Adjustment(formatter.format(rsTimeTotals.getDate("datPeriodEndDate")), 
									 	 										 	   rsTimeTotals.getString("sEmployeeID")));
								try {
									clsDatabaseFunctions.executeSQLsInTransaction(alSQLs, getServletContext(), sDBID);
								} catch (Exception e) {
									out.println("<BR><FONT COLOR=RED><B>Error [1517938446] executing statement array - " + e.getMessage() + ".</B></FONT>");
									return;
								}
							}
						} catch (SQLException e) {
							rsTimeTotals.close();
							out.println("<BR><FONT COLOR=RED><B>Error [1517938444] executing insert statements '" + sUpdateTimeTotalsSQL + "' - " + e.getMessage() + ".</B></FONT>");
							return;
						}	
					}
					rsTimeTotals.close();
				} catch (SQLException e) {
					out.println("<BR><FONT COLOR=RED><B>Error [1517938445] reading time totals with SQL: '" + sTimeTotalsSQL + "' - " + e.getMessage() + ".</B></FONT>");
					return;
				}
	    		
	    	}else{
	    		//a specific id has been passed in, then finalize the particular time total record only
	    		//need to find out who this record belongs to
	    		//sSQL = TimeCardSQLs.Get_Period_Total_Time_SQL(Integer.parseInt(sRecordID));
	    		String sGetEmployeeFromIDSQL = "SELECT * FROM " + TimeTotals.TableName 
	    			+ " WHERE (" 
	    				+ "(" + TimeTotals.id + " = " + Integer.parseInt(sRecordID) + ")"
	    			+ ")"	
	    		;
	    		try {
					ResultSet rsTotal = clsDatabaseFunctions.openResultSet(sGetEmployeeFromIDSQL, getServletContext(), sDBID);
					if (rsTotal.next()){
						sEmployee = rsTotal.getString("sEmployeeID");
					}else{
						sEmployee = "";
					}
					rsTotal.close();
				} catch (SQLException e) {
					out.println("<BR><FONT COLOR=RED><B>Error [1517938447] reading employee from record ID with SQL: '" + sGetEmployeeFromIDSQL + "' - " + e.getMessage() + ".</B></FONT>");
					return;
				}
	    		
	    		//sSQL = TimeCardSQLs.Get_Finalize_Time_Total_SQL(sRecordID), "");
	    		String sUpdateTimeTotalsSQL = "UPDATE " + TimeTotals.TableName 
		    		+ " SET " + TimeTotals.iFinalized + " = 1"
		    		+ " WHERE (" 
		    			+ "(" + TimeTotals.id + " = " + sRecordID + ")"
		    		+ ")"
		    	;
	    		try {
					if (clsDatabaseFunctions.executeSQL(sUpdateTimeTotalsSQL, getServletContext(), sDBID)){
						ArrayList<String> alSQLs = new ArrayList<String>(0);
						alSQLs.add(TimeCardSQLs.Get_Flag_Post_Time_Entry(datPeriodEndDate.toString(), sEmployee));
						alSQLs.add(TimeCardSQLs.Get_Flag_Post_Leave_Adjustment(datPeriodEndDate.toString(), sEmployee));
						try {
							clsDatabaseFunctions.executeSQLsInTransaction(alSQLs, getServletContext(), sDBID);
						} catch (SQLException e) {
							out.println("<BR><FONT COLOR=RED><B>Error [1517938448] executing statement array - " + e.getMessage() + ".</B></FONT>");
							return;
						}
					}
				} catch (SQLException e) {
					out.println("<BR><FONT COLOR=RED><B>Error [1517938449] executing update statement SQL '" + sUpdateTimeTotalsSQL + " - " + e.getMessage() + ".</B></FONT>");
					return;
				}
	    	}
    		out.println ("<HTML>\n" + 
            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
            	 "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
            	 "<H1>" + title + "</H1>\n" + 
            	 "<META http-equiv='Refresh' content='0;URL=" +  TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");

		}else{
			//There are no unfinalized time totals
    		out.println ("<HTML>\n" + 
            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
            	 "<BODY BGCOLOR=\"#" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
            	 "<H1>" + title + "</H1>\n" + 
            	 "<BR>There is no unfinalized total at this point.<BR>" + 
            	 "<META http-equiv='Refresh' content='3;URL=" +  TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
		}

		String sDepartments = "";
		for (int i = 0; i < alDepartments.size(); i++){
			if (i == 0){
				sDepartments += alDepartments.get(i);
			}else{
				sDepartments += ", " + alDepartments.get(i);				
			}
		}
		
		String sUnfinalizedResults = "There were NO unfinalized time totals";
		if (bNoFinalizedResults == false){
			sUnfinalizedResults = "There WERE unfinalized time totals";
		}
		
		TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			TCLogEntry.LOG_OPERATION_FINALIZED_TIME_ENTRIES, 
			"Passed in record ID: '" + sRecordID + ", "
				+ "passed in Employee: '" + sEmployee + "', "
				+ "passed in period ending date: '" + sEndingPeriodDate + "', "
				+ "passed in departments: '" + sDepartments + "'."
			, 
			sUnfinalizedResults, 
			"[1517939647]"
		);

	    out.println("</BODY></HTML>");
	}
}