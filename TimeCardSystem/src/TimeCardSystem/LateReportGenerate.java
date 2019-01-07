package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Servlet that insert In-Time records into the the time entry table.*/

public class LateReportGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

    	String title = "Late Report";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
	    
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A>"
		  	+ "<BR><BR>");

	    try {
	    	
	    	//Calculate time period
	    	SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
	    	Calendar c = Calendar.getInstance();
	    	//Date SelectedMonday = (Date)DateFormatter.parse(request.getParameter("Monday"));
	    	
	    	String sMonth = request.getParameter("SelectedStartingMonth");
	    	if (sMonth.length() == 1){
	    		sMonth = "0" + sMonth;
	    	}
	    	String sDay = request.getParameter("SelectedStartingDay");
	    	if (sDay.length() == 1){
	    		sDay = "0" + sDay;
	    	}
	    	Date SelectedStartingDay = Date.valueOf(request.getParameter("SelectedStartingYear") 
	    			+ "-" + sMonth 
	    			+ "-" + sDay
	    	);
	    	
	    	sMonth = request.getParameter("SelectedEndingMonth");
	    	if (sMonth.length() == 1){
	    		sMonth = "0" + sMonth;
	    	}
	    	sDay = request.getParameter("SelectedEndingDay");
	    	if (sDay.length() == 1){
	    		sDay = "0" + sDay;
	    	}
	    	Date SelectedEndingDay = Date.valueOf(request.getParameter("SelectedEndingYear") 
	    			+ "-" + sMonth 
	    			+ "-" + sDay
	    	);
	    	c.setTime(SelectedStartingDay);
	    	String sStartingDate = SQLDateformatter.format(c.getTime());
	    	c.setTime(SelectedEndingDay);
	    	c.add(Calendar.DAY_OF_MONTH, 1);
	    	String sEndingDate = SQLDateformatter.format(c.getTime());
	    	
	    	//get Late Info
	    	String sSQL;
	    	sSQL = TimeCardSQLs.Get_Late_Entries_SQL((String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID),
														 sStartingDate, 
														 sEndingDate,
													 	 request.getParameter("SelectedEmployee"), 
														 request.getParameter("SelectedDepartment"));
	    	
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	out.println("<H2>Time Period: " + 
	    				request.getParameter("SelectedStartingMonth") + "/" +
	    				request.getParameter("SelectedStartingDay") + "/" +
	    				request.getParameter("SelectedStartingYear") +
	    				" - " + 
	    				request.getParameter("SelectedEndingMonth") + "/" +
	    				request.getParameter("SelectedEndingDay") + "/" +
	    				request.getParameter("SelectedEndingYear") + 
	    				"</H2><HR>");
		    
	    	if (request.getParameter("SelectedDepartment").compareTo("") != 0){
	    		sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("SelectedDepartment")));
	    		ResultSet rsDeptInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rsDeptInfo.next()){
	    			out.println("<B>Department: " + rsDeptInfo.getString("sDeptDesc") + "</B><BR>");
	    		}
	    		rsDeptInfo.close();
	    	}else{
	    		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
	    			out.println("<B>Department: ALL </B><BR>");
	    		}
	    	}
	    	if (request.getParameter("SelectedEmployee").compareTo("") != 0){
	    		out.println("<B>Employee: " + request.getParameter("SelectedEmployee") + "</B><BR>");	    		
	    	}else{
	    		out.println("<B>Employee: ALL </B><BR>");
	    	}
    		

    		out.println("<TABLE BORDER=2 WIDTH=90%>");
    		
    		out.println("<TR>");
    	
	    	if (request.getParameter("SelectedDepartment").compareTo("") == 0 && request.getParameter("SelectedEmployee").compareTo("") == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Department</H4></TD>");
	    	}
    		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
    			out.println("<TD ALIGN=CENTER><H4>Employee</H4></TD>");
    		}
			out.println("<TD ALIGN=CENTER><H4>Start Time</H4></TD>");
			out.println("<TD ALIGN=CENTER><H4>In Time</H4></TD>");
			out.println("<TD ALIGN=CENTER><H4>Out Time</H4></TD>");
			out.println("<TD ALIGN=CENTER><H4>Late Minute*</H4></TD>");
			
			while (rs.next()){
				/*
				//if the time length is less than the grace period, don't display this entry.
				//get the late grace period length for this employee 
	    		sSQL = TimeCardSQLs.Get_Employee_Late_Grace_Period_SQL(rs.getString("Employees.sEmployeeID"));
	    		ResultSet rsGP = TimeCardUtilities.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		double dGP;
	    		if (rsGP.next()){
	    			dGP = rsGP.getDouble("dLateGracePeriod");
	    		}else{
	    			dGP = 0;
	    		}
	    		rsGP.close();
				double dLateMin = TimeCardUtilities.RoundHalfUp((Timestamp.valueOf(rs.getString("dtinTime")).getTime() - Timestamp.valueOf(rs.getString("dtInTime").subSequence(0, 10) + " " + rs.getString("tStartTime")).getTime() - dGP * 60000) / 1000 /60.0, 0);
				*/
        		out.println("<TR>");
        		if (request.getParameter("SelectedDepartment").compareTo("") == 0 && request.getParameter("SelectedEmployee").compareTo("") == 0){
        			//employee department
        			out.println("<TD ALIGN=CENTER>" + rs.getString("Departments.sDeptDesc") + "</TD>");
        		}
        		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
	        		//employee name
	        		out.println("<TD ALIGN=CENTER>" + rs.getString("sEmployeeFirstName") + " " + rs.getString("sEmployeeLastName") + "</TD>");
        		}
        		//employee start time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("tStartTime") + "</TD>");  
        		//in time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("dtInTime") + "</TD>");  
        		//out time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("dtOutTime") + "</TD>");
        		//late min
        		out.println("<TD ALIGN=CENTER>" + rs.getInt("iLateMinute") + "</TD></TR>");
        	}    		
    		out.println("</TABLE>");
    		rs.close();
    		
	    	out.print("*Late minutes are actual late minutes (from start time to punch-in time) for the employee.");
	    	out.println("&nbsp;Excused-lates will not be listed in this report.<BR>");
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}
