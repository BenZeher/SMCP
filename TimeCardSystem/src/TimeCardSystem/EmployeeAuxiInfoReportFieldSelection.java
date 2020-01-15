package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;

public class EmployeeAuxiInfoReportFieldSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Employee Auxiliary Information Report";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiInfoReport\">");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	//Select Field
        	out.println("<TR><TD ALIGN=CENTER><H3>Field(s) to include in the report</H3></TD>");
        	out.println("<TD>");
        	
        	ArrayList<String> alAuxiFields = new ArrayList<String>(0);
        	//add one entry for "all Types"
        	alAuxiFields.add("<INPUT TYPE=CHECKBOX NAME=!0!ALLTYPES VALUE=\"-1\"><B>All Fields</B>");
        	//add entries for employee identification
        	alAuxiFields.add("<INPUT TYPE=CHECKBOX NAME=!0!sEmployeeID VALUE=\"-1\"><B>Employee ID</B>");
        	alAuxiFields.add("<INPUT TYPE=CHECKBOX NAME=!0!sEmployeeFirstName VALUE=\"-1\"><B>Employee First Name</B>");
        	alAuxiFields.add("<INPUT TYPE=CHECKBOX NAME=!0!sEmployeeLastName VALUE=\"-1\"><B>Employee Last Name</B>");
        	
        	//add entries for auxi fields
        	String sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
	        ResultSet rsAuxiFields = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        ResultSetMetaData rsAuxiFieldsMetaData = rsAuxiFields.getMetaData();
        	for (int i=3;i<=rsAuxiFieldsMetaData.getColumnCount();i++){
        		alAuxiFields.add("<INPUT TYPE=CHECKBOX NAME=\"!0!" + rsAuxiFieldsMetaData.getColumnName(i) + "\" VALUE=0>" + rsAuxiFieldsMetaData.getColumnName(i));
    		}
        	
        	out.println(TimeCardUtilities.Build_HTML_Table(3, alAuxiFields, 0, false));
        	out.println("</SELECT></TD></TR>");
	         
        	//Select Order
        	out.println("<TR><TD ALIGN=CENTER><H3>Sorting and Grouping</H3></TD>");
        	out.println("<TD>");
        	
        	ArrayList<String> alLATypes = new ArrayList<String>(0);
        	//add one entry for "all Types"
        	out.println("<INPUT TYPE=CHECKBOX NAME=!1!Departments VALUE=\"-1\"><B>Group by Departments</B><BR>");
        	//add entries for employee identification
        	alLATypes.add("<INPUT TYPE=RADIO NAME=!1!EmployeeSort VALUE=\"sEmployeeID\" CHECKED><B>Sort by Employee ID</B>");
        	alLATypes.add("<INPUT TYPE=RADIO NAME=!1!EmployeeSort VALUE=\"sEmployeeFirstName\"><B>Sort by Employee First Name</B>");
        	alLATypes.add("<INPUT TYPE=RADIO NAME=!1!EmployeeSort VALUE=\"sEmployeeLastName\"><B>Sort by Employee Last Name</B>");
        	
        	
        	//add entries for auxi fields
        	sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
        	for (int i=3;i<=rsAuxiFieldsMetaData.getColumnCount();i++){
        		alLATypes.add("<INPUT TYPE=CHECKBOX NAME=\"!1!" + rsAuxiFieldsMetaData.getColumnName(i) + "\" VALUE=0>" + rsAuxiFieldsMetaData.getColumnName(i));
    		}
        	rsAuxiFields.close();
        	
        	out.println(TimeCardUtilities.Build_HTML_Table(3, alLATypes, 0, false));
        	out.println("</SELECT></TD></TR>");
        	
        	
        	
        	
        	rsAuxiFields.close();
	        out.println ("</TABLE><BR>");

        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Next----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579099059] Error in EmployeeAuxiInfoReportFieldSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}