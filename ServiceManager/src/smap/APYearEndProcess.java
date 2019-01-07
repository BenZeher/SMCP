package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsDatabaseFunctions;

public class APYearEndProcess extends java.lang.Object{

	private static String m_sErrorMessage;
	
	APYearEndProcess(
			){
		m_sErrorMessage = "";
	}
	public static void processYearEnd(
			Connection conn
			) throws Exception{

		//First, make sure there are NO unposted batches in the system:
		String SQL = "SELECT * FROM " + SMTableapbatches.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
				+ " OR (" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
			+ ")"
			;
		boolean bOpenBatchesWereFound = false;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				bOpenBatchesWereFound = true;
				rs.close();
			}else{
				rs.close();
			}
		}catch(Exception e){
			throw new Exception("Error [1506367714] checking for open batches with SQL: " + SQL + " - " + e.getMessage());
		}
		if (bOpenBatchesWereFound){
			throw new Exception("There are open batches that must be posted or deleted before this function can be run.");
		}
		
		
		//Start a data transaction:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Could not start data transaction");
		}
		
		//Next, run an update statement to update the vendor statistics from the current statistics.
		//SQL = "UPDATE " + SMTableapvendorstatistics.TableName
		//	+ " SET " + SMTableapvendorstatistics. + " = "
		//	+ SMTablearcustomerstatistics.sAmountOfHighestInvoice
		//	+ ", " + SMTablearcustomerstatistics.sHighestBalanceLastYear + " = "
		//	+ SMTablearcustomerstatistics.sHighestBalance
		//	;
		//System.out.println("UPDATE last year values = " + SQL);
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error updating last year values.";
				clsDatabaseFunctions.rollback_data_transaction(conn);
				//return false;
			}
		}catch(SQLException e){
			m_sErrorMessage = "Error updating last year values - " + e.getMessage();
			clsDatabaseFunctions.rollback_data_transaction(conn);
			//return false;
		}
		
		//Next, run an update statement to reset all the arcustomer statistics to zero to start over:
		//SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
		//+ " SET " + SMTablearcustomerstatistics.sAmountOfHighestInvoice + " = 0.00"
		//+ ", " + SMTablearcustomerstatistics.sHighestBalance + " = 0.00"
		//;
		//System.out.println("UPDATE current year values = " + SQL);
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error resetting current year values.";
				clsDatabaseFunctions.rollback_data_transaction(conn);
				//return false;
			}
		}catch(SQLException e){
			m_sErrorMessage = "Error resetting current year values - " + e.getMessage();
			clsDatabaseFunctions.rollback_data_transaction(conn);
			//return false;
		}
		
		//Commit the transaction:
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			m_sErrorMessage = "Could not commit data transaction";
			//return false;
		}
		
		//return true;
	}
	public static String getErrorMessage (){
		return m_sErrorMessage;
	}
}
