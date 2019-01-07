package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

/** Servlet that insert In-Time records into the the time entry table.*/

public class LateFlagChange extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {
	    	String sSQL;
	    	
	    	sSQL = TimeCardSQLs.Get_Change_Late_Flag_SQL(Integer.parseInt(request.getParameter("ID")), 
	    												 Integer.parseInt(request.getParameter("CurrentStatus")));
	    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    		out.println("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    	}else{
	    		out.println("<BR><H2>Failed to alter late flag for this time entry.</H2><BR>");
	    		out.println ("<TD><A HREF=\"" + response.encodeURL(request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID")) + "\"><IMG src=\"" 
+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
+ "return.gif\"></A></TD>");
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error in LateFlagChange class!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}