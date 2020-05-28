package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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
import SMDataDefinition.SMTablelabortypes;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smap.APVendor;


public class SMEditSMSummaryEdit extends HttpServlet {
	
	public static final String SAVE_BUTTON_CAPTION = "Save " + SMEstimateSummary.OBJECT_NAME;
	public static final String SAVE_COMMAND_VALUE = "SAVESUMMARY";
	public static final String DELETE_BUTTON_CAPTION = "Delete " + SMEstimateSummary.OBJECT_NAME;
	public static final String DELETE_COMMAND_VALUE = "DELETESUMMARY";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String BUTTON_ADD_MANUAL_ESTIMATE_CAPTION = "Add estimate manually";
	public static final String BUTTON_ADD_MANUAL_ESTIMATE = "ADDMANUALESTIMATE";
	public static final String BUTTON_ADD_VENDOR_QUOTE_CAPTION = "Add vendor quote number:";
	public static final String BUTTON_ADD_VENDOR_QUOTE = "ADDVENDORQUOTE";
	public static final String FIELD_VENDOR_QUOTE = "VENDORQUOTENUMBER";
	public static final String BUTTON_FIND_VENDOR_QUOTE_CAPTION = "Find vendor quote:";
	public static final String BUTTON_FIND_VENDOR_QUOTE = "FINDVENDORQUOTE";
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
	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_COST = "LABELADJUSTEDTOTALMATERIALCOST";
	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
	//public static final String FIELD_ADJUSTED_TOTAL_FREIGHT = "FIELDADJUSTEDTOTALFREIGHT";
	public static final String FIELD_ADJUSTED_TOTAL_FREIGHT_CAPTION = "TOTAL FREIGHT:";
	//public static final String FIELD_ADJUSTED_LABOR_UNITS = "FIELDADJUSTEDLABORUNITS";
	public static final String FIELD_ADJUSTED_LABOR_UNITS_CAPTION = "LABOR UNITS:";
	//public static final String FIELD_ADJUSTED_COST_PER_LABOR_UNIT = "FIELDADJUSTEDCOSTPERLABORUNIT";
	public static final String FIELD_ADJUSTED_COST_PER_LABOR_UNIT_CAPTION = "LABOR COST/UNIT:";
	public static final String LABEL_ADJUSTED_TOTAL_LABOR_COST = "LABELADJUSTEDTOTALLABORCOST";
	public static final String LABEL_ADJUSTED_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
	public static final String LABEL_ADJUSTED_MU_PER_LABOR_UNIT = "LABELADJUSTEDMUPERLABORUNIT";
	public static final String FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION = "MU PER LABOR UNIT:";
	public static final String LABEL_ADJUSTED_MU_PERCENTAGE = "LABELADJUSTEDMUPERCENTAGE";
	public static final String FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION = "MU PERCENTAGE:";
	public static final String LABEL_ADJUSTED_GP_PERCENTAGE = "LABELADJUSTEDGPPERCENTAGE";
	public static final String FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION = "GP PERCENTAGE:";
	//public static final String FIELD_ADJUSTED_TOTAL_MARKUP = "FIELDADJUSTEDTOTALMARKUP";
	public static final String LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL = "LABELADJUSTEDTOTALTAXONMATERIAL";
	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY = "LABELADJUSTEDTOTALFORSUMMARY";
	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION = "ADJUSTED TOTAL FOR ESTIMATE SUMMARY #";
	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX = "LABELADJUSTEDRETAILSALESTAX";
	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
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
		

		SMEstimateSummary summary = new SMEstimateSummary(request);
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

	
	private String getEditHTML(SMMasterEditEntry sm, SMEstimateSummary summary) throws Exception{
		
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
		sControlHTML += "</SELECT>"
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
		
		//Order type:
		//First, add a blank item so we can be sure the user chose one:
		sControlHTML = "<SELECT NAME = \"" + SMTablesmestimatesummaries.iordertype + "\""
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
		
		sControlHTML += "</SELECT>"
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
			+ " VALUE=\"" + summary.getsdescription() + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sdescriptionLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
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
			+ " VALUE=\"" + summary.getsremarks() + "\""
			+ " MAXLENGTH=" + Integer.toString(SMTablesmestimatesummaries.sremarksLength)
			+ " STYLE=\"width: 7in; height: 0.25in\""
			+ ">"
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		//Include an outer table:
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		s += "  <TR>" + "\n";
		s += "    <TD>" + "\n";
		s += buildEstimateTable(conn, summary);
		
		s += buildTotalsTable(summary);
		
		//Close the outer table:
		s += "    </TD>" + "\n";
		s += "  </TR>" + "\n";
		s += "</TABLE>" + "\n";
		

		return s;
	}
	private String buildEstimateTable(Connection conn, SMEstimateSummary summary) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 5;
		
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
			
			//Estimate ID:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" >"
					+ summary.getEstimateArray().get(i).getslid()
					+ "</TD>" + "\n"
				;		
			
			//Remove button:
			s+= "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED + "\" >"
					+ buildRemoveEstimateButton(summary.getEstimateArray().get(i).getslid())
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
			+ buildEstimateButtons()
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
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ "0.00"
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
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ "0.00"  // TODO - fill in this value with javascript
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total tax
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ ">"
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ "0.00"  // TODO - fill in this value with javascript
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
				+ " NAME = \"" + LABEL_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " ID = \"" + LABEL_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = 0.00"
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
				+ " NAME = \"" + LABEL_ADJUSTED_MU_PERCENTAGE + "\""
				+ " ID = \"" + LABEL_ADJUSTED_MU_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = 0.00"
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
				+ " NAME = \"" + LABEL_ADJUSTED_GP_PERCENTAGE + "\""
				+ " ID = \"" + LABEL_ADJUSTED_GP_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = 0.00"
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
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlmarkupamt + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlmarkupamt + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"" + summary.getsbdadjustedlmarkupamt() + "\""
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
			+ "0.00"  // TODO - fill in this value with javascript
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
			+ LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION + " " + sSummaryID + ":"
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
	
	private String buildRemoveEstimateButton(String sEstimateNumber) {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + BUTTON_REMOVE_ESTIMATE_CAPTION + "\""
			+ " name=\"" + BUTTON_REMOVE_ESTIMATE_BASE + "\""
			+ " id=\"" + BUTTON_REMOVE_ESTIMATE_BASE + "\""
			//TODO - involve the estimate number in the removal:
			+ " onClick=\"removestimate();\">"
			+ BUTTON_REMOVE_ESTIMATE_CAPTION
			+ "</button>\n"
		;

		return s;
	}
	
	private String buildEstimateButtons() {
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
				+ " VALUE=\"" + "" + "\""
			    + " SIZE=" + "18"
				+ " MAXLENGTH=" + Integer.toString(SMTablesmestimates.svendorquotenumberLength)
				//+ " ONCHANGE=\"flagDirty();\""
				+ ">" + "\n"
		;
		
		s += "&nbsp;";
		
		s += "<button type=\"button\""
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
			s += "function deletesummary(){\n"
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