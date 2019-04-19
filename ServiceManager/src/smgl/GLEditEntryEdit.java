package smgl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditEntryEdit  extends HttpServlet {

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
	public static final String UPDATE_AND_ADD_NEW_BUTTON_LABEL = "Update and add <B><FONT COLOR=RED>n</FONT></B>ew"; // N
	
	public static final String BOOKMARK_TOP_OF_TABLES = "TopOfTables";
	public static final String RETURN_TO_TABLES_BOOKMARK = "RETURNTOTABLESPARAM";
	
	//Hot keys:
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; // D
	public static final String VIEW_APPLY_TO_DOCUMENTS_BUTTON_LABEL = "Vie<B><FONT COLOR=RED>w</FONT></B> table of apply-to documents"; // W
	public static final String HIDE_APPLY_TO_DOCUMENTS_BUTTON_LABEL = "<B><FONT COLOR=RED>H</FONT></B>ide table of apply-to documents"; // H
	public static final String CALCULATE_TERMS_BUTTON_LABEL = "Calculate <B><FONT COLOR=RED>t</FONT></B>erms"; // T
	
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
			+ RECORDWASCHANGED_FLAG_VALUE + "\""
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
    	//On hold:
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
		
    	//Fiscal year
    	s += "  <TR>\n";
    	
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Fiscal&nbsp;year:&nbsp;</TD>\n";
		
		if (bEditable){
			int iCurrentFiscalYear = 0;
			try {
				iCurrentFiscalYear = GLFiscalPeriod.getCurrentFiscalYear(sm.getsDBID(), sm.getFullUserName(), getServletContext());
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT><BR>";
			}
			int iFiscalYear = Integer.parseInt(entry.getsfiscalyear());
			if (iFiscalYear == 0){
				iFiscalYear = iCurrentFiscalYear;
			}
			sControlHTML = "<SELECT NAME = \"" + SMTablegltransactionbatchentries.ifiscalyear + "\" >\n";
			sControlHTML += "<OPTION"
				+ " VALUE=\"" 
				+ "-1" 
				+ "\">" 
				+ "** SELECT FISCAL YEAR **"
				+ "</OPTION>\n"
			;
			
			for (int i = 2015; i < 2050; i++){
				sControlHTML += "<OPTION";
				if (iFiscalYear == i){
					sControlHTML += " selected=YES ";
				}
				sControlHTML += " VALUE=\"" 
					+ Integer.toString(i) 
					+ "\">" 
					+ Integer.toString(i)
					+ "</OPTION>\n"
				;
			}

			sControlHTML += "</SELECT>\n";
			
			sControlHTML += "    </TD>\n";
		}else{
			sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.ifiscalyear + "\""
	    		+ " VALUE=\"" + entry.getsfiscalyear() + "\""
	    		+ ">"
	    		+ entry.getsfiscalyear()
			;
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
        		+ "</TD>\n";   	
    	
    	//Fiscal period
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Fiscal&nbsp;period:&nbsp;</TD>\n";
		
		if (bEditable){
			int iCurrentFiscalPeriod = 0;
			try {
				iCurrentFiscalPeriod = GLFiscalPeriod.getCurrentFiscalPeriod(sm.getsDBID(), sm.getFullUserName(), getServletContext());
			} catch (Exception e) {
				s += "<BR><FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT><BR>";
			}
			int iFiscalPeriod = Integer.parseInt(entry.getsfiscalperiod());
			if (iFiscalPeriod == 0){
				iFiscalPeriod = iCurrentFiscalPeriod;
			}
			//System.out.println("[1555702651] fiscal period = " + iFiscalPeriod);
			sControlHTML = "<SELECT NAME = \"" + SMTablegltransactionbatchentries.ifiscalperiod + "\" >\n";
			sControlHTML += "<OPTION"
				+ " VALUE=\"" 
				+ "-1" 
				+ "\">" 
				+ "** SELECT FISCAL PERIOD **"
				+ "</OPTION>\n"
			;
			
			for (int i = 1; i <= 13; i++){
				sControlHTML += "<OPTION";
				if (iFiscalPeriod == i){
					sControlHTML += " selected=YES ";
				}
				sControlHTML += " VALUE=\"" 
					+ Integer.toString(i) 
					+ "\">" 
					+ Integer.toString(i)
					+ "</OPTION>\n"
				;
			}

			sControlHTML += "</SELECT>\n";
			
			sControlHTML += "    </TD>\n";
		}else{
			sControlHTML = "<INPUT TYPE=HIDDEN NAME=\"" + SMTablegltransactionbatchentries.ifiscalperiod + "\""
	    		+ " VALUE=\"" + entry.getsfiscalperiod() + "\""
	    		+ ">"
	    		+ entry.getsfiscalperiod()
			;
		}
		
    	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >" + sControlHTML 
        		+ "</TD>\n";  
    	
    	s += "  </TR>\n";    
     	
        //Source Ledger:
    	s += "  <TR>\n";
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
 			
 			for (int i = 0; i < GLSourceLedgers.NO_OF_SOURCELEDGERS; i++){
 				sControlHTML += "<OPTION";
 				if (entry.getssourceledger().compareToIgnoreCase(GLSourceLedgers.getSourceLedgerDescription(i)) == 0){
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
 		
        //Source document transaction ID:
     	s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Source document ID:&nbsp;</TD>\n"
 		    ;
     	if (bEditable){
     		sControlHTML = "<INPUT TYPE=TEXT NAME=\"" + SMTablegltransactionbatchentries.lsourceledgertransactionlineid + "\""
 	    		+ " VALUE=\"" + clsStringFunctions.filter(entry.getssourceledgertransactionlineid()) + "\""
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
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton() + "\n";
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
    		s += "<BR>" + createSaveButton() + "&nbsp;" + createSaveAndAddButton() + "&nbsp;" + createDeleteButton() + "\n";
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
					+ "Reference</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Comment</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Transaction<BR>Date</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source Ledger</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source Type</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Debit</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Credit</TD>\n";

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

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Reference</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Comment</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Transaction<BR>Date</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source<BR>Ledger</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Source Type</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Debit</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Credit</TD>\n";
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
		
		ArrayList<String> arrSourceLedgers = new ArrayList<String>(0);
		for (int i = 0; i < GLSourceLedgers.NO_OF_SOURCELEDGERS; i++){
			arrSourceLedgers.add(GLSourceLedgers.getSourceLedgerDescription(i));
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
				sLineText += "\n<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTablegltransactionbatchlines.sacctid + "\""
						+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
						+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
						+ SMTablegltransactionbatchlines.sacctid + "\""
						+ " onchange=\"flagDirty();\""
						 + " >\n"
					;
				
				String sBuffer = "";
				String sGLAcctSelections = "";
				int iCounter = 0;
					for (int j = 0; j < arrGLAccts.size(); j++){
						sBuffer += "<OPTION";
						if (arrGLAccts.get(j).toString().compareTo(line.getsacctid()) == 0){
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
				
			}else{
				sLineText += clsStringFunctions.filter(line.getsacctid());
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
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getstransactiondate());
			}
			sLineText += "</TD>\n";
			
			//Source Ledger:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "\n<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.ssourceledger + "\""
					+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.ssourceledger + "\""
					+ " onchange=\"flagDirty();\""
					 + " >\n"
				;
				
				String ssSourceLedgerSelections = "";
				for (int j = 0; j < arrSourceLedgers.size(); j++){
					ssSourceLedgerSelections += "<OPTION";
					if (arrSourceLedgers.get(j).toString().compareTo(line.getssourceledger()) == 0){
						ssSourceLedgerSelections += " selected=yes";
					}
					ssSourceLedgerSelections += " VALUE=\"" + arrSourceLedgers.get(j).toString() + "\">" + arrSourceLedgers.get(j).toString() + "\n";
				}
				sLineText += ssSourceLedgerSelections;
				sLineText += "</SELECT>"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getssourceledger());
			}
			sLineText += "</TD>\n";

			//Source type:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.ssourcetype + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getssourcetype()) + "\""
				    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.ssourcetypeLength)
				    + " SIZE = " + "4"
				    + " onchange=\"flagDirty();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getssourcetype());
			}
			sLineText += "</TD>\n";
			
			//Debit:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.bddebitamt + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdebitamt()) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getsdebitamt());
			}
			sLineText += "</TD>\n";
			
			//Credit:
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			if (bEditable){
				sLineText += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.bdcreditamt + "\""
			    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscreditamt()) + "\""
				    + " MAXLENGTH=" + "13"
				    + " SIZE = " + "12"
				    + " onchange=\"updateLineTotal();\""
			    	+ ">"
				;
			}else{
				sLineText += clsStringFunctions.filter(line.getscreditamt());
			}
			sLineText += "</TD>\n";
			
			sLineText += "  </TR>\n";
			bOddRow = !bOddRow;
			
			//Add the buffer into the main string:
			s += sLineText;
		}

		if (bEditable){
			//Add one blank line so the user can add lines:
			GLTransactionBatchLine line = new GLTransactionBatchLine();
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
			s += "<INPUT TYPE=HIDDEN NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.lid + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getslid()) + "\""
		    	+ ">"
		    	+ "\n"
		    ;
			
			//Line number:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >"
				+ "(NEW)" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.llinenumber + "\""
	    		+ " VALUE=\"" + clsStringFunctions.filter(line.getslinenumber()) + "\""
	    		+ ">"
				+ "</TD>\n";
			
			// Acct:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			
			s  += "<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sacctid + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sacctid + "\""
				+ " onchange=\"flagDirty();\""
				 + " >\n"
			;
			for (int i = 0; i < arrGLAccts.size(); i++){
				s += "<OPTION";
				if (arrGLAccts.get(i).toString().compareTo(line.getsacctid()) == 0){
					s += " selected=yes";
				}
				s += " VALUE=\"" + arrGLAccts.get(i).toString() + "\">" + arrGLDescriptions.get(i).toString() + "\n";
			}
			s += "</SELECT>"
			;
			s += "</TD>\n";
			
			//Line description:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sdescription + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdescription()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sdescriptionLength)
			    + " SIZE = " + "40"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";			
			
			//Reference:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.sreference + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsreference()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.sreferenceLength)
			    + " SIZE = " + "40"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";			

			//Comment:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.scomment + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscomment()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.scommentLength)
			    + " SIZE = " + "25"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Transaction date:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.dattransactiondate + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getstransactiondate()) + "\""
			    + " MAXLENGTH=" + "10"
			    + " SIZE = " + "8"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Source ledger:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s  += "<SELECT NAME = \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.ssourceledger + "\""
					+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
					+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
					+ SMTablegltransactionbatchlines.ssourceledger + "\""
					+ " onchange=\"flagDirty();\""
					 + " >\n"
				;
				for (int i = 0; i < arrSourceLedgers.size(); i++){
					s += "<OPTION";
					if (arrSourceLedgers.get(i).toString().compareTo(line.getssourceledger()) == 0){
						s += " selected=yes";
					}
					s += " VALUE=\"" + arrSourceLedgers.get(i).toString() + "\">" + arrSourceLedgers.get(i).toString() + "\n";
				}
				s += "</SELECT>"
				;
				s += "</TD>\n";
			s += "</TD>\n";
			
			//Source type:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.ssourcetype + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getssourcetype()) + "\""
			    + " MAXLENGTH=" + Integer.toString(SMTablegltransactionbatchlines.ssourcetypeLength)
			    + " SIZE = " + "4"
			    + " onchange=\"flagDirty();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Debit:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bddebitamt + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bddebitamt + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getsdebitamt()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "12"
			    + " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			//Credit:
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + " \" >";
			s += "<INPUT TYPE=TEXT"
				+ " style=\"text-align:right;\""
				+ " NAME=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bdcreditamt + "\""
				+ " ID=\"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER 
				+ clsStringFunctions.PadLeft(line.getslinenumber().trim(), "0", GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH) 
				+ SMTablegltransactionbatchlines.bdcreditamt + "\""
		    	+ " VALUE=\"" + clsStringFunctions.filter(line.getscreditamt()) + "\""
			    + " MAXLENGTH=" + "13"
			    + " SIZE = " + "12"
			    + " onchange=\"updateLineTotal();\""
		    	+ ">"
			;
			s += "</TD>\n";
			
			s += "  </TR>\n";
		}
		
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
		 
		s += "function flagDirty() {\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
			+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
		+ "}\n";
		
		s += "function updateLineTotal(){\n"
			+ "    calculatelinetotal();"
			+ "    flagDirty();\n"
			+ "}\n"
		;
		
		/*
		s += "function calculatelinetotal(){\n"
			+ "    //Turn off the line amt warning by default:\n"
			//+ "    document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.display= \"none\"\n"
			+ "    var linetotal = getFloat(\"0.00\");\n"
			+ "    var entryamt = getFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMTablegltransactionbatchentries.bdentryamount + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        entryamt = getFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        entryamt = getFloat(temp);\n"
			+ "    }\n"
			
			+ "    // For each of the lines on the entry, add the amount:\n"
			+ "	   for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
			+ "        //Get the name of the control:\n"
   			+ "	       var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
			+ "        //If the control name starts with '" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER + "', then pick off the rest of it:\n"
   			+ "        if (testName.substring(0, " + Integer.toString(GLTransactionBatchEntry.LINE_NUMBER_PARAMETER.length()) + "	) == \"" + GLTransactionBatchEntry.LINE_NUMBER_PARAMETER + "\"){\n"
   			+ "            //If the string ENDS with the field name '" + SMTablegltransactionbatchentries.bdamount + "', then it's a line amount:\n"
   			+ "            if (testName.endsWith(\"" + SMTablegltransactionbatchentries.bdamount + "\") == true){\n"
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
   		*/
		
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

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
