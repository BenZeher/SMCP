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

import SMClasses.MySQLs;
import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

public class SMListSecurityBy extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMListSecurityLevels))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sListBy = request.getParameter("ListBy");
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String title = "List Security By " + sListBy;
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    
		   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMLISTSECURITYBY, "REPORT", "SMListSecurityby:" + sListBy, "[1564757440]");
	    
	    
	    // Get the recordset of security information:
		try{
	        String sSQL = MySQLs.Get_Security_List_SQL(sListBy);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

	        String sHeading1 = "";
	        String sHeading2 = "";
	        String sHeading3 = "";
	        String sHeading4 = "";
	        String sHeading5 = "";
	        String sField1 = "";
	        String sField2 = "";
	        String sField3 = "";
	        String sField4 = "";
	        String sField5 = "";
	        
	        // If it's 'list by user'
	        if(sListBy.compareTo("User") == 0){
	        	sHeading1 = "User";
	        	sHeading2 = "First Name";
	        	sHeading3 = "Last Name";
	        	sHeading4 = "Function";
	        	sHeading5 = "Group";
	        	sField1 = SMTableusers.sUserName;
	        	sField2 = SMTableusers.sUserFirstName;
	        	sField3 = SMTableusers.sUserLastName;
	        	sField4 = SMTablesecuritygroupfunctions.sFunction;
	        	sField5 = SMTablesecuritygroupfunctions.sGroupName;
	        }
        	if (sListBy.compareToIgnoreCase("Group") == 0){
	        	sHeading1 = "Group";
	        	sHeading2 = "Function";
	        	sHeading3 = "User";
	        	sHeading4 = "First Name";
	        	sHeading5 = "Last Name";
	        	sField1 = SMTablesecuritygroupfunctions.sGroupName;
	        	sField2 = SMTablesecuritygroupfunctions.sFunction;
	        	sField3 = SMTableusers.sUserName;
	        	sField4 = SMTableusers.sUserFirstName;
	        	sField5 = SMTableusers.sUserLastName;

        	}
        	if (sListBy.compareToIgnoreCase("Function") == 0){
        		//List by function:
	        	sHeading2 = "Function";
	        	sHeading1 = "Group";
	        	sHeading3 = "User";
	        	sHeading4 = "First Name";
	        	sHeading5 = "Last Name";
	        	sField2 = SMTablesecuritygroupfunctions.sFunction;
	        	sField1 = SMTablesecuritygroupfunctions.sGroupName;
	        	sField3 = SMTableusers.sUserName;
	        	sField4 = SMTableusers.sUserFirstName;
	        	sField5 = SMTableusers.sUserLastName;
        	}
        	if (sListBy.compareToIgnoreCase("GroupList") == 0){
        		//List by function:
	        	sHeading1 = "Group";
	        	sHeading2 = "";
	        	sHeading3 = "";
	        	sHeading4 = "";
	        	sHeading5 = "";
	        	sField1 = SMTablesecuritygroups.sSecurityGroupName;
	        	sField2 = "";
	        	sField3 = "";
	        	sField4 = "";
	        	sField5 = "";
        	}
        	if (sListBy.compareToIgnoreCase("FunctionList") == 0){
        		//List by function:
	        	sHeading1 = "Function";
	        	sHeading2 = "Function ID";
	        	sHeading3 = "";
	        	sHeading4 = "";
	        	sHeading5 = "";
	        	sField1 = SMTablesecurityfunctions.sFunctionName;
	        	sField2 = SMTablesecurityfunctions.iFunctionID;
	        	sField3 = "";
	        	sField4 = "";
	        	sField5 = "";
        	}
	        	
        	out.println(SMUtilities.getMasterStyleSheetLink());
        	
	        // Set up table and headings:
	    	out.println("<TABLE BGCOLOR=\"#FFFFFF\" WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\"><TR>");
	    	out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
	    	if (sHeading1.length() !=0){
	    		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">" + sHeading1 + "</TD>");
	    	}
	    	if (sHeading2.length() !=0){
	    		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">"  + sHeading2 + "</TD>");	
	    	}
	    	if (sHeading3.length() !=0){
	    		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">" + sHeading3 + "</TD>");
	    	}
	    	if (sHeading4.length() !=0){
	    		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">"  + sHeading4 + "</TD>");	
	    	}
	    	if (sHeading5.length() !=0){
	    		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">" + sHeading5 + "</TD>");
	    	}
	    	out.println("</TR>");
	    	
	    	int iCount=0;
        	while (rs.next()){
        		if(iCount % 2 == 0) {
        	    	out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
        		}else {
        	    	out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
        		}
        		if (sField1.length() !=0 ){
        			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rs.getString(sField1) + "</TD>");
        		}
        		if (sField2.length() !=0 ){
        			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rs.getString(sField2) + "</TD>");
        		}
        		if (sField3.length() !=0 ){
        			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(sField3) + "</TD>");
        		}
        		if (sField4.length() !=0 ){
        			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(sField4) + "</TD>");
        		}
        		if (sField5.length() !=0 ){
        			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rs.getString(sField5) + "</TD>");
        		}
        		out.println("</TR>");
        		iCount++;
        	}
        	rs.close();
	        out.println ("<BR>");
	        
	        //End the table:
	        out.println("</TABLE>");
		}catch (SQLException ex){
	    	System.out.println("Error in SMManagePasswords class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

	    
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}