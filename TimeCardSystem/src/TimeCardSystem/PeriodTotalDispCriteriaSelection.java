package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/** Servlet that reads pin number for validation.*/

public class PeriodTotalDispCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String SUBMIT_LIST_BUTTON_NAME = "SubmitList";
	public static final String SUBMIT_LIST_BUTTON_VALUE = "----List----";
	public static final String SUBMIT_EXPORT_BUTTON_NAME = "SubmitExport";
	public static final String SUBMIT_EXPORT_BUTTON_VALUE = "----Export CSV----";
	public static final String Paramwarnings = "Warning";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    

	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Period time totals";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
		if(request.getParameter(Paramwarnings) != null){
			out.println("<B><FONT COLOR=\"RED\">Warning: " + request.getParameter(Paramwarnings) + "</B></FONT>");
		}
	    HttpSession CurrentSession = request.getSession();
    	out.println("<BR><BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalReportGenerate\">");
        	
        	out.println("<B>Use any combination of the criterias below, keep the unused ones as \"ALL\"<BR><BR>");
        	
        	out.println("<TABLE CELLPADDING=10 border=4>");
        	
        	
        	//list all available periods
	    	SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd-yyyy");
	    	
        	out.println("<TR><TD ALIGN=CENTER><H3> Available periods </H3></TD>");
        	String sSQL = TimeCardSQLs.Retieve_Available_Periods_SQL();
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println ("<TD><SELECT NAME=\"SelectedPeriodFrom\">");
        	out.println ("<OPTION VALUE=\"\">----All Periods---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("datPeriodEndDate") + ">" + formatter.format(rs.getTimestamp("datPeriodEndDate")));
        	}
	        out.println ("</SELECT><HR>");
	        out.println("<INPUT TYPE=CHECKBOX NAME=\"SelectMultiplePeriods\" VALUE=1>Select a range of periods.<BR>");
			out.println("<SELECT NAME=\"SelectedPeriodTo\">");
        	out.println("<OPTION VALUE=\"\">----All Periods---- ");
        	
			rs.beforeFirst();
        	while (rs.next()){
	        	out.println("<OPTION VALUE=" + rs.getString("datPeriodEndDate") + ">" + formatter.format(rs.getTimestamp("datPeriodEndDate")));
        	}
	        out.println("</SELECT>");
	        out.println("</TD></TR>");

        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
        	//get recordset of departments
        	sSQL = TimeCardSQLs.Get_Department_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString());
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	
        	//display available departments. 
        	out.println ("<TD>");
        	ArrayList<String> alDepartments = new ArrayList<String>(0);
        	//add one entry for "all department"
        	alDepartments.add("<INPUT TYPE=CHECKBOX NAME=!!ALLDEPT VALUE=0><B>All Departments</B>");
        	while (rs.next()){
        		alDepartments.add("<INPUT TYPE=CHECKBOX NAME=!!" + rs.getInt("iDeptID") + " VALUE=0>" + rs.getString("sDeptDesc"));
        	}
        	//System.out.println("Department count: " + alDepartments.size());
        	out.println(TimeCardUtilities.Build_HTML_Table(4, alDepartments, 0, false));
	        out.println ("</TD></TR>");
	        
        	//Employee selection, default to "All Employees".
        	out.println("<TR><TD ALIGN=CENTER><H3> Employee </H3></TD>");
        	//get recordset of departments
        	sSQL = TimeCardSQLs.Get_Employee_List_SQL(true);
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
	        //print the first line here. 
        	//out.println ("METHOD=POST onSubmit=\"return dropdown(this.Mechanic)\">");
        	out.println ("<TD><SELECT NAME=\"SelectedEmployee\">" );
        	out.println ("<OPTION VALUE=\"\">----All Employees---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + ">" + rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName"));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
	        rs.close();
        	
	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" NAME=\"" + SUBMIT_LIST_BUTTON_NAME+ "\" VALUE=\"" + SUBMIT_LIST_BUTTON_VALUE + "\">");
        	out.println ("&nbsp;&nbsp;");
        	out.println ("<INPUT TYPE=\"SUBMIT\" NAME=\"" + SUBMIT_EXPORT_BUTTON_NAME+ "\" VALUE=\"" + SUBMIT_EXPORT_BUTTON_VALUE + "\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in PeriodTimeTotal class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}

