package smfa;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablefatransactions;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class FAPeriodEndProcessingAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		 		+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAPeriodEndProcessing)){
	    	return;
	    }

		FAPeriodEndProcessing cProcessor = new FAPeriodEndProcessing();
		cProcessor.setFiscalYear(clsManageRequestParameters.get_Request_Parameter("FISCALYEAR", request));
		cProcessor.setFiscalPeriod(clsManageRequestParameters.get_Request_Parameter("FISCALPERIOD", request));
		cProcessor.setTransactionDate(clsManageRequestParameters.get_Request_Parameter("TRANSACTIONDATE", request));
		if (request.getParameter("PROVISIONALPOSTING") != null){
			cProcessor.setProvisional(true);
		}else{
			cProcessor.setProvisional(false);
		}
		if (request.getParameter("CONSOLIDATEGLBATCHDETAILS") != null){
			cProcessor.setConsolidateGLBatchDetails(true);
		}else{
			cProcessor.setConsolidateGLBatchDetails(false);
		}

		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost(primary) - User: " + sUserID
			+ " - "
			+ sUserFullName
				);
		//Need a connection here because it involves a data transaction:
		//if anything goes wrong, reverse the whole process.
		clsDatabaseFunctions.start_data_transaction(conn);
		try{
			cProcessor.doProcess(sUserID, conn);
		}catch(Exception e){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			//remove all inserted transactions if there are any:
			String sSQL = "DELETE FROM " + SMTablefatransactions.TableName + 
							" WHERE " + 
								SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalYear + " = " + clsManageRequestParameters.get_Request_Parameter("FISCALYEAR", request) +
							" AND " +
								SMTablefatransactions.TableName + "." + SMTablefatransactions.iFiscalPeriod + " = " + clsManageRequestParameters.get_Request_Parameter("FISCALPERIOD", request);
			try {
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			} catch (SQLException e1) {
			}

			//Remove all GL export records, if there are any:
			String sBatchNumber = Long.toString((Long.parseLong(clsManageRequestParameters.get_Request_Parameter("FISCALYEAR", request).trim()) * 100) 
				+ Long.parseLong(clsManageRequestParameters.get_Request_Parameter("FISCALPERIOD", request)));
			sSQL = "DELETE FROM " + SMTableglexportheaders.TableName + 
				" WHERE " + 
					SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchnumber + " = " + sBatchNumber;
			try {
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			} catch (SQLException e1) {
			}
			
			sSQL = "DELETE FROM " + SMTableglexportdetails.TableName + 
					" WHERE " + 
					SMTableglexportdetails.TableName + "." + SMTableglexportdetails.lbatchnumber + " = " + sBatchNumber;
			try {
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			} catch (SQLException e1) {
			}
			
			//Add an entry to the system log to let us know what happened:
			SMLogEntry log = new SMLogEntry(conn);
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_FIXEDASSETS, "FA Period Process Failed", e.getMessage(), "[1435346362]");
			
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067476]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAPeriodEndProcessingSelect"
					+ "?" + cProcessor.getQueryString()
					+ "&Warning=Could not finish period end process: " + e.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067477]");
		/*
		String disposition = "attachment; fileName= " + "FA-Export-" +sDatabaseName + "-" + sProcessingType + "-" + sDetailType + "-" +SMUtilities.now("MM-dd-yyyy") + ".CVS";
		response.setHeader("Content-Disposition", disposition);
		for (int i=0;i<sExportContent.size();i++){
			out.println(sExportContent.get(i));
		}
		*/
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAPeriodEndProcessingSelect"
				+ "?" + cProcessor.getQueryString()
				+ "&Status=Period end processing is complete: <BR>" + cProcessor.getErrorMessageString()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
