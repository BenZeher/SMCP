package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that reads pin number for validation.*/

public class PostPeriodTotalConfirmation extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Post period time total confirmation";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
	    
	    try {
	    	
	    	String sEndPeriodDate = request.getParameter("SelectedEndMonth") + "/" + request.getParameter("SelectedEndDay") + "/" + request.getParameter("SelectedEndYear");
	    	
	    	//get department name of chosen.
	    	String sDepartmentName = "N/A";
	    	if (request.getParameter("SelectedDepartment").compareTo("0") == 0){
	    		sDepartmentName = "ALL DEPARTMENTS"; 
	    	}else{
		    	String sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("SelectedDepartment")));
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
		    	if (rs.next()){
		    		sDepartmentName = rs.getString("sDeptDesc");
		    	}
		    	rs.close();
	    	}
	    	out.println("<BR><BR><H2>You are about to post period time totals for <FONT COLOR=MAROON>" 
	    		+ sDepartmentName + "</FONT> ending on <FONT COLOR=MAROON>" + sEndPeriodDate + "</FONT>.</H2>");
	    	out.println("<H3>To continue with posting, click <B>\"Confirm\"</B>. Otherwise, use the back button to go back.</H3>");
	    	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PostPeriodTotal\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME=SelectedEndYear VALUE=" + request.getParameter("SelectedEndYear") + ">");
	    	out.println("<INPUT TYPE=HIDDEN NAME=SelectedEndMonth VALUE=" + request.getParameter("SelectedEndMonth") + ">");
	    	out.println("<INPUT TYPE=HIDDEN NAME=SelectedEndDay VALUE=" + request.getParameter("SelectedEndDay") + ">");
	    	out.println("<INPUT TYPE=HIDDEN NAME=SelectedDepartment VALUE=" + request.getParameter("SelectedDepartment") + ">");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Confirm----\">");
        	out.println ("</FORM>");
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error [1417641416] in PostPeriodTotalConfirmation class - " + ex.getMessage());
	    }
	}
}
