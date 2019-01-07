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
import SMDataDefinition.SMTableicitemstatistics;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICClearStatisticsSelection  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearStatistics)){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    String title = "Clear IC Statistics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICClearStatistics) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("NOTE: This function will clear ALL item statistics OLDER THAN the"
	    		+ " YEAR and MONTH selected.<BR><BR>");
	    
	    try {
	    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICClearStatisticsAction\">");
	    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");

	    	//Transaction Date Range
	    	out.println("Clear all monthly statistics BEFORE:");
	    	
	    	String SQL = "SELECT DISTINCT "
	    		+ SMTableicitemstatistics.lYear
	    		+ " FROM " + SMTableicitemstatistics.TableName
	    		+ " ORDER BY " + SMTableicitemstatistics.lYear
	    		;
	    	
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(
	    		SQL, 
	    		getServletContext(),
	    		sDBID,
	    		"MySQL",
	    		this.toString() + ".doPost - User: " 
	    		+ sUserID
	    		+ " - "
	    		+ sUserFullName);
	    	out.println("Year - ");
	    	out.println("<SELECT NAME = \"" + "ClearBeforeYear" + "\">");
	    	while (rs.next()){
	    		String sYear = Long.toString(rs.getLong(SMTableicitemstatistics.lYear));
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
	    	
	    	out.println("<BR><BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Clear statistics ----\">");
	    	out.println(clsCreateHTMLFormFields.TDCheckBox("ConfirmClearing", false, "Confirm clearing"));
	    	
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
