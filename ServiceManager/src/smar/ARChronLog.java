package smar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import javax.servlet.ServletContext;

import SMDataDefinition.SMTablearchronlog;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ARChronLog {

	private Connection conn;
	private ServletContext context;
	private String m_sDBID;
    public ARChronLog(Connection cn)
    {
    	conn = cn;
    }
    public ARChronLog(String sDBID, ServletContext cont)
    {
    	context = cont;
    	m_sDBID = sDBID;
    	
    }
    public void writeEntry (
    		BigDecimal bdAmount,
    		int iDocType,
    		long lOriginalBatchNumber,
    		long lOriginalEntryNumber,
    		long lDocID,
    		String sDescription,
    		String sDocNumber,
    		String sPayeePayor,
    		String sUserID,
    		String sUserFullName,
    		String sSQL,
    		String sApplyToDoc,
    		String sApplyFromDoc,
    		String sParentDoc
   		) throws Exception{
    
    		String SQL = "INSERT INTO " + SMTablearchronlog.TableName
    			+ " ("
	    			+ SMTablearchronlog.datlogdate
	    			+ ", " + SMTablearchronlog.damount
	    			+ ", " + SMTablearchronlog.idoctype
	    			+ ", " + SMTablearchronlog.ldocid
	    			+ ", " + SMTablearchronlog.loriginalbatchnumber
	    			+ ", " + SMTablearchronlog.loriginalentrynumber
	    			+ ", " + SMTablearchronlog.sdescription
	    			+ ", " + SMTablearchronlog.sdocnumber
	    			+ ", " + SMTablearchronlog.spayeepayor
	    			+ ", " + SMTablearchronlog.ssql
	    			+ ", " + SMTablearchronlog.luserid
	    			+ ", " + SMTablearchronlog.sapplyfromdoc
	    			+ ", " + SMTablearchronlog.sapplytodoc
	    			+ ", " + SMTablearchronlog.sparentdoc
	    			+ ", " + SMTablearchronlog.suserfullname
	    			
	    			+ ") SELECT"
    				+ " NOW()" //datlogdate
    				+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmount) //damount
    				+ ", " + Integer.toString(iDocType) //idoctype
    				+ ", " + Long.toString(lDocID) //ldocid
    				+ ", " + Long.toString(lOriginalBatchNumber) //loriginalbatchnumber
    				+ ", " + Long.toString(lOriginalEntryNumber) //loriginalentrynumber
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'" //sdescription
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDocNumber) + "'" //sdocnumber
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sPayeePayor) + "'" //spayeepayor
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sSQL) + "'" //ssql
    				+ ", " + sUserID + "" //luserid
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sApplyFromDoc) + "'" //sapplyfromdoc
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sApplyToDoc) + "'" //sapplytodoc
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sParentDoc) + "'" //sparentdoc
    				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'" //suserfullname
    			;
    		
    		if (conn != null){
    			try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (Exception e) {
					throw new Exception("Error [1387574611] adding archron entry - " + e.getMessage() + ".");
				}
    		}else{
        		try {
					if (!clsDatabaseFunctions.executeSQL(SQL, context, m_sDBID)){
						throw new Exception("Error [1387574612] adding archron entry.");
					}
				} catch (Exception e) {
					throw new Exception("Error [1387574613] adding archron entry - " + e.getMessage() + ".");
				}
    		}
    	return;
    }
}
