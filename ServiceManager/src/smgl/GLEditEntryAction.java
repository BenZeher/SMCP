package smgl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditEntryAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLEditBatches)){return;}
	    //Read the entry fields from the request object:
		GLTransactionBatchEntry entry = new GLTransactionBatchEntry(request);
		
	    //Save this now so it's not lost after the delete or save:
	    String sBatchNumber = entry.getsbatchnumber();
	    String sEntryNumber = entry.getsentrynumber();
	    
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.COMMAND_FLAG, request);
	    
	    //Set the calling class back to the 'edit' class here:
	    String sCallingClass = "smgl.GLEditEntryEdit";
	    
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_DELETE) == 0){
		    GLTransactionBatch batch = new GLTransactionBatch(sBatchNumber);
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
    	    		SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
    	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.RECORDWASCHANGED_FLAG, request)
    	    		+ "&" + "CallingClass=" + sCallingClass
		    		);
				return;
			}

	    	//If the delete succeeded, the entry will be initialized:
	    	//Re-set the job number in the new, blank entry:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				entry = new GLTransactionBatchEntry();
				entry.setsbatchnumber(sBatchNumber);
				smaction.setCallingClass("smgl.GLEditEntriesEdit");
				smaction.redirectAction(
					"", 
					GLTransactionBatchEntry.OBJECT_NAME + ": " + sEntryNumber + " was successfully deleted.", 
					SMTablegltransactionbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.RECORDWASCHANGED_FLAG, request)
				);
			}
			return;
	    }

		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_SAVE) == 0){
	    	try {
	    		GLTransactionBatch batch = new GLTransactionBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				entry.setsentrynumber(sEntryNumber);
				smaction.getCurrentSession().setAttribute(GLTransactionBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = GLTransactionBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTablegltransactionbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.RECORDWASCHANGED_FLAG, request)
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
					GLTransactionBatchEntry.OBJECT_NAME + ": " + entry.getsentrynumber() + " was successfully saved.",
	   	    		SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
	    }
    	
		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_SAVE_AND_ADD) == 0){
	    	try {
	    		GLTransactionBatch batch = new GLTransactionBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				//Reset the entry number, in case the update function updated it:
				entry.setsentrynumber(sEntryNumber);
				smaction.getCurrentSession().setAttribute(GLTransactionBatchEntry.OBJECT_NAME, entry);
				
				String sWarning = GLTransactionBatch.stripOutInvalidEntryErrors(e.getMessage());
				//sWarning = e.getMessage();
				smaction.redirectAction(
					"Could not save: " + sWarning, 
					"", 
    	    		SMTablegltransactionbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.RECORDWASCHANGED_FLAG, request)
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
					SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditEntryEdit"
    	    		+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + GLEditBatchesEdit.BATCH_EDITABLE_PARAMETER + "=" + GLEditBatchesEdit.BATCH_EDITABLE_PARAMETER_VALUE_TRUE
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=-1"
    	    		+ "&" + "CallingClass=" + "smgl.GLEditBatchesEdit"
    	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				;
					
   				try {
   					response.sendRedirect(sRedirectString);
   				} catch (IOException e) {
   					String sWarning = "after Save And Add command - error redirecting with string: " + sRedirectString;
   					smaction.redirectAction(
   							sWarning, 
   							"", 
   							SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
   							);
   						return;
   				}
   				return;
			}
	    }
    	
    	//Process remove line
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_REMOVELINE) == 0){
    		String sLineNumber = clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.LINE_NUMBER_TO_DELETE_PARAM, request);
    		try {
	    		entry.removeLineByLineNumber(sLineNumber);
	    		GLTransactionBatch batch = new GLTransactionBatch(entry.getsbatchnumber());
	    		batch.updateBatchEntry(entry, getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(GLTransactionBatchEntry.OBJECT_NAME, entry);
				smaction.redirectAction(
					"Could not remove line number " + sLineNumber + " - " + e.getMessage(), 
					"", 
    	    		SMTablegltransactionbatches.lbatchnumber + "=" + sBatchNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + sEntryNumber
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.RECORDWASCHANGED_FLAG, request)
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
	   	    		SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + entry.getsentrynumber()
    	    		+ "&" + SMTablegltransactionbatchentries.lid + "=" + entry.getslid()
    	    		+ "&" + "CallingClass=" + sCallingClass
				);
			}
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