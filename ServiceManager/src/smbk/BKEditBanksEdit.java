package smbk;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;

public class BKEditBanksEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		BKBank entry = new BKBank(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smbk.BKEditBanksAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.BKEditBanks
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.BKEditBanks)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(BKBank.ParamObjectName) != null){
	    	entry = (BKBank) currentSession.getAttribute(BKBank.ParamObjectName);
	    	currentSession.removeAttribute(BKBank.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKEditBanksSelect"
							+ "?" + BKBank.Paramlid + "=" + entry.getslid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
				}
	    	}
	    }
	    smedit.printHeaderTable();
	    
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Bank Function Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + BKBank.Paramlid + "=" + entry.getslid()
				+ "&Warning=Could not load bank with ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, BKBank entry) throws SQLException{

		String s = "<TABLE BORDER=1>";
		String sID = "NEW";
		if (
			(!sm.getAddingNewEntryFlag())
			|| (entry.getslid().compareToIgnoreCase("-1") != 0)
		){
			sID = entry.getslid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Bank ID</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramlid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		if (sm.getAddingNewEntryFlag()){
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					BKBank.Paramsshortname,
					entry.getsshortname().replace("\"", "&quot;"), 
					SMTablebkbanks.sshortnamelength, 
					"<B>Short name: <FONT COLOR=RED>*Required*</FONT></B>",
					"Use a short nickname here to quickly identify this bank",
					"40"
			);
		}else{
			s += "<TR><TD ALIGN=RIGHT><B>Short name: <FONT COLOR=RED>*Required*</FONT></B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsshortname() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramsshortname + "\" VALUE=\"" 
					+ entry.getsshortname() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
			
			s += "<TR><TD ALIGN=RIGHT><B>Date last maintained</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsdattimelastmaintained() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramdattimelastmaintained + "\" VALUE=\"" 
					+ entry.getsdattimelastmaintained() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;

			s += "<TR><TD ALIGN=RIGHT><B>Last maintained by</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + " " + entry.getslastmaintainedbyfullname() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramllastmaintainedbyid + "\" VALUE=\"" 
					+ entry.getllastmaintainedbyid() + "\">"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramslastmaintainedbyfullname + "\" VALUE=\"" 
					+ entry.getslastmaintainedbyfullname() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;
		}
        
		//Active?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			BKBank.Paramiactive, 
			Integer.parseInt(entry.getsactive()), 
			"Active?", 
			"Uncheck this to make the bank inactive");

		//Bank name
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsbankname,
				entry.getsbankname().replace("\"", "&quot;"), 
				SMTablebkbanks.sbanknamelength, 
				"<B>Name: <FONT COLOR=RED>*Required*</FONT></B>",
				"Name of bank",
				"40"
		);
		
		//Account name
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaccountname,
				entry.getsaccountname().replace("\"", "&quot;"), 
				SMTablebkbanks.saccountnamelength, 
				"<B>Account Name:</FONT></B>",
				"Name of account",
				"40"
		);

		//Account number
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaccountnumber,
				entry.getsaccountnumber().replace("\"", "&quot;"), 
				SMTablebkbanks.saccountnumberlength, 
				"<B>Account Number:</FONT></B>",
				"",
				"40"
		);
		
		//Routing number
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsroutingnumber,
				entry.getsroutingnumber().replace("\"", "&quot;"), 
				SMTablebkbanks.sroutingnumberlength, 
				"<B>Routing Number:</FONT></B>",
				"",
				"40"
		);

		//Address line 1:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaddressline1,
				entry.getsaddressline1().replace("\"", "&quot;"), 
				SMTablebkbanks.saddressline1length, 
				"<B>Address line 1:</FONT></B>",
				"",
				"40"
		);
		
		//Address line 2:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaddressline2,
				entry.getsaddressline2().replace("\"", "&quot;"), 
				SMTablebkbanks.saddressline2length, 
				"<B>Addres line 2:</FONT></B>",
				"",
				"40"
		);
		
		//Address line 3:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaddressline3,
				entry.getsaddressline3().replace("\"", "&quot;"), 
				SMTablebkbanks.saddressline3length, 
				"<B>Address line 3:</FONT></B>",
				"",
				"40"
		);
		
		//Address line 4:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsaddressline4,
				entry.getsaddressline4().replace("\"", "&quot;"), 
				SMTablebkbanks.saddressline4length, 
				"<B>Address line 4:</FONT></B>",
				"",
				"40"
		);
		
		//City:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramscity,
				entry.getscity().replace("\"", "&quot;"), 
				SMTablebkbanks.scitylength, 
				"<B>City:</FONT></B>",
				"",
				"40"
		);
		
		//State:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramsstate,
				entry.getsstate().replace("\"", "&quot;"), 
				SMTablebkbanks.sstatelength, 
				"<B>State:</FONT></B>",
				"",
				"40"
		);

		//Country
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramscountry,
				entry.getscountry().replace("\"", "&quot;"), 
				SMTablebkbanks.scountrylength, 
				"<B>Country:</FONT></B>",
				"",
				"40"
		);

		//Postal code
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramspostalcode,
				entry.getspostalcode().replace("\"", "&quot;"), 
				SMTablebkbanks.spostalcodelength, 
				"<B>Postal code:</FONT></B>",
				"",
				"40"
		);

		//GL Account:
		ArrayList<String>arrAccounts = new ArrayList<String>(0);
		ArrayList<String>arrAccountDescriptions = new ArrayList<String>(0);
		arrAccounts.add("");
		arrAccountDescriptions.add("** SELECT A GL ACCOUNT **");
		String SQL = "SELECT"
			+ " " + SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglaccounts.TableName
			+ " WHERE ("
				+ "(" + SMTableglaccounts.lActive + " = 1)"
			+ ") ORDER BY " + SMTableglaccounts.sAcctID
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".getEditHTML - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
						)
			);
			while (rs.next()){
				arrAccounts.add(rs.getString(SMTableglaccounts.sAcctID));
				arrAccountDescriptions.add(rs.getString(SMTableglaccounts.sAcctID) + " - " + rs.getString(SMTableglaccounts.sDesc));
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error [1403034441] listing GL accounts - " + e.getMessage());
		}
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			BKBank.Paramsglaccount, 
			arrAccounts, 
			entry.getsglaccount(), 
			arrAccountDescriptions, 
			"GL account:", 
			"Select the GL Account associated with this bank account.")
		;
		
		//Check form to be used:
		ArrayList<String>arrCheckFormIDs = new ArrayList<String>(0);
		ArrayList<String>arrCheckFormNames = new ArrayList<String>(0);
		arrAccounts.add("");
		arrAccountDescriptions.add("** SELECT A CHECK FORM **");
		SQL = "SELECT"
			+ " " + SMTableapcheckforms.lid
			+ ", " + SMTableapcheckforms.sname
			+ " FROM " + SMTableapcheckforms.TableName
			+ " ORDER BY " + SMTableapcheckforms.sname
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".getEditHTML (check forms) - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
						)
			);
			while (rs.next()){
				arrCheckFormIDs.add(rs.getString(SMTableapcheckforms.lid));
				arrCheckFormNames.add(rs.getString(SMTableapcheckforms.sname));
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error [1403034541] listing AP check forms - " + e.getMessage());
		}
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			BKBank.Paramicheckformid,
			arrCheckFormIDs, 
			entry.getscheckformid(),
			arrCheckFormNames, 
			"Check form:", 
			"Select the check form to be used this bank account.")
		;
		
		//Next check number:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				BKBank.Paramlnextchecknumber,
				entry.getsnextchecknumber().replace("\"", "&quot;"), 
				12, 
				"<B>Next check number:</FONT></B>",
				"",
				"40"
		);
		
		//Recent balance
		s += "<TR><TD ALIGN=RIGHT><B>Most recent balance</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsrecentbalance() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Parambdrecentbalance + "\" VALUE=\"" 
					+ entry.getsrecentbalance() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;
		//Recent balance date
		s += "<TR><TD ALIGN=RIGHT><B>Date of most recent balance</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsrecentbalancedate() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + BKBank.Paramdatrecentbalancedate + "\" VALUE=\"" 
					+ entry.getsrecentbalancedate() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;
		
		s += "</TABLE>";
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
