package smas;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLTableFormFields;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;

public class ASConfigureControllerEdit  extends HttpServlet {

	public static final String UPDATE_CONTROLLER_SOFTWARE_BUTTON_NAME = "UPDATEPROGRAM";
	public static final String UPDATE_CONTROLLER_SOFTWARE_BUTTON_LABEL = "Update controller with latest software version";
	public static final String UPDATE_CONTROLLER_SOFTWARE_CONFIRM_NAME = "UPDATECHECK";
	public static final String RESTART_CONTROLLER_BUTTON_NAME = "RESTARTCONTROLLER";
	public static final String RESTART_CONTROLLER_BUTTON_LABEL = "Restart the controller";
	public static final String RESTART_CONTROLLER_CONFIRM_NAME = "RESTARTCHECK";
	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SSController entry = new SSController(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				OBJECT_NAME + " configuration",
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASConfigureControllerAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASConfigureControllers
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASConfigureControllers)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a Security System Controller object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		if (currentSession.getAttribute(OBJECT_NAME) != null){
			entry = (SSController) currentSession.getAttribute(OBJECT_NAME);
			currentSession.removeAttribute(OBJECT_NAME);
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			try {
				entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASConfigureControllerSelect"
								+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
								+ "&Warning=" + e.getMessage()
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
				return;
			}
		}

		//NOW try to read the controller itself and get its data:
		try {
			entry.readControllerConfiguration(
					smedit.getUserName(), 
					SMUtilities.getUserFullName(
							smedit.getUserName(), 
							getServletContext(), 
							smedit.getsDBID(), 
							SMUtilities.getFullClassName(this.toString()) + ".doPost"),
							getServletContext(),
							SMBackgroundScheduleProcessor.getServerID(getServletContext())
					);
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASConfigureControllerSelect"
							+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
							+ "&Warning=" + clsServletUtilities.URLEncode(e1.getMessage())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
			return;
		}

		smedit.printHeaderTable();

		//Add a link to return to the original URL:
		if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
			smedit.getPWOut().println(
					"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
							+ "Back to report" + "</A>");
		}

		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ smedit.getsDBID() + "\">Return to Alarm Systems Main Menu</A><BR>");

		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);
		try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
					+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getslid() + " - " + sError
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
			return;
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSController entry) throws SQLException{

		String s = "<TABLE BORDER=1>";
		String sID = entry.getslid();
		s += "<TR><TD ALIGN=RIGHT><B>Controller</B>:</TD><TD><B>" 
				+ sID 
				+ " " 
				+ entry.getscontrollername()
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.lid + "\" VALUE=\"" 
				+ entry.getslid() + "\">"
				+ "</B></TD><TD>" + entry.getsdescription() + "</TD></TR>";

		//Version:
		s += "<TR><TD ALIGN=RIGHT><B>Controller current<BR>software version</B>:</TD>"
				+ "<TD><B>" + entry.getcontrollersoftwareversion() + "</B></TD>"
				+ "<TD>Version on SMCP server is " + SSConstants.CONTROLLER_VERSION + "</TD></TR>";


		//Update the controller software version?
		int iCurrentControllerSoftwareVersion = 0;
		try {
			iCurrentControllerSoftwareVersion = Integer.parseInt(entry.getcontrollersoftwareversion());
		} catch (NumberFormatException e) {
			s += "<BR><FONT COLOR=RED><B>The software version in the live controller could not be read correctly.</B></FONT><BR>";
		}

		if (
				(iCurrentControllerSoftwareVersion > 0)
				&& (Integer.parseInt(SSConstants.CONTROLLER_VERSION) > iCurrentControllerSoftwareVersion)
				){
			s += "<TR><TD ALIGN=RIGHT><B>Update controller?</B>:</TD>"
					+ "<TD>";
			//Display a button and confirmation to update the controller's software:
			s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + UPDATE_CONTROLLER_SOFTWARE_BUTTON_NAME + "'"
					+ " VALUE='" + UPDATE_CONTROLLER_SOFTWARE_BUTTON_LABEL + "'" 
					+ " STYLE='height: 0.24in'>";
			s += "  Check to confirm program update: <INPUT TYPE=CHECKBOX NAME=\"" 
					+ UPDATE_CONTROLLER_SOFTWARE_CONFIRM_NAME + "\"></P>";
			s += "</TD>";

			s += "<TD>Use this to upgrade the controller's software to the newest version on the SMCP server.</TD>";
			s += "</TR>";
		}

		s += "<TR><TD ALIGN=RIGHT><B>Restart controller?</B>:</TD>"
				+ "<TD>";
		//Display a button and confirmation to restart:
		s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + RESTART_CONTROLLER_BUTTON_NAME + "'"
				+ " VALUE='" + RESTART_CONTROLLER_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>";
		s += "  Check to confirm controller restart: <INPUT TYPE=CHECKBOX NAME=\"" 
				+ RESTART_CONTROLLER_CONFIRM_NAME + "\"></P>";
		s += "</TD>";

		s += "<TD>Use this to restart the controller.</TD>";
		s += "</TR>";

		//Raspberry pi hostname:
		s += "<TR><TD ALIGN=RIGHT><B>Controller's host name</B>:</TD>"
				+ "<TD>";
		//Display a button and confirmation to update the controller's software:
		s += entry.getcontrollerhostname();
		s += "</TD>";

		s += "<TD>This is the hostname of the Raspberry Pi powering this controller.</TD>";
		s += "</TR>";

		//Controller name
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_KEY_NAME,
				entry.getsconfigfilecontrollername().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_KEY_NAME_MAX_LENGTH, 
				"<B>Controller name: </B>",
				"The program will automatically update this field in the 'Edit controllers' function when you update here.",
				"75"
				);

		//Passcode
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_KEY_PASSCODE,
				entry.getsconfigfilepasscode().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_KEY_PASSCODE_MAX_LENGTH, 
				"<B>Controller passcode: </B>",
				"The program will automatically update this field in the 'Edit controllers' function when you update here.",
				"75"
				);

		//Controller listening port:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_LISTENING_PORT,
				entry.getsconfigfilelisteningport().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_LISTENING_PORT_MAX_LENGTH, 
				"<B>Controller's listening port: </B>",
				"This is the port on which the controller itself will listen.  <FONT COLOR=RED><B>"
				+ "If you are port forwarding to the controller, do NOT enter the FORWARDING port here, enter the"
				+ " actual port on which the controller is listening.</B></FONT>",
				"75"
				);

		//Web app URL
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_WEB_APP_URL,
				entry.getsconfigfilewebappurl().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_WEB_APP_URL_MAX_LENGTH, 
				"<B>SMCP URL: </B>",
				"This is the URL used by the controller to connect with the SMCP web server",
				"75"
				);

		//Web App port:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_WEB_APP_PORT,
				entry.getsconfigfilewebappport().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_WEB_APP_PORT_MAX_LENGTH, 
				"<B>SMCP listening port: </B>",
				"This is the port used by the controller to connect with the SMCP web server",
				"75"
				);

		//DatabaseID
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_DATABASE_ID,
				entry.getsconfigfiledatabaseid().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_DATABASE_ID_MAX_LENGTH, 
				"<B>Database ID: </B>",
				"This should be the database ID associated with the current company (" + sm.getsDBID() + ").",
				"75"
				);

		//Log file:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SSConstants.CONFFILE_LOG_FILE,
				entry.getsconfigfilelogfilename().replace("\"", "&quot;"), 
				SSConstants.CONFFILE_LOG_FILE_MAX_LENGTH, 
				"<B>Controller log file: </B>",
				"This is the the full name of the log file on the controller",
				"75"
				);

		//Logging level:
		ArrayList<String> arrValues = new ArrayList<String>(0);
		arrValues.add("0");
		arrValues.add("1");
		arrValues.add("2");
		arrValues.add("3");
		ArrayList<String> arrLabels = new ArrayList<String>(0);
		arrLabels.add("0 - No logging");
		arrLabels.add("1 - Minimal logging");
		arrLabels.add("2 - Standard logging");
		arrLabels.add("3 - Maximum logging");
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SSConstants.CONFFILE_LOGGING_LEVEL, 
				arrValues, 
				entry.getsconfigfilelogginglevel().replace("\"", "&quot;"), 
				arrLabels, 
				"<B>Logging level: </B>", 
				"The higher the number, the more verbose the logging the controller will do."
				);

		s += "</TABLE>";

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
