package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ICBulkTransferEdit  extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final String DARK_ROW_BG_COLOR = "#cceeff";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	private static final String sICBulkTransferEditCalledClassName = "ICBulkTransferAction";
	public static final int INITIAL_NUMBER_OF_ROWS = 25;
	public static final String PARAM_COMMAND = "COMMAND";
	public static final String PARAM_COMMAND_CREATE = "CREATEBATCH";
	public static final String PARAM_COMMAND_VALIDATE = "VALIDATEBATCH";
	public static final String PARAM_COMMAND_ADD_TEN_ROWS = "ADDTENROWS";
	private static final String CREATE_BATCH_LABEL = "Create batch";
	private static final String VALIDATE_BATCH_LABEL = "Validate batch";
	private static final String ADD_TEN_ROWS_LABEL = "Add " + Integer.toString(ICBulkTransferAction.BULK_TANSFER_NUMBER_OF_ROWS_TO_ADD) + " rows";
	public static final String PARAM_COMMAND_RESTOCK_FROM_SHIPMENTS = "RESTOCKFROMSHIPMENTS";
	public static final String RESTOCK_FROM_SHIPMENTS_LABEL = "Restock location(s)";
	public static final String STARTING_DATE_FIELD = "STARTINGDATE";
	public static final String LOCATION_PARAMETER = "RESTOCKLOCATION";
	public static final String RESTOCK_FROM_LOCATION_PARAMETER = "RESTOCKFROMLOCATION";

	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICBulkTransfers
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    ICEntry entry = new ICEntry();
	    entry.sBatchType(Integer.toString(ICEntryTypes.TRANSFER_ENTRY));
	    if ((ICEntry)CurrentSession.getAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY) != null){
	    	entry = (ICEntry)CurrentSession.getAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY);
	    	CurrentSession.removeAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY);
	    }
	    
	    //We always want the ICEntry to display a minimum number of lines:
	    int iNumberOfRowsToDisplay = INITIAL_NUMBER_OF_ROWS;
	    String sNumberOfRows = clsManageRequestParameters.get_Request_Parameter(ICBulkTransferAction.PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request);
	    try {
			iNumberOfRowsToDisplay = Integer.parseInt(sNumberOfRows);
		} catch (NumberFormatException e) {
			//If we can't read this, we'll drop back to the initial number of rows...
		}
	    
	    for (int i = entry.getLineCount(); i < iNumberOfRowsToDisplay; i++){
	    	ICEntryLine line = new ICEntryLine();
	    	line.setQtyString("1.0000");
	    	entry.add_line(line);
	    }
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    
	    String sScript = "";
		
		sScript += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		sScript += "<script type=\"text/javascript\">\n";
		
		sScript += "function createbatch() {\n";
		sScript += "    document.getElementById(\"" + PARAM_COMMAND + "\").value = \"" 
			+ PARAM_COMMAND_CREATE + "\";\n";
		sScript += "    document.forms[\"MAINFORM\"].submit();\n";
		sScript += "    return;\n";
		sScript += "}\n";

		sScript += "function validatetransfers() {\n";
		sScript += "    document.getElementById(\"" + PARAM_COMMAND + "\").value = \"" 
			+ PARAM_COMMAND_VALIDATE + "\";\n";
		sScript += "    document.forms[\"MAINFORM\"].submit();\n";
		sScript += "    return;\n";
		sScript += "}\n";
		
		sScript += "function addtenrows() {\n";
		sScript += "    document.getElementById(\"" + PARAM_COMMAND + "\").value = \"" 
			+ PARAM_COMMAND_ADD_TEN_ROWS + "\";\n";
		sScript += "    document.forms[\"MAINFORM\"].submit();\n";
		sScript += "    return;\n";
		sScript += "}\n";
		
		sScript += "function restockfromshipments() {\n";
		sScript += "    document.getElementById(\"" + PARAM_COMMAND + "\").value = \"" 
			+ PARAM_COMMAND_RESTOCK_FROM_SHIPMENTS + "\";\n";
		sScript += "    document.forms[\"MAINFORM\"].submit();\n";
		sScript += "    return;\n";
		sScript += "}\n";
		
		sScript += "</script>\n";
	    
		out.println(sScript);
		
	    String title = "Enter bulk item transfers.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B>" + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPrintUPCLabels) 
	    		+ "\">Summary</A><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic." + sICBulkTransferEditCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + PARAM_COMMAND + "\" VALUE=\"" + "" + "\""
	    		+ " id=\"" + PARAM_COMMAND + "\"" + "\">");
	    
	    //Add the default values for the ICEntry values:
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamBatchType + "' VALUE='" + entry.sBatchType() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamBatchNumber + "' VALUE='" + entry.sBatchNumber() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamEntryNumber + "' VALUE='" + entry.sEntryNumber() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamEntryType + "' VALUE='" + entry.sEntryType() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamDocNumber + "' VALUE='" + entry.sDocNumber() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamEntryDescription + "' VALUE='" + entry.sEntryDescription() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamEntryDate + "' VALUE='" + entry.sStdEntryDate() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICEntry.ParamNumberOfLines + "' VALUE='" + Integer.toString(entry.getLineCount()) + "'>");
	    
	    //This tells the program how many rows to actually DISPLAY on the screen:
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICBulkTransferAction.PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS 
	    	+ "' VALUE='" + Integer.toString(entry.getLineCount()) + "'>");
	    
		out.println("\n" + sStyleScripts() + "\n");
		
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICBulkTransferAction.PARAM_BULK_TANSFER_CALLINGCLASS + "' VALUE=\"" + this.getClass().getName() + "\">");
		
	    String sItemNumber = "";
	    String sQuantity = "";
	    String sFromLocation = "";
	    String sToLocation = "";
	    String sUOM = "";
	    String sItemDesc = "";
	    String sQtyOnHandAtFromLocation = "";
	    String sQtyOnHandAtToLocation = "";
	    int iCounter = 0;
	    
	    out.println("<TABLE class = \" basicwithborder \" >");
	    
	    //Headings:
	    boolean bShowInfoFields = true;
	    out.println(createHeaderRow(bShowInfoFields));
	    
	    //Get a connection here:
	    Connection conn = null;
	    try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + " - user: " + sUserName);
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>Error [1475774076] getting connection - " + e.getMessage() + ".</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
	    
	    for (int i = 0;i < entry.getLineCount(); i++){
			out.println("<TR style = \" background-color: " + LIGHT_ROW_BG_COLOR +  ";  \">");

    	    sQuantity = entry.getLineByIndex(i).sQtySTDFormat();
			if (sQuantity.compareToIgnoreCase("") == 0){
    	    	sQuantity = "1.0000";
    	    }
    
    	    out.println("<TD class = \" centerjustifiedcell \" style = \" background-color: " + DARK_ROW_BG_COLOR +  ";  \">" +  clsStringFunctions.PadLeft(Integer.toString(iCounter + 1), "0", 3) + "</TD>");
    	    out.println("<TD class = \" centerjustifiedcell \"><INPUT TYPE=TEXT NAME=\"" + ICEntryLine.ParamLineQty
    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
    	    		+ sQuantity + "\"SIZE=16 MAXLENGTH=13 STYLE="
    	    		+ "\"width: 1in; height: 0.25in\"></TD>");
    	    
    	    //IF there's an item number, we'll read the item description:
    	    boolean bValidItemFound = false;
    	    
    	    //Set the defaults:
	    	sQtyOnHandAtFromLocation = "";
	    	sQtyOnHandAtToLocation = "";
			sUOM = "";
			sItemDesc = "";
    	    
    	    if(entry.getLineByIndex(i).sItemNumber().trim().compareToIgnoreCase("") != 0){
    	    	String SQL = "SELECT"
    	    		+ " " + SMTableicitems.sCostUnitOfMeasure
    	    		+ ", " + SMTableicitems.sItemDescription
    	    		+ " FROM " + SMTableicitems.TableName
    	    		+ " WHERE ("
    	    			+ "(" + SMTableicitems.sItemNumber + " = '" + entry.getLineByIndex(i).sItemNumber().trim() + "')"
    	    		+ ")"
    	    	;
    	    	try {
					ResultSet rsItems = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsItems.next()){
						sUOM = rsItems.getString(SMTableicitems.sCostUnitOfMeasure);
						sItemDesc = rsItems.getString(SMTableicitems.sItemDescription);
						bValidItemFound = true;
					}else{
						sUOM = "<FONT COLOR=RED>N/A</FONT>";
						sItemDesc = "<FONT COLOR=RED>ITEM NOT FOUND</FONT>";
					}
					rsItems.close();
				} catch (SQLException e) {
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080780]");
					out.println("<BR><B><FONT COLOR=RED>Error [1475774077] getting reading item with SQL: " + SQL 
							+ " - " + e.getMessage() + ".</FONT></B><BR>");
					out.println("</BODY></HTML>");
					return;
				}
    	    }
    	    
    	    out.println("<TD class = \" leftjustifiedcell \"  style = \" background-color: " + DARK_ROW_BG_COLOR +  ";  \">" 
    	    	+ sUOM + "</TD>");
    	    
    	    sItemNumber = entry.getLineByIndex(i).sItemNumber();
    	    out.println("<TD class = \" centerjustifiedcell \"><INPUT TYPE=TEXT NAME=\"" + ICEntryLine.ParamLineItemNumber
    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
    	    		+ sItemNumber + "\"SIZE=24 MAXLENGTH=24 STYLE="
    	    		+ "\"width: 1.2in; height: 0.25in\"></TD>");
    	    
    	    out.println("<TD class = \" leftjustifiedcell \"  style = \" background-color: " + DARK_ROW_BG_COLOR +  ";  \">" + sItemDesc + "</TD>");
    	    
    	    sFromLocation = entry.getLineByIndex(i).sLocation();
    	    out.println("<TD class = \" centerjustifiedcell \"><INPUT TYPE=TEXT NAME=\"" + ICEntryLine.ParamLineLocation 
    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
    	    		+ sFromLocation + "\"SIZE=24 MAXLENGTH=24 STYLE="
    	    		+ "\"width: 1.2in; height: 0.25in\"></TD>");
    	    
    	    if (bValidItemFound == true){
    	    	String SQL = "SELECT"
    	    		+ " " + SMTableicitemlocations.sQtyOnHand
    	    		+ " FROM " + SMTableicitemlocations.TableName
    	    		+ " WHERE ("
    	    			+ "(" + SMTableicitemlocations.sItemNumber + " = '" + entry.getLineByIndex(i).sItemNumber().trim() + "')"
    	    			+ " AND (" + SMTableicitemlocations.sLocation + " = '" + entry.getLineByIndex(i).sLocation().trim() + "')"
    	    		+ ")"
    	    	;
    	    	BigDecimal bdQtyOnHandAtFromLocation = new BigDecimal("0.0000");
    	    	try {
					ResultSet rsFromLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsFromLocation.next()){
						bdQtyOnHandAtFromLocation = rsFromLocation.getBigDecimal(SMTableicitemlocations.sQtyOnHand);
						sQtyOnHandAtFromLocation = clsManageBigDecimals.BigDecimalToScaledFormattedString(
								4, bdQtyOnHandAtFromLocation);
					}else{
						sQtyOnHandAtFromLocation = "<FONT COLOR=RED>0.0000</FONT>";
					}
					rsFromLocation.close();
				} catch (SQLException e) {
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080781]");
					out.println("<BR><B><FONT COLOR=RED>Error [1475774078] getting reading FROM location info with SQL: " + SQL 
							+ " - " + e.getMessage() + ".</FONT></B><BR>");
					out.println("</BODY></HTML>");
					return;
				}
				if (bdQtyOnHandAtFromLocation.compareTo(
					new BigDecimal(entry.getLineByIndex(i).sQtySTDFormat().replace(",", ""))) < 0){
					sQtyOnHandAtFromLocation = "<FONT COLOR=RED>" + sQtyOnHandAtFromLocation + "</FONT>";
				}
    	    	
    	    	SQL = "SELECT"
        	    		+ " " + SMTableicitemlocations.sQtyOnHand
        	    		+ " FROM " + SMTableicitemlocations.TableName
        	    		+ " WHERE ("
        	    			+ "(" + SMTableicitemlocations.sItemNumber + " = '" + entry.getLineByIndex(i).sItemNumber().trim() + "')"
        	    			+ " AND (" + SMTableicitemlocations.sLocation + " = '" + entry.getLineByIndex(i).sTargetLocation() + "')"
        	    		+ ")"
        	    	;
    	    	BigDecimal bdQtyOnHandAtToLocation = new BigDecimal("0.0000");
        	    	try {
    					ResultSet rsToLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
    					if (rsToLocation.next()){
    						bdQtyOnHandAtToLocation = rsToLocation.getBigDecimal(SMTableicitemlocations.sQtyOnHand);
    						sQtyOnHandAtToLocation = clsManageBigDecimals.BigDecimalToScaledFormattedString(
    								4, bdQtyOnHandAtToLocation);
    					}else{
    						sQtyOnHandAtToLocation = "<FONT COLOR=RED>0.0000</FONT>";
    					}
    					rsToLocation.close();
    				} catch (SQLException e) {
    					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080782]");
    					out.println("<BR><B><FONT COLOR=RED>Error [1475774079] getting reading TO location info with SQL: " + SQL 
    							+ " - " + e.getMessage() + ".</FONT></B><BR>");
    					out.println("</BODY></HTML>");
    					return;
    				}
    	    }
    	    
    	    out.println("<TD class = \" rightjustifiedcell \"  style = \" background-color: " + DARK_ROW_BG_COLOR +  ";  \">" + sQtyOnHandAtFromLocation + "</TD>");
    	    
    	    sToLocation = entry.getLineByIndex(i).sTargetLocation();
    	    out.println("<TD class = \" centerjustifiedcell \"><INPUT TYPE=TEXT NAME=\"" + ICEntryLine.ParamLineTargetLocation 
    	    		+ Integer.toString(iCounter) + "\" VALUE=\""
    	    		+ sToLocation + "\"SIZE=24 MAXLENGTH=24 STYLE="
    	    		+ "\"width: 1.2in; height: 0.25in\"></TD>");
    	    
    	    out.println("<TD class = \" rightjustifiedcell \"  style = \" background-color: " + DARK_ROW_BG_COLOR +  ";  \">" + sQtyOnHandAtToLocation + "</TD>");
	    	    
	    	out.println("</TR>");
	    	iCounter = iCounter + 1;
	    }
	    
	    out.println("</TABLE>");
	    
	    //add java script to this button press to check for label total
	    out.println("<BR><button type=\"button\""
				+ " value=\"" + PARAM_COMMAND_CREATE + "\""
				+ " name=\"" + PARAM_COMMAND_CREATE + "\""
				+ " onClick=\"createbatch();\">" + CREATE_BATCH_LABEL + "</button>\n");

	    out.println("&nbsp;<button type=\"button\""
				+ " value=\"" + PARAM_COMMAND_VALIDATE + "\""
				+ " name=\"" + PARAM_COMMAND_VALIDATE + "\""
				+ " onClick=\"validatetransfers();\">" + VALIDATE_BATCH_LABEL + "</button>\n");
	    
	    out.println("&nbsp;<button type=\"button\""
				+ " value=\"" + PARAM_COMMAND_ADD_TEN_ROWS + "\""
				+ " name=\"" + PARAM_COMMAND_ADD_TEN_ROWS + "\""
				+ " onClick=\"addtenrows();\">" + ADD_TEN_ROWS_LABEL + "</button>\n");

	    //Add controls for restocking a location:
	    
	    out.println("<BR><BR>");
	    out.println("<TABLE class = \" basicwithborder \" >");
	    
	    out.println("<TR><TD><B><U>Re-stocking</U></B></TD></TR>");
	    out.println("<TR><TD>Re-stock items that were recently shipped from these locations:<BR>");
	    
	    //List the locations:
		//select location
		String SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY "  + SMTablelocations.sLocation
		;
		try {
			ResultSet rsLocations = clsDatabaseFunctions.openResultSet(SQL, conn);
			String sChecked = "";
			while(rsLocations.next()){
				String sLocation = rsLocations.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
				out.println("<INPUT TYPE=CHECKBOX NAME=\"" + LOCATION_PARAMETER 
					+ sLocation + "\""
				);
				if (request.getParameter(LOCATION_PARAMETER + sLocation) != null){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				out.println(" " + sChecked + " "
					+ " width=0.25>" 
					+ sLocation + " - "
					+ rsLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription) + "<BR>");
			}
			rsLocations.close();
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080783]");
			out.println("<BR><B><FONT COLOR=RED>Error [1475774089] getting locations with SQL: " + SQL 
					+ " - " + e.getMessage() + ".</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		out.println("</TD></TR>");
		
		//Read them again for the 'restock from' location:
	    out.println("<TR><TD>Take the 'restocking' items from THIS location:");
	    
	    //List the locations:
	    SQL = "SELECT"
			+ " " + SMTablelocations.sLocation
			+ ", " + SMTablelocations.sLocationDescription
			+ " FROM " + SMTablelocations.TableName
			+ " ORDER BY "  + SMTablelocations.sLocation
		;

		out.println("<SELECT NAME = \"" + RESTOCK_FROM_LOCATION_PARAMETER + "\">");
		String s = "";
	    try {
			ResultSet rsLocations = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsLocations.next()){
				String sLocation = rsLocations.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
				s += "<OPTION";
				if (clsManageRequestParameters.get_Request_Parameter(RESTOCK_FROM_LOCATION_PARAMETER, request).compareToIgnoreCase(sLocation) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sLocation + "\">"
					+ sLocation + " - " + rsLocations.getString(SMTablelocations.sLocationDescription)
					+ "</OPTION>"
					+ rsLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription);
			}
			rsLocations.close();
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080784]");
			out.println("<BR><B><FONT COLOR=RED>Error [1475774089] getting locations with SQL: " + SQL 
					+ " - " + e.getMessage() + ".</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080785]");
	    out.println(s);
	    out.println("</SELECT>");
		out.println("</TD></TR>");
	    
	    String sStartingDate = clsManageRequestParameters.get_Request_Parameter(STARTING_DATE_FIELD, request);
	    out.println("<TR><TD>ONLY include shipments starting on:&nbsp;"
	    		+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_DATE_FIELD, 
						sStartingDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(STARTING_DATE_FIELD, getServletContext())
	    		+ "</TD></TR>");
	    
	    out.println("<TR><TD>&nbsp;<button type=\"button\""
				+ " value=\"" + PARAM_COMMAND_RESTOCK_FROM_SHIPMENTS + "\""
				+ " name=\"" + PARAM_COMMAND_RESTOCK_FROM_SHIPMENTS + "\""
				+ " onClick=\"restockfromshipments();\">" + RESTOCK_FROM_SHIPMENTS_LABEL + "</button></TD></TR>");
	    
	    out.println("</TABLE>");
	    
	    out.println("</FORM>");
		
		out.println("</BODY></HTML>");
	}
	private String createHeaderRow(boolean bShowInfoHeadings){
		String s = "";

		s += "<TR style = \" background-color: LightGray; color: black; \" >";

		String sUOM = "";
		String sItemDesc = "";
		String sFromLocationQtyOnHand = "";
		String sToLocationQtyOnHand = "";
		
		if (bShowInfoHeadings){
			sUOM = "U/M";
			sItemDesc = "Description";
			sFromLocationQtyOnHand = "Qty O/H";
			sToLocationQtyOnHand = "Qty O/H";
		}
		s += 
			"<TD class = \" rightjustifiedheading \" >" + "Row" + "</TD>"
					+ "<TD class = \" leftjustifiedheading \" >" + "Quantity" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + sUOM + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "Item" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + sItemDesc + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "FROM<BR>location" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + sFromLocationQtyOnHand + "</TD>"
			+ "<TD class = \" centerjustifiedheading \" >" + "TO<BR>location" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + sToLocationQtyOnHand + "</TD>"
			+ "</TR>"
		;
		return s;
	}
	private String sStyleScripts(){
		String s = "";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
				"table.basic {"
				//+ "border-width: 0px; "
				//+ "border-spacing: 2px; "
				//+ "border-style: outset; "
				//+ "border-style: solid; "
				//+ "border-style: none; "
				//+ "border-color: black; "
				+ "border-collapse: collapse; "
				+ "width: 100%; "
				+ "font-size: " + "small" + "; "
				+ "font-family : Arial; "
				+ "color: black; "
				//+ "background-color: white; "
				+ "}"
				+ "\n"
				;

		s +=
				"table.basicwithborder {"
						+ "border-width: 1px; "
						+ "border-spacing: 2px; "
						+ "border-style: outset; "
						+ "border-style: solid; "
						//+ "border-style: none; "
						+ "border-color: black; "
						+ "border-collapse: collapse; "
						//+ "width: 100%; "
						+ "font-size: " + "medium" + "; "
						+ "font-family : Arial; "
						+ "color: black; "
						//+ "background-color: white; "
						+ "}"
						+ "\n"
						;
		/*
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
		 */
		//This is the def for a table cell, left justified:
		s +=
				"td.leftjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						+ "border: 1px solid; "
						//+ "border-style: none; "
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
						+ "text-align: left; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//style= \" word-wrap:break-word; \"
		//style= \" word-wrap:normal; white-space:pre-wrap; \" 
		s +=
				"td.leftjustifiedcellforcewrap {"
						+ "height: " + sRowHeight + "; "
						//+ "border: 0px solid; "
						//+ "border-style: none; "
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
						+ "text-align: left; "
						+ "color: black; "
						+ "word-wrap:break-word; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;
		//This is the def for a table cell, right justified:
		s +=
				"td.rightjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						+ "border: 1px solid; "
						//+ "border-style: none; "
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
						+ "text-align: right; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//This is the def for a table cell, center justified:
		s +=
				"td.centerjustifiedcell {"
						+ "height: " + sRowHeight + "; "
						+ "border: 1px solid; "
						//+ "border-style: none; "
						+ "padding: 2px; "
						//+ "border-color: " + CELL_BORDER_COLOR + "; "
						+ "vertical-align: center;"
						+ "font-family : Arial; "
						+ "font-weight: bold; "
						+ "font-size: small; "
						+ "text-align: center; "
						+ "color: black; "
						//+ "height: 50px; "
						+ "}"
						+ "\n"
						;

		//This is the def for a left-aligned heading on a table:
		s +=
				"td.leftjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
				+ "font-weight: bold; "
				+ "text-align: left; "
				+ "vertical-align:bottom; "
				+ "}"
				+ "\n"
				;

		//This is the def for a right-aligned heading on a table:
		s +=
				"td.rightjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
				+ "font-weight: bold; "
				+ "text-align: right; "
				+ "vertical-align:bottom; "
				+ "}"
				+ "\n"
				;

		//This is the def for a center-aligned heading on a table:
		s +=
				"td.centerjustifiedheading {"
				//+ "border: 0px solid; "
				+ "border-style: none; "
				//+ "bordercolor: 000; "
				+ "padding: 2px; "
				+ "font-family : Arial; "
				+ "font-size: medium; "
				+ "font-weight: bold; "
				+ "text-align: center; "
				+ "vertical-align:bottom; "
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
