package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICEntryLine extends java.lang.Object{
	
	public static final String ParamLineID = "LineID";
	public static final String ParamLineItemNumber = "LineItemNumber";
	public static final String ParamLineLocation = "LineLocation";
	public static final String ParamLineDesc = "LineDesc";
	public static final String ParamLineQty = "LineQty";
	public static final String ParamLineCost = "LineCost";
	public static final String ParamLinePrice = "LinePrice";
	public static final String ParamLineControlAccount = "LineControlAccount";
	public static final String ParamLineDistributionAccount = "LineDistributionAccount";
	public static final String ParamLineComment = "LineComment";
	public static final String ParamLineEntryID = "LineEntryID";
	public static final String ParamLineCategory = "LineCategory";
	public static final String ParamLineReceiptNum = "LineReceiptNum";
	public static final String ParamLineCostBucketID = "LineCostBucketID";
	public static final String ParamLineBatchNumber = "LineBatchNumber";
	public static final String ParamLineEntryNumber = "LineEntryNumber";
	public static final String ParamLineLineNumber = "LineLineNumber";
	public static final String ParamLineUnitOfMeasure = "LineUnitOfMeasure";
	public static final String ParamLineTargetLocation = "LineTargetLocation";
	public static final String ParamInvoiceNumber = "InvoiceNumber";
	public static final String ParamInvoiceLineNumber = "InvoiceLineNumber";
	public static final String ParamReceiptLineID = "ReceiptLineID";
	
	private String m_slid;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sLineNumber;
	private String m_sItemNumber;
	private String m_sLocation;
	private String m_sQty;
	private String m_sCost;
	private String m_sPrice;
	private String m_sControlAcct;
	private String m_sDistributionAcct;
	private String m_sDescription;
	private String m_sComment;
	private long m_lEntryId;
	private String m_sCategory;
	private String m_sReceiptNum;
	private String m_sCostBucketID;
	private String m_sReceiptLineID;
	private String m_sTargetLocation;
	private String m_sInvoiceNumber;
	private String m_sInvoiceLineNumber;
	private String m_sErrorMessage;
	
	private String sUnitOfMeasure = "";
	private String sItemDescription = "";
    //============ Constructor
	public ICEntryLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber,
    		String sEntryId
        ) {
		initLineVariables();
		sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
    	sLineNumber(sLineNumber);
    	sEntryId(sEntryId);
	}
	public ICEntryLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber
        ) {
		initLineVariables();
    	sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
    	sLineNumber(sLineNumber);
	}
    //Use this constructor when adding a new line:
	public ICEntryLine(
    		String sBatchNumber,
    		String sEntryNumber
        ) {
		initLineVariables();
    	sBatchNumber(sBatchNumber);
    	sEntryNumber(sEntryNumber);
	}
	public ICEntryLine(
        ) {
		initLineVariables();
	}
	
    public ICEntryLine (HttpServletRequest req){
    	initLineVariables();
    	m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineBatchNumber, req).trim();
    	m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineEntryNumber, req).trim();
    	m_sLineNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineLineNumber, req).trim();
    	m_slid = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineID, req).trim();
    	m_sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineItemNumber, req).trim().toUpperCase();
    	m_sLocation = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineLocation, req).trim();
    	m_sTargetLocation = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineTargetLocation, req).trim();
    	
    	//First get the qty:
    	m_sQty = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineQty, req).trim();
    	if (m_sQty.compareToIgnoreCase("") == 0){
    		m_sQty = "0.0000";
    	}
    	//If there's a user drop down choice to pick either positive or negative qty, take that into account:
    	String sQtyPositiveOrNegative = clsManageRequestParameters.get_Request_Parameter("QtyPositiveOrNegative", req).trim();
    	//System.out.println("In " + this.toString() + ".ICEntryLine(req) - sQtyPositiveOrNegative = " + sQtyPositiveOrNegative);
    	//If it's a DECREASE, add a minus sign - in any other case, replace any minus sign:
    	if (sQtyPositiveOrNegative.compareToIgnoreCase("NEGATIVE") == 0){
			m_sQty = "-" + clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineQty, req).trim().replace("-", "");
		}
    	if (sQtyPositiveOrNegative.compareToIgnoreCase("POSITIVE") == 0){
			m_sQty = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineQty, req).trim().replace("-", "");
		}
    	if (sQtyPositiveOrNegative.compareToIgnoreCase("UNSELECTED") == 0){
			m_sQty = "0.0000";
		}
    	
    	m_sCost = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCost, req).trim();
    	if (m_sCost.compareToIgnoreCase("") == 0){
    		m_sCost = "0.00";
    	}else{
    	}
    	//If there's a user drop down choice to pick either increase or decrease of cost, take that into account:
    	String sCostPositiveOrNegative = clsManageRequestParameters.get_Request_Parameter("CostPositiveOrNegative", req).trim();
    	//If it's a DECREASE, add a minus sign - in any other case, replace any minus sign:
    	if (sCostPositiveOrNegative.compareToIgnoreCase("NEGATIVE") == 0){
			m_sCost = "-" + clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCost, req).trim().replace("-", "");
		}
    	if (sCostPositiveOrNegative.compareToIgnoreCase("POSITIVE") == 0){
			m_sCost = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCost, req).trim().replace("-", "");
		}
    	if (sCostPositiveOrNegative.compareToIgnoreCase("UNSELECTED") == 0){
			m_sCost = "0.00";
		}
    	
    	m_sPrice = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLinePrice, req).trim();
    	
    	//If there's a selection for pos/neg QTY, but NOT one for cost, then it's either a 
    	//shipment/return or a physical count and
    	//the cost and price have to be set to positive or negative, depending on whether it's
    	//a shipment or a return:
    	if (
    			(sQtyPositiveOrNegative.compareToIgnoreCase("") != 0)
    			&& (sCostPositiveOrNegative.compareToIgnoreCase("") == 0)
    	){
    		//Set the sign on the cost and price:
        	if (sQtyPositiveOrNegative.compareToIgnoreCase("NEGATIVE") == 0){
        		m_sCost = "-" + clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCost, req).trim().replace("-", "");
        		m_sPrice = "-" + clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLinePrice, req).trim().replace("-", "");
    		}
        	if (sQtyPositiveOrNegative.compareToIgnoreCase("POSITIVE") == 0){
        		m_sCost = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCost, req).trim().replace("-", "");
        		m_sPrice = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLinePrice, req).trim().replace("-", "");
    		}
        	if (sQtyPositiveOrNegative.compareToIgnoreCase("UNSELECTED") == 0){
        		m_sCost = "0.00";
        		m_sPrice = "0.00";
    		}
    	}
    	
    	m_sControlAcct = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineControlAccount, req).trim();
    	m_sDistributionAcct = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineDistributionAccount, req).trim();
    	m_sDescription = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineDesc, req).trim();
    	m_sComment = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineComment, req).trim();
    	
    	String sEntryID = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineEntryID, req).trim();
    	try{
    		m_lEntryId = Long.parseLong(sEntryID);
    	}catch (NumberFormatException e){
    		m_lEntryId = -1;
    	}
    	m_sCategory = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCategory, req).trim();
    	m_sReceiptNum = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineReceiptNum, req).trim();
    	m_sCostBucketID = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamLineCostBucketID, req).trim();
    	if (m_sCostBucketID.compareToIgnoreCase("") == 0){
    		m_sCostBucketID = "-1";
    	}
    	m_sInvoiceNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamInvoiceNumber, req).trim();
    	m_sInvoiceLineNumber = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamInvoiceLineNumber, req).trim();
    	if (m_sInvoiceLineNumber.compareToIgnoreCase("") == 0){
    		m_sInvoiceLineNumber = "0";
    	}
    	m_sReceiptLineID = clsManageRequestParameters.get_Request_Parameter(ICEntryLine.ParamReceiptLineID, req).trim();
    }
    //Methods:
    public boolean batchIsOpen(ServletContext context, String sDBID){
    	
    	boolean bResult = false;
    	
    	//Make sure the batch is still open:
    	String SQL = "";
    	try {
    		SQL = "SELECT"
    			+ " " + ICEntryBatch.ibatchstatus
    			+ " FROM " + ICEntryBatch.TableName
    			+ " WHERE ("
    				+ ICEntryBatch.lbatchnumber + " = " + m_sBatchNumber
    			+ ")"
    		;
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				"ICEntryLine.batchIsOpen"
    		);
    		if (rs.next()){
    			int iStatus = rs.getInt(ICEntryBatch.ibatchstatus);
    			if (
    					(iStatus == SMBatchStatuses.ENTERED)
    					|| (iStatus == SMBatchStatuses.IMPORTED)
    			){
    				bResult = true;
    			}else{
    				m_sErrorMessage = "This batch (" + m_sBatchNumber + ") is no longer open.";
    			}
    		}else{
    			m_sErrorMessage = "Could not read batch to see if it is still open.";
    		}
    		rs.close();
    	}catch (SQLException e){
    		//System.out.println("Error checking if batch is open in " + this.toString() + " - " + e.getMessage());
    		m_sErrorMessage = "Error checking if batch is open with SQL: " + SQL + " - " + e.getMessage();
    	}
    	return bResult;
    }
    public boolean save (ServletContext context, String sDBID){
    	String SQL = "";
    	
    	//Make sure the batch is still open, just in case someone posted it since we loaded it:
    	if (!batchIsOpen(context, sDBID)){
    		return false;
    	}
    	
    	if (m_sLineNumber.compareToIgnoreCase("-1") == 0){
	    	//Add a new line:
    		SQL = "SELECT "
    			+ SMTableicentrylines.llinenumber
    			+ " FROM " + SMTableicentrylines.TableName
    			+ " WHERE (" 
    				+ "(" + SMTableicentrylines.lbatchnumber + " = " + sBatchNumber() + ")"
    				+ " AND (" + SMTableicentrylines.lentrynumber + " = " + sEntryNumber() + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableicentrylines.llinenumber + " DESC";
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID);
		    	
		    	if (rs.next()){
		    		sLineNumber(Long.toString(rs.getLong(SMTableicentrylines.llinenumber) + 1));
		    	}
		    	else{
		    		sLineNumber("1");
		    	}
		    	rs.close();
	        }catch (SQLException ex){
	        	m_sErrorMessage = "Error setting reading line number in " + this.toString() + " with SQL: " + SQL + " - " + ex.getMessage();
	    	    return false;
	    	}
	        
	        //Now add a new record:

			SQL = "INSERT INTO "
				+ SMTableicentrylines.TableName
				+ " ("
				+ SMTableicentrylines.lbatchnumber
				+ ", " + SMTableicentrylines.lentrynumber
				+ ", " + SMTableicentrylines.llinenumber
				+ ", " + SMTableicentrylines.bdcost
				+ ", " + SMTableicentrylines.bdprice
				+ ", " + SMTableicentrylines.bdqty
				+ ", " + SMTableicentrylines.lentryid
				+ ", " + SMTableicentrylines.scategorycode
				+ ", " + SMTableicentrylines.scomment
				+ ", " + SMTableicentrylines.sdescription
				+ ", " + SMTableicentrylines.scontrolacct
				+ ", " + SMTableicentrylines.sdistributionacct
				+ ", " + SMTableicentrylines.sitemnumber
				+ ", " + SMTableicentrylines.slocation
				+ ", " + SMTableicentrylines.sreceiptnum
				+ ", " + SMTableicentrylines.lcostbucketid
				+ ", " + SMTableicentrylines.stargetlocation
				+ ", " + SMTableicentrylines.linvoicelinenumber
				+ ", " + SMTableicentrylines.sinvoicenumber
				+ ", " + SMTableicentrylines.lreceiptlineid
				+ ") VALUES ("
				+ sBatchNumber()
				+ ", " + sEntryNumber()
				+ ", " + sLineNumber()
				+ ", " + sCostSTDFormat().replace(",", "")
				+ ", " + sPriceSTDFormat().replace(",", "")
				+ ", " + sQtySTDFormat().replace(",", "")
				+ ", " + sEntryId()
				+ ", '" + sCategoryCode() + "'"
				+ ", '" + sComment() + "'"
				+ ", '" + sDescription() + "'"
				+ ", '" + sControlAcct() + "'"
				+ ", '" + sDistributionAcct() + "'" 
				+ ", '" + sItemNumber().toUpperCase() + "'"
				+ ", '" + sLocation() + "'"
				+ ", '" + sReceiptNum() + "'"
				+ ", " + sCostBucketID()
				+ ", '" + sTargetLocation() + "'"
				+ ", " + sInvoiceLineNumber()
				+ ", '" + sInvoiceNumber() + "'"
				+ ", " + sReceiptLineID()
				+ ")"
    			;
    	}else{

    		/*
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber,
    		String sAmount,
    		String sDocAppliedtoID,
    		String sEntryID,
    		String sComment,
    		String sDescription,
    		String sDocAppliedTo,
    		String sGLAcct,
    		String sApplyToOrderNumber
    		){
    		*/
    		SQL = "UPDATE "
    			+ SMTableicentrylines.TableName
    			+ " SET "

    			+ SMTableicentrylines.bdcost + " = " + sCostSTDFormat().replace(",", "")
    			+ ", " + SMTableicentrylines.bdprice + " = " + sPriceSTDFormat().replace(",", "")
    			+ ", " + SMTableicentrylines.bdqty + " = " + sQtySTDFormat().replace(",", "")
    			+ ", " + SMTableicentrylines.lentryid + " = " + sEntryId()
    			+ ", " + SMTableicentrylines.scategorycode + " = " + sCategoryCode()
    			+ ", " + SMTableicentrylines.scomment + " = '" + sComment() + "'"
    			+ ", " + SMTableicentrylines.sdescription + " = '" + sDescription() + "'"
    			+ ", " + SMTableicentrylines.scontrolacct + " = '" + sControlAcct() + "'"
    			+ ", " + SMTableicentrylines.sdistributionacct + " = '" + sDistributionAcct() + "'"
    			+ ", " + SMTableicentrylines.sitemnumber + " = '" + sItemNumber().toUpperCase() + "'"
    			+ ", " + SMTableicentrylines.slocation + " = '" + sLocation() + "'"
    			+ ", " + SMTableicentrylines.sreceiptnum + " = '" + sReceiptNum() + "'"
    			+ ", " + SMTableicentrylines.lcostbucketid + " = " + sCostBucketID()
    			+ ", " + SMTableicentrylines.slocation + " = '" + sTargetLocation() + "'"
    			+ ", " + SMTableicentrylines.linvoicelinenumber + " = " + sInvoiceLineNumber()
    			+ ", " + SMTableicentrylines.sinvoicenumber + " = '" + sInvoiceNumber() + "'"
    			+ ", " + SMTableicentrylines.lreceiptlineid + " = " + sReceiptLineID()
    			
    			+ " WHERE ("
    				+ "(" + SMTableicentrylines.lbatchnumber + " = " + sBatchNumber() + ")"
    				+ " AND (" + SMTableicentrylines.lentrynumber + " = " + sEntryNumber() + ")"
    				+ " AND (" + SMTableicentrylines.llinenumber + " = " + sLineNumber() + ")"
    			+ ")";
    	}
    	
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
	    		System.out.println("Could not complete update transaction - entry" + " was not updated.<BR>");
	    	}else{
	    		
	    		System.out.println("Successfully updated entry" + ": " + sEntryNumber() + ".");
	    	}
    	}catch(SQLException ex){
    		m_sErrorMessage = "Error in " + this.toString() + " saving ICEntryLine with SQL: " + SQL + " - " + ex.getMessage();
    	    return false;
    	}
    	return true;
    }
    public boolean load (
		String sBatchNumber,
		String sEntryNumber,
		String sLineNumber,
		ServletContext context, 
		String sDBID
		){
    
	    if (! sBatchNumber(sBatchNumber)){
	    	m_sErrorMessage = "Loading ICEntryLine [1387471229] - Invalid sBatchNumber - " + sBatchNumber;
	    	return false;
	    }
	
	    if (! sEntryNumber(sEntryNumber)){
	    	m_sErrorMessage = "Loading ICEntryLine [1387471230] - Invalid sEntryNumber - " + sEntryNumber;
	    	return false;
	    }
	    
	    if (! sLineNumber(sLineNumber)){
	    	m_sErrorMessage = "Loading ICEntryLine [1387471231] - Invalid sLineNumber - " + sEntryNumber;
	    	return false;
	    }
	
		String SQL = "SELECT * " 
			+ " FROM " + SMTableicentrylines.TableName
			+ " WHERE (" 
			+ "(" + SMTableicentrylines.lbatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableicentrylines.lentrynumber + " = " + sEntryNumber + ")"
			+ " AND (" + SMTableicentrylines.llinenumber + " = " + sLineNumber + ")"
			+ ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID); 
			rs.next();

			sId(Long.toString(rs.getLong(SMTableicentrylines.lid)));
			m_sItemNumber = rs.getString(SMTableicentrylines.sitemnumber);
			m_sLocation = rs.getString(SMTableicentrylines.slocation);
			m_sTargetLocation = rs.getString(SMTableicentrylines.stargetlocation);
			m_sQty = clsManageBigDecimals.BigDecimalToFormattedString(
				"#########0.0000", rs.getBigDecimal(
					SMTableicentrylines.bdqty).setScale(4, BigDecimal.ROUND_HALF_UP));
			m_sCost = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				rs.getBigDecimal(SMTableicentrylines.bdcost).setScale(2, BigDecimal.ROUND_HALF_UP));
			m_sPrice = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					rs.getBigDecimal(SMTableicentrylines.bdprice));
			sControlAcct(rs.getString(SMTableicentrylines.scontrolacct));
			sDistributionAcct(rs.getString(SMTableicentrylines.sdistributionacct));
			sDescription(rs.getString(SMTableicentrylines.sdescription));
			sComment(rs.getString(SMTableicentrylines.scomment));
			lEntryId(rs.getLong(SMTableicentrylines.lentryid));
			sCategoryCode(rs.getString(SMTableicentrylines.scategorycode));
			sReceiptNum(rs.getString(SMTableicentrylines.sreceiptnum));
			sCostBucketID(Long.toString(rs.getLong(SMTableicentrylines.lcostbucketid)));
			sInvoiceNumber(rs.getString(SMTableicentrylines.sinvoicenumber));
			sInvoiceLineNumber(Long.toString(rs.getLong(SMTableicentrylines.linvoicelinenumber)));
			sReceiptLineID(Long.toString(rs.getLong(SMTableicentrylines.lreceiptlineid)));
			rs.close();
		}catch (SQLException ex){
			m_sErrorMessage = "Error [1387471354] loading line - SQL = " + SQL + " - " + ex.getMessage();
	        return false;
		}
    	return true;
    }
    public boolean load (
		String sID,
		ServletContext context, 
		String sDBID
		){
    
    	String SQL = "SELECT * " 
    		+ " FROM " + SMTableicentrylines.TableName
    		+ " WHERE (" 
    		+ "(" + SMTableicentrylines.lid + " = " + sID + ")"
    		+ ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID); 
			rs.next();

			//Load the variables:
			m_sBatchNumber = Long.toString(rs.getLong(SMTableicentrylines.lbatchnumber));
			m_sEntryNumber = Long.toString(rs.getLong(SMTableicentrylines.lentrynumber));
			m_sLineNumber = Long.toString(rs.getInt(SMTableicentrylines.llinenumber));
			m_sItemNumber = rs.getString(SMTableicentrylines.sitemnumber);
			m_sLocation = rs.getString(SMTableicentrylines.slocation);
			m_sTargetLocation = rs.getString(SMTableicentrylines.stargetlocation);
			m_sQty = clsManageBigDecimals.BigDecimalToFormattedString(
				"#########0.0000", rs.getBigDecimal(SMTableicentrylines.bdqty));
			m_sCost = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				rs.getBigDecimal(SMTableicentrylines.bdcost).setScale(2, BigDecimal.ROUND_HALF_UP));
			m_sPrice = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				rs.getBigDecimal(SMTableicentrylines.bdprice));
			sControlAcct(rs.getString(SMTableicentrylines.scontrolacct));
			sDistributionAcct(rs.getString(SMTableicentrylines.sdistributionacct));
			sDescription(rs.getString(SMTableicentrylines.sdescription));
			sComment(rs.getString(SMTableicentrylines.scomment));
			lEntryId(rs.getLong(SMTableicentrylines.lentryid));
			sCategoryCode(rs.getString(SMTableicentrylines.scategorycode));
			sReceiptNum(rs.getString(SMTableicentrylines.sreceiptnum));
			sCostBucketID(Long.toString(rs.getLong(SMTableicentrylines.lcostbucketid)));
			sInvoiceNumber(rs.getString(SMTableicentrylines.sinvoicenumber));
			sInvoiceLineNumber(Long.toString(rs.getLong(SMTableicentrylines.linvoicelinenumber)));
			sReceiptLineID(Long.toString(rs.getLong(SMTableicentrylines.lreceiptlineid)));
			rs.close();
		}catch (SQLException ex){
			m_sErrorMessage = "Error [1387471429] loading IC Entry Line - " + ex.getMessage();
	        return false;
		}
    	return true;
    }
    public boolean load (
		long lID,
		ServletContext context, 
		String sDBID
		){
    
    	return load(Long.toString(lID), context, sDBID);
    }

    public String sBatchNumber (){
    	return m_sBatchNumber;
    }
    public boolean sBatchNumber (String sBatchNumber){
    	try{
    		m_sBatchNumber = sBatchNumber;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Batch number '" + sBatchNumber + "' is not valid";
    		return false;
    	}
    }
    public String sEntryNumber (){
    	return m_sEntryNumber;
    }
    public boolean sEntryNumber (String sEntryNumber){
    	try{
    		m_sEntryNumber = sEntryNumber;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Entry number '" + sEntryNumber + "' is not valid";
    		return false;
    	}
    }

    public String sLineNumber (){
    	return m_sLineNumber;
    }
    public boolean sLineNumber (String sLineNumber){
    	try{
    		@SuppressWarnings("unused")
			long lLineNumber = Long.parseLong(sLineNumber);
    		m_sLineNumber = sLineNumber;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Line number '" + sLineNumber + "' is not valid";
    		return false;
    	}
    }
    public void lEntryId (long lEntryId){
    	m_lEntryId = lEntryId;
    }

    public long lEntryId (){
    	return m_lEntryId;
    }

    public void sEntryId (String sEntryId){
    	m_lEntryId = Long.parseLong(sEntryId);
    }
    public String sEntryId (){
    	return Long.toString(m_lEntryId);
    }
    public boolean sItemNumber (String sItemNumber){
    	m_sItemNumber = sItemNumber.toUpperCase();
    	return true;
    }
    public String sItemNumber (){
   		return m_sItemNumber;
    }
    public boolean sLocation (String sLocation){
    	m_sLocation = sLocation;
    	return true;
    }
    public String sLocation (){
   		return m_sLocation;
    }
    public boolean sTargetLocation (String sTargetLocation){
    	m_sTargetLocation = sTargetLocation;
    	return true;
    }
    public String sTargetLocation (){
   		return m_sTargetLocation;
    }
    public boolean sControlAcct (String sControlAcct){
    	m_sControlAcct = sControlAcct;
    	return true;
    }
    public String sControlAcct (){
    	return m_sControlAcct;
    }
    public boolean sCategoryCode (String sCategoryCode){
    	m_sCategory = sCategoryCode;
    	return true;
    }
    public String sCategoryCode (){
    	return m_sCategory;
    }
    public boolean sDistributionAcct (String sDistributionAcct){
    	m_sDistributionAcct = sDistributionAcct;
    	return true;
    }
    public String sDistributionAcct (){
    	return m_sDistributionAcct;
    }
    public boolean sDescription (String sDescription){
    	m_sDescription = sDescription;
    	return true;
    }
    public String sDescription (){
    	return m_sDescription;
    }

    public boolean setCostString (String sCost){
    	try{
    		sCost = sCost.replace(",", "");
    		BigDecimal bd = new BigDecimal(sCost);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_sCost =  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Cost '" + sCost + "' is not valid";
    		return false;
    	}
    }
    public String sCostSTDFormat (){
    	return m_sCost;
    }
    public boolean setQtyString (String sQty){
    	try{
    		sQty = sQty.replace(",", "");
    		BigDecimal bd = new BigDecimal(sQty);
    		bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
    		m_sQty =  clsManageBigDecimals.BigDecimalToFormattedString("#########0.0000", bd);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Qty '" + sQty + "' is not valid";
    		return false;
    	}
    }
    public String sQtySTDFormat (){
    	return m_sQty;
    }
    public boolean getItemDetails (Connection conn){
    	boolean bDetailsReadSuccessfully = false;
    	
    	sUnitOfMeasure = "";
    	sItemDescription = "";
    	
    	String SQL = "SELECT " + SMTableicitems.sCostUnitOfMeasure
    	+ ", " + SMTableicitems.sItemDescription
		+ " FROM " + SMTableicitems.TableName
		+ " WHERE " + SMTableicitems.sItemNumber + " = '" + m_sItemNumber + "'"
		;
		//System.out.println("In " + this.toString() + ".sUnitOfMeasure - SQL = " + SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					conn);
			if(rs.next()){
				sUnitOfMeasure = rs.getString(SMTableicitems.sCostUnitOfMeasure);
				sItemDescription = rs.getString(SMTableicitems.sItemDescription);
				bDetailsReadSuccessfully = true;
				//System.out.println("In " + this.toString() + ".sUnitOfMeasure - sUnitOfMeasure = " + sUnitOfMeasure);
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error reading unit of measure: " + e.getMessage();
		}
    	return bDetailsReadSuccessfully;
    }
    public boolean getItemDetails(ServletContext context, String sDBID, String sUserID, String sUserFullName){

    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + ".getItemDetails - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			
    			);
    	
    	if (conn == null){
    		m_sErrorMessage = "Error reading unit of measure: could not open connection.";
    		return false;
    	}
    	
    	boolean bDetailsReadSuccessfully = getItemDetails(conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080860]");
    	return bDetailsReadSuccessfully;
    }
    public String sUnitOfMeasure(){
    	return sUnitOfMeasure;
    }
    public String sItemDescription(){
    	return sItemDescription;
    }

    public boolean setPriceString (String sPrice){
    	try{
    		sPrice = sPrice.replace(",", "");
    		BigDecimal bd = new BigDecimal(sPrice);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_sPrice =  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Price '" + sPrice + "' is not valid";
    		return false;
    	}
    }
    public String sPriceSTDFormat (){
    	return m_sPrice;
    }
    public boolean sComment (String sComment){
    	m_sComment = sComment;
    	return true;
    }
    public String sComment (){
    	return m_sComment;
    }
    public boolean sReceiptNum (String sReceiptNum){
    	m_sReceiptNum = sReceiptNum;
    	return true;
    }
    public String sReceiptNum (){
    	return m_sReceiptNum;
    }
    public void sCostBucketID (String sCostBucketID){
    	m_sCostBucketID = sCostBucketID;
    }
    public String sCostBucketID (){
    	return m_sCostBucketID;
    }
    public void sReceiptLineID (String sReceiptLineID){
    	m_sReceiptLineID = sReceiptLineID;
    }
    public String sReceiptLineID (){
    	return m_sReceiptLineID;
    }
    public void sId (String sId){
    	m_slid = sId;
    }
    public String sId (){
    	return m_slid;
    }
    public void sInvoiceNumber (String sInvoiceNumber){
    	m_sInvoiceNumber = sInvoiceNumber;
    }
    public String sInvoiceNumber (){
    	return m_sInvoiceNumber;
    }
    public void sInvoiceLineNumber (String sInvoiceLineNumber){
    	m_sInvoiceLineNumber = sInvoiceLineNumber;
    }
    public String sInvoiceLineNumber (){
    	return m_sInvoiceLineNumber;
    }

    public String getErrorMessage(){
    	return m_sErrorMessage;
    }
    
    public String read_out_debug_data(){
    	String sResult = " ** ICLine READ OUT DEBUG DATA: ";
    	sResult += "\nid: " + sId();
    	sResult += "\nbatch: " + sBatchNumber();
    	sResult += "\nentry: " + sEntryNumber();
    	sResult += "\nlineno: " + sLineNumber();
    	sResult += "\nitem number: " + sItemNumber();
    	sResult += "\nlocation: " + sLocation();
    	sResult += "\nqty: " + sQtySTDFormat();
    	sResult += "\ncost: " + sCostSTDFormat();
    	sResult += "\nprice: " + sPriceSTDFormat();
    	sResult += "\ncontrol acct: " + sControlAcct();
    	sResult += "\ndistribution acct: " + sDistributionAcct();
    	sResult += "\ndesc: " + sDescription();
    	sResult += "\ncomment: " + sComment();
    	sResult += "\nentryid: " + sEntryId();
    	sResult += "\ncategorycode: " + sCategoryCode();
    	sResult += "\nsreceiptnum: " + sReceiptNum();
    	sResult += "\ncostbucketid: " + sCostBucketID();
    	sResult += "\ntargetlocation: " + sTargetLocation();
    	sResult += "\ninvoicenumber: " + sInvoiceNumber();
    	sResult += "\ninvoicelinenumber: " + sInvoiceLineNumber();
    	sResult += "\nreceiptlineid: " + sReceiptLineID();
	return sResult;
    }
    private void initLineVariables(){
       	m_sBatchNumber = "-1";
    	m_sEntryNumber = "-1";
    	m_sLineNumber = "-1";
    	m_slid = "-1";
    	m_sItemNumber = "";
    	m_sLocation = "";
    	m_sQty = "0.0000";
    	m_sCost = "0.00";
    	m_sPrice = "0.00";
    	m_sControlAcct = "";
    	m_sDistributionAcct = "";
    	m_sDescription = "INITIALIZED LINE";
    	m_sComment = "";
    	m_lEntryId = -1;
    	m_sCategory = "";
    	m_sReceiptNum = "";
    	m_sCostBucketID = "-1";
    	m_sTargetLocation = "";
    	m_sInvoiceNumber = "";
    	m_sInvoiceLineNumber = "0";
    	m_sReceiptLineID = "-1";
    }
}
