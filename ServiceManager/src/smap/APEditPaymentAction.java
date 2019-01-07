package smap;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditPaymentAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APEditBatches)){return;}
	    //Read the entry fields from the request object:
		APBatchEntry entry = new APBatchEntry(request);
		
		//System.out.println("[1509034610] - batch entry: " + entry.dumpData());

		//Remove any session object:
	    if (smaction.getCurrentSession().getAttribute(APBatchEntry.OBJECT_NAME) != null){
	    	smaction.getCurrentSession().removeAttribute(APBatchEntry.OBJECT_NAME);
	    }
		
	    //Save this now so it's not lost after the delete or save:
	    String sBatchNumber = entry.getsbatchnumber();
	    String sEntryNumber = entry.getsentrynumber();
	    
	    //Set the calling class back to the 'edit' class here:
	    String sCallingClass = "smap.APEditBatchesEdit";
	    
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.COMMAND_FLAG, request);
	    
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_DELETE) == 0){
		    APBatch batch = new APBatch(sBatchNumber);
		    try {
				batch.deleteEntry(
					sBatchNumber, 
					entry.getsentrynumber(), 
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
					);
			} catch (Exception e) {
		    	smaction.redirectAction(
		    		"Could not delete: " + e.getMessage(), 
		    		"", 
    	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
		    		);
				return;
			}

	    	//If the delete succeeded, the entry will be initialized:
	    	//Re-set the job number in the new, blank entry:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				entry = new APBatchEntry();
				entry.setsbatchnumber(sBatchNumber);
				smaction.setCallingClass("smap.APEditBatchesEdit");
				smaction.redirectAction(
					"", 
					APBatchEntry.OBJECT_NAME + ": " + sEntryNumber + " was successfully deleted.", 
					SMTableapbatches.lbatchnumber + "=" + sBatchNumber
					+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE) 
					+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)	
				);
			}
			return;
	    }

		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_SAVE) == 0){
    		APBatch batch = null;
    		String sNewEntryID = "";
	    	try {
	    		batch = new APBatch(entry.getsbatchnumber());
	    		sNewEntryID = batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
		    	//Reload the updated batch entry so we know we have the correct info in it:
	    		entry.setslid(sNewEntryID);
		    	entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserID());
		    	//System.out.println("[1509034611] - batch entry: " + entry.dumpData());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = APBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + sNewEntryID
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
	    	
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				//System.out.println("[1500996069] - " + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
	    		//+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
	    		//+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
	    		//+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
	    		//+ "&" + "CallingClass=" + sCallingClass);
				smaction.redirectAction(
					"", 
					APBatchEntry.OBJECT_NAME + ": " + entry.getsentrynumber() + " was successfully saved.",
	   	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + sNewEntryID
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
			return;
	    }
    	
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_SAVE_AND_ADD) == 0){
    		APBatch batch = null;
    		String sNewEntryID = "";
	    	try {
	    		batch = new APBatch(entry.getsbatchnumber());
	    		sNewEntryID = batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
		    	//Reload the updated batch entry so we know we have the correct info in it:
	    		entry.setslid(sNewEntryID);
		    	entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserID());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = APBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + sNewEntryID
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
	    	
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				String sRedirectString =
					SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit"
    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
    	    		+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
    	    		+ "&" + APEditBatchesEdit.BATCH_EDITABLE_PARAMETER + "=" + APEditBatchesEdit.BATCH_EDITABLE_PARAMETER_VALUE_TRUE
    	    		+ "&" + SMTableapbatchentries.lid + "=-1"
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(entry.getientrytype())
    	    		+ "&" + "CallingClass=" + "smap.APEditBatchesEdit"
    	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				;
					
   				try {
   					response.sendRedirect(sRedirectString);
   				} catch (IOException e) {
   					String sWarning = "after " + "Update and add new' command" + " - error redirecting with string: " + sRedirectString;
   					//System.out.println("Error [1490389109] In " + this.toString() + sWarning);
   					smaction.redirectAction(
   							sWarning, 
   							"", 
   							SMTableapbatchentries.lid + "=" + entry.getslid()
   							+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
   							);
   						return;
   				}
   				return;
			}
			return;
	    }
    	
		//If this class has been called from the 'find vendor' button:
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_FINDVENDOR) == 0){
			//Then call the finder to search for vendors:
			String sRedirectString = 
				APVendor.getFindVendorLink
					(smaction.getCallingClass(),
					APEditPaymentEdit.FOUND_VENDOR_PARAMETER,
					SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID(),
					getServletContext(),
					smaction.getsDBID()
					)

				+ "*" + SMTableapbatchentries.lid + "=" + entry.getslid()
				+ "*" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				+ "*" + APEditPaymentEdit.RETURNING_FROM_FIND_VENDOR_FLAG + "=TRUE"
				+ "*" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
	    		+ "*" + "CallingClass=" + sCallingClass
				+ "*" + entry.getQueryString().replace("&", "*")
			;
			//System.out.println("[1529094292] sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				String sWarning = "after " + "finding vendor" + " - error redirecting with string: " + sRedirectString;
				//System.out.println("Error [1490389109] In " + this.toString() + sWarning);
				smaction.redirectAction(
						sWarning, 
						"", 
						SMTableapbatchentries.lid + "=" + entry.getslid()
						+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
						);
					return;
			}
			return;
		}
    	
		//If this class has been called from the 'refresh vendor info' button:
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_UPDATEVENDORINFO) == 0){
    		smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
			smaction.redirectAction(
				"", 
				"", 
				SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + APEditPaymentEdit.RETURNING_FROM_UPDATEVENDORINFO_FLAG + "=true"
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
					+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
				);
			return;
		}
    	
    	//Process unapply line
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_UNAPPLYLINE) == 0){
    		String sLineNumber = clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.LINE_NUMBER_TO_UNAPPLY_PARAM, request);
    		try {
	    		entry.removeLineByLineNumber(sLineNumber);
	    		APBatch batch = new APBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not unapply line number " + sLineNumber + " - " + e.getMessage(), 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    				+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
	    			+ "&" + APEditPaymentEdit.RETURN_TO_TABLES_BOOKMARK + "=Y"
	    			+ "&" + "CallingClass=" + sCallingClass
				);
				return;
			}
			//If the line removal succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					"Line number " + sLineNumber + " was successfully removed.",
	   	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + APEditPaymentEdit.RETURN_TO_TABLES_BOOKMARK + "=Y"
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
			return;
    	}
    	
    	//Process apply line
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_APPLYTODOC) == 0){
    		String sApplyToDocNumber = clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.APPLYTODOCNUMBER_TO_APPLY_PARAM, request);
    		try {
	    		entry.applyLineToDocNumber(sApplyToDocNumber, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
	    		APBatch batch = new APBatch(entry.getsbatchnumber());
	    		
	    		//System.out.println("[1497977634] line disc amt = " + entry.getLineArray().get(0).getsbddiscountappliedamt());
	    		
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
	    		
	    		//System.out.println("[1497977635] batch line disc amt = " + batch.getBatchEntryArray().get(0).getLineArray().get(0).getsbddiscountappliedamt());
	    		
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				smaction.redirectAction(
					"Error [1537907939] - Could not apply to document number '" + sApplyToDocNumber + "' - " + e.getMessage(), 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
	    			+ "&" + APEditPaymentEdit.RETURN_TO_TABLES_BOOKMARK + "=Y"
	    			+ "&" + "CallingClass=" + sCallingClass
				);
				return;
			}
			//If the line apply succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					"Payment was successfully applied to document number '" + sApplyToDocNumber + "'.",
	   	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + APEditPaymentEdit.RETURN_TO_TABLES_BOOKMARK + "=Y"
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
			return;
    	}
    	
    	//Process remove line (only happens on misc payments)
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_REMOVELINE) == 0){
    		String sLineNumber = clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.LINE_NUMBER_TO_DELETE_PARAM, request);
    		try {
	    		entry.removeLineByLineNumber(sLineNumber);
	    		APBatch batch = new APBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not remove line number " + sLineNumber + " - " + e.getMessage(), 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + APEditPaymentEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditPaymentEdit.RECORDWASCHANGED_FLAG, request)
	    			+ "&" + "CallingClass=" + sCallingClass
				);
				return;
			}
			//If the line removal succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					"Line number " + sLineNumber + " was successfully removed.",
	   	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
			return;
    	}
    	
    	//If the user chose to print a check:
    	if (sCommandValue.compareToIgnoreCase(APEditPaymentEdit.COMMAND_VALUE_PRINT_CHECKS) == 0){
    		response.sendRedirect(
    			SMUtilities.getURLLinkBase(getServletContext()) + "smap.APPrintChecksEdit"
    			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    			+ "&" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    			+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    			+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    			+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    			+ "&" + APPrintChecksEdit.CLEAR_UNFINALIZED_CHECKS + "=Y"
    			+ "&" + "CallingClass=" + sCallingClass
    			);
    		return;
    	}
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}