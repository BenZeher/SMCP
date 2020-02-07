package smcontrolpanel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablepricelistlevellabels;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class SMPriceLevelLabels extends java.lang.Object{

	public static final String ParamObjectName = "Price Level Labels";
	
	private String m_sbasepricelabel;
	private String m_slevel1label;
	private String m_slevel2label;
	private String m_slevel3label;
	private String m_slevel4label;
	private String m_slevel5label;

	public SMPriceLevelLabels(
        ) {
		m_sbasepricelabel = "";
		m_slevel1label = "";
		m_slevel2label = "";
		m_slevel3label = "";
		m_slevel4label = "";
		m_slevel5label = "";
    }
    public SMPriceLevelLabels(HttpServletRequest req) {
		m_sbasepricelabel = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.sbasepricelabel, req).trim();
		m_slevel1label = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.spricelevel1label, req).trim();
		m_slevel2label = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.spricelevel2label, req).trim();
		m_slevel3label = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.spricelevel3label, req).trim();
		m_slevel4label = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.spricelevel4label, req).trim();
		m_slevel5label = clsManageRequestParameters.get_Request_Parameter(SMTablepricelistlevellabels.spricelevel5label, req).trim();
    }
    public void load(String sDBName, ServletContext context, String sUser) throws Exception{
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBName, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1580848621] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1580848622]");
    }

	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + SMTablepricelistlevellabels.TableName
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_sbasepricelabel = rs.getString(SMTablepricelistlevellabels.sbasepricelabel);
				m_slevel1label = rs.getString(SMTablepricelistlevellabels.spricelevel1label);
				m_slevel2label = rs.getString(SMTablepricelistlevellabels.spricelevel2label);
				m_slevel3label = rs.getString(SMTablepricelistlevellabels.spricelevel3label);
				m_slevel4label = rs.getString(SMTablepricelistlevellabels.spricelevel4label);
				m_slevel5label = rs.getString(SMTablepricelistlevellabels.spricelevel5label);
			}else{
				throw new Exception("Error [1580848623] - no record found for price level labels.");
			}
			rs.close();
		}catch (Exception ex){
			throw new Exception("Error [1580848624] loading price level labels using SQL: " + SQL + " - " + ex.getMessage());
		}
	}
    public void save(ServletContext context, String sDBIB, String sUserName) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBIB, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1580848625] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1580848626]");
			throw new Exception(e1.getMessage());
		}
		//Update the editable fields.
		String SQL = "UPDATE " + SMTablepricelistlevellabels.TableName 
			+ " SET " +  SMTablepricelistlevellabels.sbasepricelabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sbaselabel()) + "'"
			+ ", " + SMTablepricelistlevellabels.spricelevel1label + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slevel1label()) + "'"
			+ ", " + SMTablepricelistlevellabels.spricelevel2label + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slevel2label()) + "'"
			+ ", " + SMTablepricelistlevellabels.spricelevel3label + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slevel3label()) + "'"
			+ ", " + SMTablepricelistlevellabels.spricelevel4label + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slevel4label()) + "'"
			+ ", " + SMTablepricelistlevellabels.spricelevel5label + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slevel5label()) + "'"
		;
		//System.out.println("[2020351626326] " + "SQL = '" + SQL + "'");
	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1580848627]");
	 		throw new Exception("Error [1580848628] saving " + ParamObjectName + " record - " + e.getMessage());
	 	}

	 	clsDatabaseFunctions.freeConnection(context, conn, "[1580848629]");
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_sbasepricelabel = clsValidateFormFields.validateStringField(m_sbasepricelabel, SMTablepricelistlevellabels.sbasepricelabelLength, "Base level label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

    	try {
    		m_slevel1label = clsValidateFormFields.validateStringField(m_slevel1label, SMTablepricelistlevellabels.spricelevel1labelLength, "Level 1 label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	
    	try {
    		m_slevel2label = clsValidateFormFields.validateStringField(m_slevel2label, SMTablepricelistlevellabels.spricelevel2labelLength, "Level 2 label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	
    	try {
    		m_slevel3label = clsValidateFormFields.validateStringField(m_slevel3label, SMTablepricelistlevellabels.spricelevel3labelLength, "Level 3 label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	
    	try {
    		m_slevel4label = clsValidateFormFields.validateStringField(m_slevel4label, SMTablepricelistlevellabels.spricelevel4labelLength, "Level 4 label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	
    	try {
    		m_slevel5label = clsValidateFormFields.validateStringField(m_slevel5label, SMTablepricelistlevellabels.spricelevel5labelLength, "Level 5 label", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    	
    }

	public String get_sbaselabel() {
		return m_sbasepricelabel;
	}
	public String get_slevel1label() {
		return m_slevel1label;
	}
	public String get_slevel2label() {
		return m_slevel2label;
	}
	public String get_slevel3label() {
		return m_slevel3label;
	}
	public String get_slevel4label() {
		return m_slevel4label;
	}
	public String get_slevel5label() {
		return m_slevel5label;
	}
}