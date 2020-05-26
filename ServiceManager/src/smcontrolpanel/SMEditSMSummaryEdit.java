package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smap.APVendor;


public class SMEditSMSummaryEdit extends HttpServlet {
	
	public static final String SAVE_BUTTON_LABEL = "Save " + SMEstimateSummary.OBJECT_NAME;
	public static final String SAVE_COMMAND_VALUE = "SAVESUMMARY";
	public static final String DELETE_BUTTON_LABEL = "Delete " + SMEstimateSummary.OBJECT_NAME;
	public static final String DELETE_COMMAND_VALUE = "DELETESUMMARY";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		SMEstimateSummary summary = new SMEstimateSummary(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimateSummary.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditSMSummaryEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMEstimateSummary.OBJECT_NAME) != null){
	    	summary = (SMEstimateSummary) currentSession.getAttribute(SMEstimateSummary.OBJECT_NAME);
	    	currentSession.removeAttribute(SMEstimateSummary.OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	try {
					summary.load(getServletContext(), smedit.getsDBID(), smedit.getUserID());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
							+ "?" + SMTablesmestimatesummaries.lid + "=" + summary.getslid()
							+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
				}
	    	}
	    }
	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    try {
	    	createEditPage(getEditHTML(smedit, summary), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit
			);
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesmestimatesummaries.lid + "=" + summary.getslid()
				+ "&Warning=Could not load Summary ID: " + summary.getslid() + " - " + SMUtilities.URLEncode(sError)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
}
	
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm
	) throws Exception{
		//Create HTML Form
		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//Add save and delete buttons
		pwOut.println("<BR>" + createSaveButton() + "&nbsp;" + createDeleteButton());
		pwOut.println("</FORM>");
	}

	
	private String getEditHTML(SMMasterEditEntry sm, SMEstimateSummary summary) throws SQLException{
		
		String sControlHTML = "";
		
		String s = sCommandScripts(summary, sm);
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\""
				+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">" + "\n";
		
		String sLid = summary.getslid();
		if (sm.getAddingNewEntryFlag()){
			sLid = "(NEW)";
		}

		s += "<B>Summary ID</B>: " + sLid + "\n" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimatesummaries.lid + "\""
			+ " VALUE=\"" + summary.getslid()
			+ " ID=\"" + summary.getslid() + "\">"
			+ "\n"
		;
			
		s += "&nbsp;<B>Created by: </B>" 
			+ summary.getscreatedbyfullname()
			+ " on " + summary.getsdatetimecreated()
		;
		
		s += "&nbsp;<B>Last modified by: </B>" 
			+ summary.getslastmodifiedbyfullname()
			+ " on " + summary.getsdatetimeslastmodified()
		;
		
		//Header table:
		int iNumberOfColumns = 4;
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Project:"
			+ "</TD>" + "\n"
			+ "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns - 1) 
				+ " class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimatesummaries.sjobname + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.sjobname + "\""
			+ " VALUE=\"" + summary.getsjobname() + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sjobnameLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
			+ ">"
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		//Sales Lead
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "<B>Sales Lead ID:</B>"
			+ "</TD>" + "\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimatesummaries.lsalesleadid + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.lsalesleadid + "\""
			+ " VALUE=\"" + summary.getslsalesleadid() + "\""
			+ " MAXLENGTH=" + "15"
			+ " STYLE=\"width: 1in; height: 0.25in\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		//Tax Type:
		ArrayList<String> arrTaxes = new ArrayList<String>(0);
		ArrayList<String> arrTaxDescriptions = new ArrayList<String>(0);
		String SQL = "SELECT"
			+ " " + SMTabletax.lid
			+ ", " + SMTabletax.staxjurisdiction
			+ ", " + SMTabletax.staxtype
			+ " FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.iactive + " = 1)"
				+ " AND (" + SMTabletax.ishowinorderentry + " = 1)"
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
			s += "<B>Error [1590530698] reading tax info - " + e.getMessage() + "</B><BR>";
		}

		sControlHTML = "<SELECT NAME = \"" + SMTablesmestimatesummaries.itaxid + "\""
				+ " onchange=\"flagDirty();\""
				 + " >\n"
			;
		String sTempBuffer = "";
		int TAX_BUFFER_SIZE = 100;
			for (int i = 0; i < arrTaxes.size(); i++){
				sTempBuffer += "<OPTION";
				if (arrTaxes.get(i).toString().compareTo(summary.getsitaxid()) == 0){
					sTempBuffer += " selected=yes";
				}
				sTempBuffer += " VALUE=\"" + arrTaxes.get(i).toString() + "\">" + arrTaxDescriptions.get(i).toString() + "\n";
				if ((i % TAX_BUFFER_SIZE) == 0) {
					sControlHTML += sTempBuffer;
					sTempBuffer = "";
				}
			}
		//Get any remaining items from the buffer:
		sControlHTML += sTempBuffer;
		sControlHTML += "</SELECT>"
		;
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "<B>Tax type:</B>"
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ sControlHTML
				+ "</TD>" + "\n"
			;

		s += "  </TR>" + "\n";
		
		
		
		
		s += "</TABLE>" + "\n";
		
/*
			s += "<TR><TD ALIGN=RIGHT><B>Date last edited</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + summary.getsdatelastmaintained() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendor.Paramdatlastmaintained + "\" VALUE=\"" 
					+ summary.getsdatelastmaintained() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;

			s += "<TR><TD ALIGN=RIGHT><B>Last edited by</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + summary.getslasteditedby() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendor.Paramslasteditedby + "\" VALUE=\"" 
					+ summary.getslasteditedby() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;
        
		//Vendor name
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsname,
				summary.getsname().replace("\"", "&quot;"), 
				SMTableicvendors.snameLength, 
				"<B>Name: <FONT COLOR=RED>*Required*</FONT></B>",
				"Vendor's company name",
				"40",
				"flagDirty();"
		);
		
		//Terms:
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
					+ ".getEditHTML - user: " 
							+ sm.getUserID()
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
			s += "<BR><B>Error [1451600175] reading terms codes - " + e.getMessage() + ".</B><BR>";
		}
		
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				APVendor.Paramsterms, 
			arrTerms, 
			summary.getsterms(), 
			arrTermsDescriptions, 
			"Payment terms <FONT COLOR=RED>*Required*</FONT>", 
			"",
			"flagDirty();"
		);

		//On payment hold?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			APVendor.Paramionpaymenthold, 
			Integer.parseInt(summary.getionpaymenthold()), 
			"On payment hold?", 
			"Check this to prevent payments to this vendor",
			"flagDirty();"
			);
		
		//Active?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			APVendor.Paramiactive, 
			Integer.parseInt(summary.getsactive()), 
			"Active?", 
			"Uncheck this to make the vendor inactive",
			"flagDirty();"
			);

		//PO confirmation required?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			APVendor.Paramipoconfirmationrequired, 
			Integer.parseInt(summary.getspoconfirmationrequired()), 
			"PO confirmation required?", 
			"Check this if the vendor normally sends an acknowledgment before they ship product",
			"flagDirty();"
			);

		//Address line 1
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsaddressline1,
				summary.getsaddressline1().replace("\"", "&quot;"), 
				SMTableicvendors.saddressline1Length, 
				"<B>Address line 1:</B>",
				"First line of billing address",
				"40",
				"flagDirty();"
		);

		//Address line 2
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsaddressline2,
				summary.getsaddressline2().replace("\"", "&quot;"), 
				SMTableicvendors.saddressline2Length, 
				"<B>Address line 2:</B>",
				"Second line of billing address",
				"40",
				"flagDirty();"
		);
		
		//Address line 3
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsaddressline3,
				summary.getsaddressline3().replace("\"", "&quot;"), 
				SMTableicvendors.saddressline3Length, 
				"<B>Address line 3:</B>",
				"Third line of billing address",
				"40",
				"flagDirty();"
		);
		
		//Address line 4
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsaddressline4,
				summary.getsaddressline4().replace("\"", "&quot;"), 
				SMTableicvendors.saddressline4Length, 
				"<B>Address line 4:</B>",
				"Fourth line of billing address",
				"40",
				"flagDirty();"
		);

		//City
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramscity,
				summary.getscity().replace("\"", "&quot;"), 
				SMTableicvendors.scityLength, 
				"<B>City:</B>",
				"&nbsp;",
				"40",
				"flagDirty();"
		);
		
		//State
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsstate,
				summary.getsstate().replace("\"", "&quot;"), 
				SMTableicvendors.sstateLength, 
				"<B>State:</B>",
				"&nbsp;",
				"40",
				"flagDirty();"
		);

		//Postal code
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramspostalcode,
				summary.getspostalcode().replace("\"", "&quot;"), 
				SMTableicvendors.spostalcodeLength, 
				"<B>Zip code:</B>",
				"(No punctuation or spaces)",
				"40",
				"flagDirty();"
		);

		//Country
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramscountry,
				summary.getscountry().replace("\"", "&quot;"), 
				SMTableicvendors.scountryLength, 
				"<B>Country:</B>",
				"&nbsp;",
				"40",
				"flagDirty();"
		);

		//Contact name
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramscontactname,
				summary.getscontactname().replace("\"", "&quot;"), 
				SMTableicvendors.scontactnameLength, 
				"<B>Contact name:</B>",
				"&nbsp;",
				"40",
				"flagDirty();"
		);

		//Phone number
		if(summary.getsphonenumber().replace("\"", "&quot;").compareToIgnoreCase("")==0) {
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					APVendor.Paramsphonenumber,
					summary.getsphonenumber().replace("\"", "&quot;"), 
					SMTableicvendors.sphonenumberLength, 
					"<B>Phone number:</B>",
					"(No punctuation)",
					"40",
					"flagDirty();"
			);
		} else {
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					APVendor.Paramsphonenumber,
					summary.getsphonenumber().replace("\"", "&quot;"), 
					SMTableicvendors.sphonenumberLength, 
					"<B><A HREF=\"tel:" + summary.getsphonenumber().replace("\"", "&quot;") + "\">Phone number:</A></B>",
					"(No punctuation)",
					"40",
					"flagDirty();"
			);
		}

		
		//Fax number
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsfaxnumber,
				summary.getsfaxnumber().replace("\"", "&quot;"), 
				SMTableicvendors.sfaxnumberLength, 
				"<B>Fax number:</B>",
				"(No punctuation)",
				"40",
				"flagDirty();"
		);
		
		//Company account code:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramscompanyaccountcode,
				summary.getscompanyaccountcode().replace("\"", "&quot;"), 
				SMTableicvendors.scompanyaccountcodeLength, 
				"<B>Company account code:</B>",
				"The account code this vendor uses to identify our company",
				"40",
				"flagDirty();"
		);
		
		//Email address
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsvendoremail,
				summary.getsvendoremail().replace("\"", "&quot;"), 
				SMTableicvendors.svendoremailLength, 
				"<B>Email Address:</B>",
				"Example: someone@somewhere.com",
				"40",
				"flagDirty();"
				);
		
		//Web address
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramswebaddress,
				summary.getswebaddress().replace("\"", "&quot;"), 
				SMTableicvendors.swebaddressLength, 
				"<B>Web address:</B>",
				"Example: www.somewebsite.com",
				"40",
				"flagDirty();"
		);
		
		//Account Set:
		ArrayList<String> arrAccountSetIDs = new ArrayList<String>(0);
		ArrayList<String> arrAccountSetNames = new ArrayList<String>(0);
		String sSQL = "SELECT"
			+ " " + SMTableapaccountsets.lid
			+ ", " + SMTableapaccountsets.sacctsetname
			+ ", " + SMTableapaccountsets.sdescription				
			+ " FROM " + SMTableapaccountsets.TableName
			+ " ORDER BY " + SMTableapaccountsets.lid 
		;
		//First, add an account set item so we can be sure the user chose one:
		arrAccountSetIDs.add("");
		arrAccountSetNames.add("*** Select account set ***");
		arrAccountSetIDs.add("0");
		arrAccountSetNames.add("None");
				
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
				sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
				+ ".getEditHTML - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
					);
			while (rs.next()) {
				arrAccountSetIDs.add(rs.getString(SMTableapaccountsets.lid));
				arrAccountSetNames.add(
					rs.getString(SMTableapaccountsets.sacctsetname)
				);
			}
				rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1451600174] reading account set codes - " + e.getMessage() + ".</B><BR>";
		}		
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
					APVendor.Paramiapaccountset, 
					arrAccountSetIDs, 
					summary.getiapaccountset(), 
					arrAccountSetNames, 
					"Account Set: <FONT COLOR=RED>*Required*</FONT>", 
					"Set of general ledger accounts associated with this vendor's transactions.",
					"flagDirty();"
				);
				
		//Banks:
		ArrayList<String> arrBankIDs = new ArrayList<String>(0);
		ArrayList<String> arrBankDescriptions = new ArrayList<String>(0);
		 sSQL = "SELECT"
			+ " " + SMTablebkbanks.lid
			+ ", " + SMTablebkbanks.saccountname
			+ " FROM " + SMTablebkbanks.TableName
			+ " ORDER BY " + SMTablebkbanks.lid 
		;
		//First, add a bank account so we can be sure the user chose one:
		 arrBankIDs.add("");
		 arrBankDescriptions.add("*** Select bank account ***");
				
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			while (rs.next()) {
				arrBankIDs.add(rs.getString(SMTablebkbanks.lid));
				arrBankDescriptions.add(rs.getString(SMTablebkbanks.saccountname)
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1451600173] reading bank account codes - " + e.getMessage() + ".</B><BR>";
		}
		
		//In the event that there WERE no banks set up, add the option to choose 'NONE':
		if(arrBankIDs.size() == 1) {
		 arrBankIDs.add("0");
		 arrBankDescriptions.add("None");
		}
		
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				APVendor.Paramibankcode, 
				arrBankIDs, 
				summary.getibankcode(), 
				arrBankDescriptions, 
				"Bank Account: <FONT COLOR=RED>*Required*</FONT>", 
				"Bank from which checks for this vendor will be drawn.",
				"flagDirty();"
			);

		//Vendor groups:
		ArrayList<String> arrVendorGroupIDs = new ArrayList<String>(0);
		ArrayList<String> arrVendorGroupDescriptions = new ArrayList<String>(0);
		 sSQL = "SELECT"
			+ " " + SMTableapvendorgroups.lid
			+ ", " + SMTableapvendorgroups.sgroupid
			+ ", " + SMTableapvendorgroups.sdescription
			+ " FROM " + SMTableapvendorgroups.TableName
			+ " ORDER BY " + SMTableapvendorgroups.sgroupid 
		;
		//First, add a Vendor Group so we can be sure the user chose one:
		 arrVendorGroupIDs.add("");
		 arrVendorGroupDescriptions.add("*** Select vendor group ***");
				
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName());
			while (rs.next()) {
				arrVendorGroupIDs.add(rs.getString(SMTableapvendorgroups.lid));
				arrVendorGroupDescriptions.add(rs.getString(SMTableapvendorgroups.sgroupid)
					+ " - " + rs.getString(SMTableapvendorgroups.sdescription)
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1498827736] reading bank account codes - " + e.getMessage() + ".</B><BR>";
		}			
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			APVendor.Paramivendorgroupid,
			arrVendorGroupIDs, 
			summary.getsvendorgroupid(), 
			arrVendorGroupDescriptions, 
			"Vendor group: <FONT COLOR=RED>*Required*</FONT>", 
			"",
			"flagDirty();"
		);		
		
		
		//Backcharge memo:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
				APVendor.Parammbackchargememo,
				summary.getmbackchargememo().replace("\"", "&quot;"), 
				"<B>Backcharge memo:</B>",
				"Include any notes here relating to backcharging this vendor, such as hourly rates, whether they pay for travel time, etc.",
				3,
				80,
				"flagDirty();"
		);
		
		//Invoicing defaults:
		s += "<TR style=\"background-color:grey; color:white; \" \"><TD COLSPAN=3>"
			+ "<B>&nbsp;INVOICING DEFAULTS</B>"
			+ "</TD></TR>"
		;
		
		s += "<TR ><TD COLSPAN=3>"
				+ "<I>Select a default distribution code OR a default expense account OR neither, but not both:</I>"
				+ "</TD></TR>"
			;
		
		//Default Distribution code:
		ArrayList<String> arrDisCodes = new ArrayList<String>(0);
		ArrayList<String> arrDistCodeDescriptions = new ArrayList<String>(0);
		 sSQL = "SELECT * "
			+ " FROM " + SMTableapdistributioncodes.TableName
			+ " ORDER BY " + SMTableapdistributioncodes.sdistcodename 
		;
		//First, add expense account so we can be sure the user chose one:
		 arrDisCodes.add("");
		 arrDistCodeDescriptions.add("*** Select default distribution code ***");
		 arrDisCodes.add("-1");
		 arrDistCodeDescriptions.add("Not used");
						
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			while (rs.next()) {
				arrDisCodes.add(rs.getString(SMTableapdistributioncodes.sdistcodename));
				arrDistCodeDescriptions.add(rs.getString(SMTableapdistributioncodes.sdistcodename)
				+ " "
				+ rs.getString(SMTableapdistributioncodes.sdescription)
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1490827928] reading default distribution codes - " + e.getMessage() + ".</B><BR>";
		}			
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				APVendor.Paramsdefaultdistributioncode, 
				arrDisCodes, 
				summary.getsdefaultdistributioncode(), 
				arrDistCodeDescriptions, 
				"Default Distribution Code:", 
				"New invoice entries for this vendor will default to this distribution code.",
				"setDefaultGLAccountToNotUsed(this);"
			);
		
		//Default Expense Account:
		ArrayList<String> arrGLAcctIDs = new ArrayList<String>(0);
		ArrayList<String> arrGLAcctDescriptions = new ArrayList<String>(0);
		 sSQL = "SELECT * "
			+ " FROM " + SMTableglaccounts.TableName
			+ " ORDER BY " + SMTableglaccounts.sAcctID 
		;
		//First, add expense account so we can be sure the user chose one:
		 arrGLAcctIDs.add("");
		 arrGLAcctDescriptions.add("*** Select default expense account ***");
		 arrGLAcctIDs.add("-1");
		 arrGLAcctDescriptions.add("Not used");
						
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			while (rs.next()) {
				arrGLAcctIDs.add(rs.getString(SMTableglaccounts.sAcctID));
				String sInactive = "";
				if(rs.getLong(SMTableglaccounts.lActive) == 0){
					sInactive = "(Inactive)";
				}
				arrGLAcctDescriptions.add(rs.getString(SMTableglaccounts.sFormattedAcct)
				+ " "
				+ rs.getString(SMTableglaccounts.sDesc)
				+ " "
				+ sInactive
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1451600132] reading default expense account codes - " + e.getMessage() + ".</B><BR>";
		}			
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				APVendor.Paramsdefaultexpenseacct, 
				arrGLAcctIDs, 
				summary.getsdefaultexpenseacct(), 
				arrGLAcctDescriptions, 
				"Default Expense Account:", 
				"New invoice entries for this vendor will default to this expense account.",
				"setDefaultDistCodeToNotUsed(this);"
			);
		
		//Default invoice line description:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				APVendor.Paramsdefaultinvoicelinedescription,
				summary.getsdefaultinvoicelinedescription().replace("\"", "&quot;"), 
				SMTableicvendors.sdefaultinvoicelinedesclength, 
				"<B>Default Invoice Line Description:</B>",
				"New invoice entries for this vendor will default to this line description.",
				"40",
				"flagDirty();"
		);
		
		//Generate a separate check for each invoice:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			APVendor.Paramigenerateseparatepaymentsforeachinvoice,
			Integer.parseInt(summary.getsgenerateseparatepaymentsforeachinvoice()), 
			"Separate check for each invoice?", 
			"Checking this will cause each invoice to be paid with a separate check",
			"flagDirty();"
			);
		
		//Tax reporting:
		s += "<TR style=\"background-color:grey; color:white; \" \"><TD COLSPAN=3>"
			+ "<B>&nbsp;TAX REPORTING</B>"
			+ "</TD></TR>"
		;
		
		ArrayList<String>arrTaxReportingTypes = new ArrayList<String>(0);
		ArrayList<String>arrTaxReportingTypeDescriptions = new ArrayList<String>(0);
		for (int i = 0; i < SMTableicvendors.NUMBER_OF_TAX_REPORTING_TYPES; i++){
			arrTaxReportingTypes.add(Integer.toString(i));
			arrTaxReportingTypeDescriptions.add(SMTableicvendors.getTaxReportingTypeDescriptions(i));
		}
		s += "<TR>";
		s += "<TD ALIGN=RIGHT><B>" + "Tax reporting type:" + " </B></TD>";
		s += "<TD ALIGN=LEFT> <SELECT NAME = \"" + APVendor.Paramitaxreportingtype + "\""
				+ " ID=\"" + APVendor.Paramitaxreportingtype + "\""
				+ " ONCHANGE=\"" + "flagDirty();checkReportingType();" + "\">";
		for (int i = 0; i < arrTaxReportingTypes.size(); i++){
			s += "<OPTION";
			if (arrTaxReportingTypes.get(i).toString().compareTo(summary.getstaxreportingtype()) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrTaxReportingTypes.get(i).toString() + "\">" + arrTaxReportingTypeDescriptions.get(i).toString();
		}
		s += "</SELECT></TD>";
		s += "<TD ALIGN=LEFT>" + "Select the type of tax reporting for this vendor" + "</TD>";
		s += "</TR>";

		//Start table body tag to display or hide tax reporting options based on tax reporting type selected
		s += "<tbody id=\"taxoptions\">";
		
		ArrayList<String>arr1099CPRSCodeIDs = new ArrayList<String>(0);
		ArrayList<String>arr1099CPRSCodeDescriptions = new ArrayList<String>(0);
		arr1099CPRSCodeIDs.add("0");
		arr1099CPRSCodeDescriptions.add("None");

		 sSQL = "SELECT * "
			+ " FROM " + SMTableap1099cprscodes.TableName
			+ " ORDER BY " + SMTableap1099cprscodes.lid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML.reading 1099/CPRS codes- user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			while (rs.next()) {
				arr1099CPRSCodeIDs.add(Long.toString(rs.getLong(SMTableap1099cprscodes.lid)));
				String sInactive = "";
				if(rs.getLong(SMTableap1099cprscodes.iactive) == 0){
					sInactive = "(Inactive)";
				}
				arr1099CPRSCodeDescriptions.add(rs.getString(SMTableap1099cprscodes.sclassid)
				+ " "
				+ rs.getString(SMTableap1099cprscodes.sdescription)
				+ " "
				+ sInactive
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<B>Error [1451598881] reading AP 1099/CPRS codes - " + e.getMessage() + ".</B><BR>";
		}
		
		s += "<TR>";
		s += "<TD ALIGN=RIGHT><B>" + "1099/CPRS Code:" + " </B></TD>";
		s += "<TD ALIGN=LEFT> <SELECT NAME = \"" + APVendor.Parami1099CPRSid + "\""
			+ " ONCHANGE=\"" + "flagDirty();" + "\""
			+ " ID=\"" + APVendor.Parami1099CPRSid + "\">";
		for (int i = 0; i < arr1099CPRSCodeIDs.size(); i++){
			s += "<OPTION";
			if (arr1099CPRSCodeIDs.get(i).toString().compareTo(summary.gets1099CPRSid()) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arr1099CPRSCodeIDs.get(i).toString() + "\">" + arr1099CPRSCodeDescriptions.get(i).toString();
		}
		s += "</SELECT></TD>";
		s += "<TD ALIGN=LEFT>" + "Select code for tax reporting" + "</TD>";
		s += "</TR>";
		
		
		//Tax ID number:
		s += "<TR>";
		s += "<TD ALIGN=RIGHT>"
			+ "<B>Tax ID:</B>"
			+ "</TD>"
		;
		s += "<TD ALIGN=LEFT>";
		s += "<INPUT TYPE=TEXT NAME=\"" + APVendor.Paramstaxidentifyingnumber + "\""
			+ " ID=\"" + APVendor.Paramstaxidentifyingnumber + "\""
			+ " VALUE=\"" + summary.getstaxidentifyingnumber() + "\""
		    + "SIZE=" + "20"
			+ " MAXLENGTH=" + Integer.toString(SMTableicvendors.staxidentifyingnumberlength)
			+ " ONCHANGE=\"flagDirty();\""
			+ ">"
		;
		//List the tax ID types:
		s += "&nbsp;ID type:";
		s += "<SELECT NAME = \"" + APVendor.Paramitaxidnumbertype + "\"" 
		+ " ID=\"" + APVendor.Paramitaxidnumbertype + "\""
		+ " ONCHANGE=\"flagDirty();\""+ ">";
		for (int i = 0; i < SMTableicvendors.NUMBER_OF_TAX_ID_TYPES; i++){
			String sTaxNumberTypeID = Integer.toString(i);
			s += "<OPTION ";
			if (summary.getstaxidnumbertype().compareToIgnoreCase(sTaxNumberTypeID) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + sTaxNumberTypeID + "\">" + SMTableicvendors.getTaxIDTypeDescriptions(i);
		}
		s += "</SELECT>";
		s += "</TD>";
		
		s += "<TD ALIGN=LEFT>"
			+ " The Tax ID provided by the vendor"
			+ "</TD>"
		;
		s += "</TR>";	
		//End table body tag for hiding tax options
		s += "</tbody>";
		
		
		//Remit to locations:
		s += "<TR style=\"background-color:grey; color:white; \"><TD COLSPAN=3>"
			+ "<B>&nbsp;REMIT TO LOCATIONS</B>"
			+ "</TD></TR>"
		;
		
		//Primary remit to location:
		//Default Expense Account:
		ArrayList<String> arrRemitToCodes = new ArrayList<String>(0);
		ArrayList<String> arrRemitToDescriptions = new ArrayList<String>(0);
		sSQL = "SELECT * "
			+ " FROM " + SMTableapvendorremittolocations.TableName
			+ " WHERE (" + SMTableapvendorremittolocations.svendoracct + "='" + summary.getsvendoracct() + "')"
			+ " ORDER BY " + SMTableapvendorremittolocations.sremittocode 
		;
		//First, add expense account so we can be sure the user chose one:
		arrRemitToCodes.add("");
		arrRemitToDescriptions.add("NONE");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML.getting remit to codes - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
			while (rs.next()) {
				arrRemitToCodes.add(rs.getString(SMTableapvendorremittolocations.sremittocode));
				String sInactive = "";
				if(rs.getLong(SMTableapvendorremittolocations.iactive) == 0){
					sInactive = "(Inactive)";
				}
				arrRemitToDescriptions.add(rs.getString(SMTableapvendorremittolocations.sremittocode)
				+ " "
				+ rs.getString(SMTableapvendorremittolocations.sremittoname)
				+ " "
				+ sInactive
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B>Error [1451596219] reading primary remit to codes - " + e.getMessage() + ".</B><BR>";
		}			
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				APVendor.Paramsprimaryremittocode, 
				arrRemitToCodes, 
				summary.getsprimaryremittocode(), 
				arrRemitToDescriptions, 
				"Primary remit to location:", 
				"This will be the default remit to location for this vendor",
				"flagDirty();"
			);
		
		//Remit to Locations
		s += "<TR><TD ALIGN=RIGHT><B>Edit Remit To Locations</B>:</TD>";
		s += "<TD>" + createEditRemitToLocationsButton()+ "</TD>"
		+ "<TD>Use button to edit remit to locations</TD>"
		+ "</TR>"
		;
				
		s += "</TABLE>";
		
		//Add a field for Google Docs link
				String sCreateAndUploadButton = "";
				if (
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMCreateGDriveVendorFolders, 
						sm.getUserID(), 
						getServletContext(), 
						sm.getsDBID(),
						(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					)
					&& (!sm.getAddingNewEntryFlag())
				){
					sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
				}
					s += "<BR><B><FONT SIZE=3>Google Drive link:</FONT></B>" + "&nbsp;" + sCreateAndUploadButton + "<BR>"
						+ "<INPUT TYPE=TEXT NAME=\"" + APVendor.Paramsgdoclink + "\""
						+ " onchange=\"flagDirty();\""
						+ " VALUE=\"" + summary.getsgdoclink().replace("\"", "&quot;") + "\""
						+ "SIZE=" + "70"
						+ " MAXLENGTH=" + Integer.toString(254)
						+ "<BR><BR>"
					;
					
		*/
		return s;
	}
	private String sCommandScripts(
			SMEstimateSummary summary, 
			SMMasterEditEntry smmaster
			) throws SQLException{
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
					
			//Prompt to save:
			s += "window.onbeforeunload = promptToSave;\n";
			
			s += "window.onload = checkReportingType;\n";

			s += "function promptToSave(){\n"		
				
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + SAVE_COMMAND_VALUE + "\""
						+ " && document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + DELETE_COMMAND_VALUE + "\"){\n"
				+ "        return 'You have unsaved changes!';\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n\n"
			;
			
			//Delete:
			s += "function isdelete(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
				//+ "    }\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			
			//Edit Locations
			/*
			s += "function editlocations(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + EDIT_LOCATIONS_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			*/
			
			//Create folder and/or upload files:
			/*
			s += "function createanduploadfolder(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
				;
			*/
			//Check Reporting type
			s += "function checkReportingType(){\n"
				+ "	 if(document.getElementById(\"" + APVendor.Paramitaxreportingtype + "\").value == \"" + Integer.toString(SMTableicvendors.TAX_REPORTING_TYPE_NONE) + "\"){\n"
					//Hide tax options if reporting type is none
				+ "    document.getElementById(\"" + "taxoptions" + "\").style.display =\"none\" ;\n"
				+ "	 }else{\n"
					//Otherwise, show tax options
				+ "    document.getElementById(\"" + "taxoptions" + "\").style.display =\"table-row-group\" ;\n"
				+ "  }\n"
				+ "}\n"
			;
			
			//Toggle the default distribution code and default GL account on and off:
			s += "function setDefaultGLAccountToNotUsed(objControl){\n"
				+ "    var idx = objControl.selectedIndex;\n"
				+ "    selectedvalue = objControl.options[idx].value;\n"
				+ "    if (selectedvalue == ''){\n"
				+ "        return; \n"
				+ "    }\n"
				+ "    if (selectedvalue == '-1'){\n"
				+ "        return; \n"
				+ "    }\n"
				+ "    document.getElementById(\"" + APVendor.Paramsdefaultexpenseacct + "\").value = \"" + "-1" + "\";\n"
				+ "    flagDirty();"
				//+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
			//Toggle the default distribution code and default GL account on and off:
			s += "function setDefaultDistCodeToNotUsed(objControl){\n"
				+ "    var idx = objControl.selectedIndex;\n"
				+ "    selectedvalue = objControl.options[idx].value;\n"
				+ "    if (selectedvalue == ''){\n"
				+ "        return; \n"
				+ "    }\n"
				+ "    if (selectedvalue == '-1'){\n"
				+ "        return; \n"
				+ "    }\n"
				+ "    document.getElementById(\"" + APVendor.Paramsdefaultdistributioncode + "\").value = \"" + "-1" + "\";\n"
				+ "    flagDirty();"
				//+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
			//Flag page dirty:
			s += "function flagDirty() {\n"
					+ "    flagRecordChanged();\n"
					+ "}\n"
				;

			s += "function flagRecordChanged() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "}\n"
				;
			s += "</script>\n";
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
	private String createDeleteButton(){
		String s = "";
		s = "<button type=\"button\""
		+ " value=\"" + DELETE_BUTTON_LABEL + "\""
		+ " name=\"" + DELETE_BUTTON_LABEL + "\""
		+ " onClick=\"isdelete();\">"
		+ DELETE_BUTTON_LABEL
		+ "</button>\n";
		
		s += "<INPUT TYPE='CHECKBOX' NAME='" + CONFIRM_DELETE_CHECKBOX 
				+ "' VALUE='" + CONFIRM_DELETE_CHECKBOX + "' > Check to confirm before deleting";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}