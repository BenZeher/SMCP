package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARSetInactiveCustomersGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//formats
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARSetInactiveCustomers))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String m_sWarning = "";
	    /**************Get Parameters**************/
	    String sLastActivityDate = clsManageRequestParameters.get_Request_Parameter("LastActivityDate", request);
	  
    	//Convert the date to a SQL one:
    	java.sql.Date datLastActivityDate = null;
		try {
			datLastActivityDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sLastActivityDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423841180] Invalid last activity date: '" + sLastActivityDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
	    
    	String sStartingCustomer = request.getParameter("StartingCustomer").trim().toUpperCase();
    	String sEndingCustomer = request.getParameter("EndingCustomer").trim().toUpperCase();
	    if (sStartingCustomer.compareToIgnoreCase(sEndingCustomer) == 1){
	    	m_sWarning = "Starting customer must be alphabetically BEFORE ending customer.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
	    }    	
    	
    	boolean bIncludeInactives = false;
    	if (request.getParameter("IncludeInactives").compareToIgnoreCase("TRUE") == 0){
    		bIncludeInactives = true;
    	}
    	/**************End Parameters**************/
    	
    	//Customized title
    	String sReportTitle = "";
    	if (bIncludeInactives){
    		sReportTitle = "Set INACTIVE customers to ACTIVE";
    	}else{
    		sReportTitle = "Set ACTIVE customers to INACTIVE";
    	}
    	String sIncludeInactives = "ACTIVE";
    	if(bIncludeInactives){
    		sIncludeInactives = "INACTIVE";
    	}
    	String sCriteria = "Starting with customer <B>" + sStartingCustomer + "</B>"
    		+ ", ending with customer <B>" + sEndingCustomer + "</B>, " 
    		+ "<B>INCLUDING ONLY </B> customers which are currently " + sIncludeInactives
    		+ ", listing only those customers which:&nbsp;"
    		+ "1) have no activity since <B>" + sLastActivityDate + "</B>&nbsp;"
    		+ "and 2) have <B>NO</B> open transactions<BR>"
    		;
    	 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A></TD></TR></TABLE>");
    	
    	out.println();
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSetInactiveCustomersAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	if (bIncludeInactives){
    		out.println("<INPUT TYPE=HIDDEN NAME='SETACTIVEFLAGTO' VALUE='1'>");
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked customers to ACTIVE----\">");
    	}else{
    		out.println("<INPUT TYPE=HIDDEN NAME='SETACTIVEFLAGTO' VALUE='0'>");
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked customers to INACTIVE----\">");
    	}
    	
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	
    	try {
			printList(
				getServletContext(),
				sDBID,
				sStartingCustomer,
				sEndingCustomer,
				datLastActivityDate,
				bIncludeInactives,
				out,
				sUserID,
				sUserFullName
			);
		} catch (Exception e) {
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + e.getMessage()
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    		
    	if (bIncludeInactives){
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked customers to ACTIVE----\">");
    	}else{
    		out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Set checked customers to INACTIVE----\">");
    	}
    	out.println("</FORM>");
	    out.println("</BODY></HTML>");
	}
	
	private void printHeading(PrintWriter pwOut, boolean bInactive){
		
		String sCheckBoxHeading = "Make INACTIVE?";
		if (bInactive){
			sCheckBoxHeading = "Make ACTIVE?";
		}
		
		pwOut.println("<TABLE WIDTH=100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		pwOut.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> " + sCheckBoxHeading + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Customer #</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Name</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Last invoice</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Last payment</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Last credit</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR><TD COLSPAN=6 CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD><TR>");
		
	}
	private void printList(
    		ServletContext context,
    		String sDBID,
    		String sStartingCust,
    		String sEndingCust,
    		java.sql.Date datLastActivity,
    		boolean bIncInactives,
    		PrintWriter pwOut,
    		String sUserID,
    		String sUserFullName
    		) throws Exception{

		String SQL = "SELECT "
			+ "NothingCurrentCustomers." + SMTablearcustomer.sCustomerNumber
			+ ", " + "NothingCurrentCustomers." + SMTablearcustomer.sCustomerName
			+ ", " + "NothingCurrentCustomers." + SMTablearcustomer.iActive
			+ ", " + SMTablearcustomerstatistics.TableName + "." +  SMTablearcustomerstatistics.sDateOfLastInvoice
			+ ", " + SMTablearcustomerstatistics.TableName + "." +  SMTablearcustomerstatistics.sDateOfLastPayment
			+ ", " + SMTablearcustomerstatistics.TableName + "." +  SMTablearcustomerstatistics.sDateOfLastCredit
			
			+ " FROM " + SMTablearcustomerstatistics.TableName + ", "  
			+ "(SELECT " 
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber 
				+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName 
				+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.iActive 
				+ ", NothingCurrent." + SMTableartransactions.spayeepayor 
				+ ", NothingCurrent.OpenDocCount"
			+ " FROM " + SMTablearcustomer.TableName + " LEFT JOIN" 
			+ " (SELECT " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor
			+ ", Count(" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor
			+ ") AS OpenDocCount"
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE (((" + SMTableartransactions.TableName + "." 
			+ SMTableartransactions.dcurrentamt + ")<>0))"
			+ " GROUP BY " + SMTableartransactions.TableName + "." 
			+ SMTableartransactions.spayeepayor + ") AS NothingCurrent" 
			+ " ON " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
			+ " = NothingCurrent." + SMTableartransactions.spayeepayor
			+ " WHERE ("
				+ "ISNULL(NothingCurrent." + SMTableartransactions.spayeepayor + ") = TRUE"
			+ ")"
			+ ") AS NothingCurrentCustomers"
			+ " WHERE ("
			//WHERE CLAUSE:
			+ "(NothingCurrentCustomers." + SMTablearcustomer.sCustomerNumber 
			+ " = " + SMTablearcustomerstatistics.TableName + "." 
			+ SMTablearcustomerstatistics.sCustomerNumber + ")"
			+ " AND (NothingCurrentCustomers." + SMTablearcustomer.sCustomerNumber + " >= '" 
			+ sStartingCust + "')"
			+ " AND (NothingCurrentCustomers." + SMTablearcustomer.sCustomerNumber + " <= '" 
			+ sEndingCust + "')"

			//Dates:
			+ " AND (" + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastInvoice
				+ " <= '" + clsDateAndTimeConversions.utilDateToString(datLastActivity, "yyyy-MM-dd") + " 23:59:59')"
				
			+ " AND (" + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastPayment
				+ " <= '" + clsDateAndTimeConversions.utilDateToString(datLastActivity, "yyyy-MM-dd") + " 23:59:59')"

			+ " AND (" + SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sDateOfLastCredit
				+ " <= '" + clsDateAndTimeConversions.utilDateToString(datLastActivity, "yyyy-MM-dd") + " 23:59:59')"	
			;	
			//Inactives?
			if(bIncInactives){
				SQL = SQL + " AND (NothingCurrentCustomers." + SMTablearcustomer.iActive
				+ " = 0)";
			}else{
				SQL = SQL + " AND (NothingCurrentCustomers." + SMTablearcustomer.iActive
				+ " = 1)";
			}
			SQL = SQL + ")";

    	//System.out.println("In " + this.getServletName() + ".printList, SQL = " + SQL);
    	int iLinesPrinted = 0;
    	long lCustomersPrinted = 0;
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context,
    				sDBID,
    				"MySQL",
    				this.toString() + ".printList - User: " + sUserID
    				+ " - "
    				+ sUserFullName
    				);
    		printHeading(pwOut, bIncInactives);
    		while (rs.next()){

    			if(iLinesPrinted%2 == 0) {
    				pwOut.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
    			}else {
    				pwOut.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
    			}
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + clsCreateHTMLFormFields.TDCheckBox(
    	    			"CUSTNUM" + rs.getString(SMTablearcustomer.sCustomerNumber), false, "") + "</TD>");
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + rs.getString(SMTablearcustomer.sCustomerNumber) + "</TD>");
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + rs.getString(SMTablearcustomer.sCustomerName) + "</TD>");
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + reverseSQLDateString(rs.getString(SMTablearcustomerstatistics.TableName + "." 
        				+ SMTablearcustomerstatistics.sDateOfLastInvoice)) + "</TD>");
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + reverseSQLDateString(rs.getString(SMTablearcustomerstatistics.TableName + "." 
        				+ SMTablearcustomerstatistics.sDateOfLastPayment)) + "</TD>");
    			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"> " + reverseSQLDateString(rs.getString(SMTablearcustomerstatistics.TableName + "." 
        				+ SMTablearcustomerstatistics.sDateOfLastCredit)) + "</TD>");	
    			pwOut.println("</TR>");
    			iLinesPrinted++;
    			lCustomersPrinted++;
    		}
    		rs.close();
    		pwOut.println("</TABLE><BR>");
    		pwOut.println(lCustomersPrinted + " customers printed.");
    	}catch (SQLException e){
    		throw new Exception("Error [1548726997] reading customers to set to inactive - " + e.getMessage() + ".");
    	}
		return;
	}
	private String reverseSQLDateString(String sSQLDateString){
		
		if (sSQLDateString.compareToIgnoreCase("1899-12-31") == 0){
			return "00/00/0000";
		}else{
		return sSQLDateString.substring(5, 7)
			+ "/" + sSQLDateString.substring(8, 10)
			+ "/" + sSQLDateString.substring(0, 4);

		}
	}
}
