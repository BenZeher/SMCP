package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import TCSDataDefinition.*;

public class EmployeeConfidentialEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    
	    String title = "Time Card System";
	    String subtitle = "Edit Employee Confidential Info";
	    String bar = "";
	    try {
			bar = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>Error [1443448642] - could not read current session attribute for company - " + e.getMessage() + "</B></FONT><BR>");
		}
	    out.println(TimeCardUtilities.TCBarTitleSubBGColor(bar, title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
	    try {
	    	//check for user previlidge again before displaying.

			if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												AccessControlFunctionList.EditEmployeeConfidentialInformation) || 
				TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
												AccessControlFunctionList.ViewEmployeeConfidentialInformation)){
		 
				//get employee info
		        String sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("EmployeeID"));
		        ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        
		        //get department info
		        sSQL = TimeCardSQLs.Get_Department_List_SQL();
		        ResultSet rsDepartments = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        /*
	    		sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info(request.getParameter("Employee"));
	    		ResultSet rsEmployeeAuxiInfo = TimeCardUtilities.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        */
	
		        if (rsEmployeeInfo.next()){
		        	//save the original EmplopyeeID
			        out.println ("<BR><H2>Employee Confidential Information: " + rsEmployeeInfo.getString(Employees.sEmployeeFirstName) + " " + rsEmployeeInfo.getString(Employees.sEmployeeLastName) + "</H2><BR>");
			     	
			        out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
		        	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeConfidentialSave\">");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"EmployeeID\" VALUE=\"" + request.getParameter("EmployeeID") + "\">");
	
	        		//Employee start time
		        	out.println ("<TR><TD><B>Employee Start Time</B></TD>"); 
		        	String sStartTime = rsEmployeeInfo.getString(Employees.tStartTime);
		        	int iMinute = Integer.parseInt(sStartTime.substring(3, 5));
		        	int iAMPM = Integer.parseInt(sStartTime.substring(0, 2)) / 12;
		        	int iHour = Integer.parseInt(sStartTime.substring(0, 2)) % 12;
		        	if (iHour == 0 && iAMPM == 1){
		        		iHour = 12;
		        	}
		        	out.println("<TD>");
	        		out.println("Hour <SELECT NAME=\"SelectedHour\">");
		        		for (int i=0; i<=12;i++){
		        			if (i == iHour){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
		        		}
		        	out.println("</SELECT>");
		        	out.println("Minute <SELECT NAME=\"SelectedMinute\">");
			    		for (int i=0; i<=59;i++){
		        			if (i == iMinute){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("AM/PM <SELECT NAME=\"SelectedAMPM\">");
			    		for (int i=Calendar.AM; i<=Calendar.PM;i++){
		        			if (i == iAMPM){
		        				if (i == Calendar.AM){
		        					out.println("<OPTION SELECTED VALUE=" + Calendar.AM + ">" + "AM");
		        				}else{
		        					out.println("<OPTION SELECTED VALUE=" + Calendar.PM + ">" + "PM");
		        				}		
		        			}else{
		        				if (i == Calendar.AM){
		        					out.println("<OPTION VALUE=" + Calendar.AM + ">" + "AM");
		        				}else{
		        					out.println("<OPTION VALUE=" + Calendar.PM + ">" + "PM");
		        				}
		        			}
			    		}
		    		out.println("</SELECT></TD></TR>");	
		        	
	        		//Pin number
		        	out.println ("<TR><TD><B>Employee Pin Number</B></TD><TD>" + 
				      		  	  "<INPUT TYPE=TEXT NAME=\"EmployeePinNumber\" VALUE=\"" + rsEmployeeInfo.getString(Employees.sPinNumber)+ "\" SIZE=6 MAXLENGTH=6> Up to 6 alpha-numerics</TD></TR>");
		        	//SSN
		        	//String sDECRYPT = rsEmployeeInfo.getString(Employees.sSSN);
		        	out.println ("<TR><TD><B>Employee SSN</B></TD><TD>" + 
			      		  	      "<INPUT TYPE=TEXT NAME=\"EmployeeSSN\" VALUE=\"" + rsEmployeeInfo.getString("decryptedSSN") + "\" SIZE=9 MAXLENGTH=9> Exact 9 digits</TD></TR>"); 
		    		//SSNMARKSCO
		        	//Birthday
		        	out.println("<TR><TD><B>Birthday</B></TD>");
		        	Calendar c = Calendar.getInstance();
		        	if (rsEmployeeInfo.getString(Employees.datBirthday).compareTo("0000-00-00") == 0){
		        		c.setTimeInMillis(System.currentTimeMillis());
		        	}else{
		        		c.setTime(rsEmployeeInfo.getDate(Employees.datBirthday));
		        	}
		        	out.println("<TD>");
		        	
	        		out.println("Month <SELECT NAME=\"SelectedBirthMonth\">");
		        		for (int i=1; i<=12;i++){
		        			if (i == c.get(Calendar.MONTH) + 1){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
		        		}
		        	out.println("</SELECT>");
			        	
		        	out.println("Day <SELECT NAME=\"SelectedBirthDay\">");
			    		for (int i=1; i<=31;i++){
		        			if (i == c.get(Calendar.DAY_OF_MONTH)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("Year <SELECT NAME=\"SelectedBirthYear\">");
			    		for (int i=1901; i<=2069;i++){
		        			if (i == c.get(Calendar.YEAR)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("</TD></TR>");
		        	
		        	//Hired Date
		        	out.println("<TR><TD><B>Hired Date</B></TD>");
		        	if (rsEmployeeInfo.getString(Employees.datHiredDate).compareTo("0000-00-00") == 0){
		        		c.setTimeInMillis(System.currentTimeMillis());
		        	}else{
		        		c.setTime(rsEmployeeInfo.getDate(Employees.datHiredDate));
		        	}
		        	out.println("<TD>");
		        	
	        		out.println("Month <SELECT NAME=\"SelectedHiredMonth\">");
		        		for (int i=1; i<=12;i++){
		        			if (i == c.get(Calendar.MONTH) + 1){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
		        		}
		        	out.println("</SELECT>");
			        	
		        	out.println("Day <SELECT NAME=\"SelectedHiredDay\">");
			    		for (int i=1; i<=31;i++){
		        			if (i == c.get(Calendar.DAY_OF_MONTH)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("Year <SELECT NAME=\"SelectedHiredYear\">");
			    		for (int i=1950; i<=2069;i++){
		        			if (i == c.get(Calendar.YEAR)){
		        				out.println("<OPTION SELECTED VALUE=" + i + ">" + i);
		        			}else{
		        				out.println("<OPTION VALUE=" + i + ">" + i);
		        			}
			    		}
		    		out.println("</SELECT>");	
		        	out.println("</TD></TR>");
		        	
		        	
		        	//Pay Type
		        	out.println("<TR><TD><B>Employee Pay Type</B></TD>");
		        	//get recordset of employee types
		        	sSQL = TimeCardSQLs.Get_Pay_Type_Info_SQL();	
		        	ResultSet rsPayTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        	
		        	//display available status. 
		        	out.println ("<TD>");
		        	ArrayList<String> alPayTypes = new ArrayList<String>(0);
		        	//add one entry for "all department"
		        	while (rsPayTypes.next()){
		        		if (TimeCardUtilities.TypeCombinationCheck(rsEmployeeInfo.getInt(Employees.iEmployeePayType), rsPayTypes.getInt(PayTypes.iTypeID))){
		        			alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt(PayTypes.iTypeID) + " VALUE=0 CHECKED>" + rsPayTypes.getString(PayTypes.sTypeTitle) + " - " + rsPayTypes.getString(PayTypes.sTypeDesc));
		        		}else{
		        			alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsPayTypes.getInt(PayTypes.iTypeID) + " VALUE=0>" + rsPayTypes.getString(PayTypes.sTypeTitle) + " - " + rsPayTypes.getString(PayTypes.sTypeDesc));
		        		}
		        	}
		        	rsPayTypes.close();
		        	out.println(TimeCardUtilities.Build_HTML_Table(3, alPayTypes, 0, false));
			        out.println ("</TD></TR>");
		        	
		        	
			        //Eligible Employee Status
		        	out.println("<TR><TD><B>Employee Status</B></TD>");
		        	//get recordset of employee status
		        	sSQL = TimeCardSQLs.Get_Employee_Status_Info_SQL();	
		        	ResultSet rsEmployeeStatus = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        	
		        	//display available status. 
		        	out.println ("<TD>");
		        	ArrayList<String> alEmployeeStatus = new ArrayList<String>(0);
		        	//add one entry for "all department"
		        	while (rsEmployeeStatus.next()){
		        		if (TimeCardUtilities.TypeCombinationCheck(rsEmployeeInfo.getInt(Employees.iEmployeeStatus), rsEmployeeStatus.getInt(EmployeeStatus.iStatusID))){
		        			alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt(EmployeeStatus.iStatusID) + " VALUE=0 CHECKED>" + rsEmployeeStatus.getString(EmployeeStatus.sStatusTitle) + " - " + rsEmployeeStatus.getString(EmployeeStatus.sStatusDesc));
		        		}else{
		        			alEmployeeStatus.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsEmployeeStatus.getInt(EmployeeStatus.iStatusID) + " VALUE=0>" + rsEmployeeStatus.getString(EmployeeStatus.sStatusTitle) + " - " + rsEmployeeStatus.getString(EmployeeStatus.sStatusDesc));
		        		}
		        	}
		        	rsEmployeeStatus.close();
		        	out.println(TimeCardUtilities.Build_HTML_Table(3, alEmployeeStatus, 0, false));
			        out.println ("</TD></TR>");
		        	
			        
		        	//Work Hour
		        	out.println ("<TR><TD><B>Weekly Work Hour</B></TD><TD>" + 
	      			      "<INPUT TYPE=TEXT NAME=\"WorkHour\" VALUE=\"" + rsEmployeeInfo.getDouble(Employees.dWorkHour) + "\" SIZE=7 MAXLENGTH=7>Numbers ONLY</TD></TR>");
		        	
		        	//Is Active?
		        	out.println ("<TR><TD><B>Is Active?</B></TD><TD>");
		        	if (rsEmployeeInfo.getInt(Employees.iActive) == 1){
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsActive\" VALUE=1 CHECKED>Yes<BR>");
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsActive\" VALUE=0>No<BR>");
		        	}else{
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsActive\" VALUE=1>Yes<BR>");
		        		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"IsActive\" VALUE=0 CHECKED>No<BR>");
		        	}
			        out.println ("</TD></TR>");
	        		out.println ("</TABLE>");
		        
		        	out.println ("<BR>");
		        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
		        	if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
														AccessControlFunctionList.EditEmployeeConfidentialInformation)){
		        		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	}
		        	out.println ("</FORM>");
		        }else{
		        	//failed looking up employee
		        	out.println("<FONT SIZE=3><B>There is no information for the selected employee.</B></FONT>");
		        }
	        	rsEmployeeInfo.close();
	        	rsDepartments.close();
			}else{
				out.println("<FONT SIZE=3><B>Your don't have right to view information on this page.</B></FONT>");
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
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
}