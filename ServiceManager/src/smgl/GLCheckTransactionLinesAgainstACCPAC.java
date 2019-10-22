package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLCheckTransactionLinesAgainstACCPAC extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_GLACCOUNT = "GLACCOUNT";
	public static final String PARAM_FISCALYEAR = "FISCALYEAR";
	public static final String PARAM_FISCALPERIOD = "FISCALPERIOD";
	
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
		String sFiscalYear = request.getParameter(PARAM_FISCALYEAR);
		String sFiscalPeriod = request.getParameter(PARAM_FISCALPERIOD);
		String sGLAccount = request.getParameter(PARAM_GLACCOUNT);

	    String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    smaction.getPwOut().println("GL Transactions for GL account '<B>" + sGLAccount 
	    	+ "</B>', fiscal year <B>" + sFiscalYear + "</B>, period <B>" + sFiscalPeriod + "</B>.");
	    
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.getPwOut().println("<BR>Error [1571684770] getting SMCP connection - " + e1.getMessage());
			return;
		}
    	
    	//get an ACCPAC connection here:
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
				smaction.getPwOut().println("<BR>Error [1571684771] reading GL options with SQL: '" + SQL +"'.");
				return;  			}
		} catch (SQLException e) {
			//Redirect back to calling class:
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426616]");
			smaction.getPwOut().println("<BR>Error [1571684772] Unable to open GL Options table - function cannot run - " + e.getMessage());
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
			smaction.getPwOut().println("<BR>Error [1571684773] Unable to get ACCPAC connection - " + e.getMessage());
			return;  
		}
    	
    	long lStartingTimeInMS = System.currentTimeMillis();
 
    	//Read the two recordets here and display them:
    	try {
    		GLFinancialDataCheck dc = new GLFinancialDataCheck();
			dc.readMatchingRecordsets(conn, cnACCPAC, sFiscalYear, sFiscalPeriod, sGLAccount, smaction.getPwOut());
		} catch (Exception e) {
			smaction.getPwOut().println("<BR><B>Error comparing ACCPAC transactions to SMCP transactions - " + e.getMessage());
		}
    	
		if (cnACCPAC != null){
			try {
				cnACCPAC.close();
			} catch (SQLException e1) {
				//Don't need to do anything here....
			}
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1571426619]");
		
		smaction.getPwOut().println("<BR><B>" + Long.toString((System.currentTimeMillis() - lStartingTimeInMS)/1000L) + "</B> seconds elapsed.<BR>");
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