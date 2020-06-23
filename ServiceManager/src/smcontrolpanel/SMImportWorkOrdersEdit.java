package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class SMImportWorkOrdersEdit  extends HttpServlet {
	public static final String CHECKBOX_BASE = "CHECKBOXBASE";
	public static final int LENGTH_OF_CHECKBOX_PADDING = 13;
	public static final int LENGTH_OF_LINE_COUNTER_PADDING = 6;
	public static final String SUBMIT_FOR_IMPORT_BUTTON_NAME = "SUBMITFORIMPORT";
	public static final String SUBMIT_FOR_IMPORT_BUTTON_LABEL = "Import selected work orders";
	public static final String DO_NOT_SHIP_EXISTING_ITEMS_CHECKBOX_NAME = "DONOTSHIPITEMSONORDER";
	public static final String DO_NOT_SHIP_EXISTING_ITEMS_CHECKBOX_LABEL = "Do <B><I>NOT</I></B> ship items already on the order";
	public static final String LOCATION_LIST = "LOCATIONLIST";
	public static final String CATEGORY_LIST = "CATEGORYLIST";
	private static final String TABLE_BACKGROUND_COLOR = "LightSteelBlue";
	private static final String EVEN_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static final String LOCATION_OPTION_USE_WORK_ORDER_LOCATIONS = "";
	public static final String LOCATION_LABEL_USE_WORK_ORDER_LOCATIONS = "Use work order locations";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Work orders",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMImportWorkOrdersAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditOrders
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		smedit.setTitle("Import " + smedit.getObjectName());
	    smedit.printHeaderTable();
		String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMTableorderheaders.strimmedordernumber, request);
		//Add a link to go back to the order:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + sTrimmedOrderNumber
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString()) + "\">" + "Return to order details" + "</A>"
		);
		
		smedit.setbIncludeUpdateButton(false);
		smedit.setbIncludeDeleteButton(false);
		if (bDebugMode){
			System.out.println("[1579272423] In " + this.toString() + " just before createEditPage.");
		}
	    try {
			smedit.createEditPage(getEditHTML(smedit, sTrimmedOrderNumber),"");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.getFullClassName(this.toString())
				+ "?" + SMTableorderheaders.strimmedordernumber + "=" + sTrimmedOrderNumber
				+ "&Warning=Error building import page - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		if (bDebugMode){
			System.out.println("[1579272428] In " + this.toString() + " just after createEditPage.");
		}
	    return;
	}

	private String getEditHTML(
			SMMasterEditEntry sm, 
			String sTrimmedOrderNumber 
	) throws Exception{

		String s = "";
		s += sStyleScripts();

		//Store the entry variables that we know at this point:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTableorderheaders.strimmedordernumber + "\" VALUE=\"" 
				+ sTrimmedOrderNumber + "\"" + "\">";
		//Order number
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sTrimmedOrderNumber);
		if (!order.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
			throw new Exception("Could not load order number " + sTrimmedOrderNumber + " - " + order.getErrorMessages());
		}
		s += "<B>Order #:</B>&nbsp;" + sTrimmedOrderNumber
			+ "&nbsp;<B>Bill to:</B>&nbsp;" + order.getM_sBillToName()
			+ "&nbsp;<B>Ship to:</B>&nbsp;" + order.getM_sShipToName()
        ;
    	
		//List the locations, defaulting to the order header location:
		s += "<BR>Ship imported items from this location:&nbsp;";
		String SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY " + SMTablelocations.sLocation
		;
		ArrayList<String>arrLocations = new ArrayList<String>(0);
		ArrayList<String>arrLocationDescriptions = new ArrayList<String>(0);
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sm.getsDBID(), 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".getEditHTML - user: " + sm.getUserID()
			+ " - "
			+ sm.getFullUserName()
				);
		while (rs.next()){
			arrLocations.add(rs.getString(SMTablelocations.sLocation));
			arrLocationDescriptions.add(rs.getString(SMTablelocations.sLocationDescription));
		}
		arrLocations.add(LOCATION_OPTION_USE_WORK_ORDER_LOCATIONS);
		arrLocationDescriptions.add("Use work order locations");
		rs.close();
		s += clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
			LOCATION_LIST, 
			arrLocations, 
			order.getM_sLocation(), 
			arrLocationDescriptions);
		
		//List the locations, defaulting to the order header location:
		s += "<BR>Set all imported items to this category:&nbsp;";
		SQL = "SELECT"
			+ " " + SMTableiccategories.sCategoryCode
			+ ", " + SMTableiccategories.sDescription
			+ " FROM " + SMTableiccategories.TableName
			+ " WHERE ("
				+ "(" + SMTableiccategories.iActive + " = 1)"
			+ ")"
			+ " ORDER BY " + SMTableiccategories.sCategoryCode
		;
		ArrayList<String>arrCategories = new ArrayList<String>(0);
		ArrayList<String>arrCategoryDescriptions = new ArrayList<String>(0);
		rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sm.getsDBID(), 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".getEditHTML listing categories - user: " + sm.getUserID()
			+ " - "
			+ sm.getFullUserName()
				);
		while (rs.next()){
			arrCategories.add(rs.getString(SMTableiccategories.sCategoryCode));
			arrCategoryDescriptions.add(rs.getString(SMTableiccategories.sCategoryCode) 
				+ " - " + rs.getString(SMTableiccategories.sDescription));
		}
		rs.close();
		s += clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
			CATEGORY_LIST, 
			arrCategories, 
			order.getM_sDefaultItemCategory(), 
			arrCategoryDescriptions);
		s += "<BR>";
		
		s += "<BR>";
		//Check box for shipping:
		s+= DO_NOT_SHIP_EXISTING_ITEMS_CHECKBOX_LABEL
			+ " <INPUT TYPE=CHECKBOX" 
			+ clsServletUtilities.CHECKBOX_CHECKED_STRING 
			+ " NAME=\"" + DO_NOT_SHIP_EXISTING_ITEMS_CHECKBOX_NAME + "\""
			+ "width=0.25>"
		;
		s += "<BR>";
    	try {
			s += listWorkOrders(sm, sTrimmedOrderNumber);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    	
    	s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_FOR_IMPORT_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_FOR_IMPORT_BUTTON_LABEL + "' STYLE='height: 0.24in'>";
		return s;
	}
	private String listWorkOrders(SMMasterEditEntry sm, String sTrimmedOrderNumber) throws Exception{
		String s = "";
		s += "<TABLE class= \"querylist \">";
		s += "<TR>";
		s += "<TH class = \" querylineheadingcenter \">Import?</TH>";
		s += "<TH class = \" querylineheadingright \">Work Order ID</TH>";
		s += "<TH class = \" querylineheadingleft \">Date</TH>";
		s += "<TH class = \" querylineheadingleft \">Technician</TH>";

		String SQL = "SELECT"
			+ " * FROM " + SMTableworkorders.TableName
			+ " WHERE ("
				+ "(" + SMTableworkorders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
				+ " AND (" + SMTableworkorders.iimported + " = 0)"
			+ ") ORDER BY " + SMTableworkorders.dattimedone
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(),
			sm.getsDBID(),
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".listWorkOrders - user: " + sm.getUserID()
			+ " - "
			+ sm.getFullUserName()
		);
		boolean bIsOddRow = false;
		int iLineCounter = 1;
		while (rs.next()){
			String sRowSuffix = "evenrow";
			if (bIsOddRow){
				sRowSuffix = "oddrow";
			}
			String sWorkOrderID = Long.toString(rs.getLong(SMTableworkorders.lid));
			String sLink =
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderAction?"
				+ SMWorkOrderEdit.COMMAND_FLAG + "=" + SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE
				+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
				+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sTrimmedOrderNumber
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() 
				+ "\">" + sWorkOrderID 
				+ "</A>"
			;
			
			s += "<TR><TD class = \" queryfieldcenteraligned" + sRowSuffix + "\">"
				+ "<INPUT TYPE=CHECKBOX "
				+ " NAME=\"" + CHECKBOX_BASE 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineCounter), "0", LENGTH_OF_LINE_COUNTER_PADDING)
					+ clsStringFunctions.PadLeft(sWorkOrderID, "0", LENGTH_OF_CHECKBOX_PADDING)
					 + "\""
				+ " id = \"" + CHECKBOX_BASE 
					+ clsStringFunctions.PadLeft(Integer.toString(iLineCounter), "0", LENGTH_OF_LINE_COUNTER_PADDING)
					+ clsStringFunctions.PadLeft(sWorkOrderID, "0", LENGTH_OF_CHECKBOX_PADDING) + "\""
				+ " width=0.25"
				+ ">"
				+ "</TD>"
				
				+ "<TD class = \" queryfieldrightaligned" + sRowSuffix + "\">"
				+ sLink
				+ "</TD>"
				
				+ "<TD class = \" queryfieldleftaligned" + sRowSuffix + "\">"
				+ clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableworkorders.dattimedone))
				+ "</TD>"
				
				+ "<TD class = \" queryfieldleftaligned" + sRowSuffix + "\">"
				+ rs.getString(SMTableworkorders.smechanicname)
				+ "</TD>"
				;
			bIsOddRow = !bIsOddRow;
			iLineCounter++;
		}
		rs.close();
		s += "</TABLE>";
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		//String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		//Layout table:
		s +=
			"table.querylist {"
			+ "border-width: " + "1" + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			//+ "border-collapse: separate; "
			//+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			+ "background-color: " + TABLE_BACKGROUND_COLOR + "; "
			+ "}"
			+ "\n"
			;

		s += "td.queryfieldleftalignedoddrow {"
			+ "height: " + sRowHeight + "; "
			//+ "vertical-align: bottom;"
			+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			//+ "color: black; "
			+ "}"
			+ "\n"
		;
		
		//This is the def for a right aligned ODD ROW field:
		s +=
			"td.queryfieldrightalignedoddrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned ODD ROW field:
		s +=
			"td.queryfieldcenteralignedoddrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;	
		
		//This is the def for a left aligned EVEN ROW field:
		s +=
			"td.queryfieldleftalignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for a right aligned EVEN ROW field:
		s +=
			"td.queryfieldrightalignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a center aligned EVEN ROW field:
		s +=
			"td.queryfieldcenteralignedevenrow {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + EVEN_ROW_BACKGROUND_COLOR + "; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: center; "
					//+ "color: black; "
			+ "}"
			+ "\n"
			;	
		
		//This is the def for the querylist lines heading:
		s +=
			"th.querylineheadingleft {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: black; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.querylineheadingright {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: black; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		s +=
			"th.querylineheadingcenter {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			//+ "background-color: #708090; "
			+ "background-color: black; "
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
