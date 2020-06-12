package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;


/**
 * @author tom
 *
 */
public class SMEditSMEstimateEdit extends HttpServlet {
	
	public static final String SAVE_BUTTON_CAPTION = "Save " + SMEstimate.OBJECT_NAME;
	public static final String SAVE_COMMAND_VALUE = "SAVESUMMARY";
	public static final String DELETE_BUTTON_CAPTION = "Delete " + SMEstimate.OBJECT_NAME;
	public static final String DELETE_COMMAND_VALUE = "DELETESUMMARY";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_COST = "LABELCALCULATEDTOTALMATERIALCOST";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL = "LABELCALCULATEDTOTALTAX";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
	public static final String FIELD_FREIGHT_CAPTION = "TOTAL FREIGHT:";
	public static final String FIELD_LABOR_UNITS_CAPTION = "LABOR UNITS:";
	public static final String FIELD_COST_PER_LABOR_UNIT_CAPTION = "LABOR COST/UNIT:";
	public static final String LABEL_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
	public static final String LABEL_TOTAL_LABOR_COST = "TOTOALLABORCOST";
	public static final String FIELD_MU_PER_LABOR_UNIT = "LABELMUPERLABORUNIT";
	public static final String FIELD_MU_PER_LABOR_UNIT_CAPTION = "MU PER LABOR UNIT:";
	public static final String FIELD_MU_PERCENTAGE = "LABELMUPERCENTAGE";
	public static final String FIELD_MU_PERCENTAGE_CAPTION = "MU PERCENTAGE:";
	public static final String FIELD_GP_PERCENTAGE = "LABELGPPERCENTAGE";
	public static final String FIELD_GP_PERCENTAGE_CAPTION = "GP PERCENTAGE:";
	public static final String LABEL_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
	public static final String LABEL_COST_SUBTOTAL_CAPTION = "COST SUBTOTAL:";
	public static final String LABEL_COST_SUBTOTAL = "COSTSUBTOTAL";
	public static final String FIELD_ADDITIONAL_TAXED_COST_CAPTION = "ADDITIONAL COST SUBJECT TO USE TAX:";
	public static final String FIELD_ADDITIONAL_UNTAXED_COST_CAPTION = "ADDITIONAL COST NOT SUBJECT TO USE TAX:";
	public static final String FIELD_LABOR_SELL_PRICE_PER_UNIT_CAPTION = "LABOR SELL PRICE PER UNIT:";
	public static final String LABEL_PRODUCT_UNIT_COST = "LABELPRODUCTUNITCOST";
	
	public static final String LABEL_LABOR_SELL_PRICE_CAPTION = "LABOR SELL PRICE:";
	public static final String LABEL_LABOR_SELL_PRICE = "LABORSELLPRICE";
	public static final String LABEL_MATERIAL_SELL_PRICE_CAPTION = "MATERIAL SELL PRICE:";
	public static final String LABEL_MATERIAL_SELL_PRICE = "MATERIALSELLPRICE";
	public static final String LABEL_TOTAL_SELL_PRICE_CAPTION = "TOTAL SELL PRICE:";
	public static final String LABEL_TOTAL_SELL_PRICE = "TOTALSELLPRICE";
	public static final String LABEL_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
	public static final String LABEL_RETAIL_SALES_TAX = "RETAILSALESTAX";
	public static final String BUTTON_BACK_INTO_PRICE_CAPTION = "Process";
	public static final String BUTTON_BACK_INTO_PRICE = "Process";
	
	public static final String FIELD_BACK_INTO_DESIRED_PRICE = "FIELDBACKINTODESIREDPRICE";
	public static final String LABEL_TOTAL_COST_AND_MARKUP_CAPTION = "TOTAL COST AND MARK-UP:";
	public static final String LABEL_TOTAL_COST_AND_MARKUP = "TOTALCOSTANDMARKUP";
	public static final String REPLACE_VENDOR_QUOTE_BUTTON_CAPTION = "Replace vendor quote";
	public static final String REPLACE_VENDOR_QUOTE_BUTTON = "REPLACEVENDORQUOTEBUTTON";
	public static final String REPLACE_VENDOR_QUOTE_COMMAND = "REPLACEVENDORQUOTECOMMAND";
	public static final String REFRESH_VENDOR_QUOTE_BUTTON_CAPTION = "Refresh vendor quote";
	public static final String REFRESH_VENDOR_QUOTE_BUTTON = "REFRESHVENDORQUOTEBUTTON";
	public static final String REFRESH_VENDOR_QUOTE_COMMAND = "REFRESHVENDORQUOTECOMMAND";
	public static final String FIND_ITEM_BUTTON_CAPTION = "Find item";
	public static final String FIND_ITEM_BUTTON = "FINDITEM";
	public static final String FIND_ITEM_COMMAND = "FINDITEMCOMMAND";
	public static final String REFRESH_ITEM_BUTTON_CAPTION = "Refresh item";
	public static final String REFRESH_ITEM_BUTTON = "REFRESHITEM";
	public static final String REFRESH_ITEM_COMMAND = "REFRESHITEMCOMMAND";
	public static final String REFRESH_ITEM_LINE_NUMBER = "REFRESHITEMLINENUMBER";
	
	public static final String PARAM_FIND_ITEM_RETURN_FIELD = "PARAMFINDITEMRETURNFIELD";
	public static final String LOOKUP_ITEM_COMMAND = "LOOKUPITEMCOMMAND";
	public static final String PARAM_LOOKUP_ITEM_LINENUMBER = "LOOKUPITEMLINENUMBER";
	public static final String RETURNING_FROM_FINDER = "RETURNINGFROMFINDER";
	
	public static final String UNSAVED_ESTIMATE_LABEL = "(UNSAVED)";
	public static final String UNSAVED_SUMMARY_LINE_LABEL = "(UNSAVED)";
	public static final String EMPTY_VENDOR_QUOTE_LABEL = "(NONE)";
	public static final String FIELD_REPLACE_QUOTE_WITH_NUMBER = "PARAMREPLACEQUOTEWITHNUMBER";
	public static final String FIELD_REPLACE_QUOTE_LINE = "PARAMREPLACEQUOTELINE";
	public static final String ESTIMATE_LINE_PREFIX = "ESTLINEPREFIX";
	public static final int ESTIMATE_LINE_NO_PAD_LENGTH = 6;
	
	
	public static final String WARNING_OBJECT = "SMEDITSMSUMMARYWARNINGOBJECT";
	public static final String RESULT_STATUS_OBJECT = "SMEDITSMSUMMARYRESULTSTATUSOBJECT";
	
	//Calculation fields:
	public static final String PARAM_TOTAL_ESTIMATE_MATERIAL_COST = "TOTALESTIMATEMATERIALCOST";
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimate.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditSMEstimateAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);   
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMEstimate estimate = new SMEstimate(request);
		estimate.setslsummarylid(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lsummarylid, request));
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have an estimate object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMEstimate.OBJECT_NAME) != null){
	    	estimate = (SMEstimate) currentSession.getAttribute(SMEstimate.OBJECT_NAME);
	    	currentSession.removeAttribute(SMEstimate.OBJECT_NAME);
			
	    	// No reason to re-load this class if it's being picked up in the session.
	    	//But if we are returning from finding an item, we'll want to update that item value:
	    	//So we'll iterate through the request parameters and see if it includes one of the item number fields:
	    	
	    	for (int iLineCounter = 0; iLineCounter <= estimate.getLineArray().size(); iLineCounter++) {
	    		String sLineItemNumberParam = ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
						Integer.toString(iLineCounter + 1), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber;
	    		String sReturnedItemNumber = clsManageRequestParameters.get_Request_Parameter(sLineItemNumberParam, request);
	    		if (sReturnedItemNumber.compareToIgnoreCase("") != 0){
	    			estimate.getLineArray().get(iLineCounter).setsitemnumber(sReturnedItemNumber);
	    		}
	    	}
	    	
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	try {
					estimate.load(getServletContext(), smedit.getsDBID(), smedit.getUserID());
				} catch (Exception e) {
					//System.out.println("[202006042606] - e = '" + e.getMessage() + "'");
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
						+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
				}
	    	}
	    }
	    
	    //If we're returning from the item finder, then refresh all the lines on the estimate:
	   // System.out.println("[202006122806] - get_Request_Parameter(RETURNING_FROM_FINDER = '" 
	   // 	+ clsManageRequestParameters.get_Request_Parameter(RETURNING_FROM_FINDER, request) + "'");
	    if (clsManageRequestParameters.get_Request_Parameter(RETURNING_FROM_FINDER, request).compareToIgnoreCase("") != 0){
	    	try {
				estimate.refreshAllItems(getServletContext(), smedit.getsDBID(), smedit.getUserID());
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
						+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
			}
	    }
	    
	    //Load the estimate summary, whether it's a new estimate or not:
	    try {
			estimate.loadSummary(getServletContext(), smedit.getsDBID(), smedit.getUserID());
		} catch (Exception e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
					+ "&Warning=" + SMUtilities.URLEncode(e1.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
				return;
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
	    			estimate,
	    			request
	    		), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit,
				estimate
			);
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
				+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
				+ "&Warning=Could not load estimate ID: " + estimate.getslid() + " - " + SMUtilities.URLEncode(sError)
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
			SMEstimate estimate
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
		//Add save button
		pwOut.println("<BR>" + createSaveButton());
		pwOut.println("</FORM>");
	}

	private String getEditHTML(SMMasterEditEntry sm, SMEstimate estimate, HttpServletRequest req) throws Exception{
		
		String s = sCommandScripts(estimate, sm);
		
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
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_FIND_ITEM_RETURN_FIELD + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + PARAM_FIND_ITEM_RETURN_FIELD + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.lid + "\""
				+ " VALUE=\"" + estimate.getslid() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.lid + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.lsummarylid + "\""
				+ " VALUE=\"" + estimate.getslsummarylid() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.lsummarylid + "\""+ "\">" + "\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.lsummarylinenumber + "\""
				+ " VALUE=\"" + estimate.getslsummarylinenumber() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.lsummarylinenumber + "\""+ "\">" + "\n";

		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.lcreatedbyid + "\""
				+ " VALUE=\"" + estimate.getslcreatedbyid() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.lcreatedbyid + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.datetimecreated + "\""
				+ " VALUE=\"" + estimate.getsdatetimecreated() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.datetimecreated + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.screatedbyfullname + "\""
				+ " VALUE=\"" + estimate.getscreatedbyfullname() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.screatedbyfullname + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.ivendorquotelinenumber + "\""
				+ " VALUE=\"" + estimate.getsivendorquotelinenumber() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.ivendorquotelinenumber + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesmestimates.svendorquotenumber + "\""
				+ " VALUE=\"" + estimate.getsvendorquotenumber() + "\""+ " "
				+ " ID=\"" + SMTablesmestimates.svendorquotenumber + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_LOOKUP_ITEM_LINENUMBER + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + PARAM_LOOKUP_ITEM_LINENUMBER + "\""+ "\">" + "\n";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + REFRESH_ITEM_LINE_NUMBER + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + REFRESH_ITEM_LINE_NUMBER + "\""+ "\">" + "\n";
		
		//This is used to store the on-the-fly retail sales tax rate in case the user changes the tax drop down
		s += "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.bdtaxrate + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.bdtaxrate + "\""
			+ " VALUE=\"" + estimate.getsummary().getsbdtaxrate() + "\""
			+ ">" + "\n"
			;
		
		//This is used to calculate taxes on-the-fly
		s += "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\""
			+ " VALUE=\"" + estimate.getsummary().getsicalculatetaxoncustomerinvoice() + "\""
			+ ">" + "\n"
			;
		
		//This is used to calculate taxes on-the-fly
		s += "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\""
			+ " VALUE=\"" + estimate.getsummary().getsicalculatetaxonpurchaseorsale() + "\""
			+ ">" + "\n"
			;
		
		//Include an outer table:
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		s += "  <TR>" + "\n";
		s += "    <TD>" + "\n";
		s += buildSummaryHeaderTable(conn, estimate, sm);
		
		s += buildEstimateHeaderTable(conn, estimate, sm);
		
		String sBackgroundColor = SMUtilities.getInitBackGroundColor(getServletContext(), sm.getsDBID());
		s += buildEstimateLinesTable(conn, estimate, sm, sBackgroundColor, req);
		
		s += buildTotalsTable(estimate);
		
		s += printBackIntoControls();
		
		//Close the outer table:
		s += "    </TD>" + "\n";
		s += "  </TR>" + "\n";
		s += "</TABLE>" + "\n";
		
		return s;
	}

	private String buildEstimateLinesTable(
		Connection conn, 
		SMEstimate estimate,
		SMMasterEditEntry sm,
		String sBackgroundColor,
		HttpServletRequest req
		) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 8;
		
		s += "<BR>";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" style = \" width:100%; \" >" + "\n";
		
		s += "  <TR>" + "\n";
		
		//Qty:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Quantity"
			+ "</TD>" + "\n"
		;
		
		//Item number:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Item #"
			+ "</TD>" + "\n"
		;
		
		//Blank column for item finder on options:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ ""
			+ "</TD>" + "\n"
		;
		
		//Blank column for item refresh on options:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ ""
			+ "</TD>" + "\n"
		;
		
		//Product description:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Product description"
			+ "</TD>" + "\n"
		;
		
		//U/M:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "U/M"
			+ "</TD>" + "\n"
		;
		
		//Unit cost:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Unit cost"
			+ "</TD>" + "\n"
		;
		
		//Extended cost:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Extended cost"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>" + "\n";
		
		String sReadOnlyValue = "";
		String sFieldBackgroundColorStyle = "";
		
		if (estimate.getsvendorquotenumber().compareToIgnoreCase("") != 0) {
			sReadOnlyValue = "readonly";
			sFieldBackgroundColorStyle = " background-color: " + sBackgroundColor + "; ";
		}
		//If it's a vendor quote, then we don't let the user change the qty, item, U/M, or costs:
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.bdquantity + "\""
			+ " ID=\"" + SMTablesmestimates.bdquantity + "\""
			+ " VALUE=\"" + estimate.getsbdquantity() + "\""
			+ " MAXLENGTH=15"
			+ " style = \" text-align:right; width:65px;" + sFieldBackgroundColorStyle + "\""
			+ " " + sReadOnlyValue
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.sitemnumber + "\""
			+ " ID=\"" + SMTablesmestimates.sitemnumber + "\""
			+ " VALUE=\"" + estimate.getsitemnumber() + "\""
			+ " MAXLENGTH=32"
			+ " style = \" width:100px;" + sFieldBackgroundColorStyle + "\""
			+ " " + sReadOnlyValue
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		//Blank column for item finder:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; font-style:underline; \" >"
				+ ""
				+ "</TD>" + "\n"
			;
		
		//Blank column for item refresh:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; font-style:underline; \" >"
				+ ""
				+ "</TD>" + "\n"
			;
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.sproductdescription + "\""
			+ " ID=\"" + SMTablesmestimates.sproductdescription + "\""
			+ " VALUE=\"" + estimate.getsproductdescription().replace("\"", "&quot;") + "\""
			+ " MAXLENGTH=32"
			+ " style = \" width:500px;\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.sunitofmeasure + "\""
			+ " ID=\"" + SMTablesmestimates.sunitofmeasure + "\""
			+ " VALUE=\"" + estimate.getsunitofmeasure().replace("\"", "&quot;") + "\""
			+ " MAXLENGTH=32"
			+ " style = \" width:50px;" + sFieldBackgroundColorStyle + "\""
			+ " " + sReadOnlyValue
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
				+ ">"
				+ "<LABEL"
				+ " NAME=\"" + LABEL_PRODUCT_UNIT_COST + "\""
				+ " ID=\"" + LABEL_PRODUCT_UNIT_COST + "\""
				+ " MAXLENGTH=32"
				+ " style = \" text-align:right; width:70px;" + sFieldBackgroundColorStyle + "\""
				+ ">"
				+ "0.00"
				+ "</LABEL>"
				+ "</TD>" + "\n"
			;
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.bdextendedcost + "\""
			+ " ID=\"" + SMTablesmestimates.bdextendedcost + "\""
			+ " VALUE=\"" + estimate.getsbdextendedcost() + "\""
			+ " MAXLENGTH=32"
			+ " style = \" text-align:right; width:80px;" + sFieldBackgroundColorStyle + "\""
			+ " " + sReadOnlyValue
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
				+ " style = \" font-weight:bold; font-style:underline; \" >"
				+ "OPTIONS"
				+ "</TD>" + "\n"
			;
		
		s += "  </TR>" + "\n";
		
		//List all the saved lines here:
		
		//We'll use this to figure out if the user couldn't save the last line, so we can just redisplay it as the last line, and
		// not add a new BLANK line at the bottom:
		boolean bLastLineWasNotSaved = false;
		for (int iEstimateLineCounter = 0; iEstimateLineCounter < estimate.getLineArray().size(); iEstimateLineCounter++) {
			//Display each line:
			
			int iLineNumber = iEstimateLineCounter + 1;
			if (
				(estimate.getLineArray().get(iEstimateLineCounter).getslid().compareToIgnoreCase("-1") == 0)
			) {
				bLastLineWasNotSaved = true;
				//Here we'll set the line number to ZERO so this will be our 'new' line:
				iLineNumber = 0;
			}
			
			//System.out.println("[202006125051] - array index = " + iEstimateLineCounter + ", getslid() = '" 
			//	+ estimate.getLineArray().get(iEstimateLineCounter).getslid() + "', bLastLineWasNotSaved = " + bLastLineWasNotSaved
			//	+ ", iLineNumber = " + iLineNumber
			//);
			
			s += "  <TR>" + "\n";
			s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsbdquantity() + "\""
					+ " MAXLENGTH=15"
					+ " style = \" text-align:right; width:65px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					
					//Store the line ID in a hidden field:
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.lid + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.lid + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getslid() + "\""
					+ ">"
					+ "</TD>" + "\n"
				;
			
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsitemnumber() + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:100px;\""
					+ " onchange=\"lookUpItem('" + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + "');\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				//Refresh item info
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
						+ " style = \" font-weight:bold; font-style:underline; \" >"
						+ ""
						+ buildItemRefreshButton(Integer.toString(iLineNumber))
						+ "</TD>" + "\n"
					;
				
				//Column to find item:
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
						+ " style = \" font-weight:bold; font-style:underline; \" >"
						+ ""
						+ buildItemFinderButton(Integer.toString(iLineNumber))
						+ "</TD>" + "\n"
					;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getslinedescription().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:500px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsunitofmeasure().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:50px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
						+ ">"
						+ "<INPUT TYPE=TEXT"
						+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
								Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdunitcost + "\""
						+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
								Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdunitcost + "\""
						+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsbdunitcost() + "\""
						+ " MAXLENGTH=32"
						+ " style = \" text-align:right; width:70px;\""
						+ " onchange=\"flagDirty();\""
						+ ">"
						+ "</TD>" + "\n"
					;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iLineNumber), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsbdextendedcost() + "\""
					+ " MAXLENGTH=32"
					+ " style = \" text-align:right; width:80px; background-color: " + sBackgroundColor + "; \""
					+ " onchange=\"flagDirty();\""
					+ " readonly "
					+ ">"
					+ "</TD>" + "\n"
				;
				s += "  </TR>" + "\n";
		}
		
		//Now add one blank line for new entries:
		// We only need a new blank line if all the previous lines were already saved - otherwise, we'll just
		// be using the last, unsaved line as our 'new' line.
		
		//So if the last line was NOT NOT saved - meaning if it WAS saved, then we'll add a new blank line:
		if(!bLastLineWasNotSaved) {
			s += "  <TR>" + "\n";
			s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
						"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " VALUE=\"" + "0.0000" + "\""
					+ " MAXLENGTH=15"
					+ " style = \" text-align:right; width:65px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					
					//Store the line ID in a hidden field:
					+ "<INPUT TYPE=HIDDEN"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.lid + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.lid + "\""
					+ " VALUE=\"" + "-1" + "\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				//If there's an item number being returned by the finder, insert that into this line:
				String sItemNumber = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
						ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
								"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber,
					req);
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " VALUE=\"" + sItemNumber + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:100px;\""
					+ " onchange=\"lookUpItem('" + clsStringFunctions.PadLeft("0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + "');\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
						+ " style = \" font-weight:bold; font-style:underline; \" >"
						+ ""
						+ "</TD>" + "\n"
					;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
						+ " style = \" font-weight:bold; font-style:underline; \" >"
						+ buildItemFinderButton("0")
						+ "</TD>" + "\n"
					;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " VALUE=\"" + "" + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:500px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " VALUE=\"" + "" + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:50px;\""
					+ " onchange=\"flagDirty();\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
						+ ">"
						+ "<INPUT TYPE=TEXT"
						+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
								"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdunitcost + "\""
						+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
								"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdunitcost + "\""
						+ " VALUE=\"" + "0.00" + "\""
						+ " MAXLENGTH=32"
						+ " style = \" text-align:right; width:70px;\""
						+ " onchange=\"flagDirty();\""
						+ ">"
						+ "</TD>" + "\n"
					;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							"0", "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " VALUE=\"" + "0.00" + "\""
					+ " MAXLENGTH=32"
					+ " style = \" text-align:right; width:80px; background-color: " + sBackgroundColor + "; \""
					+ " onchange=\"flagDirty();\""
					+ " readonly "
					+ ">"
					+ "</TD>" + "\n"
				;
				s += "  </TR>" + "\n";
		}
		
		s += "</TABLE>" + "\n";
		
		return s;
	}
	private String buildEstimateHeaderTable(
		Connection conn, 
		SMEstimate estimate,
		SMMasterEditEntry sm
		) throws Exception{
		String s = "";
		
		String sEstimateID = UNSAVED_ESTIMATE_LABEL;
		if (
			(estimate.getslid().compareToIgnoreCase("-1") != 0)
			&& (estimate.getslid().compareToIgnoreCase("0") != 0)
			&& (estimate.getslid().compareToIgnoreCase("") != 0)
				
		) {
			sEstimateID = estimate.getslid();
		}
		
		s += "<BR>" + "\n";
		
		String sSummaryLineNumber = UNSAVED_SUMMARY_LINE_LABEL;
		if (estimate.getslsummarylinenumber().compareToIgnoreCase("-1") != 0) {
			sSummaryLineNumber = estimate.getslsummarylinenumber();
		}
		s += "<B>Estimate ID:</B>&nbsp;" + sEstimateID
			+ "&nbsp;&nbsp;"
			+ "<B>Summary line #:</B>&nbsp;" + sSummaryLineNumber
			+ "&nbsp;&nbsp;"
			+ "<B>Created:</B> " + estimate.getsdatetimecreated() + " by " + estimate.getscreatedbyfullname() 
			+ "&nbsp;&nbsp;"
			+ "Last modified " + estimate.getsdatetimelastmodified() + " by " + estimate.getslastmodifiedbyfullname()
			 + "\n"
		;
		
		s += "<BR>" + "\n";
		
		s += "<B>Insert as prefix label using item #:</B>&nbsp;" 
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.sprefixlabelitem + "\""
			+ " ID = \"" + SMTablesmestimates.sprefixlabelitem + "\""
			+ " style = \" text-align:left; width:100px;\""
			+ " VALUE = \"" + estimate.getsprefixlabelitem().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			 + "\n"
			+ "&nbsp;&nbsp;"
			+ "<B>Estimate description:</B>&nbsp;" 
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.sdescription + "\""
			+ " ID = \"" + SMTablesmestimates.sdescription + "\""
			+ " style = \" text-align:left; width:600px;\""
			+ " VALUE = \"" + estimate.getsdescription().replace("\"", "&quot;") + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			+ "\n"
			
		;
		
		s += "<BR>" + "\n";
		
		String sVendorQuoteLink = estimate.getsvendorquotenumber();
		if (sVendorQuoteLink.compareToIgnoreCase("") != 0) {
			if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMOHDirectQuoteList,
				sm.getUserID(), 
				conn, 
				sm.getLicenseModuleLevel())) {
				
				//Create a link to the vendor's quote:
				sVendorQuoteLink = 
			    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOHDirectQuote"
			    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    		+ "&" + "C_QuoteNumberString=" + sVendorQuoteLink
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
			    		+ "\">" + sVendorQuoteLink + "</A>"
			    		+ ", line #" + estimate.getsivendorquotelinenumber()
				;
				
			}
		}else {
			sVendorQuoteLink = EMPTY_VENDOR_QUOTE_LABEL;
		}
		
		s += "<B>Vendor quote #:</B>&nbsp;" + sVendorQuoteLink + "\n";
		
		s += "<BR>" + "\n";
		
		s += createRefreshVendorQuoteLineButton()
			+ "&nbsp;&nbsp;"
			+ createReplaceVendorQuoteLineButton()
			+ " with vendor quote #:&nbsp;"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + FIELD_REPLACE_QUOTE_WITH_NUMBER + "\""
			+ " ID = \"" + FIELD_REPLACE_QUOTE_WITH_NUMBER + "\""
			+ " style = \" text-align:left; width:150px;\""
			+ " VALUE = \"" + "" + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>" + "\n"
			+ ", line number:&nbsp;"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + FIELD_REPLACE_QUOTE_LINE + "\""
			+ " ID = \"" + FIELD_REPLACE_QUOTE_LINE + "\""
			+ " style = \" text-align:left; width:60px;\""
			+ " VALUE = \"" + "" + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>" + "\n"
		;
		
		s += "<BR>" + "\n";
		;

		return s;
	}
	private String buildSummaryHeaderTable(Connection conn, SMEstimate estimate, SMMasterEditEntry sm) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 4;
		
		//Get the summary:
		SMEstimateSummary summary = new SMEstimateSummary();
		summary.setslid(estimate.getslsummarylid());
		try {
			summary.load(conn);
		}catch (Exception e) {
			throw new Exception("Error [202006040000] - loading summary with ID '" + summary.getslid() + " - " + e.getMessage());
		}
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" style = \" width:100%; \" >" + "\n";
		
		s += "  <TR>" + "\n";
		
		//Summary ID:
		String sSummaryLink = 
		    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMSummaryEdit"
			    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    		+ "&" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
			    		+ "\">" + "Estimate Summary #: " + estimate.getslsummarylid() + "</A>"
				;
				
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ sSummaryLink
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ ""
			+ "</TD>" + "\n"
		;
		
		//Project:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Project:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ summary.getsjobname()
			+ "</TD>" + "\n"
		;

		s += "  <TR>" + "\n";
		
		//Sales lead
		String sSalesLeadLink = summary.getslsalesleadid();
		if (summary.getslsalesleadid().compareToIgnoreCase("") != 0) {
			boolean bAllowEditSalesLeads = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditBids, sm.getUserID(), conn, sm.getLicenseModuleLevel());
			if (bAllowEditSalesLeads) {
				sSalesLeadLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
			    	+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    	+ "&" + SMBidEntry.ParamID + "=" + summary.getslsalesleadid()
			    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			    	+ "\">" + sSalesLeadLink + "</A>"
				;
			}
		}
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "Sales lead #:"
				+ "</TD>" + "\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ sSalesLeadLink
				+ "</TD>" + "\n"
			;
		
		// Labor type
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "Labor type:"
				+ "</TD>" + "\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ summary.getslabortypedescription(conn)
				+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";
		
		s += "  <TR>" + "\n";
		
		//Tax type:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Tax type:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ summary.getstaxdescription()
			+ "</TD>" + "\n"
		;
		
		//Order type:
		String sOrderTypeDescription = SMTableorderheaders.getOrderTypeDescriptions(Integer.parseInt(summary.getsiordertype()));
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Order type:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ sOrderTypeDescription
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";

		//Description:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1)
			+ "\" >"
			+ "Description:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ summary.getsdescription()
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Remarks:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1)
			+ "\" >"
			+ "Remarks:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ summary.getsremarks()
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		return s;
		
	}
	
	private String buildTotalsTable(SMEstimate estimate) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 6;

		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" style = \" width:100%; \" >" + "\n";
		
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
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//total freight
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ FIELD_FREIGHT_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdfreight + "\""
			+ " ID = \"" + SMTablesmestimates.bdfreight + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdfreight() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";

		s += "  <TR>" + "\n";
		
		//Labor units
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 2) + " >"
			+ FIELD_LABOR_UNITS_CAPTION
			+ "&nbsp;"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdlaborquantity + "\""
			+ " ID = \"" + SMTablesmestimates.bdlaborquantity + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdlaborquantity() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			+ "&nbsp;"
			+ FIELD_COST_PER_LABOR_UNIT_CAPTION
			+ "&nbsp;"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdlaborcostperunit + "\""
			+ " ID = \"" + SMTablesmestimates.bdlaborcostperunit + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdlaborcostperunit() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		
		//Total labor cost
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ LABEL_TOTAL_LABOR_COST_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_TOTAL_LABOR_COST + "\""
			+ " ID = \"" + LABEL_TOTAL_LABOR_COST + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Additional cost subject to use tax:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ FIELD_ADDITIONAL_TAXED_COST_CAPTION
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.sadditionalpretaxcostlabel + "\""
			+ " ID = \"" + SMTablesmestimates.sadditionalpretaxcostlabel + "\""
			+ " style = \" text-align:right; width:200px;\""
			+ " VALUE = \"" + estimate.getsadditionalpretaxcostlabel() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\""
			+ " ID = \"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdadditionalpretaxcostamount() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Cost subtotal:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_COST_SUBTOTAL_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_COST_SUBTOTAL + "\""
			+ " ID = \"" + LABEL_COST_SUBTOTAL + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//MU per labor unit
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ FIELD_MU_PER_LABOR_UNIT_CAPTION
				+ "&nbsp;"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_MU_PER_LABOR_UNIT + "\""
				+ " ID = \"" + FIELD_MU_PER_LABOR_UNIT + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = 0.00"
				+ " onchange=\"calculateMUusingMUperlaborunit();\""
				+ ">"
				+ "</INPUT>"
				
				//MU Pctge
				+ "&nbsp;"
				+ FIELD_MU_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "&nbsp;"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_MU_PERCENTAGE + "\""
				+ " ID = \"" + FIELD_MU_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"0.00\""
				+ " onchange=\"calculateMUusingMUpercentage();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		
		//GP percentage
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ FIELD_GP_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + FIELD_GP_PERCENTAGE + "\""
				+ " ID = \"" + FIELD_GP_PERCENTAGE + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"0.00\""
				+ " onchange=\"calculateMUusingGPpercentage();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		
		//Total MU
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ LABEL_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<INPUT TYPE=TEXT"
				+ " NAME = \"" + SMTablesmestimates.bdmarkupamount + "\""
				+ " ID = \"" + SMTablesmestimates.bdmarkupamount + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"" + estimate.getsbdmarkupamount() + "\""
				+ " onchange=\"flagDirty();\""
				+ ">"
				+ "</INPUT>"
				
				+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";
		
		//total tax
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ estimate.getsummary().getstaxdescription() + " "
			+ estimate.getsummary().getsbdtaxrate() + "% "
			+  LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION
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
		
		//total cost and mark-up:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_TOTAL_COST_AND_MARKUP_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_TOTAL_COST_AND_MARKUP + "\""
			+ " ID = \"" + LABEL_TOTAL_COST_AND_MARKUP + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Additional cost NOT subject to use tax:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ FIELD_ADDITIONAL_UNTAXED_COST_CAPTION
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.sadditionalposttaxcostlabel + "\""
			+ " ID = \"" + SMTablesmestimates.sadditionalposttaxcostlabel + "\""
			+ " style = \" text-align:right; width:200px;\""
			+ " VALUE = \"" + estimate.getsadditionalposttaxcostlabel() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\""
			+ " ID = \"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdadditionalposttaxcostamount() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Labor sell price per unit:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ FIELD_LABOR_SELL_PRICE_PER_UNIT_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdlaborsellpriceperunit + "\""
			+ " ID = \"" + SMTablesmestimates.bdlaborsellpriceperunit + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdlaborsellpriceperunit() + "\""
			+ " onchange=\"flagDirty();\""
			+ ">"
			+ "</INPUT>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Labor sell price
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_LABOR_SELL_PRICE_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_LABOR_SELL_PRICE + "\""
			+ " ID = \"" + LABEL_LABOR_SELL_PRICE + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Material sell price
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_MATERIAL_SELL_PRICE_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_MATERIAL_SELL_PRICE + "\""
			+ " ID = \"" + LABEL_MATERIAL_SELL_PRICE + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Total sell price
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_TOTAL_SELL_PRICE_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_TOTAL_SELL_PRICE + "\""
			+ " ID = \"" + LABEL_TOTAL_SELL_PRICE + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		//Retail sales tax
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
			+ LABEL_RETAIL_SALES_TAX_CAPTION
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + LABEL_RETAIL_SALES_TAX + "\""
			+ " ID = \"" + LABEL_RETAIL_SALES_TAX + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"
			
			+ "</TD>" + "\n"
		;
		s += "  </TR>" + "\n";
		
		s += "</TABLE>" + "\n";
		
		return s;
	}
	
	private String buildItemFinderButton(String sLineNumber) {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + FIND_ITEM_BUTTON_CAPTION + "\""
			+ " name=\"" + FIND_ITEM_BUTTON + "\""
			+ " id=\"" + FIND_ITEM_BUTTON + "\""
			+ " onClick=\"invokeitemfinder('" 
				+ ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
					sLineNumber.trim(), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber
			+ "');\">"
			+ FIND_ITEM_BUTTON_CAPTION
			+ "</button>\n"
		;

		return s;
	}
	private String buildItemRefreshButton(String sPaddedLineNumber) {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + REFRESH_ITEM_BUTTON_CAPTION + "\""
			+ " name=\"" + REFRESH_ITEM_BUTTON + sPaddedLineNumber + "\""
			+ " id=\"" + REFRESH_ITEM_BUTTON + sPaddedLineNumber + "\""
			+ " onClick=\"invokeitemrefresh('"  + sPaddedLineNumber + "');\">"
			+ REFRESH_ITEM_BUTTON_CAPTION
			+ "</button>\n"
		;

		return s;
	}
	
	private String createReplaceVendorQuoteLineButton() {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + REPLACE_VENDOR_QUOTE_BUTTON_CAPTION + "\""
			+ " name=\"" + REPLACE_VENDOR_QUOTE_BUTTON + "\""
			+ " id=\"" + REPLACE_VENDOR_QUOTE_BUTTON + "\""
			+ " onClick=\"replacevendorquote();\">"
			+ REPLACE_VENDOR_QUOTE_BUTTON_CAPTION
			+ "</button>\n"
		;
		return s;
	}
	
	private String createRefreshVendorQuoteLineButton() {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + REFRESH_VENDOR_QUOTE_BUTTON_CAPTION + "\""
			+ " name=\"" + REFRESH_VENDOR_QUOTE_BUTTON + "\""
			+ " id=\"" + REFRESH_VENDOR_QUOTE_BUTTON + "\""
			+ " onClick=\"refreshvendorquote();\">"
			+ REFRESH_VENDOR_QUOTE_BUTTON_CAPTION
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
	private String printBackIntoControls() {
		String s = "";
		
		s += "<BR><B>'Back Into' price:</B> Adjust mark-up to bring total selling price to: "
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

	private String sCommandScripts(
			SMEstimate estimate, 
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


			s += "\n";
			
			s += "function triggerinitiation(){\n"		
				+ "    recalculatelivetotals();\n"
				//+ "    checkfortaxupdates();\n"
				+ "}\n\n"
			;
			
			s += "function promptToSave(){\n"		
				
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "        if (\n"
				+ "            (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + SAVE_COMMAND_VALUE + "\") \n"
				+ "            && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + DELETE_COMMAND_VALUE + "\") \n"
				+ "            && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + LOOKUP_ITEM_COMMAND + "\") \n"
				+ "            && (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + REFRESH_ITEM_COMMAND + "\") \n"
				+ "        ) {\n"
				+ "            return 'You have unsaved changes!';\n"
				+ "        } \n"
				+ "    }\n"
				+ "}\n\n"
			;
			
			//Replace vendor quote:
			s += "function replacevendorquote(){\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REPLACE_VENDOR_QUOTE_COMMAND + "\";\n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Refresh vendor quote:
			s += "function refreshvendorquote(){\n"
					//+ "    alert('refreshing vendor quote'); \n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REFRESH_VENDOR_QUOTE_COMMAND + "\";\n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Find item for estimate option:
			s += "function invokeitemfinder(sItemFinderResultField){\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + FIND_ITEM_COMMAND + "\";\n"
					+ "    document.getElementById(\"" + PARAM_FIND_ITEM_RETURN_FIELD + "\").value = sItemFinderResultField; \n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Refresh item for estimate option:
			s += "function invokeitemrefresh(slinenumber){\n"
					//+ "    alert('Refresh line ' + slinenumber); \n"
					+ "    // First, record that the record is being changed: \n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + REFRESH_ITEM_COMMAND + "\";\n"
					+ "    document.getElementById(\"" + REFRESH_ITEM_LINE_NUMBER + "\").value = slinenumber; \n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Get item info when a new item number is entered:
			s += "function lookUpItem(slinenumber){\n"
					+ "    //If there's no qty on the line, then just turn around right now: \n"
					+ "    var lineqty = parseFloat(\"0.0000\");\n"
					+ "    var temp = (document.getElementById(" 
						+ "'" + ESTIMATE_LINE_PREFIX + "' + slinenumber + '" + SMTablesmestimatelines.bdquantity + "'"
						+ ").value).replace(',','');\n"
					+ "    if (temp == ''){\n"
					+ "        lineqty = parseFloat(\"0.00\");\n"
					+ "    }else{\n"
					+ "        lineqty = parseFloat(temp);\n"
					+ "    }\n"
					+ "    \n"
					+ "    if (compare4DecimalPlaceFloats(lineqty, 0.0000)){ \n"
					+ "      alert('The item information on this line cannot be updated unless there is a quantity on the line.'); \n"
					+ "      document.getElementById('" + ESTIMATE_LINE_PREFIX + "' + slinenumber + '" + SMTablesmestimatelines.sitemnumber + "').value = ''; \n"
					+ "      document.getElementById('" + ESTIMATE_LINE_PREFIX + "' + slinenumber + '" + SMTablesmestimatelines.bdquantity + "').focus; \n"
					+ "      return; \n"
					+ "    } \n"
					
					+ "    // First, record that the record is being changed: \n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + LOOKUP_ITEM_COMMAND + "\";\n"
					+ "    document.getElementById(\"" + PARAM_LOOKUP_ITEM_LINENUMBER + "\").value = slinenumber; \n"
					+ "    document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			s += "function backintoprice(){\n"
					
				//+ "    alert('Back into'); \n"	
				+ "    var currenttotalprice = parseFloat(\"0.00\");\n"
				+ "    var desiredtotalprice = parseFloat(\"0.00\");\n"
				+ "    var currentmarkup = parseFloat(\"0.00\");\n"
				+ "    var requiredmarkup = parseFloat(\"0.00\");\n"
				+ "    var desireddifference = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + LABEL_TOTAL_SELL_PRICE + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        currenttotalprice = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        currenttotalprice = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    var temp = (document.getElementById(\"" + FIELD_BACK_INTO_DESIRED_PRICE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        desiredtotalprice = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        desiredtotalprice = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    desireddifference = desiredtotalprice - currenttotalprice; \n"

				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        currentmarkup = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        currentmarkup = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    requiredmarkup = currentmarkup + desireddifference; \n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value=formatNumber(requiredmarkup);\n"
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
				+ "    var materialcosttotal = parseFloat(\"0.00\");\n"
				
				+ "    //Get the product cost: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdextendedcost + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        materialcosttotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        materialcosttotal = parseFloat(temp);\n"
				+ "    }\n"
				+ "    \n"
				
				+ "    //Extend the qty X the unit cost for all the lines: \n"
				+ "    //Get the line costs: \n"
				+ "    for (i=0; i<document.forms[0].length; i++){\n"
				+ "        ctl = document.forms[0].elements[i];\n"
				+ "        if (ctl.name.indexOf('" + ESTIMATE_LINE_PREFIX + "') >= 0 ){ \n"
				+ "            if (ctl.name.indexOf('" + SMTablesmestimatelines.bdquantity + "') >= 0 ){ \n"
				+ "                var lineqty = parseFloat(\"0.0000\"); \n"
				+ "                var lineunitcost = parseFloat(\"0.00\"); \n"
				+ "                var lineextendedcost = parseFloat(\"0.00\"); \n"
				+ "                var lineunitcostfieldname = ctl.name.replace('" + SMTablesmestimatelines.bdquantity + "', '" + SMTablesmestimatelines.bdunitcost + "');\n"
				+ "                var lineextendedcostfieldname = ctl.name.replace('" + SMTablesmestimatelines.bdquantity + "', '" + SMTablesmestimatelines.bdextendedcost + "');\n"
				+ "                var temp = (ctl.value).replace(',','');\n"
				+ "                if (temp == ''){\n"
				+ "                    lineqty = parseFloat(\"0.00\");\n"
				+ "                }else{\n"
				+ "                    lineqty = parseFloat(temp);\n"
				+ "                }\n"
				+ "                \n"
				+ "                var temp = (document.getElementById(\"\" + lineunitcostfieldname + \"\").value).replace(',','');\n"
				+ "                if (temp == ''){\n"
				+ "                    lineunitcost = parseFloat(\"0.00\");\n"
				+ "                }else{\n"
				+ "                    lineunitcost = parseFloat(temp);\n"
				+ "                }\n"
				+ "                \n"
				+ "                lineextendedcost = lineqty * lineunitcost; \n"
				+ "                document.getElementById(\"\" + lineextendedcostfieldname + \"\").value = formatNumber(lineextendedcost); \n"
				
				+ "            } \n"
				+ "        } \n"
				
				+ "    } \n"
				
				
				+ "\n"
				+ "    //Get the line costs: \n"
				+ "    for (i=0; i<document.forms[0].length; i++){\n"
				+ "        ctl = document.forms[0].elements[i];\n"
				+ "        if (ctl.name.indexOf('" + ESTIMATE_LINE_PREFIX + "') >= 0 ){ \n"
				+ "            if (ctl.name.indexOf('" + SMTablesmestimatelines.bdextendedcost + "') >= 0 ){ \n"
				+ "                var temp = (ctl.value).replace(',','');\n"
				+ "                if (temp == ''){\n"
				+ "                    materialcosttotal = materialcosttotal + parseFloat(\"0.00\");\n"
				+ "                }else{\n"
				+ "                    materialcosttotal = materialcosttotal + parseFloat(temp);\n"
				+ "                }\n"
				+ "            } \n"
				+ "        } \n"
				
				+ "    } \n"
				+ "    document.getElementById(\"" + LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\").innerText=formatNumber(materialcosttotal);\n\n"
			
				+ "    //Calculate labor \n"
				+ "    var laborunits = parseFloat(\"0.00\");\n"
				+ "    var laborcostperunit = parseFloat(\"0.00\");\n"
				+ "    var laborcosttotal = parseFloat(\"0.00\");\n"
			
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborquantity + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        laborunits = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        laborunits = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborcostperunit + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        laborcostperunit = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        laborcostperunit = parseFloat(temp);\n"
				+ "    }\n"
				+ "    laborcosttotal = laborunits * laborcostperunit; \n"
				+ "    document.getElementById(\"" + LABEL_TOTAL_LABOR_COST + "\").innerText=formatNumber(laborcosttotal);\n\n"

				+ "    var costsubtotal = parseFloat(\"0.00\");\n"
				+ "    var additionalpretaxcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        additionalpretaxcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        additionalpretaxcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var freight = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdfreight + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        freight = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        freight = parseFloat(temp);\n"
				+ "    }\n"

				+ "    costsubtotal = materialcosttotal + freight + laborcosttotal + additionalpretaxcost; \n"
				+ "    document.getElementById(\"" + LABEL_COST_SUBTOTAL + "\").innerText=formatNumber(costsubtotal);\n\n"
				
				+ "    // Mark-up calculations: \n"
				+ "    //Calculate the MU per labor unit, as a percentage and as a GP percentage: \n"
				+ "    var markupamount = parseFloat(\"0.00\");\n"
				+ "    var markupperlaborunit = parseFloat(\"0.00\");\n"
				+ "    //Get the markup amount: \n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        markupamount = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        markupamount = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    if(compare2DecimalPlaceFloats(laborunits, parseFloat(\"0.00\"))){ \n"
				+ "        markupperlaborunit = markupamount; \n"
				+ "    } else { \n"
				+ "        markupperlaborunit = markupamount / laborunits; \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + FIELD_MU_PER_LABOR_UNIT + "\").value=formatNumber(markupperlaborunit);\n"
				+ "    \n\n"
				
				+ "    var markuppercentage = parseFloat(\"0.00\");\n"
				
				+ "    if(compare2DecimalPlaceFloats(laborunits, parseFloat(\"0.00\"))){ \n"
				+ "        markuppercentage = \"\"; \n"
				+ "    } else { \n"
				+ "        markuppercentage = (markupamount / costsubtotal) * 100; \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + FIELD_MU_PERCENTAGE + "\").value=formatNumber(markuppercentage);\n\n"
				
				+ "    var taxonmaterial = parseFloat(\"0.00\");\n"
				+ "    var taxrateaspercentage = parseFloat(\"0.00\");\n"
				+ "    var icalculatetaxoncustomerinvoice = parseFloat(\"0.00\");\n"
				+ "    var icalculatetaxonpurchaseorsale = parseFloat(\"0.00\");\n"
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

				+ "    var totalcostandmarkup = parseFloat(\"0.00\");\n"
				+ "    totalcostandmarkup = costsubtotal + markupamount + taxonmaterial; \n"
				+ "    document.getElementById(\"" + LABEL_TOTAL_COST_AND_MARKUP + "\").innerText=formatNumber(totalcostandmarkup);\n"
				+ "    \n\n"
				
				+ "    var laborsellprice = parseFloat(\"0.00\");\n"
				+ "    var laborsellpriceperunit = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborsellpriceperunit + "\").value);\n"
				+ "    if (temp == ''){\n"
				+ "        laborsellpriceperunit = parseFloat(\"0\");\n"
				+ "    }else{\n"
				+ "        laborsellpriceperunit = parseFloat(temp); \n"
				+ "    }\n"
				+ "    laborsellprice = laborunits * laborsellpriceperunit; \n"
				+ "    document.getElementById(\"" + LABEL_LABOR_SELL_PRICE + "\").innerText=formatNumber(laborsellprice);\n"
				+ "    \n"
				
				+ "    var additionalposttaxcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        additionalposttaxcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        additionalposttaxcost = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var totalsellprice = parseFloat(\"0.00\");\n"
				+ "    totalsellprice = totalcostandmarkup + additionalposttaxcost; \n"
				+ "    document.getElementById(\"" + LABEL_TOTAL_SELL_PRICE + "\").innerText=formatNumber(totalsellprice);\n"
				+ "    \n"
				
				+ "    var materialsellprice = parseFloat(\"0.00\");\n"
				+ "    materialsellprice = totalsellprice - laborsellprice; \n"
				+ "    document.getElementById(\"" + LABEL_MATERIAL_SELL_PRICE + "\").innerText=formatNumber(materialsellprice);\n"
				+ "    \n"
				
				+ "    //Get the retail sales tax for the estimate: \n"
				+ "    var retailsalestaxamount = parseFloat(\"0.00\");\n"
				+ "    if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
							+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
				+ "        retailsalestaxamount = parseFloat((totalsellprice * (taxrateaspercentage / 100)).toFixed(2)); \n"
				+ "    }else{ \n"
				+ "        retailsalestaxamount = parseFloat(\"0.00\"); \n"
				+ "    } \n"
				+ "    document.getElementById(\"" + LABEL_RETAIL_SALES_TAX + "\").innerText=formatNumber(retailsalestaxamount);\n"
				+ "    \n"
				
				+ "    var gppercentage = parseFloat(\"0.00\");\n"
				+ "    gppercentage = markupamount / totalsellprice; \n"
				+ "    gppercentage = gppercentage * 100; \n"
				+ "    document.getElementById(\"" + FIELD_GP_PERCENTAGE + "\").value=formatNumber(gppercentage);\n"
				+ "    \n"
				
			;
			s += "}\n"
	   		;
			
			//Set all editable fields to their correct decimal formats:
			s += "function formatnumberinputfields(){ \n"
					
				+ "    var fieldvalue = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdextendedcost + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdextendedcost + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdfreight + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdfreight + "\").value=formatNumber(fieldvalue);\n"
								
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborcostperunit + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdlaborcostperunit + "\").value=formatNumber(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborquantity + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.0000\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdlaborquantity + "\").value=formatNumberTo4Places(fieldvalue);\n"
				
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborsellpriceperunit + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdlaborsellpriceperunit + "\").value=formatNumber(fieldvalue);\n"
												
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value=formatNumber(fieldvalue);\n"
										
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdquantity + "\").value).replace(',','');\n"
				+ "    if (!isNumeric(temp)){ \n"
				+ "        temp = ''; \n"
				+ "    } \n"
				+ "    if (temp == ''){\n"
				+ "        fieldvalue = parseFloat(\"0.0000\");\n"
				+ "    }else{\n"
				+ "        fieldvalue = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdquantity + "\").value=formatNumberTo4Places(fieldvalue);\n"
				
				+ "    //Reformat the line values: \n"
				+ "    "
				+ "    for (i=0; i<document.forms[0].length; i++){\n"
				+ "        ctl = document.forms[0].elements[i];\n"
				+ "        if (ctl.name.indexOf('" + ESTIMATE_LINE_PREFIX + "') >= 0 ){ \n"
				+ "            if (ctl.name.indexOf('" + SMTablesmestimatelines.bdextendedcost + "') >= 0 ){ \n"
				+ "                var temp = (ctl.value).replace(',','');\n"
				+ "                if (!isNumeric(temp)){ \n"
				+ "                    temp = ''; \n"
				+ "                } \n"
				+ "                if (temp == ''){\n"
				+ "                    fieldvalue = parseFloat(\"0.00\");\n"
				+ "                }else{\n"
				+ "                    fieldvalue = parseFloat(temp);\n"
				+ "                }\n"
				+ "                document.getElementById(ctl.name).value=formatNumber(fieldvalue);\n"
				+ "            } \n"
				+ "        } \n"
				+ "        if (ctl.name.indexOf('" + ESTIMATE_LINE_PREFIX + "') >= 0 ){ \n"
				+ "            if (ctl.name.indexOf('" + SMTablesmestimatelines.bdquantity + "') >= 0 ){ \n"
				+ "                var temp = (ctl.value).replace(',','');\n"
				+ "                if (!isNumeric(temp)){ \n"
				+ "                    temp = ''; \n"
				+ "                } \n"
				+ "                if (temp == ''){\n"
				+ "                    fieldvalue = parseFloat(\"0.0000\");\n"
				+ "                }else{\n"
				+ "                    fieldvalue = parseFloat(temp);\n"
				+ "                }\n"
				+ "                document.getElementById(ctl.name).value=formatNumberTo4Places(fieldvalue);\n"
				+ "            } \n"
				+ "        } \n"

				+ "    }"
				
				+ "}\n\n"
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
			
			//Compare floats with 4 decimal precision:
			s += "function compare4DecimalPlaceFloats(float1, float2){ \n"
				+ "    var firstfloatstring = (Math.round(parseFloat(float1)*100)/100).toFixed(4); \n"
				+ "    var secondfloatstring = (Math.round(parseFloat(float2)*100)/100).toFixed(4); \n"
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
				+ "    var totalmarkup = parseFloat(\"0.00\");\n"
				+ "    var MUpercentage = parseFloat(\"0.00\");\n"
				
				
				+ "    var temp = (document.getElementById(\"" + FIELD_MU_PERCENTAGE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        MUpercentage = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        MUpercentage = parseFloat(temp);\n"
				+ "    }\n"
				+ "    MUpercentage = MUpercentage / 100; \n"
				
				+ "    //Get the total cost before mark-up:\n"
				+ "    var costsubtotal = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_COST_SUBTOTAL + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        costsubtotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        costsubtotal = parseFloat(temp);\n"
				+ "    }\n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value=(costsubtotal * MUpercentage).toFixed(2);\n"
				+ "    flagDirty();\n"
	   			;
			s += "}\n"
	   		;
			
			//Recalculate MU using MU per labor unit:
			s += "function calculateMUusingMUperlaborunit(){\n"
				+ "    var markup = parseFloat(\"0.00\");\n"
				+ "    var MUperlaborunit = parseFloat(\"0.00\");\n"
				+ "    var laborunits = parseFloat(\"0.00\");\n"
				
				+ "    var temp = (document.getElementById(\"" + FIELD_MU_PER_LABOR_UNIT + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        MUperlaborunit = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        MUperlaborunit = parseFloat(temp);\n"
				+ "    }\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdlaborquantity + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        laborunits = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        laborunits = parseFloat(temp);\n"
				+ "    }\n"
				+ "    markup = MUperlaborunit * laborunits; \n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value=markup.toFixed(2);\n"
				+ "    flagDirty();\n"
	   			;
			s += "}\n"
	   		;
			
			//Recalculate MU using GP percentage:
			s += "function calculateMUusingGPpercentage(){\n"
				+ "    var markup = parseFloat(\"0.00\");\n"
				+ "    var GPpercentage = parseFloat(\"0.00\");\n"
				+ "    var GPpercentageAsFraction = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + FIELD_GP_PERCENTAGE + "\").value).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        GPpercentage = parseFloat(\"0.00\")\n;"
				+ "    }else{\n"
				+ "        GPpercentage = parseFloat(temp);\n"
				+ "    }\n"
				+ "    GPpercentageAsFraction = GPpercentage / 100; \n"
				
				+ "    var costsubtotal = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_COST_SUBTOTAL + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        costsubtotal = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        costsubtotal = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    var taxonmaterial = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        taxonmaterial = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        taxonmaterial = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    var additionalposttaxcost = parseFloat(\"0.00\");\n"
				+ "    var temp = (document.getElementById(\"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\").innerText).replace(',','');\n"
				+ "    if (temp == ''){\n"
				+ "        additionalposttaxcost = parseFloat(\"0.00\");\n"
				+ "    }else{\n"
				+ "        additionalposttaxcost = parseFloat(temp);\n"
				+ "    }\n"
				
				+ "    var premarkupcost = parseFloat(\"0.00\");\n"
				+ "    var premarkupcost = costsubtotal + taxonmaterial + additionalposttaxcost; \n"
				+ "    markup = (premarkupcost / (1 - (GPpercentageAsFraction))) - premarkupcost; \n"
				+ "    document.getElementById(\"" + SMTablesmestimates.bdmarkupamount + "\").value=markup.toFixed(2);\n"
				+ "    flagDirty();\n"
				
	   			;
			s += "}\n"
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

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}