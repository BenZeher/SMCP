package smcontrolpanel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import SMDataDefinition.SMTablebidproductamounts;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTablecustomercalllog;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import SMDataDefinition.SMTableglexternalcompanypulls;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTableglfiscalsets;
import SMDataDefinition.SMTablegltransactionlines;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableinvoicemgrcomments;
import SMDataDefinition.SMTablelaborbackcharges;
import SMDataDefinition.SMTablematerialreturns;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordermgrcomments;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablessuserevents;
import SMDataDefinition.SMTablesystemlog;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMPurgeData extends java.lang.Object{

	private static String m_sErrorMessage;
	
	public SMPurgeData(
			){
		m_sErrorMessage = "";
	}
	public static void purgeData(
			java.sql.Date datPurgeDeadline, 
			boolean bPurgeOrders,
			boolean bPurgeCustomerCallLogs,
			boolean bPurgeBids,
			boolean bPurgeSalesContacts,
			boolean bPurgeSystemLog,
			boolean bPurgeMaterialReturns,
			boolean bPurgeSecuritySystemLogs,
			boolean bPurgeGLData,
			Connection conn
			) throws Exception{
		
		//If the user chose to purge NOTHING then advise and return:
		if (
			!bPurgeOrders
			&& !bPurgeCustomerCallLogs
			&& !bPurgeBids
			&& !bPurgeSalesContacts
			&& !bPurgeSystemLog
			&& !bPurgeMaterialReturns
			&& !bPurgeSecuritySystemLogs
			&& !bPurgeGLData
		){
			throw new Exception("You did not select anything to purge.");
		}
		
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
			purgeTables(
				conn, 
				datPurgeDeadline, 
				bPurgeOrders, 
				bPurgeCustomerCallLogs, 
				bPurgeBids, 
				bPurgeSalesContacts, 
				bPurgeSystemLog,
				bPurgeMaterialReturns,
				bPurgeSecuritySystemLogs,
				bPurgeGLData
			);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Purge failed - " + e.getMessage());
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Could not commit data transaction.");
		}
	}
	
	private static void purgeTables(
			Connection conn, 
			java.sql.Date datPurgeDeadline,
			boolean bPurgeOrders,
			boolean bPurgeCustomerCallLogs,
			boolean bPurgeBids,
			boolean bPurgeSalesContacts,
			boolean bPurgeSystemLog,
			boolean bPurgeMaterialReturns,
			boolean bPurgeSSLogs,
			boolean bPurgeGLData
		) throws Exception{

		if (bPurgeOrders){
			try {
				purgeOrders(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		if (bPurgeCustomerCallLogs){
			try {
				purgeCustomerCallLogs(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		if (bPurgeBids){
			try {
				purgeBids(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		/*
		if (bPurgeSalesContacts){
			try {
				purgeSalesContacts(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		*/
		if (bPurgeSystemLog){
			try {
				purgeSystemLogs(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}	
		if (bPurgeMaterialReturns){
			try {
				purgeMaterialReturns(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		if (bPurgeSSLogs){
			try {
				purgeSecuritySystemLogs(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
		if (bPurgeGLData){
			try {
				purgeGLData(conn, datPurgeDeadline);
			} catch (Exception e) {
				throw new Exception (e.getMessage());
			}
		}
	}
	private static void purgeOrders(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		//First, create a table listing orders that can be purged:
		String SQL = "DROP TABLE IF EXISTS TEMP_ORDER_LIST";
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//No problem, continue on
			}
		} catch (SQLException e) {
			// No problem
		}

	    SQL = "CREATE TABLE TEMP_ORDER_LIST ("
	    	+ "sOrderNumber varchar(" + Integer.toString(SMTableorderheaders.sOrderNumberLength) + ")"
	    	+ ", sTrimmedOrderNumber varchar(" 
	    		+ Integer.toString(SMTableorderheaders.sOrderNumberLength) + ")"
	    	+ ", bEligibleForPurge int(11)"
	    	+ ", KEY `OrderNumberKey` (sOrderNumber)"
	    	+ ", KEY `TrimmedOrderNumberKey` (sTrimmedOrderNumber)"
	    	+ ", KEY `EligibleForPurgeKey` (bEligibleForPurge)"
	    	+ ")"
	    	;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not create temporary order list - " + e.getMessage() + ".");
		}
	    
	    //First, populate the order list table with ALL of the order numbers and uniquifiers:
	    //After this statement, all of the orders are listed in the temporary table
		//with a status of 'Eligible to purge':
	    SQL = "INSERT INTO TEMP_ORDER_LIST"
	    	+ " (sOrderNumber, bEligibleForPurge, sTrimmedOrderNumber)"
	    	+ " SELECT " + SMTableorderheaders.sOrderNumber + " AS sOrderNumber"
	    	+ ", 1 as `bEligibleForPurge`"
	    	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
	    		+ " AS sTrimmedOrderNumber"
	    	+ " FROM " + SMTableorderheaders.TableName
	    	+ " WHERE ("
	    		+ SMTableorderheaders.datOrderDate + " < " + "'" + clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "'"
	    	+ ")"
	    	;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not insert into temporary order list - SQL:" + SQL + " - " + e.getMessage() + ".");
		}
		
	    //So now we have a table (TemporaryOrderList) with a list of ALL the order number and
	    //uniquifiers in the current data set.
		
	    //At this point, we have all the orders previous to the purge date listed in the 'TemporaryOrderList' table and
	    //each order is flagged as 'EligibletoPurge'.
	    
	    //Now if the remaining orders are either canceled OR have nothing remaining to ship,
	    //They can be flagged as purgeable in the temporary orders list.
	    
	    //Next, flag the 'unshipped' orders as NOT purgeable in the temporary order list:
	    SQL = "UPDATE TEMP_ORDER_LIST"
	    	+ " LEFT JOIN " + SMTableorderdetails.TableName
	    	+ " ON TEMP_ORDER_LIST.sTrimmedOrderNumber=" + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
	    	+ " SET `bEligibleForPurge` = 0"
	    	+ " WHERE ("
	        	+ " ((" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " <> 0)"
	        	+ " OR (" + SMTableorderdetails.TableName + "." 
	        		+ SMTableorderdetails.dQtyShippedToDate + " = 0))"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not flag unshipped from order list - " + e.getMessage() + ".");
		}
		
	    //Flag the canceled orders as 'purgeable' in the temporary order list, regardless of their 'shipped'
	    //or 'not shipped' state:
	    SQL = "UPDATE TEMP_ORDER_LIST"
	    	+ " LEFT JOIN " + SMTableorderheaders.TableName
	    	+ " ON TEMP_ORDER_LIST.sTrimmedOrderNumber=" + SMTableorderheaders.TableName + "." + SMTableorderdetails.strimmedordernumber
		    + " SET `bEligibleForPurge` = 1"
		    + " WHERE ("
		        + "(" + SMTableorderheaders.TableName + "."
		        	+ SMTableorderheaders.datOrderCanceledDate + " >= '1900-01-01')" 
		    + ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not flag canceled orders - " + e.getMessage() + ".");
		}
	    
	    //Now delete all the 'nonpurgeable' orders from the list: what remains are the
	    //purgeable' orders:
	    SQL = "DELETE FROM TEMP_ORDER_LIST"
	    	+ " WHERE ("
	        	+ "`bEligibleForPurge` = 0"
	        + ")"
	    ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete nonpurgeable orders - " + e.getMessage() + ".");
		}
		
		//Now start actually purging records:
		//Remove order header records:
	    SQL = "DELETE FROM " + SMTableorderheaders.TableName
	    	+ " USING " + SMTableorderheaders.TableName + ", TEMP_ORDER_LIST"
	    	+ " WHERE ("
	        	+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
	        	+ " = TEMP_ORDER_LIST.sTrimmedOrderNumber"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete order headers - " + e.getMessage() + ".");
		}
		
		//Now remove any orphaned order detail records:
		SQL = "DELETE ORDDET.* FROM " + SMTableorderdetails.TableName + " ORDDET"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON ORDDET." + SMTableorderdetails.strimmedordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete orphaned order details - " + e.getMessage() + ".");
		}
		
		//TODO - change this when we can use a trimmed order number in ordermgrcomments:
		//Now remove orphaned order manager comment records:
		SQL = "DELETE ORDMGRCOMMENTS.* FROM " + SMTableordermgrcomments.TableName + " ORDMGRCOMMENTS"
				//TJROC
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON ORDMGRCOMMENTS." + SMTableordermgrcomments.sordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.sOrderNumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete orphaned order manager comments - " + e.getMessage() + ".");
		}
		
		//Remove invoice headers for those orders from the current database:
	    SQL = "DELETE FROM " + SMTableinvoiceheaders.TableName
	    	+ " USING " + SMTableinvoiceheaders.TableName + ", TEMP_ORDER_LIST"
	    	+ " WHERE ("
	        	+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber
	        		+ " = TEMP_ORDER_LIST.sTrimmedOrderNumber"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete invoice headers - " + e.getMessage() + ".");
		}
	    
		//Remove orphaned invoice detail records:
		SQL = "DELETE INVDET.* FROM " + SMTableinvoicedetails.TableName + " INVDET"
			+ " LEFT JOIN " + SMTableinvoiceheaders.TableName + " INVHEAD ON INVDET." + SMTableinvoicedetails.sInvoiceNumber
			+ " = INVHEAD." + SMTableinvoiceheaders.sInvoiceNumber
			+ " WHERE (INVHEAD." + SMTableinvoiceheaders.sInvoiceNumber + " IS NULL)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete invoice details - " + e.getMessage() + ".");
		}
		
		//Remove orphaned invoice manager comment records:
		SQL = "DELETE INVMGRCOMMENT.* FROM " + SMTableinvoicemgrcomments.TableName + " INVMGRCOMMENT"
				+ " LEFT JOIN " + SMTableinvoiceheaders.TableName + " INVHEAD ON INVMGRCOMMENT." + SMTableinvoicemgrcomments.sinvoicenumber
				+ " = INVHEAD." + SMTableinvoiceheaders.sInvoiceNumber
				+ " WHERE (INVHEAD." + SMTableinvoiceheaders.sInvoiceNumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete invoice manager comments - " + e.getMessage() + ".");
		}
		
		//Remove orphaned critical dates:
		SQL = "DELETE CRITICALDATE.* FROM " + SMTablecriticaldates.TableName + " CRITICALDATE"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON CRITICALDATE." + SMTablecriticaldates.sdocnumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete critical dates - " + e.getMessage() + ".");
		}
		
		//Remove change orders:
		SQL = "DELETE COS.* FROM " + SMTablechangeorders.TableName + " COS"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON COS." + SMTablechangeorders.sJobNumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete critical dates - " + e.getMessage() + ".");
		}
		
		//Remove work order records:
		SQL = "DELETE WO.* FROM " + SMTableworkorders.TableName + " WO"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON WO." + SMTableworkorders.strimmedordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete work order records - " + e.getMessage() + ".");
		}
		
		//Remove orphaned material return records:
		SQL = "DELETE MT.* FROM " + SMTablematerialreturns.TableName + " MT"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON MT." + SMTablematerialreturns.strimmedordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete material return records - " + e.getMessage() + ".");
		}
		
		//Remove orphaned delivery ticket records:
		SQL = "DELETE DT.* FROM " + SMTabledeliverytickets.TableName + " DT"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON DT." + SMTabledeliverytickets.strimmedordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete delivery ticket records - " + e.getMessage() + ".");
		}
		
		//Remove orphaned labor back charge records:
		SQL = "DELETE LBC.* FROM " + SMTablelaborbackcharges.TableName + " LBC"
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ORDHEAD ON LBC." + SMTablelaborbackcharges.strimmedordernumber
				+ " = ORDHEAD." + SMTableorderheaders.strimmedordernumber
				+ " WHERE (ORDHEAD." + SMTableorderheaders.strimmedordernumber + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete labor back charge records - " + e.getMessage() + ".");
		}
		
		//Finally, remove the temporary table:
		SQL = "DROP TABLE IF EXISTS TEMP_ORDER_LIST";
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//No problem, continue on
			}
		} catch (SQLException e) {
			// No problem
		}

	}
	private static void purgeCustomerCallLogs(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove customer call records before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablecustomercalllog.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablecustomercalllog.datCallTime + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete customer call records - " + e.getMessage() + ".");
		}
	}
	private static void purgeBids(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove sales leads before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablebids.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablebids.dattimeoriginationdate + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        	+ " AND (" + SMTablebids.sstatus + " != '" + SMTablebids.STATUS_PENDING + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete " + SMBidEntry.ParamObjectName + " records - " + e.getMessage() + ".");
		}
		
		//Remove  product amounts that go with the sales leads:
		SQL = "DELETE PA.* FROM " + SMTablebidproductamounts.TableName + " PA"
				+ " LEFT JOIN " + SMTablebids.TableName + " BIDALIAS ON PA." + SMTablebidproductamounts.lBidID
				+ " = BIDALIAS." + SMTablebids.lid
				+ " WHERE (BIDALIAS." + SMTablebids.lid + " IS NULL)"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete job cost records - " + e.getMessage() + ".");
		}

	}
	private static void purgeGLData(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		//Remove all the GL export headers and details:
	    String SQL = "DELETE FROM " + SMTableglexportheaders.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTableglexportheaders.datentrydate + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523511] - Could not delete " + "GL export header" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}

		SQL = "DELETE GLEXDETAILS.* FROM " + SMTableglexportdetails.TableName + " GLEXDETAILS"
			+ " LEFT JOIN " + SMTableglexportheaders.TableName + " GLEXHEADERS"
			+ " ON ("
			
			+ "(GLEXDETAILS." + SMTableglexportdetails.lbatchnumber 
				+ " = GLEXHEADERS." + SMTableglexportheaders.lbatchnumber + ")"
			+ " AND (GLEXDETAILS." + SMTableglexportdetails.lbatchentry 
				+ " = GLEXHEADERS." + SMTableglexportheaders.lbatchentry + ")"
			+ " AND (GLEXDETAILS." + SMTableglexportdetails.sdetailsourceledger
				+ " = GLEXHEADERS." + SMTableglexportheaders.ssourceledger + ")"
				
			+ ")"
			+ " WHERE (GLEXHEADERS." + SMTableglexportheaders.id + " IS NULL)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523512] - Could not delete " + "GL export detail" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}

		//Remove all records of previous external company 'pulls':
	    SQL = "DELETE FROM " + SMTableglexternalcompanypulls.TableName
    	+ " WHERE ("
        	+ "(" + SMTableglexternalcompanypulls.dattimepulldate + " < '" 
        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
        + ")"
        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523513] - Could not delete " + "external company 'pull'" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		//We need to get the last fiscal year that can be purged here since we need to go by fiscal years, not dates for the
		//rest of these deletions:
		//No matter what the purge date, we can remove up to the fiscal year of the purge date - we don't want to remove ANY records from
		//the actual fiscal year of the purge date, or otherwise the data for that year will be incomplete:
		String sFiscalYearToKeep = clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd").substring(0, 4);
		
		//Remove all gl financial statement data
	    SQL = "DELETE FROM " + SMTableglfinancialstatementdata.TableName
    	+ " WHERE ("
        	+ "(" + SMTableglfinancialstatementdata.ifiscalyear + " < " 
        		+ sFiscalYearToKeep + ")"
        + ")"
        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523514] - Could not delete " + "financial statement data" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		//Remove all fiscal year data:
	    SQL = "DELETE FROM " + SMTableglfiscalperiods.TableName
    	+ " WHERE ("
        	+ "(" + SMTableglfiscalperiods.ifiscalyear + " < " 
        		+ sFiscalYearToKeep + ")"
        + ")"
        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523515] - Could not delete " + "fiscal period" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		//Remove all fiscal set data:
	    SQL = "DELETE FROM " + SMTableglfiscalsets.TableName
    	+ " WHERE ("
        	+ "(" + SMTableglfiscalsets.ifiscalyear + " < " 
        		+ sFiscalYearToKeep + ")"
        + ")"
        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523516] - Could not delete " + "fiscal set" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		//Remove all transaction data:
	    SQL = "DELETE FROM " + SMTablegltransactionlines.TableName
    	+ " WHERE ("
        	+ "(" + SMTablegltransactionlines.ifiscalyear + " < " 
        		+ sFiscalYearToKeep + ")"
        + ")"
        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1576523517] - Could not delete " + "GL transaction line" + " records with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
	}
	/*
	private static void purgeSalesContacts(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove sales contact records from before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablesalescontacts.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablesalescontacts.datlastcontactdate + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        	+ " AND (" + SMTablesalescontacts.binactive + " = 1)"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete sales contact records - " + e.getMessage() + ".");
		}
	}
	
	*/
	private static void purgeSystemLogs(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove system log records from before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablesystemlog.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablesystemlog.datloggingtime + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete system log records - " + e.getMessage() + ".");
		}
	}
	private static void purgeMaterialReturns(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove material return records from before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablematerialreturns.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablematerialreturns.datresolved + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Could not delete material return records - " + e.getMessage() + ".");
		}
	}
	private static void purgeSecuritySystemLogs(
			Connection conn, 
			java.sql.Date datPurgeDeadline
		) throws Exception{

		// Remove device event records from before the purge deadline date:
	    String SQL = "DELETE FROM " + SMTablessdeviceevents.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablessdeviceevents.dattimeoccurrence + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1486478577] - Could not delete security system device event records - " + e.getMessage() + ".");
		}
		
		// Remove user event records from before the purge deadline date:
	    SQL = "DELETE FROM " + SMTablessuserevents.TableName
	    	+ " WHERE ("
	        	+ "(" + SMTablessuserevents.dattimeoccurrence + " < '" 
	        		+ clsDateAndTimeConversions.sqlDateToString(datPurgeDeadline, "yyyy-MM-dd") + "')"
	        + ")"
	        ;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1486478578] - Could not delete security system user event records - " + e.getMessage() + ".");
		}
	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
