package SMDataDefinition;

public class SMTablejobcost {

	//Table Name
	public static final String TableName = "jobcost";
	
	//Field names:
	public static final String ID = "ID";
	public static final String sJobNumber = "sJobNumber";
	public static final String datDate = "datDate";
	public static final String sMechanic = "sMechanic";
	public static final String dQtyofHours = "dQtyofHours";
	//public static final String sJobType = "sJobType";
	public static final String mworkdescription = "mworkdescription";
	public static final String dTravelHours = "dTravelHours";
	public static final String decBackChargeHours = "decBackChargeHours";
	public static final String sLastEditedBy = "slasteditedby";
	public static final String dattimeLastEdit = "dattimelastedit";
	public static final String sscheduledby = "sscheduledby";
	public static final String ijoborder = "ijoborder";
	public static final String slocation = "slocation";
	//public static final String smechanicssn = "smechanicssn";
	public static final String smechanicfullname = "smechanicfullname";
	public static final String sservicecode = "sservicecode";
	public static final String sassistant = "sassistant";
	public static final String sschedulecomment = "sschedulecomment";
	public static final String sstartingtime = "sstartingtime";
	public static final String dattimeleftprevious = "dattimeleftprevious";
	public static final String dattimearrivedatcurrent = "dattimearrivedatcurrent";
	public static final String dattimeleftcurrent = "dattimeleftcurrent";
	public static final String dattimearrivedatnext = "dattimearrivedatnext";
    public static final String imechid = "imechid";
	
	//Field Lengths:
	public static final int sJobNumberLength = 8;
	public static final int sMechanicLength = 50;
	//public static final int sJobTypeLength = 64; 
	public static final int sDESCLength = 65000; 
	public static final int sLastEditedByLength = 50;
	public static final int sscheduledbyLength = 128;
	public static final int slocationLength = 6;
	//public static final int smechanicssnLength = 9;
	public static final int smechanicfullnameLength = 50;
	public static final int sservicecodeLength = 6;
	public static final int sassistantLength = 75;
	public static final int sschedulecommentLength = 255;
	public static final int sstartingtimeLength = 10;
	
	//This string gets placed in the job cost 'sJobNumber' field if it's a schedule entry that's NOT
	//associated with any particular job:
	public static final String DUMMY_JOB_NUMBER = "100";
	
}
