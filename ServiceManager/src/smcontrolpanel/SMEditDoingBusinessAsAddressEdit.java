package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMClasses.SMDoingbusinessasaddress;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsTextEditorFunctions;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class SMEditDoingBusinessAsAddressEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String SAVE_BUTTON_LABEL = "Save " + SMDoingbusinessasaddress.ParamObjectName;
	public static final String SAVE_COMMAND_VALUE = "SAVEADDRESS";
	public static final String DELETE_BUTTON_LABEL = "Delete " + SMDoingbusinessasaddress.ParamObjectName;
	public static final String DELETE_COMMAND_VALUE = "DELETEADDRESS";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	private static final String FORM_NAME = "MAINFORM";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		SMDoingbusinessasaddress entry = new SMDoingbusinessasaddress(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditDoingBusinessAsAddressAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditDoingBusinessAsAddresses
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditDoingBusinessAsAddresses)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		//Check to see if an address was selected.
		if(entry.getslid().compareToIgnoreCase("0") == 0 && !smedit.getAddingNewEntryFlag()) {
			//If this is a new record that failed coming back to the edit screen do nothing
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDoingBusinessAsAddressSelection"
						+ "?" + "Warning=" + clsServletUtilities.URLEncode("Select an address to edit or add a new one.")
					);	
				return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a dba entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMDoingbusinessasaddress.ParamObjectName) != null){
	    	entry = (SMDoingbusinessasaddress) currentSession.getAttribute(SMDoingbusinessasaddress.ParamObjectName);
	    	currentSession.removeAttribute(SMDoingbusinessasaddress.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	try {
		    		entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
		    	}catch (Exception e){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDoingBusinessAsAddressEdit"
						+ "?" + SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
						+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
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
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
	    smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
								+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n");
	    
	    smedit.getPWOut().println(sCommandScripts(entry, smedit));
	    
	    createEditPage(getEditHTML(smedit, entry), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit
			);
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditDoingBusinessAsAddressEdit"
				+ "?" + SMDoingbusinessasaddress.Paramlid + "=" + entry.getslid()
				+ "&Warning=" + clsServletUtilities.URLEncode("Could not load entry ID: " + entry.getslid() + " - " + sError)
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
			sFormString += " class=" + sFormClassName + " >";
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
		pwOut.println("<BR>" + createSaveButton() + "&nbsp;" +createDeleteButton());
		pwOut.println("</FORM>");
	}

	
	private String getEditHTML(SMMasterEditEntry sm, SMDoingbusinessasaddress entry) throws SQLException{
		String s = "";
		s +=	clsTextEditorFunctions.getJavascriptTextEditToolBarFunctions();
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\""
				+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">";
		
		s += "<TABLE BORDER=1>";
		
		//Check if this is a new record
		String sID = "";
		if (sm.getAddingNewEntryFlag()){
			sID = "NEW";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMDoingbusinessasaddress.ParamNewRecord + "\" VALUE=\"" + "1" + "\">";
		}else{		
			sID =  entry.getslid();
		}
		//ID
		s += "<TR><TD ALIGN=RIGHT><B>ID</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + sID + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMDoingbusinessasaddress.Paramlid + "\" VALUE=\"" 
					+ entry.getslid() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		
		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMDoingbusinessasaddress.Paramsdescription,
				entry.getsdescription().replace("\"", "&quot;"), 
				SMTabledoingbusinessasaddresses.sDescriptionLength, 
				"<B>Description:</B>",
				"Short description that will appear alphabetically in drop down menus when selecting DBA ",
				"40",
				"flagDirty();"
		);
		
		//Address 
		s += clsTextEditorFunctions.Create_Edit_Form_Editable_MultilineText_Input_Row_with_Style(
				SMDoingbusinessasaddress.Parammaddress,
				entry.getmaddress(),
				"Address:",
				"Address that will appear on Invoices, Proposals, Work Orders, and Delivery Tickets. "
				+ " This address text will automatically align on documents. Centered for invoices, top left justified for all other documents.",
				200,
				500,
				"flagDirty();",
				true,
				false,
				"STYLE=\"vertical-align:top;\""
				);


		//Remit To Address 
		s += clsTextEditorFunctions.Create_Edit_Form_Editable_MultilineText_Input_Row_with_Style(
				SMDoingbusinessasaddress.Parammremittoaddress,
				entry.getmremittoaddress(),
				"Remit To Address:",
				"Remit to address will appear ONLY on Invoices.",
				200,
				500,
				"flagDirty();",
				true,
				false,
				"STYLE=\"vertical-align:top;\""
				);
		
		//Logo
		ArrayList<String> arrValues = new ArrayList<String>(0);
        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
        arrValues.add("YES");
        arrDescriptions.add("YES");
        arrValues.add("NO");
        arrDescriptions.add("NO");
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row_Adjustable_Width(
				SMDoingbusinessasaddress.Paramslogo, 
        		arrValues, 
        		entry.getslogo().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Show Invoice Logo:", 
        		"Show Company Logo on the Invoice",
        		"",
        		250
        		);
		s += createInputFile(entry)
		   + "</TABLE>";
		return s;
	}
	
	private String createInputFile(SMDoingbusinessasaddress entry) {
		String s = "";
		 s += "<TR id = \"ilogofiles\">"
			  + "<TD ALIGN=RIGHT><B>Invoice Logo File:</B></TD>"
			  + "<TD ALIGN=LEFT>"
			  + " <INPUT TYPE= TEXT NAME=\"" + SMDoingbusinessasaddress.ParamiInvoicelogo + "\" ID=\"" + SMDoingbusinessasaddress.ParamiInvoicelogo + "\""
			  		+ " SIZE = 40  VALUE = \""+entry.getsInvoiceLogo()+"\">"
			  + "</TD>"
			  + "<TD>Image logo used for the Invoice. Example: <b>invoicelogo.png</b> </TD>"
			  + "</TR>"
			  + "<TR id = \"plogofiles\">"
			  + "<TD ALIGN=RIGHT><B>Proposal Logo File:</B></TD>"
			  + "<TD ALIGN=LEFT>"
			  + " <INPUT TYPE= TEXT NAME=\"" + SMDoingbusinessasaddress.ParamiProposallogo + "\" ID=\"" + SMDoingbusinessasaddress.ParamiProposallogo + "\""
			  		+ " SIZE = 40  VALUE = \""+entry.getsProposalLogo()+"\">"
			  + "</TD>"
			  + "<TD>Image logo used for the proposal. Example: <b>proposallogo.png</b> </TD>"
			  + "</TR>"
			  + "<TR id = \"dlogofiles\">"
			  + "<TD ALIGN=RIGHT><B>Delivery Ticket Logo File:</B></TD>"
			  + "<TD ALIGN=LEFT>"
			  + " <INPUT TYPE= TEXT NAME=\"" + SMDoingbusinessasaddress.ParamiDeliveryTicketReceiptLogo + "\" ID=\"" + SMDoingbusinessasaddress.ParamiDeliveryTicketReceiptLogo + "\""
			  		+ " SIZE = 40  VALUE = \""+entry.getsDeliveryTicketReceiptLogo()+"\">"
			  + "</TD>"
			  + "<TD>Image logo used for the delivery ticket receipt.  Example: <b>deliveryticketlogo.png</b> </TD>"
			  + "</TR>"
			  + "<TR id = \"wlogofiles\">"
			  + "<TD ALIGN=RIGHT><B>Work Order Receipt Logo File:</B></TD>"
			  + "<TD ALIGN=LEFT>"
			  + " <INPUT TYPE= TEXT NAME=\"" + SMDoingbusinessasaddress.ParamiWorkOrderReceiptLogo + "\" ID=\"" + SMDoingbusinessasaddress.ParamiWorkOrderReceiptLogo + "\""
			  		+ " SIZE = 40  VALUE = \""+entry.getsWorkOrderReceiptLogo()+"\">"
			  + "</TD>"
			  + "<TD>Image logo used for the work order receipt. Example: <b>workorderlogo.png</b></TD>"
			  + "</TR>"
		 	  + "</div>";
		return s;
	}
	
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\" save();\">"
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

	
	private String sCommandScripts(
			SMDoingbusinessasaddress vendor, 
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
			
			//Make sure that file input is shown when apporopiate
			
			s += "window.onload = showLogoImport;\n";
					
			//Prompt to save:
			s += "window.onbeforeunload = promptToSave;\n";		
			
			//Chck for changes before saving 
			s += "function promptToSave(){\n"		
				
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + SAVE_COMMAND_VALUE + "\""
						+ " && document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + DELETE_COMMAND_VALUE + "\"){\n"
				+ "        return 'You have unsaved changes!';\n"
				+ "        }\n"
				+ "    }\n"
				+ "}\n\n";
			
			s += "function removeLogoText(){\n"
					  +  "    var ilogoimport = document.getElementById('"+SMDoingbusinessasaddress.ParamiInvoicelogo+"');\n"
					  +  "    var plogoimport = document.getElementById('"+SMDoingbusinessasaddress.ParamiProposallogo+"');\n"
					  +  "    var dlogoimport = document.getElementById('"+SMDoingbusinessasaddress.ParamiDeliveryTicketReceiptLogo+"');\n"
					  +  "    var wlogoimport = document.getElementById('"+SMDoingbusinessasaddress.ParamiWorkOrderReceiptLogo+"');\n"
					  +  "    ilogoimport.value = '';\n"
					  +  "    plogoimport.value = '';\n"
					  +  "    dlogoimport.value = '';\n"
					  +  "    wlogoimport.value = '';\n"
					  +  "}\n"
					;
					
			
			//Delete:
			s += "function isdelete(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.getElementById(\"" + FORM_NAME + "\").submit();\n"
				+ "    return true;\n"
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

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

