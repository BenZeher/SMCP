package smar;

import SMClasses.SMBatchTypes;
import SMClasses.MySQLs;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTableartransactionline;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;
import SMDataDefinition.SMTableentries;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditReversalEntry extends HttpServlet {
	
	/*
	 * Reverses:

	A misc receipt:
	Create a transaction: amt opposite original, current amount zero, same control account
	Create an armatchingline: applies to the original document, amount is opposite original
	Create another armatching line to reduce the reversal's current amount to zero
	Entry: control acct, same, entry amount opposite original entry amount
	Entrylines: dist. acct - user selects, line amount opposite original entryline
	
	A receipt:
	Create a transaction: amt opposite the original amount, current amt zero (after the armatchinglines
	 are created in the batch posting), same control account (cash account).
	
	Create armatchingline: from each of the entrylines.  If there was originally an unapplied line, an armatching
	 line is created to bring the current amount to zero.  All of the armatchinglines created should bring the 
	 current amount of the reversed transaction to zero.
	
	Create armatchinglines to apply to the reversal, also, and the current amount of the reversing 
	transaction should end up at zero also.
	
	Entry: control acct, same, entry amount opposite the original amount
	Entrylines: 
	
	One for each original matchingline's target - so if we are reversing a check, we have one entryline for 
	every line that the original check applied to, but opposite the original check entryline amount.  
	The dist GL is ALWAYS the AR account for that customer.
	
	If there were any unapplied lines in the original receipt, we would know because the entry amount 
	would not equal the matching amounts - we have to subtract the total of the lines from the original 
	transaction amount.  For any unapplied lines, we would have to create a single line, with the GL set 
	to the default AR account for the customer, the amount opposite the original unapplied amount.  
	These lines would apply to the original receipt.
	
	A prepay:
	Same as a receipt - Some of the prepay may now be applied to invoices, so they have to be reversed, 
	just as if it was a receipt.
	
	
	An apply-to:
	Same as a receipt
	 */
	
	private static final long serialVersionUID = 1L;

	private static final String sObjectName = "Reversal Entry";
	
	private String m_sCustomerARCustomerDepositAcct;

	//We'll use these to store the GL List, so we don't have to load it several times:
	// TJR - 1/28/2019 - these are global variables, but they only get written once, so we're leaving them here for now
	//    because they speed up processing.
	private ArrayList<String> m_sGLValues = new ArrayList<String>();
    private ArrayList<String> m_sGLDescriptions = new ArrayList<String>();

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter m_pwOut = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditBatches))
			{
				return;
			}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		AREntryInput m_EntryInput = (AREntryInput) CurrentSession.getAttribute("EntryInput");
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		boolean m_bIsNewEntry = false;
		if (request.getParameter("EntryNumber") != null){
			if (clsManageRequestParameters.get_Request_Parameter("EntryNumber", request).equalsIgnoreCase("-1")){
				m_bIsNewEntry = true; 
			}
		}

		String m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", request);
		String m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", request);
		String m_sDocumentType = clsManageRequestParameters.get_Request_Parameter("DocumentType", request);
		String m_sEditable = clsManageRequestParameters.get_Request_Parameter("Editable", request);
		boolean m_bEditable = false;
		if (m_sEditable.compareToIgnoreCase("Yes") == 0){
			m_bEditable = true;
		}else {
			m_bEditable = false;
		}
		String m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", request);
		String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String m_sApplyToDocumentID = clsManageRequestParameters.get_Request_Parameter("DocumentID", request);
	    
		//Try to load an AREntryInput object from which to build the form:
		if (m_EntryInput == null){
			try {
				m_EntryInput = loadAREntryInput(
					sDBID,
					sUserID, 
					sUserFullName,
					m_sBatchNumber,
					m_sEntryNumber,
					m_sBatchType,
					m_bIsNewEntry,
					m_sApplyToDocumentID
				);
			} catch (Exception e) {
	    		response.sendRedirect(
						SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
						+ "?BatchNumber=" + m_sBatchNumber
						+ "&BatchType=" + m_sBatchType
						+ "&DocumentID=" + m_sApplyToDocumentID
						+ "&DocumentType=" + m_sDocumentType
						+ "&Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	        		);
				return;
			}
		}
	    String title;
	    String subtitle = "";
	    if (m_bIsNewEntry){
	    	title = "Edit NEW " + sObjectName;
	    }else{
	    	title = "Edit " + sObjectName + ": " + m_EntryInput.getsEntryNumber();	
	    }

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Accounts Receivable Main Menu</A><BR>");
	    
	    //Print a link to return to the 'edit batch' page:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
	    		+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
	    		+ "&BatchType=" + m_EntryInput.getsBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Return to Edit Batch " + m_EntryInput.getsBatchNumber() + "</A><BR><BR>");

		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!createFormFromAREntryInput(sDBID, sUserID, sUserFullName, m_pwOut, m_EntryInput, m_bEditable, m_sApplyToDocumentID)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_sBatchType
					+ "&DocumentID=" + m_sApplyToDocumentID
					+ "&DocumentType=" + m_sDocumentType
					+ "&Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private AREntryInput loadAREntryInput(
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sBatchNumber,
			String sEntryNumber,
			String sBatchType,
			boolean bIsNewEntry,
			String sApplyToDocumentID
			) throws Exception{
		
		AREntryInput m_EntryInput = new AREntryInput();
		String m_sCustomerARAcct = "";
		
		//If the class has NOT been passed an AREntryInput query string, we'll have to build it:
		//Have to construct the AREntryInput object here:
		m_EntryInput = new AREntryInput();
		if (bIsNewEntry){
			//If it's a new entry:
			//Load the transaction this adjustment will apply to:
			//Load the transaction this adjustment will apply to:
			if(!clsValidateFormFields.IsValidLong(sApplyToDocumentID)){
	    		throw new Exception("Document ID " + sApplyToDocumentID + " is not valid.");
			}
	    	ARTransaction m_Transaction = new ARTransaction(sApplyToDocumentID);
	    	if (! m_Transaction.load(getServletContext(), sDBID)){
	    		throw new Exception("Could not load transaction with document ID: " + sApplyToDocumentID + " - " + m_Transaction.getErrorMessage());
	    	}	    	
	    	
	    	//If it has already been reversed, do not let it be reversed again:
	    	try {
				if (checkForPreviousReversal(m_Transaction.getsTransactionID(), sDBID, sUserID, sUserFullName)){
					throw new Exception("This document was already reversed and can't be reversed again.");
				}
			} catch (SQLException e) {
				throw new Exception(e.getMessage());
			}
	    	
	    	//Confirm that the transaction CAN be reversed, i.e., that it's either a receipt,
	    	//or a prepay, or a misc cash entry, or an apply-to:
	    	if (
	    			m_Transaction.getiDocType() == ARDocumentTypes.APPLYTO
	    			|| m_Transaction.getiDocType() == ARDocumentTypes.MISCRECEIPT
	    			|| m_Transaction.getiDocType() == ARDocumentTypes.PREPAYMENT
	    			|| m_Transaction.getiDocType() == ARDocumentTypes.RECEIPT
	    		){
	    		
	    	}else{
	    		throw new Exception("Can not reverse " + ARDocumentTypes.Get_Document_Type_Label(m_Transaction.getiDocType()) + ".");
	    	}
	    	
			m_EntryInput.setBatchNumber(sBatchNumber);
			m_EntryInput.setBatchType(sBatchType);
			m_EntryInput.setCustomerNumber(m_Transaction.getCustomerNumber());
			String sCashAcct = m_Transaction.getControlAcct();
			if (sCashAcct.compareToIgnoreCase("") != 0){
				m_EntryInput.setControlAcct(m_Transaction.getControlAcct());
			}
			//Get the batch date as the default entry date:
			ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
			try {
				batch.load(getServletContext(), sDBID);
				m_EntryInput.setDocDate(batch.sStdBatchDateString());
			} catch (Exception e) {
				m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			}
			m_EntryInput.setDocDescription("Reversed doc ID: " + sApplyToDocumentID);
			m_EntryInput.setDocumentType(Integer.toString(ARDocumentTypes.REVERSAL));
			m_EntryInput.setEntryNumber("-1");
			m_EntryInput.setEntryID("-1");
			m_EntryInput.setOriginalAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					m_Transaction.getdOriginalAmount().negate()));
			
			//For reversals of misc receipts, we just need to create a line from the original transaction
			//Add one entry line to reverse the original entry:
			ARLineInput line;
			if (m_Transaction.getiDocType() == ARDocumentTypes.MISCRECEIPT){
				line = new ARLineInput();
				//We use the amount of the artransaction as the line amount:
				line.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_Transaction.getdOriginalAmount()));
				line.setApplyToOrderNumber(m_Transaction.getOrderNumber());
				line.setComment("Reversing a " + ARDocumentTypes.Get_Document_Type_Label(m_Transaction.getiDocType()));
				line.setDescription("");
				line.setDocAppliedTo(m_Transaction.getDocNumber());
				line.setDocAppliedToID(sApplyToDocumentID);
				line.setEntryID("-1");
				line.setLineID("-1");
				//On a misc receipt reversal, the user has to pick the distribution GL (usually a
				//misc income account), so we don't set that here
				m_EntryInput.addLine(line);
			}else{
				//Get the customer name here:
			    if (m_EntryInput.getsCustomerNumber().compareToIgnoreCase("") != 0){
			    	ARCustomer m_Customer = new ARCustomer(m_EntryInput.getsCustomerNumber());
					if (! m_Customer.load(getServletContext(), sDBID)){
						throw new Exception("Could not load customer: " + m_EntryInput.getsCustomerNumber());
					}
			    	m_sCustomerARAcct = m_Customer.getARControlAccount(
			    			getServletContext(), sDBID);
			    	m_sCustomerARCustomerDepositAcct = m_Customer.getARPrepayLiabilityAccount(
			    			getServletContext(), sDBID);
			    }
				//For reversals of cash, prepays, and apply-to's, we need to read the lines:
				//Misc receipts won't have any . . . . 
			    
			    //Now load the child matching lines:
			    
				//Now we get all of the transactions that the 'to-be-reversed' transaction applied to.  If the
				//to-be-reversed transaction is an apply-to, then we just have to get all of the armatching lines
				//that it included.  But if it's a pre-pay, or a receipt that we're trying to reverse,
				//we only need the matching lines that the to-be-reversed transaction created, but that
				//don't apply back to itself:
				String SQL = "SELECT *" 
					+ " FROM " + SMTablearmatchingline.TableName
					+ " WHERE (";
							if (m_Transaction.getiDocType() == ARDocumentTypes.APPLYTO){
								//If it's an apply-to, we have to get all the lines from the apply-to:
								SQL = SQL + "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
									+ " = " +  m_Transaction.getsTransactionID() + ")";
							}else{
								//If it's a receipt or prepay, we only need the lines from the original transaction
								//that were applied to OTHER transactions:
								SQL = SQL +  "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
									+ " = '" +  m_Transaction.getDocNumber() + "')"
									+ " AND ("+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
									+ " = '" +  m_Transaction.getCustomerNumber() + "')"
						
									+ " AND ("+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
									+ " != '" +  m_Transaction.getDocNumber() + "')";
							}
						SQL = SQL + ")"
						+ " ORDER BY " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
						;
		        try{
			        ResultSet rsMatchingLines = clsDatabaseFunctions.openResultSet(
			        	SQL, 
			        	getServletContext(), 
			        	sDBID,
			        	"MySQL",
			        	this.toString() + ".loadChildMatchingLines (1) - User: " + sUserID
			        	+ " - "
			        	+ sUserFullName
			        		);
			        while (rsMatchingLines.next()){
			        	ARLineInput ar_line = new ARLineInput();
			        	//We negate the line because we want to do an exact reverse:
			        	ar_line.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsMatchingLines.getBigDecimal(SMTableartransactionline.damount).negate()));
			        	SQL = "SELECT * FROM " + SMTableartransactions.TableName
			        		+ " WHERE ("
			        			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid
			        			+ " = " + Long.toString(rsMatchingLines.getLong(SMTablearmatchingline.ldocappliedtoid))
			        		+ ")"
			        	;
			        	ResultSet rsApplyToTransactions = clsDatabaseFunctions.openResultSet(
			    	        	SQL, 
			    	        	getServletContext(), 
			    	        	sDBID,
			    	        	"MySQL",
			    	        	this.toString() + ".loadChildMatchingLines (2) - User: " + sUserID
			    	        	+ " - "
			    	        	+ sUserFullName
			    	        	);
			        	if(rsApplyToTransactions.next()){
			            	String sOriginalApplyToID = Long.toString(rsApplyToTransactions.getLong(SMTableartransactions.lid));
			            	ar_line.setDocAppliedTo(rsApplyToTransactions.getString(SMTableartransactions.sdocnumber));
			            	ar_line.setDocAppliedToID(sOriginalApplyToID);
			            	ar_line.setApplyToOrderNumber(rsApplyToTransactions.getString(SMTableartransactions.sordernumber));
			            	ar_line.setComment("Un-applying from ID " + sOriginalApplyToID);
			            	ar_line.setDescription(
				        		"Reverse original application from Doc ID: " + m_Transaction.getsTransactionID() 
				        		+ " to Doc ID: " + sOriginalApplyToID);
			            	ar_line.setEntryID("-1");
				        	//This account will always be the customer's AR account:
			            	ar_line.setLineAcct("");
			        	}else{
			        		throw new Exception("Could not load apply-to transaction for doc ID: " + m_Transaction.getsTransactionID());
			        	}
			        	rsApplyToTransactions.close();
			        	m_EntryInput.addLine(ar_line);
			        }
			        rsMatchingLines.close();

				}catch (SQLException ex){
					throw new Exception("Error [1548709140] loading child matching lines - " + ex.getMessage());
				}
			    
			    //There may have been unapplied lines on the original entry.  We won't find any armatchinglines
			    //for these, so we have to recreate one line to account for the total of any unapplied lines now:
			    BigDecimal bdCalculatedLineTotal = BigDecimal.ZERO;
			    for (int i = 0; i < m_EntryInput.getLineCount(); i++){
			    	bdCalculatedLineTotal = bdCalculatedLineTotal.add(
			    		clsStringFunctions.bdStringToBigDecimal(m_EntryInput.getLine(i).getAmount(),2));
			    }
			    
			    //TJR - test these lines - 3/14/2011:
			    //If the transaction to be reversed has a current amount on it, then we need to add a line
			    //to it to make sure we get the entire transaction reversed back and the net on it is zero:
			    if (m_Transaction.getdCurrentAmount().compareTo(BigDecimal.ZERO) != 0){
					line = new ARLineInput();
					//The artransaction amount and any armatchinglines applying to it have the same sign,
					// so to reverse,
					//we have to reverse the sign of the transaction:
					line.setAmount(
						clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								m_Transaction.getdCurrentAmount()));
					line.setApplyToOrderNumber("");
					line.setComment("Reversing Doc ID " + sApplyToDocumentID);
					line.setDescription("Reduces the current amount on the transaction to zero");
					line.setDocAppliedTo(m_Transaction.getDocNumber());
					line.setDocAppliedToID(sApplyToDocumentID);
					line.setEntryID("-1");
					line.setLineID("-1");
					m_EntryInput.addLine(line);
			    }
			    
			    //The 'bdAmountAlreadyApplied' is the amount that has already been applied FROM the 
			    //to-be-reversed transaction, determined by subtracting the current amount from the
			    //original amount
			    BigDecimal bdAmountAlreadyApplied = 
			    	m_Transaction.getdOriginalAmount().subtract(m_Transaction.getdCurrentAmount());
			    if(bdCalculatedLineTotal.compareTo(bdAmountAlreadyApplied) != 0){
			    	//If the total of the armatchinglines which were created by this transaction and applied
			    	//to OTHER transactions doesn't match the amount that the 'bdAmountAlreadyApplied', then
			    	//some armatching lines have been cleared, which means that the transaction(s) that this
			    	//to-be-reversed document originally applied to are gone and we can't reverse:
			    	
			    	//Commented out this code on 3/14/2011 - TJR
			    	//Someone had created a reversal of a cash entry, but the invoice that the cash
			    	//entry had been applied to was no longer in the system, so wound up creating
			    	//a positive balance cash entry, which we couldn't do anything with.  So
			    	//changed the logic in here to prevent that.
			    	/*
					line = new ARLineInput();
					line.setAmount(
						ARUtilities.BigDecimalTo2DecimalSTDFormat(
							m_Transaction.getdOriginalAmount().subtract(bdCalculatedLineTotal)));
					line.setApplyToOrderNumber("");
					line.setComment("Reversing Doc ID " + m_sApplyToDocumentID);
					line.setDescription("");
					line.setDocAppliedTo(m_Transaction.getDocNumber());
					line.setDocAppliedToID(m_sApplyToDocumentID);
					line.setEntryID("-1");
					line.setLineID("-1");
					m_EntryInput.addLine(line);
					*/
			    	throw new Exception("You are trying to reverse this transaction, but some of the transactions"
			    		+ " to which THIS transaction applied have already "
			    		+ "been cleared from the system - so this transaction can NOT be reversed.");
			    }
			    
			    //Set the GL acct on the dist lines here:
			    for (int i = 0; i < m_EntryInput.getLineCount(); i++){
			    	//If it's a prepay, it should be the customer's 'customer deposit' account:
			    	//TODO - figure out how to reverse an apply-to if it involved a prepay?????
			    	if (m_Transaction.getiDocType() == ARDocumentTypes.PREPAYMENT){
			    		m_EntryInput.getLine(i).setLineAcct(m_sCustomerARCustomerDepositAcct);
			    	}else{
			    		m_EntryInput.getLine(i).setLineAcct(m_sCustomerARAcct);
			    	}
			    }
			}
		    
		    //System.out.println("In " + this.toString() + ".loadAREntryInput - NEW ENTRY:\n" + m_EntryInput.getDataDump());
		}else{
			//If it's an existing entry:
			//Load the existing entry:
			AREntry entry = new AREntry();
			entry = new AREntry();
			if (!entry.load(sBatchNumber, sEntryNumber, getServletContext(), sDBID)){
		    	throw new Exception("Could not load entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber
		    			+ "\n" + entry.getErrorMessage());
			}
			if (!m_EntryInput.loadFromEntry(entry)){
				throw new Exception("Could not load entry input from entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber);
			}
			//System.out.println("In " + this.toString() + ".loadAREntryInput - LOADING FROM EXISTING ENTRY:\n" + m_EntryInput.getDataDump());
		}
		return m_EntryInput;
	}
	private boolean createFormFromAREntryInput(
			String sDBID, 
			String sUserID, 
			String sUserFullName, 
			PrintWriter m_pwOut, 
			AREntryInput ar_entry_input, 
			boolean bEditable, 
			String sApplyToDocumentID
			){
		
		//Include the javascript:
		m_pwOut.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREntryUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm (m_pwOut, ar_entry_input, bEditable, sApplyToDocumentID);

        //Display the entry header fields:
	    displayEntryHeaderFields (sDBID, m_pwOut, ar_entry_input, bEditable);
        
	    if (!loadGLList(sDBID, sUserID, sUserFullName)){
	    	return false;
	    }
	    
        if (bEditable){
        	displayEditableEntryFields (m_pwOut, ar_entry_input);
        }
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields (m_pwOut, ar_entry_input);
        }
        
	    //Now display the transaction lines:
        m_pwOut.println("<B>Line distribution:</B><BR>");
	    
	    //If it's not editable, just show the current applied lines:
	    if (! bEditable){
	    	Display_NONEditable_Lines(m_pwOut, ar_entry_input);
	    }else{

	        //Display the line header:
		    Display_Line_Header(m_pwOut);

		    //Display all the current transaction lines:
		    if (!displayTransactionLines(m_pwOut, ar_entry_input)){
		    	return false;
		    }	    
		    m_pwOut.println("</TABLE>");
	    }
	    //End the entry edit form:
	    m_pwOut.println("</FORM>");  

		return true;
	}
	private boolean checkForPreviousReversal(String sDocID, String sDBID, String sUserID, String sUserFullName) throws SQLException{
		
		boolean bPreviouslyReversed = false;
		String SQL = "SELECT"
			+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
			+ " FROM " + SMTablearmatchingline.TableName + " LEFT JOIN "
			+ SMTableartransactions.TableName + " ON " + SMTablearmatchingline.TableName + "." 
			+ SMTablearmatchingline.lparenttransactionid
			+ " = " + SMTableartransactions.TableName + "." + SMTableartransactions.lid
			+ " WHERE ("
				+ "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
				+ " = " + sDocID + ")"
				+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype
				+ " = " + ARDocumentTypes.REVERSAL + ")"
			+ ")"
			;
		
        try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
			    	SQL, 
			    	getServletContext(), 
			    	sDBID,
			    	"MySQL",
			    	this.toString() + ".loadChildMatchingLines (1) - User: " + sUserID
			    	+ " - "
			    	+ sUserFullName);
			
			if (rs.next()){
				bPreviouslyReversed = true;
			}
			rs.close();
			
		} catch (Exception e) {
			throw new SQLException(
				"Error checking to see if this document was already reversed with SQL: " + SQL + " - " + e.getMessage());
		}
		return bPreviouslyReversed;
	}

	private void storeHiddenFieldsOnForm(PrintWriter m_pwOut, AREntryInput m_EntryInput, boolean bEditable, String sApplyToDocumentID){
		
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchNumber + "\" VALUE=\"" + m_EntryInput.getsBatchNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryNumber + "\" VALUE=\"" + m_EntryInput.getsEntryNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamCustomerNumber + "\" VALUE=\"" + m_EntryInput.getsCustomerNumber() + "\">");
	    String sEditable;
	    if (bEditable){
	    	sEditable = "Yes";
	    }else{
	    	sEditable = "No";
	    }
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE=\"" + sEditable + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryID + "\" VALUE=\"" + m_EntryInput.getsEntryID() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchType + "\" VALUE=\"" + m_EntryInput.getsBatchType() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamDocumentType + "\" VALUE=\"" + m_EntryInput.getsDocumentType() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamDocNumber + "\" VALUE=" + m_EntryInput.getsDocNumber() + ">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + "AREditReversalEntry" + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "DocumentID" + "\" VALUE=\"" + sApplyToDocumentID + "\">");
	    
	}
	private void displayEntryHeaderFields (String sDBID, PrintWriter m_pwOut, AREntryInput m_EntryInput, boolean bEditable){
		
		m_pwOut.println("<B>" + SMBatchTypes.Get_Batch_Type(Integer.parseInt(m_EntryInput.getsBatchType())) + "</B>");
		m_pwOut.println(" batch number: <B>" + m_EntryInput.getsBatchNumber() + "</B>;");
        if (m_EntryInput.getsEntryNumber().equalsIgnoreCase("-1")){
        	m_pwOut.println(" entry number: <B>NEW</B>.  ");
        }else{
        	m_pwOut.println(" entry number: <B>" + m_EntryInput.getsEntryNumber() + "</B>.  ");
        }
        m_pwOut.println("For customer: " + "<B>" + m_EntryInput.getsCustomerNumber());
    	
        //Get the customer name:
        String sCustomerName = "** NOT FOUND **";
        ARCustomer cus = new ARCustomer(m_EntryInput.getsCustomerNumber());
        if (!cus.load(getServletContext(), sDBID)){
        	//Just jump out here
        }else{
        	sCustomerName = cus.getM_sCustomerName();
        }
        m_pwOut.println(" - " 
        		+ sCustomerName
        		+ "</B><BR>");
    	
        m_pwOut.println(" Document type: <B>" 
        		+ ARDocumentTypes.Get_Document_Type_Label(
        				Integer.parseInt(m_EntryInput.getsDocumentType()))
        		+ "</B>.  ");

	    if (bEditable){
	    	m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
        }
	}
	private void displayEditableEntryFields(PrintWriter m_pwOut, AREntryInput m_EntryInput){
		
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
        //START ROW 1
		m_pwOut.println("<TR>");
        
        //Doc Number:
		m_pwOut.println("<TD>");
		m_pwOut.println("Doc. #: " + m_EntryInput.getsDocNumber());
		m_pwOut.println("</TD>");

        //Doc date:
		m_pwOut.println("<TD>");
		
		m_pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDate, 
        		clsStringFunctions.filter(m_EntryInput.getsDocDate()), 
        		10, 
        		"Doc&nbsp;Date:", 
        		"",
        		8
        		)
        );
		m_pwOut.println(SMUtilities.getDatePickerString(AREntryInput.ParamDocDate, getServletContext()));
		m_pwOut.println("</TD>");

        //Entry amount:
		m_pwOut.println("<TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ AREntryInput.ParamOriginalAmount
    			+ "\" VALUE=\"" + m_EntryInput.getsOriginalAmount() + "\">");
		m_pwOut.println(m_EntryInput.getsOriginalAmount());
		m_pwOut.println("</TD>");

        //Control Acct:
		m_pwOut.println("<TD>");
		m_pwOut.println("Control&nbsp;Acct:");
		
		//if (bListGLAccts){
			//List the GL accounts:
			m_pwOut.println("<SELECT NAME = \"" + AREntryInput.ParamControlAcct + "\">");
	        
	        //Read out the array list:
			m_pwOut.println("<OPTION");
			//SQLStatements.get(i).toString()
			if (m_EntryInput.getsControlAcct().compareToIgnoreCase("") == 0){
				m_pwOut.println(" selected=yes");
			}
			m_pwOut.println(" VALUE=\"" + "" + "\">");
			m_pwOut.println("*** Select a control account ***");
	        for (int i = 0; i < m_sGLValues.size();i++){
	        	m_pwOut.println("<OPTION");
				//SQLStatements.get(i).toString()
				if (m_sGLValues.get(i).toString().compareToIgnoreCase(m_EntryInput.getsControlAcct()) == 0){
					m_pwOut.println(" selected=yes");
				}
				m_pwOut.println(" VALUE=\"" + m_sGLValues.get(i).toString() + "\">");
				m_pwOut.println(m_sGLDescriptions.get(i).toString());
	        }
	        
	        m_pwOut.println("</SELECT>");

		m_pwOut.println("</TD>");

        //END ROW 1
		m_pwOut.println("</TR>");
        
        //START ROW 2
		//Terms:
		m_pwOut.println("<TD>");
		m_pwOut.println("Terms: (N/A)");
		m_pwOut.println("</TD>");
		
		//Due Date:
		m_pwOut.println("<TD>");
		m_pwOut.println("Due Date: (N/A)");
		m_pwOut.println("</TD>");

        //Display the out-of-balance amount:
        m_pwOut.println("<TD>");
        m_pwOut.println("Out of Balance: ");
        m_pwOut.println(m_EntryInput.getsUndistributedAmount());
        m_pwOut.println("</TD>");
		
        //Description:
		m_pwOut.println("<TD>");
		m_pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDescription, 
        		ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(m_EntryInput.getsDocDescription())), 
        		SMTableentries.sdocdescriptionLength, 
        		"Description:", 
        		"",
        		48
        		)
        );
		m_pwOut.println("</TD>");
        
        //END ROW 2:
        m_pwOut.println("</TR>");
        
        //START ROW 3:
        m_pwOut.println("<TR>");
        
        //Apply-to-document number:
        m_pwOut.println("<TD>Apply-to doc: <B>&nbsp;</B></TD>");

        //Apply-to-document ID:
        m_pwOut.println("<TD>Apply-to doc ID: <B>&nbsp;</B></TD>");
        
        //END ROW 3:
        m_pwOut.println("</TR>");
        
        m_pwOut.println("</TABLE>");
	}
	private boolean loadGLList(String sDBID, String sUserID, String sUserFullName){
        m_sGLValues.clear();
        m_sGLDescriptions.clear();
        try{
	        String sSQL = MySQLs.Get_GL_Account_List_SQL(false);

	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID,
		        	"MySQL",
		        	this.toString() + ".loadGLList (1) - User: " + sUserID
		        	+ " - "
		        	+ sUserFullName
	        		);
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsGLAccts.next()){
	        	m_sGLValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	m_sGLDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}
	public void displayNonEditableEntryFields (PrintWriter m_pwOut, AREntryInput m_EntryInput){

		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
		//START ROW 1
        //Doc Number:
		m_pwOut.println("<TD>Doc #: <B>" + clsStringFunctions.filter(m_EntryInput.getsDocNumber())+ "</B></TD>");
        //Doc date:
		m_pwOut.println("<TD>Doc. date:<B>" + m_EntryInput.getsDocDate() + "</B></TD>");
        //Original amt:
		m_pwOut.println("<TD>Entry amt: <B>" + m_EntryInput.getsOriginalAmount() + "</B></TD>");
        //Control Acct:
		m_pwOut.println("<TD>Control acct: <B>" + m_EntryInput.getsControlAcct() + "</B></TD>");

        //END ROW 1
		m_pwOut.println("</TR>");

        //START ROW 2
		m_pwOut.println("<TR>");

        //Terms:
		m_pwOut.println("<TD>Terms: <B>(N/A)</B></TD>");
        //Due date:
		m_pwOut.println("<TD>Due date: <B>(N/A)</B></TD>");
        //Out of balance amt:
		m_pwOut.println("<TD>Out of balance: <B>" + m_EntryInput.getsUndistributedAmount() + "</B></TD>");		
		//Description:
		m_pwOut.println("<TD>Description: <B>"
        		+ ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(m_EntryInput.getsDocDescription())) 
        		+ "</B></TD>");
        
        //END ROW 2:
		
        //START ROW 3:
        m_pwOut.println("<TR>");
        
        //Apply-to-document number:
        m_pwOut.println("<TD>Apply-to doc: <B>&nbsp;</B></TD>");
        
        //Apply-to-document ID:
        m_pwOut.println("<TD>Apply-to doc ID: <B>&nbsp;</B></TD>");
        
        //END ROW 3:
		m_pwOut.println("</TR>");
		m_pwOut.println("</TABLE>");
	}
	private void Display_Line_Header(PrintWriter m_pwOut){
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2>");
		m_pwOut.println("<TR>");
		m_pwOut.println("<TD><B><U>Apply to Doc #</B></U></TD>");
		m_pwOut.println("<TD><B><U>Apply to Doc ID</B></U></TD>");
		m_pwOut.println("<TD><B><U>GL Account</B></U></TD>");
		m_pwOut.println("<TD><B><U>Amount</B></U></TD>");
		m_pwOut.println("<TD><B><U>Description</B></U></TD>");
		m_pwOut.println("<TD><B><U>Comment</B></U></TD>");
		m_pwOut.println("</TR>");

	}
	private boolean Display_NONEditable_Lines(PrintWriter m_pwOut, AREntryInput m_EntryInput){
		
        //Display the line header:
		Display_Line_Header(m_pwOut);
        for (int i = 0; i < m_EntryInput.getLineCount(); i++){
        	ARLineInput line = m_EntryInput.getLine(i);
        	//Apply to doc #:
        	m_pwOut.println("<TR>");
        	m_pwOut.println("<TD>");
        	m_pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDocAppliedTo())));
        	m_pwOut.println("</TD>");

        	//Apply to doc ID:
        	m_pwOut.println("<TD>");
        	m_pwOut.println(clsStringFunctions.filter(line.getDocAppliedToID()));
        	m_pwOut.println("</TD>");
        	
        	//GL Acct:
        	m_pwOut.println("<TD>");
        	m_pwOut.println(clsStringFunctions.filter(line.getLineAcct()));
        	m_pwOut.println("</TD>");
        	
        	//Amount:
        	m_pwOut.println("<TD ALIGN = RIGHT>");
        	m_pwOut.println(line.getAmount());
        	m_pwOut.println("</TD>");
        	
        	//Description:
        	m_pwOut.println("<TD>");
        	m_pwOut.println(clsStringFunctions.filter(line.getDescription()));
        	m_pwOut.println("</TD>");
        	
        	m_pwOut.println("</TR>");
        }

        m_pwOut.println("</TABLE>");

		return true;
	}
	private boolean displayTransactionLines(PrintWriter m_pwOut, AREntryInput m_EntryInput){

		//  Get the lines by reading the database:
		int iLineIndex = 0;
        for (int i = 0; i < m_EntryInput.getLineCount(); i++){
        	ARLineInput line = m_EntryInput.getLine(iLineIndex);

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDocAppliedTo 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedTo() + "\">");
        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineDocAppliedToID 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedToID() + "\">");

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineAmt 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getAmount() + "\">");

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineApplyToOrderNumber 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getApplyToOrderNumber() + "\">");

        	m_pwOut.println("<TR>");

        	m_pwOut.println("<TD>");
        	m_pwOut.println(ARUtilities.Fill_In_Empty_String_For_HTML_Cell(line.getDocAppliedTo()));
        	m_pwOut.println("</TD>");

        	m_pwOut.println("<TD>");
        	m_pwOut.println(line.getDocAppliedToID());
        	m_pwOut.println("</TD>");
        	
        	m_pwOut.println("<TD>");
        	//The user must pick a GL account for misc cash reversals, but any other kind of 
        	//cash reversal always uses the AR account.  Any other type will already have a
        	//GL account in it, but the line GL account for a misc cash reversal will be blank:
        	if (line.getLineAcct().trim().compareToIgnoreCase("") == 0){
	        	m_pwOut.println("<SELECT NAME = \"" + ARLineInput.ParamDistAcct 
	            		+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) + "\">");
	        	
	        	//add the first line as a default, so we can tell if they didn't pick a GL:
	        	m_pwOut.println("<OPTION");
				m_pwOut.println(" VALUE=\"" + "" + "\">");
				m_pwOut.println(" - Select a distribution GL account - ");
	        	
	            //Read out the array list:
				//System.out.println("In AREditReversalEntry.displayTransactionLines line " + i + " GL Acct = " + line.getLineAcct());
	            for (int iGLCount = 0; iGLCount<m_sGLValues.size();iGLCount++){
	            	m_pwOut.println("<OPTION");
	    			if (m_sGLValues.get(iGLCount).toString().compareToIgnoreCase(line.getLineAcct()) == 0){
	    				m_pwOut.println( " selected=yes");
	    			}
	    			m_pwOut.println(" VALUE=\"" + m_sGLValues.get(iGLCount).toString() + "\">");
	    			m_pwOut.println(m_sGLDescriptions.get(iGLCount).toString());
	            }
	            m_pwOut.println("</SELECT>");
        	}else{
            	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
            			+ ARLineInput.ParamDistAcct
            			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
            			+ "\" VALUE=\"" + line.getLineAcct() + "\">");

            	m_pwOut.println(line.getLineAcct());
        	}
            m_pwOut.println("</TD>");

            //Amount:
            m_pwOut.println("<TD ALIGN = RIGHT>");
            m_pwOut.println(line.getAmount());
            m_pwOut.println("</TD>");

        	//Description:
            m_pwOut.println("<TD>");
            m_pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineDesc 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getDescription()), 
        			25, 
        			"", 
        			""
        			)
        	);
            m_pwOut.println("</TD>");

        	//Comment:
            m_pwOut.println("<TD>");
            m_pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineComment 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getComment()), 
        			25, 
        			"", 
        			""
        			)
        	);
            m_pwOut.println("</TD>");
        	
            m_pwOut.println("</TR>");
        	iLineIndex ++;
        }
		
    	//Record the number of lines:
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamNumberOfLines + "\" VALUE=\"" + Integer.toString(iLineIndex) + "\">");
		
		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
