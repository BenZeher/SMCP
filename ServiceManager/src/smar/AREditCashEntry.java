package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;

public class AREditCashEntry extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String sObjectName = "Cash Entry";
	
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
			if (ARUtilities.get_Request_Parameter("EntryNumber", request).equalsIgnoreCase("-1")){
				m_bIsNewEntry = true; 
			}
		}

		String m_sBatchNumber = ARUtilities.get_Request_Parameter("BatchNumber", request);
		String m_sEntryNumber = ARUtilities.get_Request_Parameter("EntryNumber", request);
		String m_sEditable = ARUtilities.get_Request_Parameter("Editable", request);
		boolean m_bEditable = false;
		if (m_sEditable.compareToIgnoreCase("Yes") ==0){
			m_bEditable = true;
		}
		String m_sBatchType = ARUtilities.get_Request_Parameter("BatchType", request);
		String m_sWarning = ARUtilities.get_Request_Parameter("Warning", request);
		String m_sCustomerNumber = ARUtilities.get_Request_Parameter("CustomerNumber", request);
		String m_sDocumentType = ARUtilities.get_Request_Parameter("DocumentType", request);
	    
	    //If we are returning from finding an expense account, update that account and account info:
	    if (request.getParameter("FOUND" + GLAccount.Paramobjectname) != null){
	    	m_EntryInput.setControlAcct((request.getParameter("FOUND" + GLAccount.Paramobjectname)));
	    }
	    
		//Try to load an AREntryInput object from which to build the form:
	    if (m_EntryInput == null){
		    try {
		    	m_EntryInput = loadAREntryInput(
						sDBID,
						m_sBatchNumber,
						m_sEntryNumber,
						m_sBatchType,
						m_sDocumentType,
						m_sCustomerNumber,
						m_bIsNewEntry)
				;
			} catch (Exception e) {
	    		m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
						+ "?BatchNumber=" + m_sBatchNumber
						+ "&BatchType=" + m_sBatchType
						+ "&DocumentType=" + m_sDocumentType
						+ "&DocumentID="
						+ "&Warning=" + m_sWarning
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

		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!ARCreateEntryForm.createFormFromAREntryInput(
				m_pwOut, 
				m_bEditable, 
				m_EntryInput, 
				getServletContext(), 
				sDBID,
				"AREditCashEntry",
				"",
				"-1"
		)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_EntryInput.getsBatchType()
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
		String sBatchNumber,
		String sEntryNumber,
		String sBatchType,
		String sDocumentType,
		String sCustomerNumber,
		boolean bIsNewEntry) throws Exception{
		
		AREntryInput m_EntryInput = new AREntryInput();
		
		//Have to construct the AREntryInput object here:
		m_EntryInput = new AREntryInput();
		if (bIsNewEntry){
			//If it's a new entry:
			m_EntryInput.setBatchNumber(sBatchNumber);
			m_EntryInput.setBatchType(sBatchType);
			m_EntryInput.setDocumentType(sDocumentType);
			m_EntryInput.setCustomerNumber(sCustomerNumber);
			
			ARCustomer cust = new ARCustomer(sCustomerNumber);
			if (!cust.load(getServletContext(), sDBID)){
				throw new Exception("Could not load customer " + sCustomerNumber);
			}
			m_EntryInput.setControlAcct(cust.getCashAccount(getServletContext(), sDBID));
			//Get the batch date as the default entry date:
			ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
			try {
				batch.load(getServletContext(), sDBID);
				m_EntryInput.setDocDate(batch.sStdBatchDateString());
			} catch (Exception e) {
				m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			}
			
			if (m_EntryInput.getsDocumentType().equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
				m_EntryInput.setDocDescription("Cash entry");
			}else{
				m_EntryInput.setDocDescription("Prepay");
			}
			
			m_EntryInput.setEntryNumber("-1");
			m_EntryInput.setEntryID("-1");
			m_EntryInput.setOriginalAmount("0.00");
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
		}
		return m_EntryInput;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
