package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTablebkbanks;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smbk.BKBank;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APPrintChecksAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final int NUMBER_OF_ALIGNMENT_CHECKS_TO_PRINT = 2;
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APPrintChecks
		)
		){
			return;
		}

		String sBatchNumber = clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.lbatchnumber, request);
		String sCheckFormID = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.CHECK_FORM_ID, request);
		String sBankID = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BANK_ID, request);
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1, request)){return;}

		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
				+ "&" + APPrintChecksEdit.CHECK_FORM_ID + "=" + sCheckFormID
				+ "&CallingClass=" + "smap.APEditBatchesEdit"
			;
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), smaction.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e1) {
			sRedirectString += 
				"&" + "Warning" + "=" + "Error [1504206295] getting data connection - " + e1.getMessage()
			;
			redirectProcess(sRedirectString, response);
			return;
		}

		out.println("<HTML><BODY>");
		
		if (clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BUTTON_PRINT, request).compareToIgnoreCase("") != 0){
			String sListOfEntryIDsInCheckRun = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.LIST_OF_ENTRYIDS_IN_CHECK_RUN, request);
			try {
				printChecks(sBatchNumber, sListOfEntryIDsInCheckRun, sCheckFormID, conn, out, smaction, request);
			} catch (Exception e) {
				sRedirectString += 
					"&" + "Warning" + "=" + "Error [1504206296] displaying printed checks - " + e.getMessage()
				;
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			out.println("</BODY></HTML>");
			return;
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BUTTON_ALIGN, request).compareToIgnoreCase("") != 0){
			try {
				out.println(displaySampleAlignment(sBatchNumber, sCheckFormID, conn, out, smaction));
			} catch (Exception e) {
				sRedirectString += 
					"&" + "Warning" + "=" + "Error [1504206296] displaying printed checks - " + e.getMessage()
				;
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			out.println("</BODY></HTML>");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return;
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BUTTON_RESET_SELECTED_CHECKS, request).compareToIgnoreCase("") != 0){
			try {
				resetSelectedChecks(request, conn, smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				sRedirectString += 
					"&" + "Warning" + "=" + "Error [1504206396] reprinting selected checks - " + e.getMessage()
				;
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			sRedirectString += "Status=" + "Checks were reset successfully.";
			redirectProcess(sRedirectString, response);
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BUTTON_RESET_CHECK_RANGE, request).compareToIgnoreCase("") != 0){
			
			String sStatus = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.PARAM_RESET_CHECK_RANGE_STATUS, request);
			String sStartingCheckNumber = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.RESET_RANGE_STARTING_CHECK_NUMBER, request);
			String sEndingCheckNumber = clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.RESET_RANGE_ENDING_CHECK_NUMBER, request);
			sRedirectString += 
				"&" + APPrintChecksEdit.RESET_RANGE_STARTING_CHECK_NUMBER + "=" + sStartingCheckNumber
				+ "&" + APPrintChecksEdit.RESET_RANGE_ENDING_CHECK_NUMBER + "=" + sEndingCheckNumber
			;
			try {
				resetRangeOfChecks(sBatchNumber, sStartingCheckNumber, sEndingCheckNumber, sStatus, conn, smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				sRedirectString += 
					"&" + "Warning" + "=" + "Error [1504206496] reprinting range of checks - " + e.getMessage()
				;
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			sRedirectString += "&" + "Status=" + "Range of checks was reset successfully.";
			redirectProcess(sRedirectString, response);
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(APPrintChecksEdit.BUTTON_SET_NEXT_CHECK_NUMBER, request).compareToIgnoreCase("") != 0){
			if (request.getParameter(APPrintChecksEdit.CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER) == null){
				sRedirectString += 
				"&" + "Warning" + "=" + "You chose to update the next check number, but did not click the 'Confirm' check box.";
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			String sNextCheckNumber = clsManageRequestParameters.get_Request_Parameter(SMTablebkbanks.lnextchecknumber, request);
			try {
				updateNextCheckNumber(conn, sNextCheckNumber, sBankID, smaction.getUserID());
			} catch (Exception e) {
				sRedirectString += 
					"&" + "Warning" + "=" + "Error [1504206497] setting the next check number - " + e.getMessage()
				;
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				redirectProcess(sRedirectString, response);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			sRedirectString += "&" + "Status=" + "Next check number was updated successfully.";
			redirectProcess(sRedirectString, response);
		}
		
		return;
	}

	private void updateNextCheckNumber(Connection conn, String sNextCheckNumber, String sBankID, String sUserID) throws Exception{
		
		//Confirm that the next number requested is valid:
		long lCurrentHighestCheckNumberUsed = 0;
		String SQL = "SELECT"
			+ " MAX(CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED)) AS HIGHESTCHECKNUMBER"
			+ " FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				 + "(" + SMTableapchecks.iprinted + " = 1)"
				 //+ " AND (" + SMTableapchecks.ivoid + " = 0)"  //Don't want to re-use a 'voided' check number, either
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				lCurrentHighestCheckNumberUsed = rs.getLong("HIGHESTCHECKNUMBER");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1504649724] reading highest current check number with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		long lRequestedCheckNumber = 0L;
		try {
			lRequestedCheckNumber = Long.parseLong(sNextCheckNumber);
		} catch (Exception e) {
			throw new Exception("Error [1504649725] requested check number (" + sNextCheckNumber + ") is not valid.");
		}
		
		if (lRequestedCheckNumber < 1){
			throw new Exception("Error [1504659798] - check number cannot be less than 1.");
		}
		
		if (lCurrentHighestCheckNumberUsed > lRequestedCheckNumber){
			throw new Exception("Error [1504659799] - there are already checks with numbers higher than the number you requested (" + sNextCheckNumber + ").");
		}
		
		BKBank bank = new BKBank();
		bank.setslid(sBankID);
		try {
			bank.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1504649726] loading bank with IS '" + sBankID + "' - " + e.getMessage());
		}
		
		bank.setsnextchecknumber(sNextCheckNumber);
		try {
			bank.save_without_data_transaction(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1504649727] setting next check number - " + e.getMessage());
		}
		
		return;
		
	}
	private void resetSelectedChecks(
		HttpServletRequest req,
		Connection conn,
		String sUserID,
		String sUserFullName
		) throws Exception{
		
    	Enumeration <String> eParams = req.getParameterNames();
    	String sParameter = "";
    	String sCheckNumber = "";
    	String sStatus = "";
    	while (eParams.hasMoreElements()){
    		sParameter = eParams.nextElement();
    		if (sParameter.startsWith(APPrintChecksEdit.CHECK_SELECTED_PREFIX)){
    			sCheckNumber = sParameter.substring(APPrintChecksEdit.CHECK_SELECTED_PREFIX.length(), sParameter.length());
    			sStatus = clsManageRequestParameters.get_Request_Parameter(sParameter, req).trim();
    			//System.out.println("[1504640545] sParameter = '" + sParameter + "', sCheckNumber = '" + sCheckNumber + "', sStatus = '" + sStatus + "'");
    			
    			//Update the check status:
    			try {
					APCheck.updateCheckPrintedStatus(conn, sCheckNumber, sCheckNumber, sStatus, sUserID, sUserFullName);
				} catch (Exception e) {
					throw new Exception(e.getMessage());
				}
    		}
    	}
    	return;
	}
	
	private void resetRangeOfChecks(
			String sBatchNumber,
			String sStartingCheckNumber,
			String sEndingCheckNumber,
			String sStatus,
			Connection conn,
			String sUserID,
			String sUserFullName
			) throws Exception{
			
		//First make sure that the range is valid:
		long lStartingCheckNumber = 0L;
		try {
			lStartingCheckNumber = Long.parseLong(sStartingCheckNumber.trim());
		} catch (Exception e) {
			throw new Exception("Error [1504641090] - starting check number '" + sStartingCheckNumber + "' is invalid.");
		}
		
		long lEndingCheckNumber = 0L;
		try {
			lEndingCheckNumber = Long.parseLong(sEndingCheckNumber.trim());
		} catch (Exception e) {
			throw new Exception("Error [1504641091] - ending check number '" + sEndingCheckNumber + "' is invalid.");
		}
		
		//If the numbers are not in the right order:
		if (lEndingCheckNumber < lStartingCheckNumber){
			throw new Exception("Error [1504641092] - starting check number (" + sStartingCheckNumber + ") is higher than ending check number (" + sEndingCheckNumber + ").");
		}

		//Verify all the checks in the range first:
		for (long lCheckNumber = lStartingCheckNumber; lCheckNumber <= lEndingCheckNumber; lCheckNumber++){
			verifyCheckForResetting(conn, Long.toString(lCheckNumber), sBatchNumber);
		}
		
		//Now go on to try to update the check statuses:
		try {
			APCheck.updateCheckPrintedStatus (conn, sStartingCheckNumber, sEndingCheckNumber, sStatus, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return;
	}
	private void verifyCheckForResetting (Connection conn, String sCheckNumber, String sBatchNumber) throws Exception{
		
		String sError = "";
		long lBatchNumber;
		try {
			lBatchNumber = Long.parseLong(sBatchNumber.trim());
		} catch (Exception e) {
			throw new Exception("Error [1504641093] - batch number (" + sBatchNumber + ") is invalid.");
		}
		
		//Now check to make sure that:
		//  1) The checks in the range all belong to this batch number
		//  2) The checks in the range are all unposted
		String SQL = "SELECT"
			+ " " + SMTableapchecks.iposted
			+ ", " + SMTableapchecks.lbatchnumber
			+ " FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecks.schecknumber + " = '" + sCheckNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getInt(SMTableapchecks.iposted) == 1){
					sError += "  Some of those checks as already posted.";
				}
				if (rs.getLong(SMTableapchecks.lbatchnumber) != lBatchNumber){
					sError += "  Some of those checks are from different batches.";
				}
			}else{
				sError += " Some of those check numbers were not found.";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1504645633] reading check to verify - " + e.getMessage());
		}
		
		if (sError.compareToIgnoreCase("") != 0){
			throw new Exception("Error(s) [1504645632] validating check range to reset -" + sError);
		}
		return;
			
	}
	private void printChecks(
			String sBatchNumber,
			String sListOfEntryIDsInCheckRun,
			String sCheckFormID,
			Connection conn,
			PrintWriter pwOut,
			SMMasterEditAction smaction,
			HttpServletRequest req) throws Exception{
		
		APCheckFormProcessor processor = new APCheckFormProcessor();
		
		//Print a link back to the 'print checks' screen for the user:
		String sReturnForm =
			"<FORM" + "\n"
			+ " NAME=\"" + "MAIN" + "\"" + "\n"
			+ " ACTION=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APConfirmCheckPrintAction\"" + "\n"
			+ " METHOD=\"" + "POST" + "\"" + "\n"
			+ ">\n"
			+ " <DIV style=\"background-color:lightblue;\" >"  + "\n"
			
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\""
			+ " VALUE=\"" + smaction.getsDBID() + "\""
			+ ">\n"
			
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTableapbatches.lbatchnumber + "\""
			+ " VALUE=\"" + sBatchNumber + "\""
			+ ">\n"
			
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + APConfirmCheckPrintAction.COMMADELIMITED_LIST_OF_FINALIZED_CHECK_BATCHENTRYIDS + "\""
			+ " VALUE=\"" + sListOfEntryIDsInCheckRun + "\""
			+ ">\n"
		;
			
			//We only need the entry, entry ID, and entry type if we are printing a single payment entry:
			//(If the list of entry IDs contains more than one, it will also contain commas.)
			if (!sListOfEntryIDsInCheckRun.contains(",")){
				sReturnForm += "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + SMTableapbatchentries.lentrynumber + "\""
					+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lentrynumber, req) + "\""
					+ ">\n"
					
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + SMTableapbatchentries.lid + "\""
					+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lid, req) + "\""
					+ ">\n"
					
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + SMTableapbatchentries.ientrytype + "\""
					+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.ientrytype, req) + "\""
					+ ">\n"
				;
			}
		    
			sReturnForm += 
			"\n\n" + "<TABLE WIDTH=100% BORDER=1>\n"
			+ "  <TR>\n"
			+ "    <TD ALIGN=CENTER>\n"
			+ "<B><I>Did all the checks print successfully?</I></B><BR>\n"
			+ "(Once you confirm that all the checks printed successfully, they will be set to <B><I><FONT COLOR=RED>FINALIZED</FONT></I></B>,"
				+ " meaning they CANNOT be re-printed, and the payment can no longer be edited.)<BR>\n"
			+ createNoButton()+ "&nbsp;&nbsp;" + createYesButton() + "\n"
			
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME= \"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_VALUE + "\""
			+ " ID= \"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_VALUE + "\""
			+ " VALUE= \"" + APConfirmCheckPrintAction.BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY_LABEL + "\""
			+ ">" + "\n"
			
			+ "</TD\n"
			+ "  </TR>" + "\n"
			+ "</TABLE>" + "\n"
			+ "</DIV>" + "\n"
			+ "</FORM>" + "\n"
		;
		
		//Javascript to confirm choice:
		pwOut.println(
			"  <script type=\"text/javascript\">\n"
			+ "\n"
			
			+ "    function processSuccessfulPrint(){\n"
			+ "        if (confirm('Are you sure the checks all printed correctly and you are ready to FINALIZE them?')){\n"
			+ "            document.getElementById(\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_VALUE + "\").value = \"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_SUCCESSFULLY_LABEL + "\";\n"
			+ "            document.forms[\"MAIN\"].submit();\n"
			+ "        }else{\n"
			+ "            return;\n"
			+ "        }\n"
			+ "    }\n"
			
			+ "    function processUNSuccessfulPrint(){\n"
			+ "        document.getElementById(\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_VALUE + "\").value = \"" + APConfirmCheckPrintAction.BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY_LABEL + "\";\n"
			+ "        document.forms[\"MAIN\"].submit();\n"
			+ "    }\n"
			
			+ "  </script>"
		);
		
		pwOut.println( 
			"<style type=\"text/css\">\n"
			+ "    @media print  { .noprint  { display: none; } }\n"
			+ "</style>\n"
			+ "<div class=\"noprint\">\n"
			+ sReturnForm
			+ "\n"
			+ "</div>\n"
		);
		
		APBatch batch = new APBatch(sBatchNumber);
		try {
			batch.loadBatch(conn);
		} catch (Exception e1) {
			pwOut.println("<BR><B><FONT COLR=RED>Error [1543332192] in loading AP batch - " + e1.getMessage() + "</FONT></B><BR>");
		}
		
		try {
			pwOut.println(processor.printCheckRun(batch, conn, sListOfEntryIDsInCheckRun, sCheckFormID, smaction.getUserID(), smaction.getFullUserName()));
		} catch (Exception e) {
			pwOut.println("<BR><B><FONT COLOR=RED>Error [1543332092] in printCheckRun - " + e.getMessage() + "</FONT></B><BR>");
		}
		return;

	}
	private String createNoButton(){
		return  "<button type=\"button\""
			+" value=\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY_LABEL + "\""
			+ "name=\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY + "\""
			+ " onClick=\"processUNSuccessfulPrint();\">"
			+  APConfirmCheckPrintAction.BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY_LABEL
			+ "</button>\n";
	}
	private String createYesButton(){
		return  "<button type=\"button\""
			+" value=\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_SUCCESSFULLY_LABEL + "\""
			+ "name=\"" + APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_SUCCESSFULLY + "\""
			+ " onClick=\"processSuccessfulPrint();\">"
			+  APConfirmCheckPrintAction.BUTTON_CHECKS_PRINTED_SUCCESSFULLY_LABEL
			+ "</button>\n";
	}
	
	private String displaySampleAlignment(
			String sBatchNumber,
			String sCheckFormID,
			Connection conn,
			PrintWriter pwOut,
			SMMasterEditAction smaction) throws Exception{
		
		String s = "";
		
		APBatch batch = new APBatch(sBatchNumber);
		try {
			batch.loadBatch(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1504284903] loading batch number " + sBatchNumber + " - " + e1.getMessage());
		}
		
		int iMaxNumberOfLinesToPrintPerPage = 0;
		String SQL = "SELECT"
			+ " * FROM " + SMTableapcheckforms.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecks.lid + " = " + sCheckFormID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				iMaxNumberOfLinesToPrintPerPage = rs.getInt(SMTableapcheckforms.inumberofadvicelinesperpage);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1505507191] - no check form found with ID '" + sCheckFormID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1505507192] - could not read check forms with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		for (int i = 1; i <= NUMBER_OF_ALIGNMENT_CHECKS_TO_PRINT; i++){
			APCheck check = new APCheck();
			check.populateSampleCheckFromPaymentEntry(conn, batch.getBatchEntryArray().get(0), sCheckFormID, smaction.getUserID(), 1, iMaxNumberOfLinesToPrintPerPage);
			APCheckFormProcessor processor = new APCheckFormProcessor();
			if (i > 1){
				s += APCheckFormProcessor.getPageBreak();
			}
			s += processor.printIndividualCheck(
				conn,
				check,
				smaction.getUserID()
			);
		}
		return s;
	}

	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		//System.out.println("[1504211050] sRedirectString = '" + sRedirectString + "'");
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395238124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395238125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
