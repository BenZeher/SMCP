package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.util.Calendar;

public class LateReportCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Late report criterias";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LateReportGenerate\">");
        	
        	out.println("<TABLE CELLPADDING=10>");
        	//
        	out.println("<TR><TD ALIGN=CENTER><H3> Starting Date </H3></TD>");
        	Calendar c = Calendar.getInstance();
        	
        	//set start date to be the start day of the pay period based on today's date.
        	c.setTime(new Date(System.currentTimeMillis()));
        	c.add(Calendar.DAY_OF_MONTH, -(TimeCardUtilities.Get_Pay_Period_Length(getServletContext())) * 7);
        	//out.println("Period date: " + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.YEAR));
        	out.println("<TD>");
        		out.println("Month <SELECT NAME=\"SelectedStartingMonth\">");
	        		for (int i=1; i<=12;i++){
	        			if (i == c.get(Calendar.MONTH) + 1){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
	        	out.println("Day <SELECT NAME=\"SelectedStartingDay\">");
		    		for (int i=1; i<=31;i++){
	        			if (i == c.get(Calendar.DAY_OF_MONTH)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("Year <SELECT NAME=\"SelectedStartingYear\">");
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
        	
        	out.println("<TR><TD ALIGN=CENTER><H3> Ending Date </H3></TD>");
        	//set end date to be yesterday
        	c.setTime(new Date(System.currentTimeMillis()));
        	c.add(Calendar.DAY_OF_MONTH, -1);
        	//out.println("Monday date: " + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.YEAR));
        	out.println("<TD>");
        		out.println("Month <SELECT NAME=\"SelectedEndingMonth\">");
	        		for (int i=1; i<=12;i++){
	        			if (i == c.get(Calendar.MONTH) + 1){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
	        		}
	        	out.println("</SELECT>");
	        	out.println("Day <SELECT NAME=\"SelectedEndingDay\">");
		    		for (int i=1; i<=31;i++){
	        			if (i == c.get(Calendar.DAY_OF_MONTH)){
	        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
	        			}else{
	        				out.println("<OPTION VALUE=" + i + ">" + i);
	        			}
		    		}
	    		out.println("</SELECT>");	
	        	out.println("Year <SELECT NAME=\"SelectedEndingYear\">");
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
        	String sSQL;
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
	        
        	//Employee selection, default to "All Employees".
        	out.println("<TR><TD ALIGN=CENTER><H3> Employee </H3></TD>");
        	sSQL = TimeCardSQLs.Get_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(), false, false);
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"SelectedEmployee\">" );
        	out.println ("<OPTION VALUE=\"\">----All Employees---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + ">" + rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName"));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	     
	        rs.close();
	        out.println ("</TABLE><BR>");
        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579100899] Error in LateReportCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
