package smar;

import SMClasses.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditBatchesUpdate extends HttpServlet{
	
	/*
	 * Parameters:
	 * Batch - batch number
	 * ibatchtype - batch type as an integer
	 * Post - the 'post batch' button
	 * ConfirmPost - the 'confirm posting' check box was clicked
	 * Delete - the 'delete batch' button
	 * ConfirmDelete - the 'confirm delete' check box was clicked
	 */
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Batch";
	private static String m_sWarning = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	String sBatchNumber = request.getParameter("BatchNumber");
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
    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String title = "Updating " + sObjectName + "'" + sBatchNumber + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    ARBatch batch = new ARBatch(sBatchNumber);
    batch.sBatchType(request.getParameter("BatchType"));
    batch.sLastEditedByID(sUserID);
    batch.sLastEditedByID(sUserFullName);
    batch.sModuleType(SMModuleTypes.AR);
    
	if (request.getParameter("Delete") != null){
		if (request.getParameter("ConfirmDelete") != null){
			try {
				batch.flag_as_deleted(getServletContext(), sDBID);
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + "Batch " + batch.sBatchNumber() + " was deleted."
			    		+ "'>");
				out.println("</BODY></HTML>");
			    return;
			} catch (Exception e) {
				m_sWarning = "WARNING: Error deleting batch";
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + m_sWarning
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		else{
			m_sWarning = "WARNING: You clicked the Delete button, but did not confirm by checking the checkbox.";
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatches" 
		    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + m_sWarning
		    		+ "'>");
			out.println("</BODY></HTML>");
		    return;
		}
	}
	if (request.getParameter("Post") != null){
		if (request.getParameter("ConfirmPost") != null){
			try {
				batch.load(getServletContext(), sDBID);
			} catch (Exception e) {
				m_sWarning = "WARNING: could not load batch " + sBatchNumber + ": \n" + batch.getErrorMessages();
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + m_sWarning
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;	
			}
			try {
				batch.post_with_data_transaction(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					out)
				;
				
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + "Posting complete."
		    		+ "'>")
		    	;
				out.println("</BODY></HTML>");
			    return;
			} catch (Exception e) {
				m_sWarning = "WARNING: Error posting batch " + sBatchNumber + ": \n" + e.getMessage();
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + clsServletUtilities.URLEncode(m_sWarning)
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		else{
			m_sWarning = "WARNING: You clicked the Post button, but did not confirm by checking the checkbox.";
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + clsServletUtilities.URLEncode(m_sWarning)
		    		+ "'>");
			out.println("</BODY></HTML>");
		    return;
		}
	}
	
	//Otherwise, we need to save the batch
    if (Validate_Batch(batch, request, out)){
    	if (save_batch(batch, getServletContext(), sDBID, sUserID, sUserFullName)){
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + "Batch " + batch.sBatchNumber() + " saved."
			    		+ "'>");
				out.println("</BODY></HTML>");
    		
    	}else{
    		//If it DIDN'T save:
    		m_sWarning = "WARNING: Error saving batch - " + batch.getErrorMessages();
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + clsServletUtilities.URLEncode(m_sWarning)
		    		+ "'>");
			out.println("</BODY></HTML>");
    	}
    }
    else{
    	//Invalid entries:
		out.println("<META http-equiv='Refresh' content='" + "10" + ";URL=" 
	    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
	    		+ "?BatchNumber=" + batch.sBatchNumber()
	    		+ "&BatchType=" + batch.sBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&Warning=" + clsServletUtilities.URLEncode(m_sWarning)
	    		+ "'>");
		out.println("</BODY></HTML>");
	    return;
	    }
    }

    private boolean save_batch(ARBatch batch, ServletContext context, String sConf, String sUserID, String sUserFullName){
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf,
			"MySQL",
			this.toString() + ".save_batch - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
		if (conn == null){
			return false;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067530]");
			return false;
		}
		
		try {
			batch.save_without_data_transaction(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067531]");
			return false;		
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067532]");
		return true;
    }
	private static boolean Validate_Batch(ARBatch batch, HttpServletRequest req, PrintWriter pwOut){
		
		//TODO - validate entered information:
		//System.out.println("In AREditBatchesUpdate.Validate_Batch: year = '" 
		//		+ req.getParameter("BatchDateYear") + "'"
		//		+ ", month = '" + req.getParameter("BatchDateMonth") + "'"
		//		+ ", day = '" + req.getParameter("BatchDateDay") + "'"
		//);
		
		String sBatchDate = ARUtilities.get_Request_Parameter(SMEntryBatch.datbatchdate, req);
		//if (! batch.tsBatchDate(
		//		req.getParameter("BatchDateYear"), 
		//		req.getParameter("BatchDateMonth"), 
		//		req.getParameter("BatchDateDay"))){
		//	return false;
		//}
		
		if (sBatchDate.trim().compareToIgnoreCase("") == 0){
			pwOut.println("Batch date cannot be blank<BR>");
			m_sWarning = "WARNING: Invalid batch date passed";
			return false;
		}
		
		if (!batch.tsBatchDate(clsDateAndTimeConversions.StringToTimestamp("MM/dd/yyyy", sBatchDate))){
			pwOut.println("Invalid batch date passed<BR>");
			m_sWarning = "WARNING: Invalid batch date passed";
			return false;
		}
		//System.out.println("In AREditBatchesUpdate.Validate_Batch: batch.sStdBatchDateString() = " + batch.sStdBatchDateString());
		
		if (req.getParameter(SMEntryBatch.ibatchstatus) == null){
			pwOut.println("Null batch status passed<BR>");
			m_sWarning = "WARNING: Null batch status passed";
			return false;
		}
		if (! batch.sBatchStatus(req.getParameter(SMEntryBatch.ibatchstatus))){
			pwOut.println("Invalid batch status passed<BR>");
			m_sWarning = "WARNING: Invalid batch status passed";
			return false;
		}
		if (req.getParameter(SMEntryBatch.lcreatedbyid) == null){
			pwOut.println("Null 'created by' passed<BR>");
			m_sWarning = "WARNING: Null 'created by' passed";
			return false;
		}
		batch.sCreatedByID(req.getParameter(SMEntryBatch.lcreatedbyid));
		
		if (req.getParameter(SMEntryBatch.sbatchdescription) == null){
			pwOut.println("Null description passed<BR>");
			m_sWarning = "WARNING: Null description passed";
			return false;
		}
		batch.sBatchDescription(req.getParameter(SMEntryBatch.sbatchdescription));
		return true;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
