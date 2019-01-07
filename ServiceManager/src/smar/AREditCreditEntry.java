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

public class AREditCreditEntry extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static String sObjectName = "Entry";
	
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sEditable;
	private String m_sBatchType;
	private String m_sWarning;
	private String m_sApplyToDocumentNumber;
	private String m_sApplyToDocumentID;
	private ARTransaction m_Transaction;
	private AREntryInput m_EntryInput;
	private HttpServletRequest m_hsrRequest;
	private boolean m_bIsNewEntry = false;
	private boolean m_bEditable = false;

	private static String sDBID = "";
	private static String sCompanyName = "";
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName  = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		m_EntryInput = (AREntryInput) CurrentSession.getAttribute("EntryInput");
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		m_hsrRequest = request;
	    get_request_parameters();
	    
		//Try to load an AREntryInput object from which to build the form:
		if (!loadAREntryInput()){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
					+ "?BatchNumber=" + m_sBatchNumber
					+ "&BatchType=" + m_sBatchType
					+ "&DocumentType=" + ARDocumentTypes.CREDIT_STRING
					+ "&DocumentID=" + m_sApplyToDocumentID
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
					+ "'>"		
				);
			return;
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
				"AREditCreditEntry",
				m_sApplyToDocumentNumber,
				m_sApplyToDocumentID
		)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_EntryInput.getsBatchType()
					+ "&DocumentType=" + ARDocumentTypes.CREDIT_STRING
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
				m_EntryInput.setBatchNumber(m_sBatchNumber);
				m_EntryInput.setBatchType(m_sBatchType);
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
		    	m_sApplyToDocumentNumber = m_Transaction.getDocNumber();
				m_EntryInput.setCustomerNumber(m_Transaction.getCustomerNumber());
				m_EntryInput.setControlAcct(m_Transaction.getControlAcct());
				//Get the batch date as the default entry date:
				ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
				try {
					batch.load(getServletContext(), sDBID);
					m_EntryInput.setDocDate(batch.sStdBatchDateString());
				} catch (Exception e) {
					m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
				}
				m_EntryInput.setDocDescription("Credited doc ID: " + m_sApplyToDocumentID);
				m_EntryInput.setDocumentType(ARDocumentTypes.CREDIT_STRING);
				m_EntryInput.setEntryNumber("-1");
				m_EntryInput.setEntryID("-1");
				m_EntryInput.setOriginalAmount(m_Transaction.getOriginalAmountSTDFormat());
				m_EntryInput.setOrderNumber(m_Transaction.getOrderNumber());
			    //if (!loadChildMatchingLines()){
			    //	m_sWarning = "Could not load transaction lines for document ID: " + m_sApplyToDocumentID;
			    //	return false;
			    //}
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
			}
		}
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
		m_sWarning = ARUtilities.get_Request_Parameter("Warning", m_hsrRequest);
		m_sApplyToDocumentID = ARUtilities.get_Request_Parameter("DocumentID", m_hsrRequest);
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
