package smar;
import java.sql.DriverManager;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;

import SMClasses.SMOHDirectQuoteList;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import smgl.GLFinancialDataCheck;
import smgl.GLTransactionBatch;

public class TESTBatchExport extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args){
		
		java.sql.Connection conn = null;
		
		//Localhost settings:
		String sURL = "localhost"; //Google Cloud SQL = 35.243.233.33
		String sDBID = "servmgr1"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "smuser7sT559";//"smuser7sT559";
		String sPassword = "kJ26D3G9bvK8";//"kJ26D3G9bvK8";
		
		//Google server settings:
//		String sURL = "35.243.233.33"; //Google Cloud SQL = 35.243.233.33
//		String sDBID = "servmgrmadg"; //servmgr1 - default
//		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
//		String sUser = "smuser7sT559";//"smuser7sT559";
//		String sPassword = "kJ26D3G9bvK8";//"kJ26D3G9bvK8";
		
		
		//OHD Tampa settings:
		/*
		sURL = "23.111.150.171";
		sDBID = "smcpcontrols"; //servmgr1 - default
		sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		sUser = "smadmin";
		sPassword = "jSdy78GHk9Ygh";
		*/
		
		//OHD Daytona settings:
		/*
		String sURL = "74.50.124.130";
		String sDBID = "smdaytona"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "smuser";
		String sPassword = "smuser";
		*/
		
		//String sConnectStringParams = "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		//sConnString = "jdbc:" + "mysql" + "://" + sURL + ":" + "3306" + "/" + "servmgr1" + sConnectStringParams;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Local string for laptop:
			conn = DriverManager.getConnection(sConnString, sUser, sPassword);

			
			//conn = DriverManager.getConnection("jdbc:mysql://" + "smcp001.com" + ":3306/" + "servmgr1" + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
				//+ "&allowMultiQueries=true"
				//,"smuser7sT559", "kJ26D3G9bvK8");
		}catch (Exception E) { 
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				//Local string for laptop:
				conn = DriverManager.getConnection(sConnString, sUser, sPassword);
				//conn = DriverManager.getConnection("jdbc:mysql://" + "smcp001.com" + ":3306/" + "servmgr1" + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
					//+ "&allowMultiQueries=true"
					//, "smuser7sT559", "kJ26D3G9bvK8");

			}catch(Exception F){
				System.out.println("[1579120402] " + F.getMessage() + " - " + F.getLocalizedMessage());
			}
			System.out.println("[1579120410] " + E.getMessage() + " - " + E.getLocalizedMessage());
		}

		/*
		//TEST PERVASIVE CONNECTION:
		Connection cnAP = null;
		String sAPDatabaseURL = "madg01.com";
		String sAPDatabaseName = "comp1";
		String sAPUserName = "jdbc";
		String sAPPassword = "TScb15^%!";
		try
			{
				cnAP = DriverManager.getConnection("jdbc:pervasive://" + sAPDatabaseURL + ":1583/" + sAPDatabaseName + "", sAPUserName, sAPPassword);
		}catch (Exception localException2) {
			try {
				Class.forName("com.pervasive.jdbc.v2.Driver").newInstance();
				cnAP = DriverManager.getConnection("jdbc:pervasive://" + sAPDatabaseURL + ":1583/" + sAPDatabaseName + "", sAPUserName, sAPPassword);
			} catch (InstantiationException e) {
				System.out.println("InstantiationException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (IllegalAccessException e) {
				System.out.println("IllegalAccessException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (SQLException e) {
				System.out.println("SQLException getting ACCPAC connection - " + e.getMessage());
				return;
			}
		}
		
		if (cnAP == null){
			System.out.println("Could not get Pervasive connection");
			return;
		}
		*/
		
		/*****************************************************
		// TEST MS SQL Connection:
		Connection cnAP = null;
		String sAPDatabaseURL = "madg01.com";
		String sAPDatabaseName = "comp1";
		String sAPUserName = "jdbc";
		String sAPPassword = "TScb15^%!";
		try
		{
			cnAP = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
		}
		catch (Exception localException2) {
			try {
				//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
				//Class.forName("com.microsoft.jdbc.sqlserver.sqlserverdriver").newInstance();
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
				//cnGL = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sGLDatabaseURL + ":1433;DatabaseName=" + sGLDatabaseName, sGLUserName, sGLPassword);
				cnAP = DriverManager.getConnection("jdbc:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
				//String Url = "jdbc:sqlserver://localhost:1433;databaseName=movies";
		        //Connection connection = DriverManager.getConnection(Url,"sa", "xxxxxxx);
			} catch (InstantiationException e) {
				System.out.println("InstantiationException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (IllegalAccessException e) {
				System.out.println("IllegalAccessException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException getting ACCPAC connection - " + e.getMessage());
				return;
			} catch (SQLException e) {
				System.out.println("SQLException getting ACCPAC connection - " + e.getMessage());
				return;
			}
		}
		if (cnAP == null){
			System.out.println("Could not get MS SQL connection");
			return;
		}
		*/
		
		GLFinancialDataCheck fdc = new GLFinancialDataCheck();
		try {
			fdc.checkMissingFiscalSets("12345073", conn, "");
		} catch (Exception e) {
			System.out.println("[202005151708] - " + e.getMessage());
		}
		System.out.println("[202005043334] - DONE");
		
		/*
    	GLTransactionBatch externalbatch = new GLTransactionBatch("1397");
    	try {
			externalbatch.loadExternalCompanyBatch(conn, "1", "1397");
		} catch (Exception e1) {
			System.out.println("[202005154012] - " + e1.getMessage());
		}
    	System.out.println("[202005043334] - DONE");
    	
    	GLTransactionBatch duplicatedbatch = null;
    	try {
    		duplicatedbatch = externalbatch.duplicateCurrentBatch(
    				conn, 
    				"Tom Ronayne", 
    				"2", 
    				"DBID", 
    				null
    		);
		} catch (Exception e) {
			System.out.println("[202005154541] - Exception: " + e.getMessage() + ".");
		}
    	
		//Save the batch:
		try {
			duplicatedbatch.save_without_data_transaction(conn, "2", "Tom Ronayne", false);
		} catch (Exception e) {
			System.out.println("[202005154542] - Exception: " + e.getMessage() + ".");
		}
		
		System.out.println("[202005043334] - DONE");
		*/
		
		/*
		//TEST OEAuth2 token processing:
		String sClientID = "OHDIRECT_TRN~RsIYM0KCBFUFPett0vpIByzc0lTOoWf_XmTNIyPPX9w";//clientId
		String sClientSecret = "HbKO2Gik6H6AY9ajZOCD3jr8Zs2Ya2B1yOcW8nbP4DnXcgUyZDXORg2X0qPA2NjV8uqSGObFGSiPXt5O_hll9A";//client secret
		String tokenURL = "https://mingle-sso.inforcloudsuite.com:443/OHDIRECT_TRN/as/token.oauth2"; //"https://api.byu.edu/token";
		String sTokenUserName = "OHDIRECT_TRN#OJN29nurKChctKa-uVCqIqigIHzSS6n1D5I4EJiRgQWnPZd_IQPA_oM7c2LV43tfMDt4DZajs5Ge0hwCSwf3EQ";
		String sTokenPassword = "RdlTFYkWWDXjSst7SHq1Cw6SwmvHpQ1yyG6g2YTRV1mB1seOFTP8oTdmAXZ7FwPayzR54VysYutAhD6k0ek8Aw";
		String sFullRequestString = "https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=C_QuoteNumberString%20eq%20'SQAL000008-1'";
		
		for (int i = 0; i < 40; i++) {
			try {
				clsOEAuthFunctions.getOHDirectToken(
					sTokenUserName, 
					sTokenPassword, 
					tokenURL, 
					sClientID, 
					sClientSecret);
				
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				System.out.println("[202004242608] - " + e.getMessage());
			} catch (Exception e) {
				System.out.println("[202004242626] - " + e.getMessage());
			}
		}
		System.out.println("DONE");
		*/
		
		/*
		//String sRequest = "C_DealerQuoteLineDetail?%24filter=C_QuoteLine%20eq%20'ecbb8a6f-0a46-4036-b8b1-ab8700e4cb20'";
		String sRequest = "C_DealerQuoteLineDetail?$filter=C_QuoteLine%20eq%20'ecbb8a6f-0a46-4036-b8b1-ab8700e4cb20'&%24orderby=C_SortOrder%20asc";
		SMOHDirectQuoteLineDetailList qldl = new SMOHDirectQuoteLineDetailList();
		try {
			qldl.getQuoteLineList(sRequest, conn);
		} catch (Exception e4) {
			System.out.println("[202004233047] - " + e4.getMessage());
		}
		
		for (int i = 0; i < qldl.getDescriptions().size(); i++) {
			System.out.println("Line Detail " + qldl.getSortOrders().get(i) + ", Desc: " + qldl.getDescriptions().get(i) + ": '" + qldl.getValues().get(i) + "'.");
		}
		System.out.println("DONE");
		*/
		
		/*
		String sRequest = "C_DealerQuoteLine?%24filter=C_Quote%20eq%20'00bac513-b658-ea11-82fa-d2da283a32ca'";
		ArrayList<String> arrQuoteLineIDs;
		ArrayList<String> arrQuoteNumbers;
		ArrayList<BigDecimal> arrLineNumbers;
		ArrayList<String> arrDescriptions;
		ArrayList<String> arrLastConfigurationDescriptions;
		ArrayList<BigDecimal> arrQuantities;
		ArrayList<BigDecimal> arrUnitCosts;
		ArrayList<BigDecimal> arrTotalCosts;
		SMOHDirectQuoteLineList qll = new SMOHDirectQuoteLineList();
		try {
			qll.getQuoteLineList(sRequest, conn);
		} catch (Exception e4) {
			System.out.println("[202004233047] - " + e4.getMessage());
		}
		arrQuoteNumbers = qll.getQuoteNumbers();
		arrQuoteLineIDs = qll.getQuoteLineIDs();
		arrLineNumbers = qll.getLineNumbers();
		arrDescriptions = qll.getDescriptions();
		arrLastConfigurationDescriptions = qll.getLastConfigurationDescriptions();
		arrQuantities = qll.getQuantities();
		arrUnitCosts = qll.getUnitCosts();
		arrTotalCosts = qll.getTotalCosts();
		
		for (int i = 0; i < arrQuoteNumbers.size(); i++) {
			System.out.println("Line " + arrLineNumbers.get(i) + ", ID: " + arrQuoteLineIDs.get(i) + ", Desc: '" + arrDescriptions.get(i) + "', "
					+ " Last config: '" + arrLastConfigurationDescriptions.get(i) + "', Qty: " + arrQuantities.get(i) + ", Unit Cost: " + arrUnitCosts.get(i) + ", Total: " + arrTotalCosts.get(i));
		}
		System.out.println("DONE");
		*/
		
		String sRequest = "C_DealerQuote?%24filter=C_LastModifiedDate%20gt%20'2020-01-09'";
		sRequest = "C_DealerQuote?%24filter="
			+ SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE + "%20gt%20'2020-01-09'"
			+ "&%24orderby=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE + "%20asc"
		;
		ArrayList<String> arrQuoteNumbers = new ArrayList<String>(0);
		ArrayList<String> arrNames = new ArrayList<String>(0);
		SMOHDirectQuoteList ql = new SMOHDirectQuoteList();
		String sUserID = "0";
		try {
			ql.getQuoteList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			System.out.println("[202004233047] - " + e4.getMessage());
		}
		arrQuoteNumbers = ql.getQuoteNumbers();
		arrNames = ql.getQuoteNames();
		for (int i = 0; i < arrNames.size(); i++) {
			System.out.println("Number " + arrQuoteNumbers.get(i) + ", Name " + i + " = '" + arrNames.get(i) + "'.");
		}
		System.out.println("DONE");
		

		/*
		String sOnHoldDate = "";
		try {
			ServletUtilities.clsDBServerTime dbtime;
			dbtime = new ServletUtilities.clsDBServerTime(conn);
			sOnHoldDate = dbtime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATETIME_FORMAT_FOR_DISPLAY);
			System.out.println("[202003271414] - sOnHoldDate = '" + sOnHoldDate + "'.");
		} catch (Exception e) {
			System.out.println("Error [202072930130] " + "Could not get current date and time - " + e.getMessage());
		}
		try {
			System.out.println("Date = '" +
				clsDateAndTimeConversions.convertDateFormat(
					sOnHoldDate, 
					SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
					SMUtilities.DATETIME_24HR_FORMAT_FOR_SQL, 
					SMUtilities.EMPTY_SQL_DATC_CreatedByETIME_VALUE) + "'"
			)
			;
		} catch (Exception e3) {
			System.out.println("[202003273417] - ");
		}
		*/
		
		/*
		APVendor ven = new APVendor();
		ven.setsvendoracct("OHD03");
		ven.load(conn);
		try {
			ven.mailNewVendorNotification("Tom Ronayne", "OHD Washington", conn);
		} catch (Exception e2) {
			System.out.println("Error  - " + e2.getMessage());
		}
		
		//Test emailing function:
		try {
			sendEmailWithEmbeddedHTML(
					"smtp.gmail.com",
			        "printmanager@odcdc.com", 
			        "madg1973!",
			        "465",
			        "printmanager@odcdc.com", 
			        "tjprona@gmail.com",
			        "Test Subject", 
			        "This is the body",
			        false,
			        null)
			;
		} catch (Exception e1) {
			System.out.println("Email NOT sent - " + e1.getMessage());
		}
		
		System.out.println("DONE");
		*/
		
		/*
		//Test PO on hold function:
		ICPOHeader pohead = new ICPOHeader();
		pohead.setsID("49694");
		if(!pohead.load(conn)){
			System.out.println("[202072150436] " + "error loading - " + pohead.getErrorMessages());
		}
		
		pohead.setipaymentonhold("1");
		pohead.setdatpaymentplacedonhold("03/12/2020 04:00:00 PM");
		pohead.setlpaymentonholdbyuserid("6");
		pohead.setmpaymentonholdreason("Test reason");
		pohead.setspaymentonholdbyfullname("Tom Ronayne");
		
		try {
			pohead.placeRelatedInvoicesOnHold(conn, pohead.getsID());
		} catch (Exception e) {
			System.out.println("[202072151164] " + " error - " + e.getMessage());
		}
		System.out.println("[202072151310] " + "Done");
		*/
		
		/*
		SMSalesOrderTaxCalculator sotc = null;
		SMOrderHeader order = new SMOrderHeader();
		order.setM_sOrderNumber("510859");
		if(!order.load(conn)){
			System.out.println(order.getErrorMessages());
		}
		try {
			sotc = new SMSalesOrderTaxCalculator(
					order.salesTaxRate(conn), 
				new BigDecimal(order.getM_dPrePostingInvoiceDiscountAmount().replace(",","")));
			;
		} catch (SQLException e) {
			System.out.println("Error [1411066627] calculating taxes - " + e.getMessage());
		}
		for (int i = 0; i < order.get_iOrderDetailCount(); i++){
			SMOrderDetail detail = order.getOrderDetail(i);
			try {
				sotc.addLine(
						new BigDecimal(detail.getM_dExtendedOrderPrice().replace(",", "")), 
						Integer.parseInt(detail.getM_iTaxable()), 
						new BigDecimal(detail.getM_dQtyShipped().replace(",", "")),
						detail.getM_sItemNumber());
				System.out.println("[2020281513337] " + "Ext price for line " + i + " = " + detail.getM_dExtendedOrderPrice());
			} catch (NumberFormatException e) {
				System.out.println("Number format error [1411066629] loading line to calculate taxes - " + detail.getM_sItemNumber() 
					+ " - detail.getM_iTaxable() = " + detail.getM_iTaxable() + " - " + e.getMessage());
			} catch (Exception e) {
				System.out.println("General error [1411066630] getting loading line to calculate taxes - " + e.getMessage());
			}
		}
		try {
			sotc.calculateSalesTax();
		} catch (Exception e) {
			System.out.println("Error [1411066631] calculating taxes - " + e.getMessage());

		}
		
		for (int i = 0; i < sotc.getLineCount(); i++){
			String sTaxable = "Y";
			if (sotc.getIsLineTaxable(i) == 0){
				sTaxable = "N";
			}
			System.out.println("Line " + Integer.toString((i + 1)) + " - " + sotc.getItem(i)
				+ " " + sotc.getQtyShipped(i).toString()
				+ ", TAXABLE: " + sTaxable
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getLineExtendedPriceBeforeDiscount(i))
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getLineExtendedPriceAfterDiscount(i))
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getSalesTaxAmountPerLine(i))
			);
		}
		System.out.println("DONE");
		*/
		
		
		//Test GL Transaction Batch posting:
		ServletUtilities.clsDatabaseFunctions.start_data_transaction(conn);
		GLTransactionBatch glbatch = new GLTransactionBatch("469");
		try {
			glbatch.post_with_connection(conn, "1", "airo");
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			System.out.println("[1579120434] " + e.getMessage());
		}
		//clsDatabaseFunctions.commit_data_transaction(conn);
		ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
		System.out.println("DONE");
		
		
		/*
		//Test GL conversion function:
		String s = "";
		try {
			GLACCPACConversion conv = new GLACCPACConversion();
			s = conv.processGLFinancialData(conn, "airo");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println(s);
		
		
		/*
		//Test purging GL data:
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		java.sql.Date datPurgeDeadline = null;
		try {
			datPurgeDeadline = new java.sql.Date(df.parse("01-01-1991").getTime());
		} catch (ParseException e) {
			System.out.println("[2019350150334] " + e.getMessage());
		}
		try {
			SMPurgeData.purgeData(
				datPurgeDeadline, 
				false, //boolean bPurgeOrders,
				false, //boolean bPurgeCustomerCallLogs,
				false, //boolean bPurgeBids,
				false, //boolean bPurgeSalesContacts,
				false, //boolean bPurgeSystemLog,
				false, //boolean bPurgeMaterialReturns,
				false, //boolean bPurgeSecuritySystemLogs,
				true, //bPurgeGLData,
				conn
				)
			;
		} catch (Exception e) {
			System.out.println("[2019350150582] " + e.getMessage());
		}
		System.out.println("DONE");
		*/
		
		/*
		//Test updating financial statement records:
		GLFinancialDataCheck dc = new GLFinancialDataCheck();
		try {
			System.out.println(dc.processFinancialRecords(
				"",
				"2018",
				conn,
				true
				)
			);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("DONE");
		*/
		
		/*
		//Test financial statement integrity:
		GLFinancialDataCheck objFinCheck = new GLFinancialDataCheck();
		try {
			//PrintWriter out = new PrintWriter(System.out);
			System.out.println(objFinCheck.checkFiscalSetsAgainstTransactions("", "2018", conn, false));
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
		System.out.println("DONE");create table glfinancialstatementdata_bak like glfinancialstatementdata;
create table glfiscalsets_bak like glfiscalsets;
create table gltransactionbatchentries_bak like gltransactionbatchentries;
create table gltransactionbatches_bak like gltransactionbatches;
create table gltransactionbatchlines_bak like gltransactionbatchlines;
create table gltransactionlines_bak like gltransactionlines;

insert into glfinancialstatementdata_bak select * from glfinancialstatementdata;
insert into glfiscalsets_bak select * from glfiscalsets;
insert into gltransactionbatchentries_bak select * from gltransactionbatchentries;
insert into gltransactionbatches_bak select * from gltransactionbatches;
insert into gltransactionbatchlines_bak select * from gltransactionbatchlines;
insert into gltransactionlines_bak select * from gltransactionlines;
		*/
		
		/*
		//test GLFiscalYear saving:
		GLFiscalYear fy = new GLFiscalYear();
		fy.set_sifiscalyear("2019");
		
		try {
			fy.load(conn);
			fy.saveWithConnection(conn, "0", "Tom R");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("DONE");
		*/
		
		/*
		//Test GL Pull:
		GLExternalPull pull = new GLExternalPull();
		try {
			pull.pullCompany(
					sDBID,
					"2",
					"Tom Ronayne",
					"1", 
					"2019", 
					"10", 
					conn,
					null);
		} catch (Exception e1) {
			System.out.println("Error - " + e1.getMessage());
		}
		System.out.println("DONE");
		*/
		
		/*
		//Test GL Transaction Batch for AR:
		ARBatch arbatch = new ARBatch("28260");
		
		try {
			arbatch.post_without_data_transaction(conn, "1", "Tom R");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("DONE");
		*/
		
		/*
		//Test financial statement integrity:
		GLFinancialDataCheck objFinCheck = new GLFinancialDataCheck();
		try {
			PrintWriter out = new PrintWriter(System.out);
			objFinCheck.readMatchingRecordsets(conn, cnAP, "2019", "10", "7790007304C", out, null, "servmgr1");
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
		System.out.println("DONE");
		
		try {
			System.out.println(objFinCheck.processFinancialRecords("10101073", "2016", conn, false, true, cnAP, null, null)); // "10101073"
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
		System.out.println("DONE");
		*/
		
		
		/*
				APBatch batch = new APBatch("514");

		ServletUtilities.clsDatabaseFunctions.start_data_transaction(conn);
		try {
			batch.post_with_connection(conn, "1", "Tom");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		ServletUtilities.clsDatabaseFunctions.commit_data_transaction(conn);
		System.out.println("DONE");
		*/

		
		//***********************************************************************
		//TEST EMAILER
		
		//Get the temporary file path for saving files:

/*
		//Email one copy to each person in the email address list:;
		String sSMTPServer = "smtp.gmail.com";
		String sSMTPUserName = "ohd-washington-noreply@odcdc.com";
		String sSMTPPassword = "SMTPPassword";
		String sEmailTo = "benzeher@odcdc.com";
		String sSMTPReplyToAddress = "ohd-washington-noreply@overheaddoors.com";
		String sEmailBody = "This is a test email message";
		String sEmailSubject = "Testing emailer";
		try {
			clsEmailInlineHTML.sendEmailWithEmbeddedHTML(
				sSMTPServer, //System option
				sSMTPUserName, //System option
				sSMTPPassword, //System option
				sEmailTo,
				sSMTPReplyToAddress,//System option
				sEmailSubject, 
				sEmailBody, 
				null,
				null
				);
		} catch (Exception e2) {
			System.out.println("Error sending email  - " + e2.getMessage());
		}
		System.out.println("Done email function.");
*/		
		/* IMPORT EXPENSED COST
		String SMTabletempExpensedCost = "tempExpensedCost";

		String sSQLtemp = "SELECT * FROM " + SMTabletempExpensedCost;
		try{
			ResultSet rsTempTable = clsDatabaseFunctions.openResultSet(sSQLtemp, conn);		    
			
			//Loop through the entire temp table containing all invoice detail of non-stock items and corresponding invoice date
			System.out.println("Looping through invoice detail in temp table...");
			int iterator = 0;
			while (rsTempTable.next()){
				iterator++;

				BigDecimal bdExpensedCost = new BigDecimal(0.00);
				boolean bUpdate = false;
				
				//Execute average per invoice line in temp table
				String sSQL = "SELECT " + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.sitemnumber
						+ ", ROUND(AVG(" + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdamount 
						+ " / " + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdqtyreceived + "),2) AS AVGCOST"
						+ " FROM " + SMTableaptransactionlines.TableName
						+ " LEFT JOIN " + SMTableaptransactions.TableName + " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid 
						+ " = " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid
						+ " WHERE ("
						+ "(" + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.sitemnumber + " = '" + rsTempTable.getString("sItemNumber") +  "')"
				//			+ " AND "
				//		+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " + "0" + ")"
							+ " AND "	
							+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + " <= '" + rsTempTable.getString("datInvoiceDate") + "')"
			//				+ " AND "
			//				+ " (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + " > '" + rsTempTable.getString("datInvoiceDateMinusThreeYears") + "')"
						+ ")"
						+ " GROUP BY (" + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.sitemnumber + ")"
						;
				try{
					ResultSet rsAPTransactionsAvg = clsDatabaseFunctions.openResultSet(sSQL, conn);		    
					if (rsAPTransactionsAvg.next()){
						bdExpensedCost = rsAPTransactionsAvg.getBigDecimal("AVGCOST");
						bUpdate = true;
					}
					
	
					rsAPTransactionsAvg.close();
				}catch (SQLException ex){
					System.out.println("Error in Calculate_Invoice_Detail_Expensed_Cost: " + ex.getMessage());
				}
				
				//Log every 1000th iteration
				if(iterator%1000 == 0) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					System.out.println("Record number: " + Integer.toString(iterator) + " Time: " + dateFormat.format(date));;
				}
				//Skip the update if the value is Zero
				if(bUpdate) {
					 
			        String sSQLUpdate = "UPDATE " + SMTableinvoicedetails.TableName 
			        				+ " SET"
			        				+ " " + SMTableinvoicedetails.bdexpensedcost + " = " + bdExpensedCost.toString()
			        				+ " WHERE(" 
			        				+ " (" +SMTableinvoicedetails.sInvoiceNumber+ " = '" +rsTempTable.getString("sInvoiceNumber") + "')"
			        				+ " AND "
			        				+ " (" + SMTableinvoicedetails.iLineNumber + " = "  + Integer.toString(rsTempTable.getInt("iLineNumber")) + ")"
			        				
			        				+ ")";
			        try{
			        	clsDatabaseFunctions.executeSQL(sSQLUpdate, conn);
			        }catch(SQLException ex){
			        		System.out.println("Error Updating!! - " + ex.getMessage());
			        }
				}
			}
		
		rsTempTable.close();
	}catch (SQLException ex){
		System.out.println("Error in Calculate Expensed Cost: " + ex.getMessage());
	}
		
		System.out.println("All Done!");
		*/
		/*
		String sNote = "123*SESSIONTAG*456";
		
		System.out.println(sNote);
		System.out.println(sNote.replaceAll("\\*SESSIONTAG\\*", ""));
		;
				
				;
		System.out.println(sNote);
		System.out.println(clsStringFunctions.filter(sNote));
		
		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
		rcpt.setsID("57578");
		rcpt.delete(conn, "TR", "6");
		*/

	}
	/*
    private static void sendEmailWithEmbeddedHTML(
    		String host,
            final String userName, 
            final String password,
            String sMailPort,
            String replytoAddress,
            String toAddress,
            String subject, 
            String body,
            boolean bUseHTML,
            Map<String, String> mapInlineImages)
                throws Exception {
        // sets SMTP server properties
        Properties properties;
		try {
			properties = new Properties();
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", sMailPort);
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.user", userName);
			properties.put("mail.password", password);
			//properties.put("mail.smtp.from", fromAddress);  //This line doesn't appear to have any effect..... TJR - 3/8/2018
		} catch (Exception e) {
			throw new Exception("Error [1395084034] - setting properties in EmailInlineHTML - " + e.getMessage());
		}
 
        // creates a new session with an authenticator
        Authenticator auth;
		try {
			auth = new Authenticator() {
			    public PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(userName, password);
			    }
			};
		} catch (Exception e) {
			throw new Exception("Error [1395085035] - getting Authenticator in EmailInlineHTML - " + e.getMessage());
		}
        Session session;
		try {
			session = Session.getInstance(properties, auth);
		} catch (Exception e) {
			throw new Exception("Error [1395085036] - getting session in EmailInlineHTML - " + e.getMessage());
		}
 
        // creates a new e-mail message
        Message msg;
		try {
			msg = new MimeMessage(session);
 
			msg.setFrom(new InternetAddress(userName));
			InternetAddress[] mailAddress_REPLY_TO = new InternetAddress[1];
			mailAddress_REPLY_TO[0] = new InternetAddress(replytoAddress);
			msg.setReplyTo(mailAddress_REPLY_TO);
			//InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
			//If there is more than one address, it should be separated with commas:
			if (toAddress.indexOf(',') > 0){ 
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			}else{
				//msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			}
			
			msg.setSubject(subject);
			//msg.setSentDate(new Date());
 
			if (bUseHTML){

				// adds inline image attachments
				if (mapInlineImages != null){
					// creates message part
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setContent(body, "text/html");
					// creates multi-part
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart);

					if (mapInlineImages.size() > 0) {
					    Set<String> setImageID = mapInlineImages.keySet();
					    for (String contentId : setImageID) {
					        MimeBodyPart imagePart = new MimeBodyPart();
					        imagePart.setHeader("Content-ID", "<" + contentId + ">");
					        imagePart.setDisposition(MimeBodyPart.INLINE);
					        String imageFilePath = mapInlineImages.get(contentId);
					        try {
					            imagePart.attachFile(imageFilePath);
					        } catch (IOException ex) {
					            ex.printStackTrace();
					        }
					        multipart.addBodyPart(imagePart);
					    }
					}
					msg.setContent(multipart);
				}else{
					msg.setContent(body, "text/html");
				}
			}else{
				//If we're NOT sending HTML:
				msg.setContent(body, "text/plain");
			}
		} catch (Exception e) {
			throw new Exception("Error [1395085037] - preparing message in sendEmailWithEmbeddedHTML - " + e.getMessage());
		}
        try {
			Transport.send(msg);
		} catch (Exception e) {
			throw new Exception("Error [1395085038] - sending message in sendEmailWithEmbeddedHTML - " + e.getMessage());
		}
    }
	*/
}
