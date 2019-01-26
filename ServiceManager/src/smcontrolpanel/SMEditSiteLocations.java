package smcontrolpanel;

import SMDataDefinition.SMTablesitelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import SMDataDefinition.SMTablearcustomershiptos;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditSiteLocations extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Site Location";
	private static String sCalledClassName = "SMEditSiteLocationsEdit";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSiteLocations))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + sObjectName + "s";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    //String sOutPut = "";
	    
	    //Add drop down list of current records:
		try{
	        String sSQL = SMMySQLs.Get_SiteLocations_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        out.println("Current site locations:<BR>");
	     	out.println ("<SELECT NAME=\"" + sObjectName + "\">" );
        	
        	while (rs.next()){
        		//First, get the account number to put in the option values:
        		out.println("<OPTION VALUE=\"" + clsStringFunctions.PadLeft(rs.getString(SMTablesitelocations.sAcct), " ", SMTablesitelocations.sAcctLength) + 
        										 clsStringFunctions.PadLeft(rs.getString(SMTablesitelocations.sShipToCode), " ", SMTablesitelocations.sShipToCodeLength) + 
        										 clsStringFunctions.PadLeft(rs.getString(SMTablesitelocations.sLabel), " ", SMTablesitelocations.sLabelLength) + 
        					"\">");
        		out.println(rs.getString(SMTablesitelocations.sAcct).trim() + " - " + 
		        			rs.getString(SMTablesitelocations.sShipToCode).trim() + " - " +
		        			rs.getString(SMTablesitelocations.sLabel).trim());
        	}
        	rs.close();
	        	//End the drop down list:
        	
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new label and a confirmation:
		
		out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>" + 
					"<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'>" +
					"  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
		
		try{
	        String sSQL = SMMySQLs.Get_Customer_ShipTo_List_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

	        out.println("<BR><BR>Add A Site Location:<BR>");
	        //Create a small table:
	        out.println("<TABLE BORDER=12 CELLSPACING=2><TR>");
	        out.println("<TD align = right>Choose Customer Ship-To For New Label:</TD>");
	        out.println("<TD><SELECT NAME=\"CustomerShipTos\">");
        	
        	while (rs.next()){
        		
    	     	out.println("<OPTION VALUE=\"" + clsStringFunctions.PadLeft(rs.getString(SMTablearcustomershiptos.sCustomerNumber), " ", SMTablearcustomershiptos.sCustomerNumberLength) + //First, get the account number to put in the option values 
    	     									 clsStringFunctions.PadLeft(rs.getString(SMTablearcustomershiptos.sShipToCode), " ", SMTablearcustomershiptos.sShipToCodeLength) + //Next, get the ship to code
    	     				"\">");
    	     	out.println(rs.getString(SMTablearcustomershiptos.sCustomerNumber).trim() + " - " + 
		        			rs.getString(SMTablearcustomershiptos.sShipToCode).trim() + " - " +
		        			rs.getString(SMTablearcustomershiptos.sDescription).trim());
	        	
        	}
        	rs.close();
	        	//End the drop down list:
        	out.println("</SELECT</TD>");
        	out.println("<TD>&nbsp;</TD>");
        	out.println("</TR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

		out.println("<TR><TD align = right>New Label To Be Added:</TD>");
		out.println("<TD><INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			SMTablesitelocations.sLabelLength + 
			" STYLE=\"width: 2.41in; height: 0.25in\"></TD>");
		out.println("<TD>Maximum of " + SMTablesitelocations.sLabelLength + " characters.</TD>");
		out.println("</TABLE>");
		out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'><BR>");
		out.println("</FORM>");
		
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}