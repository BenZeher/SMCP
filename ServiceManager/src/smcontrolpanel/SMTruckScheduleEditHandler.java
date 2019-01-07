package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMClasses.SMWorkOrderHeader;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMTruckScheduleEditHandler extends HttpServlet{

	public static final int EDITENTRY_MOVELEFT = 0;
	public static final int EDITENTRY_COPYLEFT = 1;
	public static final int EDITENTRY_MOVERIGHT = 2;
	public static final int EDITENTRY_COPYRIGHT = 3;
	public static final int EDITENTRY_MOVEUP = 4;
	public static final int EDITENTRY_MOVEDOWN = 5;
	public static final String EDITENTRYMODE = "EditEntryMode";
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserFullName = "";
	private String sUserID= "";
	private String sWarning;
	//private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		sWarning = "";
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMConfigureWorkOrders)
		){
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sTruckScheduleQueryString = 
			(String) CurrentSession.getAttribute(SMViewTruckScheduleReport.TRUCKSCHEDULEQUERYSTRING);
		if (sTruckScheduleQueryString == null){
			sTruckScheduleQueryString = "";
		}
		CurrentSession.removeAttribute(SMViewTruckScheduleReport.TRUCKSCHEDULEQUERYSTRING);
		
		//We need to read the work order, make the requested change, then call the Action class to
		//make the edit:
		
		//Get the work order entry:
		SMWorkOrderHeader entry = new SMWorkOrderHeader();
		entry.setlid(clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramlid, request));
		try {
			entry.load(sDBID, sUserFullName, getServletContext());
		} catch (Exception e) {
			sWarning = "Could not load work order with ID '" + entry.getlid() + "'.";
		}
		int iEditMode = -1;
		try {
			iEditMode = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(EDITENTRYMODE, request));
		} catch (NumberFormatException e) {
			sWarning = "Could not parse " + EDITENTRYMODE + " parameter, value = ''" 
				+ clsManageRequestParameters.get_Request_Parameter(EDITENTRYMODE, request) + "'";
			return;
		}
		Calendar calEntryDate = Calendar.getInstance();
		BigDecimal bdTotalHours = new BigDecimal(0.00);
		SMWorkOrderHeader tempwo = new SMWorkOrderHeader();
		String sResultString = "";
		String sOriginalDate = entry.getsscheduleddate();
		switch (iEditMode){
			case EDITENTRY_COPYLEFT:
				//Trigger the program to create a NEW entry on the previous day, at the bottom of the job sequence:
				//First, copy all the values we want to keep to a temporary work order:
				tempwo.setmechanicsinitials(entry.getmechanicsinitials());
				tempwo.setmechanicsname(entry.getmechanicsname());
				tempwo.setmechid(entry.getmechid());
				tempwo.setsassistant(entry.getsassistant());
				tempwo.setsstartingtime(entry.getsstartingtime());
				tempwo.setstrimmedordernumber(entry.getstrimmedordernumber());

				try {
					calEntryDate.setTime(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", entry.getsscheduleddate()));
				} catch (ParseException e) {
					sWarning = "Error:[1423662232] Invalid Entry Date: '" + entry.getsscheduleddate() + "' - " + e.getMessage();
					return;
				}
				calEntryDate.add(Calendar.DATE, -1);
				//Now that we've saved the values we need, initialize the original entry and copy the saved fields back into it:
				//This clears all the work order lines, and insures that it's really a 'new' work order:
				entry = new SMWorkOrderHeader();
				entry.setmechanicsinitials(tempwo.getmechanicsinitials());
				entry.setmechanicsname(tempwo.getmechanicsname());
				entry.setmechid(tempwo.getmechid());
				entry.setsassistant(tempwo.getsassistant());
				entry.setsstartingtime(tempwo.getsstartingtime());
				entry.setstrimmedordernumber(tempwo.getstrimmedordernumber());
				
				//Update these fields:
				entry.setsscheduleddate(clsDateAndTimeConversions.CalendarToString(calEntryDate, "M/d/yyyy"));
				entry.setsjoborder("99");
				entry.setdattimearrivedatcurrent("00/00/0000 00:00 AM");
				entry.setdattimearrivedatnext("00/00/0000 00:00 AM");
				entry.setdattimeleftcurrent("00/00/0000 00:00 AM");
				entry.setdattimeleftprevious("00/00/0000 00:00 AM");
				sResultString = "Successfully COPIED work order for order '" 
					+ entry.getstrimmedordernumber() 
					+ "' - mechanic " + entry.getmechanicsname()
					+ " from " + sOriginalDate.replace("/", "-") + " to " + entry.getsscheduleddate().replace("/", "-") + "."
				;
				break;
			case EDITENTRY_COPYRIGHT:
				//Trigger the program to create a NEW entry on the previous day, at the bottom of the job sequence:
				//First, copy all the values we want to keep to a temporary work order:
				tempwo.setmechanicsinitials(entry.getmechanicsinitials());
				tempwo.setmechanicsname(entry.getmechanicsname());
				tempwo.setmechid(entry.getmechid());
				tempwo.setsassistant(entry.getsassistant());
				tempwo.setsstartingtime(entry.getsstartingtime());
				tempwo.setstrimmedordernumber(entry.getstrimmedordernumber());

				try {
					calEntryDate.setTime(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", entry.getsscheduleddate()));
				} catch (ParseException e) {
					sWarning = "Error:[1423662232] Invalid Entry Date: '" + entry.getsscheduleddate() + "' - " + e.getMessage();
					return;
				}
				calEntryDate.add(Calendar.DATE, 1);
				//Now that we've saved the values we need, initialize the original entry and copy the saved fields back into it:
				//This clears all the work order lines, and insures that it's really a 'new' work order:
				entry = new SMWorkOrderHeader();
				entry.setmechanicsinitials(tempwo.getmechanicsinitials());
				entry.setmechanicsname(tempwo.getmechanicsname());
				entry.setmechid(tempwo.getmechid());
				entry.setsassistant(tempwo.getsassistant());
				entry.setsstartingtime(tempwo.getsstartingtime());
				entry.setstrimmedordernumber(tempwo.getstrimmedordernumber());
				
				//Update these fields:
				entry.setsscheduleddate(clsDateAndTimeConversions.CalendarToString(calEntryDate, "M/d/yyyy"));
				entry.setsjoborder("99");
				entry.setdattimearrivedatcurrent("00/00/0000 00:00 AM");
				entry.setdattimearrivedatnext("00/00/0000 00:00 AM");
				entry.setdattimeleftcurrent("00/00/0000 00:00 AM");
				entry.setdattimeleftprevious("00/00/0000 00:00 AM");
				sResultString = "Successfully COPIED work order for order '" 
						+ entry.getstrimmedordernumber() 
						+ "' - mechanic " + entry.getmechanicsname()
						+ " from " + sOriginalDate.replace("/", "-") + " to " + entry.getsscheduleddate().replace("/", "-") + "."
					;
				break;
			case EDITENTRY_MOVEDOWN:
				entry.setsjoborder(Integer.toString(Integer.parseInt(entry.getsjoborder()) + 1) );
				sResultString = "Successfully MOVED work order for order '" 
						+ entry.getstrimmedordernumber() 
						+ "' - mechanic " + entry.getmechanicsname()
						+ " DOWN on " + entry.getsscheduleddate().replace("/", "-") + "."
					;
				break;
			case EDITENTRY_MOVELEFT:
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getsbackchargehours()));
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getsqtyofhours()));
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getstravelhours()));
				if (bdTotalHours.compareTo(BigDecimal.ZERO) != 0){
					sWarning = "You cannot move an entry that already has hours entered in it.";
				}else{
					try {
						calEntryDate.setTime(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", entry.getsscheduleddate()));
					} catch (ParseException e) {
						sWarning = "Error:[1423662234] Invalid Entry Date: '" + entry.getsscheduleddate() + "' - " + e.getMessage();
						return;
					}
					calEntryDate.add(Calendar.DATE, -1);
					entry.setsscheduleddate(clsDateAndTimeConversions.CalendarToString(calEntryDate, "M/d/yyyy"));
					entry.setsjoborder("99");
				}
				sResultString = "Successfully MOVED work order for order '" 
						+ entry.getstrimmedordernumber() 
						+ "' - mechanic " + entry.getmechanicsname()
						+ " from " + sOriginalDate.replace("/", "-") + " to " + entry.getsscheduleddate().replace("/", "-") + "."
					;
				break;
			case EDITENTRY_MOVERIGHT:
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getsbackchargehours()));
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getsqtyofhours()));
				bdTotalHours = bdTotalHours.add(new BigDecimal(entry.getstravelhours()));
				if (bdTotalHours.compareTo(BigDecimal.ZERO) != 0){
					sWarning = "You cannot move an entry that already has hours entered in it.";
				}else{
					try {
						calEntryDate.setTime(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", entry.getsscheduleddate()));
					} catch (ParseException e) {
						sWarning = "Error:[1423662235] Invalid Entry Date: '" + entry.getsscheduleddate() + "' - " + e.getMessage();
						return;
					}
					calEntryDate.add(Calendar.DATE, 1);
					entry.setsscheduleddate(clsDateAndTimeConversions.CalendarToString(calEntryDate, "M/d/yyyy"));
					entry.setsjoborder("99");
				}
				sResultString = "Successfully MOVED work order for order '" 
						+ entry.getstrimmedordernumber() 
						+ "' - mechanic " + entry.getmechanicsname()
						+ " from " + sOriginalDate.replace("/", "-") + " to " + entry.getsscheduleddate().replace("/", "-") + "."
					;
				break;
			case EDITENTRY_MOVEUP:
				int iJobOrder = Integer.parseInt(entry.getsjoborder());
				if (iJobOrder > 1){
					iJobOrder = iJobOrder - 1;
					sResultString = "Successfully MOVED work order for order '" 
							+ entry.getstrimmedordernumber() 
							+ "' - mechanic " + entry.getmechanicsname()
							+ " UP on " + entry.getsscheduleddate().replace("/", "-") + "."
						;
				}else{
					iJobOrder = 1;
					sResultString = "Work order for order '" 
							+ entry.getstrimmedordernumber() 
							+ "' - mechanic " + entry.getmechanicsname()
							+ " was ALREADY the first entry on " + entry.getsscheduleddate().replace("/", "-") 
							+ ", so it was NOT moved."
						;
				}
				entry.setsjoborder(Integer.toString(iJobOrder) );

				break;
			default:
		}
		try {
			entry.saveFromConfigure(getServletContext(), sDBID, sUserID, sUserFullName, SMWorkOrderHeader.SAVING_FROM_SCHEDULE_HANDLER);
		} catch (Exception e1) {
			sWarning = "Error [1427144736] Could not save entry - " + e1.getMessage();
		}
		
		sWarning = sWarning.trim();
		//If there was no error, record that the schedule entry was moved:
		if (sWarning.compareToIgnoreCase("") == 0){
			SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_MOVEDSCHEDULEENTRYFROMSCHEDULESCREEN
				, sResultString, 
				"Work order #" + entry.getlid(), 
				"[1428498914]");
		}
		
		String sOriginalTruckScheduleURL = "";
		//Then IF there is a truck schedule query string in the session:
		if (sTruckScheduleQueryString.compareToIgnoreCase("") != 0){
			//We save that as the 'original URL' so we can go back to it:
			sOriginalTruckScheduleURL = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMViewTruckScheduleGenerate?"
			+ sTruckScheduleQueryString
			;
			
			if (sWarning.compareToIgnoreCase("") == 0){
				sOriginalTruckScheduleURL += "&" + SMWorkOrderHeader.LASTENTRYEDITED_PARAM + "=" + entry.getlid() + "&STATUS=" + sResultString;
			}else{
				sOriginalTruckScheduleURL += "&" + "Warning=" + clsServletUtilities.URLEncode(sWarning);
			}
			
			response.sendRedirect(sOriginalTruckScheduleURL);
		}else{
			out.println("Error getting truck schedule query string.");
		}

		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}