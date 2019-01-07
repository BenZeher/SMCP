package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

import TCSDataDefinition.*;

/* Servlet that insert In-Time records into the the time entry table.*/

public class DepartmentEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Department Info";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

/*
	    	  Departments;
				+------------------+-------------+------+-----+---------+-------+
				| Field            | Type        | Null | Key | Default | Extra |
				+------------------+-------------+------+-----+---------+-------+
				| iDeptID          | int(11)     |      | PRI | 0       |       |
				| sDeptDesc        | varchar(30) | YES  |     |         |       |
				| dDeptRate        | double      | YES  |     | 0       |       |
				| dLateGracePeriod | double      | YES  |     | 0       |       |
				+------------------+-------------+------+-----+---------+-------+
*/
	    	if (Integer.parseInt(request.getParameter("Department")) == 0){
	    		//new department
	        	out.println ("<BR><H2>Department Information: New Department</H2><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentInfoSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Department ID
	        	out.println ("<TR><TD><B>Department ID</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"DepartmentID\" SIZE=3 MAXLENGTH=3> Choose an integer between 1 - 999 only</TD></TR>");
	        	//Department Name
	        	out.println ("<TR><TD><B>Department Name</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"DepartmentName\" SIZE=20 MAXLENGTH=30> Up to 30 alpha-numerics</TD></TR>");
	        	//Department Rate
	        	out.println ("<TR><TD><B>Department Rate</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"DepartmentRate\" VALUE=0></TD></TR>");
	        	//Late Grace Period Length
	        	out.println ("<TR><TD><B>Late Grace Period Length</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"LateGracePeriodLength\" VALUE=0></TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"Did\" VALUE=\"0\">");
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing department, get Department info
		        String sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("Department")));
		        ResultSet rsDepartmentInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        if (rsDepartmentInfo.next()){
			        out.println ("<BR><H2>Department Information: " + rsDepartmentInfo.getString(Departments.sDeptDesc) + "</H2><BR>");
		        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentInfoSave\">");
		        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	//Department ID
		        	out.println ("<TR><TD><B>Department ID</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"DepartmentID\" VALUE=" + rsDepartmentInfo.getInt("iDeptID")+ "></TD></TR>");
		        	//Department Name
		        	out.println ("<TR><TD><B>Department Name</B></TD><TD>" + 
		        			      "<INPUT TYPE=TEXT NAME=\"DepartmentName\" SIZE=20 MAXLENGTH=30 VALUE=\"" + rsDepartmentInfo.getString("sDeptDesc")+ "\"> Up to 30 alpha-numerics</TD></TR>");
		        	//Department Rate
		        	out.println ("<TR><TD><B>Department Rate</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"DepartmentRate\" VALUE=" + rsDepartmentInfo.getDouble("dDeptRate")+ "></TD></TR>");
		        	//Late Grace Period Length
		        	out.println ("<TR><TD><B>Late Grace Period Length</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"LateGracePeriodLength\" VALUE=" + rsDepartmentInfo.getDouble("dLateGracePeriod")+ "></TD></TR>");
		        	out.println ("</Table>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Did\" VALUE=\"" + rsDepartmentInfo.getInt("iDeptID") + "\">");
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println ("</FORM>");
		        	//Option to	delete current record.
		        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentRemove\">");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"Department\" VALUE=\"" + rsDepartmentInfo.getInt("iDeptID") + "\">");
		        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
		        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this department.");
		        	out.println("</FORM>");
		        }else{
		        	//no department returned.
		        }
		        rsDepartmentInfo.close();
		        
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
	
	public boolean IsServiceType(int iMechType, int iServiceTypeID) throws SQLException{
		
		System.out.println("iMechType = " + iMechType + ", iServiceTypeID = " + iServiceTypeID);
		for (int i=24; i>iServiceTypeID; i--){
			if (iMechType >= Math.pow(2, i)){
				iMechType = iMechType - (int)Math.pow(2, i);
			}
		}
		
		if (iMechType >= Math.pow(2, iServiceTypeID)){
			return true;
		}else{
			return false;
		}
	}
}