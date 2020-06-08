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
//	public static final String REMOVE_ESTIMATE_COMMAND = "REMOVEESTIMATE";
//	public static final String PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED = "REMOVEESTIMATELINENUMBER";
//	public static final String BUTTON_ADD_MANUAL_ESTIMATE_CAPTION = "Add estimate manually";
//	public static final String BUTTON_ADD_MANUAL_ESTIMATE = "ADDMANUALESTIMATE";
//	public static final String BUTTON_ADD_VENDOR_QUOTE_CAPTION = "Add vendor quote number:";
//	public static final String BUTTON_ADD_VENDOR_QUOTE = "ADDVENDORQUOTE";
//	public static final String FIELD_VENDOR_QUOTE = "VENDORQUOTENUMBER";
//	public static final String BUTTON_FIND_VENDOR_QUOTE_CAPTION = "Find vendor quote";
//	public static final String BUTTON_FIND_VENDOR_QUOTE = "FINDVENDORQUOTE";
//	public static final String FIND_VENDOR_QUOTE_COMMAND_VALUE = "FINDVENDORQUOTECOMMAND";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_COST = "LABELCALCULATEDTOTALMATERIALCOST";
	public static final String LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
//	public static final String LABEL_CALCULATED_TOTAL_FREIGHT = "LABELCALCULATEDTOTALFREIGHT";
//	public static final String LABEL_CALCULATED_TOTAL_FREIGHT_CAPTION = "TOTAL FREIGHT:";
//	public static final String LABEL_CALCULATED_TOTAL_LABOR_UNITS = "LABELCALCULATEDTOTALLABORUNITS";
//	public static final String LABEL_CALCULATED_TOTAL_LABOR_UNITS_CAPTION = "TOTAL LABOR UNITS:";
//	public static final String LABEL_CALCULATED_TOTAL_LABOR_COST = "LABELCALCULATEDTOTALLABORCOST";
//	public static final String LABEL_CALCULATED_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
//	public static final String LABEL_CALCULATED_TOTAL_MARKUP = "LABELCALCULATEDTOTALMARKUP";
//	public static final String LABEL_CALCULATED_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL = "LABELCALCULATEDTOTALTAX";
	public static final String LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
//	public static final String LABEL_CALCULATED_TOTAL_FOR_SUMMARY = "LABELCALCULATEDTOTALFORSUMMARY";
//	public static final String LABEL_CALCULATED_TOTAL_FOR_SUMMARY_CAPTION = "CALCULATED TOTAL FOR ESTIMATE SUMMARY #";
//	public static final String LABEL_CALCULATED_RETAIL_SALES_TAX = "LABELCALCULATEDRETAILSALESTAX";
//	public static final String LABEL_CALCULATED_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
//	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_COST = "LABELADJUSTEDTOTALMATERIALCOST";
//	public static final String LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION = "TOTAL MATERIAL COST:";
	public static final String FIELD_FREIGHT_CAPTION = "TOTAL FREIGHT:";
	public static final String FIELD_LABOR_UNITS_CAPTION = "LABOR UNITS:";
	public static final String FIELD_COST_PER_LABOR_UNIT_CAPTION = "LABOR COST/UNIT:";
//	public final String LABEL_ADJUSTED_TOTAL_LABOR_COST = "LABELADJUSTEDTOTALLABORCOST";
	public static final String LABEL_TOTAL_LABOR_COST_CAPTION = "TOTAL LABOR COST:";
	public static final String LABEL_TOTAL_LABOR_COST = "TOTOALLABORCOST";
	public static final String FIELD_ADJUSTED_MU_PER_LABOR_UNIT = "LABELADJUSTEDMUPERLABORUNIT";
	public static final String FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION = "MU PER LABOR UNIT:";
	public static final String FIELD_ADJUSTED_MU_PERCENTAGE = "LABELADJUSTEDMUPERCENTAGE";
	public static final String FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION = "MU PERCENTAGE:";
	public static final String FIELD_ADJUSTED_GP_PERCENTAGE = "LABELADJUSTEDGPPERCENTAGE";
	public static final String FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION = "GP PERCENTAGE:";
	public static final String LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION = "TOTAL MARK-UP:";
//	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL = "LABELADJUSTEDTOTALTAXONMATERIAL";
//	public static final String LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION = "TOTAL TAX ON MATERIAL:";
//	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY = "LABELADJUSTEDTOTALFORSUMMARY";
//	public static final String LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION = "ADJUSTED TOTAL FOR ESTIMATE SUMMARY #";
//	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX = "LABELADJUSTEDRETAILSALESTAX";
//	public static final String LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION = "RETAIL SALES TAX:";
	public static final String LABEL_COST_SUBTOTAL_CAPTION = "COST SUBTOTAL:";
	public static final String LABEL_COST_SUBTOTAL = "COSTSUBTOTAL";
	public static final String FIELD_ADDITIONAL_TAXED_COST_CAPTION = "ADDITIONAL COST SUBJECT TO USE TAX:";
	public static final String FIELD_ADDITIONAL_UNTAXED_COST_CAPTION = "ADDITIONAL COST NOT SUBJECT TO USE TAX:";
	public static final String FIELD_LABOR_SELL_PRICE_PER_UNIT_CAPTION = "LABOR SELL PRICE PER UNIT:";
	
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
	public static final String PARAM_FIND_ITEM_RETURN_FIELD = "PARAMFINDITEMRETURNFIELD";
	
	public static final String UNSAVED_ESTIMATE_LABEL = "(UNSAVED)";
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
			
	    	try {
	    		estimate.load(getServletContext(), smedit.getsDBID(), smedit.getUserID());
			} catch (Exception e) {
				System.out.println("[202006040606] - could not load estimate - " + e.getMessage());
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslid()
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
		
		//Include an outer table:
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		s += "  <TR>" + "\n";
		s += "    <TD>" + "\n";
		s += buildSummaryHeaderTable(conn, estimate);
		
		s += buildEstimateHeaderTable(conn, estimate, sm);
		
		s += buildEstimateLinesTable(conn, estimate, sm, req);
		
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
		HttpServletRequest req
		) throws Exception{
		
		String s = "";
		int iNumberOfColumns = 7;
		
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
		
		//Extended price:
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\""
			+ " style = \" font-weight:bold; font-style:underline; \" >"
			+ "Extended cost"
			+ "</TD>" + "\n"
		;
		
		s += "  </TR>\n";
		
		s += "  <TR>" + "\n";
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.bdquantity + "\""
			+ " ID=\"" + SMTablesmestimates.bdquantity + "\""
			+ " VALUE=\"" + estimate.getsbdquantity() + "\""
			+ " MAXLENGTH=15"
			+ " style = \" text-align:right; width:100px;\""
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
			+ " style = \" width:100px;\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		//Blank column for item finder:
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
			+ " style = \" width:50px;\""
			+ ">"
			+ "</TD>" + "\n"
		;
		
		s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimates.bdextendedcost + "\""
			+ " ID=\"" + SMTablesmestimates.bdextendedcost + "\""
			+ " VALUE=\"" + estimate.getsbdextendedcost() + "\""
			+ " MAXLENGTH=32"
			+ " style = \" text-align:right; width:120px;\""
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
		for (int iEstimateLineCounter = 0; iEstimateLineCounter < estimate.getLineArray().size(); iEstimateLineCounter++) {
			//Display each line:
			s += "  <TR>" + "\n";
			s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdquantity + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsbdquantity() + "\""
					+ " MAXLENGTH=15"
					+ " style = \" text-align:right; width:100px;\""
					+ ">"
					+ "</TD>" + "\n"
				;
			
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sitemnumber + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsitemnumber() + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:100px;\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\""
						+ " style = \" font-weight:bold; font-style:underline; \" >"
						+ ""
						//+ buildItemFinderButton(Integer.toString(iEstimateLineCounter))
						+ "</TD>" + "\n"
					;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.slinedescription + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getslinedescription().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:500px;\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.sunitofmeasure + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsunitofmeasure().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH=32"
					+ " style = \" width:50px;\""
					+ ">"
					+ "</TD>" + "\n"
				;
				
				s += "    <TD  class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\""
					+ ">"
					+ "<INPUT TYPE=TEXT"
					+ " NAME=\"" +  ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " ID=\"" + ESTIMATE_LINE_PREFIX + clsStringFunctions.PadLeft(
							Integer.toString(iEstimateLineCounter), "0", ESTIMATE_LINE_NO_PAD_LENGTH) + SMTablesmestimatelines.bdextendedcost + "\""
					+ " VALUE=\"" + estimate.getLineArray().get(iEstimateLineCounter).getsbdextendedcost() + "\""
					+ " MAXLENGTH=32"
					+ " style = \" text-align:right; width:120px;\""
					+ ">"
					+ "</TD>" + "\n"
				;
				s += "  </TR>" + "\n";
		}
		
		//Now add one blank line for new entries:
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
				+ " style = \" text-align:right; width:100px;\""
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
				+ ">"
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
				+ " style = \" text-align:right; width:120px;\""
				+ ">"
				+ "</TD>" + "\n"
			;
			s += "  </TR>" + "\n";
		
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
		
		s += "<B>Estimate ID:</B>&nbsp;" + sEstimateID
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
			+ " onchange=\"flagdirty();\""
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
			+ " onchange=\"flagdirty();\""
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
				//TODO
				sVendorQuoteLink = 
			    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOHDirectQuote"
			    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    		+ "&" + "C_QuoteNumberString=" + sVendorQuoteLink
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
			    		+ "\">" + sVendorQuoteLink + "</A>"
			    		+ ", line #" + estimate.getsivendorquotelinenumber()
				;
				// http://localhost:8080/sm/smcontrolpanel.SMDisplayOHDirectQuote?C_QuoteNumberString=SQAL000008-1&db=servmgr1&CallingClass=smcontrolpanel.SMOHDirectQuoteListing
				
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
			//+ " onchange=\"flagdirty();\""
			+ ">"
			+ "</INPUT>" + "\n"
			+ "&nbsp;, line number:&nbsp;"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + FIELD_REPLACE_QUOTE_LINE + "\""
			+ " ID = \"" + FIELD_REPLACE_QUOTE_LINE + "\""
			+ " style = \" text-align:left; width:60px;\""
			+ " VALUE = \"" + "" + "\""
			//+ " onchange=\"flagdirty();\""
			+ ">"
			+ "</INPUT>" + "\n"
		;
		
		s += "<BR>" + "\n";
		;

		return s;
	}
	private String buildSummaryHeaderTable(Connection conn, SMEstimate estimate) throws Exception{
		
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
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Estimate Summary #:"
			+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
			+ summary.getslid()
			
			+ "<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + SMTablesmestimates.lsummarylid + "\""
			+ " ID=\"" + SMTablesmestimates.lsummarylid + "\""
			+ " VALUE=\"" + estimate.getslsummarylid() + "\""
			+ ">"
			
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
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "Sales lead #:"
				+ "</TD>" + "\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" >"
				+ summary.getslsalesleadid()
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
			+ " onchange=\"recalculatelivetotals();\""
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
			+ " onchange=\"recalculatelivetotals();\""
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
			+ " onchange=\"recalculatelivetotals();\""
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
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\""
			+ " ID = \"" + SMTablesmestimates.bdadditionalpretaxcostamount + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdadditionalpretaxcostamount() + "\""
			+ " onchange=\"recalculatelivetotals();\""
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
				+ FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION
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
				+ " NAME = \"" + SMTablesmestimates.bdmarkupamount + "\""
				+ " ID = \"" + SMTablesmestimates.bdmarkupamount + "\""
				+ " style = \" text-align:right; width:100px;\""
				+ " VALUE = \"" + estimate.getsbdmarkupamount() + "\""
				+ " onchange=\"recalculatelivetotals();\""
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
			+ "</TD>" + "\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\""
			+ " ID = \"" + SMTablesmestimates.bdadditionalposttaxcostamount + "\""
			+ " style = \" text-align:right; width:100px;\""
			+ " VALUE = \"" + estimate.getsbdadditionalposttaxcostamount() + "\""
			+ " onchange=\"recalculatelivetotals();\""
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
			+ " onchange=\"recalculatelivetotals();\""
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
	
	private String createReplaceVendorQuoteLineButton() {
		String s = "";
		s += "<button type=\"button\""
			+ " value=\"" + REPLACE_VENDOR_QUOTE_BUTTON_CAPTION + "\""
			+ " name=\"" + REPLACE_VENDOR_QUOTE_BUTTON + "\""
			+ " id=\"" + REPLACE_VENDOR_QUOTE_BUTTON + "\""
			+ " onClick=\"replacevendorquote;\">"
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
			+ " onClick=\"refreshvendorquote;\">"
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
				+ "        if (document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + SAVE_COMMAND_VALUE + "\""
						+ " && document.getElementById(\"" + COMMAND_FLAG + "\").value != \"" + DELETE_COMMAND_VALUE + "\"){\n"
				+ "        return 'You have unsaved changes!';\n"
				+ "        }\n"
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
			
			//Save
			s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"" + FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			
			/*
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
			*/
			s += "function flagDirty() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n";
			
			
						
			//Recalculate live totals:
			s += "function recalculatelivetotals(){\n"
			
			;
					
				/*
					
				//+ "    alert('Recalculating');\n"
					
				// TJR - 6/2/2020 - we don't want the tax to update automatically when the page loads.
				// That should be done deliberately by the user if he WANTS to update the tax info.
				//+ "    //Set the retail sales tax rate, based on the current index of the tax drop down: \n"
				//+ "    taxChange(document.getElementById(\"" + SMTablesmestimatesummaries.itaxid + "\")); \n"
				+ "\n"
				
				//+ "    //Turn off the line amt warning by default:\n"
				//+ "    document.getElementById(\"" + CALCULATED_LINE_TOTAL_FIELD_CONTAINER + "\").style.display= \"none\"\n"
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
			*/
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
			
			/*
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
				+ "    recalculatelivetotals();\n"
				
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
				+ "    recalculatelivetotals();\n"
				
	   			;
			s += "}\n"
	   		;
			*/
			
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

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}