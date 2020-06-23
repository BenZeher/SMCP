package smap;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableaptransactions;
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

public class APEditReversalEdit  extends HttpServlet {

	public static final String BUTTON_LABEL_REMOVELINE = "Remove";
	public static final String COMMAND_VALUE_REMOVELINE = "RemoveLine";
	public static final String BUTTON_LABEL_EDITLINE = "Edit";
	public static final String COMMAND_VALUE_EDITLINE = "EditLine";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String LINE_NUMBER_TO_DELETE_PARAM = "LineNumberParam";
	public static final String BUTTON_LABEL_SAVE = "Update";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String BUTTON_LABEL_DELETE = "Delete";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	public static final String BUTTON_LABEL_SAVE_AND_ADD = "Update and add new";
	public static final String COMMAND_VALUE_SAVE_AND_ADD = "Updateandaddnew";
	
	public static final String BUTTON_LABEL_FINDVENDOR = "Find vendor";
	public static final String COMMAND_VALUE_FINDVENDOR = "FINDVENDOR";
	public static final String RETURNING_FROM_FIND_VENDOR_FLAG = "RETURNINGFROMFINDER";
	public static final String FOUND_VENDOR_PARAMETER = "FOUND" + SMTableapbatchentries.svendoracct;
	
	public static final String BUTTON_LABEL_UPDATEVENDORINFO = "Update vendor info";
	public static final String COMMAND_VALUE_UPDATEVENDORINFO = "UPDATEVENDORINFO";
	public static final String UPDATED_VENDOR_INFO_PARAMETER = "UPDATED" + SMTableapbatchentries.svendoracct + "INFO";
	public static final String RETURNING_FROM_UPDATEVENDORINFO_FLAG = "RETURNINGFROMUPDATEVENDORINFO";
	
	public static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW"; //"#FF2080";
	public static final int NUMBER_PADDING_LENGTH = 11;
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	
	public static final String CALCULATED_LINE_TOTAL_FIELD = "CALCULATEDLINETOTAL";
	public static final String CALCULATED_LINE_TOTAL_FIELD_CONTAINER = "CALCULATEDLINETOTALCONTAINER";
	
	//Hot keys:
	public static final String FIND_VENDOR_BUTTON_LABEL = "<B><FONT COLOR=RED>F</FONT></B>ind vendor"; // F
	public static final String UPDATE_VENDOR_INFO_BUTTON_LABEL = "Update <B><FONT COLOR=RED>v</FONT></B>endor info"; // V
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; // U
	public static final String UPDATE_AND_ADD_NEW_BUTTON_LABEL = "Update and add <B><FONT COLOR=RED>n</FONT></B>ew"; // N
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; // D
	
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
				"smap.APEditReversalAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditBatches
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APReverseChecks)){
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
	    	//Try to update the lines on the reversal for this vendor and check:
	    	try {
				entry.update_check_reversal_data(smedit.getsDBID(),  getServletContext(), smedit.getUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error reading check information for vendor '" 
					+ entry.getsvendoracct() + "', check number '" + entry.getschecknumber() + "' - " + e.getMessage()
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
	    	//Try to update the lines on the reversal for this vendor and check:
	    	try {
				entry.update_check_reversal_data(smedit.getsDBID(),  getServletContext(), smedit.getUserName());
			} catch (Exception e) {
				smedit.getPWOut().println("<BR><FONT COLOR=RED><B>Error reading check information for vendor '" 
					+ entry.getsvendoracct() + "', check number '" + entry.getschecknumber() + "' - " + e.getMessage()
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
	    		+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch " + entry.getsbatchnumber() + "</A><BR>\n");
		
	    APBatch batch = new APBatch(entry.getsbatchnumber());
	    try {
			batch.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
		} catch (Exception e1) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
				+ "?" + SMTableapbatches.lbatchnumber + "=" + entry.getsbatchnumber()
				+ "&Warning=Error [1511370898] could not load batch number " + entry.getsbatchnumber() + " - " + e1.getMessage()
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
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, APBatchEntry entry, boolean bEditable) throws SQLException{
		String s = "";
		try {
			s += sCommandScript(sm.getsDBID(), sm, entry);
		} catch (Exception e2) {
			s += "<BR><FONT COLOR=RED><B>" + e2.getMessage() + "</B></FONT><BR>";
		}
		
		//Store some command values here:
		//Store whether or not the record has been changed:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//Store which line the user has chosen to delete:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + LINE_NUMBER_TO_DELETE_PARAM + "\""
		+ "\">";
		
		//Store hidden entry values here:
		//Store the GL control account, which the user shouldn't be changing on the screen:
		s += "\n<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTableapbatchentries.scontrolacct + "\""
			+ " ID=\"" + SMTableapbatchentries.scontrolacct + "\""
			+ " VALUE=\"" + entry.getscontrolacct() + "\" >";
		
		s += "\n<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTableapbatchentries.bddiscount + "\""
			+ " ID=\"" + SMTableapbatchentries.bddiscount + "\""
			+ " VALUE=\"" + entry.getsdiscountamt() + "\" >";
		
		s += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.bdtaxrate + "\""
				+ " ID=\"" + SMTableapbatchentries.bdtaxrate + "\""
				+ " VALUE=\"" + entry.getsbdtaxrate() + "\" >";
		
		s += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.datdiscount + "\""
				+ " ID=\"" + SMTableapbatchentries.datdiscount + "\""
				+ " VALUE=\"" + entry.getsdatdiscount() + "\" >";
		
		s += "\n<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + SMTableapbatchentries.datduedate + "\""
				+ " ID=\"" + SMTableapbatchentries.datduedate + "\""
				+ " VALUE=\"" + entry.getsdatduedate() + "\" >";
		
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
		}else{
			s += "\n" + "<INPUT TYPE=HIDDEN NAME=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\""
				+ " id=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\""
				+ " VALUE=\"" + "Y" + "\""
				+ ">"
			;
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
    			
       //Check number:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Check&nbsp;number to reverse:&nbsp;</TD>\n";
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatchentries.schecknumber + "\""
	    	    + " VALUE=\"" + clsStringFunctions.filter(entry.getschecknumber()) + "\""
	    	    + " MAXLENGTH=" + Integer.toString(SMTableapchecks.schecknumberLength)
	    	    + " SIZE = " + "8"
	    	    + " onchange=\"flagDirty();\""
	    	    + ">"
    	    ;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.schecknumber + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getschecknumber()) + "\""
    	    	+ ">"
    	    	+ entry.getschecknumber()
    	    ;
    	}
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";

    	s += "  </TR>" + "\n";
    	
    	s += "  <TR>" + "\n";
    	
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
    	
    	s += "  </TR>" + "\n";
    	
    	//Amt:
    	//Hide the 'sign' on the number while it's displayed:
    	s += "  <TR>\n"
	    	+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Amount:&nbsp;</TD>\n"
	    ;
		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatchentries.bdentryamount + "\""
			+ " ID=\"" + SMTableapbatchentries.bdentryamount + "\""
    		+ " VALUE=\"" + entry.getsentryamount() + "\""
    		+ ">"
    	;
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" 
    		+ sControlHTML 
    		+ "<div style = \"" + " display: inline; color: black" + "\" id=\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\"><label id=\"" + CALCULATED_LINE_TOTAL_FIELD + "\" >" 
			+ "" + "</label></div></TD>\n";
    	
    	//Document date:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Document&nbsp;date:&nbsp;</TD>\n";
    	
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
    	
    	s += "</TABLE>\n";
    	
    	//Display the first row of control buttons:
    	if (bEditable){
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton() + "\n";
    	}
    	
    	s += "<BR>\n";
    	
		s += clsServletUtilities.createHTMLComment("Start the reversals list table here.");
		try {
			s += displayCheckReversalLines(entry, bEditable, sm);
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED><B>Error [1489683067] displaying detail lines - " + e.getMessage() 
				+ "</B></FONT><BR>\n"
				;
		}
		return s;
	}
	private String displayCheckReversalLines(
		APBatchEntry entry, 
		boolean bEditable, 
		SMMasterEditEntry smmastereditentry
		) throws Exception{
		String s = "";
		
		//System.out.println("[1490927362] entry.getLineArray().size(): " + entry.getLineArray().size());
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Line&nbsp;#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Invoice date</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Invoice #</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Original amt</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Discount date</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Due date</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Current amt</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Applied amt</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Discount available</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Discount applied</TD>\n";

			
		s += "  </TR>\n";

		//Load the lines for the current entry:
		String sBackgroundColor = "";
		int iLineCounter = 0;
		//System.out.println("[1490927363] entry.getLineArray().size(): " + entry.getLineArray().size());
		for (int i = 0; i < entry.getLineArray().size(); i++){
			APBatchEntryLine line = entry.getLineArray().get(i);
			sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
			if ((iLineCounter % 2) == 1){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}
			
			s += "  <TR class = \"" + sBackgroundColor + " \">\n";
			
			String sInvoiceDate = SMUtilities.EMPTY_DATE_VALUE;
			String sOriginalInvoiceAmt = "0.00";
			String sDiscountDate = SMUtilities.EMPTY_DATE_VALUE;
			String sDueDate = SMUtilities.EMPTY_DATE_VALUE;
			String sCurrentInvoiceAmt = "0.00";
			String sInvoiceDiscountAvailableAmt = "0.00";
			//Get the information for the check line from the original:
			String SQL = "SELECT"
				+ " " + SMTableaptransactions.datdocdate
				+ ", " + SMTableaptransactions.bdoriginalamt
				+ ", " + SMTableaptransactions.datdiscountdate
				+ ", " + SMTableaptransactions.datduedate
				+ ", " + SMTableaptransactions.bdcurrentamt
				+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
				+ " FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
				+ ")"
			;
			ResultSet rsApplyToDoc = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smmastereditentry.getsDBID(), 
				"MySQL", SMUtilities.getFullClassName(this.toString()) + ".displayCheckReversalLines - user: " 
				+ smmastereditentry.getUserID()
				+ " - "
				+ smmastereditentry.getFullUserName()
					);
			if (rsApplyToDoc.next()){
				sInvoiceDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToDoc.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sOriginalInvoiceAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsApplyToDoc.getBigDecimal(SMTableaptransactions.bdoriginalamt));
				sDiscountDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToDoc.getString(SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToDoc.getString(SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				sCurrentInvoiceAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsApplyToDoc.getBigDecimal(SMTableaptransactions.bdcurrentamt));
				sInvoiceDiscountAvailableAmt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsApplyToDoc.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable));
			}
			
			//Line number:
			String sLineNumber = "NEW";
			if (line.getslinenumber().compareToIgnoreCase("-1") != 0){
				sLineNumber = line.getslinenumber();
			}
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ sLineNumber 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", APBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTableapbatchentrylines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			//Invoice date
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sInvoiceDate);  //Get the check date here
			s += "</TD>\n";
			
			//Invoice number
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(line.getsapplytodocnumber());  //Get the invoice number here
			s += "</TD>\n";
			
			//Original amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sOriginalInvoiceAmt);  //Get the check date here
			s += "</TD>\n";
			
			//Discount date
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sDiscountDate);  //Get the check date here
			s += "</TD>\n";
			
			//Due date
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sDueDate);  //Get the check date here
			s += "</TD>\n";
			
			//Current amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sCurrentInvoiceAmt);  //Get the check date here
			s += "</TD>\n";
			
			//Applied amt
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(line.getsbdamount());  //Get the check date here
			s += "</TD>\n";
			
			//Discount available
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(sInvoiceDiscountAvailableAmt);  //Get the check date here
			s += "</TD>\n";
			
			//Discount applied
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += clsStringFunctions.filter(line.getsbddiscountappliedamt());  //Get the check date here
			s += "</TD>\n";
			
			s += "  </TR>\n";
			iLineCounter++;
		}

		s += "</TABLE>\n";
		return s;
	}

	private String sCommandScript(String sDBID, SMMasterEditEntry sm, APBatchEntry entry) throws Exception{
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
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"		
			//Don't prompt on these functions:
			//+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_FINDVENDOR + "\" ){\n"
			//+ "        return;\n"
			//+ "    }\n"
			//+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  COMMAND_VALUE_FINDAPPLYTODOCUMENTNUMBER + "\" ){\n"
			//+ "        return;\n"
			//+ "    }\n"
			
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

		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
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
			+ "    if (confirm('Are you sure you want to delete this " + "check reversal" + "?')){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
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
			+ "    flagDirty(); \n"
			+ "} \n"
		;
		
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
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
		
		s += "</script>\n";
		return s;
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
	private String createUpdateVendorInfoButton(){
		return  "<button type=\"button\""
				+" value=\"" + UPDATE_VENDOR_INFO_BUTTON_LABEL + "\""
				+ "name=\"" + UPDATE_VENDOR_INFO_BUTTON_LABEL + "\""
				+ " onClick=\"updateVendorInfo();\">"
				+  UPDATE_VENDOR_INFO_BUTTON_LABEL
				+ "</button>\n";
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
