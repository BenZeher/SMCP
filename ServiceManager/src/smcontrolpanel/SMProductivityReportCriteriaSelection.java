package smcontrolpanel;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;


public class SMProductivityReportCriteriaSelection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMProductivityReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Productivity Report";
	    String subtitle = "listing criterias";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMProductivityReportGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	Calendar c = Calendar.getInstance();
        	//set start time to the first month of current month
        	c.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(System.currentTimeMillis()));
        	out.println("<TR><TD ALIGN=CENTER><H3>Invoice Starting Date </H3></TD>");
       		out.println("<TD>"
    				+ clsCreateHTMLFormFields.TDTextBox(
    						"StartingDate", 
    						clsDateAndTimeConversions.utilDateToString((new Date(c.getTimeInMillis())),"M/d/yyyy"), 
    						10, 
    						10, 
    						""
    					) 
    				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
    				+ "</TD>");
        	out.println("</TR>");
        	
        	c.setTimeInMillis(SMUtilities.FindLastDayOfMonth(System.currentTimeMillis()));
        	out.println("<TR><TD ALIGN=CENTER><H3>Invoice Ending Date </H3></TD>");
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
        	
        	try{ 
    		    //Location List
    	        String sSQL = SMMySQLs.Get_Locations_SQL();
    	        //System.out.println("Location SQL: " + sSQL);
    	        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
    	    	out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Location</B></FONT></TD><TD VALIGN=CENTER>");
	        	//display available status. 
	        	ArrayList<String> alPayTypes = new ArrayList<String>(0);
	        	while (rsLocations.next()){
	        		alPayTypes.add("<INPUT TYPE=CHECKBOX NAME=!1!" + rsLocations.getString(SMTablelocations.sLocation) + " VALUE=0>" + rsLocations.getString(SMTablelocations.sLocation) + " - " + rsLocations.getString(SMTablelocations.sLocationDescription));
	        	}
	        	rsLocations.close();
	        	out.println(SMUtilities.Build_HTML_Table(4, alPayTypes, 0, false));
		        out.println ("</TD></TR>");
    	    
		        //Service Type List
    	        sSQL = SMMySQLs.Get_Distinct_Servicetypes_SQL();
    	        //System.out.println("Servicetype SQL: " + sSQL);
    	        ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
    	    	out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Service Type</B></FONT></TD><TD VALIGN=CENTER>");
	        	//display available status.
	        	ArrayList<String> alServiceTypes = new ArrayList<String>(0);
	        	while (rsServiceTypes.next()){
	        		if(rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
	        			alServiceTypes.add("<INPUT TYPE=CHECKBOX NAME=!2!" + rsServiceTypes.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode) 
		        		+ " VALUE=0>" + rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName) + "");
	        		}
	        	}
	        	rsServiceTypes.close();
	        	out.println(SMUtilities.Build_HTML_Table(4, alServiceTypes, 0, false));
		        out.println ("</TD></TR>");
    	    
		        //Item Category List
		        sSQL = "SELECT "
					+ SMTableiccategories.sCategoryCode
					+ ", " + SMTableiccategories.sDescription
					+ " FROM " + SMTableiccategories.TableName
					+ " ORDER BY " + SMTableiccategories.sCategoryCode
					;
		        if (!SMUtilities.getICImportFlag(sDBID, getServletContext())){
		        	/*TBDL
		        	sDatabaseType = "PERVASIVE";
		        	sSQL = SMACCPACSQLs.Get_IC_Category_List_SQL();*/
		        }
    	        ResultSet rsItemCategories = clsDatabaseFunctions.openResultSet(
    	        		sSQL, 
    	        		getServletContext(), 
    	        		sDBID, "MySQL", 
    	        		"smcontrolpanel.SMProductivityReportCriteriaSelection");
    	    	out.println("<TR><TD VALIGN=CENTER><FONT SIZE=4><B>Item Categories</B></FONT></TD><TD VALIGN=CENTER>");
            	out.println("<TABLE WIDTH=100% BORDER=0>");
    	        //make this criterion optional
    	        out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=CheckItemCategories VALUE=0>Check item categories</TD></TR>");
    	        out.println("<TR><TD><HR></TD></TR>");
	        	//display available status.
	        	ArrayList<String> alItemCategories = new ArrayList<String>(0);
	        	while (rsItemCategories.next()){
	        		//TBDL
	        		//if (sDatabaseType.compareToIgnoreCase("MySQL") == 0){
		        		alItemCategories.add(
		        			"<INPUT TYPE=CHECKBOX NAME=!3!" 
		        			+ rsItemCategories.getString(
		        			SMTableiccategories.TableName + "." + SMTableiccategories.sCategoryCode) 
		        			+ " VALUE=0 CHECKED>" 
		        			+ rsItemCategories.getString(
		        			SMTableiccategories.TableName + "." + SMTableiccategories.sCategoryCode)
		        		);
	        		//}else{
	        		//alItemCategories.add(
	        		//	"<INPUT TYPE=CHECKBOX NAME=!3!" 
	        		//	+ rsItemCategories.getString(
	        		//	ICCATG.TableName + "." + ICCATG.sCategoryCode) 
	        		//	+ " VALUE=0 CHECKED>" 
	        		//	+ rsItemCategories.getString(
	        		//	ICCATG.TableName + "." + ICCATG.sCategoryCode)
	        		//);
	        		//}
	        	}
	        	rsItemCategories.close();
	        	out.println("<TR><TD>");
	        	out.println(SMUtilities.Build_HTML_Table(12, alItemCategories, 0, false));
		        out.println("</TD></TR></TABLE>");
		        out.println("</TD></TR>");
		        
    	    }catch (SQLException ex){
    	    	//catch SQL exceptions
    	    	System.out.println("Error when creating and displaying location and service type list.");
    	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
    	    	System.out.println("SQL: " + ex.getSQLState());
    	    }
    	    
	        out.println("</TABLE><BR><BR>");
	        
        	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
    	    out.println(clsCreateHTMLFormFields.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
        	out.println("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in SMProductivityReportCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
}
