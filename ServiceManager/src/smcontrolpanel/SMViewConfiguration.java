package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMModuleListing;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ConnectionPool.CompanyDataCredentials;
import ConnectionPool.PoolUtilities;
import ConnectionPool.ServerSettingsFileParameters;
import ConnectionPool.WebContextParameters;

public class SMViewConfiguration  extends HttpServlet {
	private static final long serialVersionUID = 1L;
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
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
								+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		String sOpts = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_OPTS);

		long lLicenseModuleLevel = 0;
		try {
			lLicenseModuleLevel = Long.parseLong(sLicenseModuleLevel);
		} catch (NumberFormatException e1) {
			//Don't catch anything here....
		}
		
		String title = "";
		if (!SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewSystemConfiguration, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModuleLevel)
				){
			out.println("<BR><B>You do not have permission to view these setttings.</B><BR>");
			return;
		}
		
		//Record the use of this function:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_SMVIEWSYSTEMCONFIGURATION, 
				"REPORT", 
				"Viewing system configuration for: " + sCompanyName, 
				"[1564759531]"
			);

		
		title = "View System Configuration";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, "#FFFFFF", sCompanyName));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		out.println(SMUtilities.getMasterStyleSheetLink());
		//out.println("\n" + sStyleScripts() + "\n");		
		
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println("<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >");
		out.println("<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING +  " \">\n" 
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>Setting</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>Value</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>Remark</B>"
			+ "</TD>\n"
		);
		boolean bOddRow = true;
		
		//Read settings from server settings file.
		ServerSettingsFileParameters serverSettingsFile;
		String sControlDatabaseURL = "";
		String sControlDatabaseName = "";
		String sControlDatabasePort = "";
		String sControlDatabaseUser = "";
		String sControlDatabasePw = "";	
		try {
			serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(getServletContext()) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);

			sControlDatabaseURL = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
			sControlDatabaseName = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME);
			sControlDatabasePort = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT);
			sControlDatabaseUser = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME);
			sControlDatabasePw = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD);	
		} catch (Exception e1) {
			out.println("<FONT COLOR=RED>Error [1540912265] - rerror reading control database information from server settings file </FONT>");
		}
		Date creationTime = new Date(CurrentSession.getCreationTime());
	    Date lastAccessed = new Date(CurrentSession.getLastAccessedTime());
	    Date webservertime = new Date(System.currentTimeMillis());
	    DateFormat formatter = DateFormat.getDateTimeInstance(
	            DateFormat.MEDIUM, DateFormat.MEDIUM);
	    String sCurrentControlDatabaseDate = "N/A";
	    String SQL = "SELECT DATE_FORMAT(NOW(), '%b %e, %Y %h:%i:%s %p') AS currdate";
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + ".getting current date - user: '" 
	    + sUserID 
	    + "'"
	    + " - "
	    + sUserFullName);
			if (rs.next()){
				sCurrentControlDatabaseDate = rs.getString("currdate");
			}
			rs.close();
		} catch (SQLException e) {
			//No need to catch this...
		}
	    
		out.println(createRow("Web app name:", WebContextParameters.getInitWebAppName(getServletContext()), "The name of the app on the web server for this program.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Program title:", WebContextParameters.getInitProgramTitle(getServletContext()), "Full title of the program.", bOddRow));
		bOddRow = !bOddRow;
		
		out.println(createRow("Licensed modules:", SMModuleListing.getModuleList(lLicenseModuleLevel), "Modules configured for this company.", bOddRow));
		bOddRow = !bOddRow;
		
		String sExpirationDate = SMUtilities.getSMCPLicenseExpirationDate(getServletContext(), sDBID);
		ServletUtilities.clsDBServerTime objServerTime = null;
		try {
			objServerTime = new ServletUtilities.clsDBServerTime(sDBID, sUserFullName, getServletContext());
			if (sExpirationDate.compareToIgnoreCase(objServerTime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_SQL)) < 0){
				if (sExpirationDate.compareToIgnoreCase(SMUtilities.EMPTY_SQL_DATE_VALUE) != 0){
					sExpirationDate = "<B><FONT COLOR=RED>" + sExpirationDate + " - YOUR CURRENT SMCP LICENSE HAS EXPIRED</FONT></B>";
				}
			}
		} catch (Exception e1) {
			//Don't choke on this.
		}
		out.println(createRow("License expires on:", sExpirationDate, "Your current SMCP license expires on this date.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Path to web app:", SMUtilities.getAbsoluteRootPath(request, getServletContext()), "The path on the web server to the folder that holds the program itself.", bOddRow)); //?
		bOddRow = !bOddRow;
		out.println(createRow("Path to SM temporary files:", SMUtilities.getAbsoluteSMTempPath(request, getServletContext()), "The path that will hold any temporary files for the program.", bOddRow)); //?
		bOddRow = !bOddRow;
		out.println(createRow("Path to SM javascripts:", 
				SMUtilities.getAbsoluteRootPath(request, getServletContext()).replace(
					System.getProperty("file.separator") + WebContextParameters.getInitWebAppName(getServletContext()) + System.getProperty("file.separator"), "")
				+ WebContextParameters.getInitScriptPath(getServletContext()),
			"The path on the web server to the folder that holds all the script files for the program.", bOddRow));
		bOddRow = !bOddRow;
		
		out.println(createRow("Path to SM 'local resources':", 
				SMUtilities.getAbsoluteRootPath(request, getServletContext()).replace(
					System.getProperty("file.separator") + WebContextParameters.getInitWebAppName(getServletContext()) + System.getProperty("file.separator"), "")
				+ WebContextParameters.getLocalResourcesPath(getServletContext()),
			"Local resources include customized invoice or proposal logos, etc..", bOddRow));
		
		
		//out.println(createRow("Path to local resource files:", WebContextParameters.getLocalResourcesPath(getServletContext()), "Local resources include customized invoice or proposal logos, etc.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Control database server:", sControlDatabaseURL , "Address of the database server hosting the control database.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Control database time:", sCurrentControlDatabaseDate, "Current date/time on the database server hosting the control database. "
					+ "(NOTE: this could be different than the database server hosting this particular company database).", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Control database server port:", sControlDatabasePort, "Port number used to connect to the control database server (usually 3309).", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Control database name:", sControlDatabaseName, "The 'control database' lists all of the individual company databases and credentials for each.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Control database user name:", sControlDatabaseUser, "User name used to connect to the control database.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Maximum number of database connections:", WebContextParameters.getMaximumNumberOfConnections(getServletContext()), "Maximum number of connections allowed to accumulate in the connection pool.", bOddRow));
		bOddRow = !bOddRow;
		/*out.println(createRow("Web app to create doc folders:", WebContextParameters.getcreatefolderURL(getServletContext()), "This is the URL of the Google 'web app' that is used to create document folders in Google Drive.", bOddRow));
		bOddRow = !bOddRow;*/
		out.println(createRow("Running on web server:", SMUtilities.getHostName(), "The name of the server hosting the web application.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Web server information:", getServletContext().getServerInfo(), "Name and version of the current web server.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current web server time:", formatter.format(webservertime), "Date and time on the current web server.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("'Link base' for program links:", SMUtilities.getURLLinkBase(getServletContext()), "The link base identifies the URL and web app which is used as the base of any links the prorgam creates.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current program version:", SMUpdateData.getProgramVersion() + " - " + SMUpdateData.getCopyright(), "Program version and copyrght.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Last revision date:", SMUpdateData.getLastRevisionDate(), "Program version and copyrght.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Database revision number:", SMUtilities.getDatabaseRevisionNumber(
			getServletContext(), 
			sDBID, 
			SMUtilities.getFullClassName(this.toString()), 
			sUserID,
			sUserFullName), "Minor database revision number, corresponds to every database structure change.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current user name:", sUserName, "User name you are currently logged in under - stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current user ID:", sUserID, "User ID associated with your login name - stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current user full name:", sUserFullName, "First and last name associated with your login name - stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("'OPTS' login parameter:", sOpts, "'OPTS' parameter in login request string.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current database ID:", sDBID, "This identifies the database you are currently working with - stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current Company name:", sCompanyName, "Read from the database and stored in your current web session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session ID:", CurrentSession.getId(), "Session ID provided by web server for your session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session creation time:", formatter.format(creationTime), "When you logged in and created the current session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Current session last accessed:", formatter.format(lastAccessed), "When you last accessed the current session.", bOddRow));
		bOddRow = !bOddRow;
		out.println(createRow("Maximum session interval:", Integer.toString(SMUtilities.SMCP_MAX_SESSION_INTERVAL_IN_SECONDS/60) + " minutes", "Maximum time a session will stay open after the last access.", bOddRow));
		bOddRow = !bOddRow;
		
		out.println("</TABLE>");

		//Now show all of the databases and information in the smcpcontrols database:
		out.println("<BR><U><B>SMCP Controls Listing of Company Databases<B></U>");
		out.println("<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  + "\" >");
		out.println("<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING +  " \">\n" 
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>DB ID</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>DB Name</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>URL</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>Port</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>User</B>"
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \">"
			+ "<B>Comment</B>"
			+ "</TD>\n"
		);
		
		//Read the company data credentials database:
		Connection conn = null;
		String sConnectionString = 
			"jdbc:mysql://" 
				+ sControlDatabaseURL 
				+ ":" + sControlDatabasePort + "/" 
				+ sControlDatabaseName
				+ "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
		;
		try {
			conn = DriverManager.getConnection(
				sConnectionString, 
				sControlDatabaseUser, 
				sControlDatabasePw);
		}catch (Exception E) { 
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(
						sConnectionString, 
						sControlDatabaseUser, 
						sControlDatabasePw
				);
			}catch(Exception e){
				out.println("<BR><FONT COLOR=RED>Error [1435701029] getting connection for control database information - '" 
					+ sDBID + "' - " + e.getMessage() + "</FONT>"
				);
				out.println("</TABLE>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		
		SQL = "SELECT * FROM " + CompanyDataCredentials.TableName 
			+ " ORDER BY " + CompanyDataCredentials.sdatabaseid
		;
		
		try {
			ResultSet rs = PoolUtilities.openResultSet(SQL, conn); 
			while(rs.next()){
				out.println(createCDCRow(
					rs.getString(CompanyDataCredentials.sdatabaseid), 
					rs.getString(CompanyDataCredentials.sdatabasename), 
					rs.getString(CompanyDataCredentials.sdatabaseurl), 
					rs.getString(CompanyDataCredentials.sdatabaseport), 
					rs.getString(CompanyDataCredentials.sdatabaseuser),  
					rs.getString(CompanyDataCredentials.scomment), 
					bOddRow)
				);
				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (SQLException e) {
			out.println("<BR><FONT COLOR=RED>Error [1435701030] reading control database information - '" 
				+ sDBID + "' - " + e.getMessage() + "</FONT>"
			);
			out.println("</TABLE>");
			out.println("</BODY></HTML>");
			return;
		}
		
		out.println("</TABLE>");
		
		out.println("</BODY></HTML>");
	}
	private String createRow(String sLabel, String sValue, String sRemark, boolean bOddRow){
		
		String sFilteredRemark = sRemark.trim();
		if (sFilteredRemark.compareToIgnoreCase("") == 0){
			sFilteredRemark = "&nbsp;";
		}
		String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
		if (bOddRow){
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;
		}
		String s = "<TR class = \" " + sBackgroundColor +  " \">";
		s +=  "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sLabel
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sValue
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sFilteredRemark
			+ "</TD>\n"
		;
		s += "</TR>\n";
		
		return s;
	}
	private String createCDCRow(
			String sDBID, 
			String sDBName, 
			String sURL, 
			String sPort, 
			String sUser, 
			String sComment, 
			boolean bOddRow){
		
		String sFilteredComment = sComment.trim();
		if (sFilteredComment.compareToIgnoreCase("") == 0){
			sFilteredComment = "&nbsp;";
		}
		String sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_ODD;
		if (bOddRow){
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN;
		}
		String s = "<TR CLASS = \"" + sBackgroundColor +  "\">";
		s += "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sDBID
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sDBName
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sURL
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sPort
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sUser
			+ "</TD>\n"
			+ "<TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \">"
			+ sComment
			+ "</TD>\n"
		;
		s += "</TR>\n";
		
		return s;
	}
	/*private String sStyleScripts(){
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
				+ "vertical-align:bottom; "
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
			+ "vertical-align:bottom; "
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
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}*/
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}