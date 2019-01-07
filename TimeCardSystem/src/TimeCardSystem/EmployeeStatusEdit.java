package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeStatusEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Employee Status";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

	    	/*
	    	+--------------+-------------+------+-----+---------+-------+
			| Field        | Type        | Null | Key | Default | Extra |
			+--------------+-------------+------+-----+---------+-------+
			| iStatusID    | int(11)     |      | PRI | 0       |       |
			| sStatusTitle | varchar(20) | YES  |     |         |       |
			| sStatusDesc  | text        | YES  |     | NULL    |       |
			+--------------+-------------+------+-----+---------+-------+	    	
	    	*/
	    	if (Integer.parseInt(request.getParameter("Status")) == 0){
	    		//new department
	        	out.println ("<BR><H2>Employee Status: New Status</H2><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeStatusSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Time Entry Type ID
	        	out.println ("<TR><TD><B>Status ID</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"StatusID\" SIZE=3 MAXLENGTH=3>Choose an integer between 1 - 16 only</TD></TR>");
	        	//Time Entry Type Title
	        	out.println ("<TR><TD><B>Status Title</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"StatusTitle\" SIZE=3 MAXLENGTH=3>Up to 3 alpha-numerics</TD></TR>");
	        	//Time Entry Type Description
	        	out.println ("<TR><TD><B>Status Description</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"StatusDesc\" SIZE=20 MAXLENGTH=150>Up to 150 alpha-numerics</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Sid\" VALUE=\"0\">");
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing type, get Time entry type info
		        String sSQL = TimeCardSQLs.Get_Employee_Status_Info_SQL(request.getParameter("Status"));
		        ResultSet rsTypeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        if (rsTypeInfo.next()){
			        out.println ("<BR><H2>Employee Status Information: " + rsTypeInfo.getString("sStatusTitle") + "</H2><BR>");
		        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeStatusSave\">");
		        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	//Status Type ID
		        	out.println ("<TR><TD><B>Status ID</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"StatusID\" SIZE=3 MAXLENGTH=3 VALUE=" + rsTypeInfo.getInt("iStatusID")+ "></TD></TR>");
		        	//Status Type Title
		        	out.println ("<TR><TD><B>Status Title</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"StatusTitle\" SIZE=3 MAXLENGTH=3 VALUE=\"" + rsTypeInfo.getString("sStatusTitle")+ "\">Up to 3 alpha-numerics</TD></TR>");
		        	//Status Description
		        	out.println ("<TR><TD><B>Status Description</B></TD><TD>" + 
		        			      "<INPUT TYPE=TEXT NAME=\"StatusDesc\" SIZE=20 MAXLENGTH=150 VALUE=\"" + rsTypeInfo.getString("sStatusDesc")+ "\">Up to 150 alpha-numerics</TD></TR>");
		        	out.println ("</Table>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Sid\" VALUE=\"" + rsTypeInfo.getInt("iStatusID") + "\">");
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println ("</FORM>");
		        	//Option to	delete current record.
		        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeStatusRemove\">");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Status\" VALUE=\"" + rsTypeInfo.getInt("iStatusID") + "\">");
		        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
		        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this status.");
		        	out.println("</FORM>");
		        }else{
		        	//no department returned.
		        }
		        rsTypeInfo.close();
		        
	    	}
	    } catch (SQLException ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	        out.println("SQLState: " + ex.getSQLState() + "<BR>");
	        out.println("SQL: " + ex.getErrorCode() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}