package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMSendInvoiceSelection extends HttpServlet {
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SALESGROUP_PARAM_SEPARATOR = ",";
	public static final String SERVICE_TYPE_PARAM = "SERVICETYPE";
	public static final String SELECT_INVS_WITH_EMAIL_ADDRESSES_PARAM = "SelectInvoicesWithEmailAddressesByDefault";
	public static final String EXTENDED_PRICE_PARAM = "ShowExtendedPriceForEachItem";
	public static final String LABOR_MATERIALS_PARAM = "ShowLaborAndMaterialSubtotals";
	public static final String TAX_BREAKDOWN_PARAM = "ShowTaxBreakdown";
	public static final String ALL_ITEMS_PARAM = "ShowALLItemsOnInvoiceIncludingDNP";
	public static final String SUPRESS_PAGEBREAK_PARAM = "SuppressDetailsPageBreak";
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMSendInvoices))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		
	    String title = "Send Invoices";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMInvoiceAuditList)
	    	+ "\">Summary</A><BR><BR>");
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSendInvoiceGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<TABLE BORDER=1 CELLPADDING=1 WIDTH=800px>");

		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD  VALIGN=\"TOP\" align=\"right\"><B>Include order types:<B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.id
				+ " FROM " + SMTableorderheaders.TableName
				+ " LEFT JOIN " + SMTableservicetypes.TableName + " ON "
				+ SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " = "
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
				+ " GROUP BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
				+ " ORDER BY " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName + " DESC";;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				if(rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
					out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SERVICE_TYPE_PARAM 
							  + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode ) + "\" CHECKED width=0.25>" 
							  + rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName) + "<BR>");
				}
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read service types table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");

		//checkboxes for sales groups:
		out.println("<TR>");
		out.println("<TD  VALIGN=\"TOP\" align=\"right\"><B>Include sales groups:<B></TD>");
		out.println("<TD>");
		
		SQL = "SELECT DISTINCT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup 
			+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode
			+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
				+ " FROM " + SMTableorderheaders.TableName
			+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = "
			+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
			+ " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup ;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				String sSalesGroupCode = rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode);
				String sSalesGroupDesc = rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc);
				if (sSalesGroupCode == null){
					sSalesGroupCode = "(BLANK)";
				}
				if (sSalesGroupDesc == null){
					sSalesGroupDesc = "(BLANK)";
				}

				out.println(
						  "<INPUT TYPE=CHECKBOX NAME=\"" + SALESGROUP_PARAM
						  + sSalesGroupCode
						  + SALESGROUP_PARAM_SEPARATOR
						  + Integer.toString(rs.getInt(SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup))						   
						  + "\" CHECKED width=0.25>" 
						  + sSalesGroupCode + " - " + sSalesGroupDesc
						  + "<BR>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read sales group table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");
		String sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		out.println("<TR><TD VALIGN=\"TOP\" align=\"right\"><B>Pre-select&nbsp;invoices&nbsp;with<BR>email&nbsp;addresses:</B></TD>"
				+ "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SELECT_INVS_WITH_EMAIL_ADDRESSES_PARAM + "\"" + sChecked + "\">"
				+ "(Invoices that have email addresses<BR>&nbsp;&nbsp;&nbsp;&nbsp;will be selected by default in the list.)" 
				+ "</TD></TR>");
		
		out.println("<TR><TD VALIGN=\"TOP\" align=\"right\"><B>Invoice display/print options:</B></TD>"
				+ "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + EXTENDED_PRICE_PARAM + "\" VALUE=1>Show extended price for each item<BR>" 
				+ "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + LABOR_MATERIALS_PARAM + "\" VALUE=1>Show labor and material subtotals<BR>"
				+ "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + TAX_BREAKDOWN_PARAM + "\" VALUE=1>Show tax breakdown<BR>" 
				+ "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + ALL_ITEMS_PARAM + "\" VALUE=1>Show all items on invoice (including 'DNP' items)<BR>" 
				+ "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + SUPRESS_PAGEBREAK_PARAM + "\" VALUE=1>Start details immediately below totals (no page break before the details)<BR>" 
				+ "</TD></TR>");
		
        out.println("</TABLE>");
        
    	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	    //out.println(SMUtilities.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
    	out.println("</FORM>");
		    
	    out.println("</BODY></HTML>");
	}
}
