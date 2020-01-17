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

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMSalesEffortCheckSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String SALESPERSON_CHECKBOX = "SALESPERSONCHECKBOX";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMJobCostDailyReport
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    String title = "Sales Effort Check";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMSalesEffortCheck)
		    	+ "\">Summary</A><BR>");
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMJobCostDailyReportGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<TABLE BORDER=1 WIDTH=100% CELLPADDING=1>");
    	
		//Order dates:
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");

		//Group by:
		out.println("<TR>");
		out.println("<TD><B>Date range (choose 'Previous Month', 'Current Month',<BR>or enter a date range"
				+ " in mm/dd/yyyy format):</B></TD>");
		out.println("<TD>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;"
			+ "Starting:&nbsp;" + clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
			+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
			+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox("EndingDate", sDefaultEndDate, 10, 10, "")
			+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
		);
		out.println("</TD>");
		out.println("</TR>");
		
		createSalespersonSelections(out, sDBID, sUserID, sUserFullName);
			    
		out.println("<TR>");
		out.println("<TD>" + "<B>Suppress details:</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDCheckBox("SuppressDetails", false, "Check this to ONLY print the overall totals") + "</TD>");
		out.println("</TR>");
	    
        out.println("</TABLE>");
        
    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	    //out.println(SMUtilities.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
    	out.println("</FORM>");
		    
	    out.println("</BODY></HTML>");
	}
	private void createSalespersonSelections(PrintWriter pwOut, String sDBID, String sUserID, String sUserFullName){
    	try{ 
	        //Mechanic List
	        String SQL = "SELECT * FROM " + SMTablesalesperson.TableName
	        	+ " ORDER BY " + SMTablesalesperson.sSalespersonCode
	        ;
	        ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID, 
	        	"MySQL", 
	        	SMUtilities.getFullClassName(this.toString()) 
	        		+ ".createSalespersonSelections - user: " + sUserID
	        		+ " - "
	        		+ sUserFullName
	        		);
	        pwOut.println("<TR><TD VALIGN=TOP><B>Salespersons</B></FONT></TD><TD VALIGN=CENTER>");
	        pwOut.println("<TABLE WIDTH=100% BORDER=0>");

        	ArrayList <String> alSalepersons = new ArrayList<String>(0);
        	while (rsSalespersons.next()){
        		alSalepersons.add("<INPUT TYPE=CHECKBOX NAME=\"" + SALESPERSON_CHECKBOX 
       				+ rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\" VALUE=0 >"
       				+ rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - "
       				+ rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " "
       				+ rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName)
       				);
        	}
        	rsSalespersons.close();
        	pwOut.println("<TR><TD>");
        	pwOut.println(SMUtilities.Build_HTML_Table(2, alSalepersons, 0, false));
        	pwOut.println("</TD></TR></TABLE>");
        	pwOut.println("</TD></TR>");
	        
	    }catch (SQLException ex){
	    	//catch SQL exceptions
	    	System.out.println("[1579274949] Error: " + ex.getErrorCode() + " - " + ex.getMessage());
	    	System.out.println("SQL: " + ex.getSQLState());
	    }

	}
}
