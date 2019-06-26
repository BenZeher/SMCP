package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableap1099cprscodes;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecklines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTableapvendorstatistics;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTablebkpostedentries;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablereminders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class APACCPACConversion  extends java.lang.Object{

	//private static String sStatus = "";
	
	public APACCPACConversion(){
		
	}
	
	public String convertData(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType,
			int iACCPACAPVersion,
			String sUser,
			boolean bRemoveAllReminders) throws Exception{
		
		String s = "";
		
		try {
			s += reverseDataChanges(cnSMCP, bRemoveAllReminders);
		} catch (Exception e1) {
			throw new Exception("Error [1450702575] reversing any previous data changes - " + e1.getMessage());
		}
		
		//Reset the string:
		s = "";
		try {
			s += processMasterTables(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1449781331] - " + e.getMessage());
		}
		
		try {
			s += processVendorTables(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587331] - " + e.getMessage());
		}
		
		try {
			s += processVendorStatistics(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587332] - " + e.getMessage());
		}
		
		try {
			s += processAPTransactions(cnSMCP, cnACCPAC, iAPDatabaseType, iACCPACAPVersion);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587333] - " + e.getMessage());
		}
		
		try {
			s += processAPTransactionLines(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587334] - " + e.getMessage());
		}
		
		try {
			s += processUpdatingAPTransactionLines(cnSMCP);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587335] - " + e.getMessage());
		}
		
		try {
			s += processAPMatchingLines(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587336] - " + e.getMessage());
		}
		
		try {
			s += processUpdatingApplyFromAPMatchingLines(cnSMCP);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587337] - " + e.getMessage());
		}
		
		try {
			s += processUpdatingApplyToAPMatchingLines(cnSMCP);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587338] - " + e.getMessage());
		}
		
		//OPTIONAL:
		try {
			s += processUpdatingVendorAddresses(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			//Unwind any CRITICAL data changes we made here:
			reverseDataChanges(cnSMCP, bRemoveAllReminders);
			throw new Exception("Error [1526587339] - " + e.getMessage());
		}
		
		return s;
	}
	public String reverseDataChanges(Connection cnSMCP, boolean bRemoveAllReminders) throws Exception{
		String s = "Rolling back critical changes to SMCP data...<BR>";
		//We only remove CRITICAL data changes, i.e., those that might affect processing:

		//Remove any vendors that we added from ACCPAC:
		String SQL = "DELETE FROM " + SMTableicvendors.TableName
			+ " WHERE ("
				+ "(" + SMTableicvendors.iaddedbyapconversion + " != 0)"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449781330] - could not remove ACCPAC-added vendors - " + e.getMessage());
		}
		s+= "Vendors that were added from ACCPAC have been removed.<BR>";
		
		//initialize any fields in the vendors table that we've updated:
		SQL = "UPDATE " + SMTableicvendors.TableName
			+ " SET " + SMTableicvendors.ibankcode + " = 0"
			+ ", " + SMTableicvendors.iapaccountset + " = 0"
			+ ", " + SMTableicvendors.sdefaultexpenseacct + " = ''"
			+ ", " + SMTableicvendors.sdefaultdistributioncode + " = ''"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449856914] - could not initialize updated vendor fields - " + e.getMessage());
		}
		s+= "Vendor fields that were updated from ACCPAC have been re-initialized.<BR>";
		
		//First initialize the ACCPAC Terms Codes on the vendor terms:
		//String sTablename = SMTableicvendorterms.TableName;
		//SQL = "UPDATE " + sTablename
		//	+ " SET " + SMTableicvendorterms.sACCPACtermscode + " = ''"
		//;
		//try {
		//	Statement stmt = cnSMCP.createStatement();
		//	stmt.execute(SQL);
		//} catch (Exception e) {
		//	throw new Exception("Error [1450305134] - could not initialize vendor terms from " + sTablename + " table - " + e.getMessage());
		//}
		
		//Remove any terms which were only added by the ACCPAC conversion:
		//SQL = "DELETE FROM " + sTablename 
		//	+ " WHERE ("
		//		+ "(" + SMTableicvendorterms.iaddedfromACCPAC + " = 1)"
		//	+ ")"
		//;
		//try {
		//	Statement stmt = cnSMCP.createStatement();
		//	stmt.execute(SQL);
		//} catch (Exception e) {
		//	throw new Exception("Error [14503051235] - could not remove ACCPAC-added terms in " + sTablename + " table - " + e.getMessage());
		//}
		
		//Remove any banks which were added by a previous ACCPAC conversion:
		SQL = "DELETE FROM " + SMTablebkbanks.TableName
			+ " WHERE ("
				+ "(" + SMTablebkbanks.iaddedbyapconversion + " != 0)"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450493999] - could not remove ACCPAC-added banks from SMCP - " + e.getMessage());
		}
		
		//Now reset the auto increment for the ID:
		long lMaxID = 0;
		SQL = "select IF(count(*) > 0, MAX(" + SMTablebkbanks.lid + "), 0) FROM " + SMTablebkbanks.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()){
				lMaxID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception ("Error [1450700282] getting MAX value from " + SMTablebkbanks.TableName + " - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + SMTablebkbanks.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450700282] - could not reset auto_increment in " + SMTablebkbanks.TableName + " - " + e.getMessage());
		}
		
		s+= "Banks that were added from ACCPAC have been removed.<BR>";
		
		//Remove vendor statistics:
		SQL = "DELETE FROM " + SMTableapvendorstatistics.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450130304] - could not remove ACCPAC-added statistics - " + e.getMessage());
		}
		s+= "ALL statistics have been removed.<BR>";
		
		//Remove vendor groups:
		SQL = "DELETE FROM " + SMTableapvendorgroups.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450277707] - could not remove ACCPAC-added vendor groups - " + e.getMessage());
		}
		//Now reset the auto increment for the ID:
		lMaxID = 0;
		SQL = "select IF(count(*) > 0, MAX(" + SMTableapvendorgroups.lid + "), 0) FROM " + SMTableapvendorgroups.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()){
				lMaxID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception ("Error [1450700292] getting MAX value from " + SMTableapvendorgroups.TableName + " - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + SMTableapvendorgroups.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450700293] - could not reset auto_increment in " + SMTableapvendorgroups.TableName + " - " + e.getMessage());
		}
		s+= "ALL vendor groups have been removed.<BR>";
		
		//Remove vendor remit to locations:
		SQL = "DELETE FROM " + SMTableapvendorremittolocations.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450472573] - could not remove ACCPAC-added vendor remit to locations - " + e.getMessage());
		}
		s+= "ALL vendor remit to locations have been removed.<BR>";
		
		//Remove account sets:
		SQL = "DELETE FROM " + SMTableapaccountsets.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450494199] - could not remove all the account sets - " + e.getMessage());
		}
		//Now reset the auto increment for the ID:
		lMaxID = 0;
		SQL = "select IF(count(*) > 0, MAX(" + SMTableapaccountsets.lid + "), 0) FROM " + SMTableapaccountsets.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()){
				lMaxID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception ("Error [14507002924] getting MAX value from " + SMTableapaccountsets.TableName + " - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + SMTableapaccountsets.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450700295] - could not reset auto_increment in " + SMTableapaccountsets.TableName + " - " + e.getMessage());
		}
		s+= "ALL account sets have been removed.<BR>";
		
		//Remove distribution codes:
		SQL = "DELETE FROM " + SMTableapdistributioncodes.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450494200] - could not remove all the distribution codes - " + e.getMessage());
		}
		//Now reset the auto increment for the ID:
		lMaxID = 0;
		SQL = "select IF(count(*) > 0, MAX(" + SMTableapdistributioncodes.lid + "), 0) FROM " + SMTableapdistributioncodes.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()){
				lMaxID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception ("Error [14507002926] getting MAX value from " + SMTableapdistributioncodes.TableName + " - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + SMTableapdistributioncodes.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450700297] - could not reset auto_increment in " + SMTableapdistributioncodes.TableName + " - " + e.getMessage());
		}
		s+= "ALL distribution codes have been removed.<BR>";
		
		//Remove 1099/CPRS codes:
		SQL = "DELETE FROM " + SMTableap1099cprscodes.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450494201] - could not remove all the 1099/CPRS codes - " + e.getMessage());
		}
		
		//Now reset the auto increment for the ID:
		lMaxID = 0;
		SQL = "select IF(count(*) > 0, MAX(" + SMTableap1099cprscodes.lid + "), 0) FROM " + SMTableap1099cprscodes.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()){
				lMaxID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception ("Error [1450700787] getting MAX value from " + SMTableap1099cprscodes.TableName + " with SQL: '" + SQL + "' - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + SMTableap1099cprscodes.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1450700788] - could not reset auto_increment in " + SMTableap1099cprscodes.TableName + " - " + e.getMessage());
		}
		s+= "ALL 1099/CPRS codes have been removed.<BR>";
		
		
		if (bRemoveAllReminders){
			//Remove any SM Schedules:
			SQL = "DELETE FROM " + SMTablereminders.TableName;
			try {
				Statement stmt = cnSMCP.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1450876837] - could not remove all the schedules - " + e.getMessage());
			}
			
			//Now reset the auto increment for the ID:
			lMaxID = 0;
			SQL = "select IF(count(*) > 0, MAX(" + SMTablereminders.lid + "), 0) FROM " + SMTablereminders.TableName;
			try {
				Statement stmt = cnSMCP.createStatement();
				ResultSet rs = stmt.executeQuery(SQL);
				if (rs.next()){
					lMaxID = rs.getLong(1);
				}
				rs.close();
			} catch (Exception e1) {
				throw new Exception ("Error [1450876838] getting MAX value from " + SMTablereminders.TableName + " with SQL: '" + SQL + "' - " + e1.getMessage());
			}
			
			SQL = "ALTER TABLE " + SMTablereminders.TableName + " AUTO_INCREMENT = " + Long.toString(lMaxID + 1);
			try {
				Statement stmt = cnSMCP.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1450876839] - could not reset auto_increment in " + SMTablereminders.TableName + " - " + e.getMessage());
			}
			s+= "ALL SM Schedules have been removed.<BR>";
		}
		
		
		//Remove all batches, entries, and lines:
		SQL = "TRUNCATE TABLE " + SMTableapbatches.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238660] - could not truncate ap batches - " + e.getMessage());
		}
		s+= "ALL AP Batches have been truncated.<BR>";
		
		SQL = "TRUNCATE TABLE " + SMTableapbatchentries.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238661] - could not truncate ap batch entries - " + e.getMessage());
		}
		s+= "ALL AP Batch Entries have been truncated.<BR>";
		
		SQL = "TRUNCATE TABLE " + SMTableapbatchentrylines.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238662] - could not truncate ap batch entry lines - " + e.getMessage());
		}
		s+= "ALL AP Batch Entry Lines have been truncated.<BR>";
		
		try {
			s += truncateAPTransactionTables(cnSMCP);
		} catch (Exception e) {
			throw new Exception("Error [1496411125] - could not truncate ap batch entry lines - " + e.getMessage());
		}
		
		//Remove all checks and lines:
		SQL = "TRUNCATE TABLE " + SMTableapchecks.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1506096752] - could not truncate ap checks - " + e.getMessage());
		}
		s+= "ALL AP Checks have been truncated.<BR>";
		
		SQL = "TRUNCATE TABLE " + SMTableapchecklines.TableName;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1506096753] - could not truncate ap check lines - " + e.getMessage());
		}
		s+= "ALL AP Check lines have been truncated.<BR>";
		
		
		//Remove all the GL export records for AP:
		SQL = "DELETE FROM " + SMTableglexportheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableglexportheaders.ssourceledger + " = 'AP')"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1497357964] - could not remove GL export headers for AP batches - " + e.getMessage());
		}
		s+= "ALL GL export header records for AP batches have been deleted.<BR>";
		
		SQL = "DELETE FROM " + SMTableglexportdetails.TableName
			+ " WHERE ("
				+ "(" + SMTableglexportdetails.sdetailsourceledger + " = 'AP')"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1497357965] - could not remove GL export details for AP batches - " + e.getMessage());
		}
		s+= "ALL GL export detail records for AP batches have been deleted.<BR>";
		
		//Remove any bank account entries that were related to AP:
		SQL = "DELETE FROM " + SMTablebkaccountentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkaccountentries.ssourcemodule + " = '" + SMModuleTypes.AP + "')"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1497357165] - could not remove unposted bank account entries coming from AP - " + e.getMessage());
		}
		s+= "ALL unposted bank account entries coming from AP payments have been deleted.<BR>";
		
		//Remove any posted bank account entries that were related to AP:
		SQL = "DELETE FROM " + SMTablebkpostedentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkpostedentries.ssourcemodule + " = '" + SMModuleTypes.AP + "')"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1497357166] - could not remove posted bank account entries coming from AP - " + e.getMessage());
		}
		s+= "ALL posted bank account entries coming from AP payments have been deleted.<BR>";
		
		return s;
	}
	
	public String truncateAPTransactionTables(Connection connSMCP) throws Exception{
		String s = "";
		
		//Remove all transactions, matching lines, and transaction lines:
		String SQL = "TRUNCATE TABLE " + SMTableaptransactionlines.TableName;
		try {
			Statement stmt = connSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238663] - could not truncate ap transaction lines - " + e.getMessage());
		}
		s+= "ALL AP Transaction Lines have been truncated.<BR>";
		
		SQL = "TRUNCATE TABLE " + SMTableaptransactions.TableName;
		try {
			Statement stmt = connSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238664] - could not truncate ap transactions - " + e.getMessage());
		}
		s+= "ALL AP Transactions have been truncated.<BR>";
		
		SQL = "TRUNCATE TABLE " + SMTableapmatchinglines.TableName;
		try {
			Statement stmt = connSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496238665] - could not truncate ap matching lines - " + e.getMessage());
		}
		
		s+= "ALL AP Matching Lines have been truncated.<BR>";		
		return s;
	}
	public String processMasterTables(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//Populate AP Account Sets:
		try {
			sStatus += convertAccountSets(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Populate AP Distribution codes:
		try {
			sStatus += convertDistributionCodes(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Populate AP 1099/CPRS Reporting codes:
		try {
			sStatus += convert1099CPRSCodes(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Populate any banks that aren't in SMCP:
		try {
			sStatus += processBanks(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
		
	}
	public String processVendorTables(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//Update vendor groups:
		try {
			sStatus += convertVendorGroups(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Populate vendor remit to locations:
		try {
			sStatus += convertVendorRemitToLocations(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Update vendor records:
		try {
			sStatus += processVendors(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
		
	}
	public String processUpdatingVendorAddresses(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//Update vendor address info:
		try {
			sStatus += processUpdateVendorAddressesFromACCPAC(cnSMCP, cnACCPAC, iAPDatabaseType, sUser);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
		
	}
	public String processVendorStatistics(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//Bring vendor statistics over:
		try {
			sStatus += convertStatistics(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
		
	}
	
	public String processAPTransactions(
		Connection cnSMCP,
		Connection cnACCPAC,
		int iAPDatabaseType,
		int iACCPACAPVersion) throws Exception{

		String sStatus = "";

		//Populate transactions
		try {
			sStatus += convertTransactions(cnSMCP, cnACCPAC, iAPDatabaseType, iACCPACAPVersion);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
	}
	
	public String processAPTransactionLines(
		Connection cnSMCP,
		Connection cnACCPAC,
		int iAPDatabaseType) throws Exception{

		String sStatus = "";

		//Populate transaction lines
		try {
			sStatus += insertTransactionLines(cnSMCP, cnACCPAC, iAPDatabaseType);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return sStatus;
	}
	
	public String processUpdatingAPTransactionLines(
			Connection cnSMCP) throws Exception{

			String sStatus = "";

			//Update transaction lines
			try {
				sStatus += updateTransactionLines(cnSMCP);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			
			return sStatus;
		}

	public String processAPMatchingLines(
			Connection cnSMCP,
			Connection cnACCPAC,
			int iAPDatabaseType) throws Exception{

			String sStatus = "";

			//Populate matching lines
			try {
				sStatus += insertMatchingLines(cnSMCP, cnACCPAC, iAPDatabaseType);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			
			return sStatus;
		}
	
	public String processUpdatingApplyFromAPMatchingLines(
			Connection cnSMCP) throws Exception{

			String sStatus = "";

			//Populate matching lines
			try {
				sStatus += updateApplyFromMatchingLines(cnSMCP);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			
			return sStatus;
		}
	
	public String processUpdatingApplyToAPMatchingLines(
			Connection cnSMCP) throws Exception{

			String sStatus = "";

			//Populate matching lines
			try {
				sStatus += updateApplyToMatchingLines(cnSMCP);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			
			return sStatus;
		}

	
	public static String convertTransactions(
			Connection cnSMCP,
			Connection cnACCPAC,
			int iAPDatabaseType,
			int iACCPACAPVersion) throws Exception{
		
		String s = "";
		/*
		IDTRXTYPE 12, TXTTRXTYPE 1 = doc type INVOICE
		IDTRXTYPE 32, TXTTRXTYPE 3 = doc type applied credit to, credit note
		IDTRXTYPE 13, TXTTRXTYPE 1 = invoice
		IDTRXTYPE 22, TXTTRXTYPE 2 = applied debit to
		IDTRXTYPE 50, TXTTRXTYPE 10 = pre-pay
		IDTRXTYPE 51, TXTTRXTYPE 11 = PAYMENT
		*/
		//First, populate the aptransactions table:
		// APOBL contains the AP transactions:
		String sTablename = SMTableaptransactions.TableName;
		
		String SQL = "";
		if (iACCPACAPVersion > SMTableapoptions.AP_VERSION_ACCPAC54){
			SQL = "SELECT"
				+ " APOBL.AMTDUEHC"
				+ ", APOBL.AMTDISCHC"
				+ ", APOBL.AMTINVCHC"
				+ ", APOBL.AMTDISCHC"
				+ ", APOBL.DATEDISC"
				+ ", APOBL.DATEINVC"
				+ ", APOBL.DATEINVCDU"
				
				
				//+ ", CASE APOBL.IDTRXTYPE"
				//+ "     WHEN 12 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE
				//+ "     WHEN 32 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE
				//+ "     WHEN 13 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE
				//+ "     WHEN 22 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE
				//+ "     WHEN 50 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT
				//+ "     WHEN 51 THEN " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT
				//+ "     ELSE -1"
				//+ " END AS DOCTYPE"
				
				
				+ ", APOBL.IDTRXTYPE"
				+ ", APOBL.IDACCTSET"
				+ ", APOBL.DESCINVC"
				+ ", APOBL.IDINVC"
				+ ", APOBL.IDVEND"
				+ ", APOBL.CNTBTCH"
				+ ", APOBL.CNTITEM"
				+ ", APOBL.SWPYSTTS"
				+ ", APRAS.IDACCTAP"
				+ ", APOBL.LONGSERIAL"
				+ ", APOBL.IDRMIT"
				+ " FROM APOBL"
				+ " LEFT JOIN APRAS"
				+ " ON APOBL.IDACCTSET=APRAS.ACCTSET"
				;
		}else{
			SQL = "SELECT"
				+ " APOBL.AMTDUEHC"
				+ ", APOBL.AMTDISCHC"
				+ ", APOBL.AMTINVCHC"
				+ ", APOBL.AMTDISCHC"
				+ ", APOBL.DATEDISC"
				+ ", APOBL.DATEINVC"
				+ ", APOBL.DATEINVCDU"
				+ ", APOBL.IDTRXTYPE"
				+ ", APOBL.DESCINVC"
				+ ", APOBL.IDINVC"
				+ ", APOBL.IDVEND"
				+ ", APOBL.CNTBTCH"
				+ ", APOBL.CNTITEM"
				+ ", APOBL.SWPYSTTS"
				+ ", APOBL.LONGSERIAL"
				+ ", APOBL.IDRMIT"
				+ " FROM APOBL"
				;
		}

		ResultSet rsTransactions;
		try {
			Statement stmtACCPAC = cnACCPAC.createStatement();
			rsTransactions = stmtACCPAC.executeQuery(SQL);
		} catch (Exception e2) {
			throw new Exception("Error [1526501645] reading AP transactions with SQL: " + SQL + " - " + e2.getMessage() + ".");
		}
		int iCounter = 0;
		
		//Turn off auto commit in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("SET autocommit=0");
		} catch (Exception e) {
			rsTransactions.close();
			throw new Exception("Error [1497903946] - could not set AUTOCOMMIT to '0' - " + e.getMessage());
		}
		
		while (rsTransactions.next()){
			long lPONumber = 0;
			try {
				lPONumber = Long.parseLong(rsTransactions.getString("IDPONBR"));
			} catch (Exception e1) {
				lPONumber = 0;
			}
			
			long lSalesOrderNumber = 0;
			try {
				lSalesOrderNumber = Long.parseLong(rsTransactions.getString("IDORDERNBR"));
			} catch (Exception e1) {
				lSalesOrderNumber = 0;
			}
			String sControlAcct = "";

			//This field wasn't used in ACCPAC version 5.4:
			if (iACCPACAPVersion > SMTableapoptions.AP_VERSION_ACCPAC54){
				if (rsTransactions.getString("IDACCTAP") != null){
					sControlAcct = rsTransactions.getString("IDACCTAP").trim().replace("-", "");
				}
			}
			//Set the doc type here:
			int iDocType = 0;
			
			switch (rsTransactions.getInt("APOBL.IDTRXTYPE")){
				case 12:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE;
					break;
				case 32:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE;
					break;
				case 13:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE;
					break;
				case 22:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE;
					break;
				case 50:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT;
					break;
				case 51:
					iDocType = SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT;
					break;
				default:
					iDocType = -1;
			}
			
			String sCheckNumber = "";
			if (
				(iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)
				|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)
				|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)
			){
				if (rsTransactions.getString("IDRMIT").compareToIgnoreCase("") != 0){
					long lCheckNumber;
					try {
						lCheckNumber = Long.parseLong(rsTransactions.getString("IDRMIT").trim());
						sCheckNumber = Long.toString(lCheckNumber);
					} catch (Exception e) {
						sCheckNumber = rsTransactions.getString("IDRMIT").trim();
					}
				}
			}
			
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableaptransactions.bdcurrentamt
				+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
				+ ", " + SMTableaptransactions.bdoriginalamt
				+ ", " + SMTableaptransactions.bdoriginaldiscountavailable
				+ ", " + SMTableaptransactions.datdiscountdate
				+ ", " + SMTableaptransactions.datdocdate
				+ ", " + SMTableaptransactions.datduedate
				+ ", " + SMTableaptransactions.idoctype
				+ ", " + SMTableaptransactions.ionhold
				+ ", " + SMTableaptransactions.lapplytopurchaseorderid
				+ ", " + SMTableaptransactions.lapplytosalesorderid
				+ ", " + SMTableaptransactions.loriginalbatchnumber
				+ ", " + SMTableaptransactions.loriginalentrynumber
				+ ", " + SMTableaptransactions.sapplytoinvoicenumber
				+ ", " + SMTableaptransactions.schecknumber
				+ ", " + SMTableaptransactions.scontrolacct
				+ ", " + SMTableaptransactions.sdocdescription
				+ ", " + SMTableaptransactions.sdocnumber
				+ ", " + SMTableaptransactions.svendor
				+ ") VALUES ("
				+ " " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableaptransactions.bdcurrentamtScale, rsTransactions.getBigDecimal("AMTDUEHC")).replace(",", "") //Current Amt
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableaptransactions.bdcurrentamtScale, rsTransactions.getBigDecimal("AMTDISCHC")).replace(",", "") //Current disc available
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableaptransactions.bdcurrentamtScale, rsTransactions.getBigDecimal("AMTINVCHC")).replace(",", "") //Original Amt
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableaptransactions.bdcurrentamtScale, rsTransactions.getBigDecimal("AMTDISCHC")).replace(",", "") // Original discount available
				+ ", '" + convertACCPACLongDateToString(rsTransactions.getLong("DATEDISC"), false) + "'" //Discount date
				+ ", '" + convertACCPACLongDateToString(rsTransactions.getLong("DATEINVC"), false) + "'" //Doc date
				+ ", '" + convertACCPACLongDateToString(rsTransactions.getLong("DATEINVCDU"), false) + "'" //Due date
				+ ", " + Integer.toString(iDocType) //Doc type
				+ ", " + Integer.toString(rsTransactions.getInt("SWPYSTTS")) //on hold
				+ ", " + Long.toString(lPONumber) //PO number
				+ ", " + Long.toString(lSalesOrderNumber) //Order number
				+ ", " + Integer.toString(rsTransactions.getInt("CNTBTCH")) //ACCPAC Batch number
				+ ", "  + Integer.toString(rsTransactions.getInt("CNTITEM")) //Entry number
				+ ", ''" //Apply to invoice number
				+ ", '" + FormatSQLStatement(sCheckNumber) + "'" //Check number
				+ ", '" + FormatSQLStatement(sControlAcct) + "'" //Control acct
				+ ", '" + FormatSQLStatement(rsTransactions.getString("DESCINVC").trim()) + "'"	//Desc			
				+ ", '" + FormatSQLStatement(rsTransactions.getString("IDINVC").trim()) + "'"	//Doc number
				+ ", '" + FormatSQLStatement(rsTransactions.getString("IDVEND").trim()) + "'"	//Vendor
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsTransactions.close();
				throw new Exception("Error [1496281534] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
			if ((iCounter % 500) == 0){
				System.out.println("[1557330503] - AP transaction counter = " + Integer.toString(iCounter)
					+ ", SQLInsert = '" + SQLInsert + "'.");
			}
		}
		rsTransactions.close();
		
		//Turn auto commit back on in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			throw new Exception("Error [1497903947] - could not COMMIT inserts - " + e.getMessage());
		}
		
		//If it's ACCPAC version 5.4, we need to populate the control account based on the vendor:
		if (iACCPACAPVersion == SMTableapoptions.AP_VERSION_ACCPAC54){
			String SQLUpdate = "UPDATE " + SMTableaptransactions.TableName
				+ " LEFT JOIN " + SMTableicvendors.TableName
				+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
				+ " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
				+ " LEFT JOIN " + SMTableapaccountsets.TableName
				+ " ON " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
				+ " = " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset
				+ " SET " + SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct + " = " 
					+ SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
			;
			try {
				Statement stmtUpdate = cnSMCP.createStatement();
				stmtUpdate.execute(SQLUpdate);
			} catch (Exception e) {
				rsTransactions.close();
				throw new Exception("Error [1526579326] - could not update control accounts in " + sTablename + " table - " + e.getMessage());
			}
		}
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP transaction records to " + sTablename + "<BR>";

		return s;
	}
	public static String insertTransactionLines(
			Connection cnSMCP,
			Connection cnACCPAC,
			int iAPDatabaseType) throws Exception{
		
		String s = "";
		
		//Now update the transaction lines:
		String sTablename = SMTableaptransactionlines.TableName;
		//First we need to add a couple fields temporarily:
		String SQL = "ALTER TABLE " + SMTableaptransactionlines.TableName
			+ " ADD COLUMN svendor varchar(" + Integer.toOctalString(SMTableicvendors.svendoracctLength) + ") NOT NULL DEFAULT ''"
			+ ", ADD COLUMN sdocnumber varchar(" + SMTableaptransactions.sdocnumberlength + ") NOT NULL DEFAULT '' "
			+ ", ADD KEY vendor_key(svendor)"
			+ ", ADD KEY docnumber_key(sdocnumber)"
		;
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute(SQL);
		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate column name")){
				//Let this go, since it means the column is already there.
			}else{
				throw new Exception("Error [1496350403] - could not add temporary fields to AP transaction lines in " + sTablename + " table - " + e.getMessage());
			}
		}
		
		//APOBLJ contains invoice lines:
		SQL = "SELECT"
			+ " APOBLJ.AMTINVCHC"
			+ ", APOBLJ.CNTLINE"
			+ ", APOBLJ.IDGLACCT"
			+ ", APOBLJ.IDVEND"
			+ ", APOBLJ.IDINVC"
			+ " FROM APOBLJ"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsTransactionLines = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		
		//Turn off auto commit in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("SET autocommit=0");
		} catch (Exception e) {
			rsTransactionLines.close();
			throw new Exception("Error [1497903947] - could not set AUTOCOMMIT to '0' - " + e.getMessage());
		}
		
		while (rsTransactionLines.next()){

			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableaptransactionlines.bdamount
				+ ", " + SMTableaptransactionlines.lapplytodocid
				+ ", " + SMTableaptransactionlines.loriginalbatchnumber
				+ ", " + SMTableaptransactionlines.loriginalentrynumber
				+ ", " + SMTableaptransactionlines.loriginallinenumber
				+ ", " + SMTableaptransactionlines.lpoheaderid
				+ ", " + SMTableaptransactionlines.lporeceiptlineid
				+ ", " + SMTableaptransactionlines.lreceiptheaderid
				+ ", " + SMTableaptransactionlines.ltransactionheaderid
				+ ", " + SMTableaptransactionlines.sapplytodocnumber
				+ ", " + SMTableaptransactionlines.scomment
				+ ", " + SMTableaptransactionlines.sdescription
				+ ", " + SMTableaptransactionlines.sdistributionacct
				+ ", " + SMTableaptransactionlines.sdistributioncodename
				+ ", " + "svendor"
				+ ", " + "sdocnumber"
				+ ") VALUES ("
				+ " " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableaptransactions.bdcurrentamtScale, rsTransactionLines.getBigDecimal("AMTINVCHC")).replace(",", "") //Amt
				+ ", 0" //apply-to-doc ID
				+ ", 0" //Original batch number
				+ ", 0" //Original entry number
				+ ", " + rsTransactionLines.getInt("CNTLINE")
				+ ", 0" //PO header ID
				+ ", 0" //POReceipt line ID
				+ ", 0" //Receipt header ID
				+ ", 0" //Transaction header ID - we'll update this in the next SQL command
				+ ", ''"  //Apply to doc number
				+ ", '(Converted from ACCPAC)'" //Comment
				+ ", ''" //Description
				+ ", '" + FormatSQLStatement(rsTransactionLines.getString("IDGLACCT").trim()).replace("-", "") + "'"  //Dist acct
				+ ", ''"  //Dist code name
				+ ", '" + FormatSQLStatement(rsTransactionLines.getString("IDVEND").trim()).replace("-", "") + "'"  //Vendor
				+ ", '" + FormatSQLStatement(rsTransactionLines.getString("IDINVC").trim()).replace("-", "") + "'"  //Vendor
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsTransactionLines.close();
				throw new Exception("Error [1496350502] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
			if ((iCounter % 500) == 0){
				System.out.println("[1557330504] - AP transaction line counter = " + Integer.toString(iCounter)
					+ ", SQLInsert = '" + SQLInsert + "'.");
			}
		}
		rsTransactionLines.close();
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP transaction line records to " + sTablename + "<BR>";
		
		//Turn auto commit back on in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			throw new Exception("Error [1497903948] - could not COMMIT inserts - " + e.getMessage());
		}
		
		return s;
	}
	
	public static String updateTransactionLines(Connection cnSMCP) throws Exception{
		
		//Now we need to link the invoice transaction lines to the transactions, to populate a few more fields:
		String SQL = "UPDATE " + SMTableaptransactionlines.TableName
			+ " LEFT JOIN " + SMTableaptransactions.TableName
			+ " ON (" + SMTableaptransactionlines.TableName +".svendor" + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + ")"
			+ " AND (" + SMTableaptransactionlines.TableName +".sdocnumber" + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ")"
			+ " SET " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lpoheaderid + " = "
				+ " IF(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid
				+ " IS NULL, 0, " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid + ")"
			
			+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid + "=" 
				+ " IF(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
				+ " IS NULL, 0, " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + ")"
			
			+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sapplytodocnumber + "=" 
				+ " IF(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sapplytoinvoicenumber
				+ " IS NULL, '', " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sapplytoinvoicenumber + ")"
		;
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496350503] - could not update invoice transaction lines in " + SMTableaptransactionlines.TableName + " table - " + e.getMessage());
		}
		
		long lRowsUpdated = 0L;
		ResultSet rs = clsDatabaseFunctions.openResultSet("SELECT ROW_COUNT()", cnSMCP);
		if (rs.next()){
			lRowsUpdated = rs.getLong(1);
		}
		rs.close();
		
		//Remove the temporary fields:
		SQL = "ALTER TABLE " + SMTableaptransactionlines.TableName
			+ " DROP COLUMN svendor"
			+ ", DROP COLUMN sdocnumber"
		;
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1496350504] - could not drop temporary transaction lines fields in " + SMTableaptransactionlines.TableName + " table - " + e.getMessage());
		}
		
		return "Number of AP transaction lines updated: " + Long.toString(lRowsUpdated) + ".";
	}
	
	public static String insertMatchingLines(
		Connection cnSMCP,
		Connection cnACCPAC,
		int iAPDatabaseType) throws Exception{
		
		String s = "";
		
		//Update the AP Matching lines:
		//APOBP contains matching lines:
		String sTablename = SMTableapmatchinglines.TableName;
		String SQL = "SELECT"
			+ " APOBP.AMTPAYMHC"
			+ ", APOBP.DATEBUS"
			+ ", APOBP.IDMEMOXREF"
			+ ", APOBP.IDVEND"
			+ ", APOBP.IDINVC"
			+ " FROM APOBP"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsMatchingLines = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		
		//Turn off auto commit in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("SET autocommit=0");
		} catch (Exception e) {
			rsMatchingLines.close();
			throw new Exception("Error [1497903949] - could not set AUTOCOMMIT to '0' - " + e.getMessage());
		}
		
		while (rsMatchingLines.next()){

			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableapmatchinglines.bdappliedamount
				+ ", " + SMTableapmatchinglines.bddiscountappliedamount
				+ ", " + SMTableapmatchinglines.dattransactiondate
				+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
				+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
				+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
				+ ", " + SMTableapmatchinglines.sappliedtodocnumber
				+ ", " + SMTableapmatchinglines.sdescription
				+ ", " + SMTableapmatchinglines.svendor
				+ ") VALUES ("
				+ " " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapmatchinglines.bdappliedamountScale, rsMatchingLines.getBigDecimal("AMTPAYMHC").negate()).replace(",", "") //NEGATED Amt applied
				+ ", 0.00" //Discount applied
				+ ", '" + convertACCPACLongDateToString(rsMatchingLines.getLong("DATEBUS"), false) + "'" //Transaction date
				+ ", 0" //Transaction applied from - update next
				+ ", 0" //Transaction applied to - update next
				+ ", '" + FormatSQLStatement(rsMatchingLines.getString("IDMEMOXREF").trim()).replace("-", "") + "'" //Transaction applied from doc number
				+ ", '" + FormatSQLStatement(rsMatchingLines.getString("IDINVC").trim()).replace("-", "") + "'" //Transaction applied to doc number
				+ ", '(Converted from ACCPAC)'"
				+ ", '" + FormatSQLStatement(rsMatchingLines.getString("IDVEND").trim()).replace("-", "") + "'" //Transaction applied to doc number
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsMatchingLines.close();
				throw new Exception("Error [1496350505] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rsMatchingLines.close();
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP matching line records to " + sTablename + "<BR>";
		
		//Turn auto commit back on in the target table:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			throw new Exception("Error [1497903950] - could not COMMIT inserts - " + e.getMessage());
		}
		
		return s;
	}
	
	public static String updateApplyFromMatchingLines(
			Connection cnSMCP) throws Exception{
			
			//Turn off auto commit in the target table:
		/*
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute("SET autocommit=0");
			} catch (Exception e) {
				throw new Exception("Error [1497903951] - could not set AUTOCOMMIT to '0' - " + e.getMessage());
			}
		*/
			//Now update the apply from ID in the matching lines:
			String SQL = "UPDATE " + SMTableapmatchinglines.TableName
				+ " LEFT JOIN " + SMTableaptransactions.TableName
				+ " ON (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ")"
				+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + ")"
				+ " SET " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
				+ " WHERE ("
					+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + " IS NOT NULL)"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1496350506] - could not apply from doc ID in " + SMTableapmatchinglines.TableName + " table - " + e.getMessage());
			}
			
			long lRowsUpdated = 0L;
			ResultSet rs = clsDatabaseFunctions.openResultSet("SELECT ROW_COUNT()", cnSMCP);
			if (rs.next()){
				lRowsUpdated = rs.getLong(1);
			}
			rs.close();
			
			/*
			//Turn auto commit back on in the target table:
			try {
				Statement stmtUpdate = cnSMCP.createStatement();
				stmtUpdate.execute("COMMIT");
			} catch (Exception e) {
				throw new Exception("Error [1497903952] - could not COMMIT updates - " + e.getMessage());
			}
			*/

			return "Number of AP APPLY-FROM matching lines updated: " + Long.toString(lRowsUpdated) + ".";
		}
	
	public static String updateApplyToMatchingLines(
			Connection cnSMCP) throws Exception{
			
			//Turn off auto commit in the target table:
			/*
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute("SET autocommit=0");
			} catch (Exception e) {
				throw new Exception("Error [1497903953] - could not set AUTOCOMMIT to '0' - " + e.getMessage());
			}
			*/
			//Now update the apply to ID in the matching lines:
			String SQL = "UPDATE " + SMTableapmatchinglines.TableName
				+ " LEFT JOIN " + SMTableaptransactions.TableName
				+ " ON (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedtodocnumber + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ")"
				+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + ")"
				+ " SET " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedtoid + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
				+ " WHERE ("
					+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + " IS NOT NULL)"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1496350507] - could not apply to doc ID in " + SMTableapmatchinglines.TableName + " table - " + e.getMessage());
			}
			
			long lRowsUpdated = 0L;
			ResultSet rs = clsDatabaseFunctions.openResultSet("SELECT ROW_COUNT()", cnSMCP);
			if (rs.next()){
				lRowsUpdated = rs.getLong(1);
			}
			rs.close();
			
			//Turn auto commit back on in the target table:
			/*
			try {
				Statement stmtUpdate = cnSMCP.createStatement();
				stmtUpdate.execute("COMMIT");
			} catch (Exception e) {
				throw new Exception("Error [1497903954] - could not COMMIT updates - " + e.getMessage());
			}
			*/
			
			return "Number of AP APPLY-TO matching lines updated: " + Long.toString(lRowsUpdated) + ".";
		}
	
	private static String convertAccountSets(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{

		String s = "";
		String sTablename = SMTableapaccountsets.TableName;
		String SQL = "SELECT * FROM APRAS";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsAcctSets = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsAcctSets.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableapaccountsets.sacctsetname
				+ ", " + SMTableapaccountsets.sdescription
				+ ", " + SMTableapaccountsets.spayablescontrolacct
				+ ", " + SMTableapaccountsets.spurchasediscountacct
				+ ", " + SMTableapaccountsets.sprepaymentacct
				+ ") VALUES ("
				+ "'" + FormatSQLStatement(rsAcctSets.getString("ACCTSET").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("TEXTDESC").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("IDACCTAP").trim()).replace("-", "") + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("DISCACCT").trim()).replace("-", "") + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("PPAYACCT").trim()).replace("-", "") + "'"
				+ ")"
				+ " ON DUPLICATE KEY UPDATE"
				+ " " + SMTableapaccountsets.sdescription + " = '" + FormatSQLStatement(rsAcctSets.getString("TEXTDESC").trim()) + "'"
				+ ", " + SMTableapaccountsets.spayablescontrolacct + " = '" + FormatSQLStatement(rsAcctSets.getString("IDACCTAP").trim()) + "'"
				+ ", " + SMTableapaccountsets.spurchasediscountacct + " = '" + FormatSQLStatement(rsAcctSets.getString("DISCACCT").trim()) + "'"
				+ ", " + SMTableapaccountsets.sprepaymentacct + " = '" + FormatSQLStatement(rsAcctSets.getString("PPAYACCT").trim()) + "'"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsAcctSets.close();
				throw new Exception("Error [1449264977] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rsAcctSets.close();
		
		//Confirm that all the accounts are really in the SMCP GL...
		SQL = "SELECT * FROM " + SMTableapaccountsets.TableName;
		Statement stmtacctsets = cnSMCP.createStatement();
		ResultSet rsacctsets = stmtacctsets.executeQuery(SQL);
		while (rsacctsets.next()){
			//Check payables account:
			SQL = "SELECT * FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.sAcctID + " = '" + rsacctsets.getString(SMTableapaccountsets.spayablescontrolacct) + "')"
				+ ")"
			;
			try {
				Statement stmt1 = cnSMCP.createStatement();
				ResultSet rsPayables = stmt1.executeQuery(SQL);
				if (!rsPayables.next()){
					s += "Account set '" + rsacctsets.getString(SMTableapaccountsets.sacctsetname) 
						+ "' references AP Payables Account '" + rsacctsets.getString(SMTableapaccountsets.spayablescontrolacct) + "'"
						+ " which is not in the GL Accounts.<BR>";
				}
				rsPayables.close();
			} catch (Exception e) {
				throw new Exception("Error [1449526728] reading payables control account from GL - " + e.getMessage());
			}
			
			//Check prepayment account:
			SQL = "SELECT * FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.sAcctID + " = '" + rsacctsets.getString(SMTableapaccountsets.sprepaymentacct) + "')"
				+ ")"
			;
			try {
				Statement stmt1 = cnSMCP.createStatement();
				ResultSet rsPayables = stmt1.executeQuery(SQL);
				if (!rsPayables.next()){
					s += "Account set '" + rsacctsets.getString(SMTableapaccountsets.sacctsetname) 
						+ "' references AP Prepayment Account '" + rsacctsets.getString(SMTableapaccountsets.sprepaymentacct) + "'"
						+ " which is not in the GL Accounts.<BR>";
				}
				rsPayables.close();
			} catch (Exception e) {
				throw new Exception("Error [1449526729] reading prepayment account from GL - " + e.getMessage());
			}
			
			//Check discount account:
			SQL = "SELECT * FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.sAcctID + " = '" + rsacctsets.getString(SMTableapaccountsets.spurchasediscountacct) + "')"
				+ ")"
			;
			try {
				Statement stmt1 = cnSMCP.createStatement();
				ResultSet rsPayables = stmt1.executeQuery(SQL);
				if (!rsPayables.next()){
					s += "Account set '" + rsacctsets.getString(SMTableapaccountsets.sacctsetname) 
						+ "' references AP Discount Account '" + rsacctsets.getString(SMTableapaccountsets.spurchasediscountacct) + "'"
						+ " which is not in the GL Accounts.<BR>";
				}
				rsPayables.close();
			} catch (Exception e) {
				throw new Exception("Error [1449526730] reading Discount account from GL - " + e.getMessage());
			}
		}
		rsacctsets.close();
		//pwOut.println("<BR>Added " + Integer.toString(iCounter) + " AP account set records to " + sTablename + "<BR>");
		s +=  "<BR>Added/updated " + Integer.toString(iCounter) + " AP account set records to " + sTablename + "<BR>";
		return s;
	}
	private static String convertDistributionCodes(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{
		
		//First delete the distribution code records:
		String sTablename = SMTableapdistributioncodes.TableName;
		String s = "";
		String SQL = "SELECT * FROM APRDC";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsAcctSets = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsAcctSets.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableapdistributioncodes.sdistcodename
				+ ", " + SMTableapdistributioncodes.sdescription
				+ ", " + SMTableapdistributioncodes.sglacct
				+ ", " + SMTableapdistributioncodes.idiscountable
				+ ") VALUES ("
				+ "'" + FormatSQLStatement(rsAcctSets.getString("DISTID").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("TEXTDESC").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsAcctSets.getString("IDGLACCT").trim()).replace("-", "") + "'"
				+ ", " + Integer.toString(rsAcctSets.getInt("SWDISCABL"))
				+ ") ON DUPLICATE KEY UPDATE "
				+ " " + SMTableapdistributioncodes.sdescription + " = '" + FormatSQLStatement(rsAcctSets.getString("TEXTDESC").trim()) + "'"
				+ ", " + SMTableapdistributioncodes.sglacct + " = '" + FormatSQLStatement(rsAcctSets.getString("IDGLACCT").trim()) + "'"
				+ ", " + SMTableapdistributioncodes.idiscountable + " = " + Integer.toString(rsAcctSets.getInt("SWDISCABL"))
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsAcctSets.close();
				throw new Exception("Error [1450105422] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rsAcctSets.close();
		
		//Confirm that all the accounts are really in the SMCP GL...
		SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
		;
		Statement stmtdistcodes = cnSMCP.createStatement();
		ResultSet rsdistcodes = stmtdistcodes.executeQuery(SQL);
		while (rsdistcodes.next()){
			SQL = "SELECT * FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.sAcctID + " = '" + rsdistcodes.getString(SMTableapdistributioncodes.sglacct) + "')"
				+ ")"
			;
			try {
				Statement stmt1 = cnSMCP.createStatement();
				ResultSet rsPayables = stmt1.executeQuery(SQL);
				if (!rsPayables.next()){
					s += "Distribution code '" + rsdistcodes.getString(SMTableapdistributioncodes.sdistcodename) 
						+ "' references GL Account '" + rsdistcodes.getString(SMTableapdistributioncodes.sglacct) + "'"
						+ " which is not in the GL Accounts.<BR>";
				}
				rsPayables.close();
			} catch (Exception e) {
				throw new Exception("Error [1450105423] reading GL account from GL - " + e.getMessage());
			}
		}

		//pwOut.println("<BR>Added " + Integer.toString(iCounter) + " AP account set records to " + sTablename + "<BR>");
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP distribution code records to " + sTablename + "<BR>";
		return s;
	}
	private static String convertVendorGroups(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{
		
		//First delete the vendor group records:
		String sTablename = SMTableapvendorgroups.TableName;
		String s = "";

		//Load the account sets, banks, distribution codes, and 1009CPRS's into arrays so we can quickly read them for each record:
		ArrayList<Integer>arr1099CPRSCodeIDs = new ArrayList<Integer>(0);
		ArrayList<String>arr1099CPRSCodes = new ArrayList<String>(0);
		ArrayList<Integer>arrDistributionCodeIDs = new ArrayList<Integer>(0);
		ArrayList<String>arrDistributionCodes = new ArrayList<String>(0);
		ArrayList<Integer>arrAccountSetIDs = new ArrayList<Integer>(0);
		ArrayList<String>arrAccountSetCodes = new ArrayList<String>(0);
		ArrayList<Integer>arrBankIDs = new ArrayList<Integer>(0);
		ArrayList<String>arrBankShortNames = new ArrayList<String>(0);
		
		//Load the 1099/CPRS codes:
		arr1099CPRSCodeIDs.add(0);
		arr1099CPRSCodes.add("");
		String SQL = "SELECT * FROM " + SMTableap1099cprscodes.TableName + " ORDER BY " + SMTableap1099cprscodes.sclassid;
		try {
			Statement stmtSMCP = cnSMCP.createStatement();
			ResultSet rs = stmtSMCP.executeQuery(SQL);
			while (rs.next()){
				arr1099CPRSCodeIDs.add(rs.getInt(SMTableap1099cprscodes.lid));
				arr1099CPRSCodes.add(rs.getString(SMTableap1099cprscodes.sclassid));
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1450381490] - could not load records from " + SMTableap1099cprscodes.TableName + " table to create vendor groups - " + e1.getMessage());
		}
		
		//Load the distributon codes:
		arrDistributionCodeIDs.add(0);
		arrDistributionCodes.add("");
		SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName + " ORDER BY " + SMTableapdistributioncodes.sdistcodename;
		try {
			Statement stmtSMCP = cnSMCP.createStatement();
			ResultSet rs = stmtSMCP.executeQuery(SQL);
			while (rs.next()){
				arrDistributionCodeIDs.add(rs.getInt(SMTableapdistributioncodes.lid));
				arrDistributionCodes.add(rs.getString(SMTableapdistributioncodes.sdistcodename));
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1450381491] - could not load records from " + SMTableapdistributioncodes.TableName + " table to create vendor groups - " + e1.getMessage());
		}
		
		//Load the account sets:
		arrAccountSetIDs.add(0);
		arrAccountSetCodes.add("");
		SQL = "SELECT * FROM " + SMTableapaccountsets.TableName + " ORDER BY " + SMTableapaccountsets.sacctsetname;
		try {
			Statement stmtSMCP = cnSMCP.createStatement();
			ResultSet rs = stmtSMCP.executeQuery(SQL);
			while (rs.next()){
				arrAccountSetIDs.add(rs.getInt(SMTableapaccountsets.lid));
				arrAccountSetCodes.add(rs.getString(SMTableapaccountsets.sacctsetname));
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1450381492] - could not load records from " + SMTableapaccountsets.TableName + " table to create vendor groups - " + e1.getMessage());
		}
		
		//Load the banks:
		arrBankIDs.add(0);
		arrBankShortNames.add("");
		SQL = "SELECT * FROM " + SMTablebkbanks.TableName + " ORDER BY " + SMTablebkbanks.sshortname;
		try {
			Statement stmtSMCP = cnSMCP.createStatement();
			ResultSet rs = stmtSMCP.executeQuery(SQL);
			while (rs.next()){
				arrBankIDs.add(rs.getInt(SMTablebkbanks.lid));
				arrBankShortNames.add(rs.getString(SMTablebkbanks.sshortname));
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1450381492] - could not load records from " + SMTablebkbanks.TableName + " table to create vendor groups - " + e1.getMessage());
		}
		
		SQL = "SELECT * FROM APVGR";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsVendorGroups = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsVendorGroups.next()){
			String sDistributeBy = Integer.toString(rsVendorGroups.getInt("SWDISTBY"));
			//If in ACCPAC the vendor group was set to distribute by 'Distribution Set', then we have to set it to 'None' here,
			//And the user will have to correct it manually if they want it changed:
			if (sDistributeBy.compareToIgnoreCase("0") == 0){
				sDistributeBy = Integer.toString(SMTableapvendorgroups.DISTRIBUTE_BY_TYPE_NONE);
			}
			
			//Get the corresponding IDs for the 1099/CPRS table, account sets, distribution codes, and banks:
			int i1099CPRS_Index = arr1099CPRSCodes.indexOf(rsVendorGroups.getString("CLASSID").trim());
			if (i1099CPRS_Index == -1){
				i1099CPRS_Index = 0;
			}
			int iAccountSet_Index = arrAccountSetCodes.indexOf(rsVendorGroups.getString("ACCTSETID").trim());
			if (iAccountSet_Index == -1){
				iAccountSet_Index = 0;
			}
			int iBank_Index = arrBankShortNames.indexOf(rsVendorGroups.getString("BANKID").trim());
			if (iBank_Index == -1){
				iBank_Index = 0;
			}
			int iDistributionCode_Index = arrDistributionCodes.indexOf(rsVendorGroups.getString("DISTCODE").trim());
			if (iDistributionCode_Index == -1){
				iDistributionCode_Index = 0;
			}
			String SQLInsert = "";
			try {
				SQLInsert = "INSERT INTO " + sTablename + "("
					+ SMTableapvendorgroups.i1099CPRScode
					+ ", " + SMTableapvendorgroups.iactive
					+ ", " + SMTableapvendorgroups.iapaccountset
					+ ", " + SMTableapvendorgroups.ibankcode
					+ ", " + SMTableapvendorgroups.idistributeby
					+ ", " + SMTableapvendorgroups.idistributioncodeusedfordistribution
					+ ", " + SMTableapvendorgroups.iprintseparatechecks
					+ ", " + SMTableapvendorgroups.itaxreportingtype
					//+ ", " + SMTableapvendorgroups.itaxtype
					+ ", " + SMTableapvendorgroups.sdescription
					+ ", " + SMTableapvendorgroups.sglacctusedfordistribution
					+ ", " + SMTableapvendorgroups.sgroupid
					//+ ", " + SMTableapvendorgroups.staxjurisdiction
					+ ", " + SMTableapvendorgroups.stermscode
					+ ") VALUES ("
					+ " " + Integer.toString(arr1099CPRSCodeIDs.get(i1099CPRS_Index))
					+ ", " + Integer.toString(rsVendorGroups.getInt("ACTIVESW"))
					+ ", " + Integer.toString(arrAccountSetIDs.get(iAccountSet_Index))
					+ ", " + Integer.toString(arrBankIDs.get(iBank_Index))
					+ ", " + sDistributeBy
					+ ", " + Integer.toString(arrDistributionCodeIDs.get(iDistributionCode_Index))
					+ ", " + Integer.toString(rsVendorGroups.getInt("PRTSEPCHKS"))
					+ ", " + Integer.toString(rsVendorGroups.getInt("TAXRPTSW"))
					// Tax Type
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("DESCRIPTN").trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("GLACCTID").trim().replace("-", "")) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("GROUPID").trim()) + "'"
					// Tax Jurisdiction
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("TERMCODE").trim()) + "'"
					+ ") ON DUPLICATE KEY UPDATE "
					+ " " + SMTableapvendorgroups.i1099CPRScode + " = " + Integer.toString(arr1099CPRSCodeIDs.get(i1099CPRS_Index))
					+ ", " + SMTableapvendorgroups.iactive + " = " + Integer.toString(rsVendorGroups.getInt("ACTIVESW"))
					+ ", " + SMTableapvendorgroups.iapaccountset + " = " + Integer.toString(arrAccountSetIDs.get(iAccountSet_Index))
					+ ", " + SMTableapvendorgroups.ibankcode + " = " + Integer.toString(arrBankIDs.get(iBank_Index))
					+ ", " + SMTableapvendorgroups.idistributeby + " = " + sDistributeBy
					+ ", " + SMTableapvendorgroups.idistributioncodeusedfordistribution + " = " + Integer.toString(arrDistributionCodeIDs.get(iDistributionCode_Index))
					+ ", " + SMTableapvendorgroups.iprintseparatechecks + " = " + Integer.toString(rsVendorGroups.getInt("PRTSEPCHKS"))
					+ ", " + SMTableapvendorgroups.itaxreportingtype + " = " + Integer.toString(rsVendorGroups.getInt("TAXRPTSW"))
					+ ", " + SMTableapvendorgroups.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("DESCRIPTN").trim()) + "'"
					+ ", " + SMTableapvendorgroups.sglacctusedfordistribution + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("GLACCTID").trim()) + "'"
					+ ", " + SMTableapvendorgroups.stermscode + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorGroups.getString("TERMCODE").trim()) + "'"
				;
			} catch (Exception e1) {
				rsVendorGroups.close();
				throw new Exception("Error [1451400752] - could not build insert statement - SQLInsert = " + SQLInsert + " - " + e1.getMessage());
			}
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsVendorGroups.close();
				throw new Exception("Error [1450277492] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rsVendorGroups.close();
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP vendor group record(s) to " + sTablename + "<BR>";
		return s;
	}
	
	private static String convertVendorRemitToLocations(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{

		String sTablename = SMTableapvendorremittolocations.TableName;
		String s = "";

		String SQL = "SELECT * FROM APVNR";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsVendorRemitTos = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsVendorRemitTos.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableapvendorremittolocations.datlastmaintained
				+ ", " + SMTableapvendorremittolocations.iactive
				+ ", " + SMTableapvendorremittolocations.saddressline1
				+ ", " + SMTableapvendorremittolocations.saddressline2
				+ ", " + SMTableapvendorremittolocations.saddressline3
				+ ", " + SMTableapvendorremittolocations.saddressline4
				+ ", " + SMTableapvendorremittolocations.scity
				+ ", " + SMTableapvendorremittolocations.scontactname
				+ ", " + SMTableapvendorremittolocations.scountry
				+ ", " + SMTableapvendorremittolocations.semailaddress
				+ ", " + SMTableapvendorremittolocations.sfaxnumber
				+ ", " + SMTableapvendorremittolocations.slasteditedby
				+ ", " + SMTableapvendorremittolocations.sphonenumber
				+ ", " + SMTableapvendorremittolocations.spostalcode
				+ ", " + SMTableapvendorremittolocations.sremittocode
				+ ", " + SMTableapvendorremittolocations.sremittoname
				+ ", " + SMTableapvendorremittolocations.sstate
				+ ", " + SMTableapvendorremittolocations.svendoracct
				//+ ", " + SMtableapvendorremittolocations.swebaddress
				+ ") VALUES ("
				+ " '" + convertACCPACLongDateToString(rsVendorRemitTos.getLong("DATELASTMN"), false) + "'"
				+ ", " + Integer.toString(rsVendorRemitTos.getInt("SWACTV"))
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE1").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE2").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE3").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE4").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("NAMECITY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("NAMECTAC").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODECTRY").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("EMAIL").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTPHON2").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("AUDTUSER").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTPHON1").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODEPSTL").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("IDVENDRMIT").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("RMITNAME").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODESTTE").trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("IDVEND").trim()) + "'"
				//web address isn't in ACCPAC
				+ ") ON DUPLICATE KEY UPDATE "
				+ " " + SMTableapvendorremittolocations.datlastmaintained + " = '" + convertACCPACLongDateToString(rsVendorRemitTos.getLong("DATELASTMN"), false) + "'"
				+ ", " + SMTableapvendorremittolocations.iactive + " = " + Integer.toString(rsVendorRemitTos.getInt("SWACTV"))
				+ ", " + SMTableapvendorremittolocations.saddressline1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE1").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.saddressline2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE2").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.saddressline3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE3").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.saddressline4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTSTRE4").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.scity + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("NAMECITY").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.scontactname + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("NAMECTAC").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.scountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODECTRY").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.semailaddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("EMAIL").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.sfaxnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTPHON2").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.slasteditedby + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("AUDTUSER").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.sphonenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("TEXTPHON1").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.spostalcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODEPSTL").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.sremittoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("RMITNAME").trim()) + "'"
				+ ", " + SMTableapvendorremittolocations.sstate + " = '" + clsDatabaseFunctions.FormatSQLStatement(rsVendorRemitTos.getString("CODESTTE").trim()) + "'"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsVendorRemitTos.close();
				throw new Exception("Error [1450474728] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rsVendorRemitTos.close();
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP vendor remit to record(s) to " + sTablename + "<BR>";
		return s;
	}
	/*
	private static String convertSchedules(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			PrintWriter pwOut) throws Exception{

		String sTablename = SMTablereminders.TableName;
		String s = "";

		String SQL = "SELECT * FROM CSSKTB";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsSchedules = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsSchedules.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTablereminders.datlastrundate
				+ ", " + SMTablereminders.datstartdate
				+ ", " + SMTablereminders.idayofmonth
				+ ", " + SMTablereminders.ifrequencytype
				+ ", " + SMTablereminders.ifriday
				+ ", " + SMTablereminders.iinterval
				+ ", " + SMTablereminders.imonday
				+ ", " + SMTablereminders.imonth
				+ ", " + SMTablereminders.iphase
				+ ", " + SMTablereminders.isaturday
				+ ", " + SMTablereminders.isunday
				+ ", " + SMTablereminders.ithursday
				+ ", " + SMTablereminders.ituesday
				+ ", " + SMTablereminders.iwednesday
				+ ", " + SMTablereminders.iweek
				+ ", " + SMTablereminders.iweekday
				+ ", " + SMTablereminders.sdescription
				+ ", " + SMTablereminders.sschedulecode
				+ ", " + SMTablereminders.iremindermode
				+ ", " + SMTablereminders.iremindhowmanydaysinadvance
				+ ", " + SMTablereminders.susertobereminded
				+ ") VALUES ("
				+ " '" + convertACCPACLongDateToString(rsSchedules.getLong("LASTDATE"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsSchedules.getLong("ACTIVEDATE"), false) + "'"
				+ ", " + Integer.toString(rsSchedules.getInt("MONTHDAY"))
				+ ", " + Integer.toString(rsSchedules.getInt("FREQUENCY"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFFRI"))
				+ ", " + Integer.toString(rsSchedules.getInt("INTERVAL"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFMON"))
				+ ", " + Integer.toString(rsSchedules.getInt("MONTH"))
				+ ", " + Integer.toString(rsSchedules.getInt("PHASE"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFSAT"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFSUN"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFTHU"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFTUE"))
				+ ", " + Integer.toString(rsSchedules.getInt("WDFWED"))
				+ ", " + Integer.toString(rsSchedules.getInt("WEEK"))
				+ ", " + Integer.toString(rsSchedules.getInt("WEEKDAY"))
				+ ", '" + SMUtilities.FormatSQLStatement(rsSchedules.getString("SCHEDDESC").trim()) + "'"
				+ ", '" + SMUtilities.FormatSQLStatement(rsSchedules.getString("SCHEDKEY").trim()) + "'"
				+ ", " + Integer.toString(rsSchedules.getInt("USERMODE"))
				+ ", " + Integer.toString(rsSchedules.getInt("REMINDLEAD"))
				+ ", '" + SMUtilities.FormatSQLStatement(rsSchedules.getString("USERID").trim()) + "'"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsSchedules.close();
				throw new Exception("Error [1450877268] - could not insert into " + sTablename + " table - SQL: " + SQLInsert
					+ e.getMessage());
			}
		}
		rsSchedules.close();
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " SM Schedule record(s) to " + sTablename + "<BR>";
		return s;
	}
	*/
	private static String convert1099CPRSCodes(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{
		
		//First delete the 1099/CPRS records:
		String sTablename = SMTableap1099cprscodes.TableName;
		String s = "";

		String SQL = "SELECT * FROM APCLX";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rs1099CPRSCodes = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rs1099CPRSCodes.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableap1099cprscodes.bdminimumreportingamt
				+ ", " + SMTableap1099cprscodes.iactive
				+ ", " + SMTableap1099cprscodes.sclassid
				+ ", " + SMTableap1099cprscodes.sdescription
				+ ") VALUES ("
				+ " " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofcreditnotesscale, rs1099CPRSCodes.getBigDecimal("MINAMT")).replace(",", "")
				+ ", " + Integer.toString(rs1099CPRSCodes.getInt("SWACTV"))
				+ ", '" + FormatSQLStatement(rs1099CPRSCodes.getString("CLASSID").trim()).replace("-", "") + "'"
				+ ", '" + FormatSQLStatement(rs1099CPRSCodes.getString("CLASSDESC").trim()).replace("-", "") + "'"
				+ ") ON DUPLICATE KEY UPDATE"
				+ " " + SMTableap1099cprscodes.bdminimumreportingamt + " = " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofcreditnotesscale, rs1099CPRSCodes.getBigDecimal("MINAMT")).replace(",", "")
				+ ", " + SMTableap1099cprscodes.iactive + " = " + Integer.toString(rs1099CPRSCodes.getInt("SWACTV"))
				+ ", " + SMTableap1099cprscodes.sdescription + " = '" + FormatSQLStatement(rs1099CPRSCodes.getString("CLASSDESC").trim()).replace("-", "") + "'"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rs1099CPRSCodes.close();
				throw new Exception("Error [1450189438] - could not insert into " + sTablename + " table - " + e.getMessage());
			}
		}
		rs1099CPRSCodes.close();
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP 1099/CPRS code record(s) to " + sTablename + "<BR>";
		return s;
	}
	/*
	private static String checkVendorTerms(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			PrintWriter pwOut) throws Exception{
		
		String sTermsNotFound = "";
		String s = "";
		
		//First initialize the ACCPAC Terms Codes on the vendor terms:
		//String sTablename = SMTableicvendorterms.TableName;
		//String SQL = "UPDATE " + sTablename
		//	+ " SET " + SMTableicvendorterms.sACCPACtermscode + " = ''"
		//;
		//String s = "";
		//try {
		//	Statement stmt = cnSMCP.createStatement();
		//	stmt.execute(SQL);
		//} catch (Exception e) {
		//	throw new Exception("Error [1450305122] - could not initialize vendor terms from " + sTablename + " table - " + e.getMessage());
		//}
		
		//Remove any terms which were only added by the ACCPAC conversion:
		//SQL = "DELETE FROM " + sTablename 
		//	+ " WHERE ("
		//		+ "(" + SMTableicvendorterms.iaddedfromACCPAC + " = 1)"
		//	+ ")"
		//;
		//try {
		//	Statement stmt = cnSMCP.createStatement();
		//	stmt.execute(SQL);
		//} catch (Exception e) {
		//	throw new Exception("Error [14503051223] - could not remove ACCPAC-added terms in " + sTablename + " table - " + e.getMessage());
		//}

		String SQL = "SELECT * FROM APRTA";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsAPTerms = stmtACCPAC.executeQuery(SQL);
		while (rsAPTerms.next()){
			//First try to match the ACCPAC terms with a terms record in icvendorterms:
			SQL = "SELECT"
				+ " " + SMTableicvendorterms.sTermsCode
				+ " FROM " + SMTableicvendorterms.TableName
				+ " WHERE ("
					+ "(" + SMTableicvendorterms.sTermsCode + " = '" + rsAPTerms.getString("TERMSCODE").trim() + "')"
				+ ")"
			;
			Statement stmtSMCP = cnSMCP.createStatement();
			ResultSet rsSMCPTerms = stmtSMCP.executeQuery(SQL);
			if (!rsSMCPTerms.next()){
				sTermsNotFound += "ACCPAC AP Terms '" + rsAPTerms.getString("TERMSCODE") + "'<BR>";
			}
			rsSMCPTerms.close();
		}
		rsAPTerms.close();
		
		if (sTermsNotFound.compareToIgnoreCase("") != 0){
			throw new Exception("<FONT COLOR=RED><B>CRITICAL WARNING: The ACCPAC terms listed below were not found in SMCP - these will have to be set up manually"
				+ " to make sure that ACCPAC and SMCP terms match:<BR>"
				+ sTermsNotFound
				+ "</B></FONT>"
			);
		}
		
		s +=  "<BR>Checked to make sure that all ACCPAC AP Terms are also in SMCP.<BR>";
		return s;
	}
	*/
	private static String convertStatistics(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType) throws Exception{
		
		String sTablename = SMTableapvendorstatistics.TableName;
		String s = "";

		String SQL = "SELECT * FROM APVSM";
		int iCounter;
		ResultSet rsStatistics = null;
		try {
			Statement stmtACCPAC = cnACCPAC.createStatement();
			rsStatistics = stmtACCPAC.executeQuery(SQL);
			iCounter = 0;
			while (rsStatistics.next()){
				
				//The 'number of pays used in averaging' is the TOTAL number of days-to-pay for the vendor, for the period.
				//This number, divided by the total number of payments, will give us the 'average days to pay' for that vendor/period.
				
				// 'CNTDTOPAY' is the total number of days to pay in the period
				// 'CNTINVCPD' is the total number of invoices paid in the period
				// 'AVGDAYSPAY' is the average number of days to pay in the period, which is equal to CNTDTOPAY / CNTINVCPD
				
				// We don't need to carry the average number of days to pay in the period, because we can calulate it any time, on the fly
				
				//int iNumberOfPaysUsedInAveraging = 0;
				//if (rsStatistics.getInt("AVGDAYSPAY") > 0){
				//	iNumberOfPaysUsedInAveraging = rsStatistics.getInt("CNTDTOPAY") / rsStatistics.getInt("AVGDAYSPAY");
				//}
				
				String SQLInsert = "INSERT INTO " + sTablename + "("
					+ SMTableapvendorstatistics.bdamountofadjustments
					+ ", " + SMTableapvendorstatistics.bdamountofcreditnotes
					+ ", " + SMTableapvendorstatistics.bdamountofdebitnotes
					+ ", " + SMTableapvendorstatistics.bdamountofdiscounts
					+ ", " + SMTableapvendorstatistics.bdamountofdiscountslost
					+ ", " + SMTableapvendorstatistics.bdamountofinvoices
					+ ", " + SMTableapvendorstatistics.bdamountofpayments
					+ ", " + SMTableapvendorstatistics.lmonth
					+ ", " + SMTableapvendorstatistics.lnumberofadjustments
					+ ", " + SMTableapvendorstatistics.lnumberofcredits
					+ ", " + SMTableapvendorstatistics.lnumberofdaystopay
					+ ", " + SMTableapvendorstatistics.lnumberofdebits
					+ ", " + SMTableapvendorstatistics.lnumberofdiscountslost
					+ ", " + SMTableapvendorstatistics.lnumberofdiscountstaken
					+ ", " + SMTableapvendorstatistics.lnumberofinvoices
					+ ", " + SMTableapvendorstatistics.lnumberofinvoicespaid
					//+ ", " + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging
					+ ", " + SMTableapvendorstatistics.lnumberofpayments
					+ ", " + SMTableapvendorstatistics.lyear
					+ ", " + SMTableapvendorstatistics.svendoracct
					
					+ ") VALUES ("
					
					//bdamountofadjustments
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofadjustmentsscale, rsStatistics.getBigDecimal("AMTADJHC")).replace(",", "")
					
					//bdamountofcreditnotes
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofcreditnotesscale, rsStatistics.getBigDecimal("AMTCRHC")).replace(",", "")
					
					//bdamountofdebitnotes
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofdebitnotesscale, rsStatistics.getBigDecimal("AMTDRHC")).replace(",", "")
					
					//bdamountofdiscounts
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofdiscountsscale, rsStatistics.getBigDecimal("AMTDISCHC")).replace(",", "")
					
					//bdamountofdiscountslost
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofdiscountslostscale, rsStatistics.getBigDecimal("AMTLOSTHC")).replace(",", "")
					
					//bdamountofinvoices
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofinvoicesscale, rsStatistics.getBigDecimal("AMTINVCHC")).replace(",", "")
					
					//bdamountofpayments
					+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapvendorstatistics.bdamountofpaymentsscale, rsStatistics.getBigDecimal("AMTPAYMHC")).replace(",", "")
					
					// We don't need to store the average days to pay, because it's a calculation:
					// lnumberofdaystopay / lnumberofinvoicespaid
					//+ ", " + Integer.toString(rsStatistics.getInt("AVGDAYSPAY"))
					
					//lmonth
					+ ", " + Integer.toString(rsStatistics.getInt("CNTPERD"))
					
					//lnumberofadjustments
					+ ", " + Integer.toString(rsStatistics.getInt("CNTADJ"))
					
					//lnumberofcredits
					+ ", " + Integer.toString(rsStatistics.getInt("CNTCR"))
					
					//lnumberofdaystopay
					+ ", " + Integer.toString(rsStatistics.getInt("CNTDTOPAY"))
					
					//lnumberofdebits
					+ ", " + Integer.toString(rsStatistics.getInt("CNTDR"))
					
					//lnumberofdiscountslost
					+ ", " + Integer.toString(rsStatistics.getInt("CNTLOST"))
					
					//lnumberofdiscountstaken
					+ ", " + Integer.toString(rsStatistics.getInt("CNTDISC"))
					
					//lnumberofinvoices
					+ ", " + Integer.toString(rsStatistics.getInt("CNTINVC"))
					
					//lnumberofinvoicespaid
					+ ", " + Integer.toString(rsStatistics.getInt("CNTINVCPD"))
					
					//lnumberofpayments
					+ ", " + Integer.toString(rsStatistics.getInt("CNTPAYM"))
					
					//lyear
					+ ", " + Integer.toString(rsStatistics.getInt("CNTYR"))
					
					//svendoracct
					+ ", '" + FormatSQLStatement(rsStatistics.getString("VENDORID").trim()) + "'"
					+ ")"
				;
				try {
					Statement stmtInsert = cnSMCP.createStatement();
					stmtInsert.execute(SQLInsert);
					iCounter++;
				} catch (Exception e) {
					rsStatistics.close();
					throw new Exception("Error [1450128862] - could not insert into " + sTablename + " table with SQL: " + SQLInsert + " - " + e.getMessage());
				}
			}
			rsStatistics.close();
		} catch (Exception e) {
			rsStatistics.close();
			throw new Exception("Error [1450130726] - could not read table 'APVSM' with SQL: " + SQL + " - " + e.getMessage());
		}
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " AP monthly vendor statistics to " + sTablename + "<BR>";
		return s;
	}
	private static String processBanks(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType,
			String sConversionUser) throws Exception{
		//Now create any banks necessary:
		String SQL = "SELECT * FROM BKACCT";
		String s = "";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		Statement stmtSMCP = cnSMCP.createStatement();
		ResultSet rsACCPACBanks = null;
		ResultSet rsSMCPBanks = null;
		int iCounter = 0;
		try {
			rsACCPACBanks = stmtACCPAC.executeQuery(SQL);
			while (rsACCPACBanks.next()){
				//If this bank is not already built, then insert it now:
				try {
					SQL = "SELECT * FROM " + SMTablebkbanks.TableName
						+ " WHERE ("
							+ "(" + SMTablebkbanks.sshortname + " = '" + rsACCPACBanks.getString("BANK").trim() + "')"
						+ ")"
					;
					rsSMCPBanks = stmtSMCP.executeQuery(SQL);
					if (!rsSMCPBanks.next()){
						SQL = "INSERT INTO " + SMTablebkbanks.TableName + "("
							+ SMTablebkbanks.dattimelastmaintained
							+ ", " + SMTablebkbanks.iactive
							+ ", " + SMTablebkbanks.saccountname
							+ ", " + SMTablebkbanks.saccountnumber
							+ ", " + SMTablebkbanks.sbankname
							+ ", " + SMTablebkbanks.sglaccount
							+ ", " + SMTablebkbanks.llastmaintainedbyid
							+ ", " + SMTablebkbanks.slastmaintainedbyfullname
							+ ", " + SMTablebkbanks.sroutingnumber
							+ ", " + SMTablebkbanks.sshortname
							+ ", " + SMTablebkbanks.iaddedbyapconversion
							+ ", " + SMTablebkbanks.saddressline1
							+ ", " + SMTablebkbanks.saddressline2
							+ ", " + SMTablebkbanks.saddressline3
							+ ", " + SMTablebkbanks.saddressline4
							+ ", " + SMTablebkbanks.scity
							+ ", " + SMTablebkbanks.sstate
							+ ", " + SMTablebkbanks.scountry
							+ ", " + SMTablebkbanks.spostalcode
							+ ") VALUES ("
							+ "NOW()"
							+ ", " + Integer.toString(
								Math.abs((rsACCPACBanks.getInt("INACTIVE") - 1)) //This math reverses the value of 1 or 0, since in ACCPAC it's 'INACTIVE', but in SMCP the field is 'ACTIVE'...
								)
							+ ", '" + rsACCPACBanks.getString("NAME").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("BKACCT").trim() + "'"
							+ ", '" + "(Bank added by AP Conversion)" + "'"
							+ ", '" + rsACCPACBanks.getString("IDACCT").trim().replace("-", "") + "'"
							+ ", "  + "0"
							+ ", '" + "AP ACCPAC Conversion'"
							+ ", '" + rsACCPACBanks.getString("TRANSIT").trim().replace("-", "") + "'"
							+ ", '" + rsACCPACBanks.getString("BANK").trim() + "'"
							+ ", 1"
							+ ", '" + rsACCPACBanks.getString("ADDR1").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("ADDR2").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("ADDR3").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("ADDR4").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("CITY").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("STATE").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("COUNTRY").trim() + "'"
							+ ", '" + rsACCPACBanks.getString("POSTAL").trim() + "'"
							+ ")"
						;
						try {
							Statement stmtInsertBank = cnSMCP.createStatement();
							stmtInsertBank.execute(SQL);
						} catch (Exception e1) {
							throw new Exception("Error [1449852777] adding bank with SQL: " + SQL + " - " + e1.getMessage());
						}
						iCounter++;
					}
					rsSMCPBanks.close();
				} catch (Exception e) {
					throw new Exception("Error [1450493560] - could not list existing banks in SMCP - " + e.getMessage() + ".");
				}
			}
		} catch (Exception e) {
			throw new Exception("Error [1449767642] listing ACCPAC banks - " + e.getMessage());
		}
		rsACCPACBanks.close();
		
		s +=  "<BR>Added " + Integer.toString(iCounter) + " Banks to " + SMTablebkbanks.TableName + "<BR>";
		return s;
	}
	private static String processVendors(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType,
			String sConversionUser) throws Exception{
		String s = "";
		String SQL = "";
		String sTempVendorsTable = "ventemp";
		Statement stmtSMCP = cnSMCP.createStatement();
		Statement stmtACCPAC = cnACCPAC.createStatement();
		
		//First create a temporary vendors table in MySQL to represent the ACCPAC vendors:
		SQL = "DROP TABLE IF EXISTS " + sTempVendorsTable;
		try {
			stmtSMCP.execute(SQL);
		} catch (Exception e1) {
			//We don't care about errors here - the table may not have been created so errors don't matter.
		}
		
		SQL = "CREATE TABLE " + sTempVendorsTable + " LIKE " + SMTableicvendors.TableName;
		try {
			stmtSMCP.execute(SQL);
		} catch (Exception e1) {
			throw new Exception("Error [1449764246] could not create temporary vendors table - " + e1.getMessage());
		}
		
		SQL = "ALTER TABLE " + sTempVendorsTable 
			+ " ADD COLUMN sacctsetname varchar(32) NOT NULL DEFAULT ''"
			+ ", ADD COLUMN sbankname varchar(8) NOT NULL DEFAULT ''"
			+ ", ADD COLUMN staxreporting1099CPRScode varchar(6) NOT NULL DEFAULT ''"
			+ ", ADD COLUMN svendorgroup varchar(6) NOT NULL DEFAULT ''"
			;
		
		try {
			stmtSMCP.execute(SQL);
		} catch (Exception e1) {
			throw new Exception("Error [1449764247] couldn't alter temporary vendors table - " + e1.getMessage());
		}
		
		//Read all the ACCPAC vendor into the temporary vendors table:
		SQL = "SELECT * FROM APVEN";
		try {
			ResultSet rsACCPACVendors = stmtACCPAC.executeQuery(SQL);
			while (rsACCPACVendors.next()){
				//Add this vendor to the temporary table:
				SQL = "INSERT INTO " + sTempVendorsTable + "("
					+ " " + SMTableicvendors.datlastmaintained
					+ ", " + SMTableicvendors.iactive
					+ ", " + "sacctsetname"
					+ ", " + "sbankname"
					+ ", " + "staxreporting1099CPRScode"
					+ ", " + "svendorgroup"
					//+ ", " + SMTableicvendors.ipoconfirmationrequired
					+ ", " + SMTableicvendors.saddressline1
					+ ", " + SMTableicvendors.saddressline2
					+ ", " + SMTableicvendors.saddressline3
					+ ", " + SMTableicvendors.saddressline4
					+ ", " + SMTableicvendors.scity
					//+ ", " + SMTableicvendors.scompanyacctcode
					+ ", " + SMTableicvendors.scontactname
					+ ", " + SMTableicvendors.scountry
					+ ", " + SMTableicvendors.sdefaultdistributioncode
					+ ", " + SMTableicvendors.sdefaultexpenseacct
					+ ", " + SMTableicvendors.sfaxnumber
					+ ", " + SMTableicvendors.sgdoclink
					+ ", " + SMTableicvendors.slasteditedbyfullname
					+ ", " + SMTableicvendors.sname
					+ ", " + SMTableicvendors.sphonenumber
					+ ", " + SMTableicvendors.spostalcode
					+ ", " + SMTableicvendors.sstate
					+ ", " + SMTableicvendors.sterms
					+ ", " + SMTableicvendors.svendoracct
					+ ", " + SMTableicvendors.swebaddress
					+ ", " + SMTableicvendors.svendoremail
					+ ", " + SMTableicvendors.sprimaryremittocode
					+ ", " + SMTableicvendors.itaxreportingtype
					+ ", " + SMTableicvendors.staxidentifyingnumber
					+ ", " + SMTableicvendors.itaxidnumbertype
					+ ", " + SMTableicvendors.igenerateseparatepaymentsforeachinvoice
					+ ") VALUES ("
					+ "'" + convertACCPACLongDateToString(rsACCPACVendors.getLong("DATELASTMN"), true) + "'" //SMTableicvendors.datlastmaintained
					+ ", " + Integer.toString(rsACCPACVendors.getInt("SWACTV")) //SMTableicvendors.iactive
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("IDACCTSET").trim()) + "'" //account set NAME
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("BANKID").trim()) + "'" //bank ID from ACCPAC
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("CLASID").trim()) + "'" //1099 or CPRS code for tax reporting from ACCPAC
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("IDGRP").trim()) + "'" //vendor group
					//SMTableicvendors.ipoconfirmationrequired
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE1").trim()) + "'" //SMTableicvendors.saddressline1
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE2").trim()) + "'" //SMTableicvendors.saddressline2
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE3").trim()) + "'" //SMTableicvendors.saddressline3
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE4").trim()) + "'" //SMTableicvendors.saddressline4
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("NAMECITY").trim()) + "'" //SMTableicvendors.scity
					//SMTableicvendors.scompanyacctcode
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("NAMECTAC").trim()) + "'" //SMTableicvendors.scontactname
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("CODECTRY").trim()) + "'" //SMTableicvendors.scountry
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("DISTCODE").trim()) + "'" //SMTableicvendors.sdefaultdistributioncode
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("GLACCNT").trim().replace("-", "")) + "'" //SMTableicvendors.sdefaultexpenseacct
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTPHON2").trim()) + "'" //SMTableicvendors.sfaxnumber
					+ ", ''" //sgdoclink
					+ ", '" + sConversionUser + "'" //SMTableicvendors.slasteditedby
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("VENDNAME").trim()) + "'" //SMTableicvendors.sname
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTPHON1").trim()) + "'" //SMTableicvendors.sphonenumber
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("CODEPSTL").trim()) + "'" //SMTableicvendors.spostalcode
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("CODESTTE").trim()) + "'" //SMTableicvendors.sstate
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TERMSCODE").trim()) + "'" //SMTableicvendors.sterms
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("VENDORID").trim()) + "'" //SMTableicvendors.svendoracct
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("WEBSITE").trim()) + "'" //SMTableicvendors.swebaddress
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("PRIMRMIT").trim()) + "'" //SMTableicvendors.sprimaryremittocode
					+ ", " + Integer.toString(rsACCPACVendors.getInt("TAXRPTSW")) //SMTableicvendors.itaxreportingtype
					+ ", '" + FormatSQLStatement(rsACCPACVendors.getString("TAXNBR").trim()) + "'" //SMTableicvendors.staxidentifyingnumber
					+ ", " + Integer.toString(rsACCPACVendors.getInt("TAXIDTYPE")) //SMTableicvendors.itaxidnumbertype
					+ ", " + Integer.toString(rsACCPACVendors.getInt("PRTSEPCHKS")) //SMTableicvendors.igenerateseparatepaymentsforeachinvoice
					+ ")"
					;
				try {
					stmtSMCP.execute(SQL);
				} catch (Exception e1) {
					throw new Exception("Error [1449764244] inserting vendor into temporary table with SQL: " + SQL + " - " + e1.getMessage());
				}
			}
			rsACCPACVendors.close();
		} catch (Exception e) {
			throw new Exception("Error [1449764245] reading ACCPAC vendors - " + e.getMessage());
		}
		
		//Confirm that the GL accounts are all in SMCP
		SQL = "SELECT DISTINCT"
			+ " " + SMTableicvendors.sdefaultexpenseacct
			+ " FROM " + sTempVendorsTable
			+ " LEFT JOIN " + SMTableglaccounts.TableName
			+ " ON " + sTempVendorsTable + "." + SMTableicvendors.sdefaultexpenseacct 
			+ " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " WHERE ("
				+ "(" + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID + " IS NULL)"
				+ " AND (" + sTempVendorsTable + "." + SMTableicvendors.sdefaultexpenseacct + " != '')"
			+ ")"
		;
		s += "<BR><BR>The following GL accounts are used as default accounts on vendors, but need to be added to the GL in SMCP:<BR>";
		int iMissingGLCount = 0;
		ResultSet rsMissingGLs = null;
		try {
			rsMissingGLs = stmtSMCP.executeQuery(SQL);
			while (rsMissingGLs.next()){
				s+= rsMissingGLs.getString(sTempVendorsTable + "." + SMTableicvendors.sdefaultexpenseacct) + "<BR>";
				iMissingGLCount++;
			}
		} catch (Exception e) {
			throw new Exception("Error [1449767642] listing missing GL accounts - " + e.getMessage());
		}
		if (iMissingGLCount == 0){
			s += "(None)<BR>";
		}
		
		//List the vendors NOT in SMCP:
		SQL = "SELECT"
			+ " " + sTempVendorsTable + "." + SMTableicvendors.svendoracct
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sname
			+ " FROM " + sTempVendorsTable + " LEFT JOIN " + SMTableicvendors.TableName 
			+ " ON " + sTempVendorsTable + "." + SMTableicvendors.svendoracct + "=" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
			+ " WHERE ("
				+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " IS NULL)"
			+ ") ORDER BY " + sTempVendorsTable + "." + SMTableicvendors.svendoracct
		;
		
		ResultSet rsMissingVendors = null;
		s += "<BR><BR>The following vendors are in ACCPAC but not in SMCP - they are being added automatically:<BR>";
		try {
			rsMissingVendors = stmtSMCP.executeQuery(SQL);
			while (rsMissingVendors.next()){
				s+= rsMissingVendors.getString(SMTableicvendors.svendoracct) 
					+ " - " + rsMissingVendors.getString(SMTableicvendors.sname) + "<BR>";
			}
		} catch (Exception e) {
			throw new Exception("Error [1449767642] listing missing vendors - " + e.getMessage());
		}
		rsMissingVendors.close();
		
		//Now update the temporary vendor table with the correct bank IDS:
		SQL = "UPDATE " + sTempVendorsTable + " LEFT JOIN " + SMTablebkbanks.TableName
			+ " ON " + sTempVendorsTable + "." + "sbankname = " + SMTablebkbanks.TableName + "." + SMTablebkbanks.sshortname
			+ " SET " + sTempVendorsTable + "." + SMTableicvendors.ibankcode
			+ " = " + SMTablebkbanks.TableName + "." + SMTablebkbanks.lid
			+ " WHERE ("
				+ "(" + SMTablebkbanks.TableName + "." + SMTablebkbanks.lid + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmtUpdateBanks = cnSMCP.createStatement();
			stmtUpdateBanks.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449854730] updating bank IDs with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Update the temporary vendor table with the correct account set IDs:
		SQL = "UPDATE " + sTempVendorsTable + " LEFT JOIN " + SMTableapaccountsets.TableName
			+ " ON " + sTempVendorsTable + "." + "sacctsetname = " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname
			+ " SET " + sTempVendorsTable + "." + SMTableicvendors.iapaccountset
			+ " = " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
			+ " WHERE ("
				+ "(" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmtUpdateAcctSets = cnSMCP.createStatement();
			stmtUpdateAcctSets.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449854731] updating AP account set IDs with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Update the temporary vendor table with the correct tax reporting 1099/CPRS IDs:
		SQL = "UPDATE " + sTempVendorsTable + " LEFT JOIN " + SMTableap1099cprscodes.TableName
			+ " ON " + sTempVendorsTable + "." + "staxreporting1099CPRScode = " + SMTableap1099cprscodes.TableName + "." + SMTableap1099cprscodes.sclassid
			+ " SET " + sTempVendorsTable + "." + SMTableicvendors.i1099CPRSid
			+ " = " + SMTableap1099cprscodes.TableName + "." + SMTableap1099cprscodes.lid
			+ " WHERE ("
				+ "(" + SMTableap1099cprscodes.TableName + "." + SMTableap1099cprscodes.lid + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmtUpdateAcctSets = cnSMCP.createStatement();
			stmtUpdateAcctSets.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1451591447] updating AP tax reporting 1099/CPRS IDs with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Update the temporary vendor table with the correct vendor group IDs:
		SQL = "UPDATE " + sTempVendorsTable + " LEFT JOIN " + SMTableapvendorgroups.TableName
			+ " ON " + sTempVendorsTable + "." + "svendorgroup = " + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid
			+ " SET " + sTempVendorsTable + "." + SMTableicvendors.ivendorgroupid
			+ " = " + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid
			+ " WHERE ("
				+ "(" + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmtUpdateAcctSets = cnSMCP.createStatement();
			stmtUpdateAcctSets.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1498829422] updating AP tax reporting 1099/CPRS IDs with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Add any vendors NOT in SMCP
		SQL = " INSERT INTO " + SMTableicvendors.TableName + "("
			+ SMTableicvendors.datlastmaintained
			+ ", " + SMTableicvendors.iactive
			+ ", " + SMTableicvendors.iaddedbyapconversion
			+ ", " + SMTableicvendors.iapaccountset
			+ ", " + SMTableicvendors.ibankcode
			+ ", " + SMTableicvendors.saddressline1
			+ ", " + SMTableicvendors.saddressline2
			+ ", " + SMTableicvendors.saddressline3
			+ ", " + SMTableicvendors.saddressline4
			+ ", " + SMTableicvendors.scity
			+ ", " + SMTableicvendors.scontactname
			+ ", " + SMTableicvendors.scountry
			+ ", " + SMTableicvendors.sdefaultdistributioncode
			+ ", " + SMTableicvendors.sdefaultexpenseacct
			+ ", " + SMTableicvendors.sfaxnumber
			+ ", " + SMTableicvendors.sgdoclink
			+ ", " + SMTableicvendors.slasteditedbyfullname
			+ ", " + SMTableicvendors.sname
			+ ", " + SMTableicvendors.sphonenumber
			+ ", " + SMTableicvendors.spostalcode
			+ ", " + SMTableicvendors.sprimaryremittocode
			+ ", " + SMTableicvendors.sstate
			+ ", " + SMTableicvendors.sterms
			+ ", " + SMTableicvendors.svendoracct
			+ ", " + SMTableicvendors.svendoremail
			+ ", " + SMTableicvendors.swebaddress
			+ ", " + SMTableicvendors.itaxreportingtype
			+ ", " + SMTableicvendors.staxidentifyingnumber
			+ ", " + SMTableicvendors.i1099CPRSid
			+ ", " + SMTableicvendors.itaxidnumbertype
			+ ") "
			+ " SELECT"
			+ " " + sTempVendorsTable + "." + SMTableicvendors.datlastmaintained
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.iactive
			+ ", 1" //SMTableicvendors.iaddedbyapconversion
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.iapaccountset
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.ibankcode
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.saddressline1
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.saddressline2
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.saddressline3
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.saddressline4
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.scity
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.scontactname
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.scountry
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sdefaultdistributioncode
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sdefaultexpenseacct
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sfaxnumber
			+ ", ''" //sgdoclink
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.slasteditedbyfullname
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sname
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sphonenumber
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.spostalcode
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sprimaryremittocode
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sstate
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.sterms
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.svendoracct
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.svendoremail
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.swebaddress
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.itaxreportingtype
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.staxidentifyingnumber
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.i1099CPRSid
			+ ", " + sTempVendorsTable + "." + SMTableicvendors.itaxidnumbertype
			
			+ " FROM " + sTempVendorsTable + " LEFT JOIN " + SMTableicvendors.TableName + " AS SMCPVEN"
			+ " ON " + sTempVendorsTable + "." + SMTableicvendors.svendoracct + "=SMCPVEN." + SMTableicvendors.svendoracct
			+ " WHERE ("
				+ "(SMCPVEN." + SMTableicvendors.svendoracct + " IS NULL)"
			+ ")"
		;
		try {
			Statement stmtAddVendors = cnSMCP.createStatement();
			stmtAddVendors.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449854732] adding vendors to SMCP with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Link the account set ID, bank ID, and default expense account, ETC to the vendor
		SQL = "UPDATE " + SMTableicvendors.TableName + " LEFT JOIN " + sTempVendorsTable
			+ " ON " + sTempVendorsTable + "." + SMTableicvendors.svendoracct + " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
			+ " SET " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.iapaccountset
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.ibankcode
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultdistributioncode
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.sdefaultdistributioncode
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultexpenseacct
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.sdefaultexpenseacct
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.itaxreportingtype
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.itaxreportingtype
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.staxidentifyingnumber
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.staxidentifyingnumber
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.i1099CPRSid
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.i1099CPRSid
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.itaxidnumbertype
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.itaxidnumbertype
			+", " + SMTableicvendors.TableName + "." + SMTableicvendors.ivendorgroupid
			+ " = " + sTempVendorsTable + "." + SMTableicvendors.ivendorgroupid
			
			+ " WHERE ("
				+ "(" +  sTempVendorsTable + "." + SMTableicvendors.svendoracct + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmtUpdateVendorFields = cnSMCP.createStatement();
			stmtUpdateVendorFields.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1449854733] updating AP vendor fields with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Drop the temporary table:
		SQL = "DROP TABLE " + sTempVendorsTable;
		try {
			stmtSMCP.execute(SQL);
		} catch (Exception e1) {
			//We don't care about errors here - the table may not have been created so errors don't matter.
		}
		
		return s;
	}
	private static String processUpdateVendorAddressesFromACCPAC(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType,
			String sConversionUser) throws Exception{
		String s = "";
		String SQL = "";
		long lVendorsUpdated = 0L;
		
		//Read all the ACCPAC vendor into the temporary vendors table:
		SQL = "SELECT * FROM APVEN";
		try {
			Statement stmtACCPAC = cnACCPAC.createStatement();
			ResultSet rsACCPACVendors = stmtACCPAC.executeQuery(SQL);
			while (rsACCPACVendors.next()){
				//Add this vendor to the temporary table:
				SQL = "UPDATE " + SMTableicvendors.TableName + " SET "
					+ " " + SMTableicvendors.saddressline1 + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE1").trim()) + "'" //SMTableicvendors.saddressline1 + "'"
					+ ", " + SMTableicvendors.saddressline2 + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE2").trim()) + "'" //SMTableicvendors.saddressline2 + "'"
					+ ", " + SMTableicvendors.saddressline3 + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE3").trim()) + "'" //SMTableicvendors.saddressline3 + "'"
					+ ", " + SMTableicvendors.saddressline4 + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTSTRE4").trim()) + "'" //SMTableicvendors.saddressline4 + "'"
					+ ", " + SMTableicvendors.scity + " = '" + FormatSQLStatement(rsACCPACVendors.getString("NAMECITY").trim()) + "'" //SMTableicvendors.scity
					+ ", " + SMTableicvendors.scontactname + " = '" + FormatSQLStatement(rsACCPACVendors.getString("NAMECTAC").trim()) + "'" //SMTableicvendors.scontactname
					+ ", " + SMTableicvendors.scountry + " = '" + FormatSQLStatement(rsACCPACVendors.getString("CODECTRY").trim()) + "'" //SMTableicvendors.scountry
					//+ ", " + SMTableicvendors.sdefaultdistributioncode
					//+ ", " + SMTableicvendors.sdefaultexpenseacct
					+ ", " + SMTableicvendors.sfaxnumber + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTPHON2").trim()) + "'" //SMTableicvendors.sfaxnumber
					//+ ", " + SMTableicvendors.sgdoclink
					//+ ", " + SMTableicvendors.slasteditedby
					+ ", " + SMTableicvendors.sname + " = '" + FormatSQLStatement(rsACCPACVendors.getString("VENDNAME").trim()) + "'" //SMTableicvendors.sname
					+ ", " + SMTableicvendors.sphonenumber + " = '" + FormatSQLStatement(rsACCPACVendors.getString("TEXTPHON1").trim()) + "'" //SMTableicvendors.sphonenumber
					+ ", " + SMTableicvendors.spostalcode + " = '" + FormatSQLStatement(rsACCPACVendors.getString("CODEPSTL").trim()) + "'" //SMTableicvendors.spostalcode
					+ ", " + SMTableicvendors.sstate + " = '" + FormatSQLStatement(rsACCPACVendors.getString("CODESTTE").trim()) + "'" //SMTableicvendors.sstate
					//+ ", " + SMTableicvendors.sterms
					//+ ", " + SMTableicvendors.svendoracct
					+ ", " + SMTableicvendors.swebaddress + " = '" + FormatSQLStatement(rsACCPACVendors.getString("WEBSITE").trim()) + "'" //SMTableicvendors.swebaddress
					+ ", " + SMTableicvendors.svendoremail + " = '" + FormatSQLStatement(rsACCPACVendors.getString("EMAIL").trim()) + "'" //SMTableicvendors.svendoremail
					+ ", " + SMTableicvendors.sprimaryremittocode + " = '" + FormatSQLStatement(rsACCPACVendors.getString("PRIMRMIT").trim()) + "'" //SMTableicvendors.sprimaryremittocode
					//+ ", " + SMTableicvendors.itaxreportingtype
					//+ ", " + SMTableicvendors.staxidentifyingnumber
					//+ ", " + SMTableicvendors.itaxidnumbertype
					//+ ", " + SMTableicvendors.igenerateseparatepaymentsforeachinvoice
					+ " WHERE ("
						+ " (" + SMTableicvendors.svendoracct + " = '" + rsACCPACVendors.getString("VENDORID").trim() + "')"
					+ ")"
					;
				try {
					Statement stmtSMCP = cnSMCP.createStatement();
					lVendorsUpdated += stmtSMCP.executeUpdate(SQL);
				} catch (Exception e1) {
					throw new Exception("Error [1514909107] updating vendor addresses with SQL: " + SQL + " - " + e1.getMessage());
				}
			}
			rsACCPACVendors.close();
		} catch (Exception e) {
			throw new Exception("Error [1514909108] reading ACCPAC vendors to update addresses - " + e.getMessage());
		}
		s += Long.toString(lVendorsUpdated) + " vendor addresses were updated from ACCPAC.<BR>";
		return s;
	}

	public static String FormatSQLStatement(String s) {

		if (s != null){
			s = s.replace("'", "''");
			s = s.replace("\\", "\\\\");
			//s = s.replace("\"", "\"\"");
		}

		return s;
	}
	private static String convertACCPACLongDateToString(long lDate, boolean bUseNowForNulls){

		if (lDate == 0L){
			if (bUseNowForNulls){
				return now("yyyy-MM-dd");
			}else{
				return "0000-00-00";
			}
		}

		String sDate = Long.toString(lDate);
		return sDate.substring(0, 4) + "-" + sDate.substring(4, 6) + "-" + sDate.substring(6, 8);
	}
	public static String now(String sDateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
		return sdf.format(cal.getTime());

		/*
		 Samples:
		 System.out.println(DateUtils.now("dd MMMMM yyyy"));
		 System.out.println(DateUtils.now("yyyyMMdd"));
		 System.out.println(DateUtils.now("dd.MM.yy"));
		 System.out.println(DateUtils.now("MM/dd/yy"));
		 System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
		 System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
		 System.out.println(DateUtils.now("h:mm a"));
		 System.out.println(DateUtils.now("H:mm:ss:SSS"));
		 System.out.println(DateUtils.now("K:mm a,z"));
		 System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
		 */
	}
}
