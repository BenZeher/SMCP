package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICAdjustmentLineUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private ICEntryLine m_Line;
	//HttpServletRequest parameters:
	private String m_sSave;
	private String m_sDelete;
	private String m_sConfirmDelete;
	private String m_sUpdateWriteOffAccount;
	private String m_sUpdateCostBuckets;
	private String m_sCallingClass;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sLineNumber;
	private String m_sBatchType;
	private String m_sWarning = "";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

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
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    			+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    m_sWarning = "";
	    
	    //Collect all the request parameters:
	    getRequestParameters(request);
	    
	    //Instantiate a new line:
	    m_Line = new ICEntryLine(request);
	    m_Line.sBatchNumber(m_sBatchNumber);
	    m_Line.sEntryNumber(m_sEntryNumber);
	    m_Line.sLineNumber(m_sLineNumber);
	    //System.out.println("In " + this.toString() + "after getRequestParameters - line dump = " + m_Line.read_out_debug_data());
	    
	    //If there's an entry input object in the session, get rid of it:
	    CurrentSession.removeAttribute("EntryLine");
	    //Update the line object:
	    CurrentSession.setAttribute("EntryLine", m_Line);
	    
	    String sRedirectString = 
			"BatchNumber=" + m_Line.sBatchNumber()
			+ "&EntryNumber=" + m_Line.sEntryNumber()
			+ "&LineNumber=" + m_Line.sLineNumber()
			+ "&BatchType=" + m_sBatchType
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
	    
	    if (m_sUpdateCostBuckets.compareToIgnoreCase("") != 0){
	    	sRedirectString += "&" + ICEditAdjustmentLine.UPDATE_COST_BUCKETS_COMMAND + "=Y";
	    }
	    
	    //First, make sure there is no posting going on:
    	ICOption options = new ICOption();
    	try {
    		
    		String sPostingProcess = "ADJUSTMENT LINE UPDATE";
    		options.checkAndUpdatePostingFlagWithoutConnection(getServletContext(), sDBID, m_sCallingClass, sUserFullName, sPostingProcess);
			
		} catch (Exception e1) {
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
					+ sRedirectString
					+ "&Warning=" + e1.getMessage()
			);
		}
    	
    	updateProcess(CurrentSession, sDBID, sUserID, sUserFullName);
		try {
			options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		} catch (Exception e) {
			//Don't trap this, because the user will just have to reset the posting FLAG - AND we don't want to lose any error message coming back from the called function
		}
		
		response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
				+ sRedirectString
				+ "&Warning=" + m_sWarning
		);
	    
	}
	private void updateProcess(HttpSession CurrentSession, String sDBID, String sUserID, String sUserFullName){
		//Branch here, depending on the request:
	    //If it's a request to update the write off account, update that account:
	    //System.out.println("In " + this.toString() + " 01");
	    if (m_sUpdateWriteOffAccount.compareToIgnoreCase("") != 0){
	    	if (!setDefaultWriteOffAccount(
	    			getServletContext(), 
	    			sDBID, 
	    			sUserID,
	    			sUserFullName,
	    			m_Line.sLocation()
	    		)){
	    		//System.out.println("In " + this.toString() + " 02");
	    	}
	    	
	    	return;
	    }
	    
	    if (m_sUpdateCostBuckets.compareToIgnoreCase("") != 0){
	    	return;
	    }
	    
	    if (m_sDelete.compareToIgnoreCase("") != 0){
	    	if (m_sConfirmDelete.compareToIgnoreCase("") == 0){
	    		m_sWarning = "You chose to delete, but did not check the 'confirming' check box.";
		    	return;
	    	}else{
	    		//Delete the line:
	    		if (!deleteLine(sDBID, sUserID, sUserFullName)){
			    	return;	    			
	    		}
		    	return;
	    	}
	    }

	    if (m_sSave.compareToIgnoreCase("") != 0){
	    	if (!saveLine(
	    			getServletContext(), 
	    			sDBID, 
	    			sUserID,
	    			sUserFullName
	    		)){
	    		//System.out.println("In " + this.toString() + " 02");
	    	}
	    	//Store the saved line in the session
	    	CurrentSession.setAttribute("EntryLine", m_Line);
	    	return;
	    }
	}

	private boolean saveLine(
			ServletContext context, 
			String sDBID,
			String sUserID,
			String sUserFullName
		){
		
		ICEntry entry = new ICEntry();
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), context, sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			return false;
		}
		//System.out.println("In " + this.toString() + ".saveLine: dist acct = " + m_Line.sDistributionAcct());
		//System.out.println("In " + this.toString() + ".saveLine: entry dump - " + entry.read_out_debug_data());
		
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".saveLine - User: " + sUserID
				+ " - "
				+ sUserFullName
				);
		
		if (conn == null){
			m_sWarning = "Could not open data connection to save line.";
			return false;
		}
		
		//Validate the line first in case we can't save it at all:
		if (!entry.validateSingleLine(m_Line, conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080764]");
			return false;
		}
		
		//If it's a new line, just add it to the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){
			//Validate it now so we know that it's going to be in the entry for sure.  We need to 
			//know this so we can get the line ID and line number from the last line after it's
			//saved, and that's only correct if the line is going to be saved.

			if (!entry.add_line(m_Line)){
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080765]");
				return false;
			}
			//If the line was successfully added, update the line number:
			m_Line.sLineNumber(entry.sLastLine());
			//System.out.println("In " + this.toString() + ".saveLine: adding new line - entry dump - " + entry.read_out_debug_data());
		}else{
			boolean bErrorUpdatingEntry = false;
			//If it's an existing line, update it:
			//System.out.println("In " + this.toString() + ".saveLine: updating line - line dump - " + m_Line.read_out_debug_data());
			if (!entry.setLineCostString(m_Line.sId(), m_Line.sCostSTDFormat())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineQtyString(m_Line.sId(), m_Line.sQtySTDFormat())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineItemNumber(m_Line.sId(), m_Line.sItemNumber())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineLocation(m_Line.sId(), m_Line.sLocation())){
				bErrorUpdatingEntry = true;
			}
			//We don't use price on adjustments:
			//if (!entry.setLinePriceString(m_Line.sId(), m_Line.sPriceSTDFormat())){
			//	bErrorUpdatingEntry = true;
			//}
			if (!entry.setLineDescription(m_Line.sId(), m_Line.sDescription())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineComment(m_Line.sId(), m_Line.sComment())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineControlAcct(m_Line.sId(), m_Line.sControlAcct())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineDistributionAcct(m_Line.sId(), m_Line.sDistributionAcct())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineCategory(m_Line.sId(), m_Line.sCategoryCode())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineReceiptNumber(m_Line.sId(), m_Line.sReceiptNum())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineCostBucketID(m_Line.sId(), m_Line.sCostBucketID())){
				bErrorUpdatingEntry = true;
			}
			if (bErrorUpdatingEntry){
				m_sWarning = "Could not save in " + this.toString() + ".saveLine - ";
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080766]");
				return false;
			}
		}

		//System.out.println("In " + this.toString() + ".saveLine: modified line - entry dump - " + entry.read_out_debug_data());
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080767]");
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080768]");
		
		//Reload the line class, now that the line was saved:
		String sBatchNumber = m_Line.sBatchNumber();
		String sEntryNumber = m_Line.sEntryNumber();
		String sLineNumber = "";
		//If it was a new line, populate the line number by getting the last line from the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){
			sLineNumber = entry.sLastLine();
		}else{
			sLineNumber = m_Line.sLineNumber();
		}
		
		//Finally, load the line again into the class:
		m_Line = new ICEntryLine();
		if (!m_Line.load(sBatchNumber, sEntryNumber, sLineNumber, context, sDBID)){
			m_sWarning = m_sWarning + "\n" + m_Line.getErrorMessage();
			return false;
		}
		
		return true;
	}
	private boolean deleteLine(String sDBID, String sUserID, String sUserFullName){
		
		ICEntry entry = new ICEntry(m_Line.sBatchNumber(), m_Line.sEntryNumber());
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), getServletContext(), sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			return false;
		}
		
		//Setting these to zero will cause the entry to drop this line:
		entry.setLineCost(m_Line.sId(), BigDecimal.ZERO);
		entry.setLineQty(m_Line.sId(), BigDecimal.ZERO);
		entry.remove_zero_amount_lines();
		
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), sDBID, "MySQL", this.toString() + ".deleteLine - User: " + sUserID + " - " + sUserFullName);
		
		if (conn == null){
			m_sWarning = "Could not get data connection to delete line.";
			return false;
		}
		
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080762]");
			return false;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080763]");
		return true;
	}
    private boolean setDefaultWriteOffAccount(
    		ServletContext context, 
    		String sDBID, 
    		String sUserID,
    		String sUserFullName,
    		String sLocation
    		){
    	
    	if (sLocation.compareToIgnoreCase("") == 0){
    		m_sWarning = "You chose to set the default write off account, but you have not selected "
    			+ "a location.";
    		return false;
    	}
    	
    	String SQL = "SELECT "
    		+ SMTablelocations.sGLWriteOffAcct
    		+ " FROM " 
    		+ SMTablelocations.TableName
    		+ " WHERE ("
    			+ "(" + SMTablelocations.sLocation + " = '" + sLocation + "')"
    		+ ")"
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				this.toString() + ".setDefaultWriteOffAccount - user: " 
    				+ sUserID
    				+ " - "
    				+ sUserFullName);
    		if (rs.next()){
    			m_Line.sDistributionAcct(rs.getString(SMTablelocations.sGLWriteOffAcct));
    			//System.out.println("In " + this.toString() + ".setDefaultWriteOffAccount = 01");
    		}else{
    			m_sWarning = "No matching account set record.";
    			//System.out.println("In " + this.toString() + ".setDefaultWriteOffAccount = 02");
    			m_Line.sDistributionAcct("");
    		}
    		rs.close();
    	}catch (SQLException e){
    		m_Line.sDistributionAcct("");
    		//System.out.println("In " + this.toString() + ".setDefaultWriteOffAccount = 03");
    		m_sWarning = "SQL Error setting default write off account: " + e.getMessage();
    	}
    	
    	return true;
    }
	private void getRequestParameters(
    	HttpServletRequest req){

		m_sSave = clsManageRequestParameters.get_Request_Parameter("Save", req);
		m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", req);
		m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", req);
		m_sUpdateWriteOffAccount = clsManageRequestParameters.get_Request_Parameter("SubmitWriteOffAccount", req);
		m_sUpdateCostBuckets = clsManageRequestParameters.get_Request_Parameter(ICEditAdjustmentLine.SUBMIT_UPDATE_COST_BUCKETS_COMMAND, req);
		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", req);
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", req);
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", req);
		m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", req);
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", req);
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}