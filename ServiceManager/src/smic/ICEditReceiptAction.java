package smic;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditReceiptAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		//boolean bDebugMode = false;
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    ICOption options = new ICOption();
		
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditReceipts)){return;}
	    //Read the entry fields from the request object:
		ICPOReceiptHeader entry = new ICPOReceiptHeader(request);
		
		String sWarning = "";
		String sStatus = "";
		
		//**********************************************************
		//Special cases:
		//Get a list of parameters so we can see if the user chose to receive one entire line:
    	Enumeration <String> e = request.getParameterNames();
    	String sParam = "";
    	
    	SMLogEntry log = new SMLogEntry(smaction.getsDBID(), getServletContext());
    	String sTimeStamp = Long.toString(System.currentTimeMillis());
    	
    	while (e.hasMoreElements()){
    		sParam = e.nextElement();
    		if (sParam.contains(ICEditReceiptEdit.SUBMIT_BUTTON_RECEIVE_OUTSTANDING)){
    	    	log.writeEntry(
    	        		sUserID, 
    	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
    	        		"Receiving remaining qty on PO line, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
    	        		"", 
    	        		"[1531259736]"
    	        	);
	    		try{
					options.checkAndUpdatePostingFlagWithoutConnection(
							getServletContext(), 
							sDBID, 
							clsServletUtilities.getFullClassName(this.toString()) + ".doPost", 
							sUserFullName,
							"RECEIVE REMAINING QTY ON PO LINE");
				} catch (Exception e4) {
	    	    	log.writeEntry(
	    	        		sUserID, 
	    	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	    	        		"Receiving remaining qty on PO line, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	    	        		"Error checking posting flag - " + e4.getMessage(), 
	    	        		"[1531259737]"
	    	        	);
	    	    	smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
					smaction.redirectAction(
							"ERROR checking and updating posting flag " + e4.getMessage(), 
							"",
							ICPOHeader.Paramlid + "=" + entry.getsID()
					);
					return;
				}
    			//if (bDebugMode){
    			//	System.out.println("In " + this.toString() + ".doPost - sParam = " + sParam);
    			//}
    			String sPOLineID = sParam.substring(
    				ICEditReceiptEdit.SUBMIT_BUTTON_RECEIVE_OUTSTANDING.length(), sParam.length());
    			//if (bDebugMode){
    			//	System.out.println("In " + this.toString() + ".doPost - sPOLineID = " + sPOLineID);
    			//}
    			
    			sWarning = "";
    			sStatus = "PO line was successfully received.";
    			
    			try {
					entry.checkToReceiveQtyRemainingOnPOLine(
						sPOLineID, 
						getServletContext(), 
						smaction.getsDBID(), 
						smaction.getFullUserName(),
						smaction.getUserID()
					);
				} catch (Exception e3) {
	    	    	log.writeEntry(
	    	        		sUserID, 
	    	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	    	        		"Receiving remaining qty on PO line, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	    	        		"Error receiving: " + e3.getMessage(), 
	    	        		"[1531259738]"
	    	        	);
					
    				//Put the entry back into the session object so the calling class can read the unsaved version of the data:
					smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
    				sWarning = "Could not receive POLine - " + e3.getMessage();
    				sStatus = "";
				}
    			//Now send a response back to the browser:
    	    	log.writeEntry(
    	        		sUserID, 
    	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
    	        		"Receiving remaining qty on PO line, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp,
    	        		"Function returned successfully", 
    	        		"[1531259739]"
    	        	);
    	    	try {
					options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
				}
				smaction.redirectAction(
						sWarning, 
						sStatus,
						ICPOHeader.Paramlid + "=" + entry.getsID()
				);
				return;
    		}
    	}
	    if(request.getParameter(ICEditReceiptEdit.SUBMIT_BUTTON_RECEIVE_ALL) != null){
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	        		"Receiving all lines on PO, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	        		"", 
	        		"[1531259836]"
	        	);
	    	try {
				options.checkAndUpdatePostingFlagWithoutConnection(
						getServletContext(), 
						sDBID, 
						clsServletUtilities.getFullClassName(this.toString()) + ".doPost", 
						sUserFullName,
						"RECEIVE ALL LINES ON PO");
			} catch (Exception e4) {
		    	log.writeEntry(
		        		sUserID, 
		        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
		        		"Receiving all lines on PO, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
		        		"Error checking posting flag: " + e4.getMessage(), 
		        		"[1531259837]"
		        	);
		    	smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
				smaction.redirectAction(
						"ERROR checking and updating posting flag "+e4.getMessage(), 
						"",
						ICPOHeader.Paramlid + "=" + entry.getsID()
				);
				return;
			}
	    	try {
	    		entry.checkToReceiveAllOutstandingLines(smaction.getsDBID(), 
	    				smaction.getUserID(),
	    				smaction.getFullUserName(),
	    				getServletContext()
	    		);
			} catch (Exception e2) {
    	    	log.writeEntry(
    	        		sUserID, 
    	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
    	        		"Receiving all lines on PO, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp,
    	        		"Error receiving: " + e2.getMessage(), 
    	        		"[1531259838]"
    	        	);
				smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
				sWarning = "Could not receive all - " + e2.getMessage();
				sStatus = "";
				
			}
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				sWarning = "";
				sStatus = "All outstanding items were received successfully";
			}
			try {
				options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
			} catch (Exception e1) {
			}
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	        		"Receiving all lines on PO, rcpt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	        		"Function returned successfully", 
	        		"[1531259839]"
	        	);
			smaction.redirectAction(
					sWarning, 
					sStatus,
					ICPOHeader.Paramlid + "=" + entry.getsID()
				);
			return;
	    }
	    //End of special cases
		//********************************
	    
	    //********************************
	    //If it's a 'delete':
	    if(smaction.isDeleteRequested()){
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	        		"Deleting receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	        		"", 
	        		"[1531259930]"
	        	);
		    try {
				options.checkAndUpdatePostingFlagWithoutConnection(
						getServletContext(), 
						sDBID, 
						clsServletUtilities.getFullClassName(this.toString() + ".doPost"), 
						sUserFullName, 
						"EDIT RECEIPT ACTION"
				);
			} catch (Exception e2) {
		    	log.writeEntry(
		        		sUserID, 
		        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
		        		"Deleting receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
		        		"Error checking posting flag: " + e2.getMessage(), 
		        		"[1531259931]"
		        	);
				smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
				sWarning = "ERROR checking and updating posting flag "+e2.getMessage();
				sStatus = "";
				smaction.redirectAction(
						sWarning, 
						sStatus,
						ICPOHeader.Paramlid + "=" + entry.getsID()
				);
				return;
			}
		    try {
				deleteReceipt(entry,smaction);
			} catch (Exception e3) {
		    	log.writeEntry(
		        		sUserID, 
		        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
		        		"Deleting receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
		        		"Error deleting receipt: " + e3.getMessage(), 
		        		"[1531259932]"
		        	);
			    try{
			    	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
			    }catch(Exception e1){
			    }
			    sWarning = e3.getMessage();
			    sStatus = "";
				smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
				smaction.redirectAction(
					sWarning,
					sStatus, 
					ICPOHeader.Paramlid + "=" + entry.getsID()
				);
				return;
			}
		    try{
		    	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		    }catch(Exception e1){
		    }
		    //Delete was successful:
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	        		"Deleting receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	        		"Receipt deleted successfully", 
	        		"[1531259933]"
	        	);
		    smaction.redirectAction(
		    	"", 
		    	"Receipt was deleted successfully", 
		    	ICPOHeader.Paramlid + "=" + entry.getsID()
		    );
		    return;
	    }
	    //********************************
	    
	    //********************************
	    //If it's just an update:
    	log.writeEntry(
        		sUserID, 
        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
        		"Updating receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
        		"", 
        		"[1531259130]"
        	);
	    if(smaction.isEditRequested()){
		    sWarning = "";
		    sStatus = "Receipt was saved successfully.";
		    try {
	 			options.checkAndUpdatePostingFlagWithoutConnection(
	 					getServletContext(), 
	 					sDBID, 
	 					clsServletUtilities.getFullClassName(this.toString() + ".doPost"), 
	 					sUserFullName, 
	 					"UPDATING RECEIPT"
	 			);
		 		}catch (Exception e2) {
		 	    	log.writeEntry(
		 	        		sUserID, 
		 	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
		 	        		"Updating receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
		 	        		"Error checking posting flag: " + e2.getMessage(), 
		 	        		"[1531259131]"
		 	        	);
		 			smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
		 			sWarning = "ERROR checking and updating posting flag "+e2.getMessage();
		 			sStatus = "";
		 			smaction.redirectAction(
							sWarning, 
							sStatus,
							ICPOHeader.Paramlid + "=" + entry.getsID()
					);
					return;
		 		}
		    
		    try {
				updateReceipt(entry,smaction);
			} catch (Exception e2) {
	 	    	log.writeEntry(
	 	        		sUserID, 
	 	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
	 	        		"Updating receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
	 	        		"Error updating receipt: " + e2.getMessage(), 
	 	        		"[1531259132]"
	 	        	);
				smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
				sWarning = e2.getMessage();
				sStatus = "";
			}
		    
		    try{
		    	options.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		    }catch(Exception e1){
		    	
		    }
 	    	log.writeEntry(
 	        		sUserID, 
 	        		SMLogEntry.LOG_OPERATION_ICRECEIVINGPO, 
 	        		"Updating receipt #" + entry.getsID() + ", TimeStamp: " + sTimeStamp, 
 	        		"Returning to calling class ", 
 	        		"[1531259133]"
 	        	);
		    smaction.redirectAction(sWarning, sStatus, ICPOHeader.Paramlid + "=" + entry.getsID());
	    }
	}
	
	private void deleteReceipt(ICPOReceiptHeader entry, SMMasterEditAction smaction) throws Exception{
	    if (!smaction.isDeleteConfirmed()){
	    	throw new Exception("You must click CONFIRM to delete a receipt.");
	    }
    	//Save this now so it's not lost after the delete:
	    if (!entry.delete(getServletContext(), 
	    		smaction.getsDBID(), 
	    		smaction.getUserID(),
	    		smaction.getFullUserName()
	    		)){
	    	throw new Exception("Error [1531261796] - could not delete receipt - " + entry.getErrorMessages());
	    }
	}
	private void updateReceipt(ICPOReceiptHeader entry, SMMasterEditAction smaction) throws Exception{
	    
		//If it's an edit, process then:
		if(!entry.save_without_data_transaction(
				getServletContext(), 
				smaction.getsDBID(), 
				smaction.getUserName(),
				smaction.getUserID(),
				smaction.getFullUserName())){
			smaction.getCurrentSession().setAttribute(ICPOReceiptHeader.ParamObjectName, entry);
			throw new Exception("Error [1531261282] saving receipt - " + entry.getErrorMessages());
		}
	    return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}