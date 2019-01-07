package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;

import java.sql.*;
import TCSDataDefinition.*;

public class EmployeeGeneralEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Edit Employee General Information";
	    String sCompanyName = "";
	    try{
	    	sCompanyName = CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
	    }catch(NullPointerException e){
	    	
	    }
	    String bar = "Time Card System - " + sCompanyName;
	    out.println(TimeCardUtilities.TCBarTitleSubBGColor(bar, title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {
	    	
	    	//get employee info
	        String sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("EmployeeID"));
	        ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        
	        //get department info
	        sSQL = TimeCardSQLs.Get_Department_List_SQL();
	        ResultSet rsDepartments = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	        
    		sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info(request.getParameter("EmployeeID"));
    		ResultSet rsEmployeeAuxiInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

    		boolean bNewEmployee;

	        if (rsEmployeeInfo.next()){
	        	bNewEmployee = false;
	        	//save the original EMployeeID
	        	String sFullName = rsEmployeeInfo.getString(Employees.sEmployeeFirstName) + " " + rsEmployeeInfo.getString(Employees.sEmployeeLastName);
		    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, rsEmployeeInfo.getString(Employees.sEmployeeID));
		    	CurrentSession.setAttribute(clsServletUtilities.SESSION_PARAM_FULL_USER_NAME, sFullName + " - " + sCompanyName);
		        out.println ("<BR><H2>Employee Information: " + sFullName + "</H2><BR>");
		     	
		        out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeGeneralSave\">");
	        	out.println ("<TR><TD COLSPAN=2><H3>Basic Information</H3></TD></TR>");
	        	//Employee ID
	        	out.println ("<TR><TD><B>Employee ID</B></TD><TD>" + 
		      		  	      "<B>" + rsEmployeeInfo.getString(Employees.sEmployeeID)+ "</B>" +
		      		  	  	  "<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sEmployeeID) + "\"></TD></TR>");
	        	//Employee Name
	        	out.println ("<TR><TD><B>Employee First Name</B></TD><TD>" + 
	        			      "<INPUT TYPE=TEXT NAME=\"EmployeeFirstName\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sEmployeeFirstName)+ "\" SIZE=20 MAXLENGTH=20> Up to 20 characters</TD></TR>");
	        	out.println ("<TR><TD><B>Employee Middle Name</B></TD><TD>" + 
			      		  	  "<INPUT TYPE=TEXT NAME=\"EmployeeMiddleName\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sEmployeeMiddleName)+ "\" SIZE=3 MAXLENGTH=3> Up to 3 characters</TD></TR>");
	        	out.println ("<TR><TD><B>Employee Last Name</B></TD><TD>" + 
		      		  	      "<INPUT TYPE=TEXT NAME=\"EmployeeLastName\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sEmployeeLastName)+ "\" SIZE=20 MAXLENGTH=25> Up to 25 characters</TD></TR>");
	        	//Department
	        	out.println ("<TR><TD><B>Employee Department</B></TD></TD><TD><SELECT NAME=\"EmployeeDepartment\">");
	        	while (rsDepartments.next()){
	        		if (rsDepartments.getInt(Departments.iDeptID) == rsEmployeeInfo.getInt(Employees.iDepartmentID)){
	        			out.println("<OPTION SELECTED VALUE=" + rsDepartments.getInt(Departments.iDeptID) + ">" + rsDepartments.getString(Departments.sDeptDesc));
	        		}else{
	        			out.println("<OPTION VALUE=" + rsDepartments.getInt(Departments.iDeptID) + ">" + rsDepartments.getString(Departments.sDeptDesc));
	        		}
	        	}
	    		out.println("</SELECT></TD></TR>");
	       
        		//contact information
        		out.println ("<TR><TD COLSPAN=2><A NAME=\"CONTACTINFO\"/><H3>Contact Information</H3></TD></TR>");
	        	//Extension
	        	out.println ("<TR><TD><B>Extension</B></TD><TD>" + 
      			      "<INPUT TYPE=TEXT NAME=\"EmployeeExtension\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sExtension)+ "\" SIZE=6 MAXLENGTH=6>Up to 6 digits</TD></TR>");
	        	//Office Phone
	        	String PhoneNumber = TimeCardUtilities.Filter_Number_String(rsEmployeeInfo.getString(Employees.sOfficePhone));
	        	out.println ("<TR><TD><B>Office Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneAreaCode\" VALUE=\"" + PhoneNumber.substring(0,3)+ "\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneSwitch\" VALUE=\"" + PhoneNumber.substring(3,6)+ "\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneEndUnit\" VALUE=\"" + PhoneNumber.substring(6,10)+ "\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Cell Phone
	        	PhoneNumber = TimeCardUtilities.Filter_Number_String(rsEmployeeInfo.getString(Employees.sCellPhone));
	        	out.println ("<TR><TD><B>Cell Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneAreaCode\" VALUE=\"" + PhoneNumber.substring(0,3)+ "\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneSwitch\" VALUE=\"" + PhoneNumber.substring(3,6)+ "\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneEndUnit\" VALUE=\"" + PhoneNumber.substring(6,10)+ "\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Home Phone
	        	PhoneNumber = TimeCardUtilities.Filter_Number_String(rsEmployeeInfo.getString(Employees.sHomePhone));
	        	out.println ("<TR><TD><B>Home Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneAreaCode\" VALUE=\"" + PhoneNumber.substring(0,3)+ "\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneSwitch\" VALUE=\"" + PhoneNumber.substring(3,6)+ "\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneEndUnit\" VALUE=\"" + PhoneNumber.substring(6,10)+ "\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Email
	        	out.println ("<TR><TD><B>Company Email</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"EmployeeEmail\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sEmail)+ "\" SIZE=40 MAXLENGTH=40>Up to 40 alpha-numerics</TD></TR>");
	        	
	        	//Address
	        	out.println ("<TR><TD><B>Address</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressLine1\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sAddressLine1)+ "\" SIZE=50 MAXLENGTH=50>Up to 50 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>&nbsp;</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressLine2\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sAddressLine2)+ "\" SIZE=50 MAXLENGTH=50>Up to 50 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>City</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressCity\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sAddressCity)+ "\" SIZE=30 MAXLENGTH=30>Up to 30 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>State</B></TD><TD><SELECT NAME=\"AddressState\">" + TimeCardUtilities.Print_50_States_Drop_Down_Options(rsEmployeeInfo.getString(Employees.sAddressState)) + "</SELECT></TD></TR>");
	          	out.println ("<TR><TD><B>Zip Code</B></TD><TD><INPUT TYPE=TEXT NAME=\"AddressZipCode\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sAddressZip)+ "\" SIZE=10 MAXLENGTH=10></TD></TR>");  			      					
	          	out.println ("<TR><TD><B>Country</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressCountry\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sAddressCountry)+ "\" SIZE=30 MAXLENGTH=30>Up to 30 alpha-numerics</TD></TR>");
	          	
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"NextelDirectCall\" VALUE=\"\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"IsPhoneService\" VALUE=0>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"IsPhoneInsured\" VALUE=0>");
	        	
		        //User defined info section
        		if (rsEmployeeAuxiInfo.next()){
	        		ResultSetMetaData metaEAInfo = rsEmployeeAuxiInfo.getMetaData();
	        		if (metaEAInfo.getColumnCount() > 2){
	        			out.println ("<TR><TD COLSPAN=2><A NAME=\"AUXIINFO\"/><H3>Auxiliary Information (User Defined)</H3></TD></TR>");
	        		}
	        		for (int i=3;i<=metaEAInfo.getColumnCount();i++){
	    	        	out.println ("<TR><TD VALIGN=TOP><B>" + metaEAInfo.getColumnName(i) + "</B></TD><TD>"); 
	    	        	switch (metaEAInfo.getColumnType(i)){
	    	        		case Types.VARCHAR:	//print out an input box for user to type a string
	    	        			out.println (clsCreateHTMLFormFields.TDTextBox(metaEAInfo.getColumnName(i),
					    	        									 rsEmployeeAuxiInfo.getString(i),
					    	        									 40,
					    	        									 metaEAInfo.getColumnDisplaySize(i),
	    	        													 ""));break;
	    	        		case Types.DATE:	//print out an input box for user to select a date
	    	        			out.println (clsCreateHTMLFormFields.TDDateSelection(metaEAInfo.getColumnName(i),
							    											   rsEmployeeAuxiInfo.getDate(i),
				    	        											   ""));break;
	    	        		case Types.TIME:	//print out an input box for user to select a time
	    	        			out.println (clsCreateHTMLFormFields.TDTimeSelection(metaEAInfo.getColumnName(i),
				    	        											   Timestamp.valueOf("1900-01-01 " + rsEmployeeAuxiInfo.getString(i)), 
				    	        											   ""));break;
	    	        		case Types.DOUBLE:	//print out an input box for user to input a double number
	    	        			out.println (clsCreateHTMLFormFields.TDDoubleBox(metaEAInfo.getColumnName(i),
																		   rsEmployeeAuxiInfo.getDouble(i),
																		   40,
																		   metaEAInfo.getColumnDisplaySize(i),
				    	        										   ""));break;
	    	        		case Types.INTEGER:	//print out an input box for user to select yes or no
	    	        			out.println (clsCreateHTMLFormFields.TDYesNo(metaEAInfo.getColumnName(i),
																	   rsEmployeeAuxiInfo.getInt(i),
			    	        										   ""));break;
	    	        	}
	    	        	out.println ("</TD></TR>");
	        		}
	        	}else{
	        		//no auxiliary data for this employee. 
	        		//Display default info
	    	        sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
	    	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	        ResultSetMetaData metaEAInfo = rs.getMetaData();
	    	        if (metaEAInfo.getColumnCount() > 2){
	        			out.println ("<TR><TD COLSPAN=2><A NAME=\"AUXIINFO\"/><H3>Auxiliary Information (User Defined)</H3></TD></TR>");
	        		}
	    	        for (int i=3;i<=metaEAInfo.getColumnCount();i++){
	    	        	out.println ("<TR><TD VALIGN=TOP><B>" + metaEAInfo.getColumnName(i) + "</B></TD><TD>");
	    	        	switch (metaEAInfo.getColumnType(i)){
	    	        		case Types.VARCHAR:	//print out an input box for user to type a string
	    	        			out.println(clsCreateHTMLFormFields.TDTextBox(metaEAInfo.getColumnName(i),
				    	        										"",
				    	        										40,
				    	        										metaEAInfo.getColumnDisplaySize(i),
				    	        										""));break;
	    	        		case Types.DATE:	//print out an input box for user to type a string
	    	        			out.println(clsCreateHTMLFormFields.TDDateSelection(metaEAInfo.getColumnName(i),
							    										   	  new Date(System.currentTimeMillis()),
				    	        										   	  ""));break;
	    	        		case Types.TIME:	//print out an input box for user to type a string
	    	        			out.println(clsCreateHTMLFormFields.TDTimeSelection(metaEAInfo.getColumnName(i),
				    	        											  new Timestamp(System.currentTimeMillis()), 
				    	        											  ""));break;
	    	        		case Types.DOUBLE:	//print out an input box for user to type a string
	    	        			out.println(clsCreateHTMLFormFields.TDDoubleBox(metaEAInfo.getColumnName(i),
																		  0,
																		  40,
																		  metaEAInfo.getColumnDisplaySize(i),
				    	        										  ""));break;
	    	        		case Types.INTEGER:	//print out an input box for user to select yes or no
	    	        			out.println (clsCreateHTMLFormFields.TDYesNo(metaEAInfo.getColumnName(i),
																	   0,
			    	        										   ""));break;
	    	        	}
	    	        	out.println ("</TD></TR>");
	        		}
	    	        rs.close();
	        	}
	        	out.println ("</TABLE>");
	        	
	        }else{	//create new employee
	        	
	        	bNewEmployee = true;
	        	//set the ID to be an invalid id.
		    	CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, "a0b0c0");
        		
		        out.println ("<BR><H2>Employee Information: New Employee" + "</H2><BR>");

		        out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
	        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeGeneralSave\">");
	        	out.println ("<TR><TD COLSPAN=2><H3>Basic Information</H3></TD></TR>");
	        	//Employee ID
	        	out.println ("<TR><TD><B>Employee ID</B></TD><TD>" + 
	        			clsCreateHTMLFormFields.TDTextBox("EmployeeID", "", 20, 9, "Up to 9 alpha-numerics") + 
        					 "</TD></TR>");
	        	//Employee Name
	        	out.println ("<TR><TD><B>Employee First Name</B></TD><TD>" +
	        			clsCreateHTMLFormFields.TDTextBox("EmployeeFirstName", "", 20, 20, "Up to 20 characters") +
	        			     "</TD></TR>");
	        	out.println ("<TR><TD><B>Employee Middle Name</B></TD><TD>" + 
	        			clsCreateHTMLFormFields.TDTextBox("EmployeeMiddleName", "", 3, 3, "Up to 3 characters") +
			      		  	 "</TD></TR>");
	        	out.println ("<TR><TD><B>Employee Last Name</B></TD><TD>" + 
	        			clsCreateHTMLFormFields.TDTextBox("EmployeeLastName", "", 20, 20, "Up to 20 characters") +
		     		  	 	 "</TD></TR>");
	        	//Department
	        	out.println ("<TR><TD><B>Employee Department</B></TD></TD><TD><SELECT NAME=\"EmployeeDepartment\">");
	        	while (rsDepartments.next()){
	        		out.println("<OPTION VALUE=" + rsDepartments.getInt(Departments.iDeptID) + ">" + rsDepartments.getString(Departments.sDeptDesc));
	        	}
	    		out.println("</SELECT></TD></TR>");
        		
        		//Contact information section
        		out.println ("<TR><TD COLSPAN=2><A NAME=\"CONTACTINFO\"/><H3>Contact Information</H3></TD></TR>");

        		//Extension
	        	out.println ("<TR><TD><B>Extension</B></TD><TD>" + 
        					 "<INPUT TYPE=TEXT NAME=\"EmployeeExtension\" SIZE=6 MAXLENGTH=6> Up to 6 digits</TD></TR>");
	        	
	        	//Office Phone
	        	out.println ("<TR><TD><B>Office Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneAreaCode\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneSwitch\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeOfficePhoneEndUnit\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Cell Phone
	        	out.println ("<TR><TD><B>Cell Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneAreaCode\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneSwitch\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeCellPhoneEndUnit\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Home Phone
	        	out.println ("<TR><TD><B>Home Phone</B></TD><TD>" + 
	      			      		"(<INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneAreaCode\" SIZE=3 MAXLENGTH=3>)" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneSwitch\" SIZE=3 MAXLENGTH=3>-" +
	      			      		" <INPUT TYPE=TEXT NAME=\"EmployeeHomePhoneEndUnit\" SIZE=4 MAXLENGTH=4>" +
	      			      	"</TD></TR>");
	        	//Email
	        	out.println ("<TR><TD><B>Company Email</B></TD><TD>" + 
	      			      	 "<INPUT TYPE=TEXT NAME=\"EmployeeEmail\" SIZE=40 MAXLENGTH=40> Up to 40 alpha-numerics</TD>");

	        	//Address
	        	out.println ("<TR><TD><B>Address</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressLine1\" SIZE=50 MAXLENGTH=50>Up to 50 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>&nbsp;</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressLine2\" SIZE=50 MAXLENGTH=50>Up to 50 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>City</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"AddressCity\" SIZE=30 MAXLENGTH=30>Up to 30 alpha-numerics</TD></TR>");
	          	out.println ("<TR><TD><B>State</B></TD><TD><SELECT NAME=\"AddressState\">" + TimeCardUtilities.Print_50_States_Drop_Down_Options() + "</SELECT></TD></TR>");
	          	out.println ("<TR><TD><B>ZipCode</B></TD><TD><INPUT TYPE=TEXT NAME=\"AddressZipCode\" SIZE=10 MAXLENGTH=10></TD></TR>");  			      					
	          	out.println ("<TR><TD><B>Country</B></TD><TD><INPUT TYPE=TEXT NAME=\"AddressCountry\" VALUE=\"United States\" SIZE=30 MAXLENGTH=30>Up to 30 alpha-numerics</TD></TR>");
	          	
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"NextelDirectCall\" VALUE=\"\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"IsPhoneService\" VALUE=0>");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"IsPhoneInsured\" VALUE=0>");
        		
        		//no auxiliary data for this employee. 
        		//Display default info
    	        sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
    	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
    	        ResultSetMetaData metaEAInfo = rs.getMetaData();
    	        if (metaEAInfo.getColumnCount() > 2){
        			out.println ("<TR><TD COLSPAN=2><A NAME=\"AUXIINFO\"/><H3>Auxiliary Information (User Defined)</H3></TD></TR>");
        		}
    	        for (int i=3;i<=metaEAInfo.getColumnCount();i++){
    	        	out.println ("<TR><TD VALIGN=TOP><B>" + metaEAInfo.getColumnName(i) + "</B></TD><TD>");
    	        	switch (metaEAInfo.getColumnType(i)){
    	        		case Types.VARCHAR:	//print out an input box for user to type a string
    	        			out.println(clsCreateHTMLFormFields.TDTextBox(metaEAInfo.getColumnName(i),
			    	        										"",
			    	        										40,
			    	        										metaEAInfo.getColumnDisplaySize(i),
			    	        										""));break;
    	        		case Types.DATE:	//print out an input box for user to type a string
    	        			out.println(clsCreateHTMLFormFields.TDDateSelection(metaEAInfo.getColumnName(i),
						    										   	  new Date(System.currentTimeMillis()),
			    	        										   	  ""));break;
    	        		case Types.TIME:	//print out an input box for user to type a string
    	        			out.println(clsCreateHTMLFormFields.TDTimeSelection(metaEAInfo.getColumnName(i),
			    	        											  new Timestamp(System.currentTimeMillis()), 
			    	        											  ""));break;
    	        		case Types.DOUBLE:	//print out an input box for user to type a string
    	        			out.println(clsCreateHTMLFormFields.TDDoubleBox(metaEAInfo.getColumnName(i),
																	  0,
																	  40,
																	  metaEAInfo.getColumnDisplaySize(i),
			    	        										  ""));break;
    	        		case Types.INTEGER:	//print out an input box for user to select yes or no
    	        			out.println (clsCreateHTMLFormFields.TDYesNo(metaEAInfo.getColumnName(i),
																   0,
		    	        										   ""));break;
    	        	}
    	        	out.println ("</TD></TR>");
        		}
    	        rs.close();
        		out.println ("</TABLE>");
	        }
        	
        	out.println ("<BR>");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												AccessControlFunctionList.EditEmployeeGeneralInformation)){
        		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
        	}
        	out.println ("</FORM>");
	        
        	//ltong 2008-12-01 May need some control in for removing.
        	//delete current record.
        	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												AccessControlFunctionList.EditEmployeeGeneralInformation) &&
				!bNewEmployee){
	        	out.println("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeRemove\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + request.getParameter("EmployeeID") + "\">");
	        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
	        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete----\">");
	        	out.println("<INPUT TYPE=CHECKBOX NAME=DoubleCheck VALUE=1>Check here if you want to delete this employee.");
	        	out.println("</FORM>");
        	}
        	
        	rsEmployeeInfo.close();
        	rsEmployeeAuxiInfo.close();
        	rsDepartments.close();
        	
	    } catch (SQLException ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	        out.println("SQLState: " + ex.getSQLState() + "<BR>");
	        out.println("SQL: " + ex.getErrorCode() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public boolean IsServiceType(int iMechType, int iServiceTypeID) throws SQLException{
		
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