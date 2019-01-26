package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableapoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class APConvertACCPACAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APConvertACCPACData))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		        + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    /**************Get Parameters**************/

    	//Customized title
    	String sTitle = "Convert ACCPAC Accounts Payable Data to SMCP";
    	out.println(SMUtilities.SMCPTitleSubBGColor(
			sTitle, 
			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME), 
			SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
			SMUtilities.DEFAULT_FONT_FAMILY, 
			CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString(),
			false,
			"")
		);

    	//If the user didn't CONFIRM, then we can just drop out here:
    	if (request.getParameter(APConvertACCPAC.CONFIRM_CONVERSION_CHECKBOX_NAME) == null){
    		sWarning = "You did not check the checkbox to CONFIRM.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	
    	//Get the necessary connections:
        String sACCPACDatabaseURL = "";
        String sACCPACDatabasename = "";
        String sACCPACDatabaseuser = "";
        String sACCPACDatabasepw = "";
        int iACCPACDatabaseType = 0;
        int iACCPACAPVersion = 0;
        
        String SQL = "SELECT * FROM " + SMTableapoptions.TableName;
        try {
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".doGet - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rsOptions.first()){
				if (rsOptions.getInt(SMTableapoptions.iusessmcpap) == 1){
					rsOptions.close();
					sWarning = "You cannot convert the ACCPAC data into SMCP once you've begun using SMCP AP.";
		    		response.sendRedirect(
		    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
		    				+ "&" + "Warning=" + sWarning
		    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		);			
		        	return;
				}
			    sACCPACDatabaseURL = rsOptions.getString(SMTableapoptions.saccpacdatabaseurl);
			    sACCPACDatabasename = rsOptions.getString(SMTableapoptions.saccpacdatabasename);
			    sACCPACDatabaseuser = rsOptions.getString(SMTableapoptions.saccpacdatabaseuser);
			    sACCPACDatabasepw = rsOptions.getString(SMTableapoptions.saccpacdatabaseuserpw);
			    iACCPACDatabaseType = rsOptions.getInt(SMTableapoptions.iaccpacdatabasetype);
			    iACCPACAPVersion = rsOptions.getInt(SMTableapoptions.iaccpacversion);
			}else{
				rsOptions.close();
		   		sWarning = "Unable to open AP Options table - function cannot run.";
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
	    				+ "&" + "Warning=" + sWarning
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		);			
	        	return;
			}
		} catch (SQLException e) {
			//Redirect back to calling class:
	   		sWarning = "Unable to read AP Options table - " + e.getMessage();
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
        Connection cnSMCP;
 		try {
 			cnSMCP = clsDatabaseFunctions.getConnectionWithException(
 				getServletContext(), 
 				sDBID, 
 				"MySQL", 
 				SMUtilities.getFullClassName(this.toString()) + ".doGet - user: " + sUserFullName);
 		} catch (Exception e) {
 	   		sWarning = "Unable to get SMCP connection - " + e.getMessage();
     		response.sendRedirect(
     				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
     				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
     				+ "&" + "Warning=" + sWarning
     				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
     		);			
         	return;
 		}
        Connection cnACCPAC;
		try {
			cnACCPAC = getACCPACConnection(
					iACCPACDatabaseType,
					sACCPACDatabaseURL,
					sACCPACDatabasename,
					sACCPACDatabaseuser,
					sACCPACDatabasepw);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[]1547047443");
	   		sWarning = "Unable to get ACCPAC connection - " + e.getMessage();
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	//Now decide which function they've chosen, and process it:
    	String sFunctionValue = clsManageRequestParameters.get_Request_Parameter(APConvertACCPAC.RADIO_FIELD_NAME, request);
    	int iFunctionValue = 0;
    	try {
			iFunctionValue = Integer.parseInt(sFunctionValue);
		} catch (NumberFormatException e2) {
    		sWarning = "Invalid function value '" + sFunctionValue + "'.";
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[]1547047444");
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[]1547047445");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + APConvertACCPAC.ROLLBACK_OPTION_VALUE
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	String sProcessingResult = "";
    	long lStartingTime = System.currentTimeMillis();
    	try {
    		sProcessingResult = processSelectedFunction(
    			iFunctionValue, 
    			iACCPACDatabaseType, 
    			iACCPACAPVersion, 
    			sUserName, 
    			cnSMCP, 
    			cnACCPAC
    		);
		} catch (Exception e2) {
    		sWarning = e2.getMessage() + ".";
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[]1547047446");
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[]1547047447");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + APConvertACCPAC.ROLLBACK_OPTION_VALUE
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}

    	//If the function returns successfully, then we can update the next function value:
    	sProcessingResult += "<BR><I> - Time elapsed " + Long.toString((System.currentTimeMillis() - lStartingTime)/1000L) 
    		+ " seconds.</I><BR>";
    	
    	String sNextFunctionValue = "";
    	if (iFunctionValue == APConvertACCPAC.LAST_FUNCTION_IN_SEQUENCE){
    		sNextFunctionValue = Integer.toString(APConvertACCPAC.ROLLBACK_OPTION_VALUE);
    	}else{
    		sNextFunctionValue = Integer.toString(iFunctionValue + 1);
    	}
		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[]1547047448");
		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[]1547047449");
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ APConvertACCPAC.RADIO_FIELD_NAME + "=" + sNextFunctionValue
				+ "&" + "Status=" + "Function was successfully processed.<BR>"
					+ sProcessingResult
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	private String processSelectedFunction(
		int iSelectedFunction, 
		int iACCPACDatabaseType, 
		int iACCPACAPVersion,
		String sUser,
		Connection cnSMCP, 
		Connection cnACCPAC
		) throws Exception{
		
		APACCPACConversion conv = new APACCPACConversion();
		
    	switch(iSelectedFunction){
    	case APConvertACCPAC.ROLLBACK_OPTION_VALUE:
    		return conv.reverseDataChanges(cnSMCP, false);
    	case APConvertACCPAC.PROCESS_MASTER_TABLES_OPTION_VALUE:
    		return conv.processMasterTables(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case APConvertACCPAC.PROCESS_VENDOR_TABLES_OPTION_VALUE:
    		return conv.processVendorTables(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case APConvertACCPAC.PROCESS_VENDOR_STATISTICS_OPTION_VALUE:
    		return conv.processVendorStatistics(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case APConvertACCPAC.PROCESS_VENDOR_TRANSACTIONS_OPTION_VALUE:
    		return conv.processAPTransactions(cnSMCP, cnACCPAC, iACCPACDatabaseType, iACCPACAPVersion);
    	case APConvertACCPAC.PROCESS_VENDOR_TRANSACTION_LINES_INSERT_OPTION_VALUE:
    		return conv.processAPTransactionLines(cnSMCP, cnACCPAC, iACCPACDatabaseType);
    	case APConvertACCPAC.PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_OPTION_VALUE:
    		return conv.processUpdatingAPTransactionLines(cnSMCP);
    	case APConvertACCPAC.PROCESS_INSERT_VENDOR_MATCHING_LINES_OPTION_VALUE:
    		return conv.processAPMatchingLines(cnSMCP, cnACCPAC, iACCPACDatabaseType);
    	case APConvertACCPAC.PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_OPTION_VALUE:
    		return conv.processUpdatingApplyFromAPMatchingLines(cnSMCP);
    	case APConvertACCPAC.PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_OPTION_VALUE:
    		return conv.processUpdatingApplyToAPMatchingLines(cnSMCP);
    	case APConvertACCPAC.PROCESS_UPDATE_VENDOR_ADDRESSES_OPTION_VALUE:
    		return conv.processUpdatingVendorAddresses(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	default:
    		throw new Exception("Function value " + iSelectedFunction + " is not valid");
    	}
	}
	private Connection getACCPACConnection(
			int iACCPACDatabaseType,
			String sACCPACDatabaseURL,
			String sACCPACDatabaseName,
			String sACCPACUserName,
			String sACCPACPassword
			) throws Exception{
		Connection cnACCPAC = null;
		
		//If we're reading a Pervasive DB:
		if (iACCPACDatabaseType == SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE){
		//Pervasive connection
			try
				{
				cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sACCPACDatabaseURL + ":1583/" + sACCPACDatabaseName + "", sACCPACUserName, sACCPACPassword);
			}catch (Exception localException2) {
				try {
					Class.forName("com.pervasive.jdbc.v2.Driver").newInstance();
					cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sACCPACDatabaseURL + ":1583/" + sACCPACDatabaseName + "", sACCPACUserName, sACCPACPassword);
				} catch (InstantiationException e) {
					throw new Exception("InstantiationException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new Exception("IllegalAccessException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (ClassNotFoundException e) {
					throw new Exception("ClassNotFoundException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (SQLException e) {
					throw new Exception("SQLException getting ACCPAC Pervasive connection - " + e.getMessage());
				}
			}
			
			if (cnACCPAC == null){
				throw new Exception("Could not get Pervasive connection");
			}
		//If we're reading an MS SQL DB:
		}else{
			try
			{
				cnACCPAC = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sACCPACDatabaseURL + ":1433;DatabaseName=" + sACCPACDatabaseName, sACCPACUserName, sACCPACPassword);
			}
			catch (Exception localException2) {
				try {
					//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
					//Class.forName("com.microsoft.jdbc.sqlserver.sqlserverdriver").newInstance();
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
					//cnAP = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
					cnACCPAC = DriverManager.getConnection("jdbc:sqlserver://" + sACCPACDatabaseURL + ":1433;DatabaseName=" + sACCPACDatabaseName, sACCPACUserName, sACCPACPassword);
					//String Url = "jdbc:sqlserver://localhost:1433;databaseName=movies";
			        //Connection connection = DriverManager.getConnection(Url,"sa", "xxxxxxx);
				} catch (InstantiationException e) {
					throw new Exception("InstantiationException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new Exception("IllegalAccessException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (ClassNotFoundException e) {
					throw new Exception("ClassNotFoundException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (SQLException e) {
					throw new Exception("SQLException getting ACCPAC MS SQL connection - " + e.getMessage());
				}
			}
			if (cnACCPAC == null){
				throw new Exception("Could not get MS SQL connection");
			}
		}
		return cnACCPAC;
	}
}