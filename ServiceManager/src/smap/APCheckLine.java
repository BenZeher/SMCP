package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SMDataDefinition.SMTableapchecklines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APCheckLine {
	
	private String m_slid;
	private String m_lcheckid;
	private String m_sichecklinenumber;
	private String m_sbdgrossamount;
	private String m_sbddiscounttaken;
	private String m_sbdnetpaid;
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_slentrylinenumber;
	private String m_sapplytdocnumber;
	private String m_datapplytodocdate;
	
	
	public APCheckLine(){
		initializeVariables();
	}
	
	public void validate_fields(Connection conn) throws Exception{
		
	}
	public void save_without_data_transaction (Connection conn, String sUserID) throws Exception{

		try {
			validate_fields(conn);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		String SQL = "";
		SQL = "INSERT into " + SMTableapchecklines.TableName
			+ " (" 
			+ SMTableapchecklines.bddiscounttaken
			+ ", " + SMTableapchecklines.bdgrossamount
			+ ", " + SMTableapchecklines.bdnetpaid
			+ ", " + SMTableapchecklines.datapplydocdate
			+ ", " + SMTableapchecklines.lbatchnumber
			+ ", " + SMTableapchecklines.lcheckid
			+ ", " + SMTableapchecklines.lchecklinenumber
			+ ", " + SMTableapchecklines.lentrylinenumber
			+ ", " + SMTableapchecklines.lentrynumber
			+ ", " + SMTableapchecklines.sapplytdocnumber
			+ ")"
			+ " VALUES ("
			+ "" + getsdiscounttaken().trim().replaceAll(",", "")
			+ ", " + getsgrossamount().trim().replaceAll(",", "")
			+ ", " + getsnetpaid().trim().replaceAll(",", "")
			+ ", '" + getsapplytodocdateInSQLFormat() + "'"
			+ ", " + getsbatchnumber()
			+ ", " + getslcheckid()
			+ ", " + getschecklinenumber()
			+ ", " + getsentrylinenumber()
			+ ", " + getsentrynumber()
			+ ", '" + getsapplytodocnumber() + "'"
			+ ")"
			
			//Unique key is:
			//UNIQUE KEY checklinenumberkey (`lcheckid`, `lchecklinenumber`)
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableapchecklines.bddiscounttaken + " = " + getsdiscounttaken().trim().replaceAll(",", "")
			+ ", " + SMTableapchecklines.bdgrossamount + " = " + getsgrossamount().trim().replaceAll(",", "")
			+ ", " + SMTableapchecklines.bdnetpaid + " = " + getsnetpaid().trim().replaceAll(",", "")
			+ ", " + SMTableapchecklines.datapplydocdate + " = '" + getsapplytodocdateInSQLFormat() + "'"
			+ ", " + SMTableapchecklines.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTableapchecklines.lentrylinenumber + " = " + getsentrylinenumber()
			+ ", " + SMTableapchecklines.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTableapchecklines.sapplytdocnumber + " = '" + getsapplytodocnumber() + "'"
			;
		//System.out.println("[1506956267] SQL = '" + SQL + "'");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1504802560] updating AP check line ID" + getslid() + " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//Get the ID of the apcheckline record:
		//Regardless of whether it was just inserted, OR if we are overwriting a new record, the lcheckid and lchecklinenumber form a unique key.  So
		//if we query to get the corresponding record using those two fields, we'll get the correct id for this line record, and we don't have to 
		//be concerned with whether its' an 'insert' or not:
		String sSQL = "SELECT"
			+ " " + SMTableapchecklines.lid
			+ " FROM " + SMTableapchecklines.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecklines.lcheckid + " = " + getslcheckid() + ")"
				+ " AND (" + SMTableapchecklines.lchecklinenumber + " = " + getschecklinenumber() + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()) {
				setslid(Long.toString(rs.getLong(SMTableapchecklines.lid)));
				rs.close();
			}else {
				rs.close();
				throw new Exception("Error [1508715392] - no check line record found with check ID '" + getslcheckid() + "' and  check line number '" + getschecklinenumber() + "'.");
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1504802561] Could not get check line record - " + e.getMessage());
		}

		return;
	}
	public void load(Connection conn, String sUserID, String sLid) throws Exception{
		String SQL = "SELECT * FROM " + SMTableapchecklines.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecklines.lid + " = " + sLid + ")"
			+ ")"
		;
	
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setsapplytodocnumber(rs.getString(SMTableapchecklines.sapplytdocnumber));
				setsbatchnumber(Long.toString(rs.getLong(SMTableapchecklines.lbatchnumber)));
				setschecklinenumber(Long.toString(rs.getLong(SMTableapchecklines.lchecklinenumber)));
				setsdatapplytodocdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapchecklines.datapplydocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsdiscounttaken(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapchecklines.bddiscounttakenScale, rs.getBigDecimal(SMTableapchecklines.bddiscounttaken)));
				setsentrylinenumber(Long.toString(rs.getLong(SMTableapchecklines.lentrylinenumber)));
				setsentrynumber(Long.toString(rs.getLong(SMTableapchecklines.lentrynumber)));
				setsgrossamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapchecklines.bdgrossamountScale, rs.getBigDecimal(SMTableapchecklines.bdgrossamount)));
				setslcheckid(Long.toString(rs.getLong(SMTableapchecklines.lcheckid)));
				setslid(Long.toString(rs.getLong(SMTableapchecklines.lid)));
				setsnetpaid(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapchecklines.bdnetpaidScale, rs.getBigDecimal(SMTableapchecklines.bdnetpaid)));
			}else{
				rs.close();
				throw new Exception("Error [1504800749] - No AP check line found with lid = " + sLid + ".");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1504800750] - " + e.getMessage());
		}
	}
	public String getschecklinenumber(){
		return m_sichecklinenumber;
	}
	public void setschecklinenumber(String sLineNumber){
		m_sichecklinenumber = sLineNumber;
	}
	public String getslid(){
		return m_slid;
	}
	public void setslid(String slid){
		m_slid = slid;
	}
	public String getsbatchnumber(){
		return m_sbatchnumber;
	}
	public void setsbatchnumber(String sBatchNumber){
		m_sbatchnumber = sBatchNumber;
	}
	public String getsentrynumber(){
		return m_sentrynumber;
	}
	public void setsentrynumber(String sEntryNumber){
		m_sentrynumber = sEntryNumber;
	}
	public String getslcheckid(){
		return m_lcheckid;
	}
	public void setslcheckid(String sCheckID){
		m_lcheckid = sCheckID;
	}
	public String getsgrossamount(){
		return m_sbdgrossamount;
	}
	public void setsgrossamount(String sCheckAmount){
		m_sbdgrossamount = sCheckAmount;
	}
	public String getsdiscounttaken(){
		return m_sbddiscounttaken;
	}
	public void setsdiscounttaken(String sDeductions){
		m_sbddiscounttaken = sDeductions;
	}
	public String getsnetpaid(){
		return m_sbdnetpaid;
	}
	public void setsnetpaid(String sNetPaid){
		m_sbdnetpaid = sNetPaid;
	}
	public String getsentrylinenumber(){
		return m_slentrylinenumber;
	}
	public void setsentrylinenumber(String sEntryLineNumber){
		m_slentrylinenumber = sEntryLineNumber;
	}
	public String getsapplytodocnumber(){
		return m_sapplytdocnumber;
	}
	public void setsapplytodocnumber(String sApplyToDocNumber){
		m_sapplytdocnumber = sApplyToDocNumber;
	}
	public String getsapplytodocdate(){
		return m_datapplytodocdate;
	}
	public String getsapplytodocdateInSQLFormat() throws Exception{
		if (m_datapplytodocdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_datapplytodocdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatapplytodocdate(String sdatApplyToDocDate){
		m_datapplytodocdate = sdatApplyToDocDate;
	}
	private void initializeVariables(){
		m_slid = "-1";
		m_lcheckid = "-1";
		m_sichecklinenumber = "-1";
		m_sbdgrossamount = "0.00";
		m_sbddiscounttaken = "0.00";
		m_sbdnetpaid = "0.00";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_slentrylinenumber = "-1";
		m_sapplytdocnumber = "";
		m_datapplytodocdate = SMUtilities.EMPTY_DATE_VALUE;
	}
}
