package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;



/* Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeAuxiFieldEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Employee Auxiliary Field Info";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {

	    	if (Integer.parseInt(request.getParameter("FieldID")) < 0){
	    		//new Field
	        	out.println ("<BR><H2>Field Information: New Field</H2><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Field Name
	        	out.println ("<TR><TD><B>Field Name</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"FieldName\" SIZE=20 MAXLENGTH=30> Up to 30 alpha-numerics Only</TD></TR>");
	        	//Field Type
	        	out.println ("<TR><TD><B>Field Type</B></TD><TD>");
	        	out.println ("<SELECT NAME=\"FieldType\">");
	        	out.println ("<OPTION VALUE=" + Types.VARCHAR + " SELECTED>Text  (default to an empty string)" + 
	        				 "<OPTION VALUE=" + Types.DATE + ">Date  (default to 01-01-1900)" +
	        				 "<OPTION VALUE=" + Types.TIME + ">Time  (default to 00:00:00)" +
	        				 "<OPTION VALUE=" + Types.DOUBLE + ">Number  (default to 0)" +
	        				 "<OPTION VALUE=" + Types.INTEGER + ">Yes/No  (default to \"No\")" +
	        				 "</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldID\" VALUE=\"-1\">");
	        	
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	    	}else{
	    		//existing department, user can only change name 
	    		//get Department info
	    		String sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
		        //out.println (sSQL);
		        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)); 
		        int iCol = Integer.parseInt(request.getParameter("FieldID"));
		        ResultSetMetaData rsEAIMetaData = rs.getMetaData();
		        
		        //new Field
	        	out.println ("<BR><H2>Field Information: " + rsEAIMetaData.getColumnName(iCol) + "</H2><BR>");
	        	
	        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldSave\">");
	        	out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	//Field Name
	        	out.println ("<TR><TD><B>Field Name</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"FieldName\" SIZE=20 MAXLENGTH=30 VALUE=\"" + rsEAIMetaData.getColumnName(iCol) + "\"> Up to 30 alpha-numerics Only</TD></TR>");
	        	//Field Type
	        	out.println ("<TR><TD><B>Field Type</B></TD><TD>");
	        	
	        	switch (rsEAIMetaData.getColumnType(iCol)){
		    		case Types.VARCHAR:	out.println("Text");break;
		    		case Types.DATE:	out.println("Date"); break;
		    		case Types.TIME:	out.println("Time"); break;
		    		case Types.DOUBLE:  out.println("Number"); break;
		    		case Types.INTEGER:  out.println("Yes/No"); break;
		    		default: 			out.println("N/A");break;
	    		}
	        	out.println("</TD></TR>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldType\" VALUE=" + rsEAIMetaData.getColumnType(iCol) + ">");

	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldID\" VALUE=" + request.getParameter("FieldID") + ">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldNameOri\" VALUE=\"" + rsEAIMetaData.getColumnName(iCol) + "\">");
	        	out.println ("</Table>");
	        	
	        	out.println ("<BR>");
	        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
	        	out.println ("</FORM>");
	        	
	        	//Option to	delete current record.
	        	out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldRemove\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldID\" VALUE=\"" + request.getParameter("FieldID") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"FieldName\" VALUE=\"" + rsEAIMetaData.getColumnName(iCol) + "\">");
	        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
	        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this field.");
	        	out.println("</FORM>");
	        	rs.close();
		        
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