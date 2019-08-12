package SMClasses;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smap.APVendor;
import smar.ARCallSheet;
import smar.ARDocumentTypes;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMBidEntry;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;
import smic.ICPOHeader;
import smic.ICPOInvoice;
import smic.ICPOReceiptHeader;

public class FinderResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public static final String FINDER_OBJECT_NAME_PARAM = "ObjectName";
	public static final String FINDER_RESULT_CLASS_PARAM = "ResultClass";
	public static final String FINDER_DOC_TYPE_PARAM = "DocumentType";
	public static final String FINDER_SEARCHING_CLASS_PARAM = "SearchingClass";
	public static final String FINDER_RETURN_FIELD_PARAM = "ReturnField";
	public static final String FINDER_PARAMETER_STRING_PARAM = "ParameterString";
	public static final String RESULT_LIST_FIELD = "ResultListField";
	public static final String RESULT_LIST_HEADING = "ResultHeading";
	public static final String RESULT_FIELD_ALIAS = "ResultFieldAlias";
	public static final String OBJECT_ORDER_EXTENDED = "Order (Extended)";
	public static final String OBJECT_ORDER_BY_DETAIL = "Order (by Detail)";
	public static final String SEARCH_ITEM = "Item";
	public static final String SEARCH_MOSTUSEDITEMS = "Items Listed By Usage";
	public static final String SEARCH_NONDEDICATEDITEMS = "Non-dedicated Items";
	public static final String SEARCH_ITEMS_SHOWING_LOCATION_QTYS = "Items Listing Qtys By Location";
	public static final String SEARCH_ACTIVE_ITEM = "ACTIVE Item";
	public static final String COMPLETE_BILL_TO_ADDRESS = "COMPLETEBILLTOADDRESS";
	public static final String COMPLETE_SHIP_TO_ADDRESS = "COMPLETESHIPTOADDRESS";
	public static final String UNINVOICED_PO_RECEIPT_OBJECT = "Uninvoiced PO Receipt";
	public static final String ITEMS_WITH_VENDOR_ITEMS = "Items With Vendor Item Numbers";
	public static final String ASSET = "Asset";
	
	//These are reserved field names, which are used as aliases for more complex field calculations:
	public static final String ITEM_LOCATION_QTY_OH = "ITEMLOCATIONQTYONHAND";
	public static final String ITEM_NON_STOCK_FLAG = "ITEMNONSTOCKFLAG";
	
	
	//This is used if we want to further limit the query - for example by stipulating only one particular vendor when we are searching for AP transactions, etc.
	public static final String ADDITIONAL_WHERE_CLAUSE_PARAMETER = "ADDITIONALWHERECLAUSE";
	//This is used to give the finder box a special title, e.g.: 'List of AP Transactions for vendor number OHD02'
	public static final String FINDER_BOX_TITLE = "FINDERBOXTITLE";
	
	//private static final String TABLE_RESULTS_ID = "RESULTSTABLE";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request);
		
		// TJR - 6/4/2019 - removed this - DBID should always be passed in through the request:
		//String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		

		//Get the parameters:
		String sObjectName = clsManageRequestParameters.get_Request_Parameter(FINDER_OBJECT_NAME_PARAM, request);
		String sSearchingClass = clsManageRequestParameters.get_Request_Parameter(FINDER_SEARCHING_CLASS_PARAM, request);
		String sDocumentType = clsManageRequestParameters.get_Request_Parameter(FINDER_DOC_TYPE_PARAM, request);
		String sReturnField = clsManageRequestParameters.get_Request_Parameter(FINDER_RETURN_FIELD_PARAM, request);

		String sParameterString = "";
		if (request.getParameter(FINDER_PARAMETER_STRING_PARAM) != null){
			sParameterString = (String) request.getParameter(FINDER_PARAMETER_STRING_PARAM);
		}

		String title = sObjectName + " search results.";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(sCommandScripts());
		//Print a link to the first page after login:
		//out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin\">Return to user login</A><BR>");
		//out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu\">Return to Accounts Receivable Main Menu</A><BR><BR>");

		//Get the fields and headings needed to list the results:
		//Store the display fields and headings for the results page:
		ArrayList<String> sResultListFields = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_LIST_FIELD + Integer.toString(i)) != null){
				sResultListFields.add((String) request.getParameter(RESULT_LIST_FIELD + Integer.toString(i)));
			}
		}
		if (sResultListFields.size() == 0){
			out.println("<BR>Error [1429302419] - sResultListFields.size() = 0,"
				+ " Object name = '" + sObjectName + "'" 
				+ ", SearchingClass = '" + sSearchingClass + "'"
				+ ".<BR>");
			return;
		}
		ArrayList<String> sResultFieldAliases = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_FIELD_ALIAS + Integer.toString(i)) != null){
				sResultFieldAliases.add((String) request.getParameter(RESULT_FIELD_ALIAS + Integer.toString(i)));
			}else{
				sResultFieldAliases.add("");
			}
		}
		if (bDebugMode){
			for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
				System.out.println("[1352731763] - sResultFieldAliases.get(" + i + ") = " + sResultFieldAliases.get(i));
			}
		}
		ArrayList<String> sDisplayHeadings = new ArrayList<String>();
		for (int i = 0; i < ObjectFinder.MAX_NUMBER_OF_RESULT_FIELDS; i++){
			if (request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)) != null){
				sDisplayHeadings.add((String) request.getParameter(RESULT_LIST_HEADING + Integer.toString(i)));
			}
		}

		String sSearchField = request.getParameter("sSearchField");
		String sSearchType = request.getParameter("sSearchType");
		String sSearchText = request.getParameter("sSearchString");
		String sSQL = "";

		boolean bUsedSpecialSQL = false;
		if (
				(sObjectName.equalsIgnoreCase(ICPOHeader.ParamObjectName))
				 || (sObjectName.equalsIgnoreCase("OPEN " + ICPOHeader.ParamObjectName))
				
		){
			sSQL = buildICPOSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText,
					sObjectName
			);
			bUsedSpecialSQL = true;
		}
		
		if (sObjectName.equalsIgnoreCase(ICPOHeader.ParamObjectName  + " by Item")){
			sSQL = buildICPOByItemSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText,
					sObjectName
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(SMBidEntry.ParamObjectName)){
			sSQL = buildSMBIDSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(OBJECT_ORDER_EXTENDED)){
			sSearchText = sSearchText.trim();
			if (sSearchText.compareToIgnoreCase("") == 0){
				out.println("<BR>Error [1499696333] - the 'Extended' search requires some actual text for searching - it cannot search for an empty string.<BR>\n");
				out.println("</TABLE>\n<BR>");
				out.println("</BODY></HTML>");
				return;
			}
			sSQL = buildSMExtendedOrderSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(OBJECT_ORDER_BY_DETAIL)){
			sSQL = buildSMOrderDetailSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(UNINVOICED_PO_RECEIPT_OBJECT)){
			sSQL = buildUninvoicedPOReceiptSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(ITEMS_WITH_VENDOR_ITEMS)){
			sSQL = buildItemWithVendorItemSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(SEARCH_MOSTUSEDITEMS)){
			sSQL = buildMostUsedItemSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}
	
		if (sObjectName.equalsIgnoreCase(SEARCH_NONDEDICATEDITEMS)){
			sSQL = buildNonDedicatedItemSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}
		
		if (sObjectName.equalsIgnoreCase(SEARCH_ITEMS_SHOWING_LOCATION_QTYS)){
			sSQL = buildItemsWithLocationQtysSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText,
					clsManageRequestParameters.get_Request_Parameter(FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER, request)
			);
			bUsedSpecialSQL = true;
		}
		
		if (sObjectName.equalsIgnoreCase(SMTableproposals.ObjectName)){
			sSearchText = sSearchText.trim();
			if (sSearchText.compareToIgnoreCase("") == 0){
				out.println("<BR>Error [1499696334] - this search requires some actual text for searching - it cannot search for an empty string.<BR>\n");
				out.println("</TABLE>\n<BR>");
				out.println("</BODY></HTML>");
				return;
			}
			sSQL = buildProposalSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText,
					sUserFullName,
					sCompanyName
			);
			bUsedSpecialSQL = true;
		}
		
		if (sObjectName.equalsIgnoreCase(SMTaxCertificate.ParamObjectName )){
			sSQL = buildTaxCertificateSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}
		
		if (sObjectName.equalsIgnoreCase(SMTablesalescontacts.OBJECT_NAME)){
			sSQL = buildSalesContactWithSalesPersonSQLStatement(
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText
			);
			bUsedSpecialSQL = true;
		}

		if (sObjectName.equalsIgnoreCase(SMTableaptransactions.OBJECT_NAME)){
			sSQL = buildAPTransactionsSQLStatement(
				sResultListFields,
				sSearchType,
				sSearchField,
				sSearchText,
				clsManageRequestParameters.get_Request_Parameter(FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER, request)
			);
			bUsedSpecialSQL = true;
		}
		
		if (!bUsedSpecialSQL){
			sSQL = buildStdSQLStatement(
					sObjectName,
					sDocumentType,
					sResultListFields,
					sSearchType,
					sSearchField,
					sSearchText,
					clsManageRequestParameters.get_Request_Parameter(FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER, request)
			);
		}
		
		//System.out.println("[1491249950] sSQL = '" + sSQL + "'");
		
	    //Unless told not to, print a link to the first page after login:
		if (clsManageRequestParameters.get_Request_Parameter(ObjectFinder.DO_NOT_SHOW_MENU_LINK, request).compareToIgnoreCase("") == 0){
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		}
		
		//If there is a 'special title' for the finder box, print it here:
		if (clsManageRequestParameters.get_Request_Parameter(FINDER_BOX_TITLE, request).compareToIgnoreCase("") != 0){
			out.println("<BR><B><I>" + clsManageRequestParameters.get_Request_Parameter(FINDER_BOX_TITLE, request) + "</I></B>\n");
		}
		
		//Now set up the table and header row:
		out.println("<TABLE WIDTH=100% BGCOLOR=\"#FFFFFF\" CELLSPACING=2 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n");
		//out.println("<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\"" 
		//	+ " ID=\"" + TABLE_RESULTS_ID + "\""
		//	+ ">\n");

		//Display the headings:
		out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" >\n");
		//out.println("  <TR>\n");
		for (int i = 0; i<sDisplayHeadings.size(); i++){
			out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \""
				//The javascript sort routine is WAY too slow for any long tables:
				//+ "	style = \"cursor: pointer;\" onclick=\"sortResultsTable(" + Integer.toString(i) + ");\"
				+ ">"
				+ "<B><FONT SIZE=2>" + sDisplayHeadings.get(i) + "</FONT></B></TD>\n"
			);
		}
		out.println("  </TR>\n");	    	

		//Now run the query:
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".doPost - user: '" + sUserID + " - " + sUserFullName + "', company: '" + sCompanyName + "'");
			//Now list the records:
			String sFieldName = "";
			int iCount =0;
			while(rs.next()){
				//for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
					//System.out.println("[1491249955]");
					//System.out.println("[1491249951] field type = '" + rs.getMetaData().getColumnType(i) + "'");
					//System.out.println("[1491249954] field label = '" + rs.getMetaData().getColumnLabel(i) + "'");
				//}
				
				//if (bOddRow){
				//	out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + "\" >\n");
				//}else{
				//	out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + "\" >\n");
				//}
				
				//We assume the first field returns the value we are looking for, so we make the first
				//field a link, which goes back, ultimately, to the class that called the search:
				sFieldName = (String) sResultListFields.get(0);
				//If there is an alias for the result field, use that:
				//The aliased field name is indexed one HIGHER, because in the parameters we pass to the previous function,
				//there is never a 'FieldAlias0' - it is one-based.
				if (sResultFieldAliases.get(1).compareToIgnoreCase("") != 0){
					sFieldName = (String) sResultFieldAliases.get(1);
				}
				String sFieldValue = rs.getString(sFieldName);
				if (sFieldValue == null){
					sFieldValue = "";
				}
				if(iCount % 2 ==0) {
					out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + " \" >\n");
				}else {
					out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + " \" >\n");
				}
				if (sReturnField.compareToIgnoreCase("") != 0){
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"    	
						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
						+ sSearchingClass + "?"
						+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + sReturnField + "=" 
						+ clsServletUtilities.URLEncode(sFieldValue.trim())
						//We have to put the '=' back after encoding the string: - TJR - 7/19/2011
						+ clsServletUtilities.URLEncode(sParameterString).replace("*", "&").replace("%3D", "=")
						+ "\">"
						+ sFieldValue.trim()
						+ "</A></FONT></TD>\n");
				}else{
					//If there IS no return field, don't display a link:
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"    	
						+ sFieldValue.trim()
						+ "</FONT></TD>\n");
				}
				
				//Now print the rest of the fields:
				String sDataValue = "";
				for (int i=1;i<sResultListFields.size();i++){
					out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
						+ "<FONT SIZE=2>"); 
					sFieldName = (String) sResultListFields.get(i);
					//System.out.println("[1491249952] sFieldName = '" + sFieldName + "'");
					//The aliased field name is indexed one HIGHER, because in the parameters we pass to the previous function,
					//there is never a 'FieldAlias0' - it is one-based.
					String sFieldAlias = (String) sResultFieldAliases.get(i + 1);
					//If there is an alias for the result field, use that:
					if (sFieldAlias.compareToIgnoreCase("") != 0){
						sFieldName = sFieldAlias;
					}
					//System.out.println("[1491249956] sFieldName = '" + sFieldName + "'");
					if (rs.getString(sFieldName) == null || rs.getString(sFieldName).compareTo("") == 0){
						out.println("&nbsp;");
					}else{
						sDataValue = rs.getString(sFieldName).trim();
						if (sFieldName.compareToIgnoreCase(sSearchField) == 0){
							out.println(
									highlightResult(sDataValue, sSearchText)
								//sDataValue.replaceAll(
								//	"(?i)" + sSearchText, "<span style=\"background-color: #FFFF66\">"
								//	+ sSearchText + "</span>")
							);
							// <span style="background-color: #FFFF66">selected text</span>
						}else{
							out.println(sDataValue);
						}
					}
					out.println("</FONT></TD>\n");
				}
				out.println("  </TR>\n");
				iCount++;
			}
			rs.close();
		}catch (Exception ex){
			out.println("Error [1481731164] - could not execute search query with SQL: " + sSQL 
				+ " - " + ex.getMessage() + ".<BR>");
			clsServletUtilities.sysprint(this.toString(), sUserFullName, "Error [1481731165] - could not execute search query with SQL: " + sSQL 
				+ " - " + ex.getMessage() + ".");
		}

		out.println("</TABLE>\n<BR>");
		out.println("</BODY></HTML>");
	}

	private String buildTaxCertificateSQLStatement(
			ArrayList<String> sResultListFields, String sSearchType,
			String sSearchField, String sSearchText) {
		String sSQL = "";
		
		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			sSQL += ", " + sResultListFields.get(i);
		}
		
		sSQL += " FROM " + SMTabletaxcertificates.TableName 
				+ " LEFT JOIN " + SMTablearcustomer.TableName 
				+ " ON " + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomernumber
				+ " = " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
				+ " LEFT JOIN " + SMTableorderheaders.TableName 
				+ " ON " + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.sjobnumber
				+ "=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber;
		
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}
		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.lid;
		return sSQL;
	}

	private String buildSalesContactWithSalesPersonSQLStatement(
			ArrayList<String> sResultListFields, 
			String sSearchType,
			String sSearchField, 
			String sSearchText
			) {
		String sSQL = "";
		
		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase(SMTablesalescontacts.binactive) == 0){
				sSQL += ", " 
					+ " IF (" + SMTablesalescontacts.binactive + " = 0, 'Yes', 'No')"
					+ " AS " + SMTablesalescontacts.binactive;
				continue;
			}
			//If there are no special fields in the result fields, then just add the field:
			sSQL += ", " + sResultListFields.get(i);
		}
		
		sSQL += " FROM " + SMTablesalescontacts.TableName 
				+ " LEFT JOIN " + SMTablesalesperson.TableName 
				+ " ON " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode
				+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode;

		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}
		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.id;
		return sSQL;
	}

	
	private String buildAPTransactionsSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField, 
			String sSearchText,
			String sAdditionalWhereParameter) {
		String sSQL = "";
		
		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			sSQL += ", " + sResultListFields.get(i);
		}
		
		sSQL += " FROM " + SMTableaptransactions.TableName 
			+ " LEFT JOIN " + SMTableicvendors.TableName 
			+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			+ " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
		;
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}
		
		//If there are any additional limiting 'where' qualifiers passed in:
		if (sAdditionalWhereParameter.compareToIgnoreCase("") != 0){
			sSQL += " AND (" + sAdditionalWhereParameter + ")";
		}
		
		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid;

		//System.out.println("[1506711659] - SQL = '" + sSQL + "'");
		return sSQL;
	}
	
	private String buildICPOSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText,
			String sObjectName){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase("STATUS") == 0){
				sSQL += ", " 
					+ " IF (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 0, 'ENTERED', IF (" 
					+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 1, 'PARTIAL RCPT', IF (" 
					+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 2, 'COMPLETE', 'DELETED')))"
					+ " AS STATUS";
				continue;
			}
			//If there are no special fields in the result fields, then just add the field:
			sSQL += ", " + sResultListFields.get(i);
		}

		String sWhere = "";
		
		sSQL += " FROM " + SMTableicpoheaders.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
		;
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		//If we are looking for OPEN Purchase Orders, add this:
		if (sObjectName.compareToIgnoreCase("OPEN " + ICPOHeader.ParamObjectName) == 0){
			sSQL += " AND ("
				+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus 
				+ " = " + Integer.toString(SMTableicpoheaders.STATUS_ENTERED) + ")"
				+ " OR "
				+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus 
				+ " = " + Integer.toString(SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED) + ")"
			+ ")"
			;
		}
		
		if (sWhere.length() != 0){
			sSQL += sWhere;
		}
		
		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate + " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildICPOSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildICPOByItemSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText,
			String sObjectName){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase("STATUS") == 0){
				sSQL += ", " 
					+ " IF (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 0, 'ENTERED', IF (" 
					+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 1, 'PARTIAL RCPT', IF (" 
					+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = 2, 'COMPLETE', 'DELETED')))"
					+ " AS STATUS";
				continue;
			}
			//If there are no special fields in the result fields, then just add the field:
			sSQL += ", " + sResultListFields.get(i);
		}

		String sWhere = "";
		
		sSQL += " FROM " + SMTableicpolines.TableName
			
			+ " LEFT JOIN " + SMTableicpoheaders.TableName
			+ " ON " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = "
			 + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid			
		;
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}
		
		if (sWhere.length() != 0){
			sSQL += sWhere;
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate + " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildICPOVendorItemSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildSMBIDSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
		
			if (sResultListFields.get(i).compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS) == 0){
				sSQL += ", TRIM("
					+ "CONCAT("
					+ "IF(ISNULL(" + SMTablebids.sshiptoaddress1 + "),'', " 
						+ SMTablebids.sshiptoaddress1 + "), ' ',"
					+ "IF(ISNULL(" + SMTablebids.sshiptoaddress2 + "),'', " 
						+ SMTablebids.sshiptoaddress2 + "), ' ',"
					+ "IF(ISNULL(" + SMTablebids.sshiptoaddress3 + "),'', " 
						+ SMTablebids.sshiptoaddress3 + "), ' ',"
					+ "IF(ISNULL(" + SMTablebids.sshiptoaddress4 + "),'', " 
						+ SMTablebids.sshiptoaddress4 + "), ' ',"
					+ "IF(ISNULL(" + SMTablebids.sshiptocity + "),'', " 
						+ SMTablebids.sshiptocity + "), ' '"
					+ ")"
					+ ") AS " + COMPLETE_SHIP_TO_ADDRESS
				;
			}else{			
				sSQL += ", " + sResultListFields.get(i);
			}
		}

		String sWhere = "";

		sSQL += " FROM " + SMTablebids.TableName
			+ " LEFT JOIN " + SMTableprojecttypes.TableName
			+ " ON " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = "
			+ SMTableprojecttypes.TableName + "." + SMTableprojecttypes.iTypeId
		;
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){			
			if(sSearchField.compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS)== 0){				
				sSQL += "((" + SMTablebids.sshiptoaddress1 + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress2 + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress3 + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress4 + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptocity + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
					;				
			}else{
			
				sSQL += "(" + sSearchField + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
			}
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			if(sSearchField.compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS)== 0){
				sSQL += "((" + SMTablebids.sshiptoaddress1 + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress2 + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress3 + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptoaddress4 + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
					+ " OR "
					+ "(" + SMTablebids.sshiptocity + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
					;				
			}else{				
				sSQL += "(" + sSearchField + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";				
			}
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			if(sSearchField.compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS)== 0){
				sSQL += "((" + SMTablebids.sshiptoaddress1 + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
					+ "OR"
					+ "(" + SMTablebids.sshiptoaddress2 + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
					+ "OR"
					+ "(" + SMTablebids.sshiptoaddress3 + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
					+ "OR"
					+ "(" + SMTablebids.sshiptoaddress4 + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
					+ "OR"
					+ "(" + SMTablebids.sshiptocity + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "'))"
					;
			}else{		
				sSQL += "(" + sSearchField + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
			}
		}

		if (sWhere.length() != 0){
			sSQL += sWhere;
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTablebids.lid + " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildSMBIDSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	private String buildUninvoicedPOReceiptSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT DISTINCT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + SMTableicporeceiptheaders.TableName + " LEFT JOIN"
			+ " " + SMTableicporeceiptlines.TableName + " ON "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " = "
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON " 
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			+ " WHERE ("
			+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpoinvoiceid + " = " 
				+ Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET) + ")"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 1)"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus 
				+ " != " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
			+ " AND";
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid 
		+ " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildUninvoicePOReceiptSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildProposalSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText,
			String sUserName,
			String sCompany){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT "
			+ " 'Finder.buildProposalSQLStatement' AS REPORTNAME "
			+ ", '" + sUserName + "' AS USERNAME"
			+ ", '" + sCompany + "' AS COMPANY"
		;
		//We assume there is always at least one field:
		sSQL += "," + sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){

			if (sResultListFields.get(i).compareToIgnoreCase(COMPLETE_BILL_TO_ADDRESS) == 0){
				sSQL += ", TRIM("
					+ "CONCAT("
					+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1 + "),'', " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2 + "),'', " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3 + "),'', " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4 + "),'', " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity + "),'', " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity + "), ' '"
					+ ")"
					+ ") AS " + COMPLETE_BILL_TO_ADDRESS
				;
			}else{
				if (sResultListFields.get(i).compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS) == 0){
					sSQL += ", TRIM("
						+ "CONCAT("
						+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + "),'', " 
							+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + "),'', " 
							+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + "),'', " 
							+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + "),'', " 
							+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + "),'', " 
							+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + "), ' '"
						+ ")"
						+ ") AS " + COMPLETE_SHIP_TO_ADDRESS
					;
					
				}else
					sSQL += ", " + sResultListFields.get(i);
				}
		}

		String sWhere = "";

		sSQL += " FROM " + SMTableproposals.TableName + " LEFT JOIN " + SMTableorderheaders.TableName
			+ " ON " + SMTableproposals.TableName + "." + SMTableproposals.strimmedordernumber + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
		;

		sSQL += " WHERE (";

		String sWhereClause = "";
		
		//This is the DEFAULT where clause:
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sWhereClause = "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sWhereClause = "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sWhereClause = "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		//But if the user chose to search on a CONCATENATED field, the where clause must look like one of these:
		//If it's a search on all bill to addresses:
		if (sSearchField.compareToIgnoreCase(COMPLETE_BILL_TO_ADDRESS) == 0){
			if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Containing") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine1 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine2 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine3 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToAddressLine4 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToCity + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "'))"
				;
			}
			
		}
		//If it's a search on 'all ship to addresses':
		if (sSearchField.compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS) == 0){
			if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Containing") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
				sWhereClause = "((" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "'))"
				;
			}
			
		}
		
		//sWhereClause += " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")";
		
		sSQL += sWhereClause;
		
		if (sWhere.length() != 0){
			sSQL += sWhere;
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableproposals.TableName + "." + SMTableproposals.strimmedordernumber + " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildProposalSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildNonDedicatedItemSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT DISTINCT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase(ITEM_NON_STOCK_FLAG) == 0){
				sSQL += ", IF(" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0, 'STOCK', '<B>NON</B>-STOCK'" 
					+ ") AS " + ITEM_NON_STOCK_FLAG;
				continue;
			}
			sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.iActive + " = 1)"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + " = '')"
			+ " AND";
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
		+ " ASC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildNonDedicatedItemSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildItemsWithLocationQtysSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText,
			String sAdditionalWhereParameter){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase(ITEM_LOCATION_QTY_OH) == 0){
				sSQL += ", IF(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " IS NULL, 0.0000, " 
					+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + ") AS " + ITEM_LOCATION_QTY_OH;
				continue;
			}
			if (sResultListFields.get(i).compareToIgnoreCase(ITEM_NON_STOCK_FLAG) == 0){
				sSQL += ", IF(" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0, 'STOCK', '<B>NON</B>-STOCK'" 
					+ ") AS " + ITEM_NON_STOCK_FLAG;
				continue;
			}
			sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + SMTableicitems.TableName + " LEFT JOIN"
			+ " " + SMTableicitemlocations.TableName + " ON "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " WHERE ("
			;
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}
		
		//If there are any additional limiting 'where' qualifiers passed in:
		if (sAdditionalWhereParameter.compareToIgnoreCase("") != 0){
			sSQL += " AND (" + sAdditionalWhereParameter + ")";
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
		+ " ASC";
		
		if (bDebugMode){
			System.out.println("[1549042834] In " + this.toString() + ".buildItemsWithLocationQtysSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildItemWithVendorItemSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT DISTINCT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase(ITEM_NON_STOCK_FLAG) == 0){
				sSQL += ", IF(" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0, 'STOCK', '<B>NON</B>-STOCK'" 
					+ ") AS " + ITEM_NON_STOCK_FLAG;
				continue;
			}
			sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + SMTableicitems.TableName + " LEFT JOIN"
			+ " " + SMTableicvendoritems.TableName + " ON "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber
			+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.iActive + " = 1)"
			+ " AND";
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
		+ " ASC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildItemWithVendorItemSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	
	private String buildMostUsedItemSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			if (sResultListFields.get(i).compareToIgnoreCase(ITEM_NON_STOCK_FLAG) == 0){
				sSQL += ", IF(" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0, 'STOCK', '<B>NON</B>-STOCK'" 
					+ ") AS " + ITEM_NON_STOCK_FLAG;
				continue;
			}
			sSQL += ", " + sResultListFields.get(i);
		}

		
		sSQL += " FROM " + SMTableicitems.TableName + " LEFT JOIN" 
				+ " (SELECT " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + " AS INVITEM"
				+ ", SUM(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped + ") AS SHIPPEDTOTAL"
				+ " FROM " + SMTableinvoicedetails.TableName + " RIGHT JOIN"
				+ " (SELECT " + SMTableicitems.sItemNumber + " AS ITEMNO"
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
				;
				//+ "(sItemDescription LIKE '%Traditional%')"
				if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
					sSQL += "(" + sSearchField + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
				}
				if (sSearchType.compareToIgnoreCase("Containing") == 0){
					sSQL += "(" + sSearchField + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
				}
				if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
					sSQL += "(" + sSearchField + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
				}	
				sSQL += ")"
				+ ") AS ICITEMQUERY"
				+ " ON ICITEMQUERY.ITEMNO = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
				+ " GROUP BY " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
				+ ") AS FINALQUERY ON FINALQUERY.INVITEM = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
				+ " WHERE ("
				;
				//+ "(icitems.sItemDescription LIKE '%Traditional%')"
				if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
					sSQL += "(" + sSearchField + " LIKE " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
				}
				if (sSearchType.compareToIgnoreCase("Containing") == 0){
					sSQL += "(" + sSearchField + " LIKE " 
					+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
				}
				if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
					sSQL += "(" + sSearchField + " = " 
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
				}				
				
				sSQL += ")"
				+ " ORDER BY SHIPPEDTOTAL DESC, " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
		;
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildMostUsedItemSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}

	private String buildSMOrderDetailSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
				sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + SMTableorderheaders.TableName + " INNER JOIN"
			+ " " + SMTableorderdetails.TableName
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
			+ " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
		;
		
		sSQL += " WHERE (";
			//+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " ="
			//+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
			//+ " AND";
			
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		//if (sWhere.length() != 0){
		//	sSQL += sWhere;
		//}

		sSQL += " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")";
		
		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableorderheaders.sOrderNumber + " DESC," + " " + SMTableorderdetails.sItemNumber;
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildSMOrderDetailSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}
	private String buildSMExtendedOrderSQLStatement(
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText){
		String sSQL = "";

		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){

			if (sResultListFields.get(i).compareToIgnoreCase(COMPLETE_BILL_TO_ADDRESS) == 0){
				sSQL += ", TRIM("
					+ "CONCAT("
					+ "IF(ISNULL(" + SMTableorderheaders.sBillToAddressLine1 + "),'', " 
						+ SMTableorderheaders.sBillToAddressLine1 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.sBillToAddressLine2 + "),'', " 
						+ SMTableorderheaders.sBillToAddressLine2 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.sBillToAddressLine3 + "),'', " 
						+ SMTableorderheaders.sBillToAddressLine3 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.sBillToAddressLine4 + "),'', " 
						+ SMTableorderheaders.sBillToAddressLine4 + "), ' ',"
					+ "IF(ISNULL(" + SMTableorderheaders.sBillToCity + "),'', " 
						+ SMTableorderheaders.sBillToCity + "), ' '"
					+ ")"
					+ ") AS " + COMPLETE_BILL_TO_ADDRESS
				;
			}else{
				if (sResultListFields.get(i).compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS) == 0){
					sSQL += ", TRIM("
						+ "CONCAT("
						+ "IF(ISNULL(" + SMTableorderheaders.sShipToAddress1 + "),'', " 
							+ SMTableorderheaders.sShipToAddress1 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.sShipToAddress2 + "),'', " 
							+ SMTableorderheaders.sShipToAddress2 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.sShipToAddress3 + "),'', " 
							+ SMTableorderheaders.sShipToAddress3 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.sShipToAddress4 + "),'', " 
							+ SMTableorderheaders.sShipToAddress4 + "), ' ',"
						+ "IF(ISNULL(" + SMTableorderheaders.sShipToCity + "),'', " 
							+ SMTableorderheaders.sShipToCity + "), ' '"
						+ ")"
						+ ") AS " + COMPLETE_SHIP_TO_ADDRESS
					;
					
				}else
					sSQL += ", " + sResultListFields.get(i);
				}
		}

		String sWhere = "";

		sSQL += " FROM " + SMTableorderheaders.TableName
		;

		sSQL += " WHERE (";

		String sWhereClause = "";
		
		//This is the DEFAULT where clause:
		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sWhereClause = "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sWhereClause = "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sWhereClause = "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		//But if the user chose to search on a CONCATENATED field, the where clause must look like one of these:
		//If it's a search on all bill to addresses:
		if (sSearchField.compareToIgnoreCase(COMPLETE_BILL_TO_ADDRESS) == 0){
			if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
				sWhereClause = "((" + SMTableorderheaders.sBillToAddressLine1 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine2 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine3 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine4 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToCity + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Containing") == 0){
				sWhereClause = "((" + SMTableorderheaders.sBillToAddressLine1 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine2 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine3 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine4 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sBillToCity + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
				sWhereClause = "((" + SMTableorderheaders.sBillToAddressLine1 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine2 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine3 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sBillToAddressLine4 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sBillToCity + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "'))"
				;
			}
			
		}
		//If it's a search on 'all ship to addresses':
		if (sSearchField.compareToIgnoreCase(COMPLETE_SHIP_TO_ADDRESS) == 0){
			if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
				sWhereClause = "((" + SMTableorderheaders.sShipToAddress1 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress2 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress3 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress4 + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToCity + " LIKE " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Containing") == 0){
				sWhereClause = "((" + SMTableorderheaders.sShipToAddress1 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress2 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress3 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToAddress4 + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ " OR (" + SMTableorderheaders.sShipToCity + " LIKE " 
				+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%'))"
				;
			}
			if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
				sWhereClause = "((" + SMTableorderheaders.sShipToAddress1 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sShipToAddress2 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sShipToAddress3 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sShipToAddress4 + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')"
				+ " OR (" + SMTableorderheaders.sShipToCity + " = " 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "'))"
				;
			}
			
		}
		
		sWhereClause += " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")";
		
		sSQL += sWhereClause;
		
		if (sWhere.length() != 0){
			sSQL += sWhere;
		}

		sSQL += ")";
		
		sSQL += " ORDER BY " + SMTableorderheaders.sOrderNumber + " DESC";
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".buildSMExtendedOrderSQLStatement - SQL = " + sSQL);
		}
		return sSQL;
	}

	private String buildStdSQLStatement(
			String sObject,
			String sDocumentType,
			ArrayList<String> sResultListFields,
			String sSearchType,
			String sSearchField,
			String sSearchText,
			String sAdditionalWhereParameter){
		String sSQL = "";
		String sTable = "";
		if (sObject.equalsIgnoreCase("Customer")){
			sTable = SMTablearcustomer.TableName;
		}
		if (sObject.equalsIgnoreCase("Document")){
			sTable = SMTableartransactions.TableName;
		}
		if (sObject.equalsIgnoreCase("Order")){
			sTable = SMTableorderheaders.TableName;
		}
		if (sObject.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sTable = SMTableorderheaders.TableName;
		}		
		if (sObject.equalsIgnoreCase("Invoice")){
			sTable = SMTableinvoiceheaders.TableName;
		}
		if (sObject.equalsIgnoreCase(SMBidEntry.ParamObjectName)){
			sTable = SMTablebids.TableName;
		}
		if (sObject.equalsIgnoreCase(SEARCH_ITEM)){
			sTable = SMTableicitems.TableName;
		}
		if (sObject.equalsIgnoreCase(SEARCH_ACTIVE_ITEM)){
			sTable = SMTableicitems.TableName;
		}
		if (sObject.equalsIgnoreCase("SalesContact")){
			sTable = SMTablesalescontacts.TableName;
		}
		if (sObject.equalsIgnoreCase(APVendor.ParamObjectName)){
			sTable = SMTableicvendors.TableName;
		}
		if (sObject.equalsIgnoreCase(GLAccount.Paramobjectname)){
			sTable = SMTableglaccounts.TableName;
		}
		if (sObject.equalsIgnoreCase("ACTIVE " + GLAccount.Paramobjectname)){
			sTable = SMTableglaccounts.TableName;
		}

		if (sObject.equalsIgnoreCase(ICPOReceiptHeader.ParamObjectName)){
			sTable = SMTableicpoheaders.TableName + ", " + SMTableicporeceiptheaders.TableName;
		}

		if (sObject.equalsIgnoreCase(ICPOInvoice.ParamObjectName)){
			sTable = SMTableicpoinvoiceheaders.TableName;
		}
		
		if (sObject.equalsIgnoreCase(ARCallSheet.ParamObjectName)){
			sTable = SMTablecallsheets.TableName;
		}
		
		if (sObject.equalsIgnoreCase("Asset")){
			sTable = SMTablefamaster.TableName;
		}
	
		if (sObject.equalsIgnoreCase(SMMaterialReturn.ParamObjectName)){
			sTable = SMTablematerialreturns.TableName;
		}
		if (sObject.equalsIgnoreCase(SMTaxCertificate.ParamObjectName)){
			sTable = SMTabletaxcertificates.TableName;
		}
		if (sObject.equalsIgnoreCase(SMTableaptransactions.OBJECT_NAME)){
			sTable = SMTableaptransactions.TableName;
		}

		//Add any 'where' clauses needed here:
		//If we are adding a credit, we need to search for an invoice ONLY, so we need to add that
		//qualifier to the where clause:
		String sWhere = "";
		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
			sWhere = " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")";
		}
		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.CASHADJUSTMENT_STRING)){
			sWhere = " AND (" 
				+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.MISCRECEIPT_STRING + ")"
				+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.PREPAYMENT_STRING + ")"
				+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RECEIPT_STRING + ")"
				+ ")";
		}
		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDITADJUSTMENT_STRING)){
			sWhere = " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.CREDIT_STRING + ")";
		}
		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICEADJUSTMENT_STRING)){
			sWhere = " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")";
		}
		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
			sWhere = " AND (" 
				//Only cash, prepays, and misc cash can be reversed:
				+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.MISCRECEIPT_STRING + ")"
				+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.PREPAYMENT_STRING + ")"
				+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RECEIPT_STRING + ")"
				+ ")";
		}

		if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
			sWhere = " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")";
		}

		//If the finder is looking for orders, we don't want to show any quotes in the results:
		if (sObject.compareToIgnoreCase("Order") == 0){
			sWhere += " AND (" + SMTableorderheaders.iOrderType + " != " 
			+ SMTableorderheaders.ORDERTYPE_QUOTE + ")";
		}
		
		if (sObject.compareToIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(
			SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
			sWhere += " AND (" + SMTableorderheaders.iOrderType + " = " 
			+ SMTableorderheaders.ORDERTYPE_QUOTE + ")";
		}
		
		//If there are any additional limiting 'where' qualifiers passed in:
		if (sAdditionalWhereParameter.compareToIgnoreCase("") != 0){
			sWhere += " AND (" + sAdditionalWhereParameter + ")";
		}
		
		//Construct the SQL statement to be used for the search:
		sSQL = " SELECT ";
		//We assume there is always at least one field:
		sSQL += sResultListFields.get(0);
		for (int i = 1; i < sResultListFields.size(); i++){
			sSQL += ", " + sResultListFields.get(i);
		}

		sSQL += " FROM " + sTable;
		sSQL += " WHERE (";

		if (sSearchType.compareToIgnoreCase("Beginning with") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Containing") == 0){
			sSQL += "(" + sSearchField + " LIKE " 
			+ "'%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')";
		}
		if (sSearchType.compareToIgnoreCase("Exactly matching") == 0){
			sSQL += "(" + sSearchField + " = " 
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "')";
		}

		//IF we are searching for a PO receipt ID, we have to add a qualifier to join the tables:
		if (sObject.compareToIgnoreCase(ICPOReceiptHeader.ParamObjectName) == 0){
			sSQL += " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid + ")"; 
		}
		
		//If we are searching for a GL Account, but only want ACTIVE accounts, add this qualifier:
		if (sObject.compareToIgnoreCase("ACTIVE " + GLAccount.Paramobjectname) == 0){
			sSQL += " AND (" + SMTableglaccounts.TableName + "." + SMTableglaccounts.lActive + " = 1)"; 
		}
		
		//If we are searching for an inventory item, but only want ACTIVE items, add this qualifier:
		if (sObject.compareToIgnoreCase(SEARCH_ACTIVE_ITEM) == 0){
			sSQL += " AND (" + SMTableicitems.TableName + "." + SMTableicitems.iActive + " = 1)"; 
		}

		if (sWhere.length() != 0){
			sSQL += sWhere;
		}

		sSQL += ")";
		
		//Sort order:
		if (sObject.equalsIgnoreCase("Order")){
			sSQL += " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " DESC";
		}
		if (sObject.equalsIgnoreCase(SMTableorderheaders.getOrderTypeDescriptions(SMTableorderheaders.ORDERTYPE_QUOTE))){
			sSQL += " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " DESC";
		}
		if (sObject.equalsIgnoreCase("Project")){
			sSQL += " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + " DESC";
		}
		if (sObject.equalsIgnoreCase("Asset")){
			sSQL += " ORDER BY " + SMTablefamaster.TableName + "." + SMTablefamaster.sAssetNumber;
		}
		return sSQL;
	}
	private String highlightResult(String sResult, String sSearchString){
		String s = "";
		int iFirstPosition = sResult.toUpperCase().indexOf(sSearchString.toUpperCase());
		if (iFirstPosition >= 0){
			s = sResult.substring(0, iFirstPosition) + "<span style=\"background-color: #FFFF66\">"
			+ sResult.substring(iFirstPosition, iFirstPosition + sSearchString.length()) 
			+ "</span>"
			+ sResult.substring(iFirstPosition + sSearchString.length(), sResult.length())
			;
		}
		
		return s;
	}
	private String sCommandScripts(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		s += "function sortResultsTable(nSortColumnIndex) { \n"
			//This sort is WAY too slow on long result lists....
			//+ "    sortTable(\"" + TABLE_RESULTS_ID + "\", nSortColumnIndex, 1); \n"
			+ "} \n"
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