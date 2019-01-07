package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class ManagerAccessControlSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "saving....";
	    out.println (TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {

/*	        ManagerAccessControl;
			+---------------+------------+------+-----+---------+----------------+
			| Field         | Type       | Null | Key | Default | Extra          |
			+---------------+------------+------+-----+---------+----------------+
			| id            | int(4)     |      | PRI | NULL    | auto_increment |
			| sManagerID    | varchar(9) |      |     |         |                |
			| iDepartmentID | int(11)    |      |     | 0       |                |
			+---------------+------------+------+-----+---------+----------------+
*/
	        String s[] = request.getParameterValues("SELECTEDDEPT");
	        if (s != null){ 
		        //reset the flags for this manager
		        String  sSQL = TimeCardSQLs.Get_Reset_Manager_Access_Control(request.getParameter("ManagerID"));
		        clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        //set the flags for this manager
		        for (int i = 0; i < s.length; i++){
		        	sSQL = TimeCardSQLs.Get_Insert_Manager_Access_Control(request.getParameter("ManagerID"),
							  											  Integer.parseInt(s[i]));
		        	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        }
	        }else{
	        	//reset the flags for this manager
		        String  sSQL = TimeCardSQLs.Get_Reset_Manager_Access_Control(request.getParameter("ManagerID"));
		        clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        }
        		        
        	out.println ("<BR>");
        	out.println ("<H4>Information saved!!</H4><BR><BR>");
        	out.println("<META http-equiv='Refresh' content='1;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("ManagerID") + "'>");
        	//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "SMMgr.MechList>Click here to return to managers list.</A>");
	     	  
	    } catch (SQLException ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	        out.println("SQLState: " + ex.getSQLState() + "<BR>");
	        out.println("SQL: " + ex.getErrorCode() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}