package smcontrolpanel;

//import java.io.ByteArrayInputStream;
//import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//import javax.swing.text.BadLocationException;
//import javax.swing.text.Document;
//import javax.swing.text.rtf.RTFEditorKit;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import ServletUtilities.clsDatabaseFunctions;

public class SMUpdateData extends java.lang.Object{

	private static final int m_CurrentDatabaseVersion = 1370;
	private static final String m_sVersionNumber = "1.4";
	private static final String m_sLastRevisionDate = "4/4/2019";
	private static final String m_sCopyright = "Copyright 2003-2019 AIRO Tech OMD, Inc.";

	private String m_sErrorMessage;
	private String m_sSuccessMessage;
	private int m_iReadDatabaseVersion;
	private SMLogEntry log;
	private boolean bDebugMode = false;

	public String getM_sErrorMessage() {
		if (! m_sErrorMessage.equalsIgnoreCase("")){
			return m_sErrorMessage;
		}else{
			return m_sSuccessMessage;
		}
	}
	public SMUpdateData(
	){
		m_sErrorMessage = "";
	}
	public boolean update(
			Connection conn,
			String sUserID,
			String sDBID
	){

		//Initialize the log entry:
		log = new SMLogEntry(conn);

		//Update data:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessage = "Could not start data transaction.";
			return false;
		}
		try {
			updateData (conn, sUserID, sDBID);
		} catch (Exception e) {
			m_sErrorMessage = e.getMessage();
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			m_sErrorMessage = "Could not commit data transaction.";
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		//Log the entry:
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMUPDATEDATA, 
				"Security Functions Successfully Updated", "",
				"[1376509362]"
		);

		m_sSuccessMessage = "Security Functions Successfully Updated";

		return true;
	}

	private void updateData (Connection conn, String sUserID, String sDBID) throws Exception{

		//First, update the data
		//Store the version we started with so we can show it later :
		try {
			m_iReadDatabaseVersion = getReadDatabaseVersion(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

		//Update a local variable we can use in the loop :
		int iReadDatabaseVersion = m_iReadDatabaseVersion;
		
		while (iReadDatabaseVersion < m_CurrentDatabaseVersion){
			//Log the entry:
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMUPDATEDATA, 
					"Preparing to update database version from "
					+ iReadDatabaseVersion, "",
					"[1376509363]"
			);			

			if (!processDataChanges(conn, iReadDatabaseVersion, sUserID)){
				throw new Exception("Error [1403021282] - " + m_sErrorMessage);
			}

			if (bDebugMode){
				System.out.println("In " + this.toString() 
					+ " - processDataChanges completed with iReadDatabaseVersion = " + iReadDatabaseVersion);
			}
			try {
				iReadDatabaseVersion = getReadDatabaseVersion(conn);
			} catch (Exception e) {
				throw new Exception("Error [1403021283] - Could not read database revision number in while loop - " + e.getMessage());
			}
		}
		//Next, update the security functions
		try {
			updateSecurityFunctions(conn, sDBID);
		} catch (Exception e) {
			throw new Exception("Error [1403021285] - " + e.getMessage());
		}
	}
	private boolean execUpdate(String sUserID, String sSQL, Connection conn, int iCurrentSysDatabaseVersion){
		
		//Log the entry:
		log.writeEntry(			
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMUPDATEDATA, 
				"SQL ATTEMPTED update statement from version "
				+ m_iReadDatabaseVersion + " to "
				+ Integer.toString(m_CurrentDatabaseVersion), sSQL,
				"[1376509358]"
		);
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
		}catch (SQLException e){
			m_sErrorMessage = "SQL Error updating from database version " 
				+ iCurrentSysDatabaseVersion
				+ ": " + e.getMessage()
				+ " - SQL: " + sSQL;
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMUPDATEDATA, 
					"SQL FAILED update statement from version "
					+ m_iReadDatabaseVersion + " to "
					+ Integer.toString(m_CurrentDatabaseVersion)
					+ " - " + e.getMessage(),
					sSQL,
					"[1376509359]"
			);
			return false;
		}
		
		return true;
	}
	private void updateSecurityFunctions (Connection conn, String sDBID) throws Exception{

		//First, remove all the current security functions:
		//String SQL = "DELETE FROM " + SMTablesecurityfunctions.TableName;
		//try{
		//	Statement stmt = conn.createStatement();
		//	stmt.execute(SQL);
		//}catch(SQLException e){
		//	throw new Exception("Could not clear all security functions before re-inserting - error [1403021590] - " + e.getMessage());
		//}

		String SQL = "";
		//Next, insert/update all the functions:
		SMSystemFunctions sys = new SMSystemFunctions(sDBID);
		for (int i = 0; i < sys.getSecurityFunctionCount() ;i++){
			SQL = "INSERT INTO " + SMTablesecurityfunctions.TableName + "("
			+ SMTablesecurityfunctions.iFunctionID
			+ ", " + SMTablesecurityfunctions.sFunctionName
			+ ", " + SMTablesecurityfunctions.slink
			+ ", " + SMTablesecurityfunctions.sDescription
			+ ", " + SMTablesecurityfunctions.imodulelevelsum
			+ ") VALUES ("
			+ Long.toString(sys.getSecurityFunctionID(i))
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunction(i)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunctionLink(i)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunctionDescription(i)) + "'"
			+ ", " + sys.getSecurityFunctionModuleLevel(i)
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesecurityfunctions.sFunctionName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunction(i)) + "'"
			+ ", " + SMTablesecurityfunctions.slink + " = '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunctionLink(i)) + "'"
			+ ", " + SMTablesecurityfunctions.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(sys.getSecurityFunctionDescription(i)) + "'"
			+ ", " + SMTablesecurityfunctions.imodulelevelsum + " = " + sys.getSecurityFunctionModuleLevel(i)
			;
			//System.out.println("[1467129107] " + sys.getSecurityFunctionID(i) + " = " + sys.getSecurityFunctionModuleLevel(i));
			try{
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch(Exception e){
				throw new Exception("Could not insert function ID " + sys.getSecurityFunctionID(i) + "with SQL: " + SQL 
					+ " - error [1403021591] - " + e.getMessage());
			}
		}

		try {
			//Now remove the ones that are no longer in the list:
			SQL = "SELECT"
				+ " " + SMTablesecurityfunctions.iFunctionID
				+ " FROM " + SMTablesecurityfunctions.TableName
				+ " ORDER BY " + SMTablesecurityfunctions.iFunctionID
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				boolean bFunctionWasNotFound = true;
				for (int i = 0; i < sys.getSecurityFunctionCount() ;i++){
					if (sys.getSecurityFunctionID(i) == rs.getLong(SMTablesecurityfunctions.iFunctionID)){
						bFunctionWasNotFound = false;
						break;
					}
				}
				if (bFunctionWasNotFound){
					//Remove it and restart:
					SQL = "DELETE FROM " + SMTablesecurityfunctions.TableName
						+ " WHERE ("
							+ "(" + SMTablesecurityfunctions.iFunctionID + " = " 
							+ Long.toString(rs.getLong(SMTablesecurityfunctions.iFunctionID)) + ")"
						+ ")"
					;
					System.out.println("[1467142979] - SQL = " + SQL);
					try {
						Statement stmt = conn.createStatement();
						stmt.execute(SQL);
					} catch (Exception e) {
						throw new Exception("Could not delete function ID " + Long.toString(rs.getLong(SMTablesecurityfunctions.iFunctionID))
							+ " - " + e.getMessage()
						);
					}
				}
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Could not list existing security group functions to remove orphans with SQL: " + SQL 
					+ " - error [1403021592] - " + e1.getMessage());
		}
		
		//Next, we have to remove any SecurityGroupFunctions that refer to obsolete functions:
		SQL = "DELETE " + SMTablesecuritygroupfunctions.TableName 
		+ " FROM ("
		+ SMTablesecuritygroupfunctions.TableName
		+ " LEFT JOIN " + SMTablesecurityfunctions.TableName + " AS functions"
		+ " ON " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
		+ " =  functions." + SMTablesecurityfunctions.iFunctionID
		+ ")"
		+ " WHERE ((functions." + SMTablesecurityfunctions.iFunctionID + ") Is Null)"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch(SQLException e){
			throw new Exception("Could not delete orphaned security group functions with SQL: " + SQL 
				+ " - error [1403021592] - " + e.getMessage());
		}

		//Next, update the function names in the securitygroupfunctions table:
		SQL = "UPDATE " + SMTablesecurityfunctions.TableName
		+ " INNER JOIN " + SMTablesecuritygroupfunctions.TableName
		+ " ON " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.iFunctionID
		+ " = " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
		+ " SET " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sFunction
		+ " = " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.sFunctionName
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch(SQLException e){
			throw new Exception("Could not update security group function names with SQL: " + SQL 
				+ " - error [1403021593] - " + e.getMessage());
		}
	}

	private int getReadDatabaseVersion (Connection conn) throws Exception {

		int iReadDatabaseVersion = -1;
		try{
			//System.out.println("SELECT * FROM " + SMTablecompanyprofile.TableName);
			ResultSet rs = clsDatabaseFunctions.openResultSet("SELECT * FROM " + SMTablecompanyprofile.TableName, conn);
			if (rs.next()){
				iReadDatabaseVersion = rs.getInt(SMTablecompanyprofile.iDatabaseVersion);
			}else{
				throw new Exception("Error [1403020486]: no database revision number record");
			}
			rs.close();
		}catch (SQLException e){
			throw new Exception("Error [1403020487] in " + this.toString() 
				+ ".getReadDatabaseVersion - getting database revision number - " + e.getMessage());
		}
		if (iReadDatabaseVersion == -1){
			throw new Exception("Error [1403020497] in " + this.toString() 
				+ ".getReadDatabaseVersion - database revision number is at -1.");
		}
		return iReadDatabaseVersion;
	}

	public String getErrorMessage (){
		if (! m_sErrorMessage.equalsIgnoreCase("")){
			return m_sErrorMessage;
		}else{
			return m_sSuccessMessage;
		}
	}
	public static int getDatabaseVersion (){
		return m_CurrentDatabaseVersion;
	}
	public static String getProgramVersion (){
		return m_sVersionNumber;
	}
	public static String getLastRevisionDate (){
		return m_sLastRevisionDate;
	}
	public static String getCopyright (){
		return m_sCopyright;
	}
	/*
	private String convertRTFToText(String sRTF) throws IOException, BadLocationException{

		if (sRTF == null){
			return "";
		}
		RTFEditorKit rtfParser = new RTFEditorKit();
		Document document = rtfParser.createDefaultDocument();

		byte[] rtfBytes = sRTF.getBytes();
		String sText = "";
		try {
			rtfParser.read(new ByteArrayInputStream(rtfBytes), document, 0);
			//Apparently if the text is NOT RTF, the document.getLength returns zero, so we have to trap that:
			if (document.getLength() > 0){
				sText =  document.getText(0, document.getLength());
			}else{
				sText = sRTF;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return sText;
	}
	*/
	private boolean processDataChanges(Connection conn, int iSystemDatabaseVersion, String sUser){

		String SQL = "";
		int iVersionUpdatedTo = -1;

		if (iSystemDatabaseVersion < 410){
			iVersionUpdatedTo = 410;
			//Set the database version to 410 to bring it up to the minimum version:
			//Now update the database version in the data:
			SQL = "UPDATE " + SMTablecompanyprofile.TableName
			+ " SET " + SMTablecompanyprofile.iDatabaseVersion
			+ " = " + Integer.toString(iVersionUpdatedTo);
			try{
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch (SQLException e){
				m_sErrorMessage = "Error [1403020637] - SQL Error updating data with version " + Integer.toString(iVersionUpdatedTo)
				+ ": " + e.getMessage();
				return false;
			}
			
		}
		switch (iSystemDatabaseVersion){
		//BEGIN CASE:
		
		/*
		
		case 410:
			//test update:
			SQL = "ALTER TABLE CallSheets add column vtest varchar(10)";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 411;
			break;
			//END CASE:

			//BEGIN CASE:
		case 411:
			//test update:
			SQL = "ALTER TABLE CallSheets drop column vtest";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 420;
			break;
			//END CASE:

			//BEGIN CASE:
		case 420:
			//test update:
			SQL = "ALTER TABLE smoptions add column `sicglexportpath` varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 421;
			break;
			//END CASE:

			//BEGIN CASE:
		case 421:
			SQL = "ALTER TABLE OrderHeaders add column `iSalesGroup` int(11) default 0";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "ALTER TABLE InvoiceHeaders add column `iSalesGroup` int(11) default 0";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = " CREATE TABLE `SalesGroups` (" + 
			"`iSalesGroupId` int(11) NOT NULL default '0', " +
			"`sSalesGroupCode` varchar(8) default '', " +
			"`sSalesGroupDesc` varchar(255) default '', " +
			"PRIMARY KEY  (`iSalesGroupId`) " +
			") ENGINE=InnoDB";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = 422;
			break;
			//END CASE:

			//BEGIN CASE:
		case 422:
			SQL = "ALTER TABLE SMOptions add column `stimecarddatabase` varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = 423;
			break;
			//END CASE:

			//BEGIN CASE:
		case 423:
			SQL = "ALTER TABLE Locations add column `sglinventoryacct` varchar(45) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE Locations add column `sglpayableclearingacct` varchar(45) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE Locations add column `sglwriteoffacct` varchar(45) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = 424;
			break;
			//END CASE:

			//BEGIN CASE:
		case 424:
			SQL = "create table `invoicemgrcomments` ("
				+ " `id` int(11) NOT NULL auto_increment,"
				+ " `sinvoicenumber` varchar(15) NOT NULL default '',"
				+ " `llinenumber` int(11) NOT NULL default '-1',"
				+ " `datlastedited` datetime NOT NULL default '0000-00-00 00:00:00',"
				+ " `suser` varchar(128) NOT NULL default '',"
				+ " `susername` varchar(102) NOT NULL default '',"
				+ " `mcomment` text NOT NULL default '',"
				+ " PRIMARY KEY (`id`),"
				+ " KEY `invnumkey` (`sinvoicenumber`)"
				+ ") ENGINE=InnoDB";
			//System.out.println("In " + this.toString() + ".processDataChanges - updating to 425, SQL = " + SQL);
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage);
				return false;
			}

			iVersionUpdatedTo = 425;
			break;
			//END CASE:

			//BEGIN CASE:
		case 425:

			SQL = "create table `ordermgrcomments` ("
				+ " `id` int(11) NOT NULL auto_increment,"
				+ " `sordernumber` varchar(22) NOT NULL default '',"
				+ " `sinvoicenumber` varchar(15) NOT NULL default '',"
				+ " `datlastedited` datetime NOT NULL default '0000-00-00 00:00:00',"
				+ " `suser` varchar(128) NOT NULL default '',"
				+ " `susername` varchar(102) NOT NULL default '',"
				+ " `mcomment` text NOT NULL,"
				+ " PRIMARY KEY (`id`),"
				+ " KEY `ordnumkey` (`sordernumber`),"
				+ " KEY `invnumkey` (`sinvoicenumber`)"
				+ ") ENGINE=InnoDB";

			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}else{
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER table `invoicemgrcomments` drop column llinenumber";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 426;
			break;
			//END CASE:

			//BEGIN CASE:
		case 426:
			SQL = "ALTER table `SMOptions` add column sorderdocspath varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 427;
			break;
			//END CASE:

			//BEGIN CASE:
		case 427:
			SMUtilities.start_data_transaction(conn);

			SQL = "RENAME table `JobCost` to `JobCostOld`;";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					SMUtilities.rollback_data_transaction(conn);
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "CREATE TABLE `JobCost` ("
				+ "`ID` int(11) AUTO_INCREMENT,"
				+ " `sJobNumber` varchar(8) NOT NULL default '',"
				+ " `datDate` datetime default NULL,"
				+ " `sMechanic` varchar(50) default '',"
				+ " `dQtyofHours` double default '0',"
				+ " `sJobType` varchar(64) default '',"
				+ " `sDESC` varchar(255) default '',"
				+ " `dTravelHours` double NOT NULL default '0',"
				+ " `decBackChargeHours` decimal(6,2) default '0.00',"
				+ " `slasteditedby` varchar(50) NOT NULL default '',"
				+ " `dattimelastedit` datetime NOT NULL default '0000-00-00 00:00:00',"
				+ " PRIMARY KEY  (`ID`),"
				+ " KEY JobNumberKey (`sJobNumber`)"
				+ " ) ENGINE=InnoDB;";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					SMUtilities.rollback_data_transaction(conn);
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "INSERT INTO `JobCost` ("
				+ "`sJobNumber`,"
				+ " `datDate`,"
				+ " `sMechanic`,"
				+ " `dQtyofHours`,"
				+ " `sJobType`,"
				+ " `sDESC`,"
				+ " `dTravelHours`,"
				+ " `decBackChargeHours`"
				+ ") SELECT"
				+ " `sJobNumber`,"
				+ " `datDate`,"
				+ " `sMechanic`,"
				+ " `dQtyofHours`,"
				+ " `sJobType`,"
				+ " `sDESC`,"
				+ " `dTravelHours`,"
				+ " `decBackChargeHours`"
				+ " FROM `JobCostOld`";
			//System.out.println(SQL);
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					SMUtilities.rollback_data_transaction(conn);
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "DROP TABLE `JobCostOld`;";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					SMUtilities.rollback_data_transaction(conn);
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SMUtilities.commit_data_transaction(conn);

			iVersionUpdatedTo = 428;
			break;
			//END CASE:

			//BEGIN CASE:
		case 428:
			SQL = "ALTER table `SMOptions` add column sbiddocspath varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 429;
			break;
			//END CASE:

			//BEGIN CASE:
		case 429:
			SQL = "ALTER table `SMOptions` add column sarchivedatabasename varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "ALTER table `CompanyProfile` drop column dDatabaseVersion";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "ALTER table `CompanyProfile` drop column test11";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				//In case field is not there, don't return false:
				//return false;
			}
			SQL = "ALTER table `CompanyProfile` drop column test12";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				//In case field is not there, don't return false:
				//return false;
			}
			iVersionUpdatedTo = 430;
			break;
			//END CASE:

			//BEGIN CASE:
		case 430:
			SQL = "ALTER table `aroptions` add column iflagimports int(11) NOT NULL default '1'";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "UPDATE `aroptions` set iflagimports = 1";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "ALTER table `icoptions` add column iflagimports int(11) NOT NULL default '1'";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			SQL = "UPDATE `icoptions` set iflagimports = 1";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 431;
			break;
			//END CASE:

			//BEGIN CASE:
		case 431:
			SQL = "ALTER table `SalesContacts` add column sdescription varchar(128) default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 432;
			break;
			//END CASE:

			//BEGIN CASE:
		case 432:
			SQL = "ALTER table `Bids` add column saltphonenumber varchar(20) default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 433;
			break;
			//END CASE:

			//BEGIN CASE:
		case 433:
			SQL = "ALTER table `Bids` add column sfaxnumber varchar(20) default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}
			iVersionUpdatedTo = 434;
			break;
			//END CASE:

			//BEGIN CASE:
		case 434:
			SQL = "alter table Locations modify column sAddress1 varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sAddress2 varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sAddress3 varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sAddress4 varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sCity varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sState varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sZip varchar(15) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sCountry varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sPhone varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sFax varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sSecondOfficeName varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sSecondOfficePhone varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sContact varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sLogo varchar(255) NOT NULL default 'YES'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sTollFreeNumber varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "alter table Locations modify column sWebSite varchar(50) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 435:
			SQL = "alter table OHDCustomDBConnectionString add column sconnectstring varchar(255) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "update OHDCustomDBConnectionString set sconnectstring = sDatabaseConnectionString";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 436:
			SQL = "alter table glaccounts add column lactive int(11) NOT NULL default '1'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "update OHDCustomDBConnectionString set sconnectstring = sDatabaseConnectionString";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 437:
			SQL = "ALTER table `Locations` add column sgltransferclearingacct varchar(45) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 438:
			SQL = "ALTER table `InvoiceDetails` add column lictransactionid int(11) NOT NULL default '-1'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 439:
			SQL = "ALTER table `aroptions` add column iexportto int(11) NOT NULL default '0'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 440:
			SQL = "ALTER table `icoptions` add column iexportto int(11) NOT NULL default '0'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 441:
			SQL = "ALTER table `Locations` add column sremittocompanydescription varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittoaddress1 varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittoaddress2 varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittoaddress3 varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittoaddress4 varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittocity varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittocontact varchar(60) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittostate varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittozip varchar(15) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittocountry varchar(30) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittophone varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "ALTER table `Locations` add column sremittofax varchar(20) NOT NULL default ''";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			//Add update statements to set values for the remit to's:
			SQL = "UPDATE `Locations` SET"
				+ " sremittocompanydescription = sCompanyDescription"
				+ ", sremittoaddress1 = sAddress1"
				+ ", sremittoaddress2 = sAddress2"
				+ ", sremittoaddress3 = sAddress3"
				+ ", sremittoaddress4 = sAddress4"
				+ ", sremittocity = sCity"
				+ ", sremittocontact = sContact"
				+ ", sremittostate = sState"
				+ ", sremittozip = sZip"
				+ ", sremittocountry = sCountry"
				+ ", sremittophone = sPhone"
				+ ", sremittofax = sFax"
				;

			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 442:
			SQL = "CREATE TABLE bidproducttypes ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ "  , sproducttype varchar(72) NOT NULL DEFAULT ''"
				+ "  , PRIMARY KEY (lid)"
				+ "  , UNIQUE KEY producttypekey (sproducttype)"
				+ ") ENGINE=INNODB"
				;

			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			SQL = "CREATE TABLE bidproductamounts ("
				+ "  lbidid int(11) NOT NULL DEFAULT '0'"
				+ "  , lbidproducttypeid int(11) NOT NULL DEFAULT '0'"
				+ "  , bdamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ "  , UNIQUE KEY bidandtypekey (lbidid, lbidproducttypeid)"
				+ ") ENGINE=INNODB"
				;

			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 443:
			SQL = "CREATE TABLE labelprinters ("
				+ " lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sname varchar(32) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(254) NOT NULL DEFAULT ''"
				+ ", shost varchar(64) NOT NULL DEFAULT ''"
				+ ", iport int(11) NOT NULL DEFAULT '9100'"
				+ "  , PRIMARY KEY (lid)"
				+ "  , UNIQUE KEY namekey (sname)"
				+ ") ENGINE=INNODB"
				;

			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 444:

			SQL = "ALTER table `arcustomers` add column iuseselectronicdeposit int(11) NOT NULL default '0'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

		case 445:

			SQL = "ALTER table `labelprinters` add column itopmargin int(11) NOT NULL default '0'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}
			SQL = "ALTER table `labelprinters` add column ileftmargin int(11) NOT NULL default '0'";
			try{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			}catch (Exception ex) {
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ " with SQL: " + SQL + " - " + ex.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 446:
			//Added 4/28/2011 - TJR:
			SQL = "ALTER TABLE labelprinters add column `sfont` varchar(8) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE labelprinters add column `ibarcodewidth` int(11) NOT NULL default '0'";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE labelprinters add column `ibarcodeheight` int(11) NOT NULL default '0'";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

		case 447:
			//Added 5/2/2011 - TJR:			
			SQL = "ALTER TABLE labelprinters add column `idarkness` int(11) NOT NULL default '0'";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

		case 448:
			//Added 5/12/2011 - TJR:			
			SQL = "ALTER TABLE icvendoritems add column `scomment` varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE icvendors add column `scompanyaccountcode` varchar(64) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE icvendors add column `swebaddress` varchar(128) NOT NULL default ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE:

			//BEGIN CASE:
		case 449:
			//Added 5/13/2011 - TJR:			
			SQL = "ALTER TABLE armatchinglines add key `ldocappliedtoid_key` (ldocappliedtoid)";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

			//BEGIN CASE:
		case 450:
			//Added 5/19/2011 - TJR:			
			SQL = "ALTER TABLE SMOptions add column `sorderdocsftpurl` varchar(128) NOT NULL DEFAULT ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			SQL = "ALTER TABLE SMOptions add column `sbiddocsftpurl` varchar(128) NOT NULL DEFAULT ''";
			try{
				if (!SMUtilities.executeSQL(SQL, conn)){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
				}
			}catch (SQLException e){
				m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
				+ ": " + e.getMessage();
				return false;
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

			//BEGIN CASE:
		case 451:
			//Added 6/16/2011 - TJR:
			for (int i = 1; i <= 30; i++){
				String sFieldName = "Payroll " + SMUtilities.PadLeft(Integer.toString(i), "0", 2);
				SQL = "ALTER TABLE Projects drop column `" + sFieldName + "`";
				try{
					if (!SMUtilities.executeSQL(SQL, conn)){
						m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion;
					}
				}catch (SQLException e){
					m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
					+ ": " + e.getMessage();
					return false;
				}
			}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

			//BEGIN CASE:
		case 452:
			//Added 6/20/2011 - TJR:
			SQL = "ALTER TABLE OrderHeaders add column datcontractreceived datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column datshopdrawingssubmitted datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column datshopdrawingsapproved datetime NOT NULL DEFAULT '0000-00-00  00:00:00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column bdtruckdays DECIMAL(17,4) NOT NULL DEFAULT '0.00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column scarpenterrate varchar(50) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column slaborerrate varchar(50) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column selectricianrate varchar(50) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column bdtotalmarkup DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column bdtotalcontractamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column datwarrantyexpiration datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column swagescalenotes varchar(255) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column ssecondarybilltophone varchar(20) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE OrderHeaders add column ssecondaryshiptophone varchar(20) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			//Add some indexes to the Projects table to speed this up:
			SQL = "ALTER TABLE Projects ADD KEY jobnumberkey(`Job Number`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY contractreceivedkey(`Contract Received`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY shopdrawingssubmittedkey(`Shop Drawings Submitted`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY shopdrawingsapprovedkey(`Shop Drawings Approved`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY truckdayskey(`Truck Days`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY carpenterkey(`Carpenter`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY laborerkey(`Laborer`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY electriciankey(`Electrician`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY totalmukey(`Total MU`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY amountkey(`Amount`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY warrantyexpkey(`Warranty Exp`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY wagescalecommentskey(`sWageScaleComments`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY 2ndphonenumberkey(`2ndPhoneNumber`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY 2ndjobphonekey(`2ndJobPhone`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY projectmanagerkey(`Project Manager`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY phonenumberkey(`Phone Number`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY superintendentkey(`Superintendent`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY jobphonekey(`Job Phone`)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD sordernumber varchar(8) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE Projects SET sordernumber = LPAD(TRIM(`Job Number`), 8, ' ')";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "ALTER TABLE Projects ADD KEY ordernumberkey(sordernumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			//UPDATE THE CURRENT FIELDS:
			SQL = "UPDATE OrderHeaders RIGHT JOIN Projects ON"
				+ " OrderHeaders.sOrderNumber = Projects.sordernumber"
				+ " SET OrderHeaders.datcontractreceived = if((Projects.`Contract Received` IS NULL),'0000-00-00 00:00:00', IF(Projects.`Contract Received` < '1901-01-01 00:00:00', '0000-00-00 00:00:00', Projects.`Contract Received`))"
				+ ", OrderHeaders.datshopdrawingssubmitted = if((Projects.`Shop Drawings Submitted` IS NULL),'0000-00-00 00:00:00', IF(Projects.`Shop Drawings Submitted` < '1901-01-01 00:00:00', '0000-00-00 00:00:00', Projects.`Shop Drawings Submitted`))"
				+ ", OrderHeaders.datshopdrawingsapproved = if((Projects.`Shop Drawings Approved` IS NULL),'0000-00-00 00:00:00', IF(Projects.`Shop Drawings Approved` < '1901-01-01 00:00:00', '0000-00-00 00:00:00', Projects.`Shop Drawings Approved`))"
				+ ", OrderHeaders.bdtruckdays = if(Projects.`Truck Days` IS NULL, 0.00, Projects.`Truck Days`)"
				+ ", OrderHeaders.scarpenterrate = if(Projects.`Carpenter` IS NULL, '', Projects.`Carpenter`)"
				+ ", OrderHeaders.slaborerrate = if(Projects.`Laborer` IS NULL, '', Projects.`Laborer`)"
				+ ", OrderHeaders.selectricianrate = if(Projects.`Electrician` IS NULL, '', Projects.`Electrician`)"
				+ ", OrderHeaders.bdtotalmarkup = if(Projects.`Total MU` IS NULL, 0.00, Projects.`Total MU`)"
				+ ", OrderHeaders.bdtotalcontractamount = if(Projects.`Amount` IS NULL, 0.00, Projects.`Amount`)"
				+ ", OrderHeaders.datwarrantyexpiration = if((Projects.`Warranty Exp` IS NULL),'0000-00-00 00:00:00', IF(Projects.`Warranty Exp` < '1901-01-01 00:00:00', '0000-00-00 00:00:00', Projects.`Warranty Exp`))"
				+ ", OrderHeaders.swagescalenotes = if(Projects.`sWageScaleComments` IS NULL, '', Projects.`sWageScaleComments`)"
				+ ", OrderHeaders.ssecondarybilltophone = if(Projects.`2ndPhoneNumber` IS NULL, '', Projects.`2ndPhoneNumber`)"
				+ ", OrderHeaders.ssecondaryshiptophone = if(Projects.`2ndJobPhone` IS NULL, '', Projects.`2ndJobPhone`)"
				;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}			

			SQL = "UPDATE OrderHeaders RIGHT JOIN Projects ON"
				+ " OrderHeaders.sOrderNumber = Projects.sordernumber"
				+ " SET OrderHeaders.sBillToContact = Projects.`Project Manager`"
				+ " WHERE ("
				+ "TRIM(Projects.`Project Manager`) != ''"
				+ ")"
				;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}			

			SQL = "UPDATE OrderHeaders RIGHT JOIN Projects ON"
				+ " OrderHeaders.sOrderNumber = Projects.sordernumber"
				+ " SET OrderHeaders.sBillToPhone = Projects.`Phone Number`"
				+ " WHERE ("
				+ "TRIM(Projects.`Phone Number`) != ''"
				+ ")"
				;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			SQL = "UPDATE OrderHeaders RIGHT JOIN Projects ON"
				+ " OrderHeaders.sOrderNumber = Projects.sordernumber"
				+ " SET OrderHeaders.sShipToContact = Projects.`Superintendent`"
				+ " WHERE ("
				+ "TRIM(Projects.`Superintendent`) != ''"
				+ ")"
				;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			SQL = "UPDATE OrderHeaders RIGHT JOIN Projects ON"
				+ " OrderHeaders.sOrderNumber = Projects.sordernumber"
				+ " SET OrderHeaders.sShipToPhone = Projects.`Job Phone`"
				+ " WHERE ("
				+ "TRIM(Projects.`Job Phone`) != ''"
				+ ")"
				;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
		case 453:
			//Added 7/6/2011 - TJR:
			//Re-design the Call Sheets table:
			SQL = "alter table CallSheets DROP KEY AcctCallSheetName";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table CallSheets ADD UNIQUE KEY acctcallsheetname(sAcct, sCallSheetName)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table CallSheets DROP PRIMARY KEY";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table CallSheets add column id int(11) primary key auto_increment";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
		case 454:
			SQL = "alter table OrderHeaders add column mFieldNotes text";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
		case 455:
			//Added 8/4/2011 - TJR:
			//Update Call Sheets table:
			SQL = "SELECT * FROM CallSheets";
			try {
				ResultSet rs = SMUtilities.openResultSet(SQL, conn);
				while(rs.next()){
					SQL = "UPDATE CallSheets SET mNotes = '"
							+ SMUtilities.FormatSQLStatement(convertRTFToText(rs.getString("mNotes")))
							+ "' WHERE id = " + Long.toString(rs.getLong("id"))
							;
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){
						rs.close();
						return false;
					}
				}
				rs.close();
			} catch (SQLException e) {
				m_sErrorMessage = "SQL Error updating call sheet notes: " + e.getMessage()
					+ " - SQL: " + SQL;
				log.writeEntry(
						sUser, 
						"UPDATEDATA", 
						"SQL FAILED updating call sheet notes"
						+ " - " + e.getMessage(),
						SQL,
						"[1376509360]"
				);
				return false;
			} catch (IOException e) {
				m_sErrorMessage = "IO Error updating call sheet notes: " + e.getMessage();
			log.writeEntry(
					sUser, 
					"UPDATEDATA", 
					"IO FAILED updating call sheet notes"
					+ " - " + e.getMessage(),
					"",
					"[1376509361]"
			);
			return false;
			} catch (BadLocationException e) {
				m_sErrorMessage = "BadLocation Error updating call sheet notes: " + e.getMessage();
				log.writeEntry(
						sUser, 
						"UPDATEDATA", 
						"BadLocation FAILED updating call sheet notes"
						+ " - " + e.getMessage(),
						"",
						"[1376509517]"
				);
			}
			
			//Took these back out because SM still needed them - 8/6/2011 - TJR
			//Alter users table:
			//SQL = "alter table Users drop column iAllowCallSheetAccess";
			//if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			//SQL = "alter table Users drop column iLimitCallSheetAccessToUser";
			//if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			//Alter change orders table:
			SQL = "alter table `Change Orders` drop column bBilledInFull";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
		case 456:
			//Added by TJR - 8/5/2011:
			SQL = "alter table SMOptions add column sbackgroundcolor varchar(6) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE SMOptions set sbackgroundcolor = '" + SMUtilities.DEFAULT_BK_COLOR + "'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
		case 457:
			//Added by LTO - 8/11/2011:
			SQL = "alter table Bids add column iordersourceid int(9) DEFAULT 0";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table Bids add column sordersourcedesc varchar(255) DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
		case 458:
			//Added by TJR - 8/9/2011:
			SQL = "alter table JobCost add column sscheduledby varchar(128) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column ijoborder int(11) NOT NULL DEFAULT '0'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column slocation varchar(6) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column smechanicssn varchar(9) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column smechanicname varchar(50) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sshiptoname varchar(60) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sjobsalesperson varchar(3) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sbilltoname varchar(60) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sservicecode varchar(6) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sassistant varchar(75) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}			
			SQL = "alter table JobCost add column svehiclelabel varchar(10) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

		//BEGIN CASE:
		case 459:
			//Added by TJR - 8/17/2011:
			SQL = "alter table JobCost add column sschedulecomment varchar(255) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 460:
			//Added by TJR - 8/22/2011:
			SQL = "alter table TruckSchedule add column ljobcostid int(11) NOT NULL DEFAULT '0'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
	
		//BEGIN CASE:
		case 461:
			//Added by TJR - 8/23/2011:
			SQL = "alter table CallSheets modify column sCustomerName varchar(60) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table CallSheets modify column sCallSheetName varchar(75) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
		
		//BEGIN CASE:
		case 462:
			//Added by TJR - 8/25/2011:
			SQL = "alter table Mechanics add column sstartingtime varchar(10) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add column sstartingtime varchar(10) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 463:
			//Added by TJR - 9/2/2011:
			SQL = "alter table OrderHeaders add column strimmedordernumber varchar(22) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table OrderHeaders add KEY idxstrimmedordernumber (strimmedordernumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table InvoiceHeaders add column strimmedordernumber varchar(22) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table InvoiceHeaders add KEY idxstrimmedordernumber (strimmedordernumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table OrderDetails add column strimmedordernumber varchar(22) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table OrderDetails add KEY idxstrimmedordernumber (strimmedordernumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE OrderHeaders SET strimmedordernumber = TRIM(sOrderNumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE InvoiceHeaders SET strimmedordernumber = TRIM(sOrderNumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE OrderDetails, OrderHeaders SET OrderDetails.strimmedordernumber = "
				+ "TRIM(OrderHeaders.sOrderNumber) WHERE (OrderHeaders.dOrderUniqueifier = OrderDetails.dUniqueOrderID)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE

		//BEGIN CASE:
		case 464:
			//Added by TJR - 9/7/2011:
			SQL = "alter table CallSheets modify column sCollector varchar(8) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE CallSheets set sResponsibility = '' WHERE sResponsibility IS NULL";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table CallSheets modify column sResponsibility varchar(8) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE

		//BEGIN CASE:
		case 465:
			//Added by TJR - 9/8/2011:
			SQL = "alter table `Critical Dates` add column sassignedby varchar(8) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "UPDATE `Critical Dates`, OrderHeaders set `Critical Dates`.sassignedby = OrderHeaders.sSalesperson"
				+ " WHERE `Critical Dates`.`Job Number` = OrderHeaders.strimmedordernumber"
			;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
		//BEGIN CASE:
		case 466:
			//Added by TJR - 9/16/2011:
			SQL = "alter table JobCost add key datekey (datDate)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add key mechanicssnkey (smechanicssn)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost add key mechanickey (sMechanic)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table TruckSchedule add key mechanicssnkey (sMechanicSSN)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table TruckSchedule add key truckdatekey (datTruckDate)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table TruckSchedule add key jobnumberkey (sJobNumber)";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 467:
			//Added by TJR 9/24/2011:
			//First we clear all the links to job cost records in the Truck Schedule table:
			SQL = "UPDATE " + "TruckSchedule" 
				+ " SET " + "ljobcostid"
				+ " = 0"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				m_sErrorMessage = "Error clearing jobcost IDs from TruckSchedule with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}
			
			//Next delete ANY job cost records that were only created in a previous run of this function from
			//Truck Schedule records.  We can tell which ones they are because they will have NO hours and NO 
			// 'sDESC', which are entered when people add job cost hours:
			SQL = "DELETE FROM " + SMTablejobcost.TableName
			+ " WHERE ("
				+ "(" + SMTablejobcost.dQtyofHours + " = 0.00)"
				+ " AND (" + SMTablejobcost.dTravelHours + " = 0.00)"
				+ " AND (" + SMTablejobcost.decBackChargeHours + " = 0.00)"
				+ " AND (" + SMTablejobcost.mworkdescription + " = '')"
			+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				m_sErrorMessage = "Error clearing empty job cost records with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}
			
			//Next we clear any fields in the job cost table which may have been previously transferred from 
			//the truck schedule table - these are records that WERE created by real job cost entries, but 
			//that had fields updated from the Truck Schedule:
			SQL = "UPDATE " + SMTablejobcost.TableName + ", " + SMTablemechanics.TableName
				+ " SET " + SMTablejobcost.TableName + "." + SMTablejobcost.sschedulecomment + " = ''"
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.ijoborder + " = 0"
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn + " = "
				+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechSSN //MechSSNSCO
				+ " WHERE ("
					+ "(" + SMTablejobcost.TableName + "." + SMTablejobcost.sMechanic + " = "
					+ SMTablemechanics.sMechInitial + ")"
				+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				m_sErrorMessage = "Error clearing jobcost fields with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}

			//Now start updating current truck schedule records into the job cost records:
			SQL = "UPDATE " + "TruckSchedule" + ", " + SMTablejobcost.TableName
				+ " SET " + "TruckSchedule" + "." + "ljobcostid"
				+ " = " + SMTablejobcost.TableName + "." + SMTablejobcost.ID
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sschedulecomment + " = "
				+ "TruckSchedule" + "." + "sDESC"
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.ijoborder + " = "
				+ "TruckSchedule" + "." + "iJobOrder"
				
				+ " WHERE (" 
					
					//the dates match:
					+ "(" + "TruckSchedule" + "." + "datTruckDate" + " = "
					+ SMTablejobcost.TableName + "." + SMTablejobcost.datDate + ")"
					
					//We don't want any Truck schedule records without job numbers:
					+ " AND (" + "TruckSchedule" + "." + "sJobNumber" + " != '0')"
					
					//Match the job numbers:
					+ " AND (" + "TruckSchedule" + "." + "sJobNumber" + " = "
					+ SMTablejobcost.TableName + "." + SMTablejobcost.sJobNumber + ")"
					
					//Match the 'ijoborders' (job costs are all zero if they haven't been updated by Truck Schedule
					// records, but valid truck schedule records all start with '1',
					+ " AND (" + "TruckSchedule" + "." + "iJobOrder" + " = "
					+ SMTablejobcost.TableName + "." + SMTablejobcost.ijoborder + " + 1)"
					
					//Ignore truck schedule records with a '0' in the joborder:
					+ " AND (" + "TruckSchedule" + "." + "iJobOrder" + " != 0)"
					
					//Ignore truck schedule records with a '999' in the joborder:
					+ " AND (" + "TruckSchedule" + "." + "iJobOrder" + " != 999)"
					
					//Match the mechanics:
					+ " AND (" + "TruckSchedule" + "." + "sMechanicSSN" + " = "
					+ SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn + ")"
				+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				m_sErrorMessage = "Error updating jobcost from TruckSchedules with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}

			//NOW we insert into the Job Cost table all the Truck Schedule records that haven't matched up with any
			//Job Cost records:
			//NOTE - the Truck Schedule records used to update the jobcost records in this query
			//won't have their 'ijobcostid' updated at all.
			SQL = "INSERT INTO " + SMTablejobcost.TableName
				+ "("
				+ SMTablejobcost.TableName + "." + SMTablejobcost.datDate
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.ijoborder
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sJobNumber
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sschedulecomment
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sscheduledby
				+ ")"
				+ " SELECT "
				+ "TruckSchedule" + "." + "datTruckDate"
				+ ", " + "TruckSchedule" + "." + "iJobOrder"
				+ ", " + "TruckSchedule" + "." + "sJobNumber"
				+ ", " + "TruckSchedule" + "." + "sMechanicSSN"
				+ ", " + "TruckSchedule" + "." + "sDESC"
				+ ", " + "TruckSchedule" + "." + "sScheduledBy"
				+ " FROM " + "TruckSchedule"
				+ " WHERE ("
					+ "(" + "TruckSchedule" + "." + "ljobcostid" + " = 0)"
					+ " AND (" + "TruckSchedule" + "." + "iJobOrder" + " != 0)"
					+ " AND (" + "TruckSchedule" + "." + "iJobOrder" + " != 999)"
				+ ")"
				;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				m_sErrorMessage = "Error INSERTing jobcost records from TruckSchedules with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}

			//Update the mechanics's SSN here:
			SQL = "UPDATE " + SMTablejobcost.TableName + ", " + SMTablemechanics.TableName
				+ " SET " + SMTablejobcost.TableName + "." + SMTablejobcost.sassistant + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sAssistant
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechSSN //MechSSNSCO
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sstartingtime + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sstartingtime
				+ " WHERE ("
					+ "(" + SMTablejobcost.TableName + "." + SMTablejobcost.sMechanic + " = "
						+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial + ")"
					+ " AND (" + SMTablejobcost.TableName + "." + SMTablejobcost.sMechanic + " != '')"
				+ ")"
				;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e2) {
				m_sErrorMessage = "Error updating mechanic info on job cost table with SQL: " + SQL + " - " + e2.getMessage();
				return false;
			};

			//NOW, do the opposite: update mechanic's initials using the SSN, for any that came over w/o initials:
			SQL = "UPDATE " + SMTablejobcost.TableName + ", " + SMTablemechanics.TableName
				+ " SET " + SMTablejobcost.TableName + "." + SMTablejobcost.sassistant + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sAssistant
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sMechanic + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sstartingtime + "=" 
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sstartingtime
				+ " WHERE ("
				+ "(" + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn + " = "
					+ SMTablemechanics.TableName + "." + SMTablemechanics.sMechSSN + ")"  //MechSSNSCO
				+ " AND (" + SMTablejobcost.TableName + "." + SMTablejobcost.smechanicssn + " != '')"
			+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e2) {
				m_sErrorMessage = "Error updating mechanic info on job cost table with SQL: " + SQL + " - " + e2.getMessage();
				return false;
			};

			SQL = "UPDATE " + SMTablejobcost.TableName + ", " + SMTableorderheaders.TableName
				+ " SET " + SMTablejobcost.TableName + "." + SMTablejobcost.slocation + "=" 
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation
				+ ", " + SMTablejobcost.TableName + "." + SMTablejobcost.sservicecode + "=" 
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
				+ " WHERE ("
					+ "(" + SMTablejobcost.TableName + "." + SMTablejobcost.sJobNumber + " = "
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + ")"
				+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e2) {
				m_sErrorMessage = "Error updating order info on job cost table with SQL: " + SQL + " - " + e2.getMessage();
				return false;
			};
			
			//Finally, increment all the ijoborder's in JobCost by one so we no longer have any zero ijoborders:
			SQL = "UPDATE " + SMTablejobcost.TableName
				+ " SET " + SMTablejobcost.ijoborder + " = " + SMTablejobcost.ijoborder + " + 1"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e2) {
				m_sErrorMessage = "Error incrementing job order in job cost table with SQL: " + SQL + " - " + e2.getMessage();
				return false;
			};
			
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 468:
			//Added by TJR - 10/4/2011:
			SQL = "drop table TruckSchedule";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost drop column smechanicname";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost drop column sshiptoname";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost drop column sjobsalesperson";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost drop column sbilltoname";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			SQL = "alter table JobCost drop column svehiclelabel";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE

		//BEGIN CASE:
		case 469:
			//Added by TJR - 10/13/2011:
			SQL = "alter table OrderHeaders add column sgeocode varchar(64) NOT NULL DEFAULT ''";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 470:
			//Added by TJR - 10/25/2011:
			SQL = "alter table icporeceiptlines add column lpoinvoiceid int(11) NOT NULL DEFAULT '-1'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
		//END CASE
			
		//BEGIN CASE:
		case 471:
			//Added by TJR - 10/27/2011:
			SQL = "alter table icpoinvoicelines add column lporeceiptid int(11) NOT NULL DEFAULT '-1'";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

			//Update the po invoice lines with the receipt id:
			SQL = "UPDATE icpoinvoicelines LEFT JOIN icpoinvoiceheaders"
				+ " ON icpoinvoicelines.lpoinvoiceheaderid = icpoinvoiceheaders.lid"
				+ " set icpoinvoicelines.lporeceiptid = icpoinvoiceheaders.lreceiptid";
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			//Update the po receipt lines that have corresponding icpoinvoicelines:
			SQL = "UPDATE icporeceiptlines LEFT JOIN icpoinvoicelines ON icporeceiptlines.lid = "
				+ "icpoinvoicelines.lporeceiptlineid"
				+ " set icporeceiptlines.lpoinvoiceid = icpoinvoicelines.lpoinvoiceheaderid"
				+ " WHERE ("
				+ "(icpoinvoicelines.lpoinvoiceheaderid IS NOT NULL)"
				+ ")"
			;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			
			//Now there may be some po receipt lines whose header WAS actually invoiced, but there MAY be no invoice
			//in the system, so we have to update those with a ZERO:
			SQL = "UPDATE icporeceiptlines LEFT JOIN icporeceiptheaders ON icporeceiptlines.lreceiptheaderid = "
				+ "icporeceiptheaders.lid"
				+ " set icporeceiptlines.lpoinvoiceid = 0"
				+ " WHERE ("
					+ "(icporeceiptheaders.sinvoicenumber != '')"
					+ " AND (icporeceiptlines.lpoinvoiceid = -1)"
					+ " AND (icporeceiptheaders.lid IS NOT NULL)"
				+ ")"
			;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 472:
				//Added by LTO 2011/11/01:
				SQL = "alter table icitemlocations add column `bdminqtyonhand` decimal(17,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 473:
				//Added by TJR 2011/11/07:
				SQL = "alter table OrderHeaders add column `sclonedfrom` varchar(22) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE:
			case 474:
				//Added by TJR 2011/12/23:
				SQL = "alter table SalesContacts modify column salespersoncode varchar(8) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table `Critical Dates` modify column Responsible varchar(8) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 475:
				//Added by TJR 2011/12/23:
				SQL = "alter table OrderDetails add column isuppressdetailoninvoice int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table InvoiceDetails add column isuppressdetailoninvoice int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
			case 476:
				//Added by TJR 2012/1/23:
				SQL = "alter table icpoinvoiceheaders add column itaxclass int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpoinvoiceheaders add column staxgroup varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				

			//BEGIN CASE:
			case 477:
				//Added by TJR 2012/1/26:
				SQL = "alter table OrderDetails add column iprintondeliveryticket int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
			case 478:
				//Added by TJR 2012/1/30:
				SQL = "alter table Users add column smechanicinitials varchar(4) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE:
			case 479:
				//Added by TJR 2012/1/31:
				SQL = "alter table SecurityFunctions modify column slink text";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 480:
				//Added by TJR 2012/2/4:
				SQL = " CREATE TABLE `usergeocodes` ("
				+ " `id` int(11) NOT NULL auto_increment"
				+ ", `suser` varchar(32) NOT NULL default ''"
				+ ", `sfirstname` varchar(50) NOT NULL default ''"
				+ ", `slastname` varchar(50) NOT NULL default ''"
				+ ", `slatitude` varchar(32) NOT NULL default ''"
				+ ", `slongitude` varchar(32) NOT NULL default ''"
				+ ", `sspeed` varchar(32) NOT NULL default ''"
				+ ", `saltitude` varchar(32) NOT NULL default ''"
				+ ", `saccuracy` varchar(32) NOT NULL default ''"
				+ ", `saltitudeaccuracy` varchar(32) NOT NULL default ''"
				+ ", `dattimeentry` datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "PRIMARY KEY  (`id`) "
				+ ") ENGINE=MyISAM";

				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 481:
				//Added by TJR 2012/2/21:
				SQL = "alter table icpoinvoiceheaders add column datentered datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update icpoinvoiceheaders set datentered = datinvoice";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 482:
				//Added by TJR 2012/2/23:
				SQL = "update InvoiceHeaders set sSalesperson = '' WHERE sSalesperson = 'N/A'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update OrderHeaders set sSalesperson = '' WHERE sSalesperson = 'N/A'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				

			case 483:
				//Added by TJR 2012/3/21 to get rid of nulls in some of the old order header records:
				SQL = "update OrderHeaders set sShipToAddress1 = if(sShipToAddress1 IS NULL, '',sShipToAddress1)"
					+ ", sShipToAddress2 = if(sShipToAddress2 IS NULL, '',sShipToAddress2)"
					+ ", sShipToAddress3 = if(sShipToAddress3 IS NULL, '',sShipToAddress3)"
					+ ", sShipToAddress4 = if(sShipToAddress4 IS NULL, '',sShipToAddress4)"
					+ ", sShipToCity = if(sShipToCity IS NULL, '',sShipToCity)"
					+ ", sShipToState = if(sShipToState IS NULL, '',sShipToState)"
					+ ", sShipToZip = if(sShipToZip IS NULL, '',sShipToZip)"
					+ ", sShipToCountry = if(sShipToCountry IS NULL, '',sShipToCountry)"
					+ ", sShipToPhone = if(sShipToPhone IS NULL, '',sShipToPhone)"
					+ ", sShipToFax = if(sShipToFax IS NULL, '',sShipToFax)"
					+ ", sShipToContact = if(sShipToContact IS NULL, '',sShipToContact)"
					+ ", sBillToName = if(sBillToName IS NULL, '',sBillToName)"
					+ ", sBillToAddressLine1 = if(sBillToAddressLine1 IS NULL, '',sBillToAddressLine1)"
					+ ", sBillToAddressLine2 = if(sBillToAddressLine2 IS NULL, '',sBillToAddressLine2)"
					+ ", sBillToAddressLine3 = if(sBillToAddressLine3 IS NULL, '',sBillToAddressLine3)"
					+ ", sBillToAddressLine4 = if(sBillToAddressLine4 IS NULL, '',sBillToAddressLine4)"
					+ ", sBillToCity = if(sBillToCity IS NULL, '',sBillToCity)"
					+ ", sBillToState = if(sBillToState IS NULL, '',sBillToState)"
					+ ", sBillToZip = if(sBillToZip IS NULL, '',sBillToZip)"
					+ ", sBillToCountry = if(sBillToCountry IS NULL, '',sBillToCountry)"
					+ ", sBillToPhone = if(sBillToPhone IS NULL, '',sBillToPhone)"
					+ ", sBillToFax = if(sBillToFax IS NULL, '',sBillToFax)"
					+ ", sBillToContact = if(sBillToContact IS NULL, '',sBillToContact)"
					+ ", sPONumber = if(sPONumber IS NULL, '',sPONumber)"
					+ ", sFOBPoint = if(sFOBPoint IS NULL, '',sFOBPoint)"
					+ ", sTaxExemptNumber = if(sTaxExemptNumber IS NULL, '',sTaxExemptNumber)"
					+ ", sPrePostingInvoiceDiscountDesc = if(sPrePostingInvoiceDiscountDesc IS NULL, '',sPrePostingInvoiceDiscountDesc)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE:
			case 484:
				//Added by TJR 2012/3/27:
				SQL = "alter table JobCost add column dattimeleftprevious datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", add column dattimearrivedatcurrent datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", add column dattimeleftcurrent datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", add column dattimearrivedatnext datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 485:
				//Added by LTO 2012/4/12:
				SQL = "CREATE TABLE `fa_master` (" + 
						  "`sTruckNumber` varchar(32) DEFAULT NULL," + 
						  " " + "`sNotePayableGLAcct` varchar(128) DEFAULT NULL," + 
						  " " + "`sState` varchar(32) DEFAULT NULL," + 
						  " " + "`sAssetNumber` varchar(50) NOT NULL DEFAULT ''," + 
						  " " + "`sDescription` varchar(255) DEFAULT NULL," + 
						  " " + "`datAcquisitionDate` datetime DEFAULT NULL," + 
						  " " + "`dAcquisitionAmount` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`sClass` varchar(6) DEFAULT NULL," + 
						  " " + "`sSerialNumber` varchar(128) DEFAULT NULL," + 
						  " " + "`sLicenseTagNumber` varchar(32) DEFAULT NULL," + 
						  " " + "`sLocation` varchar(32) DEFAULT NULL," + 
						  " " + "`datDateSold` datetime DEFAULT NULL," + 
						  " " + "`sGaragedLocation` varchar(32) DEFAULT NULL," + 
						  " " + "`sLossOrGainGL` varchar(128) DEFAULT NULL," + 
						  " " + "`sDepreciationType` varchar(12) DEFAULT NULL," + 
						  " " + "`dCurrentValue` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`sComment` varchar(254) DEFAULT NULL," + 
						  " " + "`dAmountSoldFor` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`sDepreciationGLAcct` varchar(128) DEFAULT NULL," + 
						  " " + "`sAccumulatedDepreciationGLAcct` varchar(128) DEFAULT NULL," + 
						  " " + "`dAccumulatedDepreciation` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`dYTDDepreciation` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`dSalvageValue` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`dYTDDisposedAmount` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  " " + "`dYTDPurchaseAmount` decimal(17,4) NOT NULL DEFAULT '0.0000'," + 
						  "`id` mediumint(9) NOT NULL AUTO_INCREMENT," + 
						  " " + "PRIMARY KEY (`id`)," + 
						  " " + "UNIQUE KEY `sAssetNumber` (`sAssetNumber`)" + 
						") ENGINE=MyISAM AUTO_INCREMENT=466 DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				SQL = "CREATE TABLE `fa_transactions` (" + 
						  " " + "`datTransactionDate` datetime DEFAULT NULL," +
						  " " + "`dAmountDepreciated` decimal(17,4) NOT NULL DEFAULT '0.0000'," +
						  " " + "`iFiscalYear` int(11) DEFAULT '0'," +
						  " " + "`iFiscalPeriod` int(11) DEFAULT '0'," +
						  " " + "`iProvisionalPosting` int(11) DEFAULT '0'," +
						  " " + "`sTransAccumulatedDepreciationGLAcct` varchar(128) DEFAULT NULL," +
						  " " + "`sTransDepreciationGLAcct` varchar(128) DEFAULT NULL," +
						  " " + "`sTransAssetNumber` varchar(50) NOT NULL DEFAULT ''," +
						  " " + "`sTransactionType` varchar(6) DEFAULT ''," +
						  " " + "`sTransComment` varchar(64) DEFAULT ''," +
						  " " + "`datPostingDate` datetime DEFAULT NULL" +
						") ENGINE=MyISAM DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				SQL = "CREATE TABLE `fa_locations` (" + 
							" " + "`sLocLocation` varchar(6) NOT NULL DEFAULT ''," + 
							" " + "`sLocDescription` varchar(30) DEFAULT ''" + 
						") ENGINE=MyISAM DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				SQL = "CREATE TABLE `fa_depreciationtype` (" + 
							" " + "`sDepreciationType` varchar(12) NOT NULL DEFAULT ''," +
							" " + "`sCalculationType` varchar(12) DEFAULT NULL," +
							" " + "`iLifeInMonths` int(11) DEFAULT NULL" +
						") ENGINE=MyISAM DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				SQL = "CREATE TABLE `fa_classes` (" +
							" " + "`sClass` varchar(6) NOT NULL DEFAULT ''," +
							" " + "`sClassDescription` varchar(128) DEFAULT ''" +
						" " + ") ENGINE=MyISAM DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 486:
				//Added by TJR 2012/4/24:
				SQL = "alter table InvoiceDetails add key sinvoicenumberkey (sInvoiceNumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 487:
				//Added by TJR 2012/4/24:
				SQL = "alter table OrderDetails add bdestimatedunitcost DECIMAL (17,4) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 488:
				//Added by TJR 2012/5/7:
				SQL = "alter table Users drop column iDefaultInstallationTicketCopies";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iDefaultInvoiceCopies";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iDefaultServiceTicketCopies";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iKeepAliveIntervalInSeconds";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultInstallationTicketDeviceName";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultInstallationTicketPaperBin";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultInvoiceDeviceName";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultInvoicePaperBin";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultOrderHeaderLocation";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultOrderServiceType";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultServiceTicketDeviceName";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultServiceTicketPaperBin";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sDefaultPMSearchField";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iDefaultPMSearchStyle";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iWageScaleAlert";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iCheckJobSiteAddress";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sExcelLocation";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iIdleOutIntervalInMinutes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column sResiCustomDBConnectionString";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iLimitCallSheetAccessToUser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table Users drop column iAllowCallSheetAccess";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
			case 489:
				//Added by TJR 2012/5/7:
				SQL = "rename table ATO_CLSDBCPX_LOGGING to systemlog";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 490:
				//Added by TJR 2012/5/8:
				SQL = "update ordermgrcomments set sordernumber = trim(sordernumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE:
			case 491:
				//Added by TJR 2012/5/8:
				SQL = "alter table icpoheaders drop column laccpacheaderseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpolines drop column laccpaclinesequence";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpolines drop column laccpacporcseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpolines drop column porlseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptheaders drop column rcphseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines drop column rcphseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines drop column porlseq";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 492:
				//Added by TJR 2012/5/17:
				SQL = "update fa_master set datAcquisitionDate = '0000-00-00 00:00:00' WHERE (datAcquisitionDate < '1901-01-01 00:00:00')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update fa_master set datDateSold = '0000-00-00 00:00:00' WHERE (datDateSold < '1901-01-01 00:00:00')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 493:
				//Added by TJR 2012/5/17:
				SQL = "alter table fa_master add column sdriver varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 494:
				//Added by LTO 2012/5/22:
				SQL = "alter table Bids add column `datcreatedtime` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 495:
				//Added by TJR 2012/5/25:
				SQL = "drop table if exists Suppliers";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists ClientInfo";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists MRUs";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists PONumbers";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists Projects";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists Salespersons";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists calculateditemlocations";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists User";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists `source drop down list`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "drop table if exists `Source Drop Down List`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
			case 496:
				//Added by TJR 2012/5/25:
				SQL = "rename table `Change Orders` to changeorders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "rename table `Critical Dates` to criticaldates";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Job Number` sjobnumber varchar(20) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Change Order Number` dchangeordernumber double NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Change Order Date` dattimechangeorderdate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Change Order Description` sdescription varchar(75) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Change Order Amount` damount double NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Total MU` dtotalmarkup double NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table changeorders change column `Truck Days` dtruckdays double NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table criticaldates change column `Critical Date` datcriticaldate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table criticaldates change column `Job Number` sordernumber varchar(20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table criticaldates change column `Resolved Flag` iresolved tinyint(4) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
			case 497:
				//Added by TJR 2012/5/25:
				SQL = "alter table OrderHeaders add column sshiptoemail varchar(80) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	

			//BEGIN CASE:
			case 498:
				//Added by TJR 2012/5/30:
				//If MySQL is running on a Windows server, these names are already lower case, so we won't stop if these fail:
				SQL = "rename table Bids to bids";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table CallSheets to callsheets";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table CompanyProfile to companyprofile";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table ConveniencePhrases to conveniencephrases";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table CustomerCallLog to customercalllog";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table DefaultItemCategories to defaultitemcategories";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table InvoiceDetails to invoicedetails";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table InvoiceHeaders to invoiceheaders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table JobCost to jobcost";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table JobType to jobtype";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table LaborTypes to labortypes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table Locations to locations";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table Mechanics to mechanics";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table OHDCustomDBConnectionString to ohdcustomdbconnectionstring";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table OrderDetails to orderdetails";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table OrderHeaders to orderheaders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table OrderSources to ordersources";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table ProjectTypes to projecttypes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SMOptions to smoptions";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SSOrderHeaders to ssorderheaders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SalesContacts to salescontacts";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SalesGroups to salesgroups";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table Salesperson to salesperson";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SecurityFunctions to securityfunctions";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SecurityGroupFunctions to securitygroupfunctions";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SecurityGroups to securitygroups";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SecurityUserGroups to securityusergroups";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table ServiceTypes to servicetypes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SiteLocations to sitelocations";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table SuppressItemQtyLookup to suppressitemqtylookup";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table Tax to tax";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table Users to users";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}
				SQL = "rename table WorkPerformedCodes to workperformedcodes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	

			//BEGIN CASE:
			case 499:
				//Added by TJR 2012/5/30:
				SQL = "alter table icitems add isuppressitemqtylookup int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update icitems left join suppressitemqtylookup on icitems.sitemnumber=suppressitemqtylookup.sItemNumber"
					+ " set icitems.isuppressitemqtylookup = 1 where suppressitemqtylookup.sItemNumber is not null";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "DROP TABLE suppressitemqtylookup";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE:
			case 500:
				//Added by TJR 2012/5/30:
				SQL = "alter table smoptions add iusesauthentication int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table smoptions add ssmtpusername varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table smoptions add ssmtppassword varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE:
			case 501:
				//Added by TJR 2012/6/28:
				SQL = "alter table icinventoryworksheet add unique key itemlocationkey (lphysicalinventoryid, slocation, sitemnumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE					
			//BEGIN CASE:
			case 502:
				//Added by TJR 2012/6/28:
				SQL = "alter table invoiceheaders modify column sTaxAuthority varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table orderheaders modify column sTaxAuthority varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE
			case 503:
				//Added by TJR 2012/7/23:
				SQL = "alter table orderheaders add column sgdoclink text";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update orderheaders set sgdoclink = ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE
			case 504:
				//Added by LTO 2012/7/25:
				SQL = "alter table mechanics add column sMechColorCode varChar(6) default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE
			case 505:
				//Added by TJR 8/7/12:
				SQL = "alter table mechanics change column sMechColorCode smechcolorcode varchar(6) default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//Added by BPK 8/7/12:
				SQL = "ALTER TABLE invoicedetails"
					+ " DROP COLUMN dMostRecentCost"
					+ ", DROP COLUMN iLineType"
					+ ", DROP COLUMN sItemControlAcctSet"
					+ ", DROP COLUMN sPickingSequence"
					+ ", DROP COLUMN sICItemComment1"
					+ ", DROP COLUMN sICItemComment2"
					+ ", DROP COLUMN iReturnToInventory"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE invoiceheaders"
					+ " DROP COLUMN iPrintStatus"
					+ ", DROP COLUMN sFOBPoint"
					+ ", DROP COLUMN sFiscalYear"
					+ ", DROP COLUMN iFiscalPeriod"
					+ ", DROP COLUMN iDayEndPrinted"
					+ ", DROP COLUMN sInvoiceCreationTime"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderdetails"
					+ " DROP COLUMN iLineType"
					+ ", DROP COLUMN sItemControlAcctSet"
					+ ", DROP COLUMN sPickingSequence"
					+ ", DROP COLUMN dQtyBackOrdered"
					+ ", DROP COLUMN dOrderUnitConversion"
					+ ", DROP COLUMN iPriceOverridden"
					+ ", DROP COLUMN dMostRecentCost"
					+ ", DROP COLUMN iOrderComplete"
					+ ", DROP COLUMN sICItemComment1"
					+ ", DROP COLUMN sICItemComment2"
					+ ", DROP COLUMN dExtendedPriceAfterDiscount"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderheaders"
					+ " DROP COLUMN sOrderFiscalYear"
					+ ", DROP COLUMN iOrderFiscalPeriod"
					+ ", DROP COLUMN sFOBPoint"
					+ ", DROP COLUMN sOrderCreationTime"
					+ ", DROP COLUMN iPrintStatus"
					+ ", DROP COLUMN iOverCreditLimit"
					+ ", DROP COLUMN sTaxExemptNumber"
					+ ", DROP COLUMN iOrderCompleted"
					+ ", DROP COLUMN datOrderCompletionDate"
					+ ", DROP COLUMN datShipmentDate"
					+ ", DROP COLUMN iNumberOfLinesQtyShipped"
					+ ", DROP COLUMN sLastPrintedBy"
					+ ", DROP COLUMN sLastPrintedDate"
					+ ", DROP COLUMN iNewConstruction"
					+ ", DROP COLUMN datcontractreceived"
					+ ", DROP COLUMN datshopdrawingssubmitted"
					+ ", DROP COLUMN datshopdrawingsapproved"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE ssorderheaders"
					+ " DROP COLUMN TEXEMPT1"
					+ ", DROP COLUMN TEXEMPT2"
					+ ", DROP COLUMN From_Speed_Search"
					+ ", DROP COLUMN DELETEFLAG"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE
			case 506:
				//Added by LTO 2012/8/13:
				SQL = "CREATE TABLE `colortable` (" +
						  "`irow` mediumint(8) NOT NULL DEFAULT '0'," +
						  "`icol` mediumint(8) NOT NULL DEFAULT '0'," +
						  "`scolorcode` varchar(6) DEFAULT '000000'," +
						  "PRIMARY KEY (`irow`,`icol`)" +
						") ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `mechanics` drop column smechcolorcode";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `mechanics` add column smechcolorrow int(11) default 0";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `mechanics` add column smechcolorcol int(11) default 0";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//populate the color table
				SQL = "insert into colortable (irow, icol, scolorcode) values (0, 0, '000000'),(0, 1, '336699'),(0,2,'3366CC'),(0,3,'003399'),(0,4,'000099'),(0,5,'0000CC'),(0,6,'000066')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (1,0,'006666'),(1,1,'006699'),(1,2,'0099CC'),(1,3,'0066CC'),(1,4,'0033CC'),(1,5,'0000FF'),(1,6,'3333FF'),(1,7,'333399')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (2,0,'669999'),(2,1,'009999'),(2,2,'33CCCC'),(2,3,'00CCFF'),(2,4,'0099FF'),(2,5,'0066FF'),(2,6,'3366FF'),(2,7,'3333CC'),(2,8,'666699')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (3,0,'339966'),(3,1,'00CC99'),(3,2,'00FFCC'),(3,3,'00FFFF'),(3,4,'33CCFF'),(3,5,'3399FF'),(3,6,'6699FF'),(3,7,'6666FF'),(3,8,'6600FF'),(3,9,'6600CC')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (4,0,'339933'),(4,1,'00CC66'),(4,2,'00FF99'),(4,3,'66FFCC'),(4,4,'66FFFF'),(4,5,'66CCFF'),(4,6,'99CCFF'),(4,7,'9999FF'),(4,8,'9966FF'),(4,9,'9933FF'),(4,10,'9900FF')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (5,0,'006600'),(5,1,'00CC00'),(5,2,'00FF00'),(5,3,'66FF99'),(5,4,'99FFCC'),(5,5,'CCFFFF'),(5,6,'CCCCFF'),(5,7,'CC99FF'),(5,8,'CC66FF'),(5,9,'CC33FF'),(5,10,'CC00FF'),(5,11,'9900CC')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (6,0,'003300'),(6,1,'009933'),(6,2,'33CC33'),(6,3,'66FF66'),(6,4,'99FF99'),(6,5,'CCFFCC'),(6,6,'FFFFFF'),(6,7,'FFCCFF'),(6,8,'FF99FF'),(6,9,'FF66FF'),(6,10,'FF00FF'),(6,11,'CC00CC'),(6,12,'660066')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (7,0,'336600'),(7,1,'009900'),(7,2,'66FF33'),(7,3,'99FF66'),(7,4,'CCFF99'),(7,5,'FFFFCC'),(7,6,'FFCCCC'),(7,7,'FF99CC'),(7,8,'FF66CC'),(7,9,'FF33CC'),(7,10,'CC0099'),(7,11,'993399')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (8,0,'333300'),(8,1,'669900'),(8,2,'99FF33'),(8,3,'CCFF66'),(8,4,'FFFF99'),(8,5,'FFCC99'),(8,6,'FF9999'),(8,7,'FF6699'),(8,8,'FF3399'),(8,9,'CC3399'),(8,10,'990099')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (9,0,'666633'),(9,1,'99CC00'),(9,2,'CCFF33'),(9,3,'FFFF66'),(9,4,'FFCC66'),(9,5,'FF9966'),(9,6,'FF6666'),(9,7,'FF0066'),(9,8,'CC6699'),(9,9,'993366')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (10,0,'999966'),(10,1,'CCCC00'),(10,2,'FFFF00'),(10,3,'FFCC00'),(10,4,'FF9933'),(10,5,'FF6600'),(10,6,'FF5050'),(10,7,'CC0066'),(10,8,'660033')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (11,0,'996633'),(11,1,'CC9900'),(11,2,'FF9900'),(11,3,'CC6600'),(11,4,'FF3300'),(11,5,'FF0000'),(11,6,'CC0000'),(11,7,'990033')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into colortable (irow, icol, scolorcode) values (12,0,'663300'),(12,1,'996600'),(12,2,'CC3300'),(12,3,'993300'),(12,4,'990000'),(12,5,'800000'),(12,6,'660000')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE:
			case 507:
				//Added by TJR 2012/8/31:
				SQL = "alter table orderheaders modify column sOrderCreatedBy varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table invoiceheaders modify column sCreatedBy varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE:
			case 508:
				//Added by TJR 2012/9/7:
				SQL = "alter table salesgroups add unique key salesgroupcodekey (sSalesGroupCode)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			//BEGIN CASE:
			case 509:
				//Added by TJR 2012/9/17:
				SQL = "alter table icpolines add key `headerkey` (lpoheaderid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines add key `headerkey` (lreceiptheaderid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpolines add key `itemnumberkey` (sitemnumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines add key `itemnumberkey` (sitemnumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 510:
				//Added by TJR 2012/9/27:
				SQL = "create table glexportheaders("
					+ "id int(11) NOT NULL AUTO_INCREMENT"
					+ ", irecordtype int(11) NOT NULL DEFAULT '0'"
					+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"
					+ ", lbatchentry int(11) NOT NULL DEFAULT '0'"
					+ ", ssourceledger varchar(2) NOT NULL DEFAULT ''"
					+ ", sssourcetype varchar(2) NOT NULL DEFAULT ''"
					+ ", sjournaldescription varchar(60) NOT NULL DEFAULT ''"
					+ ",ssourcedescription varchar(32) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (id)"
					+ ", KEY batchnumberkey (lbatchnumber)"
					+ ", KEY entrynumberkey (lbatchentry)"
					+ ", UNIQUE KEY batchentrysourcekey (lbatchnumber, lbatchentry, ssourceledger)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "create table glexportdetails("
					+ "id int(11) NOT NULL AUTO_INCREMENT"
					+ ", irecordtype int(11) NOT NULL DEFAULT '0'"
					+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"
					+ ", lbatchentry int(11) NOT NULL DEFAULT '0'"
					+ ", ldetailjournalid int(11) NOT NULL DEFAULT '0'"
					+ ", ldetailtransactionnumber int(11) NOT NULL DEFAULT '0'"
					+ ", sdetailaccountid varchar(45) NOT NULL DEFAULT ''"
					+ ", bddetailtransactionamount DECIMAL (17, 2) NOT NULL DEFAULT '0.00'"
					+ ", sdetailtransactiondescription varchar(60) NOT NULL DEFAULT ''"
					+ ", sdetailtransactionreference varchar(60) NOT NULL DEFAULT ''"
					+ ", datdetailtransactiondate DATE NOT NULL DEFAULT '0000-00-00'"
					+ ", sdetailsourceledger varchar(2) NOT NULL DEFAULT ''"
					+ ", sdetailsourcetype varchar(2) NOT NULL DEFAULT ''"
					+ ", sdetailcomment varchar(254) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (id)"
					+ ", KEY batchnumberkey (lbatchnumber)"
					+ ", KEY entrynumberkey (lbatchentry)"
					+ ", KEY batchentrysourcekey (lbatchnumber, lbatchentry, sdetailsourceledger)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE:
			case 511:
				//Added by LTO 2012/10/5:
				SQL = "alter table orderheaders add column bddepositamount decimal(17,2) default 0";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE:
			case 512:
				//Added by LTO 2012/10/8:
				SQL = "alter table icitems add column ihideoninvoicedefault int(11) NOT NULL default 0";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 513:
			//Added by TJR 2012/11/7:
				SQL = "create table savedqueries("
					+ "id int(11) NOT NULL AUTO_INCREMENT"
					+ ", suser varchar(128) NOT NULL DEFAULT ''"
					+ ", sfirstname varchar(50) NOT NULL DEFAULT ''"
					+ ", slastname varchar(50) NOT NULL DEFAULT ''"
					+ ", dattimesaved DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", stitle varchar(254) NOT NULL DEFAULT ''"
					+ ", `ssql` text NOT NULL"
					+ ", PRIMARY KEY (id)"
					+ ", KEY titlekey (stitle)"
					+ ", KEY userkey (suser)"
					+ ") ENGINE = MyISAM";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
					
			//BEGIN CASE:
				//error: duplicated field name
			case 514:
				//Added by TJR 2012/11/8:
				SQL = "alter table savedqueries add column scomment varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 515:
				//Added by TJR 2012/11/13:
				SQL = "alter table bids add column sgdoclink text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 516:
				SQL = "alter table arcustomers add column sgdoclink text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE:
				//error: duplicated field name
			case 517:
				SQL = "alter table savedqueries add column iprivate int(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE:
			case 518:
				SQL = "alter table icvendoritems add key itemkey(`sitemnumber`)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 519:
				SQL = "alter table icporeceiptheaders add key poidkey(`lpoheaderid`)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				

			//BEGIN CASE:
			case 520:
				//SQL = "alter table tax drop column sTaxAuthority";
				//if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//SQL = "alter table tax drop column sTaxAuthDesc";
				//if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table orderheaders drop column sTaxAuthority";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table invoiceheaders drop column sTaxAuthority";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE:
			case 521:
				//Added by TJR 2013/1/2:
				SQL = "alter table icporeceiptheaders add column screatedby varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines add column smemo varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE:

			//BEGIN CASE:
			case 522:
				//Added by LTO 2013/1/7:
				SQL = "ALTER table `arcustomers` add column irequiresstatements int(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE:

			//BEGIN CASE:
			case 523:
				//Added by TJR 2013/1/8:
				SQL = "alter table icporeceiptlines add key invoicekey (lpoinvoiceid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icpoinvoiceheaders add key receiptkey (lreceiptid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptheaders add key datreceivedkey (datreceived)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE:
				
			//BEGIN CASE:
			case 524:
				//Added by TJR 2013/1/10:
				SQL = "alter table icitems add column sworkordercomment varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE icitems LEFT JOIN icporeceiptlines ON icitems.sitemnumber = icporeceiptlines.sitemnumber "
					+ " SET icitems.sworkordercomment = icporeceiptlines.smemo WHERE "
					+ "icporeceiptlines.smemo != ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icporeceiptlines drop column smemo";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE:
				
			case 525:
			//Added by TJR 1/15/2013:
				SQL = "create table defaultsalesgroupsalespersons("
					+ "id int(11) NOT NULL AUTO_INCREMENT"
					+ ", lsalesgroupid int(11) NOT NULL DEFAULT '0'"
					+ ", scustomercode varchar(12) NOT NULL DEFAULT ''"
					+ ", ssalespersoncode varchar(8) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (id)"
					+ ", UNIQUE KEY groupcustomerkey (lsalesgroupid, scustomercode)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//Now insert records for all the customers with default salespersons:
				try {
					ResultSet rsSalesGroups = SMUtilities.openResultSet(
						"SELECT * FROM salesgroups", 
						conn);
					while (rsSalesGroups.next()){
						String sSQL = "INSERT INTO defaultsalesgroupsalespersons ("
							+ "lsalesgroupid"
							+ ", scustomercode"
							+ ", ssalespersoncode"
							+ ") "
							+ " SELECT "
							+ "'" + Long.toString(rsSalesGroups.getLong("salesgroups.iSalesGroupId")) + "'"
							+ ", arcustomers.sCustomerNumber"
							+ ", arcustomers.sSalesperson"
							+ " FROM arcustomers WHERE ("
							+ "sSalesperson != ''"
							+ ")"
							;
						if (!execUpdate(sUser, sSQL, conn, iSystemDatabaseVersion)){return false;}
					}
					rsSalesGroups.close();
				} catch (SQLException e) {
					m_sErrorMessage = "Error updating default salespersons - " + e.getMessage();
					return false;
				}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE:
			case 526:
				//Added by TJR 2013/1/18:
				//alter table Locations modify column sAddress1 varchar(30) NOT NULL default ''
				SQL = "alter table jobcost modify column sDESC text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table jobcost change sDESC mworkdescription text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE:
				
			//BEGIN CASE
			case 527:
				//Added by TJR 2013/1/22:
				SQL = "alter table icpoheaders add column sgdoclink text";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update icpoheaders set sgdoclink = ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 528:
				//Added by TJR 2013/2/25:
				SQL = "alter table arcustomers drop column sSalesperson";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table arcustomers add column irequirespo int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 529:
				//Added by TJR 2013/3/26:
				SQL = "create table proposals("
						+ "strimmedordernumber varchar(22) NOT NULL DEFAULT '0'"
						+ ", datproposaldate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
						+ ", PRIMARY KEY (strimmedordernumber)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 530:
				//Added by TJR 2013/3/28:
				SQL = "alter table proposals add column sfurnishandinstallstring varchar(96) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sapprovedbyuser varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sapprovedbyfullname varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column dattimeapproved datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sbodydescription TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sextranotes TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column soptions TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate1 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate2 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate3 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate4 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate1price varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate2price varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate3price varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column salternate4price varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column swrittenproposalamt varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column snumericproposalamt varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sterms TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column iprintlogo int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE
			case 531:
				//Added by LTO 2013/4/1:
				SQL = "alter table salesperson add column ssalespersontitle varchar(50) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table salesperson add column sdirectdial varchar(20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table salesperson add column ssalespersonemail varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 532:
				//Added by TJR 2013/4/3:
				SQL = "alter table salesperson modify column sdirectdial varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 533:
				//Added by LTO 2013/4/8:
				SQL = "create table proposalterms("
						+ "id int(11) NOT NULL AUTO_INCREMENT"
						+ ", sProposalTermCode varchar(12) NOT NULL DEFAULT ''"
						+ ", mProposalTermDesc text NOT NULL"
						+ ", PRIMARY KEY (id)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 534:
				//Added by TJR 2013/4/9:
				SQL = "alter table proposalterms modify column sProposalTermCode varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 535:
				//Added by TJR 2013/4/10:
				SQL = "alter table proposalterms add column sdefaultpaymentterms varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposalterms add column sdaystoaccept varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE
			case 536:
				//Added by TJR 2013/4/10:
				SQL = "alter table proposals add column spaymentterms varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column sdaystoaccept varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column itermsid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				
				
			//BEGIN CASE
			case 537:
				//Added by TJR 2013/4/12:
				SQL = "create table proposalphrases("
						+ "id int(11) NOT NULL AUTO_INCREMENT"
						+ ", sproposalphrasename varchar(64) NOT NULL DEFAULT ''"
						+ ", mproposalphrase text NOT NULL"
						+ ", PRIMARY KEY (id)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 538:
				//Added by TJR 2013/5/10:
				SQL = "create table zipcodes ("
					+ "szipcode varchar(5) NOT NULL DEFAULT '0'"
					+ ", sprimarycity varchar(128) NOT NULL DEFAULT ''"
					+ ", sacceptablecities TEXT NOT NULL"
					+ ", sstate varchar(2) NOT NULL DEFAULT ''"
					+ ", scounty varchar(128) NOT NULL DEFAULT ''"
					+ ", slatitude varchar(13) NOT NULL DEFAULT '0.0'"
					+ ", slongitude varchar(13) NOT NULL DEFAULT '0.0'"
					+ ", scountry varchar(8) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (szipcode)"
					+ ") Engine = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 539:
				//Added by LTO 2013/5/29:
				SQL = "CREATE TABLE `wagescalerecords` (" +
					  "`sEmployeeName` varchar(128) NOT NULL DEFAULT '', " +
					  "`sEmployeeSSN` varchar(9) NOT NULL DEFAULT '', " +
					  "`sEmployeeAddress` varchar(255) NOT NULL DEFAULT '', " +
					  "`sEmployeeCity` varchar(128) NOT NULL DEFAULT '', " +
					  "`sEmployeeState` varchar(128) NOT NULL DEFAULT '', " +
					  "`sEmployeeZipCode` varchar(12) NOT NULL DEFAULT '', " +
					  "`sEmployeeTitle` varchar(32) NOT NULL DEFAULT '', " +
					  "`datPeriodEndDate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', " +
					  "`sCostNumber` varchar(32) NOT NULL DEFAULT '', " +
					  "`dRegHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dOTHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dDTHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dPayRate` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dHolidayHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dPersonalHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dVacHours` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dGross` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dFederal` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dSS` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dMedicare` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dState` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dMiscDed` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`dNetPay` decimal(17,2) NOT NULL DEFAULT '0.00', " +
					  "`id` int(11) NOT NULL AUTO_INCREMENT, " +
					  "`sCreatedBy` varchar(50) NOT NULL DEFAULT '', " +
					  "PRIMARY KEY (`id`)" +
					") ENGINE=InnoDB AUTO_INCREMENT=3946 DEFAULT CHARSET=latin1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 540:
				//Added by TJR 2013/5/31:
				SQL = "alter table labelprinters add column iprinterlanguage int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update labelprinters set iprinterlanguage = 1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 541:
				//Added by LTO 2013/6/4:
				SQL = "alter table wagescalerecords add column dVacAllowed decimal(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 542:
				//Added by TJR 2013/6/11:
				SQL = "alter table orderdetails drop column dLineTaxAmount";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 543:
				//Added by TJR 2013/6/19:
				SQL = "alter table salesperson add column msignature MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 544:
				//Added by LTO 2013/6/28
				SQL = "alter table wagescalerecords add column `sEmployeeAddress2` varchar(255) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 545:
				//Added by TJR 2013/7/5
				SQL = "alter table orderheaders add column `lbidid` int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table orderheaders add column `squotedescription` varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 546:
				//Added by TJR 8/14/2013
				SQL = "alter table systemlog add column `sreferenceid` varchar(12) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 547:
				//Added by TJR 8/21/2013
				SQL = "alter table proposals add column `isigned` int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column `ssignedbyuser` varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column `ssignedbyfullname` varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table proposals add column `dattimesigned` datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 548:
				//Added by TJR 9/19/2013
				SQL = "UPDATE proposals set sbodydescription  = IF( soptions > '',  CONCAT(sbodydescription, '\n\n','OPTIONS:\n',soptions),  sbodydescription )";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE proposals set sbodydescription  = IF( sextranotes > '',  CONCAT(sbodydescription, '\n\n','EXTRA NOTES:\n',sextranotes),  sbodydescription )";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE proposals drop column soptions";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE proposals drop column sextranotes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 549:
				//Added by TJR 10/2/2013
				//We need to change the smoptions table to an InnoDB table so it can be part of data transactions:
				SQL = "ALTER TABLE smoptions ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//For years, the 'Next Invoice Number' in smoptions has actually been the LAST invoice number.  
				//The program has been changed to treat it as the NEXT invoice number, and the following lines update the smoptions table:
				SQL = "SELECT sInvoiceNumber from invoiceheaders order by sInvoiceNumber DESC limit 1";
				String sNextInvoiceNumber = "";
				try {
					ResultSet rs = SMUtilities.openResultSet(SQL, conn);
					if (rs.next()){
						sNextInvoiceNumber = SMUtilities.PadLeft(
							Long.toString(
								(Long.parseLong(rs.getString(SMTableinvoiceheaders.sInvoiceNumber).trim()) + 1)
							), " ", SMTablesmoptions.snextinvoicenumberlength
						);
					}
					rs.close();
				} catch (Exception e) {
					m_sErrorMessage = "Error getting last invoice number: " 
							+ e.getMessage()
							+ " - SQL: " + SQL;
						log.writeEntry(
								sUser, 
								"UPDATEDATA", 
								"SQL FAILED trying to get last invoice number"
								+ " - " + e.getMessage(),
								SQL,
								"[1380724307]"
						);
					return false;
				}
				SQL = "UPDATE smoptions set NextInvoiceNumber = '" + sNextInvoiceNumber + "'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 550:
				//Added by TJR 10/2/2013
				SQL = "ALTER TABLE smoptions add column sinvoicelogofilename varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sproposallogofilename varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE smoptions set sinvoicelogofilename  = 'invoicelogo.gif', sproposallogofilename = 'proposallogo.gif'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 551:
				//Added by TJR 10/8/2013
				SQL = "ALTER TABLE smoptions add column sftpexporturl varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sftpexportuser varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sftpexportpw varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sarftpexportpath varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sicftpexportpath varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sfaglexportpath varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sbankname varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column sbankrecglacct varchar(45) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column icreatebankrecexport int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 552:
				//Added by TJR 10/10/2013
				SQL = "ALTER TABLE smoptions drop column sicglexportpath";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions drop column sicftpexportpath";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions drop column sfaglexportpath";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions drop column sarchivedatabasename";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions change column sarglexportpath sfileexportpath varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions change column sarftpexportpath sftpfileexportpath varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 553:
				//Added by TJR 10/14/2013
				SQL = "create table proposalphrasegroups ("
						+ "id int(11) NOT NULL AUTO_INCREMENT"
						+ ", sgroupname varchar(64) NOT NULL DEFAULT ''"
						+ ", PRIMARY KEY (id)"
						+ ", UNIQUE KEY groupnamekey (sgroupname)"
						+ ") Engine = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE proposalphrases add column iphrasegroupid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO proposalphrasegroups (sgroupname) VALUES ('Common phrases')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE proposalphrases set iphrasegroupid = 1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 554:
				//Added by TJR 10/22/2013
				SQL = "ALTER TABLE proposalphrases add column isortorder int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "SELECT * FROM proposalphrases order by iphrasegroupid, sproposalphrasename";
			try {
				ResultSet rs = SMUtilities.openResultSet(SQL, conn);
				int iCurrentGroupID = -1;
				int iCurrentSortOrder = 0;
				while(rs.next()){
					if (iCurrentGroupID != rs.getInt(SMTableproposalphrases.iphrasegroupid)){
						iCurrentSortOrder = 1;
						iCurrentGroupID = rs.getInt(SMTableproposalphrases.iphrasegroupid);
					}
					//Update the sort order:
					SQL = "UPDATE proposalphrases set isortorder = " + Integer.toString(iCurrentSortOrder) 
							+ " WHERE (id = " + Integer.toString(rs.getInt(SMTableproposalphrases.sid)) + ")";
					Statement stmt = conn.createStatement();
					try {
						stmt.execute(SQL);
					} catch (Exception e) {
						m_sErrorMessage = "SQL Error updating sort order with SQL '" + SQL + "' - " + e.getMessage();
						return false;
					}
					//Increment the sort order:
					iCurrentSortOrder++;
				}
			} catch (SQLException e) {
				m_sErrorMessage = "SQL Error reading proposal phrases to update sort order - " + e.getMessage();
				return false;
			}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 555:
				//Added by TJR 11/6/2013
				SQL = "ALTER TABLE fa_classes ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_depreciationtype ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_locations ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_master ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_transactions ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendoritems ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE projecttypes ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE savedqueries ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE securityfunctions ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE usergeocodes ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 556:
				//Added by TJR 11/6/2013
				SQL = "ALTER TABLE conveniencephrases ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE defaultitemcategories ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE salescontacts ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 557:
				//Added by TJR 11/8/2013
				SQL = "ALTER TABLE proposalterms modify column sdefaultpaymentterms varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE proposals modify column spaymentterms varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 558:
				//Added by TJR 11/12/2013
				SQL = "ALTER TABLE aroptions add column ienforcecreditlimit int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 559:
				//Added by TJR 11/25/2013
				SQL = "ALTER TABLE smoptions drop column sorderdocspath";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions drop column sbiddocspath";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 560:
				//Added by LTO 11/25/2013
				SQL = "alter table jobcost add column smechanicfullname varchar(50) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 561:
				//Added by LTO 12/02/2013
				SQL = "CREATE TABLE `tempmechonorders` (" +
						"`sMechSSN` varchar(9) NOT NULL DEFAULT ''," + //MechSSNSCO
						"`sMechInitial` varchar(4) DEFAULT ''," +
						"`sMechFullName` varchar(50) DEFAULT ''," +
						"`sOrderNumber` varchar(10) NOT NULL DEFAULT ''," +
						"`id` mediumint(9) NOT NULL AUTO_INCREMENT," +
						" PRIMARY KEY (`id`)," +
						" KEY `mainIDX` (`sMechInitial`, `smechssn`, `smechfullname`, `sOrderNumber`)," + //MechSSNSCO
						" KEY `mechinitialkey` (`sMechInitial`)," +
						" KEY `ordernumberkey` (`sOrderNumber`)" +
						") ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "insert into tempmechonorders (sMechSSN, sMechInitial, sMechFullName, sOrderNumber) select distinct smechssn, smechinitial, smechfullname, strimmedordernumber from orderdetails"; //MechSSNSCO
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE jobcost LEFT JOIN tempmechonorders" +
						" ON jobcost.sMechanic = tempmechonorders.smechinitial" +
						" AND jobcost.sjobnumber = tempmechonorders.sOrderNumber" +
					 " SET" +
					 	" jobcost.smechanicssn = if(tempmechonorders.smechssn is null, '', tempmechonorders.smechssn)," + //MechSSNSCO
					 	" jobcost.smechanicfullname = if(tempmechonorders.smechfullname is null, '', tempmechonorders.smechfullname)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//fill in mechanic name for the job cost records with no order information.
				SQL = "UPDATE jobcost LEFT JOIN tempmechonorders" +
						" ON jobcost.sMechanic = tempmechonorders.smechinitial" +
					 " SET" +
					 	" jobcost.smechanicssn = if(tempmechonorders.smechssn is null, '', tempmechonorders.smechssn)," + //MechSSNSCO
					 	" jobcost.smechanicfullname = if(tempmechonorders.smechfullname is null, '', tempmechonorders.smechfullname)" +
					 " WHERE" +
					 	" jobcost.sMechanic <> ''" +
					 	" AND" +
					 	" jobcost.smechanicfullname = ''" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				SQL = "DROP TABLE `tempmechonorders`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 562:
				//Added by TJR 12/3/2013
				SQL = "CREATE TABLE `workorders` ("
						+ "lid int(11) NOT NULL AUTO_INCREMENT"
						+ ", smechanicinitials varchar(4) NOT NULL DEFAULT ''"
						+ ", smechanicname varchar(50) NOT NULL DEFAULT ''"
						+ ", sssn varchar(9) NOT NULL DEFAULT ''"
						+ ", dattimeposted datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
						+ ", dattimedone datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
						+ ", strimmedordernumber varchar(22) NOT NULL DEFAULT ''"
						+ ", ljobcostentryid int(11) NOT NULL DEFAULT '-1'"
						+ ", ssignedbyname varchar(80) NOT NULL DEFAULT ''"
						+ ", dattimesigned datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
						+ ", msignature MEDIUMTEXT NOT NULL"
						+ ", mcomments MEDIUMTEXT NOT NULL"
						+ ", PRIMARY KEY (lid)"
						+ ", KEY strimmedordernumberkey (strimmedordernumber)"
						+ ", KEY ljobcostentryidkey (ljobcostentryid)"
						+ ", KEY smechanicinitialskey (smechanicinitials)"
						+ ", KEY sssnkey (sssn)"
						+ ") ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "CREATE TABLE `workorderdetails` ("
						+ "lid int(11) NOT NULL AUTO_INCREMENT"
						+ ", lworkorderid int(11) NOT NULL DEFAULT '-1'"
						+ ", idetailtype int(11) NOT NULL DEFAULT '-1'"
						+ ", sitemnumber varchar(24) NOT NULL DEFAULT ''"
						+ ", sitemdesc varchar(75) NOT NULL DEFAULT ''"
						+ ", lorderdetailnumber int(11) NOT NULL DEFAULT '-1'"
						+ ", llinenumber int(11) NOT NULL DEFAULT '-1'"
						+ ", llinenumberid int(11) NOT NULL DEFAULT '-1'"
						+ ", lworkperfomedlinenumber int(11) NOT NULL DEFAULT '-1'"
						+ ", sworkperformed varchar(254) NOT NULL DEFAULT ''"
						+ ", bdquantity DECIMAL (17,4) NOT NULL DEFAULT '0.0000'"
						+ ", bdunitprice DECIMAL (17,2) NOT NULL DEFAULT '0.00'"
						+ ", PRIMARY KEY (lid)"
						+ ", KEY lworkorderidkey (lworkorderid)"
						+ ") ENGINE=InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 563:
				//Added by LTO 12/10/2013
				//fill in mechanic name for the job cost records with no order information.
				SQL = "UPDATE jobcost LEFT JOIN mechanics" +
						" ON jobcost.sMechanic = mechanics.smechinitial" +
					 " SET" +
					 	" jobcost.smechanicssn = if(mechanics.smechssn is null, '', mechanics.smechssn)," + //MechSSNSCO
					 	" jobcost.smechanicfullname = if(mechanics.smechfullname is null, '', mechanics.smechfullname)" +
					 " WHERE" +
					 	" jobcost.sMechanic <> ''" +
					 	" AND" +
					 	" jobcost.smechanicfullname = ''" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 564:
				//Added by TJR 1/6/2014
				//fill in mechanic name for the job cost records with no order information.
				SQL = "ALTER TABLE orderheaders drop column datCompletedDate" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderheaders drop column iRequisitionDueDay" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE jobcost drop column sJobType" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "DROP TABLE customercalllog" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "DROP TABLE jobtype" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;} 
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 565:
				//Added by TJR 1/6/2014
				//fill in mechanic name for the job cost records with no order information.
				SQL = "ALTER TABLE icvendors add column datlastmaintained datetime NOT NULL DEFAULT '0000-00-0000: 00:00:00'" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors add column slasteditedby varchar(128) NOT NULL DEFAULT ''" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors add column iactive int(11) NOT NULL DEFAULT '1'" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 566:
				//Added by LTO 1/24/2014
				SQL = "ALTER TABLE users add column iactive int(11) NOT NULL DEFAULT '1'" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 567:
				//Added by TJR 1/30/2014
				SQL = "ALTER TABLE workorderdetails add UNIQUE key workorderlinekey(lworkorderid, llinenumber, lworkperfomedlinenumber)" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 568:
				//Added by LTO 1/24/2014
				SQL = "ALTER TABLE icpoheaders add column iconfirmed int(11) NOT NULL DEFAULT '0'" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 569:
				//Added by TJR 2/6/2014
				SQL = "ALTER TABLE workorderdetails add column sunitofmeasure varchar(10) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 570:
				//Added by TJR 2/7/2014
				SQL = "ALTER TABLE workorderdetails change column lworkperfomedlinenumber lworkperformedlinenumber int(11) NOT NULL DEFAULT '-1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 571:
				//Added by TJR 2/10/2014
				SQL = "ALTER TABLE workorderdetails drop column llinenumberid";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorderdetails add column sworkperformedcode varchar(50) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 572:
				//Added by LTO 2/21/2014
				SQL = "ALTER TABLE icvendors add column ipoconfirmationrequired int(11) NOT NULL DEFAULT '0'" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 573:
				//Added by TJR 2/21/2014
				SQL = "ALTER TABLE servicetypes add column mworkorderterms MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 574:
				//Added by LTO 2/21/2014
				SQL = "ALTER TABLE orderheaders modify column `sPONumber` varchar(40) DEFAULT NULL" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE invoiceheaders modify column `sPONumber` varchar(40) DEFAULT NULL" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE entries modify column `sentryponumber` varchar(40) DEFAULT NULL" ;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 575:
				//Added by TJR 2/24/2014
				SQL = "ALTER TABLE servicetypes drop primary key";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes add column id int(11) PRIMARY KEY auto_increment";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes add UNIQUE key CodeKey(sCode)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes drop column LASTEDITUSER";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes drop column LASTEDITPROCESS";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes drop column LASTEDITDATE";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE servicetypes drop column LASTEDITTIME";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 576:
				//Added by TJR 2/24/2014
				SQL = "ALTER TABLE workorders drop key `ljobcostentryidkey`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add UNIQUE key `ljobcostentryidkey`(ljobcostentryid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 577:
				//Added by LTO 20140228
				SQL = "ALTER TABLE fa_master add column scomment1 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_master add column scomment2 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_master add column scomment3 varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE fa_master add column sgdoclink TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 578:
				//Added by TJR 3/6/2014
				SQL = "ALTER TABLE workorders add column madditionalworkcomments MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add column madditionalworksignature MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 579:
				//Added by TJR 3/10/2014
				SQL = "ALTER TABLE workorders add column iadditionalworkauthorized int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 580:
				//Added by LTO 3/10/2014
				SQL = "ALTER TABLE icpoheaders add column lphase int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icpoheaders add column screatedby varchar(128) NOT NULL DEFAULT 'na'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 581:
				//Added by TJR 3/19/2014
				SQL = "ALTER TABLE workorders add column istatus int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 582:
				//Added by LTO 20140404
				SQL = "ALTER TABLE bids add column isalescontactid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 583:
				//Added by TJR 4/18/2014
				SQL = "ALTER TABLE workorders add column iimported int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE		
				
			//BEGIN CASE
			case 584:
				//Added by TJR 4/21/2014
				SQL = "ALTER TABLE workorders add column iposted int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE workorders set iposted = 1 where (istatus > 0)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE workorders set iimported = 1 where (istatus > 1)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add column ltimestamp INT NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders drop column istatus";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 585:
				//Added by TJR 4/22/2014
				SQL = "ALTER TABLE workorders add column minstructions MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorderdetails add column bdqtyassigned DECIMAL (17,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
			
			//BEGIN CASE
			case 586:
				//LTO 20140422
				SQL = "alter table tax change sTaxJurisdiction sTaxJurisdiction varchar(12) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table arcustomers change staxjurisdiction staxjurisdiction varchar(12) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 587:
				//TJR 5/19/2014
				SQL = "create table bkbanks ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", sshortname varchar(32) NOT NULL DEFAULT ''"
					+ ", sbankname varchar(72) NOT NULL DEFAULT ''"
					+ ", saccountname varchar(60) NOT NULL DEFAULT ''"
					+ ", saccountnumber varchar(60) NOT NULL DEFAULT ''"
					+ ", dattimelastmaintained DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", slastmaintainedby varchar(128) NOT NULL DEFAULT ''"
					+ ", slastmaintainedbyfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", iactive int(11) NOT NULL DEFAULT '1'"
					+ ", bdrecentbalance DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
					+ ", datrecentbalancedate DATE NOT NULL DEFAULT '0000-00-00'"
					+ ", PRIMARY KEY (lid)"
					+ ", UNIQUE KEY shortnamekey(sshortname)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	

			//BEGIN CASE
			case 588:
				//TJR 5/28/2014
				SQL = "alter table bids add column sshiptoaddress1 varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptoaddress2 varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptoaddress3 varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptoaddress4 varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptocity varchar(30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptostate varchar(30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids add column sshiptozip varchar(20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
			//BEGIN CASE
			case 589:
				//TJR 6/3/2014
				SQL = "alter table bids change sphonenumber sphonenumber varchar(30) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids change saltphonenumber saltphonenumber varchar(30) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids change sfaxnumber sfaxnumber varchar(30) not null default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = REPLACE(sphonenumber, ' ', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = REPLACE(sphonenumber, '(', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = REPLACE(sphonenumber, ')', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = REPLACE(sphonenumber, '-', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = CONCAT("
					+ "LEFT(sphonenumber, 3)"
					+ ", '-'"
					+ ", MID(sphonenumber, 4, 3)"
					+ ", '-'"
					+ ", RIGHT(sphonenumber, 4)"
				+ ")"
				+ " WHERE (LENGTH(sphonenumber) >= 10)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sphonenumber = CONCAT(sphonenumber, ' ', 'x', sextension)"
						+ " WHERE (sextension != '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table bids drop column sextension";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set saltphonenumber = REPLACE(saltphonenumber, ' ', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set saltphonenumber = REPLACE(saltphonenumber, '(', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set saltphonenumber = REPLACE(saltphonenumber, ')', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set saltphonenumber = REPLACE(saltphonenumber, '-', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set saltphonenumber = CONCAT(LEFT(saltphonenumber, 3), '-', MID(saltphonenumber, 4, 3), '-', RIGHT(saltphonenumber, 4))"
					+ " WHERE (LENGTH(saltphonenumber) >= 10)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				SQL = "update bids set sfaxnumber = REPLACE(sfaxnumber, ' ', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sfaxnumber = REPLACE(sfaxnumber, '(', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sfaxnumber = REPLACE(sfaxnumber, ')', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sfaxnumber = REPLACE(sfaxnumber, '-', '')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update bids set sfaxnumber = CONCAT(LEFT(sfaxnumber, 3), '-', MID(sfaxnumber, 4, 3), '-', RIGHT(sfaxnumber, 4))"
					+ " WHERE (LENGTH(sfaxnumber) >= 10)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
			
			//BEGIN CASE:
			case 590:
				//TJR 6/6/2014
				SQL = "alter table workorders change madditionalworksignature mdetailsheettext MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "create table workorderdetailsheets ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", sname varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", mtext MEDIUMTEXT NOT NULL"
					+ ", PRIMARY KEY (lid)"
					+ ", UNIQUE KEY namekey(sname)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 591:
				//TJR 6/17/2014
				SQL = "alter table bkbanks ADD COLUMN sglaccount varchar(45) DEFAULT '' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "create table bkstatements ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", datstatementdate date NOT NULL DEFAULT '0000-00-00'"
					+ ", bdstartingbalance DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
					+ ", bdstatementbalance DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
					+ ", iposted int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lid)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "create table bkaccountentries ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", lstatementid int(11) NOT NULL default '0'"
					+ ", ientrytype int(11) NOT NULL default '0'"
					+ ", bdamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
					+ ", ssourcemodule varchar(2) NOT NULL DEFAULT ''"
					+ ", ibatchtype int(11) NOT NULL default '0'"
					+ ", ibatchnumber int(11) NOT NULL default '0'"
					+ ", ibatchentrynumber int(11) NOT NULL default '0'"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", datentrydate date NOT NULL DEFAULT '0000-00-00'"
					+ ", PRIMARY KEY (lid)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 592:
				//TJR 6/18/2014
				SQL = "alter table bkaccountentries ADD COLUMN sglaccount varchar(45) DEFAULT '' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 593:
				//TJR 6/19/2014
				SQL = "alter table bkaccountentries ADD COLUMN sdocnumber varchar(75) DEFAULT '' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 594:
				//TJR 6/19/2014
				SQL = "alter table bkstatements ADD COLUMN lbankid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 595:
				//TJR 7/7/2014
				SQL = "alter table workorderdetails ADD COLUMN lsetpricetozero int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 596:
				//TJR 7/8/2014
				SQL = "alter table workorderdetails ADD COLUMN bdextendedprice DECIMAL (17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 597:
				//TJR 7/22/2014
				SQL = "create table bkpostedentries ("
						+ "lid int(11) NOT NULL auto_increment"
						+ ", lstatementid int(11) NOT NULL default '0'"
						+ ", ientrytype int(11) NOT NULL default '0'"
						+ ", bdamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
						+ ", ssourcemodule varchar(2) NOT NULL DEFAULT ''"
						+ ", ibatchtype int(11) NOT NULL default '0'"
						+ ", ibatchnumber int(11) NOT NULL default '0'"
						+ ", ibatchentrynumber int(11) NOT NULL default '0'"
						+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
						+ ", datentrydate date NOT NULL DEFAULT '0000-00-00'"
						+ ", sglaccount varchar(45) DEFAULT '' NOT NULL"
						+ ", sdocnumber varchar(75) DEFAULT '' NOT NULL"
						+ ", icleared int(11) NOT NULL DEFAULT '0'"
						+ ", PRIMARY KEY (lid)"
					+ ") Engine = InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 598:
				//TJR 8/4/2014
				SQL = "alter table bkpostedentries ADD COLUMN loriginalentryid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 599:
				//TJR 8/21/2014
				SQL = "create table materialreturns ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", datinitiated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", sinitiatedby varchar(128) NOT NULL DEFAULT ''"
					+ ", sinitiatedbyfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", iresolved int(11) NOT NULL default '0'"
					+ ", datresolved DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", sresolvedby varchar(128) NOT NULL DEFAULT ''"
					+ ", sresolvedbyfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(254) NOT NULL DEFAULT ''"
					+ ", mcomments MEDIUMTEXT NOT NULL"
					+ ", mresolutioncomments MEDIUMTEXT NOT NULL"
					+ ", iworkorderid int(11) NOT NULL default '0'"
					+ ", strimmedordernumber varchar(22) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE:
			case 600:
				//TJR 8/28/2014
				SQL = "alter table tax ADD COLUMN iactive int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//BEGIN CASE:
			case 601:
				//SCO 9/11/2014
				SQL = "create table deliverytickets ("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", datinitiated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", sinitiatedby varchar(128) NOT NULL DEFAULT ''"
					+ ", sinitiatedbyfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(254) NOT NULL DEFAULT ''"
   					+ ", mcomments MEDIUMTEXT NOT NULL"
					+ ", strimmedordernumber varchar(22) NOT NULL DEFAULT ''"
					+ ", ssignedbyname varchar(80) NOT NULL DEFAULT ''"
					+ ", dattimesigned DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", msignatuure MEDIUMTEXT NOT NULL"
					+ ", scompanyname varchar(70) NOT NULL DEFAULT ''"
					+ ", scompanyaddressline1 varchar(60) NOT NULL DEFAULT ''"
					+ ", scompanyaddressline2 varchar(60) NOT NULL DEFAULT ''"
					+ ", scompanyphone varchar(20) NOT NULL DEFAULT ''"
					+ ", sbranchoffice varchar(30) NOT NULL DEFAULT ''"
					+ ", sbranchphone varchar(30) NOT NULL DEFAULT ''"
					+ ", sbilltoname varchar(60) NOT NULL DEFAULT ''"
					+ ", sbilltoadd1 varchar(60) NOT NULL DEFAULT ''"
					+ ", sbilltoadd3 varchar(60) NOT NULL DEFAULT ''"
					+ ", sbilltoadd4 varchar(60) NOT NULL DEFAULT ''"
					+ ", sbilltocontact varchar(20) NOT NULL DEFAULT ''"
					+ ", sbilltophone varchar(30) NOT NULL DEFAULT ''"
					+ ", sponumber varchar(30) NOT NULL DEFAULT ''"
					+ ", sshiptoname varchar(60) NOT NULL DEFAULT ''"
					+ ", sshiptoadd1 varchar(60) NOT NULL DEFAULT ''"
					+ ", sshiptoadd3 varchar(60) NOT NULL DEFAULT ''"
					+ ", sshiptoadd4 varchar(60) NOT NULL DEFAULT ''"
					+ ", sshiptocountry varchar(30) NOT NULL DEFAULT ''"
					+ ", sshiptocontact varchar(60) NOT NULL DEFAULT ''"
					+ ", sshiptophone varchar(30) NOT NULL DEFAULT ''"
					+ ", sshiptofax varchar(30) NOT NULL DEFAULT ''"
					+ ", smechanicname varchar(70) NOT NULL DEFAULT ''"
					+ ", scustomerfullname varchar(60) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
				+ ") Engine = InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 602:
				//Added by SCO 10/6/2014
				SQL = "ALTER TABLE mechanics add column lid int(11) unique NOT NULL auto_increment";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE jobcost add column imechid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderdetails add column imechid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords add column imechid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add column imechid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE invoicedetails add column imechid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table jobcost add key imechidkey (imechid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table orderdetails add key imechidkey (imechid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table wagescalerecords add key imechidkey (imechid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table workorders add key imechidkey (imechid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table invoicedetails add key imechidkey (imechid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 603:
				//Added by TJR 10/7/2014
				SQL = "ALTER TABLE mechanics add column semployeeid varchar(32) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//Read the corresponding value from the timecard system to fill in the em[ployee ID:
				SQL = "SELECT stimecarddatabase FROM smoptions";
				String sTimeCardDatabase = "";
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						sTimeCardDatabase = rs.getString("stimecarddatabase");
					}
					rs.close();
				} catch (SQLException e) {
					m_sErrorMessage = "SQL Error reading timecarddatabase to update mechanics with SQL: " + SQL 
						+ " - " + e.getMessage();
					return false;
				}
				if (sTimeCardDatabase.compareToIgnoreCase("") != 0){
					SQL = "UPDATE mechanics, " + sTimeCardDatabase + ".Employees SET mechanics.semployeeid = "
						+ sTimeCardDatabase + ".Employees.sEmployeeID "
						+ " WHERE (mechanics.sMechSSN = " + sTimeCardDatabase + ".Employees.sSSN)"
					;
				}
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 604:
				//Added by TJR 10/9/2014
				SQL = "ALTER TABLE wagescalerecords drop key imechidkey";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords drop column imechid";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 605:
				//Added by SCO 10/10/2014
				SQL = "ALTER TABLE materialreturns add column icreditstatus int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE materialreturns add column iponumber int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table materialreturns add key ipokey (iponumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 606:
				//Added by TJR 10/13/2014
				SQL = "UPDATE invoicedetails LEFT JOIN mechanics ON invoicedetails.sMechSSN=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
		
			//BEGIN CASE
			case 607:
				//Added by TJR 10/13/2014
				SQL = "UPDATE workorders LEFT JOIN mechanics ON workorders.sssn=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 608:
				//Added by TJR 10/13/2014
				SQL = "UPDATE jobcost LEFT JOIN mechanics ON jobcost.smechanicssn=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
 
			//BEGIN CASE
			case 609:
				//Added by TJR 10/13/2014
				SQL = "UPDATE orderdetails LEFT JOIN mechanics ON orderdetails.sMechSSN=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 610:
				//Added by TJR 10/13/2014
				SQL = "ALTER TABLE invoicedetails DROP column sMechSSN";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 611:
				//Added by TJR 10/15/2014
				//Update the MECH ID's one last time before we remove the SSN field:
				SQL = "UPDATE workorders LEFT JOIN mechanics ON workorders.sssn=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//We'll make a backup of this table, just in case, before we drop the SSN field:
				SQL = "CREATE TABLE workorders_bak LIKE workorders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO workorders_bak SELECT * FROM workorders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders DROP column sssn";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 612:
				//Added by SCO 10/17/2014
				//Drop table due to insignificance
				SQL = "ALTER TABLE arcustomers DROP column mProductionNotes";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 613:
				//Added by TJR 10/17/2014
				//Update the MECH ID's one last time before we remove the SSN field:
				SQL = "UPDATE jobcost LEFT JOIN mechanics ON jobcost.smechanicssn=mechanics.sMechSSN set imechid = mechanics.lid where (mechanics.lid IS NOT NULL)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//We'll make a backup of this table, just in case, before we drop the SSN field:
				SQL = "CREATE TABLE jobcost_bak LIKE jobcost";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO jobcost_bak SELECT * FROM jobcost";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE jobcost DROP column smechanicssn";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 614:
				//Added by TJR 10/20/2014
				//Update the MECH ID's one last time before we remove the SSN field:
				SQL = "UPDATE orderdetails LEFT JOIN mechanics ON orderdetails.sMechSSN=mechanics.sMechSSN"
				 + " set imechid = mechanics.lid where ((mechanics.lid IS NOT NULL) AND (orderdetails.imechid < 1))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//We'll make a backup of this table, just in case, before we drop the SSN field:
				SQL = "CREATE TABLE orderdetails_bak LIKE orderdetails";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO orderdetails_bak SELECT * FROM orderdetails";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderdetails DROP column sMechSSN";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 615:
				//Added by TJR 10/20/2014
				//We'll make a backup of this table, just in case, before we drop the SSN field:
				SQL = "CREATE TABLE mechanics_bak LIKE mechanics";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO mechanics_bak SELECT * FROM mechanics";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE mechanics DROP primary key";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE mechanics DROP column sMechSSN";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE mechanics add primary key (lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE				

			//BEGIN CASE
			case 616:
				//Added by TJR 10/29/2014
				SQL = "ALTER TABLE bkaccountentries add column icleared int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//If the entry has NO statement ID, it's not cleared.
				//If the entry has a valid statement ID, then it's cleared.
				//If the entry has NO statement ID, then IF there is ONE unposted statement, the entry gets the statement ID of that unposted statement.
				//If there are any unposted statements, then we have to address those individually.
				SQL = "UPDATE bkaccountentries set icleared  = 1 WHERE (lstatementid > 0)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 617:
				//Added by TJR 10/30/2014
				SQL = "ALTER TABLE orderdetails add column minternalcomments MEDIUMTEXT NOT NULL AFTER mInvoiceComments";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 618:
				//Added by TJR 10/31/2014
				SQL = "DROP TABLE orderdetails_bak";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 619:
				//Added by TJR 10/31/2014
				SQL = "DROP TABLE mechanics_bak";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 620:
				//Added by TJR 10/31/2014
				SQL = "DROP TABLE jobcost_bak";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 621:
				//Added by TJR 10/31/2014
				SQL = "DROP TABLE workorders_bak";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	

			//BEGIN CASE
			case 622:
				//Added by TJR 11/10/2014
				//The 'DELETE ITEMS' function until now had neglected to delete statistics, so there were a lot in the data
				//from items which had not been deleted
				SQL = "DELETE STATISTICS FROM icitemstatistics as STATISTICS"
					+ " LEFT JOIN icitems ON STATISTICS.sitemnumber=icitems.sitemnumber"
					+ " WHERE ("
						+ "(icitems.sitemnumber IS NULL)"
					+ ")"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE	
				
			//BEGIN CASE
			case 623:
				//Added by SCO 1/06/2015
				SQL = "ALTER TABLE locations ADD column sadditionalnotes MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
				
			//BEGIN CASE
			case 624:
				//Added by TJR 2/12/2015
				SQL = "ALTER TABLE workorders ADD column mmanagersnotes MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//JC2WO:
			//BEGIN CASE
			case 625:
				//Added by TJR 3/3/2015 - adding all the needed jobcost fields to the work order:
				SQL = "ALTER TABLE workorders ADD column bdqtyofhours DECIMAL(6,2) DEFAULT '0.00' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column datscheduleddate datetime DEFAULT '0000-00-00 00:00:00' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column mworkdescription TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column bdtravelhours DECIMAL(6,2) DEFAULT '0.00' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column bdbackchargehours DECIMAL(6,2) DEFAULT '0.00' NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column slasteditedby varchar(50) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}				
				SQL = "ALTER TABLE workorders ADD column sscheduledby varchar(128) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}		
				SQL = "ALTER TABLE workorders ADD column ijoborder int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column sassistant varchar(75) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column sschedulecomment varchar(255) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column sstartingtime varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column dattimeleftprevious datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column dattimearrivedatcurrent datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column dattimeleftcurrent datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders ADD column dattimearrivedatnext datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add KEY scheduledatekey (datscheduleddate)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				
				// - To remove these fields, run these commands:
 				//ALTER TABLE workorders DROP column bdqtyofhours;
				//ALTER TABLE workorders DROP column datscheduleddate;
				//ALTER TABLE workorders DROP column mworkdescription;
				//ALTER TABLE workorders DROP column bdtravelhours;
				//ALTER TABLE workorders DROP column bdbackchargehours;
				//ALTER TABLE workorders DROP column slasteditedby;
				//ALTER TABLE workorders DROP column sscheduledby;
				//ALTER TABLE workorders DROP column ijoborder;
				//ALTER TABLE workorders DROP column sassistant;
				//ALTER TABLE workorders DROP column sschedulecomment;
				//ALTER TABLE workorders DROP column sstartingtime;
				//ALTER TABLE workorders DROP column dattimeleftprevious;
				//ALTER TABLE workorders DROP column dattimearrivedatcurrent;
				//ALTER TABLE workorders DROP column dattimeleftcurrent;
				//ALTER TABLE workorders DROP column dattimearrivedatnext;
			break;	
			//END CASE

			//BEGIN CASE
			case 626:
				//Added by TJR 3/3/2015 - updating all the current work orders with the related job cost data:
				SQL = "UPDATE workorders LEFT JOIN jobcost ON workorders.ljobcostentryid=jobcost.ID"
					+ " SET workorders.bdqtyofhours = jobcost.dQtyofHours"
					+ ", workorders.datscheduleddate = jobcost.datDate"
					+ ", workorders.mworkdescription = jobcost.mworkdescription"
					+ ", workorders.bdtravelhours = jobcost.dTravelHours"
					+ ", workorders.bdbackchargehours = jobcost.decBackChargeHours"
					+ ", workorders.slasteditedby = jobcost.slasteditedby"
					+ ", workorders.sscheduledby = jobcost.sscheduledby"
					+ ", workorders.ijoborder = jobcost.ijoborder"
					+ ", workorders.sassistant = jobcost.sassistant"
					+ ", workorders.sschedulecomment = jobcost.sschedulecomment"
					+ ", workorders.sstartingtime = jobcost.sstartingtime"
					+ ", workorders.dattimeleftprevious = jobcost.dattimeleftprevious"
					+ ", workorders.dattimearrivedatcurrent = jobcost.dattimearrivedatcurrent"
					+ ", workorders.dattimeleftcurrent = jobcost.dattimeleftcurrent"
					+ ", workorders.dattimearrivedatnext = jobcost.dattimearrivedatnext"
					+ " WHERE ("
						+ "(jobcost.ID IS NOT NULL)"
					+ ")"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 627:
				//Added by TJR 3/3/2015 - creating new work orders for any old job cost entries that didn't have work orders:
				SQL = "INSERT INTO workorders ("
				    + "dattimeposted"
				    + ", dattimedone"
				    + ", ssignedbyname"
				    + ", msignature"
				    + ", madditionalworkcomments"
				    + ", mdetailsheettext"
				    + ", iadditionalworkauthorized"
				    + ", iimported"
				    + ", iPOSTED"
				    + ", dattimesigned"
				    + ", minstructions"
				    + ", mmanagersnotes"
					+ ", bdqtyofhours"
					+ ", datscheduleddate"
					+ ", mworkdescription"
					+ ", bdtravelhours"
					+ ", bdbackchargehours"
					+ ", slasteditedby"
					+ ", sscheduledby"
					+ ", ijoborder"
					+ ", sassistant"
					+ ", sschedulecomment"
					+ ", sstartingtime"
					+ ", dattimeleftprevious"
					+ ", dattimearrivedatcurrent"
					+ ", dattimeleftcurrent"
					+ ", dattimearrivedatnext"
					+ ", smechanicinitials"
					+ ", smechanicname"
					+ ", strimmedordernumber"
					+ ", ljobcostentryid"
					+ ", ltimestamp"
					+ ", imechid"
					+ ", mcomments"
					+ ") "
					+ " SELECT"
				    + " '0000-00-00 00:00:00' AS dattimeposted"
				    + ", '0000-00-00 00:00:00' AS dattimedone"
				    + ", '' AS ssignedbyname"
				    + ", '' AS msignature"
				    + ", '' AS madditionalworkcomments"
				    + ", '' AS mdetailsheettext"
				    + ", 0 AS iadditionalworkauthorized"
				    + ", 0 AS iimported"
				    + ", 0 AS iPOSTED"
				    + ", '0000-00-00 00:00:00' AS dattimesigned"
				    + ", '' AS minstructions"
				    + ", '' AS mmanagersnotes"
					+ ", jobcost.dQtyofHours AS bdqtyofhours"
					+ ", jobcost.datDate AS datscheduleddate"
					+ ", jobcost.mworkdescription AS mworkdescription"
					+ ", jobcost.dTravelHours as bdtravelhours"
					+ ", jobcost.decBackChargeHours as bdbackchargehours"
					+ ", jobcost.slasteditedby as slasteditedby"
					+ ", jobcost.sscheduledby as sscheduledby"
					+ ", jobcost.ijoborder as ijoborder"
					+ ", jobcost.sassistant as sassistant"
					+ ", jobcost.sschedulecomment as sschedulecomment"
					+ ", jobcost.sstartingtime as sstartingtime"
					+ ", jobcost.dattimeleftprevious as dattimeleftprevious"
					+ ", jobcost.dattimearrivedatcurrent as dattimearrivedatcurrent"
					+ ", jobcost.dattimeleftcurrent as dattimeleftcurrent"
					+ ", jobcost.dattimearrivedatnext as dattimearrivedatnext"
					+ ", jobcost.sMechanic as smechanicinitials"
					+ ", jobcost.smechanicfullname as smechanicname"
					+ ", jobcost.sJobNumber as strimmedordernumber"
					+ ", jobcost.ID as ljobcostentryid"
					+ ", UNIX_TIMESTAMP() as ltimestamp"
					+ ", jobcost.imechid as imechid"
					+ ", '(This work order was never used - it was created automatically from an older schedule entry before the work order system was in place.)' as mcomments"
					+ " FROM jobcost LEFT JOIN workorders as WO ON jobcost.ID=WO.ljobcostentryid"
					+ " WHERE ("
						+ "(WO.lid IS NULL)"
					+ ") ORDER BY ID"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			//BEGIN CASE:
			case 628:
			//SCO 3/9/2015
				SQL = "CREATE TABLE taxcertificates ("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", screatedby varchar(128) NOT NULL DEFAULT '' "		 	 	 
					+ ", screatedbyfullname varchar(128) NOT NULL DEFAULT '' "
					+ ", datcreated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00' "
					+ ", datreceived DATE NOT NULL DEFAULT '0000-00-00' "
					+ ", datexpired DATE NOT NULL DEFAULT '0000-00-00' "
					+ ", datissued DATE NOT NULL DEFAULT '0000-00-00' "
					+ ", scustomername varchar(60) NOT NULL DEFAULT '' "
					+ ", scustomernumber varchar(12) NOT NULL DEFAULT '' "
					+ ", sexemptnumber VARCHAR(100) NOT NULL DEFAULT '' "
					+ ", sjobnumber VARCHAR(22) NOT NULL DEFAULT '' "
					+ ", staxjurisdiction VARCHAR(12) NOT NULL DEFAULT '' "
					+ ", mnotes MEDIUMTEXT NOT NULL "
					+ ", PRIMARY KEY (lid)"
					+ ") Engine = InnoDB"
					;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			 break;
			//END CASE
			 
			//BEGIN CASE
			case 629:
				//Added by TJR 3/11/2015 - updating all the current work orders with the related job cost data:
				//Had to run this again because some work orders had been added since the first pass, and some of the data was no longer synced:
				SQL = "UPDATE workorders LEFT JOIN jobcost ON workorders.ljobcostentryid=jobcost.ID"
					+ " SET workorders.dattimeleftprevious = jobcost.dattimeleftprevious"
					+ ", workorders.dattimearrivedatcurrent = jobcost.dattimearrivedatcurrent"
					+ ", workorders.dattimeleftcurrent = jobcost.dattimeleftcurrent"
					+ ", workorders.dattimearrivedatnext = jobcost.dattimearrivedatnext"
					+ " WHERE ("
						+ "(jobcost.ID IS NOT NULL)"
					+ ")"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 630:
				//Added by SCO 3/12/2015
				SQL = "ALTER TABLE locations ADD column sworeceiptcomment MEDIUMTEXT NOT NULL AFTER sadditionalnotes ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 631:
				//Added by TJR 4/3/2015
				SQL = "ALTER TABLE workorders DROP KEY ljobcostentryidkey";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 632:
				//Added by TJR 4/6/2015
				SQL = "select * from securitygroupfunctions where (ifunctionid=60)";
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					while (rs.next()){
						SQL = "INSERT IGNORE INTO securitygroupfunctions ("
							+ "sGroupName"
							+ ", sFunction"
							+ ", ifunctionid"
							+ ") VALUES ("
							+ "'" + rs.getString("sGroupName") + "'"
							+ ", 'SM Configure Work Orders'"
							+ ", 30"
							+ ")"
						;
						if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){
							rs.close();
							return false;
						}
					}
					rs.close();
				} catch (SQLException e) {
					m_sErrorMessage = "SQL Error [1428328460] udpdating 'Configure' permissions with SQL: " + SQL + " - " + e.getMessage();
					return false;
				}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 633:
				//Added by SCO 4/09/2015
				SQL = "ALTER TABLE taxcertificates ADD column sgdoclink text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 634:
				//Added by TJR 4/30/2015
				SQL = "INSERT IGNORE INTO securitygroupfunctions ("
					+ "sGroupName"
					+ ", sFunction"
					+ ", ifunctionid)"
					+ " SELECT"
					+ " sSecurityGroupName"
					+ ", 'SM View Order Header Information'"
					+ ", 1056"
					+ " FROM "
					+ " securitygroups"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 635:
				//Added by TJR 5/12/2015
				SQL = "ALTER TABLE securityfunctions ADD column sdescription text NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 636:
				//Added by TJR 6/4/2015
				SQL = "DROP TABLE jobcost";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 637:
				//Added by TJR 6/4/2015
				SQL = "ALTER TABLE workorders DROP column ljobcostentryid";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 638:
				//Added by TJR 6/12/2015
				SQL = "ALTER TABLE workorders add column sschedulechangedby varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE workorders add column dattimelastschedulechange DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE
			case 639:
				//Added by TJR 6/12/2015
				SQL = "ALTER TABLE bids add column stakeoffpersoncode varchar(8) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE bids add column spricingpersoncode varchar(8) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 640:
				//Added by TJR 7/14/2015
				SQL = "ALTER TABLE workorderdetailsheets add column itype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE
			case 641:
				//Added by TJR 8/16/2015
				SQL = "ALTER TABLE smoptions add column gdriveorderparentfolderid varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdriveorderfolderprefix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdriveorderfoldersuffix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdrivesalesleadparentfolderid varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdrivesalesleadfolderprefix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdrivesalesleadfoldersuffix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE
			case 642:
				//Added by BZ 8/20/2015
				SQL = "ALTER TABLE icoptions add column gdrivepurchaseordersparentfolderid varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icoptions add column gdrivepurchaseordersfolderprefix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icoptions add column gdrivepurchaseordersfoldersuffix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE aroptions add column gdrivecustomersparentfolderid varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE aroptions add column gdrivecustomersfolderprefix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE aroptions add column gdrivecustomersfoldersuffix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 643:
				//Added by BZ 8/27/2015
				SQL = "ALTER TABLE smoptions add column gdrivecreatenewfolderurl varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 644:
				//Added by TJR 9/8/2015
				SQL = "ALTER TABLE smoptions add column gdriveuploadfileurl varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 645:
				//Added by TJR 9/8/2015
				SQL = "ALTER TABLE smoptions add column icopysalesleadfolderurltoorder int (11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column irenamesalesleadfolderurltoorder int (11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 646:
				//Added by BJZ 9/15/2015
				SQL = "ALTER TABLE smoptions DROP column irenamesalesleadfolderurltoorder";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smoptions add column gdriverenamefolderurl varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
	
			//BEGIN CASE
			case 647:
				//Added by BJZ 9/20/2015
				SQL = "CREATE TABLE deliveryticketterms ("
					+ "sTermsCode int(11) NOT NULL AUTO_INCREMENT"
					+ ", mTerms MEDIUMTEXT NOT NULL"
					+ ", sDescription varchar(60) NOT NULL DEFAULT '' "
					+ ", iActive int(11) NOT NULL DEFAULT '0' "		 	 	 				
					+ ", datLastMaintained DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00' "
					+ ", PRIMARY KEY (sTermsCode)"
					+  ") Engine = InnoDB"
					;	
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}	
				SQL = "ALTER TABLE deliverytickets DROP column scustomerfullname";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column iworkorderid int (11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sterms varchar (6) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column iposted int (11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE sdescription sdetaillines MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE dattimesigned datsigneddate DATE NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE sbilltoadd3 sbilltoadd2 VARCHAR (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE sbilltoadd4 sbilltoadd3 VARCHAR (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE sshiptoadd3 sshiptoadd2 VARCHAR (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets CHANGE sshiptoadd4 sshiptoadd3 VARCHAR (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
		
			//BEGIN CASE
			case 648:
				//Added by BJZ 11/02/2015
				SQL = "ALTER TABLE wagescalerecords MODIFY sEmployeeSSN VARCHAR (30) NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords MODIFY sEmployeeZipCode VARCHAR (30) NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dRegHours sRegHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dOTHours sOTHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dDTHours sDTHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dPayRate sPayRate VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dHolidayHours sHolidayHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dPersonalHours sPersonalHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dVacHours sVacHours VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dGross sGross VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dFederal sFederal VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dSS sSS VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dMedicare sMedicare VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dState sState VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dMiscDed sMiscDed VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dNetPay sNetPay VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords CHANGE COLUMN dVacAllowed sVacAllowed VARCHAR (40) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE wagescalerecords ADD COLUMN sEncryptedEmployeeName VARCHAR (128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE
			case 649:
				//Added by BJZ 11/02/2015	
				SQL = "ALTER TABLE deliverytickets add column scompanyaddressline3 varchar (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column scompanyaddressline4 varchar (60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column scompanycity varchar (20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column scompanystate varchar (20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column scompanyzip varchar (15) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sshiptocity varchar (30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sshiptostate varchar (30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sshiptozip varchar (20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sbilltocity varchar (30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sbilltostate varchar (30) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column sbilltozip varchar (20) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
	
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 650:
				//Added by BJZ 11/03/2015	
				SQL = "ALTER TABLE deliverytickets change column msignatuure msignature MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
	
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 651:
				//Added by BJZ 11/03/2015	
				SQL = "ALTER TABLE deliverytickets change column sterms stermscode VARCHAR (6) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE deliverytickets add column mterms MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
	
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 652:
				//Added by BJZ 11/09/2015	
				SQL = "CREATE TABLE laborbackcharges ("
						+ "lid int(11) NOT NULL AUTO_INCREMENT"
						+ ", datinitiated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00' "
						+ ", sinitiatedby VARCHAR(60) NOT NULL DEFAULT '' "
						+ ", sinitiatedbyfullname VARCHAR(60) NOT NULL DEFAULT '' "		 	 	 				
						+ ", istatus INT(6) NOT NULL DEFAULT '1' "
						+ ", datresolved DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00' "
						+ ", sresolvedby VARCHAR(60) NOT NULL DEFAULT ''"
						+ ", sresolvedbyfullname VARCHAR(60) NOT NULL DEFAULT '' "				
						+ ", strimmedordernumber VARCHAR(60) NOT NULL DEFAULT '' "
						+ ", scustomername VARCHAR(60) NOT NULL DEFAULT '' "
						+ ", datdatesent DATE NOT NULL DEFAULT '0000-00-00' "
						+ ", svendor VARCHAR(60) NOT NULL DEFAULT '' "
						+ ", sdescription VARCHAR(254) NOT NULL DEFAULT '' "
						+ ", scomments VARCHAR(254) NOT NULL DEFAULT '' "
						+ ", dhours DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", dMiscCost DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", dLaborRate DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", dtotal DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", dcreditamount DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", doutstandingcredits DECIMAL(17,2) NOT NULL DEFAULT '0.00' "
						+ ", PRIMARY KEY (lid)"
						+  ") Engine = InnoDB"
						;	
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}	
	
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 653:
				//Added by TJR 11/14/2015	
				SQL = "ALTER TABLE systemlog add key `date_key` (datLoggingDate)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE
			case 654:
				//Added by TJR 11/14/2015	
				SQL = "ALTER TABLE systemlog add key `user_key` (sLoggingUser)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 655:
				//Added by TJR 11/14/2015	
				SQL = "ALTER TABLE systemlog add key `operation_key` (sLoggingOperation)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 656:
				//Added by BJZ 11/16/2015	
				SQL = "ALTER TABLE laborbackcharges change column dhours bdhours DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column dMiscCost bdmisccost DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column dLaborRate bdlaborrate DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column dtotal bdtotal DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column dcreditamount bdcreditamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column doutstandingcredits bdoutstandingcredits DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 657:
				//Added by BJZ 11/16/2015	
				SQL = "ALTER TABLE laborbackcharges change column strimmedordernumber strimmedordernumber VARCHAR(22) NOT NULL DEFAULT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column svendor svendoracct VARCHAR(12) NOT NULL DEFAULT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 658:
				//Added by BJZ 11/19/2015	
				SQL = "ALTER TABLE laborbackcharges change column sdescription sdescription MEDIUMTEXT NOT NULL ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column scomments scomments MEDIUMTEXT NOT NULL ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges add column datcreditnotedate DATE NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges add column scategorycode VARCHAR(6) NOT NULL DEFAULT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 659:
				//Added by BJZ 11/30/2015	
				SQL = "ALTER TABLE laborbackcharges drop column istatus ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges drop column sresolvedby ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges drop column sresolvedbyfullname ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges drop column datresolved ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges add column bdcreditdenied  DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column bdcreditamount bdcreditreceived  DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE laborbackcharges change column bdtotal bdcreditrequested DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 660:
				//Added by BJZ 12/01/2015	
				SQL = "ALTER TABLE laborbackcharges drop column bdoutstandingcredits ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 661:
				//Added by BJZ 12/01/2015	
				SQL = "ALTER TABLE deliverytickets MODIFY sbilltocontact VARCHAR(60) ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 662:
				//Added by TJR 12/7/2015	
				SQL = "CREATE TABLE apaccountsets ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sacctsetname varchar(32) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(60) NOT NULL DEFAULT ''"
				+ ", iactive int(11) NOT NULL DEFAULT '1'"
				+ ", spayablescontrolacct varchar(45) NOT NULL DEFAULT ''"
				+ ", spurchasediscountacct varchar(45) NOT NULL DEFAULT ''"
				+ ", sprepaymentacct varchar(45) NOT NULL DEFAULT ''"
				+ " , PRIMARY KEY (lid)"
				+ " , UNIQUE KEY accsetnamekey (sacctsetname)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 663:
				//Added by TJR 12/7/2015	
				SQL = "ALTER TABLE icvendors ADD COLUMN iapaccountset int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors ADD COLUMN ibankcode int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors ADD COLUMN sdefaultexpenseacct varchar(45) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 664:
				//Added by TJR 12/7/2015	
				SQL = "CREATE TABLE apoptions ("
				+ "  ibatchpostinginprocess int(11) NOT NULL DEFAULT '0'"
				+ ", suser varchar(128) NOT NULL DEFAULT ''"
				+ ", sprocess varchar(128) NOT NULL DEFAULT ''"
				+ ", datstartdate datetime NOT NULL default '0000-00-00 00:00:00'"
				+ ", iusessmcpap int(11) NOT NULL DEFAULT '0'"
				+ ", iaccpacversion int(11) NOT NULL DEFAULT '0'"
				+ ", gdrivevendorsparentfolderid varchar(72) NOT NULL DEFAULT ''"
				+ ", gdrivevendorsderfolderprefix varchar(32) NOT NULL DEFAULT ''"
				+ ", gdrivevendorsfoldersuffix varchar(128) NOT NULL DEFAULT ''"
				+ ", saccpacdatabaseurl varchar(128) NOT NULL DEFAULT ''"
				+ ", saccpacdatabasename varchar(128) NOT NULL DEFAULT ''"
				+ ", saccpacdatabaseuser varchar(128) NOT NULL DEFAULT ''"
				+ ", saccpacdatabasuserpw varchar(128) NOT NULL DEFAULT ''"
				+ ", iaccpacdatabasetype int(11) NOT NULL DEFAULT '0'"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 665:
				//Added by TJR 12/10/2015	
				SQL = "ALTER TABLE icvendors ADD COLUMN iaddedbyapconversion int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 666:
				//Added by TJR 12/11/2015	
				SQL = "ALTER TABLE smoptions DROP COLUMN gdrivecreatenewfolderurl";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 667:
				//Added by TJR 12/11/2015	
				SQL = "ALTER TABLE icvendors ADD COLUMN sgdoclink TEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 668:
				//Added by TJR 12/14/2015	
				SQL = "CREATE TABLE apdistributioncodes ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sdistcodename varchar(32) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(60) NOT NULL DEFAULT ''"
				+ ", idiscountable int(11) NOT NULL DEFAULT '0'"
				+ ", sglacct varchar(45) NOT NULL DEFAULT ''"
				+ " , PRIMARY KEY (lid)"
				+ " , UNIQUE KEY distcodenamekey (sdistcodename)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 669:
				//Added by TJR 12/14/2015	
				SQL = "CREATE TABLE apvendorstatistics ("
				+ " svendoracct varchar(12) NOT NULL DEFAULT ''"
				+ ", lyear int(11) NOT NULL DEFAULT '0'"
				+ ", lmonth int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofinvoices int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofcredits int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofdebits int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofpayments int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofdiscountstaken int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofdiscountslost int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofadjustments int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofinvoicespaid int(11) NOT NULL DEFAULT '0'"
				+ ", lnumberofdaystopay int(11) NOT NULL DEFAULT '0'"
				+ ", laveragedaystopay int(11) NOT NULL DEFAULT '0'"
				+ ", bdamountofinvoices DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofcreditnotes DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofdebitnotes DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofpayments DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofdiscounts DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofdiscountslost DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", bdamountofadjustments DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ " , UNIQUE KEY vendoryearmonthkey (svendoracct, lyear, lmonth)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 670:
				//Added by TJR 12/15/2015	
				SQL = "CREATE TABLE ap1099cprscodes ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sclassid varchar(6) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(60) NOT NULL DEFAULT ''"
				+ ", iactive int(11) NOT NULL DEFAULT '1'"
				+ ", bdminimumreportingamt DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
				+ ", PRIMARY KEY (lid)"
				+ ", UNIQUE KEY classidkey (sclassid)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 671:
				//Added by BJZ 12/15/2015	
				SQL = "ALTER TABLE deliverytickets ADD COLUMN sdeliveredby VARCHAR(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 672:
				//Added by TJR 12/15/2015	
				SQL = "CREATE TABLE apvendorgroups ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sgroupid varchar(12) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(60) NOT NULL DEFAULT ''"
				+ ", iactive int(11) NOT NULL DEFAULT '1'"
				+ ", iapaccountset int(11) NOT NULL DEFAULT '0'"
				+ ", ibankcode int(11) NOT NULL DEFAULT '0'"
				+ ", stermscode varchar(6) NOT NULL DEFAULT ''"
				+ ", iprintseparatechecks int(11) NOT NULL DEFAULT '0'"
				+ ", idistributioncodeusedfordistribution int(11) NOT NULL DEFAULT '0'"
				+ ", sglacctusedfordistribution varchar(45) NOT NULL DEFAULT ''"
				+ ", idistributeby int(11) NOT NULL DEFAULT '0'"
				+ ", staxjurisdiction varchar(12) NOT NULL DEFAULT ''"
				+ ", itaxtype int(11) NOT NULL DEFAULT '0'"
				+ ", itaxreportingtype int(11) NOT NULL DEFAULT '0'"
				+ ", i1099CPRScode int(11) NOT NULL DEFAULT '0'"
				+ ", PRIMARY KEY (lid)"
				+ ", UNIQUE KEY groupidkey (sgroupid)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 673:
				//Added by TJR 12/15/2015	
				SQL = "alter table icvendors change column sgdoclink sgdoclink varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 674:
				//Added by TJR 12/18/2015	
				SQL = "CREATE TABLE apvendorremittolocations ("
				+ " svendoracct varchar(12) NOT NULL DEFAULT ''"
				+ ", sremittocode varchar(12) NOT NULL DEFAULT ''"
				+ ", iactive int(11) NOT NULL DEFAULT '1'"
				+ ", sremittoname varchar(60) NOT NULL DEFAULT ''"
				+ ", saddressline1 varchar(60) NOT NULL DEFAULT ''"
				+ ", saddressline2 varchar(60) NOT NULL DEFAULT ''"
				+ ", saddressline3 varchar(60) NOT NULL DEFAULT ''"
				+ ", saddressline4 varchar(60) NOT NULL DEFAULT ''"
				+ ", scity varchar(30) NOT NULL DEFAULT ''"
				+ ", sstate varchar(30) NOT NULL DEFAULT ''"
				+ ", spostalcode varchar(20) NOT NULL DEFAULT ''"
				+ ", scountry varchar(30) NOT NULL DEFAULT ''"
				+ ", scontactname varchar(30) NOT NULL DEFAULT ''"
				+ ", sphonenumber varchar(30) NOT NULL DEFAULT ''"
				+ ", sfaxnumber varchar(30) NOT NULL DEFAULT ''"
				+ ", swebaddress varchar(128) NOT NULL DEFAULT ''"
				+ ", semailaddress varchar(128) NOT NULL DEFAULT ''"
				+ ", datlastmaintained datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
				+ ", slasteditedby varchar(128) NOT NULL DEFAULT ''"
				+ ", PRIMARY KEY (svendoracct, sremittocode)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 675:
				//Added by TJR 12/18/2015	
				SQL = "alter table icvendors add column sprimaryremittocode varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 676:
				//Added by TJR 12/18/2015	
				SQL = "ALTER TABLE bkbanks ADD COLUMN iaddedbyapconversion int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 677:
				//Added by TJR 12/22/2015	
				SQL = "CREATE TABLE aprecurringpaymentschedules ("
				+ "  lid int(11) AUTO_INCREMENT NOT NULL"
				+ ", sschedulecode varchar(32) NOT NULL DEFAULT ''"
				+ ", sdescription varchar(60) NOT NULL DEFAULT ''"
				+ ", iinterval int(11) NOT NULL DEFAULT '1'"
				+ ", iphase int(11) NOT NULL DEFAULT '1'"
				+ ", idayofmonth int(11) NOT NULL DEFAULT '1'"
				+ ", iweek int(11) NOT NULL DEFAULT '1'"
				+ ", iweekday int(11) NOT NULL DEFAULT '1'"
				+ ", imonth int(11) NOT NULL DEFAULT '1'"
				+ ", isunday int(11) NOT NULL DEFAULT '0'"
				+ ", imonday int(11) NOT NULL DEFAULT '0'"
				+ ", ituesday int(11) NOT NULL DEFAULT '0'"
				+ ", iwednesday int(11) NOT NULL DEFAULT '0'"
				+ ", ithursday int(11) NOT NULL DEFAULT '0'"
				+ ", ifriday int(11) NOT NULL DEFAULT '0'"
				+ ", isaturday int(11) NOT NULL DEFAULT '0'"
				+ ", datstartdate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
				+ ", ifrequencytype int(11) NOT NULL DEFAULT '1'"
				+ ", PRIMARY KEY (lid)"
				+ ", UNIQUE KEY schedulecodekey (sschedulecode)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 678:
				//Added by TJR 12/23/2015	
				SQL = "ALTER TABLE aprecurringpaymentschedules ADD COLUMN datlastrundate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 679:
				//Added by TJR 12/23/2015	
				SQL = "RENAME table aprecurringpaymentschedules to smschedules";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 680:
				//Added by TJR 12/23/2015	
				SQL = "ALTER TABLE smschedules ADD COLUMN iremindhowmanydaysinadvance int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules ADD COLUMN iremindermode int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules ADD COLUMN susertobereminded varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 681:
				//Added by TJR 12/23/2015	
				SQL = "ALTER TABLE icvendors ADD COLUMN itaxreportingtype int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors ADD COLUMN staxidentifyingnumber varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors ADD COLUMN i1099CPRSid int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icvendors ADD COLUMN itaxidnumbertype int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 682:
				//Added by TJR 1/20/2015	
				SQL = "ALTER TABLE tax  DROP COLUMN sTaxAuthority";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE tax  DROP COLUMN sTaxAuthDesc";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 683:
				//Added by TJR 1/21/2015	
				SQL = "alter table tax change column dTaxRate bdtaxrate DECIMAL(7,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 684:
				//Added by TJR 1/21/2015	
				SQL = "alter table invoiceheaders change column dTaxRate bdtaxrate DECIMAL(7,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 685:
				//Added by TJR 1/21/2015	
				SQL = "alter table invoiceheaders change column dTaxAmount bdtaxamount DECIMAL(17,2)  NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 686:
				//Added by TJR 1/21/2015	
				SQL = "alter table invoiceheaders change column dTaxBase bdtaxbase DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 687:
				//Added by TJR 1/21/2015	
				SQL = "alter table invoicedetails change column dLineTaxAmount bdlinetaxamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 688:
				//Added by TJR 1/21/2015	
				SQL = "alter table orderheaders change column dTaxBase bdtaxbase DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 689:
				//Added by TJR 1/21/2015	
				SQL = "alter table orderheaders change column dOrderTaxAmount bdordertaxamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 690:
				//Added by BJZ 1/19/2016	
				SQL = "ALTER TABLE smschedules DROP COLUMN iweek";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN iweekday";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN ifrequencytype";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN iphase";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN susertobereminded";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN datlastrundate";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules MODIFY datstartdate date NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 691:
				//Added by BJZ 1/19/2016	
				SQL = "CREATE TABLE smscheduledusers ("
				+ "  sschedulecode varchar(32) NOT NULL DEFAULT ''"
				+ ", suser varchar(128) NOT NULL DEFAULT ''"
				+ ", datlastacknowledgeddate datetime NOT NULL DEFAULT '0000-00-00 00:00:00'"
				+ ", PRIMARY KEY (sschedulecode, suser)"
				+ ") ENGINE=INNODB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 692:
				//Added by TJR 1/22/2016	
				SQL = "alter table tax change column sTaxJurisdiction staxjurisdiction varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax change column iTaxType itaxtype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax change column sTaxJurisdictionDesc sdescription varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax change column sTaxTypeDesc staxtype varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 693:
				//Added by TJR 1/22/2016	
				SQL = "alter table tax add column icalculateonpurchaseorsale int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add column idisplaytaxoncustomerinvoice int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add column ishowinorderentry int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add column ishowinaccountspayable int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 694:
				//Added by TJR 1/22/2016	
				SQL = "alter table tax drop primary key";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add column lid int(11) primary key auto_increment";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add UNIQUE key jurisdictionitypekey (staxjurisdiction, itaxtype)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table tax add UNIQUE key jurisdictionstypekey (staxjurisdiction, staxtype)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 695:
				//Added by BJZ 1/25/2016	
				SQL = "ALTER TABLE smschedules MODIFY COLUMN sdescription MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules ADD COLUMN datlasteditdate DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules ADD COLUMN slasteditedby VARCHAR (128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smscheduledusers ADD COLUMN datlastrundate DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smschedules DROP COLUMN iremindhowmanydaysinadvance";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 696:
				//Added by TJR 1/29/2016	
				SQL = "ALTER TABLE tax change COLUMN idisplaytaxoncustomerinvoice icalculatetaxoncustomerinvoice int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 697:
				//Added by TJR 1/29/2016
				//Set default values for the new fields in the tax table:
				SQL = "update tax set"
					+ " icalculateonpurchaseorsale = IF(bdtaxrate > 0.00, 1, 0)"
					+ ", icalculatetaxoncustomerinvoice = IF(bdtaxrate > 0.00, 1, 0)"
					+ ", ishowinorderentry = 1"
					+ ", ishowinaccountspayable = 1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 698:
				//Added by TJR 1/29/2016
				SQL = "ALTER TABLE arcustomers ADD COLUMN itaxid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 699:
				//Added by TJR 1/29/2016
				SQL = "UPDATE arcustomers LEFT JOIN tax on ((arcustomers.staxjurisdiction=tax.staxjurisdiction)"
					+ " AND (arcustomers.itaxtype=tax.itaxtype))"
					+ " set arcustomers.itaxid = IF(tax.lid IS NULL, 0, tax.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 700:
				//Added by BJZ 1/29/2016	
				SQL = "ALTER TABLE smscheduledusers CHANGE COLUMN datlastacknowledgeddate datlastacknowledgedreminderdate DATE NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE smscheduledusers ADD COLUMN datlastacknowledgeddate DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 701:
				//Added by TJR 1/29/2016	
				SQL = "ALTER TABLE arcustomershiptos DROP COLUMN sTaxGroup";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 702:
				//Added by TJR 1/29/2016	
				SQL = "ALTER TABLE icpoinvoiceheaders CHANGE COLUMN sTaxGroup staxjurisdiction VARCHAR(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 703:
				//Added by TJR 1/29/2016	
				SQL = "ALTER TABLE icpoinvoiceheaders ADD COLUMN itaxid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icpoinvoiceheaders ADD COLUMN bdtaxrate DECIMAL(7,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icpoinvoiceheaders ADD COLUMN staxtype VARCHAR(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE icpoinvoiceheaders ADD COLUMN icalculateonpurchaseorsale INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 704:
				//Added by TJR 1/29/2016	
				SQL = "UPDATE icpoinvoiceheaders LEFT JOIN tax on ((icpoinvoiceheaders.staxjurisdiction=tax.staxjurisdiction)"
					+ " AND (icpoinvoiceheaders.itaxclass=tax.itaxtype))"
					+ " set icpoinvoiceheaders.itaxid = IF(tax.lid IS NULL, 0, tax.lid)"
					+ ", icpoinvoiceheaders.bdtaxrate = IF(tax.bdtaxrate IS NULL, 0.0000, tax.bdtaxrate)"
					+ ", icpoinvoiceheaders.staxtype = IF(tax.staxtype IS NULL, '', tax.staxtype)"
					+ ", icpoinvoiceheaders.icalculateonpurchaseorsale = IF(tax.icalculateonpurchaseorsale IS NULL, 0, tax.icalculateonpurchaseorsale)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 705:
				//Added by TJR 2/2/2016	
				SQL = "ALTER TABLE orderheaders ADD COLUMN itaxid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 706:
				//Added by TJR 2/2/2016	
				SQL = "ALTER TABLE orderheaders ADD COLUMN staxtype varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 707:
				//Added by TJR 2/2/2016	
				SQL = "ALTER TABLE orderheaders CHANGE COLUMN sTaxGroup staxjurisdiction VARCHAR(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 708:
				//Added by TJR 2/2/2016	
				SQL = "UPDATE orderheaders LEFT JOIN tax on ((orderheaders.staxjurisdiction=tax.staxjurisdiction)"
					+ " AND (orderheaders.iTaxClass=tax.itaxtype))"
					+ " set orderheaders.itaxid = IF(tax.lid IS NULL, 0, tax.lid)"
					+ ", orderheaders.staxtype = IF(tax.staxtype IS NULL, '', tax.staxtype)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 709:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoicedetails CHANGE COLUMN bdlinetaxamount bdlinesalestaxamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 710:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders CHANGE COLUMN bdtaxbase bdsalestaxbase DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 711:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders CHANGE COLUMN bdtaxamount bdsalestaxamount DECIMAL(17,2) NOT NULL DEFAULT '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 712:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders CHANGE COLUMN sTaxGroup staxjurisdiction VARCHAR(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 713:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders ADD COLUMN itaxid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 714:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders ADD COLUMN staxtype VARCHAR(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 715:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders ADD COLUMN icalculateonpurchaseorsale INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 716:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders ADD COLUMN icalculatetaxoncustomerinvoice INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 717:
				//Added by TJR 2/4/2016	
				SQL = "ALTER TABLE invoiceheaders CHANGE COLUMN icalculateonpurchaseorsale icalculatetaxonpurchaseorsale INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			// The following SQL commands should be run at this point - but - 
			// This can't be run until all the tax rates in the tax table are manually set, even for the 'use' taxes
			// It should just be run manually using the 'SM Execute SQL Command' function, not by the program:
			 
			 //Added by TJR 2/4/2016	
			 UPDATE invoiceheaders LEFT JOIN orderheaders ON orderheaders.strimmedordernumber=invoiceheaders.strimmedordernumber
 			 SET invoiceheaders.itaxid = orderheaders.itaxid
 			 WHERE (invoiceheaders.itaxid = 0)
 			 
 			 UPDATE invoiceheaders LEFT JOIN tax on ((invoiceheaders.itaxid=tax.lid)
			  set invoiceheaders.staxtype = IF(tax.staxtype IS NULL, '', tax.staxtype)
			  , invoiceheaders.icalculatetaxoncustomerinvoice = IF(tax.icalculatetaxoncustomerinvoice IS NULL, 0, tax.icalculatetaxoncustomerinvoice)
			  , invoiceheaders.icalculatetaxonpurchaseorsale = IF(tax.icalculateonpurchaseorsale IS NULL, 0, tax.icalculateonpurchaseorsale)
			  , invoiceheaders.bdtaxrate = IF(invoiceheaders.bdtaxrate = 0.0000,IF(tax.bdtaxrate IS NULL, 0.0000, tax.bdtaxrate),invoiceheaders.bdtaxrate)
 			 

			//BEGIN CASE:
			case 718:
				//Added by TJR 2/8/2016	
				SQL = "create table costcenters("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", iactive int(11) NOT NULL DEFAULT '1'"
					+ ", scostcentername varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
					+ ", UNIQUE KEY scostcenternamekey (scostcentername)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 719:
				//Added by BJZ 2/9/2016	
				SQL = "ALTER TABLE glaccounts ADD COLUMN scostcentername VARCHAR(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 720:
				//Added by TJR 2/10/2016	
				SQL = "ALTER TABLE glaccounts DROP COLUMN scostcentername";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 721:
				//Added by TJR 2/10/2016	
				SQL = "ALTER TABLE glaccounts ADD COLUMN icostcenterid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 722:
				//Added by TJR 2/10/2016	
				SQL = "UPDATE smscheduledusers SET datlastacknowledgedreminderdate = NOW() "
						+ "WHERE ( datlastacknowledgedreminderdate = '0000-00-00' )";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 723:
				//Added by BJZ 3/2/2016	
				SQL = "ALTER TABLE workorderdetails ADD COLUMN slocationcode VARCHAR(6) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 724:
				//Added by TJR 3/7/2016	
				SQL = "ALTER TABLE smoptions ADD COLUMN datpostingperiodstartdate DATE NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 725:
				//Added by TJR 3/7/2016	
				SQL = "ALTER TABLE smoptions ADD COLUMN datpostingperiodenddate DATE NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 726:
				//Added by BJZ 3/8/2016	
				SQL = "UPDATE workorderdetails"
				+ " LEFT JOIN workorders on workorders.lid = workorderdetails.lworkorderid"
				+ " LEFT JOIN orderdetails on orderdetails.strimmedordernumber = workorders.strimmedordernumber"
				+ " SET workorderdetails.slocationcode=orderdetails.sLocationCode" 
				+ " WHERE("
				+ " (workorderdetails.lorderdetailnumber=orderdetails.iDetailNumber)"
				+ " AND (orderdetails.sLocationcode != '')"
				+ " ) ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 727:
				//Added by BJZ 3/8/2016	
				SQL = "UPDATE workorderdetails"
				+ " LEFT JOIN workorders on workorders.lid = workorderdetails.lworkorderid"
				+ " LEFT JOIN orderheaders on orderheaders.strimmedordernumber = workorders.strimmedordernumber"
				+ " SET workorderdetails.slocationcode=orderheaders.sLocation" 
				+ " WHERE("
				+ " (workorderdetails.slocationcode = '')"
				+ " AND (orderheaders.sLocation IS NOT NULL)"
				+ " ) ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 728:
				//Added by TJR 3/23/2016	
				SQL = "create table sscontrollers("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", sunitid varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", spasscode varchar(32) NOT NULL DEFAULT ''"
					+ ", dattimelastmaintained DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", slastmaintainedby varchar(128) NOT NULL DEFAULT ''"
					+ ", slastmaintainedbyfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
					+ ", UNIQUE KEY sunitidkey (sunitid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 729:
				//Added by TJR 3/23/2016	
				SQL = "create table ssdevices("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", lcontrollerid int(11) NOT NULL DEFAULT '0'"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", sgpiopinnumber varchar(8) NOT NULL DEFAULT ''"
					+ ", ipintype int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 730:
				//Added by TJR 3/23/2016	
				SQL = "create table ssalarmzones("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", szoneid varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", iarmed int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 731:
				//Added by TJR 3/23/2016	
				SQL = "ALTER table sscontrollers add column scontrollerurl varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER table sscontrollers add column slisteningport varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 732:
				//Added by TJR 3/25/2016	
				SQL = "ALTER TABLE sscontrollers CHANGE COLUMN sunitid scontrollername varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 733:
				//Added by TJR 3/25/2016	
				SQL = "ALTER TABLE ssdevices ADD UNIQUE KEY controllergpiopinkey (lcontrollerid, sgpiopinnumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 734:
				//Added by TJR 3/25/2016	
				SQL = "ALTER TABLE ssalarmzones CHANGE COLUMN szoneid szonename varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE ssalarmzones ADD UNIQUE KEY zonenamekey (szonename)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 735:
				//Added by TJR 3/27/2016	
				SQL = "RENAME TABLE ssalarmzones to sszones";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE sszones ADD column izonetype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 736:
				//Added by TJR 3/27/2016	
				SQL = "ALTER table ssdevices add column lzoneid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 737:
				//Added by TJR 3/27/2016	
				SQL = "create table ssauthorizedzoneusers("
					+ " lzoneid int(11) NOT NULL DEFAULT '0'"
					+ ", suser varchar(128) NOT NULL DEFAULT ''"
					+ ", UNIQUE KEY zoneuserkey(lzoneid, suser)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 738:
				//Added by TJR 3/28/2016	
				SQL = "create table ssdeviceevents("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", ldeviceid int(11) NOT NULL DEFAULT '0'"
					+ ", lcontrollerid int(11) NOT NULL DEFAULT '0'"
					+ ", lzoneid int(11) NOT NULL DEFAULT '0'"
					+ ", scontrollername varchar(32) NOT NULL DEFAULT ''"
					+ ", scontrollerdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", szonename varchar(32) NOT NULL DEFAULT ''"
					+ ", szonedescription varchar(128) NOT NULL DEFAULT ''"
					+ ", sgpiopinnumber varchar(8) NOT NULL DEFAULT ''"
					+ ", ipintype int(11) NOT NULL DEFAULT '0'"
					+ ", ieventtype int(11) NOT NULL DEFAULT '0'"
					+ ", dattimeoccurrence DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", scomment varchar(254) NOT NULL DEFAULT ''"
					+ ", sreferenceid varchar(12) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
					+ ", KEY ldeviceidkey(ldeviceid)"
					+ ", KEY lcontrolleridkey(lcontrollerid)"
					+ ", KEY lzoneidkey(lzoneid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 739:
				//Added by TJR 3/29/2016	
				SQL = "create table ssuserevents("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", ieventtype int(11) NOT NULL DEFAULT '0'"
					+ ", suser varchar(128) NOT NULL DEFAULT ''"
					+ ", suserfullname varchar(128) NOT NULL DEFAULT ''"
					+ ", suserlatitude varchar(24) NOT NULL DEFAULT ''"
					+ ", suserlongitude varchar(24) NOT NULL DEFAULT ''"
					+ ", ldeviceid int(11) NOT NULL DEFAULT '0'"
					+ ", sdevicedescription varchar(128) NOT NULL DEFAULT ''"
					+ ", lzoneid int(11) NOT NULL DEFAULT '0'"
					+ ", szonename varchar(32) NOT NULL DEFAULT ''"
					+ ", szonedescription varchar(128) NOT NULL DEFAULT ''"
					+ ", dattimeoccurrence DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", scomment varchar(254) NOT NULL DEFAULT ''"
					+ ", sreferenceid varchar(12) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY (lid)"
					+ ", KEY ldeviceidkey(ldeviceid)"
					+ ", KEY lzoneidkey(lzoneid)"
					+ ", KEY userkey(suser)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 740:
				//Added by TJR 3/29/2016	
				SQL = "ALTER table ssuserevents add column seventtypelabel varchar(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 741:
				//Added by TJR 3/31/2016	
				SQL = "ALTER table sszones add column ibreachstate int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER table sszones add column snotificationemails varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 742:
				//Added by TJR 3/31/2016	
				SQL = "ALTER table sszones add column ialarmsetdelayinterval int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 743:
				//Added by TJR 4/1/2016	
				SQL = "ALTER table sszones add column datlastarmed DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 744:
				//Added by TJR 4/5/2016	
				SQL = "DROP TABLE ssdevices";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "create table ssdevices("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", linputcontrollerid int(11) NOT NULL DEFAULT '0'"
					+ ", loutputcontrollerid int(11) NOT NULL DEFAULT '0'"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", sinputterminalnumber varchar(8) NOT NULL DEFAULT ''"
					+ ", soutputterminalnumber varchar(8) NOT NULL DEFAULT ''"
					+ ", idevicetype int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE			
			
			//BEGIN CASE:
			case 745:
				//Added by TJR 4/7/2016	
				SQL = "ALTER table ssuserevents change column lzoneid lalarmid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER table ssuserevents change column szonename salarmname varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER table ssuserevents change column szonedescription salarmdescription varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 746:
				//Added by TJR 4/7/2016
				SQL = "RENAME TABLE ssauthorizedzoneusers TO ssdeviceusers";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER table ssdeviceusers change column lzoneid ldeviceid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 747:
				//Added by TJR 4/11/2016
				SQL = "create table ssalarmsequences("
					+ "lid int(11) NOT NULL AUTO_INCREMENT"
					+ ", ialarmstate int(11) NOT NULL DEFAULT '0'"
					+ ", sname varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", semailnotifications varchar(254) NOT NULL DEFAULT ''"
					+ ", lalarmsetdelayinterval int(11) NOT NULL DEFAULT '0'"
					+ ", datlastarmed DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", PRIMARY KEY (lid)"
					+ ", UNIQUE KEY namekey(sname)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 748:
				//Added by TJR 411/2016
				SQL = "DROP TABLE sszones";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 749:
				//Added by TJR 4/12/2016
				SQL = "create table ssalarmtriggerdevices("
					+ "lalarmsequenceid int(11) NOT NULL DEFAULT '0'"
					+ ", ldeviceid int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lalarmsequenceid, ldeviceid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 750:
				//Added by TJR 4/13/2016
				SQL = "create table ssalarmactivationdevices("
					+ "lalarmsequenceid int(11) NOT NULL DEFAULT '0'"
					+ ", ldeviceid int(11) NOT NULL DEFAULT '0'"
					+ ", lactivationduration int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY (lalarmsequenceid, ldeviceid)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 751:
				//Added by TJR 4/13/2016
				SQL = "alter table ssdevices add column iactivationtype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 752:
				//Added by TJR 4/14/2016
				SQL = "alter table ssdevices add column iinputtype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 753:
				//Added by TJR 4/14/2016
				SQL = "alter table ssalarmsequences change column lalarmsetdelayinterval lalarmsetcountdown int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 754:
				//Added by TJR 4/18/2016
				SQL = "alter table ssdeviceevents change column sgpiopinnumber sterminalnumber varchar(8) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table ssdeviceevents add column sdevicedescription varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table ssdeviceevents change column ipintype iterminaltype int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 755:
				//Added by TJR 5/4/2016
				SQL = "create table ssalarmsequenceusers("
					+ " lalarmsequenceid int(11) NOT NULL DEFAULT '0'"
					+ ", suser varchar(128) NOT NULL DEFAULT ''"
					+ ", UNIQUE KEY zoneuserkey(lalarmsequenceid, suser)"
					+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 756:
				//Added by BJZ 5/11/2016
				SQL = "alter table smoptions add column gdriveworkorderparentfolderid varchar(72) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table smoptions add column gdriveworkorderfolderprefix varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table smoptions add column gdriveworkorderfoldersuffix  varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 757:
				//Added by BJZ 5/11/2016
				SQL = "alter table workorders add column sgdoclink varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 758:
				//Added by BJZ 5/17/2016
				SQL = "alter table arcustomers drop column staxjurisdiction";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table arcustomers drop column itaxtype";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//END CASE
			case 759:
				//Added by BJZ 5/17/2016
				SQL = "alter table icpoinvoiceheaders drop column itaxclass";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 760:
				//Added by TJR 6/6/2016
				SQL = "alter table ssalarmsequences add column lsecondstorearm int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 761:
				//Added by TJR 6/6/2016
				SQL = "alter table ssalarmsequences drop column lsecondstorearm";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 762:
				//Added by TJR 6/7/2016
				SQL = "create table ssoptions ("
						+ "itrackuserlocations int(11) NOT NULL DEFAULT '0'"
					+ ") Engine = InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "INSERT INTO ssoptions(itrackuserlocations) VALUES (0)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 763:
				//Added by TJR 6/8/2016
				SQL = "ALTER TABLE ssalarmsequences change column semailnotifications semailnotifications MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 764:
				//Added by TJR 6/13/2016
				SQL = "alter table ssalarmsequences add column slastarmedby varchar(128) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table ssalarmsequences add column slastarmedbyfullname varchar(128) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//BEGIN CASE:
			case 765:
				//Added by TJR 6/13/2016
				SQL = "alter table ssdevices add column sremarks MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 766:
				//Added by TJR 6/16/2016
				SQL = "alter table ssdevices add column iactive int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table sscontrollers add column iactive int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 767:
				//Added by TJR 6/28/2016
				SQL = "alter table securityfunctions add column imodulenumber int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 768:
				//Added by TJR 6/28/2016
				SQL = "alter table securityfunctions drop primary key";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table securityfunctions add primary key(iFunctionID)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 769:
				//Added by BJZ 6/29/2016
				SQL = "alter table servicetypes add mworeceiptcomment MEDIUMTEXT NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table locations drop column sworeceiptcomment";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 770:
				//Added by TJR 7/1/2016
				SQL = "alter table securityfunctions change column imodulenumber imodulelevelsum int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 771:
				//Added by BJZ 7/7/2016
				SQL = "RENAME TABLE smscheduledusers TO reminderusers";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "RENAME TABLE smschedules TO reminders";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 772:
				//Added by BJZ 7/7/2016
				SQL = "create table scheduleentries("
						+ "lid int(11) NOT NULL auto_increment"
						+ ", suser varchar(128) NOT NULL DEFAULT ''"
						+ ", datentrydate DATE NOT NULL DEFAULT '0000-00-00'"
						+ ", isequencenumber int(11) NOT NULL DEFAULT '0'"
						+ ", mcomment MEDIUMTEXT NOT NULL"
						+ ", sordernumber varchar(22) NOT NULL DEFAULT ''"
						+ ", isalescontactid int(11) NOT NULL DEFAULT '0'"
						+ ", ibidid int(11) NOT NULL DEFAULT '0'"
						+ ", saddress1 varchar(60) NOT NULL DEFAULT ''"
						+ ", saddress2 varchar(60) NOT NULL DEFAULT ''"
						+ ", saddress3 varchar(60) NOT NULL DEFAULT ''"
						+ ", saddress4 varchar(60) NOT NULL DEFAULT ''"
						+ ", scity varchar(30) NOT NULL DEFAULT ''"
						+ ", sstate varchar(30) NOT NULL DEFAULT ''"
						+ ", szip varchar(20) NOT NULL DEFAULT ''"
						+ ", sgeocode varchar(64) NOT NULL DEFAULT ''"
						+ ", PRIMARY KEY (lid)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 773:
				//Added by BJZ 7/22/2016
				SQL = "create table schedulegroups("
						+ "igroupid int(11) NOT NULL auto_increment"
						+ ", sschedulegroupname varchar(50) NOT NULL DEFAULT ''"
						+ ", sschedulegroupdesc MEDIUMTEXT NOT NULL"
						+ ", PRIMARY KEY (igroupid)"
						+ ", UNIQUE (sschedulegroupname)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 774:
				//Added by BJZ 7/22/2016
				SQL = "create table scheduleusergroups("
						+ "sschedulegroupname varchar(50) NOT NULL DEFAULT ''"
						+ ", susername varchar(128) NOT NULL DEFAULT ''"
						+ ", PRIMARY KEY (sschedulegroupname, susername)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 775:
				//Added by TJR 9/2/2016
				SQL = "alter table icvendorterms add iminimumdaysallowedforduedayofmonth int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table icvendorterms add iminimumdaysallowedfordiscountduedayofmonth int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 776:
				//Added by TJR 9/7/2016
				SQL = "ALTER TABLE artransactions change column sponumber sponumber varchar(40) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 777:
				//Added by TJR 9/15/2016
				SQL = "ALTER TABLE systemlog change column sLoggingUser sLoggingUser varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 778:
				//Added by BJZ 9/19/2016
				SQL = "ALTER TABLE arcustomers add sinvoicingemail varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE arcustomers add sinvoicingcontact varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE arcustomers add sinvoicingnotes mediumtext NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 779:
				//Added by BJZ 9/19/2016
				SQL = "ALTER TABLE orderheaders add sinvoicingemail varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderheaders add sinvoicingcontact varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE orderheaders add sinvoicingnotes mediumtext NOT NULL";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 780:
				//Added by BJZ 9/19/2016
				SQL = "ALTER TABLE invoiceheaders add iinvoicingstate int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 781:
				//Added by TJR 9/27/2016
				SQL = "ALTER TABLE locations add ishowintruckschedule int(11) NOT NULL DEFAULT '1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 782:
				//Added by TJR 9/7/2016
				SQL = "ALTER TABLE deliverytickets change column sponumber sponumber varchar(40) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 783:
				//Added by BJZ 10/28/2016
				SQL = "ALTER TABLE taxcertificates add sprojectlocation varchar(128) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 784:
				//Added by TR 11/10/2016
				SQL = "ALTER TABLE glexportdetails add sdetailformattedaccountid varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 785:
				//Added by TR 11/10/2016
				SQL = "UPDATE glexportdetails"
					+ " LEFT JOIN glaccounts ON glexportdetails.sdetailaccountid = glaccounts.sacctid"
					+ " SET glexportdetails.sdetailformattedaccountid = glaccounts.sformattedacctid"
					+ " WHERE ("
						+ "(glaccounts.sformattedacctid IS NOT NULL)"
						+ " AND (glexportdetails.sdetailaccountid != '')"
					+ ")"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 786:
				//Added by TJR 12/13/2016
				SQL = "ALTER TABLE ssalarmsequences add datlastdisarmed DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 787:
				//Added by TJR 12/13/2016
				SQL = "create table sseventschedules("
						+ "lid int(11) NOT NULL auto_increment"
						+ ", sdescription varchar(254) NOT NULL DEFAULT ''"
						+ ", sname varchar(32) NOT NULL DEFAULT ''"
						+ ", iactive int(11) NOT NULL DEFAULT '0'"
						+ ", istarttime int(11) NOT NULL DEFAULT '0'"
						+ ", idurationinminutes int(11) NOT NULL DEFAULT '0'"
						+ ", idaysoftheweek int(11) NOT NULL DEFAULT '0'"
						+ ", PRIMARY KEY (lid)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 788:
				//Added by TJR 12/13/2016
				SQL = "create table sseventscheduledetails("
						+ "lid int(11) NOT NULL auto_increment"
						+ ", lsseventscheduleid int(11) NOT NULL DEFAULT '0'"
						+ ", ideviceorsequence int(11) NOT NULL DEFAULT '0'"
						+ ", ldeviceorsequenceid int(11) NOT NULL DEFAULT '0'"
						+ ", iactiontype int(11) NOT NULL DEFAULT '0'"
						+ ", iresetdelay int(11) NOT NULL DEFAULT '0'"
						+ ", PRIMARY KEY (lid)"
						+ ") ENGINE = InnoDB";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 789:
				//Added by TJR 12/16/2016
				SQL = "ALTER TABLE sseventschedules add UNIQUE key namekey (sname)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
*/
			
			//BEGIN CASE:
			case 790:
				//Added by TJR 1/12/2017
				SQL = "ALTER TABLE sseventscheduledetails add UNIQUE key scheduletypeidkey (lsseventscheduleid, ideviceorsequence, ldeviceorsequenceid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 791:
				//Added by TJR 1/12/2017
				SQL = "ALTER TABLE sseventscheduledetails add iactivated int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 792:
				//Added by TJR 1/19/2017
				SQL = "alter table ssalarmsequences add column slastdisarmedby varchar(128) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "alter table ssalarmsequences add column slastdisarmedbyfullname varchar(128) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//BEGIN CASE:
			case 793:
				//Added by TJR 2/14/2017
				SQL = "ALTER TABLE invoicedetails add ilaboritem int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 794:
				//Added by TJR 2/14/2017
				SQL = "UPDATE invoicedetails LEFT JOIN icitems ON invoicedetails.sItemNumber=icitems.sitemnumber"
					+ " SET invoicedetails.ilaboritem = "
						+ "IF (icitems.sitemnumber is null, "
							+ "if(LEFT(invoicedetails.sItemNumber,3) = 'LAB', 1, 0), icitems.ilaboritem)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 795:
				//Added by TJR 2/28/2017
				SQL = "ALTER TABLE icporeceiptheaders"
					+ " ADD dattimelastupdated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", ADD slastupdateuser VARCHAR(128) NOT NULL DEFAULT ''"
					+ ", ADD slastupdateprocess VARCHAR(64) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 796:
				//Added by TJR 3/7/2017
				SQL = "create table apbatchentries("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"	
					+ ", lentrynumber int(11) NOT NULL DEFAULT '0'"
					+ ", ientrytype int(11) NOT NULL DEFAULT '0'"	
					+ ", sdocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", sentrydescription varchar(128) NOT NULL DEFAULT ''"
					+ ", datentrydate date NOT NULL default '0000-00-00'"
					+ ", llastline int(11) NOT NULL DEFAULT '0'"
					+ ", bdentryamount decimal(17,2) NOT NULL default '0.00'"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `batch_entry_key` (`lbatchnumber`,`lentrynumber`)"
					+ ", KEY `entrynumberkey` (`lentrynumber`)"
					+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 797:
				//Added by TJR 3/7/2017
				SQL = "CREATE TABLE `apbatches` ("
					+ "`lbatchnumber` int(11) NOT NULL auto_increment"
					+ ", `datbatchdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", `ibatchstatus` int(11) NOT NULL default '0'"
					+ ", `sbatchdescription` varchar(128) NOT NULL default ''"
					+ ", `ibatchtype` int(11) NOT NULL default '0'"
					+ ", `datlasteditdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", `lbatchlastentry` int(11) NOT NULL default '0'"
					+ ", `screatedby` varchar(128) NOT NULL default ''"
					+ ", `slasteditedby` varchar(128) NOT NULL default ''"
					+ ", `datpostdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", PRIMARY KEY  (`lbatchnumber`)"
					+ " ) ENGINE=InnoDB"
				;
				
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 798:
				//Added by TJR 3/7/2017
				SQL = "ALTER TABLE apoptions ADD COLUMN icreatetestbatchesfrompoinvoices int(11) NOT NULL DEFAULT '0'"
				;
				
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 799:
				//Added by TJR 3/7/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN lponumber int(11) NOT NULL DEFAULT '0'"
				;
				
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 800:
				//Added by TJR 3/7/2017
				SQL = "create table apbatchentrylines("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"	
					+ ", lentrynumber int(11) NOT NULL DEFAULT '0'"
					+ ", llinenumber int(11) NOT NULL DEFAULT '0'"
					+ ", sdistributioncodename varchar(32) NOT NULL DEFAULT ''"
					+ ", bdamount decimal(17,2) NOT NULL default '0.00'"
					+ ", scontrolacct varchar(75) NOT NULL DEFAULT ''"
					+ ", sdistributionacct varchar(75) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(75) NOT NULL DEFAULT ''"
					+ ", scomment varchar(254) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `batch_entry_line_key` (`lbatchnumber`,`lentrynumber`, `llinenumber`)"
					+ ", KEY `batchnumberkey` (`lbatchnumber`)"
					+ ", KEY `entrynumberkey` (`lentrynumber`)"
					+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 801:
				//Added by TJR 3/10/2017
				SQL = "ALTER TABLE apbatchentries DROP COLUMN lponumber";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE apbatchentries ADD COLUMN scontrolacct VARCHAR(75) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE apbatchentries ADD COLUMN svendoracct VARCHAR(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 802:
				//Added by TJR 3/10/2017
				SQL = "ALTER TABLE apbatchentrylines DROP COLUMN scontrolacct";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE apbatchentrylines ADD COLUMN lpoheaderid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE apbatchentrylines ADD COLUMN lreceiptheaderid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 803:
				//Added by TJR 3/10/2017
				SQL = "ALTER TABLE apoptions ADD COLUMN iexportto int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 804:
				//Added by TJR 3/16/2017
				SQL = "create table aptransactions("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", loriginalbatchnumber int(11) NOT NULL DEFAULT '0'"	
					+ ", loriginalentrynumber int(11) NOT NULL DEFAULT '0'"
					+ ", svendor varchar(12) NOT NULL DEFAULT ''"
					+ ", sdocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", idoctype int(11) NOT NULL DEFAULT '0'"
					+ ", datdocdate date NOT NULL default '0000-00-00'"
					+ ", datduedate date NOT NULL default '0000-00-00'"
					+ ", doriginalamt decimal(17,2) NOT NULL default '0.00'"
					+ ", dcurrentamt decimal(17,2) NOT NULL default '0.00'"
					+ ", sdocdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", scontrolacct varchar(75) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `batch_entry_key` (`loriginalbatchnumber`,`loriginalentrynumber`)"
					+ ", KEY `batchnumberkey` (`loriginalbatchnumber`)"
					+ ", KEY `entrynumberkey` (`loriginalentrynumber`)"
					+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 805:
				//Added by TJR 3/16/2017
				SQL = "create table apmatchinglines("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", svendor varchar(12) NOT NULL DEFAULT ''"
					+ ", damount decimal(17,2) NOT NULL default '0.00'"
					+ ", sdescription varchar(75) NOT NULL DEFAULT ''"
					+ ", dattransactiondate date NOT NULL default '0000-00-00'"
					+ ", sappliedfromdocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", sappliedtodocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", ltransactionappliedfromid int(11) NOT NULL DEFAULT '0'"	
					+ ", ltransactionappliedtoid int(11) NOT NULL DEFAULT '0'"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", KEY `vendor_key` (`svendor`)"
					+ ", KEY `vendor_appliedfromdocnumber_key` (`svendor`,`sappliedfromdocnumber`)"
					+ ", KEY `sappliedfromdocnumber_key` (`sappliedfromdocnumber`)"
					+ ", KEY `ltransactionappliedfromid_key` (`ltransactionappliedfromid`)"
					+ ", KEY `ltransactionappliedtoid_key` (`ltransactionappliedtoid`)"
					+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 806:
				//Added by TJR 3/17/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN datdocdate date NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 807:
				//Added by TJR 3/17/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN datdiscount date NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 808:
				//Added by TJR 3/17/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN datduedate date NOT NULL DEFAULT '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 809:
				//Added by BJZ 3/16/2017
				SQL = "ALTER TABLE ssdevices ADD COLUMN icontactduration bigint(64) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "update ssdevices SET icontactduration = 500";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 810:
				//Added by TJR 3/21/2017
				SQL = "ALTER TABLE apbatchentrylines MODIFY COLUMN sdescription VARCHAR(96) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 811:
				//Added by BJZ 3/21/2017
				SQL = "ALTER TABLE laborbackcharges ADD COLUMN svendoritemnumber VARCHAR(24) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 812:
				//Added by BJZ 3/21/2017
				SQL = "ALTER TABLE sseventschedules MODIFY COLUMN sname VARCHAR(64) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 813:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN sterms varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 814:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN bddiscount decimal(17,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 815:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN svendorname varchar(60) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 816:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN staxjurisdiction varchar(12) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 817:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN itaxid int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 818:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN bdtaxrate decimal(7,4) NOT NULL DEFAULT '0.0000'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 819:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN staxtype varchar(254) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 820:
				//Added by TJR 3/27/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN icalculateonpurchaseorsale int(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 821:
				//Added by TJR 3/29/2017
				SQL = "ALTER TABLE icvendors ADD COLUMN sdefaultdistributioncode varchar(32) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 822:
				//Added by TJR 3/29/2017
				SQL = "ALTER TABLE icvendors ADD COLUMN sdefaultinvoicelinedesc varchar(96) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 823:
				//Added by TJR 4/3/2017
				SQL = "ALTER TABLE apbatchentries ADD COLUMN sapplytodocnumber varchar(75) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 824:
				//Added by TJR 4/3/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN schecknumber varchar(12) NOT NULL DEFAULT '' AFTER sapplytodocnumber"
					+ ", ADD COLUMN sremittocode varchar(12) NOT NULL DEFAULT '' AFTER schecknumber"
					+ ", ADD COLUMN sremittoname varchar(60) NOT NULL DEFAULT '' AFTER sremittocode"
					+ ", ADD COLUMN sremittoaddressline1 varchar(60) NOT NULL DEFAULT '' AFTER sremittoname"
					+ ", ADD COLUMN sremittoaddressline2 varchar(60) NOT NULL DEFAULT '' AFTER sremittoaddressline1"
					+ ", ADD COLUMN sremittoaddressline3 varchar(60) NOT NULL DEFAULT '' AFTER sremittoaddressline2"
					+ ", ADD COLUMN sremittoaddressline4 varchar(60) NOT NULL DEFAULT '' AFTER sremittoaddressline3"
					+ ", ADD COLUMN sremittocity varchar(30) NOT NULL DEFAULT '' AFTER sremittoaddressline4"
					+ ", ADD COLUMN sremittostate varchar(30) NOT NULL DEFAULT '' AFTER sremittocity"
					+ ", ADD COLUMN sremittopostalcode varchar(20) NOT NULL DEFAULT '' AFTER sremittostate"
					+ ", ADD COLUMN sremittocountry varchar(30) NOT NULL DEFAULT '' AFTER sremittopostalcode"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 825:
				//Added by BJZ 4/13/2017
				SQL = "ALTER TABLE smoptions ADD COLUMN isignatureboxwidth int(11) NOT NULL DEFAULT 400";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 826:
				//Added by TJR 4/14/2017
				SQL = "ALTER TABLE apbatchentrylines"
					+ " ADD COLUMN lapplytodocid int(11) NOT NULL DEFAULT '0' AFTER lreceiptheaderid"
					+ ", ADD COLUMN sapplytodocnumber varchar(75) NOT NULL DEFAULT '' AFTER lapplytodocid"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 827:
				//Added by TJR 4/20/2017
				SQL = "ALTER TABLE aptransactions"
					+ " ADD COLUMN datdiscountdate  date NOT NULL default '0000-00-00'"
					+ ", ADD COLUMN bdoriginaldiscountavailable decimal(17,2) NOT NULL default '0.00'"
					+ ", ADD COLUMN bdcurrentdiscountavailable decimal(17,2) NOT NULL default '0.00'"
					+ ", ADD COLUMN ionhold int(11) NOT NULL DEFAULT '0'"
					+ ", CHANGE COLUMN doriginalamt bdoriginalamt decimal(17,2) NOT NULL default '0.00'"
					+ ", CHANGE COLUMN dcurrentamt bdcurrentamt decimal(17,2) NOT NULL default '0.00'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 828:
				//Added by TJR 4/20/2017
				SQL = "ALTER TABLE apmatchinglines"
					+ " ADD COLUMN bddiscountappliedamount decimal(17,2) NOT NULL default '0.00'"
					+ ", CHANGE COLUMN damount bdappliedamount decimal(17,2) NOT NULL default '0.00'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 829:
				//Added by TJR 4/20/2017
				SQL = "ALTER TABLE apbatchentrylines"
					+ " ADD COLUMN bdapplieddiscountamt decimal(17,2) NOT NULL default '0.00'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 830:
				//Added by TJR 4/20/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN ionhold int(11) NOT NULL default '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 831:
				//Added by TJR 4/27/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN lbankid int(11) NOT NULL default '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 832:
				//Added by BJZ 5/08/2017
				SQL = "ALTER TABLE workorders"
					+ " ADD COLUMN lsignatureboxwidth int(11) NOT NULL default '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE workorders SET lsignatureboxwidth = 400"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 833:
				//Added by BJZ 5/08/2017
				SQL = "ALTER TABLE deliverytickets"
					+ " ADD COLUMN lsignatureboxwidth int(11) NOT NULL default '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "UPDATE deliverytickets SET lsignatureboxwidth = 400"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 834:
				//Added by TJR 5/11/2017
				SQL = "ALTER TABLE icvendors"
					+ " ADD COLUMN igenerateseparatepaymentsforeachinvoice int(11) NOT NULL default '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 835:
				//Added by TJR 5/16/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " DROP COLUMN sapplytodocnumber"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 836:
				//Added by TJR 5/16/2017
				SQL = "ALTER TABLE apbatchentrylines"
					+ " ADD COLUMN iapplytodoctype INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 837:
				//Added by TJR 5/22/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN lsalesordernumber INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN lpurchaseordernumber INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN sapplytoinvoicenumber VARCHAR(75) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 838:
				//Added by TJR 5/22/2017
				SQL = "ALTER TABLE aptransactions"
					+ " ADD COLUMN lapplytopurchaseorderid INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN lapplytosalesorderid INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN sapplytoinvoicenumber VARCHAR(75) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 839:
				//Added by BJZ 5/25/2017
				SQL = "ALTER TABLE scheduleentries"
					+ " ADD COLUMN sshiptoname VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sbilltoname VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN scontactname VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sphone VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN semail VARCHAR(128) NOT NULL DEFAULT ''"
					+ ", CHANGE COLUMN isequencenumber iminuteofday INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 840:
				//Added by BJZ 5/25/2017
				SQL = "RENAME TABLE scheduleentries TO appointments"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 841:
				//Added by BJZ 5/25/2017
				SQL = "ALTER TABLE schedulegroups"
					+ " CHANGE COLUMN sschedulegroupname sappointmentgroupname VARCHAR(50) NOT NULL DEFAULT ''"
					+ ", CHANGE COLUMN sschedulegroupdesc sappointmentgroupdesc mediumtext NOT NULL"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			case 842:
				//Added by BJZ 5/25/2017
				SQL = "RENAME TABLE schedulegroups TO appointmentgroups"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 843:
				//Added by BJZ 5/25/2017
				SQL = "ALTER TABLE scheduleusergroups"
					+ " CHANGE COLUMN sschedulegroupname sappointmentgroupname VARCHAR(50) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 844:
				//Added by BJZ 5/25/2017
				SQL = "RENAME TABLE scheduleusergroups TO appointmentusergroups"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 845:
				//Added by TJR 5/26/2017
				SQL = "create table aptransactionlines("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", ltransactionheaderid int(11) NOT NULL DEFAULT '0'"
					+ ", loriginalbatchnumber int(11) NOT NULL DEFAULT '0'"
					+ ", loriginalentrynumber int(11) NOT NULL DEFAULT '0'"
					+ ", loriginallinenumber int(11) NOT NULL DEFAULT '0'"
					+ ", bdamount decimal(17,2) NOT NULL default '0.00'"
					+ ", sdistributioncodename varchar(32) NOT NULL DEFAULT ''"
					+ ", sdistributionacct varchar(75) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(96) NOT NULL DEFAULT ''"
					+ ", scomment varchar(254) NOT NULL DEFAULT ''"
					+ ", lpoheaderid int(11) NOT NULL DEFAULT '0'"
					+ ", lreceiptheaderid int(11) NOT NULL DEFAULT '0'"
					+ ", lporeceiptlineid int(11) NOT NULL DEFAULT '0'"
					+ ", lapplytodocid int(11) NOT NULL DEFAULT '0'"
					+ ", sapplytodocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", KEY `transaction_header_key` (`ltransactionheaderid`)"
					+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 846:
				//Added by TJR 5/30/2017
				SQL = "ALTER TABLE apbatchentrylines"
					+ " ADD COLUMN lporeceiptlineid INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 847:
				//Added by TJR 6/2/2017
				SQL = "ALTER TABLE aptransactions"
					+ " DROP KEY batch_entry_key"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 848:
				//Added by TJR 6/2/2017
				SQL = "ALTER TABLE aptransactions"
					+ " ADD KEY vendor_key (svendor)"
					+ ", ADD KEY docnumber_key (sdocnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 849:
				//Added by TJR 6/2/2017
				SQL = "ALTER TABLE apmatchinglines"
					+ " ADD KEY applytodoc_key (sappliedtodocnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 850:
				//Added by TJR 6/9/2017
				SQL = "ALTER TABLE apvendorstatistics"
					+ " ADD lnumberofpayapplicationsusedforaveraging INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 851:
				//Added by TJR 6/9/2017
				SQL = "ALTER TABLE apvendorstatistics"
					+ " DROP laveragedaystopay"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 852:
				//Added by BJZ 6/19/2017
				SQL = "ALTER TABLE appointments"
					+ " ADD COLUMN datcreatedtime DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE appointments"
					+ " ADD COLUMN screateduser VARCHAR(128) NOT NULL DEFAULT ''";
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 853:
				//Added by TJR 6/20/2017
				SQL = "ALTER TABLE aptransactionlines"
					+ " ADD COLUMN bddiscountappliedamount  decimal(17,2) NOT NULL default '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 854:
				//Added by TJR 6/27/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN iprintcheck INT(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 855:
				//Added by TJR 6/30/2017
				SQL = "ALTER TABLE icvendors"
					+ " ADD COLUMN ivendorgroupid INT(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 856:
				//Added by BJZ 6/30/2017
				SQL = "ALTER TABLE users"
					+ " ADD COLUMN susercolorrow INT(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE users"
					+ " ADD COLUMN susercolorcol INT(11) NOT NULL default '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 857:
				//Added by TJR 7/7/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY datInvoiceDate_key (datInvoiceDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 858:
				//Added by TJR 7/7/2017
				SQL = "ALTER TABLE armatchinglines"
					+ " ADD KEY dattransactiondate_key (dattransactiondate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 859:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY iLineNumber_key (iLineNumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 860:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY sSalesperson_key (sSalesperson)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 861:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY sServiceTypeCode_key (sServiceTypeCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 862:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY sLocation_key (sLocation)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 863:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY iSalesGroup_key (iSalesGroup)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 864:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY datOrderDate_key (datOrderDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 865:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY iSalesGroup_key (iSalesGroup)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 866:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY sServiceTypeCode_key (sServiceTypeCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 867:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY iinvoicingstate_key (iinvoicingstate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 868:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE criticaldates"
					+ " ADD KEY Responsible_key (Responsible)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 869:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE criticaldates"
					+ " ADD KEY datcriticaldate_key (datcriticaldate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 870:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE workorders"
					+ " ADD KEY dattimedone_key (dattimedone)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 871:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE workorders"
					+ " ADD KEY ijoborder_key (ijoborder)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 872:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE bids"
					+ " ADD KEY dattimebiddate_key (dattimebiddate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 873:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE ssuserevents"
					+ " ADD KEY dattimeoccurrence_key (dattimeoccurrence)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 874:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE ssdeviceevents"
					+ " ADD KEY dattimeoccurrence_key (dattimeoccurrence)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 875:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icporeceiptlines"
					+ " ADD KEY llinenumber_key (llinenumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 876:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icpoheaders"
					+ " ADD KEY datpodate_key (datpodate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 877:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icpolines"
					+ " ADD KEY llinenumber_key (llinenumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 878:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icentrylines"
					+ " ADD KEY lentrynumber_key (lentrynumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 879:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icvendoritems"
					+ " ADD KEY svendoritemnumber_key (svendoritemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 880:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icporeceiptlines"
					+ " ADD KEY sglexpenseacct_key (sglexpenseacct)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 881:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icpoheaders"
					+ " ADD KEY svendor_key (svendor)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 882:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE icporeceiptheaders"
					+ " ADD KEY screatedby_key (screatedby)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 883:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE ictransactiondetails"
					+ " ADD KEY ldetailnumber_key (ldetailnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 884:
				//Added by TJR 7/10/2017
				SQL = "ALTER TABLE artransactions"
					+ " ADD KEY iretainage_key (iretainage)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 885:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY LASTEDITUSER_key (LASTEDITUSER)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 886:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY sCustomerCode_key (sCustomerCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 887:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY itaxid_key (itaxid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 888:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY iOrderType_key (iOrderType)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 889:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY datLastPostingDate_key (datLastPostingDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 890:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY datOrderCanceledDate_key (datOrderCanceledDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 891:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY dQtyShipped_key (dQtyShipped)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 892:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icpoheaders"
					+ " ADD KEY lstatus_key (lstatus)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 893:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icpolines"
					+ " ADD KEY slocation_key (slocation)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 894:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icporeceiptlines"
					+ " ADD KEY bdqtyreceived_key (bdqtyreceived)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 895:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icporeceiptheaders"
					+ " ADD KEY lpostedtoic_key (lpostedtoic)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 896:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icporeceiptheaders"
					+ " ADD KEY lstatus_key (lstatus)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 897:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE icporeceiptlines"
					+ " ADD KEY slocation_key (slocation)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 898:
				//Added by TJR 7/11/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY sLocationCode_key (sLocationCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 899:
				//Added by TJR 7/12/2017
				SQL = "ALTER TABLE icitems"
					+ " ADD KEY sdedicatedtoordernumber_key (sdedicatedtoordernumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 900:
				//Added by TJR 7/12/2017
				SQL = "ALTER TABLE icitemlocations"
					+ " ADD KEY bdqtyonhand_key (bdqtyonhand)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 901:
				//Added by TJR 7/13/2017
				SQL = "ALTER TABLE icitemlocations"
					+ " ADD KEY bdtotalcost_key (bdtotalcost)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 902:
				//Added by TJR 7/13/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY dQtyOrdered_key (dQtyOrdered)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 903:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE taxcertificates"
					+ " ADD KEY scustomernumber_key (scustomernumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 904:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE taxcertificates"
					+ " ADD KEY sjobnumber_key (sjobnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 905:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icpoheaders"
					+ " ADD KEY screatedby_key (screatedby)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 906:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE bids"
					+ " ADD KEY iprojecttype_key (iprojecttype)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 907:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE reminderusers"
					+ " ADD KEY sschedulecode_key (sschedulecode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 908:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE reminders"
					+ " ADD KEY sschedulecode_key (sschedulecode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 909:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE users"
					+ " ADD KEY sDefaultSalespersonCode_key (sDefaultSalespersonCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 910:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE workorderdetails"
					+ " ADD KEY sitemnumber_key (sitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 911:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE criticaldates"
					+ " ADD KEY iresolved_key (iresolved)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 912:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE orderheaders"
					+ " ADD KEY sOrderCreatedBy_key (sOrderCreatedBy)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 913:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE defaultsalesgroupsalespersons"
					+ " ADD KEY ssalespersoncode_key (ssalespersoncode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 914:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE invoicedetails"
					+ " ADD KEY iDetailNumber_key (iDetailNumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 915:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY sSalesperson_key (sSalesperson)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 916:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE salescontacts"
					+ " ADD KEY salespersoncode_key (salespersoncode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 917:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE invoiceheaders"
					+ " ADD KEY sCreatedBy_key (sCreatedBy)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 918:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE invoicedetails"
					+ " ADD KEY sExpenseGLAcct_key (sExpenseGLAcct)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 919:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE glaccounts"
					+ " ADD KEY iCostCenterID_key (iCostCenterID)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 920:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE fa_master"
					+ " ADD KEY sAssetNumber_key (sAssetNumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 921:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE fa_master"
					+ " ADD KEY sClass_key (sClass)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 922:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE fa_transactions"
					+ " ADD KEY sTransAssetNumbers_key (sTransAssetNumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 923:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE fa_transactions"
					+ " ADD KEY datTransactionDate_key (datTransactionDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 924:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE fa_classes"
					+ " ADD KEY sClass_key (sClass)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 925:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icentrylines"
					+ " ADD KEY lreceiptlineid_key (lreceiptlineid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 926:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icentrylines"
					+ " ADD KEY sitemnumber_key (sitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 927:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icbatches"
					+ " ADD KEY smoduletype_key (smoduletype)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 928:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE glexportheaders"
					+ " ADD KEY ssourceledger_key (ssourceledger)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 929:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE glexportdetails"
					+ " ADD KEY sdetailsourceledger_key (sdetailsourceledger)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 930:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE iccosts"
					+ " ADD KEY lreceiptlineid_key (lreceiptlineid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 931:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icbatches"
					+ " ADD KEY screatedby_key (screatedby)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 932:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icinventoryworksheet"
					+ " ADD KEY sitemnumber_key (sitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 933:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icphysicalcountlines"
					+ " ADD KEY sitemnumber_key (sitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 934:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icpolines"
					+ " ADD KEY svendorsitemnumber_key (svendorsitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 935:
				//Added by TJR 7/15/2017
				SQL = "ALTER TABLE icpoinvoiceheaders"
					+ " ADD KEY lexportsequencenumber_key (lexportsequencenumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 936:
				//Added by TJR 7/19/2017
				SQL = "ALTER TABLE materialreturns"
					+ " ADD KEY iworkorderid_key (iworkorderid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 937:
				//Added by TJR 7/19/2017
				SQL = "ALTER TABLE arcustomers"
					+ " ADD KEY sAccountSet_key (sAccountSet)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 938:
				//Added by TJR 7/20/2017
				SQL = "ALTER TABLE salesperson"
					+ " ADD KEY sSalespersonUserName_key (sSalespersonUserName)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 939:
				//Added by TJR 7/20/2017
				SQL = "ALTER TABLE salesperson"
					+ " ADD KEY sSalespersonCode_key (sSalespersonCode)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 940:
				//Added by TJR 7/21/2017
				SQL = "ALTER TABLE icpolines"
					+ " ADD KEY bdqtyordered_key (bdqtyordered)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 941:
				//Added by TJR 7/21/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY dQtyShippedToDate_key (dQtyShippedToDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 942:
				//Added by TJR 7/21/2017
				SQL = "ALTER TABLE orderdetails"
					+ " ADD KEY datLineBookedDate_key (datLineBookedDate)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//**************************
			
			/* - Possible new keys:
			x orderheaders.sSalesperson
			x orderheaders.sServiceTypeCode
			x orderheaders.sLocation
			x orderheaders.iSalesGroup
			x orderheaders.datOrderDate
			orderheaders.sBillToName
			orderheaders.sBillToAddressLine1
			orderheaders.sBillToAddressLine2
			orderheaders.sBillToAddressLine3
			orderheaders.sBillToAddressLine4
			orderheaders.sBillToCity
			orderheaders.sShipToName
			orderheaders.sShipToAddress1
			orderheaders.sShipToAddress2
			orderheaders.sShipToAddress3
			orderheaders.sShipToAddress4
			orderheaders.sShipToCity
			
			x invoiceheaders.iSalesGroup
			x invoiceheaders.sServiceTypeCode
			x invoiceheaders.iinvoicingstate
			
			x criticaldates.Responsible
			x criticaldates.datcriticaldate
			x workorders.dattimedone
			x workorders.ijoborder
			x bids.dattimebiddate
			x ssuserevents.dattimeoccurrence
			x ssdeviceevents.dattimeoccurrence
			x icporeceiptlines.llinenumber
			x icpoheaders.datpodate
			x icpolines.llinenumber
			x icentrylines.lentrynumber
			x icvendoritems.svendoritemnumber
			x icporeceiptlines.llinenumber
			x icporeceiptlines.sglexpenseacct
			x icpoheaders.svendor
			x icporeceiptheaders.screatedby
			x ictransactiondetails.ldetailnumber
			x artransactions.iretainage
			
			x orderheaders.LASTEDITUSER
			x orderheaders.sCustomerCode
			x orderheaders.itaxid
			x orderheaders.iOrderType
			x orderheaders.datLastPostingDate
			x orderheaders.datOrderCanceledDate
			x orderdetails.dQtyShipped
			
			x icpoheaders.lstatus
			x icpolines.slocation
			x icporeceiptlines.bdqtyreceived
			x icporeceiptheaders.lpostedtoic
			x icporeceiptheaders.lstatus
			
			x icporeceiptlines.slocation
			x orderdetails.sLocationCode
			
			x icitems.sdedicatedtoordernumber
			x icitemlocations.bdqtyonhand
			
			x orderdetails.dQtyOrdered
			x icitemlocations.bdtotalcost
			
			//TODO:
			x taxcertificates.scustomernumber
			x taxcertificates.sjobnumber
			x icpoheaders.screatedby
			x bids.iprojecttype
			x reminderusers.sschedulecode
			x reminders.sschedulecode
			x users.sDefaultSalespersonCode
			x workorderdetails.sitemnumber
			x criticaldates.iresolved
			x orderheaders.sOrderCreatedBy
			x defaultsalesgroupsalespersons.ssalespersoncode
			x invoicedetails.iDetailNumber
			x invoiceheaders.sSalesperson
			x salescontacts.salespersoncode
			x invoiceheaders.sCreatedBy
			x invoicedetails.sExpenseGLAcct
			x glaccounts.iCostCenterID
			x fa_master.sAssetNumber
			x fa_master.sClass
			x fa_transactions.sTransAssetNumber
			x fa_transactions.datTransactionDate
			x fa_classes.sClass
			x icentrylines.lreceiptlineid
			x icentrylines.sitemnumber
			x icbatches.smoduletype
			x glexportheaders.ssourceledger
			x glexportdetails.sdetailsourceledger
			x iccosts.lreceiptlineid
			x icbatches.screatedby
			x icinventoryworksheet.sitemnumber
			x icphysicalcountlines.sitemnumber
			x icpolines.svendorsitemnumber
			x icpoinvoiceheaders.lexportsequencenumber
			
			x materialreturns.iworkorderid
			X arcustomers.sAccountSet
			x salesperson.sSalespersonUserName
  			x salesperson.sSalespersonCode
  			x icpolines.bdqtyordered
  			x orderdetails.dQtyShippedToDate
  			X orderdetails.datLineBookedDate
			*/
			
			//BEGIN CASE:
			case 943:
				//Added by TJR 8/15/2017
				SQL = "ALTER TABLE aptransactions"
					+ " ADD COLUMN schecknumber VARCHAR(12) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 944:
				//Added by TJR 8/17/2017
				SQL = "CREATE TABLE apcheckforms("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", sname varchar(32) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", mtext MEDIUMTEXT NOT NULL"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `name_key` (`sname`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 945:
				//Added by TJR 8/22/2017
				SQL = "ALTER TABLE bkbanks"
					+ " ADD COLUMN sroutingnumber VARCHAR(60) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 946:
				//Added by TJR 8/22/2017
				SQL = "ALTER TABLE bkbanks"
					+ " ADD COLUMN saddressline1 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN saddressline2 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN saddressline3 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN saddressline4 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN scity VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sstate VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN scountry VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN spostalcode VARCHAR(20) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 947:
				//Added by TJR 8/23/2017
				SQL = "ALTER TABLE apcheckforms"
					+ " ADD COLUMN inumberofadvicelinesperpage INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 948:
				//Added by TJR 8/29/2017
				SQL = "CREATE TABLE apchecks("
						+ "lid int(11) NOT NULL auto_increment"
						+ ", schecknumber varchar(12) NOT NULL DEFAULT ''"
						+ ", lbankid int(11) NOT NULL DEFAULT '0'"
						+ ", lcheckformid int(11) NOT NULL DEFAULT '0'"
						+ ", bdamount  decimal(17,2) NOT NULL default '0.00'"
						+ ", datcheckdate DATE NOT NULL default '0000-00-00'"
						+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"
						+ ", lentrynumber int(11) NOT NULL DEFAULT '0'"
						+ ", ltransactionid int(11) NOT NULL DEFAULT '0'"
						+ ", ivoid int(11) NOT NULL DEFAULT '0'"
						+ ", iposted int(11) NOT NULL DEFAULT '0'"
						+ ", iprinted int(11) NOT NULL DEFAULT '0'"
						+ ", screatedby varchar(128) NOT NULL DEFAULT ''"
						+ ", PRIMARY KEY  (`lid`)"
						+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 949:
				//Added by TJR 8/29/2017
				SQL = "ALTER TABLE bkbanks"
					+ " ADD COLUMN icheckformid INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN lnextchecknumber INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 950:
				//Added by TJR 8/31/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN svendoracct VARCHAR(12) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 951:
				//Added by TJR 8/31/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN svendorname VARCHAR(60) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 952:
				//Added by TJR 9/5/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN ipagenumber INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN ilastpage INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 953:
				//Added by TJR 9/6/2017
				SQL = "ALTER TABLE apchecks add UNIQUE key `batchentrypagekey`(lbatchnumber, lentrynumber, ipagenumber)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 954:
				//Added by TJR 9/7/2017
				SQL = "CREATE TABLE apchecklines("
					+ "lid int(11) NOT NULL auto_increment"
					+ ", lcheckid int(11) NOT NULL DEFAULT '0'"
					+ ", lchecklinenumber int(11) NOT NULL DEFAULT '0'"
					+ ", bdgrossamount  decimal(17,2) NOT NULL default '0.00'"
					+ ", bddeductions  decimal(17,2) NOT NULL default '0.00'"
					+ ", bdnetpaid  decimal(17,2) NOT NULL default '0.00'"
					+ ", lbatchnumber int(11) NOT NULL DEFAULT '0'"
					+ ", lentrynumber int(11) NOT NULL DEFAULT '0'"
					+ ", lentrylinenumber int(11) NOT NULL DEFAULT '0'"
					+ ", sapplytdocnumber varchar(75) NOT NULL DEFAULT ''"
					+ ", datapplydocdate DATE NOT NULL default '0000-00-00'"
					+ ", PRIMARY KEY (`lid`)"
					+ ", UNIQUE KEY checklinenumberkey (`lcheckid`, `lchecklinenumber`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 955:
				//Added by TJR 9/11/2017
				SQL = "ALTER TABLE apchecks add lbatchentryid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 956:
				//Added by TJR 9/18/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN sremittoname VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittoaddressline1 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittoaddressline2 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittoaddressline3 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittoaddressline4 VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittocity VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittostate VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittocountry VARCHAR(30) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN sremittopostalcode VARCHAR(20) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 957:
				//Added by BJZ 9/18/2017
				SQL = "ALTER TABLE appointments"
					+ " ADD COLUMN inotificationtime INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN inotificationsent INT(11) NOT NULL DEFAULT '0'"

				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 958:
				//Added by TJR 9/18/2017
				SQL = "ALTER TABLE apbatches"
					+ " ADD COLUMN ichecksprinted INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 959:
				//Added by TJR 9/22/2017
				SQL = "ALTER TABLE aptransactions"
					+ " ADD COLUMN lbatchentryid INT(11) NOT NULL DEFAULT '0'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 960:
				//Added by BJZ 10/4/2017
				SQL = "ALTER TABLE bids"
					+ " ADD COLUMN screatedfromordernumber VARCHAR(22) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 961:
				//Added by TJR 11/2/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN dattimecreated DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'" 
					+ ", ADD COLUMN dattimeprinted DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
					+ ", ADD COLUMN screatedbyfullname VARCHAR(128) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 962:
				//Added by TJR 11/2/2017
				SQL = "ALTER TABLE apchecks"
					+ " ADD COLUMN sprintedby VARCHAR(128) NOT NULL DEFAULT ''" 
					+ ", ADD COLUMN sprintedbyfullname VARCHAR(128) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 963:
				//Added by TJR 11/3/2017
				SQL = "ALTER TABLE apchecks"
					+ " DROP KEY batchentrypagekey" 
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 964:
				//Added by BJZ 11/10/2017
				SQL = "CREATE TABLE userscustomelinks("
						+ "lid int(11) NOT NULL auto_increment COMMENT '[010401] PK auto_incrementing ID'"
						+ ", susername varchar(128) NOT NULL DEFAULT '' COMMENT '[010101] M:1'"
						+ ", surl varchar(1024) NOT NULL DEFAULT ''"
						+ ", surlname varchar(128) NOT NULL DEFAULT ''"
						+ ", isequence int(11) NOT NULL DEFAULT '0'"
						+ ", PRIMARY KEY (`lid`)"
						+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 965:
				//Added by BJZ 11/10/2017
				SQL = 	"ALTER TABLE `users` CHANGE `sUserName` `sUserName` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101] PK'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `users` CHANGE `sDefaultSalespersonCode` `sDefaultSalespersonCode` varchar(10) DEFAULT '' COMMENT '[010202] M:M'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `salesperson` CHANGE `sSalespersonCode` `sSalespersonCode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202] CPK'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `salesperson` CHANGE `sSalespersonUserName` `sSalespersonUserName` varchar(75) NOT NULL DEFAULT '' COMMENT '[010201] CPK'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 966:
				//Added by TJR 11/10/2017
				SQL = "RENAME TABLE userscustomelinks TO userscustomlinks";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 967:
				//Added by BJZ 11/11/2017
				SQL = 	"ALTER TABLE users COMMENT = '[0101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE salesperson COMMENT = '[0102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE mechanics COMMENT = '[0103]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE userscustomlinks COMMENT = '[0104]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `mechanics` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010301] PK'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `mechanics` CHANGE `sMechInitial` `sMechInitial` varchar(4) DEFAULT NULL COMMENT '[010302] UK 1:M'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `users` CHANGE `smechanicinitials` `smechanicinitials` varchar(4) NOT NULL DEFAULT '' COMMENT '[010302] M:1'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 968:
				//Added by TJR 11/15/2017
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD COLUMN iprintingfinalized INT(11) NOT NULL DEFAULT '0'" 
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 969:
				//Added by TJR 11/15/2017
				SQL = "UPDATE apbatchentries"
					+ " LEFT JOIN apbatches"
					+ " ON apbatches.lbatchnumber=apbatchentries.lbatchnumber"
					+ " SET iprintingfinalized  = 1"
					+  " WHERE ("
						+ "(apbatches.ibatchtype=1)"
						+ " AND (apbatchentries.iprintcheck=1)"
						+ " AND (apbatches.ichecksprinted = 1)"
					+ ")"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 970:
				//Added by BJZ 11/12/2017
				SQL = 	"ALTER TABLE `users` DROP PRIMARY KEY";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `users` ADD `lid` INT(11) PRIMARY KEY AUTO_INCREMENT";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE users ADD UNIQUE (sUserName)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `users` CHANGE `sUserName` `sUserName` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101] UNI'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = 	"ALTER TABLE `users` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010102] PK'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 971:
				//Added by TJR 11/20/2017
				SQL = 	"ALTER TABLE apchecklines CHANGE COLUMN bddeductions bddiscounttaken decimal(17,2) NOT NULL default '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 972:
				//Added by TJR 11/21/2017
				SQL = 	"ALTER TABLE apbatchentrylines ADD COLUMN bdpayableamount decimal(17,2) NOT NULL default '0.00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 973:
				//Added by TJR 11/21/2017
				// This will basically only be used in one case, where there are already apbatchentrylines.
				//Normally this will run before there are ever any actual lines to update anyway....
				SQL = 	"UPDATE apbatchentrylines SET bdpayableamount = (bdamount + bdapplieddiscountamt) * -1"
					+ " WHERE ("
						+ "(bdamount < 0.00)"  //Only need to do this for payments....
					+ ")"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 974:
				//Added by BJZ 12/7/2017
				//Adjusting comments. 
				SQL = "ALTER TABLE `userscustomlinks` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `users` CHANGE `sUserName` `sUserName` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `users` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `salesperson` CHANGE `sSalespersonUserName` `sSalespersonUserName` varchar(75) NOT NULL DEFAULT '' COMMENT '[010201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `salesperson` CHANGE `sSalespersonCode` `sSalespersonCode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `mechanics` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = "ALTER TABLE `mechanics` CHANGE `sMechInitial` `sMechInitial` varchar(4) DEFAULT NULL COMMENT '[010302] 1:M'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 975:
				//Added by BJZ 12/13/2017
				SQL = 	"ALTER TABLE smoptions ADD COLUMN sgoogleapikey varchar(100) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 976:
				//Added by TJR 12/13/2017
				SQL = 	"ALTER TABLE apbatches DROP COLUMN ichecksprinted";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 977:
				//Added by TJR 12/18/2017
				SQL = "alter table ap1099cprscodes CHANGE lid lid int(11) NOT NULL auto_increment COMMENT '[060101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 978:
				//Added by TJR 12/18/2017
				SQL = "alter table apaccountsets CHANGE lid lid int(11) NOT NULL auto_increment COMMENT '[060201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 979:
				//Added by TJR 12/18/2017
				SQL = "alter table apaccountsets CHANGE sacctsetname sacctsetname varchar(32) NOT NULL DEFAULT '' COMMENT '[060202]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 980:
				//Added by TJR 12/18/2017
				SQL = "alter table apbatchentries CHANGE lid lid int(11) NOT NULL auto_increment COMMENT '[060301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 981:
				//Added by TJR 12/18/2017
				SQL = "alter table apbatchentries CHANGE lbatchnumber lbatchnumber  int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 982:
				//Added by TJR 12/18/2017
				SQL = "alter table apbatchentries CHANGE lentrynumber lentrynumber  int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 983:
				//Added by TJR 12/18/2017
				SQL = "alter table apbatchentries CHANGE scontrolacct scontrolacct varchar(75) NOT NULL DEFAULT '' COMMENT '[090101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 984:
				//Added by TJR 12/18/2017
				SQL = "alter table apbatchentries CHANGE svendoracct svendoracct  varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 985:
				//Added by TJR 12/18/2017
				SQL = "alter table icvendors CHANGE svendoracct svendoracct varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 986:
				//Added by TJR 12/18/2017
				SQL = "alter table icvendors CHANGE iapaccountset iapaccountset int(11) NOT NULL DEFAULT '0' COMMENT '[060201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 987:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apbatchentrylines` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[060501]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 988:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apbatchentrylines` CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 989:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apbatchentrylines` CHANGE `lentrynumber` `lentrynumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 990:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apbatchentrylines` CHANGE `llinenumber` `llinenumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060504]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 991:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE  apbatches CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL AUTO_INCREMENT COMMENT '[060601]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 992:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apcheckforms` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[060701]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 993:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apchecklines` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[060801]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 994:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apchecklines` CHANGE `lcheckid` `lcheckid` int(11) NOT NULL DEFAULT '0' COMMENT '[060901]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 995:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apchecklines` CHANGE `lchecklinenumber` `lchecklinenumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060802]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 996:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apchecks` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[060901]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 997:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apdistributioncodes` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[061001]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 998:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apdistributioncodes` CHANGE `sdistcodename` `sdistcodename` varchar(32) NOT NULL DEFAULT '' COMMENT '[061002]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 999:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apmatchinglines` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[061101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1000:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `aptransactionlines` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[061201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1001:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `aptransactions` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[061301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1002:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorgroups` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[061401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1003:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorgroups` CHANGE `sgroupid` `sgroupid` varchar(12) NOT NULL DEFAULT '' COMMENT '[061402]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1004:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorremittolocations` CHANGE `svendoracct` `svendoracct` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1005:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorremittolocations` CHANGE `sremittocode` `sremittocode` varchar(12) NOT NULL DEFAULT '' COMMENT '[061501]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1006:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorstatistics` CHANGE `svendoracct` `svendoracct` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1007:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorstatistics` CHANGE `lyear` `lyear` int(11) NOT NULL DEFAULT '0' COMMENT '[061601]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1008:
				//Added by TJR 12/18/2017
				SQL = "ALTER TABLE `apvendorstatistics` CHANGE `lmonth` `lmonth` int(11) NOT NULL DEFAULT '0' COMMENT '[061602]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1009:
				//Added by TJR 12/18/2017
				SQL = "alter table glaccounts CHANGE sacctid sacctid varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1010:
				//Added by TJR 12/18/2017
				SQL = "alter table glaccounts CHANGE `icostcenterid` `icostcenterid` int(11) NOT NULL DEFAULT '0' COMMENT '[090201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1011:
				//Added by TJR 12/18/2017
				SQL = "alter table costcenters CHANGE lid lid int(11) NOT NULL auto_increment COMMENT '[090201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1012:
				//Added by TJR 12/18/2017
				SQL = "alter table costcenters CHANGE scostcentername scostcentername varchar(32) NOT NULL DEFAULT '' COMMENT '[090202]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1013:
				//Added by BJZ 12/19/2017
				SQL = 	"ALTER TABLE `appointmentusergroups` CHANGE `susername` `susername` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1014:
				//Added by BJZ 12/19/2017
				SQL = 	"ALTER TABLE appointmentusergroups ADD COLUMN luserid int(11) NOT NULL default '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1015:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apaccountsets` CHANGE `spayablescontrolacct` `spayablescontrolacct` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ",CHANGE `spurchasediscountacct` `spurchasediscountacct` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ",CHANGE `sprepaymentacct` `sprepaymentacct` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1016:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `icvendorterms` CHANGE `stermscode` `stermscode` varchar(6) NOT NULL DEFAULT '' COMMENT '[061701]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1017:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `tax` CHANGE `staxjurisdiction` `staxjurisdiction` varchar(12) NOT NULL DEFAULT '' COMMENT '[010404]'"
					+ ", CHANGE `itaxtype` `itaxtype` int(11) NOT NULL DEFAULT '0' COMMENT '[010403]'"
					+ ", CHANGE `staxtype` `staxtype` varchar(254) NOT NULL DEFAULT '' COMMENT '[010402]'"
					+ ", CHANGE `sglacct` `sglacct` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1018:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `bkbanks` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[080101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1019:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `orderheaders` CHANGE `dOrderUniqueifier` `dOrderUniqueifier` double NOT NULL DEFAULT '0' COMMENT '[030102]'"
					+ ", CHANGE `sOrderNumber` `sOrderNumber` varchar(22) DEFAULT NULL COMMENT '[030101]'"
					+ ", CHANGE `strimmedordernumber` `strimmedordernumber` varchar(22) NOT NULL DEFAULT '' COMMENT '[030103]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1020:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `icpoheaders` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1021:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `invoiceheaders` CHANGE `sInvoiceNumber` `sInvoiceNumber` varchar(15) NOT NULL DEFAULT '' COMMENT '[030201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1022:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `icporeceiptheaders` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1023:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `icporeceiptlines` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1024:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apchecks` CHANGE `schecknumber` `schecknumber` varchar(12) NOT NULL DEFAULT '' COMMENT '[060902]'"
					+ ", CHANGE `lbankid` `lbankid` int(11) NOT NULL DEFAULT '0' COMMENT '[080101]'"
					+ ", CHANGE `lcheckformid` `lcheckformid` int(11) NOT NULL DEFAULT '0' COMMENT '[060701]'"
					+ ", CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'"
					+ ", CHANGE `lentrynumber` `lentrynumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'"
					+ ", CHANGE `ltransactionid` `ltransactionid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'"
					+ ", CHANGE `screatedby` `screatedby` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'"
					+ ", CHANGE `svendoracct` `svendoracct` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'"
					+ ", CHANGE `lbatchentryid` `lbatchentryid` int(11) NOT NULL DEFAULT '0' COMMENT '[060301]'"
					+ ", CHANGE `sprintedby` `sprintedby` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1025:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apbatchentries` CHANGE `sdocnumber` `sdocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'"
					+ ", CHANGE `scontrolacct` `scontrolacct` varchar(75) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `svendoracct` `svendoracct` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'"
					+ ", CHANGE `sterms` `sterms` varchar(12) NOT NULL DEFAULT '' COMMENT '[061701]'"
					+ ", CHANGE `itaxid` `itaxid` int(11) NOT NULL DEFAULT '0' COMMENT '[010401]'"
					+ ", CHANGE `schecknumber` `schecknumber` varchar(12) NOT NULL DEFAULT '' COMMENT '[060902]'"
					+ ", CHANGE `sremittocode` `sremittocode` varchar(12) NOT NULL DEFAULT '' COMMENT '[061501]'"
					+ ", CHANGE `lbankid` `lbankid` int(11) NOT NULL DEFAULT '0' COMMENT '[080101]'"
					+ ", CHANGE `lsalesordernumber` `lsalesordernumber` int(11) NOT NULL DEFAULT '0' COMMENT '[030103]'"
					+ ", CHANGE `lpurchaseordernumber` `lpurchaseordernumber` int(11) NOT NULL DEFAULT '0' COMMENT '[050101]'"
					+ ", CHANGE `sapplytoinvoicenumber` `sapplytoinvoicenumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[030201]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1026:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `aptransactions` CHANGE `loriginalbatchnumber` `loriginalbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'"
					+ ", CHANGE `loriginalentrynumber` `loriginalentrynumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'"
					+ ", CHANGE `svendor` `svendor` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'"
					+ ", CHANGE `sdocnumber` `sdocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'"
					+ ", CHANGE `idoctype` `idoctype` int(11) NOT NULL DEFAULT '0' COMMENT '[061302]'"
					+ ", CHANGE `scontrolacct` `scontrolacct` varchar(75) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `lapplytopurchaseorderid` `lapplytopurchaseorderid` int(11) NOT NULL DEFAULT '0' COMMENT '[050101]'"
					+ ", CHANGE `lapplytosalesorderid` `lapplytosalesorderid` int(11) NOT NULL DEFAULT '0' COMMENT '[030103]'"
					+ ", CHANGE `sapplytoinvoicenumber` `sapplytoinvoicenumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[030201]'"
					+ ", CHANGE `schecknumber` `schecknumber` varchar(12) NOT NULL DEFAULT '' COMMENT '[060902]'"
					+ ", CHANGE `lbatchentryid` `lbatchentryid` int(11) NOT NULL DEFAULT '0' COMMENT '[060301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1027:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apbatchentrylines` CHANGE `sdistributioncodename` `sdistributioncodename` varchar(32) NOT NULL DEFAULT '' COMMENT '[061002]'"
					+ ", CHANGE `sdistributionacct` `sdistributionacct` varchar(75) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `lpoheaderid` `lpoheaderid` int(11) NOT NULL DEFAULT '0' COMMENT '[050101]'"
					+ ", CHANGE `lreceiptheaderid` `lreceiptheaderid` int(11) NOT NULL DEFAULT '0' COMMENT '[050201]'"
					+ ", CHANGE `lapplytodocid` `lapplytodocid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'"
					+ ", CHANGE `sapplytodocnumber` `sapplytodocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'"
					+ ", CHANGE `iapplytodoctype` `iapplytodoctype` int(11) NOT NULL DEFAULT '0' COMMENT '[061302]'"
					+ ", CHANGE `lporeceiptlineid` `lporeceiptlineid` int(11) NOT NULL DEFAULT '0' COMMENT '[050301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1028:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apbatches` CHANGE `screatedby` `screatedby` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'"
					+ ", CHANGE `slasteditedby` `slasteditedby` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1029:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apchecklines` CHANGE `lcheckid` `lcheckid` int(11) NOT NULL DEFAULT '0' COMMENT '[060901]'"
					+ ", CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'"
					+ ", CHANGE `lentrynumber` `lentrynumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'"
					+ ", CHANGE `lentrylinenumber` `lentrylinenumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060504]'"
					+ ", CHANGE `sapplytdocnumber` `sapplytdocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1030:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apmatchinglines` CHANGE `svendor` `svendor` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'"
					+ ", CHANGE `sappliedfromdocnumber` `sappliedfromdocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'"
					+ ", CHANGE `sappliedtodocnumber` `sappliedtodocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'"
					+ ", CHANGE `ltransactionappliedfromid` `ltransactionappliedfromid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'"
					+ ", CHANGE `ltransactionappliedtoid` `ltransactionappliedtoid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1031:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apoptions` CHANGE `suser` `suser` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1032:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `aptransactionlines` CHANGE `ltransactionheaderid` `ltransactionheaderid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'"
					+ ", CHANGE `loriginalbatchnumber` `loriginalbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060601]'"
					+ ", CHANGE `loriginalentrynumber` `loriginalentrynumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060302]'"
					+ ", CHANGE `loriginallinenumber` `loriginallinenumber` int(11) NOT NULL DEFAULT '0' COMMENT '[060504]'"
					+ ", CHANGE `sdistributioncodename` `sdistributioncodename` varchar(32) NOT NULL DEFAULT '' COMMENT '[061002]'"
					+ ", CHANGE `sdistributionacct` `sdistributionacct` varchar(75) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `lpoheaderid` `lpoheaderid` int(11) NOT NULL DEFAULT '0' COMMENT '[050101]'"
					+ ", CHANGE `lreceiptheaderid` `lreceiptheaderid` int(11) NOT NULL DEFAULT '0' COMMENT '[050201]'"
					+ ", CHANGE `lporeceiptlineid` `lporeceiptlineid` int(11) NOT NULL DEFAULT '0' COMMENT '[050301]'"
					+ ", CHANGE `lapplytodocid` `lapplytodocid` int(11) NOT NULL DEFAULT '0' COMMENT '[061301]'"
					+ ", CHANGE `sapplytodocnumber` `sapplytodocnumber` varchar(75) NOT NULL DEFAULT '' COMMENT '[060304]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1033:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apvendorgroups` CHANGE `iapaccountset` `iapaccountset` int(11) NOT NULL DEFAULT '0' COMMENT '[060201]'"
					+ ", CHANGE `ibankcode` `ibankcode` int(11) NOT NULL DEFAULT '0' COMMENT '[080101]'"
					+ ", CHANGE `stermscode` `stermscode` varchar(6) NOT NULL DEFAULT '' COMMENT '[061701]'"
					+ ", CHANGE `idistributioncodeusedfordistribution` `idistributioncodeusedfordistribution` int(11) NOT NULL DEFAULT '0' COMMENT '[061001]'"
					+ ", CHANGE `sglacctusedfordistribution` `sglacctusedfordistribution` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", CHANGE `staxjurisdiction` `staxjurisdiction` varchar(12) NOT NULL DEFAULT '' COMMENT '[010404]'"
					+ ", CHANGE `itaxtype` `itaxtype` int(11) NOT NULL DEFAULT '0' COMMENT '[010403]'"
					+ ", CHANGE `i1099CPRScode` `i1099CPRScode` int(11) NOT NULL DEFAULT '0' COMMENT '[060101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1034:
				//Added by TJR 12/20/2017
				SQL = "ALTER TABLE `apvendorremittolocations` CHANGE `svendoracct` `svendoracct` varchar(12) NOT NULL DEFAULT '' COMMENT '[060401]'"
					+ ", CHANGE `slasteditedby` `slasteditedby` varchar(128) NOT NULL DEFAULT '' COMMENT '[010101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1035:
				//Added by BJZ 12/21/2017
				SQL = "UPDATE appointmentusergroups"
				+ " LEFT JOIN users ON users.sUserName = appointmentusergroups.susername"
				+ " SET appointmentusergroups.luserid = users.lid";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1036:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE appointmentusergroups DROP PRIMARY KEY";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1037:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE appointmentusergroups ADD PRIMARY KEY (luserid, sappointmentgroupname)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1038:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE appointmentusergroups DROP COLUMN susername";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1039:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE `appointmentgroups` CHANGE `igroupid` `igroupid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010501]'"
				+ ", CHANGE `sappointmentgroupname` `sappointmentgroupname` varchar(50) NOT NULL DEFAULT '' COMMENT '[010502]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1040:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE `appointmentusergroups` CHANGE `luserid` `luserid` int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `sappointmentgroupname` `sappointmentgroupname` varchar(50) NOT NULL DEFAULT '' COMMENT '[010502]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1041:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE appointments ADD COLUMN luserid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 1042:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE appointments ADD COLUMN lcreateduserid INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1043:
				//Added by BJZ 12/26/2017
				SQL = "UPDATE appointments"
				+ " LEFT JOIN users ON users.sUserName = appointments.suser"
				+ " SET appointments.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1044:
				//Added by BJZ 12/26/2017
				SQL = "UPDATE appointments"
				+ " LEFT JOIN users ON users.sUserName = appointments.screateduser"
				+ " SET appointments.lcreateduserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1045:
				//Added by BJZ 12/21/2017
				SQL = "ALTER TABLE appointments DROP COLUMN screateduser, DROP COLUMN suser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
		
			//BEGIN CASE:
			case 1046:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE `appointments` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010601]'"
					+ ", CHANGE `luserid` `luserid` int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `lcreateduserid` `lcreateduserid` int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `sordernumber` `sordernumber` varchar(22) NOT NULL DEFAULT '' COMMENT '[030103]'"
					+ ", CHANGE `isalescontactid` `isalescontactid` int(11) NOT NULL DEFAULT '0' COMMENT '[040201]'"
					+ ", CHANGE `ibidid` `ibidid` int(11) NOT NULL DEFAULT '0' COMMENT '[040101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1047:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE `bids` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[040101]'"
						+ ", CHANGE `isalescontactid` `isalescontactid` int(11) NOT NULL DEFAULT '0' COMMENT '[040201]'"
						+ ", CHANGE `ssalespersoncode` `ssalespersoncode` varchar(9) NOT NULL DEFAULT '' COMMENT '[010202]'"
						+ ", CHANGE `sCreatedBy` `sCreatedBy` varchar(128) DEFAULT '' COMMENT '[010101]'"
						+ ", CHANGE `screatedfromordernumber` `screatedfromordernumber` varchar(22) NOT NULL DEFAULT '' COMMENT '[030103]'"
						+ ", CHANGE `spricingpersoncode` `spricingpersoncode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202]'"
						+ ", CHANGE `stakeoffpersoncode` `stakeoffpersoncode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1048:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE `salescontacts` CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[040201]'"
					+ ", CHANGE `salespersoncode` `salespersoncode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202]'"
					+ ", CHANGE `scustomernumber` `scustomernumber` varchar(12) NOT NULL DEFAULT '' COMMENT '[020101]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1049:
				//Added by BJZ 12/26/2017
				SQL = "ALTER TABLE `arcustomers` CHANGE `sCustomerNumber` `sCustomerNumber` varchar(12) NOT NULL DEFAULT '' COMMENT '[020101]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 1050:
				//Added by BJZ 1/3/2018
				SQL = "ALTER TABLE invoicemgrcomments ADD COLUMN luserid INT(11) NOT NULL DEFAULT '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1051:
				//Added by BJZ 1/3/2018
				SQL = "UPDATE invoicemgrcomments"
				+ " LEFT JOIN users ON users.sUserName = invoicemgrcomments.suser"
				+ " SET invoicemgrcomments.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1052:
				//Added by BJZ 1/3/2018
				SQL = "ALTER TABLE `invoicemgrcomments` CHANGE `susername` `suserfullname` varchar(100) NOT NULL DEFAULT ''"
						+ ", CHANGE `sinvoicenumber` `sinvoicenumber` varchar(15) NOT NULL DEFAULT '' COMMENT '[030201]'"
						+ ", CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[030301]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1053:
				//Added by BJZ 1/3/2018
				SQL = "ALTER TABLE `invoicemgrcomments` DROP COLUMN `suser` "; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			case 1054:
				//Added by BJZ 1/4/2018
				SQL = "ALTER TABLE ordermgrcomments ADD COLUMN luserid INT(11) NOT NULL DEFAULT '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1055:
				//Added by BJZ 1/4/2018
				SQL = "UPDATE ordermgrcomments"
				+ " LEFT JOIN users ON users.sUserName = ordermgrcomments.suser"
				+ " SET ordermgrcomments.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1056:
				//Added by BJZ 1/4/2018
				SQL = "ALTER TABLE `ordermgrcomments` CHANGE `susername` `suserfullname` varchar(100) NOT NULL DEFAULT ''"
						+ ", CHANGE `sinvoicenumber` `sinvoicenumber` varchar(15) NOT NULL DEFAULT '' COMMENT '[030201]'"
						+ ", CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[030401]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1057:
				//Added by BJZ 1/4/2018
				SQL = "ALTER TABLE `ordermgrcomments` DROP COLUMN `suser` "; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1058:
				//Added by BJZ 1/9/2018
				SQL = "ALTER TABLE `userscustomlinks` ADD COLUMN  luserid INT(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1059:
				//Added by BJZ 1/9/2018
				SQL = "UPDATE userscustomlinks"
				+ " LEFT JOIN users ON users.sUserName = userscustomlinks.susername"
				+ " SET userscustomlinks.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1060:
				//Added by BJZ 1/9/2018
				SQL = "ALTER TABLE `userscustomlinks` DROP COLUMN `susername` "; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1061:
				//Added by BJZ 1/9/2018
				SQL = "ALTER TABLE `userscustomlinks` CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[010701]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1062:
				//Added by BJZ 1/19/2018
				SQL = "ALTER TABLE `securityusergroups` ADD COLUMN  luserid INT(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1063:
				//Added by BJZ 1/19/2018
				SQL = "ALTER TABLE securityusergroups DROP PRIMARY KEY"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1064:
				//Added by BJZ 1/19/2018
				SQL = "UPDATE securityusergroups"
				+ " LEFT JOIN users ON users.sUserName = securityusergroups.sUserName"
				+ " SET securityusergroups.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1065:
				//Added by BJZ 1/21/2018
				SQL = "DELETE FROM securityusergroups WHERE luserid=0"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
				
			
			//BEGIN CASE:
			case 1066:
				//Added by BJZ 1/19/2018
				SQL = "ALTER TABLE securityusergroups ADD PRIMARY KEY(sSecurityGroupName, luserid)"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1067:
				//Added by BJZ 1/19/2018
				SQL = "ALTER TABLE `securityusergroups` DROP COLUMN `sUserName`"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1068:
				//Added by BJZ 1/30/2018
				SQL = "CREATE TABLE customlinks("
						+ "lid int(11) NOT NULL auto_increment COMMENT '[010701]'"
						+ ", surl varchar(1024) NOT NULL DEFAULT ''"
						+ ", surlname varchar(128) NOT NULL DEFAULT ''"
						+ ", PRIMARY KEY (`lid`)"
						+ " ) ENGINE=InnoDB"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1069:
				//Added by BJZ 1/30/2018
				SQL = "INSERT INTO customlinks (surl, surlname)"
						+ " SELECT DISTINCT surl, surlname"
						+ " FROM userscustomlinks"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1070:
				//Added by BJZ 1/30/2018
				SQL = "ALTER TABLE userscustomlinks DROP PRIMARY KEY, CHANGE lid lid int(11)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1071:
				//Added by BJZ 1/30/2018
				SQL = "UPDATE userscustomlinks"
						+ " LEFT JOIN customlinks ON customlinks.surl = userscustomlinks.surl AND customlinks.surlname = userscustomlinks.surlname"
						+ " SET userscustomlinks.lid = IF(customlinks.lid IS NULL, 0, customlinks.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1072:
				//Added by BJZ 1/30/2018
				SQL = "ALTER TABLE `userscustomlinks` DROP COLUMN `surl`, DROP COLUMN `surlname`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1073:
				//Added by BJZ 1/30/2018
				SQL = "ALTER TABLE `userscustomlinks` CHANGE `lid` `icustomlinkid` int(11) NOT NULL DEFAULT '0' COMMENT '[010701]'"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1074:
				//Added by BJZ 1/30/2018
				SQL = "ALTER TABLE `userscustomlinks` ADD PRIMARY KEY (`icustomlinkid`,`luserid`)"; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1075:
				//Added by TJR 2/16/2018
				SQL = clsDatabaseFunctions.updateFieldValueFromNullToEmptyString ("sSalesperson", "orderheaders"); 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				SQL = clsDatabaseFunctions.updateDBFieldDefaultValueToEmptyString ("sSalesperson", "orderheaders", 8); 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1076:
				//Added by BJZ 2/20/2018
				SQL = "CREATE TABLE doingbusinessasaddresses("
						+ "  lid int(11) NOT NULL auto_increment COMMENT '[010801]'"
						+ ", sname varchar(60) NOT NULL DEFAULT ''"
						+ ", sdescription varchar(100) NOT NULL DEFAULT ''"
						+ ", saddress1 varchar(30) NOT NULL DEFAULT ''"
						+ ", saddress2 varchar(30) NOT NULL DEFAULT ''"
						+ ", saddress3 varchar(30) NOT NULL DEFAULT ''"
						+ ", saddress4 varchar(30) NOT NULL DEFAULT ''"
						+ ", scity varchar(20) NOT NULL DEFAULT ''"
						+ ", sstate varchar(20) NOT NULL DEFAULT ''"
						+ ", szip varchar(15) NOT NULL DEFAULT ''"
						+ ", scountry varchar(20) NOT NULL DEFAULT ''"
						+ ", sphone varchar(20) NOT NULL DEFAULT ''"
						+ ", semail varchar(100) NOT NULL DEFAULT ''"
						+ ", swebsite varchar(100) NOT NULL DEFAULT ''"
						+ ", mcomments MEDIUMTEXT NOT NULL"
						+ ", PRIMARY KEY (`lid`)"
						+ " ) ENGINE=InnoDB"
					; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1077:
				//Added by TJR 2/22/2018
				SQL = "ALTER TABLE `aptransactionlines`"
					+ " ADD sunitofmeasure varchar(10) NOT NULL DEFAULT ''"
					+ ", ADD sitemnumber varchar(24) NOT NULL DEFAULT ''"
					+ ", ADD bdqtyreceived DECIMAL(17,4) NOT NULL DEFAULT '0.0000'"
				; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1078:
				//Added by BJZ 2/26/2018
				SQL = "ALTER TABLE doingbusinessasaddresses ADD COLUMN slocationcode VARCHAR(6) NOT NULL DEFAULT '' "
						+ "COMMENT '[010901] Used link to locations table during set up.'"
					; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				//Added by BJZ 2/26/2018
				SQL = "ALTER TABLE doingbusinessasaddresses ADD COLUMN ssecondphone VARCHAR(20) NOT NULL DEFAULT '' "
					; 
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1079:
				//Added by BJZ 2/26/2018
				SQL = "INSERT INTO doingbusinessasaddresses "
						+ "(slocationcode"
						+ ", sname "
						+ ", saddress1 "
						+ ", saddress2 "
						+ ", saddress3 "
						+ ", saddress4 "
						+ ", scity "
						+ ", sstate "
						+ ", szip "
						+ ", scountry "
						+ ", sphone "
						+ ", ssecondphone "
						+ ", swebsite "
						+ ", mcomments "
						+ " ) "
						+ " SELECT "
						+ " sLocation"
						+ ", sremittocompanydescription"
						+ ", sremittoaddress1"
						+ ", sremittoaddress2"
						+ ", sremittoaddress3"
						+ ", sremittoaddress4"
						+ ", sremittocity"
						+ ", sremittostate"
						+ ", sremittozip"
						+ ", sremittocountry"
						+ ", sremittophone"
						+ ", sTollFreeNumber"
						+ ", sWebSite"
						+ ", sadditionalnotes"					
						+ " FROM locations"
						+ " ORDER BY sLocation"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1080:
				//Added by BJZ 2/26/2018
				SQL = "ALTER TABLE `locations` CHANGE `sLocation` `sLocation` varchar(6) NOT NULL DEFAULT '' COMMENT '[010901]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1081:
				//Added by BJZ 2/27/2018
				SQL = "ALTER TABLE orderheaders ADD COLUMN idoingbusinessasaddressid int(11) NOT NULL DEFAULT '0' COMMENT '[010801]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1082:
				//Added by BJZ 2/27/2018
				SQL = "UPDATE orderheaders "
						+ "INNER JOIN doingbusinessasaddresses ON orderheaders.sLocation = doingbusinessasaddresses.slocationcode " 
						+ "SET orderheaders.idoingbusinessasaddressid = doingbusinessasaddresses.lid"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1083:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE `doingbusinessasaddresses`"
						+ " CHANGE `sname` `sname` varchar(60) NOT NULL DEFAULT '' COMMENT '[010802]'"
						+ ", CHANGE `sdescription` `sdescription` varchar(100) NOT NULL DEFAULT '' COMMENT '[010803]'"
						+ ", CHANGE `saddress1` `saddress1` varchar(30) NOT NULL DEFAULT '' COMMENT '[010804]'"
						+ ", CHANGE `saddress2` `saddress2` varchar(30) NOT NULL DEFAULT '' COMMENT '[010805]'"
						+ ", CHANGE `saddress3` `saddress3` varchar(30) NOT NULL DEFAULT '' COMMENT '[010806]'"
						+ ", CHANGE `saddress4` `saddress4` varchar(30) NOT NULL DEFAULT '' COMMENT '[010807]'"
						+ ", CHANGE `scity` `scity` varchar(20) NOT NULL DEFAULT '' COMMENT '[010808]'"
						+ ", CHANGE `sstate` `sstate` varchar(20) NOT NULL DEFAULT '' COMMENT '[010809]'"
						+ ", CHANGE `szip` `szip` varchar(15) NOT NULL DEFAULT '' COMMENT '[010810]'"
						+ ", CHANGE `scountry` `scountry` varchar(20) NOT NULL DEFAULT '' COMMENT '[010811]'"
						+ ", CHANGE `sphone` `sphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010812]'"
						+ ", CHANGE `ssecondphone` `ssecondphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010813]'"
						+ ", CHANGE `semail` `semail` varchar(100) NOT NULL DEFAULT '' COMMENT '[010814]'"
						+ ", CHANGE `swebsite` `swebsite` varchar(100) NOT NULL DEFAULT '' COMMENT '[010815]'"
						+ ", CHANGE `mcomments` `mcomments` mediumtext NOT NULL COMMENT '[010816]'"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1084:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE deliverytickets "
						+ "CHANGE `scompanyname` `sdbaname` varchar(60) NOT NULL DEFAULT '' COMMENT '[010802]'"
						+ ", CHANGE `scompanyaddressline1` `sdbaaddressline1` varchar(30) NOT NULL DEFAULT '' COMMENT '[010804]'"
						+ ", CHANGE `scompanyaddressline2` `sdbaaddressline2` varchar(30) NOT NULL DEFAULT '' COMMENT '[010805]'"
						+ ", CHANGE `scompanyaddressline3` `sdbaaddressline3` varchar(30) NOT NULL DEFAULT '' COMMENT '[010806]'"
						+ ", CHANGE `scompanyaddressline4` `sdbaaddressline4` varchar(30) NOT NULL DEFAULT '' COMMENT '[010807]'"
						+ ", CHANGE `scompanycity` `sdbacity` varchar(20) NOT NULL DEFAULT '' COMMENT '[010808]'"
						+ ", CHANGE `scompanystate` `sdbastate` varchar(20) NOT NULL DEFAULT '' COMMENT '[010809]'"
						+ ", CHANGE `scompanyzip` `sdbazip` varchar(15) NOT NULL DEFAULT '' COMMENT '[010810]'"
						+ ", CHANGE `scompanyphone` `sdbaphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010812]'"
						+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[011001]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1085:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE deliverytickets "
						+ "ADD COLUMN `sdbacountry` varchar(20) NOT NULL DEFAULT '' COMMENT '[010811]'"
						+ ", ADD COLUMN `sdbasecondphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010813]'"
						+ ", ADD COLUMN `sdbaemail` varchar(100) NOT NULL DEFAULT '' COMMENT '[010814]'"
						+ ", ADD COLUMN `sdbawebsite` varchar(100) NOT NULL DEFAULT '' COMMENT '[010815]'"
						+ ", ADD COLUMN `mdbacomments` mediumtext NOT NULL COMMENT '[010816]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1086:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE deliverytickets DROP COLUMN `sbranchoffice`"
						+ ", DROP COLUMN `sbranchphone`"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1087:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE `deliveryticketterms` CHANGE `sTermsCode` `sTermsCode` int(11) NOT NULL AUTO_INCREMENT COMMENT '[011101]'"
						+ ", CHANGE `mTerms` `mTerms` mediumtext NOT NULL COMMENT '[011102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1088:
				//Added by BJZ 3/1/2018
				SQL = "ALTER TABLE `deliverytickets` CHANGE `stermscode` `stermscode` varchar(6) NOT NULL DEFAULT '' COMMENT '[011101]'"
						+ ", CHANGE `mterms` `mterms` mediumtext NOT NULL COMMENT '[011102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1089:
				//Added by BJZ 3/2/2018
				SQL = "ALTER TABLE workorders "
						+ " ADD COLUMN `sdbaname` varchar(60) NOT NULL DEFAULT '' COMMENT '[010802]'"
						+ ", ADD COLUMN `sdbadescription` varchar(100) NOT NULL DEFAULT '' COMMENT '[010803]'"
						+ ", ADD COLUMN `sdbaaddress1` varchar(30) NOT NULL DEFAULT '' COMMENT '[010804]'"
						+ ", ADD COLUMN `sdbaaddress2` varchar(30) NOT NULL DEFAULT '' COMMENT '[010805]'"
						+ ", ADD COLUMN `sdbaaddress3` varchar(30) NOT NULL DEFAULT '' COMMENT '[010806]'"
						+ ", ADD COLUMN `sdbaaddress4` varchar(30) NOT NULL DEFAULT '' COMMENT '[010807]'"
						+ ", ADD COLUMN `sdbacity` varchar(20) NOT NULL DEFAULT '' COMMENT '[010808]'"
						+ ", ADD COLUMN `sdbastate` varchar(20) NOT NULL DEFAULT '' COMMENT '[010809]'"
						+ ", ADD COLUMN `sdbazip` varchar(15) NOT NULL DEFAULT '' COMMENT '[010810]'"
						+ ", ADD COLUMN `sdbacountry` varchar(20) NOT NULL DEFAULT '' COMMENT '[010811]'"
						+ ", ADD COLUMN `sdbaphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010812]'"
						+ ", ADD COLUMN `sdbasecondphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010813]'"
						+ ", ADD COLUMN `sdbaemail` varchar(100) NOT NULL DEFAULT '' COMMENT '[010814]'"
						+ ", ADD COLUMN `sdbawebsite` varchar(100) NOT NULL DEFAULT '' COMMENT '[010815]'"
						+ ", ADD COLUMN `mdbacomments` mediumtext NOT NULL COMMENT '[010816]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1090:
				//Added by BJZ 3/2/2018
				SQL = "UPDATE workorders "
						+ "LEFT JOIN orderheaders ON orderheaders.strimmedordernumber = workorders.strimmedordernumber "
						+ "INNER JOIN doingbusinessasaddresses ON orderheaders.sLocation = doingbusinessasaddresses.slocationcode " 
						+ "SET workorders.sdbaname = doingbusinessasaddresses.sname "
						+ ", workorders.sdbadescription = doingbusinessasaddresses.sdescription	"
						+ ", workorders.sdbaaddress1 = doingbusinessasaddresses.saddress1 "
						+ ", workorders.sdbaaddress2 = doingbusinessasaddresses.saddress2 "
						+ ", workorders.sdbaaddress3 = doingbusinessasaddresses.saddress3 "
						+ ", workorders.sdbaaddress4 = doingbusinessasaddresses.saddress4 "
						+ ", workorders.sdbacity = doingbusinessasaddresses.scity "
						+ ", workorders.sdbastate = doingbusinessasaddresses.sstate "
						+ ", workorders.sdbazip = doingbusinessasaddresses.szip "
						+ ", workorders.sdbacountry = doingbusinessasaddresses.scountry "
						+ ", workorders.sdbaphone = doingbusinessasaddresses.sphone "
						+ ", workorders.sdbasecondphone = doingbusinessasaddresses.ssecondphone "
						+ ", workorders.sdbaemail = doingbusinessasaddresses.semail "
						+ ", workorders.sdbawebsite = doingbusinessasaddresses.swebsite "
						+ ", workorders.mdbacomments = doingbusinessasaddresses.mcomments "
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1091:
				//Added by TJR 3/5/2018
				SQL = "ALTER TABLE glaccounts ADD COLUMN iallowaspoexpense int(11) NOT NULL DEFAULT '1'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1092:
				//Added by BJZ 3/6/2018
				SQL = "ALTER TABLE doingbusinessasaddresses"
						+ " ADD COLUMN ssecondofficename varchar (30) NOT NULL DEFAULT '' COMMENT '[010817]'"
						+ ", ADD COLUMN ssecondofficephone varchar (20) NOT NULL DEFAULT '' COMMENT '[010818]'"
						+ ", ADD COLUMN slogo varchar (255) NOT NULL DEFAULT '' COMMENT '[010819]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1093:
				//Added by BJZ 3/6/2018
				SQL = "UPDATE doingbusinessasaddresses "
						+ "INNER JOIN locations ON locations.sLocation = doingbusinessasaddresses.slocationcode " 
						+ "SET doingbusinessasaddresses.ssecondofficename = locations.sSecondOfficeName"
						+ ", doingbusinessasaddresses.ssecondofficephone = locations.sSecondOfficePhone"
						+ ", doingbusinessasaddresses.slogo = locations.sLogo"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1094:
				//Added by BJZ 3/6/2018
				SQL = "ALTER TABLE invoiceheaders "
						+ " ADD COLUMN `sdbaname` varchar(60) NOT NULL DEFAULT '' COMMENT '[010802]'"
						+ ", ADD COLUMN `sdbaaddress1` varchar(30) NOT NULL DEFAULT '' COMMENT '[010804]'"
						+ ", ADD COLUMN `sdbaaddress2` varchar(30) NOT NULL DEFAULT '' COMMENT '[010805]'"
						+ ", ADD COLUMN `sdbaaddress3` varchar(30) NOT NULL DEFAULT '' COMMENT '[010806]'"
						+ ", ADD COLUMN `sdbaaddress4` varchar(30) NOT NULL DEFAULT '' COMMENT '[010807]'"
						+ ", ADD COLUMN `sdbacity` varchar(20) NOT NULL DEFAULT '' COMMENT '[010808]'"
						+ ", ADD COLUMN `sdbastate` varchar(20) NOT NULL DEFAULT '' COMMENT '[010809]'"
						+ ", ADD COLUMN `sdbazip` varchar(15) NOT NULL DEFAULT '' COMMENT '[010810]'"
						+ ", ADD COLUMN `sdbacountry` varchar(20) NOT NULL DEFAULT '' COMMENT '[010811]'"
						+ ", ADD COLUMN `sdbaphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010812]'"
						+ ", ADD COLUMN `sdbasecondphone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010813]'"
						+ ", ADD COLUMN `sdbaemail` varchar(100) NOT NULL DEFAULT '' COMMENT '[010814]'"
						+ ", ADD COLUMN `sdbawebsite` varchar(100) NOT NULL DEFAULT '' COMMENT '[010815]'"
						+ ", ADD COLUMN `mdbacomments` mediumtext NOT NULL COMMENT '[010816]'"
						+ ", ADD COLUMN `sdbasecondofficename` varchar(30) NOT NULL DEFAULT '' COMMENT '[010817]'"
						+ ", ADD COLUMN `sdbasecondofficephone` varchar(20) NOT NULL DEFAULT '' COMMENT '[010818]'"
						+ ", ADD COLUMN `sdbalogo` varchar(255) NOT NULL DEFAULT '' COMMENT '[010819]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1095:
				//Added by BJZ 3/6/2018
				SQL = "UPDATE invoiceheaders "
						+ "INNER JOIN orderheaders ON orderheaders.strimmedordernumber = invoiceheaders.strimmedordernumber "
						+ "INNER JOIN doingbusinessasaddresses ON orderheaders.idoingbusinessasaddressid = doingbusinessasaddresses.lid " 
						+ "SET invoiceheaders.sdbaname = doingbusinessasaddresses.sname "
						+ ", invoiceheaders.sdbaaddress1 = doingbusinessasaddresses.saddress1 "
						+ ", invoiceheaders.sdbaaddress2 = doingbusinessasaddresses.saddress2 "
						+ ", invoiceheaders.sdbaaddress3 = doingbusinessasaddresses.saddress3 "
						+ ", invoiceheaders.sdbaaddress4 = doingbusinessasaddresses.saddress4 "
						+ ", invoiceheaders.sdbacity = doingbusinessasaddresses.scity "
						+ ", invoiceheaders.sdbastate = doingbusinessasaddresses.sstate "
						+ ", invoiceheaders.sdbazip = doingbusinessasaddresses.szip "
						+ ", invoiceheaders.sdbacountry = doingbusinessasaddresses.scountry "
						+ ", invoiceheaders.sdbaphone = doingbusinessasaddresses.sphone "
						+ ", invoiceheaders.sdbasecondphone = doingbusinessasaddresses.ssecondphone "
						+ ", invoiceheaders.sdbaemail = doingbusinessasaddresses.semail "
						+ ", invoiceheaders.sdbawebsite = doingbusinessasaddresses.swebsite "
						+ ", invoiceheaders.mdbacomments = doingbusinessasaddresses.mcomments "
						+ ", invoiceheaders.sdbasecondofficename = doingbusinessasaddresses.ssecondofficename "
						+ ", invoiceheaders.sdbasecondofficephone = doingbusinessasaddresses.ssecondofficephone "
						+ ", invoiceheaders.sdbalogo = doingbusinessasaddresses.slogo "
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1096:
				//Added by TJR 3/8/2018
				SQL = "ALTER TABLE aptransactions "
					+ " ADD UNIQUE KEY vendor_doc_key(svendor, sdocnumber)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1097:
				//Added by TJR 3/15/2018
				SQL = "ALTER TABLE smoptions "
					+ " ADD COLUMN ssmtpreplytoname varchar(72) NOT NULL DEFAULT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1098:
				//Added by TJR 3/15/2018
				SQL = "UPDATE smoptions "
					+ " SET ssmtpreplytoname = ssmtpusername"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1099:
				//Added by TJR 3/21/2018
				SQL = "ALTER TABLE aptransactions ADD COLUMN staxjurisdiction varchar(12) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN itaxid int(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN bdtaxrate decimal(7,4) NOT NULL DEFAULT '0.0000'"
					+ ", ADD COLUMN staxtype varchar(254) NOT NULL DEFAULT ''"
					+ ", ADD COLUMN icalculateonpurchaseorsale int(11) NOT NULL DEFAULT '0'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1100:
				//Added by TJR 3/21/2018
				SQL = "UPDATE aptransactions"
					+ " LEFT JOIN apbatchentries ON "
					+ " aptransactions.lbatchentryid = apbatchentries.lid"
					+ " SET aptransactions.staxjurisdiction =  apbatchentries.staxjurisdiction"
					+ ", aptransactions.itaxid =  apbatchentries.itaxid"
					+ ", aptransactions.bdtaxrate =  apbatchentries.bdtaxrate"
					+ ", aptransactions.staxtype =  apbatchentries.staxtype"
					+ ", aptransactions.icalculateonpurchaseorsale =  apbatchentries.icalculateonpurchaseorsale"
					+ " WHERE ("
						+ "(apbatchentries.lid IS NOT NULL)"
					+ ")"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1101:
				//Added by TJR 3/21/2018
				SQL = "ALTER TABLE aptransactions"
					+ " CHANGE `itaxid` `itaxid` INT(11) NOT NULL DEFAULT '0' COMMENT '[010401]'"
					+ ", CHANGE `staxjurisdiction` `staxjurisdiction` VARCHAR(12) NOT NULL DEFAULT '' COMMENT '[010404]'"
					+ ", CHANGE `staxtype` `staxtype` VARCHAR(254) NOT NULL DEFAULT '' COMMENT '[010402]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1102:
				//Added by TJR 3/21/2018
				SQL = "ALTER TABLE aptransactionlines"
					+ " ADD COLUMN  sitemdescription VARCHAR(75) NOT NULL DEFAULT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1103:
				//Added by TJR 3/21/2018
				SQL = "UPDATE aptransactionlines"
					+ " LEFT JOIN icporeceiptlines ON"
					+ " aptransactionlines.lporeceiptlineid = icporeceiptlines.lid"
					+ " SET aptransactionlines.sitemdescription = icporeceiptlines.sitemdescription"
					+ " WHERE ("
						+ "(aptransactionlines.lporeceiptlineid > 0)"
						+ " AND (icporeceiptlines.lid IS NOT NULL)"
					+ ")"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1104:
				//Added by TJR 3/23/2018
				SQL = "ALTER TABLE savedqueries"
					+ " CHANGE `scomment` `scomment` mediumtext NOT NULL"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1105:
				//Added by TJR 3/24/2018
				SQL = "ALTER TABLE aptransactionlines"
					+ " ADD KEY itemnumberkey(sitemnumber)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1106:
				//Added by TJR 3/24/2018
				SQL = "ALTER TABLE aptransactionlines"
					+ " ADD KEY lapplytodocidkey(lapplytodocid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1107:
				//Added by TJR 3/24/2018
				SQL = "ALTER TABLE aptransactionlines"
					+ " ADD KEY sdistributionacctkey(sdistributionacct)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1108:
				//Added by TJR 3/27/2018
				SQL = "ALTER TABLE apbatchentries"
					+ " ADD column iinvoiceincludestax INT(11) NOT NULL DEFAULT '0' COMMENT '[061303]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1109:
				//Added by TJR 3/27/2018
				SQL = "ALTER TABLE aptransactions"
					+ " ADD column iinvoiceincludestax INT(11) NOT NULL DEFAULT '0' COMMENT '[061303]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1110:
				//Added by TJR 3/27/2018
				SQL = "ALTER TABLE icpoinvoiceheaders"
					+ " ADD column iinvoiceincludestax INT(11) NOT NULL DEFAULT '0' COMMENT '[061303]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1111:
				//Added by TJR 4/3/2018
				SQL = "CREATE TABLE gloptions ("
					+ " ibatchpostinginprocess INT(11) NOT NULL DEFAULT '0'"
					+ ", suser VARCHAR(128) NOT NULL DEFAULT ''"
					+ ", sprocess VARCHAR(128) NOT NULL DEFAULT ''"
					+ ", datstartdate datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", iusessmcpgl INT(11) NOT NULL DEFAULT '0'"
					+ ", iaccpacversion int(11) NOT NULL DEFAULT '0'"
					+ ", saccpacdatabaseurl varchar(128) NOT NULL DEFAULT ''"
					+ ", saccpacdatabasename varchar(128) NOT NULL DEFAULT ''"
					+ ", saccpacdatabaseuser varchar(128) NOT NULL DEFAULT ''"
					+ ", saccpacdatabasuserpw varchar(128) NOT NULL DEFAULT ''"
					+ ", iaccpacdatabasetype int(11) NOT NULL DEFAULT '0'"
					+ ", icreatetestbatchesfromsubmodules int(11) NOT NULL DEFAULT '0'"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1112:
				//Added by TJR 4/3/2018
				SQL = "CREATE TABLE glbatches ("
					+ "`lbatchnumber` int(11) NOT NULL auto_increment COMMENT '[090401]'"
					+ ", `datbatchdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", `ibatchstatus` int(11) NOT NULL default '0'"
					+ ", `sbatchdescription` varchar(128) NOT NULL default ''"
					+ ", `ibatchtype` int(11) NOT NULL default '0'"
					+ ", `datlasteditdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", `lbatchlastentry` int(11) NOT NULL default '0'"
					+ ", `screatedby` varchar(128) NOT NULL default ''"
					+ ", `slasteditedby` varchar(128) NOT NULL default ''"
					+ ", `datpostdate` datetime NOT NULL default '0000-00-00 00:00:00'"
					+ ", PRIMARY KEY  (`lbatchnumber`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1113:
				//Added by TJR 4/3/2018
				SQL = "CREATE TABLE gltransactionlines ("
					+ "`lid` int(11) NOT NULL auto_increment COMMENT '[090301]'"
					+ ", `loriginalbatchnumber` int(11) NOT NULL default '0' COMMENT '[090401]'"
					+ ", `loriginalentrynumber` int(11) NOT NULL default '0'"
					+ ", `loriginallinenumber` int(11) NOT NULL default '0'"
					+ ", `lsourceledgertransactionlineid` int(11) NOT NULL default '0'"
					+ ", `sacctid` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", `ifiscalyear` int(11) NOT NULL default '0'"
					+ ", `ifiscalperiod` int(11) NOT NULL default '0'"
					+ ", `dattransactiondate` date NOT NULL default '0000-00-00'"
					+ ", `ssourceledger` VARCHAR(2) NOT NULL DEFAULT ''"
					+ ", `stransactiontype` VARCHAR(32) NOT NULL DEFAULT ''"
					+ ", `bdamount` DECIMAL(17,2) NOT NULL DEFAULT '0.00'"
					+ ", `sdescription` varchar(60) NOT NULL default ''"
					+ ", `sreference` varchar(60) NOT NULL default ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", KEY ACCTKEY(`sacctid`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1114:
				//Added by TJR 4/6/2018
				SQL = "CREATE TABLE glaccountsegments ("
					+ "`lid` int(11) NOT NULL auto_increment COMMENT '[090501]'"
					+ ", `sdescription` varchar(60) NOT NULL default ''"
					+ ", `iuseinclosing` int(11) NOT NULL default '0'"
					+ ", `ilength` int(11) NOT NULL default '0'"
					+ ", PRIMARY KEY  (`lid`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1115:
				//Added by TJR 4/6/2018
				SQL = "ALTER TABLE glaccountsegments ADD UNIQUE KEY descriptionkey(sdescription)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1116:
				//Added by TJR 4/9/2018
				SQL = "CREATE TABLE glacctsegmentvalues ("
					+ "`lid` int(11) NOT NULL auto_increment COMMENT '[090601]'"
					+ ", `lsegmentid` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `svalue` varchar(15) NOT NULL default ''"
					+ ", `sdescription` varchar(60) NOT NULL default ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", KEY segmentidkey (`lsegmentid`)"
					+ ", UNIQUE KEY idsegmentvalkey (`lid`, `lsegmentid`, `svalue`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1117:
				//Added by TJR 4/11/2018
				SQL = "ALTER TABLE glacctsegmentvalues"
					+ " DROP KEY idsegmentvalkey"
					+ ", ADD UNIQUE KEY segmentvalkey (`lsegmentid`, `svalue`)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1118:
				//Added by BJZ 4/17/2018
				SQL = "ALTER TABLE bids ADD COLUMN lsalesgroupid INT(11) NOT NULL DEFAULT '0' COMMENT '[040301]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1119:
				//Added by BJZ 4/17/2018
				SQL = "ALTER TABLE `salesgroups` CHANGE `iSalesGroupId` `iSalesGroupId` int(11) NOT NULL DEFAULT '0' COMMENT '[040301]'"
						+ ", CHANGE `sSalesGroupCode` `sSalesGroupCode` varchar(8) DEFAULT '' COMMENT '[040302]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1120:
				//Added by BJZ 4/17/2018
				SQL = "ALTER TABLE `defaultsalesgroupsalespersons` CHANGE `lsalesgroupid` `lsalesgroupid` int(11) NOT NULL DEFAULT '0' COMMENT '[040301]'"
						+ ", CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[040401]'"
						+ ", CHANGE `scustomercode` `scustomercode` varchar(12) NOT NULL DEFAULT '' COMMENT '[020101]'"
						+ ", CHANGE `ssalespersoncode` `ssalespersoncode` varchar(8) NOT NULL DEFAULT '' COMMENT '[010202]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1121:
				//Added by BJZ 4/17/2018
				SQL = "ALTER TABLE `invoiceheaders` CHANGE `sCustomerCode` `sCustomerCode` varchar(12) DEFAULT NULL COMMENT '[020101]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1122:
				//Added by BJZ 4/17/2018
				SQL = "ALTER TABLE `orderheaders` CHANGE `sCustomerCode` `sCustomerCode` varchar(12) DEFAULT NULL COMMENT '[020101]'"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1123:
				//Added by BJZ 4/18/2018
				SQL = "UPDATE bids" 
						+ " INNER JOIN orderheaders ON orderheaders.lbidid = bids.id"  
						+ " SET bids.lsalesgroupid = orderheaders.iSalesGroup"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1124:
				//Added by TJR 4/20/2018
				SQL = "SELECT iSalesGroupId FROM salesgroups";
				long iNumberOfSalesGroups = 0;
				long iSalesGroupID = 0L;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					iSalesGroupID = rs.getLong("iSalesGroupId");
					iNumberOfSalesGroups++;
				}
				rs.close();
			} catch (SQLException e) {
				System.out.println("Error [1524226868] reading sales groups - " + e.getMessage());
			}
				
				//If there's only one sales group (often the case), then it's safe to set ALL of the sales leads to use that
				// one sales group, if the sales group ID has not yet been set:
				if (iNumberOfSalesGroups == 1){
					SQL = "UPDATE bids" 
						+ " SET bids.lsalesgroupid = " + Long.toString(iSalesGroupID)
						+ " WHERE (bids.lsalesgroupid < 1)"
						;
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				}

				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1125:
				//Added by TJR 4/20/2018
				SQL = "CREATE TABLE glaccountstructures ("
					+ "`lid` int(11) NOT NULL auto_increment COMMENT '[090701]'"
					+ ", `sstructureid` varchar(6) NOT NULL default ''"
					+ ", `sdescription` varchar(60) NOT NULL default ''"
					+ ", `lsegmentid1` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength1` int(11) NOT NULL default '0'"
					+ ", `lsegmentid2` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength2` int(11) NOT NULL default '0'"
					+ ", `lsegmentid3` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength3` int(11) NOT NULL default '0'"
					+ ", `lsegmentid4` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength4` int(11) NOT NULL default '0'"
					+ ", `lsegmentid5` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength5` int(11) NOT NULL default '0'"
					+ ", `lsegmentid6` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength6` int(11) NOT NULL default '0'"
					+ ", `lsegmentid7` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength7` int(11) NOT NULL default '0'"
					+ ", `lsegmentid8` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength8` int(11) NOT NULL default '0'"
					+ ", `lsegmentid9` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength9` int(11) NOT NULL default '0'"
					+ ", `lsegmentid10` int(11) NOT NULL default '0' COMMENT '[090501]'"
					+ ", `llength10` int(11) NOT NULL default '0'"
					+ ", PRIMARY KEY  (`lid`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1126:
				//Added by TJR 4/24/2018
				SQL = "ALTER TABLE glaccountstructures ADD UNIQUE KEY structureidkey (`sstructureid`)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1127:
				//Added by BJZ 4/30/2018
				SQL = "ALTER TABLE apoptions ADD COLUMN luserid int(11) NOT NULL default '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1128:
				//Added by BJZ 4/30/2018
				SQL = "UPDATE apoptions"
				+ " LEFT JOIN users ON users.sUserName = apoptions.suser"
				+ " SET apoptions.luserid = IF(users.lid IS NULL, 0, users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1129:
				//Added by BJZ 4/30/2018
				SQL = "ALTER TABLE apoptions DROP COLUMN suser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1130:
				//Added by BJZ 4/30/2018
				SQL = "ALTER TABLE apbatches ADD COLUMN lcreatedby int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ", ADD COLUMN llasteditedby int(11) NOT NULL default '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1131:
				//Added by BJZ 4/30/2018
				SQL = "UPDATE apbatches"
				+ " LEFT JOIN users ON users.sUserName = apbatches.screatedby"
				+ " SET apbatches.lcreatedby = IF(users.lid IS NULL, 0, users.lid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1132:
				//Added by BJZ 4/30/2018
				SQL = "UPDATE apbatches"
				+ " LEFT JOIN users ON users.sUserName = apbatches.slasteditedby"
				+ " SET apbatches.llasteditedby = IF(users.lid IS NULL, 0, users.lid)"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1133:
				//Added by BJZ 4/30/2018
				SQL = "ALTER TABLE apbatches DROP COLUMN screatedby, DROP COLUMN slasteditedby";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1134:
				//Added by BJZ 5/7/2018
				SQL = "ALTER TABLE doingbusinessasaddresses"
						+ "  ADD COLUMN maddress mediumtext NOT NULL COMMENT '[010820]'"
						+ ", ADD COLUMN mremittoaddress mediumtext NOT NULL COMMENT '[010821]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1135:
				//Added by BJZ 5/7/2018				
				SQL = "UPDATE doingbusinessasaddresses SET maddress = "
				//This is using the format of the invoice display
						+ "CONCAT("
						+ " IF(saddress1 != '', CONCAT(saddress1, '\\n'), '')"
						+ ",IF(saddress2 != '', CONCAT(saddress2, '\\n'), '')"
						+ ",IF(saddress3 != '', CONCAT(saddress3, '\\n'), '')"
						+ ",IF(saddress4 != '', CONCAT(saddress4, '\\n'), '')"
						+ ",scity, ', ' , sstate, ' ', szip, '\\n'"
						+ ",'Phone: ', sphone, '\\n'"
						+ ",'Toll Free: ',ssecondphone, '\\n'"
						+ ",swebsite, '\\n'"
						+ ",'\\n'"
						+ ",IF(ssecondofficename != '', CONCAT('<B>', ssecondofficename, '</B>', '\\n', ssecondofficephone), '\\n')"
						+ ")";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1136:
				//Added by BJZ 5/7/2018				
			SQL = "ALTER TABLE deliverytickets"
					+ "  ADD COLUMN mdbaaddress mediumtext NOT NULL COMMENT '[010820]'"
					+ ", ADD COLUMN mdbaremittoaddress mediumtext NOT NULL COMMENT '[010821]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1137:
				//Added by BJZ 5/8/2018				
			SQL = "ALTER TABLE workorders"
					+ "  ADD COLUMN mdbaaddress mediumtext NOT NULL COMMENT '[010820]'"
					+ ", ADD COLUMN mdbaremittoaddress mediumtext NOT NULL COMMENT '[010821]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1138:
				//Added by BJZ 5/8/2018				
			SQL = "ALTER TABLE invoiceheaders"
					+ "  ADD COLUMN mdbaaddress mediumtext NOT NULL COMMENT '[010820]'"
					+ ", ADD COLUMN mdbaremittoaddress mediumtext NOT NULL COMMENT '[010821]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1139:
				//Added by BJZ 5/8/2018				
				SQL = "UPDATE deliverytickets SET mdbaaddress = "
				//This is using the current format of the delivery ticket
						+ "CONCAT("
						+ " IF(sdbaaddressline1 != '', CONCAT(sdbaaddressline1, '\\n'), '')"
						+ ",IF(sdbaaddressline2 != '', CONCAT(sdbaaddressline2, '\\n'), '')"
						+ ",IF(sdbaaddressline3 != '', CONCAT(sdbaaddressline3, '\\n'), '')"
						+ ",IF(sdbaaddressline4 != '', CONCAT(sdbaaddressline4, '\\n'), '')"
						+ ",IF(sdbacity != '', CONCAT('',sdbacity), '')"
						+ ",IF(sdbastate != '', CONCAT(', ', sdbastate), '')"
						+ ",IF(sdbazip != '', CONCAT(' ', sdbazip), '')"
						+ ")";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1140:
				//Added by BJZ 5/8/2018				
				SQL = "UPDATE workorders SET mdbaaddress = "
				//This is using the current format of the delivery ticket
						+ "CONCAT("
						+ " IF(sdbaaddress1 != '', CONCAT(sdbaaddress1, '\\n'), '')"
						+ ",IF(sdbaaddress2 != '', CONCAT(sdbaaddress2, '\\n'), '')"
						+ ",IF(sdbaaddress3 != '', CONCAT(sdbaaddress3, '\\n'), '')"
						+ ",IF(sdbaaddress4 != '', CONCAT(sdbaaddress4, '\\n'), '')"
						+ ",IF(sdbacity != '', CONCAT('',sdbacity), '')"
						+ ",IF(sdbastate != '', CONCAT(', ', sdbastate), '')"
						+ ",IF(sdbazip != '', CONCAT(' ', sdbazip), '')"
						+ ",IF(mdbacomments != '', CONCAT('\\n', mdbacomments), '')"
						+ ",IF(sdbawebsite != '', CONCAT('\\n<A HREF=\"', IF(sdbawebsite NOT LIKE '%http%://%', 'http://',''),sdbawebsite, '\">', sdbawebsite, '</A>'), '')"
						+ ")";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1141:
				//Added by BJZ 5/8/2018				
				SQL = "UPDATE invoiceheaders SET mdbaaddress = "
				//This is using the current format of the delivery ticket
						+ "CONCAT("
						+ " IF(sdbaaddress1 != '', CONCAT(sdbaaddress1, '\\n'), '')"
						+ ",IF(sdbaaddress2 != '', CONCAT(sdbaaddress2, '\\n'), '')"
						+ ",IF(sdbaaddress3 != '', CONCAT(sdbaaddress3, '\\n'), '')"
						+ ",IF(sdbaaddress4 != '', CONCAT(sdbaaddress4, '\\n'), '')"
						+ ",sdbacity, ', ' , sdbastate, ' ', sdbazip, '\\n'"
						+ ",'Phone: ', sdbaphone, '\\n'"
						+ ",'Toll Free: ',sdbasecondphone, '\\n'"
						+ ",IF(sdbawebsite != '', CONCAT('<A HREF=\"', IF(sdbawebsite NOT LIKE '%http%://%', 'http://',''),sdbawebsite, '\">', sdbawebsite, '</A>'), ''), '\\n'"
						+ ",'\\n'"
						+ ",IF(sdbasecondofficename != '', CONCAT('<B>', sdbasecondofficename, '</B>', '\\n', sdbasecondofficephone), '\\n')"
						+ ")";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			
			//BEGIN CASE:
			case 1142:
				//Added by BJZ 5/16/2018
				SQL = "ALTER TABLE apbatches ADD COLUMN slasteditedbyfullname varchar(124) NOT NULL default '' COMMENT '', "
						+ " ADD COLUMN screatedbyfullname varchar(124) NOT NULL default '' COMMENT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1143:
				//Added by BJZ 5/16/2018
				SQL = "UPDATE apbatches"
				+ " LEFT JOIN users ON users.lid = apbatches.lcreatedby"
				+ " SET apbatches.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1144:
				//Added by BJZ 5/16/2018
				SQL = "UPDATE apbatches"
				+ " LEFT JOIN users ON users.lid = apbatches.llasteditedby"
				+ " SET apbatches.slasteditedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1145:
				//Added by BJZ 5/18/2018
				SQL = "ALTER TABLE entrybatches ADD COLUMN lcreatedbyid int(11) NOT NULL default '0' COMMENT '[010102]' "
						+ ", ADD COLUMN screatedbyfullname varchar(128) NOT NULL default '' COMMENT ''"
						+ ", ADD COLUMN llasteditedbyid int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ", ADD COLUMN slasteditedbyfullname varchar(128) NOT NULL default '' COMMENT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1146:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE entrybatches"
				+ " LEFT JOIN users ON users.sUserName = entrybatches.screatedby"
				+ " SET entrybatches.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1147:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE entrybatches"
				+ " LEFT JOIN users ON users.sUserName = entrybatches.screatedby"
				+ " SET entrybatches.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
				+ ", entrybatches.lcreatedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1148:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE entrybatches"
				+ " LEFT JOIN users ON users.sUserName = entrybatches.slasteditedby"
				+ " SET entrybatches.slasteditedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
				+ ", entrybatches.llasteditedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1149:
				//Added by BJZ 5/18/2018
				SQL = "ALTER TABLE aroptions ADD COLUMN suserfullname varchar(128) NOT NULL default '' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1150:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE aroptions"
				+ " LEFT JOIN users ON users.sUserName = aroptions.suser"
				+ " SET aroptions.suserfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1151:
				//Added by BJZ 5/18/2018
				SQL = "ALTER TABLE archronlog ADD COLUMN luserid int(11) NOT NULL default '0' COMMENT '[010102]' "
					+ ", ADD COLUMN suserfullname varchar(128) NOT NULL default '' COMMENT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1152:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE archronlog"
				+ " LEFT JOIN users ON users.sUserName = archronlog.suser"
				+ " SET archronlog.suserfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
				+ ", archronlog.luserid = IF(users.lid IS NULL, '0', users.lid)";
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1153:
				//Added by BJZ 5/22/2018
				SQL = "UPDATE doingbusinessasaddresses"
				+ " SET sdescription = sname"
				+ ", maddress = CONCAT('<div><b style=\"font-size: medium;\">', doingbusinessasaddresses.sname, '</b></div>', REPLACE(doingbusinessasaddresses.maddress,'\\n' ,'<BR/>'))"
				
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1154:
				//Added by BJZ 5/22/2018
				SQL = "UPDATE invoiceheaders "
						+ "SET mdbaaddress = CONCAT('<div><b style=\"font-size: medium;\">', invoiceheaders.sdbaname, '</b></div><font size=\"2\">', REPLACE(invoiceheaders.mdbaaddress,'\\n' ,'<BR/>'), '</font>')"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1155:
				//Added by BJZ 5/22/2018
				SQL = "UPDATE workorders "
						+ "SET mdbaaddress = CONCAT('<div><b style=\"font-size: medium;\">', workorders.sdbaname, '</b></div><font size=\"2\">', REPLACE(workorders.mdbaaddress,'\\n' ,'<BR/>'), '</font>')"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1156:
				//Added by BJZ 5/22/2018
				SQL = "UPDATE deliverytickets "
						+ "SET mdbaaddress = CONCAT('<div><b style=\"font-size: medium;\">', deliverytickets.sdbaname, '</b></div><font size=\"2\">', REPLACE(deliverytickets.mdbaaddress,'\\n' ,'<BR/>'), '</font>')"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1157:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE invoiceheaders " 
						+ "ADD COLUMN sdbadescription VARCHAR(100) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1158:
				//Added by BJZ 5/18/2018
				SQL = "UPDATE invoiceheaders SET sdbadescription = sdbaname"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1159:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE doingbusinessasaddresses "  
						+ "DROP COLUMN slocationcode"
						+ ", DROP COLUMN saddress1" 
						+ ", DROP COLUMN saddress2" 
						+ ", DROP COLUMN saddress3" 
						+ ", DROP COLUMN saddress4" 
						+ ", DROP COLUMN scity" 
						+ ", DROP COLUMN sstate" 
						+ ", DROP COLUMN szip" 
						+ ", DROP COLUMN sphone" 
						+ ", DROP COLUMN ssecondphone" 
						+ ", DROP COLUMN swebsite"  
						+ ", DROP COLUMN ssecondofficename"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1160:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE deliverytickets " 
						+ "DROP COLUMN sdbaaddressline1" 
						+ ", DROP COLUMN sdbaaddressline2"  
						+ ", DROP COLUMN sdbaaddressline3"  
						+ ", DROP COLUMN sdbaaddressline4"  
						+ ", DROP COLUMN sdbacity"  
						+ ", DROP COLUMN sdbastate"  
						+ ", DROP COLUMN sdbazip"  
						+ ", DROP COLUMN sdbaphone" 
						+ ", DROP COLUMN sdbasecondphone" 
						+ ", DROP COLUMN sdbawebsite" 
						+ ", DROP COLUMN sdbaemail" 
						+ ", DROP COLUMN mdbacomments" 
						+ ", DROP COLUMN sdbaname"
						+ ", DROP COLUMN sdbacountry"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1161:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE workorders " 
						+ "DROP COLUMN sdbaaddress1" 
						+ ", DROP COLUMN sdbaaddress2"  
						+ ", DROP COLUMN sdbaaddress3"  
						+ ", DROP COLUMN sdbaaddress4"  
						+ ", DROP COLUMN sdbacity"  
						+ ", DROP COLUMN sdbastate"  
						+ ", DROP COLUMN sdbazip"  
						+ ", DROP COLUMN sdbaphone" 
						+ ", DROP COLUMN sdbasecondphone" 
						+ ", DROP COLUMN sdbawebsite" 
						+ ", DROP COLUMN sdbaemail" 
						+ ", DROP COLUMN mdbacomments" 
						+ ", DROP COLUMN sdbaname"
						+ ", DROP COLUMN sdbacountry"
						+ ", DROP COLUMN sdbadescription"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1162:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE invoiceheaders " 
						+ "DROP COLUMN sdbaaddress1" 
						+ ", DROP COLUMN sdbaaddress2"  
						+ ", DROP COLUMN sdbaaddress3"  
						+ ", DROP COLUMN sdbaaddress4"  
						+ ", DROP COLUMN sdbacity"  
						+ ", DROP COLUMN sdbastate"  
						+ ", DROP COLUMN sdbazip"  
						+ ", DROP COLUMN sdbaphone" 
						+ ", DROP COLUMN sdbasecondphone" 
						+ ", DROP COLUMN sdbawebsite" 
						+ ", DROP COLUMN sdbaemail" 
						+ ", DROP COLUMN sdbasecondofficename" 
						+ ", DROP COLUMN mdbacomments" 
						+ ", DROP COLUMN sdbaname"
						+ ", DROP COLUMN sdbacountry"
						+ ", DROP COLUMN sdbasecondofficephone"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1163:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE entrybatches " 
						+ "DROP COLUMN screatedby" 
						+ ", DROP COLUMN slasteditedby"  
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1164:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE aroptions "
						+ "DROP COLUMN suser"  
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1165:
				//Added by BJZ 5/31/2018
				SQL = "ALTER TABLE archronlog "
						+ "DROP COLUMN suser"  
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1166:
				//Added by TJR 5/31/2018
				SQL = "ALTER TABLE glaccounts ADD COLUMN iaddedbyACCPACconversion INT(11) NOT NULL DEFAULT '0'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1167:
				//Added by TJR 5/31/2018
				SQL = "ALTER TABLE glaccounts ADD COLUMN lstructureid INT(11) NOT NULL DEFAULT '0' COMMENT '[090701]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1168:
				//Added by TJR 6/5/2018
				SQL = "CREATE TABLE glfinancialstatementdata ("
					+ "`sacctid` VARCHAR(45) NOT NULL DEFAULT '' COMMENT '[090101]'"
					+ ", `ifiscalyear` INT(11) NOT NULL DEFAULT '0'"
					+ ", `bdopeningbalance` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod1` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod2` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod3` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod4` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod5` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod6` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod7` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod8` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod9` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod10` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod11` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod12` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod13` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod14` DECIMAL(17,2) NOT NULL default '0'"
					+ ", `bdnetchangeperiod15` DECIMAL(17,2) NOT NULL default '0'"
					+ ", PRIMARY KEY  (`sacctid`,`ifiscalyear`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1169:
				//Added by TJR 6/6/2018
				SQL = "CREATE TABLE glaccountgroups ("
					+ "`lid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '[090801]'"
					+ ", `sgroupcode` VARCHAR(12) NOT NULL DEFAULT ''"
					+ ", `sdescription` VARCHAR(60) NOT NULL DEFAULT ''"
					+ ", `ssortcode` VARCHAR(12) NOT NULL DEFAULT ''"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `groupcodekey` (sgroupcode)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1170:
				//Added by TJR 6/6/2018
				SQL = "ALTER TABLE glaccounts ADD COLUMN laccountgroupid INT(11) NOT NULL DEFAULT '0' COMMENT '[090801]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1171:
				//Added by TJR 6/7/2018
				SQL = "ALTER TABLE gloptions ADD COLUMN sclosingaccount VARCHAR(45) NOT NULL DEFAULT '' COMMENT '[090101]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1172:
				//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icinvoiceexportsequences ADD COLUMN luserid int(11) NOT NULL default '0' COMMENT '[010102]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE

			//BEGIN CASE:
			case 1173:
			//Added by BJZ 6/8/2018
			SQL = "UPDATE icinvoiceexportsequences"
				+ " LEFT JOIN users ON users.sUserName = icinvoiceexportsequences.suser"
			    + " SET icinvoiceexportsequences.luserid = IF(users.lid IS NULL, '0', users.lid)";
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
				
			//BEGIN CASE:
			case 1174:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icitems ADD COLUMN slastedituserfullname varchar(128) NOT NULL default '' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1175:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icitems"
						+ " LEFT JOIN users ON users.sUserName = icitems.slastedituser"
						+ " SET icitems.slastedituserfullname = CONCAT(IF(users.sUserFirstName IS NULL, icitems.slastedituser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1176:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icoptions ADD COLUMN suserfullname varchar(128) NOT NULL default '' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1177:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icoptions"
						+ " LEFT JOIN users ON users.sUserName = icoptions.suser"
						+ " SET icoptions.suserfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icoptions.suser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1178:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icphysicalinventories ADD COLUMN screatedbyfullname varchar(128) NOT NULL default '' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1179:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icphysicalinventories"
						+ " LEFT JOIN users ON users.sUserName = icphysicalinventories.screatedby"
						+ " SET icphysicalinventories.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icphysicalinventories.screatedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1180:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icporeceiptheaders ADD COLUMN screatedbyfullname varchar(128) NOT NULL default '' COMMENT ''"
						+ ",  ADD COLUMN slastupdateuserfullname varchar(128) NOT NULL default '' COMMENT ''"
						+ ",  ADD COLUMN lcreatedbyid int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ",  ADD COLUMN llastupdateuserid int(11) NOT NULL default '0' COMMENT '[010102]'"				
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1181:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icporeceiptheaders"
						+ " LEFT JOIN users ON CONCAT(users.sUserFirstName, ' ', users.sUserLastName) = icporeceiptheaders.screatedby"
						+ " SET icporeceiptheaders.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icporeceiptheaders.screatedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
						+ ", icporeceiptheaders.lcreatedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1182:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icporeceiptheaders"
						+ " LEFT JOIN users ON users.sUserName = icporeceiptheaders.slastupdateuser"
						+ " SET icporeceiptheaders.slastupdateuserfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icporeceiptheaders.slastupdateuser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
						+ ", icporeceiptheaders.llastupdateuserid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1183:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE ictransactions ADD COLUMN lpostedbyid int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ", ADD COLUMN spostedbyfullname varchar(128) NOT NULL default '' COMMENT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1184:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE ictransactions"
						+ " LEFT JOIN users ON users.sUserName = ictransactions.spostedby"
						+ " SET ictransactions.spostedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, ictransactions.spostedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
						+ ", ictransactions.lpostedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1185:
			//Added by BJZ 6/8/2018
				SQL = "ALTER TABLE icbatches ADD COLUMN lcreatedbyid int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ", ADD COLUMN screatedbyfullname varchar(128) NOT NULL default '' COMMENT ''"
						+ ", ADD COLUMN llasteditedbyid int(11) NOT NULL default '0' COMMENT '[010102]'"
						+ ", ADD COLUMN slasteditedbyfullname varchar(128) NOT NULL default '' COMMENT ''"					
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1186:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icbatches"
						+ " LEFT JOIN users ON users.sUserName = icbatches.screatedby"
						+ " SET icbatches.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icbatches.screatedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
						+ ", icbatches.lcreatedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1187:
			//Added by BJZ 6/8/2018
				SQL = "UPDATE icbatches"
						+ " LEFT JOIN users ON users.sUserName = icbatches.slasteditedby"
						+ " SET icbatches.slasteditedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icbatches.slasteditedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
						+ ", icbatches.llasteditedbyid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1188:
				//Added by TJR 6/12/2018
				SQL = "ALTER TABLE icinvoiceexportsequences ADD COLUMN suserfullname VARCHAR(128) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1189:
				//Added by EMM 6/13/2018
				SQL = "ALTER TABLE icoptions ADD COLUMN lpostingtimestamp BIGINT(64) NOT NULL default 0";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1190:
				//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icinvoiceexportsequences DROP COLUMN suser"
						+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050401]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1191:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icitems DROP COLUMN slastedituser"
						+ ", CHANGE `sitemnumber` `sitemnumber` varchar(24) NOT NULL DEFAULT '' COMMENT '[050701]'"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1192:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icoptions DROP COLUMN suser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1193:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icphysicalinventories DROP screatedby"
						+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050801]'"
						+ ", CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[050501]'"
						+ ", CHANGE `slocation` `slocation` varchar(6) NOT NULL DEFAULT '' COMMENT '[010901]'"
						+ ", CHANGE `sstartingitemnumber` `sstartingitemnumber` varchar(24) NOT NULL DEFAULT '' COMMENT '[050701]'"
						+ ", CHANGE `sendingitemnumber` `sendingitemnumber` varchar(24) NOT NULL DEFAULT '' COMMENT '[050701]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1194:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icporeceiptheaders DROP COLUMN screatedby"
						+ ", DROP COLUMN slastupdateuser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1195:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE ictransactions DROP COLUMN spostedby"
						+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050601]'"
						+ ", CHANGE `loriginalbatchnumber` `loriginalbatchnumber` int(11) NOT NULL DEFAULT '0' COMMENT '[050501]'"
						+ ", CHANGE `slocation` `slocation` varchar(6) NOT NULL DEFAULT '' COMMENT '[010901]'"
						+ ", CHANGE `sitemnumber` `sitemnumber` varchar(24) NOT NULL DEFAULT '' COMMENT '[050701]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1196:
			//Added by BJZ 6/15/2018
				SQL = "ALTER TABLE icbatches DROP COLUMN screatedby"
						+ ", DROP COLUMN slasteditedby"
						+ ", CHANGE `lbatchnumber` `lbatchnumber` int(11) NOT NULL AUTO_INCREMENT COMMENT '[050501]';"					
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1197:
			//Added by TJR 6/18/2018
				SQL = "RENAME table `glfinancialstatementdata` to `glfiscalsets`"					
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1198:
			//Added by BJZ 6/20/2018
			SQL = "ALTER TABLE icpoheaders CHANGE `sassignedtoname` `sassignedtofullname` varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN sdeletedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN screatedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1199:
			//Added by BJZ 6/21/2018
			SQL = "UPDATE icpoheaders"
					+ " LEFT JOIN users ON users.sUserName = icpoheaders.sdeletedby"
					+ " SET icpoheaders.sdeletedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icpoheaders.sdeletedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1200:
			//Added by BJZ 6/21/2018
			SQL = "UPDATE icpoheaders"
					+ " LEFT JOIN users ON users.sUserName = icpoheaders.screatedby"
					+ " SET icpoheaders.screatedbyfullname = TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icpoheaders.screatedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName)))"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1201:
			//Added by BJZ 6/21/2018
			SQL = "ALTER TABLE icpoheaders "
					+ " DROP COLUMN sdeletedby "
					+ ", DROP COLUMN screatedby "
					+ ", CHANGE `svendor` `svendor` varchar(24) NOT NULL DEFAULT '' COMMENT '[060401]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1202:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icaccountsets "
					+ " ADD COLUMN slastedituserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN llastedituserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1203:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icaccountsets"
					+ " LEFT JOIN users ON users.sUserName = icaccountsets.slastedituser"
					+ " SET icaccountsets.slastedituserfullname = CONCAT(IF(users.sUserFirstName IS NULL, icaccountsets.slastedituser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", icaccountsets.llastedituserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1204:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icaccountsets "
					+ " DROP COLUMN slastedituser "
					+ ", CHANGE `saccountsetcode` `saccountsetcode` varchar(6) NOT NULL DEFAULT '' COMMENT '[050901]'"
					+ ", CHANGE `icostingmethod` `icostingmethod` int(11) NOT NULL DEFAULT '0' COMMENT '[051001] 0=LIFO, 1=FIFO, 2=Average Cost'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1205:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icoptions "
					+ "CHANGE `lcostingmethod` `lcostingmethod` int(11) NOT NULL DEFAULT '0' COMMENT '[051001] 0=LIFO, 1=FIFO, 2=Average Cost'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1206:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icpoheaders ADD COLUMN lassignedtouserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN ldeletedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN lcreatedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1207:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icpoheaders"
					+ " LEFT JOIN users ON TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icpoheaders.sdeletedbyfullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))) = TRIM(sdeletedbyfullname)"
					+ " SET icpoheaders.ldeletedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1208:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icpoheaders"
					+ " LEFT JOIN users ON TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icpoheaders.screatedbyfullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))) = TRIM(screatedbyfullname)"
					+ " SET icpoheaders.lcreatedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1209:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icpoheaders"
					+ " LEFT JOIN users ON TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icpoheaders.sassignedtofullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))) = TRIM(sassignedtofullname)"
					+ " SET icpoheaders.lassignedtouserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1210:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icitems ADD COLUMN llastedituserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `scategorycode` `scategorycode` varchar(6) NOT NULL DEFAULT '' COMMENT '[051101]'"
					+ ", CHANGE `sdefaultpricelistcode` `sdefaultpricelistcode` varchar(6) NOT NULL DEFAULT '' COMMENT '[051201]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1211:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icitems"
					+ " LEFT JOIN users ON TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icitems.slastedituserfullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))) = TRIM(icitems.slastedituserfullname)"
					+ " SET icitems.llastedituserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1212:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE iccategories CHANGE `scategorycode` `scategorycode` varchar(6) NOT NULL DEFAULT '' COMMENT '[051101]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1213:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE pricelistcodes CHANGE `spricelistcode` `spricelistcode` varchar(6) NOT NULL DEFAULT '' COMMENT '[051201]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1214:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icphysicalinventories ADD COLUMN lcreatedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1215:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icphysicalinventories"
					+ " LEFT JOIN users ON TRIM(CONCAT(IF(users.sUserFirstName IS NULL, icphysicalinventories.screatedbyfullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))) = TRIM(icphysicalinventories.screatedbyfullname)"
					+ " SET icphysicalinventories.lcreatedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1216:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icporeceiptheaders CHANGE sdeletedby sdeletedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1217:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE iccategories ADD COLUMN llastedituserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN slastedituserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1218:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE iccategories"
					+ " LEFT JOIN users ON users.sUserName = iccategories.slastedituser"
					+ " SET iccategories.slastedituserfullname = CONCAT(IF(users.sUserFirstName IS NULL, iccategories.slastedituser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", iccategories.llastedituserid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1219:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE iccategories DROP COLUMN slastedituser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//BEGIN CASE:
			case 1220:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icitemprices ADD COLUMN llastedituserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN slastedituserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1221:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE icitemprices"
					+ " LEFT JOIN users ON users.sUserName = icitemprices.slastedituser"
					+ " SET icitemprices.slastedituserfullname = CONCAT(IF(users.sUserFirstName IS NULL, icitemprices.slastedituser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", icitemprices.llastedituserid = IF(users.lid IS NULL, '0', users.lid)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1222:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE icitemprices DROP COLUMN slastedituser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1223:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE reminders CHANGE slasteditedby slastediteduserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lcreatedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `lid` `lid` int(11) NOT NULL AUTO_INCREMENT COMMENT '[011201]'"
					+ ", CHANGE `sschedulecode` `sschedulecode` varchar(32) NOT NULL DEFAULT '' COMMENT '[011202]'"
					+ ", CHANGE `iremindermode` `iremindermode` int(11) NOT NULL DEFAULT '1' COMMENT '0=Personal, 1=General';"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1224:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE reminders"
					+ " LEFT JOIN users ON users.sUserName = reminders.slastediteduserfullname"
					+ " SET reminders.slastediteduserfullname = CONCAT(IF(users.sUserFirstName IS NULL, reminders.slastediteduserfullname, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", reminders.lcreatedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1225:
			//Added by BJZ 6/29/2018
				SQL = "ALTER TABLE reminderusers ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
						+ ", CHANGE `sschedulecode` `sschedulecode` varchar(32) NOT NULL DEFAULT '' COMMENT '[011202]'"
						+ ", CHANGE `datlastacknowledgedreminderdate` `datlastacknowledgedreminderdate` date NOT NULL DEFAULT '0000-00-00' COMMENT 'scheduled date of reminder that was last acknowledged'"
						+ ", CHANGE `datlastacknowledgeddate` `datlastacknowledgeddate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT 'date user last acknowleged this reminder'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1226:
			//Added by BJZ 6/29/2018
			SQL = "UPDATE reminderusers"
					+ " LEFT JOIN users ON users.sUserName = reminderusers.suser"
					+ " SET reminderusers.luserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1227:
			//Added by BJZ 6/29/2018
			SQL = "ALTER TABLE reminderusers drop primary key, add primary key(luserid, sschedulecode)"
					+ ", DROP COLUMN suser";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1228:
			//Added by TJR 7/5/2018
			SQL = "CREATE TABLE glfiscalperiods ("
				+ " `ifiscalyear` INT(11) NOT NULL DEFAULT '0' COMMENT '[091001]'"
				+ ", `ilasteditedbyuserid` INT(11) NOT NULL DEFAULT '0'"
				+ ", `slasteditedbyfullusername` VARCHAR(128) NOT NULL DEFAULT ''"
				+ ", `inumberofperiods` INT(11) NOT NULL DEFAULT '0'"
				+ ", `iactive` INT(11) NOT NULL DEFAULT '0'"
				+ ", `ilockadjustmentperiod` INT(11) NOT NULL DEFAULT '0'"
				+ ", `ilockclosingperiod` INT(11) NOT NULL DEFAULT '0'"
				+ ", `datbeginningdateperiod1` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod2` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod3` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod4` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod5` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod6` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod7` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod8` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod9` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod10` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod11` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod12` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datbeginningdateperiod13` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod1` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod2` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod3` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod4` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod5` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod6` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod7` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod8` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod9` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod10` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod11` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod12` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", `datendingdateperiod13` DATE NOT NULL DEFAULT '0000-00-00'"
				+ ", PRIMARY KEY  (`ifiscalyear`)"
				+ " ) ENGINE=InnoDB"
			;		
				
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 1229:
			//Added by TJR 7/5/2018
			SQL = "INSERT INTO gloptions ("
				+ "ibatchpostinginprocess"
				+ ", suser"
				+ ", sprocess"
				+ ", datstartdate"
				+ ", iusessmcpgl"
				+ ", iaccpacversion"
				+ ", icreatetestbatchesfromsubmodules"
				+ ", sclosingaccount"
				+ ") VALUES ("
				+ "0"
				+ ", ''"
				+ ", ''"
				+ ", '0000-00-00'"
				+ ", 0"
				+ ", 0"
				+ ", 0"
				+ ", ''"
				+ ")"
			;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 1230:
			//Added by TJR 7/10/2018
			SQL = "ALTER TABLE glfiscalperiods ADD COLUMN datlastediteddateandtime DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'"
			;
			if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1231:
			//Added by BJZ 7/25/2018
			SQL = "ALTER TABLE proposals ADD COLUMN lapprovedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN lsignedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1232:
			//Added by BJZ 7/25/2018
			SQL = "UPDATE proposals"
					+ " LEFT JOIN users ON users.sUserName = proposals.sapprovedbyuser"
					+ " SET proposals.lapprovedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1233:
			//Added by BJZ 7/25/2018
			SQL = "UPDATE proposals"
					+ " LEFT JOIN users ON users.sUserName = proposals.ssignedbyuser"
					+ " SET proposals.lsignedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1234:
			//Added by BJZ 7/25/2018
			SQL = "ALTER TABLE proposals DROP COLUMN sapprovedbyuser, DROP COLUMN ssignedbyuser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1235:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE arcustomers ADD COLUMN sLastEditUserFullName varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lLastEditUserID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1236:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE arcustomers"
					+ " LEFT JOIN users ON users.sUserName = arcustomers.sLastEditUser"
					+ " SET arcustomers.sLastEditUserFullName = CONCAT(IF(users.sUserFirstName IS NULL, arcustomers.sLastEditUser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", arcustomers.lLastEditUserID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			
			//BEGIN CASE:
			case 1237:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE arcustomers DROP COLUMN sLastEditUser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1238:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE arcustomergroups ADD COLUMN sLastEditUserFullName varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lLastEditUserID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", CHANGE `sGroupCode` `sGroupCode` varchar(6) NOT NULL DEFAULT '' COMMENT '[020201]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1239:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE arcustomergroups"
					+ " LEFT JOIN users ON users.sUserName = arcustomergroups.sLastEditUser"
					+ " SET arcustomergroups.sLastEditUserFullName = CONCAT(IF(users.sUserFirstName IS NULL, arcustomergroups.sLastEditUser, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", arcustomergroups.lLastEditUserID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE			
			
			//BEGIN CASE:
			case 1240:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE arcustomergroups DROP COLUMN sLastEditUser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1241:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE savedqueries ADD COLUMN suserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1242:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE savedqueries"
					+ " LEFT JOIN users ON users.sUserName = savedqueries.suser"
					+ " SET savedqueries.suserfullname = TRIM(CONCAT(savedqueries.sfirstname, ' ', savedqueries.slastname))"
					+ ", savedqueries.luserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE			
			
			//BEGIN CASE:
			case 1243:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE savedqueries DROP COLUMN sfirstname, DROP COLUMN slastname, DROP COLUMN suser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1244:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE smoptions ADD COLUMN LASTEDITUSERFULLNAME varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN LASTEDITUSERID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1245:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE smoptions"
					+ " LEFT JOIN users ON users.sUserName = smoptions.LASTEDITUSER"
					+ " SET smoptions.LASTEDITUSERFULLNAME = CONCAT(IF(users.sUserFirstName IS NULL, smoptions.LASTEDITUSER, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", smoptions.LASTEDITUSERID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE			
			
			//BEGIN CASE:
			case 1246:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE smoptions DROP COLUMN LASTEDITUSER";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1247:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE invoiceheaders ADD COLUMN sCreatedByFullName varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lCreatedByID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1248:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE invoiceheaders"
					+ " INNER JOIN users ON users.sUserName = invoiceheaders.sCreatedBy"
					+ " SET invoiceheaders.sCreatedByFullName = CONCAT(IF(users.sUserFirstName IS NULL, invoiceheaders.sCreatedBy, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", invoiceheaders.lCreatedByID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE			
			
			//BEGIN CASE:
			case 1249:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE invoiceheaders DROP COLUMN sCreatedBy";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1250:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE orderheaders ADD COLUMN LASTEDITUSERFULLNAME varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN LASTEDITUSERID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN sOrderCreatedByFullName varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lOrderCreatedByID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1251:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE orderheaders"
					+ " INNER JOIN users ON users.sUserName = orderheaders.LASTEDITUSER"
					+ " SET orderheaders.LASTEDITUSERFULLNAME = CONCAT(IF(users.sUserFirstName IS NULL, orderheaders.LASTEDITUSER, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", orderheaders.LASTEDITUSERID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE		
			
			//BEGIN CASE:
			case 1252:
			//Added by BJZ 7/26/2018
			SQL = "UPDATE orderheaders"
					+ " INNER JOIN users ON users.sUserName = orderheaders.sOrderCreatedBy"
					+ " SET orderheaders.sOrderCreatedByFullName = CONCAT(IF(users.sUserFirstName IS NULL, orderheaders.sOrderCreatedBy, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", orderheaders.lOrderCreatedByID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1253:
			//Added by BJZ 7/26/2018
			SQL = "ALTER TABLE orderheaders DROP COLUMN LASTEDITUSER, DROP COLUMN sOrderCreatedBy";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1254:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE workorders ADD COLUMN slasteditedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN llasteditedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN sscheduledbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lscheduledbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN sschedulechangedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lschedulechangedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			//BEGIN CASE:
			case 1255:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE workorders"
					+ " INNER JOIN users ON users.sUserName = workorders.slasteditedby"
					+ " SET workorders.slasteditedbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, workorders.slasteditedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", workorders.llasteditedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE		
			
			//BEGIN CASE:
			case 1256:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE workorders"
					+ " INNER JOIN users ON users.sUserName = workorders.sscheduledby"
					+ " SET workorders.sscheduledbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, workorders.sscheduledby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", workorders.lscheduledbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1257:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE workorders"
					+ " INNER JOIN users ON users.sUserName = workorders.sschedulechangedby"
					+ " SET workorders.sschedulechangedbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, workorders.sschedulechangedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", workorders.lschedulechangedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1258:
			//Added by BJZ 7/31/2018
			SQL = "ALTER TABLE workorders DROP COLUMN sschedulechangedby, DROP COLUMN sscheduledby, DROP COLUMN slasteditedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1259:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE bids ADD COLUMN screatedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lcreatedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"

					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			
			//BEGIN CASE:
			case 1260:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE bids"
					+ " INNER JOIN users ON users.sUserName = bids.sCreatedBy"
					+ " SET bids.screatedbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, bids.sCreatedBy, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", bids.lcreatedbyuserid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1261:
			//Added by BJZ 7/31/2018
			SQL = "ALTER TABLE bids DROP COLUMN sCreatedBy";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1262:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE taxcertificates ADD COLUMN lcreatedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"

					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			
			
			//BEGIN CASE:
			case 1263:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE taxcertificates"
					+ " INNER JOIN users ON users.sUserName = taxcertificates.sCreatedBy"
					+ " SET taxcertificates.lcreatedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1264:
			//Added by BJZ 7/31/2018
			SQL = "ALTER TABLE taxcertificates DROP COLUMN sCreatedBy";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1265:
				//Added by TJR 8/7/2018
				SQL = "CREATE TABLE glstatementforms("
					+ "lid int(11) NOT NULL auto_increment COMMENT '[091101]'"
					+ ", sname varchar(64) NOT NULL DEFAULT ''"
					+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
					+ ", mtext MEDIUMTEXT NOT NULL"
					+ ", PRIMARY KEY  (`lid`)"
					+ ", UNIQUE KEY `name_key` (`sname`)"
					+ " ) ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1266:
			//Added by BJZ 7/27/2018
			SQL = "ALTER TABLE icvendors ADD COLUMN slasteditedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN llasteditedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;				
			
			//BEGIN CASE:
			case 1267:
			//Added by BJZ 7/31/2018
			SQL = "UPDATE icvendors"
					+ " INNER JOIN users ON users.sUserName = icvendors.slasteditedby"
					+ " SET icvendors.slasteditedbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, icvendors.slasteditedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					+ ", icvendors.llasteditedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1268:
			//Added by BJZ 7/31/2018
			SQL = "ALTER TABLE icvendors DROP COLUMN slasteditedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1269:
			//Added by BJZ 8/28/2018
				//2 update statements removed... 
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE		
			
			//BEGIN CASE:
			case 1270:
			//Added by BJZ 8/28/2018
				//2 update statements removed... 
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1271:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE salesperson ADD COLUMN lSalespersonUserID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
						
			//BEGIN CASE:
			case 1272:
			//Added by BJZ 8/28/2018
			SQL = "UPDATE salesperson"
					+ " INNER JOIN users ON users.sUserName = salesperson.sSalespersonUserName"
					+ " SET  salesperson.lSalespersonUserID = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1273:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE salesperson DROP PRIMARY KEY, ADD PRIMARY KEY(lSalespersonUserID, sSalespersonCode), DROP COLUMN sSalespersonUserName";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE:
			case 1274:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE gloptions CHANGE COLUMN suser suserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			
			//BEGIN CASE:
			case 1275:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE apchecks ADD COLUMN lprintedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN lcreatedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1276:
			//Added by BJZ 8/28/2018
			SQL = "UPDATE apchecks"
					+ " INNER JOIN users ON users.sUserName = apchecks.screatedby"
					+ " SET  apchecks.lcreatedbyid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1277:
			//Added by BJZ 8/28/2018
			SQL = "UPDATE apchecks"
					+ " INNER JOIN users ON users.sUserName = apchecks.sprintedby"
					+ " SET  apchecks.lprintedbyid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1278:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE apchecks DROP COLUMN sprintedby, DROP COLUMN screatedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1279:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE bkbanks ADD COLUMN llastmaintainedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1280:
			//Added by BJZ 8/28/2018
			SQL = "UPDATE bkbanks"
					+ " INNER JOIN users ON users.sUserName = bkbanks.slastmaintainedby"
					+ " SET  bkbanks.llastmaintainedbyid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1281:
			//Added by BJZ 8/28/2018
			SQL = "ALTER TABLE bkbanks DROP COLUMN slastmaintainedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1282:
			//Added by BJZ 8/31/2018
			SQL = "ALTER TABLE deliverytickets ADD COLUMN linitiatedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1283:
			//Added by BJZ 8/31/2018
			SQL = "UPDATE deliverytickets"
					+ " INNER JOIN users ON users.sUserName = deliverytickets.sinitiatedby"
					+ " SET  deliverytickets.linitiatedbyid = IF(users.lid IS NULL, '0', users.lid)";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1284:
			//Added by BJZ 8/31/2018
			SQL = "ALTER TABLE deliverytickets DROP COLUMN sinitiatedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1285:
			//Added by BJZ 8/31/2018
			SQL = "ALTER TABLE icphysicalcounts ADD COLUMN lcreatedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN screatedbyfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1286:
			//Added by BJZ 8/31/2018
			SQL = "UPDATE icphysicalcounts"
					+ " INNER JOIN users ON users.sUserName = icphysicalcounts.screatedby"
					+ " SET  icphysicalcounts.lcreatedbyid = IF(users.lid IS NULL, '0', users.lid)"
					+ " , icphysicalcounts.screatedbyfullname = CONCAT(IF(users.sUserFirstName IS NULL, icphysicalcounts.screatedby, users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1287:
			//Added by BJZ 8/31/2018
			SQL = "ALTER TABLE icphysicalcounts DROP COLUMN screatedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1288:
			//Added by BJZ 9/4/2018
			SQL = "ALTER TABLE laborbackcharges ADD COLUMN linitiatedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1289:
			//Added by BJZ 9/4/2018
			SQL = "UPDATE laborbackcharges"
					+ " INNER JOIN users ON users.sUserName = laborbackcharges.sinitiatedby"
					+ " SET  laborbackcharges.linitiatedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1290:
			//Added by BJZ 9/4/2018
			SQL = "ALTER TABLE laborbackcharges DROP COLUMN sinitiatedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1291:
			//Added by BJZ 9/10/2018
			SQL = "ALTER TABLE materialreturns ADD COLUMN linitiatedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN lresolvedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1292:
			//Added by BJZ 9/10/2018
			SQL = "UPDATE materialreturns"
					+ " INNER JOIN users ON users.sUserName = materialreturns.sinitiatedby"
					+ " SET  materialreturns.linitiatedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1293:
			//Added by BJZ 9/10/2018
			SQL = "UPDATE materialreturns"
					+ " INNER JOIN users ON users.sUserName = materialreturns.sresolvedby"
					+ " SET  materialreturns.lresolvedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1294:
			//Added by BJZ 9/10/2018
			SQL = "ALTER TABLE materialreturns DROP COLUMN sinitiatedby, DROP COLUMN sresolvedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1295:
			//Added by BJZ 9/4/2018
			SQL = "ALTER TABLE wagescalerecords ADD COLUMN lCreatedByID int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1296:
			//Added by BJZ 9/4/2018
			SQL = "UPDATE wagescalerecords"
					+ " INNER JOIN users ON users.sUserName = wagescalerecords.sCreatedBy"
					+ " SET  wagescalerecords.lCreatedByID = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1297:
			//Added by BJZ 9/4/2018
			SQL = "ALTER TABLE wagescalerecords DROP COLUMN sCreatedBy";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1298:
			//Added by BJZ 9/11/2018
			SQL = "ALTER TABLE ssalarmsequences ADD COLUMN llastarmedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN llastdisarmedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1299:
			//Added by BJZ 9/11/2018
			SQL = "UPDATE ssalarmsequences"
					+ " INNER JOIN users ON users.sUserName = ssalarmsequences.slastarmedby"
					+ " SET  ssalarmsequences.llastarmedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1300:
			//Added by BJZ 9/11/2018
			SQL = "UPDATE ssalarmsequences"
					+ " INNER JOIN users ON users.sUserName = ssalarmsequences.slastdisarmedby"
					+ " SET  ssalarmsequences.llastdisarmedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1301:
			//Added by BJZ 9/11/2018
			SQL = "ALTER TABLE ssalarmsequences DROP COLUMN slastarmedby, DROP COLUMN slastdisarmedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1302:
			//Added by BJZ 9/12/2018
			SQL = "ALTER TABLE ssalarmsequenceusers ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1303:
			//Added by BJZ 9/12/2018
			SQL = "UPDATE ssalarmsequenceusers"
					+ " INNER JOIN users ON users.sUserName = ssalarmsequenceusers.suser"
					+ " SET  ssalarmsequenceusers.luserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1304:
			//Added by BJZ 9/12/2018
			SQL = "ALTER TABLE ssalarmsequenceusers drop index zoneuserkey, add primary key(luserid, lalarmsequenceid), DROP COLUMN suser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1305:
			//Added by BJZ 9/12/2018
			SQL = "ALTER TABLE ssuserevents ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1306:
			//Added by BJZ 9/12/2018
			SQL = "UPDATE ssuserevents"
					+ " INNER JOIN users ON users.sUserName = ssuserevents.suser"
					+ " SET  ssuserevents.luserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1307:
			//Added by BJZ 9/12/2018
			SQL = "ALTER TABLE ssuserevents DROP COLUMN suser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1308:
			//Added by BJZ 9/12/2018
			SQL = "ALTER TABLE ssdeviceusers ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1309:
			//Added by BJZ 9/12/2018
			SQL = "UPDATE ssdeviceusers"
					+ " INNER JOIN users ON users.sUserName = ssdeviceusers.suser"
					+ " SET  ssdeviceusers.luserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1310:
			//Added by BJZ 9/12/2018
				SQL = "ALTER TABLE ssdeviceusers drop index zoneuserkey, add primary key(luserid, ldeviceid), DROP COLUMN suser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1311:
			//Added by BJZ 9/13/2018
			SQL = "ALTER TABLE sscontrollers ADD COLUMN llastmaintainedbyid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1312:
			//Added by BJZ 9/14/2018
			SQL = "UPDATE sscontrollers"
					+ " INNER JOIN users ON users.sUserName = sscontrollers.slastmaintainedby"
					+ " SET  sscontrollers.llastmaintainedbyid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1313:
			//Added by BJZ 9/15/2018
				SQL = "ALTER TABLE sscontrollers DROP COLUMN slastmaintainedby";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1314:
			//Added by BJZ 9/13/2018
			SQL = "ALTER TABLE usergeocodes ADD COLUMN luserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1315:
			//Added by BJZ 9/14/2018
			SQL = "UPDATE usergeocodes"
					+ " INNER JOIN users ON users.sUserName = usergeocodes.suser"
					+ " SET  usergeocodes.luserid = IF(users.lid IS NULL, '0', users.lid)"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1316:
			//Added by BJZ 9/15/2018
				SQL = "ALTER TABLE usergeocodes DROP COLUMN suser";
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1317:
			//Added by BJZ 9/14/2018
			SQL = "ALTER TABLE systemlog ADD COLUMN sLoggingUserFullName varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1318:
			//Added by BJZ 9/17/2018
			SQL = "ALTER TABLE criticaldates "
					+ " ADD COLUMN lassignedbyuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN sassignedbyuserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN lresponsibleuserid int(11) NOT NULL DEFAULT '0' COMMENT '[010102]'"
					+ ", ADD COLUMN sresponsibleuserfullname varchar(128) NOT NULL DEFAULT '' COMMENT ''"
					+ ", ADD COLUMN itype int(11) NOT NULL DEFAULT '0' COMMENT ''"
					+ ", CHANGE COLUMN sordernumber sdocnumber varchar(20) NOT NULL DEFAULT '' COMMENT ''"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1319:
			//Added by BJZ 9/17/2018
			SQL = "UPDATE criticaldates"
					+ " LEFT JOIN salesperson ON criticaldates.Responsible = salesperson.sSalespersonCode "
					+ " LEFT JOIN users ON salesperson.lSalespersonUserID = users.lid "
					+ " SET  criticaldates.lresponsibleuserid = IF(users.lid IS NULL, '0', users.lid)"
					+ ", criticaldates.sresponsibleuserfullname = CONCAT(IF(users.sUserFirstName IS NULL, '', users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
					;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1320:
			//Added by BJZ 9/17/2018
				SQL = "UPDATE criticaldates"
						+ " LEFT JOIN salesperson ON criticaldates.sassignedby = salesperson.sSalespersonCode "
						+ " LEFT JOIN users ON salesperson.lSalespersonUserID = users.lid "
						+ " SET  criticaldates.lassignedbyuserid = IF(users.lid IS NULL, '0', users.lid)"
						+ ", criticaldates.sassignedbyuserfullname = CONCAT(IF(users.sUserFirstName IS NULL, '' , users.sUserFirstName), ' ', IF(users.sUserLastName IS NULL, '', users.sUserLastName))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE	
			
			//BEGIN CASE:
			case 1321:
			//Added by BJZ 9/17/2018
			SQL = "UPDATE criticaldates SET itype = 1";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1322:
			//Added by TJR 9/20/2018
			SQL = "ALTER TABLE tax DROP KEY jurisdictionitypekey, DROP COLUMN itaxtype";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1323:
			//Added by TJR 10/1/2018
			SQL = "ALTER TABLE icphysicalinventories DROP COLUMN sstartingitemnumber, DROP COLUMN sendingitemnumber";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1324:
			//Added by BJZ 10/4/2018
			SQL = "ALTER TABLE smoptions ADD COLUMN iusegoogleplacesapi int(11) NOT NULL DEFAULT '0' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1325:
			//Added by TJR 10/9/2018
			SQL = "ALTER TABLE icoptions ADD COLUMN isuppressbarcodesonnonstockitems int(11) NOT NULL DEFAULT '0' COMMENT '' ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1326:
			//Added by TJR 10/16/2018
			SQL = "ALTER TABLE icaccountsets DROP COLUMN icostingmethod";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1327:
			//Added by BJZ 11/30/2018
				SQL = "ALTER TABLE smoptions ADD COLUMN iinvoicingflag int(11) NOT NULL DEFAULT '0' COMMENT '' "
					+ ", ADD COLUMN datinvoicingflagdatetime datetime DEFAULT NULL COMMENT ''"
					+ ", ADD COLUMN iinvoicinguserid int(11) NOT NULL DEFAULT '0' COMMENT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
					
			//BEGIN CASE:
			case 1328:
			//Added by EMM 12/06/2018
				SQL = "ALTER TABLE doingbusinessasaddresses "
						+" ADD sInvoiceLogo varchar(100) NOT NULL DEFAULT '',"
						+" ADD sProposalLogo varchar(100) NOT NULL DEFAULT '',"
						+ " ADD sDeliveryTicketReceiptLogo varchar(100) NOT NULL DEFAULT '',"
						+ " ADD sWorkOrderReceiptlogo varchar(100) NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1329:
			//Added by BJZ 12/13/2018
				SQL = "ALTER TABLE systemlog CHANGE COLUMN iLoggingID lid int(11) NOT NULL auto_increment COMMENT ''"
						+", CHANGE COLUMN datLoggingDate datloggingtime datetime DEFAULT NULL"
						+", CHANGE COLUMN sLoggingUser suserid varchar(128) NOT NULL DEFAULT '' COMMENT '[010102]'"
						+", CHANGE COLUMN sLoggingUserFullName suserfullname varchar(128) NOT NULL DEFAULT ''"
						+", CHANGE COLUMN sLoggingOperation soperation varchar(50) NOT NULL DEFAULT ''"
						+", CHANGE COLUMN sLoggingDescription mdescription mediumtext"
						+", CHANGE COLUMN sLoggingSQL mcomment mediumtext"
						+", DROP COLUMN sLoggingTime"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1330:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'datLoggingDate', 'datloggingtime')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1331:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'sLoggingUser', 'suserid')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1332:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'sLoggingUserFullName', 'suserfullname')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE:
			case 1333:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'sLoggingOperation', 'soperation')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

			//BEGIN CASE:
			case 1334:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'sLoggingDescription', 'mdescription')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE

			//BEGIN CASE:
			case 1335:
			//Added by TJR 12/31/2018
				SQL = "UPDATE savedqueries SET ssql = REPLACE(ssql, 'sLoggingSQL', 'mcomment')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;
			//END CASE
			
			//BEGIN CASE
			case 1336:
				//Added by EMM 1/2/2019
				SQL = "ALTER TABLE invoiceheaders "
							+" ADD COLUMN sdbainvoicelogo varchar(128) NOT NULL DEFAULT ''";
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
					iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
				
				
			//BEGIN CASE	
			case 1337:	
				//Added by EMM 1/2/2019
				SQL = "UPDATE  invoiceheaders  SET sdbainvoicelogo = (SELECT sinvoicelogofilename FROM smoptions)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
			
				
			case 1338:
				//Added by EMM 1/2/2019
				SQL = "ALTER TABLE proposals "
							+" ADD COLUMN sdbaproposallogo varchar(128) NOT NULL DEFAULT ''";
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
					iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
				
				//BEGIN CASE	
			case 1339:	
				//Added by EMM 1/2/2019
				SQL = "UPDATE  proposals  SET sdbaproposallogo = (SELECT sproposallogofilename FROM smoptions)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
				
				
			case 1340:
				//Added by EMM 1/2/2019
				SQL = "ALTER TABLE deliverytickets "
							+" ADD COLUMN sdbadeliveryticketreceiptlogo varchar(128)NOT NULL DEFAULT ''";
					if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
					iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
				
				//BEGIN CASE	
			 case 1341:	
				//Added by EMM 1/2/2019
				SQL = "UPDATE  deliverytickets  SET sdbadeliveryticketreceiptlogo = (SELECT sproposallogofilename FROM smoptions)";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE	
				
				//BEGIN CASE
			case 1342:
				//Added by EMM 1/2/2019
			    SQL = "ALTER TABLE workorders "
						+" ADD COLUMN sdbaworkorderlogo varchar(128)NOT NULL DEFAULT ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
				//END CASE
					
			  //BEGIN CASE	
			case 1343:	
			  //Added by EMM 1/2/2019
			  SQL = "UPDATE  workorders  SET sdbaworkorderlogo = (SELECT sproposallogofilename FROM smoptions)";
			  if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
			  iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			  break;
			  //END CASE
			  
			//BEGIN CASE
			case 1344:	
				  //Added by EMM 1/2/2019
				  SQL = "UPDATE  doingbusinessasaddresses dba, smoptions sm  "
				  		+ " SET dba.sInvoiceLogo = sm.sinvoicelogofilename, "
						+ " dba.sProposalLogo = sm.sproposallogofilename, "
				  		+ " dba.sDeliveryTicketReceiptLogo = sm.sproposallogofilename, "
						+ " dba.sWorkOrderReceiptlogo = sm.sproposallogofilename "
				  		;
				  if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				  iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				  break;
			//END CASE
				  
			//BEGIN CASE
			case 1345:	
				//Added by BJZ 1/4/2019
				 SQL = "UPDATE  savedqueries SET ssql = REPLACE(ssql, 'SESSIONTAG=*SESSIONTAG*','db=*DBID*')";
				 if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				 iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				 break;
			//END CASE
				 
			//BEGIN CASE
			case 1346:
				//Added by EMM 1/14/2019
				 SQL = "ALTER TABLE  icitems ADD icannotbepurchased int(11) NOT NULL DEFAULT 0; ";
				 if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				 iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				 break;
			//END CASE
				 
			//BEGIN CASE
			case 1347:
				//Added by BJZ 1/16/2019
				SQL = "UPDATE proposals SET sbodydescription=REPLACE(sbodydescription,'\\n','<br/>')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1348:
				//Added by BJZ 1/16/2019
				SQL = "UPDATE proposals SET sbodydescription=REPLACE(sbodydescription,'\\r','')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1349:
				//Added by BJZ 1/16/2019
				SQL = "UPDATE proposalphrases SET mproposalphrase=REPLACE(mproposalphrase,'\\n','<br/>')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1350:
				//Added by BJZ 1/16/2019
				SQL = "UPDATE proposalphrases SET mproposalphrase=REPLACE(mproposalphrase,'\\r','')";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 1351:
				//Added by BJZ 1/16/2019
				SQL = "ALTER TABLE  invoicedetails ADD bdexpensedcost  decimal(17,2) NOT NULL default '0.00' COMMENT 'The average cost of non-stock items at the time the invoice is created.'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1352:
				//Added by TJR 2/4/2019
				SQL = "ALTER TABLE icitems  ADD icannotbesold int(11) NOT NULL DEFAULT '0'; ";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1353:
				//Added by TJR 2/21/2019
				SQL = "ALTER TABLE icpoheaders DROP COLUMN lphase";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1354:
				//Added by TJR 2/22/2019
				SQL = "ALTER TABLE materialreturns"
					+ " ADD COLUMN itobereturned INT(11) NOT NULL DEFAULT '0'"
					+ ", ADD COLUMN svendoracct VARCHAR(12) NOT NULL DEFAULT ''"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE

			//BEGIN CASE
			case 1355:
				//Added by TJR 3/8/2019
				SQL = "ALTER TABLE smoptions"
					+ " DROP COLUMN sbankname"
					+ ", DROP COLUMN sbankrecglacct"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1356:
				//Added by TJR 3/11/2019
				SQL = "ALTER TABLE glaccounts"
					+ " ADD COLUMN bdannualbudget DECIMAL(17,2) NOT NULL DEFAULT '0.00' COMMENT 'The annual budget for the GL account.'";
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1357:
				//Added by TJR 3/13/2019
				SQL = "ALTER TABLE glaccounts"
					+ " ADD COLUMN inormalbalancetype INT(11) NOT NULL DEFAULT '0' COMMENT 'Indicates account type (credit or debit balance).'";
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1358:
				//Added by TJR 3/13/2019
				SQL = "CREATE TABLE `glfinancialstatementdata` ("
					+ "`sacctid` varchar(45) NOT NULL DEFAULT '' COMMENT '[090101]',"
					+ "`ifiscalyear` int(11) NOT NULL DEFAULT '0',"
					+ "`ifiscalperiod` int(11) NOT NULL DEFAULT '0',"
					+ "`bdnetchangeforperiod` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdnetchangeforperiodpreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdtotalyeartodate` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdtotalpreviousyeartodate` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdopeningbalancepreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdopeningbalance` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdnetchangeforpreviousperiod` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "`bdnetchangeforpreviousperiodpreviousyear` decimal(17,2) NOT NULL DEFAULT '0.00',"
					+ "PRIMARY KEY (`sacctid`,`ifiscalyear`,`ifiscalperiod`)"
					+ ") ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1359:
				//Added by TJR 3/21/2019
				SQL = "DROP TABLE `glstatementforms`";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1360:
				//Added by TJR 3/21/2019
				SQL = "ALTER TABLE `gltransactionlines` ADD COLUMN datpostingdate date NOT NULL default '0000-00-00'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1361:
				//Added by TJR 3/21/2019
				SQL = "ALTER TABLE `gltransactionlines`"
					+ " ADD COLUMN iconsolidatedposting INT(11) NOT NULL default '0'"
					+ ", ADD COLUMN ssourcetype VARCHAR(2) NOT NULL default ''";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
				//BEGIN CASE
			case 1362:
				//Added by BJZ 3/25/2019
				SQL = "UPDATE `orderheaders`"
					+ " SET sServiceTypeCode = IF(sServiceTypeCode='SH0001','RS',IF(sServiceTypeCode='SH0002','RI',IF(sServiceTypeCode='SH0003','CS',IF(sServiceTypeCode='SH0004','CI',sServiceTypeCode))))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
			//BEGIN CASE
			case 1363:
				//Added by BJZ 3/25/2019
				SQL = "UPDATE `invoiceheaders`"
						+ " SET sServiceTypeCode = IF(sServiceTypeCode='SH0001','RS',IF(sServiceTypeCode='SH0002','RI',IF(sServiceTypeCode='SH0003','CS',IF(sServiceTypeCode='SH0004','CI',sServiceTypeCode))))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1364:
				//Added by BJZ 3/25/2019
				SQL = "UPDATE `defaultitemcategories`"
					+ " SET ServiceTypeCode = IF(ServiceTypeCode='SH0001','RS',IF(ServiceTypeCode='SH0002','RI',IF(ServiceTypeCode='SH0003','CS',IF(ServiceTypeCode='SH0004','CI',ServiceTypeCode))))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
			//BEGIN CASE
			case 1365:
				//Added by BJZ 3/25/2019
				SQL = "UPDATE `servicetypes`"
					+ " SET sCode = IF(sCode='SH0001','RS',IF(sCode='SH0002','RI',IF(sCode='SH0003','CS',IF(sCode='SH0004','CI',sCode))))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
			
				//BEGIN CASE
			case 1366:
				//Added by BJZ 3/13/2019
				SQL = "CREATE TABLE `mechanicservicetypes` ("
					+ "`imechanicid` int(11) NOT NULL DEFAULT '0' COMMENT '[010301]',"
					+ "`sservicetypecode` varchar(6) NOT NULL DEFAULT '' COMMENT '[011302]',"
					+ "PRIMARY KEY (`imechanicid`,`sservicetypecode`)"
					+ ") ENGINE=InnoDB"
				;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
				
			//BEGIN CASE:
			case 1367:
				//Added by BJZ 3/26/2019
				SQL = "SELECT lid, iMechType  FROM mechanics";
				int iMechType = 0;
				int iMechid = 0;
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	
					while (rs.next()){
						iMechType = rs.getInt("iMechType");
						iMechid = rs.getInt("lid");
						
						if(iMechType > 0 && iMechType < 16) {

						SQL = "INSERT INTO mechanicservicetypes (imechanicid, sservicetypecode) VALUES ";
	
						switch(iMechType) {
						case 1: SQL += "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")";	break;	
						case 2: SQL += "(" + Integer.toString(iMechid) + ", " + "'RI'" + ")"; break;
						case 3: SQL += "(" + Integer.toString(iMechid) + ", " + "'RI'" + "), "
									 + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")"; break;
						case 4: SQL += "(" + Integer.toString(iMechid) + ", " + "'CS'" + ")"; break;
						case 5: SQL += "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")";	break;	
						case 6: SQL += "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + ")"; break;
						case 7: SQL += "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")"; break;
						case 8: SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + ")"; break;
						case 9: SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")";	break;
						case 10:SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + ")";	break;
						case 11:SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")"; break;
						case 12:SQL += "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'CI'" + ")"; break;
						case 13:SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")"; break;
						case 14:SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + ")"; break;
						case 15:SQL += "(" + Integer.toString(iMechid) + ", " + "'CI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'CS'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RI'" + "), "
								     + "(" + Integer.toString(iMechid) + ", " + "'RS'" + ")"; break;
						}
						SQL += "";
						if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
						}
					}
					rs.close();
				} catch (SQLException e) {
					System.out.println("Error [1553627170] populating mechanicsservicetype table - " + e.getMessage());
				}
					iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;	
				//END CASE
				
		   //BEGIN CASE:
			case 1368:
				//Added by BJZ 3/26/2019
				SQL = "ALTER TABLE `servicetypes` "
						+ "CHANGE `id` `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '[011301]', "
						+ "CHANGE `sCode` `sCode` varchar(6) NOT NULL DEFAULT '' COMMENT '[011302]'";
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
			break;	
			//END CASE
			
			//BEGIN CASE
			case 1369:
				//Added by BJZ 4/1/2019
				SQL = "UPDATE `workperformedcodes`"
					+ " SET sCode = IF(sCode='SH0001','RS',IF(sCode='SH0002','RI',IF(sCode='SH0003','CS',IF(sCode='SH0004','CI',sCode))))"
						;
				if (!execUpdate(sUser, SQL, conn, iSystemDatabaseVersion)){return false;}
				iVersionUpdatedTo = iSystemDatabaseVersion + 1;
				break;
			//END CASE
				
		//End switch:
		}
		
		//Now update the database version in the data:
		SQL = "UPDATE " + SMTablecompanyprofile.TableName
		+ " SET " + SMTablecompanyprofile.iDatabaseVersion
		+ " = " + Integer.toString(iVersionUpdatedTo);

		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (Exception ex) {
			m_sErrorMessage = "SQL Error updating from database version " + iSystemDatabaseVersion
			+ " with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}

		return true;
	}

}
