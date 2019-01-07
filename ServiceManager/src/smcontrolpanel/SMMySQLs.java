package smcontrolpanel;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

public class SMMySQLs extends SMClasses.MySQLs{
	static String SQL = "";
	static SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");
	
	public static String Get_Insert_Customer_Call_Log_SQL(String sUserName, 
														  String sLogTime,
														  String sCustomerName,
														  String sCustomerPhone,
														  String sCityState,
														  String sCallTime,
														  int iSourceCode,
														  String sCallNote){
		
		SQL = "INSERT INTO " + SMTablecustomercalllog.TableName + " (" + 
				" " + SMTablecustomercalllog.sUserName + "," + 
				" " + SMTablecustomercalllog.datLogTime + "," +
				" " + SMTablecustomercalllog.sCustomerName + "," +
				" " + SMTablecustomercalllog.sPhoneNumber + "," +
				" " + SMTablecustomercalllog.sCity + "," +
				" " + SMTablecustomercalllog.datCallTime + "," +
				" " + SMTablecustomercalllog.iOrderSourceID + "," +
				" " + SMTablecustomercalllog.mNote + ")" +
			" VALUES(" + 
				" '" + sUserName + "'," +
				" '" + sLogTime + "'," +
				" '" + sCustomerName + "'," +
				" '" + sCustomerPhone + "'," +
				" '" + sCityState + "'," +
				" '" + sCallTime + "'," +
				" " + iSourceCode + "," +
				" '" + sCallNote + "')"; 
				
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Customer_Call_Log_List_SQL(Date datStart, 
														Date datEnd, 
														int iSource){
		
		SQL = "SELECT * FROM " + SMTablecustomercalllog.TableName + ", " + SMTableordersources.TableName + 
				" WHERE" +
					" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + " = " + 
						  SMTableordersources.TableName + "." + SMTableordersources.iSourceID + 
				  " AND" +
					" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime + " >= '" + datStart.toString() + " 00:00:00" + "'" +
				  " AND" +
					" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime + " <= '" + datEnd.toString() + " 23:59:59" + "'";
		if (iSource != 0){
			SQL = SQL + " AND " + 
					" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + " = '" + iSource + "'";
		}
		
		SQL = SQL + " ORDER BY " +
				SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + "," + 
				SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime;
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Sales_Contact_List_SQL(String sSalesperson, 
													//String sCustomer,
													int iCheckLastContactDate,
													String sLastContactStartingDate,
													String sLastContactEndingDate,
													int iCheckNextContactDate,
													String sNextContactStartingDate,
													String sNextContactEndingDate,
													int iActiveOnly
													){
		
		SQL = "SELECT * FROM" 
			+ " " + SMTablesalescontacts.TableName + " LEFT JOIN " + SMTablesalesperson.TableName 
			+ " ON " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + " = "
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			+ " LEFT JOIN " + SMTablearcustomerstatistics.TableName + " ON "
			+ SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber + " = "
			+ SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCustomerNumber 
			+ " WHERE (" 
				+ "(1 = 1)" //Just put this here so we could add 'ANDS' below...
			;
		if (sSalesperson.compareToIgnoreCase("ALLSP") != 0){
			SQL += " AND ("  + SMTablesalescontacts.salespersoncode + " = '" + sSalesperson + "')";
		}
		if (iCheckLastContactDate == 1){
			SQL += " AND (" + SMTablesalescontacts.datlastcontactdate + " >= '" + sLastContactStartingDate + "')" 
				+ " AND (" + SMTablesalescontacts.datlastcontactdate + " <= '" + sLastContactEndingDate + "')";
		}
		if (iCheckNextContactDate == 1){
			SQL += " AND (" + SMTablesalescontacts.datnextcontactdate + " >= '" + sNextContactStartingDate + "')" 
				+ " AND (" + SMTablesalescontacts.datnextcontactdate + " <= '" + sNextContactEndingDate + "')";
		}
		if (iActiveOnly == 1){
			SQL += " AND (" + SMTablesalescontacts.binactive + " = 0)"; 
		}
		
		SQL += ") ORDER BY" + 
				" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + "," + 
				" " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastInvoice + " DESC";
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Sales_Contact_List_SQL(String sSalesperson, 
													String sCustomer,
													String sContact,
													int iCheckLastContactDate,
													Timestamp tsLastContactStartingDate,
													Timestamp tsLastContactEndingDate,
													int iCheckNextContactDate,
													Timestamp tsNextContactStartingDate,
													Timestamp tsNextContactEndingDate,
													int iActiveOnly
													){
		
		SQL = "SELECT * FROM" + 
				" " + SMTablesalescontacts.TableName + "," + 
				" " + SMTablesalesperson.TableName + "," +
				" " + SMTablearcustomerstatistics.TableName + 
			  " WHERE" + 
			  	" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + " =" + 
			  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
			  	" AND" + 
			  	" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber + " =" + 
			  	" " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCustomerNumber;
			
		if (sSalesperson.compareTo("ALLSP") != 0){
			SQL = SQL + " AND "  + SMTablesalescontacts.salespersoncode + " = '" + sSalesperson + "'";
		}
		
		//System.out.println("Customer = '" + sCustomer + "'");
		if (sCustomer != null){
			if (sCustomer.trim().compareTo("") != 0){
				SQL = SQL + " AND " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber + " = '" + sCustomer + "'";
			}
		}
		
		if (sContact != null){
			if (sContact.trim().compareTo("") != 0){
				SQL = SQL + " AND " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scontactname + " = '" + sContact + "'";
			}
		}
		
		if (iCheckLastContactDate == 1){
			SQL = SQL + " AND " + SMTablesalescontacts.datlastcontactdate + " >= '" + tsLastContactStartingDate.toString() + "'" + 
				               " AND " + SMTablesalescontacts.datlastcontactdate + " <= '" + tsLastContactEndingDate.toString() + "'";
		}
		if (iCheckNextContactDate == 1){
			SQL = SQL + " AND " + SMTablesalescontacts.datnextcontactdate + " >= '" + tsNextContactStartingDate.toString() + "'" + 
				               " AND " + SMTablesalescontacts.datnextcontactdate + " <= '" + tsNextContactEndingDate.toString() + "'";
		}
		if (iActiveOnly == 1){
			SQL = SQL + " AND " + SMTablesalescontacts.binactive + " = 0"; 
		}
		
		SQL += " ORDER BY" + 
				" " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode + "," + 
				" " + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastInvoice;
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Sales_Contact_By_ID_SQL(int id){
		
		SQL = "SELECT * FROM " + SMTablesalescontacts.TableName + 
			  " WHERE" + 
			  	" " + SMTablesalescontacts.id + " = " + id;
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Delete_Sales_Contact_SQL(int id){

		SQL = "DELETE FROM " + SMTablesalescontacts.TableName +
			  " WHERE" + 
			  	" " + SMTablesalescontacts.id + " = " + id;
				
		//System.out.println(SQL);
		return SQL;
	}
	/*  +--------------------+-------------+------+-----+---------------------+----------------+
		| Field              | Type        | Null | Key | Default             | Extra          |
		+--------------------+-------------+------+-----+---------------------+----------------+
		| id                 | int(11)     |      | PRI | NULL                | auto_increment |
		| scustomernumber    | varchar(12) |      | MUL |                     |                |
		| salespersoncode    | char(3)     |      | MUL |                     |                |
		| scustomername      | varchar(60) |      |     |                     |                |
		| scontactname       | varchar(60) |      |     |                     |                |
		| sphonenumber       | varchar(20) |      |     |                     |                |
		| semailaddress      | varchar(75) |      |     |                     |                |
		| datlastcontactdate | datetime    |      |     | 0000-00-00 00:00:00 |                |
		| datnextcontactdate | datetime    |      |     | 0000-00-00 00:00:00 |                |
		| mnotes             | mediumtext  | YES  |     | NULL                |                |
		| binactive          | int(11)     |      |     | 0                   |                |
		+--------------------+-------------+------+-----+---------------------+----------------+
	 */

	public static String Insert_Sales_Contact_SQL(String sSalesperson, 
												  String sCustomerCode,
												  String sCustomerName,
												  String sContactName,
												  String sPhoneNumber,
												  String sEmailAddress,
												  String sLastContactDate,
												  String sNextContactDate,
												  int iInactive,
												  String sDescription,
												  String sNote
												  ){
					
		SQL = "INSERT INTO " + SMTablesalescontacts.TableName + "(" +
			" " + SMTablesalescontacts.salespersoncode + "," + 
			" " + SMTablesalescontacts.scustomernumber + "," +
			" " + SMTablesalescontacts.scustomername + "," +
			" " + SMTablesalescontacts.scontactname + "," +
			" " + SMTablesalescontacts.sphonenumber + "," +
			" " + SMTablesalescontacts.semailaddress + "," +
			" " + SMTablesalescontacts.datlastcontactdate + "," +
			" " + SMTablesalescontacts.datnextcontactdate + "," +
			" " + SMTablesalescontacts.binactive + "," +
			" " + SMTablesalescontacts.sdescription + "," +
			" " + SMTablesalescontacts.mnotes + ")" +
		" VALUES (" +
			" '" + sSalesperson + "'," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sCustomerCode) + "'," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sCustomerName) + "'," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sContactName) + "'," + 
			" '" + sPhoneNumber + "'," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sEmailAddress) + "'," + 
			" '" + sLastContactDate + "'," + 
			" '" + sNextContactDate + "'," + 
			" " + iInactive + "," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'," + 
			" '" + clsDatabaseFunctions.FormatSQLStatement(sNote) + "')"; 
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Bid_Follow_Up_List_SQL(String sSalesperson, 
												    int iProjectType,
												    int iCheckLastContactDate,
												    Timestamp tsLastContactStartingDate,
												    Timestamp tsLastContactEndingDate,
												    int iCheckNextContactDate,
												    Timestamp tsNextContactStartingDate,
												    Timestamp tsNextContactEndingDate,
												    String sSortOrder1,
												    String sSortOrder2,
												    int iStatusPending,
												    int iStatusSuccessful,
												    int iStatusUnsuccessful,
												    int iStatusInactive
												  	){
		
		SQL = "SELECT * FROM " + SMTablebids.TableName + ", " + SMTablesalesperson.TableName + ", " + SMTableprojecttypes.TableName + 
			  " WHERE" + 
			  	" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " =" + 
			  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
			  	" AND" + 
			  	" " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " =" +
			  	" " + SMTableprojecttypes.TableName + "." + SMTableprojecttypes.iTypeId;
		if (sSalesperson.compareTo("ALLSP") != 0){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " = '" + sSalesperson + "'";
		}
		if (iProjectType > 0){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = " + iProjectType;
		}
			  
		if (iCheckLastContactDate == 1){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " >= '" + tsLastContactStartingDate.toString() + "'" + 
			  			  " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " <= '" + tsLastContactEndingDate.toString() + "'";
		}
		if (iCheckNextContactDate == 1){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate + " >= '" + tsNextContactStartingDate.toString() + "'" + 
			  			  " AND " + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate + " <= '" + tsNextContactEndingDate.toString() + "'";
		}
		
		if (iStatusPending == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'";
		}
		if (iStatusSuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
		}
		if (iStatusUnsuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
		}
		if (iStatusInactive == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
		}
		
		//default to sort by bidding date.
		SQL = SQL + " ORDER BY ";
		if (sSortOrder1.compareTo("Salesperson") == 0 ){
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
		}else if (sSortOrder1.compareTo("Origination Date") == 0 ){
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.dattimeoriginationdate;
		}else if (sSortOrder1.compareTo("Customer Name") == 0 ){
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.scustomername;
		}else{
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
		}
		SQL = SQL + ", ";
		if (sSortOrder2.compareTo("Last Contact Date") == 0 ){
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate;
		}else if (sSortOrder2.compareTo("Next Contact Date") == 0 ){
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate;
		}else{
			SQL = SQL + SMTablebids.TableName + "." + SMTablebids.dattimebiddate;
		}
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Bid_TO_DO_List_SQL(String sSalesperson, 
											    int iProjectType,
											    int iCheckBidDate,
											    Timestamp tsBidDateStartingDate,
											    Timestamp tsBidDateEndingDate,
											    String sSelectedSortOrder,
											    int iStatusPending,
											    int iStatusSuccessful,
											    int iStatusUnsuccessful,
											    int iStatusInactive
											  	){

		SQL = "SELECT * FROM " + SMTablebids.TableName + ", " + SMTablesalesperson.TableName + 
			  " WHERE" + 
			  	" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " =" + 
			  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
			  	" AND" + 
			  	" " + SMTablebids.dattimeactualbiddate + " = '0000-00-00 00:00:00'";
		if (sSalesperson.compareTo("ALLSP") != 0){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " = '" + sSalesperson + "'";
		}
		if (iProjectType > 0){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = " + iProjectType;
		}
			  
		if (iCheckBidDate == 1){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + " >= '" + tsBidDateStartingDate.toString() + "'" + 
			  			  " AND " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + " <= '" + tsBidDateEndingDate.toString() + "'";
		}
		
		if (iStatusPending == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'";
		}
		if (iStatusSuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
		}
		if (iStatusUnsuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
		}
		if (iStatusInactive == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
		}
		
		//default to sort by bidding date.
		if (sSelectedSortOrder.compareTo("Salesperson") == 0){
			SQL = SQL + " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + "," + 
					" " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate;
		}else{
			SQL = SQL + " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.dattimebiddate + "," + 
					" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
		}
		//System.out.println(SQL);
		return SQL;
		
	}
	
	public static String Get_Bid_By_ID_SQL(int id){
		
		SQL = "SELECT * FROM " + SMTablebids.TableName + 
			  " WHERE" + 
			  	" " + SMTablebids.lid + " = " + id;
		
		//System.out.println(SQL);
		return SQL;
	}
	/*
	public static String Update_Bid_SQL(int id,
									    String sSalespersonCode,
									    Timestamp datOriginateDate,
									    String sCustomerName,
									    String sProjectName,
									    int iProjectType,
									    String sDescription,
									    String sBinNumber,
									    int iHasBidDate,
									    Timestamp datBidDate,
									    String sContactName,
									    String sPhoneNumber,
									    String sEmailAddress,
									    int iHasPlanReceivedDate,
									    Timestamp datPlanReceivedDate,
									    int iHasTakeoffCompleteDate,
									    Timestamp datTakeoffCompleteDate,
									    int iHasPriceCompletedDate,
									    Timestamp datPriceCompletedDate,
									    int iHasActualBidDate,
									    Timestamp datActualBidDate,
									    double dApproxAmount,
									    int iHasLastContactDate,
									    Timestamp datLastContactDate,
									    int iHasNextContactDate,
									    Timestamp datNextContactDate,
									    String sFollowUpNote,
									    String sBidStatus,
									    String sgdoclink){
		
		SQL = "UPDATE " + SMTablebids.TableName + " SET" + 
				" " + SMTablebids.ssalespersoncode + " = '" + sSalespersonCode + "'," + 
				" " + SMTablebids.dattimeoriginationdate + " = '" + datOriginateDate.toString() + "'," +
				" " + SMTablebids.scustomername + " = '" + sCustomerName + "'," +
				" " + SMTablebids.sprojectname + " = '" + sProjectName + "'," +
				" " + SMTablebids.iprojecttype + " = '" + iProjectType + "'," +
				" " + SMTablebids.mdescription + " = '" + sDescription + "'," +
				" " + SMTablebids.sbinnumber + " = '" + sBinNumber + "'," +
				" " + SMTablebids.scontactname + " = '" + sContactName + "'," +
				" " + SMTablebids.sphonenumber + " = '" + sPhoneNumber + "'," +
				" " + SMTablebids.emailaddress + " = '" + sEmailAddress + "'," + 
				" " + SMTablebids.dapproximateamount + " = '" + dApproxAmount + "',";
		if (iHasBidDate == 1){
			SQL = SQL + " " + SMTablebids.dattimebiddate + " = '" + datBidDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.dattimebiddate + " = '0000-00-00 00:00:00',";
		}
		if (iHasPlanReceivedDate == 1){
			SQL = SQL + " " + SMTablebids.dattimeplansreceived + " = '" + datPlanReceivedDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.dattimeplansreceived + " = '0000-00-00 00:00:00',";
		}
		if (iHasTakeoffCompleteDate == 1){
			SQL = SQL + " " + SMTablebids.dattimetakeoffcomplete + " = '" + datTakeoffCompleteDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.dattimetakeoffcomplete + " = '0000-00-00 00:00:00',";
		}
		if (iHasPriceCompletedDate == 1){
			SQL = SQL + " " + SMTablebids.dattimepricecomplete + " = '" + datPriceCompletedDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.dattimepricecomplete + " = '0000-00-00 00:00:00',";
		}
		if (iHasActualBidDate == 1){
			SQL = SQL + " " + SMTablebids.dattimeactualbiddate + " = '" + datActualBidDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.dattimeactualbiddate + " = '0000-00-00 00:00:00',";
		}
		if (iHasLastContactDate == 1){
			SQL = SQL + " " + SMTablebids.datlastcontactdate + " = '" + datLastContactDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.datlastcontactdate + " = '0000-00-00 00:00:00',";
		}
		if (iHasNextContactDate == 1){
			SQL = SQL + " " + SMTablebids.datnextcontactdate + " = '" + datNextContactDate.toString() + "',";
		}else{
			//wipe-out
			SQL = SQL + " " + SMTablebids.datnextcontactdate + " = '0000-00-00 00:00:00',";
		}
		SQL = SQL + 
				" " + SMTablebids.mfollwupnotes + " = '" + sFollowUpNote + "'," +
				" " + SMTablebids.sstatus + " = '" + sBidStatus + "'" +
				" " + SMTablebids.sgdoclink + " = '" + SMUtilities.FormatSQLStatement(sgdoclink) + "'" +
			" WHERE" + 
				  	" " + SMTablebids.lid + " = " + id;
			  	
		//System.out.println(SQL);
		return SQL;
		
	}
	
	public static String Insert_Bid_SQL(String sSalespersonCode,
									    Timestamp datOriginateDate,
									    String sCustomerName,
									    String sProjectName,
									    int iProjectType,
									    String sDescription,
									    String sBinNumber,
									    int iHasBidDate,
									    Timestamp datBidDate,
									    String sContactName,
									    String sPhoneNumber,
									    String sEmailAddress,
									    int iHasPlanReceivedDate,
									    Timestamp datPlanReceivedDate,
									    int iHasTakeoffCompleteDate,
									    Timestamp datTakeoffCompleteDate,
									    int iHasPriceCompletedDate,
									    Timestamp datPriceCompletedDate,
									    int iHasActualBidDate,
									    Timestamp datActualBidDate,
									    double dApproxAmount,
									    int iHasLastContactDate,
									    Timestamp datLastContactDate,
									    int iHasNextContactDate,
									    Timestamp datNextContactDate,
									    String sFollowUpNote,
									    String sBidStatus,
									    String sUserName,
									    String sgdoclink){
		
		SQL = "INSERT INTO " + SMTablebids.TableName + "(" + 
				SMTablebids.ssalespersoncode + ", " +
				SMTablebids.dattimeoriginationdate + ", " +
				SMTablebids.scustomername + ", " +
				SMTablebids.sprojectname + ", " +
				SMTablebids.iprojecttype + ", " +
				SMTablebids.mdescription + ", " +
				SMTablebids.sbinnumber + ", " +
				SMTablebids.scontactname + ", " +
				SMTablebids.sphonenumber + ", " +
				SMTablebids.emailaddress + ", " + 
				SMTablebids.dapproximateamount + ", ";
		if (iHasBidDate == 1){
			SQL = SQL + SMTablebids.dattimebiddate + ", ";
		}
		if (iHasPlanReceivedDate == 1){
			SQL = SQL + SMTablebids.dattimeplansreceived + ", ";
		}
		if (iHasTakeoffCompleteDate == 1){
			SQL = SQL + SMTablebids.dattimetakeoffcomplete + ", ";
		}
		if (iHasPriceCompletedDate == 1){
			SQL = SQL + SMTablebids.dattimepricecomplete + ", ";
		}
		if (iHasActualBidDate == 1){
			SQL = SQL + SMTablebids.dattimeactualbiddate + ", ";
		}
		if (iHasLastContactDate == 1){
			SQL = SQL + SMTablebids.datlastcontactdate + ", ";
		}
		if (iHasNextContactDate == 1){
			SQL = SQL + SMTablebids.datnextcontactdate + ", ";
		}
		SQL = SQL + 
				SMTablebids.mfollwupnotes + ", " +
				SMTablebids.sstatus + ", " +
				SMTablebids.sCreatedBy + 
				SMTablebids.sgdoclink + 
			") VALUES ( " + 
				" '" + sSalespersonCode + "'," + 
				" '" + datOriginateDate.toString() + "'," +
				" '" + sCustomerName + "'," +
				" '" + sProjectName + "'," +
				" " + iProjectType + "," +
				" '" + sDescription + "'," +
				" '" + sBinNumber + "'," +
				" '" + sContactName + "'," +
				" '" + sPhoneNumber + "'," +
				" '" + sEmailAddress + "'," +
				" '" + dApproxAmount + "',";
		if (iHasBidDate == 1){
			SQL = SQL + " '" + datBidDate.toString() + "',";
		}
		if (iHasPlanReceivedDate == 1){
			SQL = SQL + " '" + datPlanReceivedDate.toString() + "',";
		}
		if (iHasTakeoffCompleteDate == 1){
			SQL = SQL + " '" + datTakeoffCompleteDate.toString() + "',";
		}
		if (iHasPriceCompletedDate == 1){
			SQL = SQL + " '" + datPriceCompletedDate.toString() + "',";
		}
		if (iHasActualBidDate == 1){
			SQL = SQL + " '" + datActualBidDate.toString() + "',";
		}
		if (iHasLastContactDate == 1){
			SQL = SQL + " '" + datLastContactDate.toString() + "',";
		}
		if (iHasNextContactDate == 1){
			SQL = SQL + " '" + datNextContactDate.toString() + "',";
		}
		SQL = SQL + 
			  	" '" + sFollowUpNote + "', " + 
			  	" '" + sBidStatus + "', " + 
			  	" '" + sUserName + "'"
			  	+ "'" + SMUtilities.FormatSQLStatement(sgdoclink) + "'"
			  	+ ")";
			  	
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Delete_Bid_SQL(int id){

		SQL = "DELETE FROM " + SMTablebids.TableName +
			  " WHERE" + 
			  	" " + SMTablebids.lid + " = " + id;
				
		//System.out.println(SQL);
		return SQL;
	}
	*/
	public static String Get_Project_Type_SQL(){
		
		SQL = "SELECT * FROM " + SMTableprojecttypes.TableName;
		
		//System.out.println(SQL);
		return SQL;
	}
	/* LTO 20140108 Obsolete
	public static String Get_Distinct_Bin_List_SQL(){
		
		SQL = "SELECT DISTINCT " + SMTablebids.sbinnumber + 
			  " FROM " + SMTablebids.TableName + 
			  " ORDER BY" + 
			  	" " + SMTablebids.sbinnumber;

		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Bin_Number_List_SQL(String sBinNumber,
												 String sSalespersonCode,
												 int iProjectType,
												 int iCheckLastContactDate,
												 Timestamp tsLastContactStartDate,
												 Timestamp tsLastContactEndDate,
												 int iStatusPending,
												 int iStatusSuccessful,
												 int iStatusUnsuccessful,
												 int iStatusInactive){
		
		SQL = "SELECT * FROM " + SMTablebids.TableName + 
			  " WHERE 1=1 ";
		
		if (sBinNumber.compareTo("ALL") != 0){
			if (sBinNumber.compareTo("ALLBIN") == 0){
				SQL = SQL + " AND " + SMTablebids.sbinnumber + " <> ''";
			}else{
				SQL = SQL + " AND " + SMTablebids.sbinnumber + " = '" + sBinNumber + "'";	
			}
		}
		if (sSalespersonCode.compareTo("ALLSP") != 0){
			  SQL = SQL + " AND " + SMTablebids.ssalespersoncode + " = '" + sSalespersonCode + "'";
		}
		if (iProjectType > 0){
			  SQL = SQL + " AND " + SMTablebids.iprojecttype + " = " + iProjectType;
		}	  
		if (iCheckLastContactDate == 1){
			  SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " >= '" + tsLastContactStartDate.toString() + "'" + 
			  			  " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " <= '" + tsLastContactEndDate.toString() + "'";
		}
		if (iStatusPending == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'"; 
		}
		if (iStatusSuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
		}
		if (iStatusUnsuccessful == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
		}
		if (iStatusInactive == 0){
			SQL = SQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
		}
		
		//default to sort by bidding date.
		SQL = SQL + " ORDER BY " + SMTablebids.sbinnumber + "," + 
					" " + SMTablebids.datlastcontactdate;
		
		//System.out.println(SQL);
		return SQL;
	}
	*/
	public static String Get_Productivity_Report_SQL(Timestamp tsStartDate,
													 Timestamp tsEndDate,
													 ArrayList<String> alLocations,
													 ArrayList<String> alServiceTypes,
													 ArrayList<String> alItemCategories){
		
		SQL = "SELECT * FROM " + SMTableinvoiceheaders.TableName + ", " + 
								 SMTableinvoicedetails.TableName + 
				" WHERE" +
					" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" + 
					" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber +
				  " AND" +
				  	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + tsStartDate.toString() + "'" +
				  " AND" +
				  	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + tsEndDate.toString() + "'";
				   
		//if there is any location selected, attach them
		if (alLocations.size() == 0){
			//no location selected, make the SQL return nothing
			SQL = SQL + " AND" +
						  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + " = '-1'";
		}else{
			if (alLocations.get(0).toString().compareTo("ALLLOC") != 0){
				String sLocations = "";
				SQL = SQL + " AND (";
				for (int i=0;i<alLocations.size();i++){
					sLocations = sLocations + " OR" + 
								  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + " = '" + alLocations.get(i) + "'";
				}
				//remove the leading OR
				sLocations = sLocations.substring(4);
				SQL = SQL + sLocations + ")";
			}
		}
		   
		//if there is any service type selected, attach them
		if (alServiceTypes.size() == 0){
			//no location selected, make the SQL return nothing
			SQL = SQL + " AND" +
						  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = 'SH9999'";
		}else{
			if (alServiceTypes.get(0).toString().compareTo("ALLST") != 0){
				SQL = SQL + " AND (";
				String sServiceTypes = "";
				for (int i=0;i<alServiceTypes.size();i++){
					sServiceTypes = sServiceTypes + " OR" + 
								  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = '" + alServiceTypes.get(i) + "'";
				}
				//remove the leading OR
				sServiceTypes = sServiceTypes.substring(4);
				SQL = SQL + sServiceTypes + ")";
			}
		}
		   
		//if there is any category type selected, attach them
		if (alItemCategories.size() == 0){
			//no location selected, make the SQL return nothing
			SQL = SQL + " AND" +
						  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = ''";
		}else{
			if (alItemCategories.get(0).toString().compareTo("ALLIC") != 0){
				SQL = SQL + " AND (";
				String sItemCategories = "";
				for (int i=0;i<alItemCategories.size();i++){
					sItemCategories = sItemCategories + " OR" + 
								  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory + " = '" + alItemCategories.get(i) + "'";
				}
				//remove the leading OR
				sItemCategories = sItemCategories.substring(4);
				SQL = SQL + sItemCategories + ")";
			}
		}
		
		SQL = SQL + " ORDER BY" + 
						" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechInitial + ", " + 
						" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber;
		
		//System.out.println("Get_Productivity_Report_SQL: " + SQL);
		return SQL;
	}
	
	public static String Get_Open_Orders_Report_SQL(){
	
		SQL = "SELECT DISTINCT" + "\n" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + ","  + "\n" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "," + "\n" + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + "," + "\n" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + "," + "\n" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "," + "\n" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName + "," + "\n" + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + "," + "\n" + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + "\n" +
		
			  " FROM" + "\n" + 
				  " " + SMTableorderheaders.TableName + "\n" + 
		      " INNER JOIN" + 
				  " " + SMTableorderdetails.TableName + "\n" + 
			  " ON" + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " = " + 
				  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + "\n" + 
			  " WHERE (" + "\n" + 
			  	  " (" + "\n" +
			  	  	"(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')" + 
			  	" OR" +
			  	  " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " IS NULL))" + "\n" +
			  	" AND (" +
			  	  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " > 0)" + "\n" +
			  	  
			  	"/* NO QUOTES! */" + "\n" +
			  	" AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")" +
			  	")" + "\n" +
			  " ORDER BY" + 
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "," +
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "," +
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + "\n";

		System.out.println("[1522165727] - Open Orders SQL: '" + SQL + "'");
		return SQL;
	}
	
	/*
	 * SELECT OrderHeaders.sOrderNumber, 
	 * OrderHeaders.sBillToName, 
	 * OrderHeaders.sDefaultItemCategory, 
	 * OrderHeaders.LASTEDITUSER, 
	 * OrderHeaders.datOrderCanceledDate, 
	 * OrderHeaders.mInternalComments
FROM OrderHeaders
WHERE (OrderHeaders.datOrderCanceledDate)>["Enter Beginning Date"] 
	And (OrderHeaders.datOrderCanceledDate)<[ "Enter Ending Date"];

	 */

	public static String Get_Canceled_Jobs_Report_SQL(Timestamp tsStartDate,
													  Timestamp tsEndDate){
	
		SQL = "SELECT" +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + "," +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "," +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultItemCategory + "," + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.LASTEDITUSERFULLNAME + "," + 
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.mInternalComments +
			  " FROM" + 
				  " " + SMTableorderheaders.TableName + 
			  " WHERE" + 
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " >= '" + tsStartDate.toString() + "'" +
			  	" AND" +
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " <= '" + tsEndDate.toString() + "'";

		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Inter_Department_Labor_Charges_Report_SQL(Timestamp tsInvoiceStartDate,
																	   Timestamp tsInvoiceEndDate){
		
		SQL = "SELECT" + 
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName + "," + 
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber + "," + 
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + "," +
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + "," + 
				  " " + "if (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType + " = 0," +
				            " if (MID(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + ",1,3) = 'LAB', " + 
				            	" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped + "," +
				            	" 0" + 
				            	")" + 
				            	"," + 
				            " if (MID(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + ",1,3) = 'LAB', " + 
				            	" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped + " * -1," +
				            	" 0" + 
				            	")" + 
				            ")" +
				        " AS 'dQualityLaborShipped'," + 
				  //" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iFiscalPeriod + "," +
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType + "," +
				  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + "," +
				  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory + "," +
				  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + "," +
				  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sLocationCode + "," +
				  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultItemCategory +
			  " FROM" + 
			  	  " ((" + SMTableinvoiceheaders.TableName + 
			  	  " INNER JOIN" + 
			  	  " " + SMTableinvoicedetails.TableName + 
			  	  " ON" + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" +
			  	  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ")" +
			  	  " INNER JOIN" + 
			  	  " " + SMTableorderheaders.TableName + 
			  	  " ON" + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber + " = " + 
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + ")" + 
			  	  
			  	  " LEFT JOIN (" + Get_Warranty_Labor_SQL(tsInvoiceStartDate, tsInvoiceEndDate) + ") AS qryWarrantyLabor" + 
			  	  " ON" +  
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" +
			  	  " qryWarrantyLabor." + SMTableinvoicedetails.sInvoiceNumber + 
			  " GROUP BY" + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + "," + 
			  	  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sLocationCode + "," +
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName + "," +
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber + "," + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + "," + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + "," +
			  	  " 'dQualityLaborShipped'" + "," + 
			  	  //" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iFiscalPeriod + "," + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType + "," + 
			  	  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory + "," + 
			  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultItemCategory + "," + 
			  	  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + "," + 
			  	  " LEFT(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + ", 3)" +
			  " HAVING" + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + tsInvoiceStartDate.toString() + "'" + 
			  	  " AND" + 
			  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + tsInvoiceEndDate.toString() + "'" +
			  	  " AND" +
			  	  " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + " = 'LABIDL'";  

		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Warranty_Labor_SQL(Timestamp tsInvoiceStartDate,
			   									Timestamp tsInvoiceEndDate){
		
		SQL = "SELECT" + 
					" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + "," + 
					" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + "," + 
					//" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iFiscalPeriod + "," +
					" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + "," + 
					" " + "if (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + " = 'LABRM'" + "," + 
							" 'LABRM', '') AS bLABRM" +
			  " FROM" +
			  	" (" + SMTableinvoiceheaders.TableName + 
			  	" INNER JOIN" +
			  	" " + SMTableinvoicedetails.TableName + 
			  	" ON" + 
			  	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" +
			  	" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ")" +
			  	" INNER JOIN" + 
			  	" " + SMTableorderheaders.TableName +
			  	" ON" + 
			  	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber + " =" +
			  	" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber +
			  " GROUP BY" +
			  	" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + "," +
			  	//" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iFiscalPeriod + "," +
			  	" if (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + " = 'LABRM','LABRM','')" + 
			  " HAVING" +  
		  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + tsInvoiceStartDate.toString() + "'" + 
		  	  " AND" + 
		  	  " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + tsInvoiceEndDate.toString() + "'" +
		  	  " AND `bLABRM` = 'LABRM'"
		  	  //NO QUOTES!
		  	  + " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
		  	  + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
		  	  ;

		//System.out.println("Get_Warranty_Labor_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Order_Source_Listing_SQL(String sStartDate,
			   										  String sEndDate,
			   										  String sReportType, 
			   										  ArrayList<String> alServiceTypes,
			   										  ArrayList<String> alLocations){
		if (sReportType.compareTo("Orders") == 0){
			SQL = "SELECT"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderSourceID + " AS iOrderSourceID,"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderSourceDesc + " AS sOrderSourceDesc,"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " AS sDocNumber,"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + " AS datDocDate,"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + " AS sCustomerName,"
					+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderCreatedByFullName + " AS sCreatedByFullName,"
					+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice  + " * "
					+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered  + " +"
					+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShippedToDate  + ")"
					+ " " + "AS dAmount" + "," 
					+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.datLineBookedDate + ""
					+ " FROM "
				  	+ " " + SMTableorderdetails.TableName
				  	+ " INNER JOIN"
				  	+ " " + SMTableorderheaders.TableName 
				  	+ " ON" 
				  	+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + " = "
				  	+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier 
				  	+ " "
				  	+ " WHERE (" 
				  	+ " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + " >= '" + sStartDate + "')"
				  	+ " AND"
				  	+ " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + " <= '" + sEndDate + "')"
				  	+ " AND ("
				  		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '1899/12/31')" 
				  		+ " OR " 
				  		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '0000-00-00 00:00:00')"
				  		+ ")" 
				  	//NO QUOTES!
				  	+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
				  	+ SMTableorderheaders.ORDERTYPE_QUOTE + ")";
	
			SQL += " AND ("; 
			for (int i=0;i<alServiceTypes.size();i++){
			 SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = '" + alServiceTypes.get(i).toString() + "')"
			 		+ " OR";
			}		
			SQL = SQL.substring(0, SQL.length() - 3) + ")";
			
			SQL += " AND ("; 
			for (int i=0;i<alLocations.size();i++){
			 SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + " = '" 
					 + alLocations.get(i).toString() + "')"
			 		 + " OR";
			}		
			SQL = SQL.substring(0, SQL.length() - 3) + ")";
			
			SQL += ") ORDER BY"
					 +" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderSourceDesc + ","
					 +" " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber;
			

		}else if (sReportType.compareTo("Invoices") == 0){
			//LTO 20121023
			//Edited by:SCO 20140807
			SQL = "SELECT"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iOrderSourceID + " AS iOrderSourceID,"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderSourceDesc + " AS sOrderSourceDesc,"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " AS sDocNumber,"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " AS datDocDate,"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName + " AS sCustomerName,"
				+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCreatedByFullName + " AS sCreatedByFullName,"
				+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount + " AS dAmount" + ""
		  + " FROM"
		  	+ "(" + SMTableinvoicedetails.TableName
		  	+ " INNER JOIN"
		  	+ " " + SMTableinvoiceheaders.TableName 
		  	+ " ON"
		  	+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + " = "
		  	+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
		  	+ ") "
		  + " WHERE ("
		  	+ "( " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + sStartDate + "')"
		  	+ " AND"
		  	+ " (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + sEndDate + "')";
			SQL += " AND ("; 
			for (int i=0;i<alServiceTypes.size();i++){
			 SQL += " (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = '" 
					 + alServiceTypes.get(i).toString() + "')" +
			 		" OR";
			}		
			SQL = SQL.substring(0, SQL.length() - 3) + ")";
			
			SQL += " AND ("; 
			for (int i=0;i<alLocations.size();i++){
			 SQL += " (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + " = '" 
					 + alLocations.get(i).toString() + "')"
			 		+ " OR";
			}		
			SQL = SQL.substring(0, SQL.length() - 3) + ")";
			
			SQL += ") ORDER BY"
			  	+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderSourceDesc + ","
			  	+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber;
		}else{
			SQL = "SELECT"
					+ " " + SMTablebids.TableName + "." + SMTablebids.iordersourceid + " AS iOrderSourceID,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.sordersourcedesc + " AS sOrderSourceDesc,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.lid + " AS sDocNumber,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.dattimeoriginationdate + " AS datDocDate,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.scustomername + " AS sCustomerName,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.screatedbyfullname + " AS sCreatedByFullName,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.lcreatedbyuserid + " AS sCreatedByID,"
					+ " " + SMTablebids.TableName + "." + SMTablebids.dapproximateamount + " AS dAmount,"
					+ " FROM"
				  	+ " " + SMTablebids.TableName

				  	+ " WHERE (" 
				  	+ " " + SMTablebids.TableName + "." + SMTablebids.dattimeoriginationdate + " >= '" + sStartDate + "'"
				  	+ " AND"
				  	+ " " + SMTablebids.TableName + "." + SMTablebids.dattimeoriginationdate + " <= '" + sEndDate + "'"
				  	+ ")"
				  	+ " ORDER BY"
				  	+ " " + SMTablebids.TableName + "." + SMTablebids.sordersourcedesc + ","
				  	+ " " + SMTablebids.TableName + "." + SMTablebids.lid;
		}
		
		//System.out.println("[1351014250] Get_Order_Source_Listing_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Items_On_Sales_Order_With_Non_Zero_Qty_SQL(){
	
		SQL = "SELECT DISTINCT" + 
				" " + SMTableorderheaders.sOrderNumber + "," +
				" " + SMTableorderdetails.sItemNumber +
			  " FROM" + 
			  " " + SMTableorderdetails.TableName + ", " +
			  " " + SMTableorderheaders.TableName + 
			  " WHERE" +
			  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " = " +
			  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + 
			  " AND" + 
			  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " <> 0" + 
			  " AND" +
			  " LEFT(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + ", 3) <> 'ELP'" +
			  //NO QUOTES!
			  " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
			  + SMTableorderheaders.ORDERTYPE_QUOTE + ")" +
			  " ORDER BY" + 
			  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber;
			  
		//System.out.println("Get_Items_On_Sales_Order_With_Non_Zero_Qty_SQL = " + SQL);
		return SQL;
	}
	
	public static String Get_Customer_ShipTo_List_SQL(){
		
		SQL = "SELECT" + 
				" " + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sCustomerNumber + "," +
				" " + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sShipToCode + "," +
				" " + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sDescription +
			  " FROM" +
			  	" " + SMTablearcustomershiptos.TableName + 
			  " ORDER BY" +
			  	" " + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sCustomerNumber + "," +
			  	" " + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sShipToCode;
		  
		//System.out.println("Get_Customer_ShipTo_List_SQL = " + SQL);
		return SQL;
	}

	public static String Get_Customer_Header_Info_SQL(String sCustomerNumber){
		
		//System.out.println("Customer Number = " + sCustomerNumber);
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName;
		if (sCustomerNumber != null){
			//ltong why ignore blank customer name? 
			//20091001
			//if (sCustomerNumber.trim().compareTo("") != 0){
				SQL = SQL + " WHERE" + 
					" " + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerNumber + "'";
			//}
		}
		
		//System.out.println("Get_Customer_Header_Info_SQL = " + SQL);
		return SQL;
	}

}
