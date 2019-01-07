package ServletUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import javax.servlet.ServletContext;

public class clsEmailInlineHTML {
	private static final String SMTP_PORT_NUMBER = "587";
	private static final String SIGNATURE_IMAGE_FILE_PREFIX = "SIG";
	public static final String NAME_OF_SIGNATURE_IMAGE = "signatureimage";
	public static final String NAME_OF_LOGO_IMAGE = "Logoimage";
	
	public static void sendEmailWithEmbeddedHTML(
    		String host,
            final String userName, 
            final String password, 
            String toAddress,
            String replyToAddress,
            String subject, 
            String htmlBody,
            Map<String, String> mapInlineImages,
            ArrayList<String>arrFilesToAttach)
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
			throw new Exception("Error [1395084035] - getting Authenticator in EmailInlineHTML - " + e.getMessage());
		}
        Session session;
		try {
			session = Session.getInstance(properties, auth);
		} catch (Exception e) {
			throw new Exception("Error [1395084036] - getting session in EmailInlineHTML - " + e.getMessage());
		}
 
        // creates a new e-mail message
        Message msg;
		try {
			msg = new MimeMessage(session);
 
			msg.setFrom(new InternetAddress(userName));
			
			InternetAddress[] mailAddress_REPLY_TO = new InternetAddress[1];
			mailAddress_REPLY_TO[0] = new InternetAddress(replyToAddress);
			msg.setReplyTo(mailAddress_REPLY_TO);
			
			//InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			//If there is more than one address, it should be separated with commas:
			if (toAddress.indexOf(',') > 0){ 
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			}else{
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
			}
			
			msg.setSubject(subject);
			msg.setSentDate(new Date());
 
			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html");
 
			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
 
			// adds inline image attachments
			if (mapInlineImages != null && mapInlineImages.size() > 0) {
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
			
			//Adds any attached files:
			if (arrFilesToAttach != null){
				for (int i = 0; i < arrFilesToAttach.size(); i++){
					String sFullFilePathName = arrFilesToAttach.get(i);
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
			throw new Exception("Error [1395084037] - preparing message in EmailInlineHTML - " + e.getMessage());
		}
        try {
			Transport.send(msg);
		} catch (Exception e) {
			throw new Exception("Error [1395084038] - sending message in EmailInlineHTML - " + e.getMessage());
		}
    }
    public static void emailEmbeddedHTMLWithSignature(
    		String sProgramRootPath,
    		String jsonString,
    		int iImageWidth,
    		int iImageHeight,
    		int iStrokeWidth,
    		int iRColor,
    		int iGColor,
    		int iBColor,
    		String sHost,
            final String sEmailUser,
            final String sPassword, 
            String sToAddress,
            String sReplyToAddress,
            String sSubject, 
            String sHTMLBody,
            ServletContext context
    	) throws Exception{
    	
    	//System.out.println("file.separator = " + System.getProperty("file.separator"));
    	String sFullSignatureFileName = sProgramRootPath
    		+ "images"
			+ System.getProperty("file.separator") 
    		+ SIGNATURE_IMAGE_FILE_PREFIX 
    		+ Long.toString(System.currentTimeMillis())
    		+ ".png"
    	;
    	//First, write the signature to an image file:
    	try {
			clsBase64Functions.writeJSONToPNGFile(
				jsonString,
				iImageWidth,
				iImageHeight,
				iStrokeWidth,
				iRColor,
				iGColor,
				iBColor,
				sFullSignatureFileName
			);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    	//String sFullOHDLogoFileName = sProgramRootPath
		//			        		+ "images"
		//			    			+ System.getProperty("file.separator") + "OHDLogo.gif";
    	//Here we read the image information and insert it into the body:
    	Map<String, String> inlineImages = new HashMap<String, String>();
    	inlineImages.put(NAME_OF_SIGNATURE_IMAGE, sFullSignatureFileName);
    	//TODO
    	//inlineImages.put(NAME_OF_OHD_LOGO_IMAGE, sFullOHDLogoFileName);
    	try {
			sendEmailWithEmbeddedHTML(sHost, sEmailUser, sPassword, sToAddress, sReplyToAddress, sSubject, sHTMLBody, inlineImages, null);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    	
    	//Remove the image file from disk:
    	try {
			deleteCurrentSignatureFile(sFullSignatureFileName);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
    }
	private static void deleteCurrentSignatureFile(String sFullImageFileName) throws Exception{
		File n = new File(sFullImageFileName);
		if (!n.delete()){
			throw new Exception("Error [1395084039] - Unable to delete " + sFullImageFileName + ".");
		}
	}

}