package smcontrolpanel;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelabortypes;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMIncorporateSummaryEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String LABORTYPE_LIST_OPTION_NOT_CHOSEN_VALUE = "";
	public static String LOCATION_LIST_OPTION_NOT_CHOSEN_VALUE = "";
	public static String TAX_LIST_OPTION_NOT_CHOSEN_VALUE = "";
	public static String ITEMCATEGORY_LIST_OPTION_NOT_CHOSEN_VALUE = "";
	public static String CALCULATE_BUTTON_NAME = "CALCULATE";
	public static String CALCULATE_BUTTON_LABEL = "Calculate billing values";
	public static String BUILDITEMS_BUTTON_NAME = "BUILDITEMS";
	public static String BUILDITEMS_BUTTON_LABEL = "Build inventory items";
	public static String ADDITEMSTOORDER_BUTTON_NAME = "ADDITEMS";
	public static String ADDITEMSTOORDER_BUTTON_LABEL = "Create items and add to order";
	//public static String ADDBLANKLINE_BUTTON_NAME = "ADDBLANKLINE";
	//public static String ADDBLANKLINE_BUTTON_LABEL = "Add a blank line";
	public static String FIRST_ENTRY_INTO_CLASS = "FIRSTENTRYINTOCLASS";
	public static String CHOOSE_CATEGORY_METHOD_PARAM = "CHOOSECATEGORYMETHOD";
	public static String CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL = "Choose categories for each line";
	public static String CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL = "Choose single category for all lines";
	public static String CHOOSE_CATEGORY_BY_HEADER_BUTTON_NAME = "CHOOSECATEGORYBYHEADERBUTTON";
	public static String CHOOSE_CATEGORY_BY_LINE_BUTTON_NAME = "CHOOSECATEGORYBYLINEBUTTON";
	
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		SMIncorporateSummary entry = new SMIncorporateSummary();
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMIncorporateSummaryAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMDirectItemEntry)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		String sSummaryID = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.lid, request);
		String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.strimmedordernumber, request);
		
		//If this is a 'resubmit', meaning it's being called by the action class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//If this is the first time this is invoked, that is, if it's not a 'RESUBMIT',
		//make sure to get rid of any session object:
		
		//If there is an object in the current session:
		if (currentSession.getAttribute(SMIncorporateSummary.ParamObjectName) != null){
			entry = (SMIncorporateSummary) currentSession.getAttribute(SMIncorporateSummary.ParamObjectName);
			if (bDebugMode){
				System.out.println("[1579269590] In " + this.toString() + " entry after reading from request: " + entry.read_out_debug_data());
			}
			//Remove the object from the session:
			currentSession.removeAttribute(SMIncorporateSummary.ParamObjectName);
		}
		if (bDebugMode){
			System.out.println("[1579269724] In " + this.toString() + " entry after reading from request: " + entry.read_out_debug_data());
		}
	    smedit.printHeaderTable();
	    
	    /*
		boolean bDirectEntryAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMDirectItemEntry, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		*/
	    
		//Add a link to go back to the summary:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMEditSMSummaryEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "&" + SMTablesmestimatesummaries.lid + "=" + sSummaryID
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString()) + "\">" + "Return to summary #" + sSummaryID + "</A>"
		);
		
		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);
		if (bDebugMode){
			System.out.println("[1593612679] In " + this.toString() + " just before createEditPage.");
		}
		
		//Try to load the summary:
		SMEstimateSummary summary = new SMEstimateSummary();
		summary.setslid(sSummaryID);
		try {
			summary.load(getServletContext(), smedit.getsDBID(), smedit.getUserID());
		} catch (Exception e1) {
			currentSession.setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, (String)e1.getMessage());
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTablesmestimatesummaries.lid + "=" + sSummaryID
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
					return;
		}
		
		entry.setM_ordernumber(sTrimmedOrderNumber);
		
	    try {
			smedit.createEditPage(
					getEditHTML(
							smedit,
							entry,
							summary), 
							""
					);
		} catch (Exception e) {
			currentSession.setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, (String)"Could not create edit page - " + e.getMessage());
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesmestimatesummaries.lid + "=" + sSummaryID
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		if (bDebugMode){
			System.out.println("[1579269537] In " + this.toString() + " just after createEditPage.");
		}
	    return;
	}
	private String createControlButtons(String sNonStockMaterialItem, boolean bChooseCategoryByLine){
		String s = "";
		
		s += "<BR>";
		
		//s += "<INPUT TYPE=SUBMIT NAME='" 
		//	+ ADDBLANKLINE_BUTTON_NAME
		//	+ "'" 
		//	+ "' VALUE='"
		//	+ ADDBLANKLINE_BUTTON_LABEL
		//	+ "'" + " STYLE='height: 0.24in'>"
		//	+ "&nbsp;"
		//;
		
		//s += "<INPUT TYPE=SUBMIT NAME='" 
		//	+ CALCULATE_BUTTON_NAME
		//	+ "'" 
		//	+ "' VALUE='"
		//	+ CALCULATE_BUTTON_LABEL
		//	+ "'" + " STYLE='height: 0.24in'>"
		//	+ "&nbsp;"
		//	;
		
		//s += "<INPUT TYPE=SUBMIT NAME='" 
		//	+ BUILDITEMS_BUTTON_NAME
		//	+ "'" 
		//	+ "' VALUE='"
		//	+ BUILDITEMS_BUTTON_LABEL
		//	+ "'" + " STYLE='height: 0.24in'>";
		
		s += "<BR>" + "\n";
		
		s += "<INPUT TYPE=SUBMIT NAME='" 
			+ ADDITEMSTOORDER_BUTTON_NAME
			+ "'" 
			+ "' VALUE='"
			+ ADDITEMSTOORDER_BUTTON_LABEL
			+ "'" + " STYLE='height: 0.24in'>"
			+ "&nbsp;" + "\n"
			;
		
		if (bChooseCategoryByLine){
			s += "<INPUT TYPE=SUBMIT NAME='" 
				+ CHOOSE_CATEGORY_BY_HEADER_BUTTON_NAME
				+ "'" 
				+ "' VALUE='"
				+ CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL
				+ "'" + " STYLE='height: 0.24in'>"
				+ "&nbsp;" + "\n"
			;
		}else{
			s += "<INPUT TYPE=SUBMIT NAME='" 
				+ CHOOSE_CATEGORY_BY_LINE_BUTTON_NAME
				+ "'" 
				+ "' VALUE='"
				+ CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL
				+ "'" + " STYLE='height: 0.24in'>"
				+ "&nbsp;" + "\n"
				;
		}
		
		//s += "<BR><FONT SIZE=2>"
		//	+ "<B>NOTE:&nbsp;</B>To add the <I><B>material</B></I> items using a NON-STOCK item,"
		//	+ " enter that item number here (otherwise new dedicated items will be created):</B></FONT>"
		//	+ "<INPUT TYPE=TEXT NAME=\"" + SMIncorporateSummary.Paramsnonstockmaterialitem + "\""
		//	+ " VALUE=\"" + sNonStockMaterialItem + "\" SIZE=13 MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength) + ">"
		//;
		
		return s;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			SMIncorporateSummary entry,
			SMEstimateSummary summary
	) throws Exception{

		//First, load the locations:
		ArrayList<String> arrLocations = new ArrayList<String>(0);
		ArrayList<String> arrLocationNames = new ArrayList<String>(0);
		ArrayList<String> arrLaborTypes = new ArrayList<String>(0);
		ArrayList<String> arrLaborTypeNames = new ArrayList<String>(0);
		ArrayList<String> arrItemCategories = new ArrayList<String>(0);
		ArrayList<String> arrItemCategoryNames = new ArrayList<String>(0);
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".getEditHTML - user: " + sm.getUserID()
					+ " - " + sm.getFullUserName()
			);
		} catch (Exception e1) {
			throw new Exception("Error [202007013951] - getting connection - " + e1.getMessage());
		}
		
		try {
			loadLocations(arrLocations, arrLocationNames, conn);
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080419]");
			throw e;
		}
		try {
			loadLaborTypes(arrLaborTypes, arrLaborTypeNames, conn);
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080410]");
			throw e;
		}
		try {
			loadItemCategories(arrItemCategories, arrItemCategoryNames, conn);
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080411]");
			throw e;
		}
		//Load the order:
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(entry.getM_ordernumber());
		if (!order.load(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080412]");
			throw new Exception("Could not load order number " + entry.getM_ordernumber() + " - " + order.getErrorMessages());
		}
		
		if (summary.getsitaxid().compareToIgnoreCase(order.getitaxid()) != 0) {
			throw new Exception("Error [202007014404] - the tax selected on the summary does not match the tax on this order (" 
				+ order.getstaxjurisdiction() + " - " + order.getstaxtype() + ") - modify"
				+ " and save the summary, then try again."
			);
		}
		
		//TODO - re-work this remaining code:
		
		entry.setstaxjurisdiction(order.getstaxjurisdiction());
		
		//Load the incorporation object with the values from the summary:
		entry.loadSummaryValues(summary.getslid(), conn);
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080913]");		
		String s = "";
		//Store the entry variables that we know at this point:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMIncorporateSummary.Paramsordernumber + "\" VALUE=\"" 
				+ entry.getM_ordernumber() + "\"" + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMIncorporateSummary.Paramstaxjurisdiction + "\" VALUE=\"" 
				+ entry.getstaxjurisdiction() + "\"" + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + CHOOSE_CATEGORY_METHOD_PARAM + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(CHOOSE_CATEGORY_METHOD_PARAM, sm.getRequest()) + "\"" + "\">";
		
		//Order number
		s += "<B>Order #:</B>&nbsp;" + entry.getM_ordernumber()
			+ "&nbsp;<B>Bill to:</B>&nbsp;" + order.getM_sBillToName()
			+ "&nbsp;<B>Ship to:</B>&nbsp;" + order.getM_sShipToName()
			+ "&nbsp;<B>Total order contract amount:</B>&nbsp;" + order.getM_bdtotalcontractamount()
        ;

        s += "<BR><B>Total billing amt for summary"
        	+ "<a href=\"#totalbillingamt\"><SUP>1</SUP></a>"
        	+ ":</B>&nbsp;"
        	+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(summary.getTotalPrice())
        	+ "\n";
    	;
        
    	/*
		s += "&nbsp;<B>Unit labor cost"
			+ "<a href=\"#unitlaborcost\"><SUP>2</SUP></a>"
			+ ":</B>";
        s += "<INPUT TYPE=TEXT NAME=\"" + SMIncorporateSummary.Paramsunitlaborcost + "\""
    	+ " VALUE=\"" + entry.getM_sunitlaborcost().replace("\"", "&quot;") + "\""
    	+ " SIZE=" + "9"
    	+ " MAXLENGTH=" + "15"
    	+ ">"
        ;
		*/
		s += "<BR><B>Labor type: </B>";
		s += "<SELECT NAME=\"" + SMIncorporateSummary.Paramslabortype + "\"" + ">";
		//Add one for the 'Other':
		s += "<OPTION";
		if (entry.getM_slabortype().compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + LABORTYPE_LIST_OPTION_NOT_CHOSEN_VALUE 
			+ "\">" + "** Select a labor type ***</OPTION>";
		
		for (int i = 0; i < arrLaborTypes.size(); i++){
			s += "<OPTION";
			if (arrLaborTypes.get(i).compareToIgnoreCase(summary.getsilabortype()) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + arrLaborTypes.get(i).toString() + "\">" + arrLaborTypeNames.get(i).toString();
			s += "</OPTION>";
		}
    	s += "</SELECT>";
		
        //location
		s += "&nbsp;<B>Location: </B>";
		String sLocation = entry.getM_slocation();
		if (sLocation.compareToIgnoreCase("") == 0){
			sLocation = order.getM_sLocation();
		}
		s += "<SELECT NAME=\"" + SMIncorporateSummary.Paramslocation + "\"" + ">";
		//Add one for the 'Other':
		s += "<OPTION";
		if (sLocation.compareToIgnoreCase("") == 0){
			s += " selected=YES ";
		}
		s += " VALUE=\"" + LOCATION_LIST_OPTION_NOT_CHOSEN_VALUE 
			+ "\">" + "** Select a location ***</OPTION>";
		
		for (int i = 0; i < arrLocations.size(); i++){
			s += "<OPTION";
			if (arrLocations.get(i).compareToIgnoreCase(sLocation) == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + arrLocations.get(i).toString() + "\">" + arrLocationNames.get(i).toString();
			s += "</OPTION>";
		}
    	s += "</SELECT>";
    	
    	boolean bChooseCategoryByLine = clsManageRequestParameters.get_Request_Parameter(CHOOSE_CATEGORY_METHOD_PARAM, sm.getRequest()).compareToIgnoreCase(CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL) == 0;
    	
    	if(!bChooseCategoryByLine){
	        //item category
			s += "&nbsp;<B>Item category: </B>";
			String sItemCategory = entry.getM_sitemcategory();
			if (sItemCategory.compareToIgnoreCase("") == 0){
				sItemCategory = order.getM_sDefaultItemCategory();
			}
			s += "<SELECT NAME=\"" + SMIncorporateSummary.Paramsitemcategory + "\"" + ">";
			//Add one for the 'Other':
			s += "<OPTION";
			if (sItemCategory.compareToIgnoreCase("") == 0){
				s += " selected=YES ";
			}
			s += " VALUE=\"" + ITEMCATEGORY_LIST_OPTION_NOT_CHOSEN_VALUE 
				+ "\">" + "** Select an item category ***</OPTION>";
			
			for (int i = 0; i < arrItemCategories.size(); i++){
				s += "<OPTION";
				if (arrItemCategories.get(i).compareToIgnoreCase(sItemCategory) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + arrItemCategories.get(i).toString() + "\">" + arrItemCategories.get(i).toString() + " - " + arrItemCategoryNames.get(i).toString();
				s += "</OPTION>";
			}
	    	s += "</SELECT>";
    	}
    	
		s += "<BR><FONT SIZE=2>"
				+ "<B>NOTE:&nbsp;</B>To add the <I><B>material</B></I> items using a NON-STOCK item,"
				+ " enter that item number here (otherwise new dedicated items will be created):</B></FONT>"
				+ "<INPUT TYPE=TEXT NAME=\"" + SMIncorporateSummary.Paramsnonstockmaterialitem + "\""
				+ " VALUE=\"" + entry.getM_snonstockmaterialitem() + "\" SIZE=13 MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength) + ">"
			;
    	
    	s += createControlButtons(entry.getM_snonstockmaterialitem(), 
    		clsManageRequestParameters.get_Request_Parameter(CHOOSE_CATEGORY_METHOD_PARAM, sm.getRequest()).compareToIgnoreCase(CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL) == 0);
    	
    	s += listItemLines(
				getServletContext(), 
				sm.getsDBID(), 
				sm.getUserName(), 
				entry,
				arrItemCategories,
				arrItemCategoryNames,
				order.getM_sDefaultItemCategory(),
				summary,
				bChooseCategoryByLine
		);
    	s += sFootnotes();
		return s;
	}
	private String listItemLines(
			ServletContext context, 
			String sDBID, 
			String sUserName,
			SMIncorporateSummary entry,
			ArrayList <String>arrCategories,
			ArrayList <String>arrItemCategoryNames,
			String sDefaultCategory,
			SMEstimateSummary summary,
			boolean bChooseCategoryByLine
	){
		String s = "";
		int iColumnCounter = 0;
		
		s += "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">" + "\n";
		s += "  <TR>" + "\n";
		s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Line</B></FONT></TD>" + "\n";
		iColumnCounter++;
		s += "    <TD VALIGN=BOTTOM ALIGN=LEFT><FONT SIZE=2><B>Line&nbsp;type</B></FONT></TD>" + "\n";
		iColumnCounter++;
		s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Qty</B></FONT></TD>" + "\n";
		iColumnCounter++;
		s += "    <TD VALIGN=BOTTOM><FONT SIZE=2><B>Item number</B></FONT></TD>" + "\n";
		iColumnCounter++;
		s += "    <TD VALIGN=BOTTOM><FONT SIZE=2><B>Item description</B></FONT></TD>" + "\n";
		iColumnCounter++;
		s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>U/M</B></FONT></TD>" + "\n";
		iColumnCounter++;
		if (bChooseCategoryByLine){
			s += "    <TD VALIGN=BOTTOM><FONT SIZE=2><B>Item category</B></FONT></TD>" + "\n";
			iColumnCounter++;
		}
		//s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Estimated<BR>extended<BR>cost"
		//	+ "<a href=\"#estimatedextendedcost\"><SUP>2</SUP></a>"
		//	+ "</B></FONT></TD>" + "\n";
		//iColumnCounter++;
		
		//s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Estimated<BR>extended<BR>labor&nbsp;units"
		//	+ "<a href=\"#estimatedextendedlaborunits\"><SUP>3</SUP></a>"
		//	+ "</FONT></B></TD>";
		//s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Cost&nbsp;per<BR>labor&nbsp;unit"
		//		+ "</FONT></B></TD>" + "\n";
		s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Calculated<BR>extended<BR>price"
			+ "<a href=\"#extendedcalculatedprice\"><SUP>4</SUP></a>"	
			+ "</B></FONT></TD>" + "\n";
		iColumnCounter++;
		//s += "    <TD VALIGN=BOTTOM ALIGN=RIGHT><FONT SIZE=2><B>Calculated<BR>extended<BR>labor&nbsp;price"
		//	+ "<a href=\"#extendedcalculatedprice\"><SUP>4</SUP></a>"
		//	+ "</B></FONT></TD>" + "\n";
		
		String sLastEstimateID = "";
		BigDecimal bdTotalCost = new BigDecimal("0.00");
		BigDecimal bdTotalPrice = new BigDecimal("0.00");
		for (int i = 0; i < entry.getNumberOfLines(); i++){
			
			if (entry.getLine(i).getM_sestimateid().compareToIgnoreCase(sLastEstimateID) != 0) {
				//Print an estimate heading line:
				String sEstimateLink = "&nbsp;"
						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMEstimateEdit"
			    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    		+ "&" + SMTablesmestimates.lid + "=" + entry.getLine(i).getM_sestimateid()
			    		+ "&" + SMTablesmestimates.lsummarylid + "=" + summary.getslid()
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
			    		+ "\">" + entry.getLine(i).getM_sestimateid() + "</A>"
			    		+ "&nbsp;"
		    		;
				
				s += "  <TR style = \" background-color:" + SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_BLUE
					+ "; color:black; \" >" + "\n";
				s += "    <TD COLSPAN=" + Integer.toString(iColumnCounter) + " >"
					+ "<B><I>ESTIMATE ID: " + sEstimateLink + "</I></B>"
					+ "</TD>" + "\n"
				;
				s += "  </TR>" + "\n";
				
				sLastEstimateID = entry.getLine(i).getM_sestimateid();
			}
			
			s += "  <TR>" + "\n";

			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramllinenumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_llinenumber().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "3"
				+ " MAXLENGTH=" + "8"
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + " text-align:right; " + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
			
			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramslinetype + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_slinetype().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "8"
				+ " MAXLENGTH=" + "8"
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + " text-align:left; font-style:italic; " + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
			
			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsquantity + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_squantity().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "6"
				+ " MAXLENGTH=" + "15"
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + " text-align:right; " + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
				//s += "<TD>" 
				//	+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsitemnumber + SMUtilities.PadLeft(Integer.toString(i), "0", 6)+ "\""
				//	+ " VALUE=\"" + entry.getLine(i).getM_sitemnumber().replace("\"", "&quot;") + "\""
				//	+ " SIZE=" + "12"
				//	+ " MAXLENGTH=" + SMTableorderdetails.sItemNumberLength
				//	+ ">"
				//	+ "</TD>" + "\n";

			s += "    <TD>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsitemnumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
					+ " VALUE=\"" + entry.getLine(i).getM_sitemnumber().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "8"
					+ " MAXLENGTH=" + SMTableorderdetails.sItemNumberLength
					+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + "\""
					+ " readonly "
					+ ">"
					+ "</TD>" + "\n";
			
			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsitemdescription + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_sitemdescription().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "50"
				+ " MAXLENGTH=" + SMTableorderdetails.sItemDescLength
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
			
			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsunitofmeasure + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_sunitofmeasure().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "5"
				+ " MAXLENGTH=" + SMTableorderdetails.sOrderUnitOfMeasureLength
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
			
			if (bChooseCategoryByLine){
				String sItemCategory = entry.getLine(i).getM_scategorycode().replace("\"", "&quot;");
				if (sItemCategory.compareToIgnoreCase("") == 0){
					sItemCategory = sDefaultCategory;
				}
				s += "    <TD>" + "\n";
				s += "<SELECT"
					+ " style = \" background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_GREY_BLUE + "; \""
					+ " NAME=\"" + SMIncorporateSummary.Paramsitemcategory +  clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "\"" + ">" + "\n";
				//Add one for the 'Other':
				s += "<OPTION";
				if (sItemCategory.compareToIgnoreCase("") == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + ITEMCATEGORY_LIST_OPTION_NOT_CHOSEN_VALUE 
					+ "\">" + "** Select an item category ***</OPTION>" + "\n";
				
				for (int j = 0; j < arrCategories.size(); j++){
					s += "<OPTION";
					if (arrCategories.get(j).compareToIgnoreCase(sItemCategory) == 0){
						s += " selected=YES ";
					}
					s += " VALUE=\"" + arrCategories.get(j).toString() + "\">" + arrCategories.get(j).toString() + " - " + arrItemCategoryNames.get(j).toString();
					s += "</OPTION>" + "\n";
				}
		    	s += "</SELECT>" + "\n";
		    	s += "    </TD>" + "\n";
			}
			
			//s += "    <TD>" 
			//	+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsestimatedextendedmaterialcost + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
			//	+ " VALUE=\"" + entry.getLine(i).getM_sestimatedextendedmaterialcost().replace("\"", "&quot;") + "\""
			//	+ " SIZE=" + "7"
			//	+ " MAXLENGTH=" + "15"
			//	+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + " text-align:right; " + "\""
			//	+ " readonly "
			//	+ ">"
			//	+ "</TD>" + "\n";
			//s += "<TD>" 
			//	+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsextendedlaborunits + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
			//	+ " VALUE=\"" + entry.getLine(i).getM_sextendedlaborunits().replace("\"", "&quot;") + "\""
			//	+ " SIZE=" + "7"
			//	+ " MAXLENGTH=" + "15"
			//	+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_YELLOW + " text-align:right; " + "\""
			//	+ " readonly "
			//	+ ">"
			//	+ "</TD>" + "\n";
			
			//Cost per labor unit:
			//s += "<TD>" 
			//	+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsextendedlaborunits + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
			//	+ " VALUE=\"" + entry.getLine(i).getM_sextendedlaborunits().replace("\"", "&quot;") + "\""
			//	+ " SIZE=" + "7"
			//	+ " MAXLENGTH=" + "15"
			//	+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_YELLOW + " text-align:right; " + "\""
			//	+ " readonly "
			//	+ ">"
			//	+ "</TD>" + "\n";

			//These following are calculated fields:

			s += "    <TD>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMSummaryIncorporationLine.Paramsestimatedextendedmaterialcost + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)+ "\""
				+ " VALUE=\"" + entry.getLine(i).getM_sextendedmaterialbillingvalue().replace("\"", "&quot;") + "\""
				+ " SIZE=" + "7"
				+ " MAXLENGTH=" + "15"
				+ " style = \"" + SMMasterStyleSheetDefinitions.LABEL_COLOR_THEME_GREY_BLUE + " text-align:right; " + "\""
				+ " readonly "
				+ ">"
				+ "</TD>" + "\n";
			//s += "<TD ALIGN=RIGHT>" + entry.getLine(i).getM_sextendedmaterialbillingvalue() + "</TD>"
				//+ "<TD ALIGN=RIGHT>" + entry.getLine(i).getM_sextendedlaborbillingvalue() + "</TD>"
				//+ "<TD ALIGN=RIGHT>" + entry.getLine(i).getM_slaborunitsperunit() + "</TD>"
				//+ "<TD ALIGN=RIGHT>" + entry.getLine(i).getM_sgrossprofit() + "</TD>"
			;
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMSummaryIncorporationLine.Paramsestimatedextendedlaborcost 
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "\" VALUE=\"" 
					+ entry.getLine(i).getM_sestimatedextendedlaborcost() + "\"" + "\">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMSummaryIncorporationLine.Paramsextendedlaborbillingvalue
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "\" VALUE=\"" 
					+ entry.getLine(i).getM_sextendedlaborbillingvalue() + "\"" + "\">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMSummaryIncorporationLine.Paramsextendedmaterialbillingvalue
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "\" VALUE=\"" 
					+ entry.getLine(i).getM_sextendedmaterialbillingvalue() + "\"" + "\">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMSummaryIncorporationLine.Paramsgrossprofit
					+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "\" VALUE=\"" 
					+ entry.getLine(i).getM_sgrossprofit() + "\"" + "\">";
			
			bdTotalCost = bdTotalCost.add(new BigDecimal(entry.getLine(i).getM_sestimatedextendedmaterialcost().replace(",", "")));
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(entry.getLine(i).getM_sextendedmaterialbillingvalue().replace(",", "")));
			
			s += "  </TR>" + "\n";
		}
		
		//Print a 'totals' line:
		s += "  <TR style = \" background-color:" + SMMasterStyleSheetDefinitions.BACKGROUND_BLACK + "; color:white; \" >" + "\n";
		s += "    <TD COLSPAN=" + Integer.toString(iColumnCounter - 1) + " ALIGN=RIGHT >"
				+ "<B><I>TOTALS:</I></B>"
				+ "</TD>" + "\n"
				//+ "    <TD ALIGN=RIGHT>"
				//+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCost) + "</B>"
				//+ "</TD>" + "\n"
				+ "    <TD ALIGN=RIGHT>"
				+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPrice) + "</B>"
				+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n"; 
		
		s += "</TABLE>" + "\n";
		return s;
	}
	private String sFootnotes(){
		String s = "<B>NOTES:</B>";
		s += "<FONT SIZE=2>";

		s += "<BR><a name=\"totalbillingamt\"><SUP>1</SUP> <B>'Total billing amount'</B> is the targeted billing amount for this calculation."
				+ "  If you are calculating billing values for the whole order, it would be the contract amount; or if you are calculating"
				+ " the billing values for a change to an existing order, it would be the total billing value of that change order."
			;
		/*
		s += "<BR><a name=\"unitlaborcost\"><SUP>2</SUP> <B>'Unit labor cost'</B> is the presumed labor COST per labor unit."
			+ "  So, for example, if you use TRUCK DAYS, and you assume that each truck day COSTS your company about $180,"
			+ " the Unit Labor Cost would be 180."
		;
		*/
		s += "<BR><a name=\"estimatedextendedcost\"><SUP>2</SUP> <B>'Estimated extended material cost'</B> is the estimated material"
			+ " cost of the entire line, which is the estimated unit cost times the quantity on that line.";
		
		s += "<BR><a name=\"estimatedextendedlaborunits\"><SUP>3</SUP> <B>'Estimated extended labor units'</B> is the amount of labor,"
			+ " in your customary unit, like hours or truck days, which you assume it will take to install all the items on that one line.";
		
		s += "<BR><a name=\"extendedcalculatedprice\"><SUP>4</SUP> <B>'Calculated prices'</B> are the EXTENDED prices for each line"
			+ " in the grid for the labor and material.  These are ZERO until the '" + CALCULATE_BUTTON_LABEL + "'"
			+ " is clicked.  This function's price calculation is detailed here:<BR>";
		
		s += "<BR>";
		s += "1) It first checks to make sure that the contract amount, gross profit, and labor unit cost are not zero.<BR>";
		s += "2) It next calculates the TOTAL ESTIMATED MATERIAL COST for the order by adding up the extended estimated material"
			+ " cost for all the lines.<BR>";
		s += "3) For each item in the grid, it sets the EXTENDED ESTIMATED LABOR COST by multiplying the unit labor cost times the"
				+ " extended labor unit quantity.<BR>";
		s += "4) Then it checks through all the lines to find the last line with a material cost and the last line with a labor cost on it.<BR>";
		s += "5) Next it calculates the labor billing values first: For each line, it calculates the extended labor sell price by multiplying"
			+ " the labor unit markup times the number of labor units, and then adding it to the estimated (extended) labor cost.<BR>";
		s += "6) After that, it subtracts the total labor billing value from the total contract amount to get the total material billing value.<BR>";
		s += "7) It calculates the ratio of 'material cost to material sell price' by dividing the total material billing value into"
			+ " the total estimated material cost.  This MATERIAL COST PROPORTION represents the portion of the material sell price"
			+ " that is equal"
			+ " to the material cost.  (If there is NO material cost, the calculation stops here.)<BR>";
		s += "8) For each of the lines UP TO the last line with a material cost, it calculates a material billing value,"
			+ " which is equal to the estimated (extended) material cost divided by the 'MATERIAL COST PROPORTION'.<BR>";
		s += "9) Finally, whatever amount of material billing value is left, it makes that the material billing value for the last line."
				+ "  This is done to compensate for any rounding that happened in the previous calculations and ensures that all the"
				+ " billing values add up to the exact contract amount.<BR>";
		
		s += "</FONT>";
		
		return s;
	}
	private boolean loadLocations(
			ArrayList<String> arrLocs,
			ArrayList<String> arrLocNames,
			Connection conn
	) throws SQLException{
		
		String SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY " + SMTablelocations.sLocation
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrLocs.add(rs.getString(SMTablelocations.sLocation));
				arrLocNames.add(rs.getString(SMTablelocations.sLocationDescription));
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		}
		
		return true;
	}

	private boolean loadItemCategories(
			ArrayList<String> arrItemCategories,
			ArrayList<String> arrItemCategoryNames,
			Connection conn
	) throws SQLException{
		
		String SQL = "SELECT"
			+ " " + SMTableiccategories.sCategoryCode
			+ ", " + SMTableiccategories.sDescription
			+ " FROM " + SMTableiccategories.TableName
			+ " ORDER BY " + SMTableiccategories.sCategoryCode
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrItemCategories.add(rs.getString(SMTableiccategories.sCategoryCode));
				arrItemCategoryNames.add(rs.getString(SMTableiccategories.sDescription));
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		}
		
		return true;
	}
	private boolean loadLaborTypes(
			ArrayList<String> arrLaborTypes,
			ArrayList<String> arrLaborTypeNames,
			Connection conn
	) throws SQLException{
		
		String SQL = "SELECT *"
			+ " FROM " + SMTablelabortypes.TableName
			+ " ORDER BY " + SMTablelabortypes.sID
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				arrLaborTypes.add(rs.getString(SMTablelabortypes.sID));
				arrLaborTypeNames.add(rs.getString(SMTablelabortypes.sLaborName));
			}
			rs.close();
		} catch (SQLException e) {
			throw e;
		}
		
		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
