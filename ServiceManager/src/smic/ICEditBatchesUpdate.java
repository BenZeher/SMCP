package smic;

import SMClasses.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

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
	private static String m_EditBatchesUpdateWarning = "";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
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
    
	if (request.getParameter("Delete") != null){
		if (request.getParameter("ConfirmDelete") != null){
			if (batch.flag_as_deleted(getServletContext(), sDBID)){
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + "Batch " + batch.sBatchNumber() + " was deleted."
			    		+ "'>");
				out.println("</BODY></HTML>");
			    return;
			}
			else{
				m_EditBatchesUpdateWarning = "WARNING: Error deleting batch";
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
			    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + m_EditBatchesUpdateWarning
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		else{
			m_EditBatchesUpdateWarning = "WARNING: You clicked the Delete button, but did not confirm by checking the checkbox.";
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatches" 
		    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + m_EditBatchesUpdateWarning
		    		+ "'>");
			out.println("</BODY></HTML>");
		    return;
		}
	}
	if (request.getParameter("Post") != null){
		if (request.getParameter("ConfirmPost") != null){
			if (!batch.load(getServletContext(), sDBID)){
				m_EditBatchesUpdateWarning = "WARNING: could not load batch " + sBatchNumber + ": \n" + batch.getErrorMessages();
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + m_EditBatchesUpdateWarning
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;				
			}
			if (batch.post_with_data_transaction(
					getServletContext(), 
					sDBID, 
					sUserID,
					sUserFullName,
					out)){
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + "Posting complete."
			    		+ "'>");
				out.println("</BODY></HTML>");
			    return;
			}
			else{
				m_EditBatchesUpdateWarning = "WARNING: Error posting batch " + sBatchNumber + ": \n" 
					+ clsServletUtilities.URLEncode(batch.getErrorMessages());
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + m_EditBatchesUpdateWarning
			    		+ "'>");
				out.println("</BODY></HTML>");
				return;
			}
		}
		else{
			m_EditBatchesUpdateWarning = "WARNING: You clicked the Post button, but did not confirm by checking the checkbox.";
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + m_EditBatchesUpdateWarning
		    		+ "'>");
			out.println("</BODY></HTML>");
		    return;
		}
	}
	
	//Otherwise, we need to save the batch
    if (Validate_Batch(batch, request, out)){
    	if (save_batch(batch, getServletContext(), sDBID, sUserFullName, sUserID)){
				out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
			    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
			    		+ "?BatchNumber=" + batch.sBatchNumber()
			    		+ "&BatchType=" + batch.sBatchType()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&Warning=" + "Batch " + batch.sBatchNumber() + " saved."
			    		+ "'>");
				out.println("</BODY></HTML>");
    		
    	}else{
    		//If it DIDN'T save:
    		m_EditBatchesUpdateWarning = "WARNING: Error saving batch - " + batch.getErrorMessages();
			out.println("<META http-equiv='Refresh' content='" + "0" + ";URL=" 
		    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
		    		+ "?BatchNumber=" + batch.sBatchNumber()
		    		+ "&BatchType=" + batch.sBatchType()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&Warning=" + m_EditBatchesUpdateWarning
		    		+ "'>");
			out.println("</BODY></HTML>");
    	}
    }
    else{
    	//Invalid entries:
		out.println("<META http-equiv='Refresh' content='" + "10" + ";URL=" 
	    		+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
	    		+ "?BatchNumber=" + batch.sBatchNumber()
	    		+ "&BatchType=" + batch.sBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&Warning=" + m_EditBatchesUpdateWarning
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
	private static boolean Validate_Batch(ICEntryBatch batch, HttpServletRequest req, PrintWriter pwOut){
		
		if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", req.getParameter(ICEntryBatch.datbatchdate))){
			pwOut.println("Invalid batch date passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Invalid batch date passed";
			return false;
		}

		java.sql.Date datBatchDate = null;
		try {
			datBatchDate = clsDateAndTimeConversions.StringTojavaSQLDate(
				"MM/dd/yyyy", req.getParameter(ICEntryBatch.datbatchdate));
		} catch (ParseException e) {
			pwOut.println("Invalid batch date passed<BR>");
			m_EditBatchesUpdateWarning = "Error:[1423767376] Invalid batch date: '" + datBatchDate + "' - " + e.getMessage();
			return false;
		}
		
		if (! batch.setBatchDate(clsDateAndTimeConversions.utilDateToString(datBatchDate, "yyyy-MM-dd"))){
			pwOut.println("Invalid batch date passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Invalid batch date passed";
			return false;
		}
		
		if (req.getParameter(ICEntryBatch.ibatchstatus) == null){
			pwOut.println("Null batch status passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Null batch status passed";
			return false;
		}
		if (! batch.sBatchStatus(req.getParameter(ICEntryBatch.ibatchstatus))){
			pwOut.println("Invalid batch status passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Invalid batch status passed";
			return false;
		}
		if (req.getParameter(ICEntryBatch.screatedbyfullname) == null){
			pwOut.println("Null 'created by fullname' passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Null 'created by fullname' passed";
			return false;
		}
		batch.sSetCreatedByFullName(req.getParameter(ICEntryBatch.screatedbyfullname));
		
		if (req.getParameter(ICEntryBatch.lcreatedbyid) == null){
			pwOut.println("Null 'created by ID' passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Null 'created by ID' passed";
			return false;
		}
		batch.sSetCreatedByID(req.getParameter(ICEntryBatch.lcreatedbyid));
		
		batch.sSetCreatedByFullName(req.getParameter(ICEntryBatch.screatedbyfullname));
		
		if (req.getParameter(ICEntryBatch.sbatchdescription) == null){
			pwOut.println("Null description passed<BR>");
			m_EditBatchesUpdateWarning = "WARNING: Null description passed";
			return false;
		}	
		batch.sBatchDescription(req.getParameter(ICEntryBatch.sbatchdescription));
		return true;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
