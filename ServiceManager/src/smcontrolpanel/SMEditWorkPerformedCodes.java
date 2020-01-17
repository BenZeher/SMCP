package smcontrolpanel;

import SMDataDefinition.SMTableworkperformedcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import SMDataDefinition.SMTableservicetypes;

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

public class SMEditWorkPerformedCodes extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Work Performed Code";
	private static String sCalledClassName = "SMEditWorkPerformedCodesEdit";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditWorkPerformedCodes))
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
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = MySQLs.Get_WorkPerformedCode_List_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + sObjectName + "\">" );
        	
        	while (rs.next()){
        		
        		//Build the OPTION VALUE out of the service type and the
        		//work performed code:
        		String sOptionValue = clsStringFunctions.PadLeft(
        				rs.getString(SMTableworkperformedcodes.sCode), 
        				" ", 
        				SMTableworkperformedcodes.sCodeLength
        				); 
        		
        		sOptionValue += clsStringFunctions.PadLeft(
        				rs.getString(SMTableworkperformedcodes.sWorkPerformedCode), 
        				" ", 
        				SMTableworkperformedcodes.sWorkPerformedCodeLength
        				);  
        		
        		sOutPut = "<OPTION VALUE=\"" + sOptionValue + "\">";
        		
        		sOutPut += rs.getString(SMTableworkperformedcodes.sCode).trim() + " - ";
        		sOutPut += rs.getString(SMTableworkperformedcodes.sWorkPerformedCode).trim() + " - ";
        		//Phrases can be very long, so we truncate it to fit in
        		//the list:
        		String sPhrase = "";
        		if (rs.getString(SMTableworkperformedcodes.sWorkPerformedPhrase) != null){
        			sPhrase = rs.getString(SMTableworkperformedcodes.sWorkPerformedPhrase);
        		}
        		if (sPhrase.length() > 75){
        			sPhrase = clsStringFunctions.StringLeft(sPhrase, 75) + "...";
        		}
        		//SPECIAL CODE HERE - trimmed the value of the phrase:
        		sOutPut += sPhrase;
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("[1579271814] Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='height: 0.24in'></P>\n";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='height: 0.24in'>\n";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">\n";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='height: 0.24in'></P>\n";
		sOutPut = sOutPut + 
			" New " + sObjectName + " To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			SMTableworkperformedcodes.sWorkPerformedCodeLength + 
			" STYLE=\"width: 2.41in; height: 0.25in\"></P>\n";
		
		//Build a list of Service Types:
		sOutPut += "<P>Service Type For New " + sObjectName + ": ";
		try{
	        String sSQL = MySQLs.Get_Servicetypes_SQL();
	        ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        sOutPut += "<SELECT NAME=\"NewServiceType\">";
        	
        	while (rsServiceTypes.next()){
        		//flag that there are multiple entries.
        		sOutPut += "<OPTION VALUE=\"" + rsServiceTypes.getString(SMTableservicetypes.sCode) + "\">";
        		sOutPut += rsServiceTypes.getString(SMTableservicetypes.sCode) + " - " + rsServiceTypes.getString(SMTableservicetypes.sName);
        	}
        	rsServiceTypes.close();
		}catch (SQLException ex){
	    	System.out.println("[1579271821] Error in SMEditUsers class - reading service types!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		sOutPut += "</P>\n";
		
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}