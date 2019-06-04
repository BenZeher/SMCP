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

public class APEditReversalAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APEditBatches)){return;}
	    //Read the entry fields from the request object:
		APBatchEntry entry = new APBatchEntry(request);
		//smaction.getCurrentSession().setAttribute(BKBank.ParamObjectName, entry);
		
	    //Save this now so it's not lost after the delete or save:
	    String sBatchNumber = entry.getsbatchnumber();
	    String sEntryNumber = entry.getsentrynumber();
	    
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.COMMAND_FLAG, request);
	    
	    //System.out.println("[1529095547] - sCommandValue = '" + sCommandValue + "'");
	    
	    //Set the calling class back to the 'edit' class here:
	    String sCallingClass = "smap.APEditBatchesEdit";
	    
    	if (sCommandValue.compareToIgnoreCase(APEditInvoiceEdit.COMMAND_VALUE_DELETE) == 0){
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
    	    		+ "&" + APEditInvoiceEdit.RECORDWASCHANGED_FLAG + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.RECORDWASCHANGED_FLAG, request)
    	   	    	+ "&" + APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE, request)
    	    		+ "&" + "CallingClass=" + sCallingClass
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
    	    		+ "&" + APEditInvoiceEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.RECORDWASCHANGED_FLAG, request)
    	   	    	+ "&" + APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE, request)
				);
			}
			return;
	    }

		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(APEditInvoiceEdit.COMMAND_VALUE_SAVE) == 0){
	    	try {
	    		APBatch batch = new APBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				entry.setsentrynumber(sEntryNumber);
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = APBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + APEditInvoiceEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.RECORDWASCHANGED_FLAG, request)
    	   	    	+ "&" + APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE, request)
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					APBatchEntry.OBJECT_NAME + ": " + entry.getsentrynumber() + " was successfully saved.",
	   	    		SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	   	    	+ "&" + APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE, request)
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
	    }
    	
		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(APEditInvoiceEdit.COMMAND_VALUE_SAVE_AND_ADD) == 0){
	    	try {
	    		APBatch batch = new APBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				entry.setsentrynumber(sEntryNumber);
				smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = APBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
    	    		+ "&" + APEditInvoiceEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.RECORDWASCHANGED_FLAG, request)
    	   	    	+ "&" + APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(APEditInvoiceEdit.PARAM_TOGGLEUNAPPLIEDTABLE, request)
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
	    	//smaction.getCurrentSession().removeAttribute(BKBank.ParamObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				String sRedirectString =
					SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditInvoiceEdit"
    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTableapbatches.ibatchtype + "=" + SMTableapbatches.AP_BATCH_TYPE_INVOICE
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
   					String sWarning = "after Save And Add command - error redirecting with string: " + sRedirectString;
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
	    }
    	
		//If this class has been called from the 'find vendor' button:
    	if (sCommandValue.compareToIgnoreCase(APEditReversalEdit.COMMAND_VALUE_FINDVENDOR) == 0){
			//Then call the finder to search for vendors:
			String sRedirectString = 
				APVendor.getFindVendorLink(
					smaction.getCallingClass(),
					APEditPaymentEdit.FOUND_VENDOR_PARAMETER,
					"",
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

			//System.out.println("[1529095141] sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				String sWarning = "after " + APEditReversalEdit.BUTTON_LABEL_FINDVENDOR + " - error redirecting with string: " + sRedirectString;
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
    	if (sCommandValue.compareToIgnoreCase(APEditReversalEdit.COMMAND_VALUE_UPDATEVENDORINFO) == 0){
    		smaction.getCurrentSession().setAttribute(APBatchEntry.OBJECT_NAME, entry);
			smaction.redirectAction(
				"", 
				"", 
				SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + APEditReversalEdit.RETURNING_FROM_UPDATEVENDORINFO_FLAG + "=true"
					+ "&" + "CallingClass=" + sCallingClass
    	    		+ "&" + APEditReversalEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(APEditReversalEdit.RECORDWASCHANGED_FLAG, request)
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