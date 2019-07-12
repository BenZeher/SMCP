package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;

public class AREntryUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public static final String FIND_GL_CONTROL_ACCT_BUTTON_NAME = "FINDGLEXPENSEACCT";
    public static final String FIND_GL_CONTROL_ACCT_BUTTON_LABEL = "Find GL control account"; 
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
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
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		      + " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    
	    //If there's an entry input object in the session, get rid of it:
	    CurrentSession.removeAttribute("EntryInput");
	    
	    //Instantiate a new entry:
	    AREntry m_Entry = new AREntry();
	    
	    //Collect all the request parameters:
	    String m_sDelete = "";
	    String m_sConfirmDelete = "";
	    String m_sCallingClass = "";
	    boolean bSaveAndAdd = false;
	    String m_sApplyToDocumentID = "";
	    AREntryInput m_EntryInput = null;
	    try {
			m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", request);
			m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", request);
			m_EntryInput = new AREntryInput(request);
			m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
			m_sApplyToDocumentID = clsManageRequestParameters.get_Request_Parameter("DocumentID", request);
			if(request.getParameter("SaveAndAdd") != null){
				bSaveAndAdd = true;
			}
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
					+ "Editable=Yes"
					+ "&BatchNumber=" + m_Entry.sBatchNumber()
					+ "&EntryNumber=" + m_Entry.sEntryNumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=Error reading request data - " + e1.getMessage() + "."
			);
			return;
		}
	    
	    //Load the input parameters into the new entry:
	    //System.out.println("In AREntryUpdate - m_EntryInput = " + m_EntryInput.getDataDump());
	    if (!m_EntryInput.loadToEntry(m_Entry, getServletContext(), sDBID)){
	    	CurrentSession.setAttribute("EntryInput", m_EntryInput);
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
					+ "Editable=Yes"
					+ "&DocumentID=" + m_sApplyToDocumentID
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_EntryInput.getErrorMessageString()
					//+ "&" + m_EntryInput.getQueryString()
			);
	    	return;
	    }
	    	    
		String subtitle = "";
		String title = "";
	
		//If it's a request to 'Find' a GL control account:
		if (request.getParameter(FIND_GL_CONTROL_ACCT_BUTTON_NAME) != null){
			//Then call the finder to search for items:
			CurrentSession.setAttribute("EntryInput", m_EntryInput);
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=" + "ACTIVE " + GLAccount.Paramobjectname
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smar." + m_sCallingClass
				+ "&ReturnField=" + "FOUND" + GLAccount.Paramobjectname
				+ "&SearchField1=" + SMTableglaccounts.sDesc
				+ "&SearchFieldAlias1=Description"
				+ "&SearchField2=" + SMTableglaccounts.sAcctID
				+ "&SearchFieldAlias2=Account%20Number"
				+ "&ResultListField1="  + SMTableglaccounts.sAcctID
				+ "&ResultHeading1=Account%20Number."
				+ "&ResultListField2="  + SMTableglaccounts.sDesc
				+ "&ResultHeading2=Description"
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "*Editable=" + clsManageRequestParameters.get_Request_Parameter("Editable", request)
				+ "*RETURNINGFROMFINDER=TRUE"
				+ m_EntryInput.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				System.out.println("In " + this.toString() 
					+ "In " + this.toString() + " - After FINDGLACCOUNT - error redirecting with string: " 
					+ sRedirectString);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
						+ "Editable=Yes"
						+ "&BatchNumber=" + m_Entry.sBatchNumber()
						+ "&EntryNumber=" + m_Entry.sEntryNumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=Error invoking finder - " + e.getMessage() + "."
				);
				return;
			}
			return;
		}
		
		//Process if it's a 'delete':
		if (!m_sDelete.equalsIgnoreCase("")){
			if (m_sConfirmDelete.equalsIgnoreCase("")){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
						+ "Editable=Yes"
						+ "&BatchNumber=" + m_Entry.sBatchNumber()
						+ "&EntryNumber=" + m_Entry.sEntryNumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=You chose to delete the entry, but did not check the 'Confirm delete' box."
						
				);	
			    return;
			}
			ARBatch batch = new ARBatch(m_Entry.sBatchNumber());
			if (batch.delete_entry_with_transaction(
					m_Entry.sBatchNumber(), 
					m_Entry.sEntryNumber(), 
					getServletContext(), sDBID)){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit?"
						+ "BatchNumber=" + m_Entry.sBatchNumber()
						+ "&BatchType=" + m_Entry.sBatchType()
						+ "&Editable=Yes"						
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning="
				);		
			}else{
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
						+ "Editable=Yes"
						+ "&BatchNumber=" + m_Entry.sBatchNumber()
						+ "&EntryNumber=" + m_Entry.sEntryNumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=Error - Could not delete entry."
				);
			}
			return;
		}
	
		//If it's a new entry:
		if (m_Entry.iEntryNumber() == -1){
			title = "Adding new entry in batch: " + m_EntryInput.getsBatchNumber();
		}else{
			title = "Updating entry: " + m_EntryInput.getsEntryNumber() + " in batch: " + m_EntryInput.getsBatchNumber();
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//If the user has requested to apply any remaining cash to unapplied cash, update the 
		//entry now:
		//DistributeToUnappliedCash
		if (request.getParameter("DistributeToUnappliedCash") != null){
			m_Entry.distributeRemainingAmtToUnappliedCash(getServletContext(), sDBID);
		}
		
		//If the entry cannot be processed, pass the EntryInput object back to the editing screen
		//to advise the user and reload the screen:
		
		//We do not allow zero amount lines here:
		m_Entry.remove_zero_amount_lines();
		try {
			save_entry(getServletContext(), sDBID, sUserFullName, m_Entry);
		} catch (Exception e) {
			CurrentSession.setAttribute("EntryInput", m_EntryInput);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
					+ "Editable=Yes"
					+ "&DocumentID=" + m_sApplyToDocumentID
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=Could not process entry - " + e.getMessage()
				);
			
	        return;
		}
		
	    //If the entry CAN be processed, load the successful entry back into the EntryInput class, and 
	    //allow the entry editor to display it:
			//If it's a cash entry, and the user chose to save and add another entry, link to that here:
			if (bSaveAndAdd){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
						+ "?BatchNumber=" + m_Entry.sBatchNumber()
						+ "&BatchType=" + m_Entry.sBatchType()
						+ "&DocumentType=" + m_Entry.sDocumentType()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);

			}else{
				m_EntryInput.loadFromEntry(m_Entry);
				//Redirect:
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + m_sCallingClass + "?"
						+ "BatchNumber=" + m_Entry.sBatchNumber()
						+ "&EntryNumber=" + m_Entry.sEntryNumber()
						+ "&Editable=Yes"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning="
						//+ "&" + m_EntryInput.getQueryString()
				);

			}
	        return;
	}
	private void save_entry(ServletContext context, String sDBID, String sUserFullName, AREntry m_Entry) throws Exception{
		//Need a connection here for the data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID,
			"MySQL",
			this.toString() + ".save_entry");
		if (conn == null){
			throw new Exception("Could not get connection to save entry.");
		}
		
		try{
			setPostingFlag(
				conn, 
				sUserFullName, 
				"SAVING ENTRY " + m_Entry.iEntryNumber() + " IN BATCH " + m_Entry.iBatchNumber()
			);
		}catch(Exception e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067555]");
			throw new Exception("Could not set AR posting flag.");
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067556]");
			throw new Exception("Could not start data transaction");
		}
		
		if (!m_Entry.save_without_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067557]");
			if (m_Entry.getErrorMessage().compareToIgnoreCase("") == 0){
				throw new Exception("Unspecified error in entry class saving entry");
			}else{
				throw new Exception(m_Entry.getErrorMessage());
			}
		}else{
			clsDatabaseFunctions.commit_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067558]");
			return;
		}
	}

	public void setPostingFlag(Connection conn, String sUserFullName, String sProcess) throws Exception{
	    	//First check to make sure no one else is posting:
	    	try{
	    		String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
	    		ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(SQL, conn);
	    		if (!rsAROptions.next()){
	        		throw new Exception("Error [1548711989] getting aroptions record");
	    		}else{
	    			if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
	    				throw new Exception("A previous posting is not completed - "
	    					+ rsAROptions.getString(SMTablearoptions.suserfullname) + " has been "
	    					+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
	    					+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate) + ".");
	    			}
	    		}
	    		rsAROptions.close();
	    	}catch (SQLException e){
	    		throw new Exception("Error [1548712046] checking for previous posting - " + e.getMessage());
	    	}
	    	//If not, then set the posting flag:
	    	try{
	    		String SQL = "UPDATE " + SMTablearoptions.TableName 
	    			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1"
	    			+ ", " + SMTablearoptions.datstartdate + " = NOW()"
	    			+ ", " + SMTablearoptions.sprocess 
	    				+ " = '" + sProcess + "'"
	       			+ ", " + SMTablearoptions.suserfullname + " = '" + sUserFullName + "'"
	    			;
	    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    			throw new Exception("Error [1548712047] setting posting flag in aroptions"
	    					+ " with SQL = " + SQL + ".");
	    		}
	    	}catch (SQLException e){
	    		throw new Exception("Error [1548712048] setting posting flag in aroptions - " + e.getMessage());
	    	}
	    	return;
	}
	private boolean clearPostingFlag(Connection conn){
		
		try{
    		String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablearoptions.sprocess + " = ''"
   			+ ", " + SMTablearoptions.suserfullname + " = ''"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
        		System.out.println("In AREntryUpdate.clearPostingFlag: "
        				+ "Error clearing posting flag in aroptions");
        		return false;
    		}
    	}catch (SQLException e){
			System.out.println("Error clearing posting flag in aroptions - " + e.getMessage());
    		return false;
    	}
		return true;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}