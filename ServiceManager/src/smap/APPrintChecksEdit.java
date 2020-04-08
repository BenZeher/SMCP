package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableapchecklines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTablebkbanks;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smbk.BKBank;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APPrintChecksEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static String BUTTON_PRINT = "PRINT";
	public static String BUTTON_PRINT_LABEL = "Print checks";
	public static String BUTTON_ALIGN = "ALIGN";
	public static String BUTTON_ALIGN_LABEL = "Align checks";
	public static String BUTTON_RESET_SELECTED_CHECKS = "RESETSTATUS";
	public static String BUTTON_RESET_STATUS_LABEL = "Reset check status individually";
	public static String BUTTON_SET_NEXT_CHECK_NUMBER = "SETNEXTCHECKNUMBER";
	public static String BUTTON_SET_NEXT_CHECK_NUMBER_LABEL = "Update next check number";
	public static String CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER = "CONFIRMNEXTCHECKNUMBER";
	public static String CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER_LABEL = "Confirm";
	
	
	public static String BUTTON_RESET_CHECK_RANGE = "SELECTREPRINTTANGE";
	public static String BUTTON_RESET_CHECK_RANGE_LABEL = "Reset check status for range";
	public static String PARAM_RESET_CHECK_RANGE_STATUS = "PARAMCHECKRESETSTATUS";
	public static String RESET_RANGE_STARTING_CHECK_NUMBER = "REPRINTTANGESTARTINGCHECKNUMBER";
	public static String RESET_RANGE_ENDING_CHECK_NUMBER = "REPRINTTANGEENDINGCHECKNUMBER";
	
	public static String CHECK_FORM_ID = "CHECKFORMID";
	public static String CHECK_SELECTED_PREFIX = "CHECKSELECTEDPREFIX";
	public static String VOID_FLAG = "(VOID)";
	public static String BANK_ID = "BANKID";
	
	//This flag, if set, tells us to clear ANY unfinalized checks for the entries so that we can start over.
	//We would normally do this whenever the user is coming to print the checks, but AFTER trying to print the checks, if the
	//user says they did NOT print successfully, we would NOT want to clear the checks in order to give him a chance to reprint those same checks.
	//So in that case - where the user tried to print but indicates they did NOT print successfully - we WOULD NOT want this flag set.
	public static String CLEAR_UNFINALIZED_CHECKS = "CLEARUNFINALIZEDCHECKS";
	
	public static String LIST_OF_ENTRYIDS_IN_CHECK_RUN = "LISTOFENTRYIDSINCHECKRUN";
	
	private static String sCalledClassName = "APPrintChecksAction";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APPrintChecks //add new rights if necessary
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sBatchNumber = clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.lbatchnumber, request);
	    
	    //If this class is being passed in an entry number, then we are letting the user print JUST the check(s) for that single entry:
	    String sEntryID = clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lid, request);
	    
	    String title = "Print Checks";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
	    		sCompanyName));

	    out.println(SMUtilities.getMasterStyleSheetLink());
	    
		if (clsManageRequestParameters.get_Request_Parameter("Warning", request).compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>WARNING: " + clsManageRequestParameters.get_Request_Parameter("Warning", request) + "</B></FONT><BR>");
		}
		if (clsManageRequestParameters.get_Request_Parameter("Status", request).compareToIgnoreCase("") != 0){
			out.println("<BR><B>NOTE: " + clsManageRequestParameters.get_Request_Parameter("Status", request) + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		
		//Print a link back to the batch:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit?"
				+ SMTableapbatches.lbatchnumber + "=" + sBatchNumber
				+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to AP Batch #" + sBatchNumber + " </A><BR>");
		
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APPrintChecks)
				+ "\">Summary</A><BR>");
	    
		//If we are printing a single entry, then on error we should redirect back to the 'APEditPaymentEdit' screen:
		String sRedirectString = "";
		if (sEntryID.compareToIgnoreCase("") == 0){
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
				+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)
			; 
		}else{
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&" + SMTableapbatchentries.lbatchnumber + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lbatchnumber, request) 
	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lentrynumber, request)
	    		+ "&" + SMTableapbatchentries.lid + "=" + sEntryID
	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.ientrytype, request)
	    		+ "&" + "Editable=Y"
	    		+ "&" + "CallingClass=" + "smap.APEditBatchesEdit"
			;
		}
		//System.out.println("[1510880552] - sEntryID = '" + sEntryID + "', sRedirectString = '" + sRedirectString + "'");
		APBatch batch = new APBatch(sBatchNumber);
	    try {
			batch.load(getServletContext(), sDBID, sUserName);
		} catch (Exception e) {
			redirectProcess(
				sRedirectString
					+ "&Warning=" + clsServletUtilities.URLEncode("Error [1504189906] - could not load batch number " + sBatchNumber + " - " + e.getMessage())
				, 
				response);
			return;
		}
	    
	    //Validate all the entries one last time before we go to print checks:
	    try {
			batch.validate_fields(sUserID, getServletContext(), sDBID, false);
		} catch (Exception e2) {
			CurrentSession.removeAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT);
			CurrentSession.setAttribute(APEditBatchesEdit.AP_BATCH_POSTING_SESSION_WARNING_OBJECT, e2.getMessage());
			sRedirectString = 
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "&" + SMTableapbatches.lbatchnumber + "=" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lbatchnumber, request) 
		    		+ "&" + "CallingClass=" + this.toString()
				;
			redirectProcess(
					sRedirectString
					+ ""
					, 
					response);
				return;
		}
	    
	    //If the checks have all been finalized for this batch, then we go back to the batch edit screen:
	    if (batch.bAllChecksHaveBeenFinalized()){
			redirectProcess(
				sRedirectString
				+ "&Status=" + clsServletUtilities.URLEncode("Checks were printed successfully and finalized.")
				, 
				response);
			return;
	    }
	    
	    //If we are printing a SINGLE payment, get that entry:
	    APBatchEntry objSingleBatchEntry = null;
	    if (sEntryID.compareToIgnoreCase("") != 0){
	    	objSingleBatchEntry = new APBatchEntry();
	    	objSingleBatchEntry.setslid(sEntryID);
	    	try {
				objSingleBatchEntry.load(getServletContext(), sDBID, sUserName);
			} catch (Exception e) {
				redirectProcess(
					sRedirectString
						+ "&Warning=" + clsServletUtilities.URLEncode("Error [1510771036] - could not load single entry to print check(s) " + " - " + e.getMessage())
					, 
					response);
				return;
			}
	    }
	    
	    //We don't want to print any checks for payments with a ZERO amount, so check that now:
	    try {
			checkForZeroAmtPayments(objSingleBatchEntry, batch);
		} catch (Exception e) {
			redirectProcess(
				sRedirectString
				+ "&Warning=" + clsServletUtilities.URLEncode("Error [1510771056] - checking for zero amount payments " + " - " + e.getMessage())
				, 
				response);
			return;
		}
	    
	    //If we are coming to this screen for the first time, not returning from an attempted print, then we'll want to remove any associated checks which have
	    //NOT been finalized:
	    if (clsManageRequestParameters.get_Request_Parameter(CLEAR_UNFINALIZED_CHECKS, request).compareToIgnoreCase("") != 0){
	    	try {
				deleteUnfinalizedChecks(sDBID, sUserName);
			} catch (Exception e) {
				redirectProcess(
					sRedirectString
					+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
					,
					response);
				return;
			}
	    }
	    
	    //Add drop down list of check forms, default to the one for the bank in the first entry:
	    //TODO - may need to double check this logic in the future, in case there's the potential that a payment batch includes different banks, and different check forms....!!!
	    BKBank bank = new BKBank();
	    
	    //IF we are printing just one payment entry, then get the bank for THAT payment entry:
	    if (objSingleBatchEntry != null){
	    	bank.setslid(objSingleBatchEntry.getslbankid());
	    //Otherwise, just pick off the first one in the batch that has a valid bank ID:
	    }else{
	    	for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
	    		if (batch.getBatchEntryArray().get(i).getslbankid().compareToIgnoreCase("0") != 0){
	    			bank.setslid(batch.getBatchEntryArray().get(0).getslbankid());
	    			break;
	    		}
	    	}
	    }
	    
	    try {
			bank.load(getServletContext(), sDBID, sUserID, sUserFullName);
		} catch (Exception e) {
			redirectProcess(
				sRedirectString
					+"&Warning=" + clsServletUtilities.URLEncode("Error [1504189907] - could not load bank with ID " + batch.getBatchEntryArray().get(0).getslbankid() + "' - " + e.getMessage())
				, 
				response);
			return;
		}
		
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smap." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.lbatchnumber + "\" VALUE=\"" + sBatchNumber + "\">\n");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lid + "\" VALUE=\"" + sEntryID + "\">\n");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lentrynumber + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.lentrynumber, request) + "\">\n");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.ientrytype + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(SMTableapbatchentries.ientrytype, request) + "\">\n");
	    
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + BANK_ID + "\" VALUE=\"" + bank.getslid() + "\">\n");
	    
	    String sOutPut = "";
	    
	    sOutPut += "<BR><TABLE>\n";
	    
	    sOutPut += "  <TR>\n"
		    	+ "    <TD ALIGN=RIGHT>Bank:</TD>\n"
		    	+ "    <TD ALIGN=LEFT>"
		    	+ bank.getsshortname() + " - " + bank.getsbankname()
		    	+ "    </TD>"
		    	+ "  </TR>"
		    ;
	    
	    sOutPut += "  <TR>\n"
	    	+ "    <TD ALIGN=RIGHT>Check form:</TD>\n"
	    	+ "    <TD ALIGN=LEFT>"
	    ;
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableapcheckforms.TableName
	        	+ " ORDER BY " + SMTableapcheckforms.sname
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        String sCheckFormID = clsManageRequestParameters.get_Request_Parameter(CHECK_FORM_ID, request);
	        if (sCheckFormID.compareToIgnoreCase("") == 0){
	        	sCheckFormID = bank.getscheckformid();
	        }
	     	sOutPut += "<SELECT NAME=\"" + CHECK_FORM_ID + "\">\n";
        	String sSelected = "";
        	while (rs.next()){
        		if (sCheckFormID.compareToIgnoreCase(Long.toString(rs.getLong(SMTableapcheckforms.lid))) == 0){
        			sSelected = " selected=yes";
        		}else{
        			sSelected = "";
        		}
        		sOutPut += "<OPTION"
        			+ sSelected
        			+ " VALUE=\"" + Long.toString(rs.getLong(SMTableapcheckforms.lid)) + "\">"
        			+ rs.getString(SMTableapcheckforms.sname) + " - " + rs.getString(SMTableapcheckforms.sdescription)
        			+ "\n"
        		; 
        	}
        	rs.close();
	        	//End the drop down list:
        	sOutPut += "</SELECT>\n";
        	sOutPut += "    </TD>\n";
		}catch (SQLException e){
			out.println("Error [1504190334] getting list of check forms - " + e.getMessage());
		}
		sOutPut += "  </TR>\n";
		
		boolean bAllowNextCheckNumberUpdate = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.BKUpdateNextCheckNumber, 
			sUserID, 
			getServletContext(), 
			sDBID, 
			(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
	    sOutPut += "  <TR>\n"
		    	+ "    <TD ALIGN=RIGHT>Next check number:</TD>\n"
		    	+ "    <TD ALIGN=LEFT>"
		;
	    
	    if (bAllowNextCheckNumberUpdate){
	    	sOutPut +=
	    		"<INPUT TYPE=TEXT NAME=\"" + SMTablebkbanks.lnextchecknumber + "\""
	        	+ " VALUE=\"" + clsStringFunctions.filter(bank.getsnextchecknumber()) + "\""
	        	+ " MAXLENGTH=" + "13"
	        	+ " SIZE = " + "8"
	        	+ ">"
	        	+ "&nbsp;"
        		+ "<INPUT TYPE=SUBMIT NAME='" + BUTTON_SET_NEXT_CHECK_NUMBER + "' VALUE='" + BUTTON_SET_NEXT_CHECK_NUMBER_LABEL + "'>" 
        		//Confirming checkbox:
        		+ "&nbsp;&nbsp;"
        		+ "<LABEL NAME='CONFIRMNEXTCHECKNUMBERUPDATE'>"
        		+ "<INPUT TYPE=CHECKBOX"
        		+ " NAME='" + CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER + "'"
        		+ " ID='" + CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER + "'"
        		+ ">"
        		+ "&nbsp;" + CHECKBOX_CONFIRMING_SET_NEXT_CHECK_NUMBER_LABEL
        		+ "</LABEL>"
        	;
	    }else{
	    	sOutPut += clsStringFunctions.filter(bank.getsnextchecknumber());
	    			
	    }

		sOutPut += "    </TD>"
		    + "  </TR>"
		;
		
		sOutPut += "</TABLE>\n";
		
		sOutPut += "<P>"
			+ "<INPUT TYPE=SUBMIT NAME='" + BUTTON_PRINT + "' VALUE='" + BUTTON_PRINT_LABEL + "'>" 
			+ "&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='" + BUTTON_ALIGN + "' VALUE='" + BUTTON_ALIGN_LABEL + "'>"
			+ "&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='" + BUTTON_RESET_SELECTED_CHECKS + "' VALUE='" + BUTTON_RESET_STATUS_LABEL + "'>"
			+ "</P>\n"
		;
		
		//Get the first and last check numbers for the batch:
		String sStartingCheckRangeNumber = "0";
		String sEndingCheckRangeNumber = "0";
		String SQL = "SELECT"
			+ " " + " MAX(CAST(" + SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber + " AS UNSIGNED)) AS ENDINGCHECKNUMBER"
			+ ", " + " MIN(CAST(" + SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber + " AS UNSIGNED)) AS STARTINGCHECKNUMBER"
			+ " FROM " + SMTableapchecks.TableName
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON " + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid + "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			+ " WHERE ("
			;
			//If we are only printing for one payment, then limit the list to that single payment entry:
			if (objSingleBatchEntry != null){
				SQL += " (" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid + " = " + sEntryID + ")";
			}else{
				SQL += "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + sBatchNumber + ")"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintingfinalized + " = 0)"
				;
			}
			SQL += ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			if (rs.next()){
				sStartingCheckRangeNumber = Long.toString(rs.getLong("STARTINGCHECKNUMBER"));
				sEndingCheckRangeNumber = Long.toString(rs.getLong("ENDINGCHECKNUMBER"));
			}
			rs.close();
		} catch (SQLException e1) {
			out.println("Error [1504190434] reading starting and ending checks with SQL: '" + SQL + "' - " + e1.getMessage());
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(RESET_RANGE_STARTING_CHECK_NUMBER, request).compareToIgnoreCase("") != 0){
			sStartingCheckRangeNumber = clsManageRequestParameters.get_Request_Parameter(RESET_RANGE_STARTING_CHECK_NUMBER, request);
		}
		if (clsManageRequestParameters.get_Request_Parameter(RESET_RANGE_ENDING_CHECK_NUMBER, request).compareToIgnoreCase("") != 0){
			sEndingCheckRangeNumber = clsManageRequestParameters.get_Request_Parameter(RESET_RANGE_ENDING_CHECK_NUMBER, request);
		}
		
		sOutPut += "<P>"
			+ "Reset a range of checks to: "
			
			+ "<SELECT"
			+ " NAME = '" + PARAM_RESET_CHECK_RANGE_STATUS + "'"
			+ " ID = '" + PARAM_RESET_CHECK_RANGE_STATUS + "'"
			+ ">\n"
			+ "  <OPTION VALUE = '" + "0" + "' >NOT PRINTED</OPTION>\n"
			+ "  <OPTION VALUE = '" + "1" + "' >PRINTED</OPTION>\n"
			+ "</SELECT>\n"
			
			+ " starting with check number:&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + RESET_RANGE_STARTING_CHECK_NUMBER + "\""
			+ " VALUE = \"" + sStartingCheckRangeNumber + "\""
			+ " MAXLENGTH = " + "13"
			+ " SIZE = " + "8"
			+ ">"
			+ "&nbsp; ending with check number:&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + RESET_RANGE_ENDING_CHECK_NUMBER + "\""
			+ " VALUE = \"" + sEndingCheckRangeNumber + "\""
			+ " MAXLENGTH = " + "13"
			+ " SIZE = " + "8"
			+ ">"

			+ "&nbsp;<INPUT TYPE=SUBMIT NAME='" + BUTTON_RESET_CHECK_RANGE + "' VALUE='" + BUTTON_RESET_CHECK_RANGE_LABEL + "'>"
			+ "</P>\n";
		
		try {
			sOutPut += getCheckList(sDBID, sUserFullName, batch, objSingleBatchEntry, sUserID);
		} catch (Exception e) {
			redirectProcess(
				sRedirectString
					+ "&Warning=Error [1504192060] - could not list checks - " + e.getMessage()
				, 
				response);
			return;
		}
		
		sOutPut += "</FORM>\n";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	private void checkForZeroAmtPayments(APBatchEntry singleentry, APBatch batch) throws Exception{
		
		//System.out.println("[1513201544]");
		if (singleentry != null){
			//System.out.println("[1513201545]");
			if (new BigDecimal(singleentry.getsentryamount().replaceAll(",", "")).compareTo(BigDecimal.ZERO) == 0){
				throw new Exception("Error [1510872273] - payment amount cannot be zero.");
			}
			return;
		}else{
			//Check all the unfinalized batch entries if they are supposed to have checks printed:
			try {
				for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
					APBatchEntry entry = batch.getBatchEntryArray().get(i);
					if (
						(entry.getsiprintcheck().compareToIgnoreCase("1") == 0)
						&& (entry.getsiprintingfinalized().compareToIgnoreCase("0") == 0)
						//Don't care about apply-tos, because they don't get checks:
						&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
						&& (new BigDecimal(entry.getsentryamount().replaceAll(",", "")).compareTo(BigDecimal.ZERO) == 0)
					){
						throw new Exception("Error [1510872274] - payment amount on entry " + entry.getsentrynumber() 
						+ " is zero, checks cannot be printed.");
					}
				}
			} catch (Exception e) {
				throw new Exception("Error [1513201542] trying to check all the unfinalized batch entries - " + e.getMessage());
			}
			return;
		}
	}
	private String getCheckList(String sDBID, String sUserFullName, APBatch batch, APBatchEntry singlebatchentry, String sUserID) throws Exception{
		String s = "";
		
		s += printTableHeading();
		
		s += sPrintCheckListHeadingRow();
		
		if (singlebatchentry != null){
			s += printCheckListLinesForSingleEntry(sDBID, sUserID, sUserFullName, singlebatchentry);
		}else{
			s += printCheckListLines(sDBID, sUserID, sUserFullName, batch);
		}
		s += printTableFooting();
		
		return s;
	}
	private String sPrintCheckListHeadingRow(){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Check status</B>"
			+ "    </TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + " \"  VALIGN=BOTTOM >"
			+ "<B>Check<BR>number</B>"
			+ "    </TD>\n";
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Payee</B>"
			+ "    </TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Payee name</B>"
			+ "    </TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Entry<BR>number</B>"
			+ "    </TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Page<BR>Number</B>"
			+ "    </TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Last<BR>Page?</B>"
			+ "    </TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + " \" VALIGN=BOTTOM >"
			+ "<B>Check<BR>amount</B>"
			+ "    </TD>\n";

		s += "</TR>";
		
		return s;
	}
	private void deleteUnfinalizedChecks(String sDBID, String sUser) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".deleteUnfinalizedChecks - user: " + sUser
			);
		} catch (Exception e) {
			throw new Exception("Error [1510845328] getting connection to delete unfinalized checks - " + e.getMessage());
		}
		
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059474]");
			throw new Exception("Error [1510845329] starting data transaction.");
		}
		
		String SQL = "DELETE " + SMTableapchecks.TableName + " FROM"
			+ " " + SMTableapchecks.TableName
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON " + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid + "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			+ " WHERE ("
				//Delete the checks if there IS no longer a batch entry matching it
				+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid + " IS NULL)"
				//OR delete if the matching batch entry is not flagged as 'printing finalized'
				+ " OR (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintingfinalized + " = 0)"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059475]");
			throw new Exception("Error [1510845327] deleting unfinalized checks with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		//Delete the orphaned apchecklines:
		SQL = "DELETE " + SMTableapchecklines.TableName + " FROM"
			+ " " + SMTableapchecklines.TableName
			+ " LEFT JOIN " + SMTableapchecks.TableName
			+ " ON " + SMTableapchecklines.TableName + "." + SMTableapchecklines.lcheckid + "=" + SMTableapchecks.TableName + "." + SMTableapchecks.lid
			+ " WHERE ("
				+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lid + " IS NULL)"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059476]");
			throw new Exception("Error [1510845331] deleting orhpaned check lines with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		//Commit the transaction:
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059477]");
			throw new Exception("Error [1510845330] committing transaction to delete unfinalized checks.");
		}
		//Free the connection:
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059478]");
		
		return;
		
	}
	private String printCheckListLinesForSingleEntry(
		String sDBID,
		String sUserID,
		String sUserFullName,
		APBatchEntry singleentry
		) throws Exception{
		String s = "";
		
		//We need a string of comma-delimited entry IDs to pass down the line to the confirmation screen, so that the program can be told WHICH batch entries
		//now have FINALIZED checks:
		String sListOfFinalizedPaymentEntries = "";
		
		//Just double check that somehow we're not trying to print a 'finalized' check:
		if (singleentry.getsiprintingfinalized().compareToIgnoreCase("1") == 0){
			throw new Exception("Checks for this payment entry have already been printed and finalized, so you cann't re-print it.");
		}
		//See if there are already checks for this entry and if so, use those in the list:
		String SQL = "SELECT * FROM " + SMTableapchecks.TableName
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON (" 
				+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid + ")"
			+ ")" 
			+ " WHERE ("
				+ " (" + SMTableapchecks.lbatchentryid + " = " + singleentry.getslid() + ")"
				+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintingfinalized + " = 0)"
				+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid + " IS NOT NULL)"
				+ ") ORDER BY LPAD(" + SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber + ", " + SMTableapchecks.schecknumberLength + ", ' ')"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".printCheckListLines - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
		);
		boolean bChecksHaveBeenPrinted = false;
		boolean bOddRow = true;
		while(rs.next()){
			bChecksHaveBeenPrinted = true;
			String sLastPage = "Y";
			if (rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ilastpage) == 0){
				sLastPage = "<SPAN style=\" color:red; font-weight:bold; \" >N</SPAN>";
			}
			s += printCheckLine(
				Integer.toString(rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.iprinted)),
				rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber),
				rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.svendoracct),
				rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.svendorname),
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapchecks.TableName + "." + SMTableapchecks.bdamount)),
				rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ivoid),
				Long.toString(rs.getLong(SMTableapchecks.TableName + "." + SMTableapchecks.lentrynumber)),
				Long.toString(rs.getLong(SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber)),
				Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid)),
				Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype)),
				Integer.toString(rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ipagenumber)),
				sLastPage,
				bOddRow,
				sDBID
			);
			bOddRow = !bOddRow;
		}
		rs.close();
		
		//If they WERE no checks already for this entry, then we'll just build an empty check line for the entry:
		
		if (!bChecksHaveBeenPrinted){
			s += printCheckLine(
				Integer.toString(0),
				singleentry.getschecknumber(),
				singleentry.getsvendoracct(),
				singleentry.getsremittoname(),
				singleentry.getsentryamount(),
				0,
				singleentry.getsentrynumber(),
				singleentry.getsbatchnumber(),
				singleentry.getslid(),
				singleentry.getsentrytype(),
				"0",
				"(N/A)",
				false,
				sDBID
			);
		}
		
		sListOfFinalizedPaymentEntries += singleentry.getslid();
		s += "\n"
			+ "<INPUT"
			+ " TYPE=HIDDEN"
			+ " NAME = \"" + LIST_OF_ENTRYIDS_IN_CHECK_RUN + "\""
			+ " ID = \"" + LIST_OF_ENTRYIDS_IN_CHECK_RUN + "\""
			+ " VALUE = \"" + sListOfFinalizedPaymentEntries + "\""
			+ ">" + "\n"
		;
		
		return s;
	}
	private String printCheckListLines(
		String sDBID,
		String sUserID,
		String sUserFullName,
		APBatch batch
		) throws Exception{
		String s = "";
		
		//We need a string of comma-delimited entry IDs to pass down the line to the confirmation screen, so that the program can be told WHICH batch entries
		//now have FINALIZED checks:
		String sListOfFinalizedPaymentEntries = "";
		
		//First, we try to get any checks that were already printed for this batch
		//If we are returning to this screen from trying to print checks, then there may already be checks created and possibly printed that we need to
		//re-display to allow the user to reprint them indefinitely, until they are 'finalized':
		boolean bChecksFound = false;
		boolean bOddRow = true;
		ResultSet rs;
		String SQL = "";
		try {
			SQL = "SELECT * FROM " + SMTableapchecks.TableName
				+ " LEFT JOIN " + SMTableapbatchentries.TableName
				+ " ON (" 
					+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
					+ " AND (" + SMTableapchecks.TableName + "." + SMTableapchecks.lentrynumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
				+ ")" 
				+ " WHERE ("
					+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + batch.getsbatchnumber() + ")"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintingfinalized + " = 0)"		
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid + " IS NOT NULL)"
					+ ") ORDER BY LPAD(" + SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber + ", " + SMTableapchecks.schecknumberLength + ", ' ')"			
			;
			rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".printCheckListLines - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
			);
			
			while (rs.next()){
				bChecksFound = true;
				//Populate the fields we need with the check values:
				String sLastPage = "Y";
				if (rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ilastpage) == 0){
					sLastPage = "<SPAN style=\" color:red; font-weight:bold; \" >N</SPAN>";
				}
				
				s += printCheckLine(
					Integer.toString(rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.iprinted)),
					rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber),
					rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.svendoracct),
					rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.svendorname),
					clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapchecks.TableName + "." + SMTableapchecks.bdamount)),
					rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ivoid),
					Long.toString(rs.getLong(SMTableapchecks.TableName + "." + SMTableapchecks.lentrynumber)),
					Long.toString(rs.getLong(SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber)),
					Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid)),
					Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype)),
					Integer.toString(rs.getInt(SMTableapchecks.TableName + "." + SMTableapchecks.ipagenumber)),
					sLastPage,
					bOddRow,
					sDBID
				);
				sListOfFinalizedPaymentEntries += Long.toString(rs.getLong(SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid)) + ",";
				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1504194831] reading AP checks with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//If there were NO unfinalized checks found for the batch, then they haven't been printed yet, and we 
		//build the list from the available payment batch entries instead:
		if (!bChecksFound){
			for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
				APBatchEntry entry = batch.getBatchEntryArray().get(i);
				if (
					//If the entry is SUPPOSED to have a check printed:
					(entry.getsiprintcheck().compareToIgnoreCase("1") == 0)
					//AND if the entry doesn't already have a 'finalized' check associated with it:
					&& (entry.getsiprintingfinalized().compareToIgnoreCase("0") == 0)
					//AND if it's not an 'apply-to', which doesn't GET checks printed:
					&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
				){
					s += printCheckLine(
						Integer.toString(0),
						entry.getschecknumber(),
						entry.getsvendoracct(),
						entry.getsremittoname(),
						entry.getsentryamount(),
						0,
						entry.getsentrynumber(),
						entry.getsbatchnumber(),
						entry.getslid(),
						entry.getsentrytype(),
						"0",
						"(N/A)",
						(i % 2) != 0,
						sDBID
					);
					sListOfFinalizedPaymentEntries += entry.getslid() + ",";
				}
			}
		}
		
		//Store the list of entry IDs so that if the user finalizes the check printing, we can pass that list back to be finalized:
		//Trim off any trailing commas:
		if (sListOfFinalizedPaymentEntries.endsWith(",")){
			sListOfFinalizedPaymentEntries = sListOfFinalizedPaymentEntries.substring(0, sListOfFinalizedPaymentEntries.length() - 1);
		}
		s += "\n"
			+ "<INPUT"
			+ " TYPE=HIDDEN"
			+ " NAME = \"" + LIST_OF_ENTRYIDS_IN_CHECK_RUN + "\""
			+ " ID = \"" + LIST_OF_ENTRYIDS_IN_CHECK_RUN + "\""
			+ " VALUE = \"" + sListOfFinalizedPaymentEntries + "\""
			+ ">" + "\n"
		;
		
		return s;
	}
	private String printCheckLine(
		String sCheckPrintedStatus,
		String sCheckNumber,
		String sPayee,
		String sPayeeName,
		String sCheckAmount,
		int iVoid,
		String sEntryNumber,
		String sBatchNumber,
		String sEntryID,
		String sEntryType,
		String sPageNumber,
		String sLastPage,
		boolean bOddRow,
		String sDBID
		) throws Exception{
		String s = "";
		
		String sFormattedCheckNumber = sCheckNumber;
		String sDisplayedCheckPrintedStatus = "NOT PRINTED";
		if (sCheckNumber.compareToIgnoreCase("") == 0){
			sFormattedCheckNumber = "-0-";
		}else{
			sDisplayedCheckPrintedStatus = 
				"<SELECT NAME = '" + CHECK_SELECTED_PREFIX + sCheckNumber + "'"
					+ " ID = '" + CHECK_SELECTED_PREFIX + sCheckNumber + "'"
				+ ">"
			;
			if(sCheckPrintedStatus.compareToIgnoreCase("0") == 0){
				sDisplayedCheckPrintedStatus +=
					" <OPTION VALUE = 0  SELECTED=yes >NOT PRINTED</OPTION>"
					+ " <OPTION VALUE = 1>PRINTED</OPTION>";
			}else{
				sDisplayedCheckPrintedStatus +=
					" <OPTION VALUE = 0>NOT PRINTED</OPTION>"
					+ " <OPTION VALUE = 1  SELECTED=yes >PRINTED</OPTION>";
			}

			sDisplayedCheckPrintedStatus += "</SELECT>";
		}
		
		String sEntryNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit" 
    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
    	    + "&" + SMTableapbatchentries.lid + "=" + sEntryID
    	    + "&" + SMTableapbatchentries.ientrytype + "=" + sEntryType
    	    + "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    + "\">"
    	    + sEntryNumber 
    	    + "</A>"
    	    + "\n"
    	;
		
		if (bOddRow){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
		}
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			
			//Check printed status
			+  sDisplayedCheckPrintedStatus
			+ "</TD>\n"
			
			//Check number
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sFormattedCheckNumber
			+ "</TD>\n"
			
			//Payee
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sPayee
			+ "</TD>\n"
			
			//Payee name
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sPayeeName
			+ "</TD>\n"

			//Entry number
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sEntryNumberLink
			+ "</TD>\n"
			
			//Page number
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sPageNumber
			+ "</TD>\n"
			
			//Last Page?
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sLastPage
			+ "</TD>\n"
			
			//Check amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sCheckAmount
			+ "</TD>\n"
			+ "  </TR>\n"
		;
		
		return s;
	}
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		return s;
	}
	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1504188232] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1504188233] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}