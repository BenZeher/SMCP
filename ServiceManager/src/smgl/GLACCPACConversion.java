package smgl;

import java.math.BigDecimal;
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
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class GLACCPACConversion  extends java.lang.Object{

	
	public GLACCPACConversion(){
		
	}
	
	public String reverseDataChanges(Connection cnSMCP, boolean bRemoveAllReminders) throws Exception{
		String s = "Rolling back critical changes to SMCP data...<BR>";
		//We only remove CRITICAL data changes, i.e., those that might affect processing:

		System.out.println("[1552318880] - starting reverseDataChanges.");
		
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
		
		System.out.println("[1552318881] - removed segments.");
		
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
		
		System.out.println("[1552318882] - removed segment values.");
		
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
		
		System.out.println("[1552318883] - removed account structures.");
		
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
		
		System.out.println("[1552318884] - removed accounts created from ACCPAC.");
		
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
		
		System.out.println("[1552318885] - removed fiscal sets.");
		
		//Remove any segment values that we added from ACCPAC:
		SQL = "TRUNCATE " + SMTableglaccountgroups.TableName
		;
		try {
			Statement stmt = cnSMCP.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528310248] - could not remove GL account groups - " + e.getMessage());
		}
		s+= "GL Account Groups that were added from ACCPAC have been removed.<BR>";
		
		System.out.println("[1552318886] - removed account groups.");
		
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
		
		System.out.println("[1552318887] - removed fiscal periods.");
		
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
			 
			 + " FROM [comp1].[dbo].[GLABRX]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT1\" ON [GLABRX].[ABRKID1]=SEGMENT1.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT2\" ON [GLABRX].[ABRKID2]=SEGMENT2.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT3\" ON [GLABRX].[ABRKID3]=SEGMENT3.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT4\" ON [GLABRX].[ABRKID4]=SEGMENT4.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT5\" ON [GLABRX].[ABRKID5]=SEGMENT5.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT6\" ON [GLABRX].[ABRKID6]=SEGMENT6.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT7\" ON [GLABRX].[ABRKID7]=SEGMENT7.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT8\" ON [GLABRX].[ABRKID8]=SEGMENT8.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT9\" ON [GLABRX].[ABRKID9]=SEGMENT9.[ACCTBLKID]"
			 + " LEFT JOIN [comp1].[dbo].[GLABK] AS \"SEGMENT10\" ON [GLABRX].[ABRKID10]=SEGMENT10.[ACCTBLKID]"
			 + " ORDER BY [GLABRX].[ACCTBRKID]"
		;
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

			System.out.println("[1524253546] - SQL = " + SQLInsert);
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
	
	public String processGLFiscalSets(
			Connection cnSMCP, 
			Connection cnACCPAC, 
			int iAPDatabaseType, 
			String sUser) throws Exception{
		
		String sStatus = "";
		
		//First delete any GL financial statement data that may have been added by a previous conversion:
		String sTablename = SMTableglfiscalsets.TableName;
		String SQL = "DELETE FROM " + SMTableglfiscalsets.TableName
		;
		try {
			Statement stmtDelete = cnSMCP.createStatement();
			stmtDelete.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1528221318] - could not delete GL financial statement data using SQL '" + SQL + "' - " + e.getMessage());
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

		sStatus +=  "<BR>Inserted " + Integer.toString(iCounter) + " GL financial statement records into " + sTablename + ".<BR>";
		
		//Now update the GL fiscalstatementdata table:
		SQL = "SELECT * from " + SMTableglfiscalsets.TableName
			+ " ORDER BY " + SMTableglfiscalsets.ifiscalyear + " DESC";
		ResultSet rsFiscalSets = clsDatabaseFunctions.openResultSet(SQL, cnSMCP);
		String SQLInsert = "";
		String sPreviousYearSQL = "";
		long lLastFiscalYear = 0L;
		//String sLastFiscalPeriodDateOfPreviousYear = "0000-00-00";
		//String sLastFiscalPeriodDateOfTwoYearsPrevious = "0000-00-00";
		int iLastPeriodOfPreviousYear = 0;
		int iLastPeriodOfTwoYearsPrevious = 0;
		while(rsFiscalSets.next()){
			
			//IF we've moved to a new fiscal year, then we have to determine the LAST period of the two previous fiscal years:
			if (rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear) != lLastFiscalYear){
				//We need to determine the LAST PERIOD of the two previous years:
				String SQLFiscalPeriods = "SELECT * FROM " + SMTableglfiscalperiods.TableName
					+ " WHERE ("
						+ "(" + SMTableglfiscalperiods.ifiscalyear + " = " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear) - 1L) + ")"
						+ " OR (" + SMTableglfiscalperiods.ifiscalyear + " = " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear) - 2L) + ")"
					+ ")"
					+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear + " DESC"
				;
				ResultSet rsRecentFiscalPeriods = clsDatabaseFunctions.openResultSet(SQLFiscalPeriods, cnSMCP);

				if (rsRecentFiscalPeriods.next()){
					//This is the PREVIOUS fiscal year:
					iLastPeriodOfPreviousYear = rsRecentFiscalPeriods.getInt(SMTableglfiscalperiods.inumberofperiods);
				}
				
				if (rsRecentFiscalPeriods.next()){
					//This is the fiscal year TWO YEARS PREVIOUS:
					iLastPeriodOfTwoYearsPrevious = rsRecentFiscalPeriods.getInt(SMTableglfiscalperiods.inumberofperiods);
				}
			}
			
			//Get the previous year's fiscal set, if there is one:
			sPreviousYearSQL = "SELECT * from " + SMTableglfiscalsets.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear) - 1) + ")"
				+ ")"
			;
			ResultSet rsPreviousYearFiscalSet = clsDatabaseFunctions.openResultSet(sPreviousYearSQL, cnSMCP);
			boolean bPreviousYearWasFound = false;
			if(rsPreviousYearFiscalSet.next()){
				bPreviousYearWasFound = true;
			}
			
			//Get the fiscal set from TWO YEARS PREVIOUS, if there is one:
			sPreviousYearSQL = "SELECT * from " + SMTableglfiscalsets.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear) - 2) + ")"
				+ ")"
			;
			ResultSet rsTwoYearsPreviousFiscalSet = clsDatabaseFunctions.openResultSet(sPreviousYearSQL, cnSMCP);
			boolean bTwoYearsPreviousWasFound = false;
			if(rsTwoYearsPreviousFiscalSet.next()){
				bTwoYearsPreviousWasFound = true;
			}

			//PERIOD 1:
			String sPeriod = "1";
			BigDecimal bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod1);
			BigDecimal bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod1);
			}
					
			BigDecimal bdNetChangeForPreviousPeriod = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				// Here we use the value we found above for the last period of the previous year:
				if (iLastPeriodOfPreviousYear == 13){
					bdNetChangeForPreviousPeriod = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13);
				}
				if (iLastPeriodOfPreviousYear == 12){
					bdNetChangeForPreviousPeriod = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);
				}
			}
			
			BigDecimal bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bTwoYearsPreviousWasFound){
				// Here we use the value we found above for the last period of the year two years previous:
				if (iLastPeriodOfTwoYearsPrevious == 13){
					bdNetChangeForPreviousPeriodPreviousYear = rsTwoYearsPreviousFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13);
				}
				if (iLastPeriodOfTwoYearsPrevious == 12){
					bdNetChangeForPreviousPeriodPreviousYear = rsTwoYearsPreviousFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);
				}
			}
				
			BigDecimal bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			BigDecimal bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			BigDecimal bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = new BigDecimal("0.00");
			}
			BigDecimal bdTotalYearToDate = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod1);

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				throw new Exception("Error [1552579530] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 2:
			sPeriod = "2";
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2);
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2);
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod1);
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod1);
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2));
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2));

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				throw new Exception("Error [1552579531] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 3:
			sPeriod = "3";
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3);
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3);
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2);
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod2);
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3));
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3));

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				throw new Exception("Error [1552579532] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 4:	// !CHANGE!
			sPeriod = "4"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod3);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579533] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 5:	// !CHANGE!
			sPeriod = "5"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod4);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579534] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 6:	// !CHANGE!
			sPeriod = "6"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod5);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579536] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 7:	// !CHANGE!
			sPeriod = "7"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod6);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579537] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 8:	// !CHANGE!
			sPeriod = "8"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod7);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579538] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 9:	// !CHANGE!
			sPeriod = "9"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod8);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579539] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}	
			
			//PERIOD 10:	// !CHANGE!
			sPeriod = "10"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod9);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579540] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 11:	// !CHANGE!
			sPeriod = "11"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod10);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579541] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 12:	// !CHANGE!
			sPeriod = "12"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod11);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12));  // !CHANGE!

			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579542] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			//PERIOD 13:	// !CHANGE!
			sPeriod = "13"; // !CHANGE!
			
			bdNetChangeForPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13);  // !CHANGE!
			bdNetChangeForPeriodPreviousYear = new BigDecimal(0);
			if (bPreviousYearWasFound){
				bdNetChangeForPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13);  // !CHANGE!
			}
			bdNetChangeForPreviousPeriod = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);  // !CHANGE!
			
			bdNetChangeForPreviousPeriodPreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdNetChangeForPreviousPeriodPreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod12);  // !CHANGE!
			}
			bdOpeningBalance = rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			bdOpeningBalancePreviousYear = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdOpeningBalancePreviousYear = rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdopeningbalance);
			}
			bdTotalPreviousYearToDate = new BigDecimal("0.00");
			if (bPreviousYearWasFound){
				bdTotalPreviousYearToDate = bdTotalPreviousYearToDate.add(rsPreviousYearFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13));  // !CHANGE!
			}
			bdTotalYearToDate = bdTotalYearToDate.add(rsFiscalSets.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod13));  // !CHANGE!
			
			SQLInsert = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
				+ "("
				+ SMTableglfinancialstatementdata.sacctid
				+ ", " + SMTableglfinancialstatementdata.ifiscalyear
				+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate
				+ ") VALUES ("
				+ "'" + rsFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
				+ ", " + Long.toString(rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear))
				+ ", " + sPeriod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriod) // bdnetchangeforperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPeriodPreviousYear) //bdnetchangeforperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriod) //bdnetchangeforpreviousperiod
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetChangeForPreviousPeriodPreviousYear) //bdnetchangeforpreviousperiodpreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalance) // bdopeningbalance
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdOpeningBalancePreviousYear) //bdopeningbalancepreviousyear
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalPreviousYearToDate) // bdtotalpreviousyeartodate
				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalYearToDate) // bdtotalyeartodate
				+ ")"
			;
			try {
				Statement stmtInsert = cnSMCP.createStatement();
				stmtInsert.execute(SQLInsert);
				iCounter++;
			} catch (Exception e) {
				rsGLFiscalSets.close();
				 // !CHANGE! below
				throw new Exception("Error [1552579543] - could not insert Period " + sPeriod + " into " + SMTableglfinancialstatementdata.TableName + " table with SQL '" + SQLInsert + "' - " + e.getMessage());
			}
			
			rsPreviousYearFiscalSet.close();
			
			//Remember the fiscal year:
			lLastFiscalYear =  rsFiscalSets.getLong(SMTableglfiscalsets.ifiscalyear);
			
		}
		rsFiscalSets.close();
		sStatus += "  Fiscal sets converted to financial statement data.<BR>";
		
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
		
		//Now read the ACCPAC fisal periods into SMCP:
		SQL = "SELECT * FROM CSFSC"
		;
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsGLFiscalPeriods = stmtACCPAC.executeQuery(SQL);
		int iCounter = 0;
		while (rsGLFiscalPeriods.next()){
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
				+ ", " + SMTableglfiscalperiods.ilockclosingperiod
				+ ", " + SMTableglfiscalperiods.ilockadjustmentperiod
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
				+ ", " + Long.toString(rsGLFiscalPeriods.getLong("STATUSCLS"))
				+ ", " + Long.toString(rsGLFiscalPeriods.getLong("STATUSADJ"))
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
