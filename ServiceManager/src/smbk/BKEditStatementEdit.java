package smbk;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchTypes;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTablebkpostedentries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class BKEditStatementEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String POST_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>ost"; //P
	public static final String POSTCOMMAND_VALUE = "POSTSTATEMENT";
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String DELETE_BUTTON_LABEL = "<B><FONT COLOR=RED>D</FONT></B>elete"; //D
	public static final String DELETECOMMAND_VALUE = "DELETE";
	public static final String ADD_ENTRY_BUTTON_LABEL = "<B><FONT COLOR=RED>A</FONT></B>dd manual entry"; //S
	public static final String ADDENTRY_VALUE = "ADDENTRY";
	public static final String DELETE_ENTRY_BUTTON_LABEL = "Delete entry"; //D
	public static final String DELETEENTRYCOMMAND_VALUE = "DELETEENTRY";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	private static final String ORDERCOMMANDS_TABLE_BG_COLOR = "#99CCFF";
	private static final String ENTRIES_TABLE_ODD_ROW_BG_COLOR = "#B8E6A0";
	private static final String TOTALS_TABLE_BG_COLOR = "#F2C3FA";
	public static final String CHECKBOX_MARKER = "CHECKBOXMARKER";
	public static final String DEPOSIT_ENTRY_AMOUNT_MARKER = "DEPOSITENTRYAMTMARKER";
	public static final String WITHDRAWAL_ENTRY_AMOUNT_MARKER = "WITHDRAWALENTRYAMTMARKER";
	public static final String ENTRY_ID_MARKER = "ENTRYIDMARKER";
	public static final String OUTSTANDING_DEPOSITS_TOTAL_FIELD = "OUTSTANDINGDEPOSITSTOTALFIELD";
	public static final String OUTSTANDING_WITHDRAWALS_TOTAL_FIELD = "OUTSTANDINGWITHDRAWALSTOTALFIELD";
	public static final String ADJUSTED_BANK_BALANCE_FIELD = "ADJUSTEDBANKBALANCEFIELD";
	public static final String CALCULATED_GL_BALANCE_FIELD = "CALCULATEDGLBALANCEFIELD";
	public static final String ENTERED_GL_BALANCE_FIELD = "ENTEREDGLBALANCEFIELD";
	public static final int OVERALL_NUMBER_LENGTH_WITH_PADDING = 9;
	private BigDecimal bdOutstandingWithdrawalsTotal;
	private BigDecimal bdOutstandingDepositsTotal;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				BKBankStatement.ObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smbk.BKEditStatementAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.BKEditStatements
		);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.BKEditStatements)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		BKBankStatement statement = new BKBankStatement();
		try {
			statement.loadFromHTTPRequest(request);
		} catch (Exception e2) {
			smedit.getPWOut().println("Error [1391794199] loading bank statement from request: " + e2.getMessage());
			return;
		}
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a statement object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(BKBankStatement.ObjectName) != null){
			statement = (BKBankStatement) currentSession.getAttribute(BKBankStatement.ObjectName);
			currentSession.removeAttribute(BKBankStatement.ObjectName);

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			//If we are adding a new statement...
			if (smedit.getAddingNewEntryFlag()){
				statement.set_lid(BKBankStatement.UNSAVED_STATEMENT_LID);
				if (statement.get_lbankid().compareToIgnoreCase("") == 0){
					smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + "You must select a bank to add a new statement" 
						+ "</FONT></B><BR>");
					return;
				}
			}else{
				try {
					statement.load(smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName(),  getServletContext());
				} catch (Exception e2) {
					smedit.getPWOut().println("Error [1391549542] loading " + BKBankStatement.ObjectName + " - " + e2.getMessage());
					return;
				}
			}
		}

	    smedit.getPWOut().println(getHeaderString(
			BKBankStatement.ObjectName + " edit", 
			"", 
			SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), 
			SMUtilities.DEFAULT_FONT_FAMILY,
			(String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME)
			))
		;

		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		
	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		if (sStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B>" + sStatus + "</B><BR>");
		}		

	    //Print a link to the first page after login:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "\">Return to user login</A><BR>");
		
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "\">Return to bank functions main menu</A>&nbsp;");
		
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKEditStatementSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to bank statement selections</A>");
	    
		smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
				+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
			);
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
		try{
			smedit.createEditPage(
				getEditHTML(smedit, statement, BKBankStatement.ObjectName), 
				""
			);
		} catch (Exception e) {
			String sError = "Could not create edit page - " + e.getMessage();
			smedit.getPWOut().println(sError);
			return;
		}
		return;
	}
	
	private String getEditHTML(
		SMMasterEditEntry sm, 
		BKBankStatement bankstatement, 
		String sObjectName) throws Exception{

		String s = "";
		//Initialize the totals:
		bdOutstandingWithdrawalsTotal = new BigDecimal("0.00");
		bdOutstandingDepositsTotal = new BigDecimal("0.00");
		s += sCommandScripts(sm);
		s += sStyleScripts();

		//Store whether or not the record has been changed this includes ANY change, including approval:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		//New Row
		s += "<TR>";
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";		

		//Create the tables
		s += "<TR><TD>" + createStatementHeaderTable(sm, bankstatement) + "</TD></TR>";
		if (bankstatement.get_iposted().compareToIgnoreCase("0") == 0){
			s += "<TR><TD>" + createCommandsTable() + "</TD></TR>";
		}
		s += "<TR><TD>" + createEntriesTable(sm, bankstatement) + "</TD></TR>";
		if (bankstatement.get_iposted().compareToIgnoreCase("0") == 0){
			s += "<TR><TD>" + createCommandsTable() + "</TD></TR>";
		}
		
		try {
			s += "<TR><TD>" + createTotalsTable(bankstatement) + "</TD></TR>";
		} catch (Exception e) {
			throw new Exception ("Error creating totals table - " + e.getMessage());
		}

		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		return s;
	}

	private String createCommandsTable(){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ ORDERCOMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >";
		s += createSaveButton();
		s += createDeleteButton();
		s += createPostButton();
		s += createAddEntryButton();
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		return s;
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	private String createPostButton(){
		return "<button type=\"button\""
				+ " value=\"" + POST_BUTTON_LABEL + "\""
				+ " name=\"" + POST_BUTTON_LABEL + "\""
				+ " onClick=\"post();\">"
				+ POST_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createDeleteButton(){
		return "<button type=\"button\""
				+ " value=\"" + DELETE_BUTTON_LABEL + "\""
				+ " name=\"" + DELETE_BUTTON_LABEL + "\""
				+ " onClick=\"deletestatement();\">"
				+ DELETE_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createAddEntryButton(){
		return "<button type=\"button\""
				+ " value=\"" + ADD_ENTRY_BUTTON_LABEL + "\""
				+ " name=\"" + ADD_ENTRY_BUTTON_LABEL + "\""
				+ " onClick=\"addentry();\">"
				+ ADD_ENTRY_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createDeleteEntryButton(String sLid, String sAmt){
		return "<button type=\"button\""
				+ " value=\"" + DELETE_ENTRY_BUTTON_LABEL + sLid + "\""
				+ " name=\"" + DELETE_ENTRY_BUTTON_LABEL + "\""
				+ " onClick=\"deleteentry(" + sLid + ", " + sAmt.replace(",", "") + ");\">"
				+ DELETE_ENTRY_BUTTON_LABEL
				+ "</button>\n"
				;
	}
	private String createStatementHeaderTable(
			SMMasterEditEntry sm, 
			BKBankStatement statement 
			) throws Exception{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:StatementHeaderTable; \" width=100% >\n";	
		s += "<TR>";
		
		if (statement.get_iposted().compareToIgnoreCase("1") == 0){
			//Just build a read-only table:
			s += "<TD class=\" fieldlabel \">Statement ID:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + statement.get_lid() + "</TD>";
			//Bank:
			String sBankName = "";
			String SQL = "SELECT"
				+ " " + SMTablebkbanks.sshortname
				+ " FROM " + SMTablebkbanks.TableName
				+ " WHERE ("
					+ "(" + SMTablebkbanks.lid + " = " + statement.get_lbankid() + ")"
				+ ")"
			;
		    try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
						+ ".getEditHTML - user: " 
						+ sm.getUserID()
						+ " - "
						+ sm.getFullUserName()
						);
				if (rs.next()) {
					sBankName = rs.getString(SMTablebkbanks.sshortname);
				}else{
					throw new Exception("Bank with ID '" + statement.get_lbankid() + "' not found.");
				}
				rs.close();
			} catch (SQLException e) {
				s += "<BR><B>Error reading bank data - " + e.getMessage();
			}
			
			s += "<TD class=\" fieldlabel \">Bank:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + sBankName + "</TD>";
			s += "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + "Y" + "</TD>";
			s += "<TD class=\" fieldlabel \">Statement Date:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" 
				+ statement.get_datstatementdate() + "</TD>"; 
			s += "<TD class=\" fieldlabel \">Adjusted Bank Balance:&nbsp;</TD>" + "<TD class=\"readonlyrightfield\">" 
				+ statement.get_bdstartingbalance() + "</TD>"; 
			s += "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Parambdstartingbalance + "\" VALUE=\"" + statement.get_bdstartingbalance() + "\""
					+ " id=\"" + BKBankStatement.Parambdstartingbalance + "\"" + ">";
			s += "<TD class=\" fieldlabel \">General Ledger Balance:&nbsp;</TD>" + "<TD class=\"readonlyrightfield\">" 
				+ statement.get_bdstatementbalance() + "</TD>";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Parambdstatementbalance + "\" VALUE=\"" + statement.get_bdstatementbalance() + "\""
					+ " id=\"" + BKBankStatement.Parambdstatementbalance + "\"" + ">";
		}else{
			String sStatementID = "(NEW)";
			if(statement.get_lid().compareToIgnoreCase(BKBankStatement.UNSAVED_STATEMENT_LID) != 0){
				sStatementID = statement.get_lid();
			}
			s += "<TD class=\" fieldlabel \">Statement ID:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + sStatementID 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Paramlid + "\" VALUE=\"" + statement.get_lid() + "\""
						+ " id=\"" + BKBankStatement.Paramlid + "\"" + ">"
				+ "</TD>";
			//Bank:
			String sBankName = "";
			String SQL = "SELECT"
				+ " " + SMTablebkbanks.sshortname
				+ " FROM " + SMTablebkbanks.TableName
				+ " WHERE ("
					+ "(" + SMTablebkbanks.lid + " = " + statement.get_lbankid() + ")"
				+ ")"
			;
		    try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
						+ ".getEditHTML - user: " 
							+ sm.getUserID()
							+ " - "
							+ sm.getFullUserName()
						);
				if (rs.next()) {
					sBankName = rs.getString(SMTablebkbanks.sshortname);
				}else{
					throw new Exception("Bank with ID '" + statement.get_lbankid() + "' not found.");
				}
				rs.close();
			} catch (SQLException e) {
				s += "<BR><B>Error reading bank data - " + e.getMessage();
			}
			
			s += "<TD class=\" fieldlabel \">Bank:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + sBankName 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Paramlbankid + "\" VALUE=\"" + statement.get_lbankid() + "\""
					+ " id=\"" + BKBankStatement.Paramlbankid + "\"" + ">"
				+ "</TD>";
			
			String sPosted = "N";
			if (statement.get_iposted().compareToIgnoreCase("1") == 0){
				sPosted = "Y";
			}
			s += "<TD class=\" fieldlabel \">Posted?:&nbsp;</TD>" + "<TD class=\"readonlyleftfield\">" + sPosted 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Paramiposted + "\" VALUE=\"" + statement.get_iposted() + "\""
					+ " id=\"" + BKBankStatement.Paramiposted + "\"" + ">"
				+ "</TD>"
			;
			
			//Statement date:
			s += "<TD class=\" fieldlabel \">Statement date:&nbsp;</TD>"; 
			s += "<TD class=\"fieldcontrol\">"
				+ "<INPUT TYPE=TEXT NAME=\"" + BKBankStatement.Paramdatstatementdate + "\""
				+ " VALUE=\"" + statement.get_datstatementdate().replace("\"", "&quot;") + "\""
				+ " id = \"" + BKBankStatement.Paramdatstatementdate + "\""
				+ " onchange=\"flagDirty();\""
				+ " SIZE=" + "9"
				+ " MAXLENGTH=" + "10"
				+ ">"
				+ SMUtilities.getDatePickerString(BKBankStatement.Paramdatstatementdate, getServletContext())
				+ "</TD>"
			;
			
			;
			//Starting balance:
			s += "<TD class=\" fieldlabel \">Adjusted Bank Balance:&nbsp;</TD>"
				+ "<TD class=\"fieldcontrol\">"
				+ "<INPUT TYPE=TEXT NAME=\"" + BKBankStatement.Parambdstartingbalance + "\""
				+ " id = \"" + BKBankStatement.Parambdstartingbalance + "\""
				+ " VALUE=\"" + statement.get_bdstartingbalance().replace("\"", "&quot;") + "\""
				+ " onchange=\"updateTotals();\""
				+ " SIZE=" + "12"
				+ " MAXLENGTH=" + "15"
				+ ">"
				+ "</TD>"
			;
			//Statement balance:
			s += "<TD class=\" fieldlabel \">General Ledger Balance:&nbsp;</TD>"
				+ "<TD class=\"fieldcontrol\">"
				+ "<INPUT TYPE=TEXT NAME=\"" + BKBankStatement.Parambdstatementbalance + "\""
				+ " id = \"" + BKBankStatement.Parambdstatementbalance + "\""
				+ " VALUE=\"" + statement.get_bdstatementbalance().replace("\"", "&quot;") + "\""
				+ " onchange=\"updateTotals();\""
				+ " SIZE=" + "12"
				+ " MAXLENGTH=" + "15"
				+ ">"
				+ "</TD>"
			;
		}
		s += "</TR>";
		//Close the table:
		s += "</TABLE style = \" title:StatementHeaderTable; \">\n";
		return s;
	}
	private String createTotalsTable(BKBankStatement statement) throws Exception{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:TotalsTable; background-color: "
				+ TOTALS_TABLE_BG_COLOR + "; \" >\n";
		
		s += "<TR>";
		s+= "<TD class=\" readonlyrightfield \">" + "<B>ADJUSTED BANK BALANCE:&nbsp;</B></TD>" 	
			+ "<TD class=\" readonlyrightfield \"><label id=\"" + ADJUSTED_BANK_BALANCE_FIELD + "\" >" 
			+ statement.get_bdstartingbalance().replace(",","") + "</label>" + "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s+= "<TD class=\" readonlyrightfield \">" + "<B>TOTAL DEPOSITS OUTSTANDING:&nbsp;</B></TD>" 	
			+ "<TD class=\" readonlyrightfield \"><label id=\"" + OUTSTANDING_DEPOSITS_TOTAL_FIELD + "\" >" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingDepositsTotal).replace(",","") + "</label>" + "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s+= "<TD class=\" readonlyrightfield \">" + "<B>TOTAL WITHDRAWALS OUTSTANDING:&nbsp;</B></TD>" 	
			+ "<TD class=\" readonlyrightfield \"><label id=\"" + OUTSTANDING_WITHDRAWALS_TOTAL_FIELD + "\" >" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingWithdrawalsTotal).replace(",","") + "</label>" + "</TD>"
		;
		s += "</TR>";
		
		//Calculate the calculated GL balance:
		BigDecimal bdCalculatedGLBalance = new BigDecimal("0.00");
		if (statement.get_bdstartingbalance().compareToIgnoreCase("") != 0){
			try {
				bdCalculatedGLBalance = bdCalculatedGLBalance.add(new BigDecimal(statement.get_bdstartingbalance().replace(",", "")));
			} catch (Exception e) {
				throw new Exception("couldn't get calculated GL balance from '" + statement.get_bdstartingbalance() + "'.");
			}
		}
		bdCalculatedGLBalance = bdCalculatedGLBalance.add(bdOutstandingDepositsTotal);
		bdCalculatedGLBalance = bdCalculatedGLBalance.add(bdOutstandingWithdrawalsTotal);
		
		s += "<TR>";
		s+= "<TD class=\" readonlyrightfield \">" + "<B>CALCULATED GL BALANCE:&nbsp;</B></TD>" 	
				+ "<TD class=\" readonlyrightfield \"><label id=\"" + CALCULATED_GL_BALANCE_FIELD + "\" >" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCalculatedGLBalance).replace(",","") + "</label>" + "</TD>"
			;
		s += "</TR>";
		
		s += "<TR>";
		s+= "<TD class=\" readonlyrightfield \">" + "<B>ENTERED GL BALANCE:&nbsp;</B></TD>" 	
				+ "<TD class=\" readonlyrightfield \"><label id=\"" + ENTERED_GL_BALANCE_FIELD + "\" >" 
				+ statement.get_bdstatementbalance().replace(",","") + "</label>" + "</TD>"
			;
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:TotalsTable; \">\n";
		return s;
	}
	private String createEntriesTable(
			SMMasterEditEntry sm, 
			BKBankStatement statement
			) throws Exception{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:EntriesTable; background-color: "
			+ SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_GREEN + "; \" >\n";
//		s += "<TABLE class = \" innermost \" style=\" title:EntriesTable; \" >\n";

		//Build the header:
		s += " <TR>";
		s += "<TD class = \" fieldleftheading \">" + "Source" + "</TD>";
		s += "<TD class = \" fieldleftheading \">" + "Doc Number" + "</TD>";
		s += "<TD class = \" fieldrightheading \">" + "Cleared" + "</TD>";			
		s += "<TD class = \" fieldrightheading \">" + "Amount" + "</TD>";
		s += "<TD class = \" fieldleftheading \">" + "Entry date" + "</TD>";
		s += "<TD class = \" fieldrightheading \">" + "Batch" + "</TD>";
		s += "<TD class = \" fieldrightheading \">" + "Entry" + "</TD>";
		s += "<TD class = \" fieldleftheading \">" + "Description" + "</TD>";
		if (statement.get_iposted().compareToIgnoreCase("1") != 0){		
			s += "<TD class = \" fieldleftheading \">" + "Delete?" + "</TD>";
		}
		s += "</TR>\n\n"
		;
		
		//Here we want to list ANY bank account entries with the selected bank's GL account:
		BKBank bank = new BKBank();
		bank.setslid(statement.get_lbankid());
		try {
			bank.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName());
		} catch (Exception e1) {
			throw new Exception("Error [1403554874] - could not load bank information - " + e1.getMessage());
		}
		
		String SQL = "SELECT"
			+ " " + SMTablebkaccountentries.bdamount + " AS AMT"
			+ ", " + SMTablebkaccountentries.datentrydate + " AS ENTRYDATE"
			+ ", " + SMTablebkaccountentries.ibatchnumber + " AS BATCHNUMBER"
			+ ", " + SMTablebkaccountentries.ibatchentrynumber + " AS ENTRYNUMBER"
			+ ", " + SMTablebkaccountentries.icleared + " AS CLEARED"
			+ ", " + SMTablebkaccountentries.ientrytype + " AS ENTRYTYPE"
			+ ", " + SMTablebkaccountentries.lid + " AS LID"
			+ ", " + SMTablebkaccountentries.lstatementid + " AS STATEMENTID"
			+ ", " + SMTablebkaccountentries.sdescription + " AS DESCRIPTION"
			+ ", " + SMTablebkaccountentries.sdocnumber + " AS DOCNUMBER"
			+ ", " + SMTablebkaccountentries.ssourcemodule + " AS SOURCE"
			+ ", 0 AS 'CLEARED'"
			+ " FROM " + SMTablebkaccountentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkaccountentries.sglaccount + " = '" + bank.getsglaccount() + "')"
				+ " AND (" 
			;
			 
			//If this statement is new, and not saved yet, we want to include ALL the entries which are not yet on a statement:
			if(statement.get_lid().compareToIgnoreCase(BKBankStatement.UNSAVED_STATEMENT_LID) == 0){
				SQL += "(" + SMTablebkaccountentries.lstatementid + " = " 
					+ Integer.toString(SMTablebkaccountentries.INITIAL_STATEMENT_ID_VALUE) + ")";
			//But if this statement has already been saved, then we ONLY want to see entries which are part of THIS STATEMENT.
			}else{
				SQL += "(" + SMTablebkaccountentries.lstatementid + " = " + statement.get_lid() + ")";
			}
			SQL += ")"
			+ ") ORDER BY " + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.ssourcemodule
			+ ", " + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.sdocnumber
		;
		if (statement.get_iposted().compareToIgnoreCase("1") == 0){
			//Only get the 'posted entries' for this statement:
			SQL = "SELECT"
				+ " " + SMTablebkpostedentries.bdamount + " AS AMT"
				+ ", " + SMTablebkpostedentries.datentrydate + " AS ENTRYDATE"
				+ ", " + SMTablebkpostedentries.ibatchnumber + " AS BATCHNUMBER"
				+ ", " + SMTablebkaccountentries.ibatchentrynumber + " AS ENTRYNUMBER"
				+ ", " + SMTablebkpostedentries.icleared + " AS CLEARED"
				+ ", " + SMTablebkpostedentries.ientrytype + " AS ENTRYTYPE"
				+ ", " + SMTablebkpostedentries.lid + " AS LID"
				+ ", " + SMTablebkpostedentries.lstatementid + " AS STATEMENTID"
				+ ", " + SMTablebkpostedentries.sdescription + " AS DESCRIPTION"
				+ ", " + SMTablebkpostedentries.sdocnumber + " AS DOCNUMBER"
				+ ", " + SMTablebkpostedentries.ssourcemodule + " AS SOURCE"
				+ " FROM " + SMTablebkpostedentries.TableName
				+ " WHERE ("
					+ "(" + SMTablebkpostedentries.lstatementid + " = " + statement.get_lid() + ")"
				+ ") ORDER BY " + SMTablebkpostedentries.TableName + "." + SMTablebkpostedentries.icleared + " DESC"
				+ ", " + SMTablebkpostedentries.TableName + "." + SMTablebkpostedentries.ssourcemodule
				+ ", " + SMTablebkpostedentries.TableName + "." + SMTablebkpostedentries.sdocnumber
			;
		}
		//System.out.println("[1403552192] SQL = " + SQL);
		boolean bIsOddNumberedRow = true;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createEntriesTable - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
					);
			while (rs.next()){
				//Build each line of the entry table:
				s += buildEntryLine(
					statement,
					rs.getLong("LID"),
					rs.getLong("STATEMENTID"),
					rs.getInt("CLEARED"),
					rs.getInt("ENTRYTYPE"),
					rs.getBigDecimal("AMT"),
					rs.getString("SOURCE"),
					rs.getString("DOCNUMBER"),
					rs.getLong("BATCHNUMBER"),
					rs.getLong("ENTRYNUMBER"),
					rs.getString("DESCRIPTION"),
					clsDateAndTimeConversions.resultsetDateStringToString(rs.getString("ENTRYDATE")),
					sm,
					bIsOddNumberedRow
					)
				;
				if (rs.getInt("CLEARED") != 1){
					if(rs.getInt("ENTRYTYPE") == SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL){
						bdOutstandingWithdrawalsTotal = bdOutstandingWithdrawalsTotal.add(
							rs.getBigDecimal("AMT"));
					}else{
						bdOutstandingDepositsTotal = bdOutstandingDepositsTotal.add(
							rs.getBigDecimal("AMT"));
					}
				}
				bIsOddNumberedRow = !bIsOddNumberedRow;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error loading bank account entries with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//Close the table:
		s += "</TABLE style = \" title:EntriesTable; \">\n";
		return s;
	}
	private String buildEntryLine(
		BKBankStatement statement,
		long lEntryID,
		long lStatementIDOnEntry,
		int iCleared,
		int iEntryType,
		BigDecimal bdAmount,
		String sSourceModule,
		String sDocNumber,
		long lBatchnumber,
		long lEntrynumber,
		String sDescription,
		String sEntryDate,
		SMMasterEditEntry sm,
		boolean bIsOddNumberedRow
		) throws Exception{
		String s = "";

		//If the statement is NOT posted, then the checkbox gets checked if the entry is cleared:
		String sCheckBoxChecked = "";
		if (iCleared == 1){
			sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		
		if (bIsOddNumberedRow){
			s += "<TR style = \" background-color: " + ENTRIES_TABLE_ODD_ROW_BG_COLOR + " \">"; 
		}else{
			s += "<TR>";
		}
		
		//First, store the entry ID for each one, so we can read them later, regardless of whether they are checked or not:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ENTRY_ID_MARKER + Long.toString(lEntryID) + "\" VALUE=\"" + "Y" + "\""
			+ " id=\"" + ENTRY_ID_MARKER + Long.toString(lEntryID) + "\"" + ">";
		
		s+= "<TD class=\" readonlyleftfield \">"
				+ sSourceModule
				+ "</TD>"
			;
		
		String sDocNumberLink = sDocNumber;
		if (sSourceModule.compareToIgnoreCase(SMModuleTypes.AP) == 0){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APViewTransactionInformation, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
				sDocNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewTransactionInformation" 
		    		+ "?" + SMTableaptransactions.loriginalbatchnumber + "=" + Long.toString(lBatchnumber)
		    		+ "&" + SMTableaptransactions.loriginalentrynumber + "=" + Long.toString(lEntrynumber)
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
		    		+ "\">" + sDocNumber + "</A>"
			    ;
			}
		}
		
		s+= "<TD class=\" readonlyleftfield \">"
				+ sDocNumberLink
				+ "</TD>"
		;
		
		//Only show a checkbox if the statement is not yet posted:
		String sCheckboxStatus = "";
		if (statement.get_iposted().compareToIgnoreCase("1") == 0){
			sCheckboxStatus = " disabled ";
		}

		s += "<TD class=\" fieldrightaligned \">"
			+ "<INPUT TYPE=CHECKBOX "
			+ sCheckBoxChecked
			+ " NAME=\"" + CHECKBOX_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
			+ " id = \"" + CHECKBOX_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
			+ sCheckboxStatus
			+ " onchange=\"updateTotals();\""
			+ " width=0.25>"
			+ "</TD>"
		;
		
		if (iEntryType == SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT){
			s+= "<TD class=\" readonlyrightfield \">"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount).replace(",", "")
				+ "<INPUT TYPE=HIDDEN" 
					+ " NAME=\"" + DEPOSIT_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
					+ " id=\"" + DEPOSIT_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
					+ " value= \"" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount) + "\""
				+ ">"
				+ "</TD>"
			;
		}else{
			s+= "<TD class=\" readonlyrightfield \">"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount).replace(",", "")
				+ "<INPUT TYPE=HIDDEN" 
					+ " NAME=\"" + WITHDRAWAL_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
					+ " id=\"" + WITHDRAWAL_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", OVERALL_NUMBER_LENGTH_WITH_PADDING) + "\""
					+ " value= \"" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount) + "\""
				+ ">"
				+ "</TD>"
			;
		}
		
		s+= "<TD class=\" readonlyleftfield \">"
				+ sEntryDate
				+ "</TD>"
		;
		
		String sBatchNumber = Long.toString(lBatchnumber);
		if (lBatchnumber <= 0){
			sBatchNumber = "(NA)";
		}
		String sBatchNumberLink = sBatchNumber;
		
		//If it's an AP entry, create a link to the AP batch:
		if (sSourceModule.compareToIgnoreCase(SMModuleTypes.AR) == 0){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.AREditBatches, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
				sBatchNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
		    		+ "?BatchNumber=" + sBatchNumber
		    		+ "&BatchType=" + Integer.toString(SMBatchTypes.AR_CASH)
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
		    		+ "\">" + sBatchNumber + "</A>"
			    ;
			}
		}
		
		//If it's an AP entry, create a link to the AP batch:
		if (sSourceModule.compareToIgnoreCase(SMModuleTypes.AP) == 0){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APEditBatches, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
				sBatchNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit" 
		    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
		    		+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
		    		+ "\">" + sBatchNumber + "</A>"
			    ;
			}
		}
		
		s+= "<TD class=\" readonlyrightfield \">"
				+ sBatchNumberLink
				+ "</TD>"
		;

		//Entry number link:
		String sEntryNumber = Long.toString(lEntrynumber);
		if (lEntrynumber <= 0){
			sEntryNumber = "(NA)";
		}
		String sEntryNumberLink = sEntryNumber;
		
		//If it's an AP entry, then there's no particular entry that the deposit points to:
		if (sSourceModule.compareToIgnoreCase(SMModuleTypes.AR) == 0){
			sEntryNumber = "(NA)";
		}
		
		//If it's an AP entry, create a link to the AP entry:
		if (sSourceModule.compareToIgnoreCase(SMModuleTypes.AP) == 0){
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APEditBatches, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
				sEntryNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit" 
		    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
		    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sEntryNumber
		    		+ "&" + SMTableapbatchentries.ientrytype + "=" + iEntryType
		    		+ "&" + "Editable" + "=" + "NO"
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
		    		+ "\">" + sEntryNumber + "</A>"
			    ;
			}
		}
		
		s+= "<TD class=\" readonlyrightfield \">"
				+ sEntryNumberLink
				+ "</TD>"
		;
		
		s+= "<TD class=\" readonlyleftfield \">"
				+ sDescription
				+ "</TD>"
		;
		//Delete button:
		if (statement.get_iposted().compareToIgnoreCase("1") != 0){
			s += "<TD>" + createDeleteEntryButton(Long.toString(lEntryID), clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAmount)) + "</TD>";
		}
		
		s += "</TR>\n\n";
		return s;
				
	}
	private String getHeaderString(
			String title, 
			String subtitle, 
			String sbackgroundcolor, 
			String sfontfamily, 
			String scompanyname){
		String s = SMUtilities.DOCTYPE
		+ "<HTML>"
		+ "<HEAD>";
		s += "<TITLE>" + subtitle + "</TITLE>"
		+ SMUtilities.faviconLink()
		//This line should keep the font widths 'screen' wide:
		+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
		+ "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
		+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>"
		+ "</HEAD>\n" 
		+ "<BODY BGCOLOR="
		+ "\"" 
		+ sbackgroundcolor
		+ "\""
		+ " style=\"font-family: " + sfontfamily + ";\""
		+ "\">"
		;
		s += "<TABLE BORDER=0>"
		+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	private String sCommandScripts(SMMasterEditEntry smmaster) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";
		
		
		s += "   window.addEventListener(\"beforeunload\",function(){\n" 
		  +  "     document.body.setAttribute(\"style\",\"pointer-events: none; cursor: not-allowed;\");\n"
		  +  "     document.documentElement.style.cursor = \"wait\";\n"
		  +"      });\n";

		s += "function initShortcuts() {\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        deletestatement();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        post();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+s\",function() {\n";
		s += "        save();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "}\n";
		s += "\n";

		s += "window.onload = function() {\n"
			+ "    initShortcuts();\n"
			+ "    calculateentrytotal();\n"
			+ "}\n"
		;
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
			//Check to see if the date fields were changed, and if so, flag the record was changed field:
			//+ "    if (document.getElementById(\"" + PROPOSALDATE_PARAM + "\").value != " 
			//	+ "document.getElementById(\"" + SMProposal.ParamdatproposalDate + "\").value){\n"
			//+ "        flagDirty();\n"
			//+ "    }\n"			
			
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this statement?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		//***********************
		//Post
		s += "function post(){\n"
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        alert ('You have made changes that must be saved before posting.');\n"
			+ "        return;\n"
			+ "    }\n"
			+ "    if (confirm(\"Are you sure you want to post this statement?\")){\n"
			+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + POSTCOMMAND_VALUE + "\";\n"
			+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "    }\n"
			+ "}\n"
		;
		
		//Save
		s += "function save(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + SAVECOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
			+ "}\n"
		;
		
		//Delete
		s += "function deletestatement(){\n"
				+ "    if (confirm(\"Are you sure you want to delete this statement?\")){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETECOMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "    }\n"
			+ "}\n"
		;
		
		//Add new entry:
		s += "function addentry(){\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" + RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        alert ('You have made changes that must be saved before adding an entry.');\n"
				+ "        return;\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + ADDENTRY_VALUE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
		
		//Delete entry:
		s += "function deleteentry(sLid, sAmt){\n"
				+ "    if (confirm('Are you sure you want to delete this entry for ' + sAmt + '?')){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = '" + DELETEENTRYCOMMAND_VALUE + "' + sLid;\n"
				+ "        document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "    }\n"
				+ "}\n"
			;
		
		//Flag dirty:
		s += "function flagDirty() {\n"
				+ "    flagRecordChanged();\n"
				+ "}\n"
			;

		s += "function flagRecordChanged() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n"
			;

		s += "function updateTotals() { \n"
			+ "    flagDirty(); \n"
			+ "    calculateentrytotal(); \n"
			+ "} \n\n"
		;
		s += "function getFloat(value) {\n"
				+ "    return parseFloat(value.replace(',',''), 10);\n"
				+ "}\n"
			;
		s += "function calculateentrytotal(){\n"
			+ "    var deposittotal = getFloat(\"0.00\");\n"
			+ "    var withdrawaltotal = getFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + BKBankStatement.Parambdstartingbalance + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        var adjustedbankbalance = getFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        var adjustedbankbalance = getFloat(temp);\n"
			+ "    }\n"
			+ "    temp = document.getElementById(\"" + BKBankStatement.Parambdstatementbalance + "\").value.replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        var enteredglbalance = getFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        var enteredglbalance = getFloat(temp);\n"
			+ "    }\n"
			+ "    var calculatedglbalance = getFloat(\"0.00\");\n"
			+ "	   for (i=0; i<document.forms[\"MAINFORM\"].elements.length; i++){\n"
   			+ "	       var testName = document.forms[\"MAINFORM\"].elements[i].name;\n"
   			+ "        if (testName.substring(0, " + Integer.toString(DEPOSIT_ENTRY_AMOUNT_MARKER.length()) + "	) == \"" + DEPOSIT_ENTRY_AMOUNT_MARKER + "\"){\n"
   			+ "            var entrylid = testName.substr(testName.length - " + Integer.toString(OVERALL_NUMBER_LENGTH_WITH_PADDING) + "); \n"
   			+ "            var checkboxname = '" + CHECKBOX_MARKER + "' + testName.substr(testName.length - " + Integer.toString(OVERALL_NUMBER_LENGTH_WITH_PADDING) + "); \n"
   			+ "            if (document.getElementById(checkboxname).checked==false){ \n"
   			+ "                deposittotal = deposittotal + getFloat(document.forms[\"MAINFORM\"].elements[i].value.replace(',','')); \n"
   			+ "            } \n"
   			+ "        }\n"
   			+ "        if (testName.substring(0, " + Integer.toString(WITHDRAWAL_ENTRY_AMOUNT_MARKER.length()) + "	) == \"" + WITHDRAWAL_ENTRY_AMOUNT_MARKER + "\"){\n"
   			+ "            var entrylid = testName.substr(testName.length - " + Integer.toString(OVERALL_NUMBER_LENGTH_WITH_PADDING) + "); \n"
   			+ "            var checkboxname = '" + CHECKBOX_MARKER + "' + testName.substr(testName.length - " + Integer.toString(OVERALL_NUMBER_LENGTH_WITH_PADDING) + "); \n"
   			+ "            if (document.getElementById(checkboxname).checked==false){ \n"
   			+ "                withdrawaltotal = withdrawaltotal + getFloat(document.forms[\"MAINFORM\"].elements[i].value.replace(',','')); \n"
   			+ "            } \n"
   			+ "        }\n"
   			+ "    }\n"
   			+ "    deposittotal = Math.round(deposittotal * 100.00) / 100.00;\n"
   			+ "    document.getElementById(\"" + OUTSTANDING_DEPOSITS_TOTAL_FIELD + "\").innerText=deposittotal.toFixed(2);\n"
   			+ "    withdrawaltotal = Math.round(withdrawaltotal * 100.00) / 100.00;\n"
   			+ "    document.getElementById(\"" + OUTSTANDING_WITHDRAWALS_TOTAL_FIELD + "\").innerText=withdrawaltotal.toFixed(2);\n"
   			+ "    adjustedbankbalance = Math.round(adjustedbankbalance * 100.00) / 100.00;\n"
   			+ "    document.getElementById(\"" + ADJUSTED_BANK_BALANCE_FIELD + "\").innerText=adjustedbankbalance.toFixed(2);\n"
   			+ "    enteredglbalance = Math.round(enteredglbalance * 100.00) / 100.00;\n"
   			+ "    document.getElementById(\"" + ENTERED_GL_BALANCE_FIELD + "\").innerText=enteredglbalance.toFixed(2);\n"
   			+ "    calculatedglbalance = adjustedbankbalance + deposittotal + withdrawaltotal;\n"
   			+ "    document.getElementById(\"" + CALCULATED_GL_BALANCE_FIELD + "\").innerText=calculatedglbalance.toFixed(2);\n"
   			+ "} \n\n"	
			;
		
		s += "</script>\n";
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.innermost {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		//This is the def for a left aligned field:
		s +=
			"td.fieldleftaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right aligned field:
		s +=
			"td.fieldrightaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a read only field, left justified:
		s +=
			"td.readonlyleftfield {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: normal; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a read only field, right justified:
		s +=
			"td.readonlyrightfield {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: normal; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a control on the screen:
		s +=
			"td.fieldcontrol {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for an underlined left-aligned heading on the screen:
		s +=
			"td.fieldleftheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "text-decoration:underline; "
			+ "}"
			+ "\n"
			;

		//This is the def for an underlined right-aligned heading on the screen:
		s +=
			"td.fieldrightheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "text-decoration:underline; "
			+ "}"
			+ "\n"
			;

		
		//This is the def for the order lines heading:
		s +=
			"th.orderlineheading {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: text-bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;

		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}