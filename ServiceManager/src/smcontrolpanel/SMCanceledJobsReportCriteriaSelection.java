package smcontrolpanel;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import SMDataDefinition.SMTableiccategories;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.sql.*;
import java.util.ArrayList;

public class SMCanceledJobsReportCriteriaSelection extends HttpServlet {
	
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
				SMSystemFunctions.SMCanceledJobsReport))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Canceled Jobs Report";
	    String subtitle = "listing criterias";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCanceledJobsReportGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	//set both start end time to be today
        	
        	out.println("<TR><TD ALIGN=CENTER><B>Starting Date </B></TD>");
    		out.println("<TD>"
    				+ clsCreateHTMLFormFields.TDTextBox(
    						"StartingDate", 
    						clsDateAndTimeConversions.now("M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
    				+ "</TD>");
    		out.println("</TR>");
        	
        	out.println("<TR><TD ALIGN=CENTER><B> Ending Date </B></TD>");
    		out.println("<TD>" 
    				+ clsCreateHTMLFormFields.TDTextBox(
    						"EndingDate", 
    						clsDateAndTimeConversions.now("M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    				+ "</TD>");
    		out.println("</TR>");

	        //Item Category List
    		String sSQL = "SELECT"
    			+ " " + SMTableiccategories.sCategoryCode
    			+ ", " + SMTableiccategories.sDescription
    			+ " FROM " + SMTableiccategories.TableName
    			+ " ORDER BY " + SMTableiccategories.sCategoryCode
    		;
	        
	        ResultSet rsItemCategories = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMCanceledJobsReportCriteriaSelection");
	    	out.println("<TR><TD VALIGN=CENTER><B>Item Categories</B></TD><TD VALIGN=CENTER>");
        	out.println("<TABLE WIDTH=100% BORDER=0>");
	        //make this criterion optional
	        out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=CheckItemCategories VALUE=0 CHECKED>Check item categories</TD></TR>");
	        out.println("<TR><TD><HR></TD></TR>");
        	//display available status.
        	ArrayList<String> alItemCategories = new ArrayList<String>(0);
        	while (rsItemCategories.next()){
        		//TBDL
        		//if (sDatabaseType.compareToIgnoreCase("MySQL") == 0){
            		alItemCategories.add(
               			"<INPUT TYPE=CHECKBOX NAME=!!" 
               			+ rsItemCategories.getString(SMTableiccategories.sCategoryCode) + " VALUE=0>" 
               			+ rsItemCategories.getString(SMTableiccategories.sCategoryCode));
        	
        	}
        	rsItemCategories.close();
        	out.println("<TR><TD>");
        	out.println(SMUtilities.Build_HTML_Table(12, alItemCategories, 0, false));
	        out.println("</TD></TR></TABLE>");
	        out.println("</TD></TR>");
        	
	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	//out.println ("<INPUT TYPE=CHECKBOX NAME=\"ShowCallDetail\" VALUE=1>Check here to show detailed call logs.");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in SMCanceledJobsReportCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
