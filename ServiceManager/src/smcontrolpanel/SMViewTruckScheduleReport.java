package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablemechanicservicetypes;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMViewTruckScheduleReport extends java.lang.Object{

	public static String TEMPTABLE_BASE = "TRUCKSCHEDDAYS";
	public static String TEMPTABLE_FIELD_ID = "id";
	public static String TEMPTABLE_FIELD_SCHEDULEDATE = "datscheduleddate";
	public static String TEMPTABLE_FIELD_MECHID = "imechid";
	public static String TEMPTABLE_FIELD_SEQUENCE = "isequence";
	public static String TEMPTABLE_FIELD_ORDERNUMBER = "sordernumber";
	public static int TEMPTABLE_FIELD_ORDERNUMBER_LENGTH = 8;
	public static String TEMPTABLE_FIELD_SCHEDULECOMMENT = "sschedulecomment";
	public static int TEMPTABLE_FIELD_SCHEDULECOMMENT_LENGTH = 255;
	public static String TEMPTABLE_FIELD_WORKORDERID = "lworkorderid";
	public static String TEMPTABLE_FIELD_TOTALHOURS = "bdtotalhours";
	public static String TEMPTABLE_FIELD_WORKORDERCREATED = "iworkordercreated";
	public static String TEMPTABLE_FIELD_WORKORDERIMPORTED = "iworkorderimported";
	public static String TEMPTABLE_FIELD_WORKORDERPOSTED = "iworkorderposted";
	private static String CELL_SHADED = "#EBEBEB";
	//private static String CELL_SHADED = "#FFFFFF";
	private static String CELL_UNSHADED = "#FFFFFF";
	private static String TODAYS_CELL_SHADED = "#DAE6EE";
	//private static String TODAYS_CELL_UNSHADED = "#FFFF80";
	private static String TODAYS_CELL_UNSHADED = "#E9F7FF";
	public static String SERVICETYPE_COLOR = "#CF6031";
	private static String SCHEDULECOMMENT_COLOR = "RED";
	private static String SHIP_TO_CITY_COLOR = "GREEN";
	public static String WORK_ORDER_STATUS_COLOR = "BLACK";
	public static String TRUCKSCHEDULEQUERYSTRING = "TruckScheduleQueryString";
	public static String MAPDATEPARAMETER = "MAPDATE";
	private static String MOVE_JOB_LEFT_SYMBOL = "<";
	private static String MOVE_JOB_RIGHT_SYMBOL = ">";
	private static String COPY_JOB_LEFT_SYMBOL = "<<";
	private static String COPY_JOB_RIGHT_SYMBOL = ">>";
	private static String MOVE_JOB_UP_SYMBOL = "^";
	private static String MOVE_JOB_DOWN_SYMBOL = "v";
	
	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	//private long lTimer;
	private String sLinkToPreviousWeek = "";
	private String sLinkToNextWeek = "";
	private String sInterval = "";
	public SMViewTruckScheduleReport(
	){
		m_sErrorMessage = "";
	}
	public void initializeReport(
			ArrayList<String>sLocations,
			ArrayList<String>sServiceTypes,
			String sStartingDate,
			String sEndingDate,
			boolean bDateRangeChosen,
			boolean bDateRangeToday,
			boolean bDateRangeThisWeek,
			boolean bDateRangeNextWeek,
			boolean bAllowScheduleEditing,
			boolean bAllowCopyOrMoveEditing,
			boolean bOnlyShowZeroHours,
			boolean bLookUpMechanic,
			String sLastEntryEdited,
			String sMechanicInitials,
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
			throw new Exception("Error [1428686581] getting connection - " + e.getMessage());
		}
		if (conn == null){
			throw new Exception("WARNING: Could not get data connection.");
		}
		processReport(
			conn,
			sLocations,
			sServiceTypes,
			sStartingDate,
			sEndingDate,
			bDateRangeChosen,
			bDateRangeToday,
			bDateRangeThisWeek,
			bDateRangeNextWeek,
			bAllowScheduleEditing,
			bAllowCopyOrMoveEditing,
			bOnlyShowZeroHours,
			bLookUpMechanic,
			sLastEntryEdited,
			sMechanicInitials,
			sUserID,
			sInstanceMarker,
			sDBID,
			out,
			bOutputToCSV,
			context,
			sLicenseModuleLevel);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080680]");
		return;
	}
	private void processReport(
			Connection conn,
			ArrayList<String>sLocations,
			ArrayList<String>sServiceTypes,
			String sStartingDate,
			String sEndingDate,
			boolean bDateRangeChosen,
			boolean bDateRangeToday,
			boolean bDateRangeThisWeek,
			boolean bDateRangeNextWeek,
			boolean bAllowScheduleEditing,
			boolean bAllowCopyOrMoveEditing,
			boolean bOnlyShowZeroHours,
			boolean bLookUpMechanic,
			String sLastEntryEdited,
			String sMechanicInitials,
			String sUserID,
			String sInstanceMarker,
			String sDBID,
			PrintWriter out,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel) throws Exception{
		
		String SQL = "";
		
		String sTempDayTable = TEMPTABLE_BASE + Long.toString(System.currentTimeMillis());
		try {
			createTemporaryTruckScheduleTable(
					conn,
					sLocations,
					sServiceTypes,
					sStartingDate,
					sEndingDate,
					bOnlyShowZeroHours,
					sMechanicInitials,
					sTempDayTable,
					context)
			;
		} catch (Exception e2) {
			SQL = "DROP TABLE IF EXISTS " + sTempDayTable;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				//Don't stop for this:
			}
			throw new Exception("Error [1428685612] - could not create temporary table - " + e2.getMessage());
		}

		printTableLayout(out);
		
		SQL = "SELECT"
			+ " " + sTempDayTable + ".*"
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.lid
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sAssistant
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sstartingtime
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sVehicleLabel
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
			+ ", NOW() AS CURRENTDATE"
			+ " FROM " + sTempDayTable + " LEFT JOIN " + SMTablemechanics.TableName
			+ " ON " + sTempDayTable + "." + TEMPTABLE_FIELD_MECHID + " = " 
			+ SMTablemechanics.TableName + "." + SMTablemechanics.lid
			+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON "
			+ sTempDayTable + "." + TEMPTABLE_FIELD_ORDERNUMBER + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			;

		//if (bOnlyShowZeroHours){
		//	SQL += " WHERE ("
		//			+ "(" + TEMPTABLE_FIELD_TOTALHOURS + " = 0.00)"
		//		+ ")"
		//	;
		//}
		SQL += " ORDER BY" 
			+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName
			+ ", " + TEMPTABLE_FIELD_SCHEDULEDATE 
			+ ", " + TEMPTABLE_FIELD_SEQUENCE 
			+ ", " + TEMPTABLE_FIELD_WORKORDERID
			;
		//System.out.println("[1427129486] " + SQL);
		//Check permissions for editing schedule:
		boolean bConfigureWorkOrdersPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMConfigureWorkOrders,
				sUserID,
				conn,
				sLicenseModuleLevel) && bAllowScheduleEditing;

		//Check permissions for viewing orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);

		//boolean bEditSiteTimes = 
		//	(SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditArrivedAtCurrentSiteTime, sUserName, conn))
		//	|| (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditArrivedAtNextSiteTime, sUserName, conn))
		//	|| (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditLeftCurrentSiteTime, sUserName, conn))
		//	|| (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditLeftPreviousSiteTime, sUserName, conn))
		//;
		
		//Check permissions for editing work orders:
		boolean bEditWorkOrders = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditWorkOrders, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		
		//Put it all inside a form, so we can use buttons and things:
		out.println("<FORM ID='" + "MAINFORM" + "' NAME='" + "MAINFORM" + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMTruckScheduleEditHandler"
			+ " METHOD = 'POST'"
			+ ">"
		);
		
		/*
	    out.println("\n"
	    	//+ "<form>" 
	    	+ "<input type=button" 
	    	+ " value=\"insert button text here\""
	    	+ " onClick=\"self.location='http://google.com'\">"
	    	//+ " </form>" 
	    );
		*/
		
		printTableLayout(out);
		String sNavigationString = SMUtilities.getURLLinkBase(context) 
		+ "smcontrolpanel.SMViewTruckScheduleGenerate"
		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
		for (int i = 0; i < sLocations.size(); i++){
			sNavigationString += "&" + SMViewTruckScheduleSelection.LOCATION_PARAMETER + sLocations.get(i) + "=Y";
		}
		for (int i = 0; i < sServiceTypes.size(); i++){
			sNavigationString += "&" + SMViewTruckScheduleSelection.SERVICETYPE_PARAMETER + sServiceTypes.get(i) + "=Y";
		}
		//For the navigation links, we want the date range parameter to indicate that the user has chosen dates:
		sNavigationString += "&" + SMViewTruckScheduleSelection.DATE_RANGE_PARAM 
		+ "=" + SMViewTruckScheduleSelection.DATE_RANGE_CHOOSE; 

		//If the user chose to edit the schedule, we need to pass in that parameter:
		if (bAllowScheduleEditing){
			sNavigationString += "&" + SMViewTruckScheduleSelection.EDITSCHEDULE_PARAMETER + "=Y"; 
		}
		
		//If the user chose to edit with the move and copy buttons, we need to pass in that parameter:
		if (bAllowCopyOrMoveEditing){
			sNavigationString += "&" + SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER + "=Y"; 
		}

		//If the user chose to ONLY see zero-hour entries, we need to pass in that parameter:
		if (bOnlyShowZeroHours){
			sNavigationString += "&" + SMViewTruckScheduleSelection.ONLYSHOWZEROHOURS_PARAMETER + "=Y"; 
		}

		sNavigationString += "&" + SMViewTruckScheduleSelection.MECHANIC_PARAMETER + "=" + sMechanicInitials;
		
		if (bLookUpMechanic){
			sNavigationString += "&" + SMViewTruckScheduleSelection.LOOKUPMECHANIC_PARAMETER + "=Y"; 
		}
		
		printDateHeadings(sStartingDate, sEndingDate, sMechanicInitials, sNavigationString, context, out);
		Long lCurrentMechanic = 0L;
		String sCurrentDate = "";
		boolean bShadeRow = true;

		long lTimer = System.currentTimeMillis();
		try{
			//if (bDebugMode){
			//	System.out.println("In " + this.toString() + " SQL: " + SQL);
			//}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (bDebugMode){
				System.out.println("In " + this.toString() + " Main SELECT SQL took " 
						+ (System.currentTimeMillis() - lTimer) + " milliseconds");
			}
			boolean bInitialInsertLinkHasBeenPrinted = false;
			//This variable tells us which entry we are on for that day and mechanic - in other words,
			//if this is the second job for that mechanic on that day, this number will be 2.  This is 
			//different than the job sequence because that may or may not be zero-based, for example.
			long lEntryForTheDay = 0;
			while(rs.next()){
				//If there was already at least one cell AND it's starting a NEW cell, close the last cell:
				long lJobOrder = rs.getLong(TEMPTABLE_FIELD_SEQUENCE);
				long lWorkOrderID = rs.getLong(TEMPTABLE_FIELD_WORKORDERID);
				if (
						(sCurrentDate.compareToIgnoreCase("") != 0)
						&& 
						//EITHER the date OR the mechanic has changed: then we know it's a new cell:
						(
								(sCurrentDate.compareToIgnoreCase(clsDateAndTimeConversions.resultsetDateStringToString(
										rs.getString(TEMPTABLE_FIELD_SCHEDULEDATE))) != 0)
										||
										(lCurrentMechanic != (rs.getLong(TEMPTABLE_FIELD_MECHID)))
						)
				){
					out.println("</TD>");
					//Clear the flag indicating we have printed an 'Insert' link:
					bInitialInsertLinkHasBeenPrinted = false;
					//Reset the 'entry for the day' counter:
					lEntryForTheDay = 0;
				}

				//If there was already one mechanic, and it's now a NEW mechanic, print the end of the line:
				if (
						(lCurrentMechanic != 0)
						&& 
						(lCurrentMechanic != rs.getLong(TEMPTABLE_FIELD_MECHID))
				){
					out.println("</TR>");
					//Toggle the 'shader':
					bShadeRow = !bShadeRow;
				}

				//If it's a new mechanic, print the left side heading:
				if (lCurrentMechanic != rs.getLong(TEMPTABLE_FIELD_MECHID)){
					String sMechanicName = rs.getString(
							SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName);
					if (
							(sMechanicName == null)
							|| (sMechanicName.compareToIgnoreCase("") == 0)
					){
						sMechanicName = "(UNKNOWN)";
						if (bDebugMode){
							sMechanicName = Long.toString(rs.getLong(TEMPTABLE_FIELD_MECHID));
						}
					}
					String sMechanicCellClass = "\"mechanicunshaded\"";
					if (bShadeRow){
						sMechanicCellClass = "\"mechanicshaded\"";
					}
					out.println(
							"<TR>"
							+ "<TD class=" + sMechanicCellClass + ">"
							+ sMechanicName
							+ "</TD>"
					);
				}

				//If it's a new mechanic OR a new date, print a new cell:
				String sOrderNumber = rs.getString(TEMPTABLE_FIELD_ORDERNUMBER);
				String sOrderNumberLink = "";
				//If the user is allowed to view the order, give them a link:
				if (
						(sOrderNumber == null)
						|| (sOrderNumber.compareToIgnoreCase("") == 0)
						|| (sOrderNumber.compareToIgnoreCase(SMTableworkorders.DUMMY_JOB_NUMBER) == 0)
				){
					//It's unscheduled
					sOrderNumberLink = "";
					sOrderNumber = "";
				}else{
					if (bViewOrderPermitted){
						sOrderNumberLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMDisplayOrderInformation"
							+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>"
							;
					}else{
						sOrderNumberLink = sOrderNumber;
					}
				}

				//Prepare to print the actual schedule entry:
				String sEntryCellClass = "\"entryunshaded\"";
				if (bShadeRow){
					sEntryCellClass = "\"entryshaded\"";
				}
				//If this entry is for the current day, use the 'Today' colors for the cell:
				if (rs.getDate(TEMPTABLE_FIELD_SCHEDULEDATE).compareTo(rs.getDate("CURRENTDATE")) == 0){
					if (bShadeRow){
						sEntryCellClass = "\"currentdayentryshaded\"";
					}else{
						sEntryCellClass = "\"currentdayentryunshaded\"";
					}
				}
				
				String sBillToName = rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sBillToName);
				if (sBillToName == null){
					sBillToName = "";
				}
				String sServiceType = rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sServiceTypeCode);
				if (sServiceType == null){
					sServiceType = "";
				}
				//Print the actual entry:
				if (
						(lCurrentMechanic != rs.getLong(TEMPTABLE_FIELD_MECHID))
						|| (sCurrentDate.compareToIgnoreCase(clsDateAndTimeConversions.resultsetDateStringToString(
								rs.getString(TEMPTABLE_FIELD_SCHEDULEDATE))) != 0)
				){
					out.println("<TD class = " + sEntryCellClass + ">");
				}

				if (bDebugMode){
					out.println("ID: '" + rs.getLong(TEMPTABLE_FIELD_ID) + "'<BR>"
							+ "MECHID: '" + Long.toString(rs.getLong(TEMPTABLE_FIELD_MECHID)) + "'<BR>" 
							+ "ORD#: '" + rs.getString(TEMPTABLE_FIELD_ORDERNUMBER) + "'<BR>"
							+ "DATE: '" + rs.getString(TEMPTABLE_FIELD_SCHEDULEDATE) + "'<BR>"
							+ "COMMENT: '" + rs.getString(TEMPTABLE_FIELD_SCHEDULECOMMENT) + "'<BR>"
							+ "SEQ: '" + lJobOrder + "'<BR>"
							+ "WORKORDERID: '" + rs.getLong(TEMPTABLE_FIELD_WORKORDERID) + "'<BR>"
							+ "HRS: '" + rs.getBigDecimal(TEMPTABLE_FIELD_TOTALHOURS) + "'<BR>"
					);
				}

				//IF the user can edit, ALWAYS print an 'ADD' at the top of the cell:
				//And if the user can edit, we ALWAYS print a link to 'INSERT' a new entry at the top of the cell:
				if (bConfigureWorkOrdersPermitted && !bInitialInsertLinkHasBeenPrinted){
					out.println("<A HREF=\""
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMConfigWorkOrderEdit"
						+ "?" + SMWorkOrderHeader.Paramlid + "=-1"
						+ "&" + SMWorkOrderHeader.Paramscheduleddate + "=" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(
								rs.getString(TEMPTABLE_FIELD_SCHEDULEDATE))
						+ "&" + SMWorkOrderHeader.Paramimechid + "=" 
							+ rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.lid)
						+ "&" + SMWorkOrderHeader.Paramijoborder + "=1" 
						+ "&" + SMWorkOrderHeader.Paramsassistant + "=" 
							+ rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sAssistant)
						+ "&" + SMWorkOrderHeader.Paramsstartingtime + "=" 
							+ rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sstartingtime)
						+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
						+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
						//This parameter will tell 'action' class to bring us back here
						//after the editing:
						+ "&" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "=Y"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "Add&nbsp;job" + "</A>")
															;
					bInitialInsertLinkHasBeenPrinted = true;
				}

				//If the entry has a valid Job Cost ID, then we know it's a real entry, not just
				// a 'filler' to fill in the schedule:
				if (lWorkOrderID != -1){
					//Count the entry here:
					lEntryForTheDay++;
					//If it's NOT the first entry, OR the 'Insert' link has been printed, add a hard return:
					if ((lEntryForTheDay > 1) || bInitialInsertLinkHasBeenPrinted){
						out.println("<BR>");
					}
					//If this was the last entry edited, place a bookmark here:
					if (sLastEntryEdited.compareToIgnoreCase(Long.toString(lWorkOrderID)) == 0){
						out.println("<a name=\"LastEdit\">");
					}

					//Add a link to edit - put it on the 'number of hours':
					String sTotalHoursLink = " (" + rs.getBigDecimal(TEMPTABLE_FIELD_TOTALHOURS) + " hrs) ";
					//If the user can EITHER edit the schedule OR edit the site times, he should get a link here to go to the Job Cost entry:
					if (bConfigureWorkOrdersPermitted){
						sTotalHoursLink = "<A HREF=\""
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMConfigWorkOrderEdit"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"

							//This parameter will tell 'action' class to bring us back here
							//after the editing:
							+ "&" + SMWorkOrderHeader.RETURN_TO_TRUCKSCHEDULE_PARAM + "=Y" 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">"
							+ sTotalHoursLink 
							+ "</A>"
							;
					}
					String sShipToCity = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity);
					if (sShipToCity == null){
						sShipToCity = "";
					}
					
					/*
					WO-E - work order exists (was configured OR items have been added to it)
					WO-I - work order exists and was imported
					WO-P - work order exists and was posted
					WO-IP - work order exists and was imported AND posted
					*/

					String sWorkOrderStatus = "";

					//WO-E
					if (
						(rs.getInt(TEMPTABLE_FIELD_WORKORDERCREATED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERIMPORTED) == 0)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERPOSTED) == 0)
					){
						sWorkOrderStatus = "WO-E";
					}

					//WO-I
					if (
						(rs.getInt(TEMPTABLE_FIELD_WORKORDERCREATED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERIMPORTED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERPOSTED) == 0)
					){
						sWorkOrderStatus = "WO-I";
					}

					//WO-P
					if (
						(rs.getInt(TEMPTABLE_FIELD_WORKORDERCREATED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERIMPORTED) == 0)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERPOSTED) == 1)
					){
						sWorkOrderStatus = "WO-P";
					}
					
					//WO-IP
					if (
						(rs.getInt(TEMPTABLE_FIELD_WORKORDERCREATED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERIMPORTED) == 1)
						&& (rs.getInt(TEMPTABLE_FIELD_WORKORDERPOSTED) == 1)
					){
						sWorkOrderStatus = "WO-IP";
					}
					String sWorkOrderStatusLink = sWorkOrderStatus;
					if(bEditWorkOrders){
						sWorkOrderStatusLink =
							"<span style= \"white-space: nowrap;\" >"
							+ "-"
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMWorkOrderEdit?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getLong(TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">"
							+ sWorkOrderStatus + "</A>"
							+ "</span>";
					}else{
						sWorkOrderStatusLink =
							"<span style= \"white-space: nowrap;\" >"
							+ "-"
							+ sWorkOrderStatus
							+ "</span>"
						;
					}

					out.println(
						(sOrderNumberLink + " " 
							+ sBillToName.trim()
							+ " "
							+ "<FONT COLOR=" + SHIP_TO_CITY_COLOR + ">"
							+ sShipToCity + "</FONT>&nbsp;"
							+ "<I><FONT COLOR=" + SCHEDULECOMMENT_COLOR + ">" 
							+ rs.getString(TEMPTABLE_FIELD_SCHEDULECOMMENT) + "</FONT></I>"
							+ " "
							+ sTotalHoursLink
							+ "<B><FONT COLOR=" + SERVICETYPE_COLOR + "></FONT></B>" + sServiceType
							+ " "
							+ "<B><FONT COLOR=" + WORK_ORDER_STATUS_COLOR + ">" + sWorkOrderStatusLink + "</FONT></B>" ).trim()
					);
					//Add a link to edit:
					if (bConfigureWorkOrdersPermitted && bAllowCopyOrMoveEditing){
						//If the user can edit, add a link to edit the schedule:
						out.println("<BR>");
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + COPY_JOB_LEFT_SYMBOL + "\""
						    + "\" title=\"COPY TO the previous day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_COPYLEFT)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + COPY_JOB_LEFT_SYMBOL
						    + "</button>"
						    );
					    				
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + MOVE_JOB_LEFT_SYMBOL + "\""
						    + "\" title=\"MOVE TO the previous day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_MOVELEFT)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + MOVE_JOB_LEFT_SYMBOL
						    + "</button>"
						    );
												
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + MOVE_JOB_UP_SYMBOL + "\""
						    + "\" title=\"MOVE UP in the same day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_MOVEUP)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + MOVE_JOB_UP_SYMBOL
						    + "</button>"
						    );
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + MOVE_JOB_DOWN_SYMBOL + "\""
						    + "\" title=\"MOVE DOWN in the same day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_MOVEDOWN)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + MOVE_JOB_DOWN_SYMBOL
						    + "</button>"
						    );
						//MOVE TO TOMORROW:
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + MOVE_JOB_RIGHT_SYMBOL + "\""
						    + "\" title=\"MOVE TO the next day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_MOVERIGHT)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + MOVE_JOB_RIGHT_SYMBOL
						    + "</button>"
						    );
						//COPY TO THE NEXT DAY:
					    out.println("<button"
					    	+ " type=\"button\""
						    + " value=\"" + COPY_JOB_RIGHT_SYMBOL + "\""
						    + "\" title=\"COPY TO the next day\""
						    + " onClick=\"self.location='"
						    
						    + SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMTruckScheduleEditHandler"
							+ "?" + SMWorkOrderHeader.Paramlid + "=" 
							+ rs.getString(sTempDayTable + "." + TEMPTABLE_FIELD_WORKORDERID)
							+ "&CallingClass=smcontrolpanel.SMViewTruckSchedule"
							+ "&" + SMTruckScheduleEditHandler.EDITENTRYMODE + "="
							+ Integer.toString(SMTruckScheduleEditHandler.EDITENTRY_COPYRIGHT)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						    
						    + "'\""
						    + ">"
						    + COPY_JOB_RIGHT_SYMBOL
						    + "</button>"
						    );
					}
				}

				lCurrentMechanic = rs.getLong(TEMPTABLE_FIELD_MECHID);
				sCurrentDate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(TEMPTABLE_FIELD_SCHEDULEDATE));
			}
			rs.close();
		}catch (SQLException e){
			try {
				Statement stmt = conn.createStatement();
				stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
			} catch (SQLException e1) {
				//Don't stop for this:
			}
			throw new Exception("Error [1427129335] reading schedule with SQL - " + SQL + " - " + e.getMessage());
		}
		//Put the last cell ending in:
		out.println("</TD>");
		//Put the last line ending in:
		out.println("</TR>");
		
		out.println("</table>");

		//Add links to the previous and next week:
		out.println("<FONT SIZE=2>" 
				+ sLinkToPreviousWeek.replace("<BR>", "&nbsp;")
				+ "&nbsp;&nbsp;&nbsp;"
				+ sLinkToNextWeek.replace("<BR>", "&nbsp;") + "</FONT>"
		);
		
		//Close the form:
		out.println("</FORM>");
		
		//Now get rid of the temporary table:
		SQL = "DROP TABLE IF EXISTS " + sTempDayTable;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			//Don't stop for this:
		}


	}
	
	public void createTemporaryTruckScheduleTable(
			Connection conn,
			ArrayList<String>sLocations,
			ArrayList<String>sServiceTypes,
			String sStartingDate,
			String sEndingDate,
			boolean bOnlyShowZeroHours,
			String sMechanicInitials,
			String sTempTable,
			ServletContext context
	) throws Exception{
		String SQL = "";
		if(sLocations.size() == 0){
			throw new Exception("You must select a mechanic location");
		}
		if(sServiceTypes.size() == 0){
			throw new Exception("You must select at least one service type");
		}
		String sLocationsString = "";
		for (int i = 0; i < sLocations.size(); i++){
			sLocationsString += sLocations.get(i) + ",";
		}

		long lStartingTime = System.currentTimeMillis();

		SQL = "DROP TABLE IF EXISTS " + sTempTable;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			//Don't stop for this:
		}

		SQL = "CREATE TEMPORARY TABLE " + sTempTable + "("
		+ TEMPTABLE_FIELD_ID + " int(10) unsigned NOT NULL auto_increment"
		+ ", " + TEMPTABLE_FIELD_SCHEDULEDATE + " datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
		+ ", " + TEMPTABLE_FIELD_MECHID + " int(11) NOT NULL DEFAULT '0'"
		+ ", " + TEMPTABLE_FIELD_SEQUENCE + " INT(11) NOT NULL DEFAULT '0'"
		+ ", " + TEMPTABLE_FIELD_ORDERNUMBER + " varchar(" + Integer.toString(TEMPTABLE_FIELD_ORDERNUMBER_LENGTH)
		+ ") NOT NULL DEFAULT ''"
		+ ", " + TEMPTABLE_FIELD_SCHEDULECOMMENT + " varchar(" + Integer.toString(
				TEMPTABLE_FIELD_SCHEDULECOMMENT_LENGTH) + ") NOT NULL DEFAULT ''"
		+ ", " + TEMPTABLE_FIELD_WORKORDERID + " int(11) NOT NULL DEFAULT '-1'"
		+ ", " + TEMPTABLE_FIELD_TOTALHOURS + " DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
		
		// TJR - 5/9/2014 - added these fields to get work order status:
		+ ", " + TEMPTABLE_FIELD_WORKORDERCREATED + " int(11) NOT NULL DEFAULT '0'"
		+ ", " + TEMPTABLE_FIELD_WORKORDERIMPORTED + " int(11) NOT NULL DEFAULT '0'"
		+ ", " + TEMPTABLE_FIELD_WORKORDERPOSTED + " int(11) NOT NULL DEFAULT '0'"
		
		+ ", PRIMARY KEY  (" + TEMPTABLE_FIELD_ID + ")"
		+ ", KEY mechkey (" + TEMPTABLE_FIELD_MECHID + ")"
		+ ", KEY ordernumberkey (" + TEMPTABLE_FIELD_ORDERNUMBER + ")"
		+ ", KEY scheduledatekey (" + TEMPTABLE_FIELD_SCHEDULEDATE + ")"
		//+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1"
		+ ") DEFAULT CHARSET=latin1"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("SQL: '" + SQL + "' - " + e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 9 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}

		ArrayList<Long>lDisplayedMechanics = new ArrayList<Long>(0);
		
		//If they have not selected just a single mechanic, we have to figure out which mechanics we need:
		if (sMechanicInitials.compareToIgnoreCase("") != 0){
			SQL = "SELECT"
				+ " " + SMTablemechanics.lid
				+ ", " + SMTablemechanics.sMechInitial
				+ ", " + SMTablemechanics.sMechFullName
				+ ", " + SMTablemechanics.sVehicleLabel
				+ " FROM " + SMTablemechanics.TableName
				+ " WHERE (" + SMTablemechanics.sMechInitial + " = '" + sMechanicInitials + "')"
				;
		}else{
			SQL = "SELECT DISTINCT "
				+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.lid
				+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial
				+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName
				+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sVehicleLabel
				+ " FROM " + SMTablemechanicservicetypes.TableName
				+ " LEFT JOIN " + SMTablemechanics.TableName 
				+ " ON " + SMTablemechanics.TableName + "." +  SMTablemechanics.lid 
				+ " = " + SMTablemechanicservicetypes.TableName + "." + SMTablemechanicservicetypes.imechanicid
				+ " WHERE ("
				+ "(INSTR('" + sLocationsString + "', " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechLocation + ") > 0)"
				+ " AND ("
				;
			for (int i = 0; i < sServiceTypes.size(); i ++){
				if (i == 0){
					SQL += "(" + SMTablemechanicservicetypes.sservicetypecode + " = '" + sServiceTypes.get(i) + "')";
				}else{
					SQL += " OR (" + SMTablemechanicservicetypes.sservicetypecode + " = '" + sServiceTypes.get(i) + "')"; 
				}
			}
			SQL +=  ")"
				+ ")"
				;
			//System.out.println("[1385411344] SQL: " + SQL);
			if (bDebugMode){
				System.out.println("[1385411344] In " + this.toString() + " read mechanics SQL: " + SQL);
			}
		}	
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				lDisplayedMechanics.add(rs.getLong(SMTablemechanics.lid));
				//System.out.println("[1385411345] " + rs.getString(SMTablemechanics.sMechFullName) + " add into the display list.");
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1428685758] reading eligible mechanics - " + e.getMessage());
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 10 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}

		/*
		 * TJR - Changed this code 9/22/2011 - the old logic was that as long as the JOB was at
		 * one of the selected locations, we would grab it.  But the NEW logic is that the selected location
		 * ONLY refers to the MECHANIC, so if the MECHANIC is not assigned to the location selected, EVEN
		 * IF THE JOB IS IN A SELECTED LOCATION, that job and mechanic WON'T appear in the schedule.

		//Now we have to add any mechanics doing the selected type of jobs, which are NOT among the 
		//selected type of mechanics.  This can happen, for example, if we select only commercial
		//installation for the job type, but we have a residential mechanic doing a commercial installation
		//order types:
		SQL = "SELECT"
			+ " DISTINCT " + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn
			+ " FROM " + SMTablejobcost.TableName
			+ " WHERE ("
			+ "(" + SMTablejobcost.TableName + "." + SMTablejobcost.datDate + " >= '"
			+ SMUtilities.stdDateStringToSQLDateString(sStartingDate) + " 00:00:00')"
			+ " AND (" + SMTablejobcost.TableName + "." + SMTablejobcost.datDate + " <= '"
			+ SMUtilities.stdDateStringToSQLDateString(sEndingDate) + " 23:59:59')"
			;

		SQL += " AND (";
		for (int i = 0; i < sServiceTypes.size(); i++){
			if (i == 0){
				SQL += " (" + SMTablejobcost.TableName + "." + SMTablejobcost.sservicecode
				+ " = '" + sServiceTypes.get(i) + "')";
			}else{
				SQL += " OR (" + SMTablejobcost.TableName + "." + SMTablejobcost.sservicecode
				+ " = '" + sServiceTypes.get(i) + "')";
			}
		}
		SQL += ")";  // End the 'AND' clause
		//Get the locations:
			SQL += " AND (INSTR('" + sLocationsString + "', " 
			+ SMTablejobcost.TableName + "." + SMTablejobcost.slocation + ") > 0)"
			;

		//End the 'where' clause:
		SQL += ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " read mechanics from job cost SQL: " + SQL);
		}
		 */

		//This is the NEWER logic, added 9/22/2011 - TJR:
		//Now we have to add any mechanics doing the selected type of jobs, which are NOT among the 
		//selected type of mechanics.  This can happen, for example, if we select only commercial
		//installation for the job type, but we have a residential mechanic doing a commercial installation
		//order type.
		//But if the mechanic is NOT ASSIGNED to a location selected, the job won't appear on the schedule, no
		//matter what:
		SQL = "SELECT"
			+ " DISTINCT " + SMTableworkorders.TableName + "." + SMTableworkorders.imechid
			+ " FROM " + SMTableworkorders.TableName + " LEFT JOIN " + SMTablemechanics.TableName
			+ " ON " + SMTableworkorders.TableName + "." + SMTableworkorders.imechid + " = "
			+ SMTablemechanics.TableName + "." + SMTablemechanics.lid
			+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " WHERE ("
			+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " >= '"
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate) + " 00:00:00')"
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " <= '"
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate) + " 23:59:59')"
		 	//NO QUOTES!
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
				+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
			;

		//If the user selected one mechanic, we'll get ALL his schedule entries, so we don't need this:
		if (sMechanicInitials.compareToIgnoreCase("") == 0){
			SQL += " AND (";
			for (int i = 0; i < sServiceTypes.size(); i++){
				if (i == 0){
					SQL += " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
					+ " = '" + sServiceTypes.get(i) + "')";
				}else{
					SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
					+ " = '" + sServiceTypes.get(i) + "')";
				}
			}
			SQL += ")";  // End the 'AND' clause
			//Get the locations:
			SQL += " AND (INSTR('" + sLocationsString + "', " 
			+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechLocation + ") > 0)"
			;
		}else{
			//If the user selected a single mechanic:
			try {
				SQL += " AND (" + SMTablemechanics.TableName + "." + SMTablemechanics.lid + " = " 
					+ Long.toString(lDisplayedMechanics.get(0)) + ")";
			} catch (Exception e) {
				throw new Exception("Error [1413643341] creating SQL command for a single mechanic - " + e.getMessage());
			}
		}
		//End the 'where' clause:
		SQL += ")";
		//System.out.println("[1385411346] SQL #2 = " + SQL);
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " read mechanics from job cost SQL: " + SQL);
		}

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				if(!lDisplayedMechanics.contains(rs.getLong(SMTableworkorders.TableName 
						+ "." + SMTableworkorders.imechid))){
					lDisplayedMechanics.add(rs.getLong(SMTableworkorders.TableName 
							+ "." + SMTableworkorders.imechid));
				}
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1428685821] reading mechanics from schedule with SQL: " + SQL + " - " + e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 11 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}		
		//Now we have all the mechanics listed.

		if (lDisplayedMechanics.size() == 0){
			throw new Exception("Error [1428685822] your selection criteria are too limited to include ANY mechanics at all.");
		}
		
		//Now we build out our 'empty' days as records:
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calStart = Calendar.getInstance();
		try {
			calStart.setTime(dateFormat.parse(sStartingDate));
		} catch (ParseException e1) {
			throw new Exception("Error [1428685881] parsing starting date '" + sStartingDate + "' - " + e1.getMessage());
		}
		Calendar calEnd = Calendar.getInstance();
		try {
			calEnd.setTime(dateFormat.parse(sEndingDate));
		} catch (ParseException e1) {
			throw new Exception("Error [1428685882] parsing ending date '" + sEndingDate + "' - " + e1.getMessage());
		}

		if (bDebugMode){
			System.out.println("sDisplayedMechanics:");
			for (int i = 0; i < lDisplayedMechanics.size(); i++){
				System.out.println(i + " - " + lDisplayedMechanics.get(i));
			}
		}

		/*
		 * TJR - 9/24/2011 - removed these 'LOCK' commands because we had trouble with other tables 
		 * being locked when we left them in:
		SQL = "LOCK TABLES " + m_sTempDayTable + " WRITE";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			m_sErrorMessage = "Error locking table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		 */
		if (lDisplayedMechanics.size() > 0){
			while (calStart.compareTo(calEnd) <= 0){
				SQL = "INSERT INTO " + sTempTable + "("
				+ TEMPTABLE_FIELD_SCHEDULEDATE
				+ ", " + TEMPTABLE_FIELD_MECHID
				+ ") VALUES "
				;
	
				for (int i = 0; i < lDisplayedMechanics.size(); i++){
					//Insert our records here:
					if (i > 0){
						SQL += ",";
					}
					SQL += " ("
						+ "'" + clsDateAndTimeConversions.CalendarToString(calStart, "yyyy-MM-dd") + " 00:00:00'"
						+ ", '" + lDisplayedMechanics.get(i) + "'"
						+ ")"
						;
				}
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (SQLException e) {
					throw new Exception("Error [1428685883] inserting blank record with SQL: " + SQL + " - " + e.getMessage());
				}
	
				calStart.add(Calendar.DATE, 1);
			}
		}
		/*
		SQL = "UNLOCK TABLES";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			m_sErrorMessage = "Error unlocking table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		 */
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 12 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}

		//Now we have all of the mechanics' blank schedule records in the system, but we have to update them
		//with any actually scheduled jobs for these mechanics and days:  
		SQL = "UPDATE"
			+ " " + sTempTable + ", " + SMTableworkorders.TableName
			+ " SET " + sTempTable + "." + TEMPTABLE_FIELD_ORDERNUMBER + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber
			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_SEQUENCE + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder
			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_SCHEDULECOMMENT + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.sschedulecomment
			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_WORKORDERID + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.lid
			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_TOTALHOURS + " = "
			+ SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours
			+ " + " + SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours
			+ " + " + SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours

			+ " WHERE ("
			//Link the schedule records to the job cost records:
			//First by mechanic:
			+ "(" + sTempTable + "." + TEMPTABLE_FIELD_MECHID + " = " 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.imechid + ")"
			//Next by day:
			+ " AND (" + sTempTable + "." + TEMPTABLE_FIELD_SCHEDULEDATE + " = " 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + ")"

			//Get only scheduled jobs for the selected dates:
			+ " AND (" + sTempTable + "." + TEMPTABLE_FIELD_SCHEDULEDATE + " >= '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate) + " 00:00:00')"
			+ " AND (" + sTempTable + "." + TEMPTABLE_FIELD_SCHEDULEDATE + " <= '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate) + " 23:59:59')"
			//And only get the scheduled jobs with the '1' sequence - we'll add the 2's, 3's, etc., later:
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder + " = 1)"
			;

		if (bOnlyShowZeroHours){
			SQL += " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours + " = 0.00)"
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours + " = 0.00)"
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours + " = 0.00)"
			;
		}

		SQL += ")" //End the 'WHERE' clause
			;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1428685884] inserting sequence 1 jobs with SQL: " + SQL + " - "+ e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 13 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}
		//Now we add any records for jobs that are scheduled with a '1, 2, 3, etc.' sequence:
		SQL = "INSERT INTO"
			+ " " + sTempTable
			+ " ("
			+ TEMPTABLE_FIELD_MECHID
			+ ", " + TEMPTABLE_FIELD_ORDERNUMBER
			+ ", " + TEMPTABLE_FIELD_SCHEDULEDATE
			+ ", " + TEMPTABLE_FIELD_SEQUENCE
			+ ", " + TEMPTABLE_FIELD_SCHEDULECOMMENT
			+ ", " + TEMPTABLE_FIELD_WORKORDERID
			+ ", " + TEMPTABLE_FIELD_TOTALHOURS
			+ ")"
			+ "SELECT "
			+ SMTableworkorders.imechid
			+ ", " + SMTableworkorders.strimmedordernumber
			+ ", " + SMTableworkorders.datscheduleddate
			+ ", " + SMTableworkorders.ijoborder
			+ ", " + SMTableworkorders.sschedulecomment
			+ ", " + SMTableworkorders.lid
			+ ", " + SMTableworkorders.bdbackchargehours
			+ " + " + SMTableworkorders.bdqtyofhours
			+ " + " + SMTableworkorders.bdtravelhours
			+ " FROM " + SMTableworkorders.TableName
			+ " WHERE ("
			//Get only scheduled jobs for the selected dates:
			+ "(" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " >= '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate) + " 00:00:00')"
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate + " <= '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate) + " 23:59:59')"
			//Only for the selected mechanics:
			+ " AND (";
		try {
			for (int i = 0; i < lDisplayedMechanics.size(); i++){
				if(i == 0){
					SQL += "(" + SMTableworkorders.imechid + " = " + Long.toString(lDisplayedMechanics.get(i)) + ")";
				}else{
					SQL += " OR (" + SMTableworkorders.imechid + " = " + Long.toString(lDisplayedMechanics.get(i)) + ")";
				}
			}
		} catch (Exception e1) {
			throw new Exception("Error [1428685885] listing mechanics - " + e1.getMessage());
		}
		SQL += ")" //End the larger 'AND' phrase

			//And only get the scheduled jobs with greater than a '1' sequence:
			+ " AND (" + SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder + " > 1)"
			;

		if (bOnlyShowZeroHours){
			SQL += " AND (" + SMTableworkorders.bdbackchargehours + " = 0.00)"
			+ " AND (" + SMTableworkorders.bdqtyofhours + " = 0.00)"
			+ " AND (" + SMTableworkorders.bdtravelhours + " = 0.00)"
			;
		}
		SQL += ")" //End the 'WHERE' clause
			;
		if (bDebugMode){
			System.out.println("In " + this.toString() + " adding 2 and 3 records from job cost SQL = " + SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1428685886] inserting sequence 2,3, etc jobs with SQL: " + SQL + " - "+ e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 14 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}
		
		//Now we update the work order status for any records with a work order on them:
		SQL = "UPDATE"
			+ " " + sTempTable
			+ " LEFT JOIN " + SMTableworkorders.TableName + " ON " + sTempTable + "." + TEMPTABLE_FIELD_WORKORDERID + " = "
			+ SMTableworkorders.lid
			+ " SET " + sTempTable + "." + TEMPTABLE_FIELD_WORKORDERCREATED 
			+ " = IF(ISNULL(" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + ")=TRUE,0,1)"
			
			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_WORKORDERIMPORTED
			+ " = IF(ISNULL(" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + ")=TRUE,0," 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.iimported + ")"

			+ ", " + sTempTable + "." + TEMPTABLE_FIELD_WORKORDERPOSTED
			+ " = IF(ISNULL(" + SMTableworkorders.TableName + "." + SMTableworkorders.lid + ")=TRUE,0," 
			+ SMTableworkorders.TableName + "." + SMTableworkorders.iposted + ")"

			;
		if (bDebugMode){
			System.out.println("In " + this.toString() + " adding 2 and 3 records from job cost SQL = " + SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1428685887] inserting work order statuses with SQL: " + SQL + " - "+ e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " time elapsed after SQL 15 = " 
					+ (System.currentTimeMillis() - lStartingTime));
			lStartingTime = System.currentTimeMillis();
		}

		return;
	}
	private void printTableLayout(PrintWriter pwOut){
		pwOut.println("<style type=\"text/css\">");
		pwOut.println(
				"table.trucksched {"
				+ " border-style: solid;"
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
				+ " font-weight:normal;"
				+ " font-size:small;"
				//+ " border-style: none;"
				+ " border-style: solid;"
				+ " border-width:1px;"
				+ " margin:0px;"
				+ " text-align:center;"
				+ " vertical-align:top;"
				+ " background-color: " + CELL_UNSHADED + ";"
				+ "}"	
		);
		pwOut.println(
				"td.entryshaded {"
				+ " font-weight:normal;"
				+ " font-size:small;"
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
				"td.mechanicunshaded {"
				+ " vertical-align:top;"
				+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + CELL_UNSHADED + ";"
				+ "}"	
		);
		pwOut.println(
				"td.mechanicshaded {"
				+ " vertical-align:top;"
				+ " font-weight:bold;"
				+ " font-size:small;"
				//+ " text-decoration:underline;"
				+ " border-style: none;"
				+ " text-align:left;"
				+ " background-color: " + CELL_SHADED + ";"
				+ "}"	
		);

		pwOut.println("</style>");

		pwOut.println("<TABLE class=\"trucksched\">");
	}
	private boolean printDateHeadings(
			String sStartingDate, 
			String sEndingDate,
			String sMechanic,
			String sRedirectString,
			ServletContext context,
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
		out.println("<TR>");
		//Print the first cell with navigation links in it:
		sLinkToPreviousWeek = "<A HREF=\"" 
			+ sRedirectString
			+ "&" + SMViewTruckScheduleSelection.STARTING_DATE_FIELD + "=" + sPreviousStartingDate
			+ "&" + SMViewTruckScheduleSelection.ENDING_DATE_FIELD + "=" + sPreviousEndingDate
			+ "&" + SMViewTruckScheduleSelection.MECHANIC_PARAMETER + "=" + sMechanic
			+ "\">" + "Previous<BR>" + sInterval + "</A>";
		sLinkToNextWeek =  "<A HREF=\"" 
			+ sRedirectString
			+ "&" + SMViewTruckScheduleSelection.STARTING_DATE_FIELD + "=" + sNextStartingDate
			+ "&" + SMViewTruckScheduleSelection.ENDING_DATE_FIELD + "=" + sNextEndingDate
			+ "&" + SMViewTruckScheduleSelection.MECHANIC_PARAMETER + "=" + sMechanic
			+ "\">" + "Next<BR>" + sInterval + "</A>";

		out.println("<TH>"
				+ "<TABLE width=100%><TR><TD ALIGN=CENTER>"
				+ "<FONT SIZE=2>" + sLinkToPreviousWeek + "</FONT>"
				+ "</TD><TD ALIGN=CENTER>"
				+ "<FONT SIZE=2>" + sLinkToNextWeek + "</FONT>"
				+ "</TD></TR></TABLE>"
				+ "</TH>"
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
			out.println("<TH>"
					+ clsDateAndTimeConversions.CalendarToString(calStart, "E")
					+ "&nbsp;"
					+ "<A HREF=\"" 
					+ sRedirectString.replace("SMViewTruckScheduleGenerate", "SMMapDisplay")
					+ "&" + MAPDATEPARAMETER + "=" + clsDateAndTimeConversions.CalendarToString(calStart, "MM/dd/yyyy")
					+ "\">Map</A>"
					
					+ "<BR>"
					+ clsDateAndTimeConversions.CalendarToString(calStart, "MM/dd/yyyy")
					+ "</TH>"
					
			);
			calStart.add(Calendar.DATE, 1);
		}
		out.println("</TR>");
		return true;
	}

	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
