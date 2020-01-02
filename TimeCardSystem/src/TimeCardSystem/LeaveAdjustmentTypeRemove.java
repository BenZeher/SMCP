package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.util.ArrayList;

/** Servlet that insert In-Time records into the the time entry table.*/

public class LeaveAdjustmentTypeRemove extends HttpServlet{

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
	    		out.println("<META http-equiv='Refresh' content='0;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeEdit?Type=" + request.getParameter("Type") + "'>");
	    	}else{
	    		//delete the selected entry
	    		ArrayList<String> alSQLs = new ArrayList<String>(0);
	    		
	    		alSQLs.add(TimeCardSQLs.Get_Remove_Leave_Adjustment_Type_SQL(request.getParameter("Type")));
	    		
	    		if (clsDatabaseFunctions.executeSQLsInTransaction(alSQLs, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    			out.println("<META http-equiv='Refresh' content='0;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeList'>");
					out.println ("<BR><H2>You've removed the leave adjustment type sucessfully.</H2>");
		    	}else{
		    		out.println("<BR><H2>Failed to remove leave adjustment type.</H2><BR>");
		    		out.println ("<TD><A HREF=\"" + response.encodeURL("" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeEdit?Type=" + request.getParameter("Type")) + "\"><IMG src=\"" 
+ TCWebContextParameters.getInitImagePath(getServletContext()) 
+ "return.gif\"></A></TD>");
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
