package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLaborBackCharge;
import SMClasses.SMOption;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMLaborBackChargeAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditLaborBackCharges)){return;}
		smaction.getCurrentSession().removeAttribute(SMLaborBackCharge.ParamObjectName);
	    //Read the entry fields from the request object:
		SMLaborBackCharge entry = new SMLaborBackCharge(request);
		
		//If it's a delete, process that:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		SMLaborBackChargeEdit.COMMAND_FLAG, request).compareToIgnoreCase(
	    				SMLaborBackChargeEdit.DELETE_COMMAND_VALUE) == 0){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sLid = entry.getlid();
			    String sOrderNumber = entry.getstrimmedordernumber();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
			    		"Could not delete: " + e.getMessage(), 
			    		"", 
			    		SMLaborBackCharge.Paramlid + "=" + entry.getlid()
			    		);
					return;
				}

		    	//If the delete succeeded, redirect to Edit screen with a new entry:
					String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMLaborBackChargeEdit"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&Status=" + entry.getObjectName() + ": " + sLid + " was successfully deleted."
						+ "&" + SMLaborBackCharge.Paramlid + "=-1"
					    + "&" + SMLaborBackCharge.Paramstrimmedordernumber + "=" + sOrderNumber ;
					try {
						response.sendRedirect(sRedirectString);
					} catch (IOException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IOException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					} catch (IllegalStateException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IllegalStateException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					}
				
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(SMLaborBackCharge.ParamObjectName, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
	    	}
	    }
		
	    //Process if Drive Folder was Created
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		SMLaborBackChargeEdit.COMMAND_FLAG, request).compareToIgnoreCase(SMLaborBackChargeEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){  
			SMOption smopt = new SMOption();
			try {
				smopt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit"
						+ "?" + entry.getQueryString()
						+ "&Warning=Error loading SMOption: " + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID());
				return;
			}
			
			String sNewFolderName = smopt.getgdrivelaborbackchargefolderprefix() + entry.getlid() + smopt.getgdrivelaborbackchargefoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
			
			System.out.println("[2019273114179] " + "Pre Create Drive");
			try {
				String sRedirectString = smopt.getgdriveuploadfileurl()
		         		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + smopt.getgdrivelaborbackchargeparentfolderid()
		   				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.SM_LABOR_BACKCHARGE_PARAM_VALUE
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.getlid()
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
		         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
		             	;
				System.out.println("[2019273110186] " + sRedirectString);
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMLaborBackChargeEdit"
					+ "?" + entry.getQueryString()
					+ "&Warning=Could not create folder or upload files: " + e.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				);
				return;
			}
			return;
    	}
	    
		//If it's an Update, process that:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		SMLaborBackChargeEdit.COMMAND_FLAG, request).compareToIgnoreCase(
	    				SMLaborBackChargeEdit.UPDATE_COMMAND_VALUE) == 0){
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMLaborBackCharge.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
			}
	    	
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getlid() + " was successfully saved.",
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
			}
	    }
	    
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
