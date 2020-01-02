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

public class ManagerReviewListCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Manager Review List criterias";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println(
    		"<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
	    try {

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManagerReviewListGenerate\">");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
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
        	boolean bIncludeInactive = Boolean.parseBoolean(request.getParameter("ShowInactive"));
        	sSQL = TimeCardSQLs.Get_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(), bIncludeInactive, false);
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"SelectedEmployee\">" );
        	out.println ("<OPTION VALUE=\"\">----All Employees---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + ">" + rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName"));
        	}
	        out.println ("</SELECT>");
	        if (bIncludeInactive){
	        	//show a link to include all employees, regardless of activeness.
	        	out.println("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManagerReviewListCriteriaSelection?ShowInactive=false\"><FONT SIZE=1>(Exclude inactive employees)</FONT></A>");
	        }else{
	        	out.println("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManagerReviewListCriteriaSelection?ShowInactive=true\"><FONT SIZE=1>(Include inactive employees)</FONT></A>");
	        }
	        out.println ("</TD></TR>");
	        
	        //Special Type selection, default to "All Types".
        	out.println("<TR><TD ALIGN=CENTER><H3> Time Entry Type </H3></TD>");
        	sSQL = TimeCardSQLs.Get_Time_Entry_Type_Info_SQL();
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"SelectedTimeEntryType\">" );
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getInt("iTypeID") + ">" + rs.getString("sTypeTitle") + " - " + rs.getString("sTypeDesc"));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	     
	        rs.close();
	        out.println ("</TABLE><BR>");
	        
	        out.println ("<INPUT TYPE=CHECKBOX NAME=IncludeFinalized VALUE=1>Include finalized time entries.<BR><BR>");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"ShowInactive\" VALUE=\"" + request.getParameter("ShowInactive") + "\">");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR>Error in ManagerReviewListCriteriaSelection class - " + ex.getMessage() + ".<BR>");
	    }
 
	    out.println("</BODY></HTML>");
	}
}