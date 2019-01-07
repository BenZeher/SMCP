package SecuritySystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import sscommon.SSConstants;

public class ListeningService implements Runnable {
	BufferedReader is;
	PrintStream os;
	Socket clientSocket;
	ServerSocket sock;
	boolean bStopListening = false;
	boolean bDiagnosticOutputIsOn = false;
	public ListeningService(int port, boolean bOutputIsOn) {
		bDiagnosticOutputIsOn = bOutputIsOn;
		try {
			sock = new ServerSocket(port);
		}
		catch (IOException e) {
			String sError = "Error [1458178962] getting socket - " + e.getMessage();
			SecuritySystem.writeControllerLogEntry(
					"Error [1458178962] getting socket - " + e.getMessage() + ".",
					1);
			if (bDiagnosticOutputIsOn){
				System.out.println(sError);
			}
			SecuritySystem.listeningServerExceptionHandler(sError);
		}  
	}
	
	public void run() {
		while (!bStopListening){
			clientSocket = null;
			try {
				clientSocket = sock.accept();
			} catch (IOException e1) {
				String sError = "Error [1458178964] setting socket to accept connections - " + e1.getMessage();
				SecuritySystem.writeControllerLogEntry(
						"Error [1458178964] setting socket to accept connections - " + e1.getMessage() + ".",
						1);

				if (bDiagnosticOutputIsOn){
					System.out.println(sError);
				}
				bStopListening = true;
				SecuritySystem.listeningServerExceptionHandler(sError);
			}
			//this.clientSocket = tempsocket;
			SecuritySystem.writeControllerLogEntry(
					"Connection established with: " + clientSocket + ".",
					3);

			if (bDiagnosticOutputIsOn){
				System.out.println( "Connection established with: " + clientSocket );
			}
			try {
				is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				os = new PrintStream(clientSocket.getOutputStream());
			} catch (IOException e) {
				String sError = "Error [1458178965] reading IO from socket - " + e.getMessage();
				SecuritySystem.writeControllerLogEntry(
					"Error [1458178965] reading IO from socket - " + e.getMessage() + ".",
					1);

				if (bDiagnosticOutputIsOn){
					System.out.println(sError);
				}
				SecuritySystem.listeningServerExceptionHandler(sError);
			}
			String sRequestString;
			try {
				sRequestString = is.readLine();
				SecuritySystem.writeControllerLogEntry(
					"Received this request through listening socket: '" + sRequestString + "'.",
					3);

				if (bDiagnosticOutputIsOn){
					System.out.println( "Received this request through listening socket: '" + sRequestString + "'.");
				}
				String sResponseString = "";
				try {
					sResponseString = ControllerResponses.processInputFromServer(sRequestString);
				} catch (Exception e) {
					//Respond to the client that the request failed to process
					//and include the error information/code:
					sResponseString =
						SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL
						+ SSConstants.QUERY_STRING_DELIMITER
						+ e.getMessage()
					;
				}
				if (bDiagnosticOutputIsOn){
					System.out.println( "Response to request was: '" + sResponseString + "'.");
				}
				SecuritySystem.writeControllerLogEntry(
					"Response to request was: '" + sResponseString + "'.",
					3);
				
				//Send an acknowledgment back:
				os.println(sResponseString);
				
				//Close the streams:
				is.close();
				os.close();
				clientSocket.close();
				SecuritySystem.writeControllerLogEntry(
					"Connection closed.",
					3);

				if (bDiagnosticOutputIsOn){
					System.out.println( "Connection closed." );
				}
			} catch (IOException e) {
				String sError = "Error [1458178966] reading command line from socket input stream - " + e.getMessage();
				SecuritySystem.writeControllerLogEntry(
					"Error [1458178966] reading command line from socket input stream - " + e.getMessage() + ".",
					1);

				if (bDiagnosticOutputIsOn){
					System.out.println(sError);
				}
				bStopListening = true;
				SecuritySystem.listeningServerExceptionHandler(sError);
			}
		}
	}
	public void stopListening() throws Exception{
		bStopListening = true;
		sock.close();
	}
}
