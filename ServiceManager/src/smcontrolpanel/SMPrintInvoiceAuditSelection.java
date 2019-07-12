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
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMPrintInvoiceAuditSelection extends HttpServlet {
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SALESGROUP_PARAM_SEPARATOR = ",";
	public static final String INVOICE_NUMBER = "INVOICECHECK";
	public static final String ORDER_NUMBER = "ORDERCHECK";
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
				SMSystemFunctions.SMInvoiceAuditList))
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
		
	    String title = "Invoice Audit List";
	    String subtitle = "listing criterias";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMInvoiceAuditList)
	    	+ "\">Summary</A><BR><BR>");
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoiceAuditGenerate\" onsubmit=\"return validate();\" >");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<SCRIPT> \n" + 
    			"        function validate() {\n" + 
    			"            var a = document.getElementById(\"ORDERCHECK\").value;\n" + 
    			"            var b = document.getElementById(\"INVOICECHECK\").value;\n" + 
    			"            if(a!=\"\"&&b!=\"\"){\n" + 
    			"                alert(\"Input only Order Number or Invoice Number.  [1559586871]\");\n" + 
    			"                return false;\n" + 
    			"            };\n" + 
    			"            return true;\n" + 
    			"        }\n" + 
    			"</SCRIPT>");
    	out.println("<B><BR>Input Date Range, or Invoice Number, or  Order Number</B><BR>\n");
    	out.println("<TABLE BORDER=1 CELLPADDING=1>");
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		
		//TODO FOR TESTING ONLY:
		//sDefaultStartDate = "11/3/2008";
		//sDefaultEndDate = "11/5/2008";
		out.println(/*"<TD COLSPAN=2>\n"
				+ "<B><BR><P style=\"text-align:center;\">Input Date Range, or Invoice Number, or  Order Number</P><BR></B>\n"
				+ "</TD>\n"
				+ "</TR>\n "
				+"<TR>\n"
				+ "<TD COLSPAN=2 BGCOLOR=\"#DCDCDC\">\n"
				+ "<B><P style=\"text-align:center;\">Date Range</P></B>\n"
				+ "</TD>\n"
				+ "</TR>\n "*/"<TR style=\"background-color:grey; color:white; \">\n<TD COLSPAN=2>\n"
				+ "<B>&nbsp;DATE RANGE</B>"
				+ "</TD>\n</TR>\n" );
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Starting with invoice date (mm/dd/yyyy):</B></TD><TD>" 
				+ SMUtilities.getRightDatePickerString("StartingDate", getServletContext())
				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "") 
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Ending with invoice date (mm/dd/yyyy):</B></TD><TD>" 
				+ SMUtilities.getRightDatePickerString("EndingDate", getServletContext())
				+ clsCreateHTMLFormFields.TDTextBox("EndingDate", sDefaultEndDate, 10, 10, "") 
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Starting with invoice creation date (mm/dd/yyyy):</B></TD><TD>" 
				+ SMUtilities.getRightDatePickerString("StartingCreationDate", getServletContext())
				+ clsCreateHTMLFormFields.TDTextBox("StartingCreationDate", sDefaultStartDate, 10, 10, "") 
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Ending with invoice creation date (mm/dd/yyyy):</B></TD><TD>" 
				+ SMUtilities.getRightDatePickerString("EndingCreationDate", getServletContext())
				+ clsCreateHTMLFormFields.TDTextBox("EndingCreationDate", sDefaultEndDate, 10, 10, "") 
				+ "</TD>");
		out.println("</TR>");

		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD><B>Include service types:<B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT DISTINCT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.id
				+ " FROM " + SMTableorderheaders.TableName
				+ " LEFT JOIN " + SMTableservicetypes.TableName + " ON "
				+ SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " = "
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
				+ " ORDER BY " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName + " DESC";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				if(rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
				out.println("<INPUT TYPE=CHECKBOX NAME=\"SERVICETYPE" 
						  + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode) + "\" CHECKED width=0.25>" 
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
		out.println("<TD><B>Include sales groups:<B></TD>");
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
		out.println("<TR>\n"+ 
				/*"<TR>\n"
				+ "<TD COLSPAN=2 BGCOLOR=\"#DCDCDC\" >\n"
				+ "<B><P style=\"text-align:center;\">Invoice Number</P></B>\n"
				+ "</TD>\n"*/
				"<TR style=\"background-color:grey; color:white; \">\n<TD COLSPAN=2>\n"
				+ "<B>&nbsp;INVOICE NUMBER</B>"
				+ "</TD>\n</TR>\n"
				+ "</TR>\n " + 
				"<TD><B>Invoice Number:<B></TD>\n" + 
				"<TD>\n" + 
				"<input id=\""+INVOICE_NUMBER+"\" name=\""+INVOICE_NUMBER+"\" type=\"text\" />\n" + 
				"</TD>\n" + 
				"</TR>\n" + 
				/*"<TR>\n"
				+ "<TD COLSPAN=2 BGCOLOR=\"#DCDCDC\" >\n"
				+ "<B><P style=\"text-align:center;\">Order Number</P></B>\n"
				+ "</TD>\n"
				+ "</TR>\n " */  "<TR style=\"background-color:grey; color:white; \">\n<TD COLSPAN=3>"
				+ "<B>&nbsp;ORDER NUMBER</B>"
				+ "</TD>\n</TR>\n"+
				"<TR>\n" + 
				"<TD><B>Order Number:<B></TD>\n" + 
				"<TD>\n" + 
				"<input id=\""+ORDER_NUMBER+"\" name=\""+ORDER_NUMBER+"\" type=\"text\" />\n" + 
				"</TD>\n" + 
				"</TR>");
        out.println("</TABLE>");
        
    	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	    //out.println(SMUtilities.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
    	out.println("</FORM>");
		    
	    out.println("</BODY></HTML>");
	}
}
