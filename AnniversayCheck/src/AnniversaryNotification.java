import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AnniversaryNotification
{
	
	@SuppressWarnings("deprecation")
	public static void main(String[] paramArrayOfString)
	{
		//Parameters:
		// 0) database URL (server name) - e.g. "192.168.0.7"
		// 1) database name - e.g. "TimeCardSystem1"
		// 2) database user name - e.g. "smuser"
		// 3) database user password - e.g. "XXXXXXXX"
		// 4) SMTP server - e.g. "smtp.gmail.com"
		// 5) SMTP port = e.g. "465"
		// 5) SMTP user - e.g. "printmanager@odcdc.com"
		// 6) SMTP user password - e.g. "XXXXXXXX"
		String sDatabaseURL;
		String sDatabaseName;
		String sDatabaseUserName;
		String sDatabaseUserPassword;
		String sSMTPServerName;
		String sSMTPPort;
		String sSMTPUserName;
		String sSMTPUserPassword;
		final String DATETIME_FORMAT_STD = "MM/dd/yyyy hh:mm a";
		try {
			sDatabaseURL = paramArrayOfString[0];
			sDatabaseName = paramArrayOfString[1];
			sDatabaseUserName = paramArrayOfString[2];
			sDatabaseUserPassword = paramArrayOfString[3];
			sSMTPServerName = paramArrayOfString[4];
			sSMTPPort = paramArrayOfString[5];
			sSMTPUserName = paramArrayOfString[6];
			sSMTPUserPassword = paramArrayOfString[7];
		} catch (Exception e3) {
			System.out.println("[1579014007] Error " + e3.getMessage());
			String sCommandLine = "";
			for (int i = 0; i < paramArrayOfString.length; i++){
				sCommandLine += paramArrayOfString[i] + " ";
			}
			System.out.println("[1579014016] Command line: " + sCommandLine);
			return;
		}

		//Validate the strings:
		if (sSMTPUserPassword.compareToIgnoreCase("") == 0){
			System.out.println("[1579014032] Command line syntax should look like this:\n\n"
					+ "java AnniversaryNotification.class "
					+ "databaseURL "
					+ "databasename "
					+ "databaseusername "
					+ "databaseuserpassword "
					+ "smtpservername "
					+ "smtpport"
					+ "smtpusername "
					+ "smtpuserpassword"
					);
			return;
		}
		String sCommandLine = 
				"Database URL = '" + sDatabaseURL + "'\n"
						+ " Database name = '" + sDatabaseName + "'\n"
						+ " Database user name = '" + sDatabaseUserName + "'\n"
						+ " Database user pw = '" + sDatabaseUserPassword + "'\n"
						+ " SMTP server name = '" + sSMTPServerName + "'\n"
						+ " SMTP port = '" + sSMTPPort + "'\n"
						+ " SMTP user name = '" + sSMTPUserName + "'\n"
						+ " SMTP user pw = '" + sSMTPUserPassword + "'\n"
						;
		Connection conn = null;
		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sDatabaseUserName, sDatabaseUserPassword);
		}
		catch (Exception localException2) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sDatabaseUserName, sDatabaseUserPassword);
			} catch (InstantiationException e) {
				System.out.println("[1579014055] Instantiation Exception Error reading database - " + e.getMessage() + " - command line: " + sCommandLine);
			} catch (IllegalAccessException e) {
				System.out.println("[1579014111] Illegal Access Error reading database - " + e.getMessage() + " - command line: " + sCommandLine);
			} catch (ClassNotFoundException e) {
				System.out.println("[1579014117] ClassNotFound Error reading database - " + e.getMessage() + " - command line: " + sCommandLine);
			} catch (SQLException e) {
				System.out.println("[1579014122] SQLException Error reading database - " + e.getMessage() + " - command line: " + sCommandLine);
			}
		}
		String sSQL = "SELECT * FROM companyprofile";
		String sCompany;
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sSQL);
			if (!rs.next()){
				rs.close();
				conn.close();
				System.out.println("[1579014142] No company name record using sSQL: " + sSQL);
				return;
			}
			sCompany = rs.getString("scompanyname");
			rs.close();
		} catch (Exception e1) {
			System.out.println("[1579014130] Error reading company name using sSQL: '" + sSQL + "'" + e1.getMessage());
			return;
		}

		SimpleDateFormat sdfNormalDate = new SimpleDateFormat("yyyy-MM-dd EEE");
		int iBirthdayCount = 0;
		int iAnniversaryCount = 0;
		Calendar calCalculateAsOfDate = Calendar.getInstance();
		calCalculateAsOfDate.setTimeInMillis(System.currentTimeMillis());
		//Move our 'calculate as of' date back one day:
		calCalculateAsOfDate.add(Calendar.DATE, -1);
		Calendar calThisDayNextMonth = Calendar.getInstance();
		calThisDayNextMonth.setTimeInMillis(System.currentTimeMillis());
		calThisDayNextMonth.add(Calendar.MONTH, 1);
		Calendar calBirthday = Calendar.getInstance();
		Calendar calHiredDate = Calendar.getInstance();
		ArrayList<String> alBirthdayList = new ArrayList<String>(0); 
		ArrayList<String> alAnniversaryList = new ArrayList<String>(0); 

		//Prepare the header message:
		java.sql.Date datCalculatedAsOfDate;
		try {
			datCalculatedAsOfDate = new java.sql.Date(calCalculateAsOfDate.getTimeInMillis());
		} catch (Exception e) {
			System.out.println("[1585069280] "+e.getMessage());
			return;
		}
		
		String sEmail = "Birthday and Anniversary dates within the next month, calculating from " + sdfNormalDate.format(datCalculatedAsOfDate) + ":\n\n\"";
		sEmail += "UPCOMING BIRTHDAY(S) - Birthday, Name, Age:\n\n"; //str8
		String sTemp = "";
		sSQL = "SELECT * FROM Employees WHERE iActive=1 AND datBirthday <> '0000-00-00' ORDER BY DAYOFYEAR(datBirthday)";
		Statement statement;
		ResultSet rs;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(sSQL);
			while (rs.next()) {
				calBirthday.setTimeInMillis(rs.getDate("datBirthday").getTime());
				java.sql.Date datNextBirthday;
				try {
					datNextBirthday = FindNextAnniversary(calCalculateAsOfDate, 
							calThisDayNextMonth, 
							calBirthday);
				} catch (Exception e) {
					System.out.println("[1579014164] "+e.getMessage());
					return;
				}
				if (datNextBirthday != null) {
					sTemp = sdfNormalDate.format(datNextBirthday) + "\t\t";
					sTemp += rs.getString("sEmployeeFirstName").trim();
					if (rs.getString("sEmployeeMiddleName").trim().compareTo("") != 0) {
						sTemp +=  " " + rs.getString("sEmployeeMiddleName").trim();
					}
					sTemp += " " + rs.getString("sEmployeeLastName").trim();
					sTemp +=  " (" + (datNextBirthday.getYear() - calBirthday.get(Calendar.YEAR) + 1900) + ")\r";
					alBirthdayList.add(sTemp);
					iBirthdayCount ++;
				}
			}
		} catch (SQLException e2) {
			System.out.println("[1579014179] Error reading birthday data: " + e2.getMessage());
			return;
		}
		//sort now
		Collections.sort(alBirthdayList);
		//printout all birthdays
		for (int i=0;i<alBirthdayList.size();i++){
			sEmail += alBirthdayList.get(i);
		}
		if (iBirthdayCount == 0) {
			sEmail += "***No birthdays coming up.***";
		}
		sSQL = "SELECT * FROM Employees WHERE iActive=1 AND datBirthday <> '0000-00-00' ORDER BY DAYOFYEAR(datHiredDate)";
		try {
			rs = statement.executeQuery(sSQL);
			sEmail += "\n\nUPCOMING ANNIVERSARY(S) - Anniversary date, Name, Years with the company:\n\n";
			while (rs.next()) {
				calHiredDate.setTimeInMillis(rs.getDate("datHiredDate").getTime());
				java.sql.Date datNextAnniversary;
				try {
					datNextAnniversary = FindNextAnniversary(calCalculateAsOfDate, 
							calThisDayNextMonth, 
							calHiredDate);
				} catch (Exception e) {
					System.out.println("[1579014211] " + e.getMessage());
					return;
				}
				if (datNextAnniversary != null) {
					//this means this anniversary falls into next month and we want to display this anniversary.
					sTemp = sdfNormalDate.format(datNextAnniversary) + "\t\t";
					sTemp += rs.getString("sEmployeeFirstName").trim();
					if (rs.getString("sEmployeeMiddleName").trim().compareTo("") != 0) {
						sTemp +=  " " + rs.getString("sEmployeeMiddleName").trim();
					}
					sTemp += " " + rs.getString("sEmployeeLastName").trim();
					sTemp +=  " (" + (datNextAnniversary.getYear() - calHiredDate.get(Calendar.YEAR) + 1900) + ")\r";
					alAnniversaryList.add(sTemp);
					iAnniversaryCount ++;
				}
			}
		} catch (SQLException e2) {
			System.out.println("[1579014217] Error reading anniversary data: " + e2.getMessage());
			return;
		}
		//sort now
		Collections.sort(alAnniversaryList);
		//printout all birthdays
		for (int i=0;i<alAnniversaryList.size();i++){
			sEmail += alAnniversaryList.get(i);
		}
		if (iAnniversaryCount == 0) {
			sEmail += "***No anniversaries coming up.***";
		}
		ArrayList<String> alEmails = new ArrayList<String>(0);
		sSQL = "SELECT *"
			+ " FROM Employees, ACUserGroups, ACGroupFunctions"
			+ " WHERE ("
				+ "(Employees.sEmployeeID = ACUserGroups.sEmployeeID)"
				+ " AND (ACUserGroups.sGroupName = ACGroupFunctions.sGroupName)"
				+ " AND (ACGroupFunctions.sFunction = 'AnniversaryNotificationRecipient')"
			+ ")"
			;
		try {
			rs = statement.executeQuery(sSQL);
			while (rs.next()) {
				if (rs.getString("sEmail").trim().compareTo("") == 0)
					continue;
				alEmails.add(rs.getString("sEmail"));
			}
			rs.close();
			statement.close();
			conn.close();
		} catch (SQLException e1) {
			System.out.println("[1579014222] Error reading email data: " + e1.getMessage());
			return;
		}
		String sRecipients = "";
		for (int i = 0; i < alEmails.size(); i++){
			sRecipients += alEmails.get(i) + ",";
		}
		boolean bEmailSucceeded = true;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_STD);
		System.out.println(" *********************");
		try{
			sendSSLMail(
					sSMTPServerName, 
					sSMTPUserName, 
					sSMTPUserPassword, 
					sSMTPPort, 
					"Weekly Anniversary Check for " + sCompany, 
					sEmail, 
					"auto-generated", 
					sRecipients,
					false);
		}catch (Exception e){
			bEmailSucceeded = false;
			System.out.println(sdf.format(cal.getTime()) + " email FAILED - " + e.getMessage());
			System.out.println(" ********************* \n [1579015393]");
		}
		if (bEmailSucceeded){
			System.out.println(sdf.format(cal.getTime()) + " email sent successfully to recipients:");
			for (int i = 0; i < alEmails.size(); i++){
				System.out.println(" - " + alEmails.get(i));
			}
			System.out.println(" ********************* \n[1579014260]");
		}
		return;
	}

	private static java.sql.Date FindNextAnniversary(
			Calendar calCalculateAsOfDate, 		//paramCalendar1, 
			Calendar calNextMonth, //paramCalendar2, 
			Calendar calAnniversaryDay	//paramCalendar3
			) throws Exception
			{
 
		//calCalculateAsOfDate is the date we use base our calculation on, usually today or within a day of it.
		//calNextMonth is one month from now
		//calAnniversaryDay is the birthday or anniversary date we are checking
		//cTempDate is used to temporarily carry the anniversary date we are checking
		
		Calendar cTempDate = Calendar.getInstance();
		
		//Set cCurrentDate to the employee's anniversary date:
		cTempDate.setTimeInMillis(calAnniversaryDay.getTimeInMillis());
		//If the anniversary date MONTH is later than the 'As Of' month:
		if (cTempDate.get(Calendar.MONTH) > calCalculateAsOfDate.get(Calendar.MONTH))
		{
			//If the month of the anniversary is LATER than the 'as of' month, then the next anniversary is this year:
			//So we add a number of years to the original anniversary date to bring it up to this year:
			cTempDate.add(Calendar.YEAR, (calCalculateAsOfDate.get(Calendar.YEAR) - cTempDate.get(Calendar.YEAR)));
		}
		else if (cTempDate.get(Calendar.MONTH) < calCalculateAsOfDate.get(Calendar.MONTH))
		{
			//Otherwise, if the month of the anniversary is PREVIOUS to the current month, then the next anniversary is next year:
			cTempDate.add(Calendar.YEAR, calCalculateAsOfDate.get(Calendar.YEAR) - cTempDate.get(Calendar.YEAR) + 1);
		}
		else if (cTempDate.get(Calendar.DAY_OF_MONTH) < calCalculateAsOfDate.get(Calendar.DAY_OF_MONTH))
		{
			//Otherwise, if the anniversary month is THIS month AND the anniversary day is earlier than TODAY, then the next anniversary
			//is next year:
			cTempDate.add(Calendar.YEAR, calCalculateAsOfDate.get(Calendar.YEAR) - cTempDate.get(Calendar.YEAR) + 1);
		}
		else
		{
			//Or, finally, if the anniversary month is THIS month AND the anniversary day is the As Of date or later, then the next
			//anniversary is this year:
			cTempDate.add(Calendar.YEAR, calCalculateAsOfDate.get(Calendar.YEAR) - cTempDate.get(Calendar.YEAR));
		}
		
		//So at this point, 'cTempDate' carries the correct next anniversary date

		//If the next anniversary date is earlier than or previous to the next month's date
		// AND it is AFTER the 'As Of', then return the next anniversary date:
		if (
				(cTempDate.getTimeInMillis() <= calNextMonth.getTimeInMillis()) 
				&& (cTempDate.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) >= calCalculateAsOfDate.getTimeInMillis())
		) {
			return new java.sql.Date(cTempDate.getTimeInMillis());
		}else{
			return null;
		}
	}
	
	private static void sendSSLMail(
			String sMailHost,
			String sUserName,
			String sPassword,
			String sMailPort,
			String sSubject, 
			String sBody, 
			String sSender, 
			String sRecipients,
			boolean bUseHTML
			) throws Exception 
	{	
		//Sample:
		/*
		SMUtilities.sendMail(
			"smtp.gmail.com",
			"someone@gmail.com",
			"password",
			"465",
			"Subject",
			"This is a test message",
			"test@guess.com",
			"tom_ronayne@odcdc.com,tjprona@gmail.com");
		*/
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		
		//For testing only:
		//sRecipients = "tjprona@gmail.com,tom_ronayne@odcdc.com";
		
		final String sUser = sUserName;
		final String sPw = sPassword;
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", sMailHost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", sMailPort);
		props.put("mail.smtp.socketFactory.port", sMailPort);
		props.put("mail.smtp.socketFactory.class",
		"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");
		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() 
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{ return new PasswordAuthentication(sUser,sPw);	}
		});		
		MimeMessage message = new MimeMessage(session);
		message.setSender(new InternetAddress(sSender));
		message.setSubject(sSubject);
		if (bUseHTML){
			message.setContent(sBody, "text/html");
		}else{
			message.setContent(sBody, "text/plain");
		}
		if (sRecipients.indexOf(',') > 0) 
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sRecipients));
		else
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(sRecipients));
		
		Transport.send(message);
	}
}