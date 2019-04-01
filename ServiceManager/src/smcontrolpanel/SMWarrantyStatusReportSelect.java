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

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMWarrantyStatusReportSelect  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static String UNSELECT_ALL_SALESPERSONS_PARAMETER = "UNSELECTALLSALESPERSONS"; 
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMWarrantystatusreport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Warranty Status Report";
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
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWarrantyStatusReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");

		//Order dates:
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		out.println("<TR>");
		out.println("<TD><B>Date range (choose 'Previous Month', 'Current Month',<BR>or enter a date range"
				+ " in mm/dd/yyyy format):</B></TD>");
		out.println("<TD>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;");
		
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
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
			
		);
		out.println("</TD>");
		out.println("</TR>");
		
		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD>Include order types:<BR>");
		out.println("<TD>");
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				  out.println("<INPUT TYPE=CHECKBOX NAME=\"SERVICETYPE" + rs.getString(SMTableservicetypes.sCode) + "\" width=0.25>" + rs.getString(SMTableservicetypes.sName) + "<BR>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read service types table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");
		out.println("</TABLE>");
		
		out.println("<B><U>Including salespersons:</B></U>");
		String sChecked = "";
		if (clsManageRequestParameters.get_Request_Parameter(UNSELECT_ALL_SALESPERSONS_PARAMETER, request).compareTo("") == 0){
			sChecked = " checked=\"Yes\"";
			out.println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
				+ SMUtilities.getFullClassName(this.toString())
				+ "?" + UNSELECT_ALL_SALESPERSONS_PARAMETER + "=YES"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Unselect all salespersons</A><BR>"
			);
		}else{
			out.println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
				+ SMUtilities.getFullClassName(this.toString())
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Select all salespersons</A><BR>"
			);
		}
		out.println();
		
		//Salespersons:
		//Add table of salespeople:
		try{
			//String SQL = "SELECT * FROM " + SMTablesalesperson.TableName
				//+ " ORDER BY " + SMTablesalesperson.sSalespersonCode;
			
			SQL = "SELECT "
				+ "DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ " FROM ("
				+ "SELECT DISTINCT " + SMTableorderheaders.sSalesperson
				+ " FROM " + SMTableorderheaders.TableName
				+ " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
				+ ") AS DISTINCTSALESPEOPLE"
				+ " LEFT JOIN " + SMTablesalesperson.TableName + " ON"
				+ " DISTINCTSALESPEOPLE." + SMTableorderheaders.sSalesperson 
				+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode;
			
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
				
				sSalespersonList.add((String) "<INPUT TYPE=CHECKBOX " + sChecked 
	        			+ " NAME=\"SALESPERSON" 
	        			+ rs.getString(SMTableorderheaders.sSalesperson) 
	        			+ "\">" 
	        			+ rs.getString(SMTableorderheaders.sSalesperson)
	        			+ "&nbsp;" + sFirstName
	        			+ "&nbsp;" + sLastName
	        			);
	    	}
	        rs.close();
	        //out.println(SMUtilities.Build_HTML_Table(5, sSalespersonList,1,true));
	        
	        out.println(SMUtilities.Build_HTML_Table(4, sSalespersonList, 100, 1, true ,true));
	        
		}catch(SQLException e){
			out.println("<BR>Error reading salespersons - " + e.getMessage());
		}
		// End salesperson table:

		out.println("*Salespersons listed are ALL salespersons included in the order header data "
				+ "- some may no longer be in the salespersons table, and those may appear with no names.");
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
