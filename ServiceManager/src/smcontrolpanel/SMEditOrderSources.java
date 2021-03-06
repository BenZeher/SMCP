package smcontrolpanel;

import SMDataDefinition.SMTableordersources;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

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

public class SMEditOrderSources extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Order Source";
	private static final String sCalledClassName = "SMEditOrderSourcesEdit";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditOrderSources))
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
	        String sSQL = MySQLs.Get_OrderSource_List_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + sObjectName + "\">" );
        	
        	while (rs.next()){
        		String sDesc = "";
        		if (rs.getString(SMTableordersources.sSourceDesc) != null){
        			sDesc = rs.getString(SMTableordersources.sSourceDesc);
        		}
        		if (sDesc.length() > 75){
        			sDesc = clsStringFunctions.StringLeft(sDesc, 75) + "...";
        		}
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTableordersources.iSourceID) + "\">";
         		//Convenience phrases can be very long, so we truncate it to fit in
        		//the list:
        		sOutPut += rs.getString(SMTableordersources.iSourceID) +
        		//SPECIAL CODE HERE - trimmed the value of the phrase:
        		" - " + sDesc;
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("[1579270730] Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>\n";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'>\n";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">\n";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>\n";
		sOutPut = sOutPut + 
			" New Order Source ID To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			"4" + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Must be integer - 9999 or less." + 
			"</P>\n";
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