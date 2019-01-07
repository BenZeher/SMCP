package smic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendor;
import smar.SMOption;
import smcontrolpanel.SMCreateGDriveFolder;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableicpoheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class ICEditPOAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){return;}
	    //Read the entry fields from the request object:
		ICPOHeader entry = new ICPOHeader(request);
		//smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
		String sRedirectString = "";

       	//If Create And Upload Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
   			ICEditPOEdit.COMMAND_FLAG, request).compareToIgnoreCase(ICEditPOEdit.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
       		ICOption icopt = new ICOption();
        	try {
				icopt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(
						ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not upload files to folder - ICOptions could not be loaded: "
								+ e1.getMessage(), "", "");
				return;
			}

        	SMOption smopt = new SMOption();
			try {
				smopt.load(smaction.getsDBID(), getServletContext(),
						smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(
						ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not upload files to folder - SMOptions could not be loaded: "
								+ e1.getMessage(), "", "");
				return;
			}
			
        	String sNewFolderName = icopt.getgdrivepurchaseordersfolderprefix() + entry.getsID() + icopt.getgdrivepurchaseordersfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
			try {
	       		 sRedirectString = smopt.getgdriveuploadfileurl()
	              		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + icopt.getgdrivepurchaseordersparentfolderid()
	        				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sNewFolderName
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGDriveFolder.PO_RECORD_TYPE_PARAM_VALUE
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.getsID()
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
	              		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
	                  	;
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		
		
		//Special cases:
		//********************************
		String sWarning = "";
		
		//If this class has been called from the 'submit vendor' button:
		  if((clsManageRequestParameters.get_Request_Parameter("COMMANDFLAG", request).compareToIgnoreCase(ICEditPOEdit.FINDVENDOR_COMMAND_VALUE)) == 0){
			//Then call the finder to search for vendors:
			sRedirectString = 
				APVendor.getFindVendorLink(
					smaction.getCallingClass(),
					ICEditPOEdit.FOUND_VENDOR_PARAMETER,
					SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID(), 
					getServletContext(),
					smaction.getsDBID()
				)
				+ "*" + ICEditPOEdit.RETURNING_FROM_FIND_VENDOR_FLAG + "=TRUE"
				+ "*" + entry.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				sWarning = "after " + ICEditPOEdit.FIND_VENDOR_BUTTON_NAME + " - error redirecting with string: " + sRedirectString;
				System.out.println("[1490388712] In " + this.toString() + sWarning);
				smaction.redirectAction(
						sWarning, 
						"", 
						ICPOHeader.Paramlid + "=" + entry.getsID()
						);
					return;
			}
			return;
		}
		
		/*
		//Switching between saved ship vias and custom, typed in ship vias:
		if (request.getParameter(ICEditPOEdit.TOGGLE_SHIPVIA_TYPE_BUTTON_NAME) != null){
			//Then set the line to the required value:
			String sToggleLabel = request.getParameter(ICEditPOEdit.TOGGLE_SHIPVIA_TYPE_BUTTON_NAME);
			if (sToggleLabel.compareToIgnoreCase(ICEditPOEdit.SHIPVIA_TYPE_CUSTOM_LABEL) == 0){
				entry.setsshipviacode("");
			}else{
				entry.setsshipviacode("** OTHER **");
			}
			entry.setsshipvianame("");

			smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
			smaction.redirectAction(
				sWarning, 
				"", 
				ICPOHeader.Paramlid + "=" + entry.getsID()
				);
			return;
		}
		*/
	    //End of special cases
		//********************************
		
	    if((clsManageRequestParameters.get_Request_Parameter(
	        	"COMMANDFLAG", request).compareToIgnoreCase(ICEditPOEdit.DELETE_COMMAND_VALUE)) == 0){
		    if (request.getParameter(ICEditPOEdit.DELETE_CHECKBOX_NAME) != null){
			    //Save this now so it's not lost after the delete:
			    String sPONumber = entry.getsponumber();
			    if (!entry.delete(getServletContext(), 
			    		smaction.getsDBID(), 
			    		smaction.getUserID(),
			    		smaction.getFullUserName())){
			    	//System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
			    	smaction.redirectAction(
			    			"Could not delete: " + entry.getErrorMessages(), 
			    			"", 
			    			ICPOHeader.Paramlid + "=" + entry.getsID()
			    			);
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
						smaction.redirectAction(
							"", 
							entry.getObjectName() 
							+ ": " + sPONumber + " was successfully deleted.", "");
					}
					return;
			    }
	    	}else{
	    		smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
			smaction.redirectAction("You chose to delete, but did not check the 'Confirming' box.", "", "");
			return;
	    	}
	    }
		
		//If it's sorting lines process new line numbers:
	    if((clsManageRequestParameters.get_Request_Parameter(
	        	"COMMANDFLAG", request).compareToIgnoreCase(ICEditPOEdit.SORT_LINE_COMMAND_VALUE)) == 0){
	    	
	    	if(entry.getsstatus().compareToIgnoreCase("0") != 0) {
	    		smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
					clsServletUtilities.URLEncode("Can not sort lines in PO status " 
							+ SMTableicpoheaders.getStatusDescription(Integer.parseInt(entry.getsstatus()))), 
					"", 
					ICPOHeader.Paramlid + "=" + entry.getsID()
					);
				return;
	    	}
	    	
	    	
	    	if(!entry.updateLineNumbersAfterSorting(
	    			entry.getsID(),
	    			request,
	    			getServletContext(),
	    			smaction.getsDBID(), 
					smaction.getUserID())) {
	    		smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
					clsServletUtilities.URLEncode("Could not sort lines: " + entry.getErrorMessages()), 
					"", 
					ICPOHeader.Paramlid + "=" + entry.getsID()
					);
	    		
	    	}else {
	    		smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"", 
					clsServletUtilities.URLEncode(entry.getObjectName() + ": " + entry.getsponumber() + " Line order changed succefully."),
					ICPOHeader.Paramlid + "=" + entry.getsID()
					 );
				     
	    	}
			return;    	
	    }
	    
		//If it's an edit, process that:
	    if((clsManageRequestParameters.get_Request_Parameter(
	        	"COMMANDFLAG", request).compareToIgnoreCase(ICEditPOEdit.UPDATE_COMMAND_VALUE)) == 0){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName(),
					false)){
				smaction.getCurrentSession().setAttribute(ICPOHeader.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					ICPOHeader.Paramlid + "=" + entry.getsID()
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
						entry.getObjectName() + ": " + entry.getsponumber() + " was successfully saved.",
						ICPOHeader.Paramlid + "=" + entry.getsID()
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