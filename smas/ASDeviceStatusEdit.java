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
import SMDataDefinition.SMTablesscontrollers;
import SMDataDefinition.SMTablessdevices;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;

public class ASDeviceStatusEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String DARK_ROW_BG_COLOR = "#cceeff";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	private static final String ACTIVATE_DEVICE_BUTTON_NAME = "ACTIVATE";
	public static final String SET_OUTPUT_CONTACTS_PARAMETER = "PARAMACTIVEDEVICE";
	public static final String ACTIVATE_DEVICE_VALUE_ACTIVATE = "ACTIVATEDEVICE";
	public static final String ACTIVATE_DEVICE_VALUE_DEACTIVATE = "DEACTIVATEDEVICE";
	public static final String CONFIRM_CHECKBOX_NAME_PREFIX = "CHKCONFIRM";
	public static final String PARAMETER_USER_LATITUDE = "USERLATITUDE";
	public static final String PARAMETER_USER_LONGITUDE = "USERLONGITUDE";
	public static final String PARAMETER_CURRENT_INPUT_ACTIVATION_STATE = "CURRENTACTIVATIONINPUTSTATE";
	public static final String PARAMETER_CURRENT_OUTPUT_ACTIVATION_STATE = "CURRENTACTIVATIONOUTPUTSTATE";
	public static final String PARAMETER_DURATION_PREFIX = "DURATION";
	public static final String PARAMETER_LOCATION_RECORDING_ALLOWED = "LOCATIONRECORDINGALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED = "NOTALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED = "ALLOWED";

	private static boolean bDebugMode = false;
	//private static final String OBJECT_NAME = SSDevice.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Device Status",
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASDeviceStatusAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASDeviceStatus
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASActivateDevices)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
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
		smedit.getPWOut().println("<I>This screen lists all of the ACTIVE devices on ACTIVE controllers that are configured in the system.</I>"
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
		ssopt.load(getServletContext(), sm.getConfFile(), sm.getUserName());

		s += "\n" + sStyleScripts() + "\n";
		s += "\n" + sCommandScripts(ssopt.getstrackuserlocation().compareToIgnoreCase("1") == 0) + "\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + SMTablessdevices.lid + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SMTablessdevices.lid + "\""
				+ "\">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + SET_OUTPUT_CONTACTS_PARAMETER + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + SET_OUTPUT_CONTACTS_PARAMETER + "\""
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
					sm.getConfFile(), 
					"MySQL", 
					this.toString() + ".getEditHTML - user: " + sm.getUserName()
					);
		} catch (Exception e) {
			throw new Exception("Error [1461091700] getting connection - " + e.getMessage());
		}

		//Display the header row:
		s += createHeaderRow();

		s += createDeviceRows(sm, bOutputDiagnostics, sm.getUserName(), sServerID);

		s += "</TABLE>";
		
		s += createFootNotes();

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);

		return s;
	}
	private String createHeaderRow(){
		String s = "";

		s += "<TR style = \" background-color: LightGray; color: black; \" >";

		s += 
				"<TD class = \" leftjustifiedheading \" >" + "Device description" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Input&nbsp;Controller" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Input&nbsp;terminal" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Normal&nbsp;input<BR>contact&nbsp;state" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Current&nbsp;input<BR>contact&nbsp;state" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Currently<BR>listening?" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Output&nbsp;Controller" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Output&nbsp;terminal" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Current&nbsp;output<BR>contact&nbsp;state" + "</TD>"
						+ "<TD class = \" centerjustifiedheading \" >" + "Toggle&nbsp;output<BR>contact&nbsp;state" 
							+ "<SUP><a href=\"#toggledevice\">1</a></SUP>"
							+ "</TD>"
						+ "</TR>"
						;
		return s;
	}
	private String createDeviceRows(SMMasterEditEntry sm, boolean bOutputDiagnostics, String sUser, String sServerID) throws Exception{
		String s = "";
		boolean bOddRow = true;

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sm.getConfFile(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".createDeviceRows - user: " + sm.getUserName())
					);
		} catch (Exception e) {
			throw new Exception("Error [1461091701] getting connection to create device rows - " + e.getMessage());
		}

		//Get a collection of the devices this user has access to and the state of each:
		ArrayList<SSDevice>arrDevices = new ArrayList<SSDevice>(0);
		try {
			arrDevices = getDevices(conn, bOutputDiagnostics, sUser, sServerID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception(e.getMessage());
		}

		//Sort devices by description:
		ArrayList<SSDevice>arrDevicesSorted = new ArrayList<SSDevice>(0);
		while(arrDevices.size() > 1){
			int iFirstSortPlaceInArray = 0;
			String sFirstSortPlaceDescription = arrDevices.get(0).getsdescription();
			for (int i = 1; i < arrDevices.size(); i++){
				if (arrDevices.get(i).getsdescription().compareToIgnoreCase(sFirstSortPlaceDescription) < 0){
					iFirstSortPlaceInArray = i;
					sFirstSortPlaceDescription = arrDevices.get(i).getsdescription();
				}
			}
			arrDevicesSorted.add(arrDevices.get(iFirstSortPlaceInArray));
			arrDevices.remove(iFirstSortPlaceInArray);
		}
		//Finally, if there's another device in the array, it must be the last one in the sort order, so just add it to the sorted array:
		if (arrDevices.size() > 0){
			arrDevicesSorted.add(arrDevices.get(0));
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
			throw new Exception("Error [1463431224] checking device editing permissions - " + e.getMessage());
		}
		boolean bAllowControllerEdit;
		try {
			bAllowControllerEdit = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ASEditControllers, 
					sm.getUserID(), 
					conn,
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error [1463431226] checking controller editing permissions - " + e.getMessage());
		}
		for (int i = 0; i < arrDevicesSorted.size(); i++){
			String sBackgroundColor = "";

			if (bOddRow){
				sBackgroundColor = LIGHT_ROW_BG_COLOR;
			}else{
				sBackgroundColor = DARK_ROW_BG_COLOR;
			}

			s += "<TR style = \" background-color: " + sBackgroundColor +  ";  \">";

			//Device:
			String sDeviceIDLink = "ID&nbsp;" + arrDevicesSorted.get(i).getslid();

			if (bAllowDeviceEdit){
				sDeviceIDLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smas.ASEditDevicesEdit?" 
						+ SMTablessdevices.lid + "=" + arrDevicesSorted.get(i).getslid()
						+ "\">" + sDeviceIDLink + "</A>"
						;
			}
			s+= "<TD class = \" leftjustifiedcell \">"
					+ sDeviceIDLink
					+ "&nbsp;"
					+ "<I>"
					+ arrDevicesSorted.get(i).getsdescription()
					+ "</I>"
					+ "</TD>"
					;

			//Input Controller:
			SSController inputcontroller = arrDevicesSorted.get(i).getinputcontroller(conn);
			String sInputControllerID = inputcontroller.getslid();
			String sInputControllerDesc = "";
			String sInputControllerLink = "";
			if (sInputControllerID.compareToIgnoreCase("-1") == 0){
				sInputControllerID = "";
			}else{
				//Controller:
				sInputControllerLink = "Controller&nbsp;ID:&nbsp;" + sInputControllerID;
				if (bAllowControllerEdit){
					sInputControllerLink = "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smas.ASEditControllersEdit?" 
							+ SMTablesscontrollers.lid + "=" + sInputControllerID
							+ "\">" + sInputControllerLink + "</A>"
							;
				}
				sInputControllerDesc = inputcontroller.getscontrollername()
						+ "@" + inputcontroller.getscontrollerurl()
						+ ":" + inputcontroller.getslisteningport()
						;
			}

			s+= "<TD class = \" centerjustifiedcell \">"
					+ sInputControllerLink
					+ "&nbsp;"
					+ "<I>"
					+ sInputControllerDesc
					+ "</I>"
					+ "</TD>"
					;

			//Input terminal:
			s+= "<TD class = \" centerjustifiedcell \">"
					+ arrDevicesSorted.get(i).getsinputterminalnumber()
					+ "</TD>"
					;

			//Normal input state:
			s+= "<TD class = \" centerjustifiedcell \">"
					+ SMTablessdevices.getInputTypeLabel(Integer.parseInt(arrDevicesSorted.get(i).getsinputtype()))
					+ "</TD>"
					;

			//Current contact state:
			s+= "<TD class = \" centerjustifiedcell \" >"
					+ arrDevicesSorted.get(i).getinputcontactstatus()
					+ "</TD>"
					;

			//Current listening status:
			s+= "<TD class = \" centerjustifiedcell \" >"
					+ arrDevicesSorted.get(i).getinputlisteningstatus()
					+ "</TD>"
					;

			//Output Controller:
			SSController outputcontroller = arrDevicesSorted.get(i).getoutputcontroller(conn);
			String sOutputControllerID = outputcontroller.getslid();
			String sOutputControllerDesc = "";
			String sOutputControllerLink = "";
			if (sOutputControllerID.compareToIgnoreCase("-1") == 0){
				sOutputControllerID = "";
			}else{
				//Controller:
				sOutputControllerLink = "Controller&nbsp;ID:&nbsp;" + sOutputControllerID;
				if (bAllowControllerEdit){
					sOutputControllerLink = "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smas.ASEditControllersEdit?" 
							+ SMTablesscontrollers.lid + "=" + sOutputControllerID
							+ "\">" + sOutputControllerLink + "</A>"
							;
				}
				sOutputControllerDesc = outputcontroller.getscontrollername()
						+ "@" + outputcontroller.getscontrollerurl()
						+ ":" + outputcontroller.getslisteningport()
						;
			}

			s+= "<TD class = \" centerjustifiedcell \">"
					+ sOutputControllerLink
					+ "&nbsp;"
					+ "<I>"
					+ sOutputControllerDesc
					+ "</I>"
					+ "</TD>"
					;

			//Output terminal:
			//Input terminal:
			s+= "<TD class = \" centerjustifiedcell \">"
					+ arrDevicesSorted.get(i).getsoutputterminalnumber()
					+ "</TD>"
					;

			//Current contact state:
			s+= "<TD class = \" centerjustifiedcell \" >"
					+ arrDevicesSorted.get(i).getoutputcontactstatus()
					+ "</TD>"
					;

			//Toggle output contacts:
			s+= "<TD class = \" centerjustifiedcell \" >"
					+ "&nbsp;"
					+ createToggleButton(arrDevicesSorted.get(i))
					+ "&nbsp;"
					+ "</TD>"
					;

			s += "</TR>";

			bOddRow = !bOddRow;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		return s;
	}

	private ArrayList<SSDevice> getDevices(Connection conn, boolean bOutputDiagnostics, String sUser, String sServerID) throws Exception{
		ArrayList<SSDevice>arrDevices = new ArrayList<SSDevice>(0);

		//First get the status of ALL the input terminals of ALL the input controllers from devices this user is authorized to use:

		//We first get all the controllers that are needed to read the current 'activation' status.
		//We do this so we can send one single command to each to get the status of ALL the terminals in each controller.
		String SQL = "SELECT DISTINCT"
				+ " " + SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid 
				+ " FROM " + SMTablessdevices.TableName
				+ " ORDER BY " + SMTablessdevices.lid
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
			throw new Exception ("Error [1461091702] reading controllers for devices - " + e.getMessage());
		}

		//Next get all the OUTPUT controllers, so we can list their devices, too.
		SQL = "SELECT DISTINCT"
				+ " " + SMTablessdevices.TableName + "." + SMTablessdevices.loutputcontrollerid 
				+ " FROM " + SMTablessdevices.TableName
				+ " ORDER BY " + SMTablessdevices.lid
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
						boolean bUpdateActivationStatusOnTerminals = false;
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
			throw new Exception ("Error [1461091703] reading controllers for devices - " + e.getMessage());
		}

		//Sort devices?
		if (bOutputDiagnostics){
			for (int i = 0; i < arrDevices.size(); i++){
				System.out.println("[1461091705] - arrDevices[" + i + "] = \n" 
						+ "Description = " + arrDevices.get(i).getsdescription() + "\n"
						+ "Activation status = " + arrDevices.get(i).getinputcontactstatus()
						);
			}
		}
		return arrDevices;
	}
	private String createToggleButton(SSDevice device){

		if (device.getsoutputterminalnumber().compareToIgnoreCase("") == 0){
			return "";
		}
		String sSetToContactState = "";
		String sButtonLabel = "";
		boolean bDisplayButton = false;
		
		//If the device is defined to have a 'momentary contact' output, then this screen should always CLOSE the contacts,
		//no matter what the instantaneous state of the actual contacts:
		if (device.getsactivationtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT)) == 0){
			sSetToContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
			sButtonLabel = "CLOSE CONTACTS";
			bDisplayButton = true;
		}else{
			//But if the device has a CONSTANT CONTACT output activation type, set the state depending on the current state
			//of the output contacts - this way, we can OPEN contacts that are currently staying closed:
			if (device.getoutputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
				sSetToContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN;
				sButtonLabel = "OPEN CONTACTS";
				bDisplayButton = true;
			}else{
				if (device.getoutputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					sSetToContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
					sButtonLabel = "CLOSE CONTACTS";
					bDisplayButton = true;
				}else{
					//If the contact state is not determined, then don't show any buttons:

				}
			}
		}

		String s = "";
		if (bDisplayButton){
			s = "<button type=\"button\""
					+ " style = \"" 
					+ "background-color: #4CAF50;"
					+ "border: none;"
					+ "color: white;"
					+ "padding: 6px 6px;"
					+ "text-align: center;"
					+ "text-decoration: none;"
					+ "display: inline-block;"
					+ "font-size: 12px; "
					+ "box-shadow: 0 12px 16px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);"
					+ "\""
					+ " value=\"" + sButtonLabel + "\""
					+ " name=\"" + ACTIVATE_DEVICE_BUTTON_NAME + "\""
					;
			s += " onClick=\"toggledevice(" + device.getslid() + ", '" + sSetToContactState + "');\">"
					+  sButtonLabel
					+ "</button>\n"
					;
			//If the output contact is a 'continuous contact' type, add a text box for setting the length of time for the contact closure:
			if (device.getsactivationtype().compareToIgnoreCase(
					Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT)) == 0){
				s += "<BR>for&nbsp;"
						+ "<INPUT TYPE=TEXT NAME=\"" + PARAMETER_DURATION_PREFIX + device.getslid() + "\""
						+ " VALUE=\"" + "0" + "\""
						+ " ID =\"" + PARAMETER_DURATION_PREFIX + device.getslid() + "\""
						+ " SIZE=" + "6"
						+ ">&nbsp;ms"
						;
			}else{
				//Otherwise, if it's a 'momentary contact' device, just show the momentary contact duration:
				s += "<BR>for&nbsp;"
						+ "<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_DURATION_PREFIX + device.getslid() + "\""
						+ " VALUE=\"" + device.getscontactduration() + "\""
						+ " ID =\"" + PARAMETER_DURATION_PREFIX + device.getslid() + "\""
						+ ">"
						+  device.getscontactduration() + "&nbsp;ms"
						;
			}
		}else{
			s += "(N/A)";
		}
		return s;
	}
	private String createFootNotes(){
		String s = "<BR>";
		
		s += "<a name=\"toggledevice\"><SUP>1</SUP></a><B>Toggle output contact state</B>:"
				+ "<BR>"
				+ "a) If the device's output contacts are a 'momentary contact' type, then this button will ALWAYS just"
				+ " CLOSE the output contacts for the default momentary contact duration, which is"
				+ " specified for each device in ms."
				+ "<BR>"
				+ "b) If the output contacts are a 'constant contact' type, then clicking this button will"
				+ " toggle the current output contact state: if they're currently closed, they will open, and if they're currently"
				+ " open, they will close.  If you are CLOSING them, you can set the length of time for them to stay closed"
				+ " in milliseconds underneath"
				+ " the corresponding button.  (To close the contacts indefinitely, you can set the duration to any large number:"
				+ " for example, 86400000 milliseconds is one day, 31536000000 is one year, etc.)"
				+ "  If you are opening the contacts, they will simply open, and stay open indefinitely."
				+ "  "
				+ "<BR>"
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
		s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\").value = '" 
				+ PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED + "';\n";
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

		//Activate device:
		s += "function toggledevice(sDeviceID, sSetToContactState){\n"
				+ "    document.getElementById(\"" + SMTablessdevices.lid + "\").value = sDeviceID;\n"
				+ "    document.getElementById(\"" + SET_OUTPUT_CONTACTS_PARAMETER + "\").value = sSetToContactState;\n"
				+ "    getGeocode();\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
				;

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
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
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
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
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
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
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
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
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
