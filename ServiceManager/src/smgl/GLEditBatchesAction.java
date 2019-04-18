package smgl;

import SMDataDefinition.SMTablegltransactionbatches;
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
    
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
    						+ CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    String  sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
    String title = "Updating " + sObjectName + "'" + sBatchNumber + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    GLTransactionBatch batch = new GLTransactionBatch(request);
	if (request.getParameter("Delete") != null){
		if (request.getParameter("ConfirmDelete") != null){
			try {
				batch.flag_as_deleted(sBatchNumber, getServletContext(), sDBID, sUserFullName);
			} catch (Exception e) {
				CurrentSession.setAttribute(GLTransactionBatch.OBJECT_NAME, batch);
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + "Warning=" + e.getMessage()
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
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    + "&" + "Warning=You clicked the Delete button, but did not confirm by checking the checkbox."
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
			//CurrentSession.setAttribute(APBatch.OBJECT_NAME, batch);
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTablegltransactionbatches.lbatchnumber + "=" + batch.getsbatchnumber()
	    	    + "&" + "Warning=" + "You clicked the Post button, but did not confirm by checking the checkbox."
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
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    + "&" + "Warning=" + e.getMessage()
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
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
