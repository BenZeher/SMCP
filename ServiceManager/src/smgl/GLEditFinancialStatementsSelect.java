package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglstatementforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditFinancialStatementsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static String BUTTON_SUBMIT_EDIT = "SubmitEdit";
	public static String BUTTON_SUBMIT_ADD = "SubmitAdd";
	public static String BUTTON_SUBMIT_DELETE = "SubmitDelete";
	
	private static String sObjectName = "Financial Statement";
	private static String sCalledClassName = "GLEditFinancialStatementsEdit";
	private String sDBID = "";
	private String sCompanyName = "";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.GLEditFinancialStatements //add new rights if necessary
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + sObjectName + "s";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
	    		sCompanyName));

		if (clsManageRequestParameters.get_Request_Parameter("Warning", request).compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>WARNING: " + clsManageRequestParameters.get_Request_Parameter("Warning", request) + "</B></FONT><BR>");
		}
		if (clsManageRequestParameters.get_Request_Parameter("Status", request).compareToIgnoreCase("") != 0){
			out.println("<BR><B>NOTE: " + clsManageRequestParameters.get_Request_Parameter("Status", request) + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFinancialStatements)
				+ "\">Summary</A><BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smgl." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableglstatementforms.TableName
	        	+ " ORDER BY " + SMTableglstatementforms.sname
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + SMTableglstatementforms.lid + "\">" );
        	
        	while (rs.next()){
        		out.println ("<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTableglstatementforms.lid)) + "\">"
        			+ rs.getString(SMTableglstatementforms.sname)
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("Error [1502975296] getting list of statement forms - " + ex.getMessage());
		}
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='" + BUTTON_SUBMIT_EDIT + "' VALUE='Edit Selected " + sObjectName + "'" 
			+ "></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + BUTTON_SUBMIT_DELETE + "' VALUE='Delete Selected " 
			+ sObjectName + "'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + BUTTON_SUBMIT_ADD + "' VALUE='Add New " 
			+ sObjectName + "'></P>";
		sOutPut = sOutPut + 
			" New check form To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			Integer.toString(SMTableglstatementforms.snamelength) + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> The maximum length for the name is " + 
			Integer.toString(SMTableglstatementforms.snamelength) + ".</P>";
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