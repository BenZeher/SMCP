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
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		 	  + " " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }
		FAAsset asset = new FAAsset("");
		asset.loadFromHTTPRequest(request);
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
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
						+ "?" + asset.getQueryString()
						+ "&Warning=Could not save: Asset Number " +rsClasses.getString(SMTablefamaster.sAssetNumber)+" already exists"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			
			}catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		//System.out.println("1.2 - " + SMUtilities.get_Request_Parameter(ICItem.ParamItemNumber, request).trim().replace(" ", "").replace("&quot;", "\""));
		//System.out.println("1.3 - " + SMUtilities.get_Request_Parameter("LOC3", request).trim().replace(" ", "").replace("&quot;", "\""));
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
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
					+ "?" + asset.getQueryString()
					+ "&Warning=Could not save: " + asset.getErrorMessageString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067473]");
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
