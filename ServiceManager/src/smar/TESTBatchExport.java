package smar;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServlet;

import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableinvoicedetails;
import ServletUtilities.clsDatabaseFunctions;

public class TESTBatchExport extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public static void main(String[] args){
		
		java.sql.Connection conn = null;
		
		//Localhost settings:
		String sURL = "35.243.233.33"; //Google Cloud SQL = 35.243.211.251
		String sDBID = "servmgr3"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "root";//"smuser7sT559";
		String sPassword = "x14r7uidfDgvC4th";//"kJ26D3G9bvK8";
		
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
				System.out.println(F.getMessage() + " - " + F.getLocalizedMessage());
			}
			System.out.println(E.getMessage() + " - " + E.getLocalizedMessage());
		}
		
		
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
		/*
		APBatch batch = new APBatch("469");
		//clsDatabaseFunctions.start_data_transaction(conn);
		try {
			batch.loadBatch(conn);
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		clsDatabaseFunctions.rollback_data_transaction(conn);
		System.out.println("DONE");
		*/
		
		/*
		//Test GL conversion rollback:
		GLACCPACConversion conv = new GLACCPACConversion();
		try {
			conv.reverseDataChanges(conn, true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("DONE");
		*/
		/*
		APBatch batch = new APBatch("255");
		try {
			batch.load(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		clsDatabaseFunctions.start_data_transaction(conn);
		try {
			batch.post_with_connection(conn, "1", "Tom");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		System.out.println("DONE");
		*/
	}
}
