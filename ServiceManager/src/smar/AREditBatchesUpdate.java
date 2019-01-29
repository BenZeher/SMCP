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
	private static final String sObjectName = "Batch";
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
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String m_sWarning = "";
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
	try {
		Validate_Batch(batch, request, out);
	} catch (Exception e) {
    	//Invalid entries:
		out.println("<META http-equiv='Refresh' content='" + "10" + ";URL=" 
	    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
	    		+ "?BatchNumber=" + batch.sBatchNumber()
	    		+ "&BatchType=" + batch.sBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
	    		+ "'>");
		out.println("</BODY></HTML>");
	    return;
	}
	
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

    private boolean save_batch(ARBatch batch, ServletContext context, String sDBID, String sUserID, String sUserFullName){
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID,
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
	private static void Validate_Batch(ARBatch batch, HttpServletRequest req, PrintWriter pwOut) throws Exception{
		
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
			throw new Exception("WARNING: Invalid batch date passed");
		}
		
		if (!batch.tsBatchDate(clsDateAndTimeConversions.StringToTimestamp("MM/dd/yyyy", sBatchDate))){
			pwOut.println("Invalid batch date passed<BR>");
			throw new Exception("WARNING: Invalid batch date passed");
		}
		//System.out.println("In AREditBatchesUpdate.Validate_Batch: batch.sStdBatchDateString() = " + batch.sStdBatchDateString());
		
		if (req.getParameter(SMEntryBatch.ibatchstatus) == null){
			pwOut.println("Null batch status passed<BR>");
			throw new Exception("WARNING: Null batch status passed");
		}
		if (! batch.sBatchStatus(req.getParameter(SMEntryBatch.ibatchstatus))){
			pwOut.println("Invalid batch status passed<BR>");
			throw new Exception("WARNING: Invalid batch status passed");
		}
		if (req.getParameter(SMEntryBatch.lcreatedbyid) == null){
			pwOut.println("Null 'created by' passed<BR>");
			throw new Exception("WARNING: Null 'created by' passed");
		}
		batch.sCreatedByID(req.getParameter(SMEntryBatch.lcreatedbyid));
		
		if (req.getParameter(SMEntryBatch.sbatchdescription) == null){
			pwOut.println("Null description passed<BR>");
			throw new Exception("WARNING: Null description passed");
		}
		batch.sBatchDescription(req.getParameter(SMEntryBatch.sbatchdescription));
		return;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
