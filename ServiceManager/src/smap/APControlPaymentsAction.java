package smap;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APControlPaymentsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String ON_HOLD_WITHOUT_REASON_ERROR = "ONHOLDERROR";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APControlPayments)){return;}
	    
	    //If the save fails:
	    try {
			savePaymentInformation(smaction, request);
		} catch (Exception e) {
			String sOnHold = "";
			String sErrorMessage = e.getMessage();
			if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.ionhold, request).compareToIgnoreCase("") != 0){
				sOnHold = "Y";
			}
			
			if (e.getMessage().compareToIgnoreCase(ON_HOLD_WITHOUT_REASON_ERROR) == 0) {
				sOnHold = "N";
				sErrorMessage = "If you are placing this transaction on hold, the 'On hold reason' cannot be blank.";
			}
			
			smaction.redirectAction(
					"Error updating transaction - " + sErrorMessage, 
					"", 
		    		SMTableaptransactions.svendor + "=" + clsServletUtilities.URLEncode(clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, request))
		    		+ "&" + SMTableaptransactions.sdocnumber + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, request)
		    		+ "&" + SMTableaptransactions.bdcurrentdiscountavailable + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.bdcurrentdiscountavailable, request)
		    		+ "&" + SMTableaptransactions.datdiscountdate + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datdiscountdate, request)
		    		+ "&" + SMTableaptransactions.datduedate + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datduedate, request)
		    		+ "&" + SMTableaptransactions.ionhold + "=" + sOnHold
		    		+ "&" + APControlPaymentsEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
				);
		}

	    //If it succeeds:
		smaction.redirectAction(
			"", 
			"AP transaction modified successfully", 
    		SMTableaptransactions.svendor + "=" + clsServletUtilities.URLEncode(clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, request))
    		+ "&" + SMTableaptransactions.sdocnumber+ "=" + clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, request)
		);
		return;
	}
	private void savePaymentInformation(SMMasterEditAction sm, HttpServletRequest req) throws Exception{
		
		String sValidationResult = "";
		//Validate the modifiable fields:
		String sVendor = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, req).trim();
		try {
			sVendor = clsValidateFormFields.validateStringField(sVendor, SMTableaptransactions.svendorlength, "Vendor", false);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		
		String sDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, req).trim();
		try {
			sDocNumber = clsValidateFormFields.validateStringField(sDocNumber, SMTableaptransactions.sdocnumberlength, "Document number", false);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		
		String sDiscountAvailable = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.bdcurrentdiscountavailable, req);
		try {
			sDiscountAvailable = clsValidateFormFields.validateBigdecimalField(
					sDiscountAvailable, 
					"Discount available", 
					SMTableaptransactions.bdcurrentdiscountavailableScale,
					new BigDecimal("-9999999.99"),
					new BigDecimal("9999999.99")
					).replaceAll(",", "");
		} catch (Exception e) {
			sValidationResult += "  " + e.getMessage() + ".";
		}
		
		String sDiscountDate = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datdiscountdate, req);
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, sDiscountDate)){
			try {
				sDiscountDate = clsDateAndTimeConversions.convertDateFormat(sDiscountDate, SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		try {
			sDiscountDate = clsValidateFormFields.validateStandardDateField(sDiscountDate, "Discount date", true);
		} catch (Exception e) {
			sValidationResult += "  " + e.getMessage() + ".";
		}
		
		String sDueDate = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datduedate, req);
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, sDueDate)){
			try {
				sDueDate = clsDateAndTimeConversions.convertDateFormat(sDueDate, SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		try {
			sDueDate = clsValidateFormFields.validateStandardDateField(sDueDate, "Due date", false);
		} catch (Exception e) {
			sValidationResult += "  " + e.getMessage() + ".";
		}
		
		String sOnHold = "0";
		if (req.getParameter(SMTableaptransactions.ionhold) != null){
			sOnHold = "1";
		}
		
		//On hold user id:
		String sOnHoldUserID = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.lonholdbyuserid, req).trim();
		try {
			sOnHoldUserID = clsValidateFormFields.validateLongIntegerField(sOnHoldUserID, "On hold user ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		//System.out.println("[202072958390] " + "sOnHoldUserID = " + sOnHoldUserID);
		
		//On hold full user name:
		String sOnHoldFullUserName = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sonholdbyfullname, req).trim();
		try {
			sOnHoldFullUserName = clsValidateFormFields.validateStringField(
				sOnHoldFullUserName, 
				SMTableaptransactions.sonholdbyfullnamelength, 
				"On hold user full name", true);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		//System.out.println("[20207295905] " + "sOnHoldFullUserName = '" + sOnHoldFullUserName + "'.");
		
		//On hold PO Header ID:
		String sOnHoldPOHeaderID = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.lonholdpoheaderid, req).trim();
		try {
			sOnHoldPOHeaderID = clsValidateFormFields.validateLongIntegerField(sOnHoldPOHeaderID, "On hold PO header ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		
		//On hold date:
		String sOnHoldDate = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datplacedonhold, req);
		try {
			sOnHoldDate = clsValidateFormFields.validateDateTimeField(sOnHoldDate, "On hold date", SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, true);
		} catch (Exception e) {
			sValidationResult += "  " + e.getMessage() + ".";
		}
		//System.out.println("[202072959210] " + "sOnHoldDate = '" + sOnHoldDate + "'.");
		
		//On hold reason:
		String sOnHoldReason = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.monholdreason, req).trim();
		try {
			sOnHoldReason = clsValidateFormFields.validateStringField(sOnHoldReason, 16000, "On hold reason", true);
		} catch (Exception e1) {
			sValidationResult += "  " + e1.getMessage() + ".";
		}
		//System.out.println("[202072959435] " + "sOnHoldReason = '" + sOnHoldReason + "'.");
		
		if (sValidationResult.compareToIgnoreCase("") != 0){
			throw new Exception(sValidationResult);
		}

		//System.out.println("[202072102318] " + "sOnHold = '" + sOnHold + "'.");
		
		//First, get the current on hold status of the transaction
		boolean bTransactionWasAlreadyOnHold = false;
		String sSQL = "SELECT"
			+ " " + SMTableaptransactions.ionhold
			+ " FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + sVendor + "')"
				+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + sDocNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".savePaymentInformation - user: " + sm.getFullUserName()
			);
			if (rs.next()){
				if (rs.getInt(SMTableaptransactions.ionhold) == 1){
					bTransactionWasAlreadyOnHold = true;
				}
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [202072930501] " + "reading on hold status of existing transaction with SQL = '" + sSQL + "' - " + e1.getMessage());
		}
		
		if (sOnHold.compareToIgnoreCase("0") == 0){
			//System.out.println("[20207210391] " + "sOnHold == 0");
			sOnHoldUserID = "0";
			sOnHoldFullUserName = "";
			sOnHoldDate = SMUtilities.EMPTY_DATETIME_VALUE;
			sOnHoldReason = "";
			sOnHoldPOHeaderID = "0";
		}else{
			//System.out.println("[20207210391] " + "sOnHold != 0");
			//If the payment is being PLACED on hold for the first time, get the user and date info, and require a reason:

			
			//If the user is setting it on hold for the first time, then get the additional info now:
			//System.out.println("[202072103562] " + "bTransactionWasAlreadyOnHold = " + bTransactionWasAlreadyOnHold);
			if (!bTransactionWasAlreadyOnHold){
				//Get the user info, etc.:
				sOnHoldUserID = sm.getUserID();
				sOnHoldFullUserName = sm.getFullUserName();
				sOnHoldPOHeaderID = "0";  //User can't set an on hold PO number from here
				
				if (sOnHoldReason.compareToIgnoreCase("") == 0) {
					throw new Exception(ON_HOLD_WITHOUT_REASON_ERROR);
				}
				try {
					ServletUtilities.clsDBServerTime dbtime;
					dbtime = new ServletUtilities.clsDBServerTime(sm.getsDBID(), sm.getFullUserName(), getServletContext());
					sOnHoldDate = dbtime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATETIME_FORMAT_FOR_DISPLAY);
					//System.out.println("[202003271414] - sOnHoldDate = '" + sOnHoldDate + "'.");
				} catch (Exception e) {
					throw new Exception("Error [202072930130] " + "Could not get current date and time - " + e.getMessage());
				}
			}
		}
		
		String SQL = "UPDATE " + SMTableaptransactions.TableName
			+ " SET " + SMTableaptransactions.bdcurrentdiscountavailable + " = " + sDiscountAvailable.replaceAll(",", "")
			+ ", " + SMTableaptransactions.datdiscountdate + " = '" + clsDateAndTimeConversions.convertDateFormat(
				sDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
			+ ", " + SMTableaptransactions.datduedate + " = '" + clsDateAndTimeConversions.convertDateFormat(
				sDueDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
			+ ", " + SMTableaptransactions.ionhold + " = " + sOnHold
			+ ", " + SMTableaptransactions.lonholdbyuserid + " = " + sOnHoldUserID
			+ ", " + SMTableaptransactions.sonholdbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sOnHoldFullUserName) + "'"
			+ ", " + SMTableaptransactions.datplacedonhold + " = '" + clsDateAndTimeConversions.convertDateFormat(
				sOnHoldDate, 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
				SMUtilities.DATETIME_24HR_FORMAT_FOR_SQL, 
				SMUtilities.EMPTY_SQL_DATETIME_VALUE) + "'"
			+ ", " + SMTableaptransactions.monholdreason + " = '" + clsDatabaseFunctions.FormatSQLStatement(sOnHoldReason) + "'"
					+ ", " + SMTableaptransactions.lonholdpoheaderid + " = " + sOnHoldPOHeaderID
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + sVendor + "')"
				+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + sDocNumber + "')"
			+ ")"
		;
		//System.out.println("[1493058737] SQL = " + SQL);
		try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".savePaymentInformation - user: " + sm.getUserName(), getServletContext());
		} catch (Exception e) {
			throw new Exception("Error [1493058206] updating AP transaction with SQL: " + SQL + " - " + e.getMessage());
		}
		
    	//If it was already on hold, but is being taken OFF hold now, log it:
    	if ((bTransactionWasAlreadyOnHold) && (sOnHold.compareToIgnoreCase("0") == 0)){
    		SMLogEntry log = new SMLogEntry(sm.getsDBID(), getServletContext());
    		log.writeEntry(
    			sm.getUserID(), 
    			SMLogEntry.LOG_OPERATION_APINVOICEONHOLD, 
    			"Vendor '" + sVendor + "', document number '" + sDocNumber + "' taken off hold", 
    			"TAKEN OFF HOLD", 
    			"[1585596603]");
    	}
    	
    	//If it was NOT previously on hold, but is being put ON hold now, log it:
    	if ((!bTransactionWasAlreadyOnHold) && (sOnHold.compareToIgnoreCase("1") == 0)) {
       		SMLogEntry log = new SMLogEntry(sm.getsDBID(), getServletContext());
    		log.writeEntry(
    			sm.getUserID(), 
    			SMLogEntry.LOG_OPERATION_APINVOICEONHOLD, 
    			"Vendor '" + sVendor + "', document number '" + sDocNumber + " REASON: '"
    			+ sOnHoldReason + "'", 
    			"PUT ON HOLD", 
    			"[1585596604]");
    	}

	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}