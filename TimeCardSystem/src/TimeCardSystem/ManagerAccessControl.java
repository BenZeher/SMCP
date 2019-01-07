package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class ManagerAccessControl extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Manager Access Control Page";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
    	//sCurrentURL = sCurrentURL.replaceAll("&", "*");
    	
	    try {

/*
  			ManagerAccessControl;
			+--------------+------------+------+-----+---------+----------------+
			| Field        | Type       | Null | Key | Default | Extra          |
			+--------------+------------+------+-----+---------+----------------+
			| id           | int(4)     |      | PRI | NULL    | auto_increment |
			| sManagerID   | varchar(9) |      |     |         |                |
			| iDeparmentID | int(11)    |      |     | 0       |                |
			+--------------+------------+------+-----+---------+----------------+

*/
	    	out.println("<H4>Here is a list of users who are currently assigned MANAGER level of access and their accessible departments. </H4>");
	    	out.println("<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManagerAccessControlEdit?ManagerID=0&ManagerName=NEW&OriginalURL=" + sCurrentURL + "\">Assign department to new manager</A><BR><BR>");
    		out.println("<TABLE BORDER=1 WIDTH=60%>");
    		//heading
    		out.println("<TR><TD><B>Manager</B></TD>" +
    					"<TD><B>Department Name</B></TD></TR>");
	    	String sCurrentManager = "";
	    	String sManagerName = "";
	    	String sSQL = TimeCardSQLs.Get_Manager_Access_Control_Info_SQL();
	    	ResultSet rsManagerAC = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
    		
	    	while (rsManagerAC.next()){
	    		
	    		out.println("<TR>");
	    		if (sCurrentManager.compareTo(rsManagerAC.getString("sManagerID")) != 0){;
	    			sCurrentManager = rsManagerAC.getString("sManagerID");
	    			sManagerName = rsManagerAC.getString("sEmployeeLastName") + ", " + rsManagerAC.getString("sEmployeeFirstName") + " " + rsManagerAC.getString("sEmployeeMiddleName");
			    	out.println("<TR><TD COLSPAN=2></TD></TR>");
			    	out.println("<TR><TD COLSPAN=2></TD></TR>");
	    			out.println("<TD><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManagerAccessControlEdit?ManagerID=" + sCurrentManager + "&ManagerName=" + sManagerName + "&OriginalURL=" + sCurrentURL + "\">" + 
	    					    	  rsManagerAC.getString("sManagerID") + " - " + sManagerName + "</A></TD>");
	    		}else{
	    			out.println("<TD></TD>");
	    		}
	    		out.println("<TD>" + rsManagerAC.getString("sDeptDesc") + "</TD>");
	    		out.println("</TR>");
	    	}
	    	rsManagerAC.close();
		
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