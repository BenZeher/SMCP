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
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smap.APVendor;

public class ICItemsReceivedNotInvoicedSelection  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String PARAM_INCLUDE_STOCK_INVENTORY_ITEMS = "INCSTOCKINVENTORYITEMS";
	public static final String PARAM_INCLUDE_NONSTOCK_INVENTORY_ITEMS = "INCNONSTOCKINVENTORYITEMS";
	public static final String PARAM_INCLUDE_NONINVENTORY_ITEMS = "INCNONINVENTORYITEMS";
	public static final String PARAM_INCLUDE_INVOICED_ITEMS = "INCINVOICEDITEMS";
	public static final String PARAM_INCLUDE_NONINVOICED_ITEMS = "INCNONINVOICEDITEMS";
	public static final String PARAM_VENDOR = "SELECTEDVENDOR";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICListItemsReceived
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Items Received";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICListItemsReceived) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICItemsReceivedNotInvoicedGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		
		//checkboxes for locations:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Include locations:<B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT * FROM " + SMTablelocations.TableName 
			+ " ORDER BY " + SMTablelocations.sLocation ;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				  out.println(
						  "<INPUT TYPE=CHECKBOX NAME=\"LOCATION" 
						  + rs.getString(SMTablelocations.sLocation) + "\" CHECKED width=0.25>" 
						  + rs.getString(SMTablelocations.sLocationDescription) + "<BR>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read locations table - " + e.getMessage());
		}
		out.println("</TD>");
		out.println("</TR>");
		
    	out.println("<TR><TD ALIGN=RIGHT><B>With receipt date: </B></TD>");
		out.println("<TD>");
		
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		if (sStartingDate.compareToIgnoreCase("") == 0){
			sStartingDate = "1/1/1900";
		}
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
		if (sEndingDate.compareToIgnoreCase("") == 0){
			sEndingDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		out.println("Starting from "
			+ clsCreateHTMLFormFields.TDTextBox(
				"StartingDate", 
				sStartingDate, 
				10, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				);
		
		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
    			+ clsCreateHTMLFormFields.TDTextBox(
    				"EndingDate", 
    				sEndingDate, 
    				10, 
    				10, 
    				""
    				) 
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    				);

		out.println("</TD></TR>");
		
		//Vendor
		String sVendor = clsManageRequestParameters.get_Request_Parameter(PARAM_VENDOR, request);
    	out.println("<TR><TD ALIGN=RIGHT><B>For vendor: </B></TD>");
		out.println("<TD>");
		out.println(clsCreateHTMLFormFields.TDTextBox(
					PARAM_VENDOR, 
					sVendor, 
					20, 
					SMTableicpoheaders.svendorLength, 
					"&nbsp;"
					+ "<A HREF=\""
					+ APVendor.getFindVendorLink(
						clsServletUtilities.getFullClassName(this.toString()), 
						PARAM_VENDOR, 
						SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID,
						getServletContext(),
						sDBID
					)
					+ "\"> Find vendor</A>"
					+ "&nbsp;(Leave this blank to include ALL vendors.)"
					) 
		);
		
		out.println("</TD></TR>");
		
		//Inventory/NON-inventory items:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Include:<B></TD>");
		out.println("<TD>");

		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDE_STOCK_INVENTORY_ITEMS + "\"");
		out.println(" CHECKED ");
		out.println("width=0.25>STOCK Inventory items");

		
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDE_NONSTOCK_INVENTORY_ITEMS + "\"");
		out.println(" CHECKED ");
		out.println("width=0.25>NON-STOCK Inventory items");
		
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDE_NONINVENTORY_ITEMS + "\"");
		out.println(" CHECKED ");
		out.println("width=0.25>NON-INVENTORY items");

		out.println("</TD>");
		out.println("</TR>");
		
		//Invoiced/uninvoiced items:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Include:<B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDE_INVOICED_ITEMS + "\"");
		//if (bIncludeInvoicedItems){
			out.println(" CHECKED ");
		//}
		out.println("width=0.25>INVOICED items");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + PARAM_INCLUDE_NONINVOICED_ITEMS + "\"");
		//if (bIncludeUnInvoicedItems){
			out.println(" CHECKED ");
		//}
		out.println("width=0.25>UN-INVOICED items");

		out.println("</TD>");
		out.println("</TR>");
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process report----\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
