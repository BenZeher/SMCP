package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPhysicalCountLineUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditPhysicalInventory))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID); //$NON-NLS-1$
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	   
	    String m_sSendRedirect = "";
	    String m_sStatus = ""; 
	    //Collect all the request parameters:
	    String m_sSave = clsManageRequestParameters.get_Request_Parameter("Save", request); //$NON-NLS-1$
	    String m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", request); //$NON-NLS-1$
	    String m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", request); //$NON-NLS-1$
	    String m_sUpdateDefaultWriteOffAccount = clsManageRequestParameters.get_Request_Parameter("SubmitDefaultWriteOffAccount", request); //$NON-NLS-1$
	    String m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request); //$NON-NLS-1$
	    String m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", request); //$NON-NLS-1$
	    String m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", request); //$NON-NLS-1$
	    String m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", request); //$NON-NLS-1$
	    String m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", request); //$NON-NLS-1$
	    
	    //Instantiate a new line:
	    ICEntryLine m_Line = new ICEntryLine(request);
	    m_Line.sBatchNumber(m_sBatchNumber);
	    m_Line.sEntryNumber(m_sEntryNumber);
	    m_Line.sLineNumber(m_sLineNumber);
	    //System.out.println("In " + this.toString() + "after getRequestParameters - line dump = " + m_Line.read_out_debug_data());
	    
	    //If there's an entry input object in the session, get rid of it:
	    CurrentSession.removeAttribute("EntryLine"); //$NON-NLS-1$
	    //Update the line object:
	    CurrentSession.setAttribute("EntryLine", m_Line); //$NON-NLS-1$
	    
	    //First, make sure there is no posting going on:
    	ICOption options = new ICOption();
    	try {
    		options.checkAndUpdatePostingFlagWithoutConnection(
    				getServletContext(), 
    				sDBID, 
    				m_sCallingClass, 
    				sUserFullName, 
    				"PHYSICALCOUNTLINEUPDATE"
    				);	
    	} catch (Exception e1) {
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?"
					+ "BatchNumber=" + m_Line.sBatchNumber()
					+ "&EntryNumber=" + m_Line.sEntryNumber()
					+ "&LineNumber=" + m_Line.sLineNumber()
					+ "&BatchType=" + m_sBatchType
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e1.getMessage()
			);
	    	return;
		}

    	//Process the request
    	try {
    		process(CurrentSession,
    				response, 
    				sDBID, 
    				sUserID, 
    				sUserFullName, 
    				m_sUpdateDefaultWriteOffAccount, 
    				m_sDelete, 
    				m_sConfirmDelete, 
    				m_sSave, 
    				m_Line
    				);
    		if(m_sDelete.compareToIgnoreCase("") != 0) {
    			m_sStatus = "Successfully deleted line " + m_Line.sLineNumber();
    		}
    		if(m_sSave.compareToIgnoreCase("") != 0) {
    			m_sStatus = "Successfully added line " + m_Line.sLineNumber();
    		}
    		//If updating the write off account or any error redirect to same screen. Otherwise go back to entry screen. 
    		if(m_sUpdateDefaultWriteOffAccount.compareToIgnoreCase("") != 0) {
    			m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" 
    					+ "BatchNumber=" + m_Line.sBatchNumber() 
    					+ "&EntryNumber=" + m_Line.sEntryNumber() 
    					+ "&LineNumber=" + m_Line.sLineNumber() 
    					+ "&BatchType=" + m_sBatchType 
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID ;
    		}else {
    		
    			m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry" + "?" 
    				+ "BatchNumber=" + m_Line.sBatchNumber()
    				+ "&EntryNumber=" + m_Line.sEntryNumber() 
    				+ "&BatchType=" + m_sBatchType 
    				+ "&Editable=Yes"
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				+ "&Status=" + clsServletUtilities.URLEncode(m_sStatus);
    		}
   
    	}catch (Exception e) {
    		m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" 
					+ "BatchNumber=" + m_Line.sBatchNumber() 
					+ "&EntryNumber=" + m_Line.sEntryNumber() 
					+ "&LineNumber=" + m_Line.sLineNumber() 
					+ "&BatchType=" + m_sBatchType 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage()); 
    	}
    	
    	try{
        	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
    	}catch(Exception e){

    	}
    	response.sendRedirect(m_sSendRedirect);
	    
	}
	
	
	private void process( 
			HttpSession CurrentSession, 
			HttpServletResponse response, 
			String sDBID, 
			String sUserID, 
			String sUserFullName,
			String m_sUpdateDefaultWriteOffAccount,
			String m_sDelete,
			String m_sConfirmDelete,
			String m_sSave,
			ICEntryLine m_Line) throws Exception{

	//If it's a request to update the write off account, update that account:
	if (m_sUpdateDefaultWriteOffAccount.compareToIgnoreCase("") != 0){ 
    	try {
    		setDefaultWriteOffAccount(
        			getServletContext(), 
        			sDBID, 
        			sUserFullName,
        			m_Line.sLocation(),
        			m_Line);
    	}catch (Exception e){
    		throw new Exception(e.getMessage());
    	}
    	return;
    }
	//If it's a request to delete.
    if (m_sDelete.compareToIgnoreCase("") != 0){ 
    	if (m_sConfirmDelete.compareToIgnoreCase("") == 0){ 
    		throw new Exception("Confirm delete checkbox is not selected."); 

    	}else{
    		try {
    			deleteLine(sDBID, sUserID, sUserFullName, m_Line);
    		}catch(Exception e) {
    			throw new Exception("Error deleting. ");
    		}
    	}
    }
    //If its a request to save.
    if (m_sSave.compareToIgnoreCase("") != 0){ //$NON-NLS-1$
	    	
    	try {
    		saveLine(
    			getServletContext(), 
		    	sDBID, 
		    	sUserID,
		    	sUserFullName,
		    	m_Line
		    	);
	    	CurrentSession.setAttribute("EntryLine", m_Line); //$NON-NLS-1$
		    return;
	    }catch (Exception e){
	    	throw new Exception(e.getMessage());
	    }
	    	
	   }
	}
	
	private void saveLine(
			ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName,
			ICEntryLine m_Line
		) throws Exception{
		String sWarning = "";
		ICEntry entry = new ICEntry();
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), context, sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			throw new Exception(sWarning);
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL",  //$NON-NLS-1$
				this.toString() + ".saveLine - User: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						//$NON-NLS-1$
				);
		
		if (conn == null){
			sWarning = Messages.getString("ICPhysicalCountLineUpdate.61"); //$NON-NLS-1$
			throw new Exception(sWarning);
		}
		
		//Validate the line first in case we can't save it at all:
		if (!entry.validateSingleLine(m_Line, conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080886]");
			throw new Exception(sWarning);
		}
		
		//If it's a new line, just add it to the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){ //$NON-NLS-1$
			//Validate it now so we know that it's going to be in the entry for sure.  We need to 
			//know this so we can get the line ID and line number from the last line after it's
			//saved, and that's only correct if the line is going to be saved.

			if (!entry.add_line(m_Line)){
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080887]");
				throw new Exception(sWarning);
			}
			//If the line was successfully added, update the line number:
			m_Line.sLineNumber(entry.sLastLine());
			//System.out.println("In " + this.toString() + ".saveLine: adding new line - entry dump - " + entry.read_out_debug_data());
		}else{
			boolean bErrorUpdatingEntry = false;
			//If it's an existing line, update it:
			//System.out.println("In " + this.toString() + ".saveLine: updating line - line dump - " + m_Line.read_out_debug_data());
			
			//We don't use cost on physical counts:
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
			//We don't use cost bucket IDs on physical counts:
			//if (!entry.setLineCostBucketID(m_Line.sId(), m_Line.sCostBucketID())){
			//	bErrorUpdatingEntry = true;
			//}
			if (bErrorUpdatingEntry){
				sWarning = Messages.getString("ICPhysicalCountLineUpdate.65") + this.toString() + ".saveLine - "; //$NON-NLS-1$ //$NON-NLS-2$
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080888]");
				throw new Exception(sWarning);
			}
		}

		//System.out.println("In " + this.toString() + ".saveLine: price = " + m_Line.sPriceSTDFormat());
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080889]");
			throw new Exception(sWarning);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080890]");
		
		//Reload the line class, now that the line was saved:
		String sBatchNumber = m_Line.sBatchNumber();
		String sEntryNumber = m_Line.sEntryNumber();
		String sLineNumber = ""; //$NON-NLS-1$
		//If it was a new line, populate the line number by getting the last line from the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){ //$NON-NLS-1$
			sLineNumber = entry.sLastLine();
		}else{
			sLineNumber = m_Line.sLineNumber();
		}
		
		//Finally, load the line again into the class:
		m_Line = new ICEntryLine();
		if (!m_Line.load(sBatchNumber, sEntryNumber, sLineNumber, context, sDBID)){
			sWarning = sWarning + "\n" + m_Line.getErrorMessage(); //$NON-NLS-1$
			throw new Exception(sWarning);
		}
	}
	private void deleteLine(
			String sDBID,
			String sUserID, 
			String sUserFullName,
			ICEntryLine m_Line) throws Exception{
		String sWarning = "";
		ICEntry entry = new ICEntry(m_Line.sBatchNumber(), m_Line.sEntryNumber());
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), getServletContext(), sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			throw new Exception(sWarning);
		}
		
		//Setting these to zero will cause the entry to drop this line:
		entry.setLineCost(m_Line.sId(), BigDecimal.ZERO);
		entry.setLineQty(m_Line.sId(), BigDecimal.ZERO);
		entry.remove_zero_amount_lines();
		
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), sDBID, "MySQL", this.toString() + ".deleteLine - User: " 
		+ sUserID
		+ " - "
		+ sUserFullName
				); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (conn == null){
			sWarning = Messages.getString("ICPhysicalCountLineUpdate.75"); 
			throw new Exception(sWarning);
		}
		
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sWarning = sWarning + "\n" + entry.getErrorMessage().get(i); 
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080884]");
			throw new Exception(sWarning);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080885]");
	
	}
    private void setDefaultWriteOffAccount(
    		ServletContext context, 
    		String sDBID, 
    		String sUserFullName,
    		String sLocation,
    		ICEntryLine m_Line
    		) throws Exception{
    	
    	String sWarning = "";
    	if (sLocation.compareToIgnoreCase("") == 0){
    		sWarning = "You chose to set the default write off account, but you have not selected "
    			+ "a location.";
    		throw new Exception(sWarning);
    	}
    	//String sWarning = "";
    	String SQL = "SELECT " //$NON-NLS-1$
    		+ SMTablelocations.sGLWriteOffAcct
    		+ " FROM "  //$NON-NLS-1$
    		+ SMTablelocations.TableName
    		+ " WHERE (" //$NON-NLS-1$
    			+ "(" + SMTablelocations.sLocation + " = '" + sLocation + "')" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		+ ")" //$NON-NLS-1$
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL",  //$NON-NLS-1$
    				this.toString() + ".setDefaultWriteOffAccount - user: " + sUserFullName); //$NON-NLS-1$
    		if (rs.next()){
    			m_Line.sDistributionAcct(rs.getString(SMTablelocations.sGLWriteOffAcct));
    		}else{
    			//sWarning = Messages.getString("ICPhysicalCountLineUpdate.86"); //$NON-NLS-1$
    			m_Line.sCategoryCode(""); //$NON-NLS-1$
    			throw new Exception("Default write off account is empty. ");
    		}
    		rs.close();
    	}catch (SQLException e){
    		m_Line.sCategoryCode(""); //$NON-NLS-1$
    		throw new Exception("Default write off account is empty - " + e.getMessage());
    	}
    }

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}