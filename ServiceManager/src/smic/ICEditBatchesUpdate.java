package smic;

import SMClasses.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditBatchesUpdate extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String sBatchObjectName = "Batch";
	

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	String m_EditBatchesUpdateWarning = "";
	String sBatchNumber = request.getParameter("BatchNumber");
	PrintWriter out = response.getWriter();
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.ICEditBatches))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
	CurrentSession.removeAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT);
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
    			+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String title = "Updating " + sBatchObjectName + "'" + sBatchNumber + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    ICEntryBatch batch = new ICEntryBatch(sBatchNumber);
    batch.sBatchType(request.getParameter("BatchType"));
    batch.sSetLastEditedByFullName(sUserFullName);
    batch.sSetLastEditedByID(sUserID);
    batch.sModuleType(SMModuleTypes.IC);
    
    if (clsManageRequestParameters.get_Request_Parameter(
    		"COMMANDFLAG", request).compareToIgnoreCase(ICEditBatchesEdit.DELETE_COMMAND_VALUE) == 0){
		if (request.getParameter("ConfirmDelete") != null){
			if (batch.flag_as_deleted(getServletContext(), sDBID)){
				CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT , "Batch " + batch.sBatchNumber() + " was deleted." );
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "'>");
				out.println("</BODY></HTML>");
			    return;
			}
			else{
				m_EditBatchesUpdateWarning = "WARNING: Error deleting batch";
				CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  m_EditBatchesUpdateWarning);
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		else{
			m_EditBatchesUpdateWarning = "WARNING: You clicked the Delete button, but did not confirm by checking the checkbox.";
			CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  m_EditBatchesUpdateWarning);
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
		    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "'>");
			out.println("</BODY></HTML>");
		    return;
		}
	}
    if (clsManageRequestParameters.get_Request_Parameter(
    		"COMMANDFLAG", request).compareToIgnoreCase(ICEditBatchesEdit.POST_COMMAND_VALUE) == 0){
		if (request.getParameter("ConfirmPost") != null){
			if (!batch.load(getServletContext(), sDBID)){
				m_EditBatchesUpdateWarning = "WARNING: could not load batch " + sBatchNumber + ": \n" + batch.getErrorMessages();
				CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  m_EditBatchesUpdateWarning);
				response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		);
				return;				
			}
			if (batch.post_with_data_transaction(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					out)){
				CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  "Posting complete.");
				response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		);
			    return;
			}
			else{
				m_EditBatchesUpdateWarning = "WARNING: Error posting batch " + sBatchNumber + ": " 
					+ batch.getErrorMessages();
				CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT , m_EditBatchesUpdateWarning);
				response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		);
				return;
			}
		}
		else{
			m_EditBatchesUpdateWarning = "WARNING: You clicked the Post button, but did not confirm by checking the checkbox.";
			CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT , m_EditBatchesUpdateWarning);
			response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		);
			return;
		}
	}
	
	//Otherwise, we need to save the batch
    try {
    	Validate_Batch(batch, request, out);
    	if (save_batch(batch, getServletContext(), sDBID, sUserFullName, sUserID)){
			CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  "Batch " + batch.sBatchNumber() + " saved.");
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "'>");
				out.println("</BODY></HTML>");
    		
    	}else{
    		//If it DIDN'T save:
    		m_EditBatchesUpdateWarning = "WARNING: Error saving batch - " + batch.getErrorMessages();
			CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  m_EditBatchesUpdateWarning);
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "'>");
			out.println("</BODY></HTML>");
    	}
    
    
    }catch (Exception e){
    	//Invalid entries:
		CurrentSession.setAttribute(ICEditBatchesEdit.IC_BATCH_POSTING_SESSION_WARNING_OBJECT ,  e.getMessage());
		out.println("<META http-equiv='Refresh' content='" + "10" + ";URL=" 
	    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
	    		+ "?BatchNumber=" + batch.sBatchNumber()
	    		+ "&BatchType=" + batch.sBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "'>");
		out.println("</BODY></HTML>");
	    return;
	    }
    }

    private boolean save_batch(ICEntryBatch batch, ServletContext context, String sDBID, String sUserFullName, String sUserID){
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID,
			"MySQL",
			this.toString() + ".save_batch - User: " + sUserFullName);
		if (conn == null){
			return false;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080819]");
			return false;
		}
		
		if (!batch.save_without_data_transaction(conn, sUserFullName, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080820]");
			return false;			
			
		}else{
			clsDatabaseFunctions.commit_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080821]");
			return true;
		}
    }
	private static void Validate_Batch(ICEntryBatch batch, HttpServletRequest req, PrintWriter pwOut) throws Exception{
		
		if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", req.getParameter(ICEntryBatch.datbatchdate))){
			pwOut.println("Invalid batch date passed<BR>");
			throw new Exception("WARNING: Invalid batch date passed");
		}

		java.sql.Date datBatchDate = null;
		try {
			datBatchDate = clsDateAndTimeConversions.StringTojavaSQLDate(
				"MM/dd/yyyy", req.getParameter(ICEntryBatch.datbatchdate));
		} catch (ParseException e) {
			pwOut.println("Invalid batch date passed<BR>");
			throw new Exception("Error:[1423767376] Invalid batch date: '" + datBatchDate + "' - " + e.getMessage());
		}
		
		if (! batch.setBatchDate(clsDateAndTimeConversions.utilDateToString(datBatchDate, "yyyy-MM-dd"))){
			pwOut.println("Invalid batch date passed<BR>");
			throw new Exception("WARNING: Invalid batch date passed");
		}
		
		if (req.getParameter(ICEntryBatch.ibatchstatus) == null){
			pwOut.println("Null batch status passed<BR>");
			throw new Exception("WARNING: Null batch status passed");
		}
		if (! batch.sBatchStatus(req.getParameter(ICEntryBatch.ibatchstatus))){
			pwOut.println("Invalid batch status passed<BR>");
			throw new Exception("WARNING: Invalid batch status passed");
		}
		if (req.getParameter(ICEntryBatch.screatedbyfullname) == null){
			pwOut.println("Null 'created by fullname' passed<BR>");
			throw new Exception("WARNING: Null 'created by fullname' passed");
		}
		batch.sSetCreatedByFullName(req.getParameter(ICEntryBatch.screatedbyfullname));
		
		if (req.getParameter(ICEntryBatch.lcreatedbyid) == null){
			pwOut.println("Null 'created by ID' passed<BR>");
			throw new Exception("WARNING: Null 'created by ID' passed");
		}
		batch.sSetCreatedByID(req.getParameter(ICEntryBatch.lcreatedbyid));
		
		batch.sSetCreatedByFullName(req.getParameter(ICEntryBatch.screatedbyfullname));
		
		if (req.getParameter(ICEntryBatch.sbatchdescription) == null){
			pwOut.println("Null description passed<BR>");
			throw new Exception("WARNING: Null description passed");
		}	
		batch.sBatchDescription(req.getParameter(ICEntryBatch.sbatchdescription));
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
