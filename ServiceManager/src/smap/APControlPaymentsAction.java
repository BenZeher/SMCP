package smap;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APControlPaymentsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
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
			if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.ionhold, request).compareToIgnoreCase("") != 0){
				sOnHold = "Y";
			}
			smaction.redirectAction(
					"Error updating transaction - " + e.getMessage(), 
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
		
		if (sValidationResult.compareToIgnoreCase("") != 0){
			throw new Exception(sValidationResult);
		}

		String SQL = "UPDATE " + SMTableaptransactions.TableName
			+ " SET " + SMTableaptransactions.bdcurrentdiscountavailable + " = " + sDiscountAvailable.replaceAll(",", "")
			+ ", " + SMTableaptransactions.datdiscountdate + " = '" + clsDateAndTimeConversions.convertDateFormat(
				sDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
			+ ", " + SMTableaptransactions.datduedate + " = '" + clsDateAndTimeConversions.convertDateFormat(
				sDueDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
			+ ", " + SMTableaptransactions.ionhold + " = " + sOnHold
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
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}