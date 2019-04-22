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

import SMClasses.SMTax;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicvendorterms;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditInvoiceEdit  extends HttpServlet {

	public static final String BUTTON_LABEL_REMOVELINE = "Remove";
	public static final String COMMAND_VALUE_REMOVELINE = "RemoveLine";
	public static final String COMMAND_VALUE_EDITLINE = "EditLine";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String LINE_NUMBER_TO_DELETE_PARAM = "LineNumberParam";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	public static final String COMMAND_VALUE_SAVE_AND_ADD = "Updateandaddnew";
	
	public static final String BUTTON_NAME_TOGGLEUNAPPLIEDTABLE = "TOGGLEUNAPPLIEDTABLEBUTTON";
	public static final String COMMAND_VALUE_TOGGLEUNAPPLIEDTABLE = "ToggleApplyToDocuments";
	public static final String PARAM_TOGGLEUNAPPLIEDTABLE = "TOGGLEAPPLYTO";
	public static final String PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_DISPLAY = "DISPLAYTABLE";
	public static final String PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_HIDE = "HIDETABLE";

	public static final String COMMAND_VALUE_FINDVENDOR = "FINDVENDOR";
	public static final String RETURNING_FROM_FIND_VENDOR_FLAG = "RETURNINGFROMFINDER";
	public static final String FOUND_VENDOR_PARAMETER = "FOUND" + SMTableapbatchentries.svendoracct;
	
	public static final String COMMAND_VALUE_CALCULATETERMS = "CALCULATETERMS";
	public static final String RETURNING_FROM_CALCULATETERMS_FLAG = "RETURNINGFROMCALCULATETERMS";
	
	public static final String COMMAND_VALUE_UPDATEVENDORINFO = "UPDATEVENDORINFO";
	public static final String UPDATED_VENDOR_INFO_PARAMETER = "UPDATED" + SMTableapbatchentries.svendoracct + "INFO";
	public static final String RETURNING_FROM_UPDATEVENDORINFO_FLAG = "RETURNINGFROMUPDATEVENDORINFO";
	
	public static final String CALCULATED_LINE_TOTAL_FIELD = "CALCULATEDLINETOTAL";
	public static final String CALCULATED_LINE_TOTAL_FIELD_CONTAINER = "CALCULATEDLINETOTALCONTAINER";
	
	public static final String TABLE_UNAPPLIED_DOCUMENTS = "APPLYTODOCSTABLE";
	public static final String UNAPPLIED_DOCUMENTS_TABLE_CONTAINER = "UNAPPLIEDDOCSTABLECONTAINER";
	public static final String TABLE_APPLIED_DOCUMENTS = "APPLIEDDOCUMENTS";
	public static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW"; //"#FF2080";
	public static final int NUMBER_PADDING_LENGTH = 11;
	public static final String BUTTON_LABEL_APPLYLINE = "Apply";
	public static final String BUTTON_LABEL_UNAPPLYLINE = "UNApply";
	public static final String BUTTON_NAME_UNAPPLYLINE = "UNApplyButton";
	public static final String COMMAND_VALUE_UNAPPLYLINE = "UnapplyLine";
	public static final String LINE_NUMBER_TO_UNAPPLY_PARAM = "UnapplyLineNumber";
	public static final String COMMAND_VALUE_APPLYTODOC = "ApplyLine";
	public static final String APPLYTODOCNUMBER_TO_APPLY_PARAM = "ApplyToDocNumber";
	public static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	
	public static final String BOOKMARK_TOP_OF_TABLES = "TopOfTables";
	public static final String RETURN_TO_TABLES_BOOKMARK = "RETURNTOTABLESPARAM";
	
	//Hot keys:
	public static final String FIND_VENDOR_BUTTON_LABEL = "<B><FONT COLOR=RED>F</FONT></B>ind vendor"; // F
	public static final String UPDATE_VENDOR_INFO_BUTTON_LABEL = "Update <B><FONT COLOR=RED>v</FONT></B>endor info"; // V
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; // U
	public static final String UPDATE_AND_ADD_NEW_BUTTON_LABEL = "Update and add <B><FONT COLOR=RED>n</FONT></B>ew"; // N
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; // D
	public static final String VIEW_APPLY_TO_DOCUMENTS_BUTTON_LABEL = "Vie<B><FONT COLOR=RED>w</FONT></B> table of apply-to documents"; // W
	public static final String HIDE_APPLY_TO_DOCUMENTS_BUTTON_LABEL = "<B><FONT COLOR=RED>H</FONT></B>ide table of apply-to documents"; // H
	public static final String CALCULATE_TERMS_BUTTON_LABEL = "Calculate <B><FONT COLOR=RED>t</FONT></B>erms"; // T
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		APBatchEntry entry = new APBatchEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"AP Entry",
				SMUtilities.getFullClassName(this.toString()),
				"smap.APEditInvoiceAction",
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
		
		//long lStartingtime = System.currentTimeMillis();
		
	    if (currentSession.getAttribute(APBatchEntry.OBJECT_NAME) != null){
	    	entry = (APBatchEntry) currentSession.getAttribute(APBatchEntry.OBJECT_NAME);
	    	currentSession.removeAttribute(APBatchEntry.OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		//Assuming we aren't just returning from finding a vendor or apply to doc number:
	    		if (
	    			(request.getParameter(RETURNING_FROM_FIND_VENDOR_FLAG) == null)
	    		){
	    		
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
	    		}
	    	}
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
	    	try {
	    		calculateTerms(smedit, entry);
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1493652422] calculating terms for terms code '" 
					+ entry.getsterms() + "' - " + e.getMessage()
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
	    	try {
	    		calculateTerms(smedit, entry);
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1493652421] calculating terms for terms code '" 
					+ entry.getsterms() + "' - " + e.getMessage()
					+ "</B></FONT><BR>"
				);
			}
	    	//Add the vendor's 'default' line, if it's the first line, and if it's an invoice:
	    	//If we ARE adding a new entry, insert the vendor's 'default' line, if there is one:
    		try {
				entry.addDefaultInvoiceLine(getServletContext(), smedit.getsDBID(), smedit.getUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1490910300] creating initial default line for this vendor - " + e.getMessage()
					+ "</B></FONT><BR>"
				);
			}
	    }
		
	    //If we are returning from a request to calculate the terms information, do that now:
	    if (request.getParameter(RETURNING_FROM_CALCULATETERMS_FLAG) != null){
	    	try {
	    		calculateTerms(smedit, entry);
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error [1490633413] calculating terms for terms code '" 
					+ entry.getsterms() + "' - " + e.getMessage()
					+ "</B></FONT><BR>"
				);
			}
	    }
	    
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
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
	    		+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch " + entry.getsbatchnumber() + "</A><BR>\n");
		
	    APBatch batch = new APBatch(entry.getsbatchnumber());
	    try {
			batch.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
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
	    
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry, batch.bEditable()), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
				+ "&Warning=Could not invoice entry screen: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    
	    smedit.getPWOut().println(SMTableaptransactions.getInvoiceTaxLiabilityFootnote());
	    
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, APBatchEntry entry, boolean bEditable) throws SQLException{
		String s = "";
		boolean bIsCreditNote = entry.getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)) == 0;
		boolean bIsDebitNote = entry.getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)) == 0;
		try {
			s += sCommandScript(sm.getsDBID(), sm, entry);
		} catch (Exception e2) {
			s += "<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>";
		}
		
		//Get some permissions before we start:
		boolean bAllowSalesOrderViewing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(), 
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			)
		;
		
		boolean bAllowPOViewing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditPurchaseOrders, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(), 
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		boolean bAllowPOReceiptViewing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditReceipts, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(), 
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
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
		//Store database id 
				s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + sm.getsDBID() + "\""
				+ "\">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//Store which line the user has chosen to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\""
		+ "\">";
		
		//Store which line the user has chosen to unapply:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINE_NUMBER_TO_UNAPPLY_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINE_NUMBER_TO_UNAPPLY_PARAM + "\""
		+ "\">";
		
		//Store which line the user has chosen to apply:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APPLYTODOCNUMBER_TO_APPLY_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + APPLYTODOCNUMBER_TO_APPLY_PARAM + "\""
		+ "\">";
		
		//Store whether the unapplied table should be visible:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_TOGGLEUNAPPLIEDTABLE 
		+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PARAM_TOGGLEUNAPPLIEDTABLE, sm.getRequest()) + "\""
		+ " id=\"" + PARAM_TOGGLEUNAPPLIEDTABLE + "\""
		+ "\">";
		
		//Store the GL control account, which the user shouldn't be changing on the invoice screen:
		s += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.scontrolacct + "\""
				+ " VALUE=\"" + entry.getscontrolacct() + "\" >";
		
		s += "<BR><TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		
		s += "  <TR>\n";
		
		//Batchnumber:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" ><B>Batchnumber</B>:</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
				+ entry.getsbatchnumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lbatchnumber + "\" VALUE=\"" 
				+ entry.getsbatchnumber() + "\">"
				+ "</B></TD>\n"
			;
		
		//Entry number:
		String sEntryNumber = "NEW";
		if (
			(entry.getsentrynumber().compareToIgnoreCase("-1") != 0)
			&& (entry.getsentrynumber().compareToIgnoreCase("0") != 0)
			&& (entry.getsentrynumber().compareToIgnoreCase("") != 0)
		){
			sEntryNumber = entry.getsentrynumber();
		}
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" ><B>Entrynumber</B>:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ sEntryNumber 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lentrynumber + "\" VALUE=\"" 
			+ entry.getsentrynumber() + "\">"
			+ "</B></TD>\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		String sID = "NEW";
		if (
			(!sm.getAddingNewEntryFlag())
			||
			(
				(entry.getslid().compareToIgnoreCase("-1") != 0)
				&& (entry.getslid().compareToIgnoreCase("0") != 0)
				&& (entry.getslid().compareToIgnoreCase("") != 0)
			)
		){
			sID = entry.getslid();
		}
		
		//Entry ID:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" ><B>"
				+ " " + "Entry ID</B>:</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD>\n"
		;
		
		//Document type:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" ><B>Document&nbsp;type:</B></TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
			+ SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(entry.getsentrytype())).toUpperCase()
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.ientrytype + "\" VALUE=\"" 
			+ entry.getsentrytype() + "\">"
			+ "</B></TD>\n"
		;

		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		String sControlHTML = "";

		//Entry date:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Entry&nbsp;date:&nbsp;</TD>\n";
    	
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
    	
    	//Document date:
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
		
       //Doc number:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Document&nbsp;number:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.sdocnumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdocnumber()) + "\""
	    		+ " MAXLENGTH=" + Integer.toString(SMTableapbatchentries.sdocnumberLength)
	    		+ " SIZE = " + "30"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sdocnumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdocnumber()) + "\""
	    		+ ">"
	    		+ entry.getsdocnumber()
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
		
    	//Amt:
    	String sAmt = entry.getsentryamount();
    	if (bIsCreditNote){
    		//In this case we'll want to show the typically negative amounts as positive to the user:
    		try {
				BigDecimal bdTemp = new BigDecimal(sAmt.trim().replace(",", "")).negate();
				sAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTemp);
			} catch (Exception e) {
				//If we can't parse a valid amount out of the amount string, just show it as is on the screen:
				sAmt = entry.getsentryamount();
			}
    	}
    	
    	s += "  <TR>\n"
		    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Amount:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.bdentryamount + "\""
    			+ " style=\"text-align:right;\""
    			+ " ID=\"" + SMTableapbatchentries.bdentryamount + "\""
	    		+ " VALUE=\"" + sAmt + "\""
	    		+ " MAXLENGTH=" + "14"
	    		+ " SIZE = " + "10"
	    		+ " onchange=\"updateLineTotal();\""
	    		+ ">"
	    	;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.bdentryamount + "\""
    			+ " ID=\"" + SMTableapbatchentries.bdentryamount + "\""
	    		+ " VALUE=\"" + sAmt + "\""
	    		+ ">"
	    		+ sAmt
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
    		+ "&nbsp;<div style = \"" + " display: inline; color: black" + "\" id=\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\"><B>LINE TOTAL:&nbsp;<label id=\"" + CALCULATED_LINE_TOTAL_FIELD + "\" >" 
			+ "" + "</label></FONT></B></div></TD>\n";
    	
    	//On hold:
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >On&nbsp;hold?</TD>\n"
 		    ;
     	if (bEditable){
     		String sTemp = "";
     		if (entry.getsionhold().compareToIgnoreCase("0") != 0){
    			sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
    		}
     		sControlHTML = "<INPUT TYPE=CHECKBOX"
     			+ " NAME=\"" + SMTableapbatchentries.ionhold + "\""
     			+ " ID=\"" + SMTableapbatchentries.ionhold + "\""
     			+ " " + sTemp
 	    		+ " onchange=\"flagDirty();\""
 	    		+ ">"
 	    	;
     	}else{
     		String sOnHoldValue = "N";
     		if (entry.getsionhold().compareToIgnoreCase("1") == 0){
     			sOnHoldValue = "Y";
     		}
     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.ionhold + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsionhold()) + "\""
 	    		+ ">"
 	    		+ "<B>" + sOnHoldValue + "</B>"
 	    	;
     	}
     	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
    	s += "  <TR>\n";
    	
    	//Tax included?
		s += "    <TD style=\" text-align:right; font-weight:bold; \"><a href=\"#TAXLIABILITYNOTE\">Additional tax liability?<SUP>1</a></SUP>:&nbsp;</TD>\n";
		
		if (bEditable){
		
			sControlHTML = "<SELECT NAME = \"" + SMTableapbatchentries.iinvoiceincludestax + "\" >\n";
			sControlHTML += "<OPTION"
				+ " VALUE=\"" 
				+ "-1" 
				+ "\">" 
				+ "** SELECT OPTION **"
				+ "</OPTION>\n"
			;
			sControlHTML += "<OPTION";
			if (entry.getsiinvoiceincludestax().compareToIgnoreCase("1") == 0){
				sControlHTML += " selected=YES ";
			}
			sControlHTML += " VALUE=\"" 
				+ "1" 
				+ "\">" 
				+ SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.NO_ADDITIONAL_TAX_LIABILITY)
				+ "</OPTION>\n"
			;
	
			sControlHTML += "<OPTION";
			if (entry.getsiinvoiceincludestax().compareToIgnoreCase("0") == 0){
				sControlHTML += " selected=YES ";
			}
			sControlHTML += " VALUE=\"" 
				+ "0" 
				+ "\">" 
				+ SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.ADDITIONAL_TAX_LIABILITY)
				+ "</OPTION>\n"
			;
			sControlHTML += "</SELECT>\n";
			
			sControlHTML += "    </TD>\n";
		}else{
			String sTaxLiabilityLabel = SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.ADDITIONAL_TAX_LIABILITY);
			if (entry.getsiinvoiceincludestax().compareToIgnoreCase("1") == 0){
				sTaxLiabilityLabel = SMTableaptransactions.getInvoiceTaxLiabilityLabel(SMTableaptransactions.NO_ADDITIONAL_TAX_LIABILITY);
			}
			sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.iinvoiceincludestax + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsiinvoiceincludestax()) + "\""
	    		+ ">"
	    		+ sTaxLiabilityLabel
			;
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
        		+ "</TD>\n";
    	
		//Tax type:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Tax:&nbsp;</TD>\n";
		
		if (bEditable){
			ArrayList<String> arrTaxes = new ArrayList<String>(0);
			ArrayList<String> arrTaxDescriptions = new ArrayList<String>(0);
			String SQL = "SELECT"
				+ " " + SMTabletax.lid
				+ ", " + SMTabletax.staxjurisdiction
				+ ", " + SMTabletax.staxtype
				+ " FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.iactive + " = 1)"
					+ " AND (" + SMTabletax.ishowinaccountspayable + " = 1)"
				+ ")"
				+ " ORDER BY " + SMTabletax.staxjurisdiction + ", " + SMTabletax.staxtype
			;
			//First, add a blank item so we can be sure the user chose one:
			arrTaxes.add("");
			arrTaxDescriptions.add("*** Select tax ***");
			
			try {
				ResultSet rsTaxes = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
						sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
						+ ".getEditHTML - user: "
						+ sm.getUserID()
						+ " - "
						+ sm.getFullUserName()
						);
				while (rsTaxes.next()) {
					arrTaxes.add(Long.toString(rsTaxes.getLong(SMTabletax.lid)));
					arrTaxDescriptions.add(
							rsTaxes.getString(SMTabletax.staxjurisdiction)
						+ " - "
						+ rsTaxes.getString(SMTabletax.staxtype)
					);
				}
				rsTaxes.close();
			} catch (SQLException e) {
				s += "<B>Error [1490631646] reading tax info - " + e.getMessage() + "</B><BR>";
			}

			sControlHTML = "<SELECT NAME = \"" + SMTableapbatchentries.itaxid + "\""
					+ " onchange=\"flagDirty();\""
					 + " >\n"
				;
				for (int i = 0; i < arrTaxes.size(); i++){
					sControlHTML += "<OPTION";
					if (arrTaxes.get(i).toString().compareTo(entry.getsitaxid()) == 0){
						sControlHTML += " selected=yes";
					}
					sControlHTML += " VALUE=\"" + arrTaxes.get(i).toString() + "\">" + arrTaxDescriptions.get(i).toString() + "\n";
				}
				sControlHTML += "</SELECT>"
				;
		}else{
			sControlHTML = "N/A";
			SMTax tax = new SMTax();
			tax.set_slid(entry.getsitaxid());
			try {
				tax.load(sm.getsDBID(), getServletContext(), sm.getUserName());
			} catch (Exception e1) {
				//Don't do anything - just leave the string at its default value
			}

				sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.itaxid + "\""
			    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsitaxid()) + "\""
			    		+ ">"
			    		+ tax.get_staxjurisdiction() + " - " + tax.get_staxtype()
					;
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
    		+ "</TD>\n";
    	
    	
		s += "  </TR>\n";
		
		s += "  <TR>\n";
    	
        //Vendor acct:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Vendor&nbsp;account:&nbsp;</TD>\n"
	    ;
	    	
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
    		//If the user is allowed to view the vendor info, make this a link:
    		String sVendorLink = entry.getsvendoracct();
    		if (SMSystemFunctions.isFunctionPermitted(
    			SMSystemFunctions.APDisplayVendorInformation, 
    			sm.getUserID(), 
    			getServletContext(), 
    			sm.getsDBID(), 
    			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
    		){
    			sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smap.APDisplayVendorInformation"
					+ "?" + "VendorNumber" + "=" + entry.getsvendoracct()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
					+ "\">" + sVendorLink + "</A>"
				;
    		}
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.svendoracct + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsvendoracct()) + "\""
	    		+ ">"
	    		+ "<B>" + sVendorLink + "</B> - "
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";    	
    	
    	//Vendor name:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Vendor&nbsp;name:&nbsp;</TD>\n";
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" 
    		+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.svendorname + "\""
    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsvendorname()) + "\""
    		+ ">"
    		+ entry.getsvendorname()
    		+ "</TD>\n";
    		
		s += "  </TR>\n";
		
    	//Terms
	    if((!bIsCreditNote) && (!bIsDebitNote)){
	    	s += "  <TR>\n";
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Terms:&nbsp;</TD>\n"
			    ;
			if (bEditable){
				ArrayList<String> arrTerms = new ArrayList<String>(0);
				ArrayList<String> arrTermsDescriptions = new ArrayList<String>(0);
				String SQL = "SELECT"
					+ " " + SMTableicvendorterms.sTermsCode
					+ ", " + SMTableicvendorterms.sDescription
					+ " FROM " + SMTableicvendorterms.TableName
					+ " ORDER BY LPAD(" + SMTableicvendorterms.sTermsCode + ", " 
						+ Integer.toString(SMTableicvendorterms.sTermsCodeLength) + ", ' ')"
				;
				//First, add a blank item so we can be sure the user chose one:
				arrTerms.add("");
				arrTermsDescriptions.add("*** Select terms ***");
				
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
							sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
							+ ".getEditHTML - user: " + sm.getUserID()
							+ " - "
							+ sm.getFullUserName()
							);
					while (rs.next()) {
						arrTerms.add(rs.getString(SMTableicvendorterms.sTermsCode));
						arrTermsDescriptions.add(
							rs.getString(SMTableicvendorterms.sTermsCode)
							+ " - "
							+ rs.getString(SMTableicvendorterms.sDescription)
						);
					}
					rs.close();
				} catch (SQLException e) {
					s += "<B>Error [1490630421] reading terms codes - " + e.getMessage() + "</B><BR>";
				}
	
				sControlHTML = "<SELECT NAME = \"" + SMTableapbatchentries.sterms + "\""
					+ " onchange=\"flagDirty();\""
					 + " >\n"
				;
				for (int i = 0; i < arrTerms.size(); i++){
					sControlHTML += "<OPTION";
					if (arrTerms.get(i).toString().compareTo(entry.getsterms()) == 0){
						sControlHTML += " selected=yes";
					}
					sControlHTML += " VALUE=\"" + arrTerms.get(i).toString() + "\">" + arrTermsDescriptions.get(i).toString() + "\n";
				}
				sControlHTML += "</SELECT>"
					+ "&nbsp;" + createCalculateTermsButton()		
				;
				
			}else{
				String sTermsDescription = "<FONT COLOR=RED><B>Error reading terms.</B></FONT>";
				APVendorTerms terms = new APVendorTerms();
				terms.setsTermsCode(entry.getsterms());
				try {
					terms.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName());
				} catch (Exception e) {
					
				}
				sTermsDescription = terms.getsTermsDescription();
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sterms + "\""
			    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsterms()) + "\""
			    		+ ">"
			    		+ sTermsDescription
			    	;
			}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";   	
    	}else{
    		//If it's a credit note:
    		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.sterms + "\"" + " VALUE=\"" + "" + "\""+ "></TD>\n"; 
    	}    	
    	
    	//Discount amt
	    if((!bIsCreditNote) && (!bIsDebitNote)){
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Discount&nbsp;Amt:&nbsp;</TD>\n"
			    ;
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.bddiscount + "\""
	    			+ " style=\"text-align:right;\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdiscountamt()) + "\""
		    		+ " MAXLENGTH=" + "14"
		    		+ " SIZE = " + "10"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.bddiscount + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdiscountamt()) + "\""
		    		+ ">"
		    		+ entry.getsdiscountamt()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    		
	    	s += "  </TR>\n";
	    }else{
	    	//If it's a credit note:
    		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datduedate + "\"" + " VALUE=\"" + "0.00" + "\"" + ">";
	    }    	
	    
		//Due date:
	    if((!bIsCreditNote) && (!bIsDebitNote)){
	    	s += "  <TR>\n";
		    s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Due&nbsp;date:&nbsp;</TD>\n";
		    	
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.datduedate + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatduedate()) + "\""
		    		+ " MAXLENGTH=" + "10"
		    		+ " SIZE = " + "8"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableapbatchentries.datduedate, getServletContext())
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datduedate + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatduedate()) + "\""
		    		+ ">"
		    		+ entry.getsdatduedate()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    		
	    } else { //If it's a credit note:
    		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datduedate + "\""
       		    + " VALUE=\"" + SMUtilities.EMPTY_DATE_VALUE + "\""
       		    + ">"
       		;
	    }
	    
    	//Discount date:
	    if((!bIsCreditNote) && (!bIsDebitNote)){
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Discount&nbsp;date:&nbsp;</TD>\n"
	    	;
	    	
	    	if (bEditable){
	    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.datdiscount + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdiscount()) + "\""
		    		+ " MAXLENGTH=" + "10"
		    		+ " SIZE = " + "8"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableapbatchentries.datdiscount, getServletContext())
		    	;
	    	}else{
	    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datdiscount + "\""
		    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdiscount()) + "\""
		    		+ ">"
		    		+ entry.getsdatdiscount()
		    	;
	    	}
	    	
	    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
	    	s += "  </TR>\n";
	    }else{
	    	//Else if it's a credit note:
    		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.datdiscount + "\""
    		    + " VALUE=\"" + SMUtilities.EMPTY_DATE_VALUE + "\""
    		    + ">"
    		;
	    }
	    
	    s += "  <TR>\n";
	    
        //Desc:
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
    	
    	//Empty column:
    	s += "    <TD>"
    		+ "&nbsp;" // Comment
    		+ "</TD>\n"
    	;

    	s += "  </TR>\n";
    	
    	s += "  <TR>";
    	
        //Sales order number:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Related sales order number:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.lsalesordernumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslsalesordernumber()) + "\""
	    		+ " MAXLENGTH=13"
	    		+ " SIZE = " + "10"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    	;
    	}else{
    		String sSalesOrderLink = entry.getslsalesordernumber();
			if(bAllowSalesOrderViewing && entry.getslsalesordernumber().compareToIgnoreCase("0") != 0){
				sSalesOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMDisplayOrderInformation"					
						+ "?" + "OrderNumber" + "=" + entry.getslsalesordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() 
						+ "\">" + sSalesOrderLink + "</A>"
					;
				}
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lsalesordernumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslsalesordernumber()) + "\""
	    		+ ">"
	    		+ sSalesOrderLink
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
        //Purchase order number:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Related PO number:&nbsp;</TD>\n"
		    ;
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.lpurchaseordernumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslpurchaseordernumber()) + "\""
	    		+ " MAXLENGTH=13"
	    		+ " SIZE = " + "10"
	    		+ " onchange=\"flagDirty();\""
	    		+ ">"
	    	;
    	}else{
    		String sPurchaseOrderLink = entry.getslpurchaseordernumber();
    		if(bAllowPOViewing && entry.getslpurchaseordernumber().compareToIgnoreCase("0") != 0){
				sPurchaseOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICEditPOEdit"
					+ "?" + SMTableicpoheaders.lid + "=" + entry.getslpurchaseordernumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
					+ "\">" + entry.getslpurchaseordernumber() + "</A>"
				;
			}
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.lpurchaseordernumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getslpurchaseordernumber()) + "\""
	    		+ ">"
	    		+ sPurchaseOrderLink
	    	;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
 
    	
    	s += "  </TR>\n";
		
    	s += "</TABLE>\n";
    	
    	//Display the first row of control buttons:
    	if (bEditable){
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton() + "\n";
    		if (
    	    		(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
    	    		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
    		){
    			s += "&nbsp;" + createToggleUnappliedTableButton(sm) + "\n";
    		}
    	}
    	
    	s += "<BR>\n";
    	
    	if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE){
    		s += clsServletUtilities.createHTMLComment("Start the invoice details table here.");
    		try {
				s += displayInvoiceDetailLines(entry, bEditable, sm, bAllowPOViewing, bAllowPOReceiptViewing);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1489683067] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
    	}
 
    	if (
    		(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
    		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
    	){
    		s += clsServletUtilities.createHTMLComment("Start the details table here.");
    		try {
				s += buildDebitCreditDetailTables(entry, bEditable, sm, bAllowPOViewing, bAllowPOReceiptViewing);
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>Error [1494602711] displaying detail lines - " + e.getMessage() 
					+ "</B></FONT><BR>\n"
					;
			}
        	if (bEditable){
        		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton() + "\n";
        	}
    	}
		
		return s;
	}
	private String buildDebitCreditDetailTables(
			APBatchEntry entry, 
			boolean bEditable, 
			SMMasterEditEntry sm,
			boolean bAllowPOViewing,
			boolean bAllowPOReceiptViewing
		) throws Exception{
		String s = "";
    	s += clsServletUtilities.createHTMLComment("This table contains both the 'open invoices' table and the 'applied' table:");
    	
       	//If we are returning from applying or unapplying a line, return to this spot:
    	if (clsManageRequestParameters.get_Request_Parameter(RETURN_TO_TABLES_BOOKMARK, sm.getRequest()).compareToIgnoreCase("") != 0){
    		s += "<div name=\"" + BOOKMARK_TOP_OF_TABLES + "\"" + "id=\"" + BOOKMARK_TOP_OF_TABLES + "\"" + " ></div>  \n";
    	}
    	
//		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
//				+ ">\n";
    	
//		s += "  <TR>";
		
    	s += clsServletUtilities.createHTMLComment("Start the eligible apply-to documents table here.");
    	
    	if ((bEditable) && (entry.getsvendoracct().compareToIgnoreCase("") != 0)){
//    		s += "    <TD style = \" vertical-align:top; \" >";
	    	try {
				s += displayUnappliedDocumentsForDebitsAndCredits(entry, sm, bEditable);
			} catch (Exception e) {
				s += "  <TR>"
					+ "    <TD><FONT COLOR=RED><B>Error [1494602712] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
					+ "</TR>\n"
				;
			}
	    	s += "    </TD>";
    	}
    	
    	s += clsServletUtilities.createHTMLComment("Start the applied documents table here.");
    	s += "    <TD style = \" vertical-align:top; \" >";
    	try {
    		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE){
    			s += displayInvoiceDetailLines(entry, bEditable, sm, bAllowPOViewing, bAllowPOReceiptViewing);
    		}else{
    			s += displayDebitOrCreditDetailLines(entry, bEditable, sm);
    		}
		} catch (Exception e) {
			s += "  <TR>"
				+ "    <TD><FONT COLOR=RED><B>Error [1494602713] displaying detail lines - " + e.getMessage() + "</B></FONT></TD\n"
				+ "</TR>\n"
			;
		}
//    	s += "    </TD>";
//    	s += "  </TR>";
//    	s += "</TABLE>";
    	
		return s;
	}
	private String displayInvoiceDetailLines(
		APBatchEntry entry, 
		boolean bEditable, 
		SMMasterEditEntry smmastereditentry,
		boolean bAllowPOViewing,
		boolean bAllowPOReceiptViewing
		) throws Exception{
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
					+ " PO#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Receipt #</TD>\n";
			
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
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ " PO#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Receipt #</TD>\n";
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
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
						)
			);
			while(rsDistCodes.next()){
				//arrDistCodeIDs.add(rsDistCodes.getString(SMTableapdistributioncodes.lid));
				arrDistCodeNames.add(rsDistCodes.getString(SMTableapdistributioncodes.sdistcodename));
			}
			rsDistCodes.close();
		} catch (Exception e) {
			throw new Exception("Error [1490739516] reading distribution codes - " + e.getMessage());
		}
		
		SQL = "SELECT"
				+ " " + SMTableglaccounts.sAcctID
				+ ", " + SMTableglaccounts.sDesc
				+ " FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.lActive + " = 1)"
				+ ")"
				+ " ORDER BY " + SMTableglaccounts.sAcctID
			;

		ArrayList<String> arrGLAccts = new ArrayList<String>(0);
		ArrayList<String> arrGLDescriptions = new ArrayList<String>(0);
		
		try {
			ResultSet rsGLs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smmastereditentry.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + smmastereditentry.getUserID()
					+ " - "
					+ smmastereditentry.getFullUserName()
					);
    		//First, add a blank item so we can be sure the user chose one:
    		arrGLAccts.add("");
    		arrGLDescriptions.add("*** Select GL ***");
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
			s += "<B>Error [1491417541] reading GL info - " + e.getMessage() + "</B><BR>";
		}
		
		//Load the lines for the current entry:
		String sBackgroundColor = "";
		boolean bOddRow = true;
		for (int i = 0; i < entry.getLineArray().size(); i++){
			APBatchEntryLine line = entry.getLineArray().get(i);
			sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}

			String sLineText = "";
			
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n"
			;
			
			//sLineText += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
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
			//We have to handle the first line number carefully, because it COULD be a 'default' line automatically added for this vendor,
			//and so if it hasn't been saved, it has a line number of -1:
			String sLineNumber = "NEW";
			if (line.getslinenumber().compareToIgnoreCase("-1") != 0){
				sLineNumber = line.getslinenumber();
			}
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ sLineNumber 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// Distribution acct:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "\n<SELECT NAME = \"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct + "\""
						+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTableapbatchentrylines.sdistributionacct + "\""
						+ " onchange=\"flagDirty();\""
						 + " >\n"
					;
				
				String sBuffer = "";
				String sGLAcctSelections = "";
				int iCounter = 0;
					for (int j = 0; j < arrGLAccts.size(); j++){
						sBuffer += "<OPTION";
						if (arrGLAccts.get(j).toString().compareTo(line.getsdistributionacct()) == 0){
							sBuffer += " selected=yes";
						}
						sBuffer += " VALUE=\"" + arrGLAccts.get(j).toString() + "\">" + arrGLDescriptions.get(j).toString() + "\n";
						
						if ((iCounter % 50) == 0){
							sGLAcctSelections += sBuffer;
							sBuffer = "";
						}
					}
				sLineText += sGLAcctSelections;
				sLineText += "</SELECT>"
				;
				/*
				sLineText += "<INPUT TYPE=TEXT"
					+ " NAME=\"" + LINE_NUMBER_PARAMETER 
					+ SMUtilities.PadLeft(line.getslinenumber().trim(), "0", LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdistributionacct + "\""
					+ " ID=\"" + LINE_NUMBER_PARAMETER 
					+ SMUtilities.PadLeft(line.getslinenumber().trim(), "0", LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.sdistributionacct + "\""
			    	+ " VALUE=\"" + SMUtilities.filter(line.getsdistributionacct()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTableapbatchentrylines.sdistributionacctLength)
				    + " SIZE = " + "15"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
				*/
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
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(line.getsbdamount()) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsbdamount())
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(line.getsbdamount()) + "\""
			    	+ ">"
				;
			}
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
			
			//PO ID:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lpoheaderid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslpoheaderid()) + "\""
				    + " MAXLENGTH=" + "10"
				    + " SIZE = " + "9"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
	    		String sPurchaseOrderLink = entry.getslpurchaseordernumber();
	    		if(bAllowPOViewing && entry.getslpurchaseordernumber().compareToIgnoreCase("0") != 0){
					sPurchaseOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smic.ICEditPOEdit"
						+ "?" + SMTableicpoheaders.lid + "=" + entry.getslpurchaseordernumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
						+ "\">" + entry.getslpurchaseordernumber() + "</A>"
					;
				}
	    		sLineText += sPurchaseOrderLink;
			}
			sLineText += "</TD>\n";
			
			// Receipt number:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.lreceiptheaderid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslreceiptheaderid()) + "\""
				    + " MAXLENGTH=" + "10"
				    + " SIZE = " + "9"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
	    		String sReceiptNumberLink = line.getslreceiptheaderid();
	    		if(bAllowPOReceiptViewing && line.getslreceiptheaderid().compareToIgnoreCase("0") != 0){
	    			sReceiptNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smic.ICEditReceiptEdit"
						+ "?" + SMTableicporeceiptheaders.lpoheaderid + "=" + line.getslpoheaderid()
						+ "&" + SMTableicporeceiptheaders.lid + "=" + line.getslreceiptheaderid()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
						+ "\">" + line.getslreceiptheaderid() + "</A>"
					;
				}
	    		sLineText += sReceiptNumberLink;
			}
			sLineText += "</TD>\n";

			if (bEditable){
				sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ createRemoveLineButton(line.getslinenumber()) + "</TD>\n";
			}
			
			sLineText += "  </TR>\n";
			bOddRow = !bOddRow;
			
			//Add the buffer into the main string:
			s += sLineText;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			//NOTE: we ONLY add a new blank line if there is NOT already an UNSAVED 'default' line in the array:
			APBatchEntryLine line = new APBatchEntryLine();
			line.setsbatchnumber(entry.getsbatchnumber());
			line.setsentrynumber(entry.getsentrynumber());
			line.setslinenumber("0");
			
			sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}

			s += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n"
			;
			
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
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.bdamount + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsbdamount()) + "\""
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
			
			//PO ID:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lpoheaderid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslpoheaderid()) + "\""
			    + " MAXLENGTH=" + "10"
			    + " SIZE = " + "9"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			// Receipt number:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lreceiptheaderid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslreceiptheaderid()) + "\""
			    + " MAXLENGTH=" + "10"
			    + " SIZE = " + "9"
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
	private String displayDebitOrCreditDetailLines(APBatchEntry entry, boolean bEditable, SMMasterEditEntry smmastereditentry) throws Exception{
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
					+ "Applied&nbsp;Amount</TD>\n";
			
			//Apply-to doc #
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Apply-to&nbsp;#</TD>\n";
			
			//Original apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Original&nbsp;Amt</TD>\n";
			
			//Current apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Current&nbsp;Amt</TD>\n";
			
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
				+ "Applied&nbsp;Amount</TD>\n";
			
			//Apply-to doc #
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Apply-to&nbsp;#</TD>\n";
			
			//Original apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Original&nbsp;Amt</TD>\n";
			
			//Current apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Current&nbsp;Amt</TD>\n";
			
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
				SMUtilities.getFullClassName(this.toString() + ".displayDetailLines - Distribution codes - user: " + smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
				)
			);
			while(rsDistCodes.next()){
				//arrDistCodeIDs.add(rsDistCodes.getString(SMTableapdistributioncodes.lid));
				arrDistCodeNames.add(rsDistCodes.getString(SMTableapdistributioncodes.sdistcodename));
			}
			rsDistCodes.close();
		} catch (Exception e) {
			throw new Exception("Error [1490739516] reading distribution codes - " + e.getMessage());
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
					+ " - " + smmastereditentry.getFullUserName());
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
			s += "<B>Error [1491417541] reading GL info - " + e.getMessage() + "</B><BR>";
		}
		
		//Load the lines for the current entry:
		String sBackgroundColor = "";
		boolean bOddRow = true;
		boolean bAllowTransactionInformationViewing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.APViewTransactionInformation, 
			smmastereditentry.getUserID(),
			getServletContext(), 
			smmastereditentry.getsDBID(), 
			smmastereditentry.getLicenseModuleLevel()
		);
		for (int i = 0; i < entry.getLineArray().size(); i++){
			String sLineText = "";
			
			APBatchEntryLine line = entry.getLineArray().get(i);
			
			sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}

			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n"
			;
			
			//Read the AP transaction to get the date values, etc:
			String sOriginalAmt = "N/A";
			String sCurrentAmt = "N/A";
			SQL = "SELECT"
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
				SMUtilities.getFullClassName(this.toString()) + ".displayDetailLines - user: " + smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			if (rs.next()){
				sOriginalAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt));
				sCurrentAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt));
				
				@SuppressWarnings("unused")
				BigDecimal bdAppliedAmt = new BigDecimal("0.00");
				try {
					bdAppliedAmt = new BigDecimal(line.getsbdamount().trim().replace(",", ""));
				} catch (Exception e) {
					bdAppliedAmt = new BigDecimal("0.00");
				}
			}
			
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
				sLineText += "</SELECT>"
			;

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
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			String sLineAmt = line.getsbdamount();
			boolean bIsCreditNote = entry.getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)) == 0;
	    	if (bIsCreditNote){
	    		//In this case we'll want to show the typically negative amounts as positive to the user:
	    		try {
					BigDecimal bdTemp = new BigDecimal(sLineAmt.trim().replace(",", "")).negate();
					sLineAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTemp);
				} catch (Exception e) {
					//If we can't parse a valid amount out of the amount string, just show it as is on the screen:
					sLineAmt = line.getsbdamount();
				}
	    	}
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(sLineAmt) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(sLineAmt)
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTableapbatchentrylines.bdamount + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(sLineAmt) + "\""
			    	+ ">"
				;
			}
			
			sLineText += "</TD>\n";
			
			//Apply-to doc #:
			String sApplyToDocNumber = line.getsapplytodocnumber();
			if (bAllowTransactionInformationViewing){
				sApplyToDocNumber =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewTransactionInformation?"
					+ SMTableaptransactions.lid + "=" + line.getslapplytodocid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
					+ "\">" + sApplyToDocNumber + "</A>"
				;
			}
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += sApplyToDocNumber;
			
			//We'll also store our hidden fields here:
			sLineText += "<INPUT TYPE=HIDDEN\n"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lapplytodocid + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.lapplytodocid + "\""
				+ " VALUE=\"" + line.getslapplytodocid() + "\""
		    	+ ">"
			;
			
			sLineText += "<INPUT TYPE=HIDDEN\n"
				+ " NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " ID=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.sapplytodocnumber + "\""
				+ " VALUE=\"" + line.getsapplytodocnumber() + "\""
		    	+ ">"
			;
			
			sLineText += "</TD>\n";
			
			//Original apply-to amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sOriginalAmt, " ", NUMBER_PADDING_LENGTH);
			sLineText += "</TD>\n";
			
			//Current apply-to amt:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += clsStringFunctions.PadLeft(sCurrentAmt, " ", NUMBER_PADDING_LENGTH);
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
			bOddRow = !bOddRow;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			APBatchEntryLine line = new APBatchEntryLine();
			line.setsbatchnumber(entry.getsbatchnumber());
			line.setsentrynumber(entry.getsentrynumber());
			line.setslinenumber("0");
			
			sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}

			s += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
				+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
				+ ">\n"
			;
			
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
			s += "</SELECT>";
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
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsbdamount()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "12"
			    + " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Apply-to doc #
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "&nbsp;</TD>\n";
			
			//Original apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "&nbsp;</TD>\n";
			
			//Current apply-to doc amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "&nbsp;</TD>\n";
			
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
	private String displayUnappliedDocumentsForDebitsAndCredits(APBatchEntry entry, SMMasterEditEntry smmastereditentry, boolean bEditable) throws Exception{
		String s = "";
		
		//Should we display the table or not?
		
    	String sDisplayToggle = "none";
		if (clsManageRequestParameters.get_Request_Parameter(PARAM_TOGGLEUNAPPLIEDTABLE, smmastereditentry.getRequest()).compareToIgnoreCase(PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_DISPLAY) == 0){
			sDisplayToggle = "block";
		}
		
		s += "<DIV ID=\"" + UNAPPLIED_DOCUMENTS_TABLE_CONTAINER + "\" style = \"display:" + sDisplayToggle +"; \">\n";
		//Print headings:
		s += "<BR><B><I><U>APPLY-TO DOCUMENTS</U></I></B> - <I>(Click on the headings to sort)</I>\n";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\""
			+ " ID = \"" + TABLE_UNAPPLIED_DOCUMENTS + "\"\n"
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
		
		String SQL = "SELECT"
			+ " * FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
		;
		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE){
			SQL += " AND (" 
					+ "(" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE) + ")"
					+ " OR (" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE) + ")"
				+ ")"
			;
		}

		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE){
			SQL += " AND (" 
					+ "(" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE) + ")"
					+ " OR (" + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE) + ")"
				+ ")"
			;
		}
		
		SQL += " AND (" + SMTableaptransactions.bdcurrentamt + " != 0.00)"
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
				//if the document is already applied on this payment, then don't list it:
				//boolean bDocumentIsAlreadyApplied = false;
				for (int i = 0; i < entry.getLineArray().size(); i++){
					APBatchEntryLine line = entry.getLineArray().get(i);
					if (line.getsapplytodocnumber().compareToIgnoreCase(rs.getString(SMTableaptransactions.sdocnumber)) == 0){
						//bDocumentIsAlreadyApplied = true;
						break;
					}
				}
				
				// TJR - 6/15/2017 - we'll allow the user to apply to the same document as many times as needed:
				//if(!bDocumentIsAlreadyApplied){
					String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
					if (bOddRow){
						sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
					}
					s += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
						+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
						+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
						+ ">\n";
					
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdocdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datduedate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";
	
					String sDocNumberLink = rs.getString(SMTableaptransactions.sdocnumber);
					if(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APControlPayments, 
						smmastereditentry.getUserID(), 
						getServletContext(), 
						smmastereditentry.getsDBID(), 
						(String) smmastereditentry.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
						) 
					){
						sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smap.APControlPaymentsEdit"
							+ "?" + SMTableaptransactions.svendor + "=" + entry.getsvendoracct()
							+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumberLink
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smmastereditentry.getsDBID()
							+ "\">" + sDocNumberLink + "</A>"
						;
					}
					
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ sDocNumberLink
							+ "</TD>\n";
					
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";
	
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";

					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdiscountdate), "M/d/yyyy", "00/00/0000")
							+ "</TD>\n";

					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ clsStringFunctions.PadLeft(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable)), " ", NUMBER_PADDING_LENGTH)
							+ "</TD>\n";
					
					String sOnHold = "N";
					if (rs.getInt(SMTableaptransactions.ionhold) == 1){
						sOnHold = "<FONT COLOR=RED>Y</FONT>";
					}
					s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<B>" 
						+ sOnHold 
						+ "</B>"
						+ " <INPUT TYPE=HIDDEN"
						+ " NAME=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " ID=\"" + rs.getString(SMTableaptransactions.sdocnumber) + SMTableaptransactions.ionhold + "\""
						+ " VALUE=\"" + Integer.toString(rs.getInt(SMTableaptransactions.ionhold)) + "\""
						+ "</TD>\n";
					
					if (bEditable){
						s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >"
							+ createApplyLineButton(rs.getString(SMTableaptransactions.sdocnumber)) + "</TD>\n";
					}
					
					s += "  </TR>\n";
					bOddRow = !bOddRow;
				//}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1492106107] reading open invoices for vendor '" + entry.getsvendoracct() + " with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		s += "</TABLE>\n";
		s += "</DIV><BR>";
		return s;
	}
	private void calculateTerms(SMMasterEditEntry smedit, APBatchEntry entry) throws Exception{
    	try {
    		APTermsCalculator calc = new APTermsCalculator(
    			entry.getsterms(),
    			entry.getsentryamount(),
    			entry.getsdatdocdate());
    		calc.calculateTerms(getServletContext(), smedit.getsDBID(), smedit.getUserID());
    		entry.setsdiscountamt(calc.getDiscountAmountString());
    		entry.setsdatdiscount(calc.getDiscountDateString());
    		entry.setsdatduedate(calc.getDueDateString());
		} catch (Exception e) {
			throw new  Exception(e.getMessage());
		}
	}
	private String sCommandScript(String sDBID, SMMasterEditEntry sm, APBatchEntry entry) throws Exception{
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
/*
		//testing type ahead:
		s += "\n\n<script type='text/javascript' src='/sm/scripts/jquery-3.2.0.js'></script>";
		s += "\n\n<script type='text/JavaScript' src='/sm/scripts/typeahead.bundle.js'></script>\n";
		s += "<script type='text/JavaScript' src='/sm/scripts/typeahead.bundle.min.js'></script>\n";
		s += "<script type='text/JavaScript' src='/sm/scripts/typeahead.jquery.js'></script>\n";
		s += "<script type='text/JavaScript' src='/sm/scripts/typeahead.jquery.min.js'></script>\n";
		s += "<script type='text/JavaScript' src='/sm/scripts/jquery-ui.js'></script>\n";
		
		
		s += "<script type='text/JavaScript' src='/sm/scripts/horsey.js'></script>\n";
		
		s += "<script type='text/JavaScript' src='/sm/scripts/jquery.autocomplete.js'></script>\n";
		
		
		s += "<script type='text/javascript'>\n";
		
		
		s += "$(document).ready(function() {\n"
		    
		    + "var aTags = [\"ask\",\"always\", \"all\", \"alright\", \"one\", \"foo\", \"blackberry\", \"tweet\",\"force9\", \"westerners\", \"sport\"];\n"

		    + "$( \"#tags\" ).autocomplete({\n"
		        + " source: aTags\n"
		    + "});\n"
		    
		+ "});\n"
		;
		
		s += "var countries = [\n"
			+ "  { value: 'Andorra', data: 'AD' },\n"
			// ...
			+ "  { value: 'Zimbabwe', data: 'ZZ' }\n"
			+ "];\n"

			+ "$('#autocomplete').autocomplete({\n"
			+ "  lookup: countries,\n"
			+ "  onSelect: function (suggestion) {\n"
			+ "    alert('You selected: ' + suggestion.value + ', ' + suggestion.data);\n"
    		+ "  }\n"
			+ "});\n"
		;
		
		s += "var substringMatcher = function(strs) {\n"
			+ "  return function findMatches(q, cb) {\n"
			+ "    var matches, substringRegex;\n"
		    // an array that will be populated with substring matches
			+ "      matches = [];\n"
			// regex used to determine if a string contains the substring `q`
			+ "      substrRegex = new RegExp(q, 'i');\n"
		    // iterate through the pool of strings and for any string that
		    // contains the substring `q`, add it to the `matches` array
		    + "      $.each(strs, function(i, str) {\n"
			+ "        if (substrRegex.test(str)) {\n"
			+ "          matches.push(str);\n"
			+ "        }\n"
			+ "      });\n"
			+ "      cb(matches);\n"
			+ "    };\n"
			+ "};\n\n"

			+ "var states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California',\n"
			+ "'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii',\n"
			+ "'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana',\n"
			+ "'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota',\n"
			+ "'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire',\n"
			+ "'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota',\n"
			+ "'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island',\n"
			+ "'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont',\n"
			+ "'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'\n"
			+ "];\n\n"

			+ "$('#the-basics.typeahead').typeahead({\n"
			+ "  hint: true,\n"
			+ "  highlight: true,\n"
			+ "  minLength: 1\n"
			+ "},\n"
			+ "{\n"
			+ "  name: 'states',\n"
			+ "  source: substringMatcher(states)\n"
			+ "});\n"
		;
			s += "</script>\n\n";
*/		
		
		s += "<script type='text/javascript'>\n";
		
		s += "window.onload = function() {\n"
			+ "    initShortcuts();\n"
			+ "    calculatelinetotal();\n"
			+ "    jumpToBookmark('" + BOOKMARK_TOP_OF_TABLES + "'); \n"
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

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
		
		s += "function promptToSave(){\n"		
			//Don't prompt on these functions:
			//+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_FINDVENDOR + "\" ){\n"
			//+ "        return;\n"
			//+ "    }\n"
			//+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_FINDAPPLYTODOCUMENTNUMBER + "\" ){\n"
			//+ "        return;\n"
			//+ "    }\n"
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_UPDATEVENDORINFO + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_CALCULATETERMS + "\" ){\n"
			+ "        return;\n"
			+ "    }\n"
			
			//If the record WAS changed, then
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				//If is was anything but the 'SAVE' command that triggered this function...
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ COMMAND_VALUE_SAVE + "\" ){\n"
						//Prompt to see if the user wants to continue
			+ "        return 'You have unsaved changes.';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;

		s += "function jumpToBookmark(bookmarkID){ \n"
			+ "    var objTopOfTablesBookmark = document.getElementById(bookmarkID); \n"
			+ "    if (objTopOfTablesBookmark != null){ \n"
			+ "        var top = document.getElementById(bookmarkID).offsetTop; \n"
			+ "        window.scrollTo(0, top); \n"
			+ "    } \n"
			+ "} \n"
		;
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
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
		
		//Remove a detail line:
		s += "function removeLine(sLineNumber){\n"
			//+ "    if (!bScreenIsFullyDisplayed()){\n"
			//+ "        return;\n"
			//+ "    }\n"
			//+ "    alert('test');"
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
		
		s += "function editLine(sLineNumber){\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before editing a line.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_REMOVELINE + "\";\n"
			+ "    document.getElementById(\"" + LINE_NUMBER_TO_DELETE_PARAM + "\").value = \"" + "sLineNumber" + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
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
			+ "    if (confirm('Are you sure you want to delete this " + "invoice" + "?')){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		//Find Vendor:
		 s += "function findVendor(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
		    + COMMAND_VALUE_FINDVENDOR + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n";
		
		//Calculate terms:
		 s += "function calculateTerms(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
		    + COMMAND_VALUE_CALCULATETERMS + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n";
		 
		//Update vendor info:		 
		 s += "function updateVendorInfo(){\n"
			    + "    flagDirty();"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
			    + COMMAND_VALUE_UPDATEVENDORINFO + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n";
		 
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
		
		s += "function distCodeChange(selectObj, targetcontrolname) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		+ "    if (which != '' && which != '0'){\n"
		+ "        document.getElementById(targetcontrolname).value = sdistributioncodeaccounts[which];\n"
		+ "    }\n"
		+ "    flagDirty();\n"
		+ "}\n\n"; 
		
		s += "function updateLineTotal(){\n"
			+ "    calculatelinetotal();"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		s += "function calculatelinetotal(){\n"
			+ "    //Turn off the line amt warning by default:\n"
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
   			+ "            //If the string ENDS with the field name '" + SMTableapbatchentrylines.bdamount + "', then it's a line amount:\n"
   			+ "            if (testName.endsWith(\"" + SMTableapbatchentrylines.bdamount + "\") == true){\n"
   			+ "                //Add it to the line total:\n"
   			+ "                temp = document.getElementById(testName).value.replace(',','');\n"
   			+ "                if (temp != ''){\n"
   			+ "                    if(!isNaN(temp)){\n"
   			+ "                        linetotal = linetotal + getFloat(temp);\n"
   			+ "                    }\n"
   			+ "                }\n"
   			+ "            }\n"
   			+ "        }\n"
   			+ "    }\n"
   			;
		//Calculate and display the line totals:
		s += "    if (!floatsAreEqual(linetotal, entryamt)){\n"
   			+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD + "\").innerText=linetotal.toFixed(2);\n"
   			+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.color= \"red\"\n"
   			+ "    }else{\n"
   			+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD + "\").innerText=linetotal.toFixed(2);\n"
   			+ "        document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.color= \"black\"\n"
   			+ "    }\n"
   		;

		s += "}\n"
   		;
		
		s += "function sortUnappliedTable(nSortColumnIndex) { \n"
				+ "    sortTable(\"" + TABLE_UNAPPLIED_DOCUMENTS + "\", nSortColumnIndex, 1); \n"
				+ "    configUnappliedTable(); \n"
				+ "} \n"
			;
		
		//Make the 'Unapplied table' visible or invisible:
		s += "function toggleUnappliedDocsTable() {\n"
			+ "    var TableContainer = document.getElementById(\"" + UNAPPLIED_DOCUMENTS_TABLE_CONTAINER + "\");\n"
			+ "    if(TableContainer.style.display == \"block\"){\n"
			+ "        TableContainer.style.display = \"none\";\n"
			+ "        document.getElementById(\"" + BUTTON_NAME_TOGGLEUNAPPLIEDTABLE + "\").innerHTML=\"" + VIEW_APPLY_TO_DOCUMENTS_BUTTON_LABEL + "\";\n"
			+ "        document.getElementById(\"" + PARAM_TOGGLEUNAPPLIEDTABLE + "\").value=\"" + PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_HIDE + "\";\n"
			+ "    }else{\n"
			+ "        TableContainer.style.display = \"block\";\n"
			+ "        document.getElementById(\"" + BUTTON_NAME_TOGGLEUNAPPLIEDTABLE + "\").innerHTML=\"" + HIDE_APPLY_TO_DOCUMENTS_BUTTON_LABEL + "\";\n"
			+ "        document.getElementById(\"" + PARAM_TOGGLEUNAPPLIEDTABLE + "\").value=\"" + PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_DISPLAY + "\";\n"
			+ "    }"
			+ "}"
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
		
		//HIDE apply-to docs:
		s += "    shortcut.add(\"Alt+h\",function() {\n";
		s += "        toggleUnappliedDocsTable();\n";
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
		
		s += "    shortcut.add(\"Alt+t\",function() {\n";
		s += "        calculateTerms();\n";
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
		
		s += "    shortcut.add(\"Alt+w\",function() {\n";
		s += "        toggleUnappliedDocsTable();\n";
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
	private String createSaveAndAddButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_AND_ADD_NEW_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_AND_ADD_NEW_BUTTON_LABEL + "\""
				+ " onClick=\"saveandadd();\">"
				+ UPDATE_AND_ADD_NEW_BUTTON_LABEL
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
	private String createFindVendorButton(){
		return  "<button type=\"button\""
				+" value=\"" + FIND_VENDOR_BUTTON_LABEL + "\""
				+ "name=\"" + FIND_VENDOR_BUTTON_LABEL + "\""
				+ " onClick=\"findVendor();\">"
				+  FIND_VENDOR_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createCalculateTermsButton(){
		return  "<button type=\"button\""
				+" value=\"" + CALCULATE_TERMS_BUTTON_LABEL + "\""
				+ "name=\"" + CALCULATE_TERMS_BUTTON_LABEL + "\""
				+ " onClick=\"calculateTerms();\">"
				+  CALCULATE_TERMS_BUTTON_LABEL
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

	private String createToggleUnappliedTableButton(SMMasterEditEntry sm){
		
		String sButtonLabel = VIEW_APPLY_TO_DOCUMENTS_BUTTON_LABEL;
		if (clsManageRequestParameters.get_Request_Parameter(PARAM_TOGGLEUNAPPLIEDTABLE, sm.getRequest()).compareToIgnoreCase(PARAM_TOGGLEUNAPPLIEDTABLE_VALUE_DISPLAY) == 0){
			//If the table is DISPLAYED, then we need to label the button with the 'Hide table' label:
			sButtonLabel = HIDE_APPLY_TO_DOCUMENTS_BUTTON_LABEL;
		}
		return "<button type=\"button\""
				+ " value=\"" + sButtonLabel + "\""
				+ " name=\"" + BUTTON_NAME_TOGGLEUNAPPLIEDTABLE + "\""
				+ " ID=\"" + BUTTON_NAME_TOGGLEUNAPPLIEDTABLE + "\""
				+ " onClick=\"toggleUnappliedDocsTable();\">"
				+ sButtonLabel
				+ "</button>\n"
				;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
