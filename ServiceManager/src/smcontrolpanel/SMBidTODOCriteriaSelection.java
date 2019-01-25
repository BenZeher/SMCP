package smcontrolpanel;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;

import SMDataDefinition.SMTableprojecttypes;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;


public class SMBidTODOCriteriaSelection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.SMPendingBidsReport
				)
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
	    String title = "Pending " + SMBidEntry.ParamObjectName + "s";
	    String subtitle = "select criteria";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
/*
    	String sCurrentURL;
    	sCurrentURL = SMUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
*/
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMBidTODOGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	
        	//Starting salesperson
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

    		//Project Type
    		ArrayList<String> alValues = new ArrayList<String>(0);
    		ArrayList<String> alTexts = new ArrayList<String>(0);
    		sSQL = SMMySQLs.Get_Project_Type_SQL();
    		ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
    		alValues.add("0");
    		alTexts.add("All Types");
    		while (rsProjectTypes.next()){
    			alValues.add(rsProjectTypes.getString(SMTableprojecttypes.iTypeId));
    			alTexts.add(rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc));
    		}
    		rsProjectTypes.close();
    		
    		if (alValues.size() > 0){
    			out.println("<TR><TD ALIGN=CENTER><H3>Project Type</H3></TD><TD>" + clsCreateHTMLFormFields.TDDropDownBox("ProjectType", alValues, alTexts) + "</TD></TR>");
    		}else{
    			//no project type
    			out.println("<TR><TD ALIGN=CENTER><B>Project Type</B></TD><TD>NO project type in system.</TD>");
    			out.println("<INPUT TYPE=HIDDEN NAME=\"ProjectType\" VALUE=\"0\">");
    		}
    		
        	//Sort By
        	out.println("<TR><TD ALIGN=CENTER><H3>Sort By </H3></TD>");
        		out.println("<TD><SELECT NAME=\"SelectedSortOrder\">");
        			out.println("<OPTION VALUE=\"BidDate\">" + SMBidEntry.ParamObjectName.toLowerCase() + " date and time");
        			out.println("<OPTION VALUE=\"Salesperson\">Salesperson");        			
        		out.println("</TD>");
        	out.println("</TR>");
        	
        	//select in/active
        	out.println("<TR><TD ALIGN=CENTER><H3>" + SMBidEntry.ParamObjectName + " status</H3></TD>");
        	out.println("<TD>" + 
        				"<INPUT TYPE=\"CHECKBOX\" NAME=\"StatusPending\" VALUE=1 CHECKED>Pending&nbsp;&nbsp;" + 
    					"<INPUT TYPE=\"CHECKBOX\" NAME=\"StatusSuccessful\" VALUE=1>Successful&nbsp;&nbsp;" + 
    					"<INPUT TYPE=\"CHECKBOX\" NAME=\"StatusUnsuccessful\" VALUE=1>Unsuccessful&nbsp;&nbsp;" + 
    					"<INPUT TYPE=\"CHECKBOX\" NAME=\"StatusInactive\" VALUE=1>Inactive" + 
        				"</TD></TR>");

        	out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
        	out.println ("<BR><HR><B>This list includes all " + SMBidEntry.ParamObjectName.toLowerCase() + "s which do NOT have an \"actual bid date\" and which are also limited by the selected criteria above.</B>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in BidListCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
