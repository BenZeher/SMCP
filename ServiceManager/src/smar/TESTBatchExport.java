package smar;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMSalesOrderTaxCalculator;
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
		
//		//Google server settings:
//		String sURL = "35.243.233.33"; //Google Cloud SQL = 35.243.233.33
//		String sDBID = "servmgr1"; //servmgr1 - default
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
		
		
		/*
		//Test GL Transaction Batch posting:
		ServletUtilities.clsDatabaseFunctions.start_data_transaction(conn);
		GLTransactionBatch glbatch = new GLTransactionBatch("1");
		try {
			glbatch.post_with_connection(conn, "1", "airo");
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			System.out.println("[1579120434] " + e.getMessage());
		}
		//clsDatabaseFunctions.commit_data_transaction(conn);
		ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
		System.out.println("DONE");
		*/
		
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
		*/
		
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
}
