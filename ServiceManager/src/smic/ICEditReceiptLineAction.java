package smic;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ICEditReceiptLineAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditReceipts)){return;}
	    //Read the entry fields from the request object:
		ICPOReceiptLine entry = new ICPOReceiptLine(request);
    	if(bDebugMode){
    		System.out.println("In " + this.toString() + " - po receipt line dump immediately after ICPOReceiptLine(request) = "
    				+ entry.read_out_debug_data()
    		);
    	}
		//System.out.println("In " + this.toString() + " line dump = " + entry.read_out_debug_data());
		smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
		
		//Get the 'concurrency' data for the PO receipt:
		String sRcptLastUpdatedTime = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramsdattimelastupdated, request);
		String sRcptLastUpdatedProcess = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramslastupdatedprocess, request);
		String sRcptLastUpdatedUserFullName = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramslastupdatedbyfullname, request);
		
	    //Special case - it's a request to calculate the costs:
	    if(clsManageRequestParameters.get_Request_Parameter(
    		ICEditReceiptLineEdit.CALCULATECOSTS_BUTTON, request).compareToIgnoreCase("") != 0){
			try {
				entry.calculateCosts();
				smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
				smaction.redirectAction(
					"",
					"Costs recalculated.",
					ICPOReceiptLine.Paramlid + "=" + entry.getsID()
						+ "&" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsreceiptheaderid()
						+ "&" + ICPOReceiptLine.Paramlpolineid + "=" + entry.getspolineid()
				);
		    	return;
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
				smaction.redirectAction(
					"Error recalculating cost - " + e.getMessage(),
					"",
					ICPOReceiptLine.Paramlid + "=" + entry.getsID()
						+ "&" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsreceiptheaderid()
						+ "&" + ICPOReceiptLine.Paramlpolineid + "=" + entry.getspolineid()
				);
				return;
			}

	    }

	  //Special case - it's a request to 'Validate' the line:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		"VALIDATE", request).compareToIgnoreCase("") != 0){
	    	if(bDebugMode){
	    		System.out.println("In " + this.toString() + " - po receipt line dump = "
	    				+ entry.read_out_debug_data()
	    		);
	    	}
			if(!entry.validate_entry_fields(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					false)){
						smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
						smaction.redirectAction(
							entry.getErrorMessages(), 
							"", 
							ICPOReceiptLine.Paramlid + "=" + entry.getsID()
								+ "&" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsreceiptheaderid()
						);
				return;
			}else{
				//If the validation succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
				smaction.redirectAction(
					"",
					"Receipt line validated successfully.",
					ICPOReceiptLine.Paramlid + "=" + entry.getsID()
						+ "&" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsreceiptheaderid()
						+ "&" + ICPOReceiptLine.Paramlpolineid + "=" + entry.getspolineid()
				);
			}
	    	return;
	    }

		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	try {
				processSave(
					entry, 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName(),
					sRcptLastUpdatedTime,
					sRcptLastUpdatedProcess,
					sRcptLastUpdatedUserFullName);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(ICPOReceiptLine.ParamObjectName, entry);
				smaction.redirectAction(
					e.getMessage(), 
					"", 
					ICPOReceiptLine.Paramlid + "=" + entry.getsID()
    				+ "&" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsreceiptheaderid()
					);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//in the query string instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + " was successfully updated.",
					entry.getQueryString()
				);
			}
			return;
	    }
		return;
	}
	private void processSave(
			ICPOReceiptLine line, 
			String sDBID, 
			String sUser,
			String sUserID,
			String sUserFullName,
			String sReceiptLastUpdatedTime,
			String sReceiptLstUpdatedFunction,
			String sReceiptLastUpdatedByFullName) throws Exception{

    	ICOption options = new ICOption();
    	try {
    		options.checkAndUpdatePostingFlagWithoutConnection(getServletContext(), 
				sDBID, 
				clsServletUtilities.getFullClassName(this.toString() + ".processSave"), 
				sUserFullName, 
				"EDIT RECEIPT LINE");
    		} catch (Exception e1) {
			throw new Exception("Error checking options flag - " + e1.getMessage() + ".");
		}
    	try{
    		editReceiptLine(line,sDBID,sUser,sUserID,sUserFullName,sReceiptLastUpdatedTime,sReceiptLstUpdatedFunction,sReceiptLastUpdatedByFullName);
    	} catch (Exception e1){
    		options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
    		throw new Exception("Error editing receipt line - "+e1.getMessage()+".");
    	}
    	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
    	
	}
	public void editReceiptLine(
			ICPOReceiptLine line, 
			String sDBID, 
			String sUser,
			String sUserID,
			String sUserFullName,
			String sReceiptLastUpdatedTime,
			String sReceiptLstUpdatedFunction,
			String sReceiptLastUpdatedByFullName) throws Exception{
		//Can't save if somehow the receipt has already been posted to inventory:
    	ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
    	rcpt.setsID(line.getsreceiptheaderid());
    	if (!rcpt.load(getServletContext(), sDBID, sUserID, sUserFullName)){
    		throw new Exception("Could not read receipt ID " + line.getsreceiptheaderid() + " - " + rcpt.getErrorMessages() + ".");
    	}
    	if (rcpt.getspostedtoic().compareToIgnoreCase("1") == 0){
    		throw new Exception("Receipt ID " + line.getsreceiptheaderid() + " has already been posted - cannot save this line.");
    	}
    	if (rcpt.getsdattimelastupdated().compareToIgnoreCase(sReceiptLastUpdatedTime) != 0){
			throw new Exception("Error [1488389089] - This receipt has been updated (function: '" + rcpt.getslastupdatedprocess() + ")"
				+ " by user '" + rcpt.getslastupdatedbyfullname() + "'"
				+ " on " + rcpt.getsdattimelastupdated()
				+ " since you started editing it (the previous version, which you had on your screen, had been saved " + sReceiptLastUpdatedTime
				+ " by user '" + sReceiptLastUpdatedByFullName + "'"
				+ "), so"
				+ " it cannot be saved.  Refresh the receipt before you try to edit it.'"
			);
    	}

    	if(!line.save_without_data_transaction(
				getServletContext(), 
				sDBID, 
				sUser,
				sUserID,
				sUserFullName,
				false)){
			throw new Exception("Could not save: " + line.getErrorMessages() + ".");
		}
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}