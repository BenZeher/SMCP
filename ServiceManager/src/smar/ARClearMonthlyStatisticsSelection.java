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
import SMDataDefinition.SMTablearmonthlystatistics;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ARClearMonthlyStatisticsSelection  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARClearMonthlyStatistics)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Clear Monthly Statistics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARClearMonthlyStatistics)
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("NOTE: This function will clear ALL monthly statistics for ALL customers OLDER THAN the"
	    		+ " YEAR and MONTH selected.<BR><BR>");
	    
	    try {
	    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARClearMonthlyStatisticsAction\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");

	    	//Transaction Date Range
	    	out.println("Clear all monthly statistics BEFORE:");
	    	
	    	String SQL = "SELECT DISTINCT "
	    		+ SMTablearmonthlystatistics.sYear
	    		+ " FROM " + SMTablearmonthlystatistics.TableName
	    		+ " ORDER BY " + SMTablearmonthlystatistics.sYear
	    		;
	    	
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(
	    		SQL, 
	    		getServletContext(),
	    		sDBID,
	    		"MySQL",
	    		this.toString() + ".doPost - User: " + sUserID
	    		+ " - "
	    		+ sUserFullName
	    			);
	    	out.println("Year - ");
	    	out.println("<SELECT NAME = \"" + "ClearBeforeYear" + "\">");
	    	while (rs.next()){
	    		String sYear = Long.toString(rs.getLong(SMTablearmonthlystatistics.sYear));
	    		out.println("<OPTION VALUE=\"" + sYear + "\">" + sYear);
	    	}
	    	rs.close();
	    	//Drop down list:
	    	out.println("</SELECT>");
	    	
	    	out.println("Month - ");
	    	out.println("<SELECT NAME = \"" + "ClearBeforeMonth" + "\">");
	    	for (int iMonth = 1; iMonth < 13; iMonth++){
	    		out.println("<OPTION VALUE=\"" + Integer.toString(iMonth) + "\">" + Integer.toString(iMonth));
	    	}
	    	//Drop down list:
	    	out.println("</SELECT>");
	    	
	    	out.println("<BR><BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Clear monthly statistics ----\">");
	    	out.println(clsCreateHTMLFormFields.TDCheckBox("ConfirmClearing", false, "Confirm clearing"));
	    	
	    	out.println("</FORM>");
	    	
	    } catch (SQLException ex) {
	        // handle any errors
	    	System.out.println("[1579115123] Error in " + this.toString() + " class!!");
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
