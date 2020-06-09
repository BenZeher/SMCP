package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
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

import SMClasses.SMTax;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablelabortypes;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;


public class SMEditSMSummaryEdit extends HttpServlet {
	
	public static final String SAVE_BUTTON_CAPTION = "Save " + SMEstimateSummary.OBJECT_NAME;
	public static final String SAVE_COMMAND_VALUE = "SAVESUMMARY";
	public static final String DELETE_BUTTON_CAPTION = "Delete " + SMEstimateSummary.OBJECT_NAME;
	public static final String DELETE_COMMAND_VALUE = "DELETESUMMARY";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String REMOVE_ESTIMATE_COMMAND = "REMOVEESTIMATE";
	public static final String PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED = "REMOVEESTIMATELINENUMBER";
	public static final String BUTTON_ADD_MANUAL_ESTIMATE_CAPTION = "Add estimate manually";
	public static final String BUTTON_ADD_MANUAL_ESTIMATE = "ADDMANUALESTIMATE";
	public static final String ADD_MANUAL_ESTIMATE_COMMAND = "ADDMANUALESTIMATECOMMAND";
	public static final String BUTTON_ADD_VENDOR_QUOTE_CAPTION = "Add vendor quote number:";
	public static final String BUTTON_ADD_VENDOR_QUOTE = "ADDVENDORQUOTE";
	public static final String FIELD_VENDOR_QUOTE = "VENDORQUOTENUMBER";
	public static final String BUTTON_FIND_VENDOR_QUOTE_CAPTION = "Find vendor quote";
	public static final String BUTTON_FIND_VENDOR_QUOTE = "FINDVENDORQUOTE";
	public static final String FIND_VENDOR_QUOTE_COMMAND_VALUE = "FINDVENDORQUOTECOMMAND";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_COST = "LABELCALCULATEDTOTALMATERIALCOST";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
	public static final String LABEL_CALCULATED_TOTAL_FREIGHT = "LABELCALCULATEDTOTALFREIGHT";
	public static final String LABEL_CALCULATED_TOTAL_FREIGHT_CAPTION = "TOTAL FREIGHT:";
	public static final String LABEL_CALCULATED_TOTAL_LABOR_UNITS = "LABELCALCULATEDTOTALLABORUNITS";
	public static final String LABEL_CALCULATED_TOTAL_LABOR_UNITS_CAPTION = "TOTAL LABOR UNITS:";
	public static final String LABEL_CALCULATED_TOTAL_LABOR_COST = "LABELCALCULATEDTOTALLABORCOST";
	public static final String LABEL_CALCULATED_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
	public static final String LABEL_CALCULATED_TOTAL_MARKUP = "LABELCALCULATEDTOTALMARKUP";
	public static final String LABEL_CALCULATED_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL = "LABELCALCULATEDTOTALTAX";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
	public static final String LABEL_CALCULATED_TOTAL_FOR_SUMMARY = "LABELCALCULATEDTOTALFORSUMMARY";
	public static final String LABEL_CALCULATED_TOTAL_FOR_SUMMARY_CAPTION = "CALCULATED TOTAL FOR ESTIMATE SUMMARY #";
	public static final String LABEL_CALCULATED_RETAIL_SALES_TAX = "LABELCALCULATEDRETAILSALESTAX";
	public static final String LABEL_CALCULATED_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_COST = "LABELADJUSTEDTOTALMATERIALCOST";
	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
	public static final String FIELD_ADJUSTED_TOTAL_FREIGHT_CAPTION = "TOTAL FREIGHT:";
	public static final String FIELD_ADJUSTED_LABOR_UNITS_CAPTION = "LABOR UNITS:";
	public static final String FIELD_ADJUSTED_COST_PER_LABOR_UNIT_CAPTION = "LABOR COST/UNIT:";
	public static final String LABEL_ADJUSTED_TOTAL_LABOR_COST = "LABELADJUSTEDTOTALLABORCOST";
	public static final String LABEL_ADJUSTED_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
	public static final String FIELD_ADJUSTED_MU_PER_LABOR_UNIT = "LABELADJUSTEDMUPERLABORUNIT";
	public static final String FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION = "MU PER LABOR UNIT:";
	public static final String FIELD_ADJUSTED_MU_PERCENTAGE = "LABELADJUSTEDMUPERCENTAGE";
	public static final String FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION = "MU PERCENTAGE:";
	public static final String FIELD_ADJUSTED_GP_PERCENTAGE = "LABELADJUSTEDGPPERCENTAGE";
	public static final String FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION = "GP PERCENTAGE:";
	public static final String LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL = "LABELADJUSTEDTOTALTAXONMATERIAL";
	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY = "LABELADJUSTEDTOTALFORSUMMARY";
	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION = "ADJUSTED TOTAL FOR ESTIMATE SUMMARY #";
	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX = "LABELADJUSTEDRETAILSALESTAX";
	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
	public static final String FIELD_BACK_INTO_DESIRED_PRICE = "FIELDBACKINTODESIREDPRICE";
	public static final String BUTTON_BACK_INTO_PRICE_CAPTION = "Process";
	public static final String BUTTON_BACK_INTO_PRICE = "Process";
	public static final String BUTTON_REMOVE_ESTIMATE_CAPTION = "Remove";
	public static final String BUTTON_REMOVE_ESTIMATE_BASE = "REMOVEESTIMATE";
	public static final String UNSAVED_SUMMARY_LABEL = "(UNSAVED)";
	public static final String WARNING_OBJECT = "SMEDITSMSUMMARYWARNINGOBJECT";
	public static final String RESULT_STATUS_OBJECT = "SMEDITSMSUMMARYRESULTSTATUSOBJECT";
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimateSummary.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditSMSummaryAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);   
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMEstimateSummary summary = new SMEstimateSummary(request);
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMEstimateSummary.OBJECT_NAME) != null){
	    	summary = (SMEstimateSummary) currentSession.getAttribute(SMEstimateSummary.OBJECT_NAME);
	    	currentSession.removeAttribute(SMEstimateSummary.OBJECT_NAME);
			
	    	try {
	    		summary.loadEstimates(summary.getslid(), smedit.getsDBID(), getServletContext(), smedit.getFullUserName());
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMTablesmestimatesummaries.lid + "=" + summary.getslid()
						+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
			}
	    	
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
	    
	    if (currentSession.getAttribute(WARNING_OBJECT) != null) {
	    	String sWarning = (String)currentSession.getAttribute(WARNING_OBJECT);
	    	currentSession.removeAttribute(WARNING_OBJECT);
	    	if (sWarning.compareToIgnoreCase("") != 0) {
	    		smedit.getPWOut().println("<BR><FONT COLOR=RED><B>WARNING: " + sWarning + "</B></FONT><BR>");
	    	}
	    }
	    
	    if (currentSession.getAttribute(RESULT_STATUS_OBJECT) != null) {
	    	String sStatus = (String)currentSession.getAttribute(RESULT_STATUS_OBJECT);
	    	currentSession.removeAttribute(RESULT_STATUS_OBJECT);
	    	if (sStatus.compareToIgnoreCase("") != 0) {
	    		smedit.getPWOut().println("<BR><FONT COLOR=GREEN><B>WARNING: " + sStatus + "</B></FONT><BR>");
	    	}
	    }
	    
	    try {
	    	createEditPage(
	    		getEditHTML(
	    			smedit, 
	    			summary, 
	    			ServletUtilities.clsManageRequestParameters.get_Request_Parameter(FIELD_VENDOR_QUOTE, request)
	    		), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit,
				summary
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
			SMMasterEditEntry sm,
			SMEstimateSummary summary
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
				
		//Keep track of hidden variables here:
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED + "\""
			+ " ID=\"" + PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED + "\""
			+ " VALUE=\"" + "" + "\""
			+ ">"
			);
		
		//This is used to store the on-the-fly retail sales tax rate in case the user changes the tax drop down
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.bdtaxrate + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.bdtaxrate + "\""
			+ " VALUE=\"" + summary.getsbdtaxrate() + "\""
			+ ">"
			);
		
		//This is used to calculate taxes on-the-fly
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\""
			+ " VALUE=\"" + summary.getsicalculatetaxoncustomerinvoice() + "\""
			+ ">"
			);
		
		//This is used to calculate taxes on-the-fly
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\""
			+ " VALUE=\"" + summary.getsicalculatetaxonpurchaseorsale() + "\""
			+ ">"
			);
		
		
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

	private String getEditHTML(SMMasterEditEntry sm, SMEstimateSummary summary, String sFoundVendorQuote) throws Exception{
		
		String sControlHTML = "";
		
		String s = sCommandScripts(summary, sm);
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + " - user: " + sm.getFullUserName()
			);
		} catch (Exception e1) {
			throw new Exception("Error [202005274220] - could not get connection - " + e1.getMessage());
		}
		
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\""
				+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">" + "\n";
		
		String sLid = summary.getslid();
		if (sm.getAddingNewEntryFlag()){
			sLid = UNSAVED_SUMMARY_LABEL;
		}

		s += "<B>Summary ID</B>: " + sLid + "\n" 
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.lid + "\""
			+ " VALUE=\"" + summary.getslid() + "\""
			+ " ID=\"" + summary.getslid() + "\""
			+ ">"
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
			+ " VALUE=\"" + summary.getsjobname().replace("\"", "&quot;") + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sjobnameLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
			+ " onchange=\"flagDirty();\""
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
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		//Labor type:
		ArrayList<String> arrLaborTypes = new ArrayList<String>(0);
		ArrayList<String> arrLaborTypeDescriptionsDescriptions = new ArrayList<String>(0);
		String SQL = "SELECT"
			+ " " + SMTablelabortypes.sID
			+ ", " + SMTablelabortypes.sLaborName
			+ " FROM " + SMTablelabortypes.TableName
			+ " ORDER BY " + SMTablelabortypes.sLaborName
		;
		//First, add a blank item so we can be sure the user chose one:
		arrLaborTypes.add("");
		arrLaborTypeDescriptionsDescriptions.add("*** Select labor type ***");
		
		try {
			ResultSet rsLaborTypes = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsLaborTypes.next()) {
				arrLaborTypes.add(Long.toString(rsLaborTypes.getLong(SMTablelabortypes.sID)));
				arrLaborTypeDescriptionsDescriptions.add(rsLaborTypes.getString(SMTablelabortypes.sLaborName));
			}
			rsLaborTypes.close();
		} catch (SQLException e) {
			s += "<B>Error [1590535453] reading labor types - " + e.getMessage() + "</B><BR>";
		}

		sControlHTML = "<SELECT NAME = \"" + SMTablesmestimatesummaries.ilabortype + "\""
				+ " onchange=\"flagDirty();\""
				 + " >\n"
			;
			for (int i = 0; i < arrLaborTypes.size(); i++){
				sControlHTML += "<OPTION";
				if (arrLaborTypes.get(i).toString().compareTo(summary.getsilabortype()) == 0){
					sControlHTML += " selected=yes";
				}
				sControlHTML += " VALUE=\"" + arrLaborTypes.get(i).toString() + "\">" 
					+ arrLaborTypeDescriptionsDescriptions.get(i).toString() + "\n";
			}
		sControlHTML += "</SELECT> \n"
		;
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "<B>Labor type:</B>"
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ sControlHTML
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		//Tax Type:
		ArrayList<String> arrTaxes = new ArrayList<String>(0);
		ArrayList<String> arrTaxDescriptions = new ArrayList<String>(0);
		SQL = "SELECT"
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
			ResultSet rsTaxes = clsDatabaseFunctions.openResultSet(SQL, conn);
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

		sControlHTML = "\n<SELECT"
				+ " NAME = \"" + SMTablesmestimatesummaries.itaxid + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.itaxid + "\""
				+ " onchange=\"taxChange(this);\""
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
		
		//Order type:
		//First, add a blank item so we can be sure the user chose one:
		sControlHTML = "\n<SELECT"
			+ " NAME = \"" + SMTablesmestimatesummaries.iordertype + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.iordertype + "\""
			+ " onchange=\"flagDirty();\""
			+ " >\n"
		;
		
		sControlHTML += "<OPTION";
		sControlHTML += " VALUE=\"" + "" + "\">" 
			+ "*** Select order type ***" + "\n";
		
		sControlHTML += "<OPTION";
		if (Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE).compareTo(summary.getsiordertype()) == 0){
			sControlHTML += " selected=yes";
		}
		sControlHTML += " VALUE=\"" + Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE) + "\">" 
			+ SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_ACTIVE) + "\n";
		
		sControlHTML += "<OPTION";
		if (Integer.toString(SMTableorderheaders.ORDERTYPE_FUTURE).compareTo(summary.getsiordertype()) == 0){
			sControlHTML += " selected=yes";
		}
		sControlHTML += " VALUE=\"" + Integer.toString(SMTableorderheaders.ORDERTYPE_FUTURE) + "\">" 
			+ SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_FUTURE) + "\n";
		
		sControlHTML += "<OPTION";
		if (Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE).compareTo(summary.getsiordertype()) == 0){
			sControlHTML += " selected=yes";
		}
		sControlHTML += " VALUE=\"" + Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE) + "\">" 
			+ SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE) + "\n";

		sControlHTML += "<OPTION";
		if (Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING).compareTo(summary.getsiordertype()) == 0){
			sControlHTML += " selected=yes";
		}
		sControlHTML += " VALUE=\"" + Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING) + "\">" 
			+ SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_STANDING) + "\n";
		
		sControlHTML += "</SELECT> \n"
		;
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "<B>Order type:</B>"
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ sControlHTML
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Description:"
			+ "</TD>" + "\n"
			+ "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns - 1) 
				+ " class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimatesummaries.sdescription + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.sdescription + "\""
			+ " VALUE=\"" + summary.getsdescription().replace("\"", "&quot;") + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sdescriptionLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Remarks:"
			+ "</TD>" + "\n"
			+ "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns - 1) 
				+ " class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimatesummaries.sremarks + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.sremarks + "\""
			+ " VALUE=\"" + summary.getsremarks().replace("\"", "&quot;") + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sremarksLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		//Include an outer table:
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		s += "  <TR>" + "\n";
		s += "    <TD>" + "\n";
		s += buildEstimateTable(conn, summary, sFoundVendorQuote, sm.getsDBID());
		
		s += buildTotalsTable(summary);
		
		s += printBackIntoControls();
		
		//Close the outer table:
		s += "    </TD>" + "\n";
		s += "  </TR>" + "\n";
		s += "</TABLE>" + "\n";
		

		return s;
	}
	private String printBackIntoControls() {
		String s = "";
		
		s += "<BR><B>'Back Into' price:</B> Adjust mark-up to bring total adjusted selling price to: "
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + FIELD_BACK_INTO_DESIRED_PRICE + "\""
			+ " ID=\"" + FIELD_BACK_INTO_DESIRED_PRICE + "\""
			+ " VALUE=\"" + "" + "\""
			+ " MAXLENGTH=" + 15
			+ " STYLE=\"width: 1.5in; height: 0.25in\""
			+ ">"
			+ "&nbsp;"
			+ createBackIntoButton()
			+ "<BR>"
		;
		return s;
	}
	private String buildEstimateTable(
		Connection conn, 
		SMEstimateSummary summary, 
		String sFoundVendorQuote,
		String sDBID
		) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 6;
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" style = \" width:100%; \" >" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
			+ "ESTIMATES"
			+ "    </TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		//Headings:
		s += "  <TR>" + "\n";
		
		//Line number:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Line #"
			+ "</TD>" + "\n"
		;
		
		//Estimate number:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Estimate #"
			+ "</TD>" + "\n"
		;
		
		//Remove?:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Remove?"
			+ "</TD>" + "\n"
		;
		
		//Qty:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Quantity"
			+ "</TD>" + "\n"
		;
		
		//Desc:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Product Description"
			+ "</TD>" + "\n"
		;
		
		//For each estimate, show the totals:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Price"
			+ "</TD>" + "\n"
		;		
		
		s += "  </TR>" + "\n";
		
		//Get all the estimates:
		
		for (int i = 0; i < summary.getEstimateArray().size(); i++) {
			s += "  <TR>" + "\n";
			
			//Line #:
			String sEstimateLink = "&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMEstimateEdit"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + SMTablesmestimates.lid + "=" + summary.getEstimateArray().get(i).getslid()
	    		+ "&" + SMTablesmestimates.lsummarylid + "=" + summary.getEstimateArray().get(i).getslsummarylid()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
	    		+ "\">" + summary.getEstimateArray().get(i).getslsummarylinenumber() + "</A>"
	    		+ "&nbsp;"
    		;
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" >"
					+ sEstimateLink
					+ "</TD>" + "\n"
				;
			
			//Estimate ID:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" >"
					+ summary.getEstimateArray().get(i).getslid()
					+ "</TD>" + "\n"
				;		
			
			//Remove button:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED + "\" >"
					+ buildRemoveEstimateButton(summary.getEstimateArray().get(i).getslsummarylinenumber())
					+ "</TD>" + "\n"
				;
			
			//Quantity:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" >"
					+ summary.getEstimateArray().get(i).getsbdquantity()
					+ "</TD>" + "\n"
				;
			
			//Product description:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
					+ summary.getEstimateArray().get(i).getsproductdescription()
					+ "</TD>" + "\n"
				;
			
			//Price:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" >"
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(summary.getEstimateArray().get(i).getTotalPrice(conn))
					+ "</TD>" + "\n"
				;
			
			s += "  </TR>" + "\n";
		}
		
		
		s += "  <TR>" + "\n";
		s += "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns) + " >"
			+ buildEstimateButtons(sFoundVendorQuote)
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		return s;
		
	}
	
	private String buildTotalsTable(SMEstimateSummary summary) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 6;
		
		s += "<BR>";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" style = \" width:100%; \" >" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
			+ "CALCULATED TOTALS"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		//total material cost:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdextendedcostScale, summary.getbdtotalmaterialcostonestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total freight
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_TOTAL_FREIGHT_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdfreightScale, summary.getbdtotalfreightonestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total labor units:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 3) + " >"
			+ LABEL_CALCULATED_TOTAL_LABOR_UNITS_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborquantityScale, summary.getbdtotallaborunitsonestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		
		//total labor cost:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ LABEL_CALCULATED_TOTAL_LABOR_COST_CAPTION
			+ "</TD>" + "\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborcostperunitScale, summary.getbdtotallaborcostonestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		//total mark-up
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_TOTAL_MARKUP_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdmarkupamountScale, summary.getbdtotalmarkuponestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total tax
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			//+ "<LABEL NAME = \""
			+ summary.getstaxdescription() + " "
			+ summary.getsbdtaxrate() + "% "
			+ LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total amount for summary
		String sSummaryID = UNSAVED_SUMMARY_LABEL;
		if (
			(summary.getslid().compareToIgnoreCase("-1") != 0)
			&& (summary.getslid().compareToIgnoreCase("0") != 0)
			&& (summary.getslid().compareToIgnoreCase("") != 0)			
		) {
			sSummaryID = summary.getslid();
		}
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			//+ " style = \" font-size: large; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, summary.getbdcalculatedtotalprice())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			//+ " style = \" font-size: large; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_RETAIL_SALES_TAX_CAPTION
			+ "</TD>" + "\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_RETAIL_SALES_TAX + "\""
			+ " ID = \"" + LABEL_CALCULATED_RETAIL_SALES_TAX + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//space:
		s += "  <TR>" + "\n";
		s += "  </TR>" + "\n";
		
		//ADJUSTED VALUES:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
			+ "ADJUSTED TOTALS"
			+ "</TD>" + "\n"
		;
		
		//total adjusted material cost:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdextendedcostScale, summary.getbdtotalmaterialcostonestimates())
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total adjusted freight
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ FIELD_ADJUSTED_TOTAL_FREIGHT_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + summary.getsbdadjustedfreight() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Labor units
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ FIELD_ADJUSTED_LABOR_UNITS_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + summary.getsbdadjustedlaborunitqty() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		
		//Total cost per labor unit
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ FIELD_ADJUSTED_COST_PER_LABOR_UNIT_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"" + summary.getsbdadjustedlaborcostperunit() + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		
		//Total labor cost
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ LABEL_ADJUSTED_TOTAL_LABOR_COST_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<LABEL"
				+ " NAME = \"" + LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ " ID = \"" + LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ ">"
				+ "0.00"  // TODO - fill in this value with javascript
				+ "</LABEL>"
				+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";
		
		//MU per labor unit
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION
				//+ "</TD>" + "\n"
				
				//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				//+ ">"
				+ "&nbsp;"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " ID = \"" + FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = 0.00"
				+ " onchange=\"calculateMUusingMUperlaborunit();\""
				+ ">"
				+ "</INPUT>"
				
				//MU Pctge
				+ "&nbsp;"
				+ FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "&nbsp;"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " ID = \"" + FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"0.00\""
				+ " onchange=\"calculateMUusingMUpercentage();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		
		//GP percentage
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " ID = \"" + FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"0.00\""
				+ " onchange=\"calculateMUusingGPpercentage();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		
		//Total MU
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"" + summary.getsbdadjustedmarkupamt() + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";
		
		//Total tax on material
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Adjusted total
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			//+ " style = \" font-size: large; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ ">"
			+ "0.00"  // TODO - fill in this value with javascript
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Retail sales tax
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			//+ " style = \" font-size: large; \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_ADJUSTED_RETAIL_SALES_TAX + "\""
			+ " ID = \"" + LABEL_ADJUSTED_RETAIL_SALES_TAX + "\""
			+ ">"
			+ "0.00"  // TODO - fill in this value with javascript
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		return s;
	}
	
	private String buildRemoveEstimateButton(String sSummaryLineNumber) {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + BUTTON_REMOVE_ESTIMATE_CAPTION + "\""
			+ " name=\"" + BUTTON_REMOVE_ESTIMATE_BASE + "\""
			+ " id=\"" + BUTTON_REMOVE_ESTIMATE_BASE + "\""
			+ " onClick=\"removeestimate('" + sSummaryLineNumber + "');\">"
			+ BUTTON_REMOVE_ESTIMATE_CAPTION
			+ "</button>\n"
		;

		return s;
	}
	
	private String createBackIntoButton() {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + BUTTON_BACK_INTO_PRICE_CAPTION + "\""
			+ " name=\"" + BUTTON_BACK_INTO_PRICE + "\""
			+ " id=\"" + BUTTON_BACK_INTO_PRICE + "\""
			+ " onClick=\"backintoprice();\">"
			+ BUTTON_BACK_INTO_PRICE_CAPTION
			+ "</button>\n"
		;
		return s;
	}
	private String buildEstimateButtons(String sFoundVendorQuote) {
		String s = "";
		
		//Button for adding a manual quote:
		
		s += "<button type=\"button\""
			+ " value=\"" + BUTTON_ADD_MANUAL_ESTIMATE_CAPTION + "\""
			+ " name=\"" + BUTTON_ADD_MANUAL_ESTIMATE + "\""
			+ " id=\"" + BUTTON_ADD_MANUAL_ESTIMATE + "\""
			+ " onClick=\"addmanualestimate();\">"
			+ BUTTON_ADD_MANUAL_ESTIMATE_CAPTION
			+ "</button>\n"
		;
		
		s += "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		s += "<button type=\"button\""
				+ " value=\"" + BUTTON_ADD_VENDOR_QUOTE_CAPTION + "\""
				+ " name=\"" + BUTTON_ADD_VENDOR_QUOTE + "\""
				+ " id=\"" + BUTTON_ADD_VENDOR_QUOTE + "\""
				+ " onClick=\"addvendorquote();\">"
				+ BUTTON_ADD_VENDOR_QUOTE_CAPTION
				+ "</button>\n"
			;
		
		s += "<INPUT TYPE=TEXT NAME=\"" + FIELD_VENDOR_QUOTE + "\""
				+ " ID=\"" + FIELD_VENDOR_QUOTE + "\""
				+ " VALUE=\"" + sFoundVendorQuote + "\""
			    + " SIZE=" + "18"
				+ " MAXLENGTH=" + Integer.toString(SMTablesmestimates.svendorquotenumberLength)
				//+ " ONCHANGE=\"flagDirty();\""
				+ ">" + "\n"
		;
		
		s += "&nbsp;";
		
		s += "\n<button type=\"button\""
				+ " value=\"" + BUTTON_FIND_VENDOR_QUOTE_CAPTION + "\""
				+ " name=\"" + BUTTON_FIND_VENDOR_QUOTE + "\""
				+ " id=\"" + BUTTON_FIND_VENDOR_QUOTE + "\""
				+ " onClick=\"findvendorquote();\">"
				+ BUTTON_FIND_VENDOR_QUOTE_CAPTION
				+ "</button>\n"
			;
		
		return s;
	}

	private String sCommandScripts(
			SMEstimateSummary summary, 
			SMMasterEditEntry smedit
			) throws Exception{
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
			
			s += "window.onload = triggerinitiation;\n";

			//Build an array of taxes and rates to do the 'adjusted retail sales tax' calc on the fly:
			int iCounter = 0;
			String staxrates = "";
			String scalculateonpurchaseorsale = "";
			String scalculatetaxoncustomerinvoice = "";
			
			String SQL = "SELECT"
				+ " " + SMTabletax.lid
				+ ", " + SMTabletax.bdtaxrate
				+ ", " + SMTabletax.icalculateonpurchaseorsale
				+ ", " + SMTabletax.icalculatetaxoncustomerinvoice
				+ " FROM " + SMTabletax.TableName
				+ " ORDER BY " + SMTabletax.lid
				;

			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					smedit.getsDBID(), 
					"MySQL", 
					this.toString() + " [1591042478] SQL: " + SQL 
				);
				BigDecimal bdTaxRateAsPercentage = new BigDecimal("0.00");
				while (rs.next()){
					iCounter++;
					bdTaxRateAsPercentage = rs.getBigDecimal(SMTabletax.bdtaxrate);
					staxrates += "staxrates[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
						+ "\"] = \"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTabletax.bdtaxratescale, bdTaxRateAsPercentage) + "\";\n";
					
					scalculateonpurchaseorsale += "scalculateonpurchaseorsale[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
					+ "\"] = \"" + Integer.toString(rs.getInt(SMTabletax.icalculateonpurchaseorsale)) + "\";\n";
					
					scalculatetaxoncustomerinvoice += "scalculatetaxoncustomerinvoice[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
					+ "\"] = \"" + Integer.toString(rs.getInt(SMTabletax.icalculatetaxoncustomerinvoice)) + "\";\n";
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1591112142] reading taxes for javascript - " + e.getMessage());
			}
			
			//Create the arrays, if there are any:
			if (iCounter > 0){
				s += "var staxrates = new Array(" + Integer.toString(iCounter) + ")\n";
				s += staxrates + "\n";
				s += "var scalculateonpurchaseorsale = new Array(" + Integer.toString(iCounter) + ")\n";
				s += scalculateonpurchaseorsale + "\n";
				s += "var scalculatetaxoncustomerinvoice = new Array(" + Integer.toString(iCounter) + ")\n";
				s += scalculatetaxoncustomerinvoice + "\n";
			}
			
			s += "\n";
			
		    //If this is an existing summary, and if the selected tax was re-configured since the summary was last saved,
		    //notify the user that the tax has changed, and they may have to click again in the tax to reset the tax values:
			String sTaxCheckAlert = "";
		    if (!smedit.getAddingNewEntryFlag()) {
			    SMTax tax = new SMTax();
			    tax.set_slid(summary.getsitaxid());
			    try {
					tax.load(smedit.getsDBID(), getServletContext(), smedit.getFullUserName());
					if (
						(tax.get_bdtaxrate().compareToIgnoreCase(summary.getsbdtaxrate()) != 0)
						|| (tax.get_scalculatetaxoncustomerinvoice().compareToIgnoreCase(summary.getsicalculatetaxoncustomerinvoice()) != 0)
						|| (tax.get_scalculateonpurchaseorsale().compareToIgnoreCase(summary.getsicalculatetaxonpurchaseorsale()) != 0)
					) {
						sTaxCheckAlert = "alert('The selected tax has been updated in the system, so the tax calculation may no longer be accurate.  '  \n" 
								+ "       + 'To update the tax information, click on the Tax drop down list, select a different tax, then select' \n" 
								+ " 	  + ' this tax again to trigger an update.')"
								+ "\n"
							;
					}
				} catch (Exception e) {
					throw new Exception("Error [202006021627] - Could not check tax with ID: '" + summary.getsitaxid() + "' - " + e.getMessage());
				}
		    }
			
			s += "function checkfortaxupdates(){\n"	
					+ "    //This function has nothing in it unless the selected tax has been updated.\n"
					+ "    //In that case it will warn the user that the tax on the summary is not up to date.\n"
					+ "    " + sTaxCheckAlert
					+ "}\n\n"
				;
			
			s += "function triggerinitiation(){\n"		
				+ "    recalculatelivetotals();\n"
				+ "    checkfortaxupdates();\n"
				+ "}\n\n"
			;
			
			s += "function taxChange(selectObj) {\n" 
					// get the index of the selected option 
					+ "    var idx = selectObj.selectedIndex;\n"
					// get the value of the selected option 
					+ "    var which = selectObj.options[idx].value;\n"
					//+ "alert(selectObj.options[idx].value);\n"
					// use the selected option value to retrieve the ship to fields from the ship to arrays:
					+ "    if (which != ''){\n"
					+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.bdtaxrate + "\"].value = staxrates[which];\n"
					+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\"].value = scalculatetaxoncustomerinvoice[which];\n"
					+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\"].value = scalculateonpurchaseorsale[which];\n"
					
					//+ "        //Add the tax type and rate to the tax caption: \n"
					//+ "        document.forms[\"MAINFORM\"].elements[\"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION 
					//	+ "\"].innerText = staxrates[which] + '% '" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION + ";\n"
					
					//+ summary.getstaxdescription() + " "
					//+ summary.getsbdtaxrate() + "% " x
					//+ LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION
					//+ "</TD>" + "\n"
					
					//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
					//+ ">"
					//+ "<LABEL"
					//+ " NAME = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
					//+ " ID = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
					
					+ "    }\n"
					//+ "    alert('SMTablesmestimatesummaries.bdtaxrate = ' + document.getElementById(\"" + PARAM_RETAIL_SALES_TAX_RATE + "\").value); \n"
					+ "    flagDirty(); \n"
					+ "}\n\n"; 
			
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
			
			//Find vendor quote:
			s += "function findvendorquote(){\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + FIND_VENDOR_QUOTE_COMMAND_VALUE + "\";\n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Delete:
			s += "function deletesummary(){\n"
					+ "    if (confirm(\"Are you sure you want to delete this estimate summary?\")){\n"
					+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
					+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
					+ "    }\n"
				//+ "    }\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Remove/delete an estimate:
			s += "function removeestimate(sSummaryLineNumber){\n"
				+ "    if (confirm(\"Are you sure you want to delete the estimate on line number \" + sSummaryLineNumber + \"?\")){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REMOVE_ESTIMATE_COMMAND + "\";\n"
				+ "        document.getElementById(\"" + PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED + "\").value = sSummaryLineNumber;\n"
				+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "    }\n"
				+ "}\n"
			;			
			
			//Add a manual estimate:
			s += "function addmanualestimate(sSummaryLineNumber){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + ADD_MANUAL_ESTIMATE_COMMAND + "\";\n"
				+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			s += "function backintoprice(){\n"
					
				//+ "    alert('Back into'); \n"	
				+ "    var currentadjustedtotalprice = parseFloat(\"0.00\");\n"
				+ "    var desiredadjustedtotalprice = parseFloat(\"0.00\");\n"
				+ "    var currentadjustedmarkup = parseFloat(\"0.00\");\n"
				+ "    var requiredadjustedmarkup = parseFloat(\"0.00\");\n"
				+ "    var desireddifference = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        currentadjustedtotalprice = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        currentadjustedtotalprice = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + FIELD_BACK_INTO_DESIRED_PRICE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        desiredadjustedtotalprice = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        desiredadjustedtotalprice = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    desireddifference = desiredadjustedtotalprice - currentadjustedtotalprice; \n"

				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        currentadjustedmarkup = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        currentadjustedmarkup = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    requiredadjustedmarkup = currentadjustedmarkup + desireddifference; \n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=formatNumber(requiredadjustedmarkup);\n"
				+ "    recalculatelivetotals(); \n"
				+ "    document.getElementById(\"" + FIELD_BACK_INTO_DESIRED_PRICE + "\").value = \"\"; \n"
				+ "}\n"
			;
			
			s += "function flagDirty() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "    recalculatelivetotals(); \n"
				+ "}\n";
			
			
			//Recalculate live totals:
			s += "function recalculatelivetotals(){\n"
				//+ "    alert('Recalculating');\n"
				+ "    formatnumberinputfields(); \n"
				
				// TJR - 6/2/2020 - we don't want the tax to update automatically when the page loads.
				// That should be done deliberately by the user if he WANTS to update the tax info.
				//+ "    //Set the retail sales tax rate, based on the current index of the tax drop down: \n"
				//+ "    taxChange(document.getElementById(\"" + SMTablesmestimatesummaries.itaxid + "\")); \n"
				+ "\n"
				
				+ "    var adjustedlabortotalcost = parseFloat(\"0.00\");\n"
				+ "    var adjustedlaborunits = parseFloat(\"0.00\");\n"
				+ "    var adjustedlaborcostperunit = parseFloat(\"0.00\");\n"
				+ "    var adjustedtotalforsummary = parseFloat(\"0.00\");\n"
				+ "    var materialcosttotal = parseFloat(\"0.00\");\n"
				+ "    var adjustedtfreighttotal = parseFloat(\"0.00\");\n"
				+ "    var adjustedmarkuptotal = parseFloat(\"0.00\");\n"
				+ "    var taxonmaterial = parseFloat(\"0.00\");\n"
				+ "    var taxrateaspercentage = parseFloat(\"0.00\");\n"
				+ "    var icalculatetaxoncustomerinvoice = \"0\";\n"
				+ "    var icalculatetaxonpurchaseorsale = \"0\";\n"
				+ "    var totalcalculatedestimateprice = \"0\";\n"
				+ "    var totalfreightonestimates = \"0.00\"; \n"
				+ "    var totallaboronestimates = \"0.00\"; \n"
				+ "    var totalmarkuponestimates = \"0.00\"; \n"
				+ "    var calculatedretailsalestax = \"0.00\"; \n"
				+ "    var retailsalestaxrateaspercent = parseFloat(\"0.00\");\n"
				+ "    var retailsalestaxrateasdecimal = parseFloat(\"0.00\");\n"
				+ "    var adjustedretailsalestax = parseFloat(\"0.00\");\n"
				+ "    var adjustedretailsalestaxamount = parseFloat(\"0.00\");\n"
				+ "    var adjustedmarkupperlaborunit = parseFloat(\"0.00\");\n"
				+ "    var adjustedmarkuppercentage = parseFloat(\"0.00\");\n"
				+ "    var adjustedgppercentage = parseFloat(\"0.00\");\n"
				
				+ "    //Calculate the total adjusted sell price: \n"
				+ "    //Should equal totalmaterialcost + totalfreight + totallabor + totalmarkup + totalmaterialtax \n"
				+ "    \n"
				
				+ "    //Get the material total: \n"
				+ "    var temp = (document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        materialcosttotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        materialcosttotal = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    //Get the tax on material: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdtaxrate + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        taxrateaspercentage = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        taxrateaspercentage = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\").value);\n"
				+ "    if (temp == ''){\n"
				+ "        icalculatetaxoncustomerinvoice = parseInt(\"0\");\n"
				+ "    }else{\n"
				+ "        icalculatetaxoncustomerinvoice = parseInt(temp); \n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\").value);\n"
				+ "    if (temp == ''){\n"
				+ "        icalculatetaxonpurchaseorsale = parseInt(\"0\");\n"
				+ "    }else{\n"
				+ "        icalculatetaxonpurchaseorsale = parseInt(temp); \n"
				+ "    }\n"
				+ "    \n"
				
				+ "    if((icalculatetaxoncustomerinvoice == 0) && ((icalculatetaxonpurchaseorsale == " 
					+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST) + "))){ \n"
				+ "        taxonmaterial = parseFloat((materialcosttotal * (taxrateaspercentage / 100)).toFixed(2)); \n"
				+ "    }else{ \n"
				+ "        taxonmaterial = parseFloat(\"0.00\"); \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\").innerText=formatNumber(taxonmaterial);\n"
				+ "    \n"

				+ "    //Get the calculated total for the estimate summary: \n"
				+ "    //This should equal: totalmaterialcost + totalfreightonestimates + totallaborcost + totalmarkup + totaltax \n"
				
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_FREIGHT + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        totalfreightonestimates = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        totalfreightonestimates = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        totallaboronestimates = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        totallaboronestimates = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_MARKUP + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        totalmarkuponestimates = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        totalmarkuponestimates = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    totalcalculatedestimateprice = \n"
					+ "        materialcosttotal + totalfreightonestimates + totallaboronestimates + totalmarkuponestimates + taxonmaterial; \n"
				+ "    document.getElementById(\"" + LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\").innerText=formatNumber(totalcalculatedestimateprice);\n"
				+ "    \n"
					
				+ "    //Get the calculated retail sales tax for the estimate summary: \n"
				+ "    if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
				+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
				+ "        calculatedretailsalestax = parseFloat((totalcalculatedestimateprice * (taxrateaspercentage / 100)).toFixed(2)); \n"
				+ "    }else{ \n"
				+ "        calculatedretailsalestax = parseFloat(\"0.00\"); \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + LABEL_CALCULATED_RETAIL_SALES_TAX + "\").innerText=formatNumber(calculatedretailsalestax);\n"
				+ "    \n"
				
				+ "    //Get the adjusted freight amount: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedtfreighttotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedtfreighttotal = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    //Calculate the total adjusted labor cost: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedlaborunits = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedlaborunits = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedlaborcostperunit = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedlaborcostperunit = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    adjustedlabortotalcost = adjustedlaborunits * adjustedlaborcostperunit;\n"
				+ "    document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText=formatNumber(adjustedlabortotalcost);\n"
				+ "    \n"
				
				+ "    //Get the adjusted markup amount: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedmarkuptotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedmarkuptotal = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    //Set the tax on material for the adjusted section (same as tax on material above): \n"
				+ "    document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\").innerText=formatNumber(taxonmaterial);\n"
				+ "    \n"
				
				+ "    adjustedtotalforsummary = materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost + adjustedmarkuptotal + taxonmaterial; \n"
				+ "    document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\").innerText=formatNumber(adjustedtotalforsummary);\n"
				+ "    \n"
				
				+ "    //Calculate the MU per labor unit, as a percentage and as a GP percentage: \n"
				+ "    //adjustedmarkupperlaborunit = adjustedmarkuptotal / adjustedlaborunits \n"
				+ "    if(compare2DecimalPlaceFloats(adjustedlaborunits, parseFloat(\"0.00\"))){ \n"
				+ "        adjustedmarkupperlaborunit = adjustedmarkuptotal; \n"
				+ "    } else { \n"
				+ "        adjustedmarkupperlaborunit = adjustedmarkuptotal / adjustedlaborunits; \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\").value=formatNumber(adjustedmarkupperlaborunit);\n"
				+ "    \n\n"
				+ "    //adjustedmarkuppercentage = adjustedmarkuptotal / (materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost) \n"
				+ "    adjustedmarkuppercentage = adjustedmarkuptotal / (materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost); \n"
				+ "    adjustedmarkuppercentage = adjustedmarkuppercentage * 100; \n"
				+ "    document.getElementById(\"" + FIELD_ADJUSTED_MU_PERCENTAGE + "\").value=formatNumber(adjustedmarkuppercentage);\n"
				+ "    \n"
				+ "    //adjustedgppercentage = adjustedmarkuptotal / adjustedtotalforsummary \n"
				+ "    adjustedgppercentage = adjustedmarkuptotal / adjustedtotalforsummary; \n"
				+ "    adjustedgppercentage = adjustedgppercentage * 100; \n"
				+ "    document.getElementById(\"" + FIELD_ADJUSTED_GP_PERCENTAGE + "\").value=formatNumber(adjustedgppercentage);\n"
				+ "    \n"
				
				+ "    //Get the adjusted retail sales tax for the estimate summary: \n"
				+ "    if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
							+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
				+ "        adjustedretailsalestaxamount = parseFloat((adjustedtotalforsummary * (taxrateaspercentage / 100)).toFixed(2)); \n"
				+ "    }else{ \n"
				+ "        adjustedretailsalestaxamount = parseFloat(\"0.00\"); \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + LABEL_ADJUSTED_RETAIL_SALES_TAX + "\").innerText=formatNumber(adjustedretailsalestaxamount);\n"
				+ "    \n"
			;	
			s += "}\n"
	   		;
			
			//Compare floats with 2 decimal precision:
			s += "function compare2DecimalPlaceFloats(float1, float2){ \n"
				+ "    var firstfloatstring = (Math.round(parseFloat(float1)*100)/100).toFixed(2); \n"
				+ "    var secondfloatstring = (Math.round(parseFloat(float2)*100)/100).toFixed(2); \n"
				+ "    if(firstfloatstring.localeCompare(secondfloatstring) == 0){ \n"
				+ "        return true; \n"
				+ "    }else{ \n"
				+ "        return false; \n"
				+ "    }"
				+ "}\n\n"
			;
			
			//Format numbers to have commas as needed:
			s += "function formatNumber(num) {\n"
				+ "    return num.toFixed(2).replace(/(\\d)(?=(\\d{3})+(?!\\d))/g, '$1,') \n"
				+ "}\n\n"
			;
			
			//Format numbers to 4 decimal places and have commas as needed:
			s += "function formatNumberTo4Places(num) {\n"
				+ "    return num.toFixed(4); \n"
				+ "}\n\n"
			;
			
			s += "\n"
				+ "function isNumeric(value) {\n"
				+ "    if ((value == null) || (value == '')) return false;\n"
				+ "    var strippedstring = value.replace(/,/g, '');\n"
				//+ "    alert(strippedstring);\n"
				+ "    if (!strippedstring.toString().match(/^[-]?\\d*\\.?\\d*$/)) return false;\n"
				+ "    return true\n"
				+ "    }\n"
				+ "\n\n"
			;
			
			//Recalculate MU using MU percentage:
			s += "function calculateMUusingMUpercentage(){\n"
				+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
				+ "    var adjustedMUpercentage = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + FIELD_ADJUSTED_MU_PERCENTAGE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedMUpercentage = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        adjustedMUpercentage = parseFloat(temp);\n"
				+ "    }\n"
				+ "    adjustedMUpercentage = adjustedMUpercentage / 100; \n"
				
				+ "    //Get the total cost before mark-up:\n"
				+ "    var materialcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        materialcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        materialcost = parseFloat(temp);\n"
				+ "    }\n"

				+ "    var adjustedfreightcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedfreightcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedfreightcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var adjustedlaborcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedlaborcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedlaborcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var adjustedpremarkupcost = materialcost + adjustedfreightcost + adjustedlaborcost;\n"
				
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=(adjustedpremarkupcost * adjustedMUpercentage).toFixed(2);\n"
				+ "    flagDirty();\n"
				
	   			;
			s += "}\n"
	   		;
			
			//Recalculate MU using MU per labor unit:
			s += "function calculateMUusingMUperlaborunit(){\n"
				+ "    // adjustedtotalmarkup = adjustedMUperlaborunit * adjustedlaborunits \n"
				+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
				+ "    var adjustedMUperlaborunit = parseFloat(\"0.00\");\n"
				+ "    var adjustedlaborunits = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedMUperlaborunit = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        adjustedMUperlaborunit = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedlaborunits = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        adjustedlaborunits = parseFloat(temp);\n"
				+ "    }\n"
				+ "    adjustedtotalmarkup = adjustedMUperlaborunit * adjustedlaborunits; \n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=adjustedtotalmarkup.toFixed(2);\n"
				+ "    flagDirty(); \n"
	   			;
			s += "}\n"
	   		;
			
			//Recalculate MU using GP percentage:
			s += "function calculateMUusingGPpercentage(){\n"
				+ "    // adjustedtotalmarkup = (adjustedpremarkupcost / (1 - (adjustedGPpercentage/100))) - adjustedpremarkupcost \n"
				+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
				+ "    var adjustedGPpercentage = parseFloat(\"0.00\");\n"
				+ "    var adjustedGPpercentageAsFraction = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + FIELD_ADJUSTED_GP_PERCENTAGE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedGPpercentage = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        adjustedGPpercentage = parseFloat(temp);\n"
				+ "    }\n"
				+ "    adjustedGPpercentageAsFraction = adjustedGPpercentage / 100; \n"
				
				+ "    //Get the total cost before mark-up:\n"
				+ "    var materialcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        materialcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        materialcost = parseFloat(temp);\n"
				+ "    }\n"

				+ "    var adjustedfreightcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedfreightcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedfreightcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var adjustedlaborcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        adjustedlaborcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        adjustedlaborcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var adjustedpremarkupcost = materialcost + adjustedfreightcost + adjustedlaborcost;\n"
				
				+ "    adjustedtotalmarkup = (adjustedpremarkupcost / (1 - (adjustedGPpercentageAsFraction))) - adjustedpremarkupcost; \n"
				
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=adjustedtotalmarkup.toFixed(2);\n"
				+ "    flagDirty();\n"
				
	   			;
			s += "}\n"
	   		;

			//Set all editable fields to their correct decimal formats:
			s += "function formatnumberinputfields(){ \n"
					
				+ "    var fieldvalue = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.0000\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value=formatNumberTo4Places(fieldvalue);\n"
			
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=formatNumber(fieldvalue);\n"
				;
				s += "}\n\n"
			;
			s += "</script>\n";
			return s;
		}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_CAPTION + "\""
				+ " name=\"" + SAVE_BUTTON_CAPTION + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_CAPTION
				+ "</button>\n";
	}
	private String createDeleteButton(){
		String s = "";
		s = "<button type=\"button\""
		+ " value=\"" + DELETE_BUTTON_CAPTION + "\""
		+ " name=\"" + DELETE_BUTTON_CAPTION + "\""
		+ " onClick=\"deletesummary();\">"
		+ DELETE_BUTTON_CAPTION
		+ "</button>\n";
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}