package smbk;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smgl.GLAccount;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablebkaccountentries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class BKAddEntryAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private String sStatementID = "";
	private String sEntryType = "";
	private String sAmt = "";
	private String sDescription = "";
	private String sEntryDate = "";
	private String sGLAccount = "";
	private String sDocNumber = "";
	private String sBankID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.BKEditStatements)){return;}

		//Pick up all the fields we need here:
		sStatementID = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.lstatementid, request);
		sEntryType = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.ientrytype, request);
		sAmt = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.bdamount, request).trim();
		sDescription = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, request).trim();
		sEntryDate = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.datentrydate, request).trim();
		sGLAccount = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sglaccount, request).trim();
		sDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdocnumber, request).trim();
		sBankID = clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request);

		String sRedirectStringParameters =
				BKBankStatement.Paramlid + "=" + sStatementID
			+ "&" + BKBankStatement.Paramlbankid + "=" + sBankID
			+ "&" + SMTablebkaccountentries.bdamount + "=" + sAmt
			+ "&" + SMTablebkaccountentries.datentrydate + "=" + sEntryDate
			+ "&" + SMTablebkaccountentries.ientrytype + "=" + sEntryType
			+ "&" + SMTablebkaccountentries.sdescription + "=" + sDescription
			+ "&" + SMTablebkaccountentries.sdocnumber + "=" + sDocNumber
		;
		if (sStatementID.compareToIgnoreCase("-1") == 0){
			sRedirectStringParameters += "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y";
		}
		try {
			validate_entries(request, smaction);
		} catch (Exception e) {
			smaction.redirectAction(
				"Error validating entry - " + e.getMessage() + ".", 
				"", 
				sRedirectStringParameters
			);
			return;
		}
		
		//Save the record here:
		try {
			add_account_entry(request, smaction);
		} catch (Exception e) {
			smaction.redirectAction(
				"Error validating entry - " + e.getMessage() + ".", 
				"", 
				sRedirectStringParameters
			);
			return;
		}
			
		String sRedirectString = 
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKEditStatementEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&Status=Entry added successfully."
			+ "&" + sRedirectStringParameters
			;
		try {
			response.sendRedirect(sRedirectString);
		} catch (IOException e) {
			smaction.getPwOut().println("In " + this.toString() 
					+ ".redirectAction - IOException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		} catch (IllegalStateException e) {
			smaction.getPwOut().println("In " + this.toString() 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		}
	}
	private void validate_entries(HttpServletRequest request, SMMasterEditAction sm) throws Exception{
		String sErrors = "";
		boolean bIsValid = true;

		if (
			(sEntryType.compareToIgnoreCase(Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT)) != 0)
			&& (sEntryType.compareToIgnoreCase(Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL)) != 0)
		){
			sErrors += "Statement ID '" + sStatementID + "' is invalid.  ";
			bIsValid = false;
		}
		try {
			BigDecimal bdAmt = new BigDecimal(sAmt);
			if (bdAmt.compareTo(BigDecimal.ZERO) <= 0){
				sErrors += "Amount must be greater than zero.  ";
				bIsValid = false;
			}
		} catch (Exception e) {
			sErrors += "Amount '" + sAmt + "' is invalid.  ";
			bIsValid = false;
		}
		if (sDescription.length() > SMTablebkaccountentries.sdescriptionlength){
			sErrors += "Description is too long.  ";
			bIsValid = false;
		}
		if (sDescription.compareToIgnoreCase("") == 0){
			sErrors += "Description cannot be blank.  ";
			bIsValid = false;
		}
		
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEntryDate)){
			sErrors += "Entry date '" + sEntryDate + "' is invalid.";
			bIsValid = false;
		}
		GLAccount gl = new GLAccount(sGLAccount);
		if (!gl.load(getServletContext(), sm.getsDBID())){
			sErrors += "GL Account '" + sGLAccount + "' is not valid.";
			bIsValid = false;
		}else{
			if (gl.getM_sactive().compareToIgnoreCase("1") != 0){
				sErrors += "GL Account '" + sGLAccount + "' is inactive.";
				bIsValid = false;
			}
		}
		if (sDocNumber.length() > SMTablebkaccountentries.sdocnumberlength){
			sErrors += "Document number is too long.  ";
			bIsValid = false;
		}
		if (sDocNumber.compareToIgnoreCase("") == 0){
			sErrors += "Document number cannot be blank.  ";
			bIsValid = false;
		}
		if (!bIsValid){
			throw new Exception(sErrors);
		}
	}
	private void add_account_entry(HttpServletRequest request, SMMasterEditAction sm) throws Exception{
		String sInsertedAmt = sAmt;
		if (sEntryType.compareToIgnoreCase(Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL)) == 0){
			sInsertedAmt = "-1 * " + sAmt;
		}
		String SQL = "INSERT INTO " + SMTablebkaccountentries.TableName + "("
			+ SMTablebkaccountentries.bdamount
			+ ", " + SMTablebkaccountentries.datentrydate
			+ ", " + SMTablebkaccountentries.ibatchentrynumber
			+ ", " + SMTablebkaccountentries.ibatchnumber
			+ ", " + SMTablebkaccountentries.ibatchtype
			+ ", " + SMTablebkaccountentries.icleared
			+ ", " + SMTablebkaccountentries.ientrytype
			+ ", " + SMTablebkaccountentries.lstatementid
			+ ", " + SMTablebkaccountentries.sdescription
			+ ", " + SMTablebkaccountentries.sdocnumber
			+ ", " + SMTablebkaccountentries.sglaccount
			+ ", " + SMTablebkaccountentries.ssourcemodule
			+ ") VALUES ("
			+ sInsertedAmt
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sEntryDate) + "'"
			+ ", -1"
			+ ", -1"
			+ ", -1"
			+ ", 0"
			+ ", " + sEntryType
			+ ", " + SMTablebkaccountentries.INITIAL_STATEMENT_ID_VALUE
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDocNumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sGLAccount) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(SMTablebkaccountentries.SOURCE_MODULE_MANUAL_ENTRY) + "'"
			+ ")"
		;
		try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", SMUtilities.getFullClassName(this.toString()) + ".add_account_entry - user: " + sm.getUserName()
			);
		} catch (Exception e) {
			throw new Exception("Error inserting entry with SQL: " + SQL + " - " + e.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}