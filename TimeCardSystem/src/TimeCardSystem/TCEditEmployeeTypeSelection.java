package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTableEmployeeTypes;

public class TCEditEmployeeTypeSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String CREATE_NEW_EMPLOYEETYPE_VALUE = "-1";
	public static final String BUTTON_EDIT_EMPLOYEETYPE_NAME = "EDIT";
	public static final String BUTTON_EDIT_EMPLOYEETYPE_LABEL = "Edit an existing employee type";
	public static final String BUTTON_ADD_NEW_NAME = "ADDNEW";
	public static final String BUTTON_ADD_NEW_LABEL = "Add a new employee type";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		HttpSession CurrentSession = request.getSession();
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String sTitle = "Time Card System";
	    String sSubtitle = "Edit Employee Types";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

    	out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing Employee Type - " + sWarning + ".</FONT></B><BR>");
    	}
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=GREEN>" + sStatus + "</FONT></B><BR>");
    	}
    	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCEditEmployeeTypesEdit\" METHOD = \"POST\">");
    	
    	out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");
    	
    	out.println("<BR>Select an Employee Type to edit:&nbsp;");
    	
		String sSQL = "SELECT * FROM " + TCSTableEmployeeTypes.TableName 
					+ " ORDER BY " + TCSTableEmployeeTypes.sName;
		ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

        //print the first line here. 
    	out.println ("<TD><SELECT NAME=\"" + TCSTableEmployeeTypes.lid + "\">" );
    	//out.println ("<OPTION VALUE=-1>----SELECT---- ");
    	
    	while (rs.next()){
        	out.println ("<OPTION VALUE=" + Long.toString(rs.getLong(TCSTableEmployeeTypes.lid)) + ">" 
        				+ Long.toString(rs.getLong(TCSTableEmployeeTypes.lid)) + " - " + rs.getString(TCSTableEmployeeTypes.sName) 
        				+ "</OPTION>");
    	}
        out.println ("</SELECT>");
        out.println ("</TD></TR>");
        
        rs.close();
		} catch (SQLException e) {
	        out.println ("Error getting Employee Type.");
		}
    	
    	out.println ("<BR><BR><INPUT TYPE=\"SUBMIT\" NAME=\"" + BUTTON_EDIT_EMPLOYEETYPE_NAME + "\" VALUE=\"" + BUTTON_EDIT_EMPLOYEETYPE_LABEL + "\">"
    		+ "&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" + BUTTON_ADD_NEW_NAME + "\" VALUE=\"" + BUTTON_ADD_NEW_LABEL + "\">"
    	);
    	out.println ("</FORM>");
		out.println("</BODY></HTML>");
	}
}