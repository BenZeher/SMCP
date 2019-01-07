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

public class RawEntryListGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

    	String title = "Raw Time Entry List";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
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
	    	Date SelectedStartingDay = Date.valueOf(
	    			request.getParameter("SelectedStartingYear") 
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
	    	Date SelectedEndingDay = Date.valueOf(
	    			request.getParameter("SelectedEndingYear") 
	    			+ "-" + sMonth 
	    			+ "-" + sDay
	    	);
	    	c.setTime(SelectedStartingDay);
	    	String sStartingDate = SQLDateformatter.format(c.getTime());
	    	c.setTime(SelectedEndingDay);
	    	String sEndingDate = SQLDateformatter.format(c.getTime());
	    	//System.out.println(ThisMonday);
	    	c.add(Calendar.DAY_OF_MONTH, 7);
	    	
	    	String sSQL;
	    	
	    	sSQL = TimeCardSQLs.Get_Raw_TimeEntry_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(),
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
	    				"</H2><HR><BR>");
		    
    		out.println("<TABLE BORDER=2 WIDTH=80%>");
    		out.println("<TR>" +
						"<TD ALIGN=CENTER><H4>Employee Name</H4></TD>" +
						"<TD ALIGN=CENTER><H4>Event Type</H4></TD>" +
						"<TD ALIGN=CENTER><H4>Event Time</H4></TD>" +
				        "<TD ALIGN=CENTER><H4>IP Address</H4></TD></TR>");
       		while (rs.next()){
        		out.println("<TR>");
        		//employee name
        		out.println("<TD ALIGN=CENTER>" + rs.getString("sEmployeeFirstName") + " " + rs.getString("sEmployeeLastName") + "</TD>");  
        		//event type, in/out
        		if (rs.getInt("iInOut") == 0){
        			out.println("<TD ALIGN=CENTER>IN</TD>");
        		}else{
        			out.println("<TD ALIGN=CENTER>OUT</TD>");
        		}
        		//event type
        		if (rs.getString("dtTime").compareTo("0000-00-00 00:00:00") == 0){
					out.println("<TD BGCOLOR=RED>Error retriving time record</TD>");
				}else{
					out.println("<TD ALIGN=CENTER BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">" + rs.getString("dtTime") + "</TD>");
				}
        		//IP Address
        		out.println("<TD ALIGN=CENTER>" + rs.getString("sIPAddress") + "</TD>");
        		
        		out.println("</TR>");
        	}    		
    		out.println("</TABLE>");
    		
    		rs.close();
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}
