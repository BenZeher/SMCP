package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICTransferLineUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private ICEntryLine m_Line;
	//HttpServletRequest parameters:
	private String m_sSave;
	private String m_sSaveAndAddAnother;
	private String m_sDelete;
	private String m_sConfirmDelete;
	private String m_sDisplayQtys;
	private String m_sCallingClass;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sLineNumber;
	private String m_sBatchType;
	
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
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
	    
	    //Build the redirect string:
	    String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
			+ "BatchNumber=" + m_Line.sBatchNumber()
			+ "&EntryNumber=" + m_Line.sEntryNumber()
			+ "&LineNumber=" + m_Line.sLineNumber()
			+ "&BatchType=" + m_sBatchType
			+ "&" + ICEditTransferLine.DISPLAY_QTYS_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, request)
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
	    
	    //First, make sure there is no posting going on:
    	ICOption options = new ICOption();
    	try {
    		options.checkAndUpdatePostingFlagWithoutConnection(getServletContext(), sDBID, m_sCallingClass, sUserFullName, "TRANSFER LINE UPDATE");
		} catch (Exception e1) {
	    	CurrentSession.setAttribute("EntryLine", m_Line);
	    	response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
    			+ "BatchNumber=" + m_Line.sBatchNumber()
    			+ "&EntryNumber=" + m_Line.sEntryNumber()
    			+ "&LineNumber=" + m_Line.sLineNumber()
    			+ "&BatchType=" + m_sBatchType
    			+ "&" + ICEditTransferLine.DISPLAY_QTYS_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, request)
    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    			+ "&Warning=" + e1.getMessage())
	    	;
	    	return;
		}
    	try {
			processTransferLine(CurrentSession,response);
		} catch (Exception e1) {
	    	try {
				options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
			} catch (Exception e) {
			}
	    	CurrentSession.setAttribute("EntryLine", m_Line);
	    	response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
    			+ "BatchNumber=" + m_Line.sBatchNumber()
    			+ "&EntryNumber=" + m_Line.sEntryNumber()
    			+ "&LineNumber=" + m_Line.sLineNumber()
    			+ "&BatchType=" + m_sBatchType
    			+ "&" + ICEditTransferLine.DISPLAY_QTYS_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, request)
    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    			+ "&Warning=" + e1.getMessage())
	    	;
	    	return;
		}
    	try {
			options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		} catch (Exception e) {
		}
	    
    	if (m_sSaveAndAddAnother.compareToIgnoreCase("") != 0){
    		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
				+ "BatchNumber=" + m_Line.sBatchNumber()
				+ "&EntryNumber=" + m_Line.sEntryNumber()
				+ "&LineNumber=" + "-1"
				+ "&BatchType=" + m_sBatchType
				+ "&" + ICEditTransferLine.DISPLAY_QTYS_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, request)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		;
    	}else{
    		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
				+ "BatchNumber=" + m_Line.sBatchNumber()
				+ "&EntryNumber=" + m_Line.sEntryNumber()
				+ "&LineNumber=" + m_Line.sLineNumber()
				+ "&BatchType=" + m_sBatchType
				+ "&" + ICEditTransferLine.DISPLAY_QTYS_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, request)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			;
    	}
    	response.sendRedirect(sRedirectString + "&Status=" + "Line updated successfully");
    	return;
	
	}
	private void processTransferLine(HttpSession CurrentSession, HttpServletResponse response) throws Exception{
	    //If it's a request to display the qtys:
	    if (m_sDisplayQtys.compareToIgnoreCase("") != 0){
	    	if (m_Line.sItemNumber().compareToIgnoreCase("") == 0){
	    		throw new Exception("Error [1531847481] - Can't look up quantities - item number is blank");
	    	}
	    	return;
	    }
	    
	    //If it's a request to delete:
	    if (m_sDelete.compareToIgnoreCase("") != 0){
	    	if (m_sConfirmDelete.compareToIgnoreCase("") == 0){
	    		throw new Exception("You chose to delete, but did not check the 'confirming' check box.");
	    	}else{
	    		//Delete the line:
	    		try {
					deleteLine();
				} catch (Exception e) {
					throw new Exception("Error [1531849292] - " + e.getMessage());
				}
	    	}
	    	CurrentSession.removeAttribute("EntryLine");
	    	return;
	    }

	    if (m_sSave.compareToIgnoreCase("") != 0){
	    	try {
				saveLine(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName
				);
			} catch (Exception e) {
				throw new Exception("Error [1531849212] - " + e.getMessage());
			}
	    	CurrentSession.removeAttribute("EntryLine");
	    	return;
	    }
	    
	    //If it's a request to save AND add another line:
	    if (m_sSaveAndAddAnother.compareToIgnoreCase("") != 0){
	    	try {
				saveLine(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName
				);
			} catch (Exception e) {
				throw new Exception("Error [1531849213] - " + e.getMessage());
			}
	    	CurrentSession.removeAttribute("EntryLine");
	    	return;
	    }

	}
	private void saveLine(
			ServletContext context, 
			String sConf, 
			String sUserID,
			String sUseFullName
		) throws Exception{
		
		String sWarning = "";
		
		ICEntry entry = new ICEntry();
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), context, sConf)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning += "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1531849443] saving line - " + sWarning);
		}
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sConf, 
					"MySQL", 
					this.toString() + ".saveLine - User: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
		} catch (Exception e) {
			throw new Exception("Error [1531849444] opening connection to save line - " + e.getMessage());
		}
		
		//Validate the line first in case we can't save it at all:
		if (!entry.validateSingleLine(m_Line, conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning += "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081000]");
			throw new Exception("Error [1531849445] validating line - " + sWarning);
		}
		
		//If it's a new line, just add it to the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){
			//Validate it now so we know that it's going to be in the entry for sure.  We need to 
			//know this so we can get the line ID and line number from the last line after it's
			//saved, and that's only correct if the line is going to be saved.
			if (!entry.add_line(m_Line)){
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					sWarning += "\n" + entry.getErrorMessage().get(i);
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081001]");
				throw new Exception("Error [1531849446] adding line - " + sWarning);
			}
			//If the line was successfully added, update the line number:
			m_Line.sLineNumber(entry.sLastLine());
			//System.out.println("In " + this.toString() + ".saveLine: adding new line - entry dump - " + entry.read_out_debug_data());
		}else{
			boolean bErrorUpdatingEntry = false;
			//If it's an existing line, update it:
			//System.out.println("In " + this.toString() + ".saveLine: updating line - line dump - " + m_Line.read_out_debug_data());
			
			//We don't use cost on transfers:
			//if (!entry.setLineCostString(m_Line.sId(), m_Line.sCostSTDFormat())){
			//	bErrorUpdatingEntry = true;
			//}
			if (!entry.setLineQtyString(m_Line.sId(), m_Line.sQtySTDFormat())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineItemNumber(m_Line.sId(), m_Line.sItemNumber())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineLocation(m_Line.sId(), m_Line.sLocation())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLineTargetLocation(m_Line.sId(), m_Line.sTargetLocation())){
				bErrorUpdatingEntry = true;
			}
			if (!entry.setLinePriceString(m_Line.sId(), m_Line.sPriceSTDFormat())){
				bErrorUpdatingEntry = true;
			}
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
			//We don't use cost bucket IDs on transfers:
			//if (!entry.setLineCostBucketID(m_Line.sId(), m_Line.sCostBucketID())){
			//	bErrorUpdatingEntry = true;
			//}
			if (bErrorUpdatingEntry){
				sWarning = "Could not save in " + this.toString() + ".saveLine - ";
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					sWarning += "\n" + entry.getErrorMessage().get(i);
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081002]");
				throw new Exception("Error [1531849447] updating line - " + sWarning);
			}
		}

		//System.out.println("In " + this.toString() + ".saveLine: price = " + m_Line.sPriceSTDFormat());
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning += "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081003]");
			throw new Exception("Error [1531849448] saving line - " + sWarning);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547081004]");
		
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
		if (!m_Line.load(sBatchNumber, sEntryNumber, sLineNumber, context, sConf)){
			sWarning += "\n" + m_Line.getErrorMessage();
			throw new Exception("Error [1531849449] reloading saved line - " + sWarning);
		}
		
		return;
	}
	private void deleteLine() throws Exception{
		
		String sWarning = "";
		ICEntry entry = new ICEntry(m_Line.sBatchNumber(), m_Line.sEntryNumber());
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), getServletContext(), sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning += "\n" + entry.getErrorMessage().get(i);
			}
			throw new Exception("Error [1531849042] - " + sWarning);
		}
		
		//Setting these to zero will cause the entry to drop this line:
		entry.setLineCost(m_Line.sId(), BigDecimal.ZERO);
		entry.setLineQty(m_Line.sId(), BigDecimal.ZERO);
		entry.remove_zero_amount_lines();
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), sDBID, "MySQL", this.toString() + ".deleteLine - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
					);
		} catch (Exception e) {
			throw new Exception("Error [1531849043] getting connection - " + e.getMessage());
		}
		
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning += "\n" + entry.getErrorMessage().get(i);
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080998]");
			throw new Exception("Error [1531849044] deleting line - " + sWarning);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080999]");
		
		return;
	}
 	private void getRequestParameters(
    	HttpServletRequest req){

		m_sSave = clsManageRequestParameters.get_Request_Parameter("Save", req);
		m_sSaveAndAddAnother = clsManageRequestParameters.get_Request_Parameter("SaveAndAddAnother", req);
		m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", req);
		m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", req);
		m_sDisplayQtys = clsManageRequestParameters.get_Request_Parameter(ICEditTransferLine.DISPLAY_QTYS_PARAM, req);
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