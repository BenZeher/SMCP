package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeContactList extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

    	String title = "Phone List";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
    	//sCurrentURL = sCurrentURL.replaceAll("&", "*");

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {
	    		    	
	    	String sSQL;
	    	boolean bSortByDept;
        		//any access level higher than manager, no constrain on departments anymore.
        		sSQL = TimeCardSQLs.Employee_Contact_Info_List_SQL("ADMIN",
        														   request.getParameter("SelectedDepartment"),
        														   request.getParameter("SortBy"));	
        	//}else{
        	//	sSQL = TimeCardSQLs.Employee_Contact_Info_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_USERID).toString(),
			//			  										   request.getParameter("SelectedDepartment"),
        	//													   request.getParameter("SortBy"));
        	//}
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	if (request.getParameter("SortBy").compareTo("Department") == 0){
	    		bSortByDept = true;
	    	}else{
	    		bSortByDept = false;
	    	}
	    	
	    	if (request.getParameter("SelectedDepartment").compareTo("0") != 0){
	    		sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("SelectedDepartment")));
	    		ResultSet rsDeptInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rsDeptInfo.next()){
	    			out.println("<B>Department: " + rsDeptInfo.getString("sDeptDesc") + "</B><BR>");
	    		}
	    		rsDeptInfo.close();
	    	}else{
	    		out.println("<B>Department: ALL </B><BR>");
	    	}

    		out.println("<TABLE BORDER=1 WIDTH=100%>");
    		
    		out.println("<TR>");
    		/*
	    	if (request.getParameter("SelectedDepartment").compareTo("0") == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Department</H4></TD>");
	    	}
    		*/
	    	out.println("<TD ALIGN=CENTER WIDTH=%><B>Employee Name</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Ext.</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Office Phone</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Company Cell Phone</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Home Phone</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Company Email</B></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=%><B>Nextel Direct Connect</B></TD>");
			
			int iCurrentDept = -1;
			
			while (rs.next()){
				if (bSortByDept == true){
					if (iCurrentDept != rs.getInt("iDeptID")){
						out.println("<TR><TD COLSPAN=7><FONT SIZE=2><B>" + rs.getString("sDeptDesc") + "</B></FONT></TD></TR>");
						iCurrentDept = rs.getInt("iDeptID");
					}
				}
				out.println("<TR>");
				
        		//employee name
        		out.println("<TD ALIGN=CENTER><Font SIZE=2>");
        		if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
													AccessControlFunctionList.EditEmployeeGeneralInformation)){
        			out.println("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeGeneralEdit?EmployeeID=" + rs.getString("sEmployeeID") + "&OriginalURL= " + sCurrentURL + "#CONTACTINFO\">");
        		}
        		out.println(rs.getString("Employees.sEmployeeLastName") + ", " +
						    rs.getString("Employees.sEmployeeFirstName") + " " + 
						    rs.getString("Employees.sEmployeeMiddleName"));
        		if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
						AccessControlFunctionList.EditEmployeeGeneralInformation)){
        			out.println("</A>");
        		}
        		out.println("</FONT></TD>");
        		//Extension
        		if (rs.getString("sExtension").compareTo("") != 0){
        			out.println("<TD ALIGN=CENTER><Font SIZE=2>" + rs.getString("sExtension") + "</FONT></TD>");
        		}else{
        			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
        		}
        		//Office Phone
        		if (rs.getString("sOfficePhone").compareTo("") != 0){
        			out.println("<TD ALIGN=CENTER><Font SIZE=2>" + TimeCardUtilities.Format_PhoneNumber(rs.getString("sOfficePhone")) + "</FONT></TD>");
	    		}else{
	    			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
	    		}
	        		//Cell Phone
	        		if (rs.getString("sCellPhone").compareTo("") != 0){
	        		out.println("<TD ALIGN=CENTER><Font SIZE=2>" + TimeCardUtilities.Format_PhoneNumber(rs.getString("sCellPhone")) + "</FONT></TD>");
	    		}else{
	    			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
	    		}
	        		//Home Phone
	    		if (rs.getString("sHomePhone").compareTo("") != 0){
	        		out.println("<TD ALIGN=CENTER><Font SIZE=2>" + TimeCardUtilities.Format_PhoneNumber(rs.getString("sHomePhone")) + "</FONT></TD>");
	    		}else{
	    			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
	    		}
	        		//Email
	    		if (rs.getString("sEmail").compareTo("") != 0){
	        		out.println("<TD ALIGN=CENTER><Font SIZE=2>" + rs.getString("sEmail") + "</FONT></TD>");
	    		}else{
	    			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
	    		}
	        		//Nextel Direct Call
	    		if (rs.getString("sNextelDirectCall").compareTo("") != 0){
	        		out.println("<TD ALIGN=CENTER><Font SIZE=2>" + rs.getString("sNextelDirectCall") + "</FONT></TD>");
	    		}else{
	    			out.println("<TD ALIGN=CENTER><Font SIZE=2>&nbsp;</FONT></TD>");
	    		}
        	}    		
    		out.println("</TR></TABLE><BR><BR>");
    		out.println("Click on employee name to get detailed contact information.");
    		
    		rs.close();
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}
