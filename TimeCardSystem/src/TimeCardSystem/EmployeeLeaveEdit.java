package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.util.Calendar;



/** Servlet that reads pin number for validation.*/

public class EmployeeLeaveEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * This form is for editing leave adjustments. 
	 */
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Leave Adjustment Edit";
	    
    	HttpSession CurrentSession = request.getSession();
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

    	
    	try {
	    	String sSQL;
	    	String sEID = request.getParameter("Employee"); //need to know this for new records
	    	int iLType = 0;
	    	int iSpacialAdj = 0;
	    	Timestamp tsStartDate = null;
	    	Timestamp tsEndDate = null;
	    	double dDuration = 0;
	    	String sNote = "";
	    	
	    	//get id. see if it is a existing record or not.
	    	int id = Integer.parseInt(request.getParameter("id"));
	    	if (id < 0){
	    		//new record, set all default
	    		tsStartDate = new Timestamp(System.currentTimeMillis());
	    		tsEndDate = tsStartDate;
	    		dDuration = 0;
	    		sNote = "";
	    		iLType = 0;
	    		iSpacialAdj = 0;
	    	}else{
	    		//existing record. retrieve detailed record from database
	    		sSQL = TimeCardSQLs.Get_Leave_Adjustment_SQL(id);
	    		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rs.next()){
	    			tsStartDate = rs.getTimestamp("dtInTime");
	    			tsEndDate = rs.getTimestamp("dtOutTime");
	    			dDuration = rs.getDouble("dDuration");
	    			sNote = rs.getString("mNote").trim();
	    			iLType = rs.getInt("iLeaveTypeID");
	    			iSpacialAdj = rs.getInt("iSpecialAdjustment");
	    		}
	    		rs.close();
	    	}

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveSave\">");
        	
        	//forward the parameters from previous form to the next form.
        	out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=\"" + request.getParameter("id") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + sEID + "\">");
        	//out.println("OriginalURL: " + request.getParameter("OriginalURL"));
        	out.println("<TABLE BORDER=2 CELLPADDING=4>");
        	out.println("<TR><TD><H4>Leave Adjustment Type</H4></TD><TD><SELECT NAME=\"Type\">");
        	//get list of special time entry type
	    	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL();
    		ResultSet rsLT = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
    		while (rsLT.next()){
    			if (rsLT.getInt("iTypeID") == iLType){
        			out.println("<OPTION SELECTED VALUE=" + rsLT.getInt("iTypeID") + ">" + rsLT.getString("sTypeTitle") + " - " + rsLT.getString("sTypeDesc"));
        		}else{
        			out.println("<OPTION VALUE=" + rsLT.getInt("iTypeID") + ">" + rsLT.getString("sTypeTitle") + " - " + rsLT.getString("sTypeDesc"));
        		}
        	}
        	rsLT.close();
    		out.println ("</TD></TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H4> Start Date: </H4></TD>");
        	
        	Calendar c = Calendar.getInstance();
        	c.setTime(tsStartDate);
        	out.println("<TD>");
    		out.println("Month <SELECT NAME=\"SelectedStartMonth\">");
        		for (int i=1; i<=12;i++){
        			if (i == c.get(Calendar.MONTH) + 1){
        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
        			}else{
        				out.println("<OPTION VALUE=" + i + ">" + i);
        			}
        		}
        	out.println("</SELECT>");
	        	
        	out.println("Day <SELECT NAME=\"SelectedStartDay\">");
	    		for (int i=1; i<=31;i++){
        			if (i == c.get(Calendar.DAY_OF_MONTH)){
        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
        			}else{
        				out.println("<OPTION VALUE=" + i + ">" + i);
        			}
	    		}
    		out.println("</SELECT>");	
        	out.println("Year <SELECT NAME=\"SelectedStartYear\">");
	    		for (int i=1970; i<=2069;i++){
        			if (i == c.get(Calendar.YEAR)){
        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
        			}else{
        				out.println("<OPTION VALUE=" + i + ">" + i);
        			}
	    		}
    		out.println("</SELECT>");	
        	out.println("</TD></TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H4> Start Date: </H4></TD>");
        	
        	c = Calendar.getInstance();
        	c.setTime(tsEndDate);
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
        	out.println("</TD></TR>");
        	
        	out.println ("<TR><TD ALIGN=CENTER><H4> Duration: </H4></TD>");
        	out.println("<TD><INPUT TYPE=TEXT NAME=\"Duration\" SIZE=10 MAXLENGTH=10 VALUE=\"" + dDuration + "\"> Numbers ONLY</TD></TR>");
        	out.println("</TD></TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H4> Note </H4></TD>");
        	out.println("<TD><TEXTAREA NAME=\"Note\" ROWS=6 COLS=30>");
        	out.println(sNote);
	        out.println("</TEXTAREA></TD></TR></TABLE><BR>");
	        
	        out.println("<TABLE><TR><TD><INPUT TYPE=CHECKBOX NAME=SpecialAdjustmentCheck VALUE=1");
    		if (iSpacialAdj == 1){
    			out.println(" CHECKED");
    		}
			out.println(">Check here if it is a special adjustment entry*. </TD></TR></TABLE>");
			
        	
        	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
        	
        	out.println("</FORM>");
        	
        	//delete current record.
        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveRemove\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=\"" + request.getParameter("id") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete.");
        	out.println("</FORM>");
        	
        	out.println("</<BR><BR><BR><BR>*Special Adjustment Entries will not be included in any total time calculations in \"Manager's Review List\" or period end process.");
        	
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in EmployeeLeaveEdit class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
