package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import javax.servlet.ServletContext;

import SMDataDefinition.SMTableictransactiondetails;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICTransactionDetail extends clsMasterEntry{

	public static final String ParamObjectName = "Transaction detail";
	
	private long m_lid;
	private long m_ldetailnumber;
	private long m_ltransactionid;
	private long m_lcostbucketid;
	private java.sql.Date m_datetimecostbucketcreation;
	private String m_scostbucketlocation;
	private String m_scostbucketremark;
	private long m_lbucketreceiptlineid;
	private BigDecimal m_bdcostbucketcostbeforetrans;
	private BigDecimal m_bdcostchange;
	private BigDecimal m_bdcostbucketqtybeforetrans;
	private BigDecimal m_bdqtychange;
	private boolean bDebugMode = false;
	
	public ICTransactionDetail() {
		super();
		initBidVariables();
        }
    
    public boolean save_without_data_transaction (ServletContext context, String sConf, String sUser,  String sUserID, String sUserFullName){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUser);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080991]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUserFullName){
    	
    	//If it's a new line, get the next available line number for it:
    	if (m_ldetailnumber == -1){
    		String SQL = "SELECT " + SMTableictransactiondetails.ldetailnumber
    			+ " FROM " + SMTableictransactiondetails.TableName
    			+ " WHERE ("
    				+ "(" + SMTableictransactiondetails.ltransactionid + " = " 
    				+ Long.toString(m_ltransactionid) + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableictransactiondetails.ldetailnumber + " DESC LIMIT 1"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_ldetailnumber = rs.getLong(SMTableictransactiondetails.ldetailnumber) + 1L;
				}else{
					m_ldetailnumber = 1;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error getting next detail line number - " + e.getMessage());
	        	return false;
			}
    	}
    	
    	String sDateBucketCreation = "1900-01-01 00:00:01";
    	try {
			sDateBucketCreation = clsDateAndTimeConversions.sqlDateToString(
				m_datetimecostbucketcreation, "yyyy-MM-dd hh:mm:ss");
		} catch (IllegalArgumentException e) {
			//Just let the initial value pass on through
		}
				
		String SQL = "INSERT INTO " + SMTableictransactiondetails.TableName + " ("
			+ SMTableictransactiondetails.bdcostbucketcostbeforetrans
			+ ", " + SMTableictransactiondetails.bdcostbucketqtybeforetrans
			+ ", " + SMTableictransactiondetails.bdcostchange
			+ ", " + SMTableictransactiondetails.bdqtychange
			+ ", " + SMTableictransactiondetails.dattimecostbucketcreation
			+ ", " + SMTableictransactiondetails.lcostbucketid
			+ ", " + SMTableictransactiondetails.lcostbucketreceiptlineid
			+ ", " + SMTableictransactiondetails.ldetailnumber
			+ ", " + SMTableictransactiondetails.ltransactionid
			+ ", " + SMTableictransactiondetails.scostbucketlocation
			+ ", " + SMTableictransactiondetails.scostbucketremark
			+ ") VALUES ("
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableictransactiondetails.bdcostbucketcostbeforetransScale,
				m_bdcostbucketcostbeforetrans).replace(",", "")
			+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableictransactiondetails.bdcostbucketqtybeforetransScale,
				m_bdcostbucketqtybeforetrans).replace(",", "")
			+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableictransactiondetails.bdcostchangeScale,
				m_bdcostchange).replace(",", "")
			+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableictransactiondetails.bdqtychangeScale,
				m_bdqtychange).replace(",", "")
			+ ", '" + sDateBucketCreation + "'"	
			+ ", " + Long.toString(m_lcostbucketid)
			+ ", " + Long.toString(m_lbucketreceiptlineid)
			+ ", " + Long.toString(m_ldetailnumber)
			+ ", " + Long.toString(m_ltransactionid)
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scostbucketlocation.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scostbucketremark.trim()) + "'"
			+ ")"
		;
		
    	try{
    		Statement stmt = conn.createStatement();
    		stmt.executeUpdate(SQL);
    	}catch(SQLException ex){
    		if (bDebugMode){
    			clsServletUtilities.sysprint(this.toString(), sUserFullName, "Inserting transaction detail SQL = " + SQL);
    		}
    	    super.addErrorMessage("Error inserting " + ParamObjectName + " with SQL: " 
   	    		+ SQL + " - " + ex.getMessage());
    	    return false;
    	}
    	
    	//Don't need this unless we need to get the lid of this detail immediately:
		/*
    	SQL = "SELECT"
			+ " " + SMTableictransactiondetails.lid
			+ " FROM " + SMTableictransactiondetails.TableName
			+ " WHERE ("
				+ "(" + SMTableictransactiondetails.ltransactionid + " = " + Long.toString(m_ltransactionid) + ")"
				+ " AND (" + SMTableictransactiondetails.ldetailnumber + " = " + Long.toString(m_ldetailnumber) + ")"
			+ ")"
			;
		try {
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			if (rs.next()) {
				m_lid = rs.getLong(SMTableictransactiondetails.lid);
			} else {
				super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		// If something went wrong, we can't get the last ID:
		if (m_lid == -1) {
			super.addErrorMessage("Could not get last ID number.");
			return false;
		}
    	*/
    	return true;
    }
    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
    private void initBidVariables(){
    	m_lid = -1;
    	m_ldetailnumber = -1;
    	m_ltransactionid = -1;
    	m_lcostbucketid = -1;
    	m_datetimecostbucketcreation = null;
    	m_scostbucketlocation = "";
    	m_scostbucketremark = "";
    	m_lbucketreceiptlineid = -1;
    	m_bdcostbucketcostbeforetrans = BigDecimal.ZERO;
    	m_bdcostchange = BigDecimal.ZERO;
    	m_bdcostbucketqtybeforetrans = BigDecimal.ZERO;
    	m_bdqtychange = BigDecimal.ZERO;
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }

	public long getM_lid() {
		return m_lid;
	}

	public void setM_lid(long mLid) {
		m_lid = mLid;
	}

	public long getM_ldetailnumber() {
		return m_ldetailnumber;
	}

	public void setM_ldetailnumber(long mLdetailnumber) {
		m_ldetailnumber = mLdetailnumber;
	}

	public long getM_ltransactionid() {
		return m_ltransactionid;
	}

	public void setM_ltransactionid(long mLtransactionid) {
		m_ltransactionid = mLtransactionid;
	}

	public long getM_lcostbucketid() {
		return m_lcostbucketid;
	}

	public void setM_lcostbucketid(long mLcostbucketid) {
		m_lcostbucketid = mLcostbucketid;
	}

	public java.sql.Date getM_datetimecostbucketcreation() {
		return m_datetimecostbucketcreation;
	}

	public void setM_datetimecostbucketcreation(
			java.sql.Date mDatetimecostbucketcreation) {
		if (mDatetimecostbucketcreation == null){
			try {
				m_datetimecostbucketcreation 
					= clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd hh:mm:ss", "1900-01-01 00:00:01");
			} catch (ParseException e) {
				// Should never happen
			}
		}else{
			m_datetimecostbucketcreation = mDatetimecostbucketcreation;
		}
	}

	public String getM_scostbucketlocation() {
		return m_scostbucketlocation;
	}

	public void setM_scostbucketlocation(String mScostbucketlocation) {
		m_scostbucketlocation = mScostbucketlocation;
	}

	public String getM_scostbucketremark() {
		return m_scostbucketremark;
	}

	public void setM_scostbucketremark(String mScostbucketremark) {
		m_scostbucketremark = mScostbucketremark;
	}

	public long getM_lbucketreceiptlineid() {
		return m_lbucketreceiptlineid;
	}

	public void setM_lbucketreceiptlineid(long mLbucketreceiptid) {
		m_lbucketreceiptlineid = mLbucketreceiptid;
	}

	public BigDecimal getM_bdcostbucketcostbeforetrans() {
		return m_bdcostbucketcostbeforetrans;
	}

	public void setM_bdcostbucketcostbeforetrans(
			BigDecimal mBdcostbucketcostbeforetrans) {
		m_bdcostbucketcostbeforetrans = mBdcostbucketcostbeforetrans;
	}

	public BigDecimal getM_bdcostchange() {
		return m_bdcostchange;
	}

	public void setM_bdcostchange(BigDecimal mBdcostchange) {
		m_bdcostchange = mBdcostchange;
	}

	public BigDecimal getM_bdcostbucketqtybeforetrans() {
		return m_bdcostbucketqtybeforetrans;
	}

	public void setM_bdcostbucketqtybeforetrans(
			BigDecimal mBdcostbucketqtybeforetrans) {
		m_bdcostbucketqtybeforetrans = mBdcostbucketqtybeforetrans;
	}

	public BigDecimal getM_bdqtychange() {
		return m_bdqtychange;
	}

	public void setM_bdqtychange(BigDecimal mBdqtychange) {
		m_bdqtychange = mBdqtychange;
	}
}