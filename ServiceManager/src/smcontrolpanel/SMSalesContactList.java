package smcontrolpanel;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMSalesContactList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sCompanyName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		//String sPinNumber = TimeCardUtilities.filter(request.getParameter("PinNumber"));
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMSalesContactReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Sales Contact List";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    out.println("<BR><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A></FONT><BR><BR>");

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
	    try {
	    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy");
	    	
	    	//get all the parameters passed in
	    	String sSalesperson = clsManageRequestParameters.get_Request_Parameter("SelectedSalesperson", request);
	    	if (sSalesperson.compareTo("ALLSP") == 0){
		    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;All</FONT>");
		    }else{
		    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;" + sSalesperson + "</FONT>");
		    }
	    	
	    	int iCheckLastContactDate = 0;
	    	
	    	if (request.getParameter("CheckLastContactDate") != null){
	    		iCheckLastContactDate = 1;
	    	}else{
	    		iCheckLastContactDate = 0;
	    	}
	    	
	    	String sStartingLastContactDate 
	    		= clsManageRequestParameters.get_Request_Parameter("StartingLastContactDate", request);
	    	String sEndingLastContactDate 
    			= clsManageRequestParameters.get_Request_Parameter("EndingLastContactDate", request);
	    	String sStartingNextContactDate 
    			= clsManageRequestParameters.get_Request_Parameter("StartingNextContactDate", request);
	    	String sEndingNextContactDate 
    			= clsManageRequestParameters.get_Request_Parameter("EndingNextContactDate", request);
	    	
	    	try {
				sStartingLastContactDate 
					= clsDateAndTimeConversions.utilDateToString(
							clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingLastContactDate), "yyyy-MM-dd");
			} catch (ParseException e) {
				out.println("Error:[1423770972] Invalid starting last contact date - '" + sStartingLastContactDate + "' - " + e.getMessage());
	    		return;
			}
	    	try {
				sEndingLastContactDate 
				= clsDateAndTimeConversions.utilDateToString(
						clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingLastContactDate), "yyyy-MM-dd");
			} catch (ParseException e) {
				out.println("Error:[1423770973] Invalid ending last contact date - '" + sEndingLastContactDate + "' - " + e.getMessage());
	    		return;
			}
	    	try {
				sStartingNextContactDate 
				= clsDateAndTimeConversions.utilDateToString(
						clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingNextContactDate), "yyyy-MM-dd");
			} catch (ParseException e) {
				out.println("Error:[1423770974] Invalid starting next contact date - '" + sStartingNextContactDate + "' - " + e.getMessage());
	    		return;
			}
	    	try {
				sEndingNextContactDate 
				= clsDateAndTimeConversions.utilDateToString(
						clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingNextContactDate), "yyyy-MM-dd");
			} catch (ParseException e) {
				out.println("Error:[1423770975] Invalid ending next contact date - '" + sEndingNextContactDate + "' - " + e.getMessage());
	    		return;
			}
	    	
	    	if (iCheckLastContactDate == 0){
	    		alCriteria.add("<FONT SIZE=2><B>Last Contact Date:</B>&nbsp;All</FONT>");
	    	}else{
	    		alCriteria.add("<FONT SIZE=2><B>Last Contact Date:</B>&nbsp;" 
	    			+ sStartingLastContactDate + " - " + sEndingLastContactDate + "</FONT>");
	    	}
	    	//System.out.println("LastContactEndingDate: " + tsLastContactEndingDate.toString());
	    	int iCheckNextContactDate = 0;
	    	if (request.getParameter("CheckNextContactDate") != null){
	    		iCheckNextContactDate = 1;
	    	}else{
	    		iCheckNextContactDate = 0;
	    	}
	    	if (iCheckNextContactDate == 0){
	    		alCriteria.add("<FONT SIZE=2><B>Next Contact Date:</B>&nbsp;All</FONT>");
	    	}else{
	    		alCriteria.add("<FONT SIZE=2><B>Next Contact Date:</B>&nbsp;" 
	    			+ sStartingNextContactDate + " - " + sEndingNextContactDate + "</FONT>");
	    	}
	    	//System.out.println("NextContactEndingDate: " + tsNextContactEndingDate.toString());
	    	//see "SMSalesContactListCriteriaSelection" why this is omitted
	    	int iActiveOnly = Integer.parseInt(request.getParameter("ActiveOnly"));
	    	if (iActiveOnly == 1){
		    	alCriteria.add("<FONT SIZE=2><B>Active Only:</B> Yes</FONT>");
		    }else{
		    	alCriteria.add("<FONT SIZE=2><B>Active Only:</B> No</FONT>");
		    }
	    	
		    //log usage of this report
		    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMSALESCONTACTREPORT, "REPORT", "SMSalesContactList", "[1376509354]");
	    	
	    	String sSQL = SMMySQLs.Get_Sales_Contact_List_SQL(sSalesperson, 
													   //sCustomer,
													   iCheckLastContactDate,
													   sStartingLastContactDate,
													   sEndingLastContactDate,
													   iCheckNextContactDate,
													   sStartingNextContactDate,
													   sEndingNextContactDate,
													   iActiveOnly
													   );
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?id=-1&OriginalURL=" + sCurrentURL + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><B>Create New Sales Contact</B></A>&nbsp;&nbsp;&nbsp;&nbsp;" +
	    				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactListCriteriaSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><B>New Search</B></A><BR><BR>");
	    	
	    	//print out criteria array
	    	out.println(SMUtilities.Build_HTML_Table(4, 
				    								 alCriteria,
				    								 100,
				    								 0,
				    								 false,
				    								 false)
				    	);
	    	
	        out.println("<TABLE BORDER=1 WIDTH=100%><TR>");
	        	out.println("<TD ALIGN=CENTER><B>Rec ID</B></TD>");
				if (sSalesperson.compareTo("ALLSP") == 0){
					out.println("<TD ALIGN=CENTER><B>Salesperson</B></TD>");
				}
				out.println("<TD ALIGN=CENTER><B>Customer</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Last Inv Date</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Contact Name</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Phone</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Email</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Last Contact Date</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Next Contact Date</B></TD>");
				out.println("<TD ALIGN=CENTER><B>Status</B></TD>");
				out.println("<TD ALIGN=LEFT><B>Description</B></TD>");
			out.println("</TR>");
			
			//Get permissions for viewing customer info:
			boolean bAllowCustomerView = 
				SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ARDisplayCustomerInformation, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
			
		    int iCount = 0;
			
        	while (rs.next()){
        		out.println("<TR>");
			    iCount++;
        		
        			out.println("<TD ALIGN=CENTER VALIGN=TOP><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?id=" + rs.getInt(SMTablesalescontacts.id) + "&OriginalURL=" + sCurrentURL + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + rs.getInt(SMTablesalescontacts.id) + "</A></TD>");
        			if (sSalesperson.compareTo("ALLSP") == 0){
        				out.println("<TD ALIGN=CNETER VALIGN=TOP>" + rs.getString(SMTablesalescontacts.salespersoncode) + "-" +
					        									     rs.getString(SMTablesalesperson.sSalespersonLastName) + ", " + 
					        									     rs.getString(SMTablesalesperson.sSalespersonFirstName) + 
					        		"</TD>");
        			}
        			//Customer Number - Customer Name
		    		String sCustomerNumber = rs.getString(SMTablesalescontacts.scustomernumber).toUpperCase();
		    		String sCustomerInfoLink = sCustomerNumber;
					if (bAllowCustomerView){
						sCustomerInfoLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ sCustomerNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
							+ sCustomerNumber + "</A>"
						;
					}
        			
        			out.println(
        				"<TD ALIGN=LEFT VALIGN=TOP>" 
        				+ sCustomerInfoLink 
        				+ " - " + rs.getString(SMTablesalescontacts.scustomername) + "&nbsp;" 
        				+ "</TD>"
        			);
        			//Last Invoice Date
        			String sLastInvoiceDate = rs.getString(SMTablearcustomerstatistics.sDateOfLastInvoice);
        			if (sLastInvoiceDate == null){
        				sLastInvoiceDate = "&nbsp;";
        			}
        			if (sLastInvoiceDate.compareTo("1970-01-01") <= 0){
        				out.println("<TD ALIGN=CENTER VALIGN=TOP>&nbsp;</TD>");
        			}else{
        				out.println("<TD ALIGN=CENTER VALIGN=TOP>" + USDateOnlyformatter.format(rs.getDate(SMTablearcustomerstatistics.sDateOfLastInvoice)) + "&nbsp;</TD>");
        			}
        			//Contact Name
					out.println("<TD ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTablesalescontacts.scontactname) + "&nbsp;</TD>");
					//Phone Number
					out.println("<TD ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTablesalescontacts.sphonenumber) + "&nbsp;</TD>");
					//Email
					out.println("<TD ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTablesalescontacts.semailaddress) + "&nbsp;</TD>");
					//Last Contact Date
    				out.println("<TD ALIGN=CENTER VALIGN=TOP>" + USDateOnlyformatter.format(rs.getDate(SMTablesalescontacts.datlastcontactdate)) + "</TD>");
    				//Next Contact Date
    				out.println("<TD ALIGN=CENTER VALIGN=TOP>" + USDateOnlyformatter.format(rs.getDate(SMTablesalescontacts.datnextcontactdate)) + "</TD>");
    				//Status
    				out.print("<TD ALIGN=CENTER VALIGN=TOP>");
    				if (rs.getInt(SMTablesalescontacts.binactive) == 0){
    					out.print("Active");
    				}else{
    					out.print("In-Active");
    				}
    				out.println("</TD>");
    				out.println("<TD ALIGN=LEFT VALIGN=TOP>" + rs.getString(SMTablesalescontacts.sdescription).trim() + "&nbsp;" + "</TD>");
    				
        		out.println("</TR>");
        	}
		    out.println("<TR><TD COLSPAN=11><HR></TD></TR>");
		    out.println("<TR><TD ALIGN=RIGHT COLSPAN=10><FONT SIZE=4><B>Total Sales Contacts: " + iCount + "</B></FONT></TD></TR>");
		    
        	out.println("</TABLE>");
        	//close resultset
        	rs.close();
	    } catch (SQLException ex) {
		    // handle any errors
			out.println("<BR><BR>Error!!<BR>");
		    out.println("SQLException: " + ex.getMessage() + "<BR>");
		    out.println("SQLState: " + ex.getSQLState() + "<BR>");
		    out.println("SQL: " + "<BR>");
		}
		out.println("</BODY></HTML>");
	}
}

