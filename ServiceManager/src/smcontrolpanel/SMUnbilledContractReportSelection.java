package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMUnbilledContractReportSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String PRINTINDIVIDUAL_PARAMETER = "INDIVIDUAL";
	public static final String PRINTINDIVIDUAL_VALUE_YES = "YES";
	public static final String PRINTINDIVIDUAL_VALUE_NO = "NO";
	public static final String INDIVIDUALSALESPERSON_PARAMETER = "INDIVIDUALSALESPERSON";
	public static final String SALESGROUP_PARAMETER = "SALESGROUPCHECKBOX";
	public static final String SALESPERSON_PARAMETER = "SALESPERSON";
	private static final String UNCHECKALLSALESPERSONSBUTTON = "UnCheckAllSalespersons";
	private static final String UNCHECKALLSALESPERSONSLABEL = "UNCHECK All Salespersons";
	private static final String CHECKALLSALESPERSONSBUTTON = "CheckAllSalespersons";
	private static final String CHECKALLSALESPERSONSLABEL = "CHECK All Salespersons";
	public static final String SHOW_INDIVIDUAL_ORDERS_PARAMETER = "SHOWINDIVIDUALORDERS";	
	public static final String SHOW_ACTIVE_PARAMETER = "SHOWACTIVE";
	public static final String SHOW_STANDING_PARAMETER = "SHOWSTANDING";
	public static final String SHOW_WORKORDERS_PARAMETER = "SHOWWORKORDERS";
	public static final String SERVICE_TYPE_PARAMETER = "SERVICETYPES";
	public static final String GENERATE_REPORT_PARAMETER = "GENERATEREPORT";
	private static final String FORM_NAME = "UCRFORM";
	private static final String CALLED_CLASS_NAME = "smcontrolpanel.SMUnbilledContractGenerate";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L)
				){
			return;
		}
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		//String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SESSION_PARAM_USERNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
								+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		boolean bPrintIndividual = 
				clsManageRequestParameters.get_Request_Parameter(
						PRINTINDIVIDUAL_PARAMETER, request).compareToIgnoreCase(PRINTINDIVIDUAL_VALUE_YES) == 0;

		String title = "";
		String sIndividualSalesperson = "";
		if (bPrintIndividual){
			if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMUnbilledOrdersReportForIndividual, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
				out.println("<BR><B>You do not have permission to print this report for an individual.</B><BR>");
				return;
			}
			sIndividualSalesperson = getSalespersonCode(sDBID, sUserID, sUserFullName);
			if (sIndividualSalesperson.compareToIgnoreCase("") == 0){
				out.println("<BR><B>You do not have a valid salesperson code in your user set up.</B><BR>");
				return;
			}
			title = "Unbilled Orders Report For Salesperson " + sIndividualSalesperson;
		}else{
			if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMUnbilledOrdersReport, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
				out.println("<BR><B>You do not have permission to print this report.</B><BR>");
				return;
			}
			title = "Unbilled Orders Report";
		}
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println ("<I>This report lists any order lines which still have a quantity on order.  It is useful for determining if any items need to be shipped/invoiced, and can also be used"
				+ " as a way to approximately project order backlog over a future period of time.</I><BR>"
			);
		
		out.println ("<FORM NAME = \"" + FORM_NAME + "\" ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ CALLED_CLASS_NAME + "\" ONSUBMIT = \"return validateServiceType()\" METHOD='POST'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=1 border=1>");

		////select sort order
		//out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Sort By</B><BR><FONT COLOR=RED>(<I>*Not used when generating emails.</I>)</FONT></TD>");
		//out.println("<TD VALIGN=TOP><SELECT NAME=\"" + SORT_ORDER_PARAMETER + "\">");
		//out.println("<OPTION VALUE=\"" + SORT_ORDER_SALESPERSONTHENSALESGROUP + "\">" + SORT_ORDER_SALESPERSONTHENSALESGROUP);
		//out.println("<OPTION VALUE=\"" + SORT_ORDER_SALESGROUPTHENSALESPERSON + "\">" + SORT_ORDER_SALESGROUPTHENSALESPERSON);
		//out.println("</SELECT></TD></TR>");

		String sSQL;
		try {
			sSQL = "SELECT * FROM"
					+ " " + SMTablesalesgroups.TableName 
					+ " ORDER BY" + " " + SMTablesalesgroups.sSalesGroupCode;
			ResultSet rsSalesGroups = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".printing sales groups - userID: " + sUserID
					+ " - " + sUserFullName
					);
			out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Sales Group</B><TD>");
			out.println("<INPUT TYPE=CHECKBOX " + clsServletUtilities.CHECKBOX_CHECKED_STRING + " NAME=\"" + SALESGROUP_PARAMETER + "0\" width=0.25>0 - N/A&nbsp;");
			while(rsSalesGroups.next()){
				out.println("<INPUT TYPE=CHECKBOX " + clsServletUtilities.CHECKBOX_CHECKED_STRING + " NAME=\"" + SALESGROUP_PARAMETER
						+ rsSalesGroups.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId) + "\" width=0.25>" + 
						//rsSalesGroups.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode) + " - " +
						rsSalesGroups.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc) + "&nbsp;");
			}
			rsSalesGroups.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED>Error [1428955093] reading sales groups - " + e1.getMessage());
		}
		out.println("</TD>");
		out.println("</TR>");

		//select service type
		sSQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC";
		int iServiceTypeCount  = 0;
		try {
			ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".printing service types - userID: " + sUserID
					+ " - " + sUserFullName
					);
			out.println("<TR>" 
					+"     <TD ALIGN=RIGHT VALIGN=TOP>" +
					"        <div id=\"Service\">" +
					"           <B>Service Type </B>" +
					"         </div>               "+
					"      </TD><TD VALIGN=TOP>");
			while(rsServiceTypes.next()){
				iServiceTypeCount++;
				out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SERVICE_TYPE_PARAMETER 
						+ rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode) + "\" id = \""+
						SERVICE_TYPE_PARAMETER+iServiceTypeCount+"\"width=0.25>" 
						//+ rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode) + " - " 
						+ rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName) + "&nbsp;");
			}
			rsServiceTypes.close();
		} catch (SQLException e1) {
			out.println("<BR><FONT COLOR=RED>Error [1428955094] reading service types - " + e1.getMessage());
		}
		out.println("</TD>");
		out.println("</TR>");

		//Job status
		out.println ("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Job Status </B></TD><TD VALIGN=TOP>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SHOW_ACTIVE_PARAMETER + "\" CHECKED> Active&nbsp;");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SHOW_STANDING_PARAMETER + "\" CHECKED> Standing");
		out.println ("</TD></TR>");

		//show details or not
		out.println ("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Show Individual Orders? </B></TD><TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SHOW_INDIVIDUAL_ORDERS_PARAMETER + "\" CHECKED>");
		//out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + SHOW_DETAIL_PARAMETER + "\" VALUE=1 checked=\"yes\">Yes&nbsp;");
		//out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + SHOW_DETAIL_PARAMETER + "\" VALUE=0>No<BR>");
		out.println ("</TD></TR>");

		//show work orders or not
		out.println ("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Show Scheduled Work Orders? </B></TD><TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SHOW_WORKORDERS_PARAMETER + "\" CHECKED>");
		out.println ("</TD></TR>");
		
		//show statistics or not
		//out.println ("<TR><TD ALIGN=RIGHT VALIGN=TOP><B>Show Statistics? </B></TD><TD>");
		//out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SHOW_STATISTICS_PARAMETER + "\" CHECKED>");
		//out.println ("</TD></TR>");

		//Close this table, and just build a new table for the salesperson checkboxes:
		out.println("</TABLE>");

		//Salespersons:
		if (bPrintIndividual){
			out.println("<INPUT TYPE=HIDDEN NAME='" + SALESPERSON_PARAMETER + sIndividualSalesperson 
				+ "' VALUE='" + SALESPERSON_PARAMETER + sIndividualSalesperson + "'>");
		}else{
			out.println("<U><B>Salespersons</B></U>&nbsp;");
			out.println("<input type=\"button\" name=\"" + CHECKALLSALESPERSONSBUTTON + "\" value=\"" + CHECKALLSALESPERSONSLABEL 
				+ "\" onClick=\"checkall()\">");
			out.println("<input type=\"button\" name=\"" + UNCHECKALLSALESPERSONSBUTTON + "\" value=\"" + UNCHECKALLSALESPERSONSLABEL 
				+ "\" onClick=\"uncheckall()\">");
			
			//Add table of salespeople:
			String SQL = "SELECT "
					+ " IF(DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson 
						+ " IS NULL, '', DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson + ") as "
						+ SMTableorderheaders.sSalesperson
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
					+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
					+ " FROM ("
					+ "SELECT DISTINCT " 

				    //+ " IF(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '', "
				    //+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
				    + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				    //+ ") AS "  + SMTableorderheaders.sSalesperson

				    + " FROM " + SMTableorderheaders.TableName
				    + " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				    + ") AS DISTINCTSALESPEOPLE"
				    + " LEFT JOIN " + SMTablesalesperson.TableName + " ON"
				    + " DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson 
				    + " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(),
						sDBID,
						"MySQL",
						this.toString() + ".doPost - UserID: " + sUserID
						+ " - " + sUserFullName
						);
				ArrayList<String> sSalespersonList = new ArrayList<String>(0);
				String sFirstName = "";
				String sLastName = "";
				String sCheckedStatus = " checked=\"Yes\""; 
				if (clsManageRequestParameters.get_Request_Parameter(UNCHECKALLSALESPERSONSBUTTON, request).compareToIgnoreCase("") != 0){
					sCheckedStatus = ""; 
				}
				while (rs.next()){
					if (rs.getString(SMTablesalesperson.sSalespersonFirstName) == null){
						sFirstName = "";
					}else{
						sFirstName = rs.getString(SMTablesalesperson.sSalespersonFirstName).trim();
					}
					if (rs.getString(SMTablesalesperson.sSalespersonLastName) == null){
						sLastName = "";
					}else{
						sLastName = rs.getString(SMTablesalesperson.sSalespersonLastName).trim();
					}
					String sSalespersonLabel = rs.getString(SMTableorderheaders.sSalesperson).trim();
					if (sSalespersonLabel.compareToIgnoreCase("") == 0){
						sSalespersonLabel = SMOrderHeader.UNLISTEDSALESPERSON_MARKER;
					}
					sSalespersonList.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedStatus  
							+ " NAME=\"" + SALESPERSON_PARAMETER
							+ rs.getString(SMTableorderheaders.sSalesperson) + "\""
							+ " VALUE=\"" + SALESPERSON_PARAMETER
							+ rs.getString(SMTableorderheaders.sSalesperson) 
							+ "\">" 
							+ sSalespersonLabel
							+ "&nbsp;" + sFirstName
							+ "&nbsp;" + sLastName
							+ "\n"
							);
				}
				rs.close();
				out.println(SMUtilities.Build_HTML_Table(4, sSalespersonList, 100, 1, true ,true));

			}catch(SQLException e){
				out.println("<BR>Error [1428954875] reading salespersons with SQL: " + SQL + " - " + e.getMessage());
			}
			// End salesperson table:
			out.println("*Salespersons listed are ALL salespersons included in the order header data "
					+ "- some may no longer be in the salespersons table, and those may appear with no names.");
			out.println("<BR>");
		}

		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + GENERATE_REPORT_PARAMETER + "\" VALUE=\"----List----\">&nbsp;&nbsp;");
		//out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + GENERATE_EMAIL_PARAMETER + "\" VALUE=\"----Email Salespersons----\">  " 
		//	+ "Check to confirm : <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_EMAIL_PARAMETER + "\">");
		out.println("</FORM>");
		
       	out.println ("<script LANGUAGE=\"JavaScript\">");
       	
       	String s = "function checkall(){\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        if (testName.substring(0, " + Integer.toString(SALESPERSON_PARAMETER.length()) + "	) == \"" + SALESPERSON_PARAMETER + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = true;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";

       	s += "function uncheckall(){\n"
				+ "    for (i=0; i<document.forms[\"" + FORM_NAME + "\"].elements.length; i++){\n"
	   			+ "        var testName = document.forms[\"" + FORM_NAME + "\"].elements[i].name;\n"
	   			+ "        if (testName.substring(0, " + Integer.toString(SALESPERSON_PARAMETER.length()) + "	) == \"" + SALESPERSON_PARAMETER + "\"){\n"
	   			+ "            document.forms[\"" + FORM_NAME + "\"].elements[i].checked = false;\n"
	   			+ "        }\n"
	   			+ "    }\n"
			  + "}\n";
       	s += "function validateServiceType () {\n"
       		+ "  var bCheckedBox = false;\n"
       		+ "  for(i=1; i<="+iServiceTypeCount+"; i++) { \n"
       		+ "    if(document.getElementById(\""+SERVICE_TYPE_PARAMETER+"\"+i).checked){\n"
       		+ "       bCheckedBox = true;\n"
       		+ "     }\n"
       		+ "    }\n"
       		+ "  if(!bCheckedBox){\n"
       		+ "    alert(\"Must Check at least one service type\");\n"
       		+"     window.scrollTo(0,0);\n"
       		+"     document.getElementById(\"Service\").focus();\n"
       		+ "   }\n"
       		+"     return bCheckedBox;\n"
       		+"    }\n";
       		
       		
       	
       	out.println(s);
       	
       	out.println("</script>");

		out.println("</BODY></HTML>");
	}
	private String getSalespersonCode(String sDBID, String sUserID, String sUserFullName){
		String SQL = "SELECT"
				+ " " + SMTableusers.sDefaultSalespersonCode
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
				+ ")"
				;

		String sSalesPerson = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sDBID, "MySQL", SMUtilities.getFullClassName(this
							.toString())
							+ ".getSalesPersonCode - userID: " + sUserID
							+ " - " + sUserFullName);
			if (rs.next()){
				sSalesPerson = rs.getString(SMTableusers.sDefaultSalespersonCode).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//No need to do anything here - we'll return an empty string
			return "";
		}
		return sSalesPerson;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}