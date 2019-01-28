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
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPhysicalCountLineUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private ICEntryLine m_Line;
	//HttpServletRequest parameters:
	private String m_sSave;
	private String m_sDelete;
	private String m_sConfirmDelete;
	private String m_sUpdateDefaultWriteOffAccount;
	private String m_sCallingClass;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sLineNumber;
	private String m_sBatchType;
	private String m_sWarning = ""; //$NON-NLS-1$
	private String m_sSendRedirect;
	
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
	    
	    m_sWarning = ""; //$NON-NLS-1$
	    
	    //Collect all the request parameters:
	    getRequestParameters(request);
	    
	    //Instantiate a new line:
	    m_Line = new ICEntryLine(request);
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
	    //Branch here, depending on the request:
	    //If it's a request to update the write off account, update that account:
	    //System.out.println("In " + this.toString() + " 01");
    	
    	process(CurrentSession,response, sDBID, sUserID, sUserFullName);
    	try{
        	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
    	}catch(Exception e){

    	}
    	response.sendRedirect(m_sSendRedirect);
	    
	}
	
	
	private void process( HttpSession CurrentSession, HttpServletResponse response, String sDBID, String sUserID, String sUserFullName) throws ServletException, IOException{
		
	if (m_sUpdateDefaultWriteOffAccount.compareToIgnoreCase("") != 0){ //$NON-NLS-1$
    	if (!setDefaultWriteOffAccount(
    			getServletContext(), 
    			sDBID, 
    			sUserFullName,
    			m_Line.sLocation()
    		)){
    		//System.out.println("In " + this.toString() + " 02");
    	}
    	m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" //$NON-NLS-1$ //$NON-NLS-2$
				+ "BatchNumber=" + m_Line.sBatchNumber() //$NON-NLS-1$
				+ "&EntryNumber=" + m_Line.sEntryNumber() //$NON-NLS-1$
				+ "&LineNumber=" + m_Line.sLineNumber() //$NON-NLS-1$
				+ "&BatchType=" + m_sBatchType //$NON-NLS-1$
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID //$NON-NLS-1$
				+ "&Warning=" + m_sWarning; //$NON-NLS-1$

    	return;
    }
    
    if (m_sDelete.compareToIgnoreCase("") != 0){ //$NON-NLS-1$
    	if (m_sConfirmDelete.compareToIgnoreCase("") == 0){ //$NON-NLS-1$
    		m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.23"); //$NON-NLS-1$
    		m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" //$NON-NLS-1$ //$NON-NLS-2$
					+ "BatchNumber=" + m_Line.sBatchNumber() //$NON-NLS-1$
					+ "&EntryNumber=" + m_Line.sEntryNumber() //$NON-NLS-1$
					+ "&LineNumber=" + m_Line.sLineNumber() //$NON-NLS-1$
					+ "&BatchType=" + m_sBatchType //$NON-NLS-1$
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID //$NON-NLS-1$
					+ "&Warning=" + m_sWarning; //$NON-NLS-1$
	    	
	    	return;
    	}else{
    		//Delete the line:
    		if (!deleteLine(sDBID, sUserID, sUserFullName)){
    			m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" //$NON-NLS-1$ //$NON-NLS-2$
						+ "BatchNumber=" + m_Line.sBatchNumber() //$NON-NLS-1$
						+ "&EntryNumber=" + m_Line.sEntryNumber() //$NON-NLS-1$
						+ "&LineNumber=" + m_Line.sLineNumber() //$NON-NLS-1$
						+ "&BatchType=" + m_sBatchType //$NON-NLS-1$
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID //$NON-NLS-1$
						+ "&Warning=" + Messages.getString("ICPhysicalCountLineUpdate.40") + m_sWarning; //$NON-NLS-1$ //$NON-NLS-2$
		    	return;	    			
    		}
    		m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentEntry" //$NON-NLS-1$
					+ "?BatchNumber=" + m_Line.sBatchNumber() //$NON-NLS-1$
					+ "&EntryNumber=" + m_Line.sEntryNumber() //$NON-NLS-1$
					+ "&BatchType=" + m_sBatchType //$NON-NLS-1$
					+ "&Editable=Yes" //$NON-NLS-1$
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID //$NON-NLS-1$
					+ "&Warning="; //$NON-NLS-1$
	    	return;
    	}
    }

	    if (m_sSave.compareToIgnoreCase("") != 0){ //$NON-NLS-1$
	    	if (!saveLine(
	    			getServletContext(), 
	    			sDBID, 
	    			sUserID,
	    			sUserFullName
	    		)){
	    		//System.out.println("In " + this.toString() + " 02");
	    	}
	    	//Store the saved line in the session
	    	CurrentSession.setAttribute("EntryLine", m_Line); //$NON-NLS-1$
	    	m_sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + m_sCallingClass + "?" //$NON-NLS-1$ //$NON-NLS-2$
					+ "BatchNumber=" + m_Line.sBatchNumber() //$NON-NLS-1$
					+ "&EntryNumber=" + m_Line.sEntryNumber() //$NON-NLS-1$
					+ "&LineNumber=" + m_Line.sLineNumber() //$NON-NLS-1$
					+ "&BatchType=" + m_sBatchType //$NON-NLS-1$
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID //$NON-NLS-1$
					+ "&Warning=" + m_sWarning; //$NON-NLS-1$ 
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
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			return false;
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
			m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.61"); //$NON-NLS-1$
			return false;
		}
		
		//Validate the line first in case we can't save it at all:
		if (!entry.validateSingleLine(m_Line, conn)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080886]");
			return false;
		}
		
		//If it's a new line, just add it to the entry:
		if (m_Line.sLineNumber().compareToIgnoreCase("-1") == 0){ //$NON-NLS-1$
			//Validate it now so we know that it's going to be in the entry for sure.  We need to 
			//know this so we can get the line ID and line number from the last line after it's
			//saved, and that's only correct if the line is going to be saved.

			if (!entry.add_line(m_Line)){
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080887]");
				return false;
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
				m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.65") + this.toString() + ".saveLine - "; //$NON-NLS-1$ //$NON-NLS-2$
				for (int i = 0; i < entry.getErrorMessage().size(); i++){
					m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
				}
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080888]");
				return false;
			}
		}

		//System.out.println("In " + this.toString() + ".saveLine: price = " + m_Line.sPriceSTDFormat());
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080889]");
			return false;
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
			m_sWarning = m_sWarning + "\n" + m_Line.getErrorMessage(); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}
	private boolean deleteLine(String sDBID, String sUserID, String sUserFullName){
		
		ICEntry entry = new ICEntry(m_Line.sBatchNumber(), m_Line.sEntryNumber());
		if (!entry.load(m_Line.sBatchNumber(), m_Line.sEntryNumber(), getServletContext(), sDBID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			return false;
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
			m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.75"); //$NON-NLS-1$
			return false;
		}
		
		if (!entry.save_without_data_transaction(conn, sUserID)){
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				m_sWarning = m_sWarning + "\n" + entry.getErrorMessage().get(i); //$NON-NLS-1$
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080884]");
			return false;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080885]");
		return true;
	}
    private boolean setDefaultWriteOffAccount(
    		ServletContext context, 
    		String sDBID, 
    		String sUserFullName,
    		String sLocation
    		){
    	
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
    			m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.86"); //$NON-NLS-1$
    			m_Line.sCategoryCode(""); //$NON-NLS-1$
    		}
    		rs.close();
    	}catch (SQLException e){
    		m_Line.sCategoryCode(""); //$NON-NLS-1$
    		m_sWarning = Messages.getString("ICPhysicalCountLineUpdate.89") + e.getMessage(); //$NON-NLS-1$
    	}
    	
    	return true;
    }

	private void getRequestParameters(
    	HttpServletRequest req){

		m_sSave = clsManageRequestParameters.get_Request_Parameter("Save", req); //$NON-NLS-1$
		m_sDelete = clsManageRequestParameters.get_Request_Parameter("Delete", req); //$NON-NLS-1$
		m_sConfirmDelete = clsManageRequestParameters.get_Request_Parameter("ConfirmDelete", req); //$NON-NLS-1$
		m_sUpdateDefaultWriteOffAccount = clsManageRequestParameters.get_Request_Parameter("SubmitDefaultWriteOffAccount", req); //$NON-NLS-1$
		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", req); //$NON-NLS-1$
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", req); //$NON-NLS-1$
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", req); //$NON-NLS-1$
		m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", req); //$NON-NLS-1$
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", req); //$NON-NLS-1$
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}