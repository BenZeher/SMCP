package smap;

import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditBatchesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Batch";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	String sBatchNumber = request.getParameter(SMTableapbatches.lbatchnumber);
	PrintWriter out = response.getWriter();
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.APEditBatches))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    //Remove any AP Batch object, if there is one:
    CurrentSession.removeAttribute(APBatch.OBJECT_NAME);
	CurrentSession.removeAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT);

    
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
    						+ CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    String  sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
    String title = "Updating " + sObjectName + "'" + sBatchNumber + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    APBatch batch = new APBatch(request);
	if (request.getParameter("Delete") != null){
		if (request.getParameter("ConfirmDelete") != null){
			try {
				batch.flag_as_deleted(sBatchNumber, getServletContext(), sDBID, sUserFullName);
			} catch (Exception e) {
				CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
				CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
				return;
			}
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				+ "&" + "STATUS=Batch number " + sBatchNumber + "  was marked as deleted."
				);
			return;
		}
		else{
			CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
			CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT , "Warning=You clicked the Delete button, but did not confirm by checking the checkbox." );
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
				batch.post_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName, out);
			} catch (Exception e) {
				//CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
				CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
					);
			    return;
			}
			response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
					+ "&" + "Status=Batch number " + batch.getsbatchnumber() + " was successfully posted."
					);
				return;
		} else{
			//CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
			CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT ,  "Warning="+"You clicked the Post button, but did not confirm by checking the checkbox." );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				);
		    return;
		}
	}
	
	//If it's a 'Print checks' command:
	if (request.getParameter(APEditBatchesEdit.BUTTON_PRINT_CHECKS_NAME) != null){
		response.sendRedirect(
			SMUtilities.getURLLinkBase(getServletContext()) + "smap.APPrintChecksEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
			+ "&" + APPrintChecksEdit.CLEAR_UNFINALIZED_CHECKS + "=Y"
			+ "&" + "CallingClass=" + sCallingClass
			);
		return;
	}

	//Otherwise, we need to save the batch
		//First load all the entries:
		try {
			batch.loadEntries(getServletContext(), sDBID, sUserID);
			batch.save_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName, false);
		} catch (Exception e) {
			CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
			CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT , e.getMessage() );
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			return;
		}
		response.sendRedirect(
			SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
			+ "&" + "Status=Batch number " + batch.getsbatchnumber() + " was successfully saved."
			);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
