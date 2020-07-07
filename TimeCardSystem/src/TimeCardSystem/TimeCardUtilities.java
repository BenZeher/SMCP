package TimeCardSystem;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ConnectionPool.CompanyDataCredentials;
import ConnectionPool.ServerSettingsFileParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import TCSDataDefinition.ACGroupFunctions;
import TCSDataDefinition.ACUserGroups;
import TCSDataDefinition.Employees;

/** Utility class for Time Card Entry System.*/

public class TimeCardUtilities extends clsServletUtilities{
	public static final String DOCTYPE =
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		"Transitional//EN\">";

	public static final String sProgramVersion = "2.0";
	public static final String sLastUpdated = "7/7/2020";
	public static final String sJob = "OHD";
	public static final String sPro = "@#$";
	//Session attributes
	public static final String SESSION_ATTRIBUTE_DB = "db";
	public static final String SESSION_ATTRIBUTE_DB_SERVER = "databaseserver";
	//public static final String SESSION_ATTRIBUTE_USERID = "UserID";
	public static final String SESSION_ATTRIBUTE_PINNUMBER = "PINNUMBER";
	public static final String SESSION_ATTRIBUTE_COMPANYNAME = "COMPANYNAME";
	public static final String SESSION_ATTRIBUTE_ACCCONTROLINFO= "AccessControlInfo";
	public static final String SESSION_ATTRIBUTE_EID = "EID";
	//public static final String SESSION_ATTRIBUTE_ENAME = "ENAME";
	public static final String SESSION_ATTRIBUTE_EMPLOYEEFULNAME ="EmployeeFullName";
	public static final String SESSION_ATTRIBUTE_DEPARTMENTID ="DeptID";
	public static final String SESSION_ATTRIBUTE_DEPARTMENTDESCRIPTION ="DepartmentDescription";
	public static final String SESSION_ATTRIBUTE_ACCESSLVLDESCRIPTION ="AccessLevelDescription";
	
	public static final String REQUEST_PARAMETER_DB ="db";
	public static final String REQUEST_PARAMETER_USER_ID ="UserID";
	public static final String REQUEST_PARAMETER_PINNUMBER ="PinNumber";
	public static final String REQUEST_PARAMETER_EID ="EID";
	public static final String REQUEST_PARAMETER_EMPLOYEE_FULL_NAME ="EmployeeFullName";
	public static final String REQUEST_PARAMETER_ADMIN_MODE ="admin";
	
	public static final String sJobs = "1973!";
	public static final int MAX_SESSION_INTERVAL = 7200; // 2 hours
	public static final String TEMPORARY_POSTING_DATE = "9999-99-99";
	
	public static String EMPTY_DATE_STRING = "00/00/0000";
	public static String EMPTY_DATETIME_STRING = "00/00/0000 00:00 AM";
	
	public static final String PARAM_REPORTING_PERIOD = "REPORTINGPERIOD";
	
	public static final String BASE_FONT_FAMILY = "Arial";
	
	public static final String BACKGROUND_COLOR_FOR_ADMIN_SCREENS = SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_PURPLE;
	public static final String BACKGROUND_COLOR_FOR_USER_SCREENS = SMMasterStyleSheetDefinitions.BACKGROUND_PALE_TAN;
	
	public static int Get_Pay_Period_Length(ServletContext context){

		try {
			return Integer.parseInt(TCWebContextParameters.getInitPeriodLength(context));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public static double CalculateDoubleTime(String sStartingTime, String sEndingTime, String sEmployeeStartTime) {
		//All Sunday time is double time, regardless of the hours.   
		//All time worked on any day between the hours of 11PM and the employee's starting time is double time.

		double dDoubleTime = 0;
		java.util.Date Current;
		int iStartHour;
		int iStartMin;

		Calendar c = Calendar.getInstance();
		//long test;
		//double dCount = 0; 

		iStartHour = Integer.parseInt(sEmployeeStartTime.substring(0, 2));
		iStartMin = Integer.parseInt(sEmployeeStartTime.substring(3, 5));

		try{
			//test = System.currentTimeMillis();
			//System.out.println("Starting time = " + test);
			for (long l = (Timestamp.valueOf(sStartingTime)).getTime(); l < (Timestamp.valueOf(sEndingTime)).getTime(); l+=60000){
				//System.out.println("Time in long: " + l);
				Current = new java.util.Date(l);
				c.setTime(Current);
				//dCount++;

				if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
					//is it sunday?
					dDoubleTime++;
				}else if (c.get(Calendar.HOUR_OF_DAY) >= 23|| 
						(c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE)) < (iStartHour * 100 + iStartMin)){
					//is it off hours?
					dDoubleTime++;
				}
			}
			//System.out.println ("Total Laps = " + dCount);
			//System.out.println ("Ending time = " + System.currentTimeMillis());
			//System.out.println ("dDoubleTime = " + dDoubleTime);
			//System.out.println ("Loop elapsed: " + ((System.currentTimeMillis() - test) / 1000) + " secends.");
		}catch(Exception ex){
			System.out.println (ex.getMessage());
		}
		return dDoubleTime;
	}
	
	public static String getDatabaseName(HttpServletRequest request, HttpSession session, ServletContext context) throws Exception{
		
		//String sDatabaseServer = "";
		String sDatabaseID;
		String sDatabaseName = "";
		try {
			sDatabaseID = (String) session.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDatabaseID = "";
		}
		if (sDatabaseID == null){
			sDatabaseID = "";
		}
		if (sDatabaseID.compareToIgnoreCase("") == 0){
			sDatabaseID = clsManageRequestParameters.get_Request_Parameter(TimeCardUtilities.REQUEST_PARAMETER_DB, request);
		}
		
		ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		//If there is no control database name in the server settings file, then just use the database URL context parameter:
		if (serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME).compareToIgnoreCase("") == 0){
			//sDatabaseServer = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
		//Otherwise, go and read the database from the credentials table:
		}else{
			CompanyDataCredentials cdc = new CompanyDataCredentials();
			try {
				
				cdc.load(
					sDatabaseID, 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD)
				);
				sDatabaseName = cdc.get_databasename();
			} catch (Exception e) {
				throw new Exception("Could not read control database - " + e.getMessage());
			}
			
		}
		return sDatabaseName;
	}
	
public static String getDatabaseServer(HttpServletRequest request, HttpSession session, ServletContext context) throws Exception{
		
		String sDatabaseServer = "";
		String sDatabaseID;
		try {
			sDatabaseID = (String) session.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDatabaseID = "";
		}
		if (sDatabaseID == null){
			sDatabaseID = "";
		}
		if (sDatabaseID.compareToIgnoreCase("") == 0){
			sDatabaseID = clsManageRequestParameters.get_Request_Parameter(TimeCardUtilities.REQUEST_PARAMETER_DB, request);
		}
		
		ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		//If there is no control database name in the server settings file, then just use the database URL context parameter:
		if (serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME).compareToIgnoreCase("") == 0){
			sDatabaseServer = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
		//Otherwise, go and read the database from the credentials table:
		}else{
			CompanyDataCredentials cdc = new CompanyDataCredentials();
			try {
				
				cdc.load(
					sDatabaseID, 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME), 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD)
				);
				sDatabaseServer = cdc.get_databaseurl();
			} catch (Exception e) {
				throw new Exception("Could not read control database - " + e.getMessage());
			}
			
		}
		return sDatabaseServer;
	}

	public static String Session_Expire_Handling(ServletContext context){

		return "<BR><BR>Session expired, please login again.<BR>" + 
		"<FORM METHOD=\"POST\" ACTION=\"" + TCWebContextParameters.getURLLinkBase(context) + "TimeCardSystem.AdminMain\">" +
		"   Pass Code: <INPUT TYPE=\"PASSWORD\" NAME=\"" + TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER + "\" SIZE=6 MAXLENGTH=6><BR>" +
		"<BR><LEFT><INPUT TYPE=\"SUBMIT\" VALUE=\"---Login---\"></LEFT></FORM>";
	}  
	public static boolean IsAccessible(ResultSet rsAC, String sFunction){
		if (rsAC == null){
			return false;
		}
		try {
			rsAC.beforeFirst();
			while (rsAC.next()){
				if (rsAC.getString(ACGroupFunctions.TableName + "." + ACGroupFunctions.sFunction).compareTo(sFunction) == 0){
					//this function is accessible
					return true;
				}
			}
		} catch (Exception e) {
			//no match, not accessible
			return false;
		}
		return false;
	}

	public static String Filter_Number_String(String sOriPhoneNumber){

		String sNumberCollect = "";

		for (int i=0;i<sOriPhoneNumber.length();i++){
			if (sOriPhoneNumber.charAt(i) >= 48 && sOriPhoneNumber.charAt(i) <= 57){
				sNumberCollect = sNumberCollect + sOriPhoneNumber.substring(i, i+1);
			}
		}
		if (sNumberCollect.length() < 10){
			int iDiff = 10 - sNumberCollect.length();
			for (int i=1;i<=iDiff;i++){
				sNumberCollect = sNumberCollect + " ";
			}
		}
		return sNumberCollect;	
	}

	/******************************************************************/
	/*************Leave Balance Calculation Function Start*************/
	/******************************************************************/

	public static boolean CheckEligibility(ResultSet rsTypeInfo, ResultSet rsEmployeeInfo){

		try{
			return EvaluateEligibility(rsTypeInfo.getDate("dtEffectiveDate"), 
					rsTypeInfo.getInt("iEligibleEmployeePayType"), rsEmployeeInfo.getInt("iEmployeePayType"),
					rsTypeInfo.getInt("iEligibleEmployeeStatus"), rsEmployeeInfo.getInt("iEmployeeStatus"),
					rsTypeInfo.getDouble("dMinimumHourWorked"), rsEmployeeInfo.getDouble("dWorkHour")
			);
		}catch(SQLException exSQL){
			System.out.println("<BR><BR>[1579105655] Error in EmployeeLeaveManager.EvaluateEligibility!!");
			System.out.println("SQLException: " + exSQL.getErrorCode() + " - " + exSQL.toString());
			return false;
		}

	}

	public static boolean EvaluateEligibility(Date datEffectiveDate, 
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
			System.out.println("<BR><BR>[1579105666] Error in EmployeeLeaveManager.EvaluateEligibility!!");
			System.out.println("Exception: " + ex.getMessage());
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	public static double CalculateLeaveTotal(ResultSet rsTypeInfo, 
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
					Double dNumberOfMonths = null;
					try {
						dNumberOfMonths = rsLumpSumDetails.getDouble("dNumberOfMonths");
						if (dNumberOfMonths <= dMonths){
						dHours = rsLumpSumDetails.getDouble("dNumberOfHours");
						//record all the step-ups.
						alStepUpMonth.add(Double.valueOf(rsLumpSumDetails.getDouble("dNumberOfMonths")));
						alAccumuRates.add(Double.valueOf(rsLumpSumDetails.getDouble("dNumberOfHours")));
					}else{
						break;
					}
					}catch (Exception e){
						System.out.println("[15604469733] Error in EmployeeLeaveManager - " + e.getMessage());
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
			System.out.println("[1560447064] Error in EmployeeLeaveManager.CalculateLeaveTotal!!<BR>");
			System.out.println("[1560447064] Exception: " + ex.getMessage() + "<BR>");
			return 0;
		}
	}

	public static Timestamp FindStartDate(String sAP, Date datCutOff, Date datHiredDate){

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

	public static double Get_Leave_Length(Timestamp tsHiredDate,
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

	public static Timestamp Locate_Stepup_Time(Timestamp tsStartTime, double dMonth){
		Calendar cStepUp = Calendar.getInstance();
		cStepUp.setTimeInMillis(tsStartTime.getTime());
		cStepUp.add(Calendar.MONTH, (int)dMonth);
		//the number 30 is in favor for the employee
		cStepUp.add(Calendar.DAY_OF_MONTH, (int)((dMonth - (int)dMonth) * 30.4375));

		Timestamp ts = new Timestamp(cStepUp.getTimeInMillis());
		return ts;
	}

	public static String getDatePickerString (String sBoundFieldName, ServletContext context){

		String sImagePath = TCWebContextParameters.getInitImagePath(context);

		if (sImagePath != null){
			return "<img src=\"" + sImagePath + "calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShow(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}else{
			return "<img src=\"../images/calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShow(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}

	}
	public static String getDatePickerIncludeString (ServletContext context){
		String sScriptPath = TCWebContextParameters.getInitScriptPath(context);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + "scw002.js'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/scw002.js'></script>";
		}
	}
	public static String now(String sDateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
		return sdf.format(cal.getTime());

		/*
		 Samples:
		 System.out.println(DateUtils.now("dd MMMMM yyyy"));
		 System.out.println(DateUtils.now("yyyyMMdd"));
		 System.out.println(DateUtils.now("dd.MM.yy"));
		 System.out.println(DateUtils.now("MM/dd/yy"));
		 System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
		 System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
		 System.out.println(DateUtils.now("h:mm a"));
		 System.out.println(DateUtils.now("H:mm:ss:SSS"));
		 System.out.println(DateUtils.now("K:mm a,z"));
		 System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
		 */
	}

	public static String Create_Edit_Form_Text_Input_Row (
			String sFieldName,
			String sValue,
			int iFieldLength,
			String sLabel,
			String sRemark,
			String sTextBoxWidth,
			String sOnChange
	){

		String sRow = "<TR>";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>";

		sRow += "<TD ALIGN=LEFT>";
		sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
		if (sValue != null){
			sRow += " VALUE=\"" + sValue + "\"";
		}
		else{
			sRow += " VALUE=\"\"";
		}
		sRow += "SIZE=" + sTextBoxWidth;
		sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
		sRow += " ONCHANGE=\"" + sOnChange + "\"";
		//sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
		sRow += "></TD>";

		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		sRow += "</TR>";
		return sRow;

	}
	public static String Create_Edit_Form_MultilineText_Input_Row (
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			int iRows,
			int iCols,
			String onchange
	){

		String sRow = "<TR>";
		sRow += "<TD ALIGN=RIGHT VALIGN=TOP><B>" + sLabel  + " </B></TD>";

		sRow += "<TD ALIGN=LEFT>";
		sRow += "<TEXTAREA NAME=\"" + sFieldName + "\"";
		sRow += " rows=\"" + Integer.toString(iRows) + "\"";
		sRow += " cols=\"" + Integer.toString(iCols) + "\"";
		sRow += " onchange=\"" + onchange + "\"";
		if (sValue != null){
			sRow += ">" + sValue + "</TEXTAREA>";
		}
		else{
			sRow += "></TEXTAREA>";
		}
		sRow += "</TD>";

		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		sRow += "</TR>";
		return sRow;

	}
	public static String Create_Edit_Form_List_Row (
			String sFieldName,
			ArrayList<String> sValues,
			String sDefaultValue,
			ArrayList<String> sDescriptions,
			String sLabel,
			String sRemark,
			String sOnChange
	){

		String sRow = "<TR>";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel + " </B></TD>";
		sRow += "<TD ALIGN=LEFT>&nbsp;<SELECT NAME = \"" + sFieldName + "\""
				+ " ID = \"" + sFieldName + "\""
				+ " ONCHANGE=\"" + sOnChange + "\">";
		for (int i = 0; i < sValues.size(); i++){
			sRow += "<OPTION";
			if (sValues.get(i).toString().compareTo(sDefaultValue) == 0){
				sRow += " selected=yes";
			}
			sRow += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
		}
		sRow += "</SELECT></TD>";

		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		sRow += "</TR>";
		return sRow;
	}
	
	public static String Create_Edit_Form_Date_Input_Row (
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			ServletContext context
	){

		String sRow = "<TR>";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>";

		sRow += "<TD ALIGN=LEFT>";
		sRow += "<INPUT TYPE=TEXT NAME=\"" + sFieldName + "\"";
		//System.out.println(sLabel + " = " + sValue);
		if (sValue != null){
			sRow += " VALUE=\"" + sValue + "\"";
		}
		else{
			sRow += " VALUE=\"\"";
		}
		sRow += " SIZE=8";
		sRow += " MAXLENGTH=10";
		sRow += " STYLE=\"width: " + ".75" + " in; height: 0.25in\"";
		sRow += ">";
		sRow += TimeCardUtilities.getDatePickerString(sFieldName, context);

		sRow += "</TD>";

		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		sRow += "</TR>";
		return sRow;

	}
	public static String resultsetDateStringToString (
			String rsString
	){
		//2010-02-10 00:00:00

		//If it's a null string, return the 'zero date':
		if (rsString == null){
			return "00/00/0000";
		}

		//Try to parse off the string:
		String sYear = clsStringFunctions.StringLeft(rsString, 4);

		if (Integer.parseInt(sYear) < 1970){
			return "00/00/0000";
		}

		String sMonth = rsString.substring(5, 6).replace("0", "") + rsString.substring(6, 7);
		String sDay = rsString.substring(8, 9).replace("0", "") + rsString.substring(9, 10);

		String s = 
			sMonth
			+ "/"
			+ sDay
			+ "/"
			+ sYear
			;
		return s;
	}
	
	public static String stdDateStringToSQLDateString (String sMdyyyy){
		//Modified on 9/30/2015 by BZ to allow for M/d/yy formats as well:
		//Check to see if date is in M/d/yy format (i.e., using a 2 digit year)
		if( (sMdyyyy.substring(sMdyyyy.lastIndexOf("/"), sMdyyyy.length()).length() < 4)){
			String syear = "";
			syear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
			syear = syear.substring(0,2);
			
			return syear + clsStringFunctions.StringRight(sMdyyyy, 2) 
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(0, sMdyyyy.indexOf("/")), "0", 2)
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(
				sMdyyyy.indexOf("/") + 1, sMdyyyy.lastIndexOf("/")), "0", 2)
			;
		} 
		//If the date is NOT using a 2 digit year, then just parse it out and return:
		return clsStringFunctions.StringRight(sMdyyyy, 4) 
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(0, sMdyyyy.indexOf("/")), "0", 2)
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(
				sMdyyyy.indexOf("/") + 1, sMdyyyy.lastIndexOf("/")), "0", 2)
		;	
	}
	public static boolean isFunctionPermitted(String sEmployeeID, String sDBID, String sFunction, ServletContext context) throws Exception{

		boolean bFunctionIsPermitted = false;
		String sSQL = "SELECT * FROM " + ACGroupFunctions.TableName + ", " + ACUserGroups.TableName 
			+ " WHERE (" 
				+ "(" + ACGroupFunctions.TableName + "." + ACGroupFunctions.sGroupName  + " = " 
					+ ACUserGroups.TableName + "." + ACUserGroups.sGroupName + ")" 
				+ " AND (" + ACUserGroups.sEmployeeID + " = '" + sEmployeeID + "')"
				+ " AND (" + ACGroupFunctions.TableName + "." + ACGroupFunctions.sFunction + " = '" + sFunction + "')"
			+ ") ORDER BY " + ACGroupFunctions.TableName + "." + ACGroupFunctions.sFunction;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
			if (rs.next()){
				bFunctionIsPermitted = true;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1486945030] reading function permission with SQL: '" + sSQL + "' - " + e.getMessage());
		}
		
		return bFunctionIsPermitted;
	}
	
	public static String getJQueryIncludeString (){
		return "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>";
	}
	
	public static String getFullNamebyEmployeeID(String sUserID, ServletContext context, String sDBID, String sCallingFunction) {
		String sFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + Employees.sEmployeeFirstName
			+ ", " + Employees.sEmployeeLastName
			+ " FROM " + Employees.TableName
			+ " WHERE ("
				+ "(" + Employees.sEmployeeID + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID, 
					"MySQL",
					sCallingFunction
					);
			if (rs.first()){
				sFullName = rs.getString(Employees.sEmployeeFirstName).trim() + " " + rs.getString(Employees.sEmployeeLastName).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//Don't do anything, just let the function return the 'not found' string...
		}	
		return sFullName;	
	}

	public static String TCTitleSubBGColor(String title, String subtitle, String backgroundcolor) {
	
		String s = clsServletUtilities.DOCTYPE + "\n"
		+ "<HTML>" + "\n" 
		+ "  <HEAD>" + "\n"
		+ "    <TITLE>" + title + "</TITLE>" + "\n"
		+ "  </HEAD><BR>" + "\n" 
		+ "  <BODY BGCOLOR=\"" + backgroundcolor + "\" style = \" font-family:" + BASE_FONT_FAMILY + "; \"  >" + "\n"
		+ "    <TABLE BORDER=0>" + "\n"
		+ "      <TR>" + "\n"
		+ "        <TD VALIGN=BOTTOM><H1>" + title + "</H1></TD>" + "\n";
		if (subtitle.compareTo("") != 0){  
			s += "        <TD VALIGN=BOTTOM><H3>&nbsp;&nbsp;-&nbsp;&nbsp;" + subtitle + "</H3></TD>" + "\n";
		}
	
		s += "      </TR>" + "\n"
			+ "    </TABLE>" + "\n";
		return s;
	}

	public static String TCTitleSubBGColorWithOnLoadCommand(String title, String subtitle, String backgroundcolor, String sOnLoadCommand) {
	
		String s = clsServletUtilities.DOCTYPE + "\n"
		+ "<HTML>" + "\n" 
		+ "  <HEAD>" + "\n"
		+ "    <TITLE>" + "\n"
			+ title + "\n"
			+ "      </DIV>" + "\n"
		+ "</TITLE>" + "\n"
		+ "  </HEAD><BR>" + "\n" 
		+ "  <BODY BGCOLOR=\"" + backgroundcolor + "\" style = \" font-family:" + BASE_FONT_FAMILY + "; \""
			+ " " + sOnLoadCommand + " " 
		+ ">" + "\n"
		+ "    <TABLE BORDER=0>" + "\n"
		+ "      <TR>" + "\n"
		+ "        <TD VALIGN=BOTTOM>" + "\n"
			+ "        <DIV style = \" font-family: arial; font-size: xx-large; font-weight: bold; \" >"
			+ title
			+ "        </DIV>" + "\n"
		+ "        </TD>" + "\n";
		if (subtitle.compareTo("") != 0){  
			s += "        <TD VALIGN=BOTTOM><H3>&nbsp;&nbsp;-&nbsp;&nbsp;" + subtitle + "</H3></TD>" + "\n";
		}
	
		s += "      </TR>" + "\n"
			+ "    </TABLE>" + "\n";
		return s;
	}

	public static String TCTitleSubBGColorWithFont(String title, String subtitle, String backgroundcolor, String sFontFamily) {
	
		String s = clsServletUtilities.DOCTYPE +
		"<HTML>" +
		"<HEAD><TITLE>" + title + "</TITLE></HEAD>\n<BR>" + 
		"<BODY BGCOLOR=\"" + backgroundcolor + "\" style = \" font-family: " + sFontFamily + "; \" >" +
		"<TABLE BORDER=0><TR><TD VALIGN=BOTTOM><H1>" + title + "</H1></TD>";
		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H3>&nbsp;&nbsp;-&nbsp;&nbsp;" + subtitle + "</H3></TD>";
		}
	
		s = s + "</TR></TABLE>";
		return s;
	}

	public static String TCBarTitleSubBGColor(String bar, String title, String subtitle, String backgroundcolor) {
	
		String s = clsServletUtilities.DOCTYPE +
		"<HTML>" +
		"<HEAD><TITLE>" + bar + "</TITLE></HEAD>\n<BR>" + 
		"<BODY BGCOLOR=\"" + backgroundcolor + "\" style = \" font-family:" + BASE_FONT_FAMILY + "; \" >" +
		"<TABLE BORDER=0><TR><TD VALIGN=BOTTOM><H1>" + title + "</H1></TD>";
		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H3>&nbsp;&nbsp;-&nbsp;&nbsp;" + subtitle + "</H3></TD>";
		}
	
		s = s + "</TR></TABLE>";
		return s;
	}
}