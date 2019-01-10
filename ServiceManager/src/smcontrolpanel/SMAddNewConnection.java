package smcontrolpanel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;

@SuppressWarnings("serial")
public class SMAddNewConnection extends HttpServlet{

	private String sDBID = "";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    response.setContentType("text/html");
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	    
	    if (Integer.parseInt(request.getParameter("BUSY")) == 1){
	    	clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "test - " + USDateTimeformatter.format(new Date(System.currentTimeMillis())));
	    }else{
	    	int iConnectionNumber = clsDatabaseFunctions.getAvailableConnectionNumber(getServletContext());
	    	
	    	//Create an array list of connections:
	    	ArrayList<Connection> alConnections = new ArrayList<Connection>(0);
	    	//Next, artificially TIE UP all of the existing connections by 'getting' them in the new list:
	    	for (int i=0;i<iConnectionNumber;i++){
	    		alConnections.add(clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "test - " + USDateTimeformatter.format(new Date(System.currentTimeMillis()))));
	    	}
	    	//create a new connection now - because all the existing connections are 'tied up', this will force the program to open a new connection:
	    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "test - " + USDateTimeformatter.format(new Date(System.currentTimeMillis())));
	    	
	    	try{
	    		//now free all connections - this leaves us with a new list of 'free' connections, including the one we just created:
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080401]");
	    		while (!alConnections.isEmpty()){
	    			clsDatabaseFunctions.freeConnection(getServletContext(), (Connection) alConnections.get(0), "[1547080400]");
	    			alConnections.remove(0);
	    		}
	    	}catch (Exception ex){
	    		System.out.println("Error: " + ex.getMessage());
	    	}
	    }
	    //redirect
		String sRedirectString = 
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMCheckConnectionList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;

		try {
			response.sendRedirect(sRedirectString);
		} catch (IOException e) {
			System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString
			);
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}