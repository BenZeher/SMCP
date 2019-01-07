package smap;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditPaymentEdit  extends HttpServlet {

	public static final String BUTTON_LABEL_UNAPPLYLINE = "UNApply";
	public static final String BUTTON_NAME_UNAPPLYLINE = "UNApplyButton";
	public static final String COMMAND_VALUE_UNAPPLYLINE = "UnapplyLine";
	public static final String BUTTON_LABEL_REMOVELINE = "Remove";
	public static final String COMMAND_VALUE_REMOVELINE = "RemoveLine";
	public static final String LINE_NUMBER_TO_DELETE_PARAM = "LineNumberParam";
	public static final String LINE_NUMBER_TO_UNAPPLY_PARAM = "UnapplyLineNumber";
	public static final String BUTTON_LABEL_APPLYLINE = "Apply";
	public static final String COMMAND_VALUE_APPLYTODOC = "ApplyLine";
	public static final String APPLYTODOCNUMBER_TO_APPLY_PARAM = "ApplyToDocNumber";
	public static final String BUTTON_LABEL_FINDAPPLYTODOCUMENTNUMBER = "Find apply to document";
	
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	public static final String COMMAND_VALUE_SAVE_AND_ADD = "Updateandaddnew";

	public static final String COMMAND_VALUE_FINDVENDOR = "FINDVENDOR";
	public static final String RETURNING_FROM_FIND_VENDOR_FLAG = "RETURNINGFROMFINDER";
	public static final String FOUND_VENDOR_PARAMETER = "FOUND" + SMTableapbatchentries.svendoracct;
	
	public static final String COMMAND_VALUE_UPDATEVENDORINFO = "UPDATEVENDORINFO";
	public static final String UPDATED_VENDOR_INFO_PARAMETER = "UPDATED" + SMTableapbatchentries.svendoracct + "INFO";
	public static final String RETURNING_FROM_UPDATEVENDORINFO_FLAG = "RETURNINGFROMUPDATEVENDORINFO";
	
	public static final String COMMAND_VALUE_PRINT_CHECKS = "PRINTCHECKS";
	
	public static final String CALCULATED_LINE_TOTAL_FIELD = "CALCULATEDLINETOTAL";
	public static final String CALCULATED_LINE_TOTAL_FIELD_CONTAINER = "CALCULATEDLINETOTALCONTAINER";
	
	public static final String TABLE_UNAPPLIED_DOCUMENTS = "APPLYTODOCSTABLE";
	public static final String TABLE_APPLIED_DOCUMENTS = "APPLIEDDOCUMENTS";
	public static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW"; //"#FF2080";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	
	public static final String NET_AMT_FIELD_NAME = "NETAMT";
	
	/*
	public static final String APPLIED_TABLE_LINENO_COLUMN_NUMBER = "0";
	public static final String APPLIED_TABLE_DOCDATE_COLUMN_NUMBER = "1";
	public static final String APPLIED_TABLE_DUEDATE_COLUMN_NUMBER = "2";
	public static final String APPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER = "3";
	public static final String APPLIED_TABLE_ORIGINALAMT_COLUMN_NUMBER = "4";
	public static final String APPLIED_TABLE_CURRENTAMT_COLUMN_NUMBER = "5";
	public static final String APPLIED_TABLE_APPLYINGAMT_COLUMN_NUMBER = "6";
	
	public static final String UNAPPLIED_TABLE_DOCDATE_COLUMN_NUMBER = "0";
	public static final String UNAPPLIED_TABLE_DUEDATE_COLUMN_NUMBER = "1";
	public static final String UNAPPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER = "2";
	public static final String UNAPPLIED_TABLE_ORIGINALAMT_COLUMN_NUMBER = "3";
	public static final String UNAPPLIED_TABLE_CURRENTAMT_COLUMN_NUMBER = "4";
	public static final String UNAPPLIED_TABLE_APPLY_COLUMN_NAME = "APPLY_INVOICE_BUTTON_COLUMN";
	*/
	public static final int NUMBER_PADDING_LENGTH = 11;
	
	public static final String BOOKMARK_TOP_OF_TABLES = "TopOfTables";
	public static final String RETURN_TO_TABLES_BOOKMARK = "RETURNTOTABLESPARAM";
	
	//Hot keys:
	public static final String FIND_VENDOR_BUTTON_LABEL = "<B><FONT COLOR=RED>F</FONT></B>ind vendor"; // F
	public static final String UPDATE_VENDOR_INFO_BUTTON_LABEL = "Update <B><FONT COLOR=RED>v</FONT></B>endor info"; // V
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; // U
	public static final String UPDATE_AND_ADD_NEW_BUTTON_LABEL = "Update and add <B><FONT COLOR=RED>n</FONT></B>ew"; // N
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; // D
	public static final String PRINT_CHECK_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>rint check"; // P
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		APBatchEntry entry = new APBatchEntry(request);
		//System.out.println("[1489520515] - entry.getsbatchnumber() = '" + entry.getsbatchnumber() + "'");
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"AP Entry",
				SMUtilities.getFullClassName(this.toString()),
				"smap.APEditPaymentAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditBatches
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditBatches)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(APBatchEntry.OBJECT_NAME) != null){
	    	entry = (APBatchEntry) currentSession.getAttribute(APBatchEntry.OBJECT_NAME);
	    	currentSession.removeAttribute(APBatchEntry.OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		//We'll need to reload the entry every time then, to make sure we get the lines loaded as well:
		    		try {
						entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
					} catch (Exception e) {
						response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
							+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
					}
	    		//}
	    	}
	    }
	    
	    APBatch batch = new APBatch(entry.getsbatchnumber());
	    try {
	    	
			batch.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
			//System.out.println("[1543353706] load batch time elapsed: " + (System.currentTimeMillis() - lStartingtime) / 1000);
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
					+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
					+ "&Warning=Error [1490043487] could not load batch number " + entry.getsbatchnumber() + " - " + e1.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
				return;
		}
	    
	    //If the entry date is empty, then default it to the batch date:
	    if (entry.getsdatentrydate().compareToIgnoreCase(SMUtilities.EMPTY_DATE_VALUE) == 0){
	    	entry.setsdatentrydate(batch.getsbatchdate());
	    }
	    
	    //Is this payment entry editable?
	    boolean bEntryIsEditable = true;
	    //If the batch is not editable, then this entry can't be editable:
	    if (!batch.bEditable()){
	    	bEntryIsEditable = false;
	    }
	    //If this entry has had its check(s) finalized, then this entry can't be edited:
	    if (entry.getsiprintingfinalized().compareToIgnoreCase("1") == 0){
	    	bEntryIsEditable = false;
	    }
	    
		//If we are returning from finding a vendor, update that vendor and vendor info:
		if (request.getParameter(FOUND_VENDOR_PARAMETER) != null){
			entry.setsvendoracct(clsManageRequestParameters.get_Request_Parameter(FOUND_VENDOR_PARAMETER, request));
	    	try {
				entry.loadVendorInformation(getServletContext(), smedit.getsDBID(), smedit.getUserName(), smedit.getUserID(), smedit.getFullUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1490633312] loading vendor information for vendor '" + entry.getsvendoracct() + "' - " + e.getMessage()
					+ "</B></FONT><BR>"
				);
			}
		}
	    
	    //If we are returning from a request to refresh vendor info, update the vendor info:
	    if (request.getParameter(RETURNING_FROM_UPDATEVENDORINFO_FLAG) != null){
	    	try {
				entry.loadVendorInformation(getServletContext(), smedit.getsDBID(), smedit.getUserName(), smedit.getUserID(), smedit.getFullUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1490633313] loading vendor information for vendor '" + entry.getsvendoracct() + "' - " + e.getMessage()
					+ "</B></FONT><BR>"
				);
			}
	    }
		
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
	    //smedit.getPWOut().println(SMUtilities.getJQueryIncludeString());
	    smedit.printHeaderTable();
	    
	    smedit.setbIncludeDeleteButton(false);
	    smedit.setbIncludeUpdateButton(false);
	    
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to Accounts Payable Main Menu</A><BR>\n");
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch List</A><BR>\n");
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit?"
	    		+ SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
	    		+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch " + entry.getsbatchnumber() + "</A><BR>\n");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry, bEntryIsEditable), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
				+ "&Warning=Could not create payment entry screen: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, APBatchEntry entry, boolean bEditable) throws SQLException{
		String s = "";
		
		try {
			s += sCommandScript(sm.getsDBID(), entry, bEditable, sm);
		} catch (Exception e2) {
			s += "<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>";
		}
		
		//Store some command values here:
		//Store whether or not the record has been changed:
		
		//IF a vendor was found by the 'Find Vendor' function, or an apply to document number was returned, then we KNOW the record was changed:
		if (
			(sm.getRequest().getParameter(FOUND_VENDOR_PARAMETER) != null)
		){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		//Otherwise, set the 'record changed' flag to match the incoming parameter:
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		}
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//Store which line the user has chosen to unapply:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINE_NUMBER_TO_UNAPPLY_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINE_NUMBER_TO_UNAPPLY_PARAM + "\""
		+ "\">";
		
		//Store which line the user has chosen to apply:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APPLYTODOCNUMBER_TO_APPLY_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + APPLYTODOCNUMBER_TO_APPLY_PARAM + "\""
		+ "\">";
		
		//Store which line the user has chosen to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\""
		+ "\">";
		
		//Store the 'printing finalized' value:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.iprintingfinalized + "\" VALUE=\"" + entry.getsiprintingfinalized() + "\""
		+ " id=\"" + SMTableapbatchentries.iprintingfinalized + "\""
		+ "\">";
		
		//Store the GL control account, which the user shouldn't be changing on the payment screen:
		s += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.scontrolacct + "\""
				+ " VALUE=\"" + entry.getscontrolacct() + "\" >";
		
		s += "<BR><TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		String sID = "NEW";
		if (
			(entry.getslid().compareToIgnoreCase("-1") != 0)
			&& (entry.getslid().compareToIgnoreCase("0") != 0)
			&& (entry.getslid().compareToIgnoreCase("") != 0)
		){
			sID = entry.getslid();
		}else{
			s += "\n" + "<INPUT TYPE=HIDDEN NAME=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\""
				+ " id=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\""
				+ " VALUE=\"" + "Y" + "\""
				+ ">"
			;
		}
		
		s += "  <TR>\n";
		
		// ******* Batch number:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Batchnumber:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ entry.getsbatchnumber() 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lbatchnumber + "\" VALUE=\"" 
			+ entry.getsbatchnumber() + "\">"
			+ "</B></TD>\n"
		;
		
		// ******* Entry number:
		String sEntryNumber = "NEW";
		if (
			(entry.getsentrynumber().compareToIgnoreCase("-1") != 0)
			&& (entry.getsentrynumber().compareToIgnoreCase("0") != 0)
			&& (entry.getsentrynumber().compareToIgnoreCase("") != 0)
		){
			sEntryNumber = entry.getsentrynumber();
		}
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Entrynumber:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ sEntryNumber 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lentrynumber + "\" VALUE=\"" 
			+ entry.getsentrynumber() + "\">"
			+ "</B></TD>\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		// ******* Entry ID:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >"
				+ " " + "Entry ID:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD>\n"
		;

		// ******* Document type:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Document&nbsp;type:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(entry.getsentrytype())).toUpperCase()
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.ientrytype + "\" VALUE=\"" 
			+ entry.getsentrytype() + "\">"
			+ "</B></TD>\n"
		;

		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		
		String sControlHTML = "";
		
        // ******* Entry date:
    	s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Entry&nbsp;date:&nbsp;</TD>\n"
    	;
    	
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.datentrydate + "\""
	    	    + " VALUE=\"" + clsStringFunctions.filter(entry.getsdatentrydate()) + "\""
	    	    + " MAXLENGTH=" + "10"
	    	    + " SIZE = " + "8"
	    	    + " onchange=\"flagDirty();\""
	    	    + ">"
	    	    + "&nbsp;" + SMUtilities.getDatePickerString(SMTableapbatchentries.datentrydate, getServletContext())
	    	    
    	    ;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datentrydate + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatentrydate()) + "\""
    	    	+ ">"
    	    	+ entry.getsdatentrydate()
    	    ;
    	}
    		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
        // ******* Document date:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Document&nbsp;date:&nbsp;</TD>\n"
	    	;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.datdocdate + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdocdate()) + "\""
    	    	+ " MAXLENGTH=" + "10"
    	    	+ " SIZE = " + "8"
    	    	+ " onchange=\"flagDirty();\""
    	    	+ ">"
    	    	+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableapbatchentries.datdocdate, getServletContext())
    	    ;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datdocdate + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdocdate()) + "\""
    	    	+ ">"
    	    	+ entry.getsdatdocdate()
    	    ;
    	}

    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
		s += "  </TR>\n";
		
		s += "  <TR>\n";
    	
        // ******* Doc number:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Document&nbsp;number:&nbsp;</TD>\n"
	    ;

		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sdocnumber + "\""
    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdocnumber()) + "\""
    		+ ">"
    		+ "<B>" + entry.getsdocnumber() + "</B>"
    	;
    	
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
 
		// Print check?
		String sPrintCheckValue = "N";
		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >&nbsp;</TD>\n";
     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.iprintcheck + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsiprintcheck()) + "\""
 	    		+ ">"
 	    	;
		}else{
	     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Print&nbsp;check?</TD>\n"
	     		    ;
	     	if (bEditable){
	     		String sTemp = "";
	     		if (entry.getsiprintcheck().compareToIgnoreCase("0") != 0){
	    			sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
	    		}
	     		sControlHTML = "<INPUT TYPE=CHECKBOX"
	     			+ " NAME=\"" + SMTableapbatchentries.iprintcheck + "\""
	     			+ " ID=\"" + SMTableapbatchentries.iprintcheck + "\""
	     			+ " " + sTemp
	 	    		+ " onchange=\"processToggleCheckNumber();\""
	 	    		+ ">"
	 	    	;
	     	}else{
	     		if (entry.getsiprintcheck().compareToIgnoreCase("1") == 0){
	     			sPrintCheckValue = "Y";
	     		}
	     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.iprintcheck + "\""
	 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsiprintcheck()) + "\""
	 	    		+ ">"
	 	    		+ "<B>" + sPrintCheckValue + "</B>"
	 	    	;
	     	}
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
		
		s += "  </TR>\n";
		
		s += "  <TR>\n";
    	
    	// ******* Amount:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Amount:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
			//The entry amount is automatic:
			sControlHTML = "<B><label id=\"" + CALCULATED_LINE_TOTAL_FIELD + "\" ></label></B>\n"
				//Also carry the hidden entry amount field - but we'll recalculate this every time anyway....
				+ "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.bdentryamount + "\""
				+ " ID=\"" + SMTableapbatchentries.bdentryamount + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(APBatchEntry.displayAbsoluteValueForPaymentInputScreen(entry.getsentryamount())) + "\""
				+ ">"
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.bdentryamount + "\""
    			+ " ID=\"" + SMTableapbatchentries.bdentryamount + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(APBatchEntry.displayAbsoluteValueForPaymentInputScreen(entry.getsentryamount())) + "\""
	    		+ ">"
	    		+ entry.getsentryamount()
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";

    	
        // ******* Check number:
		if (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Check number:&nbsp;</TD>\n";
		}else{
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >&nbsp;</TD>\n";
		}
		
    	if ((bEditable) && (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.schecknumber + "\""
    			+ " ID=\"" + SMTableapbatchentries.schecknumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getschecknumber()) + "\""
	    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.schecknumberLength)
	    		+ " SIZE = " + "30"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.schecknumber + "\""
    			+ " ID=\"" + SMTableapbatchentries.schecknumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getschecknumber()) + "\""
	    		+ ">"
	    		+ entry.getschecknumber()
	    	;
    	}

    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
    	
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		//If this is a payment, and if the check has been 'finalized', add a row here and notify the user that the check has been finalized:
		if (
			(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			&& (entry.getsiprintingfinalized().compareToIgnoreCase("1") == 0)
		){
			s += "  <TR>\n"
				+ "    <TD>&nbsp;</TD>" + "\n"
				+ "    <TD>&nbsp;</TD>" + "\n"
				+ "    <TD COLSPAN=2 style = \" font-weight: bold; color: red;   \" >NOTE: Check(s) for this entry have been finalized.</TD>" + "\n"
				+ "  </TR>"
			;
		}
    	
        // ******* Vendor acct:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Vendor&nbsp;account:&nbsp;</TD>\n";
	    	
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.svendoracct + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsvendoracct()) + "\""
	    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.svendoracctLength)
	    		+ " SIZE = " + "10"
	    		+ " onchange=\"updateVendorInfo();\""
	    		+ ">"
	    		+ "&nbsp;" + createFindVendorButton() + createUpdateVendorInfoButton()
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.svendoracct + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsvendoracct()) + "\""
	    		+ ">"
	    		+ "<B>" + entry.getsvendoracct() + "</B> - "
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
    	// ******* Vendor name:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Vendor&nbsp;name:&nbsp;</TD>\n";
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" 
    		+ "<INPUT TYPE=INPUT"
    		+ " NAME=\"" + SMTableapbatchentries.svendorname + "\""
    		+ " ID=\"" + SMTableapbatchentries.svendorname + "\""
    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsvendorname()) + "\""
    		+ " readonly"
    		+ " SIZE = " + "40"
    		+ ">"
    		+ "</TD>\n";
    	
		s += "  </TR>\n";
		
		s += "  <TR>\n";
    	
        // ******* Desc:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Description:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.sentrydescription + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsentrydescription()) + "\""
	    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sentrydescriptionLength)
	    		+ " SIZE = " + "30"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sentrydescription + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsentrydescription()) + "\""
	    		+ ">"
	    		+ entry.getsentrydescription()
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    		
		
    	// ******* Bank:
    	if (
    		(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
    		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
    		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
    	){
    		
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Bank:&nbsp;</TD>\n";
			if (bEditable){
				ArrayList<String> arrBankIDs = new ArrayList<String>(0);
				ArrayList<String> arrBankNames = new ArrayList<String>(0);
				String SQL = "SELECT"
					+ " " + SMTablebkbanks.lid
					+ ", " + SMTablebkbanks.sbankname
					+ ", " + SMTablebkbanks.sshortname
					+ " FROM " + SMTablebkbanks.TableName
					+ " WHERE ("
						+ "(" + SMTablebkbanks.iactive + " = 1)"
					+ ")"
					+ " ORDER BY " + SMTablebkbanks.sshortname
				;
				//First, add a blank item so we can be sure the user chooses one:
				arrBankIDs.add("");
				arrBankNames.add("*** Select bank ***");
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
							sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
							+ ".getEditHTML - user: " 
							+ sm.getUserID()
							+ " - "
							+ sm.getFullUserName()
							);
					while (rs.next()) {
						arrBankIDs.add(Long.toString(rs.getLong(SMTablebkbanks.lid)));
						arrBankNames.add(
							rs.getString(SMTablebkbanks.sshortname)
						);
					}
					rs.close();
				} catch (SQLException e) {
					s += "<B>Error [1493398399] reading banks - " + e.getMessage() + "</B><BR>";
				}
	
				//If there was only ONE BANK, then we can remove the first blank line, and let the drop down default to the single bank:
				if (arrBankIDs.size() == 2){
					arrBankIDs.remove(0);
					arrBankNames.remove(0);
				}
				
				sControlHTML = "<SELECT"
					+ " NAME = \"" + SMTableapbatchentries.lbankid + "\""
					+ " ID = \"" + SMTableapbatchentries.lbankid + "\""
					+ " onchange=\"remitToChange(this);\""
					 + " >\n"
				;
				for (int i = 0; i < arrBankIDs.size(); i++){
					sControlHTML += "<OPTION";
					if (arrBankIDs.get(i).toString().compareTo(entry.getslbankid()) == 0){
						sControlHTML += " selected=yes";
					}
					sControlHTML += " VALUE=\"" + arrBankIDs.get(i).toString() + "\">" + arrBankNames.get(i).toString() + "\n";
				}
				sControlHTML += "</SELECT>"
				;
				
			}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lbankid + "\""
			    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslbankid()) + "\""
			    		+ ">"
			    		+ entry.getslbankid()
			    	;
			}
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lbankid + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslbankid()) + "\""
		    		+ ">"
		    		+ "&nbsp;"
		    	;
    	}
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
    	//Remit-to fields*************************************************************
    	//Don't show remit-to fields on an 'apply-to':
    	if (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
	    	s += "  <TR style=\"background-color:grey; color:white; \" ><TD COLSPAN=4><B>&nbsp;REMIT-TO INFORMATION:</B></TD></TR>\n";
	    	
	    	// ******* Remit to codes:
	    	s += "  <TR>\n";
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Remit-to&nbsp;codes:&nbsp;</TD>\n"
			    ;
			if (bEditable){
				ArrayList<String> arrRemitToCodes = new ArrayList<String>(0);
				ArrayList<String> arrRemitToNames = new ArrayList<String>(0);
				String SQL = "SELECT"
					+ " " + SMTableapvendorremittolocations.sremittocode
					+ ", " + SMTableapvendorremittolocations.sremittoname
					+ " FROM " + SMTableapvendorremittolocations.TableName
					+ " WHERE ("
						+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + entry.getsvendoracct() + "')"
					+ ")"
					+ " ORDER BY " + SMTableapvendorremittolocations.sremittocode
				;
				//First, add a blank item so we can be sure the user chose one:
				arrRemitToCodes.add("");
				arrRemitToNames.add("*** Select remit-to ***");
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
							sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
							+ ".getEditHTML - user: " + sm.getUserID()
							+ " - "
							+ sm.getFullUserName()
							);
					while (rs.next()) {
						arrRemitToCodes.add(rs.getString(SMTableapvendorremittolocations.sremittocode));
						arrRemitToNames.add(
							rs.getString(SMTableapvendorremittolocations.sremittocode)
							+ " - "
							+ rs.getString(SMTableapvendorremittolocations.sremittoname)
						);
					}
					rs.close();
				} catch (SQLException e) {
					s += "<B>Error [1491930640] reading remit-to codes - " + e.getMessage() + "</B><BR>";
				}
	
				sControlHTML = "<SELECT"
					+ " NAME = \"" + SMTableapbatchentries.sremittocode + "\""
					+ " ID = \"" + SMTableapbatchentries.sremittocode + "\""
					+ " onchange=\"remitToChange(this);\""
					 + " >\n"
				;
				for (int i = 0; i < arrRemitToCodes.size(); i++){
					sControlHTML += "<OPTION";
					if (arrRemitToCodes.get(i).toString().compareTo(entry.getsremittocode()) == 0){
						sControlHTML += " selected=yes";
					}
					sControlHTML += " VALUE=\"" + arrRemitToCodes.get(i).toString() + "\">" + arrRemitToNames.get(i).toString() + "\n";
				}
				sControlHTML += "</SELECT>"
				;
				
			}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittocode + "\""
			    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittocode()) + "\""
			    		+ ">"
			    		+ entry.getsremittocode()
			    	;
			}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	
	    	// ******* Remit to name:
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Name:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittoname + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittoname + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoname()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittonameLength)
		    		+ " SIZE = " + "30"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittoname + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoname()) + "\""
		    		+ ">"
		    		+ entry.getsremittoname()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    		
	    	// ******* Remit to address line 1
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Address&nbsp;line&nbsp;1:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittoaddressline1 + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittoaddressline1 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline1()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittoaddressline1Length)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittoaddressline1 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline1()) + "\""
		    		+ ">"
		    		+ entry.getsremittoaddressline1()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    		
	    	
	    	// ******* Remit to address line 2
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Address&nbsp;line&nbsp;2:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittoaddressline2 + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittoaddressline2 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline2()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittoaddressline2Length)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittoaddressline2 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline2()) + "\""
		    		+ ">"
		    		+ entry.getsremittoaddressline2()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    	
	    	// ******* Remit to address line 3
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Address&nbsp;line&nbsp;3:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittoaddressline3 + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittoaddressline3 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline3()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittoaddressline3Length)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittoaddressline3 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline3()) + "\""
		    		+ ">"
		    		+ entry.getsremittoaddressline3()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    	
	    	// ******* Remit to address line 4
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Address&nbsp;line&nbsp;4:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittoaddressline4 + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittoaddressline4 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline4()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittoaddressline4Length)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittoaddressline4 + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittoaddressline4()) + "\""
		    		+ ">"
		    		+ entry.getsremittoaddressline4()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    	
	    	// ******* Remit to city
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >City:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittocity + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittocity + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittocity()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittocityLength)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittocity + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittocity()) + "\""
		    		+ ">"
		    		+ entry.getsremittocity()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	
	    	// ******* Remit to state
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >State:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittostate + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittostate + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittostate()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittostateLength)
		    		+ " SIZE = " + "20"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittostate + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittostate()) + "\""
		    		+ ">"
		    		+ entry.getsremittostate()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    	
	    	// ******* Remit to postal code
	    	s += "  <TR>\n"
			    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Postal&nbsp;code:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittopostalcode + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittopostalcode + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittopostalcode()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittopostalcodeLength)
		    		+ " SIZE = " + "10"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittopostalcode + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittopostalcode()) + "\""
		    		+ ">"
		    		+ entry.getsremittopostalcode()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	
	    	// ******* Remit to country
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Country:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT"
	    			+ " NAME=\"" + SMTableapbatchentries.sremittocountry + "\""
	    			+ " ID=\"" + SMTableapbatchentries.sremittocountry + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittocountry()) + "\""
		    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sremittocountryLength)
		    		+ " SIZE = " + "40"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sremittocountry + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsremittocountry()) + "\""
		    		+ ">"
		    		+ entry.getsremittocountry()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    	
	    	//End of remit to fields: **************************************************************
    	}

    	s += "</TABLE>\n";
    	
    	s += "<BR>\n";
    	
    	//If we are returning from applying or unapplying a line, return to this spot:
    	if (clsManageRequestParameters.get_Request_Parameter(RETURN_TO_TABLES_BOOKMARK, sm.getRequest()).compareToIgnoreCase("") != 0){
    		s += "<div name=\"" + BOOKMARK_TOP_OF_TABLES + "\"" + "id=\"" + BOOKMARK_TOP_OF_TABLES + "\"" + " ></div>  \n";
    	}
    	
    	if (bEditable){
    		s += createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton();
    		if (
    			(entry.getslid().compareToIgnoreCase("-1") != 0)
    			&& (entry.getslid().compareToIgnoreCase("0") != 0)
    			//Don't want a 'Print check' button on apply-tos:
    			&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
    		){
    			s += "&nbsp;" + createPrintCheckButton();
    		}
    		
    		s += "\n";
    	}
    	
    	if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT){
    		s += clsServletUtilities.createHTMLComment("Start the details table here.");
    		try {
				s += buildPaymentTables(bEditable, entry, sm);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1489683067] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
    	}
    	if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
    		s += clsServletUtilities.createHTMLComment("Start the details table here.");
    		try {
				s += buildApplyToTables(bEditable, entry, sm);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1489683067] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
    	}
    	
    	if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT){
    		s += clsServletUtilities.createHTMLComment("Start the details table here.");
    		try {
				s += buildMiscPaymentDetailsTable(bEditable, entry, sm);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1489683167] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
    	}
    	
    	if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
    		s += clsServletUtilities.createHTMLComment("Start the details table here.");
    		try {
				s += buildPrePaymentDetailsTable(bEditable, entry, sm);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1489683167] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
    	}
    	
		return s;
	}
	
	private String buildPrePaymentDetailsTable(boolean bEditable, APBatchEntry entry, SMMasterEditEntry smmastereditentry) throws Exception{
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Line&nbsp;#</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Apply-to&nbsp;Doc&nbsp;Type</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Apply-to&nbsp;Document</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Amount</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Description</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Comment</TD>\n";
		
		if (bEditable){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";
		}	
		s += "  </TR>\n";
		
		//Load the lines for the current entry:
		String sBackgroundColor = "";
		int iLineCounter = 0;
		//System.out.println("[1490927363] entry.getLineArray().size(): " + entry.getLineArray().size());
		for (int i = 0; i < entry.getLineArray().size(); i++){
			String sLineText=  "";
			APBatchEntryLine line = entry.getLineArray().get(i);
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE;
			if ((iLineCounter % 2) == 1){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
			}
			sLineText += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
			if (bEditable){
				//Store the unseen fields for the lines here:
				sLineText += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
			    	+ ">"
			    	+ "\n"
			    ;
			}
			
			//Line number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ line.getslinenumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			//Apply-to doc type:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<SELECT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.iapplytodoctype + "\""
					+ " onchange=\"flagDirty();\"";
				sLineText += "<OPTION VALUE = \"" + " " + "\" > ** Select code **\n";
				for (int iApplyToTypeCounter = 0; iApplyToTypeCounter <= SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_ORDERNUMBER; iApplyToTypeCounter++){
					sLineText += "<OPTION";
					if (Integer.toString(iApplyToTypeCounter).compareTo(line.getsiapplytodoctype()) == 0){
						sLineText += " selected=yes";
					}
					sLineText += " VALUE=\"" + Integer.toString(iApplyToTypeCounter) + "\">" + SMTableapbatchentrylines.getApplyToDocumentTypeLabel(iApplyToTypeCounter) + "\n";
				}
				sLineText += "</SELECT>";
			}else{
				sLineText += clsStringFunctions.filter(SMTableapbatchentrylines.getApplyToDocumentTypeLabel(Integer.parseInt(line.getsiapplytodoctype())));
			}
			sLineText += "</TD>\n";
			
			//Apply-to doc number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(line.getsapplytodocnumber()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sapplytodocnumberLength)
				    + " SIZE = " + "25"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(clsStringFunctions.filter(line.getsapplytodocnumber()))
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(line.getsapplytodocnumber()) + "\""
			    	+ ">"
				;
			}
			
			//Add the apply-to doc ID here so we can keep re-saving it:
			sLineText += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lapplytodocid + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lapplytodocid + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(line.getslapplytodocid()) + "\""
			   	+ ">\n"
			;
			
			sLineText += "</TD>\n";
			
			//Amount:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()) + "\""
					+ " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += 
					//SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype())))
					clsStringFunctions.filter(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", ""))))
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					//+ " VALUE=\"" + SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype()))) + "\""
					+ " VALUE=\"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()) + "\""
			    	+ ">"
				;
			}
			//We'll store the 'payable amt' here alongside the line amt:
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdpayableamountScale, new BigDecimal(line.getsbdpayableamt().replace(",", ""))
					)
				)
				+ "\""
		    	+ ">"
			;
			sLineText += "</TD>\n";
			
			//Line description:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdescription + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sdescriptionLength)
				    + " SIZE = " + "40"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsdescription());
			}
			sLineText += "</TD>\n";
			
			//Comment:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.scomment + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.scommentLength)
				    + " SIZE = " + "25"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getscomment());
			}
			sLineText += "</TD>\n";

			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ createRemoveLineButton(line.getslinenumber()) + "</TD>\n";
			}
			
			sLineText += "  </TR>\n";
			s += sLineText;
			iLineCounter++;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			APBatchEntryLine line = new APBatchEntryLine();
			line.setsbatchnumber(entry.getsbatchnumber());
			line.setsentrynumber(entry.getsentrynumber());
			line.setslinenumber("0");
			
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE;
			if ((iLineCounter % 2) == 1){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
			}
			s += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
			//Store the unseen fields for the lines here:
			s += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
		    	+ ">"
		    	+ "\n"
		    ;
			
			//Line number:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "(NEW)" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			//Apply to doc type:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<SELECT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.iapplytodoctype + "\""
					+ " onchange=\"flagDirty();\"";
				s += "<OPTION VALUE = \"" + " " + "\" > ** Select code **\n";
				for (int iApplyToTypeCounter = 0; iApplyToTypeCounter <= SMTableapbatchentrylines.APPLY_TO_DOC_TYPE_ORDERNUMBER; iApplyToTypeCounter++){
					s += "<OPTION";
					if (Integer.toString(iApplyToTypeCounter).compareTo(line.getsiapplytodoctype()) == 0){
						s += " selected=yes";
					}
					s += " VALUE=\"" + Integer.toString(iApplyToTypeCounter) + "\">" + SMTableapbatchentrylines.getApplyToDocumentTypeLabel(iApplyToTypeCounter) + "\n";
				}
				s += "</SELECT>"
				;
			s += "</TD>\n";
			
			//Apply-to doc:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sapplytodocnumber + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsapplytodocnumber()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sapplytodocnumberLength)
			    + " SIZE = " + "25"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Amount:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
		    	+ " VALUE=\"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "12"
			    + " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";			
			
			//Line description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sdescription + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sdescriptionLength)
			    + " SIZE = " + "40"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
						
			//Comment:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.scomment + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.scommentLength)
			    + " SIZE = " + "25"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "&nbsp;" + "</TD>\n";
			
			s += "  </TR>\n";
		}
		
		s += "</TABLE>\n";
		
		return s;
	}
	private String buildMiscPaymentDetailsTable(boolean bEditable, APBatchEntry entry, SMMasterEditEntry smmastereditentry) throws Exception{
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
		if (bEditable){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Line&nbsp;#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Distribution<BR>Acct</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Distribution&nbsp;Code</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Amount</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Description</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Comment</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Line&nbsp;#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Distribution&nbsp;Acct</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Distribution&nbsp;Code</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Amount</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Description</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Comment</TD>\n";
		}
	
		s += "  </TR>\n";
		
		//Load the array of distribution code names:
		//ArrayList<String>arrDistCodeIDs = new ArrayList<String>(0);
		ArrayList<String>arrDistCodeNames = new ArrayList<String>(0);
		
		String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
			+ " ORDER BY " + SMTableapdistributioncodes.sdistcodename
		;
		try {
			ResultSet rsDistCodes = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".displayDetailLines - Distribution codes - user: " 
				+ smmastereditentry.getUserID())
				+ " - "
				+ smmastereditentry.getFullUserName()
			);
			while(rsDistCodes.next()){
				//arrDistCodeIDs.add(rsDistCodes.getString(SMTableapdistributioncodes.lid));
				arrDistCodeNames.add(rsDistCodes.getString(SMTableapdistributioncodes.sdistcodename));
			}
			rsDistCodes.close();
		} catch (Exception e) {
			throw new Exception("Error [1494448822] reading distribution codes - " + e.getMessage());
		}
		
		//Variables for carrying the GL distribution accounts:
		ArrayList<String> arrGLAccts = new ArrayList<String>(0);
		ArrayList<String> arrGLDescriptions = new ArrayList<String>(0);
		SQL = "SELECT"
				+ " " + SMTableglaccounts.sAcctID
				+ ", " + SMTableglaccounts.sDesc
				+ " FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.lActive + " = 1)"
				+ ")"
				+ " ORDER BY " + SMTableglaccounts.sAcctID
			;
		//First, add a blank item so we can be sure the user chose one:
		arrGLAccts.add("");
		arrGLDescriptions.add("*** Select GL ***");
		try {
			ResultSet rsGLs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smmastereditentry.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + smmastereditentry.getUserID()
					+ " - "
					+ smmastereditentry.getFullUserName()
					);
			while (rsGLs.next()) {
				arrGLAccts.add(rsGLs.getString(SMTableglaccounts.sAcctID));
				arrGLDescriptions.add(
						rsGLs.getString(SMTableglaccounts.sAcctID)
					+ " - "
					+ rsGLs.getString(SMTableglaccounts.sDesc)
				);
			}
			rsGLs.close();
		} catch (SQLException e) {
			s += "<B>Error [1494448823] reading GL info - " + e.getMessage() + "</B><BR>";
		}
		
		//Load the lines for the current entry:
		String sBackgroundColor = "";
		int iLineCounter = 0;
		//System.out.println("[1490927363] entry.getLineArray().size(): " + entry.getLineArray().size());
		for (int i = 0; i < entry.getLineArray().size(); i++){
			String sLineText = "";
			APBatchEntryLine line = entry.getLineArray().get(i);
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE;
			if ((iLineCounter % 2) == 1){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
			}
			sLineText += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
			if (bEditable){
				//Store the unseen fields for the lines here:
				sLineText += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
			    	+ ">"
			    	+ "\n"
			    ;
			}
			
			//Line number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ line.getslinenumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// Distribution acct:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText  += "\n<SELECT NAME = \"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct + "\""
						+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct + "\""
						+ " onchange=\"flagDirty();\""
						 + " >\n"
					;
				
				String sGLAcctSelections = "";
				for (int j = 0; j < arrGLAccts.size(); j++){
					sGLAcctSelections += "<OPTION";
					if (arrGLAccts.get(j).toString().compareTo(line.getsdistributionacct()) == 0){
						sGLAcctSelections += " selected=yes";
					}
					sGLAcctSelections += " VALUE=\"" + arrGLAccts.get(j).toString() + "\">" + arrGLDescriptions.get(j).toString() + "\n";
				}	
				sLineText += sGLAcctSelections;
				sLineText += "</SELECT>";
			}else{
				sLineText += clsStringFunctions.filter(line.getsdistributionacct());
			}
			sLineText += "</TD>\n";
			
			// Distribution code name:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<SELECT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdistributioncodename + "\""
					+ " onchange=\"distCodeChange(this, " 
						+ "'"
						+ APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct
						+ "'"
				+ ");\""
						+ " >\n"
						;
				sLineText += "<OPTION VALUE = \"" + " " + "\" > ** Select code **\n";
				for (int iDistCodeCounter = 0; iDistCodeCounter < arrDistCodeNames.size(); iDistCodeCounter++){
					sLineText += "<OPTION";
					if (arrDistCodeNames.get(iDistCodeCounter).toString().compareTo(line.getsdistributioncodename()) == 0){
						sLineText += " selected=yes";
					}
					sLineText += " VALUE=\"" + arrDistCodeNames.get(iDistCodeCounter).toString() + "\">" + arrDistCodeNames.get(iDistCodeCounter).toString() + "\n";
				}
				sLineText += "</SELECT>"
				;

			}else{
				sLineText += clsStringFunctions.filter(line.getsdistributioncodename());
			}
			sLineText += "</TD>\n";
			
			//Amount:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					//+ " VALUE=\"" + SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype())) + "\""
					
					+ " VALUE=\"" + clsStringFunctions.filter(
						clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()
						)
					)
					+ "\""
					
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += 
					//SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype())))
					clsStringFunctions.filter(
						clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()
						)
					)
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					//+ " VALUE=\"" + SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype()))) + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(
						clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()
						)
					)
					+ "\""
					
			    	+ ">"
				;
			}
			
			//We'll store the 'payable amt' here alongside the line amt:
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdpayableamountScale, new BigDecimal(line.getsbdpayableamt().replace(",", ""))
					)
				)
				+ "\""
		    	+ ">"
			;
			sLineText += "</TD>\n";
			
			//Line description:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdescription + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sdescriptionLength)
				    + " SIZE = " + "40"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsdescription());
			}
			sLineText += "</TD>\n";
			
			//Comment:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.scomment + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.scommentLength)
				    + " SIZE = " + "25"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getscomment());
			}
			sLineText += "</TD>\n";

			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ createRemoveLineButton(line.getslinenumber()) + "</TD>\n";
			}
			
			sLineText += "  </TR>\n";
			s += sLineText;
			iLineCounter++;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			APBatchEntryLine line = new APBatchEntryLine();
			line.setsbatchnumber(entry.getsbatchnumber());
			line.setsentrynumber(entry.getsentrynumber());
			line.setslinenumber("0");
			
			sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE;
			if ((iLineCounter % 2) == 1){
				sBackgroundColor = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
			}
			s += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
			//Store the unseen fields for the lines here:
			s += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
		    	+ ">"
		    	+ "\n"
		    ;
			
			//Line number:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "(NEW)" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// Distribution acct:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			
			s  += "<SELECT NAME = \"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sdistributionacct + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sdistributionacct + "\""
				+ " onchange=\"flagDirty();\""
				 + " >\n"
			;
			for (int i = 0; i < arrGLAccts.size(); i++){
				s += "<OPTION";
				if (arrGLAccts.get(i).toString().compareTo(line.getsdistributionacct()) == 0){
					s += " selected=yes";
				}
				s += " VALUE=\"" + arrGLAccts.get(i).toString() + "\">" + arrGLDescriptions.get(i).toString() + "\n";
			}
			s += "</SELECT>"
			;

			s += "</TD>\n";
			
			// Distribution code name:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<SELECT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdistributioncodename + "\""
						//+ " onchange=\"flagDirty();\""
						+ " onchange=\"distCodeChange(this, " 
						+ "'"
						+ APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct
						+ "'"
						+ ");\""
						+ " >\n"
						;
				s += "<OPTION VALUE = \"" + " " + "\" > ** Select code **\n";
				for (int iDistCodeCounter = 0; iDistCodeCounter < arrDistCodeNames.size(); iDistCodeCounter++){
					s += "<OPTION";
					if (arrDistCodeNames.get(iDistCodeCounter).toString().compareTo(line.getsdistributioncodename()) == 0){
						s += " selected=yes";
					}
					s += " VALUE=\"" + arrDistCodeNames.get(iDistCodeCounter).toString() + "\">" + arrDistCodeNames.get(iDistCodeCounter).toString() + "\n";
				}
				s += "</SELECT>"
				;
			s += "</TD>\n";	
			//Amount:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
		    	//+ " VALUE=\"" + SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype()))) + "\""
				+ " VALUE=\""
		    		+ clsStringFunctions.filter(
						clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()
						)
					)
		    	+ "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "12"
			    + " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			
			//Line description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sdescription + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sdescriptionLength)
			    + " SIZE = " + "40"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
						
			//Comment:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.scomment + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.scommentLength)
			    + " SIZE = " + "25"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
	
			s += "</TD>\n";
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ "&nbsp;" + "</TD>\n";
			
			s += "  </TR>\n";
		}
		
		s += "</TABLE>\n";
		
		return s;
	}
	private String buildPaymentTables(boolean bEditable, APBatchEntry entry, SMMasterEditEntry sm) throws Exception{
		String s = "";
    	s += clsServletUtilities.createHTMLComment("This table contains both the 'open invoices' table and the 'applied' table:");
    	
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
				+ ">\n";
    	
		s += "  <TR>";
		
    	s += clsServletUtilities.createHTMLComment("Start the eligible apply-to documents table here.");
    	if ((bEditable) && (entry.getsvendoracct().compareToIgnoreCase("") != 0)){
    		s += "    <TD style = \" vertical-align:top; \" >";
	    	try {
				s += displayUnappliedDocumentsForPayments(entry, sm, bEditable);
			} catch (Exception e) {
				s += "  <TR>"
					+ "    <TD><FONT COLOR=RED><B>Error [1489683067] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
					+ "</TR>\n"
				;
			}
	    	s += "    </TD>";
    	}
    	
    	s += clsServletUtilities.createHTMLComment("Start the applied documents table here.");
    	s += "    <TD style = \" vertical-align:top; \" >";
    	try {
			s += displayDetailLinesForPayments(entry, bEditable, sm);
		} catch (Exception e) {
			s += "  <TR>"
				+ "    <TD><FONT COLOR=RED><B>Error [1489683068] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
				+ "</TR>\n"
			;
		}
    	s += "    <TD>";
    	
    	
    	s += "  </TR>";
    	s += "</TABLE>";
    	
    	if (bEditable){
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton();
    		if (
    			(entry.getslid().compareToIgnoreCase("-1") != 0)
    			&& (entry.getslid().compareToIgnoreCase("0") != 0)
    			//Don't want a 'Print Check' button on apply-to's:
    			&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
    		){
    			s += "&nbsp;" + createPrintCheckButton();
    		}
    		s += "\n";
    	}
		return s;
	}
	private String buildApplyToTables(boolean bEditable, APBatchEntry entry, SMMasterEditEntry sm) throws Exception{
		String s = "";
    	s += clsServletUtilities.createHTMLComment("This table contains both the 'unapplied' table and the 'applied' table:");
    	
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
				+ ">\n";
    	
		s += "  <TR>";
		
    	s += clsServletUtilities.createHTMLComment("Start the eligible apply-to documents table here.");
    	if ((bEditable) && (entry.getsvendoracct().compareToIgnoreCase("") != 0)){
    		s += "    <TD style = \" vertical-align:top; \" >";
	    	try {
				s += displayUnappliedDocumentsForApplyTos(entry, sm, bEditable);
			} catch (Exception e) {
				s += "  <TR>"
					+ "    <TD><FONT COLOR=RED><B>Error [1489683167] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
					+ "</TR>\n"
				;
			}
	    	s += "    </TD>";
    	}
    	
    	s += clsServletUtilities.createHTMLComment("Start the applied documents table here.");
    	s += "    <TD style = \" vertical-align:top; \" >";
    	try {
			s += displayDetailLinesForApplyTos(entry, bEditable, sm);
		} catch (Exception e) {
			s += "  <TR>"
				+ "    <TD><FONT COLOR=RED><B>Error [1489683168] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
				+ "</TR>\n"
			;
		}
    	s += "    <TD>";
    	
    	
    	s += "  </TR>";
    	s += "</TABLE>";
    	
    	if (bEditable){
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createDeleteButton() + "\n";
    	}
		return s;
	}
	private String displayUnappliedDocumentsForApplyTos(APBatchEntry entry, SMMasterEditEntry smmastereditentry, boolean bEditable) throws Exception{
		String s = "";
		//Print headings:
		s += "<B><I><U>UNAPPLIED OPEN DOCUMENTS</U></I></B> - <I>(Click on the headings to sort)</I>\n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " ID = \"" + TABLE_UNAPPLIED_DOCUMENTS + "\""
			+ ">\n";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(0);\" >"
			+ "Doc<BR>Date</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(2);\" >"
				+ "Doc<BR>Number</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(3);\" >"
				+ "Original<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(1);\" >"
				+ "Discount<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(1);\" >"
				+ "Due<BR>Date</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(4);\" >"
			+ "Current<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(7);\" >"
				+ "On<BR>hold?</TD>\n";
		
		if (bEditable){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+ "Apply?</TD>\n";
		}
		
		s += "  </TR>\n";
		
		boolean bAllowLinkToDocument = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APControlPayments, 
				smmastereditentry.getUserID(), 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				(String) smmastereditentry.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		
		String SQL = "SELECT"
			+ " * FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
				//+ " AND (" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE) + ")"
				+ " AND (" + SMTableaptransactions.bdcurrentamt + " != 0.00)"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".displayEligibleApplyToDocuments - user: " 
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			boolean bOddRow = true;
			while (rs.next()){
				String sLineText = "";
				
				//if the document is already applied on this payment, then don't list it:
				boolean bDocumentIsAlreadyApplied = false;
				for (int i = 0; i < entry.getLineArray().size(); i++){
					APBatchEntryLine line = entry.getLineArray().get(i);
					if (line.getsapplytodocnumber().compareToIgnoreCase(rs.getString(SMTableaptransactions.sdocnumber)) == 0){
						bDocumentIsAlreadyApplied = true;
						break;
					}
				}
				if(!bDocumentIsAlreadyApplied){
					String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
					if (bOddRow){
						sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
					}
					sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
						+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
						+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
						+ ">\n";
					
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdocdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
					
					String sDocNumberLink = rs.getString(SMTableaptransactions.sdocnumber);
					if(bAllowLinkToDocument){
						sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smap.APControlPaymentsEdit"
							+ "?" + SMTableaptransactions.svendor + "=" + entry.getsvendoracct()
							+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumberLink
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
							+ "\">" + sDocNumberLink + "</A>"
						;
					}
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ sDocNumberLink
							+ "</TD>\n";
					
					String sOriginalAmt = clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt)), " ", NUMBER_PADDING_LENGTH);
					String sCurrentAmt = clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt)), " ", NUMBER_PADDING_LENGTH);
					//We'll want to make any credit balances appear RED, so we can tell which are positive and which are negative:
					if (rs.getBigDecimal(SMTableaptransactions.bdoriginalamt).compareTo(BigDecimal.ZERO) < 0){
						sOriginalAmt = "<FONT COLOR=RED>" 
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</FONT>";
					}
					if (rs.getBigDecimal(SMTableaptransactions.bdcurrentamt).compareTo(BigDecimal.ZERO) < 0){
						sCurrentAmt = "<FONT COLOR=RED>" 
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</FONT>";
					}
					
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ sOriginalAmt
							+ "</TD>\n";
	
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdiscountdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datduedate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ sCurrentAmt
							+ "</TD>\n";

					String sOnHold = "N";
					if (rs.getInt(SMTableaptransactions.ionhold) == 1){
						sOnHold = "<FONT COLOR=RED>Y</FONT>";
					}
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<B>" 
						+ sOnHold 
						+ "</B>"
						+ " <INPUT TYPE=HIDDEN"
						+ " NAME=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " ID=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " VALUE=\"" + Integer.toString(rs.getInt(SMTableaptransactions.ionhold)) + "\""
						+ "</TD>\n";
					
					if (bEditable){
						sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ createApplyLineButton(rs.getString(SMTableaptransactions.sdocnumber)) + "</TD>\n";
					}
					
					sLineText += "  </TR>\n";
					s += sLineText;
					bOddRow = !bOddRow;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1492106107] reading open invoices for vendor '" + entry.getsvendoracct() + " with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		s += "</TABLE>\n";
		return s;
	}
	private String displayUnappliedDocumentsForPayments(APBatchEntry entry, SMMasterEditEntry smmastereditentry, boolean bEditable) throws Exception{
		String s = "";
		//Print headings:
		s += "<B><I><U>UNAPPLIED INVOICES</U></I></B> - <I>(Click on the headings to sort)</I>\n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " ID = \"" + TABLE_UNAPPLIED_DOCUMENTS + "\""
			+ ">\n";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(0);\" >"
			+ "Doc<BR>Date</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(1);\" >"
				+ "Due<BR>Date</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(2);\" >"
				+ "Doc<BR>Number</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(3);\" >"
			+ "Original<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(4);\" >"
			+ "Current<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(5);\" >"
				+ "Discount<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(6);\" >"
				+ "Discount<BR>available</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \"  style = \"cursor: pointer;\" onclick=\"sortUnappliedTable(7);\" >"
				+ "On<BR>hold?</TD>\n";
		
		if (bEditable){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+ "Apply?</TD>\n";
		}
		
		s += "  </TR>\n";
		
		boolean bAllowLinkToDocument = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.APControlPayments, 
			smmastereditentry.getUserID(), 
			getServletContext(), 
			smmastereditentry.getsDBID(), 
			(String) smmastereditentry.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		String SQL = "SELECT"
			+ " * FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
				+ " AND (" 
					+ "(" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE) + ")"
					+ " OR (" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE) + ")"
					+ " OR (" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE) + ")"
				+ ")" //End outer 'AND'
				+ " AND (" + SMTableaptransactions.bdcurrentamt + " != 0.00)"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".displayEligibleApplyToDocuments - user: " 
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			boolean bOddRow = true;
			while (rs.next()){
				String sLineText = "";
				
				//if the document is already applied on this payment, then don't list it:
				boolean bDocumentIsAlreadyApplied = false;
				for (int i = 0; i < entry.getLineArray().size(); i++){
					APBatchEntryLine line = entry.getLineArray().get(i);
					if (line.getsapplytodocnumber().compareToIgnoreCase(rs.getString(SMTableaptransactions.sdocnumber)) == 0){
						bDocumentIsAlreadyApplied = true;
						break;
					}
				}
				if(!bDocumentIsAlreadyApplied){
					String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
					if (bOddRow){
						sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
					}
					sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
						+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
						+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
						+ ">\n";
					
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdocdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datduedate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					String sDocNumberLink = rs.getString(SMTableaptransactions.sdocnumber);
					if(bAllowLinkToDocument){
						sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smap.APControlPaymentsEdit"
							+ "?" + SMTableaptransactions.svendor + "=" + entry.getsvendoracct()
							+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumberLink
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
							+ "\">" + sDocNumberLink + "</A>"
						;
					}
					
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ sDocNumberLink
							+ "</TD>\n";
					
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";
	
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";

					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdiscountdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";

					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";
					
					String sOnHold = "N";
					if (rs.getInt(SMTableaptransactions.ionhold) == 1){
						sOnHold = "<FONT COLOR=RED>Y</FONT>";
					}
					sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<B>" 
						+ sOnHold 
						+ "</B>"
						+ " <INPUT TYPE=HIDDEN"
						+ " NAME=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " ID=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " VALUE=\"" + Integer.toString(rs.getInt(SMTableaptransactions.ionhold)) + "\""
						+ "</TD>\n";
					
					if (bEditable){
						sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ createApplyLineButton(rs.getString(SMTableaptransactions.sdocnumber)) + "</TD>\n";
					}
					
					sLineText += "  </TR>\n";
					s += sLineText;
					bOddRow = !bOddRow;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1492106107] reading open invoices for vendor '" + entry.getsvendoracct() + " with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		s += "</TABLE>\n";
		return s;
	}
	private String displayDetailLinesForPayments(APBatchEntry entry, boolean bEditable, SMMasterEditEntry smmastereditentry) throws Exception{
		String s = "";
		s += "<B><I><U>APPLIED INVOICES</U></I></B>\n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " ID = \"" + TABLE_APPLIED_DOCUMENTS + "\""
			+ ">\n";
		
		//Header row:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Line&nbsp;#</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Doc<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Doc<BR>Number</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Original<BR>Amt</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Discount<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Due<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Current<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Applied<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Discount<BR>Available</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Discount<BR>Applied</TD>\n";

		if (bEditable){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";
		}
		
		s += "  </TR>\n";
		
		//Load the lines for the current entry:
		//System.out.println("[1490927363] entry.getLineArray().size(): " + entry.getLineArray().size());
		boolean bOddRow = true;
		boolean bAllowLinkToDocument = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APControlPayments, 
				smmastereditentry.getUserID(), 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				(String) smmastereditentry.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		for (int i = 0; i < entry.getLineArray().size(); i++){
			String sLineText = "";
			
			APBatchEntryLine line = entry.getLineArray().get(i);
			
			//Read the AP transaction to get the date values, etc:
			String sDocDate = "N/A";
			String sDueDate = "N/A";
			String sOriginalAmt = "N/A";
			String sCurrentAmt = "N/A";
			String sDiscountAvailable = "N/A";
			String sDiscountDate = "N/A";
			String SQL = "SELECT"
				+ " * FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
					+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
				+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, getServletContext(),
				smmastereditentry.getsDBID(),
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".displayDetailLines - user: " 
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			if (rs.next()){
				sDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sDiscountDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sOriginalAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt));
				sCurrentAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt));
				sDiscountAvailable = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable));
			}else{
				
			}
			String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n";
			
			if (bEditable){
				//Store the unseen fields for the lines here:
				sLineText += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
			    	+ ">"
			    	+ "\n"
			    ;
			}
			
			//Line number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ line.getslinenumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			//Doc date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDocDate;
			sLineText += "</TD>\n";
			
			//Doc Number:
			String sDocNumberLink = rs.getString(SMTableaptransactions.sdocnumber);
			if(bAllowLinkToDocument){
				sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smap.APControlPaymentsEdit"
					+ "?" + SMTableaptransactions.svendor + "=" + entry.getsvendoracct()
					+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumberLink
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
					+ "\">" + sDocNumberLink + "</A>"
				;
			}
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDocNumberLink;
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(line.getsapplytodocnumber()) + "\""
	    		+ ">"
			;
			sLineText += "</TD>\n";

			//Original Amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sOriginalAmt, " ", NUMBER_PADDING_LENGTH);
			sLineText += "</TD>\n";

			// Discount date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDiscountDate;
			sLineText += "</TD>\n";

			// Due date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDueDate;
			sLineText += "</TD>\n";
			
			//Current amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sCurrentAmt, " ", NUMBER_PADDING_LENGTH);
			sLineText += "</TD>\n";

			//Amt applied:
			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdamount + "\""
					
					+ " VALUE=\"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()) + "\""
						
					//+ " VALUE=\"" + APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype()) + "\""
					+ " SIZE = " + "10"
		    		+ " onchange=\"updateLineTotal();\""
		    		+ ">"
					+ "</TD>\n";
			}else{
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
				sLineText += clsStringFunctions.PadLeft(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, new BigDecimal(line.getsbdamount().replace(",", "")).negate()), " ", NUMBER_PADDING_LENGTH);
				sLineText += "</TD>\n";
			}
			
			//We'll store the 'payable amt' here alongside the line amt:
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				//+ " VALUE=\"" + SMUtilities.filter(SMUtilities.filter(APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbdamount(), entry.getientrytype()))) + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdpayableamountScale, new BigDecimal(line.getsbdpayableamt().replace(",", ""))
					)
				)
				+ "\""
		    	+ ">"
			;
			
			//Discount available:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sDiscountAvailable, " ", NUMBER_PADDING_LENGTH);
			sLineText += "</TD>\n";
			
			//Discount applied:
			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdapplieddiscountamt + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdapplieddiscountamt + "\""
					//+ " VALUE=\"" + APBatchEntry.displayAbsoluteValueForInputScreen(line.getsbddiscountappliedamt(), entry.getientrytype()) + "\""
					+ " VALUE=\"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdapplieddiscountamtScale, new BigDecimal(line.getsbddiscountappliedamt().replace(",", "")).negate()) + "\""
						
					+ " SIZE = " + "10"
		    		+ ">"
					+ "</TD>\n";
			}else{
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
				sLineText += clsStringFunctions.PadLeft(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdapplieddiscountamtScale, new BigDecimal(line.getsbddiscountappliedamt().replace(",", "")).negate()),
					" ", NUMBER_PADDING_LENGTH);
				sLineText += "</TD>\n";
			}
			
			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ createUnapplyLineButton(line.getslinenumber()) + "</TD>\n";

			}
			sLineText += "  </TR>\n";
			s += sLineText;
			bOddRow = !bOddRow;
		}
		s += "</TABLE>\n";
		return s;
	}
	private String displayDetailLinesForApplyTos(APBatchEntry entry, boolean bEditable, SMMasterEditEntry smmastereditentry) throws Exception{
		String s = "";
		s += "<B><I><U>APPLIED INVOICES</U></I></B>\n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " ID = \"" + TABLE_APPLIED_DOCUMENTS + "\""
			+ ">\n";
		
		//Header row:
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Line&nbsp;#</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Doc<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Doc<BR>Number</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Original<BR>Amt</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Discount<BR>Date</TD>\n";

		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Due<BR>Date</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Current<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Applied<BR>Amt</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Net<BR>Amt</TD>\n";
		
		if (bEditable){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";
		}
		
		s += "  </TR>\n";
		
		//Load the lines for the current entry:
		//System.out.println("[1490927363] entry.getLineArray().size(): " + entry.getLineArray().size());
		boolean bOddRow = true;
		boolean bAllowLinkToDocument = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APControlPayments, 
				smmastereditentry.getUserID(), 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				(String) smmastereditentry.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		for (int i = 0; i < entry.getLineArray().size(); i++){
			String sLineText = "";
			
			APBatchEntryLine line = entry.getLineArray().get(i);
			
			//Read the AP transaction to get the date values, etc:
			String sDocDate = "N/A";
			String sDueDate = "N/A";
			String sOriginalAmt = "N/A";
			String sCurrentAmt = "N/A";
			String sNetAmt = "N/A";
			String sDiscountDate = "N/A";
			String SQL = "SELECT"
				+ " * FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
					+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
				+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, getServletContext(),
				smmastereditentry.getsDBID(),
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".displayDetailLines - user: " 
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			if (rs.next()){
				sDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sDiscountDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sOriginalAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt));
				sCurrentAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt));
				
				BigDecimal bdAppliedAmt = new BigDecimal("0.00");
				try {
					//TESTINGAPPLYTOS
					bdAppliedAmt = new BigDecimal(line.getsbdamount().trim().replace(",", "")).negate();
				} catch (Exception e) {
					bdAppliedAmt = new BigDecimal("0.00");
				}
				sNetAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt).add(bdAppliedAmt));
			}else{
				
			}
			String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n";
			
			if (bEditable){
				//Store the unseen fields for the lines here:
				sLineText += "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
			    	+ ">"
			    	+ "\n"
			    ;
			}
			
			//Line number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ line.getslinenumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			//Doc date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDocDate;
			sLineText += "</TD>\n";

			//Doc Number:
			String sDocNumberLink = rs.getString(SMTableaptransactions.sdocnumber);
			if(bAllowLinkToDocument){
				sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smap.APControlPaymentsEdit"
					+ "?" + SMTableaptransactions.svendor + "=" + entry.getsvendoracct()
					+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumberLink
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
					+ "\">" + sDocNumberLink + "</A>"
				;
			}
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDocNumberLink;
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(line.getsapplytodocnumber()) + "\""
	    		+ ">"
			;
			sLineText += "</TD>\n";

			//Original Amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sOriginalAmt, " ", NUMBER_PADDING_LENGTH);
			sLineText += "</TD>\n";

			// Discount date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDiscountDate;
			sLineText += "</TD>\n";

			// Due date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sDueDate;
			sLineText += "</TD>\n";
			
			//Current amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sCurrentAmt, " ", NUMBER_PADDING_LENGTH)				
				+ " <INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableaptransactions.bdcurrentamt + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableaptransactions.bdcurrentamt + "\""
				+ " VALUE=\"" + sCurrentAmt + "\""
				+ ">"
			;
			sLineText += "</TD>\n";

			//Amt applied:
			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + line.getsbdamount() + "\""
					+ " SIZE = " + "10"
		    		+ " onchange=\"updateLineTotal();\""
		    		+ ">"
					+ "</TD>\n";
			}else{
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
				sLineText += clsStringFunctions.PadLeft(line.getsbdamount(), " ", NUMBER_PADDING_LENGTH);
				sLineText += "</TD>\n";
			}
			
			//Net:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += " <INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ NET_AMT_FIELD_NAME + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ NET_AMT_FIELD_NAME + "\""
				+ " VALUE=\"" + clsStringFunctions.PadLeft(sNetAmt, " ", NUMBER_PADDING_LENGTH) + "\""
				+ " readonly"
				+ " SIZE = " + "10"
				+ ">"
			;
			//We'll store the 'payable amt' here alongside the line amt:
			sLineText += "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdpayableamount + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableapbatchentrylines.bdpayableamountScale, new BigDecimal(line.getsbdpayableamt().replace(",", ""))
					)
				)
				+ "\""
		    	+ ">"
			;
			sLineText += "</TD>\n";
			
			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ createUnapplyLineButton(line.getslinenumber()) + "</TD>\n";

			}
			sLineText += "  </TR>\n";
			s += sLineText;
			bOddRow = !bOddRow;
		}
		s += "</TABLE>\n";
		return s;
	}

	private String sCommandScript(String sDBID, APBatchEntry entry, boolean bEditable, SMMasterEditEntry sm) throws Exception{
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		s += "window.onload = function() {\n"
			+ "    initShortcuts();\n"
			+ "    calculatelinetotal();\n"
			+ "    jumpToBookmark('" + BOOKMARK_TOP_OF_TABLES + "'); \n"
			+ "    toggleCheckNumber();\n"  //Set the initial visibility of the check number field, depending on the 'print checks' state
			//+ "    configAppliedTable();\n"
			+ "}\n"
		;
		
		s += "function jumpToBookmark(bookmarkID){ \n"
			+ "    var objTopOfTablesBookmark = document.getElementById(bookmarkID); \n"
			+ "    if (objTopOfTablesBookmark != null){ \n"
			+ "        var top = document.getElementById(bookmarkID).offsetTop; \n"
			+ "        window.scrollTo(0, top); \n"
			+ "    } \n"
			+ "} \n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";
		
		//If this is a 'misc payment', then we'll need to load the distribution codes:
		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT){
			//Load the distribution code GL accounts:
			int iCounter = 0;
			
			String sdistributioncodeaccounts = "";
			
			//Add one array item for the 'blank' distribution code, if someone doesn't pick one:
			sdistributioncodeaccounts += "sdistributioncodeaccounts[\"" + " " + "\"] = \"" + "" + "\";\n";
			
			String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
				+ " ORDER BY " + SMTableapdistributioncodes.sdistcodename
			;
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + " loading dist code accts [1490748349] SQL: " + SQL 
				);
				while (rs.next()){
					iCounter++;
					sdistributioncodeaccounts += "sdistributioncodeaccounts[\"" + rs.getString(SMTableapdistributioncodes.sdistcodename).trim() + "\"] = \"" + rs.getString(SMTableapdistributioncodes.sglacct).trim().replace("\"", "'") + "\";\n";
				}
				rs.close();
			} catch (SQLException e) {
				throw new SQLException("Error reading [1490748350] dist code accts for javascript - " + e.getMessage());
			}
			
			//Create the arrays, if there are any:
			if (iCounter > 0){
				s += "var sdistributioncodeaccounts = new Array(" + Integer.toString(iCounter) + ")\n";
				s += sdistributioncodeaccounts + "\n";
			}
			
			s += "\n";
			
		}
		if ((entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
			|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)){
			s += "function removeLine(sLineNumber){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before removing a line.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    if (confirm('Are you sure you want to remove line number ' + sLineNumber + '?')){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_REMOVELINE + "\";\n"
				+ "        document.getElementById(\"" + LINE_NUMBER_TO_DELETE_PARAM + "\").value = sLineNumber;\n"
				+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "    }\n"
				+ "}\n"
			;
		}
		
		//Load the remit to codes and info, if there is any:
		//************************************************************************************************
		int iRemitToCounter = 0;
		
		String sremittonames = "";
		String sremittoaddressline1s = "";
		String sremittoaddressline2s = "";
		String sremittoaddressline3s = "";
		String sremittoaddressline4s = "";
		String sremittocities = "";
		String sremittostates = "";
		String sremittopostalcodes = "";
		String sremittocountries = "";
		
		//Add one array item for the 'blank' distribution code, if someone doesn't pick one:
		sremittonames += "sremittonames[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittoaddressline1s += "sremittoaddressline1s[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittoaddressline2s += "sremittoaddressline2s[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittoaddressline3s += "sremittoaddressline3s[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittoaddressline4s += "sremittoaddressline4s[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittocities += "sremittocities[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittostates += "sremittostates[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittopostalcodes += "sremittopostalcodes[\"" + " " + "\"] = \"" + "" + "\";\n";
		sremittocountries += "sremittocountries[\"" + " " + "\"] = \"" + "" + "\";\n";
		
		
		String SQL = "SELECT *"
				+ " FROM " + SMTableapvendorremittolocations.TableName
				+ " WHERE ("
					+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + entry.getsvendoracct() + "')"
				+ ")"
				+ " ORDER BY " + SMTableapvendorremittolocations.sremittocode
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + " loading vendor remit to data [1491934218] SQL: " + SQL 
			);
			while (rs.next()){
				iRemitToCounter++;
				sremittonames += "sremittonames[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
					+ rs.getString(SMTableapvendorremittolocations.sremittoname).trim().replace("\"", "'") + "\";\n";
				sremittoaddressline1s += "sremittoaddressline1s[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
					+ rs.getString(SMTableapvendorremittolocations.saddressline1).trim().replace("\"", "'") + "\";\n";
				sremittoaddressline2s += "sremittoaddressline2s[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.saddressline2).trim().replace("\"", "'") + "\";\n";
				sremittoaddressline3s += "sremittoaddressline3s[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.saddressline3).trim().replace("\"", "'") + "\";\n";
				sremittoaddressline4s += "sremittoaddressline4s[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.saddressline4).trim().replace("\"", "'") + "\";\n";
				sremittocities += "sremittocities[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.scity).trim().replace("\"", "'") + "\";\n";
				sremittostates += "sremittostates[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.sstate).trim().replace("\"", "'") + "\";\n";
				sremittopostalcodes += "sremittopostalcodes[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.spostalcode).trim().replace("\"", "'") + "\";\n";
				sremittocountries += "sremittocountries[\"" + rs.getString(SMTableapvendorremittolocations.sremittocode).trim() + "\"] = \"" 
						+ rs.getString(SMTableapvendorremittolocations.scountry).trim().replace("\"", "'") + "\";\n";
				
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading [14919342189] remit to records for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iRemitToCounter > 0){
			s += "var sremittonames = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittonames + "\n";
			s += "var sremittoaddressline1s = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittoaddressline1s + "\n";
			s += "var sremittoaddressline2s = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittoaddressline2s + "\n";
			s += "var sremittoaddressline3s = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittoaddressline3s + "\n";
			s += "var sremittoaddressline4s = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittoaddressline4s + "\n";
			s += "var sremittocities = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittocities + "\n";
			s += "var sremittostates = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittostates + "\n";
			s += "var sremittopostalcodes = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittopostalcodes + "\n";
			s += "var sremittocountries = new Array(" + Integer.toString(iRemitToCounter) + ")\n";
			s += sremittocountries + "\n";
		}
		
		s += "\n";
		
		//************************************************************************************************
		
		s += "function promptToSave(){\n"		
			//Don't prompt on these functions:
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_UPDATEVENDORINFO + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			//If the record WAS changed, then
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				//If is was anything but the 'SAVE' command that triggered this function...
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ COMMAND_VALUE_SAVE + "\" ){\n"
						//Prompt to see if the user wants to continue
			+ "        return \"You have unsaved changes\";\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
					
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		s += "function saveandadd(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE_AND_ADD + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;

		s += "function deleteentry(){\n"
			+ "    if (confirm('Are you sure you want to delete this " + SMTableapbatchentries.getDocumentTypeLabel(entry.getientrytype()) + "?')){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function printcheck(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_PRINT_CHECKS + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
		
		//Find Vendor:
		 s += "function findVendor(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
		    + COMMAND_VALUE_FINDVENDOR + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n";
		
		//Update vendor info:
		 s += "function updateVendorInfo(){\n"
			+ "    clearVendorFields(); \n" 
			+ "    flagDirty(); \n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
		    + COMMAND_VALUE_UPDATEVENDORINFO + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n";
		 
		s += "function clearVendorFields(){ \n"
			+ "    //Clear all the associated vendor fields if the user makes a change  to the vendor: \n"
			+ "    document.getElementById(\"" + SMTableapbatchentries.svendorname + "\").value = ''; \n"
		;
		if (
			(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
			|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
		){
			s += "    document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline1 + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline2 + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline3 + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline4 + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittocity + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittocode + "\").selectedIndex = 0; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittocountry + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittoname + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittopostalcode + "\").value = ''; \n"
				+ "    document.getElementById(\"" + SMTableapbatchentries.sremittostate + "\").value = ''; \n"
			;
		}
			s += "    flagDirty(); \n"
				+ "} \n"
		;
			
		//Functions for updating the GL acct based on the Distribution code:
		s += "function distCodeChange(selectObj, targetcontrolname) {\n" 
			// get the index of the selected option 
			+ "    var idx = selectObj.selectedIndex;\n"
			// get the value of the selected option 
			+ "    var which = selectObj.options[idx].value;\n"
			+ "    if (which != '' && which != '0'){\n"
			+ "        document.getElementById(targetcontrolname).value = sdistributioncodeaccounts[which];\n"
			+ "    }\n"
			+ "    flagDirty();\n"
			+ "}\n\n"
		; 
			
		 //Unapply line:
		 s += "function unapplyLine(linenumber){ \n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \""
				+ COMMAND_VALUE_UNAPPLYLINE + "\";\n"
			+ "    document.getElementById(\"" + LINE_NUMBER_TO_UNAPPLY_PARAM + "\").value = linenumber; \n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "} \n"
		;
		 
		 //Apply line:
		 s += "function applyLine(docnumber){ \n"
			//First, find out if the document we are applying to is ON HOLD:
			+ "    if (document.getElementById(docnumber + '" + SMTableaptransactions.ionhold + "').value == '1'){ \n"
			+ "        if (!confirm('This document is on hold.  Are you sure you want to apply a payment to it?')){ \n"
			+ "            return; \n"
			+ "        } \n"
			+ "    } \n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \""
				+ COMMAND_VALUE_APPLYTODOC + "\";\n"
			+ "    document.getElementById(\"" + APPLYTODOCNUMBER_TO_APPLY_PARAM + "\").value = docnumber; \n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "} \n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
		//Populate the remit-to fields:
		s += "function remitToChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != '' && which != '0'){\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittoname + "\").value = sremittonames[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline1 + "\").value = sremittoaddressline1s[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline2 + "\").value = sremittoaddressline2s[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline3 + "\").value = sremittoaddressline3s[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittoaddressline4 + "\").value = sremittoaddressline4s[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittocity + "\").value = sremittocities[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittostate + "\").value = sremittostates[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittopostalcode + "\").value = sremittopostalcodes[which];\n"
		+ "        document.getElementById(\"" + SMTableapbatchentries.sremittocountry + "\").value = sremittocountries[which];\n"
		+ "    }\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 
		
		s += "\n";
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		//Functions for highlighting the table rows:
		s += "function configUnappliedTable(){\n"
			+ "    var unappliedTable = document.getElementById(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\");\n"
			+ "    if (unappliedTable != null) {\n"
			+ "        var oddrow = false;\n"
			+ "        var backgroundcolor = \"\"; \n"
			+ "        for (var i = 1; i < unappliedTable.rows.length; i++) {\n"
			//+ "            tbl.rows[i].style.cursor = \"pointer\";\n"
			+ "            if (oddrow){\n"
			//+ "                unappliedTable.rows[i].style.background = \"" + TABLE_ROW_ODD_ROW_BACKGROUND_COLOR + "\"; \n"
			+ "                unappliedTable.rows[i].onmouseout = function () { this.style.backgroundColor = \"" + TABLE_ROW_ODD_ROW_BACKGROUND_COLOR + "\";  }; \n" //this.style.color = \"\";
			+ "            }else{\n"
			//+ "                unappliedTable.rows[i].style.background = \"" + TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR + "\"; \n"
			+ "                unappliedTable.rows[i].onmouseout = function () { this.style.backgroundColor = \"" + TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR + "\";  }; \n" //this.style.color = \"\";
			+ "            }\n"
			+ "            unappliedTable.rows[i].onmousemove = function () { this.style.backgroundColor = \"" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "\"; }; \n" //this.style.color = \"#FFFFFF\"; 
			//+ "            tbl.rows[i].onclick = function () { moveRowToAppliedTable(this); }; \n" //this.style.color = \"\";
			+ "            oddrow = !oddrow;\n"
		;
		
		/*
		if (bEditable){
			//Add a button for applying a row:
			s += "        if (unappliedTable.rows[i].cells[unappliedTable.rows[i].cells.length - 1].name != '" + UNAPPLIED_TABLE_APPLY_COLUMN_NAME + "'){ \n"
				+ "            var cellapplybutton = unappliedTable.rows[i].insertCell(-1);\n"
				+ "            cellapplybutton.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "            cellapplybutton.name = '" + UNAPPLIED_TABLE_APPLY_COLUMN_NAME + "'; \n"
		        + "            var btnapply = document.createElement('input'); \n"
		        + "            btnapply.type = \"button\"; \n"
		        + "            btnapply.value = \"" + BUTTON_LABEL_APPLYLINE + "\"; \n"
		        //+ "            btnapply.name = \"" + COMMAND_VALUE_APPLYLINE + "\" + i.toString(); \n"
		        + "            //Use the 'name' property to store the doc number:\n"
		        + "            btnapply.name = unappliedTable.rows[i].cells[" + UNAPPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML; \n"
		        + "            btnapply.onclick = function () { moveRowToAppliedTable(this.name);  }; \n"
		        + "            cellapplybutton.appendChild(btnapply); \n"
		        + "        } \n"
		    ;
		}
		*/
		s += "        }\n"
			+ "    }\n"
			+ "}\n"
		;
		
/*
		//Populate 'applied lines' table for the first time:
		s += "function populateAppliedLinesTable(){ \n"
			+ "    var appliedTable = document.getElementById(\"" + TABLE_APPLIED_DOCUMENTS + "\");\n";
		
			for (int i = 0; i < entry.getLineArray().size(); i++){
				APBatchEntryLine line = entry.getLineArray().get(i);
				
				//Read the AP transaction to get the date values, etc:
				String sDocDate = "N/A";
				String sDueDate = "N/A";
				String sOriginalAmt = "N/A";
				String sCurrentAmt = "N/A";
				SQL = "SELECT"
					+ " * FROM " + SMTableaptransactions.TableName
					+ " WHERE ("
						+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
						+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
					+ ")"
				;
				ResultSet rs = SMUtilities.openResultSet(
					SQL, getServletContext(),
					sDBID,
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".displayDetailLines"
				);
				if (rs.next()){
					sDocDate = SMUtilities.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
					sDueDate = SMUtilities.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
					sOriginalAmt = SMUtilities.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.doriginalamt));
					sCurrentAmt = SMUtilities.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.dcurrentamt));
				}
				
				s += "    var appliedTableRow = appliedTable.insertRow(-1);\n"
				+ "    var linenumberfieldprefix = '" + APBatchEntry.LINE_NUMBER_PARAMETER + "' + padLeft(appliedTableRow.rowIndex.toString(), " + Integer.toString(APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + ", '0'); \n"
				+ "    var celllinenumber = appliedTableRow.insertCell(-1); \n"
				+ "    celllinenumber.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"

				+ "    var celldocdate = appliedTableRow.insertCell(-1);\n"
				+ "    celldocdate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    celldocdate.innerHTML = '" + sDocDate + "'; \n"
				
				+ "    var cellduedate = appliedTableRow.insertCell(-1);\n"
				+ "    cellduedate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    cellduedate.innerHTML = '" + sDueDate + "'; \n"

				+ "    var celldocnumber = appliedTableRow.insertCell(-1);\n"
				+ "    celldocnumber.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    var cellfieldname = linenumberfieldprefix + '" + SMTableapbatchentrylines.sapplytodocnumber + "'; \n"
				+ "    celldocnumber.innerHTML = '" + line.getsapplytodocnumber() + "'"
					+  " + '<INPUT TYPE=HIDDEN"
						+ " NAME=' + cellfieldname + '"
						+ " ID=' + cellfieldname + '"
						+ " VALUE=\"" + line.getsapplytodocnumber() + "\""
						+ ">'"
					+ " \n" 
				
				+ "    var celloriginalamt = appliedTableRow.insertCell(-1);\n"
				+ "    celloriginalamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    celloriginalamt.innerHTML = '" + sOriginalAmt + "'; \n"
				
				+ "    var cellcurrentamt = appliedTableRow.insertCell(-1);\n"
				+ "    cellcurrentamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    cellcurrentamt.innerHTML = '" + sCurrentAmt + "'; \n"
				
				+ "    var cellappliedamt = appliedTableRow.insertCell(-1);\n"
				+ "    cellappliedamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
				+ "    var applyamt = document.createElement('input'); \n"
		        + "    applyamt.type=\"text\"; \n"
				+ "    applyamt.name=linenumberfieldprefix + '" + SMTableapbatchentrylines.bdamount + "'; \n"
				+ "    applyamt.id=linenumberfieldprefix + '" + SMTableapbatchentrylines.bdamount + "'; \n"
				+ "    applyamt.value = '" + line.getsbdamount() + "'; \n"
		        + "    applyamt.size = 10; \n"
				+ "    applyamt.onchange = function () { updateLineTotal();  }; \n"
		        + "    cellappliedamt.appendChild(applyamt); \n"
				;
				if (bEditable){
			        //Add a button to remove the row:
					s += "    var cellremovebutton = appliedTableRow.insertCell(-1);\n"
						+ "    cellremovebutton.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "'; \n"
				        + "    var btnremove = document.createElement('input'); \n"
				        + "    btnremove.type = \"button\"; \n"
				        + "    btnremove.name = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n" 
				        + "    btnremove.id = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n"
				        + "    btnremove.value = \"" + BUTTON_LABEL_UNAPPLYLINE + "\"; \n"
				        + "    btnremove.onclick = function () { moveRowToUnappliedTable(appliedTableRow.rowIndex);  }; \n"
				        //td.appendChild(btn);
				        + "    cellremovebutton.appendChild(btnremove); \n"
				    ;
				}
			}
				
			s += "} \n";
		;		
*/
/*
		s += "function configAppliedTable(){\n"
				+ "    var appliedTable = document.getElementById(\"" + TABLE_APPLIED_DOCUMENTS + "\");\n"
				+ "    if (appliedTable != null) {\n"
				+ "        var oddrow = false;\n"
				+ "        var backgroundcolor = \"\"; \n"
				+ "        for (var i = 1; i < appliedTable.rows.length; i++) {\n"
				//+ "            var linenumberfieldprefix = '" + APBatchEntry.LINE_NUMBER_PARAMETER + "' + padLeft(i.toString(), " + APBatchEntry.LINE_NUMBER_PADDING_LENGTH + ", '0'); \n"
				//+ "            //Set the fieldnames with the correct row numbers: \n"
				//+ "            \n"
				//+ "            //Line number: \n"
				//+ "            var linenumberfieldname = linenumberfieldprefix + '" + SMTableapbatchentrylines.lid + "'; \n"
				//+ "            appliedTable.rows[i].cells[" + APPLIED_TABLE_LINENO_COLUMN_NUMBER + "].innerHTML = (i.toString() + '<INPUT TYPE=HIDDEN"
				//	+ " NAME=' + linenumberfieldname + '"
				//	+ " ID=' + linenumberfieldname + '"
				//	+ " VALUE=' + i.toString() + '"
				//	+ ">' ); \n"
				//
				//+ "            //Replace the line numbers in the names and IDs of the fields in the row with the new line number: \n"
				//+ "            var sCellText = appliedTable.rows[i].cells[" + APPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML; \n"
				//+ "            var nLineNumberIndex = sCellText.indexOf(\"" + APBatchEntry.LINE_NUMBER_PARAMETER + "\"); \n"
				//+ "            var previousLineNumberPrefix = sCellText.substr(nLineNumberIndex, '" + APBatchEntry.LINE_NUMBER_PARAMETER + "'.length + " 
				//	+ Integer.toString(APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + "); \n"
				//+ "            \n"
				//+ "            appliedTable.rows[i].cells[" + APPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML = appliedTable.rows[i].cells[" 
				//	+ APPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML.replace(previousLineNumberPrefix, linenumberfieldprefix); \n"
				//
				//+ "            appliedTable.rows[i].cells[" + APPLIED_TABLE_APPLYINGAMT_COLUMN_NUMBER + "].innerHTML = appliedTable.rows[i].cells[" 
				//	+ APPLIED_TABLE_APPLYINGAMT_COLUMN_NUMBER + "].innerHTML.replace(previousLineNumberPrefix, linenumberfieldprefix); \n"
				//	
				//+ "            var removeLineButton = document.getElementById(previousLineNumberPrefix + '" + BUTTON_NAME_UNAPPLYLINE + "'); \n"
				//+ "            if (removeLineButton != null){ \n"
				//+ "                removeLineButton.name = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n" 
				//+ "                removeLineButton.id = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n"
				//+ "                var sLineNumber = i.toString(); \n"
				//+ "                removeLineButton.onclick = function () { moveRowToUnappliedTable(sLineNumber);  }; \n"
				//+ "            } \n"
				
				+ "            if (oddrow){\n"
				//+ "                appliedTable.rows[i].style.background = \"" + TABLE_ROW_ODD_ROW_BACKGROUND_COLOR + "\"; \n"
				+ "                appliedTable.rows[i].onmouseout = function () { this.style.backgroundColor = \"" + TABLE_ROW_ODD_ROW_BACKGROUND_COLOR + "\";  }; \n"
				+ "            }else{\n"
				//+ "                appliedTable.rows[i].style.background = \"" + TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR + "\"; \n"
				+ "                appliedTable.rows[i].onmouseout = function () { this.style.backgroundColor = \"" + TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR + "\";  }; \n"
				+ "            }"
				+ "            appliedTable.rows[i].onmousemove = function () { this.style.backgroundColor = \"" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "\"; }; \n"
				//+ "            tbl.rows[i].onclick = function () { moveRowToUnappliedTable(this); }; \n"
				+ "            oddrow = !oddrow;\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n"
			;
*/	
/*
		s += "function moveRowToAppliedTable(sDocNumber){\n"
			+ "    var appliedTable = document.getElementById(\"" + TABLE_APPLIED_DOCUMENTS + "\");\n"
			+ "    var appliedTableRow = appliedTable.insertRow(-1);\n"
			+ "    var unappliedTable = document.getElementById(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\");\n"
			+ "    var nUnappliedTableRowNumber = -1; \n"
			+ "    for (var i = 0; i < unappliedTable.rows.length; i++) {\n"
			+ "        if (unappliedTable.rows[i].cells[" + UNAPPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML == sDocNumber){ \n"
			+ "            nUnappliedTableRowNumber = i; \n"
			+ "            break; \n"
			+ "        } \n"
			+ "    } \n"
			+ "    var linenumberfieldprefix = '" + APBatchEntry.LINE_NUMBER_PARAMETER + "' + padLeft(appliedTableRow.rowIndex.toString(), " + Integer.toString(APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + ", '0'); \n"
			+ "    var unappliedRow = unappliedTable.rows[nUnappliedTableRowNumber]; \n"
			+ "    var celllinenumber = appliedTableRow.insertCell(-1); \n"
			+ "    celllinenumber.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"

			+ "    var celldocdate = appliedTableRow.insertCell(-1);\n"
			+ "    celldocdate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    celldocdate.innerHTML = unappliedRow.cells[" + UNAPPLIED_TABLE_DOCDATE_COLUMN_NUMBER +"].innerHTML; \n"
			
			+ "    var cellduedate = appliedTableRow.insertCell(-1);\n"
			+ "    cellduedate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    cellduedate.innerHTML = unappliedRow.cells[" + UNAPPLIED_TABLE_DUEDATE_COLUMN_NUMBER + "].innerHTML; \n"

			+ "    var celldocnumber = appliedTableRow.insertCell(-1);\n"
			+ "    celldocnumber.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    var cellfieldname = linenumberfieldprefix + '" + SMTableapbatchentrylines.sapplytodocnumber + "'; \n"
			+ "    celldocnumber.innerHTML = unappliedRow.cells[" + UNAPPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML"
				+  " + '<INPUT TYPE=HIDDEN"
					+ " NAME=' + cellfieldname + '"
					+ " ID=' + cellfieldname + '"
					+ " VALUE=' + unappliedRow.cells[" + UNAPPLIED_TABLE_DOCNUMBER_COLUMN_NUMBER + "].innerHTML + '"
					+ ">'"
				+ " \n" 
			
			+ "    var celloriginalamt = appliedTableRow.insertCell(-1);\n"
			+ "    celloriginalamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    celloriginalamt.innerHTML = unappliedRow.cells[" + UNAPPLIED_TABLE_ORIGINALAMT_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    var cellcurrentamt = appliedTableRow.insertCell(-1);\n"
			+ "    cellcurrentamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    cellcurrentamt.innerHTML = unappliedRow.cells[" + UNAPPLIED_TABLE_CURRENTAMT_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    var cellappliedamt = appliedTableRow.insertCell(-1);\n"
			+ "    cellappliedamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    var applyamt = document.createElement('input'); \n"
	        + "    applyamt.type=\"text\"; \n"
			+ "    applyamt.name=linenumberfieldprefix + '" + SMTableapbatchentrylines.bdamount + "'; \n"
			+ "    applyamt.id=linenumberfieldprefix + '" + SMTableapbatchentrylines.bdamount + "'; \n"
			+ "    applyamt.value = unappliedRow.cells[" + UNAPPLIED_TABLE_CURRENTAMT_COLUMN_NUMBER + "].innerHTML; \n"
	        + "    applyamt.size = 10; \n"
			+ "    applyamt.onchange = function () { updateLineTotal();  }; \n"
	        + "    cellappliedamt.appendChild(applyamt); \n"
		;
		if (bEditable){
	        //Add a button to remove the row:
			s += "    var cellremovebutton = appliedTableRow.insertCell(-1);\n"
				+ "    cellremovebutton.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "'; \n"
		        + "    var btnremove = document.createElement('input'); \n"
		        + "    btnremove.type = \"button\"; \n"
		        + "    btnremove.name = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n" 
		        + "    btnremove.id = linenumberfieldprefix + '" + BUTTON_NAME_UNAPPLYLINE + "'; \n"
		        + "    btnremove.value = \"" + BUTTON_LABEL_UNAPPLYLINE + "\"; \n"
		        + "    btnremove.onclick = function () { moveRowToUnappliedTable(appliedTableRow.rowIndex);  }; \n"
		        //td.appendChild(btn);
		        + "    cellremovebutton.appendChild(btnremove); \n"
		    ;
		}
		s += "    //Remove the original row:\n"
			+ "    document.getElementById(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\").deleteRow(unappliedRow.rowIndex);\n"
			+ "    configUnappliedTable(); \n"
			+ "    configAppliedTable(); \n"
			+ "    calculatelinetotal(); \n"
			+ "    flagDirty(); \n"
			+ "}\n"
		;
		
		s += "function moveRowToUnappliedTable(appliedRowIndex){\n"
				
			+ "    var AppliedTable = document.getElementById(\"" + TABLE_APPLIED_DOCUMENTS + "\");\n"
			+ "    var appliedRow = AppliedTable.rows[appliedRowIndex];\n"
			+ "    var unappliedTable = document.getElementById(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\");\n"
			+ "    var row = unappliedTable.insertRow(-1);\n"

			+ "    var celldocdate = row.insertCell(-1);\n"
			+ "    celldocdate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    celldocdate.innerHTML = appliedRow.cells[" + APPLIED_TABLE_DOCDATE_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    var cellduedate = row.insertCell(-1);\n"
			+ "    cellduedate.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    cellduedate.innerHTML = appliedRow.cells[" + APPLIED_TABLE_DUEDATE_COLUMN_NUMBER + "].innerHTML; \n"

			+ "    var celldocnumber = row.insertCell(-1);\n"
			+ "    celldocnumber.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    celldocnumber.innerHTML = appliedRow.cells[" + APPLIED_TABLE_ORIGINALAMT_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    var celloriginalamt = row.insertCell(-1);\n"
			+ "    celloriginalamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    celloriginalamt.innerHTML = appliedRow.cells[" + APPLIED_TABLE_CURRENTAMT_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    var cellcurrentamt = row.insertCell(-1);\n"
			+ "    cellcurrentamt.className = '" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "'; \n"
			+ "    cellcurrentamt.innerHTML = appliedRow.cells[" + APPLIED_TABLE_APPLYINGAMT_COLUMN_NUMBER + "].innerHTML; \n"
			
			+ "    //Remove the original row:\n"
			+ "    document.getElementById(\"" + TABLE_APPLIED_DOCUMENTS + "\").deleteRow(appliedRowIndex);\n"
			+ "    //Remove the 'applytodocnumber' HIDDEN field for this row:\n"
			+ "    var linenumberfieldprefix = '" + APBatchEntry.LINE_NUMBER_PARAMETER + "' + padLeft(appliedRowIndex.toString(), 6, '0'); \n"
			+ "    var cellapplytodocnumberfieldname = linenumberfieldprefix + '" + SMTableapbatchentrylines.sapplytodocnumber + "'; \n"
			+ "    var fieldApplyToDoc = document.getElementById(cellapplytodocnumberfieldname); \n"
			+ "    if (fieldApplyToDoc != null){ \n"
			+ "        fieldApplyToDoc.remove(); \n"
			+ "    } \n"
			+ "    configUnappliedTable(); \n"
			+ "    //Sort the table by the first column again:\n"
			+ "    sortTable('" + TABLE_UNAPPLIED_DOCUMENTS + "', " + APPLIED_TABLE_DOCDATE_COLUMN_NUMBER + ", 1);\n"
			+ "    configAppliedTable(); \n"
			+ "    calculatelinetotal(); \n"
			+ "    flagDirty(); \n"
			+ "}\n"
		;
*/
		s += "function updateLineTotal(){\n"
			+ "    calculatelinetotal();\n"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function calculatelinetotal(){\n"
			//+ "    //Turn off the line amt warning by default:\n"
			//+ "    document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.display= \"none\"\n"
			+ "    var linetotal = getFloat(\"0.00\");\n"
			+ "    var entryamt = getFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMTableapbatchentries.bdentryamount + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        entryamt = getFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        entryamt = getFloat(temp);\n"
			+ "    }\n"
			
			+ "    // For each of the lines on the entry, add the amount:\n"
			+ "	   for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
			+ "        //Get the name of the control:\n"
   			+ "	       var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
			+ "        //If the control name starts with '" + APBatchEntry.LINE_NUMBER_PARAMETER + "', then pick off the rest of it:\n"
   			+ "        if (testName.substring(0, " + Integer.toString(APBatchEntry.LINE_NUMBER_PARAMETER.length()) + "	) == \"" + APBatchEntry.LINE_NUMBER_PARAMETER + "\"){\n"
   			+ "            //If the string ENDS with the field name '" + SMTableapbatchentrylines.bdamount + "' OR '" + SMTableapbatchentrylines.bdapplieddiscountamt + "',\n"
   			+ "            // then it's a line amount or discount amt:\n"
   			+ "            if (testName.endsWith(\"" + SMTableapbatchentrylines.bdamount + "\") == true){\n"
   			+ "                //Add it to the line total:\n"
   			+ "                //First remove any extra spaces:\n"
   			//+ "                alert('!' + testName + '!'); \n"
   			+ "                temp = document.getElementById(testName).value.replace(/ /g, \"\");\n"
   			+ "                //Reset the text in the input field: \n"
   			+ "                document.getElementById(testName).value = temp; \n"
   			+ "                //Now replace the commas, just for the calculation: \n"
   			//+ "                temp = document.getElementById(testName).value.replace(',','');\n"
   			+ "                temp = temp.replace(/,/g , \"\");\n"
   			+ "                if (temp != ''){\n"
   			+ "                    if(!isNaN(temp)){\n"
   			+ "                        document.getElementById(testName).value = getFloat(temp).toFixed(2); \n"
   			+ "                        linetotal = linetotal + getFloat(temp);\n"
   		;
		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
   			s += "                        //Since this is an APPLY-TO, we need to update the NET AMT for this line when the applied amt changes:\n"
   				+ "                        var currentamtfieldname = testName.substring(0, " + Integer.toString(APBatchEntry.LINE_NUMBER_PARAMETER.length() + APBatchEntry.LINE_NUMBER_PADDING_LENGTH)
   					+ ") + '" + SMTableaptransactions.bdcurrentamt + "';\n"
   				+ "                        var currentamt = getFloat(\"0.00\");\n"
   				+ "                        currentamt = document.getElementById(currentamtfieldname).value.replace(',','');\n"
   				+ "                        //Strip off the spaces:\n"
   				+ "                        var currentamt = currentamt.replace(/ /g, \"\");\n"
   				+ "                        var netamt = getFloat(\"0.00\");\n"
   				+ "                        netamt = getFloat(currentamt) + getFloat(temp);\n"
   				+ "                        var netamtfiedname = testName.substring(0, " + Integer.toString(APBatchEntry.LINE_NUMBER_PARAMETER.length() + APBatchEntry.LINE_NUMBER_PADDING_LENGTH)
   					+ ") + '" + NET_AMT_FIELD_NAME + "';\n"
   				+ "                        document.getElementById(netamtfiedname).value=netamt.toFixed(2);\n"
   			;
		}
   		s	+= "                    }\n"
   			+ "                }\n"
   			+ "            }\n"
   			+ "        }\n"
   			+ "    }\n"
   			
   			//+ "    if (!floatsAreEqual(linetotal, entryamt)){\n"
   			+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD + "\").innerText=linetotal.toFixed(2);\n"
   			+ "        document.getElementById(\"" + SMTableapbatchentries.bdentryamount + "\").value=linetotal.toFixed(2);\n"
   			//+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.display= \"inline\"\n"
   			//+ "    }else{\n"
   			//+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD + "\").innerText='';\n"
   			//+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.display= \"none\"\n"
   			//+ "    }\n"
   			+ "}\n"
   		;
   		s += "function toggleCheckNumber(){\n"
   			+ "    if (document.getElementById(\"" + SMTableapbatchentries.iprintcheck + "\").checked){\n"
   			+ "        document.getElementById(\"" + SMTableapbatchentries.schecknumber + "\").style.display='none';\n"
   			+ "    }else{\n"
   			+ "        document.getElementById(\"" + SMTableapbatchentries.schecknumber + "\").style.display='';\n"
   			+ "    }\n"
   			+ "}\n"
   		;
   		
   		s += "function processToggleCheckNumber(){\n"
   	   			+ "    toggleCheckNumber();\n"
   	   			+ "    flagDirty();\n"
   	   			+ "}\n"
   	   		;
		
		s += "function sortUnappliedTable(nSortColumnIndex) { \n"
			+ "    sortTable(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\", nSortColumnIndex, 1); \n"
			+ "    configUnappliedTable(); \n"
			+ "} \n"
		;
		
		//Hot key stuff:
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        deleteentry();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+f\",function() {\n";
		s += "        findVendor();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+n\",function() {\n";
		s += "        saveandadd();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        printcheck();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+u\",function() {\n";
		s += "        save();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+v\",function() {\n";
		s += "        updateVendorInfo();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "}\n";
		s += "\n";
		s += "</script>\n";
		return s;
	}
	
	private String createUnapplyLineButton(String sLineNumber){
		return "<button type=\"button\""
				+ " VALUE=\"" + BUTTON_LABEL_UNAPPLYLINE + "\""
				//+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER + SMUtilities.PadLeft(sLineNumber.trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + BUTTON_NAME_UNAPPLYLINE + "\""
				//+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER + SMUtilities.PadLeft(sLineNumber.trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + BUTTON_NAME_UNAPPLYLINE + "\""
				+ " onClick=\"unapplyLine(" + sLineNumber.trim() + ");\">"
				+ BUTTON_LABEL_UNAPPLYLINE
				+ "</button>\n"
				;
	}
	private String createRemoveLineButton(String sLineNumber){
		return "<button type=\"button\""
				+ " value=\"" + BUTTON_LABEL_REMOVELINE + "\""
				+ " name=\"" + BUTTON_LABEL_REMOVELINE + "\""
				+ " onClick=\"removeLine(" + sLineNumber + ");\">"
				+ BUTTON_LABEL_REMOVELINE
				+ "</button>\n"
				;
	}
	private String createApplyLineButton(String sApplyToDocNumber){
		return "<button type=\"button\""
				+ " VALUE=\"" + BUTTON_LABEL_APPLYLINE + "\""
				//+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER + SMUtilities.PadLeft(sLineNumber.trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + BUTTON_NAME_APPLYLINE + "\""
				//+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER + SMUtilities.PadLeft(sLineNumber.trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) + BUTTON_NAME_APPLYLINE + "\""
				+ " onClick=\"applyLine('" + sApplyToDocNumber.trim() + "');\">"
				+ BUTTON_LABEL_APPLYLINE
				+ "</button>\n"
				;
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ UPDATE_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createDeleteButton(){
		return "<button type=\"button\""
				+ " value=\"" + DELETE_BUTTON_LABEL + "\""
				+ " name=\"" + DELETE_BUTTON_LABEL + "\""
				+ " onClick=\"deleteentry();\">"
				+ DELETE_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createSaveAndAddButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_AND_ADD_NEW_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_AND_ADD_NEW_BUTTON_LABEL + "\""
				+ " onClick=\"saveandadd();\">"
				+ UPDATE_AND_ADD_NEW_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createFindVendorButton(){
		return  "<button type=\"button\""
				+" value=\"" + FIND_VENDOR_BUTTON_LABEL + "\""
				+ "name=\"" + FIND_VENDOR_BUTTON_LABEL + "\""
				+ " onClick=\"findVendor();\">"
				+  FIND_VENDOR_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createUpdateVendorInfoButton(){
		return  "<button type=\"button\""
				+" value=\"" + UPDATE_VENDOR_INFO_BUTTON_LABEL + "\""
				+ "name=\"" + UPDATE_VENDOR_INFO_BUTTON_LABEL + "\""
				+ " onClick=\"updateVendorInfo();\">"
				+  UPDATE_VENDOR_INFO_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createPrintCheckButton(){
		return  "<button type=\"button\""
				+" value=\"" + PRINT_CHECK_BUTTON_LABEL + "\""
				+ "name=\"" + PRINT_CHECK_BUTTON_LABEL + "\""
				+ " onClick=\"printcheck();\">"
				+  PRINT_CHECK_BUTTON_LABEL
				+ "</button>\n";
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
