package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTableglfiscalsets;
import SMDataDefinition.SMTableglstatementforms;
import ServletUtilities.clsDatabaseFunctions;

public class GLStatementFormProcessor {

	private final static String VARIABLE_STARTING_DELIMITER = "[[";
	private final static String VARIABLE_ENDING_DELIMITER = "]]";
	
	private final static String STATEMENT_VARIABLE_UNFORMATTED_ACCT_NUMBER = VARIABLE_STARTING_DELIMITER + "GLSTMTUNFORMATTEDACCT" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_FORMATTED_ACCT_NUMBER = VARIABLE_STARTING_DELIMITER + "GLSTMTFORMATTEDACCT" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_ACCT_DESC = VARIABLE_STARTING_DELIMITER + "GLSTMTACCTDESC" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_ACCT_CURRENTBALANCE = VARIABLE_STARTING_DELIMITER + "GLSTMTACCTCURRENTBALANCE" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_ACCT_PREVIOUSYEARBALANCE = VARIABLE_STARTING_DELIMITER + "GLSTMTACCTPREVIOUSYEARBALANCE" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_ACCT_PREVIOUSMONTHBALANCE = VARIABLE_STARTING_DELIMITER + "GLSTMTACCTPREVIOUSMONTHBALANCE" + VARIABLE_ENDING_DELIMITER;

	private final static String STATEMENT_VARIABLE_COMPANY_NAME = VARIABLE_STARTING_DELIMITER + "GLSTMTCOMPANYNAME" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_1 = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPADD1" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_2 = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPADD2" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_3 = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPADD3" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_4 = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPADD4" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_CITY = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPCITY" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_STATE = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPSTATE" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_COUNTRY = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPCOUNTRY" + VARIABLE_ENDING_DELIMITER;
	private final static String STATEMENT_VARIABLE_COMPANY_POSTAL_CODE = VARIABLE_STARTING_DELIMITER + "GLSTMTCKCOMPZIP" + VARIABLE_ENDING_DELIMITER;
	
	private ArrayList<String>arrVariableNames = new ArrayList<String>(0);
	private ArrayList<String>arrVariableDescriptions = new ArrayList<String>(0);

	public GLStatementFormProcessor()	{
		initializeVariables();
	}

	public static String printStatement(
			Connection conn,
			String sGLStatementFormID,
			boolean bIsSample,
			String sFiscalYear,
			String sFiscalPeriod
		) throws Exception{

		String s = "";
		String SQL = "SELECT"
			+ " * FROM " + SMTableglstatementforms.TableName
			+  " WHERE ("
				+ "(" + SMTableglstatementforms.lid + " = " + sGLStatementFormID + ")"
			+ ")"
		;
		ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				s = rs.getString(SMTableglstatementforms.mtext);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1536083062] - could not read GL statement form with ID '" + sGLStatementFormID + "'.");
			}
		} catch (Exception e1) {
			throw new Exception("Error [1536083063] - could not read GL statement forms with SQL '" + SQL + "' - " + e1.getMessage());
		}
		
		//Get the account info:
		String sCurrentPeriodSQL = "SELECT * FROM " + SMTableglfiscalsets.TableName
			+ " WHERE ("
				+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
			+ " ORDER BY " + SMTableglfiscalsets.sAcctID
		;
		ResultSet rsCurrentFiscalData = null;
		try {
			rsCurrentFiscalData = clsDatabaseFunctions.openResultSet(sCurrentPeriodSQL, conn);
			while (rs.next()){
				//Display the account info here:
				
			
			}
		} catch (Exception e1) {
			throw new Exception("Error [1536083063] - could not read GL statement forms with SQL '" + SQL + "' - " + e1.getMessage());
		}
		rsCurrentFiscalData.close();
		
		/*
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
		
		s = s.replace(CHECK_VARIABLE_CHECK_AMOUNT, sCheckAmt);
		s = s.replace(CHECK_VARIABLE_CHECK_NUMBER, check.getschecknumber());
		s = s.replace(CHECK_VARIABLE_CHECK_REMIT_TO_NAME, check.getsremittoname());
		s = s.replace(CHECK_VARIABLE_VENDOR_ACCOUNT_NUMBER, check.getsvendoracct());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_1, check.getsremittoaddressline1());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_2, check.getsremittoaddressline2());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_3, check.getsremittoaddressline3());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_LINE_4, check.getsremittoaddressline4());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_CITY, check.getsremittocity());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_STATE, check.getsremittostate());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_POSTAL_CODE, check.getsremittopostalcode());
		s = s.replace(CHECK_VARIABLE_VENDOR_ADDRESS_COUNTRY, check.getsremittocountry());
*/			
		//Get the company profile info:
		SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName
		;
		rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			s = s.replace(STATEMENT_VARIABLE_COMPANY_NAME, rs.getString(SMTablecompanyprofile.sCompanyName));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_1, rs.getString(SMTablecompanyprofile.sAddress01));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_2, rs.getString(SMTablecompanyprofile.sAddress02));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_3, rs.getString(SMTablecompanyprofile.sAddress03));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_4, rs.getString(SMTablecompanyprofile.sAddress04));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_CITY, rs.getString(SMTablecompanyprofile.sCity));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_STATE, rs.getString(SMTablecompanyprofile.sState));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_COUNTRY, rs.getString(SMTablecompanyprofile.sCountry));
			s = s.replace(STATEMENT_VARIABLE_COMPANY_POSTAL_CODE, rs.getString(SMTablecompanyprofile.sZipCode));
			rs.close();
		}else{
			rs.close();
			throw new Exception("Error [1536083064] - No company profile information found.");
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

	private void initializeVariables(){

		arrVariableNames.add("<U>GL STATEMENT VARIABLES</U>");
		arrVariableDescriptions.add("&nbsp;");
		
		arrVariableNames.add(STATEMENT_VARIABLE_UNFORMATTED_ACCT_NUMBER);
		arrVariableDescriptions.add("UNformatted account number.");
		arrVariableNames.add(STATEMENT_VARIABLE_FORMATTED_ACCT_NUMBER);
		arrVariableDescriptions.add("Formatted account number.");
		arrVariableNames.add(STATEMENT_VARIABLE_ACCT_DESC);
		arrVariableDescriptions.add("Account description.");
		arrVariableNames.add(STATEMENT_VARIABLE_ACCT_CURRENTBALANCE);
		arrVariableDescriptions.add("Account current balance.");
		arrVariableNames.add(STATEMENT_VARIABLE_ACCT_PREVIOUSYEARBALANCE);
		arrVariableDescriptions.add("Account balance for the same period in the previous year.");
		arrVariableNames.add(STATEMENT_VARIABLE_ACCT_PREVIOUSMONTHBALANCE);
		arrVariableDescriptions.add("Account balance for the previous month.");
		
		
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_NAME);
		arrVariableDescriptions.add("Company name.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_1);
		arrVariableDescriptions.add("First line of company address.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_2);
		arrVariableDescriptions.add("Second line of company address.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_3);
		arrVariableDescriptions.add("Third line of company address.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_ADDRESS_LINE_4);
		arrVariableDescriptions.add("Fourth line of company address.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_CITY);
		arrVariableDescriptions.add("Company city.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_STATE);
		arrVariableDescriptions.add("Company state.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_COUNTRY);
		arrVariableDescriptions.add("Company country.");
		arrVariableNames.add(STATEMENT_VARIABLE_COMPANY_POSTAL_CODE);
		arrVariableDescriptions.add("Company postal code.");
	}
	
}
