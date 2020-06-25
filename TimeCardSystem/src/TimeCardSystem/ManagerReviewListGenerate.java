package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.*;

public class ManagerReviewListGenerate extends HttpServlet {

	//MRL
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "deprecation" })
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    
	    HttpSession CurrentSession = request.getSession();

	    try {
	    	String sCurrentEmployee;
    		String sStartTime;
    		boolean bIncludeFinalized;

			double dWeeklySum = 0;
    		double dWeeklyRegular = 0;
    		double dWeeklyDouble = 0;
    		double dOverTime = 0;
    		
    		boolean TimeCalculatable = true;
			String sInTime;
			String sFormattedInTime;
			String sOutTime;  
			String sFormattedOutTime;
			String sINBGC;
			String sOUTBGC;
			boolean bInMissing;
			boolean bOutMissing;

    		double dDailySum = 0;
    		double dDailyRegular = 0;
    		double dDailyDouble = 0;
	    	
			Timestamp inTime;
			Timestamp outTime;
	    	
	    	String sSQL;
    		boolean bWorked;
	    	ArrayList<Date> cWorkDays = new ArrayList<Date>();

	    	ResultSet rs;
	    	ResultSet rsNotes;
	    	ResultSet rsEmployeeUnfinalizedTotal;
	    	ResultSet rsWorkDays;
	    	
	    	
	    	//Calculate time period
	    	SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy EEE");
	    	SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");

	    	Timestamp tsSelectedStartingDay = new Timestamp(Integer.parseInt(request.getParameter("SelectedStartingYear")) - 1900,
											                Integer.parseInt(request.getParameter("SelectedStartingMonth")) - 1,
											                Integer.parseInt(request.getParameter("SelectedStartingDay")),
											                0,
											                0,
											                0,
											                0);
		   Timestamp tsSelectedEndingDay = new Timestamp(Integer.parseInt(request.getParameter("SelectedEndingYear")) - 1900,
											             Integer.parseInt(request.getParameter("SelectedEndingMonth")) - 1,
											             Integer.parseInt(request.getParameter("SelectedEndingDay")),
											             23,
											             59,
											             59,
											             999999999);
	    	//Customized title
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
					   "Transitional//EN\">" +
				       "<HTML>" +
				       "<HEAD><TITLE>Manager Review List</TITLE></HEAD>\n<BR>" + 
					   "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\"" +
					   " style = \" font-family:" + TimeCardUtilities.BASE_FONT_FAMILY + "; \">" +
					   "<TABLE BORDER=0 WIDTH=100%><TR><TD VALIGN=BOTTOM><FONT SIZE=4><B>Manager Review List</B></FONT></TD>" +
					   "<TD VALIGN=BOTTOM><B>&nbsp;&nbsp;&nbsp;&nbsp;" + 
					   "<FONT SIZE=2><B>Start Date:</B> " + USDateOnlyformatter.format(new Date(tsSelectedStartingDay.getTime())) +
					   "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
					   "<B>End Date:</B> " + USDateOnlyformatter.format(new Date(tsSelectedEndingDay.getTime())) + 
					   "</FONT></TD></TR>" +
					   "<TR><TD><Font SIZE=1><A HREF=\"" 
					   	+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
					   	+ "TimeCardSystem.AdminMain\">Return to main menu</A></FONT></TD></TR><TR>");
	    	/*
	    	c.setTime(tsSelectedStartingDay);
	    	String sStartingDate = SQLDateformatter.format(c.getTime());
	    	c.setTime(tsSelectedEndingDay);
	    	c.add(Calendar.DAY_OF_MONTH, 1);
	    	String sEndingDate = SQLDateformatter.format(c.getTime());
	    	*/
	    	//construct an array of working days(weekdays) during this time period.
	  
	    	Timestamp tsD = new Timestamp(tsSelectedStartingDay.getTime());
	    	while (tsD.getTime() < tsSelectedEndingDay.getTime()) {
	    		if (tsD.getDay() > 0 && tsD.getDay()< 6){
					cWorkDays.add(new Date(tsD.getTime()));
					//System.out.println("added: " + d.toString());
	    		}
	    		tsD.setTime(tsD.getTime() + 3600 * 1000 * 24); //add one day	    
	    	}
	    	//for (int i=0;i<cWorkDays.size();i++){
	    	//	System.out.println(i + ": " + cWorkDays.get(i));
	    	//}

	    	//get current URL
	    	String sCurrentURL;
	    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
	    	
	    	//System.out.println("[1543950483] - request.getRequestURI().toString() = '" + request.getRequestURI().toString() + "'");
	    	//System.out.println("[1543950484] - sCurrentURL = '" + sCurrentURL + "'");
	    	
	    	//sCurrentURL = sCurrentURL.replaceAll("&", "*");
	    	
	    	//get list of special time entry type
	    	sSQL = TimeCardSQLs.Get_Time_Entry_Type_Info_SQL();
    		ResultSet rsSTET = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

	    	//get all employees in the selected department.
	    	if (request.getParameter("SelectedEmployee").compareTo("") != 0){
	    		sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("SelectedEmployee"));
	    	}else{ 
				Enumeration<?> paramNames = request.getParameterNames();
	    		ArrayList<String> alDepartments = new ArrayList<String>(0);
	    		while(paramNames.hasMoreElements()) {
	    			String s = paramNames.nextElement().toString();
	    	        if (s.substring(0, 2).compareTo("!!") == 0){
	    	        	//if "all departments" is selected, disregard all other selections.
	    	        	if (s.substring(2).compareTo("ALLDEPT") == 0){
	    	        		alDepartments.clear();
	    	        		alDepartments.add(s.substring(2));
	    	        		//System.out.println("All departments selected");
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
	   	
	    	out.println("<TABLE BORDER=1 WIDTH=100%>");
	    	
	    	double dGrandRegular = 0;
	    	double dGrandDouble = 0;
	    	double dGrandTotal = 0;
	    	double dGrandOver = 0;
	    	double dGrandLeave = 0;
	    	
	    	double dCurrentRegular = 0;
	    	double dCurrentDouble = 0;
	    	double dCurrentTotal = 0;
	    	double dCurrentOver = 0;
    		double dLeaveTotal = 0;
	    	
	    	while (EmployeeList.next()){
	    		
	    		sCurrentEmployee = EmployeeList.getString("sEmployeeID");
	    		sStartTime = EmployeeList.getString("tStartTime");
	    		
	    		//get all punch in and out for current employee
	    		if (request.getParameter("IncludeFinalized") == null){
	    			//don't include finalized time entries
	    			bIncludeFinalized = false;
	    		}else{
	    			//include finalized time entries
	    			bIncludeFinalized = true;
	    		}
	    		if (Integer.parseInt(request.getParameter("SelectedTimeEntryType")) == 0){
		    		sSQL = TimeCardSQLs.Get_Employee_TimeEntry_List_SQL(sCurrentEmployee, 
		    															tsSelectedStartingDay.toString(), 
		    															tsSelectedEndingDay.toString(), 
			    				                                        false,
			    				                                        bIncludeFinalized);
	    		}else{
	    			sSQL = TimeCardSQLs.Get_Employee_TimeEntry_List_SQL(sCurrentEmployee,  
																		tsSelectedStartingDay.toString(), 
																		tsSelectedEndingDay.toString(), 
											                            false,
											                            bIncludeFinalized,
											                            request.getParameter("SelectedTimeEntryType"));
	    		}
	    		rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		
	    		//if the following 3 conditions are all true, don't display this employee
	    		if (!rs.next() &&		//there is no time entry for this employee in the selected time frame
	    			EmployeeList.getInt("iActive") == 0 && //the employee is currently inactive 
	    			request.getParameter("SelectedEmployee").compareTo("") == 0 //the employee is not purposely selected
	    			){
	    			//no time entry for an inactive employee, don't display anything
	    			rs.close();
	    		}else{
	    			rs.beforeFirst();
	    		
		    		out.println("<TR><TD><A NAME=\"" + sCurrentEmployee + "\"/>");
		    		out.println("<TABLE BORDER=0 WIDTH=100%>");
		    		out.println("<TR>");
		    		out.println("<TD><Font SIZE=2><B>Employee ID:</B> " + sCurrentEmployee + "</FONT></TD>");
		    		out.println("<TD><Font SIZE=2><B>Employee Name:</B> " + EmployeeList.getString(Employees.sEmployeeLastName) + ", " + EmployeeList.getString(Employees.sEmployeeFirstName) + " " + EmployeeList.getString(Employees.sEmployeeMiddleName) + "</FONT></TD>");
		    		out.println("<TD><Font SIZE=2><B>Starting Time:</B> " + sStartTime + "</FONT></TD>");
		    		out.println("</TR></TABLE>");
		    		//print out the title row for time entires
		    		out.println("<TABLE BORDER=0 WIDTH=100%>");
		    		String sNewInLink;
		    		String sNewOutLink;
		    		if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
														AccessControlFunctionList.ManagerReviewListTimeEditing)){
		    			sNewInLink = "<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?EmployeeID=" + sCurrentEmployee + "&Type=0&SpecialEntryType=0&EarlyStart=0&LateFlag=0&id=0&Date=NA&OriginalURL=" + sCurrentURL + "\"> IN </A>";
		    			sNewOutLink = "<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?EmployeeID=" + sCurrentEmployee + "&Type=1&SpecialEntryType=0&id=0&Date=NA&OriginalURL=" + sCurrentURL + "\"> OUT </A>";
		    		}else{
		    			sNewInLink = "IN";
		    			sNewOutLink = "OUT";
		    		}
		    		out.println("<TR><TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Flag</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Type</B></FONT></TD>" +
		    						"<TD WIDTH =26% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>" + sNewInLink + "</B></FONT></TD>" +
		    						"<TD WIDTH =18% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>" + sNewOutLink + "</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Late</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Regular</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Double</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Total</B></FONT></TD>" +
		    						"<TD WIDTH =17% VALIGN=TOP><Font SIZE=1><B>Notes</B></FONT></TD>" +
		    						"<TD WIDTH =15% VALIGN=TOP><Font SIZE=1><B>Admin Notes</B></FONT></TD></TR>");
	
	    			dWeeklySum = 0;
	        		dWeeklyRegular = 0;
	        		dWeeklyDouble = 0;
	        		dOverTime = 0;
	        		dLeaveTotal = 0;
	        		
	        		
	        		while (rs.next()){
	        			TimeCalculatable = true;
	
	    	    		dDailySum = 0;
	    	    		dDailyRegular = 0;
	    	    		dDailyDouble = 0;
	    				
	        			//set null time to be "NA" so they will be default to current time and date in the next page
	        			if (rs.getString(TimeEntries.dtInTime).compareTo("0000-00-00 00:00:00") == 0){
	    					sInTime = "NA";
	    					sFormattedInTime = "No punch in record";
	    					TimeCalculatable = false;
	    					bInMissing = true;
	    					sINBGC = "BGCOLOR=RED";
	    				}else{
	    					sInTime = rs.getString(TimeEntries.dtInTime);
	    					sFormattedInTime = USDateformatter.format(Timestamp.valueOf(rs.getString(TimeEntries.dtInTime)));
	    					bInMissing = false;
	    					sINBGC = "";
	    				}
	        			
	        			if (rs.getString(TimeEntries.dtInTimeOri).compareTo("0000-00-00 00:00:00") != 0){
	        				//if there is raw in-time available, augment them after the regular time.
	    					sFormattedInTime = sFormattedInTime + " (" + USTimeOnlyformatter.format(Timestamp.valueOf(rs.getString("dtInTimeOri"))) + ")";
	    				}
	        			
	    				if (rs.getString("dtOutTime").compareTo("0000-00-00 00:00:00") == 0){
	    					sOutTime = "NA";
	    					sFormattedOutTime = "No punch Out record";
	    					TimeCalculatable = false;
	    					bOutMissing = true;
	    					sOUTBGC = "BGCOLOR=RED";
	    				}else{
	    					sOutTime = rs.getString("dtOutTime");
	    					sFormattedOutTime = USDateformatter.format(Timestamp.valueOf(rs.getString("dtOutTime")));
	    					bOutMissing = false;
	    					sOUTBGC = "";
	    				}
	            		
	    				out.println("<TR>");
	    				//flags
	    				out.println("<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1>");
	    				if (rs.getInt("iEarlyStart") == 1){
	    					out.print("E");
	    				}
	    				if (rs.getString("sPeriodDate").compareTo(TimeCardUtilities.TEMPORARY_POSTING_DATE) == 0){
	    					out.print("P");
	    				}else if(rs.getString("sPeriodDate").compareTo("0000-00-00") == 0){
	    					out.println("&nbsp;");
	    				}else{
	    					out.println("F");
	    				}
	    				if (rs.getInt("iLate") != 0){
	    					//attach a link to This leter to remove itself if applicable
	    		    		if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
	    														AccessControlFunctionList.ManagerReviewListTimeEditing)){
	    						out.print("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LateFlagChange?ID=" + rs.getInt("id") + "&CurrentStatus=1" + "&OriginalURL= " + sCurrentURL + "&EmployeeID=" + sCurrentEmployee + "\">L</A>");
	    					}else{
	    						out.print("L");
	    					}
	    				}
	    				if (!TimeCalculatable){
	    					out.print("M");
	    				}
	    				out.println("</FONT></TD>");
	    				
	    				//check for labels for Special Entry Type
	    				out.println("<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1 COLOR=" + SMMasterStyleSheetDefinitions.GOOGLE_RED + ">");
	    				if (rs.getInt("iEntryTypeID") != 0){
	    					rsSTET.beforeFirst();
	    					while (rsSTET.next()){
	    						if (rsSTET.getInt("iTypeID") == rs.getInt("iEntryTypeID")){
	    							out.println(rsSTET.getString("sTypeTitle"));
	    							break;
	    						}
	    					}
	    				}else{
	    					out.println("&nbsp;");
	    				}
	    				out.println("</FONT></TD>");
	    				//In time
	    				out.println("<TD ALIGN=CENTER VALIGN=TOP");
	    				out.print(" " + sINBGC + "><Font SIZE=1>");
	    				out.print("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?Type=0&EarlyStart=" + rs.getInt("iEarlyStart") + "&LateFlag=" + rs.getInt("iLate") + "&SpecialEntryType=" + rs.getInt("iEntryTypeID") + "&id=" + rs.getInt("id") + "&Date=" + sInTime + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">");
	    				if (bInMissing){
	    					out.print("<FONT SIZE=2><b>");
	    				}
	    				if (rs.getInt("iInModified") == 0){
	    					out.println(sFormattedInTime);
	    				}else{
	    					out.println("* " + sFormattedInTime);
	    				}
	    				if (bInMissing){
	    					out.print("</b></FONT>");
	    				}
	            		//Out time		
	            		out.println("</FONT></TD><TD ALIGN=CENTER VALIGN=TOP");
	    				out.println(" " + sOUTBGC + "><Font SIZE=1>");
	    				out.print("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?Type=1&id=" + rs.getInt("id") + "&Date=" +  sOutTime + "&SpecialEntryType=" + rs.getInt("iEntryTypeID") + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">");
	    				if (bOutMissing){
	    					out.print("<FONT SIZE=2><b>");
	    				}
						if (rs.getInt("iOutModified") == 0){
							out.println(sFormattedOutTime);
						}else{
							out.println("* " + sFormattedOutTime);
						}
	    				if (bOutMissing){
	    					out.print("</b></FONT>");
	    				}
						out.println("</FONT></TD>");
	    					
	    				//late minutes
						if (rs.getInt("iLate") != 0){
							out.println("<TD VALIGN=TOP ALIGN=CENTER><Font SIZE=1><B>" + rs.getInt("iLateMinute") + "</B></FONT></TD>");
						}else{
							out.println("<TD VALIGN=TOP ALIGN=CENTER></TD>");
						}
						
	    				if (TimeCalculatable){
	    					//calculate the time
	    					inTime = Timestamp.valueOf(rs.getString("dtInTime"));
	    					outTime = Timestamp.valueOf(rs.getString("dtOutTime"));
	    					
	    					dDailySum = outTime.getTime() - inTime.getTime();
	    					
	    					//One half hour is deducted for lunch from anyday on which an employee worked more than 5 hours total.
	    					//If either in or out, or both are on sunday, don't consider lunch
	    					if (dDailySum > 18000000 && inTime.getDay() != 0 && outTime.getDay() != 0){
	    						//out.println(dDailySum);
	    						dDailySum = dDailySum - 1800000;
	    					}
	    					//calculate double time
	    					dDailyDouble = TimeCardUtilities.CalculateDoubleTime(rs.getString("dtInTime"), rs.getString("dtOutTime"), sStartTime) /1.0;
	    					//System.out.println("returned double: " + dDailyDouble);
	    					//calculate regular time
	    					dDailyRegular = dDailySum - dDailyDouble * 60 * 1000;
	    					
	    					//calculate the weekly total 
	    					dWeeklySum = dWeeklySum + dDailySum;
	    					dWeeklyDouble = dWeeklyDouble + dDailyDouble;
	    					dWeeklyRegular = dWeeklyRegular + dDailyRegular;
	    					
	    					//display time
	    					//Reg
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>" + TimeCardUtilities.RoundHalfUp(dDailyRegular / 3600 / 1000, 2) + "</B></FONT></TD>");
	    					//Double
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>" + TimeCardUtilities.RoundHalfUp(dDailyDouble / 60.0, 2) + "</B></FONT></TD>");
	    					//Total
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>" + TimeCardUtilities.RoundHalfUp(dDailySum / 3600 / 1000, 2) + "</B></FONT></TD>");
	    					
	    				}else{
	    					//Reg
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>0.0</B></FONT></TD>");
	    					//double
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>0.0</B></FONT></TD>");
	    					//Total
	    					out.println("<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1><B>0.0</B></FONT></TD>");
	    				}
	    				
	    				//find all the special notes for this record if there is any.
	    				sSQL = "Select * FROM Notes, SpecialNoteTypes WHERE iNoteTypeID = iTypeID AND iLinkID = '" + rs.getInt("id") + "'";
	    				rsNotes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    				out.println("<TD VALIGN=TOP><Font SIZE=1>");
	    				if (rsNotes.next()){
	    					//administrator are allowed to see and change notes, but managers can only see.

	    		    		if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
	    														AccessControlFunctionList.ManagerReviewListTimeEditing) ||
								TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
	    														AccessControlFunctionList.ManagerReviewListNoteEditing)){
								out.println("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteEdit?IsAdmin=1&LinkID=" + rs.getInt("id") + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">"); 
	    					}
							out.println(rsNotes.getString("mNote"));
	    					while (rsNotes.next()){
	    						out.print(" || " + rsNotes.getString("mNote"));
	    					}
	    					if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
																AccessControlFunctionList.ManagerReviewListTimeEditing) ||
								TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
	    														AccessControlFunctionList.ManagerReviewListNoteEditing)){
	    						out.println("</A>");
	    					}
	    				}else{
	    					//add a link for administrator to start new note
	    					if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
																AccessControlFunctionList.ManagerReviewListTimeEditing) ||
								TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
	    														AccessControlFunctionList.ManagerReviewListNoteEditing)){
	    						out.println("<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteEdit?IsAdmin=1&LinkID=" + rs.getInt("id") + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">+</A>");
	    					}
	    				}
	    				out.println("</FONT></TD>");
	    				
	    				//Attach admin change log
	    				if (rs.getString("mChangeLog").trim().compareTo("") !=0){
	    					out.println ("<TD VALIGN=TOP><Font SIZE=1>" + rs.getString("mChangeLog").trim() + "</FONT></TD>");
	    				}else{
	    					out.println ("<TD></TD>");
	    				}
	    				out.println ("</TR>");
	    				
	    				rsNotes.close();
	            	}
	
	        		//create the leave time list, but don't print it yet, so we can include 
	        		//the leave total into our calculation.
	        		String tempHTMLString = "<TR><TD COLSPAN=10><HR></TD></TR>" + 
	    	    							"<TR><TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>ID#</B></FONT></TD>" +
				    							"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Type</B></FONT></TD>" +
				    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Start Date</B></FONT></TD>" +
				    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>End Date</B></FONT></TD>" +
				    	    					"<TD ALIGN=CENTER VALIGN=TOP COLSPAN=3><Font SIZE=1><B>&nbsp;</B></FONT></TD>" +
				    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>Duration</B></FONT></TD>" +
				    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><B>&nbsp;</B></FONT></TD>" + 
				    	    					"<TD ALIGN=LEFT VALIGN=TOP><Font SIZE=1><B>Note</B></FONT></TD>" + 
				    	    				"</TR>";
	    	    	//get recordset
	        		//System.out.println(">>>>>>>>>>Selected End Date: " + SelectedEndingDay.toString());
	    	    	sSQL = TimeCardSQLs.Get_Leave_Adjustments_SQL(sCurrentEmployee, 
	    	    												  -1, //select every type 
	    	    												  tsSelectedStartingDay.toString(), 
	    	    												  tsSelectedEndingDay.toString(), 
																  "dtInTime", 
																  0,
																  bIncludeFinalized);
	    	    	ResultSet rsLeaves = null;
					try {
						rsLeaves = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					} catch (Exception e) {
						out.println("Error [1429029856] with SQL: '" + sSQL + " - " + e.getMessage() + "<BR>");
					}
	
	    	    	while (rsLeaves.next()){
	    	    		tempHTMLString = tempHTMLString + 
	    	    					
	    	    					"<TR><TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveEdit?Employee=" + sCurrentEmployee + 
														    																		"&id=" + rsLeaves.getInt("id") +
														    																		"&OriginalURL=" + sCurrentURL +
														    																		"\">" + rsLeaves.getInt("id") + "</A></FONT></TD>" +
	    	    						"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1>" + rsLeaves.getString("sTypeTitle");
					    	    		if (rsLeaves.getInt("iSpecialAdjustment") == 1){
					    	    			tempHTMLString = tempHTMLString + "(SA)";
					    	    		} 
					    	    		
					    	    		tempHTMLString = tempHTMLString + "</FONT></TD>" +
		    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1>" + USDateOnlyformatter.format(rsLeaves.getDate("dtInTime")) + "</FONT></TD>" +
		    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1>" + USDateOnlyformatter.format(rsLeaves.getDate("dtOutTime")) + "</FONT></TD>" +
		    	    					"<TD ALIGN=CENTER VALIGN=TOP COLSPAN=3><Font SIZE=1>&nbsp;</FONT></TD>" +
		    	    					"<TD ALIGN=RIGHT VALIGN=TOP><Font SIZE=1>" + TimeCardUtilities.RoundHalfUp(rsLeaves.getDouble("dDuration"), 2); 
				    	    		if (rsLeaves.getInt("iPaidLeave") != 1 || rsLeaves.getInt("iSpecialAdjustment") == 1){
				    	    			tempHTMLString = tempHTMLString + "*";
				    	    		} 
				    	    		tempHTMLString = tempHTMLString + 
				    	    			"</FONT></TD>" +
		    	    					"<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1>&nbsp;</FONT></TD>" + 
		    	    					"<TD ALIGN=LEFT VALIGN=TOP><Font SIZE=1>" + rsLeaves.getString("mNote") + "</FONT></TD>" + 
		    	    				"</TR>";
	    	    		if (rsLeaves.getInt("iPaidLeave") == 1 && rsLeaves.getInt("iSpecialAdjustment") != 1){
	    	    			dLeaveTotal = dLeaveTotal + rsLeaves.getDouble("dDuration");
	    	    		}
	    	    	}
	    	    	tempHTMLString = tempHTMLString + "<TR><TD COLSPAN=3></TD><TD COLSPAN=5><HR></TD></TR>" +
	    	    									  "<TR><TD COLSPAN=3></TD><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=2><Font SIZE=1><B>Total Leave Time</B></FONT></TD><TD ALIGN=RIGHT COLSPAN=3><Font SIZE=1><B>" + TimeCardUtilities.RoundHalfUp(dLeaveTotal, 2) + "</B></FONT></TD></TR>";
	    	    	/*
	    	    	System.out.println("WeeklySum = " + TimeCardUtilities.RoundHalfUp(dWeeklySum / 3600 / 1000, 2));
	    	    	System.out.println("WeeklyRegular = " + TimeCardUtilities.RoundHalfUp(dWeeklyRegular / 3600 / 1000, 2));
	    	    	System.out.println("WeeklyDouble = " + TimeCardUtilities.RoundHalfUp(dWeeklyDouble / 60.0, 2));
	    	    	System.out.println("WeeklyOver = " + TimeCardUtilities.RoundHalfUp(dOverTime / 3600 / 1000, 2));
	    	    	System.out.println("LeaveTotal = " + dLeaveTotal);
	    	    	*/
	        		//calculate overtime, adjust regular time
	        		if (dWeeklyRegular + dLeaveTotal * 3600000 > 40 * 3600000){
	        			dOverTime = dWeeklyRegular + (dLeaveTotal - 40) * 3600000;
	        			dWeeklyRegular = (40 - dLeaveTotal) * 3600000;
	        		}else{
	        			dOverTime = 0;
	        		}
	        		/*
	    	    	System.out.println("WeeklySum = " + TimeCardUtilities.RoundHalfUp(dWeeklySum / 3600 / 1000, 2));
	    	    	System.out.println("WeeklyRegular = " + TimeCardUtilities.RoundHalfUp(dWeeklyRegular / 3600 / 1000, 2));
	    	    	System.out.println("WeeklyDouble = " + TimeCardUtilities.RoundHalfUp(dWeeklyDouble / 60.0, 2));
	    	    	System.out.println("WeeklyOver = " + TimeCardUtilities.RoundHalfUp(dOverTime / 3600 / 1000, 2));
	    	    	System.out.println("LeaveTotal = " + dLeaveTotal);
	    	    	*/
	        		//insert a horizontal line
	        		out.println("<TR><TD COLSPAN=3></TD><TD COLSPAN=5><HR></TD></TR>");
	        		out.println("<TR><TD COLSPAN=5></TD><TD><Font SIZE=1><B>Regular</B></FONT></TD><TD><Font SIZE=1><B>Double</B></FONT></TD><TD><Font SIZE=1><B>W.Total</B></FONT></TD><TD><Font SIZE=1><B>OverTime</B></FONT></TD></TR>");
	        		//print out totals for this employee
	        		//out.println("<TR><TD COLSPAN=2></TD><TD COLSPAN=3><TABLE Border=1>");
	        		dCurrentRegular = TimeCardUtilities.RoundHalfUp(dWeeklyRegular / 3600 / 1000, 2);
	        		dCurrentDouble = TimeCardUtilities.RoundHalfUp(dWeeklyDouble / 60.0, 2);
	        		dCurrentTotal = TimeCardUtilities.RoundHalfUp(dWeeklySum / 3600 / 1000, 2);
	        		dCurrentOver = TimeCardUtilities.RoundHalfUp(dOverTime / 3600 / 1000, 2);
	        		
	        		out.println("<TR><TD COLSPAN=3></TD><TD ALIGN=RIGHT COLSPAN=2><Font SIZE=1><B>Calculated Totals:</B></FONT></TD>");
	        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><B>" + dCurrentRegular + "</B></FONT></TD>");
	        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><B>" + dCurrentDouble + "</B></FONT></TD>");
	        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><B>" + dCurrentTotal + "</B></FONT></TD>");
	        		out.println("<TD COLSPAN=2><TABLE BORDER=0 WIDTH=100%><TR><TD WIDTH=15% ALIGN=RIGHT><Font SIZE=1><B>" + dCurrentOver + "</B></FONT></TD><TD></TD></TR></TABLE></TD></TR>");
	        		
	        		//print out prepared leave section here
	        		out.println(tempHTMLString);
	        		
	        		//if there is saved unfinalized totals, display them too.
	        		sSQL = TimeCardSQLs.Get_Period_Total_Time_SQL(sCurrentEmployee);
	        		rsEmployeeUnfinalizedTotal = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));;
	        		
	        		if (rsEmployeeUnfinalizedTotal.next()){
	
	            		dCurrentRegular = rsEmployeeUnfinalizedTotal.getDouble("dPeriodRegular");
	            		dCurrentDouble = rsEmployeeUnfinalizedTotal.getDouble("dPeriodDouble");
	            		dCurrentTotal = rsEmployeeUnfinalizedTotal.getDouble("dPeriodTotal");
	            		dCurrentOver = rsEmployeeUnfinalizedTotal.getDouble("dPeriodOverTime");
	            		dLeaveTotal = rsEmployeeUnfinalizedTotal.getDouble("dPeriodLeave");
	
	            		out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
	            		out.println("<TR><TD COLSPAN=5></TD><TD><Font SIZE=1><B>Regular</B></FONT></TD>" +
	            										   "<TD><Font SIZE=1><B>Double</B></FONT></TD>" +
	            										   "<TD><Font SIZE=1><B>Total</B></FONT></TD>" +
	            										   "<TD COLSPAN=2><TABLE BORDER=0 WIDTH=100%><TR><TD WIDTH=20%><Font SIZE=1><B>Overtime</B></FONT></TD>" +
	            										   												"<TD WIDTH=20%><Font SIZE=1><B>Leave</B></FONT></TD><TD></TD></TR></TABLE></TD></TR>");
		        		out.println("<TR><TD COLSPAN=3></TD><TD ALIGN=RIGHT COLSPAN=2><Font SIZE=1><B>Saved Totals:</B></FONT></TD>");
		        		out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalSave\">");
		        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><INPUT SIZE=5 TYPE=TEXT NAME=AdjustedRegular VALUE=" + dCurrentRegular + "></FONT></TD>");
		        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><INPUT SIZE=5 TYPE=TEXT NAME=AdjustedDouble VALUE=" + dCurrentDouble + "></FONT></TD>");
		        		out.println("<TD ALIGN=RIGHT><Font SIZE=1><INPUT SIZE=5 TYPE=TEXT NAME=AdjustedTotal VALUE=" + dCurrentTotal + "></FONT></TD>");
		        		
		        		out.println("<TD COLSPAN=2><TABLE BORDER=0 WIDTH=100%><TR><TD WIDTH=15%><Font SIZE=1><INPUT SIZE=5 TYPE=TEXT NAME=AdjustedOver VALUE=" + dCurrentOver + "></FONT></TD>");
		        		out.println("<TD WIDTH=15%><Font SIZE=1><INPUT SIZE=5 TYPE=TEXT NAME=AdjustedLeave VALUE=" + dLeaveTotal + "></FONT></TD>");	        		
		        		out.println("<INPUT TYPE=HIDDEN NAME=\"id\" VALUE=" + rsEmployeeUnfinalizedTotal.getInt("id") + ">");
		        		out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + sCurrentURL  + "#" + sCurrentEmployee + "\">");
		        		out.println("<TD ALIGN=CENTER WIDTH=20%><INPUT TYPE=\"SUBMIT\" VALUE=\"Save\"></TD>");
		        		out.println("</FORM>");
		        		out.println("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PeriodTotalFinalize\">");
		        		out.println("<TD ALIGN=CENTER WIDTH=20%>");
		        		out.println("<INPUT TYPE=HIDDEN NAME=\"RecID\" VALUE=" + rsEmployeeUnfinalizedTotal.getInt("id") + ">");
		        		out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + sCurrentURL  + "#" + sCurrentEmployee + "\">");
		        		out.println("<Font SIZE=1><INPUT TYPE=\"SUBMIT\" VALUE=\"Finalize\"></FONT></TD></FORM></TR></TABLE></TD></TR>");
		        		
	        		}
	        		out.println("</TABLE><BR>");
	        		
	        		if (Integer.parseInt(request.getParameter("SelectedTimeEntryType")) == 0){
	        			//Missing Days list if there is any
		        		out.println("<Table BORDER=0 WIDTH=30%><TR><TD><Font SIZE=1><B>Missing Days</B></FONT></TD><TR>");
		        		//get individual workday list
		        		sSQL = TimeCardSQLs.Get_Employee_Workday_List_SQL(sCurrentEmployee, tsSelectedStartingDay.toString(), tsSelectedEndingDay.toString());
		        		rsWorkDays = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		        		
		        		boolean bMissedAny = false;
		        		for (int i=0; i<cWorkDays.size(); i++){
		        			
		        			//reset resultset for loop-through
		        			rsWorkDays.beforeFirst();
		        			rsLeaves.beforeFirst();
		            		bWorked = false;
		            		
		        			flag1: while (rsWorkDays.next()){
		        				if (rsWorkDays.getString("dtInTime").compareTo("0000-00-00 00:00:00") != 0){
		        					sInTime = rsWorkDays.getDate("dtInTime").toString();
		        				}else{
		        					sInTime = "0000-00-00";
		        				}
		        				if (rsWorkDays.getString("dtOutTime").compareTo("0000-00-00 00:00:00") != 0){
		        					sOutTime = rsWorkDays.getDate("dtOutTime").toString();
		        				}else{
		        					sOutTime = "0000-00-00";
		        				}
		        				
		        				//System.out.println ("Workday: " + cWorkDays.get(i));
		        				//System.out.println ("InTime: " + sInTime);
		        				//System.out.println ("OutTime: " + sOutTime);
		        				
		        				if (cWorkDays.get(i).toString().compareTo(sInTime) == 0 ||
		        					cWorkDays.get(i).toString().compareTo(sOutTime) == 0 ||
		        				   (cWorkDays.get(i).toString().compareTo(sInTime) > 0 && 
		        					cWorkDays.get(i).toString().compareTo(sOutTime) < 0)){
		            					bWorked = true;
		            					break flag1;
		        				}	
		        			}
		            		
		            		flag2: while (rsLeaves.next()){
		        				if (rsLeaves.getString("dtInTime").compareTo("0000-00-00") != 0){
		        					sInTime = rsLeaves.getDate("dtInTime").toString();
		        				}else{
		        					sInTime = "0000-00-00";
		        				}
		        				if (rsLeaves.getString("dtOutTime").compareTo("0000-00-00") != 0){
		        					sOutTime = rsLeaves.getDate("dtOutTime").toString();
		        				}else{
		        					sOutTime = "0000-00-00";
		        				}
		        				if (cWorkDays.get(i).toString().compareTo(sInTime) >= 0 && 
		        					cWorkDays.get(i).toString().compareTo(sOutTime) <= 0){
		            					bWorked = true;
		            					break flag2;
		        				}	
		        			}
		            		
		        			if (!bWorked){
		        				//output this day as a missing day
		        				if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
									AccessControlFunctionList.ManagerReviewListTimeEditing)){
		        					out.println("<TR><TD><Font SIZE=1>"
		        						//Add a regular day
		        						+ "<B><A HREF=\"" 
		        						+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
		        						+ "TimeCardSystem.AddDefaultPunch?FullDay=1&" + TimeCardUtilities.REQUEST_PARAMETER_EID + "=" + sCurrentEmployee 
		        						+ "&StartTime=" + sStartTime 
		        						+ "&FocusDate=" + cWorkDays.get(i) 
		        						+ "&OriginalURL=" + sCurrentURL + "\">&nbsp;<+W>&nbsp;</A>"
		        						//Add a vacation day
		        						+ "<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
		        						+ "TimeCardSystem.AddDefaultLeave?" + TimeCardUtilities.REQUEST_PARAMETER_EID + "=" + sCurrentEmployee 
		        						+ "&FocusDate=" + cWorkDays.get(i)
		        						+ "&OriginalURL=" + sCurrentURL + "\">&nbsp;<+V>&nbsp;</A>"
		        						//Add a holiday:
		        						+ "<A HREF=\"" 
		        						+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
		        						+ "TimeCardSystem.AddDefaultPunch?FullDay=1&" + TimeCardUtilities.REQUEST_PARAMETER_EID + "=" + sCurrentEmployee 
		        						+ "&StartTime=" + sStartTime 
		        						+ "&FocusDate=" + cWorkDays.get(i)
		        						+ "&" + AddDefaultPunch.ENTRY_TYPE_TITLE + "=" + AddDefaultPunch.ENTRY_TYPE_TITLE_HOLIDAY
		        						+ "&OriginalURL=" + sCurrentURL + "\">&nbsp;<+H>&nbsp;</A>"
		        						+ cWorkDays.get(i) + "</B></FONT></TD><TR>");
		        				}else if (TimeCardUtilities.IsAccessible((ResultSet) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
																		  AccessControlFunctionList.ManagerReviewListNoteEditing)){
		        					out.println("<TR><TD><Font SIZE=1><B>" + cWorkDays.get(i) + "<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
		        						+ "TimeCardSystem.AddDefaultPunch?FullDay=0&" + TimeCardUtilities.REQUEST_PARAMETER_EID + "=" 
		        						+ sCurrentEmployee + "&StartTime=" + sStartTime + "&FocusDate=" + cWorkDays.get(i) + "&OriginalURL=" + sCurrentURL 
		        						+ "\"> <+W> </A></B></FONT></TD><TR>");
		        				}else{
		        					//no choice here for no privileged user
		        				}
		        				bMissedAny = true;
		        			}
		        			//System.out.println("checking for " + i + " - " + ((Date)cWorkDays.get(i)).toString());
		        			
		        		}
		        		if (!bMissedAny){
		        			out.println("<TR><TD><Font SIZE=1>No Missing Day</FONT></TD><TR>");
		        		}
		    	    	rsWorkDays.close();
		    	    	rsLeaves.close();
		    	    	out.println("</TABLE></TD></TR>");
	        		}
	        		out.println("<TR><TD ALIGN=RIGHT><FONT SIZE=2><B>Grand Total for " + EmployeeList.getString("sEmployeeFirstName") + " "  + EmployeeList.getString("sEmployeeLastName") + " : " + TimeCardUtilities.RoundHalfUp((dLeaveTotal + dCurrentTotal), 2) + " HOURS</B></TD></TR>");
	        		
	        		
	        		out.println("</TD></TR>"); //end of each employee
	        		//accumulate grand totals
	        		dGrandRegular = dGrandRegular + dCurrentRegular;
	        		dGrandDouble = dGrandDouble + dCurrentDouble;
	        		dGrandTotal = dGrandTotal + dCurrentTotal + dLeaveTotal;
	        		dGrandOver = dGrandOver + dCurrentOver;
	        		dGrandLeave = dGrandLeave + dLeaveTotal;
	        		
	    	    	//close all the resultset
	    	    	rs.close();
	    	    	rsEmployeeUnfinalizedTotal.close();
	    		}
    		}
	    	
	    	EmployeeList.close();
    		rsSTET.close();
	    	
	    	//display grand totals for displayed employees
	    	out.println("<TR><TD><TABLE BORDER=0 WIDTH=95%><TR><TD></TD></TR>");
	    	out.println("<TR ALIGN=RIGHT><TD WIDTH=40% ALIGN=CENTER><FONT SIZE=5><B> GRAND TOTALS : </B></FONT></TD>" +
	    									"<TD WIDTH=12%><FONT SIZE=4><B>Regular Time</B></FONT></TD>" +
	    									"<TD WIDTH=12%><FONT SIZE=4><B>Double Time</B></FONT></TD>" +
	    									"<TD WIDTH=12%><FONT SIZE=4><B>Over Time</B></FONT></TD>" +
	    									"<TD WIDTH=12%><FONT SIZE=4><B>Leave Time</B></FONT></TD>" +
	    									"<TD WIDTH=12%><FONT SIZE=4><B>Total Time</B></FONT></TD>" +
	    				"</TR>");
	    	out.println("<TR><TD></TD><TD COLSPAN=5><HR></TD></TR>");
	    	out.println("<TR ALIGN=RIGHT><TD></TD><FONT SIZE=3><TD>" + 
	    					"<FONT SIZE=4><B>" + TimeCardUtilities.RoundHalfUp(dGrandRegular, 2) + "</B></FONT></TD><TD>" + 
	    					"<FONT SIZE=4><B>" + TimeCardUtilities.RoundHalfUp(dGrandDouble, 2) + "</B></FONT></TD><TD>" + 
	    					"<FONT SIZE=4><B>" + TimeCardUtilities.RoundHalfUp(dGrandOver, 2) + "</B></FONT></TD><TD>" + 
	    					"<FONT SIZE=4><B>" + TimeCardUtilities.RoundHalfUp(dGrandLeave, 2) + "</B></FONT></TD><TD>" +
	    					"<FONT SIZE=4><B>" + TimeCardUtilities.RoundHalfUp(dGrandTotal, 2) + "</B></FONT></TD>" + 
	    				"</TR>");	
	    	out.println("</TABLE></TD></TR>");
	    
	    	out.println("</TABLE><BR>");
	    	out.println("&nbsp;--&nbsp;Starred time is not included in the total time.<BR>");
	    	out.println("&nbsp;--&nbsp;One half hour(0.5 hr) is deducted for lunch from any time entry (one IN/OUT pair) on which an employee worked more than 5 hours total.<BR> " +
	    			    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If either IN or OUT, or both are on sunday, don't consider lunch.<BR>");
	    	out.println("&nbsp;--&nbsp;Flag legend: <B>E</B> - Early Start, <B>F</B> - Finalized, <B>L</B> - Late, <B>M</B> - Missing IN or OUT, <B>P</B> - Posted.<BR>");
	    	
	    	//System.out.println("System.runFinalization()...");
	    	System.runFinalization();
	    	//System.out.println("System.gc()...");
	    	System.gc();
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	    out.println("</BODY></HTML>");
	}
}
