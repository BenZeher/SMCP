package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class OnClockEmployeeList extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

    	String title = "On-Clock Employee List";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
	    
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {
	    		    	
	    	String sSQL;
	    	sSQL = TimeCardSQLs.Get_On_Clock_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(),
						  									   request.getParameter("SelectedDepartment"));
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

	    	if (request.getParameter("SelectedDepartment").compareTo("0") != 0){
	    		sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("SelectedDepartment")));
	    		ResultSet rsDeptInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rsDeptInfo.next()){
	    			out.println("<B>Department: " + rsDeptInfo.getString("sDeptDesc") + "</B><BR>");
	    		}
	    		rsDeptInfo.close();
	    	}else{
	    		out.println("<B>Department: ALL </B><BR>");
	    	}

    		out.println("<TABLE BORDER=2 WIDTH=90%>");
    		
    		out.println("<TR>");
    	
	    	if (request.getParameter("SelectedDepartment").compareTo("0") == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Department</H4></TD>");
	    	}
	    	out.println("<TD ALIGN=CENTER><H4>Employee Name</H4></TD>");
			out.println("<TD ALIGN=CENTER><H4>In Time</H4></TD>");
			
			while (rs.next()){
				out.println("<TR>");
        		if (request.getParameter("SelectedDepartment").compareTo("0") == 0){
        			//employee department
        			out.println("<TD ALIGN=CENTER>" + rs.getString("Departments.sDeptDesc") + "</TD>");
        		}
        		//employee name
        		out.println("<TD ALIGN=CENTER>" + rs.getString("Employees.sEmployeeLastName") + ", " +
        										  rs.getString("Employees.sEmployeeFirstName") + " " + 
        										  rs.getString("Employees.sEmployeeMiddleName") + "</TD>");
        		//in time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("dtInTime") + "</TD></TR>");
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
