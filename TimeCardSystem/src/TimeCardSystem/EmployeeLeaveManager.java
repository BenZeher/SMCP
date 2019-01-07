package TimeCardSystem;

import java.io.IOException;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

//import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import TimeCardSystem.LeaveTypeTotal;

public class EmployeeLeaveManager extends HttpServlet {

	//A Nightmare In ELM Class....
	
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    
	    HttpSession CurrentSession = request.getSession();

	    try {
	    	String sCurrentEmployee = request.getParameter("SelectedEmployee");
	    	
	    	String sSQL;
	    	
	    	//Calculate time period
	    	SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
	    	//SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy EEE");
	    	//SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	    	//Calendar c = Calendar.getInstance();
	    	//Date SelectedMonday = (Date)DateFormatter.parse(request.getParameter("Monday"));
	    	String sMonth = request.getParameter("SelectedStartingMonth");
	    	if (sMonth.length() == 1){
	    		sMonth = "0" + sMonth;
	    	}
	    	String sDay = request.getParameter("SelectedStartingDay");
	    	if (sDay.length() == 1){
	    		sDay = "0" + sDay;
	    	}

	    	Date SelectedStartingDay = Date.valueOf(request.getParameter("SelectedStartingYear") 
	    			+ "-" + sMonth 
	    			+ "-" + sDay
	    	);
	    	
	    	sMonth = request.getParameter("SelectedEndingMonth");
	    	if (sMonth.length() == 1){
	    		sMonth = "0" + sMonth;
	    	}
	    	sDay = request.getParameter("SelectedEndingDay");
	    	if (sDay.length() == 1){
	    		sDay = "0" + sDay;
	    	}
	    	Date SelectedEndingDay = Date.valueOf(request.getParameter("SelectedEndingYear") 
	    			+ "-" + sMonth
	    			+ "-" + sDay
	    	);
	    	
	    	//Customized title
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
					   "Transitional//EN\">" +
				       "<HTML>" +
				       "<HEAD><TITLE>Employee Leave Manager</TITLE></HEAD>\n<BR>" + 
					   "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\"" +
				       " style = \" font-family:" + TimeCardUtilities.BASE_FONT_FAMILY + " \" >" + 
					   "<TABLE BORDER=0 WIDTH=100%><TR><TD VALIGN=BOTTOM><FONT SIZE=5><B>Employee Leave Manager</B></FONT></TD>" +
					   "<TD VALIGN=BOTTOM><B>&nbsp;&nbsp;&nbsp;&nbsp;</TD></TR>" +
					   "<TR><TD><Font SIZE=1><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A>&nbsp;&nbsp;<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveManagerCriteriaSelection?SelectedEmployee=" + sCurrentEmployee + "\">Return to criteria selection</A></FONT></TD></TR></TABLE>");

	    	//get current URL
	    	String sCurrentURL;
	    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
	    	//sCurrentURL = sCurrentURL.replaceAll("&", "*");
	    	
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
    	        		//System.out.println("All types selected");
    	        		break;
    	        	}else{
    	        		alTypes.add(s.substring(3));
    	        	}
    	        }
    		}
    		if (alTypes.size() <= 0){
    			alTypes.add("-1");
    		}
    		
	    	//get employee Info
	    	sSQL = TimeCardSQLs.Get_Employee_Info_SQL(sCurrentEmployee);
	    	ResultSet rsEmployee = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	while(rsEmployee.next()){
	    		
	    		//create an arraylist for leave types associated with this employee.
		    	ArrayList<LeaveTypeTotal> alLeaveTotals = new ArrayList<LeaveTypeTotal>(0);
		    	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Types_SQL(alTypes);
		    	ResultSet rsLeaveAdjustmentTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
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
		    											 FindStartDate(rsLeaveAdjustmentTypes.getString("sAwardPeriod"), 
		    													 	   SelectedEndingDay, 
		    													 	   rsEmployee.getDate("datHiredDate")),
		    											 bCO,
		    											 rsLeaveAdjustmentTypes.getDouble("dMaximumHourAvailable")
		    											 ));
		    	}
		    	/*
		    	for (int i=0;i<alLeaveTotals.size();i++){
		    		System.out.println("leave type(" + i + "): " + ((LeaveTypeTotal)alLeaveTotals.get(i)).getTitle());
		    	}
		    	*/
		    	//System.out.println("Finished initializing all available adjustment types.");
		    	rsLeaveAdjustmentTypes.close();
	    		
		    	out.println("<TABLE BORDER=0 WIDTH=100%>");
	    		out.print("<TR>");
	    		out.print("<TD><Font SIZE=2><B>Employee ID:</B> " + sCurrentEmployee + "</FONT></TD>");
	    		out.print("<TD><Font SIZE=2><B>Employee Name:</B> " + rsEmployee.getString("sEmployeeLastName") + ", " + rsEmployee.getString("sEmployeeFirstName") + " " + rsEmployee.getString("sEmployeeMiddleName") + "</FONT></TD>");
	    		out.print("<TD><Font SIZE=2><B>Hired Date:</B> "); 
	    		if ((rsEmployee.getString("datHiredDate")).compareTo("0000-00-00") == 0){
	    			//no hire date, print "N/A"
	    			out.println("N/A</FONT></TD>");	    			
	    		}else{
	    			out.println(USDateOnlyformatter.format(clsDateAndTimeConversions.SQLDateToUtilDate(rsEmployee.getDate("datHiredDate"))) + "</FONT></TD>");
	    		}
	    		out.println("<TD><Font SIZE=2><B>Work Hour:</B> " + TimeCardUtilities.RoundHalfUp(rsEmployee.getDouble("dWorkHour"), 2) + "</FONT></TD>");
	    		//out.println("<TD><Font SIZE=2><B>Award Period:</B> " + rsEmployee.getString("sAwardPeriod") + "</FONT></TD>");
	    		//out.println("<TD><Font SIZE=2><B>Award Type:</B> " + rsEmployee.getString("sAwardType") + "</FONT></TD>");
	    		out.println("</TR>");
	    		//out.println("<TR><TD ALIGN=LEFT COLSPAN=3>Total number of selected leave types: " + alLeaveTotals.size() + "</TD></TR>");
	    		out.println("<TR>");
	    		out.println("<TD><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveEdit?Employee=" + sCurrentEmployee +  
	    																			"&id=-1" + 
	    																			"&OriginalURL=" + sCurrentURL +
	    																			"\"><img src=\"/images/TimeCardSystem/new.gif\"></A></TD>");
	    		out.println("</TR></TABLE>");
	    		
	    		//get all the leave information
	    		sSQL = TimeCardSQLs.Get_Leave_Adjustments_SQL(sCurrentEmployee, 
	    													  alTypes,
	    													  SelectedStartingDay.toString(),
	    													  SelectedEndingDay.toString(),
	    													  request.getParameter("SortBy"),
	    													  Integer.parseInt(request.getParameter("SortOrder")),
	    													  true);
	    		ResultSet rsLeaveAdjustments = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		
	    		
		    	out.println("<TABLE BORDER=0 WIDTH=100%>");
		    	out.println("<TR><TD ALIGN=CENTER COLSPAN=6>");
	    		out.println("<FONT SIZE=3><B>Leave History* Start Date:</B> " + USDateOnlyformatter.format(SelectedStartingDay) +
						    "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
						    "<B>Leave History End Date:</B> " + USDateOnlyformatter.format(SelectedEndingDay) + 
						    "</FONT>");
	    		out.println("</TD></TR>");
		    	out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
		    	out.println("<TR><TD ALIGN=CENTER WIDTH=10%><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("id", request) + "\"><B>ID</B></A></TD>" +
		    					"<TD ALIGN=CENTER WIDTH=10%><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("iLeaveTypeID", request) + "\"><B>Type</B></A></TD>" +
		    					"<TD ALIGN=CENTER WIDTH=20%><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("dtInTime", request) + "\"><B>Start Time</B></A></TD>" +
		    					"<TD ALIGN=CENTER WIDTH=20%><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("dtOutTime", request) + "\"><B>End Time</B></A></TD>" +
		    					"<TD ALIGN=CENTER WIDTH=10%><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("dDuration", request) + "\"><B>Duration</B></A></TD>" +
		    					"<TD ALIGN=CENTER WIDTH=30%><B>Note</B></TD></TR>");
		    	//out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
		    	
		    	//int iLine = 0;
		    	
		    	while (rsLeaveAdjustments.next()){
		    		//System.out.println("Adjustment Loop.");
		    		//iLine++;
		    		/*
		    		if (EvaluateEligibility(rsTypeInfo.getDate("dtEffectiveDate"), 
										    rsTypeInfo.getInt("iEligibleEmployeePayType"), rsEmployee.getInt("iEmployeePayType"),
										    rsTypeInfo.getInt("iEligibleEmployeeStatus"), rsEmployee.getInt("iEmployeeStatus"),
										    rsTypeInfo.getDouble("dMinimumHourWorked"), rsEmployee.getDouble("dWorkHour"))){
		    		*/
			    		out.println("<TR>");
			    		//line number and leave type
			    		out.println("<TD ALIGN=CENTER><FONT size=2><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveEdit?Employee=" + sCurrentEmployee + 
										    																		  "&id=" + rsLeaveAdjustments.getInt("LeaveAdjustments.id") +
										    																		  "&OriginalURL=" + sCurrentURL +
										    																		  "\">" + rsLeaveAdjustments.getInt("LeaveAdjustments.id") + "</A></FONT></TD>" +
			    					"<TD ALIGN=CENTER><FONT size=2>" + rsLeaveAdjustments.getString("LeaveAdjustmentTypes.sTypeDesc") + "</FONT></TD>");
			    		//start time
			    		if (rsLeaveAdjustments.getString("LeaveAdjustments.dtInTime").compareTo("0000-00-00") != 0){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + USDateOnlyformatter.format(clsDateAndTimeConversions.SQLDateToUtilDate(rsLeaveAdjustments.getDate("LeaveAdjustments.dtInTime"))) + "</FONT></TD>"); //catch NULLs
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2>&nbsp;</FONT></TD>"); 
			    		}
			    		//end time
			    		if (rsLeaveAdjustments.getString("LeaveAdjustments.dtOutTime").compareTo("0000-00-00") != 0){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + USDateOnlyformatter.format(clsDateAndTimeConversions.SQLDateToUtilDate(rsLeaveAdjustments.getDate("LeaveAdjustments.dtOutTime"))) + "</FONT></TD>"); //catch NULLs
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2>&nbsp;</FONT></TD>"); 
			    		}
			    		//duration, accumulate time by difference categories
			    		double dDuration = rsLeaveAdjustments.getDouble("LeaveAdjustments.dDuration");
			    		
			    		
			    		//accumulation is not done here. see below in "total section"
			    		
			    		out.print("<TD ALIGN=CENTER><FONT size=2><B>" + dDuration + "</B></FONT></TD>");
			    		//notes
			    		if (rsLeaveAdjustments.getString("LeaveAdjustments.mNote") != null){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + rsLeaveAdjustments.getString("LeaveAdjustments.mNote").trim() + "</FONT></TD></TR>");
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2> &nbsp;</FONT></TD>");
			    		}
			    		out.println("</TR>");
		    		//}
		    	}
		    	out.println("<TR><TD COLSPAN=6><HR></TD1></TR>");
		    	out.println("</TABLE><BR>");
		    	rsLeaveAdjustments.close();
		    	
		    	
		    	
		    	//total section
		    	
		    	
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
	    					//System.out.println("Before: " + ((LeaveTypeTotal)alLeaveTotals.get(i)).getTotalLogged());
	    					//System.out.println("Addition: " + dDuration);
	    					((LeaveTypeTotal)alLeaveTotals.get(i)).setTotalLogged(((LeaveTypeTotal)alLeaveTotals.get(i)).getTotalLogged() + dDuration);
	    					//System.out.println("After: " + ((LeaveTypeTotal)alLeaveTotals.get(i)).getTotalLogged());
	    				}
	    			}
	    			rsLeaves.close();
	    		}
		    	
		    	out.println("<Table BORDER=1 WIDTH=80%>");
		    	out.println("<TR><TD ALIGN=CENTER COLSPAN=5><FONT SIZE=4>Individual Balance of Current Period**:</FONT></TD></TR>");
		    	out.println("<TR><TD ALIGN=CENTER WIDTH=40%><B>Leave Type</B></TD><TD ALIGN=CENTER WIDTH=15%><B>Total (Calculated)</B></TD><TD ALIGN=CENTER WIDTH=15%><B>Total Logged</B></TD><TD ALIGN=CENTER WIDTH=15%><B>Total Credit</B></TD><TD ALIGN=CENTER WIDTH=15%><B>Available</B></TD></TR>");
		    	
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
	    			boolean bEligible = CheckEligibility(rsTypeInfo, 
	    												 rsEmployeeInfo);
	    			
	    			if (bEligible || !bShowEligibleOnly){
		    			out.print("<TR>");
		    			//display the leave description
		    			out.print("<TD ALIGN=CENTER><B>" + lttTemp.getDesc() + "<B></TD>");
		    			/*
		    			 * Total calculation
		    			 */
		    			if (!bEligible){
		    				//printout a blank row filled with N/A
		    				out.println("<TD ALIGN=RIGHT>N/A</TD>" +
		    							"<TD ALIGN=RIGHT>N/A</TD>" +
		    							"<TD ALIGN=RIGHT>N/A</TD>" +
		    							"<TD ALIGN=RIGHT>N/A</TD>");
		    			}else{
			    			double dCalTotal;
			    			//calculate total
		    				dCalTotal = CalculateLeaveTotal(rsTypeInfo, rsEmployeeInfo, rsLumpSumDetails, SelectedEndingDay);
			    			//display calculated total
			    			out.print("<TD ALIGN=RIGHT><B>" + TimeCardUtilities.RoundHalfUp(dCalTotal, 2) + "</B></TD>");
			    			//total logged/used
			    			out.print("<TD ALIGN=RIGHT><FONT COLOR=RED><B>" + TimeCardUtilities.RoundHalfUp(lttTemp.getTotalLogged(), 2) + "</B></FONT></TD>");
			    			//total credit/added
			    			out.print("<TD ALIGN=RIGHT><FONT COLOR=GREEN><B>" + TimeCardUtilities.RoundHalfUp(lttTemp.getTotalCredit(), 2) + "</B></FONT></TD>");
			    			//display time balance
			    			//System.out.println("Calculation: " + dCalTotal + " - " + lttTemp.getTotalCredit() + " - " + lttTemp.getTotalLogged() + " = " + (dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged()));
			    			if ((dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged()) > 0){
			    				out.print("<TD ALIGN=RIGHT><FONT COLOR=GREEN><B>" + TimeCardUtilities.RoundHalfUp((dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged()), 2) + "</B></FONT></TD>");
			    			}else if ((dCalTotal + lttTemp.getTotalCredit() - lttTemp.getTotalLogged()) < 0){
			    				out.print("<TD ALIGN=RIGHT><FONT COLOR=RED><B>" + TimeCardUtilities.RoundHalfUp(dCalTotal - lttTemp.getTotalCredit() - lttTemp.getTotalLogged(), 2) + "</B></FONT></TD>");
			    			}else{
			    				out.print("<TD ALIGN=RIGHT><B>0.0</B></TD>");
			    			}
		    			}
		    			out.print("</TR>");
		    		}
	    			rsTypeInfo.close();
	    			rsLumpSumDetails.close();
	    			rsEmployeeInfo.close();
	    		}
		    	out.print("</TABLE><BR>");
	    	}
	    	rsEmployee.close();
	    	
	    	//out.println("&nbsp;&nbsp;&nbsp;&nbsp;*Starred leave time is not included in the total time.<BR>");
	    	out.println("&nbsp;&nbsp;*Leave History includes all recorded leave time and adjustments within the selected time range.<BR>");
	    	out.println("**Current Period is the period starting on the immediate preceding anniversary date of each leave type, ending on selected As-Of-Date.<BR>");
	    	
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	    out.println("</BODY></HTML>");
	}
	
	private boolean CheckEligibility(ResultSet rsTypeInfo, ResultSet rsEmployeeInfo){
		
		try{
			return EvaluateEligibility(rsTypeInfo.getDate("dtEffectiveDate"), 
									   rsTypeInfo.getInt("iEligibleEmployeePayType"), rsEmployeeInfo.getInt("iEmployeePayType"),
									   rsTypeInfo.getInt("iEligibleEmployeeStatus"), rsEmployeeInfo.getInt("iEmployeeStatus"),
									   rsTypeInfo.getDouble("dMinimumHourWorked"), rsEmployeeInfo.getDouble("dWorkHour")
									   );
		}catch(SQLException exSQL){
			System.out.println("<BR><BR>Error in EmployeeLeaveManager.EvaluateEligibility!!");
			System.out.println("SQLException: " + exSQL.getErrorCode() + " - " + exSQL.toString());
			return false;
		}
		
	}
	
	private boolean EvaluateEligibility(Date datEffectiveDate, 
										int iTypePayType, int iEmployeePayType, 
										int iTypeStatus, int iEmployeeStatus,
										double dTypeWorkHour, double dEmployeeWorkHour
										){
		
		try{
			boolean b = true;
			//System.out.println("Cehck eligibility for " + rsTypeInfo.getString("sTypeTitle") + "-" + rsTypeInfo.getString("sTypeDesc"));
			//check effective time
			if (datEffectiveDate.getTime() > System.currentTimeMillis()){
				b = false;
				//System.out.println("EffectiveDate .... FAILED.");
			}
			//System.out.println("EffectiveDate .... checked.");
			//check employee pay type
			if (!TimeCardUtilities.TypeDirectCheck(iTypePayType, 
												   iEmployeePayType)){
				b = false;
				//System.out.println("EligibleEmployeePayType .... FAILED.");
			}
			//System.out.println("EligibleEmployeePayType .... checked.");
			//check employee status
			//System.out.println("rsTypeInfo.getInt(\"iEligibleEmployeeStatus\") = " + rsTypeInfo.getInt("iEligibleEmployeeStatus"));
			//System.out.println("rsEmployeeInfo.getInt(\"iEmployeeStatus\") = " + rsEmployeeInfo.getInt("iEmployeeStatus"));
			if (!TimeCardUtilities.TypeDirectCheck(iTypeStatus, 
												   iEmployeeStatus)){
				b = false;
				//System.out.println("EligibleEmployeeStatus .... FAILED.");
			}     
			//System.out.println("EligibleEmployeeStatus .... checked.");
	        //check minimum work hour
			if (dTypeWorkHour > dEmployeeWorkHour){
				b = false;
				//System.out.println("MinimumHourWorked .... FAILED.");
			}
			//System.out.println("MinimumHourWorked .... checked.");
			
			return b;
			
		}catch (Exception ex) {
	    	System.out.println("<BR><BR>Error in EmployeeLeaveManager.EvaluateEligibility!!");
	    	System.out.println("Exception: " + ex.getMessage());
	    	return false;
	    }
	}
	
	@SuppressWarnings("deprecation")
	private double CalculateLeaveTotal(ResultSet rsTypeInfo, 
									   ResultSet rsEmployeeInfo,
									   ResultSet rsLumpSumDetails,
									   Date datCutOff){
		
		try{
			
			double dHours = 0;
			//find out the "start date" of the current period of this particular leave tpye.
			Calendar cCutOff = Calendar.getInstance();
			cCutOff.setTime(datCutOff);
			Timestamp tsStartTime = FindStartDate(rsTypeInfo.getString("sAwardPeriod"), 
												  datCutOff, 
												  rsEmployeeInfo.getDate("datHiredDate"));
			//calculate earned leave amount
			if (rsTypeInfo.getDouble("dAwardType") >= 0){
				//accrue type leave
				//System.out.println("accrue type calculation: " + rsTypeInfo.getDouble("dAwardType") +  " hour/year");
				Calendar c1231 = Calendar.getInstance();
				int iDaysAYear;
				/* the following part determines how many days are there
				 * in the year of interest. If the starting date is later
				 * than February, then whether or not it's leap year is 
				 * determined by the next year. If the starting day is in
				 * January or February, then this year's leap-or-not will 
				 * determine the number of days in the calculation.
				 */
				if (tsStartTime.getMonth() <= Calendar.FEBRUARY){
					c1231.set(cCutOff.get(Calendar.YEAR), 11, 31);
					iDaysAYear = c1231.get(Calendar.DAY_OF_YEAR);
				}else{
					c1231.set(cCutOff.get(Calendar.YEAR) + 1, 11, 31);
					iDaysAYear = c1231.get(Calendar.DAY_OF_YEAR);
				}
				//System.out.println("c1231 = " + c1231.getTime().toString());
				//System.out.println("Days-A-Year = " + iDaysAYear);
				//if the employee date is later than last year's 1231, then use the employee date
				if (rsEmployeeInfo.getDate("datHiredDate").compareTo(c1231.getTime()) > 0){
					dHours = rsTypeInfo.getDouble("dAwardType") * ((cCutOff.getTimeInMillis() - rsEmployeeInfo.getDate("datHiredDate").getTime()) / 86400000.0 / iDaysAYear); 
				}else{
					dHours = rsTypeInfo.getDouble("dAwardType") * ((cCutOff.getTimeInMillis() - tsStartTime.getTime()) / 86400000.0 / iDaysAYear); 
				}
				
			}else{
				//lump sum type leave
				//findout how long this employee has worked here.
				Calendar cHiredDate = Calendar.getInstance();
				cHiredDate.setTime(rsEmployeeInfo.getDate("datHiredDate"));
				double dMonths = (cCutOff.get(Calendar.YEAR) - cHiredDate.get(Calendar.YEAR)) * 12 + cCutOff.get(Calendar.MONTH) - cHiredDate.get(Calendar.MONTH);
				if (cCutOff.get(Calendar.DATE) < cHiredDate.get(Calendar.DATE)){
					dMonths = dMonths - 1;
				}
				//start with 0, so in case there is not lump sum rules built, it will return 0, which make sense.
				//create 2 arrays for recording different accumulation rate.
				ArrayList<Double> alStepUpMonth = new ArrayList<Double>(0);
				ArrayList<Double> alAccumuRates = new ArrayList<Double>(0);
				
				while (rsLumpSumDetails.next()){
					/* this algorithm will find the hour figure corresponding
					 * to the GREATEST MONTH number. so the result may not be
					 * the greatest hour figure. for example, a 10-year-worker
					 * may get less hours than a 5-year-worker, if the rule
					 * says so, but a 10-year-worker will always get what's set
					 * for 10-year-worker, nothing else.
					 * ******************************************************
					 * this function also break down the time and calculate the 
					 * time separately if there happen to be a step up in the 
					 * period.
					 */
					if (rsLumpSumDetails.getDouble("dNumberOfMonths") <= dMonths){
						dHours = rsLumpSumDetails.getDouble("dNumberOfHours");
						//record all the step-ups.
						alStepUpMonth.add(Double.valueOf(rsLumpSumDetails.getDouble("dNumberOfMonths")));
						alAccumuRates.add(Double.valueOf(rsLumpSumDetails.getDouble("dNumberOfHours")));
						
					}else{
						break;
					}
				}
				//Calendar cStepUp = Calendar.getInstance();
				/* LTO 20080610
				 * As we got all the step-up-date now, we only concern about those
				 * dates which falls into our period, that is from start time to 
				 * cutoff time. So the following loop will go through all the 
				 * step-up-dates to see who is within range, and remove all those
				 * are not of our concern. 
				 * *****************************
				 * One extra rate needs to be recorded, is the rate for just before
				 * the first effective step up, cause we need it to be in the 
				 * calculation. 
				 */
				double dPrevRate = 0;
				for (int i=0;i<alStepUpMonth.size();i++){
					Timestamp tsStepUp = Locate_Stepup_Time(new Timestamp(rsEmployeeInfo.getDate("datHiredDate").getTime()), 
													  		Double.parseDouble(alStepUpMonth.get(i).toString()));
					//compare this step-up-date with this period's start date
					if (tsStepUp.getTime() > tsStartTime.getTime()){
						/* this means this step-up-date falls into current period,
						 * as well as all the step-up-dates following it.
						 * so we need to do a breakdown and calculate the length
						 * of vacation day by day, because of the existence of
						 * leap year.
						 * ***********************************
						 * Once gets here, the previous retrieved dHour means
						 * nothing. The new dHour will be caulculated based on
						 * all the different rate included in this period.
						 */
						dHours = Get_Leave_Length(new Timestamp(rsEmployeeInfo.getDate("datHiredDate").getTime()),
												  dPrevRate,
												  alStepUpMonth, 
												  alAccumuRates, 
												  tsStartTime 
												  //new Timestamp(datCutOff.getTime())
												  );
						break;
					}else{
						/* this step up falls out of the range, record its value then
						 * remove it from the array.
						 */
						dPrevRate = Double.parseDouble(alAccumuRates.get(i).toString());
						alStepUpMonth.remove(i);
						alAccumuRates.remove(i);
					}
				}
			}
			return dHours;
			
		}catch(Exception ex){
	    	System.out.println("<BR><BR>Error in EmployeeLeaveManager.CalculateLeaveTotal!!<BR>");
	    	System.out.println("Exception: " + ex.getMessage() + "<BR>");
	    	return 0;
		}
	}
	
	private static Timestamp FindStartDate(String sAP, Date datCutOff, Date datHiredDate){
		
		Calendar cCutOff = Calendar.getInstance();
		cCutOff.setTime(datCutOff);
		Timestamp tsStartTime;
		if (sAP.compareTo("calendaryear") == 0){
			//find the immediate Jannuary 1th
			//System.out.println("This first day of this year is: " + cCutOff.get(Calendar.YEAR) + "-01-01 00:00:00");
			tsStartTime = Timestamp.valueOf(cCutOff.get(Calendar.YEAR) + "-01-01 00:00:00.0");
			//if this employee is hired after the 01-01 of this year, 
			//use his hired date as the starting date
			if (tsStartTime.getTime() < datHiredDate.getTime()){
				tsStartTime = new Timestamp(datHiredDate.getTime());
			}
		}else if (sAP.compareTo("employeeyear") == 0){
			//find the leading anniversary date for this employee
			//System.out.println("Hired Date: " + datHiredDate);
			Calendar cHiredDate = Calendar.getInstance();
			cHiredDate.setTime(datHiredDate);
			String sDate = "";
			//System.out.println("cCutOff.get(Calendar.DAY_OF_YEAR) = " + cCutOff.get(Calendar.DAY_OF_YEAR));
			//System.out.println("cHiredDate.get(Calendar.DAY_OF_YEAR) = " + cHiredDate.get(Calendar.DAY_OF_YEAR));
			if (cCutOff.get(Calendar.MONTH) < cHiredDate.get(Calendar.MONTH)){
				//before anniversary
				sDate = sDate + (cCutOff.get(Calendar.YEAR) - 1);
			}else if (cCutOff.get(Calendar.MONTH) > cHiredDate.get(Calendar.MONTH)){
				//after anniversary
				sDate = sDate + cCutOff.get(Calendar.YEAR);
			}else if (cCutOff.get(Calendar.MONTH) == cHiredDate.get(Calendar.MONTH)){
				if (cCutOff.get(Calendar.DAY_OF_MONTH) < cHiredDate.get(Calendar.DAY_OF_MONTH)){
					//before anniversary
					sDate = sDate + (cCutOff.get(Calendar.YEAR) - 1);
				}else{
					//after anniversary
					sDate = sDate + cCutOff.get(Calendar.YEAR);
				}
			}
			/* LTO 20080520
			 * Use DAY_OF_YEAR is not going to be accurate because of leap years.
			if (cCutOff.get(Calendar.DAY_OF_YEAR) < cHiredDate.get(Calendar.DAY_OF_YEAR)){
				//before anniversary
				sDate = sDate + (cCutOff.get(Calendar.YEAR) - 1);
			}else{
				//after anniversary
				sDate = sDate + cCutOff.get(Calendar.YEAR);
			}
			*/
			
			if (cHiredDate.get(Calendar.MONTH) + 1 < 10){
				sDate = sDate + "-0" + (cHiredDate.get(Calendar.MONTH) + 1);
			}else{
				sDate = sDate + "-" + (cHiredDate.get(Calendar.MONTH) + 1);
			}
			if (cHiredDate.get(Calendar.DATE) < 10){
				sDate = sDate + "-0" + cHiredDate.get(Calendar.DATE);
			}else{
				sDate = sDate + "-" + cHiredDate.get(Calendar.DATE);
			}
			//System.out.println("Leading hired date is: " + sDate + " 00:00:00");
			tsStartTime = Timestamp.valueOf(sDate + " 00:00:00.0");
		}else{
			//how to deal with fiscal year?
			tsStartTime = new Timestamp(System.currentTimeMillis());
		}
		
		return tsStartTime;
	}
	
	private static String GetNewQueryString(String sCriteria, HttpServletRequest request){
		
		Enumeration<String> paramNames = request.getParameterNames();
		ArrayList<String> alParameters = new ArrayList<String>(0);
		
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			if (s.compareTo("SortBy") != 0 || s.compareTo("SortOrder") != 0){
				alParameters.add(s + "=" + request.getParameter(s));
			}
		}
		
		//now we have all the parameters except sort, we can 
		//reconstruct the URL with a new sort option.
		String sURL = "SortBy=" + sCriteria;
		if (Integer.parseInt(request.getParameter("SortOrder")) == 0 && 
			sCriteria.compareTo(request.getParameter("SortBy")) == 0){
			sURL = sURL + "&" + "SortOrder=1";
		}else{
			sURL = sURL + "&" + "SortOrder=0";
		}
		
		for (int i=0;i<alParameters.size();i++){
			sURL = sURL + "&" + alParameters.get(i).toString();
		}
		
		return sURL;
	}
	
	private static double Get_Leave_Length(Timestamp tsHiredDate,
										   double dStartRate,
										   ArrayList<Double> alDates, 
										   ArrayList<Double> alRates, 
										   Timestamp tsStartTime
										   //Timestamp tsEndTime
										   ){

		for (int i=0;i<alDates.size();i++){
			//System.out.println("alDates(" + i + ") = " + alDates.get(i).toString());
		}
		for (int i=0;i<alRates.size();i++){
			//System.out.println("alRates(" + i + ") = " + alRates.get(i).toString());
		}
		Calendar ctest = Calendar.getInstance();
		ctest.setTimeInMillis(tsStartTime.getTime());
		
		double dHours = 0;
		double dPeriodHours = 0;
		Calendar cPointer = Calendar.getInstance();
		Timestamp tsSmallStart = tsStartTime;
		
		for (int i=0;i<alDates.size();i++){
			Timestamp tsSmallEnd = Locate_Stepup_Time(tsHiredDate, Double.parseDouble(alDates.get(i).toString()));
			cPointer.setTimeInMillis(tsSmallStart.getTime());
			dPeriodHours = 0;
			while (cPointer.getTimeInMillis() < tsSmallEnd.getTime()){
				if (TimeCardUtilities.IsLeapYear(cPointer.get(Calendar.YEAR))){
					dPeriodHours = dPeriodHours + dStartRate / 366;
				}else{
					dPeriodHours = dPeriodHours + dStartRate / 365;
				}
				cPointer.setTimeInMillis(cPointer.getTimeInMillis() + 24 * 3600 * 1000);
			}
			dStartRate = Double.parseDouble(alRates.get(i).toString());
			tsSmallStart = new Timestamp(cPointer.getTimeInMillis());
			dHours = dHours + dPeriodHours;
		}
		/* deal with time frame from last step-up to the "End of Year".
		 * This "End of Year" is the day before next reset day. we need
		 * to calculate all the way to that day because we need to 
		 * reward in a lump sun way. We don't care if there is any other
		 * step-up before the "End of Year", that will be deal with when
		 * that time come.
		 */
		cPointer.setTimeInMillis(tsStartTime.getTime());
		cPointer.add(Calendar.YEAR, 1);
		Timestamp tsEndOfYear = new Timestamp(cPointer.getTimeInMillis());
		cPointer.setTimeInMillis(tsSmallStart.getTime());
		dPeriodHours = 0;
		while (cPointer.getTimeInMillis() < tsEndOfYear.getTime()){
			if (TimeCardUtilities.IsLeapYear(cPointer.get(Calendar.YEAR))){
				dPeriodHours = dPeriodHours + dStartRate / 366;
			}else{
				dPeriodHours = dPeriodHours + dStartRate / 365;
			}
			cPointer.setTimeInMillis(cPointer.getTimeInMillis() + 24 * 3600 * 1000);
		}
		dHours = dHours + dPeriodHours;
		return dHours;
	}
	
	private static Timestamp Locate_Stepup_Time(Timestamp tsStartTime, double dMonth){
		Calendar cStepUp = Calendar.getInstance();
		cStepUp.setTimeInMillis(tsStartTime.getTime());
		cStepUp.add(Calendar.MONTH, (int)dMonth);
		//the number 30 is in favor for the employee
		cStepUp.add(Calendar.DAY_OF_MONTH, (int)((dMonth - (int)dMonth) * 30.4375));
		
		Timestamp ts = new Timestamp(cStepUp.getTimeInMillis());
		
		return ts;
	}
}
