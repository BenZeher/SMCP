package smas;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsDatabaseFunctions;

public class ASControllerDiagnosticsSelect extends HttpServlet {
	public static final String DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME = "DIAGFUNCTIONS";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_VALUE = "DIAGFUNCTIONVIEWORCLEARLOG";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_VALUE = "DIAGFUNCTIONSAMPLETELNETCOMMANDS";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_VALUE = "DIAGFUNCTIONPINMAPPINGS";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_VALUE = "DIAGFUNCTIONTESTMODE";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_LABEL = "View or delete controller log";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_LABEL = "List sample telnet commands for controller";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_LABEL = "View controller pin-to-terminal mappings";
	public static final String DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_LABEL = "Turn test mode on or off";
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smas.ASControllerDiagnosticsEdit";
	private static String OBJECT_NAME = SSController.ParamObjectName + " diagnostics";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASControllerDiagnostics
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ASControllerDiagnostics)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Alarm Systems Main Menu</A><BR>");
		smeditselect.showAddNewButton(false);
		smeditselect.showEditButton(false);
	    try {
	    	smeditselect.createEditForm(getEditHTML(smeditselect, request));
		} catch (SQLException e) {
    		smeditselect.getPrintWriter().println("Could not create edit form - " + e.getMessage());
    		smeditselect.getPrintWriter().println("</HTML>");
			return;
		}
	    return;

	}
	private String getEditHTML(SMMasterEditSelect smselect, HttpServletRequest req) throws SQLException{

		String s = "";
	    String sID = "";
	    if (req.getParameter(SMTablesscontrollers.lid) != null){
	    	sID = req.getParameter(SMTablesscontrollers.lid);
	    }
	    
	    s += 
	    	"<B>Controller:</B><BR>"
	    	+ "<SELECT NAME=\"" + SMTablesscontrollers.lid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select controller ***";
	    
	    //Drop down the list:
	    String SQL = "SELECT "
	    	+ " " + SMTablesscontrollers.lid
	    	+ ", " + SMTablesscontrollers.iactive
	    	+ ", " + SMTablesscontrollers.scontrollername
	    	+ ", " + SMTablesscontrollers.sdescription
	    	+ " FROM " + SMTablesscontrollers.TableName
	    	+ " ORDER BY " + SMTablesscontrollers.scontrollername
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smselect.getsDBID(), "MySQL", SMUtilities
							.getFullClassName(this.toString())
							+ ".getEditHTML - user: " + smselect.getUserID()
							+ " - "
							+ smselect.getFullUserName()
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTablesscontrollers.lid));
				String sInactive = "";
				if (rs.getInt(SMTablesscontrollers.iactive) == 0){
					sInactive = " (INACTIVE)";
				}
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" 
					+ rs.getString(SMTablesscontrollers.scontrollername) + " - "
					+ rs.getString(SMTablesscontrollers.sdescription)
					+ sInactive;
			}
			rs.close();
		} catch (SQLException e) {
			s += "</SELECT><BR><B>Error reading " + OBJECT_NAME + " data - " + e.getMessage() + "</B>";
		}
		s+= "</SELECT>";
		
		//Choices of functions:
		s += "<BR><BR><I><B>Select a diagnostic function below:</B></I>";
		
	    //diagnostic function types:
		
		s += "<BR>"
			+ "<INPUT TYPE=\"RADIO\""
			+ " NAME=\"" + DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "\""
			+ " VALUE=\"" + DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_VALUE	+ "\""
			+ " checked >"
			+ DIAGNOSTIC_FUNCTION_CHOICE_VIEW_OR_CLEAR_LOG_LABEL
			+ "<BR>" 
			
			+ "<INPUT TYPE=\"RADIO\""
			+ " NAME=\"" + DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "\""
			+ " VALUE=\"" + DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_VALUE + "\" >"
			+ DIAGNOSTIC_FUNCTION_CHOICE_VIEW_SAMPLE_TELNET_COMMANDS_LABEL
			+ "<BR>" 
			
			+ "<INPUT TYPE=\"RADIO\""
			+ " NAME=\"" + DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "\""
			+ " VALUE=\"" + DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_VALUE + "\" >"
			+ DIAGNOSTIC_FUNCTION_CHOICE_VIEW_PIN_MAPPINGS_LABEL
			+ "<BR>" 
			
			+ "<INPUT TYPE=\"RADIO\""
			+ " NAME=\"" + DIAGNOSTIC_FUNCTION_RADIO_GROUP_NAME + "\""
			+ " VALUE=\"" + DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_VALUE + "\" >"
			+ DIAGNOSTIC_FUNCTION_CHOICE_TEST_MODE_LABEL
			+ "<BR>" 
		;
		s += "<P><INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='View selected diagnostic function' STYLE='height: 0.24in'>"
		;
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}