package smas;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMGoogleMapAPIKey;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ASActivateDevicesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DARK_ROW_BG_COLOR = "#cceeff";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	private static final String ACTIVATE_DEVICE_BUTTON_NAME = "ACTIVATE";
	public static final String ACTIVATE_DEVICE_PARAMETER = "PARAMACTIVEDEVICE";
	public static final String ACTIVATE_DEVICE_VALUE_ACTIVATE = "ACTIVATEDEVICE";
	public static final String ACTIVATE_DEVICE_VALUE_DEACTIVATE = "DEACTIVATEDEVICE";
	public static final String CONFIRM_CHECKBOX_NAME_PREFIX = "CHKCONFIRM";
	public static final String PARAMETER_USER_LATITUDE = "USERLATITUDE";
	public static final String PARAMETER_USER_LONGITUDE = "USERLONGITUDE";
	public static final String PARAMETER_CURRENT_INPUT_ACTIVATION_STATE = "CURRENTACTIVATIONINPUTSTATE";
	public static final String PARAMETER_CURRENT_OUTPUT_ACTIVATION_STATE = "CURRENTACTIVATIONOUTPUTSTATE";
	public static final String PARAMETER_LOCATION_RECORDING_ALLOWED = "LOCATIONRECORDINGALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED = "NOTALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED = "ALLOWED";
	//private static final int PAGE_REFRESH_INTERVAL_IN_SECONDS = 20;

	private static boolean bDebugMode = false;
	//private static final String OBJECT_NAME = SSDevice.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Device Activation",
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASActivateDevicesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASActivateDevices
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASActivateDevices)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
		//smedit.setPageRefreshIntervalInSeconds(PAGE_REFRESH_INTERVAL_IN_SECONDS);
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
		smedit.getPWOut().println("<I>This screen lists all of the ACTIVE devices (on ACTIVE controllers) which you are authorized to control.  Only devices with 'OUTPUT' terminals"
				+ " are listed here, since they are the only ones that can be controlled.</I>"
				);

		try {
			smedit.createEditPage(getEditHTML(smedit, bDebugMode, SMBackgroundScheduleProcessor.getServerID(getServletContext())), "");
		} catch (Exception e) {
			String sError = "Could not create edit page - " + e.getMessage();
			smedit.getPWOut().println("<HTML><BR><FONT COLOR=RED><B>WARNING - " + sError + "</B><FONT COLOR = RED><BR></HTML>");
			return;
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, boolean bOutputDiagnostics, String sServerID) throws Exception{
		String s = "";
		SSOptions ssopt = new SSOptions();
		ssopt.load(getServletContext(), sm.getsDBID(), sm.getUserName());

		s += "\n" + sStyleScripts() + "\n";
		s += "\n" + sCommandScripts(ssopt.getstrackuserlocation().compareToIgnoreCase("1") == 0) + "\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + SMTablessdevices.lid + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMTablessdevices.lid + "\""
				+ "\">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + ACTIVATE_DEVICE_PARAMETER + "\" VALUE=\"" + ACTIVATE_DEVICE_VALUE_ACTIVATE + "\""
				+ " id=\"" + ACTIVATE_DEVICE_PARAMETER + "\""
				+ "\">\n";

		//Values for geocodes:
		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LATITUDE + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_USER_LATITUDE + "\""
				+ "\">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LONGITUDE + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_USER_LONGITUDE + "\""
				+ "\">\n";
		
		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\""
				+ "\">\n";
		
		s += "<TABLE class = \" basicwithborder \" >";

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + ".getEditHTML - userID: " + sm.getUserID()
					);
		} catch (Exception e) {
			throw new Exception("Error [1458920376] getting connection - " + e.getMessage());
		}

		//Display the header row:
		s += createHeaderRow();

		s += createDeviceRows(sm, bOutputDiagnostics, sServerID);

		s += "</TABLE>";

		s += createFootNotes();

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);

		return s;
	}
	private String createHeaderRow(){
		String s = "";

		s += "<TR style = \" background-color: LightGray; color: black; \" >";

		s += "<TD class = \" centerjustifiedheading \" >" + "Issue command<SUP><a href=\"#issuecommand\">1</a></SUP>" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Confirm?" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Device description" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Device ID" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Current state<SUP><a href=\"#currentstate\">2</a></SUP>" + "</TD>"
				+ "</TR>"
				;
		return s;
	}
	private String createDeviceRows(SMMasterEditEntry sm, boolean bOutputDiagnostics, String sServerID) throws Exception{
		String s = "";
		boolean bOddRow = true;

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".createDeviceRows - user: " + sm.getUserName())
					);
		} catch (Exception e) {
			throw new Exception("Error [1459969436] getting connection to create device rows - " + e.getMessage());
		}

		//Get a collection of the devices this user has access to and the state of each:
		ArrayList<SSDevice>arrDevices = new ArrayList<SSDevice>(0);
		try {
			arrDevices = getUsersDevices(conn, sm.getUserName(), sm.getUserID(), bOutputDiagnostics, sServerID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception(e.getMessage());
		}

		boolean bAllowDeviceEdit;
		try {
			bAllowDeviceEdit = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ASEditDevices, 
					sm.getUserID(), 
					conn,
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error [1463431225] checking device editing permissions - " + e.getMessage());
		}

		for (int i = 0; i < arrDevices.size(); i++){
			String sBackgroundColor = "";

			//We only want to list devices that can be 'activated', so we only want ones with an output terminal:
			if (arrDevices.get(i).getsoutputterminalnumber().compareToIgnoreCase("") != 0){

				if (bOddRow){
					sBackgroundColor = LIGHT_ROW_BG_COLOR;
				}else{
					sBackgroundColor = DARK_ROW_BG_COLOR;
				}

				s += "<TR style = \" background-color: " + sBackgroundColor +  "; \">"; //line-height: 60px; padding: 10px;
				s+= "<TD class = \" centerjustifiedcell \">"
						//build a button to activate:
						+ "&nbsp;"
						+ createActivateButton(arrDevices.get(i))
						+ "&nbsp;"
						+ "</TD>"
						;
				s+= "<TD class = \" centerjustifiedcell \" >"
						//checkbox to confirm:
						+ "<INPUT TYPE=CHECKBOX "
						+ " NAME=\"" + CONFIRM_CHECKBOX_NAME_PREFIX	
						+ arrDevices.get(i).getslid() + "\""
						+ " id = \"" + CONFIRM_CHECKBOX_NAME_PREFIX	
						+ arrDevices.get(i).getslid() + "\""
						//+ " width=0.25"
						+ ">"
						+ "</TD>"
						;
				String sDeviceIDLink = arrDevices.get(i).getslid();
				if (bAllowDeviceEdit){
					sDeviceIDLink = "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smas.ASEditDevicesEdit?" 
							+ SMTablessdevices.lid + "=" + arrDevices.get(i).getslid()
							+ "\">" + sDeviceIDLink + "</A>"
							;
				}
				String sRemarks = "";
				if (arrDevices.get(i).getsremarks().compareToIgnoreCase("") != 0){
					sRemarks = "<p><span style = \" font-size: small; color: grey; \">("
							+ arrDevices.get(i).getsremarks()
							+ ")</span></p>"
							;
				}

				s+= "<TD class = \" leftjustifiedcell \">"
						+ "<I>"
						+ arrDevices.get(i).getsdescription()
						+ "</I>"
						+ sRemarks
						+ "</TD>"
						;

				s+= "<TD class = \" centerjustifiedcell \">"
						+ sDeviceIDLink
						+ "</TD>"
						;

				String sBackGroundColor = "gray";

				//If the activation state of the device can't be determined, this variable can be set to null to indicate that:
				Boolean bIsActivated;
				try {
					bIsActivated = arrDevices.get(i).getIsActivated();
				} catch (Exception e) {
					bIsActivated = null;
				}

				String sCurrentInputActivationState = SMTablessdevices.getDeviceActivationStateLabel(
						Integer.parseInt(arrDevices.get(i).getsdeviccetype()), 
						bIsActivated
						);

				try {
					if (arrDevices.get(i).getIsActivated()){
						sBackGroundColor = "red";
					}else{
						sBackGroundColor = "green";
					}
				} catch (Exception e) {
					//If we can't determine the activation status, then set it to gray:
					sBackGroundColor = "gray";
				}

				s+= "<TD class = \" centerjustifiedcell \" style = \" background-color: " + sBackGroundColor + "; color: white; \" >"
						+ sCurrentInputActivationState
						+ "<INPUT TYPE=HIDDEN NAME=\"" 
						+ arrDevices.get(i).getslid() + PARAMETER_CURRENT_INPUT_ACTIVATION_STATE 
						+ "\" VALUE=\"" + sCurrentInputActivationState + "\""
						+ " id=\"" + arrDevices.get(i).getslid() + PARAMETER_CURRENT_INPUT_ACTIVATION_STATE + "\""
						+ "\""
						+ "</TD>"
						;

				bOddRow = !bOddRow;
			}
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		return s;
	}
	private String createFootNotes(){
		String s = "<BR>";
		
		s += "<a name=\"issuecommand\"><SUP>1</SUP></a><B>Issue command</B>: "
				+ "If the output device is momentary contact, the output contact will stay closed for the time specified for that device."
				+ " If the output device is constant contact, the output contact will stay closed indefinitely untill the device is tured off or the controller is turned off. "
				+ " To close (or open) contacts by manually entering the duration, you can use the 'AS Device Status' screen."
				+ "<BR>"
				;

		s += "<a name=\"currentstate\"><SUP>2</SUP></a><B>Current state</B>: "
				+ "If the device has an input terminal, which is used to monitor the state of the device,"
				+ " then the current state is determined by the status of that input."
				+ "  For example, if the device has a normally OPEN contact, and the input terminal is CLOSED, the device is considered"
				+ " 'OPEN' or 'ON' (activated) or whatever the 'NOT NORMAL' state is defined to be.\n"
				+ "If the device does NOT have an input terminal then the output terminal's contact state is simply used to determine the current state."
				+ "  If the output contacts are CLOSED, the device is considered in its 'NOT NORMAL' (activated) state (e.g. 'OPEN' or 'ON', etc.)"
				+ " and if they are OPEN the device is considered in it's 'NORMAL' (de-activated) state."
				+ "<BR>"
				;

		//s += "<a name=\"activated\"><SUP>2</SUP></a><B>Activated?</B>: This indicates whether the device's OUTPUT TERMINALS are currently activated by the program."
		//	+ "  If the controller has CURRENTLY CLOSED the 'output' contacts (i.e., if the contacts are STILL CLOSED) to the device,"
		//	+ " then it is considered 'ACTIVATED'."
		//	+ "  If the device is not configured with an OUTPUT (as with a motion sensor or a manual door 'open' switch),"
		//	+ " then this doesn't apply ('N/A')."
		//	;

		return s;
	}
	private ArrayList<SSDevice> getUsersDevices(Connection conn, String sUser, String sUserID, boolean bOutputDiagnostics, String sServerID) throws Exception{
		ArrayList<SSDevice>arrDevices = new ArrayList<SSDevice>(0);

		//First get the status of ALL the input terminals of ALL the input controllers from devices this user is authorized to use:

		//We first get all the controllers that are needed to read the current 'activation' status.
		//We do this so we can send one single command to each to get the status of ALL the terminals in each controller.
		String SQL = "SELECT DISTINCT"
				+ " " + SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid 
				+ " FROM " + SMTablessdevices.TableName
				+ " LEFT JOIN " + SMTablessdeviceusers.TableName + " ON "
				+ SMTablessdevices.TableName + "." + SMTablessdevices.lid + " = "
				+ SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid
				+ " WHERE ("
				+ "(" + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid + " IS NOT NULL)"
				+ " AND (" + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.luserid + " = " + sUserID + ")"
				+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.iactive + " = 1)"
				+ ")"
				;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//If it's a valid controller ID, then load that controller:
				if (rs.getLong(SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid) > 0){
					SSController controller = new SSController();
					controller.setslid(Long.toString(rs.getLong(SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid)));
					controller.load(conn);
					//We only contact the controller if it's active:
					if (controller.getsactive().compareToIgnoreCase("1") == 0){
						//Now get a list of the controller's devices,
						//We'll tell the function that we need the activation statuses updated, too:
						boolean bUpdateActivationStatusOnTerminals = true;

						//(This function will load the controller when it runs:)
						ArrayList<SSDevice> controllerdevices = controller.getDeviceList(
								conn, bUpdateActivationStatusOnTerminals, bOutputDiagnostics, sUser, getServletContext(), sServerID);
						//Now for each of the controller devices, either add it to the device list OR if the
						//device is already in the list, then just update the activation status if necessary
						for (int i = 0; i < controllerdevices.size(); i++){
							//If the device is already in the list, which is possible because a device can have terminals
							//in more than one controller, then we just update the activation status of that existing device.  
							// If it's not already in the list, then we add it:
							boolean bDeviceIsAlreadyInList = false;
							for (int j = 0; j < arrDevices.size(); j++){
								//If the device is already in the list, update the status and pin info:
								if (arrDevices.get(j).getslid().compareToIgnoreCase(controllerdevices.get(i).getslid()) == 0){
									//If the controller's device has an activation status, update the device in the 'devices' array
									//with that.  
									//This is because it's possible that one controller may have already listed the device because the device
									//used this controller for its OUTPUT terminal but not its INPUT terminal.  So that controller wouldn't
									//know the status of the input terminal, and it would be blank.
									if (controllerdevices.get(i).getinputcontactstatus().compareToIgnoreCase("") != 0){
										arrDevices.get(j).setinputcontactstatus(controllerdevices.get(i).getinputcontactstatus());
									}
									bDeviceIsAlreadyInList = true;
								}
							}
							//If the controller's device is not already in the list, then go on and add it:
							if (!bDeviceIsAlreadyInList){
								//Only add ACTIVE devices to the list:
								if (controllerdevices.get(i).getsactive().compareToIgnoreCase("1") == 0){
									arrDevices.add(controllerdevices.get(i));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception ("Error [1459949912] reading controllers for devices - " + e.getMessage());
		}

		//Next get all the OUTPUT controllers, so we can list their devices, too.
		SQL = "SELECT DISTINCT"
				+ " " + SMTablessdevices.TableName + "." + SMTablessdevices.loutputcontrollerid 
				+ " FROM " + SMTablessdevices.TableName
				+ " LEFT JOIN " + SMTablessdeviceusers.TableName + " ON "
				+ SMTablessdevices.TableName + "." + SMTablessdevices.lid + " = "
				+ SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid
				+ " WHERE ("
				+ "(" + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid + " IS NOT NULL)"
				+ " AND (" + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.luserid + " = " + sUserID + ")"
				+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.iactive + " = 1)"
				+ ")"
				;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//If the output controller ID is used, then load that controller:
				if (rs.getLong(SMTablessdevices.TableName + "." + SMTablessdevices.loutputcontrollerid) > 0){
					SSController controller = new SSController();
					controller.setslid(Long.toString(rs.getLong(SMTablessdevices.TableName + "." + SMTablessdevices.loutputcontrollerid)));
					controller.load(conn);
					//We only contact the controller if it's active:
					if (controller.getsactive().compareToIgnoreCase("1") == 0){
						//We don't care about the activation statuses because we already got those in the previous loop:
						boolean bUpdateActivationStatusOnTerminals = true;

						//(This function will load the controller first:)
						ArrayList<SSDevice> controllerdevices = controller.getDeviceList(
								conn, 
								bUpdateActivationStatusOnTerminals, 
								bOutputDiagnostics,
								sUser,
								getServletContext(),
								sServerID);
						boolean bDeviceIsAlreadyInList = false;
						for (int i = 0; i < controllerdevices.size(); i++){
							//If the device is already in the list, which is possible because a device can have terminals
							//in more than one controller, then we don't have to do anything.  If it's not already
							//in the list, then we add it:
							for (int j = 0; j < arrDevices.size(); j++){
								//If the device is already in the list, update the status and pin info:
								if (arrDevices.get(j).getslid().compareToIgnoreCase(controllerdevices.get(i).getslid()) == 0){
									//Since these are OUTPUT controllers, we don't worry here what the input terminal's status is.
									//So if the device is already in the list, we don't need to add it again:
									bDeviceIsAlreadyInList  = true;
								}
							}
							if (!bDeviceIsAlreadyInList){
								//But if the device is NOT yet in the list, just add it:
								//Only add ACTIVE devices to the list:
								if (controllerdevices.get(i).getsactive().compareToIgnoreCase("1") == 0){
									arrDevices.add(controllerdevices.get(i));							
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception ("Error [1459949913] reading controllers for devices - " + e.getMessage());
		}

		//Now remove any devices that this user is not authorized to use:
		ArrayList<SSDevice>arrPermittedDevices = new ArrayList<SSDevice>(0);
		SQL = "SELECT"
				+ " " + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid
				+ " FROM " + SMTablessdeviceusers.TableName
				+ " WHERE ("
				+ "(" + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.luserid + " = " + sUserID + ")"
				+ ")"
				;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				for (int i = 0; i < arrDevices.size(); i++){
					if (arrDevices.get(i).getslid().compareToIgnoreCase(
							Long.toString(rs.getLong(SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid))) == 0){
						arrPermittedDevices.add(arrDevices.get(i));
						break;
					}
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1460053584] reading devices not authorized - " + e.getMessage());
		}

		//Sort devices:
		ArrayList<SSDevice>arrSortedDevices = new ArrayList<SSDevice>(0);
		int iIndexOfFirstDeviceInSortingOrder;
		while(arrPermittedDevices.size() > 0){
			iIndexOfFirstDeviceInSortingOrder = 0;
			for (int i = 0; i < arrPermittedDevices.size(); i++){
				if (arrPermittedDevices.get(i).getsdescription().compareTo(arrPermittedDevices.get(iIndexOfFirstDeviceInSortingOrder).getsdescription()) < 0){
					iIndexOfFirstDeviceInSortingOrder = i;
				}
			}
			arrSortedDevices.add(arrPermittedDevices.get(iIndexOfFirstDeviceInSortingOrder));
			arrPermittedDevices.remove(iIndexOfFirstDeviceInSortingOrder);
		}

		if (bOutputDiagnostics){
			for (int i = 0; i < arrSortedDevices.size(); i++){
				System.out.println("[1459991559] - arrPermittedDevices[" + i + "] = \n" 
						+ "Description = " + arrDevices.get(i).getsdescription() + "\n"
						+ "Activation status = " + arrDevices.get(i).getinputcontactstatus()
						);
			}
		}
		return arrSortedDevices;
	}
	private String createActivateButton(SSDevice device){
		//If the devices are currently 'active', then we want a 'deactivate' command to appear on this button, or vice versa.
		String sCommandLabel = "";
		//First, if the device has an input terminal, then we use that to determine the 'state' of the device, and
		//so we label the button to activate it accordingly:
		sCommandLabel = device.getCommandLabelForActivatingAndDeactivating();

		String s = "<button type=\"button\""
				+ " style = \"" 
				+ "background-color: #4CAF50;"
				+ "border: none;"
				+ "color: white;"
				+ "padding: 6px 6px;"
				+ "text-align: center;"
				+ "text-decoration: none;"
				+ "display: inline-block;"
				+ "font-size: 16px; "
				+ "box-shadow: 0 12px 16px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);"
				+ "\""
				+ " value=\"" + sCommandLabel + "\""
				+ " name=\"" + ACTIVATE_DEVICE_BUTTON_NAME + "\""
				;
		s += " onClick=\"activatedevice(" + device.getslid() + ");\">"
				+  sCommandLabel 
				+ "</button>\n"
				;
		return s;
	}
	private String sCommandScripts(boolean bTrackUserLocation){
		String s = "";

		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
				;

		//Geocode functions:
		s += "  <script type=\"text/javascript\"\n"
				//+ "    src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">\n"
				+ "    src=\"https://maps.googleapis.com/maps/api/js?key=" + SMGoogleMapAPIKey.SMCP_GMAPS_API_KEY1 + "\">\n"
				+ "  </script>\n"
				;
		s += "<script type=\"text/javascript\">\n";
		s += "window.onload = triggerInitialGeocode();\n";

		s += "var watchID;\n";
		s += "var geoLoc;\n";

		s += "var t;\n";
		s += "var timer_is_on=0;\n";

		s += "\n";
		s += "function errorHandler(err) {\n";
		s += "    if(err.code == 1) {\n";
		//s += "        alert('Error: Access is denied - ' + err.message + '!');\n";
		//s += "        alert('Error: you must allow this page to record your location to access this secured device.');\n";
		s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\").value = '" 
						+ PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED + "';\n";
		//s += "    document.forms[\"MAINFORM\"].submit();\n";
		s += "    }else if( err.code == 2) {\n";
		s += "        alert('Error: Position is unavailable!');\n";
		s += "    }\n";
		s += "}\n";
		s += "\n";

		s += "function triggerInitialGeocode(){\n";
		if (bTrackUserLocation){
			s += "    if(navigator.geolocation){\n";
			// timeout at 60000 milliseconds (60 seconds)
			s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
			//s += "        geoLoc = navigator.geolocation;\n";
			s += "        navigator.geolocation.getCurrentPosition(initialGeocodeTrigger, errorHandler, options);\n";
			//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
			s += "    }else{\n";
			s += "        alert('Browser does not support geolocation!');\n";
			s += "    }\n";
		}
		s += "}\n";

		//This function is just a dummy to allow us to force the geocoding to happen when the screen first loads - 
		//this allows the user to choose, that first time, whether to allow the site to record his location:
		s += "function initialGeocodeTrigger(position){\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
				+ "position.coords.latitude;\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
				+ "position.coords.longitude;\n"
				+ "}\n"
				;

		s += "function getGeocode(){\n";
		if (bTrackUserLocation){
			s += "    if(navigator.geolocation){\n";
			// timeout at 60000 milliseconds (60 seconds)
			s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
			//s += "        geoLoc = navigator.geolocation;\n";
			s += "        navigator.geolocation.getCurrentPosition(submitValues, errorHandler, options);\n";
			//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
			s += "    }else{\n";
			s += "        alert('Browser does not support geolocation!');\n";
			s += "    }\n";
		}else{
			s += "    document.forms[\"MAINFORM\"].submit();\n";
		}
		s += "}\n";

		s += "function submitValues(position){\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
				+ "position.coords.latitude;\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
				+ "position.coords.longitude;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.SPEED_PARAMETER + "\").value = " 
				//	+ "position.coords.speed;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER + "\").value = " 
				//	+ "position.coords.altitude;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER + "\").value = " 
				//	+ "position.coords.accuracy;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER + "\").value = " 
				//	+ "position.coords.altitudeaccuracy;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER + "\").value = " 
				//	+ "position.timestamp;\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
				;

		//*****************************************************

		//Activate device:
		s += "function activatedevice(sDeviceID){\n"
				+ "    document.getElementById(\"" + SMTablessdevices.lid + "\").value = sDeviceID;\n"
				//+ "    document.getElementById(\"" + ACTIVATE_DEVICE_PARAMETER + "\").value = " + ACTIVATE_DEVICE_VALUE_ACTIVATE + ";\n"
				+ "    getGeocode();\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
				;

		//Deactivate device:
		//s += "function deactivatedevice(sDeviceID){\n"
		//	+ "    document.getElementById(\"" + SMTablessdevices.lid + "\").value = sDeviceID;\n"
		//	+ "    document.getElementById(\"" + ACTIVATE_DEVICE_PARAMETER + "\").value = " + ACTIVATE_DEVICE_VALUE_DEACTIVATE + ";\n"
		//	+ "    getGeocode();\n"
		//	//+ "    document.forms[\"MAINFORM\"].submit();\n"
		//	+ "}\n"
		//;

		s += "</script>\n";
		return s;
	}

	private String sStyleScripts(){
		String s = "";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
				"table.basic {"
				//+ "border-width: 0px; "
				//+ "border-spacing: 2px; "
				//+ "border-style: outset; "
				//+ "border-style: solid; "
				//+ "border-style: none; "
				//+ "border-color: black; "
				+ "border-collapse: collapse; "
				+ "width: 100%; "
				+ "font-size: " + "small" + "; "
				+ "font-family : Arial; "
				+ "color: black; "
				//+ "background-color: white; "
				+ "}"
				+ "\n"
				;

		s +=
				"table.basicwithborder {"
						+ "border-width: 1px; "
						+ "border-spacing: 2px; "
						+ "border-style: outset; "
						+ "border-style: solid; "
						//+ "border-style: none; "
						+ "border-color: black; "
						+ "border-collapse: separate; "
						+ "width: 100%; "
						+ "font-size: " + "medium" + "; "
						+ "font-family : Arial; "
						+ "color: black; "
						//+ "background-color: white; "
						+ "}"
						+ "\n"
						;
		/*
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		 */
		//This is the def for a table cell, left justified:
		s +=
				"td.leftjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						//+ "border: 0px solid; "
						//+ "border-style: none; "
						+ "padding: 10px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: medium; "
						+ "text-align: left; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//style= \" word-wrap:break-word; \"
		//style= \" word-wrap:normal; white-space:pre-wrap; \" 
		s +=
				"td.leftjustifiedcellforcewrap {"
						+ "height: " + sRowHeight + "; "
						//+ "border: 0px solid; "
						//+ "border-style: none; "
						+ "padding: 10px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: medium; "
						+ "text-align: left; "
						+ "color: black; "
						+ "word-wrap:break-word; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;
		//This is the def for a table cell, right justified:
		s +=
				"td.rightjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						//+ "border: 0px solid; "
						//+ "border-style: none; "
						+ "padding: 10px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: medium; "
						+ "text-align: right; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//This is the def for a table cell, center justified:
		s +=
				"td.centerjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						//+ "border: 0px solid; "
						//+ "border-style: none; "
						+ "padding: 10px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: medium; "
						+ "text-align: center; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//This is the def for a left-aligned heading on a table:
		s +=
				"td.leftjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
				+ "font-weight: bold; "
				+ "text-align: left; "
				+ "vertical-align:bottom; "
				+ "}"
				+ "\n"
				;

		//This is the def for a right-aligned heading on a table:
		s +=
				"td.rightjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
				+ "font-weight: bold; "
				+ "text-align: right; "
				+ "vertical-align:bottom; "
				+ "}"
				+ "\n"
				;

		//This is the def for a center-aligned heading on a table:
		s +=
				"td.centerjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
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
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
