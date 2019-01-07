package smbk;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablebkaccountentries;
import ServletUtilities.clsManageRequestParameters;

public class BKAddEntryEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Account entry",
				SMUtilities.getFullClassName(this.toString()),
				"smbk.BKAddEntryAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.BKEditStatements
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.BKEditStatements)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		String sStatementID = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.lstatementid, request);
	    smedit.printHeaderTable();
		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to bank functions main menu</A><BR>");
	    
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKEditStatementSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to bank statement selections</A><BR><BR>");
		
		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);
		smedit.setUpdateButtonLabel("Add manual entry");
		//smedit.setbIncludeUpdateButton(false);
	    try {
			smedit.createEditPage(getEditHTML(smedit, sStatementID), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
    			+ "?" + BKBankStatement.Paramlid + "=" + sStatementID    				
    			+ "&Warning=Could not add new entry - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
    		;
    		if (sStatementID.compareToIgnoreCase("-1") == 0){
    			sRedirectString += "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y";
    		}
			response.sendRedirect(sRedirectString);
				return;
		}
	    return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			String sStatementID) throws SQLException{

		String sBankID = clsManageRequestParameters.get_Request_Parameter(BKBankStatement.Paramlbankid, sm.getRequest());
		//SMUtilities.printRequestParametersString(sm.getRequest());
		String sEntryDate = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.datentrydate, sm.getRequest());
		String sDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdocnumber, sm.getRequest());
		String sEntryType = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.ientrytype, sm.getRequest());
		String sDescription = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.sdescription, sm.getRequest());
		String sAmt = clsManageRequestParameters.get_Request_Parameter(SMTablebkaccountentries.bdamount, sm.getRequest());
		String s = "<TABLE BORDER=1>";

		s += "<TR><TD ALIGN=RIGHT><B>Statement ID</B>:</TD><TD><B>";
		if (sStatementID.compareToIgnoreCase("-1") == 0){
			s += "(NEW)";
		}else{
			s += sStatementID;
		}
		s += "</B></TD></TR>";

		//Entry date:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Entry date:&nbsp;</TD>"; 
		s += "<TD class=\"fieldcontrol\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMTablebkaccountentries.datentrydate + "\""
			+ " VALUE=\"" + sEntryDate.replace("\"", "&quot;") + "\""
			+ " id = \"" + SMTablebkaccountentries.datentrydate + "\""
			+ " SIZE=" + "9"
			+ " MAXLENGTH=" + "10"
			+ ">"
			+ SMUtilities.getDatePickerString(SMTablebkaccountentries.datentrydate, getServletContext())
			+ "</TD>"
		;
		s += "</TR>";
		//Amount:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Amount:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMTablebkaccountentries.bdamount + "\""
			+ " id = \"" + SMTablebkaccountentries.bdamount + "\""
			+ " VALUE=\"" + sAmt.replace("\"", "&quot;") + "\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + "15"
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		//Document number:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Document number:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMTablebkaccountentries.sdocnumber + "\""
			+ " id = \"" + SMTablebkaccountentries.sdocnumber + "\""
			+ " VALUE=\"" + sDocNumber.replace("\"", "&quot;") + "\""
			+ " SIZE=" + "30"
			+ " MAXLENGTH=" + Integer.toString(SMTablebkaccountentries.sdocnumberlength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		//Entry type:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Entry type:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
		;
		if (sEntryType.compareToIgnoreCase(Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT)) == 0){
			s += "<input type=\"radio\" name=\"" + SMTablebkaccountentries.ientrytype + "\" value=\"" 
			+ Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT) + "\"" + " checked" + ">" 
			+ SMTablebkaccountentries.getEntryLabel(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT) + "&nbsp;";

			s += "<input type=\"radio\" name=\"" + SMTablebkaccountentries.ientrytype + "\" value=\"" 
			+ Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL) + "\"" + " " + ">" 
			+ SMTablebkaccountentries.getEntryLabel(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL);

		}else{
			s += "<input type=\"radio\" name=\"" + SMTablebkaccountentries.ientrytype + "\" value=\"" 
			+ Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT) + "\"" + " " + ">" 
			+ SMTablebkaccountentries.getEntryLabel(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT) + "&nbsp;";

			s += "<input type=\"radio\" name=\"" + SMTablebkaccountentries.ientrytype + "\" value=\"" 
			+ Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL) + "\"" + " checked" + ">" 
			+ SMTablebkaccountentries.getEntryLabel(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL);
			
		}
		s += "</TD>"
		;
		s += "</TR>";
		//Description:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \">Description:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMTablebkaccountentries.sdescription + "\""
			+ " id = \"" + SMTablebkaccountentries.sdescription + "\""
			+ " VALUE=\"" + sDescription.replace("\"", "&quot;") + "\""
			+ " SIZE=" + "80"
			+ " MAXLENGTH=" + Integer.toString(SMTablebkaccountentries.sdescriptionlength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";
		//Get the GL account for this bank:
		BKBank bank = new BKBank();
		bank.setslid(sBankID);
		try {
			bank.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName());
		} catch (Exception e) {
			throw new SQLException("Could not load bank with ID '" + sBankID + "' - " + e.getMessage());
		}
		
		//Hidden values:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablebkaccountentries.lstatementid + "\" VALUE=\"" + sStatementID + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + BKBankStatement.Paramlbankid + "\" VALUE=\"" + sBankID + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablebkaccountentries.sglaccount + "\" VALUE=\"" + bank.getsglaccount() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, sm.getRequest()) + "\">\n";
		s += "</TABLE>\n";
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
