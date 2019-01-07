/* AMDG */
package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ConnectionPool.CompanyDataCredentials;
import ConnectionPool.PoolUtilities;
import ConnectionPool.ServerSettingsFileParameters;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

/** Servlet that authenticates the user.*/
public class SMLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	private String sBackgroundcolor = "#" + SMUtilities.DEFAULT_BK_COLOR;
	private String sCompanyName = "";
	public static final String INPUT_DB_PARAMS_CLASS = "smcontrolpanel.SMInputDBParams";
	/*
	 * Gets passed in the request:
	 * 'db' - the database name
	 * 'showcontextparams' - this will cause the class to list all the web context parameters on the screen for debugging
	 * 'mobile' - if using a cell phone, tablet, etc.
	 */

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String subtitle = "";
		
		//Mobile or not:
		boolean bMobileView = clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_MOBILE, request).compareToIgnoreCase("Y") == 0;
		if(clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request).compareToIgnoreCase("") == 0){
			out.println(SMUtilities.DOCTYPE
				+ "<HTML><BR>Error - no database ID passed in.");
			out.println("</BODY></HTML>");
			return;
		}
		if (!readInitialCompanyData(
				clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request),
				getServletContext(),
				out)){

			out.println("</BODY></HTML>");
			return;
		}
		out.println(SMUtilities.getLoginHead(
				subtitle,
				sBackgroundcolor,
				SMUtilities.DEFAULT_FONT_FAMILY,
				bMobileView,
				getServletContext()
			)
		);
		//Get the database server name here:
		String sDatabaseServer = "";
		try {
			sDatabaseServer = SMUtilities.getDatabaseServerURL(request, null, getServletContext());
		} catch (Exception e) {
			out.println(e.getMessage());
		}
		out.println("<B><H2>" + sCompanyName + "</H2></B>");
		out.println(
				"<BR>Program version " + SMUpdateData.getProgramVersion() + ", " + SMUpdateData.getCopyright() 
				+ "<BR>Last revised <B>" + SMUpdateData.getLastRevisionDate() + "</B>."
				+ "<BR>Running on server: '<B>" + SMUtilities.getHostName() + "</B>'."
				+ "<BR>Using database server '<B>" + sDatabaseServer + "</B>'."
		);
		
		if (clsManageRequestParameters.get_Request_Parameter(SMUtilities.REQUEST_PARAM_SHOWCONTEXTPARAMETERS, request).compareToIgnoreCase("1") == 0){
			Enumeration<String> enumContextParameters = getServletContext().getInitParameterNames();
			while(enumContextParameters.hasMoreElements()) {
				String sParamName = (String)enumContextParameters.nextElement();
				out.println("<BR>" + sParamName + " = " + getServletContext().getInitParameter(sParamName));
			}
			out.println("<BR>");
		}
		
		//If this is a redirect from internal link with an invalid session, then store all the parameters passed in
		if(clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_REDIRECT_CLASS, request).compareToIgnoreCase("") != 0) {
			out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_REDIRECT_CLASS, request) + "\">");
			 Enumeration<String> parameterNames = request.getParameterNames();
			 while (parameterNames.hasMoreElements()) {
					String paramName = parameterNames.nextElement();
					String[] paramValues = request.getParameterValues(paramName);				
					for (int i = 0; i < paramValues.length; i++) {
						String paramValue = paramValues[i];
						out.println("<INPUT TYPE=HIDDEN NAME=\"" + paramName + "\" VALUE=\"" + paramValue + "\">");
					}
				}

		}else {
			out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin" + "\" method=\"post\">");
		}
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		//out.println("<TABLE WIDTH=100% CELLPADDING=10 BORDER=4>");
		out.println("<TR>");

		//Main database ID information:
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID 
			+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request) + "\">");
		//Store the options string:
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_OPTS + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_OPTS, request) + "\">");
		

		//Mobile or not:
		if(bMobileView){
			out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_MOBILE + "\" VALUE=\"" + "Y" + "\">");
			//User name:
			out.println(
					"<P>User name:<BR><INPUT TYPE=TEXT NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_USER + "\" " // SIZE=28 "
					+ "MAXLENGTH=50 VALUE=\"" 
					+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_USER, request) + "\""
					//+ "\" STYLE=\"width: 2.41in; height: 0.25in\""
					+ "></P>"
			);

			//Password:
			out.println(
					"<P>Password:<BR><INPUT TYPE=PASSWORD NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_PASSWORD + "\" " // SIZE=28 "
					+ "MAXLENGTH=50 VALUE=\"" 
					+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_PASSWORD, request) + "\""
					//+ "\" STYLE=\"width: 2.41in; height: 0.25in\">"
					+ "</P>"
			);
			out.println("<BR>");
			out.println("<INPUT TYPE=\"submit\" VALUE=\"Login\" class = \"buttonstyle\">");
		}else{
			out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_MOBILE + "\" VALUE=\"" + "N" + "\">");
			//User name:
			out.println(
					"<P>User name:<BR><INPUT TYPE=TEXT NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_USER + "\" SIZE=28 "
					+ "MAXLENGTH=50 VALUE=\"" 
					+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_USER, request)
					+ "\" STYLE=\"width: 2.41in; height: 0.25in\"></P>"
			);

			//Password:
			out.println(
					"<P>Password:<BR><INPUT TYPE=PASSWORD NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_PASSWORD + "\" SIZE=28 "
					+ "MAXLENGTH=50 VALUE=\"" 
					+ clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_PASSWORD, request)
					+ "\" STYLE=\"width: 2.41in; height: 0.25in\"></P>"
			);
			out.println("<BR>");
			out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Login\">");
		}
		out.println("</FORM>");
		out.println("</BODY></HTML>");
		return;
	}
	private boolean readInitialCompanyData(
			String sDbID,
			ServletContext context,
			PrintWriter pwOut){

		Connection conn = null;
		if (bDebugMode){
			System.out.println("In SMLogin - getting connection for company name - sDbID = " + sDbID 
					+ ".");
		}
		try {
			conn = PoolUtilities.getConnection(
					context,
					sDbID,
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".getCompanyName"));
		} catch (Exception e1) {
			pwOut.println(SMUtilities.DOCTYPE
					+ "<HTML>"
					+ "Error getting connection to database ID '" 
					+ sDbID + "' - " + e1.getMessage()
			);
			return false;			
		}

		if (conn == null){
			
		}else{
			CompanyDataCredentials cdc = new CompanyDataCredentials();
			try {
				ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
				cdc.load(
					sDbID,
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD)
				);
			} catch (Exception e1) {
				pwOut.println(SMUtilities.DOCTYPE
						+ "<HTML>"
						+ "Error getting control database information - " 
						+ sDbID + "' - " + e1.getMessage()
				);
				return false;
			}
			//Need to identify the database here in case we are getting a connection from a pool, and
			//that pool already has a database selected:
			String sSQL = "SELECT"
				+ " " + SMTablecompanyprofile.sCompanyName
				+ " FROM " + cdc.get_databasename() + "." + SMTablecompanyprofile.TableName
				;

			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()){
					sCompanyName = rs.getString(SMTablecompanyprofile.sCompanyName);
				}
				rs.close();
			} catch (SQLException e) {
				pwOut.println(SMUtilities.DOCTYPE
						+ "<HTML>Error getting recordset for database '" 
						+ cdc.get_databasename() + "' - " + e.getMessage());
				clsDatabaseFunctions.freeConnection(context, conn);
				return false;
			}
			sSQL = "SELECT"
				+ " " + SMTablesmoptions.sbackgroundcolor
				+ " FROM " + SMTablesmoptions.TableName
				;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()){
					sBackgroundcolor = rs.getString(SMTablesmoptions.sbackgroundcolor);
				}
				rs.close();
			} catch (SQLException e) {
				pwOut.println(SMUtilities.DOCTYPE
						+ "<HTML>Error getting backgroundcolor for database '" 
						+ cdc.get_databasename() + "' - " + e.getMessage());
				clsDatabaseFunctions.freeConnection(context, conn);
				return false;
			}
		}
		clsDatabaseFunctions.freeConnection(context, conn);
		return true;
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
