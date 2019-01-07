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
import ServletUtilities.clsStringFunctions;
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

	private static String sObjectName = "Reversal Entry";
	
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sEditable;
	private String m_sBatchType;
	private String m_sDocumentType;
	private String m_sWarning;
	private String m_sApplyToDocumentID;
	private ARTransaction m_Transaction;
	private AREntryInput m_EntryInput;
	private String m_sCustomerARAcct;
	private String m_sCustomerARCustomerDepositAcct;
	private PrintWriter m_pwOut;
	private HttpServletRequest m_hsrRequest;
	private boolean m_bIsNewEntry = false;
	private boolean m_bEditable = false;
	//We'll use these to store the GL List, so we don't have to load it several times:
    private ArrayList<String> m_sGLValues = new ArrayList<String>();
    private ArrayList<String> m_sGLDescriptions = new ArrayList<String>();

	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		m_pwOut = response.getWriter();
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		m_EntryInput = (AREntryInput) CurrentSession.getAttribute("EntryInput");
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		m_hsrRequest = request;
	    get_request_parameters();
	    
		//Try to load an AREntryInput object from which to build the form:
		if (!loadAREntryInput()){
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

		//System.out.println("In " + this.toString() + " AREntryInput Dump:\n" + m_EntryInput.getDataDump());
		
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
		if (!createFormFromAREntryInput(sDBID)){
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
	private boolean loadAREntryInput(){
		
		//If the class has NOT been passed an AREntryInput query string, we'll have to build it:
		if (m_EntryInput == null){

			//Have to construct the AREntryInput object here:
			m_EntryInput = new AREntryInput();
			if (m_bIsNewEntry){
				//If it's a new entry:
				//Load the transaction this adjustment will apply to:
				//Load the transaction this adjustment will apply to:
				if(!ARUtilities.IsValidLong(m_sApplyToDocumentID)){
		    		m_sWarning = "Document ID " + m_sApplyToDocumentID + " is not valid.";
		    		return false;
				}
		    	m_Transaction = new ARTransaction(m_sApplyToDocumentID);
		    	if (! m_Transaction.load(getServletContext(), sDBID)){
		    		m_sWarning = "Could not load transaction with document ID: " + m_sApplyToDocumentID;
		    		m_sWarning += " - " + m_Transaction.getErrorMessage();
		    		return false;
		    	}	    	
		    	
		    	//If it has already been reversed, do not let it be reversed again:
		    	try {
					if (checkForPreviousReversal(m_Transaction.getsTransactionID())){
						m_sWarning = "This document was already reversed and can't be reversed again.";
						return false;
					}
				} catch (SQLException e) {
					m_sWarning = e.getMessage();
					return false;
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
		    		m_sWarning = "Can not reverse " + ARDocumentTypes.Get_Document_Type_Label(m_Transaction.getiDocType()) + ".";
		    		return false;
		    	}
		    	
				m_EntryInput.setBatchNumber(m_sBatchNumber);
				m_EntryInput.setBatchType(m_sBatchType);
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
				m_EntryInput.setDocDescription("Reversed doc ID: " + m_sApplyToDocumentID);
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
					line.setDocAppliedToID(m_sApplyToDocumentID);
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
							m_sWarning = "Could not load customer: " + m_EntryInput.getsCustomerNumber();
							return false;
						}
				    	m_sCustomerARAcct = m_Customer.getARControlAccount(
				    			getServletContext(), sDBID);
				    	m_sCustomerARCustomerDepositAcct = m_Customer.getARPrepayLiabilityAccount(
				    			getServletContext(), sDBID);
				    }
					//For reversals of cash, prepays, and apply-to's, we need to read the lines:
					//Misc receipts won't have any . . . . 
				    if (!loadChildMatchingLines(
				    		m_Transaction.getsTransactionID(), 
				    		m_Transaction.getDocNumber(), 
				    		m_Transaction.getCustomerNumber(),
				    		m_Transaction.getiDocType())
				    		){
				    	return false;
				    }
				    
				    //There may have been unapplied lines on the original entry.  We won't find any armatchinglines
				    //for these, so we have to recreate one line to account for the total of any unapplied lines now:
				    BigDecimal bdCalculatedLineTotal = BigDecimal.ZERO;
				    for (int i = 0; i < m_EntryInput.getLineCount(); i++){
				    	bdCalculatedLineTotal = bdCalculatedLineTotal.add(
				    		ARUtilities.bdStringToBigDecimal(m_EntryInput.getLine(i).getAmount(),2));
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
						line.setComment("Reversing Doc ID " + m_sApplyToDocumentID);
						line.setDescription("Reduces the current amount on the transaction to zero");
						line.setDocAppliedTo(m_Transaction.getDocNumber());
						line.setDocAppliedToID(m_sApplyToDocumentID);
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
				    	m_sWarning = "You are trying to reverse this transaction, but some of the transactions"
				    		+ " to which THIS transaction applied have already "
				    		+ "been cleared from the system - so this transaction can NOT be reversed.";
				    	return false;
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
				if (!entry.load(m_sBatchNumber, m_sEntryNumber, getServletContext(), sDBID)){
			    	m_sWarning = "Could not load entry with batch number " + m_sBatchNumber + ", entry number " + m_sEntryNumber;
			    	m_sWarning += "\n" + entry.getErrorMessage();
			    	return false;
				}
				if (!m_EntryInput.loadFromEntry(entry)){
			    	m_sWarning = "Could not load entry input from entry with batch number " + m_sBatchNumber + ", entry number " + m_sEntryNumber;
			    	return false;
				}
				//System.out.println("In " + this.toString() + ".loadAREntryInput - LOADING FROM EXISTING ENTRY:\n" + m_EntryInput.getDataDump());
			}
		}
		
		return true;
	}
	private boolean createFormFromAREntryInput(String sDBID){
		
		//Include the javascript:
		m_pwOut.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREntryUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm ();

        //Display the entry header fields:
	    displayEntryHeaderFields (sDBID);
        
	    if (!loadGLList()){
	    	return false;
	    }
	    
        if (m_bEditable){
        	displayEditableEntryFields ();
        }
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields ();
        }
        
	    //Now display the transaction lines:
        m_pwOut.println("<B>Line distribution:</B><BR>");
	    
	    //If it's not editable, just show the current applied lines:
	    if (! m_bEditable){
	    	Display_NONEditable_Lines();
	    }else{

	        //Display the line header:
		    Display_Line_Header();

		    //Display all the current transaction lines:
		    if (!displayTransactionLines()){
		    	return false;
		    }	    
		    m_pwOut.println("</TABLE>");
	    }
	    //End the entry edit form:
	    m_pwOut.println("</FORM>");  

		return true;
	}
	private boolean checkForPreviousReversal(String sDocID) throws SQLException{
		
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
	private boolean loadChildMatchingLines(
			String sParentTransactionID, 
			String sdocNumber, 
			String sCustomerNumber,
			int iDocType
			){
		
		//Now we get all of the transactions that the 'to-be-reversed' transaction applied to.  If the
		//to-be-reversed transaction is an apply-to, then we just have to get all of the armatching lines
		//that it included.  But if it's a pre-pay, or a receipt that we're trying to reverse,
		//we only need the matching lines that the to-be-reversed transaction created, but that
		//don't apply back to itself:
		String SQL = "SELECT *" 
			+ " FROM " + SMTablearmatchingline.TableName
			+ " WHERE (";
					if (iDocType == ARDocumentTypes.APPLYTO){
						//If it's an apply-to, we have to get all the lines from the apply-to:
						SQL = SQL + "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
							+ " = " +  sParentTransactionID + ")";
					}else{
						//If it's a receipt or prepay, we only need the lines from the original transaction
						//that were applied to OTHER transactions:
						SQL = SQL +  "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
							+ " = '" +  sdocNumber + "')"
							+ " AND ("+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
							+ " = '" +  sCustomerNumber + "')"
				
							+ " AND ("+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
							+ " != '" +  sdocNumber + "')";
					}
				SQL = SQL + ")"
				+ " ORDER BY " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
				;
			//System.out.println("In AREditReversalEntry.loadChildMatchingLines SQL 1 = " + SQL);
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
	        	ARLineInput line = new ARLineInput();
	        	//We negate the line because we want to do an exact reverse:
	        	line.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsMatchingLines.getBigDecimal(SMTableartransactionline.damount).negate()));
	        	SQL = "SELECT * FROM " + SMTableartransactions.TableName
	        		+ " WHERE ("
	        			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid
	        			+ " = " + Long.toString(rsMatchingLines.getLong(SMTablearmatchingline.ldocappliedtoid))
	        		+ ")"
	        	;
	        	//System.out.println("In AREditReversalEntry.loadChildMatchingLines SQL 2 = " + SQL);
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
		        	line.setDocAppliedTo(rsApplyToTransactions.getString(SMTableartransactions.sdocnumber));
		        	line.setDocAppliedToID(sOriginalApplyToID);
		        	line.setApplyToOrderNumber(rsApplyToTransactions.getString(SMTableartransactions.sordernumber));
		        	line.setComment("Un-applying from ID " + sOriginalApplyToID);
		        	line.setDescription(
		        		"Reverse original application from Doc ID: " + sParentTransactionID 
		        		+ " to Doc ID: " + sOriginalApplyToID);
		        	line.setEntryID("-1");
		        	//This account will always be the customer's AR account:
		        	line.setLineAcct("");
		        	//System.out.println("In AREditReversalEntry.loadChildMatchingLines line GL = " + line.getLineAcct());
	        	}else{
	        		m_sWarning = "Could not load apply-to transaction for doc ID: " + sParentTransactionID;
	        		return false;
	        	}
	        	rsApplyToTransactions.close();
	        	m_EntryInput.addLine(line);
	        }
	        rsMatchingLines.close();

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class: loadChildMatchingLines.");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}

		return true;
	}
	private void storeHiddenFieldsOnForm(){
		
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchNumber + "\" VALUE=\"" + m_EntryInput.getsBatchNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryNumber + "\" VALUE=\"" + m_EntryInput.getsEntryNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamCustomerNumber + "\" VALUE=\"" + m_EntryInput.getsCustomerNumber() + "\">");
	    String sEditable;
	    if (m_bEditable){
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
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "DocumentID" + "\" VALUE=\"" + m_sApplyToDocumentID + "\">");
	    
	}
	private void displayEntryHeaderFields (String sDBID){
		
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

	    if (m_bEditable){
	    	m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
        }
	}
	private void displayEditableEntryFields(){
		
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
	private boolean loadGLList(){
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
	public void displayNonEditableEntryFields (){

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
	private void Display_Line_Header(){
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
	private boolean Display_NONEditable_Lines(){
		
        //Display the line header:
		Display_Line_Header();
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
	private boolean displayTransactionLines(){

		//  Get the lines by reading the database:
		int iLineIndex = 0;
        for (int i = 0; i < m_EntryInput.getLineCount(); i++){
        	ARLineInput line = m_EntryInput.getLine(iLineIndex);

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDocAppliedTo 
        			+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedTo() + "\">");
        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineDocAppliedToID 
        			+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedToID() + "\">");

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineAmt 
        			+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getAmount() + "\">");

        	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineApplyToOrderNumber 
        			+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) 
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
	            		+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) + "\">");
	        	
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
            			+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6) 
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
        				+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6), 
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
        				+ ARUtilities.PadLeft(Integer.toString(iLineIndex), "0", 6), 
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
	private void get_request_parameters(){
 
		if (m_hsrRequest.getParameter("EntryNumber") != null){
			if (ARUtilities.get_Request_Parameter("EntryNumber", m_hsrRequest).equalsIgnoreCase("-1")){
				m_bIsNewEntry = true; 
			}else{
				m_bIsNewEntry = false;
			}
		}else{
			m_bIsNewEntry = false;
			//System.out.println("In " + this.toString() + " - didn't get parameter EntryNumber");
		}

		m_sBatchNumber = ARUtilities.get_Request_Parameter("BatchNumber", m_hsrRequest);
		m_sEntryNumber = ARUtilities.get_Request_Parameter("EntryNumber", m_hsrRequest);
		m_sEditable = ARUtilities.get_Request_Parameter("Editable", m_hsrRequest);
		if (m_sEditable.compareToIgnoreCase("Yes") ==0){
			m_bEditable = true;
		}else {
			m_bEditable = false;
		}
		m_sBatchType = ARUtilities.get_Request_Parameter("BatchType", m_hsrRequest);
		m_sDocumentType = ARUtilities.get_Request_Parameter("DocumentType", m_hsrRequest);
		m_sWarning = ARUtilities.get_Request_Parameter("Warning", m_hsrRequest);
		m_sApplyToDocumentID = ARUtilities.get_Request_Parameter("DocumentID", m_hsrRequest);
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
