package smic;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendor;
import smgl.GLAccount;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import ServletUtilities.clsManageRequestParameters;

public class ICEnterInvoiceAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String UPDATE_VENDOR_LOG_ENTRY = "UPDATEVENDORONPO";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEnterInvoices)){return;}
		
		//Clear any IC PO Invoice object in the session, in case it's there:
		smaction.getCurrentSession().removeAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT);
		
	    //Read the entry fields from the request object:
		ICPOInvoice entry = new ICPOInvoice(request);
		
		//Special cases:
		//********************************
		String sWarning = "";
		
		//If this class has been called from the 'find receipt' button:
		if (request.getParameter(ICEnterInvoiceEdit.FIND_RECEIPT_BUTTON_NAME) != null){
			//Then call the finder to search for receipts:
			String sRedirectString = "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=" + smar.FinderResults.UNINVOICED_PO_RECEIPT_OBJECT
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + ICEnterInvoiceEdit.INPUT_RECEIPT_FIELD
				+ "&SearchField1=" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
				+ "&SearchFieldAlias1=PO%20ID%20Number"
				+ "&SearchField2=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
				+ "&SearchFieldAlias2=Vendor%20Number"
				+ "&SearchField3=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
				+ "&SearchFieldAlias3=PO%20Reference"
				//+ "&SearchField4=" + SMTableicpoheaders
				//+ "&SearchFieldAlias4=Address%202"
				+ "&ResultListField1="  + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				+ "&ResultHeading1=Receipt%20ID"
				+ "&ResultListField2="  + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
				+ "&ResultHeading2=PO%20ID"
				+ "&ResultListField3="  + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
				+ "&ResultHeading3=Receipt%20Date"
				+ "&ResultListField4="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
				+ "&ResultHeading4=Vendor"
				+ "&ResultListField5="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
				+ "&ResultHeading5=Vendor%20Name"
				+ "&ResultListField6="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
				+ "&ResultHeading6=PO%20Reference"
				+ "&ResultListField7="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sdescription
				+ "&ResultHeading7=PO%20Description"
				+ "&ResultListField8="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
				+ "&ResultHeading8=PO%20Comment"
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*RETURNINGFROMFINDER=TRUE"
				+ "*CallingClass=" + smaction.getCallingClass()
				//+ "*" + entry.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				sWarning = "after " + ICEnterInvoiceEdit.FIND_RECEIPT_BUTTON_NAME 
				+ " - error redirecting with string: " + sRedirectString;
				System.out.println("Error [1456277745] In " + this.toString() + sWarning);
				smaction.redirectAction(
						sWarning, 
						"", 
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
						);
					return;
			}
			return;
		}
		
		//If this class has been called from the 'submit vendor' button:
		if (request.getParameter(ICEnterInvoiceEdit.FIND_VENDOR_BUTTON_NAME) != null){
			//Then call the finder to search for vendors:
			String sRedirectString = APVendor.getFindVendorLink(
				smaction.getCallingClass(), 
				ICEnterInvoiceEdit.FOUND_VENDOR_PARAMETER, 
				SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID(), 
				getServletContext(),
				smaction.getsDBID()
				)
				+ "*CallingClass=" + smaction.getCallingClass()
				+ "*RETURNINGFROMFINDER=TRUE"		
			;

			//System.out.println("sRedirectString = " + sRedirectString);
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				sWarning = "after " + ICEnterInvoiceEdit.FIND_VENDOR_BUTTON_NAME + " - error redirecting with string: " + sRedirectString;
				System.out.println("Error [1456277746] In " + this.toString() + sWarning);
				smaction.redirectAction(
						sWarning, 
						"", 
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
						);
					return;
			}
			return;
		}
		
		//If this class has been called from the 'Add receipt' button, we need to add the lines
		//from that receipt:
		//ADD_RECEIPT_BUTTON_NAME
		if (request.getParameter(ICEnterInvoiceEdit.ADD_RECEIPT_BUTTON_NAME) != null){
			if(!entry.addReceiptLines(
					clsManageRequestParameters.get_Request_Parameter(ICEnterInvoiceEdit.INPUT_RECEIPT_FIELD, request),
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName())){
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				smaction.redirectAction(
					"Could not add receipt lines: " + entry.getErrorMessages(), 
					"", 
					ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				return;
			}else{
				//If we successfully added the receipt lines, put the entry object in the current session
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getM_slid() + " receipt was successfully added.",
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				}
			}
			return;
		}
		
		//If this class has been called to update the vendor on the receipt:
		if (request.getParameter(ICEnterInvoiceEdit.UPDATE_VENDOR_BUTTON_NAME) != null){
			try {
				updateVendorOnReceipt(request, 
						smaction.getsDBID(), 
						smaction.getUserName(), 
						smaction.getUserID(), 
						smaction.getFullUserName(), 
						entry.getM_svendor());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				smaction.redirectAction(
					e1.getMessage(), 
					"", 
					ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				return;
			}

			if(!entry.addReceiptLines(
					clsManageRequestParameters.get_Request_Parameter(ICEnterInvoiceEdit.INPUT_RECEIPT_FIELD, request),
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName())){
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				smaction.redirectAction(
					"Could not add receipt lines: " + entry.getErrorMessages(), 
					"", 
					ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				return;
			}else{
				//If we successfully added the receipt lines, put the entry object in the current session
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getM_slid() + " vendor was updated and receipt was successfully added.",
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				}
			}
			return;
		}
		
		//If this class was called by a request to remove a receipt line:
    	Enumeration <String> eParams = request.getParameterNames();
    	String sRemoveLineParam = "";
    	String sRemoveReceiptLineID = "";
    	while (eParams.hasMoreElements()){
    		sRemoveLineParam = eParams.nextElement();
    		if (sRemoveLineParam.contains(ICEnterInvoiceEdit.REMOVE_RECEIPT_LINE_BUTTON_NAME)){
    			sRemoveReceiptLineID = sRemoveLineParam.substring(
    				(ICEnterInvoiceEdit.REMOVE_RECEIPT_LINE_BUTTON_NAME).length(), sRemoveLineParam.length());
    			entry.removeReceiptLine(sRemoveReceiptLineID);
				smaction.getCurrentSession().removeAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT);
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getM_slid() + " receipt line was successfully removed.",
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
    			return;
    		}
    	}
		
		//If this class has been called from the 'refresh vendor info' button:
		if (request.getParameter(ICEnterInvoiceEdit.REFRESH_VENDOR_INFO_BUTTON_NAME) != null){
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			smaction.redirectAction(
				"", 
				"", 
				ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + ICEnterInvoiceEdit.REFRESH_VENDOR_INFO_PARAMETER + "=true"
					//+ "&" + entry.getQueryString()
				);
			return;
		}
		//If this class has been called from the 'recalculate terms' button:
		if (request.getParameter(ICEnterInvoiceEdit.CALCULATE_TERMS_BUTTON_NAME) != null){
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			smaction.redirectAction(
				"", 
				"", 
				ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + ICEnterInvoiceEdit.CALCULATE_TERMS_PARAMETER + "=true"
					//+ "&" + entry.getQueryString()
				);
			return;
		}
		
		//If this class has been called from the 'recalculate line totals' button:
		if (request.getParameter(ICEnterInvoiceEdit.RECALCULATETOTALS_BUTTON_NAME) != null){
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			smaction.redirectAction(
				"", 
				"", 
				ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + ICEnterInvoiceEdit.RECALCULATETOTALS_PARAMETER + "=true"
					//+ "&" + entry.getQueryString()
				);
			return;
		}
		
		//IF this class has been called from the 'invoice all lines' button:
		if (request.getParameter(ICEnterInvoiceEdit.INVOICE_ALL_LINES_BUTTON_NAME) != null){
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			smaction.redirectAction(
				"", 
				"", 
				ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&" + ICEnterInvoiceEdit.INVOICE_ALL_LINES_PARAMETER + "=true"
					//+ "&" + entry.getQueryString()
				);
			return;
		}

		//If this class has been called by a finder request for the expense account:
    	Enumeration <String> e = request.getParameterNames();
    	String sParam = "";
    	String sRecordNumber = "";
    	while (e.hasMoreElements()){
    		sParam = e.nextElement();
    		if (sParam.contains(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER)){
    			sRecordNumber = sParam.substring(
    			(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER).length(), sParam.length());
    		}
    	}

		if (sRecordNumber.compareToIgnoreCase("") != 0){
			//Then call the finder to search for items:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=" + "ACTIVE " + GLAccount.Paramobjectname //We only want ACTIVE accounts listed:
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER + sRecordNumber
				+ "&SearchField1=" + SMTableglaccounts.sDesc
				+ "&SearchFieldAlias1=Description"
				+ "&SearchField2=" + SMTableglaccounts.sAcctID
				+ "&SearchFieldAlias2=Account%20Number"
				+ "&ResultListField1="  + SMTableglaccounts.sAcctID
				+ "&ResultHeading1=Account%20Number."
				+ "&ResultListField2="  + SMTableglaccounts.sDesc
				+ "&ResultHeading2=Description"
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*RETURNINGFROMFINDER=TRUE"
				//+ entry.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException ex) {
				System.out.println("In " + this.toString() 
					+ "After FINDGLACCOUNT - error redirecting with string: " + sRedirectString);
				smaction.redirectAction(
					"After FINDGLACCOUNT - error redirecting with string: " + sRedirectString, 
					"", 
					ICPOInvoice.ParamlID + "=" + entry.getM_slid()
				);
				return;
			}
			return;
		}
	    //End of special cases
		//********************************
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sInvoiceID = entry.getM_slid();
			    if (!entry.delete(getServletContext(), 
			    		smaction.getsDBID(), 
			    		smaction.getUserName(),
			    		smaction.getUserID(),
			    		smaction.getFullUserName())){
			    	//System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
			    	smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			    	smaction.redirectAction(
			    			"Could not delete: " + entry.getErrorMessages(), 
			    			"", 
			    			ICPOInvoice.ParamlID + "=" + entry.getM_slid()
			    			);
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the invoice ID in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEnterInvoiceSelection"
								+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
								+ "&Status=Invoice " + sInvoiceID + " successfully deleted.");
					}
					return;
			    }
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
			if(!entry.save_with_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					)){
				smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session
				//smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getM_slid() + " was successfully saved.",
						ICPOInvoice.ParamlID + "=" + entry.getM_slid()
					);
				}
			}
	    }
	    
	    //If the new line button is pressed
	    if (request.getParameter(ICEnterInvoiceEdit.ADD_NEW_LINE_PARAMETER) != null){
	    	//increment number of lines by 1
	    	int iNumberOfNewLines = Integer.parseInt(entry.getsnumberofnewlines());
	    	iNumberOfNewLines++;
	    	String sNumberOfNewLines = Integer.toString(iNumberOfNewLines);	
	    	entry.setsnumberofnewlines(sNumberOfNewLines);
	    	//Add empty fields to new line
	    	entry.getsNewLineDescription().add("");
			entry.getsNewLineExpenseAcct().add("");
			entry.getsNewLineInvoicedCost().add("");
			smaction.getCurrentSession().removeAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT);
			smaction.getCurrentSession().setAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT, entry);
			//System.out.println("[1456258157] - Number of New Lines: " + entry.getsnumberofnewlines());
			for(int i = 0; i < iNumberOfNewLines; i++){
				//System.out.println("[1456258158] - New Line " + i +" Desc: " + entry.getsNewLineDescription(i));
				//System.out.println("[1456258158] - New Line " + i +" Acct: " + entry.getsNewLineExpenseAcct(i));
				//System.out.println("[1456258158] - New Line " + i +" Amount: " + entry.getsNewLineInvoicedCost(i));
			}
			smaction.redirectAction(
					"", 
					"", 
					""
					);
	    	return;
	    }
		return;
	}
	private void updateVendorOnReceipt(HttpServletRequest req, String sDBID, String sUser, String sUserID, String sUserFullName, String sVendor) throws Exception{
		if (req.getParameter(ICEnterInvoiceEdit.UPDATE_VENDOR_CONFIRM_CHECKBOX) == null){
			throw new Exception("You chose to update the vendor on this receipt, but did not check the box to confirm.");
		}
		String sReceiptID = clsManageRequestParameters.get_Request_Parameter(ICEnterInvoiceEdit.INPUT_RECEIPT_FIELD, req);
		try {
			@SuppressWarnings("unused")
			long lReceiptID = Long.parseLong(sReceiptID);
		} catch (NumberFormatException e1) {
			throw new Exception("Invalid receipt number: '" + sReceiptID + "'."); 
		}
		ICPOReceiptHeader porec = new ICPOReceiptHeader();
		porec.setsID(sReceiptID);
		if (!porec.load(getServletContext(), sDBID, sUserID, sUserFullName)){
			throw new Exception("Could not load receipt number: '" + sReceiptID + "'.");
		}
		ICPOHeader pohead = new ICPOHeader();
		pohead.setsID(porec.getspoheaderid());
		if (!pohead.load(getServletContext(), sDBID, sUserID, sUserFullName)){
			throw new Exception("Could not load PO #" + porec.getspoheaderid() + " to update vendor.");
		}
		String sOriginalVendor = pohead.getsvendor();
		String sOriginalVendorName = pohead.getsvendorname();
		
		try {
			pohead.updateVendor(sVendor, getServletContext(), sDBID, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error updating vendor - " + e.getMessage());
		}
		
		//Create a log entry detailing the change:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			UPDATE_VENDOR_LOG_ENTRY, 
			"Updated PO #" + pohead.getsID() + " to vendor '" + sVendor + "'", 
			"PO originally had vendor '" + sOriginalVendor + "' - " + sOriginalVendorName + " and was created by " + pohead.getscreatedbyfullname(), 
			"[1400077057]");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}