package smic;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class ICPrintPhysicalInventoryWorksheet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICEditPhysicalInventory
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sPhysicalInventoryID = clsManageRequestParameters.get_Request_Parameter(
	    	ICPhysicalInventoryEntry.ParamID, request);
	    String sWorksheetStartingItem = clsManageRequestParameters.get_Request_Parameter(
		    	"WorksheetStartingItem", request);
	    String sWorksheetEndingItem = clsManageRequestParameters.get_Request_Parameter(
		    	"WorksheetEndingItem", request);
	    String sWorksheetLocation = clsManageRequestParameters.get_Request_Parameter(
		    	"WorksheetLocation", request);
	    
	    String title = "Worksheet For Physical Inventory #" + sPhysicalInventoryID;
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryWorksheetGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPhysicalInventoryEntry.ParamID 
				+ "' VALUE='" + sPhysicalInventoryID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + "WorksheetLocation"
				+ "' VALUE='" + sWorksheetLocation + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Starting Item number:
		out.println("<TD ALIGN=RIGHT>" + "<B>For items</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					"WorksheetStartingItem", 
					sWorksheetStartingItem, 
					10, 
					10, 
					""
					)
		);
		
		//Ending Item number:
		if (sWorksheetEndingItem.compareToIgnoreCase("") == 0){
			sWorksheetEndingItem = "ZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;&nbsp;And ending with:"
				+ clsCreateHTMLFormFields.TDTextBox(
					"WorksheetEndingItem", 
					sWorksheetEndingItem, 
					10, 
					10, 
					""
					));
		
		out.println("</TD></TR>");
		
		out.println("<TR><TD COLSPAN=2>");
		out.println("Show quantities on hand?&nbsp;&nbsp;");
		out.println("<INPUT TYPE=CHECKBOX " 
	        			+ " NAME=\"ShowQtyOnHand" 
	        			+ "\">"
	    );
		out.println("</TR>");
		
		out.println("<TR><TD COLSPAN=2>");
		out.println("Output to comma delimited file?&nbsp;&nbsp;");
		out.println("<INPUT TYPE=CHECKBOX " 
	        			+ " NAME=\"OutputToCSV" 
	        			+ "\">"
	    );
		out.println("</TR>");
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Print worksheet----\">");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
