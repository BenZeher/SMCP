package smcontrolpanel;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposalphrasegroups;
import SMDataDefinition.SMTableproposalphrases;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTableproposalterms;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsTextEditorFunctions;

public class SMProposalEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//Colors:
	private static final String BODYDESCRIPTION_TABLE_BG_COLOR = "#CCFFB2";
	private static final String ORDERCOMMANDS_TABLE_BG_COLOR = "#99CCFF";
	private static final String ALTERNATES_TABLE_BG_COLOR = "#FFBCA2";
	private static final String SIGNATUREBLOCK_TABLE_BG_COLOR = "#99CCFF";
	private static final String PRICEBLOCK_TABLE_BG_COLOR = "#99CCFF";
	private static final String TERMS_TABLE_BG_COLOR = "#CCFFB2";
	private static final String CONVENIENCEPHRASES_BG_COLOR = "#FFBCA2";
	public static final String CONVENIENCEPHRASECONTROL_MARKER = "CPM";
	
	//Commands:
	public static final String HEADER_BUTTON_LABEL = "Go to <B><FONT COLOR=RED>H</FONT></B>eader"; //H
	public static final String HEADERCOMMAND_VALUE = "HEADER";
	public static final String DETAIL_BUTTON_LABEL = "Go to <B><FONT COLOR=RED>D</FONT></B>etails"; //D
	public static final String DETAILCOMMAND_VALUE = "DETAIL";
	public static final String SAVE_BUTTON_LABEL = "<B><FONT COLOR=RED>S</FONT></B>ave"; //S
	public static final String SAVECOMMAND_VALUE = "SAVE";
	public static final String PRINT_BUTTON_LABEL = "<B><FONT COLOR=RED>P</FONT></B>rint"; //P
	public static final String PRINTCOMMAND_VALUE = "PRINT";
	public static final String DELETE_BUTTON_LABEL = "D<B><FONT COLOR=RED>e</FONT></B>lete"; //E
	public static final String DELETECOMMAND_VALUE = "DELETE";
	
	public static final String INSERTCPS_GROUP = "INSERTCPSGROUP";
	public static final String PROPOSALGROUP_PREFIX = "PROPOSALGROUP";
	public static final String PROPOSALPHRASESBYGROUPDIV = "PROPOSALPHRASESBYGROUPDIV";
	public static final String INSERTCPSINTODESCRIPTION_LABEL = "Description";
	public static final String INSERTCPSINTOOPTIONS_LABEL = "Options";
	public static final String INSERTCPSINTOEXTRANOTES_LABEL = "Extra notes";
	public static final String INSERTCPSINTODESCRIPTION_ID = "INSERTINTODESCRIPTION_ID";
	public static final String INSERTCPSINTOOPTIONS_ID = "INSERTINTOOPTIONS_ID";
	public static final String INSERTCPSINTOEXTRANOTES_ID = "INSERTINTOEXTRANOTES_ID";
	public static final String CONVENIENCEPHRASE_SELECT_NAME = "CPSELECT";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String PROPOSALDATAWASCHANGED_FLAG = "PROPOSALDATAWASCHANGEDFLAG";
	public static final String PROPOSALDATAWASCHANGED_FLAG_VALUE = "PROPOSALDATAWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String PROPOSALDATE_PARAM = "PROPOSALDATE";
	public static final String APPROVED_CHECKBOX = "APPROVEDCHECKBOX";
	public static final String SIGNATURE_CHECKBOX = "SIGNATURECHECKBOX";
	private static final String FI_PHRASE1 = "FURNISH AND INSTALL:";
	private static final String FI_PHRASE2 = "FURNISH AND DELIVER:";
	private static final String FI_PHRASE3 = "FURNISH MATERIAL ONLY:";
	private static final String FI_PHRASE4 = "FURNISH LABOR TO INSTALL ONLY:";
	private static final String FI_PHRASE5 = "FURNISH NECESSARY MATERIAL AND LABOR TO ACCOMPLISH THE FOLLOWING:";
	private static final String FI_PHRASE6 = "GENERAL SERVICE:";
	public static final String FI_PHRASE_BLANK = "(DO NOT SHOW ANY TITLE PHRASE)";
	
	//private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);

		SMProposal entry = new SMProposal("");
		entry.loadFromHTTPRequest(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMTableproposals.ObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMProposalAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditProposals
		);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditProposals)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		

		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a proposal object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(SMTableproposals.ObjectName) != null){
			entry = (SMProposal) currentSession.getAttribute(SMTableproposals.ObjectName);
			currentSession.removeAttribute(SMTableproposals.ObjectName);

			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			if (!smedit.getAddingNewEntryFlag()){
				try {
					entry.load(getServletContext(), smedit.getsDBID());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCallingClass()
							+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
							+ entry.getstrimmedordernumber()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
				}
			}
		}
		
		smedit.printLowProfileHeaderTable();
		smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		
		//String sWarning = SMUtilities.get_Request_Parameter("Warning", request);
		//if (! sWarning.equalsIgnoreCase("")){
		//	smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		//}
		
		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);

		try{
			smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
				+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n"
			);
			smedit.createEditPage(getEditHTML(smedit, entry, SMTableproposals.ObjectName, smedit.getUserID(), smedit.getFullUserName()),"");
		} catch (Exception e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getstrimmedordernumber()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					+ "&Warning=" + clsServletUtilities.URLEncode(sError)
			);
			return;
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMProposal entry, String sObjectName, String sUserID, String sUserFullName) throws Exception{
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(entry.getstrimmedordernumber());
		if (!order.load(getServletContext(), sm.getsDBID(), sUserID, sUserFullName)){
			throw new SQLException("Could not load order number " + entry.getstrimmedordernumber() + " - " + order.getErrorMessages());
		}
		String s = "";
		s += clsTextEditorFunctions.getJavascriptTextEditToolBarFunctions();
		s += sCommandScripts(entry, sm);
		s += sStyleScripts();

		//Record the database name:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sm.getsDBID() + "\""
				+ " id=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\""
				+ ">";
		
		//Store whether or not the record has been changed this includes ANY change, including approval:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\""
			+ ">";
		
		//Store whether or not the actual proposal data has been changed - this doesn't include the approval status or the signature checkbox:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PROPOSALDATAWASCHANGED_FLAG + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PROPOSALDATAWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + PROPOSALDATAWASCHANGED_FLAG + "\""
				+ ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
		+ " id=\"" + COMMAND_FLAG + "\""
		+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMProposal.Paramstrimmedordernumber + "\" VALUE=\"" 
			+ entry.getstrimmedordernumber() + "\">";
		
		//We have to keep track of these dates because when the date picker is invoked, it can change the 
		//value of these fields, but it won't trigger an 'onchange' event and we won't know the user has
		//changed the form:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PROPOSALDATE_PARAM + "\""
			+ " id=\"" + PROPOSALDATE_PARAM + "\""
			+ " VALUE=\"" + entry.getdatproposaldate() + "\"" + ">";
		
		try {
			s += "<INPUT TYPE = HIDDEN NAME=\""+SMProposal.Paramsdbaproposallogo+"\" VALUE=\""+getProposalLogoFromDBA(order,sm)+"\" />";
		}catch(Exception e) {
			throw new Exception (e.getMessage());
		}
		
		//New Row
		s += "<TR>";
		
		//Start the outer table here:
		s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";		
		
		//Customer information:
		try {
			s += "<TR><TD>" + createCustomerTable(sm, entry, order) + "</TD></TR>";
		} catch (Exception e) {
			throw new Exception (e.getMessage());
		}
		
		//Create the order commands line at the top:
		s += "<TR><TD>" + createCommandsTable() + "</TD></TR>";
		
		//Create the body description area table:
		s += "<TR><TD>" + createBodyDescriptionTable(sm, entry) + "</TD></TR>";

		//Create the price block table:
		s += "<TR><TD>" + createPriceBlockTable(entry, order, sm) + "</TD></TR>";
		
		//Create the alternates table:
		s += "<TR><TD>" + createAlternatesTable(sm, entry) + "</TD></TR>";
		
		//Create the signature block table:
		s += "<TR><TD>" + createSignatureBlockTable(sm, entry, order) + "</TD></TR>";
		
		//Create the proposal terms table:
		s += "<TR><TD>" + createTermsTable(sm, entry) + "</TD></TR>";

		//Create the signature checkbox table:
		s += "<TR><TD>" + createSignatureCheckboxTable(sm, entry) + "</TD></TR>";

		//Create the approval block table:
		s += "<TR><TD>" + createApprovalBlockTable(sm, entry) + "</TD></TR>";
		
		//Create the order commands line at the bottom:
		s += createCommandsTable();

		//Close the parent table:
		s += "</TR>";
		s += "</TABLE style=\" title:ENDParentTable; \">";
		
		return s;
	}
	
	
	private String getProposalLogoFromDBA(SMOrderHeader order, SMMasterEditEntry smedit) throws Exception{
		
		Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		smedit.getsDBID(),
	    		"MySQL",
	    		this.toString() + ".getProposalLogoFromDBA - User: " 
	    		+ smedit.getUserID()
	    		+ " - "
	    		+ smedit.getFullUserName()
	    			);
	    	if(conn == null){
	    		throw new Exception ("Error instantiating  connection [1546442292]");
	    	}
		 
		String sdbaid = order.getM_idoingbusinessasaddressid();
		String SQL = "SELECT "+SMTabledoingbusinessasaddresses.sProposalLogo+" FROM "+SMTabledoingbusinessasaddresses.TableName+" WHERE lid = "+sdbaid+"";
		String sProposalLogo = "";
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()) {
				sProposalLogo = rs.getString(SMTabledoingbusinessasaddresses.sProposalLogo);
			}
		}catch(Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080630]");
			throw new Exception ("[1546440668]" + e.getMessage());
		}	
		rs.close();
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080631]");
		return sProposalLogo;
	}
	
	private String createCustomerTable(
			SMMasterEditEntry sm, 
			SMProposal entry, 
			SMOrderHeader order
			) throws Exception{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:CustomerTable; \" width=100% >\n";	
		
		s += "<TR>";
		
		s += "<TD class=\" fieldlabel \">Proposal date:&nbsp;</TD>"
			+ "<TD class=\"fieldcontrol\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.ParamdatproposalDate + "\""
			+ " VALUE=\"" + entry.getdatproposaldate().replace("\"", "&quot;") + "\""
			+ " id = \"" + SMProposal.ParamdatproposalDate + "\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "9"
			+ " MAXLENGTH=" + "10"
			//+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
		+ SMUtilities.getDatePickerString(SMProposal.ParamdatproposalDate, getServletContext())
		;
		
		s += "<TD class=\" fieldlabel \">Quote #:&nbsp;</TD>"
				+ "<TD class= \" readonlyfield \">"
				+ order.getM_strimmedordernumber()
				+ "</TD>";
		s += "<TD class=\" fieldlabel \">Submitted to:&nbsp;</TD>"
			+ "<TD class= \" readonlyfield \">"
			+ order.getM_sBillToName()
			+ "</TD>";
		s += "<TD class=\" fieldlabel \">Ship to:&nbsp;</TD>"
				+ "<TD class= \" readonlyfield \">"
				+ order.getM_sShipToName()
				+ "</TD>";
		s += "</TR>";
		//Close the table:
		s += "</TABLE style = \" title:CustomerTable; \">\n";
		return s;
	}
	
	private String createBodyDescriptionTable(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:BodyDescriptionTable; background-color: "
				+ BODYDESCRIPTION_TABLE_BG_COLOR + "; \" width=100% >\n";

		//List of 'F&I' headings:
		s += "<TR>";
		s += "<TD class= \" fieldcontrol \"><U><B>DESCRIPTION&nbsp;</B></U>"
			+ "<SELECT NAME=\"" + SMProposal.Paramsfurnishandinstallstring + "\"" 
			+ " id = \"" + SMProposal.Paramsfurnishandinstallstring + "\""
			+ " onchange=\"flagDirty();\""
			+ ">";
		
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE1) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE1 + "\">" + FI_PHRASE1 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE2) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE2 + "\">" + FI_PHRASE2 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE3) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE3 + "\">" + FI_PHRASE3 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE4) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE4 + "\">" + FI_PHRASE4 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE5) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE5 + "\">" + FI_PHRASE5 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE6) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE6 + "\">" + FI_PHRASE6 + "</OPTION>";
			
			s += "<OPTION";
			if (entry.getsfurnishandinstallstring().compareToIgnoreCase(FI_PHRASE_BLANK) == 0){s += " selected=YES ";}
			s += " VALUE=\"" + FI_PHRASE_BLANK + "\">" + FI_PHRASE_BLANK + "</OPTION>";
			
		s += "</SELECT>";
		s += "</TD></TR>";
 
		s += "<TR><TD>";
		s += clsTextEditorFunctions.Create_Editable_Form_MultilineText_Input_Field(
				SMProposal.Paramsbodydescription,
				entry.getsbodydescription().replace("\"", "&quot;"),
				500,
				1200,
				"flagDirty();",
				false,
				false
				);
		s += "</TD></TR>";
		
		//Convenience phrases:
		s += embedProposalPhrases(sm, entry);
		
		/* - TJR - 9/19/2013 - removed options and extra notes boxes from the screen and from the data:
		//Options:
		s += "<TR><TD ALIGN=LEFT><U><B>OPTIONS</B></U></TD></TR>";
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsoptions + "\""
			+ " rows=\"" + "10" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ "style=\"width:100%\""
			+ " id = \"" + SMProposal.Paramsoptions + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsoptions().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Extra notes:
		s += "<TR><TD ALIGN=LEFT><U><B>EXTRA NOTES</B></U></TD></TR>";
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" >"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsextranotes + "\""
			+ " rows=\"" + "10" + "\""
			+ "style=\"width:100%\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " id = \"" + SMProposal.Paramsextranotes + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsextranotes().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		*/
		//Close the table:
		s += "</TABLE style = \" title:BodyDescriptionTable; \">\n";
		return s;
	}
	private String embedProposalPhrases(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";
		s += "<TR><TD>";
		
		s +=
			"\n<form name=cpform>\n"
			+ "<B>Show proposal phrases<B>\n"
			+ "<input type=\"checkbox\" id=\"cbChoices\" onclick=\"exposeProposalPhraseGroupChoices()\">"
			+ "<div id= \"CPINSERTLABEL\" style=\"display:none;\">"
		;
		//Get all the proposal groups:
		String SQL = "SELECT * FROM " + SMTableproposalphrasegroups.TableName + " ORDER BY " + SMTableproposalphrasegroups.sgroupname;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sm.getsDBID(), 
			"MySQL", 
			this.toString() + ".embedProposalPhrases - user: " + sm.getUserID()
			+ " - "
			+ sm.getFullUserName()
		);
		while (rs.next()){
			String sProposalGroupID = Integer.toString(rs.getInt(SMTableproposalphrasegroups.sid));
			s += "&nbsp;" + rs.getString(SMTableproposalphrasegroups.sgroupname)
			+ "<input type=radio"
			+ " onclick=\"exposeProposalPhraseList(" + sProposalGroupID + ")\""
			+ " value=\"" + PROPOSALGROUP_PREFIX + rs.getString(SMTableproposalphrasegroups.sgroupname) + "\""
			+ " style=\"display:inline;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + PROPOSALGROUP_PREFIX + sProposalGroupID + "\""
			+ ">"
			;
		}
		rs.close();
		/*
			+ "<div id= \"CPINSERTLABEL\" style=\"display:none;\"><B><I>Insert selected phrases into:"
			+ "</I></B>&nbsp;"

			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTODESCRIPTION_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTODESCRIPTION_ID + "\""
			+ ">"
			+ INSERTCPSINTODESCRIPTION_LABEL

			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTOOPTIONS_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTOOPTIONS_ID + "\""
			+ ">"
			+ INSERTCPSINTOOPTIONS_LABEL

			+ "<input type=radio"
			+ " value=\"" + INSERTCPSINTOEXTRANOTES_LABEL + "\""
			+ " style=\"display:none;\""
			+ " name=\"" + INSERTCPS_GROUP + "\""
			+ " id=\"" + INSERTCPSINTOEXTRANOTES_ID + "\""
			+ ">"
			+ INSERTCPSINTOEXTRANOTES_LABEL

			+ "</div>"
			*/
		s += "</div>"
			+ "</form>\n"
		;
			
		//s += "<div id=\"ScrollCB\" style=\"height:200;width:100%;background-color:" 
		//		+ CONVENIENCEPHRASES_BG_COLOR + ";overflow:auto;border:1px solid blue;display:none\">\n"
		//;
		
		SQL = "SELECT * FROM " + SMTableproposalphrases.TableName
			+ " ORDER BY " + SMTableproposalphrases.iphrasegroupid 
			+ ", " + SMTableproposalphrases.isortorder
			+ ", " + SMTableproposalphrases.sproposalphrasename
		;
		ResultSet rscps = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " [1332426094] SQL: " + SQL);
		int iProposalPhraseGroupID = -1;
		int iRecordCounter = 0;
		while (rscps.next()){
			if (iProposalPhraseGroupID != rscps.getInt(SMTableproposalphrases.iphrasegroupid)){
				if (iProposalPhraseGroupID != -1){
					s += "</div>";
				}
				//Need to start a new div:
				iProposalPhraseGroupID = rscps.getInt(SMTableproposalphrases.iphrasegroupid);
				s += "<div id=\"" + PROPOSALPHRASESBYGROUPDIV + Integer.toString(iProposalPhraseGroupID) + "\"" + " style=\"height:200;width:100%;background-color:" 
				+ CONVENIENCEPHRASES_BG_COLOR + ";overflow:auto;border:1px solid blue;display:none\">\n";
			}
			iProposalPhraseGroupID = rscps.getInt(SMTableproposalphrases.iphrasegroupid);
			//This creates a list:
			String sCPText = rscps.getString(SMTableproposalphrases.mproposalphrase).trim().replace("\n", "<BR>");
			//MySQL may also include this newline character: have to drop it now
			sCPText = sCPText.replace("\r", "");
			//Take care of the double quote (this works: " TEST DOUBLE QUOTE: \\\" END TEST")
			sCPText = sCPText.replace("\"", "&quot;");
			//sTerms = sTerms.replace("\"", "QUOTE");
			//Take care of the apostrophe (this works: " TEST APOSTROPHE: \' END TEST")
			sCPText = sCPText.replace("'", "\'");
			//Take care of the backslash (this works: " TEST BACKSLASH: \\\\ END TEST")
			sCPText = sCPText.replace("\\", "\\\\");
			s += "<input type=\"hidden\" id=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableproposalphrases.sid)) 
				+ "\" name=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableproposalphrases.sid)) + "\""
				+ " value=\"" + sCPText 
				+ "\">" 
				+ "<label name=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableproposalphrases.sid)) + "\" for=\"" + CONVENIENCEPHRASECONTROL_MARKER 
				+ Long.toString(rscps.getLong(SMTableproposalphrases.sid)) + "\""
				+ " style=\"font-weight: normal;\""
				+ " onclick=\"insertProposalPhrase('" 
				+ CONVENIENCEPHRASECONTROL_MARKER + Long.toString(rscps.getLong(SMTableproposalphrases.sid)) 
				+ "', this);\""
				+ " onmouseover=colorChangeRed(this) onmouseout=colorChangeBack(this)>"
				+ clsStringFunctions.filter(rscps.getString(SMTableproposalphrases.sproposalphrasename))
				+ "</label>"
				+ "<br>\n"
			;
			iRecordCounter++;
		}
		rscps.close();
		//Put in the last 'div':
		if (iRecordCounter > 0){
			s += "</div>";
		}
		s += "</TD></TR>";

		return s;
		
	}
	private String createAlternatesTable(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:AlternatesTable; background-color: "
				+ ALTERNATES_TABLE_BG_COLOR + "; \" width=100% >\n";

		//Alternate 1:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \" style=\"vertical-align:text-top;\"><B>Alternate&nbsp;1:</B></TD>"
			+ "<TD class=\" fieldcontrol \" style=\"width:80%\">"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsalternate1 + "\""
			+ " rows=\"" + "2" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style=\"width:100%;\""
			+ " id = \"" + SMProposal.Paramsalternate1 + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsalternate1().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "<TD class=\" fieldlabel \" ><DIV style=\"vertical-align: bottom;\"><B>Price:</B></DIV></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsalternate1price + "\""
				+  "style=\"vertical-align:text-bottom;\""
				+ " id = \"" + SMProposal.Paramsalternate1price + "\""
				+ " VALUE=\"" + entry.getsalternate1price().replace("\"", "&quot;") + "\""
				+ " onchange=\"flagDirty();\""
				//+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposals.salternate1priceLength)
				+ ">"
			;
			s += "</TD>";
		s += "</TR>";
		
		//Alternate 2:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \" style=\"vertical-align:text-top;\"><B>Alternate&nbsp;2:</B></TD>"
			+ "<TD class=\" fieldcontrol \" style=\"width:80%\">"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsalternate2 + "\""
			+ " rows=\"" + "2" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style=\"width:100%;\""
			+ " id = \"" + SMProposal.Paramsalternate2 + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsalternate2().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "<TD class=\" fieldlabel \" ><DIV style=\"vertical-align: bottom;\"><B>Price:</B></DIV></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsalternate2price + "\""
				+  "style=\"vertical-align:text-bottom;\""
				+ " id = \"" + SMProposal.Paramsalternate2price + "\""
				+ " VALUE=\"" + entry.getsalternate2price().replace("\"", "&quot;") + "\""
				+ " onchange=\"flagDirty();\""
				//+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposals.salternate2priceLength)
				+ ">"
			;
			s += "</TD>";
		s += "</TR>";
		
		//Alternate 3:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \" style=\"vertical-align:text-top;\"><B>Alternate&nbsp;3:</B></TD>"
			+ "<TD class=\" fieldcontrol \" style=\"width:80%\">"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsalternate3 + "\""
			+ " rows=\"" + "2" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style=\"width:100%;\""
			+ " id = \"" + SMProposal.Paramsalternate3 + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsalternate3().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "<TD class=\" fieldlabel \" ><DIV style=\"vertical-align: bottom;\"><B>Price:</B></DIV></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsalternate3price + "\""
				+  "style=\"vertical-align:text-bottom;\""
				+ " id = \"" + SMProposal.Paramsalternate3price + "\""
				+ " VALUE=\"" + entry.getsalternate3price().replace("\"", "&quot;") + "\""
				+ " onchange=\"flagDirty();\""
				//+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposals.salternate3priceLength)
				+ ">"
			;
			s += "</TD>";
		s += "</TR>";
		
		//Alternate 4:
		s += "<TR>";
		s += "<TD class=\" fieldlabel \" style=\"vertical-align:text-top;\"><B>Alternate&nbsp;4:</B></TD>"
			+ "<TD class=\" fieldcontrol \" style=\"width:80%\">"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsalternate4 + "\""
			+ " rows=\"" + "2" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style=\"width:100%;\""
			+ " id = \"" + SMProposal.Paramsalternate4 + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsalternate4().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "<TD class=\" fieldlabel \" ><DIV style=\"vertical-align: bottom;\"><B>Price:</B></DIV></TD>"
			+ "<TD class=\" fieldcontrol \" >"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsalternate4price + "\""
				+  "style=\"vertical-align: text-bottom;\""
				+ " id = \"" + SMProposal.Paramsalternate4price + "\""
				+ " VALUE=\"" + entry.getsalternate4price().replace("\"", "&quot;") + "\""
				+ " onchange=\"flagDirty();\""
				//+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposals.salternate4priceLength)
				+ ">"
			;
			s += "</TD>";
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:AlternatesTable; \">\n";
		return s;
	}
	
	private String createPriceBlockTable(SMProposal entry, SMOrderHeader order, SMMasterEditEntry sm) throws SQLException{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:PriceBlockTable; background-color: "
			+ PRICEBLOCK_TABLE_BG_COLOR + "; \" width=100% >\n";

		s += "<TR><TD COLSPAN=2>";
		s += "<B>We hereby propose to complete in accordance with above specification, for the sum of:</B>&nbsp;<FONT COLOR=RED>"
			+ "(NOTE: leave the following fields blank if you wish to display the price elsewhere.)</FONT>"	
			;
		s += "</TD></TR>";
		//Written amount:
		s += "<TR>";
		
		//IF we are creating this proposal for the first time, then place the price into the price field:
		String SQL = "SELECT"
				+ " " + SMTableproposals.strimmedordernumber
				+ " FROM " + SMTableproposals.TableName 
				+ " WHERE ("
					+ SMTableproposals.strimmedordernumber + " = '" + order.getM_strimmedordernumber() + "'"
				+ ")"
		;
		String sPrice = entry.getsnumericproposalamt();
		ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
				);
		if (!rs.next()){
			//Here we calculate the total proposal amount:
			BigDecimal bdShippedValue = new BigDecimal(order.getM_dTotalAmountItems().replace(",", ""));
			BigDecimal bdDiscountedAmount = new BigDecimal(order.getM_dPrePostingInvoiceDiscountAmount().replace(",",""));
			BigDecimal bdTaxAmount;
			try {
				bdTaxAmount = new BigDecimal(order.getTaxAmount(sm.getsDBID(), sm.getUserName(), getServletContext()));
			} catch (Exception e) {
				throw new SQLException("Error getting tax amount to calculate proposal total.");
			}
			sPrice = "$" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdShippedValue.subtract(bdDiscountedAmount).add(bdTaxAmount));
		}
		rs.close();
		//Numeric price:
		s += "<TD class=\" fieldrightaligned \">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsnumericproposalamt + "\""
			+ " id = \"" + SMProposal.Paramsnumericproposalamt + "\""
			+ " VALUE=\"" + sPrice.replace("\"", "&quot;") + "\""
			+ " style=\"width:100%;\""
			+ " onchange=\"flagDirty();\""
			+ " onblur=\"displaywrittenamount();\""
			//+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableproposals.snumericproposalamtLength)
			+ ">"
			+ "</TD>"
		;
		
		s += "<TD class=\" fieldcontrol \" style=\"width:80%\">"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramswrittenproposalamt + "\""
			+ " id = \"" + SMProposal.Paramswrittenproposalamt + "\""
			+ " VALUE=\"" + entry.getswrittenproposalamt().replace("\"", "&quot;") + "\""
			+ " style=\"width:100%;\""
			+ " onchange=\"flagDirty();\""
			//+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableproposals.swrittenproposalamtLength)
			+ ">"
			+ "</TD>"
		;
		s += "</TR>";

		//Close the table:
		s += "</TABLE style = \" title:PriceBlockTable; \">\n";
		return s;
	}
	private String createSignatureBlockTable(
			SMMasterEditEntry sm, 
			SMProposal entry,
			SMOrderHeader order) throws SQLException{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:SignatureBlockTable; background-color: "
				+ SIGNATUREBLOCK_TABLE_BG_COLOR + "; \" width=100% >\n";

		String sSalesPersonString = "";
		String SQL = "SELECT"
			+ " " + SMTablesalesperson.sDirectDial
			+ ", " + SMTablesalesperson.sSalespersonEmail
			+ ", " + SMTablesalesperson.sSalespersonFirstName
			+ ", " + SMTablesalesperson.sSalespersonLastName
			+ ", " + SMTablesalesperson.sSalespersonTitle
			+ " FROM " + SMTablesalesperson.TableName
			+ " WHERE ("
				+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + order.getM_sSalesperson() + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createSignatureBlockTable - user: " 
				+ sm.getUserID()
				+ " -  "
				+ sm.getFullUserName()
			);
			if (rs.next()){
				sSalesPersonString = 
					rs.getString(SMTablesalesperson.sSalespersonFirstName)
					+ " "
					+ rs.getString(SMTablesalesperson.sSalespersonLastName)
				;
				if (rs.getString(SMTablesalesperson.sSalespersonTitle).compareToIgnoreCase("") != 0){
					sSalesPersonString += ", " + rs.getString(SMTablesalesperson.sSalespersonTitle);
				}
				if (rs.getString(SMTablesalesperson.sSalespersonEmail).compareToIgnoreCase("") != 0){
					sSalesPersonString += " EMAIL: " + rs.getString(SMTablesalesperson.sSalespersonEmail);
				}
				if (rs.getString(SMTablesalesperson.sDirectDial).compareToIgnoreCase("") != 0){
					sSalesPersonString += " DIRECT DIAL: " + rs.getString(SMTablesalesperson.sDirectDial);
				}
			}else{
				sSalesPersonString = "*** NOTE: SALESPERSON NOT FOUND ***";
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error opening salesperson table - " + e.getMessage());
		}
		
		//Signature:
		//Add some space above the signature:
		s += "<TR>";
		s += "<TD COLSPAN=2 class=\"fieldrightaligned\">"
			+ sSalesPersonString
			+ "</TD>";
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:SignatureBlockTable; \">\n";
		return s;
	}
	private String createTermsTable(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";

		s += "<TABLE class = \" innermost \" style=\" title:ProposalTermsTable; background-color: "
			+ TERMS_TABLE_BG_COLOR + "; \" width=100% >\n";

		ArrayList <String>arrProposalTermIDs = new ArrayList<String>(0);
		ArrayList <String>arrProposalTermCodes = new ArrayList<String>(0);
		String SQL = "SELECT * FROM " + SMTableproposalterms.TableName + " ORDER BY " + SMTableproposalterms.sProposalTermCode;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createTermsTable - user: " + sm.getUserID()
				+ " - " 
				+ sm.getFullUserName());
			while (rs.next()){
				arrProposalTermIDs.add(Integer.toString(rs.getInt(SMTableproposalterms.sID)));
				arrProposalTermCodes.add(rs.getString(SMTableproposalterms.sProposalTermCode));
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error getting terms codes with SQL: " + SQL + " - " + e.getMessage());
		}
		s += "<TR><TD>";
		s += "<B><U>TERMS AND CONDITIONS:</U></B>&nbsp;";
		
		s += "<SELECT NAME = \"" 
			+ SMProposal.Paramitermsid + "\""
			+ " onchange=\"termsChange(this);\""
			+ " >";
		s += "<OPTION"
			+ " VALUE=\"" + "" + "\">" 
			+ "** SELECT TERMS **";
		
		for (int i = 0; i < arrProposalTermIDs.size(); i++){
			s += "<OPTION";
			if (arrProposalTermIDs.get(i).toString().compareTo(entry.getitermsid()) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrProposalTermIDs.get(i).toString() + "\">" + arrProposalTermCodes.get(i).toString();
		}
		s += "</SELECT>";
				
		//Payment terms
		s += "&nbsp;<B>Payment terms:&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramspaymentterms + "\""
			+ " id = \"" + SMProposal.Paramspaymentterms + "\""
			+ " VALUE=\"" + entry.getspaymentterms().replace("\"", "&quot;") + "\""
			//+ " style=\"width:100%;\""
			+ " onchange=\"flagDirty();\""
			+ " SIZE=" + "60"
			+ " MAXLENGTH=" + Integer.toString(SMTableproposals.spaymenttermsLength)
			+ ">"
		;
		
		//Days to accept
		s += "&nbsp;<B>Days to accept:&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMProposal.Paramsdaystoaccept + "\""
			+ " id = \"" + SMProposal.Paramsdaystoaccept + "\""
			+ " VALUE=\"" + entry.getsdaystoaccept().replace("\"", "&quot;") + "\""
			//+ " style=\"width:100%;\""
			+ " onchange=\"flagDirty();\""
			//+ " SIZE=" + "35"
			+ " MAXLENGTH=" + Integer.toString(SMTableproposals.sdaystoacceptLength)
			+ ">"
		;
		
		s += "</TD></TR>";
		
		//Proposal terms:
		s += "<TR>";
		s += "<TD class=\" fieldcontrol \" style=\"width:100%\">"
			+ "<TEXTAREA NAME=\"" + SMProposal.Paramsterms + "\""
			+ " rows=\"" + "6" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style=\"width:100%;\""
			+ " id = \"" + SMProposal.Paramsterms + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ entry.getsterms().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
		;
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:ProposalTermsTable; \">\n";
		return s;
	}
	private String createApprovalBlockTable(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:ApprovalBlockTable; \" width=100% >\n";	
		
		s += "<TR>";
		String sCheckBoxDisabled = " disabled ";
		boolean bAllowProposalApproval = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMApproveProposals, 
			sm.getUserID(), 
			getServletContext(), 
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		if (bAllowProposalApproval){
			sCheckBoxDisabled = "";
		}
			//Proposal is approved, and the user is allowed to approve/disapprove it:
			String sCheckBoxChecked = "";
			if (entry.isproposalapproved()){
				sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}
			s += "<TD class=\" fieldcontrol \">"
				+ " Proposal approved?&nbsp;"
				+ "<INPUT TYPE=CHECKBOX "
				+ sCheckBoxChecked
				+ " NAME=\"" + APPROVED_CHECKBOX + "\""
				+ " id = \"" + APPROVED_CHECKBOX + "\""
				+ sCheckBoxDisabled
				+ " onchange=\"flagRecordChanged();\""
				+ " width=0.25>"
				;
			if (entry.isproposalapproved()){
				s += "&nbsp;NOTE: Proposal was last approved on " + entry.getdattimeapproved()
					+ " by " + entry.getsapprovedbyfullname() + "."
				;
			}
			s += "</TD>";
		s += "</TR>";
		//Close the table:
		s += "</TABLE style = \" title:ApprovalBlockTable; \">\n";
		return s;
	}
	private String createSignatureCheckboxTable(
			SMMasterEditEntry sm, 
			SMProposal entry) throws SQLException{
		String s = "";
		s += "<TABLE class = \" innermost \" style=\" title:SignaturecheckboxTable; \" width=100% >\n";	
		
		s += "<TR>";
		String sCheckBoxDisabled = " disabled ";
		//If this is the user who is also the salesperson on this proposal, then give them the option to print their signature:
		String sOrderSalesperson = "";
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.sSalesperson
			//+ ", " + SMTableusers.TableName + "." + SMTableusers.sDefaultSalespersonCode
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ SMTableorderheaders.strimmedordernumber + " = '" + entry.getstrimmedordernumber() + "'"
			+ ")"
		;
		try {
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(
				SQL,
				getServletContext(),
				sm.getsDBID(),
				"MySQL",
				this.toString() + ".createsignatureTable - order query - user: " + sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
			);
			if (rsOrder.next()){
				sOrderSalesperson = rsOrder.getString(SMTableorderheaders.sSalesperson);
			}
			rsOrder.close();
		} catch (Exception e) {
			throw new SQLException("Error reading salesperson for this order - " + e.getMessage());
		}
		
		String sUserSalesperson = "";
		SQL = "SELECT"
				+ " " + SMTableusers.sDefaultSalespersonCode
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ SMTableusers.lid + " = " + sm.getUserID() + ""
				+ ")"
		;
		try {
			ResultSet rsUsers = clsDatabaseFunctions.openResultSet(
				SQL,
				getServletContext(),
				sm.getsDBID(),
				"MySQL",
				this.toString() + ".createCommonTable - user query - userID: " + sm.getUserID()
			);
			if (rsUsers.next()){
				sUserSalesperson = rsUsers.getString(SMTableusers.sDefaultSalespersonCode);
			}
			rsUsers.close();
		} catch (Exception e) {
			throw new SQLException("Error reading salesperson for this user - " + e.getMessage());
		}
		boolean bAllowSignature = false;
		//If there's an actual salesperson on the order
		if (sOrderSalesperson.compareToIgnoreCase("") != 0){
			//If this user is that salesperson, then they can print a signature
			if ((sUserSalesperson.compareToIgnoreCase("") != 0) && (sUserSalesperson.compareToIgnoreCase(sOrderSalesperson) == 0)){
				bAllowSignature = true;
			}
			//Or of this user has permission to print ANY signature, he can do it
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMSignAnySalespersonsProposals, 
					sm.getUserID(), 
					getServletContext(), 
					sm.getsDBID(),
					(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				bAllowSignature = true;
			}
		}
		
		if (bAllowSignature){
			sCheckBoxDisabled = "";
		}
		//Proposal is signed, and the user is allowed to sign it:
		String sCheckBoxChecked = "";
		if (entry.getisigned().compareToIgnoreCase("1") == 0){
			sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += "<TD class=\" fieldcontrol \">"
			+ " Sign proposal?&nbsp;"
			+ "<INPUT TYPE=CHECKBOX "
			+ sCheckBoxChecked
			+ " NAME=\"" + SIGNATURE_CHECKBOX + "\""
			+ " id = \"" + SIGNATURE_CHECKBOX + "\""
			+ sCheckBoxDisabled
			+ " onchange=\"flagRecordChanged();\""
			+ " width=0.25>"
			;
		if (entry.getisigned().compareToIgnoreCase("1") == 0){
			s += "&nbsp;NOTE: Proposal was last signed on " + entry.getdattimesigned()
				+ " by " +  entry.getssignedbyfullname() + "."
			;
		}
		s += "</TD>";
		s += "</TR>";
		//Close the table:
		s += "</TABLE style = \" title:SignatureCheckboxTable; \">\n";
		return s;
	}
	private String createCommandsTable(){
		String s = "";
		
		//Create the table:
		s += "<TABLE class = \" innermost \" style=\" title:OrderCommands; background-color: "
			+ ORDERCOMMANDS_TABLE_BG_COLOR + "; \" width=100% >\n";
		//Place the 'update' button here:
		s += "<TR><TD style = \"text-align: left; \" >"
			+ "<button type=\"button\""
			+ " value=\"" + HEADER_BUTTON_LABEL + "\""
			+ " name=\"" + HEADER_BUTTON_LABEL + "\""
			+ " onClick=\"gotoheader();\">"
			+ HEADER_BUTTON_LABEL
			+ "</button>\n"
			
			+ "<button type=\"button\""
			+ " value=\"" + DETAIL_BUTTON_LABEL + "\""
			+ " name=\"" + DETAIL_BUTTON_LABEL + "\""
			+ " onClick=\"gotodetails();\">"
			+ DETAIL_BUTTON_LABEL
			+ "</button>\n"
						
			//Save:
			+ "<button type=\"button\""
			+ " value=\"" + SAVE_BUTTON_LABEL + "\""
			+ " name=\"" + SAVE_BUTTON_LABEL + "\""
			+ " onClick=\"save();\">"
			+ SAVE_BUTTON_LABEL
			+ "</button>\n"
	
			//Delete:
			+ "<button type=\"button\""
			+ " value=\"" + DELETE_BUTTON_LABEL + "\""
			+ " name=\"" + DELETE_BUTTON_LABEL + "\""
			+ " onClick=\"deleteproposal();\">"
			+ DELETE_BUTTON_LABEL
			+ "</button>\n"

			//Print
			+ "<button type=\"button\""
			+ " value=\"" + PRINT_BUTTON_LABEL + "\""
			+ " name=\"" + PRINT_BUTTON_LABEL + "\""
			+ " onClick=\"printproposal();\">"
			+ PRINT_BUTTON_LABEL
			+ "</button>\n"
			;
		s += "</TABLE style=\" title:ENDOrderCommands; \">\n";
		return s;
	}
	private String sCommandScripts(SMProposal proposal, SMMasterEditEntry smmaster) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";

		s += "function initShortcuts() {\n";

		//TEST:
		s += "    shortcut.add(\"Alt+c\",function() {\n";
		s += "        displaywrittenamount();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+d\",function() {\n";
		s += "        gotodetails();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+e\",function() {\n";
		s += "        deleteproposal();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+h\",function() {\n";
		s += "        gotoheader();\n";
		s += "    },{\n";
		s += "        'type':'keydown',\n";
		s += "        'propagate':false,\n";
		s += "        'target':document\n";
		s += "    });\n";
		
		s += "    shortcut.add(\"Alt+p\",function() {\n";
		s += "        printproposal();\n";
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

		/*
		s += "window.onload = function(){\n"
			//+ "    document.forms.MAINFORM." + SMOrderDetail.ParamsItemDesc + ".focus();\n"
			+ "    initShortcuts();\n"
			//+ "    fg_hideform('fg_formContainer','fg_backgroundpopup');\n"
			+ "}\n\n"
		;
		*/
		//This function should make the 'Alternate 1' box autoexpand:
		s += "window.onload = function() {\n"
			+ "\n"
			+ "    initShortcuts();\n"
			+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = '';\n"
			+ "    document.getElementById(\"" + PROPOSALDATAWASCHANGED_FLAG + "\").value = '';\n"
			+ "    displaywrittenamount();\n"
			+ "\n"
	        //+ "    var " + SMProposal.Paramsbodydescription + "t = document.getElementById('" + SMProposal.Paramsbodydescription + "');\n"
	        //+ "    var " + SMProposal.Paramsbodydescription + "offset= !window.opera ? (" + SMProposal.Paramsbodydescription + "t.offsetHeight - " + SMProposal.Paramsbodydescription + "t.clientHeight) : (" + SMProposal.Paramsbodydescription + "t.offsetHeight + parseInt(window.getComputedStyle(" + SMProposal.Paramsbodydescription + "t, null).getPropertyValue('border-top-width')));\n"
	        //+ "    var resize  = function(t, offset) {\n"
	        //+ "        alert('resizing with offset: ' + offset);\n"
	        //+ "        t.style.height = 'auto';\n"
	        //+ "        t.style.height = (t.scrollHeight  + offset ) + 'px';\n"
	        //+ "    }\n"
	        //+ "    " + SMProposal.Paramsbodydescription + "t.addEventListener && " + SMProposal.Paramsbodydescription + "t.addEventListener('input', function(event) {\n"
	        //+ "        resize(" + SMProposal.Paramsbodydescription + "t, " + SMProposal.Paramsbodydescription + "offset);\n"
	        //+ "    });\n"
	        //+ "    " + SMProposal.Paramsbodydescription + "t['attachEvent']  && t.attachEvent('onkeyup', function() {\n"
	        //+ "        resize(" + SMProposal.Paramsbodydescription + "t, " + SMProposal.Paramsbodydescription + "offset);\n"
	        //+ "    " + SMProposal.Paramsbodydescription + "t['attachEvent']  && t.attachEvent('onblur', function() {\n"
	        //+ "        resize(" + SMProposal.Paramsbodydescription + "t, " + SMProposal.Paramsbodydescription + "offset);\n"
	        //+ "    });\n"
			+ "}\n"
		;

		s += sLoadTermsScript(smmaster.getsDBID(), smmaster.getUserID(), smmaster.getFullUserName());
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";

		s += "function promptToSave(){\n"
			//Make sure to update the 'written amount' first:
			+ "    displaywrittenamount();\n"
			//Check to see if the date fields were changed, and if so, flag the record was changed field:
			+ "    if (document.getElementById(\"" + PROPOSALDATE_PARAM + "\").value != " 
				+ "document.getElementById(\"" + SMProposal.ParamdatproposalDate + "\").value){\n"
			+ "        flagDirty();\n"
			+ "    }\n"			
			
			+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" 
				+ SAVECOMMAND_VALUE + "\" ){\n"
			+ "        return 'You have unsaved changes - are you sure you want to leave this proposal?';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
		//Delete proposal
		s += "function deleteproposal(){\n"
				+ "    if (confirm(\"Are you sure you want to delete this proposal?\")){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETECOMMAND_VALUE + "\";\n"
				+ "        document.forms[\"MAINFORM\"].submit();\n"
				+ "    }\n"
				+ "}\n"
			;
		
		//Go to header
		s += "function gotoheader(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + HEADERCOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		//Go to details
		s += "function gotodetails(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + DETAILCOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;

		//Print
		s += "function printproposal(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + PRINTCOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		//Save
		s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + SAVECOMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		
		//Flag proposal dirty:
		s += "function flagDirty() {\n"
				+ "    flagRecordChanged();\n"
				+ "    flagProposalDataChanged();\n"
				+ "    document.getElementById(\"" + APPROVED_CHECKBOX + "\").checked = false;\n"
				+ "    document.getElementById(\"" + SIGNATURE_CHECKBOX + "\").checked = false;\n"
				+ "}\n"
			;
		s += "function flagRecordChanged() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n"
			;
		s += "function flagProposalDataChanged() {\n"
				+ "    document.getElementById(\"" + PROPOSALDATAWASCHANGED_FLAG + "\").value = \"" 
				 + PROPOSALDATAWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n"
			;
		
		s += "function displaywrittenamount(){\n"
			+ "    var numericprice = document.getElementById(\"" + SMProposal.Paramsnumericproposalamt + "\").value;\n"
			+ "    numericprice = numericprice.trim().replace('$', '');\n"
			+ "    if (numericprice != ''){;\n"
			+ "        document.getElementById(\"" + SMProposal.Paramswrittenproposalamt + "\").value = " 
			+ "numbertowritten(numericprice);\n"
			+ "    }\n"
			+ "}\n"
		;
		
		s += "function colorChangeRed(targetLabel){\n"
			+ "    targetLabel.style.backgroundColor=\"RED\";\n"
			+ "}\n"
		;
		s += "function colorChangeBack(targetLabel){\n"
			+ "    targetLabel.style.backgroundColor=\"" + CONVENIENCEPHRASES_BG_COLOR + "\";\n"
			+ "}\n"
		;
		s += "function exposeProposalPhraseGroupChoices() {\n" 
				+ "    var status = document.getElementById('cbChoices').checked;\n" 
				+ "    if (status == true) {\n"
				+ "        document.getElementById('" + "CPINSERTLABEL" + "').style.display = \"inline\";\n"
				+ "        //Expose the list for any radio button that's checked:\n"
				+ "        for(i=1; i<100; i++){\n"
				+ "            var iproposalgroupid = i.toString();\n"
				+ "            var sgrouplabelradiobuttonid = '" + PROPOSALGROUP_PREFIX + "' + iproposalgroupid;\n"
				+ "            try {\n"
				+ "                var radioboxstatus = document.getElementById(sgrouplabelradiobuttonid).checked;\n" 
				+ "                if (radioboxstatus){;\n"
				+ "                exposeProposalPhraseList(iproposalgroupid);\n"
				+ "                };\n"
				+ "            } catch(err) {\n"
				+ "            //Do nothing here:\n"
				+ "            }\n"
				+ "        }\n"				
				+ "    } else {\n"
				+ "        document.getElementById('" + "CPINSERTLABEL" + "').style.display = \"none\";\n"
				+ "        hideAllProposalPhraseLists();\n"
				+ "    }\n" 
				+ "}\n"
			;
		s += "function exposeProposalPhraseList(sProposalPhraseGroupID) {\n" 
				//+ "    alert ('Proposal Group ID: ' + sProposalPhraseGroupID);\n"
				+ "    hideAllProposalPhraseLists();\n"
				+ "    var status = document.getElementById('cbChoices').checked;\n" 
				+ "    if (status == true) {\n"
				+ "        var sproposalgroupdivid = '" + PROPOSALPHRASESBYGROUPDIV + "' + sProposalPhraseGroupID;\n"
				+ "        document.getElementById(sproposalgroupdivid).style.display = \"block\";\n"
				+ "    }"
				+ "}\n"
			;
		s += "function hideAllProposalPhraseLists() {\n" 
				+ "    for(i=0; i<100; i++){\n"
				+ "        var sproposalgroupdivid = '" + PROPOSALPHRASESBYGROUPDIV + "' + i.toString();\n"
				+ "        try {\n"
				+ "            document.getElementById(sproposalgroupdivid).style.display = 'none';\n" 
				+ "        } catch(err) {\n"
				+ "        //Do nothing here:\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n"
			;
		
			s += "function insertProposalPhrase(sPhraseID, label) {\n" 
				+ "    var phrasestring = document.getElementById(sPhraseID).value + '<div><br/></div>';\n"
				+ "    var iFramedoc = window.frames['" + SMProposal.Paramsbodydescription + "iFrame'].document;\n"
				+ "    iFramedoc.execCommand('insertHTML',false,phrasestring);\n"
				+ "	   iFramedoc.body.focus();"
				+ "    flagDirty();\n"
				+ "}\n"
			;
			
		//Function to convert numbers to written numbers:
		s += "//NUMBER TO BE CONVERTED AND ITS NUMBER OF DIGITS\n"
			+ "function numbertowritten(numericamount){\n"
			+ "    if (numericamount == ''){\n"
			+ "        return 'Zero Dollars and Zero Cents';\n"
			+ "    }\n"
			+ "    var n = numericamount.replace(\",\", \"\");\n"
			+ "    var parts = n.split(\".\");\n"
			+ "    if (parts.length > 2){\n"
			+ "        return 'Invalid number';\n"
			+ "    }\n"
			+ "    var wholepart = parts[0];\n"
			+ "    var decimalpart = parts[1];\n"
			+ "    var l = wholepart.length;\n"
			+ "    var reg = /^[0-9]+$/;\n"
			+ "    var powers = [\"Thousand\", \"Million\", \"Billion\", \"Trillion\", \"Quadrillion\", \"Quintillion\", \"Sextillion\", \"Septilltion\", \"Octillion\", \"Novtillion\", \"Decitillion\"];\n"
			+ "    var ones = [\"One\", \"Two\", \"Three\", \"Four\", \"Five\", \"Six\", \"Seven\", \"Eight\", \"Nine\"];\n"
			+ "    var one = [1, 2, 3, 4, 5, 6, 7, 8, 9];\n"
			+ "    var ten = [\"Eleven\", \"Twelve\", \"Thirteen\", \"Fourteen\", \"Fifteen\", \"Sixteen\", \"Seventeen\", \"Eighteen\", \"Nineteen\"];\n"
			+ "    var tens = [\"Ten\", \"Twenty\", \"Thirty\", \"Forty\", \"Fifty\", \"Sixty\", \"Seventy\", \"Eighty\", \"Ninety\"];\n"
			+ "    var p = Math.floor(l/3);\n"
			+ "    var z = '';\n"
			+ "\n"
			
			+ "//IF NUMBER IS VALID\n"
			+ "    if(wholepart.match(reg)){\n"
			+ "        for(i=0; i<l; i++){\n"
			+ "            var e = (l-i)-1; var f = e+1; var g = (f%3);\n"
			+ "//IF IN THE HUNDREDS\n"
			+ "            if(g==0){\n"
			+ "                if(wholepart[i]>0){\n"
			+ "                    y = wholepart[parseInt(i)];\n"
			+ "                    z += ones[y-1]+' Hundred ';\n"
			+ "                    //ADDS THE POWER OF THOUSAND\n"
			+ "                    if(e>=3){\n"
			+ "                        if((wholepart[i+1]==0)&&(wholepart[i+2]==0)){\n"
			+ "                            z += ' '+powers[Math.floor(e/3)-1]+' ';\n"
			+ "                        }\n"
			+ "                    }\n"
			+ "                }\n"
			+ "             }\n"
			+ "             //IF IN THE TENS\n"
			+ "             if(g==2){\n"
			+ "                 if(wholepart[i]>0){\n"
			+ "                     //CHECKS FOR NUMBERS 11-19\n"
			+ "                     if(wholepart[i]=='1'){\n"
			+ "                         if(wholepart[i+1]=='0'){\n"
			+ "                             z += 'Ten';\n"
			+ "                         } else {\n"
			+ "                             //GET THE NUMBER FROM THE 'ten' ARRAY\n"
			+ "                             y = wholepart[parseInt(i+1)];\n"
			+ "                             z += ten[y-1];\n"
			+ "                         }\n"
			+ "                      } else {\n"
			+ "                          y = wholepart[parseInt(i)];\n"
			+ "                          z += tens[y-1];\n"
			+ "                      }\n"
			+ "                      //ADDS THE POWER OF THOUSAND\n"
			+ "                      if(e>=3){\n"
			+ "                          if(wholepart[i+1]==0){\n"
			+ "                              z += ' '+powers[Math.floor(e/3)-1]+' ';\n"
			+ "                          }\n"
			+ "                      }\n"
			+ "                }\n"
			+ "            }\n"
			+ "            //IF IN THE ONES\n"
			+ "            if(g==1){\n"
			+ "                if(wholepart[i]>0){\n"
			+ "                    y = wholepart[parseInt(i)];\n"
			+ "                    if(wholepart[i-1]>1){\n"
			+ "                        z += '-'+ones[y-1]; //IF TENS PLACE DIGIT > 1, ADDS THE DASH\n"
			+ "                    } else {\n"
			+ "                    if(wholepart[i-1]=='1'){\n"
			+ "                        z += ''; //IF NUMBER IS FROM 11-19, THIS PART IS BLANK (ALREADY SET)\n"
			+ "                    } else {\n"
			+ "                        z += ones[y-1];\n"
			+ "                    }\n"
			+ "                }\n"
			+ "                //ADDS THE POWER OF THOUSAND\n"
			+ "                if(e>=3){\n"
			+ "                    z += ' '+powers[Math.floor(e/3)-1]+' ';\n"
			+ "                }\n"
			+ "            }\n"
			+ "        }\n"
			+ "    }\n"
			+ "} else {\n"
			+ "    z = 'Invalid Number';\n"
			+ "}\n"
			+ "    var dollars = 'Dollars';\n"
			+ "    if (z == 'One'){\n"
			+ "        dollars = 'Dollar';\n"
			+ "    };\n"
			+ "    z = z + ' ' + dollars;"
			+ "\n"
			+ "//Now process the decimal part:\n"
			+ "    if (decimalpart == ''){\n"
			+ "        return z + ' And Zero Cents.';\n"
			+ "    }\n"
			+ "    if (parts.length == 1){\n"
			+ "        return z + ' And Zero Cents.';\n"
			+ "    }\n"
			+ "    if (decimalpart.length == 1){\n"
			+ "        decimalpart = '0' + decimalpart;\n"
			+ "    }\n"
			+ "    if(!decimalpart.match(reg)){\n"
			+ "        return 'Invalid number';\n"
			+ "    }\n"
			+ "    //CHECKS FOR NUMBERS 11-19\n"
			+ "    var d = '';\n"
			+ "    if(decimalpart[0]=='1'){\n"
			+ "        if(decimalpart[1]=='0'){\n"
			+ "            d = 'Ten';\n"
			+ "        } else {\n"
			+ "            //GET THE NUMBER FROM THE 'ten' ARRAY\n"
			+ "            y = decimalpart[parseInt(1)];\n"
			+ "            d = ten[y-1];\n"
			+ "        }\n"
			+ "     } else {\n"
			+ "         y = decimalpart[parseInt(0)];\n"
			+ "         if (y != '0'){\n"
			+ "             d = tens[y-1];\n"
			+ "         }\n"
			+ "         if (decimalpart[1] != '0'){\n"
			+ "             y = decimalpart[parseInt(1)];\n"
			+ "             d = d + ' ' + ones[y - 1];\n"
			+ "         }\n"
			+ "     }\n"
			+ "    if ((decimalpart[0] == '0') && decimalpart[1] == '0'){\n"
			+ "        d = 'Zero';\n"
			+ "    }\n"
			+ " if (z == ''){\n"
			+ "     z = 'Zero Dollars';\n"
			+ " }\n"
			+ "    z = z.trim();"
			+ "    d = d.trim();"
			+ "    var cents = 'Cents';\n"
			+ "    if (d == 'One'){\n"
			+ "        cents = 'Cent';\n"
			+ "    };\n"
			+ " return z + ' And ' + d + ' ' + cents;\n"
			+ "}\n"
		;
		s += "</script>\n";
		return s;
	}
	private String sLoadTermsScript(String sDBID, String sUserID, String sUserFullName) throws SQLException{
		//Here we have to build javascript arrays of the terms fields:
		int iCounter = 0;
		String spaymentterms = "";
		String sdaystoaccept = "";
		String sterms = "";
		String s = "";
		String SQL = "SELECT"
			+ " " + SMTableproposalterms.mProposalTermDesc
			+ ", " + SMTableproposalterms.sdaystoaccept
			+ ", " + SMTableproposalterms.sdefaultpaymentterms
			+ ", " + SMTableproposalterms.sID
			+ " FROM " + SMTableproposalterms.TableName
			+ " ORDER BY " + SMTableproposalterms.sID
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + " sLoadTermsScript - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
			);
			while (rs.next()){
				iCounter++;
				spaymentterms += "spaymentterms[\"" + Integer.toString(rs.getInt(SMTableproposalterms.sID)) 
					+ "\"] = \"" + rs.getString(SMTableproposalterms.sdefaultpaymentterms).trim().replace("\"", "'") + "\";\n";
				sdaystoaccept += "sdaystoaccept[\"" + Integer.toString(rs.getInt(SMTableproposalterms.sID)) 
					+ "\"] = \"" + rs.getString(SMTableproposalterms.sdaystoaccept).trim().replace("\"", "'") + "\";\n";
				//We'll do a lot of filtering here, because this could be long and include a lot of special characters:
				//First, we'll convert any newline characters temporarily:
				String sTerms = rs.getString(SMTableproposalterms.mProposalTermDesc).trim().replace("\n", "<BR>");
				//MySQL may also include this newline character: have to drop it now
				sTerms = sTerms.replace("\r", "");
				//Take care of the double quote (this works: " TEST DOUBLE QUOTE: \\\" END TEST")
				sTerms = sTerms.replace("\"", "&quot;");
				//sTerms = sTerms.replace("\"", "QUOTE");
				//Take care of the apostrophe (this works: " TEST APOSTROPHE: \' END TEST")
				sTerms = sTerms.replace("'", "\'");
				//Take care of the backslash (this works: " TEST BACKSLASH: \\\\ END TEST")
				sTerms = sTerms.replace("\\", "\\\\");
				
				sterms += "sterms[\"" + Integer.toString(rs.getInt(SMTableproposalterms.sID)) 
					+ "\"] = \"" 
					+ sTerms
					+ "\";\n";
				//sterms += "sterms[\"" + Integer.toString(rs.getInt(SMTableproposalterms.sID)) 
				//		+ "\"] = \"" 
				//		+ " TEST APOSTROPHE: \' END TEST"
				//		+ " TEST DOUBLE QUOTE: \\\" END TEST"
				//		+ " TEST BACKSLASH: \\\\ END TEST"
				//		+ " TEST NEWLINE:\\\n\" + \"<BR>\" + \"END TEST"
				//		+ "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading terms for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var spaymentterms = new Array(" + Integer.toString(iCounter) + ")\n";
			s += spaymentterms + "\n";
			
			s += "var sdaystoaccept = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sdaystoaccept + "\n";
			
			s += "var sterms = new Array(" + Integer.toString(iCounter) + ")\n";
			s += sterms + "\n";
		}
		s += "\n";
		s += "function termsChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "    var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "    var which = selectObj.options[idx].value;\n"
		//+ "alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "    if (which != ''){\n"
		//+ "        if (confirm(\"Are you sure you want to update the terms on this proposal?\")){\n"
		+ "            document.forms[\"MAINFORM\"].elements[\"" + SMProposal.Paramsdaystoaccept + "\"].value = sdaystoaccept[which];\n"
		+ "            document.forms[\"MAINFORM\"].elements[\"" + SMProposal.Paramspaymentterms + "\"].value = spaymentterms[which];\n"
		+ "            var termstring = sterms[which].split('<BR>').join('\\n');\n"
		+ "            termstring = termstring.split('&quot;').join('\"');\n"
		+ "            document.forms[\"MAINFORM\"].elements[\"" + SMProposal.Paramsterms + "\"].value = termstring;\n"
		//+ "            document.forms[\"MAINFORM\"].elements[\"" + SMProposal.Paramsterms + "\"].value = sterms[which].replace(\"<BR>\", \"\\n\").replace(\"&quot;\",\"\\\"\");\n"
		+ "            flagDirty();\n"
		//+ "        }\n"
		+ "    }\n"
		+ "}\n\n";
		
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

		//s +=
		//	"table.main th {"
		//	+ "border-width: " + sBorderSize + "px; "
		//	+ "padding: 2px; "
		//	//+ "border-style: inset; "
		//	+ "border-style: none; "
		//	+ "border-color: white; "
		//	+ "background-color: white; "
		//	+ "color: black; "
		//	+ "font-family : Arial; "
		//	+ "vertical-align: text-middle; "
		//	//+ "height: 50px; "
		//	+ "}"
		//	+ "\n"
		//	;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: #FFFFFF; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

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
		
		//This is the def for a label field:
		s +=
			"td.readonlyfield {"
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
		
		//This is the def for an underlined heading on the screen:
		s +=
			"td.fieldheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
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
