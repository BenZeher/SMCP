package smas;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASMainMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sSessionTag = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ASAlarmFunctions))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String subtitle = "Alarm System";
		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}
		sSessionTag = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG);
		/*
		if (bMobileView){
			String sHeading = SMUtilities.DOCTYPE
			+ "<HTML>"
			+ "<HEAD>"
			+ "<TITLE>" + title + "</TITLE>"
			+ SMUtilities.faviconLink(getServletContext())
			//This line should keep the font widths 'screen' wide:
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />"
			+ "</HEAD>\n" 
			+ "<BODY BGCOLOR=\"" 
			+ "white"
			+ "\">"
			+ SMUtilities.getLogoLink(getServletContext())
			;
			if (subtitle.compareTo("") != 0){
				sHeading += "<TABLE BORDER=0><TR>";
				sHeading += "<TD VALIGN=BOTTOM><H3>" + subtitle + "</H3></TD>";
			sHeading += "</TR></TABLE>";
			}
			out.println(sHeading);
		}else{
			out.println(SMUtilities.TitleSubBGColorWithLogo(
					subtitle, 
					SMUtilities.getInitBackGroundColor(getServletContext(), sConfFile),
					SMUtilities.DEFAULT_FONT_FAMILY,
					getServletContext()
					)
			);
			out.println("<BR>");
		}
		 */
		out.println(SMUtilities.getMenuHead(
				subtitle,
				SMUtilities.getInitBackGroundColor(getServletContext(), (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)),
				SMUtilities.DEFAULT_FONT_FAMILY,
				bMobileView,
				getServletContext()
		)
		);
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		//Print a link to the first page after login:
		if (bMobileView){
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG + "=" + sSessionTag 
				+ "\"  style=\"color: darkblue; font-weight:100; \">Return to user login</A><BR>");

		}else{
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG + "=" + sSessionTag 
				+ "\" >Return to user login</A><BR>");
		}
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
								+ " " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		if (!buildMenus(
				bMobileView,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID),
				sUserFullName,
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG),
				out,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
			out.println("Error building menus.");
		}
		out.println("</BODY></HTML>");
	}
	private boolean buildMenus(
			boolean bMobileView,
			String sUserName, 
			String sUserID, 
			String sUserFullName,
			String sConf, 
			String sSessionTag, 
			PrintWriter pwOut,
			String sLicenseModuleLevel
	){

		ArrayList<Long> arPermittedFunctions = new ArrayList<Long>(0);
		ArrayList<String> arPermittedFunctionNames = new ArrayList<String>(0);
		ArrayList<String> arPermittedFunctionLinks = new ArrayList<String>(0);
		ArrayList<Long> arMenu = new ArrayList<Long>(0); 

		//First, get a list of the permitted functions:
		try{
			String SQL = "SELECT DISTINCT "
				+ SMTablesecurityfunctions.TableName  + "." + SMTablesecurityfunctions.iFunctionID
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.slink
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.sFunctionName
				+ " FROM"
				+ " " + SMTablesecuritygroupfunctions.TableName 
				+ ", " + SMTablesecurityusergroups.TableName
				+ ", " + SMTablesecurityfunctions.TableName
				+ " WHERE (" 
				+ "(" + SMTablesecuritygroupfunctions.TableName + "." 
				+ SMTablesecuritygroupfunctions.sGroupName 
				+ " = " + SMTablesecurityusergroups.TableName + "." 
				+ SMTablesecurityusergroups.sSecurityGroupName + ")" 

				+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." 
				+ SMTablesecuritygroupfunctions.ifunctionid 
				+ " = " + SMTablesecurityfunctions.TableName + "." 
				+ SMTablesecurityfunctions.iFunctionID + ")" 

				+ " AND (" + SMTablesecurityusergroups.TableName + "." 
				+ SMTablesecurityusergroups.luserid + "=" + sUserID + ")"
				
				+ " AND ((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)"
				
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf,
					"MySQL",
					this.toString() + ".buildMenus - User: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);

			while(rs.next()){
				arPermittedFunctions.add(rs.getLong(SMTablesecurityfunctions.iFunctionID));
				arPermittedFunctionNames.add(rs.getString(SMTablesecurityfunctions.sFunctionName));
				arPermittedFunctionLinks.add(rs.getString(SMTablesecurityfunctions.slink));
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error getting list of permitted functions - " + e.getMessage());
			return false;
		}

		//Set up the layout:
		if (bMobileView){
			pwOut.println(SMUtilities.layoutMobileMenu());
		}

		//Build menus depending on the user's security levels:
		//Master Edit:
		arMenu.clear();
		
		arMenu.add(SMSystemFunctions.ASEditSSOptions);
		
		arMenu.add(SMSystemFunctions.ASEditControllers);
		
		arMenu.add(SMSystemFunctions.ASConfigureControllers);
		
		arMenu.add(SMSystemFunctions.ASControllerDiagnostics);
		
		arMenu.add(SMSystemFunctions.ASEditDevices);
		
		arMenu.add(SMSystemFunctions.ASEditAlarmSequences);
		
		arMenu.add(SMSystemFunctions.ASEditEventSchedules);
		
		arMenu.add(SMSystemFunctions.ASAuthorizeDeviceUsers);
		
		arMenu.add(SMSystemFunctions.ASAuthorizeAlarmSequenceUsers);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Master Edit",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Master Edit",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}
		//Processing
		arMenu.clear();
		
		arMenu.add(SMSystemFunctions.ASActivateDevices);
		
		arMenu.add(SMSystemFunctions.ASActivateAlarmSequences);
		
		arMenu.add(SMSystemFunctions.ASActivateSelectedSequences);
		
		//arMenu.add(SMSystemFunctions.ASArmZones);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Processing",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Processing",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}

		//Reports:
		arMenu.clear();
		
		arMenu.add(SMSystemFunctions.ASDeviceStatus);
		
		arMenu.add(SMSystemFunctions.ASListDeviceEventsByDate);
		
		arMenu.add(SMSystemFunctions.ASListUserEventsByDate);
		
		arMenu.add(SMSystemFunctions.ASListAllSSEventsByDate);
		
		arMenu.add(SMSystemFunctions.ASListAuthorizedDeviceUsers);
		
		arMenu.add(SMSystemFunctions.ASListAuthorizedAlarmSequenceUsers);
		
		arMenu.add(SMSystemFunctions.ASListOverridenDeviceEvents);
		
		arMenu.add(SMSystemFunctions.ASListUsersPermissions);
		
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
