package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class PayTypeEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Pay Type Info";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

	    	/*
	    	  Departments;
				+------------+-------------+------+-----+---------+-------+
				| Field      | Type        | Null | Key | Default | Extra |
				+------------+-------------+------+-----+---------+-------+
				| iTypeID    | int(11)     |      | PRI | 0       |       |
				| sTypeDesc  | text        | YES  |     | NULL    |       |
				| sTypeTitle | varchar(20) | YES  |     |         |       |
				+------------+-------------+------+-----+---------+-------+
 			*/
	    	if (Integer.parseInt(request.getParameter("Type")) == 0){
	    		//new department
	        	out.println ("<BR><H2>Pay Type Information: New Type</H2><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Pay Type ID
	        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3>Choose an integer between 1 - 16 only</TD></TR>");
	        	//Pay Type Title
	        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3>Up to 3 alpha-numerics</TD></TR>");
	        	//Pay Type Description
	        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=20 MAXLENGTH=150>Up to 150 alpha-numerics</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"0\">");
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing type, get pay type info
		        String sSQL = TimeCardSQLs.Get_Pay_Type_Info_SQL(request.getParameter("Type"));
		        ResultSet rsTypeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        if (rsTypeInfo.next()){
			        out.println ("<BR><H2>Pay Type Information: " + rsTypeInfo.getString("sTypeTitle") + "</H2><BR>");
		        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeSave\">");
		        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	//Pay Type ID
		        	out.println ("<TR><TD><B>Type ID</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"TypeID\" SIZE=3 MAXLENGTH=3 VALUE=" + rsTypeInfo.getInt("iTypeID")+ "></TD></TR>");
		        	//Pay Type Title
		        	out.println ("<TR><TD><B>Type Title</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"TypeTitle\" SIZE=3 MAXLENGTH=3 VALUE=\"" + rsTypeInfo.getString("sTypeTitle")+ "\">Up to 3 alpha-numerics</TD></TR>");
		        	//Pay Type Description
		        	out.println ("<TR><TD><B>Type Description</B></TD><TD>" + 
		        			      "<INPUT TYPE=TEXT NAME=\"TypeDesc\" SIZE=20 MAXLENGTH=150 VALUE=\"" + rsTypeInfo.getString("sTypeDesc")+ "\">Up to 150 alpha-numerics</TD></TR>");
		        	out.println ("</Table>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Tid\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println ("</FORM>");
		        	//Option to	delete current record.
		        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeRemove\">");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Type\" VALUE=\"" + rsTypeInfo.getInt("iTypeID") + "\">");
		        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
		        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this pay type.");
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