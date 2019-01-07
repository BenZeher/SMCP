package smbk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablebkaccountentries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class BKEditStatementAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.BKEditStatements)){return;}
		BKBankStatement statement;
		statement = new BKBankStatement();
		try {
			statement.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(BKBankStatement.ObjectName, statement);
			smaction.redirectAction(
					"Error reading request information: " + e2.getMessage(), 
					"", 
					""
			);
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " loadFromHTTPRequest failed");
    		}
			return;
		}
		
		smaction.getCurrentSession().removeAttribute(BKBankStatement.ObjectName);
		
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.COMMAND_FLAG, request);
	    //System.out.println("[1404231445] sCommandValue = '" + sCommandValue + "'");
    	//If it's a request to save the statement:
    	if (sCommandValue.compareToIgnoreCase(
    			BKEditStatementEdit.SAVECOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into save");
    		}
    		
    		try {
    			statement.save(getServletContext(), smaction.getsDBID(), smaction.getUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(BKBankStatement.ObjectName, statement);
				smaction.redirectAction(
						"Could not save: " + e.getMessage(), 
						"", 
						BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				);
	    		if (bDebugMode){
	    			System.out.println("In " + this.toString() + " save failed");
	    		}
				return;
			}
			smaction.getCurrentSession().removeAttribute(BKBankStatement.ObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					BKBankStatement.ObjectName + " was successfully saved.",
					BKBankStatement.Paramlid + "=" + statement.get_lid()
				);
			}
    	}
    	
    	//If it's a request to post:
    	if (sCommandValue.compareToIgnoreCase(
    			BKEditStatementEdit.POSTCOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into post");
    		}
    		try {
				statement.post_without_data_transaction(getServletContext(), smaction.getsDBID(), smaction.getUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(BKBankStatement.ObjectName, statement);
				smaction.redirectAction(
						"Could not post: " + e.getMessage(), 
						"", 
						BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)

				);
	    		if (bDebugMode){
	    			System.out.println("In " + this.toString() + " post failed");
	    		}
				return;
			}
			smaction.getCurrentSession().removeAttribute(BKBankStatement.ObjectName);
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					BKBankStatement.ObjectName + " was successfully posted.",
					BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				);
			}
    	}
    	
    	//If it's a request to add an entry:
    	if (sCommandValue.compareToIgnoreCase(
    			BKEditStatementEdit.ADDENTRY_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into add entry");
    		}
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smbk.BKAddEntryEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTablebkaccountentries.lstatementid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlid, request)
			+ "&" + BKBankStatement.Paramlbankid + "=" + clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, request)
			+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
				+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	//If it's a request to delete:
    	if (sCommandValue.compareToIgnoreCase(
    			BKEditStatementEdit.DELETECOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into delete");
    		}
    		try {
    			statement.delete(getServletContext(), smaction.getsDBID(), smaction.getUserName(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(BKBankStatement.ObjectName, statement);
				smaction.redirectAction(
						"Could not delete: " + e.getMessage(), 
						"", 
						BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				);
	    		if (bDebugMode){
	    			System.out.println("In " + this.toString() + " save failed");
	    		}
				return;
			}
			smaction.getCurrentSession().removeAttribute(BKBankStatement.ObjectName);
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKEditStatementSelect"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + "smbk.BKEditStatementSelect"
			;
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				smaction.getPwOut().println("<B>Statement was successfully deleted.</B>");
			}
			
			return;
    	}
    	//If it's a request to delete an entry:
    	if (sCommandValue.contains(BKEditStatementEdit.DELETEENTRYCOMMAND_VALUE)){
    		if (bDebugMode){
    			System.out.println("In " + this.toString() + " into delete entry");
    		}
    		String sLid = sCommandValue.substring(BKEditStatementEdit.DELETEENTRYCOMMAND_VALUE.length(), sCommandValue.length());
    		//System.out.println("[1404231444] sLid = '" + sLid + "'");
    		try {
    			delete_entry(sLid, smaction);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(BKBankStatement.ObjectName, statement);
				smaction.redirectAction(
						"Could not delete entry: " + e.getMessage(), 
						"", 
						BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				);
	    		if (bDebugMode){
	    			System.out.println("In " + this.toString() + " save failed");
	    		}
				return;
			}
			smaction.redirectAction(
					"", 
					"Entry was successfully deleted.",
					BKBankStatement.Paramlid + "=" + statement.get_lid()
						+ "&" + BKEditStatementEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(BKEditStatementEdit.RECORDWASCHANGED_FLAG, request)
						+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "="
							+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, request)
				);
			return;
    	}
    	return;
	}
	private void delete_entry(String sLid, SMMasterEditAction sm)throws Exception {
		String SQL = "DELETE FROM " + SMTablebkaccountentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkaccountentries.lid + "=" + sLid + ")"
			+ ")"
		;
		try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".delete_entry - user: " + sm.getUserName());
		} catch (Exception e) {
			throw new Exception("Error deleting entry  - " + e.getMessage());
		}
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
