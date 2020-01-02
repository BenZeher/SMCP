package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class LeaveAdjustmentTypeEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Leave Type Info";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	String sSQL = "";
	    try {
	    	/*
				+--------------------------+-------------+------+-----+--------------+-------+
				| Field                    | Type        | Null | Key | Default      | Extra |
				+--------------------------+-------------+------+-----+--------------+-------+
				| iTypeID                  | int(11)     |      | PRI | 0            |       |
				| sTypeTitle               | varchar(20) | YES  |     |              |       |
				| sTypeDesc                | text        | YES  |     | NULL         |       |
				| dtEffectiveDate          | date        | YES  |     | 0000-00-00   |       |
				| iEligibleEmployeePayType | int(11)     | YES  |     | 0            |       |
				| iEligibleEmploeeStatus   | int(11)     | YES  |     | 0            |       |
				| dMinimumHourWorked       | double      | YES  |     | 0            |       |
				| sAwardPeriod             | varchar(12) | YES  |     | calendaryear |       |
				| dAwardType               | double      | YES  |     | 0            |       |
				| iCarriedOver             | int(1)      | YES  |     | 0            |       |
				| dMaximumHourAvailable    | double      | YES  |     | 0            |       |
				+--------------------------+-------------+------+-----+--------------+-------+
 			*/
	    	if (Integer.parseInt(request.getParameter("Type")) == 0){
	    		//new Leave Adjustment Type
	        	out.println ("<BR><FONT SIZE=4>Leave Type Information: <B>New Type</B></FONT><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Leave Adjustment Type ID
	        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3> Choose an integer between 1 - 16 only</TD></TR>");
	        	//Leave Adjustment Type Title
	        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3> Up to 3 alpha-numerics</TD></TR>");
	        	//Leave Adjustment Type Description
	        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=40 MAXLENGTH=150> Up to 150 alpha-numerics</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"0\">");
	        	
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing type, get Leave Adjustment type info
		        sSQL = TimeCardSQLs.Get_Leave_Adjustment_Type_Info_SQL(request.getParameter("Type"));
		        ResultSet rsTypeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        if (rsTypeInfo.next()){
			        out.println ("<BR><FONT SIZE=4>Leave Type Information: <B>" + rsTypeInfo.getString("sTypeTitle") + " - " + rsTypeInfo.getString("sTypeDesc") + "</B></FONT><BR>");
		        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeSave\">");
		        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	//Leave Adjustment Type ID, not editable.
		        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3 VALUE=" + rsTypeInfo.getInt("iTypeID")+ "></TD></TR>");
		        	//Leave Adjustment Type Title
		        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3 VALUE=\"" + rsTypeInfo.getString("sTypeTitle")+ "\">Up to 3 alpha-numerics</TD></TR>");
		        	//Leave Adjustment Type Description
		        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
		        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=40 MAXLENGTH=150 VALUE=\"" + rsTypeInfo.getString("sTypeDesc")+ "\">Up to 150 alpha-numerics</TD></TR>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");
					        
		        	out.println ("</Table>");
		        	
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println ("</FORM>");
		        	//Option to	delete current record.
		        	//hardwire "vacation" in the program.
		        	if (Integer.parseInt(request.getParameter("Type")) != 1){
			        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeRemove\">");
			        	out.println("<INPUT TYPE=HIDDEN NAME=\"Type\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");
			        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
			        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this pay type.");
			        	out.println("</FORM>");
		        	}
		        }else{
		        	//no leave adjustment type returned.
		        }
		        rsTypeInfo.close();
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error [1551905341] - reading Leave Adjustment Types with SQL = '" + sSQL + "' - " + ex.getMessage());
	    }
	
	    out.println("</BODY></HTML>");
	}
}