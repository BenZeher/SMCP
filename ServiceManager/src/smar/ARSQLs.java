package smar;

import SMClasses.*;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ARSQLs extends SMClasses.MySQLs {

	private static String SQL = "";
	
	//Account sets:
	public static String Get_AcctSets_List_SQL(){
		SQL = "SELECT *" 
		+ " FROM " + SMTablearacctset.TableName
		+ " ORDER BY " + SMTablearacctset.sAcctSetCode;
		//System.out.println("Get_Acctsets_List_SQL = " + SQL);
		return SQL;
	}
	public static String Get_PriceListCodes_List_SQL(){
		SQL = "SELECT *" 
		+ " FROM " + SMTablepricelistcodes.TableName
		+ " ORDER BY " + SMTablepricelistcodes.spricelistcode;
		//System.out.println("Get_PriceListCodes_List_SQL = " + SQL);
		return SQL;
	}
	public static String Get_AcctSet_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablearacctset.TableName + 
		" WHERE (" + 
			"(" + SMTablearacctset.sAcctSetCode + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_AcctSet_By_Code = " + SQL);
		return SQL;
	}
	public static String Get_PriceListCode_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablepricelistcodes.TableName + 
		" WHERE (" + 
			"(" + SMTablepricelistcodes.spricelistcode + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_PriceListCode_By_Code = " + SQL);
		return SQL;
	}
	public static String Get_AcctSet_By_GLAcct(String sGLAcct){
		SQL = "SELECT * FROM " + SMTablearacctset.TableName + 
		" WHERE (" + 
			"(" + SMTablearacctset.sAcctsReceivableControlAcct + " = '" + sGLAcct + "')" +
			" OR (" + SMTablearacctset.sCashAcct + " = '" + sGLAcct + "')" +
			" OR (" + SMTablearacctset.sPrepaymentLiabilityAcct + " = '" + sGLAcct + "')" +
			" OR (" + SMTablearacctset.sReceiptDiscountsAcct + " = '" + sGLAcct + "')" +
			" OR (" + SMTablearacctset.sRetainageAcct + " = '" + sGLAcct + "')" +
			" OR (" + SMTablearacctset.sWriteOffAcct + " = '" + sGLAcct + "')" +
		")";
		//System.out.println ("Get_AcctSet_By_GLAcct = " + SQL);
		return SQL;
	}
	
	public static String Delete_AcctSet_SQL(String sCode){
		SQL = "DELETE FROM " +
		SMTablearacctset.TableName +
		" WHERE (" + 
			"(" + SMTablearacctset.sAcctSetCode + " = '" + sCode + "')" +
		")";
		
		//System.out.println ("Delete_AcctSet_SQL = " + SQL);
		return SQL;
	}
	public static String Delete_PriceListCode_SQL(String sCode){
		SQL = "DELETE FROM " +
		SMTablepricelistcodes.TableName +
		" WHERE (" + 
			"(" + SMTablepricelistcodes.spricelistcode + " = '" + sCode + "')" +
		")";
		
		//System.out.println ("Delete_PriceListCode_SQL = " + SQL);
		return SQL;
	}
	
	public static String Insert_AcctSet_SQL(			
		String sCode,
		String iActive,
		String sAcctsReceivableControlAcct,
		String sDescription,
		String sPrepaymentLiabilityAcct,
		String sReceiptDiscountsAcct,
		String sRetainageAcct,
		String sWriteOffAcct,
		String sCashAcct){
		
		SQL = "INSERT into " + SMTablearacctset.TableName
		+ " (" 
			+ SMTablearacctset.sAcctSetCode
			+ ", " + SMTablearacctset.datLastMaintained
			+ ", " + SMTablearacctset.iActive
			+ ", " + SMTablearacctset.sAcctsReceivableControlAcct
			+ ", " + SMTablearacctset.sDescription
			+ ", " + SMTablearacctset.sPrepaymentLiabilityAcct
			+ ", " + SMTablearacctset.sReceiptDiscountsAcct
			+ ", " + SMTablearacctset.sRetainageAcct
			+ ", " + SMTablearacctset.sWriteOffAcct
			+ ", " + SMTablearacctset.sCashAcct
		+ ")"
		+ " VALUES ("
			+ "'" + sCode + "'"
			+ ", NOW()"
			+ ", " + iActive
			+ ", '" + sAcctsReceivableControlAcct + "'"
			+ ", '" + sDescription + "'"
			+ ", '" + sPrepaymentLiabilityAcct + "'"
			+ ", '" + sReceiptDiscountsAcct + "'"
			+ ", '" + sRetainageAcct + "'"
			+ ", '" + sWriteOffAcct + "'"
			+ ", '" + sCashAcct + "'"
		+ ")"
		;
		//System.out.println ("Insert_AcctSet_SQL = " + SQL);
		return SQL;
	}
	public static String Insert_PriceCode_SQL(			
			String sCode,
			String sDescription
			){
			
			SQL = "INSERT into " + SMTablepricelistcodes.TableName
			+ " (" 
				+ SMTablepricelistcodes.spricelistcode
				+ ", " + SMTablepricelistcodes.sdescription
			+ ")"
			+ " VALUES ("
				+ "'" + sCode + "'"
				+ ", '" + sDescription + "'"
			+ ")"
			;
			//System.out.println ("Insert_PriceCode_SQL = " + SQL);
			return SQL;
		}
	public static String Update_AcctSet_SQL(
			String sCode,
			String iActive,
			String sAcctsReceivableControlAcct,
			String sDescription,
			String sPrepaymentLiabilityAcct,
			String sReceiptDiscountsAcct,
			String sRetainageAcct,
			String sWriteOffAcct,
			String sCashAcct
			  ){
			
			SQL = "UPDATE " + SMTablearacctset.TableName
			+ " SET " 
			+ SMTablearacctset.datLastMaintained + " = NOW(), "
			+ SMTablearacctset.iActive + " = " + iActive + ", "
			+ SMTablearacctset.sAcctsReceivableControlAcct + " = '" + sAcctsReceivableControlAcct + "', "
			+ SMTablearacctset.sDescription + " = '" + sDescription + "', "
			+ SMTablearacctset.sPrepaymentLiabilityAcct + " = '" + sPrepaymentLiabilityAcct + "', "
			+ SMTablearacctset.sReceiptDiscountsAcct + " = '" + sReceiptDiscountsAcct + "', "
			+ SMTablearacctset.sRetainageAcct + " = '" + sRetainageAcct + "', "
			+ SMTablearacctset.sWriteOffAcct + " = '" + sWriteOffAcct + "', "
			+ SMTablearacctset.sCashAcct + " = '" + sCashAcct + "'"
			
			+ " WHERE (" 
				+ "(" + SMTablearacctset.sAcctSetCode + " = '" + sCode + "')"
				+ ")";

			//System.out.println ("Update_Tax_SQL = " + SQL);
			return SQL;
		}
	public static String Update_PriceListCode_SQL(
			String sCode,
			String sDescription
			  ){
			
			SQL = "UPDATE " + SMTablepricelistcodes.TableName
			+ " SET " 
			+ SMTablepricelistcodes.sdescription + " = '" + sDescription + "'"
			
			+ " WHERE (" 
				+ "(" + SMTablepricelistcodes.spricelistcode + " = '" + sCode + "')"
				+ ")";

			//System.out.println ("Update_PriceListCode_SQL = " + SQL);
			return SQL;
		}
	//Terms:
	public static String Get_Terms_List_SQL(){
		SQL = "SELECT " 
		+ SMTablearterms.sTermsCode + ", "
		+ SMTablearterms.sDescription
		+ " FROM " + SMTablearterms.TableName
		+ " ORDER BY " + SMTablearterms.sTermsCode;
		//System.out.println("Get_Terms_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Terms_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablearterms.TableName + 
		" WHERE (" + 
			"(" + SMTablearterms.sTermsCode + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_Terms_By_Code = " + SQL);
		return SQL;
	}
	
	public static String Delete_Terms_SQL(String sCode){
		SQL = "DELETE FROM " +
		SMTablearterms.TableName+
		" WHERE (" + 
			"(" + SMTablearterms.sTermsCode + " = '" + sCode + "')" +
		")";
		
		//System.out.println ("Delete_Terms_SQL = " + SQL);
		return SQL;
	}
	
	public static String Insert_Terms_SQL(
			String sCode,
			String iActive,
			String dDiscountPercent,
			String sDescription,
			String iDiscountDayOfTheMonth,
			String iDiscountNumberOfDays,
			String iDueDayOfTheMonth,
			String iDueNumberOfDays
			){
			
			SQL = "INSERT INTO " + SMTablearterms.TableName
			+ " (" 
			+ SMTablearterms.sTermsCode
			+ ", " + SMTablearterms.iActive
			+ ", " + SMTablearterms.dDiscountPercent
			+ ", " + SMTablearterms.sDescription
			+ ", " + SMTablearterms.iDiscountDayOfTheMonth
			+ ", " + SMTablearterms.iDiscountNumberOfDays
			+ ", " + SMTablearterms.iDueDayOfTheMonth
			+ ", " + SMTablearterms.iDueNumberOfDays
			+ ", " + SMTablearterms.datLastMaintained
			
			+ ") VALUES (" 
				+ "'" + sCode + "'"
				+ ", " + iActive
				+ ", " + dDiscountPercent
				+ ", '" + sDescription + "'"
				+ ", " + iDiscountDayOfTheMonth
				+ ", " + iDiscountNumberOfDays
				+ ", " + iDueDayOfTheMonth
				+ ", " + iDueNumberOfDays
				+ ", NOW()"
			+ ")"
			;

			//System.out.println ("Insert_Terms_SQL = " + SQL);
			return SQL;
		}

	
	public static String Update_Terms_SQL(
		String sCode,
		String iActive,
		String dDiscountPercent,
		String sDescription,
		String iDiscountDayOfTheMonth,
		String iDiscountNumberOfDays,
		String iDueDayOfTheMonth,
		String iDueNumberOfDays
		){
		
		SQL = "UPDATE " + SMTablearterms.TableName
		+ " SET " 
		+ SMTablearterms.datLastMaintained + " = NOW(), "
		+ SMTablearterms.iActive + " = " + iActive + ", "
		+ SMTablearterms.sDescription + " = '" + sDescription + "', "
		+ SMTablearterms.dDiscountPercent + " = " + dDiscountPercent + ", "
		+ SMTablearterms.iDiscountDayOfTheMonth + " = " + iDiscountDayOfTheMonth + ", "
		+ SMTablearterms.iDiscountNumberOfDays + " = " + iDiscountNumberOfDays + ", "
		+ SMTablearterms.iDueDayOfTheMonth + " = " + iDueDayOfTheMonth + ", "
		+ SMTablearterms.iDueNumberOfDays + " = " + iDueNumberOfDays
		
		+ " WHERE (" 
			+ "(" + SMTablearterms.sTermsCode + " = '" + sCode + "')"
			+ ")";

		//System.out.println ("Update_Tax_SQL = " + SQL);
		return SQL;
	}

	//Customers
	public static String Get_Customer_List_SQL(){
		SQL = "SELECT " 
		+ SMTablearcustomer.sCustomerNumber + ", "
		+ SMTablearcustomer.sCustomerName
		+ " FROM " + SMTablearcustomer.TableName
		+ " ORDER BY " + SMTablearcustomer.sCustomerNumber;
		//System.out.println("Get_Customer_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Customer_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_Customer_By_Code = " + SQL);
		return SQL;
	}
	public static String Get_Customers_By_AccountSet(String sAcctSet){
		SQL = "SELECT"
			+ " " + SMTablearcustomer.sCustomerNumber
			+ " FROM " + SMTablearcustomer.TableName 
			+ " WHERE (" 
				+ "(" + SMTablearcustomer.sAccountSet + " = '" + sAcctSet + "')"
			+ ")"
			;
		//System.out.println ("Get_Customers_By_AccountSet = " + SQL);
		return SQL;
	}
	public static String Get_Customers_By_PriceListCode(String sCode){
		SQL = "SELECT"
			+ " " + SMTablearcustomer.sCustomerNumber
			+ " FROM " + SMTablearcustomer.TableName 
			+ " WHERE (" 
				+ "(" + SMTablearcustomer.sPriceListCode + " = '" + sCode + "')"
			+ ")"
			;
		//System.out.println ("Get_Customers_By_PriceListCode = " + SQL);
		return SQL;
	}
	
	//Get_CustomerCashAcct_By_Code
	public static String Get_CustomerCashAcct_By_Code(String sCode){
		SQL = "SELECT "
		+ SMTablearacctset.sCashAcct
		+ " FROM " + SMTablearcustomer.TableName
		+ ", " + SMTablearacctset.TableName
		+ " WHERE (" 
			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "')"
			+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
		+ ")";
		//System.out.println ("Get_CustomerCashAcct_By_Code = " + SQL);
		return SQL;
	}
	
	//Get_CustomerARControlAcct_By_Code
	public static String Get_CustomerARControlAcct_By_Code(String sCode){
		SQL = "SELECT "
		+ SMTablearacctset.sAcctsReceivableControlAcct
		+ " FROM " + SMTablearcustomer.TableName
		+ ", " + SMTablearacctset.TableName
		+ " WHERE (" 
			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "')"
			+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
		+ ")";
		//System.out.println ("Get_CustomerARControlAcct_By_Code = " + SQL);
		return SQL;
	}
	public static String Delete_Customer_SQL(String sCode){
		SQL = "DELETE FROM " +
		SMTablearcustomer.TableName +
		" WHERE (" + 
			"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "')" +
		")";
		
		//System.out.println ("Delete_Customer_SQL = " + SQL);
		return SQL;
	}
	public static String Delete_CustomerStatistics_SQL(String sCode){
		SQL = "DELETE FROM "
		+ SMTablearcustomerstatistics.TableName
		+ " WHERE (" 
			+ "(" + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sCode + "')"
		+ ")"
		;
		
		//System.out.println ("Delete_CustomerStatistics_SQL = " + SQL);
		return SQL;
	}
	public static String Delete_CustomerMonthlyStatistics_SQL(String sCode){
		SQL = "DELETE FROM "
		+ SMTablearmonthlystatistics.TableName
		+ " WHERE (" 
			+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCode + "')"
		+ ")"
		;
		
		//System.out.println ("Delete_CustomerMonthlyStatistics_SQL = " + SQL);
		return SQL;
	}
	public static String Delete_CustomerShipTos_For_Customer_SQL(String sCode){
		SQL = "DELETE FROM "
		+ SMTablearcustomershiptos.TableName
		+ " WHERE (" 
			+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCode + "')"
		+ ")"
		;
		
		//System.out.println ("Delete_CustomerShipTo_SQL = " + SQL);
		return SQL;
	}
	
	public static String Insert_Customer_SQL(
		String sCode,
		String sStartDate,
		String sCreditLimit,
		String sActive,
		String sOnHold,
		String sCustomerComments,
		String sAccountingNotes,
		String sAccountSet,
		String sAddressLine1,
		String sAddressLine2,
		String sAddressLine3,
		String sAddressLine4,
		String sCity,
		String sContactName,
		String sCountry,
		String sCustomerName,
		String sFaxNumber,
		String sLastEditUserFullName,
		String sLastEditUserID,
		String sPhoneNumber,
		String sPostalCode,
		String sState,
		String sTerms,
		String sCustomerGroup,
		String sEmailAddress,
		String sWebAddress,
		String sPriceListCode,
		String sPriceLevel,
		//String sTaxJurisdiction,
		//String sTaxType,
		String sUsesElectronicDeposit,
		String sRequiresStatements,
		String sRequiresPO,
		String sgdoclink,
		String sTaxID,
		String sInvoicingContact,
		String sInvoicingEmail,
		String sInvoicingNotes
		){
			
		SQL = "INSERT into " + SMTablearcustomer.TableName
		+ " (" 
			+ SMTablearcustomer.datLastMaintained
			+ ", " + SMTablearcustomer.sCustomerNumber
			+ ", " + SMTablearcustomer.datStartDate
			+ ", " + SMTablearcustomer.dCreditLimit
			+ ", " + SMTablearcustomer.iActive
			+ ", " + SMTablearcustomer.iOnHold
			+ ", " + SMTablearcustomer.mCustomerComments
			+ ", " + SMTablearcustomer.mAccountingNotes
			+ ", " + SMTablearcustomer.sAccountSet
			+ ", " + SMTablearcustomer.sAddressLine1
			+ ", " + SMTablearcustomer.sAddressLine2
			+ ", " + SMTablearcustomer.sAddressLine3
			+ ", " + SMTablearcustomer.sAddressLine4
			+ ", " + SMTablearcustomer.sCity
			+ ", " + SMTablearcustomer.sContactName
			+ ", " + SMTablearcustomer.sCountry
			+ ", " + SMTablearcustomer.sCustomerName
			+ ", " + SMTablearcustomer.sFaxNumber
			+ ", " + SMTablearcustomer.sLastEditUserFullName
			+ ", " + SMTablearcustomer.lLastEditUserID
			+ ", " + SMTablearcustomer.sPhoneNumber
			+ ", " + SMTablearcustomer.sPostalCode
			+ ", " + SMTablearcustomer.sState
			+ ", " + SMTablearcustomer.sTerms
			+ ", " + SMTablearcustomer.sCustomerGroup
			+ ", " + SMTablearcustomer.sEmailAddress
			+ ", " + SMTablearcustomer.sWebAddress
			+ ", " + SMTablearcustomer.sPriceListCode
			+ ", " + SMTablearcustomer.ipricelevel
			+ ", " + SMTablearcustomer.iuseselectronicdeposit
			+ ", " + SMTablearcustomer.irequirespo
			+ ", " + SMTablearcustomer.irequiresstatements
			+ ", " + SMTablearcustomer.sgdoclink
			+ ", " + SMTablearcustomer.itaxid
			+ ", " + SMTablearcustomer.sinvoicingcontact
			+ ", " + SMTablearcustomer.sinvoicingemail
			+ ", " + SMTablearcustomer.sinvoicingnotes
		+")"
		+ " VALUES (" 
			+ "NOW()"
			+ ", '" + sCode + "'"
			+ ", '" + sStartDate + "'"
			+ ", " + sCreditLimit
			+ ", " + sActive
			+ ", " + sOnHold
			+ ", '" + sCustomerComments + "'"
			+ ", '" + sAccountingNotes + "'"
			+ ", '" + sAccountSet + "'"
			+ ", '" + sAddressLine1 + "'"
			+ ", '" + sAddressLine2 + "'"
			+ ", '" + sAddressLine3 + "'"
			+ ", '" + sAddressLine4 + "'"
			+ ", '" + sCity + "'"
			+ ", '" + sContactName + "'"
			+ ", '" + sCountry + "'"
			+ ", '" + sCustomerName + "'"
			+ ", '" + sFaxNumber + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sLastEditUserFullName) + "'"
			+ ", " + sLastEditUserID + ""
			+ ", '" + sPhoneNumber + "'"
			+ ", '" + sPostalCode + "'"
			+ ", '" + sState + "'"
			+ ", '" + sTerms + "'"
			+ ", '" + sCustomerGroup + "'"
			+ ", '" + sEmailAddress + "'"
			+ ", '" + sWebAddress + "'"
			+ ", '" + sPriceListCode + "'"
			+ ", " + sPriceLevel
			//+ ", '" + sTaxJurisdiction + "'"
			//+ ", " + sTaxType
			+ ", " + sUsesElectronicDeposit
			+ ", " + sRequiresPO
			+ ", " + sRequiresStatements
			+ ", '" + sgdoclink + "'"
			+ ", " + sTaxID
			+ ", '" + sInvoicingContact + "'"
			+ ", '" + sInvoicingEmail + "'"
			+ ", '" + sInvoicingNotes + "'"
		+ ")"
		;
		//System.out.println ("Insert_Customer_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_Customer_SQL(
		String sCode,
		String sStartDate,
		String sCreditLimit,
		String sActive,
		String sOnHold,
		String sCustomerComments,
		String sAccountingNotes,
		String sAccountSet,
		String sAddressLine1,
		String sAddressLine2,
		String sAddressLine3,
		String sAddressLine4,
		String sCity,
		String sContactName,
		String sCountry,
		String sCustomerName,
		String sFaxNumber,
		String sLastEditUserFullName,
		String sLastEditUserID,
		String sPhoneNumber,
		String sPostalCode,
		String sState,
		String sTerms,
		String sCustomerGroup,
		String sEmailAddress,
		String sWebAddress,
		String sPriceListCode,
		String sPriceLevel,
		//String sTaxJurisdiction,
		//String sTaxType,
		String sUsesElectronicDeposit,
		String sRequiresStatements,
		String sRequiresPO,
		String sgdoclink,
		String sTaxID,
		String sInvoicingContact,
		String sInvoicingEmail,
		String sInvoicingNotes
		){
		
		SQL = "UPDATE " + SMTablearcustomer.TableName
		+ " SET " 
		+ SMTablearcustomer.datLastMaintained + " = NOW(), "
		+ SMTablearcustomer.datStartDate + " = '" + sStartDate + "', "
		+ SMTablearcustomer.dCreditLimit + " = " + sCreditLimit + ", "
		+ SMTablearcustomer.iActive + " = " + sActive + ", "
		+ SMTablearcustomer.iOnHold + " = " + sOnHold + ", "
		+ SMTablearcustomer.mCustomerComments + " = '" + sCustomerComments + "', "
		+ SMTablearcustomer.mAccountingNotes + " = '" + sAccountingNotes + "', "
		+ SMTablearcustomer.sAccountSet + " = '" + sAccountSet + "', "
		+ SMTablearcustomer.sAddressLine1 + " = '" + sAddressLine1 + "', "
		+ SMTablearcustomer.sAddressLine2 + " = '" + sAddressLine2 + "', "
		+ SMTablearcustomer.sAddressLine3 + " = '" + sAddressLine3 + "', "
		+ SMTablearcustomer.sAddressLine4 + " = '" + sAddressLine4 + "', "
		+ SMTablearcustomer.sCity + " = '" + sCity + "', "
		+ SMTablearcustomer.sContactName + " = '" + sContactName + "', "
		+ SMTablearcustomer.sCountry + " = '" + sCountry + "', "
		+ SMTablearcustomer.sCustomerName + " = '" + sCustomerName + "', "
		+ SMTablearcustomer.sFaxNumber + " = '" + sFaxNumber + "', "
		+ SMTablearcustomer.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sLastEditUserFullName) + "', "
		+ SMTablearcustomer.lLastEditUserID + " = " + sLastEditUserID + ", "
		+ SMTablearcustomer.sPhoneNumber + " = '" + sPhoneNumber + "', "
		+ SMTablearcustomer.sPostalCode + " = '" + sPostalCode + "', "
		+ SMTablearcustomer.sState + " = '" + sState + "', "
		+ SMTablearcustomer.sTerms + " = '" + sTerms + "', "
		+ SMTablearcustomer.sCustomerGroup + " = '" + sCustomerGroup + "', "
		+ SMTablearcustomer.sEmailAddress + " = '" + sEmailAddress + "', "
		+ SMTablearcustomer.sWebAddress + " = '" + sWebAddress + "', "
		+ SMTablearcustomer.sPriceListCode + " = '" + sPriceListCode + "', "
		+ SMTablearcustomer.ipricelevel + " = " + sPriceLevel + ", "
		+ SMTablearcustomer.iuseselectronicdeposit + " = " + sUsesElectronicDeposit + ", "
		+ SMTablearcustomer.irequirespo + " = " + sRequiresPO + ", "
		+ SMTablearcustomer.irequiresstatements + " = " + sRequiresStatements + ", "
		+ SMTablearcustomer.sgdoclink + " = '" + sgdoclink + "',"
		+ SMTablearcustomer.itaxid + " = " + sTaxID + ","
		+ SMTablearcustomer.sinvoicingcontact + " = '" + sInvoicingContact + "',"
		+ SMTablearcustomer.sinvoicingemail + " = '" + sInvoicingEmail + "',"
		+ SMTablearcustomer.sinvoicingnotes + " = '" + sInvoicingNotes + "'" 
		+ " WHERE (" 
			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sCode + "')"
			+ ")";

		//System.out.println ("Update_Customer_SQL = " + SQL);
		return SQL;
	}
	public static String Get_CustomerStatistics_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablearcustomerstatistics.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_CustomerStatistics_By_Code = " + SQL);
		return SQL;
	}
	public static String Update_CustomerStatistics_By_Code(
			String sCode,
			String sCurrentBalance,
			String sAmountOfHighestInvoice,
			String sAmountOfHighestInvoiceLastYear,
			String sAmountOfLastCredit,
			String sAmountOfLastInvoice,
			String sAmountOfLastPayment,
			String sDateOfLastCredit,
			String sDateOfLastInvoice,
			String sDateOfLastPayment,
			String sHighestBalance,
			String sHighestBalanceLastYear,
			String sNumberOfOpenInvoices,
			String sTotalNumberOfPaidInvoices,
			String sTotalNumberOfDaysToPay
			){
		SQL = "UPDATE " + SMTablearcustomerstatistics.TableName 
			+ " SET"
			+ " " + SMTablearcustomerstatistics.sCurrentBalance + " = " + sCurrentBalance
			+ ", " + SMTablearcustomerstatistics.sAmountOfHighestInvoice + " = " + sAmountOfHighestInvoice
			+ ", " + SMTablearcustomerstatistics.sAmountOfHighestInvoiceLastYear + " = " + sAmountOfHighestInvoiceLastYear
			+ ", " + SMTablearcustomerstatistics.sAmountOfLastCredit + " = " + sAmountOfLastCredit
			+ ", " + SMTablearcustomerstatistics.sAmountOfLastInvoice + " = " + sAmountOfLastInvoice
			+ ", " + SMTablearcustomerstatistics.sAmountOfLastPayment + " = " + sAmountOfLastPayment
			+ ", " + SMTablearcustomerstatistics.sDateOfLastCredit + " = '" + sDateOfLastCredit + "'"
			+ ", " + SMTablearcustomerstatistics.sDateOfLastInvoice + " = '" + sDateOfLastInvoice + "'"
			+ ", " + SMTablearcustomerstatistics.sDateOfLastPayment + " = '" + sDateOfLastPayment + "'"
			+ ", " + SMTablearcustomerstatistics.sHighestBalance + " = " + sHighestBalance
			+ ", " + SMTablearcustomerstatistics.sHighestBalanceLastYear + " = " + sHighestBalanceLastYear
			+ ", " + SMTablearcustomerstatistics.sNumberOfOpenInvoices + " = " + sNumberOfOpenInvoices
			+ ", " + SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices + " = " + sTotalNumberOfPaidInvoices
			+ ", " + SMTablearcustomerstatistics.sTotalDaysToPay + " = " + sTotalNumberOfDaysToPay
		
		+ " WHERE (" 
			+ "(" + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sCode + "')"
		+ ")";
		//System.out.println ("Update_CustomerStatistics_By_Code = " + SQL);
		return SQL;
	}
	//Insert_CustomerStatistics_By_Code
	public static String Insert_CustomerStatistics_By_Code(
			String sCode,
			String sCurrentBalance,
			String sAmountOfHighestInvoice,
			String sAmountOfHighestInvoiceLastYear,
			String sAmountOfLastCredit,
			String sAmountOfLastInvoice,
			String sAmountOfLastPayment,
			String sDateOfLastCredit,
			String sDateOfLastInvoice,
			String sDateOfLastPayment,
			String sHighestBalance,
			String sHighestBalanceLastYear,
			String sNumberOfOpenInvoices,
			String sTotalNumberOfPaidInvoices,
			String sTotalNumberOfDaysToPay
			){
		SQL = "INSERT INTO " + SMTablearcustomerstatistics.TableName 
			+ " ("
				+ SMTablearcustomerstatistics.sCurrentBalance
				+ ", " + SMTablearcustomerstatistics.sAmountOfHighestInvoice
				+ ", " + SMTablearcustomerstatistics.sAmountOfHighestInvoiceLastYear
				+ ", " + SMTablearcustomerstatistics.sAmountOfLastCredit
				+ ", " + SMTablearcustomerstatistics.sAmountOfLastInvoice
				+ ", " + SMTablearcustomerstatistics.sAmountOfLastPayment
				+ ", " + SMTablearcustomerstatistics.sCustomerNumber
				+ ", " + SMTablearcustomerstatistics.sDateOfLastCredit
				+ ", " + SMTablearcustomerstatistics.sDateOfLastInvoice
				+ ", " + SMTablearcustomerstatistics.sDateOfLastPayment
				+ ", " + SMTablearcustomerstatistics.sHighestBalance
				+ ", " + SMTablearcustomerstatistics.sHighestBalanceLastYear
				+ ", " + SMTablearcustomerstatistics.sNumberOfOpenInvoices
				+ ", " + SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices
				+ ", " + SMTablearcustomerstatistics.sTotalDaysToPay
			+ ") VALUES ("
				+ sCurrentBalance
				+ ", " + sAmountOfHighestInvoice
				+ ", " + sAmountOfHighestInvoiceLastYear
				+ ", " + sAmountOfLastCredit
				+ ", " + sAmountOfLastInvoice
				+ ", " + sAmountOfLastPayment
				+ ", '" + sCode + "'"
				+ ", '" + sDateOfLastCredit + "'"
				+ ", '" + sDateOfLastInvoice + "'"
				+ ", '" + sDateOfLastPayment + "'"
				+ ", " + sHighestBalance
				+ ", " + sHighestBalanceLastYear
				+ ", " + sNumberOfOpenInvoices
				+ ", " + sTotalNumberOfPaidInvoices
				+ ", " + sTotalNumberOfDaysToPay
			+ ")";
		//System.out.println ("Insert_CustomerStatistics_By_Code = " + SQL);
		return SQL;
	}
	public static String Get_Monthly_Statistics(String sCode, String sYear, String sMonth){
		SQL = "SELECT * FROM " + SMTablearmonthlystatistics.TableName 
			+ " WHERE (" 
				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCode + "')"
				+ " AND (" + SMTablearmonthlystatistics.sYear + " = " + sYear + ")"
				+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " + sMonth + ")"
			+ ")";
		//System.out.println ("Get_Monthly_Statistics = " + SQL);
		return SQL;
	}
	public static String Get_Monthly_Statistics_For_Customer(String sCode){
		SQL = "SELECT * FROM " + SMTablearmonthlystatistics.TableName 
			+ " WHERE (" 
				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCode + "')"
			+ ")"
			+ " ORDER BY " + SMTablearmonthlystatistics.sYear + " DESC" 
				+ ", " + SMTablearmonthlystatistics.sMonth + " DESC";
		//System.out.println ("Get_Monthly_Statistics_For_Customer = " + SQL);
		return SQL;
	}
	public static String Get_Open_Invoice_Count(String sCode){
		SQL = "SELECT COUNT(*) FROM " + SMTableartransactions.TableName 
			+ " WHERE (" 
				+ "(" + SMTableartransactions.spayeepayor + " = '" + sCode + "')"
				+ " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
				+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
			+ ")";
		//System.out.println ("Get_Open_Invoice_Count = " + SQL);
		return SQL;
	}
	public static String Get_Current_Balance(String sCode){
		SQL = "SELECT SUM(" + SMTableartransactions.dcurrentamt + ") FROM " + SMTableartransactions.TableName 
			+ " WHERE (" 
				+ "(" + SMTableartransactions.spayeepayor + " = '" + sCode + "')"
				
				//Don't pick up retainage here:
				+ " AND (" + SMTableartransactions.iretainage + " = 0)"
			+ ")";
		//System.out.println ("Get_Current_Balance = " + SQL);
		return SQL;
	}
	public static String Update_Monthly_Statistics(
			String sCode,
			String sYear,
			String sMonth,
			String sInvoiceTotal,
			String sCreditTotal,
			String sPaymentTotal,
			String sNumberOfInvoices,
			String sNumberOfCredits,
			String sNumberOfPayments,
			String sAverageNumberOfDaysToPay,
    		String sNumberOfPaidInvoices, 
    		String sTotalNumberOfDaysToPay
			){
		SQL = "UPDATE " + SMTablearmonthlystatistics.TableName 
			+ " SET"
			+ " " + SMTablearmonthlystatistics.sAverageDaysToPay + " = " + sAverageNumberOfDaysToPay
			+ ", " + SMTablearmonthlystatistics.sCreditTotal + " = " + sCreditTotal
			+ ", " + SMTablearmonthlystatistics.sInvoiceTotal + " = " + sInvoiceTotal
			+ ", " + SMTablearmonthlystatistics.sMonth + " = " + sMonth
			+ ", " + SMTablearmonthlystatistics.sNumberOfCredits + " = " + sNumberOfCredits
			+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices + " = " + sNumberOfInvoices
			+ ", " + SMTablearmonthlystatistics.sNumberOfPayments + " = " + sNumberOfPayments
			+ ", " + SMTablearmonthlystatistics.sPaymentTotal + " = " + sPaymentTotal
			+ ", " + SMTablearmonthlystatistics.sNumberOfPaidInvoices + " = " + sNumberOfPaidInvoices
			+ ", " + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay + " = " + sTotalNumberOfDaysToPay
			+ " WHERE (" 
				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCode + "')"
				+ " AND (" + SMTablearmonthlystatistics.sYear + " = " + sYear + ")"
				+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " + sMonth + ")"
		+ ")";
		//System.out.println ("Update_Monthly_Statistics = " + SQL);
		return SQL;
	}
	public static String Insert_Monthly_Statistics(
			String sCode,
			String sYear,
			String sMonth,
			String sInvoiceTotal,
			String sCreditTotal,
			String sPaymentTotal,
			String sNumberOfInvoices,
			String sNumberOfCredits,
			String sNumberOfPayments,
			String sAverageNumberOfDaysToPay,
    		String sNumberOfPaidInvoices, 
    		String sTotalNumberOfDaysToPay
			){
		SQL = "INSERT INTO " + SMTablearmonthlystatistics.TableName 
			
			+ " ("
			+ SMTablearmonthlystatistics.sCustomerNumber
			+ ", " + SMTablearmonthlystatistics.sYear
			+ ", " + SMTablearmonthlystatistics.sCreditTotal
			+ ", " + SMTablearmonthlystatistics.sInvoiceTotal
			+ ", " + SMTablearmonthlystatistics.sMonth
			+ ", " + SMTablearmonthlystatistics.sNumberOfCredits
			+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices
			+ ", " + SMTablearmonthlystatistics.sNumberOfPayments
			+ ", " + SMTablearmonthlystatistics.sPaymentTotal
			+ ", " + SMTablearmonthlystatistics.sAverageDaysToPay
			+ ", " + SMTablearmonthlystatistics.sNumberOfPaidInvoices
			+ ", " + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay
			+ ") VALUES ("
			
			+ "'" + sCode + "'"
			+ ", " + sYear
			+ ", " + sCreditTotal
			+ ", " + sInvoiceTotal
			+ ", " + sMonth
			+ ", " + sNumberOfCredits
			+ ", " + sNumberOfInvoices
			+ ", " + sNumberOfPayments
			+ ", " + sPaymentTotal
			+ ", " + sAverageNumberOfDaysToPay
			+ ", " + sNumberOfPaidInvoices
			+ ", " + sTotalNumberOfDaysToPay

		+ ")";
		//System.out.println ("Insert_Monthly_Statistics = " + SQL);
		return SQL;
	}
	//Customer Groups
	public static String Get_CustomerGroup_List_SQL(){
		SQL = "SELECT " 
		+ SMTablearcustomergroups.sGroupCode + ", "
		+ SMTablearcustomergroups.sDescription
		+ " FROM " + SMTablearcustomergroups.TableName
		+ " ORDER BY " + SMTablearcustomergroups.sGroupCode;
		//System.out.println("Get_CustomerGroup_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_CustomerGroup_By_Code(String sCode){
		SQL = "SELECT * FROM " + SMTablearcustomergroups.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomergroups.sGroupCode + " = '" + sCode + "')" +
		")";
		//System.out.println ("Get_CustomerGroup_By_Code = " + SQL);
		return SQL;
	}
	
	public static String Delete_CustomerGroup_SQL(String sCode){
		SQL = "DELETE FROM " +
		SMTablearcustomergroups.TableName +
		" WHERE (" + 
			"(" + SMTablearcustomergroups.sGroupCode + " = '" + sCode + "')" +
		")";
		
		//System.out.println ("Delete_CustomerGroup_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Customers_By_CustomerGroup(String sCustomerGroup){
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomer.sCustomerGroup + " = '" + sCustomerGroup + "')" +
		")";
		//System.out.println ("Get_Customers_By_CustomerGroup = " + SQL);
		return SQL;
	}
	public static String Get_Customers_By_Terms(String sTerms){
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomer.sTerms + " = '" + sTerms + "')" +
		")";
		//System.out.println ("Get_Customers_By_Terms = " + SQL);
		return SQL;
	}
	public static String Insert_CustomerGroup_SQL(
			String sCode,
			String iActive,
			String sDescription,
			String sLastEditUserFullName,
			String sLastEditUserID
	){
		SQL = "INSERT into " + SMTablearcustomergroups.TableName +
		" (" 
			+ SMTablearcustomergroups.sGroupCode
			+ "," + SMTablearcustomergroups.datLastMaintained
			+ "," + SMTablearcustomergroups.iActive
			+ "," + SMTablearcustomergroups.sDescription
			+ "," + SMTablearcustomergroups.sLastEditUserFullName
			+ "," + SMTablearcustomergroups.lLastEditUserID
		+ ")"
		+ " VALUES ("
			+ "'" + sCode + "'"
			+ ", NOW()"
			+ ", '" + iActive + "'"
			+ ", '" + sDescription + "'"
			+ ", '" + sLastEditUserFullName + "'"
			+ ", " + sLastEditUserID + ""
		+ ")"
		;
		System.out.println ("[1558360566] - Insert_CustomerGroup_SQL = " + SQL);
		return SQL;
	}
	
	public static String Update_CustomerGroup_SQL(
		String sCode,
		String sDescription,
		String sActive,
		String sLastEditUser,
		String sLastEditUserID
		){
		
		SQL = "UPDATE " + SMTablearcustomergroups.TableName
		+ " SET " 
		+ SMTablearcustomergroups.datLastMaintained + " = NOW(), "
		+ SMTablearcustomergroups.sDescription + " = '" + sDescription + "', "
		+ SMTablearcustomergroups.iActive + " = " + sActive + ", "
		+ SMTablearcustomergroups.sLastEditUserFullName + " = '" + sLastEditUser + "'" + ","
		+ SMTablearcustomergroups.lLastEditUserID + " = " + sLastEditUserID 
		
		+ " WHERE (" 
			+ "(" + SMTablearcustomergroups.sGroupCode + " = '" + sCode + "')"
			+ ")";

		//System.out.println ("Update_CustomerGroup_SQL = " + SQL);
		return SQL;
	}

	//Customer ship to's
	public static String Get_CustomerShipTo_List_SQL(){
		SQL = "SELECT " 
		+ SMTablearcustomershiptos.sCustomerNumber + ", "
		+ SMTablearcustomershiptos.sShipToCode + ", "
		+ SMTablearcustomershiptos.sDescription
		+ " FROM " + SMTablearcustomershiptos.TableName
		+ " ORDER BY " 
		+ SMTablearcustomershiptos.sCustomerNumber + ", "
		+ SMTablearcustomershiptos.sShipToCode;
		//System.out.println("Get_CustomerShipTo_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_CustomerShipTo_By_Code(String sCustomerNumber, String sShipToCode){
		SQL = "SELECT * FROM " + SMTablearcustomershiptos.TableName + 
		" WHERE (" + 
			"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerNumber + "')" +
			" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')" + 
		")";
		//System.out.println ("Get_CustomerShipTo_By_Code = " + SQL);
		return SQL;
	}
	public static String Delete_SiteLocations_For_ShipTo_SQL(String sCustomerNumber, String sShipToCode){
		SQL = "DELETE FROM " +
		SMTablesitelocations.TableName +
		" WHERE (" + 
			"(" + SMTablesitelocations.sAcct + " = '" + sCustomerNumber + "')" +
			" AND (" + SMTablesitelocations.sShipToCode + " = '" + sShipToCode + "')" + 
		")";
		
		//System.out.println ("Delete_SiteLocations_For_ShipTo_SQL = " + SQL);
		return SQL;
	}
	public static String Delete_CustomerShipTo_SQL(String sCustomerNumber, String sShipToCode){
		SQL = "DELETE FROM " +
				SMTablearcustomershiptos.TableName +
		" WHERE (" + 
			"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerNumber + "')" +
			" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')" + 
		")";
		
		//System.out.println ("Delete_CustomerShipTo_SQL = " + SQL);
		return SQL;
	}
	public static String Insert_CustomerShipTo_SQL(
    	String sCustomerNumber,
		String sShipToCode,
		String sDescription,
		String sAddressLine1,
		String sAddressLine2,
		String sAddressLine3,
		String sAddressLine4,
		String sCity,
		String sState,
		String sCountry,
		String sPostalCode,
		String sContactName,
		String sPhoneNumber,
		String sFaxNumber
			){
		SQL = "INSERT into " + SMTablearcustomershiptos.TableName
		+ " ("
			+ SMTablearcustomershiptos.sCustomerNumber
			+ ", " + SMTablearcustomershiptos.sShipToCode 
			+ ", " + SMTablearcustomershiptos.sDescription
			+ ", " + SMTablearcustomershiptos.sAddressLine1
			+ ", " + SMTablearcustomershiptos.sAddressLine2
			+ ", " + SMTablearcustomershiptos.sAddressLine3
			+ ", " + SMTablearcustomershiptos.sAddressLine4
			+ ", " + SMTablearcustomershiptos.sCity
			+ ", " + SMTablearcustomershiptos.sState
			+ ", " + SMTablearcustomershiptos.sCountry
			+ ", " + SMTablearcustomershiptos.sPostalCode
			+ ", " + SMTablearcustomershiptos.sContactName
			+ ", " + SMTablearcustomershiptos.sPhoneNumber
			+ ", " + SMTablearcustomershiptos.sFaxNumber
		+ ")"
		+ " VALUES ("
			+ "'" + sCustomerNumber + "'" 
			+ ", '" + sShipToCode + "'"
			+ ", '" + sDescription + "'"
			+ ", '" + sAddressLine1 + "'"
			+ ", '" + sAddressLine2 + "'"
			+ ", '" + sAddressLine3 + "'"
			+ ", '" + sAddressLine4 + "'"
			+ ", '" + sCity + "'"
			+ ", '" + sState + "'"
			+ ", '" + sCountry + "'"
			+ ", '" + sPostalCode + "'"
			+ ", '" + sContactName + "'"
			+ ", '" + sPhoneNumber + "'"
			+ ", '" + sFaxNumber + "'"
		+ ")"
		;
		return SQL;
	}

	public static String Update_CustomerShipTo_SQL(
		String sCustomerNumber,
		String sShipToCode,
		String sDescription,
		String sAddressLine1,
		String sAddressLine2,
		String sAddressLine3,
		String sAddressLine4,
		String sCity,
		String sContactName,
		String sCountry,
		String sFaxNumber,
		String sPhoneNumber,
		String sPostalCode,
		String sState
		){
		
		SQL = "UPDATE " + SMTablearcustomershiptos.TableName
		+ " SET " 
		+ SMTablearcustomershiptos.sAddressLine1 + " = '" + sAddressLine1 + "', "
		+ SMTablearcustomershiptos.sAddressLine2 + " = '" + sAddressLine2 + "', "
		+ SMTablearcustomershiptos.sAddressLine3 + " = '" + sAddressLine3 + "', "
		+ SMTablearcustomershiptos.sAddressLine4 + " = '" + sAddressLine4 + "', "
		+ SMTablearcustomershiptos.sCity + " = '" + sCity + "', "
		+ SMTablearcustomershiptos.sContactName + " = '" + sContactName + "', "
		+ SMTablearcustomershiptos.sCountry + " = '" + sCountry + "', "
		+ SMTablearcustomershiptos.sDescription + " = '" + sDescription + "', "
		+ SMTablearcustomershiptos.sFaxNumber + " = '" + sFaxNumber + "', "
		+ SMTablearcustomershiptos.sPhoneNumber + " = '" + sPhoneNumber + "', "
		+ SMTablearcustomershiptos.sPostalCode + " = '" + sPostalCode + "', "
		+ SMTablearcustomershiptos.sState + " = '" + sState + "' "
		
		+ " WHERE (" 
			+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerNumber + "')"
			+ " AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')"
		+ ")";

		//System.out.println ("Update_CustomerShipTo_SQL = " + SQL);
		return SQL;
	}
	
	//Customer transactions inquiry:
	public static String Get_Customer_Transactions_SQL(String sCustomerNumber, boolean bOpenTransactionsOnly){
		SQL = "SELECT *" 
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerNumber + "')";
			
			if (bOpenTransactionsOnly){
				SQL += " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
			}
		SQL += ")"
		+ " ORDER BY " 
		+ SMTableartransactions.datdocdate + ", "
		+ SMTableartransactions.sdocnumber;
		//System.out.println("Get_Customer_Transactions_SQL = " + SQL);
		return SQL;
	}

	public static String Get_Open_Transactions_By_GLAcctSQL(String sGLAcct){
		SQL = "SELECT " 
		+ SMTableartransactions.lid
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.scontrolacct + " = '" + sGLAcct + "')"
			+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
		+ ")"
		;
		//System.out.println("Get_Open_Transactions_By_GLAcctSQL = " + SQL);
		return SQL;
	}
	public static String Get_Open_Transactions_By_Terms(String sTerms){
		SQL = "SELECT " 
		+ SMTableartransactions.lid
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.sterms + " = '" + sTerms + "')"
			+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
		+ ")"
		;
		//System.out.println("Get_Open_Transactions_By_Terms = " + SQL);
		return SQL;
	}
	public static String Get_Open_Orders_For_Customer_SQL(String sCustomerNumber){
		SQL = "SELECT " 
		+ SMTableorderheaders.sOrderNumber
		+ " FROM " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
		+ " WHERE ("
			+ "(" + SMTableorderdetails.TableName + ".dUniqueOrderID = " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + ")"
			
			+ " AND (" + SMTableorderheaders.sCustomerCode + " = '" + sCustomerNumber + "')"
			
			+ " AND (" + SMTableorderdetails.TableName + ".dQtyOrdered != 0.00)"
			
			+ " AND ("
				+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
				+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
			+ ")"
			
		+ ")"
		;
		//System.out.println("Get_Open_Orders_For_Customer_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Open_Orders_For_AcctSet_SQL(String sAcctSet){
		SQL = "SELECT " 
		+ SMTableorderheaders.sOrderNumber
		+ " FROM " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
		+ " WHERE ("
			+ "(" + SMTableorderdetails.TableName + ".dUniqueOrderID = " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + ")"
			
			+ " AND (" + SMTableorderheaders.sCustomerControlAcctSet + " = '" + sAcctSet + "')"
			
			+ " AND (" + SMTableorderdetails.TableName + ".dQtyOrdered != 0.00)"
			
			+ " AND ("
				+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
				+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
			+ ")"
			
		+ ")"
		;
		//System.out.println("Get_Open_Orders_For_AcctSet_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Open_Orders_For_PriceListCode_SQL(String sPriceListCode){
		SQL = "SELECT " 
		+ SMTableorderheaders.sOrderNumber
		+ " FROM " + SMTableorderheaders.TableName
		+ " WHERE ("
			+ "(" + SMTableorderheaders.sDefaultPriceListCode + " = '" + sPriceListCode + "')"
			
			+ " AND ("
				+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
				+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
			+ ")"
			
		+ ")"
		;
		//System.out.println("Get_Open_Orders_For_PriceListCode_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Open_Orders_For_Terms_SQL(String sTerms){
		SQL = "SELECT " 
		+ SMTableorderheaders.sOrderNumber
		+ " FROM " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
		+ " WHERE ("
			+ "(" + SMTableorderdetails.TableName + ".dUniqueOrderID = " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + ")"
			
			+ " AND (" + SMTableorderheaders.sTerms + " = '" + sTerms + "')"
			
			+ " AND (" + SMTableorderdetails.TableName + ".dQtyOrdered != 0.00)"
			
			+ " AND ("
				+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
				+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
			+ ")"
			
		+ ")"
		;
		//System.out.println("Get_Open_Orders_For_Terms_SQL = " + SQL);
		return SQL;
	}

	public static String Get_Unexported_Invoices_For_Customer_SQL(String sCustomerNumber){
		SQL = "SELECT " 
		+ SMTableinvoiceheaders.sInvoiceNumber
		+ " FROM " + SMTableinvoiceheaders.TableName
		+ " WHERE ("
			+ "(" + SMTableinvoiceheaders.sCustomerCode + " = '" + sCustomerNumber + "')"
			+ " AND (" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
			
		+ ")"
		;
		//System.out.println("Get_Unexported_Invoices_For_Customer_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Unexported_Invoices_For_AcctSet_SQL(String sAcctSet){
		SQL = "SELECT " 
		+ SMTableinvoiceheaders.sInvoiceNumber
		+ " FROM " + SMTableinvoiceheaders.TableName
		+ " WHERE ("
			+ "(" + SMTableinvoiceheaders.sCustomerControlAcctSet + " = '" + sAcctSet + "')"
			+ " AND (" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
			
		+ ")"
		;
		//System.out.println("Get_Unexported_Invoices_For_AcctSet_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Unexported_Invoices_For_Terms_SQL(String sTerms){
		SQL = "SELECT " 
		+ SMTableinvoiceheaders.sInvoiceNumber
		+ " FROM " + SMTableinvoiceheaders.TableName
		+ " WHERE ("
			+ "(" + SMTableinvoiceheaders.sTerms + " = '" + sTerms + "')"
			+ " AND (" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
			
		+ ")"
		;
		//System.out.println("Get_Unexported_Invoices_For_Terms_SQL = " + SQL);
		return SQL;
	}
	public static String Get_Unexported_InvoiceDetails_For_GLAcct_SQL(String sGLAcct){
		SQL = "SELECT " 
		+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
		+ " FROM " + SMTableinvoiceheaders.TableName + ", " + SMTableinvoicedetails.TableName
		+ " WHERE ("
			+ "("
				+ "(" + SMTableinvoicedetails.sExpenseGLAcct + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableinvoicedetails.sInventoryGLAcct + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableinvoicedetails.sRevenueGLAcct + " = '" + sGLAcct + "')"
			+ ")"
			
			+ " AND ("
			
			+ " (" + SMTableinvoiceheaders.iExportedToAR + " != 1) OR (" + SMTableinvoiceheaders.iExportedToIC + " != 1)"
			
			+ ")"
			
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ ")"
			
		+ ")"
		;
		//System.out.println("Get_Unexported_InvoiceDetails_For_GLAcct_SQL = " + SQL);
		return SQL;
	}

	//Update_OpenTransactions_SQL
	public static String Update_OpenTransactions_SQL(
			String stransactionid,
			String sbatchnumber,
			String sentrynumber,
			String spayeepayor,
			String sdocnumber,
			String sdoctype,
			String sterms,
			String sdatdocdate,
			String sdatduedate,
			String sdoriginalamt,
			String sdcurrentamt,
			String sdocdescription,
			String sordernumber,
			String sretainage,
			String sponumber
			){
		SQL = "UPDATE " + SMTableartransactions.TableName + " SET "
		
		+ SMTableartransactions.datdocdate + " = '" + sdatdocdate + "'"
		+ ", " + SMTableartransactions.datduedate + " = '" + sdatduedate + "'"
		+ ", " + SMTableartransactions.dcurrentamt + " = " + sdcurrentamt
		+ ", " + SMTableartransactions.doriginalamt + " = " + sdoriginalamt
		+ ", " + SMTableartransactions.idoctype + " = " + sdoctype
		+ ", " + SMTableartransactions.loriginalbatchnumber + " = " + sbatchnumber
		+ ", " + SMTableartransactions.loriginalentrynumber + " = " + sentrynumber
		+ ", " + SMTableartransactions.sdocdescription + " = '" + sdocdescription + "'"
		+ ", " + SMTableartransactions.sdocnumber + " = '" + sdocnumber + "'"
		+ ", " + SMTableartransactions.sordernumber + " = '" + sordernumber + "'"
		+ ", " + SMTableartransactions.spayeepayor + " = '" + spayeepayor + "'"
		+ ", " + SMTableartransactions.sterms + " = '" + sterms + "'"
		+ ", " + SMTableartransactions.iretainage + " = " + sretainage
		+ ", " + SMTableartransactions.sponumber + " = '" + sponumber + "'"

		+ " WHERE (" 
			+ "(" + SMTableartransactions.lid + " = " + stransactionid + ")"
			+ ")";
		//System.out.println("Update_OpenTransactions_SQL = " + SQL);
		return SQL;
	}
	
	//Add_New_OpenTransaction_SQL
	public static String Add_New_OpenTransaction_SQL(
			String sbatchnumber,
			String sentrynumber,
			String spayeepayor,
			String sdocnumber,
			String sdoctype,
			String sterms,
			String sdatdocdate,
			String sdatduedate,
			String sdoriginalamt,
			String sdcurrentamt,
			String sdocdescription,
			String sordernumber,
			String scontrolacct,
			String sretainage,
			String sponumber
			){
		SQL = "INSERT INTO " + SMTableartransactions.TableName + " ("
		
		+ SMTableartransactions.datdocdate
		+ ", " + SMTableartransactions.datduedate
		+ ", " + SMTableartransactions.dcurrentamt
		+ ", " + SMTableartransactions.doriginalamt
		+ ", " + SMTableartransactions.idoctype
		+ ", " + SMTableartransactions.loriginalbatchnumber
		+ ", " + SMTableartransactions.loriginalentrynumber
		+ ", " + SMTableartransactions.sdocdescription
		+ ", " + SMTableartransactions.sdocnumber
		+ ", " + SMTableartransactions.sordernumber
		+ ", " + SMTableartransactions.spayeepayor
		+ ", " + SMTableartransactions.sterms
		+ ", " + SMTableartransactions.scontrolacct
		+ ", " + SMTableartransactions.iretainage
		+ ", " + SMTableartransactions.sponumber

		+ ") VALUES ("
		+ "'" + sdatdocdate + "'"
		+ ", '" + sdatduedate + "'"
		+ ", " + sdcurrentamt
		+ ", " + sdoriginalamt
		+ ", " + sdoctype
		+ ", " + sbatchnumber
		+ ", " + sentrynumber
		+ ", '" + sdocdescription + "'"
		+ ", '" + sdocnumber + "'"
		+ ", '" + sordernumber + "'"
		+ ", '" + spayeepayor + "'"
		+ ", '" + sterms + "'"
		+ ", '" + scontrolacct + "'"
		+ ", " + sretainage
		+ ", '" + sponumber + "'" 
		+ ")";
		//System.out.println("Add_New_OpenTransaction_SQL = " + SQL);
		return SQL;
	}
	//Get_OpenTransactions_By_ID_SQL:
	public static String Get_OpenTransactions_By_ID_SQL(String sTransactionID){
		SQL = "SELECT *" 
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.lid + " = " + sTransactionID + ")";
		SQL += ")";
		//System.out.println("Get_OpenTransactions_By_ID_SQL = " + SQL);
		return SQL;
	}
	//Get_OpenTransactions_By_Customer_And_Doc_SQL:
	public static String Get_OpenTransactions_By_Customer_And_Doc_SQL(String sCustomerCode, String sDocnumber){
		SQL = "SELECT *" 
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerCode + "')"
			+ " AND (" + SMTableartransactions.sdocnumber + " = '" + sDocnumber + "')"
			+ ")";
		//System.out.println("Get_OpenTransactions_By_Customer_And_Doc_SQL = " + SQL);
		return SQL;
	}
	
	//Get_OpenInvoices_By_Customer:
	public static String Get_OpenAppliableDocuments_By_Customer(String sCustomerCode){
		SQL = "SELECT *" 
		+ " FROM " + SMTableartransactions.TableName
		+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerCode + "')"
			+ " AND (" 
				+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
				+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RETAINAGE_STRING + ")"
			+ ")"
			+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
			+ ")";
		//System.out.println("Get_OpenInvoices_By_Customer = " + SQL);
		return SQL;
	}

	public static String Get_ChildTransactionLines_By_TransactionID_SQL(String sTransactionID){
		SQL = "SELECT *" 
		+ " FROM " + SMTablearmatchingline.TableName
		+ ", " + SMTableartransactions.TableName
		+ " WHERE ("
			
			+ "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
			+ " = " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ")"
			
			+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber 
			+ " = " + SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + ")"

			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.lid
			+ " = " + sTransactionID + ")"
			
			+ ")"
			
			+ " ORDER BY " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid;
		//System.out.println("Get_OpenTransactions_By_ID_SQL = " + SQL);
		return SQL;
	}
	
	//Get_Unposted_Entries_For_Customer
	public static String Get_Unposted_Entries_For_Customer(String sCustomerCode){
		SQL = "SELECT " + SMTableentries.lid 
		+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
		+ " WHERE ("
			+ "(" + SMTableentries.spayeepayor + " = '" + sCustomerCode + "')"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
		+ ")"
		;
		//System.out.println("Get_Unposted_Entries_For_Customer = " + SQL);
		return SQL;
	}
	//Get_Unposted_Entries_For_Terms
	public static String Get_Unposted_Entries_For_Terms(String sTermsCode){
		SQL = "SELECT " + SMTableentries.lid 
		+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
		+ " WHERE ("
			+ "(" + SMTableentries.stermscode + " = '" + sTermsCode + "')"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
		+ ")"
		;
		//System.out.println("Get_Unposted_Entries_For_Terms = " + SQL);
		return SQL;
	}
	//Get_Unposted_Entries_For_GLAcct
	public static String Get_Unposted_AR_Entries_For_GLAcct(String sGLAcct){
		SQL = "SELECT " + SMTableentries.lid 
		+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
		+ " WHERE ("
			+ "(" + SMTableentries.scontrolacct + " = '" + sGLAcct + "')"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
		+ ")"
		;
		//System.out.println("Get_Unposted_Entries_For_GLAcct = " + SQL);
		return SQL;
	}
	//Get_Unposted_TransactionLines_For_GLAcct
	public static String Get_Unposted_AR_Entry_Lines_For_GLAcct(String sGLAcct){
		SQL = "SELECT " + SMTableentries.TableName + "." + SMTableentries.lid 
		+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
		 + ", " + SMTableentrylines.TableName
		+ " WHERE ("
			+ "(" + SMTableentrylines.sglacct + " = '" + sGLAcct + "')"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + " = " 
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ientrynumber + " = " 
				+ SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber + ")"
			+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
			+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
		+ ")"
		;
		//System.out.println("Get_Unposted_TransactionLines_For_GLAcct = " + SQL);
		return SQL;
	}


	public static String Get_SM_Invoices_For_Import(){
		SQL = "SELECT " + SMTableinvoiceheaders.sInvoiceNumber
			
		+ " FROM " + SMTableinvoiceheaders.TableName

		+ " WHERE ("
			+ "(" + SMTableinvoiceheaders.iExportedToAR + " = 0)"

			+ ")"
		+ " ORDER BY " 
		+ SMTableinvoiceheaders.sInvoiceNumber;
		//System.out.println("Get_SM_Invoices_For_Import = " + SQL);
		return SQL;
	}

	public static String Create_Temporary_Aging_Table(String sTableName, boolean createAsTemporary){
		SQL = "CREATE";
			if(createAsTemporary){
				SQL = SQL + " TEMPORARY";
			}
			SQL = SQL + " TABLE " + sTableName + " ("
				+ "scustomer varchar(" + SMTablearcustomer.sCustomerNumberLength + ") NOT NULL default '',"
				+ "scustomername varchar(" + SMTablearcustomer.sCustomerNameLength + ") NOT NULL default '',"
				+ "ldocid int(11) NOT NULL default '0',"
				+ "idoctype int(11) NOT NULL default '0',"
				+ "sdocnumber varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "datdocdate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "datduedate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "datapplytodate datetime NOT NULL default '0000-00-00 00:00:00'," //Date of the apply-to trans
				+ "doriginalamt decimal(17,2) NOT NULL default '0.00',"
				+ "dcurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "sordernumber varchar(22) NOT NULL default '',"
				+ "ssource varchar(7) NOT NULL default '',"
				+ "lappliedto int(11) NOT NULL default '0',"
				+ "sdocappliedto varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "dagingcolumncurrent decimal(17,2) NOT NULL default '0.00',"
				+ "dagingcolumnfirst decimal(17,2) NOT NULL default '0.00',"
				+ "dagingcolumnsecond decimal(17,2) NOT NULL default '0.00',"
				+ "dagingcolumnthird decimal(17,2) NOT NULL default '0.00',"
				+ "dagingcolumnover decimal(17,2) NOT NULL default '0.00',"
				+ "dcreditlimit decimal(17,2) NOT NULL default '0.00',"
				+ "dbalance decimal(17,2) NOT NULL default '0.00',"
				+ "dretainagebalance decimal(17,2) NOT NULL default '0.00',"
				+ "dapplytodoccurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "lparenttransactionid int(11) NOT NULL default '0'"
				//+ ", KEY customerkey (scustomer)"
				//+ ", KEY appliedtokey (lappliedto)"
				//+ ", KEY docnumberkey (sdocnumber)"
				//+ ", KEY parenttransactionkey (lparenttransactionid)"
			+ ") " //ENGINE = MyISAM"
			;
		//System.out.println("Create_Temporary_Aging_Table = " + SQL);
		return SQL;
		
	}
	public static String Create_Temporary_Activity_Table(boolean createAsTemporary){
		SQL = "CREATE";
			if(createAsTemporary){
				SQL = SQL + " TEMPORARY";
			}
			SQL = SQL + " TABLE aractivitylines ("
				+ "scustomer varchar(" + SMTablearcustomer.sCustomerNumberLength + ") NOT NULL default '',"
				+ "scustomername varchar(" + SMTablearcustomer.sCustomerNameLength + ") NOT NULL default '',"
				+ "ldocid int(11) NOT NULL default '0',"
				+ "idoctype int(11) NOT NULL default '0',"
				+ "sdocnumber varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "datdocdate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "datduedate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "doriginalamt decimal(17,2) NOT NULL default '0.00',"
				+ "dcurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "sordernumber varchar(22) NOT NULL default '',"
				+ "ssource varchar(7) NOT NULL default '',"
				+ "lappliedto int(11) NOT NULL default '0',"
				+ "sdocappliedto varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "loriginalbatchnumber int(11) NOT NULL default '0',"
				+ "loriginalentrynumber int(11) NOT NULL default '0',"
				+ "ldaysover int(11) NOT NULL default '0',"
				+ "dcreditlimit decimal(17,2) NOT NULL default '0.00',"
				+ "dapplytodoccurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "lparenttransactionid int(11) NOT NULL default '0',"
				+ "KEY customerkey (scustomer),"
				+ "KEY appliedtokey (lappliedto),"
				+ "KEY docnumberkey (sdocnumber),"
				+ "KEY parenttransactionkey (lparenttransactionid)"
			+ ") " //ENGINE = MyISAM"
			;
		//System.out.println("Create_Temporary_Activity_Table = " + SQL);
		return SQL;
		
	}
	public static String Drop_Temporary_Aging_Table(String sTableName){
		SQL = "DROP TEMPORARY TABLE " + sTableName;
		//System.out.println("Drop_Temporary_Aging_Table = " + SQL);
		return SQL;
	}
	public static String Drop_Temporary_Activity_Table(){
		SQL = "DROP TEMPORARY TABLE aractivitylines";
		//System.out.println("Drop_Temporary_Activity_Table = " + SQL);
		return SQL;
	}

	public static String Insert_Transactions_Into_Activity_Table(
			String sStartingCustomer, 
			String sEndingCustomer, 
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			boolean bIncludeFullyPaidTransactions
			){
		SQL = "INSERT INTO aractivitylines ("
				+ "scustomer,"
				+ " ldocid,"
				+ " idoctype,"
				+ " sdocnumber,"
				+ " datdocdate,"
				+ " datduedate,"
				+ " doriginalamt,"
				+ " dcurrentamt,"
				+ " sordernumber,"
				+ " ssource,"
				+ " lappliedto,"
				+ " sdocappliedto,"
				+ " loriginalbatchnumber,"
				+ " loriginalentrynumber,"
				+ " ldaysover,"
				+ " dapplytodoccurrentamt,"
				+ " lparenttransactionid,"
				+ " scustomername,"
				+ " dcreditlimit"
				
				+ ") SELECT"
				+ " " + SMTableartransactions.spayeepayor
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.idoctype
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.datdocdate
				+ ", " + SMTableartransactions.datduedate
				+ ", " + SMTableartransactions.doriginalamt
				+ ", " + SMTableartransactions.dcurrentamt
				+ ", " + SMTableartransactions.sordernumber
				+ ", 'CONTROL'"
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.loriginalentrynumber
				+ ", 0"
				+ ", " + SMTableartransactions.dcurrentamt + " AS dapplytocurramt"
				+ ", " + SMTableartransactions.lid
				+ ", IF(" + SMTablearcustomer.sCustomerName + " IS NULL, '(NOT FOUND)', " 
					+ SMTablearcustomer.sCustomerName + ") AS scustomername"
				+ ", IF(" + SMTablearcustomer.dCreditLimit + " IS NULL, 0.00, " 
					+ SMTablearcustomer.dCreditLimit + ") AS dcreditlimit"
			+ " FROM " + SMTableartransactions.TableName + " LEFT JOIN " + SMTablearcustomer.TableName
			+ " ON " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + " = "
			+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
			+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor + ">='" + sStartingCustomer + "')"
				+ " AND (" + SMTableartransactions.spayeepayor + "<='" + sEndingCustomer + "')"
				+ " AND (" + SMTableartransactions.datdocdate + ">='" + clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + "')"
				+ " AND (" + SMTableartransactions.datdocdate + "<='" + clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + "')";
				if(!bIncludeFullyPaidTransactions){
					SQL = SQL + " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
				}
			SQL = SQL + ")"
			;
		//System.out.println("Insert_Transactions_Into_Activity_Table = " + SQL);
		return SQL;
	}

	public static String Update_Parent_Document_Type_In_Aging_Table(String sTableName){
		SQL = "UPDATE " + sTableName + ", " + SMTableartransactions.TableName
			+ " SET " + sTableName + ".idoctype = " 
			+ SMTableartransactions.TableName + "." + SMTableartransactions.idoctype
			+ " WHERE ("
				+ "(" + sTableName + ".ssource = 'DIST')"
				//Link the tables:
				+ " AND (" + sTableName + ".lparenttransactionid = "
					+ SMTableartransactions.TableName + "." + SMTableartransactions.lid + ")"
			+ ")"
			;
		//System.out.println("Update_Parent_Document_Type_In_Aging_Table = " + SQL);
		return SQL;
	}

	public static String Insert_Lines_Into_Activity_Table(
			String sStartingCustomer, 
			String sEndingCustomer, 
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			boolean bIncludeFullyPaidTransactions
			){
		SQL = "INSERT INTO aractivitylines ("
			+ "scustomer,"
			+ " ldocid,"
			+ " sdocnumber,"
			+ " datdocdate,"
			+ " datduedate,"
			+ " doriginalamt,"
			+ " dcurrentamt,"
			+ " sordernumber,"
			+ " ssource,"
			+ " lappliedto,"
			+ " sdocappliedto,"
			+ " dapplytodoccurrentamt,"
			+ " lparenttransactionid,"
			+ " scustomername,"
			+ " dcreditlimit,"
			+ " loriginalbatchnumber,"
			+ " loriginalentrynumber"
			
		+ ") SELECT"
			+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate
			//Applied amounts have the same sign as the apply-to amount, and so they must be negated:
			+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", ''"
			+ ", 'DIST'"
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
			+ ", IF(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + " IS NULL, '(NOT FOUND)', " 
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + ") AS scustomername"
			+ ", IF(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + " IS NULL, 0.00, " 
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + ") AS dcreditlimit"
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalbatchnumber
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalentrynumber
		+ " FROM " + SMTablearmatchingline.TableName + " LEFT JOIN " + SMTableartransactions.TableName
		+ " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid + " = "
		+  SMTableartransactions.TableName + "." + SMTableartransactions.lid
		+ " LEFT JOIN " + SMTablearcustomer.TableName + " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor 
		+ "=" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
		+ " WHERE"
			+ " " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ">='" 
				+ sStartingCustomer + "'"
			+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + "<='" 
				+ sEndingCustomer + "'"
			+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate + ">='" 
				+ clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + "'"
			+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate + "<='" 
				+ clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + "'"
		;
		//System.out.println("Insert_Lines_Into_Activity_Table = " + SQL);
		return SQL;
	}

	//Update_AgingColumns_In_Aging_Table
	public static String Update_AgingColumns_In_Aging_Table(
			String sTableName,
			String sAgedAsOfDate,
			String sCurrentAgingColumn,
			String sFirstAgingColumn,
			String sSecondAgingColumn,
			String sThirdAgingColumn
			){
		
		//Might want to allow user to choose to age by due date (datduedate) instead:
		SQL = "UPDATE " + sTableName + " SET" 
			+ " dagingcolumncurrent = IF ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) <= " + sCurrentAgingColumn + ", doriginalamt, 0.00)"
			+ ", dagingcolumnfirst = IF (((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) > " + sCurrentAgingColumn + ") AND ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) <= " + sFirstAgingColumn + "), doriginalamt, 0.00)"
			+ ", dagingcolumnsecond = IF (((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) > " + sFirstAgingColumn + ") AND ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) <= " + sSecondAgingColumn + "), doriginalamt, 0.00)"
			+ ", dagingcolumnthird = IF (((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) > " + sSecondAgingColumn + ") AND ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) <= " + sThirdAgingColumn + "), doriginalamt, 0.00)"
			+ ", dagingcolumnover = IF ((TO_DAYS('" + sAgedAsOfDate + "') - TO_DAYS(datapplytodate)) > " + sThirdAgingColumn + ", doriginalamt, 0.00)"
			;
		//System.out.println("Update_AgingColumns_In_Aging_Table = " + SQL);
		return SQL;
	}
	public static String Update_DaysOver_In_Activity_Table(
			){
		
		SQL = "UPDATE aractivitylines SET" 
			+ " ldaysover = IF((TO_DAYS(NOW()) - TO_DAYS(datduedate))>0,(TO_DAYS(NOW()) - TO_DAYS(datduedate)),0)"
			+ " WHERE ("
				+ "(ssource = 'CONTROL')"
				+ " AND (dcurrentamt != 0.00)"
			+ ")"
			;
		//System.out.println("Update_DaysOver_In_Activity_Table = " + SQL);
		return SQL;
	}
	public static String Remove_Fully_Paid_TransactionLines_From_Activity_Table(
		){

		SQL = "DELETE FROM aractivitylines" 
			+ " WHERE ("
				+ "(dcurrentamt = 0.00)"
			+ ")"
			;
		//System.out.println("Remove_Fully_Paid_TransactionLines_From_Activity_Table = " + SQL);
		return SQL;
	}

	public static String Delete_GL_Account_SQL(String sGLAcct){
		SQL = "DELETE FROM " + SMTableglaccounts.TableName
		+ " WHERE " + SMTableglaccounts.sAcctID + " = '" + sGLAcct + "'"
		;
		//System.out.println("Delete_GL_Account_SQL = " + sSQL);
		return SQL;
	}
	public static String Get_Activity_Report(){
	
		SQL = "SELECT * FROM aractivitylines"
			+ " ORDER BY scustomer, sdocappliedto, ssource, datdocdate";
		//System.out.println("Get_Activity_Report = " + SQL);
		return SQL;
	}
}
