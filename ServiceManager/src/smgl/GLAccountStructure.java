package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglaccountsegments;
import SMDataDefinition.SMTableglaccountstructures;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLAccountStructure extends java.lang.Object{
	
	public static final String ParamObjectName = "Account Structure";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sstructureid;
	private String m_sdescription;
	private String m_slsegmentID1;
	private String m_sllength1;
	private String m_slsegmentID2;
	private String m_sllength2;
	private String m_slsegmentID3;
	private String m_sllength3;
	private String m_slsegmentID4;
	private String m_sllength4;
	private String m_slsegmentID5;
	private String m_sllength5;
	private String m_slsegmentID6;
	private String m_sllength6;
	private String m_slsegmentID7;
	private String m_sllength7;
	private String m_slsegmentID8;
	private String m_sllength8;
	private String m_slsegmentID9;
	private String m_sllength9;
	private String m_slsegmentID10;
	private String m_sllength10;
	
	private String m_sNewRecord;
	
	public GLAccountStructure(){
		m_lid = "0";;
		m_sstructureid = "";
		m_sdescription = "";
		m_slsegmentID1 = "0";
		m_sllength1 = "0";
		m_slsegmentID2 = "0";
		m_sllength2 = "0";
		m_slsegmentID3 = "0";
		m_sllength3 = "0";
		m_slsegmentID4 = "0";
		m_sllength4 = "0";
		m_slsegmentID5 = "0";
		m_sllength5 = "0";
		m_slsegmentID6 = "0";
		m_sllength6 = "0";
		m_slsegmentID7 = "0";
		m_sllength7 = "0";
		m_slsegmentID8 = "0";
		m_sllength8 = "0";
		m_slsegmentID9 = "0";
		m_sllength9 = "0";
		m_slsegmentID10 = "0";
		m_sllength10 = "0";
		m_sNewRecord = "";
	}
	
	public GLAccountStructure (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lid, req).trim();
		m_sstructureid = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.sstructureid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.sdescription, req).trim();
		if (req.getParameter(SMTableglaccountstructures.lsegmentid1) == null){
			m_slsegmentID1 = "0";
		}else{
			m_slsegmentID1 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid1, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid2) == null){
			m_slsegmentID2 = "0";
		}else{
			m_slsegmentID2 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid2, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid3) == null){
			m_slsegmentID3 = "0";
		}else{
			m_slsegmentID3 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid3, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid4) == null){
			m_slsegmentID4 = "0";
		}else{
			m_slsegmentID4 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid4, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid5) == null){
			m_slsegmentID5 = "0";
		}else{
			m_slsegmentID5 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid5, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid6) == null){
			m_slsegmentID6 = "0";
		}else{
			m_slsegmentID6 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid6, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid7) == null){
			m_slsegmentID7 = "0";
		}else{
			m_slsegmentID7 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid7, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid8) == null){
			m_slsegmentID8 = "0";
		}else{
			m_slsegmentID8 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid8, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid9) == null){
			m_slsegmentID9 = "0";
		}else{
			m_slsegmentID9 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid9, req).trim();
		}
		if (req.getParameter(SMTableglaccountstructures.lsegmentid10) == null){
			m_slsegmentID10 = "0";
		}else{
			m_slsegmentID10 = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountstructures.lsegmentid10, req).trim();
		}
		
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(GLAccountStructure.ParamsNewRecord, req).trim();
	}
	
    public void load (Connection conn) throws Exception{
        	
    	String SQL = "SELECT * FROM " + SMTableglaccountstructures.TableName
				+ " WHERE ("
				+ SMTableglaccountstructures.lid + " = " + m_lid 
			+	")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_lid = Long.toString(rs.getLong(SMTableglaccountstructures.lid));
    			m_sstructureid = rs.getString(SMTableglaccountstructures.sstructureid);
    			m_sdescription = rs.getString(SMTableglaccountstructures.sdescription);
    			m_slsegmentID1 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid1));
    			m_sllength1 = Long.toString(rs.getLong(SMTableglaccountstructures.llength1));
    			m_slsegmentID2 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid2));
    			m_sllength2 = Long.toString(rs.getLong(SMTableglaccountstructures.llength2));
    			m_slsegmentID3 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid3));
    			m_sllength3 = Long.toString(rs.getLong(SMTableglaccountstructures.llength3));
    			m_slsegmentID4 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid4));
    			m_sllength4 = Long.toString(rs.getLong(SMTableglaccountstructures.llength4));
    			m_slsegmentID5 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid5));
    			m_sllength5 = Long.toString(rs.getLong(SMTableglaccountstructures.llength5));
    			m_slsegmentID6 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid6));
    			m_sllength6 = Long.toString(rs.getLong(SMTableglaccountstructures.llength6));
    			m_slsegmentID7 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid7));
    			m_sllength7 = Long.toString(rs.getLong(SMTableglaccountstructures.llength7));
    			m_slsegmentID8 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid8));
    			m_sllength8 = Long.toString(rs.getLong(SMTableglaccountstructures.llength8));
    			m_slsegmentID9 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid9));
    			m_sllength9 = Long.toString(rs.getLong(SMTableglaccountstructures.llength9));
    			m_slsegmentID10 = Long.toString(rs.getLong(SMTableglaccountstructures.lsegmentid10));
    			m_sllength10 = Long.toString(rs.getLong(SMTableglaccountstructures.llength10));

    			m_sNewRecord = "0";
    			rs.close();
    			return;
    		}else{
    			rs.close();
    			setNewRecord("1");
    			return;
    		}
    	}catch (Exception e){
    		throw new Exception("Error [1524589656] reading " + ParamObjectName + " record: " + e.getMessage());
    	}
	}
    
    public void load(String sConf, ServletContext context, String sUserFullName) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sConf, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user ID: " + sUserFullName);
    	} catch (Exception e) {
    		throw new Exception("Error [1524589657] getting connection to load " + ParamObjectName + " - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Error [1524589658] could not get connection to load " + ParamObjectName + ".");
    	}
    	try {
			load(conn);
			clsDatabaseFunctions.freeConnection(context, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception("Error [1524589659] could load " + ParamObjectName + " - " + e.getMessage());
		}
    }

    public void saveEditableFields(ServletContext context, String sDBID, String sUserName) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":saveEditableFields - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1524589660] could not get connection to save " + ParamObjectName + ".");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception(e.getMessage());
		}
		
		//Check to see if the record already exists:
		String SQL = "INSERT INTO " + SMTableglaccountstructures.TableName
			+ " ("
				+ SMTableglaccountstructures.llength1
				+ ", " + SMTableglaccountstructures.llength2
				+ ", " + SMTableglaccountstructures.llength3
				+ ", " + SMTableglaccountstructures.llength4
				+ ", " + SMTableglaccountstructures.llength5
				+ ", " + SMTableglaccountstructures.llength6
				+ ", " + SMTableglaccountstructures.llength7
				+ ", " + SMTableglaccountstructures.llength8
				+ ", " + SMTableglaccountstructures.llength9
				+ ", " + SMTableglaccountstructures.llength10
				+ ", " + SMTableglaccountstructures.lsegmentid1
				+ ", " + SMTableglaccountstructures.lsegmentid2
				+ ", " + SMTableglaccountstructures.lsegmentid3
				+ ", " + SMTableglaccountstructures.lsegmentid4
				+ ", " + SMTableglaccountstructures.lsegmentid5
				+ ", " + SMTableglaccountstructures.lsegmentid6
				+ ", " + SMTableglaccountstructures.lsegmentid7
				+ ", " + SMTableglaccountstructures.lsegmentid8
				+ ", " + SMTableglaccountstructures.lsegmentid9
				+ ", " + SMTableglaccountstructures.lsegmentid10
				+ ", " + SMTableglaccountstructures.sdescription
				+ ", " + SMTableglaccountstructures.sstructureid
			+ ") VALUES ("
				+ getssegmentlength1()
				+ ", " + getssegmentlength2()
				+ ", " + getssegmentlength3()
				+ ", " + getssegmentlength4()
				+ ", " + getssegmentlength5()
				+ ", " + getssegmentlength6()
				+ ", " + getssegmentlength7()
				+ ", " + getssegmentlength8()
				+ ", " + getssegmentlength9()
				+ ", " + getssegmentlength10()
				+ ", " + getssegmentid1()
				+ ", " + getssegmentid2()
				+ ", " + getssegmentid3()
				+ ", " + getssegmentid4()
				+ ", " + getssegmentid5()
				+ ", " + getssegmentid6()
				+ ", " + getssegmentid7()
				+ ", " + getssegmentid8()
				+ ", " + getssegmentid9()
				+ ", " + getssegmentid10()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsstructureid()) + "'"
			+ ") ON DUPLICATE KEY UPDATE "
				+ SMTableglaccountstructures.llength1 + " = " + getssegmentlength1()
				+ ", " + SMTableglaccountstructures.llength2 + " = " + getssegmentlength2()
				+ ", " + SMTableglaccountstructures.llength3 + " = " + getssegmentlength3()
				+ ", " + SMTableglaccountstructures.llength4 + " = " + getssegmentlength4()
				+ ", " + SMTableglaccountstructures.llength5 + " = " + getssegmentlength5()
				+ ", " + SMTableglaccountstructures.llength6 + " = " + getssegmentlength6()
				+ ", " + SMTableglaccountstructures.llength7 + " = " + getssegmentlength7()
				+ ", " + SMTableglaccountstructures.llength8 + " = " + getssegmentlength8()
				+ ", " + SMTableglaccountstructures.llength9 + " = " + getssegmentlength9()
				+ ", " + SMTableglaccountstructures.llength10 + " = " + getssegmentlength10()
				+ ", " + SMTableglaccountstructures.lsegmentid1 + " = " + getssegmentid1()
				+ ", " + SMTableglaccountstructures.lsegmentid2 + " = " + getssegmentid2()
				+ ", " + SMTableglaccountstructures.lsegmentid3 + " = " + getssegmentid3()
				+ ", " + SMTableglaccountstructures.lsegmentid4 + " = " + getssegmentid4()
				+ ", " + SMTableglaccountstructures.lsegmentid5 + " = " + getssegmentid5()
				+ ", " + SMTableglaccountstructures.lsegmentid6 + " = " + getssegmentid6()
				+ ", " + SMTableglaccountstructures.lsegmentid7 + " = " + getssegmentid7()
				+ ", " + SMTableglaccountstructures.lsegmentid8 + " = " + getssegmentid8()
				+ ", " + SMTableglaccountstructures.lsegmentid9 + " = " + getssegmentid9()
				+ ", " + SMTableglaccountstructures.lsegmentid10 + " = " + getssegmentid10()
				+ ", " + SMTableglaccountstructures.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
				+ ", " + SMTableglaccountstructures.sstructureid + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsstructureid()) + "'"
			;
	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn);
	 		throw new Exception("Error [1524589661] saving " + ParamObjectName + " - " + e.getMessage());
	 	}
		
		//Update the ID if it's an insert successful:
	 	if (m_sNewRecord.compareToIgnoreCase("1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_lid = Long.toString(rs.getLong(1));
				}else {
					m_lid = "";
				}
				rs.close();
			} catch (SQLException e) {
				clsDatabaseFunctions.freeConnection(context, conn);
				throw new Exception("Error [1524589662] saving with SQL '" + SQL + "' - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.freeConnection(context, conn);
				throw new Exception("Error [1524589663] -could not get last ID number.");
			}
	 	}
	 	
		//Change new record status
	 	m_sNewRecord = "1";
		clsDatabaseFunctions.freeConnection(context, conn);
		return;			
    }
    
	public void delete(String sCode, Connection conn) throws Exception{
			
			//First, check that the record exists:
			String SQL = "SELECT * FROM " + SMTableglaccountstructures.TableName
				+ " WHERE ("
					+ SMTableglaccountstructures.lid + " = " + m_lid 
				+ ")"
			;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if(!rs.next()){
					rs.close();
					throw new Exception(ParamObjectName + " " + sCode + " cannot be found.");
				}else{
					rs.close();
				}
				
			}catch(SQLException e){
				throw new Exception("Error [1524589664] - checking " + ParamObjectName + " to delete - " + e.getMessage());
			}
			
			try{
				SQL = "DELETE FROM " + SMTableglaccountstructures.TableName
					+ " WHERE ("
						+ SMTableglaccountstructures.lid + " = " + m_lid
					+ ")"
				;
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch(Exception e){
				throw new Exception("Error [1524589665] deleting " + ParamObjectName + " with SQL '" + SQL + "' - " + e.getMessage());
			}
			return;
		}
	
	private void validateEntries(Connection conn) throws Exception{
		
		String s = "";
		
		//Guarantee that any unused segments are set to ZERO:
		if (getssegmentid1().compareToIgnoreCase("") == 0){
			setssegmentid1("0");
		}
		if (getssegmentid2().compareToIgnoreCase("") == 0){
			setssegmentid2("0");
		}
		if (getssegmentid3().compareToIgnoreCase("") == 0){
			setssegmentid3("0");
		}
		if (getssegmentid4().compareToIgnoreCase("") == 0){
			setssegmentid4("0");
		}
		if (getssegmentid5().compareToIgnoreCase("") == 0){
			setssegmentid5("0");
		}
		if (getssegmentid6().compareToIgnoreCase("") == 0){
			setssegmentid6("0");
		}
		if (getssegmentid7().compareToIgnoreCase("") == 0){
			setssegmentid7("0");
		}
		if (getssegmentid8().compareToIgnoreCase("") == 0){
			setssegmentid8("0");
		}
		if (getssegmentid9().compareToIgnoreCase("") == 0){
			setssegmentid9("0");
		}
		if (getssegmentid10().compareToIgnoreCase("") == 0){
			setssegmentid10("0");
		}
		
		try {
			updateSegmentLengths(conn);
		} catch (Exception e) {
			s += e.getMessage();
		}
		
		System.out.println(
				"[1527708587]" + "\n"
				+ " Length 1 = '" + getssegmentlength1() + "'" + "\n"
				+ " Length 2 = '" + getssegmentlength2() + "'" + "\n"
				+ " Length 3 = '" + getssegmentlength3() + "'" + "\n"
				+ " Length 4 = '" + getssegmentlength4() + "'" + "\n"
				+ " Length 5 = '" + getssegmentlength5() + "'" + "\n"
				+ " Length 6 = '" + getssegmentlength6() + "'" + "\n"
				+ " Length 7 = '" + getssegmentlength7() + "'" + "\n"
				+ " Length 8 = '" + getssegmentlength8() + "'" + "\n"
				+ " Length 9 = '" + getssegmentlength9() + "'" + "\n"
				+ " Length 10 = '" + getssegmentlength10() + "'" + "\n"
			);
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_lid, "ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID1, "Segment ID 1", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength1, "Segment 1 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID2, "Segment ID 2", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength2, "Segment 2 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID3, "Segment ID 3", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength3, "Segment 3 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID4, "Segment ID 4", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength4, "Segment 4 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID5, "Segment ID 5", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength5, "Segment 5 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID6, "Segment ID 6", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength6, "Segment 6 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID7, "Segment ID 7", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength7, "Segment 7 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID8, "Segment ID 8", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength8, "Segment 8 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID9, "Segment ID 9", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength9, "Segment 9 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_slsegmentID10, "Segment ID 10", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_sllength10, "Segment 10 Length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sdescription, SMTableglaccountstructures.sdescriptionLength, "Structure description", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sstructureid, SMTableglaccountstructures.sstructureidlLength, "Structure ID", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			updateSegmentLengths(conn);
		} catch (Exception e) {
			s += e.getMessage();
		}
		
		if (s.compareToIgnoreCase("") != 0){
			throw new Exception(s);
		}
	}
	
	public void processSegmentChange(String sSegmentID) throws Exception{
		//This function simply gets passed a segment ID, and isn't told whether it's ADDING or REMOVING the segment.
		//If it finds the segment ALREADY in the structure, it removes it, and then 'moves the other segments up'
		// in the structure.
		
		//If it DOESN'T find the segment already in the structure, then it ADDS it to the next available segment field.
		
		boolean bSegmentNotUsed = true;
		
		if (getssegmentid1().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid1(getssegmentid2());
			setssegmentlength1(getssegmentlength2());
			setssegmentid2(getssegmentid3());
			setssegmentlength2(getssegmentlength3());
			setssegmentid3(getssegmentid4());
			setssegmentlength3(getssegmentlength4());
			setssegmentid4(getssegmentid5());
			setssegmentlength4(getssegmentlength5());
			setssegmentid5(getssegmentid6());
			setssegmentlength5(getssegmentlength6());
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid2().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid2(getssegmentid3());
			setssegmentlength2(getssegmentlength3());
			setssegmentid3(getssegmentid4());
			setssegmentlength3(getssegmentlength4());
			setssegmentid4(getssegmentid5());
			setssegmentlength4(getssegmentlength5());
			setssegmentid5(getssegmentid6());
			setssegmentlength5(getssegmentlength6());
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid3().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid3(getssegmentid4());
			setssegmentlength3(getssegmentlength4());
			setssegmentid4(getssegmentid5());
			setssegmentlength4(getssegmentlength5());
			setssegmentid5(getssegmentid6());
			setssegmentlength5(getssegmentlength6());
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid4().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid4(getssegmentid5());
			setssegmentlength4(getssegmentlength5());
			setssegmentid5(getssegmentid6());
			setssegmentlength5(getssegmentlength6());
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid5().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid5(getssegmentid6());
			setssegmentlength5(getssegmentlength6());
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid6().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid6(getssegmentid7());
			setssegmentlength6(getssegmentlength7());
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid7().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid7(getssegmentid8());
			setssegmentlength7(getssegmentlength8());
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid8().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid8(getssegmentid9());
			setssegmentlength8(getssegmentlength9());
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
			
		if (getssegmentid9().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid9(getssegmentid10());
			setssegmentlength9(getssegmentlength10());
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		if (getssegmentid10().compareToIgnoreCase(sSegmentID) == 0){
			bSegmentNotUsed = false;
			setssegmentid10("0");
			setssegmentlength10("0");
			return;
		}
		
		//If it's NOT already being used, then add it to the next available slot:
		//System.out.println("[1527025520] - segment is NOT being used");
		
		if (bSegmentNotUsed){
			//System.out.println("[1527025521] - segmentid1() = '" + getssegmentid1() + "', sSegmentID = '" + sSegmentID + "'");
			if (getssegmentid1().compareToIgnoreCase("0") == 0){
				setssegmentid1(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid2().compareToIgnoreCase("0") == 0){
				setssegmentid2(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid3().compareToIgnoreCase("0") == 0){
				setssegmentid3(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid4().compareToIgnoreCase("0") == 0){
				setssegmentid4(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid5().compareToIgnoreCase("0") == 0){
				setssegmentid5(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid6().compareToIgnoreCase("0") == 0){
				setssegmentid6(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid7().compareToIgnoreCase("0") == 0){
				setssegmentid7(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid8().compareToIgnoreCase("0") == 0){
				setssegmentid8(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid9().compareToIgnoreCase("0") == 0){
				setssegmentid9(sSegmentID);
				return;
			}
		}
		if (bSegmentNotUsed){
			if (getssegmentid10().compareToIgnoreCase("0") == 0){
				setssegmentid10(sSegmentID);
				return;
			}
		}
		return;
	}
	
	private void updateSegmentLengths(Connection conn) throws Exception{
		
		String SQL = "SELECT * FROM " + SMTableglaccountsegments.TableName;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		
		setssegmentlength1("0");
		setssegmentlength2("0");
		setssegmentlength3("0");
		setssegmentlength4("0");
		setssegmentlength5("0");
		setssegmentlength6("0");
		setssegmentlength7("0");
		setssegmentlength8("0");
		setssegmentlength9("0");
		setssegmentlength10("0");
		
		try {
			while (rs.next()){
				String sSegmentID = Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.lid));
				System.out.println("[1527708851] - sSegmentID = '" + sSegmentID + "', getssegmentid1() = '" + getssegmentid1() + "'");
				if (getssegmentid1().compareTo(sSegmentID) == 0){
					setssegmentlength1(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid2().compareTo(sSegmentID) == 0){
					setssegmentlength2(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid3().compareTo(sSegmentID) == 0){
					setssegmentlength3(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid4().compareTo(sSegmentID) == 0){
					setssegmentlength4(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid5().compareTo(sSegmentID) == 0){
					setssegmentlength5(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid6().compareTo(sSegmentID) == 0){
					setssegmentlength6(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid7().compareTo(sSegmentID) == 0){
					setssegmentlength7(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid8().compareTo(sSegmentID) == 0){
					setssegmentlength8(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid9().compareTo(sSegmentID) == 0){
					setssegmentlength9(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
				if (getssegmentid10().compareTo(sSegmentID) == 0){
					setssegmentlength10(Long.toString(rs.getLong(SMTableglaccountsegments.TableName + "." + SMTableglaccountsegments.ilength)));
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1527095478] updating GL Account Structure segment lengths - " + e.getMessage() + ".");
		}
		
		return;
		
	}
	
	public String getlid() {
		return m_lid;
	}
	public void setlid(String lid) {
		m_lid = lid;
	}
	public String getssegmentid1() {
		return m_slsegmentID1;
	}
	public void setssegmentid1(String ssegmentid1) {
		m_slsegmentID1 = ssegmentid1;
	}
	public String getssegmentid2() {
		return m_slsegmentID2;
	}
	public void setssegmentid2(String ssegmentid2) {
		m_slsegmentID2 = ssegmentid2;
	}
	public String getssegmentid3() {
		return m_slsegmentID3;
	}
	public void setssegmentid3(String ssegmentid3) {
		m_slsegmentID3 = ssegmentid3;
	}
	public String getssegmentid4() {
		return m_slsegmentID4;
	}
	public void setssegmentid4(String ssegmentid4) {
		m_slsegmentID4 = ssegmentid4;
	}
	public String getssegmentid5() {
		return m_slsegmentID5;
	}
	public void setssegmentid5(String ssegmentid5) {
		m_slsegmentID5 = ssegmentid5;
	}
	public String getssegmentid6() {
		return m_slsegmentID6;
	}
	public void setssegmentid6(String ssegmentid6) {
		m_slsegmentID6 = ssegmentid6;
	}
	public String getssegmentid7() {
		return m_slsegmentID7;
	}
	public void setssegmentid7(String ssegmentid7) {
		m_slsegmentID7 = ssegmentid7;
	}
	public String getssegmentid8() {
		return m_slsegmentID8;
	}
	public void setssegmentid8(String ssegmentid8) {
		m_slsegmentID8 = ssegmentid8;
	}
	public String getssegmentid9() {
		return m_slsegmentID9;
	}
	public void setssegmentid9(String ssegmentid9) {
		m_slsegmentID9 = ssegmentid9;
	}
	public String getssegmentid10() {
		return m_slsegmentID10;
	}
	public void setssegmentid10(String ssegmentid10) {
		m_slsegmentID10 = ssegmentid10;
	}
	public String getssegmentlength1() {
		return m_sllength1;
	}
	public void setssegmentlength1(String ssegmentlength1) {
		m_sllength1 = ssegmentlength1;
	}
	public String getssegmentlength2() {
		return m_sllength2;
	}
	public void setssegmentlength2(String ssegmentlength2) {
		m_sllength2 = ssegmentlength2;
	}
	public String getssegmentlength3() {
		return m_sllength3;
	}
	public void setssegmentlength3(String ssegmentlength3) {
		m_sllength3 = ssegmentlength3;
	}
	public String getssegmentlength4() {
		return m_sllength4;
	}
	public void setssegmentlength4(String ssegmentlength4) {
		m_sllength4 = ssegmentlength4;
	}
	public String getssegmentlength5() {
		return m_sllength5;
	}
	public void setssegmentlength5(String ssegmentlength5) {
		m_sllength5 = ssegmentlength5;
	}
	public String getssegmentlength6() {
		return m_sllength6;
	}
	public void setssegmentlength6(String ssegmentlength6) {
		m_sllength6 = ssegmentlength6;
	}
	public String getssegmentlength7() {
		return m_sllength7;
	}
	public void setssegmentlength7(String ssegmentlength7) {
		m_sllength7 = ssegmentlength7;
	}
	public String getssegmentlength8() {
		return m_sllength8;
	}
	public void setssegmentlength8(String ssegmentlength8) {
		m_sllength8 = ssegmentlength8;
	}
	public String getssegmentlength9() {
		return m_sllength9;
	}
	public void setssegmentlength9(String ssegmentlength9) {
		m_sllength9 = ssegmentlength9;
	}
	public String getssegmentlength10() {
		return m_sllength10;
	}
	public void setssegmentlength10(String ssegmentlength10) {
		m_sllength10 = ssegmentlength10;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getsstructureid() {
		return m_sstructureid;
	}
	public void setsstructureid(String sstructureid) {
		m_sstructureid = sstructureid;
	}
	public void setNewRecord(String newrecord){
		m_sNewRecord = newrecord;
	}
	public String getNewRecord(){
		return m_sNewRecord;
	}
	public String getObjectName(){
	    return ParamObjectName;
	}
}