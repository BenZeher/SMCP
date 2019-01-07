package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.Employees;

import java.sql.*;


/** Servlet that reads pin number for validation.*/

public class EmployeeList extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
//		String sPinNumber = TimeCardUtilities.filter(request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER));
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
	    
	    boolean bIncludeInactive = Boolean.parseBoolean(request.getParameter("ShowInactive"));
	    
	    String title = "Time Card System";
	    String subtitle = "Employee list";
	    if (bIncludeInactive){
	    	subtitle += " (Includes ACTIVE and INACTIVE Employees)";
	    }else{
	    	subtitle += " (Includes Only ACTIVE Employees)";
	    }
	    String bar = "";
	    try {
			bar = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>Error [1443448641] - could not read current session attribute for company - " + e.getMessage() + "</B></FONT><BR>");
		}
	    out.println(TimeCardUtilities.TCBarTitleSubBGColor(bar, title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
    	
	    try {
	    	
	        String sSQL = ""; //TimeCardSQLs.Get_Employee_List_SQL(bIncludeInactive);
	        //out.println (sSQL);
	        if (bIncludeInactive){
	        	out.println("<H2>Employee(s) in system (INCLUDING INACTIVE):</H2><BR>");
		    }else{
		    	out.println("<H2>Employee(s) in system (ACTIVE ONLY):</H2><BR>");
		    }
	        
	    	if (request.getParameter("InfoType").compareTo("general") == 0){
	    		out.println ("<FORM METHOD=POST ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeGeneralEdit\">");
	    		sSQL = TimeCardSQLs.Get_Employee_List_SQL(bIncludeInactive);
	    	}else if (request.getParameter("InfoType").compareTo("confidential") == 0){
	    		out.println ("<FORM METHOD=POST ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeConfidentialEdit\">");
	    		sSQL = TimeCardSQLs.Get_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(), bIncludeInactive, false); //restrict employee list by manager's access
	    	}
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)); 
        	
        	//out.println ("METHOD=POST onSubmit=\"return dropdown(this.Mechanic)\">");
        	out.println ("<SELECT NAME=\"EmployeeID\">" );
        	//only editing employee general information allows new employee creation.
        	if (request.getParameter("InfoType").compareTo("general") == 0 && 
    			TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												AccessControlFunctionList.EditEmployeeConfidentialInformation)){ 
        		out.println ("<OPTION VALUE=a0b0c0>----Create New Employee---- ");
        	}
        	
        	while (rs.next()){
        		//flag that there is multiple entries.
        		String sInactive = "";
        		if (rs.getInt(Employees.iActive) == 0){
        			sInactive = " (INACTIVE)";
        			
        		}
	        	out.println ("<OPTION VALUE=" + rs.getString("sEmployeeID") + ">" 
	        		+ rs.getString("sEmployeeID") + " - " + rs.getString("sEmployeeLastName") + ", " + rs.getString("sEmployeeFirstName") + sInactive);
        	}
	        	//finish the table.
	        out.println ("</SELECT> ");
	        if (bIncludeInactive){
	        	//show a link to include all employees, regardless of activeness.
	        	out.println("<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeList?ShowInactive=false&InfoType=" + request.getParameter("InfoType") + "\"><FONT SIZE=2>(Exclude inactive employees)</FONT></A>");
	        }else{
	        	out.println("<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeList?ShowInactive=true&InfoType=" + request.getParameter("InfoType") + "\"><FONT SIZE=2>(Include inactive employees)</FONT></A>");
	        }
	        out.println ("<BR>");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"InfoType\" VALUE=\"" + request.getParameter("InfoType") + "\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + sCurrentURL + "\">");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Edit----\">");
        	out.println ("</FORM>");
		   
        	rs.close();
        	
	    } catch (SQLException ex) {
		    // handle any errors
			out.println("<BR><BR>Error!!<BR>");
		    out.println("SQLException: " + ex.getMessage() + "<BR>");
		    out.println("SQLState: " + ex.getSQLState() + "<BR>");
		    out.println("SQL: " + "<BR>");
		}
		out.println("</BODY></HTML>");
	}
}
