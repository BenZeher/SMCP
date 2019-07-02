package smgl;

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

import SMDataDefinition.SMTablegloptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLConvertACCPACAction extends HttpServlet {

	public static final String SESSION_ATTRIBUTE_RESULT = "SESSIONATTRIBUTERESULT";
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
				SMSystemFunctions.GLConvertACCPACData))
		{
			return;
		}

		//System.out.println("[1523043654]");
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    CurrentSession.removeAttribute(SESSION_ATTRIBUTE_RESULT);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + 
	    				" " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME); 
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    /**************Get Parameters**************/

    	//Customized title
    	String sTitle = "Convert ACCPAC General Ledger Data to SMCP";
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
    	if (request.getParameter(GLConvertACCPAC.CONFIRM_CONVERSION_CHECKBOX_NAME) == null){
    		sWarning = "You did not check the checkbox to CONFIRM.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	//System.out.println("[1523043655]");
    	//Get the necessary connections:
        String sACCPACDatabaseURL = "";
        String sACCPACDatabasename = "";
        String sACCPACDatabaseuser = "";
        String sACCPACDatabasepw = "";
        int iACCPACDatabaseType = 0;
        
        String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
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
				if (rsOptions.getInt(SMTablegloptions.iusessmcpgl) == 1){
					rsOptions.close();
					sWarning = "You cannot convert the ACCPAC data into SMCP once you've begun using SMCP GL.";
		    		response.sendRedirect(
		    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
		    				+ "&" + "Warning=" + sWarning
		    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		);			
		        	return;
				}
			    sACCPACDatabaseURL = rsOptions.getString(SMTablegloptions.saccpacdatabaseurl);
			    sACCPACDatabasename = rsOptions.getString(SMTablegloptions.saccpacdatabasename);
			    sACCPACDatabaseuser = rsOptions.getString(SMTablegloptions.saccpacdatabaseuser);
			    sACCPACDatabasepw = rsOptions.getString(SMTablegloptions.saccpacdatabaseuserpw);
			    iACCPACDatabaseType = rsOptions.getInt(SMTablegloptions.iaccpacdatabasetype);
			}else{
				rsOptions.close();
		   		sWarning = "Unable to open GL Options table - function cannot run.";
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
	    				+ "&" + "Warning=" + sWarning
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		);			
	        	return;
			}
		} catch (SQLException e) {
			//Redirect back to calling class:
	   		sWarning = "Unable to read GL Options table - " + e.getMessage();
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
        
        //System.out.println("[1523043656]");
        Connection cnSMCP;
 		try {
 			cnSMCP = clsDatabaseFunctions.getConnectionWithException(
 				getServletContext(), 
 				sDBID, 
 				"MySQL", 
 				SMUtilities.getFullClassName(this.toString()) + ".doGet - user: " + sUserName);
 		} catch (Exception e) {
 	   		sWarning = "Unable to get SMCP connection - " + e.getMessage();
     		response.sendRedirect(
     				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
     				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
     				+ "&" + "Warning=" + sWarning
     				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
     		);			
         	return;
 		}
        Connection cnACCPAC;
        //System.out.println("[1523043657]");
		try {
			cnACCPAC = getACCPACConnection(
					iACCPACDatabaseType,
					sACCPACDatabaseURL,
					sACCPACDatabasename,
					sACCPACDatabaseuser,
					sACCPACDatabasepw);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[1547080728]");
	   		sWarning = "Unable to get ACCPAC connection - " + e.getMessage();
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request)
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		//System.out.println("[1523043658]");
    	//Now decide which function they've chosen, and process it:
    	String sFunctionValue = clsManageRequestParameters.get_Request_Parameter(GLConvertACCPAC.RADIO_FIELD_NAME, request);
    	int iFunctionValue = 0;
    	try {
			iFunctionValue = Integer.parseInt(sFunctionValue);
		} catch (NumberFormatException e2) {
    		sWarning = "Invalid function value '" + sFunctionValue + "'.";
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[1547080729]");
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[1547080730]");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + GLConvertACCPAC.ROLLBACK_OPTION_VALUE
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	//System.out.println("[1523043653] - iFunctionValue = " + iFunctionValue);
    	
    	String sProcessingResult = "";
    	long lStartingTime = System.currentTimeMillis();
    	//System.out.println("[1552318878] - going into processSelectedFunction");
    	try {
    		sProcessingResult = processSelectedFunction(
    			iFunctionValue, 
    			iACCPACDatabaseType, 
    			sUserName, 
    			sUserID, 
    			sUserFullName, 
    			cnSMCP, 
    			cnACCPAC,
    			sACCPACDatabasename);
		} catch (Exception e2) {
    		sWarning = e2.getMessage() + ".";
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[1547080731]");
    		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[1547080732]");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + GLConvertACCPAC.ROLLBACK_OPTION_VALUE
    				+ "&" + "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}

    	//If the function returns successfully, then we can update the next function value:
    	sProcessingResult += "<BR><I> - Time elapsed " + Long.toString((System.currentTimeMillis() - lStartingTime)/1000L) 
    		+ " seconds.</I><BR>";
    	
    	String sNextFunctionValue = "";
    	if (iFunctionValue == GLConvertACCPAC.LAST_FUNCTION_IN_SEQUENCE){
    		sNextFunctionValue = Integer.toString(GLConvertACCPAC.ROLLBACK_OPTION_VALUE);
    	}else{
    		sNextFunctionValue = Integer.toString(iFunctionValue + 1);
    	}
		clsDatabaseFunctions.freeConnection(getServletContext(), cnSMCP, "[1547080733]");
		clsDatabaseFunctions.freeConnection(getServletContext(), cnACCPAC, "[1547080734]");
		CurrentSession.setAttribute(SESSION_ATTRIBUTE_RESULT, sProcessingResult);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ GLConvertACCPAC.RADIO_FIELD_NAME + "=" + sNextFunctionValue
				+ "&" + "Status=" + "Function was successfully processed.<BR>"
					//+ sProcessingResult
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	private String processSelectedFunction(
		int iSelectedFunction, 
		int iACCPACDatabaseType, 
		String sUser,
		String sUserID,
		String sUserFullName,
		Connection cnSMCP, 
		Connection cnACCPAC,
		String sDatabaseName
		) throws Exception{
		
		GLACCPACConversion conv = new GLACCPACConversion();
		
		//System.out.println("[1552318879] - iSelectedFunction = '" + iSelectedFunction + "'.");
		
    	switch(iSelectedFunction){
    	case GLConvertACCPAC.ROLLBACK_OPTION_VALUE:
    		return conv.reverseDataChanges(cnSMCP, false);
    	case GLConvertACCPAC.PROCESS_GL_SEGMENTS_VALUE:
    		return conv.processGLAcctSegmentTables(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_SEGMENT_VALUES_VALUE:
    		return conv.processGLAcctSegmentValueTables(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_ACCOUNT_STRUCTURES_VALUE:
    		return conv.processGLAccountStructureTables(cnSMCP, cnACCPAC, sDatabaseName, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_ACCOUNT_GROUPS_VALUE:
    		return conv.processGLAccountGroups(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_ACCOUNT_MASTER_VALUE:
    		return conv.processGLAccounts(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_FISCAL_CALENDAR_VALUE:
    		return conv.processGLFiscalCalendar(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser, sUserID, sUserFullName);
    	case GLConvertACCPAC.PROCESS_GL_FISCAL_SETS_VALUE:
    		return conv.processGLFiscalSets(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser);
    	case GLConvertACCPAC.PROCESS_GL_POSTEDTRANSACTIONS_VALUE:
    		return conv.processGLTransactions(cnSMCP, cnACCPAC, iACCPACDatabaseType, sUser, sUserID, sUserFullName);
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
		if (iACCPACDatabaseType == SMTablegloptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE){
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
					//cnGL = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sGLDatabaseURL + ":1433;DatabaseName=" + sGLDatabaseName, sGLUserName, sGLPassword);
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
