package smar;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMDataDefinition.SMTablearcustomerstatistics;
import ServletUtilities.clsDatabaseFunctions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ARYearEndProcess extends java.lang.Object{

	private static String m_sErrorMessage;
	
	ARYearEndProcess(
			){
		m_sErrorMessage = "";
	}
	public static boolean processYearEnd(
			Connection conn
			){

		//First, make sure there are NO unposted batches in the system:
		String SQL = "SELECT * FROM " + SMEntryBatch.TableName
			+ " WHERE ("
				+ "(" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
				+ " OR (" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
			+ ")"
			;
		
		try{
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessage = "There are unposted batches - cannot run year end.";
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessage = "Error checking for open batches - " + e.getMessage();
			return false;
		}

		//Start a data transaction:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessage = "Could not start data transaction";
			return false;
		}
		
		//Next, run an update statement to update the arcustomer statistics from the current statistics.
		SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
			+ " SET " + SMTablearcustomerstatistics.sAmountOfHighestInvoiceLastYear + " = "
			+ SMTablearcustomerstatistics.sAmountOfHighestInvoice
			+ ", " + SMTablearcustomerstatistics.sHighestBalanceLastYear + " = "
			+ SMTablearcustomerstatistics.sHighestBalance
			;
		//System.out.println("UPDATE last year values = " + SQL);
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error updating last year values.";
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			m_sErrorMessage = "Error updating last year values - " + e.getMessage();
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//Next, run an update statement to reset all the arcustomer statistics to zero to start over:
		SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
		+ " SET " + SMTablearcustomerstatistics.sAmountOfHighestInvoice + " = 0.00"
		+ ", " + SMTablearcustomerstatistics.sHighestBalance + " = 0.00"
		;
		//System.out.println("UPDATE current year values = " + SQL);
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error resetting current year values.";
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}catch(SQLException e){
			m_sErrorMessage = "Error resetting current year values - " + e.getMessage();
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//Commit the transaction:
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			m_sErrorMessage = "Could not commit data transaction";
			return false;
		}
		
		return true;
	}
	public static String getErrorMessage (){
		return m_sErrorMessage;
	}
}
