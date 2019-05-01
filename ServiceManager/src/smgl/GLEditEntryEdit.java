package smgl;

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
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditEntryEdit  extends HttpServlet {

	public static final String BUTTON_LABEL_REMOVELINE = "Remove";
	public static final String COMMAND_VALUE_REMOVELINE = "RemoveLine";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String ADDING_LINE_FLAG = "ADDINGLINE";
	public static final String LINE_NUMBER_TO_DELETE_PARAM = "LineNumberParam";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	public static final String COMMAND_VALUE_SAVE_AND_ADD_ENTRY = "Updateandaddnewentry";
	public static final String COMMAND_VALUE_SAVE_AND_ADD_LINE = "Updateandaddnewline";
	
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
	
	public static final String UPDATE_BUTTON_LABEL = "<B><FONT COLOR=RED>U</FONT></B>pdate"; // U
	public static final String UPDATE_AND_ADD_NEW_ENTRY_BUTTON_LABEL = "Update and add <B><FONT COLOR=RED>n</FONT></B>ew ENTRY"; // N
	public static final String UPDATE_AND_ADD_NEW_LINE_BUTTON_LABEL = "Updat<B><FONT COLOR=RED>e</FONT></B> and add new LINE"; // E
	
	public static final String FIND_ACCT_BUTTON_LABEL = "Find Acct";
	public static final String ACCT_DESC_LABEL_BASE = "ACCOUNTLABEL";
	
	public static final String BOOKMARK_TOP_OF_TABLES = "TopOfTables";
	public static final String RETURN_TO_TABLES_BOOKMARK = "RETURNTOTABLESPARAM";
	
	public static final String PARAM_FISCAL_YEAR_AND_PERIOD = "FISCALYEARANDPERIOD";
	public static final String FISCAL_YEAR_AND_PERIOD_DELIMITER = " - ";
	public static final String PARAM_SOURCE_LEDGER_AND_TYPE = "SOURCELEDGERANDTYPE";
	
	
	//Hot keys:
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; // D
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		GLTransactionBatchEntry entry = new GLTransactionBatchEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"GL Entry",
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLEditEntryAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditBatches
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditBatches)){
			smedit.getPWOut().println("Error [1555609737] in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//long lStartingtime = System.currentTimeMillis();
		
	    if (currentSession.getAttribute(GLTransactionBatchEntry.OBJECT_NAME) != null){
	    	entry = (GLTransactionBatchEntry) currentSession.getAttribute(GLTransactionBatchEntry.OBJECT_NAME);
	    	currentSession.removeAttribute(GLTransactionBatchEntry.OBJECT_NAME);
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
						+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
						+ "&Warning=" + e.getMessage()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
				}
	    	}
	    }
	    
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
	    smedit.printHeaderTable();
	    
	    smedit.setbIncludeDeleteButton(false);
	    smedit.setbIncludeUpdateButton(false);
	    
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to General Ledger Main Menu</A><BR>\n");
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditBatchesSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch List</A><BR>\n");
	    smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditBatchesEdit?"
	    		+ SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Batch " + entry.getsbatchnumber() + "</A><BR>\n");
		
	    GLTransactionBatch batch = new GLTransactionBatch(entry.getsbatchnumber());
	    try {
			batch.load(getServletContext(), smedit.getsDBID(), smedit.getUserName());
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
					+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
					+ "&Warning=Error [1555610014] could not load batch number " + entry.getsbatchnumber() + " - " + e1.getMessage()
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
				+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + entry.getsbatchnumber()
				+ "&Warning=Error editing entry ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, GLTransactionBatchEntry entry, boolean bEditable) throws SQLException{
		String s = "";
		String sControlHTML = "";
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
				
		s += "<BR><TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		
		s += "  <TR>\n";
		
		//Batchnumber:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" ><B>Batchnumber</B>:</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" ><B>" 
				+ entry.getsbatchnumber() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.lbatchnumber + "\" VALUE=\"" 
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
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.lentrynumber + "\" VALUE=\"" 
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
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD>\n"
		;
		
		//Auto reverse
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Auto-reverse?</TD>\n"
 		    ;
     	if (bEditable){
     		String sTemp = "";
     		if (entry.getsautoreverse().compareToIgnoreCase("0") != 0){
    			sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
    		}
     		sControlHTML = "<INPUT TYPE=CHECKBOX"
     			+ " NAME=\"" + SMTablegltransactionbatchentries.iautoreverse + "\""
     			+ " ID=\"" + SMTablegltransactionbatchentries.iautoreverse + "\""
     			+ " " + sTemp
 	    		+ " onchange=\"flagDirty();\""
 	    		+ ">"
 	    	;
     	}else{
     		String sAutoReverseValue = "N";
     		if (entry.getsautoreverse().compareToIgnoreCase("1") == 0){
     			sAutoReverseValue = "Y";
     		}
     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.iautoreverse + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsautoreverse()) + "\""
 	    		+ ">"
 	    		+ "<B>" + sAutoReverseValue + "</B>"
 	    	;
     	}
     	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";

		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		//Entry date:
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Entry&nbsp;date:&nbsp;</TD>\n";
    	
    	if (bEditable){
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatchentries.datentrydate + "\""
	    	    + " VALUE=\"" + clsStringFunctions.filter(entry.getsdatentrydate()) + "\""
	    	    + " MAXLENGTH=" + "10"
	    	    + " SIZE = " + "8"
	    	    + " onchange=\"flagDirty();\""
	    	    + ">"
	    	    + "&nbsp;" + SMUtilities.getDatePickerString(SMTablegltransactionbatchentries.datentrydate, getServletContext())
    	    ;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.datentrydate + "\""
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
    		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatchentries.datdocdate + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdocdate()) + "\""
    	    	+ " MAXLENGTH=" + "10"
    	    	+ " SIZE = " + "8"
    	    	+ " onchange=\"flagDirty();\""
    	    	+ ">"
    	    	+ "&nbsp;" + SMUtilities.getDatePickerString(SMTablegltransactionbatchentries.datdocdate, getServletContext())
    	    ;
    	}else{
    		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.datdocdate + "\""
    	    	+ " VALUE=\"" + clsStringFunctions.filter(entry.getsdatdocdate()) + "\""
    	    	+ ">"
    	    	+ entry.getsdatdocdate()
    	    ;
    	}

    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
    	
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
    	//Fiscal year and period
    	s += "  <TR>\n";
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Fiscal&nbsp;year/period:&nbsp;</TD>\n";
		
		if (bEditable){
			//int iCurrentFiscalYear = 0;
			//try {
			//	iCurrentFiscalYear = GLFiscalPeriod.getCurrentFiscalYear(sm.getsDBID(), sm.getFullUserName(), getServletContext());
			//} catch (Exception e) {
			//	s += "<BR><FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT><BR>";
			//}
			
			sControlHTML = "<SELECT NAME = \"" + PARAM_FISCAL_YEAR_AND_PERIOD + "\" >\n";
			sControlHTML += "<OPTION"
				+ " VALUE=\"" 
				+ "-1" 
				+ "\">" 
				+ "** SELECT FISCAL YEAR/PERIOD **"
				+ "</OPTION>\n"
			;
			
			String SQL = "SELECT"
				+ " * FROM " + SMTableglfiscalperiods.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalperiods.iperiod1locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod2locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod3locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod4locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod5locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod6locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod7locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod8locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod9locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod10locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod11locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod12locked + " = 0)"
					+ " OR (" + SMTableglfiscalperiods.iperiod13locked + " = 0)"
				+ ")"
				+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear
			;
			
			try {
				ResultSet rsOpenFiscalPeriods = ServletUtilities.clsDatabaseFunctions.openResultSet(
					SQL, getServletContext(), sm.getsDBID(), "MySQL", this.toString() + ".getEditHTML - " + sm.getFullUserName());
				while (rsOpenFiscalPeriods.next()){
					int iNumberOfPeriods = rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.inumberofperiods);
					int iFiscalYear = rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.ifiscalyear);
					int iFiscalPeriod = 1;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod1locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 2;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod2locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 3;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod3locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 4;
					//System.out.println("[1556051562] - rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod4locked) = '" + rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod4locked) + "'.");
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod4locked) == 0){
						//System.out.println("[1556051564] - into if clause, entry.getsfiscalyear() = '" + entry.getsfiscalyear() 
						//+ "', entry.getsfiscalperiod() = '" + entry.getsfiscalperiod() + "'.");
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
						//System.out.println("[1556051563] - period 4 unlocked, sControlHTML = '" + sControlHTML + "'.");
					}
					iFiscalPeriod = 5;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod5locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 6;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod6locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 7;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod7locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 8;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod8locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 9;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod9locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 10;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod10locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 11;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod11locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					iFiscalPeriod = 12;
					if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod12locked) == 0){
						sControlHTML += "<OPTION";
						try {
							if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
								if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
									sControlHTML += " selected=YES ";	
								}
							}
						} catch (Exception e) {
							//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
							//stop anything, just go on:
						}
						sControlHTML += " VALUE=\"" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "\">" 
							+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
							+ "</OPTION>\n"
						;
					}
					if (iNumberOfPeriods > 12){
						iFiscalPeriod = 13;
						if (rsOpenFiscalPeriods.getInt(SMTableglfiscalperiods.iperiod2locked) == 0){
							sControlHTML += "<OPTION";
							try {
								if (iFiscalYear == Integer.parseInt(entry.getsfiscalyear())){
									if (Integer.parseInt(entry.getsfiscalperiod()) == iFiscalPeriod){
										sControlHTML += " selected=YES ";	
									}
								}
							} catch (Exception e) {
								//If we generate an exception because getsfiscalyear or getsfiscalperiod is blank, don't 
								//stop anything, just go on:
							}
							sControlHTML += " VALUE=\"" 
								+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
								+ "\">" 
								+ Integer.toString(iFiscalYear) + FISCAL_YEAR_AND_PERIOD_DELIMITER + Integer.toString(iFiscalPeriod) 
								+ "</OPTION>\n"
							;
						}
					}
				}
				rsOpenFiscalPeriods.close();
			} catch (NumberFormatException e) {
				sControlHTML += "<BR><B><FONT COLOR=RED>Error [1556051257] reading open fiscal periods - " + e.getMessage() + "</FONT></B><BR>";
			}
		
			sControlHTML += "</SELECT>\n";
			
			sControlHTML += "    </TD>\n";
		}else{
			sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_FISCAL_YEAR_AND_PERIOD + "\""
	    		+ " VALUE=\"" + entry.getsfiscalyear() + FISCAL_YEAR_AND_PERIOD_DELIMITER + entry.getsfiscalperiod() + "\""
	    		+ ">"
	    		+ entry.getsfiscalyear() + FISCAL_YEAR_AND_PERIOD_DELIMITER + entry.getsfiscalperiod()
			;
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
        		+ "</TD>\n";   	
    	

        //Source Ledger:
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Source&nbsp;ledger:&nbsp;</TD>\n"
 		    ;
     	if (bEditable){
 			sControlHTML = "<SELECT NAME = \"" + SMTablegltransactionbatchentries.ssourceledger + "\" >\n";
 			sControlHTML += "<OPTION"
 				+ " VALUE=\"" 
 				+ "-1" 
 				+ "\">" 
 				+ "** SELECT SOURCE LEDGER **"
 				+ "</OPTION>\n"
 			;
 			
 			//Default the source ledger to Journal Entry, if there isn't one already:
 			String sSourceLedger = GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_JOURNAL_ENTRY);
 			if (entry.getssourceledger().compareToIgnoreCase("") != 0){
 				sSourceLedger = entry.getssourceledger();
 			}
 			for (int i = 0; i < GLSourceLedgers.NO_OF_SOURCELEDGERS; i++){
 				sControlHTML += "<OPTION";
 				if (sSourceLedger.compareToIgnoreCase(GLSourceLedgers.getSourceLedgerDescription(i)) == 0){
 					sControlHTML += " selected=YES ";
 				}
 				sControlHTML += " VALUE=\"" 
 					+ GLSourceLedgers.getSourceLedgerDescription(i)
 					+ "\">" 
 					+ GLSourceLedgers.getSourceLedgerDescription(i)
 					+ "</OPTION>\n"
 				;
 			}

 			sControlHTML += "</SELECT>\n";
 			
 			sControlHTML += "    </TD>\n";
 		}else{
 			sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.ssourceledger + "\""
 	    		+ " VALUE=\"" + entry.getssourceledger() + "\""
 	    		+ ">"
 	    		+ entry.getssourceledger()
 			;
     	}
     	
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
 		s += "  </TR>\n";
     	
        //Source document transaction ID:
 		s += "  <TR>\n";
 		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Source document ID:&nbsp;</TD>\n"
 		    ;
     	if (bEditable){
     		String sDefaultID = "0";
     		if (entry.getssourceledgertransactionlineid().compareToIgnoreCase("") != 0){
     			sDefaultID = entry.getssourceledgertransactionlineid();
     		}
     		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatchentries.lsourceledgertransactionlineid + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(sDefaultID) + "\""
 	    		+ " MAXLENGTH=" + "13"
 	    		+ " SIZE = " + "10"
 	    		+ " onchange=\"flagDirty();\""
 	    		+ ">"
 	    	;
     	}else{
     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.lsourceledgertransactionlineid + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getssourceledgertransactionlineid()) + "\""
 	    		+ ">"
 	    		+ entry.getssourceledgertransactionlineid()
 	    	;
     	}
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
     	
     	s += "    <TD>&nbsp;</TD>\n"
     		+ "    <TD>&nbsp;</TD>"
     	;
     	
     	s += "  </TR>\n";
     	
        //Description:
    	s += "  <TR>\n";
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Description:&nbsp;</TD>\n"
 		    ;
     	if (bEditable){
     		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatchentries.sentrydescription + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsentrydescription()) + "\""
 	    		+ " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchentries.sentrydescriptionLength)
 	    		+ " SIZE = " + "60"
 	    		+ " onchange=\"flagDirty();\""
 	    		+ ">"
 	    	;
     	}else{
     		sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.sentrydescription + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getsentrydescription()) + "\""
 	    		+ ">"
 	    		+ entry.getsentrydescription()
 	    	;
     	}
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML + "</TD>\n";
     	s += "  </TR>\n";
     	
    	s += "</TABLE>\n";
 
    	//Display the first row of control buttons:
    	if (bEditable){
    		s += "<BR>" + createSaveButton() 
    		+ "&nbsp;" + createSaveAndAddLineButton()
    		+ "&nbsp;" + createSaveAndAddEntryButton()
    		+ "&nbsp;" + createDeleteButton() + "\n";
    	}
    	
    	s += "<BR>\n";
    	

		s += clsServletUtilities.createHTMLComment("Start the details table here.");
		try {
			s += buildDetailTables(entry, bEditable, sm);
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED><B>Error [1494602711] displaying detail lines - " + e.getMessage() 
				+ "</B></FONT><BR>\n"
				;
		}
    	if (bEditable){
       		s += "<BR>" + createSaveButton() 
    		+ "&nbsp;" + createSaveAndAddLineButton()
    		+ "&nbsp;" + createSaveAndAddEntryButton()
    		+ "&nbsp;" + createDeleteButton() + "\n";
    	}
		
		return s;
	}

	private String buildDetailTables(
		GLTransactionBatchEntry entry, 
		boolean bEditable, 
		SMMasterEditEntry smmastereditentry
		) throws Exception{
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
		if (bEditable){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Line&nbsp;#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Acct</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Description</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Debit</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Credit</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Transaction&nbsp;Date</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source<BR>Type</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Description</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Reference</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Comment</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+ "Remove?</TD>\n";
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "Line&nbsp;#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Acct</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Description</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Debit</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Credit</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Transaction&nbsp;Date</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source<BR>Type</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Description</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Reference</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Comment</TD>\n";
			
		}
	
		s += "  </TR>\n";
		
		
		String SQL = "SELECT"
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
			s += "<B>Error [1555708287] reading GL info - " + e.getMessage() + "</B><BR>";
		}
		
		//Load the lines for the current entry:
		String sBackgroundColor = "";
		boolean bOddRow = true;
		for (int i = 0; i < entry.getLineArray().size(); i++){
			GLTransactionBatchLine line = entry.getLineArray().get(i);
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
				sLineText += "<INPUT TYPE=HIDDEN NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.lid + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
			    	+ ">"
			    	+ "\n"
			    ;
			}
			
			//Line number:
			String sLineNumber = "NEW";
			if (line.getslinenumber().compareToIgnoreCase("-1") != 0){
				sLineNumber = line.getslinenumber();
			}
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ sLineNumber 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// acct:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sacctid + "\""
					+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sacctid + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(line.getsacctid()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sacctidLength)
				    + " SIZE = " + "14"
				    + " onchange=\"verifyGL(this, "
				    	+ "'" 
				    	+ ACCT_DESC_LABEL_BASE 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				    + "');\""
			    	+ ">"
				    
			    	//+ findGLAccountButton(GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					//	+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					//	+ SMTablegltransactionbatchlines.sacctid)
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsacctid());
			}
			sLineText += "</TD>\n";
			
			//Acct description:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			sLineText += "<LABEL ID = \"" + ACCT_DESC_LABEL_BASE 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) + "\" >" 
				+  clsStringFunctions.filter(line.getsacctid()) + "</LABEL>";
			sLineText += "</TD>\n";
			
			//Debit:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.bddebitamt + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdebitamt()) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "8"
				    //+ " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsdebitamt());
			}
			sLineText += "</TD>\n";
			
			//Credit:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""	
					+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.bdcreditamt + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscreditamt()) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "8"
				    //+ " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getscreditamt());
			}
			sLineText += "</TD>\n";
			
			//Transaction date:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.dattransactiondate + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getstransactiondate()) + "\""
				    + " MAXLENGTH=" + "10"
				    + " SIZE = " + "8"
				    + " onchange=\"flagDirty();\""
				    + ">"
				    + "&nbsp;" + SMUtilities.getDatePickerString(
				    		GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
							+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
							+ SMTablegltransactionbatchlines.dattransactiondate,
							getServletContext()
						)
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getstransactiondate());
			}
			sLineText += "</TD>\n";
			
			//Source type:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "\n<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ PARAM_SOURCE_LEDGER_AND_TYPE + "\""
					+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ PARAM_SOURCE_LEDGER_AND_TYPE +  "\""
					+ " onchange=\"flagDirty();\""
					 + " >\n"
				;
				
				String ssSourceTypeSelections = "";
				for (int j = 0; j < GLSourceLedgers.getSourceTypes().size(); j++){
					ssSourceTypeSelections += "<OPTION";
					if (GLSourceLedgers.getSourceTypes().get(j).compareTo(line.getssourceledger() 
						+ GLSourceLedgers.SOURCE_LEDGER_AND_TYPE_DELIMITER + line.getssourcetype()) == 0){
						ssSourceTypeSelections += " selected=yes";
					}
					ssSourceTypeSelections += " VALUE=\"" + GLSourceLedgers.getSourceTypes().get(j) + "\">" + GLSourceLedgers.getSourceTypes().get(j) + "\n";
				}
				sLineText += ssSourceTypeSelections;
				sLineText += "</SELECT>"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getssourceledger() + GLSourceLedgers.SOURCE_LEDGER_AND_TYPE_DELIMITER + line.getssourcetype());
			}
			sLineText += "</TD>\n";
			
			//Line description:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sdescription + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sdescriptionLength)
				    + " SIZE = " + "40"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsdescription());
			}
			sLineText += "</TD>\n";
			
			//Reference:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sreference + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsreference()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sreferenceLength)
				    + " SIZE = " + "30"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsreference());
			}
			sLineText += "</TD>\n";

			//Comment:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.scomment + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.scommentLength)
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
			bOddRow = !bOddRow;
			
			//Add the buffer into the main string:
			s += sLineText;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			GLTransactionBatchLine transactionline = new GLTransactionBatchLine();
			transactionline.setsbatchnumber(entry.getsbatchnumber());
			transactionline.setsentrynumber(entry.getsentrynumber());
			transactionline.setslinenumber("0");
			
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
			s += "<INPUT TYPE=HIDDEN NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.lid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getslid()) + "\""
		    	+ ">"
		    	+ "\n"
		    ;
			
			//Line number:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "(NEW)" 
				+ "<INPUT TYPE=HIDDEN"
				+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.llinenumber + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.llinenumber + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// Acct:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			
			s += "<INPUT TYPE=TEXT"
					+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sacctid + "\""
					+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.sacctid + "\""
					+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getsacctid()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sacctidLength)
				    + " SIZE = " + "14"
				    + " onchange=\"verifyGL(this, "
			    	+ "'" 
			    	+ ACCT_DESC_LABEL_BASE 
					+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
			    + "');\""
				    
			    	//+ findGLAccountButton(GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					//	+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					//	+ SMTablegltransactionbatchlines.sacctid)
				;

			s += "</TD>\n";
			
			//Acct description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<LABEL ID = \"" + ACCT_DESC_LABEL_BASE 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) + "\" >" 
				+  clsStringFunctions.filter(transactionline.getsacctid()) + "</LABEL>";
			s += "</TD>\n";
			
			//Debit:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bddebitamt + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bddebitamt + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getsdebitamt()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "8"
			    //+ " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Credit:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bdcreditamt + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bdcreditamt + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getscreditamt()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "8"
			    //+ " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Transaction date:
			String sDefaultTransactionDate = entry.getsdatentrydate();
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.dattransactiondate + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(sDefaultTransactionDate) + "\""
			    + " MAXLENGTH=" + "10"
			    + " SIZE = " + "8"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			    + "&nbsp;" + SMUtilities.getDatePickerString(
			    		GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTablegltransactionbatchlines.dattransactiondate,
						getServletContext()
					)
			;
			s += "</TD>\n";
			
			//Source type:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "\n<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ PARAM_SOURCE_LEDGER_AND_TYPE + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ PARAM_SOURCE_LEDGER_AND_TYPE +  "\""
				+ " onchange=\"flagDirty();\""
				 + " >\n"
			;
			
			String ssSourceTypeSelections = "";
			String sDefaultSourceType = "JE" + GLSourceLedgers.SOURCE_LEDGER_AND_TYPE_DELIMITER + "JE";
			for (int j = 0; j < GLSourceLedgers.getSourceTypes().size(); j++){
				ssSourceTypeSelections += "<OPTION";
				if (GLSourceLedgers.getSourceTypes().get(j).compareTo(sDefaultSourceType) == 0){
					ssSourceTypeSelections += " selected=yes";
				}
				ssSourceTypeSelections += " VALUE=\"" + GLSourceLedgers.getSourceTypes().get(j) + "\">" + GLSourceLedgers.getSourceTypes().get(j) + "\n";
			}
			s += ssSourceTypeSelections;
			s += "\n</SELECT>\n";
			s += "    </TD>\n";
			
			//Line description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sdescription + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getsdescription()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sdescriptionLength)
			    + " SIZE = " + "40"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";			
			
			//Reference:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sreference + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getsreference()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sreferenceLength)
			    + " SIZE = " + "30"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";			

			//Comment:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.scomment + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(transactionline.getscomment()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.scommentLength)
			    + " SIZE = " + "25"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			s += "  </TR>\n";
		}
		
		//Print a line for the totals:
		s += "  <TR style = \"  background-color:" + TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR + ";  \""
				+ ">\n"
			;
		s += "    <TD COLSPAN=3 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
		s += "<B>TOTALS:</B>";
		s += "</TD>\n";
		
		BigDecimal bdDebitTotal = entry.getDebitTotal();
		BigDecimal bdCreditTotal = entry.getCreditTotal();
		
		String sDebitTotal = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitTotal);
		String sCreditTotal = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditTotal);
		
		if (bdDebitTotal.compareTo(bdCreditTotal) != 0){
			sDebitTotal = "<B><FONT COLOR=RED>" + sDebitTotal + "</FONT></B>";
			sCreditTotal = "<B><FONT COLOR=RED>" + sCreditTotal + "</FONT></B>";
		}
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
		s += "<B>" + sDebitTotal + "</B>";
		s += "</TD>\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
		s += "<B>" + sCreditTotal + "</B>";
		s += "</TD>\n";
		
		s += "  </TR>\n";
		
		s += "</TABLE>\n";
		return s;
	}
	
	private String sCommandScript(String sDBID, SMMasterEditEntry sm, GLTransactionBatchEntry entry) throws Exception{
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
			+ "    displayGLDescriptions();\n"
			+ "    setfocustofirstline();\n"
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		//Load the GL accounts:
		int iCounter = 0;
		
		String sGLAccounts = "";
		String sGLAccountDescriptions = "";
		
		//Add one array item for the 'blank' distribution code, if someone doesn't pick one:
		//sGLAccounts += "sglaccounts[\"" + " " + "\"] = \"" + "" + "\";\n";
		
		String SQL = "SELECT"
			+ " " + SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglaccounts.TableName
			+ " ORDER BY " + SMTableglaccounts.sAcctID
		;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + " loading GL accts [1556209941] SQL: " + SQL 
			);
			while (rs.next()){
				if (iCounter == 0){
					sGLAccounts += "\"" + rs.getString(SMTableglaccounts.sAcctID).trim().replace("\"", "'") + "\"\n";
					sGLAccountDescriptions += "\"" + rs.getString(SMTableglaccounts.sDesc).trim().replace("\"", "'") + "\"\n";
				}else{
					sGLAccounts += ", \"" + rs.getString(SMTableglaccounts.sAcctID).trim().replace("\"", "'") + "\"\n";
					sGLAccountDescriptions += ", \"" + rs.getString(SMTableglaccounts.sDesc).trim().replace("\"", "'") + "\"\n";
				}
				iCounter++;
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading [1556209942] GL accts for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var sglaccounts = [\n" + sGLAccounts + "]\n\n";
			s += "var sglaccountdescriptions = [\n" + sGLAccountDescriptions + "]\n\n";
		}
		
		s += "\n";

		s += "function promptToSave(){\n"		
			//If the record WAS changed, then
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				//If it was anything but the 'SAVE' command that triggered this function...
			+ "        if (\n"
			+ "            (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + COMMAND_VALUE_SAVE + "\" )\n"
			+ "            && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + COMMAND_VALUE_SAVE_AND_ADD_ENTRY + "\" )\n"
			+ "            && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + COMMAND_VALUE_SAVE_AND_ADD_LINE + "\" )\n"
			+ "        ){\n"
						//Prompt to see if the user wants to continue
			+ "        return 'You have unsaved changes.';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;

		s += "function setfocustofirstline(){ \n";
		if (ServletUtilities.clsManageRequestParameters.get_Request_Parameter(ADDING_LINE_FLAG, sm.getRequest()).compareToIgnoreCase("") != 0){
			s += "    document.getElementById(\"" 
				+ GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft("0", "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH)
				+ SMTablegltransactionbatchlines.sacctid
				+ "\").focus();\n"
			;
		}
			s += "    return;\n"
			+ "} \n"
		;
//		+ " ID=\"" +  
//		+ clsStringFunctions.PadLeft(transactionline.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
//		+ SMTablegltransactionbatchlines.llinenumber + "\""
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		//Verify a GL account:
		s += "function verifyGL(obj, labelid){\n"
			+ "    var glfoundindex = sglaccounts.indexOf(obj.value);\n"
			+ "    if (glfoundindex > -1){\n"
			+ "        //alert('Index found = ' + glfoundindex);\n"
			+ "        var glacctdesc = sglaccountdescriptions[glfoundindex];\n"
			+ "        //alert('Acct desc = ' + glacctdesc);\n"
			+ "        document.getElementById(labelid).innerHTML = glacctdesc;\n"
			+ "    }else{\n"
			+ "        alert('\\'' + obj.value + '\\' is not a valid GL Account.');\n"
			+ "        obj.focus();\n"
			+ "        obj.select();\n"
			+ "    }"
			+ "    flagDirty();\n"
			+ "    return;\n"
			+ "}\n"
		;
		
		//Populate GL descriptions on startup:
		s += "function displayGLDescriptions(){\n"
			+ "    var elements = document.getElementById('" + SMMasterEditEntry.MAIN_FORM_NAME + "').elements;\n"
			+ "    for (var i = 0, element; element = elements[i++];) {\n"
			+ "        if (element.id.includes('" + SMTablegltransactionbatchlines.sacctid + "')){\n"
			+ "            if (element.value !== ''){\n"
			+ "                var labelid = element.id.replace('" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER + "', '');\n"
			+ "                labelid = '" + ACCT_DESC_LABEL_BASE + "' + labelid;\n"
			+ "                labelid = labelid.replace('" + SMTablegltransactionbatchlines.sacctid + "', '');\n"
			+ "                verifyGL(element, labelid);\n"
			+ "                //Need to reset the 'dirty' flag:\n"
			+ "                document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = '';\n"
			+ "            }\n"
			+ "        }\n"
        	+ "    }\n"
			+ "    return;\n"
			+ "}\n"
		;
		
		//Remove a detail line:
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
		
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		s += "function saveandaddentry(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE_AND_ADD_ENTRY + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		s += "function saveandaddline(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE_AND_ADD_LINE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
		s += "function deleteentry(){\n"
			+ "    if (confirm('Are you sure you want to delete this " + "entry" + "?')){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;

		s += "function findacct(){\n"
			+ "    alert('Not yet implemented.');\n"
			+ "}\n"
		;

		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
		//Hot key stuff:
		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        deleteentry();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+e\",function() {\n";
		s += "        saveandaddline();\n";
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
		s += "        saveandaddentry();\n";
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

	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ UPDATE_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createSaveAndAddEntryButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_AND_ADD_NEW_ENTRY_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_AND_ADD_NEW_ENTRY_BUTTON_LABEL + "\""
				+ " onClick=\"saveandaddentry();\">"
				+ UPDATE_AND_ADD_NEW_ENTRY_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createSaveAndAddLineButton(){
		return "<button type=\"button\""
				+ " value=\"" + UPDATE_AND_ADD_NEW_LINE_BUTTON_LABEL + "\""
				+ " name=\"" + UPDATE_AND_ADD_NEW_LINE_BUTTON_LABEL + "\""
				+ " onClick=\"saveandaddline();\">"
				+ UPDATE_AND_ADD_NEW_LINE_BUTTON_LABEL
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
//	private String findGLAccountButton(String sFieldName){
//		return "<button type=\"button\""
//				+ " value=\"" + FIND_ACCT_BUTTON_LABEL + "\""
//				+ " name=\"" + FIND_ACCT_BUTTON_LABEL + "\""
//				+ " onClick=\"findacct(sFieldName);\">"
//				+ FIND_ACCT_BUTTON_LABEL
//				+ "</button>\n"
//				;
//	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
