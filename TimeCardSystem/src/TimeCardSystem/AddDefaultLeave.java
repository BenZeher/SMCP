package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class AddDefaultLeave extends HttpServlet{

	static final long serialVersionUID = 0;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
	    String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		
	    String title = "Time Card System";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    //System.out.println ("OriginalURL = " + request.getParameter("OriginalURL"));
	    out.println("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_EID) + "'>");
	    
	    try {
	    	
	    	String sEmpID = request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_EID);
	    	Date datLeaveDate = Date.valueOf(request.getParameter("FocusDate"));
	    	
	    	String sSQL = TimeCardSQLs.Get_Insert_Vacation_Day_SQL(sEmpID, datLeaveDate);
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    	
	    	TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
	    	log.writeEntry(
	    		sUserID, 
	    		TCLogEntry.LOG_OPERATION_ADMIN_ADDED_VACATION_LEAVE_ADJUSTMENT, 
	    		"For employee '" + sEmpID + "' leave date: '" + request.getParameter("FocusDate") + "'.", 
	    		sSQL, 
	    		"[1518039520]"
	    	);
	    	
    		out.println ("<H2>Time entry record added.</H2");
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	System.out.println ("Err" + ex.getMessage());
	    	out.println(TimeCardUtilities.Session_Expire_Handling(getServletContext()));
	    }
	    out.println("</BODY></HTML>");
	}
}