package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

public class TRANSACTIONSQLs {

	private static String SQL = "";
	
	//Transaction batches:
	public static String Get_TransactionBatch_List_SQL(String sModuleType, String sLimit){
		SQL = "SELECT " 
		+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ", "
		+ SMEntryBatch.datbatchdate + ", "
		+ SMEntryBatch.ibatchstatus + ", "
		+ SMEntryBatch.ibatchtype + ", "
		+ SMEntryBatch.sbatchdescription + ", "
		+ SMEntryBatch.ibatchlastentry + ", "
		+ SMEntryBatch.TableName + "." + SMEntryBatch.lcreatedbyid + ", "
	
		+ "SUM(" + 
			SMTableentries.TableName + "." + SMTableentries.doriginalamount
			+ ") AS dbatchtotal"
	
		+ " FROM " + SMEntryBatch.TableName + " LEFT JOIN " 
			+ SMTableentries.TableName + " ON "
			+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
			+ " = " + SMTableentries.TableName + "." + SMTableentries.ibatchnumber
				
		+ " WHERE ("
			+ "(" + SMEntryBatch.smoduletype + " = '" + sModuleType + "')"
		+ ")"

		+ " GROUP BY " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
		
		+ " ORDER BY " 
		+ SMEntryBatch.ibatchnumber + " DESC"
		+ " LIMIT " + sLimit;
		//System.out.println("Get_TransactionBatch_List_SQL = " + SQL);
		return SQL;
	}
	public static String Get_PostedTransactionBatch_List_SQL(String sModuleType, String sLimit){
		SQL = "SELECT " 
		+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ", "
		+ SMEntryBatch.datbatchdate + ", "
		+ SMEntryBatch.ibatchstatus + ", "
		+ SMEntryBatch.ibatchtype + ", "
		+ SMEntryBatch.sbatchdescription + ", "
		+ SMEntryBatch.ibatchlastentry + ", "
		+ SMEntryBatch.TableName + "." + SMEntryBatch.lcreatedbyid
	
		+ " FROM " + SMEntryBatch.TableName

		+ " WHERE ("
			+ "(" + SMEntryBatch.smoduletype + " = '" + sModuleType + "')"
			+ " AND (" + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.POSTED + ")"
		+ ")"

        + " ORDER BY "
        + SMEntryBatch.ibatchnumber + " DESC";

        if (sLimit.trim().compareToIgnoreCase("") != 0){
                SQL = SQL + " LIMIT " + sLimit;
        }
        //System.out.println("Get_PostedTransactionBatch_List_SQL = " + SQL);
        return SQL;
	}
	public static String Get_TransactionBatch_List_SQL(String sModuleType){
		SQL = "SELECT " 
		+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ", "
		+ SMEntryBatch.datbatchdate + ", "
		+ SMEntryBatch.ibatchstatus + ", "
		+ SMEntryBatch.ibatchtype + ", "
		+ SMEntryBatch.sbatchdescription + ", "
		+ SMEntryBatch.ibatchlastentry + ", "
		+ SMEntryBatch.TableName + "." + SMEntryBatch.lcreatedbyid + ", "
	
		+ "SUM(" + 
			SMTableentries.TableName + "." + SMTableentries.doriginalamount
			+ ") AS dbatchtotal"
	
		+ " FROM " + SMEntryBatch.TableName + " LEFT JOIN " 
			+ SMTableentries.TableName + " ON "
			+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber 
			+ " = " + SMTableentries.TableName + "." + SMTableentries.ibatchnumber
				
		+ " WHERE ("
			+ "(" + SMEntryBatch.smoduletype + " = '" + sModuleType + "')"
		+ ")"

		+ " GROUP BY " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
		
		+ " ORDER BY " 
		+ SMEntryBatch.ibatchnumber + " DESC";
		//System.out.println("Get_TransactionBatch_List_SQL = " + SQL);
		return SQL;
	}
	
	public static String Add_New_TransactionBatch_SQL(
		String sBatchType,
		String sBatchStatus,
		String sBatchDescription,
		String sCreatedByID,
		String sCreatedByFullName,
		String sLastEditedByID,
		String sLastEditedByFullName,
		String sModuleType,
		String sLastEntry
		){

		SQL = "INSERT into " + SMEntryBatch.TableName
		+ " (" 
			+ SMEntryBatch.ibatchtype
			+ ", " + SMEntryBatch.datbatchdate
			+ ", " + SMEntryBatch.datlasteditdate
			+ ", " + SMEntryBatch.ibatchstatus
			+ ", " + SMEntryBatch.sbatchdescription
			+ ", " + SMEntryBatch.lcreatedbyid
			+ ", " + SMEntryBatch.screatedbyfullname
			+ ", " + SMEntryBatch.llasteditedbyid
			+ ", " + SMEntryBatch.slasteditedbyfullname
			+ ", " + SMEntryBatch.smoduletype
			+ ", " + SMEntryBatch.ibatchlastentry
		+ ")"
		+ " VALUES ("
			+ "'" + sBatchType + "'"
			+ ", NOW()"
			+ ", NOW()"
			+ ", " + sBatchStatus
			+ ", '" + sBatchDescription + "'"
			+ ", " + sCreatedByID + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sCreatedByFullName) + "'"
			+ ", " + sLastEditedByID + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sLastEditedByFullName) + "'"
			+ ", '" + sModuleType + "'"
			+ ", " + sLastEntry
		+ ")";
		//System.out.println ("Add_New_TransactionBatch_SQL = " + SQL);
		return SQL;
	}
	
	//Update_Batch_LastEntryNumber
	public static String Update_Batch_LastEntryNumber(
			String sBatchNumber,
			String sLastEntry
			){
			SQL = "UPDATE "
				+ SMEntryBatch.TableName
				+ " SET "
				+ SMEntryBatch.ibatchlastentry + " = " + sLastEntry
				+ " WHERE ("
					+ "(" + SMEntryBatch.ibatchnumber + " = " + sBatchNumber + ")"
				+ ")";
			
			//System.out.println("Update_TransactionBatch = " + SQL);
			return SQL;
		}

	//Flag_Batch_Deleted
	public static String Flag_Batch_Deleted(
			String sBatchNumber
			){
			SQL = "UPDATE "
				+ SMEntryBatch.TableName
				+ " SET "
				+ SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.DELETED
				+ " WHERE ("
					+ "(" + SMEntryBatch.ibatchnumber + " = " + sBatchNumber + ")"
				+ ")";
			
			//System.out.println("Flag_Batch_Deleted = " + SQL);
			return SQL;
		}
	
	//Get_Last_TransactionBatch
	public static String Get_Last_TransactionBatch(){
		SQL = "SELECT * " 
		+ " FROM " + SMEntryBatch.TableName
		+ " ORDER BY " 
		+ SMEntryBatch.ibatchnumber + " DESC "
		+ " LIMIT 1";
		//System.out.println("Get_Last_TransactionBatch = " + SQL);
		return SQL;
	}
	
	//Get_TransactionBatch_By_Number
	public static String Get_TransactionBatch_By_Number(String sBatchNumber){
		SQL = "SELECT * " 
		+ " FROM " + SMEntryBatch.TableName
		+ " WHERE (" 
		+ SMEntryBatch.ibatchnumber + " = " + sBatchNumber
		+ ")";
		//System.out.println("Get_TransactionBatch_By_Number = " + SQL);
		return SQL;
	}

	//Get_Number_Of_Entries_In_Batch
	public static String Get_Number_Of_Entries_In_Batch(String sBatchNumber){
		SQL = "SELECT COUNT(*) FROM " + SMTableentries.TableName
		+ " WHERE ("
			+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
		+ ")";
		//System.out.println("Get_Number_Of_Entries_In_Batch = " + SQL);
		return SQL;		
	}
	
	//Get_TransactionEntryList_By_BatchNumber
	public static String Get_TransactionEntryList_By_BatchNumber(String sBatchNumber){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentries.TableName
		+ " WHERE (" 
		+ SMTableentries.ibatchnumber + " = " + sBatchNumber
		+ ")";
		//System.out.println("Get_TransactionEntryList_By_BatchNumber = " + SQL);
		return SQL;
	}
	
	public static String Get_AscendingAppliedEntryLineList_By_BatchNumber(String sBatchNumber){
		SQL = "SELECT * FROM "
		+ SMTableentries.TableName + ", " + SMTableentrylines.TableName + ", " + SMTableartransactions.TableName
		+ " WHERE (" 
			+ "(" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber + " = " 
				+ SMTableentries.TableName + "." + SMTableentries.ientrynumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid + " != -1)"
			
			//Link the artransactions table to the entry - we want the transaction that was created by this 
			//entry:
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.spayeepayor + " = "
				+ SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ")"

			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.sdocnumber + " = "
				+ SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + ")"

		+ ")"
		+ " ORDER BY " 
			+ SMTableentries.TableName + "." + SMTableentries.ientrynumber 
			+ "," + SMTableentrylines.TableName + "." + SMTableentrylines.ilinenumber + " ASC";
		//System.out.println("Get_AscendingEntryLineList_By_BatchNumber = " + SQL);
		return SQL;
	}
	//Get_TransactionEntry
	public static String Get_TransactionEntry(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT " + SMTableentries.TableName + ".*"
		+ ", " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchtype
		+ " FROM " + SMTableentries.TableName
		+ ", " + SMEntryBatch.TableName
		+ " WHERE (" 
		+ "(" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" +  SMTableentries.TableName + "." + SMTableentries.ientrynumber + " = " + sEntryNumber + ")"
		+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
			+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
		+ ")";
		return SQL;
	}
	
	//Get_TransactionEntry_By_DocNumber
	public static String Get_TransactionEntry_By_DocNumber(String sCustomerNumber, String sDocNumber){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentries.TableName
		+ ", " + SMEntryBatch.TableName
		+ " WHERE (" 
		+ "(" + SMTableentries.spayeepayor + " = '" + sCustomerNumber + "')"
		+ " AND (" + SMTableentries.sdocnumber + " = '" + sDocNumber + "')"
		+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
			+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
		+ ")";
		//System.out.println("Get_TransactionEntry_By_DocNumber = " + SQL);
		return SQL;
	}
	
	//Get_TransactionEntry_By_EntryID
	public static String Get_TransactionEntry_By_EntryID(String sEntryID){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentries.TableName
		+ ", " + SMEntryBatch.TableName
		+ " WHERE (" 
		+ "(" + SMTableentries.lid + " = " + sEntryID + ")"
		+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
			+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
		+ ")";
		//System.out.println("Get_TransactionEntry_By_EntryID = " + SQL);
		return SQL;
	}
	
	//Get_TotalEntryAppliedAmount
	public static String Get_TotalEntryAppliedAmount(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT "
		+ " SUM(" + SMTableentrylines.damount + ")"
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")";
		//System.out.println("Get_TotalEntryAppliedAmount = " + SQL);
		return SQL;
	}
	
	//Add_TransactionEntry
	public static String Add_TransactionEntry(
		String sBatchNumber,
		String sEntryNumber,
		String sPayeePayor,
		String sDocNumber,
		String sDocDescription,
		String sDocDate,
		String sTerms,
		String sDueDate,
		String sLastLine,
		String sControlAccount,
		String sOrderNumber,
		String sOriginalAmount
		){
		SQL = "INSERT INTO "
			+ SMTableentries.TableName
			+ " ("
			+ SMTableentries.ibatchnumber
			+ ", " + SMTableentries.ientrynumber
			+ ", " + SMTableentries.spayeepayor
			+ ", " + SMTableentries.sdocnumber
			+ ", " + SMTableentries.sdocdescription
			+ ", " + SMTableentries.datdocdate
			+ ", " + SMTableentries.stermscode
			+ ", " + SMTableentries.datduedate
			+ ", " + SMTableentries.ilastline
			+ ", " + SMTableentries.scontrolacct
			+ ", " + SMTableentries.sordernumber
			+ ", " + SMTableentries.doriginalamount
			+ ") VALUES ("
			+ sBatchNumber
			+ ", " + sEntryNumber
			+ ", '" + sPayeePayor + "'"
			+ ", '" + sDocNumber + "'"
			+ ", '" + sDocDescription + "'"
			+ ", '" + sDocDate + "'"
			+ ", '" + sTerms + "'"
			+ ", '" + sDueDate + "'"
			+ ", " + sLastLine
			+ ", '" + sControlAccount + "'"
			+ ", '" + sOrderNumber + "'"
			+ ", " + sOriginalAmount
			+ ")";
		
		//System.out.println("Add_TransactionEntry = " + SQL);
		return SQL;
	}
	
	//Update_TransactionEntry
	public static String Update_TransactionEntry(
		String sBatchNumber,
		String sEntryNumber,
		String sDocumentType,
		String sPayeePayor,
		String sDocNumber,
		String sDocDescription,
		String sDocDate,
		String sTerms,
		String sDueDate,
		String sLastLine,
		String sControlAccount,
		String sOrderNumber,
		String sOriginalAmount
		){
		SQL = "UPDATE "
			+ SMTableentries.TableName
			+ " SET "
			+ SMTableentries.spayeepayor + " = '" + sPayeePayor + "'"
			+ ", " + SMTableentries.idocumenttype + " = " + sDocumentType
			+ ", " + SMTableentries.sdocnumber + " = '" + sDocNumber + "'"
			+ ", " + SMTableentries.sdocdescription + " = '" + sDocDescription + "'"
			+ ", " + SMTableentries.datdocdate + " = '" + sDocDate + "'"
			+ ", " + SMTableentries.stermscode + " = '" + sTerms + "'"
			+ ", " + SMTableentries.datduedate + " = '" + sDueDate + "'"
			+ ", " + SMTableentries.ilastline + " = " + sLastLine
			+ ", " + SMTableentries.scontrolacct + " = '" + sControlAccount + "'"
			+ ", " + SMTableentries.sordernumber + " = '" + sOrderNumber + "'"
			+ ", " + SMTableentries.doriginalamount + " = " + sOriginalAmount
			+ " WHERE ("
				+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
				+ " AND (" + SMTableentries.ientrynumber + " = " + sEntryNumber + ")"
			+ ")";
		
		//System.out.println("Update_TransactionEntry = " + SQL);
		return SQL;
	}
	
	//Get_Last_Entry_Number
	public static String Get_Last_Entry_Number(String sBatchNumber){
		SQL = "SELECT "
		+ SMTableentries.ientrynumber
		+ " FROM " + SMTableentries.TableName
		+ " WHERE (" 
		+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
		+ ")"
		+ " ORDER BY " + SMTableentries.ientrynumber + " DESC LIMIT 1";
		//System.out.println("Get_Last_Entry_Number = " + SQL);
		return SQL;
	}
	
	//Get_Number_Of_Lines_In_Entry
	public static String Get_Number_Of_Lines_In_Entry(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT COUNT(*) FROM " + SMTableentrylines.TableName
		+ " WHERE ("
			+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")";
		//System.out.println("Get_Number_Of_Lines_In_Entry = " + SQL);
		return SQL;		
	}
	
	//Delete_Transaction_Entry
	public static String Delete_Transaction_Entry(String sBatchNumber, String sEntryNumber){
		SQL = "DELETE " 
		+ " FROM " + SMTableentries.TableName
		+ " WHERE (" 
		+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentries.ientrynumber + " = " + sEntryNumber + ")"
		+ ")";
		//System.out.println("Delete_Transaction_Entry = " + SQL);
		return SQL;
	}
	
	//Renumber_Transaction_Entry
	public static String Renumber_Transaction_Entry(
			String sEntryID, 
			String sNewEntryNumber
			){
		SQL = "UPDATE " 
		+ SMTableentries.TableName
		+ " SET " + SMTableentries.ientrynumber
		+ " = " + sNewEntryNumber
		+ " WHERE (" 
		+ "(" + SMTableentries.lid + " = " + sEntryID + ")"
		+ ")";
		//System.out.println("Renumber_Transaction_Entry = " + SQL);
		return SQL;
	}
	
	//Get_Selected_TransactionLines
	public static String Get_Selected_TransactionLines(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")";
		//System.out.println("Get_Selected_TransactionLines = " + SQL);
		return SQL;
	}
	
	//Get the lines AND data from the 'Apply To' documents:
	//Here we are getting all the lines that are in this entry, based on the entry ID.
	//Additionally, we are reading information from the entries to which these lines apply:
	public static String Get_Applying_TransactionLines(String sEntryID){
		SQL = "SELECT "
		+ SMTableentrylines.TableName + "." + SMTableentrylines.ilinenumber
		+ ", " + SMTableentrylines.TableName + "." + SMTableentrylines.lid
		+ ", " + SMTableentrylines.TableName + "." + SMTableentrylines.damount
		+ ", " + SMTableentrylines.TableName + "." + SMTableentrylines.sapplytoordernumber
		+ ", " + SMTableentries.TableName + "." + SMTableentries.sdocnumber
		+ ", " + SMTableentries.TableName + "." + SMTableentries.idocumenttype
		+ ", " + SMTableentries.TableName + "." + SMTableentries.datdocdate
		+ ", " + SMTableentries.TableName + "." + SMTableentries.doriginalamount
		//+ ", " + SMTables.DATA_TABLE_SM_TRANSACTION_BATCHES + "." + SMTransactionBatch.ibatchstatus
		
		+ " FROM (" 
			+ SMTableentrylines.TableName + " LEFT JOIN " + SMTableentries.TableName
			+ " ON " + SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid
			+ "= " + SMTableentries.TableName + "." + SMTableentries.lid
		+ ")"
		
		//+ " INNER JOIN " + SMTables.DATA_TABLE_SM_TRANSACTION_BATCHES + " ON "
		//+ SMTableentries.TableName + "." + AREntry.ibatchnumber
		//+ " = " + SMTables.DATA_TABLE_SM_TRANSACTION_BATCHES + "." + SMTransactionBatch.ibatchnumber
		
		+ " WHERE (" 
			+ "(" + SMTableentrylines.TableName + "." + SMTableentrylines.lentryid + " = " + sEntryID + ")"
		//	+ " AND (" + SMTables.DATA_TABLE_SM_TRANSACTION_BATCHES + "." + SMTransactionBatch.ibatchstatus 
		//		+ " < " + SMBatchStatuses.POSTED + ")"
		+ ")";
		//System.out.println("Get_Applying_TransactionLines = " + SQL);
		return SQL;
	}
	
	//Get_TransactionLine
	public static String Get_TransactionLine(String sBatchNumber, String sEntryNumber, String sLineNumber){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ " AND (" + SMTableentrylines.ilinenumber + " = " + sLineNumber + ")"
		+ ")";
		//System.out.println("Get_TransactionLine = " + SQL);
		return SQL;
	}
	
	//Get_TransactionLine
	public static String Get_TransactionLine(String lLineID){
		SQL = "SELECT * " 
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.lid + " = " + lLineID + ")"
		+ ")";
		//System.out.println("Get_TransactionLine = " + SQL);
		return SQL;
	}
	
	//Delete_Transaction_Line
	public static String Delete_Transaction_Line(String sLineID){
		SQL = "DELETE " 
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.lid + " = " + sLineID + ")"
		+ ")";
		//System.out.println("Delete_Transaction_Line = " + SQL);
		return SQL;
	}
	
	//Delete_Transaction_Lines_For_Entry
	public static String Delete_Transaction_Lines_For_Entry(String sBatchNumber, String sEntryNumber){
		SQL = "DELETE " 
		+ "FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")";
		//System.out.println("Delete_Transaction_Lines_For_Entry = " + SQL);
		return SQL;
	}
	
	//Renumber_Transaction_Line_For_Entry
	public static String Renumber_Transaction_Line_For_Entry(
			String sBatchNumber, 
			String sOldEntryNumber,
			String sNewEntryNumber){
		SQL = "UPDATE " 
		+ SMTableentrylines.TableName
		+ " SET " + SMTableentrylines.ientrynumber
		+ " = " + sNewEntryNumber
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sOldEntryNumber + ")"
		+ ")";
		//System.out.println("Renumber_Transaction_Line_For_Entry = " + SQL);
		return SQL;
	}
	
	//Renumber_All_Lines_For_Batch
	public static String Renumber_All_Lines_For_Batch(
			String sBatchNumber 
			){
		
		SQL = "UPDATE " 
		+ SMTableentrylines.TableName + ", " + SMTableentries.TableName
		+ " SET " + SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber
		+ " = " + SMTableentries.TableName+ "." + SMTableentries.ientrynumber
		+ " WHERE ("
			+ "(" 
				+ SMTableentrylines.TableName + "." + SMTableentrylines.lentryid
				+ " = "
				+ SMTableentries.TableName+ "." + SMTableentries.lid 
			+ ")"
			+ " AND (" 
				+ SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber
				+ " = "
				+ sBatchNumber
			+ ")"
		+ ")";
		//System.out.println("Renumber_All_Lines_For_Batch = " + SQL);
		return SQL;
	}
	
	//Get_Last_Line_Number
	public static String Get_Last_Line_Number(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT "
		+ SMTableentrylines.ilinenumber
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
			+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")"
		+ " ORDER BY " + SMTableentrylines.ilinenumber + " DESC";
		//System.out.println("Get_Last_Line_Number = " + SQL);
		return SQL;
	}

	//Add_TransactionLine
	public static String Add_TransactionLine(
		String sBatchNumber,
		String sEntryNumber,
		String sLineNumber,
		String sDocAppliedTo,
		String sGLAcct,
		String sDescription,
		String sAmount,
		String sComment,
		String sDocAppliedToID,
		String sEntryID,
		String sApplyToOrderNumber
		){
		SQL = "INSERT INTO "
			+ SMTableentrylines.TableName
			+ " ("
			+ SMTableentrylines.ibatchnumber
			+ ", " + SMTableentrylines.ientrynumber
			+ ", " + SMTableentrylines.ilinenumber
			+ ", " + SMTableentrylines.damount
			+ ", " + SMTableentrylines.ldocappliedtoid
			+ ", " + SMTableentrylines.lentryid
			+ ", " + SMTableentrylines.scomment
			+ ", " + SMTableentrylines.sdescription
			+ ", " + SMTableentrylines.sdocappliedto
			+ ", " + SMTableentrylines.sglacct
			+ ", " + SMTableentrylines.sapplytoordernumber
			+ ") VALUES ("
			+ sBatchNumber
			+ ", " + sEntryNumber
			+ ", " + sLineNumber
			+ ", " + sAmount
			+ ", " + sDocAppliedToID
			+ ", " + sEntryID
			+ ", '" + sComment + "'"
			+ ", '" + sDescription + "'"
			+ ", '" + sDocAppliedTo + "'"
			+ ", '" + sGLAcct + "'"
			+ ", '" + sApplyToOrderNumber + "'"
			+ ")";
		
		//System.out.println("TRANSACTIONSQLs.Add_TransactionLine = " + SQL);
		return SQL;
	}

	//Update_TransactionLine
	public static String Update_TransactionLine(
		String sBatchNumber,
		String sEntryNumber,
		String sLineNumber,
		String sAmount,
		String sDocAppliedtoID,
		String sEntryID,
		String sComment,
		String sDescription,
		String sDocAppliedTo,
		String sGLAcct,
		String sApplyToOrderNumber
		){
		SQL = "UPDATE "
			+ SMTableentrylines.TableName
			+ " SET "

			+ SMTableentrylines.damount + " = " + sAmount
			+ ", " + SMTableentrylines.ldocappliedtoid + " = " + sDocAppliedtoID
			+ ", " + SMTableentrylines.lentryid + " = " + sEntryID
			+ ", " + SMTableentrylines.scomment + " = '" + sComment + "'"
			+ ", " + SMTableentrylines.sdescription + " = '" + sDescription + "'"
			+ ", " + SMTableentrylines.sdocappliedto + " = '" + sDocAppliedTo + "'"
			+ ", " + SMTableentrylines.sglacct + " = '" + sGLAcct + "'"
			+ ", " + SMTableentrylines.sapplytoordernumber + " = '" + sApplyToOrderNumber + "'"

			+ " WHERE ("
				+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
				+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
				+ " AND (" + SMTableentrylines.ilinenumber + " = " + sLineNumber + ")"
			+ ")";
		
		System.out.println("[1579186424] Update_TransactionLine = " + SQL);
		return SQL;
	}

	//Update_TransactionLine_By_ID
	public static String Update_TransactionLine_By_ID(
		String sLineID,
		String sBatchNumber,
		String sEntryNumber,
		String sLineNumber,
		String sAmount,
		String sDocAppliedtoID,
		String sEntryID,
		String sComment,
		String sDescription,
		String sDocAppliedTo,
		String sGLAcct,
		String sApplyToOrderNumber
		){
		SQL = "UPDATE "
			+ SMTableentrylines.TableName
			+ " SET "

			+ SMTableentrylines.damount + " = " + sAmount
			+ ", " + SMTableentrylines.ibatchnumber + " = " + sBatchNumber
			+ ", " + SMTableentrylines.ientrynumber + " = " + sEntryNumber
			+ ", " + SMTableentrylines.ilinenumber + " = " + sLineNumber
			+ ", " + SMTableentrylines.ldocappliedtoid + " = " + sDocAppliedtoID
			+ ", " + SMTableentrylines.lentryid + " = " + sEntryID
			+ ", " + SMTableentrylines.scomment + " = '" + sComment + "'"
			+ ", " + SMTableentrylines.sdescription + " = '" + sDescription + "'"
			+ ", " + SMTableentrylines.sdocappliedto + " = '" + sDocAppliedTo + "'"
			+ ", " + SMTableentrylines.sglacct + " = '" + sGLAcct + "'"
			+ ", " + SMTableentrylines.sapplytoordernumber + " = '" + sApplyToOrderNumber + "'"

			+ " WHERE ("
				+ "(" + SMTableentrylines.lid + " = " + sLineID + ")"
			+ ")";
		
		//System.out.println("TRANSACTIONSQLs.Update_TransactionLine_By_ID = " + SQL);
		return SQL;
	}

	
	//Get_AscendingLineList_By_Batch_And_Entry
	public static String Get_AscendingLineList_By_Batch_And_Entry(String sBatchNumber, String sEntryNumber){
		SQL = "SELECT "
		+ SMTableentrylines.lid
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
		+ " AND (" + SMTableentrylines.ientrynumber + " = " + sEntryNumber + ")"
		+ ")"
		+ " ORDER BY " + SMTableentrylines.ilinenumber + " ASC";
		//System.out.println("Get_AscendingLineList_By_Batch_And_Entry = " + SQL);
		return SQL;
	}
	
	//Get_AscendingLineList_By_EntryID
	public static String Get_AscendingLineList_By_EntryID(String sEntryID){
		SQL = "SELECT * "
		+ " FROM " + SMTableentrylines.TableName
		+ " WHERE (" 
		+ "(" + SMTableentrylines.lentryid + " = " + sEntryID + ")"
		+ ")"
		+ " ORDER BY " + SMTableentrylines.ilinenumber + " ASC";
		//System.out.println("Get_AscendingLineList_By_Batch_And_Entry = " + SQL);
		return SQL;
	}
	
	//Renumber_Transaction_Line
	public static String Renumber_Transaction_Line(
			String sLineID, 
			String sNewLineNumber
			){
		SQL = "UPDATE " 
		+ SMTableentrylines.TableName
		+ " SET " + SMTableentrylines.ilinenumber
		+ " = " + sNewLineNumber
		+ " WHERE (" 
		+ "(" + SMTableentrylines.lid + " = " + sLineID + ")"
		+ ")";
		//System.out.println("Renumber_Transaction_Line = " + SQL);
		return SQL;
	}
	
	//Update_Entry_LastLineNumber
	public static String Update_Entry_LastLineNumber(
			String sBatchNumber,
			String sEntryNumber,
			String sLastLineNumber
			){
			SQL = "UPDATE "
				+ SMTableentries.TableName
				+ " SET "
				+ SMTableentries.ilastline + " = " + sLastLineNumber
				+ " WHERE ("
					+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
					+ " AND (" + SMTableentries.ientrynumber + " = " + sEntryNumber + ")"
				+ ")";
			
			//System.out.println("Update_Entry_LastLineNumber = " + SQL);
			return SQL;
	}
	
	//Get_Total_PostedAmountApplied
	public static String Get_Total_PostedAmountApplied(String sEntryID){
		SQL = "SELECT SUM(" + SMTableentrylines.damount + ")"
		+ " FROM " 
			+ SMTableentrylines.TableName
			+ ", " + SMEntryBatch.TableName
		+ " WHERE ("
			+ "(" + SMTableentrylines.ldocappliedtoid + " = " + sEntryID + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber 
				+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
			+ " AND (" + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.POSTED + ")"
		+ ")";
		//System.out.println("Get_Total_PostedAmountApplied = " + SQL);
		return SQL;
	}
}
