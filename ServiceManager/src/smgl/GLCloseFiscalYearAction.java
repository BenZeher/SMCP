package smgl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTablegltransactionlines;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLCloseFiscalYearAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLCloseFiscalYear)){return;}
	    //Read the entry fields from the request object:
	    String sFiscalYear = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLCloseFiscalYearEdit.PARAM_FISCAL_YEAR_SELECTION, request);
	    String sBatchDate = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLCloseFiscalYearEdit.PARAM_BATCH_DATE, request);
		
		if (request.getParameter(GLPullIntoConsolidationSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
		
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".updateCompanies - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	GLTransactionBatch glbatch = new GLTransactionBatch("-1");
    	glbatch.setlcreatedby(smaction.getUserID());
    	glbatch.setllasteditedby(smaction.getUserID());
    	glbatch.setsbatchdate(sBatchDate);
    	glbatch.setsbatchdescription("Closing Journal Entries");

    	GLTransactionBatchEntry glentry = new GLTransactionBatchEntry();
    	glentry.setsdatdocdate(sBatchDate);
    	glentry.setsdatentrydate(sBatchDate);
    	glentry.setsentrydescription("Closing Entry");
    	glentry.setsentrynumber("1");
    	glentry.setsfiscalperiod("15");
    	glentry.setsfiscalyear(sFiscalYear);
    	glentry.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_JOURNAL_ENTRY));

    	//Now get the transaction batch lines:
    	//TODO:
    	String SQL = "SELECT " + SMTableglfiscalperiods.inumberofperiods
    		+ " FROM " + SMTableglfiscalperiods.TableName
    		+ " WHERE ("
    			+ "(" + SMTableglfiscalperiods.ifiscalyear + " = " + sFiscalYear + ")" 
    		+ ")"
    	;
//    	int iNumberOfPeriods = 0;
//    	try {
//			ResultSet rsFiscalPeriod = ServletUtilities.clsDatabaseFunctions.openResultSet(
//				SQL, 
//				getServletContext(), 
//				smaction.getsDBID(), 
//				"MySQL", 
//				this.toString() + ".doPost - user: " + smaction.getFullUserName()
//			);
//			if (rsFiscalPeriod.next()){
//				iNumberOfPeriods = rsFiscalPeriod.getInt(SMTableglfiscalperiods.inumberofperiods);
//				rsFiscalPeriod.close();
//			}else{
//				rsFiscalPeriod.close();
//			}
//		} catch (SQLException e1) {
//			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1564156142]");
//			smaction.getCurrentSession().setAttribute(
//				GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, 
//				"Error [1564156143] reading number of periods - " + e1.getMessage()
//			);
//			smaction.redirectAction(
//				"", 
//				"", 
//	    		""
//			);
//			return;
//		}

    	SQL = "SELECT"
    		+ " SUM(" + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.bdamount + ") AS ACCTTOTAL"
    		+ ", " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
    		+ " FROM " + SMTablegltransactionlines.TableName
    		+ " LEFT JOIN " + SMTableglaccounts.TableName + " ON "
    		+ SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
    		+ " WHERE ("
    			+ "(" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
    		+ ") GROUP BY " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
    	;
    	try {
			ResultSet rsIncomeStatementAcctTotals = ServletUtilities.clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					smaction.getsDBID(), 
					"MySQL", 
					this.toString() + ".doPost - user: " + smaction.getFullUserName()
				);
			while (rsIncomeStatementAcctTotals.next()){
				GLTransactionBatchLine line = new GLTransactionBatchLine();
				line.setAmount(
					ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsIncomeStatementAcctTotals.getBigDecimal("ACCTTOTAL").negate()).replace(",", "")
				);
				line.setsacctid(rsIncomeStatementAcctTotals.getString(SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid));
				line.setscomment("");
				line.setsdescription("Last period balance for account");
				line.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_JOURNAL_ENTRY));
				line.setssourcetype("JE");
				line.setstransactiondate(sBatchDate);
				
				glentry.addLine(line);
			}
			rsIncomeStatementAcctTotals.close();
		} catch (SQLException e1) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875714]");
			smaction.getCurrentSession().setAttribute(
				GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT,
				"Error [1564156616] getting account totals - " + e1.getMessage()
			);
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
		}
    	
    	//Now add one balancing entry for the retained earnings account:
    	//TODO
    	
    	glbatch.addBatchEntry(glentry);
    	if (!ServletUtilities.clsDatabaseFunctions.start_data_transaction(conn)){
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875714]");
			smaction.getCurrentSession().setAttribute(
				GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT,
				"Error [1564156636] could not start data transaction."
			);
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
    	}
    	try {
			glbatch.save_without_data_transaction(conn, smaction.getUserID(), smaction.getFullUserName(), false);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875714]");
			smaction.getCurrentSession().setAttribute(
				GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT,
				"Error [1564156639] could not save batch."
			);
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
		}
    	
    	try {
			ServletUtilities.clsDatabaseFunctions.commit_data_transaction_with_exception(conn);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875734]");
			smaction.getCurrentSession().setAttribute(
				GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT,
				"Error [1564156639] could not commit transaction."
			);
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
		}
    	
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875615]");
		smaction.redirectAction(
			"", 
			"Company with ID '" + "" + "' was successfully pulled into the consolidated company.",
    		""
		);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}