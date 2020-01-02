package smcontrolpanel;

//import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsNotifications;
import ServletUtilities.clsStringFunctions;

/** Servlet that authenticates the user.*/
public class SMAuthenticate{
	public static final String PARAM_TEST_NEW_LOGON = "TESTNEWLOGON";
	public static final String PARAM_UPDATE_DATABASE = "UPDATEDATA";
	private static boolean bDebugMode = false;
	
	public static boolean authenticateSMCPCredentials(
			HttpServletRequest req,
			HttpServletResponse res,
			ServletContext context,
			Long lFunctionID
	){
		
		res.setContentType("text/html");
		PrintWriter out;
		try {
			out = res.getWriter();
		} catch (IOException e) {
			return false;		
		}
		
		//String sSessionParameter = clsManageRequestParameters.get_Request_Parameter(SMUtilities.REQUEST_PARAM_SESSIONTAG, req);
	    String sUserName = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_USER, req);
	    String sUserFullName = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME, req) + " "
	    						+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME, req);
	    String sPassword = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_PASSWORD, req);
	    String sDatabaseID = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, req);
	    String sOpts = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_OPTS, req);
	    String sMobile = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_MOBILE, req);
	    String sSessionID = req.getSession().getId();
	    String sSessionDatabase =  (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sSessionUsername =  (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);

	    //System.out.println("[20202131735] " + " sUserName = '" + sUserName + "'.");
	    
        if(sSessionID == null) {
        	sSessionID = "";
        }
        if(sSessionDatabase == null) {
        	sSessionDatabase = "";
        }
        if(sSessionUsername == null) {
        	sSessionUsername = "";
        }
        
        //If there is a parameter to update the database then do that and return to the calling class. 
        //NOTE: If this parameter exists then we know the user has already been authenticated.
		if(clsManageRequestParameters.get_Request_Parameter(PARAM_UPDATE_DATABASE, req).compareToIgnoreCase("") != 0) {
			SMUpdateData dat  = new SMUpdateData();
			Connection conn = clsDatabaseFunctions.getConnection(
					context, 
					sSessionDatabase, 
					"MySQL", 
					"In " 
					+ "SMAuthenticate.checkForUpdates - User: " 
					+ (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID)
					);
			if (conn == null){
				out.write("Error getting connection to read database revision number from system during program update.");
			}
			
			if (!dat.update(conn, (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID), sDatabaseID)){
				out.write("Error:" + clsStringFunctions.filter(dat.getErrorMessage()));
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080407]");
			}else{
				out.write("Successfully updated SMCP to database version " + SMUpdateData.getDatabaseVersion());
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080408]");
				req.getSession().removeAttribute(SMUtilities.SMCP_SESSION_PARAM_UPDATE_REQUIRED);
			}
			return false;
		}
		
        boolean bAllQuickLinkParametersExist = ((sDatabaseID.compareToIgnoreCase("") != 0) && (sUserName.compareToIgnoreCase("") != 0) && (sPassword.compareToIgnoreCase("") != 0 ));
        boolean bComingFromLoginScreen = clsManageRequestParameters.get_Request_Parameter("CallingClass", req).compareToIgnoreCase("smcontrolpanel.SMLogin") == 0;
	    boolean bProgramUpdateRequired =  (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_UPDATE_REQUIRED) != null;
		
        if((!bAllQuickLinkParametersExist && sDatabaseID.compareToIgnoreCase("") == 0) ) {
        	
        	// MISSING DB
        	/*SMUtilities.sysprint(
        		"[1541794978]", 
        		sUserFullName, 
        		"Missing db parameter in request class: req.getRequestURI = '" 
        			+ req.getRequestURI() + "',"
               	+ " req parameters  = '" + clsManageRequestParameters.getAllRequestParameters(req) + "', " 
                	+ " session parameters = '" + clsServletUtilities.getSessionAttributes(req.getSession()) + "', "
                	+ " function ID = '" + Long.toString(lFunctionID) + "'"
                	+ "."
        	);*/
        	
        	//System.out.println("[1541794978] - Missing db parameter in request class: req.getRequestURI = '" + req.getRequestURI() + "',"
            //		+ " req.getQueryString()  = '" + req.getQueryString() + "'."
            //	);    	
        }
        
        //Make sure the current session is valid 
    	 if (!validateCurrentSession(sSessionID, sDatabaseID, sSessionDatabase) && !bAllQuickLinkParametersExist){
    		 //System.out.println("[202021317458] " + " current session is NOT valid");
	 	    	//String sCompanyName =  (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		    	
	 	    	//If the session is not valid and there is a DB parameter redirect to that login page
	 	    	if(sDatabaseID.compareToIgnoreCase("") != 0){
	 				if(sDatabaseID.compareToIgnoreCase(sSessionDatabase) != 0){
	 					//CONNECTION ERROR?
	 					String sOptsParamValue = sOpts;
	 					if (sOptsParamValue.compareToIgnoreCase("") == 0){
	 						sOptsParamValue = (String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_OPTS);
	 					}
	 					String sRedirectstring = SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMLogin" 
 		 					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDatabaseID 
 		 					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_REDIRECT_CLASS + "=" + req.getServletPath().replace("/","")
 							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_OPTS + "=" + sOptsParamValue
	 					;
	 					if (req.getQueryString() != null) {
	 						sRedirectstring += "&" + req.getQueryString();
	 					} 						
	 					try {
							res.sendRedirect(sRedirectstring);
						} catch (IOException e) {
						}
	 					return false;
	 				}
	 			
	 			//Otherwise we do not know what login to redirect to so display an invalid session error
	 			}else {
	 				out.println("<HTML><BODY> This session is no longer valid. Please log in again. </BODY></HTML>");
			    	return false;
	 			}
	 	    	
    	 	}
	    
	    //Get userID from UserName for now..
	    String sUserID = SMUtilities.getUserIDbyUserName(sUserName, context, sDatabaseID);
	    				
	    if (bDebugMode){
	    	System.out.println("In SMAuthenticate.authenticateCredentials - starting process:"
	    		+ "calling class = " + clsManageRequestParameters.get_Request_Parameter("CallingClass", req)
	    	);
		    if (sDatabaseID.compareToIgnoreCase("") != 0){
		    	System.out.println("In SMAuthenticate.authenticateCredentials - Database ID is not null - 01");
		    }
		    if (sUserName.compareToIgnoreCase("") != 0){
		    	System.out.println("In SMAuthenticate.authenticateCredentials - user is not null - 02");
		    }
		    if (sUserID.compareToIgnoreCase("") != 0){
		    	System.out.println("In SMAuthenticate.authenticateCredentials - userid is not null - 03");
		    }
		    if (sPassword.compareToIgnoreCase("") != 0){
		    	System.out.println("In SMAuthenticate.authenticateCredentials - pw is not null - 04");
		    }
	    }
	    
	  // If we are coming from the main login screen then we always create a new session.
	  // If the session still has a parameter to update the database than we always create a new session.
	  // Otherwise we create a new session if we have the database, user, password in the parameters list (aka quick links) 
	  // AND the database or user parameter is not already matching the current session information.
	  // OR If no session values exists for the database id or username then we always create a new session.
	    
	    try {
	    	//System.out.println("[202011916558] " + "");
			if (bProgramUpdateRequired || bComingFromLoginScreen 
				||((bAllQuickLinkParametersExist && ((sDatabaseID.compareToIgnoreCase(sSessionDatabase) != 0 || sUserName.compareToIgnoreCase(sSessionUsername) != 0 )))
				||(sSessionDatabase.compareToIgnoreCase("") == 0 || sSessionUsername.compareToIgnoreCase("") == 0 )
				)){
				
				//System.out.println("[202011916559] " + "");
				//Try to validate the user:
				//First get any session and invalidate it:
				HttpSession CurrentSession = req.getSession(true);
				try {
					CurrentSession.invalidate();
				} catch (Exception e) {
					//System.out.println("Error [1415807185] invalidating session - " + e.getMessage());
				}
				CurrentSession = req.getSession(true);
				CurrentSession.setMaxInactiveInterval(SMUtilities.SMCP_MAX_SESSION_INTERVAL_IN_SECONDS);
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID, sDatabaseID);
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME, sUserName);
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID, sUserID);
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_ACCESSCOUNTER, (Long)(1L));
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_OPTS, sOpts);
				
				//System.out.println("[202011916560] " + "");
				
				//Get the module level and license info:
				//Try to read the license file here:
			    String sLicenseModuleLevel = "";
				try {
					sLicenseModuleLevel = SMUtilities.getSMCPModuleLevel(context, sDatabaseID);
				} catch (Exception e) {
					out.println("<HTML>WARNING: Error [1467076089] - License file cannot be found - " + e.getMessage() + ".</BODY></HTML>");
					return false;
				}
				
				//System.out.println("[202011916561] " + "");
				
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL, sLicenseModuleLevel);
				//CurrentSession.setAttribute(SMUtilities.SESSION_PARAM_CHECK_SCHEDULE, "YES");

				if (sMobile.compareToIgnoreCase("Y") == 0){
					CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE, "Y");
				}else{
					CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE, "N");
				}
				
				//System.out.println("[202011916562] " + "");
				
				// TJR - 10/1/2014 - changed this to make it match the actual session ID:
				//CurrentSession.setAttribute(SMUtilities.SESSIONTAG_SESSION_PARAM, Long.toString(System.currentTimeMillis()));
				CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG, CurrentSession.getId());
				SMLogEntry log = new SMLogEntry(sDatabaseID, context);
				String sSessionTagAttribute = "N/A";
				try {
					sSessionTagAttribute = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG);
				} catch (Exception e1) {
					//System.out.println("Error [1421161767] reading session tag attribute - " + e1.getMessage());
				}
				/* TJR - 2/3/2015 - removed this to reduce volume of logging:
				log.writeEntry(
						sUserName, 
						"" + SMUtilities.REQUEST_PARAM_SESSIONTAG + " CREATED", 
						"SMAuthenticate", 
						sSessionTagAttribute, 
						"[1412691802]");
				*/
				log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMUSERLOGIN, 
					"Logging in", 
					sSessionTagAttribute + " OPTS='" + sOpts + "', "
						+ " query string = '" + clsManageRequestParameters.getAllRequestParametersMaskingPassword(req) + "'"
					, 
					"[1376509305]");
				if (bDebugMode){
					//System.out.println("In SMAuthenticate.authenticateCredentials - going into processLogin - 04");
				}
				
				//System.out.println("[202011916563] " + "");
				
				if (!processSMCPLogIn(
					sDatabaseID,
					sUserName,
					sUserID,
					sUserFullName,
					sPassword,
					sOpts,
					CurrentSession,
					req,
					out, 
					context
					)
				){
					try {
						CurrentSession.invalidate();
					} catch (Exception e) {
						System.out.println("Error [1420218596] - trying to invalidate session - " + e.getMessage());
					}
					return false;
				}else{
					out.println(createNotifications(
							(String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME),
							(String) req.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_UPDATE_REQUIRED),
							context
							));
					if (!SMSystemFunctions.isFunctionPermitted(
						lFunctionID, 
						sUserID, 
						context, 
						sDatabaseID,
						sLicenseModuleLevel)
					){
						out.println("<HTML>WARNING: You do not currently have access to this function.</BODY></HTML>");
							return false;
					}else{
						return true;
					}
				}
			}else{
			    //If there's not a complete set of databaseID, username, and password, then check to see if
			    //the session tag passed in and the session tag in the session match:
				//First get a session - if there IS one:
				HttpSession CurrentSession = req.getSession(false);
				if (CurrentSession == null){
					out.println("<HTML>WARNING: Error [1414081298] No valid session found.  Please log"
							+ " in again.</BODY></HTML>");
						return false;
				}
				//Set the 'mobile' status:
				if (sMobile.compareToIgnoreCase("Y") == 0){
					CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE, "Y");
				}else{
					CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE, "N");
				}

		    	String sSessionTagReadFromSession = "";
				try {
					sSessionTagReadFromSession = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG);
				} catch (Exception e) {
					out.println("<HTML>WARNING: This session is no longer valid.  Please log"
							+ " in again.</BODY></HTML>");
						return false;
				}
				
				//check to see if the session tag in the request is different than the session tag in the session itself:
				checkSMCPRequestAndSessionMatch(req, CurrentSession, lFunctionID);
				
				if(sSessionTagReadFromSession == null){
					//The session tag attribute in the session was null:
					out.println("<HTML>Warning: Error [1414080365] - the session is no longer valid - session tag in SESSION is null.</BODY></HTML>");
					return false;
				}
				//System.out.println("[202011916566] " + "");
				String sUserIDFromSession = "";
				String sDBIDFromSession = "";
				try {
					sUserIDFromSession = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
					sDBIDFromSession = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
				} catch (Exception e) {
					System.out.println("Error [1421162363] getting user and DB ID - " + e.getMessage());
				}
	    		try {
					if (!clsServletUtilities.isSessionValid(CurrentSession)){
						CurrentSession = req.getSession(true);
					}
				} catch (Exception e) {
					clsServletUtilities.sysprint("SMAuthenticate.processLogIn", sUserName, "Error[1414430464] - Reading Session " + e.getMessage());
				}
    			if (!SMSystemFunctions.isFunctionPermitted(
        				lFunctionID, 
        				sUserIDFromSession, 
        				context,
        				sDBIDFromSession,
        				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
        			)
    			){
					out.println("<HTML>WARNING: You do not currently have access to this particular function.</BODY></HTML>");
						return false;
    			}else{
    				return true;
    			}
			}
		} catch (Exception e) {
			out.println("Error [1415894420] in SMAuthenticate - " + e.getMessage());
			//System.out.println("Error [1415894421] in SMAuthenticate - " + e.getMessage());
			return false;
		}
	}
	
	private static String createNotifications(String sCompanyName, String sUpdateRquired, ServletContext context){
		
		String s = "";
		//Include
		s += "<HTML>"
				+ clsServletUtilities.getJQueryIncludeString() 
				+ clsServletUtilities.getJQueryUIIncludeString() 
				+ clsServletUtilities.getMasterStyleSheetLink()
				+ "<BODY>";
		//HTML for notifications
		if(sUpdateRquired != null) {
			s += clsNotifications.info_center_overlay(
					"Updating database from version " + sUpdateRquired + " to version " + Integer.toString(SMUpdateData.getDatabaseVersion()) 
					+ ". Please wait..."
					);
	
		}else {
			s += clsNotifications.success_center_fade("You have created a new session in <B>" +sCompanyName+"</B>" );
		}
		
		//Scripts for notifications
		s +=	"<script>";
		
		if(sUpdateRquired != null) {
			s += "function asyncRequest() {\n"
					+ " "
					+ "var xhr = new XMLHttpRequest();\n\n" 
	                + "xhr.onreadystatechange = function(){\n"  
	                + "    if (this.readyState == 4 && this.status == 200){\n"
	                + "   		if (this.responseText.includes(\"Error\")){\n"
	                + "			     document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").removeAttribute(\"class\");\n"	
	                + "			     document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").setAttribute(\"class\", \""+SMMasterStyleSheetDefinitions.NOTIFICATION_WARNING_CENTER+"\");\n"	
	                
	                + "        		 document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").innerHTML = this.responseText; \n"
	                + "    		}else{\n"
	                + "         	document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").innerHTML = this.responseText; \n"
	                + "         	setTimeout(function(){\n"
	                + " 				location.reload();\n"	
	                + "				}, 4000); \n"
	                + "			}\n" 
	                + "    }"
	                + "     if (this.readyState == 4 && this.status != 200){\n"
	                + "			      document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").removeAttribute(\"class\");\n"
	                + "	             document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").setAttribute(\"class\", \""+ SMMasterStyleSheetDefinitions.NOTIFICATION_WARNING_CENTER +"\");\n"
	                + "        		 document.getElementById(\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\").innerHTML = this.responseText; \n"
	                + "	   }\n" 
	                +"};\n\n"
	                
	                //Send the request
	                + "xhr.open(\"POST\", \"" + clsServletUtilities.getURLLinkBase(context) + "smcontrolpanel.SMUserLogin" + "\");\n"
	                + "xhr.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");\n"
	                + "xhr.send(\"" + PARAM_UPDATE_DATABASE + "=Y" + "\");\n"
					+ "}\n\n"
					;
		}
		
		//Add on document ready function.
		s +=   "$(document).ready(function (){\n";
		
		if(sUpdateRquired != null) {
			s += "setTimeout(asyncRequest, 3000);\n";
		}
				s +=	"});\n";
	    s += "</script>";
	
		s +=	"</BODY>"
				+ "</HTML>";
		return s;
	}

	private static boolean validateCurrentSession(String sCurrentSessionID, String sParameterDBID, String sCurrentDBID) {
		
		//If the session does not exist
		if(sCurrentSessionID.compareToIgnoreCase("") == 0) {
			return true;
		}
		//If the database ID parameter does not match the current database ID stored in the session 
		if(sParameterDBID.compareToIgnoreCase("") != 0){
			if(sParameterDBID.compareToIgnoreCase(sCurrentDBID) != 0){
				return false;
			}
		}
		
		// TJR - 12/21/2018 - no longer checking this:
		//If the session ID does not match the current session ID stored in the session 
		//if (sParameterSessionID.compareToIgnoreCase("") != 0 ) {
		//	if (sParameterSessionID.compareToIgnoreCase(sCurrentSessionID) != 0 ) {
		//		return false;
		//	}
		//}	
		return true;
	}
	
	
	private static void checkSMCPRequestAndSessionMatch(HttpServletRequest req, HttpSession session, long lFunctionID){
		
		//System.out.println("[202011916568] " + "");
		
		//String sSessionTagFromRequest = clsManageRequestParameters.get_Request_Parameter(SMUtilities.REQUEST_PARAM_SESSIONTAG, req);
		String sSessionTagFromSession = "";
		try {
			sSessionTagFromSession = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG);
		} catch (Exception e) {
			sSessionTagFromSession = "";
		}
		if (sSessionTagFromSession == null){
			sSessionTagFromSession = "";
		}
		
		//System.out.println("[202011916569] " + "");
		
		// TJR - 12/21/2018 - ignoring any session tags in requests now
		
		//if (sSessionTagFromRequest.compareToIgnoreCase("") == 0){
		//	//Just return:
		//	return;
		//}
		
		//System.out.println("[202011916570] " + "");
		
		String sUser = "";
		try {
			sUser = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		} catch (Exception e) {
			sUser = "N/A";
		}
		
		String sDatabaseID = "";
		try {
			sDatabaseID = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		} catch (Exception e) {
			sUser = "N/A";
		}
		
		//System.out.println("[202011916572] " + "");
		
		if (sSessionTagFromSession.compareToIgnoreCase("") == 0){
			clsServletUtilities.sysprint(
				"SMAuthenticate", 
				sUser, 
				"Error [1424113404] - session tag read from session is empty."
				+ ", Database ID = '" + sDatabaseID + "'."
			);
		}
		
		//System.out.println("[202011916573] " + "");
		
		//if (sSessionTagFromRequest.compareToIgnoreCase(sSessionTagFromSession) != 0){
		//	SMUtilities.sysprint(
		//		"SMAuthenticate", 
		//		sUser, 
		//		"Error [1424113405] - mismatch: session from request = '" + sSessionTagFromRequest 
		//		+ "', session tag from session = '" + sSessionTagFromSession + "'"
		//		+ ", Database ID = '" + sDatabaseID + "', Function ID = '" + Long.toString(lFunctionID) + "', "
		//		+ "querystring: '" + req.getQueryString() + "'."
		//	);
		//}
	}
	private static boolean processSMCPLogIn(
			String sDatabaseID,
			String sUserName,
			String sUserID,
			String sUserFullName,
			String sPassword,
			String sOpts,
			HttpSession session,
			HttpServletRequest req,
			PrintWriter pwOut, 
			ServletContext context
	){
		boolean bResult = true;
		//Read the company name from the company profile:
		String SQL = "SELECT"
			+ " " + SMTablecompanyprofile.sCompanyName
			+ " FROM " + SMTablecompanyprofile.TableName
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
        		SQL, 
        		context, 
        		sDatabaseID,
        		"MySQL",
        		"SMAuthenticate" + ".processLogIn = User: " 
        		+ sUserID
        		+ " - "
        		+ sUserFullName
        		+ " [1331736997]"
	        	);
			if (rs.next()){
				session.setAttribute(
						SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME, 
						rs.getString(SMTablecompanyprofile.sCompanyName));
				session.setAttribute(
						clsServletUtilities.SESSION_PARAM_LOCALE, 
						rs.getString(SMTablecompanyprofile.sCompanyName));
				session.setAttribute(
						clsServletUtilities.SESSION_PARAM_FULL_USER_NAME,
							session.getAttribute(clsServletUtilities.SESSION_PARAM_FULL_USER_NAME)
							+ " - " 
							+ rs.getString(SMTablecompanyprofile.sCompanyName));
				
			}else{
				session.setAttribute(
						SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME, 
						"");	
				session.setAttribute(
						clsServletUtilities.SESSION_PARAM_LOCALE, 
						"");
			}
			rs.close();
		}catch(SQLException e){
			clsServletUtilities.sysprint("SMAuthenticate.processLogIn", sUserName, "Error reading company profile - " + e.getMessage());
			bResult = false;
			return bResult;
		}
		
	    try{
			SQL = "SELECT"
				+ " SHA('" + sPassword + "') AS SUBMITTEDPW"
				+ ", " + SMTableusers.sHashedPw
				+ ", " + SMTableusers.sUserFirstName
				+ ", " + SMTableusers.sUserLastName
				+ ", " + SMTableusers.iactive
				+ " FROM "
			  	+ SMTableusers.TableName
			  	+ " WHERE ("
			  		+ "(" + SMTableusers.sUserName + " = '" + sUserName + "')"
			  		+ " AND (" + SMTableusers.iactive + " = 1)"
			  	+ ")";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
        		SQL, 
        		context, 
        		sDatabaseID,
        		"MySQL",
        		"SMAuthenticate" + ".processLogIn = User: " + sUserID
        		+ " - "
        		+ sUserFullName
	        	);
		    if (rs.next()){
		    	if (rs.getString("SUBMITTEDPW").compareTo(rs.getString(SMTableusers.sHashedPw))!= 0){
		    		SMLogEntry log = new SMLogEntry(sDatabaseID, context);
			    	log.writeEntry(
			    			sUserID, 
			    			SMLogEntry.LOG_OPERATION_SMUSERLOGIN, 
			    			"Invalid password for user '" + sUserName + "'", 
			    			"OPTS='" + sOpts + "', "
			    					+ " query string = '" + clsManageRequestParameters.getAllRequestParametersMaskingPassword(req) + "'"
			    			,
			    			"[1376509306]"
			    			);
			    	bResult = false;
			    	pwOut.println("<HTML>Invalid password for user '" + sUserName + "'. Please try again.</HTML");	
		    	}else{
			    	if (!checkForSMCPUpdates(
			    			sDatabaseID,
			    			sUserName,
			    			sUserID,
			    			sUserFullName,
			    			pwOut,
			    			context,
			    			req
			    	)){
			    		bResult = false;
			    		if (bDebugMode){
			    			System.out.println("In SMAuthenticate.processLogin - checkForUpdates=false - 10");
			    		}
			    	}else{
				    	//User is authenticated, database is checked, proceed:
				    	session.setAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME, rs.getString(SMTableusers.sUserFirstName));
				    	session.setAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME, rs.getString(SMTableusers.sUserLastName));
				    	
				    	//Try to get the company name along with the user name:
				    	String sCompanyName = "";
				    	if ((String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) != null){
				    		sCompanyName = " - " + (String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
				    	}
				    	session.setAttribute(
				    		clsServletUtilities.SESSION_PARAM_FULL_USER_NAME, 
				    		rs.getString(SMTableusers.sUserFirstName) 
				    			+ " " 
				    			+ rs.getString(SMTableusers.sUserLastName)
				    			+ sCompanyName
				    	);
			    	}
		    	}
		    }else{
		    	SMLogEntry log = new SMLogEntry(sDatabaseID, context);
		    	log.writeEntry(
		    			sUserID, 
		    			SMLogEntry.LOG_OPERATION_SMUSERLOGIN, 
		    			"Invalid username (" + sUserName + "')", 
		    			"OPTS='" + sOpts + "',"
		    			+ " query string = '" + clsManageRequestParameters.getAllRequestParametersMaskingPassword(req) + "'",
		    			"[1376509516]"
		    			);
		    	bResult = false;
		    	pwOut.println("<HTML>Username '" + sUserName + "' is not valid and active. Please try again.</HTML>");	
		    }
			rs.close();
	    } catch (SQLException ex){
	    	pwOut.println("<HTML>Error [1414092791] processing log in - " + ex.getMessage() + ".</HTML>");	
	    }
	    return bResult;
	}
	private static boolean checkForSMCPUpdates(
			String sDBIB, 
			String sUserName,
			String sUserID,
			String sUserFullName,
			PrintWriter pwOut, 
			ServletContext context,
			HttpServletRequest req){

		try{
			String SQL = "SELECT " + SMTablecompanyprofile.iDatabaseVersion + " FROM " + SMTablecompanyprofile.TableName;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
	        		sDBIB,
	        		"MySQL",
	        		"SMAuthenticate" + ".checkForUpdates = User: " 
	        		+ sUserID
	        		+ "  - "
	        		+ sUserFullName
	        		+ " [1331736977]"
	        	);
			if (rs.next()){
				int iReadDatabaseVersion = rs.getInt(SMTablecompanyprofile.iDatabaseVersion);
				if (iReadDatabaseVersion >= SMUpdateData.getDatabaseVersion()){
					rs.close();
					return true;
				}else{
					rs.close();
					//Store the database version number in the session.
					//If this is present when loading the notifications later in this class, then we tell the user
					//the program is updating and lock the screen. 
					//Then we send an ajax request to execute the update.
					req.getSession().setAttribute(SMUtilities.SMCP_SESSION_PARAM_UPDATE_REQUIRED,  Integer.toString(iReadDatabaseVersion));	
					return true;
				}
			}else{
				rs.close();
				pwOut.println("<HTML>Error reading database revision number from system.</HTML>");
				return false;				
			}
		}catch(SQLException e){
			pwOut.println("<HTML>Error reading database revision number from system: " + e.getMessage() + "</HTML>");
			return false;
		}
	}
}
