package smic;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicshipvias;
import ServletUtilities.clsDatabaseFunctions;

public class ICEditPOShipViasSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "PO Ship Via";
	private static String sCalledClassName = "ICEditPOShipViasEdit";
	private String sDBID = "";
	private String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ICEditPOShipVias)){
	    	return;
	    }

		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		title = "Edit " + sObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	  //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPOShipVias) 
	    		+ "\">Summary</A><BR><BR>");
		
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." 
	    		+ sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
	    out.println("Ship via code and name:");
	    String sSQL = "";
		try{
	        sSQL = "SELECT * FROM"
	        	+ " " + SMTableicshipvias.TableName
	        	+ " ORDER BY"
	        	+ " " + SMTableicshipvias.sshipviacode;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID, 
	        	"MySQL", 
	        	this.toString() + ".doPost - user: " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME)
	        );
	     	out.println ("<SELECT NAME=\"" + sObjectName + "\">" );
        	
        	while (rs.next()){
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTableicshipvias.sshipviacode) + "\">" +
        					rs.getString(SMTableicshipvias.sshipviacode) + " - " + 
        					rs.getString(SMTableicshipvias.sshipvianame);
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println ("Error reading classes with SQL: " + sSQL + " - " + ex.getMessage());
		}
		//Display text boxes:
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='height: 0.24in'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + 
			" New PO Ship Via Code To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			Integer.toString(SMTableicshipvias.sshipviacodeLength) + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Must be " + Integer.toString(SMTableicshipvias.sshipviacodeLength)
			+ " alpha-numerical characters or less." + 
			"</P>";
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
