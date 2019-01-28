package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablearacctset;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AREditMiscReceiptEntry extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = " Misc Receipt Entry";
	
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
		String m_sAccountSet = ARUtilities.get_Request_Parameter("AccountSet", request);
	    
		//Try to load an AREntryInput object from which to build the form:
		if (m_EntryInput == null){
			try {
				loadAREntryInput(
					sDBID,
					sUserID,
					sUserFullName,
					m_sBatchNumber,
					m_sEntryNumber,
					m_sBatchType,
					m_sAccountSet,
					m_bIsNewEntry
				);
			} catch (Exception e) {
	    		m_pwOut.println(
						"<META http-equiv='Refresh' content='0;URL=" 
						+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
						+ "?BatchNumber=" + m_sBatchNumber
	    	    		+ "&BatchType=" + m_sBatchType
	    	    		+ "&DocumentType=" + ARDocumentTypes.MISCRECEIPT_STRING
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

		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!ARCreateEntryForm.createFormFromAREntryInput(
				m_pwOut, 
				m_bEditable, 
				m_EntryInput, 
				getServletContext(), 
				sDBID,
				"AREditMiscReceiptEntry",
				"UNAPPLIED",
				"-1"
		)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_EntryInput.getsBatchType()
					+ "&DocumentType=" + ARDocumentTypes.MISCRECEIPT_STRING
					+ "&Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;
		}

		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private void loadAREntryInput(
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sBatchNumber,
			String sEntryNumber,
			String sBatchType,
			String sAccountSet,
			boolean bIsNewEntry
			) throws Exception{
		
		AREntryInput m_EntryInput = new AREntryInput();
		
		//Have to construct the AREntryInput object here:
		m_EntryInput = new AREntryInput();
		if (bIsNewEntry){
			//If it's a new entry:
			m_EntryInput.setBatchNumber(sBatchNumber);
			m_EntryInput.setBatchType(sBatchType);
			m_EntryInput.setCustomerNumber("");
			//Get the batch date as the default entry date:
			ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
			try {
				batch.load(getServletContext(), sDBID);
				m_EntryInput.setDocDate(batch.sStdBatchDateString());
			} catch (Exception e) {
				m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			}
			m_EntryInput.setDueDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			m_EntryInput.setDocDescription("Misc. receipt");
			m_EntryInput.setDocumentType(ARDocumentTypes.MISCRECEIPT_STRING);
			m_EntryInput.setEntryNumber("-1");
			m_EntryInput.setEntryID("-1");
			m_EntryInput.setOriginalAmount("0.00");
			loadCashAccount(sDBID, sUserID, sUserFullName, m_EntryInput, sAccountSet);
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
		return;
	}

	private boolean loadCashAccount(String sDBID, String sUserID, String sUserFullName, AREntryInput m_EntryInput, String sAccountSet){
		try{
			String SQL = ARSQLs.Get_AcctSet_By_Code(sAccountSet);
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".loadCashAccount - User: " + sUserID
				+ " "
				+ sUserFullName
					);
			if (rs.next()){
				m_EntryInput.setControlAcct(rs.getString(SMTablearacctset.sCashAcct));
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
			
		}catch (SQLException e){
			System.out.println("In " + this.toString() + ".loadCashAccount - SQL error: " + e.getMessage());
			return false;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
