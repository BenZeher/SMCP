package smcontrolpanel;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMBidReportCriteriaSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String SALESGROUP_PARAM = "SALESGROUPCODE";
	public static final String SALESGROUP_PARAM_SEPARATOR = ",";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	   
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.SMSalesLeadReport
				)
		){
			return;
		}
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
	    	+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME)
	    ;
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String title = SMBidEntry.ParamObjectName + " Report";
	    String subtitle = "select criterias";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
    	
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    //This prints out the JavaScript onto the HTML Page.
	    out.println(checker);
	    try {

        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMBidReportGenerate\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<TABLE BORDER=10 CELLPADDING=10>");
        	
        	out.println("<TR><TD ALIGN=CENTER><H3>Salesperson </H3></TD><TD>");
    		
			String sSQL = "SELECT "
				+ "DISTINCTSALESPEOPLE." + SMTablebids.ssalespersoncode
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ " FROM ("
				+ "SELECT DISTINCT " + SMTablebids.ssalespersoncode
				+ " FROM " + SMTablebids.TableName
				+ " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode
				+ ") AS DISTINCTSALESPEOPLE"
				+ " LEFT JOIN " + SMTablesalesperson.TableName + " ON"
				+ " DISTINCTSALESPEOPLE." + SMTablebids.ssalespersoncode 
				+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, 
													 getServletContext(),
													 sDBID,
													 "MySQL",
													 this.toString() + ".doPost - User: " + sUserID
													 + " - "
													 + sUserFullName
													 );
			ArrayList<String> sSalespersonList = new ArrayList<String>(0);
			//sSalespersonList.add((String) "<INPUT TYPE=CHECKBOX " + " checked=\"Yes\"" 
        	//		+ " NAME=\"SALESPERSONN/A" +  "\">"
        	//		+ "N/A"
        	//		);
			boolean bCheckSalesperson;
			if (request.getParameter("CheckSalesperson") != null){
				bCheckSalesperson = new Boolean(request.getParameter("CheckSalesperson"));
			}else{
				bCheckSalesperson = true;
			}
			String sFirstName = "";
			String sLastName = "";
			while (rs.next()){
				if (rs.getString(SMTablesalesperson.sSalespersonFirstName) == null){
					sFirstName = "";
				}else{
					sFirstName = rs.getString(SMTablesalesperson.sSalespersonFirstName).trim();
				}
				if (rs.getString(SMTablesalesperson.sSalespersonLastName) == null){
					sLastName = "";
				}else{
					sLastName = rs.getString(SMTablesalesperson.sSalespersonLastName).trim();
				}
				
				sSalespersonList.add((String) "<LABEL><INPUT TYPE=CHECKBOX" + (bCheckSalesperson?" checked":"")  
	        			+ " NAME=\"SALESPERSON" 
	        			+ rs.getString(SMTablebids.ssalespersoncode) 
	        			+ "\" CLASS=\"sales\""
	        			+ ">" 
	        			+ rs.getString(SMTablebids.ssalespersoncode)
	        			+ "&nbsp;" + sFirstName
	        			+ "&nbsp;" + sLastName 
	        			+ "</LABEL>"
	        			);
	    	}
	        rs.close();
	        //out.println(SMUtilities.Build_HTML_Table(5, sSalespersonList,1,true));
	        
	        out.println(SMUtilities.Build_HTML_Table(5, sSalespersonList, 100, 0, true ,true));
    	        
    	
	        out.println("<BUTTON TYPE=BUTTON ONCLICK=\" uncheck() \">Click to UN-Check all salespersons</BUTTON>");		
	        out.println("<BUTTON TYPE=BUTTON ONCLICK=\" check() \">Click to Check all salespersons</BUTTON>");
		        
    		out.println("<BR><BR>*Salespersons listed are ALL salespersons included in the " + SMBidEntry.ParamObjectName.toLowerCase() + " data "
    				+ "- some may no longer be in the salespersons table, and those may appear with no names.");
    		out.println("</TD></TR>");
    		
    		
    		//Sales Group 
    		
    		out.println("<TR>");
    		out.println("<TD><B>Include sales groups:<B></TD>");
    		out.println("<TD>");
    		
    		
    		
    		String sSalesGroupSQL = "SELECT DISTINCT " + SMTablebids.TableName + "." + SMTablebids.lsalesgroupid 
    				+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode
    				+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
    					+ " FROM " + SMTablebids.TableName
    				+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON "
    				+ SMTablebids.TableName + "." + SMTablebids.lsalesgroupid + " = "
    				+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
    				+ " ORDER BY " + SMTablebids.TableName + "." + SMTablebids.lsalesgroupid ;
    		try{
    		ResultSet rsSalesGroup = clsDatabaseFunctions.openResultSet(sSalesGroupSQL, getServletContext(), sDBID);
    			while(rsSalesGroup.next()){
    				String sSalesGroupCode = rsSalesGroup.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode);
    				String sSalesGroupDesc = rsSalesGroup.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc);
    				if (sSalesGroupCode == null){
    					sSalesGroupCode = "(BLANK)";
    				}
    				if (sSalesGroupDesc == null){
    					sSalesGroupDesc = "(BLANK)";
    				}

    				out.println(
    						"  <LABEL NAME = \"" + "SALESGROUPLABELSALES" + sSalesGroupCode + " \" >"
    						+  "  <INPUT TYPE=CHECKBOX NAME=\"" + SALESGROUP_PARAM
    						+ sSalesGroupCode
    						+ SALESGROUP_PARAM_SEPARATOR
    						+ Integer.toString(rsSalesGroup.getInt(SMTablebids.TableName + "." + SMTablebids.lsalesgroupid))						   
    						+ "\" CHECKED width=0.25>" 
    						+ sSalesGroupCode + " - " + sSalesGroupDesc
    						+ "</LABEL>"
    						+ "<BR>");
    			}
    			rs.close();
    		}catch (SQLException e){
    			out.println("Could not read sales group table - " + e.getMessage());
    		}
    		
    		out.println("</TD>");
    		out.println("</TR>");
    		
	
    		
    		
        	
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
        	

    		//Order dates:
    		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
    		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
    		out.println("<TR>");
    		out.println("<TD><B>Date range (choose 'Previous Month', 'Current Month',<BR>or enter a date range"
    				+ " in mm/dd/yyyy format):</B></TD>");
    		out.println("<TD>");
    		out.println("<LABEL><input type=\"radio\" name=\"DateRange\" value=\"PreviousMonth\"> Previous month<BR></LABEL>");
    		out.println("<LABEL><input type=\"radio\" name=\"DateRange\" value=\"CurrentMonth\" checked> Current month<BR></LABEL>");
    		out.println("<LABEL><input type=\"radio\" name=\"DateRange\" value=\"SelectedDates\">&nbsp;</LABEL>");
    		
    		out.println(
    			"Starting:&nbsp;" 
    				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "")
    				//+ "<INPUT TYPE=TEXT NAME=\"" + "StartingDate" + "\" "
    				//+ "onclick='scwShow(this,event);' "
    				//+ "VALUE=\"" + sDefaultStartDate 
    				//	+ "\" SIZE = " + 10 + " MAXLENGTH = " + 10 + ">" + ""
    				//Date picker icon:
    				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
    				+ "&nbsp;&nbsp;Ending:&nbsp;" + clsCreateHTMLFormFields.TDTextBox(
    						"EndingDate", sDefaultEndDate, 10, 10, "")
    				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
    			
    		);
    		out.println("</TD>");
    		out.println("</TR>");
    		
        	//Show salesperson totals only:
        	out.println("<TR><TD ALIGN=CENTER><B>Suppress individual " + SMBidEntry.ParamObjectName.toLowerCase() +"s?</B></TD>");
           	out.println("<TD>" + 
    				"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"OnlyShowSubtotals\" VALUE=0 >Only show salesperson subtotals</LABEL>" + 
    				"</TD></TR>");
    		
        	//Status
        	out.println("<TR><TD ALIGN=CENTER><B>" + SMBidEntry.ParamObjectName + " Status</B></TD>");
        	out.println("<TD>" + 
        				"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusPending\" VALUE=1 CHECKED>Pending&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusSuccessful\" VALUE=1 CHECKED>Successful&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusUnsuccessful\" VALUE=1 CHECKED>Unsuccessful&nbsp;&nbsp;</LABEL>" + 
    					"<LABEL><INPUT TYPE=\"CHECKBOX\" NAME=\"StatusInactive\" VALUE=1>Inactive</LABEL>" + 
        				"</TD></TR>");
        	
	        out.println ("</TABLE><BR><BR>");
	        
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
        	out.println ("</FORM>");
		    
	    } catch (Exception ex) {
	        // handle any errors
	    	System.out.println("Error in BidReportCriteriaSelection class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	    }
 
	    out.println("</BODY></HTML>");
	}
	
	//JavaScript to Check and Un-Check all salespersons
	String checker = "<script>\n" + 
			"function uncheck(){\n" + 
			"    var list = document.getElementsByClassName('sales');\n" + 
			"    for (var i = 0; i < list.length; i++) list[i].checked = false;\n" + 
			"}\n" + 
			"\n" + 
			"function check(){\n" + 
			"    var list = document.getElementsByClassName('sales');\n" + 
			"    for (var i = 0; i < list.length; i++) list[i].checked = true;\n" + 
			"}\n" + 
			"\n" + 
			"</script>";
}
