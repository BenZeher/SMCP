package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMAverageMUReportSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String PRINTINDIVIDUAL_PARAMETER = "INDIVIDUAL";
	public static String PRINTINDIVIDUAL_VALUE_YES = "YES";
	public static String PRINTINDIVIDUAL_VALUE_NO = "NO";
	public static String INDIVIDUALSALESPERSON_PARAMETER = "INDIVIDUALSALESPERSON";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), -1L)){
			return;
		}
		
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    boolean bPrintIndividual = 
	    	clsManageRequestParameters.get_Request_Parameter(
	    	PRINTINDIVIDUAL_PARAMETER, request).compareToIgnoreCase(PRINTINDIVIDUAL_VALUE_YES) == 0;
	    
	    String title = "";
	    String sIndividualSalesperson = "";
	    if (bPrintIndividual){
		    if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMIndividualAverageMUpertruckday, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
		    	out.println("<BR><B>You do not have permission to print this report for an individual.</B><BR>");
		    	return;
		    }
		    sIndividualSalesperson = getSalespersonCode(sDBID, sUserID, sUserFullName);
		    if (sIndividualSalesperson.compareToIgnoreCase("") == 0){
		    	out.println("<BR><B>You do not have a valid salesperson code in your user set up.</B><BR>");
		    	return;
		    }
		    title = "Average Mark Up Per Truck Day Report For Salesperson " + sIndividualSalesperson;
	    }else{
		    if (!SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMAverageMUpertruckday, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
					){
		    	out.println("<BR><B>You do not have permission to print this report.</B><BR>");
		    	return;
		    }
		    title = "Average Mark Up Per Truck Day Report";
	    }
	    
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
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMAverageMUReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if (bPrintIndividual){
			out.println("<INPUT TYPE=HIDDEN NAME='" + PRINTINDIVIDUAL_PARAMETER 
				+ "' VALUE='" + PRINTINDIVIDUAL_VALUE_YES + "'>");
			out.println("<INPUT TYPE=HIDDEN NAME='" + INDIVIDUALSALESPERSON_PARAMETER 
					+ "' VALUE='" + sIndividualSalesperson + "'>");
		}else{
			out.println("<INPUT TYPE=HIDDEN NAME='" + PRINTINDIVIDUAL_PARAMETER 
					+ "' VALUE='" + PRINTINDIVIDUAL_VALUE_NO + "'>");
			out.println("<INPUT TYPE=HIDDEN NAME='" + INDIVIDUALSALESPERSON_PARAMETER 
					+ "' VALUE=''>");
		}
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Order dates:
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		out.println("<TR>");
		out.println("<TD><B>Date range (choose 'Previous Month', 'Current Month',<BR>or enter a date range"
				+ " in mm/dd/yyyy format):</B></TD>");
		out.println("<TD>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month<BR>");
		out.println("<input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;");
		
		out.println(
			"Starting:&nbsp;" 
				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
				//Date picker icon:
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox(
						"EndingDate", sDefaultEndDate, 10, 10, "")
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
			
		);
		out.println("</TD>");
		out.println("</TR>");
		
		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD>Include order types:</TD>");
		out.println("<TD>");
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName + " ORDER BY " + SMTableservicetypes.sCode ;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while(rs.next()){
				  out.println("<INPUT TYPE=CHECKBOX NAME=\"SERVICETYPE" + rs.getString(SMTableservicetypes.sCode) + "\" width=0.25>" + rs.getString(SMTableservicetypes.sName) + "<BR>");
			}
			rs.close();
		}catch (SQLException e){
			out.println("Could not read service types table - " + e.getMessage());
		}
		
		out.println("</TD>");
		out.println("</TR>");
		
		
		//Group by:
		out.println("<TR>");
		out.println("<TD>Group by:</TD>");
		out.println("<TD>");
		out.println("<input type=\"radio\" name=\"GroupBy\" value=\"OrderType\" checked> Order type, then salesperson<BR>");
		out.println("<input type=\"radio\" name=\"GroupBy\" value=\"Salesperson\"> Salesperson, then order type<BR>");
		out.println("</TD>");
		out.println("</TR>");
		
		//Show in summary
		//checkboxes for Order types:
		out.println("<TR>");
		out.println("<TD>Show summary only:</TD>");
		out.println("<TD>");
		out.println("<INPUT TYPE=CHECKBOX NAME=\"Summary\" width=0.25><BR>");
		out.println("</TD>");
		out.println("</TR>");
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process report----\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	private String getSalespersonCode(String sDBID, String sUserID, String sUserFullName){
		String SQL = "SELECT"
			+ " " + SMTableusers.sDefaultSalespersonCode
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
			;
		
		String sSalesPerson = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sDBID, "MySQL", SMUtilities.getFullClassName(this
							.toString())
							+ ".getSalesPersonCode - user: " 
							+ sUserID
							+ " - "
							+ sUserFullName
					);
			if (rs.next()){
				sSalesPerson = rs.getString(SMTableusers.sDefaultSalespersonCode).trim();
			}
			rs.close();
		} catch (SQLException e) {
			//No need to do anything here - we'll return an empty string
		}
		return sSalesPerson;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
