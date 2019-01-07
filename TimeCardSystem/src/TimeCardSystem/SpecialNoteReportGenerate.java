package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.text.SimpleDateFormat;

/** Servlet that insert In-Time records into the the time entry table.*/

public class SpecialNoteReportGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

    	String title = request.getParameter("SelectedNoteType").substring(request.getParameter("SelectedNoteType").indexOf("-+-") + 3) + " Report";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, "", TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {
	    	
	    	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
	    	
	    	//out.println ("index: " + request.getParameter("SelectedNoteType").indexOf("-+-") + "<BR>");
	    	
	    	String sSQL = TimeCardSQLs.Get_Period_Special_Note_SQL(Integer.parseInt(request.getParameter("SelectedNoteType").substring(0, request.getParameter("SelectedNoteType").indexOf("-+-"))),
	    														   request.getParameter("SelectedPeriod"), 
	    														   request.getParameter("SelectedEmployee"), 
	    														   request.getParameter("SelectedDepartment"));
	    	//out.println ("<BR> " + sSQL + "<BR>");
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	//display this only when there is a parameter passed in
	    	//out.println("<B>Note Type: " + title + "</B><BR>");
	    	if (request.getParameter("SelectedPeriod").compareTo("") != 0){
	    		if (request.getParameter("SelectedPeriod").compareTo(TimeCardUtilities.TEMPORARY_POSTING_DATE) == 0){
	    			out.println("<B>Period ending date: Unfinalized</B><BR>");
	    		}else if (request.getParameter("SelectedPeriod").compareTo("0000-00-00") == 0){
	    			out.println("<B>Period ending date: Unposted</B><BR>");
	    		}else{
	    			out.println("<B>Period ending date: " + formatter.format(Timestamp.valueOf(request.getParameter("SelectedPeriod") + " 00:00:00")) + "</B><BR>");
	    		}
	    	}else{
	    		out.println("<B>Period ending date: ALL </B><BR>");
	    	}
	    	
	    	if (request.getParameter("SelectedEmployee").compareTo("") != 0){
	    		out.println("<B>Employee: " + request.getParameter("SelectedEmployee") + "</B><BR>");	    		
	    	}else{
	    		if (request.getParameter("SelectedDepartment").compareTo("") != 0){
		    		sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("SelectedDepartment")));
		    		ResultSet rsDeptInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    		if (rsDeptInfo.next()){
		    			out.println("<B>Department: " + rsDeptInfo.getString("sDeptDesc") + "</B><BR>");
		    		}
		    		rsDeptInfo.close();
		    	}else{
		    		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
		    			out.println("<B>Department: ALL </B><BR>");
		    		}
		    	}
	    		out.println("<B>Employee: ALL </B><BR>");
	    	}

    		out.println("<HR><BR><TABLE BORDER=1 WIDTH=95%>");
    		out.println("<TR>");

	    	if (Integer.parseInt(request.getParameter("SelectedNoteType").substring(0, request.getParameter("SelectedNoteType").indexOf("-+-"))) == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Note Type</H4></TD>");
	    	}
	    	if (request.getParameter("SelectedDepartment").compareTo("") == 0 && request.getParameter("SelectedEmployee").compareTo("") == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Department</H4></TD>");
	    	}
    		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
    			out.println("<TD ALIGN=CENTER><H4>Employee</H4></TD>");
    		}
	    	if (request.getParameter("SelectedPeriod").compareTo("") == 0){
	    		out.println("<TD ALIGN=CENTER><H4>Period End</H4></TD>");
	    	}

			out.println("<TD ALIGN=CENTER><H4>In Time</H4></TD>");
			out.println("<TD ALIGN=CENTER><H4>Out Time</H4></TD>");
			out.println("<TD ALIGN=CENTER WIDTH=30%><H4>Note</H4></TD></TR>");
    		
       		while (rs.next()){
        		out.println("<TR>");
        		//Note type
        		if (Integer.parseInt(request.getParameter("SelectedNoteType").substring(0, request.getParameter("SelectedNoteType").indexOf("-+-"))) == 0){
        			out.println("<TD ALIGN=CENTER>" + rs.getString("sTypeTitle") + "</TD>");
    	    	}
        		//Department
        		if (request.getParameter("SelectedDepartment").compareTo("") == 0 && request.getParameter("SelectedEmployee").compareTo("") == 0){
        			out.println("<TD ALIGN=CENTER>" + rs.getString("sDeptDesc") + "</TD>");
        		}
        		//employee name
        		if (request.getParameter("SelectedEmployee").compareTo("") == 0){
        			out.println("<TD ALIGN=CENTER>" + rs.getString("sEmployeeFirstName") + " " + rs.getString("sEmployeeLastName") + "</TD>");
        		}
        		//Period end date
    	    	if (request.getParameter("SelectedPeriod").compareTo("") == 0){
    	    		if (rs.getString("sPeriodDate").compareTo("0000-00-00") == 0 ||
    	    			rs.getString("sPeriodDate").compareTo(TimeCardUtilities.TEMPORARY_POSTING_DATE) == 0){
    	    			out.println("<TD ALIGN=CENTER>Unfinalized</TD>");
    	    		}else{
    	    			out.println("<TD ALIGN=CENTER>" + formatter.format(Timestamp.valueOf(rs.getString("sPeriodDate") + " 00:00:00")) + "</TD>");
    	    		}
    	    	}
        		//Start time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("dtInTime") + "</TD>");  
    	    	//End time
        		out.println("<TD ALIGN=CENTER>" + rs.getString("dtOutTime") + "</TD>");  
    	    	//Note
        		out.println("<TD ALIGN=CENTER>" + rs.getString("mNote") + "</TD>");  
        		
        		out.println("</TR>");
        	}    		
    		out.println("</TABLE>");
    		
    		rs.close();
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}
