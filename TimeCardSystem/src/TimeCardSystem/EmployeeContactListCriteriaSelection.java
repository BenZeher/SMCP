package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that reads pin number for validation.*/

public class EmployeeContactListCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Employee contact information criterias";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	out.println("Select the departent(s) to get a list of employee's contact information. <BR><BR>");

	    try {

        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeContactList\">");
        	
        	out.println("<TABLE CELLPADDING=10>");
        	String sSQL;
        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
        	//get recordset of departments
        	sSQL = TimeCardSQLs.Get_Department_List_SQL();
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println("<TD><SELECT NAME=\"SelectedDepartment\">" );
        	out.println("<OPTION VALUE=0>----All Departments---- ");
        	
        	while (rs.next()){
	        	out.println("<OPTION VALUE=" + rs.getString("iDeptID") + ">" + rs.getInt("iDeptID") + " - " + rs.getString("sDeptDesc"));
        	}
	        out.println("</SELECT>");
	        out.println("</TD></TR>");
	        
	        //sort method
        	out.println ("<TR><TABLE BORDER=0><TR><TD><B>Sort by:</B></TD><TD>");
        	out.println ("<INPUT TYPE=\"RADIO\" NAME=\"SortBy\" VALUE=\"LastName\" CHECKED>Last Name<BR>");
    		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"SortBy\" VALUE=\"Department\">Department<BR>");
    		out.println ("</TD></TR></TABLE></TD></TR>");
	            
	        rs.close();
        	
	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in PhoneListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
