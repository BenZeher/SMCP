package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

public class OnClockEmployeeListCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "On-clock employee list criterias";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	out.println("Select the departent(s) to get a list of employees who are currently on the clock (punched in). <BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.OnClockEmployeeList\">");
        	
        	out.println("<TABLE CELLPADDING=10>");
        	String sSQL;
        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
        	//get recordset of departments
        	sSQL = TimeCardSQLs.Get_Department_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString());
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println ("<TD><SELECT NAME=\"SelectedDepartment\">" );
        	out.println ("<OPTION VALUE=0>----All Departments---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("iDeptID") + ">" + rs.getInt("iDeptID") + " - " + rs.getString("sDeptDesc"));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	            
	        rs.close();
        	
	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579104265] Error in OnClockEmployeeListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
