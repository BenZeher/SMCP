package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableapchecklines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsNumberToEnglishWords;
import ServletUtilities.clsServletUtilities;
import smbk.BKBank;
import smcontrolpanel.SMUtilities;

public class APCheckFormProcessor {

	private final static String VARIABLE_STARTING_DELIMITER = "[[";
	private final static String VARIABLE_ENDING_DELIMITER = "]]";
	
	private final static String EMPTY_CHECK_AMOUNT_STRING = "XXXX";
	
	private final static String CHECK_VARIABLE_CHECK_DATE = VARIABLE_STARTING_DELIMITER + "CKDATE" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_CHECK_AMOUNT = VARIABLE_STARTING_DELIMITER + "CKAMOUNT" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_CHECK_NUMBER = VARIABLE_STARTING_DELIMITER + "CKNUMBER" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_CHECK_REMIT_TO_NAME = VARIABLE_STARTING_DELIMITER + "CKREMITTONAME" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_CHECK_AMOUNT_IN_TEXT = VARIABLE_STARTING_DELIMITER + "CKAMOUNTINTEXT" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ACCOUNT_NUMBER = VARIABLE_STARTING_DELIMITER + "CKVENDORACCTNUMBER" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_LINE_1 = VARIABLE_STARTING_DELIMITER + "CKVENDORADD1" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_LINE_2 = VARIABLE_STARTING_DELIMITER + "CKVENDORADD2" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_LINE_3 = VARIABLE_STARTING_DELIMITER + "CKVENDORADD3" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_LINE_4 = VARIABLE_STARTING_DELIMITER + "CKVENDORADD4" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_CITY = VARIABLE_STARTING_DELIMITER + "CKVENDORCITY" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_STATE = VARIABLE_STARTING_DELIMITER + "CKVENDORSTATE" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_POSTAL_CODE = VARIABLE_STARTING_DELIMITER + "CKVENDORZIP" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_VENDOR_ADDRESS_COUNTRY = VARIABLE_STARTING_DELIMITER + "CKVENDORCOUNTRY" + VARIABLE_ENDING_DELIMITER;
	
	private final static String CHECK_VARIABLE_BANK_NAME = VARIABLE_STARTING_DELIMITER + "CKBANKNAME" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_ADDRESS_LINE_1 = VARIABLE_STARTING_DELIMITER + "CKBANKADD1" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_ADDRESS_LINE_2 = VARIABLE_STARTING_DELIMITER + "CKBANKADD2" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_ADDRESS_LINE_3 = VARIABLE_STARTING_DELIMITER + "CKBANKADD3" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_ADDRESS_LINE_4 = VARIABLE_STARTING_DELIMITER + "CKBANKADD4" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_CITY = VARIABLE_STARTING_DELIMITER + "CKBANKCITY" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_STATE = VARIABLE_STARTING_DELIMITER + "CKBANKSTATE" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_COUNTRY = VARIABLE_STARTING_DELIMITER + "CKBANKCOUNTRY" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_POSTAL_CODE = VARIABLE_STARTING_DELIMITER + "CKBANKZIP" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_BANK_ROUTING_NUMBER = VARIABLE_STARTING_DELIMITER + "CKBANKRTINGNO" + VARIABLE_ENDING_DELIMITER;
	
	private final static String CHECK_VARIABLE_COMPANY_NAME = VARIABLE_STARTING_DELIMITER + "CKCOMPANYNAME" + VARIABLE_ENDING_DELIMITER;
	
	private final static String CHECK_VARIABLE_COMPANY_ADDRESS_LINE_1 = VARIABLE_STARTING_DELIMITER + "CKCOMPADD1" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_ADDRESS_LINE_2 = VARIABLE_STARTING_DELIMITER + "CKCOMPADD2" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_ADDRESS_LINE_3 = VARIABLE_STARTING_DELIMITER + "CKCOMPADD3" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_ADDRESS_LINE_4 = VARIABLE_STARTING_DELIMITER + "CKCOMPADD4" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_CITY = VARIABLE_STARTING_DELIMITER + "CKCOMPCITY" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_STATE = VARIABLE_STARTING_DELIMITER + "CKCOMPSTATE" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_COUNTRY = VARIABLE_STARTING_DELIMITER + "CKCOMPCOUNTRY" + VARIABLE_ENDING_DELIMITER;
	private final static String CHECK_VARIABLE_COMPANY_POSTAL_CODE = VARIABLE_STARTING_DELIMITER + "CKCOMPZIP" + VARIABLE_ENDING_DELIMITER;
	
	//Advice (line) variables:
	private final static String ADVICE_VARIABLE_DOC_NUMBER = VARIABLE_STARTING_DELIMITER + "ADDOCNUM" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_DOC_DATE = VARIABLE_STARTING_DELIMITER + "ADDOCDATE" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_GROSS_AMOUNT = VARIABLE_STARTING_DELIMITER + "ADGROSSAMT" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_DEDUCTIONS = VARIABLE_STARTING_DELIMITER + "ADDEDUCTIONS" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_NET_PAID = VARIABLE_STARTING_DELIMITER + "ADNETPAID" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_GROSS_AMOUNT_TOTAL = VARIABLE_STARTING_DELIMITER + "ADGROSSTOT" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_DEDUCTIONS_TOTAL = VARIABLE_STARTING_DELIMITER + "ADDEDTOT" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_NET_PAID_TOTAL = VARIABLE_STARTING_DELIMITER + "ADNETTOT" + VARIABLE_ENDING_DELIMITER;
	
	//Tags to designate the beginning and end of the 'repeatable' advice section:
	private final static String ADVICE_VARIABLE_START_TAG = VARIABLE_STARTING_DELIMITER + "ADBEGIN" + VARIABLE_ENDING_DELIMITER;
	private final static String ADVICE_VARIABLE_END_TAG = VARIABLE_STARTING_DELIMITER + "ADEND" + VARIABLE_ENDING_DELIMITER;
	
	private ArrayList<String>arrVariableNames = new ArrayList<String>(0);
	private ArrayList<String>arrVariableDescriptions = new ArrayList<String>(0);

	public APCheckFormProcessor()	{
		initializeVariables();
	}

	public String printIndividualCheck(
			Connection conn,
			APCheck check,
			String sUserID
		) throws Exception{
			
			String s = "";
			int iMaxNumberOfLinesToPrintPerPage = 0;
			String SQL = "SELECT"
				+ " * FROM " + SMTableapcheckforms.TableName
				+  " WHERE ("
					+ "(" + SMTableapcheckforms.lid + " = " + check.getsicheckformid() + ")"
				+ ")"
			;
			ResultSet rs;
			try {
				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					s = rs.getString(SMTableapcheckforms.mtext);
					iMaxNumberOfLinesToPrintPerPage = rs.getInt(SMTableapcheckforms.inumberofadvicelinesperpage);
					rs.close();
				}else{
					rs.close();
					throw new Exception("Error [1505508578] - could not read check form with ID '" + check.getsicheckformid() + "'.");
				}
			} catch (Exception e1) {
				throw new Exception("Error [1505508579] - could not read check forms with SQL '" + SQL + "' - " + e1.getMessage());
			}
			
			BKBank bank = new BKBank();
			bank.setslid(check.getslbankid());
			try {
				bank.load(conn);
			} catch (Exception e) {
				throw new Exception("Error [1505163280] loading bank with ID '" + check.getslbankid() + "' - " + e.getMessage());
			}
			
			s = s.replace(CHECK_VARIABLE_BANK_NAME, bank.getsbankname());
			s = s.replace(CHECK_VARIABLE_BANK_ADDRESS_LINE_1, bank.getsaddressline1());
			s = s.replace(CHECK_VARIABLE_BANK_ADDRESS_LINE_2, bank.getsaddressline2());
			s = s.replace(CHECK_VARIABLE_BANK_ADDRESS_LINE_3, bank.getsaddressline3());
			s = s.replace(CHECK_VARIABLE_BANK_ADDRESS_LINE_4, bank.getsaddressline3());
			s = s.replace(CHECK_VARIABLE_BANK_CITY, bank.getscity());
			s = s.replace(CHECK_VARIABLE_BANK_STATE, bank.getsstate());
			s = s.replace(CHECK_VARIABLE_BANK_COUNTRY, bank.getscountry());
			s = s.replace(CHECK_VARIABLE_BANK_POSTAL_CODE, bank.getspostalcode());
			s = s.replace(CHECK_VARIABLE_BANK_ROUTING_NUMBER, bank.getsroutingnumber());
			
			s = s.replace(CHECK_VARIABLE_CHECK_DATE, check.getsdatcheckdate());
			String sCheckAmt = check.getscheckamount();
			String sCheckAmtWrittenInText = getAmountInWrittenText(check.getscheckamount());
			if (sCheckAmt.compareToIgnoreCase("0.00") == 0){
				sCheckAmt = EMPTY_CHECK_AMOUNT_STRING;
				sCheckAmtWrittenInText = EMPTY_CHECK_AMOUNT_STRING;
			}
			s = s.replace(CHECK_VARIABLE_CHECK_AMOUNT, sCheckAmt);
			s = s.replace(CHECK_VARIABLE_CHECK_NUMBER, check.getschecknumber());
			s = s.replace(CHECK_VARIABLE_CHECK_REMIT_TO_NAME, check.getsremittoname());
			s = s.replace(CHECK_VARIABLE_CHECK_AMOUNT_IN_TEXT, sCheckAmtWrittenInText);
			s = s.replace(CHECK_VARIABLE_VENDOR_ACCOUNT_NUMBER, check.getsvendoracct());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_1, check.getsremittoaddressline1());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_2, check.getsremittoaddressline2());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_3, check.getsremittoaddressline3());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_4, check.getsremittoaddressline4());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_CITY, check.getsremittocity());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_STATE, check.getsremittostate());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_POSTAL_CODE, check.getsremittopostalcode());
			s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_COUNTRY, check.getsremittocountry());
				
			//Get the company profile info:
			SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName
			;
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				s = s.replace(CHECK_VARIABLE_COMPANY_NAME, rs.getString(SMTablecompanyprofile.sCompanyName));
				s = s.replace(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_1, rs.getString(SMTablecompanyprofile.sAddress01));
				s = s.replace(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_2, rs.getString(SMTablecompanyprofile.sAddress02));
				s = s.replace(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_3, rs.getString(SMTablecompanyprofile.sAddress03));
				s = s.replace(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_4, rs.getString(SMTablecompanyprofile.sAddress04));
				s = s.replace(CHECK_VARIABLE_COMPANY_CITY, rs.getString(SMTablecompanyprofile.sCity));
				s = s.replace(CHECK_VARIABLE_COMPANY_STATE, rs.getString(SMTablecompanyprofile.sState));
				s = s.replace(CHECK_VARIABLE_COMPANY_COUNTRY, rs.getString(SMTablecompanyprofile.sCountry));
				s = s.replace(CHECK_VARIABLE_COMPANY_POSTAL_CODE, rs.getString(SMTablecompanyprofile.sZipCode));
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1503431161] - No company profile information found.");
			}
			
			s = replaceAdviceLines(check, s, conn, iMaxNumberOfLinesToPrintPerPage, Integer.parseInt(check.getsipagenumber()));
			
			return s;
			
		}
	private String replaceAdviceLines(
		APCheck check, 
		String sCheckFormatString, 
		Connection conn, 
		int iMaxNumberOfAdviceLinesToPrintPerPage, 
		int iPageNumber) throws Exception{
		String s = sCheckFormatString;
		
		//If there is NO advice beginning AND ending tags, then don't print ANY advice lines:
		if (
			(sCheckFormatString.indexOf(ADVICE_VARIABLE_START_TAG) < 0)
			|| (sCheckFormatString.indexOf(ADVICE_VARIABLE_END_TAG) + ADVICE_VARIABLE_END_TAG.length() < 0)
		){
			return s;
		}
		
		//First, get the advice format line:
		String sAdviceFormatString = sCheckFormatString.substring(
			sCheckFormatString.indexOf(ADVICE_VARIABLE_START_TAG),
			sCheckFormatString.indexOf(ADVICE_VARIABLE_END_TAG) + ADVICE_VARIABLE_END_TAG.length());
		
		//For each advice line, populate the advice format with the real variables:
		String sAdviceLinesSection = "";
		BigDecimal bdGrossAmt = new BigDecimal("0.00");
		BigDecimal bdNetAmt = new BigDecimal("0.00");
		
		int iNumberOfAdviceLines = check.getLineArray().size();
		int iNumberOfAdviceLinesPrinted = 0;
		for (int i = 0; i < iNumberOfAdviceLines; i++){
			APCheckLine checkline = check.getLineArray().get(i);
			bdGrossAmt = new BigDecimal(checkline.getsgrossamount().replace(",", ""));
			bdNetAmt = new BigDecimal(checkline.getsnetpaid().replace(",", ""));
			
			//Now drop off the beginning and ending tag on each line:
			String sReplacedAdviceFormatLine = sAdviceFormatString.replace(ADVICE_VARIABLE_START_TAG, "").replace(ADVICE_VARIABLE_END_TAG, "");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DOC_NUMBER, checkline.getsapplytodocnumber());
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DOC_DATE, checkline.getsapplytodocdate());
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_GROSS_AMOUNT, clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrossAmt));
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DEDUCTIONS, clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrossAmt.subtract(bdNetAmt)));
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_NET_PAID, clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetAmt));

			//Add each advice line to the accumulated section text:
			sAdviceLinesSection += sReplacedAdviceFormatLine;
			iNumberOfAdviceLinesPrinted++;
		}
		
		//Add blank lines for the remaining number of lines that have to be printed:
		for (int i = 0; i < (iMaxNumberOfAdviceLinesToPrintPerPage - iNumberOfAdviceLinesPrinted); i++){
			String sReplacedAdviceFormatLine = sAdviceFormatString.replace(ADVICE_VARIABLE_START_TAG, "").replace(ADVICE_VARIABLE_END_TAG, "");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DOC_NUMBER, "&nbsp;");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DOC_DATE, "&nbsp;");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_GROSS_AMOUNT, "&nbsp;");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_DEDUCTIONS, "&nbsp;");
			sReplacedAdviceFormatLine = sReplacedAdviceFormatLine.replace(ADVICE_VARIABLE_NET_PAID, "&nbsp;");
			
			//Add each advice line to the accumulated 
			sAdviceLinesSection += sReplacedAdviceFormatLine;
		}
		
		//Finally, replace the original advice format line with the processed advice lines section:
		s = sCheckFormatString.replace(sAdviceFormatString, sAdviceLinesSection);
		
		//Now replace any advice totals in the check format:
		if (check.getsilastpage().compareToIgnoreCase("1") == 0){
			//Now we have to show the TOTAL amount for the whole payment entry.
			//If this is a multi-page check, we'll have to get the totals from all the check pages
			//which correspond to this batch entry (payment) - so we can't just add up the lines
			//on this page alone:
			String SQL = "SELECT"
				+ " SUM(" + SMTableapchecklines.bdnetpaid + ") AS NETPAID"
				+ ", SUM(" + SMTableapchecklines.bdgrossamount + ") AS GROSSAMT"
				+ " FROM " + SMTableapchecklines.TableName
				+ " WHERE ("
					+ "(" + SMTableapchecklines.lbatchnumber + " = " + check.getsbatchnumber() + ")"
					+ " AND (" + SMTableapchecklines.lentrynumber + " = " + check.getsentrynumber() + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					//The sums COULD be null, if we are printing an 'alignment', for example, so catch that:
					String sGrossAmt = "0.00";
					if (rs.getBigDecimal("GROSSAMT") != null){
						sGrossAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("GROSSAMT"));
					}
					String sDeductions = "0.00";
					if (
						(rs.getBigDecimal("GROSSAMT") != null) && (rs.getBigDecimal("NETPAID") != null) 
					){
						sDeductions = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rs.getBigDecimal("GROSSAMT").subtract(rs.getBigDecimal("NETPAID"))
						);
					}
					String sNetPaid = "0.00";
					if (rs.getBigDecimal("NETPAID") != null){
						sNetPaid = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("NETPAID"));
					}
					s = s.replace(ADVICE_VARIABLE_GROSS_AMOUNT_TOTAL, sGrossAmt);
					s = s.replace(ADVICE_VARIABLE_DEDUCTIONS_TOTAL, sDeductions);
					s = s.replace(ADVICE_VARIABLE_NET_PAID_TOTAL, sNetPaid);
					rs.close();
				}else{
					rs.close();
					throw new Exception("Error [1508942815] getting advice totals with SQL '" + SQL + "' - no records found");
				}
			} catch (Exception e) {
				throw new Exception("Error [1508942816] getting advice totals with SQL '" + SQL + "' - " + e.getMessage());
			}

		}else{
			s = s.replace(ADVICE_VARIABLE_GROSS_AMOUNT_TOTAL, "");
			s = s.replace(ADVICE_VARIABLE_DEDUCTIONS_TOTAL, "");
			s = s.replace(ADVICE_VARIABLE_NET_PAID_TOTAL, "");
		}
		return s;
	}
	public static final String getPageBreak(){
		//return"<P CLASS=\"breakhere\"></P>\n";
		return "\n\n<P style=\"page-break-before: always;\"></P>\n\n";
		//style="page-break-before: always"
	}
	public ArrayList<String> getVariableNames(){
		return arrVariableNames;
	}
	public ArrayList<String> getVariableDescriptions(){
		return arrVariableDescriptions;
	}
	private String getAmountInWrittenText(String sAmount) throws Exception{
		return clsNumberToEnglishWords.convertCurrencyFromDecimalFormattedString(sAmount);
	}
	private void initializeVariables(){

		arrVariableNames.add("<U>CHECK VARIABLES</U>");
		arrVariableDescriptions.add("&nbsp;");
		
		arrVariableNames.add(CHECK_VARIABLE_CHECK_DATE);
		arrVariableDescriptions.add("The check date.");
		arrVariableNames.add(CHECK_VARIABLE_CHECK_AMOUNT);
		arrVariableDescriptions.add("The total amount of the check.");
		arrVariableNames.add(CHECK_VARIABLE_CHECK_NUMBER);
		arrVariableDescriptions.add("The check number.");
		arrVariableNames.add(CHECK_VARIABLE_CHECK_REMIT_TO_NAME);
		arrVariableDescriptions.add("The remit to name (vendor name) that will appear on the check.");
		arrVariableNames.add(CHECK_VARIABLE_CHECK_AMOUNT_IN_TEXT);
		arrVariableDescriptions.add("The check amount, written out in English text.");
		
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ACCOUNT_NUMBER);
		arrVariableDescriptions.add("Vendor's account number.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_1);
		arrVariableDescriptions.add("First line of vendor's remit-to address.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_2);
		arrVariableDescriptions.add("Second line of vendor's remit-to address.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_3);
		arrVariableDescriptions.add("Third line of vendor's remit-to address.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_4);
		arrVariableDescriptions.add("Fourth line of vendor's remit-to address.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_CITY);
		arrVariableDescriptions.add("Vendor's remit-to city.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_STATE);
		arrVariableDescriptions.add("Vendor's remit-to state.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_POSTAL_CODE);
		arrVariableDescriptions.add("Vendor's remit-to postal code.");
		arrVariableNames.add(CHECK_VARIABLE_VENDOR_ADDRESS_COUNTRY);
		arrVariableDescriptions.add("Vendor's remit-to country.");
		
		arrVariableNames.add(CHECK_VARIABLE_BANK_NAME);
		arrVariableDescriptions.add("Bank name.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_ADDRESS_LINE_1);
		arrVariableDescriptions.add("First line of the bank's address.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_ADDRESS_LINE_2);
		arrVariableDescriptions.add("Second line of the bank's address.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_ADDRESS_LINE_3);
		arrVariableDescriptions.add("Third line of the bank's address.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_ADDRESS_LINE_4);
		arrVariableDescriptions.add("Fourth line of the bank's address.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_CITY);
		arrVariableDescriptions.add("Bank's city.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_STATE);
		arrVariableDescriptions.add("Bank's state.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_COUNTRY);
		arrVariableDescriptions.add("Bank's country.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_POSTAL_CODE);
		arrVariableDescriptions.add("Bank's postal code.");
		arrVariableNames.add(CHECK_VARIABLE_BANK_ROUTING_NUMBER);
		arrVariableDescriptions.add("Bank's routing number.");
		
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_NAME);
		arrVariableDescriptions.add("Company name.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_1);
		arrVariableDescriptions.add("First line of company address.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_2);
		arrVariableDescriptions.add("Second line of company address.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_3);
		arrVariableDescriptions.add("Third line of company address.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_ADDRESS_LINE_4);
		arrVariableDescriptions.add("Fourth line of company address.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_CITY);
		arrVariableDescriptions.add("Company city.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_STATE);
		arrVariableDescriptions.add("Company state.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_COUNTRY);
		arrVariableDescriptions.add("Company country.");
		arrVariableNames.add(CHECK_VARIABLE_COMPANY_POSTAL_CODE);
		arrVariableDescriptions.add("Company postal code.");
		
		
		//Advice fields:
		arrVariableNames.add("&nbsp;");
		arrVariableDescriptions.add("&nbsp;");
		
		arrVariableNames.add("<U>ADVICE VARIABLES</U>");
		arrVariableDescriptions.add("&nbsp;");
		
		arrVariableNames.add(ADVICE_VARIABLE_START_TAG);
		arrVariableDescriptions.add("Use this to indicate the BEGINNING of the advice section.");
		
		arrVariableNames.add(ADVICE_VARIABLE_END_TAG);
		arrVariableDescriptions.add("Use this to indicate the END of the advice section.");
		
		arrVariableNames.add(ADVICE_VARIABLE_DOC_NUMBER);
		arrVariableDescriptions.add("The document number (e.g. invoice number) for each invoice which this check is paying.");
		
		arrVariableNames.add(ADVICE_VARIABLE_DOC_DATE);
		arrVariableDescriptions.add("The document date for each invoice which this check is paying.");

		arrVariableNames.add(ADVICE_VARIABLE_GROSS_AMOUNT);
		arrVariableDescriptions.add("The gross (before discount) amount of each invoice which this check is paying.");
		
		arrVariableNames.add(ADVICE_VARIABLE_DEDUCTIONS);
		arrVariableDescriptions.add("The 'deduction' (discount taken) amount of each invoice which this check is paying.");

		arrVariableNames.add(ADVICE_VARIABLE_NET_PAID);
		arrVariableDescriptions.add("The net amount paid (gross less any discounts taken) on each invoice.");
		
		arrVariableNames.add(ADVICE_VARIABLE_GROSS_AMOUNT_TOTAL);
		arrVariableDescriptions.add("The total of the gross amounts for all the invoices paid with this check.");
		
		arrVariableNames.add(ADVICE_VARIABLE_DEDUCTIONS_TOTAL);
		arrVariableDescriptions.add("The total of the deductions for all the invoices paid with this check.");
		
		arrVariableNames.add(ADVICE_VARIABLE_NET_PAID_TOTAL);
		arrVariableDescriptions.add("The total of the net paid amounts for all the invoices paid with this check.");

	}
	
	public String printCheckRun(APBatch batch, Connection conn, String sListOfEntryIDsToPrint, String sCheckFormID, String sUserID, String sUserFullName) throws Exception{
		String s = "";
		
		//System.out.println("[1543332094] - going into createCheckRecordsForCheckRun");
		createCheckRecordsForCheckRun(batch, conn, sListOfEntryIDsToPrint, sCheckFormID, sUserID, sUserFullName);
		
		//Now print the checks:
		//System.out.println("[1543332095] - going into printCheckRunListToScreen");
		s += printCheckRunListToScreen(conn, sCheckFormID, sListOfEntryIDsToPrint, sUserID, sUserFullName);
		//System.out.println("[1543332096] - after printCheckRunListToScreen");
		return s;
	}

	private String printCheckRunListToScreen(Connection conn, String sCheckFormID, String sListOfEntriesInCheckRun, String sUserID, String sUserFullName) throws Exception{
		String s = "";
		
		String sListOfEntryIDsInRun[] = sListOfEntriesInCheckRun.split(",");
		String sCheckRunLogInfo = "";
		String SQL = "SELECT"
			+ " * FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				+ " (" + SMTableapchecks.ivoid + " = 0)"
				+ " AND (" + SMTableapchecks.iprinted + " = 0)"
				+ " AND ("
			;
			for (int i = 0; i < sListOfEntryIDsInRun.length; i++){
				if (i == 0){
					SQL += "(" + SMTableapchecks.lbatchentryid + "=" + sListOfEntryIDsInRun[i] + ")";
				}else{
					SQL += " OR (" + SMTableapchecks.lbatchentryid + "=" + sListOfEntryIDsInRun[i] + ")";
				}
			}
		
			SQL += ")"
			+ ")"
			+ " ORDER BY CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED)"
		;
			
		//System.out.println("[1543332105] - SQL = '" + SQL + "'");
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		boolean bIsFirstCheck = true;
		while (rs.next()){
			APCheck check = new APCheck();
			check.load(conn, sUserID, Long.toString(rs.getLong(SMTableapchecks.lid)));
			
			if (!bIsFirstCheck){
				s += getPageBreak();
			}
			
			BKBank bank = new BKBank();
			bank.setslid(check.getslbankid());
			try {
				bank.load(conn);
			} catch (Exception e1) {
				throw new Exception("Error [1506106218] loading bank with ID '" + check.getslbankid() + "' - " + e1.getMessage());
			}
			
			try {
				s += printIndividualCheck(
					conn,
					check,
					sUserID
				);
			} catch (Exception e1) {
				throw new Exception("Error [1506106219] printing individual check for check number '" + check.getschecknumber() + "' - " + e1.getMessage());
			}
			
			//Now save the 'printed' status:
			try {
				APCheck.updateCheckPrintedStatus(conn, check.getschecknumber(), check.getschecknumber(), "1", sUserID, sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [1505420103] updating check's printed status - " + e.getMessage());
			}
			
			bIsFirstCheck = false;
			
			//Add a line to the logging info for this check run:
			if (sCheckRunLogInfo.compareToIgnoreCase("") != 0){
				sCheckRunLogInfo += "\n";
			}
			sCheckRunLogInfo += 
				"Check #: " + rs.getString(SMTableapchecks.schecknumber)
				+ ", page number: " + rs.getInt(SMTableapchecks.ipagenumber)
				+ ", check date: " + clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableapchecks.datcheckdate))
				+ ", amt: " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapchecks.bdamount))
				+ ", batch #: " + Long.toString(rs.getLong(SMTableapchecks.lbatchnumber))
				+ ", entry #: " + Long.toString(rs.getLong(SMTableapchecks.lentrynumber))
				+ ", using check form ID: " + Long.toString(rs.getLong(SMTableapchecks.lcheckformid))
				+ ", bank ID: " + Long.toString(rs.getLong(SMTableapchecks.lbankid))
				+ ", remit to name: " + rs.getString(SMTableapchecks.sremittoname)
				+ ", vendor #: " + rs.getString(SMTableapchecks.svendoracct)
				+ ", check created at: " + clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableapchecks.dattimecreated))
				+ ", check created at: " + clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTableapchecks.dattimecreated), 
					SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
					SMUtilities.EMPTY_DATETIME_VALUE)
					+ " by " + rs.getString(SMTableapchecks.screatedbyfullname)
			;
		}
		rs.close();

		//System.out.println("[1543332106]");
		
		//Record the check run:
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_APCHECKRUNPRINTED, 
			"Check run printed", 
			sCheckRunLogInfo, 
			"[1510859674]"
		);
		
		return s;
	}

	private void createCheckRecordsForCheckRun(APBatch batch, Connection conn, String sListOfEntryIDsInCheckRun, String sCheckFormID, String sUserID, String sUserFullName) throws Exception{
		
		//This function will create all of the check records for the entries listed in the check run, if they don't exist already:
		
		//First we need to know how many lines can be printed on a check page, so we can create multiple check pages
		//if there are too many lines on a check:
		String SQL = "SELECT"
			+ " * FROM " + SMTableapcheckforms.TableName
			+ " WHERE ("
				+ "(" + SMTableapcheckforms.lid + " = " + sCheckFormID + ")"
			+ ")"
		;
		int iMaxNumberOfAdviceLinesToPrintPerPage = 0;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				iMaxNumberOfAdviceLinesToPrintPerPage = rs.getInt(SMTableapcheckforms.inumberofadvicelinesperpage);
			}else{
				throw new Exception("Error [1504810060] - could not load check form with ID '" + sCheckFormID + "'.");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1504810061] reading check form text with SQL '" + SQL + "' - " + e.getMessage());
		}

		//Start a data transaction, so that all the check inserts either HAPPEN or NOT:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1504886659] - could not start data transaction.");
		}
		
		//System.out.println("[1543332097]");
		String sEntryIDList[] = sListOfEntryIDsInCheckRun.split(",");
		for (int i = 0; i < sEntryIDList.length; i++){
			//System.out.println("[1543341576] - in for loop, i = " + i);
			APBatchEntry entry = new APBatchEntry();
			entry.setslid(sEntryIDList[i]);
			try {
				entry.load(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1510850916] creating check records - " + e.getMessage());
			}
			//IF there is NO check for this entry already, then we'll create a new check:
			SQL = "SELECT"
				+ " " + SMTableapchecks.lid
				+ " FROM " + SMTableapchecks.TableName
				+ " WHERE ("
					+ "(" + SMTableapchecks.lbatchnumber + " = " + entry.getsbatchnumber() + ")"
					+ " AND (" + SMTableapchecks.lentrynumber + " = " + entry.getsentrynumber() + ")"
					+ " AND (" + SMTableapchecks.ivoid + " = 0)"
				+ ")"
			;
			boolean bUnvoidedCheckWasFound = false;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					bUnvoidedCheckWasFound = true;
				}
				rs.close();
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1509739133] testing for unvoided checks - " + e.getMessage());
			}
			
			//If an UNVOIDED check was NOT found, then we need to create a check for this entry:
			if (!bUnvoidedCheckWasFound){
				//Create checks for this entry:
				int iNumberOfPagesToPrint = (entry.getLineArray().size() / iMaxNumberOfAdviceLinesToPrintPerPage) + 1;
				if ((entry.getLineArray().size() % iMaxNumberOfAdviceLinesToPrintPerPage) == 0){
					iNumberOfPagesToPrint = iNumberOfPagesToPrint - 1;
				}
				
				int iNumberOfAdviceLines = 55;
				int iMaxAdviceLinesForForm = 27;
				int iNumberOfPages = (iNumberOfAdviceLines / iMaxAdviceLinesForForm) + 1;
				if ((iNumberOfAdviceLines % iMaxAdviceLinesForForm) == 0){
					iNumberOfPages = iNumberOfPages - 1;
				}
				
				for (int iPageNumber = 1; iPageNumber <= iNumberOfPagesToPrint; iPageNumber++){
					//Create a check for this page:
					String sIsLastPage = "0";
					if (iPageNumber == iNumberOfPagesToPrint){
						sIsLastPage = "1";
					}
					try {
						//System.out.println("[1543341575] - going to insertCheckRecord iNumberOfPagesToPrint = " + iNumberOfPagesToPrint + ", iPageNumber = " + iPageNumber);
						insertCheckRecord(batch, entry, iMaxNumberOfAdviceLinesToPrintPerPage, iPageNumber, sIsLastPage, sCheckFormID, conn, sUserID, sUserFullName);
					} catch (Exception e) {
						clsDatabaseFunctions.rollback_data_transaction(conn);
						throw new Exception("Error [1543331973] inserting check records - " + e.getMessage() + ".");
					}
				}
			}
		}

		//System.out.println("[1543332098]");
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1504886659] committing data transaction.");
		}
		
	}
	private void insertCheckRecord(
		APBatch batch,
		APBatchEntry batchentry, 
		int iMaxNumberOfAdviceLinesToPrintPerPage,
		int iPageNumber,
		String sIsLastPage,
		String sCheckFormID,
		Connection conn, 
		String sUserID,
		String sUserFullName
		) throws Exception{
		
		//long lStarttime = System.currentTimeMillis();
		//System.out.println("[1543341840] - start time = " + lStarttime + ", entry " + batchentry.getsentrynumber() + ", iPageNumber = " + iPageNumber);
		
		APCheck check = new APCheck();
		BKBank bank = new BKBank();
		bank.setslid(batchentry.getslbankid());
		try {
			bank.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1504810637] loading bank information - " + e.getMessage());
		}
		
		//try {
		//	bank.updatenextchecknumber(conn, sUser);
		//} catch (Exception e) {
		//	throw new Exception("Error [1505157033] - " + e.getMessage());
		//}
		
		//System.out.println("[1543332099]");
		
		//System.out.println("[1543341842] - elapsed time 2 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		check.setschecknumber(bank.getsnextchecknumber());
		check.setsbatchnumber(batchentry.getsbatchnumber());
		check.setslbatchentryid(batchentry.getslid());
		if (sIsLastPage.compareToIgnoreCase("1") == 0){
			check.setscheckamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(new BigDecimal(batchentry.getsentryamount().replace(",", "")).negate()));
		}else{
			check.setscheckamount("0.00");
		}
		check.setscreatedbyid(sUserID);
		check.setsdatcheckdate(batchentry.getsdatdocdate());
		check.setsentrynumber(batchentry.getsentrynumber());
		check.setsicheckformid(sCheckFormID);
		check.setsilastpage(sIsLastPage);
		check.setsipagenumber(Integer.toString(iPageNumber));
		check.setsiprinted("0");
		check.setsivoid("0");
		check.setslbankid(batchentry.getslbankid());
		check.setsltransactionid("0");
		check.setsvendoracct(batchentry.getsvendoracct());
		check.setsvendorname(batchentry.getsvendorname());
		check.setsremittoname(batchentry.getsremittoname());
		check.setsremittoaddressline1(batchentry.getsremittoaddressline1());
		check.setsremittoaddressline2(batchentry.getsremittoaddressline2());
		check.setsremittoaddressline3(batchentry.getsremittoaddressline3());
		check.setsremittoaddressline4(batchentry.getsremittoaddressline4());
		check.setsremittocity(batchentry.getsremittocity());
		check.setsremittostate(batchentry.getsremittostate());
		check.setsremittocountry(batchentry.getsremittocountry());
		check.setsremittopostalcode(batchentry.getsremittopostalcode());
		
		//System.out.println("[1543332100]");
		//System.out.println("[1543341843] - elapsed time 3 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//Create the check lines here:
		for (int iCheckLineCounter = ((iPageNumber - 1) * iMaxNumberOfAdviceLinesToPrintPerPage) + 1;
				
			iCheckLineCounter <= ((iPageNumber) * iMaxNumberOfAdviceLinesToPrintPerPage);
			iCheckLineCounter++){
			
			APCheckLine checkline = new APCheckLine();
			
			//If we've hit the end of the batch entry lines, then just drop out:
			if (iCheckLineCounter > batchentry.getLineArray().size()){
				break;
			}
			
			//Otherwise, populate the check line from the batch entry line:
			APBatchEntryLine batchline = batchentry.getLineArray().get(iCheckLineCounter - 1); //The array is zero-based, but the check line number is 1 based
			checkline.setsapplytodocnumber(batchline.getsapplytodocnumber());
			checkline.setsbatchnumber(batchline.getsbatchnumber());
			
			//Each check, even if it's the 2nd or 3rd 'page' of a multi-page check, starts with check line number 1.
			//So although it might be the 50th advice line on the payment, if it's the first line on this page of a multi-page
			//check, then it's check line number 1, not check line number 50:
			int iCheckLineNumber = iCheckLineCounter - ((iPageNumber - 1) * iMaxNumberOfAdviceLinesToPrintPerPage);
			checkline.setschecklinenumber(Integer.toString(iCheckLineNumber));
			
			//If this is a pre-pay for example, there may not be any 'apply to doc ID':
			if ((batchline.getslapplytodocid().compareToIgnoreCase("0") == 0) || (batchline.getslapplytodocid().compareToIgnoreCase("-1") == 0)){
				checkline.setsdatapplytodocdate(clsServletUtilities.EMPTY_DATE_VALUE);
				BigDecimal bdAmount = new BigDecimal(batchline.getsbdamount().replace(",", ""));
				checkline.setsgrossamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount.negate()));
			}else{
				//Get information from the apply-to doc:
				String SQL = "SELECT"
					+ " " + SMTableaptransactions.datdocdate
					+ ", " + SMTableaptransactions.bdcurrentamt
					+ " FROM " + SMTableaptransactions.TableName
					+ " WHERE ("
						+ "(" + SMTableaptransactions.lid + " = " + batchline.getslapplytodocid() + ")"
					+ ")"
				;
				ResultSet rsApplyToTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsApplyToTransaction.next()){
					checkline.setsdatapplytodocdate(
						clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rsApplyToTransaction.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE)
					);
					checkline.setsgrossamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsApplyToTransaction.getBigDecimal(SMTableaptransactions.bdcurrentamt)));
								//new BigDecimal(getGrossAmount(batchline).replace(",", "")).negate() - these amounts are NEGATIVE on the apbatchentryline record
					rsApplyToTransaction.close();
				}else{
					rsApplyToTransaction.close();
					throw new Exception("Error [1511145472] could not find apply-to transaction with ID '" + batchline.getslapplytodocid()
					+ "' for entry " + batchline.getsentrynumber() + ", line " + batchline.getslinenumber() + "."
					);
				}
			}
			
			checkline.setsdiscounttaken(
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					new BigDecimal(batchline.getsbddiscountappliedamt().replace(",", "")).negate()
					)
			);
			checkline.setsentrylinenumber(batchline.getslinenumber());
			checkline.setsentrynumber(batchline.getsentrynumber());
			
			checkline.setsnetpaid(
					clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						new BigDecimal(batchline.getsbdamount().replace(",", "")).negate()
						)
				);
			checkline.setslcheckid(check.getslid());  //This will be 0 until the check is first saved, and we'll update it at that time
			check.addLine(checkline);
			
			//System.out.println("[1543341844] - elapsed time 4 = " + (System.currentTimeMillis() - lStarttime) / 1000 + " iMaxNumberOfAdviceLinesToPrintPerPage = " + iMaxNumberOfAdviceLinesToPrintPerPage);
		}
		
		//System.out.println("[1543332101]");
		//System.out.println("[1543341845] - elapsed time 5 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		try {
			check.save_without_data_transaction(conn, sUserID, sUserFullName, batch);
		} catch (Exception e) {
			throw new Exception("Error [1504886455] saving check - " + e.getMessage());
		}
		
		//System.out.println("[1543332102]");
		
		//Record the check number in the batch entry IF this is the last page of the check.
		//If it's NOT the last page of the check, then it's a ZERO amount check, and we don't need to save it
		//as the batch entry's 'check number':
		
		//System.out.println("[1543341846] - elapsed time 6 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		//System.out.println("[1543341946] - check.getsilastpage() = '" + check.getsilastpage() + "'.");
		if (check.getsilastpage().compareToIgnoreCase("1") == 0){
			try {
				batchentry.setschecknumber(check.getschecknumber());
			} catch (Exception e1) {
				throw new Exception("Error [1508717362] updating batch entry with check number - " + e1.getMessage());
			}
		}
		
		//System.out.println("[1543341847] - elapsed time 7 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//System.out.println("[1543332103]");
		
		//Now update the next check number again, because we just used the previous 'next check number':
		try {
			bank.incrementnextchecknumber(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1508443552] - " + e.getMessage());
		}
		//System.out.println("[1543332104]");
		
		//System.out.println("[1543341841] - elapsed time = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		return;
	}
	public static String displayAlignmentCheckForm(
			String sCheckFormID,
			int iNumberOfChecksToPrint,
			String sSampleVendorCode,
			String sSampleRemitToCode,
			String sSampleBankID,
			int iNumberOfAdviceLines,
			String sDBID,
			String sUserID,
			String sCalledClassName,
			Connection conn) throws Exception{

		String sOutPut = "";
		
		sOutPut += "<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>" + "\n";
		sOutPut += "<INPUT TYPE=HIDDEN NAME='" + SMTableapcheckforms.lid + "' VALUE='" + sCheckFormID + "'>" + "\n";

		//Get the MAX number of advice lines that can be printed with this form:
		int iMaxAdviceLinesForForm = 0;
		String SQL = "SELECT"
			+ " " + SMTableapcheckforms.inumberofadvicelinesperpage
			+ " FROM " + SMTableapcheckforms.TableName
			+ " WHERE ("
				+ "(" + SMTableapcheckforms.lid + " = " + sCheckFormID + ")"
			+ ")"
		;
		try {
			ResultSet rsCheckForms = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCheckForms.next()){
				iMaxAdviceLinesForForm = rsCheckForms.getInt(SMTableapcheckforms.inumberofadvicelinesperpage);
				rsCheckForms.close();
			}else{
				rsCheckForms.close();
				throw new Exception("Error [1511210875] - no check form found with ID '" + sCheckFormID + "'.");
			}
		} catch (Exception e1) {
			throw new Exception("Error [1511210876] - error reading check forms with SQL '" + SQL + "'. - " + e1.getMessage());
		}
		
		int iNumberOfPagesPerCheck = (iNumberOfAdviceLines / iMaxAdviceLinesForForm) + 1;
		if ((iNumberOfAdviceLines % iMaxAdviceLinesForForm) == 0){
			iNumberOfPagesPerCheck = iNumberOfPagesPerCheck - 1;
		}
		
		//Create an array to hole the checks:
		ArrayList<APCheck> arrCheckList = new ArrayList<APCheck>(0);
		int iNumberOfAdviceLinesCreatedForThisCheck = 0;
		//Create dummy checks:
		for (int iPageNumber = 1; iPageNumber <= iNumberOfPagesPerCheck; iPageNumber++){
			APCheck check = new APCheck();
			check.setsbatchnumber("0");
			check.setschecknumber("0000000001");
			check.setscreatedbyid(sUserID);
			check.setsdatcheckdate(SMUtilities.EMPTY_DATE_VALUE);
			check.setsentrynumber("0");
			check.setsicheckformid(sCheckFormID);
			check.setsiprinted("0");
			check.setsivoid("0");
			check.setslbankid(sSampleBankID);
			check.setslbatchentryid("0");
			check.setslid("0");
			check.setsltransactionid("0");
			check.setsvendoracct(sSampleVendorCode);
			check.setscheckamount("0.00");
			
			String siLastPage = "0";
			if (iPageNumber == iNumberOfPagesPerCheck){
				siLastPage = "1";
			}
			check.setsilastpage(siLastPage);
			check.setsipagenumber(Integer.toString(iPageNumber));
			
			if (sSampleRemitToCode.compareToIgnoreCase("") != 0){
				SQL = "SELECT * FROM " + SMTableapvendorremittolocations.TableName
					+ " WHERE ("
						+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + sSampleVendorCode + "')"
						+ " AND (" + SMTableapvendorremittolocations.sremittocode + " = '" + sSampleRemitToCode + "')"
					+ ")"
				;
			}else{
				SQL = "SELECT * FROM " + SMTableicvendors.TableName
					+ " WHERE ("
						+ "(" + SMTableicvendors.svendoracct + " = '" + sSampleVendorCode + "')"
					+ ")"
				;
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (sSampleRemitToCode.compareToIgnoreCase("") != 0){
					check.setsvendorname(rs.getString(SMTableapvendorremittolocations.sremittoname).trim());
					check.setsremittoname(rs.getString(SMTableapvendorremittolocations.sremittoname).trim());
					check.setsremittoaddressline1(rs.getString(SMTableapvendorremittolocations.saddressline1).trim());
					check.setsremittoaddressline2(rs.getString(SMTableapvendorremittolocations.saddressline2).trim());
					check.setsremittoaddressline3(rs.getString(SMTableapvendorremittolocations.saddressline3).trim());
					check.setsremittoaddressline4(rs.getString(SMTableapvendorremittolocations.saddressline4).trim());
					check.setsremittocity(rs.getString(SMTableapvendorremittolocations.scity).trim());
					check.setsremittostate(rs.getString(SMTableapvendorremittolocations.sstate).trim());
					check.setsremittocountry(rs.getString(SMTableapvendorremittolocations.scountry).trim());
					check.setsremittopostalcode(rs.getString(SMTableapvendorremittolocations.spostalcode).trim());
				}else{
					check.setsvendorname(rs.getString(SMTableicvendors.sname).trim());
					check.setsremittoname(rs.getString(SMTableicvendors.sname).trim());
					check.setsremittoaddressline1(rs.getString(SMTableicvendors.saddressline1).trim());
					check.setsremittoaddressline2(rs.getString(SMTableicvendors.saddressline2).trim());
					check.setsremittoaddressline3(rs.getString(SMTableicvendors.saddressline3).trim());
					check.setsremittoaddressline4(rs.getString(SMTableicvendors.saddressline4).trim());
					check.setsremittocity(rs.getString(SMTableicvendors.scity).trim());
					check.setsremittostate(rs.getString(SMTableicvendors.sstate).trim()); 
					check.setsremittocountry(rs.getString(SMTableicvendors.scountry).trim());
					check.setsremittopostalcode(rs.getString(SMTableicvendors.spostalcode).trim());
				}
				rs.close();
			}else{
				rs.close();
				if (sSampleRemitToCode.compareToIgnoreCase("") != 0){
					throw new Exception("Error [1503673834] - no remit to location address found for vendor '" + sSampleVendorCode + "', remit to location code '" + sSampleRemitToCode + "'.");
				}else{
					throw new Exception("Error [1503673835] - no record found for vendor '" + sSampleVendorCode + "'.");
				}
			}
			
			//Create dummy lines for the check:
			int iNumberOfLinesOnThisPage = 0;
			while (iNumberOfAdviceLinesCreatedForThisCheck < iNumberOfAdviceLines){
				//If we're at the bottom of the page, then kick out and create another check:
				if (iNumberOfLinesOnThisPage >= iMaxAdviceLinesForForm){
					break;
				}
				APCheckLine adviceline = new APCheckLine();
				adviceline.setsapplytodocnumber("INV0000" + Integer.toString(iNumberOfAdviceLinesCreatedForThisCheck + 1));
				adviceline.setsbatchnumber(check.getsbatchnumber());
				adviceline.setschecklinenumber(Integer.toString(iNumberOfAdviceLinesCreatedForThisCheck + 1));
				adviceline.setsdatapplytodocdate(SMUtilities.EMPTY_DATE_VALUE);
				adviceline.setsdiscounttaken("0.00");
				adviceline.setsentrylinenumber("0");
				adviceline.setsentrynumber("0");
				adviceline.setsgrossamount("0.00");
				adviceline.setslcheckid(check.getslid());
				adviceline.setslid("0");
				adviceline.setsnetpaid("0.00");
				check.addLine(adviceline);
				iNumberOfAdviceLinesCreatedForThisCheck++;
				iNumberOfLinesOnThisPage++;
			}
			
			//Finally, add each page into the array:
			arrCheckList.add(check);
		}
		
		//Display the check form text, which consists of various HTML fields:
		APCheckFormProcessor processor = new APCheckFormProcessor();
		for (int iCheckCounter = 1; iCheckCounter <= iNumberOfChecksToPrint; iCheckCounter++){
			for (int iCheckPage = 1; iCheckPage <= arrCheckList.size(); iCheckPage++){
				try {
					sOutPut += processor.printIndividualCheck(conn, arrCheckList.get(iCheckPage - 1), sUserID);
				} catch (Exception e) {
					throw new Exception("Error [1511298436] printing check for check number '" + arrCheckList.get(iCheckPage - 1).getschecknumber() + "' - " + e.getMessage());
				}
				//We'll need to append a page break unless this is the very last page of all the checks:
				if (
					(iCheckCounter == iNumberOfChecksToPrint)
					&& (iCheckPage == arrCheckList.size())
				){
					//Very last page of the run - don't add a page break
				}else{
					//It's NOT the last page of the run, so add a page break:
					sOutPut += APCheckFormProcessor.getPageBreak();
				}
			}
		}
		return sOutPut;
	}
}
