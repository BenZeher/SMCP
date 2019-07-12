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
import SMDataDefinition.SMTablearoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ARSelectForCustomerNumberChange  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARRenumberMergeCustomers))
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
	    String title = "Change/Merge Customer code";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B><FONT>NOTE: " + sStatus + "</FONT></B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARRenumberMergeCustomers) 
	    		+ "\">Summary</A><BR>");
	    
	    //If the iflagimports flag is NOT set, don't allow customer number merges because we don't
	    //want to write to any data in SM:
	    String SQL = "SELECT " + SMTablearoptions.iflagimports + " FROM " + SMTablearoptions.TableName;
	    boolean iFlagInvoices = false;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(),
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".doPost - user: " + sUserID
							+ " - "
							+ sUserFullName
							));
			if (rs.next()){
				if (rs.getLong(SMTablearoptions.iflagimports) == 1){
					iFlagInvoices = true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading the 'flag invoice' flag - " + e.getMessage() + ".  Customers cannot "
					+ "be processed at this time.</BODY></HTML>");
			return;
		}
	    
		if (!iFlagInvoices){
			out.println("The 'flag invoice' flag is set to FALSE - Customers cannot be processed at "
					+ "this time because the flag indicates that Servce Manager data cannot be altered."
					+ "</BODY></HTML>");
			return;
		}
		
	    out.println("<BR>If you selected CHANGE TO, then all of the CURRENT customer's transactions will be" 
	    		+ " marked with the NEW customer code.  The customer master, "
	    		+ "AR chronological log, customer ship-to's, customer statistics,"
	    		+ " monthly statistics, any transactions, any applying (payment) records, call sheets, "
	    		+ "batch entries,"
	    		+ " ALL invoices, ALL orders (open and closed), ALL sales contacts, ALL site locations"
	    		+ "  and ALL SpeedSearch records will be updated to the new customer code.  If"
	    		+ " the new customer code is already in AR, or Service Manager,"
	    		+ " it will warn you and it won't make the change.<BR><BR>"
	    		+ "If you selected MERGE INTO, then all of the CURRENT customer's transactions "
	    		+ "will be changed"
	    		+ " to the MERGE INTO customer code.  The CURRENT customer master will be deleted,"
	    		+ " the AR chronological log will be merged, the customer"
	    		+ " ship-to's and site locations will be added (if possible) to the MERGE INTO customer's.  The"
	    		+ " customer ship-tos, site locations, customer statistics, any transactions, any applying"
	    		+ " (payment) records,"
	    		+ " ALL batch entries,"
	    		+ " ALL orders (open and closed), ALL invoices, ALL call sheets"
	    		+ " ALL sales contacts, ALL site locations,"
	    		+ " and ALL"
	    		+ " SpeedSearch records will be updated to the MERGE INTO customer code.  If the MERGE INTO"
	    		+ " customer code is NOT in AR, it will warn you and it won't make the change.  If there are"
	    		+ " duplicate document numbers in A/R for the CURRENT and the MERGE INTO customer, it will warn"
	    		+ " you and it won't make the change."
	    		+ "  If there are duplicate ship-to's, call sheets, or sales contacts, it will warn you"
	    		+ " and it will not make the change."
	    		+ "<BR><BR>"
	    		);
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARCustomerNumberChangeGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE BORDER=1>");
		out.println("<TR><TD><B>Change type:</B></TD></TR>");
		out.println("<TR><TD><INPUT TYPE=\"radio\" name=\"GROUPTYPE\" value=\"Change\">Change to</TD></TR>");
		out.println("<TR><TD><INPUT TYPE=\"radio\" name=\"GROUPTYPE\" value=\"Merge\">Merge into</TD></TR>");
		out.println("</TABLE>");
		out.println("<B>Change (merge) FROM customer code:</B>&nbsp;(Type the CURRENT customer code here):");
		out.println("<INPUT TYPE=TEXT NAME=\"" + "FromCustomer" + "\" SIZE=" 
				+ SMTablearcustomer.sCustomerNumberLength + "; MAXLENGTH=15 >");
		out.println ("<BR>");
		out.println("<B>Change (merge) TO customer code:</B>&nbsp;(Type the NEW customer code here):");
		out.println("<INPUT TYPE=TEXT NAME=\"" + "ToCustomer" + "\" SIZE=" 
				+ SMTablearcustomer.sCustomerNumberLength + "; MAXLENGTH=15 >");
		out.println ("<BR><BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process change/merge----\">");
		out.println("  Check to confirm change: <INPUT TYPE=CHECKBOX NAME=\"ConfirmChange\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
