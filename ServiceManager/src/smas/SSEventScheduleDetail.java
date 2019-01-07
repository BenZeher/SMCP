package smas;

import java.sql.Connection;

import SMDataDefinition.SMTablesseventscheduledetails;
import ServletUtilities.clsMasterEntry;

public class SSEventScheduleDetail extends clsMasterEntry{

	public static final String ParamObjectName = "Event Schedule Detail";

	private String m_slid;
	private String m_slsseventscheduleid;
	private String m_sideviceoralarmsequence;
	private String m_sldeviceorsequenceid;
	private String m_siactiontype;
	private String m_siresetdelay; //In minutes
	private String m_siactivated;

	public SSEventScheduleDetail() {
		super();
		initRecordVariables();
	}

	public void validate_entry_fields (Connection conn) throws Exception{
		//Validate the entries here:
		m_slid = m_slid.trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_slid + "'.");
		}

		m_slsseventscheduleid = m_slsseventscheduleid.trim();
		if (m_slsseventscheduleid.compareToIgnoreCase("") == 0){
			m_slsseventscheduleid = "-1";
		}
		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slsseventscheduleid);
		} catch (Exception e) {
			throw new Exception("Invalid Event Schedule ID: '" + m_slsseventscheduleid + "'.");
		}
		
		if (
			(m_sideviceoralarmsequence.compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE)) != 0)
			&& (m_sideviceoralarmsequence.compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE)) != 0)
		){
			throw new Exception("Invalid 'device or sequence' value: '" + m_sideviceoralarmsequence + "'.");
		}
		
		m_sldeviceorsequenceid = m_sldeviceorsequenceid.trim();
		if (m_sldeviceorsequenceid.compareToIgnoreCase("") == 0){
			m_sldeviceorsequenceid = "-1";
		}
		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_sldeviceorsequenceid);
		} catch (Exception e) {
			throw new Exception("Invalid device/sequence ID: '" + m_sldeviceorsequenceid + "'.");
		}
		
		//For now, we aren't going to verify the 'action type' because we may not even need such a thing:
		//if it's an alarm sequence, we just 'arm' it, and if it's a device, we just close the contacts.
		//m_siactiontype =
		
		m_siresetdelay = m_siresetdelay.trim();
		if (m_siresetdelay.compareToIgnoreCase("") == 0){
			m_siresetdelay = "0";
		}
		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_siresetdelay);
		} catch (Exception e) {
			throw new Exception("Invalid reset delay: '" + m_siresetdelay + "'.");
		}
	}
	
	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getslsseventscheduleid() {
		return m_slsseventscheduleid;
	}

	public void setslsseventscheduleid(String slsseventscheduleid) {
		m_slsseventscheduleid = slsseventscheduleid;
	}

	public String getsideviceoralarmsequence() {
		return m_sideviceoralarmsequence;
	}

	public void setsideviceoralarmsequence(String sideviceoralarmsequence) {
		m_sideviceoralarmsequence = sideviceoralarmsequence;
	}
	
	public String getsldeviceorsequenceid() {
		return m_sldeviceorsequenceid;
	}

	public void setsldeviceorsequenceid(String sldeviceorsequenceid) {
		m_sldeviceorsequenceid = sldeviceorsequenceid;
	}
	
	public String getsiactiontype() {
		return m_siactiontype;
	}

	public void setsiactiontype(String siactiontype) {
		m_siactiontype = siactiontype;
	}
	
	public String getsiresetdelay() {
		return m_siresetdelay;
	}
	public void setsiresetdelay(String siresetdelay) {
		m_siresetdelay = siresetdelay;
	}
	
	public String getsiactivated() {
		return m_siactivated;
	}
	public void setsiactivated(String siactivated) {
		m_siactivated = siactivated;
	}
	
	public String getObjectName(){
		return ParamObjectName;
	}

	private void initRecordVariables(){
		m_slid = "-1";
		m_slsseventscheduleid = "0";
		m_sideviceoralarmsequence = "0";
		m_sldeviceorsequenceid = "0";
		m_siactiontype = "0";
		m_siresetdelay = "0";
		m_siactivated = "0";
	}
}

