package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICPOReceivingReportSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICPOReceivingReport
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Items To Be Received Listed By PO";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPOReceivingReport) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPOReceivingReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		
		//Starting and ending vendor numbers:
		String sStartingVendor = clsManageRequestParameters.get_Request_Parameter("StartingVendor", request);
		if (sStartingVendor.compareToIgnoreCase("") == 0){
			sStartingVendor = "";
		}
		String sEndingVendor = clsManageRequestParameters.get_Request_Parameter("EndingVendor", request);
		if (sEndingVendor.compareToIgnoreCase("") == 0){
			sEndingVendor = clsStringFunctions.PadLeft("", "Z", SMTableicpoheaders.svendorLength);
		}
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>For vendors:<B></TD>");
		out.println("<TD>");
		out.println("Starting from "
				+ clsCreateHTMLFormFields.TDTextBox(
					"StartingVendor", 
					sStartingVendor, 
					12, 
					SMTableicpoheaders.svendorLength, 
					"") 
					);
			
			out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
	    			+ clsCreateHTMLFormFields.TDTextBox(
	    				"EndingVendor", 
	    				sEndingVendor, 
	    				12, 
	    				SMTableicpoheaders.svendorLength, 
	    				"") 
	    				);
		out.println("</TD>");
		out.println("</TR>");
		
		//checkboxes for locations:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Include locations:<B></TD>");
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
		
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Show Items that are fully received</B></TD>");
		out.println("<TD><INPUT TYPE = CHECKBOX NAME = \"itemfullyreceived\" id = \"itemfullyreceived\"></INPUT></TD>");
    	out.println("<TR><TD ALIGN=RIGHT><B>With PO date: </B></TD>");
		out.println("<TD>");
		
		String sStartingPODate = clsManageRequestParameters.get_Request_Parameter("StartingPODate", request);
		if (sStartingPODate.compareToIgnoreCase("") == 0){
			sStartingPODate = "1/1/1900";
		}
		String sEndingPODate = clsManageRequestParameters.get_Request_Parameter("EndingPODate", request);
		if (sEndingPODate.compareToIgnoreCase("") == 0){
			sEndingPODate = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		out.println("Starting from "
			+ clsCreateHTMLFormFields.TDTextBox(
				"StartingPODate", 
				sStartingPODate, 
				10, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString("StartingPODate", getServletContext())
				);
		
		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
    			+ clsCreateHTMLFormFields.TDTextBox(
    				"EndingPODate", 
    				sEndingPODate, 
    				10, 
    				10, 
    				""
    				) 
    				+ SMUtilities.getDatePickerString("EndingPODate", getServletContext())
    				);

		out.println("</TD></TR>");
		
    	out.println("<TR><TD ALIGN=RIGHT><B>With arrival date: </B></TD>");
		out.println("<TD>");
		
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		if (sStartingDate.compareToIgnoreCase("") == 0){
			sStartingDate = "1/1/1900";
		}
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
		if (sEndingDate.compareToIgnoreCase("") == 0){
			sEndingDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		out.println("Starting from "
			+ clsCreateHTMLFormFields.TDTextBox(
				"StartingDate", 
				sStartingDate, 
				10, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				);
		
		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
    			+ clsCreateHTMLFormFields.TDTextBox(
    				"EndingDate", 
    				sEndingDate, 
    				10, 
    				10, 
    				""
    				) 
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    				);

		out.println("</TD></TR>");
		
		out.println("<TR><TD ALIGN=RIGHT><B>Export report:</B></TD>");
		out.println("<TD>Output to comma delimited file?&nbsp;&nbsp;");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"OutputToCSV\">");
		out.println("</TD></TR>");
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
