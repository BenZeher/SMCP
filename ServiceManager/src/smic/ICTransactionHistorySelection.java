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
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICTransactionHistorySelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICTransactionHistory
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "IC Transaction History";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICTransactionHistory) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICTransactionHistoryGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Starting Item number:
		out.println("<TD ALIGN=RIGHT>" + "<B>List transaction history for items:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					"StartingItemNumber", 
					clsManageRequestParameters.get_Request_Parameter("StartingItemNumber", request), 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					)
		);

		//Ending Item number:
		String sEndingItem = clsManageRequestParameters.get_Request_Parameter("EndingItemNumber", request);
		if (sEndingItem.compareToIgnoreCase("") == 0){
			sEndingItem = "ZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;&nbsp;And ending with:"
				+ clsCreateHTMLFormFields.TDTextBox(
					"EndingItemNumber", 
					sEndingItem, 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		out.println("</TD></TR>");

    	out.println("<TR><TD ALIGN=RIGHT><B>With transaction dates: </B></TD>");
		out.println("<TD>");
		
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		if (sStartingDate.compareToIgnoreCase("") == 0){
			sStartingDate = clsDateAndTimeConversions.now("M/d/yyyy");
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
		
		//checkboxes for transactiontypes:
		out.println("<TR>");
		out.println("<TD VALIGN=TOP ALIGN=RIGHT><B>Include transaction types:<B></TD>");
		out.println("<TD>");
		
		//public static int SHIPMENT_ENTRY = 0;
		//public static int RECEIPT_ENTRY = 1;
		//public static int ADJUSTMENT_ENTRY = 2;
		//public static int TRANSFER_ENTRY = 3;
		//public static int PHYSICALCOUNT_ENTRY = 4;
		String sChecked = "";
		
		for (int i = 0; i <= 4; i++){
			if (clsManageRequestParameters.get_Request_Parameter("TRANSACTIONTYPE" 
					+ Integer.toString(i), request) != null){
				sChecked = "CHECKED";
			}
			out.println(
				"<INPUT TYPE=CHECKBOX NAME=\"TRANSACTIONTYPE" 
					+ Integer.toString(i) 
					+ "\" " + sChecked + " width=0.25>" 
					+ ICEntryTypes.Get_Entry_Type(i)
					+ "<BR>"
			);
		}
		
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
