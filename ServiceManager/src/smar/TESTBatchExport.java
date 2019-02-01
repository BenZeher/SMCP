package smar;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;

import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import smic.ICPOReceiptHeader;

public class TESTBatchExport extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public static void main(String[] args){
		
		java.sql.Connection conn = null;
		
		//Localhost settings:
		String sURL = "localhost"; //Google Cloud SQL = 35.243.211.251
		String sDBID = "servmgr1"; //servmgr1 - default
		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
		String sUser = "smuser";//"smuser7sT559";
		String sPassword = "smuser";//"kJ26D3G9bvK8";
		
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
			while (rsTempTable.next()){
				
				BigDecimal bdExpensedCost = new BigDecimal(0.00);
				
				
				//Execute average per invoice line in temp table
				String sSQL = "SELECT " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber
						+ ", " + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdamount 
						+ ", " + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdqtyreceived 
						+ " FROM " + SMTableaptransactionlines.TableName
						+ " LEFT JOIN " + SMTableaptransactions.TableName + " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid 
						+ " = " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid
						+ " WHERE ("
						+ "(" + SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.sitemnumber + " = '" + rsTempTable.getString("sItemNumber") +  "')"
							+ " AND "
							+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = '" + "0" + "')"
							+ " AND "	
							+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + " <= '" + rsTempTable.getString("datInvoiceDate") + "')"
						+ ")"
						;
				try{
					ResultSet rsAPTransactions = clsDatabaseFunctions.openResultSet(sSQL, conn);		    
					
					BigDecimal bdQty = new BigDecimal(0.00);
					BigDecimal bdAmount = new BigDecimal(0.00);
					while (rsAPTransactions.next()){
						bdAmount.add(rsAPTransactions.getBigDecimal(SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdamount));
						bdQty.add(rsAPTransactions.getBigDecimal(SMTableaptransactionlines.TableName + "." +SMTableaptransactionlines.bdqtyreceived));

					}
					if(bdQty.compareTo(BigDecimal.valueOf(0.00)) < 1 ) {
						bdExpensedCost = new BigDecimal(0.00);
					}else {
						bdExpensedCost = bdAmount.divide(bdQty);
					}
					
					
					rsAPTransactions.close();
				}catch (SQLException ex){
					System.out.println("Error in Calculate_Invoice_Detail_Expensed_Cost: " + ex.getMessage());
				}
				

				 //after invoice is created, reset deposit on order.
		        String sSQLUpdate = "UPDATE " + SMTabletempExpensedCost 
		        				+ " SET"
		        				+ " " + "bdexpensedcost" + " = " + bdExpensedCost.toString()
		        				+ " WHERE(" 
		        				+ " (sInvoiceNumber" + " = '" +rsTempTable.getString("sInvoiceNumber") + "')"
		        				+ " AND "
		        				+ " (" + "iLineNumber = " + Integer.toString(rsTempTable.getInt("iLineNumber")) + ")"
		        				
		        				+ ")";
		        try{
		        	clsDatabaseFunctions.executeSQL(sSQLUpdate, conn);
		        }catch(SQLException ex){
		        		System.out.println("Error!! - " + ex.getMessage());
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
