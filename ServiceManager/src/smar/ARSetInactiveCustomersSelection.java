package smar;

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
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ARSetInactiveCustomersSelection  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
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
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Set Inactive Customers";
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
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARSetInactiveCustomers) 
	    		+ "\">Summary</A><BR>");
	    
	    try {
	    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSetInactiveCustomersGenerate\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    	out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
	    	out.println("<TR>");
	    	out.println("<TD ALIGN=RIGHT><B>Customer Selection:</B></TD>");
	    	
	    	//get customer list from database
	    	String sSQL =  "SELECT " 
	    			+ SMTablearcustomer.sCustomerNumber + ", "
	    			+ SMTablearcustomer.sCustomerName
	    			+ " FROM " + SMTablearcustomer.TableName
	    			+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " ASC LIMIT 1";
	    	ResultSet rsCustomers = clsDatabaseFunctions.openResultSet(
	    			sSQL, 
	    			getServletContext(), 
	    			sDBID,
	    			"MySQL",
	    			this.toString() + ".doPost (1) - User: " + sUserID
	    			+ " - "
	    			+ sUserFullName
	    			);
	    	String sStartingCustomerNumber = "";
	    	if (rsCustomers.next()){
	    		sStartingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
	    	}
	    	rsCustomers.close();
	    	out.println("<TD>" + "<B>Starting with:</B> " + clsCreateHTMLFormFields.TDTextBox("StartingCustomer", sStartingCustomerNumber, 10, 10, "") + "</TD>");
	    	
	    	sSQL =  "SELECT " 
	    			+ SMTablearcustomer.sCustomerNumber + ", "
	    			+ SMTablearcustomer.sCustomerName
	    			+ " FROM " + SMTablearcustomer.TableName
	    			+ " ORDER BY " + SMTablearcustomer.sCustomerNumber + " DESC LIMIT 1";
	    	rsCustomers = clsDatabaseFunctions.openResultSet(
	    			sSQL, 
	    			getServletContext(), 
	    			sDBID,
	    			"MySQL",
	    			this.toString() + ".doPost (2) - User: " + sUserID
	    			+ " - "
	    			+ sUserFullName
	    			);
	    	String sEndingCustomerNumber = "";
	    	if (rsCustomers.next()){
	    		sEndingCustomerNumber = rsCustomers.getString(SMTablearcustomer.sCustomerNumber);
	    	}
	    	rsCustomers.close();
	    	out.println("<TD>" + "<B>Ending with:</B> " + clsCreateHTMLFormFields.TDTextBox("EndingCustomer", sEndingCustomerNumber, 10, 10, "") + "</TD>");
	    	out.println("</TR>");
	    	
	    	//Transaction Date Range
		    out.println(ARUtilities.Create_Edit_Form_DateText_Input_Row(
		    		"LastActivityDate", 
		    		"1/1/1900", 
		    		10, 
		    		"Show customers with no activity since:", 
		    		"(Enter as mm/dd/yyyy)", 
		    		"1.00",
		    		getServletContext()
		    		)
		    		);
	    	
	    	//out.println("<TR>");
	    	//out.println("<TD ALIGN=LEFT><B>Show customers with no activity since:</B></TD>");
	    	
	    	//+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
	    	
	    	
	    	//out.println("<TD>" 
	    	//		+ ARUtilities.TDTextBox("LastActivityDate", "1/1/1900", 10, 10, "(Enter as mm/dd/yyyy)")
	    	//		+ "</TD>");
	    	//out.println("<TD>" + "&nbsp;</TD>");
	    	//out.println("</TR>");
	    	
	    	out.println("<TR>");
	    	
	    	out.println("<TD ALIGN=RIGHT><B>List ONLY:</B></TD>");
	    	
	    	//Drop down list:
	    	out.println("<TD>");
	    	out.println("<SELECT NAME = \"" + "IncludeInactives" + "\">");
	    	out.println("<OPTION VALUE=\"" + "TRUE" + "\">" + "INACTIVE customers");
	    	out.println("<OPTION VALUE=\"" + "FALSE" + "\">" + "ACTIVE customers");
	    	out.println("</SELECT>");
	    	
	    	out.println("</TD>");
	    	out.println("<TD>" + "&nbsp;</TD>");
	    	out.println("</TR>");
	    	
	    	out.println("</TABLE>");
	    	out.println("<BR>");
	    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
	    	out.println("</FORM>");
	    	
	    } catch (SQLException ex) {
	        // handle any errors
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
