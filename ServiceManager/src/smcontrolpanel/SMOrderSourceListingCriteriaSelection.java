package smcontrolpanel;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import smar.ARUtilities;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.sql.*;

public class SMOrderSourceListingCriteriaSelection extends HttpServlet {
	public static final String LOCATION_PARAM = "LOCATION";
	public static final String SUMMARYONLY_PARAM = "ShowSummaryOnly";
	public static final String PIECHART_PARAM = "ShowPieChart";
	
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
				SMSystemFunctions.SMOrderSourceListing)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
    					+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
	    String title = "Marketing Source Report";
	    String subtitle = "listing criterias";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOrderSourceListingGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	
    		//Order dates:
    		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
    		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
    		out.println("<TR>");
    		out.println("<TD><B>Date range:</B><BR>"
    				+ "<span style = \" font-size: small; \" >"
    				+ "(Choose 'Previous Month', 'Current Month', or enter a date range"
    				+ " in mm/dd/yyyy format.<BR>" 
    				+ "For orders, the 'Date' refers to the Order Date, " 
    				+ "for " + SMBidEntry.ParamObjectName + ", the 'Date' refers to the " + SMBidEntry.ParamObjectName + " Date, " 
    				+ "for invoices, the 'Date' refers to the Invoice Date. )"
    				+ "</span>"
    				+ "</TD>");
    		out.println("<TD>");
    		out.println("<input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month<BR>");
    		out.println("<input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month<BR>");
    		out.println("<input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;");
    		
    		out.println(
    			"Starting:&nbsp;" 
    				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
    				//Date picker icon:
    				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
    				+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox(
    						"EndingDate", sDefaultEndDate, 10, 10, "")
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    			
    		);
    		out.println("</TD>");
    		out.println("</TR>");
    		
    		//radio button for report type
    		out.println("<TR>");
    		out.println("<TD><B>Report type:</B></TD>");
    		out.println("<TD>");
    		out.println("<input type=\"radio\" name=\"ReportType\" value=\"Order\" checked> Orders&nbsp;");
    		out.println("<input type=\"radio\" name=\"ReportType\" value=\"" + SMBidEntry.ParamObjectName + "\"> " + SMBidEntry.ParamObjectName + "s&nbsp;");
    		out.println("<input type=\"radio\" name=\"ReportType\" value=\"Invoice\"> Invoices&nbsp;");
    		out.println("</TD>");
    		out.println("</TR>");
    		
    		//checkboxes for Order types:
    		out.println("<TR>");
    		out.println("<TD><B>Include order types:<B><BR><FONT COLOR=RED SIZE=2>"
    			+ "(This only applies to orders and invoices, NOT " + SMBidEntry.ParamObjectName + "s.)</FONT></TD>");
    		out.println("<TD>");
    		
    		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC" ;
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
        	
    		//Locations:
    		out.println("<TR>");
    		out.println("<TD><B>Include locations:</B><BR><B><FONT COLOR=RED SIZE=2>"
    			+ "(This only applies to orders and invoices, NOT " + SMBidEntry.ParamObjectName + "s.)</FONT></B></TD>");
    		out.println("<TD>");
    		SQL = "SELECT"
    			+ " " + SMTablelocations.sLocation
    			+ ", " + SMTablelocations.sLocationDescription
    			+ " FROM " + SMTablelocations.TableName
    		;
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".get location - user: " +sUserID
					+ " - "
					+ sUserFullName);
				
				while (rs.next()){
  				  out.println(
						  "<INPUT TYPE=CHECKBOX NAME=\"" + LOCATION_PARAM 
						  + rs.getString(SMTablelocations.sLocation) + "\" CHECKED width=0.25>" 
						  + rs.getString(SMTablelocations.sLocation) + " - " + rs.getString(SMTablelocations.sLocationDescription) + "<BR>");
				}
				rs.close();
			} catch (Exception e) {
				out.println("Could not read locations table - " + e.getMessage());
			}
    		out.println("</TD>");
    		out.println("</TR>");
    		
    		//Report format:
    		out.println("<TR>");
    		out.println("<TD><B>Report format:</B></TD>");
    		out.println("<TD>");
        	out.println ("<INPUT TYPE=CHECKBOX NAME=\"" + SUMMARYONLY_PARAM + "\" VALUE=0>Show summary only.<BR>");
        	out.println ("<INPUT TYPE=CHECKBOX NAME=\"" + PIECHART_PARAM + "\" VALUE=0 CHECKED>Show pie chart.");
    		out.println("</TD>");
    		out.println("</TR>");
    		
	        out.println ("</TABLE>");

	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\"><BR>");

        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println ("Error in SMOrderSourceListingCriteriaSelection class - " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
