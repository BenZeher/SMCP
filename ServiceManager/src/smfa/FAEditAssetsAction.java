package smfa;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablefamaster;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FAEditAssetsAction extends HttpServlet{
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);

	    //Remove any leftover object here:
	    CurrentSession.removeAttribute(FAAsset.sObjectName);
	    
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		 	  + " " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }
	    
		FAAsset asset = new FAAsset("");
		asset.loadFromHTTPRequest(request);
		
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
		    	CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsEdit"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (asset.getAssetNumber().compareToIgnoreCase("") == 0){
		    	CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsEdit"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=You must enter an asset number to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		sDBID,
		    		"MySQL",
		    		this.toString() + ".doPost - User: " + sUserID
		    		+ " - "
		    		+ sUserFullName
		    			);
		    	if(conn == null){
		    		CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsEdit"
        					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
        					+ "&Warning=Error deleting item - cannot get connection."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	try {
					asset.delete(asset.getAssetNumber(), conn);
				} catch (Exception e) {
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067474]");
			    	CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsEdit"
    					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
    					+ "&" + asset.getQueryString()
    					+ "&Warning=Error deleting asset - " + e.getMessage()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
				}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067475]");
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
					+ "?Status=Successfully deleted asset " + asset.getAssetNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
	    }
		
		//If we're adding a NEW asset:
		if(request.getParameter("AssetNumberNEW") != null){
			String sSQL = "SELECT * FROM "+SMTablefamaster.TableName
						+" WHERE "+SMTablefamaster.sAssetNumber+" = '"+request.getParameter("AssetNumberNEW")+"'";
			try{
				ResultSet rsClasses = clsDatabaseFunctions.openResultSet(
			        	sSQL, 
			        	getServletContext(), 
			        	sDBID,
			        	"MySQL",
			        	this.toString() + "Check if asset Number exists - User: " + sUserID
			        	+ " - "
			        	+ sUserFullName
			        	);
			if(rsClasses.next()){
				CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
						+ "?Warning=Could not save: Asset Number " +rsClasses.getString(SMTablefamaster.sAssetNumber)+" already exists"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			
			}catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName	
				);
		
		//Need a connection here because it involves a data transaction:
		if(!asset.save(sUserName, conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067472]");
			CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
					+ "?Warning=Could not save: " + asset.getErrorMessageString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067473]");
		CurrentSession.setAttribute(FAAsset.sObjectName, (FAAsset)asset);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
				+ "&SubmitEdit=Y"
				+ "&Status=" + clsServletUtilities.URLEncode("Asset number " + asset.getAssetNumber() + " was updated successfully.")
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
