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

import SMDataDefinition.SMTableworkorderdetailsheets;
import ServletUtilities.clsDatabaseFunctions;

public class SMDetailSheetSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static String sObjectName = "Detail Sheet";
	private static String sCalledClassName = "SMDetailSheetEdit";
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
				SMSystemFunctions.SMEditDetailSheets //add new rights if necessary
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

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableworkorderdetailsheets.TableName
	        	+ " ORDER BY " + SMTableworkorderdetailsheets.sname 
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + SMDetailSheetEdit.DETAIL_SHEET_ID + "\">" );
        	
        	while (rs.next()){
        		String sEntryFormLabel = "";
        		if (rs.getLong(SMTableworkorderdetailsheets.itype) == 1){
        			sEntryFormLabel = " (" + SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL + ")";
        		}
        		out.println ("<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTableworkorderdetailsheets.lid)) + "\">"
        			+ rs.getString(SMTableworkorderdetailsheets.sname) + sEntryFormLabel
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("Error getting list of detail sheets - " + ex.getMessage());
		}
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='" + SMDetailSheetEdit.BUTTON_SUBMIT_EDIT + "' VALUE='Edit Selected " + sObjectName + "'" 
			+ "></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SMDetailSheetEdit.BUTTON_SUBMIT_DELETE + "' VALUE='Delete Selected " 
			+ sObjectName + "'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='" + SMDetailSheetEdit.BUTTON_SUBMIT_ADD + "' VALUE='Add New " 
			+ sObjectName + "'></P>";
		sOutPut = sOutPut + 
			" New Detail Sheet To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			Integer.toString(SMTableworkorderdetailsheets.snamelength) + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Detail sheet name maximum length is " + 
			Integer.toString(SMTableworkorderdetailsheets.snamelength) + ".</P>";
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