package ServletUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;

public class clsFTPFunctions {

	public static void putFile(
			String sFTPUrl, 
			String sUser, 
			String sPw, 
			String sSourceFile, 
			String sTargetFile,
			int iFileType) throws Exception{
	    FTPClient con = null;
	
	    try
	    {
	        con = new FTPClient();
	        //If the address contains a port, then call the connect function differently:
	        if (sFTPUrl.contains(":")){
	        	String sPort = sFTPUrl.substring(sFTPUrl.indexOf(":") + 1);
	        	String sURL = sFTPUrl.substring(0, sFTPUrl.indexOf(":"));
	        	con.connect(sURL, Integer.parseInt(sPort));
	        }else{
	        	con.connect(sFTPUrl);
	        }
	        
	        if (con.login(sUser, sPw))
	        {
	            con.enterLocalPassiveMode(); // important!
	            con.setFileType(iFileType);
	            //String data = "/home/tom/Desktop/AR Aging.png";
	            String data = sSourceFile;
	
	            FileInputStream in = new FileInputStream(new File(data));
	            //boolean result = con.storeFile("/AIROTECH/AR Aging.png", in);
	            boolean result = con.storeFile(sTargetFile, in);
	            in.close();
	            con.logout();
	            con.disconnect();
	            if (!result){
	            	throw new Exception("Unable to send file using FTP.");
	            }
	        }
	    }
	    catch (Exception e)
	    {
	    	throw new Exception("Error sending file - " + e.getMessage());
	    }
	}
	
	public static void getFile(
			String sFTPUrl, 
			String sUser, 
			String sPw, 
			String sSourceFile, 
			String sTargetFile,
			int iFileType) throws Exception{
	    FTPClient con = null;

	    try
	    {
	        con = new FTPClient();
	        con.connect(sFTPUrl);

	        if (con.login(sUser, sPw))
	        {
	            con.enterLocalPassiveMode(); // important!
	            con.setFileType(iFileType);
	            //String data = "/sdcard/vivekm4a.m4a";

	            OutputStream out = new FileOutputStream(new File(sTargetFile));
	            boolean result = con.retrieveFile(sSourceFile, out);
	            out.close();
	            con.logout();
	            con.disconnect();
	            if (!result) {
	            	throw new Exception("Unable to receive file using FTP.");
	            }
	        }
	    }
	    catch (Exception e)
	    {
	    	throw new Exception("Error receiving file - " + e.getMessage());
	    }

	}
}
