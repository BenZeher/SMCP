package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class CustomerCallLogSave extends HttpServlet {
	//OBSOLETE?
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomerCallLogEntryForm))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Customer Call Log";
	    String subtitle = "Saving....";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    //SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	    Timestamp tsCallTime = new Timestamp(System.currentTimeMillis());

	    String sCustomerName = request.getParameter("CustomerName");
	    String sCustomerPhone = request.getParameter("PhoneAreaCode") + 
	    						request.getParameter("PhoneSwitch") + 
	    						request.getParameter("PhoneEndUnit") + 
	    						request.getParameter("PhoneExtension");
	    String sCityState = request.getParameter("CityState");
	    int iSourceCode = Integer.parseInt(request.getParameter("OrderSource"));
	    String sCallNote = request.getParameter("CallNote");
	    
	    //System.out.println("USENOW = " + request.getParameter("USENOW"));
	    //System.out.println("USECUSTOM = " + request.getParameter("USECUSTOM"));
	    
	    if (request.getParameter("USENOW") == null){
	    	if (request.getParameter("USECUSTOM") == null){
	    		//nothing checked, don't do anything.
	    		out.println("<FONT COLOR=RED SIZE=4><B>You have to set customer called time in order to create a log entry.</B></FONT><BR><BR><BR>");
	    		out.println("Use the \"<B>BACK</B>\" button of your brower to go back and try to save again.");
	    	}else{
	    		String sStartDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request)
				+ " "
				+ Integer.toString(clsDateAndTimeConversions.NormalToMilitary(
					   Integer.parseInt(request.getParameter("SelectedHour")), 
					   Integer.parseInt(request.getParameter("SelectedAMPM"))))
				+ ":"
				+ request.getParameter("SelectedMinute");
				java.sql.Date sqldatStartDate;
				try{
					sqldatStartDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy H:m", sStartDate);
				}catch (ParseException pse){
					out.println("Invalid start date format - '" + sStartDate + "'");
					return;
				}
				tsCallTime = new Timestamp(sqldatStartDate.getTime());
	    		out.println(Log_Customer_Call(sCustomerName,
											  sCustomerPhone,
											  sCityState,
											  iSourceCode,
											  sCallNote,
											  tsCallTime,
											  CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME).toString(),
											  new Timestamp(System.currentTimeMillis()),
											  sDBID));
	    	}
	    }else{
	    	if (request.getParameter("USECUSTOM") == null){
	    		//use current time
	    		out.println(Log_Customer_Call(sCustomerName,
											  sCustomerPhone,
											  sCityState,
											  iSourceCode,
											  sCallNote,
											  tsCallTime,
											  CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME).toString(),
											  new Timestamp(System.currentTimeMillis()),
											  sDBID));
	    	}else{
	    		//both are checked, display warning message
	    		out.println("<FONT COLOR=RED SIZE=4><B>You chose to use both <FONT SIZE=5>\"current time\"</FONT> and <FONT SIZE=5>\"custom time\"</FONT> which is not allowed.</B></FONT> <BR>" +
	    					"To create a log entry, please go back to uncheck one time option.<BR><BR>");
	    		out.println("Use the \"<B>BACK</B>\" button of your browser to go back and try to save again.");
	    	}
	    }
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	
	private String Log_Customer_Call(String sCustomerName,
									 String sCustomerPhone,
									 String sCustomerCity,
									 int iOrderSource,
									 String sCallNote,
									 Timestamp tsCallTime,
									 String sUserName,
									 Timestamp tsLogTime,
									 String sDBID){
	    
		if (iOrderSource == 0){
			return "<FONT COLOR=RED SIZE=4><B>You have to pick a order source before saving the log.</B></FONT><BR><BR>" + 
				   "Use the \"<B>BACK</B>\" button of your browser to go back and try to save again.";
		}else if (sCustomerName.trim().length() == 0){
			return "<FONT COLOR=RED SIZE=4><B>You have to enter a customer name before saving the log.</B></FONT><BR><BR>" + 
			       "Use the \"<B>BACK</B>\" button of your browser to go back and try to save again.";
		}else if (sCustomerPhone.trim().length() == 0){
			return "<FONT COLOR=RED SIZE=4><B>You have to enter a customer phone number before saving the log.</B></FONT><BR><BR>" + 
		       	   "Use the \"<B>BACK</B>\" button of your browser to go back and try to save again.";
		}else{
		    String sSQL = SMMySQLs.Get_Insert_Customer_Call_Log_SQL(sUserName, 
		    														(new Timestamp(System.currentTimeMillis())).toString(), 
		    														clsDatabaseFunctions.FormatSQLStatement(sCustomerName), 
		    														clsDatabaseFunctions.FormatSQLStatement(sCustomerPhone), 
		    														clsDatabaseFunctions.FormatSQLStatement(sCustomerCity), 
		    														tsCallTime.toString(), 
		    														iOrderSource,
		    														clsDatabaseFunctions.FormatSQLStatement(sCallNote));
		    try{
			    if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID)){
			    	
			    	return "Customer call log saved successfully.<BR>" + 
			    		   "<META http-equiv='Refresh' content='1;URL=" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.CustomerCallLogEntry?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "'>";
			    	
			    }else{
			    	//System.out.println("Failed saving customer call log.");
			    	return "Saving customer call log failed.<BR>" + 
			    		   "<FONT COLOR=RED SIZE=4><B>Error occurred whensaving the call log.</B><BR><BR>" +
			    		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.CustomerCallLogEntry?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Click here to return to call log entry form.</A>";
			    }
		    }catch (SQLException ex){
		    	//catch SQL exceptions
		    	System.out.println("Error when saving customer call log.");
		    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
		    	System.out.println("SQL: " + ex.getSQLState());
		    	return "<FONT COLOR=RED SIZE=4><B>Error occurred whensaving the call log.</B><BR><BR>" + 
		    		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.CustomerCallLogEntry?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Click here to return to call log entry form.</A>";
		    }
		}
	}
}