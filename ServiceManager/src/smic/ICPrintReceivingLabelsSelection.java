package smic;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICPrintReceivingLabelsSelection  extends HttpServlet {
	
	public static final String PRINT_RECEIVED_OR_UNRECEIVED_ITEMS = "PRINTRECEIVEDORUNRECEIVEDITEMS";
	public static final String PRINT_RECEIVED_ITEMS_VALUE = "RECEIVED";
	public static final String PRINT_UNRECEIVED_ITEMS_VALUE = "UNRECEIVED";
	public static final String PRINT_RECEIVED_ITEMS_LABEL = "Print RECEIVED items ONLY";
	public static final String PRINT_UNRECEIVED_ITEMS_LABEL = "Print UNRECEIVED items ONLY";
	public static final String STARTING_RECEIPT_DATE_PARAMETER = "STARTINGRECEIPTDATE";
	public static final String ENDING_RECEIPT_DATE_PARAMETER = "ENDINGRECEIPTDATE";
	public static final String RECEIVED_BY_PARAMETER = "RECEIVEDBY";
	public static final String RECEIVED_BY_ANYONE = "** Anyone **";
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICPrintReceivingLabels
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Print Receiving Labels";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
	    		sCompanyName)
	    );
		
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPrintReceivingLabels) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintReceivingLabelsGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4 style=\"font-size: small; \"; >");
		
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
						  "<LABEL><INPUT TYPE=CHECKBOX NAME=\"LOCATION" 
						  + rs.getString(SMTablelocations.sLocation) + "\" CHECKED width=0.25>" 
						  + rs.getString(SMTablelocations.sLocationDescription) + "<BR></LABEL>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read locations table - " + e.getMessage());
		}
		out.println("</TD>");
		out.println("</TR>");
		
		//Received/Not received:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Receipt status:<B></TD>");
		out.println("<TD>");
		//Set the default to 'UnReceived':
		String sUnreceivedChecked = "CHECKED";
		String sReceivedChecked = "";
		if (clsManageRequestParameters.get_Request_Parameter(PRINT_RECEIVED_OR_UNRECEIVED_ITEMS, request).compareToIgnoreCase(PRINT_RECEIVED_ITEMS_VALUE) == 0){
			sReceivedChecked = "CHECKED";
			sUnreceivedChecked = "";
		}
		out.println("<LABEL><input type=\"radio\" name=\"" + PRINT_RECEIVED_OR_UNRECEIVED_ITEMS + "\" value=\"" 
				+ PRINT_UNRECEIVED_ITEMS_VALUE + "\"" + sUnreceivedChecked + ">" + PRINT_UNRECEIVED_ITEMS_LABEL + "<BR></LABEL>");

		out.println("<LABEL><input type=\"radio\" name=\"" + PRINT_RECEIVED_OR_UNRECEIVED_ITEMS + "\" value=\"" 
				+ PRINT_RECEIVED_ITEMS_VALUE + "\"" + sReceivedChecked + ">" + PRINT_RECEIVED_ITEMS_LABEL + "<BR></LABEL>");
		out.println("</TD>");
		out.println("</TR>");
		
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
		
    	out.println("<TR><TD ALIGN=RIGHT><B>With expected receipt date: </B></TD>");
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
    				+ "&nbsp;NOTE: the expected date range is only used if you choose to print UNRECEIVED items."
    				);

		out.println("</TD></TR>");
		
		//Add selections for the receiving date and the status:
		
		//Receipt dates:
    	out.println("<TR><TD ALIGN=RIGHT><B>With receipt date: </B></TD>");
		out.println("<TD>");
		
		String sStartingReceiptDate = clsManageRequestParameters.get_Request_Parameter(STARTING_RECEIPT_DATE_PARAMETER, request);
		if (sStartingReceiptDate.compareToIgnoreCase("") == 0){
			sStartingReceiptDate = "1/1/1900";
		}
		String sEndingReceiptDate = clsManageRequestParameters.get_Request_Parameter(ENDING_RECEIPT_DATE_PARAMETER, request);
		if (sEndingReceiptDate.compareToIgnoreCase("") == 0){
			sEndingReceiptDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}

		out.println("Starting from "
			+ clsCreateHTMLFormFields.TDTextBox(
				STARTING_RECEIPT_DATE_PARAMETER, 
				sStartingReceiptDate, 
				10, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString(STARTING_RECEIPT_DATE_PARAMETER, getServletContext())
				);
		
		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
    			+ clsCreateHTMLFormFields.TDTextBox(
    				ENDING_RECEIPT_DATE_PARAMETER, 
    				sEndingReceiptDate, 
    				10, 
    				10, 
    				""
    				) 
    				+ SMUtilities.getDatePickerString(ENDING_RECEIPT_DATE_PARAMETER, getServletContext())
    				+ "&nbsp;NOTE: the receipt date range is only used if you choose to print RECEIVED items."
    				);

		out.println("</TD></TR>");
		
		//Received by drop-down:
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Include items received by:<B></TD>");
		out.println("<TD>");
		
		SQL = "SELECT"
			+ " DISTINCT" 
			+ " " + SMTableicporeceiptheaders.lcreatedbyid
			+ ", " + SMTableicporeceiptheaders.screatedbyfullname
			+ " FROM " + SMTableicporeceiptheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.screatedbyfullname + " != '')"
			+ ")"
			+ " ORDER BY " + SMTableicporeceiptheaders.screatedbyfullname
			;
		ArrayList <String>arrReceiptUsersFullName = new ArrayList <String>(0);
		ArrayList <String>arrReceiptUsersID = new ArrayList <String>(0);
		arrReceiptUsersFullName.add(RECEIVED_BY_ANYONE);
		arrReceiptUsersID.add("0");
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				arrReceiptUsersID.add(rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lcreatedbyid));
				arrReceiptUsersFullName.add(rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname));
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not icporeceiptheaders table - " + e.getMessage());
		}
		out.println(clsCreateHTMLFormFields.TDDropDownBox(
			RECEIVED_BY_PARAMETER, 
			arrReceiptUsersID,
			arrReceiptUsersFullName, 
			clsManageRequestParameters.get_Request_Parameter(RECEIVED_BY_PARAMETER, request))
			+ "&nbsp; NOTE: this is ignored if you choose to print only UNRECEIVED items."
		);
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
