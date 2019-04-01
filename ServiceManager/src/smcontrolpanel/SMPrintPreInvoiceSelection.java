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
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMPrintPreInvoiceSelection extends HttpServlet {
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
				SMSystemFunctions.SMPreInvoice))
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
		
	    String title = "Pre-Invoice Report";
	    String subtitle = "listing criterias";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMPreInvoice)
	    	+ "\">Summary</A><BR><BR>");
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintPreInvoiceGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<TABLE BORDER=1 WIDTH=100% CELLPADDING=1>");
    	
		String sDefaultStartDate = "1/1/2000";
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		
		//TODO FOR TESTING ONLY:
		//sDefaultStartDate = "11/3/2008";
		//sDefaultEndDate = "11/5/2008";
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Starting with order posting date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "") 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Ending with order posting date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox("EndingDate", sDefaultEndDate, 10, 10, "") 
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
				+ "</TD>");
		out.println("</TR>");

		//checkboxes for locations:
		out.println("<TR>");
		out.println("<TD><B>Include locations:<B></TD>");
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
		
		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD><B>Include service types:<B></TD>");
		out.println("<TD>");
		
		SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sName + " DESC" ;
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
		
		if (!SMUtilities.getICImportFlag(sDBID, getServletContext())){
			out.println("<TR>");
			out.println("<TD>" + "<B>Update the most recent costs on shipped items:</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDCheckBox(
						"UpdateCosts", 
						false,
						"If 'Update the most recent costs on shipped items' is checked, "
						+ "any items that are shipped but not invoiced on ALL orders will be updated "
						+ "with the most recent cost for that item in that location.  This will cause "
						+ "the Pre-Invoice Report to reflect the most up-to-date costs available at "
						+ "this time.<BR>"
						+ "NOTE: Checking this box may slow down the report."
						
				) + "</TD>");
			out.println("</TR>");
		}
        out.println("</TABLE>");
        
    	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
    	out.println("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
