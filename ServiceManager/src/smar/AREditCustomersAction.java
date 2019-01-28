package smar;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMCreateGDriveFolder;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditCustomersAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomers))
			{
				return;
			}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		ARCustomer cust = new ARCustomer("");
		cust.loadFromHTTPRequest(request);

       	//If Create And Upload Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
    			AREditCustomersEdit.COMMAND_FLAG, request).compareToIgnoreCase(AREditCustomersEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//First, if there's an '&' in the customer number, this won't fly - notify the user and return:
    		if (cust.getM_sCustomerNumber().contains("&")){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
						+ "?" + cust.getQueryString()
						+ "&Warning=Error [1448920399] - cannot manage Google Drive folders for customers with an ampersand in their account number."
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
				return;
    		}
    		
			// Need to get Parent Folder ID and Web App URL
			AROptions aropt = new AROptions();
			try {
				aropt.load(sDBID, getServletContext(), sUserName);
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
						+ "?" + cust.getQueryString()
						+ "&Warning=Error loading AROptions: " + cust.getErrorMessageString()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
				return;
			}
			
			SMOption smopt = new SMOption();
			try {
				smopt.load(sDBID, getServletContext(), sUserName);
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
						+ "?" + cust.getQueryString()
						+ "&Warning=Error loading SMOption: " + cust.getErrorMessageString()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
				return;
			}
			
			String sNewFolderName = aropt.getgdrivecustomerfolderprefix() + cust.getM_sCustomerNumber() + aropt.getgdrivecustomerfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
       	
			try {
				String sRedirectString = smopt.getgdriveuploadfileurl()
		         		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + aropt.getgdrivecustomerparentfolderid()
		   				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGDriveFolder.AR_CUSTOMER_RECORD_TYPE_PARAM_VALUE
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + cust.getM_sCustomerNumber()
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
		             	;
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
					+ "?" + cust.getQueryString()
					+ "&Warning=Could not create folder or upload files: " + e.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			return;
    	}
		
		//Need a connection here because it involves a data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
			
		if(!cust.save(sUserFullName, sUserID, sCompanyName, conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067534]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
					+ "?" + cust.getQueryString()
					+ "&Warning=Could not save: " + cust.getErrorMessageString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067535]");
		//System.out.println("In " + this.toString() + " cust.querystring = " + cust.getQueryString());
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
				+ "?" + cust.getQueryString()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		return;
	}
	
	private String getCreateGDriveReturnURL(HttpServletRequest req)throws Exception{
		String sTemp;
		try {
			sTemp = clsServletUtilities.getServerURL(req, getServletContext());
		} catch (Exception e) {
			throw new Exception(" Error [1542747893]"+e.getMessage());
		}
		sTemp += "/" + WebContextParameters.getInitWebAppName(getServletContext()) + "/";
		sTemp += "smcontrolpanel.SMCreateGDriveFolder";
		return sTemp;
	}
	
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1440626558] In " + SMUtilities.getFullClassName(this.toString()) 
					+ ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1440626559] In " + SMUtilities.getFullClassName(this.toString()) 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		}
	}
	
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
