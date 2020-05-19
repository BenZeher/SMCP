package smgl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablegloptions;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLFinancialDataCheckAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLFinancialDataCheckSelect.SESSION_RESULTS_OBJECT);
			smaction.getCurrentSession().removeAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If these attributes aren't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLCheckFinancialData)){return;}
	    //Read the entry fields from the request object:
		String sStartingFiscalYear = request.getParameter(GLFinancialDataCheckSelect.PARAM_FISCAL_YEAR_SELECTION);
		String sGLAccount = request.getParameter(GLFinancialDataCheckSelect.PARAM_GL_ACCOUNTS);
	    String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sSelectedProcess = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLFinancialDataCheckSelect.RADIO_OPTIONS_GROUP, request);
	    
	    System.out.println("[2019331850338] " + "sSelectedProcess = '" + sSelectedProcess + "'");
	    
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	GLFinancialDataCheck dc = new GLFinancialDataCheck();
    	String sResults = "";
    	//System.out.println("[2019289938156] " + "sGLAccount = '" + sGLAccount + "'");
    	//System.out.println("[2019289938346] " + "sFiscalYear = '" + sFiscalYear + "'");
    	
    	long lStartingTimeInMS = System.currentTimeMillis();
    	
    	//If the user chose to check transactionlines against fiscal sets, branch here:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.CHECK_TRANSACTIONLINES_AGAINST_FISCAL_SETS) == 0){
        	try {
    			sResults = dc.checkFiscalSetsAgainstTransactions(sGLAccount, sStartingFiscalYear, conn, false);
    		} catch (Exception e) {

    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426618]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}	
    	}
    	
    	//If the user chose to check fiscal sets against financial statement data, branch here:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.CHECK_FISCALSETS_AGAINST_FINANCIALSTATEMENTDATA) == 0){
        	try {
    			sResults = dc.processFinancialRecords(
    				sGLAccount, 
    				sStartingFiscalYear, 
    				conn, 
    				false
    				);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426718]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}
    	}
    	
    	//If the user chose to check fiscal sets against ACCPAC data, branch here:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.CHECK_SMCPFISCALSETS_AGAINST_ACCPACFISCALSETS) == 0){
        	//If the user chose to check against ACCPAC, get an ACCPAC connection here:
        	Connection cnACCPAC = null;
            String sACCPACDatabaseURL = "";
            String sACCPACDatabasename = "";
            String sACCPACDatabaseuser = "";
            String sACCPACDatabasepw = "";
            int iACCPACDatabaseType = 0;
            
            String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
            try {
    			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
    					SQL, 
    					getServletContext(), 
    					sDBID, 
    					"MySQL", 
    					SMUtilities.getFullClassName(this.toString()) + ".doGet - user: " + sUserID
    					+ " - "
    					+ sUserFullName
    					);
    			if (rsOptions.first()){
    			    sACCPACDatabaseURL = rsOptions.getString(SMTablegloptions.saccpacdatabaseurl);
    			    sACCPACDatabasename = rsOptions.getString(SMTablegloptions.saccpacdatabasename);
    			    sACCPACDatabaseuser = rsOptions.getString(SMTablegloptions.saccpacdatabaseuser);
    			    sACCPACDatabasepw = rsOptions.getString(SMTablegloptions.saccpacdatabaseuserpw);
    			    iACCPACDatabaseType = rsOptions.getInt(SMTablegloptions.iaccpacdatabasetype);
    			}else{
    				rsOptions.close();
    				ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426615]");
    				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to open GL Options table - function cannot run.");
    				smaction.redirectAction(
    						"", 
    						"", 
    			    		""
    					);
    					return;    			}
    		} catch (SQLException e) {
    			//Redirect back to calling class:
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426616]");
				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to open GL Options table - function cannot run.");
				smaction.redirectAction(
						"", 
						"", 
			    		""
					);
					return;  
    		}
            
    		try {
    			cnACCPAC = getACCPACConnection(
    					iACCPACDatabaseType,
    					sACCPACDatabaseURL,
    					sACCPACDatabasename,
    					sACCPACDatabaseuser,
    					sACCPACDatabasepw);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426617]");
				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to get ACCPAC connection - " + e.getMessage());
				smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
    		}
    	try {
        		sResults = dc.checkFiscalSetsAgainstACCPACFiscalSets(
        			sGLAccount, 
        			sStartingFiscalYear, 
        			conn, 
        			cnACCPAC, 
        			getServletContext(), 
        			sDBID
        		);
    		} catch (Exception e) {
    			if (cnACCPAC != null){
    				try {
    					cnACCPAC.close();
    				} catch (SQLException e1) {
    					//Don't need to do anything here....
    				}
    			}
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426818]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}
			if (cnACCPAC != null){
				try {
					cnACCPAC.close();
				} catch (SQLException e1) {
					//Don't need to do anything here....
				}
			}
    	}
    	
    	//If the user chose to check the SMCP transactions against the ACCPAC transactions, branch here:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.CHECK_SMCPTRANSACTIONS_AGAINST_ACCPACTRANSACTIONS) == 0){
        	//If the user chose to check against ACCPAC, get an ACCPAC connection here:
        	Connection cnACCPAC = null;
            String sACCPACDatabaseURL = "";
            String sACCPACDatabasename = "";
            String sACCPACDatabaseuser = "";
            String sACCPACDatabasepw = "";
            int iACCPACDatabaseType = 0;
            
            String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
            try {
    			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
    					SQL, 
    					getServletContext(), 
    					sDBID, 
    					"MySQL", 
    					SMUtilities.getFullClassName(this.toString()) + ".doGet - user: " + sUserID
    					+ " - "
    					+ sUserFullName
    					);
    			if (rsOptions.first()){
    			    sACCPACDatabaseURL = rsOptions.getString(SMTablegloptions.saccpacdatabaseurl);
    			    sACCPACDatabasename = rsOptions.getString(SMTablegloptions.saccpacdatabasename);
    			    sACCPACDatabaseuser = rsOptions.getString(SMTablegloptions.saccpacdatabaseuser);
    			    sACCPACDatabasepw = rsOptions.getString(SMTablegloptions.saccpacdatabaseuserpw);
    			    iACCPACDatabaseType = rsOptions.getInt(SMTablegloptions.iaccpacdatabasetype);
    			}else{
    				rsOptions.close();
    				ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571429615]");
    				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to open GL Options table - function cannot run.");
    				smaction.redirectAction(
    						"", 
    						"", 
    			    		""
    					);
    					return;    			}
    		} catch (SQLException e) {
    			//Redirect back to calling class:
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571429616]");
				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to open GL Options table - function cannot run.");
				smaction.redirectAction(
						"", 
						"", 
			    		""
					);
					return;  
    		}
            
    		try {
    			cnACCPAC = getACCPACConnection(
    					iACCPACDatabaseType,
    					sACCPACDatabaseURL,
    					sACCPACDatabasename,
    					sACCPACDatabaseuser,
    					sACCPACDatabasepw);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571429617]");
				smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "Unable to get ACCPAC connection - " + e.getMessage());
				smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
    		}
    	try {
        		sResults = dc.checkTransactionsAgainstACCPACTransactions(
        			sGLAccount, 
        			sStartingFiscalYear, 
        			conn, 
        			cnACCPAC, 
        			getServletContext(), 
        			sDBID
        		);
    		} catch (Exception e) {
    			if (cnACCPAC != null){
    				try {
    					cnACCPAC.close();
    				} catch (SQLException e1) {
    					//Don't need to do anything here....
    				}
    			}
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571429818]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}
			if (cnACCPAC != null){
				try {
					cnACCPAC.close();
				} catch (SQLException e1) {
					//Don't need to do anything here....
				}
			}
    	}
    	
    	//If the user chose to INSERT any missing the fiscal/financial records:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.PARAM_INSERTMISSINGFISCALANDFINANCIAL_DATA) == 0){
        	try {
        		sResults = dc.checkMissingFiscalSets(sGLAccount, conn, sStartingFiscalYear);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571427318]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}	
    	}
    	
    	//If the user chose to UPDATE the fiscal sets from the transaction lines:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.PARAM_UPDATE_FISCALSET_DATA) == 0){
        	try {
    			sResults = dc.checkFiscalSetsAgainstTransactions(sGLAccount, sStartingFiscalYear, conn, true);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571427618]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}	
    	}
    	
    	//If the user chose to UPDATE the financial statement data to match the fiscal sets, branch here:
    	if (sSelectedProcess.compareToIgnoreCase(GLFinancialDataCheckSelect.PARAM_UPDATE_FINANCIAL_DATA) == 0){
        	try {
    			sResults = dc.processFinancialRecords(
    				sGLAccount, 
    				sStartingFiscalYear, 
    				conn, 
    				true
    				);
    		} catch (Exception e) {
    			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571427718]");
    			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
    			smaction.redirectAction(
    					"", 
    					"", 
    		    		""
    				);
    				return;
    		}
    	}
    	
    	//return after successful processing:
		sResults += "<BR><B>" + Long.toString((System.currentTimeMillis() - lStartingTimeInMS)/1000L) + "</B> seconds elapsed.<BR>";

		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426619]");
    	smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_RESULTS_OBJECT, sResults);
		smaction.redirectAction(
				"", 
				"", 
	    		""
			);
		return;
	}
	private Connection getACCPACConnection(
			int iACCPACDatabaseType,
			String sACCPACDatabaseURL,
			String sACCPACDatabaseName,
			String sACCPACUserName,
			String sACCPACPassword
			) throws Exception{
		Connection cnACCPAC = null;
		
		//If we're reading a Pervasive DB:
		if (iACCPACDatabaseType == SMTablegloptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE){
		//Pervasive connection
			try
				{
				cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sACCPACDatabaseURL + ":1583/" + sACCPACDatabaseName + "", sACCPACUserName, sACCPACPassword);
			}catch (Exception localException2) {
				try {
					Class.forName("com.pervasive.jdbc.v2.Driver").newInstance();
					cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sACCPACDatabaseURL + ":1583/" + sACCPACDatabaseName + "", sACCPACUserName, sACCPACPassword);
				} catch (InstantiationException e) {
					throw new Exception("InstantiationException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new Exception("IllegalAccessException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (ClassNotFoundException e) {
					throw new Exception("ClassNotFoundException getting ACCPAC Pervasive connection - " + e.getMessage());
				} catch (SQLException e) {
					throw new Exception("SQLException getting ACCPAC Pervasive connection - " + e.getMessage());
				}
			}
			
			if (cnACCPAC == null){
				throw new Exception("Could not get Pervasive connection");
			}
		//If we're reading an MS SQL DB:
		}else{
			try
			{
				cnACCPAC = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sACCPACDatabaseURL + ":1433;DatabaseName=" + sACCPACDatabaseName, sACCPACUserName, sACCPACPassword);
			}
			catch (Exception localException2) {
				try {
					//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
					//Class.forName("com.microsoft.jdbc.sqlserver.sqlserverdriver").newInstance();
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
					//cnGL = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sGLDatabaseURL + ":1433;DatabaseName=" + sGLDatabaseName, sGLUserName, sGLPassword);
					cnACCPAC = DriverManager.getConnection("jdbc:sqlserver://" + sACCPACDatabaseURL + ":1433;DatabaseName=" + sACCPACDatabaseName, sACCPACUserName, sACCPACPassword);
					//String Url = "jdbc:sqlserver://localhost:1433;databaseName=movies";
			        //Connection connection = DriverManager.getConnection(Url,"sa", "xxxxxxx);
				} catch (InstantiationException e) {
					throw new Exception("InstantiationException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (IllegalAccessException e) {
					throw new Exception("IllegalAccessException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (ClassNotFoundException e) {
					throw new Exception("ClassNotFoundException getting ACCPAC MS SQL connection - " + e.getMessage());
				} catch (SQLException e) {
					throw new Exception("SQLException getting ACCPAC MS SQL connection - " + e.getMessage());
				}
			}
			if (cnACCPAC == null){
				throw new Exception("Could not get MS SQL connection");
			}
		}
		return cnACCPAC;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}