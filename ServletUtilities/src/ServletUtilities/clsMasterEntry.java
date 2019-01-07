package ServletUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

public class clsMasterEntry extends java.lang.Object{

	//General for all classes:
	public static String EMPTY_DATE_STRING = "00/00/0000";
	public static String EMPTY_DATETIME_STRING = "00/00/0000 00:00 AM";
	//public static String ParamObjectName = "";
	
	//Particular to the specific class
	public static final String ParambAddingNewEntry = "bAddingNewEntry";
	
	private String m_sobjectname;
	private ArrayList<String> m_sErrorMessage;
	private ArrayList<String> m_sStatusMessage;

	public clsMasterEntry() {
		initVariables();
        }

	protected clsMasterEntry (HttpServletRequest req){
		initVariables();
		
		/* SAMPLES:
		//Text field:
		m_ssalespersoncode = SMUtilities.get_Request_Parameter(SMBidEntry.Paramssalespersoncode, req).trim();
		
		//Date field:
		m_datoriginationdate = SMUtilities.get_Request_Parameter(SMBidEntry.Paramdatoriginationdate, req).trim();
		if (m_datoriginationdate.compareToIgnoreCase("") == 0){
			m_datoriginationdate = EMPTY_DATE_STRING;
		}
		
		//Date and time field:
		m_dattimebiddate = SMUtilities.get_Request_Parameter(
				SMBidEntry.Paramdattimebiddate, req).trim()
		+ " "
		+ SMUtilities.get_Request_Parameter(
				SMBidEntry.Paramdattimebiddate + "SelectedHour", req).trim()
		+ ":" 
		+ SMUtilities.get_Request_Parameter(
				SMBidEntry.Paramdattimebiddate + "SelectedMinute", req).trim()
		+ " "
		;
		
		if (SMUtilities.get_Request_Parameter(
				SMBidEntry.Paramdattimebiddate + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
			m_dattimebiddate = m_dattimebiddate + "PM";
		}else{
			m_dattimebiddate = m_dattimebiddate + "AM";
		}

		m_sphonenumber = SMUtilities.getPhoneNumberFromHTTPRequest(SMBidEntry.Paramsphonenumber, req);
		m_sextension = SMUtilities.get_Request_Parameter(SMBidEntry.Paramsextension, req).trim();

		//BigDecimal field:
		m_dapproximateamount = SMUtilities.get_Request_Parameter(
				SMBidEntry.Paramdapproximateamount, req).trim();
		if (m_dapproximateamount.compareToIgnoreCase("") == 0){
			m_dapproximateamount = "0.00";
		}
		*/
	}

    public void clearError(){
    	m_sErrorMessage.clear();
    }
    public void addErrorMessage(String sMsg){
    	m_sErrorMessage.add(sMsg);
    }
    public String getErrorMessages(){
    	String sReturnString = "";

    	for (int i = 0; i < m_sErrorMessage.size(); i++){
    		if (i == 0){
    			sReturnString = m_sErrorMessage.get(0);
    		}else{
    			sReturnString = sReturnString + "; " + m_sErrorMessage.get(i);
    		}
    	}
    	return sReturnString;
    }
    
    public void clearStatus(){
    	m_sStatusMessage.clear();
    }
    public void addStatusMessage(String sMsg){
    	m_sStatusMessage.add(sMsg);
    }
    public String getStatusMessages(){
    	String sReturnString = "";

    	for (int i = 0; i < m_sStatusMessage.size(); i++){
    		if (i == 0){
    			sReturnString = m_sStatusMessage.get(0);
    		}else{
    			sReturnString = sReturnString + "; " + m_sStatusMessage.get(i);
    		}
    	}
    	return sReturnString;
    }
    
	protected String ampmDateTimeToSQLDateTime(String sAMPMDateString){
		
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm a");
		sdf.setLenient(false); // This is very important
		try{
			java.util.Date myDate = sdf.parse(sAMPMDateString);
			java.sql.Timestamp ts = new java.sql.Timestamp(myDate.getTime());
			return clsDateAndTimeConversions.TimeStampToString(ts, "yyyy-MM-dd HH:mm", "0000-00-00 00:00");
		}catch(ParseException pse){
			return "0000-00-00 00:00";
		}
	}
	
    protected void initVariables(){
		m_sobjectname = "Object";
    	m_sErrorMessage = new ArrayList<String>(0);
    	m_sStatusMessage = new ArrayList<String>(0);
    }
    
    public void setObjectName(String sObjectName){
    	m_sobjectname = sObjectName;
    }
    public String getObjectName(){
    	return m_sobjectname;
    }
}