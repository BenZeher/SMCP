package smas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import ServletUtilities.clsServletUtilities;
import sscommon.SSConstants;
import sscommon.SSUtilities;

/**
 http://alvinalexander.com/blog/post/java/simple-java-socket-client-class-program
 * A complete Java class that demonstrates how to use the Socket
 * class, specifically how to open a socket, write to the socket,
 * and read from the socket.
 *
 * @author alvin alexander, devdaily.com.
 *
 */

public class ASClientService
{
	//If this is set to TRUE, then the program won't send any actual commands to the controllers, 
	//but will, instead, output the commands to System.out:
	private boolean bFakeSendingCommands = false;
	// call our constructor to start the program
	//public static void main(String[] args)
	//{
	//  new ClientService();
	//}
	public ASClientService()
	{
		//Constructor stuff here:
	}

	//This sends a request to the web app and returns true if it gets an OK back, false if not:
	public String sendRequest(
			String sURL, 
			int iPort, 
			String sRequestString,
			boolean bOutputDiagnostics) throws Exception{
		
    	if (bOutputDiagnostics){
    		System.out.println("[1463575924] - sending request to client service:\n" 
    		+ "URL = '" + sURL + "'\n"
    		+ "Port = '" + Integer.toString(iPort) + "'\n"
    		+ " Request String = " + sRequestString);
    	}
		
    	if (bFakeSendingCommands){
    		clsServletUtilities.sysprint(this.toString(), "TEST", "[1493912839] sRequestString = '" + sRequestString + "'");
    		return "ACK=OK";
    	}
    	
		String sResult = "";
		try{
			// open a socket
			Socket socket = openSocket(sURL, iPort);

			// write-to, and read-from the socket.
			sResult = writeToAndReadFromSocket(socket, sRequestString, bOutputDiagnostics);

			// print out the result we got back from the server
			if (bOutputDiagnostics){
				System.out.println("Result from writing client socket: " + sResult);
			}

			// close the socket, and we're done
			socket.close();
		}
		catch (Exception e)
		{
			throw new Exception("Error [1463575925] sending request to web app from ClientService, "
				+ "sRequestString = '" + sRequestString + "', sResult = '" + sResult + "' - " + e.getMessage());
		}
		//If we got an acknowledgment, then the server understood our request and we return the string:
		String sAcknowledgement = SSUtilities.getKeyValue(sResult, SSConstants.QUERY_KEY_ACKNOWLEDGMENT);
		if (sAcknowledgement.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL) == 0){
			return sResult;
		}else{
			throw new Exception(SSUtilities.getKeyValue(sResult, SSConstants.QUERY_KEY_REQUEST_ERROR));
		}
	}

	//Returns the response from the web app:
	private String writeToAndReadFromSocket(Socket socket, String writeTo, boolean bOutputDiagnostics) throws Exception
	{
    	if (bOutputDiagnostics){
    		System.out.println("[1463575926] - heading into write socket: " + writeTo);
    	}
		try
		{
			// write text to the socket
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bufferedWriter.write(writeTo + "\n");
			bufferedWriter.flush();
			// read text from the socket
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String str;
			while ((str = bufferedReader.readLine()) != null)
			{
				sb.append(str + "\n");
			}
			// close the reader, and return the results as a String
			bufferedReader.close();
			return sb.toString();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Open a socket connection to the given server on the given port.
	 * This method currently sets the socket timeout value to 10 seconds.
	 * (A second version of this method could allow the user to specify this timeout.)
	 */
	private Socket openSocket(String server, int port) throws Exception
	{
		Socket socket;

		// create a socket with a timeout
		try
		{
			InetAddress inteAddress = InetAddress.getByName(server);
			SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);

			// create a socket
			socket = new Socket();

			// this method will block no more than timeout ms.
			int timeoutInMs = SSConstants.CLIENT_SOCKET_MAX_TIME_TO_BLOCK_IN_SECONDS * 1000;   // 10 seconds
			socket.connect(socketAddress, timeoutInMs);

			return socket;
		} 
		catch (SocketTimeoutException ste) 
		{
			throw new Exception("Timed out waiting for socket - " + ste.getMessage());
		}
	}
}