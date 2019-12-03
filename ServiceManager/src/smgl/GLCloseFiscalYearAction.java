package smgl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfiscalsets;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLCloseFiscalYearAction extends HttpServlet{

	/*
	GL Year End Closing Logic:
	
	1) The GL 'Closing Account' is determined.
	
	2) A new GL batch is created with ONE entry in it. The fiscal year is the year being closed, and the period for the entry is 15
	 (which is the special, 'hidden' period, used only for closing).
	
	3) Then we get a list of the ending balances for ALL of the income/expense accounts.
	
	4) Next we go through each of the accounts on that list: if the ending balance is zero, we ignore it.
	
	5) For each account, we add a line to the GL batch entry: the AMOUNT is the ending balance for that account, NEGATED (multiplied by -1).
	
	6) After a line has been added (to the batch) for all of the income/expense accounts with ending balances,
	 we add one more line for the Retained Earnings account.  The amount of the line is the total from the income/expense accounts. 
	 This number is NOT reversed, because we already reversed each of the lines above.
	
	This creates a batch that should set all the income/expense accounts ending balances (as of period 15) to zero, and it also
	 ADDS that total amount to the Retained Earnings account. 
	
	*/
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
    	
    	String sClosingAccount = "";
    	GLOptions gloptions = new GLOptions();
    	if (!gloptions.load(conn)){
    		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875814]");
    		String sErrorMessage = "";
    		for (int i = 0; i < gloptions.getErrorMessages().size(); i++){
    			if (i == 0){
    				sErrorMessage += gloptions.getErrorMessages().get(i);
    			}else{
    				sErrorMessage += ", " + gloptions.getErrorMessages().get(i);
    			}
    		}
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, (String)sErrorMessage);
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
    	}
    	sClosingAccount = gloptions.getsClosingAccount();
    	
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
    	glentry.setsfiscalperiod(Integer.toString(SMTableglfiscalsets.TOTAL_NUMBER_OF_GL_PERIODS));
    	glentry.setsfiscalyear(sFiscalYear);
    	glentry.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_GL));
    	//Flag this as a 'closing entry':
    	glentry.setsclosingentry("1");

    	//Now get the ending balances for all the income statement accounts:
    	String SQL = "SELECT"
    		+ " SUM("  
    		+ SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdopeningbalance
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod1
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod2
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod3
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod4
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod5
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod6
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod7
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod8
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod9
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod10
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod11
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod12
    		+ " + " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod13
    		+ ") AS ACCTTOTAL"
    		+ ", " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID
    		+ " FROM " + SMTableglfiscalsets.TableName
    		+ " LEFT JOIN " + SMTableglaccounts.TableName + " ON "
    		+ SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID + " = " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
    		+ " WHERE ("
    			+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + sFiscalYear + ")"
    			+ " AND (" + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType + " = '" + SMTableglaccounts.ACCOUNT_TYPE_INCOME_STATEMENT + "')"
    		+ ") GROUP BY " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID
    	;
    	BigDecimal bdTotalForRetainedEarnings = new BigDecimal("0.00");
    	try {
			ResultSet rsIncomeStatementAcctTotals = ServletUtilities.clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					smaction.getsDBID(), 
					"MySQL", 
					this.toString() + ".doPost - user: " + smaction.getFullUserName()
				);
			while (rsIncomeStatementAcctTotals.next()){
				//Ignore accounts that have a zero total
				if (rsIncomeStatementAcctTotals.getBigDecimal("ACCTTOTAL").compareTo(BigDecimal.ZERO) != 0){
					GLTransactionBatchLine line = new GLTransactionBatchLine();
					line.setAmount(
						ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rsIncomeStatementAcctTotals.getBigDecimal("ACCTTOTAL").negate()).replace(",", "")
					);
					line.setsacctid(rsIncomeStatementAcctTotals.getString(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID));
					line.setscomment("");
					line.setsdescription("Last period balance for account");
					line.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_GL));
					line.setssourcetype("JE");
					line.setstransactiondate(sBatchDate);
					
					glentry.addLine(line);
					bdTotalForRetainedEarnings = bdTotalForRetainedEarnings.add(rsIncomeStatementAcctTotals.getBigDecimal("ACCTTOTAL"));
				}
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
		GLTransactionBatchLine line = new GLTransactionBatchLine();
		line.setAmount(ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalForRetainedEarnings).replace(",", ""));
		line.setsacctid(sClosingAccount);
		line.setscomment("");
		line.setsdescription("Last period balance for account");
		line.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_GL));
		line.setssourcetype("JE");
		line.setstransactiondate(sBatchDate);
		
		glentry.addLine(line);
    	
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
				"Error [1564156639] could not save batch - " + e.getMessage()
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
			"Closing entries were successfully created in batch #" + glbatch.getsbatchnumber() + ".",
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