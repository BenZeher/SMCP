package smap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ConnectionPool.WebContextParameters;
import SMClasses.SMOption;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditVendorAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APEditVendors)){return;}
	    //Read the entry fields from the request object:
		APVendor entry = new APVendor(request);
		
		//Get the command value from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(APEditVendorsEdit.COMMAND_FLAG, request);
		
		//If Edit remit to location button, process that:
		if(sCommandValue.compareToIgnoreCase(APEditVendorsEdit.EDIT_LOCATIONS_COMMAND_VALUE) == 0){

			if(!entry.load (getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
				smaction.getCurrentSession().setAttribute(APVendor.ParamObjectName, entry);
		    	smaction.redirectAction(
			    		"Vendor must be saved before editing remit to locations: " + entry.getErrorMessages(), 
			    		"", 
			    		APVendor.Paramsvendoracct + "=" + entry.getsvendoracct()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
			    		);
				return;
			}else
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
	    	        + "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
				);
					return;
			
		}
		
		//If Create And Upload Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
   			APEditVendorsEdit.COMMAND_FLAG, request).compareToIgnoreCase(APEditVendorsEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
       		APVendor apvendor = new APVendor();
        	try {
        		apvendor.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(APVendor.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not upload files to folder - APVendor could not be loaded: "
								+ e1.getMessage(), "", "");
				return;
			}

        	APOptions apopt = new APOptions();
			try {
				apopt.load(smaction.getsDBID(), getServletContext(), smaction.getUserID());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(
						APVendor.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not upload files to folder - APOptions could not be loaded: "
								+ e1.getMessage(), "", "");
				return;
			}
			
			SMOption smopt = new SMOption();
			try {
				smopt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(
						APVendor.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not upload files to folder - SMOptions could not be loaded: "
								+ e1.getMessage(), "", "");
				return;
			}
			
        	String sNewFolderName = apopt.getgdrivevendorfolderprefix() + entry.getsvendoracct() + apopt.getgdrivevendorfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
			try {
				 String sRedirectString = smopt.getgdriveuploadfileurl()
			         		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + apopt.getgdrivevendorparentfolderid()
			   				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
			         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.AP_VENDOR_RECORD_TYPE_PARAM_VALUE
			         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.getsvendoracct()
			         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
			         		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
			             	;
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		
    	//System.out.println("[1508352380] - command flag = '" + sCommandValue + "'.");
    	
	    if(sCommandValue.compareToIgnoreCase(APEditVendorsEdit.DELETE_COMMAND_VALUE) == 0){
		    if (clsManageRequestParameters.get_Request_Parameter(APEditVendorsEdit.CONFIRM_DELETE_CHECKBOX, request)
			    	.compareToIgnoreCase(APEditVendorsEdit.CONFIRM_DELETE_CHECKBOX) == 0){
			    //Save this now so it's not lost after the delete:
			    String sID = entry.getsvendoracct();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
			    		"Could not delete vendor '" + sID + " - " + e.getMessage(), 
			    		"", 
			    		APVendor.Paramsvendoracct + "=" + entry.getsvendoracct()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
			    		);
					return;
				}

			    //If the delete succeeded, the entry will be initialized:
		    	//Re-set the job number in the new, blank entry:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.getCurrentSession().setAttribute(APVendor.ParamObjectName, entry);
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + sID + " was successfully deleted.", 
						""
					);
				}
				return;
	    	}else{
	    		smaction.redirectAction(
						"You chose to delete without checking the confirm before deleting checkbox.", 
						"", 
						APVendor.Paramsvendoracct + "=" + entry.getsvendoracct()
						);
				return;
	    	}
	    }
		//If it's an edit, process that:
	    boolean bSavingNewVendor = clsManageRequestParameters.get_Request_Parameter(
	    		SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request).compareToIgnoreCase("") != 0;
	    if(sCommandValue.compareToIgnoreCase(APEditVendorsEdit.SAVE_COMMAND_VALUE) == 0){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME),
					bSavingNewVendor)){
				smaction.getCurrentSession().setAttribute(APVendor.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not save: " + entry.getErrorMessages(), 
						"", 
						APVendor.Paramsvendoracct + "=" + entry.getsvendoracct()
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
						);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getsvendoracct() + " was successfully saved.",
						entry.getQueryString()
					);
				}
			}
	    }
		return;
	}
	
	private String getCreateGDriveReturnURL(HttpServletRequest req) throws Exception{
		String sTemp = "";
		try {
			sTemp = clsServletUtilities.getServerURL(req, getServletContext());
		}catch(Exception e) {
			throw new Exception("Error [1542748060] "+e.getMessage());
		}
		sTemp += "/" + WebContextParameters.getInitWebAppName(getServletContext()) + "/";
		sTemp += "smcontrolpanel.SMCreateGDriveFolder";
		return sTemp;
	}
	
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1397679126] In " + SMUtilities.getFullClassName(this.toString()) 
					+ ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1397679127] In " + SMUtilities.getFullClassName(this.toString()) 
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