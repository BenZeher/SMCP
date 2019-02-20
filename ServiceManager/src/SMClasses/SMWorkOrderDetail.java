package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smic.ICItem;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableworkorderdetails;
import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class SMWorkOrderDetail extends clsMasterEntry{

	public static final String ParamObjectName = "work order line";
	
	public static final String Paramlid  = "lid";
	public static final String Paramlworkorderid = "lworkorderid";
	public static final String Paramidetailtype = "idetailtype";
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramsitemdesc = "sitemdesc";
	public static final String Paramlorderdetailnumber = "lorderdetailnumber";
	public static final String Paramllinenumber = "llinenumber";
	public static final String Paramlworkperformedlinenumber = "lworkperformedlinenumber";
	public static final String Paramsworkperformedcode = "sworkperformedcode";
	public static final String Paramsworkperformed = "sworkperformed";
	public static final String Parambdquantity = "bdquantity";
	public static final String Parambdunitprice = "bdunitprice";
	public static final String Paramsunitofmeasure = "sunitofmeasure";
	public static final String Parambdqtyassigned = "bdqtyassigned";
	public static final String Paramllsetpricetozero = "lsetpricetozero";
	public static final String Parambdextendedprice = "bdextendedprice";
	public static final String Paramslocationcode = "slocationcode";
	
	private String m_lid;
	private String m_lworkorderid;
	private String m_idetailtype;
	private String m_sitemnumber;
	private String m_sitemdesc;
	private String m_lorderdetailnumber;
	private String m_llinenumber;
	private String m_lworkperfomedlinenumber;
	private String m_sworkperformedcode;
	private String m_sworkperformed;
	private String m_bdquantity;
	private String m_bdunitprice;
	private String m_suom;
	private String m_bdqtyassigned;
	private String m_lsetpricetozero;
	private String m_bdextendedprice;
	private String m_slocationcode;
	//private boolean bDebugMode = false;
	
	public SMWorkOrderDetail() {
		super();
		initWorkOrderDetailVariables();
        }
	
	SMWorkOrderDetail (HttpServletRequest req){
		super(req);
		initWorkOrderDetailVariables();
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramlid, req).trim();
		if (m_lid.compareToIgnoreCase("") == 0){
			m_lid = "-1";
		}
		m_lworkorderid = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramlworkorderid, req).trim();
		if (m_lworkorderid.compareToIgnoreCase("") == 0){
			m_lworkorderid = "-1";
		}
		m_idetailtype = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramidetailtype, req).trim();
		m_sitemnumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramsitemnumber, req).trim();
		m_sitemdesc = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramsitemdesc, req).trim();
		m_lorderdetailnumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramlorderdetailnumber, req).trim();
		m_llinenumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramllinenumber, req).trim();
		if (m_llinenumber.compareToIgnoreCase("") == 0){
			m_llinenumber = "-1";
		}
		m_lworkperfomedlinenumber = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramlworkperformedlinenumber, req).trim();
		if (m_lworkperfomedlinenumber.compareToIgnoreCase("") == 0){
			m_lworkperfomedlinenumber = "-1";
		}
		m_sworkperformedcode = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramsworkperformedcode, req).trim();
		m_sworkperformed = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramsworkperformed, req).trim();
		m_bdquantity = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Parambdquantity, req).trim();
		if (m_bdquantity.compareToIgnoreCase("") == 0){
			m_bdquantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorderdetails.bdquantityDecimals, BigDecimal.ZERO);
		}
		m_bdunitprice = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Parambdunitprice, req).trim();
		if (m_bdunitprice.compareToIgnoreCase("") == 0){
			m_bdunitprice = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorderdetails.bdunitpriceDecimals, BigDecimal.ZERO);
		}
		m_bdextendedprice = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Parambdextendedprice, req).trim();
		if (m_bdextendedprice.compareToIgnoreCase("") == 0){
			m_bdextendedprice = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorderdetails.bdextendedpriceDecimals, BigDecimal.ZERO);
		}
		
		m_suom = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramsunitofmeasure, req).trim();
		m_bdqtyassigned = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Parambdqtyassigned, req).trim();
		if (m_bdqtyassigned.compareToIgnoreCase("") == 0){
			m_bdqtyassigned = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableworkorderdetails.bdqtyassignedDecimals, BigDecimal.ZERO);
		}
		m_lsetpricetozero = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramllsetpricetozero, req).trim();
		if (m_lsetpricetozero.compareToIgnoreCase("") == 0){
			m_lsetpricetozero = "0";
		}
		m_slocationcode = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderDetail.Paramslocationcode, req).trim();
	}
    public void load (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1390944852] opening data connection.");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067779]");
			throw new Exception("Error [1390944853] loading " + ParamObjectName + " - " + e.getMessage());
		}
    	
    	return;
    	
    }
    public void load (Connection conn) throws Exception{
    	try {
			load (m_lid, conn);
		} catch (Exception e) {
			throw new Exception("Error [1390944854] loading " + ParamObjectName + " - " + e.getMessage());
		}
    }
    private void load (String sID, Connection conn) throws Exception{

    	@SuppressWarnings("unused")
		long lID;
		try{
			lID = Long.parseLong(sID);
		}catch(NumberFormatException n){
			throw new Exception("Invalid ID: '" + sID + "'");
		}
    	
		String SQL = " SELECT * FROM " + SMTableworkorderdetails.TableName
			+ " WHERE ("
				+ SMTableworkorderdetails.lid + " = " + sID
			+ ")";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_lid = Long.toString(rs.getLong(SMTableworkorderdetails.lid));
				m_lworkorderid = Long.toString(rs.getLong(SMTableworkorderdetails.lworkorderid));
				m_idetailtype = Long.toString(rs.getLong(SMTableworkorderdetails.idetailtype));
				m_sitemnumber = rs.getString(SMTableworkorderdetails.sitemnumber);
				m_sitemdesc = rs.getString(SMTableworkorderdetails.sitemdesc);
				m_lorderdetailnumber = Long.toString(rs.getLong(SMTableworkorderdetails.lorderdetailnumber));
				m_llinenumber = Long.toString(rs.getLong(SMTableworkorderdetails.llinenumber));
				m_lworkperfomedlinenumber = Long.toString(rs.getLong(SMTableworkorderdetails.lworkperformedlinenumber));
				m_sworkperformedcode = rs.getString(SMTableworkorderdetails.sworkperformedcode);
				m_sworkperformed = rs.getString(SMTableworkorderdetails.sworkperformed);
				m_bdquantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorderdetails.bdquantityDecimals, rs.getBigDecimal(SMTableworkorderdetails.bdquantity));
				m_bdextendedprice = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorderdetails.bdextendedpriceDecimals, rs.getBigDecimal(SMTableworkorderdetails.bdextendedprice));
				m_bdunitprice = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorderdetails.bdunitpriceDecimals, rs.getBigDecimal(SMTableworkorderdetails.bdunitprice));
				m_suom = rs.getString(SMTableworkorderdetails.sunitofmeasure);
				m_bdqtyassigned = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorderdetails.bdqtyassignedDecimals, rs.getBigDecimal(SMTableworkorderdetails.bdqtyassigned));
				m_lsetpricetozero = Long.toString(rs.getLong(SMTableworkorderdetails.lsetpricetozero));
				m_slocationcode = rs.getString(SMTableworkorderdetails.slocationcode);
				rs.close();
			} else {
				rs.close();
				throw new Exception("No " + ParamObjectName + " found for : '" + sID
					+ "'");
				
			}
		} catch (SQLException e) {
			throw new Exception("Error reading " + ParamObjectName + " for : '" + sID
					+ "' - " + e.getMessage());
		}
		return;
    }
    
    public void save_without_data_transaction (
    		Connection conn, 
    		String sUserID,
    		SMLogEntry log, 
    		int iSavingFromWhichScreen, 
    		ServletContext context) throws Exception{
    	//System.out.println("[005] qty assigned = " + getsbdqtyassigned());
    	validate_line_fields(conn);
    	//System.out.println("[006] qty assigned = " + getsbdqtyassigned());
    	//If it's a new line, get the next available line number for it:
    	if (m_llinenumber.compareToIgnoreCase("-1") == 0){
    		String SQL = "SELECT " + SMTableworkorderdetails.llinenumber
    			+ " FROM " + SMTableworkorderdetails.TableName
    			+ " WHERE ("
    				+ "(" + SMTableworkorderdetails.lworkorderid + " = " + m_lworkorderid + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableworkorderdetails.llinenumber + " DESC LIMIT 1"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_llinenumber = Long.toString(rs.getLong(SMTableworkorderdetails.llinenumber) + 1L);
				}else{
					m_llinenumber = "1";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception ("Error [1390945473] getting next line number with SQL: " + SQL + " - " + e.getMessage());
			}
    	}
		String SQL = "INSERT INTO " + SMTableworkorderdetails.TableName + " ("
			+ SMTableworkorderdetails.bdquantity
			+ ", " + SMTableworkorderdetails.bdextendedprice
			+ ", " + SMTableworkorderdetails.bdunitprice
			+ ", " + SMTableworkorderdetails.idetailtype
			+ ", " + SMTableworkorderdetails.llinenumber
			+ ", " + SMTableworkorderdetails.lorderdetailnumber
			+ ", " + SMTableworkorderdetails.lworkorderid
			+ ", " + SMTableworkorderdetails.lworkperformedlinenumber
			+ ", " + SMTableworkorderdetails.lsetpricetozero
			+ ", " + SMTableworkorderdetails.sitemdesc
			+ ", " + SMTableworkorderdetails.sitemnumber
			+ ", " + SMTableworkorderdetails.sworkperformed
			+ ", " + SMTableworkorderdetails.sworkperformedcode
			+ ", " + SMTableworkorderdetails.sunitofmeasure
			+ ", " + SMTableworkorderdetails.bdqtyassigned
			+ ", " + SMTableworkorderdetails.slocationcode
			+ ") VALUES ("
			+ getsbdquantity().replace(",", "")
			+ ", " + getsbdextendedprice().replace(",", "")
			+ ", " + getsbdunitprice().replace(",", "")
			+ ", " + getsdetailtype()
			+ ", " + getslinenumber()
			+ ", " + getsorderdetailnumber()
			+ ", " + getsworkorderid()
			+ ", " + getsworkperformedlinenumber()
			+ ", " + getssetpricetozero()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsitemdesc()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsitemnumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsworkperformed()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsworkperformedcode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsuom()) + "'"
			+ ", " + getsbdqtyassigned().replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getslocationcode()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTableworkorderdetails.bdquantity + " = " + getsbdquantity().replace(",", "")
			+ ", " + SMTableworkorderdetails.bdextendedprice + " = " + getsbdextendedprice().replace(",", "")
			+ ", " + SMTableworkorderdetails.bdunitprice + " = " + getsbdunitprice().replace(",", "")
			+ ", " + SMTableworkorderdetails.idetailtype + " = " + getsdetailtype()
			+ ", " + SMTableworkorderdetails.llinenumber + " = " + getslinenumber()
			+ ", " + SMTableworkorderdetails.lorderdetailnumber + " = " + getsorderdetailnumber() 
			+ ", " + SMTableworkorderdetails.lworkorderid + " = " + getsworkorderid()
			+ ", " + SMTableworkorderdetails.lsetpricetozero + " = " + getssetpricetozero()
			+ ", " + SMTableworkorderdetails.lworkperformedlinenumber + " = " + getsworkperformedlinenumber()
			+ ", " + SMTableworkorderdetails.sitemdesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsitemdesc()) + "'"
			+ ", " + SMTableworkorderdetails.sitemnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsitemnumber()) + "'"
			+ ", " + SMTableworkorderdetails.sworkperformed + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsworkperformed()) + "'"
			+ ", " + SMTableworkorderdetails.sworkperformedcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsworkperformedcode()) + "'"
			+ ", " + SMTableworkorderdetails.sunitofmeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsuom()) + "'"
			+ ", " + SMTableworkorderdetails.bdqtyassigned + " = " + getsbdqtyassigned().replace(",", "")
			+ ", " + SMTableworkorderdetails.slocationcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslocationcode()) + "'"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
    	}catch(SQLException ex){
    		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getsworkorderid() 
						+ " in DETAIL save - ",
						"EXCEPTION CAUGHT - SQL = " + SQL + " - " + ex.getMessage(),
						"[1429288670]")
					;
	    		}
    		throw new Exception("Error [1391115493] Could not insert/update " + ParamObjectName + " with SQL: " + SQL + " - " + ex.getMessage());
    	}
		if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
					Integer.toString(iSavingFromWhichScreen) + " WOID :" + getsworkorderid() 
					+ " in DETAIL save",
					" - SQL = " + SQL,
					"[1429288671]")
				;
		}
		SQL = "SELECT"
			+ " " + SMTableworkorderdetails.lid
			+ " FROM " + SMTableworkorderdetails.TableName
			+ " WHERE ("
				+ "(" + SMTableworkorderdetails.lworkorderid + " = " + getsworkorderid() + ")"
				+ " AND (" + SMTableworkorderdetails.llinenumber + " = " + getslinenumber() + ")"
				+ " AND (" + SMTableworkorderdetails.lworkperformedlinenumber + " = " + getsworkperformedlinenumber() + ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				setslid(Long.toString(rs.getLong(SMTableworkorderdetails.lid)));
			} else {
				if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
							Integer.toString(iSavingFromWhichScreen) + " WOID :" + getsworkorderid() 
							+ " in DETAIL save getting line ID - ID NOT FOUND",
							" - SQL = " + SQL,
							"[1429288672]")
						;
				}
				throw new Exception("Error [1391115516] Could not get last ID number with SQL: " + SQL);
			}
			rs.close();
		} catch (SQLException e) {
			if (WebContextParameters.getLogWorkOrderUpdates(context).compareToIgnoreCase("True") == 0){
				log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_SMWORKORDERSAVEDEBUGGING, 
						Integer.toString(iSavingFromWhichScreen) + " WOID :" + getsworkorderid() 
						+ " in DETAIL save getting line ID - EXCEPTION CAUGHT",
						" - SQL = " + SQL + " - " + e.getMessage(),
						"[1429288672]")
					;
			}
			throw new Exception("Error [1391115517] Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
		}
		// If something went wrong, we can't get the last ID:
		if (this.getslid().compareToIgnoreCase("-1") == 0) {
			throw new Exception("Error [1391115518] Unable to update last ID number in work order details.");
		}
		//System.out.println("[007] qty assigned = " + getsbdqtyassigned());
    }
    
    public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName, int iSavingFromWhichScreen) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1391115713] opening data connection.");
    	}
    	SMLogEntry log = new SMLogEntry(sDBIB, context);
    	try {
			delete (conn,  sUserID, log, iSavingFromWhichScreen, context);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067777]");
			throw new Exception("Error [1391115714] deleting work order line - " + e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067778]");
    }
    public void delete (Connection conn, String sUserID, SMLogEntry log, int iSavingFromWhichScreen, ServletContext context) throws Exception{
    	
    	//TODO - What are the rules - what CAN'T we delete?
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1391115715] starting data transaction to delete work order line.");
    	}
    	
    	String SQL = "DELETE FROM " + SMTableworkorderdetails.TableName
    		+ " WHERE ("
    			+ "(" + SMTableworkorderdetails.lid + " = " + getslid() + ")"
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1391115716] deleting work order line with SQL: " + SQL + " - " + ex.getMessage());
		}
    	
		SMWorkOrderHeader wohead = new SMWorkOrderHeader();
		wohead.setlid(getsworkorderid());
		try {
			wohead.updateLineNumbersAfterLineDeletion(conn, sUserID, log, iSavingFromWhichScreen, context);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception(e.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1391115717] committing data transaction to delete work order detail.");
		}
		
		//Empty the values:
		initWorkOrderDetailVariables();
    }
    public void validate_entry_fields(
    		ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName) throws Exception{

    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " + sUserID + " - " + sUserFullName)
    	);
    	if (conn == null){
    		throw new Exception("Error [1391116644] - Could not get connection to validate entry fields.");
    	}
    	try {
			validate_line_fields(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067780]");	
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067781]");
    }
    public void validate_line_fields (Connection conn) throws Exception{
    	
        //Validate the entries here:
    	String sErrors = "";
    	boolean bValid = true;
    	long lID = 0;
		try {
			lID = Long.parseLong(getslid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + getslid() + "'.  ";
		}
    	
    	if (lID < -1){
    		bValid = false;
    		sErrors += "Invalid ID: '" + getslid() + "'.  ";
    	}
    	
		try {
			lID = Long.parseLong(getsworkorderid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid work order ID: '" + getsworkorderid() + "'.  ";
		}
    	
    	if ((lID < -1) || (lID == 0)){
    		bValid = false;
    		sErrors += "Invalid work order ID: '" + getsworkorderid() + "'.  ";
    	}

		try {
			lID = Long.parseLong(getsorderdetailnumber());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid order detail number: '" + getsorderdetailnumber() + "'.  ";
		}
    	
    	if ((lID < -1) || (lID == 0)){
    		bValid = false;
    		sErrors += "Invalid order detail number: '" + getsorderdetailnumber() + "'.  ";
    	}
    	
		try {
			lID = Long.parseLong(getslinenumber());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid line number: '" + getslinenumber() + "'.  ";
		}
    	
    	if ((lID < -1) || (lID == 0)){
    		bValid = false;
    		sErrors += "Invalid line number: '" + getslinenumber() + "'.  ";
    	}

		try {
			lID = Long.parseLong(getsworkperformedlinenumber());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid work performed line number: '" + getsworkperformedlinenumber() + "'.  ";
		}
    	
    	if ((lID < -1) || (lID == 0)){
    		bValid = false;
    		sErrors += "Invalid work performed line number: '" + getsworkperformedlinenumber() + "'.  ";
    	}
    	
    	try {
			if ((getsdetailtype().compareToIgnoreCase(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) != 0)
				&& (getsdetailtype().compareToIgnoreCase(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) != 0))
			{
				bValid = false;
				sErrors += "Invalid detail type '" + getsdetailtype() + "'.  ";
			}
		} catch (Exception e1) {
			bValid = false;
			sErrors += "Invalid detail type '" + getsdetailtype() + "'.  ";
		}
    	
    	//If it's an inventory item, then validate the inventory item:
    	if ((getsdetailtype().compareToIgnoreCase(Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0)){
        	setsitemnumber(getsitemnumber().trim());
        	ICItem item = new ICItem(getsitemnumber());
            if (!item.load(conn)){
       	       	bValid = false;
            	sErrors += "Invalid item number: '" + getsitemnumber() + "'.  Check this item number to see if it is typed correctly.";
	            setsitemnumber(getsitemnumber().trim().toUpperCase());
	            //Set the unit of measure to 'EA' as the default:
	            setsuom("EA");
            }else{
	            if (item.getActive().compareToIgnoreCase("1") != 0){
	            	bValid = false;
	       	       	sErrors += "Item number: '" + m_sitemnumber + "' is an INACTIVE item.  ";
	            }
	            
	            if (item.getCannotBeSoldFlag().compareToIgnoreCase("1") == 0){
	            	bValid = false;
	       	       	sErrors += "Item number: '" + m_sitemnumber + "' is not configured as a sellable item.  ";
	            }
	            
	            //Update the item number, this makes it uppercase, typically, for example:
	            setsitemnumber(item.getItemNumber());
	            setsitemdesc(getsitemdesc().trim());
	            //If the user didn't enter an item description, then read it from the item:
	            if (getsitemdesc().compareToIgnoreCase("") == 0){
	            	setsitemdesc(item.getItemDescription());
	            }
	            
	            //Set the unit of measure from the icitem record:
	            setsuom(item.getCostUnitOfMeasure());
            }
        	if (getsitemdesc().length() > SMTableworkorderdetails.sitemdescLength){
        		bValid = false;
        		sErrors += "Item description can only be " + Integer.toString(SMTableworkorderdetails.sitemdescLength) + " long.  ";
        	}
        	
    		//Qty ordered:
    		setsbdquantity(getsbdquantity().replace(",", "").trim());
            if (getsbdquantity().compareToIgnoreCase("") == 0){
            	setsbdquantity(clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableworkorderdetails.bdquantityDecimals, BigDecimal.ZERO));
            }
    		BigDecimal bdQty = new BigDecimal(0);
            try{
            	bdQty = new BigDecimal(getsbdquantity());
               	setsbdquantity(clsManageBigDecimals.BigDecimalToScaledFormattedString(
                	SMTableworkorderdetails.bdquantityDecimals, bdQty));
            }catch(NumberFormatException e){
            	bValid = false;
        		sErrors += "Invalid quantity: '" + getsbdquantity() + "'.  ";
            }
            
    		//Qty assigned:
            //System.out.println("[1398364337] work order detail " + this.getsitemnumber() + " qty assigned: " + getsbdqtyassigned());
    		setsbdqtyassigned(getsbdqtyassigned().replace(",", "").trim());
            if (getsbdqtyassigned().compareToIgnoreCase("") == 0){
            	setsbdqtyassigned(clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableworkorderdetails.bdqtyassignedDecimals, BigDecimal.ZERO));
            }
    		BigDecimal bdQtyAssigned = new BigDecimal(0);
            try{
            	bdQtyAssigned = new BigDecimal(getsbdqtyassigned());
               	setsbdqtyassigned(clsManageBigDecimals.BigDecimalToScaledFormattedString(
                	SMTableworkorderdetails.bdqtyassignedDecimals, bdQtyAssigned));
            }catch(NumberFormatException e){
            	bValid = false;
        		sErrors += "Invalid quantity assigned: '" + getsbdqtyassigned() + "'.  ";
            }
            
            //System.out.println("[1398364337] work order detail readout in validate detail line:\n" + read_out_debug_data() );

            //To be a valid work order detail, it must have either a qty assigned OR a qty used:
            //System.out.println("[1398357620] line for item " + this.getsitemnumber() + " has bdQty = " + bdQty + ", bdQtyAssigned = " + bdQtyAssigned + ".");
            if (
            		(bdQty.compareTo(BigDecimal.ZERO) <= 0)
            		&& (bdQtyAssigned.compareTo(BigDecimal.ZERO) <= 0)
            ){
            	bValid = false;
            	sErrors += "Line " + getslinenumber() + " for item " + getsitemnumber() 
            		+ " must have EITHER a qty assigned, or a qty used.  ";
            }

    		//Extended price:
    		setsbdextendedprice(getsbdextendedprice().replace(",", ""));
            if (getsbdextendedprice().compareToIgnoreCase("") == 0){
            	setsbdextendedprice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableworkorderdetails.bdextendedpriceDecimals, BigDecimal.ZERO));
            }
    		BigDecimal bdExtendedPrice = new BigDecimal(0);
            try{
            	bdExtendedPrice = new BigDecimal(getsbdextendedprice());
            	//TJR - 8/7/2014 - commented this out to allow negative prices on work orders:
                //if (bdExtendedPrice.compareTo(BigDecimal.ZERO) < 0){
                //	bValid = false;
                //	sErrors += "Extended price must be a positive number: " + getsbdextendedprice() + ".  ";
                //}else{
                	setsbdunitprice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
                	SMTableworkorderdetails.bdextendedpriceDecimals, bdExtendedPrice));
                //}
            }catch(NumberFormatException e){
            	bValid = false;
        		sErrors += "Invalid quantity: '" + getsbdquantity() + "'.  ";
            }
            
    		//Unit price:
    		setsbdunitprice(getsbdunitprice().replace(",", ""));
            if (getsbdunitprice().compareToIgnoreCase("") == 0){
            	setsbdunitprice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableworkorderdetails.bdunitpriceDecimals, BigDecimal.ZERO));
            }
    		BigDecimal bdUnitPrice = new BigDecimal(0);
            try{
            	bdUnitPrice = new BigDecimal(getsbdunitprice());
            	//TJR - 8/7/2014 - commented this out to allow negative prices on work orders:
                //if (bdUnitPrice.compareTo(BigDecimal.ZERO) < 0){
                //	bValid = false;
                //	sErrors += "Unit price must be a positive number: " + getsbdunitprice() + ".  ";
                //}else{
                	setsbdunitprice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
                	SMTableworkorderdetails.bdunitpriceDecimals, bdUnitPrice));
                //}
            }catch(NumberFormatException e){
            	bValid = false;
        		sErrors += "Invalid quantity: '" + getsbdquantity() + "'.  ";
            }
            
            if (
            	(getssetpricetozero().compareToIgnoreCase("1") != 0)
            	&& (getssetpricetozero().compareToIgnoreCase("0") != 0)
            ){
            	bValid = false;
            	sErrors += "Invalid value for set price to zero: '" + getssetpricetozero() + "'.";
            }
    	}

        setsworkperformed(getsworkperformed().trim());
        if (getsworkperformed().length() > SMTableworkorderdetails.sworkperformedLength){
        	bValid = false;
        	sErrors += "Work performed cannot be more than " + Integer.toString(SMTableworkorderdetails.sworkperformedLength) + ".  ";
        }
        setsworkperformedcode(getsworkperformedcode().trim());
        if (getsworkperformedcode().length() > SMTableworkorderdetails.sworkperformedcodeLength){
        	bValid = false;
        	sErrors += "Work performed code cannot be more than " + Integer.toString(SMTableworkorderdetails.sworkperformedcodeLength) + ".  ";
        }
        
        setslocationcode(getslocationcode().trim());
        if (getslocationcode().length() > SMTableworkorderdetails.slocationcodeLength){
        	bValid = false;
        	sErrors += "Location code cannot be more than " + Integer.toString(SMTableworkorderdetails.slocationcodeLength) + ".  ";
        }
        
        if (!bValid){
        	throw new Exception(sErrors);
        }
    }
    private void initWorkOrderDetailVariables(){
    	m_lid = "-1";
    	m_lworkorderid = "-1";
    	m_idetailtype = "";
    	m_sitemnumber = "";
    	m_sitemdesc = "";
    	m_lorderdetailnumber = "";
    	m_llinenumber = "-1";
    	m_lworkperfomedlinenumber = "-1";
    	m_sworkperformedcode = "";
    	m_bdextendedprice = "0.00";
    	m_sworkperformed = "";
    	m_bdquantity = "0.0000";
    	m_bdunitprice = "0.00";
    	m_suom = "";
    	m_bdqtyassigned = "0.0000";
    	m_lsetpricetozero = "0";
    	m_slocationcode = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
	public void setslid(String sLid) {
		m_lid = sLid;
	}
	public String getslid() {
		return m_lid;
	}
	public void setsworkorderid(String sWorkOrderId) {
		m_lworkorderid = sWorkOrderId;
	}
	public String getsworkorderid() {
		return m_lworkorderid;
	}
	public void setsdetailtype(String sDetailType) {
		m_idetailtype = sDetailType;
	}
	public String getsdetailtype() {
		return m_idetailtype;
	}
	public void setsitemnumber(String sItemNumber) {
		m_sitemnumber = sItemNumber;
	}
	public String getsitemnumber() {
		return m_sitemnumber;
	}
	public void setsitemdesc(String sItemDesc) {
		m_sitemdesc = sItemDesc;
	}
	public String getsitemdesc() {
		return m_sitemdesc;
	}
	public void setsorderdetailnumber(String sOrderDetailNumber) {
		m_lorderdetailnumber = sOrderDetailNumber;
	}
	public String getsorderdetailnumber() {
		return m_lorderdetailnumber;
	}
	public void setslinenumber(String sLineNumber) {
		m_llinenumber = sLineNumber;
	}
	public String getslinenumber() {
		return m_llinenumber;
	}
	public void setsworkperformedlinenumber(String sWorkPerformedLineNumber) {
		m_lworkperfomedlinenumber = sWorkPerformedLineNumber;
	}
	public String getsworkperformedlinenumber() {
		return m_lworkperfomedlinenumber;
	}
	public void setsworkperformedcode(String sWorkPerformedCode) {
		m_sworkperformedcode = sWorkPerformedCode;
	}
	public String getsworkperformedcode() {
		return m_sworkperformedcode;
	}
	public void setsworkperformed(String sWorkPerformed) {
		m_sworkperformed = sWorkPerformed;
	}
	public String getsworkperformed() {
		return m_sworkperformed;
	}
	public void setsbdextendedprice(String sbdExtendedPrice) {
		m_bdextendedprice = sbdExtendedPrice;
	}
	public String getsbdextendedprice() {
		return m_bdextendedprice;
	}
	public void setsbdquantity(String sbdQuantity) {
		m_bdquantity = sbdQuantity;
	}
	public String getsbdquantity() {
		return m_bdquantity;
	}
	public void setsbdunitprice(String sbdUnitPrice) {
		m_bdunitprice = sbdUnitPrice;
	}
	public String getsbdunitprice() {
		return m_bdunitprice;
	}
	public void setsuom(String sUnitOfMeasure) {
		m_suom = sUnitOfMeasure;
	}
	public String getsuom() {
		return m_suom;
	}
	public void setsbdqtyassigned(String sbdQtyAssigned) {
		m_bdqtyassigned = sbdQtyAssigned;
	}
	public String getsbdqtyassigned() {
		return m_bdqtyassigned;
	}
	public void setssetpricetozero(String sSetPriceToZero) {
		m_lsetpricetozero = sSetPriceToZero;
	}
	public String getssetpricetozero() {
		return m_lsetpricetozero;
	}
	public void setslocationcode(String slocationcode) {
		m_slocationcode = slocationcode;
	}
	public String getslocationcode() {
		return m_slocationcode;
	}
	
    public String read_out_debug_data(){
    	String sResult = "  ** SMWorkOrderDetail read out: ";
    	sResult += "\nID: " + getslid();
    	sResult += "\nWork Order ID: " + getsworkorderid();
    	sResult += "\nDetail Type: " + getsdetailtype();
    	sResult += "\nItem Number: " + getsitemnumber();
    	sResult += "\nItem Description: " + getsitemdesc();
    	sResult += "\nOrder Detail Number: " + getsorderdetailnumber();
    	sResult += "\nLine Number: " + getslinenumber();
    	sResult += "\nWork Performed Line Number: " + getsworkperformedlinenumber();
    	sResult += "\nWork Performed Code: " + getsworkperformedcode();
    	sResult += "\nWork Performed: " + getsworkperformed();
    	sResult += "\nQty: " + getsbdquantity();
    	sResult += "\nUnit Price: " + getsbdunitprice();
    	sResult += "\nExtended Price: " + getsbdextendedprice();
    	sResult += "\nUnit of measure: " + getsuom();
    	sResult += "\nQty assigned: " + getsbdqtyassigned();
    	sResult += "\nSet price to zero: " + getssetpricetozero();
    	return sResult;
    }
    /*
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ ServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + Paramlid + "=" 
			+ ServletUtilities.URLEncode(getsID());
		sQueryString += "&" + Parambdextendedordercost + "=" 
			+ ServletUtilities.URLEncode(getsextendedordercost());
		sQueryString += "&" + Parambdextendedreceivedcost + "=" 
			+ ServletUtilities.URLEncode(getsextendedreceivedcost());
		sQueryString += "&" + Parambdqtyordered + "=" 
			+ ServletUtilities.URLEncode(getsqtyordered());
		sQueryString += "&" + Parambdqtyreceived + "=" 
			+ ServletUtilities.URLEncode(getsqtyreceived());
		sQueryString += "&" + Parambdunitcost + "=" 
			+ ServletUtilities.URLEncode(getsunitcost());
		sQueryString += "&" + Paramdatexpected + "=" 
			+ ServletUtilities.URLEncode(getsdatexpected());
		sQueryString += "&" + Paramllinenumber + "=" 
			+ ServletUtilities.URLEncode(getslinenumber());
		sQueryString += "&" + Paramlpoheaderid + "=" 
			+ ServletUtilities.URLEncode(getspoheaderid());
		sQueryString += "&" + Paramsglexpenseacct + "=" 
			+ ServletUtilities.URLEncode(getsglexpenseacct());
		sQueryString += "&" + Paramsitemdescription + "=" 
			+ ServletUtilities.URLEncode(getsitemdescription());
		sQueryString += "&" + Paramsitemnumber + "=" 
			+ ServletUtilities.URLEncode(getsitemnumber());
		sQueryString += "&" + Paramslocation + "=" 
			+ ServletUtilities.URLEncode(getslocation());
		sQueryString += "&" + Paramsunitofmeasure + "=" 
			+ ServletUtilities.URLEncode(getsunitofmeasure());
		sQueryString += "&" + Paramsvendorsitemnumber + "=" 
			+ ServletUtilities.URLEncode(getsvendorsitemnumber());
		sQueryString += "&" + Paramsinstructions + "=" 
			+ ServletUtilities.URLEncode(getsinstructions());
		sQueryString += "&" + Paraminoninventoryitem + "=" 
			+ ServletUtilities.URLEncode(getsnoninventoryitem());
		sQueryString += "&" + Paramsvendorsitemcomment + "=" 
			+ ServletUtilities.URLEncode(getsvendorsitemcomment());
		return sQueryString;
	}
	*/
}