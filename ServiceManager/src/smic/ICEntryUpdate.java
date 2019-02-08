package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICEntryUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    
	    //local variables 
	    ICEntry m_Entry;
		String m_sDelete;
		String m_sConfirmDelete;
		String m_sCallingClass;
		
	    //If there's an entry input object in the session, get rid of it:
	    CurrentSession.removeAttribute("EntryInput");
	    CurrentSession.removeAttribute("EntryLine");
	    
	    //Collect all the request parameters:
		m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", request);
		m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", request);
		m_Entry = new ICEntry(request);
		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		String subtitle = "";
		String title = "";
	
		//If the posting flag is set, then don't allow any changes:
    	ICOption options = new ICOption();
    	try {
    		options.checkAndUpdatePostingFlagWithoutConnection(getServletContext(),
			   sDBID, 
			   clsServletUtilities.getFullClassName(this.toString() + ".entryUpdate"), 
			   sUserFullName, 
			   "ENTRY UPDATE");
		} catch (Exception e1) {
			CurrentSession.setAttribute(ICEntry.ParamObjectName, m_Entry);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
				+ "Editable=Yes"
				+ "&BatchNumber=" + m_Entry.sBatchNumber()
				+ "&EntryNumber=" + m_Entry.sEntryNumber()
				+ "&BatchType=" + m_Entry.sBatchType()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e1.getMessage()
			);	
			return;
		}
    	try {
			processUpdate(
					request,
					response,
					out,
					title,
					CurrentSession,
					subtitle, 
					sDBID, 
					sCompanyName, 
					sUserID, 
					m_sDelete, 
					m_sConfirmDelete, 
					m_Entry);
		} catch (Exception e1) {
	    	try {
				options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
			} catch (Exception e) {
			}
	    	CurrentSession.setAttribute(ICEntry.ParamObjectName, m_Entry);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
				+ "Editable=Yes"
				+ "&BatchNumber=" + m_Entry.sBatchNumber()
				+ "&EntryNumber=" + m_Entry.sEntryNumber()
				+ "&BatchType=" + m_Entry.sBatchType()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e1.getMessage()
			);	
			return;
		}
    	
    	try {
			options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		} catch (Exception e) {
		}
    	CurrentSession.setAttribute(ICEntry.ParamObjectName, m_Entry);
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
			+ "Editable=Yes"
			+ "&BatchNumber=" + m_Entry.sBatchNumber()
			+ "&EntryNumber=" + m_Entry.sEntryNumber()
			+ "&BatchType=" + m_Entry.sBatchType()
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&Warning=" + ""
		);	
		return;
	}
	
	private  void processUpdate(
			HttpServletRequest request,
			HttpServletResponse response,
			PrintWriter out,
			String title,
			HttpSession CurrentSession,
			String subtitle,
			String sDBID,
			String sCompanyName,
			String sUserID,
			String m_sDelete,
			String m_sConfirmDelete,
			ICEntry m_Entry) throws Exception {
		//Process if it's a 'delete':
		if (!m_sDelete.equalsIgnoreCase("")){
			if (m_sConfirmDelete.equalsIgnoreCase("")){
				throw new Exception("You chose to delete the entry, but did not check the 'Confirm delete' box.");
			}
			ICEntryBatch batch = new ICEntryBatch(m_Entry.sBatchNumber());
			if (batch.delete_entry_with_transaction(
					m_Entry.sBatchNumber(), 
					m_Entry.sEntryNumber(), 
					getServletContext(), sDBID)){
				return;
			}else{
				throw new Exception("Error [1530897292] - Could not delete entry - " + batch.getErrorMessages()
				);
			}
		}
	
		//If it's an existing entry, try to load the entry here:
		if (m_Entry.sEntryNumber().compareToIgnoreCase("-1") != 0){
			if (!m_Entry.load(getServletContext(), sDBID)){
				throw new Exception("Error [1530897366] - Could not load entry for batch " + m_Entry.sBatchNumber() 
					+ ", entry " + m_Entry.sEntryNumber() + " - " + m_Entry.getErrorMessageString()
				);
			}
		}
		
		//Update the user input fields here:
		m_Entry.sDocNumber(clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamDocNumber, request));
		m_Entry.sEntryDate(clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamEntryDate, request));
		m_Entry.sEntryDescription(clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamEntryDescription, request));
		
		//If it's a new entry:
		if (m_Entry.lEntryNumber() == -1){
			title = "Adding new entry in batch: " + m_Entry.sBatchNumber();
		}else{
			title = "Updating entry: " + m_Entry.sEntryNumber() + " in batch: " + m_Entry.sBatchNumber();
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//System.out.println("In " + this.toString() + " - m_Entry dump: " + m_Entry.read_out_debug_data());
		
		try {
			save_entry(m_Entry, getServletContext(), sDBID, sUserID);
		}catch (Exception e) {
			throw new Exception("Error [1530897648] Could not process entry - " + e.getMessage());
		}

		return;
	}
	
	private void save_entry(ICEntry m_Entry, ServletContext context, String sDBID, String sUserID) throws Exception{
		//Need a connection here for the data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID,
			"MySQL",
			this.toString() + ".save_entry");
		if (conn == null){
			throw new Exception("could not get connection to save entry.");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080861]");
			throw new Exception("could not start data transaction");
		}
		
		//We do not allow zero amount lines here:
		m_Entry.remove_zero_amount_lines();
		
		if (!m_Entry.save_without_data_transaction(conn, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080862]");
			if (m_Entry.getErrorMessage().size() == 0){
				throw new Exception("unspecified error in entry class saving entry");
			}else{
				for (int i = 0; i < m_Entry.getErrorMessage().size();){
					throw new Exception(m_Entry.getErrorMessage().get(i) + "\n");
				}
			}								
		}else{
			clsDatabaseFunctions.commit_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080863]");
		}
	}

	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}