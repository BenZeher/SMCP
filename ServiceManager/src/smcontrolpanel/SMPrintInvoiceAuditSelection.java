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

import smar.ARUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMPrintInvoiceAuditSelection extends HttpServlet {
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SALESGROUP_PARAM_SEPARATOR = ",";
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
	    
	    String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
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
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoiceAuditGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<TABLE BORDER=1 CELLPADDING=1>");
    	
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		
		//TODO FOR TESTING ONLY:
		//sDefaultStartDate = "11/3/2008";
		//sDefaultEndDate = "11/5/2008";
		
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
		out.println("<TD><B>Include order types:<B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sCode ;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				  out.println(
						  "<INPUT TYPE=CHECKBOX NAME=\"SERVICETYPE" 
						  + rs.getString(SMTableservicetypes.sCode) + "\" CHECKED width=0.25>" 
						  + rs.getString(SMTableservicetypes.sName) + "<BR>");
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
		
        out.println("</TABLE>");
        
    	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	    //out.println(SMUtilities.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
    	out.println("</FORM>");
		    
	    out.println("</BODY></HTML>");
	}
}
