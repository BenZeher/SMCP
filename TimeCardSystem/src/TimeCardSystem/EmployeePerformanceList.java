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

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import TCSDataDefinition.*;

public class EmployeePerformanceList extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings({ "deprecation" })
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
	    	//SimpleDateFormat SQLDateformatter = new SimpleDateFormat("yyyy-MM-dd");
	    	SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy EEE");
	    	SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
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
	    			+ "-" + sDay);
	    	
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
				       "<HEAD><TITLE>Employee Performance Report</TITLE></HEAD>\n<BR>" + 
					   "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\"" +
				       " style = \" font-family:" + TimeCardUtilities.BASE_FONT_FAMILY + "; \"" +
					   " >" + 
					   "<TABLE BORDER=0 WIDTH=100%><TR><TD VALIGN=BOTTOM><FONT SIZE=4><B>Employee Performance Report</B></FONT></TD>" +
					   "<TD VALIGN=BOTTOM><B>&nbsp;&nbsp;&nbsp;&nbsp;" + 
					   "<FONT SIZE=2><B>Start Date:</B> " + USDateOnlyformatter.format(SelectedStartingDay) +
					   "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
					   "<B>End Date:</B> " + USDateOnlyformatter.format(SelectedEndingDay) + 
					   "</FONT></TD></TR>" +
					   "<TR><TD><Font SIZE=1><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A>&nbsp;&nbsp;<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeePerformanceListCriteriaSelection?SelectedEmployee=" + sCurrentEmployee + "\">Return to criteria selection</A></FONT></TD></TR></TABLE>");

	    	//get current URL
	    	String sCurrentURL;
	    	sCurrentURL = TimeCardUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
	    	//sCurrentURL = sCurrentURL.replaceAll("&", "*");
    		
	    	//create a list of what types are selected.
	    	Enumeration<String> paramNames = request.getParameterNames();
    		ArrayList<String> alLeaveTypes = new ArrayList<String>(0);
    		ArrayList<String> alWorkTypes = new ArrayList<String>(0);
    		TypeLoop:
    		while(paramNames.hasMoreElements()) {
    			String s = paramNames.nextElement().toString();
    			
    			try{
    				int iSwitch = Integer.parseInt(s.substring(1, 2));

        			switch (iSwitch) {
        				case 0: try{
        							alLeaveTypes.clear();
        							alLeaveTypes.add("ALLTYPES");
        							alWorkTypes.clear();
        							alWorkTypes.add("ALLTYPES");
        							break TypeLoop;
        						}catch(Exception ex){
                    				//System.out.println("Error in EmployeePerformanceList - case 0: " + ex.getMessage());
        						}
    	                case 1: try{
    	                			alLeaveTypes.add(s.substring(3));
                    				break;
                    			}catch(Exception ex){
                    				//System.out.println("Error in EmployeePerformanceList - case 1: " + ex.getMessage());
                    			}
    	                case 2: try{
		                			alWorkTypes.add(s.substring(3));
		            				break;
		            			}catch(Exception ex){
                    				//System.out.println("Error in EmployeePerformanceList - case 2: " + ex.getMessage());
		            			}
    	                case 3: try{
		                			alWorkTypes.add("Late");
		            				break;
		            			}catch(Exception ex){
		            				//System.out.println("Error in EmployeePerformanceList - case 2: " + ex.getMessage());
		            			}
        			}
    			}catch(Exception ex){
    				//System.out.println("Not a parameter we care about at this point.");
    			}
    		}
    		
	    	//get employee Info
	    	sSQL = TimeCardSQLs.Get_Employee_Info_SQL(sCurrentEmployee);
	    	ResultSet rsEmployee = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    	
	    	while(rsEmployee.next()){
	    		String sStartTime = rsEmployee.getString("tStartTime");
	    		
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
	    		out.println("<TR><TD><FONT SIZE=2><B>Start Time:</B> " + sStartTime + "</TD></TR>");
	    		out.println("</TABLE>");
    		
		    	
	    		if (alLeaveTypes.size() > 0){

		    		//get all the leave information
		    		out.println("<A NAME=LEAVESEC>");
		    		out.println("<H3>Leave Time Record(s):</H3>");
		    		out.println("<TABLE BORDER=0 WIDTH=100%>");
			    	out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
			    	out.println("<TR><TD ALIGN=CENTER WIDTH=10%><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Leave", LeaveAdjustments.id, "LEAVESEC", request) + "\"><B>ID</B></A></TD>" +
			    					"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Leave", LeaveAdjustments.iLeaveTypeID, "LEAVESEC", request) + "\"><B>Type</B></A></TD>" +
			    					"<TD ALIGN=CENTER WIDTH=20%><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Leave", LeaveAdjustments.dtInTime, "LEAVESEC", request) + "\"><B>Start Time</B></A></TD>" +
			    					"<TD ALIGN=CENTER WIDTH=20%><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Leave", LeaveAdjustments.dtOutTime, "LEAVESEC", request) + "\"><B>End Time</B></A></TD>" +
			    					"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Leave", LeaveAdjustments.dDuration, "LEAVESEC", request) + "\"><B>Duration</B></A></TD>" +
			    					"<TD ALIGN=LEFT WIDTH=30%><FONT SIZE=1><B>Note</B></TD></TR>");
			    	out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
			    	
		    		sSQL = TimeCardSQLs.Get_Leave_Adjustments_SQL(sCurrentEmployee, 
	    													      alLeaveTypes,
	    													      SelectedStartingDay.toString(),
	    													      SelectedEndingDay.toString(),
	    													      request.getParameter("LeaveSortBy"),
	    													      Integer.parseInt(request.getParameter("LeaveSortOrder")),
	    													      Boolean.parseBoolean(request.getParameter("IncludeSpecialAdjustment")));
		    		
		    		ResultSet rsLeaveEntries = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    		
		    		
			    	
			    	//int iLine = 0;
			    	boolean bRecordExist = false;
			    	while (rsLeaveEntries.next()){
			    		//iLine++;
			    		bRecordExist = true;
			    		out.println("<TR>");
			    		//line number and leave type
			    		out.println("<TD ALIGN=CENTER><FONT size=2><B><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeLeaveEdit?Employee=" + sCurrentEmployee + 
									    																		  "&id=" + rsLeaveEntries.getInt("LeaveAdjustments.id") +
									    																		  "&OriginalURL=" + sCurrentURL +
									    																		  "\">" + rsLeaveEntries.getInt("LeaveAdjustments.id") + "</A></B></FONT></TD>" +
			    					"<TD ALIGN=CENTER><FONT size=2>" + rsLeaveEntries.getString("LeaveAdjustmentTypes.sTypeTitle") + "</FONT></TD>");
			    		//start time
			    		if (rsLeaveEntries.getString("LeaveAdjustments.dtInTime").compareTo("0000-00-00") != 0){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + USDateOnlyformatter.format(clsDateAndTimeConversions.SQLDateToUtilDate(rsLeaveEntries.getDate("LeaveAdjustments.dtInTime"))) + "</FONT></TD>"); //catch NULLs
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2>&nbsp;</FONT></TD>"); 
			    		}
			    		//end time
			    		if (rsLeaveEntries.getString("LeaveAdjustments.dtOutTime").compareTo("0000-00-00") != 0){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + USDateOnlyformatter.format(clsDateAndTimeConversions.SQLDateToUtilDate(rsLeaveEntries.getDate("LeaveAdjustments.dtOutTime"))) + "</FONT></TD>"); //catch NULLs
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2>&nbsp;</FONT></TD>"); 
			    		}
			    		//duration
			    		out.print("<TD ALIGN=CENTER><FONT size=2><B>" + rsLeaveEntries.getDouble("LeaveAdjustments.dDuration") + "</B></FONT></TD>");
			    		//notes
			    		if (rsLeaveEntries.getString("LeaveAdjustments.mNote") != null){
			    			out.println("<TD ALIGN=CENTER><FONT size=2>" + rsLeaveEntries.getString("LeaveAdjustments.mNote") + "</FONT></TD></TR>");
			    		}else{
			    			out.println("<TD ALIGN=CENTER><FONT size=2>&nbsp;</FONT></TD>");
			    		}
			    		out.println("</TR>");
			    	}
			    	if (!bRecordExist){
			    		out.println("<TR><TD COLSPAN=6 ALIGN=CENTER><FONT SIZE=3><B>No Leave Record.</B></FONT></TD><TR>");
			    	}
			    	out.println("<TR><TD COLSPAN=6><HR></TD1></TR>");
			    	out.println("</TABLE><BR>");
			    	rsLeaveEntries.close();
	    		}
	    		
	    		if (alWorkTypes.size() > 0){
	    			
		    		//get all the Work information
			    	out.println("<A NAME=WORKSEC>");
		    		out.println("<H3>Special Work Time Record(s):</H3>");
		    		out.println("<TABLE BORDER=0 WIDTH=100%>");
		    		out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
		    		out.println("<TR><TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>Flag*</B></TD>" +
		    						//"<TD WIDTH=10% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Work", "id", request) + "\"><B>ID</B></A></FONT></TD>" + 
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Work", "iEntryTypeID", "WORKSEC", request) + "\"><B>Type</B></A></FONT></TD>" +
		    						"<TD WIDTH =26% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Work", "dtInTime", "WORKSEC", request) + "\"><B>IN</B></A></FONT></TD>" +
		    						"<TD WIDTH =18% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Work", "dtOutTime", "WORKSEC", request) + "\"><B>OUT</B></A></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><A HREF=\"" + request.getRequestURI().toString() + "?" + GetNewQueryString("Work", "iLateMinute", "WORKSEC", request) + "\"><B>Late</B></A></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>Regular</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>Double</B></FONT></TD>" +
		    						"<TD WIDTH =4% ALIGN=CENTER VALIGN=TOP><FONT SIZE=1><B>Total</B></FONT></TD>" +
		    						"<TD WIDTH =17% VALIGN=TOP><FONT SIZE=1><B>Notes</B></TD>" +
		    						"<TD WIDTH =15% VALIGN=TOP><B>Admin Notes</B></TD></TR>");
		    		out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
		    		
		    		sSQL = TimeCardSQLs.Get_Employee_TimeEntry_List_SQL(sCurrentEmployee,
		    															SelectedStartingDay.toString() + " 00:00:00",
			    													    SelectedEndingDay.toString() + " 23:59:59", 
		    															false, 
		    															true,
		    															alWorkTypes,
			    													    request.getParameter("WorkSortBy"),
			    													    Integer.parseInt(request.getParameter("WorkSortOrder"))); 
		    													    
		    		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    		//get list of special time entry type
			    	sSQL = TimeCardSQLs.Get_Time_Entry_Type_Info_SQL();
		    		ResultSet rsSTET = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	
		    		
		    		
		    		//get all punch in and out for current employee
			    	boolean bRecordExist = false;
		    		boolean TimeCalculatable = true;
					String sInTime;
					String sFormattedInTime;
					String sOutTime;  
					String sFormattedOutTime;
					String sINBGC;
					String sOUTBGC;
					boolean bInMissing;
					boolean bOutMissing;
	
					Timestamp inTime;
					Timestamp outTime;
			    	ResultSet rsNotes;
		    		
	        		while (rs.next()){
	        			bRecordExist = true;
	        			TimeCalculatable = true;
	
	        			//set null time to be "NA" so they will be default to current time and date in the next page
	        			if (rs.getString("dtInTime").compareTo("0000-00-00 00:00:00") == 0){
	    					sInTime = "NA";
	    					sFormattedInTime = "No punch in record";
	    					TimeCalculatable = false;
	    					bInMissing = true;
	    					sINBGC = "BGCOLOR=RED";
	    				}else{
	    					sInTime = rs.getString("dtInTime");
	    					sFormattedInTime = USDateformatter.format(Timestamp.valueOf(rs.getString("dtInTime")));
	    					bInMissing = false;
	    					sINBGC = "";
	    				}
	        			
	        			if (rs.getString("dtInTimeOri").compareTo("0000-00-00 00:00:00") != 0){
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
	    					out.print("<B>L</B>");
	    				}
	    				if (!TimeCalculatable){
	    					out.print("M");
	    				}
	    				out.println("</FONT></TD>");
	    				
	    				//check for labels for Special Entry Type
	    				out.println("<TD ALIGN=CENTER VALIGN=TOP><Font SIZE=1 COLOR=#FF0000>");
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
	    				out.print("<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?Type=0&EarlyStart=" + rs.getInt("iEarlyStart") + "&LateFlag=" + rs.getInt("iLate") + "&SpecialEntryType=" + rs.getInt("iEntryTypeID") + "&id=" + rs.getInt("id") + "&Date=" + sInTime + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">");
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
	    				out.print("<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryEdit?Type=1&id=" + rs.getInt("id") + "&Date=" +  sOutTime + "&SpecialEntryType=" + rs.getInt("iEntryTypeID") + "&EmployeeID=" + sCurrentEmployee + "&OriginalURL=" + sCurrentURL + "\">");
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
	    					
	    					double dDailySum = outTime.getTime() - inTime.getTime();
	    					
	    					//One half hour is deducted for lunch from anyday on which an employee worked more than 5 hours total.
	    					//If either in or out, or both are on sunday, don't consider lunch
	    					if (dDailySum > 18000000 && inTime.getDay() != 0 && outTime.getDay() != 0){
	    						//out.println(dDailySum);
	    						dDailySum = dDailySum - 1800000;
	    					}
	    					//calculate double time
	    					double dDailyDouble = TimeCardUtilities.CalculateDoubleTime(rs.getString("dtInTime"), rs.getString("dtOutTime"), sStartTime) /1.0;
	    					//calculate regular time
	    					double dDailyRegular = dDailySum - dDailyDouble * 60 * 1000;
	    					
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
							out.println(rsNotes.getString("mNote"));
	    					while (rsNotes.next()){
	    						out.print(" || " + rsNotes.getString("mNote"));
	    					}
	    				}else{
	    					out.println("&nbsp;");
	    				}
	    				out.println("</FONT></TD>");
	    				
	    				//Attach admin change log
	    				if (rs.getString("mChangeLog").trim().compareTo("") !=0){
	    					out.println ("<TD VALIGN=TOP><Font SIZE=1>" + rs.getString("mChangeLog").trim() + "</FONT></TD>");
	    				}else{
	    					out.println ("<TD>&nbsp;</TD>");
	    				}
	    				
	    				rsNotes.close();
	    				out.println ("</TR>");
	            	}
    	    		if (!bRecordExist){
    	    			out.println("<TR><TD COLSPAN=10 ALIGN=CENTER><FONT SIZE=3><B>No Time Entry Record.</B></FONT></TD><TR>");
    	    		}
            		out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
            		out.println("</TABLE>");
            		
            		rs.close();
            		rsSTET.close();
            		
	    		}
		    	out.println("</TABLE><BR>");
	    	}
	    	rsEmployee.close();
	    	
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("Exception: " + ex.toString() + "<BR>");
	    }
	    out.println("</BODY></HTML>");
	}

	private String GetNewQueryString(String sTypePrefix, 
									 String sCriteria,
									 String sLocationSuffix,
									 HttpServletRequest request){
		
		Enumeration<String> paramNames = request.getParameterNames();
		ArrayList<String> alParameters = new ArrayList<String>(0);
		
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			if (s.compareTo(sTypePrefix + "SortBy") != 0 || s.compareTo(sTypePrefix + "LeaveSortOrder") != 0){
				alParameters.add(s + "=" + request.getParameter(s));
			}
		}
		
		//now we have all the parameters except sort, we can 
		//reconstruct the URL with a new sort option.
		String sURL = sTypePrefix + "SortBy=" + sCriteria;
		if (Integer.parseInt(request.getParameter(sTypePrefix + "SortOrder")) == 0 && 
			sCriteria.compareTo(request.getParameter(sTypePrefix + "SortBy")) == 0){
			sURL = sURL + "&" + sTypePrefix + "SortOrder=1";
		}else{
			sURL = sURL + "&" + sTypePrefix + "SortOrder=0";
		}
		
		for (int i=0;i<alParameters.size();i++){
			sURL = sURL + "&" + alParameters.get(i).toString();
		}
		
		sURL = sURL + "#" + sLocationSuffix;
		
		return sURL;
	}
}
