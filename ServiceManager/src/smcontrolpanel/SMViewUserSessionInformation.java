package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMViewUserSessionInformation  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DARK_ROW_BG_COLOR = "#DCDCDC";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	private static final String HEADER_BG_COLOR = "#C2E0FF";
	private static final String USER_PLATFORM_FIELD = "USERPLATFORMFIELD";
	private static final String LOCAL_DATETIME_FIELD = "LOCALDATETIMEFIELD";
	private static final String BROWSER_FIELD = "BROWSERFIELD";
	private static final String BROWSER_LANGUAGE_FIELD = "BROWSERLANGUAGEFIELD";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L)
				){
			return;
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sOpts = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_OPTS);

		String title = "";
		if (!SMSystemFunctions.isFunctionPermitted(
				-1, 
				sUserID, 
				getServletContext(), 
				sDBID,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
				){
			out.println("<BR><B>You do not have permission to view these setttings.</B><BR>");
			return;
		}
		
		//Record the use of this function:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_VIEWUSERSESSIONINFO, 
			"Viewing user info", 
			sCompanyName, 
			"[1435785109]"
		);
		
		title = "View Session Information";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		out.println("\n" + sCommandScripts() + "\n");
		out.println("\n" + sStyleScripts() + "\n");		
		
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println("<TABLE class = \"basic\" >");
		out.println("<TR style = \" background-color: " + HEADER_BG_COLOR +  "; \">" 
			+ "<TD class = \"centerjustifiedheading \">"
			+ "<B>Setting</B>"
			+ "</TD>"
			+ "<TD class = \"centerjustifiedheading \">"
			+ "<B>Value</B>"
			+ "</TD>"
			+ "<TD class = \"centerjustifiedheading \">"
			+ "<B>Remark</B>"
			+ "</TD>"
		);
		boolean bOddRow = true;
		
		long lCreationTimeInMS = CurrentSession.getCreationTime();
		long lLastAccessedTimeInMS = CurrentSession.getLastAccessedTime();
		Date creationTime = new Date(lCreationTimeInMS);
	    Date lastAccessed = new Date(lLastAccessedTimeInMS);
	    long lCreationTimeInMinutes = lCreationTimeInMS / (1000 * 60);
	    long lLastAccessTimeInMinutes = lLastAccessedTimeInMS / (1000 * 60);
	    long lTimeInSessionInMinutes = lLastAccessTimeInMinutes - lCreationTimeInMinutes;
	    long lTimeRemainingInMinutes = (CurrentSession.getMaxInactiveInterval()/60) -  lTimeInSessionInMinutes;
	    
	    //System.out.println("[1544220948]\n"
	    //	+ "lCreationTimeInMinutes: '" + lCreationTimeInMinutes + "'\n"
	    //	+ "lLastAccessTimeInMinutes: '" + lLastAccessTimeInMinutes + "'\n"
	    //	+ "max inactive interval: '" + CurrentSession.getMaxInactiveInterval()/60 + "'\n"
	    //	+ "lTimeInSession: '" + lTimeInSessionInMinutes + "'\n"
	    //	+ "lTimeRemaining time: '" + lTimeRemainingInMinutes + "'\n"
	    //);
	    
	    DateFormat formatter = DateFormat.getDateTimeInstance(
	            DateFormat.MEDIUM, DateFormat.MEDIUM);
	    String sUserFullName = "N/A";
	    String sDefaultSalespersonCode = "N/A";
	    String sEmailAddress = "N/A";
	    String sMechanicInitials = "N/A";
	    String SQL = "SELECT * FROM " + SMTableusers.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTableusers.lid + " = " + sUserID + ")"
	    	+ ")"
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".read user information - userID: '" + sUserID + "'"
			);
			if (rs.next()){
				sUserFullName = rs.getString(SMTableusers.sUserFirstName) + " " + rs.getString(SMTableusers.sUserLastName);
				sDefaultSalespersonCode = rs.getString(SMTableusers.sDefaultSalespersonCode);
			    sEmailAddress = rs.getString(SMTableusers.semail);
			    sMechanicInitials = rs.getString(SMTableusers.smechanicinitials);
			}
			rs.close();
		} catch (SQLException e) {
			//No need to catch this....
		}
	    
	    String sSecurityGroups = "";
	    SQL = "SELECT * FROM " + SMTablesecurityusergroups.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTablesecurityusergroups.luserid + " = " + sUserID + ")"
	    	+ ") ORDER BY " + SMTablesecurityusergroups.sSecurityGroupName
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".read user information - userID: '" + sUserID + "'"
			);
			while (rs.next()){
				if (sSecurityGroups.compareToIgnoreCase("") == 0){
					sSecurityGroups += rs.getString(SMTablesecurityusergroups.sSecurityGroupName);					
				}else{
					sSecurityGroups += ", " + rs.getString(SMTablesecurityusergroups.sSecurityGroupName);
				}
			}
			rs.close();
		} catch (SQLException e) {
			//No need to catch this....
		}	    
	    
		out.println(createRow("Program title:", WebContextParameters.getInitProgramTitle(getServletContext()), "Full title of the program.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current program version:", SMUpdateData.getProgramVersion() + " - " + SMUpdateData.getCopyright(), "Program version and copyrght.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Last revision date:", SMUpdateData.getLastRevisionDate(), "Last revision date on this version.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Database revision number:", SMUtilities.getDatabaseRevisionNumber(
			getServletContext(), 
			sDBID, 
			SMUtilities.getFullClassName(this.toString()), 
			sUserID,
			sUserFullName), "Minor database revision number, corresponds to every database structure change.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session ID:", CurrentSession.getId(), "Session ID provided by web server for your session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Time in session:", Long.toString(lTimeInSessionInMinutes) + " minutes", "Elapsed time in the current session.", bOddRow));
		bOddRow = !bOddRow;		
		out.println(createRow("Session time remaining:", Long.toString(lTimeRemainingInMinutes) + " minutes", "Minutes until this session expires.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session creation time:", formatter.format(creationTime), "When you logged in and created the current session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session last accessed:", formatter.format(lastAccessed), "When you last accessed the current session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Maximum session interval:", Integer.toString(SMUtilities.SMCP_MAX_SESSION_INTERVAL_IN_SECONDS/60) + " minutes", "Maximum time a session will stay open after the last access.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current user:", sUserName + " - " + sUserFullName, "User name you are currently logged in under - stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Email address:", sEmailAddress, "Your email address as stored in the program.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Default salesperson code:", sDefaultSalespersonCode, "Your salesperson code (if you have one).", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Your mechanic initials:", sMechanicInitials, "Your initials as a mechanic in the system (if applicable).", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Your security groups:", sSecurityGroups.replace(",", "<BR>"), "System security groups of which you are a member.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current Company name:", sCompanyName, "Read from the database and stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("'OPTS' parameter from the log in link:", sOpts, "Links to the program can include an '&OPTS=xxxx' parameter to pass information from the link.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Local OS Platform:", "<p id=\"" + USER_PLATFORM_FIELD + "\"></p>", "Your local operating system.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Browser:", "<p id=\"" + BROWSER_FIELD + "\"></p>", "Your browser version.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Browser language:", "<p id=\"" + BROWSER_LANGUAGE_FIELD + "\"></p>", "Your browser language setting.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Local computer time:", "<p id=\"" + LOCAL_DATETIME_FIELD + "\"></p>", "The current date and time in your computer.", bOddRow));
		bOddRow = !bOddRow;
		out.println("</TABLE>");

		out.println("</BODY></HTML>");
	}
	private String createRow(String sLabel, String sValue, String sRemark, boolean bOddRow){
		
		String sFilteredRemark = sRemark.trim();
		if (sFilteredRemark.compareToIgnoreCase("") == 0){
			sFilteredRemark = "&nbsp;";
		}
		String sBackgroundColor = DARK_ROW_BG_COLOR;
		if (bOddRow){
			sBackgroundColor = LIGHT_ROW_BG_COLOR;
		}
		String s = "<TR style = \" background-color: " + sBackgroundColor +  "; \">";
		s += "<TD class = \"rightjustifiedheading \" >"
			+ sLabel
			+ "</TD>"
			+ "<TD class = \"leftjustifiedcell \" >"
			+ sValue
			+ "</TD>"
			+ "<TD class = \"leftjustifiedcell \">"
			+ "<I>" + sFilteredRemark + "</I>"
			+ "</TD>"
		;
		s += "</TR>";
		
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		//String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
			+ "border-width: 1px; "
			+ "border-spacing: 0px; "
			//+ "border-style: outset; "
			+ "border-style: solid; "
			//+ "border-style: none; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			//+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, left justified:
		s +=
				"td.leftjustifiedcell {"
				+ "border: 1px solid; "
				+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: small; "
				+ "font-weight: normal; "
				+ "text-align: left; "
				+ "vertical-align:top; "
				+ "}"
				+ "\n"
				;
		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:top; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:top; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	private String sCommandScripts(){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script src=\"scripts/detect.js\"></script>";
		
		s += "<script type='text/javascript'>\n";
		
		s += "function displayLocalValues() {\n";
		s += "    var ua = detect.parse(navigator.userAgent);\n";
		/*
		ua.browser.family // "Mobile Safari"
		ua.browser.name // "Mobile Safari 4.0.5"
		ua.browser.version // "4.0.5"
		ua.browser.major // 4
		ua.browser.minor // 0
		ua.browser.patch // 5

		ua.device.family // "iPhone"
		ua.device.name // "iPhone"
		ua.device.version // ""
		ua.device.major // null
		ua.device.minor // null
		ua.device.patch // null
		ua.device.type // "Mobile"
		ua.device.manufacturer // "Apple"

		ua.os.family // "iOS"
		ua.os.name // "iOS 4"
		ua.os.version // "4"
		ua.os.major // 4
		ua.os.minor // 0
		ua.os.patch // null
		*/
		
		s += "    document.getElementById(\"" + USER_PLATFORM_FIELD + "\").innerHTML = navigator.platform;\n";
		s += "    document.getElementById(\"" + BROWSER_FIELD + "\").innerHTML = ua.browser.name;\n";
		s += "    document.getElementById(\"" + BROWSER_LANGUAGE_FIELD + "\").innerHTML = navigator.language;\n";
		s += "    document.getElementById(\"" + LOCAL_DATETIME_FIELD + "\").innerHTML = timenow();\n";
		
		//TEST:
		s += "    console.log(navigator);\n";
		
		s += "    console.log(ua.device.family);\n";
		s += "    console.log(ua.device.name);\n";
		s += "    console.log(ua.device.manufacturer);\n";
		s += "    console.log(ua.os.family);\n";
		s += "    console.log(ua.os.name);\n";
		s += "    console.log(ua.os.version);\n";
		s += "    console.log(ua.browser.version);\n";
		s += "    console.log(ua.browser.major);\n";
		s += "    console.log(ua.device.minor);\n";
		s += "    console.log(ua.device.patch);\n";

		s += "}\n";
		s += "\n";
		
		s += "function timenow(){\n";
		s += "    var now= new Date(),\n";
		s += "        ampm= 'am',\n";
		s += "        h= now.getHours(),\n"; 
		s += "        m= now.getMinutes(),\n"; 
		s += "        s= now.getSeconds();\n";
		s += "    if(h>= 12){\n";
		s += "        if(h>12) h -= 12;\n";
		s += "            ampm= 'pm';\n";
		s += "    }\n";

		s += "    if(m<10) m= '0'+m;\n";
		s += "    if(s<10) s= '0'+s;\n";
		s += "    return now.toLocaleDateString('en-us')+ ' ' + h + ':' + m + ':' + s + ' ' + ampm;\n";
		s += "}\n";

		s += "window.onload = function(){\n"
			+ "    displayLocalValues();\n"
			+ "}\n\n"
		;
		
		s += "</script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}