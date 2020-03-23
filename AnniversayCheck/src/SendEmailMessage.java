import java.security.Security;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

 
public class SendEmailMessage{
	/*
	public static boolean SendMail(String paramString1, int paramInt, String paramString2, ArrayList<String> paramArrayList, String paramString3, String paramString4, String paramString5)
	   {
	     Socket localSocket = null;
	     DataOutputStream localDataOutputStream = null;
	     DataInputStream localDataInputStream = null;
	 
	     Date localDate = new Date(System.currentTimeMillis());
	     DateFormat localDateFormat = DateFormat.getDateInstance(0, Locale.US);
	     try
	     {
	       localSocket = new Socket(paramString1, paramInt);
	       localDataOutputStream = new DataOutputStream(localSocket.getOutputStream());
	       localDataInputStream = new DataInputStream(localSocket.getInputStream());
	 
	       if ((localSocket != null) && (localDataOutputStream != null) && (localDataInputStream != null)) {
	         try
	         {
	           localDataOutputStream.writeBytes("HELO " + paramString4 + "\r\n");
	 
	           localDataOutputStream.writeBytes("MAIL From: " + paramString2 + "\r\n");
	 
	           for (int i = 0; i < paramArrayList.size(); ++i) {
	             System.out.println("RCPT TO: " + paramArrayList.get(i).toString());
	             localDataOutputStream.writeBytes("RCPT To: " + paramArrayList.get(i).toString() + "\r\n");
	           }
	 
	           localDataOutputStream.writeBytes("DATA\r\n");
	 
	           localDataOutputStream.writeBytes("X-Mailer: Via Java\r\n");
	           localDataOutputStream.writeBytes("DATE: " + localDateFormat.format(localDate) + "\r\n");
	           localDataOutputStream.writeBytes("From: " + paramString2 + "\r\n");
	           localDataOutputStream.writeBytes("To: " + paramArrayList + "\r\n");
	 
	           localDataOutputStream.writeBytes("Subject: " + paramString3 + "\r\n");
	           localDataOutputStream.writeBytes(paramString5 + "\r\n");
	           localDataOutputStream.writeBytes("\r\n.\r\n");
	           localDataOutputStream.writeBytes("QUIT\r\n");
	           String str;
	           do
	           {
	             if ((str = localDataInputStream.readLine()) == null) break;
	             System.out.println("RESPONSELINE:" + str); }
	           while (str.indexOf("Ok") == -1);
	         }
	         catch (Exception localException1)
	         {
	           System.out.println("Cannot send email as an error occurred.");
	           return false;
	         }
	       }
	 
	     }
	     catch (Exception localException2)
	     {
	       System.out.println("Host " + paramString1 + "unknown");
	       return false;
	     }
	 
	     return true;
	}
	*/

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
			"aynot23yllom4",
			"465",
			"Subject",
			"This is a test message",
			"test@guess.com",
			"tom_ronayne@odcdc.com,tjprona@gmail.com");
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
		message.setSender(new InternetAddress(sender));
		message.setSubject(subject);
		if (bUseHTML){
			message.setContent(body, "text/html");
		}else{
			message.setContent(body, "text/plain");
		}
		if (recipients.indexOf(',') > 0) 
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
		else
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
		
		Transport.send(message);
	}
}

/* Location:           C:\Users\Li Tong\workspace\AnniversayCheck\src\AnniversaryCheck.jar
 * Qualified Name:     SendEmailMessage
 * Java Class Version: 5 (49.0)
 * JD-Core Version:    0.5.3
 */