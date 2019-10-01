package ServletUtilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.Security;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ConnectionPool.ServerSettingsFileParameters;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTablesmoptions;

public class clsServletUtilities {

	//Date/time constants:
	public final static String EMPTY_DATETIME_VALUE = "00/00/0000 00:00:00 AM";
	public final static String EMPTY_DATE_VALUE = "00/00/0000";
	public final static String DATETIME_FORMAT_FOR_SQL = "yyyy-MM-dd hh:mm:ss";
	
	//This will change, for example, a date/time like '1/1/2018 1:00:00 PM' to '2018-01-01 13:00:00'
	// Without the 'HH', it would be converted to '2018-01-01 01:00:00'
	public final static String DATETIME_24HR_FORMAT_FOR_SQL = "yyyy-MM-dd HH:mm:ss";  
	public final static String DATE_FORMAT_FOR_SQL = "yyyy-MM-dd";
	public final static String EMPTY_SQL_DATETIME_VALUE = "0000-00-00 00:00:00";
	public final static String EMPTY_SQL_DATE_VALUE = "0000-00-00";
	public final static String DATE_FORMAT_FOR_DISPLAY = "MM/dd/yyyy";
	public final static String DATETIME_FORMAT_FOR_DISPLAY = "MM/dd/yyyy hh:mm:ss a";
	public final static String DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY = "Mdyy";   //For example: '060117' for 06/01/2017
	
	private static final String SMTP_PORT_NUMBER = "587";
	
	public static final String DOCTYPE =
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		"Transitional//EN\">";

	public static final String DEFAULT_BK_COLOR = "FDF5E6";
	
	//TJR - 4/15/2011 - this is supposed to be the CORRECT way to check a checkbox,
	//according to the HTML standard (http://www.w3.org/TR/html401/interact/forms.html#checkbox)
	public static String CHECKBOX_CHECKED_STRING = " checked=\"yes\" ";
	public static String CHECKBOX_UNCHECKED_STRING = " checked=\"no\" ";
	
	//These are intended to be read and displayed by tomcat in the sessions manager.
	
	/*
	 Note about how tomcat determines the 'username' and the 'locale', which appear in the sessions manager:
	 
	For the user name, the manager uses the Session.getPrincipal() if available; if not, it tries
	the following Session attribute names:
	  Login
	  User
	  userName
	  UserName
	  Utilisateur
	using the given case, lower case, and upper case.  Failing that, it searches for attributes
	that are instances of java.security.Principal or javax.security.auth.Subject.
	
	For locale, the manager tries the following attribute names:
	  org.apache.struts.action.LOCALE
	  org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE
	  javax.servlet.jsp.jstl.fmt.locale
	  Locale
	  java.util.Locale
	again using the given case, lower case, and upper case.  Additional checks are made for an
	attribute name containing both "tapestry" and "engine", and instances of java.util.Locale.
	 */
	public static String SESSION_PARAM_FULL_USER_NAME = "Login";
	public static String SESSION_PARAM_LOCALE = "Locale";
	
	public static double RoundHalfUp(double dOriginal, int iDecimal) {
		BigDecimal bd = new BigDecimal(dOriginal);
		bd = bd.setScale(iDecimal, BigDecimal.ROUND_HALF_UP);
		dOriginal = bd.doubleValue();
		return(dOriginal);
	}

	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}

	public static Date FindNextTargetDay(long lTime, int TargetDay) {
		Calendar c = Calendar.getInstance(); 
		c.setTimeInMillis(lTime);
		//starting date will not be considered
		c.add(Calendar.DAY_OF_MONTH, 1);
		try{
			while (c.get(Calendar.DAY_OF_WEEK) != TargetDay){
				c.add(Calendar.DAY_OF_MONTH, 1);
			}		  
			return new java.sql.Date(c.getTime().getTime());
		}catch(Exception ex){
			System.out.println("Error in finding previous date");
			System.out.println("Exception = " + ex.getMessage());
			return Date.valueOf("1979-04-10");
		}
	}

	public static Date FindPreviousTargetDay(long lTime, int TargetDay) {
		Calendar c = Calendar.getInstance(); 
		c.setTimeInMillis(lTime);
		//starting date will not be considered
		c.add(Calendar.DAY_OF_MONTH, -1);
		try{
			while (c.get(Calendar.DAY_OF_WEEK) != TargetDay){
				c.add(Calendar.DAY_OF_MONTH, -1);
			}

			return new java.sql.Date(c.getTime().getTime());
		}catch(Exception ex){
			System.out.println("Error in finding previous date");
			System.out.println("Exception = " + ex.getMessage());
			return Date.valueOf("1979-04-10");
		}
	}

	public static long FindFirstDayOfMonth(long lTime){

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(lTime);

		Calendar cFirstDay = Calendar.getInstance();
		cFirstDay.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
		return cFirstDay.getTimeInMillis();
	}

	public static long FindLastDayOfMonth(long lTime){

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(lTime);

		Calendar cLastDay = Calendar.getInstance();
		cLastDay.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
		cLastDay.add(Calendar.MONTH, 1);
		cLastDay.add(Calendar.DAY_OF_YEAR, -1);
		return cLastDay.getTimeInMillis();
	}

	public static String Print_50_States_Drop_Down_Options(){
		return "<OPTION VALUE=\"AK\">AK" + 
		"<OPTION VALUE=\"AL\">AL" + 
		"<OPTION VALUE=\"AR\">AR" + 
		"<OPTION VALUE=\"AZ\">AZ" + 
		"<OPTION VALUE=\"CA\">CA" +
		"<OPTION VALUE=\"CO\">CO" +
		"<OPTION VALUE=\"CT\">CT" +
		"<OPTION VALUE=\"DE\">DE" +
		"<OPTION VALUE=\"FL\">FL" +
		"<OPTION VALUE=\"GA\">GA" +
		"<OPTION VALUE=\"HI\">HI" +
		"<OPTION VALUE=\"IA\">IA" +
		"<OPTION VALUE=\"ID\">ID" +
		"<OPTION VALUE=\"IL\">IL" +
		"<OPTION VALUE=\"IN\">IN" +
		"<OPTION VALUE=\"KS\">KS" +
		"<OPTION VALUE=\"KY\">KY" +
		"<OPTION VALUE=\"LA\">LA" +
		"<OPTION VALUE=\"MA\">MA" +
		"<OPTION VALUE=\"MD\">MD" +
		"<OPTION VALUE=\"ME\">ME" +
		"<OPTION VALUE=\"MI\">MI" +
		"<OPTION VALUE=\"MN\">MN" +
		"<OPTION VALUE=\"MO\">MO" +
		"<OPTION VALUE=\"MS\">MS" +
		"<OPTION VALUE=\"MT\">MT" +
		"<OPTION VALUE=\"NC\">NC" +
		"<OPTION VALUE=\"ND\">ND" +
		"<OPTION VALUE=\"NE\">NE" +
		"<OPTION VALUE=\"NH\">NH" +
		"<OPTION VALUE=\"NJ\">NJ" +
		"<OPTION VALUE=\"NM\">NM" +
		"<OPTION VALUE=\"NV\">NV" +
		"<OPTION VALUE=\"NY\">NY" +
		"<OPTION VALUE=\"OH\">OH" +
		"<OPTION VALUE=\"OK\">OK" +
		"<OPTION VALUE=\"OR\">OR" +
		"<OPTION VALUE=\"PA\">PA" +
		"<OPTION VALUE=\"RI\">RI" +
		"<OPTION VALUE=\"SC\">SC" +
		"<OPTION VALUE=\"SD\">SD" +
		"<OPTION VALUE=\"TN\">TN" +
		"<OPTION VALUE=\"TX\">TX" +
		"<OPTION VALUE=\"UT\">UT" +
		"<OPTION VALUE=\"VA\">VA" +
		"<OPTION VALUE=\"VT\">VT" +
		"<OPTION VALUE=\"WA\">WA" +
		"<OPTION VALUE=\"WI\">WI" +
		"<OPTION VALUE=\"WV\">WV" +
		"<OPTION VALUE=\"WY\">WY";
	}
	
	public static String Print_50_States_Drop_Down_Options(String sDefault){

		String s = "<OPTION VALUE=\"AK\">AK" +
		"<OPTION VALUE=\"AL\">AL" + 
		"<OPTION VALUE=\"AR\">AR" + 
		"<OPTION VALUE=\"AZ\">AZ" + 
		"<OPTION VALUE=\"CA\">CA" +
		"<OPTION VALUE=\"CO\">CO" +
		"<OPTION VALUE=\"CT\">CT" +
		"<OPTION VALUE=\"DE\">DE" +
		"<OPTION VALUE=\"FL\">FL" +
		"<OPTION VALUE=\"GA\">GA" +
		"<OPTION VALUE=\"HI\">HI" +
		"<OPTION VALUE=\"IA\">IA" +
		"<OPTION VALUE=\"ID\">ID" +
		"<OPTION VALUE=\"IL\">IL" +
		"<OPTION VALUE=\"IN\">IN" +
		"<OPTION VALUE=\"KS\">KS" +
		"<OPTION VALUE=\"KY\">KY" +
		"<OPTION VALUE=\"LA\">LA" +
		"<OPTION VALUE=\"MA\">MA" +
		"<OPTION VALUE=\"MD\">MD" +
		"<OPTION VALUE=\"ME\">ME" +
		"<OPTION VALUE=\"MI\">MI" +
		"<OPTION VALUE=\"MN\">MN" +
		"<OPTION VALUE=\"MO\">MO" +
		"<OPTION VALUE=\"MS\">MS" +
		"<OPTION VALUE=\"MT\">MT" +
		"<OPTION VALUE=\"NC\">NC" +
		"<OPTION VALUE=\"ND\">ND" +
		"<OPTION VALUE=\"NE\">NE" +
		"<OPTION VALUE=\"NH\">NH" +
		"<OPTION VALUE=\"NJ\">NJ" +
		"<OPTION VALUE=\"NM\">NM" +
		"<OPTION VALUE=\"NV\">NV" +
		"<OPTION VALUE=\"NY\">NY" +
		"<OPTION VALUE=\"OH\">OH" +
		"<OPTION VALUE=\"OK\">OK" +
		"<OPTION VALUE=\"OR\">OR" +
		"<OPTION VALUE=\"PA\">PA" +
		"<OPTION VALUE=\"RI\">RI" +
		"<OPTION VALUE=\"SC\">SC" +
		"<OPTION VALUE=\"SD\">SD" +
		"<OPTION VALUE=\"TN\">TN" +
		"<OPTION VALUE=\"TX\">TX" +
		"<OPTION VALUE=\"UT\">UT" +
		"<OPTION VALUE=\"VA\">VA" +
		"<OPTION VALUE=\"VT\">VT" +
		"<OPTION VALUE=\"WA\">WA" +
		"<OPTION VALUE=\"WI\">WI" +
		"<OPTION VALUE=\"WV\">WV" +
		"<OPTION VALUE=\"WY\">WY";
		//find where the selected state first
		if (sDefault.trim().length() > 0){
			s = s.substring(0, s.indexOf(sDefault) - 7) + "SELECTED " + s.substring(s.indexOf(sDefault) - 6);
		}
		return s;
	}

	public static String Build_HTML_Table (int iNumberOfColumns,
			ArrayList<String> sTableValues,
			int iBorderWidth,
			boolean bEqualWidth){

		//find out total rows, including empty space:
		int iTotalRows = (int)Math.ceil(sTableValues.size() / (double)iNumberOfColumns); 
		//System.out.println("Total Row = " + iTotalRows);
		//find out how many full columns
		int iFullColumns = sTableValues.size() % iNumberOfColumns;
		if (iFullColumns == 0){
			iFullColumns = iNumberOfColumns;
		}

		String sTable = "<TABLE BORDER = " + iBorderWidth + ">\n<TR>\n";
		int iArrayCounter = 0;
		try {
			for (int iCol=0 ; iCol < iNumberOfColumns; iCol++){
				int iLessRow;
				if (iCol < iFullColumns){
					iLessRow = 0;
				}else{
					iLessRow = 1;
				}
				sTable = sTable + "<TD VALIGN=TOP";
				if (bEqualWidth){
					sTable = sTable + " WIDTH = " + 100 / iNumberOfColumns + "%";
				}
				sTable = sTable + ">\n<TABLE BORDER=0 WIDTH=100%>";
				for (int iRow=0; iRow < iTotalRows - iLessRow; iRow++){
					sTable = sTable + "<TR>\n<TD>\n " + sTableValues.get(iArrayCounter) + " </TD>\n</TR>\n\n";
					iArrayCounter++;
				}
				sTable = sTable + "</TABLE>\n</TD>\n";
			}
		} catch (Exception e) {
			sTable += "Error [1419007396] - " + e.getMessage();
		}

		sTable = sTable + "</TR>\n\n</TABLE>\n";
		return sTable;
	} 

	public static String Build_HTML_Table (int iNumberOfColumns,
			ArrayList<String> sTableValues,
			int iTotalWidthPercentage,
			int iBorderWidth,
			boolean bEqualWidth,
			boolean bVertical){

		String sTable = "<TABLE BORDER=" + iBorderWidth;

		if (iTotalWidthPercentage > 0){
			sTable = sTable  + " WIDTH=" + iTotalWidthPercentage + "%";
		}
		sTable = sTable + ">";

		if (bVertical){
			//find out total rows, including empty space:
			int iTotalRows = (int)Math.ceil(sTableValues.size() / (double)iNumberOfColumns); 
			//System.out.println("Total Row = " + iTotalRows);
			//find out how many full columns
			int iFullColumns = sTableValues.size() % iNumberOfColumns;
			if (iFullColumns == 0){
				iFullColumns = iNumberOfColumns;
			}

			int iArrayCounter = 0;
			sTable = sTable + "<TR>\n";
			for (int iCol=0 ; iCol < iNumberOfColumns; iCol++){
				int iLessRow;
				if (iCol < iFullColumns){
					iLessRow = 0;
				}else{
					iLessRow = 1;
				}
				sTable = sTable + "<TD VALIGN=TOP";
				if (bEqualWidth){
					sTable = sTable + " WIDTH = " + 100 / iNumberOfColumns + "%";
				}
				sTable = sTable + "><TABLE BORDER=0 WIDTH=100%>";
				for (int iRow=0; iRow < iTotalRows - iLessRow; iRow++){
					sTable = sTable + "<TR>\n<TD>\n " + sTableValues.get(iArrayCounter) + " </TD>\n</TR>\n";
					iArrayCounter++;
				}
				sTable = sTable + "</TABLE></TD>\n";
			}

			sTable = sTable + "</TR>\n";
		}else{
			//find out total rows, including empty space:
			int iArrayCounter = 0;
			while (iArrayCounter < sTableValues.size()){
				sTable = sTable + "<TR>\n";
				for (int iCol=0 ; iCol < iNumberOfColumns; iCol++){
					sTable = sTable + "<TD VALIGN=TOP";
					if (bEqualWidth){
						sTable = sTable + " WIDTH = " + 100 / iNumberOfColumns + "%";
					}
					if (iArrayCounter < sTableValues.size()){
						sTable = sTable + ">" + sTableValues.get(iArrayCounter) + " </TD>\n";
					}else{
						sTable = sTable + ">&nbsp;</TD>\n";
					}
					iArrayCounter++;
					//System.out.println("iArrayCounter = " + iArrayCounter);
				}
				sTable = sTable + "</TR>\n";
			}
		}
		sTable = sTable + "</TABLE>";
		return sTable;
	} 
	public static String Build_HTML_Table (int iNumberOfColumns,
			ArrayList<String> sTableValues,
			int iTotalWidthPercentage,
			int iBorderWidth,
			boolean bEqualWidth,
			boolean bVertical,
			String sColor
			){

		String sTable = "<TABLE BORDER=" + iBorderWidth;

		if (iTotalWidthPercentage > 0){
			sTable = sTable  + " WIDTH=" + iTotalWidthPercentage + "%";
		}
		sTable += " BGCOLOR = \"" + sColor + "\" ";
		sTable = sTable + ">";

		if (bVertical){
			//find out total rows, including empty space:
			int iTotalRows = (int)Math.ceil(sTableValues.size() / (double)iNumberOfColumns); 
			//System.out.println("Total Row = " + iTotalRows);
			//find out how many full columns
			int iFullColumns = sTableValues.size() % iNumberOfColumns;
			if (iFullColumns == 0){
				iFullColumns = iNumberOfColumns;
			}

			int iArrayCounter = 0;
			sTable = sTable + "<TR>\n";
			for (int iCol=0 ; iCol < iNumberOfColumns; iCol++){
				int iLessRow;
				if (iCol < iFullColumns){
					iLessRow = 0;
				}else{
					iLessRow = 1;
				}
				sTable = sTable + "<TD VALIGN=TOP";
				if (bEqualWidth){
					sTable = sTable + " WIDTH = " + 100 / iNumberOfColumns + "%";
				}
				sTable = sTable + "><TABLE BORDER=0 WIDTH=100%>";
				for (int iRow=0; iRow < iTotalRows - iLessRow; iRow++){
					sTable = sTable + "<TR>\n<TD>\n " + sTableValues.get(iArrayCounter) + " </TD>\n</TR>\n";
					iArrayCounter++;
				}
				sTable = sTable + "</TABLE></TD>\n";
			}

			sTable = sTable + "</TR>\n";
		}else{
			//find out total rows, including empty space:
			int iArrayCounter = 0;
			while (iArrayCounter < sTableValues.size()){
				sTable = sTable + "<TR>\n";
				for (int iCol=0 ; iCol < iNumberOfColumns; iCol++){
					sTable = sTable + "<TD VALIGN=TOP";
					if (bEqualWidth){
						sTable = sTable + " WIDTH = " + 100 / iNumberOfColumns + "%";
					}
					if (iArrayCounter < sTableValues.size()){
						sTable = sTable + ">" + sTableValues.get(iArrayCounter) + " </TD>\n";
					}else{
						sTable = sTable + ">&nbsp;</TD>\n";
					}
					iArrayCounter++;
					//System.out.println("iArrayCounter = " + iArrayCounter);
				}
				sTable = sTable + "</TR>\n";
			}
		}
		sTable = sTable + "</TABLE>";
		return sTable;
	} 

	public static boolean TypeCombinationCheck(int iCombination, int iID){

		if ((iCombination ^ (int)Math.pow(2, iID)) < iCombination){
			return true;
		}else{
			return false;
		}
	} 

	public static boolean TypeDirectCheck(int iCombination1, int iCombination2){

		if ((iCombination1 ^ iCombination2) < iCombination1){
			return true;
		}else{
			return false;
		}
	}

	public static String ConstructTimeString(String sHour, String sMinute, String sAMPM){

		String sStartTimeString;
		if (Integer.parseInt(sHour) == 12 && Integer.parseInt(sAMPM) == 1){
			sStartTimeString = "12:" + sMinute + ":00";
		}else if(Integer.parseInt(sHour) == 12 && Integer.parseInt(sAMPM) == 0){
			sStartTimeString = "00:" + sMinute + ":00";
		}else{
			sStartTimeString = (Integer.parseInt(sHour) + Integer.parseInt(sAMPM) * 12) + ":" + sMinute + ":00";
		}
		return sStartTimeString;
	}	

	public static boolean IsLeapYear(int iYear){

		/* this function checks to see if the given year is a leap year
		 * if it is a leap year, return 1, otherwise return 0. In this 
		 * way, just simply add the result of this function will make 
		 * sense.
		 */
		if (iYear % 4 == 0) {
			if (iYear % 100 != 0) {
				return true;
			}else if (iYear % 400 == 0) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}

	public static String URLEncode(String s){

		String sEncodedURL;

		try{
			sEncodedURL = URLEncoder.encode(s, "UTF-8" );
		}catch(UnsupportedEncodingException ex){
			System.out.println("Error in URLEncode: " + ex.getMessage());
			sEncodedURL = "";
		}
		return sEncodedURL;
	}

	public static String URLDecode(String s){

		String sDecodedURL;

		try{
			sDecodedURL = URLDecoder.decode(s, "UTF-8" );
		}catch(UnsupportedEncodingException ex){
			System.out.println("Error in URLDecode: " + ex.toString());
			sDecodedURL = "";
		}
		return sDecodedURL;
	}

	public static boolean SendMail (String sHostName, 
			int iPort, 
			String sMailFrom, 
			ArrayList<String> sMailTo, 
			String sSubject,
			String sYourServerName,
			String sMessage){

		Socket smtpSocket = null;
		DataOutputStream os = null;
		DataInputStream is = null;

		Date dDate = new Date(System.currentTimeMillis());
		DateFormat dFormat = DateFormat.getDateInstance(DateFormat.FULL,Locale.US);
		/*
		System.out.println("[1341937774] sHostName = " + sHostName);
		System.out.println("[1341937774] iPort = " + iPort);
		System.out.println("[1341937774] sMainForm = " + sMailFrom);
		for (int i=0;i<sMailTo.size();i++){
			System.out.println("[1341937774] sMailTo(" + i + ") = " + sMailTo.get(i)); 
		}
		System.out.println("[1341937774] sSubject = " + sSubject);
		System.out.println("[1341937774] sYourServerName = " + sYourServerName);
		*/
		//System.out.println("Email server passed into SendMail(): " + sHostName);
		try { // Open port to server
		    InetAddress addr = InetAddress.getByName(sHostName);
		    SocketAddress sockaddr = new InetSocketAddress(addr, iPort);

			smtpSocket = new Socket();
			smtpSocket.connect(sockaddr, 10*1000);
			os = new DataOutputStream(smtpSocket.getOutputStream());
			is = new DataInputStream(smtpSocket.getInputStream());

			if(smtpSocket != null && os != null && is != null){
				// Connection was made.  Socket is ready for use.
				try{   
					os.writeBytes("HELO " + sYourServerName + "\r\n");
					// You will add the email address that the server 
					// you are using knows  you as.
					os.writeBytes("MAIL From: " + sMailFrom + "\r\n");
	
					// Who the email is going to.
					for (int i = 0; i < sMailTo.size(); i++) {
						//System.out.println("RCPT TO: " + sMailTo.get(i).toString());
						os.writeBytes("RCPT To: " + sMailTo.get(i).toString() + "\r\n");
					}
	
					// Now we are ready to add the message and the 
					// header of the email to be sent out.                
					os.writeBytes("DATA\r\n");
	
					os.writeBytes("X-Mailer: Via Java\r\n");
					os.writeBytes("DATE: " + dFormat.format(dDate) + "\r\n");
					os.writeBytes("From: " + sMailFrom + "\r\n");
					os.writeBytes("To: " + sMailTo + "\r\n");
	
					//Again if you want to send a CC then add this.
					//os.writeBytes("Cc: CCDUDE <CCPerson@theircompany.com>\r\n");
	
					//Here you can now add a BCC to the message as well
					//os.writeBytes("RCPT Bcc: BCCDude<BCC@invisiblecompany.com>\r\n");
	
					os.writeBytes("Subject: " + sSubject + "\r\n");
					os.writeBytes(sMessage + "\r\n");
					os.writeBytes("\r\n.\r\n");
					os.writeBytes("QUIT\r\n");
	
					// Now send the email off and check the server reply.  
					// Was an OK is reached you are complete.
					String responseline;
					
					BufferedReader buffInStream = new BufferedReader (new InputStreamReader(is));
					
					//while((responseline = is.readLine())!=null)
					
					while(true){
						responseline = buffInStream.readLine();
						if(responseline.indexOf("Ok") != -1){
							break;
						}
						if (responseline.compareToIgnoreCase("") == 0){
							break;
						}
					}
				}catch(Exception e){  
					System.out.println("Error [1454607127] - Cannot send email as an error occurred - " + e.getMessage());
					smtpSocket.close();
					return false;
				}		  
			}else{
				System.out.println("Error [1454607128] - Failed to establish connection.");
				smtpSocket.close();
				return false;
			}
			smtpSocket.close();
		} catch (SocketTimeoutException e){
			System.out.println("In ServletUtilities.SendMail - " 
				+ "Error [1454607129] Timeout trying to open socket at " + sHostName + " on port " + iPort + " - " + e.getMessage());
			return false;
		} catch (UnknownHostException e) {
			System.out.println("In ServletUtilities.SendMail - " 
				+ "Error [1454607130] Can't find host open socket at " + sHostName + " on port " + iPort + " - " + e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("In ServletUtilities.SendMail - " 
				+ "Error [1454607131] IO Exception writing to socket at " + sHostName + " on port " + iPort + " - " + e.getMessage());
			return false;
		} catch(Exception e) { 
			System.out.println("Error [1454607132] Host " + sHostName + "unknown - " + e.getMessage());
			return false;
		}
		//System.out.println("16");
		return true;
	}

	public static String Format_PhoneNumber(String sOriPhoneNumber){

		String sTest = sOriPhoneNumber.trim();
		String sFormatted = "";
		for (int i = 0; i < sTest.length();i++){
			if ("01234567890".contains(sTest.subSequence(i, i + 1))){
				sFormatted = sFormatted + sTest.substring(i, i + 1);
			}
		}

		if (sFormatted.length() == 10){
			return "(" + sFormatted.substring(0, 3) + ") " 
			+ sFormatted.substring(3, 6) + "-" 
			+ sFormatted.substring(6, 10);
		}else if (sFormatted.length() < 10){
			return sOriPhoneNumber;
		}else{
			return "(" + sFormatted.substring(0, 3) + ") " +
			sFormatted.substring(3, 6) + "-" +  
			sFormatted.substring(6, 10) + "&nbsp;&nbsp;Ext&nbsp;" + 
			sFormatted.substring(10);
		}	
	}
	public static String addPhoneNumberLink(String sOriPhoneNumber){

		//if (sOriPhoneNumber.indexOf(" ") != -1){
		//	sOriPhoneNumber = sOriPhoneNumber.substring(0, sOriPhoneNumber.indexOf(" "));
		//}
		String sTest = sOriPhoneNumber.trim();
		
		String sUnFormatted  = "";
		//Pick off ONLY the digits in the phone number until we get 10 of them, then use that as the 'unformatted' phone number
		for (int i = 0; i < sTest.length();i++){
			if ("01234567890".contains(sTest.subSequence(i, i + 1))){
				sUnFormatted = sUnFormatted + sTest.substring(i, i + 1);
				if (sUnFormatted.length() == 10){
					break;
				}
			}
		}
		//Example: <a href="tel:+13235798328p22">323-579-8328 ext. 22</a>
		return "<a href=\"tel:+1" + sUnFormatted + "\">" + sOriPhoneNumber + "</a>";
	}
	
	public static String getAbsoluteRootPath(HttpServletRequest req, ServletContext context){

		String sPath = "";
		
		if (context.getInitParameter(WebContextParameters.webappname) != null){
			sPath = System.getProperty( "catalina.base" ) 
					+ System.getProperty("file.separator")  + "webapps" 
					+ System.getProperty("file.separator") + context.getInitParameter(WebContextParameters.webappname);
		}else{
			sPath = System.getProperty( "catalina.base" ) + System.getProperty("file.separator") + "webapps";
		}

		//Strip off any file separators we don't need:
		while (
				sPath.endsWith(System.getProperty("file.separator"))
				|| sPath.endsWith(".")
		){
			sPath = sPath.substring(0, sPath.length() - 1);
		}

		//Now add back one file separator:
		sPath = sPath + System.getProperty("file.separator");

		return sPath;
	}
	
	public static String getAbsoluteSQLRootPath(ServletContext servlet, HttpSession CurrentSession, String sDATABASEID) throws Exception{
		String path = "";
		try{
			String sSQL = "SELECT @@secure_file_priv";
			ResultSet rsSQLPathResultSet = clsDatabaseFunctions.openResultSet(sSQL, servlet, sDATABASEID);
			while(rsSQLPathResultSet.next()){
				path = rsSQLPathResultSet.getString("@@secure_file_priv");
			}
			rsSQLPathResultSet.close();
		}catch(Exception e ){
			throw new Exception("ERROR [1534442681] "+e.getMessage());
		}
		return path;
	}
	
	public static String getDatePickerStringWithSelect (String sBoundFieldName, String sCheckedFieldID, ServletContext context){

		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);

		if (sImagePath != null){
			return "<img src=\"" + sImagePath + "calendar_icon.png\" "
					+ "alt=\"calendar_icon.png\" "
					+ "onclick='scwShow(scwID(\"" + sBoundFieldName + "\"),event); document.getElementById(\""+ sCheckedFieldID + "\").checked = true;' />";
		}else{
			return "<img src=\"../images/calendar_icon.png\" "
					+ "alt=\"calendar_icon.png\" "
					+ "onclick='scwShow(scwID(\"" + sBoundFieldName + "\"),event); document.getElementById(\""+ sCheckedFieldID + "\").checked = true;' />";
		}

	}
	
	public static String getDatePickerString (String sBoundFieldName, ServletContext context){

		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);

		if (sImagePath != null){
			return "<img src=\"" + sImagePath + "calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShow(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}else{
			return "<img src=\"../images/calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShow(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}

	}
	
	public static String getRightDatePickerString (String sBoundFieldName, ServletContext context){

		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);

		if (sImagePath != null){
			return "<img src=\"" + sImagePath + "calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShowPositionedRight(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}else{
			return "<img src=\"../images/calendar_icon.png\" alt=\"calendar_icon.png\" onclick='scwShowPositionedRight(scwID(\"" 
			+ sBoundFieldName + "\"),event);' />";
		}
	}
	
	public static String getMasterStyleSheetLink(){
		//Adding the '?v=1.1' SHOULD force the browser to refresh the file:
		return "\n<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/MasterStyleSheet.css?v=1.2\" media=\"all\" />\n"
				+ "\n<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/Notifications.css\" media=\"all\" />\n";
	}
	

	public static String getDatePickerIncludeString (ServletContext context){
		String sScriptPath = context.getInitParameter(WebContextParameters.scriptpath);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + "scw002.js'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/scw002.js'></script>";
		}
	}
	
	public static String getColorPickerIncludeString (ServletContext context){
		String sScriptPath = context.getInitParameter(WebContextParameters.scriptpath);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + "jscolor.js'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/jscolor.js'></script>";
		}
	}
	
	public static String getShortcutJSIncludeString (ServletContext context){
		String sScriptPath = context.getInitParameter(WebContextParameters.scriptpath);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + "shortcuts.js'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/shortcuts.js'></script>";
		}
	}
	public static String getSMCPJSIncludeString (ServletContext context){
		String sScriptPath = context.getInitParameter(WebContextParameters.scriptpath);

		if (sScriptPath != null){
			return "<script type='text/JavaScript' src='" + sScriptPath + "smcp.js'></script>";
		}else{
			return "<script type='text/JavaScript' src='../javascript/smcp.js'></script>";
		}
	}
	
	public static String getDrivePickerJSIncludeString (String sRecordType, String sKeyValue,  ServletContext context, String sDBID) throws Exception{

		String sAppId = "";
		String sClientId = "";
		String sDeveloperKey = "";
		String sDomainAccount = "";
		String sDomain = "";
		String sParentFolder = "";
		String sFolderName = sKeyValue;

		String SQL = "SELECT * FROM " + SMTablesmoptions.TableName
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					"ServletUtilities.getDrivePickerJSIncludeString"
					+ " [1331745216456]"
				);
				if (rs.next()){
					//If the drive picker is not set to be used in the settings then return.
					if(rs.getInt(SMTablesmoptions.iusegoogledrivepickerapi) == 0) {
						return "";
					}
					//get all of the API credentials.
					if(rs.getString(SMTablesmoptions.sgoogleapiprojectid).compareToIgnoreCase("") != 0) {
						sAppId = rs.getString(SMTablesmoptions.sgoogleapiprojectid).trim();
					}
					if(rs.getString(SMTablesmoptions.sgoogleapiclientid).compareToIgnoreCase("") != 0) {
						sClientId =  rs.getString(SMTablesmoptions.sgoogleapiclientid).trim();
					}
					if(rs.getString(SMTablesmoptions.sgoogleapikey).compareToIgnoreCase("") != 0) {
						sDeveloperKey =  rs.getString(SMTablesmoptions.sgoogleapikey).trim();
					}
					sDomainAccount =  rs.getString(SMTablesmoptions.sgoogledomain);
					if(sDomainAccount.trim().compareToIgnoreCase("") == 0) {
						sDomain = "";
					}else {
						sDomain = sDomainAccount.substring(sDomainAccount.indexOf("@") + 1).trim();
					}
					
					switch(sRecordType){
						case SMCreateGoogleDriveFolderParamDefinitions.ORDER_RECORD_TYPE_PARAM_VALUE:
							sFolderName = rs.getString(SMTablesmoptions.gdriveorderfolderprefix) 
							+ sKeyValue + rs.getString(SMTablesmoptions.gdriveorderfoldersuffix);
							sParentFolder = rs.getString(SMTablesmoptions.gdriveorderparentfolderid);
							break;
						case SMCreateGoogleDriveFolderParamDefinitions.DISPLAYED_ORDER_TYPE_PARAM_VALUE:
							sFolderName = rs.getString(SMTablesmoptions.gdriveorderfolderprefix) 
							+ sKeyValue + rs.getString(SMTablesmoptions.gdriveorderfoldersuffix);
							sParentFolder = rs.getString(SMTablesmoptions.gdriveorderparentfolderid);
							break;
						case SMCreateGoogleDriveFolderParamDefinitions.WORK_ORDER_TYPE_PARAM_VALUE:
							sFolderName = rs.getString(SMTablesmoptions.gdriveworkorderfolderprefix) 
							+ sKeyValue + rs.getString(SMTablesmoptions.gdriveworkorderfoldersuffix);
							sParentFolder = rs.getString(SMTablesmoptions.gdriveworkorderparentfolderid);
							break;
						case SMCreateGoogleDriveFolderParamDefinitions.SALESLEAD_RECORD_TYPE_PARAM_VALUE:
							sFolderName = rs.getString(SMTablesmoptions.gdrivesalesleadfolderprefix) 
							+ sKeyValue + rs.getString(SMTablesmoptions.gdrivesalesleadfoldersuffix);
							sParentFolder = rs.getString(SMTablesmoptions.gdrivesalesleadparentfolderid);
							break;
						
						case SMCreateGoogleDriveFolderParamDefinitions.PO_RECORD_TYPE_PARAM_VALUE:
						     
							 SQL = "SELECT * FROM " + SMTableicoptions.TableName;
							try {
								ResultSet rsICOptions = clsDatabaseFunctions.openResultSet(
									SQL, 
									context, 
									sDBID, 
									"MySQL", 
									"ServletUtilities.getDrivePickerJSIncludeString"
									+ " [1331745216234]"
								);
								if (rsICOptions.next()){
									sFolderName = rsICOptions.getString(SMTableicoptions.gdrivepurchaseordersfolderprefix) 
										+ sKeyValue + rsICOptions.getString(SMTableicoptions.gdrivepurchaseordersfoldersuffix);
									sParentFolder = rsICOptions.getString(SMTableicoptions.gdrivepurchaseordersparentfolderid);
								}		
								rsICOptions.close();
							}catch (Exception e) {
								throw new Exception("Error reading parent folder from ICOptions.");	
							}
							break;
							
						case SMCreateGoogleDriveFolderParamDefinitions.AP_VENDOR_RECORD_TYPE_PARAM_VALUE:
						     
							SQL = "SELECT * FROM " + SMTableapoptions.TableName;
							try {
								ResultSet rsAPOptions = clsDatabaseFunctions.openResultSet(
									SQL, 
									context, 
									sDBID, 
									"MySQL", 
									"ServletUtilities.getDrivePickerJSIncludeString"
									+ " [13317452162349]"
								);
								if (rsAPOptions.next()){
									sFolderName = rsAPOptions.getString(SMTableapoptions.gdrivevendorsderfolderprefix) 
										+ sKeyValue + rsAPOptions.getString(SMTableapoptions.gdrivevendorsfoldersuffix);
									sParentFolder = rsAPOptions.getString(SMTableapoptions.gdrivevendorsparentfolderid);
								}		
								rsAPOptions.close();
							}catch (Exception e) {
								throw new Exception("Error reading parent folder from APOptions.");	
							}
							break;
							
						case SMCreateGoogleDriveFolderParamDefinitions.AR_CUSTOMER_RECORD_TYPE_PARAM_VALUE:
						     
							SQL = "SELECT * FROM " + SMTablearoptions.TableName;
							try {
								ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(
									SQL, 
									context, 
									sDBID, 
									"MySQL", 
									"ServletUtilities.getDrivePickerJSIncludeString"
									+ " [521745222349]"
								);
								if (rsAROptions.next()){
									sFolderName = rsAROptions.getString(SMTablearoptions.gdrivecustomersderfolderprefix) 
										+ sKeyValue + rsAROptions.getString(SMTablearoptions.gdrivecustomersfoldersuffix);
									sParentFolder = rsAROptions.getString(SMTablearoptions.gdrivecustomersparentfolderid);
								}		
								rsAROptions.close();
							}catch (Exception e) {
								throw new Exception("Error reading parent folder from AROptions.");	
							}
							break;
							
						case SMCreateGoogleDriveFolderParamDefinitions.SM_LABOR_BACKCHARGE_PARAM_VALUE:
							sFolderName = rs.getString(SMTablesmoptions.gdrivelaborbackchargefolderprefix) 
							+ sKeyValue + rs.getString(SMTablesmoptions.gdrivelaborbackchargefoldersuffix);
							sParentFolder = rs.getString(SMTablesmoptions.gdrivelaborbackchargeparentfolderid);
							break;
							
						default:
							break;
					}
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error reading API credentials from SMOptions.");
			}
		
		String sScriptPath = context.getInitParameter(WebContextParameters.scriptpath);
		if (sScriptPath != null){
			//Note: consider hiding this information in a json file so it is not public in the html source. 
			return ""
			+ "<script>"
			+ " var appId = '"+ sAppId + "';\n" 
			+ " var clientId = '" + sClientId + "';\n" 
			+ " var developerKey = '" + sDeveloperKey + "';\n" 
			+ " var folderName = '" + sFolderName + "';\n" 
			+ " var recordtype = '" + sRecordType + "';\n" 
			+ " var domain = '" + sDomain + "';\n" 
			+ " var domainaccount = '" + sDomainAccount + "';\n" 
			+ " var parentfolderid = '" + sParentFolder + "';\n" 
			+ " var keyvalue = '" + sKeyValue + "';\n"
			+ "</script>\n"
			+ "<script type=\"text/javascript\" src=\"https://apis.google.com/js/api.js?key=" + sDeveloperKey + "\"></script>"
		    + "<script type='text/JavaScript' src='" + sScriptPath + "drivepicker.js'></script>\n";
			
		}else{
			return "<script type='text/JavaScript' src='../javascript/drivepicker.js'></script>";
		}
	}
	
	public static String getImagePath(ServletContext context){
		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);
		if (sImagePath != null){
			return 	sImagePath;
		}else{
			return 	"\"../images/\"";
		}
	}
	public static String getLogoLink (ServletContext context){

		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);

		if (sImagePath != null){
			return 	"<A HREF=\"" + "https://sites.google.com/site/airotechservicemanager/\">" 
				+ "<img src=\"" + sImagePath + "header.png\" alt=\"Service Manager Control Panel\""
				+ " style=\"border: 0px;\""
				+ "/>"
				+ "</A>"
			;
		}else{
			return 	"<A HREF=\"" + "https://sites.google.com/site/airotechservicemanager/\">" 
				+ "<img src=\"../images/header.png\" alt=\"Service Manager Control Panel\""
				+ " style=\"border: 0px;\""
				+ "/>"
				+ "</A>"
			;
		}
	}
	
	public static String getLargeLogoLink (ServletContext context){

		String sImagePath = context.getInitParameter(WebContextParameters.imagepath);

		if (sImagePath != null){
			return 	"<A HREF=\"" + "https://sites.google.com/site/airotechservicemanager/\">" 
				+ "<img src=\"" + sImagePath + "headerlarge.png\" alt=\"Service Manager Control Panel\""
				+ " style=\"border: 0px;\""
				+ "/>"
				+ "</A>"
			;
		}else{
			return 	"<A HREF=\"" + "https://sites.google.com/site/airotechservicemanager/\">" 
				+ "<img src=\"../images/headerlarge.png\" alt=\"Service Manager Control Panel\""
				+ " style=\"border: 0px;\""
				+ "/>"
				+ "</A>"
			;
		}
	}

	public static String getURLLinkBase(ServletContext context){
		return ConnectionPool.WebContextParameters.getURLLinkBase(context);
	}
	
	public static String getInitBackGroundColor(ServletContext context, String sDBID){
		
		String sBackGroundColor = "#" + DEFAULT_BK_COLOR;
		String SQL = "SELECT"
			+ " " + SMTablesmoptions.sbackgroundcolor
			+ " FROM " + SMTablesmoptions.TableName
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				"ServletUtilities.getInitBackGroundColor"
				+ " [1331736814]"
			);
			if (rs.next()){
				sBackGroundColor = "#" + rs.getString(SMTablesmoptions.sbackgroundcolor);
			}
			rs.close();
		} catch (SQLException e) {
			//Do nothing - just use the default color value.
			//System.out.println("In ServletUtilities.getInitBackgroundColor - error: " + e.getMessage());
		}
		return sBackGroundColor;
		//return ConnectionPool.WebContextParameters.getInitBackGroundColor(context);
	}
	
	public static String getInitBackGroundColor(ServletContext context, Connection conn){
		
		String sBackGroundColor = "#" + DEFAULT_BK_COLOR;
		String SQL = "SELECT"
			+ " " + SMTablesmoptions.sbackgroundcolor
			+ " FROM " + SMTablesmoptions.TableName
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				conn
			);
			if (rs.next()){
				sBackGroundColor = "#" + rs.getString(SMTablesmoptions.sbackgroundcolor);
			}
			rs.close();
		} catch (SQLException e) {
			//Do nothing - just use the default color value.
		}
		
		return sBackGroundColor;
		//return ConnectionPool.WebContextParameters.getInitBackGroundColor(context);
	}

	public static String getHostName(){
		String sHostName = "";
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    // Get hostname
		    sHostName = addr.getHostName();
		} catch (UnknownHostException e) {
		}
		return sHostName;
	}
	
	public static String getInitProgramTitle(ServletContext context){
		return ConnectionPool.WebContextParameters.getInitProgramTitle(context);
	}
	//This function is only used to send customer/vendor notifications, and notifications of canceled orders:
	public static void sendEmail(
			String sMailServerHost,
			String sUserName,
			String sPassword,
			String sReplyToAddress,
			String sMailPort,
			String sSubject, 
			String sBody, 
			String sSender, 
			String sSendingServer,
			ArrayList<String> arrRecipients,
			boolean bUsesSMTPAuthentication,
			boolean bUseHTML
			) throws Exception{
		
		if (bUsesSMTPAuthentication){
			String sRecipients = "";
			for (int i = 0; i < arrRecipients.size(); i++){
				sRecipients+= arrRecipients.get(i) + ",";
			}
			/*
			sendSSLMail(
					sMailServerHost,
					sUserName,
					sPassword,
					sMailPort,
					sSubject, 
					sBody, 
					sSender, 
					sRecipients,
					bUseHTML
					);
			*/
			sendEmailWithEmbeddedHTML(
				sMailServerHost,
				sUserName, 
				sPassword,
				sReplyToAddress,
				sRecipients,
				sSubject, 
				sBody,
				false,
	            null
	         );
		}else{
			if (!SendMail(
					sMailServerHost, 
					Integer.parseInt(sMailPort), 
					sReplyToAddress, 
					arrRecipients, 
					sSubject, 
					sSendingServer, 
					sBody)
				){
				throw new Exception("Could not send unauthenticated mail.");
			}
		}
		
	}
	
	//The following function is only used in the 'Intranet'  project and the Time Card system:
	public static void sendSSLMail(
			String mailhost,
			String sUserName,
			String sPassword,
			String smailport,
			String subject, 
			String body, 
			String sender, 
			String recipients,
			boolean bUseHTML
			) throws Exception 
	{	
		//Sample:
		/*
		SMUtilities.sendMail(
			"smtp.gmail.com",
			"tjprona@gmail.com",
			"pw",
			"465",
			"Subject",
			"This is a test message",
			"test@guess.com",
			"tom_ronayne@odcdc.com,tjprona@gmail.com");
		
		System.out.println("[1372257833] - "
			+ "mailhost = '" + mailhost + "'\n"
			+ ", sUserName = '" + sUserName + "'\n"
			+ ", sPassword = '" + sPassword + "'\n"
			+ ", smailport = '" + smailport + "'\n"
			+ ", subject = '" + subject + "'\n"
			+ ", body = '" + body + "'\n"
			+ ", sender = '" + sender + "'\n"
			+ ", recipients = '" + recipients + "'\n"
		);
		*/
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		final String sUser = sUserName;
		final String sPw = sPassword;
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", smailport);
		props.put("mail.smtp.socketFactory.port", smailport);
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
		message.setSender(new InternetAddress(sUserName));
		message.setSubject(subject);
		if (bUseHTML){
			message.setContent(body, "text/html");
		}else{
			message.setContent(body, "text/plain");
		}
		if (recipients.indexOf(',') > 0){ 
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
		}else{
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
		}
		
		Transport.send(message);
	}
	//This function is only called from within this class:
    private static void sendEmailWithEmbeddedHTML(
    		String host,
            final String userName, 
            final String password,
            String replytoAddress,
            String toAddress,
            String subject, 
            String body,
            boolean bUseHTML,
            Map<String, String> mapInlineImages)
                throws Exception {
        // sets SMTP server properties
        Properties properties;
		try {
			properties = new Properties();
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", SMTP_PORT_NUMBER);
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.user", userName);
			properties.put("mail.password", password);
			//properties.put("mail.smtp.from", fromAddress);  //This line doesn't appear to have any effect..... TJR - 3/8/2018
		} catch (Exception e) {
			throw new Exception("Error [1395084034] - setting properties in EmailInlineHTML - " + e.getMessage());
		}
 
        // creates a new session with an authenticator
        Authenticator auth;
		try {
			auth = new Authenticator() {
			    public PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(userName, password);
			    }
			};
		} catch (Exception e) {
			throw new Exception("Error [1395085035] - getting Authenticator in EmailInlineHTML - " + e.getMessage());
		}
        Session session;
		try {
			session = Session.getInstance(properties, auth);
		} catch (Exception e) {
			throw new Exception("Error [1395085036] - getting session in EmailInlineHTML - " + e.getMessage());
		}
 
        // creates a new e-mail message
        Message msg;
		try {
			msg = new MimeMessage(session);
 
			msg.setFrom(new InternetAddress(userName));
			InternetAddress[] mailAddress_REPLY_TO = new InternetAddress[1];
			mailAddress_REPLY_TO[0] = new InternetAddress(replytoAddress);
			msg.setReplyTo(mailAddress_REPLY_TO);
			//InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			//If there is more than one address, it should be separated with commas:
			if (toAddress.indexOf(',') > 0){ 
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			}else{
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
			}
			
			msg.setSubject(subject);
			//msg.setSentDate(new Date());
 
			if (bUseHTML){

				// adds inline image attachments
				if (mapInlineImages != null){
					// creates message part
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setContent(body, "text/html");
					// creates multi-part
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart);

					if (mapInlineImages.size() > 0) {
					    Set<String> setImageID = mapInlineImages.keySet();
					    for (String contentId : setImageID) {
					        MimeBodyPart imagePart = new MimeBodyPart();
					        imagePart.setHeader("Content-ID", "<" + contentId + ">");
					        imagePart.setDisposition(MimeBodyPart.INLINE);
					        String imageFilePath = mapInlineImages.get(contentId);
					        try {
					            imagePart.attachFile(imageFilePath);
					        } catch (IOException ex) {
					            ex.printStackTrace();
					        }
					        multipart.addBodyPart(imagePart);
					    }
					}
					msg.setContent(multipart);
				}else{
					msg.setContent(body, "text/html");
				}
			}else{
				//If we're NOT sending HTML:
				msg.setContent(body, "text/plain");
			}
		} catch (Exception e) {
			throw new Exception("Error [1395085037] - preparing message in sendEmailWithEmbeddedHTML - " + e.getMessage());
		}
        try {
			Transport.send(msg);
		} catch (Exception e) {
			throw new Exception("Error [1395085038] - sending message in sendEmailWithEmbeddedHTML - " + e.getMessage());
		}
    }
	public static String getFileSuffix(String  sFileName) {
	    try {
	        return sFileName.substring(sFileName.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}
	public static String getImageFileGraphicsType(String  sFileSuffix) {
		//Do a little filtering:
		String sImageFileType = sFileSuffix;
		if (sImageFileType.contains(".jpeg")){
			sImageFileType = "jpg";
		}
		if (sImageFileType.contains(".gif")){
			sImageFileType = "gif";
		}
		if (sImageFileType.contains(".png")){
			sImageFileType = "png";
		}
		return sImageFileType;
	}

	public static String encloseSQLCommandInDoubleQuotes(String sSQL){
		String s = "";
		
		String sLineArray[] = sSQL.split("\n");
		
		for (int iLine = 0; iLine < sLineArray.length; iLine++){
			if (iLine == 0){
				s += "\"";
			}else{
				s += "+ " + "\"";
			}
			s +=  sLineArray[iLine] + "\"" + " + \"\\n\"";
		}
		return s;
	}
	public static String getFullClassName(String sThisToString){

		//Returns the full class name if you pass in 'this.toString()' from the class
		//For example, it might return 'smcontrolpanel.SMBidEdit'
		if (sThisToString.contains("@")){
			return sThisToString.substring(0, sThisToString.indexOf("@"));
		}else{
			return sThisToString;
		}

	}

	public static void sysprint (String sthis_to_string, String sUser, String sMessage){
		java.util.Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = formatter.format(todaysDate);
		System.out.println(
				"SYSPRINT: "
				+ formattedDate
				+ " - "
				+ getFullClassName(sthis_to_string)
				+ " - "
				+ "USER: "
				+ sUser
				+ " - "
				+ sMessage
		);
	}

	public static String createHTMLComment(String sComment){
		return "\n<!--" + sComment + "-->\n";
	}

	public static String getRootPath(ServletContext context){
		String sPath = "";
	
		if (context.getInitParameter(WebContextParameters.webappname) != null){
			sPath = System.getProperty( "catalina.base" ) 
					+ System.getProperty("file.separator")  + "webapps" 
					+ System.getProperty("file.separator") + context.getInitParameter(WebContextParameters.webappname);
		}else{
			sPath = System.getProperty( "catalina.base" ) + System.getProperty("file.separator") + "webapps";
		}

		//Strip off any file separators we don't need:
		while (
				sPath.endsWith(System.getProperty("file.separator"))
				|| sPath.endsWith(".")
		){
			sPath = sPath.substring(0, sPath.length() - 1);
		}
	
		//Now add back one file separator:
		sPath = sPath + System.getProperty("file.separator");
	
		return sPath;
	}

	//This should return a string that looks something like: 'http://smcp001.com:8080', or just '123.456.789', or 'localhost:8080', etc.
	//Basically, it should include EVERYTHING on the URL up to the web app, etc. - everything EXCEPT: '/sm.smcontrolpanel.SM......'
	public static String getServerURL (HttpServletRequest req, ServletContext context) throws Exception{
		String sURL = "";
		try {
			ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
			sURL += serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME);			
		}catch(Exception e) {
			throw new Exception("Error [1542655042] "+e.getMessage());
		}
			
		return sURL;
	}

	//This is designed to trap errors that occasionally happen when trying to read a string from a session:
	public static String getStringAttributeFromSession(HttpSession session, String sStringAttribute){
		String s = "";
		try {
			s = (String) session.getAttribute(sStringAttribute);
		} catch (Exception e) {
			return "";
		}
		return s;
	}

	public static boolean isSessionValid(HttpSession session){
		try {
			  @SuppressWarnings("unused")
			long sd = session.getCreationTime();
		} catch (IllegalStateException ise) {
			return false;
		}
		return true;
	}

	public static String getJQueryIncludeString (){
		return "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>";
	}
	public static String getJQueryUIIncludeString (){
		//UI version 1.11.4 optimizes the sortable function
		return "<script src=\" https://ajax.googleapis.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js\"></script>"
			+ "<link rel=\"stylesheet\" href=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/base/jquery-ui.css\">";
	}
	
	public static String getBootstrapCSSIncludeString (){
		//Get local boostrap.css file
		return "\n<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/Boostrap3.4.1.css?v=1.3\" media=\"all\" />\n";
	}
	
	public static String getPlacesJavascript (
			String sPlaceHolderText,
			String sAddressParamID, 
			String sCityParamID, 
			String sStateParamID,
			String sZipParamID){	
		
			//API address component key words - defined by the Google Places API
			final String sShortName = "short_name";
			final String sLongName = "long_name";
			final String sStreetNumberComponentTypeName = "street_number";
			final String sRouteComponentTypeName = "route";
			final String sCityComponentTypeName = "locality";
			final String sStateComponentTypeName = "administrative_area_level_1";
			final String sZipComponentTypeName = "postal_code";
			
			String s = " <script>\n";
				
			s+="		function gm_authFailure() {\n"  	
					+ "	}\n";
			
			s+= "	    $(document).ready(function() {\n"
					+ "			initAutocomplete" + sAddressParamID + "();\n"
					+ "	});\n";
			
			s +=" 		function initAutocomplete" + sAddressParamID + "() {\n"  
					+ "        // Create the autocomplete object, restricting the search to geographical\n" 
					+ "        // location types.\n"
					+ "  document.getElementById (\"" + sAddressParamID + "\").placeholder = \"" + sPlaceHolderText + "\";"
					+ " $(\"#" + sAddressParamID + "\").attr(\"autocomplete\", \"off\");\n"

					+ "        autocomplete"+sAddressParamID+ " = new google.maps.places.Autocomplete(\n" 
					+ "            (document.getElementById('" + sAddressParamID + "')),\n" 
					+ "            {types: ['geocode']});\n\n" 
					+ "        // When the user selects an address from the dropdown, populate the address\n" 
					+ "        // fields in the form.\n" 
					
					+ "        autocomplete"+sAddressParamID+".addListener('place_changed', fillInAddress" + sAddressParamID + ");\n"
					+ "      }\n"
					;
			
			s+="		function fillInAddress" + sAddressParamID + "() {\n" 
					+ "        // Get the place details from the autocomplete object.\n" 
					+ "        var place = autocomplete"+sAddressParamID+".getPlace();\n" 
					+ "\n"  
					+ "          document.getElementById('" + sAddressParamID + "').value = '';\n"  
					+ "          document.getElementById('" + sCityParamID + "').value = '';\n"
					+ "          document.getElementById('" + sStateParamID + "').value = '';\n"
					+ "          document.getElementById('" + sZipParamID + "').value = '';\n"
					+ "\n" 
					+ "        // Get each component of the address from the place details\n"
					+ "        // and fill the corresponding field on the form.\n" 
					+ "        for (var i = 0; i < place.address_components.length; i++) {\n" 					
					+ "          var addressType = place.address_components[i].types[0];\n" 
					+ "\n			 "  
					+ "          if (addressType == \"" +sStreetNumberComponentTypeName+ "\") {\n" 
					+ "            var val = place.address_components[i][\"" + sShortName + "\"];\n"
					+ "            document.getElementById('" + sAddressParamID + "').value = val;\n"  
					+ "          	 }\n"  
					+ "          if (addressType == \"" + sRouteComponentTypeName + "\") {\n" 
					+ "            var val = place.address_components[i][\"" + sLongName + "\"];\n"
					+ "            document.getElementById('" + sAddressParamID + "').value = \n"
					+ "				document.getElementById('" + sAddressParamID + "').value + ' ' + val;\n"  
					+ "          	 }\n"  
					+ "          	if (addressType == \""+sCityComponentTypeName+"\") {\n" 
					+ "             var val = place.address_components[i][\"" + sLongName + "\"];\n"  
					+ "             document.getElementById('" + sCityParamID + "').value = val;\n"  
					+ "           }\n"  
					+ "          if (addressType == \"" + sStateComponentTypeName + "\") {\n"  
					+ "             var val = place.address_components[i][\"" + sShortName + "\"];\n" 
					+ "             document.getElementById('" + sStateParamID + "').value = val;\n"  
					+ "           }\n" 
					+ "          if (addressType == \"" + sZipComponentTypeName + "\") {\n"  
					+ "             var val = place.address_components[i][\"" + sShortName + "\"];\n" 
					+ "             document.getElementById('" + sZipParamID + "').value = val;\n" 
					+ "           }\n" 
					+ "        }\n"  
					+ "      }"
					;
		      // Bias the autocomplete object to the user's geographical location,
		      // as supplied by the browser's 'navigator.geolocation' object.			
			s+="		function geolocate" + sAddressParamID + "() {\n" 
					+ "       	if (navigator.geolocation) {\n"  
					+ "          navigator.geolocation.getCurrentPosition(function(position) {\n" 
					+ "            var geolocation = {\n" 
					+ "              lat: position.coords.latitude,\n" 
					+ "              lng: position.coords.longitude\n" 
					+ "            };\n" 
					+ "            var circle = new google.maps.Circle({\n" 
					+ "              center: geolocation,\n" 
					+ "              radius: position.coords.accuracy\n" 
					+ "            });\n" 
					+ "           autocomplete" + sAddressParamID + ".setBounds(circle.getBounds());\n" 

					+ "          });\n" 
					+ "        }\n"
					+ "  }\n";
			
			s += "</script>";
			
			return s;
		
	}
	
	public static String getPlacesAPIIncludeString (
			ServletContext context, 
			String sDBID){
		
		 String sAPIKey = getPlacesAPIKey(context, sDBID);
		if(sAPIKey.compareToIgnoreCase("") == 0) {
			return "";
		}else {
			return  "<script src=\"https://maps.googleapis.com/maps/api/js?key=" + sAPIKey + "&libraries=places\"></script> \n";
		}
	}
	
	public static String getPlacesAPIKey (
			ServletContext context, 
			String sDBID){
		
		String sAPIKey = "";
		String sUsePlacesAPI = "0";
		String SQL = "SELECT"
				+ " " + SMTablesmoptions.sgoogleapikey
				+ ", " + SMTablesmoptions.iusegoogleplacesapi
				+ " FROM " + SMTablesmoptions.TableName
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					"ServletUtilities.getPlacesAPIKey"
					+ " [133173634567]"
				);
				if (rs.next()){
					sAPIKey = rs.getString(SMTablesmoptions.sgoogleapikey).trim();
					sUsePlacesAPI = Integer.toString(rs.getInt(SMTablesmoptions.iusegoogleplacesapi)).trim();
				}
				rs.close();
			} catch (SQLException e) {
				return "";
			}
			
			if((sAPIKey.trim().compareToIgnoreCase("") == 0) || (sUsePlacesAPI.compareToIgnoreCase("1") != 0)) {
				return "";
			}
				
			return sAPIKey;
		
	}

	public static String createGoogleMapLink(String sConcatenatedAddress){
		String sLinkBase = "https://maps.google.com/maps?hl=en&geocode=&q=";
		return sLinkBase + clsServletUtilities.URLEncode(sConcatenatedAddress.trim());
	}

	public static String createGoogleMapLink(
			String sAddress1,
			String sAddress2,
			String sAddress3,
			String sAddress4,
			String sCity,
			String sState,
			String sCountry
	){
		String sLinkBase = "https://maps.google.com/maps?hl=en&geocode=&q=";
		String sAddress = sAddress1.trim()
		+ " " + sAddress2.trim()
		+ " " + sAddress3.trim()
		+ " " + sAddress4.trim()
		+ " " + sCity.trim()
		+ " " + sState.trim()
		+ " " + sCountry.trim();
		return sLinkBase + clsServletUtilities.URLEncode(sAddress.trim());
	}

	public static String getFileNameSuffix(String sFileName){
	
		if (sFileName.contains(".") == false){
			return "*N/A*";
		}else{
	
		}
		return sFileName.substring(sFileName.lastIndexOf(".")+ 1, sFileName.length());
	}

	public static String getNoCacheHeaderString (){
		return "\n<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\" />"
			+ "\n<meta http-equiv=\"Pragma\" content=\"no-cache\" />"
			+ "\n<meta http-equiv=\"Expires\" content=\"0\" />"
		;
	}

	public static String Fill_In_Empty_String_For_HTML_Cell(String s){
		if (s.length() ==0){
			return "-";
		}else{
			return s;
		}
	}

	public static String ConvertCheckboxResultToString(String sCheckBoxResult){
	
		if (sCheckBoxResult == null){
			//System.out.println("checkbox is null");
			return "0";
		}
		else{
			//System.out.println("checkbox is not null");
			return "1";
		}
	
	}
	
	public static String getContextParameters(ServletContext context){
		String sContextParameters = "";
		Enumeration<String> enumContextParameters = context.getInitParameterNames();
		while(enumContextParameters.hasMoreElements()) {
			String sParamName = (String)enumContextParameters.nextElement();
			if (sContextParameters.compareToIgnoreCase("") == 0){
				sContextParameters += sParamName + " = " + context.getInitParameter(sParamName);
			}else{
				sContextParameters += ", " + sParamName + " = " + context.getInitParameter(sParamName);
			}
		}
		return sContextParameters;
	}
	public static String getSessionAttributes(HttpSession session){
		
		String sSessionParameters = "";
		Enumeration<String> enumSessionParameters = session.getAttributeNames();
		while(enumSessionParameters.hasMoreElements()) {
			String sAttributeName = (String)enumSessionParameters.nextElement();
			if (sSessionParameters.compareTo("") == 0){
				sSessionParameters += sAttributeName + " = " + session.getAttribute(sAttributeName);
			}else{
				sSessionParameters += ", " + sAttributeName + " = " + session.getAttribute(sAttributeName);
			}
		}
		return sSessionParameters;
	}
}