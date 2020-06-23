
package smcontrolpanel;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.servlet.ServletContext;

import SMClasses.SMAppointment;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableappointments;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;


public class SMViewAppointmentCalendarReport extends java.lang.Object{

	private static String CELL_SHADED = "#EBEBEB";
	private static String ENTRY_BACKGROUND = "#e6ffe6";
	private static String ENTRY_BACKGROUND_HOVER = "#b3ffb3";
	private static String TODAYS_CELL_SHADED = "#DAE6EE";
	private static String TODAYS_CELL_UNSHADED = "#E9F7FF";
	private static String APPOINTMENT_COMMENT_COLOR = "RED";
	private static String SHIP_TO_CITY_COLOR = "GREEN";
	
	public static String APPOINTMENTQUERYSTRING = "AppointmentQueryString";
	public static String MAPDATEPARAMETER = "MAPDATE";
//	private static String MOVE_JOB_LEFT_SYMBOL = "<";
//	private static String MOVE_JOB_RIGHT_SYMBOL = ">";
//	private static String COPY_JOB_LEFT_SYMBOL = "<<";
//	private static String COPY_JOB_RIGHT_SYMBOL = ">>";
//	private static String MOVE_JOB_UP_SYMBOL = "^";
//	private static String MOVE_JOB_DOWN_SYMBOL = "v";
//	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
//	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
//	public static final String COMMAND_FLAG = "COMMANDFLAG";
//	public static final String COMMAND_VALUE_ADD_ENTRY = "ADDENTRY";
	
	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	//private long lTimer;
	private String sLinkToPreviousWeek = "";
	private String sLinkToNextWeek = "";
	private String sInterval = "";
	public SMViewAppointmentCalendarReport(
	){
		m_sErrorMessage = "";
	}
	public void initializeReport(
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> aUserIDs,
			boolean bDisplayOneDay,
			boolean bDateRangeChosen,
			boolean bDateRangeToday,
			boolean bDateRangeThisWeek,
			boolean bDateRangeNextWeek,
			boolean bAllowAppointmentCalendarEditing,
			String sUserID,
			String sDBID,
			String sInstanceMarker,
			PrintWriter out,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel
	) throws Exception{
		//Get a connection, and try to print:
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".doPost - userID: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1494603482] getting connection - " + e.getMessage());
		}
		if (conn == null){
			throw new Exception("WARNING: Could not get data connection.");
		}
		try {
			processReport(
				conn,
				sStartingDate,
				sEndingDate,
				aUserIDs,
				bDisplayOneDay,
				bDateRangeChosen,
				bDateRangeToday,
				bDateRangeThisWeek,
				bDateRangeNextWeek,
				bAllowAppointmentCalendarEditing,
				sUserID,
				sInstanceMarker,
				sDBID,
				out,
				bOutputToCSV,
				context,
				sLicenseModuleLevel);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080678]");
			throw new Exception("Error [1499358161] - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080679]");
		return;
	}
	private void processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> aUserIDs,
			boolean bDisplaySingleDay,
			boolean bDateRangeChosen,
			boolean bDateRangeToday,
			boolean bDateRangeThisWeek,
			boolean bDateRangeNextWeek,
			boolean bAllowAppointmentCalendarEditing,
			String sUserID,
			String sInstanceMarker,
			String sDBID,
			PrintWriter out,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel) throws Exception{
		
		out.println(SMUtilities.getSMCPJSIncludeString(context));
		out.println(clsServletUtilities.getJQueryIncludeString());

		printTableLayout(out);
		
		String SQL = "SELECT"
			+ " *"
			+ " FROM " + SMTableappointments.TableName
			+ " WHERE (";

			SQL += "((" + SMTableappointments.datentrydate + " <= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate)+ "')" 
			+ " AND (" + SMTableappointments.datentrydate + " >= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate) + "')";
			
			SQL += ") AND (" 
			;
		
		for(int i = 0; aUserIDs.size() > i; i++){
			SQL += "(" + SMTableappointments.luserid + "=" + aUserIDs.get(i) + ") ";
			if(i != aUserIDs.size() - 1){
				SQL += " OR ";
			}
		}
			
		SQL += ")) ORDER BY" 
			+ " " + SMTableappointments.lid 
			+ ", " + SMTableappointments.datentrydate 
			+ ", " + SMTableappointments.iminuteofday 
			;
		//System.out.println("[1427124486] " + SQL);

		//Check permissions for editing appointments:
/*		boolean bEditAppointments = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditAppointmentCalendar,
				sUserName,
				conn,
				sLicenseModuleLevel);

	*/	

		//Put it all inside a form, so we can use buttons and things:
		out.println("<FORM ID='" + "MAINFORM" + "' NAME='" + "MAINFORM" + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMAppointmentCalendarHandler"
			+ " METHOD = 'POST'"
			+ ">"
		);
		
		String sNavigationString = SMUtilities.getURLLinkBase(context) 
		+ "smcontrolpanel.SMViewAppointmentCalendarGenerate"
		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
		String sNavigationStringParams = "";
		//For the navigation links, we want the date range parameter to indicate that the user has chosen dates:
		sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM 
		+ "=" + SMViewAppointmentCalendarSelection.DATE_RANGE_CHOOSE; 

		//If the user chose to edit the appointment, we need to pass in that parameter:
		if (bAllowAppointmentCalendarEditing){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER + "=Y"; 
		}
		
		for(int i = 0; aUserIDs.size() > i; i++){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.USER_PREFIX + aUserIDs.get(i);
		}

		
		sNavigationString += sNavigationStringParams;
		printDateHeadings(sStartingDate, sEndingDate, sNavigationString, context, sDBID, out);
	
		sNavigationStringParams +=  "&" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + sStartingDate;
		sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + sEndingDate;

		long lTimer = System.currentTimeMillis();
		try{

			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (bDebugMode){
				System.out.println("[1579275215] In " + this.toString() + " Main SELECT SQL took " 
						+ (System.currentTimeMillis() - lTimer) + " milliseconds");
				System.out.println("SQL staement - " + SQL);
			}
			
			//get the difference between the starting and ending dates
			int iIntervalOfDays = 7;
			iIntervalOfDays = (int) clsDateAndTimeConversions.getFirstDateMinusSecondDateInDays(sStartingDate, sEndingDate) * -1 + 1;
			int iNumberOfDaysToDisplay = (int) iIntervalOfDays * aUserIDs.size();
			
			//Set variables to load calendar day cell objects
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			Calendar calCurrentDate = Calendar.getInstance();
			ArrayList<CalendarDayCell> arrCalendarDayCells = new ArrayList<CalendarDayCell>();
			//Manually fill the array list of objects
			for(int i = 0; iNumberOfDaysToDisplay > i; i++){
				arrCalendarDayCells.add(new CalendarDayCell());
			}		
			//Load the user and date for each calendar day cell
			for(int i = 0; aUserIDs.size() > i; i++){
				//Set the current date to the starting date for each new user
				try {
					calCurrentDate.setTime(dateFormat.parse(sStartingDate));
				} catch (ParseException e1) {
					throw new Exception("Error parsing starting date for previous link - " + e1.getMessage());
					
				}
				//set the user and date for each calendar day cell
				calCurrentDate.add(Calendar.DATE, -1);
				for(int j = 0; iIntervalOfDays > j; j++){
					arrCalendarDayCells.get((i * iIntervalOfDays) + j).sUser = aUserIDs.get(i);
					//add a day to current date
					calCurrentDate.add(Calendar.DATE, 1);		
					arrCalendarDayCells.get((i * iIntervalOfDays) + j).sDate = clsDateAndTimeConversions.CalendarToString(calCurrentDate, "yyyy-MM-dd");
				}	
			}
			
			//Load the entries from the SQL results into the calendar day cells
			while(rs.next()){
				String sCurrentUser = "";
				String sCurrentDate = "";
				sCurrentUser = rs.getString(SMTableappointments.luserid);
				sCurrentDate = rs.getString(SMTableappointments.datentrydate);
				//loop through all calendar day cells
				for(int i = 0; arrCalendarDayCells.size() > i; i++){
					//if the user AND date of the SQL result matches the calendar day cell,
					//load entry for that cell
					if(arrCalendarDayCells.get(i).sUser.compareToIgnoreCase(sCurrentUser) == 0
					&& arrCalendarDayCells.get(i).sDate.compareToIgnoreCase(sCurrentDate) == 0){
						//To load page faster, load all the calendar info from the original SQL results.
						SMAppointment CurrentCalendarAppointmentInfo = new SMAppointment();
						CurrentCalendarAppointmentInfo.setslid(rs.getString(SMTableappointments.lid));
						CurrentCalendarAppointmentInfo.setluserid(rs.getString(SMTableappointments.luserid));
						CurrentCalendarAppointmentInfo.setdatentrydate(rs.getString(SMTableappointments.datentrydate));
						CurrentCalendarAppointmentInfo.setiminuteofday(SMAppointment.timeIntegerToString(rs.getInt(SMTableappointments.iminuteofday)));
						CurrentCalendarAppointmentInfo.setmcomment(rs.getString(SMTableappointments.mcomment));
						CurrentCalendarAppointmentInfo.setsordernumber(rs.getString(SMTableappointments.sordernumber));
						CurrentCalendarAppointmentInfo.setisalescontactid(Integer.toString(rs.getInt(SMTableappointments.isalescontactid)));
						CurrentCalendarAppointmentInfo.setibidid(Integer.toString(rs.getInt(SMTableappointments.ibidid)));
						CurrentCalendarAppointmentInfo.setsaddress1(rs.getString(SMTableappointments.saddress1));
						CurrentCalendarAppointmentInfo.setsaddress2(rs.getString(SMTableappointments.saddress2));
						CurrentCalendarAppointmentInfo.setsaddress3(rs.getString(SMTableappointments.saddress3));
						CurrentCalendarAppointmentInfo.setsaddress4(rs.getString(SMTableappointments.saddress4));
						CurrentCalendarAppointmentInfo.setscity(rs.getString(SMTableappointments.scity));
						CurrentCalendarAppointmentInfo.setsstate(rs.getString(SMTableappointments.sstate));
						CurrentCalendarAppointmentInfo.setszip(rs.getString(SMTableappointments.szip));
						CurrentCalendarAppointmentInfo.setsgeocode(rs.getString(SMTableappointments.sgeocode));
						CurrentCalendarAppointmentInfo.setscontactname(rs.getString(SMTableappointments.scontactname));
						CurrentCalendarAppointmentInfo.setsbilltoname(rs.getString(SMTableappointments.sbilltoname));
						CurrentCalendarAppointmentInfo.setsshiptoname(rs.getString(SMTableappointments.sshiptoname));
						CurrentCalendarAppointmentInfo.setsphone(rs.getString(SMTableappointments.sphone));
						CurrentCalendarAppointmentInfo.setsemail(rs.getString(SMTableappointments.semail));
						
						arrCalendarDayCells.get(i).entryInfo.add(CurrentCalendarAppointmentInfo);
						
					}
				}
			}
			rs.close();
			
			//Loop through all the calendar day cell object to fill the calendar
			int iRowColor = 0;
			int iRowEntryCounter = 0;
			String sColumnClass = "";
			//Loop through the users for every row of the calendar
			for(int iUserIndex = 0; aUserIDs.size() > iUserIndex; iUserIndex++){
				iRowColor++;
				sColumnClass="CLASS=\"userunshaded\"";
				if(iRowColor%2 == 0){
					sColumnClass="CLASS=\"usershaded\"";
				}
				 out.println("<TR><TD " + sColumnClass + ">" + SMUtilities.getFullNamebyUserID(aUserIDs.get(iUserIndex), conn) + "</TD>");
				 //Loop through every day of the calendar for the columns
				 for(int iDayOfWeekIndex = 0; iIntervalOfDays > iDayOfWeekIndex; iDayOfWeekIndex++){
					
					 
					 sColumnClass = "CLASS=\"entryshaded\"";
						if(iRowEntryCounter%2 == 0){
							sColumnClass="CLASS=entryunshaded";
						}
					 String sCurrentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					 if(arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).sDate.compareToIgnoreCase(sCurrentDate) == 0){
						 
						 if(iRowColor%2 == 0){
							 sColumnClass = "CLASS=\"currentdayentryunshaded\"";
						 }
						 sColumnClass = "CLASS=\"currentdayentryshaded\"";
					 }
			
					 out.println("<TD " + sColumnClass + ">"); 
					 if(bAllowAppointmentCalendarEditing){
						 out.println(" <a "
							+ "href=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMEditAppointmentEdit"
							+ "?" + SMAppointment.Paramlid + "=-1"
							+ "&" + SMAppointment.Paramluserid + "=" + aUserIDs.get(iUserIndex)
							+ "&" + SMAppointment.Paramdatentrydate + "=" + clsDateAndTimeConversions.resultsetDateStringToString(arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).sDate)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ sNavigationStringParams 
							+ "\""
							+ "> "
							+ " Add entry"
							+ "</a>");
					 }
					 out.println("<br>");
					 //Sort the the entry of this day by time before displaying by overriding the comparator for the appointment entry object
					 if(arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).entryInfo.size() > 1){
						 Collections.sort(arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).entryInfo, new Comparator<SMAppointment>() {
							 @Override
							 public int compare(SMAppointment o1, SMAppointment o2) {
								 int i = 0;
								 try {
									 i = Integer.compare(SMAppointment.timeStringToInteger(o1.getiminuteofday()), (SMAppointment.timeStringToInteger(o2.getiminuteofday())));
								} catch (Exception e) {
									System.out.println("[1579275220] ERROR in COMPARE: " + e.getMessage());
								}
								return i;
							 }
						 });
					 }
					 
					 //Print the appointment info in a day cell
					 for(int iEntryIndex = 0; arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).entryInfo.size() > iEntryIndex; iEntryIndex++){
						 SMAppointment appointmentEntry = arrCalendarDayCells.get((iUserIndex * iIntervalOfDays) + iDayOfWeekIndex).entryInfo.get(iEntryIndex);
						 
						 out.println(printAppointment(appointmentEntry, context, bAllowAppointmentCalendarEditing, bDisplaySingleDay, sDBID));
	
						 
					 }
					
					 out.println("</TD>\n");
				 
				 }
				 iRowEntryCounter++;
				 out.println("</TR>\n");
			}		
		}catch (Exception e){
			if(aUserIDs.size() == 0){
				throw new Exception("Atleast one user must be selected. ");
			}
			throw new Exception("Error [1495054326] - " + e.getMessage());
		}
		//Put the last cell ending in:
		out.println("</td>");
		//Put the last line ending in:
		out.println("</tr>");
		
		out.println("</table>");

		//Add links to the previous and next week:
		out.println("<FONT SIZE=2>" 
				+ sLinkToPreviousWeek.replace("<BR>", "&nbsp;")
				+ "&nbsp;&nbsp;&nbsp;"
				+ sLinkToNextWeek.replace("<BR>", "&nbsp;") + "</FONT>"
		);
		
		//Close the form:
		out.println("</FORM>");

		out.println(sCommandScript(context, sNavigationStringParams, sDBID));

	}
	
	private String printAppointment(SMAppointment se, ServletContext context, boolean bAllowAppointmentCalendarEditing, boolean bDisplaySingleDay, String sDBID) {
		String s = "";
		 String sEntryID = se.getslid(); 
		 String sTime = se.getiminuteofday() ;
		 String sName = se.getscontactname();
		 String sBillToName = se.getsbilltoname();
		 String sShipToName = se.getsshiptoname();
		 String sAddress1 = se.getsaddress1();
		 String sAddress2 = se.getsaddress2();
		 String sAddress3 = se.getsaddress3();
		 String sAddress4 = se.getsaddress4();
		 String sCity = se.getscity();
		 String sState = se.getsstate();
		 String sZip = se.getszip();
		 String sPhone = se.getsphone();
		 String sEmail = se.getsemail();
		 String sComment = se.getmcomment();
		 String sOrderNum = se.getsordernumber();
		 String sBidID = se.getibidid();
		 String sSalesID = se.getisalescontactid();

		 String sCompleteAddress = "";
	if(bDisplaySingleDay){
		 s += "<BR>"
			+ "<span title=\"";
		 if(sName.compareToIgnoreCase("") != 0){
			 s+=  sName;
		 }
		 if((sShipToName.compareToIgnoreCase("") != 0) && (sShipToName.compareToIgnoreCase(sName) != 0)){
			 s +=  "\n" + sShipToName;
		 }
		 if(sAddress1.compareToIgnoreCase("") != 0){
			 sCompleteAddress += "\n" + sAddress1 ; 
		 }
		 if(sAddress2.compareToIgnoreCase("") != 0){
			 sCompleteAddress += "\n" + sAddress2; 
		 }
		 if(sAddress3.compareToIgnoreCase("") != 0){
			 sCompleteAddress += "\n" + sAddress3; 
		 }
		 if(sAddress4.compareToIgnoreCase("") != 0){
			 sCompleteAddress += "\n" + sAddress4 ; 
		 }
		 
		 sCompleteAddress += "\n" + sCity + " " + sState + " " + sZip;
		 sCompleteAddress = sCompleteAddress.replace("\n", " ");

		 if(sEmail.compareToIgnoreCase("") != 0){
			 s += "\n" + sEmail;		
		 }
	s += "\">";
	 
	 
	 String sEntryClass = "entryrecordeditsingleday"; 
	 if(!bAllowAppointmentCalendarEditing){
		 sEntryClass = "entryrecordsingleday";
	 }
	 
	s += "<div class=\"" + sEntryClass + "\" id=\"" + sEntryID + "\">";
	s += "<div align=left>&nbsp;<B>" + sTime + "</B></div>";	 
	 //Order, bid, or sales contact id
	 if(sOrderNum.compareToIgnoreCase("") != 0){
		s +=
				 "<div align=right><I>Order</I>: " 
				+" <a "
					+ "href=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNum
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\""
						+ "> "
					+ sOrderNum
					+ "</a> &nbsp;"
		 + "</div>"
				 ;
	 } 
	 if(sBidID.compareToIgnoreCase("0") != 0){
		 s +=
				 "<div align=right><I>Sales Lead</I>: " 
					+" <a "
					+ "href=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMEditBidEntry?lid=" + sBidID
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\""
						+ "> "
					+ sBidID
					+ "</a>"									 
				 + "&nbsp;</div>"
				 ;	 
	 } 
	 if(sSalesID.compareToIgnoreCase("0") != 0){
		 s +=
				 "<div align=right><I>Sales Contact ID</I>: "
					+" <a "
					+ "href=\"" 
					+ SMUtilities.getURLLinkBase(context) 
					+ "smcontrolpanel.SMSalesContactEdit?id=" + sSalesID
					+ "\""
					+ "> "
					+ sSalesID
					+ "</a>"
					+ "&nbsp;</div>"
				 ;
	 }

	 
	 if(sBillToName.compareToIgnoreCase("") != 0){
		s += "&nbsp;" + sBillToName + ",";
	 }
	 if(sBillToName.compareToIgnoreCase("") != 0){
		 s+= "<br>";
	 }

	s += "&nbsp;" + sName + "&nbsp;<a href=\"http://maps.google.com/?q=" + sCompleteAddress + "\">" + sCompleteAddress + "</a><br>";


	 s += "<br>";
		if(sPhone.compareToIgnoreCase("") != 0){
			 s+="&nbsp;Phone: <a href=\"tel:" + sPhone + "\">" + sPhone + " </a><br><br>";
		}

	 if(sComment.compareToIgnoreCase("") != 0 ){
		 s += "&nbsp;<i><font color=\"" + APPOINTMENT_COMMENT_COLOR + "\">" + sComment + "</font></i>"; 
	 }
	 	s	+= "</div></span></font>";

	
	}else{
			 s += "<BR>"
				+ "<span title=\"";
			 if(sName.compareToIgnoreCase("") != 0){
				 s+=  sName;
			 }
			 if((sShipToName.compareToIgnoreCase("") != 0) && (sShipToName.compareToIgnoreCase(sName) != 0)){
				 s +=  "\n" + sShipToName;
			 }
			 if(sAddress1.compareToIgnoreCase("") != 0){
				 sCompleteAddress += "\n" + sAddress1 ; 
			 }
			 if(sAddress2.compareToIgnoreCase("") != 0){
				 sCompleteAddress += "\n" + sAddress2; 
			 }
			 if(sAddress3.compareToIgnoreCase("") != 0){
				 sCompleteAddress += "\n" + sAddress3; 
			 }
			 if(sAddress4.compareToIgnoreCase("") != 0){
				 sCompleteAddress += "\n" + sAddress4 ; 
			 }

			 sCompleteAddress += "\n" + sCity + " " + sState + " " + sZip;
			 s += sCompleteAddress; 

			 
			 
			 if(sPhone.compareToIgnoreCase("") != 0){
					 s += "\n" + sPhone;
			 }
			 if(sEmail.compareToIgnoreCase("") != 0){
				 s += "\n" + sEmail;		
			 }
		s += "\">";
		 
		 
		 String sEntryClass = "entryrecordedit"; 
		 if(!bAllowAppointmentCalendarEditing){
			 sEntryClass = "entryrecord";
		 }
		 
		s += "<div class=\"" + sEntryClass + "\" id=\"" + sEntryID + "\">";
				 
		s += "<div align=left>&nbsp;<B>" + sTime + "</B></div>"
				
				 ;
		s += "<div id = \"AppointmentInfo\">\n ";
		 //Order, bid, or sales contact id
		 if(sOrderNum.compareToIgnoreCase("") != 0){
			s +=
					 "<div align=right><I>Order</I>: " 
					+" <a "
						+ "href=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNum
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\""
							+ "> "
						+ sOrderNum
						+ "</a> &nbsp;"
			 + "</div>"
					 ;
		 } 
		 if(sBidID.compareToIgnoreCase("0") != 0){
			 s +=
					 "<div align=right><I>Sales Lead</I>: " 
						+" <a "
						+ "href=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMEditBidEntry?lid=" + sBidID
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\""
							+ "> "
						+ sBidID
						+ "</a>"									 
					 + "&nbsp;</div>"
					 ;	 
		 } 
		 if(sSalesID.compareToIgnoreCase("0") != 0){
			 s +=
					 "<div align=right><I>Sales Contact ID</I>: "
						+" <a "
						+ "href=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMSalesContactEdit?id=" + sSalesID
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\""
						+ "> "
						+ sSalesID
						+ "</a>"
						+ "&nbsp;</div>"
					 ;
		 }

		 
		 if(sBillToName.compareToIgnoreCase("") != 0){
			s += "&nbsp;" + sBillToName + ",";
		 }
			 if(sCity.compareToIgnoreCase("") != 0){
				 s += "<font color=\"" + SHIP_TO_CITY_COLOR + "\"> " + sCity.replace(" ", "&nbsp;") + "</font><br>";
				 		 
			} 
		 if(sComment.compareToIgnoreCase("") != 0 ){
			 s += "&nbsp;<i><font color=\"" + APPOINTMENT_COMMENT_COLOR + "\">" + sComment + "</font></i>"; 
		 }
		 	
		 	s += "</div>\n";
		 	s	+= "</div></span>";
	}
		
		return s;
	}
	private String sCommandScript(ServletContext context, String sNavigationStringParams, String sDBID){
		String sEditEntryLinkBase = SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMEditAppointmentEdit";
		
		String s = "";
		s += "<NOSCRIPT>\n"
			+ "		<font color=red>\n"
			+ "		<H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "		</font>\n"
			+ "</NOSCRIPT>\n"
			;
		
		s += "<script type='text/javascript'>\n";
		
		//s += "window.onbeforeunload = setTime;\n";
		s +=  "function TextHighlighted(text){\n"
				+ "  var highlightedtext = '';\n"
				+ "  if(window.getSelection){\n"
				+ "     highlightedtext = window.getSelection();\n"
				+"   }else if (document.getSelection){\n"
				+"      highlightedtext = document.getSelection();\n"
				+"   }else if (document.selection) {\n"
				+"      highlightedtext = document.selection.createRange().text;\n"
				+"    }\n"
				+"    if(highlightedtext == ''){\n"
				+"          return false;\n"
				+"     }\n"
				+"     return true;\n"
				+" }\n";
		s +=  "$('div.entryrecordedit').on('click', function(e) {\n"
				+"       if(!TextHighlighted(document.getElementById('AppointmentInfo'))){\n"
				+	"  if ( e.ctrlKey ) {\n"
				+ "    window.open(\"" + sEditEntryLinkBase + "?lid=\" + $(this).attr('id') + \"" 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sNavigationStringParams + "\");\n"
				+ "  }else{\n"
				+ "      window.open(\"" + sEditEntryLinkBase + "?lid=\" + $(this).attr('id') + \"" 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sNavigationStringParams + "\");\n"
				+ "   }\n"
				+ " }\n"
				+"});\n\n"
			;	
		
		s +=  "$('div.entryrecordeditsingleday').on('click', function(e) {\n"
				+	"if ( e.ctrlKey ) {\n"
				+ "window.open(\"" + sEditEntryLinkBase + "?lid=\" + $(this).attr('id') + \"" 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sNavigationStringParams + "\");\n"
				+ "   } else {\n"
				+ "window.location.href=\"" + sEditEntryLinkBase + "?lid=\" + $(this).attr('id') + \"" 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sNavigationStringParams + "\"\n"
				+"    }\n"
				+"});\n\n"
				;
		
		s += "$('div.entryrecordedit').find('a').click(function(e) {\n"
				+	" e.stopPropagation();\n"
				+"});\n\n"
				;
		
		s += "$('div.entryrecordeditsingleday').find('a').click(function(e) {\n"
				+	" e.stopPropagation();\n"
				+"});\n\n"
				;	
		
		s += " </script>\n";
		
		return s;
	}
	private void printTableLayout(PrintWriter pwOut){
		pwOut.println("<style type=\"text/css\">");
		pwOut.println(
				"table.trucksched {"
				+ " border-style: solid;"
				+ " table-layout:fixed;"
				//+ " border-collapse:separate;"
				+ " border-collapse:collapse;"
				+ " width:100%;"
				+ " font-size:small;"
				+ " font-family:Arial;"
				+ "}"		
		);
		pwOut.println(
				"table.trucksched th {"
				+ " vertical-align:top;"
				+ " font-weight:bold;"
				+ " font-size:normal;"
				+ " text-decoration:underline;"
				+ " text-decoration:none;"
				+ " border-style: none;"
				//+ " text-align:left;"
				+ "}"	
		);
		pwOut.println(
				"td.currentdayentryunshaded {"
				+ " width: auto;"
				+ " font-weight:normal;"
				+ " font-size:small;"
				//+ " border-style: none;"
				+ " border-style: solid;"
				+ " border-width:1px;"
				+ " margin:0px;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " background-color: " + TODAYS_CELL_UNSHADED + ";"
				+ "}"	
		);
		pwOut.println(
				"td.currentdayentryshaded {"
				+ " width: auto;"
				+ " font-weight:normal;"
				+ " font-size:small;"
				//+ " border-style: none;"
				+ " border-style: solid;"
				+ " border-width:1px;"
				+ " margin:0px;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " background-color: " + TODAYS_CELL_SHADED + ";"
				+ "}"	
		);
		
		pwOut.println(
				"td.entryunshaded {"
				+ " width: auto;"
				+ " font-weight:normal;"
				+ " font-size:small;"
				//+ " border-style: none;"
				+ " border-style: solid;"
				+ " border-width:1px;"
				+ " margin:0px;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + ";"
				+ "}"	
		);
		pwOut.println(
				"td.entryshaded {"
				+ " font-weight:normal;"
				+ " font-size:small;"
				+ " width: auto;"
				//+ " border-style: none;"
				+ " border-style: solid;"
				+ " border-width:1px;"
				+ " margin:0px;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " background-color: " + CELL_SHADED + ";"
				+ "}"	
		);
		pwOut.println(
				"td.userunshaded {"
				+ " width: 100px;"
				+ " vertical-align:top;"
				+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + ";"
				+ "}"	
		);
		pwOut.println(
				"td.usershaded {"
				+ " width: 100px;"
				+ " vertical-align:top;"
				+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + CELL_SHADED + ";"
				+ "}"	
		);
		
		pwOut.println(
				"div.entryrecord {"
				+ " vertical-align:top;"
				//+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + ENTRY_BACKGROUND + ";"
				+ "}"	
		);
		
		pwOut.println(
				"div.entryrecordedit {"
				+ " vertical-align:top;"
				//+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + ENTRY_BACKGROUND + ";"
				+ "}"	
		);
		
		pwOut.println(
				"div.entryrecordsingleday {"
				+ " vertical-align:top;"
				//+ " font-weight:bold;"
				+ " font-size:large;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + ENTRY_BACKGROUND + ";"
				+ "}"	
		);
		
		pwOut.println(
				"div.entryrecordeditsingleday {"
				+ " vertical-align:top;"
				//+ " font-weight:bold;"
				+ " font-size:large;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + ENTRY_BACKGROUND + ";"
				+ "}"	
		);
		
		
		
		pwOut.println(
				"div.entryrecordedit:hover {"
				+ " background-color: " + ENTRY_BACKGROUND_HOVER + ";"
				+ "}"	
		);
		
		pwOut.println(
				"div.entryrecordeditsingleday:hover {"
				+ " background-color: " + ENTRY_BACKGROUND_HOVER + ";"
				+ "}"	
		);

		pwOut.println(
				"div.additionalentryinfo {"
				+ " background-color: " + "#ff9999" + ";"
				+ "}"	
		);

		pwOut.println("</style>");

		pwOut.println("<TABLE class=\"trucksched\">");
	}
	private boolean printDateHeadings(
			String sStartingDate, 
			String sEndingDate,
			String sRedirectString,
			ServletContext context,
			String sDBID,
			PrintWriter out){

		sInterval = "Week";
		int iInterval = 7;
		if (sStartingDate.compareToIgnoreCase(sEndingDate) == 0){
			sInterval = "Day";
			iInterval = 1;
		}

		Calendar calPreviousStartDate = Calendar.getInstance();
		Calendar calPreviousEndDate = Calendar.getInstance();
		Calendar calNextStartDate = Calendar.getInstance();
		Calendar calNextEndDate = Calendar.getInstance();

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		try {
			calPreviousStartDate.setTime(dateFormat.parse(sStartingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing starting date for previous link - " + e1.getMessage();
			return false;
		}
		try {
			calPreviousEndDate.setTime(dateFormat.parse(sEndingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing ending date for previous link - " + e1.getMessage();
			return false;
		}
		try {
			calNextStartDate.setTime(dateFormat.parse(sStartingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing starting date for next link - " + e1.getMessage();
			return false;
		}
		try {
			calNextEndDate.setTime(dateFormat.parse(sEndingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing ending date for next link - " + e1.getMessage();
			return false;
		}

		calPreviousStartDate.add(Calendar.DATE, -1 * iInterval);
		calPreviousEndDate.add(Calendar.DATE, -1 * iInterval);
		calNextStartDate.add(Calendar.DATE, iInterval);
		calNextEndDate.add(Calendar.DATE, iInterval);

		String sPreviousStartingDate = clsDateAndTimeConversions.CalendarToString(calPreviousStartDate, "MM/dd/yyyy");
		String sPreviousEndingDate = clsDateAndTimeConversions.CalendarToString(calPreviousEndDate, "MM/dd/yyyy");
		String sNextStartingDate = clsDateAndTimeConversions.CalendarToString(calNextStartDate, "MM/dd/yyyy");
		String sNextEndingDate = clsDateAndTimeConversions.CalendarToString(calNextEndDate, "MM/dd/yyyy");
		out.println("<tr>");
		//Print the first cell with navigation links in it:
		sLinkToPreviousWeek = "<A HREF=\"" 
			+ sRedirectString
			+ "&" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + sPreviousStartingDate
			+ "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + sPreviousEndingDate
			+ "\">" + "Previous<BR>" + sInterval + "</A>";
		sLinkToNextWeek =  "<A HREF=\"" 
			+ sRedirectString
			+ "&" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + sNextStartingDate
			+ "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + sNextEndingDate
			+ "\">" + "Next<BR>" + sInterval + "</A>";

		out.println("<th width=125px>"
				+ "<table width=100%><tr><td ALIGN=CENTER>"
				+ "<FONT SIZE=2>" + sLinkToPreviousWeek + "</FONT>"
				+ "</td><td ALIGN=CENTER>"
				+ "<FONT SIZE=2>" + sLinkToNextWeek + "</FONT>"
				+ "</td></tr></table>"
				+ "</th>"
		);
	
		Calendar calStart = Calendar.getInstance();
		try {
			calStart.setTime(dateFormat.parse(sStartingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing starting date - " + e1.getMessage();
			return false;
		}
		Calendar calEnd = Calendar.getInstance();
		try {
			calEnd.setTime(dateFormat.parse(sEndingDate));
		} catch (ParseException e1) {
			m_sErrorMessage = "Error parsing ending date - " + e1.getMessage();
			return false;
		}

		while (calStart.compareTo(calEnd) <= 0){
			out.println("<th>"
					+ clsDateAndTimeConversions.CalendarToString(calStart, "E")
					+ "&nbsp;"
					+ "<A HREF=\"" 
					+ sRedirectString.replace("SMViewAppointmentCalendarGenerate", "SMMapDisplayAppointments")
					+ "&" + MAPDATEPARAMETER + "=" + clsDateAndTimeConversions.CalendarToString(calStart, "MM/dd/yyyy")
					+ "\">Map</A>"
					+ "<BR>"
					+ clsDateAndTimeConversions.CalendarToString(calStart, "MM/dd/yyyy")
					+ "</th>"
					
			);
			calStart.add(Calendar.DATE, 1);
		}
		out.println("</tr>");
		return true;
	}

	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	
	private class CalendarDayCell{
		//User and date make up a calendar day cell
		public String sUser = "";
		public String sDate = "";
		//This holds all the entry info in a day cell
		public ArrayList<SMAppointment> entryInfo = new ArrayList<SMAppointment>();
		
		
	}
}

