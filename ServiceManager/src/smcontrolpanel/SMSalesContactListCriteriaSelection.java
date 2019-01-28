package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMSalesContactListCriteriaSelection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if(!SMAuthenticate.authenticateSMCPCredentials(request,	response,getServletContext(), SMSystemFunctions.SMSalesContactReport)){
			return;
		}
		
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    String title = "Sales Contact Report";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?id=-1&OriginalURL=" 
	    		+ sCurrentURL 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\"><B>Create New Sales Contact</B></A>");
	    try {
        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactList\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE CELLPADDING=10 BORDER=1>");
        	
        	//select salesperson
        	String sSQL = SMMySQLs.Get_Salesperson_List_SQL();
        	ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (sDBID));
        	sSQL = SMMySQLs.Get_User_By_Username(sUserName);
        	ResultSet rsUserInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	String sDefaultSPCode;
        	if (rsUserInfo.next()){
        		sDefaultSPCode = (rsUserInfo.getString(SMTableusers.sDefaultSalespersonCode) + "").trim();
        	}else{
        		sDefaultSPCode = "";
        	}
        	rsUserInfo.close();
        	out.println("<TR><TD ALIGN=CENTER><H3>Salesperson </H3></TD>");
        	out.println("<TD><SELECT NAME=\"SelectedSalesperson\">");
        	out.println("<OPTION VALUE=ALLSP>All Salespersons");
        	while (rsSalespersons.next()){
        		if (rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).compareTo(sDefaultSPCode) == 0){
            		out.println("<OPTION SELECTED VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\">" + 
		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
		            									    rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName)); 
        		}else{
	        		out.println("<OPTION VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\">" + 
	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + " - " + 
	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) + " " + 
	        									   rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName));
        		}
        	}
        	out.println("</TD></TR>");
        	rsSalespersons.close();
        	Calendar c = Calendar.getInstance();
        	c.setTime(new Date(System.currentTimeMillis()));
        	//select last contacted
        	out.println("<TR><TD ALIGN=CENTER VALIGN=CENTER><H3>Date Criterias</H3></TD>");
        	out.println("<TD><TABLE WIDTH=100% BORDER=0>");
        	//select last contact date
        	out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=CheckLastContactDate VALUE=0>Check last contact date</TD></TR>");
        	out.println("<TR><TD>");
        	c.add(Calendar.MONTH, -1);
        	
    		out.println(clsCreateHTMLFormFields.TDTextBox(
    						"StartingLastContactDate", 
    						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("StartingLastContactDate", getServletContext())
    				);

	    	out.println(" -- ");

	    	c.add(Calendar.MONTH, 1);
    		out.println(clsCreateHTMLFormFields.TDTextBox(
					"EndingLastContactDate", 
					clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(),"M/d/yyyy"), 
					10, 
					10, 
					""
				) 
			+ SMUtilities.getDatePickerString("EndingLastContactDate", getServletContext())
			);
        	out.println("</TD>");
        	out.println("</TR>");

        	//select next contact date
        	out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=CheckNextContactDate VALUE=0 CHECKED>Check next contact date</TD></TR>");
        	out.println("<TR><TD>");
    		out.println(clsCreateHTMLFormFields.TDTextBox(
					"StartingNextContactDate", 
					"1/1/2000", 
					10, 
					10, 
					""
				) 
			+ SMUtilities.getDatePickerString("StartingNextContactDate", getServletContext())
			);

    		out.println(" -- ");
    		
    		out.println(clsCreateHTMLFormFields.TDTextBox(
					"EndingNextContactDate", 
					clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(),"M/d/yyyy"), 
					10, 
					10, 
					""
				) 
			+ SMUtilities.getDatePickerString("EndingNextContactDate", getServletContext())
			);
        	out.println("</TD>");
        	out.println("</TR>");
        	out.println("</TABLE></TD></TR>");

        	//select in/active
        	out.println("<TR><TD>Active contact only?</TD>");
        	out.println("<TD>" + 
        				"<INPUT TYPE=\"RADIO\" NAME=\"ActiveOnly\" VALUE=1 CHECKED>Yes<BR>" + 
    					"<INPUT TYPE=\"RADIO\" NAME=\"ActiveOnly\" VALUE=0>No<BR>" + 
        				"</TD></TR>");
        	
        	out.println("</TABLE>");
	    	
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OrderBy\" VALUE=\"" + SMTablesalescontacts.salespersoncode + ", " + 
        																 SMTablesalescontacts.scustomernumber + ", " + 
        																 SMTablesalescontacts.datnextcontactdate + "\">");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in SMSalesContactListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		doGet(request, response);
	}
	
}