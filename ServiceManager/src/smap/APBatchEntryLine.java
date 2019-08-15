package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsValidateFormFields;
import smgl.GLAccount;

public class APBatchEntryLine {

	private String m_slid;
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_slinenumber;
	private String m_sdistributioncodename;
	private String m_sbdamount;
	private String m_sdistributionacct;
	private String m_sdescription;
	private String m_scomment;
	private String m_slpoheaderid;
	private String m_slreceiptheaderid;
	private String m_slporeceiptlineid;
	private String m_slapplytodocid;
	private String m_sapplytodocnumber;
	private String m_sbddiscountappliedamt;
	private String m_iapplytodoctype;
	private String m_sbdpayableamt;
	
	public APBatchEntryLine() 
	{
		initializeVariables();
	}
	
	public void save_without_data_transaction (Connection conn, String sUserName, int iEntryType, String sEntryID, boolean bBatchIsBeingPosted) throws Exception{

		try {
			validate_fields(conn, iEntryType, sEntryID, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		String SQL = "";
		SQL = "INSERT into " + SMTableapbatchentrylines.TableName
			+ " (" 
			+ SMTableapbatchentrylines.bdamount
			+ ", " + SMTableapbatchentrylines.bdapplieddiscountamt
			+ ", " + SMTableapbatchentrylines.bdpayableamount
			+ ", " + SMTableapbatchentrylines.iapplytodoctype
			+ ", " + SMTableapbatchentrylines.lapplytodocid
			+ ", " + SMTableapbatchentrylines.lbatchnumber
			+ ", " + SMTableapbatchentrylines.lentrynumber
			+ ", " + SMTableapbatchentrylines.llinenumber
			+ ", " + SMTableapbatchentrylines.lpoheaderid
			+ ", " + SMTableapbatchentrylines.lporeceiptlineid
			+ ", " + SMTableapbatchentrylines.lreceiptheaderid
			+ ", " + SMTableapbatchentrylines.sapplytodocnumber
			+ ", " + SMTableapbatchentrylines.scomment
			+ ", " + SMTableapbatchentrylines.sdescription
			+ ", " + SMTableapbatchentrylines.sdistributionacct
			+ ", " + SMTableapbatchentrylines.sdistributioncodename
			+ ")"
			+ " VALUES ("
			+ "" + getsbdamount().trim().replaceAll(",", "")
			+ ", " + getsbddiscountappliedamt().trim().replaceAll(",", "")
			+ ", " + getsbdpayableamt().trim().replace(",", "")
			+ ", " + getsiapplytodoctype()
			+ ", " + getslapplytodocid()
			+ ", " + getsbatchnumber()
			+ ", " + getsentrynumber()
			+ ", " + getslinenumber()
			+ ", " + getslpoheaderid()
			+ ", " + getslporeceiptlineid()
			+ ", " + getslreceiptheaderid()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsapplytodocnumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscomment()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdistributionacct()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdistributioncodename()) + "'"
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableapbatchentrylines.bdamount + " = " + getsbdamount().trim().replace(",", "")
			+ ", " + SMTableapbatchentrylines.bdapplieddiscountamt + " = " + getsbddiscountappliedamt().trim().replace(",", "")
			+ ", " + SMTableapbatchentrylines.bdpayableamount + " = " + getsbdpayableamt().trim().replace(",", "")
			+ ", " + SMTableapbatchentrylines.iapplytodoctype + " = " + getsiapplytodoctype()
			+ ", " + SMTableapbatchentrylines.lapplytodocid + " = " + getslapplytodocid()
			+ ", " + SMTableapbatchentrylines.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTableapbatchentrylines.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTableapbatchentrylines.llinenumber + " = " + getslinenumber()
			+ ", " + SMTableapbatchentrylines.lpoheaderid + " = " + getslpoheaderid()
			+ ", " + SMTableapbatchentrylines.lporeceiptlineid + " = " + getslporeceiptlineid()
			+ ", " + SMTableapbatchentrylines.lreceiptheaderid + " = " + getslreceiptheaderid()
			+ ", " + SMTableapbatchentrylines.sapplytodocnumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsapplytodocnumber()) + "'"
			+ ", " + SMTableapbatchentrylines.scomment  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscomment()) + "'"
			+ ", " + SMTableapbatchentrylines.sdescription  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
			+ ", " + SMTableapbatchentrylines.sdistributionacct  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdistributionacct()) + "'"
			+ ", " + SMTableapbatchentrylines.sdistributioncodename  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdistributioncodename()) + "'"
			;
		
		//System.out.println("[1497979197] Line insert SQL = " + SQL);
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1488915092] updating AP batch entry " + getsentrynumber() + " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//If the batch was newly created, get the new batch number:

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
				throw new Exception("Error [1488915462] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1488915463] Could not get last ID number.");
			}
		}
		return;
	}
	public void validate_fields(Connection conn, int iEntryType, String sEntryID, boolean bBatchIsBeingPosted) throws Exception{
		
		String sResult = "";
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Line ID", -1, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchnumber = clsValidateFormFields.validateLongIntegerField(
				m_sbatchnumber, 
				"Batch number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sentrynumber = clsValidateFormFields.validateLongIntegerField(
					m_sentrynumber, 
				"Entry number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_slinenumber = clsValidateFormFields.validateLongIntegerField(
					m_slinenumber, 
				"Line number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdistributioncodename = clsValidateFormFields.validateStringField(
				m_sdistributioncodename, 
				SMTableapbatchentrylines.sdistributioncodenameLength, 
				"Distribution code", 
				true
			).trim();
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		m_sbdamount = m_sbdamount.replaceAll(",", "");
		try {
			m_sbdamount = clsValidateFormFields.validateBigdecimalField(
				m_sbdamount, 
				"Amount", 
				SMTableapbatchentrylines.bdamountScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (
			iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE
		){
			m_sbddiscountappliedamt = "0.00";
		}
		m_sbddiscountappliedamt = m_sbddiscountappliedamt.replaceAll(",", "");
		try {
			m_sbddiscountappliedamt = clsValidateFormFields.validateBigdecimalField(
					m_sbddiscountappliedamt, 
				"Applied Discount Amount", 
				SMTableapbatchentrylines.bdamountScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		m_sbdpayableamt = m_sbdpayableamt.replaceAll(",", "");
		try {
			m_sbdpayableamt = clsValidateFormFields.validateBigdecimalField(
					m_sbdpayableamt, 
				"Payable Amount", 
				SMTableapbatchentrylines.bdpayableamountScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//We always have to have a distribution acct on the line, except for credit notes and debit notes.  These
		// can be entered without GL accounts, to allow the user to edit them, but at posting time, we'll make sure they're valid:
		try {
			m_sdistributionacct = clsValidateFormFields.validateStringField(
					m_sdistributionacct, 
					SMTableapbatchentrylines.sdistributionacctLength, 
				"Distribution account", 
				(iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Now validate the distribution acct:
		if (m_sdistributionacct.compareToIgnoreCase("") != 0){
			GLAccount glacct = new GLAccount(m_sdistributionacct);
			if(!glacct.load(conn)){
				sResult += "  GL Account '" + m_sdistributionacct + "' could not be found.";
			}
		}
		
		try {
			m_sdescription = clsValidateFormFields.validateStringField(
					m_sdescription, 
					SMTableapbatchentrylines.sdescriptionLength, 
				"Description", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_scomment = clsValidateFormFields.validateStringField(
					m_scomment, 
					SMTableapbatchentrylines.scommentLength, 
				"Comment", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_slpoheaderid = clsValidateFormFields.validateLongIntegerField(
					m_slpoheaderid, 
				"PO number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_slreceiptheaderid = clsValidateFormFields.validateLongIntegerField(
					m_slreceiptheaderid, 
				"PO Receipt number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slporeceiptlineid = clsValidateFormFields.validateLongIntegerField(
				m_slporeceiptlineid, 
				"PO Receipt line ID", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the apply to fields, if this is a payment OR a check reversal:
		if (
			(iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_REVERSAL)
		){
			try {
				m_slapplytodocid = clsValidateFormFields.validateLongIntegerField(
						m_slapplytodocid, 
					"Apply-to document ID", 
					-1L, 
					clsValidateFormFields.MAX_LONG_VALUE);
			} catch (Exception e) {
				sResult += "  " + e.getMessage() + ".";
			}
			try {
				m_sapplytodocnumber = clsValidateFormFields.validateStringField(
						m_sapplytodocnumber, 
						SMTableapbatchentrylines.sapplytodocnumberLength, 
					"Apply-to document number", 
					false
				);
			} catch (Exception e) {
				sResult += "  " + e.getMessage() + ".";
			}
			
			//We only need to check the amount remaining on the apply-to if this entry is just being saved.
			//If this is part of a batch posting process, then by this point, the apply to doc's current amt will already
			// have this application reflected in it, and we can't run this check on it:
			if (!bBatchIsBeingPosted){
				
				//Only check lines that HAVE a valid 'apply-to-doc' ID:
				if (
					(getslapplytodocid().compareToIgnoreCase("0") != 0)
					&& (getslapplytodocid().compareToIgnoreCase("-1") != 0)
					){
					//Now try to read the apply-to doc:
					String SQL = "SELECT"
						+ " " + SMTableaptransactions.sdocnumber
						+ ", " + SMTableaptransactions.bdcurrentamt
						+ ", " + SMTableaptransactions.svendor
						+ " FROM " + SMTableaptransactions.TableName
						+ " WHERE ("
							+ "(" + SMTableaptransactions.lid + " = " + m_slapplytodocid + ")"
						+ ")"
					;
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						if (rs.getString(SMTableaptransactions.sdocnumber).compareToIgnoreCase(m_sapplytodocnumber) != 0){
							sResult += "  Apply-to document number '" + m_sapplytodocnumber + "' is not correct for AP transaction with ID '" + m_slapplytodocid + "'.";
						}
						
						//On a regular invoice, the current amt due is normally a POSITIVE number:
						BigDecimal bdCurrentAmtDue = rs.getBigDecimal(SMTableaptransactions.bdcurrentamt);
						
						//The amount being paid is a negative number....
						BigDecimal bdAmountBeingApplied = new BigDecimal(getsbdamount().replaceAll(",", ""));
						
						//Update the payable amt here:
						setsbdpayableamt(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdpayableamountScale, bdCurrentAmtDue));
						
						//On a regular payment, the discount amount on a payment line is normally a negative number:
						BigDecimal bdDiscountBeingApplied = new BigDecimal(m_sbddiscountappliedamt.replaceAll(",", ""));
						//System.out.println("[1508255713] - bdCurrentAmtDue = " + bdCurrentAmtDue + ", bdDiscountBeingApplied = " + bdDiscountBeingApplied + ".");
						
						//**************************************************************************************************
						//Confirm here that we are not trying to apply a discount amt larger than the total amount due:
						//if (
						//	//The discount being applied is NEGATIVE, and the current amt due is POSITIVE, so we have to reverse the sign of the discount to compare the numbers:
						//	(bdDiscountBeingApplied.negate().compareTo(bdCurrentAmtDue) > 0)
						//	&& (bdDiscountBeingApplied.compareTo(BigDecimal.ZERO) != 0)
						//){
						//	sResult += "  Discount applied (" 
						//		+ bdDiscountBeingApplied.negate() + ") is more than the current amt due (" 
						//		+ bdCurrentAmtDue + ") on document '" + m_sapplytodocnumber + "' (with ID '" + m_slapplytodocid + "') for vendor '" 
						//		+ rs.getString(SMTableaptransactions.svendor) + "'.";
						//}
						
						//****************************************************************************************************
						//Confirm here that the amount being paid PLUS the discount amt does not exceed the total amount payable:
						if (
							(bdAmountBeingApplied.add(bdDiscountBeingApplied).negate()).compareTo(bdCurrentAmtDue) > 0
						){
							sResult += "  The amount being applied (" + bdAmountBeingApplied.negate() + ") PLUS the discount being applied (" 
								+ bdDiscountBeingApplied.negate() + ") is more than the current amt due (" 
								+ bdCurrentAmtDue + ") on document '" + m_sapplytodocnumber + "' (with ID '" + m_slapplytodocid + "') for vendor '" 
								+ rs.getString(SMTableaptransactions.svendor) + "'.";
						}
						
					}else{
						sResult += "  Apply-to document with ID '" + m_slapplytodocid + "' was not found.";
					}
					rs.close();
				}
			}
		}
		
		if (
			(getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_INVOICENUMBER)) != 0)
			&& (getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_ORDERNUMBER)) != 0)
			&& (getsiapplytodoctype().compareToIgnoreCase(Integer.toString(SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_PONUMBER)) != 0)
		){
			sResult += "  Apply to document type '" + getsiapplytodoctype() + "' is not valid.";
		}
		
		//We have to make sure that NO other UNposted entry line is already applying to the same 'apply-to':
		
		if (
			(getslapplytodocid().compareToIgnoreCase("0") != 0)
			&& (getslapplytodocid().compareToIgnoreCase("-1") != 0)
		){
			String SQL = "SELECT"
				+ " " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber
				+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber
				+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.llinenumber
				+ " FROM " + SMTableapbatchentrylines.TableName
				+ " LEFT JOIN " + SMTableapbatchentries.TableName
				+ " ON ("
					+ "(" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber 
						+ " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
					+ " AND (" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber 
						+ " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
					+ ")"
				+ " LEFT JOIN " + SMTableapbatches.TableName
				+ " ON " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber + " = " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber
				+ " WHERE ("
				
					//Ignore this particular entry, but check any others:
					+ " (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid + " != " + sEntryID + ")"
					
					//And only look in UNposted batches:
					+ " AND (" 
						+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
						+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
					+ ")"
					
					//Get any other line that applies to the same document:
					+ " AND ("  + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lapplytodocid + " = " + getslapplytodocid() + ")"
				+ ")"
			;
			String sListOfConflicts = "";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				sListOfConflicts = "";
				while (rs.next()){
					//Double check that we're not catching this same entry - this could happen, even with this query, if the line ID wasn't updated yet:
					if (
						(Long.toString(rs.getLong(SMTableapbatchentrylines.lbatchnumber)).compareToIgnoreCase(getsbatchnumber()) == 0)
						&& (Long.toString(rs.getLong(SMTableapbatchentrylines.lentrynumber)).compareToIgnoreCase(getsentrynumber()) == 0)
					){
						//This is our current entry - don't warn about this....
					}else{
						//But if we've found one that's NOT matching the lid or batch/entry for this line, warn about it:
						if (sListOfConflicts.compareToIgnoreCase("") != 0){
							sListOfConflicts += ", ";
						}
						sListOfConflicts += "batch " + Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber))
							+ ", entry " + Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber))
							+	 ", line " + Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.llinenumber))
						;

					}
					//if (sListOfConflicts.compareToIgnoreCase("") != 0){
					//	break;
					//}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1510277722] querying for apply-to document confilicts with SQL '" + SQL + "' - " + e.getMessage());
			}
			
			if (sListOfConflicts.compareToIgnoreCase("") != 0){
				sResult += "Line " + getslinenumber() + " on entry " + getsentrynumber() + " is being applied to document number '" + getsapplytodocnumber() + "'"
					+ " but the following lines from other unposted entries are ALREADY being applied to that document: "
					+ sListOfConflicts
					+ ".  You must post the other entries before you can apply "
					+ "new lines to it.  ";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
	}
	public void load(Connection conn, String sLid, int iEntryType) throws Exception{
		String SQL = "SELECT * FROM " + SMTableapbatchentrylines.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatchentrylines.lid + " = " + sLid + ")"
			+ ")"
		;
	
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setsiapplytodoctype(Long.toString(rs.getLong(SMTableapbatchentrylines.iapplytodoctype)));
				setslapplytodocid(Long.toString(rs.getLong(SMTableapbatchentrylines.lapplytodocid)));
				setsbatchnumber(Long.toString(rs.getLong(SMTableapbatchentrylines.lbatchnumber)));
				setsentrynumber(Long.toString(rs.getLong(SMTableapbatchentrylines.lentrynumber)));
				setsbdpayableamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.bdpayableamount)));
				setslinenumber(Long.toString(rs.getLong(SMTableapbatchentrylines.llinenumber)));
				setsbdamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.bdamount)));
				setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.bdapplieddiscountamt)));
				setsapplytodocnumber(rs.getString(SMTableapbatchentrylines.sapplytodocnumber));
				setscomment(rs.getString(SMTableapbatchentrylines.scomment));
				setsdescription(rs.getString(SMTableapbatchentrylines.sdescription));
				setsdistributionacct(rs.getString(SMTableapbatchentrylines.sdistributionacct));
				setsdistributioncodename(rs.getString(SMTableapbatchentrylines.sdistributioncodename));
				setslid(Long.toString(rs.getLong(SMTableapbatchentrylines.lid)));
				setslpoheaderid(Long.toString(rs.getLong(SMTableapbatchentrylines.lpoheaderid)));
				setslreceiptheaderid(Long.toString(rs.getLong(SMTableapbatchentrylines.lreceiptheaderid)));
				setslporeceiptlineid(Long.toString(rs.getLong(SMTableapbatchentrylines.lporeceiptlineid)));
			}else{
				rs.close();
				throw new Exception("Error [1489248030] - No AP batch entry line found with lid = " + sLid + ".");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1489248031] - " + e.getMessage());
		}
	}
	public void checkDistributionAccount(Connection conn) throws Exception{
		
		if (getsdistributionacct().compareToIgnoreCase("") == 0){
			throw new Exception("Missing GL account on entry " + getsentrynumber() + ", line " + getslinenumber());
		}
		//ALL entry lines must have a valid GL account:
		GLAccount glacct = new GLAccount(getsdistributionacct());
		if(!glacct.load(conn)){
			throw new Exception("Invalid GL account '" + getsdistributionacct() 
			+ " on entry number " + getsentrynumber()
				+ ", line " + getslinenumber()
			);
		}
	}
	public APBatchEntryLine copyLine(){
		APBatchEntryLine newline = new APBatchEntryLine();
		newline.setsapplytodocnumber(this.getsapplytodocnumber());
		newline.setsbatchnumber(this.getsbatchnumber());
		newline.setsbdamount(this.getsbdamount());
		newline.setsbddiscountappliedamt(this.getsbddiscountappliedamt());
		newline.setsbdpayableamt(this.getsbdpayableamt());
		newline.setscomment(this.getscomment());
		newline.setsdescription(this.getsdescription());
		newline.setsdistributionacct(this.getsdistributionacct());
		newline.setsdistributioncodename(this.getsdistributioncodename());
		newline.setsentrynumber(this.getsentrynumber());
		newline.setsiapplytodoctype(this.getsiapplytodoctype());
		newline.setslapplytodocid(this.getslapplytodocid());
		newline.setslid(this.getslid());
		newline.setslinenumber(this.getslinenumber());
		newline.setslpoheaderid(this.getslpoheaderid());
		newline.setslporeceiptlineid(this.getslporeceiptlineid());
		newline.setslreceiptheaderid(this.getslreceiptheaderid());
		return newline;
	}
	public String getslid(){
		return m_slid;
	}
	public void setslid(String sLid){
		m_slid = sLid;
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
	public String getslinenumber(){
		return m_slinenumber;
	}
	public void setslinenumber(String sLineNumber){
		m_slinenumber = sLineNumber;
	}
	public String getsdistributioncodename(){
		return m_sdistributioncodename;
	}
	public void setsdistributioncodename(String sdistributioncodename){
		m_sdistributioncodename = sdistributioncodename;
	}
	public String getsbdamount(){
		return m_sbdamount;
	}

	public void setsbdamount(String sbdamount){
		m_sbdamount = sbdamount;
	}
	public String getsbddiscountappliedamt(){
		return m_sbddiscountappliedamt;
	}

	public void setsbddiscountappliedamt(String sbddiscountappliedamount){
		m_sbddiscountappliedamt = sbddiscountappliedamount;
	}
	
	public String getsdistributionacct(){
		return m_sdistributionacct;
	}
	public void setsdistributionacct(String sdistributionacct){
		m_sdistributionacct = sdistributionacct;
	}
	public String getsdescription(){
		return m_sdescription;
	}
	public void setsdescription(String sdescription){
		m_sdescription = sdescription;
	}
	public String getscomment(){
		return m_scomment;
	}
	public void setscomment(String scomment){
		m_scomment = scomment;
	}
	
	public String getslpoheaderid(){
		return m_slpoheaderid;
	}
	public void setslpoheaderid(String slpoheaderid){
		m_slpoheaderid = slpoheaderid;
	}
	
	public String getslreceiptheaderid(){
		return m_slreceiptheaderid;
	}
	public void setslreceiptheaderid(String slreceiptheaderid){
		m_slreceiptheaderid = slreceiptheaderid;
	}
	
	public String getslporeceiptlineid(){
		return m_slporeceiptlineid;
	}
	public void setslporeceiptlineid(String slporeceiptlineid){
		m_slporeceiptlineid = slporeceiptlineid;
	}

	public String getslapplytodocid(){
		return m_slapplytodocid;
	}
	public void setslapplytodocid(String slapplytodocid){
		m_slapplytodocid = slapplytodocid;
	}	
	public String getsapplytodocnumber(){
		return m_sapplytodocnumber;
	}
	public void setsapplytodocnumber(String sapplytodocnumber){
		m_sapplytodocnumber = sapplytodocnumber;
	}
	public String getsiapplytodoctype(){
		return m_iapplytodoctype;
	}
	public void setsiapplytodoctype(String sApplyToDocType){
		m_iapplytodoctype = sApplyToDocType;
	}
	public String getsbdpayableamt(){
		return m_sbdpayableamt;
	}
	public void setsbdpayableamt(String sbdpayableamt){
		m_sbdpayableamt = sbdpayableamt;
	}
	
	public String dumpData(){
		String s = "";
		s += "    Apply-to doc number:" + getsapplytodocnumber() + "\n";
		s += "    Batch number:" + getsbatchnumber() + "\n";
		s += "    Amt:" + getsbdamount() + "\n";
		s += "    Disc applied amt:" + getsbddiscountappliedamt() + "\n";
		s += "    Comment:" + getscomment() + "\n";
		s += "    Description:" + getsdescription() + "\n";
		s += "    Dist acct:" + getsdistributionacct() + "\n";
		s += "    Dist code:" + getsdistributioncodename() + "\n";
		s += "    Entry #:" + getsentrynumber() + "\n";
		s += "    Apply-to doc ID:" + getslapplytodocid() + "\n";
		s += "    Line ID:" + getslid() + "\n";
		s += "    Line number:" + getslinenumber() + "\n";
		s += "    PO ID:" + getslpoheaderid() + "\n";
		s += "    PO Receipt ID:" + getslreceiptheaderid() + "\n";
		s += "    PO Receipt Line ID:" + getslporeceiptlineid() + "\n";
		s += "    Apply-to doc type:" + getsiapplytodoctype() + "\n";
		s += "    Payable amt:" + getsbdpayableamt() + "\n";
		
		return s;
	}
	private void initializeVariables(){
		m_slid  = "-1";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_slinenumber = "0";
		m_sdistributioncodename = "";
		m_sbdamount = "0.00";
		m_sdistributionacct = "";
		m_sdescription = "";
		m_scomment = "";
		m_slpoheaderid = "0";
		m_slreceiptheaderid = "0";
		m_slporeceiptlineid = "0";
		m_slapplytodocid = "0";
		m_sapplytodocnumber = "";
		m_sbddiscountappliedamt = "0.00";
		m_iapplytodoctype = "0";
		m_sbdpayableamt = "0.00";
	}
}
