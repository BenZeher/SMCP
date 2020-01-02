package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import TimeCardSystem.LeaveTypeTotal;
import TCSDataDefinition.*;

public class EmployeeLeaveSummaryGenerate extends HttpServlet {

	//A Nightmare In ELM Class....
	
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    
	    HttpSession CurrentSession = request.getSession();

	    try {
	    	String sSQL;
	    	
	    	//Calculate time period
	    	SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
	    	//SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy EEE");
	    	//Date SelectedStartingDay = Date.valueOf(request.getParameter("SelectedStartingYear") + "-" + request.getParameter("SelectedStartingMonth") + "-" + request.getParameter("SelectedStartingDay"));
	    	
	    	//Date SelectedEndingDay = Date.valueOf(request.getParameter("SelectedEndingYear") + "-0" + request.getParameter("SelectedEndingMonth") + "-" + request.getParameter("SelectedEndingDay"));
	    	@SuppressWarnings({ "deprecation" })
			Date SelectedEndingDay = new Date(Integer.parseInt(request.getParameter("SelectedEndingYear")) - 1900, 
					    					  Integer.parseInt(request.getParameter("SelectedEndingMonth")) - 1, 
					    					  Integer.parseInt(request.getParameter("SelectedEndingDay")));
	    	
	    	//Customized title
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
				        "<HTML>" +
				        "<HEAD><TITLE>Employee Leave Summary</TITLE></HEAD>\n<BR>" + 
					    "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">" +
					    "<TABLE BORDER=0 WIDTH=100%><TR><TD VALIGN=BOTTOM><FONT SIZE=5><B>Employee Leave Summary</B></FONT></TD>" +
					    "<TD VALIGN=BOTTOM><B>&nbsp;&nbsp;&nbsp;&nbsp;</TD></TR>" +
					    "<TR><TD><Font SIZE=1><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A></TD></TR></TABLE>");

	    	boolean bShowEligibleOnly;
	    	if (request.getParameter("ShowEligibleOnly") != null){
	    		bShowEligibleOnly = true;
	    	}else{
	    		bShowEligibleOnly = false;
	    	}
	    	
	    	//create a list of what types are selected.
	    	Enumeration<String> paramNames = request.getParameterNames();
    		ArrayList<String> alTypes = new ArrayList<String>(0);
    		while(paramNames.hasMoreElements()) {
    			String s = paramNames.nextElement().toString();
    	        if (s.substring(0, 3).compareTo("!1!") == 0){
    	        	//if "all departments" is selected, disregard all other selections.
    	        	if (s.substring(3).compareTo("ALLTYPES") == 0){
    	        		alTypes.clear();
    	        		alTypes.add(s.substring(3));
    	        		break;
    	        	}else{
    	        		alTypes.add(s.substring(3));
    	        	}
    	        }
    		}
    		if (alTypes.size() <= 0){
    			alTypes.add("-1");
    		}

	    	//get all employees in the selected department.
	    	if (request.getParameter("SelectedEmployee").compareTo("") != 0){
	    		sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("SelectedEmployee"));
	    	}else{ 
	    		paramNames = request.getParameterNames();
	    		ArrayList<String> alDepartments = new ArrayList<String>(0);
	    		while(paramNames.hasMoreElements()) {
	    			String s = paramNames.nextElement().toString();
	    	        if (s.substring(0, 2).compareTo("!!") == 0){
	    	        	//if "all departments" is selected, disregard all other selections.
	    	        	if (s.substring(2).compareTo("ALLDEPT") == 0){
	    	        		alDepartments.clear();
	    	        		alDepartments.add(s.substring(2));
	    	        		break;
	    	        	}else{
	    	        		alDepartments.add(s.substring(2));
	    	        	}
	    	        }
	    		}
	    		boolean bIncludeInactive = Boolean.parseBoolean(request.getParameter("ShowInactive"));
	    		if (alDepartments.size() > 0) {
	    			if (alDepartments.get(0).toString().compareTo("ALLDEPT") == 0){
		        		sSQL = TimeCardSQLs.Get_Employee_List_SQL(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString(), bIncludeInactive, false);
	    			}else{
		    			//if there is any specific selected department, go with them
		    			sSQL = TimeCardSQLs.Get_Employee_List_By_Department_SQL(alDepartments, bIncludeInactive, false);	
	    			}
	    		}else{
	    			//if there is no department selected, don't display any
	    			sSQL = TimeCardSQLs.Get_Employee_Info_SQL("nobody");
	    		}
	    	}
	    	ResultSet EmployeeList = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	ArrayList<String> alTable = new ArrayList<String>(0);

	    	alTable.add("<B>Employee</B>");
	    	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL(alTypes);
	    	ResultSet rsLeaveAdjustmentTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	while (rsLeaveAdjustmentTypes.next()){
	    		alTable.add("<B>" + rsLeaveAdjustmentTypes.getString(LeaveAdjustmentTypes.sTypeTitle) + "</B>");
	    	}
	    	int iColumnNumber = alTable.size();
	    	
	    	//check balance for all employees.
	    	while (EmployeeList.next()){
	    		String sCurrentEmployee = EmployeeList.getString(Employees.sEmployeeID);
		    	sSQL = TimeCardSQLs.Get_Employee_Info_SQL(sCurrentEmployee);
		    	ResultSet rsEmployee = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
		    	while(rsEmployee.next()){
		    		//print out employee name in the first column of each row
		    		alTable.add(rsEmployee.getString(Employees.sEmployeeLastName) + ", " + rsEmployee.getString(Employees.sEmployeeFirstName));
		    		//create an arraylist for leave types associated with this employee.
			    	ArrayList<LeaveTypeTotal> alLeaveTotals = new ArrayList<LeaveTypeTotal>(0);
			    	rsLeaveAdjustmentTypes.beforeFirst();
			    	while (rsLeaveAdjustmentTypes.next()){					
			    		boolean bCO = false;
						if (rsLeaveAdjustmentTypes.getInt("iCarriedOver") == 1){
							bCO = true;
						}
			    		alLeaveTotals.add(new LeaveTypeTotal(rsLeaveAdjustmentTypes.getInt("iTypeID"),
			    											 rsLeaveAdjustmentTypes.getString("sTypeTitle"),
			    											 rsLeaveAdjustmentTypes.getString("sTypeDesc"),
			    											 0,
			    											 0,
			    											 TimeCardUtilities.FindStartDate(rsLeaveAdjustmentTypes.getString("sAwardPeriod"), 
			    													 	   					 SelectedEndingDay, 
			    													 	   					 rsEmployee.getDate("datHiredDate")),
			    											 bCO,
			    											 rsLeaveAdjustmentTypes.getDouble("dMaximumHourAvailable")
			    											 ));
			    	}
			    	//get totals from begin of time.
	
		    		for (int i=0;i<alLeaveTotals.size();i++){
		    			
		    			//only corresponding type and entries happened after the start time gets added in
		    			sSQL = TimeCardSQLs.Get_Leave_Adjustments_SQL(sCurrentEmployee, //Employee
		    														((LeaveTypeTotal)alLeaveTotals.get(i)).getID(), //leave type ID
		    														  SQLDateformatter.format(((LeaveTypeTotal)alLeaveTotals.get(i)).getStartDate()), //the starting date for this leave type
		    														  SelectedEndingDay.toString(), //the cutoff date
		    														  "id", // sort the rs by id
		    														  0, //sort the rs ascendingly
		    														  true); //show finalized adjustments too
		    			ResultSet rsLeaves = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    			
		    			while(rsLeaves.next()){
		    				
		    				double dDuration = rsLeaves.getDouble("dDuration");
		    				if (dDuration < 0){
		    					((LeaveTypeTotal)alLeaveTotals.get(i)).setTotalCredit(((LeaveTypeTotal)alLeaveTotals.get(i)).getTotalCredit() + dDuration);
		    				}else{
		    					((LeaveTypeTotal)alLeaveTotals.get(i)).setTotalLogged(((LeaveTypeTotal)alLeaveTotals.get(i)).getTotalLogged() + dDuration);
		    				}
		    			}
		    			rsLeaves.close();
		    		}
		    		
			    	for (int i=0;i<alLeaveTotals.size();i++){	
		    			//get leave type
			    		LeaveTypeTotal lttTemp = (LeaveTypeTotal)alLeaveTotals.get(i);
		    			
			    		sSQL = TimeCardSQLs.Get_Leave_Adjustment_Type_Info_SQL(lttTemp.getID());
						ResultSet rsTypeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
						sSQL = TimeCardSQLs.Get_Lump_Sum_Detail_SQL("" + lttTemp.getID());
						ResultSet rsLumpSumDetails = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
						sSQL = TimeCardSQLs.Get_Employee_Info_SQL(rsEmployee.getString("sEmployeeID"));
						ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
						//no need to check, should be unique for both.
						rsTypeInfo.next();
						rsEmployeeInfo.next();
			    		
		    			//check to see if this employee is eligible for this leave type.
		    			boolean bEligible = TimeCardUtilities.CheckEligibility(rsTypeInfo, 
		    												 				   rsEmployeeInfo);
		    			
		    			if (bEligible || !bShowEligibleOnly){
			    			/*
			    			 * Total calculation
			    			 */
			    			if (!bEligible){
			    				//printout a blank row filled with N/A
			    				alTable.add("N/A");
			    			}else{
				    			double dCalTotal;
				    			//calculate total
			    				dCalTotal = TimeCardUtilities.CalculateLeaveTotal(rsTypeInfo, rsEmployeeInfo, rsLumpSumDetails, SelectedEndingDay);
				    			/*
			    				//display calculated total
				    			out.print("<TD ALIGN=RIGHT><B>" + TimeCardUtilities.RoundHalfUp(dCalTotal, 2) + "</B></TD>");
				    			//total logged/used
				    			out.print("<TD ALIGN=RIGHT><FONT COLOR=RED><B>" + TimeCardUtilities.RoundHalfUp(lttTemp.getTotalLogged(), 2) + "</B></FONT></TD>");
				    			//total credit/added
				    			out.print("<TD ALIGN=RIGHT><FONT COLOR=GREEN><B>" + TimeCardUtilities.RoundHalfUp(lttTemp.getTotalCredit(), 2) + "</B></FONT></TD>");
				    			//display time balance
				    			*/
				    			if ((dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged()) > 0){
				    				alTable.add("<FONT COLOR=GREEN><B>" + TimeCardUtilities.RoundHalfUp((dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged()), 2) + "</B></FONT>");
				    			}else if ((dCalTotal + lttTemp.getTotalCredit() - lttTemp.getTotalLogged()) < 0){
				    				alTable.add("<FONT COLOR=RED><B>" + TimeCardUtilities.RoundHalfUp(dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged(), 2) + "</B></FONT>");
				    			}else{
				    				alTable.add("<B>0.0</B>");
				    			}
			    			}
			    		}
		    			rsTypeInfo.close();
		    			rsLumpSumDetails.close();
		    			rsEmployeeInfo.close();
		    		}
			    	//out.print("<BR>");
		    	}
		    	rsEmployee.close();
	    	}
	    	out.println(TimeCardUtilities.Build_HTML_Table(iColumnNumber, alTable, 100, 1, false, false));
	    	EmployeeList.close();
	    	rsLeaveAdjustmentTypes.close();
	    	//out.println("&nbsp;&nbsp;&nbsp;&nbsp;*Starred leave time is not included in the total time.<BR>");
	    	//out.println("&nbsp;&nbsp;*Leave History includes all recorded leave time and adjustments within the selected time range.<BR>");
	    	out.println("**Current Period is the period starting on the immediate preceding anniversary date of each leave type, ending on selected As-Of-Date.<BR>");
	    	
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	    out.println("</BODY></HTML>");
	}
}
