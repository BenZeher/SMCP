package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

/** Servlet that reads pin number for validation.*/

public class PeriodTotalEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * This form is for editing time entries. 
	 * 1.	if there is already a date passed in, default to that date.
	 * 2.	if there is no date at all, default to today.
	 */
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Time Entry Edit";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    		    
	    try {
        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalSave\">");
        	
        	//forward the parameters from previous form to the next form.
        	out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=\"" + request.getParameter("RecID") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	
        	//out.println("OriginalURL: " + request.getParameter("OriginalURL"));
        	out.println("<TABLE CELLPADDING=4>");
        	out.println("<TR>");
        	out.println("<TD ALIGN=CENTER><H4> Total Hours </H4></TD>");
        	out.println("<TD ALIGN=CENTER><INPUT TYPE=TEXT NAME=AdjustedTotal VALUE=" + request.getParameter("Total")+ "></TD>");
        	out.println("</TR>");

        	out.println("<TR>");
        	out.println("<TD ALIGN=CENTER><H4> Regular Hours </H4></TD>");
        	out.println("<TD ALIGN=CENTER><INPUT TYPE=TEXT NAME=AdjustedRegular VALUE=" + request.getParameter("Regular")+ "></TD>");
        	out.println("</TR>");

        	out.println("<TR>");
        	out.println("<TD ALIGN=CENTER><H4> Over Hours </H4></TD>");
        	out.println("<TD ALIGN=CENTER><INPUT TYPE=TEXT NAME=AdjustedOver VALUE=" + request.getParameter("Over")+ "></TD>");
        	out.println("</TR>");

        	out.println("<TR>");
        	out.println("<TD ALIGN=CENTER><H4> Double Hours </H4></TD>");
        	out.println("<TD ALIGN=CENTER><INPUT TYPE=TEXT NAME=AdjustedDouble VALUE=" + request.getParameter("Double")+ "></TD>");
        	out.println("</TR>");

        	out.println("<TR>");
        	out.println("<TD ALIGN=CENTER><H4> Leave Hours </H4></TD>");
        	out.println("<TD ALIGN=CENTER><INPUT TYPE=TEXT NAME=AdjustedLeave VALUE=" + request.getParameter("Leave")+ "></TD>");
        	out.println("</TR>");
        	
	        out.println("</TABLE><BR><BR>");
	        
        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
        	
        	out.println("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("Error in PeriodTotalEdit class - " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
