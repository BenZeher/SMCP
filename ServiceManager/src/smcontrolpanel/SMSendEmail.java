package smcontrolpanel;

import javax.mail.*;
import javax.mail.internet.*;

import java.util.ArrayList;
import java.util.Properties;

public class SMSendEmail {
    
	public static boolean SendMail(String sHostName, 
								   //int iPort, 
								   String sMailFrom, 
								   ArrayList<String> sMailTo, 
								   String sSubject,
								   //String sYourServerName,
								   String sMessage){
		
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", sHostName);
		//props.setProperty("mail.user", "li");
		//props.setProperty("mail.password", "TScb15^%!");
		
		try{
			//System.out.println("1");
			Session mailSession = Session.getDefaultInstance(props, null);
			//System.out.println("2");
			Transport transport = mailSession.getTransport();
			//System.out.println("3");
			
			MimeMessage message = new MimeMessage(mailSession);
			//System.out.println("4");
			message.setSubject(sSubject);
			message.setSender(new InternetAddress(sMailFrom));
			//System.out.println("5");
			message.setContent(sMessage, "text/html");
			//System.out.println("6");
			for (int i=0;i<sMailTo.size();i++){
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(sMailTo.get(i).toString()));
				//System.out.println("7 - " + i);
			}
			transport.connect();
			//System.out.println("8");
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			//System.out.println("9");
			transport.close();
			return true;
		}catch (Exception ex){
			System.out.println("Exception: " + ex.getMessage());
			return false;
		}
    }
}
