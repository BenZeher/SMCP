package smfa;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTablefatransactions;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;

public class FAClearTransactionHistory extends java.lang.Object{

	public static SimpleDateFormat sdfTime = new SimpleDateFormat("hhmmss");
	public static SimpleDateFormat sdfNormalDate = new SimpleDateFormat("MM/dd/yyyy");
	private String m_sFiscalYear = "";
	private String m_sFiscalPeriod = "";
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String>(0);
    private boolean bDebugMode = false;
	public FAClearTransactionHistory(){
		m_sFiscalYear = "";
		m_sFiscalPeriod = "";
	}

	public void doProcess(String sUserID,
					 	  Connection conn
					 	  )throws Exception{
		
		if (m_sFiscalYear.trim().compareTo("") == 0){
			m_sErrorMessageArray.add("You must select a valid fiscal year.");
		}
		if (m_sFiscalPeriod.trim().compareTo("") == 0){
			m_sErrorMessageArray.add("You must select a valid fiscal period.");
		}
		if (m_sErrorMessageArray.size() > 0){
			return;
		}
	
		//Begin a transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Could not start data transaction.");
		}
		
	    String sSQL = "DELETE FROM " + SMTablefatransactions.TableName +
	    				" WHERE" +
	    					" " + SMTablefatransactions.iFiscalYear + "<" + m_sFiscalYear +
	    					" OR " +
	    						"(" + SMTablefatransactions.iFiscalYear + "=" + m_sFiscalYear + 
	    						" AND " +
	    						" " + SMTablefatransactions.iFiscalPeriod + "<=" + m_sFiscalPeriod +
	    						")";
	    try{
	    	//System.out.println("SQL = " + sSQL);
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException e){
	    	m_sErrorMessageArray.add("Error removing transaction history records with SQL: " 
	    			+ sSQL + "<BR>" +
		    						 e.getMessage() + "<BR>" +
		    						 e.getSQLState());
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	return;
	    }
	    
    	//Delete any GL export header or detail records for these batches:
	    sSQL = "DELETE FROM " + SMTableglexportheaders.TableName
	    + " WHERE ("
	    	+ "(" + SMTableglexportheaders.lbatchnumber + " <= " + Long.toString((Long.parseLong(m_sFiscalYear) * 100) 
	    			+ Long.parseLong(m_sFiscalPeriod)) + ")"
	    	+ " AND (" + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.FA + "')"
	    + ")"
	    ;
	    
	    try{
	    	if (bDebugMode){
	    		System.out.println("In " + this.toString() + " [1349376740] DELETE GL header SQL = " + sSQL);
	    	}
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException e){
	    	m_sErrorMessageArray.add("Error removing GL export history records with SQL: " 
	    			+ sSQL + "<BR>" +
		    						 e.getMessage() + "<BR>" +
		    						 e.getSQLState());
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	return;
	    }
	    
	    sSQL = "DELETE FROM " + SMTableglexportdetails.TableName
	    + " WHERE ("
	    	+ "(" + SMTableglexportdetails.lbatchnumber + " <= " + Long.toString((Long.parseLong(m_sFiscalYear) * 100) 
	    			+ Long.parseLong(m_sFiscalPeriod)) + ")"
	    	+ " AND (" + SMTableglexportdetails.sdetailsourceledger + " = '" + SMModuleTypes.FA + "')"
	    + ")"
	    ;
	    
	    try{
	    	if (bDebugMode){
	    		System.out.println("In " + this.toString() + " [1349376741] DELETE GL Detail SQL = " + sSQL);
	    	}
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException e){
	    	m_sErrorMessageArray.add("Error removing GL export history records with SQL: " 
	    			+ sSQL + "<BR>" +
		    						 e.getMessage() + "<BR>" +
		    						 e.getSQLState());
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	return;
	    }
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	m_sErrorMessageArray.add("Error committing data transaction:");
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	return;
	    }
	    
	    //Record the clearing in the system log:
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID,
	    	SMLogEntry.LOG_OPERATION_FIXEDASSETSTRANSACTIONCLEARING, 
	    	"Cleared FA transactions up to Fiscal Year '" + m_sFiscalYear + "', period '" + m_sFiscalPeriod + "'" , 
	    	"", 
	    	"[1490029098]"
	    );
	    
	    return;
	}
	
	public void setFiscalYear(String s){
		m_sFiscalYear = s;
	}

	public String getFiscalYear(){
		return m_sFiscalYear;
	}
	
	public void setFiscalPeriod(String s){
		m_sFiscalPeriod = s;
	}

	public String getFiscalPeriod(){
		return m_sFiscalPeriod;
	}
	
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += "FISCALYEAR=" + clsServletUtilities.URLEncode(m_sFiscalYear);
		sQueryString += "&FISCALPERIOD=" + clsServletUtilities.URLEncode(m_sFiscalPeriod);
				
		return sQueryString;
	}
	
}
