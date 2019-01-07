package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;



/** Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeLeaveRemove extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUserID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
	    String sEmployeeID = request.getParameter("EmployeeID");
	    
	    String title = "Time Card System";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {
	    	String sSQL;
	    	
	    	//out.println ("StartTimestamp: " + sqlStartTimeStamp.toString());
	    	//out.println ("EndTimestamp: " + sqlEndTimeStamp.toString());
	    	if (request.getParameter("DoubleCheck") == null){
	    		//don't delete, just go back.
	    		out.println("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    	}else{
	    		//delete the selected entry
	    		sSQL = TimeCardSQLs.Get_Remove_Leave_Adjustment(Integer.parseInt(request.getParameter("id")));
	    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
					out.println ("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "'>");
					out.println ("<BR><H2>You've removed the leave adjustment sucessfully.</H2>");
					
					TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
					log.writeEntry(
						sUserID, 
						TCLogEntry.LOG_OPERATION_ADMIN_REMOVED_LEAVE_ADJUSTMENT, 
						"Leave adjustment deleted for '" + sEmployeeID + "'", 
						sSQL, 
						"[1518039164]"
					);
		    	}else{
		    		out.println("<BR><H2>Failed to remove the leave adjustment.</H2><BR>");
		    		out.println ("<TD><A HREF=\"" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "\"><IMG src=\"" 
						+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
						+ "return.gif\"></A></TD>");
		    	}
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error in EmployeeLeaveRemove!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}