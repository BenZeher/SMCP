package smas;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ASControllerDiagnosticsEdit  extends HttpServlet {

	public static final String TURN_TEST_MODE_ON_BUTTON = "TURNTESTMODEON";
	public static final String TURN_TEST_MODE_OFF_BUTTON = "TURNTESTMODEOFF";
	public static final String TEST_MODE_CHANGE_CONFIRM = "CONFIRMTESTMODECHANGE";
	
	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSController entry = new SSController(request);
		//System.out.println("[1463764293] - lid = " + entry.getslid());
		//System.out.println("[1463764296] - querystring = '" + request.getQueryString() + "'");
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASControllerDiagnosticsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASControllerDiagnostics
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASControllerDiagnostics)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
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
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" 
					+ smedit.getSessionTag() + "\">Return to Alarm Systems Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
	    
		//Try to load the entry first:
		try {
			entry.load(getServletContext(), smedit.getConfFile(), smedit.getUserID(), smedit.getFullUserName());
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
					+ "&Warning=" + "Error [1463762491] could not load controller - " + clsServletUtilities.URLEncode(e1.getMessage())
					+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
				);
				return;
		}
		String sDiagnosticFunction = clsManageRequestParameters.get_Request_Parameter(
			ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME, request);
		try {
			smedit.createEditPage(getEditHTML(smedit, entry, sDiagnosticFunction, SMBackgroundScheduleProcessor.getServerID(getServletContext())), "");
		} catch (SQLException e) {
    		String sError = "Could not create log page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
				+ "&Warning=" + sError
				+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSController entry, String sDiagnosticFunction, String sServerID) throws SQLException{

		String s = "";
		
		//Store the diagnostic function selection:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "\" VALUE=\"" 
				+ sDiagnosticFunction + "\">";
		//If it's the controller log, give an option to delete it:
		
		if (sDiagnosticFunction.compareToIgnoreCase(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_VALUE) == 0){
			s += "<INPUT TYPE=SUBMIT NAME='" + SMMasterEditEntry.SUBMIT_DELETE_BUTTON_NAME + "' VALUE='Delete controller log'" 
					+ " STYLE='height: 0.24in'>";
			s += "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
					+ SMMasterEditEntry.CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>";
		}

		//This is the default:
		String sTitle = "Controller's diagnostic log";
		if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_VALUE) == 0){
			sTitle = "Diagnostic telnet commands for controller";
		}
		if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_VALUE) == 0){
			sTitle = "Controller's current pin-to-terminal mappings";
		}
		
		if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_VALUE) == 0){
			sTitle = "Turn test mode on or off for the controller";
		}
		
		//TODO.....
		s += "<TABLE BORDER=1>";
		s += "<TR><TD>Controller ID:" 
				+ " <B><I>" + entry.getslid() + "</I></B>" 
				+ " " 
				+ "Name: <B><I>" + entry.getscontrollername() + "</I></B>"
				+ " Description: <B><I>" + entry.getsdescription() + "</I></B>"
				+ " - " + sTitle + ":"

				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.lid + "\" VALUE=\"" 
				+ entry.getslid() + "\">"
				+ "</TD></TR>";
		
		//If we are turning test mode on or off, we'll do that here
		if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_VALUE) == 0){
			sTitle = "Turn test mode on or off for the controller";
			
			boolean bTestModeOn = false;
			try {
				bTestModeOn = entry.getTestModeState(getServletContext(), sm.getConfFile(), sm.getUserName(), sm.getUserID(), sm.getFullUserName(),  sServerID);
			} catch (Exception e) {
				s += "<B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B>";
			}
			s += "<TR><TD>'Test mode' sets the controller in a special mode that works as follows:<BR>"
					+ "Connecting a pair of 'input' terminals (with a jumper, for example) will activate the"
					+ " LED on the board of the output relay which is 32 terminal numbers HIGHER than the"
					+ " number of the input terminal being shorted.  For example, in test mode, if you short"
					+ " terminal number 11, the output LED for terminal 43 will light, and the corresponding"
					+ " output relay will be energized.<BR><BR>"
					+ " The purpose is to be able to confirm that each input and each output on the board is"
					+ " working as designed. Test mode cannot be turned on if any of the input terminals are in 'listening' mode"
					+ " or if any of the output terminals are active (contacts are closed)."
					+ "</TD>"
					+ " </TR>"
				;
			if (bTestModeOn){
				s += "<TR><TD>Test mode is currently <B><FONT COLOR=RED>" + "ON" + "</FONT></B>.";
				s += "&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='" + TURN_TEST_MODE_OFF_BUTTON + "' VALUE='Turn test mode OFF'" 
						+ " STYLE='height: 0.24in'>";
				s += "  Check to confirm: <INPUT TYPE=CHECKBOX NAME=\"" 
						+ TEST_MODE_CHANGE_CONFIRM + "\"></P>";
			}else{
				s += "<TR><TD>Test mode is currently <B><FONT COLOR=RED>" + "OFF" + "</FONT></B>.";
				s += "&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='" + TURN_TEST_MODE_ON_BUTTON + "' VALUE='Turn test mode ON'" 
						+ " STYLE='height: 0.24in'>";
				s += "  Check to confirm: <INPUT TYPE=CHECKBOX NAME=\"" 
						+ TEST_MODE_CHANGE_CONFIRM + "\"></P>";
			}
			
		}else{
			//But if it's one of the other functions, then we need to display a body of text
			//Display text here:
			String sText = "";
			try {
				if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_VALUE) == 0){
					sText = entry.getLogFile(getServletContext(), sm.getConfFile(), sm.getUserName(), sm.getUserID(), sm.getFullUserName(), sServerID).replace("&", "&amp;");
				}
				if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_VALUE) == 0){
					sText = entry.getSampleCommands(getServletContext(), sm.getConfFile(), sm.getUserName(), sm.getUserID(), sm.getFullUserName(), sServerID).replace("&", "&amp;");
				}
				if (sDiagnosticFunction.compareTo(ASControllerDiagnosticsSelect.DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_VALUE) == 0){
					sText = entry.getTerminalMappings(getServletContext(), sm.getConfFile(), sm.getUserName(), sm.getUserID(), sm.getFullUserName(),  sServerID).replace("&", "&amp;");
				}
			} catch (Exception e1) {
				s += "<B><FONT COLOR=RED>" + e1.getMessage() + "</FONT></B>";
			}
			s += "<TR><TD style = \" font-size:small; font-family:Courier; background-color:white; \" >"
				+ sText
				+ "<TD>"
				+ "</TR>"
			;
		}
		
		s += "</TABLE>";
		
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
