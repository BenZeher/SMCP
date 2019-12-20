package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglaccountsegments;
import SMDataDefinition.SMTableglaccountstructures;
import SMDataDefinition.SMTableglacctsegmentvalues;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTableglfiscalsets;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class GLACCPACConversion  extends java.lang.Object{

	
	public GLACCPACConversion(){
		
	}
	
	public String reverseDataChanges(Connection cnSMCP, boolean bRemoveAllReminders) throws Exception{
		String s = "Rolling back critical changes to SMCP data...<BR>";
		//We only remove CRITICAL data changes, i.e., those that might affect processing:

		//System.out.println("[1552318880] - starting reverseDataChanges.");
		
		//Remove any segments that we added from ACCPAC:
		String SQL = "TRUNCATE " + SMTableglaccountsegments.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1523028659] - could not remove GL account segments - " + e.getMessage());
		}
		s+= "GL Account Segments that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318881] - removed segments.");
		
		//Remove any segment values that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTableglacctsegmentvalues.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1523305752] - could not remove GL account segment values - " + e.getMessage());
		}
		s+= "GL Account Segment Values that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318882] - removed segment values.");
		
		//Remove any account structures that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTableglaccountstructures.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1523305753] - could not remove GL account structures - " + e.getMessage());
		}
		s+= "GL Account Structures that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318883] - removed account structures.");
		
		//Remove any GL accounts that were added in a previous conversion:
		SQL = "DELETE FROM " + SMTableglaccounts.TableName
			+ " WHERE ("
				+ "(" + SMTableglaccounts.iaddedbyACCPACconversion + " = 1)"
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1527796104] - could not remove GL accounts added by the conversion with SQL '" + SQL + "' - " + e.getMessage());
		}
		s+= "GL Accounts that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318884] - removed accounts created from ACCPAC.");
		
		//Remove any GL financial statement data that was added in a previous conversion:
		SQL = "TRUNCATE " + SMTableglfiscalsets.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528222445] - could not remove GL fiscal sets added by the conversion with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "TRUNCATE " + SMTableglfinancialstatementdata.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528223445] - could not remove GL financial statement data added by the conversion with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		s+= "GL financial statement data that was added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318885] - removed fiscal sets.");
		
		//Remove any account groups that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTableglaccountgroups.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528310248] - could not remove GL account groups - " + e.getMessage());
		}
		s+= "GL Account Groups that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318886] - removed account groups.");
		
		//Remove any fiscal calendars that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTableglfiscalperiods.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1530809060] - could not remove GL fiscal periods - " + e.getMessage());
		}
		s+= "GL Fiscal Periods that were added from ACCPAC have been removed.<BR>";
		
		//System.out.println("[1552318887] - removed fiscal periods.");
		
		//Remove any GL transactions that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTablegltransactionlines.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1553200508] - could not remove GL transactions - " + e.getMessage());
		}
		s+= "GL transactions that were added from ACCPAC have been removed.<BR>";
		
		//Here we'll try to add the unique key to the gltransactionlines table JUST IN CASE that hasn't been done yet.
		//But if it fails, we're not going to hold up the show over it:
		String sAddingKeyMessage = "Unique key successfully added to " + SMTablegltransactionlines.TableName + ".";
		SQL = "ALTER TABLE " + SMTablegltransactionlines.TableName + " add unique key uniquetransactionkey ("
			+ SMTablegltransactionlines.loriginalbatchnumber
			+ ", " + SMTablegltransactionlines.loriginalentrynumber
			+ ", " + SMTablegltransactionlines.loriginallinenumber
			+ ", " + SMTablegltransactionlines.ifiscalyear
			+ ", " + SMTablegltransactionlines.ifiscalperiod
			+ ")"
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			//We don't react to this error, since the key might already be on there....
			sAddingKeyMessage = "Could not add key to " + SMTablegltransactionlines.TableName + " - " + e.getMessage();
		}
		s+= sAddingKeyMessage + ".<BR>";
		
		//System.out.println("[1553200509] - removed GL transactions.");
		
		//Remove any GL transaction batches that we might have been testing:
		SQL = "TRUNCATE " + SMTablegltransactionbatches.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1558378442] - could not remove GL transaction batches - " + e.getMessage());
		}
		
		SQL = "TRUNCATE " + SMTablegltransactionbatchentries.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1558378444] - could not remove GL transaction batch entries - " + e.getMessage());
		}
		
		SQL = "TRUNCATE " + SMTablegltransactionbatchlines.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1558378445] - could not remove GL transaction batch lines - " + e.getMessage());
		}
		
		s+= "GL transaction batches have been removed.<BR>";
		
		//System.out.println("[1558378443] - removed GL transaction batches.");
		
		return s;
	}
	
	public String processGLAcctSegmentTables(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete the current segment records:
		String sTablename = SMTableglaccountsegments.TableName;
		String SQL = "TRUNCATE " + SMTableglaccountsegments.TableName;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1523044170] - could not delete existing records from " + sTablename + " table - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM GLABK";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsAcctSegments = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsAcctSegments.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglaccountsegments.ilength
				+ ", " + SMTableglaccountsegments.sdescription
				+ ", " + SMTableglaccountsegments.iuseinclosing
				+ ") VALUES ("
				+ Integer.toString(rsAcctSegments.getInt("ABLKLEN"))
				+ ", '" + FormatSQLStatement(rsAcctSegments.getString("ABLKDESC").trim()) + "'"
				+ ", " + Integer.toString(rsAcctSegments.getInt("CLOSESW"))
				+ ") ON DUPLICATE KEY UPDATE "
				+ " " + SMTableglaccountsegments.ilength + " = " + Integer.toString(rsAcctSegments.getInt("ABLKLEN"))
				+ ", " + SMTableglaccountsegments.sdescription + " = '" + FormatSQLStatement(rsAcctSegments.getString("ABLKDESC").trim()) + "'"
				+ ", " + SMTableglaccountsegments.iuseinclosing + " = " + Integer.toString(rsAcctSegments.getInt("CLOSESW"))
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsAcctSegments.close();
				throw new Exception("Error [1523041993] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsAcctSegments.close();

		sStatus +=  "<BR>Added " + Integer.toString(iCounter) + " GL account segments to " + sTablename + "<BR>";
		
		return sStatus;
		
	}
	
	public String processGLAcctSegmentValueTables(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete the current segment value records:
		String sTablename = SMTableglacctsegmentvalues.TableName;
		String SQL = "TRUNCATE " + SMTableglacctsegmentvalues.TableName;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1523304851] - could not delete existing records from " + sTablename + " table - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM GLASV"
			+ " LEFT JOIN GLABK ON GLASV.IDSEG=GLABK.ACCTBLKID"
			+ " ORDER BY GLASV.IDSEG, GLASV.SEGVALDESC"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsAcctSegmentValues = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsAcctSegmentValues.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglacctsegmentvalues.lsegmentid
				+ ", " + SMTableglacctsegmentvalues.sdescription
				+ ", " + SMTableglacctsegmentvalues.svalue
				+ ") SELECT"
				+ " " + SMTableglaccountsegments.lid
				+ ", '" + FormatSQLStatement(rsAcctSegmentValues.getString("SEGVALDESC").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsAcctSegmentValues.getString("SEGVAL").trim()) + "'"
				+ " FROM " + SMTableglaccountsegments.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccountsegments.sdescription + " = '" + FormatSQLStatement(rsAcctSegmentValues.getString("ABLKDESC").trim()) + "')"
				+ ")"
			;
			//System.out.println("[1527800693] - SQL = '" + SQLInsert + "'.");
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsAcctSegmentValues.close();
				throw new Exception("Error [1523305683] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsAcctSegmentValues.close();

		sStatus +=  "<BR>Added " + Integer.toString(iCounter) + " GL account segment values to " + sTablename + "<BR>";
		
		return sStatus;
		
	}
	
	public String processGLAccountStructureTables(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			String sDatabaseName,
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete the current account structure records:
		String sTablename = SMTableglaccountstructures.TableName;
		String SQL = "TRUNCATE " + sTablename;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1524246646] - could not delete existing records from " + sTablename + " table - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM GLABRX"
			+ " LEFT JOIN GLABK ON GLABRX.ABRKID1=GLABK.ACCTBLKID"
			+ " ORDER BY GLABRX.ACCTBRKID"
		;
		
		SQL = "SELECT"
			 + "[GLABRX].[ACCTBRKID]"
			 + ", [GLABRX].[ABRKDESC]"
			 + ", [GLABRX].[ABRKLEN1]"
			 + ", [GLABRX].[ABRKLEN2]"
			 + ", [GLABRX].[ABRKLEN3]"
			 + ", [GLABRX].[ABRKLEN4]"
			 + ", [GLABRX].[ABRKLEN5]"
			 + ", [GLABRX].[ABRKLEN6]"
			 + ", [GLABRX].[ABRKLEN7]"
			 + ", [GLABRX].[ABRKLEN8]"
			 + ", [GLABRX].[ABRKLEN9]"
			 + ", [GLABRX].[ABRKLEN10]"
			 
			 + ", ISNULL(SEGMENT1.ABLKDESC, '') AS 'SEGMENT1DESC'"
			 + ", ISNULL(SEGMENT2.ABLKDESC, '') AS 'SEGMENT2DESC'"
			 + ", ISNULL(SEGMENT3.ABLKDESC, '') AS 'SEGMENT3DESC'"
			 + ", ISNULL(SEGMENT4.ABLKDESC, '') AS 'SEGMENT4DESC'"
			 + ", ISNULL(SEGMENT5.ABLKDESC, '') AS 'SEGMENT5DESC'"
			 + ", ISNULL(SEGMENT6.ABLKDESC, '') AS 'SEGMENT6DESC'"
			 + ", ISNULL(SEGMENT7.ABLKDESC, '') AS 'SEGMENT7DESC'"
			 + ", ISNULL(SEGMENT8.ABLKDESC, '') AS 'SEGMENT8DESC'"
			 + ", ISNULL(SEGMENT9.ABLKDESC, '') AS 'SEGMENT9DESC'"
			 + ", ISNULL(SEGMENT10.ABLKDESC, '') AS 'SEGMENT10DESC'"
			 
			 + " FROM [" + sDatabaseName + "].[dbo].[GLABRX]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT1\" ON [GLABRX].[ABRKID1]=SEGMENT1.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT2\" ON [GLABRX].[ABRKID2]=SEGMENT2.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT3\" ON [GLABRX].[ABRKID3]=SEGMENT3.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT4\" ON [GLABRX].[ABRKID4]=SEGMENT4.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT5\" ON [GLABRX].[ABRKID5]=SEGMENT5.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT6\" ON [GLABRX].[ABRKID6]=SEGMENT6.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT7\" ON [GLABRX].[ABRKID7]=SEGMENT7.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT8\" ON [GLABRX].[ABRKID8]=SEGMENT8.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT9\" ON [GLABRX].[ABRKID9]=SEGMENT9.[ACCTBLKID]"
			 + " LEFT JOIN [" + sDatabaseName + "].[dbo].[GLABK] AS \"SEGMENT10\" ON [GLABRX].[ABRKID10]=SEGMENT10.[ACCTBLKID]"
			 + " ORDER BY [GLABRX].[ACCTBRKID]"
		;
		//System.out.println("[1560445567] ACCPAC SQL = '" + SQL + "'.");
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rs = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rs.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglaccountstructures.llength1
				+ ", " + SMTableglaccountstructures.llength2
				+ ", " + SMTableglaccountstructures.llength3
				+ ", " + SMTableglaccountstructures.llength4
				+ ", " + SMTableglaccountstructures.llength5
				+ ", " + SMTableglaccountstructures.llength6
				+ ", " + SMTableglaccountstructures.llength7
				+ ", " + SMTableglaccountstructures.llength8
				+ ", " + SMTableglaccountstructures.llength9
				+ ", " + SMTableglaccountstructures.llength10
				
				+ ", " + SMTableglaccountstructures.lsegmentid1
				+ ", " + SMTableglaccountstructures.lsegmentid2
				+ ", " + SMTableglaccountstructures.lsegmentid3
				+ ", " + SMTableglaccountstructures.lsegmentid4
				+ ", " + SMTableglaccountstructures.lsegmentid5
				+ ", " + SMTableglaccountstructures.lsegmentid6
				+ ", " + SMTableglaccountstructures.lsegmentid7
				+ ", " + SMTableglaccountstructures.lsegmentid8
				+ ", " + SMTableglaccountstructures.lsegmentid9
				+ ", " + SMTableglaccountstructures.lsegmentid10
				
				
				+ ", " + SMTableglaccountstructures.sdescription
				+ ", " + SMTableglaccountstructures.sstructureid
				+ ") SELECT"
				+ " " + Integer.toString(rs.getInt("ABRKLEN1"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN2"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN3"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN4"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN5"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN6"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN7"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN8"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN9"))
				+ ", " + Integer.toString(rs.getInt("ABRKLEN10"))

				+ ", " + "IF(SEGMENTTABLE1." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE1." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE2." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE2." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE3." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE3." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE4." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE4." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE5." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE5." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE6." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE6." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE7." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE7." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE8." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE8." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE9." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE9." + SMTableglaccountsegments.lid + ")"
				+ ", " + "IF(SEGMENTTABLE10." + SMTableglaccountsegments.lid + " IS NULL, 0, SEGMENTTABLE10." + SMTableglaccountsegments.lid + ")"
				
				+ ", '" + FormatSQLStatement(rs.getString("ABRKDESC").trim()) + "'"
				+ ", '" + FormatSQLStatement(rs.getString("ACCTBRKID").trim()) + "'"
				
				+ " FROM"
				+ " " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE1"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE2 ON SEGMENTTABLE2." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT2DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE3 ON SEGMENTTABLE3." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT3DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE4 ON SEGMENTTABLE4." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT4DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE5 ON SEGMENTTABLE5." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT5DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE6 ON SEGMENTTABLE6." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT6DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE7 ON SEGMENTTABLE7." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT7DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE8 ON SEGMENTTABLE8." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT8DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE9 ON SEGMENTTABLE9." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT9DESC").trim() + "'"
				+ " LEFT JOIN " + SMTableglaccountsegments.TableName + " AS SEGMENTTABLE10 ON SEGMENTTABLE10." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT10DESC").trim() + "'"
				
				+ " WHERE ("
					+ "(SEGMENTTABLE1." + SMTableglaccountsegments.sdescription + " = '" + rs.getString("SEGMENT1DESC").trim() + "')" 
				+ ")"
			;

			//System.out.println("[1524253546] - SQL = " + SQLInsert);
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rs.close();
				throw new Exception("Error [1524247221] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rs.close();

		sStatus +=  "<BR>Added " + Integer.toString(iCounter) + " GL account structures to " + sTablename + "<BR>";
		
		return sStatus;
		
	}
	public String processGLAccounts(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL accounts that may have been added by a previous conversion:
		String sTablename = SMTableglaccounts.TableName;
		String SQL = "DELETE FROM " + SMTableglaccounts.TableName
			+ " WHERE ("
				+ "(" + SMTableglaccounts.iaddedbyACCPACconversion + " = 1)"
			+ ")"
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1527796252] - could not delete GL accounts that were previously added using SQL '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM GLAMF"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsGLAccts = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsGLAccts.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglaccounts.lActive
				+ ", " + SMTableglaccounts.iaddedbyACCPACconversion
				+ ", " + SMTableglaccounts.inormalbalancetype
				+ ", " + SMTableglaccounts.laccountgroupid
				+ ", " + SMTableglaccounts.lstructureid
				+ ", " + SMTableglaccounts.sAcctID
				+ ", " + SMTableglaccounts.sAcctType
				+ ", " + SMTableglaccounts.sDesc
				+ ", " + SMTableglaccounts.sFormattedAcct
				+ ")"
				
				+ " SELECT"
				+ " " + Integer.toString(rsGLAccts.getInt("ACTIVESW"))
				+ ", 1"
				+ ", " + Integer.toString(rsGLAccts.getInt("ACCTBAL"))
				+ ", " + "ACCOUNTGROUPSTABLE." + SMTableglaccountgroups.lid
				+ ", " + "STRUCTURESTABLE." + SMTableglaccountstructures.lid
				+ ", '" + FormatSQLStatement(rsGLAccts.getString("ACCTID").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsGLAccts.getString("ACCTTYPE").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsGLAccts.getString("ACCTDESC").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsGLAccts.getString("ACCTFMTTD").trim()) + "'"
				+ " FROM " + SMTableglaccountstructures.TableName + " STRUCTURESTABLE"
				+ ", " + SMTableglaccountgroups.TableName + " ACCOUNTGROUPSTABLE"
				+ " WHERE ("
					+ "(" + SMTableglaccountstructures.sstructureid + " = '" + FormatSQLStatement(rsGLAccts.getString("ABRKID").trim()) + "')"
					+ " AND (" + SMTableglaccountgroups.sgroupcode + " = '" + FormatSQLStatement(rsGLAccts.getString("ACCTGRPCOD").trim()) + "')"
				+ ")"
				
				+ " ON DUPLICATE KEY UPDATE "
				+ SMTableglaccounts.lActive + " = " + Integer.toString(rsGLAccts.getInt("ACTIVESW"))
				+ ", " + SMTableglaccounts.inormalbalancetype + " = " + Integer.toString(rsGLAccts.getInt("ACCTBAL"))
				+ ", " + SMTableglaccounts.laccountgroupid + " = " + "ACCOUNTGROUPSTABLE." + SMTableglaccountgroups.lid
				+ ", " + SMTableglaccounts.lstructureid + " = " + "STRUCTURESTABLE." + SMTableglaccountstructures.lid
				+ ", " + SMTableglaccounts.sAcctType + " = '" + FormatSQLStatement(rsGLAccts.getString("ACCTTYPE").trim()) + "'"
				+ ", " + SMTableglaccounts.sDesc + " = '" + FormatSQLStatement(rsGLAccts.getString("ACCTDESC").trim()) + "'"
				+ ", " + SMTableglaccounts.sFormattedAcct + " = '" + FormatSQLStatement(rsGLAccts.getString("ACCTFMTTD").trim()) + "'"
			;
			//System.out.println("[1527802644] - SQL = '" + SQLInsert + "'.");
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLAccts.close();
				throw new Exception("Error [1527796445] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsGLAccts.close();

		sStatus +=  "<BR>Updated or inserted " + Integer.toString(iCounter) + " GL accounts to " + sTablename + "<BR>";
		
		return sStatus;
		
	}
	
	public String processGLFinancialData(Connection cnSMCP, String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL financial statement data that may have been added by a previous conversion:
		String sTablename = SMTableglfinancialstatementdata.TableName;
		String SQL = "DELETE FROM " + sTablename
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1570825887] - could not delete GL financial statement data using SQL '" + SQL + "' - " + e.getMessage());
		}	
				
		//Next, get the earliest year in the fiscal set data:
		String sStartingFiscalYear = "";
		SQL = "SELECT"
			+ " " + SMTableglfiscalsets.ifiscalyear 
			+ " FROM  " + SMTableglfiscalsets.TableName
			+ " ORDER BY " + SMTableglfiscalsets.ifiscalyear + " ASC";
		ResultSet rsFiscalSets = clsDatabaseFunctions.openResultSet(SQL, cnSMCP);
		if (rsFiscalSets.next()){
			sStartingFiscalYear = Long.toString(rsFiscalSets.getInt(SMTableglfiscalsets.ifiscalyear));
		}
		rsFiscalSets.close();
		
		GLFinancialDataCheck dc = new GLFinancialDataCheck();
		ServletUtilities.clsDatabaseFunctions.start_data_transaction(cnSMCP);
		try {
			dc.processFinancialRecords(
				"", 
				sStartingFiscalYear,
				cnSMCP,
				true
			);
		} catch (Exception e1) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(cnSMCP);
			throw new Exception("Error [20193541558439] " + " - " + e1.getMessage() + ".");
		}
		ServletUtilities.clsDatabaseFunctions.commit_data_transaction(cnSMCP);
		
		//Get the count of financial statement records:
		SQL = "SELECT COUNT(*) AS RECORDCOUNT FROM " + SMTableglfinancialstatementdata.TableName;
		long lCount;
		try {
			ResultSet rsCount = clsDatabaseFunctions.openResultSet(SQL, cnSMCP);
			lCount = 0L;
			if (rsCount.next()){
				lCount = rsCount.getLong("RECORDCOUNT");
			}
			rsCount.close();
		} catch (Exception e) {
			throw new Exception("Error [20192841639353] " + "reading record count from " + SMTableglfinancialstatementdata.TableName + " - " + e.getMessage());
		}
		
		sStatus +=  "<BR>Processed GL fiscal sets into " 
			+ Long.toString(lCount) + " financial statement records.";
		return sStatus;
		
	}
	
	public String processGLFiscalSets(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL fiscal sets that may have been added by a previous conversion:
		String sTablename = SMTableglfiscalsets.TableName;
		String SQL = "DELETE FROM " + SMTableglfiscalsets.TableName
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528221318] - could not delete GL financial statement data using SQL '" + SQL + "' - " + e.getMessage());
		}
		
		Statement stmtCommit;
		try {
			stmtCommit = cnSMCP.createStatement();
			stmtCommit.execute("SET autocommit=0");
		} catch (Exception e1) {
			throw new Exception("Error [20192971431473] " + "setting AUTOCOMMIT to ZERO to insert GL fiscal sets - " + e1.getMessage());
		}
		
		SQL = "SELECT * FROM GLAFS"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsGLFiscalSets = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsGLFiscalSets.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglfiscalsets.bdnetchangeperiod1
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod10
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod11
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod12
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod13
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod14
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod15
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod2
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod3
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod4
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod5
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod6
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod7
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod8
				+ ", " + SMTableglfiscalsets.bdnetchangeperiod9
				+ ", " + SMTableglfiscalsets.bdopeningbalance
				+ ", " + SMTableglfiscalsets.ifiscalyear
				+ ", " + SMTableglfiscalsets.sAcctID
				+ ") VALUES("
				+ " " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD1")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD10")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD11")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD12")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD13")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD14")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD15")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD2")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD3")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD4")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD5")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD6")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD7")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD8")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("NETPERD9")).replace(",", "")
				+ ", " + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableglfiscalsets.bdnetchangeperiodScale, rsGLFiscalSets.getBigDecimal("OPENBAL")).replace(",", "")
				+ ", " + Integer.toString(rsGLFiscalSets.getInt("FSCSYR"))
				+ ", '" + FormatSQLStatement(rsGLFiscalSets.getString("ACCTID").trim()) + "'"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				throw new Exception("Error [1528222265] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsGLFiscalSets.close();
		
		try {
			stmtCommit.execute("COMMIT");
		} catch (Exception e) {
			throw new Exception("Error [20192971432371] " + "committing GL Fiscal Set Inserts - " + e.getMessage());
		}

		sStatus +=  "<BR>Inserted " + Integer.toString(iCounter) + " GL fiscal set records into " + sTablename + ".<BR>";

		return sStatus;
		
	}
	public String processGLTransactions(
		Connection cnSMCP, 
		Connection cnACCPAC, 
		int iAPDatabaseType, 
		String sUser,
		String sUserID,
		String sUserFullName) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL transactionlines that may have been added by a previous conversion:
		String sTablename = SMTablegltransactionlines.TableName;
		String SQL = "TRUNCATE " + sTablename;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1553200423] - could not delete GL transactions using SQL '" + SQL + "' - " + e.getMessage());
		}
		
		/* GLPOST field names:
		[ACCTID]
      ,[FISCALYR]
      ,[FISCALPERD]
      ,[SRCECURN]
      ,[SRCELEDGER]
      ,[SRCETYPE]
      ,[POSTINGSEQ]
      ,[CNTDETAIL]
      ,[AUDTDATE]
      ,[AUDTTIME]
      ,[AUDTUSER]
      ,[AUDTORG]
      ,[JRNLDATE]
      ,[BATCHNBR]
      ,[ENTRYNBR]
      ,[TRANSNBR]
      ,[EDITALLOWD]
      ,[CONSOLIDAT]
      ,[COMPANYID]
      ,[JNLDTLDESC]
      ,[JNLDTLREF]
      ,[TRANSAMT]
      ,[TRANSQTY]
      ,[SCURNDEC]
      ,[SCURNAMT]
      ,[HCURNCODE]
      ,[RATETYPE]
      ,[SCURNCODE]
      ,[RATEDATE]
      ,[CONVRATE]
      ,[RATESPREAD]
      ,[DATEMTCHCD]
      ,[RATEOPER]
      ,[DRILSRCTY]
      ,[DRILLDWNLK]
      ,[DRILAPP]
      ,[RPTAMT]
      ,[VALUES]
      ,[DOCDATE]
		 */
		
		//Get the count of GL transactions from ACCPAC:
		long lNumberOfACCPACGLTransactions = 0L;
		
		SQL = "SELECT COUNT(*) AS TRANSACTIONCOUNT FROM GLPOST";
		try {
			Statement stmtACCPAC = cnACCPAC.createStatement();
			ResultSet rsTransactionsCount = stmtACCPAC.executeQuery(SQL);
			if (rsTransactionsCount.next()){
				lNumberOfACCPACGLTransactions = rsTransactionsCount.getLong("TRANSACTIONCOUNT");
			}else{
				rsTransactionsCount.close();
				throw new Exception("Error [1553377271] - no record returned when counting ACCPAC GL transactions.");
			}
			rsTransactionsCount.close();
		} catch (Exception e1) {
			throw new Exception("Error [1553377272] - counting ACCPAC GL transactions - " + e1.getMessage());
		}
		
		//System.out.println("[1553458951] - ACCPAC GL Transaction count = " + Long.toString(lNumberOfACCPACGLTransactions) + ".");
		
		//Now get the highest batchnumber from the table:
		long lHighestBatchNumber = 0L;
		ResultSet rsBatchNumbers;
		try {
			SQL = "SELECT BATCHNBR FROM GLPOST ORDER BY BATCHNBR DESC";
			Statement stmtBatchNumbers = cnACCPAC.createStatement();
			rsBatchNumbers = stmtBatchNumbers.executeQuery(SQL);
			if (rsBatchNumbers.next()){
				lHighestBatchNumber = rsBatchNumbers.getLong("BATCHNBR");
			}
			rsBatchNumbers.close();
		} catch (Exception e1) {
			throw new Exception("Error [1523042193] - reading highest batch nuber from ACCPAC - " + e1.getMessage());
		}
		
		
		ResultSet rsPostedTransactions;
		try {
			SQL = "SELECT * FROM GLPOST";
			Statement stmtACCPAC = cnACCPAC.createStatement();
			rsPostedTransactions = stmtACCPAC.executeQuery(SQL);
		} catch (Exception e1) {
			throw new Exception("Error [1523042093] - reading ACCPAC posted GL transactions - " + e1.getMessage());
		}
		long lInsertCounter = 0L;
		//long lStartingTime = System.currentTimeMillis();
		int iNumberOfQueriesPerInsert = 100;
		String SQLInsert = "";
		//System.out.println("[1553458952] - going into while loop.");
		
		//We turn this off to get faster inserts:
		Statement stmtCommit;
		try {
			stmtCommit = cnSMCP.createStatement();
			stmtCommit.execute("SET autocommit=0");
		} catch (Exception e1) {
			throw new Exception("Error [20192971431453] " + "setting AUTOCOMMIT to ZERO to insert gltransactionlines - " + e1.getMessage());
		}
		while (rsPostedTransactions.next()){
			//The batch, entry, and line number combination must be unique - so we need to
			// make sure that each record gets a unique combination, just in case some
			// of the ACCPAC records come in with ZEROES for these numbers:
			
			//We'll make the batch numbers negative so they'll never conflict with any new records as
			// users add new batches in the future;
			long lACCPACBatchNumber = rsPostedTransactions.getLong("BATCHNBR") * -1;
			
			if (lACCPACBatchNumber == 0L){
				lHighestBatchNumber++;
				lACCPACBatchNumber = lHighestBatchNumber * -1;
			}
			if (lInsertCounter == 0){
				SQLInsert = "INSERT INTO " + sTablename + "("
					+ SMTablegltransactionlines.bdamount
					+ ", " + SMTablegltransactionlines.datpostingdate
					+ ", " + SMTablegltransactionlines.dattransactiondate
					+ ", " + SMTablegltransactionlines.iconsolidatedposting
					+ ", " + SMTablegltransactionlines.ifiscalperiod
					+ ", " + SMTablegltransactionlines.ifiscalyear
					+ ", " + SMTablegltransactionlines.loriginalbatchnumber
					+ ", " + SMTablegltransactionlines.loriginalentrynumber
					+ ", " + SMTablegltransactionlines.loriginallinenumber
					+ ", " + SMTablegltransactionlines.sSourceledgertransactionlink
					+ ", " + SMTablegltransactionlines.sacctid
					+ ", " + SMTablegltransactionlines.sdescription
					+ ", " + SMTablegltransactionlines.sreference
					+ ", " + SMTablegltransactionlines.ssourceledger
					+ ", " + SMTablegltransactionlines.ssourcetype
					+ ", " + SMTablegltransactionlines.stransactiontype
					+ ") VALUES ";
			}
			if (lInsertCounter > 0){
				SQLInsert += ", ";
			}
		
			SQLInsert += "("
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablegltransactionlines.bdamountScale, rsPostedTransactions.getBigDecimal("TRANSAMT")).replace(",", "") //bdamount
			+ ", '" + convertACCPACLongDateToString(rsPostedTransactions.getLong("JRNLDATE"), false) + "'" //datpostingdate
			+ ", '" + convertACCPACLongDateToString(rsPostedTransactions.getLong("DOCDATE"), false) + "'" //dattransactiondate
			+ ", " + Integer.toString(rsPostedTransactions.getInt("CONSOLIDAT")) //consolidated posting
			+ ", " + Integer.toString(rsPostedTransactions.getInt("FISCALPERD")) //fiscal period
			+ ", " + Integer.toString(rsPostedTransactions.getInt("FISCALYR")) //fiscal year
			+ ", " + Long.toString(lACCPACBatchNumber) //original batch number
			+ ", " + Long.toString(rsPostedTransactions.getLong("ENTRYNBR")) //original entry number
			+ ", " + Long.toString(rsPostedTransactions.getLong("TRANSNBR")) //original line number
			+ ", 0" //source transaction link
			+ ", '" + FormatSQLStatement(rsPostedTransactions.getString("ACCTID").trim()) + "'" //sacctid
			+ ", '" + FormatSQLStatement(rsPostedTransactions.getString("JNLDTLDESC").trim()) + "'" //sdescription
			+ ", '" + FormatSQLStatement(rsPostedTransactions.getString("JNLDTLREF").trim()) + "'" //reference
			+ ", '" + FormatSQLStatement(rsPostedTransactions.getString("SRCELEDGER").trim()) + "'" //source ledger
			+ ", '" + FormatSQLStatement(rsPostedTransactions.getString("SRCETYPE").trim()) + "'" //source type
			+ ", 0" //stransactiontype
			+ ")"
			;
			lInsertCounter++;

			if (lInsertCounter >= iNumberOfQueriesPerInsert){
				try {
					Statement stmtInsert = cnSMCP.createStatement();
					stmtInsert.execute(SQLInsert);
				} catch (Exception e) {
					rsPostedTransactions.close();
					try {
						stmtCommit.execute("COMMIT");
					} catch (Exception e1) {
						throw new Exception("Error [20192971442321] " + "committing GL transaction Inserts - " + e1.getMessage());
					}
					throw new Exception("Error [1523051993] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
				}
				lInsertCounter = 0;
			}
			
//			if ((lCounter % 1000L) == 0){
//				System.out.println("[1553459657] - at " + lCounter + " inserts, at " + iNumberOfQueriesPerInsert 
//					+ " queries per insert, inserts are taking " + (System.currentTimeMillis() - lStartingTime) + "ms per 1000.");
//				lStartingTime = System.currentTimeMillis();
//			}
			
		}
		rsPostedTransactions.close();
		
		//Now we insert any of the remaining transaction records from ACCPAC:
		try {
			Statement stmtInsert = cnSMCP.createStatement();
			stmtInsert.execute(SQLInsert);
		} catch (Exception e) {
			rsPostedTransactions.close();
			try {
				stmtCommit.execute("COMMIT");
			} catch (Exception e1) {
				throw new Exception("Error [20192971432921] " + "committing GL transaction Inserts - " + e1.getMessage());
			}
			throw new Exception("Error [1523043993] - could not insert remaining transactions into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
		}
		
		//System.out.println("[2019297133028] " + "Inserts took " + ((System.currentTimeMillis() - lStartingTime) / 1000) + " seconds.");
		try {
			stmtCommit.execute("COMMIT");
		} catch (Exception e) {
			throw new Exception("Error [20192971432321] " + "committing GL transaction Inserts - " + e.getMessage());
		}
		//System.out.println("[2019297133029] " + "Including COMMIT, inserts took " + ((System.currentTimeMillis() - lStartingTime) / 1000) + " seconds.");

		sStatus +=  "<BR>ACCPAC has " + Long.toString(lNumberOfACCPACGLTransactions) 
			+ " posted GL transactions, all added to " + sTablename + " in SMCP.<BR>";
		
		return sStatus;
	}
	public String processGLFiscalCalendar(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser,
			String sUserID,
			String sUserFullName
			) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL fiscal periods that may have been added by a previous conversion:
		String sTablename = SMTableglfiscalperiods.TableName;
		String SQL = "DELETE FROM " + SMTableglfiscalperiods.TableName
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1530809394] - could not delete GL fiscal periods using SQL '" + SQL + "' - " + e.getMessage());
		}
		
		//Now read the ACCPAC fiscal periods into SMCP:
		SQL = "SELECT * FROM CSFSC"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsGLFiscalPeriods = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		
		//Get the current year: we're going to set all the years to 'closed' except the current one.
		//ACCPAC doesn't really flag a year as 'closed' but we need to.
		Calendar calendar = Calendar.getInstance();
		String sCurrentYear = Integer.toString(calendar.get(Calendar.YEAR));
		String sFiscalYearClosed = "1";
		while (rsGLFiscalPeriods.next()){
			if (sCurrentYear.compareToIgnoreCase(rsGLFiscalPeriods.getString("FSCYEAR").trim()) == 0){
				sFiscalYearClosed = "0";
			}else{
				sFiscalYearClosed = "1";
			}
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglfiscalperiods.datbeginningdateperiod1
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod2
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod3
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod4
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod5
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod6
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod7
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod8
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod9
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod10
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod11
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod12
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod13
				+ ", " + SMTableglfiscalperiods.datendingdateperiod1
				+ ", " + SMTableglfiscalperiods.datendingdateperiod2
				+ ", " + SMTableglfiscalperiods.datendingdateperiod3
				+ ", " + SMTableglfiscalperiods.datendingdateperiod4
				+ ", " + SMTableglfiscalperiods.datendingdateperiod5
				+ ", " + SMTableglfiscalperiods.datendingdateperiod6
				+ ", " + SMTableglfiscalperiods.datendingdateperiod7
				+ ", " + SMTableglfiscalperiods.datendingdateperiod8
				+ ", " + SMTableglfiscalperiods.datendingdateperiod9
				+ ", " + SMTableglfiscalperiods.datendingdateperiod10
				+ ", " + SMTableglfiscalperiods.datendingdateperiod11
				+ ", " + SMTableglfiscalperiods.datendingdateperiod12
				+ ", " + SMTableglfiscalperiods.datendingdateperiod13
				+ ", " + SMTableglfiscalperiods.iactive
				+ ", " + SMTableglfiscalperiods.ifiscalyear
				+ ", " + SMTableglfiscalperiods.ilasteditedbyuserid
				+ ", " + SMTableglfiscalperiods.iclosed
				//+ ", " + SMTableglfiscalperiods.ilockadjustmentperiod
				+ ", " + SMTableglfiscalperiods.inumberofperiods
				+ ", " + SMTableglfiscalperiods.slasteditedbyfullusername
				+ ", " + SMTableglfiscalperiods.datlastediteddateandtime
				+ ") VALUES("
				+ " '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE1"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE2"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE3"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE4"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE5"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE6"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE7"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE8"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE9"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE10"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE11"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE12"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("BGNDATE13"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE1"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE2"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE3"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE4"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE5"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE6"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE7"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE8"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE9"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE10"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE11"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE12"), false) + "'"
				+ ", '" + convertACCPACLongDateToString(rsGLFiscalPeriods.getLong("ENDDATE13"), false) + "'"
				+ ", " + Long.toString(rsGLFiscalPeriods.getLong("ACTIVE"))
				+ ", " + rsGLFiscalPeriods.getString("FSCYEAR")
				+ ", " + sUserID
				+ ", " + sFiscalYearClosed
				//+ ", " + Long.toString(rsGLFiscalPeriods.getLong("STATUSADJ"))
				+ ", " + Long.toString(rsGLFiscalPeriods.getLong("PERIODS"))
				+ ", '" + sUserFullName + "'"
				//+ ", DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%S')"
				+ ", NOW()"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalPeriods.close();
				throw new Exception("Error [1530810942] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsGLFiscalPeriods.close();
		
		//If there are any periods in any year PAST the specified number of periods (such as a period 13 in a year with 12 periods), then
		// Set the unused period dates to '00/00/0000':
		String SQLUpdate = "UPDATE " + SMTableglfiscalperiods.TableName
			+ " SET " + SMTableglfiscalperiods.datbeginningdateperiod13 + " = '0000-00-00'"
			+ ", " + SMTableglfiscalperiods.datendingdateperiod13 + " = '0000-00-00'"
			+ ", " + SMTableglfiscalperiods.iperiod13locked + " = 1"
			+ " WHERE ("
				+ "(" + SMTableglfiscalperiods.inumberofperiods + " = 12)"
			+ ")"
		;
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQLUpdate);
		} catch (Exception e) {
			rsGLFiscalPeriods.close();
			throw new Exception("Error [1530811942] - could not update unused period dates in " + sTablename + " table with SQL '" + SQLUpdate + "' - " + e.getMessage());
		}

		// TJR - 12/4/2019 - This was added to prevent the unsolved problem of the Harrisburg glfiscalperiods appearing to
		// be corrupt after the ACCPAC conversion.  It creates a brand new table after the fiscal periods
		//are populated.
		SQL = "DROP TABLE IF EXISTS glfiscalperiods_bak";
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20193381620527] " + "deleting temporary table with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "RENAME TABLE " + SMTableglfiscalperiods.TableName + " TO glfiscalperiods_bak";
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20193381620528] " + "running SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "CREATE TABLE " + SMTableglfiscalperiods.TableName + " LIKE glfiscalperiods_bak";
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20193381620529] " + "running SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "INSERT INTO " + SMTableglfiscalperiods.TableName + " SELECT * FROM glfiscalperiods_bak";
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20193381620530] " + "running SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "DROP TABLE IF EXISTS glfiscalperiods_bak";
		try {
			Statement stmtUpdate = cnSMCP.createStatement();
			stmtUpdate.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20193381620531] " + "running SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		sStatus +=  "<BR>Inserted " + Integer.toString(iCounter) + " GL fiscal period records into " + sTablename + "<BR>";
		
		return sStatus;
		
	}
	
	public String processGLAccountGroups(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL account groups that may have been added by a previous conversion:
		String sTablename = SMTableglaccountgroups.TableName;
		String SQL = "TRUNCATE " + SMTableglaccountgroups.TableName
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528309612] - could not delete GL account groups using SQL '" + SQL + "' - " + e.getMessage());
		}
		
		SQL = "SELECT * FROM GLACGRP"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsGLAccountGroups = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsGLAccountGroups.next()){
			String SQLInsert = "INSERT INTO " + sTablename + "("
				+ SMTableglaccountgroups.sdescription
				+ ", " + SMTableglaccountgroups.sgroupcode
				+ ", " + SMTableglaccountgroups.ssortcode
				+ ") VALUES("
				+ " '" + FormatSQLStatement(rsGLAccountGroups.getString("ACCTGRPDES").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsGLAccountGroups.getString("ACCTGRPCOD").trim()) + "'"
				+ ", '" + FormatSQLStatement(rsGLAccountGroups.getString("SORTCODE").trim()) + "'"
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLAccountGroups.close();
				throw new Exception("Error [1528310168] - could not insert into " + sTablename + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
		}
		rsGLAccountGroups.close();

		sStatus +=  "<BR>Inserted " + Integer.toString(iCounter) + " GL account group records into " + sTablename + "<BR>";
		
		return sStatus;
		
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
