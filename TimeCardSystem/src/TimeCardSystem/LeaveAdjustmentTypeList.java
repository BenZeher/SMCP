
package TimeCardSystem;

import java.io.*;
//	import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that reads pin number for validation.*/

public class LeaveAdjustmentTypeList extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Leave adjustment type list";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {
	    	
	        String sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)); 
	        
	        out.println("<H2>Leave adjustment type(s) in system:</H2><BR>");
	        
        	//print the first line here. 
        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeEdit\">");
        	out.println ("<SELECT NAME=\"Type\">" );
        	out.println ("<OPTION VALUE=0>----Create New Type---- ");
        	
        	while (rs.next()){
        		out.println ("<OPTION VALUE=" + rs.getInt("iTypeID") + ">" + rs.getInt("iTypeID") + ". " + rs.getString("sTypeTitle") + " - " + rs.getString("sTypeDesc"));
        	}
        	
        	//finish the table.
	        out.println ("</SELECT>");
	        out.println ("<BR>");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Edit----\">");
        	out.println ("</FORM>");

        	//close resultset
        	rs.close();
        	
	    } catch (SQLException ex) {
		    // handle any errors
			out.println("<BR><BR>Error!!<BR>");
		    out.println("SQLException: " + ex.getMessage() + "<BR>");
		    out.println("SQLState: " + ex.getSQLState() + "<BR>");
		    out.println("SQL: " + "<BR>");
		}
		out.println("</BODY></HTML>");
	}
}

