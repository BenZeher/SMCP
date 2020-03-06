package smcontrolpanel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.MySQLs;
import SMClasses.SMAppointment;
import SMDataDefinition.SMTableappointments;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;


public class SMAppointmentNotificationHandler  extends clsMasterEntry{

	private static final boolean bDebugMode = false;
	
	public String processAppointmentReminders(
		ServletContext context,
		String sDatabaseID
		) throws Exception{
		String s = "";
		Connection conn = null;


		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDatabaseID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".processSchedules"));
		} catch (Exception e1) {
			//If we can't get a connection here, just exit quietly - it's probably not a legitimate database:
			System.out.println("[1505420436] - can't connect to company database ID '" 
					+ sDatabaseID + "' which is listed in company data credentials - it probably doesn't exist.");
				return s;
		}
		
		//Get a of all appointments for today
		String SQL = "SELECT "
			+ SMTableappointments.lid
			+ ", " + SMTableappointments.iminuteofday
			+ ", " + SMTableappointments.inotificationtime
			+ ", " + SMTableappointments.inotificationsent
			+ " FROM " + SMTableappointments.TableName
			+ " WHERE ("
			+ "(UNIX_TIMESTAMP(" + SMTableappointments.datentrydate + ")/60 +" + SMTableappointments.iminuteofday + ") -" + SMTableappointments.inotificationtime 
			+ "< UNIX_TIMESTAMP(NOW())/60"
			+ " AND "
			+ "(CURDATE() <=" + SMTableappointments.datentrydate + " )"
			+ " AND "
			+ "(" + SMTableappointments.inotificationsent + "= 0)"
			+ " AND "
			+ "(" + SMTableappointments.inotificationtime + "!= -1)"
			+ ")"
			+ "ORDER BY " + SMTableappointments.datentrydate + ", " + SMTableappointments.iminuteofday
		;
		
		if(bDebugMode){
			System.out.println("[1505420492] - " + SQL);
		}
		
		try {
			ResultSet rs = null;
			try {
				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			} catch (SQLException e) {
				//If it's just because the database doesn't exist, don't choke over it:
				if (e.getMessage().contains("doesn't exist")){
					//System.out.println("Error [1578602673] - company database ID '" + sDatabaseID + "'"
					//	+ ", which is listed in " + CompanyDataCredentials.TableName 
					//	+ ", is missing table '" + SMTableappointments.TableName + "'."
					//);
					clsDatabaseFunctions.freeConnection(context, conn, "[1547080402]");
					return s;
				}
			}
			catch (Exception e) {
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080403]");
				throw new Exception("Error [1505420942] - " + e.getMessage());
			}		

			if (rs == null){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080404]");
				return s;
			}

			while (rs.next()){
					Thread thread = new Thread(new emailAppointmentNotification(conn, Integer.toString(rs.getInt(SMTableappointments.lid))));
					thread.start();
			}
			rs.close();
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080405]");
			throw new Exception("Error [1505421053] reading appointment records using database ID '" + sDatabaseID + "' - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080400]");
		return s;
	}
	
	
	public String sendAppointmentEmailNotification(Connection conn, String appointmentID) throws Exception{
		String s = "";
		//load the appointment
		SMAppointment appointment = new SMAppointment(); 
		appointment.setslid(appointmentID);
		appointment.load(conn);

		//Get user information:
		String sUsersFullName = "";
		String sUsersEmail = "";
		
		String SQL = "";
		SQL = MySQLs.Get_User_By_UserID(appointment.getluserid());
		try {
			ResultSet rsUserInfo = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsUserInfo.next()){
				sUsersFullName = rsUserInfo.getString(SMTableusers.sUserFirstName) + " " + rsUserInfo.getString(SMTableusers.sUserLastName);
				sUsersEmail = rsUserInfo.getString(SMTableusers.semail);
			}
			rsUserInfo.close();
		} catch (Exception e1) {
			throw new Exception("Error [1505758950] getting user information for appointment notificaiton.");
		}
		
		//Now go get the info we need to send an email:
		ArrayList<String> arrNotifyEmails = new ArrayList<String>(0);
		arrNotifyEmails.add(sUsersEmail);
		//String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sUserName = ""; 
		String sPassword = ""; 
		String sReplyToAddress = "";
		boolean bUsesSMTPAuthentication = false;
	
		try{
			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
				+ " FROM " 
				+ SMTablesmoptions.TableName;
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOptions.next()){
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim(); 
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				rsOptions.close();
			}else{
				rsOptions.close();
				throw new Exception("Error [1505758951] getting smoptions record to get email information.");
			}
		}catch(SQLException e){
			throw new Exception("Error [1505758952] getting email information from smoptions with SQL: " + SQL + "  - " + e.getMessage());
		}
		//Create unique subject line for each appointment email
		String sTitle = "Appointment: " + appointment.getsshiptoname() + " at " + appointment.getiminuteofday() + " " + appointment.getdatentrydate()
		;
		//TODO Edit body with links.
		String sBody = "This is an appointment reminder for " + sUsersFullName
			+ "\n\n Date: " + appointment.getdatentrydate()
			+ "\n Time: " + appointment.getiminuteofday()
			+ "\n Comment: " + appointment.getmcomment()
			+ "\n\n";
			
		if(appointment.getibidid().compareToIgnoreCase("0") != 0){
			sBody += "Generated by sales lead: " + appointment.getibidid();
		}
		if(appointment.getisalescontactid().compareToIgnoreCase("0") != 0){
			sBody += "Generated by sales contact ID: " + appointment.getibidid();
		}
		if(appointment.getsordernumber().compareToIgnoreCase("") != 0){
			sBody += "Generated by order: " + appointment.getibidid();
		}
			sBody += "\n Contact Name: " + appointment.getscontactname()
			+ "\n Bill to Name: " + appointment.getsbilltoname();
			
			sBody += "\n Ship To Name: " + appointment.getsshiptoname();
			//TODO Create address as map link
			String sMapAddress = appointment.getsaddress1().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getsaddress2().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getsaddress3().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getsaddress4().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getscity().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getsstate().trim();
			sMapAddress	= sMapAddress.trim() + " " + appointment.getszip().trim();
			
			//if(appointment.getsgeocode().compareToIgnoreCase("") != 0){
			//	sBody += "\n Ship To Address: <A href=\"https://www.google.com/maps/search/?api=1&query=" + appointment.getsgeocode() + "\">" + sMapAddress + "</A>";
			//}else{
				sBody += "\n Ship To Address:" + sMapAddress;
			//}
		if(appointment.getsphone().compareToIgnoreCase("") != 0){
			sBody += "\n Phone: " + appointment.getsphone();
		}
		if(appointment.getsphone().compareToIgnoreCase("") != 0){
			sBody += "\n Email: " + appointment.getsemail();
		}			
			sBody += "\n\n\n" + "appointment created by " + SMUtilities.getFullNamebyUserID(appointment.getlcreateduserid(), conn) + " on " + clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(appointment.getdatcreatedtime())
		;

		int iSMTPPort;
		try {
			iSMTPPort = Integer.parseInt(sSMTPPort);
		} catch (NumberFormatException e) {
			throw new Exception("Error parsing email port '" + sSMTPPort + "' [1505758953] - " + e.getMessage());
		}
		try {

			SMUtilities.sendEmail(
					sSMTPServer, 
					sUserName, 
					sPassword,
					sReplyToAddress,
					Integer.toString(iSMTPPort),
					sTitle,
					sBody,
					"SMCP@" + sSMTPSourceServerName,
					sSMTPSourceServerName, 
					arrNotifyEmails, 
					bUsesSMTPAuthentication,
					true
			);
		} catch (Exception e) {
			throw new Exception("Error sending email [1505758954] " + e.getMessage());
		}		
		//Update the notification flag 
		appointment.updateNotificationSentFlag(conn, "1");
		return s;
	}
	
	public class emailAppointmentNotification implements Runnable {
		Connection conn = null;
		String sAppointmentID = "";
		
		public emailAppointmentNotification(Connection connection, String sAppointment) {
			conn = connection;
			sAppointmentID = sAppointment;
		}
		@Override
		public void run() {
			try {
				sendAppointmentEmailNotification(conn, sAppointmentID);
			} catch (Exception e) {
				System.out.println("[1546268264] - Email for appointment ID " + sAppointmentID 
						+ " failed to send in emailAppointmentNotification thread - "
						+ e.getMessage());
				return;
			}
		}
	}
	
	
}
