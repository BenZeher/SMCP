package SMDataDefinition;

import sscommon.SSConstants;

public class SMTablessdevices {
	public static final String TableName = "ssdevices";
	
	public static final String lid = "lid";
	public static final String linputcontrollerid = "linputcontrollerid";
	public static final String loutputcontrollerid = "loutputcontrollerid";
	public static final String sdescription = "sdescription";
	public static final String sinputterminalnumber = "sinputterminalnumber";
	public static final String iinputtype = "iinputtype";
	public static final String soutputterminalnumber = "soutputterminalnumber";
	public static final String idevicetype = "idevicetype";
	public static final String iactivationtype = "iactivationtype";
	public static final String icontactduration = "icontactduration";  //in ms
	public static final String sremarks = "sremarks";
	public static final String iactive = "iactive";
	
	//Lengths
	public static final int sdescriptionlength = 128;
	public static final int sinputterminalnumberlength = 8;
	public static final int soutputterminalnumberlength = 8;
	
	//Device types:
	public static final int DEVICE_TYPE_DOOR = 0;
	public static final int DEVICE_TYPE_MOTIONSENSOR = 1;
	public static final int DEVICE_TYPE_ONOFF = 2;
	public static final int DEVICE_TYPE_SMOKEDETECTOR = 3;
	
	//Input types:
	public static final int DEVICE_INPUT_TYPE_NORMALLY_CLOSED = 0;
	public static final int DEVICE_INPUT_TYPE_NORMALLY_OPEN = 1;
	
	//Device type labels:
	public static final String DEVICE_TYPE_DOOR_LABEL = "Door";
	public static final String DEVICE_TYPE_MOTIONSENSOR_LABEL = "Motion sensor";
	public static final String DEVICE_TYPE_SMOKEDETECTOR_LABEL = "Smoke detector";
	public static final String DEVICE_TYPE_ONOFF_LABEL = "General on/off device";
	
	//Device activation commands:
	public static final String DEVICE_TYPE_COMMAND_DOOR_ACTIVATE = "Open";
	public static final String DEVICE_TYPE_COMMAND_DOOR_DEACTIVATE = "Close";
	public static final String DEVICE_TYPE_COMMAND_MOTIONSENSOR_ACTIVATE = "Activate";
	public static final String DEVICE_TYPE_COMMAND_MOTIONSENSOR_DEACTIVATE = "De-activate";
	public static final String DEVICE_TYPE_COMMAND_SMOKEDETECTOR_ACTIVATE = "Activate";
	public static final String DEVICE_TYPE_COMMAND_SMOKEDETECTOR_DEACTIVATE = "De-activate";
	public static final String DEVICE_TYPE_COMMAND_ONOFF_ACTIVATE = "Turn on";
	public static final String DEVICE_TYPE_COMMAND_ONOFF_DEACTIVATE = "Turn off";
	
	//Device activation statuses:
	public static final String DEVICE_TYPE_STATE_DOOR_ACTIVATED = "Open";
	public static final String DEVICE_TYPE_STATE_DOOR_DEACTIVATED = "Closed";
	public static final String DEVICE_TYPE_STATE_MOTIONSENSOR_ACTIVATED = "Activated";
	public static final String DEVICE_TYPE_STATE_MOTIONSENSOR_DEACTIVATED = "De-activated";
	public static final String DEVICE_TYPE_STATE_SMOKEDETECTOR_ACTIVATED = "Activated";
	public static final String DEVICE_TYPE_STATE_SMOKEDETECTOR_DEACTIVATED = "De-activated";
	public static final String DEVICE_TYPE_STATE_ONOFF_ACTIVATED = "On";
	public static final String DEVICE_TYPE_STATE_ONOFF_DEACTIVATED = "Off";
	
	//Device activation types:
	public static final int DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT = 0;
	public static final int DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT = 1;
	
	//Device default contact duration
	public static final int DEVICE_DEFAULT_CONTACT_DURATION = 500;
	
	public static String getDeviceTypeLabel(int iDeviceType){
		switch(iDeviceType){
		case DEVICE_TYPE_DOOR:
			return DEVICE_TYPE_DOOR_LABEL;
		case DEVICE_TYPE_MOTIONSENSOR:
			return DEVICE_TYPE_MOTIONSENSOR_LABEL;
		case DEVICE_TYPE_ONOFF:
			return DEVICE_TYPE_ONOFF_LABEL;
		case DEVICE_TYPE_SMOKEDETECTOR:
			return DEVICE_TYPE_SMOKEDETECTOR_LABEL;
		default:
			return "(UNKNOWN)";
		}
	}
	
	public static String getDeviceActivationStateLabel(int iDeviceType, Boolean bIsActivated){

		if (bIsActivated == null){
			return "(UNKNOWN)";
		}
		
		switch(iDeviceType){

		case DEVICE_TYPE_DOOR:
			if (bIsActivated){
				return DEVICE_TYPE_STATE_DOOR_ACTIVATED;
			}
			else{
				return DEVICE_TYPE_STATE_DOOR_DEACTIVATED;
			}
		case DEVICE_TYPE_MOTIONSENSOR:
			if (bIsActivated){
				return DEVICE_TYPE_STATE_MOTIONSENSOR_ACTIVATED;
			}
			else{
				return DEVICE_TYPE_STATE_MOTIONSENSOR_DEACTIVATED;
			}
		case DEVICE_TYPE_ONOFF:
			if (bIsActivated){
				return DEVICE_TYPE_STATE_ONOFF_ACTIVATED;
			}
			else{
				return DEVICE_TYPE_STATE_ONOFF_DEACTIVATED;
			}
		case DEVICE_TYPE_SMOKEDETECTOR:
			if (bIsActivated){
				return DEVICE_TYPE_STATE_SMOKEDETECTOR_ACTIVATED;
			}
			else{
				return DEVICE_TYPE_STATE_SMOKEDETECTOR_DEACTIVATED;
			}
		default:
			return "(UNKNOWN)";
		}
	}
	
	public static String getDeviceActivateCommandLabel(String sDeviceType, String sContactStatus, String sNormalContactType){
		
		int iDeviceType = Integer.parseInt(sDeviceType);
		switch(iDeviceType){
		
		case DEVICE_TYPE_DOOR:
			if (sNormalContactType.compareToIgnoreCase(Integer.toString(DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_DOOR_ACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_DOOR_DEACTIVATE;
				}else{
					return "(N/A)";
				}
			}else{
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_DOOR_DEACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_DOOR_ACTIVATE;
				}else{
					return "(N/A)";
				}
			}
		case DEVICE_TYPE_MOTIONSENSOR:
			if (sNormalContactType.compareToIgnoreCase(Integer.toString(DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_MOTIONSENSOR_ACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_MOTIONSENSOR_DEACTIVATE;
				}else{
					return "(N/A)";
				}
			}else{
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_MOTIONSENSOR_DEACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_MOTIONSENSOR_ACTIVATE;
				}else{
					return "(N/A)";
				}
			}
		case DEVICE_TYPE_SMOKEDETECTOR:
			if (sNormalContactType.compareToIgnoreCase(Integer.toString(DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_SMOKEDETECTOR_ACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_SMOKEDETECTOR_DEACTIVATE;
				}else{
					return "(N/A)";
				}
			}else{
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_SMOKEDETECTOR_DEACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_SMOKEDETECTOR_ACTIVATE;
				}else{
					return "(N/A)";
				}
			}
		case DEVICE_TYPE_ONOFF:
			if (sNormalContactType.compareToIgnoreCase(Integer.toString(DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_ONOFF_ACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_ONOFF_DEACTIVATE;
				}else{
					return "(N/A)";
				}
			}else{
				if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return DEVICE_TYPE_COMMAND_ONOFF_DEACTIVATE;
				}else if (sContactStatus.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return DEVICE_TYPE_COMMAND_ONOFF_ACTIVATE;
				}else{
					return "(N/A)";
				}
			}
		default:
			return "(N/A)";
		}
	}
	
	public static String getActivationTypeLabel(int iActivationType){
		switch(iActivationType){
		case DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT:
			return "Momentary contact";
		case DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT:
			return "Constant contact";
		default:
			return "(N/A)";
		}
	}
	
	public static String getInputTypeLabel(int iInputType){
		switch(iInputType){
		case DEVICE_INPUT_TYPE_NORMALLY_CLOSED:
			return "Normally closed";
		case DEVICE_INPUT_TYPE_NORMALLY_OPEN:
			return "Normally open";
		default:
			return "(N/A)";
		}
	}
}
