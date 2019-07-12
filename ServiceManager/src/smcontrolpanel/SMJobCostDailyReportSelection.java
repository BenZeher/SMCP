package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMJobCostDailyReportSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMJobCostDailyReport
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
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
    					+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    boolean bSelectByCategory = false;
	    if (clsManageRequestParameters.get_Request_Parameter("SelectByCategory", request).trim().compareToIgnoreCase("yes") == 0){
	    	bSelectByCategory = true;
	    }
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    String title = "Job Cost Daily Report";
	    String subtitle = "listing criterias";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMJobCostDailyReport)
		    	+ "\">Summary</A><BR>");
	    
	    if (bSelectByCategory){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
		    		+ SMUtilities.getFullClassName(this.toString())
		    		+ "?SelectByCategory=no"
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Select by individual mechanic</A><BR>");	    	
	    }else{
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
		    		+ SMUtilities.getFullClassName(this.toString())
		    		+ "?SelectByCategory=yes"
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Select by locations and mechanic types</A><BR>");
	    }

    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMJobCostDailyReportGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
    	if (bSelectByCategory){
    		out.println("<INPUT TYPE=HIDDEN NAME=SelectByCategory VALUE=\"" + "yes" + "\">");
    	}else{
    		out.println("<INPUT TYPE=HIDDEN NAME=SelectByCategory VALUE=\"" + "no" + "\">");
    	}
    	
    	out.println("<TABLE BORDER=1 WIDTH=100% CELLPADDING=1>");
    	
		String sDefaultStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		String sDefaultEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Starting with order date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox("StartingDate", sDefaultStartDate, 10, 10, "") 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "</TD>");
		out.println("</TR>");
		
		out.println("<TR>");
		out.println("<TD>" + "<B>Ending with (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox("EndingDate", sDefaultEndDate, 10, 10, "") 
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())
				+ "</TD>");
		out.println("</TR>");
		
		if (bSelectByCategory){
			createCategorySelections(out, sDBID, sUserID, sUserFullName);
		}else{
			createMechanicSelections(out, sDBID, sUserID, sUserFullName);
		}
			    
		out.println("<TR>");
		out.println("<TD>" + "<B>Suppress details:</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDCheckBox("SuppressDetails", false, "Check this to ONLY print the daily and overall totals") + "</TD>");
		out.println("</TR>");
	    
        out.println("</TABLE>");
        
    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
	    //out.println(SMUtilities.TDCheckBox("CheckSubtotalOnly", false, "<B>Show subtotals for mechanics only.</B>"));
    	out.println("</FORM>");
		    
	    out.println("</BODY></HTML>");
	}
	private void createMechanicSelections(PrintWriter pwOut, String sDBID, String sUserID, String sUserFullName){
    	try{ 
	        //Mechanic List
	        String SQL = "SELECT * FROM " + SMTablemechanics.TableName
	        	+ " ORDER BY " + SMTablemechanics.sMechInitial;
	        //System.out.println("Item Categories SQL: " + sSQL);
	        ResultSet rsMechanics = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID, 
	        	"MySQL", 
	        	SMUtilities.getFullClassName(this.toString()) 
	        		+ ".createMechanicSelections - user: " + sUserID
	        		+ " - "
	        		+ sUserFullName);
	        pwOut.println("<TR><TD VALIGN=CENTER><B>Mechanics</B></FONT></TD><TD VALIGN=CENTER>");
	        pwOut.println("<TABLE WIDTH=100% BORDER=0>");

        	ArrayList <String> alMechanics = new ArrayList<String>(0);
        	while (rsMechanics.next()){
       			alMechanics.add("<INPUT TYPE=CHECKBOX NAME=MechCheckbox" 
       				+ rsMechanics.getString(SMTablemechanics.sMechInitial) + " VALUE=0 >" 
       				+ rsMechanics.getString(SMTablemechanics.sMechInitial) + " - "
       				+ rsMechanics.getString(SMTablemechanics.sMechFullName)
       				);
        	}
        	rsMechanics.close();
        	pwOut.println("<TR><TD>");
        	pwOut.println(SMUtilities.Build_HTML_Table(3, alMechanics, 0, false));
        	pwOut.println("</TD></TR></TABLE>");
        	pwOut.println("</TD></TR>");
	        
	    }catch (SQLException ex){
	    	//catch SQL exceptions
	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
	    	System.out.println("SQL: " + ex.getSQLState());
	    }

	}
	private void createCategorySelections(PrintWriter pwOut, String sDBID, String sUserID, String sUserFullName){
    	try{ 
	        //Location list
	        String SQL = "SELECT"
	        	+ " " + SMTablelocations.sLocation
	        	+ ", " + SMTablelocations.sLocationDescription
	        	+ " FROM " + SMTablelocations.TableName
	        	+ " ORDER BY " + SMTablelocations.sLocation;
	        //System.out.println("Item Categories SQL: " + sSQL);
	        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID, 
	        	"MySQL", 
	        	SMUtilities.getFullClassName(this.toString())+ ".createCategorySelections - user: " 
	        		+ sUserID
	        		+ " - "
	        		+ sUserFullName
	        		);
	        pwOut.println("<TR><TD VALIGN=CENTER><B>Mechanic default locations</B></FONT></TD><TD VALIGN=CENTER>");
	        pwOut.println("<TABLE WIDTH=100% BORDER=0>");

        	ArrayList <String> alLocations = new ArrayList<String>(0);
        	while (rsLocations.next()){
       			alLocations.add("<INPUT TYPE=CHECKBOX NAME=LocationCheckBox"
       				+ rsLocations.getString(SMTablelocations.sLocation)
       				+ " VALUE=0" + " >" 
       				+ rsLocations.getString(SMTablelocations.sLocation) + " - "
       				+ rsLocations.getString(SMTablelocations.sLocationDescription)
       				);
        	}
        	rsLocations.close();
        	pwOut.println("<TR><TD>");
        	pwOut.println(SMUtilities.Build_HTML_Table(3, alLocations, 0, false));
        	pwOut.println("</TD></TR></TABLE>");
        	pwOut.println("</TD></TR>");
	        
	    }catch (SQLException ex){
	    	//catch SQL exceptions
	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
	    	System.out.println("SQL: " + ex.getSQLState());
	    }

        //Service type list
	    try{
	        String SQL = "SELECT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
					+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName
					+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.id
					+ " FROM " + SMTableorderheaders.TableName
					+ " LEFT JOIN " + SMTableservicetypes.TableName + " ON "
					+ SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " = "
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
					+ " GROUP BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode 
					+ " ORDER BY " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName + " DESC";
	        
	        ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID, 
	        	"MySQL", 
	        	SMUtilities.getFullClassName(this.toString())+ ".createCategorySelections - user: " 
	        		+ sUserID + " - "+ sUserFullName);
	        pwOut.println("<TR><TD VALIGN=CENTER><B>Mechanic default service types</B></FONT></TD><TD VALIGN=CENTER>");
	        pwOut.println("<TABLE WIDTH=100% BORDER=0>");
	
	    	ArrayList <String> alServiceTypes = new ArrayList<String>(0);
	    	while (rsServiceTypes.next()){
	   			if(rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
	   				alServiceTypes.add("<INPUT TYPE=CHECKBOX NAME=ServiceTypeCheckbox" 
	   		   				+ rsServiceTypes.getString(SMTableservicetypes.sCode) + " VALUE=0 >" 
	   		   				+ rsServiceTypes.getString(SMTableservicetypes.sName)
	   		   				);	
	   			}		
	    	}
	    	rsServiceTypes.close();
	    	pwOut.println("<TR><TD>");
	    	pwOut.println(SMUtilities.Build_HTML_Table(2, alServiceTypes, 0, false));
	    	pwOut.println("</TD></TR></TABLE>");
	    	pwOut.println("</TD></TR>");
	        
	    }catch (SQLException ex){
	    	//catch SQL exceptions
	    	System.out.println("Error: " + ex.getErrorCode() + " - " + ex.getMessage());
	    	System.out.println("SQL: " + ex.getSQLState());
	    }
	}
}
