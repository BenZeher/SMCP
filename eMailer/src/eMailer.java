import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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

public class eMailer
{
	public final static String SMTP_SERVER_URL_SWITCH = "s";
	public final static String SMTP_SERVER_PORT_SWITCH = "o";
	public final static String SENDING_USERNAME_SWITCH = "u";
	public final static String SENDING_USER_PASSWORD_SWITCH = "p";
	public final static String REPLY_TO_ADDRESS_SWITCH = "e";
	public final static String RECIPIENTS_SWITCH = "r";
	public final static String SUBJECT_SWITCH = "b";
	public final static String EMAIL_TEXT_SWITCH = "t";
	public final static String ATTACHED_FILE_SWITCH = "a";
	public final static String INLINE_TEXT_FILE_SWITCH = "i";
	public final static String PRINT_SYNTAX_HELP_SWITCH = "-h";
	
	public final static String COMMAND_LINE_VALUE_DELIMITER = ",";
	public final static String SMTP_PORT_NUMBER = "587";
		
	public static void main(String[] paramArrayOfString)
	{

		String sSMTPServerURL = "";
		String sSMTPPort = "";
		String sSendingUserName = "";
		String sSendingUserPassword = "";
		String sReplyToAddress = "";
		String sRecipients = "";
		String sSubject = "";
		String sText = "";
		String sInlineTextFile = "";
		String sAttachedFilePaths = "";

		if (paramArrayOfString.length == 0) {
			printSyntax();
			return;
		}

		//Check for syntax help:
		for (int i = 0; i < paramArrayOfString.length; i++){
			if (paramArrayOfString[i].compareToIgnoreCase(PRINT_SYNTAX_HELP_SWITCH) == 0){
				printSyntax();
				return;
			}
		}
		
		//Read the parameters into variables:
		for (int i = 0; i < paramArrayOfString.length; i++){
			if (paramArrayOfString[i].compareToIgnoreCase("-" + SMTP_SERVER_URL_SWITCH) == 0){
				sSMTPServerURL = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + SMTP_SERVER_PORT_SWITCH) == 0){
				sSMTPPort = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + SENDING_USERNAME_SWITCH) == 0){
				sSendingUserName = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + SENDING_USER_PASSWORD_SWITCH) == 0){
				sSendingUserPassword = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + REPLY_TO_ADDRESS_SWITCH) == 0){
				sReplyToAddress = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + RECIPIENTS_SWITCH) == 0){
				sRecipients = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + SUBJECT_SWITCH) == 0){
				sSubject = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + EMAIL_TEXT_SWITCH) == 0){
				sText = paramArrayOfString[i + 1].trim();
				continue;
			}
			if (paramArrayOfString[i].compareToIgnoreCase("-" + INLINE_TEXT_FILE_SWITCH) == 0){
				sInlineTextFile = paramArrayOfString[i + 1].trim();
				continue;
			}
			
			if (paramArrayOfString[i].compareToIgnoreCase("-" + ATTACHED_FILE_SWITCH) == 0){
				sAttachedFilePaths = paramArrayOfString[i + 1].trim();
				continue;
			}
		}
		
		ArrayList<String>arrFilesToAttach = null;

		if (sAttachedFilePaths.compareToIgnoreCase("") != 0){
			arrFilesToAttach = new ArrayList<String>(0);
			String sAttachedFileList[] = sAttachedFilePaths.split(COMMAND_LINE_VALUE_DELIMITER);
			for (int i = 0; i < sAttachedFileList.length; i++){
				arrFilesToAttach.add(sAttachedFileList[i]);
			}
		}
		
		//If the user chose to insert a file into the body text of the email, read that file now:
		if (sInlineTextFile.compareToIgnoreCase("") != 0){
			try {
				sText += "\n\n" + readInlineTextFile(sInlineTextFile);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		//Now send the email:
		try {
			sendEmail(
				sSMTPServerURL,
				sSMTPPort,
				sSendingUserName, 
				sSendingUserPassword, 
				sRecipients,
				sReplyToAddress,
				sSubject, 
			    sText,
			    arrFilesToAttach)
			;
		} catch (Exception e) {
			System.out.println("Failed to send - " + e.getMessage());
			return;
		}
		System.out.println("Email successfully sent.");
		return;
	}
	public static void printSyntax(){
		System.out.println("Command line usage: eMailer <options>");
		System.out.println("Options:");
		System.out.println("  -" + SMTP_SERVER_URL_SWITCH + " smtpserverurl");
		System.out.println("  -" + SMTP_SERVER_PORT_SWITCH + " smtpserverport (email will be sent on port " + SMTP_PORT_NUMBER + " by default.)");
		System.out.println("  -" + SENDING_USERNAME_SWITCH + " smtpusername");
		System.out.println("  -" + SENDING_USER_PASSWORD_SWITCH + " smtpuserpassword");
		System.out.println("  -" + REPLY_TO_ADDRESS_SWITCH + " replytoaddress");
		System.out.println("  -" + RECIPIENTS_SWITCH + " recipients (can be a comma-delimited string of recipient addresses - NO SPACES ALLOWED.)");
		System.out.println("  -" + SUBJECT_SWITCH + " subject");
		System.out.println("  -" + EMAIL_TEXT_SWITCH + " text");
		System.out.println("  -" + INLINE_TEXT_FILE_SWITCH + " text file to be included INLINE as email body.");
		System.out.println("  -" + ATTACHED_FILE_SWITCH + " pathtofiletoattach (can be a comma-delimited string of valid file paths - NO SPACES ALLOWED.)");
		System.out.println("  -" + PRINT_SYNTAX_HELP_SWITCH + " (display syntax help)");
		return;
	}
	
	public static void sendEmail(
    		String sSMTPServer,
    		String sPort,
            final String sSendingUserName, 
            final String sSendingUserPassword, 
            String sRecipients,
            String sReplyToAddress,
            String sSubject, 
            String sText,
            ArrayList<String>arrFilesToAttach)
                throws Exception {

		if (sPort.compareToIgnoreCase("") == 0){
			sPort = SMTP_PORT_NUMBER;
		}
		
		// sets SMTP server properties
        Properties properties;
		try {
			properties = new Properties();
			properties.put("mail.smtp.host", sSMTPServer);
			properties.put("mail.smtp.port", SMTP_PORT_NUMBER);
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.user", sSendingUserName);
			properties.put("mail.password", sSendingUserPassword);
		} catch (Exception e) {
			throw new Exception("Error [1542748835] - setting properties for email - " + e.getMessage());
		}
 
        // creates a new session with an authenticator
        Authenticator auth;
		try {
			auth = new Authenticator() {
			    public PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(sSendingUserName, sSendingUserPassword);
			    }
			};
		} catch (Exception e) {
			throw new Exception("Error [1542748836] - getting Authenticator in email function - " + e.getMessage());
		}
        Session session;
		try {
			session = Session.getInstance(properties, auth);
		} catch (Exception e) {
			throw new Exception("Error [1542748837] - getting session in email function - " + e.getMessage());
		}
 
        // creates a new e-mail message
        Message msg;
		try {
			msg = new MimeMessage(session);
 
			msg.setFrom(new InternetAddress(sSendingUserName));
			
			InternetAddress[] mailAddress_REPLY_TO = new InternetAddress[1];
			String m_sReplyToAddress = sReplyToAddress;
			if (m_sReplyToAddress.compareToIgnoreCase("") == 0){
				m_sReplyToAddress = sSendingUserName;
			}
			mailAddress_REPLY_TO[0] = new InternetAddress(m_sReplyToAddress);
			msg.setReplyTo(mailAddress_REPLY_TO);
			
			//InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			//If there is more than one address, it should be separated with commas:
			if (sRecipients.indexOf(',') > 0){ 
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sRecipients));
			}else{
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(sRecipients));
			}
			
			msg.setSubject(sSubject);
			msg.setSentDate(new Date());
 
			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			//messageBodyPart.setContent(sText, "text/html");
			messageBodyPart.setContent(sText, "text/plain");
 
			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			//Adds any attached files:
			if (
				(arrFilesToAttach != null) && (arrFilesToAttach.size() > 0)
			){
				for (int i = 0; i < arrFilesToAttach.size(); i++){
					String sFullFilePathName = arrFilesToAttach.get(i);
					
					//First confirm that we can READ the file:
					try {
						File file = new File(sFullFilePathName); 
						BufferedReader br = new BufferedReader(new FileReader(file)); 
						String sTestString; 
						while ((sTestString = br.readLine()) != null){
						    //Do nothing 
						}
						br.close();
					} catch (Exception e) {
						throw new Exception("Error [1542748840] - could not open file '" + sFullFilePathName + "' with READ permission - " + e.getMessage());
					} 
					
					String sFileName = sFullFilePathName.substring(sFullFilePathName.lastIndexOf(System.getProperty("file.separator")) + 1, sFullFilePathName.length());
					messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(sFullFilePathName);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(sFileName);
					multipart.addBodyPart(messageBodyPart);
				}
			}
			
			msg.setContent(multipart);
		} catch (Exception e) {
			throw new Exception("Error [1542748838] - preparing message in EmailInlineHTML - " + e.getMessage());
		}
        try {
			Transport.send(msg);
		} catch (Exception e) {
			throw new Exception("Error [1542748839] - sending message in EmailInlineHTML - " + e.getMessage());
		}
        return;
    }
	private static String readInlineTextFile(String sFullPathName) throws Exception{
		
		String s = "";
		try {
			File file = new File(sFullPathName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				s += line + "\n";
			}
			bufferedReader.close();
		} catch (Exception e) {
			throw new Exception("Error [1544740437] reading specified inline text file '" + sFullPathName + "' - " + e.getMessage());
		}
		return s;
	}
}