package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditRetainageEntry extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Retainage Entry";

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
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
	    AREntryInput m_EntryInput = (AREntryInput) CurrentSession.getAttribute("EntryInput");
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		boolean m_bIsNewEntry = false;
		if (request.getParameter("EntryNumber") != null){
			if (clsManageRequestParameters.get_Request_Parameter("EntryNumber", request).equalsIgnoreCase("-1")){
				m_bIsNewEntry = true; 
			}else{
				m_bIsNewEntry = false;
			}
		}else{
			m_bIsNewEntry = false;
			//System.out.println("In " + this.toString() + " - didn't get parameter EntryNumber");
		}

		String m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", request);
		String m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", request);
		String m_sEditable = clsManageRequestParameters.get_Request_Parameter("Editable", request);
		boolean m_bEditable = false;
		if (m_sEditable.compareToIgnoreCase("Yes") ==0){
			m_bEditable = true;
		}
		String m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", request);
		String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String m_sApplyToDocumentID = clsManageRequestParameters.get_Request_Parameter("DocumentID", request);
		
		String m_sRetainagePercentage = clsManageRequestParameters.get_Request_Parameter("RetainagePercentage", request);
		if(!clsValidateFormFields.IsValidBigDecimal(m_sRetainagePercentage, 2)){
			m_sRetainagePercentage = "0.00";
		}
	    
		//Try to load an AREntryInput object from which to build the form:
		if(m_EntryInput == null){
			try {
				m_EntryInput = loadAREntryInput(
					sDBID,
					m_sBatchNumber,
					m_sEntryNumber,
					m_sBatchType,
					m_bIsNewEntry,
					m_sApplyToDocumentID,
					m_sRetainagePercentage
				);
			} catch (Exception e) {
	    		m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
						+ "?BatchNumber=" + m_sBatchNumber
						+ "&BatchType=" + m_sBatchType
						+ "&DocumentType=" + ARDocumentTypes.RETAINAGE_STRING
						+ "&DocumentID="
						+ "&Warning=" + e.getMessage()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "'>"		
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

		//Try to construct the rest of the screen from from the AREntryInput object:
		String sappliedtotransactiondocnumber = "";
		try {
			sappliedtotransactiondocnumber = getApplyToTransactionDocNumber(m_sApplyToDocumentID, getServletContext(), sDBID);
		} catch (Exception e) {
			//Let this drop:
		}
		
		if (!ARCreateEntryForm.createFormFromAREntryInput(
				m_pwOut, 
				m_bEditable, 
				m_EntryInput, 
				getServletContext(), 
				sDBID,
				"AREditRetainageEntry",
				sappliedtotransactiondocnumber,
				m_sApplyToDocumentID
		)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_EntryInput.getsBatchType()
					+ "&DocumentType=" + ARDocumentTypes.RETAINAGE_STRING
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
			String sBatchNumber,
			String sEntryNumber,
			String sBatchType,
			boolean bIsNewEntry,
			String sApplyToDocumentID,
			String sRetainagePercentage
			) throws Exception{
		
		AREntryInput m_EntryInput = new AREntryInput();

		//Have to construct the AREntryInput object here:
		m_EntryInput = new AREntryInput();
		if (bIsNewEntry){
			//If it's a new entry:
			//Load the transaction this retainage will apply to:
			if(!clsValidateFormFields.IsValidLong(sApplyToDocumentID)){
	    		throw new Exception("Document ID '" + sApplyToDocumentID + "' is not valid.");
			}
			ARTransaction m_Transaction = new ARTransaction(sApplyToDocumentID);
	    	if (! m_Transaction.load(getServletContext(), sDBID)){
	    		throw new Exception("Could not load transaction with document ID: " + sApplyToDocumentID
	    				+ " - " + m_Transaction.getErrorMessage());
	    	}	    	
			m_EntryInput.setBatchNumber(sBatchNumber);
			m_EntryInput.setBatchType(sBatchType);
			m_EntryInput.setCustomerNumber(m_Transaction.getCustomerNumber());
			//Get the batch date as the default entry date:
			ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
			try {
				batch.load(getServletContext(), sDBID);
				m_EntryInput.setDocDate(batch.sStdBatchDateString());
			} catch (Exception e) {
				m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			}
			m_EntryInput.setDueDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			m_EntryInput.setDocDescription("Applied to doc ID: " + sApplyToDocumentID);
			m_EntryInput.setDocumentType(ARDocumentTypes.RETAINAGE_STRING);
			m_EntryInput.setEntryNumber("-1");
			m_EntryInput.setEntryID("-1");
			
			//Get the terms for this customer:
			ARCustomer cus = new ARCustomer(m_Transaction.getCustomerNumber());
			if(cus.load(getServletContext(), sDBID)){
				m_EntryInput.setTerms(cus.getM_sTerms());
			}
			
			//For retainage, the control account is the retainage acct:
			m_EntryInput.setControlAcct(cus.getARRetainageAccount(getServletContext(), sDBID));
			
			//Calculate the retainage amount based on the retainage percentage:
			BigDecimal bdRetPercent = new BigDecimal(sRetainagePercentage);
			BigDecimal bdOneHundred = new BigDecimal(100);
			BigDecimal bdOriginalAmt = m_Transaction.getdOriginalAmount();
			BigDecimal bdRetainageAmt = bdOriginalAmt.multiply(bdRetPercent.divide(bdOneHundred));
			m_EntryInput.setOriginalAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdRetainageAmt));
			m_EntryInput.setTerms(m_Transaction.getTerms());
			m_EntryInput.setOrderNumber(m_Transaction.getOrderNumber());
			
			//Now we need to create one single child line, which applies against the original invoice:
        	ARLineInput line = new ARLineInput();
        	//Lines on retainage need to be positive, since they act like cash lines, applying against
        	//the original invoice:
        	line.setAmount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_Transaction.getdOriginalAmount()));
        	//These should apply to the 'apply-to doc, because they reduce the balance on that invoice:
        	line.setDocAppliedTo(m_Transaction.getDocNumber());
        	line.setDocAppliedToID(sApplyToDocumentID);
        	
        	//The distribution account should be the account on the entry, normally the AR account:
        	line.setLineAcct(m_Transaction.getControlAcct());
        	//System.out.println("In " + this.toString() + ".loadAREntryInput - dist line acct = " + line.getLineAcct());
        	m_EntryInput.addLine(line);
        	//System.out.println("in " + this.toString() + " - line was added; line aplytoid = " + m_EntryInput.getLine(0).getDocAppliedToID());
		}else{
			//If it's an existing entry:
			//Load the existing entry:
			AREntry entry = new AREntry();
			entry = new AREntry();
			if (!entry.load(sBatchNumber, sEntryNumber, getServletContext(), sDBID)){
		    	throw new Exception("Could not load entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber
		    	+  "\n" + entry.getErrorMessage());
			}
			if (!m_EntryInput.loadFromEntry(entry)){
		    	throw new Exception("Could not load entry input from entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber);
			}
		}
		return m_EntryInput;
	}
	private String getApplyToTransactionDocNumber(String  sTransactionID, ServletContext context, String sDBID) throws Exception{
		String sDocNumber = "";
		String SQL = "SELECT " + SMTableartransactions.sdocnumber
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableartransactions.lid + " = " + sTransactionID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				clsServletUtilities.getFullClassName(this.toString() + ".getApplyToTransaction")
			);
			if (rs.next()){
				sDocNumber = rs.getString(SMTableartransactions.sdocnumber);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1548703757] getting apply to transaction document number - " + e.getMessage());
		}
		
		return sDocNumber;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
