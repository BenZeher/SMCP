package smap;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class APControlPaymentsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String BUTTON_LABEL_SAVE = "Update";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"AP Open Invoice",
				SMUtilities.getFullClassName(this.toString()),
				"smap.APControlPaymentsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APControlPayments
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APControlPayments)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		String sVendor = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, request);
		String sDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, request);
		
	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Accounts Payable Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, sVendor, sDocNumber), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTableaptransactions.svendor + "=" + sVendor
				+ "&" + SMTableaptransactions.sdocnumber + "=" + sDocNumber
				+ "&Warning=Could not load open invoice for vendor '" + sVendor + "' with document number '" 
					+ sDocNumber + "' - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, String sVendor, String sDocNumber) throws SQLException{

		String s = "";
		
		s += sCommandScript(sm) + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
			+ " id=\"" + COMMAND_FLAG + "\""
			+ "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + sm.getsDBID() + "\""
				+ "\">";
		
		s += "<TABLE BORDER=1>\n";
		
		String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " = '" + sVendor + "')"
				+ " AND (" + SMTableaptransactions.sdocnumber + " = '" + sDocNumber + "')"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(),
			sm.getsDBID(), 
			"MySQL",
			SMUtilities.getFullClassName(this.toString()) + ".getEditHTML - user: " + sm.getUserID()
			+ " - "
			+ sm.getFullUserName()
				);
		if (rs.next()){
			//Doc ID
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT><B>Document ID</B>:</TD>\n"
				+ "    <TD><B>" 
				+ Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid))
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			//Vendor
    		boolean bViewVendorInfoAllowed = SMSystemFunctions.isFunctionPermitted(
    			SMSystemFunctions.APDisplayVendorInformation, 
    			sm.getUserID(), 
    			getServletContext(), 
    			sm.getsDBID(), 
    			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
       		);
        	String sVendorLink = sVendor;
        	if(bViewVendorInfoAllowed){
        		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." 
    	    		+ "APDisplayVendorInformation" 
    	    		+ "?" + "VendorNumber" + "=" + sVendor
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
    	    		+ "\">"
    	    		+ sVendor
    	    		+ "</A>";
        	}
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT><B>Vendor</B>:</TD>\n"
				+ "    <TD><B>" 
				+ sVendorLink
				+ " <INPUT TYPE=HIDDEN"
					+ " NAME = \"" + SMTableaptransactions.svendor + "\""
					+ " ID = \"" + SMTableaptransactions.svendor + "\""
					+ " VALUE = \"" + sVendor + "\""
					+ ">"
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			//Doc Number:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Document Number</B>:</TD>\n"
					+ "    <TD><B>" 
					+ sDocNumber
					+ " <INPUT TYPE=HIDDEN"
						+ " NAME = \"" + SMTableaptransactions.sdocnumber + "\""
						+ " ID = \"" + SMTableaptransactions.sdocnumber + "\""
						+ " VALUE = \"" + sDocNumber + "\""
						+ ">"
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Doc Date:
			String sDocDate = SMUtilities.EMPTY_DATE_VALUE;
			try {
				sDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e1) {
				//Don't need to do anything here
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Document Date</B>:</TD>\n"
					+ "    <TD><B>" 
					+ sDocDate
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;

			//Document type:
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT><B>Document type</B>:</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
				+ SMTableapbatchentries.getDocumentTypeLabel(rs.getInt(SMTableaptransactions.idoctype))
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
    		//Tax:
    		String sTax = "(N/A)";
    		if (rs.getString(SMTableaptransactions.staxjurisdiction).compareToIgnoreCase("") != 0){
    			sTax = rs.getString(SMTableaptransactions.staxjurisdiction) + " - " + rs.getString(SMTableaptransactions.staxtype);
    		}
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT><B>Tax</B>:</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
				+ sTax
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			//Original amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Original amt</B>:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginalamt))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;

			//Current amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Current amt</B>:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Original discount amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Original discount amt</B>:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdoriginaldiscountavailable))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//On hold?
			//If the request returns an 'on hold' parameter, OR if the recordset returns one, set it to 'on hold':
     		String sTemp = "";
     		if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.ionhold, sm.getRequest()).compareToIgnoreCase("Y") == 0){
    			sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
    		}else{
    			if (rs.getInt(SMTableaptransactions.ionhold) == 1){
    				sTemp += clsServletUtilities.CHECKBOX_CHECKED_STRING;
    			}
    		}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>On hold?</B>:</TD>\n"
					+ "    <TD><B>" 
					+ "<INPUT TYPE=CHECKBOX"
	     			+ " NAME=\"" + SMTableaptransactions.ionhold + "\""
	     			+ " ID=\"" + SMTableaptransactions.ionhold + "\""
	     			+ " " + sTemp
	 	    		+ " onchange=\"flagDirty();\""
	 	    		+ ">"
					+ "</B></TD>\n"
					+ "    <TD>Check to put invoice ON HOLD.</TD>\n"
					+ "  </TR>\n"
				;
     		
			//Due date:
			String sDueDate = "";
			try {
				sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.datduedate), 
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
						SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e) {
				sDueDate = "<B><FONT COLOR=RED>" + SMUtilities.EMPTY_DATE_VALUE + "</FONT></B>";
			}
			//If there's any date coming back in the request, use that instead:
			if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datduedate, sm.getRequest()).compareToIgnoreCase("") != 0){
				sDueDate = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datduedate, sm.getRequest());
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Due&nbsp;date</B>:</TD>\n"
					+ "    <TD>"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMTableaptransactions.datduedate + "\""
		    		+ " VALUE=\"" + sDueDate + "\""
		    		+ " MAXLENGTH=" + "10"
		    		+ " SIZE = " + "8"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableaptransactions.datduedate, getServletContext())
		    		+ "</TD>\n"
		    		+ "    <TD>Update the invoice due date here.</TD>\n"
					+ "  </TR>\n"
		    	;
			
			//Discount date:
			String sDiscountDate = "";
			try {
				sDiscountDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.datdiscountdate), 
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
						SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e) {
				sDiscountDate = "<B><FONT COLOR=RED>" + SMUtilities.EMPTY_DATE_VALUE + "</FONT></B>";
			}
			//If there's any date coming back in the request, use that instead:
			if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datdiscountdate, sm.getRequest()).compareToIgnoreCase("") != 0){
				sDiscountDate = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.datdiscountdate, sm.getRequest());
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Discount&nbsp;date</B>:</TD>\n"
					+ "    <TD>"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMTableaptransactions.datdiscountdate + "\""
		    		+ " VALUE=\"" + sDiscountDate + "\""
		    		+ " MAXLENGTH=" + "10"
		    		+ " SIZE = " + "8"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    		+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableaptransactions.datdiscountdate, getServletContext())
		    		+ "</TD>\n"
		    		+ "    <TD>Update the invoice discount date here.</TD>\n"
					+ "  </TR>\n"
		    	;
			
			//Discount amt available:
			String sDiscountAvailable = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable));

			//If there's any value coming back in the request, use that instead:
			if (clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.bdcurrentdiscountavailable, sm.getRequest()).compareToIgnoreCase("") != 0){
				sDiscountAvailable = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.bdcurrentdiscountavailable, sm.getRequest());
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT><B>Discount&nbsp;Available</B>:</TD>\n"
					+ "    <TD>"
					+ "<INPUT TYPE=TEXT"
					+ " style=\"text-align:right;\""
					+ " NAME=\"" + SMTableaptransactions.bdcurrentdiscountavailable + "\""
		    		+ " VALUE=\"" + sDiscountAvailable + "\""
		    		+ " MAXLENGTH=" + "13"
		    		+ " SIZE = " + "8"
		    		+ " onchange=\"flagDirty();\""
		    		+ ">"
		    		+ "</TD>\n"
		    		+ "    <TD>Update the discount amount still available here.</TD>\n"
					+ "  </TR>\n"
		    	;
		}else{
			rs.close();
			throw new SQLException("No record found.");
		}

		s += "</TABLE>";
		
		s += createSaveButton();
		return s;
	}
	private String sCommandScript(SMMasterEditEntry sm){
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
		
		s += "function promptToSave(){\n"		
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
		
		s += "function flagDirty() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n";
		
		s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
		
		s += " </script>\n";
		
		return s;
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + BUTTON_LABEL_SAVE + "\""
				+ " name=\"" + BUTTON_LABEL_SAVE + "\""
				+ " onClick=\"save();\">"
				+ BUTTON_LABEL_SAVE
				+ "</button>\n"
				;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
