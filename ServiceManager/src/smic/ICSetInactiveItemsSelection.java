package smic;

import java.io.IOException;
import java.io.PrintWriter;
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
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class ICSetInactiveItemsSelection  extends HttpServlet {
	
	public static final String CHECKBOX_INCLUDE_STOCK_ITEMS = "INCLUDESTOCKITEMS";
	public static final String CHECKBOX_INCLUDE_STOCK_ITEMS_LABEL = "Stock items";
	public static final String CHECKBOX_INCLUDE_NONSTOCK_ITEMS = "INCLUDENONSTOCKITEMS";
	public static final String CHECKBOX_INCLUDE_NONSTOCK_ITEMS_LABEL = "Non-stock items";
	public static final String ENDING_DATE = "ENDINGDATE";
	public static final String SORT_BY_PARAMETER = "SORTBY";
	public static final String SORT_BY_ITEM = "SORTBYITEM";
	public static final String SORT_BY_LAST_TRANSACTION_DATE = "SORTBYLASTTRANSACTIONDATE";
	public static final String SORT_BY_ITEM_LABEL = "Sort by item number";
	public static final String SORT_BY_LAST_TRANSACTION_DATE_LABEL = "Sort by last transaction date";
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICSetInactiveItems
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    String title = "Set Inactive Items";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" 
	    		+ Long.toString(SMSystemFunctions.ICSetInactiveItems) 
	    		+ "\">Summary</A><BR>");
	    
	    out.println("NOTE: This list will NOT include any items which are still pending shipment on open orders,"
	    		+ " Purchase Orders, PO Receipts, or any items with costs or quantities.<BR>");
	    
	    try {
	    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICSetInactiveItemsGenerate\" METHOD=\"POST\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    	out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>Item Selection:</B></TD>");
	    	
	    	String sSQL =
	    		"SELECT"
	    		+ " " + SMTableicitems.sItemNumber
	    		+ " FROM " + SMTableicitems.TableName
	    		+ " ORDER BY " + SMTableicitems.sItemNumber
	    		+ " ASC LIMIT 1";
	    	ResultSet rsItems = clsDatabaseFunctions.openResultSet(
	    			sSQL, 
	    			getServletContext(), 
	    			sDBID,
	    			"MySQL",
	    			this.toString() + ".doPost (1) - User: " + sUserName);
	    	String sStartingItemNumber = "";
	    	if (rsItems.next()){
	    		sStartingItemNumber = rsItems.getString(SMTableicitems.sItemNumber);
	    	}
	    	rsItems.close();
	    	out.println("<TD>" + "<B>Starting with:</B> " 
	    			+ clsCreateHTMLFormFields.TDTextBox("StartingItem", sStartingItemNumber, 10, 10, "") + "</TD>");
	    	
	    	sSQL =
	    		"SELECT"
	    		+ " " + SMTableicitems.sItemNumber
	    		+ " FROM " + SMTableicitems.TableName
	    		+ " ORDER BY " + SMTableicitems.sItemNumber
	    		+ " DESC LIMIT 1";

	    	rsItems = clsDatabaseFunctions.openResultSet(
	    			sSQL, 
	    			getServletContext(), 
	    			sDBID,
	    			"MySQL",
	    			this.toString() + ".doPost (2) - User: " + sUserName);
	    	String sEndingItemNumber = "";
	    	if (rsItems.next()){
	    		sEndingItemNumber = rsItems.getString(SMTableicitems.sItemNumber);
	    	}
	    	rsItems.close();
	    	out.println("<TD>" + "<B>Ending with:</B> " + clsCreateHTMLFormFields.TDTextBox("EndingItem", sEndingItemNumber, 10, 10, "") + "</TD>");
	    	out.println("</TR>");
	    		    	
	    	//List actives/inactives:
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>List only:</B></TD>");
	    	//Drop down list:
	    	out.println("<TD>");
	    	out.println("<SELECT NAME = \"" + "IncludeInactives" + "\">");
	    	out.println("<OPTION VALUE=\"" + "TRUE" + "\">" + "INACTIVE items");
	    	out.println("<OPTION VALUE=\"" + "FALSE" + "\">" + "ACTIVE items");
	    	out.println("</SELECT>");
	    	out.println("</TD>");
	    	out.println("<TD>" + "&nbsp;</TD>");
	    	out.println("</TR>");
	    	
	    	//Sort by:
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>Sort by:</B></TD>");
	    	//Drop down list:
	    	out.println("<TD>");
	    	out.println("<SELECT NAME = \"" + SORT_BY_PARAMETER + "\">");
	    	out.println("<OPTION VALUE=\"" + SORT_BY_ITEM + "\">" + SORT_BY_ITEM_LABEL);
	    	out.println("<OPTION VALUE=\"" + SORT_BY_LAST_TRANSACTION_DATE + "\">" + SORT_BY_LAST_TRANSACTION_DATE_LABEL);
	    	out.println("</SELECT>");
	    	out.println("</TD>");
	    	out.println("<TD>" + "&nbsp;</TD>");
	    	out.println("</TR>");
	    	
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>Include:</B></TD>");
	    	//Drop down list:
	    	out.println("<TD>");
	    	out.println(
	    		"<INPUT TYPE=CHECKBOX" + clsServletUtilities.CHECKBOX_CHECKED_STRING + " NAME=\"" + CHECKBOX_INCLUDE_STOCK_ITEMS + "\" width=0.25>"
	    		+ CHECKBOX_INCLUDE_STOCK_ITEMS_LABEL
	    		+ "&nbsp;"
	    		+"<INPUT TYPE=CHECKBOX" + clsServletUtilities.CHECKBOX_CHECKED_STRING + " NAME=\"" + CHECKBOX_INCLUDE_NONSTOCK_ITEMS + "\" width=0.25>"
	    		+ CHECKBOX_INCLUDE_NONSTOCK_ITEMS_LABEL
	    	);
	    	out.println("<TD>" + "&nbsp;</TD>");    	
	    	out.println("</TR>");
	    	//Ending date:
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>Last transaction date before:</B></TD>");	
	    	out.println("<TD ALIGN=LEFT><B>"
	    			+ clsCreateHTMLFormFields.TDTextBox(
	    					ENDING_DATE, 
	    					clsDateAndTimeConversions.now("M/d/yyyy"), 
	    					10, 
	    					10, 
	    					""
	    					) 
	    					+ SMUtilities.getDatePickerString(ENDING_DATE, getServletContext())
	    			+ "</B></TD>");
	    	out.println("<TD ALIGN=RIGHT><B></B></TD>");
	    	out.println("</TR>");    	
	    	out.println("</TABLE>");
	    	out.println("<BR>");
	    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
	    	out.println("</FORM>");
	    	
	    } catch (SQLException ex) {
	    	out.println("<BR><FONT COLOR=RED>Error [1400702905] - " + ex.getMessage() + ".</FONT>");
	    }
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
