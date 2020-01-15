package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;

/** Servlet that reads pin number for validation.*/

public class SpecialNoteReportCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Special note report";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    HttpSession CurrentSession = request.getSession();
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteReportGenerate\">");
        	
        	out.println("<B>Use any combination of the criterias below, keep the unused ones as \"ALL\"<BR><BR>");
        	
        	out.println("<TABLE CELLPADDING=10 border=4>");
        	
        	//list all available special note types
        	//Department selection, default to "All Types".
        	out.println("<TR><TD ALIGN=CENTER><H3> Note Types </H3></TD>");
        	//get recordset of departments
        	String sSQL = TimeCardSQLs.Retrieve_Special_Note_Types_SQL();
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println ("<TD><SELECT NAME=\"SelectedNoteType\">" );
        	out.println ("<OPTION VALUE=\"0-+-All Notes\">----All Types---- ");
        	
        	while (rs.next()){
        		if (rs.getInt("iTypeID") != 0){
        			out.println ("<OPTION VALUE=\"" + rs.getString("iTypeID")+ "-+-" + rs.getString("sTypeTitle") + "\">" + rs.getString("sTypeTitle"));
        		}
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
        	
        	//list all available periods
	    	SimpleDateFormat formatter = new SimpleDateFormat ("MM-dd-yyyy");
	    	
        	out.println("<TR><TD ALIGN=CENTER><H3> Available periods </H3></TD>");
        	sSQL = TimeCardSQLs.Retieve_Available_Periods_SQL();
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println ("<TD><SELECT NAME=\"SelectedPeriod\">" );
        	out.println ("<OPTION VALUE=\"\">----All Periods---- ");
        	out.println ("<OPTION VALUE=\"0000-00-00\">**Unposted**");
        	out.println ("<OPTION VALUE=\"" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "\">**Unfinalized**");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("datPeriodEndDate") + ">" + formatter.format(rs.getTimestamp("datPeriodEndDate")));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
        	        	
        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
        	//get recordset of departments
        	sSQL = TimeCardSQLs.Get_Department_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString());
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        //print the first line here. 
        	out.println ("<TD><SELECT NAME=\"SelectedDepartment\">" );
        	out.println ("<OPTION VALUE=\"\">----All Departments---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("iDeptID") + ">" + rs.getInt("iDeptID") + " - " + rs.getString("sDeptDesc"));
        	}
	        out.println ("</SELECT>");
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
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579104751] Error in PeriodTimeTotal class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}



