package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTablegltransactionbatches;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditBatchesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "GL Transaction Batch";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	String sBatchNumber = request.getParameter(SMTablegltransactionbatches.lbatchnumber);
	PrintWriter out = response.getWriter();
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.GLEditBatches))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    //Remove any AP Batch object, if there is one:
    CurrentSession.removeAttribute(GLTransactionBatch.OBJECT_NAME);
    CurrentSession.removeAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT);
    
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
    						+ CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
    String title = "Updating " + sObjectName + "'" + sBatchNumber + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    //If this is a request to post multiple batches, then branch off to that function:
    String sMultiplePostCommand = "";
    if (clsManageRequestParameters.get_Request_Parameter(GLEditBatchesSelect.BUTTON_POST_SELECTED_BATCHES, request).compareToIgnoreCase("") != 0){
    	sMultiplePostCommand = GLEditBatchesSelect.BUTTON_POST_SELECTED_BATCHES;
    }else{
    	if (clsManageRequestParameters.get_Request_Parameter(GLEditBatchesSelect.BUTTON_POST_ALL_BATCHES, request).compareToIgnoreCase("") != 0){
        	sMultiplePostCommand = GLEditBatchesSelect.BUTTON_POST_ALL_BATCHES;
    	}
    }
    
    if (sMultiplePostCommand.compareToIgnoreCase("") != 0){
    	try {
			postMultipleBatches(sMultiplePostCommand, request, sDBID, sUserFullName, sUserID);
		} catch (Exception e) {
			CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditBatchesSelect"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			return;
		}
		response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditBatchesSelect"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + "Status=Selected batches were successfully posted."
				);
			return;
    }
    
    GLTransactionBatch batch = new GLTransactionBatch(request);
	if (request.getParameter("Delete") != null){
		if (request.getParameter("ConfirmDelete") != null){
			try {
				batch.flag_as_deleted(sBatchNumber, getServletContext(), sDBID, sUserFullName);
			} catch (Exception e) {
				CurrentSession.setAttribute(GLTransactionBatch.OBJECT_NAME, batch);
				CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
				return;
			}
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				+ "&" + "STATUS=Batch number " + sBatchNumber + "  was marked as deleted."
				);
			return;
		}
		else{
			CurrentSession.setAttribute(GLTransactionBatch.OBJECT_NAME, batch);
			CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT ,  "Warning=You clicked the Delete button, but did not confirm by checking the checkbox." );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			return;
		}
	}

	if (request.getParameter("Post") != null){
		if (request.getParameter("ConfirmPost") != null){
			try {
				batch.post_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName);
			} catch (Exception e) {
				//CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
				CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
		    	    + "&" + "Warning=" + e.getMessage()
					);
			    return;
			}
			response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
					+ "&" + "Status=Batch number " + batch.getsbatchnumber() + " was successfully posted."
					);
				return;
		} else{
			CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT ,"Warning=" + "You clicked the Post button, but did not confirm by checking the checkbox.");
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				);
		    return;
		}
	}
	
	if (request.getParameter("Reverse") != null){
		if (request.getParameter("ConfirmReversal") != null){
			String sNewBatchNumber = "";
			try {
				//System.out.println("[20191711623581] going to reverse batch");
				sNewBatchNumber = batch.reverse_batch(getServletContext(), sDBID, sUserID, sUserFullName, out);
				//System.out.println("[20191711622303] " + "sNewBatchNumber = '" + sNewBatchNumber + "'.");
			} catch (Exception e) {
				CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
					);
			    return;
			}
			response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
					+ "&" + "Status=Batch number " + batch.getsbatchnumber() + " was successfully reversed into new batch number " + sNewBatchNumber + "."
					);
				return;
		} else{
			CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , "Warning=" + "You clicked the Reverse button, but did not confirm by checking the checkbox." );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				);
		    return;
		}
	}

	//Otherwise, we need to save the batch
		//First load all the entries:
		try {
			batch.loadEntries(getServletContext(), sDBID, sUserID);
			batch.save_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName, false);
		} catch (Exception e) {
			CurrentSession.setAttribute(GLTransactionBatch.OBJECT_NAME, batch);
			CurrentSession.setAttribute(GLEditBatchesEdit.GL_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			return;
		}
		response.sendRedirect(
			SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
			+ "&" + "Status=Batch number " + batch.getsbatchnumber() + " was successfully saved."
			);
		return;
	}
	private void postMultipleBatches(
		String sMultiplePostCommand,
		HttpServletRequest req,
		String sDBID,
		String sUserFullName,
		String sUserID
		) throws Exception{
		
		//First collect all the batches we want to post:
		ArrayList<Long>arrBatchesToPost = new ArrayList<Long>(0);
		
		if (sMultiplePostCommand.compareToIgnoreCase(GLEditBatchesSelect.BUTTON_POST_ALL_BATCHES) == 0){
			//We have to get all the unposted batches from the database:
			String SQL = "SELECT "
				+ SMTablegltransactionbatches.lbatchnumber
				+ " FROM " + SMTablegltransactionbatches.TableName
				+ " WHERE ("
					+ "(" + SMTablegltransactionbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + SMTablegltransactionbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ") ORDER BY " + SMTablegltransactionbatches.lbatchnumber
			;
			try {
				ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".postMultipleBatches - user: " + sUserFullName
				);
				while (rs.next()){
					arrBatchesToPost.add(rs.getLong(SMTablegltransactionbatches.lbatchnumber));
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [20193021417192] " + "reading postable batches - " + e.getMessage());
			}
		}else{
			//Get all the selected batches from the request object:
	    	Enumeration <String> eParams = req.getParameterNames();
	    	String sLineParam = "";
	    	while (eParams.hasMoreElements()){
	    		sLineParam = eParams.nextElement();
	    		if (sLineParam.startsWith(GLEditBatchesSelect.POSTING_CHECKBOX_SUFFIX)){
	    			try {
						arrBatchesToPost.add(Long.parseLong(sLineParam.replace(GLEditBatchesSelect.POSTING_CHECKBOX_SUFFIX, "")));
					} catch (Exception e) {
						throw new Exception("Error [20193021424481] " + "batch number parameter = '" + sLineParam + "' - " + e.getMessage());
					}
	    		}
	    	}
		}
		
		if (arrBatchesToPost.size() == 0){
			throw new Exception("There are no batches to post.");
		}
		
		//Sort the array:
		Collections.sort(arrBatchesToPost);
		
		//TODO - actually post the batches:
		for (int i = 0; i < arrBatchesToPost.size(); i++){
			GLTransactionBatch batch = new GLTransactionBatch(Long.toString(arrBatchesToPost.get(i)));
			try {
				batch.post_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [20193021449194] " + "error posting batch number: " + Long.toString(arrBatchesToPost.get(i)));
			}
		}
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
