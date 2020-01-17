package smcontrolpanel;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import SMClasses.MySQLs;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

import SMDataDefinition.SMTableprojecttypes;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;


public class SMBidFollowUpCriteriaSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.SMBidFollowUpReport)
		){
			return;
		}

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = SMBidEntry.ParamObjectName + " Follow-Up List";
	    String subtitle = "select criteria";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Return to user login</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMBidFollowUpGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	
        	//Starting salesperson
        	String sSQL = MySQLs.Get_Salesperson_List_SQL();
        	ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (sDBID));
        	sSQL = MySQLs.Get_User_By_Username(sUserName);
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

    		//Project Type
    		ArrayList<String> alValues = new ArrayList<String>(0);
    		ArrayList<String> alTexts = new ArrayList<String>(0);
    		sSQL = "SELECT * FROM " + SMTableprojecttypes.TableName;
    		ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
    		alValues.add("0");
    		alTexts.add("All Types");
    		while (rsProjectTypes.next()){
    			alValues.add(rsProjectTypes.getString(SMTableprojecttypes.iTypeId));
    			alTexts.add(rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc));
    		}
    		rsProjectTypes.close();
    		
    		if (alValues.size() > 0){
    			out.println("<TR><TD ALIGN=CENTER><B>Project Type</B></TD><TD>" + clsCreateHTMLFormFields.TDDropDownBox("ProjectType", alValues, alTexts) + "</TD></TR>");
    		}else{
    			//no project type
    			out.println("<TR><TD ALIGN=CENTER><B>Project Type</B></TD><TD>NO project type in system.</TD>");
    			out.println("<INPUT TYPE=HIDDEN NAME=\"ProjectType\" VALUE=\"0\">");
    		}
        	
        	Calendar c = Calendar.getInstance();
        	c.setTimeInMillis(System.currentTimeMillis());
        	c.add(Calendar.MONTH, -1);
        	//Last Contact Date Range
        	out.println("<TR><TD ALIGN=CENTER><H3>Last Contact Date </H3></TD>");
       		out.println("<TD><LABEL><INPUT TYPE=CHECKBOX NAME=CheckLastContactDate VALUE=0>"
        				+ "<B>Use the following range of last contact dates to limit the list</B></LABEL><HR>");
        		
    		out.println("Starting from "
        			+ clsCreateHTMLFormFields.TDTextBox(
        				"LastContactStartDate", 
        				clsDateAndTimeConversions.CalendarToString(c, "M/d/yyyy"),
        				10, 
        				10, 
        				""
        				) 
        				+ SMUtilities.getDatePickerString("LastContactStartDate", getServletContext())
        				);
    		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
        			+ clsCreateHTMLFormFields.TDTextBox(
        				"LastContactEndDate", 
        				clsDateAndTimeConversions.now("M/d/yyyy"),
        				10, 
        				10, 
        				""
        				) 
        				+ SMUtilities.getDatePickerString("LastContactEndDate", getServletContext())
        				);
        	out.println("</TR>");
        	//Next Contact Date Range
        	out.println("<TR><TD ALIGN=CENTER><H3>Next Contact Date </H3></TD>");
        		out.println("<TD><LABEL><INPUT TYPE=CHECKBOX NAME=CheckNextContactDate VALUE=0 CHECKED>"
        				+ "<B>Use the following range of next contact dates to limit the list</B></LABEL><HR>");
        		
    		out.println("Starting from "
        			+ clsCreateHTMLFormFields.TDTextBox(
        				"NextContactStartDate", 
        				"1/1/2000",
        				10, 
        				10, 
        				""
        				) 
        				+ SMUtilities.getDatePickerString("NextContactStartDate", getServletContext())
        				);
    		out.println("&nbsp;&nbsp;through&nbsp;&nbsp;"
        			+ clsCreateHTMLFormFields.TDTextBox(
        				"NextContactEndDate", 
        				clsDateAndTimeConversions.now("M/d/yyyy"),
        				10, 
        				10, 
        				""
        				) 
        				+ SMUtilities.getDatePickerString("NextContactEndDate", getServletContext())
        				);

        	out.println("</TR>");
        	
        	//Sort By
        	out.println("<TR><TD ALIGN=CENTER><H3>Sort By </H3></TD>");
	    		out.println("<TD><SELECT NAME=\"SelectedSortOrder1\">");
					out.println("<OPTION VALUE=\"Customer Name\" SELECTED>Customer name");
					out.println("<OPTION VALUE=\"Salesperson\">Salesperson");
					out.println("<OPTION VALUE=\"Origination Date\">Origination date");
				out.println("</SELECT>");
        		out.println("&nbsp; <B>then</B> &nbsp; <SELECT NAME=\"SelectedSortOrder2\">");
        			out.println("<OPTION VALUE=\"" + SMBidEntry.ParamObjectName + " Date\" SELECTED>" + SMBidEntry.ParamObjectName + " date and time");
        			out.println("<OPTION VALUE=\"Last Contact Date\">Last contact date");
        			out.println("<OPTION VALUE=\"Next Contact Date\">Next contact date");
        		out.println("</SELECT>");
        		out.println("</TD>");
        	out.println("</TR>");
        	
        	//Status
        	out.println("<TR><TD ALIGN=CENTER><H3>" + SMBidEntry.ParamObjectName + " Status</H3></TD>");
        	out.println("<TD>" + 
        				"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusPending\" VALUE=1 CHECKED>Pending&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusSuccessful\" VALUE=1>Successful&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusUnsuccessful\" VALUE=1>Unsuccessful&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusInactive\" VALUE=1>Inactive</LABEL>" + 
        				"</TD></TR>");

	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	//out.println ("<INPUT TYPE=CHECKBOX NAME=\"ShowSummaryOnly\" VALUE=1>Check here to show summary only.");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("[1579265730] Error in BidListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
