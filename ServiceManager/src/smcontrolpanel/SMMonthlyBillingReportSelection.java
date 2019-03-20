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

import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMMonthlyBillingReportSelection  extends HttpServlet {
	
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SERVICETYPE_PARAM = "SERVICETYPECODE";
	public static final String PARAM_SEPARATOR = ",";

	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMMonthlyBilling))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Monthly Billing Report";
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
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMMonthlyBillingReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Order dates:
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");

		//Group by:
		out.println("<TR>");
		out.println("<TD><B>Date range (choose 'Previous Month', 'Current Month',<BR>or enter a date range"
				+ " in mm/dd/yyyy format):</B></TD>");
		out.println("<TD>");
		out.println("<LABEL NAME = \"" + "DATERANGELABELPREVIOUSMONTH \" ><input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month</LABEL><BR>");
		out.println("<LABEL NAME = \"" + "DATERANGELABELCURRENTMONTH \" ><input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month</LABEL><BR>");
		out.println("<LABEL NAME = \"" + "DATERANGELABELSELECTEDDATES \" ><input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;");
		
		out.println(
			"Starting:&nbsp;" 
				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
				//+ "<INPUT TYPE=TEXT NAME=\"" + "StartingDate" + "\" "
				//+ "onclick='scwShow(this,event);' "
				//+ "VALUE=\"" + sDefaultStartDate 
				//	+ "\" SIZE = " + 10 + " MAXLENGTH = " + 10 + ">" + ""
				//Date picker icon:
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox(
						"EndingDate", sDefaultEndDate, 10, 10, "")
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext()) + "</LABEL>"
			
		);
		out.println("</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD><B>Service types:</B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT DISTINCT " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode 
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.id
				+ " FROM " + SMTableinvoiceheaders.TableName
				+ " LEFT JOIN " + SMTableservicetypes.TableName + " ON "
				+ SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " = "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
				+ " ORDER BY " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName ;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
				while(rs.next()){
					String sServiceTypeCode = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode);
					String sServiceTypeName = rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName);

					if (sServiceTypeName == null || sServiceTypeName.compareToIgnoreCase("") == 0){
						sServiceTypeName = "(BLANK)";
					}
					
					if (sServiceTypeCode != null && sServiceTypeCode.compareToIgnoreCase("") != 0){
					out.println(
							  "<LABEL NAME = \"" + "SERVICETYPELABEL" + sServiceTypeCode + " \" >"
							  + "<INPUT TYPE=CHECKBOX NAME=\"" + SERVICETYPE_PARAM
							  + sServiceTypeCode					
							  + "\" CHECKED width=0.25>" 
							  + sServiceTypeCode + " - " + sServiceTypeName
							  + "</LABEL>"
							  + "<BR>");
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
				
				if (sSalesGroupDesc == null || sSalesGroupDesc.compareToIgnoreCase("") == 0){
					sSalesGroupDesc = "(BLANK)";
				}
				if (sSalesGroupCode != null && sSalesGroupCode.compareToIgnoreCase("") != 0){
				out.println(
						 "<LABEL NAME = \"" + "SALESGROUPLABEL" + sSalesGroupCode + " \" >"
						  + "<INPUT TYPE=CHECKBOX NAME=\"" + SALESGROUP_PARAM
						  + sSalesGroupCode
						  + PARAM_SEPARATOR
						  + Integer.toString(rs.getInt(SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup))						   
						  + "\" CHECKED width=0.25>" 
						  + sSalesGroupCode + " - " + sSalesGroupDesc
						  + "</LABEL>"
						  + "<BR>");
				}
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read sales group table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");
		
		//Show detailed information:
		out.println("<TR>");
		out.println("<TD><B>Show detailed invoice information:</B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"Detailed\" width=0.25><BR>");
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
