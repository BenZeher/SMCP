package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.util.Calendar;

/** Servlet that reads pin number for validation.*/

public class PostPeriodTotalCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Post total period time criterias";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    HttpSession CurrentSession = request.getSession();
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	out.println("Make sure all time entries are properly filled in before proceeding with posting time totals. <BR><BR><BR>");
	    try {
        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PostPeriodTotalConfirmation\">");
        	out.println("<TABLE CELLPADDING=10>");
        	Calendar c = Calendar.getInstance();
        	c.setTime(new Date(System.currentTimeMillis()));
        	c.add(Calendar.DAY_OF_MONTH, -1);
        	
        	//period end date. Default to yesterday.
        	out.println("<TR><TD ALIGN=CENTER><H3> Period End Date </H3></TD>");
        	//out.println("Monday date: " + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.YEAR));
        	out.println("<TD>");
        		out.println("Month <SELECT NAME=\"SelectedEndMonth\">");
	        		for (int i=1; i<=12;i++){
	        			if (i == c.get(Calendar.MONTH) + 1){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
	        	out.println("Day <SELECT NAME=\"SelectedEndDay\">");
		    		for (int i=1; i<=31;i++){
	        			if (i == c.get(Calendar.DAY_OF_MONTH)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("Year <SELECT NAME=\"SelectedEndYear\">");
		    		for (int i=1970; i<=2069;i++){
	        			if (i == c.get(Calendar.YEAR)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
        	out.println("</TD>");
        	out.println("</TR>");
        	
        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
        	//get recordset of departments
        	String sSQL = TimeCardSQLs.Get_Department_List_SQL();
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
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Post----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in PostPeriodTotalCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
