package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.LeaveAdjustmentTypes;
import TCSDataDefinition.SpecialEntryTypes;

public class EmployeePerformanceListCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Employee Performance Report";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeePerformanceList\">");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	//
        	out.println("<TR><TD ALIGN=CENTER><H3>Starting Date </H3></TD>");
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
        	String sSQL;
	        
        	//Employee selection, default to "All Employees".
        	out.println("<TR><TD ALIGN=CENTER><H3> Employee </H3></TD>");
        	sSQL = TimeCardSQLs.Get_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(), false, false);	
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"SelectedEmployee\">" );
        	String sEmployee;
        	if (request.getParameter("SelectedEmployee") != null){
        		sEmployee = request.getParameter("SelectedEmployee");
        	}else{
        		sEmployee = "nobody";
        	}
        	while (rs.next()){
        		if (rs.getString("sEmployeeID").compareTo(sEmployee) == 0){
        			out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + " SELECTED>" + rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName"));
        		}else{
        			out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + ">" + rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName"));
        		}
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
	        //Employee selection, default to "All Employees".
        	out.println("<TR><TD ALIGN=CENTER><H3> Type </H3></TD>");
        	out.println("<TD>");
        	
        	ArrayList<String> alLATypes = new ArrayList<String>(0);
        	//add one entry for "all Types"
        	alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!0!ALLTYPES VALUE=\"-1\"><B>All Types</B>");
        	
        	//add entries for leaves
        	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL();
	        ResultSet rsLATypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	while (rsLATypes.next()){
        		alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsLATypes.getInt(LeaveAdjustmentTypes.iTypeID) + " VALUE=0>" + rsLATypes.getString(LeaveAdjustmentTypes.sTypeTitle) + " - " + rsLATypes.getString(LeaveAdjustmentTypes.sTypeDesc));
        	}
        	rsLATypes.close();
        	
        	//add entry for late
        	alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!3!LATE VALUE=0><B>Late</B>");
        	
        	//add all special time entries types
        	sSQL = TimeCardSQLs.Get_Time_Entry_Type_Info_SQL();
	        ResultSet rsSTETypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	while (rsSTETypes.next()){
        		//get around regular
        		if (rsSTETypes.getInt("iTypeID") != 0){
        			alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsSTETypes.getInt(SpecialEntryTypes.iTypeID) + " VALUE=0>" + rsSTETypes.getString(SpecialEntryTypes.sTypeTitle) + " - " + rsSTETypes.getString(SpecialEntryTypes.sTypeDesc));
        		}
        	}
        	rsSTETypes.close();
        	
        	out.println(TimeCardUtilities.Build_HTML_Table(3, alLATypes, 0, false));
        	out.println("</SELECT></TD></TR>");
	         
	        rs.close();
	        out.println ("</TABLE><BR>");

	        out.println("<INPUT TYPE=HIDDEN NAME=\"WorkSortBy\" VALUE=\"id\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"WorkSortOrder\" VALUE=0>");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"LeaveSortBy\" VALUE=\"id\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"LeaveSortOrder\" VALUE=0>");
	        
	        out.println ("<INPUT TYPE=CHECKBOX NAME=IncludeSpecialAdjustment VALUE=\"true\">Include special adjustment entries.<BR><BR>");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in EmployeeLeaveListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}