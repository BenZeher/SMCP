package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTableEmployeeTypes;

public class MilestonesReportCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String UNCHECKALLTYPESBUTTON = "UnCheckAllType";
	private static final String UNCHECKALLTYPESABEL = "UNCHECK All Employee Types";
	private static final String CHECKALLTYPESBUTTON = "CheckAllTypes";
	private static final String CHECKALLTYPESSLABEL = "CHECK All Employee Types";
	private static final String FORM_NAME = "MAINFORM";

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Milestones Report criterias";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(TimeCardUtilities.getDatePickerIncludeString(getServletContext()));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A>&nbsp;");

	    try {
        	out.println ("<FORM NAME=\"" + FORM_NAME + "\" ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.MilestonesReportGenerate\">");
           	      	
        	/*out.println("<TABLE CELLPADDING=10 BORDER=1>");	
 			//Show details?
        	out.println("<TR><TD ALIGN=CENTER><H3> Show details? </H3></TD>");
        	out.println("<TD><label><INPUT TYPE=CHECKBOX NAME=\"" + MilestonesReportGenerate.PARAM_CHECKBOX_SHOW_DETAILS 
        		+ "\" VALUE=0>" + "&nbsp;Check this to display the more detail</label></TD>");
        	out.println("</TR>");
        	out.println("</TABLE>");
        	 */
        	out.println("<BR><B><I>Select by EITHER Employee Type(s) OR Employee:</I></B><BR>");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	//Employee type selection, default to "All Employee Types".
        	out.println("<TR>"
        		+ "<TD ALIGN=CENTER>"
        		+ "<H3>"
        		+ "<label>"
        		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_TYPE_OR_EMPLOYEE + "\""
        		+ " VALUE=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_TYPE + "\" checked >"
        		+ " Select by Employee Type"
        		+ "</label>"
        		+ "<BR><BR>"
        		+ "<input type=\"button\" name=\"" + CHECKALLTYPESBUTTON + "\" value=\"" + CHECKALLTYPESSLABEL 
        			+ "\" onClick=\"checkall()\">"
        		+ "<BR><BR>"
        		+ "<input type=\"button\" name=\"" + UNCHECKALLTYPESBUTTON + "\" value=\"" + UNCHECKALLTYPESABEL 
        			+ "\" onClick=\"uncheckall()\">"
        		+ "</H3></TD>");

        	//get recordset of employee types.
        	String sSQL = "SELECT * FROM " + TCSTableEmployeeTypes.TableName;

        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	
        	//display ALL employee types. 
        	out.println ("<TD>");
        	ArrayList<String> arrEmployeetypes = new ArrayList<String>(0);
        	//add one entry for "all department"
        	while (rs.next()){
        		arrEmployeetypes.add("<label><INPUT TYPE=CHECKBOX NAME=\"" + MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX + rs.getInt(TCSTableEmployeeTypes.lid) 
        								+ "\" VALUE=\"" + rs.getInt(TCSTableEmployeeTypes.lid) + "\">" + rs.getString(TCSTableEmployeeTypes.sName) 
        						   + "</label>");
        	}
        	out.println(TimeCardUtilities.Build_HTML_Table(4, arrEmployeetypes, 0, false));
	        out.println ("</TD></TR>");
	        
        	//Employee selection, default to "All Employees".
        	out.println("<TR>"
        		+ "<TD ALIGN=CENTER>"
        		+ "<H3>"
        		+ "<label>"
        		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_TYPE_OR_EMPLOYEE + "\""
        		+ " VALUE=\"" + MilestonesReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE + "\" >"
        		+ " Select by Employee"
        		+ "</label>"
        		+ "</H3></TD>");
        	
    		sSQL = "SELECT * FROM " + Employees.TableName
    			+ " WHERE (" 
    				+ "(" + Employees.TableName + "." + Employees.iActive + " = 1)"
    			+ ") ORDER BY " + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
        	
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"" + MilestonesReportGenerate.PARAM_SELECTED_EMPLOYEE + "\">" );
        	out.println ("<OPTION VALUE=\"" + MilestonesReportGenerate.PARAM_ALL_EMPLOYEES_VALUE + "\">----All Employees---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString(Employees.sEmployeeID) + ">" 
	        		+ rs.getString(Employees.sEmployeeID) + " - " 
	        		+ rs.getString(Employees.sEmployeeLastName) + ", " 
	        		+ rs.getString(Employees.sEmployeeFirstName));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
	        out.println ("</TABLE><BR>");

	        out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + MilestonesReportGenerate.PARAM_DISPLAY_REPORT + "\" VALUE=\"----Display----\">");
        	out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + MilestonesReportGenerate.PARAM_ALLOW_CLEARING + "\" VALUE=\"Y\">");
        	out.println("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><FONT COLOR=RED> Error [1442882275] in MilestonesReportCriteriaSelection class - " + ex.getMessage() + ".");
	    }
 
	    out.println(createJavascriptFunctions());
	    out.println("</BODY></HTML>");
	}
	
	private String createJavascriptFunctions(){
		String s = "";
       	s += "<script LANGUAGE=\"JavaScript\">";
       	
       	s += "function checkall(){\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        if (testName.substring(0, " + Integer.toString(MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX.length()) + ") == \"" + MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = true;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";

       	s += "function uncheckall(){\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        if (testName.substring(0, " + Integer.toString(MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX.length()) + ") == \"" + MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = false;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";
       	
       	s += "</script>\n";
       	return s;
	}
}