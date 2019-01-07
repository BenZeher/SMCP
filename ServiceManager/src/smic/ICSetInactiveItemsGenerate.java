package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableictransactions;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICSetInactiveItemsGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String ITEM_CHECKBOX_NAME = "ITEMNUM";
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String m_sWarning = "";
	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private boolean bDebugMode = false;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    		request, 
	    		response, getServletContext(), SMSystemFunctions.ICSetInactiveItems)){
	    	return;
	    }
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    /**************Get Parameters**************/
	    
    	String sStartingItem = request.getParameter("StartingItem").trim().toUpperCase();
    	String sEndingItem = request.getParameter("EndingItem").trim().toUpperCase();
	    if (sStartingItem.compareToIgnoreCase(sEndingItem) == 1){
	    	m_sWarning = "Starting item must be alphabetically BEFORE ending item.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
	    } 
	    java.sql.Date datEndDate = null;
	    String sEndDate = clsManageRequestParameters.get_Request_Parameter(ICSetInactiveItemsSelection.ENDING_DATE, request);
	    try {
	    	datEndDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndDate);
		} catch (ParseException e1) {
			String sWarning = URLEncoder.encode("Error:[1423661739] Invalid delete date: '" + datEndDate + "' - " + e1.getMessage(), "UTF-8");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + sWarning
			);
		}
    	boolean bIncludeInactives = false;
    	if (request.getParameter("IncludeInactives").compareToIgnoreCase("TRUE") == 0){
    		bIncludeInactives = true;
    	}
    	boolean bIncludeStockItems = request.getParameter(ICSetInactiveItemsSelection.CHECKBOX_INCLUDE_STOCK_ITEMS) != null;
    	boolean bIncludeNonStockItems = request.getParameter(ICSetInactiveItemsSelection.CHECKBOX_INCLUDE_NONSTOCK_ITEMS) != null;
    	String sSortBy = clsManageRequestParameters.get_Request_Parameter(ICSetInactiveItemsSelection.SORT_BY_PARAMETER, request);
    	if ((!bIncludeStockItems) && (!bIncludeNonStockItems)){
	    	m_sWarning = "You have chosen NOT to list EITHER stock items OR non-stock items - there is nothing to list.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
    	}
    	/**************End Parameters**************/
    	
    	//Customized title
    	String sReportTitle = "";
    	if (bIncludeInactives){
    		sReportTitle = "Set INACTIVE items to ACTIVE";
    	}else{
    		sReportTitle = "Set ACTIVE items to INACTIVE";
    	}
    	String sIncludeInactives = "ACTIVE";
    	if(bIncludeInactives){
    		sIncludeInactives = "INACTIVE";
    	}
    	String sSortByCriteria = "";
    	if (sSortBy.compareToIgnoreCase(ICSetInactiveItemsSelection.SORT_BY_LAST_TRANSACTION_DATE) == 0){
    		sSortByCriteria = "last transaction date";
    	}else{
    		sSortByCriteria = "item number";
    	}
    	String sCriteria = "Starting with item <B>" + sStartingItem + "</B>"
    		+ ", ending with item <B>" + sEndingItem + "</B>, " 
    		+ "<B>INCLUDING ONLY </B> items which are currently " + sIncludeInactives
    		+ ", listing only those items which:<BR>"
    		+ "Have no quantities or costs on hand,"
    		+ " are on no open orders,"
    		+" are on no open PO's or receipts"
    	;
    	if (bIncludeStockItems && bIncludeNonStockItems){
    		sCriteria += ", are either <B>STOCK</B> or <B>NON-STOCK</B> items";
    	}
    	if (bIncludeStockItems && !bIncludeNonStockItems){
    		sCriteria += ", are <B>STOCK</B> items only";
    	}
    	if (!bIncludeStockItems && bIncludeNonStockItems){
    		sCriteria += ", are <B>NON-STOCK</B> items only";
    	}

    	sCriteria += ", sorted by " + sSortByCriteria;
    	sCriteria += ".<BR>";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A></TD></TR></TABLE>");
    	
    	out.println();
    	out.println ("<FORM NAME=ITEMLIST ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICSetInactiveItemsAction\" METHOD=\"POST\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	if (bIncludeInactives){
    		out.println("<INPUT TYPE=HIDDEN NAME='SETACTIVEFLAGTO' VALUE='1'>");
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked items to ACTIVE----\">");
    	}else{
    		out.println("<INPUT TYPE=HIDDEN NAME='SETACTIVEFLAGTO' VALUE='0'>");
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked items to INACTIVE----\">");
    	}

    	out.println("<BR>");
		out.println("<input type=\"button\" name=\"CheckAll\" value=\"Check All Items\" onClick=\"checkAll()\">");
		out.println("<input type=\"button\" name=\"UnCheckAll\" value=\"Uncheck All Items\" onClick=\"uncheckAll()\">");
    	
    	if (!printList(
    		getServletContext(),
    		sDBID,
    		datEndDate,
    		sStartingItem,
    		sEndingItem,
    		bIncludeInactives,
    		bIncludeStockItems,
    		bIncludeNonStockItems,
    		sSortBy,
    		out,
    		(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
    		)){
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + m_sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    		
    	if (bIncludeInactives){
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked items to ACTIVE----\">");
    	}else{
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked items to INACTIVE----\">");
    	}

    	out.println("</FORM>");
    	out.println(printCheckFunctions());
	    out.println("</BODY></HTML>");
	}
	private String printCheckFunctions(){
		String s = "";
       	s += "<script LANGUAGE=\"JavaScript\">\n";
       	
       	s += "function checkAll()\n";
       	s += "{\n";
       	//s += "    for (i = 0; i < field.length; i++)";
       	//s += "        field[i].checked = true;";
       	s += "    for (i=0; i<document.forms[\"ITEMLIST\"].elements.length; i++){\n";
		s += "        var testName = document.forms[\"ITEMLIST\"].elements[i].name;\n";
		//s += "        alert(testName.substring(0, " + Integer.toString(ITEM_CHECKBOX_NAME.length()) + "));\n";
		s += "        if (testName.substring(0, " + Integer.toString(ITEM_CHECKBOX_NAME.length()) + "	) == \"" + ITEM_CHECKBOX_NAME + "\"){\n";
		s += "            document.forms[\"ITEMLIST\"].elements[i].checked = true;\n";
		s += "        }\n";
		s += "    }\n";
       	s += "}\n";

       	s += "function uncheckAll()\n";
       	s += "{\n";
       	s += "    for (i=0; i<document.forms[\"ITEMLIST\"].elements.length; i++){\n";
		s += "        var testName = document.forms[\"ITEMLIST\"].elements[i].name;\n";
		//s += "        alert(testName.substring(0, " + Integer.toString(ITEM_CHECKBOX_NAME.length()) + "));\n";
		s += "        if (testName.substring(0, " + Integer.toString(ITEM_CHECKBOX_NAME.length()) + "	) == \"" + ITEM_CHECKBOX_NAME + "\"){\n";
		s += "            document.forms[\"ITEMLIST\"].elements[i].checked = false;\n";
		s += "        }\n";
		s += "    }\n";
       	s += "}\n";
       	
       	s += "</script>\n";
       	return s;
	}

	private void printHeading(PrintWriter pwOut, boolean bInactive){
		
		String sCheckBoxHeading = "Make INACTIVE?";
		if (bInactive){
			sCheckBoxHeading = "Make ACTIVE?";
		}
		
		pwOut.println("<TABLE BORDER=0>");
		pwOut.println("<TR>" + 
		    "<TD ALIGN=LEFT VALIGN=BOTTOM ><B><FONT SIZE=2>" + sCheckBoxHeading + "</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM ><B><FONT SIZE=2>Item #</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM ><B><FONT SIZE=2>Description</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM ><B><FONT SIZE=2>Last transaction date</FONT></B></TD>" +
		"</TR>" + 
   		"<TR><TD COLSPAN=6><HR></TD><TR>");
		
	}
	private boolean printList(
    		ServletContext context,
    		String sConf,
    		Date datEndDate,
    		String sStartingItem,
    		String sEndingItem,
    		boolean bIncInactives,
    		boolean bIncludeStockItems,
    		boolean bIncludeNonStockItems,
    		String sSortBy,
    		PrintWriter pwOut,
    		String sLicenseModuleLevel
    		){

		//Make sure there are no unposted batches:
		String SQL = "SELECT"
			+ " " + ICEntryBatch.lbatchnumber
			+ " FROM " + ICEntryBatch.TableName
			+ " WHERE ("
				+ "(" + ICEntryBatch.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
				+ " OR (" + ICEntryBatch.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
			+ ")"
			;
 		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".printList"
			);
			if (rs.next()){
				m_sWarning = "There are unposted batches - these must be posted before you can set items as inactive.";
				rs.close();
				return false;
			}else{
				rs.close();
			}
		} catch (SQLException e2) {
			m_sWarning = "Error checking for unposted batches = " + e2.getMessage();
			return false;
		}
		
		//First, create a temporary table to hold all the item records:
		SQL = "CREATE TEMPORARY TABLE ICITEMLIST ("
			+ "sitemnumber varchar(" + Integer.toString(SMTableicitems.sItemNumberLength) 
				+ ") NOT NULL DEFAULT ''"
			+ ", sitemdescription varchar(" + Integer.toString(SMTableicitems.sItemDescriptionLength) 
				+ ") NOT NULL DEFAULT ''"
			+ ", datlasttransaction date DEFAULT '0000-00-00'"
			+ ", PRIMARY KEY (sitemnumber)"
			+ ") " //ENGINE=MyISAM"
		;

		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf, 
			"MySQL", 
			this.toString() + ".printList"
		);
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error creating temporary item table - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
		
		//Now populate the table with active/inactive items:
		long lStartTime = System.currentTimeMillis();
		SQL = "INSERT INTO ICITEMLIST"
			+ " ("
			+ "sitemnumber"
			+ ", sitemdescription"
			+ ") SELECT"
			+ " " + SMTableicitems.sItemNumber
			+ ", " + SMTableicitems.sItemDescription
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
			;
		
				//Inactives?
				if(bIncInactives){
					SQL = SQL + "(" + SMTableicitems.iActive
					+ " = 0)";
				}else{
					SQL = SQL + "(" + SMTableicitems.iActive
					+ " = 1)";
				}
				
				//Stock items?
				if (!bIncludeStockItems){
					SQL += " AND (" + SMTableicitems.inonstockitem + "!=0)";
				}

				//Non-Stock items?
				if (!bIncludeNonStockItems){
					SQL += " AND (" + SMTableicitems.inonstockitem + "!=1)";
				}

				SQL += " AND (" + SMTableicitems.sItemNumber +  ">= '" + sStartingItem + "')"
				+ " AND (" + SMTableicitems.sItemNumber + " <= '" + sEndingItem + "')"
			+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e1) {
	    		m_sWarning = "Error inserting into temporary item table with SQL: " + SQL + " - " 
	    		+ e1.getMessage() + ".";
	    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
	    		clsDatabaseFunctions.freeConnection(context, conn);
	    		return false;
	    	}		
		    if (bDebugMode){
		    	System.out.println("In " + this.toString() + " [1487711844] - inserts took " 
		    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
		    	);
		    	lStartTime = System.currentTimeMillis();
		    }
		
		//Delete items that are after the the last transaction date selected. 	    
		SQL = "SELECT " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
				+ "," + " MAX(" + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate + ") AS 'TRANSDATE'"
				+ " FROM " +  SMTableictransactions.TableName
				+ " LEFT JOIN " + SMTableicitems.TableName + " ON " 
				+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber  
				+ " = " + SMTableictransactions.TableName  + "." + SMTableictransactions.sitemnumber
				+ " GROUP BY " + SMTableictransactions.TableName  + "." + SMTableictransactions.sitemnumber
				;

		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
			//If TRANSDATE > 'entered date' DELETE from ICITEMS 
				if (rs.getDate("TRANSDATE").after(datEndDate)){
				SQL = "DELETE FROM ICITEMLIST"
						+ " WHERE (" + "ICITEMLIST.sitemnumber = '" + rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber) 	+ "')"
					;	
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (SQLException e1) {
		    		m_sWarning = "Error ignoring items on orders with SQL: " + SQL + " - " + e1.getMessage() + ".";
		    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
					clsDatabaseFunctions.freeConnection(context, conn);
		    		return false;
					}										
				}
			}				
			rs.close();		
		}catch(SQLException e){
			m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
			clsDatabaseFunctions.freeConnection(context, conn);
			return false;
		}
		
		
		//Delete items that are outstanding in order entry:
		SQL = "DELETE FROM ICITEMLIST"
			+ " WHERE ("
				+ "ICITEMLIST.sitemnumber IN ("
					+ "SELECT DISTINCT " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber 
					+ " as sitemnumber"
					+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " 
					+ SMTableorderheaders.TableName + " ON " 
					+ SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID 
					+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
					+ " WHERE ("
						+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " <> 0)"
						+ " AND NOT (" + SMTableorderheaders.TableName 
						+ "." + SMTableorderheaders.datOrderCanceledDate + " >'1900-01-01')"
					+ ")"
				+ ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error ignoring items on orders with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
			clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}		
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711847] - order entry filter took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		//Ignore items that are outstanding on PO's
		SQL = "DELETE FROM ICITEMLIST"
			+ " WHERE ("
				+ "ICITEMLIST.sitemnumber IN ("
					+ "SELECT DISTINCT " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
					+ " as sitemnumber"
					+ " FROM " + SMTableicpolines.TableName + " LEFT JOIN " 
					+ SMTableicpoheaders.TableName + " ON " 
					+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid 
					+ " = " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
					+ " WHERE ("
						+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered + " > " 
						+ SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ")"
						+ " AND ("
							+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
							+ SMTableicpoheaders.STATUS_ENTERED + ")"
							+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
							+ SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
						+ ")"
					+ ")"
				+ ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error ignoring items on Purchase orders with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}		
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711849] - purchase order filter took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		//Remove any items that are outstanding on Receipts
		SQL = "DELETE FROM ICITEMLIST"
			+ " WHERE ("
				+ "ICITEMLIST.sitemnumber IN ("
					+ "SELECT DISTINCT " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber 
					+ " as sitemnumber"
					+ " FROM " + SMTableicporeceiptlines.TableName + " LEFT JOIN " 
					+ SMTableicporeceiptheaders.TableName + " ON " 
					+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid 
					+ " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
					+ " WHERE ("
						+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " = " 
						+ SMTableicporeceiptheaders.STATUS_ENTERED + ")"
						+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic 
						+ " = 0)"
					+ ")"
				+ ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error ignoring items on Purchase order receipts with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711851] - PO receipt filter took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		//Remove items that have any costs or qtys in cost buckets:
		SQL = "DELETE FROM ICITEMLIST"
			+ " WHERE ("
				+ "ICITEMLIST.sitemnumber IN ("
				
					+ "SELECT DISTINCT " + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber 
					+ " as sitemnumber"
					+ " FROM " + SMTableiccosts.TableName 
					+ " WHERE ("
						+ "(" + SMTableiccosts.TableName + "." + SMTableiccosts.bdCost + " != 0.00)" 
						+ " OR (" + SMTableiccosts.TableName + "." + SMTableiccosts.bdQty + " != 0.0000)" 
					+ ")"
					
				+ ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error ignoring items in iccosts with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711853] - ICCOST filter took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		//Remove items that have any qtys in locations:
		SQL = "DELETE FROM ICITEMLIST"
			+ " WHERE ("
				+ "ICITEMLIST.sitemnumber IN ("
				
					+ "SELECT DISTINCT " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
					+ " as sitemnumber"
					+ " FROM " + SMTableicitemlocations.TableName 
					+ " WHERE ("
						+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " != 0.00)" 
						+ " OR (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " != 0.0000)" 
					+ ")"
					
				+ ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error ignoring items in icitemlocations with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711855] - item location filter took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		SQL = "UPDATE ICITEMLIST"
			+ " SET datlasttransaction = "
			+ "(SELECT MAX(" + SMTableictransactions.datpostingdate + ")"
			+ " FROM " + SMTableictransactions.TableName 
			+ " WHERE ("
			+ " ICITEMLIST.sitemnumber = " 
			+ SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber + ")"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
    		m_sWarning = "Error updating last transaction dates with SQL: " + SQL + " - " + e1.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " [1487711857] - set datLastTransaction took " 
	    		+ Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    	lStartTime = System.currentTimeMillis();
	    }
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		if (sSortBy.compareToIgnoreCase(ICSetInactiveItemsSelection.SORT_BY_LAST_TRANSACTION_DATE) == 0){
			SQL = "SELECT * FROM ICITEMLIST ORDER BY datlasttransaction, sitemnumber";
		}else{
			SQL = "SELECT * FROM ICITEMLIST ORDER BY sitemnumber";
		}
    	long lItemsPrinted = 0;
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				conn);
    		
    		printHeading(pwOut, bIncInactives);
    		while (rs.next()){
    			pwOut.println("<TR>");
    			pwOut.println("<TD>" + clsCreateHTMLFormFields.TDCheckBox(
    	    		ITEM_CHECKBOX_NAME + rs.getString("sitemnumber"), false, "") + "</TD>");	
    			
    			//Link to item for authorized users:
    			String sItemNumber = rs.getString("sitemnumber");
    			String sItemNumberLink = "";
				if (bViewItemPermitted) {
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
				    		+ sItemNumber
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
    			pwOut.println("<TD>" + sItemNumberLink + "</TD>");
    			
    			
    			pwOut.println("<TD>" + rs.getString("sitemdescription") + "</TD>");
    			pwOut.println("<TD>" + clsDateAndTimeConversions.resultsetDateStringToString(rs.getString("datlasttransaction")) 
    				+ "</TD>");
    			
    			pwOut.println("</TR>");
    			lItemsPrinted++;
    		}
    		rs.close();
    		pwOut.println("</TABLE><BR>");
    		pwOut.println(lItemsPrinted + " items printed.");
    	}catch (SQLException e){
    		m_sWarning = "Error reading items - " + e.getMessage() + ".";
    		m_sWarning += Remove_Temp_Table_ICITEMLIST(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
		m_sWarning = Remove_Temp_Table_ICITEMLIST(conn);
		clsDatabaseFunctions.freeConnection(context, conn);
		return true;
	}
	
	private String Remove_Temp_Table_ICITEMLIST(Connection conn){

    	String SQL = "DROP TABLE IF EXISTS ICITEMLIST";
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException ex){
    		return "<BR>Error removing temp table ICITEMLIST - " + ex.getMessage() + ".";
    	}
    	return "";
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}
}
