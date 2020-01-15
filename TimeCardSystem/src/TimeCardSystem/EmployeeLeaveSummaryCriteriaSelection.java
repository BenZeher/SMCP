package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.LeaveAdjustmentTypes;

public class EmployeeLeaveSummaryCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Employee leave summary report";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveSummaryGenerate\">");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        
        	Calendar c = Calendar.getInstance();
        	out.println("<TR><TD ALIGN=CENTER><H3> As Of Date </H3></TD>");
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
    
        	//Department selection, default to "All Department".
        	out.println("<TR><TD ALIGN=CENTER><H3> Department </H3></TD>");
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
        	
	        //Employee selection, default to "All Employees".
        	out.println("<TR><TD ALIGN=CENTER><H3> Leave Type </H3></TD>");
	        sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL();
	        ResultSet rsLATypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	out.println("<TD>");
        	
        	ArrayList<String> alLATypes = new ArrayList<String>(0);
        	//add one entry for "all Types"
        	alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!1!ALLTYPES VALUE=\"-1\"><B>All Types</B>");
        	while (rsLATypes.next()){
        		alLATypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsLATypes.getInt(LeaveAdjustmentTypes.iTypeID) + " VALUE=0>" + rsLATypes.getString(LeaveAdjustmentTypes.sTypeTitle) + " - " + rsLATypes.getString(LeaveAdjustmentTypes.sTypeDesc));
        	}
        	rsLATypes.close();
        	out.println(TimeCardUtilities.Build_HTML_Table(3, alLATypes, 0, false));
        	out.println("</SELECT></TD></TR>");
	         
	        rs.close();
	        out.println ("</TABLE><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("<INPUT TYPE=\"CHECKBOX\" NAME=\"ShowEligibleOnly\" VALUE=\"1\">Show eligible leave type total only.");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579100869] Error in EmployeeLeaveSummaryCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}