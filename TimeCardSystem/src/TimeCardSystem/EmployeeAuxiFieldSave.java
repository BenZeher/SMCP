package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

/* Servlet that inserts In-Time records into the the time entry table.*/
public class EmployeeAuxiFieldSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {
	    	int iFid = Integer.parseInt(request.getParameter("FieldID"));
	    
	    	if (iFid < 0){
	    		//new field. add to table
		    	String sSQL = TimeCardSQLs.Get_Add_New_Column_SQL("EmployeeAuxiliaryInfo", //table name 
		    													  request.getParameter("FieldName"), //column name
		    													  Integer.parseInt(request.getParameter("FieldType")) //column type
		    													  );
		    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
		    		//saving successful;
    				out.println ("<BR>");
    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldList'>");
    	        	//out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed;
					out.println("<BR><BR>Saving Employee Auxiliary Field information failed!!<BR>");
					out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldList>Click here to return to employee auxiliary field list.</A>");
				}
		    	
	    	}else{
	    		//existing field, proceed to save changes.
	    		String sSQL = TimeCardSQLs.Get_Update_Column_SQL("EmployeeAuxiliaryInfo", //table name 
	    														 request.getParameter("FieldNameOri"), //old name
																 request.getParameter("FieldName"), //new name
																 Integer.parseInt(request.getParameter("FieldType")) //column type
																 );
	    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    			//saving successful;
    				out.println ("<BR>");
    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldList'>");
    	        	//out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed;
					out.println("<BR><BR>Updating Employee Auxiliary Field information failed!!<BR>");
					out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeAuxiFieldList>Click here to return to employee auxiliary field list.</A>");
				}
	    	}
	    } catch (Exception ex) {
	    	out.println("Error in EmployeeAuxiFieldSave: " + ex.getMessage());
	    }
	
	    out.println("</BODY></HTML>");
	}
}