package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SMDataDefinition.SMTablesmestimatelines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsValidateFormFields;

public class SMEstimateLine {

	private String m_slid;
	private String m_slsummaryid;
	private String m_slestimateid;
	private String m_slestimatelinenumber;
	private String m_sbdquantity;
	private String m_sitemnumber;
	private String m_slinedescription;
	private String m_sunitofmeasure;
	private String m_sbdextendedcost;
	
	public SMEstimateLine() 
	{
		initializeVariables();
	}
	
	public void save_without_data_transaction (Connection conn, String sUserName, boolean bBatchIsBeingPosted) throws Exception{

		try {
			validate_fields(conn);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		String SQL = "";
		SQL = "INSERT into " + SMTablesmestimatelines.TableName
			+ " (" 
			+ SMTablesmestimatelines.bdextendedcost
			+ ", " + SMTablesmestimatelines.bdquantity
			+ ", " + SMTablesmestimatelines.lestimatelid
			+ ", " + SMTablesmestimatelines.lestimatelinenumber
			+ ", " + SMTablesmestimatelines.lsummarylid
			+ ", " + SMTablesmestimatelines.sitemnumber
			+ ", " + SMTablesmestimatelines.slinedescription
			+ ", " + SMTablesmestimatelines.sunitofmeasure
			+ ")"
			+ " VALUES ("
			+ "" + m_sbdextendedcost.trim().replaceAll(",", "")
			+ ", " + m_sbdquantity.trim().replaceAll(",", "")
			+ ", " + m_slestimateid
			+ ", " + m_slestimatelinenumber
			+ ", " + m_slsummaryid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slinedescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesmestimatelines.bdextendedcost + " = " + m_sbdextendedcost.trim().replaceAll(",", "")
			+ ", " + SMTablesmestimatelines.bdquantity + " = " + m_sbdquantity.trim().replaceAll(",", "")
			+ ", " + SMTablesmestimatelines.lestimatelid + " = " + m_slestimateid
			+ ", " + SMTablesmestimatelines.lestimatelinenumber + " = " + m_slestimatelinenumber
			+ ", " + SMTablesmestimatelines.lsummarylid + " = " + m_slsummaryid
			+ ", " + SMTablesmestimatelines.sitemnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", " + SMTablesmestimatelines.slinedescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_slinedescription) + "'"
			+ ", " + SMTablesmestimatelines.sunitofmeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			;
			
		//System.out.println("[202005200548] - SQL = '" + SQL + ".");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1589990783] updating estimate line " + m_slestimatelinenumber
			+ ", on estimate number " + m_slestimateid
			+ ", summary number " + m_slsummaryid
				+ " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//If the line was newly created, get the new batch number:

		if (getslid().compareToIgnoreCase("-1") == 0){
			String sSQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					setslid(Long.toString(rs.getLong(1)));
				}else {
					setslid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1589990784] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1589990785] Could not get last ID number.");
			}
		}
		return;
	}
	public void validate_fields(Connection conn) throws Exception{
		
		String sResult = "";
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Estimate line ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slsummaryid = clsValidateFormFields.validateLongIntegerField(
				m_slsummaryid, 
				"Estimate summary ID", 
				1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slestimateid = clsValidateFormFields.validateLongIntegerField(
					m_slestimateid, 
				"Estimate ID", 
				1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slestimatelinenumber = clsValidateFormFields.validateLongIntegerField(
				m_slestimatelinenumber, 
				"Estimate line number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sbdquantity = clsValidateFormFields.validateBigdecimalField(
				m_sbdquantity, 
				"Line quantity", 
				SMTablesmestimatelines.bdquantityScale,
				new BigDecimal("-999999999.9999"),
				new BigDecimal("999999999.9999")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sitemnumber = clsValidateFormFields.validateStringField(
				m_sitemnumber, 
				SMTablesmestimatelines.sitemnumberLength, 
				"Item number", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slinedescription = clsValidateFormFields.validateStringField(
				m_slinedescription, 
				SMTablesmestimatelines.slinedescriptionLength,
				"Line item description", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sunitofmeasure = clsValidateFormFields.validateStringField(
					m_sunitofmeasure, 
				SMTablesmestimatelines.sunitofmeasureLength,
				"Unit of measure", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sbdextendedcost = clsValidateFormFields.validateBigdecimalField(
				m_sbdextendedcost, 
				"Line quantity", 
				SMTablesmestimatelines.bdextendedcostScale,
				new BigDecimal("-999999999.99"),
				new BigDecimal("999999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
	}
	public void load(Connection conn, String sLid) throws Exception{
		String SQL = "SELECT * FROM " + SMTablesmestimatelines.TableName
			+ " WHERE ("
				+ "(" + SMTablesmestimatelines.lid + " = " + sLid + ")"
			+ ")"
		;
	
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_slid = Long.toString(rs.getLong(SMTablesmestimatelines.lid));
				m_slsummaryid = Long.toString(rs.getLong(SMTablesmestimatelines.lsummarylid));
				m_slestimateid = Long.toString(rs.getLong(SMTablesmestimatelines.lestimatelid));
				m_slestimatelinenumber = Long.toString(rs.getLong(SMTablesmestimatelines.lestimatelinenumber));
				m_sbdquantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatelines.bdquantityScale, rs.getBigDecimal(SMTablesmestimatelines.bdquantity));
				m_sitemnumber = rs.getString(SMTablesmestimatelines.sitemnumber).trim();
				m_slinedescription = rs.getString(SMTablesmestimatelines.slinedescription).trim();
				m_sunitofmeasure = rs.getString(SMTablesmestimatelines.sunitofmeasure).trim();
				m_sbdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatelines.bdextendedcostScale, rs.getBigDecimal(SMTablesmestimatelines.bdextendedcost));
			}else{
				rs.close();
				throw new Exception("Error [1589991935] - No estimate line found with lid = " + sLid + ".");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1589991936] - " + e.getMessage());
		}
	}

	public String getslid(){
		return m_slid;
	}
	public void setslid(String sLid){
		m_slid = sLid;
	}

	public String getslsummaryid(){
		return m_slsummaryid;
	}
	public void setslsummaryid(String slsummaryid){
		m_slsummaryid = slsummaryid;
	}
	
	public String getslestimateid(){
		return m_slestimateid;
	}
	public void setslestimateid(String slestimateid){
		m_slestimateid = slestimateid;
	}
	
	public String getslestimatelinenumber(){
		return m_slestimatelinenumber;
	}
	public void setslestimatelinenumber(String slestimatelinenumber){
		m_slestimatelinenumber = slestimatelinenumber;
	}
	
	public String getsbdquantity(){
		return m_sbdquantity;
	}
	public void setsbdquantity(String sbdquantity){
		m_sbdquantity = sbdquantity;
	}
	
	public String getsitemnumber(){
		return m_sitemnumber;
	}
	public void setsitemnumber(String sitemnumber){
		m_sitemnumber = sitemnumber;
	}
	
	public String getslinedescription(){
		return m_slinedescription;
	}
	public void setslinedescription(String slinedescription){
		m_slinedescription = slinedescription;
	}
	
	public String getsunitofmeasure(){
		return m_sunitofmeasure;
	}
	public void setsunitofmeasure(String sunitofmeasure){
		m_sunitofmeasure = sunitofmeasure;
	}
	
	public String getsbdextendedcost(){
		return m_sbdextendedcost;
	}
	public void setsbdextendedcost(String sbdextendedcost){
		m_sbdextendedcost = sbdextendedcost;
	}
	

	public String dumpData(){
		
		String s = "";
		s += "    lid:" + getslid() + "\n";
		s += "    Estimate ID:" + getslestimateid() + "\n";
		s += "    Estimate line number:" + getslestimatelinenumber() + "\n";
		s += "    Qty:" + getsbdquantity() + "\n";
		s += "    Item number:" + getsitemnumber() + "\n";
		s += "    Line description:" + getslinedescription() + "\n";
		s += "    U/M:" + getsunitofmeasure() + "\n";
		s += "    Extended cost:" + getsbdextendedcost() + "\n";

		return s;
	}
	private void initializeVariables(){
		m_slid  = "-1";
		
		m_slsummaryid = "-1";
		m_slestimateid = "-1";
		m_slestimatelinenumber = "0";
		m_sbdquantity = "0.0000";
		m_sitemnumber = "";
		m_slinedescription = "";
		m_sunitofmeasure = "";
		m_sbdextendedcost = "0.00";
	}
}
