package smas;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;
import SMDataDefinition.SMTablesscontrollers;
import SMDataDefinition.SMTablessdevices;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;

public class ASEditDevicesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSDevice.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSDevice entry = new SSDevice(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASEditDevicesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASEditDevices
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASEditDevices)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(OBJECT_NAME) != null){
	    	entry = (SSDevice) currentSession.getAttribute(OBJECT_NAME);
	    	currentSession.removeAttribute(OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserName(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASEditDevicesSelect"
							+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
				}
	    	}
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
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smas.ASEditDevicesSelect"
				+ "?" + SMTablessdevices.lid + "=" + entry.getslid()
				+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSDevice entry) throws Exception{

		String s = "<TABLE BORDER=1>";
		String sID = "NEW";
		if ((entry.getslid().compareToIgnoreCase("-1") != 0)){
			sID = entry.getslid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Device</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablessdevices.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".getEditHTML - user: " + sm.getUserName()
			);
		} catch (Exception e) {
			throw new Exception("Error [1458920376] getting connection - " + e.getMessage());
		}

		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
			SMTablessdevices.sdescription,
			entry.getsdescription().replace("\"", "&quot;"), 
			SMTablessdevices.sdescriptionlength, 
			"<B>Description: <FONT COLOR=RED>*Required*</FONT></B>",
			"Enter a description that clearly identifies the device, like 'Main office vestibule motion sensor'",
			"75"
		);

		//Active device?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				SMTablessdevices.iactive, 
				Integer.parseInt(entry.getsactive()), 
				"Is device active?", 
				"If unchecked, this device won't be included in activation lists and won't cause alarms to be triggered."
			);
		
		//Remarks:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
			SMTablessdevices.sremarks, 
			entry.getsremarks(), 
			"Remarks:", 
			"This will appear to the user, on the 'Activate' screen(s)", 
			3, 
			75);
		
		//SMTablessdevices.idevicetype
		ArrayList<String>arrDeviceTypeValues = new ArrayList<String>(0);
		ArrayList<String>arrDeviceTypeLabels = new ArrayList<String>(0);
		arrDeviceTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_TYPE_DOOR));
		arrDeviceTypeLabels.add(SMTablessdevices.getDeviceTypeLabel(SMTablessdevices.DEVICE_TYPE_DOOR));
		arrDeviceTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_TYPE_MOTIONSENSOR));
		arrDeviceTypeLabels.add(SMTablessdevices.getDeviceTypeLabel(SMTablessdevices.DEVICE_TYPE_MOTIONSENSOR));
		arrDeviceTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_TYPE_SMOKEDETECTOR));
		arrDeviceTypeLabels.add(SMTablessdevices.getDeviceTypeLabel(SMTablessdevices.DEVICE_TYPE_SMOKEDETECTOR));
		arrDeviceTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_TYPE_ONOFF));
		arrDeviceTypeLabels.add(SMTablessdevices.getDeviceTypeLabel(SMTablessdevices.DEVICE_TYPE_ONOFF));

		s += clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Row(
			SMTablessdevices.idevicetype, 
			"Type of device:", 
			"General on/off devices are things like an alarm bell, or a light, etc.", 
			arrDeviceTypeLabels, 
			arrDeviceTypeValues, 
			entry.getsdeviccetype()
		);

		//Get a list of controllers here:
		ArrayList<String>arrControllerIDs = new ArrayList<String>(0);
		ArrayList<String>arrControllerNames = new ArrayList<String>(0);
		arrControllerIDs.add("-1");
		arrControllerNames.add("(N/A)");
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " ORDER BY " + SMTablesscontrollers.scontrollername;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrControllerIDs.add(Long.toString(rs.getLong(SMTablesscontrollers.lid)));
				arrControllerNames.add(rs.getString(SMTablesscontrollers.scontrollername) 
								+ " - " + rs.getString(SMTablesscontrollers.sdescription));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1458928200] getting list of controllers with SQL: " + SQL + " - " + e.getMessage());
		}

		//Input controller:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			SMTablessdevices.linputcontrollerid, 
			arrControllerIDs, 
			entry.getsinputcontrollerid(), 
			arrControllerNames, 
			"Input Controller:", 
			"Choose the controller to which the the device's INPUT terminal is connected."
		);
		
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablessdevices.sinputterminalnumber,
				entry.getsinputterminalnumber().replace("\"", "&quot;"), 
				SMTablessdevices.sinputterminalnumberlength, 
				"<B>Input terminal:</B>",
				"This is the number on the INPUT controller terminal strip.",
				"30"
		);
		
		//Input type:
		ArrayList<String>arrInputTypeValues = new ArrayList<String>(0);
		ArrayList<String>arrInputTypeLabels = new ArrayList<String>(0);
		arrInputTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED));
		arrInputTypeLabels.add(SMTablessdevices.getInputTypeLabel(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED));
		arrInputTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN));
		arrInputTypeLabels.add(SMTablessdevices.getInputTypeLabel(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN));

		s += clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Row(
			SMTablessdevices.iinputtype, 
			"Type of input (sensor) contact:", 
			"If the input contacts for this device are normally closed and opening them indicates the 'ALARMED' or 'ACTIVE' state (for a door this would be 'Open',"
				+ ", for a motion sensor this would be 'Activated', etc.) choose " 
				+ "<B><I>" + SMTablessdevices.getInputTypeLabel(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED) + "</I></B>."
				+ "  If the input contacts are normally OPEN, and you want the contact CLOSING to indicate the 'ALARMED' or 'ACTIVE'"
				+ " state, then choose <B><I>" + SMTablessdevices.getInputTypeLabel(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN) + "</I></B>.",
				arrInputTypeLabels, 
				arrInputTypeValues, 
			entry.getsinputtype()
		);
		
		//Output controller:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			SMTablessdevices.loutputcontrollerid, 
			arrControllerIDs, 
			entry.getsoutputcontrollerid(), 
			arrControllerNames, 
			"Output Controller:", 
			"Choose the controller to which the the device's OUTPUT terminal is connected."
		);
		
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablessdevices.soutputterminalnumber,
				entry.getsoutputterminalnumber().replace("\"", "&quot;"), 
				SMTablessdevices.soutputterminalnumberlength, 
				"<B>Output terminal:</B>",
				"This is the number on the OUTPUT controller terminal strip.",
				"30"
		);
		
		//Activation type:
		//SMTablessdevices.idevicetype
		ArrayList<String>arrActivationTypeValues = new ArrayList<String>(0);
		ArrayList<String>arrActivationTypeLabels = new ArrayList<String>(0);
		arrActivationTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT));
		arrActivationTypeLabels.add(SMTablessdevices.getActivationTypeLabel(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT));
		arrActivationTypeValues.add(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT));
		arrActivationTypeLabels.add(SMTablessdevices.getActivationTypeLabel(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT));
	
		s += "<TR>";
		s += "<TD ALIGN=RIGHT><B>" + "Type of output (activation) contact:"  + " </B></TD>";

		s += "<TD ALIGN=LEFT>";

		s += clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Field(
				SMTablessdevices.iactivationtype, 
				arrActivationTypeLabels, 
				arrActivationTypeValues, 
				entry.getsactivationtype());
		
		String sContactDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;
		if(entry.getscontactduration().compareToIgnoreCase("") != 0){
			sContactDuration = entry.getscontactduration();
		}
		s += "&nbsp;&nbsp;"
			+ "<INPUT TYPE=\"TEXT\""
			+ " NAME=\"" + SMTablessdevices.icontactduration + "\""
			+ " VALUE=\"" + sContactDuration + "\""
			+ " STYLE=\"width:50px;\"> ms";
		s += "</TD>";

		s += "<TD ALIGN=LEFT>" 
				+ " Choose CONSTANT contact closure to provide a continuous contact when this device is ACTIVATED - contacts will stay closed indefinitely"
				+ " until the device is DEACTIVATED by a schedule, alarm sequence, or manually. (typically used for ON/OFF devices)" 
				+ "  Choose MOMENTARY contact closure to provide a duration in ms of contact closure on BOTH ACTIVATE and DEACTIVATE events."

				+ "</TD>";
		s += "</TR>";
		
		//End table
		s += "</TABLE>";
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067608]");
		
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
