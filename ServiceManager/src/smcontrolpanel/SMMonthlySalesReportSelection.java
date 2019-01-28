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
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMMonthlySalesReportSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SALESGROUP_PARAM_SEPARATOR = ",";
	public static final String PRINTINDIVIDUAL_PARAMETER = "INDIVIDUAL";
	public static final String PRINTINDIVIDUAL_VALUE_YES = "YES";
	public static final String PRINTINDIVIDUAL_VALUE_NO = "NO";
	public static final String INDIVIDUALSALESPERSON_PARAMETER = "INDIVIDUALSALESPERSON";
	public static final String CHECKALLSALESPERSONSBUTTON = "CheckAllSalespersons";
	public static final String CHECKALLSALESPERSONSLABEL = "CHECK All Salespersons";
	public static final String UNCHECKALLSALESPERSONSBUTTON = "UnCheckAllSalespersons";
	public static final String UNCHECKALLSALESPERSONSLABEL = "UNCHECK All Salespersons";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L)){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + 
	    				" " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String  sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    boolean bPrintIndividual = 
	    	clsManageRequestParameters.get_Request_Parameter(
	    	PRINTINDIVIDUAL_PARAMETER, request).compareToIgnoreCase(PRINTINDIVIDUAL_VALUE_YES) == 0;
	    
	    String title = "";
	    String sIndividualSalesperson = "";
	    if (bPrintIndividual){
		    if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMIndividualMonthlySales, 
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
		    title = "Monthly Sales Report For Salesperson " + sIndividualSalesperson;
	    }else{
		    if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMMonthlySales, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
		    	out.println("<BR><B>You do not have permission to print this report.</B><BR>");
		    	return;
		    }
		    title = "Monthly Sales Report";
	    }
	    
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
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMMonthlySalesReportGenerate\">");
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
		out.println("<LABEL NAME = \"" + "DATERANGELABELSELECTEDDATES \" ><input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\"></LABEL>&nbsp;"
			+ "Starting:&nbsp;" + clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
			+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
			+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox("EndingDate", sDefaultEndDate, 10, 10, "")
			+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
		);
		out.println("</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD><B>Billing type:</B></TD>");
		out.println("<TD>");
		out.println("<LABEL NAME = \"" + "BILLINGTYPELABELSALES \" ><input type=\"radio\" name=\"SalesType\" value=\"Sales\" checked> Sales</LABEL>&nbsp;");
		out.println("<LABEL NAME = \"" + "BILLINGTYPELABELSERVICE \" ><input type=\"radio\" name=\"SalesType\" value=\"Service\"> Service</LABEL>&nbsp;");
		out.println("<LABEL NAME = \"" + "BILLINGTYPELABELSALESANDSERVICE \" ><input type=\"radio\" name=\"SalesType\" value=\"SalesAndService\"> Sales AND Service</LABEL>");
		out.println("</TD>");
		out.println("</TR>");
		
		//checkboxes for sales groups:
		out.println("<TR>");
		out.println("<TD><B>Include sales groups:<B></TD>");
		out.println("<TD>");
		
		String SQL = "SELECT DISTINCT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup 
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
						"<LABEL NAME = \"" + "SALESGROUPLABELSALES" + sSalesGroupCode + " \" >"
						+  "<INPUT TYPE=CHECKBOX NAME=\"" + SALESGROUP_PARAM
						+ sSalesGroupCode
						+ SALESGROUP_PARAM_SEPARATOR
						+ Integer.toString(rs.getInt(SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup))						   
						+ "\" CHECKED width=0.25>" 
						+ sSalesGroupCode + " - " + sSalesGroupDesc
						+ "</LABEL>"
						+ "<BR>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read sales group table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");
		
		//Show detailed information:
		out.println("<TR>");
		out.println("<TD><B>Show individual orders:</B></TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"ShowIndividualOrders\" checked=\"yes\" width=0.25>");
		out.println("</TD>");
		out.println("</TR>");
		
		out.println("</TABLE><BR>");
		
		if (bPrintIndividual){
			out.println("<INPUT TYPE=HIDDEN NAME='SALESPERSON" + sIndividualSalesperson + "' VALUE='" + sIndividualSalesperson + "'>");
		}else{
			out.println("<B><U>Including salespersons*:</U></B>"
				+ "&nbsp;&nbsp;"
				+ "<INPUT TYPE=\"SUBMIT\" NAME = \"" 
				+ UNCHECKALLSALESPERSONSBUTTON 
				+ "\" VALUE=\"" + UNCHECKALLSALESPERSONSLABEL + "\">"
				+ "&nbsp;&nbsp;"
				+ "<INPUT TYPE=\"SUBMIT\" NAME = \"" 
				+ CHECKALLSALESPERSONSBUTTON 
				+ "\" VALUE=\"" + CHECKALLSALESPERSONSLABEL + "\">"
			);
			
			//Add table of salespeople:
			
				//String SQL = "SELECT * FROM " + SMTablesalesperson.TableName
					//+ " ORDER BY " + SMTablesalesperson.sSalespersonCode;
				
			SQL = "SELECT "
				+ "DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ ", SALESPERSON"
				+ " FROM ("
				+ "SELECT DISTINCT " 
				
			    + " IF(" 
			    		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = '')"
			    	+ ", "
			    	+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
			    	+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				+ ") AS "  + "'SALESPERSON'"
				
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				
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
					this.toString() + ".doPost - User: " + sUserID
					+ " - "
					+ sUserFullName
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
					
					sSalespersonList.add((String) "<LABEL NAME=\"SALESPERSONLABEL" + rs.getString(SMTableorderheaders.sSalesperson) + " \" >"
							+ "<INPUT TYPE=CHECKBOX " + sCheckedStatus  
		        			+ " NAME=\"SALESPERSON" 
		        			+ rs.getString(SMTableorderheaders.sSalesperson) 
		        			+ "\">" 
		        			+ rs.getString("SALESPERSON")
		        			+ "&nbsp;" + sFirstName
		        			+ "&nbsp;" + sLastName
		        			+ "</LABEL>"
		        			);
		    	}
		        rs.close();
		        out.println(SMUtilities.Build_HTML_Table(4, sSalespersonList, 100, 1, true ,true));
		        
			}catch(SQLException e){
				out.println("<BR>Error reading salespersons with SQL: " + SQL + " - " + e.getMessage());
			}
			// End salesperson table:
			out.println("*Salespersons listed are ALL salespersons included in the order header data "
					+ "- some may no longer be in the salespersons table, and those may appear with no names.");
			out.println("<BR>");
		}
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process report----\">");
		out.println("</FORM>");
	    	
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
							+ ".getSalesPersonCode - user: " 
							+ sUserID
							+ " - "
							+ sUserFullName
					);
			if (rs.next()){
				sSalesPerson = rs.getString(SMTableusers.sDefaultSalespersonCode).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//No need to do anything here - we'll return an empty string
		}
		return sSalesPerson;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
