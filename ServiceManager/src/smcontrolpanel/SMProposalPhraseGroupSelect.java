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

import SMDataDefinition.SMTableproposalphrasegroups;
import ServletUtilities.clsDatabaseFunctions;

public class SMProposalPhraseGroupSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String sObjectName = "Proposal Phrase Group";
	private static final String sCalledClassName = "SMProposalPhraseGroupEdit";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposalPhraseGroups //add new rights if necessary
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
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
	        	+ SMTableproposalphrasegroups.TableName
	        	+ " ORDER BY " + SMTableproposalphrasegroups.sid;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + SMTableproposalphrasegroups.sid + "\">" );
        	
        	while (rs.next()){
        		out.println ("<OPTION VALUE=\"" + rs.getString(SMTableproposalphrasegroups.sid) + "\">"
        			+ Integer.toString(rs.getInt(SMTableproposalphrasegroups.sid)) + " - "
        			+ rs.getString(SMTableproposalphrasegroups.sgroupname)
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("Error getting list of proposal phrase groups - " + ex.getMessage());
		}
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "'" 
			+ "></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " 
			+ sObjectName + "'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " 
			+ sObjectName + "'></P>";
		sOutPut = sOutPut + 
			" New Proposal Phrase To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			Integer.toString(SMTableproposalphrasegroups.sgroupnameLength) + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Proposal phrase group name maximum length is " + 
			Integer.toString(SMTableproposalphrasegroups.sgroupnameLength) + ".</P>";
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