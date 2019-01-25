package smcontrolpanel;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.util.Calendar;

import SMDataDefinition.SMTableordersources;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

	//OBSOLETE?
public class CustomerCallLogListCriteriaSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomerCallList))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Customer Call Log";
	    String subtitle = "listing criterias";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.CustomerCallLogListGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	//set both start end time to be today
        	out.println("<TR><TD ALIGN=CENTER><H3> Starting Date </H3></TD>");
        	//find current monday
        	Calendar c = Calendar.getInstance();
        	c.setTimeInMillis(System.currentTimeMillis());

        	out.println("<TD>");
    		out.println(
    				clsCreateHTMLFormFields.TDTextBox(
    						"StartingDate", 
    						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
    				);
        	
        	out.println("</TD>");
        	out.println("</TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H3> Ending Date </H3></TD>");
        	
        	out.println("<TD>");
    		out.println(
    				clsCreateHTMLFormFields.TDTextBox(
    						"EndingDate", 
    						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    				);
        	
        	out.println("</TD>");
        	out.println("</TR>");
        	
        	try{ 
    		    //order source list
    	        String sSQL = SMMySQLs.Get_OrderSource_List_SQL();
    	        //System.out.println("OrderSource SQL: " + sSQL);
    	        ResultSet rsOrderSources = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID));
    	    	out.println ("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Order Source</B></FONT></TD><TD VALIGN=CENTER><SELECT NAME=\"OrderSource\">");
    	    	out.println("<OPTION VALUE=0>****All Order Sources****");
    	    	while (rsOrderSources.next()){
    	    		out.println("<OPTION VALUE=\"" + rsOrderSources.getInt(SMTableordersources.iSourceID) + "\">" + rsOrderSources.getInt(SMTableordersources.iSourceID) + " - " + rsOrderSources.getString(SMTableordersources.sSourceDesc));
    	    	}
    	    	rsOrderSources.close();
    	    	out.println("</SELECT></TD></TR>");
    	    	
    	    	
    	    }catch (SQLException ex){
    	    	//catch SQL exceptions
    	    	System.out.println("Error when creating and displaying order source drop down list.");
    	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
    	    	System.out.println("SQL: " + ex.getSQLState());
    	    }

	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("<INPUT TYPE=CHECKBOX NAME=\"ShowCallDetail\" VALUE=1>Check here to show detailed call logs.");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in CustomerCallLogListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
