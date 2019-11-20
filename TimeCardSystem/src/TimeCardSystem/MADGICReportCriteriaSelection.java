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
import TCSDataDefinition.Departments;
import TCSDataDefinition.Employees;

public class MADGICReportCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String UNCHECKALLDEPTSBUTTON = "UnCheckAllDepts";
	private static final String UNCHECKALLDEPTSLABEL = "UNCHECK All Departments";
	private static final String CHECKALLDEPTSBUTTON = "CheckAllDepts";
	private static final String CHECKALLDEPTSSLABEL = "CHECK All Departments";
	private static final String FORM_NAME = "MAINFORM";

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "MADGIC Report criteria";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(TimeCardUtilities.getDatePickerIncludeString(getServletContext()));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

	    try {
        	out.println ("<FORM NAME=\"" + FORM_NAME + "\" ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.MADGICReportGenerate\">");
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	
        	//MADGIC Periods:
        	out.println("<TR><TD ALIGN=CENTER><H3> Reporting period </H3></TD>");
        	
        	//just use today's date until otherwise suggested
        	out.println("<TD>");
        	out.println(TCMADGICEvent.createReportingPeriodListBox());
	        out.println ("</TD></TR>");
        	
        	//Show details?
        	out.println("<TR><TD ALIGN=CENTER><H3> Show details? </H3></TD>");
        	out.println("<TD><label><INPUT TYPE=CHECKBOX NAME=\"" + MADGICReportGenerate.PARAM_CHECKBOX_SHOW_DETAILS 
        		+ "\" VALUE=0>" + "&nbsp;Check this to display the individual time entries</label></TD>");
        	out.println("</TR>");
        	out.println("</TABLE>");
        	
        	out.println("<BR><B><I>Select by EITHER Department(s) OR Employee(s):</I></B><BR>");
        	
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	//Department selection, default to "All Department".
        	out.println("<TR>"
        		+ "<TD ALIGN=CENTER>"
        		+ "<H3>"
        		+ "<label>"
        		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_DEPT_OR_EMPLOYEE + "\""
        		+ " VALUE=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_DEPT + "\" checked >"
        		+ " Select by Department"
        		+ "</label>"
        		+"<br>"
        		+"<label><INPUT TYPE=CHECKBOX NAME= \"FIELD\" ID=\"FIELD\" onClick = \"selectDepartment(this);\">Field</label>"
        		+"<br>"
        		+"<label><INPUT TYPE=CHECKBOX NAME= \"OFFICE\" ID=\"OFFICE\" onClick = \"selectDepartment(this);\">Office</label>"
        		+ "<BR><BR>"
        		+ "<input type=\"button\" name=\"" + CHECKALLDEPTSBUTTON + "\" value=\"" + CHECKALLDEPTSSLABEL 
        			+ "\" onClick=\"checkall()\">"
        		+ "<BR><BR>"
        		+ "<input type=\"button\" name=\"" + UNCHECKALLDEPTSBUTTON + "\" value=\"" + UNCHECKALLDEPTSLABEL 
        			+ "\" onClick=\"uncheckall()\">"
        		+ "</H3></TD>");

        	//get recordset of departments, qualifying it by the current user's permissions:
        	String sSQL = "SELECT * FROM " + Departments.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName 
        		+ " WHERE (" 
        			+ "(" + Departments.iDeptID + " = " + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID + ")" 
        			+ " AND (" + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString() + "')"
        		+ ")"
        		+ " ORDER BY " + Departments.iDeptID;
        	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        	
        	//display available departments. 
        	out.println ("<TD>");
        	ArrayList<String> alDepartments = new ArrayList<String>(0);
        	//add one entry for "all department"
        	while (rs.next()){
        		String sDescription = rs.getString(Departments.sDeptDesc);
        		if(sDescription.contains("Field"))
        			alDepartments.add("<label><INPUT TYPE=CHECKBOX NAME=\"" + MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX + rs.getInt("iDeptID") + "\" CLASS = \"FIELD\" VALUE=0>" + sDescription + "</label>");
        		else if(sDescription.contains("Office"))
        			alDepartments.add("<label><INPUT TYPE=CHECKBOX NAME=\"" + MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX + rs.getInt("iDeptID") + "\"CLASS = \"OFFICE\" VALUE=0>" + sDescription + "</label>");
        		else
        			alDepartments.add("<label><INPUT TYPE=CHECKBOX NAME=\"" + MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX + rs.getInt("iDeptID") + "\"  VALUE=0>" + sDescription + "</label>");
        	}
        	
        	out.println(TimeCardUtilities.Build_HTML_Table(4, alDepartments, 0, false));
	        out.println ("</TD></TR>");
	        
        	//Employee selection, default to "All Employees".
        	out.println("<TR>"
        		+ "<TD ALIGN=CENTER>"
        		+ "<H3>"
        		+ "<label>"
        		+ "<INPUT TYPE=\"RADIO\" NAME=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_DEPT_OR_EMPLOYEE + "\""
        		+ " VALUE=\"" + MADGICReportGenerate.PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE + "\" >"
        		+ " Select by Employee"
        		+ "</label>"
        		+ "</H3></TD>");
        	
    		sSQL = "SELECT * FROM " + Employees.TableName + ", " + TCSDataDefinition.ManagerAccessControlDefinitions.TableName 
    			+ " WHERE (" 
    				+ Employees.TableName + "." + Employees.iDepartmentID + " = " 
    				+ TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.iDepartmentID 
    				+ " AND (" + TCSDataDefinition.ManagerAccessControlDefinitions.TableName + "." + TCSDataDefinition.ManagerAccessControlDefinitions.sManagerID + " = '" 
    					+ CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString() + "')"
    				+ " AND (" + Employees.TableName + "." + Employees.iActive + " = 1)"
    			+ ") ORDER BY " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
        	
        	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        
        	out.println ("<TD><SELECT NAME=\"" + MADGICReportGenerate.PARAM_SELECTED_EMPLOYEE + "\">" );
        	out.println ("<OPTION VALUE=\"" + MADGICReportGenerate.PARAM_ALL_EMPLOYEES_VALUE + "\">----All Employees---- ");
        	
        	while (rs.next()){
	        	out.println ("<OPTION VALUE=" + rs.getString(Employees.sEmployeeID) + ">" 
	        		+ rs.getString(Employees.sEmployeeID) + " - " 
	        		+ rs.getString(Employees.sEmployeeLastName) + ", " 
	        		+ rs.getString(Employees.sEmployeeFirstName));
        	}
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
	        out.println ("</TABLE><BR>");

	        out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + MADGICReportGenerate.PARAM_DISPLAY_REPORT + "\" VALUE=\"----Display----\">");
        	out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + MADGICReportGenerate.PARAM_EXPORT_REPORT + "\" VALUE=\"----Export----\">");
        	out.println("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><FONT COLOR=RED> Error [1442002275] in MADGICReportCriteriaSelection class - " + ex.getMessage() + ".");
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
	   			+ "        if (testName.substring(0, " + Integer.toString(MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX.length()) + ") == \"" + MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = true;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";
       	
       	s += "function selectDepartment(source){\n"
				+ "var items = document.getElementsByClassName(''+source.id+'');\n"
				+ "for(var i = 0; i < items.length; i++){\n"
				+ "if(items[i].type == \"checkbox\")\n"
				+ "    items[i].checked = source.checked;\n  "
				+ "}\n"
			+ "}\n";
       	
       	
       	
       	s += "function uncheckall(){\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        if (testName.substring(0, " + Integer.toString(MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX.length()) + ") == \"" + MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = false;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";
       	
       	s += "</script>\n";
       	return s;
	}
}