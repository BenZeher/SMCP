package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.util.ArrayList;

/** Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeRemove extends HttpServlet{

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
	    	if (request.getParameter("DoubleCheck") == null){
	    		//don't delete, just go back.
	    		out.println("<META http-equiv='Refresh' content='1;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
	    	}else{
	    		//delete the selected entry
	    		ArrayList<String> alSQLs = new ArrayList<String>(0);
	    		alSQLs.add(TimeCardSQLs.Get_Remove_Employee_SQL(request.getParameter("EmployeeID")));
	    		alSQLs.add(TimeCardSQLs.Get_Remove_Employee_Auxiliary_SQL(request.getParameter("EmployeeID")));
	    		if (clsDatabaseFunctions.executeSQLsInTransaction(alSQLs, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    			out.println("<META http-equiv='Refresh' content='0;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
					out.println ("<BR><H2>You've removed the employee sucessfully.</H2>");
		    	}else{
		    		out.println("<BR><H2>Failed to remove employee.</H2><BR>");
		    		//out.println ("<TD><A HREF=\"" 
		    		//+ response.encodeURL("" 
		    		//+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
		    		//+ "TimeCardSystem.EmployeeEdit?Employee=" + request.getParameter("Employee")) + "\"><IMG src=\"" 
		    		//+ ConnectionPool.WebContextParameters.getInitImagePath(getServletContext()) 
		    		//+ "return.gif\"></A></TD>");
		    	}
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}