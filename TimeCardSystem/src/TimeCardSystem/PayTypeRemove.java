package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

/** Servlet that insert In-Time records into the the time entry table.*/

public class PayTypeRemove extends HttpServlet{

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
	    	
	    	if (request.getParameter("DoubleCheck") == null){
	    		//don't delete, just go back.
	    		out.println("<META http-equiv='Refresh' content='0;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeEdit?Type=" + request.getParameter("Type") + "'>");
	    	}else{
	    		//delete the selected entry
	    		sSQL = TimeCardSQLs.Get_Remove_Pay_Type_SQL(request.getParameter("Type"));
	    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    			out.println("<META http-equiv='Refresh' content='0;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeList'>");
					out.println ("<BR><H2>You've removed the pay type sucessfully.</H2>");
		    	}else{
		    		out.println("<BR><H2>Failed to remove pay type.</H2><BR>");
		    		out.println ("<TD><A HREF=\"" + response.encodeURL("" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeEdit?Type=" + request.getParameter("Type")) + "\"><IMG src=\"" 
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
